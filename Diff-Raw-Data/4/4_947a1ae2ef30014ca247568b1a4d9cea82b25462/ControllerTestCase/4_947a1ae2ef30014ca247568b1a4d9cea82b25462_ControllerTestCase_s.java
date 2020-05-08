 package edu.northwestern.bioinformatics.studycalendar.web;
 
 import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
 import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.FieldError;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author Rhett Sutphin
  */
 public abstract class ControllerTestCase extends WebTestCase {
     protected static void assertNoBindingErrorsFor(String fieldName, Map<String, Object> model) {
         BindingResult result = (BindingResult) model.get(BindingResult.MODEL_KEY_PREFIX + "command");
         List<FieldError> fieldErrors = result.getFieldErrors(fieldName);
         assertEquals("There were errors for field " + fieldName + ": " + fieldErrors, 0, fieldErrors.size());
     }
 
     protected void assertRolesAllowed(Collection<ResourceAuthorization> actual, PscRole... expected) {
         Collection<PscRole> actualRoles = new ArrayList<PscRole>();
         if (actual == null) {
             actualRoles = Arrays.asList(PscRole.values());
         } else {
             for (ResourceAuthorization actualResourceAuthorization : actual) {
                 actualRoles.add(actualResourceAuthorization.getRole());
             }
         }
 
         for (PscRole role : expected) {
             assertTrue(role.getDisplayName() + " should be allowed",
                 actualRoles.contains(role));
         }
     }
 }
