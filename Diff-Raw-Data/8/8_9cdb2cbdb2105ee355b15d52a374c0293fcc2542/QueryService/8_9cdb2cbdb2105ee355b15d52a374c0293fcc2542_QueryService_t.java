 package com.cnnic.whois.service;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.cnnic.whois.bean.DomainQueryParam;
 import com.cnnic.whois.bean.EntityQueryParam;
 import com.cnnic.whois.bean.IpQueryParam;
 import com.cnnic.whois.bean.PageBean;
 import com.cnnic.whois.bean.QueryParam;
 import com.cnnic.whois.bean.QueryType;
 import com.cnnic.whois.dao.CacheQueryDAO;
 import com.cnnic.whois.dao.QueryDAO;
 import com.cnnic.whois.dao.QueryEngine;
 import com.cnnic.whois.execption.QueryException;
 import com.cnnic.whois.execption.RedirectExecption;
 import com.cnnic.whois.util.WhoisProperties;
 import com.cnnic.whois.util.WhoisUtil;
 
 public class QueryService {
 	private static QueryService queryService = new QueryService();
 	private QueryDAO queryDAO = QueryDAO.getQueryDAO();
 	private CacheQueryDAO cache = CacheQueryDAO.getQueryDAO();
 	private QueryEngine queryEngine = QueryEngine.getEngine();
 	public static int MAX_SIZE_FUZZY_QUERY = WhoisProperties
 			.getMaxSizeFuzzyQuery();
 
 	public static QueryService getQueryService() {
 		return queryService;
 	}
 
 	public Map<String, Object> queryIP(String ipInfo, int ipLength,
 			String role, String format) throws QueryException,
 			RedirectExecption {
 		long[] ipLongs = WhoisUtil.parsingIp(ipInfo, ipLength);
 		Map map = queryEngine.query(QueryType.IP, new IpQueryParam("",ipLongs[0], ipLongs[1], ipLongs[2],
 				ipLongs[3]), role, format);
 
 		if (map == null) {
 			queryEngine.query(QueryType.IPREDIRECTION, 
 					new IpQueryParam("",ipLongs[0], ipLongs[1], ipLongs[2],ipLongs[3]), role, format);
 			return queryError("404", role, format);
 		}
 
 		if ((map.get("$mul$IP") instanceof Object[])) {
 			Object[] mapObj = (Object[]) map.get("$mul$IP");
 			List list = new ArrayList();
 			Map mapInfo = new LinkedHashMap();
 
 			mapInfo.put("$mul$IP", list.toArray());
 			return mapInfo;
 		}
 		return map;
 	}
 
 	private Map<String, Object> longToIP(Map<String, Object> map) {
 		Object ipversion = map.get("IP Version");
 
 		String startHightAddress = map.get("StartHighAddress").toString();
 		String startLowAddress = map.get("StartLowAddress").toString();
 		String endHighAddress = map.get("EndHighAddress").toString();
 		String endLowAddress = map.get("EndLowAddress").toString();
 
 		map.remove("StartHighAddress");
 		map.remove("StartLowAddress");
 		map.remove("EndHighAddress");
 		map.remove("EndLowAddress");
 		String startAddress = "";
 		String endAddress = "";
 		if (ipversion != null) {
 			if (ipversion.toString().indexOf("v6") != -1) {
 				startAddress = WhoisUtil.ipV6ToString(
 						Long.parseLong(startHightAddress),
 						Long.parseLong(startLowAddress));
 				endAddress = WhoisUtil.ipV6ToString(
 						Long.parseLong(endHighAddress),
 						Long.parseLong(endLowAddress));
 			} else {
 				startAddress = WhoisUtil.longtoipV4(Long
 						.parseLong(startLowAddress));
 				endAddress = WhoisUtil
 						.longtoipV4(Long.parseLong(endLowAddress));
 			}
 			map.put("Start Address", startAddress);
 			map.put("End Address", endAddress);
 		}
 		return map;
 	}
 
 	public Map<String, Object> queryAS(int asInfo, String role, String format)
 			throws QueryException, RedirectExecption {
 		Map map = queryEngine.query(QueryType.AUTNUM, new QueryParam(asInfo+""), role, format);
 		if (map == null) {
 			getRedirectionURL("autnum", Integer.toString(asInfo));
 			return queryError("404", role, format);
 		}
 
 		return map;
 	}
 
 	public Map<String, Object> queryNameServer(String ipInfo, String role,
 			String format) throws QueryException, RedirectExecption {
 		Map map = queryEngine.query(QueryType.NAMESERVER, new QueryParam(ipInfo), role, format);
 		if (map == null) {
 			return queryError("404", role, format);
 		}
 		return map;
 	}
 
 	public Map<String, Object> fuzzyQueryNameServer(String nameServer,
 			String role, String format, PageBean page) throws QueryException,
 			RedirectExecption {
 		Map dnrMap = queryEngine.query(QueryType.SEARCHNS,
 				new QueryParam(nameServer), role, format,page);
 		if (dnrMap == null) {
 			return queryError("404", role, format);
 		}
 		return dnrMap;
 	}
 
 	public Map<String, Object> fuzzyQueryDomain(String domain,
 			String domainPuny, String role, String format, PageBean page)
 			throws QueryException, RedirectExecption {
 		Map dnrMap = queryEngine.query(QueryType.SEARCHDOMAIN, 
 				new DomainQueryParam(domain,domainPuny), role, format,page);
 		if (dnrMap == null) {
 			return queryError("404", role, format);
 		}
 		return dnrMap;
 	}
 
 	public Map<String, Object> queryDomain(String ipInfo, String role,
 			String format) throws QueryException, RedirectExecption {
 		Map rirMap = queryEngine.query(QueryType.RIRDOMAIN, new QueryParam(ipInfo), role, format);
 		//cache.queryDoamin("", role, format);
 		Map dnrMap = queryEngine.query(QueryType.DNRDOMAIN, new QueryParam(ipInfo), role, format);
 		//this.queryDAO.queryDNRDoamin(ipInfo, role, format);
 
 		if ((rirMap == null) && (dnrMap == null)) {
 			String queryType = "dnrdomain";
 
 			if (rirMap == null)
 				queryType = "rirdomain";
 			getRedirectionURL(queryType, ipInfo);
 			return queryError("404", role, format);
 		}
 
 		Map wholeMap = new LinkedHashMap();
 		if (rirMap != null) {
 			wholeMap.putAll(rirMap);
 		}
 
 		if (dnrMap != null) {
 			wholeMap.putAll(dnrMap);
 		}
 
 		return wholeMap;
 	}
 
 	public Map<String, Object> queryEntity(String queryPara, String role,
 			String format) throws QueryException, SQLException {
 		try {
 			Map map = queryEngine.query(QueryType.ENTITY, new QueryParam(queryPara), role, format);
 			if (map == null) {
 				return queryError("404", role, format);
 			}
 			return map;
 		} catch (RedirectExecption e) {
 			throw new QueryException(e);
 		}
 	}
 
 	public Map<String, Object> fuzzyQueryEntity(String fuzzyQueryParamName,
 			String queryPara, String role, String format, PageBean page)
 			throws QueryException, SQLException {
 		try {
			Map map = queryEngine.query(QueryType.SEARCHENTITY, 
 					new EntityQueryParam(queryPara,fuzzyQueryParamName), role, format,page);
 			if (map == null) {
 				return queryError("404", role, format);
 			}
 			return map;
 		} catch (RedirectExecption e) {
 			throw new QueryException(e);
 		}
 	}
 
 	public Map<String, Object> queryLinks(String queryPara, String role,
 			String format) throws QueryException {
 		try {
 			Map map = queryEngine.query(QueryType.LINKS, new QueryParam(queryPara), role, format);
 			if (map == null) {
 				return queryError("404", role, format);
 			}
 			return map;
 		} catch (RedirectExecption e) {
 			throw new QueryException(e);
 		}
 	}
 
 	public Map<String, Object> queryPhones(String queryPara, String role,
 			String format) throws QueryException {
 		try {
 			Map map = queryEngine.query(QueryType.PHONES, new QueryParam(queryPara), role, format);
 			if (map == null) {
 				return queryError("404", role, format);
 			}
 			return map;
 		} catch (RedirectExecption e) {
 			throw new QueryException(e);
 		}
 	}
 
 	public Map<String, Object> queryPostalAddress(String queryPara,
 			String role, String format) throws QueryException {
 		try {
 			Map map = queryEngine.query(QueryType.POSTALADDRESS, new QueryParam(queryPara), role, format);
 			if (map == null) {
 				return queryError("404", role, format);
 			}
 			return map;
 		} catch (RedirectExecption e) {
 			throw new QueryException(e);
 		}
 	}
 
 	public Map<String, Object> queryVariants(String queryPara, String role,
 			String format) throws QueryException {
 		try {
 			Map map = queryEngine.query(QueryType.VARIANTS, new QueryParam(queryPara), role, format);
 			if (map == null) {
 				return queryError("404", role, format);
 			}
 			return map;
 		} catch (RedirectExecption e) {
 			throw new QueryException(e);
 		}
 	}
 
 	public Map<String, Object> querySecureDNS(String queryPara, String role,
 			String format) throws QueryException {
 		try {
 			Map map = queryEngine.query(QueryType.SECUREDNS, new QueryParam(queryPara), role, format);
 			if (map == null) {
 				return queryError("404", role, format);
 			}
 			return map;
 		} catch (RedirectExecption e) {
 			throw new QueryException(e);
 		}
 	}
 
 	public Map<String, Object> queryDsData(String queryPara, String role,
 			String format) throws QueryException {
 		try {
 			Map map = queryEngine.query(QueryType.DSDATA, new QueryParam(queryPara), role, format);
 			if (map == null) {
 				return queryError("404", role, format);
 			}
 			return map;
 		} catch (RedirectExecption e) {
 			throw new QueryException(e);
 		}
 	}
 
 	public Map<String, Object> queryKeyData(String queryPara, String role,
 			String format) throws QueryException {
 		try {
 			Map map = queryEngine.query(QueryType.KEYDATA, new QueryParam(queryPara), role, format);
 			if (map == null) {
 				return queryError("404", role, format);
 			}
 			return map;
 		} catch (RedirectExecption e) {
 			throw new QueryException(e);
 		}
 	}
 
 	public Map<String, Object> queryDelegationKeys(String queryPara,
 			String role, String format) throws QueryException {
 		try {
 			Map map = queryEngine.query(QueryType.DELETATIONKEY, new QueryParam(queryPara), role, format);
 			if (map == null) {
 				return queryError("404", role, format);
 			}
 			return map;
 		} catch (RedirectExecption e) {
 			throw new QueryException(e);
 		}
 	}
 
 	public Map<String, Object> queryNotices(String queryPara, String role,
 			String format) throws QueryException {
 		try {
 			Map map = queryEngine.query(QueryType.NOTICES, new QueryParam(queryPara), role, format);
 			if (map == null) {
 				return queryError("404", role, format);
 			}
 			return map;
 		} catch (RedirectExecption e) {
 			throw new QueryException(e);
 		}
 	}
 
 	public Map<String, Object> queryRegistrar(String queryPara, String role,
 			boolean isJoinTable, String format) throws QueryException {
 		Map map = this.queryDAO.queryRegistrar(queryPara, role, isJoinTable,
 				format);
 
 		if (map == null) {
 			return queryError("404", role, format);
 		}
 
 		return map;
 	}
 
 	public Map<String, Object> queryRemarks(String queryPara, String role,
 			String format) throws QueryException {
 		try {
 			Map map = queryEngine.query(QueryType.REMARKS, new QueryParam(queryPara), role, format);
 			if (map == null) {
 				return queryError("404", role, format);
 			}
 			return map;
 		} catch (RedirectExecption e) {
 			throw new QueryException(e);
 		}
 	}
 
 	public Map<String, Object> queryEvents(String queryPara, String role,
 			String format) throws QueryException {
 		try {
 			Map map = queryEngine.query(QueryType.EVENTS, new QueryParam(queryPara), role, format);
 			if (map == null) {
 				return queryError("404", role, format);
 			}
 			return map;
 		} catch (RedirectExecption e) {
 			throw new QueryException(e);
 		}
 	}
 
 	public Map<String, Object> queryError(String errorCode, String role,
 			String format) throws QueryException {
 		try {
 			Map ErrorMessageMap = null;
 			ErrorMessageMap = queryEngine.query(QueryType.ERRORMSG, new QueryParam(errorCode), role, format);
 			return ErrorMessageMap;
 		} catch (RedirectExecption e) {
 			throw new QueryException(e);
 		}
 	}
 
 	public Map<String, Object> queryHelp(String helpCode, String role,
 			String format) throws QueryException {
 		try {
 			Map helpMap = null;
 			helpMap = queryEngine.query(QueryType.HELP, new QueryParam(helpCode), role, format);
 			return helpMap;
 		} catch (RedirectExecption e) {
 			throw new QueryException(e);
 		}
 	}
 
 	private void getRedirectionURL(String queryType, String queryPara)
 			throws QueryException, RedirectExecption {
 		this.queryDAO.queryRedirection(queryType, queryPara);
 	}
 }
