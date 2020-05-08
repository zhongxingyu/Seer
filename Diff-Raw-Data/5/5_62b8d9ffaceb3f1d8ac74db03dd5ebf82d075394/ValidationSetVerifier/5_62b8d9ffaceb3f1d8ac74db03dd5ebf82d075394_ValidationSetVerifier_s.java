 package org.eclipse.b3.aggregator.engine;
 
 import static java.lang.String.format;
 
 import java.io.File;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.eclipse.b3.aggregator.Aggregation;
 import org.eclipse.b3.aggregator.Configuration;
 import org.eclipse.b3.aggregator.Contribution;
 import org.eclipse.b3.aggregator.MappedRepository;
 import org.eclipse.b3.aggregator.MappedUnit;
 import org.eclipse.b3.aggregator.MetadataRepositoryReference;
 import org.eclipse.b3.aggregator.PackedStrategy;
 import org.eclipse.b3.aggregator.ValidationSet;
 import org.eclipse.b3.aggregator.impl.AggregationImpl;
 import org.eclipse.b3.aggregator.util.InstallableUnitUtils;
 import org.eclipse.b3.aggregator.util.SpecialQueries;
 import org.eclipse.b3.aggregator.util.VerificationDiagnostic;
 import org.eclipse.b3.p2.MetadataRepository;
 import org.eclipse.b3.p2.util.P2Bridge;
 import org.eclipse.b3.p2.util.P2Utils;
 import org.eclipse.b3.util.ExceptionUtils;
 import org.eclipse.b3.util.LogUtils;
 import org.eclipse.b3.util.MonitorUtils;
 import org.eclipse.b3.util.TimeUtils;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.MultiStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.SubMonitor;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.equinox.internal.p2.director.Explanation;
 import org.eclipse.equinox.internal.p2.director.Explanation.HardRequirement;
 import org.eclipse.equinox.internal.p2.director.Explanation.MissingGreedyIU;
 import org.eclipse.equinox.internal.p2.director.Explanation.MissingIU;
 import org.eclipse.equinox.internal.p2.director.Explanation.PatchedHardRequirement;
 import org.eclipse.equinox.internal.p2.director.Explanation.Singleton;
 import org.eclipse.equinox.internal.p2.director.ProfileChangeRequest;
 import org.eclipse.equinox.internal.p2.director.QueryableArray;
 import org.eclipse.equinox.internal.p2.engine.InstallableUnitOperand;
 import org.eclipse.equinox.internal.p2.engine.Operand;
 import org.eclipse.equinox.internal.p2.engine.ProvisioningPlan;
 import org.eclipse.equinox.internal.p2.metadata.IRequiredCapability;
 import org.eclipse.equinox.internal.p2.touchpoint.eclipse.PublisherUtil;
 import org.eclipse.equinox.internal.provisional.p2.director.PlannerStatus;
 import org.eclipse.equinox.internal.provisional.p2.director.RequestStatus;
 import org.eclipse.equinox.p2.core.ProvisionException;
 import org.eclipse.equinox.p2.engine.IProfile;
 import org.eclipse.equinox.p2.engine.IProfileRegistry;
 import org.eclipse.equinox.p2.engine.ProvisioningContext;
 import org.eclipse.equinox.p2.metadata.IArtifactKey;
 import org.eclipse.equinox.p2.metadata.IInstallableUnit;
 import org.eclipse.equinox.p2.metadata.IInstallableUnitPatch;
 import org.eclipse.equinox.p2.metadata.IRequirement;
 import org.eclipse.equinox.p2.metadata.IRequirementChange;
 import org.eclipse.equinox.p2.metadata.Version;
 import org.eclipse.equinox.p2.planner.IPlanner;
 import org.eclipse.equinox.p2.query.IQuery;
 import org.eclipse.equinox.p2.query.IQueryResult;
 import org.eclipse.equinox.p2.query.IQueryable;
 import org.eclipse.equinox.p2.query.QueryUtil;
 import org.eclipse.equinox.p2.repository.artifact.IArtifactRepository;
 import org.eclipse.equinox.p2.repository.artifact.IArtifactRepositoryManager;
 import org.eclipse.equinox.p2.repository.artifact.IFileArtifactRepository;
 import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
 import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
 
 public class ValidationSetVerifier extends BuilderPhase {
 
 	public static class AnalyzedPlannerStatus extends MultiStatus {
 
 		private static final String MESSAGE_INDENT = "  ";
 
 		private static void appendChildren(StringBuilder messageBuilder, IStatus[] children, String indent, int level) {
 			for(IStatus child : children) {
 				for(int i = 0; i < level; ++i)
 					messageBuilder.append(indent);
 				messageBuilder.append(child.getMessage()).append('\n');
 				if(child.isMultiStatus())
 					appendChildren(messageBuilder, child.getChildren(), indent, level + 1);
 			}
 		}
 
 		private static StringBuilder getRootProblemMessage(Explanation rootProblem) {
 			IStatus status = rootProblem.toStatus();
 			StringBuilder messageBuilder = new StringBuilder(status.getMessage()).append('\n');
 
 			if(status.isMultiStatus())
 				appendChildren(messageBuilder, status.getChildren(), MESSAGE_INDENT, 1);
 
 			return messageBuilder;
 		}
 
 		protected PlannerStatus plannerStatus;
 
 		protected ArrayList<VerificationDiagnostic> verificationDiagnostics = new ArrayList<VerificationDiagnostic>();
 
 		public AnalyzedPlannerStatus(Resource resource, Configuration config, PlannerStatus plannerStatus) {
 			super(
 				plannerStatus.getPlugin(), plannerStatus.getCode(), plannerStatus.getMessage(),
 				plannerStatus.getException());
 			this.plannerStatus = plannerStatus;
 
 			RequestStatus requestStatus = plannerStatus.getRequestStatus();
 			if(requestStatus == null)
 				return;
 
 			Set<Explanation> explanations = requestStatus.getExplanations();
 			if(explanations == null)
 				return;
 
 			// The set of the root problem explanations
 			LinkedHashSet<Explanation> rootProblems = new LinkedHashSet<Explanation>();
 
 			// The map of dependency chain explanations
 			HashMap<IInstallableUnit, HashSet<IRequirement>> links = new HashMap<IInstallableUnit, HashSet<IRequirement>>();
 
 			for(Explanation explanation : explanations) {
 				if(explanation instanceof HardRequirement) {
 					// This represents one link in the chain of dependencies from the root requirement
 					// (the verification IU) to the conflicting/missing IU
 					HardRequirement link = (HardRequirement) explanation;
 					HashSet<IRequirement> requirementSet = links.get(link.iu);
 					if(requirementSet == null) {
 						requirementSet = new HashSet<IRequirement>();
 						links.put(link.iu, requirementSet);
 					}
 
 					requirementSet.add(link.req);
 				}
 				else if(explanation instanceof PatchedHardRequirement) {
 					// This represents one link in the chain of dependencies from the root requirement
 					// (the verification IU) to the conflicting/missing IU
 					PatchedHardRequirement link = (PatchedHardRequirement) explanation;
 					HashSet<IRequirement> requirementSet = links.get(link.iu);
 					if(requirementSet == null) {
 						requirementSet = new HashSet<IRequirement>();
 						links.put(link.iu, requirementSet);
 					}
 
 					for(IRequirementChange change : link.patch.getRequirementsChange()) {
 						if(change.newValue().equals(link.req)) {
 							for(IRequirement r : link.iu.getRequirements()) {
 								if(r instanceof IRequiredCapability && change.matches((IRequiredCapability) r))
 									requirementSet.add(r);
 							}
 						}
 					}
 
 					requirementSet = links.get(link.patch);
 					if(requirementSet == null) {
 						requirementSet = new HashSet<IRequirement>();
 						links.put(link.patch, requirementSet);
 					}
 					requirementSet.add(link.req);
 				}
 				else if(explanation instanceof MissingIU || explanation instanceof MissingGreedyIU ||
 						explanation instanceof Singleton)
 					// MissingIU means we have a missing dependency problem
 					// Singleton means we have a dependency version conflict problem
 					rootProblems.add(explanation);
 			}
 
 			// a cache of IInstallableUnit parents
 			HashMap<IInstallableUnit, VerificationDiagnostic.DependencyLink> dependencyChainsCache = new HashMap<IInstallableUnit, VerificationDiagnostic.DependencyLink>();
 			for(Explanation rootProblem : rootProblems) {
 				if(rootProblem instanceof Singleton) {
 					IInstallableUnit[] ius = ((Singleton) rootProblem).ius;
 					LinkedHashSet<VerificationDiagnostic.DependencyLink> dependencyChains = new LinkedHashSet<VerificationDiagnostic.DependencyLink>(
 						ius.length);
 
 					for(IInstallableUnit iu : ius) {
 						dependencyChains.add(getDependencyChain(iu, links, dependencyChainsCache));
 					}
 					// just in case we failed to construct some dependency chain
 					dependencyChains.remove(null);
 
 					org.eclipse.emf.common.util.URI resourceURI = resource.getURI();
 					LinkedHashSet<org.eclipse.emf.common.util.URI> modelElementURISet = new LinkedHashSet<org.eclipse.emf.common.util.URI>(
 						dependencyChains.size());
 					StringBuilder messageBuilder = getRootProblemMessage(rootProblem);
 
 					for(VerificationDiagnostic.DependencyLink dependencyChain : dependencyChains) {
 						VerificationDiagnostic.identifyDependencyChain(dependencyChain, resource, "\n", MESSAGE_INDENT);
 
 						modelElementURISet.add(dependencyChain.getModelElementURI().deresolve(resourceURI));
 
 						messageBuilder.append('\n');
 						InstallableUnitUtils.appendIdentifier(messageBuilder, dependencyChain.getInstallableUnit());
 						messageBuilder.append(" is required by:");
 
 						VerificationDiagnostic.DependencyLink parent = dependencyChain.getParent();
 						if(parent != null)
 							messageBuilder.append(parent.getIdentifier());
 					}
 
 					String message = messageBuilder.toString();
 					LogUtils.error(message);
 					add(new Status(IStatus.ERROR, plannerStatus.getPlugin(), message));
 
 					// just in case we could not find URI of a model element corresponding to some of the dependency chains
 					modelElementURISet.remove(null);
 
 					VerificationDiagnostic.Singleton[] relatedDiagnostics = new VerificationDiagnostic.Singleton[modelElementURISet.size()];
 					int i = 0;
 					for(org.eclipse.emf.common.util.URI modelElementURI : modelElementURISet) {
 						VerificationDiagnostic.Singleton singleton = new VerificationDiagnostic.Singleton(
 							rootProblem, modelElementURI, relatedDiagnostics);
 						relatedDiagnostics[i++] = singleton;
 						verificationDiagnostics.add(singleton);
 					}
 				}
 				else if(rootProblem instanceof MissingIU) {
 					MissingIU missingIU = ((MissingIU) rootProblem);
 					VerificationDiagnostic.DependencyLink dependencyChain = getDependencyChain(
 						missingIU.iu, links, dependencyChainsCache);
 
 					if(dependencyChain != null) {
 						VerificationDiagnostic.identifyDependencyChain(dependencyChain, resource, "\n", MESSAGE_INDENT);
 						StringBuilder messageBuilder = getRootProblemMessage(rootProblem);
 
 						messageBuilder.append('\n');
 						InstallableUnitUtils.appendIdentifier(messageBuilder, missingIU.req);
 						messageBuilder.append(" is required by:");
 						messageBuilder.append(dependencyChain.getIdentifier());
 
 						String message = messageBuilder.toString();
 						LogUtils.error(message);
 						add(new Status(IStatus.ERROR, plannerStatus.getPlugin(), message));
 
 						org.eclipse.emf.common.util.URI modelElementURI = dependencyChain.getModelElementURI();
 
 						if(modelElementURI != null)
 							verificationDiagnostics.add(new VerificationDiagnostic(
 								rootProblem.toString(), dependencyChain.getModelElementURI().deresolve(
 									resource.getURI())));
 					}
 				}
 				else if(rootProblem instanceof MissingGreedyIU) {
 					MissingGreedyIU missingGreedyIU = ((MissingGreedyIU) rootProblem);
 					VerificationDiagnostic.DependencyLink dependencyChain = getDependencyChain(
 						missingGreedyIU.iu, links, dependencyChainsCache);
 
 					if(dependencyChain != null) {
 						VerificationDiagnostic.identifyDependencyChain(dependencyChain, resource, "\n", MESSAGE_INDENT);
 						StringBuilder messageBuilder = getRootProblemMessage(rootProblem);
 
 						messageBuilder.append('\n');
 						InstallableUnitUtils.appendIdentifier(messageBuilder, missingGreedyIU.iu);
 						messageBuilder.append(" is required by:");
 
 						VerificationDiagnostic.DependencyLink parent = dependencyChain.getParent();
 						if(parent != null)
 							messageBuilder.append(parent.getIdentifier());
 
 						String message = messageBuilder.toString();
 						LogUtils.error(message);
 						add(new Status(IStatus.ERROR, plannerStatus.getPlugin(), message));
 
 						org.eclipse.emf.common.util.URI modelElementURI = dependencyChain.getModelElementURI();
 
 						if(modelElementURI != null)
 							verificationDiagnostics.add(new VerificationDiagnostic(
 								rootProblem.toString(), dependencyChain.getModelElementURI().deresolve(
 									resource.getURI())));
 					}
 				}
 			}
 		}
 
 		/**
 		 * Build the dependency chain from the specified IU up to an UI which has the model element URI information attached. Use a cache to store the
 		 * built chains so that they can be reused in case other chain(s) need to be built which contain any of the cached chains as a prefix.
 		 * 
 		 * @param iu
 		 *            the IU for which to build the dependency chain
 		 * @param links
 		 *            a map of dependency chain links
 		 * @param dependencyChainsCache
 		 *            a cache of the dependency chains
 		 * @return the dependency chain from the specified IU up to an UI which has the model element URI information attached, or <code>null</code>
 		 *         if no such IU was found
 		 */
 		protected VerificationDiagnostic.DependencyLink getDependencyChain(IInstallableUnit iu,
 				HashMap<IInstallableUnit, HashSet<IRequirement>> links,
 				HashMap<IInstallableUnit, VerificationDiagnostic.DependencyLink> dependencyChainsCache) {
 			if(dependencyChainsCache.containsKey(iu))
 				return dependencyChainsCache.get(iu); // may return null in case of a dependency loop
 
 			String elementURI = iu.getProperty(VerificationDiagnostic.PROP_AGGREGATOR_MODEL_ELEMENT_URI);
 			VerificationDiagnostic.DependencyLink lastLink;
 
 			GET_DEPENDENCY_CHAIN: {
 				// if the IU has the model element URI information attached then it is the head of the dependency chain and it means that we are done
 				if(elementURI != null) {
 					lastLink = new VerificationDiagnostic.DependencyLink(iu, null);
 					break GET_DEPENDENCY_CHAIN;
 				}
 
 				// we need to put a null value to the cache to prevent (otherwise) possible infinite loop
 				dependencyChainsCache.put(iu, null);
 
 				// walk the dependency chain up and in an attempt to build a dependency chain from the given IU to an IU with the model element
 				// URI information attached
 				for(Entry<IInstallableUnit, HashSet<IRequirement>> link : links.entrySet()) {
 					for(IRequirement requirement : link.getValue()) {
 						if(requirement.isMatch(iu)) {
 							lastLink = getDependencyChain(link.getKey(), links, dependencyChainsCache);
 							if(lastLink != null) {
 								lastLink = new VerificationDiagnostic.DependencyLink(iu, lastLink);
 								break GET_DEPENDENCY_CHAIN;
 							}
 						}
 					}
 				}
 
 				return null;
 			}
 
 			dependencyChainsCache.put(iu, lastLink);
 
 			return lastLink;
 		}
 
 		public PlannerStatus getPlannerStatus() {
 			return plannerStatus;
 		}
 
 		public List<VerificationDiagnostic> getVerificationDiagnostics() {
 			return verificationDiagnostics;
 		}
 	}
 
 	private static IInstallableUnit[] getRootIUs(IMetadataRepository site, String iuName, Version version,
 			IProgressMonitor monitor) throws CoreException {
 		IQuery<IInstallableUnit> query = QueryUtil.createIUQuery(iuName, version);
 		IQueryResult<IInstallableUnit> roots = site.query(query, monitor);
 
 		if(roots.isEmpty())
 			throw ExceptionUtils.fromMessage("IU %s not found", iuName); //$NON-NLS-1$
 
 		return roots.toArray(IInstallableUnit.class);
 	}
 
 	private ValidationSet validationSet;
 
 	public ValidationSetVerifier(Builder builder, ValidationSet validationSet) {
 		super(builder);
 		this.validationSet = validationSet;
 	}
 
 	private boolean addLeafmostContributions(Set<Explanation> explanations, Map<String, Contribution> contributions,
 			IRequirement prq) {
 		boolean contribsFound = false;
 		for(Explanation explanation : explanations) {
 			if(explanation instanceof Singleton) {
 				if(contribsFound)
 					// All explicit contributions for Singletons are added at
 					// top level. We just want to find out if this Singleton
 					// is the leaf problem here, not add anything
 					continue;
 
 				for(IInstallableUnit iu : ((Singleton) explanation).ius) {
 					if(prq.isMatch(iu)) {
 						// A singleton is always a leaf problem. Add
 						// contributions if we can find any
 						if(!findContributions(iu.getId()).isEmpty()) {
 							contribsFound = true;
 							break;
 						}
 					}
 				}
 				continue;
 			}
 
 			IInstallableUnit iu;
 			IRequirement crq;
 			if(explanation instanceof HardRequirement) {
 				HardRequirement hrq = (HardRequirement) explanation;
 				iu = hrq.iu;
 				crq = hrq.req;
 			}
 			else if(explanation instanceof MissingIU) {
 				MissingIU miu = (MissingIU) explanation;
 				iu = miu.iu;
 				crq = miu.req;
 			}
 			else
 				continue;
 
 			if(prq.isMatch(iu)) {
 				// This IU would have fulfilled the failing request but it
 				// has apparent problems of its own.
 				if(addLeafmostContributions(explanations, contributions, crq)) {
 					contribsFound = true;
 					continue;
 				}
 
 				for(Contribution contrib : findContributions(iu, crq)) {
 					contributions.put(contrib.getLabel(), contrib);
 					contribsFound = true;
 				}
 				continue;
 			}
 		}
 		return contribsFound;
 	}
 
 	private ProvisioningContext createContext(URI site) {
 		List<MetadataRepositoryReference> validationRepos = validationSet.getAllValidationRepositories();
 		int top = validationRepos.size();
 		List<URI> sites = new ArrayList<URI>(top + 1);
 		sites.add(site);
 
 		URI[] repoLocations = new URI[top + 1];
 		for(int idx = 0; idx < top; ++idx) {
 			MetadataRepositoryReference mdRef = validationRepos.get(idx);
 			if(mdRef.isEnabled())
 				sites.add(URI.create(mdRef.getResolvedLocation()));
 		}
 		repoLocations = sites.toArray(new URI[sites.size()]);
 		ProvisioningContext context = new ProvisioningContext(getBuilder().getProvisioningAgent());
 		context.setMetadataRepositories(repoLocations);
 		context.setArtifactRepositories(repoLocations);
 		return context;
 	}
 
 	private List<Contribution> findContributions(IInstallableUnit iu, IRequirement rq) {
 		List<Contribution> contribs = Collections.emptyList();
 		if(!(rq instanceof IRequiredCapability))
 			return contribs;
 
 		IRequiredCapability cap = (IRequiredCapability) rq;
 		if(Builder.NAMESPACE_OSGI_BUNDLE.equals(cap.getNamespace()) ||
 				IInstallableUnit.NAMESPACE_IU_ID.equals(cap.getNamespace()))
 			contribs = findContributions(cap.getName());
 
 		if(contribs.isEmpty())
 			// Not found, try the owner of the requirement
 			contribs = findContributions(iu.getId());
 		return contribs;
 	}
 
 	private List<Contribution> findContributions(String componentId) {
 		List<Contribution> result = null;
 		for(Contribution contrib : validationSet.getAllContributions())
 			for(MappedRepository repository : contrib.getRepositories(true))
 				for(MappedUnit mu : repository.getUnits(true))
 					if(componentId.equals(mu.getName())) {
 						if(result == null)
 							result = new ArrayList<Contribution>();
 						result.add(contrib);
 					}
 		return result == null
 				? Collections.<Contribution> emptyList()
 				: result;
 	}
 
 	private Set<IInstallableUnit> getUnpatchedTransitiveScope(IQueryable<IInstallableUnit> collectedStuff,
 			IInstallableUnitPatch patch, IProfile profile, IPlanner planner, URI repoLocation, SubMonitor monitor)
 			throws CoreException {
 		monitor.beginTask(null, 10);
 		IQuery<IInstallableUnit> query = SpecialQueries.createPatchApplicabilityQuery(patch);
 		IQueryResult<IInstallableUnit> result = collectedStuff.query(query, monitor.newChild(1));
 
 		IInstallableUnit[] rootArr = result.toArray(IInstallableUnit.class);
 		// Add as root IU's to a request
 		ProfileChangeRequest request = new ProfileChangeRequest(profile);
 		for(IInstallableUnit rootIU : rootArr)
 			request.setInstallableUnitProfileProperty(rootIU, IProfile.PROP_PROFILE_ROOT_IU, Boolean.TRUE.toString());
 		request.addInstallableUnits(rootArr);
 
 		ProvisioningContext context = new ProvisioningContext(getBuilder().getProvisioningAgent());
 		context.setMetadataRepositories(new URI[] { repoLocation });
 		// we don't pass the main monitor since we expect a possible failure which is silently ignored
 		// to avoid this, we use a null monitor and when the plan is ready, we add the full amount of ticks
 		// to the main monitor
 		ProvisioningPlan plan = (ProvisioningPlan) planner.getProvisioningPlan(
 			request, context, new NullProgressMonitor());
 		monitor.worked(8);
 		IStatus status = plan.getStatus();
 		if(status.isOK()) {
 			HashSet<IInstallableUnit> units = new HashSet<IInstallableUnit>();
 			units.add(patch);
 			Operand[] ops = plan.getOperands();
 			for(Operand op : ops) {
 				if(!(op instanceof InstallableUnitOperand))
 					continue;
 
 				InstallableUnitOperand iuOp = (InstallableUnitOperand) op;
 				IInstallableUnit iu = iuOp.second();
 				if(iu != null)
 					units.add(iu);
 			}
 			return units;
 		}
 		return Collections.emptySet();
 	}
 
 	IInstallableUnit resolvePartialIU(IInstallableUnit iu, SubMonitor subMon) throws CoreException {
 		IArtifactRepositoryManager arMgr = getBuilder().getArManager();
 		String info = "Converting partial IU for " + iu.getId() + "...";
 		subMon.beginTask(info, IProgressMonitor.UNKNOWN);
 		LogUtils.debug(info);
 
 		try {
 			// Scan all mapped repositories for this IU
 			//
 			IInstallableUnit miu = null;
 			MetadataRepository mdr = null;
 			contribs: for(Contribution contrib : validationSet.getAllContributions())
 
 				for(MappedRepository repo : contrib.getRepositories(true)) {
 					MetadataRepository candidate = repo.getMetadataRepository();
 					for(IInstallableUnit candidateIU : candidate.getInstallableUnits())
 						if(iu.getId().equals(candidateIU.getId()) && iu.getVersion().equals(candidateIU.getVersion())) {
 							mdr = candidate;
 							miu = candidateIU;
 							break contribs;
 						}
 				}
 
 			if(mdr == null)
 				throw ExceptionUtils.fromMessage(
 					"Unable to locate mapped repository for IU %s/%s", iu.getId(), iu.getVersion());
 
 			IArtifactRepository sourceAr = arMgr.loadRepository(mdr.getLocation(), subMon.newChild(10));
 			File tempRepositoryFolder = getBuilder().getTempRepositoryFolder();
 			tempRepositoryFolder.mkdirs();
 
 			URI tempRepositoryURI = Builder.createURI(tempRepositoryFolder);
 			IFileArtifactRepository tempAr;
 			try {
 				tempAr = (IFileArtifactRepository) arMgr.loadRepository(tempRepositoryURI, subMon.newChild(1));
 			}
 			catch(ProvisionException e) {
 				tempAr = (IFileArtifactRepository) arMgr.createRepository(tempRepositoryURI, "temporary artifacts"
 						+ " artifacts", Builder.SIMPLE_ARTIFACTS_TYPE, Collections.<String, String> emptyMap()); //$NON-NLS-1$
 			}
 
 			Collection<IArtifactKey> artifacts = miu.getArtifacts();
 			IArtifactKey key = artifacts.iterator().next();
 			ArrayList<String> errors = new ArrayList<String>();
 			MirrorGenerator.mirror(
 				artifacts, null, sourceAr, tempAr, getBuilder().getTransport(), PackedStrategy.UNPACK_AS_SIBLING,
 				errors, subMon.newChild(1));
 			int numErrors = errors.size();
 			if(numErrors > 0) {
 				IStatus[] children = new IStatus[numErrors];
 				for(int idx = 0; idx < numErrors; ++idx)
 					children[idx] = new Status(IStatus.ERROR, Engine.PLUGIN_ID, errors.get(idx));
 				MultiStatus status = new MultiStatus(
 					Engine.PLUGIN_ID, IStatus.ERROR, children, "Unable to mirror", null);
 				throw new CoreException(status);
 			}
 
 			File bundleFile = tempAr.getArtifactFile(key);
 			if(bundleFile == null)
 				throw ExceptionUtils.fromMessage(
 					"Unable to resolve partial IU. Artifact file for %s could not be found", key);
 
 			IInstallableUnit preparedIU = PublisherUtil.createBundleIU(key, bundleFile);
 			if(preparedIU == null) {
 				LogUtils.warning(
 					"Unable to resolve partial IU. Artifact file for %s did not contain a bundle manifest", key);
 				return iu;
 			}
 			IInstallableUnit newIU = P2Bridge.importToModel(preparedIU, iu);
 
 			List<IInstallableUnit> allIUs = mdr.getInstallableUnits();
 			allIUs.remove(miu);
 			allIUs.add(newIU);
 			return newIU;
 		}
 		catch(CoreException e) {
 			for(Contribution contrib : findContributions(iu.getId()))
 				getBuilder().sendEmail(contrib, Collections.singletonList(e.getMessage()));
 			throw e;
 		}
 	}
 
 	@Override
 	public void run(IProgressMonitor monitor) throws CoreException {
 		String taskLabel = Builder.getValidationSetLabel(validationSet);
 
 		Builder builder = getBuilder();
 		Aggregation aggregation = builder.getAggregation();
 		List<Configuration> configs = aggregation.getConfigurations();
 		int configCount = configs.size();
 		SubMonitor subMon = SubMonitor.convert(monitor, configCount * 100);
 
 		LogUtils.info("Starting planner verification for validationSet: " + taskLabel); //$NON-NLS-1$
 		long start = TimeUtils.getNow();
 
 		String profilePrefix = Builder.PROFILE_ID + '_';
 		((AggregationImpl) aggregation).clearStatus();
 
 		final Set<IInstallableUnit> unitsToAggregate = builder.getUnitsToAggregate(validationSet);
 		IProfileRegistry profileRegistry = P2Utils.getProfileRegistry(builder.getProvisioningAgent());
 		IPlanner planner = P2Utils.getPlanner(builder.getProvisioningAgent());
 		IMetadataRepositoryManager mdrMgr = builder.getMdrManager();
 		try {
 			URI repoLocation = builder.getSourceCompositeURI(validationSet);
 			Set<IInstallableUnit> validationOnlyIUs = null;
 			for(MetadataRepositoryReference validationRepo : validationSet.getAllValidationRepositories()) {
 				if(validationOnlyIUs == null)
 					validationOnlyIUs = new HashSet<IInstallableUnit>();
 				validationOnlyIUs.addAll(validationRepo.getMetadataRepository().getInstallableUnits());
 			}
 			if(validationOnlyIUs == null)
 				validationOnlyIUs = Collections.emptySet();
 
 			IMetadataRepository sourceRepo = mdrMgr.loadRepository(repoLocation, subMon.newChild(1));
 			for(Configuration config : configs) {
 				if(!config.isEnabled())
 					continue;
 
 				String configName = config.getName();
 				String info = format("Verifying config %s...", configName); //$NON-NLS-1$
 				LogUtils.info(info);
 				subMon.setTaskName(info);
 
 				Map<String, String> props = new HashMap<String, String>();
 				// TODO Where is FLAVOR gone?
 				//props.put(IProfile.PROP_FLAVOR, "tooling"); //$NON-NLS-1$
 				props.put(IProfile.PROP_ENVIRONMENTS, config.getOSGiEnvironmentString());
 				props.put(IProfile.PROP_INSTALL_FEATURES, "true");
 
 				IProfile profile = null;
 				String profileId = profilePrefix + configName;
 
 				profile = profileRegistry.getProfile(profileId);
 				if(profile == null)
 					profile = profileRegistry.addProfile(profileId, props);
 
 				IInstallableUnit[] rootArr = getRootIUs(
 					sourceRepo, builder.getVerificationIUName(validationSet), Builder.ALL_CONTRIBUTED_CONTENT_VERSION,
 					subMon.newChild(9));
 
 				// Add as root IU's to a request
 				ProfileChangeRequest request = new ProfileChangeRequest(profile);
 				for(IInstallableUnit rootIU : rootArr)
 					request.setInstallableUnitProfileProperty(
 						rootIU, IProfile.PROP_PROFILE_ROOT_IU, Boolean.TRUE.toString());
 				request.addInstallableUnits(rootArr);
 
 				while(true) {
 					ProvisioningContext context = createContext(repoLocation);
 					ProvisioningPlan plan = (ProvisioningPlan) planner.getProvisioningPlan(
 						request, context,
 						subMon.newChild(80, SubMonitor.SUPPRESS_BEGINTASK | SubMonitor.SUPPRESS_SETTASKNAME));
 
 					IStatus status = plan.getStatus();
 					if(status.getSeverity() == IStatus.ERROR) {
 						sendEmails((PlannerStatus) status);
 						LogUtils.info("Done. Took %s", TimeUtils.getFormattedDuration(start)); //$NON-NLS-1$
 						throw new CoreException(new AnalyzedPlannerStatus(
 							((EObject) aggregation).eResource(), config, (PlannerStatus) status));
 					}
 
 					boolean hadPartials = false;
 
 					Set<IInstallableUnit> suspectedValidationOnlyIUs = null;
 					Operand[] ops = plan.getOperands();
 					for(Operand op : ops) {
 						if(!(op instanceof InstallableUnitOperand))
 							continue;
 
 						InstallableUnitOperand iuOp = (InstallableUnitOperand) op;
 						IInstallableUnit iu = iuOp.second();
 						if(iu == null)
 							continue;
 
 						// skip all IUs generated for verification purposes
 						if(Boolean.parseBoolean(iu.getProperty(Builder.PROP_AGGREGATOR_GENERATED_IU)))
 							continue;
 
 						if(validationOnlyIUs.contains(iu)) {
 							// This IU should not be included unless it is also included in one of
 							// the contributed repositories
 							if(suspectedValidationOnlyIUs == null)
 								suspectedValidationOnlyIUs = new HashSet<IInstallableUnit>();
 							suspectedValidationOnlyIUs.add(iu);
 						}
 						else {
 							if(!unitsToAggregate.contains(iu)) {
 								if(Boolean.valueOf(iu.getProperty(IInstallableUnit.PROP_PARTIAL_IU)).booleanValue()) {
 									iu = resolvePartialIU(iu, subMon.newChild(1));
 									hadPartials = true;
 								}
 								unitsToAggregate.add(iu);
 							}
 						}
 					}
 
 					Iterator<IInstallableUnit> itor = sourceRepo.query(
 						QueryUtil.createIUPatchQuery(), subMon.newChild(1)).iterator();
 
 					IQueryable<IInstallableUnit> collectedStuff = null;
 					while(itor.hasNext()) {
 						IInstallableUnitPatch patch = (IInstallableUnitPatch) itor.next();
 						if(!unitsToAggregate.contains(patch))
 							continue;
 
 						if(collectedStuff == null) {
 							collectedStuff = new QueryableArray(
 								unitsToAggregate.toArray(new IInstallableUnit[unitsToAggregate.size()]));
 						}
 
 						Set<IInstallableUnit> units = getUnpatchedTransitiveScope(
 							collectedStuff, patch, profile, planner, repoLocation, subMon.newChild(1));
 						for(IInstallableUnit iu : units) {
 							if(validationOnlyIUs.contains(iu)) {
 								// This IU should not be included unless it is also included in one of
 								// the contributed repositories
 								if(suspectedValidationOnlyIUs == null)
 									suspectedValidationOnlyIUs = new HashSet<IInstallableUnit>();
 								suspectedValidationOnlyIUs.add(iu);
 							}
 							else {
 								if(!unitsToAggregate.contains(iu)) {
 									if(Boolean.valueOf(iu.getProperty(IInstallableUnit.PROP_PARTIAL_IU)).booleanValue()) {
 										iu = resolvePartialIU(iu, subMon.newChild(1));
 										hadPartials = true;
 									}
 									unitsToAggregate.add(iu);
 								}
 							}
 						}
 					}
 
 					if(suspectedValidationOnlyIUs != null) {
 						// Prune the set of IU's that we suspect are there for validation
 						// purposes only using the source repository
 						//
 						final Set<IInstallableUnit> candidates = suspectedValidationOnlyIUs;
 						final boolean hadPartialsHolder[] = new boolean[] { false };
 
 						Iterator<IInstallableUnit> allIUs = sourceRepo.query(
 							QueryUtil.createIUAnyQuery(), subMon.newChild(1)).iterator();
 
 						while(allIUs.hasNext()) {
 							IInstallableUnit iu = allIUs.next();
 							if(candidates.contains(iu) && !unitsToAggregate.contains(iu)) {
 								try {
 									if(Boolean.valueOf(iu.getProperty(IInstallableUnit.PROP_PARTIAL_IU)).booleanValue()) {
 										iu = resolvePartialIU(iu, SubMonitor.convert(new NullProgressMonitor()));
 										hadPartialsHolder[0] = true;
 									}
 								}
 								catch(CoreException e) {
 									throw new RuntimeException(e);
 								}
 								unitsToAggregate.add(iu);
 							}
 						}
 					}
 
 					// exit the loop if there are no more partial IUs
 					if(!hadPartials)
 						break;
 
 					LogUtils.info("Partial IU's encountered. Verifying %s again...", configName); //$NON-NLS-1$
 				}
 			}
 			LogUtils.info("Verification successful"); //$NON-NLS-1$
 		}
 		catch(RuntimeException e) {
 			throw ExceptionUtils.wrap(e);
 		}
 		finally {
 			MonitorUtils.done(subMon);
 			P2Utils.ungetProfileRegistry(builder.getProvisioningAgent(), profileRegistry);
 			P2Utils.ungetPlanner(builder.getProvisioningAgent(), planner);
 			LogUtils.info("Done. Took %s", TimeUtils.getFormattedDuration(start)); //$NON-NLS-1$
 		}
 	}
 
 	private void sendEmails(PlannerStatus plannerStatus) {
 		Builder builder = getBuilder();
 		if(!builder.getAggregation().isSendmail())
 			return;
 
 		ArrayList<String> errors = new ArrayList<String>();
 		RequestStatus requestStatus = plannerStatus.getRequestStatus();
 		if(requestStatus == null)
 			return;
 
 		Set<Explanation> explanations = requestStatus.getExplanations();
 		Map<String, Contribution> contribs = new HashMap<String, Contribution>();
 		for(Explanation explanation : explanations) {
 			String msg = explanation.toString();
 			LogUtils.error(msg);
 			errors.add(msg);
 			if(explanation instanceof Singleton) {
 				// A singleton is always a leaf problem. Add contributions
 				// if we can find any. They are all culprits
 				for(IInstallableUnit iu : ((Singleton) explanation).ius) {
 					for(Contribution contrib : findContributions(iu.getId()))
 						contribs.put(contrib.getLabel(), contrib);
 				}
 				continue;
 			}
 
 			IInstallableUnit iu;
 			IRequirement crq;
 			if(explanation instanceof HardRequirement) {
 				HardRequirement hrq = (HardRequirement) explanation;
 				iu = hrq.iu;
 				crq = hrq.req;
 			}
 			else if(explanation instanceof MissingIU) {
 				MissingIU miu = (MissingIU) explanation;
 				iu = miu.iu;
 				crq = miu.req;
 			}
 			else
 				continue;
 
 			// Find the leafmost contributions for the problem. We don't want to
 			// blame consuming contributors
 			if(!addLeafmostContributions(explanations, contribs, crq)) {
 				for(Contribution contrib : findContributions(iu, crq))
 					contribs.put(contrib.getLabel(), contrib);
 			}
 		}
 		if(contribs.isEmpty())
 			builder.sendEmail(null, errors);
 		else {
 			for(Contribution contrib : contribs.values())
 				builder.sendEmail(contrib, errors);
 		}
 	}
 }
