-- 3D City Database - The Open Source CityGML Database
-- https://www.3dcitydb.org/
--
-- Copyright 2013 - 2021
-- Chair of Geoinformatics
-- Technical University of Munich, Germany
-- https://www.lrg.tum.de/gis/
--
-- The 3D City Database is jointly developed with the following
-- cooperation partners:
--
-- Virtual City Systems, Berlin <https://vc.systems/>
-- M.O.S.S. Computer Grafik Systeme GmbH, Taufkirchen <http://www.moss.de/>
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--     http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

SET client_min_messages TO WARNING;

--//DROP FOREIGN KEYS

ALTER TABLE public.ADDRESS_TO_BUILDING DROP CONSTRAINT ADDRESS_TO_BUILDING_FK;
ALTER TABLE public.ADDRESS_TO_BUILDING DROP CONSTRAINT ADDRESS_TO_BUILDING_ADDRESS_FK;

ALTER TABLE public.APPEARANCE DROP CONSTRAINT APPEARANCE_CITYMODEL_FK;
ALTER TABLE public.APPEARANCE DROP CONSTRAINT APPEARANCE_CITYOBJECT_FK;

ALTER TABLE public.APPEAR_TO_SURFACE_DATA DROP CONSTRAINT APPEAR_TO_SURFACE_DATA_FK1;
ALTER TABLE public.APPEAR_TO_SURFACE_DATA DROP CONSTRAINT APPEAR_TO_SURFACE_DATA_FK;

ALTER TABLE public.BREAKLINE_RELIEF DROP CONSTRAINT BREAKLINE_RELIEF_FK;

ALTER TABLE public.BUILDING DROP CONSTRAINT BUILDING_SURFACE_GEOMETRY_FK;
ALTER TABLE public.BUILDING DROP CONSTRAINT BUILDING_SURFACE_GEOMETRY_FK3;
ALTER TABLE public.BUILDING DROP CONSTRAINT BUILDING_CITYOBJECT_FK;
ALTER TABLE public.BUILDING DROP CONSTRAINT BUILDING_SURFACE_GEOMETRY_FK1;
ALTER TABLE public.BUILDING DROP CONSTRAINT BUILDING_SURFACE_GEOMETRY_FK2;
ALTER TABLE public.BUILDING DROP CONSTRAINT BUILDING_BUILDING_FK;
ALTER TABLE public.BUILDING DROP CONSTRAINT BUILDING_BUILDING_FK1;

ALTER TABLE public.BUILDING_FURNITURE DROP CONSTRAINT BUILDING_FURNITURE_FK1;
ALTER TABLE public.BUILDING_FURNITURE DROP CONSTRAINT BUILDING_FURNITURE_FK2;
ALTER TABLE public.BUILDING_FURNITURE DROP CONSTRAINT BUILDING_FURNITURE_FK;
ALTER TABLE public.BUILDING_FURNITURE DROP CONSTRAINT BUILDING_FURNITURE_ROOM_FK;

ALTER TABLE public.BUILDING_INSTALLATION DROP CONSTRAINT BUILDING_INSTALLATION_FK3;
ALTER TABLE public.BUILDING_INSTALLATION DROP CONSTRAINT BUILDING_INSTALLATION_FK;
ALTER TABLE public.BUILDING_INSTALLATION DROP CONSTRAINT BUILDING_INSTALLATION_ROOM_FK;
ALTER TABLE public.BUILDING_INSTALLATION DROP CONSTRAINT BUILDING_INSTALLATION_FK4;
ALTER TABLE public.BUILDING_INSTALLATION DROP CONSTRAINT BUILDING_INSTALLATION_FK1;
ALTER TABLE public.BUILDING_INSTALLATION DROP CONSTRAINT BUILDING_INSTALLATION_FK2;

ALTER TABLE public.CITYOBJECT DROP CONSTRAINT CITYOBJECT_OBJECTCLASS_FK;

ALTER TABLE public.CITYOBJECTGROUP DROP CONSTRAINT CITYOBJECT_GROUP_FK;
ALTER TABLE public.CITYOBJECTGROUP DROP CONSTRAINT CITYOBJECTGROUP_CITYOBJECT_FK;
ALTER TABLE public.CITYOBJECTGROUP DROP CONSTRAINT CITYOBJECTGROUP_CITYOBJECT_FK1;

ALTER TABLE public.CITYOBJECT_GENERICATTRIB DROP CONSTRAINT CITYOBJECT_GENERICATTRIB_FK;
ALTER TABLE public.CITYOBJECT_GENERICATTRIB DROP CONSTRAINT CITYOBJECT_GENERICATTRIB_FK1;

ALTER TABLE public.CITYOBJECT_MEMBER DROP CONSTRAINT CITYOBJECT_MEMBER_CITYMODEL_FK;
ALTER TABLE public.CITYOBJECT_MEMBER DROP CONSTRAINT CITYOBJECT_MEMBER_FK;

ALTER TABLE public.CITY_FURNITURE DROP CONSTRAINT CITY_FURNITURE_FK;
ALTER TABLE public.CITY_FURNITURE DROP CONSTRAINT CITY_FURNITURE_FK1;
ALTER TABLE public.CITY_FURNITURE DROP CONSTRAINT CITY_FURNITURE_FK2;
ALTER TABLE public.CITY_FURNITURE DROP CONSTRAINT CITY_FURNITURE_FK3;
ALTER TABLE public.CITY_FURNITURE DROP CONSTRAINT CITY_FURNITURE_FK4;
ALTER TABLE public.CITY_FURNITURE DROP CONSTRAINT CITY_FURNITURE_FK5;
ALTER TABLE public.CITY_FURNITURE DROP CONSTRAINT CITY_FURNITURE_FK6;
ALTER TABLE public.CITY_FURNITURE DROP CONSTRAINT CITY_FURNITURE_FK7;
ALTER TABLE public.CITY_FURNITURE DROP CONSTRAINT CITY_FURNITURE_CITYOBJECT_FK;

ALTER TABLE public.EXTERNAL_REFERENCE DROP CONSTRAINT EXTERNAL_REFERENCE_FK;

ALTER TABLE public.GENERALIZATION DROP CONSTRAINT GENERALIZATION_FK1;
ALTER TABLE public.GENERALIZATION DROP CONSTRAINT GENERALIZATION_FK;

ALTER TABLE public.GENERIC_CITYOBJECT DROP CONSTRAINT GENERIC_CITYOBJECT_FK;
ALTER TABLE public.GENERIC_CITYOBJECT DROP CONSTRAINT GENERIC_CITYOBJECT_FK1;
ALTER TABLE public.GENERIC_CITYOBJECT DROP CONSTRAINT GENERIC_CITYOBJECT_FK2;
ALTER TABLE public.GENERIC_CITYOBJECT DROP CONSTRAINT GENERIC_CITYOBJECT_FK3;
ALTER TABLE public.GENERIC_CITYOBJECT DROP CONSTRAINT GENERIC_CITYOBJECT_FK4;
ALTER TABLE public.GENERIC_CITYOBJECT DROP CONSTRAINT GENERIC_CITYOBJECT_FK5;
ALTER TABLE public.GENERIC_CITYOBJECT DROP CONSTRAINT GENERIC_CITYOBJECT_FK6;
ALTER TABLE public.GENERIC_CITYOBJECT DROP CONSTRAINT GENERIC_CITYOBJECT_FK7;
ALTER TABLE public.GENERIC_CITYOBJECT DROP CONSTRAINT GENERIC_CITYOBJECT_FK8;
ALTER TABLE public.GENERIC_CITYOBJECT DROP CONSTRAINT GENERIC_CITYOBJECT_FK9;
ALTER TABLE public.GENERIC_CITYOBJECT DROP CONSTRAINT GENERIC_CITYOBJECT_FK10;

ALTER TABLE public.GROUP_TO_CITYOBJECT DROP CONSTRAINT GROUP_TO_CITYOBJECT_FK;
ALTER TABLE public.GROUP_TO_CITYOBJECT DROP CONSTRAINT GROUP_TO_CITYOBJECT_FK1;

ALTER TABLE public.IMPLICIT_GEOMETRY DROP CONSTRAINT IMPLICIT_GEOMETRY_FK;

ALTER TABLE public.LAND_USE DROP CONSTRAINT LAND_USE_CITYOBJECT_FK;
ALTER TABLE public.LAND_USE DROP CONSTRAINT LAND_USE_SURFACE_GEOMETRY_FK;
ALTER TABLE public.LAND_USE DROP CONSTRAINT LAND_USE_SURFACE_GEOMETRY_FK1;
ALTER TABLE public.LAND_USE DROP CONSTRAINT LAND_USE_SURFACE_GEOMETRY_FK2;
ALTER TABLE public.LAND_USE DROP CONSTRAINT LAND_USE_SURFACE_GEOMETRY_FK3;
ALTER TABLE public.LAND_USE DROP CONSTRAINT LAND_USE_SURFACE_GEOMETRY_FK4;

ALTER TABLE public.MASSPOINT_RELIEF DROP CONSTRAINT MASSPOINT_RELIEF_FK;

ALTER TABLE public.OBJECTCLASS DROP CONSTRAINT OBJECTCLASS_OBJECTCLASS_FK;

ALTER TABLE public.OPENING DROP CONSTRAINT OPENING_SURFACE_GEOMETRY_FK1;
ALTER TABLE public.OPENING DROP CONSTRAINT OPENING_CITYOBJECT_FK;
ALTER TABLE public.OPENING DROP CONSTRAINT OPENING_SURFACE_GEOMETRY_FK;
ALTER TABLE public.OPENING DROP CONSTRAINT OPENING_ADDRESS_FK;

ALTER TABLE public.OPENING_TO_THEM_SURFACE DROP CONSTRAINT OPENING_TO_THEMATIC_SURFACE_FK;
ALTER TABLE public.OPENING_TO_THEM_SURFACE DROP CONSTRAINT OPENING_TO_THEMATIC_SURFAC_FK1;

ALTER TABLE public.PLANT_COVER DROP CONSTRAINT PLANT_COVER_FK;
ALTER TABLE public.PLANT_COVER DROP CONSTRAINT PLANT_COVER_FK1;
ALTER TABLE public.PLANT_COVER DROP CONSTRAINT PLANT_COVER_FK3;
ALTER TABLE public.PLANT_COVER DROP CONSTRAINT PLANT_COVER_FK2;
ALTER TABLE public.PLANT_COVER DROP CONSTRAINT PLANT_COVER_CITYOBJECT_FK;

ALTER TABLE public.RASTER_RELIEF DROP CONSTRAINT RASTER_RELIEF_FK;

ALTER TABLE public.RELIEF_COMPONENT DROP CONSTRAINT RELIEF_COMPONENT_CITYOBJECT_FK;

ALTER TABLE public.RELIEF_FEATURE DROP CONSTRAINT RELIEF_FEATURE_CITYOBJECT_FK;

ALTER TABLE public.RELIEF_FEAT_TO_REL_COMP DROP CONSTRAINT RELIEF_FEAT_TO_REL_COMP_FK;
ALTER TABLE public.RELIEF_FEAT_TO_REL_COMP DROP CONSTRAINT RELIEF_FEAT_TO_REL_COMP_FK1;

ALTER TABLE public.ROOM DROP CONSTRAINT ROOM_BUILDING_FK;
ALTER TABLE public.ROOM DROP CONSTRAINT ROOM_SURFACE_GEOMETRY_FK;
ALTER TABLE public.ROOM DROP CONSTRAINT ROOM_CITYOBJECT_FK;

ALTER TABLE public.SOLITARY_VEGETAT_OBJECT DROP CONSTRAINT SOLITARY_VEGETAT_OBJECT_FK;
ALTER TABLE public.SOLITARY_VEGETAT_OBJECT DROP CONSTRAINT SOLITARY_VEGETAT_OBJECT_FK1;
ALTER TABLE public.SOLITARY_VEGETAT_OBJECT DROP CONSTRAINT SOLITARY_VEGETAT_OBJECT_FK2;
ALTER TABLE public.SOLITARY_VEGETAT_OBJECT DROP CONSTRAINT SOLITARY_VEGETAT_OBJECT_FK3;
ALTER TABLE public.SOLITARY_VEGETAT_OBJECT DROP CONSTRAINT SOLITARY_VEGETAT_OBJECT_FK4;
ALTER TABLE public.SOLITARY_VEGETAT_OBJECT DROP CONSTRAINT SOLITARY_VEGETAT_OBJECT_FK5;
ALTER TABLE public.SOLITARY_VEGETAT_OBJECT DROP CONSTRAINT SOLITARY_VEGETAT_OBJECT_FK6;
ALTER TABLE public.SOLITARY_VEGETAT_OBJECT DROP CONSTRAINT SOLITARY_VEGETAT_OBJECT_FK7;
ALTER TABLE public.SOLITARY_VEGETAT_OBJECT DROP CONSTRAINT SOLITARY_VEGETAT_OBJECT_FK8;

ALTER TABLE public.SURFACE_GEOMETRY DROP CONSTRAINT SURFACE_GEOMETRY_FK;
ALTER TABLE public.SURFACE_GEOMETRY DROP CONSTRAINT SURFACE_GEOMETRY_FK1;

ALTER TABLE public.TEXTUREPARAM DROP CONSTRAINT TEXTUREPARAM_SURFACE_GEOM_FK;
ALTER TABLE public.TEXTUREPARAM DROP CONSTRAINT TEXTUREPARAM_SURFACE_DATA_FK;

ALTER TABLE public.THEMATIC_SURFACE DROP CONSTRAINT THEMATIC_SURFACE_ROOM_FK;
ALTER TABLE public.THEMATIC_SURFACE DROP CONSTRAINT THEMATIC_SURFACE_BUILDING_FK;
ALTER TABLE public.THEMATIC_SURFACE DROP CONSTRAINT THEMATIC_SURFACE_FK;
ALTER TABLE public.THEMATIC_SURFACE DROP CONSTRAINT THEMATIC_SURFACE_CITYOBJECT_FK;
ALTER TABLE public.THEMATIC_SURFACE DROP CONSTRAINT THEMATIC_SURFACE_FK2;
ALTER TABLE public.THEMATIC_SURFACE DROP CONSTRAINT THEMATIC_SURFACE_FK1;

ALTER TABLE public.TIN_RELIEF DROP CONSTRAINT TIN_RELIEF_SURFACE_GEOMETRY_FK;
ALTER TABLE public.TIN_RELIEF DROP CONSTRAINT TIN_RELIEF_RELIEF_COMPONENT_FK;

ALTER TABLE public.TRAFFIC_AREA DROP CONSTRAINT TRAFFIC_AREA_CITYOBJECT_FK;
ALTER TABLE public.TRAFFIC_AREA DROP CONSTRAINT TRAFFIC_AREA_FK;
ALTER TABLE public.TRAFFIC_AREA DROP CONSTRAINT TRAFFIC_AREA_FK1;
ALTER TABLE public.TRAFFIC_AREA DROP CONSTRAINT TRAFFIC_AREA_FK2;
ALTER TABLE public.TRAFFIC_AREA DROP CONSTRAINT TRAFFIC_AREA_FK3;

ALTER TABLE public.TRANSPORTATION_COMPLEX DROP CONSTRAINT TRANSPORTATION_COMPLEX_FK;
ALTER TABLE public.TRANSPORTATION_COMPLEX DROP CONSTRAINT TRANSPORTATION_COMPLEX_FK1;
ALTER TABLE public.TRANSPORTATION_COMPLEX DROP CONSTRAINT TRANSPORTATION_COMPLEX_FK2;
ALTER TABLE public.TRANSPORTATION_COMPLEX DROP CONSTRAINT TRANSPORTATION_COMPLEX_FK3;
ALTER TABLE public.TRANSPORTATION_COMPLEX DROP CONSTRAINT TRANSPORTATION_COMPLEX_FK4;

ALTER TABLE public.WATERBODY DROP CONSTRAINT WATERBODY_CITYOBJECT_FK;
ALTER TABLE public.WATERBODY DROP CONSTRAINT WATERBODY_SURFACE_GEOMETRY_FK1;
ALTER TABLE public.WATERBODY DROP CONSTRAINT WATERBODY_SURFACE_GEOMETRY_FK2;
ALTER TABLE public.WATERBODY DROP CONSTRAINT WATERBODY_SURFACE_GEOMETRY_FK3;
ALTER TABLE public.WATERBODY DROP CONSTRAINT WATERBODY_SURFACE_GEOMETRY_FK4;
ALTER TABLE public.WATERBODY DROP CONSTRAINT WATERBODY_SURFACE_GEOMETRY_FK5;
ALTER TABLE public.WATERBODY DROP CONSTRAINT WATERBODY_SURFACE_GEOMETRY_FK;

ALTER TABLE public.WATERBOD_TO_WATERBND_SRF DROP CONSTRAINT WATERBOD_TO_WATERBND_FK;
ALTER TABLE public.WATERBOD_TO_WATERBND_SRF DROP CONSTRAINT WATERBOD_TO_WATERBND_FK1;

ALTER TABLE public.WATERBOUNDARY_SURFACE DROP CONSTRAINT WATERBOUNDARY_SRF_CITYOBJ_FK;
ALTER TABLE public.WATERBOUNDARY_SURFACE DROP CONSTRAINT WATERBOUNDARY_SURFACE_FK;
ALTER TABLE public.WATERBOUNDARY_SURFACE DROP CONSTRAINT WATERBOUNDARY_SURFACE_FK1;
ALTER TABLE public.WATERBOUNDARY_SURFACE DROP CONSTRAINT WATERBOUNDARY_SURFACE_FK2;

--//DROP TABLES

DROP TABLE public.ADDRESS CASCADE;
DROP TABLE public.ADDRESS_TO_BUILDING CASCADE;
DROP TABLE public.APPEARANCE CASCADE;
DROP TABLE public.APPEAR_TO_SURFACE_DATA CASCADE;
DROP TABLE public.BREAKLINE_RELIEF CASCADE;
DROP TABLE public.BUILDING CASCADE;
DROP TABLE public.BUILDING_FURNITURE CASCADE;
DROP TABLE public.BUILDING_INSTALLATION CASCADE;
DROP TABLE public.CITYMODEL CASCADE;
DROP TABLE public.CITYOBJECT CASCADE;
DROP TABLE public.CITYOBJECTGROUP CASCADE;
DROP TABLE public.CITYOBJECT_GENERICATTRIB CASCADE;
DROP TABLE public.CITYOBJECT_MEMBER CASCADE;
DROP TABLE public.CITY_FURNITURE CASCADE;
DROP TABLE public.DATABASE_SRS CASCADE;
DROP TABLE public.EXTERNAL_REFERENCE CASCADE;
DROP TABLE public.GENERALIZATION CASCADE;
DROP TABLE public.GENERIC_CITYOBJECT CASCADE;
DROP TABLE public.GROUP_TO_CITYOBJECT CASCADE;
DROP TABLE public.IMPLICIT_GEOMETRY CASCADE;
DROP TABLE public.LAND_USE CASCADE;
DROP TABLE public.MASSPOINT_RELIEF CASCADE;
DROP TABLE public.OBJECTCLASS CASCADE;
DROP TABLE public.OPENING CASCADE;
DROP TABLE public.OPENING_TO_THEM_SURFACE CASCADE;
DROP TABLE public.ORTHOPHOTO CASCADE;
DROP TABLE public.PLANT_COVER CASCADE;
DROP TABLE public.RASTER_RELIEF CASCADE;
DROP TABLE public.RELIEF CASCADE;
DROP TABLE public.RELIEF_COMPONENT CASCADE;
DROP TABLE public.RELIEF_FEATURE CASCADE;
DROP TABLE public.RELIEF_FEAT_TO_REL_COMP CASCADE;
DROP TABLE public.ROOM CASCADE;
DROP TABLE public.SOLITARY_VEGETAT_OBJECT CASCADE;
DROP TABLE public.SURFACE_DATA CASCADE;
DROP TABLE public.SURFACE_GEOMETRY CASCADE;
DROP TABLE public.TEXTUREPARAM CASCADE;
DROP TABLE public.THEMATIC_SURFACE CASCADE;
DROP TABLE public.TIN_RELIEF CASCADE;
DROP TABLE public.TRAFFIC_AREA CASCADE;
DROP TABLE public.TRANSPORTATION_COMPLEX CASCADE;
DROP TABLE public.WATERBODY CASCADE;
DROP TABLE public.WATERBOD_TO_WATERBND_SRF CASCADE;
DROP TABLE public.WATERBOUNDARY_SURFACE CASCADE;

--//DROP SCHEMA

DROP SCHEMA GEODB_PKG CASCADE;

\echo
\echo '3DCityDB v2 schema successfully removed!'