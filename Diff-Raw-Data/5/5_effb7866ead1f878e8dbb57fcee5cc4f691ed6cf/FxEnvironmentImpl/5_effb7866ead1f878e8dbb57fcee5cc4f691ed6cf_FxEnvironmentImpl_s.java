 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2010
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.core.structure;
 
 import com.flexive.shared.*;
 import com.flexive.shared.exceptions.FxInvalidParameterException;
 import com.flexive.shared.exceptions.FxNotFoundException;
 import com.flexive.shared.exceptions.FxRuntimeException;
 import com.flexive.shared.media.FxMimeTypeWrapper;
 import com.flexive.shared.media.impl.FxMimeType;
 import com.flexive.shared.scripting.FxScriptInfo;
 import com.flexive.shared.scripting.FxScriptMapping;
 import com.flexive.shared.scripting.FxScriptMappingEntry;
 import com.flexive.shared.scripting.FxScriptSchedule;
 import com.flexive.shared.security.*;
 import com.flexive.shared.structure.*;
 import com.flexive.shared.workflow.Route;
 import com.flexive.shared.workflow.Step;
 import com.flexive.shared.workflow.StepDefinition;
 import com.flexive.shared.workflow.Workflow;
 import com.google.common.collect.Lists;
 
 import java.util.*;
 
 /**
  * Runtime object for environment metadata held in the cache.
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 @SuppressWarnings({"ThrowableInstanceNeverThrown"})
 public final class FxEnvironmentImpl implements FxEnvironment {
     private static final long serialVersionUID = 7107237825721203341L;
 
     private List<FxDataType> dataTypes;
     private List<ACL> acls;
     private List<Workflow> workflows;
     private List<FxSelectList> selectLists;
     private List<FxGroup> groups;
     private Map<Long, FxProperty> properties;
     private Map<String, Long> propertyNameLookup;
     private List<FxPropertyAssignment> propertyAssignmentsEnabled;
     private List<FxPropertyAssignment> propertyAssignmentsAll;
     private List<FxPropertyAssignment> propertyAssignmentsSystemInternalRoot;
     private List<FxGroupAssignment> groupAssignmentsEnabled;
     private List<FxGroupAssignment> groupAssignmentsAll;
     private List<FxType> types;
     private Mandator[] mandators;
     private String inactiveMandators = null;
     private String deactivatedTypes = null;
     private Map<Long, FxAssignment> assignments;
     private Map<String, Long> assignmentXPathLookup;
     private List<StepDefinition> stepDefinitions;
     private List<Step> steps;
     private List<FxScriptInfo> scripts;
     private List<FxScriptMapping> scriptMappings;
     private List<FxScriptSchedule> scriptSchedules;
     private List<FxLanguage> languages;
     private List<UserGroup> userGroups;
     private FxLanguage systemInternalLanguage;
     private long timeStamp = 0;
     //storage-type-level-mapping
     private Map<String, Map<Long, Map<Integer, List<FxFlatStorageMapping>>>> flatMappings;
     private final static List<FxFlatStorageMapping> EMPTY_FLAT_MAPPINGS = Collections.unmodifiableList(new ArrayList<FxFlatStorageMapping>(0));
 
     public FxEnvironmentImpl() {
     }
 
     /**
      * Copy constructor
      *
      * @param e source
      */
     private FxEnvironmentImpl(FxEnvironmentImpl e) {
         this.dataTypes = new ArrayList<FxDataType>(e.dataTypes);
         this.acls = new ArrayList<ACL>(e.acls);
         this.workflows = new ArrayList<Workflow>(e.workflows);
         this.groups = new ArrayList<FxGroup>(e.groups);
         this.properties = new HashMap<Long, FxProperty>(e.properties);
         this.propertyNameLookup = new HashMap<String, Long>(e.propertyNameLookup);
         this.propertyAssignmentsEnabled = new ArrayList<FxPropertyAssignment>(e.propertyAssignmentsEnabled);
         this.propertyAssignmentsAll = new ArrayList<FxPropertyAssignment>(e.propertyAssignmentsAll);
         this.propertyAssignmentsSystemInternalRoot = new ArrayList<FxPropertyAssignment>(e.propertyAssignmentsSystemInternalRoot);
         this.groupAssignmentsEnabled = new ArrayList<FxGroupAssignment>(e.groupAssignmentsEnabled);
         this.groupAssignmentsAll = new ArrayList<FxGroupAssignment>(e.groupAssignmentsAll);
         this.types = new ArrayList<FxType>(e.types);
         this.mandators = new Mandator[e.mandators.length];
         System.arraycopy(e.mandators, 0, this.mandators, 0, mandators.length);
         this.assignments = new HashMap<Long, FxAssignment>(e.assignments);
         this.assignmentXPathLookup = new HashMap<String, Long>(e.assignmentXPathLookup);
         this.stepDefinitions = new ArrayList<StepDefinition>(e.stepDefinitions);
         this.steps = new ArrayList<Step>(e.steps);
         if (e.scripts != null) {
             this.scripts = new ArrayList<FxScriptInfo>(e.scripts);
             this.scriptMappings = new ArrayList<FxScriptMapping>(e.scriptMappings);
             this.scriptSchedules = new ArrayList<FxScriptSchedule>(e.scriptSchedules);
         }
         this.selectLists = new ArrayList<FxSelectList>(e.selectLists);
         this.languages = new ArrayList<FxLanguage>(e.languages);
         this.timeStamp = e.timeStamp;
         this.userGroups = new ArrayList<UserGroup>(e.userGroups);
         this.resolveFlatMappings();
     }
 
     /**
      * Assignment of all known FxDataType
      *
      * @param dataTypes all known data types
      */
     protected void setDataTypes(List<FxDataType> dataTypes) {
         this.dataTypes = dataTypes;
     }
 
     /**
      * Assign all defined ACL's
      *
      * @param acls all defined ALC's
      */
     protected void setAcls(List<ACL> acls) {
         this.acls = acls;
     }
 
     /**
      * Assign all step definitions
      *
      * @param stepDefinitions all step definitions
      */
     protected void setStepDefinitions(List<StepDefinition> stepDefinitions) {
         this.stepDefinitions = stepDefinitions;
     }
 
     /**
      * {@inheritDoc}
      */
     public List<StepDefinition> getStepDefinitions() {
         return Collections.unmodifiableList(stepDefinitions);
     }
 
     /**
      * {@inheritDoc}
      */
     public StepDefinition getStepDefinition(long id) {
         for (StepDefinition sdef : stepDefinitions)
             if (sdef.getId() == id)
                 return sdef;
         throw new FxNotFoundException("ex.stepdefinition.load.notFound", id).asRuntimeException();
     }
 
     /**
      * {@inheritDoc}
      */
     public StepDefinition getStepDefinition(String name) {
         for (StepDefinition sdef : stepDefinitions)
             if (sdef.getName().toUpperCase().equals(name.toUpperCase()))
                 return sdef;
         throw new FxNotFoundException("ex.stepdefinition.load.notFound", name).asRuntimeException();
     }
 
     /**
      * {@inheritDoc}
      */
     public Step getStepByDefinition(long workflowId, long stepDefinitionId) {
         // Find the step
         for (Step step : steps) {
             if (step.getWorkflowId() != workflowId) continue;
             if (step.getStepDefinitionId() != stepDefinitionId) continue;
             return step;
         }
 
         // Step does not exist
         throw new FxNotFoundException("ex.stepdefinition.notFound.id.workflow", stepDefinitionId, workflowId).asRuntimeException();
     }
 
     /**
      * {@inheritDoc}
      */
     public List<Step> getStepsByDefinition(long stepDefinitionId) {
         // Find the step
         ArrayList<Step> list = new ArrayList<Step>();
         for (Step step : steps)
             if (step.getStepDefinitionId() == stepDefinitionId)
                 list.add(step);
         return Collections.unmodifiableList(list);
     }
 
     /**
      * {@inheritDoc}
      */
     public List<Step> getStepsByWorkflow(long workflowId) {
         ArrayList<Step> list = new ArrayList<Step>();
         for (Step step : steps)
             if (step.getWorkflowId() == workflowId)
                 list.add(step);
         return list;
     }
 
     /**
      * {@inheritDoc}
      */
     public Step getStep(long stepId) {
         for (Step step : steps)
             if (step.getId() == stepId)
                 return step;
         throw new FxNotFoundException("ex.step.notFound.id", stepId).asRuntimeException();
     }
 
     /**
      * {@inheritDoc}
      */
     public Step getStep(long workflowId, String stepName) {
         for (Step step : steps)
             if (step.getWorkflowId() == workflowId)
                 if (stepName.equals(getStepDefinition(step.getStepDefinitionId()).getName()))
                     return step;
         throw new FxNotFoundException("ex.step.notFound.name", stepName).asRuntimeException();
     }
 
     /**
      * Assign all steps
      *
      * @param steps all steps
      */
     protected void setSteps(List<Step> steps) {
         this.steps = steps;
     }
 
     /**
      * {@inheritDoc}
      */
     public List<Step> getSteps() {
         return Collections.unmodifiableList(steps);
     }
 
     protected void setWorkflows(List<Workflow> workflows) {
         this.workflows = workflows;
     }
 
     /**
      * Assign all defined mandators
      *
      * @param mandators all defined mandators
      */
     public void setMandators(Mandator[] mandators) {
         this.mandators = mandators.clone();
     }
 
     /**
      * Assign all defined select lists
      *
      * @param lists select lists
      */
     public void setSelectLists(List<FxSelectList> lists) {
         this.selectLists = lists;
     }
 
     /**
      * Assign all defined groups
      *
      * @param groups all defined groups
      */
     protected void setGroups(List<FxGroup> groups) {
         this.groups = groups;
     }
 
     /**
      * Assign all defined properties
      *
      * @param properties all defined properties
      */
     protected void setProperties(List<FxProperty> properties) {
         this.properties = new HashMap<Long, FxProperty>(properties.size());
         this.propertyNameLookup = new HashMap<String, Long>(properties.size());
         for (FxProperty property : properties) {
             this.properties.put(property.getId(), property);
             this.propertyNameLookup.put(
                     toUpperKey(property.getName()),
                     property.getId()
             );
         }
     }
 
     private String toUpperKey(String key) {
         final String upperName = key.toUpperCase();
         return upperName.equals(key) ? key : upperName;
     }
 
     /**
      * Assign all defined types
      *
      * @param fxTypes all defined types
      */
     protected void setTypes(List<FxType> fxTypes) {
         this.types = fxTypes;
     }
 
     /**
      * Assign FxAssignments (mixed groups/properties)
      *
      * @param assignments all assignments (mixed groups/properties)
      */
     protected void setAssignments(List<FxAssignment> assignments) {
         this.assignments = new HashMap<Long, FxAssignment>(assignments.size());
         this.assignmentXPathLookup = new HashMap<String, Long>(assignments.size());
         for (FxAssignment assignment : assignments) {
             this.assignments.put(assignment.getId(), assignment);
             this.assignmentXPathLookup.put(toUpperKey(assignment.getXPath()), assignment.getId());
         }
         if (propertyAssignmentsAll != null)
             propertyAssignmentsAll.clear();
         else
             propertyAssignmentsAll = new ArrayList<FxPropertyAssignment>(assignments.size() / 2);
         if (propertyAssignmentsEnabled != null)
             propertyAssignmentsEnabled.clear();
         else
             propertyAssignmentsEnabled = new ArrayList<FxPropertyAssignment>(assignments.size() / 2);
         if (propertyAssignmentsSystemInternalRoot != null)
             propertyAssignmentsSystemInternalRoot.clear();
         else
             propertyAssignmentsSystemInternalRoot = new ArrayList<FxPropertyAssignment>(25);
         if (groupAssignmentsAll != null)
             groupAssignmentsAll.clear();
         else
             groupAssignmentsAll = new ArrayList<FxGroupAssignment>(assignments.size() / 2);
         if (groupAssignmentsEnabled != null)
             groupAssignmentsEnabled.clear();
         else
             groupAssignmentsEnabled = new ArrayList<FxGroupAssignment>(assignments.size() / 2);
 
         for (FxAssignment curr : assignments) {
             if (curr instanceof FxPropertyAssignment) {
                 propertyAssignmentsAll.add((FxPropertyAssignment) curr);
                 if (curr.isEnabled())
                     propertyAssignmentsEnabled.add((FxPropertyAssignment) curr);
                 if (((FxPropertyAssignment) curr).getProperty().isSystemInternal() && curr.getAssignedTypeId() == 0)
                     propertyAssignmentsSystemInternalRoot.add((FxPropertyAssignment) curr);
             } else if (curr instanceof FxGroupAssignment) {
                 groupAssignmentsAll.add((FxGroupAssignment) curr);
                 if (curr.isEnabled())
                     groupAssignmentsEnabled.add((FxGroupAssignment) curr);
             } else {
 //                LOG.error("Unknown assignment class: " + curr.getClass());
                 //TODO: throw exception
             }
         }
         Collections.sort(propertyAssignmentsSystemInternalRoot);
     }
 
     /**
      * Set scripts
      *
      * @param scripts all scripts
      */
     public void setScripts(List<FxScriptInfo> scripts) {
         this.scripts = scripts;
     }
 
     /**
      * Set script mappings
      *
      * @param scriptMappings all mappings
      */
     public void setScriptMappings(List<FxScriptMapping> scriptMappings) {
         this.scriptMappings = scriptMappings;
     }
 
     /**
      * Set script schedules
      *
      * @param scriptSchedules all script schedules
      */
     public void setScriptSchedules(List<FxScriptSchedule> scriptSchedules) {
         this.scriptSchedules = scriptSchedules;
     }
 
     /**
      * Set the available languages.
      *
      * @param languages the available languages
      * @since 3.1
      */
     public void setLanguages(List<FxLanguage> languages) {
         this.languages = Collections.unmodifiableList(languages);
     }
 
     /**
      * {@inheritDoc}
      */
     public List<FxDataType> getDataTypes() {
         return Collections.unmodifiableList(dataTypes);
     }
 
     /**
      * {@inheritDoc}
      */
     public FxDataType getDataType(long id) {
         for (FxDataType dataType : dataTypes)
             if (dataType.getId() == id)
                 return dataType;
         throw new FxNotFoundException("ex.structure.dataType.notFound.id", id).asRuntimeException();
     }
 
     /**
      * {@inheritDoc}
      */
     public ACL getACL(long id) {
         for (ACL acl : acls)
             if (acl.getId() == id)
                 return acl;
         throw new FxNotFoundException("ex.structure.acl.notFound.id", id).asRuntimeException();
     }
 
 
     /**
      * {@inheritDoc}
      */
     public ACL getACL(String name) {
         for (ACL acl : acls)
             if (acl.getName().equals(name))
                 return acl;
         throw new FxNotFoundException("ex.structure.acl.notFound.name", name).asRuntimeException();
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean aclExists(String name) {
         for (ACL acl : acls)
             if (acl.getName().equals(name))
                 return true;
         return false;
     }
 
     /**
      * {@inheritDoc}
      */
     public List<ACL> getACLs() {
         return Collections.unmodifiableList(acls);
     }
 
 
     /**
      * {@inheritDoc}
      */
     public List<ACL> getACLs(ACLCategory category) {
         List<ACL> result = new ArrayList<ACL>(acls.size());
         for (ACL acl : acls) {
             if (acl.getCategory() == category) {
                 result.add(acl);
             }
         }
         return Collections.unmodifiableList(result);
     }
 
     /**
      * {@inheritDoc}
      */
     public List<ACL> getACLs(long mandatorId) {
         return getACLs(mandatorId, null, true);
     }
 
     /**
      * {@inheritDoc}
      */
     public List<ACL> getACLs(long mandatorId, boolean includeForeignAccessible) {
         return getACLs(mandatorId, null, includeForeignAccessible);
     }
 
     /**
      * {@inheritDoc}
      */
     public List<ACL> getACLs(long mandatorId, ACLCategory category, boolean includeForeignAccessible) {
         final UserTicket ticket = FxContext.getUserTicket();
         final List<ACL> result = new ArrayList<ACL>();
         for (ACL acl : acls) {
             if ((acl.getMandatorId() == mandatorId                                          // mandator filter matches
                     || (includeForeignAccessible && ticket.isAssignedToACL(acl.getId())))   // user assigned to mandator-foreign ACL
                     && (category == null || category.equals(acl.getCategory()))) {          // category filter matches
                 result.add(acl);
             }
         }
         Collections.sort(result, new FxSharedUtils.SelectableObjectWithNameSorter());
         return Collections.unmodifiableList(result);
     }
 
     /**
      * {@inheritDoc}
      */
     public ACL getDefaultACL(ACLCategory category) {
         return getACL(category.getDefaultId());
     }
 
     /**
      * {@inheritDoc}
      */
     public Workflow getWorkflow(long id) {
         for (Workflow wf : workflows)
             if (wf.getId() == id)
                 return wf;
         throw new FxNotFoundException("ex.structure.workflow.notFound.id", id).asRuntimeException();
     }
 
     /**
      * {@inheritDoc}
      */
     public Workflow getWorkflow(String name) {
         for (Workflow wf : workflows)
             if (wf.getName().equals(name))
                 return wf;
         throw new FxNotFoundException("ex.structure.workflow.notFound.id", name).asRuntimeException();
     }
 
     /**
      * {@inheritDoc}
      */
     public List<Workflow> getWorkflows() {
         return Collections.unmodifiableList(workflows);
     }
 
     /**
      * {@inheritDoc}
      */
     public Mandator getMandator(long id) {
         for (Mandator mandator : mandators)
             if (mandator.getId() == id)
                 return mandator;
         throw new FxNotFoundException("ex.structure.mandator.notFound.id", id).asRuntimeException();
     }
 
     /**
      * {@inheritDoc}
      */
     public Mandator getMandator(String name) {
         for (Mandator mandator : mandators) {
             if (mandator.getName().equals(name)) {
                 return mandator;
             }
         }
         throw new FxNotFoundException("ex.structure.mandator.notFound.name", name).asRuntimeException();
     }
 
     /**
      * {@inheritDoc}
      */
     public List<Mandator> getMandators(boolean active, boolean inactive) {
         ArrayList<Mandator> mand = new ArrayList<Mandator>(10);
         for (Mandator mandator : mandators) {
             switch ((mandator.isActive() ? 1 : 0)) {
                 case 1:
                     if (active)
                         mand.add(mandator);
                     break;
                 case 0:
                     if (inactive)
                         mand.add(mandator);
                     break;
 
             }
         }
         return Collections.unmodifiableList(mand);
     }
 
     /**
      * {@inheritDoc}
      */
     public List<FxGroup> getGroups(boolean returnReferenced, boolean returnUnreferenced,
                                    boolean returnRootGroups, boolean returnSubGroups) {
         if (returnReferenced && returnUnreferenced && returnRootGroups && returnSubGroups) {
             return Collections.unmodifiableList(groups);
         }
         ArrayList<FxGroup> result = new ArrayList<FxGroup>(groups.size());
         boolean add;
         boolean foundRoot, foundSub;
         for (FxGroup group : groups) {
             add = returnReferenced && group.isReferenced();
             if (returnUnreferenced && !group.isReferenced())
                 add = true;
             if (returnReferenced && !returnRootGroups && !returnSubGroups)
                 continue;
             if (add && group.isReferenced() && !(returnRootGroups && returnSubGroups)) {
                 //filter either root or sub groups
                 foundRoot = foundSub = false;
                 for (FxGroupAssignment ga : groupAssignmentsAll) {
                     if (ga.getGroup().getId() == group.getId()) {
                         if (ga.getParentGroupAssignment() == null)
                             foundRoot = true;
                         else
                             foundSub = true;
                     }
                 }
                 if (returnRootGroups && !foundRoot)
                     add = false;
                 if (returnSubGroups && !foundSub)
                     add = false;
             }
             if (add)
                 result.add(group);
         }
         return Collections.unmodifiableList(result);
     }
 
     /**
      * {@inheritDoc}
      */
     public FxGroup getGroup(long id) {
         for (FxGroup group : groups)
             if (group.getId() == id)
                 return group;
         throw new FxNotFoundException("ex.structure.group.notFound.id", id).asRuntimeException();
     }
 
     /**
      * {@inheritDoc}
      */
     public FxGroup getGroup(String name) {
         for (FxGroup group : groups)
             if (group.getName().equalsIgnoreCase(name))
                 return group;
         throw new FxNotFoundException("ex.structure.group.notFound.name", name).asRuntimeException();
     }
 
     /**
      * {@inheritDoc}
      */
     public List<FxProperty> getProperties(boolean returnReferenced, boolean returnUnreferenced) {
         if (returnReferenced && returnUnreferenced)
             return Collections.unmodifiableList(Lists.newArrayList(properties.values()));
         ArrayList<FxProperty> result = new ArrayList<FxProperty>(properties.size());
         for (FxProperty prop : properties.values()) {
             if (returnReferenced && prop.isReferenced())
                 result.add(prop);
             if (returnUnreferenced && !prop.isReferenced())
                 result.add(prop);
         }
         return Collections.unmodifiableList(result);
     }
 
     /**
      * {@inheritDoc}
      */
     public FxProperty getProperty(long id) {
         final FxProperty result = properties.get(id);
         if (result != null) {
             return result;
         }
         throw new FxNotFoundException("ex.structure.property.notFound.id", id).asRuntimeException();
     }
 
     /**
      * {@inheritDoc}
      */
     public FxProperty getProperty(String name) {
         final Long id = propertyNameLookup.get(name.toUpperCase());
         if (id != null) {
             return properties.get(id);
         }
         throw new FxNotFoundException("ex.structure.property.notFound.name", name).asRuntimeException();
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean propertyExists(String name) {
         return propertyNameLookup.containsKey(name.toUpperCase());
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean propertyExistsInType(String typeName, String propertyName) {
         if (!typeExists(typeName))
             throw new FxNotFoundException("ex.structure.type.notFound.name", typeName).asRuntimeException();
 
         final long typeId = getType(typeName).getId();
         for (FxPropertyAssignment a : getPropertyAssignments(true)) {
             if (a.getAssignedTypeId() == typeId && !a.isDerivedAssignment()) {
                 if (propertyName.toUpperCase().equals(a.getProperty().getName()))
                     return true;
             }
         }
 
         return false;
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean groupExistsInType(String typeName, String groupName) {
         if (!typeExists(typeName))
             throw new FxNotFoundException("ex.structure.type.notFound.name", typeName).asRuntimeException();
 
         final long typeId = getType(typeName).getId();
         for (FxGroupAssignment a : getGroupAssignments(true)) {
             if (a.getAssignedTypeId() == typeId && !a.isDerivedAssignment()) {
                 if (groupName.toUpperCase().equals(a.getGroup().getName()))
                     return true;
             }
         }
 
         return false;
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean assignmentExists(String xPath) {
         if (xPath != null && xPath.trim().length() > 0) {
            final String xPathUpper = xPath.toUpperCase(Locale.ENGLISH);
             if (!XPathElement.isValidXPath(xPathUpper))
                 return false; //avoid exceptions on malformed xpath's
            xPath = XPathElement.toXPathNoMult(xPathUpper);
             return assignmentXPathLookup.containsKey(xPathUpper);
         }
         return false;
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean groupExists(String name) {
         for (FxGroup check : groups)
             if (check.getName().equalsIgnoreCase(name))
                 return true;
         return false;
     }
 
     /**
      * {@inheritDoc}
      */
     public List<FxPropertyAssignment> getPropertyAssignments() {
         return getPropertyAssignments(false);
     }
 
     /**
      * {@inheritDoc}
      */
     public List<FxPropertyAssignment> getSystemInternalRootPropertyAssignments() {
         /*if (this.propertyAssignmentsSystemInternalRoot == null) {
             System.out.println("Null assignments!");
             new Throwable().printStackTrace();
         }*/
         return Collections.unmodifiableList(propertyAssignmentsSystemInternalRoot);
     }
 
     /**
      * {@inheritDoc}
      */
     public List<FxPropertyAssignment> getPropertyAssignments(boolean includeDisabled) {
         return Collections.unmodifiableList(includeDisabled ? propertyAssignmentsAll : propertyAssignmentsEnabled);
     }
 
     /**
      * {@inheritDoc}
      */
     public List<FxPropertyAssignment> getPropertyAssignments(long propertyId, boolean includeDisabled) {
         final List<FxPropertyAssignment> assignments = includeDisabled ? propertyAssignmentsAll : propertyAssignmentsEnabled;
         final List<FxPropertyAssignment> result = new ArrayList<FxPropertyAssignment>();
         for (FxPropertyAssignment assignment : assignments) {
             if (assignment.getProperty().getId() == propertyId) {
                 result.add(assignment);
             }
         }
         return Collections.unmodifiableList(result);
     }
 
     /**
      * {@inheritDoc}
      */
     public List<FxGroupAssignment> getGroupAssignments() {
         return getGroupAssignments(false);
     }
 
     /**
      * {@inheritDoc}
      */
     public List<FxGroupAssignment> getGroupAssignments(boolean includeDisabled) {
         return Collections.unmodifiableList(includeDisabled ? groupAssignmentsAll : groupAssignmentsEnabled);
     }
 
     /**
      * {@inheritDoc}
      */
     public List<FxGroupAssignment> getGroupAssignments(long groupId, boolean includeDisabled) {
         final List<FxGroupAssignment> assignments = includeDisabled ? groupAssignmentsAll : groupAssignmentsEnabled;
         final List<FxGroupAssignment> result = new ArrayList<FxGroupAssignment>();
         for (FxGroupAssignment assignment : assignments) {
             if (assignment.getGroup().getId() == groupId) {
                 result.add(assignment);
             }
         }
         return Collections.unmodifiableList(result);
     }
 
     /**
      * {@inheritDoc}
      */
     public List<FxType> getTypes() {
         return getTypes(true, true, true, true);
     }
 
     /**
      * {@inheritDoc}
      */
     public List<FxType> getTypes(boolean returnBaseTypes, boolean returnDerivedTypes,
                                  boolean returnTypes, boolean returnRelations) {
         return Collections.unmodifiableList(_getTypes(returnBaseTypes, returnDerivedTypes, returnTypes, returnRelations));
     }
 
     /**
      * {@inheritDoc}
      */
     public List<FxType> getReferencingRelationTypes(long typeId) {
         ArrayList<FxType> relTypes = new ArrayList<FxType>();
         List<FxType> relations = getTypes(true, true, false, true);
         for (FxType t : relations) {
             for (FxTypeRelation r : t.getRelations()) {
                 if (r.getDestination().getId() == typeId || r.getSource().getId() == typeId) {
                     relTypes.add(t);
                     break;
                 }
             }
         }
         return Collections.unmodifiableList(relTypes);
     }
 
     /**
      * Get types depending on selection criteria
      *
      * @param returnBaseTypes    return types that are not derived from another type
      * @param returnDerivedTypes return types that are derived from another type
      * @param returnTypes        return FxTypes
      * @param returnRelations    return FxTypes that are relations
      * @return FxType iterator
      */
     private List<FxType> _getTypes(boolean returnBaseTypes, boolean returnDerivedTypes, boolean returnTypes, boolean returnRelations) {
         if (returnBaseTypes && returnDerivedTypes && returnTypes && returnRelations)
             return this.types;
         ArrayList<FxType> ret = new ArrayList<FxType>(this.types.size());
         for (FxType t : types) {
             if (t.getMode() == TypeMode.Relation && returnRelations) {
                 if (t.getParent() == null && returnBaseTypes)
                     ret.add(t);
                 else if (t.getParent() != null && returnDerivedTypes)
                     ret.add(t);
             } else if (t.getMode() != TypeMode.Relation && returnTypes) {
                 if (t.getParent() == null && returnBaseTypes)
                     ret.add(t);
                 else if (t.getParent() != null && returnDerivedTypes)
                     ret.add(t);
             }
         }
         return ret;
     }
 
     /**
      * {@inheritDoc}
      */
     public FxAssignment getAssignment(String xPath) {
         if (xPath != null && xPath.trim().length() > 0) {
             xPath = XPathElement.toXPathNoMult(xPath);
             // XPath is already in upper case
             final Long id = assignmentXPathLookup.get(xPath);
             if (id != null) {
                 return assignments.get(id);
             }
         }
         throw new FxNotFoundException("ex.structure.assignment.notFound.xpath", xPath).asRuntimeException();
     }
 
     /**
      * {@inheritDoc}
      */
     public FxPropertyAssignment getPropertyAssignment(String xpath) {
         final FxAssignment assignment = getAssignment(xpath);
         try {
             return (FxPropertyAssignment) assignment;
         } catch (ClassCastException e) {
             throw new FxInvalidParameterException(xpath, "ex.structure.assignment.property.type",
                     xpath, assignment.getClass()).asRuntimeException();
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public FxPropertyAssignment getPropertyAssignment(long id) {
         final FxAssignment assignment = getAssignment(id);
         try {
             return (FxPropertyAssignment) assignment;
         } catch (ClassCastException e) {
             throw new FxInvalidParameterException("id", "ex.structure.assignment.property.id",
                     id, assignment.getClass()).asRuntimeException();
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public FxAssignment getAssignment(long assignmentId) {
         final FxAssignment result = assignments.get(assignmentId);
         if (result != null) {
             return result;
         }
         throw new FxNotFoundException("ex.structure.assignment.notFound.id", assignmentId).asRuntimeException();
     }
 
     /**
      * {@inheritDoc}
      */
     public List<FxAssignment> getDerivedAssignments(long assignmentId) {
         List<FxAssignment> ret = null;
         for (FxAssignment as : assignments.values())
             if (as.getBaseAssignmentId() == assignmentId) {
                 if (ret == null)
                     ret = new ArrayList<FxAssignment>(5);
                 ret.add(as);
             }
         if (ret == null)
             ret = new ArrayList<FxAssignment>(0);
         else {
             List<FxAssignment> ret2 = new ArrayList<FxAssignment>(0);
             for (FxAssignment as : ret) {
                 ret2.addAll(getDerivedAssignments(as.getId()));
             }
             ret.addAll(ret2);
         }
         return ret;
     }
 
     /**
      * {@inheritDoc}
      */
     public List<FxPropertyAssignment> getReferencingPropertyAssignments(long propertyId) {
         List<FxPropertyAssignment> assignments = getPropertyAssignments(true);
         List<FxPropertyAssignment> result = new ArrayList<FxPropertyAssignment>();
         for (FxPropertyAssignment assignment : assignments) {
             if (assignment.getProperty().getId() == propertyId && !assignment.isSystemInternal()) {
                 result.add(assignment);
             }
         }
         return result;
     }
 
     /**
      * {@inheritDoc}
      */
     public FxType getType(String name) {
         for (FxType type : types)
             if (type.getName().equalsIgnoreCase(name))
                 return type;
         throw new FxNotFoundException("ex.structure.type.notFound.name", name).asRuntimeException();
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean typeExists(String name) {
         for (FxType type : types)
             if (type.getName().equalsIgnoreCase(name))
                 return true;
         return false;
     }
 
     /**
      * {@inheritDoc}
      */
     public FxType getType(long id) {
         for (FxType type : types)
             if (type.getId() == id)
                 return type;
         throw new FxNotFoundException("ex.structure.type.notFound.id", id).asRuntimeException();
     }
 
     /**
      * {@inheritDoc}
      */
     public List<FxType> getTypesForProperty(long propertyId) {
         List<FxType> ret = new ArrayList<FxType>(10);
         for (FxPropertyAssignment as : propertyAssignmentsAll) {
             if (as.getProperty().getId() != propertyId)
                 continue;
             if (ret.contains(as.getAssignedType()))
                 continue;
             ret.add(as.getAssignedType());
         }
         return Collections.unmodifiableList(ret);
     }
 
     /**
      * {@inheritDoc}
      */
     public Route getRoute(long routeId) {
         for (Workflow workflow : workflows) {
             for (Route route : workflow.getRoutes()) {
                 if (route.getId() == routeId) {
                     return route;
                 }
             }
         }
         throw new FxNotFoundException("ex.structure.route.notFound.id", routeId).asRuntimeException();
     }
 
     /**
      * {@inheritDoc}
      */
     public List<FxScriptInfo> getScripts() {
         return scripts;
     }
 
     /**
      * {@inheritDoc}
      */
     public FxScriptInfo getScript(long scriptId) {
         for (FxScriptInfo si : this.scripts)
             if (si.getId() == scriptId)
                 return si;
         throw new FxNotFoundException("ex.scripting.notFound.id", scriptId).asRuntimeException();
     }
 
     /**
      * {@inheritDoc}
      */
     public FxScriptInfo getScript(String name) {
         for (FxScriptInfo si : this.scripts)
             if (si.getName().equals(name))
                 return si;
         throw new FxNotFoundException("ex.scripting.notFound.id", name).asRuntimeException();
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean scriptExists(String name) {
         for (FxScriptInfo si : this.scripts)
             if (si.getName().equals(name))
                 return true;
         return false;
     }
 
     /**
      * {@inheritDoc}
      */
     public List<FxScriptMapping> getScriptMappings() {
         return scriptMappings;
     }
 
     /**
      * {@inheritDoc}
      */
     public FxScriptMapping getScriptMapping(long scriptId) {
         for (FxScriptMapping mapping : this.scriptMappings)
             if (mapping.getScriptId() == scriptId)
                 return mapping;
         getScript(scriptId); //make sure the script exists
         FxScriptMapping mapping = new FxScriptMapping(scriptId, new ArrayList<FxScriptMappingEntry>(0), new ArrayList<FxScriptMappingEntry>(0));
         scriptMappings.add(mapping);
         return mapping;
     }
 
     /**
      * {@inheritDoc}
      *
      * @since 3.1.2
      */
     public List<FxScriptSchedule> getScriptSchedules() {
         return Collections.unmodifiableList(this.scriptSchedules);
     }
 
     /**
      * {@inheritDoc}
      *
      * @since 3.1.2
      */
     public FxScriptSchedule getScriptSchedule(long scriptScheduleId) {
         for (FxScriptSchedule ss : this.scriptSchedules)
             if (ss.getId() == scriptScheduleId)
                 return ss;
         throw new FxNotFoundException("ex.scripting.schedule.notFound.id", scriptScheduleId).asRuntimeException();
     }
 
     /**
      * {@inheritDoc}
      *
      * @since 3.1.2
      */
     public List<FxScriptSchedule> getScriptSchedulesForScript(long scriptId) {
         List<FxScriptSchedule> result = new ArrayList<FxScriptSchedule>(10);
         for (FxScriptSchedule ss : this.scriptSchedules)
             if (ss.getScriptId() == scriptId)
                 result.add(ss);
         return Collections.unmodifiableList(result);
     }
 
     /**
      * Resolve all missing dependencies
      *
      * @throws FxNotFoundException if a dependency could not be resolved
      */
     protected void resolveDependencies() throws FxNotFoundException {
         for (FxType type : types)
             type.resolveReferences(this);
         //calculate if properties and groups are referenced
         boolean ref;
         if (properties != null)
             for (FxProperty prop : properties.values()) {
                 ref = false;
                 for (FxPropertyAssignment as : this.propertyAssignmentsAll)
                     if (as.getProperty().getId() == prop.getId()) {
                         ref = true;
                     }
                 prop.setReferenced(ref);
             }
         if (groups != null)
             for (FxGroup group : groups) {
                 ref = false;
                 for (FxGroupAssignment as : this.groupAssignmentsAll)
                     if (as.getGroup().getId() == group.getId()) {
                         ref = true;
                     }
                 group.setReferenced(ref);
             }
         for (FxAssignment as : this.propertyAssignmentsAll)
             as.resolveReferences(this);
         for (FxAssignment as : this.groupAssignmentsAll)
             as.resolveReferences(this);
         for (FxSelectList list : this.getSelectLists())
             list._synchronize(this);
         //2nd pass for types (scripting for assignments can only be resolved now)
         for (FxType type : types)
             type.resolveReferences(this);
         this.resolveFlatMappings();
     }
 
     /**
      * resolve flat storage mappings and prepare them by storage and level
      */
     private void resolveFlatMappings() {
         //storage-type-level-mapping
         this.flatMappings = new HashMap<String, Map<Long, Map<Integer, List<FxFlatStorageMapping>>>>(10);
         for (FxPropertyAssignment as : this.propertyAssignmentsAll) {
             if (!as.isFlatStorageEntry())
                 continue;
             FxFlatStorageMapping mapping = as.getFlatStorageMapping();
             if (!flatMappings.containsKey(mapping.getStorage()))
                 flatMappings.put(mapping.getStorage(), new HashMap<Long, Map<Integer, List<FxFlatStorageMapping>>>(10));
             Map<Long, Map<Integer, List<FxFlatStorageMapping>>> typeMap = flatMappings.get(mapping.getStorage());
             final long assignedTypeId = as.getAssignedTypeId();
             if (!typeMap.containsKey(assignedTypeId))
                 typeMap.put(assignedTypeId, new HashMap<Integer, List<FxFlatStorageMapping>>(10));
             Map<Integer, List<FxFlatStorageMapping>> levelMap = typeMap.get(assignedTypeId);
             if (!levelMap.containsKey(mapping.getLevel()))
                 levelMap.put(mapping.getLevel(), new ArrayList<FxFlatStorageMapping>(40));
             List<FxFlatStorageMapping> mapList = levelMap.get(mapping.getLevel());
             mapList.add(mapping);
         }
     }
 
     /**
      * Update or add an existing ACL
      *
      * @param _acl ACL to update/add
      */
     protected void updateACL(ACL _acl) {
         for (int i = 0; i < acls.size(); i++)
             if (acls.get(i).getId() == _acl.getId()) {
                 acls.remove(i);
                 acls.add(_acl);
                 return;
             }
         acls.add(_acl); //add new one
     }
 
     /**
      * Remove an existing ACL
      *
      * @param id ACL to remove
      */
     protected void removeACL(long id) {
         for (int i = 0; i < acls.size(); i++)
             if (acls.get(i).getId() == id) {
                 acls.remove(i);
                 return;
             }
     }
 
     /**
      * Update or add a FxType
      *
      * @param type type to update/add
      * @throws FxNotFoundException on dependency errors
      */
     public void updateType(FxType type) throws FxNotFoundException {
         try {
             FxType org = getType(type.getId());
             types.set(types.indexOf(org), type);
         } catch (FxRuntimeException e) {
             //new type
             types.add(type);
         }
         resolveDependencies();
     }
 
     /**
      * Add a mandator
      *
      * @param mandator mandator
      */
     protected void addMandator(Mandator mandator) {
         mandators = FxArrayUtils.addElement(mandators, mandator, true);
     }
 
     /**
      * Update a mandator, silently fails if the mandator does not exist
      *
      * @param mandator mandator
      */
     public void updateMandator(Mandator mandator) {
         for (int i = 0; i < mandators.length; i++) {
             if (mandators[i].getId() == mandator.getId()) {
                 mandators[i] = mandator;
                 return;
             }
         }
         inactiveMandators = null;
     }
 
     /**
      * Remove a mandator
      *
      * @param mandatorId mandator id to remove
      */
     public void removeMandator(long mandatorId) {
         ArrayList<Mandator> al = new ArrayList<Mandator>(mandators.length - 1);
         for (Mandator mandator : mandators) {
             if (mandator.getId() != mandatorId)
                 al.add(mandator);
         }
         mandators = al.toArray(new Mandator[al.size()]);
     }
 
     /**
      * Update scripts after changes
      *
      * @param scripts         all scripts
      * @param scriptMapping   all mappings
      * @param scriptSchedules scriptSchedules
      * @throws FxNotFoundException if dependencies can not be resolved
      */
     public void updateScripting(List<FxScriptInfo> scripts, List<FxScriptMapping> scriptMapping, List<FxScriptSchedule> scriptSchedules) throws FxNotFoundException {
         this.scripts = scripts;
         this.scriptMappings = scriptMapping;
         this.scriptSchedules = scriptSchedules;
         resolveDependencies();
     }
 
     /**
      * Perform a 'deep' clone (copy) of this instance
      *
      * @return FxEnvironmentImpl
      */
     public FxEnvironmentImpl deepClone() {
         return new FxEnvironmentImpl(this);
     }
 
     /**
      * Update the timestamp of the environment to the current time
      */
     public void updateTimeStamp() {
         this.timeStamp = System.currentTimeMillis();
     }
 
     /**
      * {@inheritDoc}
      */
     public long getTimeStamp() {
         return this.timeStamp;
     }
 
     /**
      * {@inheritDoc}
      */
     public List<FxSelectList> getSelectLists() {
         return Collections.unmodifiableList(this.selectLists);
     }
 
     /**
      * {@inheritDoc}
      */
     public FxSelectList getSelectList(long id) {
         for (FxSelectList list : this.selectLists)
             if (id == list.getId())
                 return list;
         throw new FxNotFoundException("ex.structure.list.notFound", id).asRuntimeException();
     }
 
     /**
      * {@inheritDoc}
      */
     public FxSelectList getSelectList(String name) {
         for (FxSelectList list : this.selectLists)
             if (list.getName().equals(name))
                 return list;
         throw new FxNotFoundException("ex.structure.list.notFound", name).asRuntimeException();
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean selectListExists(String name) {
         for (FxSelectList list : this.selectLists)
             if (list.getName().equals(name))
                 return true;
         return false;
     }
 
     /**
      * {@inheritDoc}
      */
     public FxSelectListItem getSelectListItem(long id) {
         for (FxSelectList list : this.selectLists)
             if (list.containsItem(id))
                 return list.getItem(id);
         throw new FxNotFoundException("ex.structure.list.item.notFound", id).asRuntimeException();
     }
 
     /**
      * {@inheritDoc}
      */
     public FxSelectListItem getSelectListItem(FxSelectList list, String name) {
         if (list.containsItem(name))
             return list.getItem(name);
         throw new FxNotFoundException("ex.structure.list.item.notFound.name", name).asRuntimeException();
     }
 
     /**
      * {@inheritDoc}
      */
     public String getInactiveMandatorList() {
         if (inactiveMandators != null)
             return inactiveMandators;
         StringBuilder sb = new StringBuilder(50);
         for (Mandator m : mandators) {
             if (!m.isActive()) {
                 if (sb.length() > 0)
                     sb.append(',');
                 sb.append(m.getId());
             }
         }
         inactiveMandators = sb.toString();
         return inactiveMandators;
     }
 
     /**
      * {@inheritDoc}
      */
     public String getDeactivatedTypesList() {
         if (deactivatedTypes != null)
             return deactivatedTypes;
         StringBuilder sb = new StringBuilder(50);
         for (FxType t : types)
             if (t.getState() == TypeState.Unavailable) {
                 if (sb.length() > 0)
                     sb.append(',');
                 sb.append(t.getId());
             }
         deactivatedTypes = sb.toString();
         return deactivatedTypes;
     }
 
     /**
      * {@inheritDoc}
      */
     public List<FxFlatStorageMapping> getFlatStorageMappings(String storage, long typeId, int level) {
         try {
             return flatMappings.get(storage).get(typeId).get(level);
         } catch (Exception e) {
             return EMPTY_FLAT_MAPPINGS;
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public FxLanguage getLanguage(long id) {
         if (id == FxLanguage.SYSTEM_ID) {
             return FxLanguage.SYSTEM;
         }
         for (FxLanguage language : languages) {
             if (language.getId() == id) {
                 return language;
             }
         }
         throw new FxNotFoundException("ex.language.notFound.id", id).asRuntimeException();
     }
 
     /**
      * {@inheritDoc}
      */
     public FxLanguage getLanguage(String isoCode) {
         isoCode = isoCode.toLowerCase();
         for (FxLanguage language : languages) {
             if (language.getIso2digit().equals(isoCode)) {
                 return language;
             }
         }
         throw new FxNotFoundException("ex.language.notFound.iso", isoCode).asRuntimeException();
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean isLanguageActive(long id) {
         if (id == FxLanguage.SYSTEM_ID) {
             return true;
         }
         for (FxLanguage language : languages) {
             if (language.getId() == id) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean isLanguageActive(String isoCode) {
         isoCode = isoCode.toLowerCase();
         for (FxLanguage language : languages) {
             if (language.getIso2digit().equals(isoCode)) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * {@inheritDoc}
      */
     public List<FxLanguage> getLanguages() {
         return languages;   // already unmodifiable
     }
 
     /**
      * {@inheritDoc}
      */
     public FxType getMimeTypeMatch(String mimeType) {
         FxMimeType fxMimeType = FxMimeType.getMimeType(mimeType);
         // no proper match for given mimetype - return DocumentFile
         if (FxMimeType.UNKNOWN.equals(fxMimeType.getType()))
             return getType(FxType.DOCUMENTFILE);
 
         List<FxType> allMimeFxTypes = getType(FxType.DOCUMENTFILE).getDerivedTypes();
 
         FxType out = containsMimeType(allMimeFxTypes, fxMimeType, null);
         return out != null ? out : getType(FxType.DOCUMENTFILE);
     }
 
 
     /**
      * Private method to recursively check a list of FxTypes for a given FxMimeType
      * root == null on first call
      *
      * @param typeList the List&lt;FxType&gt;
      * @param mimeType the instance of FxMimeType
      * @param root     the root node
      * @return the FxType or null/the input root node if nothing found
      */
     private FxType containsMimeType(List<FxType> typeList, FxMimeType mimeType, FxType root) {
         FxType derivedMatch = null;
         for (FxType t : typeList) {
             FxMimeTypeWrapper wrapper = t.getMimeType();
             if (wrapper.contains(mimeType, true)) {
                 // assign root FxType for given (main) mime type
                 if (root == null)
                     root = t;
                 // have we got an exact match?
                 if (wrapper.contains(mimeType)) {
                     return t;
                 }
                 // do we have further derived types for the current one?
                 List<FxType> currentDerivedList = t.getDerivedTypes();
 
                 if (currentDerivedList != null && currentDerivedList.size() > 0) {
                     derivedMatch = containsMimeType(currentDerivedList, mimeType, root);
                 }
             }
         }
         return derivedMatch != null ? derivedMatch : root;
     }
 
     /**
      * {@inheritDoc}
      */
     public List<UserGroup> getUserGroups() {
         return userGroups;
     }
 
     /**
      * {@inheritDoc}
      */
     public UserGroup getUserGroup(long id) {
         for (UserGroup grp : userGroups)
             if (grp.getId() == id)
                 return grp;
         return null; //not found
     }
 
     /**
      * {@inheritDoc}
      */
     public UserGroup getUserGroup(String name) {
         for (UserGroup grp : userGroups)
             if (grp.getName().equalsIgnoreCase(name))
                 return grp;
         return null; //not found
     }
 
     /**
      * Set user groups
      *
      * @param userGroups user groups
      * @since 3.1.4
      */
     protected void setUserGroups(List<UserGroup> userGroups) {
         this.userGroups = userGroups;
     }
 }
