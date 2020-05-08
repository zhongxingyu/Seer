 package pt.com.broker.monitorization.db;
 
 import java.io.ByteArrayInputStream;
 import java.util.Date;
 
 import javax.xml.XMLConstants;
 import javax.xml.namespace.NamespaceContext;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathFactory;
 
 import org.caudexorigo.ErrorAnalyser;
 import org.caudexorigo.jdbc.DbExecutor;
 import org.caudexorigo.text.StringEscapeUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 public class FaultsDB
 {
 	private static Logger log = LoggerFactory.getLogger(FaultsDB.class);
 
 	/*
 	 * 
 	 * CREATE TABLE IF NOT EXISTS fault(id INT PRIMARY KEY AUTO_INCREMENT, agentName VARCHAR(255) NOT NULL, time TIMESTAMP NOT NULL, message VARCHAR(8192), shortmessage VARCHAR(255));
 	 */
 
 
 	public static void add(String agent, Date sampleDate, String message)
 	{
 		if (log.isDebugEnabled())
 		{
 			log.debug(String.format("FaultsDB.processItem(%s, %s, %s)", agent, sampleDate, message));
 		}
 
 		try
 		{
 
 			String ins_sql = ("INSERT INTO fault_data (agent_name, event_time, message, short_message) VALUES (?, ?, ?, ?)");
 
 			ErrorInfo errorInfo = extractShortMessage(message);
 
 			String escapedMsg = StringEscapeUtils.escapeHtml(errorInfo.content.replace("\n", "\\n"));
 
 			String shortMessage = StringEscapeUtils.escapeHtml(errorInfo.shortMessage.replace("\n", "\\n"));
 
 			DbExecutor.runActionPreparedStatement(ins_sql, agent, sampleDate, escapedMsg, shortMessage);
 		}
 		catch (Throwable t)
 		{
 			log.error("Failed to insert new information item.", t);
 		}
 
 	}
 
 
 	private static class SoapFaultNS implements NamespaceContext
 	{
 		private static SoapFaultNS instance = new SoapFaultNS();
 
 		public static SoapFaultNS getInstance()
 		{
 			return instance;
 		}
 
 		public String getNamespaceURI(String prefix)
 		{
 			if (prefix.equals("soap"))
 				return "http://www.w3.org/2003/05/soap-envelope";
 			else
 				return XMLConstants.NULL_NS_URI;
 		}
 
 		public String getPrefix(String namespace)
 		{
 			if (namespace.equals("http://www.w3.org/2003/05/soap-envelope"))
 				return "soap";
 			else
 				return null;
 		}
 
 		public java.util.Iterator<Object> getPrefixes(String namespace)
 		{
 			return null;
 		}
 	}
 
 	private static class ErrorInfo
 	{
 		public String content;
 		public String shortMessage;
 
 		public ErrorInfo(String shortMessage, String content)
 		{
 			this.content = content;
 			this.shortMessage = shortMessage;
 		}
 	}
 
 	private static ErrorInfo extractShortMessage(String message)
 	{
 
 		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		String shortMessage = "";
 		String content = message;
 
 		try
 		{
 			DocumentBuilder documentBuilder = docBuilderFactory.newDocumentBuilder();
 			Document doc = documentBuilder.parse(new ByteArrayInputStream(message.getBytes()));
 
 			XPath xpath = XPathFactory.newInstance().newXPath();
 
 			xpath.setNamespaceContext(SoapFaultNS.getInstance());
 
 			NodeList n_code = (NodeList) xpath.evaluate("/Envelope/Body/Fault/Reason/Text", doc, XPathConstants.NODESET);
 
 			if (n_code != null && n_code.getLength() > 0)
 			{
 				Element codeElem = (Element) n_code.item(0);
 				shortMessage = codeElem.getTextContent();
 			}
 
 			NodeList n_detail = (NodeList) xpath.evaluate("/Envelope/Body/Fault/Detail", doc, XPathConstants.NODESET);
 
 			if (n_detail != null && n_detail.getLength() > 0)
 			{
 				Element contentElem = (Element) n_detail.item(0);
 				content = contentElem.getTextContent();
 			}
 
 		}
 		catch (Throwable t)
 		{
 			Throwable r = ErrorAnalyser.findRootCause(t);
 			log.error(r.getMessage(), r);
			shortMessage = String.format("Unknown - (failed to extract data from Fault message. Reason: %s)", r.getMessage() );
 		}
 
 		return new ErrorInfo(shortMessage, content);
 	}
 }
