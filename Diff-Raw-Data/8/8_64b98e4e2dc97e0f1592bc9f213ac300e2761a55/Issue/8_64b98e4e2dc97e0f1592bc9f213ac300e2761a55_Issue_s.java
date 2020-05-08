 /*(c) Copyright 2008, VersionOne, Inc. All rights reserved. (c)*/
 package com.versionone.om;
 
 import java.util.Collection;
 import java.util.Map;
 
 import com.versionone.DB.DateTime;
 import com.versionone.om.filters.EpicFilter;
 import com.versionone.om.filters.PrimaryWorkitemFilter;
 import com.versionone.om.filters.RequestFilter;
 import com.versionone.om.listvalue.IssuePriority;
 import com.versionone.om.listvalue.IssueResolutionReason;
 import com.versionone.om.listvalue.IssueType;
 import com.versionone.om.listvalue.WorkitemSource;
 
 /**
  * Represents an issue in the VersionOne system.
  */
 @MetaDataAttribute("Issue")
 public class Issue extends ProjectAsset {
 
    private static final String END_DATE = "EndDate";
 
     /**
      * Constructor used to represent an Issue entity that DOES exist in the
      * VersionOne System.
      *
      * @param id Unique ID of this entity.
      * @param instance this entity belongs to.
      */
     Issue(AssetID id, V1Instance instance) {
         super(id, instance);
     }
 
     /**
      * Constructor used to represent an Issue entity that does NOT yet exist in
      * the VersionOne System.
      *
      * @param instance this entity belongs to.
      */
     Issue(V1Instance instance) {
         super(instance);
     }
 
     /**
      * @return This Issue rank order among all Issues.
      */
     @MetaRenamedAttribute("Order")
     public Rank<Issue> getRankOrder() {
         return (Rank<Issue>) getRank("Order");
     }
 
     /**
      * Stories and Defects associated with this Issue.
      *
      * @param filter Criteria to filter stories and defects on. Pass a
      *                DefectFilter or StoryFilter to get only Defects or
      *                Stories, respectively.
      * @return A collection primary work items that belong to this issue
      *         filtered by the passed in filter.
      */
     public Collection<PrimaryWorkitem> getPrimaryWorkitems(
             PrimaryWorkitemFilter filter) {
         filter = (filter != null) ? filter : new PrimaryWorkitemFilter();
 
         filter.issues.clear();
         filter.issues.add(this);
         return getInstance().get().primaryWorkitems(filter);
     }
 
     /**
      * Requests associated with this Issue.
      *
      * @param filter Criteria to filter stories and defects on. Pass a
      *                RequestFilter to get only Request, respectively.
      * @return A collection requests that belong to this issue filtered by the
      *         passed in filter.
      */
     public Collection<Request> getRequests(RequestFilter filter) {
         filter = (filter != null) ? filter : new RequestFilter();
 
         filter.issues.clear();
         filter.issues.add(this);
         return getInstance().get().requests(filter);
     }
 
     /**
      * Stories and Defects that cannot be completed because of this Issue.
      *
      * @param filter Criteria to filter stories and defects on. Pass a
      *                DefectFilter or StoryFilter to get only Defects or
      *                Stories, respectively.
      * @return A collection stories and defects cannot be completed because of
      *         this Issue filtered by the passed in filter.
      */
     public Collection<PrimaryWorkitem> getBlockedPrimaryWorkitems(
             PrimaryWorkitemFilter filter) {
         filter = (filter != null) ? filter : new PrimaryWorkitemFilter();
 
         filter.issues.clear();
         filter.issues.add(this);
         return getInstance().get().primaryWorkitems(filter);
     }
 
     /**
      * Epics that cannot be completed because of this Issue.
      *
      * @param filter Criteria to filter epics on.
      * @return A collection epics cannot be completed because of
      *         this Issue filtered by the passed in filter.
      */
     public Collection<Epic> getBlockedEpics(
             EpicFilter filter) {
         filter = (filter != null) ? filter : new EpicFilter();
 
         filter.blockingIssues.clear();
         filter.blockingIssues.add(this);
         return getInstance().get().epics(filter);
     }
 
     /**
      * Epics associated with this Issue.
      *
      * @param filter Criteria to filter epics on. Pass a EpicFilter to get only
      *                Epic, respectively.
      * @return A collection epics that belong to this issue filtered by the
      *         passed in filter.
      */
     public Collection<Epic> getEpics(EpicFilter filter) {
         filter = (filter != null) ? filter : new EpicFilter();
 
         filter.issues.clear();
         filter.issues.add(this);
         return getInstance().get().epics(filter);
     }
 
     /**
      * @return This Issue's Source.
      */
     public IListValueProperty getSource() {
         return getListValue(WorkitemSource.class, "Source");
     }
 
     /**
      * @return This Issue's Type.
      */
     public IListValueProperty getType() {
         return getListValue(IssueType.class, "Category");
     }
 
     /**
      * @return This Issue's Priority.
      */
     public IListValueProperty getPriority() {
         return getListValue(IssuePriority.class, "Priority");
     }
 
     /**
      * @return Reason this Issue was resolved.
      */
     public IListValueProperty getResolutionReason() {
         return getListValue(IssueResolutionReason.class, "ResolutionReason");
     }
 
     /**
      * @return Text field for the description of how this Request was resolved.
      */
     public String getResolutionDetails() {
         return (String) get("Resolution");
     }
 
     /**
      * @param value Text field for the description of how this Request was
      *                resolved.
      */
     public void setResolutionDetails(String value) {
         set("Resolution", value);
     }
 
     /**
      * @return Name of person or organization originating this Issue.
      */
     public String getIdentifiedBy() {
         return (String) get("IdentifiedBy");
     }
 
     /**
      * @param value Name of person or organization originating this Issue.
      */
     public void setIdentifiedBy(String value) {
         set("IdentifiedBy", value);
     }
 
     /**
      * @return Cross-reference of this Issue with an external system.
      */
     public String getReference() {
         return (String) get("Reference");
     }
 
     /**
      * @param value Cross-reference of this Issue with an external system.
      */
     public void setReference(String value) {
         set("Reference", value);
     }
 
     /**
      * @return Date this Issue brings the system down to a screeching halt.
      */
     public DateTime getTargetDate() {
        return new DateTime(get(END_DATE));
     }
 
     /**
      * @param value Date this Issue brings the system down to a screeching halt.
      */
     public void setTargetDate(DateTime value) {
        set(END_DATE, value.getDate());
     }
 
     /**
      * @return The Team this Issue is assigned to.
      */
     public Team getTeam() {
         return getRelation(Team.class, "Team");
     }
 
     /**
      * @param value The Team this Issue is assigned to.
      */
     public void setTeam(Team value) {
         setRelation("Team", value);
     }
 
     /**
      * @return The Member who owns this Issue.
      */
     public Member getOwner() {
         return getRelation(Member.class, "Owner");
     }
 
     /**
      * @param value The Member who owns this Issue.
      */
     public void setOwner(Member value) {
         setRelation("Owner", value);
     }
 
     /**
      * @return The Retrospectives related to this Issue.
      */
     public Collection<Retrospective> getRetrospectives() {
         return getMultiRelation("Retrospectives");
     }
 
     /**
      * Inactivates the Issue.
      *
      * @throws UnsupportedOperationException The Issue is an invalid state for
      *                 the Operation, e.g. it is already closed.
      */
     @Override
     void closeImpl() throws UnsupportedOperationException {
         getInstance().executeOperation(this, "Inactivate");
     }
 
     /**
      * Reactivates the Issue.
      *
      * @throws UnsupportedOperationException The Issue is an invalid state for
      *                 the Operation, e.g. it is already active.
      */
     @Override
     void reactivateImpl() throws UnsupportedOperationException {
         getInstance().executeOperation(this, "Reactivate");
     }
 
     /**
      * Creates a Story from this Issue.
      *
      * @return A Story in the VersionOne system related to this Issue.
      */
     public Story generateStory() {
         return generateStory(null);
     }
 
     /**
      * Creates a Story from this Issue.
      *
      * @param attributes additional attributes for the Story.
      * @return A Story in the VersionOne system related to this Issue.
      */
     public Story generateStory(Map<String, Object> attributes) {
         Story result = getInstance().createNew(Story.class, this);
         getInstance().create().addAttributes(result, attributes);
         result.save();
         return result;
     }
 
     /**
      * Creates a Defect from this Issue.
      *
      * @return A Defect in the VersionOne system related to this Issue.
      */
     public Defect generateDefect() {
         return generateDefect(null);
     }
 
     /**
      * Creates a Defect from this Issue.
      *
      * @param attributes additional attributes for
      * @return A Defect in the VersionOne system related to this Issue.
      */
     public Defect generateDefect(Map<String, Object> attributes) {
         Defect result = getInstance().createNew(Defect.class, this);
         getInstance().create().addAttributes(result, attributes);
 
         result.save();
         return result;
     }
 }
