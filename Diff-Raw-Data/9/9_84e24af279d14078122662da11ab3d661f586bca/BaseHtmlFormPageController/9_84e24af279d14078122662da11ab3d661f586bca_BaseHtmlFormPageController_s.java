 package org.openmrs.module.htmlformentryui.page.controller.htmlform;
 
 import org.apache.commons.lang.StringUtils;
 import org.openmrs.Patient;
 import org.openmrs.Visit;
 import org.openmrs.ui.framework.SimpleObject;
 import org.openmrs.ui.framework.UiUtils;
 
 /**
  * Current just used to define standard utility methods used by both BaseEnterHtmlFormPageController and EditHtmlFormWithStandardUIPageController
  */
 public abstract class BaseHtmlFormPageController {
 
 
     protected String determineReturnUrl(String returnUrl, String returnProviderName, String returnPageName, Patient patient, Visit visit, UiUtils ui) {
 
         SimpleObject returnParams = null;
 
         if (patient != null) {
             if (visit == null) {
                 returnParams = SimpleObject.create("patientId", patient.getId());
             }
             else {
                 returnParams = SimpleObject.create("patientId", patient.getId(), "visitId", visit.getId());
             }
         }
 
         // first see if a return provider and page have been specified
         if (StringUtils.isNotBlank(returnProviderName) && StringUtils.isNotBlank(returnPageName)) {
             return ui.pageLink(returnProviderName, returnPageName, returnParams);
         }
 
         // if not, see if a returnUrl has been specified
         if (StringUtils.isNotBlank(returnUrl)) {
            return "/" + ui.contextPath() + returnUrl;
         }
 
         // otherwise return to patient dashboard if we have a patient, but index if not
         if (returnParams != null && returnParams.containsKey("patientId")) {
             return ui.pageLink("coreapps", "patientdashboard/patientDashboard", returnParams);
         }
         else {
             return "/" + ui.contextPath() + "index.html";
         }
 
     }
 
 }
