##########################################
# Author: Feroz Farazi (msff2@cam.ac.uk) #
# Date:  27 Jan 2022                      #
##########################################

# Get settings and functions from the kg utils module
import kg_utils
from datetime import datetime as dt
import json
# Get the JVM module view (via jpsBaseLibGateWay instance) from the jpsSingletons module to access
# the TimeSeriesClient in the JPB_BASE_LIB
from jpsSingletons import jpsBaseLibView

# Specify plotting properties for GeoJSON features
geojson_attributes = { 'displayName': '',
                  'description': '',
                  'circle-color': '#FF0000',
                  'circle-stroke-width': 1,
                  'circle-stroke-color': '#000000',
                  'circle-stroke-opacity': 0.75,
                  'circle-opacity': 0.75
                  }

def get_all_time_series(terminal, KGClient, TSClient, now, duration, start_1, start_2, start_7):
    '''
        Returns all time series data of the terminals
    '''

    # Define query
    query = kg_utils.create_sparql_prefix('om') + \
            kg_utils.create_sparql_prefix('rdfs') + \
            kg_utils.create_sparql_prefix('comp') + \
            '''SELECT ?terminal ?dataIRI ?unit \
               WHERE { ?terminal comp:hasTaken ?gas_amount ; \
                                 rdfs:label ?name . \
                       ?gas_quantity om:hasPhenomenon ?gas_amount . \
                       ?gas_quantity om:hasValue ?dataIRI . \
                       ?dataIRI om:hasUnit ?unit }'''
                                
    # Execute query
    response = KGClient.execute(query)

    # Convert JSONArray String back to list
    response = json.loads(response)

    # Initialise lists
    dataIRIs = []
    utilities = []
    units = []
    measurement_iri = None
    # Append lists with all query results
    for r in response:
        if r['terminal'].lower() == terminal.lower():
            dataIRIs.append(r['dataIRI'])
            utilities.append("Instantaneous Flow")
            units.append((r['unit']))
            measurement_iri = r['dataIRI']
            break
    # Initialise timestamps for gas flow time series retrieval durations (time series entries stored as UTC times!)
    # Java Instant instances are associated with UTC (i.e. hold a value of date-time with a UTC time-line)
    print("INFO: Submitting TimeSeriesClient SPARQL queries at",
          dt.utcfromtimestamp(now.getEpochSecond()).strftime("%Y-%m-%dT%H:%M:%SZ"))
    # Get results for last "duration" hours (e.g. 24h)
    timeseries = TSClient.getTimeSeriesWithinBounds([measurement_iri], start_1, now)
    if not timeseries.getTimes():
        # Try last "2 x duration" hours if nothing available for last "duration" hours (e.g. 48h)
        print("WARNING: No results in last %i h, trying last %i h ..." % (duration, 2 * duration))
        timeseries = TSClient.getTimeSeriesWithinBounds([measurement_iri], start_2, now)
        if not timeseries.getTimes():
           # Last resort, try last "7 x duration" hours (e.g. last week)
           print("WARNING: No results in last %i h, trying last %i h ..." % (2 * duration, 7 * duration))
           timeseries = TSClient.getTimeSeriesWithinBounds([measurement_iri], start_7, now)

    # Retrieve time series data for retrieved set of dataIRIs
    timeseries = TSClient.getTimeSeries(dataIRIs)

    # Return time series and associated lists of variables and units
    return timeseries, utilities, units


def put_metadata_in_json(feature_id, lon, lat):
    """
       Structures metadata in JSON format
    """
    metadata = { 'id': feature_id,
                 'Longitude': lon,
                 'Latitude': lat
                 }
    return metadata


def get_metadata(terminal_iri, KGClient):
    '''
        Returns meta data for the requested terminal identified by its IRI.
    '''
    # Defines query to retrieve latitude and longitude of terminals
    query = kg_utils.create_sparql_prefix('loc') + \
            kg_utils.create_sparql_prefix('rdf') + \
            kg_utils.create_sparql_prefix('comp') + \
            '''SELECT ?iri ?loc \
               WHERE { ?iri rdf:type comp:GasTerminal ;
                             loc:lat-lon ?loc .}'''
    # Executes query
    response = KGClient.execute(query)
    # Converts JSONArray String back to list
    response = json.loads(response)
    lon = None
    lat = None
    for r in response:
        if r['iri'].lower() == terminal_iri.lower():
            coordinates = r['loc'].split('#')
            lon = coordinates[1]
            lat = coordinates[0]
    return lon, lat


def format_in_geojson(feature_id, properties, coordinates):
    """
       It structures geodata of terminals into geoJSON format.
    """
    feature = {'type': 'Feature',
               'id': int(feature_id),
               'properties': properties.copy(),
               'geometry': {'type': 'Point',
                            'coordinates': coordinates
                            }
               }
    return feature

def get_all_terminal_geodata(KGClient):
    '''
        Returns coordinates ([lon, lat]) and name (label) for all terminals
    '''

# SPARQL query string
    query = kg_utils.create_sparql_prefix('rdf') + \
            kg_utils.create_sparql_prefix('rdfs') + \
            kg_utils.create_sparql_prefix('loc') + \
            kg_utils.create_sparql_prefix('comp') + \
            '''SELECT ?location ?name
                WHERE
                {
                    ?term rdf:type comp:GasTerminal ;
                        rdfs:label ?name ;
                        loc:lat-lon ?location.
                }'''
    # Execute query
    response = KGClient.execute(query)

    # Convert JSONArray String back to list
    response = json.loads(response)
    terminal_coorindates = dict()
    for r in response:
        coordinates = r['location'].split('#')
        coordinates = [float(i) for i in coordinates]
        coordinates = coordinates[::-1]
        terminal_coorindates[r['name'].lower()] = coordinates
    return terminal_coorindates

def get_all_terminals(KGClient):
    '''
        Returns all terminals instantiated in KG as list
    '''
    # Initialise SPARQL query variables for gas terminal IRIs and names
    var1, var2 = 'iri', 'name'

    # Define query
    query = kg_utils.create_sparql_prefix('comp') + \
            kg_utils.create_sparql_prefix('rdf') + \
            kg_utils.create_sparql_prefix('rdfs') + \
            'SELECT distinct ?' + var1 + ' ?' + var2 + ' ' \
            'WHERE { ?' + var1 + ' rdf:type comp:GasTerminal; \
                                   rdfs:label ?' + var2 + '. }'
    # Execute query
    response = KGClient.execute(query)
    # Convert JSONArray String back to list
    response = json.loads(response)
    # A key-value paired list where key is the name and
    # value is the IRI of terminal
    terminals = dict()
    for r in response:
        terminals[r[var2]] = r[var1]
    return terminals

def geojson_initialise_dict():
    # Start GeoJSON FeatureCollection
    geojson = {'type': 'FeatureCollection',
               'features': []
               }
    return geojson

def generate_all_visualisation_data():
    """
       Generates all data for Gas Grid visualisation.
    """
    # Set Mapbox API key in DTVF 'index.html' file
    kg_utils.set_mapbox_apikey()

    # Initialise remote KG client with only query endpoint specified
    KGClient = jpsBaseLibView.RemoteStoreClient(kg_utils.QUERY_ENDPOINT)

    # Retrieve Java's Instant class to initialise TimeSeriesClient
    Instant = jpsBaseLibView.java.time.Instant
    instant_class = Instant.now().getClass()
    # Initialise TimeSeriesClass
    TSClient = jpsBaseLibView.TimeSeriesClient(instant_class, kg_utils.PROPERTIES_FILE)
    # Initialise timestamps for gas flow time series retrieval durations (time series entries stored as UTC times!)
    # Java Instant instances are associated with UTC (i.e. hold a value of date-time with a UTC time-line)
    now = Instant.now()
    duration = 24
    print("INFO: Submitting TimeSeriesClient SPARQL queries at",
          dt.utcfromtimestamp(now.getEpochSecond()).strftime("%Y-%m-%dT%H:%M:%SZ"))
    start_1 = now.minusSeconds(int(1 * duration * 60 * 60))
    start_2 = now.minusSeconds(int(2 * duration * 60 * 60))
    start_7 = now.minusSeconds(int(7 * duration * 60 * 60))

    # Initialise a dictionary for geoJSON outputs
    geojson = geojson_initialise_dict()
    # Initialise an array for metadata outputs
    metadata = []
    # Initialise an array with four elements for timeseries outputs
    ts_data = { 'ts': [],
                'id': [],
                'units': [],
                'headers': []
                }
    feature_id = 0

    # Get all terminals of interest
    terminals = get_all_terminals(KGClient)
    # Retrieve all geocoordinates of terminals for GeoJSON output
    terminal_coordinates = get_all_terminal_geodata(KGClient)

    # Iterate over all terminals
    for terminal, iri in terminals.items():
        feature_id += 1
        # Update GeoJSON properties
        geojson_attributes['description'] = str(terminal)
        geojson_attributes['displayName'] = terminal
        # Append results to overall GeoJSON FeatureCollection
        if terminal.lower() in terminal_coordinates:
            print('terminal_coordinates[terminal.lower()]:', terminal_coordinates[terminal.lower()])
            geojson['features'].append(format_in_geojson(feature_id, geojson_attributes, terminal_coordinates[terminal.lower()]))
        # Retrieve terminal metadata
        lon, lat = get_metadata(iri, KGClient)
        if lon == None and lat == None:
            print('The following terminal is not represented in the knowledge graph with coordinates:', iri)
        else:
            metadata.append(put_metadata_in_json(feature_id, lon, lat))
        # Retrieve time series data
        timeseries, utilities, units = get_all_time_series(iri, KGClient, TSClient, now, duration, start_1, start_2, start_7)
        ts_data['ts'].append(timeseries)
        ts_data['id'].append(feature_id)
        ts_data['units'].append(units)
        ts_data['headers'].append(utilities)

    # Retrieve all time series data for collected 'ts_data' from Java TimeSeriesClient at once
    ts_json = TSClient.convertToJSON(ts_data['ts'], ts_data['id'], ts_data['units'], ts_data['headers'])
    # Make JSON file readable in Python
    ts_json = json.loads(ts_json.toString())


if __name__ == '__main__':
    """
    If this module is executed, it will generate all data required for the
    Digital Twin Visualisation Framework.
    """
    generate_all_visualisation_data()