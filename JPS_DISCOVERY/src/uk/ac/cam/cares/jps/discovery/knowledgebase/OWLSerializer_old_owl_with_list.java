package uk.ac.cam.cares.jps.discovery.knowledgebase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import uk.ac.cam.cares.jps.base.config.AgentLocator;
import uk.ac.cam.cares.jps.discovery.api.AbstractAgentServiceDescription;
import uk.ac.cam.cares.jps.discovery.api.Agent;
import uk.ac.cam.cares.jps.discovery.api.AgentServiceDescription;
import uk.ac.cam.cares.jps.discovery.api.Parameter;
import uk.ac.cam.cares.jps.discovery.util.Helper;
import uk.ac.cam.cares.jps.discovery.util.ISerializer;

// TODO-AE remove this class if not needed any more
public class OWLSerializer_old_owl_with_list implements ISerializer {
	
	private static OWLSerializer_old_owl_with_list instance = null;
	private static final String ONTOAGENT = "http://www.theworldavatar.com/OntoAgent";
	
	Logger logger = LoggerFactory.getLogger(OWLSerializer_old_owl_with_list.class);
	private OntModel ontology = null;
	private String ontBaseIRI = null;
	private OntModel knowledgeBase = null;
	private String kbBaseIRI = null;
	private int counter = 0;
	
	private OWLSerializer_old_owl_with_list() {

		// TODO-AE migrate to new version of JENA !!!
		// TODO-AE URGENT import statement missing in JENA report, but required when reading agentxxxx.owl in Protege
		// TODO-AE introduce special IRI for Properties/Keys such as domain and also for its possible values
		// TODO-AE URGENT commit the agent ontology OWL file --> discuss directory convention, maybe own project only for ontology OWL files?
	}
	
	public static synchronized OWLSerializer_old_owl_with_list getInstance() {
		if (instance == null) {
			instance = new OWLSerializer_old_owl_with_list();
			instance.init();
		}
		return instance;
	}
	
	private void init( ) {
		// this class is a singleton. It is enough to read the ontology only once
		ontology = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
		String path = AgentLocator.getPathToJpsDataOntologyDir() + "/OntoAgent/OntoAgent.owl";
		File file = new File(path);
		ontology.read(file.toURI().toString());
		ontBaseIRI = ONTOAGENT + "/OntoAgent.owl#";
	}
	
	@Override
	public synchronized String convertToString(Serializable object) {
		// OWLSerializer is a singleton. Because it has java attributes such as knowledgeBase 
		// all public convert methods have to be synchronized for parallel access
		
		UUID uuid = Helper.createUUID();	
		ByteArrayOutputStream stream = convertToString(object, uuid);
		String s = stream.toString();
		try {
			stream.close();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return s;
	}
	
	private ByteArrayOutputStream convertToString(Serializable object, UUID uuid) {
		
		counter = 0;
		
		// Create a new model (knowledgeBase) for the instances that will be generated from the input parameter "object" 
		// This model is created in addition to the model (ontology) for the classes
		// If both instances and classes are stored in the same model, the class definition would be duplicated 
		// when writing the instances to RDF/XML
		knowledgeBase = ModelFactory.createOntologyModel();
		knowledgeBase.setNsPrefix("jpsago", ontBaseIRI);
		
		if (object instanceof Agent) {	
			kbBaseIRI = ONTOAGENT + "/Agent" + uuid + ".owl#";
			knowledgeBase.setNsPrefix("jpsagkb", kbBaseIRI);
			
			//TODO-AE if then for AgentDesription, Request, Response ...
			createAgent((Agent) object, uuid);
		} else if (object instanceof AgentServiceDescription) {
			//TODO-AE Class in OntoAgent.owl is AgentServiceDescription --> rename java Code
			//TODO-AE AgentMessage --> AgentServiceRequest, AgentServiceResponse
			kbBaseIRI = ONTOAGENT + "/AgentMessage#";
			knowledgeBase.setNsPrefix("jpsagkb", kbBaseIRI);
			//TODO-AE check that there is a least one output parameter (and domain, name...)
			createAgentDescription((AbstractAgentServiceDescription) object);
		} else {
			throw new RuntimeException("can't serialize the object of type = " + object.getClass().getName());
		}
		
		//knowledgeBase.write(System.out);
		
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		knowledgeBase.write(stream, "RDF/XML");
		
		return stream;
	}

	@Override
	public synchronized <T extends Serializable> Optional<T> convertFrom(String objectAsString) {
		
		knowledgeBase = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
		InputStream is = new ByteArrayInputStream( objectAsString.getBytes(StandardCharsets.UTF_8) );
		knowledgeBase.read(is, null);
		
		return null;
	}
	
	private void createAgent(Agent agent, UUID uuid) {
		// the way how individuals are created here follows:
		// https://stackoverflow.com/questions/43719469/create-individuals-using-jena
		
		OntClass agentClass = ontology.getOntClass(ontBaseIRI + "Agent");
		// TODO-AE why do we need .owl in the IRI
		Individual agentInd = knowledgeBase.createIndividual(ONTOAGENT + "/Agent" + uuid + ".owl#Agent", agentClass);
		
		createTripleWithDatatypeProperty(agentInd, "hasName", agent.getName().getValue());
		
		for (AgentServiceDescription current : agent.getDescriptions()) {
			Individual descrInd = createAgentDescription(current);
			createTripleWithObjectProperty(agentInd, "hasAgentServiceDescription", descrInd);
		}
	}
	
	private Individual createAgentDescription(AbstractAgentServiceDescription description) {
		
		if (description.getProperties().isEmpty()) {
			// TODO-AE create new JPS Runtime Exception in JPS_BASE
			throw new RuntimeException("empty property list of agent description");
		}
		
		Individual result = createIndividual("AgentServiceDescription");
		
		// create individuals for all parameters
		List<Parameter> params = description.getProperties();
		//TODO-AE use iri domain here
		List<Individual> allParameters = createIndividualList(params, "Property");
		params = description.getInputParameters();
		allParameters.addAll(createIndividualList(params, "InputParameter"));
		params = description.getOutputParameters();
		allParameters.addAll(createIndividualList(params, "OutputParameter"));
		
		// create OWL List from all Parameters
		Individual listInd = createTripleWithObjectProperty(result, "hasKeyValueList", "List");
		createTripleWithObjectProperty(listInd, "hasFirstListElement", allParameters.get(0));
		for (int i=0; i<allParameters.size()-1 ; i++) {
			createTripleWithObjectProperty(allParameters.get(i), "followedBy", allParameters.get(i+1));
		}
		
		return result;
	}
	
	private Individual createIndividual(String classForIndividual) {
		OntClass cl = ontology.getOntClass(ontBaseIRI + classForIndividual);
		return knowledgeBase.createIndividual(kbBaseIRI + classForIndividual + inc(), cl);
	}
	
	private List<Individual> createIndividualList(List<Parameter> params, String classForIndividualsinList) {
		List<Individual> result = new ArrayList<Individual>();
		
		OntClass cl = ontology.getOntClass(ontBaseIRI + classForIndividualsinList);
		for (Parameter current : params) {	
			Individual ind = knowledgeBase.createIndividual(kbBaseIRI + classForIndividualsinList + inc(), cl);
			createTripleWithDatatypeProperty(ind, "hasKey", current.getKey().getValue());
			if ((current.getValue() != null) && (!"null".equals(current.getValue().getValue()))) {
				createTripleWithDatatypeProperty(ind, "hasValue", current.getValue().getValue());
			}
			result.add(ind);
		}
		
		return result;
	}
	
	private int inc() {
		counter += 1;
		return counter;
	}
	
	private Individual createTripleWithObjectProperty(Individual subject, String predicate, String object) {
		OntClass cl = ontology.getOntClass(ontBaseIRI + object);
		Individual ind = knowledgeBase.createIndividual(kbBaseIRI + object + inc(), cl);
		ObjectProperty prop = ontology.getObjectProperty(ontBaseIRI + predicate);
		subject.addProperty(prop, ind);
		return ind;
	}
	
	private void createTripleWithObjectProperty(Individual subject, String predicate, Individual object) {
		ObjectProperty prop = ontology.getObjectProperty(ontBaseIRI + predicate);
		subject.addProperty(prop, object);
	}
	
	private void createTripleWithDatatypeProperty(Individual subject, String predicate, String datatype) {
		DatatypeProperty prop = ontology.getDatatypeProperty(ontBaseIRI + predicate);
		subject.addProperty(prop, datatype);
	}
	
	public void writeAsOwlFile(Agent agent) throws IOException {
		
		UUID uuid = Helper.createUUID();
		ByteArrayOutputStream bytestream = convertToString(agent, uuid);
		
		String path = AgentLocator.getPathToJpsDataKnowledgeDir() + "/OntoAgent/Agent" + uuid + ".owl";
		File file = new File(path);
		if (!file.exists()) {
			file.createNewFile();
		}
		
		FileOutputStream filestream = new FileOutputStream(file);
		bytestream.writeTo(filestream);
		bytestream.close();
		filestream.close();
	}
}
