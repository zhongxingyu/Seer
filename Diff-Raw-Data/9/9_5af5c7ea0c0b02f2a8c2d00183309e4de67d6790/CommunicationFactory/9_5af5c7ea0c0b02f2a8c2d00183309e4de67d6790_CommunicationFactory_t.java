 package br.uff.ic.labgc.comm.client;
 
 import br.uff.ic.labgc.exception.ApplicationException;
 import br.uff.ic.labgc.exception.CommunicationException;
 import br.uff.ic.labgc.properties.ApplicationProperties;
 import br.uff.ic.labgc.properties.IPropertiesConstants;
 import br.uff.ic.labgc.server.IServer;
 import java.lang.reflect.Constructor;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Cristiano
  */
 public class CommunicationFactory {
 
     private static CommunicationFactory instance;
     private Map<String, IServer> commClientList;
 
     private CommunicationFactory() {
         commClientList = new ConcurrentHashMap<String, IServer>(2);
     }
 
     /**
      * Método que retorna um singleton da fábrica de objetos de comunicação
      *
      * @return
      */
     public synchronized static CommunicationFactory getFactory() {
         if (instance == null) {
             instance = new CommunicationFactory();
         }
 
         return instance;
     }
 
     /**
      * Retorna uma instância de servidor, conforme especificado na propriedade
      * serverclass do arquivo labgc.properties
      *
      * @param hostName servidor de repositório. Caso seja localhost, devolverá
      * uma instância local do servidor.
      * @return Instância local ou remota que implementa a interface IServer.
      */
     public IServer getServer(String hostName) throws ApplicationException {
         IServer retorno = null;
         if (hostName == null || "".equals(hostName)) {
             throw new ApplicationException("hostName não pode ser nulo ou vazio.");
         }
         if (commClientList.get(hostName) == null) {
             try {
                 String serverClass = ApplicationProperties.getPropertyValue(getCommunicationStrategy(hostName));
                 Constructor c = Class.forName(serverClass).getConstructor(String.class);
                 commClientList.put(hostName, (IServer) c.newInstance(hostName));
             } catch (Exception ex) {
                 Logger.getLogger(CommunicationFactory.class.getName()).log(Level.SEVERE, null, ex);
                 throw new CommunicationException("Não foi possível instanciar um servidor.", ex);
             }
         }
         return commClientList.get(hostName);
 
     }
 
     /**
      * Retorna o identificador da estratégia de comunicação. Caso o hostname seja
      * localhost ou o mesmo hostname da propriedade localAddressIdentifier, retorna
      * uma estratégia local. Caso contrário, retorna uma estratégia remota.
      * @param hostName
      * @return 
      */
     private String getCommunicationStrategy(String hostName) {
         String commStrategy = IPropertiesConstants.COMM_LOCAL_CONNECTOR;
        if (!IPropertiesConstants.COMM_LOCAL_HOST.equalsIgnoreCase(hostName)) {
             commStrategy = IPropertiesConstants.COMM_REMOTE_CONNECTOR;
         }
         return commStrategy;
     }
 }
