 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.commons.simplerestserver;
 
 import java.util.Map;
 
 /**
  * DOCUMENT ME!
  *
  * @author   martin.scholl@cismet.de
  * @version  $Revision$, $Date$
  */
 public interface ServerParamProvider {
 
    //~ Instance fields --------------------------------------------------------

    String PARAM_JERSEY_PROPERTY_PACKAGES = "com.sun.jersey.config.property.packages"; // NOI18N

     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     Map<String, String> getServerParams();
 }
