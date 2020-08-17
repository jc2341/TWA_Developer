import nltk
from gensim import *

# penalize class unmatch

from matchers.elementMatcher import *


class Penalizer():
    def __init__(self, classAlign, icmap1,icmap2, p = 0.8, thre = 0.6):
        self.classAlign = classAlign
        self.icmap1 = icmap1
        self.icmap2 = icmap2
        self.p = p
        self.thre = thre
        #how to link class to instance?

    def sameClass(self, classlist1, classlist2):
        for c1 in classlist1:
            for c2 in classlist2:
                vid = self.classAlign.search(c1, c2)
                if vid is None:
                    continue
                if  vid > self.thre:
                    return True
        return False


    def penal(self, instanceAlign):
        toupdate = []
        for id1,id2,value in instanceAlign.map:
            if not self.sameClass(self.icmap1[id1], self.icmap2[id2]):
                toupdate.append((id1, id2, value*self.p))
            else:
                toupdate.append((id1, id2, value))


        return Alignment(toupdate)
