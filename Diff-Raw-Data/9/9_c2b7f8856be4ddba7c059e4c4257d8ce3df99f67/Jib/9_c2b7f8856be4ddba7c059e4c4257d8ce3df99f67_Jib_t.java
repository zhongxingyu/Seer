 package jib.client;
 
 import gwtjava.io.fs.FileSystem;
 import gwtjava.lang.System;
 import javac.com.sun.tools.javac.Javac;
 import jvm.main.JVM;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.TextArea;
 
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  */
 public class Jib implements EntryPoint {
     FileSystem fs = FileSystem.instance();
 
     /**
      * This is the entry point method.
      */
     @Override
     public void onModuleLoad() {
         JVM.setClassLoader(new JibClassLoader());
 
         final TextArea log = new TextArea();
         RootPanel.get("log-div").add(log);
        log.addStyleName("logBox");
         log.setVisibleLines(10);
         log.setReadOnly(true);
         System.setOut(new TextAreaPrintStream(log));
         System.setErr(System.out);
 
         final Button runButton = new Button("Run!");
         RootPanel.get("btn-div").add(runButton);
         runButton.addStyleName("runButton");
 
         runButton.addClickHandler(new ClickHandler() {
             @Override
             public void onClick(ClickEvent event) {
                 log.setValue("");
                 String code = getSourceCode();
                 String className = Javac.getClassName(code);
 
                 boolean ok = Javac.compile(className + ".java", code);
                 if (ok) {
                     System.out.println("Compiled!");
                     printMagic(fs.readFile(fs.cwd() + className + ".class"));
                     JVM.run(fs.cwd() + className);
                 }
             }
         });
     }
 
     private void printMagic(byte [] bytecode) {
         System.out.print("Magic code: 0x");
         for (int i = 0; i < 4; i++) {
             System.out.print(Integer.toHexString(bytecode[i] & 0xFF));
         }
         System.out.println();
     }
 
     private native String getSourceCode() /*-{
         return $wnd.editor.getCode();
     }-*/;
 }
