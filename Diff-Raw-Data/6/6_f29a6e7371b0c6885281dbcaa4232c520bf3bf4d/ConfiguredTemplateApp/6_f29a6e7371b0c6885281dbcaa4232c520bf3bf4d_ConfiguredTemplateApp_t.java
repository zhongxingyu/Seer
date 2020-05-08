 /**
  * 
  */
 package org.melati.app.test;
 
 import org.melati.util.test.StringInputStream;
 import org.melati.Melati;
 import org.melati.MelatiConfig;
 import org.melati.app.TemplateApp;
 import org.melati.login.AccessHandler;
 import org.melati.login.CommandLineAccessHandler;
 import org.melati.login.OpenAccessHandler;
 import org.melati.util.InstantiationPropertyException;
 import org.melati.util.MelatiException;
 
 /**
  * @author timp
  *
  */
 public class ConfiguredTemplateApp extends TemplateApp {
 
   /**
    * 
    */
   public ConfiguredTemplateApp() {
     super();
   }
 
   /**
    * {@inheritDoc}
    * @see org.melati.app.AbstractConfigApp#melatiConfig()
    */
   protected MelatiConfig melatiConfig() throws MelatiException {
     MelatiConfig config = super.melatiConfig();
 
       try {
        config.setAccessHandler(
            (AccessHandler)CommandLineAccessHandler.class
                 .newInstance());
       } catch (Exception e) {
        throw new InstantiationPropertyException(CommandLineAccessHandler.class
                 .getName(), e);
       }
 
 
     return config;
     
   }
 
   /**
    * {@inheritDoc}
    * @see org.melati.app.AbstractTemplateApp#init(java.lang.String[])
    */
   public Melati init(String[] args) throws MelatiException {
     Melati melati = super.init(args);
     CommandLineAccessHandler ah = (CommandLineAccessHandler)melati.getConfig().getAccessHandler();
     ah.setInput(new StringInputStream("_administrator_\nFIXME\n"));
     return melati;
     
   }
 
 }
