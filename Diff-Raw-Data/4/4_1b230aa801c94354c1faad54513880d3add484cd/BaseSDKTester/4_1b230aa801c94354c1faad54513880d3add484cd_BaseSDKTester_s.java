 package com.versionone.om.tests;
 
 import com.versionone.DB.DateTime;
 import com.versionone.Duration;
 import com.versionone.Oid;
 import com.versionone.apiclient.*;
 import com.versionone.om.*;
 import com.versionone.om.TransformIterable.ITransformer;
 import com.versionone.om.filters.EnvironmentFilter;
 
 
 import java.util.UUID;
 
 import java.util.*;
 
 import org.junit.After;
 
 public abstract class BaseSDKTester {
     protected final static String SCOPE_ZERO = "Scope:0";
     protected final static double ESTIMATES_PRECISION = 0.0001;    
     private V1Instance instance;
     private AssetID sandboxProjectID;
     private AssetID sandboxIterationID;
     private AssetID sandboxTeamID;
     private AssetID sandboxMemberID;
     private Oid defaultSchemeOid;
 
     private EntityFactory factory;
 
     protected Oid getDefaultSchemeOid() {
         if (defaultSchemeOid == null) {
             defaultSchemeOid = getFirstAvailableScheme().getOid();
         }
 
         return defaultSchemeOid;
     }
 
     protected String getApplicationUrl() {  
     	// test.websiteurl provided by maven POM. System environment provided by jenkins, junit eclipse config, etc
         String envar =  System.getProperty("test.websiteurl", System.getenv("TEST_URL")); 
        if (envar == null) envar = "http://localhost/VersionOne.SDK.Java.ObjectModel.Tests/";
        if (envar.endsWith("/") == false) envar.concat("/"); //ending slash is important
         return envar;
     }
 
     protected String getUsername() {
         return "admin";
     }
 
     protected String getPassword() {
         return "admin";
     }
 
     protected String getSandboxName() {
         return getClass().getSimpleName();
     }
 
     /**
      * The name to be used when creating your sandbox projects and teams.
      * Override to specify a special name. I like to call mine Fred.
      */
     protected void newSandboxProject() {
         sandboxProjectID = null;
     }
 
     /**
      * @return The ID of your sandbox project, so you can get it again yourself, Elvis.
      */
     protected AssetID getSandboxProjectID() {
         if (sandboxProjectID == null) {
             Project rootProject = getInstance().get().projectByID(SCOPE_ZERO);
             Project sandbox = createSandboxProject(rootProject);
 
             sandboxProjectID = sandbox.getID();
         }
         return sandboxProjectID;
     }
 
     /**
      * Override to create your sandbox with properties other than the defaults
      * (today as the start date, child of Scope:0, no schedule). You go, Einstein.
      *
      * @param rootProject root project
      * @return created sub project
      */
     protected Project createSandboxProject(Project rootProject) {
         Map<String, Object> mandatoryAttributes = new HashMap<String, Object>(1);
         mandatoryAttributes.put("Scheme", getDefaultSchemeOid());
 
         return getInstance().create().project(getSandboxName(), rootProject, DateTime.now(), null, mandatoryAttributes);
     }
 
     /**
      * @return A sandbox for you to play in. The Entity is retrieved from the
      *         Instance on every call (so ResetInstance will force a re-query).
      *         You don't need to do anything to initialize it. Just use it, Mort.
      */
     protected Project getSandboxProject() {
         return getInstance().get().projectByID(getSandboxProjectID());
     }
 
     protected void newSandboxIteration() {
         sandboxIterationID = null;
     }
 
     /**
      * @return The ID of your sandbox iteration, so you can get it again yourself, Elvis.
      */
     protected AssetID getSandboxIterationID() {
         if (sandboxIterationID == null) {
             sandboxIterationID = getSandboxProject().createIteration().getID();
         }
         return sandboxIterationID;
     }
 
     /**
      * @return A sandbox for you to play in. The Entity is retrieved from the
      *         Instance on every call (so ResetInstance will force a re-query).
      *         You don't need to do anything to initialize it. Just use it, Mort.
      */
     protected Iteration getSandboxIteration() {
         return getInstance().get().iterationByID(getSandboxIterationID());
     }
 
     protected void newSandboxTeam() {
         sandboxTeamID = null;
     }
 
     /**
      * @return The ID of your sandbox team, so you can get it again yourself, Elvis.
      */
     protected AssetID getSandboxTeamID() {
         if (sandboxTeamID == null) {
             sandboxTeamID = getInstance().create().team(getSandboxName()).getID();
         }
         return sandboxTeamID;
     }
 
     /**
      * @return A sandbox for you to play in. The Entity is retrieved from the
      *         Instance on every call (so ResetInstance will force a re-query).
      *         You don't need to do anything to initialize it. Just use it, Mort.
      */
     protected Team getSandboxTeam() {
         return getInstance().get().teamByID(getSandboxTeamID());
     }
 
     protected void newSandboxMember() {
         sandboxMemberID = null;
     }
 
     /**
      * @return The ID of your sandbox member, so you can get it again yourself, Elvis.
      */
     protected AssetID getSandboxMemberID() {
         if (sandboxMemberID == null) {
             sandboxMemberID = getInstance().create().member(
                     getSandboxName(), getSandboxName()).getID();
         }
         return sandboxMemberID;
     }
 
     /**
      * @return A sandbox for you to play in. The Entity is retrieved from the
      *         Instance on every call (so ResetInstance will force a re-query).
      *         You don't need to do anything to initialize it. Just use it, Mort.
      */
     protected Member getSandboxMember() {
         return getInstance().get().memberByID(getSandboxMemberID());
     }
 
     protected V1Instance getInstance() {
         if (instance == null) {
             instance = new V1Instance(getApplicationUrl(), getUsername(), getPassword());
             instance.validate();
         }
         return instance;
     }
 
     protected void resetInstance() {
         instance = null;
     }
 
     protected EntityFactory getEntityFactory() {
         if (factory == null) {
             factory = new EntityFactory(getInstance());
         }
 
         return factory;
     }
 
     /**
      * @return the {@code Guid} object.
      */
     public static String newGuid() {
         final UUID guid = UUID.randomUUID();
         return guid.toString();
     }
 
     public static class EntityToNameTransformer<T extends BaseAsset>
             implements ITransformer<T, String> {
         public String transform(T input) {
             return input.getName();
         }
     }
 
     protected static class EntityToAssetIDTransformer<T extends Entity>
             implements ITransformer<T, String> {
         public String transform(T input) {
             return input.getID().getToken();
         }
     }
 
     protected static <T> boolean findRelated(T needle, Collection<T> haystack) {
         boolean found = false;
         for (T straw : haystack) {
             if (straw.equals(needle)) {
                 found = true;
                 break;
             }
         }
         return found;
     }
 
     private AssetID _sandboxScheduleID;
 
     protected void newSandboxSchedule() {
         _sandboxScheduleID = null;
     }
 
     /**
      * @return The ID of your sandbox Schedule, so you can get it again yourself, Elvis.
      */
     protected AssetID getSandboxScheduleID() {
         if (_sandboxScheduleID == null) {
             Schedule sandbox = createSandboxSchedule();
             _sandboxScheduleID = sandbox.getID();
         }
         return _sandboxScheduleID;
     }
 
     /**
      * Override to create your sandbox with properties other than the defaults
      * (today as the start date, child of Scope:0, no schedule).  You go, Einstein.
      *
      * @return Newly created sandbox schedule.
      */
     protected Schedule createSandboxSchedule() {
         return instance.create().schedule(this.getSandboxName(), new Duration(14, Duration.Unit.Days), new Duration(0, Duration.Unit.Days));
     }
 
     /**
      * A sandbox schedule for you to play in.  The Entity is retrieved from the Instance on every call
      * (so ResetInstance will force a re-query).  You don't need to do anything to initialize it.
      * Just use it, Mort.
      *
      * @return sandbox schedule.
      */
     protected Schedule getSandboxSchedule() {
         return instance.get().scheduleByID(getSandboxScheduleID());
     }
 
     protected Story createStory(String name, Project project, Iteration iteration) {
         Story story = getEntityFactory().createStory(name, project);
         story.setIteration(iteration);
         story.save();
         return story;
     }
 
     protected Defect createDefect(String name, Project project, Iteration iteration) {
         Defect defect = getEntityFactory().createDefect(name, project);
         defect.setIteration(iteration);
         defect.save();
         return defect;
     }
 
     protected Environment createEnvironment(String name, Map<String, Object> attributes) {
         return getEntityFactory().createEnvironment(name, getSandboxProject(), attributes);
     }
 
     protected static <T> T first(Collection<T> list) {
         Iterator<T> iter = list.iterator();
         if (iter.hasNext())
             return iter.next();
         return null;
     }
 
     protected static Map<String, Object> createAttributesWithDescription(String entityName) {
         final String description = "Test for " + entityName + " creation with required attributes";
         Map<String, Object> attributes = new HashMap<String, Object>();
         attributes.put("Description", description);
         return attributes;
     }
 
     private Asset getFirstAvailableScheme() {
         IAssetType schemaType = getInstance().getApiClient().getMetaModel().getAssetType("Scheme");
         IAttributeDefinition nameDefinition = schemaType.getAttributeDefinition("Name");
 
         Query schemaQuery = new Query(schemaType);
         schemaQuery.getSelection().add(nameDefinition);
 
         try {
             QueryResult result = getInstance().getApiClient().getServices().retrieve(schemaQuery);
             return result.getAssets()[0];
         } catch (Exception ex) {
             return null;
         }
     }
 
     Environment getEnvironment() {
         String name = "Testing env abv123";
         EnvironmentFilter filter = new EnvironmentFilter();
         filter.name.add(name);
 
         Collection<Environment> env = getInstance().get().environments(filter);
 
         if (env.size() == 0) {
             return createEnvironment(name, null);
         }
 
         List<Environment> environments = new ArrayList<Environment>(env);
         return environments.get(0);
     }
 
     @After
     public void testClassTearDown() {
     	if (factory != null) {
     		factory.dispose();
     	}
     }
 }
