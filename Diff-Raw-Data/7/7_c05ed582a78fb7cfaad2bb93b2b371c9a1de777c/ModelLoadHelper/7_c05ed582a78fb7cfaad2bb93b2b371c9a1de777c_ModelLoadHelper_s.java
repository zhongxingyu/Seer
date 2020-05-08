 /**
  * Copyright (c) 2006 Eclipse.org
  * 
  * All rights reserved. This program and the accompanying materials are made
  * available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: dvorak - initial API and implementation
  */
 package org.eclipse.gmf.internal.common.migrate;
 
 import java.io.IOException;
 import java.text.MessageFormat;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.emf.common.util.BasicDiagnostic;
 import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.common.util.WrappedException;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.plugin.EcorePlugin;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 
 /**
  * This class is helper for loading model from resource, handles load exceptions, result status
  * aggregation and safe root object access.
  */
 public class ModelLoadHelper {
 	private static final String DIAGNOSTIC_SOURCE = "gmf.common.modelLoadHelper"; //$NON-NLS-1$
 	
 	private IStatus status;
 	private Diagnostic diagnostic;	
 	private URI uri;
 	private ResourceSet resourceSet;
 	
 	/**
 	 * Gets resource loaded by this helper.
 	 */
 	public Resource getLoadedResource() {
 		return resourceSet.getResource(uri, false);
 	}
 	
 	/**
 	 * Returns first EObject in the loaded resource contents list.
 	 * @return root EObject or <code>null</code> if no one is available   
 	 */
 	public EObject getContentsRoot() {
 		Resource resource =  getLoadedResource();
 		assert resource != null;
 		if(!resource.getContents().isEmpty()) {
 			return (EObject)resource.getContents().get(0);
 		}
 		return null;
 	}
 
 	public boolean isOK() {
 		return diagnostic.getSeverity() == Diagnostic.OK;
 	}
 	
 	/**
 	 * Gets the status resulted from the load resource operation.
 	 * @return the status object
 	 */
 	public IStatus getStatus() {
 		if(status == null) {
 			status = BasicDiagnostic.toIStatus(diagnostic);
 		}
 		return status;
 	}
 	
 	public Diagnostic getDiagnostics() {
 		return diagnostic;
 	}
 	
 	/**
 	 * Creates resource diagnostic wrapping the given exception.
 	 * @param resource the resource associated with the created diagnostic
 	 * @param exception non-<code>null</code> exception to be wrapped as diagnostic
 	 * 
 	 * @return diagnostic object
 	 */
 	static Resource.Diagnostic createDiagnostic(Resource resource, Exception exception) {
 		if(exception == null) {
 			throw new IllegalArgumentException("null diagnostic exception"); //$NON-NLS-1$
 		}
 		final String location = resource.getURI() == null ? null : resource.getURI().toString();
 		class ExceptionDiagnostic extends WrappedException implements Resource.Diagnostic {
 			
 			public ExceptionDiagnostic(Exception exception) {
 				super(exception);
 			}
 	
 			public String getLocation() {
 				return location;
 			}
 	
 			public int getColumn() {
 				return 0;
 			}
 	
 			public int getLine() {
 				return 0;
 			}
 		}
 		
 		return new ExceptionDiagnostic(exception);
 	}
 	
 	/**
 	 * Constructs helper for loading resource refered by URI into given
 	 * resourceset.
 	 * 
 	 * @param targetResSet
 	 *            resourceset into which the resource will be loaded
 	 * @param resourceURI
 	 *            URI referencing the resource to load
 	 */
 	public ModelLoadHelper(ResourceSet targetResSet, URI resourceURI) {
 		if(targetResSet == null || resourceURI == null) {
 			throw new IllegalArgumentException("null resourceSet or resourceURI"); //$NON-NLS-1$
 		}
 		this.resourceSet = targetResSet;
 		this.uri = resourceURI;
 		this.diagnostic = internalLoad(targetResSet, uri);
 	}	
 
 	@SuppressWarnings("unchecked")
 	private static Diagnostic internalLoad(ResourceSet resourceSet, URI uri) {
 		Diagnostic diagnostic = Diagnostic.OK_INSTANCE;
 		Resource resource = resourceSet.createResource(uri);
 		assert resource != null;
 		Exception rootException = null;
 		try {
 			resource.load(resourceSet.getLoadOptions());
 		} catch(IOException e) {
			rootException = e;
			//no need to include in resource.getErrors(), as it done automatically. 
 		} catch(RuntimeException e) {			
 			EcorePlugin.INSTANCE.getPluginLogger().log(e);			
 			resource.getErrors().add(ModelLoadHelper.createDiagnostic(resource, e));
 		}
 		
 		if(!resource.getErrors().isEmpty() || !resource.getWarnings().isEmpty()) {
 			Diagnostic resourceDiagnostic = EcoreUtil.computeDiagnostic(resource, true);
 			Integer severityOpt = new Integer(resourceDiagnostic.getSeverity() == Diagnostic.ERROR ? 0 : 1);    
 			String message = MessageFormat.format(Messages.modelLoadedWithProblems, 
 					 new Object[] { severityOpt, resource.getURI() });			
 
 			Object[] data = (rootException != null) ? new Object[] { rootException, resource } : new Object[] { resource };			
 			BasicDiagnostic loadDiagnostic = new BasicDiagnostic(DIAGNOSTIC_SOURCE, resourceDiagnostic.getCode(), message, data);
 			loadDiagnostic.addAll(resourceDiagnostic);
 			diagnostic = loadDiagnostic;
 		}
 		return diagnostic;
 	}
 }
