 /**
  * A RESTful web service on top of DSpace.
  * Copyright (C) 2010-2011 National Library of Finland
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
  */
 package fi.helsinki.lib.simplerest;
 
 import fi.helsinki.lib.simplerest.stubs.StubCommunity;
 import com.google.gson.Gson;
 import org.dspace.core.Context;
 import org.dspace.content.Community;
 import org.dspace.content.Bitstream;
 
 import java.sql.SQLException;
 import java.util.LinkedList;
 import java.util.HashSet;
 import java.util.logging.Level;
 
 import org.restlet.ext.xml.DomRepresentation;
 import org.restlet.representation.Representation;
 import org.restlet.representation.InputRepresentation;
 import org.restlet.resource.Get;
 import org.restlet.resource.Put; 
 import org.restlet.resource.Post;
 import org.restlet.resource.Delete;
 import org.restlet.resource.ResourceException;
 import org.restlet.data.MediaType;
 import org.restlet.data.Status;
 import org.restlet.data.Method;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.Priority;
 
 public class CommunityResource extends BaseResource {
 
     private static Logger log = Logger.getLogger(CommunityResource.class);
     private int communityId;
     private Community comm;
     
     public CommunityResource(Community co, int communityId){
         this.communityId = communityId;
         this.comm = co;
     }
     
     static public String relativeUrl(int communityId) {
         return "community/" + communityId;
     }
     
     @Override
     protected void doInit() throws ResourceException {
         try {
             String id = (String)getRequest().getAttributes().get("communityId");
             this.communityId = Integer.parseInt(id);
         }
         catch (NumberFormatException e) {
             ResourceException resourceException =
                 new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                                       "Community ID must be a number.");
             throw resourceException;
         }
     }
 
     // TODO: parent?
     @Get("html|xhtml|xml")
     public Representation toXml() {
         DomRepresentation representation;
         Document d;
         Context context = null;
         Community community = null;
         try{
             context = new Context();
             community = Community.find(context, communityId);
         } catch (Exception ex) {
             java.util.logging.Logger.getLogger(CommunityResource.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         try {
             representation = new DomRepresentation(MediaType.ALL);  
             d = representation.getDocument();
         }
         catch (Exception e) {
             return errorInternal(context, e.toString());
         }
 
         Element html = d.createElement("html");  
         d.appendChild(html);
 
         Element head = d.createElement("head");
         html.appendChild(head);
 
         Element title = d.createElement("title");
         head.appendChild(title);
         title.appendChild(d.createTextNode("Community " + community.getName()));
 
         Element body = d.createElement("body");
         html.appendChild(body);
 	
         Element dl = d.createElement("dl");
         setId(dl, "attributes");
         body.appendChild(dl);
 
         Element dtName = d.createElement("dt");
         dtName.appendChild(d.createTextNode("name"));
         dl.appendChild(dtName);
         Element ddName = d.createElement("dd");
         ddName.appendChild(d.createTextNode(community.getName()));
         dl.appendChild(ddName);
 
         String[] attributes = { "short_description", "introductory_text",
                                 "copyright_text", "side_bar_text" };
         for (String attribute : attributes) {
             Element dt = d.createElement("dt");
             dt.appendChild(d.createTextNode(attribute));
             dl.appendChild(dt);
 
             Element dd = d.createElement("dd");
             dd.appendChild(d.createTextNode(community.getMetadata(attribute)));
             dl.appendChild(dd);
         }
 
         Bitstream logo = community.getLogo();
         if (logo != null) {
             Element aLogo = d.createElement("a");
             String url = baseUrl() +
                 CommunityLogoResource.relativeUrl(this.communityId);
             //getRequest().getResourceRef().getIdentifier() + "/logo";
             setAttribute(aLogo, "href", url);
             setId(aLogo, "logo");
             aLogo.appendChild(d.createTextNode("Community logo"));
             body.appendChild(aLogo);
         }
         
         String url = null;
         
         try{
             url = getRequest().getResourceRef().getIdentifier();
         }catch(NullPointerException e){
             url = "";
         }
 
 	// A link to sub communities
         Element pSubCommunities = d.createElement("p");
         Element aSubCommunities = d.createElement("a");
 	setAttribute(aSubCommunities, "href", url + "/communities");
         setId(aSubCommunities, "communities");
 	aSubCommunities.appendChild(d.createTextNode("communities"));
         pSubCommunities.appendChild(aSubCommunities);
         body.appendChild(pSubCommunities);
 
 	// A link to child collections
         Element pSubCollections = d.createElement("p");
         Element aSubCollections = d.createElement("a");
 	setAttribute(aSubCollections, "href", url + "/collections");
         setId(aSubCollections, "collections");
 	aSubCollections.appendChild(d.createTextNode("collections"));
         pSubCollections.appendChild(aSubCollections);
         body.appendChild(pSubCollections);
         
         try{
             context.abort();
         }catch(NullPointerException e){
             Logger.getLogger(CommunitiesResource.class.getName()).log(url, Priority.WARN, e.toString(), e);
         }
 
         return representation;
     }
     
     @Get("json")
     public String toJson(){
         Gson gson = new Gson();
         Context context = null;
         try{
             context = new Context();
             comm = Community.find(context, communityId);
         }catch(Exception e){
             Logger.getLogger(CommunityResource.class).log(null, Priority.INFO, e, e);
         }
         StubCommunity s = new StubCommunity(comm.getID(), comm.getName(), comm.getMetadata("short_description"),
                     comm.getMetadata("introductory_text"), comm.getMetadata("copyright_text"), comm.getMetadata("side_bar_text"));
         try{
             context.abort();
         }catch(NullPointerException e){
             Logger.getLogger(CommunitiesResource.class.getName()).log(null, Priority.INFO, e.toString(), e);
         }
         return gson.toJson(s);
     }
 
     @Put
     public Representation edit(InputRepresentation rep) {
         Context c = null;
         Community community;
         try {
             c = getAuthenticatedContext();
             community = Community.find(c, this.communityId);
             if (community == null) {
                 return errorNotFound(c, "Could not find the community.");
             }
         }
         catch (SQLException e) {
             return errorInternal(c, "SQLException "+e.getMessage());
         }
 
         DomRepresentation dom = new DomRepresentation(rep);
 
         Node attributesNode = dom.getNode("//dl[@id='attributes']");
         if (attributesNode == null) {
             return error(c, "Did not find dl tag with an id 'attributes'.",
                          Status.CLIENT_ERROR_BAD_REQUEST);
         }
 	
         community.setMetadata("name", null);
         community.setMetadata("short_description", null);
         community.setMetadata("introductory_text", null);
         community.setMetadata("copyright_text", null);
         community.setMetadata("side_bar_text", null);
 
         NodeList nodes = attributesNode.getChildNodes();
 	LinkedList<String> dtList = new LinkedList();
 	LinkedList<String> ddList = new LinkedList();
 	int nNodes = nodes.getLength();
 	for (int i=0; i < nNodes; i++) {
 	    Node node = nodes.item(i);
 	    String nodeName = node.getNodeName();
 	    if (nodeName.equals("dt")) {
 		dtList.add(node.getTextContent());
 	    }
 	    else if (nodeName.equals("dd")) {
 		ddList.add(node.getTextContent());
 	    }
 	}
 	if (dtList.size() != ddList.size()) {
 	    return error(c, "The number of <dt> and <dd> elements do not match.",
 			 Status.CLIENT_ERROR_BAD_REQUEST);
 	}
         int size = dtList.size();
         for (int i=0; i < size; i++) {
             String dt = dtList.get(i);
             String dd = ddList.get(i);
             if (dt.equals("name") ||
                 dt.equals("short_description") ||
                 dt.equals("introductory_text") ||
                 dt.equals("copyright_text") ||
                 dt.equals("side_bar_text")) {
                 community.setMetadata(dt, dd);
             }
             else {
                 return error(c, "Unexpected data in attributes: " + dt,
                              Status.CLIENT_ERROR_BAD_REQUEST);
 	    }
 	}
 
         try {
             community.update();
             c.complete();
         }
         catch (Exception e) {
             return errorInternal(c, e.toString());
         }
 
         return successOk("Community updated.");
     }
 
     @Post
     public Representation post(Representation dummy) {
         HashSet<Method> allowed = new HashSet();
         allowed.add(Method.GET);
         allowed.add(Method.PUT);
         allowed.add(Method.DELETE);
         setAllowedMethods(allowed);
         return error(null,
                      "Community resource does not allow POST method.",
                      Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
     }
 
     @Delete
     public Representation delete() {
         Context c = null;
         Community community;
         try {
             c = getAuthenticatedContext();
             community = Community.find(c, this.communityId);
             if (community == null) {
                 return errorNotFound(c, "Could not find the community.");
             }
 
             community.delete();
             c.complete();
         }
         catch (Exception e) {
             return errorInternal(c, e.toString());
         }
 
         return successOk("Community deleted.");
     }
 }
