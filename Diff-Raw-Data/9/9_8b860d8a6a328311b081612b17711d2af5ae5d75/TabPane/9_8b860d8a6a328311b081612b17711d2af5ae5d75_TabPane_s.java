 /* 
  * This file is part of the Echo2 Extras Project.
  * Copyright (C) 2005-2006 NextApp, Inc.
  *
  * Version: MPL 1.1/GPL 2.0/LGPL 2.1
  *
  * The contents of this file are subject to the Mozilla Public License Version
  * 1.1 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  * http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
  * for the specific language governing rights and limitations under the
  * License.
  *
  * Alternatively, the contents of this file may be used under the terms of
  * either the GNU General Public License Version 2 or later (the "GPL"), or
  * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
  * in which case the provisions of the GPL or the LGPL are applicable instead
  * of those above. If you wish to allow use of your version of this file only
  * under the terms of either the GPL or the LGPL, and not to allow others to
  * use your version of this file under the terms of the MPL, indicate your
  * decision by deleting the provisions above and replace them with the notice
  * and other provisions required by the GPL or the LGPL. If you do not delete
  * the provisions above, a recipient may use your version of this file under
  * the terms of any one of the MPL, the GPL or the LGPL.
  */
 
 package nextapp.echo2.extras.app;
 
 import nextapp.echo2.app.Border;
 import nextapp.echo2.app.Color;
 import nextapp.echo2.app.Component;
 import nextapp.echo2.app.Extent;
 import nextapp.echo2.app.FillImage;
 import nextapp.echo2.app.Font;
 import nextapp.echo2.app.Insets;
 import nextapp.echo2.app.Pane;
 import nextapp.echo2.app.PaneContainer;
 
 /**
  * A container pane which displays child components in separate tabs.
  */
 public class TabPane extends Component 
 implements Pane, PaneContainer {
     
     public static final String INPUT_TAB_INDEX = "inputTabIndex";
     public static final String ACTIVE_TAB_INDEX_CHANGED_PROPERTY = "activeTabIndex";
     
     public static final String PROPERTY_BORDER_TYPE = "borderType";
     public static final String PROPERTY_DEFAULT_CONTENT_INSETS = "defaultContentInsets";
     public static final String PROPERTY_INSETS = "insets";
     public static final String PROPERTY_TAB_ACTIVE_BACKGROUND = "tabActiveBackground";
     public static final String PROPERTY_TAB_ACTIVE_BACKGROUND_IMAGE = "tabActiveBackgroundImage";
     public static final String PROPERTY_TAB_ACTIVE_BORDER = "tabActiveBorder";
     public static final String PROPERTY_TAB_ACTIVE_FONT = "tabActiveFont";
     public static final String PROPERTY_TAB_ACTIVE_FOREGROUND = "tabActiveForeground";
     public static final String PROPERTY_TAB_HEIGHT = "tabHeight";
     public static final String PROPERTY_TAB_INACTIVE_BACKGROUND = "tabInactiveBackground";
     public static final String PROPERTY_TAB_INACTIVE_BACKGROUND_IMAGE = "tabInactiveBackgroundImage";
     public static final String PROPERTY_TAB_INACTIVE_BORDER = "tabInactiveBorder";
     public static final String PROPERTY_TAB_INACTIVE_FONT = "tabInactiveFont";
     public static final String PROPERTY_TAB_INACTIVE_FOREGROUND = "tabInactiveForeground";
     public static final String PROPERTY_TAB_INSET = "tabInset";
     public static final String PROPERTY_TAB_SPACING = "tabSpacing";
     public static final String PROPERTY_TAB_POSITION = "tabPosition";
     public static final String PROPERTY_TAB_WIDTH = "tabWidth";
     
     /**
      * Constant for the <code>borderType</code> property indicating that no 
      * border should be drawn around the content.
      */
     public static final int BORDER_TYPE_NONE = 0;
 
     /**
      * Constant for the <code>borderType</code> property indicating that a
      * border should be drawn immediately adjacent to the tabs only.
      * If the tabs are positioned at the top of the <code>TabPane</code> the
      * border will only be drawn directly beneath the tabs with this setting.  
      * If the tabs are positioned at the bottom of the <code>TabPane</code> the
      * border will only be drawn directly above the tabs with this setting.
      * This is the default rendering style.
      */
     public static final int BORDER_TYPE_ADJACENT_TO_TABS = 1;
 
     /**
      * Constant for the <code>borderType</code> property indicating that
      * borders should be drawn above and below the content, but not at its 
      * sides.
      */
     public static final int BORDER_TYPE_PARALLEL_TO_TABS = 2;
     
     /**
      * Constant for the <code>borderType</code> property indicating that
      * borders should be drawn on all sides of the content.
      */
     public static final int BORDER_TYPE_SURROUND = 3;
     
     /**
      * Constant for the <code>tabPosition</code> property indicating that the
      * tabs should be rendered beneath the content.
      */
     public static final int TAB_POSITION_BOTTOM = 1;
     
     /**
      * Constant for the <code>tabPosition</code> property indicating that the
      * tabs should be rendered above the content.
      * This is the default rendering style.
      */
     public static final int TAB_POSITION_TOP = 0;
     
     /**
      * Index of active tab.
      */ 
     private int activeTabIndex = -1;
     
     /**
      * Returns the index of the active tab.
      * 
      * @return the active tab index
      */
     public int getActiveTabIndex() {
         return activeTabIndex;
     }
     
     /**
      * Returns the mode in which the border will be drawn around the 
      * <code>TabPane</code>.
      * 
      * @return the border border type, one of the following values:
      *         <ul>
      *          <li><code>BORDER_TYPE_NONE</code></li>
      *          <li><code>BORDER_TYPE_ADJACENT_TO_TABS</code> (the default)</li>
      *          <li><code>BORDER_TYPE_PARALLEL_TO_TABS</code></li>
      *          <li><code>BORDER_TYPE_SURROUND</code></li>
      *         </ul>
      */
     public int getBorderType() {
         Integer value = (Integer) getProperty(PROPERTY_BORDER_TYPE);
         if (value == null) {
             return BORDER_TYPE_ADJACENT_TO_TABS;
         } else {
             return value.intValue();
         }
     }
 
     /**
      * Returns the default content inset margin.  This margin is applied by
      * default to each child component.
      *
      * @return the default content inset margin
      */
     public Insets getDefaultContentInsets() {
         return (Insets) getProperty(PROPERTY_DEFAULT_CONTENT_INSETS);
     }
     
     /**
      * Returns the <code>Insets</code> around the entire <code>TabPane</code>.
      * Insets will only be drawn on sides of the <code>TabPane</code> which have
      * a border (i.e., based on the value of the <code>borderType</code> 
      * property).
      * Values for this property must be in pixel units.
      * 
      * @return the insets
      */
     public Insets getInsets() {
     	return (Insets) getProperty(PROPERTY_INSETS);
     }
     
     /**
      * Returns the background color used to render active tabs.
      * 
      * @return the active tab background
      */
     public Color getTabActiveBackground() {
         return (Color) getProperty(PROPERTY_TAB_ACTIVE_BACKGROUND);
     }
     
     /**
      * Returns the background image used to render active tabs.
      * 
      * @return the active tab background image
      */
     public FillImage getTabActiveBackgroundImage() {
         return (FillImage) getProperty(PROPERTY_TAB_ACTIVE_BACKGROUND_IMAGE);
     }
     
     /**
      * Returns the <code>Border</code> used to draw the active tab and 
      * surround the content of the <code>TabPane</code>.
      * 
      * @return the border
      */
     public Border getTabActiveBorder() {
         return (Border) getProperty(PROPERTY_TAB_ACTIVE_BORDER);
     }
     
     /**
      * Returns the font used to render active tabs.
      * 
      * @return the active tab font
      */
     public Font getTabActiveFont() {
         return (Font) getProperty(PROPERTY_TAB_ACTIVE_FONT);
     }
     
     /**
      * Returns the foreground color used to render active tabs.
      * 
      * @return the active tab foreground
      */
     public Color getTabActiveForeground() {
         return (Color) getProperty(PROPERTY_TAB_ACTIVE_FOREGROUND);
     }
     
     /**
      * Returns the height of an individual tab.
      * <code>Extent</code> values for this property must be in pixel units.
      * 
      * @return the tab height
      */
     public Extent getTabHeight() {
         return (Extent) getProperty(PROPERTY_TAB_HEIGHT);
     }
     
     /**
      * Returns the background color used to render inactive tabs.
      * 
      * @return the inactive tab background
      */
     public Color getTabInactiveBackground() {
         return (Color) getProperty(PROPERTY_TAB_INACTIVE_BACKGROUND);
     }
     
     /**
      * Returns the background image used to render inactive tabs.
      * 
      * @return the inactive tab background image
      */
     public FillImage getTabInactiveBackgroundImage() {
         return (FillImage) getProperty(PROPERTY_TAB_INACTIVE_BACKGROUND_IMAGE);
     }
 
     /**
      * Returns the <code>Border</code> used to draw inactive tabs.
      * 
      * @return the border
      */
     public Border getTabInactiveBorder() {
         return (Border) getProperty(PROPERTY_TAB_INACTIVE_BORDER);
     }
     
     /**
      * Returns the font used to render inactive tabs.
      * 
      * @return the inactive tab font
      */
     public Font getTabInactiveFont() {
         return (Font) getProperty(PROPERTY_TAB_INACTIVE_FONT);
     }
     
     /**
      * Returns the foreground color used to render inactive tabs.
      * 
      * @return the inactive tab foreground
      */
     public Color getTabInactiveForeground() {
         return (Color) getProperty(PROPERTY_TAB_INACTIVE_FOREGROUND);
     }
     
     /**
      * Returns the horizontal distance from which all tabs are inset from 
      * the edge of the <code>TabPane</code>.
      * 
      * @return the tab inset
      */
     public Extent getTabInset() {
         return (Extent) getProperty(PROPERTY_TAB_INSET);
     }
     
     /**
      * Returns the position where the tabs are located relative to the pane 
      * content.
      * 
      * @return the tab position, one of the following values:
      *         <ul>
      *          <li><code>TAB_POSITION_TOP</code></li>
      *          <li><code>TAB_POSITION_BOTTOM</code></li>
      *         </ul>
      */
     public int getTabPosition() {
         Integer tabPosition = (Integer) getProperty(PROPERTY_TAB_POSITION);
         return tabPosition == null ? TAB_POSITION_TOP : tabPosition.intValue();
     }
     
     /**
      * Returns the horizontal space between individual tabs.
      * 
      * @return the tab spacing
      */
     public Extent getTabSpacing() {
         return (Extent) getProperty(PROPERTY_TAB_SPACING);
     }
     
     /**
      * Returns the width of an individual tab.
      * 
      * @return the tab width
      */
     public Extent getTabWidth() {
         return (Extent) getProperty(PROPERTY_TAB_WIDTH);
     }
     
     /**
      * @see nextapp.echo2.app.Component#isValidParent(nextapp.echo2.app.Component)
      */
     public boolean isValidParent(Component c) {
         if (!super.isValidParent(c)) {
             return false;
         }
         // Ensure parent is a PaneContainer.
         return c instanceof PaneContainer;
     }
     
     /**
      * @see nextapp.echo2.app.Component#processInput(java.lang.String, java.lang.Object)
      */
     public void processInput(String inputName, Object inputValue) {
         super.processInput(inputName, inputValue);
         if (inputName.equals(INPUT_TAB_INDEX)) {
             setActiveTabIndex(((Integer) inputValue).intValue());
         }
     }
     
     /**
      * Sets the active tab index.
      * 
      * @param newValue the index of the child <code>Component</code> whose tab
      *        should be displayed
      */
     public void setActiveTabIndex(int newValue) {
         int oldValue = activeTabIndex;
         activeTabIndex = newValue;
         firePropertyChange(ACTIVE_TAB_INDEX_CHANGED_PROPERTY, new Integer(oldValue), new Integer(newValue));
     }
 
     /**
      * Sets the mode in which the border will be drawn around the 
      * <code>TabPane</code>.
      * 
      * @param newValue the new border type one of the following values:
      *        <ul>
      *         <li><code>BORDER_TYPE_NONE</code></li>
      *         <li><code>BORDER_TYPE_ADJACENT_TO_TABS</code> (the default)</li>
      *         <li><code>BORDER_TYPE_PARALLEL_TO_TABS</code></li>
      *         <li><code>BORDER_TYPE_SURROUND</code></li>
      *        </ul>
      */
     public void setBorderType(int newValue) {
         setProperty(PROPERTY_BORDER_TYPE, new Integer(newValue));
     }
 
     /**
      * Sets the default content inset margin.  This margin is applied by 
      * default to each child component.
      *
      * @param newValue the new default content inset margin
      */
     public void setDefaultContentInsets(Insets newValue) {
         setProperty(PROPERTY_DEFAULT_CONTENT_INSETS, newValue);
     }
 
     /**
      * Sets the <code>Insets</code> around the entire <code>TabPane</code>.
      * Insets will only be drawn on sides of the <code>TabPane</code> which have
      * a border (i.e., based on the value of the <code>borderType</code> 
      * property).
      * Values for this property must be in pixel units.
      * 
     * @return newValue the new inset
      */
     public void setInsets(Insets newValue) {
     	setProperty(PROPERTY_INSETS, newValue);
     }
     
     /**
      * Sets the background color used to render active tabs.
      * 
      * @param newValue the new active tab background
      */
     public void setTabActiveBackground(Color newValue) {
         setProperty(PROPERTY_TAB_ACTIVE_BACKGROUND, newValue);
     }
     
     /**
      * Sets the background image used to render active tabs.
      * 
      * @param newValue the new active tab background image
      */
     public void setTabActiveBackgroundImage(FillImage newValue) {
         setProperty(PROPERTY_TAB_ACTIVE_BACKGROUND_IMAGE, newValue);
     }
     
     /**
      * Sets the <code>Border</code> used to draw the active tab and 
      * surround the content of the <code>TabPane</code>.
      * 
      * @param newValue the new border
      */
     public void setTabActiveBorder(Border newValue) {
         setProperty(PROPERTY_TAB_ACTIVE_BORDER, newValue);
     }
     
     /**
      * Sets the font used to render active tabs.
      * 
      * @param newValue the new active tab font
      */
     public void setTabActiveFont(Font newValue) {
         setProperty(PROPERTY_TAB_ACTIVE_FONT, newValue);
     }
     
     /**
      * Sets the foreground color used to render active tabs.
      * 
      * @param newValue the new active tab foreground
      */
     public void setTabActiveForeground(Color newValue) {
         setProperty(PROPERTY_TAB_ACTIVE_FOREGROUND, newValue);
     }
     
     /**
      * Sets the height of an individual tab.
      * <code>Extent</code> values for this property must be in pixel units.
      * 
      * @param newValue the new tab height
      */
     public void setTabHeight(Extent newValue) {
         setProperty(PROPERTY_TAB_HEIGHT, newValue);
     }
     
     /**
      * Sets the background color used to render inactive tabs.
      * 
      * @param newValue the new inactive tab background
      */
     public void setTabInactiveBackground(Color newValue) {
         setProperty(PROPERTY_TAB_INACTIVE_BACKGROUND, newValue);
     }
     
     /**
      * Sets the background image used to render inactive tabs.
      * 
      * @param newValue the new inactive tab background image
      */
     public void setTabInactiveBackgroundImage(FillImage newValue) {
         setProperty(PROPERTY_TAB_INACTIVE_BACKGROUND_IMAGE, newValue);
     }
     
     /**
      * Sets the <code>Border</code> used to draw inactive tabs in the 
      * <code>TabPane</code>.
      * 
      * @param newValue the new border
      */
     public void setTabInactiveBorder(Border newValue) {
         setProperty(PROPERTY_TAB_INACTIVE_BORDER, newValue);
     }
     
     /**
      * Sets the font used to render inactive tabs.
      * 
      * @param newValue the new inactive tab font
      */
     public void setTabInactiveFont(Font newValue) {
         setProperty(PROPERTY_TAB_INACTIVE_FONT, newValue);
     }
     
     /**
      * Sets the foreground color used to render inactive tabs.
      * 
      * @param newValue the new inactive tab foreground
      */
     public void setTabInactiveForeground(Color newValue) {
         setProperty(PROPERTY_TAB_INACTIVE_FOREGROUND, newValue);
     }
     
     /**
      * Sets the horizontal distance from which all tabs are inset from 
      * the edge of the <code>TabPane</code>.
      * 
      * @param newValue the new tab inset
      */
     public void setTabInset(Extent newValue) {
         setProperty(PROPERTY_TAB_INSET, newValue);
     }
     
     /**
      * Sets the position where the tabs are located relative to the pane 
      * content.
      * 
      * @param newValue the new tab position, one of the following values:
      *        <ul>
      *         <li><code>TAB_POSITION_TOP</code></li>
      *         <li><code>TAB_POSITION_BOTTOM</code></li>
      *        </ul>
      */
     public void setTabPosition(int newValue) {
         setProperty(PROPERTY_TAB_POSITION, new Integer(newValue));
     }
     
     /**
      * Sets the horizontal space between individual tabs.
      * 
      * @param newValue the new tab spacing
      */
     public void setTabSpacing(Extent newValue) {
         setProperty(PROPERTY_TAB_SPACING, newValue);
     }
     
     /**
      * Sets the width of an individual tab.
      * 
      * @param newValue the new tab width
      */
     public void setTabWidth(Extent newValue) {
         setProperty(PROPERTY_TAB_WIDTH, newValue);
     }
 }
