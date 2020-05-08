 /*
                         QueryJ
 
     Copyright (C) 2002-today  Jose San Leandro Armendariz
                               chous@acm-sl.org
 
     This library is free software; you can redistribute it and/or
     modify it under the terms of the GNU General Public
     License as published by the Free Software Foundation; either
     version 2 of the License, or any later version.
 
     This library is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
     General Public License for more details.
 
     You should have received a copy of the GNU General Public
     License along with this library; if not, write to the Free Software
     Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 
     Thanks to ACM S.L. for distributing this library under the GPL license.
     Contact info: jose.sanleandro@acm-sl.com
 
  ******************************************************************************
  *
  * Filename: AbstractTemplate.java
  *
  * Author: Jose San Leandro Armendariz
  *
  * Description: Represents generic templates.
  *
  */
 package org.acmsl.queryj.api;
 
 /*
  * Importing project classes.
  */
 import org.acmsl.queryj.api.exceptions.CannotFindPlaceholderImplementationException;
 import org.acmsl.queryj.api.exceptions.CannotFindTemplateGroupException;
 import org.acmsl.queryj.api.exceptions.CannotFindTemplateInGroupException;
 import org.acmsl.queryj.api.exceptions.InvalidTemplateException;
 import org.acmsl.queryj.api.handlers.fillhandlers.FillHandler;
 import org.acmsl.queryj.api.placeholders.FillTemplateChainFactory;
 import org.acmsl.queryj.tools.PlaceholderChainProvider;
 import org.acmsl.queryj.api.exceptions.QueryJBuildException;
 import org.acmsl.queryj.metadata.DecorationUtils;
 import org.acmsl.queryj.metadata.MetadataManager;
 
 /*
  * Importing some ACM-SL Commons classes.
  */
 import org.acmsl.commons.logging.UniqueLogFactory;
 import org.acmsl.commons.utils.ClassLoaderUtils;
 
 /*
  * Importing Commons-Logging classes.
  */
 import org.antlr.v4.parse.ANTLRParser;
 import org.apache.commons.logging.Log;
 
 /*
  * Importing ANTLR classes.
  */
 
 /*
  * Importing StringTemplate classes.
  */
 import org.stringtemplate.v4.ST;
 import org.stringtemplate.v4.STErrorListener;
 import org.stringtemplate.v4.STGroup;
 import org.stringtemplate.v4.misc.STMessage;
 
 /*
  * Importing some JetBrains annotations.
  */
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 
 /*
  * Importing some JDK classes.
  */
 import java.io.Serializable;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.ServiceLoader;
 import java.util.Set;
 
 /**
  * Represents generic templates.
  * @author <a href="mailto:chous@acm-sl.org">Jose San Leandro Armendariz</a>
  */
 public abstract class AbstractTemplate<C extends TemplateContext>
     implements  STTemplate<C>,
                 DefaultThemeConstants,
                 Serializable
 {
     private static final long serialVersionUID = -3708579868912707138L;
 
     /**
      * The default StringTemplate error listener.
      */
     @NotNull
     protected static final STErrorListener
         DEFAULT_ST_ERROR_LISTENER =
             new STErrorListener()
             {
                 @Override
                 public void compileTimeError(@NotNull final STMessage stMessage)
                 {
                     @Nullable final Log log = UniqueLogFactory.getLog(AbstractTemplate.class);
 
                     if (log != null)
                     {
                         log.error(stMessage.toString());
                     }
                 }
 
                 @Override
                 public void runTimeError(@NotNull final STMessage stMessage)
                 {
                     @Nullable final Log log = UniqueLogFactory.getLog(AbstractTemplate.class);
 
                     if (log != null)
                     {
                         log.error(stMessage.toString());
                     }
                 }
 
                 @Override
                 public void IOError(@NotNull final STMessage stMessage)
                 {
                     @Nullable final Log log = UniqueLogFactory.getLog(AbstractTemplate.class);
 
                     if (log != null)
                     {
                         log.error(stMessage.toString());
                     }
                 }
 
                 @Override
                 public void internalError(@NotNull final STMessage stMessage)
                 {
                     @Nullable final Log log = UniqueLogFactory.getLog(AbstractTemplate.class);
 
                     if (log != null)
                     {
                         log.error(stMessage.toString());
                     }
                 }
             };
 
     /**
      * The template context.
      */
     private C m__TemplateContext;
 
     /**
      * Caches the StringTemplateGroup for each template class.
      */
     private static Map m__mSTCache;
 
     /**
      * The cached processed header.
      */
     private String m__strCachedProcessedHeader;
 
     /**
      * A singleton container to avoid the double-checking lock idiom.
      */
     protected static final class FinalizingThreadSingletonContainer
     {
         /**
          * The singleton instance.
          */
         private static final FinalizingThread THREAD =
             new FinalizingThread();
 
         /**
          * Retrieves the thread.
          * @return such information.
          */
         @NotNull
         public static FinalizingThread getInstance()
         {
             return THREAD;
         }
     }
 
     /**
      * Builds a {@link AbstractTemplate} with given context.
      * @param context the context.
      */
     protected AbstractTemplate(@NotNull final C context)
     {
         immutableSetTemplateContext(context);
         setSTCache(new HashMap());
     }
 
     /**
      * Specifies the {@link TemplateContext}.
      * @param context the context.
      */
     private void immutableSetTemplateContext(@NotNull final C context)
     {
         m__TemplateContext = context;
     }
 
     /**
      * Specifies the {@link TemplateContext}.
      * @param context the context.
      */
     @SuppressWarnings("unused")
     protected void setTemplateContext(@NotNull final C context)
     {
         immutableSetTemplateContext(context);
     }
 
     /**
      * Retrieves the {@link TemplateContext context}.
      * @return such context.
      */
     @NotNull
     public C getTemplateContext()
     {
         return m__TemplateContext;
     }
 
     /**
      * Specifies the cached processed header.
      * @param header such value.
      */
     protected final void immutableSetCachedProcessedHeader(@Nullable final String header)
     {
         m__strCachedProcessedHeader = header;
     }
     
     /**
      * Specifies the cached processed header.
      * @param header such value.
      */
     protected void setCachedProcessedHeader(@Nullable final String header)
     {
         immutableSetCachedProcessedHeader(header);
     }
     
     /**
      * Retrieves the cached processed header.
      * @return such value.
      */
     @Nullable
     protected String getCachedProcessedHeader()
     {
         return m__strCachedProcessedHeader;
     }
 
     /**
      * Retrieves the header.
      * @param context the template context.
      * @return such information.
      */
     @Nullable
     protected String getHeader(@NotNull final TemplateContext context)
     {
         return context.getHeader();
     }
 
     /**
      * Retrieves the processed header.
      * @param input the input.
      * @return such information.
      */
     @Nullable
     @SuppressWarnings("unused")
     public String getProcessedHeader(@NotNull final Map input)
     {
         @Nullable String result = getCachedProcessedHeader();
         
         if  (result == null)
         {
 //            result = processHeader(input, getHeader(getTemplateContext()));
         }
         
         return result;
     }
 
     /**
      * Specifies the ST cache.
      * @param map the map to use as cache.
      */
     protected static void setSTCache(@NotNull final Map map)
     {
         m__mSTCache = map;
     }
 
     /**
      * Retrieves the ST cache.
      * @return the map being used as cache.
      */
     @NotNull
     protected static Map getSTCache()
     {
         return m__mSTCache;
     }
 
     /**
      * Retrieves the string template group.
      * @return such instance.
      */
     @Nullable
     public abstract STGroup retrieveGroup();
 
     /**
      * Retrieves the string template group.
      * @param path the path.
      * @return such instance.
      */
     @Nullable
     protected STGroup retrieveGroup(@NotNull final String path)
     {
         return
             retrieveGroup(
                 path,
                 Charset.defaultCharset(),
                 getSTCache());
     }
     
     /**
      * Retrieves the string template group.
      * @param path the path.
      * @param charset the charset.
      * @param cache the ST cache.
      * @return such instance.
      */
     @SuppressWarnings("unchecked")
     @Nullable
     protected STGroup retrieveGroup(
         @NotNull final String path,
         @NotNull final Charset charset,
         @NotNull final Map cache)
     {
         @Nullable STGroup result;
         
         @NotNull final Object t_Key = buildSTGroupKey(path);
 
         result = (STGroup) cache.get(t_Key);
 
         if  (result == null)
         {
             result =
                 retrieveGroup(
                     path,
                     retrieveStErrorListener(),
                     charset,
                     STUtils.getInstance());
 
             if  (result != null)
             {
                 cache.put(t_Key, result);
             }
         }
         
         return result;
     }
 
     /**
      * Retrieves the StringTemplate error listener.
      * @return such instance.
      */
     @NotNull
     protected STErrorListener retrieveStErrorListener()
     {
         return DEFAULT_ST_ERROR_LISTENER;
     }
 
     /**
      * Builds a key to store the ST group associated to
      * given path and theme.
      * @param path the ST path.
      * @return such key.
      */
     @NotNull
     protected final Object buildSTGroupKey(@NotNull final String path)
     {
         return ".:\\AbstractTemplate//STCACHE//" + path + "//";
     }
 
     /**
      * Retrieves the string template group.
      * @param path the path.
      * @param errorListener the {@link STErrorListener}
      * instance.
      * @param charset the charset.
      * @param stUtils the {@link STUtils} instance.
      * @return such instance.
      */
     @Nullable
     protected STGroup retrieveGroup(
         @NotNull final String path,
         @NotNull final STErrorListener errorListener,
         @NotNull final Charset charset,
         @NotNull final STUtils stUtils)
     {
         return stUtils.retrieveGroup(path, errorListener, charset);
     }
 
     /**
      * Configures given {@link ST} instance.
      * @param stringTemplate such template.
      */
     @SuppressWarnings("unused")
     protected void configure(@NotNull final ST stringTemplate)
     {
         //stringTemplate.setPassThroughAttributes(true);
         //stringTemplate.setLintMode(true);
         //stringTemplate.setErrorListener(retrieveStErrorListener());
     }
 
     /**
      * Retrieves the template in given group.
      * @param group the StringTemplate group.
      * @return the template.
      */
     @Nullable
     protected ST retrieveTemplate(@Nullable final STGroup group)
     {
         @Nullable ST result = null;
         
         if  (group != null)
         {
             result = group.getInstanceOf(TEMPLATE_NAME);
         }
 
         return result;
     }
     
     /**
      * Generates the source code.
      * @return such output.
      * @throws org.acmsl.queryj.api.exceptions.InvalidTemplateException if the template cannot be generated.
      */
     @Override
     @Nullable
     public String generate(final boolean relevantOnly)
       throws InvalidTemplateException
     {
         return generate(getTemplateContext(), relevantOnly);
     }
 
     /**
      * Generates the source code.
      * @param context the {@link TemplateContext} instance.
      * @param relevantOnly whether to include only relevant placeholders.
      * @return such output.
      * @throws InvalidTemplateException if the template cannot be generated.
      */
     @Nullable
     protected String generate(@NotNull final C context, final boolean relevantOnly)
         throws  InvalidTemplateException
     {
         final String result;
 
         if (!relevantOnly)
         {
             logHeader(buildHeader());
         }
 
         //traceClassLoaders();
 
         result = generateOutput(getHeader(context), context, relevantOnly);
 
         //cleanUpClassLoaderTracing();
 
         return result;
     }
 
     /**
      * Logs a custom header.
      * @param header the header.
      */
     protected void logHeader(@Nullable final String header)
     {
         @Nullable final Log t_Log = UniqueLogFactory.getLog(AbstractTemplate.class);
         
         if  (t_Log != null)
         {
             t_Log.info(header);
         }
     }
 
     /**
      * Builds the header for logging purposes.
      * @return such header.
      */
     @NotNull
     protected String buildHeader()
     {
         return "Generating " + getClass().getName() + ".";
     }
 
     /**
      * Prints a log message displaying ClassLoader issues related
      * to ANTLR.jar and StringTemplate.jar.
      */
     @SuppressWarnings("unused")
     protected synchronized void traceClassLoaders()
     {
         @NotNull final FinalizingThread t_FinalizingThread =
             FinalizingThreadSingletonContainer.getInstance();
 
         if (t_FinalizingThread.isNew())
         {
             @Nullable final Runtime t_Runtime = Runtime.getRuntime();
 
             if  (t_Runtime != null)
             {
                 t_Runtime.removeShutdownHook(t_FinalizingThread);
                 t_Runtime.addShutdownHook(t_FinalizingThread);
             }
         }
     }
 
     /**
      * Cleans up the thread to trace class loaders on shutdown.
      */
     @SuppressWarnings("unused")
     protected void cleanUpClassLoaderTracing()
     {
         @NotNull final FinalizingThread t_FinalizingThread =
             FinalizingThreadSingletonContainer.getInstance();
 
         @Nullable final Runtime t_Runtime = Runtime.getRuntime();
 
         if  (t_Runtime != null)
         {
             t_Runtime.removeShutdownHook(t_FinalizingThread);
         }
     }
 
     /**
      * Thread to provide some information about ANTLR class loaders,
      * since it's likely to have triggered the VM shutdown.
      * @author <a href="mailto:jose.sanleandro@ventura24.es"
      * >Jose San Leandro</a>
      * @version $Revision$
      */
     protected static class FinalizingThread
         extends  Thread
     {
         /**
          * Whether the thread is new or not.
          */
         private boolean m__bNew = true;
 
         /**
          * Retrieves whether this thread has just been created or not.
          * @return such information.
          */
         @SuppressWarnings("unused")
         public boolean isNew()
         {
             final boolean result = m__bNew;
 
             m__bNew = false;
 
             return result;
         }
 
         /**
          * Runs the thread.
          */
         public void run()
         {
             traceANTLRClassLoadingIssues();
         }
 
         @Override
         public String toString()
         {
             return "FinalizingThread{ new=" + m__bNew + '}';
         }
     }
 
     /**
      * Prints a log message displaying ClassLoader issues related
      * to ANTLR.jar and StringTemplate.jar.
      */
     protected static void traceANTLRClassLoadingIssues()
     {
         // CharScanner; panic: ClassNotFoundException:
         // org.antlr.stringtemplate.language.ChunkToken
         // can happen if ANTLR gets loaded by a ClassLoader
         // with no reference to StringTemplate classes.
         @Nullable final Log t_Log =
             UniqueLogFactory.getLog(AbstractTemplate.class);
 
         @NotNull final ClassLoaderUtils t_ClassLoaderUtils =
             ClassLoaderUtils.getInstance();
 
         if  (t_Log != null)
         {
             @NotNull final StringBuilder t_sbMessage = new StringBuilder();
 
             @Nullable final String t_strAntlrLocation =
                 t_ClassLoaderUtils.findLocation(ANTLRParser.class);
 
             @Nullable final String t_strStringTemplateLocation =
                 t_ClassLoaderUtils.findLocation(ST.class);
 
             t_sbMessage.append(
                   "A fatal error in StringTemplate-based generation "
                 + "has stopped QueryJ build process.\n"
                 + "If you see error messages from StringTemplate, "
                 + "review your templates. Otherwise, if the VM "
                 + "exited due to ClassNotFoundException or "
                 + "NoClassDefFoundException regarding ANTLR or StringTemplate "
                 + "classes (i.e. ChunkToken), then "
                 + "antlr-X.Y.jar has been loaded by a class loader "
                 + "with no idea of where StringTemplate classes "
                 + "are, and therefore ANTLR's reflection-based "
                 + "instantiation fails. Check your classpath or the way "
                 + "it's defined by Ant or any other tool you might be "
                 + "using. ");
             final boolean t_bAntlrLocationFound =
                 (   (t_strAntlrLocation != null)
                  && (t_strAntlrLocation.trim().length() > 0));
             final boolean t_bStringTemplateLocationFound =
                 (   (t_strStringTemplateLocation != null)
                  && (t_strStringTemplateLocation.trim().length() > 0));
 
             if  (   (t_bAntlrLocationFound)
                  || (t_bStringTemplateLocationFound))
             {
                 t_sbMessage.append("Hint: ");
 
                 if  (   (t_strAntlrLocation != null)
                      && (t_strAntlrLocation.trim().length() > 0))
                 {
                     t_sbMessage.append("ANTLR is loaded from ");
                     t_sbMessage.append(t_strAntlrLocation);
                 }
 
                 if  (t_bStringTemplateLocationFound)
                 {
                     if (t_bAntlrLocationFound)
                     {
                         t_sbMessage.append(
                             " whereas ");
                     }
                     t_sbMessage.append(
                         "StringTemplate is loaded from ");
                     t_sbMessage.append(
                         t_strStringTemplateLocation);
                 }
             }
 
             t_Log.fatal(t_sbMessage.toString());
         }
     }
 
     /**
      * Normalizes given value, in lower-case.
      * @param value the value.
      * @param decorationUtils the {@link DecorationUtils} instance.
      * @return such output.
      */
     @SuppressWarnings("unused")
     @NotNull
     protected String normalizeLowercase(
         @NotNull final String value, @NotNull final DecorationUtils decorationUtils)
     {
         return decorationUtils.normalizeLowercase(value);
     }
 
     /**
      * Capitalizes given value.
      * @param value the value.
      * @return such output.
      */
     @NotNull
     protected String capitalize(@NotNull final String value)
     {
         return capitalize(value, DecorationUtils.getInstance());
     }
 
     /**
      * Capitalizes given value.
      * @param value the value.
      * @param decorationUtils the {@link DecorationUtils} instance.
      * @return such output.
      */
     @NotNull
     protected String capitalize(
         @NotNull final String value, @NotNull final DecorationUtils decorationUtils)
     {
         return decorationUtils.capitalize(value);
     }
 
     /**
      * Retrieves the source code generated by this template.
      * @param header the header.
      * @param context the context.
      * @param relevantOnly whether to include only relevant placeholders.
      * @return such code.
      * @throws InvalidTemplateException if the template cannot be processed.
      */
     @Nullable
     protected String generateOutput(
         @Nullable final String header, @NotNull final C context, final boolean relevantOnly)
         throws InvalidTemplateException
     {
         return
             generateOutput(
                 header,
                 context,
                 relevantOnly,
                 context.getMetadataManager());
     }
 
     /**
      * Retrieves the source code generated by this template.
      * @param header the header.
      * @param context the {@link PerTableTemplateContext} instance.
      * @param relevantOnly whether to include only relevant placeholders.
      * @param metadataManager the metadata manager.
      * @return such code.
      * @throws InvalidTemplateException if the generation process fails.
      */
     @Nullable
     @SuppressWarnings("unchecked,unused")
     protected String generateOutput(
         @Nullable final String header,
         @NotNull final C context,
         final boolean relevantOnly,
         @NotNull final MetadataManager metadataManager)
         throws InvalidTemplateException
     {
         @Nullable String result = null;
 
         @Nullable Throwable t_ExceptionToWrap = null;
 
         @Nullable final STGroup t_Group = retrieveGroup();
 
         @Nullable final ST t_Template;
 
         if (t_Group == null)
         {
            throw new CannotFindTemplateGroupException(getTemplateName());
         }
         else
         {
             t_Template = retrieveTemplate(t_Group);
 
             if (t_Template != null)
             {
                 try
                 {
                     @SuppressWarnings("unchecked")
                     @NotNull final Map placeHolders = new HashMap();
 
                     @NotNull final List<FillTemplateChain<? extends FillHandler>> fillChains =
                         buildFillTemplateChains(context);
 
                     for (@NotNull final FillTemplateChain<? extends FillHandler> chain : fillChains)
                     {
                         placeHolders.putAll(chain.providePlaceholders(relevantOnly));
                     }
 
                     for (final Map.Entry<Object, Object> placeHolder : (Set <Map.Entry<Object, Object>>) placeHolders.entrySet())
                     {
                         t_Template.add(placeHolder.getKey().toString(), placeHolder.getValue());
                     }
                     //t_Template.setErrorListener(DEFAULT_ST_ERROR_LISTENER);
                 }
                 catch (@NotNull final QueryJBuildException invalidTemplate)
                 {
                     t_ExceptionToWrap = invalidTemplate;
                 }
 
                 if (t_ExceptionToWrap == null)
                 {
                     try
                     {
                         result = t_Template.render();
                     }
                     catch (@NotNull final Throwable throwable)
                     {
                         t_ExceptionToWrap = throwable;
 
                         @Nullable final Log t_Log = UniqueLogFactory.getLog(AbstractTemplate.class);
 
                         if (t_Log != null)
                         {
                             t_Log.error(
                                 "Error in template " + getTemplateName(), throwable);
                         }
 
     /*                    @Nullable final STTreeView debugTool =
                             new StringTemplateTreeView("Debugging " + getTemplateName(), t_Template);
 
                         debugTool.setVisible(true);
 
                         while (debugTool.isVisible())
                         {
                             try
                             {
                                 Thread.sleep(1000);
                             }
                             catch (InterruptedException e)
                             {
                                 e.printStackTrace();
                             }
                         }*/
                     }
                 }
                 if (t_ExceptionToWrap != null)
                 {
                     throw buildInvalidTemplateException(context, t_Template, t_ExceptionToWrap);
                 }
             }
             else
             {
                 throw new CannotFindTemplateInGroupException(t_Group, TEMPLATE_NAME);
             }
         }
 
         return result;
     }
 
     /**
      * Builds the correct chain.
      * @param context the context.
      * @return the specific {@link FillTemplateChain}.
      * @throws QueryJBuildException if the templates are unavailable.
      */
     @SuppressWarnings("unchecked")
     @NotNull
     public List<FillTemplateChain<? extends FillHandler>> buildFillTemplateChains(@NotNull final C context)
         throws QueryJBuildException
     {
         @NotNull final List<FillTemplateChain<? extends FillHandler>> result =
             new ArrayList<FillTemplateChain<? extends FillHandler>>();
 
         @Nullable final Class<FillTemplateChainFactory<C>> factoryClass =
             retrieveFillTemplateChainFactoryClass(context);
 
         if (factoryClass != null)
         {
             @Nullable final ServiceLoader<FillTemplateChainFactory<C>> loader =
                 ServiceLoader.load(factoryClass);
 
             if (loader != null)
             {
                 for (@NotNull final FillTemplateChainFactory<C> factory : loader)
                 {
                     result.add(
                         (FillTemplateChain <? extends FillHandler >)
                             factory.createFillChain(context));
                 }
             }
             else
             {
                 throw
                     new CannotFindPlaceholderImplementationException(factoryClass);
             }
         }
         else
         {
             throw
                 new CannotFindPlaceholderImplementationException(context.getClass().getName());
         }
 
         return result;
     }
 
     /**
      *  Retrieves the placeholder chain provider.
      * @param context the context.
      * @return the {@link PlaceholderChainProvider} class.
      * @throws QueryJBuildException if the template factory class is unavailable.
      */
     @Nullable
     @SuppressWarnings("unchecked")
     protected Class<FillTemplateChainFactory<C>> retrieveFillTemplateChainFactoryClass(@NotNull final C context)
         throws QueryJBuildException
     {
         @Nullable Class<FillTemplateChainFactory<C>> result = null;
 
         @NotNull final String contextName = context.getClass().getSimpleName();
 
         @NotNull String baseName = contextName;
 
         if (baseName.endsWith("Context"))
         {
             baseName = baseName.substring(0, baseName.lastIndexOf("Context"));
         }
 
         if (baseName.endsWith("Template"))
         {
             baseName = baseName.substring(0, baseName.lastIndexOf("Template"));
         }
 
         if (!baseName.endsWith("FillTemplateChainFactory"))
         {
             baseName = baseName + "FillTemplateChainFactory";
         }
 
         baseName = "org.acmsl.queryj.api.placeholders." + baseName;
 
         try
         {
             result = (Class<FillTemplateChainFactory<C>>) Class.forName(baseName);
         }
         catch (@NotNull final ClassNotFoundException classNotFound)
         {
             @Nullable final Log t_Log = UniqueLogFactory.getLog(AbstractTemplate.class);
 
             if (t_Log != null)
             {
                 t_Log.info("Template context " + contextName + " not supported", classNotFound);
             }
         }
 
         return result;
     }
 
 
     /**
      * Builds a context-specific exception.
      * @param context the context.
      * @param template the {@link ST} instance.
      * @return the specific {@link InvalidTemplateException} for the template.
      */
     @NotNull
     public abstract InvalidTemplateException buildInvalidTemplateException(
         @NotNull final C context,
         @NotNull final ST template,
         @NotNull final Throwable actualException);
 
     @Override
     @NotNull
     public String toString()
     {
         return
             "AbstractTemplate{"
             + " cachedProcessedHeader='" + m__strCachedProcessedHeader
             + "', templateContext=" + m__TemplateContext
             + '}';
     }
 }
