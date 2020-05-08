 /*
  * (C) Copyright ${year} Nuxeo SA (http://nuxeo.com/) and contributors.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser General Public License
  * (LGPL) version 2.1 which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/lgpl.html
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * Contributors:
  *     <a href="mailto:tdelprat@nuxeo.com">Tiry</a>
  */
 
 package org.nuxeo.ui.select2;
 
 import java.io.BufferedOutputStream;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.faces.context.FacesContext;
 import javax.servlet.http.HttpServletRequest;
 
 import net.sf.json.JSONObject;
 
 import org.apache.commons.io.output.ByteArrayOutputStream;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.codehaus.jackson.JsonGenerator;
 import org.jboss.seam.ScopeType;
 import org.jboss.seam.annotations.In;
 import org.jboss.seam.annotations.Name;
 import org.jboss.seam.annotations.Scope;
 import org.nuxeo.ecm.automation.AutomationService;
 import org.nuxeo.ecm.automation.OperationContext;
 import org.nuxeo.ecm.automation.server.jaxrs.io.JsonWriter;
 import org.nuxeo.ecm.automation.server.jaxrs.io.writers.JsonDocumentWriter;
 import org.nuxeo.ecm.core.api.ClientException;
 import org.nuxeo.ecm.core.api.CoreSession;
 import org.nuxeo.ecm.core.api.DocumentModel;
 import org.nuxeo.ecm.core.api.DocumentModelList;
 import org.nuxeo.ecm.core.api.DocumentRef;
 import org.nuxeo.ecm.core.api.IdRef;
 import org.nuxeo.ecm.core.api.PathRef;
 import org.nuxeo.ecm.core.schema.SchemaManager;
 import org.nuxeo.ecm.core.schema.types.Field;
 import org.nuxeo.ecm.core.schema.types.QName;
 import org.nuxeo.ecm.core.schema.types.Schema;
 import org.nuxeo.ecm.directory.Directory;
 import org.nuxeo.ecm.directory.Session;
 import org.nuxeo.ecm.directory.api.DirectoryService;
 import org.nuxeo.ecm.platform.forms.layout.api.Widget;
 import org.nuxeo.runtime.api.Framework;
 
 @Name("select2Actions")
 @Scope(ScopeType.EVENT)
 public class Select2ActionsBean implements Serializable {
 
     private static final long serialVersionUID = 1L;
 
     @In(create = true)
    protected Map<String, String> messages;
 
     @SuppressWarnings("unused")
     private static final Log log = LogFactory.getLog(Select2ActionsBean.class);
 
     protected static final String SELECT2_RESOURCES_MARKER = "SELECT2_RESOURCES_MARKER";
 
     @In(create = true, required = false)
     protected transient CoreSession documentManager;
 
     public boolean isMultiSelection(Widget widget) {
         if (widget.getProperty("multiple") != null
                 && widget.getProperty("multiple").toString().equalsIgnoreCase(
                         "true")) {
             return true;
         }
         return false;
     }
 
     public boolean mustIncludeResources() {
         FacesContext facesContext = FacesContext.getCurrentInstance();
         if (facesContext != null) {
             HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
 
             if (request.getAttribute(SELECT2_RESOURCES_MARKER) != null) {
                 return false;
             } else {
                 request.setAttribute(SELECT2_RESOURCES_MARKER, "done");
                 return true;
             }
         }
         return false;
     }
 
     protected DocumentModel resolveReference(String storedReference,
             String operationName, String idProperty) throws Exception {
 
         if (storedReference == null || storedReference.isEmpty()) {
             return null;
         }
         DocumentModel doc = null;
 
         if (operationName == null || operationName.isEmpty()) {
             DocumentRef ref = null;
 
             if (idProperty!=null && ! idProperty.isEmpty()) {
                 String query =" select * from Document where " + idProperty + "='" + storedReference + "'";
                 DocumentModelList docs = documentManager.query(query);
                 if (docs.size()>0) {
                     return docs.get(0);
                 } else {
                     log.warn("Unable to resolve doc using property " + idProperty + " and value " + storedReference);
                     return null;
                 }
             }
             else {
                 if (storedReference.startsWith("/")) {
                     ref = new PathRef(storedReference);
                 } else {
                     ref = new IdRef(storedReference);
                 }
                 if (documentManager.exists(ref)) {
                     doc = documentManager.getDocument(ref);
                 }
             }
         } else {
             AutomationService as = Framework.getLocalService(AutomationService.class);
             OperationContext ctx = new OperationContext(documentManager);
 
             ctx.put("value", storedReference);
             ctx.put("xpath", idProperty);
 
             /*
             OperationType targetType = null;
             for (OperationType opType : as.getOperations()) {
                 if (operationName.equals(opType.getId())) {
                     targetType = opType;
                     break;
                 }
             }
 
             if (targetType!=null) {
                 //targetType.getDocumentation().
                 // run an operation : set as input
                 ctx.setInput(storedReference);
             }*/
 
             Object result = as.run(ctx, operationName, null);
 
             if (result==null) {
                 doc = null;
             } else if (result instanceof DocumentModel) {
                 doc = (DocumentModel) result;
             } else if (result instanceof DocumentModelList) {
                 DocumentModelList docs= (DocumentModelList) result;
                 if (docs.size()>0) {
                     doc = docs.get(0);
                 }
             }
         }
         return doc;
     }
 
     public String resolveSingleReference(String storedReference,
             String operationName, String idProperty, String schemaNames) throws Exception {
 
         DocumentModel doc = resolveReference(storedReference, operationName, idProperty);
         if (doc == null) {
             return "";
         }
         String[] schemas = null;
         if (schemaNames != null && !schemaNames.isEmpty()) {
             schemas = schemaNames.split(",");
         }
 
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         BufferedOutputStream out = new BufferedOutputStream(baos);
         JsonDocumentWriter.writeDocument(out, doc, schemas);
         out.flush();
         return new String(baos.toByteArray(), "UTF-8");
     }
 
     public String resolveSingleDirectoryEntry(String storedReference,
             String directoryName, boolean translateLabels) throws Exception {
 
         if (storedReference==null || storedReference.isEmpty()) {
             return "";
         }
 
         DirectoryService directoryService = Framework.getLocalService(DirectoryService.class);
         Directory directory = directoryService.getDirectory(directoryName);
         String schemaName = directory.getSchema();
         SchemaManager schemaManager = Framework.getLocalService(SchemaManager.class);
         Schema schema = schemaManager.getSchema(schemaName);
 
         Session session = null;
         try {
             session = directory.getSession();
             DocumentModel entry = session.getEntry(storedReference);
 
             JSONObject obj = new JSONObject();
             for (Field field : schema.getFields()) {
                 QName fieldName = field.getName();
                 String key = fieldName.getLocalName();
                 Serializable value = entry.getPropertyValue(fieldName.getPrefixedName());
                 if (translateLabels && "label".equals(key)) {
                    value = messages.get((String) value);
                 }
                 obj.element(key, value);
             }
             return obj.toString();
         } catch (Exception e) {
             // TODO: handle exception
             return "";
         } finally {
             try {
                 if (session != null) {
                     session.close();
                 }
             } catch (ClientException ce) {
                 log.error("Could not close directory session", ce);
             }
         }
     }
 
     @SuppressWarnings("rawtypes")
     public String resolveMultipleReferences(Object value, String operationName,
             String idProperty, String schemaNames) throws Exception {
 
         if (value == null) {
             return "[]";
         }
 
         List<String> storedRefs = new ArrayList<>();
         if (value instanceof List) {
             for (Object v : (List) value) {
                 storedRefs.add(v.toString());
             }
         } else if (value instanceof Object[]) {
             for (Object v : (Object[]) value) {
                 storedRefs.add(v.toString());
             }
         }
 
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         BufferedOutputStream out = new BufferedOutputStream(baos);
         JsonGenerator jg = JsonWriter.createGenerator(out);
         String[] schemas = null;
         if (schemaNames != null && !schemaNames.isEmpty()) {
             schemas = schemaNames.split(",");
         }
         jg.writeStartArray();
 
         for (String ref : storedRefs) {
             DocumentModel doc = resolveReference(ref, operationName, idProperty);
             if (doc == null) {
                 return "";
             }
             JsonDocumentWriter.writeDocument(jg, doc, schemas);
         }
 
         jg.writeEndArray();
         out.flush();
         String json = new String(baos.toByteArray(), "UTF-8");
 
         if (!json.endsWith("]")) { // XXX !!!
             json = json + "]";
         }
 
         return json;
     }
 
     public String resolveSingleReferenceLabel(String storedReference,
             String operationName,String idProperty, String label) throws Exception {
 
         DocumentModel doc = resolveReference(storedReference, operationName, idProperty);
         if (doc == null) {
             return "";
         }
 
         if (label != null && !label.isEmpty()) {
             Object val = doc.getPropertyValue(label);
             if (val == null) {
                 return "";
             } else {
                 return val.toString();
             }
         }
         return doc.getTitle();
     }
 
     @SuppressWarnings("rawtypes")
     public List<String> resolveMultipleReferenceLabels(Object value,
             String operationName, String idProperty, String label) throws Exception {
 
         List<String> result = new ArrayList<>();
 
         if (value == null) {
             return result;
         }
 
         List<String> storedRefs = new ArrayList<>();
         if (value instanceof List) {
             for (Object v : (List) value) {
                 storedRefs.add(v.toString());
             }
         } else if (value instanceof Object[]) {
             for (Object v : (Object[]) value) {
                 storedRefs.add(v.toString());
             }
         }
 
         for (String ref : storedRefs) {
             DocumentModel doc = resolveReference(ref, operationName, idProperty);
             if (doc != null) {
                 if (label != null && !label.isEmpty()) {
                     Object val = doc.getPropertyValue(label);
                     if (val == null) {
                         result.add("");
                     } else {
                         result.add(val.toString());
                     }
                 } else {
                     result.add(doc.getTitle());
                 }
             }
         }
         return result;
     }
 
     public String encodeParameters(Widget widget) throws Exception {
 
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         BufferedOutputStream out = new BufferedOutputStream(baos);
 
         JsonGenerator jg = JsonWriter.createGenerator(out);
         jg.writeStartObject();
 
         for (Entry<String, Serializable> entry : widget.getProperties().entrySet()) {
 
             jg.writeStringField(entry.getKey(), entry.getValue().toString());
         }
         jg.writeEndObject();
         jg.flush();
         out.flush();
         return new String(baos.toByteArray(), "UTF-8");
     }
 
 }
