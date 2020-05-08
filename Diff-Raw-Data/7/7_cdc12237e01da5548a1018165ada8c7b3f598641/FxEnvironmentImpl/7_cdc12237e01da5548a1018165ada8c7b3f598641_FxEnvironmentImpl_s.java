 /***************************************************************
  *  This file is part of the [fleXive](R) project.
  *
  *  Copyright (c) 1999-2008
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU General Public
  *  License as published by the Free Software Foundation;
  *  either version 2 of the License, or (at your option) any
  *  later version.
  *
  *  The GNU General Public License can be found at
  *  http://www.gnu.org/copyleft/gpl.html.
  *  A copy is found in the textfile GPL.txt and important notices to the
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
 
 import com.flexive.shared.FxArrayUtils;
 import com.flexive.shared.FxContext;
 import com.flexive.shared.XPathElement;
 import com.flexive.shared.exceptions.FxInvalidParameterException;
 import com.flexive.shared.exceptions.FxNotFoundException;
 import com.flexive.shared.exceptions.FxRuntimeException;
 import com.flexive.shared.scripting.FxScriptInfo;
 import com.flexive.shared.scripting.FxScriptMapping;
 import com.flexive.shared.security.ACL;
 import com.flexive.shared.security.Mandator;
 import com.flexive.shared.security.UserTicket;
 import com.flexive.shared.structure.*;
 import com.flexive.shared.workflow.Route;
 import com.flexive.shared.workflow.Step;
 import com.flexive.shared.workflow.StepDefinition;
 import com.flexive.shared.workflow.Workflow;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * Runtime object for environment metadata held in the cache.
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 public final class FxEnvironmentImpl implements FxEnvironment {
     private static final long serialVersionUID = 7107237825721203341L;
 
     private List<FxDataType> dataTypes;
     private List<ACL> acls;
     private List<Workflow> workflows;
     private List<FxSelectList> selectLists;
     private List<FxGroup> groups;
     private List<FxProperty> properties;
     private List<FxPropertyAssignment> propertyAssignmentsEnabled;
     private List<FxPropertyAssignment> propertyAssignmentsAll;
     private List<FxPropertyAssignment> propertyAssignmentsSystemInternalRoot;
     private List<FxGroupAssignment> groupAssignmentsEnabled;
     private List<FxGroupAssignment> groupAssignmentsAll;
     private List<FxType> types;
     private Mandator[] mandators;
     private String inactiveMandators = null;
     private String deactivatedTypes = null;
     private List<FxAssignment> assignments;
     private List<StepDefinition> stepDefinitions;
     private List<Step> steps;
     private List<FxScriptInfo> scripts;
     private List<FxScriptMapping> scriptMappings;
     private long timeStamp = 0;
 
 
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
         this.properties = new ArrayList<FxProperty>(e.properties);
         this.propertyAssignmentsEnabled = new ArrayList<FxPropertyAssignment>(e.propertyAssignmentsEnabled);
         this.propertyAssignmentsAll = new ArrayList<FxPropertyAssignment>(e.propertyAssignmentsAll);
         this.propertyAssignmentsSystemInternalRoot = new ArrayList<FxPropertyAssignment>(e.propertyAssignmentsSystemInternalRoot);
         this.groupAssignmentsEnabled = new ArrayList<FxGroupAssignment>(e.groupAssignmentsEnabled);
         this.groupAssignmentsAll = new ArrayList<FxGroupAssignment>(e.groupAssignmentsAll);
         this.types = new ArrayList<FxType>(e.types);
         this.mandators = new Mandator[e.mandators.length];
         System.arraycopy(e.mandators, 0, this.mandators, 0, mandators.length);
         this.assignments = new ArrayList<FxAssignment>(e.assignments);
         this.stepDefinitions = new ArrayList<StepDefinition>(e.stepDefinitions);
         this.steps = new ArrayList<Step>(e.steps);
         if (e.scripts != null) {
             this.scripts = new ArrayList<FxScriptInfo>(e.scripts);
             this.scriptMappings = new ArrayList<FxScriptMapping>(e.scriptMappings);
         }
         this.selectLists = new ArrayList<FxSelectList>(e.selectLists);
         this.timeStamp = e.timeStamp;
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
         this.mandators = FxArrayUtils.clone(mandators);
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
         this.properties = properties;
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
         this.assignments = assignments;
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
                 if (((FxPropertyAssignment) curr).getProperty().isSystemInternal() && curr.getAssignedType().getId() == 0)
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
     public List<ACL> getACLs() {
         return Collections.unmodifiableList(acls);
     }
 
 
     /**
      * {@inheritDoc}
      */
     public List<ACL> getACLs(ACL.Category category) {
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
     public List<ACL> getACLs(long mandatorId, ACL.Category category, boolean includeForeignAccessible) {
         final UserTicket ticket = FxContext.get().getTicket();
         final List<ACL> result = new ArrayList<ACL>();
         for (ACL acl : acls) {
             if ((acl.getMandatorId() == mandatorId                                          // mandator filter matches
                     || (includeForeignAccessible && ticket.isAssignedToACL(acl.getId())))   // user assigned to mandator-foreign ACL
                     && (category == null || category.equals(acl.getCategory()))) {          // category filter matches
                 result.add(acl);
             }
         }
         return Collections.unmodifiableList(result);
     }
 
     /**
      * {@inheritDoc}
      */
     public ACL getDefaultACL(ACL.Category category) {
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
             return Collections.unmodifiableList(properties);
         ArrayList<FxProperty> result = new ArrayList<FxProperty>(properties.size());
         for (FxProperty prop : properties) {
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
         for (FxProperty property : properties)
             if (property.getId() == id)
                 return property;
         throw new FxNotFoundException("ex.structure.property.notFound.id", id).asRuntimeException();
     }
 
     /**
      * {@inheritDoc}
      */
     public FxProperty getProperty(String name) {
         for (FxProperty property : properties)
             if (property.getName().equalsIgnoreCase(name))
                 return property;
         throw new FxNotFoundException("ex.structure.property.notFound.name", name).asRuntimeException();
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
             try {
                 xPath = XPathElement.toXPathNoMult(xPath);
             } catch (FxInvalidParameterException e) {
                 throw e.asRuntimeException();
             }
             for (FxAssignment as : assignments)
                 if (xPath.equals(as.getXPath()))
                     return as;
         }
         throw new FxNotFoundException("ex.structure.assignment.notFound.xpath", xPath).asRuntimeException();
     }
 
 
     /**
      * {@inheritDoc}
      */
     public FxAssignment getAssignment(long assignmentId) {
         for (FxAssignment as : assignments)
             if (as.getId() == assignmentId)
                 return as;
         throw new FxNotFoundException("ex.structure.assignment.notFound.id", assignmentId).asRuntimeException();
     }
 
     /**
      * {@inheritDoc}
      */
     public List<FxAssignment> getDerivedAssignments(long assignmentId) {
         List<FxAssignment> ret = null;
         for (FxAssignment as : assignments)
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
     public FxType getType(String name) {
         for (FxType type : types)
             if (type.getName().equalsIgnoreCase(name))
                 return type;
         throw new FxNotFoundException("ex.structure.type.notFound.name", name).asRuntimeException();
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
         throw new FxNotFoundException("ex.scripting.notFound", scriptId).asRuntimeException();
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
        throw new FxNotFoundException("ex.scripting.notFound", scriptId).asRuntimeException();
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
             for (FxProperty prop : properties) {
                 ref = false;
                 for (FxAssignment as : this.propertyAssignmentsAll)
                     if (as instanceof FxPropertyAssignment && ((FxPropertyAssignment) as).getProperty().getId() == prop.getId()) {
                         ref = true;
                     }
                 prop.setReferenced(ref);
                 if (prop.getReferencedType() != null) {
                     prop.resolveReferencedType(this);
                 }
             }
         if (groups != null)
             for (FxGroup group : groups) {
                 ref = false;
                 for (FxAssignment as : this.groupAssignmentsAll)
                     if (as instanceof FxGroupAssignment && ((FxGroupAssignment) as).getGroup().getId() == group.getId()) {
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
      * @param scripts       all scripts
      * @param scriptMapping all mappings
      * @throws FxNotFoundException if dependencies can not be resolved
      */
     public void updateScripting(List<FxScriptInfo> scripts, List<FxScriptMapping> scriptMapping) throws FxNotFoundException {
         this.scripts = scripts;
         this.scriptMappings = scriptMapping;
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
     public FxSelectListItem getSelectListItem(long id) {
         for (FxSelectList list : this.selectLists)
             if (list.containsItem(id))
                 return list.getItem(id);
         throw new FxNotFoundException("ex.structure.list.item.notFound", id).asRuntimeException();
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
 }
