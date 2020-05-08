 /**
  * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
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
 
 package com.sun.facelets.tag.core;
 
 import java.io.IOException;
 
 import javax.el.ELException;
 import javax.faces.FacesException;
 import javax.faces.component.UIComponent;
 
 import com.sun.facelets.FaceletContext;
 import com.sun.facelets.FaceletException;
 import com.sun.facelets.tag.TagAttribute;
 import com.sun.facelets.tag.TagConfig;
 import com.sun.facelets.tag.TagException;
 import com.sun.facelets.tag.TagHandler;
 
 /**
  * Sets the specified name and attribute on the parent UIComponent. If the
  * "value" specified is not a literal, it will instead set the ValueExpression
  * on the UIComponent.
  * <p />
  * See <a target="_new"
  * href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/tlddocs/f/attribute.html">tag
  * documentation</a>.
  * 
  * @see javax.faces.component.UIComponent#getAttributes()
  * @see javax.faces.component.UIComponent#setValueExpression(java.lang.String,
  *      javax.el.ValueExpression)
  * @author Jacob Hookom
 * @version $Id: AttributeHandler.java,v 1.2 2005-07-20 06:37:08 jhook Exp $
  */
 public final class AttributeHandler extends TagHandler {
 
     private final TagAttribute name;
 
     private final TagAttribute value;
 
     /**
      * @param config
      */
     public AttributeHandler(TagConfig config) {
         super(config);
         this.name = this.getRequiredAttribute("name");
         this.value = this.getRequiredAttribute("value");
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.facelets.FaceletHandler#apply(com.sun.facelets.FaceletContext,
      *      javax.faces.component.UIComponent)
      */
     public void apply(FaceletContext ctx, UIComponent parent)
             throws IOException, FacesException, FaceletException, ELException {
         if (parent == null) {
             throw new TagException(this.tag, "Parent UIComponent was null");
         }
 
         // only process if the parent is new to the tree
         if (parent.getParent() == null) {
             String n = this.name.getValue(ctx);
             if (!parent.getAttributes().containsKey(n)) {
                 if (this.value.isLiteral()) {
                     parent.getAttributes().put(n, this.value.getValue());
                 } else {
                    parent.setValueExpression(n, this.value.getValueExpression(
                            ctx, Object.class));
                 }
             }
         }
     }
 }
