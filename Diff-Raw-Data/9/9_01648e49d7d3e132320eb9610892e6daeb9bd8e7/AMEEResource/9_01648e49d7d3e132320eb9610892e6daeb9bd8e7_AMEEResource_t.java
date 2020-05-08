 package com.amee.restlet;
 
 import com.amee.domain.APIVersion;
 import com.amee.domain.auth.User;
 import com.amee.domain.data.DataCategory;
 import com.amee.domain.environment.Environment;
 import com.amee.domain.path.PathItem;
 import com.amee.restlet.profile.builder.v2.AtomFeed;
 import com.amee.service.ThreadBeanHolder;
 import com.amee.service.data.DataService;
 import com.amee.service.environment.EnvironmentService;
 import com.amee.service.profile.ProfileService;
 import org.apache.commons.lang.StringUtils;
 import org.apache.xerces.dom.DocumentImpl;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.restlet.Context;
 import org.restlet.data.*;
 import org.restlet.resource.*;
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.BeanFactory;
 import org.springframework.beans.factory.BeanFactoryAware;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 /**
  * This file is part of AMEE.
  * <p/>
  * AMEE is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  * <p/>
  * AMEE is free software and is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * <p/>
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * <p/>
  * Created by http://www.dgen.net.
  * Website http://www.amee.cc
  */
 public class AMEEResource extends BaseResource implements BeanFactoryAware {
 
     // TTL for all representations is (Now - ONE_DAY)
     private static final long ONE_DAY = 1000L * 60L * 60L * 24L;
 
     // Allowed values for the request parameter "representation".
     // The "representation" parameter specifies whether or not a representation is required in the response
     // to a POST or PUT and, if a representation is requested, the level of detail required.
     public static final String REPRESENTATION_NONE = "none";
     public static final String REPRESENTATION_FULL = "full";
     protected String representationRequested = REPRESENTATION_NONE;
 
     // Batch POST flag
     private boolean isBatchPost;
 
     @Autowired
     protected ProfileService profileService;
 
     @Autowired
     protected DataService dataService;
 
     @Autowired
     protected EnvironmentService environmentService;
 
     protected BeanFactory beanFactory;
     protected Environment environment;
     protected PathItem pathItem;
     protected DataCategory dataCategory;
 
     @Override
     public void initialise(Context context, Request request, Response response) {
         super.initialise(context, request, response);
         initVariants();
         environment = (Environment) request.getAttributes().get("environment");
         pathItem = (PathItem) request.getAttributes().get("pathItem");
     }
 
     private void initVariants() {
         List<Variant> variants = super.getVariants();
         if (isStandardWebBrowser()) {
             variants.add(new Variant(MediaType.TEXT_HTML));
         } else {
             variants.add(new Variant(MediaType.APPLICATION_XML));
             variants.add(new Variant(MediaType.APPLICATION_JSON));
             if (getAPIVersion().isNotVersionOne()) {
                 super.getVariants().add(0, new Variant(MediaType.APPLICATION_ATOM_XML));
             }
         }
     }
 
     // TODO: This is a modified duplication of the same method in BaseResource. Find a way to merge.
     @Override
     public Representation represent(Variant variant) throws ResourceException {
 
         if (log.isDebugEnabled()) {
             log.debug("represent() - method: " + getRequest().getMethod() + ", parameters: " + getForm().getMatrixString());
         }
 
         Representation representation;
         if (variant.getMediaType().equals(MediaType.TEXT_HTML)) {
             representation = getHtmlRepresentation();
         } else if (variant.getMediaType().equals(MediaType.APPLICATION_JSON)) {
             representation = getJsonRepresentation();
         } else if (variant.getMediaType().equals(MediaType.APPLICATION_XML)) {
             representation = getDomRepresentation();
         } else if (variant.getMediaType().equals(MediaType.APPLICATION_ATOM_XML)) {
             representation = getAtomRepresentation();
         } else {
             representation = super.represent(variant);
         }
 
         if (representation != null) {
             representation.setCharacterSet(CharacterSet.UTF_8);
             representation.setExpirationDate(new Date(Calendar.getInstance().getTimeInMillis() - ONE_DAY));
             representation.setModificationDate(Calendar.getInstance().getTime());
         }
         return representation;
     }
 
     protected Representation getDomRepresentation() throws ResourceException {
 
         // flag to ensure we only do the fetching work once
         final ThreadLocal<Boolean> fetched = new ThreadLocal<Boolean>();
 
         Representation representation;
         try {
 
             representation = new DomRepresentation(MediaType.APPLICATION_XML) {
 
                 public Document getDocument() throws IOException {
                     if ((fetched.get() == null) || !fetched.get()) {
                         Document doc = new DocumentImpl();
                         try {
                             fetched.set(true);
                             Element element = doc.createElement("Resources");
                             if (getAPIVersion().isNotVersionOne()) {
                                 element.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", "http://schemas.amee.cc/2.0");
                             }
                             element.appendChild(getElement(doc));
                             doc.appendChild(element);
                             setDocument(doc);
                         } catch (ResourceException ex) {
                             throw new IOException(ex);
                         }
                     }
                     return super.getDocument();
                 }
             };
 
         } catch (IOException e) {
             throw new ResourceException(e);
         }
         return representation;
     }
 
     // TODO: Needs to replace getJsonRepresentation in BaseResource or be merged.
     protected Representation getJsonRepresentation() throws ResourceException {
 
         // flag to ensure we only do the fetching work once
         final ThreadLocal<Boolean> fetched = new ThreadLocal<Boolean>();
 
         // No need to use JsonRepresentation directly as it doesn't addItemValue much beyond StringRepresentation
         return new StringRepresentation(null, MediaType.APPLICATION_JSON) {
 
             @Override
             public String getText() {
                 if ((fetched.get() == null) || !fetched.get()) {
                     try {
                         fetched.set(true);
                         JSONObject obj = getJSONObject();
                         if (!getAPIVersion().isVersionOne()) {
                             obj.put("apiVersion", getAPIVersion().toString());
                         }
                         setText(obj.toString());
                     } catch (JSONException e) {
                         // swallow
                         // TODO: replace this with a bespoke Exception implemention
                         log.error("Caught JSONException: " + e.getMessage());
                         throw new RuntimeException(e);
                     } catch (ResourceException e) {
                         // swallow
                         // TODO: replace this with a bespoke Exception implemention
                         log.error("Caught ResourceException: " + e.getMessage());
                         throw new RuntimeException(e);
                     }
                 }
                 return super.getText();
             }
         };
     }
 
     // TODO: Needs to replace getAtomRepresentation in BaseResource or be merged.
     protected Representation getAtomRepresentation() throws ResourceException {
         final org.apache.abdera.model.Element atomElement = getAtomElement();
         return new WriterRepresentation(MediaType.APPLICATION_ATOM_XML) {
             public void write(Writer writer) throws IOException {
                 AtomFeed.getInstance().getWriter().writeTo(atomElement, writer);
             }
         };
     }
 
     public PathItem getPathItem() {
         return pathItem;
     }
 
     protected void setDataCategory(String dataCategoryUid) {
         if (dataCategoryUid.isEmpty()) return;
         this.dataCategory = dataService.getDataCategoryByUid(dataCategoryUid);
     }
 
     public DataCategory getDataCategory() {
         return dataCategory;
     }
 
     public Environment getEnvironment() {
         return environment;
     }
 
     //TODO - Implementing here so that subclasses are not required to. Admin client templates will be phased out in time.
     public String getTemplatePath() {
         return null;
     }
 
     public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
         this.beanFactory = beanFactory;
     }
 
     @Override
     public Map<String, Object> getTemplateValues() {
         Map<String, Object> values = super.getTemplateValues();
         values.put("pathItem", pathItem);
         values.put("apiVersions", environmentService.getAPIVersions());
         return values;
     }
 
     public APIVersion getAPIVersion() {
         User user = (User) ThreadBeanHolder.get("user");
         return user.getAPIVersion();
     }
 
     /**
      * Produce the appropriate response for a successful PUT.
      *
      * @param uri - the URI of the updated resource.
      */
     public void successfulPut(String uri) {
         // For web browsers, continue with the same logic from AMEE 1.X.
         if (isStandardWebBrowser()) {
             getResponse().setStatus(Status.REDIRECTION_FOUND);
             getResponse().setLocationRef(uri);
         } else {
             // Return a representation when the following conditions apply:
             //  (i)   API V1.X (backwards compatibility)
             //  (ii)  if the client has specifically requested a representation
             if (getAPIVersion().isVersionOne() || isRepresentationRequested()) {
                 super.handleGet();
             } else {
                 // For PUTs in API versions >V1.X, return a 200 Accepted header.
                 getResponse().setStatus(Status.SUCCESS_OK);
             }
         }
     }
 
     /**
      * Produce the appropriate response for a successful DELETE.
      *
      * @param uri - the redirect URI. Only used if redirect is supported by the client.
      */
     public void successfulDelete(String uri) {
         if (isStandardWebBrowser()) {
             getResponse().setStatus(Status.REDIRECTION_FOUND);
             getResponse().setLocationRef(uri);
         } else {
             getResponse().setStatus(Status.SUCCESS_OK);
         }
     }
 
    /**
     * Produce the appropriate response for a successful batch POST
     */
    public void successfulBatchPost() {
        super.handleGet();
    }
 
     /**
      * Produce the appropriate response for a successful POST.
      *
      * @param parentUri - the URI of the parent resource
      * @param uid       - the uid of the created resource. This will be used to create the Location URI when a
      *                  representation has not been requested by the client.
      */
     public void successfulPost(String parentUri, String uid) {
         // For web browsers, continue with the same logic from AMEE 1.X.
         if (isStandardWebBrowser()) {
             getResponse().setStatus(Status.REDIRECTION_FOUND);
             getResponse().setLocationRef(parentUri);
         } else {
             // Return a representation when the following conditions apply:
             //  (i) API V1.X (backwards compatibility)
             //  (ii) if the client has specifically requested a representation
             //  (iii) if the request is a batch POST
            if (getAPIVersion().isVersionOne() || isRepresentationRequested()) {
                 super.handleGet();
             } else {
                 // For single POSTs in API versions >V1.X set the Location and 201 Created header.
                 getResponse().setLocationRef(parentUri + "/" + uid);
                 getResponse().setStatus(Status.SUCCESS_CREATED);
             }
         }
     }
 
     /**
      * 
      * @return true if the request specifies that a representation should be returned following a POST or PUT request
      */
     public boolean isRepresentationRequested() {
        return isFullRepresentationRequested();
     }
 
     public void setRepresentationRequested(String representationRequested) {
         this.representationRequested = representationRequested;
     }
 
     /**
      *
      * @return true if the request specifies that the full representation should be returned following a POST or PUT request
      */
     public boolean isFullRepresentationRequested() {
         return StringUtils.equals(representationRequested,REPRESENTATION_FULL);
     }
 
     public boolean isBatchPost() {
         return isBatchPost;
     }
 
     public void setIsBatchPost(boolean isBatchPost) {
         this.isBatchPost = isBatchPost;
     }
 
 }
