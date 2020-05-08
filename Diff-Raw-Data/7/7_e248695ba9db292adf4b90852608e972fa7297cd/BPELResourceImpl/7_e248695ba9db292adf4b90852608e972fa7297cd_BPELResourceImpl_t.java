 /*******************************************************************************
  * Copyright (c) 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.bpel.model.resource;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.namespace.QName;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.xerces.parsers.DOMParser;
 import org.eclipse.bpel.model.Import;
 import org.eclipse.bpel.model.Process;
 import org.eclipse.bpel.model.adapters.INamespaceMap;
 import org.eclipse.bpel.model.util.BPELConstants;
 import org.eclipse.bpel.model.util.BPELProxyURI;
 import org.eclipse.bpel.model.util.BPELUtils;
 import org.eclipse.bpel.model.util.ImportResolver;
 import org.eclipse.bpel.model.util.ImportResolverRegistry;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.xmi.impl.XMLResourceImpl;
 import org.eclipse.xsd.impl.XSDSchemaImpl;
 import org.eclipse.xsd.util.XSDConstants;
 import org.xml.sax.EntityResolver;
 import org.xml.sax.ErrorHandler;
 
 
 /**
  * @author IBM Original Contribution.
  * @author Michal Chmielewski (michal.chmielewski@oracle.com) 
  *
  */
 
 public class BPELResourceImpl extends XMLResourceImpl implements BPELResource { 
 
 	static final String EMPTY_STRING = ""; //$NON-NLS-1$
	static final String[] EMPTY_ARRAY_OF_STRING = {};
 	
 	protected static boolean USE_IMPORTS = false;
 
     /** @see #getNamespaceURI() */
     private String processNamespaceURI = BPELConstants.NAMESPACE;
     
     /** @see #getOptionUseNSPrefix() */
     private boolean optionUseNSPrefix = true;
     
 	protected boolean fValidating = false;
 	protected EntityResolver fEntityResolver = null;
 	protected ErrorHandler fErrorHandler = null;
 
 	/**
 	 * Brand new BPEL Resource Implementation.
 	 */
 	public BPELResourceImpl() {
 		super();
 	}
 
 	/**
 	 * Brand new BPEL Resource Implementation.
 	 * @param arg0
 	 */
 	public BPELResourceImpl(URI arg0) {
 		super(arg0);
 	}
 	
 	/**
 	 * Brand new BPEL Resource Implementation.
 	 * @param aURI
 	 * @param entityResolver
 	 * @param errorHandler 
 	 */
 	
 	public BPELResourceImpl(URI aURI, EntityResolver entityResolver, ErrorHandler errorHandler) {
 		super(aURI);
 		fEntityResolver = entityResolver;
 		fErrorHandler = errorHandler;
 		fValidating = true;
 	}
 
 	/**
 	 * Convert the BPEL model to an XML DOM model and then write the DOM model
 	 * to the output stream.
 	 */
 	
 	@Override	
 	public void doSave (OutputStream out, Map args) throws IOException
     {
         INamespaceMap<String,String> nsMap = BPELUtils.getNamespaceMap ( getProcess() );
         
         if (getOptionUseNSPrefix()) {
         	
        for (String prefix : nsMap.getReverse(getNamespaceURI()).toArray(EMPTY_ARRAY_OF_STRING)  ) {
 	        	if (EMPTY_STRING.equals(prefix)) {
 	        		nsMap.remove(prefix);
 	        	}
         	}        	
 	        nsMap.put(BPELConstants.PREFIX, getNamespaceURI());
 	        
         } else {
             // Install the BPEL namespace as the default namespace.
             nsMap.put(EMPTY_STRING, getNamespaceURI());
         }
         
         BPELWriter writer = new BPELWriter();
 		writer.write(this, out, args);
 	}
 	
 	/** 
 	 * Convert a BPEL XML document into the BPEL EMF model.
      * After loading, the process' namespace URI is reset to the current namespace URI.
 	 */
 	@SuppressWarnings("nls")
 	@Override
 	public void doLoad (InputStream inputStream, Map options) throws IOException
     {        
        
 // RTP:        resolver = new Resolver(this);
         
         BPELReader reader = null;
         try {
         	reader = new BPELReader( getDOMParser() );
         } catch (IOException ioe) {
         	throw ioe;
         } catch (Exception ex) {
         	throw new IOException("Problem creating XML DOM parser");
         }
         
 		reader.read(this, inputStream);
 		
         setNamespaceURI(BPELConstants.NAMESPACE);
 	}
 
     /**
      * TODO Implement getURIFragment to return our encoding.
      */
     @Override
 	public String getURIFragment(EObject eObject)
     {
         return super.getURIFragment(eObject);
     }
     
     /**
      * Find and return the EObject represented by the given uriFragment.
      *
      * @return the resolved EObject or null if none could be found.
      */
     @Override
 	public EObject getEObject(String uriFragment) {
 	    
     	if (uriFragment == null) {
 	    	return null;
 	    }
 	    
 	    try {
 	    	// Consult the superclass
 		    EObject eObject = super.getEObject(uriFragment);
 		    if (eObject != null) {
 		    	return eObject;
 		    }
 		    // Consult our helper method
 	        eObject = getEObjectExtended(uriFragment);
 	        if (eObject != null) {
 	        	return eObject;
 	        }
 			return null;
 	    } catch (RuntimeException e) {
 	    	// TODO: Should log this instead of printing to stderr.
 	        e.printStackTrace();
 	        throw e;
 	    }
 	}
 
     /**
      * Helper method for resolving the EObject.
      * 
      */
     protected EObject getEObjectExtended (String uriFragment) 
     {
     	// RTP: this implementation should be extensible
     	
         BPELProxyURI proxyURI = new BPELProxyURI(uriFragment);
         
         QName qname = proxyURI.getQName();
         String typeName = proxyURI.getTypeName();
 
         if (qname == null || typeName == null) {
             return null;
         }
         
         EObject result = null;
         
     	// Try the BPEL imports if any exist.
         Process process = getProcess();
         if (process == null) {
         	return result;
         }
         
         for(Object next : process.getImports())  {
             Import imp = (Import) next;            
             
             // The null and EMPTY_STRING problem ...
             String ns = imp.getNamespace();
             if (ns == null) {
             	ns = javax.xml.XMLConstants.DEFAULT_NS_PREFIX;
             }
             
             if (ns.equals(qname.getNamespaceURI()) == false || 
              	imp.getLocation() == null ) {
             	continue;
             }
                             	    
     	    for(ImportResolver r : ImportResolverRegistry.INSTANCE.getResolvers(imp.getImportType()) ) {
                 result = r.resolve(imp, qname, proxyURI.getID(), proxyURI.getTypeName());
                 if (result != null) {
                     return result;
                 }
             }
             // next import
         }
         
         // Failed to resolve.
         return result;
     }
 
     
     
     /**
      * Return the list of schemas that are imported in this BPEL resource.
      * This includes XSD imports and schemas present in WSDLs as well.
      * 
      * @param bIncludeXSD whether the XSD standard schemas ought to be included
      * regardless of import.
      * 
      * @return a list of XSDScheme objects
      */
     public List getSchemas ( boolean bIncludeXSD ) 
     {
     	ArrayList al = new ArrayList(8);
     	
     	// Try the BPEL imports if any exist.
         Process process = getProcess();
         if (process == null) {
         	return al;
         }
         
         for(Object next : process.getImports()) {
             Import imp = (Import) next;                                    
             if (imp.getLocation() == null ) {
             	continue;
             }                	   
     	    for(ImportResolver r : ImportResolverRegistry.INSTANCE.getResolvers(imp.getImportType())) {
                 al.addAll( r.resolve (imp, ImportResolver.RESOLVE_SCHEMA ) );
             }
             // next import
         }
         
         if (bIncludeXSD) {
         	al.add ( XSDSchemaImpl.getSchemaForSchema( XSDConstants.SCHEMA_FOR_SCHEMA_URI_2001 ) );
         }
               
         return al;    	
     }
     
     
     /**
      * Get the definitions that are included in this BPEL (via the WSDL imports)
      *  
      * @return
      */
     
     public List getDefinitions () {
     	ArrayList al = new ArrayList(8);
     	
     	// Try the BPEL imports if any exist.
         Process process = getProcess();
         if (process == null) {
         	return al;
         }
                 
         for(Object next : process.getImports()) {
             Import imp = (Import) next;                                    
             if (imp.getLocation() == null ) {
             	continue;
             }                	    
     	    for(ImportResolver r : ImportResolverRegistry.INSTANCE.getResolvers(imp.getImportType())) {
                 al.addAll( r.resolve (imp, ImportResolver.RESOLVE_DEFINITION ) );
             }
             // next import
         }                        
         return al;    	
     	
     }
     
 	/**
 	 * @see org.eclipse.bpel.model.resource.BPELResource#getProcess()
 	 */
 	public Process getProcess() {
 	    return getContents().size() == 1 && getContents().get(0) instanceof Process ? (Process) getContents().get(0) : null;
 	}
 	
 	@SuppressWarnings("nls")
 	
 	protected DocumentBuilder getDocumentBuilder() throws IOException {
 		final DocumentBuilderFactory factory = //DocumentBuilderFactory.newInstance();
 		   // new org.apache.crimson.jaxp.DocumentBuilderFactoryImpl();
 			new org.apache.xerces.jaxp.DocumentBuilderFactoryImpl();
 
 		if (fValidating && factory.getClass().getName().indexOf("org.apache.xerces") != -1)
 		{
 		  // Note: This section is subject to change as this issue will be
 		  //       addressed in a maintenance release of JSR-63.
 		  //       Hopefully this will be a proper API in JAXP 1.2!
 		  // turn dynamic schema validation on
 		  factory.setAttribute("http://apache.org/xml/features/validation/dynamic", Boolean.TRUE);
 		  // turn schema validation on
 		  factory.setAttribute("http://apache.org/xml/features/validation/schema", Boolean.TRUE);
 		  // set the default schemaLocation for syntactical validation
           factory.setAttribute("http://apache.org/xml/properties/schema/external-schemaLocation",
                BPELConstants.NAMESPACE);
 		}
 
 		factory.setIgnoringComments(true);
 		factory.setIgnoringElementContentWhitespace(true);
 
 		factory.setValidating(fValidating);
 		factory.setNamespaceAware(true);
 
 		DocumentBuilder builder;
 		try {
 			builder = factory.newDocumentBuilder();
 		} catch (ParserConfigurationException exc) {
 			// exc.printStackTrace();
 			throw new IOException(exc.toString());
 		}
 		if (fValidating) {
 			builder.setEntityResolver( fEntityResolver );
 			builder.setErrorHandler( fErrorHandler );
 		}
 
 		return builder;
 	}
 	
 	
 
 	
 	@SuppressWarnings({ "nls", "boxing" })
 	protected DOMParser getDOMParser() throws Exception {
 
 		DOMParser domParser = new LineCapturingDOMParser() ;
 				
 		if (fValidating)
 		{
 		  // Note: This section is subject to change as this issue will be
 		  //       addressed in a maintenance release of JSR-63.
 		  //       Hopefully this will be a proper API in JAXP 1.2!
 		  // turn dynamic schema validation on
 		  domParser.setFeature("http://apache.org/xml/features/validation/dynamic", Boolean.TRUE);
 		  // turn schema validation on
 		  domParser.setFeature("http://apache.org/xml/features/validation/schema", Boolean.TRUE);
 		  // set the default schemaLocation for syntactical validation
 		  domParser.setProperty("http://apache.org/xml/properties/schema/external-schemaLocation",
               BPELConstants.NAMESPACE);
 		}
 				
 		// domParser.setProperty("http://xml.org/sax/features/namespaces",true);
 		domParser.setFeature( "http://apache.org/xml/features/dom/defer-node-expansion", false );
 		domParser.setFeature( "http://apache.org/xml/features/xinclude", false);
 		
 		if (fValidating) {
 			domParser.setEntityResolver( fEntityResolver );
 			domParser.setErrorHandler( fErrorHandler );
 		}
 		
 		return domParser;
 	}
 	
 	
 	/**
 	 * @param useImports
 	 */
 	public static void setUseImports(boolean useImports) {
 	    USE_IMPORTS = useImports;
 	}
 
     /**
      * @see org.eclipse.bpel.model.resource.BPELResource#getNamespaceURI()
      */
     public String getNamespaceURI() {
         return processNamespaceURI;
     }
     
     /**
      * @see org.eclipse.bpel.model.resource.BPELResource#setNamespaceURI(java.lang.String)
      */
     public void setNamespaceURI (String namespaceURI) {
         processNamespaceURI = namespaceURI;
     }
     
     /**
      * @see org.eclipse.bpel.model.resource.BPELResource#getOptionUseNSPrefix()
      */
     public boolean getOptionUseNSPrefix() {
         return optionUseNSPrefix;
     }
     
     /**
      * @see org.eclipse.bpel.model.resource.BPELResource#setOptionUseNSPrefix(boolean)
      */
     public void setOptionUseNSPrefix(boolean useNSPrefix) {
         optionUseNSPrefix = useNSPrefix;
     }
 
 }	
