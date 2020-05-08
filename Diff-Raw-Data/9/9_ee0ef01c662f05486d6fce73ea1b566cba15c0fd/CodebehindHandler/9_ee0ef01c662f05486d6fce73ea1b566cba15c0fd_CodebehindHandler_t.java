 package com.github.jorstache;
 
 import com.google.inject.Binder;
 import com.google.inject.Inject;
 import com.google.inject.Injector;
 import com.google.inject.Module;
 import com.sampullara.mustache.MustacheException;
 import jornado.ErrorResponse;
 import jornado.Request;
 import jornado.Response;
 
 import java.io.File;
 
 /**
 * The code behind handler combines code (possibly dynamic) and a template.
 * <p/>
 * User: sam
 * Date: May 17, 2010
 * Time: 4:55:24 PM
 */
 public class CodebehindHandler extends MustacheHandler {
   @Inject
   private Injector injector;
 
   public CodebehindHandler(File root, String templatePath, String classname) throws MustacheException {
     this(root, templatePath, classname.replace(".", "/") + ".java", classname);
   }
 
   public CodebehindHandler(File root, String templatePath, String codePath, String classname) throws MustacheException {
     super(root, templatePath, codePath, classname);
   }
 
  public CodebehindHandler(File root, String templatePath, Class<?> clazz) throws MustacheException {
     super(root, templatePath);
     this.clazz = clazz;
   }
 
   @Override
   public Response handle(final Request request) {
     try {
       Injector childInjector = injector.createChildInjector(new Module() {
         @Override
         public void configure(Binder binder) {
           binder.bind(Request.class).toInstance(request);
         }
       });
       Object o = childInjector.getInstance(getCode());
       return super.handle(o);
     } catch (Exception e) {
       e.printStackTrace();
       return new ErrorResponse(e.getMessage());
     }
   }
 }
