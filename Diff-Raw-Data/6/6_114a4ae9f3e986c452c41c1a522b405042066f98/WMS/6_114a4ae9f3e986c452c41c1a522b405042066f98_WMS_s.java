 /* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.global;
 
 import org.vfny.geoserver.global.dto.ServiceDTO;
 import org.vfny.geoserver.global.dto.WMSDTO;
 
 
 /**
  * WMS
  * 
  * <p>
  * Represents the GeoServer information required to configure an  instance of
  * the WMS Server. This class holds the currently used  configuration and is
  * instantiated initially by the GeoServerPlugIn  at start-up, but may be
  * modified by the Configuration Interface  during runtime. Such modifications
  * come from the GeoServer Object  in the SessionContext.
  * </p>
  * 
  * <p>
  * WMS wms = new WMS(dto); System.out.println(wms.getName() + wms.WMS_VERSION);
  * System.out.println(wms.getAbstract());
  * </p>
  *
  * @author Gabriel Roldn
  * @version $Id: WMS.java,v 1.7 2004/02/24 02:08:50 cholmesny Exp $
  */
 public class WMS extends Service {
     /** WMS version spec implemented */
     private static final String WMS_VERSION = "1.1.1";
 
     /** WMS spec specifies this fixed service name */
     private static final String FIXED_SERVICE_NAME = "OGC:WMS";
 
     /** list of WMS Exception Formats */
     private static final String[] EXCEPTION_FORMATS = {
        "application/vnd.ogc.se_xml", "application/vnd.ogc.se_inimage",
        "application/vnd.ogc.se_blank"
     };
     
     public static final String WEB_CONTAINER_KEY = "WMS";
 
     /**
      * WMS constructor.
      * 
      * <p>
      * Stores the data specified in the WMSDTO object in this WMS Object for
      * GeoServer to use.
      * </p>
      *
      * @param config The data intended for GeoServer to use.
      */
     public WMS(WMSDTO config) {
         super(config.getService());
     }
 
     /**
      * load purpose.
      * <p>
      * loads a new instance of data into this object.
      * </p>
      * @param config
      */
     public void load(WMSDTO config) {
     	super.load(config.getService());
     }
 
     /**
      * WMS constructor.
      * 
      * <p>
      * Package constructor intended for default use by GeoServer
      * </p>
      *
      * @see GeoServer#GeoServer()
      */
     WMS() {
         super(new ServiceDTO());
     }
 
     /**
      * Implement toDTO.
      * 
      * <p>
      * Package method used by GeoServer. This method may return references, and
      * does not clone, so extreme caution sould be used when traversing the
      * results.
      * </p>
      *
      * @return WMSDTO An instance of the data this class represents. Please see
      *         Caution Above.
      *
      * @see org.vfny.geoserver.global.GlobalLayerSupertype#toDTO()
      * @see WMSDTO
      */
     public Object toDTO() {
         WMSDTO w = new WMSDTO();
         w.setService((ServiceDTO)super.toDTO());
 
         return w;
     }
 
     /**
      * getExceptionFormats purpose.
      * 
      * <p>
      * Returns a static list of Exception Formats in as Strings
      * </p>
      *
      * @return String[] a static list of Exception Formats
      */
     public String[] getExceptionFormats() {
         return EXCEPTION_FORMATS;
     }
 
     /**
      * overrides getName() to return the fixed service name as specified by OGC
      * WMS 1.1 spec
      *
      * @return static service name.
      */
     public String getName() {
         return FIXED_SERVICE_NAME;
     }
 
     /**
      * Returns the version of this WMS Instance.
      *
      * @return static version name
      */
     public static String getVersion() {
         return WMS_VERSION;
     }
 
     /**
      * Informs the user that this WMS supports SLD.  We don't currently
      * handle sld, still needs to be rolled in from geotools, so this now
      * must be false.
      *
      * @return false
      */
     public boolean supportsSLD() {
         return false;
     }
 
     /**
      * Informs the user that this WMS supports User Layers
      *
      * @return false
      */
     public boolean supportsUserLayer() {
         return false;
     }
 
     /**
      * Informs the user that this WMS supports User Styles
      *
      * @return false
      */
     public boolean supportsUserStyle() {
         return false;
     }
 
     /**
      * Informs the user that this WMS supports Remote WFS.
      *
      * @return false
      */
     public boolean supportsRemoteWFS() {
         return false;
     }
 }
