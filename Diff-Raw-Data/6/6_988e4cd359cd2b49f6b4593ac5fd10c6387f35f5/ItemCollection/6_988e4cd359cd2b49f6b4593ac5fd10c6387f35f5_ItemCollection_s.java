 /*
  * $Id: ContextPathResolver.java 794 2008-04-23 22:23:10Z andrew $
  * --------------------------------------------------------------------------------------
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 
 package org.mule.galaxy.atom;
 
 import static org.mule.galaxy.util.AbderaUtils.assertNotEmpty;
 import static org.mule.galaxy.util.AbderaUtils.createArtifactPolicyExceptionResponse;
 import static org.mule.galaxy.util.AbderaUtils.newErrorMessage;
 import static org.mule.galaxy.util.AbderaUtils.throwMalformed;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.activation.MimeType;
 import javax.xml.namespace.QName;
 
 import org.apache.abdera.Abdera;
 import org.apache.abdera.factory.Factory;
 import org.apache.abdera.i18n.iri.IRI;
 import org.apache.abdera.i18n.text.UrlEncoding;
 import org.apache.abdera.i18n.text.CharUtils.Profile;
 import org.apache.abdera.model.AtomDate;
 import org.apache.abdera.model.Content;
 import org.apache.abdera.model.Document;
 import org.apache.abdera.model.Element;
 import org.apache.abdera.model.Entry;
 import org.apache.abdera.model.ExtensibleElement;
 import org.apache.abdera.model.Person;
 import org.apache.abdera.model.Text;
 import org.apache.abdera.parser.ParseException;
 import org.apache.abdera.parser.Parser;
 import org.apache.abdera.protocol.server.RequestContext;
 import org.apache.abdera.protocol.server.RequestContext.Scope;
 import org.apache.abdera.protocol.server.context.EmptyResponseContext;
 import org.apache.abdera.protocol.server.context.ResponseContextException;
 import org.apache.abdera.protocol.server.impl.AbstractEntityCollectionAdapter;
 import org.apache.commons.lang.BooleanUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.mule.galaxy.DuplicateItemException;
 import org.mule.galaxy.Item;
 import org.mule.galaxy.NewItemResult;
 import org.mule.galaxy.NotFoundException;
 import org.mule.galaxy.PropertyException;
 import org.mule.galaxy.PropertyInfo;
 import org.mule.galaxy.Registry;
 import org.mule.galaxy.RegistryException;
 import org.mule.galaxy.artifact.Artifact;
 import org.mule.galaxy.extension.AtomExtension;
 import org.mule.galaxy.extension.Extension;
 import org.mule.galaxy.policy.PolicyException;
 import org.mule.galaxy.security.AccessException;
 import org.mule.galaxy.security.User;
 import org.mule.galaxy.type.PropertyDescriptor;
 import org.mule.galaxy.type.Type;
 import org.mule.galaxy.type.TypeManager;
 import org.mule.galaxy.util.AbderaUtils;
 import org.mule.galaxy.util.SecurityUtils;
 
 public class ItemCollection  
     extends AbstractEntityCollectionAdapter<Item> {
     public static final String NAMESPACE = "http://galaxy.mule.org/2.0";
     public static final String ID_PREFIX = "urn:galaxy:artifact:";    
     public static final QName VERSION_QNAME = new QName(NAMESPACE, "version");
 
     protected final Log log = LogFactory.getLog(getClass());
 
     protected Factory factory;
     protected Registry registry;
     private final TypeManager typeManager;
     
     public ItemCollection(Registry registry, TypeManager typeManager) {
         super();
         this.registry = registry;
         this.typeManager = typeManager;
         factory = Abdera.getInstance().getFactory();
     }
 
     public Content getContent(Item doc, RequestContext request) {
         Content content = request.getAbdera().getFactory().newContent();
 
         String description = (String) doc.getProperty("description");
         if (description != null) {
             content.setText(description);
         } else {
             content.setText("");
         }
         return content;
     }
    
     @Override
     protected String addEntryDetails(RequestContext request, 
                                      Entry atomEntry, 
                                      IRI feedIri, 
                                      Item item)
         throws ResponseContextException {
         String link = super.addEntryDetails(request, atomEntry, feedIri, item);
         
         Element itemInfo = factory.newElement(new QName(NAMESPACE, "item-info"));
         atomEntry.addExtension(itemInfo);
         itemInfo.setAttributeValue("created", AtomDate.format(item.getCreated().getTime()));
         itemInfo.setAttributeValue("name", item.getName());
         itemInfo.setAttributeValue("type", item.getType().getName());
         
         String param = request.getParameter("showProperties");
         boolean showProperties = BooleanUtils.toBoolean(param);
         if (param == null || showProperties) {
             addMetadata(item, atomEntry, request);
         }
         
         List<org.mule.galaxy.type.Type> allowed = item.getType().getAllowedChildren();
         if (allowed != null && allowed.size() > 0) {
             org.apache.abdera.model.Collection col = factory.newCollection();
             col.setAttributeValue("id", "versions");
             
             String href = getRelativeLink(request, item).toString();
             if (item.getType().inheritsFrom(TypeManager.ARTIFACT)) {
                 href += ";children";
             }
             
             col.setHref(href);
             col.setTitle("Child Items");
             atomEntry.addExtension(col);
         }
         
         return link;
     }
 
     protected void addMetadata(Item entryObj, Entry atomEntry, RequestContext request) {
         boolean showHidden = BooleanUtils.toBoolean(request.getParameter("showHiddenProperties"));
         
         ExtensibleElement metadata = factory.newElement(new QName(NAMESPACE, "metadata"));
         
         for (PropertyInfo p : entryObj.getProperties()) {
             PropertyDescriptor pd = p.getPropertyDescriptor();
             
             if (p.isVisible() || showHidden) {
                 if (pd != null && pd.getExtension() instanceof AtomExtension) {
                     ((AtomExtension) pd.getExtension()).annotateAtomEntry(entryObj, pd, atomEntry, metadata, factory);
                 } else {
                     Element prop = factory.newElement(new QName(NAMESPACE, "property"), metadata);
                     prop.setAttributeValue("name", p.getName());
                     prop.setAttributeValue("locked", new Boolean(p.isLocked()).toString());
                     
                     if (p.isVisible()) {
                         prop.setAttributeValue("visible", new Boolean(p.isVisible()).toString());
                     }
                     
                     Object value = p.getValue();
                     if (value == null) {
                         value = "";
                     }
                     
                     if (value instanceof Collection) {
                         for (Object o : ((Collection) value)) {
                             Element valueEl = factory.newElement(new QName(NAMESPACE, "value"), prop);
                             
                             valueEl.setText(o.toString());
                         }
                     } else {
                         prop.setAttributeValue("value", value.toString());
                     }
                 }
             }
         }
         
         atomEntry.addExtension(metadata);
     }
 
     @Override
     public Text getSummary(Item item, RequestContext request) {
         Artifact artifact = getArtifact(item);
         if (artifact != null) {
             String description = (String)item.getProperty("description");
             if (description == null) {
                 description = "";
             }
             
             Text summary = factory.newSummary();
             
             summary.setText(description);
             summary.setTextType(org.apache.abdera.model.Text.Type.XHTML);
             
             return summary;
         }
         return null;
     }
 
     public String getTitle(Item doc) {
         return doc.getName();
     }
     
     public InputStream getMediaStream(Item item) throws ResponseContextException {
         Artifact a = getArtifact(item);
         
        return a.getInputStream();
     }
     
     @Override
     public String getContentType(Item item) {
         Artifact artifact = getArtifact(item);
         if (artifact != null) {
             return artifact.getContentType().toString();
         }
         throw new UnsupportedOperationException();
     }
 
     private Artifact getArtifact(Item item) {
         return (Artifact) item.getProperty("artifact");
     }
 
     public String getAuthor(RequestContext request) {
         return "Mule Galaxy";
     }
 
     public Date getUpdated(Item doc) {
         return doc.getUpdated().getTime();
     }
 
     @Override
     public Item postMedia(MimeType mimeType, 
                           String slug, 
                           InputStream inputStream,
                           RequestContext request) throws ResponseContextException {
         try {
             Item parent = (Item) request.getAttribute(Scope.REQUEST, ItemResolver.ITEM);
 
             if (parent == null) {
                 EmptyResponseContext ctx = new EmptyResponseContext(500);
                 ctx.setStatusText("The specified parent item is invalid. Please POST to a valid item URL.");
                 
                 throw new ResponseContextException(ctx);
             }
 
             Map<String,Object> props = new HashMap<String,Object>();
             props.put("artifact", new Object[] { inputStream, mimeType.toString() });
             
             // If we're posting to an artifact, add it as a new version
             if (parent.getType().inheritsFrom(TypeManager.ARTIFACT)) {
                 String version = request.getParameter("X-Artifact-Version");
                 if (version == null) {
                     version = slug;
                 }
                 
                 NewItemResult result = parent.newItem(version, typeManager.getType(TypeManager.ARTIFACT_VERSION), props);
 
                 return result.getItem();
             } else {
                 // otherwise create a new artifact and version
                 String version = getVersion(request);
                 
                 NewItemResult result = parent.newItem(slug, typeManager.getType(TypeManager.ARTIFACT));
                 
                 result = result.getItem().newItem(version, typeManager.getType(TypeManager.ARTIFACT_VERSION), props);
                 
                 return result.getItem();
             }
         } catch (DuplicateItemException e) {
             throw newErrorMessage("Duplicate artifact.", "An artifact with that name already exists in this workspace.", 409);
         } catch (RegistryException e) {
             log.error("Could not add artifact.", e);
             throw new ResponseContextException(500, e);
         } catch (PolicyException e) {
             throw AbderaUtils.createArtifactPolicyExceptionResponse(e);
         } catch (AccessException e) {
             throw new ResponseContextException(401, e);
         } catch (NotFoundException e) {
             throw new ResponseContextException(500, e);
         } catch (PropertyException e) {
             throw newErrorMessage("Invalid or missing properties.", e.getMessage(), 400);
         }
     }
     
     protected User getUser() {
         return SecurityUtils.getCurrentUser();
     }
 
     protected String getVersion(RequestContext request) throws ResponseContextException {
         String version = request.getHeader("X-Artifact-Version");
         
         if (version == null || version.equals("")) {
             EmptyResponseContext ctx = new EmptyResponseContext(500);
             ctx.setStatusText("You must supply an X-Artifact-Version header!");
             
             throw new ResponseContextException(ctx);
         }
         return version;
     }
 
     @Override
     public boolean isMediaEntry(Item entry) {
         return entry.getType().inheritsFrom(TypeManager.ARTIFACT_VERSION);
     }
 
     @Override
     public List<Person> getAuthors(Item entry, RequestContext request) throws ResponseContextException {
         Person author = request.getAbdera().getFactory().newAuthor();
         author.setName(entry.getAuthor().getName());
         return Arrays.asList(author);
     }
     
     @Override
     public String getMediaName(Item item) {
         return UrlEncoding.encode(item.getName());
     }
 
     @Override
     public String getHref(RequestContext request) {
         String href = (String) request.getAttribute(Scope.REQUEST, ItemResolver.COLLECTION_HREF);
         if (href == null) {
             // this is the url we use when pulling down the services document
             href = request.getTargetBasePath() + "/registry";
         }
         return href;
     }
 
     public String getId(Item item) {
         return ID_PREFIX + item.getId();
     }
 
     @Override
     protected String getFeedIriForEntry(Item item, RequestContext request) {
         Item w = (Item) item.getParent();
         String path;
         if (w == null) {
             path = "";
         } else {
             path = UrlEncoding.encode(w.getPath(), Profile.PATH.filter());
         }
         return request.getTargetBasePath() + "/registry" + path;
     }
 
     @Override
     public String getName(Item doc) {
         return getNameOfArtifact(doc) + ";atom";
     }
     
     public String getNameOfArtifact(Item i) {
         return UrlEncoding.encode(i.getName(), Profile.PATH.filter());
     }
     
     public Item getEntry(String name, RequestContext request) throws ResponseContextException {
         return getRegistryItem(request);
     }
 
     protected Item getRegistryItem(RequestContext request) {
         return  (Item) request.getAttribute(Scope.REQUEST, ItemResolver.ITEM);
     }
 
     protected String getVersionLabel(Entry atomEntry) throws ResponseContextException {
         Element version = atomEntry.getExtension(VERSION_QNAME);
         
         if (version == null || version.getAttributeValue("label") == null) {
             throw newErrorMessage("Invalid version label", "A version element must be specified with a label attribute.", 500);
         }
         
         return version.getAttributeValue("label");
     }
 
 
     @Override
     public void putEntry(Item item, 
                          String title, 
                          Date updated, 
                          List<Person> authors, 
                          String summary,
                          Content content, 
                          RequestContext request) throws ResponseContextException {
         try {
             Document<Entry> entryDoc = request.getDocument();
             Entry atomEntry = entryDoc.getRoot();
             
             String desc = content.getValue();
             if (desc != null && desc.length() > 0) {
                 item.setProperty("description", desc);
             }
             
             mapEntryExtensions(item, atomEntry);
 
             registry.save(item);
         } catch (ParseException e) {
             throw new ResponseContextException(500, e);
         } catch (NotFoundException e) {
             throw new ResponseContextException(400, e);
         } catch (IOException e) {
             throw new ResponseContextException(500, e);
         } catch (RegistryException e) {
             throw new ResponseContextException(500, e);
         } catch (PropertyException e) {
             throw new ResponseContextException(500, e);
         } catch (PolicyException e) {
             throw AbderaUtils.createArtifactPolicyExceptionResponse(e);
         } catch (AccessException e) {
             throw new ResponseContextException(401, e);
         }
     }
 
     private void mapEntryExtensions(Item item, Entry atomEntry) throws ResponseContextException,
             PolicyException, RegistryException, AccessException, NotFoundException, PropertyException {
         Map<String,Object> props = new HashMap<String, Object>();
         List<String> invisible = new ArrayList<String>();
         
         mapEntryExtensions(item, props, invisible, atomEntry, false);
         for (Map.Entry<String, Object> e : props.entrySet()) {
             item.setProperty(e.getKey(), e.getValue());
             item.setVisible(e.getKey(), !invisible.contains(e.getKey()));
         }
     }
 
     protected void mapEntryExtensions(Item item, 
                                       Map<String,Object> properties,
                                       List<String> invisible,
                                       Entry entry, boolean newItem) throws ResponseContextException,
         PolicyException, RegistryException, AccessException {
         for (Element e : entry.getElements()) {
             QName q = e.getQName();
             
             if (NAMESPACE.equals(q.getNamespaceURI())) {
                 if ("metadata".equals(q.getLocalPart())) {
                     updateMetadata(item, properties, invisible, e);
                 } else if ("item-info".equals(q.getLocalPart()) && !newItem) {
                     updateItemInfo(item, e);
                 }
             }
         }
     }
 
     private void updateItemInfo(Item item, Element e) throws ResponseContextException {
         String name = e.getAttributeValue("name");
         
         if (name != null) {
             item.setName(name);
         }
         
         String typeName = e.getAttributeValue("type");
         if (typeName != null) {
             try {
                 Type type = typeManager.getType(typeName);
                 if (!type.equals(item.getType())) {
                     item.setType(type);
                 }
             }  catch (NotFoundException ex) {
                 throw newErrorMessage("Could not find type.", "Type " + ex.getMessage() + " does not exist.", 400);
             } catch (PropertyException ex) {
                 throw newErrorMessage("Invalid or missing properties.", ex.getMessage(), 400);
             }
         }
     }
 
     private AtomExtension getExtension(QName q) {
         for (Extension e : registry.getExtensions()) {
             if (e instanceof AtomExtension && ((AtomExtension) e).getUnderstoodElements().contains(q)) {
                 return (AtomExtension) e;
             }
         }
         return null;
     }
 
     
     private void updateMetadata(Item item,
                                 Map<String, Object> properties, 
                                 List<String> invisible,
                                 Element e) throws ResponseContextException, PolicyException, AccessException {
         for (Element propEl : e.getElements()) {
             QName q = propEl.getQName();
             
             AtomExtension atomExt = getExtension(q);
             if (atomExt != null) {
                 String property = propEl.getAttributeValue("property");
                 assertNotEmpty(property, "Lifecycle property attribute cannot be null.");
 
                 Object value = atomExt.getValue(item, factory, (ExtensibleElement) propEl);
                 if (value != null) {
                     properties.put(property, value);
                 }
             }  else {
                 String name = propEl.getAttributeValue("name");
                 if (name == null)
                     throwMalformed("You must specify name attributes on metadata properties.");
                 
                 String value = propEl.getAttributeValue("value");
 
                 if (value != null) {
                     properties.put(name, value);
                 } else if ("".equals(value)) {
                     // remove the property
                     properties.put(name, null);
                 } else {
                     List<Element> elements = propEl.getElements();
                     ArrayList<String> values = new ArrayList<String>();
                     for (Element valueEl : elements) {
                         if (valueEl.getQName().getLocalPart().equals("value")) {
                             values.add(valueEl.getText().trim());
                         }
                     }
                     
                     if (values.size() == 0) {
                         // remove the property
                         properties.put(name, null);
                     } else {
                         properties.put(name, values);
                     }
                 }
                 
                 String visible = propEl.getAttributeValue("visible");
                 if (visible != null && !BooleanUtils.toBoolean(visible)) {
                     invisible.add(visible);
                 }
             }
         }
     }
 
     public void deleteEntry(String name, RequestContext request) throws ResponseContextException {
         Item entry = getRegistryItem(request);
 
         try {
             if (entry != null) {
                 entry.delete();
             }
         } catch (RegistryException e) {
             throw new RuntimeException(e);
         } catch (AccessException e) {
             throw new ResponseContextException(405, e);
         }
     }
 
     public void deleteMedia(String name, RequestContext request) throws ResponseContextException {
         Item entry = getRegistryItem(request);
 
         try {
             entry.delete();
         } catch (RegistryException e) {
             throw new RuntimeException(e);
         } catch (AccessException e) {
             throw new ResponseContextException(405, e);
         }
     }
 
     private IRI getRelativeLink(RequestContext request, Item entryObj) {
         return new IRI(getHref(request)).resolve(getNameOfArtifact(entryObj));
     }
 
 
     public String getId(RequestContext request) {
         Item item = getRegistryItem(request);
         if (item == null) {
             return "tag:galaxy.mulesource.com,2008:registry:" + registry.getUUID() + ":feed";
         } else {
             return "tag:galaxy.mulesource.com,2008:registry:" + item.getId() + ":feed";
         }
     }
     
     public String getTitle(RequestContext request) {
         return "Mule Galaxy Registry/Repository";
     }
 
     public Iterable<Item> getEntries(RequestContext request) throws ResponseContextException {
         try {
             String q = request.getParameter("q");
             
             if (q == null || "".equals(q)) {
                 Item item = getRegistryItem(request);
                 if (item == null) {
                     List<Item> items = new ArrayList<Item>();
                     for (Item w : registry.getItems()) {
                         items.add(w);
                     }
                     return items;
                 } else {
                     return ((Item) item).getItems();
                 }
                 
             } else {
                 q = UrlEncoding.decode(q);
             }
             
             String startStr = request.getParameter("start");
             String countStr = request.getParameter("count");
             int start = 0;
             int count = 100;
             
             if (startStr != null) {
                 start = Integer.valueOf(startStr);
             }
 
             if (countStr != null) {
                 count = Integer.valueOf(countStr);
             }
             
             return registry.search(q, start, count).getResults();
         } catch (RegistryException e) {
             throw new ResponseContextException(500, e);
         } catch (AccessException e) {
             throw new ResponseContextException(401, e);
         }
     }
     
     @Override
     public Item postEntry(String title, IRI id, String summary, Date updated, List<Person> authors,
                           Content content, RequestContext request) throws ResponseContextException {
         Item parent = (Item) request.getAttribute(Scope.REQUEST, ItemResolver.ITEM);
 
         try {
             Document<Entry> e = request.getDocument();
             Entry atomEntry = e.getRoot();
             Element itemInfo = atomEntry.getExtension(new QName(NAMESPACE, "item-info"));
             
             if (itemInfo == null) {
                 throw newErrorMessage("Invalid request", "An item-info element is required.", 400);
             }
             
             // Update property information
             Map<String,Object> props = new HashMap<String, Object>();
             List<String> invisible = new ArrayList<String>();
             
             mapEntryExtensions(parent, props, invisible, atomEntry, true);
             
             String name = itemInfo.getAttributeValue("name");
             String typeName = itemInfo.getAttributeValue("type");
             
             if (name == null) {
                 name = title;
             }
             
             Type type;
             if (typeName != null) {
                 type = typeManager.getType(typeName);
             } else {
                 type = typeManager.getDefaultType();
             }
 
             Item item;
             if (parent == null) {
                 item = registry.newItem(name, type, props).getItem();
             } else { 
                 item = parent.newItem(name, type, props).getItem();
             }
             
             String desc = content.getValue();
             if (desc != null && desc.length() > 0) {
                 item.setProperty("description", desc);
             }
             
             for (String prop : invisible) {
                 item.setVisible(prop, false);
             }
             
             itemInfo.discard();
             for (Person a : atomEntry.getAuthors()) {
                 a.discard();
             }
             
             // fill in the response information
             addEntryDetails(request, atomEntry, getFeedIRI(item, request), item);
             
             registry.save(item);
             
             return item;
         } catch (DuplicateItemException e) {
             throw newErrorMessage("Duplicate artifact.", "An artifact with that name already exists in this workspace.", 409);
         } catch (RegistryException e) {
             log.error("Could not add artifact.", e);
             throw new ResponseContextException(500, e);
         } catch (IOException e) {
             throw new ResponseContextException(500, e);
         } catch (PolicyException e) {
             throw createArtifactPolicyExceptionResponse(e);
         } catch (AccessException e) {
             throw new ResponseContextException(401, e);
         } catch (NotFoundException e) {
             throw newErrorMessage("Could not find type.", "Type " + e.getMessage() + " does not exist.", 400);   
         } catch (PropertyException e) {
             log.error("Could not set property.", e);
             throw new ResponseContextException(500, e);
         }
     }
 
     private IRI getFeedIRI(Item entryObj, RequestContext request) {
       String feedIri = getFeedIriForEntry(entryObj, request);
       return new IRI(feedIri).trailingSlash();
     }
     
     /**
      * Override this method so we don't clone the document and we can actually modify the POST response....
      * 
      */
     protected Entry getEntryFromRequest(RequestContext request) throws ResponseContextException {
         Abdera abdera = request.getAbdera();
         Parser parser = abdera.getParser();
 
         Document<Element> entry_doc;
         try {
             entry_doc = request.getDocument(parser);
         } catch (ParseException e) {
             throw new ResponseContextException(500, e);
         } catch (IOException e) {
             throw new ResponseContextException(500, e);
         }
         if (entry_doc == null) {
             return null;
         }
         return (Entry) entry_doc.getRoot();
     }
     
     @Override
     public void putMedia(Item item,
                          MimeType contentType, 
                          String slug, 
                          InputStream inputStream, 
                          RequestContext request)
         throws ResponseContextException {
         throw new ResponseContextException(405);
     }
 
 }
