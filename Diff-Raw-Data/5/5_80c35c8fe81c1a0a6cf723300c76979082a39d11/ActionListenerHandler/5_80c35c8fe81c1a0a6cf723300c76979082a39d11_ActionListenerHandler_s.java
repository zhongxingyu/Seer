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
 
 package com.sun.facelets.tag.jsf.core;
 
 import java.io.IOException;
 
 import javax.el.ELException;
 import javax.el.ValueExpression;
 import javax.faces.FacesException;
 import javax.faces.component.ActionSource;
 import javax.faces.component.UIComponent;
 import javax.faces.event.ActionListener;
 
 import com.sun.facelets.FaceletContext;
 import com.sun.facelets.FaceletException;
 import com.sun.facelets.tag.TagAttribute;
 import com.sun.facelets.tag.TagAttributeException;
 import com.sun.facelets.tag.TagConfig;
 import com.sun.facelets.tag.TagException;
 import com.sun.facelets.tag.TagHandler;
 
 /**
  * Register an ActionListener instance on the UIComponent associated with the
  * closest parent UIComponent custom action. <p/> See <a target="_new"
  * href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/tlddocs/f/actionListener.html">tag
  * documentation</a>.
  * 
  * @see javax.faces.event.ActionListener
  * @see javax.faces.component.ActionSource
  * @author Jacob Hookom
 * @version $Id: ActionListenerHandler.java,v 1.2 2005/08/24 04:38:49 jhook Exp $
  */
 public final class ActionListenerHandler extends TagHandler {
 
     private Class listenerType;
 
     private final TagAttribute type;
 
     private final TagAttribute binding;
 
     /**
      * @param config
      */
     public ActionListenerHandler(TagConfig config) {
         super(config);
         this.binding = this.getAttribute("binding");
        this.type = this.getRequiredAttribute("type");
         if (type != null) {
             if (!type.isLiteral()) {
                 throw new TagAttributeException(this.tag, this.type, "Must be literal");
             }
             try {
                 this.listenerType = Class.forName(type.getValue());
             } catch (Exception e) {
                 throw new TagAttributeException(this.tag, this.type, e);
             }
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
         if (parent instanceof ActionSource) {
             // only process if parent was just created
             if (parent.getParent() == null) {
                 ActionSource src = (ActionSource) parent;
                 ActionListener listener = null;
                 ValueExpression ve = null;
                 if (this.binding != null) {
                     ve = this.binding.getValueExpression(ctx,
                             ActionListener.class);
                     listener = (ActionListener) ve.getValue(ctx);
                 }
                 if (listener == null) {
                     try {
                         listener = (ActionListener) listenerType.newInstance();
                     } catch (Exception e) {
                         throw new TagAttributeException(this.tag, this.type, e.getCause());
                     }
                     if (ve != null) {
                         ve.setValue(ctx, ve);
                     }
                 }
                 src.addActionListener(listener);
             }
         } else {
             throw new TagException(this.tag,
                     "Parent is not of type ActionSource, type is: " + parent);
         }
     }
 }
