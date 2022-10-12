import requests
from typing import Optional, Tuple
import json

class pug_api():
    def __init__(self):
        self.input_namespace_dict = {'InChI' : 'inchi/', 'SMILES' : 'smiles/'}
        self.output_suffix_dict = {'InChI' : '?inchi=', 'SMILES' : '?smiles='}

    def pug_request(self, key: str, value: str) -> Tuple[str,str]:    
        # https://pubchem.ncbi.nlm.nih.gov/rest/pug/<input specification>/<operation specification>/[<output specification>][?<operation_options>]
        pubchem_domain = 'https://pubchem.ncbi.nlm.nih.gov/rest/pug/'
        # <input specification>:
        input_domain = 'compound/' # <domain> = substance | compound | assay | gene | protein | pathway | taxonomy | cell | <other inputs>
        input_namespace = self.input_namespace_dict.get(key) # compound domain <namespace> = cid | name | smiles | inchi | sdf | inchikey | formula | <structure search> | <xref> | listkey | <fast search>
        input_identifier=  value # 'c1ccccc1CCC(O)C' is picked as a default InChI  
        # /<operation specification>
        operation_property = 'property/' # compound domain <operation specification> = record | <compound property> | synonyms | sids | cids | aids | assaysummary | classification | <xrefs> | description | conformers
        operation_property = ''
        property_tag = 'MolecularFormula,InChIKey,InChI,CanonicalSmiles,ExactMass,MolecularWeight,IsotopeAtomCount,IupacName,CovalentUnitCount,Tpsa/' # <compound property> = property / [comma-separated list of property tags]
        property_tag = ''
        # /<output specification>
        output= 'JSON/' # <output specification> = XML | ASNT | ASNB | JSON | JSONP [ ?callback=<callback name> ] | SDF | CSV | PNG | TXT
        suffix = self.output_suffix_dict.get(key)
        link = pubchem_domain+input_domain+input_namespace+operation_property+property_tag+output+suffix+input_identifier
        print(link)
        data = requests.get(link)
        file = json.loads(data.text)
        return file
    # Method for retrieving PubChem properties in a dictionary
    def get_props(self, data : dict) -> dict:
        props_list = data.get('PC_Compounds')[0].get('props')
        props = {}
        for item in props_list:
            if item.get('urn').get('name'): 
                key = str(item.get('urn').get('name'))+' '+str(item.get('urn').get('label'))
            else:
                key = str(item.get('urn').get('label'))
    
            value = [item.get('value')[key] for key in item.get('value').keys()][0]
            props[key] = value
        return props
    # Method for retrieving PubChem CID in a dictionary
    def get_cid(self, data : dict) -> dict:
        id = data.get('PC_Compounds')[0].get('id')
        return id.get('id')


if __name__ == "__main__":
    pug_access = pug_api()

    for inchi in ['InChI=1/C10H10/c1-2-6-10-8-4-3-7-9(10)5-1/h1-3,5-7H,4,8H2', 
                  'InChI=1/C10H10/c1-2-3-7-10-8-5-4-6-9-10/h4-6,8-9H,2H2,1H3', 
                  'InChI=1/C10H10/c1-2-8-5-6-9-4-3-7(1)10(8)9/h1-10H']:
        data = pug_access.pug_request('InChI', inchi)
        cid = pug_access.get_cid(data)
        props = pug_access.get_props(data)
        print(cid['cid'], props['Preferred IUPAC Name'])

    for smiles in ['C1CC2=CC=CC=C2C=C1', 
                  'CCC#CC1=CC=CC=C1', 
                  'C1=CC2C=CC3C2C1C=C3']:
        data = pug_access.pug_request('SMILES', smiles)
        cid = pug_access.get_cid(data)
        props = pug_access.get_props(data)
        print(cid['cid'], props['Preferred IUPAC Name'])