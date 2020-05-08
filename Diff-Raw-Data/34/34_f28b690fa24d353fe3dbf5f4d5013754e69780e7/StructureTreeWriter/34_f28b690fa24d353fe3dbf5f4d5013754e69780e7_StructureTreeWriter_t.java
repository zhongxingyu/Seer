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
 
 import com.flexive.faces.RequestRelativeUriMapper;
 import com.flexive.faces.components.tree.dojo.DojoTreeRenderer;
 import com.flexive.faces.javascript.tree.TreeNodeWriter;
 import com.flexive.faces.javascript.tree.TreeNodeWriter.Node;
 import com.flexive.shared.CacheAdmin;
 import com.flexive.shared.FxSharedUtils;
 import com.flexive.shared.structure.*;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import javax.servlet.http.HttpServletRequest;
 import java.io.IOException;
 import java.io.Serializable;
 import java.io.StringWriter;
 import java.util.*;
 
 /**
  * Renders the structure tree for the current user.
  *
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  */
 public class StructureTreeWriter implements Serializable {
     private static final long serialVersionUID = 7930729560359590725L;
     private static final Log LOG = LogFactory.getLog(StructureTreeWriter.class);
     //doc types
     public static final String DOC_TYPE_GROUP = "Group";
     public static final String DOC_TYPE_TYPE = "Type";
     public static final String DOC_TYPE_TYPE_RELATION = "TypeRelation";
     public static final String DOC_TYPE_ASSIGNMENT = "Assignment";
     public static final String DOC_TYPE_ASSIGNMENT_SYSTEMINTERNAL = "AssignmentSystemInternal";
     public static final String DOC_TYPE_TYPEID = "Type_";
     //node types
     public static final String NODE_TYPE_TYPE="Type";
     public static final String NODE_TYPE_TYPE_RELATION="TypeRelation";
     public static final String NODE_TYPE_ASSIGNMENT="Assignment";
     public static final String NODE_TYPE_ASSIGNMENT_SYSTEMINTERNAL = "AssignmentSystemInternal";
     public static final String NODE_TYPE_GROUP="Group";
 
     /**
      * Render the structure tree to the node writer set in the current request.
      * Called through the JSON-RPC wrapper.
      *
      * @param request the current request
      * @param typeId  the type to be rendered (or -1 for all structures)
      * @param isViewFlat if the tree is rendered flat or hierarchically
     * @param isShowLabels true if labels should be shown, false if the Alias should be shown
      * @return nothing
      */
     public String renderStructureTree(HttpServletRequest request, long typeId, boolean isViewFlat, boolean isShowLabels) {
         StringWriter localWriter = null;
         try {
             // if embedded in a tree component, use the component's tree writer
             TreeNodeWriter writer = (TreeNodeWriter) request.getAttribute(DojoTreeRenderer.PROP_NODEWRITER);
             if (writer == null) {
                 // otherwise return the tree nodes in the response
                 localWriter = new StringWriter();
                 writer = new TreeNodeWriter(localWriter, new RequestRelativeUriMapper(request), TreeNodeWriter.FORMAT_PLAIN);
             }
             writeStructureTree(writer, typeId, isViewFlat, isShowLabels, request);
             if (localWriter != null) {
                 writer.finishResponse();
             }
         } catch (Throwable e) {
             LOG.error("Failed to render structure tree: " + e.getMessage(), e);
         }
         return localWriter != null ? localWriter.toString() : "";
     }
 
     /**
      * Render the structure tree from the given start node (use -1 to print
      * the whole tree.)
      *
      * @param writer  the tree node writer
      * @param typeId  the start type ID (-1 for all types)
      * @param request the current servlet request
      * @param isViewFlat if the tree is rendered flat or hierarchically
      * @param isShowLabels     true if labels should be shown, false if the Alias should be shown
      * @throws IOException if the tree could not be written
      */
     public void writeStructureTree(TreeNodeWriter writer, long typeId, boolean isViewFlat, boolean isShowLabels, HttpServletRequest request) throws IOException {
         final FxEnvironment environment = CacheAdmin.getFilteredEnvironment();
         Map<String, Object> nodeProperties = new HashMap<String, Object>();
         if (typeId == -1) {
             // print whole tree
 
             // print root types
            List<FxType> types = new ArrayList<FxType>();
            // addAll() is used as environment.getTypes returns an unmodifiable list
            types.addAll(environment.getTypes(true, isViewFlat, true, true));
            // if show labels is true, order types by label
            if (isShowLabels)
                Collections.sort(types, new FxSharedUtils.SelectableObjectWithLabelSorter());
            for (FxType type : types) {
                 writeType(writer, nodeProperties, type, isViewFlat, isShowLabels);
             }
             // print root properties
 
             //sort assignments by position
             List<FxPropertyAssignment> systemInternalprops = new ArrayList<FxPropertyAssignment>();
             systemInternalprops.addAll(environment.getSystemInternalRootPropertyAssignments());
             Collections.sort(systemInternalprops, new FxSharedUtils.AssignmentPositionSorter());
 
             for (FxPropertyAssignment property : systemInternalprops) {
                 writePropertyAssignment(writer, nodeProperties, property, isShowLabels);
             }
 
         } else {
             // print chosen node
             writeType(writer, nodeProperties, environment.getType(typeId), isViewFlat, isShowLabels);
         }
     }
 
 
     /**
      * Render a type including its derived types, assigned groups and properties.
      *
      * @param writer         the tree node writer
      * @param type           the type to be rendered
      * @param isViewFlat if the tree is rendered flat or hierarchically
      * @param nodeProperties an existing hashmap for storing additional JS properties (cleared on entry)
      * @param isShowLabels     true if labels should be shown, false if the Alias should be shown
      * @throws IOException if the tree could not be written
      */
 
     private void writeType(TreeNodeWriter writer, Map<String, Object> nodeProperties, FxType type, boolean isViewFlat, boolean isShowLabels) throws IOException {
         nodeProperties.clear();
         nodeProperties.put("typeId", String.valueOf(type.getId()));
 
         final String docType = type.isRelation()
                 ? DOC_TYPE_TYPE_RELATION
                 : type.getIcon() != null && !type.getIcon().isEmpty()
                 ? DOC_TYPE_TYPEID + type.getId()
                 : DOC_TYPE_TYPE;
         nodeProperties.put("alias", type.getName().toUpperCase());
         nodeProperties.put("nodeType", type.isRelation() ? NODE_TYPE_TYPE_RELATION : NODE_TYPE_TYPE);
         writer.startNode(new Node(String.valueOf(type.getId()), isShowLabels ? type.getDisplayName() : (String)nodeProperties.get("alias"), docType, nodeProperties));
         writer.startChildren();
 
         // write derived types
         //if the view is not flat, add derived types as child nodes
         if (!isViewFlat) {
            List<FxType> children = new ArrayList<FxType>();
            // addAll() is used as type.getDerivedTypes returns an unmodifiable list
            children.addAll(type.getDerivedTypes());
             // if show labels is true, order types by label
            if (isShowLabels)
                Collections.sort(children, new FxSharedUtils.SelectableObjectWithLabelSorter());
            for (FxType child : children) {
                 writeType(writer, nodeProperties, child, isViewFlat, isShowLabels);
             }
         }
 
         //sort group and property assignments
         List <FxAssignment> assignments= new ArrayList<FxAssignment>();
         assignments.addAll(type.getAssignedGroups());
         assignments.addAll(type.getAssignedProperties());
         Collections.sort(assignments, new FxSharedUtils.AssignmentPositionSorter());
 
         //write group and property assignments
         for (FxAssignment a : assignments) {
             if (a instanceof FxPropertyAssignment && !a.isSystemInternal())
                 writePropertyAssignment(writer, nodeProperties, (FxPropertyAssignment)a, isShowLabels);
             else if (a instanceof FxGroupAssignment)
                 writeGroupAssignment(writer, nodeProperties, (FxGroupAssignment)a, isShowLabels);
         }
 
         writer.closeChildren();
         writer.closeNode();
     }
 
 
     /**
      * Render an assigned property.
      *
      * @param writer         the tree node writer
      * @param nodeProperties an existing hashmap for storing additional JS properties (cleared on entry)
      * @param assignment       the property to be rendered
      * @param isShowLabels     true if labels should be shown, false if the Alias should be shown
      * @throws IOException if the tree could not be written
      */
     private void writePropertyAssignment(TreeNodeWriter writer, Map<String, Object> nodeProperties, FxPropertyAssignment assignment, boolean isShowLabels) throws IOException {
         nodeProperties.clear();
         nodeProperties.put("alias", assignment.getAlias());
         nodeProperties.put("assignmentId", String.valueOf(assignment.getId()));
         nodeProperties.put("propertyId", String.valueOf(assignment.getProperty().getId()));
         nodeProperties.put("nodeType", assignment.isSystemInternal() ? NODE_TYPE_ASSIGNMENT_SYSTEMINTERNAL : NODE_TYPE_ASSIGNMENT);
         writer.writeNode(new Node(String.valueOf(assignment.getId()), isShowLabels ? assignment.getDisplayName() : (String)nodeProperties.get("alias"),
                 assignment.isSystemInternal() ? DOC_TYPE_ASSIGNMENT_SYSTEMINTERNAL : DOC_TYPE_ASSIGNMENT, nodeProperties));
     }
 
 
     /**
      * Render an assigned group, included assigned subgroups and properties.
      *
      * @param writer         the tree node writer
      * @param nodeProperties an existing hashmap for storing additional JS properties (cleared on entry)
      * @param group          the group to be rendered
      * @param isShowLabels     true if labels should be shown, false if the Alias should be shown
      * @throws IOException if the tree could not be written
      */
 
     private void writeGroupAssignment(TreeNodeWriter writer, Map<String, Object> nodeProperties, FxGroupAssignment group, boolean isShowLabels) throws IOException {
         nodeProperties.clear();
         nodeProperties.put("alias", group.getAlias());
         nodeProperties.put("assignmentId", String.valueOf(group.getId()));
         nodeProperties.put("nodeType",NODE_TYPE_GROUP);
         writer.startNode(new Node(String.valueOf(group.getId()), isShowLabels ? group.getDisplayName() : (String)nodeProperties.get("alias"), DOC_TYPE_GROUP, nodeProperties));
         writer.startChildren();
 
         // sort and write nested groups
         List<FxGroupAssignment> nested = new ArrayList<FxGroupAssignment>();
         nested.addAll(group.getAssignedGroups());
         Collections.sort(nested, new FxSharedUtils.AssignmentPositionSorter());
         for (FxGroupAssignment nestedGroup : nested) {
             writeGroupAssignment(writer, nodeProperties, nestedGroup, isShowLabels);
         }
 
         // sort and write properties
         List<FxPropertyAssignment> props = new ArrayList<FxPropertyAssignment>();
         props.addAll(group.getAssignedProperties());
         Collections.sort(props, new FxSharedUtils.AssignmentPositionSorter());
         for (FxPropertyAssignment property : props) {
             if (!property.isSystemInternal()) {
                 writePropertyAssignment(writer, nodeProperties, property, isShowLabels);
             }
         }
 		// add assigned properties
 		writer.closeChildren();
 		writer.closeNode();
 	}
 }
