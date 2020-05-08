 /*
  * See the NOTICE file distributed with this work for additional
  * information regarding copyright ownership.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.xwiki.workflowpublication.internal;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import org.apache.commons.lang.StringUtils;
 import org.xwiki.component.annotation.Component;
 import org.xwiki.context.Execution;
 import org.xwiki.logging.LogLevel;
 import org.xwiki.logging.event.LogEvent;
 import org.xwiki.model.EntityType;
 import org.xwiki.model.reference.DocumentReference;
 import org.xwiki.model.reference.DocumentReferenceResolver;
 import org.xwiki.model.reference.EntityReference;
 import org.xwiki.model.reference.EntityReferenceSerializer;
 import org.xwiki.workflowpublication.PublicationRoles;
 import org.xwiki.workflowpublication.PublicationWorkflow;
 import org.xwiki.workflowpublication.WorkflowConfigManager;
 
 import com.xpn.xwiki.XWikiContext;
 import com.xpn.xwiki.XWikiException;
 import com.xpn.xwiki.doc.XWikiAttachment;
 import com.xpn.xwiki.doc.XWikiDocument;
 import com.xpn.xwiki.doc.merge.MergeConfiguration;
 import com.xpn.xwiki.doc.merge.MergeResult;
 import com.xpn.xwiki.objects.BaseObject;
 import com.xpn.xwiki.objects.BaseProperty;
 import com.xpn.xwiki.objects.classes.PropertyClass;
 import com.xpn.xwiki.web.XWikiMessageTool;
 
 /**
  * @version $Id$
  */
 @Component
 public class DefaultPublicationWorkflow implements PublicationWorkflow
 {
     public static final String WF_CONFIG_REF_FIELDNAME = "workflow";
 
     public static final String WF_TARGET_FIELDNAME = "target";
 
     public final static String WF_STATUS_FIELDNAME = "status";
 
     public final static String WF_IS_TARGET_FIELDNAME = "istarget";
 
     public final static int DRAFT = 0;
 
     public final static int PUBLISHED = 1;
 
     public final static String STATUS_MODERATING = "moderating";
 
     public final static String STATUS_VALIDATING = "validating";
 
     public final static String STATUS_VALID = "valid";
 
     public final static String STATUS_DRAFT = "draft";
 
     public final static String STATUS_PUBLISHED = "published";
 
     public final static String STATUS_ARCHIVED = "archived";
 
     public static final EntityReference COMMENTS_CLASS = new EntityReference("XWikiComments", EntityType.DOCUMENT,
         new EntityReference("XWiki", EntityType.SPACE));
 
     /**
      * The reference to the xwiki rights, relative to the current wiki. <br />
      */
     public static final EntityReference RIGHTS_CLASS = new EntityReference("XWikiRights", EntityType.DOCUMENT,
         new EntityReference("XWiki", EntityType.SPACE));
 
     /**
      * The groups property of the rights class.
      */
     public static final String RIGHTS_GROUPS = "groups";
 
     /**
      * The levels property of the rights class.
      */
     public static final String RIGHTS_LEVELS = "levels";
 
     /**
      * The users property of the rights class.
      */
     public static final String RIGHTS_USERS = "users";
 
     /**
      * The 'allow / deny' property of the rights class.
      */
     public static final String RIGHTS_ALLOWDENY = "allow";
 
     /**
      * For translations.
      */
     private XWikiMessageTool messageTool;
 
     /**
      * The execution, to get the context from it.
      */
     @Inject
     private Execution execution;
 
     /**
      * The current entity reference resolver, to resolve the notions class reference.
      */
     @Inject
     @Named("current")
     private DocumentReferenceResolver<EntityReference> currentReferenceEntityResolver;
 
     @Inject
     @Named("currentmixed")
     private DocumentReferenceResolver<String> currentMixedStringDocRefResolver;
 
     @Inject
     @Named("explicit")
     private DocumentReferenceResolver<String> explicitStringDocRefResolver;
 
     @Inject
     @Named("explicit")
     private DocumentReferenceResolver<EntityReference> explicitReferenceDocRefResolver;
 
     @Inject
     @Named("compactwiki")
     private EntityReferenceSerializer<String> compactWikiSerializer;
 
     @Inject
     private WorkflowConfigManager configManager;
 
     @Inject
     private PublicationRoles publicationRoles;
 
     /**
      * Reference string serializer.
      */
     @Inject
     private EntityReferenceSerializer<String> stringSerializer;
 
     /**
      * {@inheritDoc}
      * 
      * @see org.xwiki.workflowpublication.PublicationWorkflow#isWorkflowDocument(com.xpn.xwiki.doc.XWikiDocument,
      *      com.xpn.xwiki.XWikiContext)
      */
     @Override
     public boolean isWorkflowDocument(XWikiDocument document, XWikiContext context) throws XWikiException
     {
         BaseObject workflowInstance =
             document.getXObject(currentReferenceEntityResolver.resolve(PublicationWorkflow.PUBLICATION_WORKFLOW_CLASS));
         return workflowInstance != null;
     }
 
     @Override
     public DocumentReference getDraftDocument(DocumentReference targetRef, XWikiContext xcontext) throws XWikiException
     {
         String workflowsQuery =
             "select obj.name from BaseObject obj, StringProperty target, IntegerProperty istarget where "
                 + "obj.className = ? and obj.id = target.id.id and target.id.name = ? and target.value = ? and "
                 + "obj.id = istarget.id.id and istarget.id.name = ? and istarget.value = 0";
         List<String> params =
             Arrays.asList(compactWikiSerializer.serialize(PUBLICATION_WORKFLOW_CLASS), WF_TARGET_FIELDNAME,
                 compactWikiSerializer.serialize(targetRef), WF_IS_TARGET_FIELDNAME);
         List<String> results = xcontext.getWiki().getStore().search(workflowsQuery, 0, 0, params, xcontext);
 
         if (results.size() <= 0) {
             return null;
         }
 
         // if there are more results, use the first one
         return currentMixedStringDocRefResolver.resolve(results.get(0));
     }
 
     private DocumentReference createDraftDocument(DocumentReference targetRef, XWikiContext xcontext)
         throws XWikiException
     {
         if (getDraftDocument(targetRef, xcontext) != null) {
             return null;
         }
 
         XWikiDocument targetDocument = xcontext.getWiki().getDocument(targetRef, xcontext);
         // TODO: implement me
         return null;
 
     }
 
     @Override
     public boolean startWorkflow(DocumentReference docName, String workflowConfig, DocumentReference target,
         XWikiContext xcontext) throws XWikiException
     {
         XWikiDocument doc = xcontext.getWiki().getDocument(docName, xcontext);
 
         // Check that the target is free. i.e. no other workflow document targets this target
         if (this.getDraftDocument(target, xcontext) != null) {
             // TODO: put this error on the context
             return false;
         }
 
         BaseObject workflowObject =
             doc.newXObject(
                 explicitReferenceDocRefResolver.resolve(PublicationWorkflow.PUBLICATION_WORKFLOW_CLASS, docName),
                 xcontext);
         BaseObject wfConfig = configManager.getWorkflowConfig(workflowConfig, xcontext);
         if (wfConfig == null) {
             // TODO: put error on the context
             return false;
         }
 
         workflowObject.set(WF_CONFIG_REF_FIELDNAME, workflowConfig, xcontext);
         workflowObject.set(WF_TARGET_FIELDNAME, compactWikiSerializer.serialize(target, docName), xcontext);
 
         makeDocumentDraft(doc, workflowObject, xcontext);
 
         // save the document prepared like this
         String defaultMessage =
             "Started workflow " + workflowConfig + " on document " + stringSerializer.serialize(docName);
         String message =
            this.getMessage(defaultMessage, "workflow.save.start",
                 Arrays.asList(workflowConfig.toString(), stringSerializer.serialize(docName).toString()));
         xcontext.getWiki().saveDocument(doc, message, true, xcontext);
 
         return true;
     }
 
     @Override
     public List<String> startMatchingWorkflows(DocumentReference doc, DocumentReference target, XWikiContext xcontext)
     {
         // TODO: implement me
         return null;
     }
 
     @Override
     public boolean submitForModeration(DocumentReference document) throws XWikiException
     {
 
         XWikiContext xcontext = getXContext();
         XWikiDocument doc = xcontext.getWiki().getDocument(document, xcontext);
 
         BaseObject workflow = validateWorkflow(doc, Arrays.asList(STATUS_DRAFT), DRAFT, xcontext);
         if (workflow == null) {
             return false;
         }
 
         BaseObject wfConfig =
             configManager.getWorkflowConfig(workflow.getStringValue(WF_CONFIG_REF_FIELDNAME), xcontext);
 
         String moderators = publicationRoles.getModerators(wfConfig, xcontext);
         // if there are no moderators, submit the doc for validation instead of moderation
         if (StringUtils.isEmpty(moderators)) {
             return this.submitForValidation(document);
         }
 
         // put the status to moderating
         workflow.set(WF_STATUS_FIELDNAME, STATUS_MODERATING, xcontext);
         // and put the rights
         String validators = publicationRoles.getValidators(wfConfig, xcontext);
         String contributors = publicationRoles.getContributors(wfConfig, xcontext);
 
         // give the view and edit right to moderators and validators ...
         setRights(doc, Arrays.asList("edit", "view"), Arrays.asList(moderators, validators), Arrays.<String> asList(),
             true, xcontext);
         // ... and only view for contributors
         addRights(doc, Arrays.asList("view"), Arrays.asList(contributors), Arrays.<String> asList(), true, xcontext);
 
         // save the doc.
         // TODO: prevent the save protection from being executed, when it would be implemented
 
         // save the document prepared like this
         String defaultMessage = "Submitted document " + stringSerializer.serialize(document) + " for moderation ";
         String message =
             this.getMessage("workflow.save.submitForModeration", defaultMessage,
                 Arrays.asList(stringSerializer.serialize(document).toString()));
         xcontext.getWiki().saveDocument(doc, message, true, xcontext);
 
         return true;
     }
 
     @Override
     public boolean refuseModeration(DocumentReference document, String reason) throws XWikiException
     {
         XWikiContext xcontext = getXContext();
         XWikiDocument doc = xcontext.getWiki().getDocument(document, xcontext);
 
         BaseObject workflow = validateWorkflow(doc, Arrays.asList(STATUS_MODERATING), 0, xcontext);
         if (workflow == null) {
             return false;
         }
 
         // preconditions met, make the document back draft
         makeDocumentDraft(doc, workflow, xcontext);
 
         // save the document prepared like this
         String defaultMessage = "Refused moderation : " + reason;
         String message = getMessage("workflow.save.refuseModeration", defaultMessage, Arrays.asList(reason));
         xcontext.getWiki().saveDocument(doc, message, false, xcontext);
 
         return true;
     }
 
     @Override
     public boolean submitForValidation(DocumentReference document) throws XWikiException
     {
         XWikiContext xcontext = getXContext();
         XWikiDocument doc = xcontext.getWiki().getDocument(document, xcontext);
 
         BaseObject workflow = validateWorkflow(doc, Arrays.asList(STATUS_MODERATING, STATUS_DRAFT), DRAFT, xcontext);
         if (workflow == null) {
             return false;
         }
 
         // put the status to validating
         workflow.set(WF_STATUS_FIELDNAME, STATUS_VALIDATING, xcontext);
         // and put the rights
         BaseObject wfConfig =
             configManager.getWorkflowConfig(workflow.getStringValue(WF_CONFIG_REF_FIELDNAME), xcontext);
         String validators = publicationRoles.getValidators(wfConfig, xcontext);
         String contributors = publicationRoles.getContributors(wfConfig, xcontext);
         String moderators = publicationRoles.getModerators(wfConfig, xcontext);
 
         // give the view and edit right to validators ...
         setRights(doc, Arrays.asList("edit", "view"), Arrays.asList(validators), Arrays.<String> asList(), true,
             xcontext);
         // ... and only view for contributors and moderators
         addRights(doc, Arrays.asList("view"), Arrays.asList(moderators, contributors), Arrays.<String> asList(), true,
             xcontext);
 
         // save the doc.
         // TODO: prevent the save protection from being executed.
 
         // save the document prepared like this
         String defaultMessage = "Submitted document " + stringSerializer.serialize(document) + "for validation.";
         String message =
             getMessage("workflow.save.submitForValidation", defaultMessage,
                 Arrays.asList(stringSerializer.serialize(document).toString()));
         xcontext.getWiki().saveDocument(doc, message, true, xcontext);
 
         return true;
     }
 
     @Override
     public boolean refuseValidation(DocumentReference document, String reason) throws XWikiException
     {
         XWikiContext xcontext = getXContext();
         XWikiDocument doc = xcontext.getWiki().getDocument(document, xcontext);
 
         BaseObject workflow = validateWorkflow(doc, Arrays.asList(STATUS_VALIDATING), 0, xcontext);
         if (workflow == null) {
             return false;
         }
 
         // preconditions met, make the document back draft
         makeDocumentDraft(doc, workflow, xcontext);
 
         // save the document prepared like this
         String defaultMessage = "Refused publication : " + reason;
         String message = getMessage("workflow.save.refuseValidation", defaultMessage, Arrays.asList(reason));
         xcontext.getWiki().saveDocument(doc, message, false, xcontext);
 
         return true;
     }
 
     @Override
     public boolean validate(DocumentReference document) throws XWikiException
     {
         XWikiContext xcontext = getXContext();
         XWikiDocument doc = xcontext.getWiki().getDocument(document, xcontext);
 
         BaseObject workflow = validateWorkflow(doc, Arrays.asList(STATUS_VALIDATING), DRAFT, xcontext);
         if (workflow == null) {
             return false;
         }
 
         // put the status to valid
         workflow.set(WF_STATUS_FIELDNAME, STATUS_VALID, xcontext);
         // rights stay the same, only validator has the right to edit the document in the valid state, all other
         // participants to workflow can view it.
 
         // save the document prepared like this
         String defaultMessage = "Marked document " + stringSerializer.serialize(document) + " as valid.";
         String message =
             getMessage("workflow.save.validate", defaultMessage,
                 Arrays.asList(stringSerializer.serialize(document).toString()));
         xcontext.getWiki().saveDocument(doc, message, true, xcontext);
 
         return true;
     }
 
     @Override
     public DocumentReference publish(DocumentReference document) throws XWikiException
     {
         XWikiContext xcontext = getXContext();
         XWikiDocument doc = xcontext.getWiki().getDocument(document, xcontext);
 
         // we can only publish from validating state, check that
         BaseObject workflow = validateWorkflow(doc, Arrays.asList(STATUS_VALIDATING, STATUS_VALID), DRAFT, xcontext);
         if (workflow == null) {
             return null;
         }
 
         String target = workflow.getStringValue(WF_TARGET_FIELDNAME);
         if (StringUtils.isEmpty(target)) {
             return null;
         }
         DocumentReference targetRef = explicitStringDocRefResolver.resolve(target, document);
         XWikiDocument newDocument = xcontext.getWiki().getDocument(targetRef, xcontext);
 
         // TODO: handle checking if the target document is free...
 
         // TODO: do this for all the languages of document to copy from, and remove the languages which are not anymore
         try {
             this.copyContentsToNewVersion(doc, newDocument, xcontext);
         } catch (IOException e) {
             throw new XWikiException(XWikiException.MODULE_XWIKI_DOC, XWikiException.ERROR_XWIKI_UNKNOWN,
                 "Error accessing attachments when copying document "
                     + stringSerializer.serialize(doc.getDocumentReference()) + " to document "
                     + stringSerializer.serialize(newDocument.getDocumentReference()), e);
         }
 
         // setup the workflow and target flag, if a workflow doesn't exist already
         BaseObject newWorkflow = newDocument.getXObject(PUBLICATION_WORKFLOW_CLASS);
         if (newWorkflow == null) {
             newWorkflow = newDocument.newXObject(PUBLICATION_WORKFLOW_CLASS, xcontext);
             newWorkflow.set(WF_STATUS_FIELDNAME, STATUS_PUBLISHED, xcontext);
             newWorkflow.set(WF_IS_TARGET_FIELDNAME, 1, xcontext);
             newWorkflow.set(WF_TARGET_FIELDNAME, target, xcontext);
             newWorkflow.set(WF_CONFIG_REF_FIELDNAME, workflow.getStringValue(WF_CONFIG_REF_FIELDNAME), xcontext);
         }
 
         // TODO: figure out who should be the author of the published document
         // save the published document prepared like this
         String defaultMessage = "Published new version of the document.";
         String message = getMessage("workflow.save.publishNew", defaultMessage, null);
         xcontext.getWiki().saveDocument(newDocument, message, false, xcontext);
 
         // prepare the draft document as well
         // set the status
         workflow.set(WF_STATUS_FIELDNAME, STATUS_PUBLISHED, xcontext);
         // give back the rights to the contributors, or do we? TODO: find out!
         BaseObject wfConfig =
             configManager.getWorkflowConfig(workflow.getStringValue(WF_CONFIG_REF_FIELDNAME), xcontext);
         if (wfConfig != null) {
             String contributors = publicationRoles.getContributors(wfConfig, xcontext);
             String moderators = publicationRoles.getModerators(wfConfig, xcontext);
             String validators = publicationRoles.getValidators(wfConfig, xcontext);
 
             // give the view and edit right to contributors, moderators and validators
             setRights(doc, Arrays.asList("edit", "view"), Arrays.asList(contributors, moderators, validators),
                 Arrays.<String> asList(), true, xcontext);
         }
 
         // save the the draft document prepared like this
         String defaultMessage2 = "Published this document to " + stringSerializer.serialize(document) + ".";
         String message2 =
             getMessage("workflow.save.publishDraft", defaultMessage2,
                 Arrays.asList(stringSerializer.serialize(targetRef).toString()));
         xcontext.getWiki().saveDocument(doc, message2, false, xcontext);
 
         return targetRef;
     }
 
     @Override
     public DocumentReference unpublish(DocumentReference document, boolean forceToDraft) throws XWikiException
     {
         XWikiContext xcontext = getXContext();
         XWikiDocument targetDoc = xcontext.getWiki().getDocument(document, xcontext);
 
         // check that the document to unpublish is a workflow published document
         BaseObject targetWorkflow =
             validateWorkflow(targetDoc, Arrays.asList(STATUS_PUBLISHED, STATUS_ARCHIVED), PUBLISHED, xcontext);
         if (targetWorkflow == null) {
             return null;
         }
 
         // get the draft ref
         DocumentReference draftDocRef = this.getDraftDocument(document, xcontext);
         if (draftDocRef != null) {
             // if there is a draft reference, check whether we need to overwrite it with the published version or not
             XWikiDocument draftDoc = xcontext.getWiki().getDocument(draftDocRef, xcontext);
             BaseObject workflow = draftDoc.getXObject(PUBLICATION_WORKFLOW_CLASS);
             String draftStatus = workflow.getStringValue(WF_STATUS_FIELDNAME);
             if (STATUS_PUBLISHED.equals(draftStatus) || !forceToDraft) {
                 // a draft exists and it's either in state published, which means identical as the published doc, or
                 // some draft and the overwriting of draft is not required
                 // do nothing, draft will stay in place and target will be deleted at the end of this function
                 if (STATUS_PUBLISHED.equals(draftStatus)) // If status is published, change draft status back to draft
                 {
                     // make the draft doc draft again
                     makeDocumentDraft(draftDoc, workflow, xcontext);
                     // save the draft document
                     String defaultMessage =
                         "Created draft from published document" + stringSerializer.serialize(document) + ".";
                     String message =
                         getMessage("workflow.save.unpublish", defaultMessage,
                             Arrays.asList(stringSerializer.serialize(document).toString()));
                     xcontext.getWiki().saveDocument(draftDoc, message, true, xcontext);
                 }
             } else {
                 // the existing draft is not published and force to draft is required
                 // copy the contents from target to draft
                 try {
                     // TODO: do this for all the languages of document to copy from, and remove the languages which are
                     // not anymore
                     this.copyContentsToNewVersion(targetDoc, draftDoc, xcontext);
                 } catch (IOException e) {
                     throw new XWikiException(XWikiException.MODULE_XWIKI_DOC, XWikiException.ERROR_XWIKI_UNKNOWN,
                         "Error accessing attachments when copying document "
                             + stringSerializer.serialize(targetDoc.getDocumentReference()) + " to document "
                             + stringSerializer.serialize(draftDoc.getDocumentReference()), e);
                 }
                 // make the draft doc draft again
                 makeDocumentDraft(draftDoc, workflow, xcontext);
                 // save the draft document
                 String defaultMessage =
                     "Created draft from published document" + stringSerializer.serialize(document) + ".";
                 String message =
                     getMessage("workflow.save.unpublish", defaultMessage,
                         Arrays.asList(stringSerializer.serialize(document).toString()));
                 xcontext.getWiki().saveDocument(draftDoc, message, true, xcontext);
             }
         } else {
             draftDocRef = this.createDraftDocument(document, xcontext);
         }
 
         if (draftDocRef != null) {
             // if draft creation worked fine, delete the published doc
             xcontext.getWiki().deleteDocument(targetDoc, xcontext);
             return draftDocRef;
         } else {
             // TODO: put exception on the context
             return null;
         }
     }
 
     @Override
     public boolean editDraft(DocumentReference document) throws XWikiException
     {
         XWikiContext xcontext = getXContext();
         XWikiDocument doc = xcontext.getWiki().getDocument(document, xcontext);
         BaseObject workflow = doc.getXObject(PUBLICATION_WORKFLOW_CLASS);
         String draftStatus = workflow.getStringValue(WF_STATUS_FIELDNAME);
         if (draftStatus.equals(STATUS_PUBLISHED)) {
             makeDocumentDraft(doc, workflow, xcontext);
             String defaultMessage = "Back to draft status to enable editing.";
             String message = getMessage("workflow.save.backToDraft", defaultMessage, null);
             xcontext.getWiki().saveDocument(doc, message, true, xcontext);
             return true;
         } else
             return false;
     }
 
     @Override
     public boolean archive(DocumentReference document) throws XWikiException
     {
         XWikiContext xcontext = getXContext();
         XWikiDocument publishedDoc = xcontext.getWiki().getDocument(document, xcontext);
 
         BaseObject publishedWorkflow =
             validateWorkflow(publishedDoc, Arrays.asList(STATUS_PUBLISHED), PUBLISHED, xcontext);
         if (publishedWorkflow == null) {
             return false;
         }
 
         // finally, preconditions are met, put the document on hidden (hoping that this is what archive actually means)
         // TODO: figure out what archive actually means
         publishedWorkflow.set(WF_STATUS_FIELDNAME, STATUS_ARCHIVED, xcontext);
         publishedDoc.setHidden(true);
 
         // save it
         String defaultMessage = "Archived document.";
         String message = getMessage("workflow.save.archive", defaultMessage, null);
         xcontext.getWiki().saveDocument(publishedDoc, message, true, xcontext);
 
         return true;
     }
 
     @Override
     public DocumentReference unarchive(DocumentReference document, boolean forceToDraft) throws XWikiException
     {
         return this.unpublish(document, forceToDraft);
     }
 
     @Override
     public boolean publishFromArchive(DocumentReference document) throws XWikiException
     {
         XWikiContext xcontext = getXContext();
         XWikiDocument archivedDoc = xcontext.getWiki().getDocument(document, xcontext);
 
         BaseObject archivedWorkflow =
             validateWorkflow(archivedDoc, Arrays.asList(STATUS_ARCHIVED), PUBLISHED, xcontext);
         if (archivedWorkflow == null) {
             return false;
         }
 
         // finally, preconditions are met, put the document on visible (hoping that this is what archive actually means)
         // TODO: figure out what archive actually means
         archivedWorkflow.set(WF_STATUS_FIELDNAME, STATUS_PUBLISHED, xcontext);
         archivedDoc.setHidden(false);
 
         // save it
         String defaultMessage = "Published document from an archive.";
         String message = messageTool.get("workflow.save.publishFromArchive", defaultMessage, null);
         xcontext.getWiki().saveDocument(archivedDoc, message, true, xcontext);
 
         return true;
     }
 
     /**
      * Function that marshalls the contents from ##fromDocument## to ##toDocument##, besides the workflow object, the
      * comment objects, the annotation objects, the rigths and the history. This function does not save the destination
      * document, the caller is responsible of that, so that they can perform additional operations on the destination
      * document before save.
      * 
      * @param fromDocument
      * @param toDocument
      * @return TODO
      * @throws XWikiException
      * @throws IOException
      */
     private boolean copyContentsToNewVersion(XWikiDocument fromDocument, XWikiDocument toDocument, XWikiContext xcontext)
         throws XWikiException, IOException
     {
         // use a fake 3 way merge: previous is toDocument without comments, rights and wf object
         // current version is current toDocument
         // next version is fromDocument without comments, rights and wf object
         XWikiDocument previousDoc = toDocument.clone();
         previousDoc.removeXObjects(explicitReferenceDocRefResolver.resolve(COMMENTS_CLASS,
             previousDoc.getDocumentReference()));
         previousDoc.removeXObjects(explicitReferenceDocRefResolver.resolve(RIGHTS_CLASS,
             previousDoc.getDocumentReference()));
         previousDoc.removeXObjects(explicitReferenceDocRefResolver.resolve(PUBLICATION_WORKFLOW_CLASS,
             previousDoc.getDocumentReference()));
         // set reference and language
 
         XWikiDocument nextDoc = fromDocument.clone();
         // I shouldn't do this, but for merge to work I need to have same doc ref and I don't know how to set it
         // otherwise
         nextDoc.setDocumentReference(toDocument.getDocumentReference());
         nextDoc.removeXObjects(explicitReferenceDocRefResolver.resolve(COMMENTS_CLASS, nextDoc.getDocumentReference()));
         nextDoc.setHidden(false);
         nextDoc.removeXObjects(explicitReferenceDocRefResolver.resolve(RIGHTS_CLASS, nextDoc.getDocumentReference()));
         nextDoc.removeXObjects(explicitReferenceDocRefResolver.resolve(PUBLICATION_WORKFLOW_CLASS,
             nextDoc.getDocumentReference()));
 
         // copy the attachments from the fromDocument to toDocument
         for (XWikiAttachment fromAttachment : fromDocument.getAttachmentList()) {
             // load the content of the fromAttachment
             fromAttachment.loadContent(xcontext);
             XWikiAttachment newAttachment = toDocument.getAttachment(fromAttachment.getFilename());
             if (newAttachment == null) {
                 newAttachment =
                     toDocument.addAttachment(fromAttachment.getFilename(),
                         fromAttachment.getContentInputStream(xcontext), xcontext);
                 newAttachment.setAuthor(fromAttachment.getAuthor());
                 // the date setting is not helping, it will be the date of the copy, but put it anyway
                 newAttachment.setDate(fromAttachment.getDate());
             } else {
                 // compare the contents of the attachment to know if we should update it or not
                 // TODO: figure out how could we do this without using so much memory
                 newAttachment.loadContent(xcontext);
                 boolean isSameAttachmentContent =
                     Arrays.equals(newAttachment.getAttachment_content().getContent(), fromAttachment
                         .getAttachment_content().getContent());
                 // unload the content of the newAttachment after comparison, since we don't need it anymore and we don't
                 // want to waste memory
                 newAttachment.setAttachment_content(null);
                 if (!isSameAttachmentContent) {
                     // update it, the contents are not equal
                     newAttachment.setContent(fromAttachment.getContentInputStream(xcontext));
                 }
             }
             // unload the attachment content of the from attachment so that we don't waste memory
             fromAttachment.setAttachment_content(null);
         }
 
         // and now merge. Normally the attachments which are not in the next doc are deleted from the current doc
         MergeResult result = toDocument.merge(previousDoc, nextDoc, new MergeConfiguration(), xcontext);
 
         // for some reason the creator doesn't seem to be copied if the toDocument is new, so let's put it
         if (toDocument.isNew()) {
             toDocument.setCreatorReference(fromDocument.getCreatorReference());
         }
 
         List<LogEvent> exception = result.getLog().getLogs(LogLevel.ERROR);
         if (exception.isEmpty()) {
             return true;
         } else {
             StringBuffer exceptions = new StringBuffer();
             for (LogEvent e : exception) {
                 if (exceptions.length() == 0) {
                     exceptions.append(";");
                 }
                 exceptions.append(e.getMessage());
             }
             throw new XWikiException(XWikiException.MODULE_XWIKI_DOC, XWikiException.ERROR_XWIKI_UNKNOWN,
                 "Could not copy document contents from "
                     + stringSerializer.serialize(fromDocument.getDocumentReference()) + " to document "
                     + stringSerializer.serialize(toDocument.getDocumentReference()) + ". Caused by: "
                     + exceptions.toString());
         }
     }
 
     /**
      * Turns a document in a draft document by setting the appropriate rights, hidden, settings in the workflow object.
      * 
      * @param doc
      * @param workflow
      * @param xcontext
      * @throws XWikiException
      */
     private void makeDocumentDraft(XWikiDocument doc, BaseObject workflow, XWikiContext xcontext) throws XWikiException
     {
         BaseObject workflowObj = workflow;
         if (workflowObj == null) {
             workflowObj = doc.getXObject(PUBLICATION_WORKFLOW_CLASS);
         }
 
         workflowObj.set(WF_STATUS_FIELDNAME, STATUS_DRAFT, xcontext);
         workflowObj.set(WF_IS_TARGET_FIELDNAME, 0, xcontext);
         doc.setHidden(true);
 
         BaseObject wfConfig =
             configManager.getWorkflowConfig(workflowObj.getStringValue(WF_CONFIG_REF_FIELDNAME), xcontext);
 
         if (wfConfig != null) {
             String contributors = publicationRoles.getContributors(wfConfig, xcontext);
             String moderators = publicationRoles.getModerators(wfConfig, xcontext);
             String validators = publicationRoles.getValidators(wfConfig, xcontext);
 
             // give the view and edit right to contributors, moderators and validators
             setRights(doc, Arrays.asList("edit", "view"), Arrays.asList(contributors, moderators, validators),
                 Arrays.<String> asList(), true, xcontext);
         }
     }
 
     private BaseObject validateWorkflow(XWikiDocument document, List<String> expectedStatuses,
         Integer expectedIsTarget, XWikiContext xcontext) throws XWikiException
     {
         if (!this.isWorkflowDocument(document, xcontext)) {
             // TODO: put error on the context
             return null;
         }
         BaseObject workflowObj = document.getXObject(PUBLICATION_WORKFLOW_CLASS);
         // check statuses
         if (!expectedStatuses.contains(workflowObj.getStringValue(WF_STATUS_FIELDNAME))) {
             // TODO: put error on the context
             return null;
         }
         // check is target (i.e. is published)
         int isTargetValue = workflowObj.getIntValue(WF_IS_TARGET_FIELDNAME, 0);
         if (!((expectedIsTarget > 0 && isTargetValue > 0) || (expectedIsTarget <= 0 && expectedIsTarget <= 0))) {
             // TODO: put error on the context
             return null;
         }
         return workflowObj;
     }
 
     private void setRights(XWikiDocument document, List<String> levels, List<String> groups, List<String> users,
         boolean allowdeny, XWikiContext context) throws XWikiException
     {
         // delete existing rights, if any
         this.removeRights(document, context);
 
         addRights(document, levels, groups, users, allowdeny, context);
     }
 
     private void addRights(XWikiDocument document, List<String> levels, List<String> groups, List<String> users,
         boolean allowdeny, XWikiContext context) throws XWikiException
     {
         // create a new object of type xwiki rights
         BaseObject rightsObject = document.newXObject(RIGHTS_CLASS, context);
         // put the rights and create
         rightsObject.set(RIGHTS_ALLOWDENY, allowdeny ? 1 : 0, context);
         // prepare the value for the groups property: it's a bit uneasy, we cannot pass a list to the BaseObject.set
         // and
         // to build the string we either need to know the separator, or we need to do this bad workaround to make
         // GroupsClass build the property value
         PropertyClass groupsPropClass = (PropertyClass) rightsObject.getXClass(context).get(RIGHTS_GROUPS);
         BaseProperty groupsProperty = groupsPropClass.fromStringArray((String[]) groups.toArray());
         rightsObject.set(RIGHTS_GROUPS, groupsProperty.getValue(), context);
         PropertyClass usersPropClass = (PropertyClass) rightsObject.getXClass(context).get(RIGHTS_USERS);
         BaseProperty usersProperty = usersPropClass.fromStringArray((String[]) users.toArray());
         rightsObject.set(RIGHTS_USERS, usersProperty.getValue(), context);
         PropertyClass levelsPropClass = (PropertyClass) rightsObject.getXClass(context).get(RIGHTS_LEVELS);
         BaseProperty levelsProperty = levelsPropClass.fromStringArray((String[]) levels.toArray());
         rightsObject.set(RIGHTS_LEVELS, levelsProperty.getValue(), context);
     }
 
     private void removeRights(XWikiDocument document, XWikiContext context) throws XWikiException
     {
         document.removeXObjects(explicitReferenceDocRefResolver.resolve(RIGHTS_CLASS, document.getDocumentReference()));
     }
 
     /**
      * @return the xwiki context from the execution context
      */
     private XWikiContext getXContext()
     {
         return (XWikiContext) execution.getContext().getProperty("xwikicontext");
     }
 
     /**
      * @param key Translation key
      * @param params Parameters to include in the translation
     * @param defaultMessage Message to display if the message tool
      * @return message to use
      */
     private String getMessage(String key, String defaultMessage, List<String> params)
     {
         if (this.messageTool == null) {
             this.messageTool = this.getXContext().getMessageTool();
         }
         String message = "";
         if (params != null) {
             message = messageTool.get(key, params);
         } else {
             message = messageTool.get(key);
         }
         if (message.equals(key)) {
             return defaultMessage;
         } else {
             return message;
         }
     }
 }
