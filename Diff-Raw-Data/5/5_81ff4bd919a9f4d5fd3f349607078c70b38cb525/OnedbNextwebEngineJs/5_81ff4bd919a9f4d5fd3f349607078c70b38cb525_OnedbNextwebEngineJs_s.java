 package com.ononedb.nextweb.js;
 
 import io.nextweb.Session;
 import io.nextweb.common.LocalServer;
 import io.nextweb.common.SessionConfiguration;
 import io.nextweb.engine.Capability;
 import io.nextweb.engine.Factory;
 import io.nextweb.engine.NextwebEngine;
 import io.nextweb.engine.StartServerCapability;
 import io.nextweb.js.NextwebJs;
 import io.nextweb.js.engine.JsFactory;
 import io.nextweb.js.engine.JsNextwebEngine;
 import io.nextweb.js.engine.NextwebEngineJs;
 import io.nextweb.promise.exceptions.ExceptionListener;
 import io.nextweb.promise.exceptions.ExceptionManager;
 import io.nextweb.promise.exceptions.ExceptionResult;
 import io.nextweb.promise.js.exceptions.ExceptionUtils;
 import nx.client.gwt.services.GwtRemoteService;
 import nx.client.gwt.services.GwtRemoteServiceAsync;
 import nx.remote.RemoteConnection;
 import nx.remote.RemoteConnectionDecorator;
 import nx.remote.StoppableRemoteConnection;
 import one.client.gwt.OneGwt;
 import one.common.One;
 import one.core.domain.BackgroundListener;
 import one.core.dsl.CoreDsl;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.rpc.ServiceDefTarget;
 import com.ononedb.nextweb.OnedbNextwebEngine;
 import com.ononedb.nextweb.common.H;
 import com.ononedb.nextweb.common.OnedbFactory;
 import com.ononedb.nextweb.js.internal.OnedbJsFactory;
 import com.ononedb.nextweb.plugins.DefaultPluginFactory;
 
 import de.mxro.factories.Factories;
 import de.mxro.factories.FactoryCollection;
 import de.mxro.service.ServiceRegistry;
 import de.mxro.service.Services;
 
 /**
  * <p>
  * The onedb implementation of a {@link NextwebEngine}.
  * 
  * @author <a href="http://www.mxro.de">Max Rohde</a>
  *
  */
 public class OnedbNextwebEngineJs implements OnedbNextwebEngine, NextwebEngineJs {
 
     private final CoreDsl dsl;
 
     private final JsFactory jsFactory;
     protected ExceptionManager exceptionManager;
     protected StartServerCapability startServerCapability;
     protected FactoryCollection factories;
     protected ServiceRegistry services;
 
     public static OnedbNextwebEngineJs init() {
         final OnedbNextwebEngineJs engine = new OnedbNextwebEngineJs();
         NextwebJs.injectEngine(JsNextwebEngine.wrap(engine));
         One.setDsl(engine.getDsl());
         return engine;
     }
 
     public static OnedbNextwebEngineJs assertInitialized() {
         if (NextwebJs.getEngine() == null || (!(NextwebJs.getEngine() instanceof JsNextwebEngine))) {
             return init();
         }
 
         return (OnedbNextwebEngineJs) NextwebJs.getEngine().getEngine();
     }
 
     @Override
     public Session createSession() {
 
         final CoreDsl dsl = this.dsl;
 
         return getOnedbFactory().createSession(this, dsl.createClient(), null);
     }
 
     @Override
     public boolean hasPersistedReplicationCapability() {
 
         return true;
     }
 
     @Override
     public Session createSession(final SessionConfiguration configuration) {
         final CoreDsl dsl = this.dsl;
 
         return getOnedbFactory().createSession(this, dsl.createClient(configuration), configuration);
     }
 
     @Override
     public ExceptionManager getExceptionManager() {
 
         return exceptionManager;
     }
 
     @Override
     public OnedbFactory getOnedbFactory() {
         return new OnedbJsFactory();
     }
 
     @Override
     public Factory getFactory() {
         return new OnedbJsFactory();
     }
 
     @Override
     public FactoryCollection factories() {
         return factories;
     }
 
     @Override
     public ServiceRegistry services() {
         return services;
     }
 
     public OnedbNextwebEngineJs() {
         this(null);
     }
 
     /**
      * 
      * @param internalConnection
      *            The connection to be used for all sessions created with this
      *            engine.
      */
     public OnedbNextwebEngineJs(final StoppableRemoteConnection internalConnection) {
         super();
         this.exceptionManager = getOnedbFactory().createExceptionManager(null);
         this.exceptionManager.catchExceptions(new ExceptionListener() {
 
             @Override
             public void onFailure(final ExceptionResult r) {
                 throw new RuntimeException("Unhandled exception: " + r.exception().getMessage() + " from object "
                         + r.origin() + " (" + r.origin().getClass() + ")");
             }
         });
         this.jsFactory = new JsFactory(this);
 
         this.factories = Factories.create();
         this.services = Services.create();
 
         this.dsl = createDsl(internalConnection);
     }
 
     private final CoreDsl createDsl(final StoppableRemoteConnection internalConnection) {
         CoreDsl res;
         assert dsl == null;
 
         final GwtRemoteServiceAsync gwtService = GWT.create(GwtRemoteService.class);
 
         ((ServiceDefTarget) gwtService).setServiceEntryPoint("/servlets/v01/gwtrpc");
 
         res = OneGwt.createDsl(gwtService, "", internalConnection);
 
         res.getDefaults().getSettings().setDefaultBackgroundListener(new BackgroundListener() {
 
             @Override
             public void onBackgroundException(final Object operation, final Throwable t, final Throwable origin) {
 
                 throw new RuntimeException("Uncaught background exception: " + t.getMessage() + " for operation: ["
                         + operation + "] originating from: [" + origin + "]. " + ExceptionUtils.getStacktrace(t)
                         + " Origin Trace: " + ExceptionUtils.getStacktrace(origin), t);
             }
         });
 
         return res;
     }
 
     @Override
     public DefaultPluginFactory plugin() {
         return H.onedbDefaultPluginFactory();
     }
 
     @Override
     public JsFactory jsFactory() {
         return jsFactory;
     }
 
     @Override
     public void runSafe(final Session forSession, final Runnable task) {
         task.run(); // no multi-threading in JS assured.
     }
 
     @Override
     public boolean hasStartServerCapability() {
         return startServerCapability != null;
     }
 
     @Override
     public void injectCapability(final Capability capability) {
         if (capability instanceof StartServerCapability) {
             startServerCapability = (StartServerCapability) capability;
             return;
         }
 
         throw new IllegalArgumentException("This engine cannot recognize the capability: [" + capability.getClass()
                 + "]");
     }
 
     @Override
    public LocalServer startServer(final int port) {
         if (startServerCapability == null) {
             throw new IllegalStateException("Please inject a StartServerCapability first.");
         }
 
        return startServerCapability.startServer(port);
     }
 
     @Override
     public CoreDsl getDsl() {
         return dsl;
     }
 
     @Override
     public void addConnectionDecorator(final RemoteConnectionDecorator decorator) {
         OneGwt.getSettings().addConnectionDecorator(decorator);
     }
 
     @Override
     public void removeConnectionDecorator(final RemoteConnectionDecorator decorator) {
         OneGwt.getSettings().removeConnectionDecorator(decorator);
     }
 
     @Override
     public RemoteConnection createRemoteConnection() {
         return OneGwt.createRemoteConnection();
     }
 
     @Override
     public OnedbNextwebEngine fork(final StoppableRemoteConnection internalConnection) {
         final OnedbNextwebEngineJs forkedEngine = new OnedbNextwebEngineJs(internalConnection);
 
         forkedEngine.factories = factories;
         forkedEngine.services = services;
         forkedEngine.startServerCapability = startServerCapability;
         forkedEngine.exceptionManager = exceptionManager;
 
         return forkedEngine;
     }
 
 }
