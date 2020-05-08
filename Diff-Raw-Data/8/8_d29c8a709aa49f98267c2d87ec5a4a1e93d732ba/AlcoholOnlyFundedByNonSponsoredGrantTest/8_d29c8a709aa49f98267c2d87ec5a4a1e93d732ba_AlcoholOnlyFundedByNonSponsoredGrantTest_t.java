 // AlcoholOnlyAllowedUnderNonSponsoredGrant.java
 package edu.umn.se.trap.rules.business;
 
 import java.util.Map;
 
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.ExpectedException;
 
 import edu.umn.se.test.frame.FormDataQuerier;
 import edu.umn.se.test.frame.TrapTestFramework;
 import edu.umn.se.trap.exception.BusinessLogicException;
 import edu.umn.se.trap.exception.TRAPException;
 import edu.umn.se.trap.form.InputFieldKeys;
 import edu.umn.se.trap.test.generate.TestDataGenerator.SampleDataEnum;
 
 /**
  * @author nagell2008
  * 
  */
 public class AlcoholOnlyFundedByNonSponsoredGrantTest extends TrapTestFramework
 {
 
     Map<String, String> goodAlcoholOther = null;
     Map<String, String> badAlcoholOther = null;
 
     Map<String, String> goodAlcoholIncidental = null;
     Map<String, String> badAlcoholIncidental = null;
 
     String incidentalJustField;
     String incidentalAmntField;
     String otherJustField;
     String otherAmntField;
 
     @Before
     public void setup() throws TRAPException
     {
         super.setup(SampleDataEnum.INTERNATIONAL1);
 
         incidentalJustField = FormDataQuerier.buildFieldStrForAnIncidental(testFormData,
                 InputFieldKeys.INCIDENTAL_JUSTIFICATION_FMT);
         incidentalAmntField = FormDataQuerier.buildFieldStrForAnIncidental(testFormData,
                 InputFieldKeys.INCIDENTAL_AMOUNT_FMT);
 
         otherJustField = String.format(InputFieldKeys.OTHER_JUSTIFICATION_FMT, 1);
         otherAmntField = String.format(InputFieldKeys.OTHER_AMOUNT_FMT, 1);
     }
 
     @Rule
     public ExpectedException exception = ExpectedException.none();
 
     @Test
     public void alcoholOtherEnoughFunding() throws TRAPException
     {
         testFormData.put(otherJustField, "A Whiskey on the rocks");
         testFormData.put(otherAmntField, "1.00");
 
         saveAndSubmitTestForm();
     }
 
     @Test
     public void alcoholOtherLowFunding() throws TRAPException
     {
         exception.expect(BusinessLogicException.class);
         exception.expectMessage("Alcohol expense of");
 
        testFormData.put(otherJustField, "A martini shaken and not stirred");
         testFormData.put(otherAmntField, "100000000.00");
 
         saveAndSubmitTestForm();
     }
 
     @Test
     public void alcoholIncidentalLowFunding() throws TRAPException
     {
         exception.expect(BusinessLogicException.class);
         exception.expectMessage("Alcohol expense of");
 
         testFormData.put(incidentalJustField,
                 "I needed a beer to not die after Ponder's horrible game");
         testFormData.put(incidentalAmntField, "100000000.00");
 
         saveAndSubmitTestForm();
     }
 
     @Test
     public void alcoholIncidentalEnoughFunding() throws TRAPException
     {
         testFormData.put(incidentalJustField, "A little bit of wine for the heart. Ya know..");
         testFormData.put(incidentalAmntField, "1.00");
 
         saveAndSubmitTestForm();
     }
 
 }
