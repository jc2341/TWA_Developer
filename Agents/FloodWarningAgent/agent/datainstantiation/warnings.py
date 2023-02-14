################################################
# Authors: Markus Hofmeister (mh807@cam.ac.uk) #    
# Date: 10 Feb 2023                            #
################################################

# The purpose of this module is to provide functions to retrieve 
# flood warnings and alerts from the API and instantiate it in the KG

import uuid

from datetime import datetime as dt

from agent.datainstantiation.ea_data import retrieve_current_warnings, \
                                            retrieve_flood_area_data
from agent.datainstantiation.ons_data import retrieve_ons_county
from agent.kgutils.kgclient import KGClient
from agent.kgutils.querytemplates import *
from agent.utils.stack_configs import QUERY_ENDPOINT, UPDATE_ENDPOINT
from agent.utils.stackclients import GdalClient, GeoserverClient, \
                                     OntopClient, PostGISClient, \
                                     create_geojson_for_postgis

from py4jps import agentlogging

# Initialise logger
#TODO: Update logger level
logger = agentlogging.get_logger("dev")


def update_warnings(county=None):
    """
    Update instantiated flood warnings/alerts incl. associated flood areas in the KG, 
    i.e. instantiate missing ones, update existing ones, and archive outdated ones

    Arguments:
    county (str): County name for which to instantiate flood warnings (e.g. 'Hampshire')
                  Instantiates ALL current warnings if no county given
    """

    # Create KG client 
    kgclient = KGClient(QUERY_ENDPOINT, UPDATE_ENDPOINT)

    if county:
        ### Update flood warnings for given county ###
        #TODO: to be implemented, see note in agent\flaskapp\inputtasks\routes.py
        logger.warning("County-specific instantiation not yet implemented")
    else:
        ### Update all flood warnings ###
        # 1) Retrieve current flood warnings from API
        logger.info("Retrieving current flood warnings from API ...")
        warnings_data_api = retrieve_current_warnings()
        logger.info("Current flood warnings retrieved.")

        # 2) Retrieve instantiated flood warnings and flood areas from KG
        logger.info("Retrieving instantiated flood warnings and areas from KG ...")
        areas_kg = get_instantiated_flood_areas(kgclient=kgclient)  
        warnings_kg = get_instantiated_flood_warnings(kgclient=kgclient)      
        logger.info("Instantiated warnings and areas retrieved.")

        # 3) Extract (currently active) flood warnings and flood areas to be instantiated
        area_uris = [w.get('area_uri') for w in warnings_data_api]
        area_uris = [a for a in area_uris if a is not None]
        areas_to_instantiate = [a for a in area_uris if a not in areas_kg]
        warning_uris = [w.get('warning_uri') for w in warnings_data_api]
        warning_uris = [w for w in warning_uris if w is not None]
        warnings_to_instantiate = [w for w in warning_uris if w not in warnings_kg]
    
        # 4) Extract flood warnings to be updated (i.e. outdated information in KG)
        last_altered = {w['warning_uri']: w['last_altered'] for w in warnings_data_api}
        warnings_to_update = [w for w in warning_uris if w in warnings_kg]
        warnings_to_update = [w for w in warnings_to_update if warnings_kg.get(w) < last_altered.get(w)]

        # 5) Instantiate missing flood areas and warnings
        logger.info("Instantiating flood warnings and areas ...")
        instantiated_areas, instantiated_warnings = \
            instantiate_flood_areas_and_warnings(areas_to_instantiate, areas_kg,
                                                 warnings_to_instantiate, warnings_data_api, 
                                                 kgclient=kgclient)
        logger.info("Instantiation finished.")

        # 6) Update outdated flood warnings
        logger.info("Updating flood warnings ...")
        # updated_warnings = \
        #     instantiate_flood_areas_and_warnings(areas_to_instantiate, areas_kg,
        #                                          warnings_to_instantiate, warnings_data_api, 
        #                                          kgclient=kgclient)
        logger.info("Updating finished.")


    return instantiated_areas, instantiated_warnings, None, None


def instantiate_flood_areas_and_warnings(areas_to_instantiate: list, areas_kg: dict,
                                         warnings_to_instantiate: list, warnings_data_api: list,
                                         query_endpoint=QUERY_ENDPOINT, kgclient=None):
    """
    Instantiate flood areas and warnings in the KG

    Arguments:
        areas_to_instantiate (list): List of flood area URIs to be instantiated
        areas_kg (dict): Dictionary with instantiated flood area URIs as keys and flood area IRIs as values
        warnings_to_instantiate (list): List of flood warning URIs to be instantiated
        warnings_data_api (list): List of dictionaries with flood warning data from API

    Returns:
        Number of newly instantiated flood areas and flood warnings as int
    """

    # Create KG client if not provided
    if not kgclient:
        kgclient = KGClient(query_endpoint, query_endpoint)

    # Retrieve data for flood areas from API
    areas_data_to_instantiate = []
    logger.info("Retrieving missing flood areas from API ...")
    for area in areas_to_instantiate:
        areas_data_to_instantiate.append(retrieve_flood_area_data(area))
    logger.info("Missing flood areas retrieved.")

    # Instantiate missing flood areas
    new_areas, area_location_map = \
        instantiate_flood_areas(areas_data_to_instantiate, kgclient=kgclient)
    # Create overarching mapping between flood area URIs and location IRIs, i.e.
    # already instantiated and newly instantiated ones
    area_location_map.update(areas_kg)
    
    # Instantiate missing flood warnings
    warning_data_to_instantiate = [w for w in warnings_data_api if w.get('warning_uri') in warnings_to_instantiate]
    # Add location IRIs to warning data
    for w in warning_data_to_instantiate:
        w['location_iri'] = area_location_map.get(w.get('area_uri'))
    new_warnings = instantiate_flood_warnings(warning_data_to_instantiate, kgclient=kgclient)

    return new_areas, new_warnings


def get_instantiated_flood_warnings(query_endpoint=QUERY_ENDPOINT,
                                    kgclient=None) -> dict:
    """
    Retrieve all instantiated flood warnings with latest update timestamp

    Arguments:
        query_endpoint - SPARQL endpoint from which to retrieve data
        kgclient - pre-initialized KG client with endpoints

    Returns:
        warnings (dict): Dictionary with flood warning/alert IRIs as keys and
                         latest update timestamp as values
    """

    # Create KG client if not provided
    if not kgclient:
        kgclient = KGClient(query_endpoint, query_endpoint)
    
    # Retrieve instantiated flood warnings
    query = get_all_flood_warnings()
    res = kgclient.performQuery(query)

    # Unwrap results
    warning = [r.pop('warning_iri') for r in res]
    last_altered = [list(r.values()) for r in res]
    for i in range(len(last_altered)):
        last_altered[i] = [dt.strptime(t, BLAZEGRAPH_TIME_FORMAT) for t in last_altered[i]]
        last_altered[i] = max(last_altered[i])
    warnings = dict(zip(warning, last_altered))

    return warnings


def get_instantiated_flood_areas(query_endpoint=QUERY_ENDPOINT,
                                 kgclient=None) -> dict:
    """
    Retrieve all instantiated flood areas with associated 'hasLocation' Location IRIs

    Arguments:
        query_endpoint - SPARQL endpoint from which to retrieve data
        kgclient - pre-initialized KG client with endpoints

    Returns:
        areas (dict): Dictionary with flood area IRIs as keys and associated 
                      Location IRIs as values
    """

    # Create KG client if not provided
    if not kgclient:
        kgclient = KGClient(query_endpoint, query_endpoint)
    
    # Retrieve instantiated flood warnings
    query = get_all_flood_areas()
    res = kgclient.performQuery(query)

    # Unwrap results
    areas = {r['area_iri']: r['location_iri'] for r in res}

    return areas


def instantiate_flood_areas(areas_data: list=[],
                            query_endpoint=QUERY_ENDPOINT,
                            kgclient=None):
    """
    Instantiate list of flood area data dicts as retrieved from API by 'retrieve_flood_area_data'

    Arguments:
        areas_data (list): List of dicts with relevant flood area data
        query_endpoint - SPARQL endpoint from which to retrieve data
        kgclient - pre-initialized KG client with endpoints

    Returns:
        Number (int) of instantiated flood areas
        Dict with instantiated area IRIs as keys and Location IRIs as values
    """

    # Create KG client if not provided
    if not kgclient:
        kgclient = KGClient(query_endpoint, query_endpoint)

    # Initialise relevant Stack Clients and parameters
    postgis_client = PostGISClient()
    gdal_client = GdalClient()
    geoserver_client = GeoserverClient()

    triples = ''
    area_location_map = {}
    for area in areas_data:
        # Create IRIs for Location associated with flood area (and potential flood event)
        area['location_iri'] = KB + 'Location' + str(uuid.uuid4())
        area_location_map[area['area_uri']] = area['location_iri']
        area['admin_district_iri'] = KB + 'AdministrativeDistrict' + str(uuid.uuid4())
        # Create waterbody IRI
        area['waterbody_iri'] = KB + area['water_body_type'].capitalize() + str(uuid.uuid4())

        # Retrieve county IRI from ONS API
        logger.info("Retrieving county IRI from ONS API ...")
        area['county_iri'] = retrieve_ons_county(area['county'])

        # Create GeoJSON string for PostGIS upload
        logger.info("Create FloodArea GeoJSON string for PostGIS upload ...")
        props = ['polygon_uri', 'area_uri', 'area_types', 'county_iri', 'water_body_label']
        props = {p: area[p] for p in props}
        geojson_str = create_geojson_for_postgis(**props, kg_endpoint=query_endpoint)

        # Remove dictionary keys not required for instantiation
        area.pop('county', None)
        poly_uri = area.pop('polygon_uri', None) 

        # 1) Prepare instantiation in KG (done later in bulk)
        triples += flood_area_instantiation_triples(**area)
        
        # 2) Upload polygon to PostGIS
        # Upload OBDA mapping and create Geoserver layer when first geospatial
        # data is uploaded to PostGIS
        if not postgis_client.check_table_exists():
            logger.info('Uploading OBDA mapping ...')
            OntopClient.upload_ontop_mapping()
            # Initial data upload required to create postGIS table and Geoserver layer            
            logger.info('Uploading GeoJSON to PostGIS ...')
            gdal_client.uploadGeoJSON(geojson_str)
            logger.info('Creating layer in Geoserver ...')
            geoserver_client.create_workspace()
            geoserver_client.create_postgis_layer()
        else:        
            # Upload new geospatial information
            if not postgis_client.check_flood_area_exists(area['area_uri'], poly_uri):
                logger.info('Uploading GeoJSON to PostGIS ...')
                gdal_client.uploadGeoJSON(geojson_str)

    # Create INSERT query and perform update
    query = f"INSERT DATA {{ {triples} }}"
    kgclient.performUpdate(query)

    return len(areas_data), area_location_map


def instantiate_flood_warnings(warnings_data: list=[],
                               query_endpoint=QUERY_ENDPOINT,
                               kgclient=None):
    """
    Instantiate list of flood warning data dicts as retrieved from API by 'retrieve_current_warnings',
    further enriched with location_iri created by 'flood_area_instantiation_triples'

    Arguments:
        warnings_data (list): List of dicts with relevant flood warnings/alerts data
        query_endpoint - SPARQL endpoint from which to retrieve data
        kgclient - pre-initialized KG client with endpoints

    Returns:
        Number (int) of instantiated flood warnings/alerts
    """

    # Create KG client if not provided
    if not kgclient:
        kgclient = KGClient(query_endpoint, query_endpoint)

    triples = ''
    for warning in warnings_data:
        # Create IRI of potential flood event
        warning['flood_event_iri'] = KB + 'Flood_' + str(uuid.uuid4())

        # Remove dictionary keys not required for instantiation
        warning.pop('last_altered', None)
        
        # Construct triples for flood warning/alert instantiation
        triples += flood_warning_instantiation_triples(**warning)

    # Create INSERT query and perform update
    query = f"INSERT DATA {{ {triples} }}"
    kgclient.performUpdate(query)

    return len(warnings_data)
     