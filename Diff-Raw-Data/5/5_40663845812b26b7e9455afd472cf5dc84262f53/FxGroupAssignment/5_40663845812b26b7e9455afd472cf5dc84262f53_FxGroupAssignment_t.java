 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2014
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
 package com.flexive.shared.structure;
 
 import com.flexive.shared.CacheAdmin;
 import com.flexive.shared.EJBLookup;
 import com.flexive.shared.FxContext;
 import com.flexive.shared.XPathElement;
 import com.flexive.shared.content.FxData;
 import com.flexive.shared.content.FxGroupData;
 import com.flexive.shared.exceptions.*;
 import com.flexive.shared.security.UserTicket;
 import com.flexive.shared.value.FxString;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Random;
 
 /**
  * Assignment of a (structure) group to a type or another assignment of a (structure) group
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 public class FxGroupAssignment extends FxAssignment implements Serializable {
 
     private static final long serialVersionUID = 4006780797029441250L;
 
     /**
      * The group assigned
      */
     protected FxGroup group;
 
     /**
      * mode used for children
      */
     protected GroupMode mode;
     private List<FxAssignment> assignments;
     private List<FxPropertyAssignment> propertyAssignments;
     private List<FxGroupAssignment> groupAssignments;
 
     /**
      * Constructor
      *
      * @param assignmentId          internal id of this assignment
      * @param enabled               is this assignment enabled?
      * @param assignedType          the FxType this assignment belongs to
      * @param alias                 an optional alias, if <code>null</code> the original name will be used
      * @param xpath                 XPath relative to the assigned FxType
      * @param position              position within the same XPath hierarchy
      * @param multiplicity          multiplicity
      * @param defaultMultiplicity   default multiplicity
      * @param parentGroupAssignment (optional) parent FxGroupAssignment this group assignment belongs to
      * @param baseAssignment        base assignment (if derived the parent, if not the root assignment, if its a root assignment FxAssignment.ROOT_BASE)
      * @param label                 (optional) label
      * @param hint                  (optional) hint
      * @param group                 the assigned group
      * @param mode                  used group mode (any-of or one-of)
      * @param options               options
      */
     public FxGroupAssignment(long assignmentId, boolean enabled, FxType assignedType, String alias, String xpath, int position,
                              FxMultiplicity multiplicity, int defaultMultiplicity, FxGroupAssignment parentGroupAssignment, long baseAssignment,
                              FxString label, FxString hint, FxGroup group, GroupMode mode, List<FxStructureOption> options) {
         super(assignmentId, enabled, assignedType, alias, xpath, position, multiplicity, defaultMultiplicity, parentGroupAssignment,
                 baseAssignment, label, hint, options);
         this.group = group;
         this.mode = mode;
         this.assignments = new ArrayList<FxAssignment>(10);
         this.propertyAssignments = new ArrayList<FxPropertyAssignment>(5);
         this.groupAssignments = new ArrayList<FxGroupAssignment>(5);
 
         //allow null alias only for creating new groups if only subgroups but not the group itself should be created
         if ((alias == null || alias.trim().length() == 0) && assignmentId >= 0)
             this.alias = (group != null ? group.getName() : null); //catcher if using preload assignment
     }
 
     /**
      * Get the group this assignment relates to
      *
      * @return group this assignment relates to
      */
     public FxGroup getGroup() {
         return group;
     }
 
     /**
      * Get the multiplicity of this assignment.
      * Depending on if the assigned element allows overriding of its base multiplicity the base
      * elements multiplicity is returned or the multiplicity of the assignment
      *
      * @return multiplicity of this assignment
      */
     @Override
     public FxMultiplicity getMultiplicity() {
         return (getGroup().mayOverrideBaseMultiplicity() ? this.multiplicity : getGroup().getMultiplicity());
     }
 
     /**
      * Get all assignments (groups and properties in correct order) that are assigned to this group
      *
      * @return group assignments that are assigned to this assignment
      */
     public List<FxAssignment> getAssignments() {
         return Collections.unmodifiableList(assignments);
     }
 
     /**
      * Get all group assignments that are assigned to this assignment
      *
      * @return group assignments that are assigned to this assignment
      */
     public List<FxGroupAssignment> getAssignedGroups() {
         return Collections.unmodifiableList(groupAssignments);
     }
 
     /**
      * Get all property assignments that are assigned to this assignment
      *
      * @return property assignments that are assigned to this assignment
      */
     public List<FxPropertyAssignment> getAssignedProperties() {
         return Collections.unmodifiableList(propertyAssignments);
     }
 
     /**
      * Return all property assignments that are assigned to this group or any subgroup.
      *
      * @return all property assignments that are assigned to this group or any subgroup.
      */
     public List<FxPropertyAssignment> getAllProperties() {
         if (groupAssignments.isEmpty()) {
             return Collections.unmodifiableList(propertyAssignments);
         }
         final List<FxPropertyAssignment> result = new ArrayList<FxPropertyAssignment>();
         result.addAll(propertyAssignments);
         for (FxGroupAssignment childGroup : groupAssignments) {
             result.addAll(childGroup.getAllProperties());
         }
         return result;
     }
 
     /**
      * Get all assignments of this group and its subgroups
      *
      * @return all assignments of this group and its subgroups
      */
     public List<FxAssignment> getAllChildAssignments() {
         List<FxAssignment> ret = new ArrayList<FxAssignment>(assignments.size() * 5);
         for (FxAssignment as : assignments) {
             ret.add(as);
             if (as instanceof FxGroupAssignment)
                 addGroup(ret, (FxGroupAssignment) as);
         }
         return ret;
     }
 
     /**
      * Recursively add all subgroups to the given list
      *
      * @param ret list of assignments to build
      * @param as  current group processed
      */
     private void addGroup(List<FxAssignment> ret, FxGroupAssignment as) {
         for (FxAssignment sub : as.getAssignments()) {
             ret.add(sub);
             if (sub instanceof FxGroupAssignment)
                 addGroup(ret, (FxGroupAssignment) sub);
         }
     }
 
     /**
      * Add an assignment sorted by position to an ArrayList of assignments
      *
      * @param assignments list of assignments
      * @param as          assignment to add at the correct position in assignments
      */
     private static <T extends FxAssignment> void addSorted(List<T> assignments, T as) {
         synchronized (assignments) {
             if (assignments.contains(as))
                 return;
             for (int i = 0; i < assignments.size(); i++) {
                 if ((assignments).get(i).getPosition() >= as.getPosition()) {
                     assignments.add(i, as);
                     return;
                 }
             }
             assignments.add(as);
         }
     }
 
     /**
      * Add and sort an assignment during initialization phase
      *
      * @param as assignment to add at the correct position
      */
     protected void addAssignment(FxAssignment as) {
         addSorted(assignments, as);
         if (as instanceof FxGroupAssignment)
             addSorted(groupAssignments, (FxGroupAssignment) as);
         if (as instanceof FxPropertyAssignment)
             addSorted(propertyAssignments, (FxPropertyAssignment) as);
     }
 
     /**
      * {@inheritDoc}
      * @see com.flexive.shared.structure.FxGroupAssignment#createEmptyData(com.flexive.shared.content.FxGroupData, int, int)
      */
     @Override
     public FxData createEmptyData(FxGroupData parent, int index) {
         return createEmptyData(parent,  index, this.getPosition());
     }
 
     /**
      * {@inheritDoc}
      */
     @SuppressWarnings({"ThrowableInstanceNeverThrown"})
     @Override
     public FxData createEmptyData(FxGroupData parent, int index, int position, boolean onlySystemInternal, boolean onlyRequiredGroups) {
         FxGroupData thisGroup;
         try {
             final UserTicket ticket = FxContext.get().getRunAsSystem() ? null : FxContext.getUserTicket();
             final FxMultiplicity fxMultiplicity = this.getMultiplicity();
             // if we need to check minMulti then it has to be valid otherwise it has only valid maxMulti
             if (!fxMultiplicity.isValidMax(index))
                 throw new FxCreateException("ex.content.xpath.index.invalid", index, this.getMultiplicity(), this.getXPath()).setAffectedXPath(parent.getXPathFull(), FxContentExceptionCause.InvalidIndex).asRuntimeException();
             final String xPathFull = XPathElement.stripType(parent.getXPathFull() + (parent.isRootGroup() ? "" : "/") + this.getAlias() + "[" + index + "]");
             thisGroup = new FxGroupData(parent.getXPathPrefix(), this.getAlias(), index, this.getXPath(),
                     xPathFull, XPathElement.getIndices(xPathFull),
                     this.getId(), this.getMultiplicity(), position, parent, new ArrayList<FxData>(), this.isSystemInternal());
             final FxType type = getAssignedType();
             if (this.getMode() == GroupMode.OneOf) {
                 //if One-Of more find the first non-optional child and create it (should not happen if correctly setup
                 //but still possible, multiple non-optional childs will result in errors that are thrown in an exception
                 boolean hasRequired = false;
                 for (FxAssignment as : assignments) {
                     if (as instanceof FxPropertyAssignment && type.isUsePropertyPermissions() &&
                             ticket != null && !ticket.mayCreateACL(((FxPropertyAssignment) as).getACL().getId(), ticket.getUserId()))
                         continue;
                     if (onlySystemInternal && !as.isSystemInternal()) 
                         continue;
                    if (onlyRequiredGroups && (as instanceof FxPropertyAssignment || as.getMultiplicity().isOptional()) && !as.isSystemInternal())
                         continue;
                     if (as.getMultiplicity().isRequired()) {
                         if (hasRequired)
                             //noinspection ThrowableInstanceNeverThrown
                             throw new FxCreateException("ex.content.data.create.oneof.multiple",
                                     thisGroup.getXPathFull()).setAffectedXPath(thisGroup.getXPathFull(), FxContentExceptionCause.InvalidGroupMode).asRuntimeException();
                         hasRequired = true;
                         for (int c = 0; c < as.getMultiplicity().getMin(); c++) {
                             if (!onlySystemInternal || as.isSystemInternal()) {
                                 thisGroup.getChildren().add(as.createEmptyData(thisGroup, c + 1));
                             }
                         }
                     }
                 }
             } else { // 'regular' Any-Of group
                 for (FxAssignment as : assignments) {
                     if (as instanceof FxPropertyAssignment && type.isUsePropertyPermissions() &&
                             ticket != null && !ticket.mayCreateACL(((FxPropertyAssignment) as).getACL().getId(), ticket.getUserId()))
                         continue;
                     if (onlySystemInternal && !as.isSystemInternal()) 
                         continue;
                    if (onlyRequiredGroups && (as instanceof FxPropertyAssignment || as.getMultiplicity().isOptional()) && !as.isSystemInternal())
                        continue;
                     if (as.getMultiplicity().isOptional())
                         thisGroup.getChildren().add(as.createEmptyData(thisGroup, 1));
                     else for (int c = 0; c < as.getMultiplicity().getMin(); c++) {
                         thisGroup.getChildren().add(as.createEmptyData(thisGroup, c + 1));
                     }
                 }
             }
             thisGroup.fixChildIndices();
             return thisGroup;
         } catch (FxInvalidParameterException e) {
             throw new FxCreateException(e).asRuntimeException();
         }
     }
 
 
     /**
      * {@inheritDoc}
      */
     @Override
     public FxStructureOption getOption(String key) {
         FxStructureOption gOpt = group.getOption(key);
         if (!gOpt.isSet())
             return super.getOption(key);
         if (!gOpt.isOverridable())
             return gOpt;
         if (super.hasOption(key))
             return super.getOption(key);
         return gOpt;
     }
 
 
     /**
      * {@inheritDoc}
      */
     @Override
     public FxData createRandomData(Random rnd, FxEnvironment env, FxGroupData parent, int index, int maxMultiplicity) {
         FxGroupData thisGroup;
         try {
             final UserTicket ticket = FxContext.getUserTicket();
             thisGroup = new FxGroupData(parent == null ? "" : parent.getXPathPrefix(), this.getAlias(), index, this.getXPath(),
                     XPathElement.stripType(XPathElement.toXPathMult(this.getXPath())), XPathElement.getIndices(getXPath()),
                     this.getId(), this.getMultiplicity(), this.getPosition(), parent, new ArrayList<FxData>(), this.isSystemInternal());
             int count;
             for (FxAssignment as : assignments) {
                 if (!as.isEnabled()
                         || as.isSystemInternal()
                         || (as instanceof FxPropertyAssignment && as.getAssignedType().isUsePropertyPermissions() &&
                         !ticket.mayCreateACL(((FxPropertyAssignment) as).getACL().getId(), ticket.getUserId()))
                         || (as instanceof FxPropertyAssignment && ((FxPropertyAssignment) as).getProperty().getDataType() == FxDataType.Binary)
                         || (as instanceof FxPropertyAssignment && ((FxPropertyAssignment) as).getProperty().getDataType() == FxDataType.Reference))
                     continue;
                 count = as.getMultiplicity().getRandomRange(rnd, maxMultiplicity);
                 for (int i = 0; i < count; i++)
                     thisGroup.getChildren().add(as.createRandomData(rnd, env, thisGroup, i + 1, maxMultiplicity));
             }
 //            thisGroup.fixChildIndices();
             return thisGroup;
         } catch (FxInvalidParameterException e) {
             throw new FxCreateException(e).asRuntimeException();
         }
     }
 
 
     /**
      * Get an assignment for the given (relative to this group) XPath
      *
      * @param XPath     XPathElement array starting at this group
      * @param fullXPath the full XPath for exception reporting
      * @return FxAssignment
      * @throws FxNotFoundException if no assignment was found
      */
     public FxAssignment getAssignment(List<XPathElement> XPath, String fullXPath) throws FxNotFoundException {
         XPathElement curr = XPath.remove(0); //consume 'us'
         if (curr.getAlias().equals(this.getAlias()) && XPath.size() == 0)
             return this; //ok, its us
         if (XPath.size() > 0) {
             curr = XPath.get(0);
             for (FxAssignment as : assignments)
                 if (as.getAlias().equals(curr.getAlias())) {
                     if (as instanceof FxGroupAssignment)
                         return ((FxGroupAssignment) as).getAssignment(XPath, fullXPath);
                     else
                         return as;
                 }
         }
         throw new FxNotFoundException("ex.structure.assignment.notFound.xpath", fullXPath);
     }
 
     /**
      * Get the mode used for this group.
      *
      * @return group mode used
      */
     public GroupMode getMode() {
         return mode;
     }
 
     /**
      * Get this FxGroupAssignment as editable
      *
      * @return FxGroupAssignmentEdit
      */
     public FxGroupAssignmentEdit asEditable() {
         return new FxGroupAssignmentEdit(this);
     }
 
     /**
      * Save this assignment and return the saved instance.
      *
      * @return the saved assignment
      * @throws FxApplicationException on errors
      * @since 3.1
      */
     public FxGroupAssignmentEdit save() throws FxApplicationException {
         final long id = EJBLookup.getAssignmentEngine().save(this, true);
         return ((FxGroupAssignment) CacheAdmin.getEnvironment().getAssignment(id)).asEditable();
     }
 
     /**
      * Return a list of all assignments that were derived from this one (i.e. all assignments of
      * subtypes with the same base assignment ID). An assignment can be inherited at most once in a
      * type, since changing the (unique) alias breaks the inheritance chain.<br />
      * Implementation analogous to method found in FxPropertyAssignment.
      *
      * @param environment the environment
      * @return a list of all derived assignments
      * @since 3.1.1
      */
     public List<FxGroupAssignment> getDerivedAssignments(FxEnvironment environment) {
         final List<FxGroupAssignment> result = new ArrayList<FxGroupAssignment>();
 
         // get the base assignment ID, i.e. the assignment ID that will be used for derived types
         final long baseAssignmentId =
                 baseAssignment != ROOT_BASE
                         ? baseAssignment
                         : getId();
 
         // check for reusage of our assignment
         final List<FxType> types = getAssignedType().getId() == FxType.ROOT_ID
                 // FX-825 - ROOT does not return subtypes, just get all types from the environment
                 ? environment.getTypes()
                 // normal type, just get all subtypes
                 : getAssignedType().getDerivedTypes(true, false);
 
         for (FxType derivedType : types) {
             try {
                 final FxAssignment derivedAssignment = derivedType.getAssignment(getXPath());
                 if (derivedAssignment.isDerivedFrom(environment, baseAssignmentId)) {
                     result.add((FxGroupAssignment) derivedAssignment);
                 }
             } catch (FxRuntimeException e) {
                 // assignment not derived
             }
         }
 
         return result;
     }
 
     /**
      * Check if any (direct) sub assignments of this group are mandatory
      *
      * @return if any (direct) sub assignments of this group are mandatory
      * @since 3.1.3
      */
     public boolean hasMandatorySubAssignments() {
         for (FxPropertyAssignment pa : this.getAssignedProperties())
             if (pa.getMultiplicity().getMin() > 0)
                 return true;
         for (FxGroupAssignment ga : this.getAssignedGroups())
             if (ga.getMultiplicity().getMin() > 0)
                 return true;
         return false;
     }
 
 }
