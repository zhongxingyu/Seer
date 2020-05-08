 package com.cnnic.whois.controller;
 
 import java.io.IOException;
 import java.net.IDN;
 import java.sql.SQLException;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.lang.StringUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.annotation.ResponseStatus;
 
 import com.cnnic.whois.bean.DomainQueryParam;
 import com.cnnic.whois.bean.EntityQueryParam;
 import com.cnnic.whois.bean.IpQueryParam;
 import com.cnnic.whois.bean.QueryParam;
 import com.cnnic.whois.bean.QueryType;
 import com.cnnic.whois.dao.query.QueryEngine;
 import com.cnnic.whois.execption.QueryException;
 import com.cnnic.whois.execption.RedirectExecption;
 import com.cnnic.whois.service.QueryService;
 import com.cnnic.whois.util.WhoisUtil;
 import com.cnnic.whois.util.validate.ValidateUtils;
 
 @Controller
 @RequestMapping("/{dot}well-known/rdap")
 public class QueryController extends BaseController {
 	@Autowired
 	private QueryService queryService;
 	@Autowired
 	private QueryEngine queryEngine;
 
 	@RequestMapping(value = { "/", "" }, method = RequestMethod.GET)
 	public String index() {
 		return "/doc/index";
 	}
 
 	@RequestMapping(value = "/domains", method = RequestMethod.GET)
 	@ResponseBody
 	public void fuzzyQueryDomain(@RequestParam(required = true) String name,
 			HttpServletRequest request, HttpServletResponse response)
 			throws QueryException, RedirectExecption, IOException,
 			ServletException {
 		name = WhoisUtil.urlDecode(name);
 		name = StringUtils.trim(name);
 		Map<String, Object> resultMap = null;
 		DomainQueryParam domainQueryParam = super
 				.praseDomainQueryParams(request);
 		request.setAttribute("queryType", "domain");
 		name = super.getNormalization(name);
 		if ("*".equals(name)) {
 			super.renderResponseError422(request, response);
 			return;
 		}
 		name = WhoisUtil.getLowerCaseByLabel(name);
 		String punyDomainName = name;
 		try {
 			punyDomainName = IDN.toASCII(name);// long lable exception/not utf8 exception
 		} catch (Exception e) {
 			super.renderResponseError400(request, response);
 			return;
 		}
 		request.setAttribute("queryPara", IDN.toUnicode(punyDomainName));
 		if (!ValidateUtils.isCommonInvalidStr(punyDomainName)) {
 			super.renderResponseError400(request, response);
 			return;
 		}
 		domainQueryParam.setQueryType(QueryType.SEARCHDOMAIN);
 		domainQueryParam.setQ(name);
 		domainQueryParam.setDomainPuny(punyDomainName);
 		setMaxRecordsForFuzzyQ(domainQueryParam);
 		resultMap = queryService.query(domainQueryParam);
 		request.setAttribute("pageBean", domainQueryParam.getPage());
 		request.setAttribute("queryPath", "domains");
 		renderResponse(request, response, resultMap, domainQueryParam);
 	}
 	
 	@RequestMapping(value = "/domain/{domainName}", method = RequestMethod.GET)
 	@ResponseBody
 	public void queryDomain(@PathVariable String domainName,
 			HttpServletRequest request, HttpServletResponse response)
 			throws QueryException, RedirectExecption, IOException,
 			ServletException {
 		domainName = StringUtils.trim(domainName);
 		domainName = StringUtils.lowerCase(domainName);
 		String punyDomainName = domainName;
 		Map<String, Object> resultMap = null;
 		DomainQueryParam domainQueryParam = super
 				.praseDomainQueryParams(request);
 		request.setAttribute("queryType", "domain");
 		try {
 			domainName = WhoisUtil.toChineseUrl(domainName);
 			punyDomainName = IDN.toASCII(domainName);// long lable exception
 		} catch (Exception e) {
 			super.renderResponseError400(request, response);
 			return;
 		}
 		request.setAttribute("queryPara", IDN.toUnicode(punyDomainName));
 		if (!ValidateUtils.validateDomainName(punyDomainName)) {
 			resultMap = WhoisUtil.processError(WhoisUtil.COMMENDRRORCODE);
 		} else {
 			domainQueryParam.setQueryType(QueryType.DOMAIN);
 			domainQueryParam.setQ(domainName);
 			domainQueryParam.setDomainPuny(punyDomainName);
 			resultMap = queryService.queryDomain(domainQueryParam);
 		}
 		renderResponse(request, response, resultMap, domainQueryParam);
 	}
 
 	@RequestMapping(value = "/entities", method = RequestMethod.GET)
 	@ResponseBody
 	public void fuzzyQueryEntity(@RequestParam(required = false) String fn,
 			@RequestParam(required = false) String handle,
 			HttpServletRequest request, HttpServletResponse response)
 			throws QueryException, SQLException, IOException, ServletException {
 		Map<String, Object> resultMap = null;
 		EntityQueryParam queryParam = super.praseEntityQueryParams(request);
 		request.setAttribute("queryType", "entity");
 		if (StringUtils.isBlank(fn) && StringUtils.isBlank(handle)) {
 			super.renderResponseError400(request, response);
 			return;
 		}
 		String q = handle;
 		if (StringUtils.isNotBlank(fn)) {
 			q = fn;
 		}
 		q = WhoisUtil.urlDecode(q);
 		try {
 			IDN.toASCII(q);// long lable exception/not utf8 exception
 		} catch (Exception e) {
 			super.renderResponseError400(request, response);
 			return;
 		}
 		q = super.getNormalization(q);
 		if ("*".equals(q)) {
 			super.renderResponseError422(request, response);
 			return;
 		}
 		q = WhoisUtil.getLowerCaseIfAllAscii(q);
 		String decodeQ = WhoisUtil.toChineseUrl(q);
 		String fuzzyQuerySolrPropName = "handle";
 		String paramName = "handle";
 		if (StringUtils.isNotBlank(fn)) {
 			fuzzyQuerySolrPropName = "entityNames";
 			paramName = "fn";
 		}
 		queryParam.setQueryType(QueryType.SEARCHENTITY);
 		queryParam.setFuzzyQueryParamName(fuzzyQuerySolrPropName);
 		queryParam.setQ(decodeQ);
 		setMaxRecordsForFuzzyQ(queryParam);
 		resultMap = queryService.fuzzyQueryEntity(queryParam);
 		request.setAttribute("pageBean", queryParam.getPage());
 		request.setAttribute("queryPath", "entities");
 		request.setAttribute("queryPara", paramName + ":" + decodeQ);
 		renderResponse(request, response, resultMap, queryParam);
 	}
 
 	@RequestMapping(value = "/entity/{entityName}", method = RequestMethod.GET)
 	@ResponseBody
 	public void queryEntity(@PathVariable String entityName,
 			HttpServletRequest request, HttpServletResponse response)
 			throws QueryException, SQLException, IOException, ServletException {
 		EntityQueryParam queryParam = super.praseEntityQueryParams(request);
 		queryParam.setQueryType(QueryType.ENTITY);
 		queryParam.setQ(entityName);
 		Map<String, Object> resultMap = queryService.queryEntity(queryParam);
 		request.setAttribute("queryType", "entity");
 		request.setAttribute("queryPara", entityName);
 		renderResponse(request, response, resultMap, queryParam);
 	}
 
 	@RequestMapping(value = "/nameservers", method = RequestMethod.GET)
 	@ResponseBody
 	public void fuzzyQueryNs(@RequestParam(required = false) String name, 
 			@RequestParam(required = false) String ip,
 			HttpServletRequest request, HttpServletResponse response)
 			throws QueryException, SQLException, IOException, ServletException,
 			RedirectExecption {
 		if (StringUtils.isBlank(name) && StringUtils.isBlank(ip)) {
 			super.renderResponseError400(request, response);
 			return;
 		}else if(StringUtils.isNotBlank(name) && StringUtils.isNotBlank(ip)){
 			super.renderResponseError400(request, response);
 			return;
 		}
 		
 		String net = "0";
		Map<String, Object> resultMap = null;
		QueryParam queryParam = super.praseQueryParams(request);
 		if(StringUtils.isNotBlank(name)){
 			name = WhoisUtil.urlDecode(name);
 			name = StringUtils.trim(name);
 			name = super.getNormalization(name);
 			if ("*".equals(name)) {
 				super.renderResponseError422(request, response);
 				return;
 			}
 			name = WhoisUtil.getLowerCaseByLabel(name);
 			String punyQ = name;
 			try {
 				// long lable exception/not utf8 exception
 				punyQ = IDN.toASCII(name);
 			} catch (Exception e) {
 				super.renderResponseError400(request, response);
 				return;
 			}
 			request.setAttribute("queryPara", name);
 			if (!ValidateUtils.verifyFuzzyDomain(name)) {
 				resultMap = WhoisUtil.processError(WhoisUtil.COMMENDRRORCODE);
 			} else {
 				nameserversName(queryParam, punyQ, request);
 				resultMap = queryService.fuzzyQueryNameServer(queryParam);
 			}
 		}
 		
		request.setAttribute("queryType", "nameserver");
		
 		if(StringUtils.isNotBlank(ip)){
 			if (!ValidateUtils.verifyIP(ip, net)) {
 				super.renderResponseError400(request, response);
 				return;
 			}
 			nameserversIp(queryParam, ip, request);
 			resultMap = queryService.fuzzyQueryNameServer(queryParam);
 		}
 		renderResponse(request, response, resultMap, queryParam);
 	}
 	
 	private void nameserversName(QueryParam queryParam, String punyQ, HttpServletRequest request){
 		queryParam.setQueryType(QueryType.SEARCHNS);
 		queryParam.setQ(punyQ);
 		request.setAttribute("pageBean", queryParam.getPage());
 		request.setAttribute("queryPath", "nameservers");
 		setMaxRecordsForFuzzyQ(queryParam);
 	}
 	
 	private void nameserversIp(QueryParam queryParam, String ip, HttpServletRequest request){
 		String punyQ = ip;
 		request.setAttribute("queryPara", ip);
 		queryParam.setQueryType(QueryType.SEARCHNS);
 		queryParam.setQ(punyQ);
 		request.setAttribute("pageBean", queryParam.getPage());
 		request.setAttribute("queryPath", "nameservers");
 		setMaxRecordsForFuzzyQ(queryParam);
 	}
 
 	@RequestMapping(value = "/nameserver/{nsName}", method = RequestMethod.GET)
 	@ResponseBody
 	public void queryNs(@PathVariable String nsName,
 			HttpServletRequest request, HttpServletResponse response)
 			throws QueryException, SQLException, IOException, ServletException {
 		nsName = StringUtils.trim(nsName);
 		nsName = StringUtils.lowerCase(nsName);
 		String punyNsName = IDN.toASCII(WhoisUtil.toChineseUrl(nsName));
 		Map<String, Object> resultMap = null;
 		QueryParam queryParam = super.praseQueryParams(request);
 		request.setAttribute("queryType", "nameserver");
 		if (!ValidateUtils.verifyNameServer(nsName)) {
 			resultMap = WhoisUtil.processError(WhoisUtil.COMMENDRRORCODE);
 		} else {
 			queryParam.setQ(punyNsName);
 			queryParam.setQueryType(QueryType.NAMESERVER);
 			resultMap = queryService.query(queryParam);
 			request.setAttribute("queryPara", IDN.toUnicode(punyNsName));
 		}
 		renderResponse(request, response, resultMap, queryParam);
 	}
 
 	@RequestMapping(value = "/autnum/{autnum}", method = RequestMethod.GET)
 	@ResponseBody
 	public void queryAs(@PathVariable String autnum,
 			HttpServletRequest request, HttpServletResponse response)
 			throws QueryException, RedirectExecption, IOException,
 			ServletException {
 		QueryParam queryParam = super.praseQueryParams(request);
 		queryParam.setQ(autnum);
 		Map<String, Object> resultMap = queryService.queryAS(queryParam);
 		request.setAttribute("queryType", "autnum");
 		request.setAttribute("queryPara", autnum);
 		renderResponse(request, response, resultMap, queryParam);
 	}
 
 	@RequestMapping(value = "/dsData/{q}", method = RequestMethod.GET)
 	@ResponseBody
 	public void queryDsData(@PathVariable String q, HttpServletRequest request,
 			HttpServletResponse response) throws QueryException,
 			RedirectExecption, IOException, ServletException {
 		query(QueryType.DSDATA, q, request, response);
 	}
 
 	@RequestMapping(value = "/events/{q}", method = RequestMethod.GET)
 	@ResponseBody
 	public void queryEvents(@PathVariable String q, HttpServletRequest request,
 			HttpServletResponse response) throws QueryException,
 			RedirectExecption, IOException, ServletException {
 		query(QueryType.EVENTS, q, request, response);
 	}
 
 	@RequestMapping(value = "/help", method = RequestMethod.GET)
 	@ResponseBody
 	public void queryHelp(HttpServletRequest request,
 			HttpServletResponse response) throws QueryException,
 			RedirectExecption, IOException, ServletException {
 		Map<String, Object> resultMap = null;
 		QueryParam queryParam = super.praseQueryParams(request);
 		queryParam.setQ("helpID");
 		resultMap = queryService.queryHelp(queryParam);
 		request.setAttribute("queryPara", queryParam);
 		renderResponse(request, response, resultMap, queryParam);
 	}
 
 	@RequestMapping(value = { "/ip/{ip}", "/ip/{ip}/" }, method = RequestMethod.GET)
 	@ResponseBody
 	public void queryIp(@PathVariable String ip, HttpServletRequest request,
 			HttpServletResponse response) throws QueryException,
 			RedirectExecption, IOException, ServletException {
 		String net = "0";
 		doQueryIp(ip, request, response, net);
 	}
 
 	@RequestMapping(value = { "/ip/{ip}/{net}", "/ip/{ip}/{net}/" }, method = RequestMethod.GET)
 	@ResponseBody
 	public void queryIpWithNet(@PathVariable String ip,
 			@PathVariable String net, HttpServletRequest request,
 			HttpServletResponse response) throws QueryException,
 			RedirectExecption, IOException, ServletException {
 		net = StringUtils.trim(net);
 		doQueryIp(ip, request, response, net);
 	}
 
 	private void doQueryIp(String ip, HttpServletRequest request,
 			HttpServletResponse response, String ipLength)
 			throws QueryException, IOException, ServletException,
 			RedirectExecption {
 		ip = StringUtils.trim(ip);
 		Map<String, Object> resultMap = null;
 		IpQueryParam queryParam = super.praseIpQueryParams(request);
 		String strInfo = ip;
 		request.setAttribute("queryPara", ip);
 		request.setAttribute("queryType", "ip");
 		if (!ValidateUtils.verifyIP(strInfo, ipLength)) {
 			super.renderResponseError400(request, response);
 			return;
 		}
 		queryParam.setQ(ip);
 		queryParam.setIpInfo(strInfo);
 		queryParam.setIpLength(Integer.parseInt(ipLength));
 		resultMap = queryService.queryIP(queryParam);
 		viewResolver.writeResponse(queryParam.getFormat(),
 				queryParam.getQueryType(), request, response, resultMap);
 	}
 
 	@RequestMapping(value = "/keyData/{q}", method = RequestMethod.GET)
 	@ResponseBody
 	public void queryKeyData(@PathVariable String q,
 			HttpServletRequest request, HttpServletResponse response)
 			throws QueryException, RedirectExecption, IOException,
 			ServletException {
 		query(QueryType.KEYDATA, q, request, response);
 	}
 
 	@RequestMapping(value = "/links/{q}", method = RequestMethod.GET)
 	@ResponseBody
 	public void queryLinks(@PathVariable String q, HttpServletRequest request,
 			HttpServletResponse response) throws QueryException,
 			RedirectExecption, IOException, ServletException {
 		query(QueryType.LINKS, q, request, response);
 	}
 
 	@RequestMapping(value = "/notices/{q}", method = RequestMethod.GET)
 	@ResponseBody
 	public void queryNotices(@PathVariable String q,
 			HttpServletRequest request, HttpServletResponse response)
 			throws QueryException, RedirectExecption, IOException,
 			ServletException {
 		query(QueryType.NOTICES, q, request, response);
 	}
 
 	@RequestMapping(value = "/phones/{q}", method = RequestMethod.GET)
 	@ResponseBody
 	public void queryPhones(@PathVariable String q, HttpServletRequest request,
 			HttpServletResponse response) throws QueryException,
 			RedirectExecption, IOException, ServletException {
 		query(QueryType.PHONES, q, request, response);
 	}
 
 	@RequestMapping(value = "/postalAddress/{q}", method = RequestMethod.GET)
 	@ResponseBody
 	public void queryPostalAddress(@PathVariable String q,
 			HttpServletRequest request, HttpServletResponse response)
 			throws QueryException, RedirectExecption, IOException,
 			ServletException {
 		query(QueryType.POSTALADDRESS, q, request, response);
 	}
 
 	@RequestMapping(value = "/secureDNS/{q}", method = RequestMethod.GET)
 	@ResponseBody
 	public void querySecureDNS(@PathVariable String q,
 			HttpServletRequest request, HttpServletResponse response)
 			throws QueryException, RedirectExecption, IOException,
 			ServletException {
 		query(QueryType.SECUREDNS, q, request, response);
 	}
 
 	@RequestMapping(value = "/remarks/{q}", method = RequestMethod.GET)
 	@ResponseBody
 	public void queryRemarks(@PathVariable String q,
 			HttpServletRequest request, HttpServletResponse response)
 			throws QueryException, RedirectExecption, IOException,
 			ServletException {
 		query(QueryType.REMARKS, q, request, response);
 	}
 
 	@RequestMapping(value = "/variants/{q}", method = RequestMethod.GET)
 	@ResponseBody
 	public void queryVariants(@PathVariable String q,
 			HttpServletRequest request, HttpServletResponse response)
 			throws QueryException, RedirectExecption, IOException,
 			ServletException {
 		query(QueryType.VARIANTS, q, request, response);
 	}
 
 	private void query(QueryType queryType, String q,
 			HttpServletRequest request, HttpServletResponse response)
 			throws QueryException, IOException, ServletException {
 		Map<String, Object> resultMap = null;
 		QueryParam queryParam = praseQueryParams(request);
 		queryParam.setQueryType(queryType);
 		queryParam.setQ(q);
 		request.setAttribute("queryType", queryType.getName());
 		if (!ValidateUtils.isCommonInvalidStr(queryParam.getQ())) {
 			super.renderResponseError400(request, response);
 		} else {
 			resultMap = queryService.query(queryParam);
 			request.setAttribute("queryPara", queryParam);
 			renderResponse(request, response, resultMap, queryParam);
 		}
 	}
 
 	@RequestMapping(value = "/**", method = RequestMethod.GET)
 	@ResponseBody
 	public void error400(HttpServletRequest request,
 			HttpServletResponse response) throws QueryException,
 			RedirectExecption, IOException, ServletException {
 		QueryParam queryParam = praseQueryParams(request);
 		Map<String, Object> resultMap = WhoisUtil
 				.processError(WhoisUtil.COMMENDRRORCODE);
 		renderResponse(request, response, resultMap, queryParam);
 		return;
 	}
 
 	@ExceptionHandler(value = { RedirectExecption.class })
 	@ResponseStatus(value = HttpStatus.MOVED_PERMANENTLY)
 	public String exp(Exception ex, HttpServletRequest request,
 			HttpServletResponse response) {
 		RedirectExecption rEx = (RedirectExecption) ex;
 		response.setHeader("Accept", getFormatCookie(request));
 		response.setHeader("Location", rEx.getRedirectURL());
 		response.setHeader("Connection", "close");
 		return null;
 	}
 }
