 /**
  *
  * Copyright (c) 2012, PetalsLink
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
 package org.ow2.play.metadata.service.rest;
 
 import java.util.ArrayList;
 import java.util.List;
 
import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.Response;
 
 import org.ow2.play.metadata.api.MetadataException;
 import org.ow2.play.metadata.api.service.rest.MetadataService;
 
 /**
  * @author chamerling
  * 
  */
 public class MetadataServiceImpl implements MetadataService {
 
 	private org.ow2.play.metadata.api.service.MetadataService metadataService;
 
 	private org.ow2.play.metadata.api.service.MetadataBootstrap bootstrap;
 
 	public Response clear() {
 		if (metadataService == null) {
 			return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
 		}
 		try {
 			this.metadataService.clear();
 		} catch (MetadataException e) {
 		}
 		return Response.ok("Data deleted").build();
 	}
 
	public Response load(@QueryParam("url") String url) {
 		if (bootstrap == null || url == null) {
 			return Response.serverError().build();
 		}
 
 		List<String> urls = new ArrayList<String>();
 		urls.add(url);
 
 		try {
 			bootstrap.init(urls);
 		} catch (MetadataException e) {
 			e.printStackTrace();
 			return Response.serverError().build();
 		}
 
 		return Response.ok("Service initialized").build();
 	}
 
 	public void setBootstrap(
 			org.ow2.play.metadata.api.service.MetadataBootstrap bootstrap) {
 		this.bootstrap = bootstrap;
 	}
 
 	public void setMetadataService(
 			org.ow2.play.metadata.api.service.MetadataService metadataService) {
 		this.metadataService = metadataService;
 	}
 
 }
