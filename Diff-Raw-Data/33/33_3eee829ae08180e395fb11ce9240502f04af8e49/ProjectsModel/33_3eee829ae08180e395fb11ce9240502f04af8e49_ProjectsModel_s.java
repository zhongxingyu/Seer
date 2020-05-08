 package org.hackystat.projectbrowser.page.projects;
 
 import java.io.Serializable;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.Locale;
 import javax.xml.datatype.DatatypeConfigurationException;
 import javax.xml.datatype.DatatypeFactory;
 import javax.xml.datatype.XMLGregorianCalendar;
 import org.apache.wicket.IClusterable;
 import org.hackystat.projectbrowser.ProjectBrowserSession;
 import org.hackystat.sensorbase.resource.projects.jaxb.Invitations;
 import org.hackystat.sensorbase.resource.projects.jaxb.Members;
 import org.hackystat.sensorbase.resource.projects.jaxb.Project;
 import org.hackystat.sensorbase.resource.projects.jaxb.Properties;
 import org.hackystat.sensorbase.resource.projects.jaxb.Property;
 import org.hackystat.sensorbase.resource.projects.jaxb.Spectators;
 import org.hackystat.sensorbase.resource.projects.jaxb.UriPatterns;
 import org.hackystat.utilities.tstamp.Tstamp;
 
 /**
  * Provides a model for the management of projects.
  * 
  * @author Philip Johnson.
  * @author Randy Cox.
  */
 public class ProjectsModel implements Serializable, IClusterable {
 
   /** For serialization. */
   private static final long serialVersionUID = 1L;
 
   /** Default project name. */
   private static final String DEFAULT_PROJECT = "Default";
   
   /** The projects for this user. */
   private List<Project> projects = null;
 
   /** Selected project for this user. */
   private Project project = null;
 
   /** Current selected members from member list. */
   private List<String> memberSelection = new ArrayList<String>();
 
   /** Feedback message. */
   private String feedback = "";
 
   /** Feedback message. */
   private String projectRename = "";
 
   /** Holds rows of property uris. */
   private List<PropUriRowModel> propUriRowsView = new ArrayList<PropUriRowModel>();
 
   /** Extra property uri rows after data. */
   private int extraPropertyUriRows = 1;
 
   /**
    * The default constructor, required by Wicket.
    */
   public ProjectsModel() {
     // does nothing.
   }
 
   /**
    * The sets user selected project.
    * 
    * @param selectedProject User selected project.
    */
   public ProjectsModel(Project selectedProject) {
     setProject(selectedProject);
   }
 
   /**
    * Returns the list of projects.
    * 
    * @return The list of projects.
    */
   public List<Project> getProjects() {
     ProjectBrowserSession.get().clearProjectList();
     this.setProjects(ProjectBrowserSession.get().getProjectList());
     return this.projects;
   }
 
   /**
    * Sets the projects.
    * 
    * @param projects The projects.
    */
   public void setProjects(List<Project> projects) {
     this.projects = projects;
   }
 
   /**
    * Return the Project for this page.
    * 
    * @return The project.
    */
   public Project getProject() {
     return this.project;
   }
 
   /**
    * Updates the ProjectsModel with the project summary instance.
    * 
    * @param project The project for this summary.
    */
   public final void setProject(Project project) {
     this.project = project;
   }
 
   /**
    * Create an empty project with all strings allocated.
    */
   public final void createProject() {
     this.project = new Project();
     this.project.setName("");
     this.project.setOwner(ProjectBrowserSession.get().getUserEmail());
     this.project.setDescription("");
 
     GregorianCalendar startDate = new GregorianCalendar();
     GregorianCalendar endDate = new GregorianCalendar();
     endDate.roll(Calendar.YEAR, true);
 
     DatatypeFactory factory;
     try {
       factory = DatatypeFactory.newInstance();
       this.project.setStartTime(factory.newXMLGregorianCalendar(startDate));
       this.project.setEndTime(factory.newXMLGregorianCalendar(endDate));
     }
     catch (DatatypeConfigurationException e) {
       e.printStackTrace();
     }
     this.project.setMembers(new Members());
     this.project.setInvitations(new Invitations());
     this.project.setSpectators(new Spectators());
     this.project.setProperties(new Properties());
     UriPatterns uriPatterns = new UriPatterns();
     uriPatterns.getUriPattern().add("*");
     this.project.setUriPatterns(uriPatterns);
     loadPropUriRowsView();
   }
 
   /**
    * Gets string representation of data.
    * @return String of project information.
    */
   public String getProjectStr() {
     String result = "Project = \n"
       + "  name = " + getProjectName() + "\n"
       + "  owner = " + getProjectOwner() + "\n"
       + "  desc = " + getProjectDesc() + "\n"
       + "  start = " + getProjectStartDate().toString() + "\n"
       + "  end = " + getProjectEndDate().toString() + "\n"
       + "  members = " + this.getProjectMembersStr() + "\n"
       + "  invitations = " + this.getProjectInvitationsStr() + "\n"
       + "  spectators = " + this.getProjectSpectatorsStr() + "\n"
       + "  properties = " + this.getProjectPropertiesStr() + "\n"
       + "  uris = " + this.getProjectUriPatternsStr() + "\n";
     return result;
   }
 
   /**
    * Get the name of current project.
    * 
    * @return current project
    */
   public String getProjectName() {
     return this.project.getName();
   }
 
   /**
    * Set the name of the current project.
    * 
    * @param name of project.
    */
   public void setProjectName(String name) {
     this.project.setName(name);
   }
 
   /**
    * Parses string and adds bold to any sub-string that matches the current user email.
    * 
    * @param string String to parse for user email
    * @return String with all user email sub-string in bold.
    */
   private String convertUserEmailToBold(String string) {
     String line = string.toLowerCase();
     String user = ProjectBrowserSession.get().getUserEmail().toLowerCase();
     String boldUser = "<b>" + user + "</b>";
     String result = line.replaceAll(user, boldUser);
     return result;
   }
 
   /**
    * Parses string and adds bold to any sub-string that matches the current user email.
    * 
    * @param string String to parse for user email
    * @return String with all user email sub-string in bold.
    */
   public String removeBold(String string) {
     String result = string.toLowerCase();
     result = result.replaceAll("<b>", "");
     result = result.replaceAll("</b>", "");
     return result;
   }
 
   /**
    * Return project owner, bold if owner is current user.
    * 
    * @return project owner.
    */
   public String getProjectOwner() {
     String result = getProject().getOwner();
     return result;
   }
 
   /**
    * Return project owner, bold if owner is current user.
    * 
    * @return project owner.
    */
   public String getProjectOwnerBold() {
     String result = getProject().getOwner();
     result = convertUserEmailToBold(result);
     return result;
   }
 
   /**
    * Set project owner.
    * 
    * @param owner Project owner
    */
   public void setProjectOwner(String owner) {
     getProject().setOwner(removeBold(owner));
   }
 
   /**
    * Get start date, convert from project.startTime.
    * 
    * @return project start date in GregorianCalendar date format.
    */
   public Date getProjectStartDate() {
     Project project = getProject();
     Calendar startTime = project.getStartTime().toGregorianCalendar();
     return startTime.getTime();
   }
 
   /**
    * Set start date, convert to project.startTime.
    * 
    * @param newDate start date in GregorianCalendar date format.
    */
   public void setProjectStartDate(Date newDate) {
     XMLGregorianCalendar startTime = Tstamp.makeTimestamp(newDate.getTime());
     getProject().setStartTime(startTime);
   }
 
   /**
    * Get start date, convert from project.startTime.
    * 
    * @return project start date in GregorianCalendar date format.
    */
   public Date getProjectEndDate() {
     Project project = getProject();
     Calendar endTime = project.getEndTime().toGregorianCalendar();
     return endTime.getTime();
   }
 
   /**
    * Set end date, convert to project.startTime.
    * 
    * @param newDate project end date in GregorianCalendar date format.
    */
   public void setProjectEndDate(Date newDate) {
     XMLGregorianCalendar endTime = Tstamp.makeTimestamp(newDate.getTime());
     getProject().setEndTime(endTime);
   }
 
   /**
    * Get project span: start and end date in one string.
    * 
    * @return project date span
    */
   public String getProjectSpan() {
     SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
     String result = dateFormat.format(getProjectStartDate()) + "\n"
         + dateFormat.format(getProjectEndDate());
     return result;
   }
 
   /**
    * Get project description.
    * 
    * @return project description.
    */
   public String getProjectDesc() {
     return getProject().getDescription();
   }
 
   /**
    * Set project description.
    * 
    * @param desc project description.
    */
   public void setProjectDesc(String desc) {
     Project project = getProject();
     project.setDescription(desc);
     setProject(project);
   }
 
   /**
    * Builds a string of members with a suffix of "(N)". N is the member type passed in by
    * "type".
    * 
    * @param members List of member names.
    * @param type Member type (M = member, I = Invitee, O = Observer).
    * @return list of members with (N) attached to denote type.
    */
   private String buildMemberListStr(List<String> members, String type) {
     StringBuffer result = new StringBuffer();
     for (String member : members) {
       if (result.length() == 0) {
         result.append(member).append(" (").append(type).append(')');
       }
       else {
         result.append('\n').append(member).append(" (").append(type).append(')');
       }
     }
     return convertUserEmailToBold(result.toString());
   }
 
   /**
    * Builds a string list of members with a suffix of "(N)". N is the member type passed in by
    * "type".
    * 
    * @param members List of member names.
    * @param type Member type (M = member, I = Invitee, O = Observer).
    * @return list of members with (N) attached to denote type.
    */
   private List<String> buildMemberList(List<String> members, String type) {
     List<String> list = new ArrayList<String>();
     for (String member : members) {
       String result = member + " (" + type + ')';
       list.add(result);
     }
     return list;
   }
 
   /**
    * Get the project members in comma delimited form.
    * 
    * @param members collection of members to build string with.
    * @param delimitor to add between members.
    * @return List of project members in comma delimited form.
    */
   public String buildMembersStr(List<String> members, String delimitor) {
     StringBuffer result = new StringBuffer();
     for (String member : members) {
       if (result.length() == 0) {
         result.append(member);
       }
       else {
         result.append(delimitor).append(member);
       }
     }
     return result.toString();
   }
 
   /**
    * Parse the project members in comma delimited form into a collection.
    * 
    * @param membersStr string to parse
    * @return List of project members in comma delimited form.
    */
   public List<String> parseMembersStr(String membersStr) {
     String comma = ",";
     List<String> result = new ArrayList<String>();
     if ((membersStr != null) && (!"".equals(membersStr))) {
       String str = membersStr.replace("\n", comma);
       str = str.replace(",,", comma);
       for (String member : str.split(comma)) {
         result.add(member.trim());
       }
     }
     return result;
   }
 
   /**
    * Remove members for project.
    * 
    * @param members List of project members to remove.
    */
   public void removeMembers(List<String> members) {
     getProject().getMembers().getMember().removeAll(members);
   }
 
   /**
    * Get the project members.
    * 
    * @return List of project members.
    */
   public List<String> getProjectMembers() {
     List<String> members;
     if ((getProject() == null) || (getProject().getMembers() == null)) {
       members = new ArrayList<String>();
     }
     else {
       members = getProject().getMembers().getMember();
     }
     return members;
   }
 
   /**
    * Set the project members.
    * 
    * @param members list of project members.
    */
   public void setProjectMembers(List<String> members) {
     getProject().getMembers().getMember().clear();
     getProject().getMembers().getMember().addAll(members);
   }
 
   /**
    * Get the project members in comma delimited form.
    * 
    * @return List of project members in comma delimited form.
    */
   public String getProjectMembersStr() {
     return buildMembersStr(getProjectMembers(), ", ");
   }
 
   /**
    * Set the project members from the comma delimited member string.
    * 
    * @param memberStr comma delimited member string to parse.
    */
   public void setProjectMembersStr(String memberStr) {
     setProjectMembers(parseMembersStr(memberStr));
   }
 
   /**
    * Set help note for project members, display when no members present.
    * @return help string
    */
   public String getProjectMemberHelp() {
     String msg = "To add members, please enter the emails of the "
       + "members you wish to invite in the Invitation field.  "
       + "When the invitee logs into this application, he/she will "
       + "be able to accept or decline your invitation.  If accepted "
       + "the invitee will become a member of your project.";
     return msg;
   }
 
   /**
    * Get the project invitations.
    * 
    * @return Invitations.
    */
   public List<String> getProjectInvitations() {
     return getProject().getInvitations().getInvitation();
   }
 
   /**
    * Set the project invitations.
    * 
    * @param members list of project invitations.
    */
   public void setProjectInvitations(List<String> members) {
     getProject().getInvitations().getInvitation().clear();
     getProject().getInvitations().getInvitation().addAll(members);
   }
 
   /**
    * Get the project invitations in comma delimited form.
    * 
    * @return List of project invitations in comma delimited form.
    */
   public String getProjectInvitationsStr() {
     return buildMembersStr(getProjectInvitations(), ", ");
   }
 
   /**
    * Set the project invitations from the comma delimited member string.
    * 
    * @param memberStr comma delimited invitation string to parse.
    */
   public void setProjectInvitationsStr(String memberStr) {
     setProjectInvitations(parseMembersStr(memberStr));
   }
 
   /**
    * Get the project spectators.
    * 
    * @return Spectators.
    */
   public List<String> getProjectSpectators() {
     return getProject().getSpectators().getSpectator();
   }
 
   /**
    * Set the project invitations.
    * 
    * @param members list of project invitations.
    */
   public void setProjectSpectators(List<String> members) {
     getProject().getSpectators().getSpectator().clear();
     getProject().getSpectators().getSpectator().addAll(members);
   }
 
   /**
    * Get the project spectators in comma delimited form.
    * 
    * @return List of project spectators in comma delimited form.
    */
   public String getProjectSpectatorsStr() {
     return buildMembersStr(getProjectSpectators(), ", ");
   }
 
   /**
    * Set the project spectators from the comma delimited member string.
    * 
    * @param memberStr comma delimited spectator string to parse.
    */
   public void setProjectSpectatorsStr(String memberStr) {
     setProjectSpectators(parseMembersStr(memberStr));
   }
 
   /**
    * Get string representation of project members.
    * 
    * @return String representation of project members, comma delimited.
    */
   public String getProjectConsolidatedMembersStr() {
     String result = buildMemberListStr(getProjectMembers(), "M");
     result += ("".equals(result) ? "" : "\n" );
     result += buildMemberListStr(getProjectInvitations(), "I");
     result += ("".equals(result) ? "" : "\n" );
     result += buildMemberListStr(getProjectSpectators(), "S");
     return result;
   }
 
   /**
    * Get string representation of project members.
    * 
    * @return List representation of project members, invitees and spectators.
    */
   public List<String> getProjectConsolidatedMembers() {
     List<String> list = new ArrayList<String>();
     list.addAll(buildMemberList(getProjectMembers(), "M"));
     list.addAll(buildMemberList(getProjectInvitations(), "I"));
     list.addAll(buildMemberList(getProjectSpectators(), "S"));
     return list;
   }
 
   /**
    * Get the project properties.
    * 
    * @return list of properties.
    */
   public List<Property> getProjectProperties() {
     return getProject().getProperties().getProperty();
   }
 
   /**
    * Set the project properties.
    * 
    * @param items list of project properties.
    */
   public void setProjectProperties(List<Property> items) {
     getProject().getProperties().getProperty().clear();
     getProject().getProperties().getProperty().addAll(items);
   }
 
   /**
    * Get project properties with = signs.
    * 
    * @return string of properties with equal signs.
    */
   public String getProjectPropertiesStr() {
     List<String> list = new ArrayList<String>();
     for (Property property : getProjectProperties()) {
       list.add(property.getKey() + "=" + property.getValue());
     }
     return buildMembersStr(list, "\n");
   }
 
   /**
    * Get the project UriPatterns.
    * 
    * @return UriPattern.
    */
   public List<String> getProjectUriPatterns() {
     return getProject().getUriPatterns().getUriPattern();
   }
 
   /**
    * Set the project UriPatterns.
    * 
    * @param items Uri patterns to update.
    */
   public void setProjectUriPatterns(List<String> items) {
     getProject().getUriPatterns().getUriPattern().clear();
     getProject().getUriPatterns().getUriPattern().addAll(items);
   }
 
   /**
    * Get string representation of URL patterns.
    * 
    * @return String representation of project URI patterns.
    */
   public String getProjectUriPatternsStr() {
     return buildMembersStr(getProjectUriPatterns(), "\n");
   }
 
   /**
    * Get the project URIs in comma delimited form.
    * 
    * @return List of project URIs in comma delimited form.
    */
   public String getProjectUriCommaStr() {
     return buildMembersStr(getProjectUriPatterns(), ", ");
   }
 
   /**
    * Return the members selected from multiple list choice control.
    * 
    * @return set of members selected.
    */
   public List<String> getMemberSelection() {
     return this.memberSelection;
   }
 
   /**
    * Sets the members selected from the multiple list choice control.
    * 
    * @param memberSelection list of members in selection list
    */
   public void setMemberSelection(List<String> memberSelection) {
     this.memberSelection = memberSelection;
   }
 
   /**
    * True if this model contains no data.
    * 
    * @return True if this model has no data.
    */
   public boolean isEmpty() {
     return this.project == null;
   }
 
   /**
    * True if project can be edited.
    * 
    * @return True if project can be edited
    */
   public boolean isEditable() {
     if (this.isEmpty()) {
       return false;
     }
     Boolean editable = false;
     String owner = this.project.getOwner();
     String user = ProjectBrowserSession.get().getUserEmail();
     if (owner.compareToIgnoreCase(user) == 0) {
       editable = true;
     }
     if ((this.project.getName() != null)
         && (this.project.getName().compareToIgnoreCase(DEFAULT_PROJECT) == 0)) {
       editable = false;
     }
     return editable;
   }
 
   /**
    * True if project can be renamed.
    * 
    * @return True if project can be renamed
    */
   public boolean isRenameable() {
     if (this.isEmpty()) {
       return false;
     }
     Boolean renameable = false;
     String owner = this.project.getOwner();
     String user = ProjectBrowserSession.get().getUserEmail();
     if (owner.compareToIgnoreCase(user) == 0) {
       renameable = true;
     }
     if (this.project.getName().compareToIgnoreCase(DEFAULT_PROJECT) == 0) {
       renameable = false;
     }
     return renameable;
   }
 
   /**
    * True if project can be deleted.
    * 
    * @return True if project can be deleted.
    */
   public boolean isDeletable() {
     if (this.isEmpty()) {
       return false;
     }
     Boolean deletable = false;
     String owner = this.project.getOwner();
     String user = ProjectBrowserSession.get().getUserEmail();
     if (owner.compareToIgnoreCase(user) == 0) {
       deletable = true;
     }
     if (this.project.getName().compareToIgnoreCase(DEFAULT_PROJECT) == 0) {
       deletable = false;
     }
     return deletable;
   }
 
   /**
    * True if project can be left.
    * 
    * @return True if project can be left.
    */
   public boolean isLeavable() {
     if (this.isEmpty()) {
       return false;
     }
     Boolean leavable = false;
     String owner = this.project.getOwner();
     String user = ProjectBrowserSession.get().getUserEmail();
     List<String> members = this.project.getMembers().getMember();
     if (members.contains(user)) {
       leavable = true;
     }
     if (owner.compareToIgnoreCase(user) == 0) {
       leavable = false;
     }
     if (this.project.getName().compareToIgnoreCase(DEFAULT_PROJECT) == 0) {
       leavable = false;
     }
     return leavable;
   }
 
   /**
    * True if you have an invitation to this project.
    * 
    * @return True if project can be left.
    */
   public boolean isRepliable() {
     if (this.isEmpty()) {
       return false;
     }
     Boolean repliable = false;
     String user = ProjectBrowserSession.get().getUserEmail();
     List<String> members = this.project.getInvitations().getInvitation();
     if (members.contains(user)) {
       repliable = true;
     }
     return repliable;
   }
 
   /**
    * Gets current feedback string.
    * 
    * @return the feedback
    */
   public String getFeedback() {
     String feedback = this.feedback;
     return feedback;
   }
 
   /**
    * Sets the feedback string.
    * 
    * @param feedback the feedback to set
    */
   public void setFeedback(String feedback) {
     this.feedback = feedback;
   }
 
   /**
    * Get the list of properties and uris at bottom of edit form.
    * 
    * @return the propertyUriRows
    */
   public List<PropUriRowModel> getPropUriRowsView() {
     return propUriRowsView;
   }
 
   /**
    * Set the list of properties and uris at bottom of edit form.
    * 
    * @param propertyUriRows the propertyUriRows to set
    */
   public void setPropUriRowsView(List<PropUriRowModel> propertyUriRows) {
     this.propUriRowsView = propertyUriRows;
   }
 
   /**
    * Load propertyUriRows from project data.
    */
   public void loadPropUriRowsView() {
     if (this.project != null) {
       this.propUriRowsView.clear();
 
       Boolean propertyDone = false;
       Boolean uriDone = false;
       Boolean extraRowDone = false;
       int propertyIndex = 0;
       int uriIndex = 0;
       int filledRowCount = 0;
       List<Property> properties = this.project.getProperties().getProperty();
       List<String> uris = this.project.getUriPatterns().getUriPattern();
 
       while (!propertyDone || !uriDone || !extraRowDone) {
         PropUriRowModel row = new PropUriRowModel();
 
         if (propertyIndex < properties.size()) {
           row.propertyLabel = properties.get(propertyIndex).getKey();
           row.propertyValue = properties.get(propertyIndex).getValue();
           propertyIndex++;
         }
         else {
           row.propertyLabel = null;
           row.propertyValue = null;
         }
         propertyDone = (propertyIndex >= properties.size());
 
         row.uriPattern1 = (uriIndex < uris.size()) ? uris.get(uriIndex++) : null;
         row.uriPattern2 = (uriIndex < uris.size()) ? uris.get(uriIndex++) : null;
         row.uriPattern3 = (uriIndex < uris.size()) ? uris.get(uriIndex++) : null;
         uriDone = (uriIndex >= uris.size());
 
         this.propUriRowsView.add(row);
 
         if (propertyDone && uriDone) {
           filledRowCount = (filledRowCount == 0) ? (this.propUriRowsView.size()) : filledRowCount;
           extraRowDone = (this.propUriRowsView.size() >= (filledRowCount + 
             this.extraPropertyUriRows));
         }
       }
     }
   }
 
   /**
    * Save data from property rows into the project object.
    */
   public void savePropUriRowsSave() {
     Properties projectProperties = new Properties();
     List<Property> properties = projectProperties.getProperty();
     UriPatterns projectUris = new UriPatterns();
     List<String> uris = projectUris.getUriPattern();
 
     for (PropUriRowModel row : getPropUriRowsView()) {
       if ((row.getPropertyLabel() != null) || (row.getPropertyValue() != null)
           || (row.getUriPattern1() != null) || (row.getUriPattern2() != null)
           || (row.getUriPattern3() != null)) {
 
         if ((row.propertyLabel != null) && (!"".equals(row.propertyLabel))) {
           Property property = new Property();
           property.setKey(row.propertyLabel);
           property.setValue(row.propertyValue);
           properties.add(property);
         }
         if ((row.uriPattern1 != null) && (!"".equals(row.uriPattern1))) {
           uris.add(row.uriPattern1);
         }
         if ((row.uriPattern2 != null) && (!"".equals(row.uriPattern2))) {
           uris.add(row.uriPattern2);
         }
         if ((row.uriPattern3 != null) && (!"".equals(row.uriPattern3))) {
           uris.add(row.uriPattern3);
         }
       }
     }
     this.getProject().setProperties(projectProperties);
     this.getProject().setUriPatterns(projectUris);
   }
 
   /**
    * Add one row to bottom of Properties and URI pattern rows.
    */
   public void addPropUriRow() {
     getPropUriRowsView().add(new PropUriRowModel());
   }
 
   /**
    * Get the amount of blank lines to set under populated properties/uris.
    * 
    * @return the extraPropertyUriRows
    */
   public int getExtraPropertyUriRows() {
     return extraPropertyUriRows;
   }
 
   /**
    * Set the amount of blank lines to set under populated properties/uris.
    * 
    * @param extraPropertyUriRows the extraPropertyUriRows to set
    */
   public void setExtraPropertyUriRows(int extraPropertyUriRows) {
     this.extraPropertyUriRows = extraPropertyUriRows;
   }
 
   /**
    * Get name to rename project with.
    * 
    * @return the projectRename
    */
   public String getProjectRename() {
     return projectRename;
   }
 
   /**
    * Set name to rename project with.
    * 
    * @param projectRename the projectRename to set
    */
   public void setProjectRename(String projectRename) {
     this.projectRename = projectRename;
   }
 
   /**
    * Semantic check of project.
    * 
   * @return ture is semantic check passes.
    */
   public boolean isSemanticCheckOk() {
     boolean ok = true;
 
     Project project = getProject();
 
     // Check name for spaces
    if ((project.getName() == null) || (project.getName().contains(" "))) {
       this.feedback = "Invalid project name.";
       ok = false;
     }
 
     // Check dates
     GregorianCalendar start = project.getStartTime().toGregorianCalendar();
     GregorianCalendar end = project.getEndTime().toGregorianCalendar();
     if (start.after(end) || start.equals(end)) {
       this.feedback = "Project start should be earlier than project end date.";
       ok = false;
     }
 
     // Check for URI
     if (project.getUriPatterns().getUriPattern().isEmpty()) {
       this.feedback = "Project needs at least one URI.";
       ok = false;
     }
     return ok;
   }
 }
