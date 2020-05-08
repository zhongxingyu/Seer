 package org.triple_brain.module.model;
 
 import org.codehaus.jettison.json.JSONObject;
 import org.junit.Test;
 
 import java.util.Map;
 
 import static junit.framework.Assert.assertTrue;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.core.Is.is;
 import static org.hamcrest.core.IsNull.notNullValue;
 import static org.triple_brain.module.model.json.UserJsonFields.*;
 import static org.triple_brain.module.model.validator.UserValidator.*;
 
 /**
  * Copyright Mozilla Public License 1.1
  */
 public class UserValidatorTest {
 
     @Test
     public void email_is_mandatory() throws Exception{
         assertTrue(
                 validationWithUserReturnsFieldWithMessage(
                         validUserButWithFieldValue(EMAIL, ""),
                         EMAIL,
                         MANDATORY_EMAIL)
         );
     }
 
     @Test
     public void email_has_to_be_valid() throws Exception{
         assertTrue(
                 validationWithUserReturnsFieldWithMessage(
                         validUserButWithFieldValue(
                                 EMAIL,
                                 "roger.lamothe$example.org"
                         ),
                         EMAIL,
                         INVALID_EMAIL)
         );
     }
 
     @Test
     public void password_is_mandatory() throws Exception{
         assertTrue(
                 validationWithUserReturnsFieldWithMessage(
                         validUserButWithFieldValue(
                                 PASSWORD,
                                 ""
                         ),
                         PASSWORD,
                         MANDATORY_PASSWORD)
         );
     }
 
     @Test
     public void password_cant_be_too_short() throws Exception{
         assertTrue(
                 validationWithUserReturnsFieldWithMessage(
                         validUser()
                                 .put(PASSWORD, "pass")
                                 .put(PASSWORD_VERIFICATION, "pass"),
                         PASSWORD,
                         PASSWORD_TOO_SHORT)
         );
     }
 
     @Test
     public void password_verification_has_to_be_the_same_as_password() throws Exception{
         assertTrue(
                 validationWithUserReturnsFieldWithMessage(
                         validUser()
                                 .put(PASSWORD, "password")
                                 .put(PASSWORD_VERIFICATION, "another_password"),
                         PASSWORD,
                         PASSWORD_VERIFICATION_ERROR)
         );
     }
 
     @Test
     public void username_is_mandatory() throws Exception{
         assertTrue(
                 validationWithUserReturnsFieldWithMessage(
                         validUserButWithFieldValue(USER_NAME, ""),
                         USER_NAME,
                         MANDATORY_USER_NAME)
         );
     }
 
     @Test
     public void username_has_to_be_valid() throws Exception{
         assertTrue(
                 validationWithUserReturnsFieldWithMessage(
                         validUserButWithFieldValue(
                                 USER_NAME,
                                 "roger/lamothe"
                         ),
                         USER_NAME,
                         INVALID_USER_NAME)
         );
     }
 
     @Test
     public void username_cant_be_too_long() throws Exception{
         assertTrue(
                 validationWithUserReturnsFieldWithMessage(
                         validUserButWithFieldValue(
                                 USER_NAME,
                                 "roger_lamotheeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee"
                         ),
                         USER_NAME,
                         USER_NAME_TOO_LONG)
         );
     }
 
 
 
     private boolean validationWithUserReturnsFieldWithMessage(JSONObject user, String field, String message){
         Map<String, String> errors ;
         errors = validate(user);
         assertThat(errors.get(field), is(notNullValue()));
         assertThat(errors.get(field), is(message));
         return true;
     }
 
     private JSONObject validUserButWithFieldValue(String fieldName, String value) throws Exception{
         return validUser().put(fieldName, value);
     }
     
     private JSONObject validUser()throws Exception{
         JSONObject user = new JSONObject();
         user.put(USER_NAME, "generated_user_name");
         user.put(EMAIL, "generated_email@example.org");
         user.put(PASSWORD, "generated password");
         user.put(PASSWORD_VERIFICATION, "generated password");
         return user;
     }
 }
