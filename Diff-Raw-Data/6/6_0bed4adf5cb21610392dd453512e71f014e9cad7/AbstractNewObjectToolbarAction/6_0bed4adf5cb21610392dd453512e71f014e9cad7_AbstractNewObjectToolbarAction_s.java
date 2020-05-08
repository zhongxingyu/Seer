 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 /*
  *  Copyright (C) 2011 thorsten
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package de.cismet.cids.navigator.utils;
 
 import Sirius.navigator.connection.SessionManager;
 import Sirius.navigator.types.treenode.DefaultMetaTreeNode;
 import Sirius.navigator.types.treenode.ObjectTreeNode;
 import Sirius.navigator.ui.ComponentRegistry;
 
 import Sirius.server.middleware.types.MetaObjectNode;
 import Sirius.server.newuser.permission.Policy;
 
 import java.awt.event.ActionEvent;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 
 import de.cismet.cids.dynamics.CidsBean;
 
 import de.cismet.tools.gui.Static2DTools;
 
 /**
  * DOCUMENT ME!
  *
  * @author   thorsten
  * @version  $Revision$, $Date$
  */
 public abstract class AbstractNewObjectToolbarAction extends AbstractAction implements CidsClientToolbarItem {
 
     //~ Instance fields --------------------------------------------------------
 
     ImageIcon add = new ImageIcon(this.getClass().getResource("/Sirius/navigator/resource/img/bullet_add.png"));
     private final transient org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
     private CidsBean cb = null;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new AbstractNewObjectToolbarAction object.
      */
     public AbstractNewObjectToolbarAction() {
         try {
             cb = CidsBean.createNewCidsBeanFromTableName(getDomain(), getTableName());
            final ImageIcon base = new ImageIcon(cb.getMetaObject().getMetaClass().getIconData());
 //            final ImageIcon overlay = Static2DTools.createOverlayIcon(add, 20, 20);
 //            setIcon(Static2DTools.mergeIcons(base, overlay));
             setIcon(base);
             setTooltip(getTooltipString());
         } catch (Exception e) {
             log.warn("Could not create CidsBean in ToolbarActionProvider. Check Permissions for " + getTableName() + "@"
                         + getDomain(),
                 e);
         }
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public boolean isVisible() {
         return (cb != null) && cb.getHasWritePermission(SessionManager.getSession().getUser());
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  i  DOCUMENT ME!
      */
     public void setIcon(final Icon i) {
         putValue(Action.SMALL_ICON, i);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public Icon getIcon() {
         return (Icon)getValue(Action.SMALL_ICON);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  text  DOCUMENT ME!
      */
     public void setTooltip(final String text) {
         putValue(Action.SHORT_DESCRIPTION, text);
     }
 
     @Override
     public void actionPerformed(final ActionEvent ae) {
         try {
             cb = CidsBean.createNewCidsBeanFromTableName(getDomain(), getTableName());
             final MetaObjectNode metaObjectNode = new MetaObjectNode(
                     -1,
                     SessionManager.getSession().getUser().getDomain(),
                     cb.getMetaObject(),
                     null,
                     null,
                     true,
                     Policy.createWIKIPolicy(),
                     -1,
                     null,
                     false);
             final DefaultMetaTreeNode metaTreeNode = new ObjectTreeNode(metaObjectNode);
             ComponentRegistry.getRegistry().showComponent(ComponentRegistry.ATTRIBUTE_EDITOR);
             ComponentRegistry.getRegistry().getAttributeEditor().setTreeNode(metaTreeNode);
         } catch (Exception ex) {
             log.error("Could not create Object", ex);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public abstract String getDomain();
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public abstract String getTableName();
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public abstract String getTooltipString();
 }
