 /*******************************************************************************
  * Copyright (c) 2006-2007, Cloudsmith Inc.
  * The code, documentation and other materials contained herein have been
  * licensed under the Eclipse Public License - v 1.0 by the copyright holder
  * listed above, as the Initial Contributor under such license. The text of
  * such license is available at www.eclipse.org.
  ******************************************************************************/
 
 package org.eclipse.b3.aggregator.engine.maven;
 
 import java.io.IOException;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import org.eclipse.b3.aggregator.engine.maven.pom.DocumentRoot;
 import org.eclipse.b3.aggregator.engine.maven.pom.Model;
 import org.eclipse.b3.aggregator.engine.maven.pom.Parent;
 import org.eclipse.b3.aggregator.engine.maven.pom.PomFactory;
 import org.eclipse.b3.aggregator.engine.maven.pom.PomPackage;
 import org.eclipse.b3.aggregator.engine.maven.pom.impl.ModelImpl;
 import org.eclipse.b3.aggregator.engine.maven.pom.util.PomResourceFactoryImpl;
 import org.eclipse.b3.aggregator.engine.maven.pom.util.PomResourceImpl;
 import org.eclipse.b3.aggregator.util.LogUtils;
 import org.eclipse.b3.util.ExceptionUtils;
 import org.eclipse.b3.util.StringUtils;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceFactoryRegistryImpl;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.ecore.util.Diagnostician;
 
 /**
  * @author Filip Hrbek (filip.hrbek@cloudsmith.com)
  * 
  */
 public class POM {
 	private static final int MAX_CACHE_SIZE = 10;
 
 	private static final HashMap<URI, POM> pomCacheLRU = new LinkedHashMap<URI, POM>(MAX_CACHE_SIZE, 0.75f, true) {
 		private static final long serialVersionUID = 1L;
 
 		protected boolean removeEldestEntry(Map.Entry<URI, POM> entry) {
 			return size() >= MAX_CACHE_SIZE;
 		}
 	};
 
 	public static final String XML_SCHEMA_LOCATION = "http://maven.apache.org/maven-v4_0_0.xsd";
 
 	public static String expandProperties(String str, Map<String, String> propertyMap) {
 		if(str == null)
 			return "";
 
 		int pos;
 		int from = 0;
 		while((pos = str.indexOf('$', from)) != -1) {
 			int len = str.length();
 			if(len > pos && str.charAt(pos + 1) == '{') {
 				int end = str.indexOf('}', pos + 2);
 				if(end != -1) {
 					String varValue = propertyMap.get(str.substring(pos + 2, end));
 					int varLength = 0;
 					if(varValue != null) {
 						StringBuilder newStr = new StringBuilder(str.substring(0, pos));
 						newStr.append(varValue);
 						newStr.append(str.substring(end + 1));
 						str = newStr.toString();
 						varLength = varValue.length();
 					}
 					from = pos + varLength;
 				}
 				else
 					break;
 			}
 			else
 				from = pos + 1;
 		}
 
 		return str;
 	}
 
 	synchronized public static POM getPOM(String repoLocation, String groupId, String artifactId, String version)
 			throws CoreException {
 		URI uri = URI.createURI(repoLocation + "/" + createRelativePath(groupId, artifactId, version));
 		POM pom = pomCacheLRU.get(uri);
 		if(pom != null)
 			return pom;
 
 		pomCacheLRU.put(uri, pom = new POM(uri, repoLocation));
 
 		return pom;
 	}
 
 	private static String createRelativePath(String groupId, String artifactId, String version) {
 		groupId = StringUtils.trimmedOrNull(groupId);
 		artifactId = StringUtils.trimmedOrNull(artifactId);
 		version = StringUtils.trimmedOrNull(version);
 		return (groupId != null
 				? groupId.replace('.', '/')
 				: groupId) + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version + ".pom";
 	}
 
 	private String repoRoot;
 
 	private ResolvedModel resolvedModel;
 
 	private DocumentRoot documentRoot;
 
 	private POM parentPOM;
 
 	private String md5;
 
 	private String sha1;
 
 	private Long timestamp;
 
 	public POM() {
 		documentRoot = PomFactory.eINSTANCE.createDocumentRoot();
 		documentRoot.getXSISchemaLocation().put(PomPackage.eNS_URI, XML_SCHEMA_LOCATION);
 		documentRoot.setProject(PomFactory.eINSTANCE.createModel());
 	}
 
 	private POM(URI uri, String repoRoot) throws CoreException {
 		Resource resource = getResourceSet().getResource(uri, true);
 		EList<EObject> content = resource.getContents();
 		if(content.size() != 1)
 			throw ExceptionUtils.fromMessage("ECore Resource did not contain one resource. It had %d",
 					Integer.valueOf(content.size()));
 
 		if(resource instanceof PomResourceImpl) {
 			PomResourceImpl pomResource = (PomResourceImpl) resource;
 			md5 = pomResource.getMd5();
 			sha1 = pomResource.getSha1();
 			timestamp = pomResource.getTimestamp();
 		}
 
 		documentRoot = (DocumentRoot) content.get(0);
 		Diagnostic diag = Diagnostician.INSTANCE.validate(documentRoot);
 
 		int modifiedSeverity = Diagnostic.OK;
 		if(diag.getSeverity() >= Diagnostic.ERROR) {
 			for(Diagnostic childDiag : diag.getChildren()) {
 				String childMessage = childDiag.getMessage();
 				if(!childMessage.matches("The feature 'mixed' of 'org.eclipse.b3.aggregator.engine.maven.pom.impl.DocumentRootImpl@"
 						+ "[0-9a-f]+\\{[^\\}]+.pom#/\\}' with 2 element values must have exactly 1 element value")) {
 					LogUtils.error(childDiag.getMessage());
 					if(modifiedSeverity < childDiag.getSeverity())
 						modifiedSeverity = childDiag.getSeverity();
 				}
 			}
 		}
 
 		if(modifiedSeverity >= Diagnostic.ERROR)
 			throw ExceptionUtils.fromMessage("Maven POM model validation failed: %s", diag.getMessage());
 
 		this.repoRoot = repoRoot;
 		Model resolvedModel = getResolvedProject();
 		String relativePath = "/"
 				+ createRelativePath(resolvedModel.getGroupId(), resolvedModel.getArtifactId(),
 						resolvedModel.getVersion());
 		String uriStr = uri.toString();
 		if(!uriStr.endsWith(relativePath))
 			throw ExceptionUtils.fromMessage("Invalid path, %s should end with %s", uriStr, relativePath);
 	}
 
 	public String getArtifactId() throws CoreException {
 		return getResolvedProject().getArtifactId();
 	}
 
 	public String getGroupId() throws CoreException {
 		return getResolvedProject().getGroupId();
 	}
 
 	public String getMd5() {
 		return md5;
 	}
 
 	public POM getParentPOM() throws CoreException {
 		if(parentPOM != null)
 			return parentPOM;
 
 		Model model = getProject();
 		Parent parent = model.getParent();
 		if(parent != null)
 			return(parentPOM = getPOM(repoRoot, parent.getGroupId(), parent.getArtifactId(), parent.getVersion()));
 
 		return null;
 	}
 
 	public Model getProject() throws CoreException {
 		if(documentRoot == null || documentRoot.getProject() == null)
 			throw ExceptionUtils.fromMessage("No project available");
 
 		return documentRoot.getProject();
 	}
 
 	public Map<String, String> getProperties() throws CoreException {
 		return getResolvedProject().getPropertyMap();
 	}
 
 	public ResolvedModel getResolvedProject() throws CoreException {
 		if(resolvedModel == null)
 			resolvedModel = new ResolvedModel(repoRoot, (ModelImpl) getProject());
 
 		return resolvedModel;
 	}
 
 	public String getSha1() {
 		return sha1;
 	}
 
 	public Long getTimestamp() {
 		return timestamp;
 	}
 
 	public String getVersion() throws CoreException {
 		return getResolvedProject().getVersion();
 	}
 
 	public void save() throws CoreException {
 		save((Resource) null);
 	}
 
 	public void save(Resource resource) throws CoreException {
 		Resource targetResource = resource;
 
 		if(targetResource == null)
 			targetResource = documentRoot.eResource();
 
 		if(targetResource == null)
 			throw ExceptionUtils.fromMessage("No resource to store Maven POM");
 
 		if(resource != null && !targetResource.equals(documentRoot.eResource()))
 			targetResource.getContents().add(documentRoot);
 
 		try {
 			targetResource.save(Collections.emptyMap());
 		}
 		catch(IOException e) {
 			throw ExceptionUtils.fromMessage(e, "Unable to save Maven POM");
 		}
 	}
 
 	public void save(URI uri) throws CoreException {
 		save(getResourceSet().createResource(uri));
 	}
 
 	private ResourceSet getResourceSet() {
 		ResourceSet resourceSet = new ResourceSetImpl();
 		resourceSet.setResourceFactoryRegistry(new ResourceFactoryRegistryImpl() {
 			@Override
 			public Resource.Factory getFactory(URI uri) {
 				return new PomResourceFactoryImpl();
 			}
 
 			@Override
 			public Resource.Factory getFactory(URI uri, String contentType) {
 				return getFactory(uri);
 			}
 		});
 
 		return resourceSet;
 	}
 }
