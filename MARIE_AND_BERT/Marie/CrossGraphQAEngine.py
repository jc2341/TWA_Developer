import json
import os
import time

import torch
from torch.nn.functional import one_hot
from transformers import BertTokenizer
from Marie.PubchemEngine import PubChemQAEngine
from Marie.OntoCompChem import OntoCompChemEngine
from Marie.OntoSpecies import OntoSpeciesQAEngine
from Marie.Ontokin import OntoKinQAEngine
from Marie.Util.CommonTools.NLPTools import NLPTools
from Marie.Util.Logging import MarieLogger
from Marie.Util.Models.CrossGraphAlignmentModel import CrossGraphAlignmentModel
from Marie.Util.location import DATA_DIR
from Marie.EntityLinking.ChemicalNEL import ChemicalNEL
from Marie.WikidataEngine import WikidataEngine


def normalize_scores(scores_list):
    normalized_scores = []
    for scores in scores_list:
        max_score = max(scores)
        scores = [score / max_score for score in scores]
        if len(scores) < 5:
            diff = 5 - len(scores)
            for i in range(diff):
                scores.append(-999)
        normalized_scores.append(scores)
    return normalized_scores


class CrossGraphQAEngine:
    """
    The cross graph QA engine will answer question across domains
    """

    def __init__(self):
        self.marie_logger = MarieLogger()
        self.pubchem_engine = PubChemQAEngine()
        self.ontochemistry_engine = OntoCompChemEngine()
        self.ontospecies_engine = OntoSpeciesQAEngine()
        self.ontokin_engine = OntoKinQAEngine()
        self.wikidata_engine = WikidataEngine()
        self.nel = ChemicalNEL()

        self.domain_encoding = {"pubchem": 0, "ontocompchem": 1, "ontospecies": 2, "ontokin": 3, "wikidata": 4}
        self.encoding_domain = {v: k for k, v in self.domain_encoding.items()}
        self.domain_list = self.domain_encoding.keys()
        print(self.domain_list)
        self.engine_list = [self.pubchem_engine, self.ontochemistry_engine, self.ontospecies_engine,
                            self.ontokin_engine, self.wikidata_engine]

        self.device = torch.device("cpu")
        self.max_length = 12
        # self.tokenizer = BertTokenizer.from_pretrained('bert-base-uncased')
        self.nlp = NLPTools(tokenizer_name="bert-base-uncased")
        self.dataset_path = os.path.join(DATA_DIR, 'CrossGraph')
        self.score_adjust_model = CrossGraphAlignmentModel(device=self.device).to(self.device)
        self.score_adjust_model.load_state_dict(torch.load(os.path.join(self.dataset_path,
                                                                        'cross_graph_model_with_wikidata'),
                                                           map_location=self.device))

    def create_triple_for_prediction(self, question, score_list, domain_list, target_list):
        # try:
        question = question.replace(" of ", " ")
        question = question.replace("what is the ", " ").replace("what is ", " ")
        target = target_list[0]
        question = question.lower().replace(target.lower(), '')
        score_list = torch.FloatTensor(score_list).reshape(1, -1).squeeze(0)
        domain_list = torch.LongTensor(domain_list)
        tokenized_question = self.tokenizer(question,
                                            padding='max_length', max_length=self.max_length, truncation=True,
                                            return_tensors="pt")
        question_list = {}
        for key in tokenized_question:
            data = tokenized_question[key].repeat([1, len(score_list)])
            question_list[key] = data

        return question_list, score_list, domain_list

    def adjust_scores(self, triple):
        return self.score_adjust_model.predict(triple=triple)

    def prepare_for_visualization(self, answer_list, target_list):
        result = []
        for ans, tar in zip(answer_list, target_list):
            row = {"node": tar, "value": ans}
            result.append(row)

        return result

    def re_rank_answers(self, domain_list, score_list, answer_list, target_list):
        values, indices = torch.topk(torch.FloatTensor(score_list), k=10, largest=True)
        re_ranked_labels = [answer_list[i] for i in indices]
        re_ranked_domain_list = [domain_list[i] for i in indices]
        re_ranked_engine_list = [self.engine_list[domain] for domain in re_ranked_domain_list]
        re_ranked_score_list = [float(v.item() / 2) for v in values]
        re_ranked_domain_list = [self.encoding_domain[d] for d in re_ranked_domain_list]
        re_ranked_node_value_list = [engine.value_lookup(node) for engine, node in
                                     zip(re_ranked_engine_list, re_ranked_labels)]

        result = []

        for label, domain, score, value, target in zip(re_ranked_labels, re_ranked_domain_list, re_ranked_score_list,
                                                       re_ranked_node_value_list, target_list):
            row = {"node": label, "domain": domain, "score": score, "value": value, "target": target}
            result.append(row)

        return result

    def run(self, question, disable_alignment: bool = False, heads={}):
        """
        The main interface for the integrated QA engine
        :param disable_alignment: whether run for test purpose, if true, score alignment will be disabled
        :param question: question in string, with head entity removed
        :param heads: IRI of the head entity before cross-ontology translation, always in the form of CID (pubchem ID)
        :return: the re-ranked list of answer labels according to the adjusted scores
        """
        score_list = []
        label_list = []
        domain_list = []
        target_list = []
        print("=========================")
        stop_words = ["find", "all", "species", "what", "is", "the", "show", "me", "What", "Show"]
        tokens = [t for t in question.split(" ") if t.lower().strip() not in stop_words]
        question = " ".join(tokens)
        mention = self.nel.get_mention(question)
        # remove stop words ...
        # what is the ... of ... find all .. species .. e.g.


        if mention is not None:
            question = question.replace(mention, "")

        for domain, engine in zip(self.domain_list, self.engine_list):
            if domain in heads:
                head = heads[domain]
            else:
                head = None
            print(f"======================== USING ENGINE {domain}============================")
            if domain == "wikidata":
                print("question given: ", question, "mention", mention)
                labels, scores, targets, question_type = engine.run(question=question, mention=mention)
                if question_type == "numerical":
                    return self.prepare_for_visualization(answer_list=labels, target_list=targets)
                    # return labels, scores, targets
            else:
                labels, scores, targets = engine.run(question=question, head=head, mention=mention)
            # TODO: if domain is wikidata and gives a numerical answer, clear the results from other engines

            length_diff = 5 - len(labels)
            scores = scores + [-999] * length_diff
            labels = labels + ["EMPTY SLOT"] * length_diff
            targets = targets + ["EMPTY SLOT"] * length_diff
            score_list.append(scores)
            label_list += labels
            target_list += targets

        tokenized_question = self.nlp.tokenize_question(question, 1)
        score_factors = self.score_adjust_model.predict_domain(tokenized_question).squeeze()
        adjusted_score_list = []

        normalized_score_list = normalize_scores(score_list)
        for score, score_factor, domain in zip(normalized_score_list, score_factors, self.domain_list):
            if disable_alignment:
                score = torch.FloatTensor([1, 1, 1, 1, 1])
            else:
                score = torch.FloatTensor(score)
            print("score", score)
            print("score factor", score_factor)
            adjusted_score = score + score_factor.repeat(5)
            adjusted_score = adjusted_score.tolist()
            adjusted_score_list = adjusted_score_list + adjusted_score
            domain_list = domain_list + [self.domain_encoding[domain]] * 5
            print("domain:", domain)
            print("------------------------")

        return self.re_rank_answers(domain_list=domain_list, score_list=adjusted_score_list, answer_list=label_list,
                                    target_list=target_list)


if __name__ == '__main__':

    result_list = []

    my_qa_engine = CrossGraphQAEngine()
    START_TIME = time.time()
    rst = my_qa_engine.run(question="Show me the melting point of all species")
    print(rst)
    print(f"TIME USED: {time.time() - START_TIME}")
    #
    # START_TIME = time.time()
    # rst = my_qa_engine.run(question="find melting point of all species")
    # print(rst)
    # print(f"TIME USED: {time.time() - START_TIME}")
    #
    # START_TIME = time.time()
    # rst = my_qa_engine.run(question="what is the boiling point of 1-methoxy-2-propanol")
    # print(rst)
    # print(f"TIME USED: {time.time() - START_TIME}")


    # question_answer_dict_path = os.path.join(DATA_DIR, "CrossGraph/selected_question_answer_list.json")
    # question_answer_list = json.loads(open(question_answer_dict_path).read())
    # for obj in question_answer_list:
    #     question = obj["question"]
    #     answer = obj["answer"]
    #     pred_answers = my_qa_engine.run(question=question)
    #     print("predicted answers", pred_answers)
    #     row = {"question": question, "true_answer": answer, "pred_answer": pred_answers}
    #     result_list.append(row)
    #
    # result_path = os.path.join(DATA_DIR, "CrossGraph/selected_result_list.json")
    # with open(result_path, "w") as f:
    #     f.write(json.dumps(result_list))
    #     f.close()
    #


