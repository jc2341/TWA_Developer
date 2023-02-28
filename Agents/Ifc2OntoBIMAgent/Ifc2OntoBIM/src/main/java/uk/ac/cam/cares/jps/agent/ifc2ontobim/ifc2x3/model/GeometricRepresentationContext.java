package uk.ac.cam.cares.jps.agent.ifc2ontobim.ifc2x3.model;

import org.apache.jena.rdf.model.Statement;
import uk.ac.cam.cares.jps.agent.ifc2ontobim.jenautils.OntoBimConstant;
import uk.ac.cam.cares.jps.agent.ifc2ontobim.jenautils.StatementHandler;
import uk.ac.cam.cares.jps.agent.ifc2ontobim.ttlparser.StringUtils;

import java.util.LinkedHashSet;
import java.util.UUID;

public class GeometricRepresentationContext {
    private Double precision = null;
    private String northDirIri = null;
    private final String iri;
    private final String wcsIri;
    private final Double dimension;

    /**
     * Standard Constructor initialising the necessary and optional inputs.
     *
     * @param iri                   The instance IRI of IfcGeometricRepresentationContext in IfcOwl.
     * @param spaceDimension        A field for the integer dimension count of the coordinate space.
     * @param precision             An optional field for the value of the geometric models' precision.
     * @param worldCoordinateSysIri The IRI for establishing the IFC project's engineering coordinate system.
     * @param trueNorthDirectionIRI An optional field for indicating the IRI of the True North direction vector.
     */
    public GeometricRepresentationContext(String iri, String spaceDimension, String precision, String worldCoordinateSysIri, String trueNorthDirectionIRI) {
        String prefix = iri.contains(OntoBimConstant.HASH) ? StringUtils.getStringBeforeLastCharacterOccurrence(iri, OntoBimConstant.HASH) + OntoBimConstant.HASH :
                StringUtils.getStringBeforeLastCharacterOccurrence(iri, OntoBimConstant.BACKSLASH) + OntoBimConstant.BACKSLASH;
        // Generate new geometric representation context IRI
        this.iri = prefix + OntoBimConstant.GEOM_CONTEXT_CLASS + OntoBimConstant.UNDERSCORE + UUID.randomUUID();
        this.dimension = Double.valueOf(spaceDimension);
        this.wcsIri = worldCoordinateSysIri;
        // Parse the optional values
        if (precision != null) {
            this.precision = Double.valueOf(precision);
        }
        if (trueNorthDirectionIRI != null) {
            this.northDirIri = trueNorthDirectionIRI;
        }
    }

    public String getIri() {return this.iri;}

    /**
     * Generate and add the statements required for this Class to the statement set input.
     *
     * @param statementSet The set containing the new ontoBIM triples.
     */
    public void constructStatements(LinkedHashSet<Statement> statementSet) {
        StatementHandler.addStatement(statementSet, this.getIri(), OntoBimConstant.RDF_TYPE, OntoBimConstant.BIM_GEOM_CONTEXT_CLASS);
        StatementHandler.addStatementWithDoubleLiteral(statementSet, this.getIri(), OntoBimConstant.BIM_HAS_SPACE_DIMENSION, this.dimension);
        StatementHandler.addStatement(statementSet, this.getIri(), OntoBimConstant.BIM_HAS_WCS, this.wcsIri);
        StatementHandler.addStatement(statementSet, this.wcsIri, OntoBimConstant.RDF_TYPE, OntoBimConstant.BIM_LOCAL_PLACEMENT_CLASS);
        if (this.precision != null) {
            StatementHandler.addStatementWithDoubleLiteral(statementSet, this.getIri(), OntoBimConstant.BIM_HAS_PRECISION, this.precision);
        }
        if (this.northDirIri != null) {
            StatementHandler.addStatement(statementSet, this.getIri(), OntoBimConstant.BIM_HAS_TRUE_NORTH, this.northDirIri);
            StatementHandler.addStatement(statementSet, this.northDirIri, OntoBimConstant.RDF_TYPE, OntoBimConstant.BIM_DIR_VEC_CLASS);
        }
    }
}
