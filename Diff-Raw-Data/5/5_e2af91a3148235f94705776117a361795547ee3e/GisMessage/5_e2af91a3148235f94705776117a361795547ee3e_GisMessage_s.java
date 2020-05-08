 /**
  * ******************************************************************************************
  * Copyright (C) 2012 - Food and Agriculture Organization of the United Nations (FAO).
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification,
  * are permitted provided that the following conditions are met:
  *
  *    1. Redistributions of source code must retain the above copyright notice,this list
  *       of conditions and the following disclaimer.
  *    2. Redistributions in binary form must reproduce the above copyright notice,this list
  *       of conditions and the following disclaimer in the documentation and/or other
  *       materials provided with the distribution.
  *    3. Neither the name of FAO nor the names of its contributors may be used to endorse or
  *       promote products derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
  * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,PROCUREMENT
  * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,STRICT LIABILITY,OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
  * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * *********************************************************************************************
  */
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.sola.common.messaging;
 
 /**
  *
  * @author soladev
  */
 public class GisMessage {
 
     public static final String MSG_PREFIX = "gis";
     
     // Message groups
     private static final String TEST = MSG_PREFIX + "test";
     private static final String GENERAL = MSG_PREFIX + "gnrl";
     private static final String INFOTOOL = MSG_PREFIX + "infotool";
     private static final String LOCATE = MSG_PREFIX + "locate";
     private static final String GEOTOOLS = MSG_PREFIX + "geotools5";
     private static final String CADASTRE_CHANGE = MSG_PREFIX + "_cadastre_change_";
     private static final String CADASTRE_REDEFINITION = MSG_PREFIX + "_cadastre_redefinition_";
     private static final String CADASTRE_BOUNDARY = MSG_PREFIX + "_cadastre_boundary_";
     private static final String LEFT_PANEL = MSG_PREFIX + "_left_panel_";
     private static final String IMPORT_FILE = MSG_PREFIX + "_import_file_";
     private static final String PRINT_PUBLIC_DISPLAY = MSG_PREFIX + "_public_display_";
     private static final String SPATIAL_UNIT_GROUP = MSG_PREFIX + "_spatial_unit_group_";
    // General Messages
     /** gisgnrl001 - Error starting the service */
     public static final String GENERAL_ERROR_STARTING_SERVICE = GENERAL + "001";
     /** gisgnrl002 - CollectionFeatureSource is an inmemory wrapper */
     public static final String GENERAL_EXCEPTION_COLLFEATSOURCE = GENERAL + "002";
     /** gisgnrl003 - Filter not found */
     public static final String GENERAL_EXCEPTION_FILTER_NOTFOUND = GENERAL + "003";
     /** gisgnrl004 - Type of filter not supported */
     public static final String GENERAL_EXCEPTION_TYPE_NOTSUPPORTED = GENERAL + "004";
     /** gisgnrl005 - Under Construction */
     public static final String GENERAL_UNDER_CONSTRUCTION = GENERAL + "005";
      /** gisgnrl006 -  ControBundle error setup*/
     public static final String GENERAL_CONTROLBUNDLE_ERROR = GENERAL + "006";
     /** gisgnrl007 -  gis.controlbundleforapplication.error.setup ControBundle for Application error setup*/
     public static final String GENERAL_CONTROLBUNDLEAPP_ERROR = GENERAL + "007";
    
     /** gisgnrl008 - Layer: %s. No features could be retrieved. Source might be unavailable. */
     public static final String GENERAL_RETRIEVE_FEATURES_ERROR = GENERAL + "008";
 
     // Info Tool    
    /** gisinfotool001 - Click to get information */
     public static final String INFOTOOL_CLICK = INFOTOOL + "001";
     
     
    // Locate Remove App    
     /** gislocate001 - Click to remove location */
     public static final String LOCATE_REMOVE = LOCATE + "001";
     /** gislocate002 - Error in setting location */
     public static final String LOCATE_ERROR_SETUP = LOCATE + "002";
      /** gislocate003 - Error in setting location  Error in setting application location*/
     public static final String LOCATE_ERROR_APPLICATION = LOCATE + "003";
 
     // GEOTOOLS
     public static final String ADDING_FEATURE_ERROR = GEOTOOLS + "01";
     public static final String MAPCONTROL_MAPCONTEXT_WITHOUT_SRID_ERROR = GEOTOOLS + "02";
     public static final String DRAWINGTOOL_GEOMETRY_NOT_VALID_ERROR = GEOTOOLS + "03";
     public static final String LAYERGRAPHICS_STARTUP_ERROR = GEOTOOLS + "04";
     public static final String SHAPEFILELAYER_FILE_NOT_FOUND_ERROR = GEOTOOLS + "05";
     public static final String REMOVE_ALL_FEATURES_ERROR = GEOTOOLS + "06";
     public static final String LAYER_NOT_ADDED_ERROR = GEOTOOLS + "07";
     public static final String WMSLAYER_NOT_INITIALIZED_ERROR = GEOTOOLS + "08";
     public static final String WMSLAYER_LAYER_NOT_FOUND_ERROR = GEOTOOLS + "09";
     public static final String UTILITIES_SLD_DOESNOT_EXIST_ERROR = GEOTOOLS + "10";
     public static final String UTILITIES_SLD_LOADING_ERROR = GEOTOOLS + "11";
     public static final String UTILITIES_COORDSYS_COULDNOT_BE_CREATED_ERROR = GEOTOOLS + "12";
     public static final String DRAWINGTOOL_NOT_ENOUGH_POINTS_INFORMATIVE = GEOTOOLS + "13";
     public static final String PARCEL_TARGET_NOT_FOUND = GEOTOOLS + "14";
     public static final String PARCEL_ERROR_ADDING_PARCEL  = GEOTOOLS + "15";
     public static final String GEOTOOL_ADDING_FEATURE_ERROR = GEOTOOLS + "17";
     public static final String GEOTOOL_MAPCONTEXT_WITHOUT_SRID_ERROR = GEOTOOLS + "18";
     public static final String GEOTOOL_GEOMETRY_NOT_VALID_ERROR = GEOTOOLS + "19";
     public static final String GEOTOOL_LAYERGRAPHICS_STARTUP_ERROR    = GEOTOOLS + "20";
     public static final String GEOTOOL_FILE_NOT_FOUND_ERROR    = GEOTOOLS + "21";
     public static final String GEOTOOL_REMOVE_ALL_FEATURES_ERROR   = GEOTOOLS + "22";
     public static final String GEOTOOL_LAYER_NOT_ADDED  = GEOTOOLS + "23";
     public static final String GEOTOOL_WMSLAYER_NOT_INITIALIZED_ERROR  = GEOTOOLS + "24";
     public static final String GEOTOOL_WMSLAYER_LAYER_NOT_FOUND_ERROR = GEOTOOLS + "25";
     public static final String GEOTOOL_SLD_DOESNOT_EXIST_ERROR  = GEOTOOLS + "26";
     public static final String GEOTOOL_SLD_LOADING_ERROR  = GEOTOOLS + "27";
     public static final String GEOTOOL_COORDSYS_COULDNOT_BE_CREATED_ERROR  = GEOTOOLS + "28";
     public static final String GEOTOOL_NOT_ENOUGH_POINTS_INFORMATIVE  = GEOTOOLS + "29";
     public static final String GEOTOOL_TOOLTIP_FULL_EXTENT  = GEOTOOLS + "30";
     public static final String GEOTOOL_TOOLTIP_ZOOM_OUT  = GEOTOOLS + "31";
     public static final String GEOTOOL_TOOLTIP_ZOOM_IN  = GEOTOOLS + "32";
     public static final String GEOTOOL_TOOLTIP_PAN  = GEOTOOLS + "33";
     public static final String PRINT  = GEOTOOLS + "34";
     public static final String PRINT_LAYOUT_NOT_SELECTED = GEOTOOLS + "35";
     public static final String PRINT_SCALE_NOT_CORRECT = GEOTOOLS + "36";
     public static final String ADD_DIRECT_IMAGE_TOOLTIP = GEOTOOLS + "37";
     public static final String ADD_DIRECT_IMAGE_ADD_FIRST_POINT = GEOTOOLS + "38";
     public static final String ADD_DIRECT_IMAGE_ADD_SECOND_POINT = GEOTOOLS + "39";
     public static final String ADD_DIRECT_IMAGE_LOAD_IMAGE_ERROR = GEOTOOLS + "40";
     public static final String ADD_DIRECT_IMAGE_DEFINE_POINT_ERROR = GEOTOOLS + "41";
     public static final String REMOVE_DIRECT_IMAGE_TOOLTIP = GEOTOOLS + "42";
     public static final String ADD_DIRECT_IMAGE_DEFINE_POINT_IN_IMAGE_ERROR = GEOTOOLS + "43";
     public static final String ADD_DIRECT_IMAGE_DEFINE_ORIENTATION_POINT_1_IN_IMAGE= GEOTOOLS + "44";
     public static final String ADD_DIRECT_IMAGE_DEFINE_ORIENTATION_POINT_2_IN_IMAGE= GEOTOOLS + "45";
     public static final String ADD_DIRECT_IMAGE_LOAD_IMAGE= GEOTOOLS + "46";
     public static final String PRINT_LAYOUT_GENERATION_ERROR = GEOTOOLS + "47";
     /** gisgeotools548 - Invalid scale */
     public static final String MAP_SCALE_ERROR = GEOTOOLS + "48";
        /** gisgeotools549 - < 0.01 */
     public static final String MIN_DISPLAY_SCALE = GEOTOOLS + "49";
     /** gisgeotools550 - Scale: */
     public static final String SCALE_LABEL = GEOTOOLS + "50";
     /** gisgeotools551 - The file {0} cannot be opened automatically */
     public static final String FAILED_OPEN_FILE = GEOTOOLS + "51";
      /** gisgeotools552 - Export selected feature(s) to KML. */
     public static final String KML_EXPORT_TOOLTIP = GEOTOOLS + "52";
      /** gisgeotools553 - An error occurred while attempting to export the selected feature(s)  */
     public static final String KML_EXPORT_ERROR = GEOTOOLS + "53";
      /** gisgeotools554 - Map feature(s) have been successfully exported to %s" */
     public static final String KML_EXPORT_FILE_LOCATION = GEOTOOLS + "54";
      /** gisgeotools555 - No features are selected for export  */
     public static final String KML_EXPORT_NO_FEATURE_SELECTED = GEOTOOLS + "55";
     
      /** gisgeotools556 - WMS Layer is not rendered. Most probably the wms server is not available. Switch the layer off in order not to get this message. */
     public static final String WMSLAYER_LAYER_RENDER_ERROR = GEOTOOLS + "56";
     
     
     //CADASTRE CHANGE
     /** The point has to fall on an current node or to a line*/
     public static final String CADASTRE_CHANGE_HAS_TO_SNAP  = CADASTRE_CHANGE + "001";
     /** The points of the new parcel has to fall in the points that are marked boundary.*/
     public static final String CADASTRE_CHANGE_NEW_CO_MUST_SNAP  = CADASTRE_CHANGE + "002";
     /** The official area is wrong.*/
     public static final String CADASTRE_CHANGE_CO_OFFICIAL_AREA_WRONG  = CADASTRE_CHANGE + "003";
     /** Error adding point.*/
     public static final String CADASTRE_CHANGE_ERROR_ADDING_POINT  = CADASTRE_CHANGE + "004";
     /** Error removing point: found in parcel.*/
     public static final String CADASTRE_CHANGE_ERROR_POINT_FOUND_IN_PARCEL  = CADASTRE_CHANGE + "005";
     /** Error adding cadastre object.*/
     public static final String CADASTRE_CHANGE_ERROR_ADD_CO  = CADASTRE_CHANGE + "006";
     /** Error in cadastre change setup.*/
      public static final String CADASTRE_CHANGE_ERROR_SETUP  = CADASTRE_CHANGE + "007";
     /** Error while adding points in start of the cadastre change.*/
      public static final String CADASTRE_CHANGE_ERROR_ADDINGPOINT_IN_START  = CADASTRE_CHANGE + "008";
     /** Error while adding new cadastre objects in start of the cadastre change.*/
      public static final String CADASTRE_CHANGE_ERROR_ADDINGNEWCADASTREOBJECT_IN_START  = 
              CADASTRE_CHANGE + "009";
     /** Error while adding target cadastre objects in start of the cadastre change.*/
      public static final String CADASTRE_CHANGE_ERROR_ADDTARGET_IN_START  = CADASTRE_CHANGE + "010";
     /** The cadastre change is saved successfully.*/
     public static final String CADASTRE_CHANGE_SAVED_SUCCESSFULLY  = CADASTRE_CHANGE + "011";
     /**New cadastral objects form*/
     public static final String CADASTRE_OBJ_LIST_SHOW  = CADASTRE_CHANGE + "012";
     /**New Parcel tooltip*/
     public static final String CADASTRE_CHANGE_TOOLTIP_NEW_PARCEL  = CADASTRE_CHANGE + "013";
     /**New/ modify survey points*/
     public static final String CADASTRE_CHANGE_TOOLTIP_NEW_SURVEYPOINT  = CADASTRE_CHANGE + "014";
    /**Select / unselect target parcel*/
     public static final String CADASTRE_CHANGE_TOOLTIP_SELECT_PARCEL  = CADASTRE_CHANGE + "015";
    /**gis.baunit.cadastreobjects.error.setup  Error in cadastre object setup*/
     public static final String CADASTRE_OBJBAUNIT_SETUP_ERROR = CADASTRE_CHANGE + "016";
    /**X or Y are not valid*/
     public static final String CADASTRE_SURVEY_ADD_POINT = CADASTRE_CHANGE + "017";
     /*Click to remove location*/
     public static final String CADASTRE_TOOLTIP_REMOVE_LOCATION = CADASTRE_CHANGE + "018";
      /*Click to remove location*/
     public static final String CADASTRE_TOOLTIP_ADD_LOCATION = CADASTRE_CHANGE + "019";
       
     /**Select a node to change its coordinates*/
     public static final String CADASTRE_TOOLTIP_CHANGE_NODE  = CADASTRE_CHANGE + "020";
 
     /**Select a node to remove*/
     public static final String CADASTRE_TOOLTIP_REMOVE_NODE  = CADASTRE_CHANGE + "021";
 
     public static final String CADASTRE_CHANGE_POINTS_SHOW  = CADASTRE_CHANGE + "022";
 
     /** Error while creating a new instance of a spatial bean.*/
      public static final String CADASTRE_CHANGE_ERROR_INITIALIZE_NEW_OBJECT  = 
              CADASTRE_CHANGE + "023";
 
     /** Error while setting bean values.*/
      public static final String CADASTRE_CHANGE_ERROR_SET_BEAN_VALUES  = 
              CADASTRE_CHANGE + "024";
 
     /** Error while getting bean values.*/
      public static final String CADASTRE_CHANGE_ERROR_GET_BEAN_VALUES  = 
              CADASTRE_CHANGE + "025";
 
     /** Save changes */
      public static final String CADASTRE_CHANGE_TRANSACTION_SAVE  = 
              CADASTRE_CHANGE + "026";
 
     /** New cadastre objects */
      public static final String CADASTRE_CHANGE_FORM_NEW_OBJECTS_TITLE  = 
              CADASTRE_CHANGE + "027";
 
     /** Survey points */
      public static final String CADASTRE_CHANGE_FORM_SURVEYPOINT_TITLE  = 
              CADASTRE_CHANGE + "028";
 
      
      //Cadastre redefinition
     /**Coordinates entered are not valid.*/
     public static final String CADASTRE_REDEFINITION_COORDS_NOT_VALID  = CADASTRE_REDEFINITION + "001";
 
     /**An error occurred while adding a node. */
     public static final String CADASTRE_REDEFINITION_ADD_NODE_ERROR  = CADASTRE_REDEFINITION + "002";
     
     /**Node is related to more than 2 parcels.*/
     public static final String CADASTRE_REDEFINITION_NODE_HAS_MORE_THAN_ONE_CO  = CADASTRE_REDEFINITION + "003";
     
     /**Something went wrong while resetting the redefinition process.*/
     public static final String CADASTRE_REDEFINITION_RESET_ERROR  = CADASTRE_REDEFINITION + "004";
     
     /**Reset the cadastre redefinition process*/
     public static final String CADASTRE_REDEFINITION_RESET_TOOLTIP  = CADASTRE_REDEFINITION + "005";
 
     /**An error occurred while adding a target cadastre object. */
     public static final String CADASTRE_REDEFINITION_ADD_CO_ERROR  = CADASTRE_REDEFINITION + "006";
     
     /**Add a node.*/
     public static final String CADASTRE_TOOLTIP_ADD_NODE  = CADASTRE_REDEFINITION + "007";
 
     /**Error while adding a cadastre object target.*/
     public static final String CADASTRE_REDEFINITION_ADD_TARGET_ERROR  = 
             CADASTRE_REDEFINITION + "008";
 
     /**Error while identifying the point of the target boundary.*/
     public static final String CADASTRE_BOUNDARY_ADD_POINT_ERROR  = CADASTRE_BOUNDARY + "001";
     
     /**Modify the selected boundary*/
     public static final String CADASTRE_BOUNDARY_EDIT_TOOL_TOOLTIP  = CADASTRE_BOUNDARY + "002";
 
     /**Select a boundary to change*/
     public static final String CADASTRE_BOUNDARY_SELECT_TOOL_TOOLTIP  = CADASTRE_BOUNDARY + "003";
 
     /**Target layer is not defined.*/
     public static final String CADASTRE_BOUNDARY_TARGET_LAYER_NOT_DEFINED  = CADASTRE_BOUNDARY + "004";
     
     /**Error while adding the target boundary.*/
     public static final String CADASTRE_BOUNDARY_ADD_TARGET_BOUNDARY_ERROR = CADASTRE_BOUNDARY + "005";
 
     /**Error while clearing the selection of the boundary.*/
     public static final String CADASTRE_BOUNDARY_CLEAR_SELECTION_ERROR = CADASTRE_BOUNDARY + "006";
 
     /**New boundary must start and end in the points of the target boundary.*/
     public static final String CADASTRE_BOUNDARY_NEW_MUST_START_END_AS_TARGET = CADASTRE_BOUNDARY + "007";
     
     /**Select a point as the starting point of the changing boundary.*/
     public static final String CADASTRE_BOUNDARY_SELECT_FIRST_BOUNDARY_POINT = CADASTRE_BOUNDARY + "008";
 
     /**Select a point as the ending point of the changing boundary.*/
     public static final String CADASTRE_BOUNDARY_SELECT_SECOND_BOUNDARY_POINT = CADASTRE_BOUNDARY + "009";
 
     /**The end point must not be the same with the end point.*/
     public static final String CADASTRE_BOUNDARY_START_END_POINT_SAME = CADASTRE_BOUNDARY + "010";
     // Messages related with the left panel
     /* Layers */
     public static final String LEFT_PANEL_TAB_LAYERS_TITLE = LEFT_PANEL + "01";
     /* Find */
     public static final String LEFT_PANEL_TAB_FIND_TITLE = LEFT_PANEL + "02";
     /* There is an error while searching.*/
     public static final String LEFT_PANEL_FIND_ERROR = LEFT_PANEL + "03";
     /* Documents */
     public static final String LEFT_PANEL_TAB_DOCUMENTS_TITLE = LEFT_PANEL + "04";
     /* Public Display Map */
     public static final String LEFT_PANEL_TAB_PUBLIC_DISPLAY_MAP_TITLE = LEFT_PANEL + "05";
     /* The selected document does not have any attachment.*/
     public static final String IMPORT_FILE_DOCUMENT_DOES_NOT_HAVE_ATTACHMENT = IMPORT_FILE + "03";
 
     /* The last part is not set.*/
     public static final String PRINT_PUBLIC_DISPLAY_FILTER_NOT_FOUND = PRINT_PUBLIC_DISPLAY + "01";
 
     /* There are no cadastre objects with this last part found.*/
     public static final String PRINT_PUBLIC_DISPLAY_CENTER_LAST_PART_CO_NOT_FOUND = PRINT_PUBLIC_DISPLAY + "02";
        
     // <editor-fold defaultstate="collapsed" desc="Test Messages">  
     /** gistest001 - Unit Test Message */
     public static final String TEST001 = TEST + "001";
     // </editor-fold>
 }
