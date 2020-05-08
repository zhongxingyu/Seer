 package example.deploy.text;
 
 import static com.polopoly.cm.VersionedContentId.LATEST_VERSION;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.polopoly.cm.ContentId;
 import com.polopoly.cm.ContentReference;
 import com.polopoly.cm.ExternalContentId;
 import com.polopoly.cm.LockInfo;
 import com.polopoly.cm.VersionedContentId;
 import com.polopoly.cm.client.CMException;
 import com.polopoly.cm.client.Content;
 import com.polopoly.cm.client.ContentRead;
 import com.polopoly.cm.client.InputTemplate;
 import com.polopoly.cm.client.impl.exceptions.EJBFinderException;
 import com.polopoly.cm.collections.ContentList;
 import com.polopoly.cm.policy.Policy;
 import com.polopoly.cm.policy.PolicyCMServer;
 
 import example.deploy.hotdeploy.client.Major;
 import example.deploy.hotdeploy.util.CheckedCast;
 import example.deploy.hotdeploy.util.CheckedClassCastException;
 
 public class TextContentDeployer {
 	private static final Logger logger = Logger
 			.getLogger(TextContentDeployer.class.getName());
 
 	private TextContentSet contentSet;
 
 	private PolicyCMServer server;
 
 	private boolean ignoreContentListAddFailures = false;
 
 	public TextContentDeployer(TextContentSet contentSet, PolicyCMServer server) {
 		this.contentSet = contentSet;
 		this.server = server;
 	}
 
 	public Collection<Policy> deploy() throws DeployException {
 		boolean success = false;
 		Map<String, Policy> newVersionById = new HashMap<String, Policy>();
 
 		try {
 			for (TextContent textContent : contentSet) {
 				try {
 					Policy newVersionPolicy = createNewVersion(textContent);
 
 					newVersionById.put(textContent.getId(), newVersionPolicy);
 				} catch (CMException e) {
 					throw new DeployException("While creating "
 							+ textContent.getId() + ": " + e, e);
 				}
 			}
 
 			for (TextContent textContent : contentSet) {
 				try {
 					// do this in a second step since one of the objects in the
 					// set might
 					// be the publish content.
 					createPublishInVersion(textContent, newVersionById);
 				} catch (CMException e) {
 					throw new DeployException(
 							"While creating object to publish "
 									+ textContent.getId() + " in: " + e, e);
 				}
 			}
 
 			for (TextContent textContent : contentSet) {
 				try {
 					Policy newVersion = newVersionById.get(textContent.getId());
 
 					deploy(textContent, newVersion);
 				} catch (CMException e) {
 					throw new DeployException("While importing "
 							+ textContent.getId() + ": " + e, e);
 				}
 			}
 
 			// do this in a separate step since the deploy step clears content
 			// lists and we might be deploying
 			// to another object in the same batch.
 			for (TextContent textContent : contentSet) {
 				try {
 					for (Publishing publishing : textContent.getPublishings()) {
 						String publishInExternalId = ((ExternalIdReference) publishing
 								.getPublishIn()).getExternalId();
 
 						Policy newVersion = newVersionById.get(textContent
 								.getId());
 						Policy publishInVersion = newVersionById
 								.get(publishInExternalId);
 
 						publish(newVersion, publishInVersion,
 								publishing.getPublishInGroup(), publishing
 										.getPublishIn()
 										.resolveReference(server)
 										.getReferenceMetaDataId());
 					}
 				} catch (CMException e) {
 					throw new DeployException("While publishing "
 							+ textContent.getId() + ": " + e, e);
 				}
 			}
 
 			Iterator<Policy> newVersionIterator = newVersionById.values()
 					.iterator();
 			Collection<Policy> result = new ArrayList<Policy>();
 
 			while (newVersionIterator.hasNext()) {
 				Policy newVersion = newVersionIterator.next();
 
 				try {
 					newVersion.getContent().commit();
 				} catch (CMException e) {
 					throw new DeployCommitException("While committing "
 							+ toString(newVersion) + ": " + e, e);
 				} catch (Exception e) {
 					throw new DeployCommitException("While committing "
 							+ toString(newVersion) + ": " + e, e);
 				}
 
 				newVersionIterator.remove();
 				result.add(newVersion);
 			}
 
 			success = true;
 
 			return result;
 		} finally {
 			if (!success) {
 				for (Entry<String, Policy> newVersionEntry : newVersionById
 						.entrySet()) {
 					Policy newVersion = newVersionEntry.getValue();
 					String id = newVersionEntry.getKey();
 
 					try {
 						server.abortContent(newVersion, true);
 					} catch (CMException e) {
 						logger.log(Level.WARNING, "While aborting " + id
 								+ " after failure: " + e.getMessage(), e);
 					}
 				}
 			}
 		}
 	}
 
 	private String toString(Policy policy) {
 		try {
 			return policy.getContent().getExternalId().getExternalId();
 		} catch (CMException externalIdException) {
 			return policy.getContentId().getContentId().getContentIdString();
 		}
 	}
 
 	private void publish(Policy publish, Policy publishIn,
 			String publishInGroup, ContentId metadata) throws CMException {
 		Content publishInContent = publishIn.getContent();
 		ContentList contentList;
 
 		if (publishInGroup != null) {
 			contentList = publishInContent.getContentList(publishInGroup);
 		} else {
 			contentList = publishInContent.getContentList();
 		}
 
 		if (!contains(contentList, publish.getContentId())) {
 			contentList.add(contentList.size(), new ContentReference(publish
 					.getContentId().getContentId(), metadata));
 		}
 	}
 
 	private boolean contains(ContentList contentList, ContentId contentId)
 			throws CMException {
 		for (int i = contentList.size() - 1; i >= 0; i--) {
 			ContentId referredContentId = contentList.getEntry(i)
 					.getReferredContentId();
 
 			if (referredContentId != null
 					&& referredContentId.equalsIgnoreVersion(contentId)) {
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	private void createPublishInVersion(TextContent textContent,
 			Map<String, Policy> newVersionById) throws CMException {
 		for (Publishing publishing : textContent.getPublishings()) {
 			Reference publishIn = publishing.getPublishIn();
 			String publishInExternalId = ((ExternalIdReference) publishIn)
 					.getExternalId();
 
 			if (!newVersionById.containsKey(publishInExternalId)) {
 				Policy newPublishInVersion = createNewVersionOfExistingContent(publishIn
 						.resolveId(server));
 
 				newVersionById.put(publishInExternalId, newPublishInVersion);
 			}
 		}
 	}
 
 	private void deploy(TextContent textContent, Policy policy)
 			throws DeployException, CMException {
 		Content content = policy.getContent();
 
 		String templateId = textContent.getTemplateId();
 
 		if (templateId != null) {
 			TextContent template = contentSet.get(templateId);
 
 			if (template == null) {
 				throw new DeployException("The object \"" + templateId
 						+ "\" specified as template of " + textContent
 						+ " must be defined in the same file.");
 			}
 
 			if (template.getTemplateId() != null) {
 				throw new DeployException("Cannot use the object \""
 						+ templateId + "\" as template of " + textContent
 						+ " since it in turn also has a template.");
 			}
 
 			deploy(template, policy);
 		}
 
 		setReferences(textContent, content);
 
 		setComponents(textContent, content);
 
 		setLists(textContent, content);
 
 		setSecurityParent(textContent, content);
 
 		setInputTemplate(textContent, content);
 
 		addFiles(textContent, content);
 	}
 
 	private void addFiles(TextContent textContent, Content content)
 			throws CMException, DeployException {
 		for (Entry<String, byte[]> fileEntry : textContent.getFiles()
 				.entrySet()) {
 			byte[] fileData = fileEntry.getValue();
 			String fileName = fileEntry.getKey();
 
 			ensureDirectoryExists(content, fileName);
 			ByteArrayInputStream inputStream = new ByteArrayInputStream(
 					fileData);
 			try {
 				content.importFile(fileName, inputStream);
 			} catch (IOException e) {
 				throw new DeployException(
 						"Could not read file while importing: "
 								+ e.getMessage(), e);
 			}
 		}
 
 	}
 
 	private void ensureDirectoryExists(Content content, String fileName)
 			throws CMException {
 		String path = getDirPath(fileName);
 		logger.log(Level.FINE, "Ensuring that content file directory " + path
 				+ " exists.");
 
 		try {
 			content.createDirectory(path, true);
 		} catch (IOException e1) {
 			logger.log(Level.FINE, "Did not create content file directory "
 					+ path + " because it already exists.");
 		}
 	}
 
 	private String getDirPath(String path) {
 		// Find last index of '/'
 		int dirIndex = path.lastIndexOf("/");
 		return (dirIndex > -1) ? path.substring(0, dirIndex) : "";
 	}
 
 	private void setInputTemplate(TextContent textContent, Content content)
 			throws CMException {
 		Reference inputTemplate = textContent.getInputTemplate();
 
 		if (inputTemplate != null) {
 			content.setInputTemplateId(inputTemplate.resolveId(server));
 		}
 	}
 
 	private void setSecurityParent(TextContent textContent, Content content)
 			throws CMException {
 		Reference securityParent = textContent.getSecurityParent();
 
 		if (securityParent != null) {
 			content.setSecurityParentId(securityParent.resolveId(server));
 		}
 	}
 
 	private void setLists(TextContent textContent, Content content)
 			throws CMException {
 		for (Entry<String, List<Reference>> groupEntry : textContent.getLists()
 				.entrySet()) {
 			String group = groupEntry.getKey();
 
 			ContentList contentList = content.getContentList(group);
 
 			for (int i = contentList.size() - 1; i >= 0; i--) {
 				contentList.remove(0);
 			}
 
 			int index = 0;
 
 			for (Reference reference : groupEntry.getValue()) {
 				ContentReference resolvedReference = reference
 						.resolveReference(server);
 
 				if (resolvedReference.getReferenceMetaDataId() != null) {
 					makeSureMetadataHasReference(
 							resolvedReference.getReferenceMetaDataId(),
 							resolvedReference.getReferredContentId());
 				}
 
 				try {
 					contentList.add(index++, resolvedReference);
 				} catch (CMException e) {
 					if (!ignoreContentListAddFailures) {
 						throw e;
 					} else {
 						logger.log(Level.WARNING, "Could not add " + reference
 								+ " to content list " + group + " in "
 								+ content.getContentId().getContentIdString()
 								+ ": " + e.getMessage());
 					}
 				}
 			}
 		}
 	}
 
 	private void makeSureMetadataHasReference(ContentId referenceMetaDataId,
 			ContentId referredContentId) {
 		ContentRead metadataContent;
 
 		try {
 			metadataContent = server.getContent(referenceMetaDataId);
		} catch (EJBFinderException e) {
			// this is likely to mean that we are just in the process of
			// creating the RMD too. otherwise, it would have caused an error
			// before.
			return;
 		} catch (CMException e) {
 			logger.log(Level.WARNING, "Could not read metadata content "
 					+ referenceMetaDataId.getContentIdString() + ".");
 
 			return;
 		}
 
 		ContentId currentReferredId;
 
 		try {
 			currentReferredId = metadataContent.getContentReference(
 					"polopoly.ReferenceMetaData", "referredId");
 		} catch (CMException e3) {
 			logger.log(Level.WARNING,
 					"Could not read reference in metadata content "
 							+ referenceMetaDataId.getContentIdString() + ".");
 
 			return;
 		}
 
 		if (currentReferredId == null) {
 			Policy newVersionPolicy;
 
 			try {
 				newVersionPolicy = server.createContentVersion(metadataContent
 						.getContentId());
 			} catch (CMException e2) {
 				logger.log(
 						Level.WARNING,
 						"While trying to fix referred Id in metadata "
 								+ referenceMetaDataId.getContentIdString()
 								+ " supposed to point to "
 								+ referredContentId.getContentIdString() + ".");
 
 				return;
 			}
 
 			Content newVersion = newVersionPolicy.getContent();
 
 			try {
 				newVersion.setContentReference("polopoly.ReferenceMetaData",
 						"referredId", referredContentId);
 
 				newVersion.commit();
 			} catch (CMException e) {
 				logger.log(
 						Level.WARNING,
 						"While trying to fix referred Id in metadata "
 								+ referenceMetaDataId.getContentIdString()
 								+ " supposed to point to "
 								+ referredContentId.getContentIdString() + ".");
 
 				try {
 					server.abortContent(newVersionPolicy, true);
 				} catch (CMException e1) {
 					// swallow.
 				}
 			}
 		}
 	}
 
 	private void setComponents(TextContent textContent, Content content)
 			throws CMException {
 		for (Entry<String, Map<String, String>> groupEntry : textContent
 				.getComponents().entrySet()) {
 			String group = groupEntry.getKey();
 
 			for (Entry<String, String> componentEntry : groupEntry.getValue()
 					.entrySet()) {
 				String component = componentEntry.getKey();
 				String value = componentEntry.getValue();
 
 				content.setComponent(group, component, value);
 			}
 		}
 	}
 
 	private void setReferences(TextContent textContent, Content content)
 			throws CMException {
 		for (Entry<String, Map<String, Reference>> groupEntry : textContent
 				.getReferences().entrySet()) {
 			String group = groupEntry.getKey();
 
 			for (Entry<String, Reference> componentEntry : groupEntry
 					.getValue().entrySet()) {
 				String component = componentEntry.getKey();
 				Reference reference = componentEntry.getValue();
 
 				content.setContentReference(group, component,
 						reference.resolveId(server));
 			}
 		}
 	}
 
 	private Policy createNewVersion(TextContent textContent)
 			throws CMException, DeployException {
 		VersionedContentId contentId = null;
 
 		try {
 			contentId = server.findContentIdByExternalId(new ExternalContentId(
 					textContent.getId()));
 		} catch (EJBFinderException e) {
 		}
 
 		Policy newVersionPolicy;
 
 		if (contentId == null) {
 			InputTemplate inputTemplate = getInputTemplate(textContent);
 
 			Major major = textContent.getMajor();
 
 			TextContent atTemplate = textContent;
 
 			while (major == Major.UNKNOWN && atTemplate.getTemplateId() != null) {
 				atTemplate = contentSet.get(atTemplate.getTemplateId());
 
 				if (atTemplate != null) {
 					major = atTemplate.getMajor();
 				}
 			}
 
 			if (textContent.getMajor() == Major.UNKNOWN) {
 				major = getMajor(server, inputTemplate);
 			} else {
 				major = textContent.getMajor();
 			}
 
 			newVersionPolicy = server.createContent(major.getIntegerMajor(),
 					inputTemplate.getContentId());
 			newVersionPolicy.getContent().setExternalId(textContent.getId());
 			newVersionPolicy.getContent().setName(textContent.getId());
 		} else {
 			newVersionPolicy = createNewVersionOfExistingContent(contentId);
 		}
 
 		return newVersionPolicy;
 	}
 
 	private Policy createNewVersionOfExistingContent(ContentId contentId)
 			throws CMException {
 		VersionedContentId latestVersion = new VersionedContentId(contentId,
 				LATEST_VERSION);
 
 		latestVersion = server.translateSymbolicContentId(latestVersion);
 
 		Policy newVersionPolicy;
 		LockInfo lockInfo = server.getContent(latestVersion).getLockInfo();
 
 		if (lockInfo != null
 				&& !lockInfo.getLocker().equals(server.getCurrentCaller())) {
 			((Content) server.getContent(contentId)).forcedUnlock();
 		}
 
 		newVersionPolicy = server.createContentVersion(latestVersion);
 
 		return newVersionPolicy;
 	}
 
 	private InputTemplate getInputTemplate(TextContent textContent)
 			throws DeployException, CMException {
 		Reference inputTemplateReference = textContent.getInputTemplate();
 
 		TextContent atTemplate = textContent;
 
 		while (inputTemplateReference == null
 				&& atTemplate.getTemplateId() != null) {
 			atTemplate = contentSet.get(atTemplate.getTemplateId());
 
 			if (atTemplate != null) {
 				inputTemplateReference = atTemplate.getInputTemplate();
 			}
 		}
 
 		if (inputTemplateReference == null) {
 			throw new DeployException(textContent.getId()
 					+ " needs to specify an input "
 					+ "template since the object did not already exist.");
 		}
 
 		ContentId inputTemplateId = inputTemplateReference.resolveId(server);
 
 		InputTemplate inputTemplate;
 
 		try {
 			inputTemplate = CheckedCast.cast(
 					server.getContent(inputTemplateId), InputTemplate.class);
 		} catch (CheckedClassCastException e) {
 			throw new DeployException("The input template "
 					+ inputTemplateReference
 					+ " did not have InputTemplate as policy.");
 		}
 		return inputTemplate;
 	}
 
 	private Major getMajor(PolicyCMServer server, InputTemplate inputTemplate)
 			throws CMException, DeployException {
 		String majorString = inputTemplate.getComponent("polopoly.Client",
 				"major");
 
 		if (majorString == null) {
 			logger.log(
 					Level.WARNING,
 					"The input template "
 							+ inputTemplate.getName()
 							+ " did not specify its major (type). Assuming \"Article\".");
 
 			majorString = "Article";
 		}
 
 		Major result = Major.getMajor(majorString);
 
 		if (result == Major.UNKNOWN) {
 			throw new DeployException("The input template "
 					+ inputTemplate.getName()
 					+ " specified the unknown major \"" + majorString + "\".");
 		}
 
 		return result;
 	}
 
 	public void setIgnoreContentListAddFailures(
 			boolean ignoreContentListAddFailures) {
 		this.ignoreContentListAddFailures = ignoreContentListAddFailures;
 	}
 
 	public boolean isIgnoreContentListAddFailures() {
 		return ignoreContentListAddFailures;
 	}
 }
