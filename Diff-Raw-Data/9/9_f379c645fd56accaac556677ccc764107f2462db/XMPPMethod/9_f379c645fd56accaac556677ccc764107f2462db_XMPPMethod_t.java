 /*
  * May 14, 2006 9:10:20 AM
  * $Id$
  */
 package com.thinkparity.ophelia.model.io.xmpp;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.nio.charset.Charset;
 import java.text.DateFormat;
 import java.text.MessageFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import java.util.Map.Entry;
 
 import com.thinkparity.codebase.DateUtil;
 import com.thinkparity.codebase.ErrorHelper;
 import com.thinkparity.codebase.OS;
 import com.thinkparity.codebase.Constants.XmlRpc;
 import com.thinkparity.codebase.DateUtil.DateImage;
 import com.thinkparity.codebase.assertion.Assert;
 import com.thinkparity.codebase.email.EMail;
 import com.thinkparity.codebase.email.EMailBuilder;
 import com.thinkparity.codebase.jabber.JabberId;
 import com.thinkparity.codebase.jabber.JabberIdBuilder;
 import com.thinkparity.codebase.log4j.Log4JWrapper;
 
 import com.thinkparity.codebase.model.artifact.ArtifactReceipt;
 import com.thinkparity.codebase.model.artifact.ArtifactState;
 import com.thinkparity.codebase.model.artifact.ArtifactType;
 import com.thinkparity.codebase.model.artifact.ArtifactVersion;
 import com.thinkparity.codebase.model.backup.Statistics;
 import com.thinkparity.codebase.model.contact.Contact;
 import com.thinkparity.codebase.model.contact.ContactInvitation;
 import com.thinkparity.codebase.model.container.Container;
 import com.thinkparity.codebase.model.container.ContainerVersion;
 import com.thinkparity.codebase.model.container.ContainerVersionArtifactVersionDelta.Delta;
 import com.thinkparity.codebase.model.document.Document;
 import com.thinkparity.codebase.model.document.DocumentVersion;
 import com.thinkparity.codebase.model.document.DocumentVersionContent;
 import com.thinkparity.codebase.model.migrator.Error;
 import com.thinkparity.codebase.model.migrator.Feature;
 import com.thinkparity.codebase.model.migrator.Product;
 import com.thinkparity.codebase.model.migrator.Release;
 import com.thinkparity.codebase.model.migrator.Resource;
 import com.thinkparity.codebase.model.profile.EMailReservation;
 import com.thinkparity.codebase.model.profile.Profile;
 import com.thinkparity.codebase.model.profile.ProfileEMail;
 import com.thinkparity.codebase.model.profile.UsernameReservation;
 import com.thinkparity.codebase.model.session.Credentials;
 import com.thinkparity.codebase.model.session.Environment;
 import com.thinkparity.codebase.model.session.TemporaryCredentials;
 import com.thinkparity.codebase.model.stream.StreamSession;
 import com.thinkparity.codebase.model.user.TeamMember;
 import com.thinkparity.codebase.model.user.User;
 import com.thinkparity.codebase.model.user.UserVCard;
 import com.thinkparity.codebase.model.util.Token;
 import com.thinkparity.codebase.model.util.xmpp.event.XMPPEvent;
 import com.thinkparity.codebase.model.util.xstream.XStreamUtil;
 
 import com.thinkparity.ophelia.model.Constants.Xml;
 import com.thinkparity.ophelia.model.Constants.Xml.Service;
 import com.thinkparity.ophelia.model.io.xmpp.XMPPMethodResponse.Result;
 import com.thinkparity.ophelia.model.util.xmpp.XMPPNetworkUtil;
 import com.thinkparity.ophelia.model.util.xstream.SmackXppReader;
 
 import org.jivesoftware.smack.PacketCollector;
 import org.jivesoftware.smack.XMPPConnection;
 import org.jivesoftware.smack.filter.PacketIDFilter;
 import org.jivesoftware.smack.packet.IQ;
 import org.jivesoftware.smack.provider.IQProvider;
 import org.jivesoftware.smack.provider.ProviderManager;
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 
 /**
  * <b>Title:</b>thinkParity XMPP Method<br>
  * <b>Description:</b>An xmpp method is used to execute remote method calls.
  * Similar in nature to a jdbc network call the xmpp method will serialize
  * parameters and return an xmpp method result.
  * 
  * @author raymond@thinkparity.com
  * @version $Revision$
  */
 @SuppressWarnings("unchecked")
 public class XMPPMethod extends IQ {
 
 	/** The local <code>Locale</code>. */
     private static final Locale LOCALE;
 
     /** A <code>Log4JWrapper</code>. */
     private static final Log4JWrapper LOGGER;
 
     /** The local <code>TimeZone</code>. */
     private static final TimeZone TIME_ZONE;
 
     /** The universal <code>DateFormat</code>. */
     private static final DateFormat UNIVERSAL_FORMAT;
 
     /** The universal <code>DateImage</code>. */
     private static final DateImage UNIVERSAL_IMAGE;
 
     /** A <code>TimeZone</code>. */
     private static final TimeZone UNIVERSAL_TIME_ZONE;
 
     /** An <code>XStreamUtil</code> instance. */
     private static final XStreamUtil XSTREAM_UTIL;
 
     static {
         LOCALE = Locale.getDefault();
         LOGGER = new Log4JWrapper();
         TIME_ZONE = TimeZone.getDefault();
         UNIVERSAL_IMAGE = DateImage.ISO;
         UNIVERSAL_TIME_ZONE = TimeZone.getTimeZone("Universal"); 
         final String universalPattern = UNIVERSAL_IMAGE.toString();
         UNIVERSAL_FORMAT = new SimpleDateFormat(universalPattern);
         UNIVERSAL_FORMAT.setTimeZone(UNIVERSAL_TIME_ZONE);
         XSTREAM_UTIL = XStreamUtil.getInstance();
 
         ProviderManager.addIQProvider(Xml.Service.NAME, Xml.Service.NAMESPACE_RESPONSE, new XMPPMethodResponseProvider());
     }
 
     /** A map of remote method parameters to their values. */
     protected final List<XMPPMethodParameter> parameters;
 
     /** The number of milliseconds it took to execute the method. */
     private Long executionTime;
 
     /** The remote method name. */
     private final String name;
 
     /**
 	 * An <code>XMPPNetworkUtil</code> used to calculate the execution
 	 * timeout.
 	 */
     private final XMPPNetworkUtil networkUtil;
 
     /** The parameter xml serialization time. */
     private Long serializationTime;
 
     private Float timeoutFudge;
 
     /**
 	 * Create XMPPMethod.
 	 * 
 	 * @param name
 	 *            A method name.
 	 * @param timeout
 	 *            A number of milliseconds to wait before timing out.
 	 */
     public XMPPMethod(final String name) {
     	this(name, null);
     }
 
     /**
 	 * Create XMPPMethod.  Create method instance that will not time out.
 	 * 
 	 * @param name
 	 *            A method name.
 	 */
     public XMPPMethod(final String name, final XMPPNetworkUtil networkUtil) {
     	super();
         this.parameters = new LinkedList<XMPPMethodParameter>();
         this.name = name;
         this.networkUtil = networkUtil;
     }
 
     /**
      * Execute this method on the give connection.
      * 
      * @param xmppConnection
      *            An xmpp connection.
      * @return An xmpp method response.
      */
     public XMPPMethodResponse execute(final XMPPConnection xmppConnection) {
         // add an executed on parameter
         setParameter(Xml.All.EXECUTED_ON, DateUtil.getInstance());
         // create a collector for the response
         final PacketCollector idCollector = createPacketCollector(xmppConnection);
     	final long start = System.currentTimeMillis();
         xmppConnection.sendPacket(this);
         // this sleep has been inserted because when packets are sent within
         // x milliseconds of each other, they tend to get swallowed by the
         // smack library
         try {
         	Thread.sleep(75);
         } catch (final InterruptedException ix) {}
         try {
     		final Long timeout = null == networkUtil
     				? null : networkUtil.calculateTimeout(this);
     		XMPPMethodResponse response = null == timeout
     				? (XMPPMethodResponse) idCollector.nextResult()
 					: (XMPPMethodResponse) idCollector.nextResult(timeout);
 			executionTime = Long.valueOf(System.currentTimeMillis() - start);
             if (null == response && null != timeout) {
         		LOGGER.logWarning("XMPP method {0} has timed out after {1}ms execution time {2}ms serialization time.",
         				name, executionTime, serializationTime);
         		throw new XMPPTimeoutException(this);
             }
            LOGGER.logInfo("XMPP method {0} has executed in {1} ms.", name,
                     executionTime);
             return response;
         } catch (final Throwable t) {
             final String errorId = new ErrorHelper().getErrorId(t);
             LOGGER.logError(t, errorId);
             LOGGER.logError("name:{0}", name);
             LOGGER.logError("xmppConnection:{0}", xmppConnection);
             throw new XMPPException(errorId);
         } finally {
             // re-set the parameters post execution
             LOGGER.logVariable("postResponseCollected", DateUtil.getInstance());
             parameters.clear();
         }
     }
 
     /**
      * @see org.jivesoftware.smack.packet.IQ#getChildElementXML()
      *
      */
     public String getChildElementXML() {
     	final long start = System.currentTimeMillis();
         final String childElementXML = new StringBuilder()
             .append("<query xmlns=\"jabber:iq:parity:").append(name).append("\">")
             .append(getParametersXML())
             .append("</query>")
             .toString();
         serializationTime = Long.valueOf(System.currentTimeMillis() - start);
         LOGGER.logVariable("childElementXML.length()", childElementXML.length());
         LOGGER.logVariable("childElementXML", childElementXML);
         return childElementXML;
     }
 
     /**
 	 * Obtain the number of milliseconds to execute the method.
 	 * 
 	 * @return A number of milliseconds <code>Long</code>.
 	 */
     public Long getExecutionTime() {
     	return executionTime;
     }
 
     public final String getName() {
     	return name;
     }
 
     public Integer getParameterSize() {
     	return parameters.size();
     }
 
     /**
 	 * Obtain the number of milliseconds to serialize the parameters/results.
 	 * 
 	 * @return A number of milliseconds <code>Long</code>.
 	 */
     public Long getSerializationTime() {
     	return serializationTime;
     }
 
     public Float getTimeoutFudge() { 
     	return timeoutFudge;
     }
 
     public Boolean isSetTimeoutFudge() {
     	return null != timeoutFudge;
     }
 
     public final void setArtifactReceiptParameter(final String name,
             final List<ArtifactReceipt> values) {
         final List<XMPPMethodParameter> parameters = new ArrayList<XMPPMethodParameter>(values.size());
         for (final ArtifactReceipt value : values) {
             parameters.add(new XMPPMethodParameter(XmlRpc.LIST_ITEM, value.getClass(), value));
         }
         this.parameters.add(new XMPPMethodParameter(name, List.class, parameters));
     }
 
     public final void setContactsParameter(final String name,
             final List<Contact> value) {
         setListParameter(name, value);
     }
 
     public final void setDocumentVersionsStreamIdsParameter(final String name,
             final Map<DocumentVersion, String> value) {
         setMapParameter(name, value);
     }
 
     public final void setEMailsParameter(final String name,
             final List<EMail> value) {
         setListParameter(name, value);
     }
 
     /**
      * Set a list of jabber id parameters.
      * 
      * @param listName
      *            The list name.
      * @param name
      *            The list item names.
      * @param values
      *            The list values.
      */
     public final void setJabberIdParameters(final String listName, final String name,
             final List<JabberId> values) {
         final List<XMPPMethodParameter> parameters = new LinkedList<XMPPMethodParameter>();
         for(final JabberId value : values) {
             parameters.add(new XMPPMethodParameter(name, JabberId.class, value));
         }
         this.parameters.add(new XMPPMethodParameter(listName, List.class, parameters));
     }
 
     public final void setLongParameters(final String listName, final String name,
             final List<Long> values) {
         final List<XMPPMethodParameter> parameters = new LinkedList<XMPPMethodParameter>();
         for(final Long value : values)
             parameters.add(new XMPPMethodParameter(name, Long.class, value));
 
         this.parameters.add(new XMPPMethodParameter(listName, List.class, parameters));
     }
 
     public final void setParameter(final String name, final ArtifactType value) {
         parameters.add(new XMPPMethodParameter(name, ArtifactType.class, value));
     }
 
     public final void setParameter(final String name, final Calendar value) {
         parameters.add(new XMPPMethodParameter(name, Calendar.class, value));
     }
 
     public final void setParameter(final String name, final Credentials value) {
         parameters.add(new XMPPMethodParameter(name, Credentials.class, value));
     }
 
     public final void setParameter(final String name, final EMail value) {
         parameters.add(new XMPPMethodParameter(name, EMail.class, value));
     }
 
     public final void setParameter(final String name, final EMailReservation value) {
         parameters.add(new XMPPMethodParameter(name, EMailReservation.class, value));
     }
 
     public final void setParameter(final String name, final Error value) {
         parameters.add(new XMPPMethodParameter(name, Error.class, value));
     }
 
     public final void setParameter(final String name, final Integer value) {
         parameters.add(new XMPPMethodParameter(name, Integer.class, value));
     }
 
     public final void setParameter(final String name, final JabberId value) {
         parameters.add(new XMPPMethodParameter(name, JabberId.class, value));
     }
 
     public final void setParameter(final String name, final Locale value) {
         parameters.add(new XMPPMethodParameter(name, value.getClass(), value));
     }
 
     /**
      * Set a named parameter.
      * 
      * @param name
      *            The parameter name.
      * @param value
      *            The parameter value.
      */
     public final void setParameter(final String name, final Long value) {
         parameters.add(new XMPPMethodParameter(name, Long.class, value));
     }
 
     public final void setParameter(final String name, final OS value) {
         parameters.add(new XMPPMethodParameter(name, OS .class, value));
     }
 
     public final void setParameter(final String name, final Product value) {
         parameters.add(new XMPPMethodParameter(name, Product.class, value));
     }
 
     public final void setParameter(final String name, final Profile value) {
         parameters.add(new XMPPMethodParameter(name, Profile.class, value));
     }
 
     public final void setParameter(final String name, final ProfileEMail value) {
         parameters.add(new XMPPMethodParameter(name, ProfileEMail.class, value));
     }
 
     public final void setParameter(final String name, final Release value) {
         parameters.add(new XMPPMethodParameter(name, Release.class, value));
     }
 
     /**
      * Set a named parameter.
      * 
      * @param name
      *            The parameter name.
      * @param value
      *            The parameter value.
      */
     public final void setParameter(final String name, final String value) {
         parameters.add(new XMPPMethodParameter(name, String.class, value));
     }
 
     /**
      * Set a list of jabber id parameters.
      * 
      * @param listName
      *            The list parameter name.
      * @param itemName
      *            The item parameter name.
      * @param value
      *            The item value.
      */
     public final void setParameter(final String listName,
             final String itemName, final List<JabberId> values) {
         final List<XMPPMethodParameter> parameters = new ArrayList<XMPPMethodParameter>(values.size());
         for (final JabberId value : values) {
             parameters.add(new XMPPMethodParameter(itemName, JabberId.class, value));
         }
         this.parameters.add(new XMPPMethodParameter(listName, List.class, parameters));
     }
 
     public final <T extends ArtifactVersion> void setParameter(
             final String name, final T value) {
         parameters.add(new XMPPMethodParameter(name, value.getClass(), value));
     }
 
     public final <T extends ContactInvitation> void setParameter(
             final String name, final T value) {
         parameters.add(new XMPPMethodParameter(name, value.getClass(), value));
     }
 
     public final <T extends TimeZone> void setParameter(
             final String name, final T value) {
         parameters.add(new XMPPMethodParameter(name, value.getClass(), value));
     }
     
     public final void setParameter(final String name, final TemporaryCredentials value) {
         parameters.add(new XMPPMethodParameter(name, TemporaryCredentials.class, value));
     }
 
     public final void setParameter(final String name, final UsernameReservation value) {
         parameters.add(new XMPPMethodParameter(name, UsernameReservation.class, value));
     }
 
     public final void setParameter(final String name, final UserVCard value) {
         parameters.add(new XMPPMethodParameter(name, value.getClass(), value));
     }
 
     /**
      * Set a named parameter.
      * 
      * @param name
      *            The parameter name.
      * @param value
      *            The parameter value.
      */
     public final void setParameter(final String name, final UUID value) {
         parameters.add(new XMPPMethodParameter(name, UUID.class, value));
     }
 
     public final void setResourceParameters(final String name,
             final List<Resource> values) {
         final List<XMPPMethodParameter> parameters = new LinkedList<XMPPMethodParameter>();
         for(final Resource value : values)
             parameters.add(new XMPPMethodParameter(XmlRpc.LIST_ITEM, Resource.class, value));
         this.parameters.add(new XMPPMethodParameter(name, List.class, parameters));
     }
     
     public final void setTeamMembersParameter(final String name,
             final List<TeamMember> value) {
         setListParameter(name, value);
     }
 
     public void setTimeoutFudge(final Float timeoutFudge) {
     	this.timeoutFudge = timeoutFudge;
     }
 
     public final void setUsersParameter(final String name,
             final List<User> value) {
         /* NOTE i'm not using the generic setListParameter here because i want
          * to force slicing the value to a user object instead
          * of contact/profile/team member. */
         final List<XMPPMethodParameter> parameters = new LinkedList<XMPPMethodParameter>();
         for(final User element : value) {
             parameters.add(new XMPPMethodParameter("element", User.class, (User) element));
         }
         this.parameters.add(new XMPPMethodParameter(name, List.class, parameters));
     }
 
     /**
      * Create a packet collector that will filter on packets with the same
      * query id.
      *
      * @param iq
      *      The internet query.
      * @return A packet collector.
      */
     protected PacketCollector createPacketCollector(
             final XMPPConnection xmppConnection) {
         return xmppConnection.createPacketCollector(
                 new PacketIDFilter(getPacketID()));
     }
 
     /**
      * Flush the internal parameter map.
      *
      * @return A buffer of xml containing the parameters.
      */
     protected String getParametersXML() {
         final StringBuffer xml = new StringBuffer();
 
         for(final XMPPMethodParameter parameter : parameters)
             xml.append(getParameterXML(parameter));
 
         return xml.toString();
     }
 
     private String getParameterXML(final XMPPMethodParameter parameter) {
         final StringBuffer xml = new StringBuffer();
         xml.append("<").append(parameter.name).append(" javaType=\"")
                 .append(parameter.javaType.getName())
                 .append("\"");
         if (null == parameter.javaValue) {
             xml.append("/>");
         } else {
             xml.append(">").append(getParameterXMLValue(parameter))
                 .append("</").append(parameter.name).append(">");
         }
         return xml.toString();
     }
 
     private String getParameterXMLValue(final Calendar value) {
         final Calendar universalCalendar = (Calendar) value.clone();
         universalCalendar.setTimeZone(UNIVERSAL_TIME_ZONE);
         return ((DateFormat) UNIVERSAL_FORMAT).format(universalCalendar.getTime());
     }
 
     private String getParameterXMLValue(final XMPPMethodParameter parameter) {
         if (parameter.javaType.equals(ArtifactType.class)) {
             return parameter.javaValue.toString();
         } else if (parameter.javaType.equals(Calendar.class)) {
             return getParameterXMLValue((Calendar) parameter.javaValue);
         } else if (parameter.javaType.equals(DocumentVersionContent.class)) {
             final DocumentVersionContent dvc = (DocumentVersionContent) parameter.javaValue;
             return new StringBuffer("")
                     .append(getParameterXML(new XMPPMethodParameter("uniqueId", UUID.class, dvc.getVersion().getArtifactUniqueId())))
                     .append(getParameterXML(new XMPPMethodParameter("versionId", Long.class, dvc.getVersion().getVersionId())))
                     .append(getParameterXML(new XMPPMethodParameter("bytes", byte[].class, dvc.getContent())))
                     .toString();
         } else if (parameter.javaType.equals(EMail.class)) {
             return parameter.javaValue.toString();
         } else if (parameter.javaType.equals(Contact.class)
                 || parameter.javaType.equals(User.class)
                 || parameter.javaType.equals(TeamMember.class)
                 || parameter.javaType.equals(Profile.class)) {
             final StringWriter xmlWriter = new StringWriter();
             XSTREAM_UTIL.toXML(parameter.javaValue, xmlWriter);
             return xmlWriter.toString();
         } else if (parameter.javaType.equals(ContainerVersion.class)) {
             final StringWriter xmlWriter = new StringWriter();
             XSTREAM_UTIL.toXML(parameter.javaValue, xmlWriter);
             return xmlWriter.toString();
         } else if (parameter.javaType.equals(DocumentVersion.class)) {
             final StringWriter xmlWriter = new StringWriter();
             XSTREAM_UTIL.toXML(parameter.javaValue, xmlWriter);
             return xmlWriter.toString();
         } else if (parameter.javaType.equals(TimeZone.class)) {
             final StringWriter xmlWriter = new StringWriter();
             XSTREAM_UTIL.toXML(parameter.javaValue, xmlWriter);
             return xmlWriter.toString();
         } else if (parameter.javaType.equals(Locale.class)) {
             final StringWriter xmlWriter = new StringWriter();
             XSTREAM_UTIL.toXML(parameter.javaValue, xmlWriter);
             return xmlWriter.toString();
         } else if (parameter.javaType.equals(Credentials.class)) {
             final StringWriter xmlWriter = new StringWriter();
             XSTREAM_UTIL.toXML(parameter.javaValue, xmlWriter);
             return xmlWriter.toString();
         } else if (parameter.javaType.equals(TemporaryCredentials.class)) {
             final StringWriter xmlWriter = new StringWriter();
             XSTREAM_UTIL.toXML(parameter.javaValue, xmlWriter);
             return xmlWriter.toString();
         } else if (parameter.javaType.equals(EMail.class)) {
             return parameter.javaValue.toString();
         } else if (parameter.javaType.equals(Integer.class)) {
             return parameter.javaValue.toString();
         } else if (parameter.javaType.equals(JabberId.class)) {
             return ((JabberId) parameter.javaValue).getQualifiedJabberId();
         } else if (parameter.javaType.equals(Long.class)) {
             return parameter.javaValue.toString();
         } else if (parameter.javaType.equals(List.class)) {
             final List<XMPPMethodParameter> listItems = (List<XMPPMethodParameter>) parameter.javaValue;
             final StringBuffer xmlValue = new StringBuffer("");
             if (null != listItems && 0 < listItems.size()) {
                 for(final XMPPMethodParameter listItem : listItems) {
                     xmlValue.append(getParameterXML(listItem));
                 }
             }
             return xmlValue.toString();
         } else if (parameter.javaType.equals(Map.class)) {
             final Map<XMPPMethodParameter, XMPPMethodParameter> map =
                 (Map<XMPPMethodParameter, XMPPMethodParameter>) parameter.javaValue;
             final StringBuffer xmlValue = new StringBuffer("");
             if (null != map && 0 < map.size()) {
                 for (final Entry<XMPPMethodParameter, XMPPMethodParameter> entry : map.entrySet()) {
                     xmlValue.append(getParameterXML(entry.getKey()));
                     xmlValue.append(getParameterXML(entry.getValue()));
                 }
             }
             return xmlValue.toString();
         } else if (parameter.javaType.equals(OS.class)) {
             return ((OS) parameter.javaValue).name();
         } else if (parameter.javaType.equals(String.class)) {
             return parameter.javaValue.toString();
         } else if (parameter.javaType.equals(UUID.class)) {
             return parameter.javaValue.toString();
         } else if (ArtifactReceipt.class.isAssignableFrom(parameter.javaType)) {
             final StringWriter xmlWriter = new StringWriter();
             XSTREAM_UTIL.toXML(parameter.javaValue, xmlWriter);
             return xmlWriter.toString();
         } else if (Error.class.isAssignableFrom(parameter.javaType)) {
             final StringWriter xmlWriter = new StringWriter();
             XSTREAM_UTIL.toXML(parameter.javaValue, xmlWriter);
             return xmlWriter.toString();
         } else if (ContactInvitation.class.isAssignableFrom(parameter.javaType)) {
             final StringWriter xmlWriter = new StringWriter();
             XSTREAM_UTIL.toXML(parameter.javaValue, xmlWriter);
             return xmlWriter.toString();
         } else if (EMailReservation.class.isAssignableFrom(parameter.javaType)) {
             final StringWriter xmlWriter = new StringWriter();
             XSTREAM_UTIL.toXML(parameter.javaValue, xmlWriter);
             return xmlWriter.toString();
         } else if (UsernameReservation.class.isAssignableFrom(parameter.javaType)) {
             final StringWriter xmlWriter = new StringWriter();
             XSTREAM_UTIL.toXML(parameter.javaValue, xmlWriter);
             return xmlWriter.toString();
         } else if (Product.class.isAssignableFrom(parameter.javaType)) {
             final StringWriter xmlWriter = new StringWriter();
             XSTREAM_UTIL.toXML(parameter.javaValue, xmlWriter);
             return xmlWriter.toString();
         } else if (Release.class.isAssignableFrom(parameter.javaType)) {
             final StringWriter xmlWriter = new StringWriter();
             XSTREAM_UTIL.toXML(parameter.javaValue, xmlWriter);
             return xmlWriter.toString();
         } else if (Resource.class.isAssignableFrom(parameter.javaType)) {
             final StringWriter xmlWriter = new StringWriter();
             XSTREAM_UTIL.toXML(parameter.javaValue, xmlWriter);
             return xmlWriter.toString();
         } else if (UserVCard.class.isAssignableFrom(parameter.javaType)) {
             final StringWriter xmlWriter = new StringWriter();
             XSTREAM_UTIL.toXML(parameter.javaValue, xmlWriter);
             return xmlWriter.toString();
         } else {
             final String assertion =
                 MessageFormat.format("[XMPP METHOD] [GET PARAMTER XML VALUE] [UNKNOWN JAVA TYPE] [{0}]",
                         parameter.javaType.getName());
             throw new XMPPException(assertion);
         }
     }
 
     /**
      * Set a list parameter.
      * 
      * @param <T>
      *            A <code>List</code> type.
      * @param name
      *            A parameter name <code>String</code>.
      * @param value
      *            A parameter value <code>List</code>.
      */
     private final <T extends Object> void setListParameter(final String name,
             final List<T> value) {
         final List<XMPPMethodParameter> parameters = new LinkedList<XMPPMethodParameter>();
         for(final T element : value) {
             parameters.add(new XMPPMethodParameter("element", element.getClass(), element));
         }
         this.parameters.add(new XMPPMethodParameter(name, List.class, parameters));
     }
 
     /**
      * Set a map parameter.
      * 
      * @param <T>
      *            A <code>Map</code> key type.
      * @param <U>
      *            A <code>Map</code> value type.
      * @param name
      *            A parameter name <code>String</code>.
      * @param value
      *            A parameter value <code>Map</code>.
      */
     private final <T extends Object, U extends Object> void setMapParameter(
             final String name, final Map<T, U> value) {
         final Map<XMPPMethodParameter, XMPPMethodParameter> parameters = new HashMap<XMPPMethodParameter, XMPPMethodParameter>();
         for(final Entry<T, U> entry : value.entrySet()) {
             parameters.put(new XMPPMethodParameter("key", entry.getKey().getClass(), entry.getKey()),
                     new XMPPMethodParameter("value", entry.getValue().getClass(), entry.getValue()));
         }
         this.parameters.add(new XMPPMethodParameter(name, Map.class, parameters));
     }
 
     /** A remote result reader. */
     private static class XMPPMethodResponseProvider implements IQProvider {
 
         private final XStreamUtil xstreamUtil;
 
         private XMPPMethodResponseProvider() {
             super();
             this.xstreamUtil = XStreamUtil.getInstance();
         }
 
         /**
          * @see org.jivesoftware.smack.provider.IQProvider#parseIQ(org.xmlpull.v1.XmlPullParser)
          */
         public IQ parseIQ(final XmlPullParser parser) throws Exception {
             try {
                 return doParseIQ(parser);
             } catch (final Throwable t) {
                 LOGGER.logFatal(t, "Could not parse remote method response.");
                 throw new XMPPException(t);
             }
         }
 
         /**
          * Parse an xmpp remote method call response.
          * 
          * @param parser
          *            An <code>XmlPullParser</code>.
          * @return An <code>XMPPMethodResponse</code>.
          * @throws XmlPullParserException
          * @throws IOException
          */
         private XMPPMethodResponse doParseIQ(final XmlPullParser parser)
                 throws XmlPullParserException, IOException {
             final XMPPMethodResponse response = new XMPPMethodResponse();
             parser.next();
             while(true) {
                 LOGGER.logVariable("parser.getName()", parser.getName());
                 LOGGER.logVariable("parser.getDepth()", parser.getDepth());
                 LOGGER.logVariable("parser.getNamespace()", parser.getNamespace());
                 // stop processing when we hit the trailing query tag
                 if (XmlPullParser.END_TAG == parser.getEventType()) {
                     if (Service.NAME.equals(parser.getName())) {
                         break;
                     } else {
                         Assert.assertUnreachable(MessageFormat.format(
                                 "Parsing incomplete for xmlns {0}; tag {1} at depth {2}.",
                                 parser.getNamespace(), parser.getName(), parser.getDepth()));
                     }
                 } else {
                     response.writeResult(parseResult(parser));
                 }
             }
             return response;
         }
 
         /**
          * Parse a java calendar from a string. The date/time of the string is
          * taken to be in GMT.
          * 
          * @param s
          *            A date/time <code>String</code>.
          * @return A <code>Calendar</code> in the default locale/timezone.
          */
         private Calendar parseJavaCalendar(final String universalDateTime) {
             try {
                 final Date localDate = ((DateFormat) UNIVERSAL_FORMAT.clone()).parse(universalDateTime);
                 final Calendar localCalendar = Calendar.getInstance(TIME_ZONE, LOCALE);
                 localCalendar.setTimeInMillis(localDate.getTime());
                 return localCalendar;
             } catch (final ParseException px) {
                 throw new XMPPException(px);
             }
         }
 
         private Class parseJavaType(final XmlPullParser parser) {
             final String javaType = parser.getAttributeValue("", "javaType");
             try {
                 return Class.forName(javaType);
             } catch (final ClassNotFoundException cnfx) {
                 throw new XMPPException(MessageFormat.format(
                         "Java type {0} not supported.", javaType));
             }
         }
 
         private Object parseJavaValue(final XmlPullParser parser,
                 final Class javaType) throws XmlPullParserException, IOException {
             LOGGER.logVariable("javaType", javaType);
             if (javaType.equals(List.class)) {
                 if (parser.isEmptyElementTag()) {
                     parser.next();
                     parser.next();
                     return Collections.emptyList();
                 } else {
                     parser.next();  // move to first list item
                     final List javaValue = new ArrayList();
                     Class listJavaType;
                     while (XmlPullParser.END_TAG != parser.getEventType()) {
                         listJavaType = parseJavaType(parser);
                         javaValue.add(parseJavaValue(parser, listJavaType));
                     }
                     parser.next();  // move past end of list
                     return javaValue;
                 }
             } else if (javaType.equals(Map.class)) {
                 if (parser.isEmptyElementTag()) {
                     parser.next();
                     parser.next();
                     return Collections.emptyMap();
                 } else {
                     parser.next();
                     final Map javaValue = new HashMap();
                     Class mapKeyJavaType, mapValueJavaType;
                     Object mapKeyJavaValue, mapValueJavaValue;
                     while (XmlPullParser.END_TAG != parser.getEventType()) {
                         parser.next();  // TAG-BEGIN:entry
                         mapKeyJavaType = parseJavaType(parser);
                         mapKeyJavaValue = parseJavaValue(parser, mapKeyJavaType);
                         mapValueJavaType = parseJavaType(parser);
                         mapValueJavaValue = parseJavaValue(parser, mapValueJavaType);
                         parser.next();  // TAG-END:entry
                         javaValue.put(mapKeyJavaValue, mapValueJavaValue);
                     }
                     parser.next();
                     return javaValue;
                 }
             } else {
                 if (parser.isEmptyElementTag()) {
                     parser.next();
                     parser.next();
                     return null;
                 } else if (javaType.equals(ArtifactReceipt.class)) {
                     ArtifactReceipt artifactReceipt = null;
                     artifactReceipt = xstreamUtil.unmarshalArtifactReceipt(new SmackXppReader(parser), artifactReceipt);
                     parser.next();
                     parser.next();
                     return artifactReceipt;
                 } else if (javaType.equals(Container.class)) {
                     Container container = null;
                     container = xstreamUtil.unmarshalContainer(new SmackXppReader(parser), container);
                     parser.next();
                     parser.next();
                     return container;
                 } else if (javaType.equals(ProfileEMail.class)) {
                     ProfileEMail profileEMail = null;
                     profileEMail = (ProfileEMail) xstreamUtil.unmarshal(new SmackXppReader(parser), profileEMail);
                     parser.next();
                     parser.next();
                     return profileEMail;
                 } else if (javaType.equals(Feature.class)) {
                     Feature feature = null;
                     feature = (Feature) xstreamUtil.unmarshal(new SmackXppReader(parser), feature);
                     parser.next();
                     parser.next();
                     return feature;
                 } else if (javaType.equals(Locale.class)) {
                     Locale locale = null;
                     locale = (Locale) xstreamUtil.unmarshal(new SmackXppReader(parser), locale);
                     parser.next();
                     parser.next();
                     return locale;
                 } else if (javaType.equals(TimeZone.class)) {
                     TimeZone timeZone = null;
                     timeZone = (TimeZone) xstreamUtil.unmarshal(new SmackXppReader(parser), timeZone);
                     parser.next();
                     parser.next();
                     return timeZone;
                 } else if (javaType.equals(Product.class)) {
                     Product product = null;
                     product = (Product) xstreamUtil.unmarshal(new SmackXppReader(parser), product);
                     parser.next();
                     parser.next();
                     return product;
                 } else if (javaType.equals(EMailReservation.class)) {
                     EMailReservation reservation = null;
                     reservation = (EMailReservation) xstreamUtil.unmarshal(new SmackXppReader(parser), reservation);
                     parser.next();
                     parser.next();
                     return reservation;
                 } else if (javaType.equals(TemporaryCredentials.class)) {
                     TemporaryCredentials credentials = null;
                     credentials = (TemporaryCredentials) xstreamUtil.unmarshal(new SmackXppReader(parser), credentials);
                     parser.next();
                     parser.next();
                     return credentials;
                 } else if (javaType.equals(UsernameReservation.class)) {
                     UsernameReservation reservation = null;
                     reservation = (UsernameReservation) xstreamUtil.unmarshal(new SmackXppReader(parser), reservation);
                     parser.next();
                     parser.next();
                     return reservation;
                 } else if (javaType.equals(Release.class)) {
                     Release javaValue = null;
                     javaValue = (Release) xstreamUtil.unmarshal(new SmackXppReader(parser), javaValue);
                     parser.next();
                     parser.next();
                     return javaValue;
                 } else if (javaType.equals(Resource.class)) {
                     Resource javaValue = null;
                     javaValue = (Resource) xstreamUtil.unmarshal(new SmackXppReader(parser), javaValue);
                     parser.next();
                     parser.next();
                     return javaValue;
                 } else if (Statistics.class.isAssignableFrom(javaType)) {
                     Statistics javaValue = null;
                     javaValue = (Statistics) xstreamUtil.unmarshal(new SmackXppReader(parser), javaValue);
                     parser.next();
                     parser.next();
                     return javaValue;
                 } else if (javaType.equals(TeamMember.class)) {
                     TeamMember teamMember = null;
                     teamMember = xstreamUtil.unmarshalTeamMember(new SmackXppReader(parser), teamMember);
                     parser.next();
                     parser.next();
                     return teamMember;
                 } else if (javaType.equals(User.class)) {
                     User user = null;
                     user = xstreamUtil.unmarshalUser(new SmackXppReader(parser), user);
                     parser.next();
                     parser.next();
                     return user;
                 } else if (javaType.equals(Token.class)) {
                     Token token = null;
                     token = (Token) xstreamUtil.unmarshal(new SmackXppReader(parser), token);
                     parser.next();
                     parser.next();
                     return token;
                 } else if (ContactInvitation.class.isAssignableFrom(javaType)) {
                     ContactInvitation invitation = null;
                     invitation = (ContactInvitation) xstreamUtil.unmarshal(new SmackXppReader(parser), invitation);
                     parser.next();
                     parser.next();
                     return invitation;
                 } else if (UserVCard.class.isAssignableFrom(javaType)) {
                     UserVCard vcard = null;
                     vcard = (UserVCard) xstreamUtil.unmarshal(new SmackXppReader(parser), vcard);
                     parser.next();
                     parser.next();
                     return vcard;
                 } else if (XMPPEvent.class.isAssignableFrom(javaType)) {
                     XMPPEvent xmppEvent = null;
                     xmppEvent = xstreamUtil.unmarshalEvent(new SmackXppReader(parser), xmppEvent);
                     parser.next();
                     parser.next();
                     return xmppEvent;
                 } else {
                     parser.next();  // move to element text
                     final Object javaValue;
                     if (javaType.equals(String.class)) {
                         javaValue = parser.getText();
                         parser.next();
                     } else if (javaType.equals(ArtifactState.class)) {
                         javaValue = ArtifactState.valueOf(parser.getText());
                         parser.next();
                     } else if (javaType.equals(Environment.class)) {
                         javaValue = Environment.valueOf(parser.getText());
                         parser.next();
                     } else if (javaType.equals(Charset.class)) {
                         javaValue = Charset.forName(parser.getText());
                         parser.next();
                     } else if (javaType.equals(Delta.class)) {
                         javaValue = Delta.valueOf(parser.getText());
                         parser.next();
                     } else if (javaType.equals(ArtifactType.class)) {
                         javaValue = ArtifactType.valueOf(parser.getText());
                         parser.next();
                     } else if (javaType.equals(Boolean.class)) {
                         javaValue = Boolean.valueOf(parser.getText());
                         parser.next();
                     } else if (javaType.equals(Calendar.class)) {
                         javaValue = parseJavaCalendar(parser.getText());
                         parser.next();
                     } else if (javaType.equals(ContainerVersion.class)) {
                         javaValue = new ContainerVersion();
                         ((ContainerVersion) javaValue).setArtifactType((ArtifactType) parseJavaValue(parser, ArtifactType.class));
                         ((ContainerVersion) javaValue).setArtifactUniqueId((UUID) parseJavaValue(parser, UUID.class));
                         ((ContainerVersion) javaValue).setComment((String) parseJavaValue(parser, String.class));
                         ((ContainerVersion) javaValue).setCreatedBy((JabberId) parseJavaValue(parser, JabberId.class));
                         ((ContainerVersion) javaValue).setCreatedOn((Calendar) parseJavaValue(parser, Calendar.class));
                         ((ContainerVersion) javaValue).setName((String) parseJavaValue(parser, String.class));
                         ((ContainerVersion) javaValue).setUpdatedBy((JabberId) parseJavaValue(parser, JabberId.class));
                         ((ContainerVersion) javaValue).setUpdatedOn((Calendar) parseJavaValue(parser, Calendar.class));
                         ((ContainerVersion) javaValue).setVersionId((Long) parseJavaValue(parser, Long.class));
                     } else if (javaType.equals(Document.class)) {
                         javaValue = new Document();
                         ((Document) javaValue).setCreatedBy((JabberId) parseJavaValue(parser, JabberId.class));
                         ((Document) javaValue).setCreatedOn((Calendar) parseJavaValue(parser, Calendar.class));
                         ((Document) javaValue).setName((String) parseJavaValue(parser, String.class));
                         ((Document) javaValue).setState((ArtifactState) parseJavaValue(parser, ArtifactState.class));
                         ((Document) javaValue).setType((ArtifactType) parseJavaValue(parser, ArtifactType.class));
                         ((Document) javaValue).setUniqueId((UUID) parseJavaValue(parser, UUID.class));
                         ((Document) javaValue).setUpdatedBy((JabberId) parseJavaValue(parser, JabberId.class));
                         ((Document) javaValue).setUpdatedOn((Calendar) parseJavaValue(parser, Calendar.class));
                     } else if (javaType.equals(DocumentVersion.class)) {
                         javaValue = new DocumentVersion();
                         ((DocumentVersion) javaValue).setArtifactType((ArtifactType) parseJavaValue(parser, ArtifactType.class));
                         ((DocumentVersion) javaValue).setArtifactUniqueId((UUID) parseJavaValue(parser, UUID.class));
                         ((DocumentVersion) javaValue).setChecksum((String) parseJavaValue(parser, String.class));
                         ((DocumentVersion) javaValue).setChecksumAlgorithm((String) parseJavaValue(parser, String.class));
                         ((DocumentVersion) javaValue).setCreatedBy((JabberId) parseJavaValue(parser, JabberId.class));
                         ((DocumentVersion) javaValue).setCreatedOn((Calendar) parseJavaValue(parser, Calendar.class));
                         ((DocumentVersion) javaValue).setName((String) parseJavaValue(parser, String.class));
                         ((DocumentVersion) javaValue).setSize((Long) parseJavaValue(parser, Long.class));
                         ((DocumentVersion) javaValue).setUpdatedBy((JabberId) parseJavaValue(parser, JabberId.class));
                         ((DocumentVersion) javaValue).setUpdatedOn((Calendar) parseJavaValue(parser, Calendar.class));
                         ((DocumentVersion) javaValue).setVersionId((Long) parseJavaValue(parser, Long.class));
                     } else if (javaType.equals(EMail.class)) {
                         javaValue = EMailBuilder.parse(parser.getText());
                         parser.next();
                     } else if (javaType.equals(Integer.class)) {
                         javaValue = Integer.valueOf(parser.getText());
                         parser.next();
                     } else if (javaType.equals(StreamSession.class)) {
                         javaValue = new StreamSession();
                         ((StreamSession) javaValue).setBufferSize((Integer) parseJavaValue(parser, Integer.class));
                         ((StreamSession) javaValue).setCharset((Charset) parseJavaValue(parser, Charset.class));
                         ((StreamSession) javaValue).setEnvironment((Environment) parseJavaValue(parser, Environment.class));
                         ((StreamSession) javaValue).setId((String) parseJavaValue(parser, String.class));
                     } else if (javaType.equals(JabberId.class)) {
                         javaValue = JabberIdBuilder.parse(parser.getText());
                         parser.next();
                     } else if (javaType.equals(Long.class)) {
                         javaValue = Long.valueOf(parser.getText());
                         parser.next();
                     } else if (javaType.equals(UUID.class)) {
                         javaValue = UUID.fromString(parser.getText());
                         parser.next();
                     } else {
                         throw Assert.createUnreachable("Unsupported xml type {0}.", javaType.getName());
                     }
                     parser.next();  // move to next tag
                     return javaValue;
                 }
             }
         }
 
         private String parseName(final XmlPullParser parser) {
             return parser.getName();
         }
 
         private Result parseResult(final XmlPullParser parser)
                 throws XmlPullParserException, IOException {
             final Result result = new Result();
             result.name = parseName(parser);
             result.javaType = parseJavaType(parser);
             result.javaValue = parseJavaValue(parser, result.javaType);
             return result;
         }
     }
 }
