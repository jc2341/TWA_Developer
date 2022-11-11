import sys

from torch.optim.lr_scheduler import ExponentialLR
from tqdm import tqdm
import os
import numpy as np
import pandas as pd
import torch
from torch.utils.data import DataLoader

sys.path.append("../../..")
from Marie.Util.Dataset.CrossGraph_Dataset import CrossGraphDataset
from Marie.Util.location import DATA_DIR
from Marie.Util.Models.CrossGraphAlignmentModel import CrossGraphAlignmentModel


class CrossGraphTrainer:
    def __init__(self, df, test_step=20, epoch_num=20, batch_size=8, learning_rate=1e-2, gamma=0.1, save_model=False,
                 load_model=False):
        self.df = df
        self.epoch_num = epoch_num
        self.test_step = test_step
        self.learning_rate = learning_rate
        self.gamma = gamma

        use_cuda = torch.cuda.is_available()
        self.device = torch.device("cuda" if use_cuda else "cpu")
        self.dataset_dir = os.path.join(DATA_DIR, "ontocompchem_latent_40")
        print(f'=========== USING {self.device} ===============')
        self.model = CrossGraphAlignmentModel(device=self.device).to(self.device)
        self.optimizer = torch.optim.Adam(self.model.parameters(), lr=self.learning_rate)
        self.scheduler = ExponentialLR(self.optimizer, gamma=self.gamma)

        df_train, df_test = np.split(df.sample(frac=1, random_state=11), [int(.8 * len(df))])
        dataset_train = CrossGraphDataset(df_train)
        self.dataloader_train = DataLoader(dataset_train, batch_size=batch_size, shuffle=False)
        dataset_test = CrossGraphDataset(df_test)
        self.dataloader_test = DataLoader(dataset_test, batch_size=batch_size, shuffle=False)
        # ============= init the model ===============

    def run(self):
        previous_rate = 0
        for epoch in tqdm(range(self.epoch_num)):
            outrank_rate = self.train()
            if previous_rate > outrank_rate:
                self.scheduler.step()
            previous_rate = outrank_rate
            if epoch % self.test_step == 0:
                self.evaluate()

    def train(self):
        total_loss_train = 0
        total_outrank_rate = 0
        total_difference = 0
        for true_answer, fake_answer, true_domain, fake_domain, question in tqdm(self.dataloader_train):
            pos_triple = (question, true_answer, true_domain)  # question, score, domain
            neg_triple = (question, fake_answer, fake_domain)
            loss, outrank, delta = self.model(true_answer=pos_triple, fake_answer=neg_triple)
            loss.mean().backward()
            total_loss_train += loss.mean().cpu()
            total_outrank_rate += outrank
            total_difference += delta
        self.optimizer.step()

        print(f'current learning rate {self.scheduler.get_last_lr()}')
        print(f"total train loss {total_loss_train}")
        print(f"total out rank rate {total_outrank_rate / len(self.dataloader_train)}")
        print(f"total_difference {total_difference / len(self.dataloader_train)}")
        return total_outrank_rate / len(self.dataloader_train)

    def evaluate(self):
        """
        Evaluation matrix : [outrank rate, which measures the percentage of fake answers returned from
        another embedding space... ]
        :return:
        """


if __name__ == '__main__':
    df = pd.read_csv(os.path.join(DATA_DIR, 'CrossGraph', 'cross_graph_pairs.tsv'), sep='\t')
    my_cross_graph_trainer = CrossGraphTrainer(df)
    my_cross_graph_trainer.run()
