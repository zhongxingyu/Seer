 /*
  * $Id$
  *
  * Copyright 2003-2006 Orange Nederland Breedband B.V.
  * See the COPYRIGHT file for redistribution and use restrictions.
  */
 package org.xins.server;
 
 import java.io.File;
 import java.io.PrintWriter;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.TimeZone;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 
 import org.xins.common.MandatoryArgumentChecker;
 import org.xins.common.Utils;
 import org.xins.common.collections.BasicPropertyReader;
 import org.xins.common.collections.InvalidPropertyValueException;
 import org.xins.common.collections.MissingRequiredPropertyException;
 import org.xins.common.collections.PropertyReader;
 import org.xins.common.io.FastStringWriter;
 import org.xins.common.manageable.BootstrapException;
 import org.xins.common.manageable.DeinitializationException;
 import org.xins.common.manageable.InitializationException;
 import org.xins.common.manageable.Manageable;
 import org.xins.common.net.IPAddressUtils;
 import org.xins.common.spec.APISpec;
 import org.xins.common.spec.InvalidSpecificationException;
 import org.xins.common.text.DateConverter;
 import org.xins.common.text.ParseException;
 import org.xins.common.xml.Element;
 import org.xins.common.xml.ElementBuilder;
 import org.xins.logdoc.LogdocSerializable;
 
 /**
  * Base class for API implementation classes.
  *
  * @version $Revision$ $Date$
  * @author <a href="mailto:ernst.dehaan@orange-ft.com">Ernst de Haan</a>
  * @author <a href="mailto:anthony.goubard@orange-ft.com">Anthony Goubard</a>
  * @author Tauseef Rehman (<a href="mailto:tauseef.rehman@nl.wanadoo.com">tauseef.rehman@nl.wanadoo.com</a>)
  *
  * @since XINS 1.0.0
  */
 public abstract class API
 extends Manageable
 implements DefaultResultCodes {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    /**
     * Fully-qualified name of this class.
     */
    private static final String CLASSNAME = API.class.getName();
 
    /**
     * Successful empty call result.
     */
    private static final FunctionResult SUCCESSFUL_RESULT =
       new FunctionResult();
 
    /**
     * The runtime (initialization) property that defines the ACL (access
     * control list) rules.
     */
    private static final String ACL_PROPERTY = "org.xins.server.acl";
 
    /**
     * The name of the build property that specifies the version of the API.
     */
    static final String API_VERSION_PROPERTY = "org.xins.api.version";
 
    /**
     * The name of the build property that specifies the hostname of the
     * machine the package was built on.
     */
    private static final String BUILD_HOST_PROPERTY =
       "org.xins.api.build.host";
 
    /**
     * The name of the build property that specifies the time the package was
     * built.
     */
    private static final String BUILD_TIME_PROPERTY =
       "org.xins.api.build.time";
 
    /**
     * The name of the build property that specifies which version of XINS was
     * used to build the package.
     */
    private static final String BUILD_XINS_VERSION_PROPERTY =
       "org.xins.api.build.version";
 
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>API</code> object.
     *
     * @param name
     *    the name of the API, cannot be <code>null</code> nor can it be an
     *    empty string.
     *
     * @throws IllegalArgumentException
     *    if <code>name == null
     *          || name.{@link String#length() length()} &lt; 1</code>.
     */
    protected API(String name)
    throws IllegalArgumentException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("name", name);
       if (name.length() < 1) {
          String message = "name.length() == "
                         + name.length();
          throw new IllegalArgumentException(message);
       }
 
       // Initialize fields
       _name                = name;
       _startupTimestamp    = System.currentTimeMillis();
       _lastStatisticsReset = _startupTimestamp;
       _manageableObjects   = new ArrayList(20);
       _functionsByName     = new HashMap(89);
       _functionList        = new ArrayList(80);
       _resultCodesByName   = new HashMap(89);
       _resultCodeList      = new ArrayList(80);
       _emptyProperties     = new RuntimeProperties();
       _timeZone            = TimeZone.getDefault();
       _localIPAddress      = IPAddressUtils.getLocalHostIPAddress();
 
       // Initialize mapping from meta-function to call ID
       _metaFunctionCallIDs = new HashMap(89);
       _metaFunctionCallIDs.put("_NoOp",             new Counter());
       _metaFunctionCallIDs.put("_GetFunctionList",  new Counter());
       _metaFunctionCallIDs.put("_GetStatistics",    new Counter());
       _metaFunctionCallIDs.put("_GetVersion",       new Counter());
       _metaFunctionCallIDs.put("_CheckLinks",       new Counter());
       _metaFunctionCallIDs.put("_GetSettings",      new Counter());
       _metaFunctionCallIDs.put("_DisableFunction",  new Counter());
       _metaFunctionCallIDs.put("_EnableFunction",   new Counter());
       _metaFunctionCallIDs.put("_ResetStatistics",  new Counter());
       _metaFunctionCallIDs.put("_ReloadProperties", new Counter());
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The engine that owns this <code>API</code> object.
     */
    private Engine _engine;
 
    /**
     * The name of this API. Cannot be <code>null</code> and cannot be an empty
     * string.
     */
    private final String _name;
 
    /**
     * List of registered manageable objects. See {@link #add(Manageable)}.
     *
     * <p />This field is initialized to a non-<code>null</code> value by the
     * constructor.
     */
    private final List _manageableObjects;
 
    /**
     * Map that maps function names to <code>Function</code> instances.
     * Contains all functions associated with this API.
     *
     * <p />This field is initialized to a non-<code>null</code> value by the
     * constructor.
     */
    private final Map _functionsByName;
 
    /**
     * List of all functions. This field cannot be <code>null</code>.
     */
    private final List _functionList;
 
    /**
     * Map that maps result code names to <code>ResultCode</code> instances.
     * Contains all result codes associated with this API.
     *
     * <p />This field is initialized to a non-<code>null</code> value by the
     * constructor.
     */
    private final Map _resultCodesByName;
 
    /**
     * List of all result codes. This field cannot be <code>null</code>.
     */
    private final List _resultCodeList;
 
    /**
     * The build-time settings. This field is initialized exactly once by
     * {@link #bootstrap(PropertyReader)}. It can be <code>null</code> before
     * that.
     */
    private PropertyReader _buildSettings;
 
    /**
     * The {@link RuntimeProperties} containing the method to verify and access
     * the defined runtime properties.
     */
    private RuntimeProperties _emptyProperties;
 
    /**
     * The runtime-time settings. This field is initialized by
     * {@link #init(PropertyReader)}. It can be <code>null</code> before that.
     */
    private PropertyReader _runtimeSettings;
 
    /**
     * Timestamp indicating when this API instance was created.
     */
    private final long _startupTimestamp;
 
    /**
     * Last time the statistics were reset. Initially the startup timestamp.
     */
    private long _lastStatisticsReset;
 
    /**
     * Host name for the machine that was used for this build.
     */
    private String _buildHost;
 
    /**
     * Time stamp that indicates when this build was done.
     */
    private String _buildTime;
 
    /**
     * XINS version used to build the web application package.
     */
    private String _buildVersion;
 
    /**
     * The time zone used when generating dates for output.
     */
    private final TimeZone _timeZone;
 
    /**
     * Version of the API.
     */
    private String _apiVersion;
 
    /**
     * The API specific access rule list.
     */
    private AccessRuleList _apiAccessRuleList;
 
    /**
     * The general access rule list.
     */
    private AccessRuleList _accessRuleList;
 
    /**
     * The API specification.
     */
    private APISpec _apiSpecification;
 
    /**
     * The local IP address.
     */
    private String _localIPAddress;
 
    /**
     * Mapping from function name to the call ID for all meta-functions. This
     * field is never <code>null</code>.
     */
    private final HashMap _metaFunctionCallIDs;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Gets the name of this API.
     *
     * @return
     *    the name of this API, never <code>null</code> and never an empty
     *    string.
     */
    public final String getName() {
       return _name;
    }
 
    /**
     * Gets the list of the functions of this API.
     *
     * @return
    *    the functions of this API as a {@link List} of {@link Function} objects, never <code>null</code>.
     *
     * @since XINS 1.5.0.
     */
    public final List getFunctionList() {
       return _functionList;
    }
 
    /**
     * Gets the bootstrap properties specified for the API.
     *
     * @return
     *   the bootstrap properties, cannot be <code>null</code>.
     *
     * @since XINS 1.5.0.
     */
    public PropertyReader getBootstrapProperties() {
       return _buildSettings;
    }
 
    /**
     * Gets the API runtime properties.
     *
     * @return
    *   the runtime properties, cannot be <code>null</code>.
     */
    PropertyReader getRuntimeProperties() {
       return _runtimeSettings;
    }
 
    /**
     * Gets the runtime properties specified in the implementation.
     *
     * @return
     *    the runtime properties for the API, cannot be <code>null</code>.
     */
    public RuntimeProperties getProperties() {
 
       // This method is overridden by the APIImpl to return the generated
       // RuntimeProperties class which contains the runtime properties.
       return _emptyProperties;
    }
 
    /**
     * Gets the timestamp that indicates when this <code>API</code> instance
     * was created.
     *
     * @return
     *    the time this instance was constructed, as a number of milliseconds
     *    since the
     *    <a href="http://en.wikipedia.org/wiki/Unix_Epoch">UNIX Epoch</a>.
     */
    public final long getStartupTimestamp() {
       return _startupTimestamp;
    }
 
    /**
     * Returns the applicable time zone.
     *
     * @return
     *    the time zone, never <code>null</code>.
     */
    public final TimeZone getTimeZone() {
       return _timeZone;
    }
 
    /**
     * Bootstraps this API (wrapper method). This method calls
     * {@link #bootstrapImpl2(PropertyReader)}.
     *
     * @param buildSettings
     *    the build-time configuration properties, not <code>null</code>.
     *
     * @throws IllegalStateException
     *    if this API is currently not bootstraping.
     *
     * @throws MissingRequiredPropertyException
     *    if a required property is not given.
     *
     * @throws InvalidPropertyValueException
     *    if a property has an invalid value.
     *
     * @throws BootstrapException
     *    if the bootstrap fails.
     */
    protected final void bootstrapImpl(PropertyReader buildSettings)
    throws IllegalStateException,
           MissingRequiredPropertyException,
           InvalidPropertyValueException,
           BootstrapException {
 
       // Check state
       Manageable.State state = getState();
       if (state != BOOTSTRAPPING) {
          String message = "State is "
                         + state
                         + " instead of "
                         + BOOTSTRAPPING
                         + '.';
          Utils.logProgrammingError(message);
          throw new IllegalStateException(message);
       }
 
       // Log the time zone
       String tzShortName = _timeZone.getDisplayName(false, TimeZone.SHORT);
       String tzLongName  = _timeZone.getDisplayName(false, TimeZone.LONG);
       Log.log_3404(tzShortName, tzLongName);
 
       // Store the build-time settings
       _buildSettings = buildSettings;
 
       // Get build-time properties
       _apiVersion   = _buildSettings.get(API_VERSION_PROPERTY       );
       _buildHost    = _buildSettings.get(BUILD_HOST_PROPERTY        );
       _buildTime    = _buildSettings.get(BUILD_TIME_PROPERTY        );
       _buildVersion = _buildSettings.get(BUILD_XINS_VERSION_PROPERTY);
 
       Log.log_3212(_buildHost, _buildTime, _buildVersion, _name, _apiVersion);
 
       // Skip check if build version is not set
       if (_buildVersion == null) {
          // fall through
 
       // Check if build version identifies a production release of XINS
       } else if (! Library.isProductionRelease(_buildVersion)) {
          Log.log_3228(_buildVersion);
       }
 
       // Let the subclass perform initialization
       // TODO: What if bootstrapImpl2 throws an unexpected exception?
       bootstrapImpl2(buildSettings);
 
       // Bootstrap all instances
       int count = _manageableObjects.size();
       for (int i = 0; i < count; i++) {
          Manageable m = (Manageable) _manageableObjects.get(i);
          String className = m.getClass().getName();
          Log.log_3213(_name, className);
          try {
             m.bootstrap(_buildSettings);
             Log.log_3214(_name, className);
 
          // Missing property
          } catch (MissingRequiredPropertyException exception) {
             Log.log_3215(_name, className, exception.getPropertyName(),
                          exception.getDetail());
             throw exception;
 
          // Invalid property
          } catch (InvalidPropertyValueException exception) {
             Log.log_3216(_name,
                          className,
                          exception.getPropertyName(),
                          exception.getPropertyValue(),
                          exception.getReason());
             throw exception;
 
          // Catch BootstrapException and any other exceptions not caught
          // by previous catch statements
          } catch (Throwable exception) {
 
             // Log event
             Log.log_3217(exception, _name, className);
 
             // Throw a BootstrapException. If necessary, wrap around the
             // caught exception
             if (exception instanceof BootstrapException) {
                throw (BootstrapException) exception;
             } else {
                throw new BootstrapException(exception);
             }
          }
       }
 
       // Bootstrap all functions
       count = _functionList.size();
       for (int i = 0; i < count; i++) {
          Function f = (Function) _functionList.get(i);
          String functionName = f.getName();
          Log.log_3220(_name, functionName);
          try {
             f.bootstrap(_buildSettings);
             Log.log_3221(_name, functionName);
 
          // Missing required property
          } catch (MissingRequiredPropertyException exception) {
             Log.log_3222(_name, functionName, exception.getPropertyName(),
                          exception.getDetail());
             throw exception;
 
          // Invalid property value
          } catch (InvalidPropertyValueException exception) {
             Log.log_3223(_name,
                          functionName,
                          exception.getPropertyName(),
                          exception.getPropertyValue(),
                          exception.getReason());
             throw exception;
 
          // Catch BootstrapException and any other exceptions not caught
          // by previous catch statements
          } catch (Throwable exception) {
 
             // Log this event
             Log.log_3224(exception, _name, functionName);
 
             // Throw a BootstrapException. If necessary, wrap around the
             // caught exception
             if (exception instanceof BootstrapException) {
                throw (BootstrapException) exception;
             } else {
                throw new BootstrapException(exception);
             }
          }
       }
    }
 
    /**
     * Bootstraps this API (implementation method).
     *
     * <p />The implementation of this method in class {@link API} is empty.
     * Custom subclasses can perform any necessary bootstrapping in this
     * class.
     *
     * <p />Note that bootstrapping and initialization are different. Bootstrap
     * includes only the one-time configuration of the API based on the
     * build-time settings, while the initialization
     *
     * <p />The {@link #add(Manageable)} may be called from this method,
     * and from this method <em>only</em>.
     *
     * @param buildSettings
     *    the build-time properties, guaranteed not to be <code>null</code>.
     *
     * @throws MissingRequiredPropertyException
     *    if a required property is not given.
     *
     * @throws InvalidPropertyValueException
     *    if a property has an invalid value.
     *
     * @throws BootstrapException
     *    if the bootstrap fails.
     */
    protected void bootstrapImpl2(PropertyReader buildSettings)
    throws MissingRequiredPropertyException,
           InvalidPropertyValueException,
           BootstrapException {
       // empty
    }
 
    /**
     * Stores a reference to the <code>Engine</code> that owns this
     * <code>API</code> object.
     *
     * @param engine
     *    the {@link Engine} instance, should not be <code>null</code>.
     */
    void setEngine(Engine engine) {
       _engine = engine;
    }
 
    /**
     * Triggers re-initialization of this API. This method is meant to be
     * called by API function implementations when it is anticipated that the
     * API should be re-initialized.
     */
    protected final void reinitializeImpl() {
       _engine.initAPI();
    }
 
    /**
     * Initializes this API.
     *
     * @param runtimeSettings
     *    the runtime configuration settings, cannot be <code>null</code>.
     *
     * @throws MissingRequiredPropertyException
     *    if a required property is missing.
     *
     * @throws InvalidPropertyValueException
     *    if a property has an invalid value.
     *
     * @throws InitializationException
     *    if the initialization failed for some other reason.
     *
     * @throws IllegalStateException
     *    if this API is currently not initializing.
     */
    protected final void initImpl(PropertyReader runtimeSettings)
    throws MissingRequiredPropertyException,
           InvalidPropertyValueException,
           InitializationException,
           IllegalStateException {
 
       Log.log_3405(_name);
 
       // Store runtime settings
       _runtimeSettings = runtimeSettings;
 
       // TODO: Investigate whether we can take the configuration file reload
       //       interval from somewhere (ConfigManager? Engine?).
       String propName  = APIServlet.CONFIG_RELOAD_INTERVAL_PROPERTY;
       String propValue = runtimeSettings.get(propName);
       int interval = APIServlet.DEFAULT_CONFIG_RELOAD_INTERVAL;
       if (propValue != null && propValue.trim().length() > 0) {
          try {
             interval = Integer.parseInt(propValue);
          } catch (NumberFormatException e) {
             String detail = "Invalid interval. Must be a non-negative integer"
                           + " number (32-bit signed).";
             throw new InvalidPropertyValueException(propName, propValue,
                                                     detail);
          }
 
          if (interval < 0) {
             throw new InvalidPropertyValueException(propName, propValue,
                "Negative interval not allowed. Use 0 to disable reloading.");
          }
       }
 
       // Initialize ACL subsystem
 
       // First with the API specific access rule list
       if (_apiAccessRuleList != null) {
          _apiAccessRuleList.dispose();
       }
       _apiAccessRuleList = createAccessRuleList(runtimeSettings, ACL_PROPERTY + '.' + _name, interval);
 
       // Then read the generic access rule list
       if (_accessRuleList != null) {
          _accessRuleList.dispose();
       }
       _accessRuleList = createAccessRuleList(runtimeSettings, ACL_PROPERTY, interval);
 
       // Initialize the RuntimeProperties object.
       getProperties().init(runtimeSettings);
 
       // Initialize all instances
       int count = _manageableObjects.size();
       for (int i = 0; i < count; i++) {
          Manageable m = (Manageable) _manageableObjects.get(i);
          String className = m.getClass().getName();
          Log.log_3416(_name, className);
          try {
             m.init(runtimeSettings);
             Log.log_3417(_name, className);
 
          // Missing required property
          } catch (MissingRequiredPropertyException exception) {
             Log.log_3418(_name, className, exception.getPropertyName(),
                          exception.getDetail());
             throw exception;
 
          // Invalid property value
          } catch (InvalidPropertyValueException exception) {
             Log.log_3419(_name,
                          className,
                          exception.getPropertyName(),
                          exception.getPropertyValue(),
                          exception.getReason());
             throw exception;
 
          // Catch InitializationException and any other exceptions not caught
          // by previous catch statements
          } catch (Throwable exception) {
 
             // Log this event
             Log.log_3420(exception, _name, className);
             if (exception instanceof InitializationException) {
                throw (InitializationException) exception;
             } else {
                throw new InitializationException(exception);
             }
          }
       }
 
       // Initialize all functions
       count = _functionList.size();
       for (int i = 0; i < count; i++) {
          Function f = (Function) _functionList.get(i);
          String functionName = f.getName();
          Log.log_3421(_name, functionName);
          try {
             f.init(runtimeSettings);
             Log.log_3422(_name, functionName);
 
          // Missing required property
          } catch (MissingRequiredPropertyException exception) {
             Log.log_3423(_name, functionName, exception.getPropertyName(),
                          exception.getDetail());
             throw exception;
 
          // Invalid property value
          } catch (InvalidPropertyValueException exception) {
             Log.log_3424(_name,
                          functionName,
                          exception.getPropertyName(),
                          exception.getPropertyValue(),
                          exception.getReason());
             throw exception;
 
          // Catch InitializationException and any other exceptions not caught
          // by previous catch statements
          } catch (Throwable exception) {
 
             // Log this event
             Log.log_3425(exception, _name, functionName);
 
             // Throw an InitializationException. If necessary, wrap around the
             // caught exception
             if (exception instanceof InitializationException) {
                throw (InitializationException) exception;
             } else {
                throw new InitializationException(exception);
             }
          }
       }
 
       // TODO: Call initImpl2(PropertyReader) ?
 
       Log.log_3406(_name);
    }
 
    /**
     * Creates the access rule list for the given property.
     *
     * @param runtimeSettings
     *    the runtime properties, never <code>null</code>.
     *
     * @param aclProperty
     *    the ACL property, never <code>null</code>
     *
     * @param interval
     *    the interval in seconds to chack if the ACL file has changed and
     *    should be reloaded.
     *
     * @return
     *    the access rule list created from the property value, never <code>null</code>.
     *
     * @throws InvalidPropertyValueException
     *    if the value for the property is invalid.
     */
    private AccessRuleList createAccessRuleList(PropertyReader runtimeSettings,
          String aclProperty, int interval)
    throws InvalidPropertyValueException {
       String acl = runtimeSettings.get(aclProperty);
 
       // New access control list is empty
       if (acl == null || acl.trim().length() < 1) {
          if (aclProperty.equals(ACL_PROPERTY)) {
             Log.log_3426(aclProperty);
          }
          return AccessRuleList.EMPTY;
 
       // New access control list is non-empty
       } else {
 
          // Parse the new ACL
          try {
             AccessRuleList accessRuleList =
                AccessRuleList.parseAccessRuleList(acl, interval);
             int ruleCount = accessRuleList.getRuleCount();
             Log.log_3427(ruleCount);
             return accessRuleList;
 
          // Parsing failed
          } catch (ParseException exception) {
             String exceptionMessage = exception.getMessage();
             Log.log_3428(aclProperty, acl, exceptionMessage);
             throw new InvalidPropertyValueException(aclProperty,
                                                     acl,
                                                     exceptionMessage);
          }
       }
    }
 
    /**
     * Adds the specified manageable object. It will not immediately be
     * bootstrapped and initialized.
     *
     * @param m
     *    the manageable object to add, not <code>null</code>.
     *
     * @throws IllegalStateException
     *    if this API is currently not bootstrapping.
     *
     * @throws IllegalArgumentException
     *    if <code>instance == null</code>.
     */
    protected final void add(Manageable m)
    throws IllegalStateException,
           IllegalArgumentException {
 
       // Check state
       Manageable.State state = getState();
       if (state != BOOTSTRAPPING) {
          String message = "State is "
                         + state
                         + " instead of "
                         + BOOTSTRAPPING
                         + '.';
          Utils.logProgrammingError(message);
          throw new IllegalStateException(message);
       }
 
       // Check preconditions
       MandatoryArgumentChecker.check("m", m);
       String className = m.getClass().getName();
 
       Log.log_3218(_name, className);
 
       // Store the manageable object in the list
       _manageableObjects.add(m);
    }
 
    /**
     * Performs shutdown of this XINS API. This method will never throw any
     * exception.
     */
    protected final void deinitImpl() {
 
       // Deinitialize instances
       int count = _manageableObjects.size();
       for (int i = 0; i < count; i++) {
          Manageable m = (Manageable) _manageableObjects.get(i);
 
          String className = m.getClass().getName();
 
          Log.log_3603(_name, className);
          try {
             m.deinit();
             Log.log_3604(_name, className);
          } catch (DeinitializationException exception) {
             Log.log_3605(_name, className, exception.getMessage());
          } catch (Throwable exception) {
             Log.log_3606(exception, _name, className);
          }
       }
       _manageableObjects.clear();
 
       // Deinitialize functions
       count = _functionList.size();
       for (int i = 0; i < count; i++) {
          Function f = (Function) _functionList.get(i);
 
          String functionName = f.getName();
 
          Log.log_3607(_name, functionName);
          try {
             f.deinit();
             Log.log_3608(_name, functionName);
          } catch (DeinitializationException exception) {
             Log.log_3609(_name, functionName, exception.getMessage());
          } catch (Throwable exception) {
             Log.log_3610(exception, _name, functionName);
          }
       }
    }
 
    /**
     * Callback method invoked when a function is constructed.
     *
     * @param function
     *    the function that is added, not <code>null</code>.
     *
     * @throws NullPointerException
     *    if <code>function == null</code>.
     *
     * @throws IllegalStateException
     *    if this API state is incorrect.
     */
    final void functionAdded(Function function)
    throws NullPointerException, IllegalStateException {
 
       // Check state
       Manageable.State state = getState();
       if (state != UNUSABLE) {
          String message = "State is "
                         + state
                         + " instead of "
                         + UNUSABLE
                         + '.';
          Utils.logProgrammingError(message);
          throw new IllegalStateException(message);
       }
 
       _functionsByName.put(function.getName(), function);
       _functionList.add(function);
    }
 
    /**
     * Callback method invoked when a result code is constructed.
     *
     * @param resultCode
     *    the result code that is added, not <code>null</code>.
     *
     * @throws NullPointerException
     *    if <code>resultCode == null</code>.
     */
    final void resultCodeAdded(ResultCode resultCode)
    throws NullPointerException {
       _resultCodesByName.put(resultCode.getName(), resultCode);
       _resultCodeList.add(resultCode);
    }
 
    /**
     * Returns the function with the specified name.
     *
     * @param name
     *    the name of the function, will not be checked if it is
     *    <code>null</code>.
     *
     * @return
     *    the function with the specified name, or <code>null</code> if there
     *    is no match.
     */
    final Function getFunction(String name) {
       return (Function) _functionsByName.get(name);
    }
 
    /**
     * Get the specification of the API.
     *
     * @return
     *    the {@link APISpec} specification object, never <code>null</code>.
     *
     * @throws InvalidSpecificationException
     *    if the specification cannot be found or is invalid.
     *
     * @since XINS 1.3.0
     */
    public final APISpec getAPISpecification()
    throws InvalidSpecificationException {
 
       if (_apiSpecification == null) {
          String baseURL = null;
          ServletConfig  config  = _engine.getServletConfig();
          ServletContext context = config.getServletContext();
          try {
             String realPath = context.getRealPath("specs/");
             if (realPath != null) {
                baseURL = new File(realPath).toURL().toExternalForm();
             } else {
                baseURL = context.getResource("specs/").toExternalForm();
             }
          } catch (MalformedURLException muex) {
             // Let the base URL be null
          }
          _apiSpecification = new APISpec(getClass(), baseURL);
       }
       return _apiSpecification;
    }
 
    /**
     * Determines if the specified IP address is allowed to access the
     * specified function, returning a <code>boolean</code> value.
     *
     * <p>This method finds the first matching rule and then returns the
     * <em>allow</em> property of that rule (see
     * {@link AccessRule#isAllowRule()}). If there is no matching rule, then
     * <code>false</code> is returned.
     *
     * @param ip
     *    the IP address, cannot be <code>null</code>.
     *
     * @param functionName
     *    the name of the function, cannot be <code>null</code>.
     *
     * @return
     *    <code>true</code> if the request is allowed, <code>false</code> if
     *    the request is denied.
     *
     * @throws IllegalArgumentException
     *    if <code>ip == null || functionName == null</code>.
     */
    public boolean allow(String ip, String functionName)
    throws IllegalArgumentException {
 
       // If no property is defined only localhost is allowed
       if (_apiAccessRuleList == AccessRuleList.EMPTY &&
           _accessRuleList == AccessRuleList.EMPTY &&
           (ip.equals("127.0.0.1") || ip.equals(_localIPAddress))) {
          return true;
       }
 
       // Match an access rule
       Boolean allowed;
       try {
 
          // First check with the API specific one, then use the generic one.
          allowed = _apiAccessRuleList.isAllowed(ip, functionName);
          if (allowed == null) {
             allowed = _accessRuleList.isAllowed(ip, functionName);
          }
 
       // If the IP address cannot be parsed there is a programming error
       // somewhere
       } catch (ParseException exception) {
          String thisMethod    = "allow(java.lang.String,java.lang.String)";
          String subjectClass  = _accessRuleList.getClass().getName();
          String subjectMethod = "allow(java.lang.String,java.lang.String)";
          String detail        = "Malformed IP address: \"" + ip + "\".";
          throw Utils.logProgrammingError(CLASSNAME,    thisMethod,
                                          subjectClass, subjectMethod,
                                          detail,       exception);
       }
 
       // If there is a match, return the allow-indication
       if (allowed != null) {
          return allowed.booleanValue();
       }
 
       // No matching access rule match, do not allow
       Log.log_3553(ip, functionName);
       return false;
    }
 
    /**
     * Forwards a call to a function, using an IP address. The call will
     * actually be handled by
     * {@link Function#handleCall(long,FunctionRequest,String)}.
     *
     * @param start
     *    the start time of the request, in milliseconds since the
     *    <a href="http://en.wikipedia.org/wiki/Unix_Epoch">UNIX Epoch</a>.
     *
     * @param functionRequest
     *    the function request, never <code>null</code>.
     *
     * @param ip
     *    the remote IP address, never <code>null</code>.
     *
     * @return
     *    the result of the call, never <code>null</code>.
     *
     * @throws IllegalStateException
     *    if this object is currently not initialized.
     *
     * @throws NullPointerException
     *    if <code>functionRequest == null</code>.
     *
     * @throws NoSuchFunctionException
     *    if there is no matching function for the specified request.
     *
     * @throws AccessDeniedException
     *    if access is denied for the specified combination of IP address and
     *    function name.
     */
    final FunctionResult handleCall(long               start,
                                    FunctionRequest    functionRequest,
                                    String             ip)
    throws IllegalStateException,
           NullPointerException,
           NoSuchFunctionException,
           AccessDeniedException {
 
       // Check state first
       assertUsable();
 
       // Determine the function name
       String functionName = functionRequest.getFunctionName();
 
       // Check the access rule list
       boolean allow = allow(ip, functionName);
       if (! allow) {
          throw new AccessDeniedException(ip, functionName);
       }
 
       // Handle meta-functions
       FunctionResult result;
       if (functionName.charAt(0) == '_') {
 
          // Determine the call ID
          int callID;
          synchronized (_metaFunctionCallIDs) {
             Counter counter = (Counter) _metaFunctionCallIDs.get(functionName);
             if (counter == null) {
                throw new NoSuchFunctionException(functionName);
             } else {
                callID = counter.next();
             }
          }
 
          // Call the meta-function
          try {
             result = callMetaFunction(functionName, functionRequest);
          } catch (Throwable exception) {
             result = handleFunctionException(start, functionRequest, ip,
                                              callID, exception);
          }
 
          // Determine duration
          long duration = System.currentTimeMillis() - start;
 
          // Determine error code, fallback is a zero character
          String code = result.getErrorCode();
          if (code == null || code.length() < 1) {
             code = "0";
          }
 
          // Prepare for transaction logging
          LogdocSerializable serStart = new FormattedDate(start);
          LogdocSerializable inParams =
             new FormattedParameters(functionRequest.getParameters(), functionRequest.getDataElement());
          LogdocSerializable outParams =
             new FormattedParameters(result.getParameters(), result.getDataElement());
 
          // Log transaction before returning the result
          Log.log_3540(serStart, ip, functionName, duration, code, inParams,
                       outParams);
          Log.log_3541(serStart, ip, functionName, duration, code);
 
       // Handle normal functions
       } else {
          Function function = getFunction(functionName);
          if (function == null)  {
             throw new NoSuchFunctionException(functionName);
          }
          result = function.handleCall(start, functionRequest, ip);
       }
       return result;
    }
 
    /**
     * Handles a call to a meta-function.
     *
     * @param functionName
     *    the name of the meta-function, cannot be <code>null</code> and must
     *    start with the underscore character <code>'_'</code>.
     *
     * @param functionRequest
     *    the function request, never <code>null</code>.
     *
     * @return
     *    the result of the function call, never <code>null</code>.
     *
     * @throws NoSuchFunctionException
     *    if there is no meta-function by the specified name.
     */
    private FunctionResult callMetaFunction(String          functionName,
                                            FunctionRequest functionRequest)
    throws NoSuchFunctionException {
 
       // Check preconditions
       MandatoryArgumentChecker.check("functionName", functionName);
       if (functionName.length() < 1) {
          throw new IllegalArgumentException("functionName.length() < 1");
       } else if (functionName.charAt(0) != '_') {
          throw new IllegalArgumentException("Function name \"" +
                                             functionName +
                                             "\" is not a meta-function.");
       }
 
       FunctionResult result;
 
       // No Operation
       if ("_NoOp".equals(functionName)) {
          result = SUCCESSFUL_RESULT;
 
       // Retrieve function list
       } else if ("_GetFunctionList".equals(functionName)) {
          result = doGetFunctionList();
 
       // Get function call quantity and performance statistics
       } else if ("_GetStatistics".equals(functionName)) {
 
          // Determine value of 'detailed' argument
          String detailedArg = functionRequest.getParameters().get("detailed");
          boolean detailed   = "true".equals(detailedArg);
 
          // Get the statistics
          result = doGetStatistics(detailed);
 
          // Determine value of 'reset' argument
          String resetArg = functionRequest.getParameters().get("reset");
          boolean reset   = "true".equals(resetArg);
          if (reset) {
             doResetStatistics();
          }
 
       // Get version information
       } else if ("_GetVersion".equals(functionName)) {
          result = doGetVersion();
 
       // Check links to underlying systems
       } else if ("_CheckLinks".equals(functionName)) {
          result = doCheckLinks();
 
       // Retrieve configuration settings
       } else if ("_GetSettings".equals(functionName)) {
          result = doGetSettings();
 
       // Disable a function
       } else if ("_DisableFunction".equals(functionName)) {
          String disabledFunction = functionRequest.getParameters().get("functionName");
          result = doDisableFunction(disabledFunction);
 
       // Enable a function
       } else if ("_EnableFunction".equals(functionName)) {
          String enabledFunction = functionRequest.getParameters().get("functionName");
          result = doEnableFunction(enabledFunction);
 
       // Reset the statistics
       } else if ("_ResetStatistics".equals(functionName)) {
          result = doResetStatistics();
 
       // Reload the runtime properties
       } else if ("_ReloadProperties".equals(functionName)) {
          _engine.reloadPropertiesIfChanged();
          result = SUCCESSFUL_RESULT;
 
       // Retrieve eggs
       } else if ("_IWantTheEasterEggs".equals(functionName)) {
          result = SUCCESSFUL_RESULT;
 
       // Meta-function does not exist
       } else {
          throw new NoSuchFunctionException(functionName);
       }
 
       return result;
    }
 
    /**
     * Handles an exception caught while a function was executed.
     *
     * @param start
     *    the start time of the call, as milliseconds since the
     *    <a href="http://en.wikipedia.org/wiki/Unix_Epoch">UNIX Epoch</a>.
     *
     * @param functionRequest
     *    the request, never <code>null</code>.
     *
     * @param ip
     *    the IP address of the requester, never <code>null</code>.
     *
     * @param callID
     *    the call identifier, never <code>null</code>.
     *
     * @param exception
     *    the exception caught, never <code>null</code>.
     *
     * @return
     *    the call result, never <code>null</code>.
     */
    FunctionResult handleFunctionException(long            start,
                                           FunctionRequest functionRequest,
                                           String          ip,
                                           int             callID,
                                           Throwable       exception) {
 
       Log.log_3500(exception, _name, callID);
 
       // Create a set of parameters for the result
       BasicPropertyReader resultParams = new BasicPropertyReader();
 
       // Add the exception class
       String exceptionClass = exception.getClass().getName();
       resultParams.set("_exception.class", exceptionClass);
 
       // Add the exception message, if any
       String exceptionMessage = exception.getMessage();
       if (exceptionMessage != null) {
          exceptionMessage = exceptionMessage.trim();
          if (exceptionMessage.length() > 0) {
             resultParams.set("_exception.message", exceptionMessage);
          }
       }
 
       // Add the stack trace, if any
       FastStringWriter stWriter = new FastStringWriter(360);
       PrintWriter printWriter = new PrintWriter(stWriter);
       exception.printStackTrace(printWriter);
       String stackTrace = stWriter.toString();
       stackTrace = stackTrace.trim();
       if (stackTrace.length() > 0) {
          resultParams.set("_exception.stacktrace", stackTrace);
       }
 
       return new FunctionResult("_InternalError", resultParams);
    }
 
    /**
     * Returns a list of all functions in this API. Per function the name and
     * the version are returned.
     *
     * @return
     *    the call result, never <code>null</code>.
     */
    private final FunctionResult doGetFunctionList() {
 
       // Initialize a builder
       FunctionResult builder = new FunctionResult();
 
       // Loop over all functions
       int count = _functionList.size();
       for (int i = 0; i < count; i++) {
 
          // Get some details about the function
          Function function = (Function) _functionList.get(i);
          String name    = function.getName();
          String version = function.getVersion();
          String enabled = function.isEnabled()
                         ? "true"
                         : "false";
 
          // Add an element describing the function
          ElementBuilder functionElem = new ElementBuilder("function");
          functionElem.setAttribute("name",    name   );
          functionElem.setAttribute("version", version);
          functionElem.setAttribute("enabled", enabled);
          builder.add(functionElem.createElement());
       }
 
       return builder;
    }
 
    /**
     * Returns the call statistics for all functions in this API.
     *
     * @param detailed
     *    If <code>true</code>, the unsuccessful result will be returned sorted
     *    per error code. Otherwise the unsuccessful result won't be displayed
     *    by error code.
     *
     * @return
     *    the call result, never <code>null</code>.
     */
    private final FunctionResult doGetStatistics(boolean detailed) {
 
       // Initialize a builder
       FunctionResult builder = new FunctionResult();
 
       builder.param("startup",   DateConverter.toDateString(_timeZone, _startupTimestamp));
       builder.param("lastReset", DateConverter.toDateString(_timeZone, _lastStatisticsReset));
       builder.param("now",       DateConverter.toDateString(_timeZone, System.currentTimeMillis()));
 
       // Currently available processors
       Runtime rt = Runtime.getRuntime();
       try {
          builder.param("availableProcessors",
                        String.valueOf(rt.availableProcessors()));
       } catch (NoSuchMethodError error) {
          // NOTE: Runtime.availableProcessors() is not available in Java 1.3
       }
 
       // Heap memory statistics
       ElementBuilder heap = new ElementBuilder("heap");
       long free  = rt.freeMemory();
       long total = rt.totalMemory();
       heap.setAttribute("used",  String.valueOf(total - free));
       heap.setAttribute("free",  String.valueOf(free));
       heap.setAttribute("total", String.valueOf(total));
       try {
          heap.setAttribute("max", String.valueOf(rt.maxMemory()));
       } catch (NoSuchMethodError error) {
          // NOTE: Runtime.maxMemory() is not available in Java 1.3
       }
       builder.add(heap.createElement());
 
       // Function-specific statistics
       int count = _functionList.size();
       for (int i = 0; i < count; i++) {
          Function function = (Function) _functionList.get(i);
          FunctionStatistics stats = function.getStatistics();
 
          ElementBuilder functionElem = new ElementBuilder("function");
          functionElem.setAttribute("name", function.getName());
 
          // Successful
          Element successful = stats.getSuccessfulElement();
          functionElem.addChild(successful);
 
          // Unsuccessful
          Element[] unsuccessful = stats.getUnsuccessfulElement(detailed);
          for(int j = 0; j < unsuccessful.length; j++) {
             functionElem.addChild(unsuccessful[j]);
          }
 
          builder.add(functionElem.createElement());
       }
 
       return builder;
    }
 
    /**
     * Returns the XINS version.
     *
     * @return
     *    the call result, never <code>null</code>.
     */
    private final FunctionResult doGetVersion() {
 
       FunctionResult builder = new FunctionResult();
 
       builder.param("java.version",   System.getProperty("java.version"));
       builder.param("xmlenc.version", org.znerd.xmlenc.Library.getVersion());
       builder.param("xins.version",   Library.getVersion());
       builder.param("api.version",    _apiVersion);
 
       return builder;
    }
 
    /**
     * Returns the links in linked system components. It uses the
     * {@link CheckLinks} to connect to each link and builds a
     * {@link FunctionResult} which will have the total link count and total
     * link failures.
     *
     * @return
     *    the call result, never <code>null</code>.
     */
    private final FunctionResult doCheckLinks() {
       return CheckLinks.checkLinks(getProperties().descriptors());
    }
 
    /**
     * Returns the settings.
     *
     * @return
     *    the call result, never <code>null</code>.
     */
    private final FunctionResult doGetSettings() {
 
       FunctionResult builder = new FunctionResult();
 
       // Build settings
       Iterator names = _buildSettings.getNames();
       ElementBuilder build = new ElementBuilder("build");
       while (names.hasNext()) {
          String key   = (String) names.next();
          String value = _buildSettings.get(key);
 
          ElementBuilder property = new ElementBuilder("property");
          property.setAttribute("name", key);
          property.setText(value);
          build.addChild(property.createElement());
       }
       builder.add(build.createElement());
 
       // Runtime settings
       names = _runtimeSettings.getNames();
       ElementBuilder runtime = new ElementBuilder("runtime");
       while (names.hasNext()) {
          String key   = (String) names.next();
          String value = _runtimeSettings.get(key);
 
          ElementBuilder property = new ElementBuilder("property");
          property.setAttribute("name", key);
          property.setText(value);
          runtime.addChild(property.createElement());
       }
       builder.add(runtime.createElement());
 
       // System properties
       Properties sysProps;
       try {
          sysProps = System.getProperties();
       } catch (SecurityException ex) {
          final String THIS_METHOD  = "doGetSettings()";
          final String SUBJECT_CLASS  = "java.lang.System";
          final String SUBJECT_METHOD = "getProperties()";
          Utils.logProgrammingError(CLASSNAME,     THIS_METHOD,
                                    SUBJECT_CLASS, SUBJECT_METHOD,
                                    null,          ex);
          sysProps = new Properties();
       }
 
       Enumeration e = sysProps.propertyNames();
       ElementBuilder system = new ElementBuilder("system");
       while (e.hasMoreElements()) {
          String key   = (String) e.nextElement();
          String value = sysProps.getProperty(key);
 
          if (  key != null &&   key.trim().length() > 0
           && value != null && value.trim().length() > 0) {
             ElementBuilder property = new ElementBuilder("property");
             property.setAttribute("name", key);
             property.setText(value);
             system.addChild(property.createElement());
          }
       }
       builder.add(system.createElement());
 
       return builder;
    }
 
    /**
     * Enables a function.
     *
     * @param functionName
     *    the name of the function to disable, can be <code>null</code>.
     *
     * @return
     *    the call result, never <code>null</code>.
     */
    private final FunctionResult doEnableFunction(String functionName) {
 
       // Get the name of the function to enable
       if (functionName == null || functionName.length() < 1) {
          InvalidRequestResult invalidRequest = new InvalidRequestResult();
          invalidRequest.addMissingParameter("functionName");
          return invalidRequest;
       }
 
       // Get the Function object
       Function function = getFunction(functionName);
       if (function == null) {
          return new InvalidRequestResult();
       }
 
       // Enable or disable the function
       function.setEnabled(true);
 
       return SUCCESSFUL_RESULT;
    }
 
    /**
     * Disables a function.
     *
     * @param functionName
     *    the name of the function to disable, can be <code>null</code>.
     *
     * @return
     *    the call result, never <code>null</code>.
     */
    private final FunctionResult doDisableFunction(String functionName) {
 
       // Get the name of the function to disable
       if (functionName == null || functionName.length() < 1) {
          InvalidRequestResult invalidRequest = new InvalidRequestResult();
          invalidRequest.addMissingParameter("functionName");
          return invalidRequest;
       }
 
       // Get the Function object
       Function function = getFunction(functionName);
       if (function == null) {
          return new InvalidRequestResult();
       }
 
       // Enable or disable the function
       function.setEnabled(false);
 
       return SUCCESSFUL_RESULT;
    }
 
    /**
     * Resets the statistics.
     *
     * @return
     *    the call result, never <code>null</code>.
     */
    private final FunctionResult doResetStatistics() {
 
       // Remember when we last reset the statistics
       _lastStatisticsReset = System.currentTimeMillis();
 
       // Function-specific statistics
       int count = _functionList.size();
       for (int i = 0; i < count; i++) {
          Function function = (Function) _functionList.get(i);
          function.getStatistics().resetStatistics();
       }
       return SUCCESSFUL_RESULT;
    }
 
 
    //-------------------------------------------------------------------------
    // Inner classes
    //-------------------------------------------------------------------------
 
    /**
     * Thread-safe <code>int</code> counter.
     *
     * @version $Revision$ $Date$
     * @author <a href="mailto:ernst.dehaan@orange-ft.com">Ernst de Haan</a>
     */
    private static final class Counter extends Object {
 
       //----------------------------------------------------------------------
       // Constructors
       //----------------------------------------------------------------------
 
       /**
        * Constructs a new <code>Counter</code> that initially returns the
        * value <code>0</code>.
        */
       private Counter() {
          // empty
       }
 
 
       //----------------------------------------------------------------------
       // Fields
       //----------------------------------------------------------------------
 
       /**
        * The wrapped <code>int</code> number. Initially <code>0</code>.
        */
       private int _value;
 
 
       //----------------------------------------------------------------------
       // Methods
       //----------------------------------------------------------------------
 
       /**
        * Retrieves the next value. The first time <code>0</code> is returned,
        * the second time <code>1</code>, etc.
        *
        * @return
        *    the next sequence number.
        */
       private synchronized int next() {
          return _value++;
       }
    }
 }
