 /*
  * cilla - Blog Management System
  *
  * Copyright (C) 2012 Richard "Shred" Körber
  *   http://cilla.shredzone.org
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published
  * by the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.shredzone.cilla.admin.page.gallery;
 
 import java.io.Serializable;
 
 import javax.faces.component.UIComponent;
 
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Controller;
 
 /**
  * Bean for editing within the gallery grid of a gallery section.
  *
  * @author Richard "Shred" Körber
  */
 @Controller
@Scope("request")
 public class GalleryGridBean implements Serializable {
     private static final long serialVersionUID = 5676997413214057854L;
 
     private UIComponent uiGrid;
 
     /**
      * Reference to the {@link UIComponent} of the data grid showing the pictures of the
      * gallery.
      */
     public UIComponent getUiGrid()              { return uiGrid; }
     public void setUiGrid(UIComponent uiGrid)   { this.uiGrid = uiGrid; }
 
 }
