 /*
                         queryj
 
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
 * Description: Common logic for all templates.
  *
  * Date: 2013/08/15
  * Time: 08:24
  *
  */
 package org.acmsl.queryj.api;
 
 /*
  * Importing QueryJ-Core classes.
  */
 import org.acmsl.queryj.QueryJCommand;
 import org.acmsl.queryj.api.exceptions.CannotFindPlaceholderImplementationException;
 import org.acmsl.queryj.api.exceptions.CannotFindTemplateGroupException;
 import org.acmsl.queryj.api.exceptions.CannotFindTemplateInGroupException;
 import org.acmsl.queryj.api.exceptions.InvalidTemplateException;
 import org.acmsl.queryj.api.exceptions.QueryJBuildException;
 import org.acmsl.queryj.api.handlers.fillhandlers.FillHandler;
 import org.acmsl.queryj.api.placeholders.FillTemplateChainFactory;
 import org.acmsl.queryj.metadata.DecorationUtils;
 
 /*
  * Importing ACM-SL Commons classes.
  */
 import org.acmsl.commons.logging.UniqueLogFactory;
 import org.acmsl.commons.utils.ClassLoaderUtils;
 
 /*
  * Importing ANTLR classes.
  */
 import org.antlr.v4.parse.ANTLRParser;
 
 /*
  * Importing Apache Commons Logging classes.
  */
 import org.apache.commons.logging.Log;
 
 /*
  * Importing JetBrains annotations.
  */
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 
 /*
  * Importing checkthread.org annotations.
  */
 import org.checkthread.annotations.ThreadSafe;
 
 /*
  * Importing StringTemplate classes.
  */
 import org.stringtemplate.v4.ST;
 import org.stringtemplate.v4.STErrorListener;
 import org.stringtemplate.v4.STGroup;
 import org.stringtemplate.v4.misc.STMessage;
 
 /*
  * Importing JDK classes.
  */
 import java.io.Serializable;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.ServiceLoader;
 
 /**
 * Common logic for all templates.
  * @author <a href="mailto:queryj@acm-sl.org">Jose San Leandro</a>
  * @since 3.0
  * Created: 2013/08/15 08/24
  */
 @ThreadSafe
 public abstract class AbstractTemplate<C extends TemplateContext>
     implements  STTemplate<C>,
                 DefaultThemeConstants,
                 Serializable
 {
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
                 @Nullable final Log log = UniqueLogFactory.getLog(AbstractQueryJTemplate.class);
 
                 if (log != null)
                 {
                     log.error(stMessage.toString());
                 }
             }
 
             @Override
             public void runTimeError(@NotNull final STMessage stMessage)
             {
                 @Nullable final Log log = UniqueLogFactory.getLog(AbstractQueryJTemplate.class);
 
                 if (log != null)
                 {
                     log.error(stMessage.toString());
                 }
             }
 
             @Override
             public void IOError(@NotNull final STMessage stMessage)
             {
                 @Nullable final Log log = UniqueLogFactory.getLog(AbstractQueryJTemplate.class);
 
                 if (log != null)
                 {
                     log.error(stMessage.toString());
                 }
             }
 
             @Override
             public void internalError(@NotNull final STMessage stMessage)
             {
                 @Nullable final Log log = UniqueLogFactory.getLog(AbstractQueryJTemplate.class);
 
                 if (log != null)
                 {
                     log.error(stMessage.toString());
                 }
             }
         };
     protected static final String GENERATING = "Generating ";
     protected static final String CONTEXT_LITERAL = "Context";
     protected static final String TEMPLATE_LITERAL = "Template";
     protected static final String FILL_TEMPLATE_CHAIN_FACTORY_LITERAL = "FillTemplateChainFactory";
     protected static final String DEFAULT_PLACEHOLDER_PACKAGE = "org.acmsl.queryj.api.placeholders";
 
     /**
      * The template context.
      */
     private C m__TemplateContext;
 
     /**
      * Caches the StringTemplateGroup for each template class.
      */
     private static Map m__mSTCache;
 
     /**
      * The placeholder package.
      */
     @NotNull
     private String m__strPlaceholderPackage;
 
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
         this(context, DEFAULT_PLACEHOLDER_PACKAGE);
     }
 
     /**
      * Builds a {@link AbstractTemplate} with given context.
      * @param context the context.
      * @param placeholderPackage the package of the placeholder classes.
      */
     protected AbstractTemplate(@NotNull final C context, @NotNull final String placeholderPackage)
     {
         immutableSetTemplateContext(context);
         immutableSetPlaceholderPackage(placeholderPackage);
         setSTCache(new HashMap());
     }
 
     /**
      * Specifies the {@link QueryJTemplateContext}.
      * @param context the context.
      */
     private void immutableSetTemplateContext(@NotNull final C context)
     {
         m__TemplateContext = context;
     }
 
     /**
      * Specifies the {@link QueryJTemplateContext}.
      * @param context the context.
      */
     @SuppressWarnings("unused")
     protected void setTemplateContext(@NotNull final C context)
     {
         immutableSetTemplateContext(context);
     }
 
     /**
      * Retrieves the {@link QueryJTemplateContext context}.
      * @return such context.
      */
     @NotNull
     @Override
     public C getTemplateContext()
     {
         return m__TemplateContext;
     }
 
     /**
      * Specifies the placeholder package.
      * @param pkg such package.
      */
     protected final void immutableSetPlaceholderPackage(@NotNull final String pkg)
     {
         this.m__strPlaceholderPackage = pkg;
     }
 
     /**
      * Specifies the placeholder package.
      * @param pkg such package.
      */
     @SuppressWarnings("unused")
     protected void setPlaceholderPackage(@NotNull final String pkg)
     {
         immutableSetPlaceholderPackage(pkg);
     }
 
     /**
      * Retrieves the placeholder package.
      * @return such package.
      */
     @NotNull
     protected String getPlaceholderPackage()
     {
         return this.m__strPlaceholderPackage;
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
     @Override
     public abstract STGroup retrieveGroup();
 
     /**
      * Retrieves the string template group.
      * @param path the path.
      * @param lookupPaths the lookup paths.
      * @return such instance.
      */
     @Nullable
     protected STGroup retrieveGroup(@NotNull final String path, @NotNull final List<String> lookupPaths)
     {
         return
             retrieveGroup(
                 path,
                 lookupPaths,
                 Charset.defaultCharset(),
                 getSTCache());
     }
 
     /**
      * Retrieves the string template group.
      * @param path the path.
      * @param lookupPaths the lookup paths.
      * @param charset the charset.
      * @param cache the ST cache.
      * @return such instance.
      */
     @SuppressWarnings("unchecked")
     @Nullable
     protected STGroup retrieveGroup(
         @NotNull final String path,
         @NotNull final List<String> lookupPaths,
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
                     lookupPaths,
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
         return ".:\\AbstractQueryJTemplate//STCACHE//" + path + "//";
     }
 
     /**
      * Retrieves the string template group.
      * @param path the path.
      * @param lookupPaths the lookup paths.
      * @param errorListener the {@link STErrorListener} instance.
      * @param charset the charset.
      * @param stUtils the {@link STUtils} instance.
      * @return such instance.
      */
     @Nullable
     protected STGroup retrieveGroup(
         @NotNull final String path,
         @NotNull final List<String> lookupPaths,
         @NotNull final STErrorListener errorListener,
         @NotNull final Charset charset,
         @NotNull final STUtils stUtils)
     {
         return stUtils.retrieveGroup(path, lookupPaths, errorListener, charset);
     }
 
     /**
      * Configures given {@link org.stringtemplate.v4.ST} instance.
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
      * Logs a custom header.
      * @param header the header.
      */
     protected void logHeader(@Nullable final String header)
     {
         @Nullable final Log t_Log = UniqueLogFactory.getLog(AbstractQueryJTemplate.class);
 
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
         return GENERATING + getClass().getName() + ".";
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
         @Override
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
             UniqueLogFactory.getLog(AbstractQueryJTemplate.class);
 
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
      * @param decorationUtils the {@link org.acmsl.queryj.metadata.DecorationUtils} instance.
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
      * Retrieves the header.
      * @param context the template context.
      * @return such information.
      */
     @Nullable
     protected abstract String getHeader(@NotNull final C context);
 
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
      * @param context the {@link QueryJTemplateContext} instance.
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
      * Retrieves the source code generated by this template.
      * @param header the header.
      * @param context the context.
      * @param relevantOnly whether to include only relevant placeholders.
      * @return such code.
      * @throws InvalidTemplateException if the template cannot be processed.
      */
     @Nullable
     protected String generateOutput(
         @SuppressWarnings("unused") @Nullable final String header,
         @NotNull final C context,
         final boolean relevantOnly)
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
                     @NotNull final Map<String, Object> placeHolders = new HashMap<String, Object>();
 
                     @NotNull final List<FillTemplateChain<? extends FillHandler>> fillChains =
                         buildFillTemplateChains(context);
 
                     for (@NotNull final FillTemplateChain<? extends FillHandler> chain : fillChains)
                     {
                         @NotNull final QueryJCommand command = chain.providePlaceholders(relevantOnly);
 
                         for (@NotNull final String key: command.getKeys())
                         {
                             placeHolders.put(key, command.getObjectSetting(key));
                         }
                     }
 
                     t_Template.add(CONTEXT, placeHolders);
 
 //                    for (final Map.Entry<Object, Object> placeHolder : (Set <Map.Entry<Object, Object>>) placeHolders.entrySet())
 //                    {
 //                        t_Template.add(placeHolder.getKey().toString(), placeHolder.getValue());
 //                    }
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
 
                         @Nullable final Log t_Log = UniqueLogFactory.getLog(AbstractQueryJTemplate.class);
 
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
             retrieveFillTemplateChainFactoryClass(context, getPlaceholderPackage());
 
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
      * Retrieves the placeholder chain provider.
      * @param context the context.
      * @param placeholderPackage the placeholder package.
      * @return the {@link org.acmsl.queryj.tools.PlaceholderChainProvider} class.
      * @throws QueryJBuildException if the template factory class is unavailable.
      */
     @Nullable
     @SuppressWarnings("unchecked")
     protected Class<FillTemplateChainFactory<C>> retrieveFillTemplateChainFactoryClass(
         @NotNull final C context, @NotNull final String placeholderPackage)
         throws QueryJBuildException
     {
         @Nullable Class<FillTemplateChainFactory<C>> result = null;
 
         @NotNull final String contextName = context.getClass().getSimpleName();
 
         @NotNull final String baseName =
             buildFillTemplateChainFactoryClass(contextName, placeholderPackage);
 
         try
         {
             result = (Class<FillTemplateChainFactory<C>>) Class.forName(baseName);
         }
         catch (@NotNull final ClassNotFoundException classNotFound)
         {
             @Nullable final Log t_Log =
                 UniqueLogFactory.getLog(AbstractQueryJTemplate.class);
 
             if (t_Log != null)
             {
                 t_Log.info("Template context " + contextName + " not supported", classNotFound);
             }
         }
 
         return result;
     }
 
     /**
      * Builds the {@link FillTemplateChainFactory} class name.
      * @param contextName the context name.
      * @param placeholderPackage the placeholder package.
      * @return the class name.
      */
     @NotNull String buildFillTemplateChainFactoryClass(
         @NotNull final String contextName, @NotNull final String placeholderPackage)
     {
         @NotNull final String result;
 
         @NotNull String aux = contextName;
 
         if (aux.endsWith(CONTEXT_LITERAL))
         {
             aux = aux.substring(0, aux.lastIndexOf(CONTEXT_LITERAL));
         }
 
         if (aux.endsWith(TEMPLATE_LITERAL))
         {
             aux = aux.substring(0, aux.lastIndexOf(TEMPLATE_LITERAL));
         }
 
         if (!aux.endsWith(FILL_TEMPLATE_CHAIN_FACTORY_LITERAL))
         {
             aux = aux + FILL_TEMPLATE_CHAIN_FACTORY_LITERAL;
         }
 
         result = placeholderPackage + "." + aux;
 
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
 
     @NotNull
     @Override
     public String toString()
     {
         return "{ \"class\": \"AbstractTemplate" +
                "\", \"templateContext\": \"" + this.m__TemplateContext +
                "\", \"placeholderPackage\": \"" + this.m__strPlaceholderPackage + "\" }";
     }
 }
