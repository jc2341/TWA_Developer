from flask import Flask, jsonify, request, json

from .error_handling.exceptions import KGException, TSException
from .kg_utils.tsClientForUpdate import TSClientForUpdate
from .solar_model import SolarModel
from PVLibAgent.data_retrieval.query_data import QueryData
from PVLibAgent.kg_utils.utils import IRI, DATACLASS, TIME_FORMAT
from PVLibAgent.data_retrieval.query_timeseries import query_latest_timeseries
from PVLibAgent.data_instantiation.create_data_iris import check_data_iris
from PVLibAgent.data_instantiation.timeseries_data import timeseries_data
import agentlogging
from pathlib import Path
import os

# Create the Flask app object
app = Flask(__name__)

# Initialise logger
logger = agentlogging.get_logger("dev")

# Define location of properties file (with Triple Store and RDB settings)
PROPERTIES_FILE = os.path.abspath(os.path.join(Path(__file__).parent, "resources", "dataIRIs.properties"))

# Show an instructional message at the app root
@app.route('/')
def default():
    msg  = "To see the result of an API call, enter a URL of the form:<BR>"
    msg += "&nbsp&nbsp [this_url]/api/v1/evaluate?val=[VAL]&order=[ORDER]<BR><BR>"
    msg += "&nbsp&nbsp (where [VAL] is a float and [ORDER] is an integer between 0 and 2)"
    msg += "&nbsp&nbsp [this_url] is the host and port currently shown in the address bar"
    return msg

# Define a route for API requests
@app.route('/api/v1/evaluate', methods=['GET'])
def api():
    
    # Check arguments (query parameters)
    logger.info("Checking arguments...")

    if 'device' in request.args:
        try:
            device = str(request.args['device'])
        except ValueError:
            logger.error("Unable to parse device type.")
            return "Unable to interpret device type ('%s') as a string." % request.args['device']
    else:
        return "Error: No 'device' parameter provided."

    if device.__contains__('weatherStation'):
        try:
            iri = IRI
            try:
                latitude_value = QueryData.query_latitude(iri)
            except Exception as ex:
                logger.error("SPARQL query for latitude not successful")
                raise KGException("SPARQL query for latitude not successful.") from ex

            try:
                longitude_value = QueryData.query_longitude(iri)
            except Exception as ex:
                logger.error("SPARQL query for longitude not successful")
                raise KGException("SPARQL query for longitude not successful.") from ex

            # Construct and evaluate the model
            model = SolarModel('ModelChain', latitude_value, longitude_value)

            try:
                air_temperature_iri = QueryData.query_air_temperature(iri)
            except Exception as ex:
                logger.error("SPARQL query for air temperature IRI not successful")
                raise KGException("SPARQL query for air temperature IRI not successful.") from ex


            try:
                wind_speed_iri = QueryData.query_wind_speed(iri)
            except Exception as ex:
                logger.error("SPARQL query for wind speed IRI not successful")
                raise KGException("SPARQL query for wind speed IRI not successful.") from ex

            try:
                ghi_iri = QueryData.query_global_horizontal_irradiance(iri)
            except Exception as ex:
                logger.error("SPARQL query for global horizontal irradiance IRI not successful")
                raise KGException("SPARQL query for global horizontal irradiance IRI not successful.") from ex

            print(wind_speed_iri)
            print(air_temperature_iri)
            print(ghi_iri)
            print(latitude_value)
            print(longitude_value)
            wind_speed = query_latest_timeseries(wind_speed_iri)

            print(str(wind_speed))

            json_object = model.calculate(latitude_value, longitude_value)
        # Return the result in JSON format
            return json_object
        except ValueError as ex:
            return str(ex)

    elif str(request.args['device']).__contains__('sensor'):
        try:
            iri = IRI
            try:
                latitude_value = QueryData.query_latitude(iri)
            except Exception as ex:
                logger.error("SPARQL query for latitude not successful")
                raise KGException("SPARQL query for latitude not successful.") from ex

            try:
                longitude_value = QueryData.query_longitude(iri)
            except Exception as ex:
                logger.error("SPARQL query for longitude not successful")
                raise KGException("SPARQL query for longitude not successful.") from ex

            # Construct and evaluate the model
            model = SolarModel('ModelChain', latitude_value, longitude_value)

            try:
                ghi_iri = QueryData.query_irradiance(iri)
            except Exception as ex:
                logger.error("SPARQL query for irradiance IRI not successful")
                raise KGException("SPARQL query for irradiance IRI not successful.") from ex

            print(ghi_iri)
            print(latitude_value)
            print(longitude_value)

            iri_list = check_data_iris.check_data_iris_and_create_if_not_exist(PROPERTIES_FILE)
            results = model.calculate(latitude_value, longitude_value)
            timestamp = results["timestamp"]
            # timestamp must have the format 2017-04-01T04:00:00Z
            timestamp_list = ['2017-04-01T04:00:00Z']
            ac_power_value = results["AC Power(W)"]
            dc_power_value = results["DC Power(W)"]
            ac_power_list = [ac_power_value]
            dc_power_list = [dc_power_value]
            value_list = [ac_power_list, dc_power_list]
            print(str(timestamp))
            print(ac_power_value)
            print(dc_power_value)

            try:
                boolean_value = timeseries_data.check_data_has_timeseries(iri_list)
                if not boolean_value:
                    timeseries_data.init_timeseries(iri_list, [DATACLASS, DATACLASS], TIME_FORMAT)
            except Exception as ex:
                logger.error("Unable to initialise timeseries")
                raise TSException("Unable to initialise timeseries") from ex
            timeseries_object = TSClientForUpdate.create_timeseries(timestamp_list, iri_list, value_list)
            timeseries_data.add_timeseries_data(timeseries_object)
            return results
        except ValueError as ex:
            return str(ex)

