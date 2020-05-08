 /**
  *
  * Copyright (c) 2013, Linagora
  * 
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  *
  */
 package controllers;
 
 import java.util.Comparator;
 import java.util.List;
 import java.util.Set;
 
 import org.ow2.play.governance.permission.api.Constants;
 import org.ow2.play.governance.permission.api.Permission;
 import org.ow2.play.governance.permission.api.PermissionService;
 import org.ow2.play.metadata.api.MetaResource;
 import org.ow2.play.metadata.api.service.MetadataService;
 
 import play.data.validation.Required;
 import play.mvc.With;
 import utils.Locator;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.ImmutableSortedSet;
 import com.google.common.collect.Lists;
 
 /**
  * @author chamerling
  * 
  */
 @With(Secure.class)
 public class PermissionController extends PlayController {
 
 	/**
 	 * Get all the permissions
 	 */
 	public static void index() {
 		try {
 			PermissionService client = Locator.getPermissionService(getNode());
 			List<MetaResource> list = client.getPermissions();
 			render(list);
 		} catch (Exception e) {
 			handleException(e.getMessage(), e);
 		}
 	}
 
 	/**
 	 * 
 	 * @param name
 	 */
 	public static void permission(String name) {
 		try {
 			PermissionService client = Locator.getPermissionService(getNode());
 			MetaResource permission = client.getPermission(name);
 			render(permission);
 		} catch (Exception e) {
 			handleException(e.getMessage(), e);
 		}
 	}
 
 	public static void create() {
 		// get all the stream and all the groups...
 
 		Set<String> streams = null;
 		Set<String> groups = null;
 		try {
 			MetadataService meta = Locator.getMetaService(getNode());
 			streams = ImmutableSortedSet
 					.orderedBy(new Comparator<String>() {
 						@Override
 						public int compare(String r1, String r2) {
 							return r1.compareToIgnoreCase(r2);
 						}
 					})
 					.addAll(Collections2.transform(
 							meta.listWhere("stream", null),
 							new Function<MetaResource, String>() {
 								@Override
 								public String apply(MetaResource input) {
 									return input.getResource().getUrl() + "#"
 											+ input.getResource().getName();
 								}
 							})).build();
 
 			groups = ImmutableSortedSet
 					.orderedBy(new Comparator<String>() {
 						@Override
 						public int compare(String r1, String r2) {
 							return r1.compareToIgnoreCase(r2);
 						}
 					})
 					.addAll(Collections2.transform(
 							meta.listWhere("group", null),
 							new Function<MetaResource, String>() {
 								@Override
 								public String apply(MetaResource input) {
 									return input.getResource().getUrl() + "#"
 											+ input.getResource().getName();
 								}
 							})).build();
 
 		} catch (Exception e) {
 			e.printStackTrace();
 			flash.error("Problem while getting data");
 			index();
 		}
 		render(groups, streams);
 	}
 
 	public static void doCreate(
 			@Required(message = "Name is required") String name,
 			@Required(message = "Access is required") String[] access,
 			@Required(message = "Agent is required") String[] agent,
 			@Required(message = "Mode is required") String[] mode) {
 
 		validation.required(name);
 		validation.isTrue(access != null && access.length > 0);
 		validation.isTrue(agent != null && agent.length > 0);
 		validation.required(access);
 		validation.required(agent);
 		validation.required(mode);
 
 		if (validation.hasErrors()) {
 			params.flash();
 			validation.keep();
 			create();
 		}
 
 		try {
 			PermissionService client = Locator.getPermissionService(getNode());
 			Permission permission = new Permission();
 			permission.name = name;
 
 			if (access != null) {
 				permission.accessTo.addAll(Lists.newArrayList(access));
 			}
 
 			if (agent != null) {
 				permission.agent.addAll(Lists.newArrayList(agent));
 			}
 
 			if (mode != null) {
 				for (String string : mode) {
 					if (string.equalsIgnoreCase("read")) {
 						permission.mode.add(Constants.READ);
 					}
 					if (string.equalsIgnoreCase("write")) {
 						permission.mode.add(Constants.WRITE);
 					}
					if (string.equalsIgnoreCase("subscribe")) {
						permission.mode.add(Constants.SUBSCRIBE);
					}
					if (string.equalsIgnoreCase("subcribe")) { // compensate for typos elsewhere
 						permission.mode.add(Constants.SUBSCRIBE);
 					}
 					if (string.equalsIgnoreCase("notify")) {
 						permission.mode.add(Constants.NOTIFY);
 					}
 				}
 			}
 
 			String id = client.addPermission(permission);
 			flash.success("Permission has been created!");
 			permission(id);
 		} catch (Exception e) {
 			handleException(e.getMessage(), e);
 		}
 	}
 }
