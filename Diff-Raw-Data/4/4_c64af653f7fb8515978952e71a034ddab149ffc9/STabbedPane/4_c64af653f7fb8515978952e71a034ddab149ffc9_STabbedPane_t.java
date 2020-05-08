 /*
  * $Id$
  * (c) Copyright 2000 wingS development team.
  *
  * This file is part of wingS (http://wings.mercatis.de).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 
 package org.wings;
 
 import java.awt.Color;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.io.Serializable;
 import java.util.ArrayList;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.swing.SingleSelectionModel;
 import javax.swing.DefaultSingleSelectionModel;
 import javax.swing.GrayFilter;
 import javax.swing.ImageIcon;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import org.wings.plaf.*;
 import org.wings.session.SessionManager;
 import org.wings.style.*;
 
 // fixme: refactorize.
 /**
  * A tabbed pane shows one tab (usually a panel) at a moment.
  * The user can switch between the panels.
  *
  * @author <a href="mailto:haaf@mercatis.de">Armin Haaf</a>,
  * 	<a href="mailto:andre.lison@general-bytes.com">Andre Lison</a>
  * @version $Revision$
  */
 public class STabbedPane 
     extends SContainer
     implements SSelectionComponent, LowLevelEventListener, ChangeListener
 {
     /**
      * @see #getCGClassID
      */
     private static final String cgClassID = "TabbedPaneCG";
 
     /**
      * Where the tabs are placed.
      * @see #setTabPlacement
      */
     protected int tabPlacement = TOP;
 
     /** The default selection model */
     protected SingleSelectionModel model;
 
     ArrayList pages = new ArrayList(2);
 
     /**
      * layout used to render the tabs. Only one tab is on top at a time.
      */
     final private SCardLayout card = new SCardLayout();
 
     /**
      * container for all tabs. The card layout shows always one on
      * top.
      */
     final private SContainer contents = new SContainer(card);
 
     /** the maximum tabs per line */
     protected int maxTabsPerLine = -1;
 
     /** The style of selected tabs */
     protected String selectionStyle;
 
     /** The dynamic attributes of selected tabs */
     protected AttributeSet selectionAttributes = new SimpleAttributeSet();
 
    /** used form tabs or links */
    protected boolean showAsFormComponent = false;
 
     private Logger fLogger = Logger.getLogger("org.wings.STabbedPane");
 
 	private DynamicResource fStyleSheet = null;
 	
 	/**
 	 * Number of selected tab.
 	 */
 	protected int selectedIndex = 0;
 	
 	/**
 	 * the newly selected index during a 
 	 * lowlevelevent
 	 */
 	private int lleChangedIndex = -1;
 
     /**
      * Creates a new empty Tabbed Pane with the tabs at the top.
      * @see #addTab
      */
     public STabbedPane() {
         this(TOP);
     }
 
     /**
      * Creates an empty TabbedPane with the specified tab placement
      * of either: TOP, BOTTOM, LEFT, or RIGHT.
      * @param tabPlacement the placement for the tabs relative to the content
      * @see #addTab
      */
     public STabbedPane(int tabPlacement) {
         super();
 
         setTabPlacement(tabPlacement);
         setBackground(new Color(204,204,204));
         setSelectionBackground(new Color(170,170,255));
         setFont(new SFont("Verdana,Arial,Helvetica,sans serif", SConstants.PLAIN, 10));
 
         super.addComponent(contents, null, 0);
 		setModel(new DefaultSingleSelectionModel());
     }
 
     /**
      * @param style the style of selected cells
      */
     public void setSelectionStyle(String selectionStyle) {
         this.selectionStyle = selectionStyle;
     }
 
     /**
      * @return the style of selected cells.
      */
     public String getSelectionStyle() { return selectionStyle; }
 
     /**
      * Set a selectionAttribute.
      * @param name the selectionAttribute name
      * @param value the selectionAttribute value
      */
     public void setSelectionAttribute(String name, String value) {
         boolean changed = selectionAttributes.contains(name);
         selectionAttributes.put(name, value);
 
         if (changed)
             reload(ReloadManager.RELOAD_STYLE);
     }
 
     /**
      * return the value of an selectionAttribute.
      * @param name the selectionAttribute name
      */
     public String getSelectionAttribute(String name) {
         return selectionAttributes.get(name);
     }
 
     /**
      * remove an selectionAttribute
      * @param name the selectionAttribute name
      */
     public String removeSelectionAttribute(String name) {
         if ( selectionAttributes.contains(name) ) {
             String value = selectionAttributes.remove(name);
 
             reload(ReloadManager.RELOAD_STYLE);
 
             return value;
         }
 
         return null;
     }
 
 
     /**
      * Set the selectionAttributes.
      * @param selectionAttributes the selectionAttributes
      */
     public void setSelectionAttributes(AttributeSet selectionAttributes) {
         if (selectionAttributes == null)
             throw new IllegalArgumentException("null not allowed");
 
         if (!this.selectionAttributes.equals(selectionAttributes)) {
             this.selectionAttributes = selectionAttributes;
             reload(ReloadManager.RELOAD_STYLE);
         }
     }
 
     /**
      * @return the current selectionAttributes
      */
     public AttributeSet getSelectionAttributes() {
         return selectionAttributes;
     }
 
     /**
      * Set the background color of the selected
      * tab. This is ignored, if <i>showAsFormComponent</i> is on.
      * @param c the new background color
      */
     public void setSelectionBackground(Color color) {
         setSelectionAttribute("background-color", CSSStyleSheet.getAttribute(color));
     }
 
     /**
      * Return the background color.
      * @return the background color
      */
     public Color getSelectionBackground() {
         return CSSStyleSheet.getBackground(selectionAttributes);
     }
 
     /**
      * Set the foreground color.
      * @param c the new foreground color
      */
     public void setSelectionForeground(Color color) {
         setSelectionAttribute("color", CSSStyleSheet.getAttribute(color));
     }
 
     /**
      * Return the foreground color.
      * @return the foreground color
      */
     public Color getSelectionForeground() {
         return CSSStyleSheet.getForeground(selectionAttributes);
     }
 
     /**
      * Add a listener to the list of change listeners.
      * ChangeListeners are notified, when the tab selection changes.
      *
      * @param cl add to listener list
      */
     public void addChangeListener(ChangeListener cl) {
         addEventListener(ChangeListener.class, cl);
     }
 
     /**
      * Remove listener from the list of change listeners.
      * ChangeListeners are notified, when the tab selection changes.
      *
      * @param cl remove from listener list
      */
     public void removeChangeListener(ChangeListener cl) {
         removeEventListener(ChangeListener.class, cl);
     }
 
     /**
      * Fire ChangeEvents at all registered change listeners.
      */
     protected void fireStateChanged() {
         ChangeEvent event = null;
 
         // maybe the better way to do this is to user the getListenerList
         // and iterate through all listeners, this saves the creation of
         // an array but it must cast to the apropriate listener
         Object[] listeners = getListenerList();
         for ( int i = listeners.length-2; i>=0; i -= 2 ) {
             if ( listeners[i]==ChangeListener.class ) {
                 // Lazily create the event:
                 if ( event==null )
                     event = new ChangeEvent(this);
                 ((ChangeListener) listeners[i+1]).stateChanged(event);
             }
         }
     }
 
     /**
      * Returns the placement of the tabs for this tabbedpane.
      * @see #setTabPlacement
      */
     public int getTabPlacement() {
         return tabPlacement;
     }
 
     /**
      * Sets the tab placement for this tabbedpane.
      * Possible values are:<ul>
      * <li>SConstants.TOP
      * <li>SConstants.BOTTOM
      * <li>SConstants.LEFT
      * <li>SConstants.RIGHT
      * </ul>
      * The default value is TOP.
      *
      * @param tabPlacement the placement for the tabs relative to the content
      *
      */
     public void setTabPlacement(int tabPlacement) {
         if ( tabPlacement != TOP && tabPlacement != LEFT &&
              tabPlacement != BOTTOM && tabPlacement != RIGHT ) {
             throw new IllegalArgumentException("illegal tab placement: must be TOP, BOTTOM, LEFT, or RIGHT");
         }
 
         this.tabPlacement = tabPlacement;
 		if (fStyleSheet != null) fStyleSheet.invalidate();
     }
 
     /**
      * Returns the model associated with this tabbedpane.
      *
      * @see #setModel
      */
     public SingleSelectionModel getModel() {
         return model;
     }
 
     /**
      * Sets the model to be used with this tabbedpane.
      * @param model the model to be used
      *
      * @see #getModel
      */
     public void setModel(SingleSelectionModel model) {
         this.model = model;
         model.addChangeListener(this);
     }
 
     /**
      * Returns the currently selected index for this tabbedpane.
      * Returns -1 if there is no currently selected tab.
      *
      * @return the index of the selected tab
      * @see #setSelectedIndex
      */
     public int getSelectedIndex() {
         return model.getSelectedIndex();
     }
 
     /**
      * Sets the selected index for this tabbedpane.
      *
      * @see #getSelectedIndex
      * @see SingleSelectionModel#setSelectedIndex
      * @beaninfo
      *   preferred: true
      * description: The tabbedpane's selected tab index.
      */
     public void setSelectedIndex(int index) {
         model.setSelectedIndex(index);
     }
 
     /**
      * Returns the currently selected component for this tabbedpane.
      * Returns null if there is no currently selected tab.
      *
      * @return the component corresponding to the selected tab
      * @see #setSelectedComponent
      */
     public SComponent getSelectedComponent() {
         int index = getSelectedIndex();
         if ( index == -1 ) {
             return null;
         }
         return getTabAt(index);
     }
 
     /**
      * Sets the selected component for this tabbedpane.  This
      * will automatically set the selectedIndex to the index
      * corresponding to the specified component.
      *
      * @see #getSelectedComponent
      * @beaninfo
      *   preferred: true
      * description: The tabbedpane's selected component.
      */
     public void setSelectedComponent(SComponent c) {
         int index = indexOfComponent(c);
         if ( index != -1 ) {
             setSelectedIndex(index);
         }
         else {
             throw new IllegalArgumentException("component not found in tabbed pane");
         }
     }
 
     /**
      * Returns the index of the tab for the specified component.
      * Returns -1 if there is no tab for this component.
      * @param component the component for the tab
      */
     public int indexOfComponent(SComponent component) {
         for ( int i = 0; i < getTabCount(); ++i ) {
             if ( ((Page) pages.get(i)).component.equals(component) ) {
                 return i;
             }
         }
         return -1;
     }
 
     /**
      * Returns the number of tabs in this tabbedpane.
      *
      * @return an int specifying the number of tabbed pages
      */
     public int getTabCount() {
         return pages.size();
     }
 
     /**
      * Inserts a <i>component</i>, at <i>index</i>, represented by a
      * <i>title</i> and/or <i>icon</i>, either of which may be null.
      * Uses java.util.ArrayList internally, see insertElementAt()
      * for details of insertion conventions.
      * @param title the title to be displayed in this tab
      * @param icon the icon to be displayed in this tab
      * @param component The component to be displayed when this tab is clicked.
      * @param tip the tooltip to be displayed for this tab
      * @param index the position to insert this new tab
      *
      * @see #addTab
      * @see #removeTabAt
      */
     public void insertTab(String title, SIcon icon,
                           SComponent component, String tip,
                           int index) {
 
         SIcon disabledIcon = null;
 
         if (icon != null && icon instanceof SImageIcon) {
             disabledIcon = new SImageIcon(new ImageIcon(GrayFilter.createDisabledImage(((SImageIcon)icon).getImage())));
         }
 
         String t = (title != null) ? title : "";
 
         Page p = new Page(t, icon, disabledIcon, component, tip);
         pages.add(index, p);
 
         contents.addComponent(p.component, p.component.getComponentId());
 
         if ( pages.size() == 1 ) {
             setSelectedIndex(0);
         }
     }
 
     /**
      * Adds a <i>component</i> and <i>tip</i> represented by a <i>title</i>
      * and/or <i>icon</i>, either of which can be null.
      * Cover method for insertTab().
      * @param title the title to be displayed in this tab
      * @param icon the icon to be displayed in this tab
      * @param component The component to be displayed when this tab is clicked.
      * @param tip the tooltip to be displayed for this tab
      *
      * @see #insertTab
      * @see #removeTabAt
      */
     public void addTab(String title, SIcon icon, SComponent component, String tip) {
         insertTab(title, icon, component, tip, pages.size());
     }
 
     /**
      * Adds a <i>component</i> represented by a <i>title</i> and/or <i>icon</i>,
      * either of which can be null.
      * Cover method for insertTab().
      * @param title the title to be displayed in this tab
      * @param icon the icon to be displayed in this tab
      * @param component The component to be displayed when this tab is clicked.
      *
      * @see #insertTab
      * @see #removeTabAt
      */
     public void addTab(String title, SIcon icon, SComponent component) {
         insertTab(title, icon, component, null, pages.size());
     }
 
     /**
      * Adds a <i>component</i> represented by a <i>title</i> and no icon.
      * Cover method for insertTab().
      * @param title the title to be displayed in this tab
      * @param component The component to be displayed when this tab is clicked.
      *
      * @see #insertTab
      * @see #removeTabAt
      */
     public void addTab(String title, SComponent component) {
         insertTab(title, null, component, null, pages.size());
     }
 
 
     /**
      * Adds a <i>component</i> with the specified tab title.
      * Cover method for insertTab().
      * @param title the title to be displayed in this tab
      * @param component The component to be displayed when this tab is clicked.
      *
      * @see #insertTab
      * @see #removeTabAt
      */
     public SComponent add(String title, SComponent component) {
         addTab(title, component);
         return component;
     }
 
     /**
      * Adds a <i>component</i> at the specified tab index.  If constraints
      * is a String or an Icon, it will be used for the tab title,
      * otherwise the component's name will be used as the tab title.
      * Cover method for insertTab().
      * @param component The component to be displayed when this tab is clicked.
      * @constraints the object to be displayed in the tab
      * @param index the position to insert this new tab
      *
      * @see #insertTab
      * @see #removeTabAt
      */
     public SComponent addComponent(SComponent component, 
                                    Object constraints) {
         return addComponent(component, constraints, pages.size());
     }
 
     /**
      * Adds a <i>component</i> at the specified tab index.  If constraints
      * is a String or an Icon, it will be used for the tab title,
      * otherwise the component's name will be used as the tab title.
      * Cover method for insertTab().
      * @param component The component to be displayed when this tab is clicked.
      * @constraints the object to be displayed in the tab
      * @param index the position to insert this new tab
      *
      * @see #insertTab
      * @see #removeTabAt
      */
     public SComponent addComponent(SComponent component, 
                                    Object constraints, int index) {
         SIcon icon = constraints instanceof SIcon ? (SIcon)constraints : null;
         String title = constraints instanceof String ? (String)constraints : null;
         insertTab(title, icon, component, null, Math.min(index, pages.size()));
 
         return component;
     }
 
     /**
      * Removes the tab at <i>index</i>.
      * After the component associated with <i>index</i> is removed,
      * its visibility is reset to true to ensure it will be visible
      * if added to other containers.
      * @param index the index of the tab to be removed
      *
      * @see #addTab
      * @see #insertTab
      */
     public void removeTabAt(int index) {
         // If we are removing the currently selected tab AND
         // it happens to be the last tab in the bunch, then
         // select the previous tab
         int tabCount = getTabCount();
         int selected = getSelectedIndex();
         if ( selected >= (tabCount - 1) ) {
             setSelectedIndex(selected - 1);
         }
 
         removePageAt(index);
     }
 
     /**
      * Removes the tab which corresponds to the specified component.
      *
      * @param component the component to remove from the tabbedpane
      * @see #addTab
      * @see #removeTabAt
      */
     public void remove(SComponent component) {
         int index = indexOfComponent(component);
         if ( index != -1 ) {
             removeTabAt(index);
         }
     }
 
     /**
      * Sets the maximum tabs per line. tabs <= 0: No maximum.
      */
     public void setMaxTabsPerLine(int tabs) {
         maxTabsPerLine = tabs;
     }
 
     /**
      * Returns the maximum tabs per line.
      */
     public int getMaxTabsPerLine() {
         return maxTabsPerLine;
     }
 
     /**
      * Returns the tab title at <i>index</i>.
      *
      * @see #setTitleAt
      */
     public String getTitleAt(int index) {
         return ((Page)pages.get(index)).title;
     }
 
     /**
      * Returns the tab icon at <i>index</i>.
      *
      * @see #setIconAt
      */
     public SIcon getIconAt(int index) {
         return ((Page)pages.get(index)).icon;
     }
 
     /**
      * Returns the tab disabled icon at <i>index</i>.
      *
      * @see #setDisabledIconAt
      */
     public SIcon getDisabledIconAt(int index) {
         return ((Page)pages.get(index)).disabledIcon;
     }
 
     /**
      * Returns the tab background color at <i>index</i>.
      *
      * @see #setBackgroundAt
      */
     public Color getBackgroundAt(int index) {
         return ((Page)pages.get(index)).background;
     }
 
     /**
      * Returns the tab foreground color at <i>index</i>.
      *
      * @see #setForegroundAt
      */
     public Color getForegroundAt(int index) {
         return ((Page)pages.get(index)).foreground;
     }
 
     /**
      * Returns the tab style at <i>index</i>.
      *
      * @see #setStyleAt
      */
     public String getStyleAt(int index) {
         return ((Page)pages.get(index)).style;
     }
 
     /**
      * Returns whether or not the tab at <i>index</i> is
      * currently enabled.
      *
      * @see #setEnabledAt
      */
     public boolean isEnabledAt(int index) {
         return ((Page)pages.get(index)).enabled;
     }
 
     /**
      * Returns the component at <i>index</i>.
      *
      * @see #setComponentAt
      * @deprecated use {@link #getComponentAt} instead (swing conformity)
      */
     public SComponent getTabAt(int index) {
         return ((Page)pages.get(index)).component;
     }
 
     /**
      * Returns the component at <i>index</i>.
      *
      * @see #setComponentAt
      */
     public SComponent getComponentAt(int index) {
         return ((Page)pages.get(index)).component;
     }
 
     /**
      * Sets the title at <i>index</i> to <i>title</i> which can be null.
      * An internal exception is raised if there is no tab at that index.
      * @param index the tab index where the title should be set
      * @param title the title to be displayed in the tab
      *
      * @see #getTitleAt
      */
     public void setTitleAt(int index, String title) {
         ((Page)pages.get(index)).title = title;
     }
 
     /**
      * Sets the icon at <i>index</i> to <i>icon</i> which can be null.
      * An internal exception is raised if there is no tab at that index.
      * @param index the tab index where the icon should be set
      * @param icon the icon to be displayed in the tab
      *
      * @see #getIconAt
      */
     public void setIconAt(int index, SIcon icon) {
         ((Page)pages.get(index)).icon = icon;
     }
 
     /**
      * Sets the disabled icon at <i>index</i> to <i>icon</i> which can be null.
      * An internal exception is raised if there is no tab at that index.
      * @param index the tab index where the disabled icon should be set
      * @param icon the icon to be displayed in the tab when disabled
      *
      * @see #getDisabledIconAt
      */
     public void setDisabledIconAt(int index, SIcon disabledIcon) {
         ((Page)pages.get(index)).disabledIcon = disabledIcon;
     }
 
     /**
      * Sets the background color at <i>index</i> to <i>background</i>
      * which can be null, in which case the tab's background color
      * will default to the background color of the tabbedpane.
      * An internal exception is raised if there is no tab at that index.
      * @param index the tab index where the background should be set
      * @param background the color to be displayed in the tab's background
      *
      * @see #getBackgroundAt
      */
     public void setBackgroundAt(int index, Color background) {
         ((Page)pages.get(index)).background = background;
     }
 
 
     /**
      * Sets the foreground color.
      * @param foreground the color to be displayed as the tab's foreground
      * @see #getForeground
      */
     public void setForeground(Color foreground) {
         super.setForeground(foreground);
         if (fStyleSheet != null) fStyleSheet.invalidate();
     }
 
     /**
      * Sets the background color.
      * @param background the color to use as background.
      * @see #getForeground
      */
     public void setBackground(Color background) {
         super.setBackground(background);
         if (fStyleSheet != null) fStyleSheet.invalidate();
     }
 
     /**
      * Sets the foreground color at <i>index</i> to <i>foreground</i>
      * which can be null, in which case the tab's foreground color
      * will default to the foreground color of this tabbedpane.
      * An internal exception is raised if there is no tab at that index.
      * @param index the tab index where the foreground should be set
      * @param foreground the color to be displayed as the tab's foreground
      *
      * @see #getForegroundAt
      */
     public void setForegroundAt(int index, Color foreground) {
         ((Page)pages.get(index)).foreground = foreground;
     }
 
     /**
      * Sets the style at <i>index</i> to <i>style</i>
      * which can be null, in which case the tab's style
      * will default to the style of this tabbedpane.
      * An internal exception is raised if there is no tab at that index.
      * @param index the tab index where the style should be set
      * @param foreground the style to be used as the tab's style
      *
      * @see #getStyleAt
      */
     public void setStyleAt(int index, String style) {
         ((Page)pages.get(index)).style = style;
     }
 
     /**
      * Sets whether or not the tab at <i>index</i> is enabled.
      * An internal exception is raised if there is no tab at that index.
      * @param index the tab index which should be enabled/disabled
      * @param enabled whether or not the tab should be enabled
      *
      * @see #isEnabledAt
      */
     public void setEnabledAt(int index, boolean enabled) {
         ((Page)pages.get(index)).enabled = enabled;
     }
     
     /**
      * Set the tooltip text for tab at <i>index</i>
      * @param index set the tooltip for this tab
      */
     public void setToolTipTextAt(int index, String toolTip) {
         ((Page) pages.get(index)).toolTip = toolTip;
     }
 
 	/**
 	 * Get the tooltip text from tab at <i>index</i>
 	 * @return the text or <i>null</i> if not set.
 	 */
 	public String getToolTipTextAt(int index) {
 	    return ((Page) pages.get(index)).toolTip;
 	}
 	
     /**
      * Sets the component at <i>index</i> to <i>component</i>.
      * An internal exception is raised if there is no tab at that index.
      * @param index the tab index where this component is being placed
      * @param component the component for the tab
      *
      * @see #getComponentAt
      */
     public void setComponentAt(int index, SComponent component) {
         Page page = (Page)pages.get(index);
         if ( component != page.component ) {
             if ( page.component != null ) {
                 contents.remove(page.component);
             }
             page.component = component;
             contents.addComponent(page.component, page.component.getComponentId());
             if (getSelectedIndex() == index)
             	card.show(component);
         }
     }
 
     /**
      * Returns the first tab index with a given <i>title</i>,
      * Returns -1 if no tab has this title.
      * @param title the title for the tab
      */
     public int indexOfTab(String title) {
         for ( int i = 0; i < getTabCount(); i++ ) {
             if ( getTitleAt(i).equals( (title == null) ? "" : title) ) {
                 return i;
             }
         }
         return -1;
     }
 
     /**
      * Returns the first tab index with a given <i>icon</i>.
      * Returns -1 if no tab has this icon.
      * @param icon the icon for the tab
      */
     public int indexOfTab(SIcon icon) {
         for ( int i = 0; i < getTabCount(); i++ ) {
             if ( getIconAt(i).equals(icon) ) {
                 return i;
             }
         }
         return -1;
     }
 
     private void removePageAt(int i) {
         pages.remove(i);
         contents.remove(((Page)pages.get(i)).component);
     }
 
 	/**
 	 * Lightweight container for tab properties.
 	 */
     private class Page implements Serializable
     {
         public String		title;
         public String		toolTip;
         public Color		foreground;
         public Color		background;
         public SIcon		icon;
         public SIcon		disabledIcon;
         public boolean		enabled = true;
         public String		style;
         public SComponent	component;
 
         public Page(String title, SIcon icon,
                     SIcon disabledIcon, SComponent component, String tip) {
 			this.title = title;
 			this.toolTip = tip;
 			this.icon = icon;
 			this.disabledIcon = disabledIcon;
 			this.component = component;
         }
     }
 
     /**
      * Set display mode (href or form-component).
      * An AbstractButton can appear as HTML-Form-Button or as 
      * HTML-HREF. If button is inside a {@link org.wings.SForm} the default
      * is displaying it as html form button.
      * Setting <i>showAsFormComponent</i> to <i>false</i> will
      * force displaying as href even if button is inside 
      * a form.
      * @param showAsFormComponent if true, display as link, if false as html form component.
      */
     public void setShowAsFormComponent(boolean showAsFormComponent) {
         if (this.showAsFormComponent == showAsFormComponent) return;
         this.showAsFormComponent = showAsFormComponent;
 		if (fStyleSheet != null) fStyleSheet.invalidate();
     }
 
 	/**
       * Test, what display method is set.
       * @see #setShowAsFormComponent(boolean)
       * @return true, if displayed as link, false when displayed as html form component.
       */
     public boolean getShowAsFormComponent() {
         return showAsFormComponent && getResidesInForm();
     }
 
     /**
      * Set the parent frame of this tabbed pane
      * @param f the parent frame.
      */
     public void setParentFrame(SFrame f)
     {
         super.setParentFrame(f);
 		contents.setParentFrame(f);
 		ComponentCG cg = this.getCG();
 		if (f != null && cg instanceof org.wings.plaf.TabbedPaneCG)
 		{
 		    fLogger.log(Level.FINEST, "STabbedPane.setParentFrame, Installing stylesheet ...");
 		    fStyleSheet = ((org.wings.plaf.TabbedPaneCG) cg).installStyleSheet(this);
 		}
     }
 
     public String getCGClassID() {
         return cgClassID;
     }
 
     public void setCG(TabbedPaneCG cg) {
         super.setCG(cg);
     }
 
     /**
      * Tab was clicked.
      * @see LowLevelEventListener#processLowLevelEvent(String, String[])
      */
     public void processLowLevelEvent(String name, String[] values)
     {
         if ( !name.startsWith(getLowLevelEventId()) ) {
             return;
         }
 		for (int i=0;i<values.length;++i) {
 		    try {
 		        int index = new Integer(values[i]).intValue();
 		        if (index < 0 || index >= pages.size())
 		        	continue;
 
 		        /* prevent clever users from showing
 		         * disabled tabs
 		         */
 				if (((Page) pages.get(index)).enabled) {
 				    lleChangedIndex = index;
 				    SForm.addArmedComponent(this);
 				    return;
 				}
 		    }
 		    catch (NumberFormatException nfe) {
 		        continue;
 		    }
 		}
     }
 
 	/**
 	 * Does nothin'.
 	 */
     public void fireIntermediateEvents() {
     }
 
 	/**
 	 * Sets selection and fire changeevents, if user changed 
 	 * tab selection.
 	 */
     public void fireFinalEvents() {
         if (lleChangedIndex > -1)
         	setSelectedIndex(lleChangedIndex);
         lleChangedIndex = -1;
     }
 
     /**
      * When tab selection changed.
      * @see ChangeListener#stateChanged(ChangeEvent)
      */
     public void stateChanged(ChangeEvent ce)
     {
         final int index = model.getSelectedIndex();
         if (index >= pages.size()) return;
         card.show(((Page) pages.get(index)).component);
 
         reload(ReloadManager.RELOAD_CODE);
         fireStateChanged();
     }
 
 }
 
 /*
  * Local variables:
  * c-basic-offset: 4
  * indent-tabs-mode: nil
  * compile-command: "ant -emacs -find build.xml"
  * End:
  */
