 package de.zib.gndms.infra.system;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Sets;
 import com.google.inject.Binder;
 import com.google.inject.Injector;
 import com.google.inject.Module;
 import de.zib.gndms.infra.action.*;
 import de.zib.gndms.kit.config.ParameterTools;
 import de.zib.gndms.kit.monitor.ActionCaller;
 import de.zib.gndms.logic.action.Action;
 import de.zib.gndms.logic.action.SkipActionInitializationException;
 import de.zib.gndms.logic.model.BatchUpdateAction;
 import de.zib.gndms.logic.model.DefaultBatchUpdateAction;
 import de.zib.gndms.logic.model.DelegatingEntityUpdateListener;
 import de.zib.gndms.logic.model.EntityUpdateListener;
 import de.zib.gndms.logic.model.config.ConfigAction;
 import de.zib.gndms.logic.model.config.EchoOptionsAction;
 import de.zib.gndms.logic.model.config.HelpOverviewAction;
 import de.zib.gndms.logic.model.dspace.AssignSliceKindAction;
 import de.zib.gndms.logic.model.dspace.SetupSliceKindAction;
 import de.zib.gndms.logic.model.dspace.SetupSubspaceAction;
 import de.zib.gndms.logic.model.gorfx.ConfigOfferTypeAction;
 import de.zib.gndms.logic.model.gorfx.SetupOfferTypeAction;
 import de.zib.gndms.model.common.GridResource;
 import de.zib.gndms.model.common.ModelUUIDGen;
 import org.apache.axis.components.uuid.UUIDGen;
 import org.apache.axis.components.uuid.UUIDGenFactory;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.jetbrains.annotations.NotNull;
 
 import javax.persistence.EntityManager;
 import java.io.PrintWriter;
 import java.util.Set;
 
 
 /**
  * ThingAMagic.
 *
 * @author Stefan Plantikow<plantikow@zib.de>
 * @version $Id$
 *
 *          User: stepn Date: 03.09.2008 Time: 16:43:46
 */
 public final class ConfigActionCaller implements ActionCaller, Module {
     private @NotNull final Log logger = LogFactory.getLog(ConfigActionCaller.class);
 
     private @NotNull final UUIDGen uuidGen = UUIDGenFactory.getUUIDGen();
     private final @NotNull ModelUUIDGen actionUUIDGen = new ModelUUIDGen() {
             public @NotNull String nextUUID() {
                 return uuidGen.nextUUID();
             }
     };
 
     private final Set<Class<? extends ConfigAction<?>>> configActions =
             Sets.newConcurrentHashSet();
 
     private final Function<String, String> classToActionNameMapper =
         new Function<String, String>() {
             public String apply(@com.google.common.base.Nullable final String s) {
                 if (s.startsWith("de.zib.gndms.infra.action") && s.endsWith("Action"))
                     return ".sys" + s.substring("de.zib.gndms.infra.action".length(), s.length()-6);
                 if (s.startsWith("de.zib.gndms.logic.model") && s.endsWith("Action"))
                     return s.substring("de.zib.gndms.logic.model".length(), s.length()-6);
                 return s;
             }
         };
 
     private final GNDMSystem system;
 
 	private final Injector injector;
 
 
 	@SuppressWarnings({ "ThisEscapedInObjectConstruction", "OverlyCoupledMethod" })
 	public ConfigActionCaller(final @NotNull GNDMSystem systemParam) {
         system = systemParam;
         configActions.add(SetupSubspaceAction.class);
         configActions.add(EchoOptionsAction.class);
         configActions.add(GetHomeInfoAction.class);
         configActions.add(SetupOfferTypeAction.class);
         configActions.add(ConfigOfferTypeAction.class);
         configActions.add(SetupSliceKindAction.class);
         configActions.add(AssignSliceKindAction.class);
         configActions.add(RefreshSystemAction.class);
         configActions.add(SetupDefaultConfigletAction.class);
         configActions.add(ReadC3CatalogAction.class);
 
 		injector = system.getInstanceDir().getSystemAccessInjector().createChildInjector(this);
     }
 
 
     @SuppressWarnings({ "RawUseOfParameterizedType", "unchecked" })
     public ConfigAction<?> instantiateConfigAction(final @NotNull String name,
                                                    final @NotNull String params)
             throws ClassNotFoundException, IllegalAccessException, InstantiationException,
             ParameterTools.ParameterParseException {
         final Class<? extends ConfigAction<?>> clazz = findActionClass(name);
         if (ConfigAction.class.isAssignableFrom(clazz)) {
             final ConfigAction configAction = clazz.newInstance();
             configAction.parseLocalOptions(params);
             return configAction;
         }
         else
             throw new IllegalArgumentException("Not a ConfigAction");
     }
 
 
     @SuppressWarnings({ "MethodMayBeStatic" })
     private Class<? extends ConfigAction<?>> findActionClass(
             final @NotNull String name) throws ClassNotFoundException
     {
         if (name.length() > 0) {
             final String nameToLower = name.toLowerCase();
             if ("help".equals(nameToLower) || "-help".equals(nameToLower)
                     || "--help".equals(nameToLower)) {
                 return toConfigActionClass(HelpOverviewAction.class);
             }
             try {
                 if (name.startsWith(".sys"))
                     return toConfigActionClass(Class.forName(
                             "de.zib.gndms.infra.action" + name.substring(4) + "Action"));
             }
             catch (ClassNotFoundException e) {
                 // continue trying
             }
             if (name.charAt(0) == '.')
                 return toConfigActionClass(
                         Class.forName("de.zib.gndms.logic.model" + name + "Action"));
         }
         return toConfigActionClass(Class.forName(name));
     }
 
 
     @SuppressWarnings({ "unchecked" })
     private static Class<? extends ConfigAction<?>> toConfigActionClass(
             final Class<?> helpOverviewActionClassParam) {
          if (ConfigAction.class.isAssignableFrom(helpOverviewActionClassParam))
                 return (Class<? extends ConfigAction<?>>) helpOverviewActionClassParam;
         else
              throw new IllegalArgumentException("Given class is not a ConfigAction");
     }
 
 
     @SuppressWarnings({ "FeatureEnvy" })
     public Object callAction(
             final @NotNull String className, final @NotNull String opts,
             final @NotNull PrintWriter writer) throws Exception {
         ConfigAction<?> action = instantiateConfigAction(className, opts.trim());
         action.setOwnEntityManager(system.getEntityManagerFactory().createEntityManager());
         action.setPrintWriter(writer);
         action.setClosingWriterOnCleanUp(false);
         action.setWriteResult(true);
         action.setUUIDGen(actionUUIDGen);
         action.setOwnPostponedActions(new DefaultBatchUpdateAction<GridResource>());
         final DelegatingEntityUpdateListener<GridResource> updateListener =
                 DelegatingEntityUpdateListener.getInstance(system);
         action.getPostponedActions().setListener(updateListener);
         if (action instanceof SystemAction)
             ((SystemAction<?>)action).setSystem(system);
	    // Help Action required dynamic casting due to compiler bug in older 1.5 javac
	    // regarding generics and instanceof
        if (HelpOverviewAction.class.isInstance(action)) {
	        HelpOverviewAction helpAction = HelpOverviewAction.class.cast(action);
            helpAction.setConfigActions(configActions);
            helpAction.setNameMapper(classToActionNameMapper);
         }
 		injector.injectMembers(action);
 
         logger.info("Running " + className + ' ' + opts);
         try {
             Object retVal = action.call();
             Action<?> postAction = action.getPostponedActions();
             postAction.call();
             return retVal;
         }
         catch (SkipActionInitializationException sa) {
             // Intentionally ignored
             return null;
         }
         catch (Exception e) {
             logger.warn("Failure during " + className + ' ' + opts, e);
             throw e;
         }
     }
 
 
 	public void configure(final @NotNull Binder binder) {
 		binder.skipSources(GNDMSystem.class,
 		                   EntityManager.class,
 		                   ModelUUIDGen.class,
 		                   UUIDGen.class,
 		                   EntityUpdateListener.class,
 		                   BatchUpdateAction.class);
 		binder.bind(ConfigActionCaller.class).toInstance(this);
 	}
 }
