 package de.matrixweb.ne;
 
 import com.googlecode.javacpp.Loader;
 import com.googlecode.javacpp.Pointer;
 import com.googlecode.javacpp.annotation.Name;
 import com.googlecode.javacpp.annotation.Namespace;
 import com.googlecode.javacpp.annotation.Platform;
 import com.googlecode.javacpp.annotation.StdString;
 
 /**
  * @author marwol
  */
@Platform(include = "jv8.h", includepath = { "src/main/cpp", "target/v8/include" }, link = { "jv8" }, linkpath = "target/jv8")
 public class NativeEngine {
 
   private final NativeEngineImpl impl;
 
   /**
    * 
    */
   public NativeEngine() {
     this.impl = new NativeEngineImpl();
   }
 
   /**
    * @param script
    */
   public void addScript(final String script) {
     this.impl.addScript(script);
   }
 
   /**
    * @param name
    */
   public void prepareRun(final String name) {
     this.impl.prepareRun(name);
   }
 
   /**
    * @param input
    * @return
    */
   public String execute(final String input) {
     return this.impl.execute(input);
   }
 
  @Namespace("jv8")
   @Name("NativeEngine")
   private static class NativeEngineImpl extends Pointer {
 
     static {
       Loader.load();
     }
 
     public NativeEngineImpl() {
       allocate();
     }
 
     private native void allocate();
 
     public native void addScript(@StdString String script);
 
     public native void prepareRun(@StdString String name);
 
     @StdString
     public native String execute(@StdString String input);
 
   }
 
 }
