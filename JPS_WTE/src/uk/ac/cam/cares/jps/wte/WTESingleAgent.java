package uk.ac.cam.cares.jps.wte;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import com.google.common.primitives.Ints; 

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Resource;
import org.json.JSONObject;

import uk.ac.cam.cares.jps.base.query.JenaHelper;
import uk.ac.cam.cares.jps.base.query.JenaResultSetFormatter;
import uk.ac.cam.cares.jps.base.query.QueryBroker;
import uk.ac.cam.cares.jps.base.scenario.BucketHelper;
import uk.ac.cam.cares.jps.base.scenario.JPSContext;
import uk.ac.cam.cares.jps.base.scenario.JPSHttpServlet;
import uk.ac.cam.cares.jps.base.util.MatrixConverter;
import uk.ac.cam.cares.jps.wte.WastetoEnergyAgent;

@WebServlet(urlPatterns= {"/processresult"})
public class WTESingleAgent extends JPSHttpServlet {
	/** Find offsite technologies that use technology
	 * 
	 */
	public static String Offsiteoutput = "PREFIX j1:<http://www.theworldavatar.com/ontology/ontowaste/OntoWaste.owl#> "
			+ "PREFIX j2:<http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#> "
			+ "PREFIX j3:<http://www.theworldavatar.com/ontology/ontopowsys/PowSysPerformance.owl#> "
			+ "PREFIX j4:<http://www.theworldavatar.com/ontology/meta_model/topology/topology.owl#> "
			+ "PREFIX j5:<http://www.theworldavatar.com/ontology/ontocape/model/mathematical_model.owl#> "
			+ "PREFIX j6:<http://www.w3.org/2006/time#> "
			+ "PREFIX j7:<http://www.theworldavatar.com/ontology/ontocape/supporting_concepts/space_and_time/space_and_time_extended.owl#> "
			+ "PREFIX j8:<http://www.theworldavatar.com/ontology/ontotransport/OntoTransport.owl#> "
			+ "SELECT ?entity ?Tech1 " 
			+ "WHERE {"
			+ "?entity   j1:useTechnology ?Tech1 ."  
			+ "}"
			+ "ORDER BY DESC(?Tech1)";

	
	/** derive property that defines numerical values as described in the ontology
	 * 
	 * @param jenaOwlModel
	 * @return
	 */
	private DatatypeProperty getNumericalValueProperty(OntModel jenaOwlModel) {
		return jenaOwlModel.getDatatypeProperty(
				"http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#numericalValue");
	}
	
	/** derive property that defines subsystem relationships as described in the ontology
	 * 
	 * @param jenaOwlModel (OntModel)
	 * @return
	 */
	private ObjectProperty getHasSubsystemRelation(OntModel jenaOwlModel) {
		return jenaOwlModel.getObjectProperty(
				"http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#hasSubsystem");
	}
	/** main function. Reads the values in and copies the templates back. 
	 * 
	 */
	@Override
	protected JSONObject processRequestParameters(JSONObject requestParams, HttpServletRequest request) {
		String baseUrl= requestParams.optString("baseUrl", "testFood");
		String wasteIRI=requestParams.optString("wastenetwork", "http://www.theworldavatar.com/kb/sgp/singapore/wastenetwork/SingaporeWasteSystem.owl#SingaporeWasteSystem");
		OntModel model= WastetoEnergyAgent.readModelGreedy(wasteIRI);
		try {
			//read for FC details
			List<String[]> resu =  readAndDump(model,WastetoEnergyAgent.FCQuery);
			//select in year 1
			List<String[]> fcMapping = createFoodCourt(resu);
			//properties of OnsiteTech
			List<String[]> propertydataonsite = readAndDump(model, WastetoEnergyAgent.WTFTechOnsiteQuery);
			List<String[]> inputoffsitedata = readResult(baseUrl,"n_unit_max_offsite.csv");
		
			File f = new File(baseUrl + "/"+"offsiteCluster.csv");
			if(f.exists() && !f.isDirectory()) { 
				List<String[]> onsiteAndFC = updateinFCCluster(baseUrl,inputoffsitedata,fcMapping);
//				List<String> onsiteiricomplete=updateinOnsiteWT(fcMapping,baseUrl,propertydataonsite);
				updateinFCCluster(baseUrl,fcMapping,propertydataonsite);
			}else {
				List<String> onsiteiricomplete=updateinOnsiteWT(fcMapping,baseUrl,propertydataonsite);
				List<String> onsiteiriselected=updateinFC(baseUrl,onsiteiricomplete,inputoffsitedata,fcMapping);
				updateKBForSystem(wasteIRI, baseUrl, WastetoEnergyAgent.wasteSystemOutputQuery,onsiteiriselected); //for waste system	
			}						
			updateinOffsiteWT(inputoffsitedata,baseUrl);
		 }catch (Exception e) {
			e.printStackTrace();
		}			 
		 
		return requestParams;
	}
	/** reads the result from the csv file produced and returns as List<String[]>
	 * 
	 * @param baseUrl String
	 * @param filename name of the file. 
	 * @return
	 * @throws IOException
	 */
	public List<String[]> readResult(String baseUrl,String filename) throws IOException {

        String outputFile = baseUrl + "/"+filename;
        String csv = new QueryBroker().readFileLocal(outputFile);
        List<String[]> simulationResult = MatrixConverter.fromCsvToArray(csv);
		
		return simulationResult;
	}
	/** scans in model and reads out according to Query
	 * 
	 * @param model Ontological model created in processRequestParameters
	 * @return result of Query
	 */
	public List<String[]> readAndDump(OntModel model, String mainquery) {
		List<String[]> inputdata = new ArrayList<String[]>();
		ResultSet resultSet = JenaHelper.query(model, mainquery);
		String result = JenaResultSetFormatter.convertToJSONW3CStandard(resultSet);
        String[] keysfc = JenaResultSetFormatter.getKeys(result);
        List<String[]> resultList = JenaResultSetFormatter.convertToListofStringArrays(result, keysfc);
		return resultList;
	}
	/** helper function for createFC for later conversion
	 * 
	 * @param resultList
	 * @return
	 */
	public List<String[]> createFoodCourt(List<String[]> resultList) {
	 List<String[]> inputdata = new ArrayList<String[]>();
		for (int d = 0; d < resultList.size(); d++) {
			//entity, x, y
			String[] mapper = {resultList.get(d)[5],resultList.get(d)[1], resultList.get(d)[2] };// only extract and y
			if (resultList.get(d)[4].contentEquals("1")) { //self select for year
				inputdata.add(mapper);
			}
		}
		return inputdata;
	}
	/** creates the Onsite Waste Treatment Facility OWL file
	 * 
	 * @param inputdata {[List<String[]>]} list of FC 
	 * @param baseUrl String
	 * @return List<String> list of IRIS of onsite WTF
	 * @throws Exception
	 */
		
	public List<String> updateinOnsiteWT(List<String[]> inputdata,
			String baseUrl,
			List<String[]> propertydata) throws Exception { //creating needed onsite WTF while returning complete set of onsite iri
		
		List<String[]> unitofonsite=readResult(baseUrl,"number of units (onsite).csv");
		List<String[]>onsiteunitmapping=new ArrayList<String[]>();
		int size3=unitofonsite.size();
		int colamount3=unitofonsite.get(0).length;
		for(int x=0;x<size3;x++) { //currently 1 with one tech
			String[]linemapping= new String[colamount3];//109 elements
			for(int y=0;y<colamount3;y++) { 	
				BigDecimal bd = new BigDecimal(unitofonsite.get(x)[y]);
				double newval= Double.parseDouble(bd.toPlainString());
				linemapping[y]=bd.toPlainString();
				if(newval<0) {
					linemapping[y]="0";
				}
				
				
			}
			onsiteunitmapping.add(linemapping);	
		}
		WTEKBCreator converter = new WTEKBCreator();
		//create Onsite WTF
		converter.startConversion("onsitewtf",inputdata,onsiteunitmapping,propertydata);
		List<String>mappedonsiteiri=converter.onsiteiri;
		return mappedonsiteiri;
	}
	/** creates the Onsite Waste Treatment Facility OWL file for cluster
	 * 
	 * @param inputdata {[List<String[]>]} list of FC 
	 * @param baseUrl String
	 * @return List<String> list of IRIS of onsite WTF
	 * @throws Exception
	 */
		
	public List<String> updateinOnsiteWTCluster(List<String[]> inputdata , String baseUrl,List<String[]> propertydata) throws Exception { //creating needed onsite WTF while returning complete set of onsite iri
		List<String[]> unitofonsite=readResult(baseUrl,"offsiteCluster.csv");
		List<String[]>onsiteunitmapping=new ArrayList<String[]>();
		int size3=unitofonsite.size();
		int colamount3=unitofonsite.get(0).length;
		for(int x=0;x<size3;x++) { //currently 1 with one tech
			String[]linemapping= new String[colamount3];//nameOfCluster/nameOfonsiteWTF
			for(int y=0;y<colamount3;y++) { 	
				BigDecimal bd = new BigDecimal(unitofonsite.get(x)[y]);
				double newval= Double.parseDouble(bd.toPlainString());
				linemapping[y]=bd.toPlainString();
				if(newval<0) {
					linemapping[y]="0";
				}
				
				
			}
			onsiteunitmapping.add(linemapping);	
		}
		WTEKBCreator converter = new WTEKBCreator();
		//create Onsite WTF
		converter.startConversion("onsitewtf",inputdata,onsiteunitmapping,propertydata);
		List<String>mappedonsiteiri=converter.onsiteiri;
		return mappedonsiteiri;
	}
	/** updates the Foodcourt owl file based on the value of the treated waste (onsite) CSV and treated waste (offsite) csv
	 * This updates the waste delivered to either facility
	 * @param baseUrl
	 * @param inputdataonsite
	 * @param inputdataoffsite
	 * @param foodcourtmap
	 * @return
	 * @throws Exception
	 */
	public List<String[]> updateinFCCluster(String baseUrl,
			List<String[]> inputdataoffsite,
			List<String[]> foodcourtmap) throws Exception { //update the fc and giving selected onsite iri list
		List<String[]> clusterWTF=new ArrayList<String[]>();
		//both of them have row= fc amount, col represents onsite or offsite per tech
		List<String[]>treatedwasteon=readResult(baseUrl,"Treated waste (onsite).csv");
		//NoOfClusterx1
		List<String[]>treatedwasteoff=readResult(baseUrl,"Treated waste (offsite).csv");
		//NoOfClusterx9
		int colamount2=treatedwasteoff.get(0).length; // 3 wtf and 3 technologies currently
		//determine the number of WTF
		List<String[]> clusterOnsite = readResult(baseUrl,"Waste flow relation (offsite)");
		//noOfFCActualxnoOfFC (repeated values are clusters)
		List<String[]>sitemapping=new ArrayList<String[]>();
		HashSet<String> clusterName =new HashSet<String>(); //temporary value until it runs
		int size=clusterOnsite.size();//size = no of FC Actual
		for(int x=0;x<size;x++) {//NoOfFC
	        // HashSet should be 1. As HashSet contains only distinct values. 
	        HashSet<String> s = new HashSet<>(Arrays.asList(treatedwasteoff.get(x))); //because treatedwaste would be shorter
	        if (s.size() == 1) { //if all are zero there is only one element in a unique set so onsite
	        	int size2 = clusterOnsite.get(x).length;//determine how many FC x noOfYears(15)
	        	for(int y=0;y<size2;y++) {//|noOfFCxnoOfYears|
//					String wastetransfer=treatedwasteon.get(x)[0]; //in ton/day 
					String wastetransfer = treatedwasteon.get(clusterName.size())[0];
	        		//edit for now because we don't know how much FC waste gets fed in
					if (Integer.parseInt(clusterOnsite.get(x)[y]) == 1) {//so onsite is present
						//freak assume that we consider them a cluster even if it only has one element
						if(Double.parseDouble(wastetransfer)>0.01) {//condition if cluster does not reveal anything
							//add year on year later
							//int year = y %15;
							clusterName.add(wastetransfer);
							String[]linemapping= {Integer.toString(x),Integer.toString(y),wastetransfer, "1"};
							sitemapping.add(linemapping);
						}
					}
				} //but FC Cluster can send to multiple WTF and we don't know the mapping!!!
	        }else { 
	        	//presuming that anything that involves offsite WTF requires a cluster? 
	        	//erroneous. Logic Failure 
	        	for(int y=0;y<colamount2;y++) { //3tech*3instance
					String wastetransfer=treatedwasteoff.get(x)[y]; //in ton/day
					if (Integer.parseInt(clusterOnsite.get(x)[y]) == 1) { //aka it's present
						if(Double.parseDouble(wastetransfer)>0.01) { //assuming that the presence of a cluster
							//figure out where the position of the cluster is in Treated offsite WTF
							String[]linemapping= {Integer.toString(x),Integer.toString(y),wastetransfer, "2"};
							sitemapping.add(linemapping);		
						}
					}
				}
	        }
			
		}
		List<String[]> inputdataonsite = readResult(baseUrl,"offsiteCluster.csv");
		//NOTE: Offsite and onsite mapping could be both present!
		//I should have less than 109 cluster Names at this stage and there's a difference
		//between FC Cluster and onsite Cluster
		
		String sparqlStart = "PREFIX OW:<http://www.theworldavatar.com/ontology/ontowaste/OntoWaste.owl#> \r\n" 
		+"PREFIX OCPSYST:<http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#> \r\n"
			+ "INSERT DATA { \r\n";
		
		//outputdata= treated waste onsite
		//input data onsite=onsiteiri
		for (int d = 0; d < foodcourtmap.size(); d++) {// each iri of foodcourt
			int wasteindex = 1;
			int g = Integer.valueOf(sitemapping.get(d)[3]);
			String fcCluster =  sitemapping.get(d)[4];
			//Should go through each FC by number
			StringBuffer b = new StringBuffer();
			String currentwaste = foodcourtmap.get(d)[0].split("#")[0] + "#WasteDeliveredAmount-" + wasteindex;
			String valuecurrentwaste = foodcourtmap.get(d)[0].split("#")[0] + "#V_WasteDeliveredAmount-"
					+ wasteindex;
			Double numfromres = Double.parseDouble(sitemapping.get(d)[2]);
			int onsiteindex = Integer.valueOf(sitemapping.get(d)[1]);//onsite cluster name
			String currentwtf = "http://www.theworldavatar.com/kb/sgp/singapore/wastenetwork/FoodCourtCluster"
					+String.valueOf(onsiteindex) + ".owl#FoodCourtCluster"+String.valueOf(onsiteindex);
			b.append("<" + foodcourtmap.get(d)[0] + "> OW:deliverWaste <" + currentwaste + "> . \r\n");
			b.append("<" + currentwaste + "> a OW:WasteTransfer . \r\n");
			b.append("<" + currentwaste + "> OCPSYST:hasValue <" + valuecurrentwaste + "> . \r\n");
			b.append("<" + valuecurrentwaste + "> a OCPSYST:ScalarValue . \r\n");
			b.append("<" + valuecurrentwaste + "> OCPSYST:numericalValue " + numfromres + " . \r\n");
			b.append("<" + valuecurrentwaste
					+ "> OCPSYST:hasUnitOfMeasure <http://www.theworldavatar.com/ontology/ontocape/supporting_concepts/SI_unit/derived_SI_units.owl#ton_per_day> . \r\n");
			b.append("<" + currentwaste + "> OW:isDeliveredTo <" + currentwtf + "> . \r\n");
			wasteindex++;
			String[] arr = {currentwtf,fcCluster};
			clusterWTF.add(arr);
			String sparql = sparqlStart + b.toString() + "} \r\n";
			new QueryBroker().updateFile(foodcourtmap.get(d)[0], sparql);

		}
		return clusterWTF;
	}
	/** if dump without cluster, sparql update is updated into onsite / offsite WTF directly
	 * 
	 * @param baseUrl
	 * @param inputdataonsite
	 * @param inputdataoffsite
	 * @param foodcourtmap
	 * @return
	 * @throws Exception
	 */
	public List<String> updateinFC(String baseUrl,List<String> inputdataonsite,List<String[]> inputdataoffsite,List<String[]> foodcourtmap) throws Exception { //update the fc and giving selected onsite iri list
		List<String>selectedOnsite=new ArrayList<String>();
		//both of them have row= fc amount, col represents onsite or offsite per tech
		List<String[]>treatedwasteon=readResult(baseUrl,"Treated waste (onsite).csv");
		//noOfFc x noOfOnsiteWTF
		List<String[]>onsitemapping=new ArrayList<String[]>();
		int size=treatedwasteon.size();
		for(int x=0;x<size;x++) {
			for(int y=0;y<size;y++) {
				String wastetransfer=treatedwasteon.get(x)[y]; //in ton/day
				if(Double.parseDouble(wastetransfer)>0.01) {
					String[]linemapping= {""+x,""+y,wastetransfer};
					onsitemapping.add(linemapping);
				}
			}
		}
		
		List<String[]>treatedwasteoff=readResult(baseUrl,"Treated waste (offsite).csv");
		//noOfFc x 3 x 3
		List<String[]>offsitemapping=new ArrayList<String[]>();
		int size2=treatedwasteoff.size();
		int colamount2=treatedwasteoff.get(0).length;
		for(int x=0;x<size2;x++) {
			for(int y=0;y<colamount2;y++) { //3tech*3instance
				String wastetransfer=treatedwasteoff.get(x)[y]; //in ton/day
				if(Double.parseDouble(wastetransfer)>0.01) {
					String[]linemapping= {""+x,""+y,wastetransfer};
					offsitemapping.add(linemapping);
				}
			}
		}
		
	
		
		String sparqlStart = "PREFIX OW:<http://www.theworldavatar.com/ontology/ontowaste/OntoWaste.owl#> \r\n" 
		+"PREFIX OCPSYST:<http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#> \r\n"
			+ "INSERT DATA { \r\n";
		
		//outputdata= treated waste onsite
		//input data onsite=onsiteiri
		for (int d = 0; d < foodcourtmap.size(); d++) {// each iri of foodcourt
			int wasteindex = 1;

			StringBuffer b = new StringBuffer();
			if (onsitemapping.size() > 0) {
				String currentwaste = foodcourtmap.get(d)[0].split("#")[0] + "#WasteDeliveredAmount-" + wasteindex;
				String valuecurrentwaste = foodcourtmap.get(d)[0].split("#")[0] + "#V_WasteDeliveredAmount-"
						+ wasteindex;
				Double numfromres = Double.parseDouble(onsitemapping.get(d)[2]);
				int onsiteindex = Integer.valueOf(onsitemapping.get(d)[1]);
				String currentwtf = inputdataonsite.get(onsiteindex);
				b.append("<" + foodcourtmap.get(d)[0] + "> OW:deliverWaste <" + currentwaste + "> . \r\n");
				b.append("<" + currentwaste + "> a OW:WasteTransfer . \r\n");
				b.append("<" + currentwaste + "> OCPSYST:hasValue <" + valuecurrentwaste + "> . \r\n");
				b.append("<" + valuecurrentwaste + "> a OCPSYST:ScalarValue . \r\n");
				b.append("<" + valuecurrentwaste + "> OCPSYST:numericalValue " + numfromres + " . \r\n");
				b.append("<" + valuecurrentwaste
						+ "> OCPSYST:hasUnitOfMeasure <http://www.theworldavatar.com/ontology/ontocape/supporting_concepts/SI_unit/derived_SI_units.owl#ton_per_day> . \r\n");
				b.append("<" + currentwaste + "> OW:isDeliveredTo <" + currentwtf + "> . \r\n");
				wasteindex++;
				selectedOnsite.add(currentwtf);
			}

			if (offsitemapping.size() > 0) {
				String currentwaste = foodcourtmap.get(d)[0].split("#")[0] + "#WasteDeliveredAmount-" + wasteindex;
				String valuecurrentwaste = foodcourtmap.get(d)[0].split("#")[0] + "#V_WasteDeliveredAmount-"
						+ wasteindex;
				Double numfromres = Double.parseDouble(offsitemapping.get(d)[2]);
				int offsiteindex = Integer.valueOf(offsitemapping.get(d)[1]);
				int IndexOffsiteHeader = offsiteindex % 3; // index 0,3,6 is the first wtf, 1,4,7 is the 2nd, 2,5,8 is
															// the 3rd
				String currentoffwtf = inputdataoffsite.get(0)[IndexOffsiteHeader];
				b.append("<" + foodcourtmap.get(d)[0] + "> OW:deliverWaste <" + currentwaste + "> . \r\n");
				b.append("<" + currentwaste + "> a OW:WasteTransfer . \r\n");
				b.append("<" + currentwaste + "> OCPSYST:hasValue <" + valuecurrentwaste + "> . \r\n");
				b.append("<" + valuecurrentwaste + "> a OCPSYST:ScalarValue . \r\n");
				b.append("<" + valuecurrentwaste + "> OCPSYST:numericalValue " + numfromres + " . \r\n");
				b.append("<" + valuecurrentwaste
						+ "> OCPSYST:hasUnitOfMeasure <http://www.theworldavatar.com/ontology/ontocape/supporting_concepts/SI_unit/derived_SI_units.owl#ton_per_day> . \r\n");
				b.append("<" + currentwaste + "> OW:isDeliveredTo <" + currentoffwtf + "> . \r\n");
						wasteindex++;
			}

			String sparql = sparqlStart + b.toString() + "} \r\n";
			try {
			      FileWriter myWriter = new FileWriter("C:\\JPS_DATA\\workingdir\\JPS_SCENARIO\\scenario\\testFWec7e04f8-831f-43ab-a22d-9b91dc059b7b\\localhost_8080\\data\\78d15fd0-ff0d-4930-bf83-f0e5b93d85ae\\filename.txt");
			      myWriter.write(sparql);
			      myWriter.close();
			      
			    } catch (IOException e) {
			      System.out.println("An error occurred.");
			      e.printStackTrace();
			    }
			new QueryBroker().updateFileOLD(foodcourtmap.get(d)[0], sparql);

		}
		
		return selectedOnsite;
	}
	/** updates the knowledge base of the composite systems. 
	 * 
	 * @param iriofnetwork
	 * @param baseUrl
	 * @param queryupdate wasteSystemOutputQuery costs of composite systems
	 * @param onsiteiri
	 * @throws IOException
	 */
	public void updateKBForSystem(String iriofnetwork, String baseUrl, String queryupdate,List<String> onsiteiri) throws IOException {
		List<String[]>economic=readResult(baseUrl,"Economic output.csv");
		String result = new QueryBroker().queryFile(iriofnetwork,queryupdate);
		String[] keyswt = JenaResultSetFormatter.getKeys(result);
		List<String[]> resultList = JenaResultSetFormatter.convertToListofStringArrays(result, keyswt);
		System.out.println("answer number= " + resultList.size());
		OntModel model = JenaHelper.createModel();
		model.read(iriofnetwork, null);
		for (int ind = 1; ind < keyswt.length; ind++) {
			Individual inst = model.getIndividual(resultList.get(0)[ind]);
			if (ind == 1) {
				inst.setPropertyValue(getNumericalValueProperty(model),
						model.createTypedLiteral(Double.parseDouble(economic.get(ind - 1)[0])));
			} else {
				inst.setPropertyValue(getNumericalValueProperty(model),
						model.createTypedLiteral(Double.parseDouble(economic.get(ind)[0])));
			}
		}
		
		Individual entity = model.getIndividual(resultList.get(0)[0]);
		for(int wtfamount=0;wtfamount<onsiteiri.size();wtfamount++) {
			Resource entityonsite = model.createResource(onsiteiri.get(wtfamount));
			entity.addProperty(getHasSubsystemRelation(model), entityonsite);
		}
		
		
		String content = JenaHelper.writeToString(model);
		new QueryBroker().putOld(resultList.get(0)[0], content);

	}
	
	
	/** updates the OWL file for the Offsite Waste Treatment facilities. 
	 * 
	 * @param inputdata List<String[]>
	 * @param baseUrl String
	 * @throws Exception
	 */
	public void updateinOffsiteWT(List<String[]> inputdata,String baseUrl) throws Exception {
		//assume inputdata= input offsite data
		List<String[]>unitofoffsite=readResult(baseUrl,"number of units (offsite).csv");
		System.out.println("it goes to the offsite update");
		//filter the arrayfirst to take only non zero values
		List<String[]>filtered=new ArrayList<String[]>();
		for(int r=0;r<unitofoffsite.size();r++) {
			for(int i=0;i<unitofoffsite.get(0).length;i++) {
				String element=unitofoffsite.get(r)[i];
				if(Double.parseDouble(element)>0.01) {
					String[]component= {""+r,inputdata.get(0)[i],element};
					filtered.add(component);
				}
			}
		}
		
		if(filtered.size()>0) {
			String sparqlStart = "PREFIX OW:<http://www.theworldavatar.com/ontology/ontowaste/OntoWaste.owl#> \r\n" 
					+"PREFIX OCPSYST:<http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#> \r\n"
						+ "INSERT DATA { \r\n";
			for(int w=0;w<filtered.size();w++) {
				StringBuffer b = new StringBuffer();
				String currentunit = filtered.get(w)[1].split("#")[0] + "#UnitDeviceOf-" + filtered.get(w)[1].split("#")[1]+w; //w is precaution if duplicate instance
				int numunit = Integer.valueOf(filtered.get(w)[2]);
				//String currentwtf = inputdataonsite.get(onsiteindex);
				//0=incineration
				//1=codigestion
				//2=anaerobic
				String result = new QueryBroker().queryFile(filtered.get(w)[1], Offsiteoutput);
				String[] keyswt = JenaResultSetFormatter.getKeys(result);
				List<String[]> resultList = JenaResultSetFormatter.convertToListofStringArrays(result, keyswt);
				String techiri=resultList.get(Integer.valueOf(filtered.get(w)[0]))[1];
				b.append("<" + techiri + "> OW:realizedByDevice <" + currentunit + "> . \r\n");
				b.append("<" + currentunit + "> a OW:WasteTreatmentDevice . \r\n");
				b.append("<" + currentunit + "> OW:usedInYear " + 1 + " . \r\n");
				b.append("<" + currentunit + "> OW:amountOfUnit " + numunit + " . \r\n");
				String sparql = sparqlStart + b.toString() + "} \r\n";
				new QueryBroker().updateFile(filtered.get(w)[1], sparql);
			}
		}
	}
	
}
