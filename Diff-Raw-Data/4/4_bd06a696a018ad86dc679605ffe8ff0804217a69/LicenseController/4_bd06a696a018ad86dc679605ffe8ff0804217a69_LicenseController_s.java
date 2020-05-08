 package com.digt.web;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.text.DateFormat;
 import java.util.Calendar;
 import java.util.Locale;
 import java.util.concurrent.ExecutionException;
 
 import javax.annotation.PostConstruct;
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 import javax.mail.internet.MimeMessage.RecipientType;
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.shindig.auth.AuthInfo;
 import org.apache.shindig.auth.SecurityToken;
 import org.apache.shindig.common.servlet.GuiceServletContextListener;
 import org.apache.shindig.protocol.ProtocolException;
 import org.apache.shindig.social.opensocial.model.Person;
 import org.apache.shindig.social.opensocial.spi.PersonService;
 import org.apache.shindig.social.opensocial.spi.UserId;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import com.digt.jpa.LicenseDb;
 import com.digt.jpa.spi.LicenseService;
 import com.digt.model.License;
 import com.digt.reporting.LicenseReport;
 import com.digt.web.beans.ProductBean;
 import com.digt.web.beans.json.Message;
 import com.google.inject.Inject;
 import com.google.inject.Injector;
 import com.google.inject.Provider;
 
 import digt.com.license.DigtLicenseGenerator;
 
 @Controller
 @RequestMapping(value = "/license")
 public class LicenseController {
 	
 	@Inject
 	private Provider<Session> mailSession;
 	@Inject 
 	private PersonService personSvc;
 	@Inject 
 	private LicenseService licSvc;
 	@Autowired
 	private ProductBean prodBean;
 	@Autowired 
 	private ServletContext ctx;
 	
 	@Inject
 	private Provider<LicenseReport> licRepFactory;
 	
 	//private static final Logger LOG = Logger.getLogger(
 	//		LicenseController.class.getName());
 	private final static DateFormat DATE_FMT = DateFormat.getDateInstance(DateFormat.SHORT, new Locale("RU"));
 
 	@RequestMapping(value = "", method = RequestMethod.POST)
 	public @ResponseBody Message getLicense(HttpServletRequest request,
 			@RequestParam("prodId") int prodId) 
 					throws MessagingException, ProtocolException, 
 					InterruptedException, ExecutionException, IOException {
 		
		Message res = new Message(true, "Выполнено");
 		String[] product = prodBean.getProducts().get(prodId);
 		int flags = Integer.parseInt(product[3]);
 		SecurityToken token = new AuthInfo(request).getSecurityToken();
 		UserId userId = UserId.fromJson("@me");
 		License lic = licSvc.getLicense(userId, product[1], flags, token).get();
 		Person p = WebUtil.getPersonFromRequest(request, personSvc);
 		String template = "/WEB-INF/tpl/license_was_sent.tpl";
 		if (lic == null) {
 			DigtLicenseGenerator gen = new DigtLicenseGenerator(product[1], product[2]);
 			Calendar cal = Calendar.getInstance();
 			cal.add(Calendar.DATE, 30);
 			gen.setExpires(cal.getTime());
 			gen.setLicenseFlags(flags);
 			int serial = licSvc.getNextSerial(product[1], flags).get().intValue();
 			gen.setSerialNumber(serial);
 			String license = gen.getLicense();
 			
 			lic = new LicenseDb();
 			lic.setPerson(p);
 			lic.setGuid(product[1]);
 			lic.setLicense(license);
 			lic.setExpired(cal.getTime());
 			lic.setSerial(serial);
 			lic.setFlags(flags);
 			licSvc.store(lic);
 			template = "/WEB-INF/tpl/send_license.tpl";
 		}
 		
 		MimeMessage msg = new MimeMessage(mailSession.get());
 		msg.setRecipient(RecipientType.TO, new InternetAddress(p.getEmails().get(0).getValue()));
 		BufferedReader in = new BufferedReader(
 				new InputStreamReader(ctx.getResourceAsStream(template), WebUtil.MAIL_CHARSET));
 		msg.setSubject(in.readLine(), WebUtil.MAIL_CHARSET);
 		StringBuilder text = new StringBuilder();
 		String s;
 		while ((s = in.readLine()) != null) {
 			text.append(s).append("\n");
 		}
 		in.close();
 		String content = text.toString().replaceAll("\\{\\$license\\}", lic.getLicense())
 										.replaceAll("\\{\\$expires\\}", DATE_FMT.format(lic.getExpired()));
 		
 		msg.setContent(content, "text/html;charset="+WebUtil.MAIL_CHARSET);
 		Transport.send(msg);
 		return res;
 	}
 	
 	@PostConstruct
 	public void postInit()
 	{
 		Injector injector = (Injector)ctx.getAttribute(
 				GuiceServletContextListener.INJECTOR_ATTRIBUTE);
 		injector.injectMembers(this);
 		
 		LicenseReport licRep = licRepFactory.get();
 		if (licRep != null) {
 			licRep.setTemplateDir(ctx.getRealPath("/WEB-INF/tpl"));
 			licRep.setProdBean(prodBean);
 		}
 	}
 	
 }
