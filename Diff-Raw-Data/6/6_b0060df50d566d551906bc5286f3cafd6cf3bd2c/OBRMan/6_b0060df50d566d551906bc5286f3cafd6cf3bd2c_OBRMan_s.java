 /**
  * Copyright 2011-2012 Universite Joseph Fourier, LIG, ADELE team
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  */
 package fr.imag.adele.obrMan.internal;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import org.apache.felix.bundlerepository.Repository;
 import org.apache.felix.bundlerepository.RepositoryAdmin;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.BundleException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import fr.imag.adele.apam.ApamManagers;
 import fr.imag.adele.apam.CST;
 import fr.imag.adele.apam.Component;
 import fr.imag.adele.apam.CompositeType;
 import fr.imag.adele.apam.Implementation;
 import fr.imag.adele.apam.Instance;
 import fr.imag.adele.apam.ManagerModel;
 import fr.imag.adele.apam.RelToResolve;
 import fr.imag.adele.apam.RelationManager;
 import fr.imag.adele.apam.Resolved;
 import fr.imag.adele.apam.Specification;
 import fr.imag.adele.apam.declarations.ComponentKind;
 import fr.imag.adele.apam.declarations.ComponentReference;
 import fr.imag.adele.apam.declarations.InterfaceReference;
 import fr.imag.adele.apam.declarations.MessageReference;
 import fr.imag.adele.apam.declarations.ResolvableReference;
 import fr.imag.adele.apam.declarations.ResourceReference;
 import fr.imag.adele.apam.declarations.SpecificationReference;
 import fr.imag.adele.obrMan.OBRManCommand;
 import fr.imag.adele.obrMan.internal.OBRManager.Selected;
 
 public class OBRMan implements RelationManager, OBRManCommand {
 
 	// Link compositeType with it instance of obrManager
 	private final Map<String, OBRManager> obrManagers;
 
 	// iPOJO injected
 	private RepositoryAdmin repoAdmin;
 
 	private final Logger logger = LoggerFactory.getLogger(OBRMan.class);
 
 	private final BundleContext m_context;
 
 	private final long timeout = 10000;
 
 	static List<String> onLoadingResource = new ArrayList<String>();
 
 	/**
 	 * OBRMAN activated, register with APAM
 	 */
 	public OBRMan(BundleContext context) {
 		m_context = context;
 		obrManagers = new HashMap<String, OBRManager>();
 	}
 
 	public boolean bundleExists(String bundleName) {
 		Bundle[] bundles = m_context.getBundles();
 		for (Bundle bundle : bundles) {
 			if (bundle.getSymbolicName() != null && bundle.getSymbolicName().equals(bundleName)) {
 				if (bundle.getState() == Bundle.ACTIVE || bundle.getState() == Bundle.STARTING) {
 					return true;
 				}
 				return false;
 			}
 		}
 		return false;
 	}
 
 	private void customizedRootModelLocation() {
 
 	}
 
 	private boolean deploy(Selected selected) {
 		boolean deployed = selected.obrManager.deployInstall(selected);
 		if (!deployed) {
 			System.err.print("could not install resource ");
 			ObrUtil.printRes(selected.resource);
 		}
 		return deployed;
 	}
 
 	@Override
 	public ComponentBundle findBundle(CompositeType compoType, String bundleSymbolicName, String componentName) {
 		if (bundleSymbolicName == null || componentName == null) {
 			return null;
 		}
 
 		// Find the composite OBRManager
 		OBRManager obrManager = searchOBRManager(compoType);
 		if (obrManager == null) {
 			return null;
 		}
 
 		return obrManager.lookForBundle(bundleSymbolicName, componentName);
 	}
 
 	private Component findByName(Component source, RelToResolve rel) {
 
 		CompositeType compoType = null;
 		if (source instanceof Instance) {
 			compoType = ((Instance) source).getComposite().getCompType();
 		}
 		// Find the composite OBRManager
 		OBRManager obrManager = searchOBRManager(compoType);
 		if (obrManager == null) {
 			logger.error("OBR: No context found for composite " + compoType);
 			return null;
 		}
 
 		// Relation rel = new RelationImpl(source, new
 		// ComponentReference(componentName), kind);
 		// ((RelationImpl) rel).computeFilters(source);
 		Selected selected = obrManager.lookFor(CST.CAPABILITY_COMPONENT, "(name=" + rel.getTarget().getName() + ")", rel);
 		fr.imag.adele.apam.Component c = installInstantiate(selected);
 		if (c == null) {
 			return null;
 		}
 		if (c.getKind() != rel.getTargetKind()) {
 			logger.debug("ERROR : " + rel.getTarget().getName() + " is found but is an " + rel.getTargetKind() + " not an " + c.getKind());
 		}
 
 		return c;
 	}
 
 	@Override
 	public Set<String> getCompositeRepositories(String compositeTypeName) {
 		Set<String> result = new HashSet<String>();
 		OBRManager obrmanager = getOBRManager(compositeTypeName);
 		if (obrmanager == null) {
 			return result;
 		}
 
 		for (Repository repository : obrmanager.getRepositories()) {
 			result.add(repository.getURI());
 		}
 		return result;
 	}
 
 	public String getDeclaredOSGiOBR() {
 		return m_context.getProperty(ObrUtil.OSGI_OBR_REPOSITORY_URL);
 	}
 
 	@Override
 	public String getName() {
 		return CST.OBRMAN;
 	}
 
 	// @Override
 	// public Instance resolveImpl(Component client, Implementation impl,
 	// Relation dep) {
 	// return null;
 	// }
 	//
 	// @Override
 	// public Set<Instance> resolveImpls(Component client, Implementation impl,
 	// Relation dep) {
 	// return null;
 	// }
 
 	public OBRManager getOBRManager(String compositeTypeName) {
 		return obrManagers.get(compositeTypeName);
 	}
 
 	@Override
 	public int getPriority() {
 		return 3;
 	}
 
 	// at the end
 	@Override
 	public void getSelectionPath(Component client, RelToResolve dep, List<RelationManager> involved) {
 		involved.add(involved.size(), this);
 	}
 
 	/**
 	 * Deploy and return the component, if possible. Null if failed. if : the
 	 * component we are looking for is existing (maybe arrived in the mean
 	 * time), return it the bundle does not exist currently : deploy, wait for
 	 * the component and return it the bundle is already deployed and active :
 	 * it is not the version we are looking for : do nothing and return false
 	 * wait for the component and return it the bundle is starting : wait for
 	 * the component and return it. the bundle is installed but is not started :
 	 * try to start it, wait for the component and return it, if failed, return
 	 * null
 	 * 
 	 * @param selected
 	 *            : the selected bundle and component(s)
 	 * @return
 	 */
 	private Component installInstantiate(Selected selected) {
 		if (selected == null) {
 			return null;
 		}
 
 		/*
 		 * If the component exists, maybe arrived in the mean time, or from
 		 * another bundle or in another version, do nothing
 		 */
 		fr.imag.adele.apam.Component c = CST.componentBroker.getComponent(selected.getComponentName());
 		if (c != null) {
 			return c;
 		}
 
 		/*
 		 * Check if the bundle is already existing
 		 */
 		Bundle[] bundles = m_context.getBundles();
 		Bundle theBundle = null;
 		String bundleName = selected.resource.getSymbolicName();
 		for (Bundle bundle : bundles) {
 			if (bundle.getSymbolicName() != null && bundle.getSymbolicName().equals(bundleName)) {
 				theBundle = bundle;
 				break;
 			}
 		}
 
 		/*
 		 * Normal case : bundle does not exist : deploy, wait and return the
 		 * component
 		 */
 		if (theBundle == null) {
 			if (!deploy(selected)) {
 				// deployment failed
 				return null;
 			}
 			return waitAndReturnComponent(selected);
 		}
 
 		/*
 		 * the bundle is already deployed and active : it is not the version we
 		 * are looking for. Do nothing and return false It may be active or
 		 * starting if started in parallel by another thread ... OK wait for the
 		 * component and return it.
 		 */
 		if (theBundle.getState() == Bundle.ACTIVE || theBundle.getState() == Bundle.STARTING) {
 			if (theBundle.getVersion().equals(selected.resource.getVersion())) {
 				return waitAndReturnComponent(selected);
 			}
 			logger.error("Bundle " + selected.getComponentName() + " is already installed under version " + theBundle.getVersion() + " while trying to deploy version " + selected.resource.getVersion());
 			return null;
 		}
 
 		/*
 		 * the bundle is installed but is not started : try to start it, wait
 		 * for the component and return it, if failed, return null
 		 */
 		if (theBundle.getState() == Bundle.INSTALLED || theBundle.getState() == Bundle.RESOLVED) {
 			try {
 				theBundle.start();
 			} catch (BundleException e) {
 				// Starting failed. No solution : cannot be deployed and cannot
 				// be started. Make as if failed
 				logger.info("The bundle " + theBundle.getSymbolicName() + " is installed but cannot be started!");
 				return null;
 			}
 
 			logger.info("The bundle " + theBundle.getSymbolicName() + " is installed and has been be started!");
 			return waitAndReturnComponent(selected);
 		}
 		return null;
 	}
 
 	@Override
 	public void newComposite(ManagerModel model, CompositeType compositeType) {
 		OBRManager obrManager;
 		if (model == null) { // if no model for the compositeType, set the root
 			// composite model
 			obrManager = searchOBRManager(compositeType);
 		} else {
 			try {// try to load the compositeType model
 				Properties obrModel = new Properties();
 				obrModel.load(model.getURL().openStream());
 				obrManager = new OBRManager(this, compositeType.getName(), repoAdmin, obrModel);
 			} catch (IOException e) {// if impossible to load the model for the
 				// compositeType, set the root composite
 				// model
 				logger.error("Invalid OBRMAN Model. Cannot be read stream " + model.getURL(), e.getCause());
 				obrManager = searchOBRManager(compositeType);
 			}
 		}
 		obrManagers.put(compositeType.getName(), obrManager);
 	}
 
 	@Override
 	public void notifySelection(Component client, ResolvableReference resName, String depName, Implementation impl, Instance inst, Set<Instance> insts) {
 		// Do not care
 	}
 
 	// interface manager
 	private Component resolveByResource(Component source, RelToResolve dep) {
 		ResolvableReference resource = dep.getTarget();
 		CompositeType compoType = null;
 		if (source instanceof Instance) {
 			compoType = ((Instance) source).getComposite().getCompType();
 		}
 
 		// Find the composite OBRManager
 		OBRManager obrManager = searchOBRManager(compoType);
 		if (obrManager == null) {
 			return null;
 		}
 
 		fr.imag.adele.obrMan.internal.OBRManager.Selected selected = null;
 		if (resource instanceof SpecificationReference) {
 			selected = obrManager.lookFor(CST.CAPABILITY_COMPONENT, "(provide-specification*>" + resource.as(SpecificationReference.class).getName() + ")", dep);
 		}
 		if (resource instanceof InterfaceReference) {
 			selected = obrManager.lookFor(CST.CAPABILITY_COMPONENT, "(provide-interfaces*>" + resource.as(InterfaceReference.class).getJavaType() + ")", dep);
 		}
 		if (resource instanceof MessageReference) {
 			selected = obrManager.lookFor(CST.CAPABILITY_COMPONENT, "(provide-messages*>" + resource.as(MessageReference.class).getJavaType() + ")", dep);
 		}
 		if (selected != null) {
 			return installInstantiate(selected);
 		}
 		return null;
 	}
 
 	@Override
 	public Resolved<?> resolveRelation(Component client, RelToResolve dep) {
 		Component ret = null;
 
 		// It is either a resolution (spec -> instance) or a resource reference
 		// (interface or message)
 		if ((dep.getTarget() instanceof ResourceReference) || (((ComponentReference<?>) dep.getTarget()).getKind() == ComponentKind.SPECIFICATION && dep.getTargetKind() != ComponentKind.SPECIFICATION)) {
 			ret = resolveByResource(client, dep);
 		} else {
 			ret = findByName(client, dep);
 		}
 
 		// Not found
 		if (ret == null) {
 			return null;
 		}
 
 		/*
 		 * Check if the found component if of the right kind. If requires an
 		 * Instance, and found an implementation, it is ok, but build the
 		 * "toInstantiate" result.
 		 */
 		boolean badKind = (ret.getKind() != dep.getTargetKind());
 		if (badKind) {
 			logger.debug("Looking for " + dep.getTargetKind() + " but found " + ret);
 			// It is Ok to return an implem when an instance is required. The
 			// resolver will instantiate.
 			if (!(ret.getKind() == ComponentKind.IMPLEMENTATION && dep.getTargetKind() == ComponentKind.INSTANCE)) {
 				logger.error("invalide return from OBR : expected " + dep.getTargetKind() + " got " + ret.getKind());
 				return null;
 			}
 		}
 
 		switch (dep.getTargetKind()) {
 		case SPECIFICATION:
 			return new Resolved<Specification>((Specification) ret);
 		case IMPLEMENTATION:
 			if (badKind) {
 				return new Resolved<Implementation>((Implementation) ret, true);
 			}
 			return new Resolved<Implementation>((Implementation) ret);
 		case INSTANCE:
 			if (badKind) {
 				return new Resolved<Instance>((Implementation) ret, true);
 			}
 			return new Resolved<Instance>((Instance) ret);
 		case COMPONENT: // Not allowed
 		}
 		return null;
 	}
 
 	private OBRManager searchOBRManager(CompositeType compoType) {
 		OBRManager obrManager = null;
 
 		// in the case of root composite, compoType = null
 		if (compoType != null) {
 			obrManager = obrManagers.get(compoType.getName());
 		}
 
 		// Use the root composite if the model is not specified
 		if (obrManager == null) {
 			obrManager = obrManagers.get(CST.ROOT_COMPOSITE_TYPE);
 			if (obrManager == null) { // If the root manager was never been
 				// initialized
 				// lookFor root.OBRMAN.cfg and create obrmanager for the root
 				// composite in a customized location
 				String rootModelurl = m_context.getProperty(ObrUtil.ROOT_MODEL_URL);
 				try {// try to load root obr model from the customized location
 					if (rootModelurl != null) {
 						URL urlModel = (new File(rootModelurl)).toURI().toURL();
 						setInitialConfig(urlModel);
 					} else {
 						Properties obrModel = new Properties();
 						customizedRootModelLocation();
 						obrModel.put(ObrUtil.LOCAL_MAVEN_REPOSITORY, "true");
 						obrModel.put(ObrUtil.DEFAULT_OSGI_REPOSITORIES, "true");
 						obrManager = new OBRManager(this, CST.ROOT_COMPOSITE_TYPE, repoAdmin, obrModel);
 						obrManagers.put(CST.ROOT_COMPOSITE_TYPE, obrManager);
 					}
 				} catch (Exception e) {// if failed to load customized location,
 					// set default properties for the root
 					// model
 					logger.error("Invalid Root URL Model. Cannot be read stream " + rootModelurl, e.getCause());
 					Properties obrModel = new Properties();
 					customizedRootModelLocation();
 					obrModel.put(ObrUtil.LOCAL_MAVEN_REPOSITORY, "true");
 					obrModel.put(ObrUtil.DEFAULT_OSGI_REPOSITORIES, "true");
 					obrManager = new OBRManager(this, CST.ROOT_COMPOSITE_TYPE, repoAdmin, obrModel);
 					obrManagers.put(CST.ROOT_COMPOSITE_TYPE, obrManager);
 				}
 			}
 		}
 		return obrManager;
 	}
 
 	@Override
 	public void setInitialConfig(URL modellocation) throws IOException {
 		Properties obrModel = new Properties();
 		if (modellocation != null) {
 			obrModel.load(modellocation.openStream());
 			OBRManager obrManager = new OBRManager(this, CST.ROOT_COMPOSITE_TYPE, repoAdmin, obrModel);
 			obrManagers.put(CST.ROOT_COMPOSITE_TYPE, obrManager);
 		} else {
 			throw new IOException("URL is null");
 		}
 	}
 
 	public void start() {
 		// to load the initial OBR before to register
 		// newComposite(null, CompositeTypeImpl.getRootCompositeType()) ;
 		ApamManagers.addRelationManager(this, 3);
 		// logger.info("[OBRMAN] started");
 	}
 
 	public void stop() {
 		ApamManagers.removeRelationManager(this);
 		obrManagers.clear();
 		// logger.info("[OBRMAN] stopped");
 	}
 
 	/**
 	 * Update resources from repositories
 	 * 
 	 * @param compositeName
 	 *            the name of the composite to update or *
 	 */
 	@Override
 	public boolean updateRepos(String compositeName) {
 		if (compositeName == null) {
 			return false;
 		}
 		OBRManager obrmanager = null;
 		if (compositeName.equals("*")) {
 			for (OBRManager obrManager2 : obrManagers.values()) {
 				obrManager2.updateListOfResources(repoAdmin);
 			}
 			return true;
 		} else if (compositeName.equals("root")) {
 			obrmanager = getOBRManager(CST.ROOT_COMPOSITE_TYPE);
 		} else {
 			obrmanager = getOBRManager(compositeName);
 		}
 		if (obrmanager == null) {
 			return false;
 		}
 		obrmanager.updateListOfResources(repoAdmin);
 		return true;
 	}
 
 	private Component waitAndReturnComponent(Selected selected) {
 		// waiting for the component to be ready in Apam.
 		Component c = CST.componentBroker.getWaitComponent(selected.getComponentName(), timeout);
 
 		/*
 		 * In fact, we are waiting for its instances; they are "arriving"; wait
 		 * for them
 		 */
 		if (c != null && c instanceof Implementation) {
 			for (String instanceName : selected.getInstancesOfSelectedImpl()) {
 				CST.componentBroker.getWaitComponent(instanceName, timeout);
 			}
 		}
 		return c;
 	}
 
 }
