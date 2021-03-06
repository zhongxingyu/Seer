 package org.jboss.tools.esb.ui.bot.tests.examples;
 
 import org.jboss.tools.ui.bot.ext.config.Annotations.SWTBotTestRequires;
 import org.jboss.tools.ui.bot.ext.config.Annotations.Server;
 import org.jboss.tools.ui.bot.ext.config.Annotations.ServerState;
 import org.jboss.tools.ui.bot.ext.config.Annotations.ServerType;
 
 @SWTBotTestRequires(server=@Server(type=ServerType.SOA,state=ServerState.Running))
 public class WebServiceConsumer1 extends ESBExampleTest {
 	@Override
 	public String getExampleName() {
 		return "JBoss ESB Web Service consumer1 Example";
 	}
 	@Override
 	public String getExampleProjectName() {
 		return "webservice_consumer1";
 	}
 	@Override
 	public String getExampleClientProjectName() {
 		return "webservice_consumer1_client";
 	}
 	@Override
 	protected void executeExample() {
 		super.executeExample();	
 		String text = executeClientGetServerOutput(getExampleClientProjectName(),"src","org.jboss.soa.esb.samples.quickstart.webservice_consumer1.test","SendJMSMessage.java");
 		assertNotNull("Calling JMS Send message failed, nothing appened to server log",text);	
		assertTrue("Calling JMS Send message failed, unexpected server output :"+text,text.contains("Hello World Greeting for 'JMS'"));
 		text = null;
 		text = executeClientGetServerOutput(getExampleClientProjectName(),"src","org.jboss.soa.esb.samples.quickstart.webservice_consumer1.test","SendEsbMessage.java");
 		assertNotNull("Calling ESB Send message failed, nothing appened to server log",text);	
		assertTrue("Calling ESB Send message failed, unexpected server output :"+text,text.contains("Hello World Greeting for 'ESB'"));
 		
 	}
 }
