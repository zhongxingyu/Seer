 /*  
  * This file is part of dropvault.
  *
  * dropvault is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * dropvault is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with dropvault.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.aperigeek.dropvault.dav;
 
 import com.aperigeek.dropvault.Resource;
 import com.aperigeek.dropvault.Resource.ResourceType;
 import com.aperigeek.dropvault.dav.http.HttpMkcol;
 import com.aperigeek.dropvault.dav.http.HttpPropfind;
 import com.aperigeek.dropvault.dav.http.UnknownTypeFileEntity;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;
 import org.apache.http.HttpResponse;
 import org.apache.http.auth.AuthScope;
 import org.apache.http.auth.UsernamePasswordCredentials;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.Namespace;
 import org.jdom.input.SAXBuilder;
 
 /**
  *
  * @author Vivien Barousse
  */
 public class DAVClient {
     
     public static final String DATE_FORMAT = "EEE, d MMM yyyy kk:mm:ss z";
     
     public static final Namespace DAV_NS = Namespace.getNamespace("DAV:");
     
     protected DefaultHttpClient client;
     
     protected DateFormat dateFormat;
     
     public DAVClient(String username, String password) {
         client = new DefaultHttpClient();
         
         client.getCredentialsProvider().setCredentials(AuthScope.ANY, 
                 new UsernamePasswordCredentials(username, password));
         
         dateFormat = new SimpleDateFormat(DATE_FORMAT);
     }
     
     public Resource getResource(String uri) throws DAVException {
         HttpPropfind propfind = new HttpPropfind(uri);
         propfind.addHeader("Depth", "0");
 
         try {
             HttpResponse response = client.execute(propfind);
             
             if (response.getStatusLine().getStatusCode() == 401) {
                 throw new InvalidPasswordException();
             }
             
             if (response.getStatusLine().getStatusCode() != 207) {
                 throw new DAVException("Unexpected HTTP code: " + response.getStatusLine().getStatusCode());
             }
             
             InputStream in = response.getEntity().getContent();
             
             SAXBuilder builder = new SAXBuilder();
             Document document = builder.build(in);
             Element element = document.getRootElement()
                     .getChild("response", DAV_NS);
             
             return buildResource(element);
         } catch (ParseException ex) {
             throw new DAVException("Error in XML returned by server", ex);
         } catch (JDOMException ex) {
             throw new DAVException("Error in XML returned by server", ex);
         } catch (IOException ex) {
             throw new DAVException(ex);
         }
     }
     
     public List<Resource> getResources(Resource parent) throws DAVException {
         return getResources(parent, 1);
     }
     
     public List<Resource> getResources(Resource parent, int depth) throws DAVException {
         return getResources(parent, 
                 depth < 0 ? "Infinity" : Integer.toString(depth));
     }
     
     public InputStream get(Resource res) throws DAVException {
         try {
             HttpGet get = new HttpGet(res.getHref());
             return client.execute(get).getEntity().getContent();
         } catch (IOException ex) {
             throw new DAVException(ex);
         }
     }
     
     public void put(String uri, File file) throws DAVException {
         try {
             HttpPut put = new HttpPut(uri);
             put.setEntity(new UnknownTypeFileEntity(file));
             
             HttpResponse response = client.execute(put);
             if (response.getStatusLine().getStatusCode() != 200) {
                 throw new DAVException("Invalid status code:"
                         + response.getStatusLine().getStatusCode()
                         + " " 
                         + response.getStatusLine().getReasonPhrase());
             }
         } catch (IOException ex) {
             throw new DAVException(ex);
         }
     }
     
     public void delete(String uri) throws DAVException {
         try {
             HttpDelete delete = new HttpDelete(uri);
             
             HttpResponse response = client.execute(delete);
             if (response.getStatusLine().getStatusCode() != 200) {
                 throw new DAVException("Invalid status code:"
                         + response.getStatusLine().getStatusCode()
                         + " " 
                         + response.getStatusLine().getReasonPhrase());
             }
         } catch (IOException ex) {
             throw new DAVException(ex);
         }
     }
     
     public void mkcol(String uri) throws DAVException {
         try {
             HttpMkcol mkcol = new HttpMkcol(uri);
             
             HttpResponse response = client.execute(mkcol);
             if (response.getStatusLine().getStatusCode() != 201) {
                 throw new DAVException("Invalid status code:"
                         + response.getStatusLine().getStatusCode()
                         + " " 
                         + response.getStatusLine().getReasonPhrase());
             }
         } catch (IOException ex) {
             throw new DAVException(ex);
         }
     }
     
     protected List<Resource> getResources(Resource parent, String depth) throws DAVException {
         HttpPropfind propfind = new HttpPropfind(parent.getHref());
         propfind.addHeader("Depth", depth);
         
         try {
             HttpResponse response = client.execute(propfind);
             
             InputStream in = response.getEntity().getContent();
             
             SAXBuilder builder = new SAXBuilder();
             Document document = builder.build(in);
             List<Element> elements = document.getRootElement()
                     .getChildren("response", DAV_NS);
             
             List<Resource> resources = buildResources(elements);
             resources.remove(parent);
             
             return resources;
         } catch (ParseException ex) {
             throw new DAVException("Error in XML returned by server", ex);
         } catch (JDOMException ex) {
             throw new DAVException("Error in XML returned by server", ex);
         } catch (IOException ex) {
             throw new DAVException(ex);
         }
     }
     
     protected List<Resource> buildResources(List<Element> resps) throws ParseException {
         List<Resource> resources = new ArrayList<Resource>();
         for (Element resp : resps) {
             resources.add(buildResource(resp));
         }
         return resources;
     }
     
     protected Resource buildResource(Element resp) throws ParseException {
         Resource r = new Resource();
         
         r.setHref(resp.getChild("href", DAV_NS).getTextTrim());
         
         Element prop = resp.getChild("propstat", DAV_NS)
                 .getChild("prop", DAV_NS);
         
         r.setName(prop.getChild("displayname", DAV_NS).getTextTrim());
         r.setType(isFolder(prop) ? ResourceType.FOLDER : ResourceType.FILE);
         
         String lastModified = prop.getChild("getlastmodified", DAV_NS).getTextTrim();
         r.setLastModificationDate(dateFormat.parse(lastModified));
         
         if (r.getType() == ResourceType.FILE) {
             r.setContentType(prop.getChild("getcontenttype", DAV_NS).getTextTrim());
         }
         
         return r;
     }
     
     protected boolean isFolder(Element prop) {
         Element restype = prop.getChild("resourcetype", DAV_NS);
         if (restype == null) {
             return false;
         }
         
         Element collection = restype.getChild("collection", DAV_NS);
         if (collection == null) {
             return false;
         }
         
         return true;
     }
     
 }
