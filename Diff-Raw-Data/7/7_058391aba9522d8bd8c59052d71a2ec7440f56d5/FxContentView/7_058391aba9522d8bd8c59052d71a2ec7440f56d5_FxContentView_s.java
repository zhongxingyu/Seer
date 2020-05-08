 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2014
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.faces.components.content;
 
 import com.flexive.faces.FxJsfComponentUtils;
 import com.flexive.shared.CacheAdmin;
 import com.flexive.shared.EJBLookup;
 import com.flexive.shared.content.FxContent;
 import com.flexive.shared.content.FxData;
 import com.flexive.shared.content.FxGroupData;
 import com.flexive.shared.content.FxPK;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.exceptions.FxInvalidParameterException;
 import com.flexive.shared.exceptions.FxNotFoundException;
 import com.flexive.shared.exceptions.FxRuntimeException;
 import com.flexive.shared.interfaces.ContentEngine;
 import com.flexive.shared.structure.*;
 import com.flexive.shared.value.FxReference;
 import com.flexive.shared.value.FxString;
 import com.flexive.shared.value.FxValue;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import javax.faces.FacesException;
 import javax.faces.component.ContextCallback;
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIOutput;
 import javax.faces.context.FacesContext;
 import javax.faces.event.AbortProcessingException;
 import javax.faces.event.FacesEvent;
 import javax.faces.event.FacesListener;
 import javax.faces.event.PhaseId;
 import java.io.IOException;
 import java.util.*;
 
 import static com.flexive.faces.FxJsfComponentUtils.requireAttribute;
 
 /**
  * <p/>
  * Provides an editable view of an FxContent instance. A hashmap for accessing
  * the properties is provided by the variable <code>#{var}</code>, the content instance
  * itself can be obtained using <code>#{var}_content</code>.
  * </p>
  * <p/>
  * For example:
  * <pre>
  * &lt;fx:content pk="#{myBean.articlePk}" var="article">
  *    Title: &lt;fx:value property="title"/>
  *    &lt;h:commandLink action="#{myBean.save}">
  *      &lt;f:setPropertyActionListener target="#{myBean.content}" value="#{article_content}"/>
  *      Save article
  *    &lt;/h:commandLink>
  * &lt;/fx:content>
  * </pre>
  * </p>
  *
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  */
 public class FxContentView extends UIOutput {
     public static final String COMPONENT_TYPE = "flexive.FxContentView";
     
     private static final Log LOG = LogFactory.getLog(FxContentView.class);
 
     private FxPK pk;
     private long type = -1;
     private String typeName;
     private String var;
     private FxContent content;
     private Map<ContentKey, FxContent> contentMap = new HashMap<ContentKey, FxContent>();
     private Boolean preserveContent;
     private boolean explode = true;
 
     public FxContentView() {
         setRendererType(null);
     }
 
     public static String getExpression(String var, String xpath, String suffix) {
         return "#{" + var + "['" + xpath + (StringUtils.isNotBlank(suffix) ? "$" + suffix : "") + "']}";
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void encodeBegin(FacesContext context) throws IOException {
         provideContent(context);
     }
 
     public void provideContent(FacesContext context) {
         final Map requestMap = context.getExternalContext().getRequestMap();
         try {                                    
             final FxPK pk = (FxPK) getPk();
             getContent();
             if (content == null || (pk != null && !content.matchesPk(pk))) {
                 final ContentEngine contentInterface = EJBLookup.getContentEngine();
                 final ContentKey contentKey = new ContentKey(pk, getClientId(context));
                 if (pk == null || pk.isNew()) {
                     setContent(contentInterface.initialize(getSelectedType().getId()));
                 } else {
                     if (contentMap.containsKey(contentKey)) {
                         setContent(contentMap.get(contentKey));
                     } else {
                         setContent(contentInterface.load(pk));
                         if (isExplode()) {
                             content.getRootGroup().explode(true);
                         }
                     }
                 }
                 contentMap.put(contentKey, content);
             }
             //noinspection unchecked
             requestMap.put(getVar(), new ContentMap(content));
             //noinspection unchecked
             requestMap.put(getVar() + "_content", content);
         } catch (FxApplicationException e) {
             requestMap.remove(getVar());
             LOG.error("Failed to provide content: " + e.getMessage(), e);
             throw e.asRuntimeException();
         }
     }
 
     /**
      * Helper method to force reloading in very specific situations when the heuristic in
      * {@link #provideContent(javax.faces.context.FacesContext)} does not detect content changes
      * (for example: maximum version changed its edit/live status). Clears all cached contents
      * in this view.
      *
      * @since 3.1
      */
     public static void clearCachedContents() {
         for (UIComponent component : FacesContext.getCurrentInstance().getViewRoot().getChildren()) {
             clearCachedContents(component);
         }
     }
 
     private static void clearCachedContents(UIComponent comp) {
         if (comp instanceof FxContentView) {
             final FxContentView fcv = (FxContentView) comp;
             fcv.content = null;
             fcv.contentMap.clear();
         }
 
         if (comp.getChildCount() > 0) {
             for (UIComponent child : comp.getChildren()) {
                 clearCachedContents(child);
             }
         }
     }
 
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void encodeEnd(FacesContext context) throws IOException {
         removeContent(context, true);
     }
 
     protected void removeContent(FacesContext context) {
         removeContent(context, false);
     }
 
     protected void removeContent(FacesContext context, boolean cleanup) {
         //context.getELContext().getVariableMapper().setVariable(getVar(), null);
         context.getExternalContext().getRequestMap().remove(getVar());
         if (cleanup) {
             setContent(null);
             contentMap.clear();
         }
 //        context.getExternalContext().getRequestMap().remove(getVar() + "_content");
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void processDecodes(FacesContext context) {
         provideContent(context);
         try {
             super.processDecodes(context);
         } finally {
             removeContent(context);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void processValidators(FacesContext context) {
         provideContent(context);
         try {
             super.processValidators(context);
         } finally {
             removeContent(context);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void processUpdates(FacesContext context) {
         provideContent(context);
         try {
             super.processUpdates(context);
         } finally {
             removeContent(context);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void broadcast(FacesEvent event) throws AbortProcessingException {
         if (event instanceof WrappedEvent) {
             final FacesContext ctx = FacesContext.getCurrentInstance();
             try {
                 provideContent(ctx);
                 performBroadcast(ctx, ((WrappedEvent) event).event);
             } finally {
                 removeContent(ctx, true);
             }
         } else {
             super.broadcast(event);
         }
     }
 
     protected void performBroadcast(FacesContext ctx, FacesEvent event) {
         event.getComponent().broadcast(event);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void queueEvent(FacesEvent event) {
         super.queueEvent(new WrappedEvent(this, event));
     }
 
     @Override
     public boolean invokeOnComponent(FacesContext context, String clientId, ContextCallback callback) throws FacesException {
         provideContent(context);
         try {
             return super.invokeOnComponent(context, clientId, callback);
         } finally {
             removeContent(context);
         }
     }
 
     public Object getPk() {
         final Object pkValue = FxJsfComponentUtils.getValue(this, "pk");
         if (pkValue != null) {
             return FxPK.fromObject(pkValue);
         }
         return pk;
     }
 
     public void setPk(Object pk) {
         this.pk = pk != null ? FxPK.fromObject(pk) : null;
     }
 
     public String getVar() {
         if (var == null) {
             var = FxJsfComponentUtils.getStringValue(this, "var");
         }
         requireAttribute("fx:contentView", "var", var);
         return var;
     }
 
     public void setVar(String var) {
         this.var = var;
     }
 
     public long getType() {
         if (type == -1) {
             type = FxJsfComponentUtils.getIntegerValue(this, "type", -1);
         }
         return type;
     }
 
     public void setType(long type) {
         this.type = type;
     }
 
     public boolean isExplode() {
         final Boolean value = FxJsfComponentUtils.getBooleanValue(this, "explode");
         if (value != null) {
             this.explode = value;
         }
         return explode;
     }
 
     public void setExplode(boolean explode) {
         this.explode = explode;
     }
 
     public String getTypeName() {
         if (typeName == null) {
             typeName = FxJsfComponentUtils.getStringValue(this, "typeName");
         }
         return typeName;
     }
 
     public void setTypeName(String typeName) {
         this.typeName = typeName;
     }
 
     private FxType getSelectedType() {
         if (getType() != -1) {
             return CacheAdmin.getEnvironment().getType(getType());
         } else if (StringUtils.isNotBlank(getTypeName())) {
             return CacheAdmin.getEnvironment().getType(getTypeName());
         } else {
             throw new FxNotFoundException("ex.jsf.contentView.type.required").asRuntimeException();
         }
     }
 
     public FxContent getContent() {
         if (content == null) {
             setContent((FxContent) FxJsfComponentUtils.getValue(this, "content"));
             if (content != null) {
                 // we need to make sure the content instance still exists at
                 // the next request, so we store it in the component
                 if (preserveContent == null) {
                     preserveContent = true;
                 }
                 if (isExplode()) {
                     // initialize empty fields
                     content.getRootGroup().explode(true);
                 }
             }
         }
         return content;
     }
 
     public void setContent(FxContent content) {
         this.content = content;
     }
 
     public boolean isPreserveContent() {
         final Boolean value = FxJsfComponentUtils.getBooleanValue(this, "preserveContent");
         return value != null ? value : preserveContent != null && preserveContent;
     }
 
     public void setPreserveContent(boolean preserveContent) {
         this.preserveContent = preserveContent;
     }
 
     @Override
     public Object saveState(FacesContext facesContext) {
         Object[] state = new Object[8];
         state[0] = super.saveState(facesContext);
         state[1] = pk;  // note: don't call getPk() here - rely on the caller to provide a valid pk next time
         state[2] = var;
         state[3] = getType();
         state[4] = typeName;
         state[5] = preserveContent;
         state[6] = isPreserveContent() ? content : null;
         state[7] = explode;
         return state;
     }
 
     @Override
     public void restoreState(FacesContext facesContext, Object o) {
         Object[] state = (Object[]) o;
         super.restoreState(facesContext, state[0]);
         pk = (FxPK) state[1];
         var = (String) state[2];
         type = (Long) state[3];
         typeName = (String) state[4];
         preserveContent = (Boolean) state[5];
         content = (FxContent) state[6];
         explode = (Boolean) state[7];
     }
 
     public class ContentMap extends HashMap<String, Object> {
         private static final long serialVersionUID = -6423903577633591618L;
         private final String prefix;
         private final FxContent content;
         private Map<String, Integer> newIndices;    // caches new group/property indices for an XPath
 
         public ContentMap(FxContent content) {
             this(content, "");
         }
 
         public ContentMap(FxContent content, String prefix) {
             this.content = content;
             this.prefix = prefix;
         }
 
         @Override
         public Object put(String key, Object value) {
             final Object oldValue = get(key);
             content.setValue(((FxValue) value).getXPath(), (FxValue) value);
             return oldValue;
         }
 
         @Override
         public Object get(Object key) {
             try {
                 String path = getCanonicalPath((String) key);
                 final int suffixPos = path.indexOf('$');
                 if (suffixPos != -1) {
                     final String xpath = path.substring(0, suffixPos);
                     if (isListRequest(path)) {
                         return getList(xpath, false);
                     } else if (isListAllRequest(path)) {
                         return getList(xpath, true);
                     } else if (isLabelRequest(path)) {
                         return getLabel(xpath);
                     } else if (isHintRequest(path)) {
                         return getHint(xpath);
                     } else if (isNewValueRequest(path)) {
                         return getNewValue(xpath);
                     } else if (isResolveReferenceRequest(path)) {
                         return getResolvedReference(xpath);
                     } else if (isXPathRequest(path)) {
                         return getXPath(xpath);
                     } else if (isMayCreateMoreRequest(path)) {
                         return getMayCreateMore(xpath);
                     } else if (isXPathValidRequest(path)) {
                         return getXPathValid(xpath);
                     } else if (isDataRequest(path)) {
                         return getData();
                     } else if (isMaxIndexRequest(path)) {
                         return getMaxIndex(xpath);
                     }
                 }
                 // check the assignment type in the static structure data
                 final FxEnvironment env = CacheAdmin.getEnvironment();
                 final FxAssignment assignment = env.getAssignment(env.getType(content.getTypeId()).getName() + path);
                 if (assignment instanceof FxGroupAssignment) {
                     return new ContentMap(content, path);
                 } else {
                     return content.getValue(path);
                 }
             } catch (FxNotFoundException e) {
                 return null;
             } catch (FxRuntimeException e) {
                 if (e.getCause() instanceof FxNotFoundException) {
                     return null;
                 }
                 return e;
             } catch (FxInvalidParameterException e) {
                 return new FxString("?" + key + "?");
             }
         }
 
         private String getCanonicalPath(String path) {
            if (StringUtils.isBlank(prefix) && path.indexOf('/') != -1 && path.charAt(0) != '/') {
                 // strip type/PK information if this is an absolute XPath and we're at the root level
                return path.substring(path.indexOf('/'));
             }
             return prefix + (path.startsWith("/") ? path : '/' + path);
         }
 
         private boolean isLabelRequest(String path) {
             return path.endsWith("$label");
         }
 
         private boolean isHintRequest(String path) {
             return path.endsWith("$hint");
         }
         
         private boolean isListRequest(String path) {
             return path.endsWith("$list");
         }
 
         private boolean isListAllRequest(String path) {
             return path.endsWith("$listAll");
         }
 
         private boolean isNewValueRequest(String path) {
             return path.endsWith("$new");
         }
 
         private boolean isResolveReferenceRequest(String path) {
             return path.endsWith("$");
             //return path.indexOf(".@") > 0 && path.indexOf(".@") == path.lastIndexOf(".");
         }
 
         private boolean isXPathRequest(String path) {
             return path.endsWith("$xpath");
         }
 
         private boolean isMayCreateMoreRequest(String path) {
             return path.endsWith("$mayCreateMore");
         }
 
         private boolean isXPathValidRequest(String path) {
             return path.endsWith("$valid");
         }
 
         private boolean isDataRequest(String path) {
             return path.equals(prefix + "/$data");
         }
 
         private boolean isMaxIndexRequest(String path) {
             return path.endsWith("$maxIndex");
         }
 
         private FxString getLabel(String path) throws FxNotFoundException, FxInvalidParameterException {
             return getAssignment(path).getDisplayLabel();
         }
 
         private FxAssignment getAssignment(String xPath) {
             return CacheAdmin.getEnvironment().getAssignment(
                     content.getPropertyData(xPath).getAssignmentId()
             );
         }
 
         private FxString getHint(String path) throws FxNotFoundException, FxInvalidParameterException {
             return getAssignment(path).getHint();
         }
 
         private List<?> getList(String path, boolean includeEmpty) {
             try {
                 final FxEnvironment env = CacheAdmin.getEnvironment();
                 // use faster direct assignment lookup in the environment
                 final String typeName = env.getType(content.getTypeId()).getName();
                 final FxAssignment assignment = env.getAssignment(typeName + path);
                 if (!content.containsXPath(path)) {
                     return Collections.emptyList();
                 } else if (assignment instanceof FxPropertyAssignment) {
                     // iterate over property values
                     return content.getPropertyData(path).getValues(includeEmpty);
                 } else {
                     // create a content map for each child
                     final List<ContentMap> rowMaps = new ArrayList<ContentMap>();
                     final List<FxData> elements = content.getGroupData(path).getElements();
                     for (FxData row : elements) {
                         if (includeEmpty || !row.isEmpty()) {
                             rowMaps.add(new ContentMap(content, path + "[" + row.getIndex() + "]"));
                         }
                     }
                     return rowMaps;
                 }
             } catch (FxRuntimeException e) {
                 if (e.getConverted() instanceof FxNotFoundException) {
                     // return null object
                     return Collections.emptyList();
                 } else {
                     throw e;
                 }
             }
         }
 
         private FxValue getNewValue(String path) {
             try {
                 /*final FxPropertyData data = content.getPropertyData(path);
                 if (data.getAssignmentMultiplicity().getMax() > 1 && data.getCreateableElements() > 0) {
                     return ((FxPropertyData) data.createNew(FxData.POSITION_BOTTOM)).getValue();
                 } */
                 // TODO use content API when available
                 final String[] groups = StringUtils.split(path, '/');
                 if (groups.length == 0) {
                     throw new FxInvalidParameterException("PATH", "No new values may be created for " + path).asRuntimeException();
                 }
                 final StringBuilder newPath = new StringBuilder();
                 final StringBuilder xPathSoFar = new StringBuilder();
                 for (int i = 0; i < groups.length - 1; i++) {
                     final String group = groups[i];
                     xPathSoFar.append("/").append(group);
                     final FxGroupData data = content.getGroupData(xPathSoFar.toString());
                     final List<FxData> elements = data.getElements();
                     final int index;
                     if (getNewIndices().containsKey(xPathSoFar.toString())) {
                         index = getNewIndices().get(xPathSoFar.toString());
                     } else if (!elements.isEmpty()) {
                         final FxData lastGroup = elements.get(elements.size() - 1);
                         // re-use last empty group
                         index = lastGroup.isEmpty() ? elements.size() - 1 : elements.size();
                     } else {
                         index = elements.size();
                     }
                     if (index == elements.size() && !data.mayCreateMore()) {
                         throw new FxInvalidParameterException("PATH", "ex.jsf.contentView.create.multiplicity",
                                 xPathSoFar.toString()).asRuntimeException();
                     }
                     getNewIndices().put(xPathSoFar.toString(), index);
                     newPath.append("/").append(group).append('[').append(index + 1).append("]");
                 }
                 final FxType type = CacheAdmin.getEnvironment().getType(content.getTypeId());
                 // add property
                 newPath.append("/").append(groups[groups.length - 1]);
                 FxValue ret = content.getValue(newPath.toString());
                 if (ret != null)
                     return ret;
                 // xpath not set yet
                 final FxValue value = ((FxPropertyAssignment) type.getAssignment(path)).getEmptyValue();
                 content.setValue(newPath.toString(), value);
                 return content.getValue(newPath.toString());
             } catch (FxRuntimeException e) {
                 if (e.getConverted() instanceof FxNotFoundException) {
                     return new FxString("");
                 } else {
                     throw e;
                 }
             } 
         }
 
         private ContentMap getResolvedReference(String path) {
             // resolve referenced object
             try {
                 final FxPK pk = ((FxReference) content.getValue(path)).getDefaultTranslation();
                 final ContentKey contentKey = new ContentKey(pk, getClientId(FacesContext.getCurrentInstance()));
                 if (contentMap.containsKey(contentKey)) {
                     return new ContentMap(contentMap.get(contentKey));
                 } else if (pk.isNew()) {
                     return null;    // don't resolve empty references
                 }
                 final FxContent referencedContent = EJBLookup.getContentEngine().load(pk);
                 contentMap.put(contentKey, referencedContent);
                 return new ContentMap(referencedContent);
             } catch (FxApplicationException e) {
                 throw e.asRuntimeException();
             }
         }
 
         private String getXPath(String path) {
             return path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
         }
 
         private boolean getMayCreateMore(String path) {
             try {
                 getNewValue(path);
                 return true;
             } catch (FxRuntimeException e) {
                 return false;
             }
         }
 
         private FxGroupData getData() {
             return content.getGroupData(prefix);
         }
 
         private boolean getXPathValid(String path) {
             return content.isPropertyXPath(path);
         }
 
         private int getMaxIndex(String path) {
             return content.getMaxIndex(path);
         }
 
         public Map<String, Integer> getNewIndices() {
             if (newIndices == null) {
                 newIndices = new HashMap<String, Integer>();
             }
             return newIndices;
         }
     }
 
     protected static class WrappedEvent extends FacesEvent {
         private static final long serialVersionUID = 3341414461609445709L;
         
         protected final FacesEvent event;
 
         public WrappedEvent(FxContentView component, FacesEvent event) {
             super(component);
             this.event = event;
         }
 
         @Override
         public PhaseId getPhaseId() {
             return event.getPhaseId();
         }
 
         @Override
         public void setPhaseId(PhaseId phaseId) {
             this.event.setPhaseId(phaseId);
         }
 
         @Override
         public boolean isAppropriateListener(FacesListener listener) {
             return false;
         }
 
         @Override
         public void processListener(FacesListener listener) {
             throw new IllegalStateException();
         }
 
         @Override
         public void queue() {
             event.queue();
         }
     }
 
     private static class ContentKey {
         private final FxPK pk;
         private final String clientId;
 
         private ContentKey(FxPK pk, String clientId) {
             this.pk = pk;
             this.clientId = clientId;
         }
 
         public FxPK getPk() {
             return pk;
         }
 
         public String getClientId() {
             return clientId;
         }
 
         @Override
         public boolean equals(Object o) {
             if (this == o) return true;
             if (o == null || getClass() != o.getClass()) return false;
 
             ContentKey that = (ContentKey) o;
 
             if (clientId != null ? !clientId.equals(that.clientId) : that.clientId != null) return false;
             if (pk != null ? !pk.equals(that.pk) : that.pk != null) return false;
 
             return true;
         }
 
         @Override
         public int hashCode() {
             int result = pk != null ? pk.hashCode() : 0;
             result = 31 * result + (clientId != null ? clientId.hashCode() : 0);
             return result;
         }
     }
 
 
 }
