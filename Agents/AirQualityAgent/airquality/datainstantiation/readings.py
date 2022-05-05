###############################################
# Authors: Markus Hofmeister (mh807@cam.ac.uk) #    
# Date: 05 Apr 2022                            #
################################################

# The purpose of this module is to provide functions to retrieve 
# readings data from the API and instantiate it in the KG

import uuid
import datetime as dt
import time
from math import nan

#import agentlogging
from airquality.dataretrieval.readings import *
from airquality.dataretrieval.stations import *
from airquality.datainstantiation.stations import *
from airquality.kgutils.querytemplates import *
from airquality.kgutils.kgclient import KGClient
from airquality.kgutils.timeseries import TSClient
from airquality.errorhandling.exceptions import APIException
from airquality.utils.properties import QUERY_ENDPOINT, UPDATE_ENDPOINT
from airquality.utils.readings_mapping import READINGS_MAPPING, UNITS_MAPPING, COMPASS, \
                                             TIME_FORMAT, DATACLASS, VISIBILITY

# Initialise logger
#logger = agentlogging.get_logger("prod")


# def add_readings_timeseries(instantiated_ts_iris: list = None,
#                             api_key: str = DATAPOINT_API_KEY,
#                             query_endpoint: str = QUERY_ENDPOINT,
#                             update_endpoint: str = UPDATE_ENDPOINT) -> int:
#     """
#         Adds time series data to instantiated time series IRIs
        
#         Arguments:
#             instantiated_ts_iris - list of IRIs of instantiated time series
#     """

#     def _create_ts_subsets_to_add(times, dataIRIs, values):
#         # TimeSeriesClient faces issues to add data for all dataIRIs at once,
#         # if some dataIRIs contain missing values (None, nan); hence, create
#         # subsets of data to add without any missing entries
#         df = pd.DataFrame(index=times, data=dict(zip(dataIRIs, values)))
#         df[df.columns[~df.isnull().any()]]
#         non_nulls = df[df.columns[~df.isnull().any()]]
#         # Initialise return lists with data for non null quantities
#         times = [] if non_nulls.empty else [non_nulls.index.to_list()]
#         dataIRIs = [] if non_nulls.empty else [non_nulls.columns.to_list()]
#         values = [] if non_nulls.empty else [[non_nulls[c].values.tolist() for c in non_nulls.columns]]
#         some_nulls = [c for c in list(df.columns) if c not in list(non_nulls.columns)]
#         for c in some_nulls:
#             sub = df[c]
#             sub.dropna(inplace=True)
#             dataIRIs.append([c])
#             times.append(sub.index.to_list())
#             values.append([sub.values.tolist()])
        
#         return times, dataIRIs, values  


#     # Create MetOffice client to retrieve readings via API
#     try:
#         metclient = metoffer.MetOffer(api_key)
#     except:
#         #logger.error("MetOffer client could not be created to retrieve station readings.")
#         raise APIException("MetOffer client could not be created to retrieve station readings.")        
    
#     # Load available observations and forecasts from API
#     print('Retrieving time series data from API ...')
#     #logger.info('Retrieving time series data from API ...')
#     available_obs, available_fcs, issue_time = retrieve_readings_data_per_station(metclient, only_keys=False)
#     print('Time series data successfully retrieved.')
#     #logger.info('Time series data successfully retrieved.')
    
#     # Retrieve information about instantiated time series from KG
#     print('Retrieving time series triples from KG ...')
#     #logger.info('Retrieving time series triples from KG ...')
#     instantiated_obs = get_instantiated_observation_timeseries(query_endpoint=query_endpoint,
#                                                                update_endpoint=update_endpoint)
#     instantiated_fcs = get_instantiated_forecast_timeseries(query_endpoint=query_endpoint,
#                                                             update_endpoint=update_endpoint)
#     print('Time series triples successfully retrieved.')
#     #logger.info('Time series triples successfully retrieved.')

#     # Keep only the relevant subset for instantiated_ts_iris
#     if instantiated_ts_iris:
#         instantiated_obs = instantiated_obs[instantiated_obs['tsIRI'].isin(instantiated_ts_iris)]
#         instantiated_fcs = instantiated_fcs[instantiated_fcs['tsIRI'].isin(instantiated_ts_iris)]
#     # Get short version of variable type from full quantity type
#     instantiated_obs['reading'] = instantiated_obs['quantityType'].apply(lambda x: x.split('#')[-1])
#     instantiated_fcs['reading'] = instantiated_fcs['quantityType'].apply(lambda x: x.split('#')[-1])   

#     # Initialise update query for creation time
#     query_string = update_forecast_creation_datetime(issue_time)

#     # Initialise TimeSeriesClient
#     ts_client = TSClient.tsclient_with_default_settings()

#     added_obs = 0
#     added_fcs = 0
    
#     # Loop through all observation timeseries
#     print('Adding observation time series data ...')
#     #logger.info('Adding observation time series data ...')
#     ts_list = []
#     for tsiri in list(instantiated_obs['tsIRI'].unique()): 
#         # Extract relevant data      
#         data = instantiated_obs[instantiated_obs['tsIRI'] == tsiri]
#         station_id = data['stationID'].iloc[0]
#         # Construct time series object to be added (skip if previously
#         # instantiated time series not present in latest retrieved data)
#         if station_id in available_obs.keys():
#             times = available_obs[station_id]['times']
#             dataIRIs = data['dataIRI'].to_list()
#             # Get instantiated, but potentially missing quantity for reported interval
#             # and fill missing values with nans
#             missing_data = [i for i in data['reading'].to_list() if i not in available_obs[station_id]['readings'].keys()]
#             missing = dict(zip(missing_data, [[nan]*len(times)]*len(missing_data)))
#             readings = {**available_obs[station_id]['readings'], **missing}
#             values = [readings[i] for i in data['reading'].to_list()]
#             # Potentially split time series data addition if None/nan exist in some readings
#             times_list, dataIRIs_list, values_list = _create_ts_subsets_to_add(times, dataIRIs, values)
#             for i in range(len(times_list)):
#                 added_obs += len(dataIRIs_list[i])
#                 ts = TSClient.create_timeseries(times_list[i], dataIRIs_list[i], values_list[i])
#                 ts_list.append(ts)
#     ts_client.bulkaddTimeSeriesData(ts_list)
#     print(f'Time series data for {added_obs} observations successfully added to KG.')
#     #logger.info(f'Time series data for {added_obs} observations successfully added to KG.')
    
#     # Loop through all forecast timeseries
#     print('Adding forecast time series data ...')
#     #logger.info('Adding forecast time series data ...')
#     ts_list = []
#     for tsiri in list(instantiated_fcs['tsIRI'].unique()):  
#         # Extract relevant data      
#         data = instantiated_fcs[instantiated_fcs['tsIRI'] == tsiri]
#         station_id = data['stationID'].iloc[0]
#         # Construct time series object to be added (skip if previously
#         # instantiated time series not present in latest retrieved data)
#         if station_id in available_fcs.keys():
#             times = available_fcs[station_id]['times']
#             dataIRIs = data['dataIRI'].to_list()
#             # Get instantiated, but potentially missing quantity for reported interval
#             # and fill missing values with nans
#             missing_data = [i for i in data['reading'].to_list() if i not in available_fcs[station_id]['readings'].keys()]
#             missing = dict(zip(missing_data, [[nan]*len(times)]*len(missing_data)))
#             readings = {**available_fcs[station_id]['readings'], **missing}
#             values = [readings[i] for i in data['reading'].to_list()]
#             # Potentially split time series data addition if None/nan exist in some readings
#             times_list, dataIRIs_list, values_list = _create_ts_subsets_to_add(times, dataIRIs, values)
#             for i in range(len(times_list)):
#                 added_fcs += len(dataIRIs_list[i])            
#                 ts = TSClient.create_timeseries(times_list[i], dataIRIs_list[i], values_list[i])
#                 ts_list.append(ts)
#                 for iri in dataIRIs_list[i]:
#                     query_string += f"<{iri}> , "
#     ts_client.bulkaddTimeSeriesData(ts_list)
#     print(f'Time series data for {added_fcs} forecasts successfully added.')
#     #logger.info(f'Time series data for {added_fcs} forecasts successfully added.')

#     # Strip trailing comma and close & perform creation date update query
#     query_string = query_string[:-2]
#     query_string += f") ) }}"
#     kg_client = KGClient(query_endpoint, update_endpoint)
#     kg_client.performUpdate(query_string)
#     #logger.info('Creation time triples successfully updated.')

#     return added_obs + added_fcs


# def add_all_readings_timeseries(api_key: str = DATAPOINT_API_KEY,
#                                 query_endpoint: str = QUERY_ENDPOINT,
#                                 update_endpoint: str = UPDATE_ENDPOINT) -> int:
#     """
#         Adds latest time series readings for all instantiated time series
#     """

#     updated_ts = add_readings_timeseries(api_key=api_key,
#                                          query_endpoint=query_endpoint,
#                                          update_endpoint=update_endpoint)

#     return updated_ts


def instantiate_station_readings(instantiated_sites_list: list,
                                 query_endpoint: str = QUERY_ENDPOINT,
                                 update_endpoint: str = UPDATE_ENDPOINT) -> int:
    """
        Instantiates readings for the provided list of measurement stations
        
        Arguments:
            instantiated_sites_list - list of dictionaries with instantiated
                                      stations/sites in the form [{id : iri},]
    """

    # Create MetOffice client to retrieve readings via API
    try:
        metclient = metoffer.MetOffer(api_key)
    except Exception as ex:
        #logger.error("MetOffer client could not be created to retrieve station readings. " + ex)
        raise APIException("MetOffer client could not be created to retrieve station readings.")
    
    # Initialise update query
    triples = f""

    # Initialise lists for TimeSeriesClient's bulkInit function
    dataIRIs = []
    dataClasses = []
    timeUnit = []

    # Get already instantiated observations and forecasts (across all stations)
    print('Retrieving instantiated observation/forecast triples from KG ...')
    #logger.info('Retrieving instantiated observation/forecast triples from KG ...')
    instantiated_obs = get_instantiated_observations(query_endpoint=query_endpoint, 
                                                     update_endpoint=update_endpoint)
    instantiated_fcs = get_instantiated_forecasts(query_endpoint=query_endpoint, 
                                                  update_endpoint=update_endpoint)
    print('Observation/forecast triples successfully retrieved.')
    #logger.info('Observation/forecast triples successfully retrieved.')

    # Get short version of variable type from full quantity type
    instantiated_obs['reading'] = instantiated_obs['quantityType'].apply(lambda x: x.split('#')[-1])
    instantiated_fcs['reading'] = instantiated_fcs['quantityType'].apply(lambda x: x.split('#')[-1])                                                        

    # Load available observations and forecasts from API
    print('Retrieving available observations/forecasts from API ...')
    #logger.info('Retrieving available observations/forecasts from API ...')
    available_obs, available_fcs, _ = retrieve_readings_data_per_station(metclient)
    print('Available observations/forecasts successfully retrieved.')
    #logger.info('Available observations/forecasts successfully retrieved.')

    # Initialise number of instantiated readings
    instantiated = 0

    # Loop over all sites   
    print('Create triples to instantiate static observation/forecast information ...')
    #logger.info('Create triples to instantiate static observation/forecast information ...')
    for id in instantiated_sites_list:
        
        # Get lists of instantiated readings for current station
        inst_obs = instantiated_obs[instantiated_obs['stationID'] == id]['reading'].tolist()
        inst_fcs = instantiated_fcs[instantiated_fcs['stationID'] == id]['reading'].tolist()

        # Get available observations and forecasts for that station
        try:
            avail_obs = available_obs[id]
        except KeyError:
            # In case no observation data is available for instantiated station
            avail_obs = []
        try:
            avail_fcs = available_fcs[id]
        except KeyError:
            # In case no forecast data is available for instantiated station
            avail_fcs = []
        
        # Derive quantities to instantiate
        obs = [i for i in avail_obs if i not in inst_obs]
        fcs = [i for i in avail_fcs if i not in inst_fcs]
        both = [i for i in obs if i in fcs]
        obs = list(set(obs) - set(both))
        fcs = list(set(fcs) - set(both))

        # Get station IRI
        station_iri = instantiated_sites_list[id]

        # Initialise
        triples1, triples2, triples3, triples4 = '', '', '', ''
        dataIRIs1, dataIRIs2, dataIRIs3, dataIRIs4 = [], [], [], []
        dataClasses1, dataClasses2, dataClasses3, dataClasses4 = [], [], [], []
        timeUnit3, timeUnit4 = None, None

        # Create triples and input lists for TimeSeriesClient bulkInit
        if obs or fcs or both:
            if both:
                triples1, reading_iris, dataIRIs1, dataClasses1, _ = add_readings_for_station(station_iri, both, is_observation=True)
                triples2, _, dataIRIs2, dataClasses2, _ = add_readings_for_station(station_iri, both, reading_iris, is_observation=False)
                instantiated += len(both)
            if obs:
                triples3, _, dataIRIs3, dataClasses3, timeUnit3 = add_readings_for_station(station_iri, obs, is_observation=True)
                instantiated += len(obs)
            if fcs:
                triples4, _, dataIRIs4, dataClasses4, timeUnit4 = add_readings_for_station(station_iri, fcs, is_observation=False)
                instantiated += len(fcs)

            # Add triples to INSERT DATA query
            triples += triples1
            triples += triples2
            triples += triples3
            triples += triples4

            # Append lists to overarching TimeSeriesClient input lists
            if dataIRIs1 + dataIRIs3:
                dataIRIs.append(dataIRIs1 + dataIRIs3)
                dataClasses.append(dataClasses1 + dataClasses3)
                timeUnit.append(timeUnit3)
            if dataIRIs2 + dataIRIs4:
                dataIRIs.append(dataIRIs2 + dataIRIs4)
                dataClasses.append(dataClasses2 + dataClasses4)
                timeUnit.append(timeUnit4)

            #logger.info(f'Readings for station {id:>6} successfully added to query.')

    # Split triples to instantiate into several chunks of max size
    queries = split_insert_query(triples, max=100000)

    # Instantiate all non-time series triples
    kg_client = KGClient(query_endpoint, update_endpoint)
    # Perform SPARQL update query in chunks to avoid heap size/memory issues
    print(f'Instantiate static observation/forecast triples in {len(queries)} chunks ...')
    #logger.info(f'Instantiate static observation/forecast triples in {len(queries)} chunks ...')
    for query in queries:
        kg_client.performUpdate(query)
    print('Observations/forecasts successfully instantiated/updated.')
    #logger.info('Insert query successfully performed.')

    if dataIRIs:
        print('Instantiate static time series triples ...')
        #logger.info('Instantiate static time series triples ...')
        # Instantiate all time series triples
        ts_client = TSClient.tsclient_with_default_settings()
        ts_client.bulkInitTimeSeries(dataIRIs, dataClasses, timeUnit)
        print('Time series triples successfully added.')
        #logger.info('Time series triples successfully added.')

    return instantiated


def instantiate_all_station_readings(query_endpoint: str = QUERY_ENDPOINT,
                                     update_endpoint: str = UPDATE_ENDPOINT) -> int:
    """
        Instantiates all readings for all instantiated stations
    """

    stations = get_all_airquality_stations(query_endpoint=query_endpoint,
                                           update_endpoint=update_endpoint)
    
    instantiated = instantiate_station_readings(instantiated_sites_list=stations,
                                                query_endpoint=query_endpoint,
                                                update_endpoint=update_endpoint)

    return instantiated


def update_all_stations(query_endpoint: str = QUERY_ENDPOINT,
                        update_endpoint: str = UPDATE_ENDPOINT):

    # Instantiate all available stations (ONLY not already existing stations
    # will be newly instantiated)
    print('\nUpdate instantiated stations: ')
    #logger.info('Update instantiated stations ...')
    t1 = time.time()
    new_stations = instantiate_all_stations(query_endpoint, update_endpoint)
    t2 = time.time()
    diff = t2-t1
    print(f'Finished after: {diff//60:5>n} min, {diff%60:4.2f} s \n')


    # Instantiate all available station readings (ONLY not already existing
    # readings will be newly instantiated)
    print('\nUpdate instantiated station readings: ')
    #logger.info('Update instantiated station readings ...')
    t1 = time.time()
    new_readings = instantiate_all_station_readings(query_endpoint, update_endpoint)
    t2 = time.time()
    diff = t2-t1
    print(f'Finished after: {diff//60:5>n} min, {diff%60:4.2f} s \n')

    # # Add latest readings time series to instantiated reading quantities
    # print('\nUpdate station readings time series data: ')
    # #logger.info('Update station readings time series data ...')
    # t1 = time.time()
    # updated_ts = add_all_readings_timeseries(query_endpoint, update_endpoint)
    # t2 = time.time()
    # diff = t2-t1
    # print(f'Finished after: {diff//60:5>n} min, {diff%60:4.2f} s \n')

    # return new_stations, new_readings, updated_ts


# def add_readings_for_station(station_iri: str,
#                              readings: list, readings_iris: list = None, 
#                              is_observation: bool = True,
#                              quantity_comments: list = None):
#     """
#         Return SPARQL update query string to instantiate readings for given 
#         station IRI (query string to be included in overarching INSERT DATA query)

#         Arguments:
#             station_iri - Station IRI without trailing '<' and '>'
#             readings - list of station readings to instantiate
#                        (i.e. OntoEMS concept names)
#             readings_iris - list of IRIs for station readings (only relevant to
#                             link observation and forecast readings for same quantity
#                             to same instance instead of creating duplicates)
#             is_observation - boolean to indicate whether readings are measure
#                              or forecast
#             quantity_comments - comments to be attached to quantities
        
#         Returns
#             triples - triples to be added to INSERT DATA query
#             created_reading_iris - list of newly created quantity IRIs
#             dataIRIs, dataClasses, timeUnit - to be appended to input arguments
#                                               to TimSeriesClient bulkInit call
#     """

#     if readings_iris and (len(readings) != len(readings_iris)):
#         #logger.error("Length or readings and readings_iris does not match.")
#         raise ValueError("Length or readings and readings_iris does not match.")

#     # Initialise "creation" time for forecasts
#     t = dt.datetime.utcnow().strftime('%Y-%m-%dT%H:00:00Z')

#     # Initialise return values : triples for INSERT DATA query & input lists
#     # for bulkInit function using TimeSeriesClient
#     triples = ''
#     dataIRIs = []
#     dataClasses = []
#     timeUnit = TIME_FORMAT
#     # List for all created station readings IRIs
#     created_reading_iris = []

#     # Get concepts and create IRIs
#     for i in range(len(readings)):
#         r = readings[i]
#         # Create IRI for reported quantity
#         quantity_type = EMS + r
#         if not readings_iris:
#             quantity_iri = KB + r + '_' + str(uuid.uuid4())
#             created_reading_iris.append(quantity_iri)
#         else:
#             quantity_iri = readings_iris[i]
#         # Create Measure / Forecast IRI
#         if is_observation:
#             data_iri = KB + 'Measure_' + str(uuid.uuid4())
#             data_iri_type = OM_MEASURE
#             creation_time = None
#         else:
#             data_iri = KB + 'Forecast_' + str(uuid.uuid4())
#             data_iri_type = EMS_FORECAST
#             creation_time = t

#         unit = UNITS_MAPPING[r][0]
#         symbol = UNITS_MAPPING[r][1]

#         # Add triples to instantiate
#         comment = quantity_comments[i] if quantity_comments else None
#         triples += add_om_quantity(station_iri, quantity_iri, quantity_type,
#                                    data_iri, data_iri_type, unit, symbol,
#                                    is_observation, creation_time=creation_time, 
#                                    comment=comment)

#         # Get data to bulkInit time series
#         dataIRIs.append(data_iri)
#         dataClasses.append(DATACLASS)

#     return triples, created_reading_iris, dataIRIs, dataClasses, timeUnit


def retrieve_readings_data_from_api(crs: str = 'EPSG:4326') -> list:
    """
        Retrieve and condition station readings data from UK Air API

        Arguments:
            crs - coordinate reference system in which to return station 
                  locations (as EPSG code, e.g. 'EPSG:4326'). EPSG:4326 coordinates
                  are provided as [lat, long] and specifying any other CRS can 
                  potentially result in switched values for lat and lon
        Returns:
            List of dicts with station data as returned by API
    """

    # Construct API call to get extended information for all staions (i.e. basic
    # station data incl. information about reported readings/timeseries)
    if crs and re.match(r"EPSG:\d+", crs):
        url = f'https://uk-air.defra.gov.uk/sos-ukair/api/v1/stations?{crs}&expanded=true'
        if crs != 'EPSG:4326':
            print('Provided CRS is different from "EPSG:4326". Extraction of ' \
                + 'latitude and longitude can be erroneous.')
            #logger.info('Provided CRS is different from "EPSG:4326". Extraction of ' \
            #          + 'latitude and longitude can be erroneous.')
    else:
        raise InvalidInput ("Provided CRS does not match expected 'EPSG:' format.")

    try:
        print('Retrieving station data from API ...')
        #logger.info('Retrieving station data from API ...')
        stations_raw = requests.get(url=url).json()
        print('Station data successfully retrieved.')
        #logger.info('Station data successfully retrieved.')
    except Exception as ex:
        #logger.error("Error while retrieving station data from API.")
        raise APIException("Error while retrieving station data from API.")  

    # Create DataFrame from json response and condition data
    # StationIDs are no unique identifiers for stations (e.g. a station with several
    # measurement features has different IDs). Hence, the  station name will also 
    # serve as unique identifier
    stations = [{'station': s['properties']['label'].split('-')[0],
                 'latitude': s['geometry']['coordinates'][0],
                 'longitude': s['geometry']['coordinates'][1], 
                 'elevation': s['geometry']['coordinates'][2],
                 'ts_ids': list(s['properties']['timeseries'].keys())
                } for s in stations_raw ]
    df = pd.DataFrame(stations)
    df = df.explode('ts_ids', ignore_index=True)
    
    # Clean and condition returned API data
    df = clean_api_data(df)
    #df.set_index('ts_ids', inplace=True)

    # Add timeseries information
    ts_info = df['ts_ids'].apply(lambda x: retrieve_individual_timeseries_information_from_api(x))
    df[['pollutant', 'eionet', 'unit']] = ts_info.apply(pd.Series)

    # Add timeseries data


    # Create return list of dicts
    df.set_index('ts_ids', inplace=True)
    stations = [{k: v} for k,v in df.to_dict('index').items()]

    return stations

def retrieve_timeseries_information_from_api(ts_ids=[]) -> dict:
    """
        Retrieve information about the nature of particular timeseries/
        station reading(s) (but not the time series data itself)
        (returns information for all available timeseries if empty list is provided)

        Arguments:
            ts_ids - (list of) ID(s) of reading/timeseries
        Returns:
            Dictionary with readings information (timeseries ID as key and
            information as dictionary)
    """

    # API call to get information for provided timeseries ID(s)
    if isinstance(ts_ids, str):
        url = f'https://uk-air.defra.gov.uk/sos-ukair/api/v1/timeseries/{ts_ids}'
        ts_ids = [ts_ids]
    elif isinstance(ts_ids, list):
        url = f'https://uk-air.defra.gov.uk/sos-ukair/api/v1/timeseries?expanded=true'
    else:
        raise InvalidInput('Provided timeseries ID(s) must be a (list of) string(s).')
    try:
        ts_raw = requests.get(url=url).json()
    except Exception as ex:
        #logger.error("Error while retrieving timeseries information from API.")
        raise APIException("Error while retrieving timeseries information from API.")
    # Ensure returned data is list of dictionaries
    if isinstance(ts_raw, dict):
        ts_raw = [ts_raw]

    # Return information about all timeseries if empty list is provided
    if not ts_ids:
        ts_ids = [ts['id'] for ts in ts_raw]

    # Initialise return dictionary
    infos = {}

    # Extract relevant information from JSON response
    for ts in ts_raw:
        info = {}
        # Get information if exists (else returns None)
        id = ts.get('id')
        if id in ts_ids:
            pollutant = ts.get('parameters', {}).get('offering', {}).get('label')
            eionet = ts.get('parameters', {}).get('phenomenon', {}).get('label')
            unit = ts.get('uom')
            # Populate dictionary
            info['pollutant'] = pollutant.split('-')[-1] if pollutant else 'n/a'
            info['eionet'] = eionet if eionet else 'n/a'
            info['unit'] = unit if unit else 'n/a'
            infos[id] = info

    return infos


if __name__ == '__main__':


    t1 = time.time()
    d1 = retrieve_timeseries_information_from_api()
    #d2 = retrieve_readings_data_from_api()
    t2 = time.time()
    print(f'Elapsed seconds: {t2-t1: .2} s')

    response = update_all_stations()
    print(f"Number of instantiated stations: {response[0]}")
    print(f"Number of instantiated readings: {response[1]}")
    print(f"Number of updated time series readings (i.e. dataIRIs): {response[2]}")
