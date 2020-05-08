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
 
 /**
  * <p>
  * Preference constant
  * </p>
  * 
  * @author Cinkel_A
  * @since 1.0.0
  */
 public class DataLoadPreferences {
     /** DataLoadPreferences REMOVE_SITE_NAME field */
     public static final String REMOVE_SITE_NAME = "REMOVE_SITE_NAME";
     public static final String NETWORK_COMBINED_CALCULATION = "USE_COMBINED_CALCULATION";
     public static final String ZOOM_TO_LAYER = "ZOOM_TO_LAYER";
 
     // network loader headers
     public static final String NH_CITY = "NH_CITY";
     public static final String NH_MSC = "NH_MSC";
     public static final String NH_BSC = "NH_BSC";
     public static final String NH_SITE = "NH_SITE";
     public static final String NH_SECTOR = "NH_SECTOR";
     public static final String NH_LATITUDE = "NH_LATITUDE";
     public static final String NH_LONGITUDE = "NH_LONGITUDE";
     public static final String DEFAULT_CHARSET = "DEFAULT_CHARSET";
     
     // probe loader headers
     public static final String PR_NAME = "PR_NAME";
     public static final String PR_TYPE = "PR_TYPE";
     public static final String PR_LATITUDE = "PR_LATITUDE";
     public static final String PR_LONGITUDE = "PR_LONGITUDE";
     
 	// network site loader headers
     public static final String NS_BEAMWIDTH = "NS_BEAMWIDTH";
     public static final String NS_AZIMUTH = "NS_AZIMUTH";
     
     // neighbour loader headers
     public static final String NE_CI = "NE_CI";
     public static final String NE_LAC = "NE_LAC";
     public static final String NE_BTS = "NE_BTS";
     
     public static final String NE_ADJ_CI = "NE_ADJ_CI";
     public static final String NE_ADJ_LAC = "NE_ADJ_LAC";
     public static final String NE_ADJ_BTS = "NE_ADJ_BTS";
     
     public static final String COMMON_CRS_LIST = "COMMON_CRS_LIST";
     public static final String CRS_DELIMETERS = "--DELIMETER--";
 
     private DataLoadPreferences() {
 
     }
 
 }
