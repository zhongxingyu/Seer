 package org.hackystat.sensor.xmldata.option;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.hackystat.sensor.xmldata.XmlDataController;
 import org.junit.Assert;
 import org.junit.Test;
 
 /**
  * Tests if the arglist option operates as intended.
  * @author aito
  * 
  */
 public class TestArgListOption {
   /**
    * Tests if isValid returns the correct value depending on the specified
    * parameters.
    */
   @Test
   public void testIsValid() {
     XmlDataController controller = new XmlDataController();
     // Tests a valid argList parameter count, but invalid file.
     List<String> parameters = new ArrayList<String>();
     String testPackage = "src/org/hackystat/sensor/xmldata/testdataset/";
    parameters.add(new File("") + testPackage + "testArgList.txt");
     Option option = OptionFactory.getInstance(controller, ArgListOption.OPTION_NAME,
         parameters);
     Assert.assertTrue("ArgList accept only 1 argument.", option.isValid());
     option.process();
 
     // Tests passing invalid amount of parameters.
     parameters = new ArrayList<String>();
     option = ArgListOption.createOption(controller, parameters);
     Assert.assertFalse("ArgList accept only 1 argument.", option.isValid());
 
     // Tests passing an invalid file.
     parameters = new ArrayList<String>();
     parameters.add("Foo.xml");
     option = ArgListOption.createOption(controller, parameters);
     Assert.assertFalse("An invalid file should invalid this option.", option.isValid());
   }
 }
