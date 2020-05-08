 package org.esupportail.portlet.filemanager;
 
 import javax.xml.namespace.QName;
 
 /**
  * Constants related to the esup-filemanager API
  * 
  */
 public final class EsupFileManagerConstants {
 
    public static final String NAMESPACE = "https://www.esup-portail.org/schemas/esup-filemanager";
 
     public static final String DOWNLOAD_REQUEST_LOCAL_NAME = "DownloadRequest";
 
    public static final QName DOWNLOAD_REQUEST_QNAME = new QName(NAMESPACE, DOWNLOAD_REQUEST_LOCAL_NAME);
 
     public static final String DOWNLOAD_REQUEST_QNAME_STRING = "{" + NAMESPACE + "}" + DOWNLOAD_REQUEST_LOCAL_NAME;
     
     public static final String DOWNLOAD_RESPONSE_LOCAL_NAME = "DownloadResponse";
 
     public static final QName DOWNLOAD_RESPONSE_QNAME = new QName(NAMESPACE, DOWNLOAD_RESPONSE_LOCAL_NAME);
 
     public static final String DOWNLOAD_RESPONSE_QNAME_STRING = "{" + NAMESPACE + "}" + DOWNLOAD_RESPONSE_LOCAL_NAME;
     
     private EsupFileManagerConstants() {
     }
 }
