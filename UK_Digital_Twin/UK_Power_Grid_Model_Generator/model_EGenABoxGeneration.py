##########################################
# Author: Wanni Xie (wx243@cam.ac.uk)    #
# Last Update Date: 20 May 2021          #
##########################################

"""This module is designed to generate and update the A-box of UK power grid model_EGen."""

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

"""Create an object of Class CO2FactorAndGenCostFactor"""
ukmf = ModelFactor.ModelFactor()

"""Create an object of Class UKPowerGridModel"""
uk_egen_model = UK_PG.UKEGenModel()

"""Create an object of Class UKPowerGridTopology"""
uk_topo = UK_Topo.UKPowerGridTopology()

"""Graph store"""
# store = 'default'
store = Sleepycat()
store.__open = True
store.context_aware = True

"""Sleepycat storage path"""
defaultPath_Sleepycat = uk_egen_model.SleepycatStoragePath
topoAndConsumpPath_Sleepycat = uk_topo.SleepycatStoragePath
powerPlant_Sleepycat = ukpp.SleepycatStoragePath
userSpecifiePath_Sleepycat = None # user specified path
userSpecified_Sleepycat = False # storage mode: False: default, True: user specified

"""father node"""
father_node = UKDT.namedGraphURIGenerator(4, dt.powerGridModel, 10, "EGen")
father_uri = father_node.split('#')[0]

"""NameSpace"""
model_egen_namespace = father_uri + HASH

"""T-Box URI"""
ontocape_upper_level_system     = owlready2.get_ontology(t_box.ontocape_upper_level_system).load()
ontocape_derived_SI_units       = owlready2.get_ontology(t_box.ontocape_derived_SI_units).load()
ontocape_mathematical_model     = owlready2.get_ontology(t_box.ontocape_mathematical_model).load()
ontopowsys_PowerSystemModel     = owlready2.get_ontology(t_box.ontopowsys_PowerSystemModel).load()

"""User specified folder path"""
filepath = None
userSpecified = False

"""EGen Conjunctive graph identifier"""
model_EGen_cg_id = "http://www.theworldavatar.com/kb/UK_Digital_Twin/UK_power_grid/10_bus_model/Model_EGen"

"""Calculate the sum of capacity and total demanding"""
sum_of_capa = sum(query_model.queryAllCapacity(topoAndConsumpPath_Sleepycat, powerPlant_Sleepycat))
print('sum_of_capa is: ', sum_of_capa)
total_demand = sum(query_model.queryRegionalElecConsumption(topoAndConsumpPath_Sleepycat)) * 1000 / (24 * 365) 
print('total_demand is: ', total_demand)
capa_demand_ratio = total_demand/sum_of_capa

### Functions ### 
"""Main function: create the named graph Model_EGen and their sub graphs each EGen"""
def createModel_EGen(store, version_of_model, updateLocalOWLFile = True):
    global filepath, userSpecified, defaultPath_Sleepycat, userSpecifiePath_Sleepycat, userSpecified_Sleepycat  
    if isinstance(store, Sleepycat): 
        cg_model_EGen = ConjunctiveGraph(store=store, identifier = model_EGen_cg_id)
        if userSpecifiePath_Sleepycat == None and userSpecified_Sleepycat:
            print('****Needs user to specify a Sleepycat storage path****')
            userSpecifiePath_Sleepycat = selectStoragePath()
            userSpecifiePath_Sleepycat_ = userSpecifiePath_Sleepycat + '\\' + 'ConjunctiveGraph_UKPowerGrid_EGen'
            sl = cg_model_EGen.open(userSpecifiePath_Sleepycat_, create = False) 
            
        elif os.path.exists(defaultPath_Sleepycat) and not userSpecified_Sleepycat:
            print('****Non user specified Sleepycat storage path, will use the default storage path****')
            sl = cg_model_EGen.open(defaultPath_Sleepycat, create = False)        
        else:
            sl = cg_model_EGen.open(defaultPath_Sleepycat, create = True)   
        
        if sl == NO_STORE:
        # There is no underlying Sleepycat infrastructure, so create it
            cg_model_EGen.open(defaultPath_Sleepycat, create = True)
        else:
            assert sl == VALID_STORE, "The underlying sleepycat store is corrupt"
            
            
    
    EGen = list(query_model.queryEGen(topoAndConsumpPath_Sleepycat))
    
    for egen in EGen:         
    # if EGen[0] != None: # test
    #     egen = EGen[0] # test
        print('################START createModel_EGen#################')
        root_uri = egen[0].split('#')[0]
        namespace = root_uri + HASH
        node_locator = egen[0].split('#')[1]
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
        # link with EGen node in topology
        g.add((URIRef(root_node), URIRef(ontocape_upper_level_system.models.iri), URIRef(egen[0])))
        g.add((URIRef(egen[0]), URIRef(ontocape_upper_level_system.isModeledBy.iri), URIRef(root_node)))
        
        ###add cost function parameters###
        # calculate a, b, c
        uk_egen_costFunc = UK_PG.UKEGenModel_CostFunc(version = version_of_model)
        uk_egen_costFunc = costFuncPara(uk_egen_costFunc, topoAndConsumpPath_Sleepycat, egen, powerPlant_Sleepycat)
        
        if uk_egen_costFunc != None:
            pass
        else: 
            print ('uk_egen_costFunc should be an instance of UKEGenModel_CostFunc')
            return None 
        
        g = AddCostFuncParameterValue(g, root_node, namespace, node_locator, uk_egen_costFunc.CostFuncFormatKey, ontopowsys_PowerSystemModel.CostModel.iri, uk_egen_costFunc.MODEL)
        g = AddCostFuncParameterValue(g, root_node, namespace, node_locator, uk_egen_costFunc.StartupCostKey, ontopowsys_PowerSystemModel.StartCost.iri, \
                                      uk_egen_costFunc.STARTUP, ontocape_derived_SI_units.USD.iri)
        g = AddCostFuncParameterValue(g, root_node, namespace, node_locator, uk_egen_costFunc.ShutdownCostKey, ontopowsys_PowerSystemModel.StopCost.iri, \
                                      uk_egen_costFunc.SHUTDOWN, ontocape_derived_SI_units.USD.iri)
        g = AddCostFuncParameterValue(g, root_node, namespace, node_locator, uk_egen_costFunc.genCostnKey, ontopowsys_PowerSystemModel.genCostn.iri, uk_egen_costFunc.NCOST)
        g = AddCostFuncParameterValue(g, root_node, namespace, node_locator, uk_egen_costFunc.genCost_aKey, t_box.ontopowsys_PowerSystemModel + 'genCostcn-2', uk_egen_costFunc.a)
        g = AddCostFuncParameterValue(g, root_node, namespace, node_locator, uk_egen_costFunc.genCost_bKey, t_box.ontopowsys_PowerSystemModel + 'genCostcn-2', uk_egen_costFunc.b, \
                                      t_box.ontocape_derived_SI_units + 'GBP/MWh') # undified unit
        g = AddCostFuncParameterValue(g, root_node, namespace, node_locator, uk_egen_costFunc.genCost_cKey, t_box.ontopowsys_PowerSystemModel + 'genCostcn-2', uk_egen_costFunc.c, \
                                      t_box.ontocape_derived_SI_units + 'GBP/MWh') # undified unit
            
        ###add EGen model parametor###
        uk_egen_model_ = UK_PG.UKEGenModel(version = version_of_model)
        uk_egen_model_ = initialiseModelVar(uk_egen_model_, topoAndConsumpPath_Sleepycat, egen[0], powerPlant_Sleepycat)
        
        if uk_egen_model_ != None:
            pass
        else: 
            print ('uk_egen_model_ should be an instance of UKEGenModel')
            return None 
        
        g = AddEGenModelVariable(g, root_node, namespace, node_locator, uk_egen_model_.BUSNUMKey, int(uk_egen_model_.BUS), None, \
                                 ontopowsys_PowerSystemModel.BusNumber.iri, ontocape_mathematical_model.Parameter.iri)
        g = AddEGenModelVariable(g, root_node, namespace, node_locator, uk_egen_model_.PG_INPUTKey, float(uk_egen_model_.PG_INPUT), ontocape_derived_SI_units.MW.iri, \
                                 ontopowsys_PowerSystemModel.Pg.iri, ontocape_mathematical_model.InputVariable.iri)
        g = AddEGenModelVariable(g, root_node, namespace, node_locator, uk_egen_model_.QG_INPUTKey, float(uk_egen_model_.QG_INPUT), ontocape_derived_SI_units.Mvar.iri, \
                                 ontopowsys_PowerSystemModel.Qg.iri, ontocape_mathematical_model.InputVariable.iri)
        g = AddEGenModelVariable(g, root_node, namespace, node_locator, uk_egen_model_.QMAXKey, float(uk_egen_model_.QMAX), ontocape_derived_SI_units.Mvar.iri, \
                                 ontopowsys_PowerSystemModel.QMax.iri, ontocape_mathematical_model.Parameter.iri)
        g = AddEGenModelVariable(g, root_node, namespace, node_locator, uk_egen_model_.QMINKey, float(uk_egen_model_.QMIN), ontocape_derived_SI_units.Mvar.iri, \
                                 ontopowsys_PowerSystemModel.QMin.iri, ontocape_mathematical_model.Parameter.iri)
        g = AddEGenModelVariable(g, root_node, namespace, node_locator, uk_egen_model_.VGKey, int(uk_egen_model_.VG), None, \
                                 ontopowsys_PowerSystemModel.Vg.iri, ontocape_mathematical_model.Parameter.iri)
        g = AddEGenModelVariable(g, root_node, namespace, node_locator, uk_egen_model_.MBASEKey, int(uk_egen_model_.MBASE), ontocape_derived_SI_units.MVA.iri, \
                                 ontopowsys_PowerSystemModel.mBase.iri, ontocape_mathematical_model.Parameter.iri)
        g = AddEGenModelVariable(g, root_node, namespace, node_locator, uk_egen_model_.STATUSKey, int(uk_egen_model_.STATUS), None, \
                                 ontopowsys_PowerSystemModel.Status.iri, ontocape_mathematical_model.Parameter.iri)
        g = AddEGenModelVariable(g, root_node, namespace, node_locator, uk_egen_model_.PMAXKey, float(uk_egen_model_.PMAX), ontocape_derived_SI_units.MW.iri, \
                                 ontopowsys_PowerSystemModel.PMax.iri, ontocape_mathematical_model.Parameter.iri)
        g = AddEGenModelVariable(g, root_node, namespace, node_locator, uk_egen_model_.PMINKey, float(uk_egen_model_.PMIN), ontocape_derived_SI_units.MW.iri, \
                                 ontopowsys_PowerSystemModel.PMin.iri, ontocape_mathematical_model.Parameter.iri) 
        g = AddEGenModelVariable(g, root_node, namespace, node_locator, uk_egen_model_.PC1Key, int(uk_egen_model_.PC1), None, \
                                 ontopowsys_PowerSystemModel.Pc1.iri, ontocape_mathematical_model.Parameter.iri)
        g = AddEGenModelVariable(g, root_node, namespace, node_locator, uk_egen_model_.PC2Key, int(uk_egen_model_.PC2), None, \
                                 ontopowsys_PowerSystemModel.Pc2.iri, ontocape_mathematical_model.Parameter.iri)  
        g = AddEGenModelVariable(g, root_node, namespace, node_locator, uk_egen_model_.QC1MINKey, int(uk_egen_model_.QC1MIN), None, \
                                 ontopowsys_PowerSystemModel.QC1Min.iri, ontocape_mathematical_model.Parameter.iri)
        g = AddEGenModelVariable(g, root_node, namespace, node_locator, uk_egen_model_.QC2MINKey, int(uk_egen_model_.QC2MIN), None, \
                                 ontopowsys_PowerSystemModel.QC2Min.iri, ontocape_mathematical_model.Parameter.iri)  
        g = AddEGenModelVariable(g, root_node, namespace, node_locator, uk_egen_model_.QC1MAXKey, int(uk_egen_model_.QC1MAX), None, \
                                 ontopowsys_PowerSystemModel.QC1Max.iri, ontocape_mathematical_model.Parameter.iri)
        g = AddEGenModelVariable(g, root_node, namespace, node_locator, uk_egen_model_.QC2MAXKey, int(uk_egen_model_.QC2MAX), None, \
                                 ontopowsys_PowerSystemModel.QC2Max.iri, ontocape_mathematical_model.Parameter.iri)  
        g = AddEGenModelVariable(g, root_node, namespace, node_locator, uk_egen_model_.RAMP_AGCKey, int(uk_egen_model_.RAMP_AGC), None, \
                                 ontopowsys_PowerSystemModel.Rampagc.iri, ontocape_mathematical_model.Parameter.iri)             
        g = AddEGenModelVariable(g, root_node, namespace, node_locator, uk_egen_model_.RAMP_10Key, int(uk_egen_model_.RAMP_10), None, \
                                 ontopowsys_PowerSystemModel.Ramp10.iri, ontocape_mathematical_model.Parameter.iri)     
        g = AddEGenModelVariable(g, root_node, namespace, node_locator, uk_egen_model_.RAMP_30Key, int(uk_egen_model_.RAMP_30), None, \
                                 ontopowsys_PowerSystemModel.Ramp30.iri, ontocape_mathematical_model.Parameter.iri)     
        g = AddEGenModelVariable(g, root_node, namespace, node_locator, uk_egen_model_.RAMP_QKey, int(uk_egen_model_.RAMP_Q), None, \
                                 ontopowsys_PowerSystemModel.Rampq.iri, ontocape_mathematical_model.Parameter.iri)         
        g = AddEGenModelVariable(g, root_node, namespace, node_locator, uk_egen_model_.APFKey, int(uk_egen_model_.APF), None, \
                                 ontopowsys_PowerSystemModel.APF.iri, ontocape_mathematical_model.Parameter.iri)      
               
        # generate/update OWL files
        if updateLocalOWLFile == True:    
            # specify the owl file storage path
            defaultStoredPath = uk_egen_model.StoreGeneratedOWLs + 'Model_' + node_locator + OWL #default path
        
            # Store/update the generated owl files      
            if os.path.exists(uk_egen_model.StoreGeneratedOWLs) and not userSpecified:
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
        cg_model_EGen.close()       
    return


def AddCostFuncParameterValue(graph, root_node, namespace, node_locator, paraKey, paraType, paraValue, unit = None):
    # parameter iri
    para_iri = namespace + paraKey + node_locator
    value_para_iri = namespace + UK_PG.valueKey + paraKey + node_locator
    # add para node and type
    graph.add((URIRef(root_node), URIRef(ontocape_mathematical_model.hasModelVariable.iri), URIRef(para_iri)))
    graph.add((URIRef(para_iri), RDF.type, URIRef(paraType)))
    #add para value
    graph.add((URIRef(para_iri), URIRef(ontocape_upper_level_system.hasValue.iri), URIRef(value_para_iri)))
    graph.add((URIRef(value_para_iri), RDF.type, URIRef(ontocape_mathematical_model.ModelVariableSpecification.iri)))
    if unit != None:
        graph.add((URIRef(value_para_iri), URIRef(ontocape_upper_level_system.hasUnitOfMeasure.iri), URIRef(unit)))
    graph.set((URIRef(value_para_iri), URIRef(ontocape_upper_level_system.numericalValue.iri), Literal(paraValue)))
   
    return graph

def AddEGenModelVariable(graph, root_node, namespace, node_locator, varKey, varValue, unit, *varType):
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

def initialiseModelVar(EGen_Model, topoAndConsumpPath_Sleepycat, egen_iri, powerPlant_Sleepycat):
    if isinstance (EGen_Model, UK_PG.UKEGenModel):
        pass
    else:
        print('The first argument should be an instence of UKEGenModel')
        return None
    EGen_Model.BUS = query_model.queryBusNumber(topoAndConsumpPath_Sleepycat, egen_iri)
    capa = query_model.queryCapacity(topoAndConsumpPath_Sleepycat, powerPlant_Sleepycat, egen_iri)
    EGen_Model.PG_INPUT = capa * capa_demand_ratio    
    primaryFuel = query_model.queryPrimaryFuel(topoAndConsumpPath_Sleepycat, powerPlant_Sleepycat, egen_iri)
    
    if primaryFuel in ukmf.Renewable:
        EGen_Model.PMAX = EGen_Model.PG_INPUT
        EGen_Model.PMIN = EGen_Model.PG_INPUT
    else:
        EGen_Model.PMAX = capa
        EGen_Model.PMIN = 0
    
    EGen_Model.QMAX = EGen_Model.PMAX
    EGen_Model.QMIN = -EGen_Model.PMAX
    
    return EGen_Model

if __name__ == '__main__':    
    createModel_EGen(store, 2019)    
    print('Terminated')