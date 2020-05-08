 /*
   * JBoss, Home of Professional Open Source
   * Copyright 2007, JBoss Inc., and individual contributors as indicated
   * by the @authors tag. See the copyright.txt in the distribution for a
   * full listing of individual contributors.
   *
   * This is free software; you can redistribute it and/or modify it
   * under the terms of the GNU Lesser General Public License as
   * published by the Free Software Foundation; either version 2.1 of
   * the License, or (at your option) any later version.
   *
   * This software is distributed in the hope that it will be useful,
   * but WITHOUT ANY WARRANTY; without even the implied warranty of
   * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
   * Lesser General Public License for more details.
   *
   * You should have received a copy of the GNU Lesser General Public
   * License along with this software; if not, write to the Free
   * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
   * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
   */
 package org.jboss.security.xacml.core;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.InputStream;
 import java.io.Serializable;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 import java.util.logging.Logger;
 
 import javax.xml.XMLConstants;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.stream.XMLStreamReader;
 import javax.xml.validation.Schema;
 import javax.xml.validation.SchemaFactory;
 
 import org.jboss.security.xacml.bridge.JBossPolicyFinder;
 import org.jboss.security.xacml.factories.PolicyFactory;
 import org.jboss.security.xacml.factories.RequestResponseContextFactory;
 import org.jboss.security.xacml.interfaces.AbstractLocator;
 import org.jboss.security.xacml.interfaces.PolicyDecisionPoint;
 import org.jboss.security.xacml.interfaces.PolicyLocator;
 import org.jboss.security.xacml.interfaces.RequestContext;
 import org.jboss.security.xacml.interfaces.ResponseContext;
 import org.jboss.security.xacml.interfaces.XACMLConstants;
 import org.jboss.security.xacml.interfaces.XACMLPolicy;
 import org.jboss.security.xacml.jaxb.LocatorType;
 import org.jboss.security.xacml.jaxb.LocatorsType;
 import org.jboss.security.xacml.jaxb.Option;
 import org.jboss.security.xacml.jaxb.PDP;
 import org.jboss.security.xacml.jaxb.PoliciesType;
 import org.jboss.security.xacml.jaxb.PolicySetType;
 import org.jboss.security.xacml.jaxb.PolicyType;
 import org.jboss.security.xacml.locators.AttributeLocator;
 import org.jboss.security.xacml.locators.ResourceLocator;
 import org.jboss.security.xacml.locators.cache.CacheLocator;
 import org.jboss.security.xacml.locators.cache.DecisionCacheLocator;
 import org.jboss.security.xacml.sunxacml.PDPConfig;
 import org.jboss.security.xacml.sunxacml.ctx.RequestCtx;
 import org.jboss.security.xacml.sunxacml.ctx.ResponseCtx;
 import org.jboss.security.xacml.sunxacml.finder.AttributeFinder;
 import org.jboss.security.xacml.sunxacml.finder.AttributeFinderModule;
 import org.jboss.security.xacml.sunxacml.finder.PolicyFinderModule;
 import org.jboss.security.xacml.sunxacml.finder.ResourceFinder;
 import org.jboss.security.xacml.sunxacml.finder.ResourceFinderModule;
 import org.jboss.security.xacml.sunxacml.finder.impl.CurrentEnvModule;
 import org.jboss.security.xacml.sunxacml.finder.impl.SelectorModule;
 import org.jboss.security.xacml.util.JBossXACMLEntityResolver;
 import org.w3c.dom.Node;
 import org.xml.sax.ErrorHandler;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 
 /**
  *  <p>PDP for JBoss XACML</p>
  *  <b>Thread-safe evaluate method</b>
  *  @author Anil.Saldhana@redhat.com
  *  @since  Jul 6, 2007 
  *  @version $Revision$
  */
 public class JBossPDP implements PolicyDecisionPoint, Serializable
 {
    private static final long serialVersionUID = -7665101320619759984L;
 
    private static Logger log = Logger.getLogger(JBossPDP.class.getName());
    
    private Unmarshaller unmarshaller = null;
 
    private Set<AttributeFinderModule> attributeLocators = new HashSet<AttributeFinderModule>();
    
    private Set<PolicyLocator> policyLocators = new HashSet<PolicyLocator>();
    private Set<ResourceLocator> resourceLocators = new HashSet<ResourceLocator>();
    
    private List<CacheLocator> cacheLocators = new ArrayList<CacheLocator>();
    
    private Set<XACMLPolicy> policies = new HashSet<XACMLPolicy>();
 
    private JBossPolicyFinder policyFinder = new JBossPolicyFinder();
 
    private org.jboss.security.xacml.sunxacml.PDP policyDecisionPoint = null;
    
    private Lock lock = new ReentrantLock();
    
    /**
     * JAXBContext is thread safe and very expensive to create
     */
    private static JAXBContext jaxbContext;
 
    static
    {
       try
       {
         jaxbContext = JAXBContext.newInstance("org.jboss.security.xacml.jaxb");
       }
       catch (JAXBException e)
       {
          throw new RuntimeException(e);
       }
    
       //Following is an optimization for Sun VMs which does NOT affect other VMs
       SecurityActions.setSystemProperty("com.sun.xml.bind.v2.runtime.JAXBContextImpl.fastBoot", "true");
    }
    
    /**
     * CTR
     */
    public JBossPDP()
    {
       if(SecurityActions.getSystemProperty("org.jboss.security.xacml.schema.validation") == null)
 	     this.createValidatingUnMarshaller();
       else
          this.createUnMarshaller();
    }
 
    /**
     * Create a PDP
     * @param configFile Inputstream for the JBossXACML Config File
     */
    public JBossPDP(InputStream configFile)
    {
       this();
       try
       {
          JAXBElement<?> jxb = (JAXBElement<?>) unmarshaller.unmarshal(configFile);
          bootstrap((PDP) jxb.getValue());
       }
       catch (Exception e)
       {
          throw new RuntimeException(e);
       }
    }
 
    /**
     * Create a PDP
     * @param configFile InputSource for the JBossXACML Config File
     */
    public JBossPDP(InputSource configFile)
    {
 	  this();
       try
       {
          JAXBElement<?> jxb = (JAXBElement<?>) unmarshaller.unmarshal(configFile);
          bootstrap((PDP) jxb.getValue());
       }
       catch (Exception e)
       {
          throw new RuntimeException(e);
       }
    }
 
    /**
     * Create a PDP
     * @param configFile Parsed Node for the JBossXACML Config File
     */
    public JBossPDP(Node configFile)
    {
 	  this();
       try
       {
          JAXBElement<?> jxb = (JAXBElement<?>) unmarshaller.unmarshal(configFile);
          bootstrap((PDP) jxb.getValue());
       }
       catch (Exception e)
       {
          throw new RuntimeException(e);
       }
    }
 
    /**
     * Create a PDP
     * @param configFile XMLStreamReader for the JBossXACML Config File
     */
    public JBossPDP(XMLStreamReader configFile)
    {
 	  this();
 	  try
       {
          JAXBElement<?> jxb = (JAXBElement<?>) unmarshaller.unmarshal(configFile);
          bootstrap((PDP) jxb.getValue());
       }
       catch (Exception e)
       {
          throw new RuntimeException(e);
       }
    }
 
    /**
     * Create a PDP
     * @param configFileURL URL of the JBossXACML Config File
     */
    public JBossPDP(URL configFileURL)
    {
       this();
       try
       {
          JAXBElement<?> jxb = (JAXBElement<?>) unmarshaller.unmarshal(configFileURL.openStream());
          bootstrap((PDP) jxb.getValue());
       }
       catch (Exception e)
       {
          throw new RuntimeException(e);
       }
    }
    
    /**
     * Create a PDP
     * @param config JAXB model for configuration
     */
    public JBossPDP(JAXBElement<?> config)
    {
       Object object = config.getValue();
       if(object instanceof PDP == false)
          throw new IllegalArgumentException("Not PDP configuration");
       try
       {
          bootstrap((PDP) object);
       }
       catch (Exception e)
       {
          throw new RuntimeException(e);
       }
    }
 
    /**
     * @see PolicyDecisionPoint#setLocators(Set)
     */
    public void setLocators(Set<PolicyLocator> locators)
    {
       this.policyLocators = locators;
    }
 
    /**
     * @see PolicyDecisionPoint#setPolicies(Set)
     */
    public void setPolicies(Set<XACMLPolicy> policies)
    {
       this.policies = policies;
    }
 
    /**
     * @see PolicyDecisionPoint#evaluate(RequestContext)
     */ 
    public ResponseContext evaluate(RequestContext request)
    { 
       RequestCtx req = (RequestCtx) request.get(XACMLConstants.REQUEST_CTX);
       if (req == null)
          throw new IllegalStateException("Request Context does not contain a request");
 
       //Check if PDP is null
       if(policyDecisionPoint == null)
       {   
          this.bootstrapPDP();
       }
       
       ResponseCtx resp = null;
       
       lock.lock();
       try
       {
          int cacheLocatorsLength = cacheLocators.size();
          
          if( cacheLocatorsLength > 0 )
          {
             for( int i = 0 ; i < cacheLocatorsLength; i++ )
             {
                CacheLocator cacheLocator = cacheLocators.get(i);
                resp = cacheLocator.get( req );
                if( resp != null )
                   break;
             }
          }
          
          //We got nothing from the cache?
          if( resp == null ) 
          {
             resp = policyDecisionPoint.evaluate(req); 
             
             //add it to cache locators
             if( cacheLocatorsLength > 0 )
             {
                for( int i = 0 ; i < cacheLocatorsLength; i++ )
                {
                   CacheLocator cacheLocator = cacheLocators.get(i);
                   if( cacheLocator instanceof DecisionCacheLocator  )
                   {
                      ( ( DecisionCacheLocator ) cacheLocator ).add( req, resp );
                   } 
                }
             }  
          }  
       }
       finally
       {
          lock.unlock();
       }
 
       ResponseContext response = RequestResponseContextFactory.createResponseContext();
       response.set(XACMLConstants.RESPONSE_CTX, resp);
       return response;
    }
 
    private void bootstrap(PDP pdp) throws Exception
    {
       boolean justLocators = false;
       
       PoliciesType policiesType = pdp.getPolicies();
       //SECURITY-407: Just allow Locators
       if(policiesType != null)
       {
          List<PolicySetType> pset = policiesType.getPolicySet();
 
          this.addPolicySets(pset, true);
 
          //Take care of additional policies
          List<XACMLPolicy> policyList = this.addPolicies(policiesType.getPolicy());
          policies.addAll(policyList);  
       }
       else
       {
          justLocators = true;
       }
       
       //Take care of the locators
       LocatorsType locatorsType = pdp.getLocators();
       
       if(policiesType == null && locatorsType == null)
          throw new IllegalStateException("Configuration should have either policies or locators");
       
       List<LocatorType> locs = locatorsType.getLocator();
       for (LocatorType lt : locs)
       {
          //Get the options
          List<Option> options = lt.getOption();
          AbstractLocator locator = (AbstractLocator) loadClass(lt.getName()).newInstance();
          locator.setOptions(options);
          
          if(locator instanceof PolicyLocator)
          {
             PolicyLocator pl = (PolicyLocator)locator; 
             if(justLocators == false)     
                pl.setPolicies(policies);
             this.policyLocators.add(pl); 
          }
          else if(locator instanceof AttributeLocator)
          {
             AttributeLocator attribLocator = (AttributeLocator) locator;
             this.attributeLocators.add(attribLocator);
          }
          else if(locator instanceof ResourceLocator)
          {
             ResourceLocator resourceLocator = (ResourceLocator) locator;
             this.resourceLocators.add(resourceLocator);
          }
          else if( locator instanceof CacheLocator )
          {
             this.cacheLocators.add( (CacheLocator) locator );
          }
       } 
       
       //Since we do not have any policies in the config file, we need to specify 
       //the policy finder
       if(justLocators)
       {
          int len = this.policyLocators.size();
          if(len > 0)
          {
             for(PolicyLocator pl: policyLocators)
             {
                pl.set(XACMLConstants.POLICY_FINDER, this.policyFinder); 
             } 
          }
       } 
       
       this.bootstrapPDP(); 
    }
    
    private List<AttributeFinderModule> createAttributeFinderModules()
    {
       List<AttributeFinderModule> attributeModules = new ArrayList<AttributeFinderModule>();
       attributeModules.add(new CurrentEnvModule());
       attributeModules.add(new SelectorModule());      
       attributeModules.addAll(attributeLocators); 
       return attributeModules;
    }
 
    @SuppressWarnings("unchecked")
    private Set<PolicyFinderModule> createPolicyFinderModules()
    {
       HashSet<PolicyFinderModule> policyModules = new HashSet<PolicyFinderModule>();
       //Go through the Locators
       for (PolicyLocator locator : policyLocators)
       {
          @SuppressWarnings("rawtypes")
          List finderModulesList = (List) locator.get(XACMLConstants.POLICY_FINDER_MODULE);
          if (finderModulesList == null)
             throw new IllegalStateException("Locator " + locator.getClass().getName() + " has no policy finder modules");
          policyModules.addAll(finderModulesList);
       }
       return policyModules; 
    }
    
    private List<ResourceFinderModule> createResourceFinderModules()
    {
       List<ResourceFinderModule> resourceFinderModules = new ArrayList<ResourceFinderModule>();
       for(ResourceLocator resourceLocator: resourceLocators)
       {
          resourceFinderModules.add(resourceLocator);
       }
       return resourceFinderModules;
    }
    
    private void bootstrapPDP()
    {
       AttributeFinder attributeFinder = new AttributeFinder();
       attributeFinder.setModules(this.createAttributeFinderModules());
       
       policyFinder.setModules(this.createPolicyFinderModules());
       
       ResourceFinder resourceFinder = new ResourceFinder();
       resourceFinder.setModules(this.createResourceFinderModules());
       
       PDPConfig pdpConfig = new PDPConfig(attributeFinder, policyFinder, resourceFinder); 
       policyDecisionPoint = new org.jboss.security.xacml.sunxacml.PDP(pdpConfig);  
    }
 
    private List<XACMLPolicy> addPolicySets(List<PolicySetType> policySets, boolean topLevel) throws Exception
    {
       List<XACMLPolicy> list = new ArrayList<XACMLPolicy>();
       
       for (PolicySetType pst : policySets)
       {
          String loc = pst.getLocation();
          log.info("Reading policysets from location="+loc); 
          if( isDirectory(loc))
          {
             InputStream[] streams = this.readPoliciesFromDir(loc);
             for( InputStream stream : streams)
             {
                list.add(PolicyFactory.create(stream, policyFinder));
             }
             policies.addAll(list);
          }
          else
          {
             XACMLPolicy policySet = PolicyFactory.createPolicySet(getInputStream(loc), policyFinder);
             list.add(policySet);
             List<XACMLPolicy> policyList = this.addPolicies(pst.getPolicy());
             policySet.setEnclosingPolicies(policyList);
 
             List<PolicySetType> pset = pst.getPolicySet();
             if (pset != null)
                policySet.getEnclosingPolicies().addAll(this.addPolicySets(pset, false));
 
             if (topLevel)
                policies.add(policySet); 
          }
       }
 
       return list;
    }
 
    private List<XACMLPolicy> addPolicies(List<PolicyType> policies) throws Exception
    {
       List<XACMLPolicy> policyList = new ArrayList<XACMLPolicy>();
       for (PolicyType pt : policies)
       {
          policyList.add(PolicyFactory.createPolicy(getInputStream(pt.getLocation())));
       }
 
       return policyList;
    }
    
    private void createUnMarshaller()
    {
       try
       { 
          unmarshaller = jaxbContext.createUnmarshaller();
       }catch(JAXBException je)
       {
          throw new RuntimeException(je);
       }
    }
 
    private void createValidatingUnMarshaller()
    {
       try
       {
          createUnMarshaller();
          
          //Validate against schema
          ClassLoader tcl = SecurityActions.getContextClassLoader();
          URL schemaURL = tcl.getResource("schema/jbossxacml-2.0.xsd");
          if(schemaURL == null)
             throw new IllegalStateException("Schema URL is null:" + "schema/jbossxacml-2.0.xsd");
          
          SchemaFactory scFact = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
          scFact.setResourceResolver( new JBossXACMLEntityResolver()); 
          scFact.setErrorHandler(new ErrorHandler()
          {
             public void error(SAXParseException exception) throws SAXException
             {
                StringBuilder builder = new StringBuilder();
                builder.append("Line Number=").append(exception.getLineNumber());
                builder.append(" Col Number=").append(exception.getColumnNumber());
                builder.append(" Public ID=").append(exception.getPublicId());
                builder.append(" System ID=").append(exception.getSystemId());
                builder.append(" exc=").append(exception.getLocalizedMessage());
                
                log.finest("SAX Error:" + builder.toString());
             }
 
             public void fatalError(SAXParseException exception) throws SAXException
             {
                StringBuilder builder = new StringBuilder();
                builder.append("Line Number=").append(exception.getLineNumber());
                builder.append(" Col Number=").append(exception.getColumnNumber());
                builder.append(" Public ID=").append(exception.getPublicId());
                builder.append(" System ID=").append(exception.getSystemId());
                builder.append(" exc=").append(exception.getLocalizedMessage());
                
                log.finest("SAX Fatal Error:" + builder.toString());
             }
 
             public void warning(SAXParseException exception) throws SAXException
             {
                StringBuilder builder = new StringBuilder();
                builder.append("Line Number=").append(exception.getLineNumber());
                builder.append(" Col Number=").append(exception.getColumnNumber());
                builder.append(" Public ID=").append(exception.getPublicId());
                builder.append(" System ID=").append(exception.getSystemId());
                builder.append(" exc=").append(exception.getLocalizedMessage());
                
                log.finest("SAX Warn:" + builder.toString());        
             }
          });
          
          Schema schema = scFact.newSchema(schemaURL);
          unmarshaller.setSchema(schema);
       }
       catch (Exception jxb)
       {
          throw new RuntimeException(jxb);
       }
    }
    
    private boolean isDirectory(String location)
    {
       boolean result = false;
       File file = new File(location);
       result =  (file !=null && file.isDirectory());
       URI uri = null;
       
       if( !result)
       {
         uri = getResourceViaClassLoader(SecurityActions.getContextClassLoader(), location);
         if( uri != null)
         {
            file = new File( uri ); 
         } 
         result = (file !=null && file.isDirectory()); 
       }
       if( !result)
       { 
         uri = getResourceViaClassLoader(SecurityActions.getClassLoader(getClass()), location);
         if( uri != null)
         {
            file = new File( uri ); 
         } 
         result = (file !=null && file.isDirectory()); 
       }
       return result;
    }
    
    private URI getResourceViaClassLoader( ClassLoader cl, String location)
    {
       URL url = cl.getResource(location);
       if( url != null )
       {
          try
          {
             return url.toURI();
          }
          catch (URISyntaxException e)
          {
             // ignore
          }
       }
       return null;
    }
    
    private InputStream[] readPoliciesFromDir( String location)
    {
       URI uri = getResourceViaClassLoader(SecurityActions.getContextClassLoader(), location);
       if( uri == null)
          uri = getResourceViaClassLoader(SecurityActions.getClassLoader(getClass()), location);
       
       if( uri == null )
          throw new RuntimeException("Unable to load the URI:" + location);
       
       ArrayList<InputStream> list = new ArrayList<InputStream>();
       File dir = new File(uri);
       if( dir == null || !dir.isDirectory())
          throw new RuntimeException( location + " is not a directory" );
       String[]  files = dir.list(new FilenameFilter()
       {     
          public boolean accept(File dir, String name)
          { 
             return !name.startsWith(".");
          }
       });
       for( String fileName: files)
       {
          list.add(getInputStream(location + fileName));
       }
       InputStream[] isArr = new InputStream[list.size()];
       list.toArray(isArr);
       return isArr;
    }
 
    private InputStream getInputStream(String loc)
    {
       InputStream is = null;
       //Try URL
       try
       {
          URL url = new URL(loc);
          is = url.openStream();
       }
       catch (Exception e)
       {
       }
       if (is == null)
       {
          ClassLoader tcl = SecurityActions.getContextClassLoader();
          is = tcl.getResourceAsStream(loc);
       }
       if (is == null)
          throw new RuntimeException("Null Inputstream for " + loc);
       return is;
    }
 
    private Class<?> loadClass(String fqn) throws Exception
    {
       ClassLoader tcl = SecurityActions.getContextClassLoader();
       return tcl.loadClass(fqn);
    }
 }
