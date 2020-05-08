 package com.versionone.common.sdk;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import com.versionone.Oid;
 import com.versionone.apiclient.AndFilterTerm;
 
 import com.versionone.apiclient.APIException;
 
 import com.versionone.apiclient.Asset;
 import com.versionone.apiclient.AssetState;
 import com.versionone.apiclient.Attribute;
 import com.versionone.apiclient.ConnectionException;
 import com.versionone.apiclient.FilterTerm;
 import com.versionone.apiclient.IAssetType;
 import com.versionone.apiclient.IAttributeDefinition;
 import com.versionone.apiclient.IFilterTerm;
 import com.versionone.apiclient.ILocalizer;
 import com.versionone.apiclient.IMetaModel;
 import com.versionone.apiclient.IServices;
 import com.versionone.apiclient.Localizer;
 import com.versionone.apiclient.MetaException;
 import com.versionone.apiclient.MetaModel;
 import com.versionone.apiclient.OidException;
 import com.versionone.apiclient.OrderBy;
 import com.versionone.apiclient.Query;
 import com.versionone.apiclient.QueryResult;
 import com.versionone.apiclient.Services;
 import com.versionone.apiclient.V1APIConnector;
 import com.versionone.apiclient.V1Configuration;
 import com.versionone.apiclient.IOperation;
 import com.versionone.apiclient.V1Exception;
 
 public class ApiDataLayer {
 
     private static final String MetaUrlSuffix = "meta.v1/";
     private static final String LocalizerUrlSuffix = "loc.v1/";
     private static final String DataUrlSuffix = "rest-1.v1/";
     private static final String ConfigUrlSuffix = "config.v1/";
 
     private static final List<String> effortTrackingAttributesList = Arrays.asList(Workitem.DETAIL_ESTIMATE_PROPERTY,
             "ToDo", "Done", "Effort", "Actuals");
 
     private final Map<String, IAssetType> types = new HashMap<String, IAssetType>(5);
     private final Map<Asset, Double> efforts = new HashMap<Asset, Double>();
 
     public static final String OP_QUICK_CLOSE = "QuickClose";
     public static final String OP_CLOSE = "Inactivate";
     public static final String OP_SIGNUP = "QuickSignup";
 
     private IAssetType projectType;
     private IAssetType workitemType;
     private IAssetType primaryWorkitemType;
     private IAssetType effortType;
 
     private static ApiDataLayer instance;
     private boolean isConnected;
 
     public Oid memberOid;
     private String path;
     private String userName;
     private String password;
     private boolean integrated;
 
     private QueryResult assetList;
     private final List<IAttributeDefinition> alreadyUsedDefinition = new ArrayList<IAttributeDefinition>();
     private static Set<AttributeInfo> attributesToQuery = new HashSet<AttributeInfo>();
     private Map<String, PropertyValues> listPropertyValues;
 
     private boolean trackEffort;
     public EffortTrackingLevel defectTrackingLevel;
     public EffortTrackingLevel storyTrackingLevel;
 
     private IMetaModel metaModel;
     private IServices services;
     private ILocalizer localizer;
 
     private String currentProjectId;
     public boolean showAllTasks = true;
 
     private final ArrayList<Asset> assetsToIgnore = new ArrayList<Asset>();
 
     public void setShowAllTasks(boolean showAllTasks) {
         this.showAllTasks = showAllTasks;
     }
 
     private ApiDataLayer() {
     }
 
     /**
      * Special method ONLY for testing.
      */
     public void connectFotTesting(Object services, Object metaModel, Object localizer, Object v1Configuration)
             throws Exception {
         this.metaModel = (IMetaModel) metaModel;
         this.services = (IServices) services;
         this.localizer = (ILocalizer) localizer;
 
         final V1Configuration configConnector = (V1Configuration) v1Configuration;
         if (configConnector != null) {
             trackEffort = configConnector.isEffortTracking();
             if (trackEffort) {
                 effortType = this.metaModel.getAssetType("Actual");
             }
             storyTrackingLevel = EffortTrackingLevel.translate(configConnector.getStoryTrackingLevel());
             defectTrackingLevel = EffortTrackingLevel.translate(configConnector.getDefectTrackingLevel());
         }
 
         initTypes();
         isConnected = true;
         memberOid = this.services.getLoggedIn();
         listPropertyValues = getListPropertyValues();
     }
 
     public static ApiDataLayer getInstance() {
         if (instance == null) {
             instance = new ApiDataLayer();
         }
         return instance;
     }
 
     public boolean connect(String path, String userName, String password, boolean integrated) throws DataLayerException {
         isConnected = false;
         boolean isUserChanged = true;
         if (this.userName != null && this.password != null && this.path != null) {
             isUserChanged = !this.userName.equals(userName) || !this.password.equals(password) || !this.path.equals(path);
         }
         
         
         this.userName = userName;
         this.password = password;
         this.integrated = integrated;
         assetList = null;
         boolean isUpdateData = true;
         //TODO for remove
         //boolean isTokenChanged = true;
 
 //        String currentOid = PreferencePage.getPreferences().getString(PreferenceConstants.P_MEMBER_TOKEN);
 //        if (memberOid != null) {
 //            // location or user was changed
 //            isTokenChanged = !currentOid.equals(memberOid.getToken() + ":" + path);
 //        }
         // TODO test optimization of refresh. need to test
         isUpdateData = isUserChanged || metaModel == null || localizer == null || services == null;
         if (isUpdateData) {
             assetsToIgnore.clear();
             efforts.clear();
             types.clear();
         }
 
         try {
             if (isUpdateData) {
                 V1APIConnector metaConnector = new V1APIConnector(path + MetaUrlSuffix, userName, password);
                 metaModel = new MetaModel(metaConnector);
 
                 V1APIConnector localizerConnector = new V1APIConnector(path + LocalizerUrlSuffix, userName, password);
                 localizer = new Localizer(localizerConnector);
 
                 V1APIConnector dataConnector = new V1APIConnector(path + DataUrlSuffix, userName, password);
                 services = new Services(metaModel, dataConnector);
 
                 initTypes();
             }
             V1Configuration v1Config = new V1Configuration(new V1APIConnector(path + ConfigUrlSuffix));
 
             trackEffort = v1Config.isEffortTracking();
             if (trackEffort) {
                 effortType = metaModel.getAssetType("Actual");
             }
 
             storyTrackingLevel = EffortTrackingLevel.translate(v1Config.getStoryTrackingLevel());
             defectTrackingLevel = EffortTrackingLevel.translate(v1Config.getDefectTrackingLevel());
 
             memberOid = services.getLoggedIn();
             listPropertyValues = getListPropertyValues();
 
             isConnected = true;
             //TODO for remove
 //            if (isTokenChanged) {
 //                PreferencePage.getPreferences().setValue(PreferenceConstants.P_MEMBER_TOKEN,
 //                        memberOid.getToken() + ":" + path);
 //            }
 
             this.path = path;
             return true;
         } catch (MetaException e) {
             throw warning("Cannot connect to V1 server.", e);
         } catch (Exception e) {
             throw warning("Cannot connect to V1 server.", e);
         }
     }
 
     private void initTypes() {
         projectType = getAssetType(Workitem.PROJECT_PREFIX);
         getAssetType(Workitem.TASK_PREFIX);
         getAssetType(Workitem.TEST_PREFIX);
         getAssetType(Workitem.DEFECT_PREFIX);
         getAssetType(Workitem.STORY_PREFIX);
         workitemType = metaModel.getAssetType("Workitem");
         primaryWorkitemType = metaModel.getAssetType("PrimaryWorkitem");
     }
 
     /**
      * Reconnect with settings, used in last Connect() call.
      * 
      * @throws DataLayerException
      */
     public boolean reconnect() throws DataLayerException {
         return connect(path, userName, password, integrated);
     }
 
     public List<Workitem> getProjectTree() throws DataLayerException {
         try {
             Query scopeQuery = new Query(projectType, projectType.getAttributeDefinition("Parent"));
             FilterTerm stateTerm = new FilterTerm(projectType.getAttributeDefinition("AssetState"));
             stateTerm.NotEqual(AssetState.Closed);
             scopeQuery.setFilter(stateTerm);
             // clear all definitions used in previous queries
             alreadyUsedDefinition.clear();
             addSelection(scopeQuery, Workitem.PROJECT_PREFIX);
             QueryResult result = services.retrieve(scopeQuery);
             List<Workitem> roots = new ArrayList<Workitem>(result.getAssets().length);
             for (Asset oneAsset : result.getAssets()) {
                 roots.add(new Workitem(oneAsset, null));
             }
             return roots;
         } catch (Exception ex) {
             throw warning("Can't get projects list.", ex);
         }
     }
 
     private IAssetType getAssetType(String token) {
         IAssetType type = metaModel.getAssetType(token);
         types.put(token, type);
         return type;
     }
 
     public boolean isCurrentUserOwnerAsset(Asset childAsset) {
         return isCurrentUserOwnerAsset(childAsset, workitemType.getAttributeDefinition(Workitem.OWNERS_PROPERTY));
     }
 
     public Workitem[] getWorkitemTree() throws Exception {
         checkConnection();
         if (currentProjectId == null) {
             currentProjectId = getDefaultProjectId();
         }
 
         if (assetList == null) {
             try {
                 IAttributeDefinition parentDef = workitemType.getAttributeDefinition("Parent");
 
                 Query query = new Query(workitemType, parentDef);
 
                 // clear all definitions used in previous queries
                 alreadyUsedDefinition.clear();
                 addSelection(query, Workitem.TASK_PREFIX);
                 addSelection(query, Workitem.STORY_PREFIX);
                 addSelection(query, Workitem.DEFECT_PREFIX);
                 addSelection(query, Workitem.TEST_PREFIX);
 
                 query.setFilter(getScopeFilter(workitemType));
 
                 query.getOrderBy().majorSort(primaryWorkitemType.getDefaultOrderBy(), OrderBy.Order.Ascending);
                 query.getOrderBy().minorSort(workitemType.getDefaultOrderBy(), OrderBy.Order.Ascending);
 
                 assetList = services.retrieve(query);
             } catch (MetaException ex) {
                 throw warning("Unable to get workitems.", ex);
             } catch (Exception ex) {
                 throw warning("Unable to get workitems.", ex);
             }
         }
 
         IAttributeDefinition definition = workitemType.getAttributeDefinition(Workitem.OWNERS_PROPERTY);
         List<Workitem> res = new ArrayList<Workitem>(assetList.getAssets().length);
 
         for (Asset asset : assetList.getAssets()) {
             if (isAssetSuspended(asset)) {
                 continue;
             }
 
             if (showAllTasks || isCurrentUserOwnerAsset(asset, definition)) {
                 res.add(new Workitem(asset, null));
             }
         }
         return res.toArray(new Workitem[res.size()]);
     }
 
     private boolean isCurrentUserOwnerAsset(Asset assets, IAttributeDefinition definition) {
         Attribute attribute = assets.getAttribute(definition);
 
         Object[] owners = attribute.getValues();
         for (Object oid : owners) {
             if (memberOid.equals(oid)) {
                 return true;
             }
         }
         if (assets.hasChanged()) {
             for (Asset child : assets.getChildren()) {
                 if (isCurrentUserOwnerAsset(child, definition)) {
                     return true;
                 }
             }
         }
 
         return false;
     }
 
     public boolean checkConnection(String url, String user, String pass, boolean auth) {
         boolean result = true;
 
         V1APIConnector metaConnector = new V1APIConnector(url.toString() + MetaUrlSuffix);
         MetaModel model = new MetaModel(metaConnector);
 
         V1APIConnector dataConnector = null;
         if (auth) {
             dataConnector = new V1APIConnector(url.toString() + DataUrlSuffix);
         } else {
             dataConnector = new V1APIConnector(url.toString() + DataUrlSuffix, user, pass);
         }
 
         Services v1Service = new Services(model, dataConnector);
 
         try {
             v1Service.getLoggedIn();
         } catch (V1Exception e) {
             result = false;
         } catch (MetaException e) {
             result = false;
         }
 
         return result;
     }
 
     private void checkConnection() throws DataLayerException {
         if (!isConnected && !reconnect()) {
             throw warning("Connection is not set.");
         }
     }
 
     private IFilterTerm getScopeFilter(IAssetType assetType) {
         List<FilterTerm> terms = new ArrayList<FilterTerm>(5);
         FilterTerm term = new FilterTerm(assetType.getAttributeDefinition("Scope.AssetState"));
         term.NotEqual(AssetState.Closed);
         terms.add(term);
         term = new FilterTerm(assetType.getAttributeDefinition("Scope.ParentMeAndUp"));
         term.Equal(currentProjectId);
         terms.add(term);
         term = new FilterTerm(assetType.getAttributeDefinition("Timebox.State.Code"));
         term.Equal("ACTV");
         terms.add(term);
         term = new FilterTerm(assetType.getAttributeDefinition("AssetState"));
         term.NotEqual(AssetState.Closed);
         terms.add(term);
         return new AndFilterTerm(terms.toArray(new FilterTerm[terms.size()]));
     }
 
     // need to make AlreadyUsedDefinition.Clear(); before first call of this
     // method
     private void addSelection(Query query, String typePrefix) throws DataLayerException {
         for (AttributeInfo attrInfo : attributesToQuery) {
             if (attrInfo.prefix.equals(typePrefix)) {
                 try {
                     IAttributeDefinition def = types.get(attrInfo.prefix).getAttributeDefinition(attrInfo.attr);
                     if (!alreadyUsedDefinition.contains(def)) {
                         query.getSelection().add(def);
                         alreadyUsedDefinition.add(def);
                     }
                 } catch (MetaException e) {
                     warning("Wrong attribute: " + attrInfo, e);
                 }
             }
         }
     }
 
     private void addSelection(Query query, String typePrefix, boolean clearDefinitions) throws DataLayerException {
         if (clearDefinitions) {
             alreadyUsedDefinition.clear();
         }
         addSelection(query, typePrefix);
     }
 
     public void addProperty(String attr, String prefix, boolean isList) {
         attributesToQuery.add(new AttributeInfo(attr, prefix, isList));
     }
 
     private Map<String, PropertyValues> getListPropertyValues() throws Exception {
         Map<String, PropertyValues> res = new HashMap<String, PropertyValues>(attributesToQuery.size());
         for (AttributeInfo attrInfo : attributesToQuery) {
             if (!attrInfo.isList) {
                 continue;
             }
 
             String propertyAlias = attrInfo.prefix + attrInfo.attr;
             if (!res.containsKey(propertyAlias)) {
                 String propertyName = resolvePropertyKey(propertyAlias);
 
                 PropertyValues values;
                 if (res.containsKey(propertyName)) {
                     values = res.get(propertyName);
                 } else {
                     values = queryPropertyValues(propertyName);
                     res.put(propertyName, values);
                 }
 
                 if (!res.containsKey(propertyAlias)) {
                     res.put(propertyAlias, values);
                 }
             }
         }
         return res;
     }
 
     private static String resolvePropertyKey(String propertyAlias) {
         if (propertyAlias.equals("DefectStatus")) {
             return "StoryStatus";
         } else if (propertyAlias.equals("DefectSource")) {
             return "StorySource";
         } else if (propertyAlias.equals("ScopeBuildProjects")) {
             return "BuildProject";
         } else if (propertyAlias.equals("TaskOwners") || propertyAlias.equals("StoryOwners")
                 || propertyAlias.equals("DefectOwners") || propertyAlias.equals("TestOwners")) {
             return "Member";
         }
 
         return propertyAlias;
     }
 
     private PropertyValues queryPropertyValues(String propertyName) throws ConnectionException, APIException,
             OidException, MetaException {
         PropertyValues res = new PropertyValues();
         IAssetType assetType = metaModel.getAssetType(propertyName);
         IAttributeDefinition nameDef = assetType.getAttributeDefinition(Workitem.NAME_PROPERTY);
         IAttributeDefinition inactiveDef;
 
         Query query = new Query(assetType);
         query.getSelection().add(nameDef);
 
         // TODO need to recongnize is it possible what task can have closed
         // owner
         inactiveDef = assetType.getAttributeDefinition("Inactive");
         if (inactiveDef != null) {
             FilterTerm filter = new FilterTerm(inactiveDef);
             filter.Equal("False");
             query.setFilter(filter);
         }
 
         query.getOrderBy().majorSort(assetType.getDefaultOrderBy(), OrderBy.Order.Ascending);
 
         res.addInternal(new ValueId());
         for (Asset asset : services.retrieve(query).getAssets()) {
             String name = (String) asset.getAttribute(nameDef).getValue();
             res.addInternal(new ValueId(asset.getOid(), name));
         }
         return res;
     }
 
     public PropertyValues getListPropertyValues(String type, String propertyName) {
         String propertyKey = resolvePropertyKey(type + propertyName);
         return listPropertyValues.get(propertyKey);
     }
 
     static DataLayerException warning(String string, Exception ex) {
         return new DataLayerException(string, ex);
     }
 
     static DataLayerException warning(String string) {
         return new DataLayerException(string);
     }
 
     public boolean isTrackEffortEnabled() {
         return trackEffort;
     }
 
     Double getEffort(Asset asset) {
         return efforts.get(asset);
     }
 
     void setEffort(Asset asset, Double value) {
         if (value == null || value == 0) {
             efforts.remove(asset);
         } else {
             efforts.put(asset, value);
         }
     }
 
     public boolean isEffortTrackingRelated(String propertyName) {
         return effortTrackingAttributesList.contains(propertyName);
     }
 
     void commitAsset(Asset asset) throws V1Exception {
         services.save(asset);
         commitEffort(asset);
     }
 
     private void commitEffort(Asset asset) throws V1Exception {
         if (efforts.containsKey(asset)) {
             Asset effort = services.createNew(effortType, asset.getOid());
             effort.setAttributeValue(effortType.getAttributeDefinition("Value"), efforts.get(asset));
             effort.setAttributeValue(effortType.getAttributeDefinition("Date"), new Date());
             services.save(effort);
             efforts.remove(asset);
         }
     }
 
     void revertAsset(Asset asset) {
         asset.rejectChanges();
         efforts.remove(asset);
     }
 
     public void commitChanges() throws DataLayerException {
         checkConnection();
         try {
             commitAssetsRecursively(Arrays.asList(assetList.getAssets()));
         } catch (V1Exception e) {
             throw warning("Cannot commit changes.", e);
         }
     }
 
     private void commitAssetsRecursively(List<Asset> assets) throws V1Exception {
         for (Asset asset : assets) {
             // do not commit assets that were closed and their children
             if (assetsToIgnore.contains(asset)) {
                 continue;
             }
 
             commitAsset(asset);
             commitAssetsRecursively(asset.getChildren());
         }
     }
 
     void executeOperation(Asset asset, IOperation operation) throws V1Exception {
         services.executeOperation(operation, asset.getOid());
     }
 
     public boolean isAssetClosed(Asset asset) {
         try {
             IAttributeDefinition stateDef = asset.getAssetType().getAttributeDefinition("AssetState");
             AssetState state = AssetState.valueOf((Integer) asset.getAttribute(stateDef).getValue());
             return state == AssetState.Closed || assetsToIgnore.contains(asset.getOid().getMomentless());
         } catch (MetaException e) {
         } catch (APIException e) {
         }
         return false;
     }
 
     public boolean isAssetSuspended(Asset asset) {
         return assetsToIgnore.contains(asset);
     }
 
     public void addIgnoreRecursively(Workitem item) {
         assetsToIgnore.add(item.asset);
         for (Workitem child : item.children) {
             addIgnoreRecursively(child);
         }
     }
 
     // TODO refactor
     void refreshAsset(Workitem workitem) throws DataLayerException {
         try {
             IAttributeDefinition stateDef = workitem.asset.getAssetType().getAttributeDefinition("AssetState");
             Query query = new Query(workitem.asset.getOid().getMomentless(), false);
             addSelection(query, workitem.getTypePrefix(), true);
             query.getSelection().add(stateDef);
             QueryResult newAssets = services.retrieve(query);
 
             Asset[] parentArray = assetList.getAssets();
             List<Asset> parentList = null;
             if (workitem.parent != null) {
                 parentList = workitem.parent.asset.getChildren();
             }
 
             if (newAssets.getTotalAvaliable() != 1) {
                 assetsToIgnore.add(workitem.asset);
                 return;
             }
 
             Asset newAsset = newAssets.getAssets()[0];
             if (isAssetClosed(newAsset)) {
                 assetsToIgnore.add(workitem.asset);
                 return;
             }
 
             // Adding new Asset to parent
             for (int i = 0; i < parentArray.length; i++) {
                 if (parentArray[i].equals(workitem.asset)) {
                     parentArray[i] = newAsset;
                     break;
                 }
             }
 
             if (parentList != null) {
                 int index = parentList.indexOf(workitem.asset);
                 if (index != -1) {
                     parentList.set(index, newAsset);
                 }
             }
             newAsset.getChildren().addAll(workitem.asset.getChildren());
         } catch (MetaException ex) {
             throw warning("Unable to get workitems.", ex);
         } catch (Exception ex) {
             throw warning("Unable to get workitems.", ex);
         }
     }
 
     public void setCurrentProjectId(String value) {
         if (isProjectExist(value)) {
             currentProjectId = value;
         } else {
             currentProjectId = getDefaultProjectId();
         }
         assetList = null;
     }
 
     public String getCurrentProjectId() {
         return currentProjectId;
     }
 
     public void setCurrentProject(Workitem value) {
         currentProjectId = value.getId();
         assetList = null;
     }
 
     public Workitem getCurrentProject() throws Exception {
         if (currentProjectId == null || currentProjectId.equals("")) {
             currentProjectId = getDefaultProjectId();
         }
         return getProjectById(currentProjectId);
     }
     
     public String getCurrentMemberToken() {
         return memberOid != null ? memberOid.getToken() : null;
     }
 
     private String getDefaultProjectId() {
         String id = "";
 
         Query query = new Query(projectType);
 
         QueryResult result = null;
         try {
             result = services.retrieve(query);
         } catch (Exception ex) {
         }
 
         if (result != null && result.getTotalAvaliable() > 0) {
             id = result.getAssets()[0].getOid().getMomentless().getToken();
         }
 
         return id;
     }
 
     /***
      * Update current project Id to the root project Id from current server
      */
     public String updateCurrentProjectId() {
         currentProjectId = getDefaultProjectId();
         return currentProjectId;
     }
 
     private boolean isProjectExist(String id) {
         boolean isExist = true;
 
         try {
             Query query = new Query(Oid.fromToken(id, metaModel));
             alreadyUsedDefinition.clear();
             addSelection(query, Workitem.PROJECT_PREFIX);
             QueryResult result = null;
             result = services.retrieve(query);
             if (result.getTotalAvaliable() == 0) {
                 isExist = false;
             }
         } catch (Exception ex) {
             isExist = false;
         }
 
         return isExist;
     }
 
     private Workitem getProjectById(String id) throws Exception {
         if (!isConnected || id == null || id.equals("")) {
             return null;
         }
 
         Query query = new Query(Oid.fromToken(id, metaModel));
         // clear all definitions used in previous queries
         alreadyUsedDefinition.clear();
         addSelection(query, Workitem.PROJECT_PREFIX);
         QueryResult result;
         try {
             result = services.retrieve(query);
         } catch (MetaException ex) {
             isConnected = false;
             throw warning("Unable to get projects", ex);
         } catch (Exception ex) {
             throw warning("Unable to get projects", ex);
         }
 
         if (result.getTotalAvaliable() == 1) {
             return new Workitem(result.getAssets()[0], null);
         }
         return null;
     }
 
     public String localizerResolve(String key) throws DataLayerException {
         try {
             return localizer.resolve(key);
         } catch (Exception ex) {
             throw warning("Failed to resolve key.", ex);
         }
     }
 
     public static void resetConnection() {
         instance = null;
     }
 
 }
