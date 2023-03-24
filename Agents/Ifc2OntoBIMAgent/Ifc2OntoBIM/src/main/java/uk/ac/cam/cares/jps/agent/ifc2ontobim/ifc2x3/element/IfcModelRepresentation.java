package uk.ac.cam.cares.jps.agent.ifc2ontobim.ifc2x3.element;

import org.apache.jena.rdf.model.Statement;
import uk.ac.cam.cares.jps.agent.ifc2ontobim.ifcparser.OntoBimConstant;
import uk.ac.cam.cares.jps.agent.ifc2ontobim.utils.StatementHandler;
import uk.ac.cam.cares.jps.agent.ifc2ontobim.utils.StringUtils;

import java.util.LinkedHashSet;
import java.util.UUID;

/**
 * A class representing the IfcModelRepresentation concept in OntoBIM.
 *
 * @author qhouyee
 */
public class IfcModelRepresentation {
    private final String ifcRepIri;
    private final String prefix;
    private final String name;
    private final String uid;
    private final String placementIri;

    /**
     * Standard Constructor initialising the common inputs.
     *
     * @param iri          The element IRI from IfcOwl.
     * @param name         The name of this IFC object.
     * @param uid          The IFC uid generated for this object.
     * @param placementIri The local placement IRI for the zone's position.
     */
    public IfcModelRepresentation(String iri, String name, String uid, String placementIri) {
        this.prefix = iri.contains(OntoBimConstant.HASH) ? StringUtils.getStringBeforeLastCharacterOccurrence(iri, OntoBimConstant.HASH) + OntoBimConstant.HASH :
                StringUtils.getStringBeforeLastCharacterOccurrence(iri, OntoBimConstant.BACKSLASH) + OntoBimConstant.BACKSLASH;
        this.ifcRepIri = this.prefix + OntoBimConstant.ASSET_MODEL_REP_CLASS + OntoBimConstant.UNDERSCORE + UUID.randomUUID();
        this.name = name;
        this.uid = uid;
        String instVal = StringUtils.getStringAfterLastCharacterOccurrence(placementIri, StringUtils.UNDERSCORE);
        this.placementIri = prefix + OntoBimConstant.LOCAL_PLACEMENT_CLASS + OntoBimConstant.UNDERSCORE + instVal;
    }

    public String getIfcRepIri() { return this.ifcRepIri;}
    public String getPrefix() { return this.prefix;}
    public String getName() { return this.name;}
    public String getUid() { return this.uid;}
    public String getPlacementIri() { return this.placementIri;}

    /**
     * An abstract method that must be overridden and used in each subclass
     * to generate and add statements to the existing set.
     *
     * @param statementSet The set containing the new ontoBIM triples.
     */
    public void constructStatements(LinkedHashSet<Statement> statementSet) {
    }

    /**
     * Generate IfcModelRepresentation statements required.
     *
     * @param statementSet The set containing the new ontoBIM triples.
     */
    public void addIfcModelRepresentationStatements(LinkedHashSet<Statement> statementSet) {
        StatementHandler.addStatement(statementSet, this.getIfcRepIri(), OntoBimConstant.RDF_TYPE, OntoBimConstant.BIM_ASSET_MODEL_REP_CLASS);
        StatementHandler.addStatement(statementSet, this.getIfcRepIri(), OntoBimConstant.RDFS_LABEL, this.getName(), false);
        StatementHandler.addStatement(statementSet, this.getIfcRepIri(), OntoBimConstant.BIM_HAS_ID, this.getUid(), false);
        StatementHandler.addStatement(statementSet, this.getIfcRepIri(), OntoBimConstant.BIM_HAS_LOCAL_POSITION, this.getPlacementIri());
        StatementHandler.addStatement(statementSet, this.getPlacementIri(), OntoBimConstant.RDF_TYPE, OntoBimConstant.BIM_LOCAL_PLACEMENT_CLASS);
    }
}
