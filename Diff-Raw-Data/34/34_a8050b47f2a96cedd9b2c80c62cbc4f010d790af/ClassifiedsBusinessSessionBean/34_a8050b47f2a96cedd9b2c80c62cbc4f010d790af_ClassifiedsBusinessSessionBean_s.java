 package net.java.dev.cejug.classifieds.server.ejb3.bean;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 import javax.interceptor.AroundInvoke;
 import javax.interceptor.InvocationContext;
 import javax.xml.datatype.DatatypeConfigurationException;
 import javax.xml.datatype.DatatypeFactory;
 import javax.xml.ws.WebServiceException;
 
 import net.java.dev.cejug.classifieds.server.ejb3.entity.AdvertisementEntity;
 import net.java.dev.cejug.classifieds.server.ejb3.entity.CustomerEntity;
 import net.java.dev.cejug.classifieds.server.ejb3.entity.OperationTimestampEntity;
 import net.java.dev.cejug.classifieds.server.ejb3.entity.PublishingPeriodEntity;
 import net.java.dev.cejug.classifieds.server.ejb3.entity.PublishingPeriodEntity.PeriodState;
 import net.java.dev.cejug.classifieds.server.ejb3.entity.facade.AdvertisementFacadeLocal;
 import net.java.dev.cejug.classifieds.server.ejb3.entity.facade.CustomerFacadeLocal;
 import net.java.dev.cejug.classifieds.server.ejb3.entity.facade.OperationTimeKeeperLocal;
 import net.java.dev.cejug.classifieds.server.ejb3.entity.facade.RssChannelFacadeLocal;
 import net.java.dev.cejug.classifieds.server.generated.contract.Advertisement;
 import net.java.dev.cejug.classifieds.server.generated.contract.AdvertisementHeader;
 import net.java.dev.cejug.classifieds.server.generated.contract.AtomCollection;
 import net.java.dev.cejug.classifieds.server.generated.contract.AtomFilterCollection;
 import net.java.dev.cejug.classifieds.server.generated.contract.Channel;
 import net.java.dev.cejug.classifieds.server.generated.contract.RssCollection;
 import net.java.dev.cejug.classifieds.server.generated.contract.RssFilterCollection;
 import net.java.dev.cejug.classifieds.server.generated.contract.ServiceStatus;
 import net.java.dev.cejug.classifieds.server.generated.contract.SpamReport;
 import net.java.dev.cejug.classifieds.server.handler.TimeKeeperSoapHandler;
 
 /**
  * @WebService(name = "CejugClassifiedsBusiness", targetNamespace =
  *                  "http://cejug-classifieds.dev.java.net/business")
  * @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.WRAPPED)
  */
 
 @Stateless
 public class ClassifiedsBusinessSessionBean implements
 		ClassifiedsBusinessRemote {
 	@EJB
 	OperationTimeKeeperLocal timeKeeperFacade;
 
 	@EJB
 	AdvertisementFacadeLocal publishFacade;
 
 	@EJB
 	RssChannelFacadeLocal rssFacade;
 
 	@EJB
 	CustomerFacadeLocal customerFacade;
 
 	/**
 	 * the global log manager, used to allow third party services to override
 	 * the defult logger.
 	 */
 	private static Logger logger = Logger.getLogger(TimeKeeperSoapHandler.class
 			.getName(), "i18n/log");
 
 	private final DatatypeFactory factory;
 
 	public ClassifiedsBusinessSessionBean() {
 		try {
 			factory = DatatypeFactory.newInstance();
 		} catch (DatatypeConfigurationException e) {
 			// TODO: log
 			logger.severe(e.getMessage());
 			throw new WebServiceException(e);
 		}
 	}
 
 	@Override
 	public AtomCollection loadAtomOperation(AtomFilterCollection filter) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public RssCollection loadRssOperation(RssFilterCollection filter) {
 		try {
 			Channel channel = rssFacade.get(null);
 			RssCollection col = new RssCollection();
 			col.getRssCollection().add(channel);
 			return col;
 		} catch (Exception e) {
 			// TODO: log
 			logger.severe(e.getMessage());
 			throw new WebServiceException(e);
 		}
 	}
 
 	@Override
 	public ServiceStatus reportSpamOperation(SpamReport spam) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	// interceptor method within the bean (the bean is the aspect)
 	@AroundInvoke
 	public Object timerLog(InvocationContext ctx) throws Exception {
 		// TODO: include timezone...
 		Date start = new Date();
 		try {
 			return ctx.proceed();
 		} finally {
 			try {
 				OperationTimestampEntity stamp = new OperationTimestampEntity();
 				stamp.setOperationName(ctx.getMethod().getName());
 				stamp.setStart(start);
 				stamp.setFinish(new Date());
 				stamp.setStatus(true);
 				stamp.setClientId("kk");
 				timeKeeperFacade.update(stamp);
 			} catch (Exception error) {
 
 			}
 		}
 	}
 
 	@Override
 	public ServiceStatus publishOperation(Advertisement advertisement,
 			AdvertisementHeader header) {
 		try {
 			// loading customer
 			Map<String, String> params = new HashMap<String, String>();
 			params.clear();
			params.put("l", header.getCustomerLogin());
 			params.put("d", header.getCustomerDomain());
 			CustomerEntity customer = customerFacade.get(params);
 
 			// validating advertisement PIN
 
 			AdvertisementEntity entity = new AdvertisementEntity();
 			entity.setKeywords(advertisement.getKeywords());
 			entity.setText(advertisement.getFullText());
 			entity.setPublisher(customer);
 			entity.setSummary(advertisement.getShortDescription());
 			entity.setTitle(advertisement.getHeadline());
 			PublishingPeriodEntity period = new PublishingPeriodEntity();
 			period.setDay(new Date(advertisement.getPublishingStart()
 					.getMillisecond()));
 			period.setState(PeriodState.NEW);
 
 			Collection<PublishingPeriodEntity> c = new ArrayList<PublishingPeriodEntity>();
 			c.add(period);
 			entity.setPublishingPeriod(c);
 			publishFacade.create(entity);
 
 			ServiceStatus status = new ServiceStatus();
 			status.setDescription("OK");
 			status.setCode(202);
 
 			status
 					.setTimestamp(factory
 							.newXMLGregorianCalendar((GregorianCalendar) GregorianCalendar
 									.getInstance()));
 
 			return status;
 		} catch (Exception e) {
 			e.printStackTrace();
 			logger.severe(e.getMessage());
 			throw new WebServiceException(e);
 		}
 
 	}
 }
