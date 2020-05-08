 package net.contextfw.web.commons.cloud.session;
 
 import static java.lang.annotation.ElementType.METHOD;
 import static java.lang.annotation.RetentionPolicy.RUNTIME;
 
 import java.lang.annotation.Retention;
 import java.lang.annotation.Target;
 
 import com.google.inject.BindingAnnotation;
 
 @Target( { METHOD })
 @Retention(RUNTIME)
 @BindingAnnotation 
 public @interface CloudSessionOpenMode {
    
    OpenMode value();
 }
