 package org.extensiblecatalog.ncip.v2.koha.util;
 
 import java.util.Properties;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.log4j.Logger;
 import org.extensiblecatalog.ncip.v2.common.ConnectorConfigurationFactory;
 import org.extensiblecatalog.ncip.v2.common.DefaultConnectorConfiguration;
 import org.extensiblecatalog.ncip.v2.koha.KohaLookupItemService;
 import org.extensiblecatalog.ncip.v2.service.RemoteServiceManager;
 import org.extensiblecatalog.ncip.v2.service.ServiceException;
 import org.extensiblecatalog.ncip.v2.service.ToolkitException;
 import org.xml.sax.SAXException;
 
 /**
  * KohaRemoteServiceManager just extends KohaMediator to interface with Koha X-Services for fulfilling NCIP requests
  *
  * @author Rick Johnson
  * @organization University of Notre Dame
  */
 public class KohaRemoteServiceManager extends KohaConnector implements RemoteServiceManager {
 
 	static Logger log = Logger.getLogger(KohaLookupItemService.class);
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 7115174311329481562L;
 
 	private KohaConfiguration kohaConfig = null;
 	{
 		try {
 			DefaultConnectorConfiguration config = (DefaultConnectorConfiguration) new ConnectorConfigurationFactory(new Properties()).getConfiguration();
 			kohaConfig = new KohaConfiguration(config);
 		} catch (ToolkitException e) {
 			throw new ExceptionInInitializerError(e);
 		}
 	}
 
 	public KohaRemoteServiceManager() throws ServiceException, ParserConfigurationException, SAXException {
 
 	}
 
 	public KohaRemoteServiceManager(Properties properties) throws ServiceException, ParserConfigurationException, SAXException {
 		this();
 		log.info("KohaRemoteServiceManager constructor called");
 	}
 
 	public String getCurrencyCode() {
		return kohaConfig.getProperty(KohaConstants.CONF_KOHA_CURRENCY_CODE);
 	}
 }
