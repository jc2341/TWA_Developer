import rdflib
from owlready2 import *
from alignment import  *
class AReader():
    def __init__(self,addr):
        self.addr = addr


    def readAlignment(self,thre):
        onto = get_ontology(self.addr).load()
        print('loaded')
        r = list(default_world.sparql("""
                   SELECT  ?e1 ?e2 ?s
                   { ?m <http://knowledgeweb.semanticweb.org/heterogeneity/alignment#entity1> ?e1.
                      ?m  <http://knowledgeweb.semanticweb.org/heterogeneity/alignment#entity2> ?e2 .
                      ?m  <http://knowledgeweb.semanticweb.org/heterogeneity/alignment#measure> ?s.
                    }
            """))
        self.a = Alignment(r)
        self.a.filterDelete(thre)
        #self.a.stableMarriage()
        print("num of alignments found:"+str(len(self.a.map)))

if __name__ == "__main__":
    reader = AReader('resultDukeAllM.owl')
    reader.readAlignment(0.6)

