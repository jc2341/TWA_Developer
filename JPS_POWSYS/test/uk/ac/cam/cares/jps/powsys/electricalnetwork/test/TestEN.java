package uk.ac.cam.cares.jps.powsys.electricalnetwork.test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.jena.ontology.OntModel;
import org.json.JSONObject;

import junit.framework.TestCase;
import uk.ac.cam.cares.jps.base.config.JPSConstants;
import uk.ac.cam.cares.jps.base.discovery.AgentCaller;
import uk.ac.cam.cares.jps.base.query.QueryBroker;
import uk.ac.cam.cares.jps.base.scenario.BucketHelper;
import uk.ac.cam.cares.jps.base.scenario.JPSHttpServlet;
import uk.ac.cam.cares.jps.powsys.electricalnetwork.ENAgent;


public class TestEN extends TestCase {
	
	String dataPath = QueryBroker.getLocalDataPath();
	String baseUrl=dataPath+"/JPS_POWSYS_EN";
	String iriofnetwork="http://www.jparksimulator.com/kb/sgp/jurongisland/jurongislandpowernetwork/JurongIslandPowerNetwork.owl#JurongIsland_PowerNetwork";
	
	String genInfocost= "PREFIX j1:<http://www.theworldavatar.com/ontology/ontopowsys/PowSysRealization.owl#> " 
			+ "PREFIX j2:<http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#> "
			+ "PREFIX j3:<http://www.theworldavatar.com/ontology/ontopowsys/model/PowerSystemModel.owl#> "
			+ "PREFIX j4:<http://www.theworldavatar.com/ontology/meta_model/topology/topology.owl#> "
			+ "PREFIX j5:<http://www.theworldavatar.com/ontology/ontocape/model/mathematical_model.owl#> "
			+ "PREFIX j6:<http://www.theworldavatar.com/ontology/ontocape/chemical_process_system/CPS_behavior/behavior.owl#> "
			+ "PREFIX j7:<http://www.theworldavatar.com/ontology/ontocape/supporting_concepts/space_and_time/space_and_time_extended.owl#> "
			+ "PREFIX j8:<http://www.theworldavatar.com/ontology/ontocape/material/phase_system/phase_system.owl#> "
			+ "SELECT ?entity ?formatvalue ?startupcostvalue ?shutdowncostvalue ?gencostnvalue ?gencostn1value ?gencostn2value ?gencostcvalue "
			+ "WHERE {?entity  a  j1:PowerGenerator  ." 
			+ "?entity   j2:isModeledBy ?model ."
								
			+ "?model   j5:hasModelVariable ?format ."
			+ "?format  a  j3:CostModel  ."
			+ "?format  j2:hasValue ?vformat ."
			+ "?vformat  j2:numericalValue ?formatvalue ." 
			
			+ "?model   j5:hasModelVariable ?startup ."
			+ "?startup  a  j3:StartCost  ."
			+ "?startup  j2:hasValue ?vstartup ."
			+ "?vstartup   j2:numericalValue ?startupcostvalue ." 
			
			+ "?model   j5:hasModelVariable ?shutdown ."
			+ "?shutdown  a  j3:StopCost  ."
			+ "?shutdown  j2:hasValue ?vshutdown ."
			+ "?vshutdown   j2:numericalValue ?shutdowncostvalue ."
			
			+ "?model   j5:hasModelVariable ?gencostn ."
			+ "?gencostn  a  j3:genCostn  ."
			+ "?gencostn  j2:hasValue ?vgencostn ."
			+ "?vgencostn   j2:numericalValue ?gencostnvalue ." 
			
			+ "?model   j5:hasModelVariable ?gencostn1 ."
			+ "?gencostn1  a  j3:genCostcn-1  ."
			+ "?gencostn1  j2:hasValue ?vgencostn1 ."
			+ "?vgencostn1   j2:numericalValue ?gencostn1value ." 
			
			+ "?model   j5:hasModelVariable ?gencostn2 ."
			+ "?gencostn2  a  j3:genCostcn-2  ."
			+ "?gencostn2  j2:hasValue ?vgencostn2 ."
			+ "?vgencostn2   j2:numericalValue ?gencostn2value ." 
			
			+ "?model   j5:hasModelVariable ?gencostc ."
			+ "?gencostc  a  j3:genCostc0  ."
			+ "?gencostc  j2:hasValue ?vgencostc ."
			+ "?vgencostc   j2:numericalValue ?gencostcvalue ." 					
			+ "}";
	
	String branchInfo= "PREFIX j1:<http://www.theworldavatar.com/ontology/ontopowsys/PowSysRealization.owl#> " 
			+ "PREFIX j2:<http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#> "
			+ "PREFIX j3:<http://www.theworldavatar.com/ontology/ontopowsys/model/PowerSystemModel.owl#> "
			+ "PREFIX j4:<http://www.theworldavatar.com/ontology/meta_model/topology/topology.owl#> "
			+ "PREFIX j5:<http://www.theworldavatar.com/ontology/ontocape/model/mathematical_model.owl#> "
			+ "PREFIX j6:<http://www.theworldavatar.com/ontology/ontocape/chemical_process_system/CPS_behavior/behavior.owl#> "
			+ "PREFIX j7:<http://www.theworldavatar.com/ontology/ontocape/supporting_concepts/space_and_time/space_and_time_extended.owl#> "
			+ "PREFIX j8:<http://www.theworldavatar.com/ontology/ontocape/material/phase_system/phase_system.owl#> "
			+ "SELECT ?entity ?BusNumber1value ?BusNumber2value ?resistancevalue ?reactancevalue ?susceptancevalue ?rateAvalue ?rateBvalue ?rateCvalue ?ratiovalue ?anglevalue ?statusvalue ?angleminvalue ?anglemaxvalue "
			
			+ "WHERE {?entity  a  j1:UndergroundCable  ." 
			+ "?entity   j2:isModeledBy ?model ."
			+ "?model   j5:hasModelVariable ?num1 ."
			+ "?num1  a  j3:BusFrom  ."
			+ "?num1  j2:hasValue ?vnum1 ."
			+ "?vnum1   j2:numericalValue ?BusNumber1value ."  //number 1
			
			+ "?model   j5:hasModelVariable ?num2 ."
			+ "?num2  a  j3:BusTo  ."
			+ "?num2  j2:hasValue ?vnum2 ."
			+ "?vnum2   j2:numericalValue ?BusNumber2value ."  //number 2
			
			+ "?model   j5:hasModelVariable ?res ."
			+ "?res  a  j3:R  ."
			+ "?res  j2:hasValue ?vres ."
			+ "?vres   j2:numericalValue ?resistancevalue ."  //resistance
			
			+ "?model   j5:hasModelVariable ?rea ."
			+ "?rea  a  j3:X  ."
			+ "?rea  j2:hasValue ?vrea ."
			+ "?vrea   j2:numericalValue ?reactancevalue ."  //reactance
			
			+ "?model   j5:hasModelVariable ?sus ."
			+ "?sus  a  j3:B  ."
			+ "?sus  j2:hasValue ?vsus ."
			+ "?vsus   j2:numericalValue ?susceptancevalue ."  //susceptance
			
			+ "?model   j5:hasModelVariable ?ratea ."
			+ "?ratea  a  j3:RateA  ."
			+ "?ratea  j2:hasValue ?vratea ."
			+ "?vratea   j2:numericalValue ?rateAvalue ."  //rateA
			
			+ "?model   j5:hasModelVariable ?rateb ."
			+ "?rateb  a  j3:RateB  ."
			+ "?rateb  j2:hasValue ?vrateb ."
			+ "?vrateb   j2:numericalValue ?rateBvalue ."  //rateB
			
			+ "?model   j5:hasModelVariable ?ratec ."
			+ "?ratec  a  j3:RateC  ."
			+ "?ratec  j2:hasValue ?vratec ."
			+ "?vratec   j2:numericalValue ?rateCvalue ."  //rateC
			
			+ "?model   j5:hasModelVariable ?ratio ."
			+ "?ratio  a  j3:RatioCoefficient  ."
			+ "?ratio  j2:hasValue ?vratio ."
			+ "?vratio   j2:numericalValue ?ratiovalue ."  //ratio
			
			+ "?model   j5:hasModelVariable ?ang ."
			+ "?ang  a  j3:Angle  ."
			+ "?ang  j2:hasValue ?vang ."
			+ "?vang   j2:numericalValue ?anglevalue ." //angle
			
			+ "?model   j5:hasModelVariable ?stat ."
			+ "?stat  a  j3:BranchStatus ."
			+ "?stat  j2:hasValue ?vstat ."
			+ "?vstat   j2:numericalValue ?statusvalue ." //status
			
			+ "?model   j5:hasModelVariable ?angmin ."
			+ "?angmin  a  j3:AngleMin  ."
			+ "?angmin  j2:hasValue ?vangmin ."
			+ "?vangmin   j2:numericalValue ?angleminvalue ." //anglemin
			
			+ "?model   j5:hasModelVariable ?angmax ."
			+ "?angmax  a  j3:AngleMax  ."
			+ "?angmax  j2:hasValue ?vangmax ."
			+ "?vangmax   j2:numericalValue ?anglemaxvalue ." //anglemax
			
								
			+ "}";
	
	String busInfo= "PREFIX j1:<http://www.theworldavatar.com/ontology/ontopowsys/PowSysRealization.owl#> " 
			+ "PREFIX j2:<http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#> "
			+ "PREFIX j3:<http://www.theworldavatar.com/ontology/ontopowsys/model/PowerSystemModel.owl#> "
			+ "PREFIX j4:<http://www.theworldavatar.com/ontology/meta_model/topology/topology.owl#> "
			+ "PREFIX j5:<http://www.theworldavatar.com/ontology/ontocape/model/mathematical_model.owl#> "
			+ "PREFIX j6:<http://www.theworldavatar.com/ontology/ontocape/chemical_process_system/CPS_behavior/behavior.owl#> "
			+ "PREFIX j7:<http://www.theworldavatar.com/ontology/ontocape/supporting_concepts/space_and_time/space_and_time_extended.owl#> "
			+ "PREFIX j8:<http://www.theworldavatar.com/ontology/ontocape/material/phase_system/phase_system.owl#> "
			+ "SELECT ?BusNumbervalue ?typevalue ?activepowervalue ?reactivepowervalue ?Gsvalue ?Bsvalue ?areavalue ?VoltMagvalue ?VoltAnglevalue ?BaseKVvalue ?Zonevalue ?VMaxvalue ?VMinvalue "
			
			+ "WHERE {?entity  a  j1:BusNode  ." 
			+ "?entity   j2:isModeledBy ?model ."
			+ "?model   j5:hasModelVariable ?num ."
			+ "?num  a  j3:BusNumber  ."
			+ "?num  j2:hasValue ?vnum ."
			+ "?vnum   j2:numericalValue ?BusNumbervalue ."  //number
			
			+ "?model   j5:hasModelVariable ?type ."
			+ "?type  a  j3:BusType  ."
			+ "?type  j2:hasValue ?vtype ."
			+ "?vtype   j2:numericalValue ?typevalue ." //type

			+ "?model   j5:hasModelVariable ?Pd ."
			+ "?Pd  a  j3:PdBus  ."
			+ "?Pd  j2:hasValue ?vpd ."
			+ "?vpd   j2:numericalValue ?activepowervalue ."  //pd
			
			+ "?model   j5:hasModelVariable ?Gd ."
			+ "?Gd  a  j3:GdBus  ."
			+ "?Gd  j2:hasValue ?vgd ."
			+ "?vgd   j2:numericalValue ?reactivepowervalue ." //Gd

			+ "?model   j5:hasModelVariable ?Gsvar ."
			+ "?Gsvar  a  j3:Gs  ."
			+ "?Gsvar  j2:hasValue ?vGsvar ."
			+ "?vGsvar   j2:numericalValue ?Gsvalue ." //Gs

			+ "?model   j5:hasModelVariable ?Bsvar ."
			+ "?Bsvar  a  j3:Bs  ."
			+ "?Bsvar  j2:hasValue ?vBsvar ."
			+ "?vBsvar   j2:numericalValue ?Bsvalue ." //Bs

			+ "?model   j5:hasModelVariable ?areavar ."
			+ "?areavar  a  j3:Area  ."
			+ "?areavar  j2:hasValue ?vareavar ."
			+ "?vareavar   j2:numericalValue ?areavalue ." //area
			
			+ "?model   j5:hasModelVariable ?VM ."
			+ "?VM  a  j3:Vm  ."
			+ "?VM  j2:hasValue ?vVM ."
			+ "?vVM   j2:numericalValue ?VoltMagvalue ." //Vm
			
			+ "?model   j5:hasModelVariable ?VA ."
			+ "?VA  a  j3:Va  ."
			+ "?VA  j2:hasValue ?vVA ."
			+ "?vVA   j2:numericalValue ?VoltAnglevalue ." //Va

			+ "?model   j5:hasModelVariable ?BKV ."
			+ "?BKV  a  j3:baseKV  ."
			+ "?BKV  j2:hasValue ?vBKV ."
			+ "?vBKV   j2:numericalValue ?BaseKVvalue ." //Base KV

			+ "?model   j5:hasModelVariable ?zvar ."
			+ "?zvar  a  j3:Zone  ."
			+ "?zvar  j2:hasValue ?vzvar ."
			+ "?vzvar   j2:numericalValue ?Zonevalue ." //Zone

			+ "?model   j5:hasModelVariable ?vmaxvar ."
			+ "?vmaxvar  a  j3:VmMax  ."
			+ "?vmaxvar  j2:hasValue ?vvmaxvar ."
			+ "?vvmaxvar   j2:numericalValue ?VMaxvalue ." //Vmax
			
			+ "?model   j5:hasModelVariable ?vminvar ."
			+ "?vminvar  a  j3:VmMin  ."
			+ "?vminvar  j2:hasValue ?vvminvar ."
			+ "?vvminvar   j2:numericalValue ?VMinvalue ." //Vmin
			
											
			+ "}";
	
	String genInfo= "PREFIX j1:<http://www.theworldavatar.com/ontology/ontopowsys/PowSysRealization.owl#> " 
			+ "PREFIX j2:<http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#> "
			+ "PREFIX j3:<http://www.theworldavatar.com/ontology/ontopowsys/model/PowerSystemModel.owl#> "
			+ "PREFIX j4:<http://www.theworldavatar.com/ontology/meta_model/topology/topology.owl#> "
			+ "PREFIX j5:<http://www.theworldavatar.com/ontology/ontocape/model/mathematical_model.owl#> "
			+ "PREFIX j6:<http://www.theworldavatar.com/ontology/ontocape/chemical_process_system/CPS_behavior/behavior.owl#> "
			+ "PREFIX j7:<http://www.theworldavatar.com/ontology/ontocape/supporting_concepts/space_and_time/space_and_time_extended.owl#> "
			+ "PREFIX j8:<http://www.theworldavatar.com/ontology/ontocape/material/phase_system/phase_system.owl#> "
			+ "SELECT ?entity ?BusNumbervalue ?activepowervalue ?reactivepowervalue ?Qmaxvalue ?Qminvalue ?Vgvalue ?mBasevalue ?Statusvalue ?Pmaxvalue ?Pminvalue ?Pc1value ?Pc2value ?Qc1minvalue ?Qc1maxvalue ?Qc2minvalue ?Qc2maxvalue ?Rampagcvalue ?Ramp10value ?Ramp30value ?Rampqvalue ?apfvalue "
			
			+ "WHERE {?entity  a  j1:PowerGenerator  ." 
			+ "?entity   j2:isModeledBy ?model ."
			
			+ "?model   j5:hasModelVariable ?num ."
			+ "?num  a  j3:BusNumber  ."
			+ "?num  j2:hasValue ?vnum ."
			+ "?vnum   j2:numericalValue ?BusNumbervalue ."  //number
			
			+ "?model   j5:hasModelVariable ?Pg ."
			+ "?Pg  a  j3:Pg  ."
			+ "?Pg  j2:hasValue ?vpg ."
			+ "?vpg   j2:numericalValue ?activepowervalue ."  //pg
			
			+ "?model   j5:hasModelVariable ?Qg ."
			+ "?Qg  a  j3:Qg  ."
			+ "?Qg  j2:hasValue ?vqg ."
			+ "?vqg   j2:numericalValue ?reactivepowervalue ." //qg
			
			+ "?model   j5:hasModelVariable ?qmax ."
			+ "?qmax  a  j3:QMax  ."
			+ "?qmax  j2:hasValue ?vqmax ."
			+ "?vqmax   j2:numericalValue ?Qmaxvalue ." //qmax
			
			+ "?model   j5:hasModelVariable ?qmin ."
			+ "?qmin  a  j3:QMin  ."
			+ "?qmin  j2:hasValue ?vqmin ."
			+ "?vqmin   j2:numericalValue ?Qminvalue ." //qmin
			
			+ "?model   j5:hasModelVariable ?Vg ."
			+ "?Vg  a  j3:Vg  ."
			+ "?Vg  j2:hasValue ?vVg ."
			+ "?vVg   j2:numericalValue ?Vgvalue ." //vg
			
			+ "?model   j5:hasModelVariable ?mbase ."
			+ "?mbase  a  j3:mBase  ."
			+ "?mbase  j2:hasValue ?vmbase ."
			+ "?vmbase   j2:numericalValue ?mBasevalue ." //mbase
			
			+ "?model   j5:hasModelVariable ?stat ."
			+ "?stat  a  j3:Status ."
			+ "?stat  j2:hasValue ?vstat ."
			+ "?vstat   j2:numericalValue ?Statusvalue ." //status

			+ "?model   j5:hasModelVariable ?pmax ."
			+ "?pmax  a  j3:PMax  ."
			+ "?pmax  j2:hasValue ?vpmax ."
			+ "?vpmax   j2:numericalValue ?Pmaxvalue ." //pmax
			
			+ "?model   j5:hasModelVariable ?pmin ."
			+ "?pmin  a  j3:PMin  ."
			+ "?pmin  j2:hasValue ?vpmin ."
			+ "?vpmin   j2:numericalValue ?Pminvalue ." //pmin
			
			+ "?model   j5:hasModelVariable ?pc1 ."
			+ "?pc1  a  j3:Pc1  ."
			+ "?pc1  j2:hasValue ?vpc1 ."
			+ "?vpc1   j2:numericalValue ?Pc1value ." //pc1
			
			+ "?model   j5:hasModelVariable ?pc2 ."
			+ "?pc2  a  j3:Pc2  ."
			+ "?pc2  j2:hasValue ?vpc2 ."
			+ "?vpc2   j2:numericalValue ?Pc2value ." //pc2
			
			+ "?model   j5:hasModelVariable ?qc1min ."
			+ "?qc1min  a  j3:QC1Min  ."
			+ "?qc1min  j2:hasValue ?vqc1min ."
			+ "?vqc1min   j2:numericalValue ?Qc1minvalue ." //qc1min
			
			+ "?model   j5:hasModelVariable ?Qc1max ."
			+ "?Qc1max  a  j3:QC1Max  ."
			+ "?Qc1max  j2:hasValue ?vQc1max ."
			+ "?vQc1max   j2:numericalValue ?Qc1maxvalue ." //qc1max
			
			+ "?model   j5:hasModelVariable ?qc2min ."
			+ "?qc2min  a  j3:QC2Min  ."
			+ "?qc2min  j2:hasValue ?vqc2min ."
			+ "?vqc2min   j2:numericalValue ?Qc2minvalue ." //qc2min
		
			+ "?model   j5:hasModelVariable ?Qc2max ."
			+ "?Qc2max  a  j3:QC2Max  ."
			+ "?Qc2max  j2:hasValue ?vQc2max ."
			+ "?vQc2max   j2:numericalValue ?Qc2maxvalue ." //qc2max
			
			+ "?model   j5:hasModelVariable ?rampagc ."
			+ "?rampagc  a  j3:Rampagc  ."
			+ "?rampagc  j2:hasValue ?vrampagc ."
			+ "?vrampagc   j2:numericalValue ?Rampagcvalue ." //rampagc

			+ "?model   j5:hasModelVariable ?ramp10 ."
			+ "?ramp10  a  j3:Ramp10  ."
			+ "?ramp10  j2:hasValue ?vramp10 ."
			+ "?vramp10   j2:numericalValue ?Ramp10value ." //ramp10
			
			+ "?model   j5:hasModelVariable ?ramp30 ."
			+ "?ramp30  a  j3:Ramp30  ."
			+ "?ramp30  j2:hasValue ?vramp30 ."
			+ "?vramp30   j2:numericalValue ?Ramp30value ." //ramp30
			
			+ "?model   j5:hasModelVariable ?rampq ."
			+ "?rampq  a  j3:Rampq  ."
			+ "?rampq  j2:hasValue ?vrampq ."
			+ "?vrampq   j2:numericalValue ?Rampqvalue ." //rampq
			
			+ "?model   j5:hasModelVariable ?apf ."
			+ "?apf  a  j3:APF  ."
			+ "?apf  j2:hasValue ?vapf ."
			+ "?vapf   j2:numericalValue ?apfvalue ." //apf
			
			+ "}";
	
		
	public void testextractOWLinArray() throws IOException, URISyntaxException {
		//String baseurl="C:/JPS_DATA/workingdir/JPS_POWSYS/scenario of powsys";
		//String baseurl="D:/JPS/JParkSimulator-git/JPS_POWSYS/python/model";

		ENAgent b= new ENAgent ();
		//List<String[]>buslist= b.extractOWLinArray(b.readModelGreedy(iriofnetwork),iriofnetwork,genInfo,baseUrl);
		 // List<String[]>buslist=  b.extractOWLinArray(b.readModelGreedy(iriofnetwork),iriofnetwork,branchInfo,"branch",baseUrl);
		   List<String[]>buslist=b.extractOWLinArray(b.readModelGreedy(iriofnetwork),iriofnetwork,busInfo,"bus",baseUrl);
//		     List<String[]>buslist=  b.extractOWLinArray(b.readModelGreedy(iriofnetwork),iriofnetwork,genInfocost,"generatorcost",baseUrl);
	      System.out.println(buslist.size());
	}
	
		
	public void testgeninfogathering () throws IOException {
		
		
		ENAgent b= new ENAgent ();
		
	//String baseurl="C:/JPS_DATA/workingdir/JPS_POWSYS/scenario of Powsys";
	//String baseurl="D:/JPS/JParkSimulator-git/JPS_POWSYS/python/model";

	String busmapurl=baseUrl+"/mappingforbus.csv";
	OntModel model = b.readModelGreedy(iriofnetwork);
		List<String[]>list=b.extractOWLinArray(model,iriofnetwork,busInfo,"bus",baseUrl);
	List<String[]>list2=b.extractOWLinArray(model,iriofnetwork,genInfo,"generator",baseUrl);
	List<String[]>list3=b.extractOWLinArray(model,iriofnetwork,genInfocost,"generatorcost",baseUrl);
		
		List<String[]>list4=b.extractOWLinArray(model,iriofnetwork,branchInfo,"branch",baseUrl);
		QueryBroker broker = new QueryBroker();
		
		
		String content=b.createNewTSV(list,baseUrl+"/mappingforbus.csv",busmapurl);
		broker.put(baseUrl+"/bus.txt", content);
		
		content=b.createNewTSV(list2,baseUrl+"/mappingforgenerator.csv",busmapurl);
		broker.put(baseUrl+"/gen.txt", content);
		
		content=b.createNewTSV(list4, baseUrl+"/mappingforbranch.csv",busmapurl);
		broker.put(baseUrl+"/branch.txt", content);
		
		content=b.createNewTSV(list3, baseUrl+"/mappingforgeneratorcost.csv",busmapurl);
		broker.put(baseUrl+"/genCost.txt", content);
	}

	//not applicable as the directory will always be changed
//	public void testreading() throws IOException {
//		ENAgent b= new ENAgent ();
//		b.readResult(baseUrl+"/gen.txt", 21);
//	}

//  not applicable as the directory will always be changed
//	public void testdoconversion() throws IOException, URISyntaxException {
//		ENAgent b= new ENAgent ();
//		OntModel model = b.readModelGreedy(iriofnetwork);
//		String iriofnetwork="http://www.jparksimulator.com/kb/sgp/jurongisland/jurongislandpowernetwork/JurongIslandPowerNetwork.owl#JurongIsland_PowerNetwork";
//		//String baseurl="C:/JPS_DATA/workingdir/JPS_POWSYS/scenario of Powsys";
//		//String baseurl="D:/JPS/JParkSimulator-git/JPS_POWSYS/python/model";
//
//			List<String[]>list=b.extractOWLinArray(model,iriofnetwork,busInfo,"bus",baseUrl);
//		b.doConversion(model,iriofnetwork,baseUrl,"PF",list);
//	}
	
//  not applicable as the directory will always be changed
//	public void testrunmodel() throws IOException {
//		ENAgent agent = new ENAgent();
//		agent.runModel(baseUrl);
//		
//	}
	
	public void testStartSimulationCalling() throws IOException  {
		//why need to convert to localhost instead of twa??
		//small scenario is auto generated
		
		//ENAgent agent = new ENAgent();
		ENAgent agent = new ENAgent();
		
		String iriofnetwork = "http://www.jparksimulator.com/kb/sgp/jurongisland/jurongislandpowernetwork/JurongIslandPowerNetwork.owl#JurongIsland_PowerNetwork";
//		String baseUrl="C:/JPS_DATA/workingdir/JPS_POWSYS/scenario of Powsys";
		//String baseUrl="D:/JPS/JParkSimulator-git/JPS_POWSYS/python/model";
		baseUrl = null;
		agent.startSimulation(iriofnetwork, baseUrl,"OPF");
		
	}
	
	
	//still not completed yet
	public void testStartSimulationCallingWithScenarioCase() throws IOException  {

		JSONObject jo = new JSONObject();
		String iriofnetwork = "http://www.jparksimulator.com/kb/sgp/jurongisland/jurongislandpowernetwork/JurongIslandPowerNetwork.owl#JurongIsland_PowerNetwork";
		
		jo.put("electricalnetwork", iriofnetwork);
		
		String scenarioUrl = BucketHelper.getScenarioUrl("testENScenario");
		JPSHttpServlet.enableScenario(scenarioUrl);	
		jo.put(JPSConstants.SCENARIO_URL, scenarioUrl);
		
		String usecaseUrl = BucketHelper.getUsecaseUrl();
		JPSHttpServlet.enableScenario(scenarioUrl, usecaseUrl);	
		jo.put(JPSConstants.SCENARIO_USE_CASE_URL,  usecaseUrl);
		
		String resultStart = AgentCaller.executeGetWithJsonParameter("JPS_POWSYS/ENAgent/startsimulationPF", jo.toString());
	}
	
	public void testStartSimulationCallingBaseScenario() throws IOException  {

		String iriofnetwork = "http://www.jparksimulator.com/kb/sgp/jurongisland/jurongislandpowernetwork/JurongIslandPowerNetwork.owl#JurongIsland_PowerNetwork";
		String dataPath = QueryBroker.getLocalDataPath();
		String baseUrl = dataPath + "/JPS_POWSYS_EN";
		new ENAgent().startSimulation(iriofnetwork, baseUrl, "OPF");	
	}
	
	public void testStartSimulationCallingNonBaseScenario() throws IOException  {

		String scenarioUrl = BucketHelper.getScenarioUrl("testENScenario");
		JPSHttpServlet.enableScenario(scenarioUrl);	
		//new ScenarioClient().setOptionCopyOnRead(scenarioUrl, true);
			
		String iriofnetwork = "http://www.jparksimulator.com/kb/sgp/jurongisland/jurongislandpowernetwork/JurongIslandPowerNetwork.owl#JurongIsland_PowerNetwork";
		String dataPath = QueryBroker.getLocalDataPath();
		String baseUrl = dataPath + "/JPS_POWSYS_EN";
		new ENAgent().startSimulation(iriofnetwork, baseUrl, "OPF");
	}
}
