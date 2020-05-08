 package com.optaros.alfresco.docasu.wcs.helper;
 
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
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.Properties;
 
 import org.alfresco.model.ContentModel;
import org.alfresco.repo.search.impl.lucene.QueryParser;
 import org.alfresco.service.cmr.repository.NodeRef;
 
 import com.optaros.alfresco.docasu.wcs.CustomFileFolderService.SearchType;
 
 /**
  * @author Viorel Andronic
  * 
  */
 public class QueryManager {
 
 	private static final String LUCENE_BASE_QUERY = "-TYPE:\"" + ContentModel.TYPE_SYSTEM_FOLDER + "\"";
 
 	private static final String MIN_DATE = "1970\\-01\\-01T00:00:00";
 	private static final String MAX_DATE = "3000\\-12\\-31T00:00:00";
 
 	private static final String LUCENE_DATE_FORMAT = "yyyy\\-MM\\-dd'T00:00:00'";
 	private final SimpleDateFormat dateFormat = new SimpleDateFormat(LUCENE_DATE_FORMAT);
 
 	/**
 	 * Construct a filtered query with the values from blacklist.properties and
 	 * whitelist.properties
 	 */
 	@SuppressWarnings("unchecked")
 	private StringBuffer prepareQuery(StringBuffer query, Properties whitelist, Properties blacklist) {
 		// get all the values in blacklist.properties to build the filtered
 		// query
 		// Exclude all from blacklist (AND)
 		for (Iterator iterator = blacklist.values().iterator(); iterator.hasNext();) {
 			String value = (String) iterator.next();
 			query.append(" -TYPE:\"" + value + "\"");
 		}
 		// Include (at least) one form whitelist (OR)
 		query.append(" +(");
 		for (Iterator iterator = whitelist.values().iterator(); iterator.hasNext();) {
 			String value = (String) iterator.next();
 			query.append(" TYPE:\"" + value + "\"");
 		}
 		query.append(" )");
 		return query;
 	}
 
 	public String createListQuery(NodeRef contextNodeRef, boolean foldersOnly, Properties whitelist, Properties blacklist) {
 		StringBuffer query = prepareQuery(new StringBuffer(LUCENE_BASE_QUERY), whitelist, blacklist);
 
 		query.append(" +PARENT:\"" + contextNodeRef.toString() + "\"");
 
 		if (foldersOnly) {
 			query.append(" -TYPE:\"" + ContentModel.TYPE_CONTENT + "\"");
 		}
 
 		return query.toString();
 	}
 
 	public String createCategoryQuery(String categoryName, Properties whitelist, Properties blacklist) {
 		StringBuffer query = new StringBuffer();
 		/* we need // as wild-card to avoid writing full path */
 		/* all spaces should be replaced with _x0020_ */
 		query.append("+PATH:\"/cm:generalclassifiable//cm:" + categoryName.replaceAll(" ", "_x0020_") + "//member\"");
 		return query.toString();
 	}
 
 	public String createSearchQuery(String query, SearchType type, NodeRef lookInFolder, Date createdFrom, Date createdTo, Date modifiedFrom, Date modifiedTo,
 			Properties whitelist, Properties blacklist) {
 		StringBuffer luceneQuery = prepareQuery(new StringBuffer(LUCENE_BASE_QUERY), whitelist, blacklist);
 
 		if (query != null && query.length() > 0) {
 			// Escape Lucene characters.
			query = QueryParser.escape(query);
 
 			if (type == SearchType.ALL) {
 				luceneQuery.append(" +(TEXT:\"" + query + "\" @cm\\:name:\"" + query + "\")");
 			} else if (type == SearchType.FILE_NAME || type == SearchType.FOLDER_NAME) {
 				luceneQuery.append(" +@cm\\:name:\"" + query + "\"");
 			} else if (type == SearchType.CONTENT) {
 				luceneQuery.append(" +TEXT:\"" + query + "\"");
 			}
 		}
 
 		if (type == SearchType.FILE_NAME) {
 			luceneQuery.append(" +TYPE:\"" + ContentModel.TYPE_CONTENT + "\"");
 		} else if (type == SearchType.FOLDER_NAME) {
 			luceneQuery.append(" +TYPE:\"" + ContentModel.TYPE_FOLDER + "\"");
 		} else if (type == SearchType.CONTENT) {
 			luceneQuery.append(" +TYPE:\"" + ContentModel.TYPE_CONTENT + "\"");
 		}
 
 		if (lookInFolder != null) {
 			luceneQuery.append(" +PARENT:\"" + lookInFolder.toString() + "\"");
 		}
 
 		if (createdFrom != null || createdTo != null) {
 			writeDateRange(luceneQuery, "created", createdFrom, createdTo);
 		}
 		if (modifiedFrom != null || modifiedTo != null) {
 			writeDateRange(luceneQuery, "modified", modifiedFrom, modifiedTo);
 		}
 
 		return luceneQuery.toString();
 	}
 
 	private void writeDateRange(StringBuffer luceneQuery, String field, Date fromDate, Date toDate) {
 		String from, to;
 		if (fromDate != null) {
 			from = dateFormat.format(fromDate);
 		} else {
 			from = MIN_DATE;
 		}
 		if (toDate != null) {
 			to = dateFormat.format(toDate);
 		} else {
 			to = MAX_DATE;
 		}
 		luceneQuery.append(" +@cm\\:" + field + ":[" + from + " TO " + to + "]");
 	}
 
 }
