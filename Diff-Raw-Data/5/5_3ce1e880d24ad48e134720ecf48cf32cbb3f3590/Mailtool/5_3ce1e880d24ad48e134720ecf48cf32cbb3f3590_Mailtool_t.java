 /**********************************************************************************
  * $URL$
  * $Id$
  ***********************************************************************************
  *
  * Copyright (c) 2006, 2007 The Sakai Foundation.
  * 
  * Licensed under the Educational Community License, Version 1.0 (the "License"); 
  * you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at
  * 
  *      http://www.opensource.org/licenses/ecl1.php
  * 
  * Unless required by applicable law or agreed to in writing, software 
  * distributed under the License is distributed on an "AS IS" BASIS, 
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
  * See the License for the specific language governing permissions and 
  * limitations under the License.
  *
  **********************************************************************************/
 /*
  * Created Apr 15, 2005 by Steven Githens (s-githens@northwestern.edu)
  *
  * Modified/Expanded 2006, 2007 by SOO IL KIM (kimsooil@bu.edu)
  * 
  */
 package org.sakaiproject.tool.mailtool;
 
 // import java.lang.Thread;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.TreeSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.Vector;
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.io.FilenameUtils;
 import org.sakaiproject.tool.api.ToolSession;
 import org.sakaiproject.tool.cover.SessionManager;
 import org.sakaiproject.tool.cover.ToolManager;
 // import org.sakaiproject.email.cover.EmailService;
 import org.sakaiproject.event.cover.NotificationService;
 import org.sakaiproject.authz.api.AuthzGroup;
 import org.sakaiproject.authz.cover.AuthzGroupService;
 import org.sakaiproject.authz.api.Role;
 import org.sakaiproject.component.cover.ServerConfigurationService;
 import org.sakaiproject.site.api.Group;
 import org.sakaiproject.site.api.Site;
 import org.sakaiproject.site.api.ToolConfiguration;
 import org.sakaiproject.time.cover.TimeService;
 import org.sakaiproject.user.api.User;
 import org.sakaiproject.user.cover.UserDirectoryService;
 import org.sakaiproject.mailarchive.api.MailArchiveChannel;
 import org.sakaiproject.mailarchive.api.MailArchiveMessageEdit;
 import org.sakaiproject.mailarchive.api.MailArchiveMessageHeaderEdit;
 import org.sakaiproject.mailarchive.cover.MailArchiveService;
 import org.sakaiproject.site.cover.SiteService;
 // import org.sakaiproject.site.api.SitePage;
 // import org.sakaiproject.tool.api.ToolSession;
 // import org.sakaiproject.tool.cover.SessionManager;
 import javax.faces.context.FacesContext;
 import javax.faces.application.FacesMessage;
 import javax.faces.event.PhaseId;
 import javax.faces.event.ValueChangeEvent;
 // import javax.faces.event.ActionEvent;
 import javax.faces.event.AbortProcessingException;
 import javax.faces.validator.ValidatorException;
 import javax.faces.component.UIComponent;
 import java.util.Properties;
 import javax.mail.BodyPart;
 import javax.mail.Message;
 import javax.mail.Multipart;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.MimeBodyPart;
 import javax.mail.internet.MimeMessage;
 import javax.mail.internet.MimeMultipart;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeUtility;
 import javax.activation.DataHandler;
 import javax.activation.DataSource;
 import javax.activation.FileDataSource;
 
 /**
  * Mailtool bean for compose (expanded by kimsooil@bu.edu)
  * 
  * @author sgithen
  *
  */
 public class Mailtool {
 	private final Log log = LogFactory.getLog(this.getClass());
 
 	protected FacesContext facesContext = FacesContext.getCurrentInstance();
 
 	protected boolean DEBUG_NO_EMAIL = true;
 
 	protected static final int NUMBER_ROLES = 15;
 
 	private int MaxNumAttachment = readMaxNumAttachment();
 
 	/** Config Parameters * */
 	protected String m_realm = "";
 
 	protected List /* EmailRole */m_emailroles = new ArrayList();
 
 	protected String m_recipview = "";
 
 	protected String uploaddirectoryDefault = "/tmp/";
 
 	protected String recipviewDefault = "tree";
 
 	protected String groupAwareRoleDefault = "";
 
 	protected String groupAwareRoleFound = "";
 
 	/** For Main.jsp * */
 	protected String m_subject = "";
 
 	protected String m_subjectprefix = "";
 
 	protected String m_otheremails = "";
 
 	protected String m_replytootheremail = "";
 
 	protected String m_body = "";
 
 	protected String m_editortype = "";
 
 	protected String m_replyto = "";
 
 	protected String m_sitetype = "";
 
 	protected String m_mode = "";
 
 	protected String m_siteid = "";
 
 	protected String m_realmid = "";
 	
 	protected String sitename = "";
 
 	protected boolean is_fckeditor = false;
 
 	protected boolean is_htmlarea = false;
 
 	protected RecipientSelector m_recipientSelector = null;
 
 	protected RecipientSelector m_recipientSelector1 = null;
 
 	protected RecipientSelector m_recipientSelector2 = null;
 
 	protected RecipientSelector m_recipientSelector3 = null;
 
 	protected boolean m_selectByRole = false;
 
 	protected boolean m_selectByUser = false;
 
 	protected boolean m_selectByTree = false;
 
 	protected boolean m_selectSideBySide = false;
 
 	protected boolean m_selectByFoothill = false;
 
 	protected boolean m_archiveMessage = false;
 
 	protected boolean m_sendmecopy = false;
 
 	protected boolean m_replytosender = false;
 
 	protected boolean m_donotreply = false;
 
 	protected boolean m_replytoother = false;
 
 	protected boolean m_allusers = false;
 
 	protected boolean EmailArchiveInSite = false;
 
 	protected int num_groups = 0;
 
 	protected int num_sections = 0;
 
 	protected int num_groupawarerole = 0;
 
 	protected String m_textformat = "";
 
 	private String m_recipJSPfrag = "";
 
 	protected String m_results = "";
 
 	/** Set Sakai Services */
 	// protected EmailService m_emailService = null;
 	private UserDirectoryService m_userDirectoryService;
 
 	private AuthzGroupService m_realmService;
 
 	private AuthzGroup arole;
 
 	private SiteService siteService;
 
 	protected ToolConfiguration m_toolConfig = null;
 
 	protected Site currentSite = null;
 
 	private List attachedFiles = new ArrayList();
 
 	private List renamedRoles = new ArrayList();
 
 	private int num_roles_renamed = 0;
 
 	private int num_role_id = 0;
 
 	private String roleid = "";
 
 	private String singular = "";
 
 	private String plural = "";
 
 	private boolean already_configured = false;
 
 	private String filename = "";
 
 	private int num_files = 0;
 
 	private int num_id = 0;
 
 	private boolean attachClicked = false;
 
 	private boolean groupviewClicked = false;
 
 	private boolean sectionviewClicked = false;
 
 	private boolean groupAwareRoleviewClicked = false;
 
 	private boolean showRenamingRolesClicked = false;
 
 	private boolean allGroupSelected = false;
 
 	private boolean allSectionSelected = false;
 
 	private boolean allGroupAwareRoleSelected = false;
 
 	private boolean GroupAwareRoleExist = false;
 
 	private List selected = null;
 
 	private List selectedGroupAwareRoleUsers = null;
 
 	private List selectedGroupUsers = null;
 
 	private List selectedSectionUsers = null;
 
 	/**
 	 * Mailtool bean for compose page
 	 */
 	public Mailtool() {
 		num_groups = 0;
 		num_sections = 0;
 		num_groupawarerole = 0;
 
 		m_sitetype = getSiteType();
 		m_siteid = getSiteID();
 		m_realmid = getSiteRealmID();
 		groupAwareRoleDefault = getGroupAwareRoleDefault();
 		groupAwareRoleFound = getGroupAwareRole();
 		sitename = getSiteTitle();
 
 		setSelectorType();
 		getRecipientSelectors();
 
 		checkifGroupAwareRoleExist(); /* this initialization solves SAK-6810 */
 
 		setMessageSubject(getSubjectPrefix().equals("") ? getSubjectPrefixFromConfig()
 				: getSubjectPrefix());
 		setSubjectPrefix(getSubjectPrefixFromConfig());
 		setEmailArchiveInSite(isEmailArchiveAddedToSite());
 
 		String reply = getConfigParam("replyto").trim().toLowerCase();
 		if (reply.equals("") || reply.equals("yes")) {
 			setReplyToSelected("yes");
 		} else if (reply.equals("no")) {
 			setReplyToSelected("no");
 		} else { // reply to other email
 			setReplyToSelected("otheremail");
 			setReplyToOtherEmail(getConfigParam("replyto").trim().toLowerCase());
 		}
 
 		setSendMeCopy(getConfigParam("sendmecopy").trim().toLowerCase().equals(
 				"yes"));
 
 		setArchiveMessage(getConfigParam("emailarchive").trim().toLowerCase()
 				.equals("yes"));
 
 		String textformat = getConfigParam("messageformat").trim()
 				.toLowerCase();
 		if (textformat.equals("") || textformat.equals("htmltext")) {
 			setTextFormat("htmltext");
 		} else {
 			setTextFormat("plaintext");
 		}
 
 		log.debug("Constructor");
 	}
 
 	/** begin: Done Setting Sakai Services * */
 
 	// public void setEmailService(EmailService service) { this.m_emailService =
 	// service; }
 	public void setUserDirectoryService(UserDirectoryService service) {
 		this.m_userDirectoryService = service;
 	}
 
 	public void setAuthzGroupService(AuthzGroupService service) {
 		this.m_realmService = service;
 	}
 
 	// public void setLogger(Logger logger) { this.logger = logger; } // by SK
 	// 6/30/2006
 
 	/** end: Done Setting Sakai Services * */
 
 	public boolean isGroupviewClicked() {
 		return groupviewClicked;
 	}
 
 	public void setGroupviewClicked(boolean groupviewClicked) {
 		this.groupviewClicked = groupviewClicked;
 	}
 
 	public boolean isSectionviewClicked() {
 		return sectionviewClicked;
 	}
 
 	public void setSectionviewClicked(boolean sectionviewClicked) {
 		this.sectionviewClicked = sectionviewClicked;
 	}
 
 	public void toggle_groupviewClicked() {
 		groupviewClicked = groupviewClicked ? false : true;
 		sectionviewClicked = false; // exclusive rendering
 		groupAwareRoleviewClicked = false;
 	}
 
 	public void toggle_sectionviewClicked() {
 		sectionviewClicked = sectionviewClicked ? false : true;
 		groupviewClicked = false; // exclusive rendering
 		groupAwareRoleviewClicked = false;
 	}
 
 	public void toggle_groupAwareRoleviewClicked() {
 		groupAwareRoleviewClicked = groupAwareRoleviewClicked ? false : true;
 		sectionviewClicked = false; // exclusive rendering
 		groupviewClicked = false; // exclusive rendering
 	}
 
 	public boolean isAllGroupSelected() {
 		return allGroupSelected;
 	}
 
 	public void setAllGroupSelected(boolean allGroupSelected) {
 		this.allGroupSelected = allGroupSelected;
 	}
 
 	public boolean isAllSectionSelected() {
 		return allSectionSelected;
 	}
 
 	public void setAllSectionSelected(boolean allSectionSelected) {
 		this.allSectionSelected = allSectionSelected;
 	}
 
 	public int getNum_sections() {
 		return num_sections;
 	}
 
 	public void setNum_sections(int num_sections) {
 		this.num_sections = num_sections;
 	}
 
 	public int getNum_groups() {
 		return num_groups;
 	}
 
 	public void setNum_groups(int num_groups) {
 		this.num_groups = num_groups;
 	}
 
 	public void setattachClicked(boolean a) {
 		this.attachClicked = a;
 	}
 
 	/**
 	 * @return
 	 * 		return if attach clicked
 	 */
 	public boolean getattachClicked() {
 		return attachClicked;
 	}
 
 	/**
 	 * read a configuration from registeration file
 	 *  
 	 * @param parameter
 	 * @return
 	 * return configuration string which matches parameter
 	 */
 	protected String getConfigParam(String parameter) {
 		String p = ToolManager.getCurrentPlacement().getPlacementConfig()
 				.getProperty(parameter);
 		if (p == null)
 			return "";
 		return p;
 	}
 
 	protected void setConfigParam(String parameter, String newvalue) {
 		ToolManager.getCurrentPlacement().getPlacementConfig().setProperty(
 				parameter, newvalue);
 		// ToolManager.getCurrentPlacement().save(); // will be saved in
 		// processUpdateOptions
 	}
 
 	protected String getSiteID() {
 		return (ToolManager.getCurrentPlacement().getContext());
 	}
 
 	private String getSiteRealmID() {
 		return ("/site/" + ToolManager.getCurrentPlacement().getContext());
 	}
 
 	protected String getSiteType() {
 		// String sid=getSiteID();
 		String type = "";
 		try {
 			type = SiteService.getSite(m_siteid).getType();
 		} catch (Exception e) {
 			log.debug("Exception: Mailtool.getSiteType(), " + e.getMessage());
 		}
 		return type;
 	}
 
 	protected String getSiteTitle() {
 		// String sid=getSiteID();
 		String title = "";
 		try {
 			title = SiteService.getSite(m_siteid).getTitle();
 		} catch (Exception e) {
 			log.debug("Exception: Mailtool.getSiteTitle(), " + e.getMessage());
 		}
 		return title;
 
 	}
 
 	public String processGoToOptions() {
 		return "configure";
 	}
 
 	public String getfilename() {
 		return filename;
 	}
 
 	public void setfilename(String filename) {
 		this.filename = filename;
 	}
 
 	public String getRoleID() {
 		return roleid;
 	}
 
 	public void setRoleID(String r) {
 		this.roleid = r;
 	}
 
 	public int getnum_files() {
 		return this.num_files;
 	}
 
 	public void setnum_files(int num_files) {
 		this.num_files = num_files;
 	}
 
 	/**
 	 * @return
 	 * 		return the type of editor
 	 */
 	public String getEditorType() {
 		return m_editortype;
 	}
 
 	/**
 	 * read maximum number number of attachment from mailtool.max.num.attachment in sakai.properties
 	 * @return
 	 * 		return mailtool.max.num.attachment if set
 	 * 		Or return 10000 if not set.
 	 */
 	public int readMaxNumAttachment() {
 		try {
 			int maxnumattachment = Integer.parseInt(ServerConfigurationService
 					.getString("mailtool.max.num.attachment"));
 			return maxnumattachment;
 		} catch (NumberFormatException e) {
 			log
 					.debug("Exception: Mailtool Max Num. of attachment is set to 10000, "
 							+ e.getMessage());
 			return 10000; // Actually this means "unlimited if not set or
 							// invalid"
 		}
 	}
 
 	public int getMaxNumAttachment() {
 		return MaxNumAttachment;
 	}
 
 	public void setMaxNumAttachment(int m) {
 		this.MaxNumAttachment = m;
 	}
 
 	public String getUploadDirectory() {
 		String ud = ServerConfigurationService
 				.getString("mailtool.upload.directory");
 		if (ud != "" && ud != null) {
 			File dir = new File(ud);
 			if (dir.isDirectory())
 				return ud;
 		}
 		return uploaddirectoryDefault;
 	}
 
 	/**
 	 * check if mailtool.show.renaming.role=yes|true in sakai.properties
 	 * @return
 	 * 		return true if mailtool.show.renaming.role=yes|true
 	 */
 	public boolean isShowRenamingRoles() {
 		String rename = ServerConfigurationService
 				.getString("mailtool.show.renaming.roles");
 		if (rename != "" && rename != null) {
 			return (rename.trim().toLowerCase().equals("yes")
 					|| rename.trim().toLowerCase().equals("true") ? true
 					: false);
 		}
 		return false;
 	}
 
 	public void setEditorType(String editor) {
 		m_editortype = editor;
 	}
 
 	public String getOtherEmails() {
 		return m_otheremails;
 	}
 
 	public void setOtherEmails(String otheremails) {
 		m_otheremails = otheremails;
 	}
 
 	public String getReplyToOtherEmail() {
 		return m_replytootheremail;
 	}
 
 	public void setReplyToOtherEmail(String email) {
 		m_replytootheremail = email;
 	}
 
 	public String getMessageSubject() {
 		return m_subject;
 	}
 
 	public void setMessageSubject(String subject) {
 		m_subject = subject;
 	}
 
 	public String getSubjectPrefix() {
 		return m_subjectprefix;
 	}
 
 	public void setSubjectPrefix(String prefix) {
 		m_subjectprefix = prefix;
 	}
 
 	public String getMessageBody() {
 		return m_body;
 	}
 
 	public void setMessageBody(String body) {
 		m_body = body;
 	}
 
 	public String getResults() {
 		return m_results;
 	}
 
 	/**
 	 * set setectortype when needed.
 	 */
 	protected void setSelectorType() {
 		String type = getRecipview();
 		if (type.equals("") || type == null)
 			type = recipviewDefault;
 
 		m_selectByRole = false;
 		m_selectByUser = false;
 		m_selectByTree = false;
 		m_selectSideBySide = false;
 		m_selectByFoothill = false;
 
 		if (type.equals("role")) {
 			m_selectByRole = true;
 			m_recipJSPfrag = "selectByRole.jsp";
 		} else if (type.equals("user")) {
 			m_selectByUser = true;
 			m_recipJSPfrag = "selectByUser.jsp";
 		} else if (type.equals("sidebyside")) {
 			m_selectSideBySide = true;
 			m_recipJSPfrag = "selectSideBySide.jsp";
 		} else if (type.equals("foothill")) {
 			m_selectByFoothill = true;
 			m_recipJSPfrag = "selectByFoothill.jsp";
 		} else if (type.equals("tree")) {
 			m_selectByTree = true;
 			m_recipJSPfrag = "selectByTree.jsp";
 		}
 	}
 
 	public boolean isSelectByRole() {
 		setSelectorType();
 		return m_selectByRole;
 	}
 
 	public boolean isSelectByUser() {
 		setSelectorType();
 		return m_selectByUser;
 	}
 
 	public boolean isSelectByTree() {
 		setSelectorType();
 		return m_selectByTree;
 	}
 
 	public boolean isSelectSideBySide() {
 		setSelectorType();
 		return m_selectSideBySide;
 	}
 
 	public boolean isSelectByFoothill() {
 		setSelectorType();
 		return m_selectByFoothill;
 	}
 
 	/**
 	 * initialize compose page. it will remove all previous editing
 	 * @return
 	 * 		"cancel" for navigating to compse page
 	 */
 	public String processCancelEmail() {
 /*
 		this.m_recipientSelector = null;
 		this.m_subject = getSubjectPrefix().equals("") ? getSubjectPrefixFromConfig()
 				: getSubjectPrefix();
 		m_otheremails = "";
 		this.m_body = "";
 		num_files = 0;
 		attachedFiles.clear();
 		// m_buildNewView = true;
 		m_recipientSelector = null;
 		m_recipientSelector1 = null;
 		m_recipientSelector2 = null;
 		m_recipientSelector3 = null;
 		setAllUsersSelected(false);
 		setAllGroupSelected(false);
 		setAllSectionSelected(false);
 */
 		ToolSession ts = SessionManager.getCurrentSession().getToolSession(ToolManager.getCurrentPlacement().getId());
 		ts.clearAttributes();
 		return "cancel";
 	}
 
 	/**
 	 * Refactored mail-sending function (not using Sakai EmailService)
 	 * @return
 	 * 		return "results" for navigating to results page
 	 */
 	public String processSendEmail() {
 		/* EmailUser */selected = m_recipientSelector.getSelectedUsers();
 		if (m_selectByTree) {
 			selectedGroupAwareRoleUsers = m_recipientSelector1
 					.getSelectedUsers();
 			selectedGroupUsers = m_recipientSelector2.getSelectedUsers();
 			selectedSectionUsers = m_recipientSelector3.getSelectedUsers();
 
 			selected.addAll(selectedGroupAwareRoleUsers);
 			selected.addAll(selectedGroupUsers);
 			selected.addAll(selectedSectionUsers);
 		}
 		// Put everyone in a set so the same person doesn't get multiple emails.
 		Set emailusers = new TreeSet();
 		if (isAllUsersSelected()) { // the button for this is inactivated ...
 									// leave for future
 			for (Iterator i = getEmailGroups().iterator(); i.hasNext();) {
 				EmailGroup group = (EmailGroup) i.next();
 				emailusers.addAll(group.getEmailusers());
 			}
 		}
 		if (isAllGroupSelected()) {
 			for (Iterator i = getEmailGroupsByType("section").iterator(); i
 					.hasNext();) {
 				EmailGroup group = (EmailGroup) i.next();
 				if (group.getEmailrole().roletype.equals("section")) {
 					selected.addAll(group.getEmailusers());
 				}
 			}
 		}
 		if (isAllSectionSelected()) {
 			for (Iterator i = getEmailGroupsByType("group").iterator(); i
 					.hasNext();) {
 				EmailGroup group = (EmailGroup) i.next();
 				if (group.getEmailrole().roletype.equals("group")) {
 					selected.addAll(group.getEmailusers());
 				}
 			}
 		}
 		if (isAllGroupAwareRoleSelected()) {
 			for (Iterator i = getEmailGroupsByType("role_groupaware")
 					.iterator(); i.hasNext();) {
 				EmailGroup group = (EmailGroup) i.next();
 				if (group.getEmailrole().roletype.equals("role_groupaware")) {
 					selected.addAll(group.getEmailusers());
 				}
 			}
 		}
 		emailusers = new TreeSet(selected); // convert List to Set (remove
 											// duplicates)
 
 		m_subjectprefix = getSubjectPrefixFromConfig();
 
 		EmailUser curUser = getCurrentUser();
 
 		String fromEmail = "";
 		String fromDisplay = "";
 		if (curUser != null) {
 			fromEmail = curUser.getEmail();
 			fromDisplay = curUser.getDisplayname();
 		}
 		String fromString = fromDisplay + " <" + fromEmail + ">";
 
 		m_results = "Message sent to: <br>";
 
 		String subject = m_subject;
 
 		// Should we append this to the archive?
 		String emailarchive = "/mailarchive/channel/" + m_siteid + "/main";
 		if (m_archiveMessage && isEmailArchiveInSite()) {
 			String attachment_info = "<br/>";
 			Attachment a = null;
 			Iterator iter = attachedFiles.iterator();
 			int i = 0;
 			while (iter.hasNext()) {
 				a = (Attachment) iter.next();
 				attachment_info += "<br/>";
 				attachment_info += "Attachment #" + (i + 1) + ": "
 						+ a.getFilename() + "(" + a.getSize() + " Bytes)";
 				i++;
 			}
 			this.appendToArchive(emailarchive, fromString, subject, m_body
 					+ attachment_info);
 		}
 		List headers = new ArrayList();
 		if (getTextFormat().equals("htmltext"))
 			headers.add("content-type: text/html");
 		else
 			headers.add("content-type: text/plain");
 
 		String smtp_server = ServerConfigurationService
 				.getString("smtp@org.sakaiproject.email.api.EmailService");
 		// String smtp_port = ServerConfigurationService.getString("smtp.port");
 		try {
 			Properties props = new Properties();
 			props.put("mail.smtp.host", smtp_server);
 			// props.put("mail.smtp.port", smtp_port);
 			Session s = Session.getInstance(props, null);
 
 			MimeMessage message = new MimeMessage(s);
 
 			InternetAddress from = new InternetAddress(fromString);
 			message.setFrom(from);
 			String reply = getReplyToSelected().trim().toLowerCase();
 			if (reply.equals("yes")) {
 				// "reply to sender" is default. So do nothing
 			} else if (reply.equals("no")) {
 				String noreply = getSiteTitle() + " <noreply@" + smtp_server
 						+ ">";
 				InternetAddress noreplyemail = new InternetAddress(noreply);
 				message.setFrom(noreplyemail);
 			} else if (reply.equals("otheremail")
 					&& getReplyToOtherEmail().equals("") != true) {
 				// need input(email) validation
 				InternetAddress replytoList[] = { new InternetAddress(
 						getConfigParam("replyto").trim()) };
 				message.setReplyTo(replytoList);
 			}
 			message.setSubject(MimeUtility.encodeText( subject, "UTF-8", "Q" ));
 			String text = m_body;
 			String attachmentdirectory = getUploadDirectory();
 
 			// Create the message part
 			MimeBodyPart messageBodyPart = new MimeBodyPart();
 
 			// Fill the message
 			String messagetype = "";
 
 			if (getTextFormat().equals("htmltext")) {
 				messagetype = "text/html; charset=UTF-8";
             messageBodyPart.setContent(text, messagetype);
 			} else {
 				messagetype = "text/plain; charset=UTF-8";
             messageBodyPart.setContent(text, messagetype);
 			}
 			messageBodyPart.addHeader("Content-Transfer-Encoding", "quoted-printable");
 			messageBodyPart.addHeader("Content-Type", messagetype );
 			Multipart multipart = new MimeMultipart();
 			multipart.addBodyPart(messageBodyPart);
 
 			// Part two is attachment
 			Attachment a = null;
 			Iterator iter = attachedFiles.iterator();
 			while (iter.hasNext()) {
 				a = (Attachment) iter.next();
 				messageBodyPart = new MimeBodyPart();
 				DataSource source = new FileDataSource(attachmentdirectory
 						+ this.getCurrentUser().getUserid() + "-"
 						+ a.getFilename());
 				messageBodyPart.setDataHandler(new DataHandler(source));
 				messageBodyPart.setFileName(a.getFilename());
 				multipart.addBodyPart(messageBodyPart);
 			}
 			message.setContent(multipart);
 
 			// Send the emails
 			String recipientsString = "";
 			for (Iterator i = emailusers.iterator(); i.hasNext(); recipientsString += ",") {
 				EmailUser euser = (EmailUser) i.next();
 				String toEmail = euser.getEmail(); // u.getEmail();
 				String toDisplay = euser.getDisplayname(); // u.getDisplayName();
 				// if AllUsers are selected, do not add current user's email to
 				// recipients
 				if (isAllUsersSelected()
 						&& getCurrentUser().getEmail().equals(toEmail)) {
 					// don't add sender to recipients
 				} else {
 					recipientsString += toEmail;
 					m_results += toDisplay + (i.hasNext() ? "<br/>" : "");
 				}
 				// InternetAddress to[] = {new InternetAddress(toEmail) };
 				// Transport.send(message,to);
 			}
 			if (m_otheremails.trim().equals("") != true) {
 				//
 				// multiple email validation is needed here
 				//
 				String refinedOtherEmailAddresses = m_otheremails.trim()
 						.replace(';', ',');
 				recipientsString += refinedOtherEmailAddresses;
 				m_results += "<br/>" + refinedOtherEmailAddresses;
 				// InternetAddress to[] = {new
 				// InternetAddress(refinedOtherEmailAddresses) };
 				// Transport.send(message, to);
 			}
 			if (m_sendmecopy) {
 				message.addRecipients(Message.RecipientType.CC, fromEmail);
 				// trying to solve SAK-7410
 				// recipientsString+=fromEmail;
 				// InternetAddress to[] = {new InternetAddress(fromEmail) };
 				// Transport.send(message, to);
 			}
 			// message.addRecipients(Message.RecipientType.TO,
 			// recipientsString);
 			message.addRecipients(Message.RecipientType.BCC, recipientsString);
 
 			Transport.send(message);
 		} catch (Exception e) {
 			log.debug("Mailtool Exception while trying to send the email: "
 					+ e.getMessage());
 		}
 
 		// Clear the Subject and Body of the Message
 		m_subject = getSubjectPrefix().equals("") ? getSubjectPrefixFromConfig()
 				: getSubjectPrefix();
 		m_otheremails = "";
 		m_body = "";
 		num_files = 0;
 		attachedFiles.clear();
 		m_recipientSelector = null;
 		m_recipientSelector1 = null;
 		m_recipientSelector2 = null;
 		m_recipientSelector3 = null;
 		setAllUsersSelected(false);
 		setAllGroupSelected(false);
 		setAllSectionSelected(false);
 
 		// Display Users with Bad Emails if the option is turned on.
 		boolean showBadEmails = getDisplayInvalidEmailAddr();
 		if (showBadEmails == true) {
 			m_results += "<br/><br/>";
 
 			List /* String */badnames = new ArrayList();
 
 			for (Iterator i = selected.iterator(); i.hasNext();) {
 				EmailUser user = (EmailUser) i.next();
 				/* This check should maybe be some sort of regular expression */
 				if (user.getEmail().equals("")) {
 					badnames.add(user.getDisplayname());
 				}
 			}
 			if (badnames.size() > 0) {
 				m_results += "The following users do not have valid email addresses:<br/>";
 				for (Iterator i = badnames.iterator(); i.hasNext();) {
 					String name = (String) i.next();
 					if (i.hasNext() == true)
 						m_results += name + "/ ";
 					else
 						m_results += name;
 				}
 			}
 		}
 		return "results";
 	}
 
 	
 	public RecipientSelector getRecipientSelector() {
 		getRecipientSelectors();
 
 		return m_recipientSelector;
 	}
 
 	public RecipientSelector getRecipientSelector_GroupAwareRole() {
 		getRecipientSelectors();
 
 		return m_recipientSelector1;
 	}
 
 	public RecipientSelector getRecipientSelector_Group() {
 		getRecipientSelectors();
 
 		return m_recipientSelector2;
 	}
 
 	public RecipientSelector getRecipientSelector_Section() {
 		getRecipientSelectors();
 
 		return m_recipientSelector3;
 	}
 
 	/**
 	 * By the selected type of recipient view, initialize/populate recipient selector(s)
 	 */
 	public void getRecipientSelectors() {
 		if (m_recipientSelector == null) {
 			if (m_selectByUser == true) {
 				m_recipientSelector = new UserSelector();
 			} else if (m_selectByTree == true) {
 				m_recipientSelector = new TreeSelector();
 				m_recipientSelector1 = new TreeSelector();
 				m_recipientSelector2 = new TreeSelector(); // groups
 				m_recipientSelector3 = new TreeSelector(); // sections
 			} else if (m_selectSideBySide == true) {
 				m_recipientSelector = new SideBySideSelector();
 			} else if (m_selectByFoothill == true) {
 				m_recipientSelector = new FoothillSelector();
 			}
 
 			if (m_selectByTree == true) {
 				List emailGroups1 = getEmailGroupsByType("role");
 				List emailGroups1_1 = getEmailGroupsByType("role_groupaware");
 				List emailGroups2 = getEmailGroupsByType("group");
 				List emailGroups3 = getEmailGroupsByType("section");
 				m_recipientSelector.populate(emailGroups1);
 				m_recipientSelector1.populate(emailGroups1_1);
 				m_recipientSelector2.populate(emailGroups2);
 				m_recipientSelector3.populate(emailGroups3);
 			} else {
 				List emailGroups = getEmailGroups();
 				m_recipientSelector.populate(emailGroups);
 			}
 		}
 	}
 
 	// Get Information from the Tool Config
 	public String getSubjectPrefixFromConfig() {
 		String prefix = this.getConfigParam("subjectprefix");
 		if (prefix == null || prefix == "") {
 			String titleDefault = getSiteTitle() + ": ";
 			return titleDefault;
 		} else
 			return prefix;
 	}
 
 	/**
 	 * 	// Get Information from the Tool Config
 	 * @return
 	 * 		return the string of "recipview=" in sakai.mailtool.xml
 	 */
 	public String getRecipview() {
 		String recipview = this.getConfigParam("recipview");
 		if (recipview == null || recipview.trim().equals(""))
 			return recipviewDefault;
 		else
 			return recipview;
 	}
 
 	/**
 	 * // OOTB(Out of the box) Sakai defaults
 	 * @return
 	 * 		return default group-aware role by type
 	 *		if type=course, return Student.
 	 *		if type=project, return access.
 	 */
 	public String getGroupAwareRoleDefault() {
 //		if (getSiteType().equals("course"))
		if ("course".equals(getSiteType())) // it's a fix of SAK-11052
 			return "Student";
 //		if (getSiteType().equals("project"))
		if ("project".equals(getSiteType())) // it's a fix of SAK-11052
 			return "access";
 		return "";
 	}
 
 	/**
 	 * Get group-aware role which is set in sakai.properties
 	 * e.g. "mailtool.group.aware.role=Student,access"
 	 * 
 	 * @return
 	 * 		return the String of group-aware role name 
 	 */
 	public String getGroupAwareRole() {
 		String gar = ServerConfigurationService
 				.getString("mailtool.group.aware.role");
 		String[] gartokens = gar.split(",");
 		try {
 			arole = m_realmService.getAuthzGroup(m_realmid);
 		} catch (Exception e) {
 			log.debug("Exception: Mailtool.getEmailRoles(), " + e.getMessage());
 		}
 		for (Iterator i = arole.getRoles().iterator(); i.hasNext();) {
 			Role r = (Role) i.next();
 			String rolename = r.getId();
 			for (int t = 0; t < gartokens.length; t++) {
 				if (gartokens[t].trim().equals(rolename.trim()))
 					return rolename;
 			}
 		}
 		return groupAwareRoleDefault;
 	}
 
 	/**
 	 * check if role is listed as group-aware role in mailtool.group.aware.role (in sakai.properties)
 	 * @param role
 	 * @return
 	 * 		return true if role is listed in mailtool.group.aware.role
 	 */
 	public boolean isGroupAwareRoleInSettings(String role) {
 		String gar = ServerConfigurationService
 				.getString("mailtool.group.aware.role");
 		String[] gartokens = gar.split(",");
 
 		for (int i = 0; i < gartokens.length; i++) {
 			if (gartokens[i].trim().equals(role.trim()))
 				return true;
 		}
 		return false;
 	}
 
 	/***************************************************************************
 	 * ** it's user-permission-based checking
 	 * 
 	 * public boolean isAllowedToConfigure() {
 	 * 
 	 * String siteid="/site/"+getSiteID(); //return
 	 * m_realmService.unlock(this.getCurrentUser().getUserid(), "mail.new",
 	 * siteid); return
 	 * m_realmService.isAllowed(this.getCurrentUser().getUserid(),
 	 * "mailtool.admin", siteid);
 	 *  }
 	 **************************************************************************/
 	// role-based permission checking ... modified thanks to Seth at Columbia
 	// Jan 3 2007
 	//
 	public boolean isAllowedToSend() {
 		String mySendRole = m_realmService.getUserRole(this.getCurrentUser()
 				.getUserid(), getSiteRealmID());
 		return hasPermissionForRole(mySendRole, "mailtool.send");
 	}
 
 	// role-based permission
 	public boolean isAllowedToConfigure() {
 		String myConfigRole = m_realmService.getUserRole(this.getCurrentUser()
 				.getUserid(), getSiteRealmID());
 		return hasPermissionForRole(myConfigRole, "mailtool.admin");
 	}
 
 	public boolean isAllowedToArchiveMessage() {
 		String myConfigRole = m_realmService.getUserRole(this.getCurrentUser()
 				.getUserid(), getSiteRealmID());
 		return hasPermissionForRole(myConfigRole, "mail.new");
 	}
 	
 	/**
 	 * explicitly add the permissions for this role in !site.helper with the
 	 * following
 	 * @param role
 	 * @param permission
 	 * @return
 	 * 		return true if role has permission
 	 */
 	private boolean hasPermissionForRole(String role, String permission) {
 		Collection realmList = new ArrayList();
 		realmList.add(getSiteRealmID());
 		AuthzGroup authzGroup = null;
 		try {
 			authzGroup = m_realmService.getAuthzGroup("!site.helper");
 		} catch (Exception e) {
 			log.info("No site helper template found");
 		}
 		if (authzGroup != null) {
 			realmList.add(authzGroup.getId());
 		}
 		Set allowedFunctions = m_realmService.getAllowedFunctions(role,
 				realmList);
 		return allowedFunctions.contains(permission);
 	}
 
 	/**
 	 * check if fckeditor is set for text editing
 	 * @return
 	 * 		return if fckeditor is set for the text editing
 	 */
 	public boolean isFCKeditor() {
 		String editortype = this.getConfigParam("wysiwygeditor");
 		if (editortype.equals("") || editortype == null) {
 			editortype = ServerConfigurationService.getString("wysiwyg.editor");
 			if (editortype == null)
 				return false;
 
 			if (editortype.equals(""))
 				return false;
 
 			if (editortype.equalsIgnoreCase("fckeditor"))
 				return true;
 
 			return false;
 		} else if (editortype.equalsIgnoreCase("fckeditor"))
 			return true;
 
 		return false;
 	}
 
 	/**
 	 * check if htmlarea is set for text editing
 	 * @return
 	 * 		return true if htmlarea is set for text editing
 	 */
 	public boolean isHTMLArea() {
 		String editortype = this.getConfigParam("wysiwygeditor");
 		if (editortype.equals("") || editortype == null) {
 			editortype = ServerConfigurationService.getString("wysiwyg.editor");
 			if (editortype == null)
 				return false;
 
 			if (editortype.equals(""))
 				return false;
 
 			if (editortype.equalsIgnoreCase("htmlarea"))
 				return true;
 
 			return false;
 		} else if (editortype.equalsIgnoreCase("htmlarea"))
 			return true;
 
 		return false;
 	}
 
 	/**
 	 * check if DisplayInvalidEmailAddr set in sakai.mailtool.xml
 	 * @return
 	 * 	return true if "displayinvalidemailaddrs=yes" in Tool Config(registration) file 
 	 */
 	public boolean getDisplayInvalidEmailAddr() {
 		String invalid = this.getConfigParam("displayinvalidemailaddrs");
 		return (invalid == null ? false : (invalid.trim().toLowerCase().equals(
 				"yes") ? true : false));
 	}
 
 	/**
 	 * 	Read the tool config and build the email roles that are specified
 	 * @return
 	 * 		return EmailRoles (called from getEmailGroups())
 	 */
 	public List /* EmailRole */getEmailRoles() {
 		List /* EmailRole */theroles = new ArrayList();
 		List allgroups = new ArrayList();
 		List allsections = new ArrayList();
 		for (int i = 1; i < (NUMBER_ROLES + 1); i++) {
 			String rolerealm = this.getConfigParam("role" + i + "realmid");
 			String rolename = this.getConfigParam("role" + i + "id");
 			String rolesingular = this.getConfigParam("role" + i + "singular");
 			String roleplural = this.getConfigParam("role" + i + "plural");
 			if ((rolerealm != null && rolerealm != "")
 					&& (rolename != null && rolename != "")
 					&& (rolesingular != null && rolesingular != "")
 					&& (roleplural != null && roleplural != "")) {
 				EmailRole emailrole = null;
 
 				// if (isGroupAwareRoleInSettings(rolename)){
 				if (getGroupAwareRole().equals(rolename)) {
 					emailrole = new EmailRole(rolerealm, rolename,
 							rolesingular, roleplural, "role_groupaware");
 					num_groupawarerole++;
 				} else {
 					emailrole = new EmailRole(rolerealm, rolename,
 							rolesingular, roleplural, "role");
 				}
 				theroles.add(emailrole);
 				already_configured = true;
 			}
 		} // for
 		if (already_configured == false) {
 			try {
 				arole = m_realmService.getAuthzGroup(m_realmid);
 			} catch (Exception e) {
 				log.debug("Exception: Mailtool.getEmailRoles(), "
 						+ e.getMessage());
 			}
 			for (Iterator i = arole.getRoles().iterator(); i.hasNext();) {
 				Role r = (Role) i.next();
 				String rolename = r.getId();
 				String singular = "";
 				String plural = "";
 				if (rolename.equals("maintain")) {
 					singular = rolename;
 					plural = rolename + "ers";
 				}
 				else if (rolename.equals("access")) {
 					singular = rolename;
 					plural = rolename + " users";
 				}
 				else {
 					singular = rolename;
 					plural = rolename + "s";
 				}
 				EmailRole emailrole = null;
 				if (getGroupAwareRole().equals(rolename)) {
 					emailrole = new EmailRole("/site/" + m_siteid, rolename,
 							singular, plural, "role_groupaware");
 					num_groupawarerole++;
 				} else
 					emailrole = new EmailRole("/site/" + m_siteid, rolename,
 							singular, plural, "role");
 				theroles.add(emailrole);
 			}
 		}
 		// adding groups as roles
 		try {
 			currentSite = siteService.getSite(m_siteid);
 		} catch (Exception e) {
 			log.debug("Exception: Mailtool.getEmailRoles(): , "
 					+ e.getMessage());
 		}
 		Collection groups = currentSite.getGroups();
 		for (Iterator groupIterator = groups.iterator(); groupIterator
 				.hasNext();) {
 			Group currentGroup = (Group) groupIterator.next();
 			String groupname = currentGroup.getTitle();
 			String groupid = currentGroup.getProviderGroupId();
 			EmailRole emailrole2 = null;
 			if (currentGroup.getProperties().getProperty("sections_category") != null) {
 				emailrole2 = new EmailRole(groupid, groupname, groupname,
 						groupname, "section");
 				allsections.add(emailrole2);
 				num_sections++;
 			} else {
 				emailrole2 = new EmailRole(groupid, groupname, groupname,
 						groupname, "group");
 				allgroups.add(emailrole2);
 				num_groups++;
 			}
 		}
 		theroles.addAll(allgroups); // for sorted list in side-by-side view &
 									// scrolling list view
 		theroles.addAll(allsections); // for sorted list ...
 
 		return theroles;
 	}
 
 	/**
 	 * Check if there is a group-aware role in the site
 	 */
 	public void checkifGroupAwareRoleExist() {
 		String realmid = getSiteRealmID();
 		try {
 			arole = m_realmService.getAuthzGroup(realmid);
 		} catch (Exception e) {
 			log.debug("Exception: Mailtool.initializeCurrentRoles(), "
 					+ e.getMessage());
 		}
 		for (Iterator i = arole.getRoles().iterator(); i.hasNext();) {
 			Role r = (Role) i.next();
 			String rolename = r.getId();
 			if (isGroupAwareRoleInSettings(rolename)) {
 				setGroupAwareRoleExist(true);
 				break;
 			} else if (getGroupAwareRole().equals(rolename)) {
 				setGroupAwareRoleExist(true);
 				break;
 			}
 		}
 	}
 
 	/**
 	 * Using SiteService & SITE.getTools(), check if Email Archive is among the tools in the site
 	 * @return
 	 * 		return true if Email Archive is added to the site
 	 */
 	public boolean isEmailArchiveAddedToSite() {
 		boolean hasEmailArchive = false;
 		String toolid = "sakai.mailbox";
 		try {
 			Site site = SiteService.getSite(m_siteid);
 			Collection toolsInSite = site.getTools(toolid);
 			if (!toolsInSite.isEmpty()) {
 				hasEmailArchive = true;
 			}
 		} catch (Exception e) {
 			log.debug("Exception: Mailtool.isEmailArchiveAddedToSite(), "
 					+ e.getMessage());
 		}
 		return hasEmailArchive;
 	}
 
 	public boolean isEmailArchived() {
 		String emailarchive = this.getConfigParam("emailarchive");
 		if (emailarchive == null)
 			return false;
 		if (emailarchive.equals(""))
 			return false;
 
 		return true;
 	}
 
 	public String getTextFormat() {
 		return m_textformat;
 	}
 
 	public void setTextFormat(String format) {
 		m_textformat = format;
 	}
 
 	public void setDoNotReply(boolean value) {
 		m_donotreply = value;
 	}
 
 	public boolean isArchiveMessage() {
 		return m_archiveMessage;
 	}
 
 	public void setArchiveMessage(boolean value) {
 		m_archiveMessage = value;
 	}
 
 	public boolean isSendMeCopy() {
 		return m_sendmecopy;
 	}
 
 	public void setSendMeCopy(boolean value) {
 		m_sendmecopy = value;
 	}
 
 	public boolean isAllUsersSelected() {
 		return m_allusers;
 	}
 
 	public void setAllUsersSelected(boolean value) {
 		m_allusers = value;
 	}
 
 
 	/**
 	 * Build all groups that will be used for this
 	 * 
 	 * @return
 	 * 		return EmailGroups in the site
 	 */
 	public List /* EmailGroup */getEmailGroups() {
 		List /* EmailGroup */thegroups = new ArrayList();
 
 		List emailroles = this.getEmailRoles();
 
 		for (Iterator i = emailroles.iterator(); i.hasNext();) {
 			EmailRole emailrole = (EmailRole) i.next();
 
 			if (emailrole.roletype.equals("role")) {
 				String realmid = emailrole.getRealmid();
 
 				AuthzGroup therealm = null;
 				try {
 					therealm = m_realmService.getAuthzGroup(realmid);
 				} catch (Exception e) {
 					log.debug("Exception: Mailtool.getEmailGroups() #1, "
 							+ e.getMessage());
 				}
 				Set users = therealm.getUsersHasRole(emailrole.getRoleid());
 				List /* EmailUser */mailusers = new ArrayList();
 				for (Iterator j = users.iterator(); j.hasNext();) {
 					String userid = (String) j.next();
 					try {
 						User theuser = m_userDirectoryService.getUser(userid);
 						String firstname_for_display = "";
 						String lastname_for_display = "";
 						if (theuser.getFirstName().trim().equals("")) {
 							if (theuser.getEmail().trim().equals("")
 									&& theuser.getLastName().trim().equals(""))
 								firstname_for_display = theuser.getDisplayId(); // fix
 																				// for
 																				// SAK-7539
 							else
 								firstname_for_display = theuser.getEmail(); // fix
 																			// for
 																			// SAK-7356
 						} else {
 							firstname_for_display = theuser.getFirstName();
 						}
 						lastname_for_display = theuser.getLastName();
 						EmailUser emailuser = new EmailUser(theuser.getId(),
 								firstname_for_display, lastname_for_display,
 								theuser.getEmail());
 						mailusers.add(emailuser);
 					} catch (Exception e) {
 						log.debug("Exception: Mailtool.getEmailGroups() #2, "
 								+ e.getMessage());
 					}
 				}
 				Collections.sort(mailusers);
 				EmailGroup thegroup = new EmailGroup(emailrole, mailusers);
 				thegroups.add(thegroup);
 			} 
 			else if (emailrole.roletype.equals("role_groupaware")) { // fix SAK-10076
 					String realmid = emailrole.getRealmid();
 
 					AuthzGroup therealm = null;
 					try {
 						therealm = m_realmService.getAuthzGroup(realmid);
 					} catch (Exception e) {
 						log.debug("Exception: Mailtool.getEmailGroups() #1, "
 								+ e.getMessage());
 					}
 					Set users = therealm.getUsersHasRole(emailrole.getRoleid());
 					List /* EmailUser */mailusers = new ArrayList();
 					for (Iterator j = users.iterator(); j.hasNext();) {
 						String userid = (String) j.next();
 						try {
 							User theuser = m_userDirectoryService.getUser(userid);
 							String firstname_for_display = "";
 							String lastname_for_display = "";
 							if (theuser.getFirstName().trim().equals("")) {
 								if (theuser.getEmail().trim().equals("")
 										&& theuser.getLastName().trim().equals(""))
 									firstname_for_display = theuser.getDisplayId(); // fix
 																					// for
 																					// SAK-7539
 								else
 									firstname_for_display = theuser.getEmail(); // fix
 																				// for
 																				// SAK-7356
 							} else {
 								firstname_for_display = theuser.getFirstName();
 							}
 							lastname_for_display = theuser.getLastName();
 							EmailUser emailuser = new EmailUser(theuser.getId(),
 									firstname_for_display, lastname_for_display,
 									theuser.getEmail());
 							mailusers.add(emailuser);
 						} catch (Exception e) {
 							log.debug("Exception: Mailtool.getEmailGroups() #2, "
 									+ e.getMessage());
 						}
 					}
 					Collections.sort(mailusers);
 					EmailGroup thegroup = new EmailGroup(emailrole, mailusers);
 					thegroups.add(thegroup);
 				}
 			else if (emailrole.roletype.equals("group")) {
 				String sid = getSiteID();
 				Site currentSite = null;
 				try {
 					currentSite = siteService.getSite(sid);
 				} catch (Exception e) {
 					log.debug("Exception: Mailtool.getEmailGroups() #3, "
 							+ e.getMessage());
 				}
 				Collection groups = currentSite.getGroups();
 				Group agroup = null;
 				for (Iterator groupIterator = groups.iterator(); groupIterator
 						.hasNext();) {
 					agroup = (Group) groupIterator.next();
 					String groupname = agroup.getTitle();
 					if (emailrole.getRoleid().equals(groupname))
 						break;
 				}
 				Set users2 = agroup.getUsers();
 				List mailusers2 = new ArrayList();
 				for (Iterator k = users2.iterator(); k.hasNext();) {
 					String userid2 = (String) k.next();
 					try {
 						User theuser2 = m_userDirectoryService.getUser(userid2);
 						String firstname_for_display = "";
 						String lastname_for_display = "";
 						if (theuser2.getFirstName().trim().equals("")) {
 							if (theuser2.getEmail().trim().equals("")
 									&& theuser2.getLastName().trim().equals(""))
 								firstname_for_display = theuser2.getDisplayId(); // fix
 																					// for
 																					// SAK-7539
 							else
 								firstname_for_display = theuser2.getEmail(); // fix
 																				// for
 																				// SAK-7356
 						} else {
 							firstname_for_display = theuser2.getFirstName();
 						}
 
 						lastname_for_display = theuser2.getLastName();
 						EmailUser emailuser2 = new EmailUser(theuser2.getId(),
 								firstname_for_display, lastname_for_display,
 								theuser2.getEmail());
 						mailusers2.add(emailuser2);
 					} catch (Exception e) {
 						log.debug("Exception: Mailtool.getEmailGroups() #3-1, "
 								+ e.getMessage());
 					}
 				}
 				Collections.sort(mailusers2);
 				EmailGroup thegroup2 = new EmailGroup(emailrole, mailusers2);
 				thegroups.add(thegroup2);
 			} // else
 			else if (emailrole.roletype.equals("section")) {
 				String sid = getSiteID();
 				Site currentSite = null;
 				try {
 					currentSite = siteService.getSite(sid);
 				} catch (Exception e) {
 					log.debug("Exception: Mailtool.getEmailGroups() #4, "
 							+ e.getMessage());
 				}
 				Collection groups = currentSite.getGroups();
 				Group agroup = null;
 				for (Iterator groupIterator = groups.iterator(); groupIterator
 						.hasNext();) {
 					agroup = (Group) groupIterator.next();
 					String groupname = agroup.getTitle();
 					if (emailrole.getRoleid().equals(groupname))
 						break;
 				}
 				Set users2 = agroup.getUsers();
 				List mailusers2 = new ArrayList();
 				for (Iterator k = users2.iterator(); k.hasNext();) {
 					String userid2 = (String) k.next();
 					try {
 						User theuser2 = m_userDirectoryService.getUser(userid2);
 						String firstname_for_display = "";
 						String lastname_for_display = "";
 						if (theuser2.getFirstName().trim().equals("")) {
 							if (theuser2.getEmail().trim().equals("")
 									&& theuser2.getLastName().trim().equals(""))
 								firstname_for_display = theuser2.getDisplayId(); // fix
 																					// for
 																					// SAK-7539
 							else
 								firstname_for_display = theuser2.getEmail(); // fix
 																				// for
 																				// SAK-7356
 						} else {
 							firstname_for_display = theuser2.getFirstName();
 						}
 
 						lastname_for_display = theuser2.getLastName();
 
 						EmailUser emailuser2 = new EmailUser(theuser2.getId(),
 								firstname_for_display, lastname_for_display,
 								theuser2.getEmail());
 
 						mailusers2.add(emailuser2);
 					} catch (Exception e) {
 						log.debug("Exception: Mailtool.getEmailGroups() #4-1, "
 								+ e.getMessage());
 					}
 				}
 				Collections.sort(mailusers2);
 				EmailGroup thegroup2 = new EmailGroup(emailrole, mailusers2);
 				thegroups.add(thegroup2);
 			} // else
 		}
 		return thegroups;
 	}
 
 	/**
 	 * Build all groups that will be used for this
 	 * @param roletypefilter
 	 * @return
 	 * 	return EmailGroup by Type
 	 */
 	public List /* EmailGroup */getEmailGroupsByType(String roletypefilter) {
 		List /* EmailGroup */thegroups = new ArrayList();
 
 		List emailroles = this.getEmailRoles();
 
 		for (Iterator i = emailroles.iterator(); i.hasNext();) {
 			EmailRole emailrole = (EmailRole) i.next();
 			if (emailrole.roletype.equals("role")
 					&& roletypefilter.equals("role")) {
 				String realmid = emailrole.getRealmid();
 				AuthzGroup therealm = null;
 				try {
 					// therealm = m_realmService.getRealm(realmid);
 					therealm = m_realmService.getAuthzGroup(realmid);
 				} catch (Exception e) {
 					log.debug("Exception: Mailtool.getEmailGroups() #1, "
 							+ e.getMessage());
 				}
 				Set users = therealm.getUsersHasRole(emailrole.getRoleid());
 				List /* EmailUser */mailusers = new ArrayList();
 				for (Iterator j = users.iterator(); j.hasNext();) {
 					String userid = (String) j.next();
 					try {
 						User theuser = m_userDirectoryService.getUser(userid);
 						// trying to fix SAK-7356 (Guests are not included in
 						// recipient lists)
 						// also SAK-7539
 						String firstname_for_display = "";
 						String lastname_for_display = "";
 						if (theuser.getFirstName().trim().equals("")) {
 							if (theuser.getEmail().trim().equals("")
 									&& theuser.getLastName().trim().equals(""))
 								firstname_for_display = theuser.getDisplayId(); // fix
 																				// for
 																				// SAK-7539
 							else
 								firstname_for_display = theuser.getEmail(); // fix
 																			// for
 																			// SAK-7356
 						} else {
 							firstname_for_display = theuser.getFirstName();
 						}
 
 						lastname_for_display = theuser.getLastName();
 
 						EmailUser emailuser = new EmailUser(theuser.getId(),
 								firstname_for_display, lastname_for_display,
 								theuser.getEmail());
 						mailusers.add(emailuser);
 					} catch (Exception e) {
 						log.debug("Exception: Mailtool.getEmailGroups() #2, "
 								+ e.getMessage());
 					}
 				}
 				Collections.sort(mailusers);
 				EmailGroup thegroup = new EmailGroup(emailrole, mailusers);
 				thegroups.add(thegroup);
 			}
 
 			else if (emailrole.roletype.equals("group")
 					&& roletypefilter.equals("group")) {
 				String sid = getSiteID();
 				Site currentSite = null;
 				try {
 					currentSite = siteService.getSite(sid);
 				} catch (Exception e) {
 					log.debug("Exception: Mailtool.getEmailGroups() #3, "
 							+ e.getMessage());
 				}
 				Collection groups = currentSite.getGroups();
 				Group agroup = null;
 				for (Iterator groupIterator = groups.iterator(); groupIterator
 						.hasNext();) {
 					agroup = (Group) groupIterator.next();
 					String groupname = agroup.getTitle();
 					if (emailrole.getRoleid().equals(groupname))
 						break;
 				}
 				Set users2 = agroup.getUsersHasRole(groupAwareRoleFound);
 				List mailusers2 = new ArrayList();
 				for (Iterator k = users2.iterator(); k.hasNext();) {
 					String userid2 = (String) k.next();
 					try {
 						User theuser2 = m_userDirectoryService.getUser(userid2);
 						String firstname_for_display = "";
 						String lastname_for_display = "";
 						if (theuser2.getFirstName().trim().equals("")) {
 							if (theuser2.getEmail().trim().equals("")
 									&& theuser2.getLastName().trim().equals(""))
 								firstname_for_display = theuser2.getDisplayId(); // fix
 																					// for
 																					// SAK-7539
 							else
 								firstname_for_display = theuser2.getEmail(); // fix
 																				// for
 																				// SAK-7356
 						} else {
 							firstname_for_display = theuser2.getFirstName();
 						}
 
 						lastname_for_display = theuser2.getLastName();
 
 						EmailUser emailuser2 = new EmailUser(theuser2.getId(),
 								firstname_for_display, lastname_for_display,
 								theuser2.getEmail());
 
 						mailusers2.add(emailuser2);
 					} catch (Exception e) {
 						log.debug("Exception: Mailtool.getEmailGroups() #3-1, "
 								+ e.getMessage());
 					}
 				}
 				Collections.sort(mailusers2);
 				EmailGroup thegroup2 = new EmailGroup(emailrole, mailusers2);
 				thegroups.add(thegroup2);
 			} // else
 			else if (emailrole.roletype.equals("section")
 					&& roletypefilter.equals("section")) {
 				String sid = getSiteID();
 				Site currentSite = null;
 				try {
 					currentSite = siteService.getSite(sid);
 				} catch (Exception e) {
 					log.debug("Exception: Mailtool.getEmailGroups() #4, "
 							+ e.getMessage());
 				}
 				Collection groups = currentSite.getGroups();
 				Group agroup = null;
 				for (Iterator groupIterator = groups.iterator(); groupIterator
 						.hasNext();) {
 					agroup = (Group) groupIterator.next();
 					String groupname = agroup.getTitle();
 					if (emailrole.getRoleid().equals(groupname))
 						break;
 				}
 				Set users2 = agroup.getUsersHasRole(groupAwareRoleFound);
 
 				List mailusers2 = new ArrayList();
 				for (Iterator k = users2.iterator(); k.hasNext();) {
 					String userid2 = (String) k.next();
 					try {
 						User theuser2 = m_userDirectoryService.getUser(userid2);
 						String firstname_for_display = "";
 						String lastname_for_display = "";
 						if (theuser2.getFirstName().trim().equals("")) {
 							if (theuser2.getEmail().trim().equals("")
 									&& theuser2.getLastName().trim().equals(""))
 								firstname_for_display = theuser2.getDisplayId(); // fix
 																					// for
 																					// SAK-7539
 							else
 								firstname_for_display = theuser2.getEmail(); // fix
 																				// for
 																				// SAK-7356
 						} else {
 							firstname_for_display = theuser2.getFirstName();
 						}
 						lastname_for_display = theuser2.getLastName();
 						EmailUser emailuser2 = new EmailUser(theuser2.getId(),
 								firstname_for_display, lastname_for_display,
 								theuser2.getEmail());
 						mailusers2.add(emailuser2);
 					} catch (Exception e) {
 						log.debug("Exception: Mailtool.getEmailGroups() #4-1, "
 								+ e.getMessage());
 					}
 				}
 				Collections.sort(mailusers2);
 				EmailGroup thegroup2 = new EmailGroup(emailrole, mailusers2);
 				thegroups.add(thegroup2);
 			} // else
 			else if (emailrole.roletype.equals("role_groupaware")
 					&& roletypefilter.equals("role_groupaware")) {
 				String realmid = emailrole.getRealmid();
 				AuthzGroup therealm = null;
 				try {
 					therealm = m_realmService.getAuthzGroup(realmid);
 				} catch (Exception e) {
 					log.debug("Exception: Mailtool.getEmailGroups() #5, "
 							+ e.getMessage());
 				}
 				Set users = therealm.getUsersHasRole(emailrole.getRoleid());
 				List /* EmailUser */mailusers = new ArrayList();
 				for (Iterator j = users.iterator(); j.hasNext();) {
 					String userid = (String) j.next();
 					try {
 						User theuser = m_userDirectoryService.getUser(userid);
 						// trying to fix SAK-7356 (Guests are not included in
 						// recipient lists)
 						// also SAK-7539
 						String firstname_for_display = "";
 						String lastname_for_display = "";
 						if (theuser.getFirstName().trim().equals("")) {
 							if (theuser.getEmail().trim().equals("")
 									&& theuser.getLastName().trim().equals(""))
 								firstname_for_display = theuser.getDisplayId(); // fix
 																				// for
 																				// SAK-7539
 							else
 								firstname_for_display = theuser.getEmail(); // fix
 																			// for
 																			// SAK-7356
 						} else {
 							firstname_for_display = theuser.getFirstName();
 						}
 						lastname_for_display = theuser.getLastName();
 						EmailUser emailuser = new EmailUser(theuser.getId(),
 								firstname_for_display, lastname_for_display,
 								theuser.getEmail());
 						mailusers.add(emailuser);
 					} catch (Exception e) {
 						log.debug("Exception: Mailtool.getEmailGroups() #5-1, "
 								+ e.getMessage());
 					}
 				}
 				Collections.sort(mailusers);
 				EmailGroup thegroup = new EmailGroup(emailrole, mailusers);
 				thegroups.add(thegroup);
 			} // else
 		}
 		return thegroups;
 	}
 
 	/**
 	 * @return
 	 * 		return the current user
 	 */
 	public EmailUser getCurrentUser() {
 		EmailUser euser = null;
 		User curU = null;
 		try {
 			curU = m_userDirectoryService.getCurrentUser();
 			euser = new EmailUser(curU.getId(), curU.getDisplayName(), curU
 					.getEmail());
 		} catch (Exception e) {
 			log
 					.debug("Exception: Mailtool.getCurrentUser(), "
 							+ e.getMessage());
 		}
 		return euser;
 	}
 
 	/**
 	 * Append email to Email Archive
 	 * 
 	 * @param channelRef
 	 * @param sender
 	 * @param subject
 	 * @param body
 	 * @return
 	 * 		true if success
 	 */
 	protected boolean appendToArchive(String channelRef, String sender,
 			String subject, String body) {
 		MailArchiveChannel channel = null;
 		try {
 			channel = MailArchiveService.getMailArchiveChannel(channelRef);
 		} catch (Exception e) {
 			log.debug("Exception: Mailtool.appendToArchive() #1, "
 					+ e.getMessage());
 			return false;
 		}
 		if (channel == null) {
 			log.debug("Mailtool: The channel: " + channelRef + " is null.");
 
 			return false;
 		}
 		List mailHeaders = new Vector();
 		if (isFCKeditor() || isHTMLArea()) {
 			mailHeaders.add(MailArchiveService.HEADER_OUTER_CONTENT_TYPE
 					+ ": text/html; charset=ISO-8859-1");
 			mailHeaders.add(MailArchiveService.HEADER_INNER_CONTENT_TYPE
 					+ ": text/html; charset=ISO-8859-1");
 		} else {
 			mailHeaders.add(MailArchiveService.HEADER_OUTER_CONTENT_TYPE
 					+ ": text/plain; charset=ISO-8859-1");
 			mailHeaders.add(MailArchiveService.HEADER_INNER_CONTENT_TYPE
 					+ ": text/plain; charset=ISO-8859-1");
 		}
 		mailHeaders.add("Mime-Version: 1.0");
 		mailHeaders.add("From: " + sender);
 		mailHeaders.add("Reply-To: " + sender);
 		try {
 			// This way actually sends the email too
 			// channel.addMailArchiveMessage(subject, sender,
 			// TimeService.newTime(), mailHeaders, null, body);
 			MailArchiveMessageEdit edit = (MailArchiveMessageEdit) channel
 					.addMessage();
 			MailArchiveMessageHeaderEdit header = edit
 					.getMailArchiveHeaderEdit();
 			edit.setBody(body);
 			header.replaceAttachments(null);
 			header.setSubject(subject);
 			header.setFromAddress(sender);
 			header.setDateSent(TimeService.newTime());
 			header.setMailHeaders(mailHeaders);
 			channel.commitMessage(edit, NotificationService.NOTI_NONE);
 		} catch (Exception e) {
 			log.debug("Exception: Mailtool.appendToArchive() #2, "
 					+ e.getMessage());
 
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * File upload via valuechangeEvent listener
 	 * @param event
 	 * @throws AbortProcessingException
 	 */
 	public void processFileUpload(ValueChangeEvent event)
 			throws AbortProcessingException {
 		Attachment att = new Attachment();
 		int maxnumattachment = getMaxNumAttachment();
 		if (num_files < maxnumattachment) {
 			try {
 				FileItem item = (FileItem) event.getNewValue();
 				long fileSize = item.getSize();
 
 				filename = item.getName();
 				if (filename != null) {
 					filename = FilenameUtils.getName(filename);
 					att.setFilename(filename);
 				}
 				String ud = getUploadDirectory();
 				File dir = new File(ud);
 
 				if (isNotAlreadyUploaded(filename, attachedFiles) == false
 						&& dir.isDirectory()) {
 					File fNew = new File(getUploadDirectory(), this
 							.getCurrentUser().getUserid()
 							+ "-" + filename);
 					// in IE, fi.getName() returns full path, while in FF,
 					// returns only name.
 					att.setSize((String) Long.toString(fileSize));
 					att.setId(num_id);
 					num_files++;
 					num_id++;
 					item.write(fNew);
 					attachedFiles.add(att);
 				}
 			} catch (Exception e) {
 				log.debug("Exception: Mailtool.processFileUpload(), "
 						+ e.getMessage()); // handle exception
 			}
 		} // end if
 	}
 
 	/**
 	 * remove the attachment file specified by id
 	 */
 	public void processRemoveFile() {
 		String id = getFacesParamValue(facesContext, "id");
 		Attachment a = null;
 		Attachment aForRemoval = null;
 		Iterator iter = attachedFiles.iterator();
 		while (iter.hasNext()) {
 			a = (Attachment) iter.next();
 			if (id.equals(a.getFilename())) {
 				aForRemoval = a;
 			}
 		}
 		attachedFiles.remove(aForRemoval);
 		num_files--;
 	}
 
 	public boolean isNotAlreadyUploaded(String s, List attachedFiles) {
 		Attachment a = null;
 		Iterator iter = attachedFiles.iterator();
 		while (iter.hasNext()) {
 			a = (Attachment) iter.next();
 			if (s.equals(a.getFilename())) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public void toggle_attachClicked() {
 		attachClicked = attachClicked ? false : true;
 	}
 
 	/**
 	 * @return
 	 *		return attachedFiles List
 	 */
 	public List getAllAttachments() {
 		return attachedFiles;
 	}
 
 	public static String getFacesParamValue(FacesContext fc, String name) {
 		return (String) fc.getCurrentInstance().getExternalContext()
 				.getRequestParameterMap().get(name);
 	}
 
 	/**
 	 * Validate email address(throws validatorexception if the entered string does not look like email address
 	 * 
 	 * @param context
 	 * @param toValidate
 	 * @param value
 	 * @throws ValidatorException
 	 */
 	public void validateEmail(FacesContext context, UIComponent toValidate,
 			Object value) throws ValidatorException {
 		String enteredEmail = (String) value;
 		Pattern p = Pattern.compile("(.+@.+\\.[a-z]+)"); // Set the email
 															// pattern string
 
 		// Match the given string with the pattern
 		Matcher m = p.matcher(enteredEmail);
 
 		// Check whether match is found
 		boolean matchFound = m.matches();
 
 		if (!matchFound) {
 			FacesMessage message = new FacesMessage();
 			message.setDetail("Email not valid in Other Recipients field");
 			message.setSummary("Email not valid in Other Recipients field");
 			message.setSeverity(FacesMessage.SEVERITY_ERROR);
 			throw new ValidatorException(message);
 		}
 	}
 
 	public String getReplyToSelected() {
 		return m_replyto;
 	}
 
 	public void setReplyToSelected(String r) {
 		this.m_replyto = r;
 	}
 
 	public boolean isEmailArchiveInSite() {
 		return EmailArchiveInSite;
 	}
 
 	public void setEmailArchiveInSite(boolean emailArchiveInSite) {
 		EmailArchiveInSite = emailArchiveInSite;
 	}
 
 	public boolean isGroupAwareRoleviewClicked() {
 		return groupAwareRoleviewClicked;
 	}
 
 	public void setGroupAwareRoleviewClicked(boolean groupAwareRoleviewClicked) {
 		this.groupAwareRoleviewClicked = groupAwareRoleviewClicked;
 	}
 
 	public boolean isAllGroupAwareRoleSelected() {
 		return allGroupAwareRoleSelected;
 	}
 
 	public void setAllGroupAwareRoleSelected(boolean allGroupAwareRoleSelected) {
 		this.allGroupAwareRoleSelected = allGroupAwareRoleSelected;
 	}
 
 	public int getNum_groupawarerole() {
 		return num_groupawarerole;
 	}
 
 	public void setNum_groupawarerole(int num_groupawarerole) {
 		this.num_groupawarerole = num_groupawarerole;
 	}
 
 	public boolean isGroupAwareRoleExist() {
 		return GroupAwareRoleExist;
 	}
 
 	public void setGroupAwareRoleExist(boolean groupAwareRoleExist) {
 		GroupAwareRoleExist = groupAwareRoleExist;
 	}
 
 	public String getSitename() {
 		return sitename;
 	}
 
 	public void setSitename(String sitename) {
 		this.sitename = sitename;
 	}
 }
