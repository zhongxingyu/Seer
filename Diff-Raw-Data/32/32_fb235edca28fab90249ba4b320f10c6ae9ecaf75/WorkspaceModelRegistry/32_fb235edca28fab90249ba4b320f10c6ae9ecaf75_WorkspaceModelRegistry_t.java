 package org.eclipse.gmf.internal.xpand.build;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
 import org.eclipse.emf.ecore.plugin.EcorePlugin;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 
 class WorkspaceModelRegistry implements MetaModelSource {
 	private static class Descriptor {
 		final String workspacePath;
 		final String nsURI;
 		final Resource resource;
 
 		public Descriptor(String workspacePath, String nsURI, Resource res) {
 			assert workspacePath != null && nsURI != null && res != null;
 			this.workspacePath = workspacePath;
 			this.nsURI = nsURI;
 			this.resource = res;
 		}
 	}
 
 	private final Map<String, Descriptor> pathToDescriptor = new TreeMap<String, Descriptor>();
 	private final Map<String, Descriptor> uriToDescriptor = new TreeMap<String, Descriptor>();
 
 //	void DEBUG_DUMP() {
 //		System.err.println(">>> " + WorkspaceModelRegistry.class.getSimpleName());
 //		for (Map.Entry<String, Descriptor> e : uriToDescriptor.entrySet()) {
 //			assert e.getKey().equals(e.getValue().nsURI);
 //			System.err.println(e.getKey() + " ==> " + e.getValue().workspacePath);
 //		}
 //		System.err.println("<<< " + WorkspaceModelRegistry.class.getSimpleName());
 //	}
 	
 	public WorkspaceModelRegistry() {
 		resourceSet.getURIConverter().getURIMap().putAll(EcorePlugin.computePlatformURIMap());
 	}
 
 	public EPackage find(String nsURI) {
 		Descriptor d = uriToDescriptor.get(nsURI);
 		return d == null ? null : (EPackage) d.resource.getContents().get(0);
 	}
 
 	public void build(IProject project, IProgressMonitor monitor) throws CoreException {
 		EcoreModelResourceVisitor visitor = new EcoreModelResourceVisitor(monitor);
 		project.accept(visitor);
 		handleCollectedData(visitor);
 	}
 
 	public void build(IProject project, IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
 		EcoreModelResourceVisitor visitor = new EcoreModelResourceVisitor(monitor);
 		delta.accept(visitor);
 		handleCollectedData(visitor);
 	}
 
 	private void handleCollectedData(EcoreModelResourceVisitor visitor) {
 		for (IFile f : visitor.getRemovedModelFiles()) {
 			String workspacePath = getKeyPath(f);
 			Descriptor d = pathToDescriptor.remove(workspacePath);
 			if (d != null) {
 				Descriptor d2 = uriToDescriptor.remove(d.nsURI);
 				assert d2 == d;
 			}
 			delist(d);
 		}
 		Set<IFile> filesToAdd = new HashSet<IFile>(visitor.getNewModelFiles());
 		for (IFile f : visitor.getChangedModelFiles()) {
 			Descriptor d = pathToDescriptor.remove(getKeyPath(f));
 			if (d != null) {
 				Descriptor d2 = uriToDescriptor.remove(d.nsURI); // uri might be changed, let alone Descriptor is not modifyable
 				assert d2 == d;
 				delist(d);
 			}
 			filesToAdd.add(f);
 		}
 		for (IFile f : filesToAdd) {
 			try {
 				Resource r = attemptLoad(f);
 				if (r != null && hasSuitableContent(r)) {
 					Descriptor d = createDescriptor(f, r);
 					assert d != null;
 					pathToDescriptor.put(d.workspacePath, d);
 					uriToDescriptor.put(d.nsURI, d);
 				}
 			} catch (Exception ex) {
 				ex.printStackTrace();
 				// ignore
 			}
 		}
 	}
 
 	// TODO per-project?
	private final ResourceSet resourceSet = new ResourceSetImpl() {
		protected Resource delegatedGetResource(URI uri, boolean loadOnDemand) {
			/* 
			 * EMF 2.5 (Aug 2, 2008, [189169]) has changed the way Ecore.ecore references primitive types, i.e. #//EBoolean (same file)
			 * instead of EcorePackage.nsURI#//EBoolean. Hence, if Ecore.ecore happen to be loaded as a resource
			 * (e.g. gmfgen.ecore loaded by WorkspaceModelRegistry from a workspace, with references to genmodel.ecore and 
			 * ecore.ecore expressed as platform:/plugin/), primitive attributes of Ecore model objects can't be matched
			 * against EcorePackage.eINSTANCE.getEBoolean() metaobjects
			 * Basically, that's the same issue we had few times already, when EClass from EcorePackage doesn't match 
			 * someMetaclass.eClass() if someMetaclass was loaded from a file/resource. Now, this problem applies to 
			 * primitive types as well.
			 * 
			 * As a solution, replace platform URIs with nsURIs (unless former were overriden with some workspace-local models) 
			 */
			final Map<URI, URI> map = getURIConverter().getURIMap();
			final URI emfGenModel = URI.createURI("platform:/plugin/org.eclipse.emf.codegen.ecore/model/GenModel.ecore");
			if (uri.equals(emfGenModel) && !map.containsKey(emfGenModel)) {
				 // GenModelPackage.nsURI
				return super.delegatedGetResource(URI.createURI("http://www.eclipse.org/emf/2002/GenModel"), loadOnDemand);
			}
			final URI emfEcore = URI.createURI("platform:/plugin/org.eclipse.emf.ecore/model/Ecore.ecore");
			if (uri.equals(emfEcore) && !map.containsKey(emfEcore)) {
				return super.delegatedGetResource(URI.createURI(EcorePackage.eNS_URI), loadOnDemand);
			}
			return super.delegatedGetResource(uri, loadOnDemand);
		};
	};
 
 	private Resource attemptLoad(IFile file) throws IOException {
 		URI uri = URI.createPlatformResourceURI(file.getFullPath().toString(), false);
 		Resource res = getResourceSet(file).getResource(uri, true);
 		if (res == null) {
 			throw new FileNotFoundException("Can't load model from " + file.getFullPath());
 		}
 		return res;
 	}
 
 	private ResourceSet getResourceSet(IFile file) {
 		return resourceSet;
 	}
 
 	// works in pair with #createDescriptor - may extract these as interface to support models other than Ecore
 	private boolean hasSuitableContent(Resource r) {
 		assert r != null;
 		return r.getContents().get(0) instanceof EPackage;
 	}
 
 	private Descriptor createDescriptor(IFile f, Resource res) {
 		final String path = getKeyPath(f);
 		EPackage p = (EPackage) res.getContents().get(0);
 		final String nsURI = p.getNsURI();
 		if (nsURI == null) {
 			throw new IllegalArgumentException("Invalid model file (missed nsURI) " + path);
 		}
 		return new Descriptor(path, nsURI, res);
 	}
 
 	private void delist(Descriptor d) {
 		d.resource.unload();
 		d.resource.getResourceSet().getResources().remove(d.resource);
 	}
 
 	private String getKeyPath(IFile f) {
 		return f.getFullPath().toString();
 	}
 }
