 /*
  * 
  */
 package com.demo;
 
 import com.demo.jaxb.AuthenticationPolicies;
 import com.demo.jaxb.AuthenticationPolicy;
 import com.demo.jaxb.AuthorizationPolicies;
 import com.demo.jaxb.AuthorizationPolicy;
 import com.demo.jaxb.AuthorizationPolicy.Conditions;
 import com.demo.jaxb.CombinerType;
 import com.demo.jaxb.Identity;
 import com.demo.jaxb.IdentityCondition;
 import com.demo.jaxb.IdentityCondition.Identities;
 import com.demo.jaxb.IdentityType;
 import com.demo.jaxb.ObjectFactory;
 import com.demo.jaxb.Resource;
 import com.demo.jaxb.ResourceProtectionLevel;
 import com.demo.jaxb.Rule;
 import com.demo.jaxb.RuleCombiner;
 import com.demo.jaxb.RuleConditionCombiner;
 import com.demo.jaxb.RuleEffect;
 import com.demo.jaxb.SimpleCombiner;
 import com.demo.jaxb.SuccessResponses;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.config.ClientConfig;
 import com.sun.jersey.api.client.config.DefaultClientConfig;
 import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
 import com.sun.jersey.api.client.filter.LoggingFilter;
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.ws.rs.core.MediaType;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.JAXBException;
 
 /**
  * Demo App that shows how to use OAM's Policy Management REST APIs
  *
  * There is almost zero error checking here. This is for illustrative purposes.
  * YMMV
  *
  * @author warren.strange@oracle.com
  */
 public class OAMPolicyManager {
 
     // constants for our demo.
     public static final String DATASTORE = "OUD"; // OAM datastore used for policies
     public static final String HOSTIDENTIFIER = "ohs1";
     // Create this policy in OAM before the your run the demo
     public static final String DEFAULT_AUTHN_POLICY = "DefaultAuthenticationPolicy";
     Client client;
     WebResource base;
     String appDomain;
     ObjectFactory objFactory = new ObjectFactory(); // JAXB Obj factory
     JAXBContext jaxb;
 
     public OAMPolicyManager(String rootURL, String appDomain, String username, String password) {
         this.appDomain = appDomain;
 
         ClientConfig config = new DefaultClientConfig();
         client = Client.create(config);
 
         HTTPBasicAuthFilter authFilter = new HTTPBasicAuthFilter(username, password);
         client.addFilter(authFilter);
         client.addFilter(new LoggingFilter());
 
         base = client.resource(rootURL).
                 path("/oam/services/rest/11.1.2.0.0/ssa/policyadmin").
                 queryParam("appdomain", appDomain);
         try {
             // classloader tricks needed for Weblogic to find the jaxb stuff
 
             jaxb = JAXBContext.newInstance("com.demo.jaxb", this.getClass().getClassLoader());
         } catch (JAXBException ex) {
             Logger.getLogger(OAMPolicyManager.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public String getXML(String path) { return base.path(path).get(String.class); }
    
 
     /**
      * Get a resource object
      */
     public Resource getResource(Resource r) {
         return base.path("/resource").
                 queryParam("name", r.getName()).get(Resource.class);
     }
 
     /**
      * Get the AuthN policy specified
      *
      * @param name - the policy name
      * @returns the AuthN policy or null if not found
      */
     public AuthenticationPolicy getAuthNPolicy(String name) {
         ClientResponse r = base.path("/authnpolicy").
                 queryParam("name", name).
                 accept(MediaType.APPLICATION_XML).
                 get(ClientResponse.class);
         AuthenticationPolicies p = r.getEntity(AuthenticationPolicies.class);
 
         return (r.getStatus() == 200 ? p.getAuthenticationPolicy().get(0) : null);
 
     }
 
     /**
      * Get the AuthZ policy specified
      *
      * @param name - the policy name
      * @returns the AuuthN policy or null if not found
      */
     public AuthorizationPolicy getAuthZPolicy(String name) {
         ClientResponse r = base.path("/authzpolicy").
                 queryParam("name", name).
                 accept(MediaType.APPLICATION_XML).
                 get(ClientResponse.class);
         AuthorizationPolicies p = r.getEntity(AuthorizationPolicies.class);
 
         return (r.getStatus() == 200 ? p.getAuthorizationPolicy().get(0) : null);
     }
 
     /**
      * Delete object with name
      *
      * @param name - of object
      */
     public void deleteName(String name, String type) {
         base.path(type).queryParam("name", name).delete();
     }
 
     /**
      * Create a Resource Object
      *
      * @param resourceURL - the url to protect. eg. /foo/
      **
      * @param hostIdentifier - the OAM host identifier. Eg. ohs1
      * @return The resource object
      */
     public Resource makeResourceObj(String resourceURL, String hostIdentifier) {
         Resource r = new Resource();
 
         String name = "HTTP::" + hostIdentifier + "::" + resourceURL + "::::";
 
         r.setName(name);
         r.setApplicationDomainName(appDomain);
         r.setDescription("Resource auto created at " + new Date().toString());
         r.setHostIdentifierName(hostIdentifier);
         r.setProtectionLevel(ResourceProtectionLevel.PROTECTED);
         r.setResourceURL(resourceURL);
         return r;
     }
 
     public AuthorizationPolicy getAuthorizationPolicy(String name) {
         return base.path("/authzpolicy").
                 queryParam("name", name).get(AuthorizationPolicy.class);
     }
     /**
      * Make an AuthZ Policy object that enforces group access
      *
      * @param name - name of the AuthZ Policy
      * @param groupName - LDAP Group Name. User must be in this group
      * @return
      */
     public AuthorizationPolicy makeAuthorizationPolicyObj(String name, String groupName) {
         AuthorizationPolicy p = new AuthorizationPolicy();
 
         p.setApplicationDomainName(appDomain);
         p.setName(name + "-" + groupName);
         p.setDescription("AuthZ Policy for " + name + ". Auto Created at " + new Date().toString());
 
         // create conditions. e.g. Group Membership
         Conditions c = new Conditions();
         p.setConditions(c);
         List<IdentityCondition> idcList = c.getIdentityCondition();
         IdentityCondition idc = new IdentityCondition();
         String conditionName = "GroupCondition-" + groupName;
         idc.setName(conditionName);
         
         Identity id = new Identity();
         id.setIdentityDomain(DATASTORE);
         
         // for ldap filter
         //id.setType(IdentityType.LDAP_SEARCH_FILTER);   
         //id.setSearchFilter("\"(objectclass=foo*)\"");
         
         // For ldap group search
         id.setType(IdentityType.GROUP);
         id.setSearchFilter(groupName);
         id.setName(groupName);
         
         Identities ids = new Identities();
         ids.getIdentity().add(id);
         idc.setIdentities(ids);
         idcList.add(idc);
 
         // The rule uses conditions to ALLOW / DENY access
         Rule r = new Rule();
         RuleCombiner rc = new RuleCombiner();
 
        
         SimpleCombiner simple = new SimpleCombiner();
 
         simple.setCombinerMode(RuleConditionCombiner.ALL);
         SimpleCombiner.Conditions sc = new SimpleCombiner.Conditions();
         simple.setConditions(sc);
         sc.getCondition().add(conditionName);
         rc.setSimpleCombiner(simple);
         r.setCombinerType(CombinerType.SIMPLE);
        
         // Alternative for expression combiner
         //r.setCombinerType(CombinerType.EXPRESSION);
         //rc.setExpression(conditionName);   
         
         r.setCombiner(rc);
         r.setEffect(RuleEffect.ALLOW);
         
         AuthorizationPolicy.Rules rules = new AuthorizationPolicy.Rules();
         rules.getRule().add(r);
 
         p.setRules(rules);
 
         p.setResources(new AuthorizationPolicy.Resources());
         p.setSuccessResponses(new SuccessResponses());
 
         return p;
     }
 
     /**
      * Create a Resource
      *
      * @param r - obj describing resource
      * @return - the id of the created resource
      */
     public String createResource(Resource r) {
         // wrap in JAXBElement to keep Jersey happy
         JAXBElement e = objFactory.createResource(r);
 
         ClientResponse response = base.path("/resource").
                 type(MediaType.APPLICATION_XML).
                 post(ClientResponse.class, e);
 
         return Util.getId(response);
     }
 
     /**
      * Create an OAM AuthZ Policy
      *
      * @param authPolicy
      * @return - Response string (URI to created policy)
      */
     public String createAuthorizationPolicy(AuthorizationPolicy authPolicy) {
         JAXBElement e = objFactory.createAuthorizationPolicy(authPolicy);
 
         ClientResponse response = base.path("/authzpolicy").
                 type(MediaType.APPLICATION_XML).
                 post(ClientResponse.class, e);
 
         return response.toString();
     }
 
     public String updateAuthNPolicy(AuthenticationPolicy ap) {
         JAXBElement e = objFactory.createAuthenticationPolicy(ap);
         ClientResponse r = base.path("/authnpolicy").
                 type(MediaType.APPLICATION_XML).
                 put(ClientResponse.class, e);
         return r.toString();
     }
     
     private String updateAuthZPolicy(AuthorizationPolicy p) {
          JAXBElement e = objFactory.createAuthorizationPolicy(p);
         ClientResponse r = base.path("/authzpolicy").
                 type(MediaType.APPLICATION_XML).
                 put(ClientResponse.class, e);
         return r.toString();
     }
     
 
     /**
      * Add the resource id to the default AuthN policy
      *
      * @param id - resource id
      */
     public void addResourceToDefaultAuthnPolicy(String id) {
         AuthenticationPolicy authn = getAuthNPolicy(DEFAULT_AUTHN_POLICY);
         List<String> res = authn.getResources().getResource();
          
         res.add(id); //todo: Fix double add
         updateAuthNPolicy(authn);
     }
 
     /**
      * Remove the resource id to the default AuthN policy
      *
      * @param id - resource id
      */
     public void removeResourceFromDefaultAuthnPolicy(Resource r) { 
         AuthenticationPolicy authn = getAuthNPolicy(DEFAULT_AUTHN_POLICY);
         List<String> res = authn.getResources().getResource();
         res.clear(); // PUT requires delta
         //res.remove(r.getId());
         updateAuthNPolicy(authn);
     }
     
     /**
      * Add an Application to OAM by creating the appropriate policies to protect
      * the app
      *
      * @param appName - Name of the Application
      * @param ldapGroup - Group that is allowed to access the Application
      * @param urlPattern - URL to protect access
      */
     public void addApplication(String appName, String ldapGroup, String urlPattern) {
         p("Create Application: name=" + appName + " group=" + ldapGroup + " url=" + urlPattern);
 
         String group = Util.cleanLdapGroup(ldapGroup);
         // First Create a resource
         Resource r = makeResourceObj(urlPattern, HOSTIDENTIFIER);
         String id = createResource(r);
         p("Created Resource to protect " + urlPattern + ". ID = " + id);
 
         addResourceToDefaultAuthnPolicy(id);
 
         // Create a new AuthZ Policy that uses the group filter
         AuthorizationPolicy authPolicy = makeAuthorizationPolicyObj(appName, group);
         // Add our Resource to the policy
         List<String> policies = authPolicy.getResources().getResource();
         policies.add(id);
         p("Added resource id " + id + " to list. size list=" + policies.size());
         // Create it
         String response = createAuthorizationPolicy(authPolicy);
         p("Created Aith Policy. Response=" + response);
     }
 
    public void removeApplication(String appName, String groupName, String urlPattern) {
         // delete the resource. 
         Resource r = makeResourceObj(urlPattern, HOSTIDENTIFIER);
                
         // delete AuthZ policy. This deletes the resource for us
         deleteName(appName + "-" + groupName,"/authzpolicy");
         
         // remove resource from the authN policy
         removeResourceFromDefaultAuthnPolicy(r);  
         
        // delete the resource. T
         deleteName(r.getName(), "/resource"); // delete resource obj        
     } 
 
     // dump string to logger
     void p(String s) {
         Logger.getLogger(OAMPolicyManager.class.getName()).log(Level.INFO, s);
     }
    
 }
