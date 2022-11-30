################################################
# Authors: Markus Hofmeister (mh807@cam.ac.uk) #    
# Date: 30 Nov 2022                            #
################################################

# The purpose of this module is to instantiate/update the potential impact of a flood 
# by estimating the affected population and buildings by a particular flood alert/warning
# instantiated in the KG (using asynchronous derivation framework)

import uuid
import pandas as pd
from rdflib import Graph

from pyderivationagent import DerivationAgent
from pyderivationagent import DerivationInputs
from pyderivationagent import DerivationOutputs

from floodassessment.datamodel.iris import *
from floodassessment.kg_operations.kgclient import KGClient


class FloodAssessmentAgent(DerivationAgent):

    def __init__(self, **kwargs):
        # Initialise DerivationAgent parent instance
        super().__init__(**kwargs)

        # Initialise the Sparql_client (with defaults specified in environment variables)
        self.sparql_client = self.get_sparql_client(KGClient)
        

    def agent_input_concepts(self) -> list:
        # Please note: Declared inputs/outputs need proper instantiation incl. 
        #              RDF TYPE declarations in the KG for the derivation to work
        return [FLOOD_ALERT_WARNING, OBE_BUILDING, OM_AMOUNT_MONEY]


    def agent_output_concepts(self) -> list:
        # Output concept (i.e. result) of the Derivation
        return [FLOOD_POPULATION, FLOOD_BUILDINGS, FLOOD_IMPACT]


    def validate_inputs(self, http_request) -> bool:
        # Validate completeness of received HTTP request (i.e. non-empty HTTP request, 
        # contains derivationIRI, etc.) -> only relevant for synchronous derivation
        return super().validate_inputs(http_request)


    def validate_input_values(self, inputs, derivationIRI=None):
        """
        Check whether received input values are suitable to perform flood impact assessment.
        Throw exception if data is not suitable -> relevant for asynchronous derivation

        Arguments:
            inputs {dict} -- Dictionary of inputs with input concepts as keys and values as list
            derivationIRI {str} -- IRI of the derivation instance (optional)

        Returns:
            flood_alert_warning {str}, 
            building_iris {list}, value_estimation_iris {list}
        """

        # Verify that exactly one flood alert/warning instance is provided
        inp = inputs.get(FLOOD_ALERT_WARNING)
        if len(inp) == 1:
            flood_alert_warning = inp[0]
        else:
            self.logger.error(f"Derivation {derivationIRI}: More than one 'FloodAlertOrWarning' IRI provided.")
            raise Exception(f"Derivation {derivationIRI}: More than one 'FloodAlertOrWarning' IRI provided.")

        # Extract lists of building IRIs and value estimation IRIs
        # (i.e. in principle both lists could be empty without causing issues)
        building_iris = inputs.get(OBE_BUILDING)
        value_estimation_iris = inputs.get(OM_AMOUNT_MONEY)

        if len(value_estimation_iris) > len(building_iris):
            self.logger.error(f"Derivation {derivationIRI}: More value estimations than buildings provided.")
            raise Exception(f"Derivation {derivationIRI}: More value estimations than buildings provided.")

        return flood_alert_warning, building_iris, value_estimation_iris

    
    def process_request_parameters(self, derivation_inputs: DerivationInputs, 
                                   derivation_outputs: DerivationOutputs):
        """
            This method takes 
                1 IRI of RT:FloodALertOrWarning
                and
                1 List of OBE:Building IRIs (could be empty)
                1 List of OM:AmountMoney IRIs (could be empty)
            to assess the estimated impact of a potential flood and generate
                1 IRI of Flood:Impact
                1 IRI of Flood:Population
                1 IRI of Flood:Buildings
                (including the respective instantiation of OM:AmountOfMoney + full
                 set of triples due to ontology of units of measure representation)
        """

        # Get input IRIs from the agent inputs (derivation_inputs)
        # (returns dict of inputs with input concepts as keys and values as list)
        inputs = derivation_inputs.getInputs()
        derivIRI = derivation_inputs.getDerivationIRI()
        tx_iri, ppi_iri, avgsqm_iri, area_iri = self.validate_input_values(inputs=inputs,
                                                    derivationIRI=derivIRI)
        
        # Assess property value estimate in case all required inputs are available
        # (i.e. relevant inputs have been marked up successfully)
        g = self.estimate_property_market_value(transaction_iri=tx_iri,
                                                prop_price_index_iri=ppi_iri, 
                                                avgsqm_price_iri=avgsqm_iri, 
                                                floor_area_iri=area_iri)        

        # Collect the generated triples derivation_outputs
        derivation_outputs.addGraph(g)


    def estimate_number_of_affected_people(flood_alert_warning_iri: str) -> int:
        """
            Estimate the number of "affected" people by a flood alert/warning using 
            PostGIS' geospatial count over population density raster data within the
            boundary of the ArealExtendPolygon associated with the flood alert/warning
        """
        #TODO: Implement this method using Ontop in the Stack, i.e. query 
        #      Blazegraph using SERVICE keyword
        
        return None


    def estimate_property_market_value(self, transaction_iri:str = None,
                                       prop_price_index_iri:str = None, 
                                       avgsqm_price_iri:str = None, 
                                       floor_area_iri:str = None):
        """
        Estimate market value of property (i.e. building or flat) based on given inputs.
        Prio1: LRPPI:TransactionRecord & OntoBuiltEnv:PropertyPriceIndex
        Prio2: OntoBuiltEnv:AveragePricePerSqm & OM:Area

        Arguments:
            transaction_iri {str} - IRI of LRPPI:TransactionRecord
            prop_price_index_iri {str} - IRI of OntoBuiltEnv:PropertyPriceIndex
            avgsqm_price_iri {str} - IRI of OntoBuiltEnv:AveragePricePerSqm
            floor_area_iri {str} - IRI of OM:Area
        Returns:
            Graph to instantiate/update property market value
        """

        # Initialise market value and return triples
        market_value = None
        g = Graph()

        # # Prio 1: Check if transaction record and property price index are provided
        # # (i.e. market value assessment based on previous transaction)
        # if transaction_iri and prop_price_index_iri:
        #     # Initialise TS client
        #     ts_client = TSClient(kg_client=self.sparql_client)
        #     # 1) Retrieve representative UK House Price Index and parse as Series (i.e. unwrap Java data types)
        #     # UKHPI was set at a base of 100 in January 2015, and reflects the change in value of residential property since then
        #     # (https://landregistry.data.gov.uk/app/ukhpi/doc)
        #     try:
        #         # Retrieve time series in try-with-resources block to ensure closure of RDB connection
        #         with ts_client.connect() as conn:
        #             ts = ts_client.tsclient.getTimeSeries([prop_price_index_iri], conn)
        #         dates = [d.toString() for d in ts.getTimes()]
        #         values = [v for v in ts.getValues(prop_price_index_iri)]
        #     except Exception as ex:
        #         self.logger.error('Error retrieving/unwrapping Property Price Index time series')
        #         raise TSException('Error retrieving/unwrapping Property Price Index time series') from ex

        #     # Create UKHPI series with conditioned date index
        #     ukhpi = pd.Series(index=dates, data=values)
        #     ukhpi = ukhpi.astype(float)
        #     ukhpi.index = pd.to_datetime(ukhpi.index, format=TIME_FORMAT_LONG)
        #     ukhpi.sort_index(ascending=False, inplace=True)
        #     ukhpi.index = ukhpi.index.strftime(TIME_FORMAT_SHORT)

        #     # 2) Retrieve previous sales transaction details for previous transaction IRI
        #     #    and adjust to current market value
        #     res = self.sparql_client.get_transaction_details(transaction_iri)
        #     ukhpi_now = ukhpi.iloc[0]
        #     ukhpi_old = ukhpi[res['date']]
        #     market_value = res['price'] * ukhpi_now / ukhpi_old

        # # Prio 2: Otherwise assess market value based on FloorArea and AveragePricePerSqm
        # elif avgsqm_price_iri and floor_area_iri and not market_value:
        #     # NOTE: To ensure availability of AvgSqmPrice (i.e. derivation being computed by
        #     #       AvgSqmPrice Agent), AvgSqmPrice should be marked up as Synchronous Derivation
        #     res = self.sparql_client.get_floor_area_and_avg_price(floor_area_iri)
        #     market_value = res['floor_area'] * res['avg_price']

        # if market_value:
        #     # Round property market value to full kGBP
        #     market_value = round(market_value/1000)*1000
        #     # Create instantiation/update triples
        #     market_value_iri = KB + 'AmountOfMoney_' + str(uuid.uuid4())
        #     # Create rdflib graph with update triples 
        #     g = self.sparql_client.instantiate_property_value(graph=g,
        #                                         property_iri=res['property_iri'],
        #                                         property_value_iri=market_value_iri, 
        #                                         property_value=market_value)
        # # Return graph with SPARQL update (empty for unavailable market value)
        return g


def default():
    """
        Instructional message at the app root.
    """
    # TODO: Update path to main upon merging
    msg  = "This is an asynchronous agent to estimate the market value of a particular property (i.e. building, flat).<BR>"
    msg += "<BR>"
    msg += "For more information, please visit https://github.com/cambridge-cares/TheWorldAvatar/tree/main/Agents/PropertyValueEstimationAgent<BR>"
    return msg
