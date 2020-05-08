 /*
  * OpenRemote, the Home of the Digital Home. Copyright 2008-2009, OpenRemote Inc.
  * 
  * See the contributors.txt file in the distribution for a full listing of individual contributors.
  * 
  * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
  * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
  * later version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
  * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU Affero General Public License along with this program. If not, see
  * <http://www.gnu.org/licenses/>.
  */
 package org.openremote.modeler.domain.component;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.persistence.Transient;
 
 import org.openremote.modeler.client.utils.IDUtil;
 import org.openremote.modeler.domain.BusinessEntity;
 import org.openremote.modeler.domain.ClientGroup;
 
 import flexjson.JSON;
 
 /**
  * parent for UIButton,UISwich,UIGrid...
  * 
  * @author Javen
  * 
  */
 @SuppressWarnings("serial")
 public abstract class UIComponent extends BusinessEntity {
 
    private transient boolean removed = false;
    private Set<ClientGroup> groups = new HashSet<ClientGroup>();
 
    public UIComponent() {
    }
 
    public UIComponent(long id) {
       super(id);
    }
 
    public String getName() {
       return "UIComponent";
    }
 
    public boolean isRemoved() {
       return removed;
    }
 
    public void setRemoved(boolean removed) {
       this.removed = removed;
    }
 
    /*
     * Generate the xml content which used in panel.xml
     */
    @Transient
    @JSON(include=false)
    public abstract String getPanelXml();
    
    public int getPreferredWidth() {
       return 50;
    }
 
    public int getPreferredHeight() {
       return 50;
    }
 
    /**
     * create a new UIComponet with the same type of <b>uiComponent</b>
     * 
     * @param uiComponent
     * @return a new UIComponet with the same type of <b>uiComponent</b>
     */
    public static UIComponent createNew(UIComponent uiComponent) {
       UIComponent result = null;
       if (uiComponent != null) {
          if (uiComponent instanceof UIButton) {
             result = new UIButton();
          } else if (uiComponent instanceof UISwitch) {
             result = new UISwitch();
          } else if (uiComponent instanceof UISlider) {
             result = new UISlider();
          } else if (uiComponent instanceof UILabel) {
             result = new UILabel();
          } else if (uiComponent instanceof UIImage) {
             result = new UIImage();
          } else if (uiComponent instanceof UITabbar) {
             return new UITabbar();
          }
       }
       result.setOid(IDUtil.nextID());
       return result;
    }
 
    /**
     * create a new UIComponet with the same type and the same attribute of <b>uiComponent</b>
     * 
     * @param uiComponent
     * @return a new UIComponet with the same type and the same attribute of <b>uiComponent</b>
     */
    public static UIComponent copy(UIComponent uiComponent) {
       if (uiComponent != null) {
          if (uiComponent instanceof UIButton) {
             return new UIButton((UIButton) uiComponent);
          } else if (uiComponent instanceof UISwitch) {
             return new UISwitch((UISwitch) uiComponent);
          } else if (uiComponent instanceof UISlider) {
             return new UISlider((UISlider) uiComponent);
          } else if (uiComponent instanceof UILabel) {
             return new UILabel((UILabel) uiComponent);
          } else if (uiComponent instanceof UIImage) {
             return new UIImage((UIImage) uiComponent);
          }  else if (uiComponent instanceof UITabbar) {
             return new UITabbar((UITabbar)uiComponent);
          }
       }
       return null;
    }
 
    @Override
    public boolean equals(Object obj) {
       if (this == obj) {
          return true;
       }
       if (obj == null) {
          return false;
       }
       if (getClass() != obj.getClass()) {
          return false;
       }
       UIComponent other = (UIComponent) obj;
       return other.getPanelXml().equals(getPanelXml());
    }
    
    /**
     * @deprecated Use getGroups()
     * @return the first group
     */
    public ClientGroup getGroup() {
 	   ClientGroup group = null;
 	   if(!groups.isEmpty()) {
 		   group = (ClientGroup) groups.toArray()[0];
 	   }
 	   return group;
    }
    
    /**
     * @return All the groups of the component
     */
    public Collection<ClientGroup> getGroups() {
 	   return groups;
    }
    
    /**
     * Add a group to the component
     * @param group The group you want to add.
     */
    public void addGroup(ClientGroup group) {
 	   groups.add(group);
    }
 
    /**
     * @deprecated use addGroup(ClientGroup)
     * Set the current group of the Component
     * @param group The group you want to set
     */
    public void setGroup(ClientGroup group) {
 	   //Temporary, because we are only supporting one group at the moment.
	   if(groups.size() >= 1) {
 		   groups.clear();
 	   }
	   if(group != null) {
		   groups.add(group);
	   }
    }
    
    /**
     * A utility function to add the group section to the xml. 
     * @param xmlContent The StringBuffer containing the XML
     */
    public void addGroupsToXML(StringBuffer xmlContent) {
       xmlContent.append("            <clientgroups>\n");
       if(groups != null && groups.size() > 0) {
 	      for(ClientGroup group: groups) {
 	    	  if(group != null) {
 	    		  xmlContent.append("              <clientgroup id=\"" + group.getOid() + "\" name=\"" + group.getName() + "\"/> \n");
 	    	  }
 	      }
       }
       xmlContent.append("            </clientgroups>\n");
    }
    
    @Override
    public int hashCode() {
       return (int) getOid();
    }
 
 }
