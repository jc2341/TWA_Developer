from abc import ABC, abstractmethod
from pkg_resources import resource_string

class base_parser(ABC):

    def __init__(self):
        """ init the parser """

    def parseLog(self,logFile,parsedResults):
        # Instantiate empty lists to be populated by the parser. This means
        # if the parser find nothing it will return an empty list
        LevelOfTheory = []
        SpinMultiplicity = []
        SymmetryNumber = []

        # Start parsing the log file
        with open(logFile, "rt") as logFileHandle:
            # The parser searches each line of the file for objects of interest
            # each item found in the file is appended to the corresponding object
            for line in logFileHandle:
                # Search for the level of theory
                tmp = self.getLevelOfTheory(line)
                if tmp:
                    LevelOfTheory = LevelOfTheory.append(tmp)
                # Search for the spin multiplicity
                tmp = self.getSpinMult(line)
                if tmp:
                    SpinMultiplicity = SpinMultiplicity.append(tmp)
                # Search for the symmetry number
                tmp = self.getSymmetryNumber(line)
                if tmp:
                    SymmetryNumber = SymmetryNumber.append(tmp)

        # Assign found results to the parsedResults dictionary
        parsedResults['LevelOfTheory'] = LevelOfTheory
        parsedResults['SpinMultiplicity'] = SpinMultiplicity
        parsedResults['SymmetryNumber'] = SymmetryNumber

    @abstractmethod
    def getLevelOfTheory(self,line):
        pass

    @abstractmethod
    def getSpinMult(self,line):
        pass

    @abstractmethod
    def getSymmetryNumber(self,line):
        pass

