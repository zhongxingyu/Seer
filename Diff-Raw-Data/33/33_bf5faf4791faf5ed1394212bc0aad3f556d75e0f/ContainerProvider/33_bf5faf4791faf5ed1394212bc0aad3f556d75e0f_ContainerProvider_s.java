 /*
  * Created On: 13-Jul-06 11:42:46 AM
  */
 package com.thinkparity.ophelia.browser.application.browser.display.provider.tab.container;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import com.thinkparity.codebase.assertion.Assert;
 import com.thinkparity.codebase.filter.Filter;
 import com.thinkparity.codebase.jabber.JabberId;
 import com.thinkparity.codebase.model.artifact.Artifact;
 import com.thinkparity.codebase.model.artifact.ArtifactReceipt;
 import com.thinkparity.codebase.model.container.Container;
 import com.thinkparity.codebase.model.container.ContainerVersion;
 import com.thinkparity.codebase.model.container.ContainerVersionArtifactVersionDelta.Delta;
 import com.thinkparity.codebase.model.document.Document;
 import com.thinkparity.codebase.model.document.DocumentVersion;
 import com.thinkparity.codebase.model.profile.Profile;
 import com.thinkparity.codebase.model.user.TeamMember;
 import com.thinkparity.codebase.model.user.User;
 
import com.thinkparity.ophelia.browser.application.browser.display.provider.CompositeFlatSingleContentProvider;
import com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.container.view.DocumentView;
import com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.container.view.DraftView;
import com.thinkparity.ophelia.model.Constants.Versioning;
 import com.thinkparity.ophelia.model.contact.ContactModel;
 import com.thinkparity.ophelia.model.container.ContainerDraftMonitor;
 import com.thinkparity.ophelia.model.container.ContainerModel;
 import com.thinkparity.ophelia.model.document.DocumentModel;
 import com.thinkparity.ophelia.model.events.ContainerDraftListener;
 import com.thinkparity.ophelia.model.user.UserModel;
 
 /**
  * <b>Title:</b>thinkParity Container Tab Provider<br>
  * <b>Description:</b>A thinkParity container tab provider reads from the
  * various thinkParity model interfaces to provide the container tab with its
  * data.  It stipulates that the containers must not be archived.
  * 
  * It currently provides both display and non-display data.
  * 
  * @author rob_masako@shaw.ca; raymond@thinkparity.com
  * @version 1.1.2.5
  */
 public class ContainerProvider extends CompositeFlatSingleContentProvider {
 
     /** An instance of <code>ContactModel</code>. */
     private final ContactModel contactModel;
 
     /** An instance of <code>ContainerModel</code>. */
     private final ContainerModel containerModel;
     
     /** An instance of <code>DocumentModel</code>. */
     private final DocumentModel documentModel;
 
     /** An <code>Artifact</code> <code>Filter</code>. */
     private final Filter<? super Artifact> filter;
 
     /** An instance of <code>UserModel</code>. */
     private final UserModel userModel;
 
     /**
      * Create ContainerProvider.
      * 
      * @param profile
      *            The local user profile.
      * @param containerModel
      *            An instance of <code>ContainerModel</code>.
      * @param documentModel
      *            An instance of <code>DocumentModel</code>.
      * @param userModel
      *            An instance of <code>UserModel</code>.
      */
     public ContainerProvider(final Profile profile,
             final ContactModel contactModel,
             final ContainerModel containerModel,
             final DocumentModel documentModel, final UserModel userModel) {
         this(new Filter<Artifact>() {
             public Boolean doFilter(final Artifact o) {
                 return o.isArchived();
             }
         }, profile, contactModel, containerModel, documentModel, userModel);
     }
 
     /**
      * Create ContainerProvider.
      * 
      * @param profile
      *            The local user profile.
      * @param containerModel
      *            A thinkParity container interface.
      * @param documentModel
      *            A thinkParity document interface.
      * @param userModel
      *            A thinkParity user interface.
      */
     protected ContainerProvider(final Filter<? super Artifact> filter,
             final Profile profile, final ContactModel contactModel,
             final ContainerModel containerModel,
             final DocumentModel documentModel, final UserModel userModel) {
         super(profile);
         this.filter = filter;
         this.contactModel = contactModel;
         this.containerModel = containerModel;
         this.documentModel = documentModel;
         this.userModel = userModel;
     }
 
     /**
      * Obtain a draft monitor for a container.
      * 
      * @param containerId
      *            A container id <code>Long</code>.
      * @param listener
      *            A <code>ContainerDraftListener</code>.
      * @return A <code>ContainerDraftMonitor</code>.
      */
     public ContainerDraftMonitor getDraftMonitor(final Long containerId,
             final ContainerDraftListener listener) {
         return containerModel.getDraftMonitor(containerId, listener);
     }
 
     @Override
 	public Object getElement(Integer index, Object input) {
 		throw Assert.createNotYetImplemented("ContainerProvider#getElement");
 	}
 
 	@Override
 	public Object[] getElements(Integer index, Object input) {
 		throw Assert.createNotYetImplemented("ContainerProvider#getElements");
 	}
     
     /**
      * Determine if the container has been distributed.
      * 
      * @param containerId
      *            A <code>Long</code>.
      * @return True if this container has been distributed; false otherwise.
      */
     public Boolean isDistributed(final Long containerId) {
         return containerModel.isDistributed(containerId);
     }
 
 	/**
      * Determine if the draft document has been modified.
      * 
      * @param documentId
      *            A document id.
      * @return True if the draft document has been modified; false otherwise.
      */
     public Boolean isDraftDocumentModified(final Long documentId) {
         return documentModel.isDraftModified(documentId);
     }
 
     /**
      * Determine if the local draft is modified, ie. at least one document changed.
      * 
      * @param containerId
      *            A <code>Long</code>.
      * @return True if this container has a document that has been modified; false otherwise.
      */
     public Boolean isLocalDraftModified(final Long containerId) {
         return containerModel.isLocalDraftModified(containerId);
     }
 
     /**
 	 * Read a list of containers.
 	 * 
 	 * @return A list of containers.
 	 */
     public List<Container> read() {
     	return containerModel.read(filter);
     }
     
     /**
 	 * Read a container.
 	 * 
 	 * @param containerId
 	 *            A container id <code>Long</code>.
 	 * @return A <code>Container</code>.
 	 */
     public Container read(final Long containerId) {
     	final Container container = containerModel.read(containerId);
         if (null == container || filter.doFilter(container).booleanValue()) {
             return null;
         } else {
             return container;
         }
     }
 
     /**
      * Read and generate a list of document views for a document version. The
      * document view will consist of a document version; its delta as well as
      * the date it was first published.
      * 
      * @param containerId
      *            A container id <code>Long</code>.
      * @param versionId
      *            A container version id <code>Long</code>.
      * @return A <code>List</code> of <code>DocumentView</code>.
      */
     public List<DocumentView> readDocumentViews(final Long containerId,
             final Long versionId) {
         final Map<DocumentVersion, Delta> versions;
         final ContainerVersion previousVersion =
             containerModel.readPreviousVersion(containerId, versionId);
         if (null == previousVersion) {
             versions = containerModel.readDocumentVersionDeltas(containerId,
                     versionId);
         } else {
             versions = containerModel.readDocumentVersionDeltas(containerId,
                     versionId, previousVersion.getVersionId());
         }
         final List<DocumentView> views = new ArrayList<DocumentView>(versions.size());
         DocumentVersion firstVersion;
         DocumentView view;
         for (final Entry<DocumentVersion, Delta> entry : versions.entrySet()) {
             firstVersion = documentModel.readEarliestVersion(entry.getKey().getArtifactId());
 
             view = new DocumentView();
             view.setDelta(entry.getValue());
             view.setVersion(entry.getKey());
             view.setFirstPublishedOn(firstVersion.getCreatedOn());
             views.add(view);
         }
         Collections.sort(views, new Comparator<DocumentView>() {
             public int compare(final DocumentView o1, final DocumentView o2) {
                 return o1.getFirstPublishedOn().compareTo(
                        o2.getFirstPublishedOn());
             }
         });
         return views;
     }
 
     /**
      * Determine if the user is a contact.
      * 
      * @param userId
      *            A user id <code>Long</code>.
      * @return True if the user is a contact.
      */
     public Boolean doesExistContact(final Long userId) {
         return contactModel.doesExist(userId);
     }
     
     /**
      * Determine if there exists an outgoing user invitation.
      * 
      * @param userId
      *            A user id <code>Long</code>.
      * @return True if the user is a contact.
      */
     public Boolean doesExistOutgoingUserInvitationForUser(final Long userId) {
         return contactModel.doesExistOutgoingUserInvitationForUser(userId);
     }
     
     /**
 	 * Read a container draft view.
 	 * 
 	 * @param containerId
 	 *            A container id <code>Long</code>.
 	 * @return A <code>ContainerDraft</code>.
 	 */
     public DraftView readDraftView(final Long containerId) {
         final DraftView draftView = new DraftView();
         draftView.setDraft(containerModel.readDraft(containerId));
         DocumentVersion firstVersion;
         if (draftView.isSetDraft()) {
             for (final Document document : draftView.getDraft().getDocuments()) {
                firstVersion = documentModel.readVersion(document.getId(), Versioning.START);
                 if (null != firstVersion)
                     draftView.setFirstPublishedOn(
                             document, firstVersion.getCreatedOn());
             }
         }
         return draftView;
     }
     
     /**
      * Read a the latest container version.
      * 
      * @param containerId
      *            A container id <code>Long</code>.
      * @return A <code>ContainerVersion</code>.
      */
     public ContainerVersion readLatestVersion(final Long containerId) {
         return containerModel.readLatestVersion(containerId);
     }
 
     /**
      * Read the profile.
      * 
      * @return A <code>Profile</code>.  
      */
     public Profile readProfile() {
         return profile;
     }
 
     /**
      * Read the published to user list.
      * 
      * @param containerId
      *            A container id <code>Long</code>.
      * @param versionId
      *            A version id <code>Long</code>.
      * @return A list of <code>ArtifactReceipt</code>s.
      */
     public List<ArtifactReceipt> readPublishedTo(final Long containerId,
             final Long versionId) {
         return containerModel.readPublishedTo(containerId, versionId);
     }
 
     /**
      * Read the team.
      * 
      * @param containerId
      *            A container id <code>Long</code>.
      * @return A list of <code>TeamMember</code>s.
      */
     public List<TeamMember> readTeam(final Long containerId) {
         return containerModel.readTeam(containerId);
     }
 
     /**
      * Read a user.
      * 
      * @param userId
      *            A user id <code>JabberId</code>.
      * @return A <code>User</code>.
      */
     public User readUser(final JabberId userId) {
         return userModel.read(userId);
     }
     
     /**
      * Read a list of container versions.
      * 
      * @param containerId
      *            A container id <code>Long</code>.
      * @return A <code>List&lt;ContainerVersion&gt;</code>.
      */
     public List<ContainerVersion> readVersions(final Long containerId) {
         return containerModel.readVersions(containerId);
     }
 
     /**
 	 * Search for containers.
 	 * 
 	 * @param expression
 	 *            A search expression <code>String</code>.
 	 * @return A <code>List&lt;Long&gt;</code>.
 	 */
     public List<Long> search(final String expression) {
     	return containerModel.search(expression);
     }
 }
