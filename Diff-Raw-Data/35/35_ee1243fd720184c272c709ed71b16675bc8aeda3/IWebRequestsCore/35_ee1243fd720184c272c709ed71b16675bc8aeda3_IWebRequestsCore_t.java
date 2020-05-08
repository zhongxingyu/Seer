 package hr.brbulic.services.web.interfaces;
 
 import java.util.HashMap;
 
 /**
  * Created by IntelliJ IDEA.
  * User: brbulic
  * Date: 9/29/11
  * Time: 12:24 AM
  * To change this template use File | Settings | File Templates.
  */
 public interface IWebRequestsCore {
 
    IWebResultEventArgs beginRequestGet(String url, HashMap<String,String> params,  Object userData);
 
     IWebResultEventArgs beginRequestPost(String url, HashMap<String,String> params, Object userData);
 }
