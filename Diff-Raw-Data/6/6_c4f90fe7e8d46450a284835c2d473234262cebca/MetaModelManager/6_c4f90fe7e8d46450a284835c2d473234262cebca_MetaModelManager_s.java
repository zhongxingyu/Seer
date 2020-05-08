 /**
  * <copyright>
  *
  * Copyright (c) 2010,2013 E.D.Willink and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *	E.D.Willink - initial API and implementation
  *	E.D.Willink (CEA LIST) - Bug 399252
  *
  * </copyright>
  *
  * $Id$
  */
 package org.eclipse.ocl.examples.pivot.manager;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.WeakHashMap;
 
 import org.apache.log4j.Logger;
 import org.eclipse.emf.codegen.ecore.genmodel.GenModel;
 import org.eclipse.emf.codegen.ecore.genmodel.GenPackage;
 import org.eclipse.emf.common.notify.Adapter;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.Notifier;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EAnnotation;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EcoreFactory;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.Resource.Factory.Registry;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.ecore.xmi.XMLResource;
 import org.eclipse.emf.ecore.xmi.impl.EMOFResourceFactoryImpl;
 import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
 import org.eclipse.jdt.annotation.NonNull;
 import org.eclipse.jdt.annotation.Nullable;
 import org.eclipse.ocl.examples.domain.compatibility.EMF_2_9;
 import org.eclipse.ocl.examples.domain.elements.DomainCollectionType;
 import org.eclipse.ocl.examples.domain.elements.DomainElement;
 import org.eclipse.ocl.examples.domain.elements.DomainInheritance;
 import org.eclipse.ocl.examples.domain.elements.DomainNamedElement;
 import org.eclipse.ocl.examples.domain.elements.DomainNamespace;
 import org.eclipse.ocl.examples.domain.elements.DomainOperation;
 import org.eclipse.ocl.examples.domain.elements.DomainPackage;
 import org.eclipse.ocl.examples.domain.elements.DomainProperty;
 import org.eclipse.ocl.examples.domain.elements.DomainTupleType;
 import org.eclipse.ocl.examples.domain.elements.DomainType;
 import org.eclipse.ocl.examples.domain.elements.DomainTypedElement;
 import org.eclipse.ocl.examples.domain.ids.TemplateParameterId;
 import org.eclipse.ocl.examples.domain.ids.TypeId;
 import org.eclipse.ocl.examples.domain.library.LibraryFeature;
 import org.eclipse.ocl.examples.domain.library.UnsupportedOperation;
 import org.eclipse.ocl.examples.domain.utilities.DomainUtil;
 import org.eclipse.ocl.examples.domain.utilities.ProjectMap;
 import org.eclipse.ocl.examples.domain.utilities.StandaloneProjectMap;
 import org.eclipse.ocl.examples.domain.values.IntegerValue;
 import org.eclipse.ocl.examples.pivot.AnyType;
 import org.eclipse.ocl.examples.pivot.CollectionType;
 import org.eclipse.ocl.examples.pivot.Comment;
 import org.eclipse.ocl.examples.pivot.Constraint;
 import org.eclipse.ocl.examples.pivot.DataType;
 import org.eclipse.ocl.examples.pivot.Element;
 import org.eclipse.ocl.examples.pivot.Feature;
 import org.eclipse.ocl.examples.pivot.InvalidLiteralExp;
 import org.eclipse.ocl.examples.pivot.InvalidType;
 import org.eclipse.ocl.examples.pivot.Iteration;
 import org.eclipse.ocl.examples.pivot.LambdaType;
 import org.eclipse.ocl.examples.pivot.Library;
 import org.eclipse.ocl.examples.pivot.Metaclass;
 import org.eclipse.ocl.examples.pivot.NamedElement;
 import org.eclipse.ocl.examples.pivot.Operation;
 import org.eclipse.ocl.examples.pivot.Parameter;
 import org.eclipse.ocl.examples.pivot.ParameterableElement;
 import org.eclipse.ocl.examples.pivot.ParserException;
 import org.eclipse.ocl.examples.pivot.PivotFactory;
 import org.eclipse.ocl.examples.pivot.PivotPackage;
 import org.eclipse.ocl.examples.pivot.Precedence;
 import org.eclipse.ocl.examples.pivot.PrimitiveType;
 import org.eclipse.ocl.examples.pivot.Property;
 import org.eclipse.ocl.examples.pivot.Root;
 import org.eclipse.ocl.examples.pivot.SelfType;
 import org.eclipse.ocl.examples.pivot.State;
 import org.eclipse.ocl.examples.pivot.TemplateBinding;
 import org.eclipse.ocl.examples.pivot.TemplateParameter;
 import org.eclipse.ocl.examples.pivot.TemplateParameterSubstitution;
 import org.eclipse.ocl.examples.pivot.TemplateSignature;
 import org.eclipse.ocl.examples.pivot.TemplateableElement;
 import org.eclipse.ocl.examples.pivot.TupleType;
 import org.eclipse.ocl.examples.pivot.Type;
 import org.eclipse.ocl.examples.pivot.TypeTemplateParameter;
 import org.eclipse.ocl.examples.pivot.UnspecifiedType;
 import org.eclipse.ocl.examples.pivot.VoidType;
 import org.eclipse.ocl.examples.pivot.context.ClassContext;
 import org.eclipse.ocl.examples.pivot.context.OperationContext;
 import org.eclipse.ocl.examples.pivot.context.ParserContext;
 import org.eclipse.ocl.examples.pivot.context.PropertyContext;
 import org.eclipse.ocl.examples.pivot.ecore.Ecore2Pivot;
 import org.eclipse.ocl.examples.pivot.ecore.Pivot2Ecore;
 import org.eclipse.ocl.examples.pivot.library.StandardLibraryContribution;
 import org.eclipse.ocl.examples.pivot.messages.OCLMessages;
 import org.eclipse.ocl.examples.pivot.model.OCLMetaModel;
 import org.eclipse.ocl.examples.pivot.model.OCLstdlib;
 import org.eclipse.ocl.examples.pivot.uml.UML2Pivot;
 import org.eclipse.ocl.examples.pivot.utilities.CompleteElementIterable;
 import org.eclipse.ocl.examples.pivot.utilities.External2Pivot;
 import org.eclipse.ocl.examples.pivot.utilities.IllegalLibraryException;
 import org.eclipse.ocl.examples.pivot.utilities.PivotResource;
 import org.eclipse.ocl.examples.pivot.utilities.PivotResourceFactoryImpl;
 import org.eclipse.ocl.examples.pivot.utilities.PivotUtil;
 import org.eclipse.osgi.util.NLS;
 
 import com.google.common.collect.Iterables;
 
 public class MetaModelManager extends PivotStandardLibrary implements Adapter.Internal, MetaModelManageable
 {		
 	public class CompleteTypeOperationsIterable extends CompleteElementIterable<Type, Operation>
 	{
 		protected final Boolean selectStatic;	// null for static/non-static, true for static, false for non-static
 		
 		public CompleteTypeOperationsIterable(@NonNull Iterable<Type> types, boolean selectStatic) {
 			super(types);
 			this.selectStatic = selectStatic;
 		}
 
 		@Override
 		protected @NonNull Iterable<Operation> getInnerIterable(@NonNull Type model) {
 			return DomainUtil.nonNullEMF(model.getOwnedOperation());
 		}
 
 		@Override
 		protected @Nullable Operation getInnerValue(@NonNull Operation element) {
 			if (selectStatic != null) {
 				if (element.isStatic() != selectStatic.booleanValue()) {
 					return null;
 				}
 			}
 			return element;
 		}
 	}
 	
 	public class CompleteClassPropertiesIterable extends CompleteElementIterable<Type, Property>
 	{
 		protected final Boolean selectStatic;	// null for static/non-static, true for static, false for non-static
 		
 		public CompleteClassPropertiesIterable(@NonNull Iterable<Type> types, boolean selectStatic) {
 			super(types);
 			this.selectStatic = selectStatic;
 		}
 
 		@Override
 		protected @NonNull Iterable<Property> getInnerIterable(@NonNull Type model) {
 			return DomainUtil.nonNullEMF(model.getOwnedAttribute());
 		}
 
 		@Override
 		protected @Nullable Property getInnerValue(@NonNull Property element) {
 			if (selectStatic != null) {
 				if (element.isStatic() != selectStatic.booleanValue()) {
 					return null;
 				}
 			}
 			return element;
 		}
 	}
 	
 	public class CompleteClassSuperClassesIterable
 			extends CompleteElementIterable<Type, Type> {
 
 		public CompleteClassSuperClassesIterable(@NonNull Iterable<Type> types) {
 			super(types);
 		}
 
 		@Override
 		protected @NonNull Iterable<Type> getInnerIterable(@NonNull Type model) {
 			return DomainUtil.nonNullEMF(model.getSuperClass());
 		}
 	}
 
 	public class CompleteElementCommentsIterable
 			extends CompleteElementIterable<DomainElement, Comment> {
 
 		public CompleteElementCommentsIterable(@NonNull Iterable<? extends DomainElement> models) {
 			super(models);
 		}
 
 		@Override
 		protected Iterable<Comment> getInnerIterable(@NonNull DomainElement model) {
 			if (model instanceof Element) {
 				return DomainUtil.nonNullEMF(((Element)model).getOwnedComment());
 			}
 			else {
 				return EMPTY_COMMENT_LIST;
 			}
 		}
 	}
 
 	public class CompleteElementConstraintsIterable
 			extends CompleteElementIterable<DomainNamedElement, Constraint> {
 
 		public CompleteElementConstraintsIterable(@NonNull Iterable<? extends DomainNamedElement> models) {
 			super(models);
 		}
 
 		@Override
 		protected @NonNull Iterable<Constraint> getInnerIterable(@NonNull DomainNamedElement model) {
 			if (model instanceof NamedElement) {
 				return DomainUtil.nonNullEMF(((NamedElement)model).getOwnedRule());
 			}
 			else {
 				return EMPTY_CONSTRAINT_LIST;
 			}
 		}
 	}
 
 	/**
 	 * An OrphanServer maintains the set of OrphanClients identifying active references to an orphan element.
 	 * When the final reference is removed the orphan is killed off.
 	 *
 	public static class OrphanServer
 	{
 		protected final Element element;
 		private Set<OrphanClient> clients = null;
 		
 		public OrphanServer(Element element) {
 			this.element = element;
 		}
 
 		public void addClient(OrphanClient client) {
 			if (client != null) {
 				if (clients == null) {
 					clients = new HashSet<OrphanClient>();
 				}
 				clients.add(client);
 			}			
 		}
 
 //		public Element getElement() {
 //			return element;
 //		}
 
 		public void removeClient(OrphanClient client) {
 			if (client != null) {
 				if (clients != null) {
 					clients.remove(client);
 					if (clients.isEmpty()) {
 						clients = null;
 						element.eSet(element.eContainmentFeature(), null);
 					}
 				}
 			}			
 		}
 		
 		@Override
 		public String toString() {
 			return "<orphan-node> " + element;
 		}
 	} */
 	
 	/**
 	 * An OrphanClient adapts an EObject with an eReference to an OrphanServer within the domina of a TypeCache.
 	 * 
 	 * Changes to eContainer and eReference cause the eReference to be activated and deactivated accordingly so that
 	 * the OrphanServer is aware of how many active clients there are.
 	 *
 	public static class OrphanClient implements Adapter
 	{
 		protected final TypeCaches typeCache;
 		protected final EObject target;
 		protected final EReference eReference;
 		private OrphanServer orphanServer;
 		
 		public OrphanClient(TypeCaches typeCache, EObject target, EReference eReference) {
 			this.typeCache = typeCache;
 			this.target = target;
 			this.eReference = eReference;
 			this.orphanServer = null;
 			target.eAdapters().add(this);
 		}
 
 		protected void activate(NamedElement pivotElement) {
 			orphanServer = null; // W I P typeCache.getOrphanServer(pivotElement);
 			if (orphanServer != null) {
 				orphanServer.addClient(this);
 			}
 		}
 
 		protected void deActivate() {
 			if (orphanServer != null) {
 				orphanServer.removeClient(this);
 				orphanServer = null;
 			}
 		}
 		
 		public Notifier getTarget() {
 			return target;
 		}
 
 		public boolean isAdapterForType(Object type) {
 			return type == OrphanClient.class;
 		}
 
 		public void notifyChanged(Notification notification) {
 			if (notification.getNotifier() == target) {
 				int eventType = notification.getEventType();
 				switch (eventType) {
 					case Notification.SET: {
 						Object oldValue = notification.getOldValue();
 						Object newValue = notification.getNewValue();
 						if (newValue != oldValue) {
 							Object feature = notification.getFeature();
 							if (feature == eReference) {
 								if (oldValue != null) {
 									deActivate();
 								}
 								if (newValue instanceof NamedElement) {
 									activate((NamedElement) newValue);
 								}
 							}
 							else if (feature == target.eContainmentFeature()) {
 								if (oldValue != null) {
 									deActivate();
 								}
 								if (newValue != null) {
 									assert target == newValue;
 									Object orphan = target.eGet(eReference);
 									if (orphan instanceof NamedElement) {
 										activate((NamedElement) orphan);
 									}
 								}
 							}
 						}
 					}
 				}
 			}			
 		}
 
 		public void setTarget(Notifier newTarget) {
 			assert (newTarget == null) || (newTarget == target);
 		}		
 	} */
 	
 	public static interface Factory	// FIXME Support this via an extension point
 	{
 		/**
 		 * Return true if this factory can handle creation of a Pivot type from the
 		 * available object.
 		 */
 		boolean canHandle(@NonNull EObject eObject);
 
 		/**
 		 * Return true if this factory can handle creation of a Pivot resource from the
 		 * available resource.
 		 */
 		boolean canHandle(@NonNull Resource resource);
 		
 		/**
 		 * Configure the MetaModelManager's external ResourceSet. Implementations may install
 		 * any required extension or content to factory mappinmgs in the resource factory registry.
 		 * @param resourceSet
 		 */
 		void configure(@NonNull ResourceSet resourceSet);
 
 		/**
 		 * Return the URI of an eObject if it can be treated as a Package.
 		 */
 		@Nullable URI getPackageURI(@NonNull EObject eObject);
 
 		<T extends Element> T getPivotOf(@NonNull MetaModelManager metaModelManager, @NonNull Class<T> pivotClass, @NonNull EObject eObject) throws ParserException;
 		
 		/**
 		 * Return the root element in the Pivot resource resulting from import of the available
 		 * resource. 
 		 * @param uriFragment 
 		 * @throws ParserException 
 		 */
 		@Nullable Element importFromResource(@NonNull MetaModelManager metaModelManager, @NonNull Resource resource, @Nullable URI uri) throws ParserException;
 	}
 	
 	private static @NonNull Set<Factory> factoryMap = new HashSet<Factory>();
 	
 	public static synchronized void addFactory(@NonNull Factory factory) {
 		factoryMap.add(factory);
 	}
 	
 	static {
 		Ecore2Pivot.FACTORY.getClass();
 		UML2Pivot.FACTORY.getClass();
 	}
 	
 	private static final Logger logger = Logger.getLogger(MetaModelManager.class);
 
 	@SuppressWarnings("null")
 	public static final @NonNull List<Comment> EMPTY_COMMENT_LIST = Collections.<Comment>emptyList();
 	
 	@SuppressWarnings("null")
 	public static final @NonNull List<Constraint> EMPTY_CONSTRAINT_LIST = Collections.<Constraint>emptyList();
 
 	@SuppressWarnings("null")
 	public static final @NonNull List<Element> EMPTY_ELEMENT_LIST = Collections.<Element>emptyList();
 
 	@SuppressWarnings("null")
 	public static final @NonNull List<Operation> EMPTY_OPERATION_LIST = Collections.<Operation>emptyList();
 
 	@SuppressWarnings("null")
 	public static final @NonNull List<Property> EMPTY_PROPERTY_LIST = Collections.<Property>emptyList();
 
 	@SuppressWarnings("null")
 	public static final @NonNull List<State> EMPTY_STATE_LIST = Collections.<State>emptyList();
 
 	@SuppressWarnings("null")
 	public static final @NonNull List<TemplateParameter> EMPTY_TEMPLATE_PARAMETER_LIST = Collections.<TemplateParameter>emptyList();
 
 	@SuppressWarnings("null")
 	public static final @NonNull List<Type> EMPTY_TYPE_LIST = Collections.<Type>emptyList();
 
 	@SuppressWarnings("null")
 	public static final @NonNull List<TypeServer> EMPTY_TYPE_SERVER_LIST = Collections.<TypeServer>emptyList();
 
 	/**
 	 * Leak debugging aid. Set non-null to diagnose MetaModelManager construction and finalization.
 	 */
 	public static WeakHashMap<MetaModelManager,Object> liveMetaModelManagers = null;
 
 	public static @Nullable MetaModelManager findAdapter(@Nullable ResourceSet resourceSet) {
 		if (resourceSet == null) {
 			return null;
 		}
 		return PivotUtil.getAdapter(MetaModelManager.class, resourceSet);
 	}
 
 	public static @NonNull MetaModelManager getAdapter(@NonNull ResourceSet resourceSet) {
 		List<Adapter> eAdapters = DomainUtil.nonNullEMF(resourceSet.eAdapters());
 		MetaModelManager adapter = PivotUtil.getAdapter(MetaModelManager.class, eAdapters);
 		if (adapter == null) {
 			adapter = new MetaModelManager(resourceSet);
 //			eAdapters.add(adapter);
 		}
 		return adapter;
 	}
 
 	public static void initializePivotResourceSet(@NonNull ResourceSet pivotResourceSet) {
 		StandaloneProjectMap.initializeURIResourceMap(pivotResourceSet);
 		Registry resourceFactoryRegistry = pivotResourceSet.getResourceFactoryRegistry();
 		Map<String, Object> contentTypeToFactoryMap = resourceFactoryRegistry.getContentTypeToFactoryMap();
 		contentTypeToFactoryMap.put(PivotResource.CONTENT_TYPE, new PivotResourceFactoryImpl()); //$NON-NLS-1$
 		Map<String, Object> extensionToFactoryMap = resourceFactoryRegistry.getExtensionToFactoryMap();
 		extensionToFactoryMap.put("*", new XMIResourceFactoryImpl()); //$NON-NLS-1$
 		extensionToFactoryMap.put(PivotResource.FILE_EXTENSION, new PivotResourceFactoryImpl()); //$NON-NLS-1$
 		org.eclipse.emf.ecore.EPackage.Registry packageRegistry = pivotResourceSet.getPackageRegistry();
 		packageRegistry.put(PivotPackage.eNS_URI, PivotPackage.eINSTANCE);
 	}
 	
 	/**
 	 * The known packages.
 	 */
 	private final @NonNull PackageManager packageManager = createPackageManager();
 	
 	/**
 	 * The known precedences.
 	 */
 	private PrecedenceManager precedenceManager = null;			// Lazily created
 	
 	/**
 	 * The known tuple types.
 	 */
 	private TupleTypeManager tupleManager = null;			// Lazily created
 	
 	/**
 	 * The known tuple types.
 	 */
 	private LambdaTypeManager lambdaManager = null;			// Lazily created
 	
 	/**
 	 * The known implementation load capabilities.
 	 */
 	private ImplementationManager implementationManager = null;			// Lazily created
 		
 	private Orphanage orphanage = null;
 
 	protected DomainPackage pivotMetaModel = null;
 
 	private boolean libraryLoadInProgress = false;
 
 	protected final @NonNull ResourceSet pivotResourceSet;
 	
 	/**
 	 * All Library packages imported into the current type managed domain. All libraries
 	 * share the same URI, which for supplementary libraries may be null.
 	 */
 	protected final @NonNull List<Library> pivotLibraries = new ArrayList<Library>();	
 
 	/**
 	 * The resource of the first of the pivotLibraries. Set once actually loaded.
 	 */
 	protected Resource pivotLibraryResource = null;
 
 	protected ResourceSetImpl externalResourceSet = null;
 	
 	private final @NonNull Map<String, DomainNamespace> globalNamespaces = new HashMap<String, DomainNamespace>();
 	private final @NonNull Set<Type> globalTypes = new HashSet<Type>();
 
 	private int unspecifiedTypeCount = 0;
 	
 	/**
 	 * Map of URI to external resource converter.
 	 */
 	private final @NonNull Map<URI, External2Pivot> external2PivotMap = new HashMap<URI, External2Pivot>();
 
 	/**
 	 * The resolver for Ids and EObjects
 	 */
 	protected final @NonNull PivotIdResolver idResolver;
 
 	/**
 	 * Elements protected from garbage collection
 	 */
 	private EAnnotation lockingAnnotation = null;
 	
 	/**
 	 * MetaModelManagerListener instances to be notified of significant state changes; most notably disposal.
 	 */
 	private List<MetaModelManagerListener> listeners = null;
 
 	private boolean autoLoadPivotMetaModel = true;
 	
 	private Map<String, GenPackage> genPackageMap = null;
 	
 	public MetaModelManager() {
 		this(new ResourceSetImpl());
 		initializePivotResourceSet(pivotResourceSet);
 	}
 	
 	/**
 	 * Construct a MetaModelManager that will use projectMap to assist in locating resources.
 	 */
 	public MetaModelManager(@NonNull StandaloneProjectMap projectMap) {
 		this();
 		pivotResourceSet.eAdapters().add(projectMap);
 	}
 
 	/**
 	 * Construct a MetaModelManager that will use pivotResourceSet to contain pivot copies
 	 * of meta-models, and {@link ProjectMap.getAdapter(ResourceSet)} to assist in locating resources.
 	 */
 	public MetaModelManager(@NonNull ResourceSet pivotResourceSet) {
 		idResolver = new PivotIdResolver(this);
 //		System.out.println("ctor " + this);
 		this.pivotResourceSet = pivotResourceSet;
 		pivotResourceSet.eAdapters().add(this);
 		if (liveMetaModelManagers != null) {
 			liveMetaModelManagers.put(this, null);
 			System.out.println(Thread.currentThread().getName() + " Create " + PivotUtil.debugSimpleName(this)
 				+ " " + PivotUtil.debugSimpleName(pivotResourceSet));	
 		}
 	}
 
 	public void addClassLoader(@NonNull ClassLoader classLoader) {
 		ImplementationManager implementationManager = getImplementationManager();
 		implementationManager.addClassLoader(classLoader);
 	}
 
 	public void addExternalResource(@NonNull External2Pivot external2Pivot) {
 		external2PivotMap.put(external2Pivot.getURI(), external2Pivot);
 	}
 
 	public void addGenModel(@NonNull GenModel genModel) {
 		if (genPackageMap == null) {
 			genPackageMap = new HashMap<String, GenPackage>();
 		}
 		for (GenPackage genPackage : genModel.getGenPackages()) {
 			genPackageMap.put(genPackage.getNSURI(), genPackage);
 		}
 	}
 
 	public @Nullable DomainNamespace addGlobalNamespace(@NonNull String name, @NonNull DomainNamespace namespace) {
 		return globalNamespaces.put(name, namespace);
 	}
 
 	public boolean addGlobalTypes(@NonNull Collection<Type> types) {
 		return globalTypes.addAll(types);
 	}
 
 	public void addListener(@NonNull MetaModelManagerListener listener) {
 		if (listeners == null) {
 			listeners = new ArrayList<MetaModelManagerListener>();
 		}
 		if (!listeners.contains(listener)) {
 			listeners.add(listener);
 		}
 	}
 	
 	public void addLockedElement(@NonNull Object lockedElement) {
 		if (lockedElement instanceof EObject) {
 			if (lockingAnnotation == null) {
 				lockingAnnotation = EcoreFactory.eINSTANCE.createEAnnotation();
 			}
 			List<EObject> lockingReferences = lockingAnnotation.getReferences();
 			if (!lockingReferences.contains(lockedElement)) {
 				lockingReferences.add((EObject) lockedElement);
 			}
 		}
 	}
 
 //	public void addModel(Model pivotModel) {
 //		packageManager.addModel(pivotModel);
 //	}
 	
 	public void addOrphanClass(@NonNull Type pivotElement) {
 		if (pivotElement.getUnspecializedElement() != null) {
 			assert pivotElement.getUnspecializedElement().getUnspecializedElement() == null;
 		}
 		else {
 			assert (pivotElement instanceof LambdaType)
 				|| (pivotElement instanceof TupleType)
 				|| (pivotElement instanceof UnspecifiedType);
 		}
 		pivotElement.setPackage(getOrphanage());
 	}
 
 	/**
 	 * Return -ve if match1 is inferior to match2, +ve if match2 is inferior to match1, or
 	 * zero if both matches are of equal validity.
 	 */
 	public int compareOperationMatches(@NonNull Operation reference, @Nullable Map<TemplateParameter, ParameterableElement> referenceBindings,
 			@NonNull Operation candidate, @Nullable Map<TemplateParameter, ParameterableElement> candidateBindings) {
 		if ((reference instanceof Iteration) && (candidate instanceof Iteration)) {
 			int iteratorCountDelta = ((Iteration)candidate).getOwnedIterator().size() - ((Iteration)reference).getOwnedIterator().size();
 			if (iteratorCountDelta != 0) {
 				return iteratorCountDelta;
 			}
 			Type referenceType = PivotUtil.getOwningType(reference);
 			Type candidateType = PivotUtil.getOwningType(candidate);
 			Type specializedReferenceType = getSpecializedType(referenceType, referenceBindings);
 			Type specializedCandidateType = getSpecializedType(candidateType, candidateBindings);
 			if (referenceType != candidateType) {
 				if (conformsTo(specializedReferenceType, specializedCandidateType, null)) {
 					return 1;
 				}
 				else if (conformsTo(specializedCandidateType, specializedReferenceType, null)) {
 					return -1;
 				}
 			}
 		}
 		List<Parameter> candidateParameters = candidate.getOwnedParameter();
 		List<Parameter> referenceParameters = reference.getOwnedParameter();
 		int parameterCountDelta = candidateParameters.size() - referenceParameters.size();
 		if (parameterCountDelta != 0) {
 			return parameterCountDelta;
 		}
 		boolean referenceConformsToCandidate = true;
 		boolean candidateConformsToReference = true;
 		for (int i = 0; i < candidateParameters.size(); i++) {
 			Parameter referenceParameter = referenceParameters.get(i);
 			Parameter candidateParameter = candidateParameters.get(i);
 			if ((referenceParameter == null) || (candidateParameter == null)) {					// Doesn't happen (just a supurious NPE guard)
 				referenceConformsToCandidate = false;
 				candidateConformsToReference = false;
 			}
 			else {
 				Type referenceType = PivotUtil.getBehavioralType(referenceParameter);
 				Type candidateType = PivotUtil.getBehavioralType(candidateParameter);
 				Type specializedReferenceType = getSpecializedType(referenceType, referenceBindings);
 				Type specializedCandidateType = getSpecializedType(candidateType, candidateBindings);
 				if (referenceType != candidateType) {
 					if (!conformsTo(specializedReferenceType, specializedCandidateType, null)) {
 						referenceConformsToCandidate = false;
 					}
 					if (!conformsTo(specializedCandidateType, specializedReferenceType, null)) {
 						candidateConformsToReference = false;
 					}
 				}
 			}
 		}
 		if (referenceConformsToCandidate != candidateConformsToReference) {
 			return referenceConformsToCandidate ? 1 : -1;
 		}
 		Type referenceType = DomainUtil.nonNullModel(PivotUtil.getOwningType(reference));
 		Type candidateType = DomainUtil.nonNullModel(PivotUtil.getOwningType(candidate));
 		Type specializedReferenceType = getSpecializedType(referenceType, referenceBindings);
 		Type specializedCandidateType = getSpecializedType(candidateType, candidateBindings);
 		if (referenceType != candidateType) {
 			if (conformsTo(specializedReferenceType, specializedCandidateType, null)) {
 				return 1;
 			}
 			else if (conformsTo(specializedCandidateType, specializedReferenceType, null)) {
 				return -1;
 			}
 		}
 		return 0;
 	}
 
 	public boolean conformsTo(@NonNull Type firstType, @NonNull Type secondType, @Nullable Map<TemplateParameter, ParameterableElement> bindings) {
 		if (bindings != null) {
 			TemplateParameter firstTemplateParameter = firstType.getOwningTemplateParameter();
 			if (firstTemplateParameter != null) {
 				ParameterableElement parameterableElement = bindings.get(firstTemplateParameter);
 				if (parameterableElement instanceof Type) {
 					firstType = (Type) parameterableElement;
 				}
 			}
 			TemplateParameter secondTemplateParameter = secondType.getOwningTemplateParameter();
 			if (secondTemplateParameter != null) {
 				ParameterableElement parameterableElement = bindings.get(secondTemplateParameter);
 				if (parameterableElement instanceof Type) {
 					secondType = (Type) parameterableElement;
 				}
 				else if ((parameterableElement == null) && bindings.containsKey(secondTemplateParameter)) {
 					bindings.put(secondTemplateParameter, firstType);
 					return true;
 				}
 			}
 		}
 		if (firstType == secondType) {
 			return true;
 		}
 //		firstType = getModelType(firstType);
 //		secondType = getModelType(secondType);
 		firstType = getPrimaryType(firstType);
 		secondType = getPrimaryType(secondType);
 		if (firstType == secondType) {
 			return true;
 		}
 		if (secondType instanceof AnyType) {			// FIXME Shouldn't the library model definitions apply here too
 			return true;
 		}
 		else if (firstType instanceof AnyType) {
 			return false;
 		}
 		else if (firstType instanceof InvalidType) {
 			return true;
 		}
 		else if (secondType instanceof InvalidType) {
 			return false;
 		}
 		else if (firstType instanceof VoidType) {
 			return true;
 		}
 		else if (secondType instanceof VoidType) {
 			return false;
 		}
 		else if (firstType instanceof Metaclass) {
 			if (secondType instanceof Metaclass) {
 				return conformsToMetaclass((Metaclass)firstType, (Metaclass)secondType, bindings);
 			}
 			// Drop-through and maybe match xxClassifoer<xx> against OclType
 		}
 		else if (firstType instanceof CollectionType) {
 			if (secondType instanceof CollectionType) {
 				return conformsToCollectionType((CollectionType)firstType, (CollectionType)secondType, bindings);
 			}
 			return false;
 		}
 		else if (firstType instanceof LambdaType) {
 			if (secondType instanceof LambdaType) {
 				return conformsToLambdaType((LambdaType)firstType, (LambdaType)secondType, bindings);
 			}
 			return false;
 		}
 		else if (firstType instanceof TupleType) {
 			if (secondType instanceof TupleType) {
 				return conformsToTupleType((TupleType)firstType, (TupleType)secondType, bindings);
 			}
 			return false;
 		}
 		for (Type superClass : getSuperClasses(firstType)) {
 			if ((superClass != null) && conformsTo(superClass, secondType, bindings)) {
 				return true;
 			}
 		}
 //		List<TemplateBinding> templateBindings = actualType.getTemplateBinding();
 //		if (templateBindings.size() > 0) {
 //			TemplateableElement template = PivotUtil.getUnspecializedTemplateableElement(actualType);
 //			return conformsToClass((org.eclipse.ocl.examples.pivot.Class)template, requiredType);
 //		}
 		return false;
 	}
 
 	@Override
 	public boolean conformsToCollectionType(@NonNull DomainCollectionType firstCollectionType, @NonNull DomainCollectionType secondCollectionType) {
 		return conformsToCollectionType((CollectionType)firstCollectionType, (CollectionType)secondCollectionType, null);	// FIXME cast
 	}
 
 	protected boolean conformsToCollectionType(@NonNull CollectionType firstType, @NonNull CollectionType secondType,
 			@Nullable Map<TemplateParameter, ParameterableElement> bindings) {
 		CollectionType unspecializedFirstType = PivotUtil.getUnspecializedTemplateableElement(firstType);
 		CollectionType unspecializedSecondType = PivotUtil.getUnspecializedTemplateableElement(secondType);
 		if (!isSuperClassOf(unspecializedSecondType, unspecializedFirstType)) {
 			return false;
 		}
 		Type firstElementType = firstType.getElementType();
 		Type secondElementType = secondType.getElementType();
 		if ((firstElementType == null) || (secondElementType == null)) {
 			return false;
 		}
 		IntegerValue firstLower = firstType.getLowerValue();
 		IntegerValue secondLower = secondType.getLowerValue();
 		if (firstLower.compareTo(secondLower) > 0) {
 			return false;
 		}
 		IntegerValue firstUpper = firstType.getUpperValue();
 		IntegerValue secondUpper = secondType.getUpperValue();
 		if (firstUpper.compareTo(secondUpper) < 0) {
 			return false;
 		}
 		if (bindings != null) {
 //			if (firstElementType != null) {
 				TemplateParameter firstTemplateParameter = firstElementType.getOwningTemplateParameter();
 				if (firstTemplateParameter != null) {
 					ParameterableElement parameterableElement = bindings.get(firstTemplateParameter);
 					if (parameterableElement instanceof Type) {
 						firstElementType = (Type) parameterableElement;
 					}
 				}
 //			}
 //			if (secondElementType != null) {
 				TemplateParameter secondTemplateParameter = secondElementType.getOwningTemplateParameter();
 				if (secondTemplateParameter != null) {
 					ParameterableElement parameterableElement = bindings.get(secondTemplateParameter);
 					if (parameterableElement instanceof Type) {
 						secondElementType = (Type) parameterableElement;
 					}
 					else if ((parameterableElement == null) && bindings.containsKey(secondTemplateParameter)) {
 						bindings.put(secondTemplateParameter, firstElementType);
 						return true;
 					}
 				}
 //			}
 		}
 		if (firstElementType == secondElementType) {
 			return true;
 		}
 		else if (firstElementType instanceof UnspecifiedType) {
 			Type lowerBound = ((UnspecifiedType)firstElementType).getLowerBound();
 			if ((lowerBound != null) && conformsTo(secondElementType, lowerBound, bindings)) {
 				((UnspecifiedType)firstElementType).setLowerBound(secondElementType);
 				return true;
 			}
 			else {
 				return false;
 			}
 		}
 		else if (secondElementType instanceof UnspecifiedType) {
 			Type upperBound = ((UnspecifiedType)secondElementType).getUpperBound();
 			if ((upperBound != null) && conformsTo(upperBound, firstElementType, bindings)) {
 				((UnspecifiedType)secondElementType).setUpperBound(firstElementType);
 				return true;
 			}
 			else {
 				return false;
 			}
 		}
 		else {
 			return conformsTo(firstElementType, secondElementType, bindings);
 		}
 	}
 
 	protected boolean conformsToLambdaType(@NonNull LambdaType actualType, @NonNull LambdaType requiredType,
 			@Nullable Map<TemplateParameter, ParameterableElement> bindings) {
 		Type actualContextType = actualType.getContextType();
 		Type requiredContextType = requiredType.getContextType();
 		if ((actualContextType == null) || (requiredContextType == null)) {
 			return false;
 		}
 		if (!conformsTo(actualContextType, requiredContextType, bindings)) {
 			return false;
 		}
 		Type actualResultType = actualType.getResultType();
 		Type requiredResultType = requiredType.getResultType();
 		if ((actualResultType == null) || (requiredResultType == null)) {
 			return false;
 		}
 		if (!conformsTo(requiredResultType, actualResultType, bindings)) {	// contravariant
 			return false;
 		}
 		List<Type> actualParameterTypes = actualType.getParameterType();
 		List<Type> requiredParameterTypes = requiredType.getParameterType();
 		int iMax = actualParameterTypes.size();
 		if (iMax != requiredParameterTypes.size()) {
 			return false;
 		}
 		for (int i = 0; i < iMax; i++) {
 			Type actualParameterType = actualParameterTypes.get(i);
 			Type requiredParameterType = requiredParameterTypes.get(i);
 			if ((actualParameterType == null) || (requiredParameterType == null)) {
 				return false;
 			}
 			if (!conformsTo(actualParameterType, requiredParameterType, bindings)) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	protected boolean conformsToMetaclass(@NonNull Metaclass firstType, @NonNull Metaclass secondType,
 			@Nullable Map<TemplateParameter, ParameterableElement> bindings) {
 		Type firstElementType = firstType.getInstanceType();
 		Type secondElementType = secondType.getInstanceType();
 		if (bindings != null) {
 			TemplateParameter firstTemplateParameter = firstElementType.getOwningTemplateParameter();
 			if (firstTemplateParameter != null) {
 				ParameterableElement parameterableElement = bindings.get(firstTemplateParameter);
 				if (parameterableElement instanceof Type) {
 					firstElementType = (Type) parameterableElement;
 				}
 			}
 			TemplateParameter secondTemplateParameter = secondElementType.getOwningTemplateParameter();
 			if (secondTemplateParameter != null) {
 				ParameterableElement parameterableElement = bindings.get(secondTemplateParameter);
 				if (parameterableElement instanceof Type) {
 					secondElementType = (Type) parameterableElement;
 				}
 				else if ((parameterableElement == null) && bindings.containsKey(secondTemplateParameter)) {
 					bindings.put(secondTemplateParameter, firstElementType);
 					return true;
 				}
 			}
 		}
 		if ((firstElementType == null) || (secondElementType == null)) {
 			return false;
 		}
 		else if (firstElementType instanceof UnspecifiedType) {
 			Type lowerBound = ((UnspecifiedType)firstElementType).getLowerBound();
 			if ((lowerBound != null) && conformsTo(secondElementType, lowerBound, bindings)) {
 				((UnspecifiedType)firstElementType).setLowerBound(secondElementType);
 				return true;
 			}
 			else {
 				return false;
 			}
 		}
 		else if (secondElementType instanceof UnspecifiedType) {
 			Type upperBound = ((UnspecifiedType)secondElementType).getUpperBound();
 			if ((upperBound != null) && conformsTo(upperBound, firstElementType, bindings)) {
 				((UnspecifiedType)secondElementType).setUpperBound(firstElementType);
 				return true;
 			}
 			else {
 				return false;
 			}
 		}
 		else {
 			return conformsTo(firstElementType, secondElementType, bindings);
 		}
 	}
 
 	protected boolean conformsToTupleType(@NonNull TupleType actualType, @NonNull TupleType requiredType,
 			@Nullable Map<TemplateParameter, ParameterableElement> bindings) {
 		List<Property> actualProperties = actualType.getOwnedAttribute();
 		List<Property> requiredProperties = requiredType.getOwnedAttribute();
 		if (actualProperties.size() != requiredProperties.size()) {
 			return false;
 		}
 		for (Property actualProperty : actualProperties) {
 			Property requiredProperty = DomainUtil.getNamedElement(requiredProperties, actualProperty.getName());
 			if (requiredProperty == null) {
 				return false;
 			}
 			Type actualPropertyType = actualProperty.getType();
 			Type requiredPropertyType = requiredProperty.getType();
 			if ((actualPropertyType == null) || (requiredPropertyType == null)) {
 				return false;
 			}
 			if (!conformsTo(actualPropertyType, requiredPropertyType, bindings)) {
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	protected @NonNull ImplementationManager createImplementationManager() {
 		return new ImplementationManager(this);
 	}
 
 	public @NonNull InvalidLiteralExp createInvalidExpression(/*Object object, String boundMessage, Throwable e*/) {
 		InvalidLiteralExp invalidLiteralExp = PivotFactory.eINSTANCE.createInvalidLiteralExp();
 		invalidLiteralExp.setType(getOclInvalidType());
 //		invalidLiteralExp.setObject(object);
 //		invalidLiteralExp.setReason(boundMessage);
 //		invalidLiteralExp.setThrowable(e);
 		return invalidLiteralExp;
 	}
 
 	protected @NonNull LambdaTypeManager createLambdaManager() {
 		return new LambdaTypeManager(this);
 	}
 
 	protected @NonNull Orphanage createOrphanage() {
 		return Orphanage.getOrphanage(pivotResourceSet);
 	}
 
 	public @NonNull <T extends org.eclipse.ocl.examples.pivot.Package> T createPackage(@NonNull Class<T> pivotClass,
 			@NonNull EClass pivotEClass, @NonNull String name, @Nullable String nsURI) {
 		@SuppressWarnings("unchecked")
 		T pivotPackage = (T) pivotEClass.getEPackage().getEFactoryInstance().create(pivotEClass);
 		pivotPackage.setName(name);
 		pivotPackage.setNsURI(nsURI);
 		return pivotPackage;
 	}
 
 	protected @NonNull PackageManager createPackageManager() {
 		return new PackageManager(this);
 	}
 
 	protected @NonNull PrecedenceManager createPrecedenceManager() {
 		PrecedenceManager precedenceManager = new PrecedenceManager();
 		List<String> errors = precedenceManager.compilePrecedences(pivotLibraries);
 		for (String error : errors) {
 			logger.error(error);
 		}
 		return precedenceManager;
 	}
 
 	public @NonNull Resource createResource(@NonNull URI uri, @Nullable String contentType) {
 		Resource createResource = pivotResourceSet.createResource(uri, contentType);
 		if (createResource == null) {
 			throw new IllegalStateException("Failed to create '" + uri + "'");
 		}
 		return createResource;
 	}
 
 	public @NonNull Root createRoot(String name, String externalURI) {
 		return createRoot(Root.class, PivotPackage.Literals.ROOT, name, externalURI);
 	}
 
 	public @NonNull <T extends Root> T createRoot(@NonNull Class<T> pivotClass, /*@NonNull*/ EClass pivotEClass, String name, String externalURI) {
 		assert pivotEClass != null;
 		@SuppressWarnings("unchecked")
 		T pivotRoot = (T) pivotEClass.getEPackage().getEFactoryInstance().create(pivotEClass);
 		pivotRoot.setName(name);
 		pivotRoot.setExternalURI(externalURI);
 		return pivotRoot;
 	}
 	
 	protected @NonNull TupleTypeManager createTupleManager() {
 		return new TupleTypeManager(this);
 	}
 
 	public @NonNull UnspecifiedType createUnspecifiedType() {
 		String value = "<unspecified:" + unspecifiedTypeCount++ + ">";
 		UnspecifiedType unspecifiedType = PivotFactory.eINSTANCE.createUnspecifiedType();
 		unspecifiedType.setName(value);
 		unspecifiedType.setLowerBound(getOclAnyType());
 		unspecifiedType.setUpperBound(getOclVoidType());
 		addOrphanClass(unspecifiedType);
 		return unspecifiedType;
 	}
 
 	@Override
 	public void dispose() {
 		if (listeners != null) {
 			List<MetaModelManagerListener> savedListeners = listeners;
 			listeners = null;
 			for (MetaModelManagerListener listener : savedListeners) {
 				listener.metaModelManagerDisposed(this);
 			}
 		}
 		pivotResourceSet.eAdapters().remove(this);
 		for (Resource resource : pivotResourceSet.getResources()) {
 			resource.unload();
 		}
 		pivotResourceSet.getResources().clear();
 		pivotResourceSet.setPackageRegistry(null);
 		pivotResourceSet.setResourceFactoryRegistry(null);
 		pivotResourceSet.setURIConverter(null);
 //		pivotResourceSet.setURIResourceMap(null);
 		pivotLibraries.clear();	
 		pivotLibraryResource = null;
 		if (externalResourceSet != null) {
 			externalResourceSet.setPackageRegistry(null);
 			externalResourceSet.setResourceFactoryRegistry(null);
 			externalResourceSet.setURIConverter(null);
 			externalResourceSet.setURIResourceMap(null);
 			for (Resource resource : externalResourceSet.getResources()) {
 				resource.unload();
 			}
 			externalResourceSet = null;
 		}
 		globalNamespaces.clear();
 		globalTypes.clear();
 		external2PivotMap.clear();
 		lockingAnnotation = null;
 		idResolver.dispose();
 		packageManager.dispose();
 		if (tupleManager != null) {
 			tupleManager.dispose();
 			tupleManager = null;
 		}
 		if (lambdaManager != null) {
 			lambdaManager.dispose();
 			lambdaManager = null;
 		}
 		if (precedenceManager != null) {
 			precedenceManager.dispose();
 			precedenceManager = null;
 		}
 		if (implementationManager != null) {
 			implementationManager.dispose();
 			implementationManager = null;
 		}
 		orphanage = null;
 		pivotMetaModel = null;
 		super.dispose();
 	}
 
 	@Override
 	protected void finalize() throws Throwable {
 		if (liveMetaModelManagers != null) {
 			System.out.println("Finalize " + PivotUtil.debugSimpleName(this));		
 			Set<MetaModelManager> keySet = liveMetaModelManagers.keySet();
 			if (!keySet.isEmpty()) {
 				StringBuilder s = new StringBuilder();
 				s.append(" live");
 				for (MetaModelManager metaModelManager : keySet) {
 					s.append(" @" + Integer.toHexString(metaModelManager.hashCode()));		
 				}
 				System.out.println(s);		
 			}
 		}
 	}
 	
 //	public @Nullable PackageTracker findPackageTracker(@NonNull DomainPackage pivotPackage) {
 //		return packageManager.findPackageTracker(pivotPackage);
 //	}
 	
 //	public @Nullable TypeTracker findTypeTracker(@NonNull Type pivotType) {
 //		return packageManager.findTypeTracker(pivotType);
 //	}
 
 	/**
 	 * Return all constraints applicable to a type and its superclasses.
 	 */
 	public @NonNull Iterable<Constraint> getAllConstraints(@NonNull Type pivotType) {
 		Set<Constraint> knownConstraints = new HashSet<Constraint>();
 		for (DomainType partialSuperType : getPartialTypes(pivotType)) {
 			if (partialSuperType instanceof Type) {
 				knownConstraints.addAll(((Type)partialSuperType).getOwnedRule());
 			}
 		}
 		for (DomainType superType : getAllSuperClasses(pivotType)) {
 			if (superType != null) {
 				for (DomainType partialSuperType : getPartialTypes(superType)) {
 					if (partialSuperType instanceof Type) {
 						knownConstraints.addAll(((Type)partialSuperType).getOwnedRule());
 					}
 				}
 			}
 		}
 		return knownConstraints;
 	}
 	
 	public @NonNull Iterable<? extends DomainOperation> getAllOperations(@NonNull DomainType type, boolean selectStatic) {
 		TypeServer typeServer = packageManager.getTypeServer(type);
 		return typeServer.getAllOperations(selectStatic);
 	}
 	
 	public @NonNull Iterable<? extends DomainOperation> getAllOperations(@NonNull DomainType type, boolean selectStatic, @NonNull String name) {
 		TypeServer typeServer = packageManager.getTypeServer(type);
 		return typeServer.getAllOperations(selectStatic, name);
 	}
 
 	public @NonNull Iterable<? extends DomainOperation> getAllOperations(@NonNull DomainOperation pivotOperation) {
 		DomainInheritance pivotClass = pivotOperation.getInheritance(this);
 		if (pivotClass == null) {
 			throw new IllegalStateException("Missing owning type");
 		}
 		TypeServer typeServer = packageManager.findTypeServer(pivotClass);
 		if (typeServer != null) {
 			Iterable<? extends DomainOperation> memberOperations = typeServer.getMemberOperations(pivotOperation);
 			if (memberOperations != null) {
 				return memberOperations;
 			}
 		}
 		@SuppressWarnings("null") @NonNull List<DomainOperation> singletonList = Collections.singletonList(pivotOperation);
 		return singletonList;
 	}
 
 	public @NonNull Iterable<PackageServer> getAllPackages() {
 		if (!libraryLoadInProgress && (pivotMetaModel == null))  {
 			getPivotMetaModel();
 		}
 		return packageManager.getAllPackages();
 	}
 	
 	public @NonNull Iterable<? extends DomainProperty> getAllProperties(@NonNull DomainType type, boolean selectStatic) {
 		TypeServer typeServer = packageManager.getTypeServer(type);
 		return typeServer.getAllProperties(selectStatic);
 	}
 	
 	public @NonNull Iterable<? extends DomainProperty> getAllProperties(@NonNull DomainType type, boolean selectStatic, @NonNull String name) {
 		TypeServer typeServer = packageManager.getTypeServer(type);
 		return typeServer.getAllProperties(selectStatic, name);
 	}
 
 	public @NonNull Iterable<? extends DomainProperty> getAllProperties(@NonNull DomainProperty pivotProperty) {
 		DomainInheritance pivotClass = pivotProperty.getInheritance(this);
 		if (pivotClass == null) {
 			throw new IllegalStateException("Missing owning type");
 		}
 		TypeServer typeServer = packageManager.findTypeServer(pivotClass);
 		if (typeServer != null) {
 			Iterable<? extends DomainProperty> memberProperties = typeServer.getMemberProperties(pivotProperty);
 			if (memberProperties != null) {
 				return memberProperties;
 			}
 		}
 		@SuppressWarnings("null") @NonNull List<DomainProperty> singletonList = Collections.singletonList(pivotProperty);
 		return singletonList;
 	}
 	
 	public @NonNull Iterable<? extends DomainType> getAllSuperClasses(@NonNull DomainType type) {
 		TypeServer typeServer = packageManager.getTypeServer(type);
 		return typeServer.getAllSuperClasses();
 	}
 
 	@Deprecated
 	public @NonNull Iterable<Type> getAllTypes(@NonNull Type pivotType) {
 //		if (pivotType == null) {
 //			return EMPTY_TYPE_LIST;
 //		}
 //		return getTypeTracker(pivotType).getTypeServer().getTypes();
 		TypeServer typeServer = packageManager.findTypeServer(pivotType);
 		if (typeServer != null) {
 			@SuppressWarnings("null") @NonNull Iterable<Type> filter = Iterables.filter(typeServer.getPartialTypes(), Type.class);
 			return filter;
 		}
 		else  {
 			@SuppressWarnings("null") @NonNull List<Type> singletonList = Collections.singletonList(pivotType);
 			return singletonList;
 		}
 	}
 
 	public @NonNull CollectionType getBagType(@NonNull DomainType elementType, @Nullable IntegerValue lower, @Nullable IntegerValue upper) {
 		return getBagType(getType(elementType), lower, upper);
 	}
 
 	public @NonNull CollectionType getBagType(@NonNull Type elementType, @Nullable IntegerValue lower, @Nullable IntegerValue upper) {
 		return getCollectionType(getBagType(), elementType, lower, upper);
 	}
 
 	public @NonNull CollectionType getCollectionType(boolean isOrdered, boolean isUnique) {
 		if (isOrdered) {
 			if (isUnique) {
 				return getOrderedSetType();
 			}
 			else {
 				return getSequenceType();
 			}
 		}
 		else {
 			if (isUnique) {
 				return getSetType();
 			}
 			else {
 				return getBagType();
 			}
 		}
 	}
 	
 	@Override
 	public @NonNull CollectionType getCollectionType(@NonNull DomainType containerType, @NonNull DomainType elementType, @Nullable IntegerValue lower, @Nullable IntegerValue upper) {
 		return getCollectionType((CollectionType)getType(containerType), getType(elementType), lower, upper);
 	}
 
 	public @NonNull CollectionType getCollectionType(boolean isOrdered, boolean isUnique, @NonNull Type elementType, @Nullable IntegerValue lower, @Nullable IntegerValue upper) {
 		return getCollectionType(getCollectionType(isOrdered, isUnique), elementType, lower, upper);
 	}
 
 	public @NonNull Type getCollectionType(@NonNull String collectionTypeName, @NonNull Type elementType, @Nullable IntegerValue lower, @Nullable IntegerValue upper) {
 		if (elementType.eIsProxy()) {
 			return getOclInvalidType();
 		}
 		return getCollectionType((CollectionType)getRequiredLibraryType(collectionTypeName), elementType, lower, upper);
 	}
 
 	public @NonNull <T extends CollectionType> T getCollectionType(@NonNull T containerType, @NonNull Type elementType, @Nullable IntegerValue lower, @Nullable IntegerValue upper) {
 		assert containerType == PivotUtil.getUnspecializedTemplateableElement(containerType);
 		TemplateSignature templateSignature = containerType.getOwnedTemplateSignature();
 		if (templateSignature == null) {
 			throw new IllegalArgumentException("Collection type must have a template signature");
 		}
 		List<TemplateParameter> templateParameters = templateSignature.getParameter();
 		if (templateParameters.size() != 1) {
 			throw new IllegalArgumentException("Collection type must have exactly one template parameter");
 		}
 		boolean isUnspecialized = elementType == templateParameters.get(0).getParameteredElement();
 		if (isUnspecialized) {
 			return containerType;	
 		}
 		CollectionTypeServer typeServer = (CollectionTypeServer) getTypeServer(containerType);
 		@SuppressWarnings("unchecked")
 		T specializedType = (T) typeServer.getSpecializedType(elementType, lower, upper);
 		return specializedType;
 	}
 
 	public @NonNull Set<DomainInheritance> getCommonClasses(@NonNull Type leftClass, @NonNull Type rightClass, @NonNull Set<DomainInheritance> commonClasses) {
 		if (conformsTo(rightClass, leftClass, null)) {
 			commonClasses.add(leftClass.getInheritance(this));
 		}
 		for (Type superClass : leftClass.getSuperClass()) {
 			if (superClass != null) {
 				Set<DomainInheritance> commonSuperClasses = getCommonClasses(superClass, rightClass, commonClasses);
 				commonClasses.addAll(commonSuperClasses);
 			}
 		}
 		return commonClasses;
 	}
 	
 	public @NonNull Type getCommonType(@NonNull Type leftType, @NonNull Type rightType, @Nullable Map<TemplateParameter, ParameterableElement> templateParameterSubstitutions) {
 		if (leftType instanceof UnspecifiedType) {
 			((UnspecifiedType)leftType).setUpperBound(rightType);
 			return rightType;
 		}
 		if (rightType instanceof UnspecifiedType) {
 			((UnspecifiedType)rightType).setUpperBound(leftType);
 			return leftType;
 		}
 		if ((leftType instanceof TupleType) && (rightType instanceof TupleType)) {
 			TupleTypeManager tupleManager = getTupleManager();
 			Type commonType = tupleManager.getCommonType((TupleType)leftType, (TupleType)rightType, templateParameterSubstitutions);
 			if (commonType == null) {
 				commonType = getOclAnyType();
 			}
 			return commonType;
 		}
 		if (conformsTo(leftType, rightType, templateParameterSubstitutions)) {
 			return rightType;
 		}
 		if (conformsTo(rightType, leftType, templateParameterSubstitutions)) {
 			return leftType;
 		}
 		if ((leftType instanceof CollectionType) && (rightType instanceof CollectionType)) {
 			DomainInheritance leftInheritance = leftType.getInheritance(this);
 			DomainInheritance rightInheritance = rightType.getInheritance(this);
 			DomainInheritance commonInheritance = leftInheritance.getCommonInheritance(rightInheritance);
 			Type commonCollectionType = getType(commonInheritance); 
 			Type leftElementType = DomainUtil.nonNullModel(((CollectionType)leftType).getElementType());
 			Type rightElementType = DomainUtil.nonNullModel(((CollectionType)rightType).getElementType());
 			Type commonElementType = getCommonType(leftElementType, rightElementType, templateParameterSubstitutions); 
 			return getCollectionType(commonCollectionType, commonElementType, null, null);
 		}
 		DomainInheritance leftInheritance = leftType.getInheritance(this);
 		DomainInheritance rightInheritance = rightType.getInheritance(this);
 		DomainInheritance commonInheritance = leftInheritance.getCommonInheritance(rightInheritance);
 		return getType(commonInheritance); 
 	}
 
 	public @NonNull String getDefaultStandardLibraryURI() {
 		return defaultStandardLibraryURI;
 	}
 
 	public @Nullable <T extends EObject> T getEcoreOfPivot(@NonNull Class<T> ecoreClass, @NonNull Element element) {
 		EObject eTarget = element.getETarget();
 		if (eTarget != null) {
 			if (!ecoreClass.isAssignableFrom(eTarget.getClass())) {
 				logger.error("Ecore " + eTarget.getClass().getName() + "' element is not a '" + ecoreClass.getName() + "'"); //$NON-NLS-1$
 				return null;
 			}
 			@SuppressWarnings("unchecked")
 			T castTarget = (T) eTarget;
 			return castTarget;
 		}
 		Root root = (Root)EcoreUtil.getRootContainer(element);
 		Resource metaModel = element.eResource();
 		if (metaModel == null) {
 			return null;
 		}
 		if (metaModel instanceof OCLstdlib) {		// Not really a model so no Ecore
 			return null;
 		}
 		URI ecoreURI = DomainUtil.nonNullEMF(URI.createURI(root.getExternalURI()).appendFileExtension("ecore"));
 		Pivot2Ecore converter = new Pivot2Ecore(this, ecoreURI, null);
 		converter.convertResource(metaModel, ecoreURI);					// FIXME install ETargets
 		return converter.getCreated(ecoreClass, element);
 	}
 
 	public ResourceSet getExternalResourceSet() {
 		ResourceSetImpl externalResourceSet2 = externalResourceSet;
 		if (externalResourceSet2 == null) {
 			externalResourceSet2 = externalResourceSet = new ResourceSetImpl();
 			ProjectMap projectMap = ProjectMap.findAdapter(pivotResourceSet);
 			if (projectMap == null) {
 				projectMap = ProjectMap.getAdapter(externalResourceSet2);
 			}
 			else {
 				externalResourceSet2.eAdapters().add(projectMap);
 			}
 			projectMap.initializeResourceSet(externalResourceSet2);			
 			externalResourceSet2.getResourceFactoryRegistry().getExtensionToFactoryMap().put("emof", new EMOFResourceFactoryImpl()); //$NON-NLS-1$
 			MetaModelManagerResourceSetAdapter.getAdapter(externalResourceSet2, this);
 			for (Factory factory : factoryMap) {
 				factory.configure(externalResourceSet2);
 			}
 		}
 		return externalResourceSet2;
 	}
 
 	public @Nullable GenPackage getGenPackage(@NonNull String nsURI) {
 		if (genPackageMap != null) {
 			GenPackage genPackage = genPackageMap.get(nsURI);
 			if (genPackage != null) {
 				return genPackage;
 			}
 		}
 		ResourceSet externalResourceSet = getExternalResourceSet();
 		URI uri = EMF_2_9.EcorePlugin.getEPackageNsURIToGenModelLocationMap(false).get(nsURI);
 		if (uri != null) {
 			Resource resource = externalResourceSet.getResource(uri, true);
 			for (EObject eObject : resource.getContents()) {
 				if (eObject instanceof GenModel) {
 					for (GenPackage genPackage : ((GenModel)eObject).getGenPackages()) {
 						if (genPackage != null) {
 							return genPackage;
 						}
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 	public @NonNull Set<Map.Entry<String, DomainNamespace>> getGlobalNamespaces() {
 		@SuppressWarnings("null")
 		@NonNull Set<Entry<String, DomainNamespace>> entrySet = globalNamespaces.entrySet();
 		return entrySet;
 	}
 
 	public @NonNull Iterable<Type> getGlobalTypes() {
 		return globalTypes;
 	}
 
 	public @NonNull PivotIdResolver getIdResolver() {
 		return idResolver;
 	}
 	
 	public @NonNull LibraryFeature getImplementation(@NonNull Feature feature) throws ClassNotFoundException, SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
 		LibraryFeature implementation = feature.getImplementation();
 		if (implementation == null) {
 			ImplementationManager implementationManager = getImplementationManager();
 			implementation = implementationManager.loadImplementation(feature);
 			if (implementation == null) {
 				implementation = UnsupportedOperation.INSTANCE;
 			}
 		}
 		return implementation;
 	}
 	
 	public @NonNull LibraryFeature getImplementation(@NonNull Operation operation) throws ClassNotFoundException, SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
 		LibraryFeature implementation = operation.getImplementation();
 		if (implementation == null) {
 			ImplementationManager implementationManager = getImplementationManager();
 			implementation = implementationManager.getOperationImplementation(operation);
 			operation.setImplementation(implementation);
 		}
 		return implementation;
 	}
 
 	public @NonNull LibraryFeature getImplementation(@NonNull Property property) throws ClassNotFoundException, SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
 		LibraryFeature implementation = property.getImplementation();
 		if (implementation == null) {
 			ImplementationManager implementationManager = getImplementationManager();
 			implementation = implementationManager.getPropertyImplementation(property);
 			property.setImplementation(implementation);
 		}
 		return implementation;
 	}
 	
 	public @NonNull ImplementationManager getImplementationManager() {
 		ImplementationManager implementationManager2 = implementationManager;
 		if (implementationManager2 == null) {
 			implementationManager2 = implementationManager = createImplementationManager();
 		}
 		return implementationManager2;
 	}
 
 	public @Nullable Precedence getInfixPrecedence(@NonNull String operatorName) {
 		PrecedenceManager precedenceManager = getPrecedenceManager();
 		return precedenceManager.getInfixPrecedence(operatorName);
 	}
 
 	public @NonNull DomainInheritance getInheritance(@NonNull DomainType type) {
 		Type type1 = getType(type);
 		Type unspecializedType = (Type) type1.getUnspecializedElement();
 		Type theType = unspecializedType != null ? unspecializedType : type1;
 		TypeServer typeServer = getTypeServer(theType);
 		return typeServer;
 	}
 
 	public @NonNull LambdaTypeManager getLambdaManager() {
 		LambdaTypeManager lambdaManager2 = lambdaManager;
 		if (lambdaManager2 == null) {
 			lambdaManager2 = lambdaManager = createLambdaManager();
 		}
 		return lambdaManager2;
 	}
 	   
 	public @NonNull LambdaType getLambdaType(@NonNull String typeName, @NonNull Type contextType, @NonNull List<? extends Type> parameterTypes, @NonNull Type resultType,
 			@Nullable Map<TemplateParameter, ParameterableElement> bindings) {
 		LambdaTypeManager lambdaManager = getLambdaManager();
 		return lambdaManager.getLambdaType(typeName, contextType, parameterTypes, resultType, bindings);
 	}
 	
 	public List<Library> getLibraries() { return pivotLibraries; }
 	public Resource getLibraryResource() { return pivotLibraryResource; }
 
 	public @Nullable Type getLibraryType(@NonNull String string, @NonNull List<? extends ParameterableElement> templateArguments) {
 		Type libraryType = getRequiredLibraryType(string);
 		return getLibraryType(libraryType, templateArguments);
 	}
 
 	public @NonNull <T extends Type> T getLibraryType(@NonNull T libraryType, @NonNull List<? extends ParameterableElement> templateArguments) {
 		assert !(libraryType instanceof CollectionType);
 		assert !(libraryType instanceof Metaclass);
 		assert libraryType == PivotUtil.getUnspecializedTemplateableElement(libraryType);
 		TemplateSignature templateSignature = libraryType.getOwnedTemplateSignature();
 		List<TemplateParameter> templateParameters = templateSignature != null ? templateSignature.getParameter() : EMPTY_TEMPLATE_PARAMETER_LIST;
 		if (templateParameters.isEmpty()) {
 			return libraryType;
 		}
 		if (templateArguments.size() != templateParameters.size()) {
 			throw new IllegalArgumentException("Incorrect template bindings for template type");
 		}
 		boolean isUnspecialized = isUnspecialized(templateParameters, templateArguments);
 		if (isUnspecialized) {
 			return libraryType;	
 		}
 		TemplateableTypeServer typeServer = (TemplateableTypeServer) getTypeServer(libraryType);
 		@SuppressWarnings("unchecked")
 		T specializedType = (T) typeServer.getSpecializedType(templateArguments);
 		return specializedType;
 	}
 
 	public @NonNull Iterable<TypeServer> getLocalClasses(@NonNull org.eclipse.ocl.examples.pivot.Package pkg) {
 		return getPackageServer(pkg).getMemberTypes();
 	}
 
 	public Iterable<Comment> getLocalComments(@NonNull Operation operation) {
 		if (operation.getOwningTemplateParameter() != null) {
 			return EMPTY_COMMENT_LIST;
 		}
 		else {
 			return new CompleteElementCommentsIterable(getAllOperations(operation));
 		}
 	}
 
 	public @NonNull Iterable<Comment> getLocalComments(@NonNull Property property) {
 		if (property.getOwningTemplateParameter() != null) {
 			return EMPTY_COMMENT_LIST;
 		}
 		else {
 			return new CompleteElementCommentsIterable(getAllProperties(property));
 		}
 	}
 
 	public @NonNull Iterable<Comment> getLocalComments(@NonNull Type type) {
 //		if (type == null) {
 //			return EMPTY_COMMENT_LIST;
 //		}
 		if (type.getOwningTemplateParameter() != null) {
 			return EMPTY_COMMENT_LIST;
 		}
 		else {
 			type = PivotUtil.getUnspecializedTemplateableElement(type);
 			return new CompleteElementCommentsIterable(getAllTypes(type));
 		}
 	}
 
 	public @NonNull Iterable<Constraint> getLocalConstraints(@NonNull Operation operation) {
 		if (operation.getOwningTemplateParameter() != null) {
 			return EMPTY_CONSTRAINT_LIST;
 		}
 		else {
 			return new CompleteElementConstraintsIterable(getAllOperations(operation));
 		}
 	}
 
 	public @NonNull Iterable<Constraint> getLocalConstraints(@NonNull Property property) {
 		if (property.getOwningTemplateParameter() != null) {
 			return EMPTY_CONSTRAINT_LIST;
 		}
 		else if (property.eContainer() instanceof TupleType) {			// FIXME Find a better way
 			return EMPTY_CONSTRAINT_LIST;
 		}
 		else {
 			return new CompleteElementConstraintsIterable(getAllProperties(property));
 		}
 	}
 
 	public @NonNull Iterable<Constraint> getLocalConstraints(@NonNull Type type) {
 //		if (type == null) {
 //			return EMPTY_CONSTRAINT_LIST;
 //		}
 		if (type.getOwningTemplateParameter() != null) {
 			return EMPTY_CONSTRAINT_LIST;
 		}
 		else {
 			type = PivotUtil.getUnspecializedTemplateableElement(type);
 			return new CompleteElementConstraintsIterable(getAllTypes(type));
 		}
 	}
 
 	public @Nullable EObject getLockingObject() {
 		return lockingAnnotation;
 	}
 
 	public @NonNull Iterable<Operation> getMemberOperations(@NonNull Type type, boolean selectStatic) {
 		if (type.getOwningTemplateParameter() != null) {
 			return EMPTY_OPERATION_LIST;
 		}
 		else {
 			type = PivotUtil.getUnspecializedTemplateableElement(type);
 			return new CompleteTypeOperationsIterable(getAllTypes(type), selectStatic);
 		}
 	}
 
 	public @NonNull Iterable<? extends PackageServer> getMemberPackages(@NonNull DomainPackage pkg) {
 		return getPackageServer(pkg).getMemberPackages();
 	}
 
 	public @NonNull MetaModelManager getMetaModelManager() {
 		return this;
 	}
 
 	public @NonNull Iterable<Property> getMemberProperties(@NonNull Type type, boolean selectStatic) {
 		if (type.getOwningTemplateParameter() != null) {
 			return EMPTY_PROPERTY_LIST;
 		}
 		else {
 			type = PivotUtil.getUnspecializedTemplateableElement(type);
 			return new CompleteClassPropertiesIterable(getAllTypes(type), selectStatic);
 		}
 	}
 
 	public @NonNull Metaclass getMetaclass(@NonNull DomainType domainInstanceType) {
 		Type instanceType;
 		if (domainInstanceType instanceof Type) {
 			instanceType = (Type)domainInstanceType;
 		}
 		else {
 			instanceType = getType(domainInstanceType);
 		}
 		Metaclass metaclassType =  getMetaclassType();
 		if (instanceType == metaclassType) {
 			return metaclassType;
 		}
 		TemplateSignature templateSignature = metaclassType.getOwnedTemplateSignature();
 		List<TemplateParameter> templateParameters = templateSignature.getParameter();
 		boolean isUnspecialized = instanceType == templateParameters.get(0).getParameteredElement();	// Checked in getMetaclassType().
 		if (isUnspecialized) {
 			return metaclassType;	
 		}
 		MetaclassServer typeServer = (MetaclassServer) getTypeServer(metaclassType);
 		Metaclass metaclass = typeServer.getMetaclass(instanceType);
 		return metaclass;
 	}
 
 	@Override
 	public DomainType getMetaType(@NonNull DomainType instanceType) {
 		if (instanceType instanceof PrimitiveType) {
 			return getPivotType(TypeId.PRIMITIVE_TYPE_NAME);
 		}
 //		throw new UnsupportedOperationException();
 		return getMetaclass(instanceType);
 	}
 
 	@Override
 	public DomainPackage getNestedPackage(@NonNull DomainPackage packageServer, @NonNull String name) {
 		return ((PackageServer)packageServer).getMemberPackage(name);
 	}
 
 	@Override
 	public DomainType getNestedType(@NonNull DomainPackage packageServer, @NonNull String name) {
 		return ((PackageServer)packageServer).getMemberType(name);
 	}
 
 	@Override
 	public DomainPackage getNsURIPackage(@NonNull String nsURI) {
 		return packageManager.getPackageByURI(nsURI);
 	}
 
 	public @Nullable DomainType getOclType(@NonNull String typeName) {
 		Type pivotType = getPivotType(typeName);
 		return pivotType != null ? getInheritance(pivotType) : null;
 	}
 
 	@Override
 	public @Nullable DomainElement getOperationTemplateParameter(@NonNull DomainOperation anOperation, int index) {
 		if (anOperation instanceof Operation) {
 			anOperation = PivotUtil.getUnspecializedTemplateableElement((Operation)anOperation);
 		}
 		TypeTemplateParameter templateParameter = (TypeTemplateParameter) anOperation.getTypeParameters().get(index);
 		if (templateParameter == null) {
 			throw new UnsupportedOperationException();
 		}
 		ParameterableElement parameteredElement = templateParameter.getParameteredElement();
 		if (!(parameteredElement instanceof DomainType)) {
 			throw new UnsupportedOperationException();
 		}
 		return parameteredElement;
 	}
 
 	public @NonNull CollectionType getOrderedSetType(@NonNull DomainType elementType, @Nullable IntegerValue lower, @Nullable IntegerValue upper) {
 		return getOrderedSetType(getType(elementType), lower, upper);
 	}
 
 	public @NonNull CollectionType getOrderedSetType(@NonNull Type elementType, @Nullable IntegerValue lower, @Nullable IntegerValue upper) {
 		return getCollectionType(getOrderedSetType(), elementType, lower, upper);
 	}
 
 	public @NonNull Orphanage getOrphanage() {
 		Orphanage orphanage2 = orphanage;
 		if (orphanage2 == null) {
 			orphanage2 = orphanage = createOrphanage();
 		}
 		return orphanage2;
 	}
 
 	public PackageManager getPackageManager() {
 		return packageManager;
 	}
 	
 	public @NonNull PackageServer getPackageServer(@NonNull DomainPackage pivotPackage) {
 		if (!libraryLoadInProgress && pivotMetaModel == null) {
 			getPivotMetaModel();
 		}
 		return packageManager.getPackageServer(pivotPackage);
 	}
 
 	/**
 	 * Return a parserContext suitable for parsing OCL expressions in the context of a pivot element.
 	 * 
 	 * @throws ParserException if eObject cannot be converted to a Pivot element
 	 */
 	public @Nullable ParserContext getParserContext(@NonNull Element element, Object... todoParameters) {
 		Element pivotElement = element;
 		if (pivotElement instanceof Constraint) {
 			List<Element> constrainedElements = ((Constraint) pivotElement).getConstrainedElement();
 			pivotElement = constrainedElements.size() > 0 ? constrainedElements.get(0) : null;
 		}
 		if (pivotElement instanceof Property) {
 			return new PropertyContext(this, null, (Property) pivotElement);
 		}
 		else if (pivotElement instanceof Operation) {
 			return new OperationContext(this, null, (Operation) pivotElement, null);
 		}
 //		else if (pivotElement instanceof Stereotype) {
 //			Stereotype pivotStereotype = (Stereotype) pivotElement;
 //			return new ClassContext(this, null, pivotStereotype);
 //		}
 //		else if (pivotElement instanceof org.eclipse.ocl.examples.pivot.Class) {
 //			org.eclipse.ocl.examples.pivot.Class pivotClass = (org.eclipse.ocl.examples.pivot.Class) pivotElement;
 ////			Metaclass metaClass = getMetaclass(pivotClass);
 //			return new ClassContext(this, null, pivotClass);
 //		}
 		else {		// Class, Stereotype, State
 			for (EObject eObject = element; eObject != null; eObject = eObject.eContainer()) {
 				if ((eObject instanceof Type) && (((Type)eObject).getPackage() != null)) {	// StateMachines etc do not have Packages
 					return new ClassContext(this, null, (Type)eObject);
 				}
 			}
 			return null;
 		}
 	}
 
 	public @NonNull Iterable<? extends DomainPackage> getPartialPackages(@NonNull DomainPackage pkg, boolean loadPivotMetaModelFirst) {
 		if (!libraryLoadInProgress && loadPivotMetaModelFirst && (pivotMetaModel == null)) {
 			getPivotMetaModel();
 		}
 		PackageServer packageServer = packageManager.getPackageServer(pkg);
 		return packageServer.getPartialPackages();
 	}
 
 	public @NonNull Iterable<? extends DomainType> getPartialTypes(@NonNull DomainType pivotType) {
 		TypeServer typeServer = packageManager.getTypeServer(pivotType);
 		return typeServer.getPartialTypes();
 	}
 	
 	public @Nullable DomainPackage getPivotMetaModel() {
 		if ((pivotMetaModel == null) && autoLoadPivotMetaModel) {
 			org.eclipse.ocl.examples.pivot.Package stdlibPackage = null;
 			getOclAnyType();				// Load a default library if necessary.
 			if (!pivotLibraries.isEmpty()) {
 				stdlibPackage = pivotLibraries.get(0);
 			}
 			if (stdlibPackage != null) {
 				loadPivotMetaModel(stdlibPackage);				
 			}
 		}
 		return pivotMetaModel;
 	}
 
 	public @Nullable <T extends Element> T getPivotOf(@NonNull Class<T> pivotClass, @Nullable EObject eObject) throws ParserException {
 		if (eObject != null) {
 			for (Factory factory : factoryMap) {
 				if (factory.canHandle(eObject)) {
 					return factory.getPivotOf(this, pivotClass, eObject);
 				}
 			}
 		}
 		return null;
 	}
 
 	public @Nullable <T extends Element> T getPivotOfEcore(@NonNull Class<T> pivotClass, @Nullable EObject eObject) {
 		if (eObject == null) {
 			return null;
 		}
 		Resource metaModel = eObject.eResource();
 		if (metaModel == null) {
 			return null;
 		}
 		Ecore2Pivot ecore2Pivot = Ecore2Pivot.getAdapter(metaModel, this);
 		if (ecore2Pivot == null) {
 			return null;
 		}
 		return ecore2Pivot.getCreated(pivotClass, eObject);
 	}
 
 	public @NonNull ResourceSet getPivotResourceSet() {
 		return pivotResourceSet;
 	}
 
 	/**
 	 * Return the pivot model class for className with the Pivot Model.
 	 */
 	public @Nullable Type getPivotType(@NonNull String className) {
 		if (pivotMetaModel == null) {
 			getPivotMetaModel();
 			if (pivotMetaModel == null) {
 				return null;
 			}
 		}
 		return (Type) DomainUtil.getNamedElement(pivotMetaModel.getOwnedType(), className);		// FIXME bad cast
 	}	
 
 	@SuppressWarnings("null")
 	protected @NonNull PrecedenceManager getPrecedenceManager() {
 		if (precedenceManager == null) {
 			synchronized (this) {
 				if (precedenceManager == null) {
 					synchronized (this) {
 						precedenceManager = createPrecedenceManager();
 					}
 				}
 			}
 		}
 		return precedenceManager;
 	}
 
 //	public Iterable<? extends Nameable> getPrecedences(org.eclipse.ocl.examples.pivot.Package pivotPackage) {
 //		return pivotPackage.getOwnedPrecedence(); // FIXME make package independent
 //	}
 	
 	public @Nullable Precedence getPrefixPrecedence(@NonNull String operatorName) {
 		PrecedenceManager precedenceManager = getPrecedenceManager();
 		return precedenceManager.getPrefixPrecedence(operatorName);
 	}
 
 	@SuppressWarnings("unchecked")
 	public @NonNull <T extends EObject> T getPrimaryElement(@NonNull T element) {
 		if (element instanceof DomainOperation) {
 			return (T) getPrimaryOperation((DomainOperation)element);
 		}
 		else if (element instanceof DomainPackage) {
 			return (T) getPrimaryPackage((DomainPackage)element);
 		}
 		else if (element instanceof DomainProperty) {
 			return (T) getPrimaryProperty((DomainProperty)element);
 		}
 		else if (element instanceof Type) {
 			return (T) getPrimaryType((DomainType)element);
 		} 
 		return element;
 	}
 
 	public @NonNull DomainOperation getPrimaryOperation(@NonNull DomainOperation pivotOperation) {
 		DomainInheritance pivotClass = pivotOperation.getInheritance(this);
 		if (pivotClass != null) {					// Null for an EAnnotation element
 			TypeServer typeServer = packageManager.findTypeServer(pivotClass);
 			if (typeServer != null) {
 				DomainOperation operation = typeServer.getMemberOperation(pivotOperation);
 				if (operation != null) {
 					return operation;
 				}
 			}
 		}
 		return pivotOperation;
 	}
 
 	/**
 	 * Lookup a primary package by its URI and optionally a sub-package path.
 	 */
 	public @Nullable PackageServer getPrimaryPackage(@NonNull String nsURI, String... subPackagePath) {
 		PackageServer packageServer = packageManager.getPackageByURI(nsURI);
 		if (packageServer == null) {
 			return null;
 		}
 		if (subPackagePath != null) {
 			for (String subPackageName : subPackagePath) {
 				if (subPackageName == null) {
 					return null;
 				}
 				packageServer = packageServer.getMemberPackage(subPackageName);
 				if (packageServer == null) {
 					return null;
 				}
 			}
 		}
 		return packageServer;
 	}
 
 	/**
 	 * Lookup a primary sub-package.
 	 *
 	public @Nullable PackageServer getPrimaryPackage(@NonNull DomainPackage parentPackage, @NonNull String subPackageName) {
 		PackageTracker packageTracker = packageManager.findPackageTracker(parentPackage);
 		if (packageTracker != null) {
 			return packageTracker.getPackageServer().getMemberPackage(subPackageName);
 		}
 		else {
 			return PivotUtil.getNamedElement(parentPackage.getNestedPackage(), subPackageName);
 		}
 	} */
 
 	public @NonNull DomainPackage getPrimaryPackage(@NonNull DomainPackage aPackage) {
 		return getPackageServer(aPackage).getPivotPackage();
 //		PackageTracker packageTracker = packageManager.findPackageTracker(pivotPackage);
 //		if (packageTracker != null) {
 //			return packageTracker.getPrimaryPackage();
 //		}
 //		else {
 //			return pivotPackage;
 //		}
 	}
 
 	public @NonNull DomainProperty getPrimaryProperty(@NonNull DomainProperty pivotProperty) {
 		if ((pivotProperty instanceof Property) && (((Property)pivotProperty).eContainer() instanceof TupleType)) {		// FIXME Find a better way
 			return pivotProperty;
 		}
 		DomainInheritance owningInheritance = pivotProperty.getInheritance(this);
 		if (owningInheritance != null) {
 			@NonNull String name = DomainUtil.nonNullModel(pivotProperty.getName());
 			TypeServer typeServer = packageManager.getTypeServer(owningInheritance);
 			DomainProperty memberProperty = typeServer.getMemberProperty(name);
 			if (memberProperty != null) {
 				return memberProperty;
 			}
 		}
 		return pivotProperty;
 	}
 
 	public @NonNull Type getPrimaryType(@NonNull DomainType type) {
 		if (/*(type instanceof Type) &&*/ !isTypeServeable(type)) {
 			return (Type) type;			// FIXME bad cast
 		}
 		return getTypeServer(type).getPivotType();
 //		TypeTracker typeTracker = packageManager.findTypeTracker(pivotType);
 //		if (typeTracker != null) {
 //			return typeTracker.getPrimaryType();
 //		}
 //		else {
 //			return pivotType;
 //		}
 	}
 
 	public @Nullable org.eclipse.ocl.examples.pivot.Type getPrimaryType(@NonNull String nsURI, @NonNull String path, String... extraPath) {
 		PackageServer packageServer = packageManager.getPackageByURI(nsURI);
 		if (packageServer == null) {
 			return null;
 		}
 		if ((extraPath == null) || (extraPath.length == 0)) {
 			return packageServer.getMemberType(path);
 		}
 		else {
 			packageServer = packageServer.getMemberPackage(path);
 			if (packageServer == null) {
 				return null;
 			}
 			int iMax = extraPath.length-1;
 			for (int i = 0; i < iMax; i++) {
 				String subPackageName = extraPath[i];
 				if (subPackageName == null) {
 					return null;
 				}
 				packageServer = packageServer.getMemberPackage(subPackageName);
 				if (packageServer == null) {
 					return null;
 				}
 			}
 			String subPackageName = extraPath[iMax];
 			if (subPackageName == null) {
 				return null;
 			}
 			return packageServer.getMemberType(subPackageName);
 		}
 	}
 
 	/**
 	 * Lookup a primary type.
 	 *
 	public @Nullable Type getPrimaryType(@NonNull PackageServer parentPackage, @NonNull String typeName) {
 		PackageServer packageServer = packageManager.getPackageServer(parentPackage);
 		return packageServer.getMemberType(typeName);
 //		PackageTracker packageTracker = packageManager.getPackageTracker(parentPackage);
 //		if (packageTracker != null) {
 //			return packageTracker.getPackageServer().getMemberType(typeName);
 //		}
 //		else {
 //			return PivotUtil.getNamedElement(getLocalClasses(parentPackage), typeName);
 //			return PivotUtil.getNamedElement(parentPackage.getOwnedType(), typeName);
 //		}
 	} */
 	
 	/**
 	 * Return the URI to be used for a concrete syntax resource for an expression associated
 	 * with a uniqueContext. If uniqueContext is an Element the moniker is used as
 	 * part of the URI, otherwise a unique value is created and cached for reuse.
 	 * 
 	 * @Deprecated URIs are auto-generated
 	 */
 	@Deprecated
 	public URI getResourceIdentifier(Object uniqueContext, String subContext) {
 		return URI.createURI(EcoreUtil.generateUUID() + ".essentialocl");
 	}
 
 	public @NonNull CollectionType getSequenceType(@NonNull DomainType elementType, @Nullable IntegerValue lower, @Nullable IntegerValue upper) {
 		return getSequenceType(getType(elementType), lower, upper);
 	}
 
 	public @NonNull CollectionType getSequenceType(@NonNull Type elementType, @Nullable IntegerValue lower, @Nullable IntegerValue upper) {
 		return getCollectionType(getSequenceType(), elementType, lower, upper);
 	}
 
 	public @NonNull CollectionType getSetType(@NonNull DomainType elementType, @Nullable IntegerValue lower, @Nullable IntegerValue upper) {
 		return getSetType(getType(elementType), lower, upper);
 	}
 
 	public @NonNull CollectionType getSetType(@NonNull Type elementType, @Nullable IntegerValue lower, @Nullable IntegerValue upper) {
 		return getCollectionType(getSetType(), elementType, lower, upper);
 	}
 
 	protected @NonNull CollectionType getSpecializedCollectionType(@NonNull CollectionType type, @NonNull Map<TemplateParameter, ParameterableElement> usageBindings) {
 		CollectionType unspecializedType = PivotUtil.getUnspecializedTemplateableElement(type);
 		Map<TemplateParameter, ParameterableElement> typeBindings = PivotUtil.getAllTemplateParametersAsBindings(type);
 		PivotUtil.getAllTemplateParameterSubstitutions(typeBindings, type);
 		if ((typeBindings != null) && !typeBindings.isEmpty()) {
 			rebindTemplateBindings(typeBindings, usageBindings);	//	Re-bind the type bindings to use those of the usage.
 			TemplateParameter templateParameter = unspecializedType.getOwnedTemplateSignature().getOwnedParameter().get(0);
 			ParameterableElement templateArgument = typeBindings.get(templateParameter);
 			if (templateArgument == null) {
 				templateArgument = templateParameter.getParameteredElement();
 			}
 			if (templateArgument instanceof Type) {
 				Type templateTypeArgument = (Type)templateArgument;
 				templateTypeArgument = getSpecializedType(templateTypeArgument, usageBindings);
 				return getCollectionType(unspecializedType, templateTypeArgument, null, null);
 			}
 		}
 		return type;
 	}
 
 	protected @NonNull Type getSpecializedLambdaType(@NonNull LambdaType type, @Nullable Map<TemplateParameter, ParameterableElement> usageBindings) {
 		String typeName = DomainUtil.nonNullModel(type.getName());
 		Type contextType = DomainUtil.nonNullModel(type.getContextType());
 		@SuppressWarnings("null") @NonNull List<Type> parameterType = type.getParameterType();
 		Type resultType = DomainUtil.nonNullModel(type.getResultType());
 		LambdaType specializedLambdaType = getLambdaType(typeName, contextType, parameterType, resultType, usageBindings);
 		return specializedLambdaType;
 	}
 
 	protected @NonNull Metaclass getSpecializedMetaclass(@NonNull Metaclass type, @NonNull Map<TemplateParameter, ParameterableElement> usageBindings) {
 		Map<TemplateParameter, ParameterableElement> typeBindings = PivotUtil.getAllTemplateParametersAsBindings(type);
 		PivotUtil.getAllTemplateParameterSubstitutions(typeBindings, type);
 		if ((typeBindings != null) && !typeBindings.isEmpty()) {
 			rebindTemplateBindings(typeBindings, usageBindings);	//	Re-bind the type bindings to use those of the usage.
 			TemplateParameter templateParameter = getMetaclassType().getOwnedTemplateSignature().getOwnedParameter().get(0);
 			ParameterableElement templateArgument = typeBindings.get(templateParameter);
 			if (templateArgument == null) {
 				templateArgument = templateParameter.getParameteredElement();
 				assert templateArgument != null;
 			}
 			if (templateArgument instanceof Type) {
 				templateArgument = getSpecializedType((Type)templateArgument, usageBindings);
 			}
 			return getMetaclass((DomainType) templateArgument);
 		}
 		return type;
 	}
 
 	public @NonNull Type getSpecializedType(@NonNull Type type, @Nullable Map<TemplateParameter, ParameterableElement> usageBindings) {
 		TemplateParameter owningTemplateParameter = type.getOwningTemplateParameter();
 		if (owningTemplateParameter != null) {
 			if (usageBindings == null) {
 				return type;
 			}
 			ParameterableElement parameterableElement = usageBindings.get(owningTemplateParameter);
 			return parameterableElement instanceof Type ? (Type) parameterableElement : type;
 		}
 		else if (usageBindings == null) {
 			return type;
 		}
 		else if (type instanceof CollectionType) {
 			return getSpecializedCollectionType((CollectionType)type, usageBindings);
 		}
 		else if (type instanceof Metaclass) {
 			return getSpecializedMetaclass((Metaclass)type, usageBindings);
 		}
 		else if (type instanceof TupleType) {
 			return getTupleType((TupleType) type, usageBindings);
 		}
 		else if (type instanceof LambdaType) {
 			return getSpecializedLambdaType((LambdaType)type, usageBindings);
 		}
 		else {
 			//
 			//	Get the bindings of the type.
 			//
 			Type unspecializedType = PivotUtil.getUnspecializedTemplateableElement(type);
 			Map<TemplateParameter, ParameterableElement> typeBindings = PivotUtil.getAllTemplateParametersAsBindings(type);
 			PivotUtil.getAllTemplateParameterSubstitutions(typeBindings, type);
 			if ((typeBindings != null) && !typeBindings.isEmpty()) {
 				//
 				//	Re-bind the type bindings to use those of the usage.
 				//
 				rebindTemplateBindings(typeBindings, usageBindings);
 				//
 				//	Prepare the template argument list, one template argument per template parameter.
 				//
 				List<TemplateParameter> templateParameters = PivotUtil.getAllTemplateParameters(unspecializedType);
 				if (templateParameters != null) {
 					List<ParameterableElement> templateArguments = new ArrayList<ParameterableElement>(templateParameters.size());
 					for (TemplateParameter templateParameter : templateParameters) {
 						ParameterableElement templateArgument = typeBindings.get(templateParameter);
 						if (templateArgument == null) {
 							templateArgument = templateParameter.getParameteredElement();
 						}
 						if (templateArgument instanceof Type) {
 							templateArgument = getSpecializedType((Type)templateArgument, usageBindings);
 						}
 						templateArguments.add(templateArgument);
 					}
 					return getLibraryType(unspecializedType, templateArguments);
 				}
 			}
 		}
 		return type;
 	}
 	
 	public Iterable<Type> getSuperClasses(Type pivotType) {
 		if ((pivotType == null) || (pivotType.getOwningTemplateParameter() != null)) {
 			return Collections.<Type>singletonList(getOclAnyType());	// FIXME lower bound
 		}
 		else {
 //			if (type.getTemplateBinding().size() > 0) {		// FIXME need lazy specialization
 //			pivotType = PivotUtil.getUnspecializedTemplateableElement(pivotType);
 //			}
 			if (!libraryLoadInProgress && (pivotMetaModel == null) && (pivotType == getClassType()))  {
 				getPivotMetaModel();
 			}
 			return new CompleteClassSuperClassesIterable(getAllTypes(pivotType));
 		}
 	}
 
 	public ResourceSet getTarget() {
 		return pivotResourceSet;
 	}
 
 	public @NonNull DomainElement getTemplateParameter(@NonNull TemplateParameterId id, DomainElement context) {
 		DomainElement origin = id.getOrigin();
 		if (origin instanceof TemplateParameter) {
 			return DomainUtil.nonNullModel(((TemplateParameter)origin).getParameteredElement());
 		}
 		throw new UnsupportedOperationException();
 	}
 
 	public TupleTypeManager getTupleManager() {
 		if (tupleManager == null) {
 			tupleManager = createTupleManager();
 		}
 		return tupleManager;
 	}
 	
 	public @NonNull DomainTupleType getTupleType(@NonNull List<? extends DomainTypedElement> parts) {
 		return getTupleType(TypeId.TUPLE_NAME, parts, null);
 	}
 
 	public @NonNull TupleType getTupleType(@NonNull String typeName, @NonNull Collection<? extends DomainTypedElement> parts,
 			@Nullable Map<TemplateParameter, ParameterableElement> bindings) {
 		TupleTypeManager tupleManager = getTupleManager();
 		return tupleManager.getTupleType(typeName, parts, bindings);
 	}
 	
 	public @NonNull TupleType getTupleType(@NonNull TupleType tupleType, @Nullable Map<TemplateParameter, ParameterableElement> bindings) {
 		TupleTypeManager tupleManager = getTupleManager();
 		return tupleManager.getTupleType(tupleType, bindings);
 	}
 
 	public @NonNull Type getType(@NonNull DomainType dType) {				// FIXME simplify eliminate
 		if (dType instanceof Type) {
 			return getPrimaryType(dType);
 		}
 		DomainPackage dPackage = dType.getPackage();
		String nsURI = DomainUtil.nonNullState(dPackage.getNsURI());
		PackageServer packageServer = packageManager.getPackageByURI(nsURI);
		if (packageServer == null) {
			throw new UnsupportedOperationException();		// FIXME
		}
 		Type primaryType = packageServer.getMemberType(dType.getName());
 		if (primaryType == null) {
 			throw new UnsupportedOperationException();		// FIXME
 		}
 		return primaryType;
 	}
 	
 	public @NonNull TypeServer getTypeServer(@NonNull DomainType pivotType) {
 		if (!libraryLoadInProgress && (pivotMetaModel == null) && !(pivotType instanceof CollectionType)) {
 			getPivotMetaModel();
 		}
 		return packageManager.getTypeServer(pivotType);
 	}
 
 	@Override
 	public @Nullable
 	DomainType getTypeTemplateParameter(@NonNull DomainType aType, int index) {
 		if (aType instanceof Type) {
 			aType = PivotUtil.getUnspecializedTemplateableElement((Type)aType);
 		}
 		TypeTemplateParameter templateParameter = (TypeTemplateParameter) aType.getTypeParameters().get(index);
 		if (templateParameter == null) {
 			throw new UnsupportedOperationException();
 		}
 		ParameterableElement parameteredElement = templateParameter.getParameteredElement();
 		if (!(parameteredElement instanceof DomainType)) {
 			throw new UnsupportedOperationException();
 		}
 		return (DomainType) parameteredElement;
 	}
 
 	/**
 	 * Create implicit an opposite property if there is no explicit opposite.
 	 * <p>
 	 * Returns a list of opposite properties that have ambiguous names as a result
 	 * of establishing an implicit opposite.
 	 */
 	public @Nullable List<Property> installPropertyDeclaration(@NonNull Property thisProperty) {
 		if ((thisProperty.isTransient() || thisProperty.isVolatile()) && !thisProperty.isDerived()) {		// FIXME Are any exclusions justified?
 			return null;
 		}
 		Property opposite = thisProperty.getOpposite();
 		if (opposite != null) {
 			return null;
 		}
 		Type thatType = thisProperty.getType();
 		if (thatType instanceof CollectionType) {
 			thatType = ((CollectionType)thatType).getElementType();
 		}
 		if ((thatType == null) || (thatType instanceof DataType)) {
 			return null;
 		}
 		Type thisType = thisProperty.getOwningType();
 		if (thisType == null) {								// e.g. an EAnnotation
 			return null;
 		}
 		String name = thisType.getName();
 		// If there is an explicit property with the implicit name do nothing.
 		List<Property> ambiguousOpposites = null;
 		for (Property thatProperty : thatType.getOwnedAttribute()) {
 			if (name.equals(thatProperty.getName())) {
 				if (!thatProperty.isImplicit()) {
 					return null;
 				}
 				if (ambiguousOpposites == null) {
 					ambiguousOpposites = new ArrayList<Property>();
 				}
 				ambiguousOpposites.add(thatProperty);
 			}
 		}
 		// If there is an implicit property with the implicit name, set its opposite null
 		//   and do no more; result one name with no opposites
 //		if (ambiguousOpposite != null) {
 //			opposite.setOpposite(null);
 //			thisProperty.setOpposite(null);
 //FIXME			opposite.setUpper(BigInteger.valueOf(-1));
 //			return null;
 //		}
 		// If there is more than one opposite-less Property to the same type don't create an Ambiguity.
 //		for (Property aThisProperty : thisType.getOwnedAttribute()) {
 //			if ((aThisProperty != thisProperty) && (aThisProperty.getType() == thatType)) {	// FIXME conformsTo
 //				return null;
 //			}
 //		}
 		// If there is no implicit property with the implicit name, create one
 		//   result a pair of mutual opposites		
 		Property newOpposite = PivotFactory.eINSTANCE.createProperty();
 		newOpposite.setImplicit(true);
 		newOpposite.setName(name);
 //		newOpposite.setType(thisType);
 //		newOpposite.setLower(BigInteger.valueOf(0));
 		if (thisProperty.isComposite()) {
 //			newOpposite.setUpper(BigInteger.valueOf(1));
 			newOpposite.setType(thisType);
 			newOpposite.setIsRequired(false);
 		}
 		else {
 //			newOpposite.setUpper(BigInteger.valueOf(-1));
 			newOpposite.setType(getSetType(thisType, null, null));
 			newOpposite.setIsRequired(true);
 		}
 		thatType.getOwnedAttribute().add(newOpposite);		// WIP moved for debugging
 		newOpposite.setOpposite(thisProperty);
 		thisProperty.setOpposite(newOpposite);
 		if (ambiguousOpposites != null) {
 			ambiguousOpposites.add(newOpposite);
 		}
 		return ambiguousOpposites;
 	}
 
 	public void installResource(@NonNull Resource pivotResource) {
 		for (EObject eObject : pivotResource.getContents()) {
 			if (eObject instanceof Root) {
 				installRoot((Root)eObject);
 			}
 		}
 	}
 
 	public void installRoot(@NonNull Root pivotRoot) {
 		packageManager.addRoot(pivotRoot);
 		for (DomainPackage pivotPackage : pivotRoot.getNestedPackage()) {
 			if ((pivotPackage instanceof Library) && !pivotLibraries.contains(pivotPackage)) {
 				Library pivotLibrary = (Library)pivotPackage;
 				String uri = pivotLibrary.getNsURI();
 				if (pivotLibraries.isEmpty()) {
 					if (uri == null) {
 						throw new IllegalLibraryException(OCLMessages.MissingLibraryURI_ERROR_);
 					}
 					setDefaultStandardLibraryURI(uri);
 				}
 				else {
 					String libraryURI = getDefaultStandardLibraryURI();
 					if ((uri != null) && !uri.equals(libraryURI)) {
 						throw new IllegalLibraryException(NLS.bind(OCLMessages.ImportedLibraryURI_ERROR_, uri , libraryURI));
 					}
 				}
 				pivotLibraries.add(pivotLibrary);
 			}
 		}
 	}
 
 	public boolean isAdapterFor(@NonNull MetaModelManager metaModelManager) {
 		return this == metaModelManager;
 	}
 
 	public boolean isAdapterForType(Object type) {
 		return type == MetaModelManager.class;
 	}
 
 	public boolean isSuperClassOf(@NonNull Type unspecializedFirstType, @NonNull Type secondType) {
 		Type unspecializedSecondType = PivotUtil.getUnspecializedTemplateableElement(secondType);	// FIXME cast
 		if (unspecializedFirstType == unspecializedSecondType) {
 			return true;
 		}
 		for (Type superClass : getSuperClasses(unspecializedSecondType)) {
 			if ((superClass != null) && isSuperClassOf(unspecializedFirstType, superClass)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public boolean isTypeServeable(@NonNull DomainType type) {
 		if (!(type instanceof Type)) {
 			return false;			
 		}
 		Type pivotType = (Type)type;
 //		if (pivotType .getUnspecializedElement() != null) {
 //			return false;
 //		}
 		if (pivotType.getOwningTemplateParameter() != null) {
 			return false;
 		}
 //		if (pivotType instanceof UnspecifiedType) {
 //			return false;
 //		}
 		if (pivotType instanceof LambdaType) {
 			return false;
 		}
 //		if (pivotType instanceof TupleType) {
 //			return false;
 //		}
 		if (pivotType.eContainer() instanceof TemplateParameterSubstitution) {
 			return false;
 		}
 		return true;
 	}
 	
 	/**
 	 * Retyurn true if this type involves an UnspecifiedType.
 	 */
 	public boolean isUnderspecified(@Nullable ParameterableElement type) {
 		if (type == null) {
 			return false;
 		}
 		if (type instanceof TemplateableElement) {
 			for (TemplateBinding templateBinding : ((TemplateableElement)type).getTemplateBinding()) {
 				for (TemplateParameterSubstitution templateParameterSubstitution : templateBinding.getParameterSubstitution()) {
 					if (isUnderspecified(templateParameterSubstitution.getActual())) {
 						return true;
 					}
 				}
 			}
 		}
 		if (type instanceof UnspecifiedType) {
 			return true;
 		}
 		if (type instanceof CollectionType) {
 			return isUnderspecified(((CollectionType)type).getElementType());
 		}
 		if (type instanceof TupleType) {
 			for (Property part : ((TupleType)type).getOwnedAttribute()) {
 				if (isUnderspecified(part.getType())) {
 					return true;
 				}
 			}
 		}
 		if (type instanceof LambdaType) {
 			LambdaType lambdaType = (LambdaType)type;
 			if (isUnderspecified(lambdaType.getContextType())) {
 				return true;
 			}
 			for (Type parameterType : lambdaType.getParameterType()) {
 				if (isUnderspecified(parameterType)) {
 					return true;
 				}
 			}
 			if (isUnderspecified(lambdaType.getResultType())) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	protected boolean isUnspecialized(@NonNull List<TemplateParameter> templateParameters,
 			@NonNull List<? extends ParameterableElement> templateArguments) {
 		int iMax = templateParameters.size();
 		assert templateArguments.size() == iMax;
 		boolean isUnspecialized = true;
 		for (int i = 0; i < iMax; i++) {
 			if (templateArguments.get(i) != templateParameters.get(i).getParameteredElement()) {
 				isUnspecialized = false;
 			}
 		}
 		return isUnspecialized;
 	}
 
 	@Override
 	protected @Nullable Resource loadDefaultLibrary(@Nullable String uri) {
 		boolean savedLibraryLoadInProgress = libraryLoadInProgress;
 		libraryLoadInProgress = true;
 		try {
 			if (pivotLibraries.isEmpty()) {
 				if (uri == null) {
 					return null;
 				}
 				StandardLibraryContribution contribution = StandardLibraryContribution.REGISTRY.get(uri);
 				if (contribution == null) {
 					return null;
 				}
 				installResource(contribution.getResource());
 			}
 			assert pivotLibraryResource == null;
 			if (!pivotLibraries.isEmpty()) {
 				pivotLibraryResource = pivotLibraries.get(0).eResource();
 				for (Library pivotLibrary : pivotLibraries) {
 					if (pivotLibrary != null) {
 						PackageServer packageServer = packageManager.getPackageServer(pivotLibrary);
 						packageServer.getMemberTypes();			// FIXME side effect of creating type trackers
 						for (Type type : pivotLibrary.getOwnedType()) {
 							if (type != null) {
 								Type primaryType = getPrimaryType(type);
 								if ((type == primaryType) && PivotUtil.isLibraryType(type)) {
 									defineLibraryType(primaryType);
 								}
 							}
 						}
 					}
 				}
 			}
 			return pivotLibraryResource;
 		}
 		finally {
 			libraryLoadInProgress = savedLibraryLoadInProgress;
 		}
 	}
 
 	/**
 	 * Load the Pivot MetaModel of the Pivot Model to accompany a given pivotLibrary.
 	 * 
 	 * If this pivotLibrary has an Element type it is assumed to be a complete custom meta-model and it is used as such.
 	 * 
 	 * Otherwise the built-in Pivot Metamodel is created with name, nsPrefix and nsURI determined by the given library.
 	 * 
 	 * @param pivotLibrary
 	 */
 	protected void loadPivotMetaModel(@NonNull org.eclipse.ocl.examples.pivot.Package pivotLibrary) {
 		for (DomainPackage libPackage : getPartialPackages(pivotLibrary, false)) {
 			if (DomainUtil.getNamedElement(libPackage.getOwnedType(), PivotPackage.Literals.ELEMENT.getName()) != null) {
 				setPivotMetaModel(libPackage);	// Custom meta-model
 				return;
 			}
 		}
 		String name = DomainUtil.nonNullState(pivotLibrary.getName());
 		String nsURI = DomainUtil.nonNullState(pivotLibrary.getNsURI());
 		org.eclipse.ocl.examples.pivot.Package oclMetaModel = OCLMetaModel.create(this, name, pivotLibrary.getNsPrefix(), nsURI);
 		setPivotMetaModel(oclMetaModel);		// Standard meta-model
 		@SuppressWarnings("null")
 		@NonNull Resource pivotResource = oclMetaModel.eResource();
 //		pivotResourceSet.getResources().add(pivotResource);
 		installResource(pivotResource);
 		packageManager.addPackageNsURISynonym(OCLMetaModel.PIVOT_URI, nsURI);
 	}
 
 	public @Nullable Element loadResource(@NonNull URI uri, String alias, ResourceSet resourceSet) throws ParserException {
 		// if (EPackage.Registry.INSTANCE.containsKey(resourceOrNsURI))
 		// return EPackage.Registry.INSTANCE.getEPackage(resourceOrNsURI);
 		Resource resource;
 		URI resourceURI = uri.trimFragment();
 		String resourceURIstring = resourceURI.toString();
 		if (resourceURIstring.equals(defaultStandardLibraryURI)) {
 			if (pivotLibraryResource != null) {
 				resource = pivotLibraryResource;
 			}
 			else {
 				resource = loadDefaultLibrary(resourceURIstring);
 			}
 			if (resource instanceof XMLResource) {
 				EObject eObject = ((XMLResource)resource).getEObject(uri.fragment());
 				if (eObject instanceof Element) {
 					return (Element) eObject;
 				}
 			}
 		}
 		else {
 			StandardLibraryContribution contribution = StandardLibraryContribution.REGISTRY.get(resourceURIstring);
 			if (contribution != null) {
 				resource = contribution.getResource();
 			}
 			else {
 				External2Pivot external2Pivot = external2PivotMap.get(uri);
 				if (external2Pivot != null) {
 					resource = external2Pivot.getResource();
 				}
 				else {
 					if (resourceSet == null) {
 						resourceSet = getExternalResourceSet();
 					}
 					
 					try {
 						resource = resourceSet.getResource(resourceURI, true);
 					}
 					catch (RuntimeException e) {
 						resource = resourceSet.getResource(resourceURI, false);
 						if (resource != null) {
 							resourceSet.getResources().remove(resource);
 							resource = null;
 						}
 						throw e;
 					}
 //					if (resource != null) {
 //						if (externalResources == null) {
 //							externalResources = new HashMap<URI, Resource>();
 //						}
 //						externalResources.put(uri, resource);
 //					}
 					//
 					//	If this resource already loaded under its internal URI reuse old one
 					//
 					if (resource != null) { 
 						List<EObject> contents = resource.getContents();
 						if (contents.size() > 0) {
 							EObject firstContent = contents.get(0);
 							if (firstContent != null) {
 								for (Factory factory : factoryMap) {
 									URI packageURI = factory.getPackageURI(firstContent);
 									if (packageURI != null) {
 										External2Pivot external2Pivot2 = external2PivotMap.get(packageURI);
 										if (external2Pivot2 != null) {
 											resource = external2Pivot2.getResource();
 										}
 										break;
 									}
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 		if (resource != null) {
 			return loadResource(resource, uri);
 		}
 		logger.warn("Cannot load package with URI '" + uri + "'");
 		return null;
 	}
 
 	public @Nullable Element loadResource(@NonNull Resource resource, @Nullable URI uri) throws ParserException {
 		for (Factory factory : factoryMap) {
 			if (factory.canHandle(resource)) {
 				return factory.importFromResource(this, resource, uri);
 			}
 		}
 		throw new ParserException("Cannot create pivot from '" + uri + "'");
 //		logger.warn("Cannot convert to pivot for package with URI '" + uri + "'");
 	}
 
 	public @NonNull LibraryFeature lookupImplementation(@NonNull DomainOperation dynamicOperation) throws SecurityException, IllegalArgumentException, ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
 		return getImplementation((Operation) dynamicOperation);
 	}
 
 //	public DomainOperation zzlookupDynamicOperation(DomainType type, DomainOperation staticOperation) {
 //		return getDynamicOperation(getType(type), getOperation(staticOperation));
 //	}
 
 	public void notifyChanged(Notification notification) {
 	}
 
 	//
 	//	Re-bind the type bindings to use those of the usage.
 	//
 	protected void rebindTemplateBindings(@NonNull Map<TemplateParameter, ParameterableElement> typeBindings,
 			@NonNull Map<TemplateParameter, ParameterableElement> usageBindings) {
 		for (TemplateParameter templateParameter : typeBindings.keySet()) {
 			ParameterableElement parameterableElement = typeBindings.get(templateParameter);
 			if (parameterableElement != null) {
 				TemplateParameter aTemplateParameter = parameterableElement.getOwningTemplateParameter();
 				if (aTemplateParameter != null) {
 					ParameterableElement aParameterableElement = usageBindings.get(aTemplateParameter);
 					if (aParameterableElement != null) {
 						typeBindings.put(templateParameter, aParameterableElement);
 					}
 				}
 				else if (parameterableElement instanceof SelfType) {
 					ParameterableElement aParameterableElement = usageBindings.get(null);
 					if (aParameterableElement != null) {
 						typeBindings.put(templateParameter, aParameterableElement);
 					}
 				}
 			}
 		}
 	}
 
 	public void removeExternalResource(@NonNull External2Pivot external2Pivot) {
 		external2PivotMap.remove(external2Pivot.getURI());
 	}
 
 	public void removeListener(@NonNull MetaModelManagerListener listener) {
 		if (listeners != null) {
 			listeners.remove(listener);
 		}
 	}
 
 	/**
 	 * Return all matching operations.
 	 */
 	protected void resolveAllOperations(@NonNull Set<Operation> allOperations, @NonNull Type forType, boolean selectStatic, @NonNull String operationName, @NonNull List<Parameter> parameters) {
 		int iMax = parameters.size();
 		for (DomainOperation candidateOperation : getAllOperations(forType, selectStatic, operationName)) {
 			if (candidateOperation instanceof Operation) {
 				List<Parameter> candidateParameters = ((Operation)candidateOperation).getOwnedParameter();
 				if (candidateParameters.size() == iMax) {
 					int i = 0;
 					for ( ; i < iMax; i++) {
 						Type type = parameters.get(i).getType();
 						Type candidateType = candidateParameters.get(i).getType();
 						if (type != candidateType) {		// FIXME behavioural equivalence
 							break;
 						}
 					}
 					if (i >= iMax) {
 						allOperations.add((Operation)candidateOperation);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Return the un-overloaded operation.
 	 */
 	public @NonNull Operation resolveBaseOperation(@NonNull Operation operation) {
 		Set<Operation> allOperations = new HashSet<Operation>();
 		@SuppressWarnings("null") @NonNull Type owningType = operation.getOwningType();
 		@SuppressWarnings("null") @NonNull String operationName = operation.getName();
 		@NonNull List<Parameter> ownedParameter = operation.getOwnedParameter();
 		resolveAllOperations(allOperations, owningType, operation.isStatic(), operationName, ownedParameter);
 		Operation baseOperation = operation;
 		for (Operation candidateOperation : allOperations) {
 			if (candidateOperation != operation) {
 				@SuppressWarnings("null") @NonNull Type baseType = baseOperation.getOwningType();
 				@SuppressWarnings("null") @NonNull Type candidateType = candidateOperation.getOwningType();
 				if (conformsTo(baseType, candidateType, null)) {
 					baseOperation = candidateOperation;
 				}
 			}
 		}
 		return baseOperation;
 	}
 
 	public @Nullable Set<Operation> resolveLocalOperation(@NonNull Type pivotClass, @NonNull String operationName, Type... pivotArguments) {
 		@Nullable Map<TemplateParameter, ParameterableElement> templateParameterSubstitutions = null; 	// FIXME
 		Set<Operation> pivotOperations = null;
 		for (Operation pivotOperation : pivotClass.getOwnedOperation()) {
 			if (operationName.equals(pivotOperation.getName())) {
 				List<Parameter> pivotParameters = pivotOperation.getOwnedParameter();
 				if (pivotArguments.length == pivotParameters.size()) {
 					boolean typesConform = true;
 					for (int i = 0; i < pivotArguments.length; i++) {
 						Type argumentType = pivotArguments[i];
 						if (argumentType == null) {
 							typesConform = false;
 							break;
 						}
 						Parameter pivotParameter = pivotParameters.get(i);
 						if (pivotParameter == null) {
 							typesConform = false;
 							break;
 						}
 						Type parameterType = PivotUtil.getBehavioralType(pivotParameter);
 						if (parameterType instanceof SelfType) {
 							parameterType = pivotOperation.getOwningType();
 						}
 						if (parameterType == null) {
 							typesConform = false;
 							break;
 						}
 						if (!conformsTo(argumentType, parameterType, templateParameterSubstitutions)) {
 							typesConform = false;
 							break;
 						}
 					}
 					if (typesConform) {
 						if (pivotOperations == null) {
 							pivotOperations = new HashSet<Operation>();
 						}
 						pivotOperations.add(pivotOperation);
 					}
 				}
 			}
 		}
 		return pivotOperations;
 	}
 
 	public Operation resolveOperation(@NonNull Type leftType, @NonNull String operationName, Type... rightTypes) {
 		Set<Operation> candidateOperations = resolveOperations(leftType, operationName, rightTypes);
 		if (candidateOperations == null) {
 			return null;
 		}
 		if (candidateOperations.size() > 1) {
 			logger.warn("Ambiguous operation '" + operationName + "'");
 		}
 		return candidateOperations.iterator().next();
 	}
 
 	public Set<Operation> resolveOperations(@NonNull Type pivotClass, @NonNull String operationName, Type... pivotArguments) {
 		@SuppressWarnings("unused")
 		Map<TemplateParameter, ParameterableElement> templateParameterSubstitutions = PivotUtil.getAllTemplateParameterSubstitutions(null, pivotClass);
 		Set<Operation> pivotOperations = resolveLocalOperation(pivotClass, operationName, pivotArguments);
 		for (TemplateBinding templateBinding : pivotClass.getTemplateBinding()) {
 			TemplateSignature signature = templateBinding.getSignature();
 			TemplateableElement template = signature.getTemplate();
 			if (template instanceof Type) {
 				Set<Operation> morePivotOperations = resolveLocalOperation((Type) template, operationName, pivotArguments);
 				if (morePivotOperations != null) {
 					if (pivotOperations == null) {
 						pivotOperations = morePivotOperations;
 					}
 					else {
 						pivotOperations.addAll(morePivotOperations);
 					}
 				}
 			}
 		}
 		if (pivotOperations == null) {
 			List<Type> superClasses = pivotClass.getSuperClass();
 			if (!superClasses.isEmpty()) {
 				for (Type superClass : superClasses) {
 					if (superClass != null) {
 						Set<Operation> superOperations = resolveOperations(superClass, operationName, pivotArguments);
 						if (superOperations != null) {
 							if (pivotOperations == null) {
 								pivotOperations = superOperations;
 							} else {
 								pivotOperations.addAll(superOperations);
 							}
 						}
 					}
 				}
 			}
 			else {
 				AnyType oclAnyType = getOclAnyType();
 				if (pivotClass != oclAnyType) {		// Typically a template parameter type
 					pivotOperations = resolveOperations(oclAnyType, operationName, pivotArguments);
 				}
 			}
 		}
 		return pivotOperations;
 	}
 
 	public void setAutoLoadPivotMetaModel(boolean autoLoadPivotMetaModel) {
 		this.autoLoadPivotMetaModel  = autoLoadPivotMetaModel;
 	}
 
 	public void setDefaultStandardLibraryURI(@NonNull String defaultStandardLibraryURI) {
 		this.defaultStandardLibraryURI = defaultStandardLibraryURI;
 	}
 
 	public void setLibraryLoadInProgress(boolean libraryLoadInProgress) {
 		this.libraryLoadInProgress = libraryLoadInProgress;	
 	}
 
 	public void setPivotMetaModel(DomainPackage pivotPackage) {
 		pivotMetaModel = pivotPackage;
 	}
 
 	public void setTarget(Notifier newTarget) {
 //		assert newTarget == pivotResourceSet;
 	}
 
 	public void unsetTarget(Notifier oldTarget) {
 //		assert oldTarget == pivotResourceSet;
 	}
 }
