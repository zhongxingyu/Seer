 package net.sf.laja.parser.cdd.creator;
 
 import org.junit.Test;
 
 import static junit.framework.Assert.assertEquals;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.core.IsEqual.equalTo;
 
 public class AparameterTest {
 
     @Test
     public void signatureArguments_spaceAndCommaSeparated() {
         Aparameter aparameter = new Aparameter();
        aparameter.signature = "String name , int x";
 
         assertEquals("name, x", aparameter.signatureArguments());
     }
 
     @Test
     public void signatureArguments_commaSeparated() {
         Aparameter aparameter = new Aparameter();
        aparameter.signature = "int name1, int name2";
 
         assertEquals("name1, name2", aparameter.signatureArguments());
     }
 
     @Test
     public void convertValue() {
         String convertedValue = new Aparameter().convertValue("givenName + \\\" \\\" + surname");
 
         assertThat(convertedValue, equalTo("givenName + \" \" + surname"));
     }
 }
