 package com.amee.base.resource;
 
 import org.jdom.Document;
 import org.jdom.Element;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 /**
  * A {@link ResourceException} indicating that an expected request attribute is missing. Request attributes
  * typically consist of URL path parts, such as categoryIdentifier and itemIdentifier in this example
  * path definition: '/{categoryIdentifier}/items/{itemIdentifier}/values'.
  */
 public class MissingAttributeException extends ResourceException {
 
     private String attributeName;
 
     /**
      * Construct a MissingAttributeException with a specific attribute name.
      *
      * @param attributeName the name of the missing attribute
      */
     public MissingAttributeException(String attributeName) {
         super();
         this.setAttributeName(attributeName);
     }
 
     /**
      * Produces a {@link JSONObject} where the 'status' node contains 'MEDIA_TYPE_NOT_SUPPORTED'.
      *
      * @return the {@link JSONObject} response representation.
      */
     @Override
     public JSONObject getJSONObject() {
         try {
             JSONObject o = new JSONObject();
            o.put("status", "ERROR");
             o.put("error", "An attribute was missing: " + getAttributeName());
             return o;
         } catch (JSONException e) {
             throw new RuntimeException("Caught JSONException: " + e.getMessage(), e);
         }
     }
 
     /**
      * Produces a {@link Document} where the 'Status' node contains 'MEDIA_TYPE_NOT_SUPPORTED'.
      *
      * @return the {@link Document} response representation.
      */
     @Override
     public Document getDocument() {
         Element rootElem = new Element("Representation");
        rootElem.addContent(new Element("Status").setText("ERROR"));
         rootElem.addContent(new Element("Error").setText("An attribute was missing: " + getAttributeName()));
         return new Document(rootElem);
     }
 
     /**
      * Get the attribute name.
      *
      * @return the attribute name
      */
     public String getAttributeName() {
         return attributeName;
     }
 
     /**
      * Set the attribute name.
      *
      * @param attributeName the attribute name
      */
     public void setAttributeName(String attributeName) {
         this.attributeName = attributeName;
     }
 }
