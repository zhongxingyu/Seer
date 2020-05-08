 package org.reviewboard.rtc;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import com.ibm.team.links.common.ILink;
 import com.ibm.team.links.common.ILinkCollection;
 import com.ibm.team.links.common.IReference;
 import com.ibm.team.links.common.factory.IReferenceFactory;
 import com.ibm.team.links.common.service.ILinkService;
 import com.ibm.team.links.service.ILinkServiceLibrary;
 import com.ibm.team.process.common.IProjectArea;
 import com.ibm.team.process.common.IProjectAreaHandle;
 import com.ibm.team.repository.common.IContributor;
 import com.ibm.team.repository.common.IContributorHandle;
 import com.ibm.team.repository.common.IItem;
 import com.ibm.team.repository.common.TeamRepositoryException;
 import com.ibm.team.repository.service.IRepositoryItemService;
 import com.ibm.team.scm.common.IChangeSet;
 import com.ibm.team.scm.common.IChangeSetHandle;
 import com.ibm.team.scm.common.dto.IWorkspaceSearchCriteria;
 import com.ibm.team.scm.common.links.ILinkConstants;
 import com.ibm.team.workitem.common.model.IApproval;
 import com.ibm.team.workitem.common.model.IApprovals;
 import com.ibm.team.workitem.common.model.IWorkItem;
 import com.ibm.team.workitem.common.model.WorkItemLinkTypes;
 
 public class WorkItemInformationRetriever {
 
 	private static final String APPROVAL_REVIEWER_TYPE = "com.ibm.team.workitem.approvalType.review";
 	private static final String TIMESTAMP_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
 
 	private final IWorkItem workItem;
 
 	private final IRepositoryItemService repositoryItemService;
 	private final ILinkService linkService;
 	private Date lastCodeChange;
 
 	private final Gson gson;
 
 	public WorkItemInformationRetriever(IWorkItem workitem, IRepositoryItemService repositoryItemService, ILinkService linkService) {
 		this.gson = new GsonBuilder().setDateFormat(TIMESTAMP_DATE_FORMAT).create();
 		this.workItem = workitem;
 		this.linkService = linkService;
 		this.repositoryItemService = repositoryItemService;
 	}
 
 	public String getSummary() {
 
 		return workItem.getHTMLSummary().getPlainText();
 	}
 
 	public String getDescription() {
 
 		return workItem.getHTMLDescription().getPlainText();
 	}
 
 	public String getId() {
 
 		return String.valueOf(workItem.getId());
 	}
 
 	public boolean hasReview() {
 
 		return true;
 	}
 
 	public String[] getReviewers() throws TeamRepositoryException {
 
 		List<IContributorHandle> reviewers = new ArrayList<IContributorHandle>();
 
 		IApprovals approvals = workItem.getApprovals();
 		List<IApproval> contents = approvals.getContents();
 
 		for (IApproval approval : contents) {
 
 			String typeIdentifier = approval.getDescriptor().getTypeIdentifier();
 
 			if (!APPROVAL_REVIEWER_TYPE.equals(typeIdentifier)) {
 				continue;
 			}
 
 			IContributorHandle reviewer = approval.getApprover();
 			reviewers.add(reviewer);
 		}
 
 		return handles2userIds(reviewers);
 	}
 
 	public String getOwner() throws TeamRepositoryException {
 
 		return handle2userId(workItem.getOwner());
 	}
 
 
 	public ReviewBoardLink getLink(String address) throws URISyntaxException, TeamRepositoryException {
 
 		Collection<ILink> links = getRelatedArtifactsLinks();
 
 		for (ILink iLink : links) {
 
 			String extraInfo = iLink.getTargetRef().getExtraInfo();
 			if (extraInfo == null)
 				continue;
 
 			LinkExtraInfo lExtraInfo = gson.fromJson(extraInfo, LinkExtraInfo.class);
 			if (lExtraInfo != null && lExtraInfo.updateUrl.startsWith(address)) {
 				return new ReviewBoardLink(iLink, lExtraInfo);
 			}
 		}
 
 		return null;
 	}
 
 	@SuppressWarnings("unchecked")
 	private Collection<ILink> getRelatedArtifactsLinks() throws TeamRepositoryException {
 		ILinkServiceLibrary linkServiceLibrary = (ILinkServiceLibrary) linkService.getServiceLibrary(ILinkServiceLibrary.class);
 		IReference source = linkServiceLibrary.referenceFactory().createReferenceToItem(workItem);
 		ILinkCollection query = linkServiceLibrary.findLinksBySource(source).getLinks();
 		return query.getLinksById(WorkItemLinkTypes.RELATED_ARTIFACT);
 	}
 
 	private String handle2userId(IContributorHandle contributorHandle) throws TeamRepositoryException {
 
 		IContributor contributor = (IContributor) repositoryItemService.fetchItem(contributorHandle, null);
 		return contributor.getUserId();
 	}
 
 	private String[] handles2userIds(List<IContributorHandle> reviewers) throws TeamRepositoryException {
 
 		List<String> userIds = new ArrayList<String>();
 
 		for (IContributorHandle reviewer : reviewers) {
 			userIds.add(handle2userId(reviewer));
 		}
 
 		return userIds.toArray(new String[0]);
 	}
 
 	public List<IChangeSet> getChangeSets() throws TeamRepositoryException {
 
 		List<IChangeSetHandle> csHandles = getChangeSetHandles();
 
 		IChangeSetHandle[] csHandlesArray = csHandles.toArray(new IChangeSetHandle[0]);
 
 		IItem[] fetchItems = repositoryItemService.fetchItems(csHandlesArray, IRepositoryItemService.COMPLETE);
 		List<IChangeSet> changeSets = new ArrayList<IChangeSet>();
 
 		for (IItem iItem : fetchItems) {
 
 			IChangeSet changeSet = (IChangeSet) iItem;
 
 			if (changeSet == null) { // Access to change set is denied.
 				continue;
 			}
 
 			Date lastChangeDate = changeSet.getLastChangeDate();
 			if (this.lastCodeChange == null ||
 				this.lastCodeChange.before(lastChangeDate)) {
 				this.lastCodeChange = lastChangeDate;
 			}
 
 			changeSets.add(changeSet);
 		}
 
 		return changeSets;
 	}
 
 	private List<IChangeSetHandle> getChangeSetHandles() throws TeamRepositoryException {
 		IReference ref = IReferenceFactory.INSTANCE.createReferenceToItem(workItem);
 
 		ILink[] csLinks = (ILink[]) linkService.findAuditableLinksByNameByEndpoint(ILinkConstants.CHANGESET_WORKITEM_LINKTYPE_ID, ref);
 
 		List<IChangeSetHandle> csHandles = new ArrayList<IChangeSetHandle>();
 
 		for (ILink link : csLinks) {
 			IReference csRef = link.getOtherRef(ref);
 
 			if (!csRef.isItemReference()) {
 			    continue;
 			}
 
 			if (! (csRef.resolve() instanceof IChangeSetHandle)) {
 			    continue;
 			}
 
 			IChangeSetHandle csHandle = (IChangeSetHandle) csRef.resolve();
 			csHandles.add(csHandle);
 		}
 
 		return csHandles;
 	}
 
 
 	public IWorkspaceSearchCriteria getWorkspaceCriteria() throws TeamRepositoryException {
 
 		String projectAreaName = getProjectAreaName();
 		IWorkspaceSearchCriteria criteria = IWorkspaceSearchCriteria.FACTORY.newInstance();
 		criteria.setPartialNameIgnoreCase(projectAreaName);
 
 		return criteria;
 	}
 
 	public void createReviewLink(ReviewBoardLink link) throws URISyntaxException, TeamRepositoryException {
 
 		ILinkServiceLibrary linkServiceLibrary = (ILinkServiceLibrary) linkService.getServiceLibrary(ILinkServiceLibrary.class);
 
 		link.extraInfo.lastCodeChange = new Timestamp(this.lastCodeChange.getTime());
 		String extraInfo = gson.toJson(link.extraInfo);
 
 		IReference sourceRef = linkServiceLibrary.referenceFactory().createReferenceToItem(workItem);
 		IReference targetRef = IReferenceFactory.INSTANCE.createReferenceFromURI(new URI(link.url), link.url, extraInfo);
 		ILink iLink = linkServiceLibrary.createLink(WorkItemLinkTypes.RELATED_ARTIFACT, sourceRef, targetRef);
 
 		linkServiceLibrary.saveLink(iLink);
 	}
 
 	public void updateReviewLink(ReviewBoardLink info) throws TeamRepositoryException, URISyntaxException {
 		if (info.link != null) {
 			ILinkServiceLibrary linkServiceLibrary = (ILinkServiceLibrary) linkService.getServiceLibrary(ILinkServiceLibrary.class);
 			linkServiceLibrary.deleteLink(info.link);
 		}
 		createReviewLink(info);
 	}
 
 	private String getProjectAreaName() throws TeamRepositoryException {
 
 		IProjectAreaHandle projectAreaHandle = workItem.getProjectArea();
 		IProjectArea projectArea = (IProjectArea) repositoryItemService.fetchItem(projectAreaHandle, IRepositoryItemService.COMPLETE);
 
 		return projectArea.getName();
 	}
 
 	public boolean codeChangedSinceLastReview(ReviewBoardLink link) throws TeamRepositoryException {
 		return lastCodeChange != null && !lastCodeChange.equals(link.extraInfo.lastCodeChange); // > not !=
 	}
 }
