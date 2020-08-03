from compchemparser.ontocompchemdata.ontocompchemdata import OntoCompChemData
from bz2 import __author__

def run(log_file,output_json):
    # create OntoCompChemData object
    CompChemObj = OntoCompChemData()
    # parse the log, and once done upload data to KG
    # the upload function needs to be defined in the OntoCompChemData class
    CompChemObj.getData(log_file)
    #CompChemObj.uploadToKG()
    if output_json: 
        CompChemObj.outputjson()        
        CompChemObj.outputowl()       