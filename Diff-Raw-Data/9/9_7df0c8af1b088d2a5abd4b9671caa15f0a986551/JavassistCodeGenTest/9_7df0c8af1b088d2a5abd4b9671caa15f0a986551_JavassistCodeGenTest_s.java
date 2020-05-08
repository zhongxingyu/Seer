 /**
  * Copyright m91snik
  * Created on: 9/21/13
  * Created by: m91snik
  */
 package com.m91snik.code_gen.test.generator;
 
 import com.m91snik.code_gen.test.generator.annotation.GenClassAnnotation;
 import com.m91snik.code_gen.test.generator.annotation.GenMethodAnnotation;
 import com.m91snik.code_gen.test.generator.protocol.TestRequest;
 import com.m91snik.code_gen.test.generator.protocol.TestRequest2;
 import com.m91snik.code_gen.test.generator.protocol.TestResponse;
 import com.m91snik.code_gen.test.generator.proxy.TestCgLibProxy;
 import com.m91snik.code_gen.test.generator.proxy.TestJavassistProxy;
 import com.m91snik.code_gen.test.generator.test_target.TestTarget;
 import com.m91snik.code_gen.test.generator.utils.TestUtils;
 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import java.lang.reflect.Method;
 import java.net.URLEncoder;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath:spring/services/generator/javassist-test-root.xml"})
 public class JavassistCodeGenTest {
 
     @Autowired
     private TestTarget testTarget;
     @Autowired
     private TestJavassistProxy testProxy;
 
     @Test
     public void testWebServiceFacadeGenerator() throws Exception {
        String encode = URLEncoder.encode("What is BlazeMeter's Load Testing Cloud?", "UTF-8");
        System.out.println(encode);
         assertMethodWorking();
         assertProxyClassName();
         assertProxyAnnotations();
         assertProxyMethodsAnnotations();
     }
 
     private void assertProxyClassName() {
         String expectedClassName = "com.m91snik.code_gen.test.generator.proxy.TestJavassistProxyImpl";
         Assert.assertEquals(expectedClassName, testProxy.getClass().getName());
     }
 
     private void assertMethodWorking() throws TestException {
         TestRequest testRequest = new TestRequest(1);
         TestRequest2 testRequest2 = new TestRequest2(2);
         TestResponse testResponse = testProxy.doTestRequest(testRequest, testRequest2);
 
         Assert.assertEquals(5, testResponse.anInt);
     }
 
     private void assertProxyAnnotations() {
         GenClassAnnotation localFacadeBusinessServiceAnnotation =
                 testTarget.getClass().getAnnotation(GenClassAnnotation.class);
         GenClassAnnotation facadeBusinessServiceAnnotation =
                 testProxy.getClass().getAnnotation(GenClassAnnotation.class);
         Assert.assertEquals(localFacadeBusinessServiceAnnotation, facadeBusinessServiceAnnotation);
     }
 
     private void assertProxyMethodsAnnotations() {
         Method[] interfaceMethods = TestCgLibProxy.class.getDeclaredMethods();
         Method[] targetInterfaceMethods = TestTarget.class.getDeclaredMethods();
 
         for (Method method : testProxy.getClass().getDeclaredMethods()) {
             if (!TestUtils.isOverriddenMethod(method, interfaceMethods)) {
                 continue;
             }
             GenMethodAnnotation annotation = method.getAnnotation(GenMethodAnnotation.class);
             Assert.assertNotNull(annotation);
             Method targetMethod = TestUtils.getOverridenMethodFromClazz(method, testTarget.getClass(),
                                                                         targetInterfaceMethods);
             GenMethodAnnotation targetAnnotation = targetMethod.getAnnotation(GenMethodAnnotation.class);
             Assert.assertEquals(targetAnnotation.genEnum(), annotation.genEnum());
         }
     }
 
 }
