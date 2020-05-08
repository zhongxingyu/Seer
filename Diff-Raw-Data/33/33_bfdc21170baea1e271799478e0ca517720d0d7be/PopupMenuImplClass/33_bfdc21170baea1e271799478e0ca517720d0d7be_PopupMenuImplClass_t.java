 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.rc.swt.implclasses;
 
import org.eclipse.jubula.rc.common.exception.StepExecutionException;
import org.eclipse.jubula.tools.objects.event.EventFactory;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.Widget;
 
 /**
  * Not exactly an ImplClass, but contains ImplClass-style methods for 
  * testing Popup/Dropdown menus.
  *
  * @author BREDEX GmbH
  * @created 26.04.2007
  */
 public class PopupMenuImplClass extends MenuImplClass {
 
     /** The menu */
     private Menu m_menu;
     
     /**
      * {@inheritDoc}
      */
     public void setComponent(Object graphicsComponent) {
         m_menu = (Menu)graphicsComponent;
     }
 
     /**
      * {@inheritDoc}
      */
     public Widget getComponent() {
         return m_menu;
     }
 
     /**
      * {@inheritDoc}
      */
     protected void closeMenu(int[] indexItems) {
         super.closeMenu(indexItems);
         int pathLength = indexItems != null ? indexItems.length : 0;
         PopupMenuUtil.closePopup(getRobot(), (Menu)getComponent(), pathLength);
     }
 
     /**
      * {@inheritDoc}
      */
     protected void closeMenu(String operator, String[] pathItems) {
         super.closeMenu(operator, pathItems);
 
         int pathLength = pathItems != null ? pathItems.length : 0;
         PopupMenuUtil.closePopup(getRobot(), (Menu)getComponent(), pathLength);
     }
    
    /**
     * 
     * {@inheritDoc}
     */
    public void selectMenuItem(String namePath, final String operator) {
        final String[] pathItems = MenuUtil.splitPath(namePath);
        if (pathItems.length == 0) {
            throw new StepExecutionException("empty path to menuitem not allowed", //$NON-NLS-1$
                EventFactory.createActionError());
        }

        PopupMenuUtil.selectMenuItem(getRobot(), (Menu)getComponent(), 
                pathItems, operator);
    }
    
    /**
     * 
     * {@inheritDoc}
     */
    public void selectMenuItemByIndexpath(String indexPath) {
        final int[] indexItems = MenuUtil.splitIndexPath(indexPath);
        if (indexItems.length == 0) {
            throw new StepExecutionException("empty path to menuitem not allowed", //$NON-NLS-1$
                EventFactory.createActionError());
        }
        
        PopupMenuUtil.selectMenuItem(getRobot(), (Menu)getComponent(), 
                indexItems);
    }    
 }
