from abc import ABC, abstractmethod
import random
from typing import Dict, List, Optional, Union

from data_generation.constants import PROPERTY_LABELS


class ExampleMakerTail2Tail:
    def make_example(self, subgraph: Optional[Dict[str, Union[dict, List[dict]]]]):
        """Chemicalclass to properties/uses"""
        if subgraph is None:
            return None
        
        tails_chemclass = [x for x in subgraph["tails"] if x["type"] == "chemicalclass"]
        if len(tails_chemclass) == 0:
            return None
        
        head_helper = ExampleT2TQueryConstructorHelperHead()

        tails = subgraph["tails"]
        if len(tails) == 0:
            return None
        
        random.shuffle(tails)
        tail_helper_resolver = T2TTailHelperResolver()
        tail_helpers: List[ExampleT2TQueryConstructorHelperTail] = [
            tail_helper_resolver.resolve(tail) for tail in tails
        ]
        all_helpers: List[ExampleT2TQueryConstructorHelper] = [
            head_helper
        ] + tail_helpers

        select_variables = [helper.get_select_variables() for helper in all_helpers]
        select_variables_compact = [
            helper.get_select_variables_compact() for helper in all_helpers
        ]
        where_clauses = [helper.get_where_clauses() for helper in all_helpers]
        where_clauses_compact = [
            helper.get_where_clauses_compact() for helper in all_helpers
        ]
        ask_items = [helper.get_ask_item() for helper in tail_helpers]
        ask_items = [x for x in ask_items if x is not None]

        sparql_query = f"""SELECT DISTINCT {" ".join(select_variables)} 
WHERE {{{"".join(where_clauses).rstrip()}
}}"""
        sparql_query_compact = f"""SELECT DISTINCT {" ".join(select_variables_compact)} 
WHERE {{{"".join(where_clauses_compact)}
}}"""

        return dict(
            canonical_question=self.make_canonical_question(ask_items, chemicalclass_values=[x["ChemicalClassValue"] for x in tails_chemclass]),
            bindings=dict(species=subgraph["head"]["IdentifierValue"]),
            sparql_query=sparql_query,
            sparql_query_compact=sparql_query_compact,
        )
    
    def make_canonical_question(self, ask_items: List[str], chemicalclass_values: List[str]):
        tokens = ["What "]
        if len(ask_items) < 2:
            tokens.append("is")
        else:
            tokens.append("are")

        for i, ask_item in enumerate(ask_items):
            if i == 0:
                tokens.append(" the ")
            elif i < len(ask_items) - 1:
                tokens.append(", ")
            else:
                tokens.append(" and ")
            tokens.append(ask_item)

        tokens.append(" of chemical species classified as ")

        for i, chemclass in enumerate(chemicalclass_values):
            if i == 0:
                pass
            elif i < len(chemicalclass_values) - 1:
                tokens.append(", ")
            else:
                tokens.append(" and ")
            tokens.append(chemclass)

        tokens.append("?")
        
        return "".join(tokens)

class T2TTailHelperResolver:
    def __init__(self):
        self.chemicalclass_num = 0

    def resolve(self, tail: Dict[str, str]):
        if tail["type"] == "property":
            tail_helper = ExampleT2TQueryConstructorHelperTailProperty(
                property_name=tail["PropertyName"]
            )
        elif tail["type"] == "use":
            tail_helper = ExampleT2TQueryConstructorHelperTailUse()
        elif tail["type"] == "chemicalclass":
            self.chemicalclass_num += 1
            tail_helper = ExampleT2HQueryConstructorHelperTailChemicalClass(tail_id=self.chemicalclass_num, chemicalclass_value=tail["ChemicalClassValue"])
        else:
            raise ValueError(f"Unexpected tail type: {tail['type']}.")
        return tail_helper


class ExampleT2TQueryConstructorHelper(ABC):
    def get_select_variables(self):
        return ""

    def get_select_variables_compact(self):
        return ""

    @abstractmethod
    def get_where_clauses(self) -> str:
        pass

    @abstractmethod
    def get_where_clauses_compact(self) -> str:
        pass


class ExampleT2TQueryConstructorHelperHead(ExampleT2TQueryConstructorHelper):
    def get_select_variables(self):
        return "?label ?IUPACNameValue"

    def get_select_variables_compact(self):
        return "?IUPACNameValue"

    def get_where_clauses(self):
        return f"""
    ?SpeciesIRI rdf:type os:Species ; rdfs:label ?label ; os:hasIUPACName ?IUPACNameIRI .
    ?IUPACNameIRI os:value ?IUPACNameValue .
"""

    def get_where_clauses_compact(self):
        return f"""
    ?SpeciesIRI os:hasIUPACName ?IUPACNameValue ."""


class ExampleT2TQueryConstructorHelperTail(ExampleT2TQueryConstructorHelper):
    @abstractmethod
    def get_ask_item(self) -> Optional[str]:
        pass


class ExampleT2TQueryConstructorHelperTailProperty(
    ExampleT2TQueryConstructorHelperTail
):
    def __init__(self, property_name: str):
        self.property_name = property_name

    def get_select_variables(self):
        PropertyName = self.property_name
        return f"?{PropertyName}Value ?{PropertyName}UnitValue ?{PropertyName}ReferenceStateValue ?{PropertyName}ReferenceStateUnitValue"

    def get_select_variables_compact(self):
        PropertyName = self.property_name
        return f"?{PropertyName}Value"

    def get_where_clauses(self):
        PropertyName = self.property_name
        return f"""
    ?SpeciesIRI os:has{PropertyName} ?{PropertyName}IRI .
    ?{PropertyName}IRI os:value ?{PropertyName}Value ; os:unit ?{PropertyName}UnitIRI ; os:hasProvenance ?{PropertyName}ProvenanceIRI . 
    ?{PropertyName}UnitIRI rdfs:label ?{PropertyName}UnitValue .
    OPTIONAL{{
        ?{PropertyName}IRI os:hasReferenceState ?{PropertyName}ReferenceStateIRI .
        ?{PropertyName}ReferenceStateIRI os:value ?{PropertyName}ReferenceStateValue ; os:unit ?{PropertyName}ReferenceStateUnitIRI .
        ?{PropertyName}ReferenceStateUnitIRI rdfs:label ?{PropertyName}ReferenceStateUnitValue .
    }}
"""

    def get_where_clauses_compact(self):
        PropertyName = self.property_name
        return f"""
    ?SpeciesIRI os:has{PropertyName} ?{PropertyName}Value ."""

    def get_ask_item(self):
        return random.choice(PROPERTY_LABELS[self.property_name])


class ExampleT2TQueryConstructorHelperTailUse(ExampleT2TQueryConstructorHelperTail):
    def get_select_variables(self):
        return "?UseValue"

    def get_select_variables_compact(self):
        return "?UseValue"

    def get_where_clauses(self):
        return f"""
    ?SpeciesIRI os:hasUse ?UseIRI .
    ?UseIRI rdfs:label ?UseValue .
"""

    def get_where_clauses_compact(self):
        return f"""
    ?SpeciesIRI os:hasUse ?UseValue ."""

    def get_ask_item(self):
        return "use"


class ExampleT2HQueryConstructorHelperTailChemicalClass(
    ExampleT2TQueryConstructorHelperTail
):
    def __init__(self, tail_id: int, chemicalclass_value: str):
        self.tail_id = tail_id
        self.chemicalclass_value = chemicalclass_value

    def get_where_clauses(self):
        i = self.tail_id
        ChemicalClassValue = self.chemicalclass_value
        return f"""
    ?SpeciesIRI os:hasChemicalClass* ?x{i} .
	?x{i} ?y{i} ?z{i} .
	?z{i} rdfs:subClassOf* ?ChemicalClassIRI{i} .
	?ChemicalClassIRI{i} rdf:type os:ChemicalClass ; rdfs:label \"{ChemicalClassValue}\" .
"""

    def get_where_clauses_compact(self):
        ChemicalClassValue = self.chemicalclass_value
        return f"""
    ?SpeciesIRI os:hasChemicalClass \"{ChemicalClassValue}\" ."""

    def get_ask_item(self):
        return None

    def get_binding(self):
        i = self.tail_id
        ChemicalClassValue = self.chemicalclass_value
        return {f"ChemicalClassValue{i}": ChemicalClassValue}
