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
 import javax.faces.FacesException;
 import javax.faces.component.UIComponent;
 import javax.faces.component.UIComponentBase;
 
 import com.sun.facelets.FaceletContext;
 import com.sun.facelets.FaceletException;
 import com.sun.facelets.tag.TagAttribute;
 import com.sun.facelets.tag.TagConfig;
 import com.sun.facelets.tag.TagException;
 import com.sun.facelets.tag.TagHandler;
 
 /**
  * Register a named facet on the UIComponent associated with the closest parent
  * UIComponent custom action. <p/> See <a target="_new"
  * href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/tlddocs/f/facet.html">tag
  * documentation</a>.
  * 
  * @author Jacob Hookom
 * @version $Id: FacetHandler.java,v 1.2 2005/08/24 04:38:49 jhook Exp $
  */
 public final class FacetHandler extends TagHandler {
 
     /**
      * A UIComponent for capturing a child UIComponent, representative of the
      * desired Facet
      * 
      * @author Jacob Hookom
      * 
      */
     private final static class UIFacet extends UIComponentBase {
         public String getFamily() {
             return null;
         }
     }
 
     protected final TagAttribute name;
 
     public FacetHandler(TagConfig config) {
         super(config);
        this.name = this.getAttribute("name");
     }
 
     /* (non-Javadoc)
      * @see com.sun.facelets.FaceletHandler#apply(com.sun.facelets.FaceletContext, javax.faces.component.UIComponent)
      */
     public void apply(FaceletContext ctx, UIComponent parent)
             throws IOException, FacesException, FaceletException, ELException {
         UIFacet facet = new UIFacet();
         this.nextHandler.apply(ctx, facet);
         int childCount = facet.getChildCount();
         UIComponent c;
         if (childCount == 1) {
             c = (UIComponent) facet.getChildren().get(0);
             parent.getFacets().put(this.name.getValue(ctx), c);
         } else {
             throw new TagException(this.tag, "Facet Tag can only have one child UIComponent");
         }
     }
 }
