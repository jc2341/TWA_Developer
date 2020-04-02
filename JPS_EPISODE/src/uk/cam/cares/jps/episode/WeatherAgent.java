package uk.cam.cares.jps.episode;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.rdf4j.RDF4JException;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.http.HTTPRepository;
import org.eclipse.rdf4j.rio.RDFFormat;

public class WeatherAgent {

	static String rdf4jServer = "http://localhost:8080/rdf4j-server"; //this is only the local repo, changed if it's inside claudius
	static String repositoryID = "weatherstation";
	static Repository repo = new HTTPRepository(rdf4jServer, repositoryID);
	static String fileprefix="C:/Users/KADIT01/TOMCAT/webapps/ROOT/kb/sgp/singapore/";
	static String iriprefix="http://www.theworldavatar.com/kb/sgp/singapore/";
	public void addFiletoRepo(RepositoryConnection con,String filename,String contextiri) {
		File file =new File(fileprefix+filename);
		String baseURI=iriprefix+filename;
		try {
			
			try {
//				con.add(file, baseURI, RDFFormat.RDFXML);
				//BELOW IS FOR ADDING THE NAMED GRAPH/CONTEXT :
				ValueFactory f=repo.getValueFactory();
				IRI context= f.createIRI(contextiri);
				con.add(file, baseURI, RDFFormat.RDFXML,context);
				System.out.println("success");
			}
			finally {
				con.close();				
			}
			
		}
		catch(RDF4JException e) {
			System.out.println("fail 1");
		}
		catch (java.io.IOException e) {
			System.out.println("fail 2");
		}
	}
	public void addinstancetoRepo(RepositoryConnection con) {
		ValueFactory f=repo.getValueFactory();
		IRI stn1=f.createIRI("http://www.theworldavatar.com/kb/sgp/singapore/WeatherStation-001.owl#WeatherStation-001");
		IRI composite=f.createIRI("http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#CompositeSystem");
		IRI hassubsystem=f.createIRI("http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#hasSubsystem");
		IRI sensor1=f.createIRI("http://www.theworldavatar.com/kb/sgp/singapore/SGCloudCoverSensor-001.owl#SGCloudCoverSensor-001");

		con.add(stn1, RDF.TYPE,composite);
		con.add(stn1, hassubsystem,sensor1);
	}

	public void deleteValuetoRepo(RepositoryConnection con) {
		ValueFactory f=repo.getValueFactory();
		IRI stn1=f.createIRI("http://www.theworldavatar.com/kb/sgp/singapore/WeatherStation-001.owl#WeatherStation-001");
		con.remove(stn1,null,null); //remove all triples realted to stn1
	}
	
	public void queryValuefromRepo(RepositoryConnection con, String context) { //should we use top node concept or the name graph to categorize some triples??
		String sensorinfo = "PREFIX j2:<http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#> "
				+ "PREFIX j4:<http://www.theworldavatar.com/ontology/ontosensor/OntoSensor.owl#> "
				+ "PREFIX j5:<http://www.theworldavatar.com/ontology/ontocape/chemical_process_system/CPS_realization/process_control_equipment/measuring_instrument.owl#> "
				+ "PREFIX j6:<http://www.w3.org/2006/time#> " 
				+ "SELECT ?entity ?propval ?proptime ?proptimeval "
//				+ "WHERE " //it's replaced when named graph is used
				+ "{graph "+"<"+context+">"
				+ "{ ?entity a j5:T-Sensor ." 
				+ "  ?entity j4:observes ?prop ." 
				+ " ?prop   j2:hasValue ?vprop ."
				+ " ?vprop   j2:numericalValue ?propval ." 
				+ " ?vprop   j6:hasTime ?proptime ."
				+ " ?proptime   j6:inXSDDateTimeStamp ?proptimeval ." 
				+ "}" 
				+ "}" 
				+ "ORDER BY ASC(?proptimeval)";
		
		TupleQuery query = con.prepareTupleQuery(QueryLanguage.SPARQL, sensorinfo);
		TupleQueryResult result = query.evaluate();
		int d=0;
		try {
			while (result.hasNext()) {
				BindingSet bindingSet = result.next();
				String time = bindingSet.getValue("proptimeval").stringValue();
				String inst = bindingSet.getValue("proptime").stringValue();
				String value = bindingSet.getValue("propval").stringValue();

				// String time="random";
				System.out.println("measured property value= " + value);
				System.out.println("instance= "+inst);
				System.out.println(" at the time= " + time);
				// logger.info("species-uri: " + speciesUri);
				d++;
			}
			System.out.println("total data=" + d);
		} catch (Exception e) {

			System.out.println(e.getMessage());
		}
//		con.commit();
//		con.close();
	}
	
	public void updateRepoRoutine(RepositoryConnection con, String context,String propnameclass, String newpropvalue, String newtimestamp) {
		
		String sensorinfo = "PREFIX j2:<http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#> "
				+ "PREFIX j4:<http://www.theworldavatar.com/ontology/ontosensor/OntoSensor.owl#> "
				+ "PREFIX j5:<http://www.theworldavatar.com/ontology/ontocape/chemical_process_system/CPS_realization/process_control_equipment/measuring_instrument.owl#> "
				+ "PREFIX j6:<http://www.w3.org/2006/time#> " 
				+ "SELECT ?vprop ?propval ?proptime ?proptimeval "
//				+ "WHERE " //it's replaced when named graph is used
				+ "{graph "+"<"+context+">"
				+ "{ "
				//+ " ?entity a j5:T-Sensor ." 
				+ "  ?entity j4:observes ?prop ."
				+ " ?prop a j4:"+propnameclass+" ."
				+ " ?prop   j2:hasValue ?vprop ."
				+ " ?vprop   j2:numericalValue ?propval ." 
				+ " ?vprop   j6:hasTime ?proptime ."
				+ " ?proptime   j6:inXSDDateTimeStamp ?proptimeval ." 
				+ "}" 
				+ "}" 
				+ "ORDER BY ASC(?proptimeval)";
		
//		String sensorinfo = "PREFIX j2:<http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#> "
//				+ "PREFIX j4:<http://www.theworldavatar.com/ontology/ontosensor/OntoSensor.owl#> "
//				+ "PREFIX j5:<http://www.theworldavatar.com/ontology/ontocape/chemical_process_system/CPS_realization/process_control_equipment/measuring_instrument.owl#> "
//				+ "PREFIX j6:<http://www.w3.org/2006/time#> " 
//				+ "SELECT ?vprop ?propval ?proptime ?proptimeval "
////				+ "WHERE " //it's replaced when named graph is used
//				+ "WITH <"+context+">"
//				+ "DELETE { ?vprop   j2:numericalValue ?propval .} "
//				+ "INSERT {  ?valueemission j2:numericalValue " + outputvalue + " .} "
//				+ "{ "
//				//+ " ?entity a j5:T-Sensor ." 
//				+ "  ?entity j4:observes ?prop ."
//				+ " ?prop a j4:"+propnameclass+" ."
//				+ " ?prop   j2:hasValue ?vprop ."
//				+ " ?vprop   j2:numericalValue ?propval ." 
//				+ " ?vprop   j6:hasTime ?proptime ."
//				+ " ?proptime   j6:inXSDDateTimeStamp ?proptimeval ." 
//				+ "}" 
//				+ "}" 
//				+ "ORDER BY ASC(?proptimeval)";
//		
//		String plantupdate = "PREFIX cp:<http://www.theworldavatar.com/ontology/ontoeip/powerplants/PowerPlant.owl#> "
//				+ "PREFIX j2:<http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#> "
//				+ "PREFIX j3:<http://www.theworldavatar.com/ontology/ontocape/upper_level/technical_system.owl#> "
//				+ "PREFIX j4:<http://www.theworldavatar.com/ontology/ontoeip/system_aspects/system_realization.owl#> "
//				+ "PREFIX j5:<http://www.theworldavatar.com/ontology/ontoeip/system_aspects/system_performance.owl#> "
//				+ "WITH <" + iri + ">" 
//				+ "DELETE { ?valueemission j2:numericalValue ?vemission .} "
//				+ "INSERT { ?valueemission j2:numericalValue " + outputvalue + " .} "
//				+ "WHERE { ?generation   j5:hasEmission ?emission ." + "?emission   j2:hasValue ?valueemission . "
//				+ "?valueemission   j2:numericalValue ?vemission ." + "}";
//		
//	    Update updateQuery = con.prepareUpdate(QueryLanguage.SPARQL, sensorinfo);
//	    updateQuery.execute();
		
		TupleQuery query = con.prepareTupleQuery(QueryLanguage.SPARQL, sensorinfo);
		TupleQueryResult result = query.evaluate();
		int d=0;
		List<String[]> keyvaluemapold= new ArrayList<String[]>();
		List<String[]> valuemapold= new ArrayList<String[]>();
		try {
			while (result.hasNext()) {
				BindingSet bindingSet = result.next();
				String timevalue = bindingSet.getValue("proptimeval").stringValue();
				String timeinstance = bindingSet.getValue("proptime").stringValue();
				String propvalue = bindingSet.getValue("propval").stringValue();
				String propinstance = bindingSet.getValue("vprop").stringValue();
				String[]keyelement= {propinstance,propvalue,timeinstance,timevalue};
				keyvaluemapold.add(keyelement);
				d++;
			}
			System.out.println("total data=" + d);
			ValueFactory f=repo.getValueFactory();
			IRI numval=f.createIRI("http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#numericalValue");
			IRI timeval=f.createIRI("http://www.w3.org/2006/time#inXSDDateTimeStamp");
			for(int x=0; x<d;x++) {
				IRI prop1=f.createIRI(keyvaluemapold.get(x)[0]);
				Literal lit1=f.createLiteral(keyvaluemapold.get(x)[1]);
				con.remove(prop1,numval,null); //remove all triples realted to propval
				System.out.println(prop1+ " is removed");
				IRI prop2=f.createIRI(keyvaluemapold.get(x)[2]);
				Literal lit2=f.createLiteral(keyvaluemapold.get(x)[3]);
//				con.remove(prop2,timeval,lit2); //remove all triples realted to timeval	
				String[] comp= {keyvaluemapold.get(x)[1],keyvaluemapold.get(x)[3]};
				valuemapold.add(comp);
			}
			valuemapold.remove(0);
			String []newcontent= {newpropvalue,newtimestamp};
			valuemapold.add(newcontent);
			ValueFactory g=repo.getValueFactory();
			for(int x=0; x<d;x++) {
				IRI prop1=g.createIRI(keyvaluemapold.get(x)[0]);
				IRI prop2=g.createIRI(keyvaluemapold.get(x)[2]);
				con.add(prop1,numval,g.createLiteral(valuemapold.get(x)[0]));
//				con.add(prop2,timeval,f.createLiteral(valuemapold.get(x)[1]));
				
			}
					
			
		} catch (Exception e) {

			System.out.println(e.getMessage());
		}
		
		
	}
	
	public static void main(String[]args) {
		String context="http://www.theworldavatar.com/kb/sgp/singapore/WeatherStation-001.owl#WeatherStation-001";
		String context2="http://www.theworldavatar.com/kb/sgp/singapore/WeatherStation-002.owl#WeatherStation-002";
		RepositoryConnection con = repo.getConnection();
		String[] filenames= {"SGCloudCoverSensor-001.owl","SGTemperatureSensor-001.owl","SGWindSpeedSensor-001.owl","SGSolarIrradiationSensor-001.owl","SGPrecipitationSensor-001.owl","SGPressureSensor-001.owl","SGRelativeHumiditySensor-001.owl","SGWindDirectionSensor-001.owl"};
		String[] filenames2= {"SGWindSpeedSensor-002.owl","SGWindDirectionSensor-002.owl"};
		for(String el:filenames) {
			new WeatherAgent().addFiletoRepo(con,el,context);
			
		}
//		new WeatherAgent().queryValuefromRepo(con,context);
//		new WeatherAgent().updateRepoRoutine(con,context,"OutsideAirTemperature","32","2020-04-02T11:53+08:00");
//		new WeatherAgent().addinstancetoRepo(con);
		//new WeatherAgent().deleteValuetoRepo(con);
		
	}
	
}
