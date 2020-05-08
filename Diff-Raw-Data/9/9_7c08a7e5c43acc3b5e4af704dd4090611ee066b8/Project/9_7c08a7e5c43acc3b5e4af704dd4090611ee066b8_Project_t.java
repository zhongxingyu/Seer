 /*
  * Copyright luntsys (c) 2004-2005,
  * Date: 2004-5-20
  * Time: 15:18
  *
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met: 1.
  * Redistributions of source code must retain the above copyright notice, this
  * list of conditions and the following disclaimer. 2. Redistributions in
  * binary form must reproduce the above copyright notice, this list of
  * conditions and the following disclaimer in the documentation and/or other
  * materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND ANY
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  */
 
 package com.luntsys.luntbuild.db;
 
 import com.luntsys.luntbuild.builders.Builder;
 import com.luntsys.luntbuild.facades.Constants;
 import com.luntsys.luntbuild.facades.lb12.BuilderFacade;
 import com.luntsys.luntbuild.facades.lb12.ProjectFacade;
 import com.luntsys.luntbuild.facades.lb12.VcsFacade;
 import com.luntsys.luntbuild.security.SecurityHelper;
 import com.luntsys.luntbuild.utility.*;
 import com.luntsys.luntbuild.vcs.Vcs;
 import org.acegisecurity.AccessDeniedException;
 import org.acegisecurity.acl.basic.AclObjectIdentity;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.StringReader;
 import java.io.File;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 /**
  * A Luntbuild project.
  * 
  * <p>This is a hibernate mapping class.</p>
  *
  * @author robin shine
  */
 public class Project implements AclObjectIdentity, VariableHolder {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 5880116718483855560L;
 
 	private static Log logger = LogFactory.getLog(Project.class);
 
 	private long id;
 	private String name;
 	private String description;
 
 	/**
 	 * List of version control systems configured for this project
 	 */
 	private List vcsList;
 
 	/**
 	 * List of builders configured for this project
 	 */
 	private List builderList;
 
 	/**
 	 * Set of schedules configured for this project
 	 */
 	private Set schedules;
 
 	/**
 	 * Set of vcs logins configured for this project
 	 */
 	private Set vcsLogins;
 
 	/**
 	 * persistent field
 	 */
 	private Set rolesMappings;
 
 	/**
 	 * List of notifier class names applicable for sending build notification of this
 	 * project
 	 */
 	private List notifiers;
 
 	private Set notifyMappings;
 
 	private String variables = "versionIterator=1";
 	private int logLevel = com.luntsys.luntbuild.facades.Constants.LOG_LEVEL_NORMAL;
 
     private static ArrayList dateFormats = new ArrayList();
     static {
         dateFormats.add(new SimpleDateFormat("MM/dd/yyyy"));
         dateFormats.add(new SimpleDateFormat("MM-dd-yyyy"));
         dateFormats.add(new SimpleDateFormat("d MMM yyyy"));
         dateFormats.add(new SimpleDateFormat("d-MMM-yyyy"));
         dateFormats.add(new SimpleDateFormat("d MMM. yyyy"));
         dateFormats.add(new SimpleDateFormat("MMM d, yyyy"));
         dateFormats.add(new SimpleDateFormat("MMM. d, yyyy"));
         dateFormats.add(DateFormat.getDateInstance(SimpleDateFormat.DEFAULT));
         dateFormats.add(DateFormat.getDateInstance(SimpleDateFormat.SHORT));
         dateFormats.add(DateFormat.getDateInstance(SimpleDateFormat.MEDIUM));
         dateFormats.add(DateFormat.getDateInstance(SimpleDateFormat.LONG));
         dateFormats.add(DateFormat.getDateInstance(SimpleDateFormat.FULL));
     }
 
 
 	/**
 	 * Sets the identifier of this project, will be called by hibernate.
 	 *
 	 * @param id the identifier of this project
 	 */
 	public void setId(long id) {
 		this.id = id;
 	}
 
 	/**
 	 * Gets the identifer of this project.
 	 * 
 	 * @return the identifer of this project
 	 */
 	public long getId() {
 		return id;
 	}
 
 	/**
 	 * Sets the name of this project.
 	 * 
 	 * @param name the name of this project
 	 */
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	/**
 	 * Gets the name of this project.
 	 * 
 	 * @return the name of this project
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * Sets the description of this project.
 	 * 
 	 * @param description the description of this project
 	 */
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	/**
 	 * Gets the description of this project.
 	 * 
 	 * @return the description of this project
 	 */
 	public String getDescription() {
 		return description;
 	}
 
 	/**
 	 * Gets the VCS list of this project.
 	 * 
 	 * @return the VCS list of this project
 	 * @see com.luntsys.luntbuild.vcs.Vcs
 	 */
 	public List getVcsList() {
 		if (vcsList == null)
 			vcsList = new ArrayList();
 		return vcsList;
 	}
 
 	/**
 	 * Sets the VCS list of this project.
 	 * 
 	 * @param vcsList the list of VCS adaptors
 	 * @see com.luntsys.luntbuild.vcs.Vcs
 	 */
 	public void setVcsList(List vcsList) {
 		this.vcsList = vcsList;
 	}
 
 	/**
 	 * Gets the builder list of this project.
 	 *
 	 * @return the builder list of this project
 	 * @see com.luntsys.luntbuild.builders.Builder
 	 */
 	public List getBuilderList() {
 		if (builderList == null)
 			builderList = new ArrayList();
 		return builderList;
 	}
 
 	/**
 	 * Sets the builder list of this project.
 	 * 
 	 * @param builderList the list of builders
 	 * @see com.luntsys.luntbuild.builders.Builder
 	 */
 	public void setBuilderList(List builderList) {
 		this.builderList = builderList;
 	}
 
 	/**
 	 * Gets the list of schedules of this project.
 	 * 
 	 * @return the schedules list of this project
 	 * @see Schedule
      * @since 1.3
 	 */
 	public Set getSchedules() {
 		if (schedules == null)
 			schedules = new HashSet();
 		return schedules;
 	}
 
 	/**
 	 * Sets the list of schedules of this project.
 	 * 
 	 * @param schedules the list of schedules
 	 * @see Schedule
      * @since 1.3
 	 */
 	public void setSchedules(Set schedules) {
 		this.schedules = schedules;
 	}
 
 	/**
 	 * Gets the schedule with the specified name. The schedule does not have to be for
 	 * this project.
 	 * 
 	 * @param scheduleName the name of the schedule
 	 * @return the schedule with the specified name or <code>null</code>
 	 */
 	public Schedule getSchedule(String scheduleName) {
 		return Luntbuild.getDao().loadSchedule(getName(), scheduleName);
 	}
 
 	/**
 	 * Empty method, only want to be conform with ognl indexed property.
 	 * 
 	 * @param scheduleName
 	 * @param schedule
 	 */
 	public void setSchedule(String scheduleName, Schedule schedule) {
 		// empty method, only want to conform to ognl indexed property
 	}
 
 	/**
 	 * Get system object. Mainly used for ognl evaluation.
 	 * 
 	 * @return the system object
 	 */
 	public OgnlHelper getSystem() {
 		return new OgnlHelper();
 	}
 
 	/**
 	 * Indicates whether some other object is "equal to" this one.
 	 * 
 	 * @param obj the reference object with which to compare
 	 * @return <code>true</code> if this object is the same as the obj argument; <code>false</code> otherwise
 	 */
 	public boolean equals(Object obj) {
 		if (obj != null && obj instanceof Project) {
 			if (getId() == ((Project) obj).getId())
 				return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Returns a hash code value for the object.
 	 * 
 	 * @return a hash code value for this object
 	 * @see #equals(Object)
 	 */
 	public int hashCode() {
 		return (int) getId();
 	}
 
 	/**
 	 * Gets the VCS login mappings.
 	 * 
 	 * @return the VCS login mappings
 	 * @see VcsLogin
 	 */
 	public Set getVcsLogins() {
 		if (vcsLogins == null)
 			vcsLogins = new HashSet();
 		return vcsLogins;
 	}
 
 	/**
 	 * Sets the VCS login mappings.
 	 * 
 	 * @param vcsLogins the VCS login mappings
 	 * @see VcsLogin
 	 */
 	public void setVcsLogins(Set vcsLogins) {
 		this.vcsLogins = vcsLogins;
 	}
 
 	/**
 	 * Determines the luntbuild user by the VCS login string.
 	 * 
 	 * @param login version control system login name
 	 * @param users luntbuild users list
 	 * @return maybe <code>null</code> if no user found to match this VCS login
 	 */
 	public User getUserByVcsLogin(String login, List users) {
 		VcsLogin vcsLogin = VcsLogin.findVcsLogin(getVcsLogins(), login);
 		if (vcsLogin != null)
 			return vcsLogin.getUser();
 
 		// continue to find based on global users
 		Iterator it = users.iterator();
 		while (it.hasNext()) {
 			User user = (User) it.next();
 			if (user.getName().equals(User.CHECKIN_USER_NAME))
 				continue;
 			if (login.equalsIgnoreCase(user.getName()))
 				return user;
 		}
 		return null;
 	}
 
 	/**
 	 * Validates all properties of this project.
 	 * 
 	 * @throws ValidationException if a property has an invalid value
 	 */
 	public void validate() throws ValidationException {
 		validateBasic();
 		Iterator it = getVcsList().iterator();
 		while (it.hasNext()) {
 			Vcs vcs = (Vcs) it.next();
 			vcs.validate();
 		}
 		it = getBuilderList().iterator();
 		while (it.hasNext()) {
 			Builder builder = (Builder) it.next();
 			builder.validate();
 		}
 	}
 
 	/**
 	 * Validates all properties of this project at build time. It is different from validate()
 	 * method in the way that it enforces {@link #getVcsList()} and {@link #getBuilderList()} not empty.
 	 * 
 	 * @throws ValidationException if a property has an invalid value
 	 */
 	public void validateAtBuildTime() throws ValidationException {
 		validate();
 		if (getVcsList().size() == 0)
 			throw new ValidationException("No Version Control System defined for project: " + getName());
 	}
 
 	/**
 	 * Validates the basic properties. Complicated properties such as {@link #getVcsList()}, {@link #getBuilderList()},
 	 * will not get validated.
 	 * 
 	 * @throws ValidationException if a property has an invalid value
 	 */
 	public void validateBasic() throws ValidationException {
 		try {
 			Luntbuild.validatePathElement(getName());
 		} catch (ValidationException e) {
 			throw new ValidationException("Invalid name: " + e.getMessage());
 		}
 		setName(getName().trim());
 
 		if (logLevel != com.luntsys.luntbuild.facades.Constants.LOG_LEVEL_BRIEF && logLevel != com.luntsys.luntbuild.facades.Constants.LOG_LEVEL_NORMAL &&
 				logLevel != Constants.LOG_LEVEL_VERBOSE)
 			throw new ValidationException("Invalid log level!");
 	}
 
 	/**
 	 * Gets the facade of this project.
 	 * 
 	 * @return the facade of this project
 	 */
 	public ProjectFacade getFacade() {
 		ProjectFacade facade = new ProjectFacade();
 		facade.setId(getId());
 		facade.setName(getName());
 		facade.setDescription(getDescription());
 		facade.setVariables(getVariables());
 		facade.setLogLevel(getLogLevel());
 		facade.setNotifiers(getNotifiers());
 		Iterator it = getVcsList().iterator();
 		while (it.hasNext()) {
 			Vcs vcs = (Vcs) it.next();
 			facade.getVcsList().add(vcs.getFacade());
 		}
 		it = getBuilderList().iterator();
 		while (it.hasNext()) {
 			Builder builder = (Builder) it.next();
 			facade.getBuilderList().add(builder.getFacade());
 		}
         List projectAdmins = new ArrayList();
         List projectBuilders = new ArrayList();
         List projectViewers = new ArrayList();
         it = getRolesMappings().iterator();
         while (it.hasNext()) {
             RolesMapping role = (RolesMapping) it.next();
             if (role.getRole().getName().equals("LUNTBUILD_PRJ_ADMIN")) {
                 projectAdmins.add(role.getUser().getName());
             } else if (role.getRole().getName().equals("LUNTBUILD_PRJ_BUILDER")) {
                 projectBuilders.add(role.getUser().getName());
             } else if (role.getRole().getName().equals("LUNTBUILD_PRJ_VIEWER")) {
                 projectViewers.add(role.getUser().getName());
             }
         }
        facade.setProjectAdmins((String[])projectAdmins.toArray(new String[projectAdmins.size()]));
        facade.setProjectBuilders((String[])projectBuilders.toArray(new String[projectBuilders.size()]));
        facade.setProjectViewers((String[])projectViewers.toArray(new String[projectViewers.size()]));
         List notifyUsers = new ArrayList();
         it = getNotifyMappings().iterator();
         while (it.hasNext()) {
             NotifyMapping notify = (NotifyMapping) it.next();
             notifyUsers.add(notify.getUser().getName());
         }
        facade.setNotifyUsers((String[])notifyUsers.toArray(new String[notifyUsers.size()]));
 		return facade;
 	}
 
 	/**
 	 * Sets the facade of this project.
 	 * 
 	 * @param facade the project facade
 	 * @throws RuntimeException if an exception happens while setting the facade
 	 */
 	public void setFacade(ProjectFacade facade) throws RuntimeException {
 		setDescription(facade.getDescription());
 		setVariables(facade.getVariables());
 		setLogLevel(facade.getLogLevel());
 		setNotifiers(facade.getNotifiers());
 		try {
 			getVcsList().clear();
 			Iterator it = facade.getVcsList().iterator();
 			while (it.hasNext()) {
 				VcsFacade vcsFacade = (VcsFacade) it.next();
 				Vcs vcs = (Vcs) Class.forName(vcsFacade.getVcsClassName()).newInstance();
 				vcs.setFacade(vcsFacade);
 				getVcsList().add(vcs);
 			}
 			getBuilderList().clear();
 			it = facade.getBuilderList().iterator();
 			while (it.hasNext()) {
 				BuilderFacade builderFacade = (BuilderFacade) it.next();
 				Builder builder = (Builder) Class.forName(builderFacade.getBuilderClassName()).newInstance();
 				builder.setFacade(builderFacade);
 				getBuilderList().add(builder);
 			}
 		} catch (InstantiationException e) {
 			throw new RuntimeException(e);
 		} catch (IllegalAccessException e) {
 			throw new RuntimeException(e);
 		} catch (ClassNotFoundException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	/**
 	 * Gets the notifiers configured for this project.
 	 * 
 	 * @return the notifiers configured for this project
 	 * @see com.luntsys.luntbuild.notifiers.Notifier
 	 */
 	public List getNotifiers() {
 		if (notifiers == null)
 			notifiers = new ArrayList();
 		return notifiers;
 	}
 
 	/**
 	 * Sets list of configured notifiers.
 	 * 
 	 * @param notifiers the list of notifiers
 	 * @see com.luntsys.luntbuild.notifiers.Notifier
 	 */
 	public void setNotifiers(List notifiers) {
 		this.notifiers = notifiers;
 	}
 
 	/**
 	 * Determines if the VCS contents of this project has changed. This function will use
 	 * the following thread local variables in {@link OgnlHelper}:
 	 * <p><code>workingSchedule</code>, this variable denotes the project which this method is initiated by</p>
 	 * <p><code>antProject</code>, this variable denotes the logging ant project this method should use</p>
 	 * <p><code>revisions</code>, this variable denotes the revisions for the working schedule</p>
 	 * 
      * @param sinceDate the date to check for changes since
 	 * @return <code>true</code> if the VCS contents of this project has changed
 	 */
 	public boolean isVcsModifiedSince(Date sinceDate) {
 		org.apache.tools.ant.Project antProject = OgnlHelper.getAntProject();
 		Schedule workingSchedule = OgnlHelper.getWorkingSchedule();
 		Revisions revisions;
 
 		antProject.log("Getting revisions for project \"" + getName() + "\"...");
 		Revisions allRevisions = new Revisions();
 		Iterator it = getVcsList().iterator();
 		while (it.hasNext()) {
 			Vcs vcs = (Vcs) it.next();
 			revisions = vcs.deriveBuildTimeVcs(antProject).getRevisionsSince(sinceDate, workingSchedule, antProject);
 			allRevisions.merge(revisions);
 		}
 		if (OgnlHelper.getRevisions() == null && workingSchedule.getProject() == this)
 			OgnlHelper.setRevisions(allRevisions);
 		return allRevisions.isFileModified();
 	}
 
     /**
 	 * Determines if the VCS contents of this project has changed. This function will use
 	 * the following thread local variables in {@link OgnlHelper}:
 	 * <p><code>workingSchedule</code>, this variable denotes the project which this method is initiated by</p>
 	 * <p><code>antProject</code>, this variable denotes the logging ant project this method should use</p>
 	 * <p><code>revisions</code>, this variable denotes the revisions for the working schedule</p>
 	 * 
      * @param sinceDate the date to check for changes since
 	 * @return <code>true</code> if the VCS contents of this project has changed
      */
     public boolean isVcsModifiedSince(String sinceDate) {
         Date date;
         for (Iterator iter = dateFormats.iterator(); iter.hasNext();) {
             DateFormat df = (DateFormat) iter.next();
 
             try {
                 date = df.parse(sinceDate);
                 return isVcsModifiedSince(date);
             } catch (ParseException ex) {
                 logger.error("Unable to parse date: " + sinceDate);
                 return false;
             } catch (NumberFormatException nfe) {
                 logger.error("Unable to parse date: " + sinceDate);
                 return false;
             }
         }
         return false;
     }
 
     /**
      * Determines if the VCS contents of this project has changed since the last build of a schedule.
      * The schedule does not have to be for this project.
      * 
      * @param scheduleName the name of the schedule to check
 	 * @return <code>true</code> if the VCS contents of this project has changed
 	 * @see #isVcsModifiedSince(Date)
      */
     public boolean isVcsModified(String scheduleName) {
         Schedule s = getSchedule(scheduleName);
         Build build = Luntbuild.getDao().loadLastBuild(s);
 
         return (build == null) ? true : isVcsModifiedSince(build.getStartDate());
     }
 
 	/**
 	 * Gets the configured notification mappings.
 	 * 
 	 * @return the configured notification mappings
 	 * @see NotifyMapping
 	 */
 	public Set getNotifyMappings() {
 		if (notifyMappings == null)
 			notifyMappings = new HashSet();
 		return notifyMappings;
 	}
 
 	/**
 	 * Sets the configured notification mappings.
 	 * 
 	 * @param notifyMappings the notification mappings
 	 * @see NotifyMapping
 	 */
 	public void setNotifyMappings(Set notifyMappings) {
 		this.notifyMappings = notifyMappings;
 	}
 
 	/**
 	 * Gets the log level of this project.
 	 * 
 	 * @return the log level
 	 * @see com.luntsys.luntbuild.facades.Constants#LOG_LEVEL_BRIEF
 	 * @see com.luntsys.luntbuild.facades.Constants#LOG_LEVEL_NORMAL
 	 * @see com.luntsys.luntbuild.facades.Constants#LOG_LEVEL_VERBOSE
 	 */
 	public int getLogLevel() {
 		return logLevel;
 	}
 
 	/**
 	 * Sets the log level of this project.
 	 * 
 	 * @param logLevel the log level
 	 * @see com.luntsys.luntbuild.facades.Constants#LOG_LEVEL_BRIEF
 	 * @see com.luntsys.luntbuild.facades.Constants#LOG_LEVEL_NORMAL
 	 * @see com.luntsys.luntbuild.facades.Constants#LOG_LEVEL_VERBOSE
 	 */
 	public void setLogLevel(int logLevel) {
 		this.logLevel = logLevel;
 	}
 
 	/**
 	 * Gets the roles mappings of this project.
 	 * 
 	 * @return the roles mappings of this project
 	 * @see RolesMapping
 	 */
 	public Set getRolesMappings() {
 		if (rolesMappings == null)
 			rolesMappings = new HashSet();
 		return rolesMappings;
 	}
 
 	/**
 	 * Sets the roles mappings of this project.
 	 * 
 	 * @param rolesMappings the roles mappings
 	 * @see RolesMapping
 	 */
 	public void setRolesMappings(Set rolesMappings) {
 		this.rolesMappings = rolesMappings;
 	}
 
 	/**
 	 * Gets the list of users with the specified role.
 	 * 
 	 * @param roleName the name of the role
 	 * @return the list of users with the role
 	 * @see RolesMapping
 	 */
 	public List getMappedRolesUserList(String roleName) {
 		List usersWithAssignedRoles = new ArrayList();
 
 		Set rml = getRolesMappings();
 
 		if (rml != null) {
 			Iterator iter = rml.iterator();
 
 			while (iter.hasNext()) {
 				RolesMapping rm = (RolesMapping) iter.next();
 
 				User user = rm.getUser();
 				Role role = rm.getRole();
 
 				if (role.getName().equals(roleName)) {
 					usersWithAssignedRoles.add(user);
 				}
 			}
 		}
 
 		return usersWithAssignedRoles;
 	}
 
 	/**
 	 * Maps a list of users to a role. This method will remove existing mappings for
 	 * the role.
 	 * 
 	 * @param userlist list of users to map to the role
 	 * @param roleName name of the role to map to
 	 * @see User
 	 */
 	public void putMappedRolesUserList(List userlist, String roleName) {
 		// remove all existing users from role
 		Set rolemappings = getRolesMappings();
 
 		if (rolemappings != null) {
 			Set mappingsToRemove = new HashSet();
 			Iterator iter = rolemappings.iterator();
 			while (iter.hasNext()) {
 				RolesMapping rolemapping = (RolesMapping) iter.next();
 				if (rolemapping.getRole().getName().equals(roleName)) {
 					mappingsToRemove.add(rolemapping);
 				}
 			}
 			rolemappings.removeAll(mappingsToRemove);
 		} else {
 			rolemappings = new HashSet();
 			setRolesMappings(rolemappings);
 		}
 
 		if (userlist != null) {
 			// find dbbased matching role
 			Role role = getMatchingRole(roleName);
 
 			Iterator iter = userlist.iterator();
 
 			while (iter.hasNext()) {
 				User user = (User) iter.next();
 
 				RolesMapping rm = new RolesMapping();
 
 				rm.setUser(user);
 				rm.setProject(this);
 				rm.setRole(role);
 
 				rolemappings.add(rm);
 			}
 		}
 	}
 
 	/**
 	 * Gets the role object matching specified role name.
 	 * 
 	 * @param roleName the name of the role
 	 * @return the object matching specified role name
 	 */
 	private Role getMatchingRole(String roleName) {
 		List internalRoles = Role.getRoles();
 
 		Iterator iter = internalRoles.iterator();
 		boolean found = false;
 		Role role = null;
 
 		while (iter.hasNext() && (found == false)) {
 			role = (Role) iter.next();
 
 			found = role.getName().equals(roleName);
 		}
 
 		return role;
 	}
 
 	/**
 	 * Returns a string representation of this object.
 	 * 
 	 * @return a string representation of this object
 	 */
 	public String toString() {
 		return getName();
 	}
 
 	/**
 	 * Creates a new project by coping this project.
 	 * 
 	 * @return the new project
 	 */
 	public Project createNewByCopy() {
 		Project newProject = new Project();
 		newProject.setBuilderList(getBuilderList());
 		newProject.setDescription(getDescription());
 		newProject.setVariables(getVariables());
 		newProject.setLogLevel(getLogLevel());
 		newProject.setName(getName());
 		newProject.setNotifiers(getNotifiers());
 		newProject.setVcsList(getVcsList());
 		Iterator it = getRolesMappings().iterator();
 		while (it.hasNext()) {
 			RolesMapping mapping = (RolesMapping) it.next();
 			RolesMapping newMapping = new RolesMapping(mapping.getUser(), newProject, mapping.getRole());
 			newProject.getRolesMappings().add(newMapping);
 		}
 		it = getNotifyMappings().iterator();
 		while (it.hasNext()) {
 			NotifyMapping mapping = (NotifyMapping) it.next();
 			NotifyMapping newMapping = new NotifyMapping(newProject, mapping.getUser());
 			newProject.getNotifyMappings().add(newMapping);
 		}
 
 		it = getVcsLogins().iterator();
 		while (it.hasNext()) {
 			VcsLogin vcsLogin = (VcsLogin) it.next();
 			VcsLogin newVcsLogin = new VcsLogin(newProject, vcsLogin.getUser(), vcsLogin.getLogin());
 			newVcsLogin.setId(vcsLogin.getId());
 			newProject.getVcsLogins().add(newVcsLogin);
 		}
 		it = getSchedules().iterator();
 		while (it.hasNext()) {
 			Schedule schedule = (Schedule) it.next();
 			Schedule newSchedule = new Schedule();
 			newSchedule.setId(schedule.getId());
 			newSchedule.setProject(newProject);
 			newSchedule.setName(schedule.getName());
 			newSchedule.setFacade(schedule.getFacade());
 			newProject.getSchedules().add(newSchedule);
 		}
 		return newProject;
 	}
 
 	/**
 	 * Gets the list of users to nofify based on the notification mappings of this project.
 	 * 
 	 * @return the list of users to notify
 	 * @see User
 	 * @see #getNotifyMappings()
 	 */
 	public List getUsersToNotify() {
 		List usersToNotify = new ArrayList();
 		Iterator it = getNotifyMappings().iterator();
 		while (it.hasNext()) {
 			NotifyMapping mapping = (NotifyMapping) it.next();
 			usersToNotify.add(mapping.getUser());
 		}
 		return usersToNotify;
 	}
 
 	/**
 	 * Maps the list of users to nofify. This method will remove existing notification mappings.
 	 * 
 	 * @param usersToNotify list of users to notify
 	 * @see User
 	 */
 	public void putUsersToNotify(List usersToNotify) {
 		getNotifyMappings().clear();
 		Iterator it = usersToNotify.iterator();
 		while (it.hasNext()) {
 			User user = (User) it.next();
 			NotifyMapping mapping = new NotifyMapping();
 			mapping.setProject(this);
 			mapping.setUser(user);
 			getNotifyMappings().add(mapping);
 		}
 	}
 
 	/**
 	 * Gets all variables, encoded as a string.
 	 * 
 	 * @return all variables encoded as a string
 	 */
 	public String getVariables() {
 		return variables;
 	}
 
 	/**
 	 * Sets all variables, encoded as a string.
 	 * 
 	 * @param variables all variables encoded as a string
 	 */
 	public void setVariables(String variables) {
 		this.variables = variables;
 	}
 
 	/**
 	 * Gets a variable with specified variable name.
 	 * 
 	 * @param name the name of the variable
      * @return the variable, will not be <code>null</code>
 	 */
 	public Variable getVar(String name) {
 		if (!Luntbuild.isEmpty(getVariables())) {
 			BufferedReader reader = new BufferedReader(new StringReader(getVariables()));
 			try {
 				String line;
 				while ((line = reader.readLine()) != null) {
 					if (line.trim().equals(""))
 						continue;
 					String varName = Luntbuild.getAssignmentName(line);
 					String varValue = Luntbuild.getAssignmentValue(line);
 					if (name.trim().equals(varName)) {
 						return new Variable(this, name.trim(), varValue);
 					}
 				}
 			} catch (IOException e) {
 				// ignores
             } finally {
             	if (reader != null) try{reader.close();} catch (Exception e) {}
             }
 		}
 		return new Variable(this, name.trim(), "");
 	}
 
 	/**
 	 * Sets the value of specified variable.
 	 * 
 	 * @param name the name of the variable
 	 * @param var the value to set
 	 * @throws AccessDeniedException if the currently logged in user is not a project admin for this project
 	 */
 	public void setVar(String name, Variable var) {
 		if (!SecurityHelper.isPrjAdministrable(getId()))
 			throw new AccessDeniedException("Permission denied!");
 		if (OgnlHelper.isTestMode())
 			return;
 		String newVariables = "";
 		boolean varFound = false;
 		if (!Luntbuild.isEmpty(getVariables())) {
 			BufferedReader reader = new BufferedReader(new StringReader(getVariables()));
 			try {
 				String line;
 				while ((line = reader.readLine()) != null) {
 					if (line.trim().equals(""))
 						continue;
 					String varName = Luntbuild.getAssignmentName(line);
 					if (name.trim().equals(varName)) {
 						newVariables += name.trim() + "=" + var.getValue() + "\n";
 						varFound = true;
 					} else
 						newVariables += line + "\n";
 				}
 			} catch (IOException e) {
 				// ignores
             } finally {
             	if (reader != null) try{reader.close();} catch (Exception e) {}
             }
 		}
 		if (!varFound)
 			newVariables += name.trim() + "=" + var.getValue() + "\n";
 		setVariables(newVariables);
 		Luntbuild.getDao().saveProject(this);
 	}
 
 	/**
 	 * Gets the builder with the specified name.
 	 * 
 	 * @param name the name of the builder
 	 * @return the builder with the specified name
 	 */
 	public Builder getBuilderByName(String name) {
 		Iterator it = getBuilderList().iterator();
 		while (it.hasNext()) {
 			Builder builder = (Builder) it.next();
 			if (builder.getName().equals(name))
 				return builder;
 		}
 		return null;
 	}
 
 	/**
 	 * Gets the publishing directory of this project, builds generated in this project will be put under this
 	 * directory.
 	 * 
 	 * @return the publishing directory of this project
 	 * @throws RuntimeException if an {@link IOException} happens while finding the publishing directory
 	 */
 	public String getPublishDir() {
 		String publishDir = (String) Luntbuild.getProperties().get("publishDir");
 		if (Luntbuild.isEmpty(publishDir))
 			publishDir = new File(Luntbuild.installDir + "/publish").getAbsolutePath();
 		publishDir = publishDir + File.separator + getName();
 		try {
 			publishDir = new File(publishDir).getCanonicalPath();
 			return publishDir.replaceAll("\\\\", "\\\\\\\\"); // in order to keep back slash for ognl expression evaluation
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 }
