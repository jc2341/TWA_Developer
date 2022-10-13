/**
 * Concrete implementation of the MapHandler class that handles
 * a single CesiumJS map instance.
 */
class MapHandler_Cesium extends MapHandler {

    /**
     * MapBox popup element.
     */
    public static POPUP;

    /**
     * Map of data source keyed by layer name.
     */
    public static DATA_SOURCES = {};

    /**
     * Constructor.
     */
    constructor(manager: Manager) {
        super(manager);
    }

    /**
     * Initialise and store a new map object.
     */
    public initialiseMap(mapOptions: Object) {
        MapHandler.MAP_OPTIONS = mapOptions;

        if(MapHandler.MAP === null || MapHandler.MAP === undefined) {

            // Build the URL to pull tile imagery from Mapbox (defaults to dark theme)
            let tileURL = getDefaultImagery();

            // Initialize the Cesium Viewer in the HTML element with the `cesiumContainer` ID.
            MapHandler.MAP = new Cesium.Viewer('map', {
                timeline: false,
                animation: false,
                baseLayerPicker: false, 
                homeButton: false, 
                infoBox: false, 
                navigationHelpButton: false,
                projectionPicker: false,
                fullscreenButton: false,
                geocoder: false,
                selectionIndicator: false,
                sceneModePicker: false
            }); 

            // Remove any existing imagery providers and add our own
            MapHandler.MAP.imageryLayers.removeAll(true);
            let imageryProvider = new Cesium.UrlTemplateImageryProvider({
                url: tileURL,
                credit: "mapbox"
            });
            MapHandler.MAP.scene.imageryLayers.addImageryProvider(imageryProvider);

            // Override mouse controls 
            let controller = MapHandler.MAP.scene.screenSpaceCameraController;
            controller.tiltEventTypes = [Cesium.CameraEventType.RIGHT_DRAG];
            controller.zoomEventTypes = controller.zoomEventTypes.filter(item => item !== Cesium.CameraEventType.RIGHT_DRAG);

            // Dodgy, but the only way to change the zoom increment
            controller._zoomFactor = 2;

            // Enable picking
            let handler = new Cesium.ScreenSpaceEventHandler(MapHandler.MAP.scene.canvas);
            handler.setInputAction(event => this.handleClick(event), Cesium.ScreenSpaceEventType.LEFT_CLICK);

            // Enable hover-over silhouette
            CesiumUtils.enableSilhouettes();
            handler.setInputAction(event => this.handleMouse(event), Cesium.ScreenSpaceEventType.MOUSE_MOVE);

            MapHandler.MAP.camera.setView({
                destination : Cesium.Cartesian3.fromDegrees(mapOptions["center"][0], mapOptions["center"][1], mapOptions["center"][2]),
                orientation: {
                    heading: Cesium.Math.toRadians(mapOptions["heading"]),
                    pitch: Cesium.Math.toRadians(mapOptions["pitch"]),
                    roll: Cesium.Math.toRadians(mapOptions["roll"])
                }
            });
            MapHandler.MAP.scene.requestRender();

        } else {
            MapHandler.MAP.camera.setView({
                destination : Cesium.Cartesian3.fromDegrees(mapOptions["center"][0], mapOptions["center"][1], mapOptions["center"][2]),
                orientation: {
                    heading: Cesium.Math.toRadians(mapOptions["heading"]),
                    pitch: Cesium.Math.toRadians(mapOptions["pitch"]),
                    roll: Cesium.Math.toRadians(mapOptions["roll"])
                }
            });
            MapHandler.MAP.scene.requestRender();
        }
    }

    /**
     * Handles a click event on the map.
     * 
     * @param event mouse event
     */
    public handleClick(event) {
        if(!MapHandler.ALLOW_CLICKS) return;

        // Get the feature at the click point
        let self = this;
        CesiumUtils.getFeature(event, function(feature) {
            if(feature instanceof Cesium.ImageryLayerFeatureInfo) {
                // 2D WMS feature
                let properties = {...feature.data.properties};
                self.manager.showFeature(feature, properties);
            } else {
                // 3D feature
                let properties = {};
                let contentMetadata = feature?.content?.metadata;
    
                // Transform properties for compatability with manager code
                if (Cesium.defined(contentMetadata)) {
                    contentMetadata.getPropertyIds().forEach(id => {
                        properties[id] = contentMetadata.getProperty(id);
                    });
                } else {
                    feature.getPropertyIds().forEach(id => {
                        properties[id] = feature.getProperty(id);
                    });
                }
    
                self.manager.showFeature(feature, properties);
                CesiumUtils.setSelectedSilhouette(feature, event);
            }
        });
    }

    /**
     * Handles logic when the mouse cursor moves over or out of features.
     * 
     * @param event mouse event
     */
    private handleMouse(event) {
        if(!MapHandler.ALLOW_CLICKS) return;
        let metaBox = document.getElementById("cesiumMetaBox");
        metaBox.style.display = "none";

        // Get the feature at the click point
        CesiumUtils.getFeature(event, function(feature) {

            if(feature instanceof Cesium.ImageryLayerFeatureInfo) {
                // 2D WMS feature
                let properties = {...feature.data.properties};

                if(properties.hasOwnProperty("name")) {
                    metaBox.style.display = "block";
                    metaBox.style.bottom = `${MapHandler.MAP.canvas.clientHeight - event.endPosition.y + 50}px`;
                    metaBox.style.left = `${event.endPosition.x - 100}px`;
                    metaBox.innerHTML = properties["name"];
                } 

            } else {
                // 3D feature
                let properties = {};
                let contentMetadata = feature?.content?.metadata;
    
                // Transform properties for compatability with manager code
                if (Cesium.defined(contentMetadata)) {
                    contentMetadata.getPropertyIds().forEach(id => {
                        properties[id] = contentMetadata.getProperty(id);
                    });
                } else {
                    // Do nothing, there's no data?
                    // feature.getPropertyIds().forEach(id => {
                    //     properties[id] = feature.getProperty(id);
                    // });
                }

                if(properties.hasOwnProperty("name")) {
                    metaBox.style.display = "block";
                    metaBox.style.bottom = `${MapHandler.MAP.canvas.clientHeight - event.endPosition.y + 50}px`;
                    metaBox.style.left = `${event.endPosition.x - 100}px`;
                    metaBox.innerHTML = properties["name"];
                }
            }
        });
    }

    /**
     * Plot the contents of the input data group on the map.
     */
    public plotData(dataStore: DataStore) {
        dataStore.dataGroups.forEach(rootGroup => {
            let allLayers = rootGroup.flattenDown();
            
            allLayers.forEach(layer => {
                this.plotLayer(rootGroup, layer);
            });
        });
    }

    /**
     * Creates a visual layer on the map based on the input layer definition.
     * 
     * @param group DataGroup containing the layer.
     * @param layer definition of layer to create.
     */
    public plotLayer(group: DataGroup, layer: DataLayer) {
        let source = layer.source;
        if(source === null || source === undefined) return;

        // Add the layer's source to the map
        this.addSource(source);

        // Add the layer itself to the map
        this.addLayer(layer);
    }

    /**
     * Adds the data source to the map.
     * 
     * @param group group containing the source.
     * @param source data source.
     */
    private addSource(source: DataSource) {
       // No used in Cesium implementation
    }

    /**
     * Adds the data layer to the map.
     * 
     * @param layer layer to add.
     */
    private addLayer(layer: DataLayer) {
        let source = layer.source.definition;

        // Check the required parameters for ALL types
        let dataURI = source["uri"];
        if(dataURI === null || dataURI === undefined) {
            console.error("Cannot plot a data source that has no 'uri' parameter");
        }

        switch(source["type"].toLowerCase()) {
            // 2D data from geoserver
            case "wms":
            case "geoserver": {
                this.addGeoserver(source, layer);
            }
            break;

            // Individual, non-tiled, KML files
            case "kml": {
                this.addKMLFile(source, layer);
            }
            break;

            // Individual, non-tiled, glTF/glB files
            case "glb":
            case "gltf": {
                this.addGLTFFile(source, layer);
            }
            break;

            // 3D tiles
            case "tile":
            case "tiles": {
                this.addTileset(source, layer);
            }
            break;

            // Anything else
            default: {
                console.warn("Unknown type '" + source["type"] + "', skipping this data source.");
            }
            break;
        }

    }

    /**
     * Adds an individual KML file to the map.
     * 
     * @param source JSON definition of source data.
     * @param layerID ID of layer upon the map.
     */
    private addKMLFile(source: Object, layer: DataLayer) {
        let sourceKML = Cesium.KmlDataSource.load(source["uri"]);
        sourceKML["show"] = layer.definition["visibility"] == undefined || layer.definition["visibility"] === "visible";

        // TODO: Investigate if camera and canvas options are actually required here.
        MapHandler.MAP.dataSources.add(
            sourceKML,
            {
                camera: MapHandler.MAP.camera,
                canvas: MapHandler.MAP.canvas
            }
        );
        console.info("Added KML source to map with layer ID: "+ layer.id);

        // Cache knowledge of this source, keyed by layer id
        if(MapHandler_Cesium.DATA_SOURCES[layer.id] === null || MapHandler_Cesium.DATA_SOURCES[layer.id] === undefined) {
            MapHandler_Cesium.DATA_SOURCES[layer.id] = [];
        }
        MapHandler_Cesium.DATA_SOURCES[layer.id].push(sourceKML);
    }

    /**
     * Adds an individual, non-tiled, glTF/glB file to the map.
     * 
     * @param source JSON definition of source data. 
     * @param layerID ID of layer upon the map.
     */
    private addGLTFFile(source: Object,  layer: DataLayer) {
        // Check the position
        let position = source["position"];
        if(position === null || position === undefined) {
            console.error("Cannot plot a glTF/glB data source that has no 'position' parameter");
        }

        // Check the orientation
        let orientation = [0, 0, 0];
        if(source.hasOwnProperty("orientation")) {
            orientation = source["orientation"];
        }

        // Generate final position
        let finalPosition = Cesium.Cartesian3.fromDegrees(position[0], position[1])

        // Generate final orientation
        let finalOrientation = Cesium.Transforms.headingPitchRollQuaternion(
            finalPosition,
            new Cesium.HeadingPitchRoll(orientation[0], orientation[1], orientation[2])
        );

        // Define the entity before adding to the map
        let sourceEntity = {
            position: finalPosition,
            orientation: finalOrientation,
            model: {
                uri: source["uri"],
                scale: source.hasOwnProperty("scale") ? source["scale"] : 1.0
            },
            show: layer.definition["visibility"] == undefined || layer.definition["visibility"] === "visible"
        };

        MapHandler.MAP.entities.add(sourceEntity);
        console.info("Added glTF/glB source to map with layer ID: "+ layer.id);

        // Cache knowledge of this source, keyed by layer id
        if(MapHandler_Cesium.DATA_SOURCES[layer.id] === null || MapHandler_Cesium.DATA_SOURCES[layer.id] === undefined) {
            MapHandler_Cesium.DATA_SOURCES[layer.id] = [];
        }
        MapHandler_Cesium.DATA_SOURCES[layer.id].push(sourceEntity);
    }

    /**
     * Adds a 3D tileset to the map.
     * 
     * @param source JSON definition of source data. 
     * @param layerID ID of layer upon the map.
     */
    private addTileset(source: Object,  layer: DataLayer) {
        // Check the position (if set)
        let position = source["position"];
        if(position !== null && position !== undefined) {
            let centerCartesian = Cesium.Cartesian3.fromDegrees(position[0], position[1], position[2]);
            position = Cesium.Transforms.eastNorthUpToFixedFrame(centerCartesian);
        }

        // Define tileset options
        let options = {
            url: source["uri"],
            show: layer.definition["visibility"] == undefined || layer.definition["visibility"] === "visible"
        };

        if(position !== null && position !== undefined) {
            options["modelMatrix"] = position;
        }

        let tileset = new Cesium.Cesium3DTileset(options);

        // Add the tileset to the map
        MapHandler.MAP.scene.primitives.add(tileset);
        console.info("Added 3D tileset source to map with layer ID: "+ layer.id);

        // Cache knowledge of this source, keyed by layer id
        if(MapHandler_Cesium.DATA_SOURCES[layer.id] === null || MapHandler_Cesium.DATA_SOURCES[layer.id] === undefined) {
            MapHandler_Cesium.DATA_SOURCES[layer.id] = [];
        }
        MapHandler_Cesium.DATA_SOURCES[layer.id].push(tileset);
    }

    /**
     * 
     * @param url 
     * @param layerID 
     */
    private addGeoserver(source: Object, layer: DataLayer) {
        // Check the geoserver layer name
        let wmsLayer = source["wmsLayer"];
        if(wmsLayer === null || wmsLayer === undefined) {
            console.error("Cannot plot a WMS data source that has no 'wmsLayer' parameter");
        }

        let provider = new Cesium.WebMapServiceImageryProvider({
            url: source["uri"],
            layers: wmsLayer,
            parameters: {
                transparent: source.hasOwnProperty("transparency") ? source["transparency"] : false,
                format: source.hasOwnProperty("format") ? source["format"] : "image/png"
            },
            credit: layer.id,
        });

        let layers = MapHandler.MAP.imageryLayers;
        layers.addImageryProvider(provider);

        // Now that it's added, we can hide it (unfortunatly there's no constructor option for this)
        for(let i = 0; i < layers.length; i++) {
            if(layers.get(i).imageryProvider === provider) {
                layers.get(i).show = layer.definition["visibility"] == undefined || layer.definition["visibility"] === "visible"
            }
        }

        // Cache knowledge of this source, keyed by layer id
        if(MapHandler_Cesium.DATA_SOURCES[layer.id] === null || MapHandler_Cesium.DATA_SOURCES[layer.id] === undefined) {
            MapHandler_Cesium.DATA_SOURCES[layer.id] = [];
        }
        MapHandler_Cesium.DATA_SOURCES[layer.id].push(provider);
    }

}