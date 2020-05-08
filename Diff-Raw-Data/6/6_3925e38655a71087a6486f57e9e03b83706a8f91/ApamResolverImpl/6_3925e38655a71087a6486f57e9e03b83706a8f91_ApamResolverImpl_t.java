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
 package fr.imag.adele.apam.impl;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import fr.imag.adele.apam.ApamManagers;
 import fr.imag.adele.apam.ApamResolver;
 import fr.imag.adele.apam.CST;
 import fr.imag.adele.apam.Component;
 import fr.imag.adele.apam.Composite;
 import fr.imag.adele.apam.CompositeType;
 import fr.imag.adele.apam.Implementation;
 import fr.imag.adele.apam.Instance;
 import fr.imag.adele.apam.ManagerModel;
 import fr.imag.adele.apam.RelToResolve;
 import fr.imag.adele.apam.RelationDefinition;
 import fr.imag.adele.apam.RelationManager;
 import fr.imag.adele.apam.Resolved;
 import fr.imag.adele.apam.Specification;
 import fr.imag.adele.apam.declarations.ComponentKind;
 import fr.imag.adele.apam.declarations.ComponentReference;
 import fr.imag.adele.apam.declarations.CreationPolicy;
 import fr.imag.adele.apam.declarations.ImplementationDeclaration;
 import fr.imag.adele.apam.declarations.ImplementationReference;
 import fr.imag.adele.apam.declarations.InstanceReference;
 import fr.imag.adele.apam.declarations.InterfaceReference;
 import fr.imag.adele.apam.declarations.MessageReference;
 import fr.imag.adele.apam.declarations.RelationPromotion;
 import fr.imag.adele.apam.declarations.ResolvePolicy;
 import fr.imag.adele.apam.declarations.SpecificationReference;
 
 public class ApamResolverImpl implements ApamResolver {
 
 	/**
 	 * Impl is either unused or deployed (and therefore also unused). It becomes
 	 * embedded in compoType. If unused, remove from unused list.
 	 * 
 	 * @param compoType
 	 * @param impl
 	 */
 	private static void deployedImpl(Component source, Component comp, boolean deployed) {
 		// We take care only of implementations
 		if (!(comp instanceof Implementation)) {
 			return;
 		}
 
 		Implementation impl = (Implementation) comp;
 		// it was not deployed
 		if (!deployed && impl.isUsed()) {
 			logger.info(" : selected " + impl);
 			return;
 		}
 
 		CompositeType compoType;
 		if (source instanceof Instance) {
 			compoType = ((Instance) source).getComposite().getCompType();
 		} else if (source instanceof Implementation) {
 			compoType = ((Implementation) source).getInCompositeType().iterator().next();
 		} else {
 			logger.error("Should not call deployedImpl on a source Specification " + source);
 			// TODO in which composite to put it. Still in root ?
 			return;
 		}
 		((CompositeTypeImpl) compoType).deploy(impl);
 
 		// it is deployed or was never used so far
 		if (impl.isUsed()) {
 			logger.info(" : logically deployed " + impl);
 		} else {// it was unused so far.
 			((ComponentImpl) impl).setFirstDeployed(compoType);
 			if (deployed) {
 				logger.info(" : deployed " + impl);
 			} else {
 				logger.info(" : was here, unused " + impl);
 			}
 		}
 	}
 
 	private APAMImpl apam;
 
 	static Logger logger = LoggerFactory.getLogger(ApamResolverImpl.class);
 
 	/**
 	 * The current state of the resolver. The resolver can be temporarily
 	 * disabled, for instance for waiting for the installation of required
 	 * managers;
 	 * 
 	 */
 	private boolean enabled = false;
 
 	/**
 	 * A description of the condition that must be met to enable the resolver
 	 * again.
 	 */
 	private String condition = "resolver startup";
 
 	/**
 	 * If the resolver is disabled, the time at which it will be automatically
 	 * enabled, even if the condition is not met. This is not an delay, but the
 	 * actual future time.
 	 */
 	private long maxDisableTime = 0L;
 
 	public ApamResolverImpl(APAMImpl theApam) {
 		this.apam = theApam;
 	}
 
 	/**
 	 * Verifies if the resolver is enabled. If it is disabled blocks the calling
 	 * thread waiting for the enable condition.
 	 */
 	private synchronized void checkEnabled() {
 		while (!this.enabled) {
 			try {
 
 				/*
 				 * Verify if the disable timeout has expired, in that case
 				 * simply enable the resolver again.
 				 */
 				long currentTime = System.currentTimeMillis();
 				if (currentTime > maxDisableTime) {
 
 					System.err.println("APAM RESOLVER resuming resolution, condition did not happen: " + condition);
 					enable();
 					return;
 				}
 
 				System.err.println("APAM RESOLVER waiting for: " + condition);
 				wait(this.maxDisableTime - currentTime);
 
 			} catch (InterruptedException ignored) {
 			}
 		}
 	}
 
 	/**
 	 * Disables the resolver until the specified condition is met. If the
 	 * condition is not signaled before the specified timeout, the resolver will
 	 * be automatically enabled.
 	 */
 	public synchronized void disable(String condition, long timeout) {
 		this.enabled = false;
 		this.condition = condition;
 		this.maxDisableTime = System.currentTimeMillis() + timeout;
 
 	}
 
 	/**
 	 * Enables the resolver after the condition is met
 	 */
 	public synchronized void enable() {
 		this.enabled = true;
 		this.condition = null;
 		this.maxDisableTime = 0L;
 
 		this.notifyAll();
 	}
 
 	private Component findByName(Component client, ComponentReference<?> targetComponent, ComponentKind targetKind) {
 		if (client == null) {
 			client = CompositeImpl.getRootInstance();
 			// hummmm patch .... TODO
 			if (targetComponent.getName().equals(CST.ROOT_COMPOSITE_TYPE)) {
 				return CompositeTypeImpl.getRootCompositeType();
 			}
 		}
 
 		// CompositeType compoType = CompositeTypeImpl.getRootCompositeType();
 
 		RelationDefinition rel = new RelationDefinitionImpl(targetComponent, client.getKind(), targetKind, null, null);
 		Resolved<?> res = resolveLink(client, rel);
 		if (res == null) {
 			return null;
 		}
 		return res.singletonResolved;
 	}
 
 	@Override
 	public Component findComponentByName(Component client, String name) {
 		Component ret = findImplByName(client, name);
 		if (ret != null) {
 			return ret;
 		}
 		ret = findSpecByName(client, name);
 		if (ret != null) {
 			return ret;
 		}
 		return findInstByName(client, name);
 	}
 
 	@Override
 	public Implementation findImplByName(Component client, String implName) {
 		return (Implementation) findByName(client, new ImplementationReference<ImplementationDeclaration>(implName), ComponentKind.IMPLEMENTATION);
 	}
 
 	@Override
 	public Instance findInstByName(Component client, String instName) {
 		return (Instance) findByName(client, new InstanceReference(instName), ComponentKind.INSTANCE);
 	}
 
 	// /**
 	// * Before to resolve an implementation (i.e. to select one of its
 	// instance),
 	// * this method is called to know which managers are involved, and what are
 	// * the constraints and preferences set by the managers to this resolution.
 	// *
 	// * @param compTypeFrom
 	// * : the origin of this resolution.
 	// * @param impl
 	// * : the implementation to resolve.
 	// * @param constraints
 	// * : the constraints added by the managers. A (empty) set must be
 	// * provided as parameter.
 	// * @param preferences
 	// * : the preferences added by the managers. A (empty) list must
 	// * be provided as parameter.
 	// * @return : the managers that will be called for that resolution.
 	// */
 	// private List<RelationManager> computeSelectionPath(Component source,
 	// RelToResolve relToResolve) {
 	//
 	// /*
 	// * Get the list of external managers.
 	// *
 	// * NOTE Notice that we invoke getSelctionPath on all managers (even if
 	// resolve policy
 	// * is specified EXTERNAL. In this way, managers can influence resolution,
 	// by adding
 	// * constraints, even if they do not perform resolution themselves.
 	// *
 	// */
 	// List<RelationManager> externalPath = new ArrayList<RelationManager>();
 	// for (RelationManager relationManager :
 	// ApamManagers.getRelationManagers()) {
 	// relationManager.getSelectionPath(source, relToResolve, externalPath);
 	// }
 	//
 	// /*
 	// * Get the list of all managers, core and external
 	// */
 	// List<RelationManager> selectionPath = new ArrayList<RelationManager>();
 	//
 	// selectionPath.add(0, apam.getApamMan());
 	// selectionPath.add(0, apam.getUpdateMan());
 	//
 	//
 	// /*
 	// * If resolve = exist or internal, only predefined managers must be called
 	// */
 	//
 	// boolean resolveExternal = relToResolve.getResolve() ==
 	// ResolvePolicy.EXTERNAL ;
 	// if (resolveExternal) {
 	// selectionPath.addAll(externalPath);
 	// }
 	//
 	// return selectionPath;
 	// }
 
 	@Override
 	public Specification findSpecByName(Component client, String specName) {
 		return (Specification) findByName(client, new SpecificationReference(specName), ComponentKind.SPECIFICATION);
 	}
 
 	// if the instance is unused, it will become the main instance of a new
 	// composite.
 	private Composite getClientComposite(Instance mainInst) {
 
 		if (mainInst.equals(CompositeImpl.getRootInstance())) {
 			return CompositeImpl.getRootInstance();
 		}
 
 		if (mainInst.isUsed()) { // || (mainInst instanceof Composite)
 			return mainInst.getComposite();
 		}
 
 		/*
 		 * We are resolving a reference from an unused client instance. We
 		 * automatically build a new composite to create a context of execution.
 		 * This allows to use Apam without requiring the explicit definition of
 		 * composites, just instantiating any implementation.
 		 */
 
 		Implementation mainComponent = mainInst.getImpl();
 		String applicationName = mainComponent.getName() + "_Appli";
 		// SpecificationReference specification =
 		// mainComponent.getImplDeclaration().getSpecification();
 		Set<ManagerModel> models = new HashSet<ManagerModel>();
 
 		logger.debug("creating a dummy root composite type " + applicationName + " to contain unused " + mainInst);
 
 		CompositeType application = apam.createCompositeType(null,
 				// applicationName, specification != null ? specification.getName() :
 				// null, mainComponent.getName(),
 				applicationName, null, mainComponent.getName(), models, null);
 
 		/*
 		 * Create an instance of the application with the specified main
 		 */
 		Map<String, String> initialProperties = new HashMap<String, String>();
 		initialProperties.put(CST.APAM_MAIN_INSTANCE, mainInst.getName());
 		return (Composite) application.createInstance(null, initialProperties);
 	}
 
 	//	/**
 	//	 * Only instance have a well-defined and unique enclosing composite (type
 	//	 * and instance). Promotion control will apply only on sources that are
 	//	 * instances; but for relations, targetType can be any component. We will
 	//	 * look for a relation at the composite level, that matches the target
 	//	 * (target and targetType), whatever the Id, composite rel cardinality must
 	//	 * be multiple if the relation is multiple. If so, it is a promotion. The
 	//	 * composite relation is resolved, then the source relation is resolved as a
 	//	 * subset of the composite rel. The client becomes the embedding composite;
 	//	 * visibility and scope become the one of the embedding composite
 	//	 * 
 	//	 * Note that is more than one composite relation matches the source
 	//	 * relation, one arbitrarily is selected. To closely control promotions, use
 	//	 * the tag "promotion" in the composite relation definition.
 	//	 * 
 	//	 * 
 	//	 * @param client
 	//	 * @param relDef
 	//	 *            definition
 	//	 * @return the composite relation from the composite.
 	//	 */
 	//	private RelationDefinition getPromotionRel(Instance client, RelationDefinition relDef) {
 	//		// if (relation.getIdentifier() == null) // Not a relation
 	//		// return null;
 	//
 	//		Composite composite = client.getComposite();
 	//
 	//		// if (composite.getDeclaration() == null) {
 	//		// return null;
 	//		// }
 	//
 	//		// look if a promotion is explicitly declared for that client component
 	//		// <promotion implementation="A" relation="clientDep" to="compoDep" />
 	//		// <promotion specification="SA" relation="clientDep" to="compoDep" />
 	//		for (RelationPromotion promo : composite.getCompType().getCompoDeclaration().getPromotions()) {
 	//			if (!promo.getContentRelation().getIdentifier().equals(relDef.getName())) {
 	//				continue; // this promotion is not about our relation (not
 	//				// "clientDep")
 	//			}
 	//
 	//			String sourceName = promo.getContentRelation().getDeclaringComponent().getName();
 	//			// sourceName = "SA" or "A"
 	//			if (sourceName.equals(client.getImpl().getName()) || sourceName.equals(client.getSpec().getName())) {
 	//				// We found the right promotion from client side.
 	//				// Look for the corresponding composite relation "compoDep"
 	//				String toName = promo.getCompositeRelation().getIdentifier();
 	//				RelationDefinition foundPromo = composite.getCompType().getRelation(toName);
 	//				// if (compoDep.getIdentifier().equals(toName)) {
 	//				// We found the composite side. It is an explicit promotion. It
 	//				// should match.
 	//				if (foundPromo.matchRelation(client, foundPromo)) {
 	//					return foundPromo;
 	//				}
 	//				logger.error("Promotion is invalid. relation " + promo.getContentRelation().getIdentifier() + " of component " + sourceName + " does not match the composite relation " + foundPromo);
 	//				return null;
 	//			}
 	//		}
 	//
 	//		// Look if a relation, defined in the composite, matches the current
 	//		// relation
 	//		// Do no check composite
 	//		Component group = composite;
 	//		while (group != null) {
 	//			for (RelationDefinition compoDep : group.getLocalRelations()) {
 	//				if (relDef.matchRelation(client, compoDep)) {
 	//					return compoDep;
 	//				}
 	//			}
 	//			group = group.getGroup();
 	//		}
 	//		return null;
 	//	}
 
 	private Resolved<?> checkImplicitPromotion(Instance client, RelationDefinition relDef, Resolved<?> resolved, RelToResolve relToResolve, Composite composite, Component source) {
 
 		// Look if a relation, defined in the composite, matches the current
 		// relation
 		// Do no check composite
 		Component group = composite;
		
		RelToResolve relToResolveIntrinsic = new RelToResolveImpl(client,relDef);
		
 		while (group != null) {
 			for (RelationDefinition compoDep : group.getLocalRelations()) {
 				if (relDef.matchRelation(client, compoDep)) {
					resolved = resolvePromotion(compoDep, resolved, relToResolveIntrinsic, composite, source);
 				}
 			}
 			group = group.getGroup();
 		}
 		return resolved;
 	}
 
 	private Resolved<?> checkExplicitPromotion(Instance client, RelationDefinition relDef, Resolved<?> resolved, RelToResolve relToResolve, Composite composite, Component source) {
 		boolean isExplicitPromotion = false;
 
 		// look if a promotion is explicitly declared for that client component
 		// <promotion implementation="A" relation="clientDep" to="compoDep" />
 		// <promotion specification="SA" relation="clientDep" to="compoDep" />
 		for (RelationPromotion promo : composite.getCompType().getCompoDeclaration().getPromotions()) {
 			if (!promo.getContentRelation().getIdentifier().equals(relDef.getName())) {
 				continue; // this promotion is not about our relation (not
 				// "clientDep")
 			}
 
 			String sourceName = promo.getContentRelation().getDeclaringComponent().getName();
 			// sourceName = "SA" or "A"
 			if (sourceName.equals(client.getImpl().getName()) || sourceName.equals(client.getSpec().getName())) {
 				// We found the right promotion from client side.
 				// Look for the corresponding composite relation "compoDep"
 				String toName = promo.getCompositeRelation().getIdentifier();
 				RelationDefinition foundPromo = composite.getCompType().getRelation(toName);
 				isExplicitPromotion = true;
 				// if (compoDep.getIdentifier().equals(toName)) {
 				// We found the composite side. It is an explicit promotion.
 				// It should match.
 				if (foundPromo.matchRelation(client, foundPromo)) {
 					resolved = resolvePromotion(foundPromo, resolved, relToResolve, composite, source);
 				} else {
 					logger.error("Promotion is invalid. relation " + promo.getContentRelation().getIdentifier() + " of component " + sourceName + " does not match the composite relation " + foundPromo);
 				}
 			}
 		}
 		if (isExplicitPromotion) {
 			if (resolved == null) {
 				resolved = new Resolved<Component>(null, false);
 			}
 			return resolved;
 		} else {
 			return null;
 		}
 	}
 
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	private Resolved<?> resolvePromotion(RelationDefinition promotionRelation, Resolved<?> resolved, RelToResolve relToResolve, Composite compo, Component source) {
 		// if it is a promotion, get the composite relation targets.
 		boolean promoHasConstraints = false;
 		boolean isPromotion = false;
 
 		if (promotionRelation != null) {
 			// Check existing links
 			isPromotion = true;
 			promoHasConstraints = promotionRelation.hasConstraints();
 			if (promotionRelation.isMultiple()) {
 				if (resolved == null) {
 					resolved = new Resolved<Component>(compo.getLinkDests(promotionRelation.getName()));
 				} else {
 					resolved = resolved.merge(new Resolved(compo.getLinkDests(promotionRelation.getName())));
 				}
 
 			} else {
 				resolved = new Resolved<Component>(compo.getLinkDest(promotionRelation.getName()));
 			}
 
 			// Maybe the composite did not resolved that relation so far.
 			if (resolved.isEmpty()) {
 				resolved = resolveLink(compo, promotionRelation);
 			}
 
 			// Select the sub-set that matches the dep constraints. No
 			// source visibility control (null).
 			// Adds the manager constraints and compute filters
 			if (resolved != null && !resolved.isEmpty()) {
 				// computeSelectionPath(source, rel);
 				resolved = relToResolve.getResolved(resolved, true);
 			}
 
 		}
 
 		/*
 		 * It is resolved.
 		 */
 		if (resolved != null) {
 			if (resolved.singletonResolved != null) {
 				source.createLink(resolved.singletonResolved, relToResolve, relToResolve.hasConstraints() || promoHasConstraints, isPromotion);
 				return resolved;
 			}
 			for (Object target : resolved.setResolved) {
 				source.createLink((Component) target, relToResolve, relToResolve.hasConstraints() || promoHasConstraints, isPromotion);
 			}
 		}
 		return resolved;
 	}
 
 	/**
 	 * Performs a complete resolution of the relation, or resolution.
 	 * 
 	 * The managers is asked to find the "right" component.
 	 * 
 	 * @param client
 	 *            the instance calling implem (and where to create
 	 *            implementation ans instances if needed). Cannot be null.
 	 * @param relToResolve
 	 *            a relation declaration containing the type and name of the
 	 *            relation target. It can be -the specification Name (new
 	 *            SpecificationReference (specName)) -an implementation name
 	 *            (new ImplementationRefernece (name) -an interface name (new
 	 *            InterfaceReference (interfaceName)) -a message name (new
 	 *            MessageReference (dataTypeName))
 	 * @return the component(s) if resolved, null otherwise
 	 */
 	private Resolved<?> resolveByManagers(RelToResolve relToResolve) {
 
 		/*
 		 * Get the list of external managers.
 		 * 
 		 * NOTE Notice that we invoke getSelectionPath on all managers (even if
 		 * resolve policy is specified EXTERNAL). In this way, managers can
 		 * influence resolution, by adding constraints, even if they do not
 		 * perform resolution themselves.
 		 * 
 		 * TODO We should have two separate methods in managers: one for adding
 		 * constraints and another for actually performing resolution.
 		 */
 		Component source = relToResolve.getLinkSource();
 		// RelToResolve relToResolve = new RelToResolveImpl (source, relation);
 		List<RelationManager> externalPath = new ArrayList<RelationManager>();
 		for (RelationManager relationManager : ApamManagers.getRelationManagers()) {
 			relationManager.getSelectionPath(source, relToResolve, externalPath);
 		}
 		// Compute filters once for all, and make it final
 		((RelToResolveImpl) relToResolve).computeFilters();
 
 		/*
 		 * Get the list of all managers
 		 */
 		List<RelationManager> selectionPath = new ArrayList<RelationManager>();
 
 		selectionPath.add(0, apam.getApamMan());
 		selectionPath.add(0, apam.getUpdateMan());
 		if (apam.getApamMan() == null) {
 			throw new RuntimeException("Error while get of ApamMan");
 		}
 		if (apam.getUpdateMan() == null) {
 			throw new RuntimeException("Error while get of UpdateMan");
 		}
 
 		/*
 		 * If resolve = exist or internal, only predefined managers must be
 		 * called
 		 */
 
 		boolean resolveExternal = (relToResolve.getResolve() == ResolvePolicy.EXTERNAL);
 		if (resolveExternal) {
 			selectionPath.addAll(externalPath);
 		}
 
 		if (!relToResolve.isRelation()) { // It is a find
 			logger.info("Looking for " + relToResolve.getTargetKind() + " " + relToResolve.getTarget().getName());
 		} else {
 			logger.info("Resolving " + relToResolve);
 		}
 
 		Resolved<?> res = null;
 		boolean deployed = false;
 
 		boolean resolveExist = relToResolve.getResolve() == ResolvePolicy.EXIST;
 		String mess = "";
 		for (RelationManager manager : selectionPath) {
 			if (manager == null) {
 				throw new RuntimeException("Manager is null, SelectionPath " + selectionPath);
 			}
 			if (manager.getName() == null) {
 				throw new RuntimeException("Manager : " + manager + ", manager name is null");
 			}
 			if (!manager.getName().equals(CST.APAMMAN) && !manager.getName().equals(CST.UPDATEMAN)) {
 				deployed = true;
 			}
 			// logger.debug(manager.getName() + "  ");
 			mess += manager.getName() + "  ";
 			// Does the real job
 			res = manager.resolveRelation(source, relToResolve);
 			if (res == null || res.isEmpty()) {
 				// This manager did not found a solution, try the next manager
 				continue;
 			}
 
 			/*
 			 * a manager succeeded to find a solution If an unused or deployed
 			 * implementation. Can be into singleton or in toInstantiate if an
 			 * instance is required
 			 */
 			Component depl = (res.toInstantiate != null) ? res.toInstantiate : res.singletonResolved;
 			deployedImpl(source, depl, deployed);
 
 			/*
 			 * If an implementation is returned as "toInstantiate" it has to be
 			 * instantiated
 			 */
 			if (res.toInstantiate != null) {
 				if (relToResolve.getTargetKind() != ComponentKind.INSTANCE) {
 					logger.error(mess + "Invalid Resolved value. toInstantiate is set, but target kind is not an Instance");
 					continue;
 				}
 
 				/*
 				 * If resolveExist, we cannot instanciate.
 				 */
 				if (resolveExist) {
 					logger.error(mess + "resolve=\"exist\" but only an implementations was found : " + res.toInstantiate + " cannot be instantiated. Resolve failed.");
 					continue;
 				}
 
 				Composite compo = (source instanceof Instance) ? ((Instance) source).getComposite() : CompositeImpl.getRootInstance();
 				Instance inst = res.toInstantiate.createInstance(compo, null);
 				if (inst == null) { // may happen if impl is non instantiable
 					logger.error(mess + "Failed creating instance of " + res.toInstantiate);
 					continue;
 				}
 
 				if (!relToResolve.matchRelationConstraints(inst)) {
 					logger.debug(mess + " Instantiated instance " + inst + " does not match the constraints");
 					((ComponentImpl) inst).unregister();
 					continue;
 				}
 
 				logger.info(mess + "Instantiated " + inst);
 				if (relToResolve.isMultiple()) {
 					Set<Instance> insts = new HashSet<Instance>();
 					insts.add(inst);
 					return new Resolved<Instance>(insts);
 				} else {
 					return new Resolved<Instance>(inst);
 				}
 			}
 
 			/*
 			 * Because managers can be third party, we cannot trust them. Verify
 			 * that the result is correct.
 			 */
 			if (relToResolve.isMultiple()) {
 				if (res.setResolved == null || res.setResolved.isEmpty()) {
 					logger.info(mess + "manager " + manager + " returned an empty result. Should be null.");
 					continue;
 				}
 				if (((Component) res.setResolved.iterator().next()).getKind() != relToResolve.getTargetKind()) {
 					logger.error(mess + "Manager " + manager + " returned objects of the bad type for relation " + relToResolve);
 					continue;
 				}
 				logger.info(mess + "Selected : " + res.setResolved);
 				return res;
 			}
 
 			// Result is a singleton
 			if (res.singletonResolved == null) {
 				logger.info(mess + "manager " + manager + " returned an empty result. ");
 				continue;
 			}
 			if (res.singletonResolved.getKind() != relToResolve.getTargetKind()) {
 				logger.error(mess + "Manager " + manager + " returned objects of the bad type for relation " + relToResolve);
 				continue;
 			}
 			logger.info(mess + "Selected : " + res.singletonResolved);
 			return res;
 		}
 		// No solution found
 		logger.debug(mess + " : not found");
 		return null;
 	}
 
 	@Override
 	public Instance resolveImpl(Component client, Implementation impl, Set<String> constraints, List<String> preferences) {
 		if (client == null) {
 			client = CompositeImpl.getRootInstance();
 		}
 
 		@SuppressWarnings("rawtypes")
 		// RelToResolve dep = new RelToResolveImpl(new
 		// ImplementationReference(impl.getName()), client.getKind(),
 		// ComponentKind.INSTANCE, constraints, preferences);
 		RelationDefinition dep = new RelationDefinitionImpl(new ImplementationReference(impl.getName()), client.getKind(), ComponentKind.INSTANCE, constraints, preferences);
 
 		Resolved<?> resolve = resolveLink(client, dep);
 		if (resolve == null) {
 			return null;
 		}
 		return (Instance) resolve.setResolved;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public Set<Instance> resolveImpls(Component client, Implementation impl, Set<String> constraints) {
 		if (client == null) {
 			client = CompositeImpl.getRootInstance();
 		}
 
 		@SuppressWarnings("rawtypes")
 		// RelToResolve dep = new RelToResolveImpl(new
 		// ImplementationReference(impl.getName()), client.getKind(),
 		// ComponentKind.INSTANCE, constraints, null);
 		RelationDefinition dep = new RelationDefinitionImpl(new ImplementationReference(impl.getName()), client.getKind(), ComponentKind.INSTANCE, constraints, null);
 
 		Resolved<?> resolve = resolveLink(client, dep);
 		if (resolve == null) {
 			return null;
 		}
 		return (Set<Instance>) resolve.setResolved;
 	}
 
 	/**
 	 * The central method for the resolver.
 	 */
 	@Override
 	public Resolved<?> resolveLink(Component source2, RelationDefinition rel) {
 
 		/*
 		 * verify the resolver is actually ready to work (all managers are
 		 * present)
 		 */
 		checkEnabled();
 
 		if (source2 == null || rel == null) {
 			logger.error("missing client or relation ");
 			return null;
 		}
 
 		/*
 		 * If manual, the resolution must fail silently
 		 */
 		boolean createManual = rel.getCreation() == CreationPolicy.MANUAL;
 		if (createManual) {
 			return null;
 		}
 
 		Component source = rel.getRelSource(source2);
 		if (source == null) {
 			logger.error("Component source not at the right level; found " + source2 + " expected " + rel.getSourceKind());
 			return null;
 		}
 
 		Composite compo = null;
 		if (source instanceof Instance && rel.isRelation()) {
 			compo = getClientComposite((Instance) source);
 		}
 
 		// creates an relToResolve only considering the relation. Not completely
 		// initialized.
 		RelToResolve relToResolve = new RelToResolveImpl(source, rel);
 		// Invoke managers for resolution, add mng constraints and compute
 		// relToResolve
 
 		Resolved<?> resolved = null; //new Resolved<Component>(null,false);
 
 		if (compo != null && source instanceof Instance) {
 			resolved = checkExplicitPromotion((Instance) source, rel, resolved, relToResolve, compo, source);
 		}
 
 		if (resolved == null) {
 			//			resolved = null;
 			resolved = this.resolveByManagers(relToResolve);
 			resolved = resolvePromotion(null, resolved, relToResolve, compo, source);
 			if (compo != null && source instanceof Instance) {
 				resolved = checkImplicitPromotion((Instance) source, rel, resolved, relToResolve, compo, source);
 			}
 			//			return resolveLinkEmpty(rel, resolved, relToResolve, source);
 			//
 			//		} else {
 		}
 
 		return resolveLinkEmpty(rel, resolved, relToResolve, source);
 	}
 
 	private Resolved<?> resolveLinkEmpty(RelationDefinition rel, Resolved<?> resolved, RelToResolve relToResolve, Component source) {
 		/*
 		 * If managers could not resolve and relation cannot be promoted, give a
 		 * chance to failure manager
 		 */
 		if (resolved == null || resolved.isEmpty()) {
 			resolved = apam.getFailedResolutionManager().resolveRelation(source, relToResolve);
 		}
 
 		/*
 		 * If failure manager could not recover, just give up
 		 */
 		if (resolved == null || resolved.isEmpty()) {
 			if (rel.getName().isEmpty())
 				logger.error("Failed to resolve " + rel.getTarget().getName() + " from " + source );
 			else
 				logger.error("Failed to resolve " + rel.getTarget().getName() + " from " + source + "(relation " + rel.getName() + ")");
 			return null;
 		}
 
 		return resolved;
 	}
 
 	/**
 	 * An APAM client instance requires to be wired with one or all the
 	 * instances that satisfy the relation. WARNING : in case of interface or
 	 * message relation , since more than one specification can implement the
 	 * same interface, any specification implementing at least the provided
 	 * interface (technical name of the interface) will be considered
 	 * satisfactory. If found, the instance(s) are bound is returned.
 	 * 
 	 * @param source
 	 *            the instance that requires the specification
 	 * @param depName
 	 *            the relation name. Field for atomic; spec name for complex
 	 *            dep, type for composite.
 	 * @return
 	 */
 
 	@Override
 	public Resolved<?> resolveLink(Component source, String depName) {
 		if ((depName == null) || (source == null)) {
 			logger.error("missing client or relation name");
 			return null;
 		}
 
 		// Get the relation
 		RelationDefinition relDef = source.getRelation(depName);
 		if (relDef == null) {
 			logger.error("Relation declaration invalid or not found " + depName);
 			return null;
 		}
 		return resolveLink(source, relDef);
 	}
 
 	@Override
 	public Implementation resolveSpecByInterface(Component client, String interfaceName, Set<String> constraints, List<String> preferences) {
 
 		RelationDefinition dep = new RelationDefinitionImpl(new InterfaceReference(interfaceName), client.getKind(), ComponentKind.IMPLEMENTATION, constraints, preferences);
 		return resolveSpecByResource(client, dep);
 	}
 
 	@Override
 	public Implementation resolveSpecByMessage(Component client, String messageName, Set<String> constraints, List<String> preferences) {
 
 		RelationDefinition dep = new RelationDefinitionImpl(new MessageReference(messageName), client.getKind(), ComponentKind.IMPLEMENTATION, constraints, preferences);
 		return resolveSpecByResource(client, dep);
 	}
 
 	/**
 	 * First looks for the specification defined by its name, and then resolve
 	 * that specification. Returns the implementation that implement the
 	 * specification and that satisfies the constraints.
 	 * 
 	 * @param compoType
 	 *            : the implementation to return must either be visible from
 	 *            compoType, or be deployed.
 	 * @param specName
 	 * @param constraints
 	 *            . The constraints to satisfy. They must be all satisfied.
 	 * @param preferences
 	 *            . If more than one implementation satisfies the constraints,
 	 *            returns the one that satisfies the maximum number of
 	 *            preferences, taken in the order, and stopping at the first
 	 *            failure.
 	 * @return
 	 */
 	@Override
 	public Implementation resolveSpecByName(Instance client, String specName, Set<String> constraints, List<String> preferences) {
 		if (client == null) {
 			client = CompositeImpl.getRootInstance();
 		}
 
 		RelationDefinition dep = new RelationDefinitionImpl(new SpecificationReference(specName), client.getKind(), ComponentKind.IMPLEMENTATION, constraints, preferences);
 
 		return resolveSpecByResource(client, dep);
 	}
 
 	/**
 	 * First looks for the specification defined by its interface, and then
 	 * resolve that specification. Returns the implementation that implement the
 	 * specification and that satisfies the constraints.
 	 * 
 	 * @param compoType
 	 *            : the implementation to return must either be visible from
 	 *            compoType, or be deployed.
 	 * @param interfaceName
 	 *            . The full name of one of the interfaces of the specification.
 	 *            WARNING : different specifications may share the same
 	 *            interface.
 	 * @param interfaces
 	 *            . The complete list of interface of the specification. At most
 	 *            one specification can be selected.
 	 * @param constraints
 	 *            . The constraints to satisfy. They must be all satisfied.
 	 * @param preferences
 	 *            . If more than one implementation satisfies the constraints,
 	 *            returns the one that satisfies the maximum number of
 	 *            preferences, taken in the order, and stopping at the first
 	 *            failure.
 	 * @return
 	 */
 	public Implementation resolveSpecByResource(Component client, RelationDefinition relDef) {
 		if (relDef.getTargetKind() != ComponentKind.IMPLEMENTATION) {
 			logger.error("Invalid target type for resolveSpecByResource. Implemntation expected, found : " + relDef.getTargetKind());
 			return null;
 		}
 		Resolved<?> resolve = resolveLink(client, relDef);
 		if (resolve == null) {
 			return null;
 		}
 
 		if (resolve.singletonResolved != null) {
 			return (Implementation) resolve.singletonResolved;
 		}
 		return (Implementation) resolve.setResolved.iterator().next();
 	}
 
 	@Override
 	public void updateComponent(String componentName) {
 		Implementation impl = CST.componentBroker.getImpl(componentName);
 		if (impl == null) {
 			logger.error("Unknown component " + componentName);
 			return;
 		}
 		UpdateMan.updateComponent(impl);
 	}
 
 }
