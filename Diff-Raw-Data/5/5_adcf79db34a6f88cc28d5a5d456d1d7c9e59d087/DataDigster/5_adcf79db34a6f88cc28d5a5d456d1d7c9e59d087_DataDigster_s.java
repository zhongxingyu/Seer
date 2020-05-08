 /*******************************************************************************
  * Copyright (c) Jan 30, 2011 Zend Technologies Ltd. 
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html  
  *******************************************************************************/
 package org.zend.webapi.core.connection.data;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.restlet.data.Disposition;
 import org.restlet.engine.util.DateUtils;
 import org.restlet.ext.xml.DomRepresentation;
 import org.restlet.ext.xml.NodeList;
 import org.restlet.ext.xml.XmlRepresentation;
 import org.restlet.representation.Representation;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.zend.webapi.core.connection.data.CodeTracingStatus.State;
 import org.zend.webapi.core.connection.data.CodeTracingStatus.Status;
 import org.zend.webapi.core.connection.data.IResponseData.ResponseType;
 import org.zend.webapi.core.connection.data.values.ApplicationStatus;
 import org.zend.webapi.core.connection.data.values.IssueSeverity;
 import org.zend.webapi.core.connection.data.values.IssueStatus;
 import org.zend.webapi.core.connection.data.values.LicenseInfoStatus;
 import org.zend.webapi.core.connection.data.values.ServerStatus;
 import org.zend.webapi.core.connection.data.values.SystemEdition;
 import org.zend.webapi.core.connection.data.values.SystemStatus;
 import org.zend.webapi.core.connection.data.values.WebApiVersion;
 import org.zend.webapi.core.connection.request.IRequest;
 
 /**
  * Digest the low-level response into a high level response data
  * 
  * @author Roy, 2011
  * 
  *         TODO: do something with the strings
  */
 public class DataDigster extends GenericResponseDataVisitor {
 
 	private final Representation representation;
 	private final IResponseData data;
 
 	public DataDigster(IResponseData data, Representation representation) {
 		this.data = data;
 		this.representation = getRepresentation(representation, data);
 	}
 
 	/**
 	 * Perform the digest step
 	 * 
 	 * @param data
 	 */
 	public void digest() {
 		if (data != null) {
 			data.accept(this);
 		}
 	}
 
 	public DataDigster(IResponseData.ResponseType type,
 			Representation representation) {
 		this(create(type), representation);
 	}
 
 	public DataDigster(IRequest request, Representation representation) {
 		this(create(request), representation);
 	}
 	
 	/**
 	 * Validate XML representation.
 	 * 
 	 * @return validation result
 	 */
 	public boolean validateResponse() {
		if (representation instanceof XmlRepresentation
 				&& representation.getMediaType().getName()
						.startsWith("application/vnd.zend.serverapi")) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Helper method to get a value out of a given XPath
 	 * 
 	 * @return value of the XPath
 	 */
 	private String getValue(String path) {
 		return getValue(path, 0);
 	}
 
 	/**
 	 * Helper method to get a value out of a given XPath
 	 * 
 	 * @return value of the XPath
 	 */
 	private String getValue(String path, int occurrence) {
 		final NodeList nodes = ((XmlRepresentation) representation)
 				.getNodes(path);
 		if (nodes.size() == 0) {
 			return null;
 		}
 		final Node node = nodes.get(occurrence);
 		return node.getTextContent().trim();
 	}
 
 	/**
 	 * Helper method which returns the number of nodes with specified name which
 	 * occur before the current node (occurrence) in the whole document.
 	 * 
 	 * @return node number
 	 */
 	private int getPreviousNodesLength(String path, String nodeName,
 			int occurrence) {
 		if (occurrence == 0) {
 			return 0;
 		}
 		final NodeList nodes = ((XmlRepresentation) representation)
 				.getNodes(path);
 		if (nodes.size() == 0) {
 			return 0;
 		}
 		int result = 0;
 		for (int i = 0; i < occurrence; i++) {
 			Element node = (Element) nodes.get(i);
 			result += node.getElementsByTagName(nodeName).getLength();
 		}
 		return result;
 	}
 
 	/**
 	 * Helper method which returns number of nodes with specified name in a
 	 * current node (occurrence).
 	 * 
 	 * @return node number
 	 */
 	private int getNodesLength(String path, String nodeName, int occurrence) {
 		final NodeList nodes = ((XmlRepresentation) representation)
 				.getNodes(path);
 		if (nodes.size() == 0) {
 			return 0;
 		}
 		Element node = (Element) nodes.get(occurrence);
 		return node.getElementsByTagName(nodeName).getLength();
 	}
 
 	public boolean preVisit(SystemInfo systemInfo) {
 		String currentPath = systemInfo.getPrefix();
 
 		final String statusName = getValue(currentPath + "/status");
 		systemInfo.setStatus(SystemStatus.byName(statusName));
 
 		final String editionName = getValue(currentPath + "/edition");
 		systemInfo.setEdition(SystemEdition.byName(editionName));
 
 		final String serverVersion = getValue(currentPath
 				+ "/zendServerVersion");
 		systemInfo.setVersion(serverVersion);
 
 		final String supportedVersion = getValue(currentPath
 				+ "/supportedApiVersions");
 		List<WebApiVersion> versions = parseVersions(supportedVersion);
 		systemInfo.setSupportedApiVersions(versions);
 
 		final String phpVersion = getValue(currentPath + "/phpVersion");
 		systemInfo.setPhpVersion(phpVersion);
 
 		final String os = getValue(currentPath + "/operatingSystem");
 		systemInfo.setOperatingSystem(os);
 
 		systemInfo.setLicenseInfo(new LicenseInfo(currentPath
 				+ "/serverLicenseInfo"));
 		systemInfo.setManagerLicenseInfo(new LicenseInfo(currentPath
 				+ "/managerLicenseInfo"));
 		systemInfo.setMessageList(new MessageList(currentPath + "/messageList",
 				systemInfo.getOccurrence()));
 
 		return true;
 	}
 
 	/**
 	 * <pre>
 	 * 	<status>OK</status> 
 	 * 	<orderNumber>ZEND-ORDER-66</orderNumber>
 	 * 	<validUntil>Sat, 31 Mar 2012 00:00:00 GMT</validUntil>
 	 * 	<serverLimit>0</serverLimit>
 	 * </pre>
 	 */
 	public boolean preVisit(LicenseInfo licenseInfo) {
 		String currentPath = licenseInfo.getPrefix();
 
 		// temp for reading values
 		String value;
 
 		value = getValue(currentPath + "/status");
 		licenseInfo.setStatus(LicenseInfoStatus.byName(value));
 
 		value = getValue(currentPath + "/orderNumber");
 		licenseInfo.setOrderNumber(value);
 
 		value = getValue(currentPath + "/validUntil");
 		final Date parse = DateUtils.parse(value, DateUtils.FORMAT_RFC_1123);
 		licenseInfo.setValidUntil(parse);
 
 		value = getValue(currentPath + "/serverLimit");
 		licenseInfo.setLimit(Integer.parseInt(value == null ? "0" : value));
 
 		return true;
 	}
 
 	/**
 	 * <pre>
 	 * <messageList> 
 	 * 	<warning>This server is waiting a PHP restart</warning>
 	 * </messageList>
 	 * </pre>
 	 */
 	public boolean preVisit(MessageList messageList) {
 		// build message info list
 		List<String> errors = getMessageValues(messageList, "error");
 		messageList.setError(errors.size() > 0 ? errors : null);
 
 		List<String> warnings = getMessageValues(messageList, "warning");
 		messageList.setWarning(warnings.size() > 0 ? warnings : null);
 
 		List<String> infos = getMessageValues(messageList, "info");
 		messageList.setInfo(infos.size() > 0 ? infos : null);
 
 		return true;
 
 	}
 
 	private List<String> getMessageValues(MessageList messageList,
 			String nodeName) {
 		String currentPath = messageList.getPrefix();
 		final int size = getNodesLength(currentPath, nodeName,
 				messageList.getOccurrence());
 		final int previousSize = getPreviousNodesLength(currentPath, nodeName,
 				messageList.getOccurrence());
 		List<String> messages = new ArrayList<String>(size);
 		for (int index = previousSize; index < previousSize + size; index++) {
 			messages.add(getValue(currentPath + "/" + nodeName, index));
 		}
 		return messages;
 	}
 
 	public IResponseData getResponseData() {
 		return data;
 	}
 
 	/**
 	 * @param supportedVersion
 	 * @return
 	 */
 	private List<WebApiVersion> parseVersions(final String supportedVersion) {
 		if (supportedVersion == null) {
 			return null;
 		}
 		final String[] parsed = supportedVersion.split(",");
 		List<WebApiVersion> versions = new ArrayList<WebApiVersion>(
 				parsed.length);
 		for (String version : parsed) {
 			versions.add(WebApiVersion.byFullName(version));
 		}
 		return versions;
 	}
 
 	public boolean preVisit(ServersList serversList) {
 		String currentPath = serversList.getPrefix();
 
 		final NodeList nodes = ((XmlRepresentation) representation)
 				.getNodes(currentPath + "/serverInfo");
 		final int size = nodes.size();
 		if (size == 0) {
 			return false;
 		}
 
 		// build servers info list
 		List<ServerInfo> serversInfo = new ArrayList<ServerInfo>(size);
 		for (int index = 0; index < size; index++) {
 			serversInfo.add(new ServerInfo(currentPath + "/serverInfo", index));
 		}
 
 		serversList.setServerInfo(serversInfo);
 		return true;
 	}
 
 	/**
 	 * <pre>
 	 *       <serverInfo>
 	 *         <id>2</id>
 	 *         <name>www-02</name>
 	 *         <address>https://www-02.local:10082/ZendServer</address>
 	 *         <status>restarting</status>
 	 *         <messageList />
 	 *       </serverInfo>
 	 * 
 	 * </pre>
 	 */
 	public boolean preVisit(ServerInfo serverInfo) {
 		String currentPath = serverInfo.getPrefix();
 		int occurrence = serverInfo.getOccurrence();
 
 		String value = getValue(currentPath + "/id", occurrence);
 		serverInfo.setId(parseNumberIfExists(value));
 
 		value = getValue(currentPath + "/name", occurrence);
 		serverInfo.setName(value);
 
 		value = getValue(currentPath + "/address", occurrence);
 		serverInfo.setAddress(value);
 
 		value = getValue(currentPath + "/status", occurrence);
 		serverInfo.setStatus(ServerStatus.byName(value));
 
 		final MessageList messageList = new MessageList(currentPath
 				+ "/messageList", occurrence);
 		serverInfo.setMessageList(messageList);
 
 		return true;
 	}
 
 	@Override
 	public boolean preVisit(ServerConfig serverConfig) {
 		// representation.get
 
 		final Disposition disposition = representation.getDisposition();
 		if (disposition != null) {
 			serverConfig.setFilename(disposition.getFilename());
 		}
 		final int size = (int) representation.getSize();
 		serverConfig.setFileSize(size);
 
 		try {
 			byte[] content = new byte[size];
 			InputStream reader = representation.getStream();
 			reader.read(content);
 			serverConfig.setFileContent(content);
 		} catch (IOException e) {
 			// TODO log exception
 		}
 
 		return super.preVisit(serverConfig);
 	}
 
 	public boolean preVisit(ApplicationsList applicationsList) {
 		String currentPath = applicationsList.getPrefix();
 
 		final NodeList nodes = ((XmlRepresentation) representation)
 				.getNodes(currentPath + "/applicationInfo");
 		final int size = nodes.size();
 		if (size == 0) {
 			return false;
 		}
 
 		// build applications info list
 		List<ApplicationInfo> applicationsInfo = new ArrayList<ApplicationInfo>(
 				size);
 		for (int index = 0; index < size; index++) {
 			applicationsInfo.add(new ApplicationInfo(currentPath
 					+ "/applicationInfo", index));
 		}
 
 		applicationsList.setApplicationsInfo(applicationsInfo);
 		return true;
 	}
 
 	public boolean preVisit(ApplicationInfo applicationInfo) {
 		String currentPath = applicationInfo.getPrefix();
 		int occurrence = applicationInfo.getOccurrence();
 
 		String value = getValue(currentPath + "/id", occurrence);
 		applicationInfo.setId(parseNumberIfExists(value));
 
 		value = getValue(currentPath + "/baseUrl", occurrence);
 		applicationInfo.setBaseUrl(value);
 
 		value = getValue(currentPath + "/appName", occurrence);
 		applicationInfo.setAppName(value);
 
 		value = getValue(currentPath + "/userAppName", occurrence);
 		applicationInfo.setUserAppName(value);
 
 		value = getValue(currentPath + "/installedLocation", occurrence);
 		applicationInfo.setInstalledLocation(value);
 
 		value = getValue(currentPath + "/status", occurrence);
 		applicationInfo.setStatus(ApplicationStatus.byName(value));
 
 		final ApplicationServers applicationServers = new ApplicationServers(
 				currentPath + "/servers", occurrence);
 		applicationInfo.setServers(applicationServers);
 
 		final DeployedVersions deployedVersions = new DeployedVersions(
 				currentPath + "/deployedVersions", occurrence);
 		applicationInfo.setDeployedVersions(deployedVersions);
 
 		final MessageList messageList = new MessageList(currentPath
 				+ "/messageList", occurrence);
 		applicationInfo.setMessageList(messageList);
 
 		return true;
 	}
 
 	public boolean preVisit(DeployedVersions versions) {
 		String currentPath = versions.getPrefix();
 		final int size = getNodesLength(currentPath, "deployedVersion",
 				versions.getOccurrence());
 
 		if (size == 0) {
 			return false;
 		}
 
 		final int overallSize = getPreviousNodesLength(currentPath,
 				"deployedVersion", versions.getOccurrence());
 
 		// build versions list
 		List<DeployedVersion> versionsInfo = new ArrayList<DeployedVersion>(
 				size);
 		for (int index = overallSize; index < overallSize + size; index++) {
 			versionsInfo.add(new DeployedVersion(currentPath
 					+ "/deployedVersion", index));
 		}
 
 		versions.setDeployedVersions(versionsInfo);
 		return true;
 	}
 
 	public boolean preVisit(DeployedVersion version) {
 		String currentPath = version.getPrefix();
 		int occurrence = version.getOccurrence();
 		String value = getValue(currentPath, occurrence);
 		version.setVersion(value);
 		return true;
 	}
 
 	public boolean preVisit(ApplicationServers serversList) {
 		String currentPath = serversList.getPrefix();
 		final int size = getNodesLength(currentPath, "applicationServer",
 				serversList.getOccurrence());
 
 		if (size == 0) {
 			return false;
 		}
 
 		final int overallSize = getPreviousNodesLength(currentPath,
 				"applicationServer", serversList.getOccurrence());
 
 		// build servers info list
 		List<ApplicationServer> servers = new ArrayList<ApplicationServer>(size);
 		for (int index = overallSize; index < overallSize + size; index++) {
 			servers.add(new ApplicationServer(currentPath
 					+ "/applicationServer", index));
 		}
 
 		serversList.setAapplicationServers(servers);
 		return true;
 	}
 
 	public boolean preVisit(ApplicationServer applicationServer) {
 		String currentPath = applicationServer.getPrefix();
 		int occurrence = applicationServer.getOccurrence();
 
 		String value = getValue(currentPath + "/id", occurrence);
 		applicationServer.setId(parseNumberIfExists(value));
 
 		value = getValue(currentPath + "/deployedVersion", occurrence);
 		applicationServer.setDeployedVersion(value);
 
 		value = getValue(currentPath + "/status", occurrence);
 		applicationServer.setStatus(ApplicationStatus.byName(value));
 		return true;
 	}
 
 	@Override
 	public boolean preVisit(Parameter parameter) {
 		String currentPath = parameter.getPrefix();
 		int occurrence = parameter.getOccurrence();
 		String value = getValue(currentPath + "/name", occurrence);
 		parameter.setName(value);
 		value = getValue(currentPath + "/value", occurrence);
 		parameter.setValue(value);
 		return true;
 	}
 
 	@Override
 	public boolean preVisit(SuperGlobals superGlobals) {
 		String currentPath = superGlobals.getPrefix();
 		int occurrence = superGlobals.getOccurrence();
 		ParameterList get = new ParameterList(currentPath + "/get", occurrence);
 		superGlobals.setGet(get);
 		ParameterList post = new ParameterList(currentPath + "/post",
 				occurrence);
 		superGlobals.setPost(post);
 		ParameterList cookie = new ParameterList(currentPath + "/cookie",
 				occurrence);
 		superGlobals.setCookie(cookie);
 		ParameterList session = new ParameterList(currentPath + "/session",
 				occurrence);
 		superGlobals.setSession(session);
 		ParameterList server = new ParameterList(currentPath + "/server",
 				occurrence);
 		superGlobals.setServer(server);
 		return true;
 	}
 
 	@Override
 	public boolean preVisit(Step step) {
 		String currentPath = step.getPrefix();
 		int occurrence = step.getOccurrence();
 		String value = getValue(currentPath + "/number", occurrence);
 		step.setNumber(parseNumberIfExists(value));
 		value = getValue(currentPath + "/object", occurrence);
 		step.setObjectId(value);
 		value = getValue(currentPath + "/class", occurrence);
 		step.setClassId(value);
 		value = getValue(currentPath + "/function", occurrence);
 		step.setFunction(value);
 		value = getValue(currentPath + "/file", occurrence);
 		step.setFile(value);
 		value = getValue(currentPath + "/line", occurrence);
 		step.setLine(parseNumberIfExists(value));
 		return true;
 	}
 
 	@Override
 	public boolean preVisit(CodeTracingStatus codeTracingStatus) {
 		String currentPath = codeTracingStatus.getPrefix();
 		int occurrence = codeTracingStatus.getOccurrence();
 		String value = getValue(currentPath + "/componentStatus", occurrence);
 		codeTracingStatus.setComponentStatus(Status.byValue(value));
 		value = getValue(currentPath + "/traceEnabled", occurrence);
 		codeTracingStatus.setTraceEnabled(State
 				.byValue(parseNumberIfExists(value)));
 		value = getValue(currentPath + "/developerMode", occurrence);
 		codeTracingStatus.setDeveloperMode(State
 				.byValue(parseNumberIfExists(value)));
 		value = getValue(currentPath + "/awaitsRestart", occurrence);
 		codeTracingStatus.setAwaitsRestart(State
 				.byValue(parseNumberIfExists(value)));
 		return true;
 	}
 
 	@Override
 	public boolean preVisit(CodeTrace codeTrace) {
 		String currentPath = codeTrace.getPrefix();
 		int occurrence = codeTrace.getOccurrence();
 		String value = getValue(currentPath + "/id", occurrence);
 		codeTrace.setId(value);
 		value = getValue(currentPath + "/date", occurrence);
 		codeTrace.setDate(parseLongIfExists(value));
 		value = getValue(currentPath + "/url", occurrence);
 		codeTrace.setUrl(value);
 		value = getValue(currentPath + "/createdBy", occurrence);
 		codeTrace.setCreatedBy(value);
 		value = getValue(currentPath + "/filesize", occurrence);
 		codeTrace.setFilesize(parseNumberIfExists(value));
 		value = getValue(currentPath + "/applicationId", occurrence);
 		codeTrace.setApplicationId(parseNumberIfExists(value));
 		return true;
 	}
 
 	@Override
 	public boolean preVisit(CodeTracingList codeTracingList) {
 		String currentPath = codeTracingList.getPrefix();
 		final int size = getNodesLength(currentPath, "codeTrace",
 				codeTracingList.getOccurrence());
 
 		if (size == 0) {
 			return false;
 		}
 
 		final int overallSize = getPreviousNodesLength(currentPath,
 				"codeTrace", codeTracingList.getOccurrence());
 
 		List<CodeTrace> traces = new ArrayList<CodeTrace>(size);
 		for (int index = overallSize; index < overallSize + size; index++) {
 			traces.add(new CodeTrace(currentPath + "/codeTrace", index));
 		}
 
 		codeTracingList.setTraces(traces);
 		return true;
 	}
 
 	@Override
 	public boolean preVisit(EventsGroup eventsGroup) {
 		String currentPath = eventsGroup.getPrefix();
 		int occurrence = eventsGroup.getOccurrence();
 		String value = getValue(currentPath + "/eventsGroupId", occurrence);
 		eventsGroup.setEventsGroupId(parseNumberIfExists(value));
 		value = getValue(currentPath + "/eventsCount", occurrence);
 		eventsGroup.setEventsCount(parseNumberIfExists(value));
 		value = getValue(currentPath + "/startTime", occurrence);
 		eventsGroup.setStartTime(value);
 		value = getValue(currentPath + "/serverId", occurrence);
 		eventsGroup.setServerId(parseNumberIfExists(value));
 		value = getValue(currentPath + "/class", occurrence);
 		eventsGroup.setClassId(value);
 		value = getValue(currentPath + "/userData", occurrence);
 		eventsGroup.setUserData(value);
 		value = getValue(currentPath + "/javaBacktrace", occurrence);
 		eventsGroup.setJavaBacktrace(value);
 		value = getValue(currentPath + "/execTime", occurrence);
 		eventsGroup.setExecTime(parseNumberIfExists(value));
 		value = getValue(currentPath + "/avgExecTime", occurrence);
 		eventsGroup.setAvgExecTime(parseNumberIfExists(value));
 		value = getValue(currentPath + "/memUsage", occurrence);
 		eventsGroup.setMemUsage(parseNumberIfExists(value));
 		value = getValue(currentPath + "/avgMemUsage", occurrence);
 		eventsGroup.setAvgMemUsage(parseNumberIfExists(value));
 		value = getValue(currentPath + "/avgOutputSize", occurrence);
 		eventsGroup.setAvgOutputSize(parseNumberIfExists(value));
 		value = getValue(currentPath + "/load", occurrence);
 		eventsGroup.setLoad(value);
 		return true;
 	}
 
 	@Override
 	public boolean preVisit(Event event) {
 		String currentPath = event.getPrefix();
 		int occurrence = event.getOccurrence();
 		String value = getValue(currentPath + "/eventsGroupId", occurrence);
 		event.setEventsGroupId(value);
 		value = getValue(currentPath + "/type", occurrence);
 		event.setEventType(value);
 		value = getValue(currentPath + "/severity", occurrence);
 		event.setSeverity(value);
 		value = getValue(currentPath + "/description", occurrence);
 		event.setDescription(value);
 		SuperGlobals superGlobals = new SuperGlobals(currentPath
 				+ "/superGlobals", occurrence);
 		event.setSuperGlobals(superGlobals);
 		Backtrace backtrace = new Backtrace(currentPath + "/backtrace",
 				occurrence);
 		event.setBacktrace(backtrace);
 		return true;
 	}
 
 	@Override
 	public boolean preVisit(Backtrace backtrace) {
 		String currentPath = backtrace.getPrefix();
 		final int size = getNodesLength(currentPath, "step",
 				backtrace.getOccurrence());
 
 		if (size == 0) {
 			return false;
 		}
 
 		final int overallSize = getPreviousNodesLength(currentPath,
 				"applicationServer", backtrace.getOccurrence());
 
 		List<Step> steps = new ArrayList<Step>(size);
 		for (int index = overallSize; index < overallSize + size; index++) {
 			steps.add(new Step(currentPath + "/step", index));
 		}
 
 		backtrace.setSteps(steps);
 		return true;
 	}
 
 	@Override
 	public boolean preVisit(ParameterList parameterList) {
 		String currentPath = parameterList.getPrefix();
 		final int size = getNodesLength(currentPath, "parameter",
 				parameterList.getOccurrence());
 
 		if (size == 0) {
 			return false;
 		}
 
 		final int overallSize = getPreviousNodesLength(currentPath,
 				"parameter", parameterList.getOccurrence());
 
 		List<Parameter> parameters = new ArrayList<Parameter>(size);
 		for (int index = overallSize; index < overallSize + size; index++) {
 			parameters.add(new Parameter(currentPath + "/parameter", index));
 		}
 
 		parameterList.setParameters(parameters);
 		return true;
 	}
 
 	@Override
 	public boolean preVisit(EventsGroupDetails eventsGroupDetails) {
 		String currentPath = eventsGroupDetails.getPrefix();
 		int occurrence = eventsGroupDetails.getOccurrence();
 		String value = getValue(currentPath + "/issueId", occurrence);
 		eventsGroupDetails.setIssueId(parseNumberIfExists(value));
 		value = getValue(currentPath + "/codeTracing", occurrence);
 		eventsGroupDetails.setCodeTracing(value);
 		EventsGroup eventsGroup = new EventsGroup(currentPath + "/eventsGroup",
 				occurrence);
 		eventsGroupDetails.setEventsGroup(eventsGroup);
 		Event event = new Event(currentPath + "/event", occurrence);
 		eventsGroupDetails.setEvent(event);
 		return true;
 	}
 
 	@Override
 	public boolean preVisit(RouteDetail routeDetail) {
 		String currentPath = routeDetail.getPrefix();
 		int occurrence = routeDetail.getOccurrence();
 		String value = getValue(currentPath + "/key", occurrence);
 		routeDetail.setKey(value);
 		value = getValue(currentPath + "/value", occurrence);
 		routeDetail.setValue(value);
 		return true;
 	}
 
 	@Override
 	public boolean preVisit(EventsGroups eventsGroups) {
 		String currentPath = eventsGroups.getPrefix();
 		final int size = getNodesLength(currentPath, "eventsGroup",
 				eventsGroups.getOccurrence());
 
 		if (size == 0) {
 			return false;
 		}
 
 		final int overallSize = getPreviousNodesLength(currentPath,
 				"eventsGroup", eventsGroups.getOccurrence());
 
 		List<EventsGroup> groups = new ArrayList<EventsGroup>(size);
 		for (int index = overallSize; index < overallSize + size; index++) {
 			groups.add(new EventsGroup(currentPath + "/eventsGroup", index));
 		}
 
 		eventsGroups.setGroups(groups);
 		return true;
 	}
 
 	@Override
 	public boolean preVisit(RouteDetails routeDetails) {
 		String currentPath = routeDetails.getPrefix();
 		final int size = getNodesLength(currentPath, "routeDetail",
 				routeDetails.getOccurrence());
 
 		if (size == 0) {
 			return false;
 		}
 
 		final int overallSize = getPreviousNodesLength(currentPath,
 				"routeDetail", routeDetails.getOccurrence());
 
 		List<RouteDetail> details = new ArrayList<RouteDetail>(size);
 		for (int index = overallSize; index < overallSize + size; index++) {
 			details.add(new RouteDetail(currentPath + "/routeDetail", index));
 		}
 
 		routeDetails.setDetails(details);
 		return true;
 	}
 
 	@Override
 	public boolean preVisit(Issue issue) {
 		String currentPath = issue.getPrefix();
 		int occurrence = issue.getOccurrence();
 		String value = getValue(currentPath + "/id", occurrence);
 		issue.setId(parseNumberIfExists(value));
 		value = getValue(currentPath + "/rule", occurrence);
 		issue.setRule(value);
 		value = getValue(currentPath + "/lastOccurance", occurrence);
 		issue.setLastOccurance(value);
 		value = getValue(currentPath + "/severity", occurrence);
 		issue.setSeverity(IssueSeverity.byName(value));
 		value = getValue(currentPath + "/status", occurrence);
 		issue.setStatus(IssueStatus.byName(value));
 		GeneralDetails generalDetails = new GeneralDetails(currentPath
 				+ "/generalDetails", occurrence);
 		issue.setGeneralDetails(generalDetails);
 		RouteDetails routeDetails = new RouteDetails(currentPath
 				+ "/routeDetails", occurrence);
 		issue.setRouteDetails(routeDetails);
 		return true;
 	}
 
 	@Override
 	public boolean preVisit(IssueList issueList) {
 		String currentPath = issueList.getPrefix();
 		final int size = getNodesLength(currentPath, "issue",
 				issueList.getOccurrence());
 
 		if (size == 0) {
 			return false;
 		}
 
 		final int overallSize = getPreviousNodesLength(currentPath, "issue",
 				issueList.getOccurrence());
 
 		List<Issue> issues = new ArrayList<Issue>(size);
 		for (int index = overallSize; index < overallSize + size; index++) {
 			issues.add(new Issue(currentPath + "/issue", index));
 		}
 
 		issueList.setIssues(issues);
 		return true;
 	}
 
 	@Override
 	public boolean preVisit(IssueDetails issueDetails) {
 		String currentPath = issueDetails.getPrefix();
 		int occurrence = issueDetails.getOccurrence();
 		Issue issue = new Issue(currentPath + "/issue", occurrence);
 		issueDetails.setIssue(issue);
 		EventsGroups groups = new EventsGroups(currentPath + "/eventsGroups",
 				occurrence);
 		issueDetails.setEventsGroups(groups);
 		return true;
 	}
 
 	@Override
 	public boolean preVisit(Events events) {
 		String currentPath = events.getPrefix();
 		final int size = getNodesLength(currentPath, "event",
 				events.getOccurrence());
 
 		if (size == 0) {
 			return false;
 		}
 
 		final int overallSize = getPreviousNodesLength(currentPath, "event",
 				events.getOccurrence());
 
 		List<Event> eventsList = new ArrayList<Event>(size);
 		for (int index = overallSize; index < overallSize + size; index++) {
 			eventsList.add(new Event(currentPath + "/event", index));
 		}
 
 		events.setEvents(eventsList);
 		return true;
 	}
 
 	@Override
 	public boolean preVisit(RequestSummary requestSummary) {
 		String currentPath = requestSummary.getPrefix();
 		int occurrence = requestSummary.getOccurrence();
 		String value = getValue(currentPath + "/eventsCount", occurrence);
 		requestSummary.setEventsCount(parseNumberIfExists(value));
 		value = getValue(currentPath + "/codeTracing", occurrence);
 		requestSummary.setCodeTracing(value);
 		Events events = new Events(currentPath + "/events", occurrence);
 		requestSummary.setEvents(events);
 		return true;
 	}
 
 	@Override
 	public boolean preVisit(CodeTraceFile codeTraceFile) {
 		Disposition disposition = representation.getDisposition();
 		if (disposition != null) {
 			codeTraceFile.setFilename(disposition.getFilename());
 		}
 		int size = (int) representation.getSize();
 		codeTraceFile.setFileSize(size);
 
 		try {
 			byte[] content = new byte[size];
 			InputStream reader = representation.getStream();
 			int offset = 0;
 			while (offset < size) {
 				offset += reader.read(content, offset, size - offset);
 			}
 			codeTraceFile.setFileContent(content);
 		} catch (IOException e) {
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public boolean preVisit(IssueFile issueFile) {
 		Disposition disposition = representation.getDisposition();
 		if (disposition != null) {
 			issueFile.setFilename(disposition.getFilename());
 		}
 		int size = (int) representation.getSize();
 		issueFile.setFileSize(size);
 
 		try {
 			byte[] content = new byte[size];
 			InputStream reader = representation.getStream();
 			int offset = 0;
 			while (offset < size) {
 				offset += reader.read(content, offset, size - offset);
 			}
 			issueFile.setFileContent(content);
 		} catch (IOException e) {
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public boolean preVisit(DebugRequest debugRequest) {
 		String currentPath = debugRequest.getPrefix();
 		int occurrence = debugRequest.getOccurrence();
 		String value = getValue(currentPath + "/success", occurrence);
 		debugRequest.setSuccess(value);
 		value = getValue(currentPath + "/message", occurrence);
 		debugRequest.setMessage(value);
 		return true;
 	}
 
 	@Override
 	public boolean preVisit(ProfileRequest profileRequest) {
 		String currentPath = profileRequest.getPrefix();
 		int occurrence = profileRequest.getOccurrence();
 		String value = getValue(currentPath + "/success", occurrence);
 		profileRequest.setSuccess(value);
 		value = getValue(currentPath + "/message", occurrence);
 		profileRequest.setMessage(value);
 		return true;
 	}
 
 	@Override
 	public boolean preVisit(GeneralDetails generalDetails) {
 		String currentPath = generalDetails.getPrefix();
 		int occurrence = generalDetails.getOccurrence();
 		String value = getValue(currentPath + "/url", occurrence);
 		generalDetails.setUrl(value);
 		value = getValue(currentPath + "/sourceFile", occurrence);
 		generalDetails.setSourceFile(value);
 		value = getValue(currentPath + "/sourceLine", occurrence);
 		generalDetails.setSourceLine(parseLongIfExists(value));
 		value = getValue(currentPath + "/function", occurrence);
 		generalDetails.setFunction(value);
 		value = getValue(currentPath + "/aggregationHint", occurrence);
 		generalDetails.setAggregationHint(value);
 		value = getValue(currentPath + "/errorString", occurrence);
 		generalDetails.setErrorString(value);
 		value = getValue(currentPath + "/errorType", occurrence);
 		generalDetails.setErrorType(value);
 		return true;
 	}
 
 	/**
 	 * @param value
 	 * @return
 	 */
 	private int parseNumberIfExists(String value) {
 		if (value == null || value.length() == 0) {
 			return 0;
 		}
 		return Integer.parseInt(value);
 	}
 
 	/**
 	 * @param value
 	 * @return long
 	 */
 	private long parseLongIfExists(String value) {
 		if (value == null || value.length() == 0) {
 			return 0;
 		}
 		return Long.parseLong(value);
 	}
 
 	private static IResponseData create(IRequest request) {
 		final ResponseType type = request.getExpectedResponseDataType();
 		return create(type);
 	}
 
 	private static IResponseData create(ResponseType type) {
 		switch (type) {
 		case SYSTEM_INFO:
 			return new SystemInfo();
 		case SERVERS_LIST:
 			return new ServersList();
 		case SERVER_INFO:
 			return new ServerInfo();
 		case SERVER_CONFIG:
 			return new ServerConfig();
 		case APPLICATIONS_LIST:
 			return new ApplicationsList();
 		case APPLICATION_INFO:
 			return new ApplicationInfo();
 		case CODE_TRACING_STATUS:
 			return new CodeTracingStatus();
 		case CODE_TRACE:
 			return new CodeTrace();
 		case CODE_TRACING_LIST:
 			return new CodeTracingList();
 		case CODE_TRACE_FILE:
 			return new CodeTraceFile();
 		case REQUEST_SUMMARY:
 			return new RequestSummary();
 		case ISSUE_LIST:
 			return new IssueList();
 		case ISSUE_DETAILS:
 			return new IssueDetails();
 		case EVENTS_GROUP_DETAILS:
 			return new EventsGroupDetails();
 		case ISSUE_FILE:
 			return new IssueFile();
 		case ISSUE:
 			return new Issue();
 		case DEBUG_REQUEST:
 			return new DebugRequest();
 		case PROFILE_REQUEST:
 			return new ProfileRequest();
 		default:
 			return null;
 		}
 	}
 
 	private Representation getRepresentation(Representation representation,
 			IResponseData responseData) {
 		switch (responseData.getType()) {
 		case SERVER_CONFIG:
 		case CODE_TRACE_FILE:
 		case ISSUE_FILE:
 			return representation;
 		default:
 			return new DomRepresentation(representation);
 		}
 	}
 }
