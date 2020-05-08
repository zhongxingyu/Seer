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
 package com.flexive.shared.structure;
 
 import com.flexive.shared.*;
 import com.flexive.shared.content.FxGroupData;
 import com.flexive.shared.content.FxPK;
 import com.flexive.shared.content.FxPermissionUtils;
 import com.flexive.shared.exceptions.FxCreateException;
 import com.flexive.shared.exceptions.FxInvalidParameterException;
 import com.flexive.shared.exceptions.FxNotFoundException;
 import com.flexive.shared.exceptions.FxRuntimeException;
 import com.flexive.shared.media.FxMimeTypeWrapper;
 import com.flexive.shared.scripting.FxScriptEvent;
 import com.flexive.shared.scripting.FxScriptMapping;
 import com.flexive.shared.scripting.FxScriptMappingEntry;
 import com.flexive.shared.security.ACL;
 import com.flexive.shared.security.ACLCategory;
 import com.flexive.shared.security.LifeCycleInfo;
 import com.flexive.shared.security.UserTicket;
 import com.flexive.shared.value.FxReference;
 import com.flexive.shared.value.FxString;
 import com.flexive.shared.workflow.Workflow;
 import com.google.common.collect.Lists;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 
 import java.io.Serializable;
 import java.util.*;
 
 /**
  * Type definition
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 public class FxType extends AbstractSelectableObjectWithLabel implements Serializable, SelectableObjectWithLabel, SelectableObjectWithName {
     private static final long serialVersionUID = 5598974905776911393L;
 
     /**
      * Root type ID.
      */
     public final static long ROOT_ID = 0;
     /**
      * ROOT type name.
      *
      * @since 3.1
      */
     public static final String ROOT = "ROOT";
     /**
      * Name of the account contact data type
      */
     public static final String CONTACTDATA = "CONTACTDATA";
     /**
      * Name of the folder data type.
      */
     public static final String FOLDER = "FOLDER";
     /**
      * Name of the ROOT document type for storing (binary) file formats
      */
     public static final String DOCUMENTFILE = "DOCUMENTFILE";
     /**
      * Name of the document type for storing typical document file formats (application mime types: PDF, Word, Excel, ...)
      */
     public static final String DOCUMENT = "DOCUMENT";
     /**
      * Name of the image type for storing image file formats (mimetypes image/*)
      *
      * @since 3.1.1
      */
     public static final String IMAGE = "IMAGE";
     /**
      * Name of the audio type for storing audio file formats (mimetypes audio/*)
      *
      * @since 3.1.2
      */
     public static final String AUDIO = "AUDIO";
     /**
      * Name of the video type for storing video file formats (mimetypes video/*)
      *
      * @since 3.1.2
      */
     public static final String VIDEO = "VIDEO";
 
     protected long id;
     protected ACL ACL;
     protected ACL defaultInstanceACL;
     protected Workflow workflow;
     protected String name;
     protected FxString label;
     protected FxType parent;
     protected TypeStorageMode storageMode;
     protected TypeCategory category;
     protected TypeMode mode;
     protected LanguageMode language;
     protected TypeState state;
     protected byte permissions;
     protected boolean trackHistory;
     protected long historyAge;
     protected long maxVersions;
     protected boolean autoVersion;
     protected int maxRelSource;
     protected int maxRelDestination;
     protected boolean multipleContentACLs;
     protected boolean includedInSupertypeQueries;
     protected LifeCycleInfo lifeCycleInfo;
     protected boolean containsFlatStorageAssignments;
     protected List<FxType> derivedTypes;
     protected List<FxTypeRelation> relations;
     protected List<FxPropertyAssignment> assignedProperties;
     protected List<FxProperty> uniqueProperties;
     protected List<FxGroupAssignment> assignedGroups;
     protected List<FxAssignment> scriptedAssignments;
     protected Map<FxScriptEvent, long[]> scriptMapping;
     protected FxReference icon;
     protected List<FxStructureOption> options;
 
 
     /**
      * [fleXive] internal constructor for FxTypes, do not used this outside the flexive core!
      *
      * @param id                         type id
      * @param acl                        type ACL
      * @param defaultInstanceACL         optional default ACL to assign for new instances
      * @param workflow                   the types workflow
      * @param name                       name
      * @param label                      label
      * @param parent                     parent type
      * @param storageMode                storage mode
      * @param category                   type category
      * @param mode                       type mode
      * @param language                   language mode
      * @param state                      type state
      * @param permissions                permissions to use (bit coded)
      * @param multipleContentACLs        does this type support multiple acls for instances?
      * @param includedInSupertypeQueries include this type in super type queries?
      * @param trackHistory               track history?
      * @param historyAge                 max. age of history to keep
      * @param maxVersions                max. number of versions to keep for instances of this type
      * @param autoVersion                automatically create a new version when contents changed during a save operation
      * @param maxRelSource               max. number of relation sources
      * @param maxRelDestination          max. number of relation destination
      * @param lifeCycleInfo              life cycle info for the type
      * @param derivedTypes               list of types derived from this type
      * @param relations                  list of relations this type is affiliated with
      * @param options                    type options
      */
     public FxType(long id, ACL acl, ACL defaultInstanceACL, Workflow workflow, String name, FxString label, FxType parent, TypeStorageMode storageMode,
                   TypeCategory category, TypeMode mode, LanguageMode language, TypeState state, byte permissions,
                   boolean multipleContentACLs, boolean includedInSupertypeQueries, boolean trackHistory,
                   long historyAge, long maxVersions, boolean autoVersion, int maxRelSource, int maxRelDestination, LifeCycleInfo lifeCycleInfo,
                   List<FxType> derivedTypes, List<FxTypeRelation> relations, List<FxStructureOption> options) {
         this.id = id;
         this.ACL = acl;
         this.defaultInstanceACL = defaultInstanceACL;
         this.workflow = workflow;
         this.name = name.toUpperCase();
         this.label = label;
         this.parent = parent;
         this.storageMode = storageMode;
         this.category = category;
         this.mode = mode;
         this.language = language;
         this.state = state;
         this.permissions = permissions;
         this.multipleContentACLs = multipleContentACLs;
         this.includedInSupertypeQueries = includedInSupertypeQueries;
         this.trackHistory = trackHistory;
         this.historyAge = historyAge;
         this.maxVersions = maxVersions;
         this.autoVersion = autoVersion;
         this.maxRelSource = maxRelSource < 0 ? 0 : maxRelSource;
         this.maxRelDestination = maxRelDestination < 0 ? 0 : maxRelDestination;
         this.lifeCycleInfo = lifeCycleInfo;
         this.derivedTypes = derivedTypes;
         this.relations = relations;
         this.containsFlatStorageAssignments = false;
         this.scriptMapping = new LinkedHashMap<FxScriptEvent, long[]>(10);
         this.icon = new FxReference(false, FxReference.EMPTY).setEmpty();
         this.options = options;
         if (this.options == null)
             this.options = FxStructureOption.getEmptyOptionList(2);
     }
 
     /**
      * [fleXive] internal constructor for FxTypes, do not used this outside the flexive core!
      *
      * @param id                         type id
      * @param acl                        type ACL
      * @param workflow                   the types workflow
      * @param name                       name
      * @param label                      label
      * @param parent                     parent type
      * @param storageMode                storage mode
      * @param category                   type category
      * @param mode                       type mode
      * @param language                   language mode
      * @param state                      type state
      * @param permissions                permissions to use (bit coded)
      * @param multipleContentACLs        does this type support multiple acls for instances?
      * @param includedInSupertypeQueries include this type in super type queries?
      * @param trackHistory               track history?
      * @param historyAge                 max. age of history to keep
      * @param maxVersions                max. number of versions to keep for instances of this type
      * @param maxRelSource               max. number of relation sources
      * @param maxRelDestination          max. number of relation destination
      * @param lifeCycleInfo              life cycle info for the type
      * @param derivedTypes               list of types derived from this type
      * @param relations                  list of relations this type is affiliated with
      * @param options                    type options
      * @deprecated since 3.1.1
      */
     public FxType(long id, ACL acl, Workflow workflow, String name, FxString label, FxType parent, TypeStorageMode storageMode,
                   TypeCategory category, TypeMode mode, LanguageMode language, TypeState state, byte permissions,
                   boolean multipleContentACLs, boolean includedInSupertypeQueries, boolean trackHistory,
                   long historyAge, long maxVersions, int maxRelSource, int maxRelDestination, LifeCycleInfo lifeCycleInfo,
                   List<FxType> derivedTypes, List<FxTypeRelation> relations, List<FxStructureOption> options) {
         this(id, acl, null, workflow, name, label, parent, storageMode, category, mode, language, state, permissions,
                 multipleContentACLs, includedInSupertypeQueries, trackHistory, historyAge, maxVersions, false, maxRelSource,
                 maxRelDestination, lifeCycleInfo, derivedTypes, relations, options);
     }
 
     /**
      * Get the category of this FxType (System, User, ...)
      *
      * @return the category.
      */
     public TypeCategory getCategory() {
         return category;
     }
 
     /**
      * Internal id of this FxType
      *
      * @return the internal id of this FxType
      */
     public long getId() {
         return id;
     }
 
     /**
      * Get the ACL of this type
      *
      * @return ACL of this type
      */
     public ACL getACL() {
         return ACL;
     }
 
     /**
      * Is a default instance ACL defined for this type?
      *
      * @return if a default instance ACL is defined for this type
      * @since 3.1.1
      */
     public boolean hasDefaultInstanceACL() {
         return this.defaultInstanceACL != null;
     }
 
     /**
      * Get the default instance ACL for this type.
      * If no default instance ACL is assigned, return the global default instance ACL
      *
      * @return default instance ACL for this type
      * @since 3.1.1
      */
     public ACL getDefaultInstanceACL() {
         if (this.defaultInstanceACL == null)
             return CacheAdmin.getEnvironment().getACL(ACLCategory.INSTANCE.getDefaultId());
         return defaultInstanceACL;
     }
 
     /**
      * Getter for the assigned Workflow
      *
      * @return Workflow
      */
     public Workflow getWorkflow() {
         return workflow;
     }
 
     /**
      * Reload this types workflow, internal method, called from the StructureLoader upon Workflow changes
      *
      * @param environment environment with updated workflows
      */
     public void reloadWorkflow(FxEnvironment environment) {
         this.workflow = environment.getWorkflow(this.getWorkflow().getId());
     }
 
     /**
      * How are languages handled? (None, Single, Multiple, ...)
      *
      * @return how languages are handled
      */
     public LanguageMode getLanguage() {
         return language;
     }
 
     /**
      * Get the state of this type
      *
      * @return TypeState
      */
     public TypeState getState() {
         return state;
     }
 
     /**
      * Is this FxType defining a content or relation?
      *
      * @return mode (Content or Relation)
      */
     public TypeMode getMode() {
         return mode;
     }
 
     /**
      * Get the name of this FxType
      *
      * @return name
      */
     public String getName() {
         return name;
     }
 
     /**
      * Get the description (=label) of this FxType
      *
      * @return description (=label)
      * @deprecated replaced by {@link #getLabel()}
      */
     @Deprecated
     public FxString getDescription() {
         return getLabel();
     }
 
     /**
      * {@inheritDoc}
      */
     public FxString getLabel() {
         return label;
     }
 
     /**
      * Returrn a localized, human-readable name for the type.
      *
      * @return a localized, human-readable name for the type.
      */
     public String getDisplayName() {
         if (label != null && !label.isEmpty()) {
             return label.getBestTranslation();
         } else {
             return name;
         }
     }
 
     /**
      * Is this type a relation?
      *
      * @return if this type is a relation
      */
     public boolean isRelation() {
         return getMode() == TypeMode.Relation;
     }
 
     /**
      * Is this FxType derived from another?
      *
      * @return if this FxType is derived from another
      * @see FxType#getParent()
      */
     public boolean isDerived() {
         return parent != null;
     }
 
     /**
      * Is this FxType derived from the given type?
      *
      * @param typeId the parent type
      * @return true if this type is derived (direct or transitive) from {@code typeId}
      *         or when this type's ID is {@code typeId}
      * @since 3.1
      */
     public boolean isDerivedFrom(long typeId) {
         if (id == typeId || id == FxType.ROOT_ID) {
             return true;
         }
         for (FxType type = parent; type != null; type = type.getParent()) {
             if (type.getId() == typeId) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Is this FxType derived from the given type?
      *
      * @param typeName the type name
      * @return true if this type is derived (direct or transitive) from {@code typeName}
      *         or when the typeName refers to this type
      * @since 3.1
      */
     public boolean isDerivedFrom(String typeName) {
         if (name.equalsIgnoreCase(typeName) || "Root".equalsIgnoreCase(typeName)) {
             return true;
         }
         for (FxType type = parent; type != null; type = type.getParent()) {
             if (type.getName().equalsIgnoreCase(typeName)) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * If this FxType is derived from another FxType get the 'super' FxType
      *
      * @return FxType this one is derived from or <code>null</code>
      */
     public FxType getParent() {
         return parent;
     }
 
     /**
      * Get all FxTypes that are derived from this Type
      *
      * @return Iterator of all directly derived types
      */
     public List<FxType> getDerivedTypes() {
         return Collections.unmodifiableList(derivedTypes);
     }
 
     /**
      * Get all FxTypes that are derived from this type.
      *
      * @param transitive     if transitive dependencies (derived types of derived types) should be resolved
      * @param includeOwnType if the own type should be included (as first element)
      * @return all FxTypes that are derived from this type.
      * @since 3.1
      */
     public List<FxType> getDerivedTypes(boolean transitive, boolean includeOwnType) {
         final List<FxType> result = new ArrayList<FxType>();
         if (includeOwnType) {
             result.add(this);
         }
         if (transitive) {
             addDerivedTypes(result);
         } else {
             result.addAll(derivedTypes);
         }
         return result;
     }
 
     /**
      * Add the derived types (direct and through transitive dependency) of this type to the result list.
      *
      * @param result the result list to be populated
      * @since 3.1
      */
     protected void addDerivedTypes(Collection<FxType> result) {
         for (FxType type : derivedTypes) {
             result.add(type);
             type.addDerivedTypes(result);
         }
     }
 
     /**
      * Get how is data stored internally.
      *
      * @return how data is stored internally
      */
     public TypeStorageMode getStorageMode() {
         return storageMode;
     }
 
     /**
      * Use permissions at all?
      *
      * @return if permissions are used at all
      * @deprecated will be removed in 3.2
      */
     public boolean usePermissions() {
         return isUsePermissions();
     }
 
     /**
      * Use content instance permissions?
      *
      * @return if content instance permissions are used
      * @deprecated will be removed in 3.2
      */
     public boolean useInstancePermissions() {
         return isUseInstancePermissions();
     }
 
     /**
      * Use property permissions?
      *
      * @return if property permissions are used
      * @deprecated will be removed in 3.2
      */
     public boolean usePropertyPermissions() {
         return isUsePropertyPermissions();
     }
 
     /**
      * Use step permissions?
      *
      * @return if step permissions are used
      * @deprecated will be removed in 3.2
      */
     public boolean useStepPermissions() {
         return isUseStepPermissions();
     }
 
     /**
      * Use type permissions?
      *
      * @return if type permissions are used
      * @deprecated will be removed in 3.2
      */
     public boolean useTypePermissions() {
         return isUseTypePermissions();
     }
 
     /**
      * Use permissions at all?
      *
      * @return if permissions are used at all
      * @since 3.1
      */
     public boolean isUsePermissions() {
         return permissions != 0;
     }
 
     /**
      * Use content instance permissions?
      *
      * @return if content instance permissions are used
      * @since 3.1
      */
     public boolean isUseInstancePermissions() {
         return (permissions & FxPermissionUtils.PERM_MASK_INSTANCE) == FxPermissionUtils.PERM_MASK_INSTANCE;
     }
 
     /**
      * Use property permissions?
      *
      * @return if property permissions are used
      * @since 3.1
      */
     public boolean isUsePropertyPermissions() {
         return (permissions & FxPermissionUtils.PERM_MASK_PROPERTY) == FxPermissionUtils.PERM_MASK_PROPERTY;
     }
 
     /**
      * Use step permissions?
      *
      * @return if step permissions are used
      * @since 3.1
      */
     public boolean isUseStepPermissions() {
         return (permissions & FxPermissionUtils.PERM_MASK_STEP) == FxPermissionUtils.PERM_MASK_STEP;
     }
 
     /**
      * Use type permissions?
      *
      * @return if type permissions are used
      * @since 3.1
      */
     public boolean isUseTypePermissions() {
         return (permissions & FxPermissionUtils.PERM_MASK_TYPE) == FxPermissionUtils.PERM_MASK_TYPE;
     }
 
     /**
      * Allow multiple ACLs for a content of this type?
      *
      * @return true if multiple content ACLs are allowed
      * @since 3.1
      */
     public boolean isMultipleContentACLs() {
         return multipleContentACLs;
     }
 
     /**
      * Should this type be included in supertype queries?
      *
      * @return true if this type be included in supertype queries
      * @since 3.1
      */
     public boolean isIncludedInSupertypeQueries() {
         return includedInSupertypeQueries;
     }
 
     /**
      * Track history of changes?
      *
      * @return if history of changes is tracked
      */
     public boolean isTrackHistory() {
         return trackHistory;
     }
 
     /**
      * Get how many days history is tracked (0 = forever)
      *
      * @return how many days history is tracked (0 = forever)
      */
     public long getHistoryAge() {
         return historyAge;
     }
 
     /**
      * Get how many versions of instances are kept (-1 = infinite, 0 = none)
      *
      * @return how many versions of instances are kept (-1 = infinite, 0 = none)
      */
     public long getMaxVersions() {
         return maxVersions;
     }
 
     /**
      * Should new versions be automatically created when data changed?
      *
      * @return automatically create a new version if data changed
      */
     public boolean isAutoVersion() {
         return autoVersion;
     }
 
     /**
      * How many source instances may be related to this instance in total? (infinte = <0)
      *
      * @return how many source instances may be related to this instance in total? (infinte = <0)
      */
     public int getMaxRelSource() {
         return maxRelSource;
     }
 
     /**
      * How many destination instances may be related to this instance in total? (infinte = <0)
      *
      * @return how many destination instances may be related to this instance in total? (infinte = <0)
      */
     public int getMaxRelDestination() {
         return maxRelDestination;
     }
 
     /**
      * Get information about changes
      *
      * @return information about changes
      */
     public LifeCycleInfo getLifeCycleInfo() {
         return lifeCycleInfo;
     }
 
     /**
      * Get all group assignments that are attached to the type's root
      *
      * @return all group assignments that are attached to the type's root
      */
     public List<FxGroupAssignment> getAssignedGroups() {
         return Collections.unmodifiableList(assignedGroups);
     }
 
     /**
      * Get all property assignments that are attached to the type's root
      *
      * @return all property assignments that are attached to the type's root
      */
     public List<FxPropertyAssignment> getAssignedProperties() {
         return Collections.unmodifiableList(assignedProperties);
     }
 
     /**
      * Get all property assignments that are attached to the type's root or to a group
      * attached to the type.
      *
      * @return all property assignments for the type
      * @since 3.1
      */
     public List<FxPropertyAssignment> getAllProperties() {
         final List<FxPropertyAssignment> result = new ArrayList<FxPropertyAssignment>();
         result.addAll(assignedProperties);
         for (FxGroupAssignment groupAssignment : assignedGroups) {
             result.addAll(groupAssignment.getAllProperties());
         }
         return result;
     }
 
     /**
      * Get all assignments (groups and properties) that are attached to a type
      *
      * @return all assignments for the type
      */
     public List<FxAssignment> getAllAssignments() {
         final List<FxAssignment> result = new ArrayList<FxAssignment>(assignedProperties.size() + assignedGroups.size() * 5);
         result.addAll(assignedProperties);
         result.addAll(assignedGroups);
         result.addAll(recGroupAssignmentSearch(assignedGroups));
         return result;
     }
 
     /**
      * Recursive search through a list of GroupAssignments, returns all child assignments
      *
      * @param groupAssignments the List of FxGroupAssignments
      * @return returns a list of FxAssignments
      */
     private static List<FxAssignment> recGroupAssignmentSearch(List<FxGroupAssignment> groupAssignments) {
         if (groupAssignments.size() == 0)
             return new ArrayList<FxAssignment>(0);
 
         final List<FxAssignment> out = new ArrayList<FxAssignment>(groupAssignments.size() * 5); //  = new ArrayList<FxAssignment>(ga.getAllChildAssignments().size());
         for (FxGroupAssignment ga : groupAssignments) {
             out.addAll(ga.getAllChildAssignments());
         }
         return out;
     }
 
     /**
      * Do unique properties for this type exist?
      *
      * @return if unique properties for this type exist
      */
     public boolean hasUniqueProperties() {
         return uniqueProperties.size() > 0;
     }
 
     /**
      * Get all properties used in this type that have a unique constraint set
      *
      * @return all properties used in this type that have a unique constraint set
      */
     public List<FxProperty> getUniqueProperties() {
         return Collections.unmodifiableList(uniqueProperties);
     }
 
     /**
      * Get all possible relation combinations
      *
      * @return possible relation combinations
      */
     public List<FxTypeRelation> getRelations() {
         return Collections.unmodifiableList(relations);
     }
 
     /**
      * Does this type have mappings for the requested script event  type?
      *
      * @param event requested script event type
      * @return if mappings exist
      */
     public boolean hasScriptMapping(FxScriptEvent event) {
         return scriptMapping.get(event) != null;
     }
 
     /**
      * Do scripted assignments exists for this type?
      *
      * @return if scripted assignments exist for this type
      */
     public boolean hasScriptedAssignments() {
         return scriptedAssignments != null && scriptedAssignments.size() > 0;
     }
 
     /**
      * Get a list with all assignments that have scripts assigned for the given script type
      *
      * @param event script event
      * @return list with all assignments that have scripts assigned for the given script type
      */
     public synchronized List<FxAssignment> getScriptedAssignments(FxScriptEvent event) {
         List<FxAssignment> scripts = new ArrayList<FxAssignment>(5);
         if (!hasScriptedAssignments())
             return scripts;
         for (FxAssignment a : scriptedAssignments)
             if (a.getScriptMapping(event) != null && !scripts.contains(a))
                 scripts.add(a);
         return scripts;
     }
 
     /**
      * Get the script id's that are mapped to this type for the requested script type
      *
      * @param event requested script event
      * @return mappings or <code>null</code> if mapping does not exist for this type
      */
     public long[] getScriptMapping(FxScriptEvent event) {
         return scriptMapping.get(event);
     }
 
     /**
      * Get a Set of all events that have script mappings for this type
      *
      * @return Set of all events that have script mappings for this type
      */
     public Set<FxScriptEvent> getScriptEvents() {
         return scriptMapping.keySet();
     }
 
     /**
      * Get the permissions set for this type bit coded
      *
      * @return bit coded permissions
      */
     public byte getBitCodedPermissions() {
         return permissions;
     }
 
     /**
      * Get the preview icon of this type.
      * The icon can be empty if not defined.
      * Version is ignored and always the max version.
      *
      * @return preview icon (can be empty)
      */
     public FxReference getIcon() {
         return icon;
     }
 
     /**
      * Resolve references after initial loading
      *
      * @param fxStructure structure for references
      * @throws FxNotFoundException on errors
      */
     public void resolveReferences(FxEnvironment fxStructure) throws FxNotFoundException {
         //we only resolve if the parent is a preload type! => this can and will only happen once
         if (getParent() != null && getParent().getMode() == TypeMode.Preload) {
             //assign correct parents
 //            if (getParent().getId() == 0)
 //                this.parent = null;
 //            else {
             this.parent = fxStructure.getType(getParent().getId());
             if (!this.parent.derivedTypes.contains(this))
                 this.parent.derivedTypes.add(this);
 //            }
             //resolve derived types
             derivedTypes.clear();
             for (FxType derived : fxStructure.getTypes(true, true, true, true)) {
                 if (derived.getParent() != null && derived.getParent().getId() == getId())
                     derivedTypes.add(derived);
             }
             if (relations.size() > 0) {
                 for (FxTypeRelation relation : relations)
                     relation.resolveReferences(fxStructure);
             }
         }
         if (assignedProperties == null)
             assignedProperties = new ArrayList<FxPropertyAssignment>(10);
         else
             assignedProperties.clear();
         if (uniqueProperties == null)
             uniqueProperties = new ArrayList<FxProperty>(10);
         else
             uniqueProperties.clear();
         if (scriptedAssignments == null)
             scriptedAssignments = new ArrayList<FxAssignment>(10);
         else
             scriptedAssignments.clear();
         for (FxPropertyAssignment fxpa : fxStructure.getPropertyAssignments(true)) {
             if (fxpa.getAssignedTypeId() != this.getId())
                 continue;
             if (fxpa.hasScriptMappings() && !scriptedAssignments.contains(fxpa))
                 scriptedAssignments.add(fxpa);
             if (!containsFlatStorageAssignments && fxpa.isFlatStorageEntry())
                 containsFlatStorageAssignments = true;
             if (!fxpa.hasParentGroupAssignment())
                 assignedProperties.add(fxpa);
             if (fxpa.getProperty().getUniqueMode() != UniqueMode.None && !uniqueProperties.contains(fxpa.getProperty()))
                 uniqueProperties.add(fxpa.getProperty());
 
         }
         if (assignedGroups == null)
             assignedGroups = new ArrayList<FxGroupAssignment>(10);
         else
             assignedGroups.clear();
         for (FxGroupAssignment fxga : fxStructure.getGroupAssignments(true)) {
             if (fxga.getAssignedTypeId() != this.getId())
                 continue;
             if (fxga.hasScriptMappings() && !scriptedAssignments.contains(fxga))
                 scriptedAssignments.add(fxga);
             if (!fxga.hasParentGroupAssignment())
                 assignedGroups.add(fxga);
         }
 
         //calculate used script mappings for this type
         this.scriptMapping.clear();
         for (FxScriptMapping sm : fxStructure.getScriptMappings()) {
             for (FxScriptMappingEntry sme : sm.getMappedTypes()) {
                 if (!sme.isActive())
                     continue;
                 if (sme.getId() == this.getId()) {
                     addScriptMapping(this.scriptMapping, sme.getScriptEvent(), sm.getScriptId());
                 } else if (sme.isDerivedUsage()) {
                     for (long l : sme.getDerivedIds())
                         if (l == this.getId())
                             addScriptMapping(this.scriptMapping, sme.getScriptEvent(), sm.getScriptId());
 
                 }
             }
         }
     }
 
     private synchronized void addScriptMapping(Map<FxScriptEvent, long[]> scriptMapping, FxScriptEvent scriptEvent, long scriptId) {
         if (scriptMapping.get(scriptEvent) == null)
             scriptMapping.put(scriptEvent, new long[]{scriptId});
         else {
             long[] scripts = scriptMapping.get(scriptEvent);
             if (ArrayUtils.contains(scripts, scriptId))
                 return;
             long[] new_scripts = new long[scripts.length + 1];
             System.arraycopy(scripts, 0, new_scripts, 0, scripts.length);
             new_scripts[new_scripts.length - 1] = scriptId;
             scriptMapping.put(scriptEvent, new_scripts);
         }
     }
 
     /**
      * Create an empty FxData hierarchy for a new FxContent starting with a
      * virtual root group.
      *
      * @param xpPrefix XPath prefix like "FxType name[@pk=..]"
      * @return empty FxData hierarchy
      */
     @SuppressWarnings({"ThrowableInstanceNeverThrown"})
     public FxGroupData createEmptyData(String xpPrefix) {
         FxGroupData base;
         try {
             base = FxGroupData.createVirtualRootGroup(xpPrefix);
         } catch (FxInvalidParameterException e) {
             throw new FxCreateException(e).asRuntimeException();
         }
         final UserTicket ticket = FxContext.getUserTicket();
         int c;
 
         // Store the actually beginposition of each position including the multiplicities of previous elemnts
         // key : position ; value : "real" position
         Map<Integer, Integer> posDeltas = new HashMap<Integer, Integer>();
 
         // key : position; value : the amount of elements of the current position
         Map<Integer, Integer> posMultis = new HashMap<Integer, Integer>();
         int curBeginPos;
 
         // First : collect the information about all the multiplicities in the root group (of properties + groups)
         for (FxPropertyAssignment fxpa : assignedProperties) {
             if (!fxpa.isEnabled() || (isUsePropertyPermissions() && !ticket.mayCreateACL(fxpa.getACL().getId(), ticket.getUserId()))) {
                 continue;
             }
             posMultis.put(fxpa.getPosition(), fxpa.getDefaultMultiplicity());
         }
         for (FxGroupAssignment fxga : assignedGroups) {
             if (!fxga.isEnabled())
                 continue;
             posMultis.put(fxga.getPosition(), fxga.getDefaultMultiplicity());
         }
         // after this get a sorted list containing the positions (collected)
         List<Integer> tmpList = new ArrayList<Integer>();
         tmpList.addAll(posMultis.keySet());
         Integer [] sortedKeys = new Integer[tmpList.size()];
         tmpList.toArray(sortedKeys);
         Arrays.sort(sortedKeys);
         int nextFreePos = 0;
 /*
          now calculate the "real" positions of each element according to the multiplicity
          if the position of an element (in the type) is bigger then the calculated position, the original (bigger) is taken
          so if there is a type defining P1 on pos 20 with min./default Multi of 30 and P2 (the next) on pos 70, p2 will start on pos 70 
 */
         for (int key : sortedKeys) {
             int multi = posMultis.get(key);
             nextFreePos = Math.max(key, nextFreePos);
             posDeltas.put(key, nextFreePos);
             nextFreePos += multi;
         }
 
         // no we could add all the properties and groups because we already "know" on which position will which element start
         // IMPORTANT : if changing the inner loop from defaultMulti to "more" don't forget to change it at the
         // beginning of this method (at collecting)
         for (FxPropertyAssignment fxpa : assignedProperties) {
             if (!fxpa.isEnabled() || (isUsePropertyPermissions() && !ticket.mayCreateACL(fxpa.getACL().getId(), ticket.getUserId()))) {
                 continue;
             }
             /*if (fxpa.getMultiplicity().isOptional())
                 base.addChild(fxpa.createEmptyData(base, 1));
             else*/
             // set the beginPos to the first pos (calculated) for the given assignment
             curBeginPos = posDeltas.get(fxpa.getPosition());
             for (c = 0; c < fxpa.getDefaultMultiplicity(); c++)
                 base.addChild(fxpa.createEmptyData(base, c + 1, curBeginPos + c));
         }
 
         for (FxGroupAssignment fxga : assignedGroups) {
             if (!fxga.isEnabled())
                 continue;
             /*if (fxga.getMultiplicity().isOptional())
                 base.addChild(fxga.createEmptyData(base, 1));
             else*/
             // set the beginPos to the first pos (calculated) for the given group
             curBeginPos = posDeltas.get(fxga.getPosition());
             for (c = 0; c < fxga.getDefaultMultiplicity(); c++)
                 base.addChild(fxga.createEmptyData(base, c + 1, curBeginPos + c));
         }
         // for elements of "inner groups" we don't have to calculate the real position it is done by the fixChildIndices
         return base;
     }
 
     /**
      * Create a base group with random data
      *
      * @param pk              primary key of instance that uses this random data
      * @param env             environment
      * @param rnd             Random to use
      * @param maxMultiplicity the maximum multiplicity for groups
      * @return random data
      */
     @SuppressWarnings({"ThrowableInstanceNeverThrown"})
     public FxGroupData createRandomData(FxPK pk, FxEnvironment env, Random rnd, int maxMultiplicity) {
         FxGroupData base;
         try {
             base = FxGroupData.createVirtualRootGroup(buildXPathPrefix(pk));
         } catch (FxInvalidParameterException e) {
             throw new FxCreateException(e).asRuntimeException();
         }
         int count;
         for (FxPropertyAssignment fxpa : assignedProperties) {
             if (!fxpa.isEnabled()
                     || fxpa.isSystemInternal()
                     || fxpa.getProperty().getDataType() == FxDataType.Binary
                     || fxpa.getProperty().getDataType() == FxDataType.Reference)
                 continue;
             count = fxpa.getMultiplicity().getRandomRange(rnd, maxMultiplicity);
             for (int i = 0; i < count; i++)
                 base.getChildren().add(fxpa.createRandomData(rnd, env, base, i + 1, maxMultiplicity));
         }
 
         for (FxGroupAssignment fxga : assignedGroups) {
             if (!fxga.isEnabled())
                 continue;
             count = fxga.getMultiplicity().getRandomRange(rnd, maxMultiplicity);
             for (int i = 0; i < count; i++)
                 base.getChildren().add(fxga.createRandomData(rnd, env, base, i + 1, maxMultiplicity));
         }
         return base;
     }
 
     /**
      * Get the assignment for the given XPath
      *
      * @param parentXPath desired XPath
      * @return FxAssignment
      */
     @SuppressWarnings({"ThrowableInstanceNeverThrown"})
     public FxAssignment getAssignment(String parentXPath) {
         if (StringUtils.isEmpty(parentXPath) || "/".equals(parentXPath))
             return null; //connected to the root
         parentXPath = XPathElement.stripType(parentXPath);
         try {
             List<XPathElement> xpe = XPathElement.split(parentXPath.toUpperCase());
             if (xpe.size() == 0)
                 return null; //play safe, but should not happen
             for (FxGroupAssignment rga : assignedGroups)
                 if (rga.getAlias().equals(xpe.get(0).getAlias()))
                     return rga.getAssignment(xpe, parentXPath);
             for (FxPropertyAssignment rpa : assignedProperties)
                 if (rpa.getAlias().equals(xpe.get(0).getAlias()))
                     return rpa;
         } catch (FxNotFoundException e) {
             throw e.asRuntimeException();
         }
         throw new FxNotFoundException("ex.structure.assignment.notFound.xpath", parentXPath).asRuntimeException();
     }
 
     /**
      * Check if the requested assignment exists for this type
      *
      * @param xPath xpath of the assignment
      * @return assignment exists
      * @since 3.1
      */
     public boolean hasAssignment(String xPath) {
         try {
             getAssignment(xPath);
             return true;
         } catch (FxRuntimeException e) {
             return false;
         }
     }
 
     /**
      * Get the FxPropertyAssignment for the given XPath.
      * This is a convenience method calling internally getAssignment and casting the result to FxPropertyAssignment if
      * appropriate, else throws an FxInvalidParameterException if the assignment is a group.
      *
      * @param parentXPath desired XPath
      * @return FxAssignment
      */
     @SuppressWarnings({"ThrowableInstanceNeverThrown"})
     public FxPropertyAssignment getPropertyAssignment(String parentXPath) {
         FxAssignment pa = getAssignment(parentXPath);
         if (pa instanceof FxPropertyAssignment)
             return (FxPropertyAssignment) pa;
         throw new FxInvalidParameterException("parentXPath", "ex.structure.assignment.noProperty", parentXPath).asRuntimeException();
     }
 
     /**
      * Determines the property assignment that should be treated as the main binary content for this type.
      * Currently this is the first binary assignment that has a minimum multiplicity of 1, or if none exists
      * the first binary assignment.
      * <p>However, parent group multiplicities are not yet taken into account.</p>
      *
      * @return the property assignment that should be treated as the main binary content for this type,
      *         or null if no such assignment exists
      * @since 3.1
      */
     public FxPropertyAssignment getMainBinaryAssignment() {
         final List<FxPropertyAssignment> binaryAssignments = Lists.newArrayList();
         for (FxPropertyAssignment assignment : getAllProperties()) {
             if (assignment.getProperty().getDataType() == FxDataType.Binary) {
                 if (assignment.getMultiplicity().getMin() > 0) {
                     return assignment;
                 }
                 binaryAssignments.add(assignment);
             }
         }
         // no mandatory binary assignment found, return the first one
         return binaryAssignments.isEmpty() ? null : binaryAssignments.get(0);
     }
 
     /**
      * Returns the mandatory assignments of the given type.
      *
      * @param datatype data type to check for
      * @return the mandatory assignments of the given type.
      * @since 3.1
      */
     public List<FxPropertyAssignment> getMandatoryAssignments(FxDataType datatype) {
         final List<FxPropertyAssignment> result = new ArrayList<FxPropertyAssignment>();
         for (FxPropertyAssignment assignment : getAllProperties()) {
             if (assignment.getProperty().getDataType() == datatype && assignment.getMultiplicity().getMin() > 0) {
                 boolean allRequired = true;
                 FxAssignment parent = assignment;
                 // check if all parents are also required
                 while ((parent = parent.getParentGroupAssignment()) != null) {
                     if (parent.getMultiplicity().getMin() == 0) {
                         allRequired = false;
                         break;
                     }
                 }
                 if (allRequired) {
                     // assignment must exist in every instance, return true
                     result.add(assignment);
                 }
             }
         }
         return result;
     }
 
     /**
      * Get the FxGroupAssignment for the given XPath.
      * This is a convenience method calling internally getAssignment and casting the result to FxGroupAssignment if
      * appropriate, else throws an FxInvalidParameterException if the assignment is a property.
      *
      * @param parentXPath desired XPath
      * @return FxAssignment
      */
     @SuppressWarnings({"ThrowableInstanceNeverThrown"})
     public FxGroupAssignment getGroupAssignment(String parentXPath) {
         FxAssignment pa = getAssignment(parentXPath);
         if (pa instanceof FxGroupAssignment)
             return (FxGroupAssignment) pa;
         throw new FxInvalidParameterException("parentXPath", "ex.structure.assignment.noGroup", parentXPath).asRuntimeException();
     }
 
     /**
      * Get a list of all FxPropertyAssignments connected to this type that are assigned to the requested property
      *
      * @param propertyId requested property id
      * @return list of all FxPropertyAssignments connected to this type that are assigned to the requested property
      */
     public List<FxPropertyAssignment> getAssignmentsForProperty(long propertyId) {
         List<FxPropertyAssignment> ret = new ArrayList<FxPropertyAssignment>(10);
         for (FxPropertyAssignment rpa : assignedProperties)
             if (rpa.getProperty().getId() == propertyId)
                 ret.add(rpa);
         for (FxGroupAssignment rga : assignedGroups)
             for (FxAssignment a : rga.getAllChildAssignments())
                 if (a instanceof FxPropertyAssignment && ((FxPropertyAssignment) a).getProperty().getId() == propertyId) {
                     ret.add((FxPropertyAssignment) a);
                 }
         return ret;
     }
 
     /**
      * Get a list of all FxPropertyAssignments connected to this type that are of the given
      * {@link FxDataType}.
      *
      * @param dataType the data type
      * @return list of all FxPropertyAssignments connected to this type that are of the given data type
      */
     public List<FxPropertyAssignment> getAssignmentsForDataType(FxDataType dataType) {
         List<FxPropertyAssignment> ret = new ArrayList<FxPropertyAssignment>(10);
         for (FxPropertyAssignment rpa : assignedProperties)
             if (rpa.getProperty().getDataType().equals(dataType))
                 ret.add(rpa);
         for (FxGroupAssignment rga : assignedGroups)
             for (FxAssignment a : rga.getAllChildAssignments())
                 if (a instanceof FxPropertyAssignment && ((FxPropertyAssignment) a).getProperty().getDataType().equals(dataType)) {
                     ret.add((FxPropertyAssignment) a);
                 }
         return ret;
     }
 
     /**
      * Get all assignments directly connected to the given XPath
      *
      * @param parentXPath desired XPath
      * @return ArrayList of FxAssignment
      */
     @SuppressWarnings({"ThrowableInstanceNeverThrown"})
     public List<FxAssignment> getConnectedAssignments(String parentXPath) {
         List<FxAssignment> assignments = new ArrayList<FxAssignment>(10);
         if (StringUtils.isEmpty(parentXPath) || "/".equals(parentXPath)) {
             for (FxGroupAssignment rga : assignedGroups)
                 if (rga.getParentGroupAssignment() == null)
                     assignments.add(rga);
             for (FxPropertyAssignment rpa : assignedProperties)
                 if (rpa.getParentGroupAssignment() == null)
                     assignments.add(rpa);
             return Collections.unmodifiableList(FxAssignment.sort(assignments));
         }
         //check if the parentXPath is a group
         if (!(getAssignment(parentXPath) instanceof FxGroupAssignment))
             throw new FxInvalidParameterException("ex.structure.assignment.noGroup", parentXPath).asRuntimeException();
         for (FxGroupAssignment rga : assignedGroups)
             if (rga.getXPath().equals(parentXPath))
                 return rga.getAssignments();
         throw new FxNotFoundException("ex.structure.assignments.notFound.xpath", parentXPath).asRuntimeException();
     }
 
     /**
      * Check if the given XPath is valid for this type
      *
      * @param XPath         the XPath to check
      * @param checkProperty should the XPath point to a property?
      * @return if the XPath is valid or not
      */
     public boolean isXPathValid(String XPath, boolean checkProperty) {
         if (StringUtils.isEmpty(XPath))
             return false;
         if ("/".equals(XPath))
             return !checkProperty; //only valid for groups
         XPath = XPath.toUpperCase();
         if (!XPath.startsWith("/")) {
             if (XPath.startsWith(getName()))
                 XPath = XPathElement.stripType(XPath);
             else
                 return false;
         }
         boolean valid = true;
         try {
             int depth = 0;
             FxAssignment last = null;
             for (XPathElement xpe : XPathElement.split(XPath)) {
                 depth++;
                 if (depth == 1) {
                     //check root assignments
                     boolean found = false;
                     for (FxPropertyAssignment pa : assignedProperties) {
                         if (pa.getAlias().equals(xpe.getAlias())) {
                             last = pa;
                             // only check the max. multiplicity
                             valid = pa.isEnabled() && pa.getMultiplicity().isValidMax(xpe.getIndex());
                             found = true;
                             break;
                         }
                     }
                     if (found)
                         continue;
                     for (FxGroupAssignment ga : assignedGroups) {
                         if (ga.getAlias().equals(xpe.getAlias())) {
                             last = ga;
                             // only check the max. multiplicity
                             valid = ga.isEnabled() && ga.getMultiplicity().isValidMax(xpe.getIndex());
                             break;
                         }
                     }
                     //end root assignments check
                 } else {
                     if (last == null || (last instanceof FxPropertyAssignment)) {
                         valid = false;
                         break;
                     }
                     for (FxAssignment as : ((FxGroupAssignment) last).getAssignments()) {
                         if (last != null)
                             last = null; //reset to null for check
                         if (as.getAlias().equals(xpe.getAlias())) {
                             last = as;
                             // only check the max. multiplicity
                             valid = as.isEnabled() && as.getMultiplicity().isValidMax(xpe.getIndex());
                             break;
                         }
                     }
                 }
                 if (!valid)
                     break;
             }
             if (valid) {
                 if (checkProperty)
                     valid = last instanceof FxPropertyAssignment;
                 else
                     valid = last instanceof FxGroupAssignment;
             }
         } catch (FxRuntimeException e) {
             return false;
         }
         return valid;
     }
 
     /**
      * Does this type contain assignments that are stored in a flat storage?
      *
      * @return type contains assignments that are stored in a flat storage
      */
     public boolean isContainsFlatStorageAssignments() {
         return containsFlatStorageAssignments;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public String toString() {
         return this.getName() + "[id=" + this.getId() + "]";
     }
 
     /**
      * Get this FxType as editable
      *
      * @return FxTypeEdit
      */
     public FxTypeEdit asEditable() {
         return new FxTypeEdit(this);
     }
 
     /**
      * Build an XPath prefix for addressing an instance in XPath's
      *
      * @param pk primary key of the instance
      * @return XPath prefix like "FxType name[@pk=..]"
      */
     public String buildXPathPrefix(FxPK pk) {
         return XPathElement.xpToUpperCase(this.getName()) + "[@PK=" + pk + "]";
     }
 
     /**
      * Check if an option is set for the requested key
      *
      * @param key option key
      * @return if an option is set for the requested key
      * @since 3.1
      */
     public boolean hasOption(String key) {
         return FxStructureOption.hasOption(key, options);
     }
 
     /**
      * Get an option entry for the given key, if the key is invalid or not found a <code>FxTypeStructureOption</code> object
      * will be returned with <code>set</code> set to <code>false</code>, overridable set to <code>false</code> and value
      * set to an empty String.
      *
      * @param key option key
      * @return the found option or an object that indicates that the option is not set
      * @since 3.1
      */
     public FxStructureOption getOption(String key) {
         return FxStructureOption.getOption(key, options);
     }
 
     /**
      * Get a (unmodifiable) list of all options set for this group
      *
      * @return (unmodifiable) list of all options set for this group
      * @since 3.1
      */
     public List<FxStructureOption> getOptions() {
         return FxStructureOption.getUnmodifieableOptions(options);
     }
 
     /**
      * Retrieve the List of options which are inherited by derived types
      *
      * @return (modifiable) list of all options inherited by derived types
      * @since 3.1
      */
     public List<FxStructureOption> getInheritedOptions() {
         final List<FxStructureOption> out = new ArrayList<FxStructureOption>(options.size());
         for (FxStructureOption o : options) {
             if (o.getIsInherited()) {
                 out.add(o);
             }
         }
         return out;
     }
 
     /**
      * Retrieve the mime types for a given FxType
      *
      * @return return the mime types for a given FxType as a FxMimeType obj., or null if none are set
      * @since 3.1
      */
     public FxMimeTypeWrapper getMimeType() {
         if (isMimeTypeSet()) {
             return new FxMimeTypeWrapper(getOption(FxStructureOption.OPTION_MIMETYPE).getValue());
         }
         return null;
     }
 
     /**
      * Convenience method to check if ANY mime type was set for this FxType
      *
      * @return true if a mime type was set
      * @since 3.1
      */
     public boolean isMimeTypeSet() {
         return getOption(FxStructureOption.OPTION_MIMETYPE).isSet();
     }
 
     /**
      * Checks if a given mime type is set for the FxType
      * The given String parameter can either be a main type of a mimetype ("e.g." "image" or "image/") or a fully qualified
      * mime type including the subtype, e.g. "image/png" or "audio/wav".
      *
      * @param mimeType the mime type's type or the fully qualified mime type as a String parameter
      * @return returns true if the given mime type matches the mime types configured for this FxType
      */
     public boolean hasMimeType(String mimeType) {
         if (isMimeTypeSet()) {
             final FxMimeTypeWrapper mimeWrapper = getMimeType();
             return mimeWrapper.contains(mimeType);
         }
         return false;
     }
 }
