 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 package org.amanzi.neo.preferences;
 
 import java.nio.charset.Charset;
 
 import org.amanzi.neo.loader.internal.NeoLoaderPlugin;
 import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.geotools.referencing.CRS;
 
 /**
  * <p>
  * Preference initializer
  * </p>
  * 
  * @author Cinkel_A
  * @since 1.0.0
  */
 public class DataLoadPreferenceInitializer extends AbstractPreferenceInitializer {
 
     /**
      * constructor
      */
     public DataLoadPreferenceInitializer() {
         super();
     }
 
     @Override
     public void initializeDefaultPreferences() {
         IPreferenceStore pref = NeoLoaderPlugin.getDefault().getPreferenceStore();
         pref.setDefault(DataLoadPreferences.REMOVE_SITE_NAME, true);
         pref.setDefault(DataLoadPreferences.NETWORK_COMBINED_CALCULATION, true);
         pref.setDefault(DataLoadPreferences.ZOOM_TO_LAYER, true);
 
         pref.setDefault(DataLoadPreferences.NH_CITY, "City, Town, Ort");
         pref.setDefault(DataLoadPreferences.NH_MSC, "MSC, MSC_NAME, MSC Name");
         pref.setDefault(DataLoadPreferences.NH_BSC, "BSC, BSC_NAME, RNC, BSC Name");
         pref.setDefault(DataLoadPreferences.NH_SITE, "Site, Name, Site Name");
         pref.setDefault(DataLoadPreferences.NH_SECTOR, "Sector, Cell, BTS_Name, CELL_NAME, GSM Sector ID");
         pref.setDefault(DataLoadPreferences.NH_LATITUDE, "lat.*, y_wert.*, northing");
         pref.setDefault(DataLoadPreferences.NH_LONGITUDE, "long.*, x_wert.*, easting");
         
         pref.setDefault(DataLoadPreferences.PR_NAME, "probe");
         pref.setDefault(DataLoadPreferences.PR_TYPE, "type");
         pref.setDefault(DataLoadPreferences.PR_LATITUDE, "lat.*, .*latitude.*");
         pref.setDefault(DataLoadPreferences.PR_LONGITUDE, "long.*, .*longitude.*");
         
        pref.setDefault(DataLoadPreferences.NS_SITE, "SITE_ID");
         pref.setDefault(DataLoadPreferences.NS_AZIMUTH, ".*azimuth.*");
         pref.setDefault(DataLoadPreferences.NS_BEAMWIDTH, ".*beamwidth.*, beam, hbw");
         
         pref.setDefault(DataLoadPreferences.NE_CI, "CI");
         pref.setDefault(DataLoadPreferences.NE_BTS, "BTS_NAME");
         pref.setDefault(DataLoadPreferences.NE_LAC, "LAC");
         
         pref.setDefault(DataLoadPreferences.NE_ADJ_CI, "ADJ_CI");
         pref.setDefault(DataLoadPreferences.NE_ADJ_BTS, "ADJ_BTS_NAME");
         pref.setDefault(DataLoadPreferences.NE_ADJ_LAC, "ADJ_LAG");
         
         pref.setDefault(DataLoadPreferences.DEFAULT_CHARSET, Charset.defaultCharset().name());
         StringBuilder def;
         try {
             def = new StringBuilder(CRS.decode("EPSG:4326").toWKT()).append(DataLoadPreferences.CRS_DELIMETERS).append(CRS.decode("EPSG:31467").toWKT()).append(
                     DataLoadPreferences.CRS_DELIMETERS).append(CRS.decode("EPSG:3021").toWKT());
         } catch (Exception e) {
             NeoLoaderPlugin.exception(e);
             def = null;
         }
         pref.setDefault(DataLoadPreferences.COMMON_CRS_LIST, def.toString());
     }
 
 }
