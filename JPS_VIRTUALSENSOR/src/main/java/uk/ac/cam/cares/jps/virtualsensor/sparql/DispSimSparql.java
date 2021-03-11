package uk.ac.cam.cares.jps.virtualsensor.sparql;

import org.eclipse.rdf4j.sparqlbuilder.rdf.Iri;

import uk.ac.cam.cares.jps.virtualsensor.objects.DispSim;

import static org.eclipse.rdf4j.sparqlbuilder.rdf.Rdf.iri;

import org.eclipse.rdf4j.sparqlbuilder.core.From;
import org.eclipse.rdf4j.sparqlbuilder.core.Prefix;
import org.eclipse.rdf4j.sparqlbuilder.core.SparqlBuilder;
import org.eclipse.rdf4j.sparqlbuilder.core.query.ModifyQuery;
import org.eclipse.rdf4j.sparqlbuilder.core.query.Queries;
import org.eclipse.rdf4j.sparqlbuilder.graphpattern.TriplePattern;

public class DispSimSparql {
    private static Prefix p_dispsim = SparqlBuilder.prefix("dispsim",iri("http://www.theworldavatar.com/kb/ontodispersionsim/OntoDispersionSim.owl#"));
	private static Prefix p_citygml = SparqlBuilder.prefix("city",iri("http://www.theworldavatar.com/ontology/ontocitygml/OntoCityGML.owl#"));
	private static Prefix p_space_time_extended = SparqlBuilder.prefix("space_time_extended",iri("http://www.theworldavatar.com/ontology/ontocape/supporting_concepts/space_and_time/space_and_time_extended.owl#"));
	private static Prefix p_system = SparqlBuilder.prefix("system",iri("http://www.theworldavatar.com/ontology/ontocape/upper_level/system.owl#"));
	private static Prefix p_coordsys = SparqlBuilder.prefix("coordsys",iri("http://www.theworldavatar.com/ontology/ontocape/upper_level/coordinate_system.owl#"));
	private static Prefix p_space_time = SparqlBuilder.prefix("space_time",iri("http://www.theworldavatar.com/ontology/ontocape/supporting_concepts/space_and_time/space_and_time.owl#"));
	
	private static Prefix[] prefixes = {p_dispsim,p_citygml,p_space_time_extended,p_system,p_coordsys,p_space_time};
	
    // rdf type
    private static Iri dispersionSim = p_dispsim.iri("dispersionSim");
    private static Iri EnvelopeType = p_citygml.iri("EnvelopeType");
    private static Iri PointType = p_citygml.iri("PointType");
    private static Iri CoordinateValue = p_coordsys.iri("CoordinateValue");
    private static Iri StraightCoordinate = p_space_time.iri("StraightCoordinate");
    private static Iri ProjectedCoordinateSystem = p_space_time_extended.iri("ProjectedCoordinateSystem");
    
    //relations
    private static Iri hasNx = p_dispsim.iri("hasNx");
    private static Iri hasNy = p_dispsim.iri("hasNy");
    private static Iri hasEnvelope = p_citygml.iri("hasEnvelope");
    private static Iri srsname = p_citygml.iri("srsname");
    private static Iri lowerCornerPoint = p_citygml.iri("lowerCornerPoint");
    private static Iri upperCornerPoint = p_citygml.iri("upperCornerPoint");
    private static Iri hasGISCoordinateSystem = p_space_time_extended.iri("hasGISCoordinateSystem");
    private static Iri hasProjectedCoordinate_x = p_space_time_extended.iri("hasProjectedCoordinate_x");
    private static Iri hasProjectedCoordinate_y = p_space_time_extended.iri("hasProjectedCoordinate_y");
    private static Iri hasValue = p_system.iri("hasValue");
    private static Iri numericalValue = p_system.iri("numericalValue");
    
    //endpoint
    private static Iri sim_graph = p_dispsim.iri("Simulations");
    private static From FromGraph = SparqlBuilder.from(sim_graph);
    /**
	 * Initialise a simulation on triple-store
	 */
	public static void InitSim(int sim_index, DispSim sim) {
		String sim_id = "sim" + sim_index;
    	Iri sim_iri = p_dispsim.iri(sim_id);
    	Iri nx = p_dispsim.iri(sim_id+"Nx");
    	Iri nxValue = p_dispsim.iri(sim_id+"NxValue");
    	Iri nyValue = p_dispsim.iri(sim_id+"NyValue");
    	Iri ny = p_dispsim.iri(sim_id+"Ny");
    	Iri envelope = p_dispsim.iri(sim_id+"Envelope");
    	Iri lowerCorner = p_dispsim.iri(sim_id+"LowerCorner");
    	Iri lowerCornerCoordinates = p_dispsim.iri(sim_id+"LowerCornerCoordinates");
    	Iri upperCorner = p_dispsim.iri(sim_id+"UpperCorner");
    	Iri upperCornerCoordinates = p_dispsim.iri(sim_id+"UpperCornerCoordinates");
    	
    	Iri lowerCornerX = p_dispsim.iri(sim_id+"LowerCornerX");
    	Iri lowerCornerXValue = p_dispsim.iri(sim_id+"LowerCornerXValue");
    	Iri lowerCornerY = p_dispsim.iri(sim_id+"LowerCornerY");
    	Iri lowerCornerYValue = p_dispsim.iri(sim_id+"LowerCornerYValue");
    	
    	Iri upperCornerX = p_dispsim.iri(sim_id+"UpperCornerX");
    	Iri upperCornerXValue = p_dispsim.iri(sim_id+"UpperCornerXValue");
    	Iri upperCornerY = p_dispsim.iri(sim_id+"UpperCornerY");
    	Iri upperCornerYValue = p_dispsim.iri(sim_id+"UpperCornerYValue");
    	
    	TriplePattern sim_tp = sim_iri.isA(dispersionSim).andHas(hasNx,nx).andHas(hasNy,ny).andHas(hasEnvelope,envelope);
    	
    	TriplePattern envelope_tp = envelope.isA(EnvelopeType).andHas(lowerCornerPoint,lowerCorner).andHas(upperCornerPoint,upperCorner).andHas(srsname,sim.getScope().getCRSName());
    	
    	// lower corner
    	TriplePattern lowercorner_tp = lowerCorner.isA(PointType).andHas(hasGISCoordinateSystem,lowerCornerCoordinates);
    	TriplePattern lowercoord_tp = lowerCornerCoordinates.isA(ProjectedCoordinateSystem).andHas(hasProjectedCoordinate_x,lowerCornerX).andHas(hasProjectedCoordinate_y,lowerCornerY);
    	TriplePattern lowerxcoord_tp = lowerCornerX.isA(StraightCoordinate).andHas(hasValue,lowerCornerXValue);
    	TriplePattern lowerycoord_tp = lowerCornerY.isA(StraightCoordinate).andHas(hasValue,lowerCornerYValue);
    	TriplePattern vlowerxcoord_tp = lowerCornerXValue.isA(CoordinateValue).andHas(numericalValue, sim.getScope().getLowerx());
    	TriplePattern vlowerycoord_tp = lowerCornerYValue.isA(CoordinateValue).andHas(numericalValue, sim.getScope().getLowery());
    	
    	// repeat for upper corner
    	TriplePattern uppercorner_tp = upperCorner.isA(PointType).andHas(hasGISCoordinateSystem,upperCornerCoordinates);
    	TriplePattern uppercoord_tp = upperCornerCoordinates.isA(ProjectedCoordinateSystem).andHas(hasProjectedCoordinate_x,upperCornerX).andHas(hasProjectedCoordinate_y,upperCornerY);
    	TriplePattern upperxcoord_tp =  upperCornerX.isA(StraightCoordinate).andHas(hasValue,upperCornerXValue);
    	TriplePattern upperycoord_tp = upperCornerY.isA(StraightCoordinate).andHas(hasValue,upperCornerYValue);
    	TriplePattern vupperxcoord_tp =  upperCornerXValue.isA(CoordinateValue).andHas(numericalValue, sim.getScope().getUpperx());
    	TriplePattern vupperycoord_tp = upperCornerYValue.isA(CoordinateValue).andHas(numericalValue, sim.getScope().getUppery());
    	
    	// envelope done
    	// model grid information
    	TriplePattern nx_tp = nx.has(hasValue,nxValue);
    	TriplePattern nxval_tp = nxValue.has(numericalValue,sim.getNx());
    	TriplePattern ny_tp = ny.has(hasValue,nyValue);
    	TriplePattern nyval_tp = nyValue.has(numericalValue,sim.getNy());
    	
    	TriplePattern[] combined_tp = {sim_tp,envelope_tp,lowercorner_tp,lowercoord_tp,lowerxcoord_tp,lowerycoord_tp,vlowerxcoord_tp,vlowerycoord_tp,
    			uppercorner_tp,uppercoord_tp,upperxcoord_tp,upperycoord_tp,vupperxcoord_tp,vupperycoord_tp,nx_tp,nxval_tp,ny_tp,nyval_tp};
    	
    	ModifyQuery modify = Queries.MODIFY();
    	modify.prefix(prefixes).with(sim_graph).where().insert(combined_tp);
    	SparqlGeneral.performUpdate(modify);
    }
}
