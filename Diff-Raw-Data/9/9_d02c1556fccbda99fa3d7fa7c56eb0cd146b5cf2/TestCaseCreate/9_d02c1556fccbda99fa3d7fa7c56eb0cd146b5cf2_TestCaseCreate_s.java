 /*
  * Copyright (C) 2012 Red Hat, Inc.     
  * 
  * This copyrighted material is made available to anyone wishing to use, 
  * modify, copy, or redistribute it subject to the terms and conditions of the 
  * GNU General Public License v.2.
  * 
  * Authors: Adam Saleh (asaleh at redhat dot com)
  *          Jan Rusnacko (jrusnack at redhat dot com)
  */
 package NitrateIntegration.CommandWrapper;
 
 import NitrateIntegration.TcmsEnvironment;
 import NitrateIntegration.TcmsProperties;
 import com.redhat.nitrate.TcmsCommand;
 import com.redhat.nitrate.TcmsConnection;
 import com.redhat.nitrate.command.TestCase;
 import java.util.Hashtable;
 
 /**
  *
  * @author asaleh
  */
 public class TestCaseCreate extends CommandWrapper {
 
      static{
         CommandWrapper.enlistWrapper(TestCase.create.class, new WrapperConstructor() {
 
             public CommandWrapper create(TcmsCommand current, Class result_type,TcmsProperties properties,TcmsEnvironment env) {
                 return new TestCaseCreate(current, result_type, properties, env);
             }
         });
     }
 
     public TestCaseCreate(TcmsCommand current, Class result_type,TcmsProperties properties,TcmsEnvironment env) {
         super(current, result_type, properties, env);
     }
 
     @Override
     public Object getResultIfDuplicate(TcmsConnection connection) {
         TestCase.filter f = new TestCase.filter();
         TestCase.create comand = (TestCase.create) current;
         f.summary = comand.summary;
         f.category = comand.category;
         f.priority = comand.priority;
         f.plan = comand.plan;
         if (comand.arguments != null && comand.arguments.length() > 0) {
             f.arguments = comand.arguments;
         }
 
         CommandWrapper script = new Generic(f, TestCase.class, properties, env);
         script.perform(connection);
         TestCase found = script.getResult(TestCase.class);
 
         return found;
     }
 
     @Override
     public boolean processDependecies() {
         return true;
     }
 
     public Hashtable<String, String> description() {
         Hashtable<String, String> map = current.descriptionMap();
         map.put("priority",
                 properties.priority + " (" + map.get("priority") + ")");
         map.put("category", properties.category + " (" + map.get("category")
                 + ")");
         map.put("product", properties.product + " (" + map.get("product")
                 + ")");
         map.put("plan", properties.plan + " (" + map.get("plan") + ")");
 
         return map;
     }
 
     public String summary() {
         return description().get("summary");
     }
 
     public String toString() {
        return "Create Test Case";
     }
 }
