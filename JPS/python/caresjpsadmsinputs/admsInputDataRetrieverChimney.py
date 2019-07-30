'''
module that retreives and pack adms input info
'''
import rdflib
#from pyproj import Proj, transform
import requests
import json
import math
import sys
import os
import rdflib.plugins.sparql.results.jsonresults as jsresult
from collections import namedtuple
from admsSrcChimney import admsSrc
from admsPolygon import Polygon
from admsPol import admsPol
from caresjpsutil import PythonLogger
import cobbling
import requests
from pyproj import Proj, transform

sourceCRS = Proj(init='epsg:3857')
pm10er=10
pm25er=0
# targetCRS = Proj(init='epsg:3414')

def setpm10(value):
    global pm10er
    pm10er=value
        
def setpm25(value):
    global pm25er
    pm25er=value

class admsInputDataRetriever(object):
    BDN = namedtuple('BDN', ['BldNumBuildings','BldName','BldType','BldX','BldY','BldHeight', 'BldLength', 'BldWidth', 'BldAngle'])
    OPT = namedtuple('OPT', ['OptNumOutputs','OptPolName','OptInclude','OptShortOrLong', 'OptSamplingTime','OptSamplingTimeUnits','OptCondition','OptNumPercentiles','OptNumExceedences','OptPercentiles','OptExceedences','OptUnits','OptGroupsOrSource','OptAllSources','OptNumGroups','OptIncludedGroups','OptIncludedSource','OptCreateComprehensiveFile'])

    def __init__(self, topnode, bdnnode=None, range=None, pollutants =['HC'], srcLimit = 5, bdnLimit = 5, filterSrc = False, rawBdn = None, targetCRS = None):
        '''constructor
        inputs:
        range - user input range {'xmin', 'xmax', 'ymin', 'ymax'}, actual range is the min(user range, region envelope(e.g. jurongisland))
        topnode - uri of topnode to begin search within tree struct,
        filtersrc - true if use all children under topnode as src, false to use topnode as src directly
        bdnnode - top/colleciton node of building
        pollutants: pollutant to test
        srcLimit: limit of src number, actual number might fewer
        bdnLimit: limit of bdn number, actual number might fewer
        '''
        self.address  = None
        self.pollutants = pollutants
        self.topnode = topnode
        self.bdnnode = bdnnode
        self.srcLimit = srcLimit
        self.bdnLimit = bdnLimit
        # TODO-AE remove filterSrc and topnode
        self.filterSrc = False
        self.rawBdn = rawBdn

        self.range = self.getRange(range, targetCRS)
        print("RANGE HERE")
        #print(self.range)
        self.pythonLogger = PythonLogger('admsInputDataRetrieverChimney.py')

    def getRange(self, userrange, targetCRS):
#         max = transform(sourceCRS, targetCRS, userrange['xmax'], userrange['ymax'])
#         min = transform(sourceCRS, targetCRS, userrange['xmin'], userrange['ymin'])
        
#         return ((min[0], max[0]), (max[1] - 1000, min[1]))
#         return ((min[0], max[0]), (min[1], max[1]))
        return ((float(userrange['xmin']), float(userrange['xmax'])), (float(userrange['ymin']), float(userrange['ymax'])))

    def getSrcData(self):
        '''get all sourced data : 
        returns: data object 
        '''        
#         self.pythonLogger.postInfoToLogServer('start getSrcData(), topnode='+self.topnode)
        
        if isinstance(self.topnode, list):
            filtered = (*self.topnode,) 
        else :
            filtered = (self.topnode,)
        
        s = set()#make a set of substance to query later
        result = []
        for uri in filtered:
            print("connecting: {:s}".format(uri))
            self.connectDB(uri, connectType = 'parse')
            qdata = self.query(
                """
                PREFIX sys: <http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#>
                PREFIX space_and_time_extended: <http://www.theworldavatar.com/ontology/ontocape/supporting_concepts/space_and_time/space_and_time_extended.owl#>
                PREFIX plant:<http://www.theworldavatar.com/ontology/ontocape/chemical_process_system/CPS_realization/plant.owl#>
                PREFIX topology:<http://www.theworldavatar.com/ontology/meta_model/topology/topology.owl#>
                PREFIX behavior: <http://www.theworldavatar.com/ontology/ontocape/chemical_process_system/CPS_behavior/behavior.owl#>
                PREFIX chemical_process_system: <http://www.theworldavatar.com/ontology/ontocape/chemical_process_system/chemical_process_system.owl#>
                PREFIX techsys: <http://www.theworldavatar.com/ontology/ontocape/upper_level/technical_system.owl#>
                PREFIX phase_system:<http://www.theworldavatar.com/ontology/ontocape/material/phase_system/phase_system.owl#>
                PREFIX material: <http://www.theworldavatar.com/ontology/ontocape/material/material.owl#>
                PREFIX substance:<http://www.theworldavatar.com/ontology/ontocape/material/substance/substance.owl#>
                
                SELECT distinct ?o ?diameter ?temp ?height ?massflow ?heatcapa ?density ?moleweight
                WHERE {
                
                ?o plant:hasHeight ?he.
                ?he sys:hasValue ?hv.
                ?hv sys:numericalValue ?height .
  
                  ?o  techsys:realizes ?process.
                ?process topology:hasOutput ?stream.
                ?stream chemical_process_system:refersToGeneralizedAmount ?ga.
                
                ?ga sys:hasProperty ?ve.
                ?ve a behavior:ConvectiveMassFlowrate .
                ?ve sys:hasValue ?vv.
                ?vv sys:numericalValue ?massflow.
                
                ?o a plant:Pipe .
                ?o plant:hasInsideDiameter ?de . #?dev sys:hasValue ?de.
                ?de sys:hasValue ?ed.
                ?ed sys:numericalValue ?diameter.
                
                ?phase phase_system:has_temperature ?tempE.
                ?tempE sys:hasValue ?vte.
                ?vte sys:numericalValue ?temp .
                
                
                ?cp a phase_system:ThermodynamicStateProperty.
                ?cp sys:hasValue ?cpv.
                ?cpv sys:numericalValue ?heatcapa.
                
                ?singlephase a phase_system:SinglePhase.
                ?singlephase phase_system:has_density ?den.
                ?den a phase_system:Density.
                ?den sys:hasValue ?denv.
                ?denv sys:numericalValue ?density.
                
                
                ?mw a substance:MolecularWeight.
                ?mw sys:hasValue ?mwv.
                ?mwv sys:numericalValue ?moleweight. 
                OPTIONAL {
                ?o space_and_time_extended:hasGISCoordinateSystem ?coe .
                ?coe space_and_time_extended:hasProjectedCoordinate_x ?xe.
                ?xe sys:hasValue ?xv.
                ?xv sys:numericalValue ?x.
                
                ?coe space_and_time_extended:hasProjectedCoordinate_y ?ye.
                ?ye sys:hasValue ?yv.
                ?yv sys:numericalValue ?y.
                
                }
                }
            """)

            print("")
            print("")
            for row in qdata:
#                 print(row)
#                 print(type(row))
                print(row.asdict())
            aresult, = [row.asdict() for row in qdata]
            
            
            print(aresult)
            

            
            qdataC = self.query("""
                    PREFIX sys: <http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#>
            PREFIX substance:<http://www.theworldavatar.com/ontology/ontocape/material/substance/substance.owl#>
            PREFIX part:<http://www.theworldavatar.com/ontology/ontocape/chemical_process_system/CPS_behavior/behavior.owl#>

            SELECT DISTINCT  ?content
            WHERE {{
            {{
            ?mix a substance:Mixture.
            ?mix sys:containsDirectly  ?content. 
            }} UNION {{
             ?content   a part:ParticulateMaterialAmount.
            }}
            }}

            """)

            contents = [row['content'].toPython() for row in qdataC]

            s = s.union(contents)
            aresult['content'] = contents
            

            qdataERate = self.query("""
                    PREFIX sys: <http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#>
           PREFIX substance:<http://www.theworldavatar.com/ontology/ontocape/material/substance/substance.owl#>
           PREFIX behavior:<http://www.theworldavatar.com/ontology/ontocape/chemical_process_system/CPS_behavior/behavior.owl#>

            SELECT DISTINCT  ?er ?v
            WHERE {{
            ?mix sys:hasProperty  ?er.
            ?er  a behavior:ConvectiveMassFlowrate.
                         ?er sys:hasValue ?erv.
             ?erv sys:numericalValue ?v
            }}
            """)

            emissionrates = {row['er'].toPython():row['v'].toPython() for row in qdataERate}
            #todo: sort emissionrates to order of substances

            sorteder =  []
            for content in contents:
                name = content.split('#')[1]
                for ername, v in emissionrates.items():
                    if name in ername:
                        print('name='+name)
                        if "Particulate" in name:
                            print('name2='+name)
                            sorteder.append(pm10er)
                            sorteder.append(pm25er)
                        else:
                            sorteder.append(v)

            print(sorteder)
            print('sorteder printed')
            
            aresult['emissionrates'] = sorteder

            result.append(aresult)





            

        
        print("FILTERED :")
        print(result)
        



        
        '''   
        #use this if need query substance separately
        #query substance for substance related data
        cMap = {}

        
        #hard coding for now before I get a better solusion
        self.connectDB("http://www.theworldavatar.com/ontology/ontocape/material/substance/substance.owl", connectType = 'parse')

        template = """
            PREFIX sys: <http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#>
            PREFIX space_and_time_extended: <http://www.theworldavatar.com/ontology/ontocape/supporting_concepts/space_and_time/space_and_time_extended.owl#>
            PREFIX sub:<http://www.theworldavatar.com/ontology/ontocape/material/substance/substance.owl#>
            PREFIX behavior: <http://www.theworldavatar.com/ontology/ontocape/chemical_process_system/CPS_behavior/behavior.owl#>
            PREFIX phase_system:<http://www.theworldavatar.com/ontology/ontocape/material/phase_system/phase_system.owl#>
            SELECT DISTINCT  ?o ?hc ?d ?mw
            WHERE {{
            <{0}> behavior:hasHeatCapacityRatio  ?hce.   #heatcapacity ratio
            ?hce sys:hasValue ?hcv . 
            ?hcv sys:numericalValue ?hc.
           
            <{0}> phase_system:has_density ?de .        #density
           ?de sys:hasValue ?dv . 
            ?dv sys:numericalValue ?d.
           
             <{0}> sys:hasProperty ?mwe.                #molecular weight
             ?mwe a sub:MolecularWeight .
             ?mwe sys:hasValue ?mwv . 
            ?mwv sys:numericalValue ?mw.
            }}
            LIMIT 1
       """

        for sub in s:
            print (sub)
            print (template.format(sub))
            cMap[sub], = self.query(template.format(sub))

               
     
        
        #todo:construct an array of contents, and corresponding content data
        

            subData = {}
            subData['mw'] = [cMap[content]['mw'] for content in src['content'] ]
            subData['d'] = [cMap[content]['d'] for content in src['content'] ]
            subData['hc'] = [cMap[content]['hc'] for content in src['content'] ]
            pollutantnames = [self.polIRI2Name(content) for content in src['content'] ]
            print(subData)
            print('!!!!!!!!!!!')
            print(src['y'].datatype)
            print(src['y'].value)
            print(type(src['y'].value))

            print(src['y'].toPython)
            print(float(src['y']))

        '''
        packed = []
        for src in result:
            SrcNumPollutants = len(src['content'])
            pollutantnames = [self.polIRI2Name(content) for content in src['content'] ]
            #add the pm 2.5 as the particulat is mapped to 2 different category
            if 'http://www.theworldavatar.com/kb/ships/Chimney-1.owl#Particulate-001' in src['content']:
                SrcNumPollutants+=1 
                pollutantnames.append('PM2.5')
                
                


            newSrc = admsSrc(SrcName = src['o'].toPython(), SrcHeight = src['height'].toPython(), SrcDiameter = float(src['diameter'].toPython()), SrcPolEmissionRate = src['emissionrates'], SrcPollutants = pollutantnames,SrcTemperature = src['temp'].toPython(), SrcMolWeight = src['moleweight'].toPython(), SrcDensity = float(src['density'].toPython()), SrcSpecHeatCap = src['heatcapa'].toPython(), SrcNumPollutants=SrcNumPollutants, SrcMassFlux = src['massflow'].toPython())
            print("hello")
            print(newSrc)
            packed.append(newSrc)
        return packed

    
    def filterBdnEnvelope(self):
        '''
        Get all buildings within range by comparing range with envelope
        return list of building url
        '''  
        xRange, yRange = self.range
        qb =self.query('''
            PREFIX sys: <http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#>
            PREFIX citygml: <http://www.theworldavatar.com/ontology/ontocitygml/OntoCityGML.owl#>
            PREFIX space_and_time_extended: <http://www.theworldavatar.com/ontology/ontocape/supporting_concepts/space_and_time/space_and_time_extended.owl#>
            PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>

              SELECT distinct ?bdn 
              WHERE{{
              ?cityM a citygml:CityModelType.
              ?cityM citygml:boundedBy ?envelope .
              ?envelope a citygml:EnvelopeType.            # get all envelopes
              ?envelope citygml:upperCornerPoint ?upoint.  # get bounds of envelope
              ?upoint space_and_time_extended:hasGISCoordinateSystem ?uco.  
              ?uco   space_and_time_extended:hasProjectedCoordinate_x ?uxe.    
              ?uxe sys:hasValue ?uxv.
              ?uxv sys:numericalValue ?ux.
              ?uco   space_and_time_extended:hasProjectedCoordinate_y ?uye.
              ?uye sys:hasValue ?uyv.
              ?uyv sys:numericalValue ?uy.
              ?envelope citygml:lowerCornerPoint ?lpoint.
              ?lpoint space_and_time_extended:hasGISCoordinateSystem ?lco.  
              ?lco   space_and_time_extended:hasProjectedCoordinate_x ?lxe.    
              ?lxe sys:hasValue ?lxv.
              ?lxv sys:numericalValue ?lx.
              ?lco   space_and_time_extended:hasProjectedCoordinate_y ?lye.
              ?lye sys:hasValue ?lyv.
              ?lyv sys:numericalValue ?ly.
              ?cityM citygml:cityObjectMember ?bdn .  #get bdn belongs to filterd envelope
                Filter(xsd:double(?ly) > "{1}"^^xsd:double  && xsd:double(?uy) < "{2}"^^xsd:double && xsd:double(?lx) > "{3}"^^xsd:double && xsd:double(?ux) < "{4}"^^xsd:double) #filter envelope within range
                        }} 
            LIMIT {0}   #limit of building num
            '''.format(self.bdnLimit, *yRange, *xRange))

        ########todo: in future delete stud data
        return tuple(row['bdn'] for row in qb)


    def coreBdn2Src(self):
#         self.pythonLogger.postInfoToLogServer('calculate main building for each src')
        #compare src coords to each bdn
        y_midpoint = (self.range[1][0] + self.range[1][1])/2
        x_midpoint = (self.range[0][0] + self.range[0][1])/2
        for src in self.rawSrc:
            closed, dClosed, first = None, sys.maxsize, True
            #print('find closed bdn for src: '+src.SrcName+" with x: "+str(src.SrcX1) +" y: "+str(src.SrcY1))
#             self.pythonLogger.postInfoToLogServer('find closed bdn for src: '+src.SrcName+" with x: "+str(src.SrcX1) +" y: "+str(src.SrcY1) + ", no of buildings=" + str(len(self.rawBdn.BldX)))
            for i in range(len(self.rawBdn.BldX)):
                
                #print('bdn x: ' +str( self.rawBdn.BldX[i]))
#                 print(type(self.rawBdn.BldX[i]))
#                 print(type(src.SrcX1))
#                 print("{0} - {1}, {2} - {3}".format(self.rawBdn.BldX[i], x_midpoint, self.rawBdn.BldY[i], y_midpoint))
                dx, dy = self.rawBdn.BldX[i] - x_midpoint, self.rawBdn.BldY[i] - y_midpoint 
                d = dx * dx + dy * dy
                if first:
                    dClosed = d
                    closed =  self.rawBdn.BldName[i]
                    first = False
                #print('d:{0} dclosed:{1}'.format(d, dClosed))
                if d - dClosed  < 0:
                    closed = self.rawBdn.BldName[i]
                    dClosed = d
                    print('new smallest distance: '+str(dClosed))
            if closed is not None:
                src.setMainBuilding(closed)

            else: #err handling, something is wrong if no closed building is found, just throw it    
                raise Exception('Dear lord, no closed buildinf found for src: '+src.SrcName)        

    def getOpt(self, PolNames, SrcNames):
        numPol = len(PolNames)
        return self.OPT(numPol,PolNames, [1]*numPol,[0]*numPol,[1]*numPol,[3]*numPol,[0]*numPol,[0]*numPol,[0]*numPol,[0]*80,[0]*80,['ug/m3']*numPol,0,0,1,"Grouptank001","SrcNames",0)

    def polIRI2Name(self, polIRI):
        substances = {
        #'http://www.theworldavatar.com/ontology/ontocape/material/substance/substance.owl#chlorine':'Cl2',
        'http://www.theworldavatar.com/ontology/ontocape/material/substance/chemical_species.owl#Nitrogen__dioxide':'NO2',
        'http://www.theworldavatar.com/ontology/ontocape/material/substance/chemical_species.owl#Carbon__monoxide':'CO',
        'http://www.theworldavatar.com/ontology/ontocape/material/substance/chemical_species.owl#Carbon__dioxide':'CO2',
        'http://www.theworldavatar.com/ontology/ontocape/material/substance/chemical_species.owl#Sulfur__dioxide':'SO2',
        'http://www.theworldavatar.com/ontology/ontocape/material/substance/chemical_species.owl#Ozone':'O3',
        'http://www.theworldavatar.com/ontology/ontocape/material/substance/pseudocomponent.owl#Unburned_Hydrocarbon':'HC',
        'http://www.theworldavatar.com/ontology/ontocape/material/substance/pseudocomponent.owl#Nitrogen__oxides':'NOx',
        #'http://www.theworldavatar.com/kb/ships/Chimney-1.owl#Particulate-001':'Particulate001',
        'http://www.theworldavatar.com/kb/ships/Chimney-1.owl#Particulate-001':'PM10'
        #'http://www.theworldavatar.com/kb/ships/Chimney-1.owl#Particulate-001':'PM2.5'

        }

        if polIRI in substances.keys():
            print('Found: '+ substances[polIRI])
            return substances[polIRI]
        else:
            print (polIRI + 'Not found !!!!')
            #raise Exception('This substance is not defined!!!!')
            return None
  
    def getWeather(self):
        '''
        get weather data,
        for now we trigger a python script to write the .met file directly
        '''
        #finish writing met
        metpath = "C://JPS_DATA/workingdir/JPS/ADMS/test.met" #later it will be replaced, just ignored
        self.pythonLogger.postInfoToLogServer('path for adms met file='+metpath)
        return metpath
    
    def getBkg(self):
        '''
        get background data,
        for now we trigger a python script to write the .met file directly
        '''
        #finish writing met
        bkgpath = "C://JPS_DATA/workingdir/JPS/ADMS/testbackgrnd.bkg" #later it will be replaced, just ignored
        self.pythonLogger.postInfoToLogServer('path for adms bkg file='+bkgpath)
        return bkgpath

    def getPol(self):
        self.connectDB('http://www.theworldavatar.com/kb/ships/Chimney-1.owl', connectType = 'parse')
        qb =self.query('''
            PREFIX j.0: <http://www.theworldavatar.com/ontology/ontocape/chemical_process_system/CPS_behavior/behavior.owl#>
            PREFIX j.3: <http://www.theworldavatar.com/ontology/ontocape/supporting_concepts/geometry/geometry.owl#>
            PREFIX system: <http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#>
            PREFIX j.1:<http://www.theworldavatar.com/ontology/ontocape/material/phase_system/phase_system.owl#>

              SELECT distinct ?p ?diameter ?density ?massFraction
              WHERE{
              ?p a j.0:SingleParticle.
              ?p j.3:has_length ?de.
              ?de system:hasValue ?ve.
              ?ve system:numericalValue ?diameter.
              ?p j.1:has_density ?dene.
              ?dene system:hasValue ?dve.
              ?dve system:numericalValue ?density.
              ?p system:hasProperty ?mf.
              ?mf system:hasValue ?mfve.
              ?mfve system:numericalValue ?massFraction.
              
                        }
              ''')

        print('now query')
        for row in qb:
            print(row)
            
        self.pythonLogger.postInfoToLogServer([row['diameter'] for row in qb])
        rawSol =  admsPol('Particulate001',len(qb),[row['diameter'].toPython() for row in qb],[row['density'].toPython() for row in qb],[float(row['massFraction'].toPython()) for row in qb] )
        
        qb2 =self.query('''
            PREFIX j.0: <http://www.theworldavatar.com/ontology/ontocape/chemical_process_system/CPS_behavior/behavior.owl#>
            PREFIX system: <http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#>
            PREFIX j.1:<http://www.theworldavatar.com/ontology/ontocape/material/phase_system/phase_system.owl#>

              SELECT distinct ?massrate
              WHERE{
              ?p a j.0:ParticulateMaterialAmount.
              ?p system:hasProperty ?de.
              ?de system:hasValue ?ve.
              ?ve system:numericalValue ?massrate.
                        }
              ''')


            
        for massf in qb2:
            print(massf['massrate'].toPython())

        arrpm10D=[]
        arrpm25D=[]
        arrpm10massf=[]
        arrpm25massf=[]
        arrpm10Density=[]
        arrpm25Density=[]
        arrpm10fraction=[]
        arrpm25fraction=[]
        
        for row in qb:
            if row['diameter'].toPython()<0.00001:
                arrpm10D.append(row['diameter'].toPython())
                arrpm10Density.append(row['density'].toPython())
                indivrate1=float(row['massFraction'])*float(massf['massrate'])
                arrpm10massf.append(indivrate1)
                print(indivrate1)
                
            elif row['diameter'].toPython()<0.0000025:
                arrpm25D.append(row['diameter'].toPython())
                arrpm25Density.append(row['density'].toPython())
                indivrate2=float(row['massFraction'])*float(massf['massrate'])
                arrpm25massf.append(indivrate2)
                print(indivrate2)
                
        
        for i in range(len(arrpm10massf)):
            arrpm10fraction.append(arrpm10massf[i]/sum(arrpm10massf))
            
        for j in range(len(arrpm25massf)):
            arrpm25fraction.append(arrpm25massf[j]/sum(arrpm25massf))
                
        pm10er=setpm10(sum(arrpm10massf))
        pm25er=setpm25(sum(arrpm25massf))

        rawSolpm25 =  admsPol('PM2.5',len(arrpm25D),arrpm25D,arrpm25Density,arrpm25fraction)   
        rawSolpm10 =  admsPol('PM10',len(arrpm10D),arrpm10D,arrpm10Density,arrpm10fraction)      
            
        print(arrpm10fraction)   
                
        #rawSolpm25 =  admsPol('PM2.5',len(qbpm25),[row['diameter'].toPython() for row in qbpm25],[row['density'].toPython() for row in qbpm25],[float(row['massFraction'].toPython()) for row in qbpm25] )
        #print(rawSol)
        self.pythonLogger.postInfoToLogServer(rawSol)
         
        #return rawSol
        return rawSolpm10,rawSolpm25





    def get(self):
        '''main function, get all related info for adms
        returns: complete data for adms
        '''  
        pm10,pm25=self.getPol()    

        #get all src data
        self.rawSrc = self.getSrcData()
        ##todo: think about this case: when nothing is found ,where should we handle it?
        ##then nothing should be written, and catch this exception and mute it in main function
        if self.rawSrc is None:
            raise Exception("No src in found to requiries")


        print('raw building: ')
        print(self.rawBdn)
        rawOpt = self.getOpt(self.pollutants, [s.SrcName for s in self.rawSrc])
        self.coreBdn2Src()

        #for debuging, in future,define this for data type, i dont think it auto rpint for objet
        for src in self.rawSrc:
            print(src)
        
        met = self.getWeather()
        xran,yran = self.range
        grd = xran[0], yran[0], xran[1], yran[1]
        
        pol = pm10,pm25
        bkg=self.getBkg()
        

        #return {'Src': self.rawSrc, 'Bdn': self.rawBdn, 'Opt': rawOpt, 'Met': met, 'Grd':grd}
        return {'Src': self.rawSrc, 'Opt': rawOpt, 'Met': met, 'Grd':grd, 'Pol':pol, 'Bkg':bkg }
        
        


    def queryEndpoint(self, str):
        def literal2TLit(sparqlres):

            if 'results' not in sparqlres:
                return
            for row in sparqlres['results']['bindings']:
                for name,value in row.items():
                    if value['type'] == 'literal' and 'datatype' in value:
                        value['type'] = 'typed-literal'

        print('requesting @ '+self.address+" with query:")
        #print(str)
        resp = requests.get(self.address, params = {'query':str}, timeout = 1500, headers = {'user-agent': 'my-app/0.0.1'})
        print('raw resp:')

        print(resp.json())
        result = resp.json()
        literal2TLit(result)
        print('literal to typed-literal')
        print(result)
        qres = jsresult.JSONResult(result)#json decoded
        print('after parse:')
        print(qres)
        return qres


    
    def queryLocalGraph(self, str): 
        qres = self.g.query(str)
        return qres

    def Uri2Local(uri):
        '''replace a uri to local address
        inputs:
        uri - uri to be changed
        returns: string - local address
        '''
        return uri.replace("http://www.jparksimulator.com",config.root ).replace("http://www.theworldavatar.com",config.root)

    def remote2local(self, func):
        '''decorator to change connection function to local connection by replacing given iri to local address
        '''
        def functionWrapper(self, address):
            address = self.Uri2Local(address)
            func(self, address)
        return functionWrapper    
    
    def connectDB(self, address, connectType = 'endpoint'):
        '''connect to db anyhow (we use rdflib graph parse now)
        '''
        def connectDBActual( address):
            '''
            Actual method to connect to db
            '''
            #obsolete: use rdflib locally
            self.address = address
            if connectType is 'parse':
                self.g = rdflib.Graph()#comment out in future
                self.g.parse(address)#comment out in future


        self.qmethodMap = {'parse': self.queryLocalGraph, 'endpoint':self.queryEndpoint}

        if not sameGraph(address, self.address):
            print ('parsing graph: '+ address)
            if connectType not in self.qmethodMap:
                raise exception('db connection method not defined')
            #self.connectType = connectType
            self.query = self.qmethodMap[connectType]
            connectDBActual(address)                 
       

        
               


def sameGraph(uri1, uri2):
    def trimloc(uri):
        if uri is None:
            return None
        else:
            return uri.split('#')[0]
    return trimloc(uri1) == trimloc(uri2)

#def defineCoordConvert(inCode, outCode):
#    inProj = Proj(init=inCode)
#    outProj = Proj(init=outCode)
#    def coordConvert(x,y):
#        return transform(inProj, outProj, x, y)
#    return coordConvert

def uri2name(uri):
    base = 'http://www.theworldavatar.com/'
    return uri.split('#')[1]

ins=admsInputDataRetriever("http://www.theworldavatar.com/kb/ships/Chimney-1.owl")
ins.getSrcData()