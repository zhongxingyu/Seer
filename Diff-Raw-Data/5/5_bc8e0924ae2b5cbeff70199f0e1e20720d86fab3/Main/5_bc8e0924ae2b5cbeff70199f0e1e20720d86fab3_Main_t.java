 package net.mindengine.oculus.grid;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Properties;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import net.mindengine.oculus.experior.utils.PropertyUtils;
 import net.mindengine.oculus.grid.agent.Agent;
 import net.mindengine.oculus.grid.agent.AgentProperties;
 import net.mindengine.oculus.grid.domain.agent.AgentInformation;
 import net.mindengine.oculus.grid.domain.agent.AgentTag;
 import net.mindengine.oculus.grid.server.Server;
 import net.mindengine.oculus.grid.server.ServerProperties;
 import net.mindengine.oculus.grid.storage.DefaultAgentStorage;
 import net.mindengine.oculus.grid.storage.DefaultGridStorage;
 
 import org.xml.sax.SAXException;
 
 public class Main {
 
     public static void main(String[] args) throws Exception {
         if ( args == null || args.length == 0 ) {
             printHelp();
         }
         else if (args[0].equals("server")) {
             runServer();
         }
         else if (args[0].equals("agent")) {
             runAgent();
        }
     }
     
     private static AgentTag[] loadAgentTags() throws ParserConfigurationException, SAXException, IOException {
         File tagsFile = new File("grid.agent.tags.xml");
         return GridUtils.loadTags(tagsFile);
     }
     private static void runAgent() throws Exception {
         Agent agent = new Agent();
         Properties properties = new Properties();
         try {
             properties.load(new FileReader(new File("grid.agent.properties")));
         }
         catch (Exception e) {
             e.printStackTrace();
         }
         agent.setServerHost(properties.getProperty(AgentProperties.SERVER_HOST));
         agent.setServerPort(Integer.parseInt(properties.getProperty(AgentProperties.SERVER_PORT, "8081")));
         agent.setServerName(properties.getProperty(AgentProperties.SERVER_NAME));
         
         
         AgentInformation agentInformation = new AgentInformation();
         agentInformation.setHost(properties.getProperty(AgentProperties.AGENT_HOST));
         agentInformation.setPort(Integer.parseInt(properties.getProperty(AgentProperties.AGENT_PORT, "8082")));
         agentInformation.setName(properties.getProperty(AgentProperties.AGENT_NAME, "Agent"));
         agentInformation.setRemoteName(properties.getProperty(AgentProperties.AGENT_REMOTE_NAME, "agent"));
         agentInformation.setTags(loadAgentTags());
         agentInformation.setDescription(properties.getProperty(AgentProperties.AGENT_DESCRIPTION));
         agent.setAgentInformation(agentInformation);
         
         agent.setAgentReconnectionTimeout(Integer.parseInt(properties.getProperty(AgentProperties.AGENT_RECONNECT_TIMEOUT, "5")));
         agent.setAgentOculusGridLibrary(properties.getProperty(GridProperties.GRID_LIBRARY));
         agent.setAgentOculusRunnerProcessTemplate(properties.getProperty(AgentProperties.AGENT_OCULUS_RUNNER_PROCESS_TEMPLATE));
         
         DefaultAgentStorage storage = new DefaultAgentStorage();
         storage.setStoragePath(properties.getProperty(GridProperties.STORAGE_PATH));
         agent.setStorage(storage);
         agent.startAgent();
     }
 
     private static void runServer() throws Exception {
         Server server = new Server();
         
         Properties properties = new Properties();
         
         try {
             properties.load(new FileReader(new File("grid.server.properties")));
         }
         catch (Exception e) {
             e.printStackTrace();
         }
         PropertyUtils.overridePropertiesWithSystemProperties(properties);
         
         Integer port = Integer.parseInt(properties.getProperty(ServerProperties.SERVER_PORT, "8081"));
         String strStoreCompletedTasksTime = properties.getProperty(ServerProperties.SERVER_STORE_COMPLETED_TASKS_TIME);
         if (strStoreCompletedTasksTime == null || strStoreCompletedTasksTime.isEmpty()) {
             server.setStoreCompletedTasksTime(null);
         } else {
             server.setStoreCompletedTasksTime(Long.parseLong(strStoreCompletedTasksTime));
         }
         String serverName = properties.getProperty(ServerProperties.SERVER_NAME, "grid");
         
         //Setting a storage to handle project synchronization
         DefaultGridStorage storage = new DefaultGridStorage();
         storage.setStoragePath(properties.getProperty(GridProperties.STORAGE_PATH));
         server.setStorage(storage);
         
         server.startServer(port, serverName);
     }
     
     private static void printHelp() {
         System.out.println("Usage: java -jar oculus-grid.jar [ server | agent ]");
     }
 }
