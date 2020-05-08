 package org.mosaic.web.handler.impl;
 
 import freemarker.cache.TemplateLoader;
import freemarker.template.*;
 import java.io.IOException;
 import java.io.Reader;
 import java.io.Writer;
 import java.nio.charset.Charset;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.util.Locale;
 import java.util.Map;
 import javax.annotation.Nonnull;
 import org.mosaic.modules.Component;
 import org.mosaic.web.application.Application;
 
 /**
  * @author arik
  */
 @Component
 final class FreemarkerRenderer
 {
     private static final String CFG_KEY = FreemarkerRenderer.class.getName() + "#configuration";
 
     void render( @Nonnull Application application,
                  @Nonnull Map<String, Object> context,
                  @Nonnull String path,
                  @Nonnull Locale locale,
                 @Nonnull Writer writer ) throws IOException, TemplateException
     {
        Configuration configuration = getConfigurationForApplication( application );
        Template template = configuration.getTemplate( path, locale );
        template.process( context, writer );
     }
 
     @Nonnull
     private Configuration getConfigurationForApplication( @Nonnull Application application )
     {
         Configuration cfg = application.getAttributes().get( CFG_KEY, Configuration.class );
         if( cfg == null )
         {
             //noinspection SynchronizationOnLocalVariableOrMethodParameter
             synchronized( application )
             {
                 cfg = application.getAttributes().get( CFG_KEY, Configuration.class );
                 if( cfg == null )
                 {
                     cfg = new Configuration();
                     cfg.setDefaultEncoding( "UTF-8" );
                     cfg.setIncompatibleImprovements( new Version( 2, 3, 20 ) );
                     cfg.setTemplateLoader( new ApplicationTemplateLoader( application ) );
                    cfg.setTemplateExceptionHandler( TemplateExceptionHandler.RETHROW_HANDLER );
                 }
             }
         }
         return cfg;
     }
 
     private class ApplicationTemplateLoader implements TemplateLoader
     {
         @Nonnull
         private final Application application;
 
         private ApplicationTemplateLoader( @Nonnull Application application )
         {
             this.application = application;
         }
 
         @Override
         public Object findTemplateSource( String name ) throws IOException
         {
             return this.application.getResources().getResource( name + ".ftl" );
         }
 
         @Override
         public long getLastModified( Object templateSource )
         {
             Path file = ( ( Application.ApplicationResources.Resource ) templateSource ).getPath();
             try
             {
                 return Files.getLastModifiedTime( file ).toMillis();
             }
             catch( IOException e )
             {
                 return -1;
             }
         }
 
         @Override
         public Reader getReader( Object templateSource, String encoding ) throws IOException
         {
             Path file = ( ( Application.ApplicationResources.Resource ) templateSource ).getPath();
             return Files.newBufferedReader( file, Charset.forName( "UTF-8" ) );
         }
 
         @Override
         public void closeTemplateSource( Object templateSource ) throws IOException
         {
             // no-op
         }
     }
 }
