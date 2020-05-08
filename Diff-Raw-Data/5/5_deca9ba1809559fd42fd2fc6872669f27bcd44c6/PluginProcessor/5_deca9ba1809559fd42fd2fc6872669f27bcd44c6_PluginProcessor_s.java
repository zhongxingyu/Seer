 package com.tehbeard.utils;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.util.Set;
 
 import javax.annotation.processing.*;
 import javax.lang.model.element.Element;
 import javax.lang.model.element.PackageElement;
 import javax.lang.model.element.TypeElement;
 import javax.tools.StandardLocation;
 
 import org.bukkit.configuration.file.YamlConfiguration;
 
 
 @SuppressWarnings("restriction")
 @SupportedAnnotationTypes({"com.tehbeard.utils.PluginMod","com.tehbeard.utils.CommandMod","com.tehbeard.utils.MultiCommandMod"})
 public class PluginProcessor extends AbstractProcessor{
 
     private Writer yaml;
     YamlConfiguration yc = new YamlConfiguration();
 
 
     public void init(ProcessingEnvironment processingEnv) {
         super.init(processingEnv);
         try {
             yaml = processingEnv.getFiler().createResource(StandardLocation.SOURCE_OUTPUT , "", "plugin.yml", (Element[])null).openWriter();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     @Override
     public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundenv) {
 
         Set<? extends Element> eles = roundenv.getElementsAnnotatedWith(PluginMod.class);
         for(Element ele : eles){
             System.out.println("Found Plugin class: " + getFullClass(ele));
 
             //TODO - TYPE CHECK FOR implements Plugin
 
             PluginMod mod = ele.getAnnotation(PluginMod.class);
             yc.set("main" , getFullClass(ele));
             yc.set("name" , mod.name());
             yc.set("version" , mod.version());
             yc.set("description",orNull(mod.description()));
             yc.set("website",orNull(mod.website()));
            yc.set("load",orNull(mod.load()));
             yc.set("authors",orNull(mod.authors()));
             yc.set("depend",orNull(mod.depend()));
             yc.set("softdepend",orNull(mod.softdepend()));
             yc.set("loadbefore",orNull(mod.loadbefore()));
             yc.set("database",orNull(mod.database()));
 
         }
 
         eles = roundenv.getElementsAnnotatedWith(CommandMod.class);
         //Process command
         for(Element ele : eles){
             System.out.println("Found command class : " + getFullClass(ele));
             processCommandMod( getFullClass(ele),ele.getAnnotation(CommandMod.class));
         }
 
         //Process Multi Command
         eles = roundenv.getElementsAnnotatedWith(MultiCommandMod.class);
         for(Element ele : eles){
             System.out.println("Found command class : " + getFullClass(ele));
             MultiCommandMod mcm = ele.getAnnotation(MultiCommandMod.class);
             for(CommandMod mod : mcm.value()){
                 processCommandMod( getFullClass(ele),mod);
             }
         }
 
         //Flush if we're done
         if(roundenv.processingOver()){
             try {
                 yaml.write(yc.saveToString());
                 yaml.flush();
             } catch (IOException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
 
         }
 
         return false;
     }
 
     private String getFullClass(Element ele){
         return ((PackageElement)ele.getEnclosingElement()).getQualifiedName() + "." + ele.getSimpleName();
     }
 
     private Object orNull(Object o){
         if(o instanceof String){
             if(((String)o).length() == 0){return null;}
         }
 
         if(o instanceof String[]){
             if(((String[])o).length == 0){return null;}
         }
 
 
         return o;
     }
 
     private void processCommandMod(String classPath,CommandMod cmod){
         String cb = "commands." + cmod.name() + ".";
         yc.set(cb + "description",orNull(cmod.description()));
         yc.set(cb + "aliases",orNull(cmod.alias()));
         yc.set(cb + "permission",orNull(cmod.permission()));
         //yc.set(cb + "permission-message",orNull(cmod.())); - TODO 
         yc.set(cb + "usage",orNull(cmod.usage()));
     }
 
 }
