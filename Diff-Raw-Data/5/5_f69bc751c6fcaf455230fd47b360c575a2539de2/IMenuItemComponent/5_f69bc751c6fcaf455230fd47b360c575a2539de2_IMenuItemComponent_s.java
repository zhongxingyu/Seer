 /*******************************************************************************
  * Copyright (c) 2012 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation 
  *******************************************************************************/
 package org.eclipse.jubula.rc.common.tester.adapter.interfaces;
 
 /**
  * Interface for all necessary methods for testing MenuItems.
  * 
  * @author BREDEX GmbH
  */
 public interface IMenuItemComponent extends IComponent, ISelectionComponent {
     /**
      * @return the text which is saved in the item
      */
     public String getText();
     
     /**
      * @return <code>true</code> if the component is enabled
      */
     public boolean isEnabled();
     
     /**
      * @return <code>true</code> if the component exists
      *         that means there is an item in this wrapper 
      */
     public boolean isExisting();
     
     /**
      * @return <code>true</code> if the component is showing. If there is no
      *         implementation for getting the display status also return
      *         <code>true</code>
      */
     public boolean isShowing();
     
    /** Gets the menu of the menuitem 
     * @return the menu which is attached to the item
     */
    public IMenuComponent getMenu();
    
     /**
      * @return <code>true</code> if the menuitem has a submenu
      */
     public boolean hasSubMenu();
     /**
      * Checks whether the given menu item is a separator. 
      * This method runs in the GUI thread.
      * @return <code>true</code> if <code>menuItem</code> is a separator item.
      *         Otherwise <code>false</code>.
      */
     public boolean isSeparator();
     
     /**
      * This Methods selects the specific menuItem with an click
      * or in another toolkit specific way
      */
     public void selectMenuItem();
     
     /**
      * This method tries to open the the next Menu in the cascade
      * 
      * @return the next SubMenu in the Menu
      */
     public IMenuComponent openSubMenu();
 }
