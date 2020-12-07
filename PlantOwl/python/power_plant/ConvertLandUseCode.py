##########################################
# Author: Feroz Farazi (msff2@cam.ac.uk) #
# Date: 04 Dec 2020                      #
##########################################
from rdflib import Graph, FOAF, URIRef, BNode, Literal
from rdflib.namespace import RDF, RDFS, Namespace
import ABoxGeneration as aboxgen
from tkinter import Tk  # from tkinter import Tk for Python 3.x
from tkinter.filedialog import askopenfilename
import csv
import PropertyReader as propread
import ABoxGeneration as aboxgen

"""Declared column headers as constants"""
COLUMN_1 = 'Source'
COLUMN_2 = 'Type'
COLUMN_3 = 'Target'
COLUMN_4 = 'Relation'
COLUMN_5 = 'Value'
TOTAL_NO_OF_COLUMNS = 5

"""Predefined types source entries"""
TYPE_INSTANCE = 'Instance'
TYPE_DATA     = 'Data Property'

"""Utility constants"""
HASH = '#'
SLASH = '/'
UNDERSCORE = '_'

"""Declared an array to maintain the list of already created instances"""
instances = []
g = Graph()

def select_file():
    """Suppresses the root window of GUI"""
    Tk().withdraw()
    """Opens a file dialog box to select a file"""
    return askopenfilename()

def is_header_valid(row):
    if len(row) >= TOTAL_NO_OF_COLUMNS:
        if row[0].strip().lower()==COLUMN_1.lower() \
                and row[1].strip().lower()==COLUMN_2.lower() \
                and row[2].strip().lower()==COLUMN_3.lower() \
                and row[3].strip().lower()==COLUMN_4.lower() \
                and row[4].strip().lower()==COLUMN_5.lower():
            return True
        else:
            return False

def process_data(row):
    if len(row) >= TOTAL_NO_OF_COLUMNS:
        if row[0].strip() is None or row[0].strip()  == '' \
                or row[1].strip() is None or row[1].strip()  == '' \
                or row[2].strip() is None or row[2].strip()  == '':
           return

        if row[1].strip().lower() == TYPE_INSTANCE.lower():
            if row[2].strip() in instances:
                if row[3].strip().lower()  == '':
                    return
                else:
                    print()
                    aboxgen.link_instance(g, URIRef(row[3]),
                                              propread.getABoxIRI()+SLASH+format_iri(row[0])+HASH+format_iri(row[2])+UNDERSCORE+format_iri(row[0]),
                                              propread.getABoxIRI()+SLASH+format_iri(row[0])+HASH+format_iri(row[2])+UNDERSCORE+format_iri(row[0]))
            else:
                print('Creating an instance:')
                aboxgen.create_instance(g,
                                        URIRef(propread.getTBoxIRI()+HASH+format_iri(row[2])),
                                        propread.getABoxIRI()+SLASH+format_iri(row[0])+HASH+format_iri(row[2])+UNDERSCORE+format_iri(row[0]),
                                        format_iri(row[0]))
                instances.append(row[0])

def format_iri(iri):
    iri = iri.replace(" ","")
    return iri

def create_namespace(IRI):
    print(IRI)
    return Namespace(IRI)

def convert_lucode():
    file_path = select_file()
    with open(file_path, newline='') as csvfile:
        rows = csv.reader(csvfile, delimiter=',', quotechar='|')
        line_count = 0
        for row in rows:
           if line_count == 0:
               if not is_header_valid(row):
                   print('Found invalid header, so it will terminate now.')
                   break
               else:
                   print('Found valid header, so it is creating a graph model for adding instances to it.')
                   g = Graph()
           if line_count > 0:
               process_data(row)
           line_count +=1

if __name__ == '__main__':
    convert_lucode()

# """Creates an instance of Graph"""
# g = Graph()
# """Assigns the IRI of the OntoLandUse ontology"""
# land_use_code_iri = "http://www.theworldavatar.com/ontology/ontolanduse/OntoLandUse.owl#"
# """Binds the IRI with the landusecode literal in the graph"""
# g.bind("landusecode", land_use_code_iri)
# """Creates the namespace of lucode AC01"""
# instance_iri_lucode_namespace = Namespace("http://www.theworldavatar.com/kb/ontolanduse/AC01.owl#")
# """Assigns the IRI of lucode AC01"""
# instance_iri_lucode = instance_iri_lucode_namespace.LandUseCode_AC01
# """Assigns the name of lucode"""
# instance_name = "AC01"
# """Creates the namespace of the OntoLandUse ontology"""
# namespace_onto_land_use = Namespace("http://www.theworldavatar.com/ontology/ontolanduse/OntoLandUse.owl#")
# g = aboxgen.create_instance(g, namespace_onto_land_use.LandUseCode, instance_iri_lucode, instance_name)
# instance_iri_admindiv = "http://www.theworldavatar.com/kb/ontolanduse/England.owl#AdministrativeDivision_England"
# instance_iri_admindiv_namespace = Namespace("http://www.theworldavatar.com/kb/ontolanduse/England.owl#")
# instance_name = "England"
# g = aboxgen.create_instance(g, namespace_onto_land_use.AdministrativeDivision, instance_iri_admindiv, instance_name)
# g = aboxgen.link_instance(g, namespace_onto_land_use.appliedIn, instance_iri_lucode,
#                           instance_iri_admindiv_namespace.AdministrativeDivision_England)
# comment = "test comment for the land use code"
# g = aboxgen.link_data(g, RDFS.comment, instance_iri_lucode_namespace.LandUseCode_AC01, comment)
#
#
# g.bind("foaf", FOAF)
#
# bob = URIRef("http://example.org/people#Bob")
# linda = BNode()  # a GUID is generated
#
# name = Literal('Bob')  # passing a string
# age = Literal(24)  # passing a python int
# height = Literal(76.5)  # passing a python float
#
# g.add((bob, RDF.type, FOAF.Person))
# g.add((bob, FOAF.name, name))
# g.add((bob, FOAF.knows, linda))
# g.add((linda, RDF.type, FOAF.Person))
# g.add((linda, FOAF.name, Literal("Linda")))
#
# file_name = 'test.owl'
# g.serialize(destination=file_name, format="application/rdf+xml")
