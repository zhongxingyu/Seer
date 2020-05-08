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
 
 package com.sun.facelets.tag.ui;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.el.ELException;
 import javax.el.VariableMapper;
 import javax.faces.FacesException;
 import javax.faces.component.UIComponent;
 
 import com.sun.facelets.FaceletContext;
 import com.sun.facelets.FaceletException;
 import com.sun.facelets.FaceletHandler;
 import com.sun.facelets.TemplateClient;
 import com.sun.facelets.el.VariableMapperWrapper;
 import com.sun.facelets.tag.TagAttribute;
 import com.sun.facelets.tag.TagConfig;
 import com.sun.facelets.tag.TagHandler;
 
 /**
  * @author Jacob Hookom
 * @version $Id: DecorateHandler.java,v 1.12 2006-07-07 04:23:44 jhook Exp $
  */
 public final class DecorateHandler extends TagHandler implements TemplateClient {
 
     private final Logger log = Logger.getLogger("facelets.tag.ui.decorate");
     
     private final TagAttribute template;
 
     private final Map handlers;
     
     private final ParamHandler[] params;
 
     /**
      * @param config
      */
     public DecorateHandler(TagConfig config) {
         super(config);
         this.template = this.getRequiredAttribute("template");
         this.handlers = new HashMap();
 
         Iterator itr = this.findNextByType(DefineHandler.class);
         DefineHandler d = null;
         while (itr.hasNext()) {
             d = (DefineHandler) itr.next();
             this.handlers.put(d.getName(), d);
             if (log.isLoggable(Level.FINE)) {
                 log.fine(tag + " found Define[" + d.getName() + "]");
             }
         }
         List paramC = new ArrayList();
         itr = this.findNextByType(ParamHandler.class);
         while (itr.hasNext()) {
             paramC.add(itr.next());
         }
         if (paramC.size() > 0) {
             this.params = new ParamHandler[paramC.size()];
             for (int i = 0; i < this.params.length; i++) {
                 this.params[i] = (ParamHandler) paramC.get(i);
             }
         } else {
             this.params = null;
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.facelets.FaceletHandler#apply(com.sun.facelets.FaceletContext,
      *      javax.faces.component.UIComponent)
      */
     public void apply(FaceletContext ctx, UIComponent parent)
             throws IOException, FacesException, FaceletException, ELException {
         VariableMapper orig = ctx.getVariableMapper();
         if (this.params != null) {
             VariableMapper vm = new VariableMapperWrapper(orig);
             ctx.setVariableMapper(vm);
             for (int i = 0; i < this.params.length; i++) {
                 this.params[i].apply(ctx, parent);
             }
         }
 
        ctx.pushClient(this);
         try {
             ctx.includeFacelet(parent, this.template.getValue(ctx));
         } finally {
             ctx.setVariableMapper(orig);
             ctx.popClient(this);
         }
     }
 
     public boolean apply(FaceletContext ctx, UIComponent parent, String name) throws IOException, FacesException, FaceletException, ELException {
         if (name != null) {
             FaceletHandler handler = (FaceletHandler) this.handlers.get(name);
             if (handler != null) {
                 handler.apply(ctx, parent);
                 return true;
             } else {
                 return false;
             }
         } else {
             this.nextHandler.apply(ctx, parent);
             return true;
         }
     }
 }
