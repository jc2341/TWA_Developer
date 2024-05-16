from contextlib import contextmanager
from py4jps import agentlogging
from agent.utils.baselib_gateway import jpsBaseLibGW
from agent.utils.stack_configs import DB_URL, DB_USER, DB_PASSWORD
from agent.errorhandling.exceptions import TSException
from agent.datamodel.data_class import FORMAT, TIMECLASS, DATACLASS

# Initialise logger instance (ensure consistent logger level)
logger = agentlogging.get_logger('prod')

class TSClient:
    # Create ONE JVM module view on class level and import all required java classes
    jpsBaseLibView = jpsBaseLibGW.createModuleView()
    jpsBaseLibGW.importPackages(jpsBaseLibView, "uk.ac.cam.cares.jps.base.query.*")
    jpsBaseLibGW.importPackages(jpsBaseLibView, "uk.ac.cam.cares.jps.base.timeseries.*")

    # Date/Time data type: Instant
    # PostgreSQL supported data types: https://www.jooq.org/javadoc/dev/org.jooq/org/jooq/impl/SQLDataType.html
    Instant = jpsBaseLibView.java.time.Instant
    INSTANT = Instant.now().getClass()

    def __init__(self, kg_client, timeclass=INSTANT, rdb_url=DB_URL, rdb_user=DB_USER, rdb_password=DB_PASSWORD):
        """
        Initialise TimeSeriesClient (default properties taken from environment variables)

        Arguments:
            kg_client (KGClient): KGClient object (as per `kgclient.py`)
            timeclass: Java time class objects supported by PostgreSQL
                       (see: https://www.jooq.org/javadoc/dev/org.jooq/org/jooq/impl/SQLDataType.html)
            rdb_url (str): URL of relational database
            rdb_user (str): Username for relational database
            rdb_password (str): Password for relational database
        """
        # 1) Create an instance of a RemoteStoreClient (to retrieve RDB connection)
        try:
            self.connection = TSClient.jpsBaseLibView.RemoteRDBStoreClient(rdb_url, rdb_user, rdb_password)
            logger.info("Remote RDB Store client initialized successfully.")
        except Exception as ex:
            logger.error("Unable to initialise TS Remote Store client.")
            raise TSException("Unable to initialise TS Remote Store client.") from ex

        # 2) Initiliase TimeSeriesClient
        try:
            self.tsclient = TSClient.jpsBaseLibView.TimeSeriesClient(kg_client.kg_client, timeclass)
            logger.info("TimeSeriesClient initialized successfully.")
        except Exception as ex:
            logger.error("Unable to initialise TS client.")
            raise TSException("Unable to initialise TS client.") from ex

    @contextmanager
    def connect(self):
        """
        Create context manager for RDB connection using getConnection method of Java
        TimeSeries client (i.e. to ensure connection is closed after use)
        """
        conn = None
        try:
            conn = self.connection.getConnection()
            logger.info("Connected to the RDB successfully.")
            yield conn
        except Exception as ex:
            logger.error(f"Failed to connect to RDB: {ex}")
            raise TSException("Failed to connect to RDB.") from ex
        finally:
            if conn is not None:
                conn.close()
                logger.info("RDB connection closed.")

    @staticmethod
    def create_timeseries(times: list, dataIRIs: list, values: list):
        """
        Create Java TimeSeries object (i.e. to attach via TSClient)

        Arguments:
            times (list): List of time stamps
            dataIRIs (list): List of dataIRIs
            values (list): List of list of values per dataIRI
        """
        try:
            timeseries = TSClient.jpsBaseLibView.TimeSeries(times, dataIRIs, values)
            logger.info("TimeSeries object created successfully.")
        except Exception as ex:
            logger.error("Unable to create TimeSeries object.")
            raise TSException("Unable to create timeseries.") from ex

        return timeseries

    # def init_timeseries(self, dataIRI, times, values, ts_type, time_format):
    #     assert isinstance(dataIRI, list) and all(isinstance(x, str) for x in dataIRI), "dataIRI must be a list of strings"

    #     try:
    #         with self.connect() as conn:
    #             logger.info(f"Initializing time series for dataIRI: {dataIRI}")
                
    #             # Detailed logging for dataIRI, times, and values
    #             logger.debug(f"dataIRI: {dataIRI}")
    #             logger.debug(f"times: {times}")
    #             logger.debug(f"values: {values}")
    #             logger.debug(f"ts_type: {ts_type}")
    #             logger.debug(f"time_format: {time_format}")

    #             # Ensure dataIRI, times, and values are lists of the correct type
    #             assert isinstance(dataIRI, list), "dataIRI should be a list"
    #             assert all(isinstance(i, str) for i in dataIRI), "dataIRI elements should be strings"
    #             assert isinstance(times, list), "times should be a list"
    #             assert all(isinstance(i, str) for i in times), "times elements should be strings"
    #             assert isinstance(values, list), "values should be a list of lists"
    #             assert all(isinstance(v, list) for v in values), "values elements should be lists"
    #             assert all(all(isinstance(i, (int, float)) for i in v) for v in values), "values elements should contain numbers"

    #             logger.info(f"Calling initTimeSeries with dataIRI: {dataIRI}")
    #             self.tsclient.initTimeSeries([dataIRI], [ts_type] * len(dataIRI), time_format, conn)
                
    #             logger.info(f"Creating TimeSeries object with times: {times}")
    #             ts = TSClient.create_timeseries(times, [dataIRI], [values])
                
    #             logger.info(f"Adding TimeSeries data to TSClient")
    #             self.tsclient.addTimeSeriesData(ts, conn)
                
    #             logger.info(f"Time series successfully initialized in KG and RDB for dataIRI: {dataIRI}")
            
    #     except Exception as ex:
    #         logger.error(f"Failed to initialize time series for dataIRI: {dataIRI}. Exception: {ex}")
    #         raise TSException(f"Failed to initialize time series for dataIRI: {dataIRI}.") from ex
    def init_timeseries(self, dataIRI, times, values, ts_type, time_format):
        assert isinstance(dataIRI, list) and all(isinstance(x, str) for x in dataIRI), "dataIRI must be a list of strings"
        
        try:
            with self.connect() as conn:
                logger.info(f"Initializing time series for dataIRI: {dataIRI}")
                
                # Detailed logging for dataIRI, times, and values
                logger.debug(f"dataIRI: {dataIRI}")
                logger.debug(f"times: {times}")
                logger.debug(f"values: {values}")
                logger.debug(f"ts_type: {ts_type}")
                logger.debug(f"time_format: {time_format}")

                # Ensure dataIRI, times, and values are lists of the correct type
                assert isinstance(dataIRI, list), "dataIRI should be a list"
                assert all(isinstance(i, str) for i in dataIRI), "dataIRI elements should be strings"
                assert isinstance(times, list), "times should be a list"
                assert all(isinstance(i, str) for i in times), "times elements should be strings"
                assert isinstance(values, list), "values should be a list of lists"
                assert all(isinstance(v, list) for v in values), "values elements should be lists"
                assert all(all(isinstance(i, (int, float)) for i in v) for v in values), "values elements should contain numbers"

                # Operation 1: Initializing time series in the time series client
                try:
                    logger.info(f"Calling initTimeSeries with dataIRI: {dataIRI}")
                    self.tsclient.initTimeSeries(dataIRI, [ts_type] * len(dataIRI), time_format, conn)
                except Exception as ex:
                    logger.error(f"Failed to call initTimeSeries for dataIRI: {dataIRI}. Exception: {ex}")
                    raise Exception(f"Failed to call initTimeSeries for dataIRI: {dataIRI}.") from ex

                # Operation 2: Creating a TimeSeries object
                try:
                    logger.info(f"Creating TimeSeries object with times: {times}")
                    ts = TSClient.create_timeseries(times, dataIRI, values)
                except Exception as ex:
                    logger.error(f"Failed to create TimeSeries object for dataIRI: {dataIRI}. Exception: {ex}")
                    raise Exception(f"Failed to create TimeSeries object for dataIRI: {dataIRI}.") from ex

                # Operation 3: Adding TimeSeries data to the client
                try:
                    logger.info(f"Adding TimeSeries data to TSClient")
                    self.tsclient.addTimeSeriesData(ts, conn)
                except Exception as ex:
                    logger.error(f"Failed to add TimeSeries data for dataIRI: {dataIRI}. Exception: {ex}")
                    raise Exception(f"Failed to add TimeSeries data for dataIRI: {dataIRI}.") from ex

                logger.info(f"Time series successfully initialized in KG and RDB for dataIRI: {dataIRI}")

        except Exception as ex:
            logger.error(f"Failed to initialize time series for dataIRI: {dataIRI}. General exception: {ex}")
            raise TSException(f"Failed to initialize time series for dataIRI: {dataIRI}.") from ex


    def add_ts_data(self, dataIRI, times, values):
        """
        This method adds time series data to an already instantiated time series
        (potentially already existing values for same time stamp get overwritten)

        Arguments:
            dataIRI (str): IRI of instance with hasTimeSeries relationship
            times (list): List of times/dates
            values (list): List of actual values
        """
        try:
            with self.connect() as conn:
                ts = TSClient.create_timeseries(times, [dataIRI], [values])
                self.tsclient.addTimeSeriesData(ts, conn)
                logger.info(f"Time series data successfully added to dataIRI: {dataIRI}")
        except Exception as ex:
            logger.error(f"Failed to add time series data to dataIRI: {dataIRI}. Exception: {ex}")
            raise TSException(f"Failed to add time series data to dataIRI: {dataIRI}.") from ex

    def replace_ts_data(self, dataIRI, times, values):
        """
        This method replaces all data of an already instantiated time series
        (i.e., clears all existing data and adds new data)

        Arguments:
            dataIRI (str): IRI of instance with hasTimeSeries relationship
            times (list): List of times/dates
            values (list): List of actual values
        """
        try:
            with self.connect() as conn:
                logger.info(f"Replacing time series data for dataIRI: {dataIRI}")
                # Clear existing data
                t1 = self.tsclient.getMinTime(dataIRI, conn)
                t2 = self.tsclient.getMaxTime(dataIRI, conn)
                self.tsclient.deleteTimeSeriesHistory(dataIRI, t1, t2, conn)
                # Add new data
                ts = TSClient.create_timeseries(times, [dataIRI], [values])
                self.tsclient.addTimeSeriesData(ts, conn)
                logger.info(f"Time series data successfully replaced for dataIRI: {dataIRI}")
        except Exception as ex:
            logger.error(f"Failed to replace time series data for dataIRI: {dataIRI}. Exception: {ex}")
            raise TSException(f"Failed to replace time series data for dataIRI: {dataIRI}.") from ex

    def retrieve_timeseries(self, dataIRI, lowerbound=None, upperbound=None):
        """
        This method retrieves the time series data for a given dataIRI

        Arguments:
            dataIRI (str): IRI of instance with hasTimeSeries relationship
            lowerbound (str): Lower bound of time series data
            upperbound (str): Upper bound of time series data
        """
        try:
            with self.connect() as conn:
                ts = self.tsclient.getTimeSeriesWithinBounds([dataIRI], lowerbound, upperbound, conn)
            times = ts.getTimes()
            values = ts.getValues(dataIRI)
            # Unwrap Java time objects
            times = [t.toString() for t in times]
            logger.info(f"Time series data retrieved for dataIRI: {dataIRI}")
            return times, values
        except Exception as ex:
            logger.error(f"Failed to retrieve time series data for dataIRI: {dataIRI}. Exception: {ex}")
            raise TSException(f"Failed to retrieve time series data for dataIRI: {dataIRI}.") from ex

    def delete_timeseries(self, dataIRI: str):
        try:
            with self.connect() as conn:
                logger.info(f"Deleting time series for dataIRI: {dataIRI}")
                # Initialise time series in Blazegraph and PostgreSQL
                self.tsclient.deleteIndividualTimeSeries(dataIRI, conn)
                logger.info(f"Time series successfully deleted for dataIRI: {dataIRI}")
        except Exception as ex:
            logger.error(f"Error deleting GPS trajectory time series for dataIRI: {dataIRI}. Exception: {ex}")
            raise TSException(f"Error deleting GPS trajectory time series for dataIRI: {dataIRI}.") from ex
