 /*
  * Copyright (c) 2009. Orange Leap Inc. Active Constituent
  * Relationship Management Platform.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.orangeleap.tangerine.service.communication;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import javax.mail.MessagingException;
 import javax.mail.internet.MimeMessage;
 
 import net.sf.jasperreports.engine.JRException;
 import net.sf.jasperreports.engine.JasperExportManager;
 import net.sf.jasperreports.engine.JasperPrint;
 
 import org.apache.commons.logging.Log;
 import org.springframework.beans.BeansException;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.core.io.FileSystemResource;
 import org.springframework.mail.javamail.JavaMailSenderImpl;
 import org.springframework.mail.javamail.MimeMessageHelper;
 import org.springframework.validation.BindException;
 
 import com.jaspersoft.jasperserver.api.metadata.xml.domain.impl.ResourceDescriptor;
 import com.jaspersoft.jasperserver.api.metadata.xml.domain.impl.ResourceProperty;
 import com.jaspersoft.jasperserver.irplugin.JServer;
 import com.jaspersoft.jasperserver.irplugin.RepositoryReportUnit;
 import com.orangeleap.common.security.CasUtil;
 import com.orangeleap.tangerine.domain.CommunicationHistory;
 import com.orangeleap.tangerine.domain.Constituent;
 import com.orangeleap.tangerine.domain.Site;
 import com.orangeleap.tangerine.domain.communication.Email;
 import com.orangeleap.tangerine.domain.paymentInfo.Gift;
 import com.orangeleap.tangerine.domain.paymentInfo.Pledge;
 import com.orangeleap.tangerine.domain.paymentInfo.RecurringGift;
 import com.orangeleap.tangerine.service.CommunicationHistoryService;
 import com.orangeleap.tangerine.util.OLLogger;
 
 public class EmailService  {
 	protected final Log logger = OLLogger.getLog(getClass());
 
 	public final static String EMAIL_BODY = "_email_body";
 
 	private String baseUri = null;
 	private String repositoryUri = null;
 	private CommunicationHistoryService communicationHistoryService;
 
 	private File runReport(Site site,Map params,String templateName) {
 
 		File temp = null;
 		JServer jserver = new JServer();
 		jserver.setUsername(site.getJasperUserId());
 		jserver.setPassword(site.getJasperPassword());
 		jserver.setUrl(baseUri + repositoryUri);
 
 		try {
 
 			CasUtil.populateJserverWithCasCredentials(jserver, baseUri
 					+ "/j_acegi_cas_security_check");
 
 
 			JasperPrint print = jserver.getWSClient().runReport(
 					getReportUnit(jserver, templateName, params, site).getDescriptor(), params);
 
 			temp = File.createTempFile("orangeleap", ".pdf");
 			temp.deleteOnExit();
 			OutputStream out = new FileOutputStream(temp);
 			JasperExportManager.exportReportToPdfStream(print, out);
 			out.close();
 
 		} catch (JRException e) {
 
 			logger.error(e.getMessage());
 			return null;
 		} catch (Exception e) {
 			logger.error(e.getMessage());
 			return null;
 		}
 		return temp;
 	}
 
 	public void sendMail(String addresses, Constituent p, Gift g,
 			String subject, Map<String,String> map, String templateName, List<Email> selectedEmails) {
 		sendMail(addresses, p, g, null, null, map,
 				subject, templateName, selectedEmails);
 	}
 
 	//This is used for the old drools rules
 	public void sendMail(String addresses, Constituent p, Gift g,
 			String subject, String templateName, List<Email> selectedEmails) {
 		sendMail(addresses, p, g, null, null, new HashMap<String, String>(),
 				subject, templateName, selectedEmails);
 	}
 
 
 	public void sendMail(String addresses, Constituent p, Gift g,
 			RecurringGift recurringGift, Pledge pledge,
 			Map<String, String> reportParams, String subject,
 			String templateName, List<Email> selectedEmails) {
 
 
 		Site s = (g == null) ? p.getSite() : g.getSite();
 
 		Map<String, String> params = new HashMap<String,String>();
 		params.clear();
 
 		params.put("Id", p.getId().toString());
 		if (g != null) {
 			params.put("GiftId", g.getId().toString());
 			params.put("GiftAmount", g.getAmount().toString());
 		}
 		if (recurringGift != null) {
 			params.put("RecurringGiftId", recurringGift.getId().toString());
 		}
 		if (pledge != null) {
 			params.put("PledgeId", pledge.getId().toString());
 		}
 		params.putAll(reportParams);
 
 		//
 		// next we extract the output of the report and put it into a mime
 		// message
 		if (s.getSmtpServerName() != null) {
 			JavaMailSenderImpl sender = new JavaMailSenderImpl();
 			sender.setHost(s.getSmtpServerName());
 
 			if (s.getSmtpAccountName() != null)
 				sender.setUsername(s.getSmtpAccountName());
 			if (s.getSmtpPassword() != null)
 				sender.setPassword(s.getSmtpPassword());
 
 			MimeMessage message = sender.createMimeMessage();
 			MimeMessageHelper helper;
 			try {
 				helper = new MimeMessageHelper(message, true, "UTF-8");
 			} catch (MessagingException e2) {
 				logger.error(e2.getMessage());
 				return;
 			}
 
 			File tempFile = runReport(s,params,templateName);
 
 			if (tempFile != null) {
 				FileSystemResource file = new FileSystemResource(tempFile);
 				try {
 					helper.addAttachment(templateName + ".pdf", file);
 
 					String itemName = "recent donation";
 					if (recurringGift != null)
 						itemName = "commitment";
 					if (pledge != null)
 						itemName = "pledge";
 
 					String emailBody = reportParams.get(EMAIL_BODY);
 					helper.setText(emailBody == null ? "Thank you for your " + itemName + "!" : emailBody);
 					helper.setSubject(subject);
 					helper.setFrom(s.getSmtpFromAddress());
 
 					String emailAddresses[] = addresses.split(",");
 					for (int i = 0; i < emailAddresses.length; i++)
 						helper.addTo(emailAddresses[i]);
 
 					//
 					// finally we mail the message
 					sender.send(message);
 					tempFile.delete();
 
 					//
 					// add entry to touchpoints for this e-mail
 					if (selectedEmails != null) {
 						for (Email e : selectedEmails) {
 							CommunicationHistory ch = new CommunicationHistory();
 							ch.setConstituent(p);
 							if (g != null)
 								ch.setGiftId(g.getId());
 							if (recurringGift != null)
 								ch.setRecurringGiftId(recurringGift.getId());
 							if (pledge != null)
 								ch.setPledgeId(pledge.getId());
 							ch.setSystemGenerated(true);
 							ch.setComments("Sent e-mail using template named "
 									+ templateName);
 							ch.setEntryType("Email");
 							ch.setRecordDate(new Date());
 							ch.setEmail(e);
 							ch.setCustomFieldValue("template",
 									templateName);
 
 							ch.setSuppressValidation(true);
 							try {
 								communicationHistoryService
 										.maintainCommunicationHistory(ch);
 							} catch (BindException e1) {
 								// Should not happen when setSuppressValidation
 								// = true;
 								logger.error(e1);
 							}
 						}
 					} else {
 						// note touchpoint
 						CommunicationHistory ch = new CommunicationHistory();
 						ch.setConstituent(p);
 						if (g != null)
 							ch.setGiftId(g.getId());
 						if (recurringGift != null)
 							ch.setRecurringGiftId(recurringGift.getId());
 						if (pledge != null)
 							ch.setPledgeId(pledge.getId());
 						ch.setSystemGenerated(true);
 						ch.setComments("Sent e-mail to " + addresses
 								+ " reason: " + subject);
 						ch.setEntryType("Note");
 						ch.setRecordDate(new Date());
 						// ch.setSelectedEmail(e);
 						ch.setCustomFieldValue("template", templateName);
 
 						ch.setSuppressValidation(true);
 						try {
 							communicationHistoryService
 									.maintainCommunicationHistory(ch);
 						} catch (BindException e1) {
 							// Should not happen when setSuppressValidation =
 							// true;
 							logger.error(e1);
 						}
 					}
 
 				} catch (MessagingException e1) {
 					logger.error(e1.getMessage());
 					return;
 				}
 			}
 		} else
 			logger.error("Failed to generate report.");
 
 	}
 
 	public void sendMail(Constituent p, Gift g, String subject,
 			String templateName, Boolean primaryOnly) {
 		sendMail(p, g, null, null, new HashMap<String, String>(), subject,
 				templateName, primaryOnly);
 	}
 
 	public void sendMail(Constituent p, Gift g, RecurringGift recurringGift,
 			Pledge pledge, Map<String, String> reportParams, String subject,
 			String templateName, Boolean primaryOnly) {
 		String strEmailAddrs = "";
 		List<Email> selectedEmails = new LinkedList<Email>();
 
 		Site s = p.getSite();
 
 
 		//
 		// first we run the report passing in the constituent.id as a parameter
 		List<Email> emailAddresses = p.getEmails();
 
 		for (Email e : emailAddresses) {
 			if (e.isReceiveCorrespondence() && !e.isInactive() && e.isValid()) {
 				//check to see if the param primaryOnly is true if so only add primary email addr
 				if(primaryOnly){
 					if (e.isPrimary()){
 						selectedEmails.add(e);
 						strEmailAddrs += e.getEmailAddress() + ",";
 					}
 				}else{
 					selectedEmails.add(e);
 					strEmailAddrs += e.getEmailAddress() + ",";
 				}
 
 			}
 		}
 
 		//
 		// no e-mail addresses can receive mail
 		if (selectedEmails.size() == 0) {
 			return;
 		}
 
 		// remove the trailing ,
 		strEmailAddrs = strEmailAddrs.substring(0, strEmailAddrs
 				.lastIndexOf(","));
 
 		this.sendMail(strEmailAddrs, p, g, recurringGift, pledge, reportParams,
 				subject, templateName, selectedEmails);
 		/*
 		 * // // add entry to touchpoints for this e-mail for (Email e :
 		 * selectedEmails) { CommunicationHistory ch = new
 		 * CommunicationHistory(); ch.setConstituent(p);
 		 * ch.setGiftId(g.getId()); ch.setSystemGenerated(true);
 		 * ch.setComments("Sent e-mail using template named " +
 		 * getTemplateName()); ch.setEntryType("Email"); ch.setRecordDate(new
 		 * Date()); ch.setSelectedEmail(e); ch.setCustomFieldValue("template",
 		 * getTemplateName());
 		 *
 		 * ch.setSuppressValidation(true); try {
 		 * communicationHistoryService.maintainCommunicationHistory(ch); } catch
 		 * (BindException e1) { // Should not happen when setSuppressValidation
 		 * = true; logger.error(e1); }
 		 *
 		 * }
 		 */
 	}
 
 	private RepositoryReportUnit getReportUnit(JServer jserver,String templatename,Map map,Site s) {
 		ResourceDescriptor rd = new ResourceDescriptor();
 
 		rd.setName(templatename);
 		rd.setParentFolder("/Reports/" + s.getName()
 				+ "/emailTemplates");
 		rd.setUriString(rd.getParentFolder() + "/" + rd.getName());
 		rd.setWsType(ResourceDescriptor.TYPE_REPORTUNIT);
 		List<ResourceProperty> p = new ArrayList<ResourceProperty>();
 
 		Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
 		while (it.hasNext()) {
 			Map.Entry<String, String> me = it.next();
 
 			ResourceProperty rp = new ResourceProperty(me.getKey().toString(),
 					me.getValue().toString());
 			p.add(rp);
 		}
 
 		rd.setParameters(p);
 
 		return new RepositoryReportUnit(jserver, rd);
 	}
 
 	public String getBaseUri() {
 		return baseUri;
 	}
 
 	public void setBaseUri(String uri) {
 		this.baseUri = uri;
 	}
 
 	public String getRepositoryUri() {
 		return repositoryUri;
 	}
 
 	public void setRepositoryUri(String uri) {
 		this.repositoryUri = uri;
 	}
 
 	public Log getLogger() {
 		return logger;
 	}
 
 	public CommunicationHistoryService getCommunicationHistoryService() {
 		return communicationHistoryService;
 	}
 
 	public void setCommunicationHistoryService(
 			CommunicationHistoryService communicationHistoryService) {
 		this.communicationHistoryService = communicationHistoryService;
 	}
 }
