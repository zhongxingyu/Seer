 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package br.uff.ic.labgc.properties;
 
 /**
  * Armazena as constantes relacionadas ao arquivo de propriedades.
  * @author Cristiano
  */
 public interface IPropertiesConstants {
     public static String PROPERTIES_FILE_NAME = "br.uff.ic.labgc.properties.labgc";
     public static String COMM_LOCAL_CONNECTOR = "localConnector";
     public static String COMM_REMOTE_CONNECTOR = "remoteConnector";
     public static String COMM_REMOTE_PROTOCOL = "remoteProtocol";
     public static String COMM_LOCAL_HOST = "localHost";
    public static String LOCAL_ADDRESS_IDENTIFIER = "localAddressIdentifier";
     public static String COMM_REMOTE_PORT = "remotePort";
     public static String RMI_REPOSITORY_OBJECT = "repositoryRMIObject";
     
     /**
      * Identifica a revisão HEAD
      */
     public static final String REVISION_HEAD = "-1";
 }
