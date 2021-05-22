##########################################
# Author: Wanni Xie (wx243@cam.ac.uk)    #
# Last Update Date: 21 May 2021          #
##########################################

"""This module is designed to generate and update the A-box of UK power grid model_ELine."""

import os
import owlready2
from rdflib.extras.infixowl import OWL_NS
from rdflib import Graph, URIRef, Literal, ConjunctiveGraph
from rdflib.namespace import RDF
from rdflib.plugins.sleepycat import Sleepycat
from rdflib.store import NO_STORE, VALID_STORE
import sys
BASE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
sys.path.insert(0, BASE)
from UK_Digital_Twin_Package import UKDigitalTwin as UKDT
from UK_Digital_Twin_Package import UKDigitalTwinTBox as T_BOX
from UK_Digital_Twin_Package import UKPowerGridModel as UK_PG
from UK_Digital_Twin_Package import UKPowerPlant as UKpp
from UK_Digital_Twin_Package import UKPowerGridTopology as UK_Topo
from UK_Digital_Twin_Package import CO2FactorAndGenCostFactor as ModelFactor
from UK_Digital_Twin_Package.OWLfileStorer import storeGeneratedOWLs, selectStoragePath, readFile
from costFunctionParameterAgent import costFuncPara
import SPARQLQueryUsedInModel as query_model

"""Notation used in URI construction"""
HASH = '#'
SLASH = '/'
UNDERSCORE = '_'
OWL = '.owl'

"""Create an instance of Class UKDigitalTwin"""
dt = UKDT.UKDigitalTwin()

"""Create an object of Class UKDigitalTwinTBox"""
t_box = T_BOX.UKDigitalTwinTBox()

"""Create an object of Class UKPowerPlantDataProperty"""
ukpp = UKpp.UKPowerPlant()

"""Create an object of Class UKElineModel"""
uk_eline_model = UK_PG.UKElineModel()

"""Create an object of Class UKPowerGridTopology"""
uk_topo = UK_Topo.UKPowerGridTopology()

"""Graph store"""
store = 'default'
# store = Sleepycat()
# store.__open = True
# store.context_aware = True

"""Sleepycat storage path"""
defaultPath_Sleepycat = uk_eline_model.SleepycatStoragePath
topoAndConsumpPath_Sleepycat = uk_topo.SleepycatStoragePath
userSpecifiePath_Sleepycat = None # user specified path
userSpecified_Sleepycat = False # storage mode: False: default, True: user specified

"""father node"""
father_node = UKDT.namedGraphURIGenerator(4, dt.powerGridModel, 10, "ELine")

# """NameSpace"""
# father_uri = father_node.split('#')[0]
# model_ebus_namespace = father_uri + HASH

"""T-Box URI"""
ontocape_upper_level_system     = owlready2.get_ontology(t_box.ontocape_upper_level_system).load()
ontocape_derived_SI_units       = owlready2.get_ontology(t_box.ontocape_derived_SI_units).load()
ontocape_mathematical_model     = owlready2.get_ontology(t_box.ontocape_mathematical_model).load()
ontopowsys_PowerSystemModel     = owlready2.get_ontology(t_box.ontopowsys_PowerSystemModel).load()

"""User specified folder path"""
filepath = None
userSpecified = False

"""EBus Conjunctive graph identifier"""
model_ELine_cg_id = "http://www.theworldavatar.com/kb/UK_Digital_Twin/UK_power_grid/10_bus_model/Model_ELine"

### Functions ### 
"""Main function: create the named graph Model_EBus and their sub graphs each ELine"""
def createModel_ELine(store, version_of_model, updateLocalOWLFile = True):
    global filepath, userSpecified, defaultPath_Sleepycat, userSpecifiePath_Sleepycat, userSpecified_Sleepycat 
    # create conjunctive graph storing the generated graphs in a specified Sleepycat on-disc graph store
    if isinstance(store, Sleepycat): 
        print('The store is Sleepycat')
        cg_model_ELine = ConjunctiveGraph(store=store, identifier = model_ELine_cg_id)
        if userSpecifiePath_Sleepycat == None and userSpecified_Sleepycat:
            print('****Needs user to specify a Sleepycat storage path****')
            userSpecifiePath_Sleepycat = selectStoragePath()
            userSpecifiePath_Sleepycat_ = userSpecifiePath_Sleepycat + '\\' + 'ConjunctiveGraph_UKPowerGrid_ELine'
            sl = cg_model_ELine.open(userSpecifiePath_Sleepycat_, create = False) 
            
        elif os.path.exists(defaultPath_Sleepycat) and not userSpecified_Sleepycat:
            print('****Non user specified Sleepycat storage path, will use the default storage path****')
            sl = cg_model_ELine.open(defaultPath_Sleepycat, create = False)        
        else:
            print('****Create Sleepycat store with its default path****')
            sl = cg_model_ELine.open(defaultPath_Sleepycat, create = True)   
        
        if sl == NO_STORE:
        # There is no underlying Sleepycat infrastructure, so create it
            cg_model_ELine.open(defaultPath_Sleepycat, create = True)
        else:
            assert sl == VALID_STORE, "The underlying sleepycat store is corrupt"
    else:
        print('Store is IOMemery')
        
    ELine_busRelated, ELine_parallelBranches = list(query_model.queryELineTopologicalInformation(topoAndConsumpPath_Sleepycat))
    # EBus = checkAggregatedBus(EBus) # sum up the demand of an AggregatedBus
    #TODO: process the data in the return result--ELine_parallelBranches, read data file: branch properties
    
    
    if EBus == None:
        print('EBus is empty')
        return None
    
    for ebus in EBus:         
    # if EBus[0] != None: # test
    #     ebus = EBus[0] # test
    
        print('################START createModel_EBus#################')
        root_uri = ebus[0].split('#')[0]
        namespace = root_uri + HASH
        node_locator = ebus[0].split('#')[1]
        root_node = namespace + 'Model_' + node_locator
        
        # create a named graph
        g = Graph(store = store, identifier = URIRef(root_uri))
        # Import T-boxes
        g.add((g.identifier, OWL_NS['imports'], URIRef(t_box.ontocape_mathematical_model)))
        g.add((g.identifier, OWL_NS['imports'], URIRef(t_box.ontocape_upper_level_system)))  
        g.add((g.identifier, OWL_NS['imports'], URIRef(t_box.ontopowsys_PowerSystemModel))) 
        # Add root node type and the connection between root node and its father node   
        g.add((URIRef(root_node), URIRef(ontocape_upper_level_system.isExclusivelySubsystemOf.iri), URIRef(father_node)))
        g.add((URIRef(root_node), RDF.type, URIRef(ontocape_mathematical_model.Submodel.iri)))
        g.add((URIRef(root_node), RDF.type, URIRef(ontopowsys_PowerSystemModel.PowerFlowModelAgent.iri)))
        g.add((URIRef(father_node), URIRef(ontocape_upper_level_system.isComposedOfSubsystem .iri), URIRef(root_node)))
        # link with EBus node in topology
        g.add((URIRef(root_node), URIRef(ontocape_upper_level_system.models.iri), URIRef(ebus[0])))
        g.add((URIRef(ebus[0]), URIRef(ontocape_upper_level_system.isModeledBy.iri), URIRef(root_node)))
            
        ###add EBus model parametor###
        uk_ebus_model_ = UK_PG.UKEbusModel(version = version_of_model)
        uk_ebus_model_ = initialiseModelVar(uk_ebus_model_, ebus) 
        
        if uk_ebus_model_ != None:
            pass
        else: 
            print ('uk_ebus_model_ is none')
            return None 
        
        g = AddEBusModelVariable(g, root_node, namespace, node_locator, uk_ebus_model_.BUSNUMKey, int(uk_ebus_model_.BUS), None, \
                                 ontopowsys_PowerSystemModel.BusNumber.iri, ontocape_mathematical_model.Parameter.iri)
        g = AddEBusModelVariable(g, root_node, namespace, node_locator, uk_ebus_model_.BUSTYPEKey, int(uk_ebus_model_.TYPE), None, \
                                 ontopowsys_PowerSystemModel.BusType.iri, ontocape_mathematical_model.Parameter.iri)
        g = AddEBusModelVariable(g, root_node, namespace, node_locator, uk_ebus_model_.PD_INPUTKey, float(uk_ebus_model_.PD_INPUT), ontocape_derived_SI_units.MW.iri, \
                                 ontopowsys_PowerSystemModel.PdBus.iri, ontocape_mathematical_model.InputVariable.iri)    
        g = AddEBusModelVariable(g, root_node, namespace, node_locator, uk_ebus_model_.GD_INPUTKey, float(uk_ebus_model_.GD_INPUT), ontocape_derived_SI_units.Mvar.iri, \
                                 ontopowsys_PowerSystemModel.GdBus.iri, ontocape_mathematical_model.InputVariable.iri)       
        g = AddEBusModelVariable(g, root_node, namespace, node_locator, uk_ebus_model_.GSKey, int(uk_ebus_model_.GS), None,\
                                 ontopowsys_PowerSystemModel.Gs.iri, ontocape_mathematical_model.Parameter.iri)
        g = AddEBusModelVariable(g, root_node, namespace, node_locator, uk_ebus_model_.BSKey, int(uk_ebus_model_.BS), None,\
                                 ontopowsys_PowerSystemModel.Bs.iri, ontocape_mathematical_model.Parameter.iri)    
        g = AddEBusModelVariable(g, root_node, namespace, node_locator, uk_ebus_model_.AREAKey, int(uk_ebus_model_.AREA), None,\
                                 ontopowsys_PowerSystemModel.Area.iri, ontocape_mathematical_model.Parameter.iri)    
        g = AddEBusModelVariable(g, root_node, namespace, node_locator, uk_ebus_model_.VM_INPUTKey, float(uk_ebus_model_.VM_INPUT), ontocape_derived_SI_units.kV.iri, \
                                 ontopowsys_PowerSystemModel.Vm.iri, ontocape_mathematical_model.InputVariable.iri)         
        g = AddEBusModelVariable(g, root_node, namespace, node_locator, uk_ebus_model_.VA_INPUTKey, float(uk_ebus_model_.VA_INPUT), ontocape_derived_SI_units.degree.iri, \
                                 ontopowsys_PowerSystemModel.Va.iri, ontocape_mathematical_model.InputVariable.iri)         
        g = AddEBusModelVariable(g, root_node, namespace, node_locator, uk_ebus_model_.BASEKVKey, int(uk_ebus_model_.BASEKV), ontocape_derived_SI_units.kV.iri, \
                                 ontopowsys_PowerSystemModel.baseKV.iri, ontocape_mathematical_model.Parameter.iri)    
        g = AddEBusModelVariable(g, root_node, namespace, node_locator, uk_ebus_model_.ZONEKey, int(uk_ebus_model_.ZONE), None, \
                                 ontopowsys_PowerSystemModel.Zone.iri, ontocape_mathematical_model.Parameter.iri)   
        g = AddEBusModelVariable(g, root_node, namespace, node_locator, uk_ebus_model_.VMAXKey, int(uk_ebus_model_.VMAX), ontocape_derived_SI_units.kV.iri, \
                                 ontopowsys_PowerSystemModel.VmMax.iri, ontocape_mathematical_model.Parameter.iri) 
        g = AddEBusModelVariable(g, root_node, namespace, node_locator, uk_ebus_model_.VMINKey, int(uk_ebus_model_.VMIN), ontocape_derived_SI_units.kV.iri, \
                                 ontopowsys_PowerSystemModel.VmMin.iri, ontocape_mathematical_model.Parameter.iri)
        
        # print(g.serialize(format="pretty-xml").decode("utf-8"))
               
        # generate/update OWL files
        if updateLocalOWLFile == True:    
            # specify the owl file storage path
            defaultStoredPath = uk_ebus_model.StoreGeneratedOWLs + 'Model_' + node_locator + OWL #default path
        
            # Store/update the generated owl files      
            if os.path.exists(uk_ebus_model.StoreGeneratedOWLs) and not userSpecified:
                print('****Non user specified storage path, will use the default storage path****')
                storeGeneratedOWLs(g, defaultStoredPath)
        
            elif filepath == None:
                print('****Needs user to specify a storage path****')
                filepath = selectStoragePath()
                filepath_ = filepath + '\\' + 'Model_' + node_locator + OWL
                storeGeneratedOWLs(g, filepath_)
            else: 
                filepath_ = filepath + '\\' + 'Model_' + node_locator + OWL
                storeGeneratedOWLs(g, filepath_)
    if isinstance(store, Sleepycat):  
        cg_model_EBus.close()       
    return

def checkAggregatedBus(EBus):
    EBus_ = [ [str(ebus[0]), float(ebus[1])] for ebus in EBus]
    bus_node  = []
    for ebus in EBus_:
        if ebus[0] in bus_node:
            counter_1 = bus_node.index(ebus[0])
            counter_2 = EBus_.index(ebus) 
            if counter_2 > counter_1:
                EBus_[counter_1][1] += EBus_[counter_2][1]
                EBus_[counter_1][1] = round(EBus_[counter_1][1], 2)
                del EBus_[counter_2]
            else:
                print('counter_2 should be larger than counter_1')
                return None
        else:
            bus_node.append(ebus[0])
    return EBus_

def AddEBusModelVariable(graph, root_node, namespace, node_locator, varKey, varValue, unit, *varType):
    # parameter iri
    var_iri = namespace + varKey + node_locator
    value_var_iri = namespace + UK_PG.valueKey + varKey + node_locator
    # add var node and type
    graph.add((URIRef(root_node), URIRef(ontocape_mathematical_model.hasModelVariable.iri), URIRef(var_iri)))
    for type_ in varType:
        graph.add((URIRef(var_iri), RDF.type, URIRef(type_)))
    #add var value
    graph.add((URIRef(var_iri), URIRef(ontocape_upper_level_system.hasValue.iri), URIRef(value_var_iri)))
    graph.add((URIRef(value_var_iri), RDF.type, URIRef(ontocape_mathematical_model.ModelVariableSpecification.iri)))
    if unit != None:
        graph.add((URIRef(value_var_iri), URIRef(ontocape_upper_level_system.hasUnitOfMeasure.iri), URIRef(unit)))
    graph.set((URIRef(value_var_iri), URIRef(ontocape_upper_level_system.numericalValue.iri), Literal(varValue)))
    return graph

def initialiseModelVar(EBus_Model, EBus):
    if isinstance (EBus_Model, UK_PG.UKEbusModel):
        pass
    else:
        print('The first argument should be an instence of UKEbusModel')
        return None
    EBus_Model.BUS = int((EBus[0].split('#EBus-')[1]).split('_')[0])
    
    if EBus_Model.BUS == 1:
        EBus_Model.Type = 3
    
    EBus_Model.PD_INPUT = round((float(EBus[1]) * 1000 / (24 * 365)), 3) 
    
    return EBus_Model

if __name__ == '__main__':    
    createModel_EBus(store, 2019)       
    print('Terminated')