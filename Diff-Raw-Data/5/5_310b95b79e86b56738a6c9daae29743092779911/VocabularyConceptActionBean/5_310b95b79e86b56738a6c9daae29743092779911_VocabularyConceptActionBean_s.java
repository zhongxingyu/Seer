 /*
  * The contents of this file are subject to the Mozilla Public
  * License Version 1.1 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of
  * the License at http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS
  * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
  * implied. See the License for the specific language governing
  * rights and limitations under the License.
  *
  * The Original Code is Content Registry 3
  *
  * The Initial Owner of the Original Code is European Environment
  * Agency. Portions created by TripleDev or Zero Technologies are Copyright
  * (C) European Environment Agency.  All Rights Reserved.
  *
  * Contributor(s):
  *        Juhan Voolaid
  */
 
 package eionet.web.action;
 
 import java.util.List;
 
 import net.sourceforge.stripes.action.DefaultHandler;
 import net.sourceforge.stripes.action.ForwardResolution;
 import net.sourceforge.stripes.action.RedirectResolution;
 import net.sourceforge.stripes.action.Resolution;
 import net.sourceforge.stripes.action.UrlBinding;
 import net.sourceforge.stripes.integration.spring.SpringBean;
 import net.sourceforge.stripes.validation.ValidationMethod;
 
 import org.apache.commons.lang.StringUtils;
 
 import eionet.meta.dao.domain.VocabularyConcept;
 import eionet.meta.dao.domain.VocabularyConceptAttribute;
 import eionet.meta.dao.domain.VocabularyFolder;
 import eionet.meta.service.IVocabularyService;
 import eionet.meta.service.ServiceException;
 import eionet.util.Props;
 import eionet.util.PropsIF;
 import eionet.util.Util;
 
 /**
  * Vocabulary concept action bean.
  *
  * @author Juhan Voolaid
  */
 @UrlBinding("/vocabularyconcept/{vocabularyFolder.identifier}/{vocabularyConcept.identifier}/{$event}")
 public class VocabularyConceptActionBean extends AbstractActionBean {
 
     /** JSP pages. */
     private static final String VIEW_VOCABULARY_CONCEPT_JSP = "/pages/vocabularies/viewVocabularyConcept.jsp";
     private static final String EDIT_VOCABULARY_CONCEPT_JSP = "/pages/vocabularies/editVocabularyConcept.jsp";
 
     /** Vocabulary service. */
     @SpringBean
     private IVocabularyService vocabularyService;
 
     /** Vocabulary folder. */
     private VocabularyFolder vocabularyFolder;
 
     /** Vocabulary concept to add/edit. */
     private VocabularyConcept vocabularyConcept;
 
     /**
      * View action.
      *
      * @return
      * @throws ServiceException
      */
     @DefaultHandler
     public Resolution view() throws ServiceException {
         vocabularyFolder =
                 vocabularyService.getVocabularyFolder(vocabularyFolder.getIdentifier(), vocabularyFolder.isWorkingCopy());
         vocabularyConcept = vocabularyService.getVocabularyConcept(vocabularyFolder.getId(), vocabularyConcept.getIdentifier());
         validateView();
         return new ForwardResolution(VIEW_VOCABULARY_CONCEPT_JSP);
     }
 
     /**
      * Display edit form action.
      *
      * @return
      * @throws ServiceException
      */
     public Resolution edit() throws ServiceException {
         vocabularyFolder =
                 vocabularyService.getVocabularyFolder(vocabularyFolder.getIdentifier(), vocabularyFolder.isWorkingCopy());
         vocabularyConcept = vocabularyService.getVocabularyConcept(vocabularyFolder.getId(), vocabularyConcept.getIdentifier());
         validateView();
         return new ForwardResolution(EDIT_VOCABULARY_CONCEPT_JSP);
     }
 
     /**
      * Action for saving concept.
      *
      * @return
      * @throws ServiceException
      */
     public Resolution saveConcept() throws ServiceException {
         vocabularyService.updateVocabularyConcept(vocabularyConcept);
 
         addSystemMessage("Vocabulary concept saved successfully");
 
         RedirectResolution resolution = new RedirectResolution(getClass(), "edit");
         resolution.addParameter("vocabularyFolder.identifier", vocabularyFolder.getIdentifier());
         resolution.addParameter("vocabularyFolder.workingCopy", vocabularyFolder.isWorkingCopy());
         resolution.addParameter("vocabularyConcept.identifier", vocabularyConcept.getIdentifier());
         return resolution;
     }
 
     /**
      * Validates save concept.
      *
      * @throws ServiceException
      */
     @ValidationMethod(on = {"saveConcept"})
     public void validateSaveConcept() throws ServiceException {
         if (!isUpdateRight()) {
             addGlobalValidationError("No permission to modify vocabulary");
         }
 
         if (StringUtils.isEmpty(vocabularyConcept.getIdentifier())) {
             addGlobalValidationError("Vocabulary concept identifier is missing");
         } else {
             if (vocabularyFolder.isNumericConceptIdentifiers()) {
                 if (!Util.isNumericID(vocabularyConcept.getIdentifier())) {
                     addGlobalValidationError("Vocabulary concept identifier must be numeric value");
                 }
             } else {
                 if (!Util.isValidIdentifier(vocabularyConcept.getIdentifier())) {
                     addGlobalValidationError("Vocabulary concept identifier must be alpha-numeric value");
                 }
                 if (VocabularyFolderActionBean.RESERVED_VOCABULARY_EVENTS.contains(vocabularyConcept.getIdentifier())) {
                     addGlobalValidationError("This vocabulary concept identifier is reserved value and cannot be used");
                 }
             }
         }
         if (StringUtils.isEmpty(vocabularyConcept.getLabel())) {
             addGlobalValidationError("Vocabulary concept label is missing");
         }
 
         // Validate unique identifier
         if (!vocabularyService.isUniqueConceptIdentifier(vocabularyConcept.getIdentifier(), vocabularyFolder.getId(), vocabularyConcept.getId())) {
             addGlobalValidationError("Vocabulary concept identifier is not unique");
         }
 
         for (List<VocabularyConceptAttribute> attributes : vocabularyConcept.getAttributes()) {
             if (attributes != null) {
                 for (VocabularyConceptAttribute attr : attributes) {
                     if (attr != null) {
                         if (StringUtils.isEmpty(attr.getValue())) {
                             addGlobalValidationError("An attribute value was missing");
                             break;
                         }
                     }
                 }
             }
         }
 
         if (isValidationErrors()) {
             vocabularyConcept = vocabularyService.getVocabularyConcept(vocabularyFolder.getId(), vocabularyConcept.getIdentifier());
         }
     }
 
     /**
      * Validates view action.
      *
      * @throws ServiceException
      */
     private void validateView() throws ServiceException {
         if (vocabularyFolder.isWorkingCopy() || vocabularyFolder.isDraftStatus()) {
             if (getUser() == null) {
                 throw new ServiceException("User must be logged in");
             } else {
                 if (vocabularyFolder.isWorkingCopy() && !isUserWorkingCopy()) {
                     throw new ServiceException("Illegal user for viewing this working copy");
                 }
             }
         }
 
     }
 
     /**
      * True, if logged in user is the working user of the vocabulary.
      *
      * @return
      */
     private boolean isUserWorkingCopy() {
         boolean result = false;
         String sessionUser = getUserName();
         if (!StringUtils.isBlank(sessionUser)) {
             if (vocabularyFolder != null) {
                 String workingUser = vocabularyFolder.getWorkingUser();
                 return vocabularyFolder.isWorkingCopy() && StringUtils.equals(workingUser, sessionUser);
             }
         }
 
         return result;
     }
 
     /**
      * True, if user has update right.
      *
      * @return
      */
     private boolean isUpdateRight() {
         if (getUser() != null) {
             return getUser().hasPermission("/vocabularies", "u");
         }
         return false;
     }
 
     /**
      * Returns concept URI.
      *
      * @return
      */
     public String getConceptUri() {
         String baseUri = vocabularyFolder.getBaseUri();
         if (StringUtils.isEmpty(baseUri)) {
             baseUri = Props.getRequiredProperty(PropsIF.DD_URL) + "/vocabulary/" + vocabularyFolder.getIdentifier();
         }
         if (!baseUri.endsWith("/")) {
             baseUri += "/";
         }
        return baseUri + vocabularyConcept.getIdentifier();
     }
 
     /**
      * @return the vocabularyFolder
      */
     public VocabularyFolder getVocabularyFolder() {
         return vocabularyFolder;
     }
 
     /**
      * @param vocabularyFolder
      *            the vocabularyFolder to set
      */
     public void setVocabularyFolder(VocabularyFolder vocabularyFolder) {
         this.vocabularyFolder = vocabularyFolder;
     }
 
     /**
      * @return the vocabularyConcept
      */
     public VocabularyConcept getVocabularyConcept() {
         return vocabularyConcept;
     }
 
     /**
      * @param vocabularyConcept
      *            the vocabularyConcept to set
      */
     public void setVocabularyConcept(VocabularyConcept vocabularyConcept) {
         this.vocabularyConcept = vocabularyConcept;
     }
 
 }
