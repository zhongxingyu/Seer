 package com.discoverydns.dnsapiclient.internal.command.zone;
 
 import javax.ws.rs.client.WebTarget;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import javax.ws.rs.core.Response.Status;
 
 import com.discoverydns.dnsapiclient.command.zone.ZoneListCommand;
 import com.discoverydns.dnsapiclient.command.zone.ZoneListResponse;
 import com.discoverydns.dnsapiclient.framework.command.CommandMetaData;
 import com.discoverydns.dnsapiclient.internal.command.BaseRestCommandHandler;
 import com.discoverydns.dnsapiclient.internal.command.InvocationBuildInvoker;
 import com.discoverydns.dnsapiclient.internal.command.InvocationBuilderFactory;
 import com.discoverydns.dnsapiclient.internal.command.Method;
 import com.discoverydns.dnsapiclient.internal.command.NoEntityInvocationBuildInvoker;
 import com.discoverydns.dnsapiclient.internal.command.NoEntityInvocationBuilderFactory;
 import com.discoverydns.dnsapiclient.internal.views.ZoneListView;
 
 public class ZoneListCommandHandler extends
 		BaseRestCommandHandler<ZoneListCommand, ZoneListResponse> {
 
 	private final WebTarget zoneListTarget;
 
 	public ZoneListCommandHandler(final WebTarget baseWebTarget) {
 		super(Method.GET, Status.OK.getStatusCode(),
 				MediaType.APPLICATION_JSON_TYPE);
 		this.zoneListTarget = baseWebTarget.path("zones/");
 	}
 
 	@Override
 	public WebTarget getWebTarget(final ZoneListCommand command,
 			final CommandMetaData commandMetaData) {
 		WebTarget resolvedZoneListTarget = zoneListTarget;
 		if (command.getSearchName() != null) {
 			resolvedZoneListTarget = resolvedZoneListTarget.queryParam(
 					"searchName", command.getSearchName());
 		}
 		if (command.getSearchNameServerInterfaceSetId() != null) {
 			resolvedZoneListTarget = resolvedZoneListTarget.queryParam(
 					"searchNameServerInterfaceSetId",
 					command.getSearchNameServerInterfaceSetId());
 		}
 		if (command.getSearchGroup() != null) {
 			resolvedZoneListTarget = resolvedZoneListTarget.queryParam(
 					"searchGroup", command.getSearchGroup());
 		}
 		if (command.getSearchBrandedNameServers() != null) {
 			resolvedZoneListTarget = resolvedZoneListTarget.queryParam(
 					"searchBrandedNameServers",
 					command.getSearchBrandedNameServers());
 		}
 		if (command.getSearchDNSSECSigned() != null) {
 			resolvedZoneListTarget = resolvedZoneListTarget.queryParam(
 					"searchDNSSECSigned", command.getSearchDNSSECSigned());
 		}
 		return resolvedZoneListTarget;
 	}
 
 	@Override
 	public InvocationBuilderFactory getInvocationBuilderFactory(
 			final ZoneListCommand command, final CommandMetaData commandMetaData) {
 		return new NoEntityInvocationBuilderFactory();
 	}
 
 	@Override
 	public InvocationBuildInvoker getInvocationBuildInvoker(
 			final ZoneListCommand command, final CommandMetaData commandMetaData) {
 		return new NoEntityInvocationBuildInvoker();
 	}
 
 	@Override
 	public ZoneListResponse getCommandResponse(final Response restResponse,
 			final CommandMetaData commandMetaData) {
 		final ZoneListView zoneListView = getResponseEntity(ZoneListView.class,
 				restResponse);
 		return new ZoneListResponse(zoneListView);
 	}
 }
