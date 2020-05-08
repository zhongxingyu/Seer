 package io.dahuapp.editor.drivers;
 
 import javafx.scene.web.WebEngine;
 import netscape.javascript.JSObject;
 
 /**
  *
  * @author barraq
  */
 public class DummyDriver implements Driver {
     
     private WebEngine we;
     private String method;
     
     public DummyDriver(WebEngine we) {
         this.we = we;
     }
     
     @Override
     public void onLoad() {
         System.out.println("".getClass().getName()+" loaded");
     }
     
     @Override
     public void onStop() {
     }
     
     public void printHello() {
         System.out.println("Hello, I'm dummy");
     }
     
     public void print(String s) {
         System.out.println(s);
     }
     
     public void recordMethodWithString(String method) {
         this.method = method;
     }
     
     public void callMethodWithString(Object... args) {
         JSObject window = (JSObject)we.executeScript("window");
         window.call(method, args);
     }
 }
