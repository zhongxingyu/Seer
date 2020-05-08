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
 package com.flexive.faces.beans;
 
 import com.flexive.faces.FxJsfUtils;
 import com.flexive.faces.javascript.yui.YahooResultProvider;
 import com.flexive.faces.messages.FxFacesMsgErr;
 import com.flexive.faces.messages.FxFacesMsgInfo;
 import com.flexive.faces.messages.FxFacesMsgWarn;
 import com.flexive.shared.EJBLookup;
 import com.flexive.shared.CacheAdmin;
 import com.flexive.shared.FxFormatUtils;
 import com.flexive.shared.FxSharedUtils;
 import com.flexive.shared.structure.FxEnvironment;
 import com.flexive.shared.structure.FxType;
 import com.flexive.shared.tree.FxTreeMode;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.exceptions.FxNotFoundException;
 import com.flexive.shared.search.*;
 import com.flexive.shared.search.query.PropertyValueComparator;
 import com.flexive.shared.search.query.SqlQueryBuilder;
 import com.flexive.shared.search.query.VersionFilter;
 import com.flexive.shared.tree.FxTreeNode;
 import com.flexive.shared.value.BinaryDescriptor;
 import com.flexive.shared.value.FxValue;
 import com.flexive.war.filter.FxResponseWrapper;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import javax.faces.model.SelectItem;
 import java.io.IOException;
 import java.io.Serializable;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Arrays;
 import javax.faces.context.FacesContext;
 
 public class SearchResultBean implements ActionBean, Serializable {
     private static final long serialVersionUID = -3167186971609121457L;
     private static final Log LOG = LogFactory.getLog(SearchResultBean.class);
 
     private FxResultSet result;
     private ResultLocation location = AdminResultLocations.ADMIN;
     private ResultSessionData sessionData = null;
     private Briefcase briefcase = null; // cached briefcase object for briefcase queries
 
     // cache settings
     private FxSQLSearchParams.CacheMode cacheMode = FxSQLSearchParams.CacheMode.ON;
 
     // briefcase-related fields set in the query form
     private long briefcaseAclId;
     private String briefcaseDescription;
     private String briefcaseName;
     private Boolean createBriefcase;
 
     private boolean queryFailed;    // set to true in request when query execution failed to prevent re-submission during processing
     private List<SelectItem> subFolderItems;
     private long parentFolderId = -1;
 
 
     /**
      * {@inheritDoc}
      */
     public String getParseRequestParameters() {
         try {
             String action = FxJsfUtils.getParameter("action");
             if (StringUtils.isBlank(action)) {
                 // no action requested
                 return null;
             }
             // hack!
             FxJsfUtils.resetFaceletsComponent("frm");
             if ("fulltextSearch".equals(action)) {
                 String query = FxJsfUtils.getParameter("query");
                 if (StringUtils.isBlank(query)) {
                     new FxFacesMsgErr("SearchResult.err.query.fulltext.empty").addToContext();
                     return null;
                 }
                 resetFilters();
                 setQueryBuilder(createSqlQueryBuilder().condition("*", PropertyValueComparator.EQ, query));
                 show();
             } else if ("nodeSearch".equals(action)) {
                 // search in subtree
                 if (StringUtils.isBlank(FxJsfUtils.getParameter("nodeId"))) {
                     new FxFacesMsgErr("SearchResult.err.query.node.empty").addToContext();
                     return null;
                 }
                 resetFilters();
                 final long id = FxJsfUtils.getLongParameter("nodeId");
                 final boolean liveTree = FxJsfUtils.getBooleanParameter("liveMode", false);
                 final boolean onlyDirect = FxJsfUtils.getBooleanParameter("onlyDirect", false);
 
                 setVersionFilter(liveTree ? VersionFilter.LIVE : VersionFilter.MAX);
                 setFolderId(id);
                 setTreeMode(liveTree ? FxTreeMode.Live : FxTreeMode.Edit);
                 setOnlyDirectChildren(onlyDirect);
                 createFolderQueryBuilder();
                 show();
             } else if ("typeSearch".equals(action)) {
                 // search for contents of a type
                 if (StringUtils.isBlank(FxJsfUtils.getParameter("typeId"))) {
                     new FxFacesMsgErr("SearchResult.err.query.type.empty").addToContext();
                     return null;
                 }
                 resetFilters();
                 final long id = FxJsfUtils.getLongParameter("typeId");
                 setQueryBuilder(createSqlQueryBuilder().type(id).maxRows(Integer.MAX_VALUE));
                 setTabTitle(MessageBean.getInstance().getMessage(
                         "SearchResult.tabtitle.type",
                         CacheAdmin.getEnvironment().getType(id).getLabel()
                 ));
                 show();
             } else if ("openBriefcase".equals(action) || "openBriefcaseDetails".equals(action)) {
                 if (StringUtils.isBlank(FxJsfUtils.getParameter("briefcaseId"))) {
                     new FxFacesMsgErr("SearchResult.err.query.briefcase.empty").addToContext();
                     return null;
                 }
                 // TODO: open briefcase in own location
                 final long briefcaseId = FxJsfUtils.getLongParameter("briefcaseId");
                 resetFilters();
                 getSessionData().setBriefcaseId(briefcaseId);
                 setQueryBuilder(createSqlQueryBuilder().filterBriefcase(briefcaseId));
                 setTabTitle(MessageBean.getInstance().getMessage(
                         "SearchResult.tabtitle.briefcase",
                         getBriefcase().getName()
                 ));
                 show();
             }
         } catch (Exception e) {
             // TODO possibly pass some error message to the HTML page
             LOG.error("Failed to parse request parameters: " + e.getMessage(), e);
         }
         return null;
     }
 
     public String getTabTitle() {
         return getSessionData().getTabTitle();
     }
 
     public void setTabTitle(String tabTitle) {
         getSessionData().setTabTitle(tabTitle);
     }
 
     private SqlQueryBuilder createSqlQueryBuilder() {
         final SqlQueryBuilder builder = new SqlQueryBuilder(location, getViewType());
         builder.filterVersion(getVersionFilter());
         return builder;
     }
 
     private void createFolderQueryBuilder() {
         final SqlQueryBuilder sqb = createSqlQueryBuilder();
         if (isOnlyDirectChildren()) {
             sqb.isDirectChild(getFolderId());
             // exclude all folder types
             final FxType folderType = CacheAdmin.getEnvironment().getType(FxType.FOLDER);
             sqb.condition("typedef",
                     PropertyValueComparator.NOT_IN,
                     FxSharedUtils.getSelectableObjectIdList(
                         Iterables.concat(Arrays.asList(folderType), folderType.getDerivedTypes())
                     )
             );
             setEnableFolderActions(true);
         } else {
             sqb.isChild(getFolderId());
             // don't enable folder actions it would affect many folders at once
             setEnableFolderActions(false);
         }
         setQueryBuilder(sqb.maxRows(Integer.MAX_VALUE));
         try {
             // update tab title
             setTabTitle(MessageBean.getInstance().getMessage("SearchResult.tabtitle.folder",
                     EJBLookup.getTreeEngine().getNode(getTreeMode(), getFolderId()).getLabel())
             );
         } catch (FxApplicationException e) {
             new FxFacesMsgWarn(e).addToContext();
         }
     }
 
     public void resetFilters() {
         setStartRow(0);
         getSessionData().setBriefcaseId(-1);
         setTypeId(-1);
         setVersionFilter(VersionFilter.MAX);
         setPaginatorPage(1);
         setSortColumnKey(null);
         setFolderId(-1);
         setEnableFolderActions(false);
         setTreeMode(FxTreeMode.Edit);
     }
 
     /**
      * Show the current search result.
      *
      * @return the next page
      */
     public String show() {
         // backend briefcase creation - name check
         if (Boolean.TRUE.equals(createBriefcase) && StringUtils.isBlank(briefcaseName)) {
             new FxFacesMsgErr("Briefcase.err.name").addToContext();
             return FxJsfUtils.getManagedBean(QueryEditorBean.class).show();
         }
         if (getResult().isTruncated()) {
             new FxFacesMsgInfo("SearchResult.nfo.truncated", getResult().getTotalRowCount()).addToContext();
         }
         return "contentResult";
     }
 
     /**
      * Switch to list view.
      *
      * @return the next page
      */
     public String listView() {
         setViewType(ResultViewType.LIST);
         setFetchRows(-1);
         setStartRow(0);
         resetTableView();
         return show();
     }
 
     /**
      * Switch to thumbnail view.
      *
      * @return the next page
      */
     public String thumbView() {
         setViewType(ResultViewType.THUMBNAILS);
         setStartRow(0);
         setFetchRows(-1);
         resetTableView();
         return show();
     }
 
     /**
      * Send the current result as CSV to the response.
      *
      * @throws IOException  on I/O errors
      * @since 3.1
      */
     public void exportSpreadsheet() throws IOException {
         renderResultCsv(
                 getResult(),
                 FxJsfUtils.getResponse().getWriter(),
                 "search-result_" + System.currentTimeMillis() + ".xls",
                 '\t'
         );
 
         FacesContext.getCurrentInstance().responseComplete();
     }
 
     /**
      * Render a result set as a CSV file to the response.
      *
      * @param result        the result to be rendered
      * @param writer        the output writer
      * @param filename      the filename that should be shown in the browser
      * @param delimiter     the delimiter between fields ('\t' is best for Excel compatibility)
      * @throws IOException  if the result could not be rendered
      * 
      * @since               3.1
      */
     public static void renderResultCsv(FxResultSet result, Writer writer, String filename, char delimiter) throws IOException {
         final int firstColumnIndex = Math.max(1, result.getUserWildcardIndex());
         final FxResponseWrapper response = FxJsfUtils.getResponse();
         response.setContentType("text/csv");
         response.setCharacterEncoding("UTF-8");
         response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\";");
         // write column headers
         for (int i = firstColumnIndex; i <= result.getColumnCount(); i++) {
             if (i > firstColumnIndex) {
                 writer.write(delimiter);
             }
             writer.write(FxFormatUtils.escapeForCsv(result.getColumnLabel(i)));
         }
         writer.write("\n");
         // write rows
         for (FxResultRow row : result.getResultRows()) {
             for (int i = firstColumnIndex; i <= result.getColumnCount(); i++) {
                 final Object value = row.getValue(i);
                 if (value != null && (!(value instanceof FxValue) || !((FxValue) value).isEmpty())) {
                     // render not-empty value
                     writer.write(FxFormatUtils.escapeForCsv(value.toString()));
                 }
                 writer.write(delimiter);
             }
             writer.write('\n');
         }
     }
 
 
     /**
      * Refresh the current results (clear cache).
      *
      * @return  the next page
      */
     public String refresh() {
         setResult(null);
         return show();
     }
 
     public FxResultSet getResult() {
         if (location.isCacheInSession()) {
             result = getSessionData().getResult();
         }
         if (result == null && !queryFailed) {
             try {
                 if (Boolean.TRUE.equals(createBriefcase)) {
                     getQueryBuilder().saveInBriefcase(briefcaseName, briefcaseDescription, briefcaseAclId);
                     new FxFacesMsgInfo("Briefcase.nfo.created", briefcaseName).addToContext();
                 }
                 final List<String> columns = new ArrayList<String>();
                 columns.addAll(Arrays.asList("@pk", "@permissions", "@lock", "typedef"));
                 if (getBriefcaseId() != -1) {
                     columns.add("@metadata");
                 }
                 columns.add("@*");
                FxResultSet result = getQueryBuilder()
                         .select(columns)
                         .startRow(0)
                         .getResult();
                 if (getTypeId() != -1) {
                     // check if type ID is still available
                     boolean found = false;
                     for (FxFoundType foundType : result.getContentTypes()) {
                         if (foundType.getContentTypeId() == getTypeId()) {
                             found = true;
                             break;
                         }
                     }
                     // reset type ID if it is no longer present (e.g. because a content was removed)
                     if (!found) {
                         setTypeId(-1);
                        // re-submit query
                        result = getQueryBuilder().select(columns).startRow(0).getResult();
                     }
                 }
                 if (getTypeId() == -1 && result.getContentTypes().size() == 1) {
                     // no type selected, but only one found - search already performed for the specific type
                     setTypeId(result.getContentTypes().get(0).getContentTypeId());
                 }
 
                 setResult(result);
             } catch (FxApplicationException e ) {
                 if (!e.isMessageLogged()) {
                     LOG.warn("Failed to execute query: " + e.getMessage(), e);
                 }
                 onQueryException(e);
             } catch (Exception e) {
                 onQueryException(e);
             }
         }
         return result;
     }
 
     private void onQueryException(Exception e) {
         new FxFacesMsgErr(e).addToContext();
         queryFailed = true;     // prevent execution on next getter call of this request
         getSessionData().setContentTypes(new ArrayList<FxFoundType>(0));
         resetFilters();
     }
 
     public void setResult(FxResultSet result) {
         this.result = result;
         if (location.isCacheInSession()) {
             getSessionData().setResult(result);
         }
     }
 
     public SqlQueryBuilder getQueryBuilder() {
         final SqlQueryBuilder builder = getSessionData().getQueryBuilder();
         return builder
                 .viewType(getViewType())
                 .filterType(getTypeId())
                 .filterVersion(getVersionFilter());
     }
 
     public void setQueryBuilder(SqlQueryBuilder queryBuilder) {
         getSessionData().setQueryBuilder(queryBuilder);
         setTabTitle(null);
     }
 
     public void setStartRow(int rowStart) {
         getSessionData().setStartRow(rowStart);
     }
 
     public int getStartRow() {
         return getSessionData().getStartRow();
     }
 
     public int getFetchRows() {
         return getSessionData().getFetchRows();
     }
 
     public void setFetchRows(int fetchRows) {
         getSessionData().setFetchRows(fetchRows);
     }
 
     /**
      * Returns selectitems for possible values of the fetchRows property.
      *
      * @return selectitems for possible values of the fetchRows property.
      */
     public List<SelectItem> getFetchRowItems() {
         final List<SelectItem> result = new ArrayList<SelectItem>();
         for (int count : new int[]{10, 25, 50, 100}) {
             result.add(new SelectItem(count, MessageBean.getInstance().getMessage("SearchResult.label.fetchRows", count)));
         }
         return result;
     }
 
     public ResultViewType getViewType() {
         return getSessionData().getViewType();
     }
 
     public void setViewType(ResultViewType viewType) {
         getSessionData().setViewType(viewType);
     }
 
     public boolean isListView() {
         return ResultViewType.LIST.equals(getViewType());
     }
 
     public boolean isThumbnailView() {
         return ResultViewType.THUMBNAILS.equals(getViewType());
     }
 
     public void setTypeId(long typeId) {
         getSessionData().setTypeId(typeId);
     }
 
     public long getTypeId() {
         return getSessionData().getTypeId();
     }
 
     public List<FxFoundType> getContentTypes() {
         if (result != null) {
             getSessionData().setContentTypes(result.getContentTypes());
         }
         return getSessionData().getContentTypes();
     }
 
     /**
      * Return JSF selectitems to be displayed in the UI for selecting a type of the result set.
      *
      * @return JSF selectitems to be displayed in the UI for selecting a type of the result set.
      */
     public List<SelectItem> getContentTypeItems() {
         final List<SelectItem> items = new ArrayList<SelectItem>(getContentTypes().size() + 1);
         // "all results" item
         items.add(new SelectItem(-1L, MessageBean.getInstance().getMessage("SearchResult.label.type.all")));
         // add an entry for every found content type
         for (FxFoundType type : getContentTypes()) {
             final String label = type.getDisplayName() + " (" + type.getFoundEntries() + ")";
             items.add(new SelectItem(type.getContentTypeId(), label));
         }
         return items;
     }
 
     public BinaryDescriptor.PreviewSizes getPreviewSize() {
         return getSessionData().getPreviewSize();
     }
 
     public void setPreviewSize(BinaryDescriptor.PreviewSizes previewSize) {
         getSessionData().setPreviewSize(previewSize);
     }
 
     public ResultLocation getLocation() {
         return location;
     }
 
     public void setLocation(ResultLocation location) {
         this.location = location;
     }
 
     public ResultSessionData getSessionData() {
         if (sessionData == null) {
             sessionData = ResultSessionData.getSessionData(FxJsfUtils.getSession(), location);
         }
         return sessionData;
     }
 
     public long getBriefcaseAclId() {
         return briefcaseAclId;
     }
 
     public void setBriefcaseAclId(long briefcaseAclId) {
         this.briefcaseAclId = briefcaseAclId;
     }
 
     public String getBriefcaseDescription() {
         return briefcaseDescription;
     }
 
     public void setBriefcaseDescription(String briefcaseDescription) {
         this.briefcaseDescription = briefcaseDescription;
     }
 
     public String getBriefcaseName() {
         return briefcaseName;
     }
 
     public void setBriefcaseName(String briefcaseName) {
         this.briefcaseName = briefcaseName;
     }
 
     public Boolean getCreateBriefcase() {
         return createBriefcase;
     }
 
     public void setCreateBriefcase(Boolean createBriefcase) {
         this.createBriefcase = createBriefcase;
     }
 
     public FxSQLSearchParams.CacheMode getCacheMode() {
         return cacheMode;
     }
 
     public void setCacheMode(FxSQLSearchParams.CacheMode cacheMode) {
         this.cacheMode = cacheMode;
     }
 
     public Briefcase getBriefcase() throws FxApplicationException {
         final long briefcaseId = getSessionData().getBriefcaseId();
         if (briefcaseId == -1) {
             return null;
         }
         if (briefcase == null || briefcase.getId() != briefcaseId) {
             try {
                 briefcase = EJBLookup.getBriefcaseEngine().load(briefcaseId);
             } catch (FxNotFoundException e) {
                 getSessionData().setBriefcaseId(-1);
             }
         }
         return briefcase;
     }
 
     public long getBriefcaseId() throws FxApplicationException {
         return getSessionData().getBriefcaseId();
     }
 
     public VersionFilter getVersionFilter() {
         return getSessionData().getVersionFilter();
     }
 
     public void setVersionFilter(VersionFilter versionFilter) {
         getSessionData().setVersionFilter(versionFilter);
     }
 
     public List<SelectItem> getVersionItems() {
         final List<SelectItem> result = new ArrayList<SelectItem>(2);
         final MessageBean mb = MessageBean.getInstance();
         result.add(new SelectItem(VersionFilter.MAX, mb.getResource("SearchResult.label.version.max")));
         result.add(new SelectItem(VersionFilter.LIVE, mb.getResource("SearchResult.label.version.live")));
         return result;
     }
 
     /**
      * Returns the column key of the column that is currently used for sorting.
      *
      * @return  the column key of the column that is currently used for sorting. If null or empty,
      * the predefined sort order from the result preferences is used.
      * @since 3.1
      */
     public String getSortColumnKey() {
         return getSessionData().getSortColumnKey();
     }
 
     public void setSortColumnKey(String sortColumnKey) {
         getSessionData().setSortColumnKey(sortColumnKey);
     }
 
     /**
      * Return the sort direction as returned by the YUI datatable.
      *
      * @return  the sort direction as returned by the YUI datatable.
      * @since 3.1
      */
     public String getSortDirection() {
         return getSessionData().getSortDirection();
     }
 
     public void setSortDirection(String sortDirection) {
         getSessionData().setSortDirection(sortDirection);
     }
 
     /**
      * Return the page index of the paginator.
      *
      * @return  the page index of the paginator.
      * @since 3.1
      */
     public int getPaginatorPage() {
         return getSessionData().getPaginatorPage();
     }
 
     public void setPaginatorPage(int paginatorPage) {
         getSessionData().setPaginatorPage(paginatorPage);
     }
 
     public boolean isClearCache() {
         return false;
     }
 
     /**
      * Set marker flag to remove the current search result from the session cache.
      *
      * @param clearCache    marker flag to remove the current search result from the session cache.
      */
     public void setClearCache(boolean clearCache) {
         if (clearCache) {
             setResult(null);
         }
     }
 
     /**
      * @return the current folder ID, if the current result was built for a folder.
      * @since 3.1
      */
     public long getFolderId() {
         return getSessionData().getFolderId();
     }
 
     public void setFolderId(long folderId) {
         final boolean changed = folderId != getSessionData().getFolderId();
         getSessionData().setFolderId(folderId);
         if (changed) {
             subFolderItems = null;
             parentFolderId = -1;
             if (folderId != -1) {
                 createFolderQueryBuilder();
             }
         }
     }
 
     /**
      * @return  return the parent of folderId.
      */
     public long getParentFolderId() {
         if (parentFolderId == -1 && getFolderId() != -1) {
             if (getFolderId() == FxTreeNode.ROOT_NODE) {
                 parentFolderId = FxTreeNode.ROOT_NODE;
             } else {
                 try {
                     parentFolderId = EJBLookup.getTreeEngine().getNode(getTreeMode(), getFolderId()).getParentNodeId();
                 } catch (FxApplicationException e) {
                     new FxFacesMsgErr(e).addToContext();
                     parentFolderId = -1;
                 }
             }
         }
         return parentFolderId;
     }
 
 
     /**
      * @return  true if folder actions (e.g. removing objects) should be activated.
      * @since 3.1
      */
     public boolean isEnableFolderActions() {
         return getSessionData().isEnableFolderActions();
     }
 
     public void setEnableFolderActions(boolean enableFolderActions) {
         getSessionData().setEnableFolderActions(enableFolderActions);
     }
 
     /**
      * @return  a list of the (immediate) subfolders of the current folder
      * @since   3.1
      */
     public List<SelectItem> getSubFolderItems() {
         if (getFolderId() == -1) {
             return Lists.newArrayListWithCapacity(0);
         }
         if (subFolderItems == null) {
             final FxEnvironment env = CacheAdmin.getEnvironment();
             try {
                 subFolderItems = Lists.newArrayList();
                 // add all folder children
                 for (FxTreeNode node : EJBLookup.getTreeEngine().getTree(getTreeMode(), getFolderId(), 1).getChildren()) {
                     if (env.getType(node.getReferenceTypeId()).isDerivedFrom(FxType.FOLDER)) {
                         subFolderItems.add(new SelectItem(
                                 node.getId(),
                                 node.getLabel() == null ? node.getName() : node.getLabel().toString()
                         ));
                     }
                 }
             } catch (FxApplicationException e) {
                 new FxFacesMsgErr(e).addToContext();
             }
         }
         return subFolderItems;
     }
 
     /**
      * @return  the tree mode, if a folder query was submitted
      */
     public FxTreeMode getTreeMode() {
         return getSessionData().getTreeMode();
     }
 
     public void setTreeMode(FxTreeMode treeMode) {
         getSessionData().setTreeMode(treeMode);
     }
 
     /**
      * @return  true if only direct children should be returned for a folder query
      */
     public boolean isOnlyDirectChildren() {
         return getSessionData().isOnlyDirectChildren();
     }
 
     public void setOnlyDirectChildren(boolean value) {
         getSessionData().setOnlyDirectChildren(value);
     }
 
     /**
      * Reset the client-side table view parameters (sort, page number),
      * called e.g. when the content type filter changed.
      *
      * @since 3.1
      */
     @SuppressWarnings({"UnusedDeclaration"})
     public void resetTableView() {
 //        setSortColumnKey(null);   // remember sort column, if the column disappeared it has no effect on the result table
         setPaginatorPage(1);
     }
 
     /**
      * Returns the JSON representation of the given result set. Currently the column and row format is determined
      * by the Yahoo DataTable widget.
      *
      * @param key can be one of the following:
      * <ul>
      * <li>A {@link FxResultSet} instance</li>
      * <li>A FxSQL query, e.g. <code>SELECT id, caption</code></li>
      * <li>A {@link SqlQueryBuilder} instance</li>
      * </ul>
      * @param viewType "THUMBNAILS" or "LIST" (only relevant when a FxSQL query is passed)
      *
      * @return  the JSON representation of the search result
      * @throws java.io.IOException  if the JSON output could not be written
      */
     public static String getJsonResult(Object key, String viewType) throws IOException {
         return getJsonResult(key, viewType, -1);
     }
 
     /**
      * Returns the JSON representation of the given result set. Currently the column and row format is determined
      * by the Yahoo DataTable widget.
      *
      * @param key can be one of the following:
      * <ul>
      * <li>A {@link FxResultSet} instance</li>
      * <li>A FxSQL query, e.g. <code>SELECT id, caption</code></li>
      * <li>A {@link SqlQueryBuilder} instance</li>
      * </ul>
      * @param viewType "THUMBNAILS" or "LIST" (only relevant when a FxSQL query is passed)
      * @param startColumn   index of the start column (1-based, if set to -1 the index of "@*" will be used)
      *
      * @return  the JSON representation of the search result
      * @throws java.io.IOException  if the JSON output could not be written
      * @since 3.1
      */
     public static String getJsonResult(Object key, String viewType, int startColumn) throws IOException {
         if (key == null) {
             return null;
         }
         final ResultViewType resultViewType = StringUtils.isNotBlank(viewType)
                 ? ResultViewType.valueOf(viewType.toUpperCase())
                 : ResultViewType.LIST;
         final FxResultSet result = getResultSet(key, AdminResultLocations.DEFAULT, resultViewType);
         return YahooResultProvider.getSearchResultJSON(result,
                 startColumn != -1 ? startColumn
                         : result.getUserWildcardIndex() != -1 ? result.getUserWildcardIndex()
                         : 1
         );
     }
 
     /**
      * Return a FxResultSet stored in key. Possibly create a new
      * model wrapper if a plain result set was passed.
      *
      * @param key the (map) key to be used for creating the data model
      * @param location
      * @param viewType
      * @return a result set data model
      */
     private static FxResultSet getResultSet(Object key, ResultLocation location, ResultViewType viewType) {
         try {
             if (key instanceof FxResultSet) {
                 return (FxResultSet) key;
             } else if (key instanceof SqlQueryBuilder) {
                 return ((SqlQueryBuilder) key).getResult();
             } else if (key instanceof String) {
                 return EJBLookup.getSearchEngine().search((String) key, 0, Integer.MAX_VALUE, null,
                         location, viewType);
             }
             throw new IllegalArgumentException("Unknown query type in YahooResultProvider: "
                     + (key != null ? key.getClass() : null));
         } catch (FxApplicationException e) {
             throw e.asRuntimeException();
         }
     }
 }
