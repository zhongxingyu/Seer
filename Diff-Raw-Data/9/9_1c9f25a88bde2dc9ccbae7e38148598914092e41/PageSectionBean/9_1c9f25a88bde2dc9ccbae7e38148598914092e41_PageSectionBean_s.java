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
 package org.shredzone.cilla.admin.page;
 
 import java.io.Serializable;
 
 import javax.annotation.Resource;
 import javax.faces.component.UIInput;
 import javax.faces.context.FacesContext;
 
 import org.shredzone.cilla.ws.exception.CillaServiceException;
 import org.shredzone.cilla.ws.page.PageDto;
 import org.shredzone.cilla.ws.page.PageWs;
 import org.shredzone.cilla.ws.page.SectionDto;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Component;
 
 /**
  * A bean for creating new sections in a {@link PageDto}.
  *
  * @author Richard "Shred" Körber
  */
 @Component
@Scope("view")
 public class PageSectionBean implements Serializable {
     private static final long serialVersionUID = 9023901491882118139L;
 
     private @Resource PageWs pageWs;
     private @Resource PageBean pageBean;
 
     private UIInput sectionUi;
 
     /**
      * UI Binding for the section type selector.
      */
     public UIInput getNewSectionBinding()       { return sectionUi; }
     public void setNewSectionBinding(UIInput selectBinding) { this.sectionUi = selectBinding; }
 
     /**
      * Adds a new section to the current page. The section type is selected by the UI
      * component set at {@link #setNewSectionBinding(UIInput)}.
      */
     public void addNewSection() throws CillaServiceException {
         String newType = sectionUi.getValue().toString();
         SectionDto section = pageWs.createNewSection(newType);
         pageBean.getPage().getSections().add(section);
         FacesContext.getCurrentInstance().renderResponse();
     }
 
 }
