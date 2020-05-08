 package com.optaros.alfresco.docasu.wcs;
 
 /*
  *    Copyright (C) 2008 Optaros, Inc. All rights reserved.
  *
  *    This program is free software: you can redistribute it and/or modify
  *    it under the terms of the GNU General Public License as published by
  *    the Free Software Foundation, either version 3 of the License, or
  *    (at your option) any later version.
  *
  *    This program is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  *    GNU General Public License for more details.
  *
  *    You should have received a copy of the GNU General Public License
  *    along with this program. If not, see <http://www.gnu.org/licenses/>.
  *
  */
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.alfresco.repo.security.permissions.AccessDeniedException;
 import org.alfresco.service.cmr.model.FileInfo;
 import org.alfresco.service.cmr.repository.NodeRef;
 import org.alfresco.service.cmr.security.AccessStatus;
 import org.alfresco.service.cmr.security.PermissionService;
 import org.alfresco.web.scripts.WebScriptRequest;
 import org.alfresco.web.scripts.Status;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.optaros.alfresco.docasu.wcs.CustomFileFolderService.SearchType;
 
 /**
  * @author Jean-Luc Geering
  */
 public class Search extends AbstractDocumentWebScript {
 	private static final Log log = LogFactory.getLog(Search.class);
 
 	// maximum size for a search result set
 	private static final int RESULT_SET_MAX_SIZE = 500;
 
 	// simple search
 	private static final String PARAM_QUERY = "q";
 	private static final String PARAM_SEARCH_TYPE = "t";
 
 	// advanced search
 	private static final String PARAM_CREATED_FROM = "createdFrom";
 	private static final String PARAM_CREATED_TO = "createdTo";
 	private static final String PARAM_MODIFIED_FROM = "modFrom";
 	private static final String PARAM_MODIFIED_TO = "modTo";
 
 	private static final String DATE_FORMAT = "yyyy/MM/dd";
 
 	private final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
 
 	@Override
 	protected Map<String, String> readParams(WebScriptRequest req) {
 
 		Map<String, String> params = super.readParams(req);
 
 		readParam(params, PARAM_QUERY, req.getParameter(PARAM_QUERY));
 		readParam(params, PARAM_SEARCH_TYPE, req.getParameter(PARAM_SEARCH_TYPE));
 		readParam(params, PARAM_CREATED_FROM, req.getParameter(PARAM_CREATED_FROM));
 		readParam(params, PARAM_CREATED_TO, req.getParameter(PARAM_CREATED_TO));
 		readParam(params, PARAM_MODIFIED_FROM, req.getParameter(PARAM_MODIFIED_FROM));
 		readParam(params, PARAM_MODIFIED_TO, req.getParameter(PARAM_MODIFIED_TO));
 
 		if (log.isDebugEnabled()) {
 			log.debug("PARAM q = " + params.get(PARAM_QUERY));
 			log.debug("PARAM t = " + params.get(PARAM_SEARCH_TYPE));
 			log.debug("PARAM createdFrom = " + params.get(PARAM_CREATED_FROM));
 			log.debug("PARAM createdTo = " + params.get(PARAM_CREATED_TO));
 			log.debug("PARAM modFrom = " + params.get(PARAM_MODIFIED_FROM));
 			log.debug("PARAM modTo = " + params.get(PARAM_MODIFIED_TO));
 		}
 
 		return params;
 	}
 
 	private boolean validParams(Map<String, String> params) {
 		return params.containsKey(PARAM_QUERY) || isAdvancedSearch(params);
 	}
 
 	private boolean isAdvancedSearch(Map<String, String> params) {
 		return (params.containsKey(PARAM_NODE_ID) || params.containsKey(PARAM_CREATED_FROM) || params.containsKey(PARAM_CREATED_TO)
 				|| params.containsKey(PARAM_MODIFIED_FROM) || params.containsKey(PARAM_MODIFIED_TO));
 	}
 
 	@Override
 	public Map<String, Object> executeImpl(WebScriptRequest req, Status status) {
 		log.debug("*** Enter search request handler ***");
 
 		initServices();
 
 		Map<String, String> params = readParams(req);
 		Map<String, Object> model = new HashMap<String, Object>();
 
 		if (!validParams(params)) {
 			status.setMessage("Invalid search parameters");
 			status.setRedirect(true);
 			log.debug("Invalid search parameters");
 		} else {
 			String searchTypeParam = params.get(PARAM_SEARCH_TYPE);
 			SearchType searchType = getSearchType(searchTypeParam);
 
 			// search result set
 			List<NodeRef> searchResult = getSearchResult(params, searchType);
 
 			// filter result set
 			searchResult = filterSearchResult(searchResult);
 
 			// sort results
 			searchResult = sort(searchResult, params);
 
 			// store the size of the search result
 			int total = searchResult.size();
 
 			// restrict result set to page set
 			searchResult = doPaging(searchResult, params);
 
 			// transform results
 			List<FileInfo> nodes = toFileInfo(searchResult);
 
 			model.put("randomNumber", Math.random());
 			model.put("total", total);
 			model.put(KEYWORD_ROWS, getResultRows(nodes));
 			
 			model.put("success", true);
 			model.put("msg", "Search was successful");
 			log.debug("Search was successful");
 		}
 
 		log.debug("*** Exit search request handler ***");
 		return model;
 	}
 
 	/**
 	 * This method removes all inaccessible nodes from the result list and cuts
 	 * result set to RESULT_SET_MAX_SIZE records.
 	 */
 	private List<NodeRef> filterSearchResult(List<NodeRef> searchResult) {
 		int i = 0;
 		// remove inaccessible records from search result
 		while (i < searchResult.size() && i < RESULT_SET_MAX_SIZE) {
			if (permissionService.hasPermission(searchResult.get(i), PermissionService.READ) == AccessStatus.ALLOWED) {
 				// maintain node in search result
 				// increase step value
 				i++;
 			} else {
 				// remove node from search result
 				searchResult.remove(i);
 				// keep step value
 			}
 		}
 		// cut search result to match size == RESULT_SET_MAX_SIZE
 		if (searchResult.size() > RESULT_SET_MAX_SIZE) {
 			searchResult = searchResult.subList(0, RESULT_SET_MAX_SIZE);
 		}
 		return searchResult;
 	}
 
 	private Object[] getResultRows(List<FileInfo> nodes) {
 		int i = 0;
 		Object[] rows = new Object[nodes.size()];
 		for (FileInfo info : nodes) {
 			try {
 				Map<String, Object> row = toModelRow(info);
 				NodeRef parentRef = nodeService.getPrimaryParent(info.getNodeRef()).getParentRef();
 				if (parentRef != null) {
 					row.put("parentId", parentRef.getId());
 					row.put("parentPath", generatePath(parentRef));
 				}
 				rows[i++] = row;
 			} catch (AccessDeniedException ade) {/* Ignore node. */
 			}
 		}
 		return rows;
 	}
 
 	private List<NodeRef> getSearchResult(Map<String, String> params, SearchType searchType) {
 		List<NodeRef> searchResult = new ArrayList<NodeRef>();
 		if (!isAdvancedSearch(params)) {
 			// simple search
 			searchResult = customFileFolderService.search(storeRef, params.get(PARAM_QUERY), searchType);
 		} else {
 			// advanced search
 			NodeRef lookInFolder = null;
 			if (params.containsKey(PARAM_NODE_ID)) {
 				lookInFolder = new NodeRef(storeRef, params.get(PARAM_NODE_ID));
 			}
 
 			// advanced search parameters
 			Map<String, Date> advancedSearchParams = getAdvancedSearchParams(params);
 			Date createdFrom = advancedSearchParams.get("createdFrom");
 			Date createdTo = advancedSearchParams.get("createdTo");
 			Date modifiedFrom = advancedSearchParams.get("modifiedFrom");
 			Date modifiedTo = advancedSearchParams.get("modifiedTo");
 
 			searchResult = customFileFolderService.search(storeRef, params.get(PARAM_QUERY), searchType, lookInFolder, createdFrom, createdTo, modifiedFrom,
 					modifiedTo);
 		}
 		return searchResult;
 	}
 
 	private Map<String, Date> getAdvancedSearchParams(Map<String, String> params) {
 		Map<String, Date> result = new HashMap<String, Date>();
 
 		if (params.containsKey(PARAM_CREATED_FROM)) {
 			try {
 				// TODO transform user timezone to server timezone
 				result.put("createdFrom", dateFormat.parse(params.get(PARAM_CREATED_FROM)));
 			} catch (ParseException e) {/* TODO ignore? */
 			}
 		}
 		if (params.containsKey(PARAM_CREATED_TO)) {
 			try {
 				// TODO transform user timezone to server timezone
 				result.put("createdTo", dateFormat.parse(params.get(PARAM_CREATED_TO)));
 			} catch (ParseException e) {/* TODO ignore? */
 			}
 		}
 		if (params.containsKey(PARAM_MODIFIED_FROM)) {
 			try {
 				// TODO transform user timezone to server timezone
 				result.put("modifiedFrom", dateFormat.parse(params.get(PARAM_MODIFIED_FROM)));
 			} catch (ParseException e) {/* TODO ignore? */
 			}
 		}
 		if (params.containsKey(PARAM_MODIFIED_TO)) {
 			try {
 				// TODO transform user timezone to server timezone
 				result.put("modifiedTo", dateFormat.parse(params.get(PARAM_MODIFIED_TO)));
 			} catch (ParseException e) {/* TODO ignore? */
 			}
 		}
 
 		return result;
 	}
 
 	private SearchType getSearchType(String searchTypeParam) {
 		if ("file".equals(searchTypeParam)) {
 			return SearchType.FILE_NAME;
 		} else if ("folder".equals(searchTypeParam)) {
 			return SearchType.FOLDER_NAME;
 		} else if ("content".equals(searchTypeParam)) {
 			return SearchType.CONTENT;
 		} else {
 			return SearchType.ALL;
 		}
 	}
 
 }
