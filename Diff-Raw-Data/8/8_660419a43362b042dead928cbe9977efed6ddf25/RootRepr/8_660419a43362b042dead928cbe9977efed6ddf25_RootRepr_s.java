 package com.brightinteractive.assetbank.restapi.representations;
 
 import java.net.URL;
 
 import javax.xml.bind.annotation.XmlRootElement;
 
 /**
  * Jersey API Root URL - Points to service resources.
  * Includes API versioning to allow for future compatability.
  *
  * Eg
  *
  * {
  *     "api_name": "AssetBank RESTful API"
  *     "api_version_1_1": {
  *         "checkoutUrl": "http://..."
  *         "editorDependenciesUrl": "http://..."
  *         "lightboxesUrl": "http://..."
  *         "assetsUrl": "http://..."
  *         "signingUrl": "http://...."
  *     }
  * }
  *
  * Clients may read (GET) from this resource.
  * They should check that the api_name matches "AssetBank RESTful API" as expected,
  * and then use the sub-resources denoted in "api_version_1_1" (or another version as appropriate)
  *
  * @author Bright Interactive
  */
 @XmlRootElement(name="root")
 public class RootRepr {
     private static String API_NAME = "AssetBank RESTful API";
 
     public static class RootServices_v_1_1
     {
         public URL checkoutUrl;
         public URL editorDependenciesUrl;
         public URL lightboxesUrl;
         public URL assetsUrl;
         public URL signingUrl;
     }
 
     public static class RootServices_v_1_2
     {
         public URL checkoutUrl;
         public URL editorDependenciesUrl;
         public URL lightboxesUrl;
         public URL assetsUrl;
         public URL usersUrl;
         public URL userSearchUrl;
         public URL attributesUrl;
         public URL categorySearchUrl;
         public URL accessLevelSearchUrl;
         public URL signingUrl;
         public URL assetSearchUrl;
         public URL publishingActionsUrl;
         public URL displayAttributeGroupUrl;
     }
 
     public static class RootServices_v_1_3
     {
         public URL checkoutUrl;
         public URL editorDependenciesUrl;
         public URL lightboxesUrl;
         public URL assetsUrl;
         public URL usersUrl;
         public URL userSearchUrl;
         public URL attributesUrl;
         public URL categorySearchUrl;
         public URL accessLevelSearchUrl;
         public URL signingUrl;
         public URL assetSearchUrl;
         public URL publishingActionsUrl;
         public URL displayAttributeGroupUrl;
        public URL tempChunkedFileUrl;
     }
 
     public String api_name;
     public RootServices_v_1_1 api_version_1_1;
     public RootServices_v_1_2 api_version_1_2;
     public RootServices_v_1_3 api_version_1_3;
     public static final String LATEST_API_VERSION = "api_version_1_3";
 
     // just to make JAXB happy
     public RootRepr()
     {
     }
 
     public RootRepr(URL a_checkoutUrl,
                     URL a_editorDependenciesUrl,
                     URL a_lightboxesUrl,
                     URL a_lightboxes_v1_1_Url,
                     URL a_assetsUrl,
                     URL a_assets_v1_2_Url,
                     URL a_usersUrl,
                     URL a_userSearchUrl,
                     URL a_attributesUrl,
                     URL a_categorySearchUrl,
                     URL a_accessLevelSearchUrl,
                     URL a_signingUrl,
                     URL a_assetSearchUrl,
                     URL a_publishingActionsUrl,
                     URL a_displayAttributeGroupUrl,
                    URL a_tempChunkedFileUrl)
     {
         api_name = API_NAME;
 
         //build the 1.1 version...
         api_version_1_1 = new RootServices_v_1_1();
         api_version_1_1.checkoutUrl = a_checkoutUrl;
         api_version_1_1.editorDependenciesUrl = a_editorDependenciesUrl;
         api_version_1_1.lightboxesUrl = a_lightboxes_v1_1_Url;
         api_version_1_1.assetsUrl = a_assets_v1_2_Url;
         api_version_1_1.signingUrl = a_signingUrl;
 
         //and now the 1.2...
         api_version_1_2 = new RootServices_v_1_2();
         api_version_1_2.checkoutUrl = a_checkoutUrl;
         api_version_1_2.editorDependenciesUrl = a_editorDependenciesUrl;
         api_version_1_2.lightboxesUrl = a_lightboxesUrl;
         api_version_1_2.assetsUrl = a_assets_v1_2_Url;
         api_version_1_2.usersUrl = a_usersUrl;
         api_version_1_2.userSearchUrl = a_userSearchUrl;
         api_version_1_2.attributesUrl = a_attributesUrl;
         api_version_1_2.categorySearchUrl = a_categorySearchUrl;
         api_version_1_2.accessLevelSearchUrl = a_accessLevelSearchUrl;
         api_version_1_2.signingUrl = a_signingUrl;
         api_version_1_2.assetSearchUrl = a_assetSearchUrl;
         api_version_1_2.publishingActionsUrl = a_publishingActionsUrl;
         api_version_1_2.displayAttributeGroupUrl = a_displayAttributeGroupUrl;
 
         //and now the 1.3...
         api_version_1_3 = new RootServices_v_1_3();
         api_version_1_3.checkoutUrl = a_checkoutUrl;
         api_version_1_3.editorDependenciesUrl = a_editorDependenciesUrl;
         api_version_1_3.lightboxesUrl = a_lightboxesUrl;
         api_version_1_3.assetsUrl = a_assetsUrl;
         api_version_1_3.usersUrl = a_usersUrl;
         api_version_1_3.userSearchUrl = a_userSearchUrl;
         api_version_1_3.attributesUrl = a_attributesUrl;
         api_version_1_3.categorySearchUrl = a_categorySearchUrl;
         api_version_1_3.accessLevelSearchUrl = a_accessLevelSearchUrl;
         api_version_1_3.signingUrl = a_signingUrl;
         api_version_1_3.assetSearchUrl = a_assetSearchUrl;
         api_version_1_3.publishingActionsUrl = a_publishingActionsUrl;
         api_version_1_3.displayAttributeGroupUrl = a_displayAttributeGroupUrl;
        api_version_1_3.tempChunkedFileUrl = a_tempChunkedFileUrl;
     }
 }
