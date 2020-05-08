 package controllers;
 
 import play.*;
 import play.mvc.*;
 import play.Logger;
 
 import java.util.*;
 
 import staticUITest.ITestStaticUIDataProvider;
 
 public class TestStaticUIController extends Controller {
 
     static String toCamelCase(String s){
         String[] parts = s.split("_");
         String camelCaseString = "";
 
         for (String part : parts)
             camelCaseString = camelCaseString + toProperCase(part);
 
         return camelCaseString;
     }
 
     static String toProperCase(String s) {
         return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
     }
 
     public static void test(String controller, String action) {
         ITestStaticUIDataProvider dataProvider = null;
 
         try {
            String classToLoad = "staticUITest.staticUIData." + toCamelCase(controller) + toCamelCase(action) + "TestStaticUIDataProviderImp";
             Class<?> clazz = Application.class.getClassLoader().loadClass(classToLoad);
             dataProvider = (ITestStaticUIDataProvider) clazz.newInstance();
         } catch (ClassNotFoundException e) {
             e.printStackTrace();
         } catch (InstantiationException e) {
             e.printStackTrace();
         } catch (IllegalAccessException e) {
             e.printStackTrace();
         }
 
         dataProvider.addArgs(renderArgs.current());
         dataProvider.addSession(session.current());
         dataProvider.addFlash(flash.current());
         dataProvider.addValidationErrors(validation.current());
 
         renderTemplate("@" + controller + "." + action);
     }
 
 }
