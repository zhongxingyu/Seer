 /**
  * Licensed under the Common Development and Distribution License,
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.sun.com/cddl/
  *   
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
  * implied. See the License for the specific language governing
  * permissions and limitations under the License.
  */
 
 package com.sun.facelets.impl;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 
 import javax.el.ELContext;
 import javax.el.ELException;
 import javax.el.ELResolver;
 import javax.el.ExpressionFactory;
 import javax.el.FunctionMapper;
 import javax.el.ValueExpression;
 import javax.el.VariableMapper;
 import javax.faces.FacesException;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 
 import com.sun.facelets.Facelet;
 import com.sun.facelets.FaceletContext;
 import com.sun.facelets.FaceletException;
 import com.sun.facelets.FaceletHandler;
 import com.sun.facelets.TemplateClient;
 import com.sun.facelets.el.DefaultVariableMapper;
 import com.sun.facelets.el.ELAdaptor;
 
 /**
  * Default FaceletContext implementation.
  * 
  * A single FaceletContext is used for all Facelets involved in an invocation of
  * {@link com.sun.facelets.Facelet#apply(FacesContext, UIComponent) Facelet#apply(FacesContext, UIComponent)}.
  * This means that included Facelets are treated the same as the JSP include
  * directive.
  * 
  * @author Jacob Hookom
  * @version $Id: DefaultFaceletContext.java,v 1.4.4.3 2006/03/25 01:01:53 jhook
  *          Exp $
  */
 final class DefaultFaceletContext extends FaceletContext {
 
     private final FacesContext faces;
 
     private final ELContext ctx;
 
     private final DefaultFacelet facelet;
 
     private VariableMapper varMapper;
 
     private FunctionMapper fnMapper;
 
 	private final Map ids;
 
     public DefaultFaceletContext(DefaultFaceletContext ctx,
             DefaultFacelet facelet) {
         this.ctx = ctx.ctx;
         this.facelet = facelet;
         this.clients = ctx.clients;
         this.faces = ctx.faces;
         this.fnMapper = ctx.fnMapper;
         this.ids = ctx.ids;
         this.varMapper = ctx.varMapper;
     }
 
     public DefaultFaceletContext(FacesContext faces, DefaultFacelet facelet) {
         this.ctx = ELAdaptor.getELContext(faces);
         this.ids = new HashMap();
         this.clients = new ArrayList(5);
         this.faces = faces;
         this.facelet = facelet;
         this.varMapper = this.ctx.getVariableMapper();
         if (this.varMapper == null) {
             this.varMapper = new DefaultVariableMapper();
         }
         this.fnMapper = this.ctx.getFunctionMapper();
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.facelets.FaceletContext#getFacesContext()
      */
     public FacesContext getFacesContext() {
         return this.faces;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.facelets.FaceletContext#getExpressionFactory()
      */
     public ExpressionFactory getExpressionFactory() {
         return this.facelet.getExpressionFactory();
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.facelets.FaceletContext#setVariableMapper(javax.el.VariableMapper)
      */
     public void setVariableMapper(VariableMapper varMapper) {
         // Assert.param("varMapper", varMapper);
         this.varMapper = varMapper;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.facelets.FaceletContext#setFunctionMapper(javax.el.FunctionMapper)
      */
     public void setFunctionMapper(FunctionMapper fnMapper) {
         // Assert.param("fnMapper", fnMapper);
         this.fnMapper = fnMapper;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.facelets.FaceletContext#includeFacelet(javax.faces.component.UIComponent,
      *      java.lang.String)
      */
     public void includeFacelet(UIComponent parent, String relativePath)
             throws IOException, FaceletException, FacesException, ELException {
         this.facelet.include(this, parent, relativePath);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see javax.el.ELContext#getFunctionMapper()
      */
     public FunctionMapper getFunctionMapper() {
         return this.fnMapper;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see javax.el.ELContext#getVariableMapper()
      */
     public VariableMapper getVariableMapper() {
         return this.varMapper;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see javax.el.ELContext#getContext(java.lang.Class)
      */
     public Object getContext(Class key) {
         return this.ctx.getContext(key);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see javax.el.ELContext#putContext(java.lang.Class, java.lang.Object)
      */
     public void putContext(Class key, Object contextObject) {
         this.ctx.putContext(key, contextObject);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.facelets.FaceletContext#generateUniqueId(java.lang.String)
      */
     public String generateUniqueId(String base) {
         Integer cnt = (Integer) this.ids.get(base);
         if (cnt == null) {
             this.ids.put(base, new Integer(0));
             return base;
         } else {
             int i = cnt.intValue() + 1;
             this.ids.put(base, new Integer(i));
             return base + "_" + i;
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.facelets.FaceletContext#getAttribute(java.lang.String)
      */
     public Object getAttribute(String name) {
         if (this.varMapper != null) {
             ValueExpression ve = this.varMapper.resolveVariable(name);
             if (ve != null) {
                 return ve.getValue(this);
             }
         }
         return null;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.facelets.FaceletContext#setAttribute(java.lang.String,
      *      java.lang.Object)
      */
     public void setAttribute(String name, Object value) {
         if (this.varMapper != null) {
             if (value == null) {
                 this.varMapper.setVariable(name, null);
             } else {
                 this.varMapper.setVariable(name, this.facelet
                         .getExpressionFactory().createValueExpression(value,
                                 Object.class));
             }
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.facelets.FaceletContext#includeFacelet(javax.faces.component.UIComponent,
      *      java.net.URL)
      */
     public void includeFacelet(UIComponent parent, URL absolutePath)
             throws IOException, FaceletException, FacesException, ELException {
         this.facelet.include(this, parent, absolutePath);
     }
 
     public ELResolver getELResolver() {
         return this.ctx.getELResolver();
     }
 
     private final List clients;
 
     public void popClient(TemplateClient client) {
         if (!this.clients.isEmpty()) {
             Iterator itr = this.clients.iterator();
             while (itr.hasNext()) {
                 if (itr.next().equals(client)) {
                     itr.remove();
                     return;
                 }
             }
         }
         throw new IllegalStateException(client + " not found");
     }
 
     public void pushClient(final TemplateClient client) {
         this.clients.add(0, new TemplateManager(this.facelet, client, true));
     }
 
     public void extendClient(final TemplateClient client) {
         this.clients.add(new TemplateManager(this.facelet, client, false));
     }
 
     public boolean includeDefinition(UIComponent parent, String name)
             throws IOException, FaceletException, FacesException, ELException {
         boolean found = false;
         TemplateManager client;
 
        for (int i = 0; i < this.clients.size() && found == false; i++) {
             client = ((TemplateManager) this.clients.get(i));
             if (client.equals(this.facelet))
                 continue;            
            if (client.isRoot() && i != 0)
            	break;
             found = client.apply(this, parent, name);            
         }
 
         return found;
     }
 
     private final static class TemplateManager implements TemplateClient {
         private final DefaultFacelet owner;
 
         private final TemplateClient target;
         
         private final boolean root;
 
         private final Set names = new HashSet();
 
         public TemplateManager(DefaultFacelet owner, TemplateClient target, boolean root) {
             this.owner = owner;
             this.target = target;
             this.root = root;
         }
 
         public boolean apply(FaceletContext ctx, UIComponent parent, String name)
                 throws IOException, FacesException, FaceletException,
                 ELException {
             String testName = (name != null) ? name : "facelets._NULL_DEF_";
             if (this.names.contains(testName)) {
                 return false;
             } else {
                 this.names.add(testName);
                 boolean found = false;
                 found = this.target.apply(new DefaultFaceletContext(
                         (DefaultFaceletContext) ctx, this.owner), parent, name);
                 this.names.remove(testName);
                 return found;
             }
         }
 
         public boolean equals(Object o) {
             // System.out.println(this.owner.getAlias() + " == " +
             // ((DefaultFacelet) o).getAlias());
             return this.owner == o || this.target == o;
         }
         
         public boolean isRoot() {
         	return this.root;
         }
     }
     
 
 	public boolean isPropertyResolved() {
 		return this.ctx.isPropertyResolved();
 	}
 
 	public void setPropertyResolved(boolean resolved) {
 		this.ctx.setPropertyResolved(resolved);
 	}
 }
