 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.krohm.tagcontainer.servlet.account;
 
 import java.util.ArrayList;
 import java.util.List;
 import javax.servlet.http.HttpServletRequest;
 import org.krohm.tagcontainer.entities.ScriptEntity;
 
 /**
  *
  * @author Arnaud
  */
 public class TestGetAccountJavascriptServlet extends AbstractGetAccountJavascriptServlet {
 
     private static ScriptEntity testScript;
 
     public ScriptEntity getTestScript() {
         return testScript;
     }
 
     public void setTestScript(ScriptEntity testScript) {
         TestGetAccountJavascriptServlet.testScript = testScript;
     }
 
     @Override
     protected List<String> getUrlList(HttpServletRequest request) {
         List<String> returnList = new ArrayList<String>();
         returnList.add("http://code.jquery.com/jquery.min.js");
        returnList.add("./GetJavascript/1528795644/script.js");
        returnList.add("./GetJavascript/9876543210/script.js?thisis=aparam&thatis=anotherone");
         returnList.add("http://dummy.not.existing/script.js");
         return returnList;
 
     }
 }
