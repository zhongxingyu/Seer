 package org.antbear.tododont.web.controller.registration;
 
 import org.antbear.tododont.web.beans.UserRegistration;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import javax.validation.ConstraintViolation;
 import javax.validation.Validation;
 import javax.validation.Validator;
 import javax.validation.ValidatorFactory;
 import java.util.Set;
 
 import static org.junit.Assert.assertEquals;
 
 public class UserRegistrationTest {
 
     private static Validator validator;
 
     @BeforeClass
     public static void setup() {
         final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
         validator = factory.getValidator();
     }
 
     @Test
     public void testInvalidPassword() throws Exception {
         final UserRegistration user = new UserRegistration("foo@bar.tld", "invalid");
         final Set<ConstraintViolation<UserRegistration>> cs = validator.validate(user);
        assertEquals(1, cs.size());
     }
 
     @Test
     public void testValidPassword() throws Exception {
         final UserRegistration user = new UserRegistration("foo@bar.tld", "abc123_XZY");
         final Set<ConstraintViolation<UserRegistration>> cs = validator.validate(user);
         assertEquals(0, cs.size());
     }
 }
