 package org.apache.myfaces.otherEngines;
 
 import javax.faces.FacesException;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.RequestScoped;
 import javax.faces.bean.SessionScoped;
 import javax.script.ScriptException;
 import java.io.File;
 
 @ManagedBean(name = "scriptTestBean")
 @RequestScoped
 public class TestBeanImpl implements TestBean {
 
     TestBean _delegate = null;
 
     public TestBeanImpl() {
         try {
             String resourceRoot = this.getClass().getClassLoader().getResource("./").getFile();
             //dirty and only works if you run it in maven jetty:run but for the demo this is ok
            String separator = File.separator;
            if(separator.equals("\\")) {
                separator = separator+separator;
            }
            String resource = resourceRoot+"../../../../src/main/java/"+"org.apache.myfaces.otherEngines".replaceAll("\\.", separator)+"TestBean.js";
             _delegate = (TestBean) JavascriptProxyFactory.newInstance(TestBean.class, "TestBean", new File(resource));
         } catch (ScriptException e) {
             throw new FacesException(e);
         }
     }
 
     public String getHello() {
         return _delegate.getHello();
     }
 
     public void sayHello(String hello) {
         _delegate.sayHello(hello);
     }
 }
