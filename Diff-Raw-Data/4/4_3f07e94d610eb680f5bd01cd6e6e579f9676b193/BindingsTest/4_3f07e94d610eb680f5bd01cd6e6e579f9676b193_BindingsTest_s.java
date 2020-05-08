 package org.jboss.tools.switchyard.ui.bot.test;
 
 import static org.jboss.tools.switchyard.reddeer.binding.CXFBindingPage.DATA_FORMAT_POJO;
 import static org.jboss.tools.switchyard.reddeer.binding.JCABindingPage.ACKNOWLEDGE_MODE_AUTO;
 import static org.jboss.tools.switchyard.reddeer.binding.JCABindingPage.ACKNOWLEDGE_MODE_DUPS_OK;
 import static org.jboss.tools.switchyard.reddeer.binding.JCABindingPage.ENDPOINT_JMS;
 import static org.jboss.tools.switchyard.reddeer.binding.JCABindingPage.RESOURCE_ADAPTER_GENERIC;
 import static org.jboss.tools.switchyard.reddeer.binding.JCABindingPage.RESOURCE_ADAPTER_HORNETQ_QUEUE;
 import static org.jboss.tools.switchyard.reddeer.binding.JCABindingPage.RESOURCE_ADAPTER_HORNETQ_TOPIC;
 import static org.jboss.tools.switchyard.reddeer.binding.JCABindingPage.SUBSCRIPTION_NONDURABLE;
 import static org.jboss.tools.switchyard.reddeer.binding.JMSBindingPage.TYPE_QUEUE;
 import static org.jboss.tools.switchyard.reddeer.binding.JMSBindingPage.TYPE_TOPIC;
 import static org.jboss.tools.switchyard.reddeer.binding.MQTTBindingPage.QOS_EXACTLY_ONCE;
 import static org.jboss.tools.switchyard.reddeer.binding.MailBindingPage.ACCOUNT_TYPE_IMAP;
 import static org.jboss.tools.switchyard.reddeer.binding.OperationOptionsPage.JAVA_CLASS;
 import static org.jboss.tools.switchyard.reddeer.binding.OperationOptionsPage.OPERATION_NAME;
 import static org.jboss.tools.switchyard.reddeer.binding.OperationOptionsPage.REGEX;
 import static org.jboss.tools.switchyard.reddeer.binding.OperationOptionsPage.XPATH;
 import static org.jboss.tools.switchyard.reddeer.binding.SOAPBindingPage.SOAP_HEADERS_TYPE_DOM;
 import static org.jboss.tools.switchyard.reddeer.binding.SchedulingBindingPage.SCHEDULING_TYPE_CRON;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
 import org.jboss.reddeer.eclipse.jdt.ui.NewJavaClassWizardDialog;
 import org.jboss.reddeer.eclipse.jdt.ui.ProjectExplorer;
 import org.jboss.reddeer.junit.requirement.inject.InjectRequirement;
 import org.jboss.reddeer.junit.runner.RedDeerSuite;
 import org.jboss.reddeer.swt.handler.ShellHandler;
 import org.jboss.reddeer.swt.impl.shell.DefaultShell;
 import org.jboss.reddeer.swt.impl.shell.WorkbenchShell;
 import org.jboss.reddeer.workbench.impl.editor.TextEditor;
 import org.jboss.tools.switchyard.reddeer.binding.AtomBindingPage;
 import org.jboss.tools.switchyard.reddeer.binding.CXFBindingPage;
 import org.jboss.tools.switchyard.reddeer.binding.CamelBindingPage;
 import org.jboss.tools.switchyard.reddeer.binding.FTPBindingPage;
 import org.jboss.tools.switchyard.reddeer.binding.FTPSBindingPage;
 import org.jboss.tools.switchyard.reddeer.binding.FileBindingPage;
 import org.jboss.tools.switchyard.reddeer.binding.HTTPBindingPage;
 import org.jboss.tools.switchyard.reddeer.binding.JCABindingPage;
 import org.jboss.tools.switchyard.reddeer.binding.JMSBindingPage;
 import org.jboss.tools.switchyard.reddeer.binding.JPABindingPage;
 import org.jboss.tools.switchyard.reddeer.binding.MQTTBindingPage;
 import org.jboss.tools.switchyard.reddeer.binding.MailBindingPage;
 import org.jboss.tools.switchyard.reddeer.binding.NettyTCPBindingPage;
 import org.jboss.tools.switchyard.reddeer.binding.NettyUDPBindingPage;
 import org.jboss.tools.switchyard.reddeer.binding.RESTBindingPage;
 import org.jboss.tools.switchyard.reddeer.binding.RSSBindingPage;
 import org.jboss.tools.switchyard.reddeer.binding.SAPBindingPage;
 import org.jboss.tools.switchyard.reddeer.binding.SCABindingPage;
 import org.jboss.tools.switchyard.reddeer.binding.SFTPBindingPage;
 import org.jboss.tools.switchyard.reddeer.binding.SOAPBindingPage;
 import org.jboss.tools.switchyard.reddeer.binding.SQLBindingPage;
 import org.jboss.tools.switchyard.reddeer.binding.SchedulingBindingPage;
 import org.jboss.tools.switchyard.reddeer.component.Service;
 import org.jboss.tools.switchyard.reddeer.editor.SwitchYardEditor;
 import org.jboss.tools.switchyard.reddeer.preference.binding.BindingsPage;
 import org.jboss.tools.switchyard.reddeer.project.SwitchYardProject;
 import org.jboss.tools.switchyard.reddeer.requirement.SwitchYardRequirement;
 import org.jboss.tools.switchyard.reddeer.requirement.SwitchYardRequirement.SwitchYard;
 import org.jboss.tools.switchyard.reddeer.wizard.DefaultServiceWizard;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 // TODO Add test for editing existing bindings (now it is commented out)
 
 /**
  * Test for creating various bindings
  * 
  * @author apodhrad
  * 
  */
 @SwitchYard
 @RunWith(RedDeerSuite.class)
 public class BindingsTest {
 
 	public static final String CONTEXT_PATH = "Context Path";
 
 	public static final String PROJECT = "binding_project";
 	public static final String SERVICE = "HelloService";
 	public static final String METHOD = "sayHello";
 	public static final String PACKAGE = "com.example.switchyard." + PROJECT;
 	public static final String[] GATEWAY_BINDINGS = new String[] { "Camel Core (SEDA/Timer/URI)", "File",
 			"File Transfer (FTP/FTPS/SFTP)", "HTTP", "JCA", "JMS", "JPA", "Mail", "Network (TCP/UDP)", "REST", "SCA",
 			"Scheduling", "SOAP", "SQL" };
 	public static final String[] BINDINGS = new String[] { "Camel", "FTP", "FTPS", "File", "HTTP", "JCA", "JMS", "JPA",
 			"Mail", "Netty TCP", "Netty UDP", "REST", "SCA", "SFTP", "SOAP", "SQL", "Scheduling" };
 
 	private SwitchYardEditor editor;
 
 	@InjectRequirement
 	private static SwitchYardRequirement switchyardRequirement;
 
 	@BeforeClass
 	public static void maximizeWorkbench() {
 		new WorkbenchShell().maximize();
 	}
 
 	@BeforeClass
 	public static void createProject() {
 		try {
 			new SwitchYardEditor().saveAndClose();
 		} catch (Exception ex) {
 			// it is ok, we just try to close switchyard.xml if it is open
 		}
 
 		switchyardRequirement.project(PROJECT).binding(GATEWAY_BINDINGS).create();
 
 		// Sometimes the editor is not displayed properly, this happens only
 		// when the project is created by bot
 		new SwitchYardEditor().saveAndClose();
 		new ProjectExplorer().getProject(PROJECT).getProjectItem("SwitchYard").open();
 		new ProjectExplorer().getProject(PROJECT).select();
 
 		// Create new interface
 		NewJavaClassWizardDialog wizard = new NewJavaClassWizardDialog();
 		wizard.open();
 		wizard.getFirstPage().setName("Hello");
 		wizard.finish();
 
 		TextEditor textEditor = new TextEditor("Hello.java");
 		textEditor.setText("package com.example.switchyard.binding_project;\n" + "import javax.ws.rs.Produces;"
 				+ "import javax.ws.rs.GET;\n" + "import javax.ws.rs.Path;\n" + "import javax.ws.rs.PathParam;\n"
 				+ "public interface Hello {\n" + "@GET()\n" + "@Path(\"/{name}\")\n" + "@Produces(\"text/plain\")\n"
 				+ "String sayHello(@PathParam(\"name\") String name);\n}");
 		textEditor.save();
 		textEditor.close();
 
 		new SwitchYardEditor().addService();
 		new DefaultShell("New Service");
 		new SWTWorkbenchBot().shell("New Service").activate();
 		new DefaultServiceWizard().selectJavaInterface("Hello").setServiceName("HelloService").finish();
 		new SwitchYardEditor().save();
 	}
 
 	@AfterClass
 	public static void deleteProject() {
 		new WorkbenchShell().maximize();
 		new SwitchYardProject(PROJECT).delete(true);
 	}
 
 	@Before
 	public void focusSwitchYardEditor() {
 		editor = new SwitchYardEditor();
 	}
 
 	public void addService() {
 		editor = new SwitchYardEditor();
 		new SwitchYardEditor().addService();
 		new DefaultShell("New Service");
 		new SWTWorkbenchBot().shell("New Service").activate();
 		new DefaultServiceWizard().selectJavaInterface("Hello").setServiceName("HelloService").finish();
 		new SwitchYardEditor().save();
 	}
 
 	@After
 	public void removeAllBindings() {
 		ShellHandler.getInstance().closeAllNonWorbenchShells();
 		new Service("HelloService").showProperties().selectBindings().removeAll().ok();
 		new SwitchYardEditor().save();
 	}
 
 	@Test
 	public void atomBindingTest() throws Exception {
 		String time = "2015-01-01T00:00:00";
 
 		new Service(SERVICE).addBinding("Atom");
 		AtomBindingPage wizard = new AtomBindingPage();
 		wizard.setName("atom-binding");
 		wizard.getFeedURI().setText("http://localhost");
 		wizard.getSplitEntries().toggle(false);
 		wizard.getSplitEntries().toggle(true);
 		wizard.getFilter().toggle(false);
 		wizard.getFilter().toggle(true);
 		wizard.getLastUpdateStartingTimestamp().setText(time);
 		wizard.getSortEntriesbyDate().toggle(false);
 		wizard.getSortEntriesbyDate().toggle(true);
 		wizard.getDelayBetweenPolls().setText("1234");
 		wizard.finish();
 
 		new SwitchYardEditor().save();
 
 		String bindingPath = "/switchyard/composite/service/binding.atom";
 		assertEquals("atom-binding", editor.xpath(bindingPath + "/@name"));
 		assertEquals("http://localhost", editor.xpath(bindingPath + "/feedURI"));
 		assertEquals("true", editor.xpath(bindingPath + "/splitEntries"));
 		assertEquals("true", editor.xpath(bindingPath + "/filter"));
 		assertEquals(time, editor.xpath(bindingPath + "/lastUpdate"));
 		assertEquals("true", editor.xpath(bindingPath + "/sortEntries"));
 		assertEquals("1234", editor.xpath(bindingPath + "/consume/delay"));
 
 		BindingsPage properties = new Service(SERVICE).showProperties().selectBindings();
 		AtomBindingPage page = properties.selectAtomBinding("atom-binding");
 		assertEquals("atom-binding", page.getName());
 		properties.ok();
 	}
 
 	@Test
 	public void cxfBindingTest() throws Exception {
 		new Service(SERVICE).addBinding("CXF");
 		CXFBindingPage wizard = new CXFBindingPage();
 		wizard.setName("cxf-binding");
 		wizard.getCXFURI().setText("http://localhost");
 		wizard.getWSDLURL().setText("hello.wsdl");
 		wizard.getDataFormat().setSelection(DATA_FORMAT_POJO);
 		wizard.getServiceClass().setText("myClass.java");
 		wizard.getPortName().setText("port");
 		wizard.getRelayHeaders().toggle(false);
 		wizard.getRelayHeaders().toggle(true);
 		wizard.getWrapped().toggle(false);
 		wizard.getWrapped().toggle(true);
 		wizard.getWrappedStyle().setSelection("false");
 		wizard.getWrappedStyle().setSelection("true");
 		wizard.finish();
 
 		new SwitchYardEditor().save();
 
 		String bindingPath = "/switchyard/composite/service/binding.cxf";
 		assertEquals("cxf-binding", editor.xpath(bindingPath + "/@name"));
 		assertEquals("http://localhost", editor.xpath(bindingPath + "/cxfURI"));
 		assertEquals("hello.wsdl", editor.xpath(bindingPath + "/wsdlURL"));
 		assertEquals("myClass.java", editor.xpath(bindingPath + "/serviceClass"));
 		assertEquals("port", editor.xpath(bindingPath + "/portName"));
 		assertEquals(DATA_FORMAT_POJO, editor.xpath(bindingPath + "/dataFormat"));
 		assertEquals("true", editor.xpath(bindingPath + "/relayHeaders"));
 		assertEquals("true", editor.xpath(bindingPath + "/wrapped"));
 		assertEquals("true", editor.xpath(bindingPath + "/wrappedStyle"));
 
 		BindingsPage properties = new Service(SERVICE).showProperties().selectBindings();
 		CXFBindingPage page = properties.selectCXFBinding("cxf-binding");
 		assertEquals("cxf-binding", page.getName());
 		properties.ok();
 	}
 
 	@Test
 	public void camelBindingTest() throws Exception {
 		new Service(SERVICE).addBinding("Camel");
 		CamelBindingPage wizard = new CamelBindingPage();
 		assertEquals("camel1", wizard.getName());
 		wizard.setName("camel-binding");
 		wizard.getConfigURI().setText("camel-uri");
 		wizard.setOperationSelector(OPERATION_NAME, METHOD);
 		wizard.finish();
 
 		new SwitchYardEditor().save();
 
 		String bindingPath = "/switchyard/composite/service/binding.uri";
 		assertEquals("camel-binding", editor.xpath(bindingPath + "/@name"));
 		assertEquals("camel-uri", editor.xpath(bindingPath + "/@configURI"));
 		assertEquals(METHOD, editor.xpath(bindingPath + "/operationSelector/@operationName"));
 
 		BindingsPage properties = new Service(SERVICE).showProperties().selectBindings();
 		CamelBindingPage page = properties.selectCamelBinding("camel-binding");
 		assertEquals("camel-binding", page.getName());
 		assertEquals("camel-uri", page.getConfigURI().getText());
 		assertEquals(OPERATION_NAME, page.getOperationSelector());
 		assertEquals(METHOD, page.getOperationSelectorValue());
 		properties.ok();
 	}
 
 	@Test
 	public void ftpBindingTest() throws Exception {
 		new Service(SERVICE).addBinding("FTP");
 		FTPBindingPage wizard = new FTPBindingPage();
 		assertEquals("ftp1", wizard.getName());
 		wizard.setName("ftp-binding");
 		wizard.getHost().setText("myhost");
 		wizard.getPort().setText("1234");
 		wizard.getUserName().setText("admin");
 		wizard.getPassword().setText("admin123$");
 		wizard.getUseBinaryTransferMode().toggle(true);
 		wizard.getDirectory().setText("ftp-directory");
 		wizard.getFileName().setText("fileName");
 		wizard.getAutoCreateMissingDirectoriesinFilePath().click();
 		wizard.getAutoCreateMissingDirectoriesinFilePath().toggle(true);
 		wizard.getInclude().setText("include");
 		wizard.getExclude().setText("exclude");
 		wizard.getDeleteFilesOnceProcessed().toggle(true);
 		wizard.getProcessSubDirectoriesRecursively().toggle(true);
 		wizard.setOperationSelector(OPERATION_NAME, METHOD);
 		wizard.next();
 		wizard.getPreMove().setText("preMove");
 		wizard.getMove().setText("move");
 		wizard.getMoveFailed().setText("moveFailed");
 		wizard.getDelayBetweenPolls().setText("3000");
 		wizard.getMaxMessagesPerPoll().setText("10");
 		wizard.finish();
 
 		new SwitchYardEditor().save();
 
 		String bindingPath = "/switchyard/composite/service/binding.ftp";
 		assertEquals("ftp-binding", editor.xpath(bindingPath + "/@name"));
 		assertEquals(METHOD, editor.xpath(bindingPath + "/operationSelector/@operationName"));
 		assertEquals("ftp-directory", editor.xpath(bindingPath + "/directory"));
 		assertEquals("true", editor.xpath(bindingPath + "/autoCreate"));
 		assertEquals("fileName", editor.xpath(bindingPath + "/fileName"));
 		assertEquals("myhost", editor.xpath(bindingPath + "/host"));
 		assertEquals("1234", editor.xpath(bindingPath + "/port"));
 		assertEquals("admin", editor.xpath(bindingPath + "/username"));
 		assertEquals("admin123$", editor.xpath(bindingPath + "/password"));
 		assertEquals("true", editor.xpath(bindingPath + "/binary"));
 		assertEquals("true", editor.xpath(bindingPath + "/consume/delete"));
 		assertEquals("true", editor.xpath(bindingPath + "/consume/recursive"));
 		assertEquals("preMove", editor.xpath(bindingPath + "/consume/preMove"));
 		assertEquals("move", editor.xpath(bindingPath + "/consume/move"));
 		assertEquals("moveFailed", editor.xpath(bindingPath + "/consume/moveFailed"));
 		assertEquals("include", editor.xpath(bindingPath + "/consume/include"));
 		assertEquals("exclude", editor.xpath(bindingPath + "/consume/exclude"));
 		assertEquals("10", editor.xpath(bindingPath + "/consume/maxMessagesPerPoll"));
 		assertEquals("3000", editor.xpath(bindingPath + "/consume/delay"));
 
 		BindingsPage properties = new Service(SERVICE).showProperties().selectBindings();
 		FTPBindingPage page = properties.selectFTPBinding("ftp-binding");
 		assertEquals("ftp-binding", page.getName());
 		assertEquals("ftp-directory", page.getDirectory().getText());
 		properties.ok();
 	}
 
 	@Test
 	public void ftpsBindingTest() throws Exception {
 		new Service(SERVICE).addBinding("FTPS");
 		FTPSBindingPage wizard = new FTPSBindingPage();
 		assertEquals("ftps1", wizard.getName());
 		wizard.setName("ftps-binding");
 		wizard.getHost().setText("localhost");
 		wizard.getPort().setText("1234");
 		wizard.getUserName().setText("admin");
 		wizard.getPassword().setText("admin123$");
 		wizard.getUseBinaryTransferMode().toggle(true);
 		wizard.getDirectory().setText("ftps-directory");
 		wizard.getFileName().setText("filename");
 		wizard.getAutoCreateMissingDirectoriesinFilePath().toggle(false);
 		wizard.getAutoCreateMissingDirectoriesinFilePath().toggle(true);
 		wizard.getInclude().setText("include");
 		wizard.getExclude().setText("exclude");
 		wizard.getDeleteFilesOnceProcessed().toggle(true);
 		wizard.getProcessSubDirectoriesRecursively().toggle(true);
 		wizard.setOperationSelector(OPERATION_NAME, METHOD);
 		wizard.next();
 		wizard.getPreMove().setText("pre-move");
 		wizard.getMove().setText("move");
 		wizard.getMoveFailed().setText("move-failed");
 		wizard.getDelayBetweenPolls().setText("1200");
 		wizard.getMaxMessagesPerPoll().setText("3");
 		wizard.next();
 		wizard.getSecurityProtocol().setSelection(FTPSBindingPage.SECURITY_PROTOCOL_SSL);
 		wizard.getSecurityProtocol().setSelection(FTPSBindingPage.SECURITY_PROTOCOL_TLS);
 		wizard.getImplicit().toggle(true);
 		wizard.getExecutionProtocol().setSelection(FTPSBindingPage.EXECUTION_PROTOCOL_C);
 		wizard.getExecutionProtocol().setSelection(FTPSBindingPage.EXECUTION_PROTOCOL_S);
 		wizard.getExecutionProtocol().setSelection(FTPSBindingPage.EXECUTION_PROTOCOL_E);
 		wizard.getExecutionProtocol().setSelection(FTPSBindingPage.EXECUTION_PROTOCOL_P);
 		wizard.finish();
 
 		new SwitchYardEditor().save();
 
 		String bindingPath = "/switchyard/composite/service/binding.ftps";
 		assertEquals("ftps-binding", editor.xpath(bindingPath + "/@name"));
 		assertEquals(METHOD, editor.xpath(bindingPath + "/operationSelector/@operationName"));
 		assertEquals("ftps-directory", editor.xpath(bindingPath + "/directory"));
 		assertEquals("filename", editor.xpath(bindingPath + "/fileName"));
 		assertEquals("localhost", editor.xpath(bindingPath + "/host"));
 		assertEquals("1234", editor.xpath(bindingPath + "/port"));
 		assertEquals("admin", editor.xpath(bindingPath + "/username"));
 		assertEquals("admin123$", editor.xpath(bindingPath + "/password"));
 		assertEquals("true", editor.xpath(bindingPath + "/binary"));
 		assertEquals("true", editor.xpath(bindingPath + "/consume/delete"));
 		assertEquals("true", editor.xpath(bindingPath + "/consume/recursive"));
 		assertEquals("pre-move", editor.xpath(bindingPath + "/consume/preMove"));
 		assertEquals("move", editor.xpath(bindingPath + "/consume/move"));
 		assertEquals("move-failed", editor.xpath(bindingPath + "/consume/moveFailed"));
 		assertEquals("include", editor.xpath(bindingPath + "/consume/include"));
 		assertEquals("exclude", editor.xpath(bindingPath + "/consume/exclude"));
 		assertEquals("3", editor.xpath(bindingPath + "/consume/maxMessagesPerPoll"));
 		assertEquals("1200", editor.xpath(bindingPath + "/consume/delay"));
 
 		BindingsPage properties = new Service(SERVICE).showProperties().selectBindings();
 		FTPSBindingPage page = properties.selectFTPSBinding("ftps-binding");
 		assertEquals("ftps-binding", page.getName());
 		assertEquals("ftps-directory", page.getDirectory().getText());
 		properties.ok();
 	}
 
 	@Test
 	public void fileBindingTest() throws Exception {
 		new Service(SERVICE).addBinding("File");
 		FileBindingPage wizard = new FileBindingPage();
 		assertEquals("file1", wizard.getName());
 		wizard.setName("file-binding");
 		wizard.getDirectory().setText("file-directory");
 		wizard.getFileName().setText("test.txt");
 		wizard.getAutoCreateMissingDirectoriesinFilePath().toggle(false);
 		wizard.getAutoCreateMissingDirectoriesinFilePath().toggle(true);
 		wizard.getInclude().setText("inc");
 		wizard.getExclude().setText("ex");
 		wizard.setOperationSelector(OPERATION_NAME, METHOD);
 		wizard.getPreMove().setText("pre");
 		wizard.getMove().setText("processed");
 		wizard.getMoveFailed().setText("failed");
 		wizard.getMaxMessagesPerPoll().setText("10");
 		wizard.getDelayBetweenPolls().setText("963");
 		wizard.finish();
 
 		new SwitchYardEditor().save();
 
 		String bindingPath = "/switchyard/composite/service/binding.file";
 		assertEquals("file-binding", editor.xpath(bindingPath + "/@name"));
 		assertEquals(METHOD, editor.xpath(bindingPath + "/operationSelector/@operationName"));
 		assertEquals("file-directory", editor.xpath(bindingPath + "/directory"));
 		assertEquals("test.txt", editor.xpath(bindingPath + "/fileName"));
 		assertEquals("true", editor.xpath(bindingPath + "/autoCreate"));
 		assertEquals("963", editor.xpath(bindingPath + "/consume/delay"));
 		assertEquals("10", editor.xpath(bindingPath + "/consume/maxMessagesPerPoll"));
 		assertEquals("pre", editor.xpath(bindingPath + "/consume/preMove"));
 		assertEquals("processed", editor.xpath(bindingPath + "/consume/move"));
 		assertEquals("failed", editor.xpath(bindingPath + "/consume/moveFailed"));
 		assertEquals("inc", editor.xpath(bindingPath + "/consume/include"));
 		assertEquals("ex", editor.xpath(bindingPath + "/consume/exclude"));
 
 		BindingsPage properties = new Service(SERVICE).showProperties().selectBindings();
 		FileBindingPage page = properties.selectFileBinding("file-binding");
 		assertEquals("file-binding", page.getName());
 		assertEquals("file-directory", page.getDirectory().getText());
 		assertTrue(page.getAutoCreateMissingDirectoriesinFilePath().isChecked());
 		properties.ok();
 	}
 
 	@Test
 	public void httpBindingTest() throws Exception {
 		new Service(SERVICE).addBinding("HTTP");
 		HTTPBindingPage wizard = new HTTPBindingPage();
 		assertEquals("http1", wizard.getName());
 		wizard.setName("http-binding");
 		wizard.getContextPath().setText("http-context");
 		wizard.setOperationSelector(OPERATION_NAME, METHOD);
 		wizard.finish();
 
 		new SwitchYardEditor().save();
 
 		String bindingPath = "/switchyard/composite/service/binding.http";
 		assertEquals("http-binding", editor.xpath(bindingPath + "/@name"));
 		assertEquals(METHOD, editor.xpath(bindingPath + "/operationSelector/@operationName"));
 		assertEquals("http-context", editor.xpath(bindingPath + "/contextPath"));
 
 		BindingsPage properties = new Service(SERVICE).showProperties().selectBindings();
 		HTTPBindingPage page = properties.selectHTTPBinding("http-binding");
 		assertEquals("http-binding", page.getName());
 		page.setName("http1");
 		assertEquals("http-context", page.getContextPath().getText());
 		page.getContextPath().setText("hello");
 		assertEquals(OPERATION_NAME, page.getOperationSelector());
 		assertEquals(METHOD, page.getOperationSelectorValue());
 		page.setOperationSelector(XPATH, "/hello/method");
 		page.ok();
 
 		new SwitchYardEditor().save();
 
 		assertEquals("http1", editor.xpath(bindingPath + "/@name"));
 		assertEquals("/hello/method", editor.xpath(bindingPath + "/operationSelector.xpath/@expression"));
 		assertEquals("hello", editor.xpath(bindingPath + "/contextPath"));
 	}
 
 	@Test
 	public void jcaBindingGenericTest() throws Exception {
 		new Service(SERVICE).addBinding("JCA");
 		JCABindingPage wizard = new JCABindingPage();
 		assertEquals("jca1", wizard.getName());
 		wizard.setName("jca-binding");
 		wizard.getResourceAdapterType().setSelection(RESOURCE_ADAPTER_GENERIC);
 		wizard.getResourceAdapterArchive().setText("generic-ra.rar");
		wizard.getAcknowledgeMode().setSelection(JCABindingPage.ACKNOWLEDGE_MODE_AUTO);
 		wizard.setOperationSelector(OPERATION_NAME, METHOD);
 		wizard.next();
 		wizard.getEndpointMappingType().setSelection(ENDPOINT_JMS);
 		wizard.getTransacted().setText("false");
 		wizard.getTransacted().setText("true");
 		wizard.getBatchSize().setText("123");
 		wizard.getBatchTimeoutin().setText("2000");
 		wizard.getConnectionFactoryJNDIName().setText("jndiName");
 		wizard.getJNDIPropertiesFileName().setText("jndi.properties");
 		wizard.getDestinationType().setSelection("javax.jms.Queue");
 		wizard.finish();
 
 		new SwitchYardEditor().save();
 
 		String bindingPath = "/switchyard/composite/service/binding.jca";
 		assertEquals("jca-binding", editor.xpath(bindingPath + "/@name"));
 		assertEquals(METHOD, editor.xpath(bindingPath + "/operationSelector/@operationName"));
 		assertEquals("generic-ra.rar", editor.xpath(bindingPath + "/inboundConnection/resourceAdapter/@name"));
 		assertEquals("javax.jms.Queue", editor.xpath(bindingPath
 				+ "/inboundConnection/activationSpec/property[@name='destinationType']/@value"));
 		assertEquals("queue/YourQueueName",
 				editor.xpath(bindingPath + "/inboundConnection/activationSpec/property[@name='destination']/@value"));
 		assertEquals("", editor.xpath(bindingPath
 				+ "/inboundConnection/activationSpec/property[@name='messageSelector']/@value"));
		assertEquals("Auto-acknowledge", editor.xpath(bindingPath
				+ "/inboundConnection/activationSpec/property[@name='acknowledgeMode']/@value"));
 		assertEquals("org.switchyard.component.jca.endpoint.JMSEndpoint",
 				editor.xpath(bindingPath + "/inboundInteraction/endpoint/@type"));
 		assertEquals("true", editor.xpath(bindingPath + "/inboundInteraction/transacted"));
 		assertEquals("123", editor.xpath(bindingPath + "/inboundInteraction/batchCommit/@batchSize"));
 		assertEquals("2000", editor.xpath(bindingPath + "/inboundInteraction/batchCommit/@batchTimeout"));
 
 		BindingsPage properties = new Service(SERVICE).showProperties().selectBindings();
 		JCABindingPage page = properties.selectJCABinding("jca-binding");
 		assertEquals("jca-binding", page.getName());
 		assertEquals(RESOURCE_ADAPTER_GENERIC, page.getResourceAdapterType().getSelection());
 		assertEquals("generic-ra.rar", page.getResourceAdapterArchive().getText());
 		properties.ok();
 	}
 
 	@Test
 	public void jcaBindingHornetQueueTest() throws Exception {
 		new Service(SERVICE).addBinding("JCA");
 		JCABindingPage wizard = new JCABindingPage();
 		assertEquals("jca1", wizard.getName());
 		wizard.setName("jca-binding");
 		wizard.getResourceAdapterType().setSelection(RESOURCE_ADAPTER_HORNETQ_QUEUE);
 		wizard.getDestinationQueue().setText("queue/MyQueue");
 		wizard.getMessageSelector().setText("ms");
 		wizard.getAcknowledgeMode().setSelection(ACKNOWLEDGE_MODE_DUPS_OK);
 		wizard.setOperationSelector(REGEX, "say[H|h]ello");
 		wizard.next();
 		wizard.getEndpointMappingType().setSelection(ENDPOINT_JMS);
 		wizard.finish();
 
 		new SwitchYardEditor().save();
 
 		String bindingPath = "/switchyard/composite/service/binding.jca";
 		assertEquals("jca-binding", editor.xpath(bindingPath + "/@name"));
 		assertEquals("say[H|h]ello", editor.xpath(bindingPath + "/operationSelector.regex/@expression"));
 		assertEquals("hornetq-ra.rar", editor.xpath(bindingPath + "/inboundConnection/resourceAdapter/@name"));
 		assertEquals("javax.jms.Queue", editor.xpath(bindingPath
 				+ "/inboundConnection/activationSpec/property[@name='destinationType']/@value"));
 		assertEquals("queue/MyQueue",
 				editor.xpath(bindingPath + "/inboundConnection/activationSpec/property[@name='destination']/@value"));
 		assertEquals("ms", editor.xpath(bindingPath
 				+ "/inboundConnection/activationSpec/property[@name='messageSelector']/@value"));
 		assertEquals("Dups-ok-acknowledge", editor.xpath(bindingPath
 				+ "/inboundConnection/activationSpec/property[@name='acknowledgeMode']/@value"));
 
 		BindingsPage properties = new Service(SERVICE).showProperties().selectBindings();
 		JCABindingPage page = properties.selectJCABinding("jca-binding");
 		assertEquals("jca-binding", page.getName());
 		assertTrue(page.getResourceAdapterType().getSelection().equals(RESOURCE_ADAPTER_HORNETQ_QUEUE));
 		properties.ok();
 	}
 
 	@Test
 	public void jcaBindingHornetTopicTest() throws Exception {
 		new Service(SERVICE).addBinding("JCA");
 		JCABindingPage wizard = new JCABindingPage();
 		assertEquals("jca1", wizard.getName());
 		wizard.setName("jca-binding");
 		wizard.getResourceAdapterType().setSelection(RESOURCE_ADAPTER_HORNETQ_TOPIC);
 		wizard.getDestinationTopic().setText("topic/MyTopic");
 		wizard.getMessageSelector().setText("ms");
 		wizard.getAcknowledgeMode().setSelection(ACKNOWLEDGE_MODE_AUTO);
 		wizard.getSubscriptionDurability().setSelection(SUBSCRIPTION_NONDURABLE);
 		wizard.getClientID().setText("clientID");
 		wizard.getSubscriptionName().setText("sub-name");
 		wizard.setOperationSelector(JAVA_CLASS, "myClass.java");
 		wizard.next();
 		wizard.getEndpointMappingType().setSelection(ENDPOINT_JMS);
 		wizard.finish();
 
 		new SwitchYardEditor().save();
 
 		String bindingPath = "/switchyard/composite/service/binding.jca";
 		assertEquals("jca-binding", editor.xpath(bindingPath + "/@name"));
 		assertEquals("myClass.java", editor.xpath(bindingPath + "/operationSelector.java/@class"));
 		assertEquals("hornetq-ra.rar", editor.xpath(bindingPath + "/inboundConnection/resourceAdapter/@name"));
 		assertEquals("javax.jms.Topic", editor.xpath(bindingPath
 				+ "/inboundConnection/activationSpec/property[@name='destinationType']/@value"));
 		assertEquals("topic/MyTopic",
 				editor.xpath(bindingPath + "/inboundConnection/activationSpec/property[@name='destination']/@value"));
 		assertEquals("ms", editor.xpath(bindingPath
 				+ "/inboundConnection/activationSpec/property[@name='messageSelector']/@value"));
 		assertEquals(ACKNOWLEDGE_MODE_AUTO, editor.xpath(bindingPath
 				+ "/inboundConnection/activationSpec/property[@name='acknowledgeMode']/@value"));
 		assertEquals(
 				"sub-name",
 				editor.xpath(bindingPath
 						+ "/inboundConnection/activationSpec/property[@name='subscriptionName']/@value"));
 		assertEquals(
 				SUBSCRIPTION_NONDURABLE,
 				editor.xpath(bindingPath
 						+ "/inboundConnection/activationSpec/property[@name='subscriptionDurability']/@value"));
 		assertEquals("clientID",
 				editor.xpath(bindingPath + "/inboundConnection/activationSpec/property[@name='clientId']/@value"));
 		assertEquals(
 				"1",
 				editor.xpath("count(" + bindingPath
 						+ "/inboundConnection/activationSpec/property[@name='messageSelector'])"));
 		assertEquals(
 				"1",
 				editor.xpath("count(" + bindingPath
 						+ "/inboundConnection/activationSpec/property[@name='acknowledgeMode'])"));
 
 		BindingsPage properties = new Service(SERVICE).showProperties().selectBindings();
 		JCABindingPage page = properties.selectJCABinding("jca-binding");
 		assertEquals("jca-binding", page.getName());
 		assertTrue(page.getResourceAdapterType().getSelection().equals(RESOURCE_ADAPTER_HORNETQ_TOPIC));
 		properties.ok();
 	}
 
 	@Test
 	public void jmsBindingQueueTest() throws Exception {
 		new Service(SERVICE).addBinding("JMS");
 		JMSBindingPage wizard = new JMSBindingPage();
 		assertEquals("jms1", wizard.getName());
 		wizard.setName("jms-binding");
 		wizard.getType().setSelection(TYPE_QUEUE);
 		wizard.setQueueTopicName("myqueue");
 		wizard.getConnectionFactory().setText("#MyFactory");
 		wizard.getConcurrentConsumers().setText("3");
 		wizard.getMaximumConcurrentConsumers().setText("7");
 		wizard.getReplyTo().setText("reply-to");
 		wizard.getSelector().setText("selector");
 		wizard.getTransactionManager().setText("MyTX");
 		wizard.getTransacted().toggle(false);
 		wizard.getTransacted().toggle(true);
 		wizard.setOperationSelector(OPERATION_NAME, METHOD);
 		wizard.finish();
 
 		new SwitchYardEditor().save();
 
 		String bindingPath = "/switchyard/composite/service/binding.jms";
 		assertEquals("jms-binding", editor.xpath(bindingPath + "/@name"));
 		assertEquals(METHOD, editor.xpath(bindingPath + "/operationSelector/@operationName"));
 		assertEquals("myqueue", editor.xpath(bindingPath + "/queue"));
 		assertEquals("#MyFactory", editor.xpath(bindingPath + "/connectionFactory"));
 		assertEquals("3", editor.xpath(bindingPath + "/concurrentConsumers"));
 		assertEquals("7", editor.xpath(bindingPath + "/maxConcurrentConsumers"));
 		assertEquals("reply-to", editor.xpath(bindingPath + "/replyTo"));
 		assertEquals("selector", editor.xpath(bindingPath + "/selector"));
 		assertEquals("true", editor.xpath(bindingPath + "/transacted"));
 		assertEquals("MyTX", editor.xpath(bindingPath + "/transactionManager"));
 
 		BindingsPage properties = new Service(SERVICE).showProperties().selectBindings();
 		JMSBindingPage page = properties.selectJMSBinding("jms-binding");
 		assertEquals("jms-binding", page.getName());
 		assertEquals("myqueue", page.getQueueTopicName());
 		properties.ok();
 	}
 
 	@Test
 	public void jmsBindingTopicTest() throws Exception {
 		new Service(SERVICE).addBinding("JMS");
 		JMSBindingPage wizard = new JMSBindingPage();
 		assertEquals("jms1", wizard.getName());
 		wizard.setName("jms-binding");
 		wizard.getType().setSelection(TYPE_TOPIC);
 		wizard.setQueueTopicName("mytopic");
 		wizard.getConnectionFactory().setText("#MyFactory");
 		wizard.getConcurrentConsumers().setText("3");
 		wizard.getMaximumConcurrentConsumers().setText("7");
 		wizard.getReplyTo().setText("reply-to");
 		wizard.getSelector().setText("selector");
 		wizard.getTransactionManager().setText("MyTX");
 		wizard.getTransacted().toggle(true);
 		wizard.getTransacted().toggle(false);
 		wizard.setOperationSelector(OPERATION_NAME, METHOD);
 		wizard.finish();
 
 		new SwitchYardEditor().save();
 
 		String bindingPath = "/switchyard/composite/service/binding.jms";
 		assertEquals("jms-binding", editor.xpath(bindingPath + "/@name"));
 		assertEquals(METHOD, editor.xpath(bindingPath + "/operationSelector/@operationName"));
 		assertEquals("mytopic", editor.xpath(bindingPath + "/topic"));
 		assertEquals("#MyFactory", editor.xpath(bindingPath + "/connectionFactory"));
 		assertEquals("3", editor.xpath(bindingPath + "/concurrentConsumers"));
 		assertEquals("7", editor.xpath(bindingPath + "/maxConcurrentConsumers"));
 		assertEquals("reply-to", editor.xpath(bindingPath + "/replyTo"));
 		assertEquals("selector", editor.xpath(bindingPath + "/selector"));
 		assertEquals("false", editor.xpath(bindingPath + "/transacted"));
 		assertEquals("MyTX", editor.xpath(bindingPath + "/transactionManager"));
 
 		BindingsPage properties = new Service(SERVICE).showProperties().selectBindings();
 		JMSBindingPage page = properties.selectJMSBinding("jms-binding");
 		assertEquals("jms-binding", page.getName());
 		assertEquals("mytopic", page.getQueueTopicName());
 		properties.ok();
 	}
 
 	@Test
 	public void jpaBindingTest() throws Exception {
 		new Service(SERVICE).addBinding("JPA");
 		JPABindingPage wizard = new JPABindingPage();
 		assertEquals("jpa1", wizard.getName());
 		wizard.setName("jpa-binding");
 		wizard.getEntityClassName().setText("EClass.java");
 		wizard.getPersistenceUnit().setText("persistence.xml");
 		wizard.getTransactionManager().setText("myTX");
 		wizard.getDelete().toggle(false);
 		wizard.getDelete().toggle(true);
 		wizard.getLockEntity().toggle(false);
 		wizard.getLockEntity().toggle(true);
 		wizard.getMaximumResults().setText("5");
 		wizard.getQuery().setText("query");
 		wizard.getNamedQuery().setText("named-query");
 		wizard.getNativeQuery().setText("native-query");
 		wizard.getTransacted().toggle(false);
 		wizard.getTransacted().toggle(true);
 		wizard.finish();
 
 		new SwitchYardEditor().save();
 
 		String bindingPath = "/switchyard/composite/service/binding.jpa";
 		assertEquals("jpa-binding", editor.xpath(bindingPath + "/@name"));
 		assertEquals("EClass.java", editor.xpath(bindingPath + "/entityClassName"));
 		assertEquals("persistence.xml", editor.xpath(bindingPath + "/persistenceUnit"));
 		assertEquals("myTX", editor.xpath(bindingPath + "/transactionManager"));
 		assertEquals("true", editor.xpath(bindingPath + "/consume/consumeDelete"));
 		assertEquals("true", editor.xpath(bindingPath + "/consume/consumeLockEntity"));
 		assertEquals("5", editor.xpath(bindingPath + "/consume/maximumResults"));
 		assertEquals("query", editor.xpath(bindingPath + "/consume/consumer.query"));
 		assertEquals("named-query", editor.xpath(bindingPath + "/consume/consumer.namedQuery"));
 		assertEquals("native-query", editor.xpath(bindingPath + "/consume/consumer.nativeQuery"));
 		assertEquals("true", editor.xpath(bindingPath + "/consume/consumer.transacted"));
 
 		BindingsPage properties = new Service(SERVICE).showProperties().selectBindings();
 		JPABindingPage page = properties.selectJPABinding("jpa-binding");
 		assertEquals("jpa-binding", page.getName());
 		assertEquals("EClass.java", page.getEntityClassName().getText());
 		assertEquals("persistence.xml", page.getPersistenceUnit().getText());
 		properties.ok();
 	}
 
 	@Test
 	public void mqttBindingTest() throws Exception {
 		new Service(SERVICE).addBinding("MQTT");
 		MQTTBindingPage wizard = new MQTTBindingPage();
 		wizard.setName("mqtt-binding");
 		wizard.getHostURI().setText("tcp://localhost:1883");
 		wizard.getSubscribeTopicName().setText("topicName");
 		wizard.getConnectAttemptsMax().setText("111");
 		wizard.getReconnectAttemptsMax().setText("222");
 		wizard.getQualityofService().setSelection(QOS_EXACTLY_ONCE);
 		wizard.finish();
 
 		new SwitchYardEditor().save();
 
 		String bindingPath = "/switchyard/composite/service/binding.mqtt";
 		assertEquals("mqtt-binding", editor.xpath(bindingPath + "/@name"));
 		assertEquals("tcp://localhost:1883", editor.xpath(bindingPath + "/host"));
 		assertEquals("111", editor.xpath(bindingPath + "/connectAttemptsMax"));
 		assertEquals("222", editor.xpath(bindingPath + "/reconnectAttemptsMax"));
 		assertEquals(QOS_EXACTLY_ONCE, editor.xpath(bindingPath + "/qualityOfService"));
 		assertEquals("topicName", editor.xpath(bindingPath + "/subscribeTopicName"));
 
 		BindingsPage properties = new Service(SERVICE).showProperties().selectBindings();
 		MQTTBindingPage page = properties.selectMQTTBinding("mqtt-binding");
 		assertEquals("mqtt-binding", page.getName());
 		properties.ok();
 	}
 
 	@Test
 	public void mailBindingTest() throws Exception {
 		new Service(SERVICE).addBinding("Mail");
 		MailBindingPage wizard = new MailBindingPage();
 		assertEquals("mail1", wizard.getName());
 		wizard.setName("mail-binding");
 		wizard.getHost().setText("localhost");
 		wizard.getPort().setText("1234");
 		wizard.getUserName().setText("admin");
 		wizard.getPassword().setText("admin123$");
 		wizard.getSecured().toggle(false);
 		wizard.getSecured().toggle(true);
 		wizard.getAccountType().setSelection(ACCOUNT_TYPE_IMAP);
 		wizard.getFolderName().setText("inbox");
 		wizard.getFetchSize().setText("3");
 		wizard.getUnreadOnly().toggle(false);
 		wizard.getUnreadOnly().toggle(true);
 		wizard.getDelete().toggle(false);
 		wizard.getDelete().toggle(true);
 		wizard.setOperationSelector(OPERATION_NAME, METHOD);
 		wizard.finish();
 
 		new SwitchYardEditor().save();
 		String bindingPath = "/switchyard/composite/service/binding.mail";
 		assertEquals("mail-binding", editor.xpath(bindingPath + "/@name"));
 		assertEquals("true", editor.xpath(bindingPath + "/@secure"));
 		assertEquals("sayHello", editor.xpath(bindingPath + "/operationSelector/@operationName"));
 		assertEquals("localhost", editor.xpath(bindingPath + "/host"));
 		assertEquals("1234", editor.xpath(bindingPath + "/port"));
 		assertEquals("admin", editor.xpath(bindingPath + "/username"));
 		assertEquals("admin123$", editor.xpath(bindingPath + "/password"));
 		assertEquals("imap", editor.xpath(bindingPath + "/consume/@accountType"));
 		assertEquals("inbox", editor.xpath(bindingPath + "/consume/folderName"));
 		assertEquals("3", editor.xpath(bindingPath + "/consume/fetchSize"));
 		assertEquals("true", editor.xpath(bindingPath + "/consume/delete"));
 
 		BindingsPage properties = new Service(SERVICE).showProperties().selectBindings();
 		MailBindingPage page = properties.selectMailBinding("mail-binding");
 		assertEquals("mail-binding", page.getName());
 		assertEquals("localhost", page.getHost().getText());
 		properties.ok();
 	}
 
 	@Test
 	public void nettyTcpBindingTest() throws Exception {
 		new Service(SERVICE).addBinding("Netty TCP");
 		NettyTCPBindingPage wizard = new NettyTCPBindingPage();
 		assertEquals("tcp1", wizard.getName());
 		wizard.setName("tcp-binding");
 		wizard.getHost().setText("tcp-host");
 		wizard.getPort().setText("1234");
 		wizard.setOperationSelector(OPERATION_NAME, METHOD);
 		wizard.finish();
 
 		new SwitchYardEditor().save();
 
 		String bindingPath = "/switchyard/composite/service/binding.tcp";
 		assertEquals("tcp-binding", editor.xpath(bindingPath + "/@name"));
 		assertEquals("sayHello", editor.xpath(bindingPath + "/operationSelector/@operationName"));
 		assertEquals("tcp-host", editor.xpath(bindingPath + "/host"));
 		assertEquals("1234", editor.xpath(bindingPath + "/port"));
 
 		BindingsPage properties = new Service(SERVICE).showProperties().selectBindings();
 		NettyTCPBindingPage page = properties.selectNettyTCPBinding("tcp-binding");
 		assertEquals("tcp-binding", page.getName());
 		assertEquals("tcp-host", page.getHost().getText());
 		assertEquals("1234", page.getPort().getText());
 		properties.ok();
 	}
 
 	@Test
 	public void nettyUdpBindingTest() throws Exception {
 		new Service(SERVICE).addBinding("Netty UDP");
 		NettyUDPBindingPage wizard = new NettyUDPBindingPage();
 		assertEquals("udp1", wizard.getName());
 		wizard.setName("udp-binding");
 		wizard.getHost().setText("udp-host");
 		wizard.getPort().setText("1234");
 		wizard.getBroadcast().toggle(false);
 		wizard.getBroadcast().toggle(true);
 		wizard.setOperationSelector(OPERATION_NAME, METHOD);
 		wizard.finish();
 
 		new SwitchYardEditor().save();
 
 		String bindingPath = "/switchyard/composite/service/binding.udp";
 		assertEquals("udp-binding", editor.xpath(bindingPath + "/@name"));
 		assertEquals("sayHello", editor.xpath(bindingPath + "/operationSelector/@operationName"));
 		assertEquals("udp-host", editor.xpath(bindingPath + "/host"));
 		assertEquals("1234", editor.xpath(bindingPath + "/port"));
 		assertEquals("true", editor.xpath(bindingPath + "/broadcast"));
 
 		BindingsPage properties = new Service(SERVICE).showProperties().selectBindings();
 		NettyUDPBindingPage page = properties.selectNettyUDPBinding("udp-binding");
 		assertEquals("udp-binding", page.getName());
 		assertEquals("udp-host", page.getHost().getText());
 		assertEquals("1234", page.getPort().getText());
 		properties.ok();
 	}
 
 	@Test
 	public void restBindingTest() throws Exception {
 		new Service(SERVICE).addBinding("REST");
 		RESTBindingPage wizard = new RESTBindingPage();
 		assertEquals("rest1", wizard.getName());
 		wizard.setName("rest-binding");
 		wizard.getContextPath().setText("rest-context");
 		wizard.addInterface("Hello");
 		wizard.finish();
 
 		new SwitchYardEditor().save();
 
 		String bindingPath = "/switchyard/composite/service/binding.rest";
 		assertEquals("rest-binding", editor.xpath(bindingPath + "/@name"));
 		assertEquals(PACKAGE + ".Hello", editor.xpath(bindingPath + "/interfaces"));
 		assertEquals("rest-context", editor.xpath(bindingPath + "/contextPath"));
 
 		BindingsPage properties = new Service(SERVICE).showProperties().selectBindings();
 		RESTBindingPage page = properties.selectRESTBinding("rest-binding");
 		assertEquals("rest-binding", page.getName());
 		assertEquals("rest-context", page.getContextPath().getText());
 		assertTrue(page.getInterfaces().contains(PACKAGE + ".Hello"));
 		properties.ok();
 	}
 
 	@Test
 	public void rssBindingTest() throws Exception {
 		String time = "2015-01-01T00:00:00";
 
 		new Service(SERVICE).addBinding("RSS");
 		RSSBindingPage wizard = new RSSBindingPage();
 		wizard.setName("rss-binding");
 		wizard.getFeedURI().setText("http://localhost");
 		wizard.getSplitEntries().toggle(false);
 		wizard.getSplitEntries().toggle(true);
 		wizard.getFilter().toggle(false);
 		wizard.getFilter().toggle(true);
 		wizard.getLastUpdateStartingTimestamp().setText(time);
 		wizard.getSortEntriesbyDate().toggle(false);
 		wizard.getSortEntriesbyDate().toggle(true);
 		wizard.getDelayBetweenPolls().setText("1234");
 		wizard.finish();
 
 		new SwitchYardEditor().save();
 
 		String bindingPath = "/switchyard/composite/service/binding.rss";
 		assertEquals("rss-binding", editor.xpath(bindingPath + "/@name"));
 		assertEquals("http://localhost", editor.xpath(bindingPath + "/feedURI"));
 		assertEquals("true", editor.xpath(bindingPath + "/splitEntries"));
 		assertEquals("true", editor.xpath(bindingPath + "/filter"));
 		assertEquals("2015-01-01T00:00:00", editor.xpath(bindingPath + "/lastUpdate"));
 		assertEquals("true", editor.xpath(bindingPath + "/sortEntries"));
 		assertEquals("1234", editor.xpath(bindingPath + "/consume/delay"));
 
 		BindingsPage properties = new Service(SERVICE).showProperties().selectBindings();
 		RSSBindingPage page = properties.selectRSSBinding("rss-binding");
 		assertEquals("rss-binding", page.getName());
 		properties.ok();
 	}
 
 	@Test
 	public void sapBindingTest() throws Exception {
 		new Service(SERVICE).addBinding("SAP");
 		SAPBindingPage wizard = new SAPBindingPage();
 		wizard.setName("sap-binding");
 		wizard.getServer().setText("localhost");
 		wizard.getRFCName().setText("rfcName");
 		wizard.getTransacted().toggle(false);
 		wizard.getTransacted().toggle(true);
 		wizard.finish();
 
 		new SwitchYardEditor().save();
 
 		String bindingPath = "/switchyard/composite/service/binding.sap";
 		assertEquals("sap-binding", editor.xpath(bindingPath + "/@name"));
 		assertEquals("localhost", editor.xpath(bindingPath + "/server"));
 		assertEquals("rfcName", editor.xpath(bindingPath + "/rfcName"));
 		assertEquals("true", editor.xpath(bindingPath + "/transacted"));
 
 		BindingsPage properties = new Service(SERVICE).showProperties().selectBindings();
 		SAPBindingPage page = properties.selectSAPBinding("sap-binding");
 		assertEquals("sap-binding", page.getName());
 		properties.ok();
 	}
 
 	@Test
 	public void scaBindingTest() throws Exception {
 		new Service(SERVICE).addBinding("SCA");
 		SCABindingPage wizard = new SCABindingPage();
 		assertEquals("sca1", wizard.getName());
 		wizard.setName("sca-binding");
 		wizard.getClustered().toggle(false);
 		wizard.getClustered().toggle(true);
 		wizard.finish();
 
 		new SwitchYardEditor().save();
 
 		String bindingPath = "/switchyard/composite/service/binding.sca";
 		assertEquals("sca-binding", editor.xpath(bindingPath + "/@name"));
 		assertEquals("true", editor.xpath(bindingPath + "/@clustered"));
 
 		BindingsPage properties = new Service(SERVICE).showProperties().selectBindings();
 		SCABindingPage page = properties.selectSCABinding("sca-binding");
 		assertEquals("sca-binding", page.getName());
 		assertTrue(page.getClustered().isChecked());
 		properties.ok();
 	}
 
 	@Test
 	public void sftpBindingTest() throws Exception {
 		new Service(SERVICE).addBinding("SFTP");
 		SFTPBindingPage wizard = new SFTPBindingPage();
 		assertEquals("sftp1", wizard.getName());
 		wizard.setName("sftp-binding");
 		wizard.getHost().setText("localhost");
 		wizard.getPort().setText("1234");
 		wizard.getUserName().setText("admin");
 		wizard.getPassword().setText("admin123$");
 		wizard.getUseBinaryTransferMode().toggle(false);
 		wizard.getUseBinaryTransferMode().toggle(true);
 		wizard.getDirectory().setText("sftp-directory");
 		wizard.getFileName().setText("test.txt");
 		wizard.getAutoCreateMissingDirectoriesinFilePath().toggle(false);
 		wizard.getAutoCreateMissingDirectoriesinFilePath().toggle(true);
 		wizard.getInclude().setText("in");
 		wizard.getExclude().setText("ex");
 		wizard.getDeleteFilesOnceProcessed().toggle(false);
 		wizard.getDeleteFilesOnceProcessed().toggle(true);
 		wizard.getProcessSubDirectoriesRecursively().toggle(false);
 		wizard.getProcessSubDirectoriesRecursively().toggle(true);
 		wizard.setOperationSelector(OPERATION_NAME, METHOD);
 		wizard.next();
 		wizard.getPreMove().setText("preMove");
 		wizard.getMove().setText("move");
 		wizard.getMoveFailed().setText("moveFailed");
 		wizard.getDelayBetweenPolls().setText("1000");
 		wizard.getMaxMessagesPerPoll().setText("2");
 		wizard.next();
 		wizard.getPrivateKeyFile().setText("private.key");
 		wizard.getPrivateKeyFilePassphrase().setText("secret");
 		wizard.finish();
 
 		new SwitchYardEditor().save();
 		String bindingPath = "/switchyard/composite/service/binding.sftp";
 		assertEquals("sftp-binding", editor.xpath(bindingPath + "/@name"));
 		assertEquals("sayHello", editor.xpath(bindingPath + "/operationSelector/@operationName"));
 		assertEquals("sftp-directory", editor.xpath(bindingPath + "/directory"));
 		assertEquals("true", editor.xpath(bindingPath + "/autoCreate"));
 		assertEquals("localhost", editor.xpath(bindingPath + "/host"));
 		assertEquals("1234", editor.xpath(bindingPath + "/port"));
 		assertEquals("admin", editor.xpath(bindingPath + "/username"));
 		assertEquals("admin123$", editor.xpath(bindingPath + "/password"));
 		assertEquals("true", editor.xpath(bindingPath + "/binary"));
 		assertEquals("private.key", editor.xpath(bindingPath + "/privateKeyFile"));
 		assertEquals("secret", editor.xpath(bindingPath + "/privateKeyFilePassphrase"));
 		assertEquals("true", editor.xpath(bindingPath + "/consume/delete"));
 		assertEquals("true", editor.xpath(bindingPath + "/consume/recursive"));
 		assertEquals("preMove", editor.xpath(bindingPath + "/consume/preMove"));
 		assertEquals("move", editor.xpath(bindingPath + "/consume/move"));
 		assertEquals("moveFailed", editor.xpath(bindingPath + "/consume/moveFailed"));
 		assertEquals("in", editor.xpath(bindingPath + "/consume/include"));
 		assertEquals("ex", editor.xpath(bindingPath + "/consume/exclude"));
 		assertEquals("2", editor.xpath(bindingPath + "/consume/maxMessagesPerPoll"));
 		assertEquals("1000", editor.xpath(bindingPath + "/consume/delay"));
 
 		BindingsPage properties = new Service(SERVICE).showProperties().selectBindings();
 		SFTPBindingPage page = properties.selectSFTPBinding("sftp-binding");
 		assertEquals("sftp-binding", page.getName());
 		assertEquals("sftp-directory", page.getDirectory().getText());
 		properties.ok();
 	}
 
 	@Test
 	public void soapBindingTest() throws Exception {
 		new Service(SERVICE).addBinding("SOAP");
 		SOAPBindingPage wizard = new SOAPBindingPage();
 		assertEquals("soap1", wizard.getName());
 		wizard.setName("soap-binding");
 		wizard.getContextPath().setText("soap-context");
 		wizard.setWsdlURI("hello.wsdl");
 		wizard.getWSDLPort().setText("1234");
 		wizard.getServerPort().setText("4321");
 		wizard.getUnwrappedPayload().toggle(false);
 		wizard.getUnwrappedPayload().toggle(true);
 		wizard.getSOAPHeadersType().setSelection(SOAP_HEADERS_TYPE_DOM);
 		wizard.getConfigFile().setText("soap.conf");
 		wizard.getConfigName().setText("configName");
 		wizard.getEnable().toggle(false);
 		wizard.getEnable().toggle(true);
 		wizard.getTemporarilyDisable().toggle(false);
 		wizard.getTemporarilyDisable().toggle(true);
 		wizard.getxopExpand().toggle(false);
 		wizard.getxopExpand().toggle(true);
 		wizard.getThreshold().setText("963");
 		wizard.finish();
 
 		new SwitchYardEditor().save();
 		String bindingPath = "/switchyard/composite/service/binding.soap";
 		assertEquals("soap-binding", editor.xpath(bindingPath + "/@name"));
 		assertEquals("DOM", editor.xpath(bindingPath + "/contextMapper/@soapHeadersType"));
 		assertEquals("true", editor.xpath(bindingPath + "/messageComposer/@unwrapped"));
 		assertEquals("hello.wsdl", editor.xpath(bindingPath + "/wsdl"));
 		assertEquals("1234", editor.xpath(bindingPath + "/wsdlPort"));
 		assertEquals(":4321", editor.xpath(bindingPath + "/socketAddr"));
 		assertEquals("soap-context", editor.xpath(bindingPath + "/contextPath"));
 		assertEquals("soap.conf", editor.xpath(bindingPath + "/endpointConfig/@configFile"));
 		assertEquals("configName", editor.xpath(bindingPath + "/endpointConfig/@configName"));
 		assertEquals("true", editor.xpath(bindingPath + "/mtom/@enabled"));
 		assertEquals("963", editor.xpath(bindingPath + "/mtom/@threshold"));
 		assertEquals("true", editor.xpath(bindingPath + "/mtom/@xopExpand"));
 
 		BindingsPage properties = new Service(SERVICE).showProperties().selectBindings();
 		SOAPBindingPage page = properties.selectSOAPBinding("soap-binding");
 		assertEquals("soap-binding", page.getName());
 		assertEquals("soap-context", page.getContextPath().getText());
 		properties.ok();
 	}
 
 	@Test
 	public void sqlBindingTest() throws Exception {
 		new Service(SERVICE).addBinding("SQL");
 		SQLBindingPage wizard = new SQLBindingPage();
 		assertEquals("sql1", wizard.getName());
 		wizard.setName("sql-binding");
 		wizard.getQuery().setText("sql-query");
 		wizard.getDataSource().setText("data-source");
 		wizard.getPlaceholder().setText("place-holder");
 		wizard.getPeriod().setText("10");
 		wizard.getInitialDelayMS().setText("1234");
 		wizard.setOperationSelector(OPERATION_NAME, METHOD);
 		wizard.finish();
 
 		new SwitchYardEditor().save();
 
 		String bindingPath = "/switchyard/composite/service/binding.sql";
 		assertEquals("sql-binding", editor.xpath(bindingPath + "/@name"));
 		assertEquals("1234", editor.xpath(bindingPath + "/@initialDelay"));
 		assertEquals("10", editor.xpath(bindingPath + "/@period"));
 		assertEquals("sayHello", editor.xpath(bindingPath + "/operationSelector/@operationName"));
 		assertEquals("sql-query", editor.xpath(bindingPath + "/query"));
 		assertEquals("data-source", editor.xpath(bindingPath + "/dataSourceRef"));
 		assertEquals("place-holder", editor.xpath(bindingPath + "/placeholder"));
 
 		BindingsPage properties = new Service(SERVICE).showProperties().selectBindings();
 		SQLBindingPage page = properties.selectSQLBinding("sql-binding");
 		assertEquals("sql-binding", page.getName());
 		assertEquals("sql-query", page.getQuery().getText());
 		assertEquals("data-source", page.getDataSource().getText());
 		properties.ok();
 	}
 
 	@Test
 	public void schedulingBindingTest() throws Exception {
 		String cron = "0 0 12 * * ?";
 		String startTime = "2014-01-01T00:00:00";
 		String endTime = "2015-01-01T00:00:00";
 
 		new Service(SERVICE).addBinding("Scheduling");
 		SchedulingBindingPage wizard = new SchedulingBindingPage();
 		wizard.setName("schedule-binding");
 		wizard.getSchedulingType().setSelection(SCHEDULING_TYPE_CRON);
 		wizard.getCron().setText(cron);
 		wizard.getStartTime().setText(startTime);
 		wizard.getEndTime().setText(endTime);
 		wizard.getTimeZone().setSelection("Europe/Prague");
 		wizard.setOperationSelector(OPERATION_NAME, METHOD);
 		wizard.finish();
 
 		new SwitchYardEditor().save();
 		String bindingPath = "/switchyard/composite/service/binding.quartz";
 		assertEquals("sayHello", editor.xpath(bindingPath + "/operationSelector/@operationName"));
 		assertEquals("schedule-binding", editor.xpath(bindingPath + "/name"));
 		assertEquals("0 0 12 * * ?", editor.xpath(bindingPath + "/cron"));
 		assertEquals("2014-01-01T00:00:00", editor.xpath(bindingPath + "/trigger.startTime"));
 		assertEquals("2015-01-01T00:00:00", editor.xpath(bindingPath + "/trigger.endTime"));
 		assertEquals("Europe/Prague", editor.xpath(bindingPath + "/trigger.timeZone"));
 
 		BindingsPage properties = new Service(SERVICE).showProperties().selectBindings();
 		SchedulingBindingPage page = properties.selectSchedulingBinding("HelloService1");
 		assertEquals(cron, page.getCron().getText());
 		assertEquals(OPERATION_NAME, page.getOperationSelector());
 		assertEquals(METHOD, page.getOperationSelectorValue());
 		assertEquals(startTime, page.getStartTime().getText());
 		assertEquals(endTime, page.getEndTime().getText());
 		properties.ok();
 	}
 }
