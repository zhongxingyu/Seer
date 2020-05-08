 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.socraticgrid.documenttransformer;
 
 import java.io.InputStream;
 import java.util.HashMap;
 import junit.framework.TestCase;
 import org.junit.runner.RunWith;
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.core.io.ClassPathResource;
 import org.springframework.core.io.Resource;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 /**
  *
  * @author Jerry Goodnough
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 // ApplicationContext will be loaded from "/applicationContext.xml" and "/applicationContext-test.xml"
 // in the root of the classpath
 @ContextConfiguration(locations={"classpath:Test-BaseDocumentTransformer.xml"})
 public class TransformerTest extends TestCase
 {
    @Autowired 
    private Transformer instance;
 
     public TransformerTest()
     {
         super();
     }
     /**
      * Test of setTransformPipeline method, of class Transformer.
      */
     
     public void testSetTransformPipeline()
     {
         System.out.println("setTransformPipeline");
         HashMap<String, TransformPipeline> transformPipeline = null;
         instance.setTransformPipeline(transformPipeline);
      
     }
 
     /**
      * Test of transform method, of class Transformer.
      */
     @Test
    public void testStaticTransform() throws Exception
     {
         System.setProperty("jaxp.debug","1");
         System.out.println("transform");
         String pipeline = "Medications";
         String pipeline2 = "Medications_Base";
         Resource res = new ClassPathResource("PatientDataRequest_meds_10013.xml");
         InputStream inStr = res.getInputStream();
     
         String result = instance.transform(pipeline, inStr);
         inStr = res.getInputStream();
         String result2 = instance.transform(pipeline2, inStr);
         System.out.println("--- FHIR to ---");
         System.out.println(result2);
         System.out.println("--- Transforms to ---");
         System.out.println(result);
         //assertEquals(result,result2);
     }
     
        @Test
    public void testMultipathTransform() throws Exception
     {
         System.setProperty("jaxp.debug","1");
         System.out.println("transform");
         String pipeline = "Allergies_FHIR";
         String pipeline2 = "Allergies_JSON";
         Resource res = new ClassPathResource("PatientDataRequest_meds_10013.xml");
         InputStream inStr = res.getInputStream();
     
         String result = instance.transform(pipeline, inStr);
         inStr = res.getInputStream();
         String result2 = instance.transform(pipeline2, inStr);
         System.out.println("--- FHIR to ---");
         System.out.println(result);
         System.out.println("--- Transforms to ---");
         System.out.println(result2);
         //assertEquals(result,result2);
     }
 }
