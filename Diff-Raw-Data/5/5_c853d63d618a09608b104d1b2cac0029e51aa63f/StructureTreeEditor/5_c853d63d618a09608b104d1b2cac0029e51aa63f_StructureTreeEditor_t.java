 /***************************************************************
  *  This file is part of the [fleXive](R) backend application.
  *
  *  Copyright (c) 1999-2008
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) backend application is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU General Public
  *  License as published by the Free Software Foundation;
  *  either version 2 of the License, or (at your option) any
  *  later version.
  *
  *  The GNU General Public License can be found at
  *  http://www.gnu.org/licenses/gpl.html.
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
 package com.flexive.war.javascript.tree;
 
 import com.flexive.shared.CacheAdmin;
 import com.flexive.shared.EJBLookup;
 import com.flexive.shared.value.FxString;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.exceptions.FxInvalidParameterException;
 import com.flexive.shared.exceptions.FxNotFoundException;
 import com.flexive.shared.structure.*;
 import com.flexive.faces.beans.MessageBean;
 import org.apache.commons.lang.StringUtils;
 
 import java.io.Serializable;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Content tree edit actions invoked via JSON/RPC.
  *
  * @author Gerhard Glos (gerhard.glos@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  */
 
 public class StructureTreeEditor implements Serializable {
     private static final long serialVersionUID = -2853036616736591794L;
     private static Pattern aliasPattern = Pattern.compile("[a-zA-Z][a-zA-Z_0-9]*");
 
     /**
      * Remove an assignment, breaking inheritance if needed
      *
      * @param id the assignment id to remove
      * @throws FxApplicationException on errors
      */
     public void deleteAssignment(long id) throws FxApplicationException {
         EJBLookup.getAssignmentEngine().removeAssignment(id);
     }
 
     /**
      * Remove a type
      *
      * @param id id of the type to remove
      * @throws FxApplicationException on errors
      */
     public void deleteType(long id) throws FxApplicationException {
         EJBLookup.getTypeEngine().remove(id);
     }
 
     /**
      * Reuse a property assignment
      *
      * @param orgAssignmentId id of the assignment to reuse
      * @param newName         new name (can be empty, will be used for label if set)
      * @param xPath           XPath
      * @param type            FxType
      * @return FxPropertyAssignmentEdit
      * @throws FxNotFoundException         on errors
      * @throws FxInvalidParameterException on errors
      */
     private FxPropertyAssignmentEdit createReusedPropertyAssignment(long orgAssignmentId, String newName, String xPath, FxType type) throws FxNotFoundException, FxInvalidParameterException {
         FxPropertyAssignment assignment = (FxPropertyAssignment) CacheAdmin.getEnvironment().getAssignment(orgAssignmentId);
         FxPropertyAssignmentEdit prop;
         if (!StringUtils.isEmpty(newName)) {
             prop = FxPropertyAssignmentEdit.createNew(assignment, type, newName == null ? assignment.getAlias() : newName, xPath);
             prop.getLabel().setDefaultTranslation(StringUtils.capitalize(newName));
         } else
             prop = FxPropertyAssignmentEdit.createNew(assignment, type, assignment.getAlias(), xPath);
         return prop;
     }
 
     /**
      * Creates a derived assignment from a given assignment and pastes it into the
      * the target group or type at the first position. A new alias can also be specified.
      *
      * @param parentAssId          the id from which the assignment will be derived
      * @param parentNodeType  the nodeType from which the assignment will be derived (i.e. StructureTreeWriter.NODE_TYPE_GROUP, StructureTreeWriter.NODE_TYPE_ASSIGNMENT)
      * @param targetId       the id of the group or type the assignment will be pasted into.
      * @param targetNodeType the node type of the target (i.e. StructureTreeWriter.NODE_TYPE_GROUP, StructureTreeWriter.NODE_TYPE_TYPE).
      * @param newName        the new alias. if ==null the old will be taken.
      * @return the id of the newly created assignment
      * @throws com.flexive.shared.exceptions.FxApplicationException
      *          on errors
      */
     public long pasteAssignmentInto(long parentAssId, String parentNodeType, long targetId, String targetNodeType, String newName) throws FxApplicationException {
         String targetXPath = "/";
         FxType targetType = null;
         long assignmentId = -1;
 
         if (StructureTreeWriter.NODE_TYPE_GROUP.equals(targetNodeType)) {
             FxGroupAssignment ga = (FxGroupAssignment) CacheAdmin.getEnvironment().getAssignment(targetId);
             targetType = ga.getAssignedType();
             targetXPath = ga.getXPath();
         } else if (StructureTreeWriter.NODE_TYPE_TYPE.equals(targetNodeType) ||
                 StructureTreeWriter.NODE_TYPE_TYPE_RELATION.equals(targetNodeType)) {
             targetType = CacheAdmin.getEnvironment().getType(targetId);
         }
 
         //paste assignment into the target group/type
         if (StructureTreeWriter.NODE_TYPE_ASSIGNMENT.equals(parentNodeType)) {
             assignmentId = EJBLookup.getAssignmentEngine().
                     save(createReusedPropertyAssignment(parentAssId, newName, targetXPath, targetType).setPosition(0), false);
             //move property assignment to first position
             //FxPropertyAssignmentEdit pa = ((FxPropertyAssignment) CacheAdmin.getEnvironment().getAssignment(assignmentId)).asEditable();
             //pa.setPosition(0);
             //EJBLookup.getAssignmentEngine().save(pa, false);
         } else if (StructureTreeWriter.NODE_TYPE_GROUP.equals(parentNodeType)) {
             FxGroupAssignment assignment = (FxGroupAssignment) CacheAdmin.getEnvironment().getAssignment(parentAssId);
             assignmentId = EJBLookup.getAssignmentEngine().save(FxGroupAssignmentEdit.createNew(assignment, targetType, newName == null ? assignment.getAlias() : newName, targetXPath).setPosition(0), true);
             //move group assignment to first position
             //FxGroupAssignmentEdit ga = ((FxGroupAssignment) CacheAdmin.getEnvironment().getAssignment(assignmentId)).asEditable();
             //ga.setPosition(0);
             //EJBLookup.getAssignmentEngine().save(ga, false);
         }
         return assignmentId;
     }
 
     /**
      * Creates a derived assignment from a given assignment and pastes it at
      * a relative position above or below (indicated by steps) a destination assignment.
      * A new alias can also be specified.
      *
      * @param srcId        the id from which the assignment will be derived
      * @param srcNodeType  the nodeType from which the assignment will be derived (i.e. StructureTreeWriter.NODE_TYPE_GROUP, StructureTreeWriter.NODE_TYPE_ASSIGNMENT)
      * @param destId       the id of the destination assignment, where the assignment will be pasted at a relative position
      * @param destNodeType the node type of the destination assignment
      * @param newName      the new alias. if ==null the old will be taken.
      * @param steps        the position relative to the destination assignment, where the derived assignment will be pasted.
      * @return the id of the newly created assignment
      * @throws com.flexive.shared.exceptions.FxApplicationException
      *          on errors
      */
     public long pasteAssignmentRelative(long srcId, String srcNodeType, long destId, String destNodeType, String newName, int steps) throws FxApplicationException {
         String destXPath = "/";
         long assignmentId = -1;
         FxAssignment destAssignment = CacheAdmin.getEnvironment().getAssignment(destId);
         FxType destType = destAssignment.getAssignedType();
 
         //get destination xpath
         if (StructureTreeWriter.NODE_TYPE_GROUP.equals(destNodeType)) {
             destXPath = destAssignment.getXPath();
         } else if (StructureTreeWriter.NODE_TYPE_ASSIGNMENT.equals(destNodeType)) {
             if (destAssignment.hasParentGroupAssignment())
                 destXPath = destAssignment.getParentGroupAssignment().getXPath();
         } else {
             throw new FxInvalidParameterException("nodeType", "ex.structureTreeEditor.nodeType.invalid", destNodeType);
         }
 
         if (StructureTreeWriter.NODE_TYPE_GROUP.equals(srcNodeType)) {
             FxGroupAssignment srcAssignment = (FxGroupAssignment) CacheAdmin.getEnvironment().getAssignment(srcId);
             //create assignment
             FxGroupAssignmentEdit newAssignment = FxGroupAssignmentEdit.createNew(srcAssignment, destType, newName == null ? srcAssignment.getAlias() : newName, destXPath);
             //set position
             newAssignment.setPosition(destAssignment.getPosition() + steps);
             //save newly created assignment to db
             assignmentId = EJBLookup.getAssignmentEngine().save(newAssignment, true);
         } else if (StructureTreeWriter.NODE_TYPE_ASSIGNMENT.equals(srcNodeType)) {
             //create assignment
             FxPropertyAssignmentEdit newAssignment = createReusedPropertyAssignment(srcId, newName, destXPath, destType);
             //set position
             newAssignment.setPosition(destAssignment.getPosition() + steps);
             //save newly created assignment to db
             assignmentId = EJBLookup.getAssignmentEngine().save(newAssignment, false);
         } else {
             throw new FxInvalidParameterException("nodeType", "ex.structureTreeEditor.nodeType.invalid", srcNodeType);
         }
         return assignmentId;
     }
 
     /**
      * Moves a source assignment to a relative position of another destination assignment.
      * The assignments need to be at the same hierarchy level for positioning to work properly.
      * Steps indicates the relative position offset:
      * If steps is -1,-2..n the source assignment will be moved 1,2..n positions before the destination assignment.
      * If steps is 1,2..n the source assignment will be moved 1,2..n positions after the destination assignment.
      *
      * @param srcId       the id of the assignment that shall be moved.
      * @param srcNodeType the node type of the assignment to be moved.
      * @param destId      the id of the destination assignment relative to which the source assignment will be moved.
      * @param steps       relative position offset.
      * @throws com.flexive.shared.exceptions.FxApplicationException
      *          if the node type doesn't match StructureTreeWriter.NODE_TYPE_GROUP or StructureTreeWriter.NODE_TYPE_ASSIGNMENT
      */
     public void moveAssignmentRelative(long srcId, String srcNodeType, long destId, int steps) throws FxApplicationException {
         FxAssignment dest = CacheAdmin.getEnvironment().getAssignment(destId);
         if (StructureTreeWriter.NODE_TYPE_GROUP.equals(srcNodeType)) {
             FxGroupAssignmentEdit src = ((FxGroupAssignment) CacheAdmin.getEnvironment().getAssignment(srcId)).asEditable();
              //if the source position is smaller than the destination position, an offset of -1 needs to be added
             if (src.getPosition() < dest.getPosition())
                 steps=steps-1;
             src.setPosition(dest.getPosition() + steps);
             EJBLookup.getAssignmentEngine().save(src, false);
 
         } else if (StructureTreeWriter.NODE_TYPE_ASSIGNMENT.equals(srcNodeType)) {
             FxPropertyAssignmentEdit src = ((FxPropertyAssignment) CacheAdmin.getEnvironment().getAssignment(srcId)).asEditable();
              //if the source position is smaller than the destination position, an offset of -1 needs to be added
             if (src.getPosition() < dest.getPosition())
                 steps=steps-1;
             src.setPosition(dest.getPosition() + steps);
             EJBLookup.getAssignmentEngine().save(src, false);
         } else
             throw new FxInvalidParameterException("nodeType", "ex.structureTreeEditor.nodeType.invalid", srcNodeType);
     }
 
     public boolean validateAlias(String alias) {
         if (alias != null) {
             Matcher m = aliasPattern.matcher(alias);
             if (m.matches())
                 return true; //all correct
         }
         return false;
     }
 
     /**
      * Compares if two assignments are positioned at the same hierarchy level.
      *
      * @param id1 id of first assignment
      * @param id2 id of second assignment
      * @return true if they have the same parent type, or if parent group assignments exist, true if they have the same parent group assignment
      */
 
     public boolean isSameLevel(long id1, long id2) {
         FxAssignment a1 = CacheAdmin.getEnvironment().getAssignment(id1);
         FxAssignment a2 = CacheAdmin.getEnvironment().getAssignment(id2);
         if (a1.hasParentGroupAssignment() && a2.hasParentGroupAssignment() && a1.getParentGroupAssignment().getId()
                 == a2.getParentGroupAssignment().getId())
             return true;
         else if (!a1.hasParentGroupAssignment() && !a2.hasParentGroupAssignment() && a1.getAssignedType().getId() ==
                 a2.getAssignedType().getId())
             return true;
 
         return false;
     }
 
     /**
      * Checks if an assignment is the direct child of a given type or group.
      *
      * @param assId          id of the assignment
      * @param parentId       id of type or group assignment
      * @param parentNodeType the nodeDocType  (i.e. StructureTreeWriter.NODE_TYPE_GROUP, StructureTreeWriter.NODE_TYPE_TYPE) of the parent
      * @return true if the assignment is a direct child of the type or group
      * @throws FxInvalidParameterException for invalid nodeDocTypes
      * @throws com.flexive.shared.exceptions.FxNotFoundException
      *                                     on errors
      */
 
     public boolean isDirectChild(long assId, long parentId, String parentNodeType) throws FxInvalidParameterException, FxNotFoundException {
         if (StructureTreeWriter.NODE_TYPE_GROUP.equals(parentNodeType)) {
             FxGroupAssignment ga = (FxGroupAssignment) CacheAdmin.getEnvironment().getAssignment(parentId);
             for (FxAssignment a : ga.getAssignments()) {
                 if (a.getId() == assId)
                     return true;
             }
         } else if (StructureTreeWriter.NODE_TYPE_TYPE.equals(parentNodeType) ||
                 StructureTreeWriter.NODE_TYPE_TYPE_RELATION.equals(parentNodeType)) {
             FxType type = CacheAdmin.getEnvironment().getType(parentId);
             for (FxAssignment a : type.getConnectedAssignments("/")) {
                 if (a.getId() == assId)
                     return true;
             }
         } else
             throw new FxInvalidParameterException("nodeType", "ex.structureTreeEditor.nodeType.invalid", parentNodeType);
 
         return false;
     }
 
     /**
      * Returns if an assignment with the specified id is the parent assignment of a child assignment with specified id.
      *
      * @param parent parent assignment id
      * @param child  child assignment id
      * @return true the parent assignment is the parent of the child assignment.
      */
     public boolean isParentAssignment(long parent, long child) {
         FxAssignment a = CacheAdmin.getEnvironment().getAssignment(child);
         while (a.hasParentGroupAssignment()) {
             if (a.getParentGroupAssignment().getId() == parent)
                 return true;
             a = a.getParentGroupAssignment();
         }
         return false;
     }
 
     /**
     * Returns if an FxPropertyAssignment (or its property repsectively) has set OPTION_SEARCHABLE to true
      *
      * @param assId assignment id
      * @return if an assignment is searchable
      */
    public boolean isAssignmentSearchable(long assId) {
         return ((FxPropertyAssignment) CacheAdmin.getFilteredEnvironment().getAssignment(assId)).isSearchable();
     }
 
     /**
      * Returns if a type still. (Used to check after deletion of a type if
      * the content page is still valid or if it displays a structure element
      * that does not exist anymore).
      *
      * @param id    the id of the type
      * @return  if the type with the specified id exists.
      */
     public boolean isTypeExists(long id) {
         try {
             CacheAdmin.getFilteredEnvironment().getType(id);
         }
         catch (Exception e) {
             return false;
         }
         return true;
     }
 
     /**
      * Returns if an assignment exits. (Used to check after deletion if
      * the content page is still valid or or if it displays a structure element
      * that does not exist anymore).
      *
      * @param id    the id of the assignment
      * @return  if the type with the specified id exists.
      */
     public boolean isAssignmentExists(long id) {
         try {
             CacheAdmin.getFilteredEnvironment().getAssignment(id);
         }
         catch (Exception e) {
             return false;
         }
         return true;
     }
 
     /**
      * Creates a derived type with the specified alias of the type with the specified parent
      * id and returns the id of the derived type.
      *
      * @param parentId the id of the parent type
      * @param alias the name of derived type
      * @return the id of the derived type
      */
     public long createDerivedType(long parentId, String alias) throws FxApplicationException {
         FxType parent = CacheAdmin.getEnvironment().getType(parentId);
         FxTypeEdit derived = FxTypeEdit.createNew(alias, new FxString(parent.getLabel().isMultiLanguage(),alias), parent.getACL(), parent);
         derived.setIcon(parent.getIcon());
         return EJBLookup.getTypeEngine().save(derived);
     }
 
     /**
      * Returns the display label for the given Type/Group/Assignment with the specified id.
      *
      * @param id the id
      * @param nodeType the nodeDocType  (i.e. StructureTreeWriter.NODE_TYPE_GROUP, StructureTreeWriter.NODE_TYPE_TYPE,..)
      * @return the display label
      */
     public String getLabel(long id, String nodeType) {
         if  (StructureTreeWriter.NODE_TYPE_ASSIGNMENT.equals(nodeType)
                 || StructureTreeWriter.NODE_TYPE_ASSIGNMENT_SYSTEMINTERNAL.equals(nodeType)
                 || StructureTreeWriter.NODE_TYPE_GROUP.equals(nodeType)) {   
             return CacheAdmin.getEnvironment().getAssignment(id).getDisplayName();
         }
         else return CacheAdmin.getEnvironment().getType(id).getLabel().getBestTranslation();
     }
 
 }
