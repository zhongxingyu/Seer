 package org.eclipse.recommenders.snipeditor.tests;
 
 import org.eclipse.recommenders.snipeditor.SnipDSLInjectorProvider;
 import org.eclipse.xtext.junit4.InjectWith;
 import org.eclipselabs.xtext.utils.unittesting.XtextRunner2;
 import org.eclipselabs.xtext.utils.unittesting.XtextTest;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 
 
 @RunWith(XtextRunner2.class)
 @InjectWith(SnipDSLInjectorProvider.class)
 public class SnipDSLTest extends XtextTest {
 
 	 public SnipDSLTest() {
         super("SnipDSLTest");
 	 }
  
 	 @Test
 	 public void randomTests(){
 	
 		 suppressSerialization();
          testFile("Tests.cSnip");
 	 }
 	 @Test
 	 public void conversionTests(){
 	
 		 suppressSerialization();
        // testFile("conversions.cSnip");
 	 }
 	 @Test
 	 public void arraysTests(){
 	
 		 suppressSerialization();
         //testFile("arrays.cSnip");
 	 }
 }
