 /**
  * Acceso Inteligente
  *
  * Copyright (C) 2010-2011 Fundación Ciudadano Inteligente
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.accesointeligente.server;
 
 import org.accesointeligente.model.external.SolrResponse;
 import org.accesointeligente.server.solr.SolrClient;
 import org.accesointeligente.shared.RequestExpireType;
 import org.accesointeligente.shared.RequestSearchParams;
 import org.accesointeligente.shared.RequestStatus;
 
 import org.hibernate.Criteria;
 import org.hibernate.criterion.*;
 
 import com.google.gson.Gson;
 
 import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
 
 public class SearchParamParseUtil {
 
 	public static void criteriaAddSearchParams(Criteria criteria, RequestSearchParams params) {
 		if (params.getKeyWord() != null && !params.getKeyWord().equals("")) {
 			Conjunction keywordConjunction = Restrictions.conjunction();
 			Disjunction keyWordDisjunction;
 			String[] keyWords = params.getKeyWord().split("[ ]");
 
 			for (int i = 0; i < keyWords.length; i++) {
 				keyWords[i]  = keyWords[i].replaceAll("[ÁÀáà]","a");
 				keyWords[i]  = keyWords[i].replaceAll("[ÉÈéè]","e");
 				keyWords[i]  = keyWords[i].replaceAll("[ÍÌíì]","i");
 				keyWords[i]  = keyWords[i].replaceAll("[ÓÒóò]","o");
 				keyWords[i]  = keyWords[i].replaceAll("[ÚÙúù]","u");
 				keyWords[i]  = keyWords[i].replaceAll("[Ññ]", "n");
 				keyWords[i]  = keyWords[i].replaceAll("\\W", "");
 				keyWordDisjunction = Restrictions.disjunction();
 				keyWordDisjunction.add(Restrictions.ilike("information", keyWords[i], MatchMode.ANYWHERE));
 				keyWordDisjunction.add(Restrictions.ilike("context", keyWords[i], MatchMode.ANYWHERE));
 				keyWordDisjunction.add(Restrictions.ilike("title", keyWords[i], MatchMode.ANYWHERE));
 				keywordConjunction.add(keyWordDisjunction);
 			}
 			criteria.add(keywordConjunction);
 		}
 
 		if (params.getInstitution() != null) {
 			criteria.add(Restrictions.eq("institution", params.getInstitution()));
 		}
 
 		if (params.getMinDate() != null && params.getMaxDate() != null) {
 			criteria.add(Restrictions.between("confirmationDate", params.getMinDate(), params.getMaxDate()));
 		} else if (params.getMinDate() != null) {
 			criteria.add(Restrictions.ge("confirmationDate", params.getMinDate()));
 		} else if (params.getMaxDate() != null) {
 			criteria.add(Restrictions.le("confirmationDate", params.getMaxDate()));
 		}
 
 		if (params.getStatusClosed() || params.getStatusExpired() || params.getStatusPending()) {
 			Disjunction statusDisjunction = Restrictions.disjunction();
 
 			// Closed Marked
 			if (params.getStatusClosed()) {
 				statusDisjunction.add(Restrictions.eq("status", RequestStatus.RESPONDED));
 			}
 
 			// Expired Marked
 			if (params.getStatusExpired()) {
 				statusDisjunction.add(Restrictions.eq("expired", RequestExpireType.EXPIRED));
 			}
 
 			// Pending Marked
 			if (params.getStatusPending()) {
 				statusDisjunction.add(Restrictions.eq("status", RequestStatus.PENDING));
 				statusDisjunction.add(Restrictions.eq("status", RequestStatus.NEW));
 			}
 			criteria.add(statusDisjunction);
 		}
 	}
 
 	public static String queryAddSearchParams(RequestSearchParams params) {
 		String filters = "";
 
 		if (params.getKeyWord() != null && !params.getKeyWord().equals("")) {
 			filters += " AND (";
 			String[] keyWords = params.getKeyWord().split("[ ]");
 
 			for (int i = 0; i < keyWords.length; i++) {
 				if (i != 0) {
 					filters += " OR ";
 				}
 				keyWords[i]  = keyWords[i].replaceAll("\\W", "");
 				filters += " lower(TRANSLATE(information,'ÁáÉéÍíÓóÚúÑñ','AaEeIiOoUuNn')) like lower('%" + keyWords[i] + "%') OR ";
 				filters += " lower(TRANSLATE(context,'ÁáÉéÍíÓóÚúÑñ','AaEeIiOoUuNn')) like lower('%" + keyWords[i] + "%') OR ";
 				filters += " lower(TRANSLATE(title,'ÁáÉéÍíÓóÚúÑñ','AaEeIiOoUuNn')) like lower('%" + keyWords[i] + "%') ";
 			}
 			filters += ") ";
 		}
 
 		if (params.getInstitution() != null) {
 			filters += " AND (institutionId = " + params.getInstitution().getId() + ") ";
 		}
 
 		if (params.getMinDate() != null && params.getMaxDate() != null) {
 			filters += " AND confirmationDate BETWEEN :minDate AND :maxDate";
 		} else if (params.getMinDate() != null) {
 			filters += " AND confirmationDate >= :minDate ";
 		} else if (params.getMaxDate() != null) {
 			filters += " AND confirmationDate <= :maxDate ";
 		}
 
 		if (params.getStatusClosed() || params.getStatusPending()) {
 			filters += " AND status IN (";
 
 			// Closed Marked
 			if (params.getStatusClosed()) {
 				filters += "'" + RequestStatus.RESPONDED.toString() + "',";
 			}
 
 			// Pending Marked
 			if (params.getStatusPending()) {
 				filters += "'" + RequestStatus.PENDING.toString() + "',";
 				filters += "'" + RequestStatus.NEW.toString() + "'";
 			}
 
 			if (filters.charAt(filters.length() - 1) == ',') {
 				filters = filters.substring(0, filters.length() - 2);
 			}
 
 			filters += " )";
 		}
 
 		if (params.getStatusExpired()) {
 			// Expired Marked
 			if (params.getStatusExpired()) {
 				filters += " AND expired = '" + RequestExpireType.EXPIRED.toString() + "' ";
 			}
 		}
 
 		return filters;
 	}
 
 	public static Long solrCriteriaAddSearchParams(Criteria criteria, RequestSearchParams params, Integer offset, Integer limit) throws Exception {
 		Long totalResults = 0L;
 		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
 		String jsonSolrQuery = "q=(";
 
 		if (params.getKeyWord() != null && !params.getKeyWord().equals("")) {
 			jsonSolrQuery += "(";
			jsonSolrQuery += "title:" + params.getKeyWord();
			jsonSolrQuery += " OR information:" + params.getKeyWord();
			jsonSolrQuery += " OR context:" + params.getKeyWord();
 			jsonSolrQuery += ")";
 		} else {
 			jsonSolrQuery += "(";
 			jsonSolrQuery += "title:*";
 			jsonSolrQuery += " OR information:*";
 			jsonSolrQuery += " OR context:*";
 			jsonSolrQuery += ")";
 		}
 
 		if (params.getInstitution() != null) {
 			jsonSolrQuery += " OR institutionId:" + params.getInstitution().getId().toString();
 		}
 
 
 		if (params.getMinDate() != null && params.getMaxDate() != null) {
 			jsonSolrQuery += " AND confirmationDate:[ " + formatter.format(params.getMinDate()) + " TO " + formatter.format(params.getMaxDate()) + "]";
 		} else if (params.getMinDate() != null) {
 			jsonSolrQuery += " AND confirmationDate:[ " + formatter.format(params.getMinDate()) + " TO * ]";
 		} else if (params.getMaxDate() != null) {
 			jsonSolrQuery += " AND confirmationDate:[ * TO " + formatter.format(params.getMaxDate()) + "]";
 		}
 
 		if (params.getStatusClosed() || params.getStatusPending()) {
 			jsonSolrQuery += " AND (status:(";
 
 			// Closed Marked
 			if (params.getStatusClosed()) {
 				jsonSolrQuery += " " + RequestStatus.RESPONDED.toString() + " ";
 			}
 
 			// Pending Marked
 			if (params.getStatusPending()) {
 				jsonSolrQuery += " " + RequestStatus.PENDING.toString() + " ";
 				jsonSolrQuery += " " + RequestStatus.NEW.toString() + " ";
 			}
 			jsonSolrQuery += "))";
 		}
 
 		jsonSolrQuery += ")";
 		jsonSolrQuery += "&start=" + offset.toString();
 		jsonSolrQuery += "&rows=" + limit.toString();
 		jsonSolrQuery += "&fl=id";
 		jsonSolrQuery += "&wt=json";
 
 		if (params.getKeyWord() == null || params.getKeyWord().equals("")) {
 			jsonSolrQuery += "&sort=numericId asc";
 		}
 
 		Gson gsonEncoder = new Gson();
 		String jsonResponse = null;
 
 		try {
 			// First we get the total of requests
 			jsonResponse = SolrClient.execQuery(jsonSolrQuery);
 			SolrResponse solrResponse = gsonEncoder.fromJson(jsonResponse, SolrResponse.class);
 			totalResults = new Long(solrResponse.getResponse().getNumFound());
 			List<Integer> requestIds = new ArrayList<Integer>();
 
 			for (org.accesointeligente.model.external.SolrResponse.Response.Document responseDocument : solrResponse.getResponse().getDocs()) {
 				requestIds.add(responseDocument.getId());
 			}
 
 			criteria.add(Restrictions.in("id", requestIds));
 
 		} catch (Exception ex) {
 			throw ex;
 		}
 
 		return totalResults;
 	}
 }
