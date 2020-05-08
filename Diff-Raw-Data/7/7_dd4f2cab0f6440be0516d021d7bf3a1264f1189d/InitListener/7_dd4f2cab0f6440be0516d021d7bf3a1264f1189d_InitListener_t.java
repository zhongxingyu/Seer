 package org.stwerff.msl;
 
 import java.io.InputStream;
 
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 
 import org.stwerff.mslagents.ClockAgent;
 
 import com.almende.eve.agent.AgentFactory;
 import com.almende.eve.config.Config;
 import com.almende.eve.rpc.jsonrpc.JSONRequest;
 import com.almende.eve.rpc.jsonrpc.jackson.JOM;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 
 public class InitListener implements ServletContextListener {
 
 	public void contextDestroyed(ServletContextEvent arg0) {
 	}
 
 	public void contextInitialized(ServletContextEvent ctx) {
 		InputStream is = ctx.getServletContext().getResourceAsStream(
 				"/WEB-INF/eve.yaml");
 		Config config = new Config(is);
 		try {
 			AgentFactory factory = new AgentFactory(config);
 			if (!factory.hasAgent("clock")) {
 				factory.createAgent(ClockAgent.class, "clock");
 			}
 			ClockAgent clock = (ClockAgent) factory.getAgent("clock");
 
			for (String taskId : clock.getScheduler().getTasks()) {
				clock.getScheduler().cancelTask(taskId);
			}
 
 			ObjectNode params = JOM.createObjectNode();
 			JSONRequest request = new JSONRequest("updateHeads", params);
 			long delay = 12000; // milliseconds
 			clock.getScheduler().createTask(request, delay);
 
 			request = new JSONRequest("updateSpice", params);
 			clock.getScheduler().createTask(request, delay);
 
 			params.put("reload", new Boolean(false));
 			delay = 2000;
 			request = new JSONRequest("updateSols", params);
 			clock.getScheduler().createTask(request, delay);
 
 		} catch (Exception e) {
 			System.err.println("ERROR initializing agents");
 			e.printStackTrace();
 		}
 	}
 
 }
