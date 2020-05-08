 package org.xwiki.sankore;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import org.apache.axis.utils.StringUtils;
 import org.slf4j.Logger;
 import org.xwiki.component.annotation.Component;
 import org.xwiki.configuration.ConfigurationSource;
 import org.xwiki.context.Execution;
 import org.xwiki.model.EntityType;
 import org.xwiki.model.reference.DocumentReference;
 import org.xwiki.model.reference.DocumentReferenceResolver;
 import org.xwiki.model.reference.EntityReference;
 import org.xwiki.model.reference.SpaceReference;
 import org.xwiki.model.reference.WikiReference;
 import org.xwiki.query.QueryException;
 import org.xwiki.query.QueryManager;
 import org.xwiki.sankore.internal.InvitationClass;
 import org.xwiki.sankore.internal.InvitationXObjectDocument;
 import org.xwiki.sankore.internal.UserXObjectDocument;
 import org.xwiki.sankore.internal.XWikiUsersClass;
 
 import com.xpn.xwiki.XWikiContext;
 import com.xpn.xwiki.XWikiException;
 import com.xpn.xwiki.web.Utils;
 
 @Component
 public class DefaultMembershipManager implements MembershipManager
 {
     @Inject
     private Logger logger;
 
     @Inject
     private Execution execution;
 
     @Inject
     private QueryManager queryManager;
 
     @Inject
     private MailSender mailSender;
 
     @Inject
     @Named("wiki")
     ConfigurationSource wikiPreferences;
 
     private static final String ADMIN_EMAIL_PREFERENCE_KEY = "admin_email";
 
     private Group group;
 
     private XWikiContext xWikiContext;
 
     public static final String DEFAULT_RESOURCES_SPACE = "Groups";
     public static final String DEFAULT_MAILTEMPLATE_NAME = "MailTemplate";
 
     @SuppressWarnings("unchecked")
     private DocumentReferenceResolver<EntityReference> currentReferenceDocumentReferenceResolver = Utils.getComponent(
             DocumentReferenceResolver.class, "current/reference");
 
     public DefaultMembershipManager(Group group)
             throws XWikiException
     {
         this.group = group;
         this.xWikiContext = ContextUtils.getXWikiContext(this.execution.getContext());
     }
 
     private DocumentReference getDefaultMailTemplate(String type, String action)
     {
         String templateName = DEFAULT_MAILTEMPLATE_NAME + type + action;
         EntityReference templateReference = new EntityReference(templateName, EntityType.DOCUMENT, new EntityReference(DEFAULT_RESOURCES_SPACE, EntityType.SPACE));
 
        return currentReferenceDocumentReferenceResolver.resolve(templateReference);;
     }
 
     private boolean isRegisteredUser(String usernameOrEmail) throws XWikiException
     {
         /*
         usernameOrEmail = usernameOrEmail.trim();
         if (!isEmailAddress(usernameOrEmail)) {
             if (StringUtils.isEmpty(usernameOrEmail))
                 return false;
 
             XWikiContext xWikiContext = ContextUtils.getXWikiContext(this.execution.getContext());
 
             UserXObjectDocument UserXObjectDocument = XWikiUsersClass.getInstance(xWikiContext)
                     .newXObjectDocument(usernameOrEmail, xWikiContext);
 
             if (UserXObjectDocument.isNew()) {
                     return false;
             }
 
             return true;
         }
 
         XWikiUsersClass.getInstance(execution.getContext()).searchXObjectDocumentsByField("email", usernameOrEmail, "String", )
 
         // email address
         List<String> results = new ArrayList<String>();
 
         try {
             results = this.queryManager.createQuery("select distinct doc.fullName from XWikiDocument as doc, BaseObject as obj, StringProperty as prop where doc.space='" + UserXObjectDocument.DEFAULT_USERS_SPACE + "'" + " and obj.name=doc.fullName and obj.className='" + XWikiUsersClass.getInstance(this.execution).getClassFullName() + "' and prop.id.id=obj.id and prop.name='" + XWikiUsersClass.FIELD_EMAIL + "' and prop.value='" + usernameOrEmail + "'", "xwql")
                     .setLimit(1).setOffset(0).execute();
         } catch (QueryException qe) {
         }
 
         if (!results.isEmpty()) {
              return true;
         }  */
 
         return false;
     }
 
     public boolean inviteUser(String usernameOrEmail) throws XWikiException
     {
         /*
         if (StringUtils.isEmpty(usernameOrEmail))
             return false;
 
         XWikiUsersClass xWikiUsersClass = XWikiUsersClass.getInstance(xWikiContext);
         UserXObjectDocument invitee = null;
         // email address or username
         if (usernameOrEmail.contains("@")) {
             List list = xWikiUsersClass.searchXObjectDocumentsByField(XWikiUsersClass.FIELD_EMAIL,
                     usernameOrEmail,
                     XWikiUsersClass.FIELDT_EMAIL,
                     xWikiContext);
             if (!list.isEmpty())
                 invitee = (UserXObjectDocument) list.get(0);
         } else {
             invitee = xWikiUsersClass.getXObjectDocument(usernameOrEmail, 0, true, xWikiContext);
         }
 
         if (invitee != null) {
             InvitationClass invitationClass = InvitationClass.getInstance(xWikiContext);
             if (!invitationClass.getXObjectDocument(invitee.getFullName(), 0, true, xWikiContext)) {
                 invitationClass.newXObjectDocument()
             }
         }*/
         return false;
     }
 
     public InvitationXObjectDocument createInvitation(String usernameOrEmail, UserXObjectDocument inviter)
             throws XWikiException
     {
         InvitationClass invitationClass = InvitationClass.getInstance(xWikiContext);
 
         InvitationXObjectDocument invitationXObjectDocument = invitationClass.newXObjectDocument(
                 usernameOrEmail,
                 this.group.getInvitationsSpaceReference(),
                 xWikiContext);
 
         invitationXObjectDocument.setRequestDate(new Date());
         invitationXObjectDocument.setInviter(inviter.getFullName());
         invitationXObjectDocument.setStatus(InvitationClass.FIELD_STATUS_CREATED);
 
         invitationXObjectDocument.save();
 
         return invitationXObjectDocument;
     }
 
     public boolean sendInvitation(InvitationXObjectDocument invitationXObjectDocument)
         throws XWikiException
     {
         if (invitationXObjectDocument.getStatus().equals(InvitationClass.FIELD_STATUS_CREATED)) {
 
             String from = wikiPreferences.getProperty(ADMIN_EMAIL_PREFERENCE_KEY, String.class);
            UserXObjectDocument invitee = XWikiUsersClass.getInstance(.newXObjectDocument(invitationXObjectDocument.getInvitee(), execution.getContext());
 
            mailSender.sendMailFromTemplate(getDefaultMailTemplate("Invitation", InvitationClass.FIELD_STATUS_SENT), from, invitee.getEmail(), )
         }
 
         return false;
     }
 }
