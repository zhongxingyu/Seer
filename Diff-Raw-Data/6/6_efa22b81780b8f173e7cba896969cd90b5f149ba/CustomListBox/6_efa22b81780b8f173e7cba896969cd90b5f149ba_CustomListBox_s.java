 package org.pentaho.gwt.widgets.client.listbox;
 
 import com.allen_sauer.gwt.dnd.client.DragController;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.*;
 import com.google.gwt.user.client.ui.*;
 import org.pentaho.gwt.widgets.client.utils.ElementUtils;
 import org.pentaho.gwt.widgets.client.utils.Rectangle;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * ComplexListBox is a List-style widget can contain custom list-items made (images + text, text + checkboxes)
  * This list is displayed as a drop-down style component by default. If the visibleRowCount property is set higher
  * than 1 (default), the list is rendered as a multi-line list box.
  *
  * <P>Usage:
  *
  * <p>
  *
  * <pre>
  * ComplexListBox list = new ComplexListBox();
  *
  *  list.addItem("Alberta");
  *  list.addItem("Atlanta");
  *  list.addItem("San Francisco");
  *  list.addItem(new DefaultListItem("Testing", new Image("16x16sample.png")));
  *  list.addItem(new DefaultListItem("Testing 2", new CheckBox()));
  *
  *  list.setVisibleRowCount(6); // turns representation from drop-down to list
  *
  *  list.addChangeListener(new ChangeListener(){
  *    public void onChange(Widget widget) {
  *      System.out.println(""+list.getSelectedIdex());
  *    }
  *  });
  *</pre>
  *
  * User: NBaker
  * Date: Mar 9, 2009
  * Time: 11:01:57 AM
  *
  */
 @SuppressWarnings("deprecation")
 public class CustomListBox extends HorizontalPanel implements ChangeListener, PopupListener, MouseListener, FocusListener, KeyboardListener, ListItemListener {
   private List<ListItem> items = new ArrayList<ListItem>();
   private int selectedIndex = -1;
   private DropDownArrow arrow = new DropDownArrow();
   private int visible = 1;
   private int maxDropVisible = 15;
   private boolean editable = false;
   private VerticalPanel listPanel = new VerticalPanel();
   private ScrollPanel listScrollPanel = new ScrollPanel();
 
   // Members for drop-down style
   private FlexTable dropGrid= new FlexTable();
   private boolean popupShowing = false;
   private DropPopupPanel popup;
   private PopupList popupVbox = new PopupList();
   private FocusPanel fPanel = new FocusPanel();
   private ScrollPanel popupScrollPanel = new ScrollPanel();
 
 
   private List<ChangeListener> listeners = new ArrayList<ChangeListener>();
   private final int spacing = 1;
   private int maxHeight, maxWidth, averageHeight; //height and width of largest ListItem
   @SuppressWarnings("unused")
   private String primaryStyleName;
   private String height, width;
   private String popupHeight;
   private String popupWidth;
   private boolean suppressLayout;
 
   private boolean enabled = true;
   private String val;
   private Command command;
   private DragController dragController;
   private boolean multiSelect;
 
 
   public CustomListBox(){
 
     dropGrid.getColumnFormatter().setWidth(0, "100%"); //$NON-NLS-1$
     dropGrid.setWidget(0,1, arrow);
     dropGrid.getElement().getStyle().setProperty("tableLayout", "fixed");
     arrow.getElement().getParentElement().getStyle().setProperty("width", "20px");
     dropGrid.setCellPadding(0);
     dropGrid.setCellSpacing(1);
     updateUI();
 
     // Add List Panel to it's scrollPanel
     listScrollPanel.add(listPanel);
     listScrollPanel.setHeight("100%"); //$NON-NLS-1$
     listScrollPanel.setWidth("100%"); //$NON-NLS-1$
     listScrollPanel.getElement().getStyle().setProperty("overflowX","hidden"); //$NON-NLS-1$ //$NON-NLS-2$
     //listScrollPanel.getElement().getStyle().setProperty("padding",spacing+"px");
     listPanel.setSpacing(spacing);
     listPanel.setWidth("100%"); //$NON-NLS-1$
 
     //default to drop-down
     fPanel.add(dropGrid);
     fPanel.setHeight("100%"); //$NON-NLS-1$
     super.add(fPanel);
 
     popupScrollPanel.add(popupVbox);
     popupScrollPanel.getElement().getStyle().setProperty("overflowX","hidden"); //$NON-NLS-1$ //$NON-NLS-2$
     popupVbox.setWidth("100%"); //$NON-NLS-1$
     popupVbox.setSpacing(spacing);
 
     fPanel.addMouseListener(this);
     fPanel.addFocusListener(this);
     fPanel.addKeyboardListener(this);
 
     this.setStylePrimaryName("custom-list"); //$NON-NLS-1$
 
 
     setTdStyles(this.getElement());
     setTdStyles(listPanel.getElement());
 
     editableTextBox = new TextBox(){
       @Override
       public void onBrowserEvent(Event event) {
 //        int code = event.getKeyCode();
 
         switch(DOM.eventGetType(event)){
           case Event.ONKEYUP:
             onChange(editableTextBox);
             val = editableTextBox.getText();
 //             event.cancelBubble(true);
             break;
           case Event.ONMOUSEUP:
             super.onBrowserEvent(event);
             event.cancelBubble(true);
           default:
             return;
         }
       }
     };
     editableTextBox.setStylePrimaryName("custom-list-textbox");
   }
 
   private native void setTdStyles(Element ele)/*-{
   var tds = ele.getElementsByTagName("td");
     for( var i=0; i< tds.length; i++){
       var td = tds[i];
       if(!td.style){
         td.className = "customListBoxTdFix";
       } else {
         td.style.padding = "0px";
         td.style.border = "none";
       }
     }
   }-*/;
 
   /**
    * Removes the passed in ListItem
    *
    * @param listItem item to remove
    */
   public void remove(ListItem listItem){
     this.items.remove(listItem);
     setSelectedIndex(0);
 
     if(suppressLayout == false){
       updateUI();
     }
   }
 
   /**
    * Removes all items from the list.
    */
   public void removeAll(){
     this.items.clear();
     this.selectedIndex = -1;
     this.selectedItems.clear();
 
     if(this.suppressLayout == false){
       for(ChangeListener l : listeners){
         l.onChange(this);
       }
     }
     if(suppressLayout == false){
       updateUI();
     }
   }
 
   /**
    * Removes all items from the list.
    */
   public void clear(){
     removeAll();
   }
 
   /**
    * Convenience method to support the more conventional method of child attachment
    * @param label
    */
   public void add(String label){
     this.addItem(label);
   }
 
   /**
    * Adds the given ListItem to the list control.
    *
    * @param item ListItem
    */
   public void addItem(ListItem item){
     items.add(item);
 
     item.setListItemListener(this);
 
     // If first one added, set selectedIndex to 0
     if(items.size() == 1 && this.visible == 1){
       setSelectedIndex(0);
     }
     if(suppressLayout == false){
       updateUI();
     }
 
     if(dragController != null){
       dragController.makeDraggable(item.getWidget());
     }
   }
 
   @Override
   public void add(Widget w) {
     addItem((ListItem) w);
   }
 
   /**
    * Call this method with true will suppress the re-laying out of the widget after every add/remove.
    * This is useful when adding a large batch of items to the listbox.
    */
   public void setSuppressLayout(boolean supress){
     this.suppressLayout = supress;
     if(! suppressLayout){
 
       if(selectedIndex < 0 && this.items.size() > 0){
         this.setSelectedIndex(0); // notifies listeners
       } else {
         // just notify listeners something has changed.
         for(ChangeListener l : listeners){
           l.onChange(this);
         }
       }
 
       updateUI();
     }
   }
 
   /**
    * Convenience method creates a {@link: DefaultListItem} with the given text and adds it to the list control
    *
    * @param label
    */
   public void addItem(String label){
     DefaultListItem item = new DefaultListItem(label);
     items.add(item);
     item.setListItemListener(this);
 
     // If first one added, set selectedIndex to 0
     if(items.size() == 1){
       setSelectedIndex(0);
     }
     if(suppressLayout == false){
       updateUI();
     }
     if(dragController != null){
       dragController.makeDraggable(item.getWidget());
     }
   }
 
 
   /**
    * Returns a list of current ListItems.
    *
    * @return List of ListItems
    */
   public List<ListItem> getItems(){
     return items;
   }
 
   /**
    * Sets the number of items to be displayed at once in the lsit control. If set to 1 (default) the list is rendered
    * as a drop-down
    *
    * @param visibleCount number of rows to be visible.
    */
   public void setVisibleRowCount(int visibleCount){
     int prevCount = visible;
     this.visible = visibleCount;
 
     if(visible > 1 && prevCount == 1){
       // switched from drop-down to list
       fPanel.remove(dropGrid);
       fPanel.add(listScrollPanel);
     } else if(visible == 1 && prevCount > 1){
       // switched from list to drop-down
       fPanel.remove(listScrollPanel);
       fPanel.add(dropGrid);
     }
 
     if(suppressLayout == false){
       updateUI();
     }
   }
 
   /**
    * Returns the number of rows visible in the list
    * @return number of visible rows.
    */
   public int getVisibleRowCount(){
     return visible;
   }
 
   private void updateUI(){
     if(! this.isAttached()){
       return;
     }
     if(visible > 1){
       updateList();
     } else {
       updateDropDown();
     }
   }
 
   /**
    * Returns the number of rows to be displayed in the drop-down popup.
    *
    * @return number of visible popup items.
    */
   public int getMaxDropVisible() {
     return maxDropVisible;
   }
 
   /**
    * Sets the number of items to be visible in the drop-down popup. If set lower than the number of items
    * a scroll-bar with provide access to hidden items.
    *
    * @param maxDropVisible number of items visible in popup.
    */
   public void setMaxDropVisible(int maxDropVisible) {
     this.maxDropVisible = maxDropVisible;
 
     // Update the popup to respect this value
     if(maxHeight > 0){ //Items already added
       this.popupHeight = this.maxDropVisible * maxHeight + "px"; //$NON-NLS-1$
     }
   }
 
   private TextBox editableTextBox;
   private SimplePanel selectedItemWrapper = new SimplePanel();
   private void updateSelectedDropWidget(){
     Widget selectedWidget = new Label(""); //Default to show in case of empty sets? //$NON-NLS-1$
     boolean updateMade = true;
     if(editable == false){ // only show their widget if editable is false
       if(selectedIndex >= 0){
         selectedWidget = items.get(selectedIndex).getWidgetForDropdown();
       } else if(items.size() > 0){
         selectedWidget = items.get(0).getWidgetForDropdown();
       }
     } else {
       String previousVal = editableTextBox.getText();
       String newVal = "";
 
 
 
       if(this.val != null){
         newVal = this.val;
       } else if(selectedIndex >= 0){
         newVal = items.get(selectedIndex).getValue().toString();
       } else if(items.size() > 0){
         newVal = items.get(0).getValue().toString();
       }
 
       if(previousVal.equals(newVal) == false){
         editableTextBox.setText(newVal);
       } 
       if(previousVal != null && previousVal.equals(newVal)){
         updateMade = false;
       }
       editableTextBox.setWidth("100%"); //$NON-NLS-1$
       editableTextBox.sinkEvents(Event.KEYEVENTS);
       editableTextBox.sinkEvents(Event.MOUSEEVENTS);
       selectedWidget = editableTextBox;
 
     }
     this.setTdStyles(selectedWidget.getElement());
    // selectedItemWrapper.getElement().getStyle().setProperty("overflow", "hidden"); //$NON-NLS-1$ //$NON-NLS-2$
     selectedItemWrapper.clear();
     selectedItemWrapper.add(selectedWidget);
     dropGrid.setWidget(0,0, selectedItemWrapper);
     if(editable && updateMade){
       editableTextBox.setFocus(true);
       editableTextBox.selectAll();
     }
   }
 
   /**
    * Called by updateUI when the list is not a drop-down (visible row count > 1)
    */
   private void updateList(){
     listPanel.clear();
     maxHeight = 0;
     maxWidth = 0;
 
     //actually going to average up the heights
     for(ListItem li : this.items){
       Widget w = li.getWidget();
 
       Rectangle rect = ElementUtils.getSize(w.getElement());
       // we only care about this if the user hasn't specified a height.
       if(height == null){
         maxHeight += rect.height;
       }
       maxWidth = Math.max(maxWidth,rect.width);
 
       // Add it to the dropdown
       listPanel.add(w);
       listPanel.setCellWidth(w, "100%"); //$NON-NLS-1$
     }
     if(height == null && this.items.size() > 0){
       maxHeight = Math.round(maxHeight / this.items.size());
     }
 
     // we only care about this if the user has specified a visible row count and no heihgt
     if(height == null){
      this.listScrollPanel.setHeight((this.visible * (maxHeight + spacing)) + "px"); //$NON-NLS-1$
     } else {
       this.listScrollPanel.setHeight(height);
     }
     if(width == null){
       this.fPanel.setWidth(maxWidth + 40 + "px"); //20 is scrollbar space //$NON-NLS-1$
     }
 
   }
 
   /**
    * Called by updateUI when the list is a drop-down (visible row count = 1)
    */
   private void updateDropDown(){
 
     // Update Shown selection in grid
     updateSelectedDropWidget();
 
     // Update popup panel,
     // Calculate the size of the largest list item.
     popupVbox.clear();
     maxWidth = 0;
     averageHeight = 0; // Actually used to set the width of the arrow
     popupHeight = null;
 
     int totalHeight = 0;
     for(ListItem li : this.items){
       Widget w = li.getWidget();
 
       Rectangle rect = ElementUtils.getSize(w.getElement());
       maxWidth = Math.max(maxWidth,rect.width);
       maxHeight = Math.max(maxHeight,rect.height);
       totalHeight += rect.height;
 
       // Add it to the dropdown
       popupVbox.add(w);
       popupVbox.setCellWidth(w, "100%"); //$NON-NLS-1$
     }
 
     // Average the height of the items
     if(items.size() > 0){
       averageHeight = Math.round(totalHeight / items.size());
     }
 
 
     // Set the size of the drop-down based on the largest list item
     if(width == null){
       //TODO: move "10" to a static member
       dropGrid.setWidth(maxWidth + (spacing*4) + maxHeight + "px"); //adding a little more room with the 10  //$NON-NLS-1$
       this.popupWidth = maxWidth + (spacing*4) + maxHeight + "px"; //$NON-NLS-1$
 
     } else if(width.equals("100%")){ //$NON-NLS-1$
       dropGrid.setWidth("100%"); //$NON-NLS-1$
       this.popupWidth = maxWidth + (spacing*4) + maxHeight + "px"; //$NON-NLS-1$
     } else {
       dropGrid.setWidth("100%"); //$NON-NLS-1$
       int w = -1;
       if(width.indexOf("px") > 0) { //$NON-NLS-1$
         w = Integer.parseInt(this.width.replace("px","")); //$NON-NLS-1$ //$NON-NLS-2$
       } else if(width.indexOf("%") > 0) { //$NON-NLS-1$
         w = Integer.parseInt(this.width.replace("%","")); //$NON-NLS-1$ //$NON-NLS-2$
       }
       selectedItemWrapper.setWidth( (w - (averageHeight + (this.spacing*6))) + "px" ); //$NON-NLS-1$
 
     }
 
 
     // Store the the size of the popup to respect MaxDropVisible now that we know the item height
     // This cannot be set here as the popup is not visible :(
 
     if(maxDropVisible > 0 && items.size() > maxDropVisible){
       // (Lesser of maxDropVisible or items size) * (Average item height + spacing value)
       this.popupHeight = (Math.min(this.maxDropVisible, this.items.size()) * (averageHeight + (this.spacing * 2) )) + "px"; //$NON-NLS-1$
     } else {
       this.popupHeight = null;//ElementUtils.getSize(popupVbox.getElement()).height+ "px"; //$NON-NLS-1$
     }
 
 
   }
 
   /**
    * Used internally to hide/show drop-down popup.
    */
   private void togglePopup(){
     if(popupShowing == false){
 
       // This delayed instantiation works around a problem with the underlying GWT widgets that
       // throw errors positioning when the GWT app is loaded in a frame that's not visible.
 
       if(popup == null){
         popup = new DropPopupPanel();
         popup.addPopupListener(this);
         popup.add(popupScrollPanel);
       }
 
       int x = this.getElement().getAbsoluteLeft();
       int y = this.getElement().getAbsoluteTop() + this.getElement().getOffsetHeight() + 1;
       int windowH = Window.getClientHeight();
       int windowW = Window.getClientWidth();
 
       Rectangle popupSize = ElementUtils.getSize(popup.getElement());
       if(y + popupSize.height > windowH){
         y = windowH - popupSize.height;
       }
       if(x + popupSize.width > windowW){
         x = windowW - popupSize.width;
       }
 
       popup.setPopupPosition(x, y);
 
       popup.show();
 
       // Set the size of the popup calculated in updateDropDown().
       if(this.popupHeight != null){
         this.popupScrollPanel.getElement().getStyle().setProperty("height", this.popupHeight); //$NON-NLS-1$
       }
 
       if(this.popupWidth != null){
         String w = Math.max(this.getElement().getOffsetWidth()-2, this.maxWidth+10) + "px";
         this.popupScrollPanel.getElement().getStyle().setProperty("width", w); //$NON-NLS-1$ //$NON-NLS-2$
         popup.getElement().getStyle().setProperty("width", w);
       }
 
       scrollSelectedItemIntoView();
 
       popupShowing = true;
     } else {
       popup.hide();
       //fPanel.setFocus(true);
     }
   }
 
   private void scrollSelectedItemIntoView(){
 
     // Scroll to view currently selected widget
     //DOM.scrollIntoView(this.getSelectedItem().getWidget().getElement());
     // Side effect of the previous call scrolls the scrollpanel to the right. Compensate here
     //popupScrollPanel.setHorizontalScrollPosition(0);
 
     if(this.visible > 1){
       this.listScrollPanel.ensureVisible(items.get(selectedIndex).getWidget());
       return;
     }
 
     // if the position of the selected item is greater than the height of the scroll area plus it's scroll offset
     if( ((this.selectedIndex + 1) * this.averageHeight) > popupScrollPanel.getOffsetHeight() + popupScrollPanel.getScrollPosition()){
       popupScrollPanel.setScrollPosition( (((this.selectedIndex ) * this.averageHeight) - popupScrollPanel.getOffsetHeight()) + averageHeight );
       return;
     }
 
     // if the position of the selected item is Less than the scroll offset
     if( ((this.selectedIndex) * this.averageHeight) < popupScrollPanel.getScrollPosition()){
       popupScrollPanel.setScrollPosition( ((this.selectedIndex ) * this.averageHeight));
     }
   }
 
   /**
    * Selects the given ListItem in the list.
    *
    * @param item ListItem to be selected.
    */
   public void setSelectedItem(ListItem item){
     if(items.contains(item) == false){
       throw new RuntimeException("Item not in collection"); //$NON-NLS-1$
     }
     // Clear previously selected item
     if(selectedIndex > -1){
       items.get(selectedIndex).onDeselect();
     }
     if(visible == 1){ // Drop-down mode
       if(popupShowing) {
         togglePopup();
       }
     }
     setSelectedIndex(items.indexOf(item));
 
   }
 
   private List<ListItem> selectedItems = new ArrayList<ListItem>();
   private void handleSelection(ListItem item, Event evt){
     if(!evt.getCtrlKey() && !evt.getShiftKey() && !evt.getMetaKey()){
       for(ListItem itm : selectedItems){
         itm.onDeselect();
       }
       if(selectedIndex > -1){
         items.get(selectedIndex).onDeselect();
       }
       selectedItems.clear();
       item.onSelect();
       if(!selectedItems.contains(item)){
         selectedItems.add(item);
       }
       int prevIdx = selectedIndex;
       selectedIndex = items.indexOf(item);
 
       scrollSelectedItemIntoView();
     } else if(evt.getShiftKey()){
       int idxOfNewSelection = items.indexOf(item);
       int startIdx = Math.min(selectedIndex, idxOfNewSelection);
       int endIndex = Math.max(selectedIndex, idxOfNewSelection);
 
       for(int i=startIdx; i<=endIndex; i++){
         if(!selectedItems.contains(items.get(i))){
           selectedItems.add(items.get(i));
         }
         items.get(i).onSelect();
       }
 
     } else if(evt.getCtrlKey() || evt.getMetaKey()){
       if(selectedItems.remove(item)){
         item.onDeselect();
       } else {
         item.onSelect();
 
         if(!selectedItems.contains(item)){
           selectedItems.add(item);
         }
       }
     } else {
       if(!selectedItems.contains(item)){
         selectedItems.add(item);
       }
     }
 
     if(this.suppressLayout == false){
       for(ChangeListener l : listeners){
         l.onChange(this);
       }
     }
 
   }
 
   /**
    * Selects the ListItem at the given index (zero-based)
    *
    * @param idx index of ListItem to select
    */
   public void setSelectedIndex(int idx){
     if(idx > items.size()){
       throw new RuntimeException("Index out of bounds: "+ idx); //$NON-NLS-1$
     }
     // De-Select the current
     if(selectedIndex > -1 ){
       items.get(selectedIndex).onDeselect();
     }
 
     selectedItems.clear();
     if(idx >= 0 && items.size() > idx){
       selectedItems.add(items.get(idx));
     }
 
 
     int prevIdx = selectedIndex;
     if(idx >= 0){
       selectedIndex = idx;
       items.get(idx).onSelect();
 
       this.val = null;
       if(visible == 1 && this.isAttached()){
         scrollSelectedItemIntoView();
       }
       updateSelectedDropWidget();
     }
 
     if(this.suppressLayout == false && prevIdx != idx){
       for(ChangeListener l : listeners){
         l.onChange(this);
       }
     }
   }
 
   public void setSelectedIndices(int[] indices){
     if(multiSelect == false){
 //      throw new IllegalStateException("Cannot select more than one item in a combobox");
       if(indices.length > 0){
         setSelectedIndex(indices[0]);
       }
       return;
     }
     for(ListItem item : selectedItems){
       item.onDeselect();
     }
     selectedItems.clear();
     for(int i=0; i<indices.length; i++){
       int idx = indices[i];
       if(idx >= 0 && idx < items.size()){
         items.get(idx).onSelect();
         selectedItems.add(items.get(idx));
       }
     }
 
     if(this.suppressLayout == false){
       for(ChangeListener l : listeners){
         l.onChange(this);
       }
     }
   }
 
   /**
    * Registers a ChangeListener with the list.
    *
    * @param listener ChangeListner
    */
   public void addChangeListener(ChangeListener listener){
     listeners.add(listener);
   }
 
   /**
    * Removes to given ChangeListener from list.
    *
    * @param listener ChangeListener
    */
   public void removeChangeListener(ChangeListener listener){
     this.listeners.remove(listener);
   }
 
   /**
    * Returns the selected index of the list (zero-based)
    *
    * @return Integer index
    */
   public int getSelectedIndex(){
     return selectedIndex;
   }
 
   /**
    * Returns the number of listitems in the box
    *
    * @return number of children
    */
   public int getSize(){
     return this.items.size();
   }
 
   /**
    * Returns the currently selected item
    *
    * @return currently selected Item
    */
   public ListItem getSelectedItem(){
     if(selectedIndex < 0 || selectedIndex > items.size()){
       return null;
     }
 
     return items.get(selectedIndex);
   }
 
   public List<ListItem> getSelectedItems(){
     return new ArrayList<ListItem>(selectedItems);
   }
 
   public int[] getSelectedIndices(){
     int[] selectedIndices = new int[selectedItems.size()];
     for(int i=0; i<selectedItems.size(); i++){
       selectedIndices[i] = items.indexOf(selectedItems.get(i));
     }
     return selectedIndices;
   }
 
 
   @Override
   public void setStylePrimaryName(String s) {
     super.setStylePrimaryName(s);
     this.primaryStyleName = s;
 
     // This may have came in late. Update ListItems
     for(ListItem item : items){
       item.setStylePrimaryName(s);
     }
   }
 
   @Override
   protected void onAttach() {
     super.onAttach();
     updateUI();
   }
 
   @Override
   /**
    * Calling setHeight will implecitly change the list from a drop-down style to a list style.
    */
   public void setHeight(String s) {
     this.height = s;
     // user has specified height, focusPanel needs to be 100%;
     this.fPanel.setHeight(s);
     if(visible == 1){
       this.setVisibleRowCount(15);
     }
     super.setHeight(s);
   }
 
   @Override
   public void setWidth(String s) {
     fPanel.setWidth(s);
     this.listScrollPanel.setWidth("100%"); //$NON-NLS-1$
     this.width = s;
     this.popupWidth = s;
     if(s != null){
       dropGrid.setWidth("100%");  //$NON-NLS-1$
     }
     super.setWidth(s);
   }
 
   // ======================================= Listener methods ===================================== //
 
   public void onPopupClosed(PopupPanel popupPanel, boolean b) {
     this.popupShowing = false;
   }
 
   public void onMouseDown(Widget widget, int i, int i1) {}
 
   public void onMouseEnter(Widget widget) {}
 
   public void onMouseLeave(Widget widget) {}
 
   public void onMouseMove(Widget widget, int i, int i1) {}
 
   public void onMouseUp(Widget widget, int i, int i1) {
     if(isEnabled() == false){
       return;
     }
     if(visible == 1){ //drop-down mode
       this.togglePopup();
     }
   }
 
   public void onFocus(Widget widget) {
     if(isEnabled() == false){
       return;
     }
      //fPanel.setFocus(true);
   }
 
   public void onLostFocus(Widget widget) {}
 
   private int shiftOriginIdx = -1;
   public void onKeyDown(Widget widget, char c, int i) {
     if(c == 16){ //shift
       shiftOriginIdx = selectedIndex;
     }
   }
 
   public void onKeyPress(Widget widget, char c, int i) {}
 
   public void onKeyUp(Widget widget, char c, int i) {
     if(isEnabled() == false){
       return;
     }
     if(c == 16){
       shiftOriginIdx = -1;
     }
     boolean fireEvents = false;
     switch(c){
       case 38: // UP
         if(selectedIndex > 0){
 
           if(multiSelect && !Event.getCurrentEvent().getShiftKey()){
             for(ListItem itm : selectedItems){
               itm.onDeselect();
             }
             selectedItems.clear();
             ListItem itm = items.get(selectedIndex - 1);
             selectedIndex = selectedIndex - 1;
             itm.onSelect();
             if(!selectedItems.contains(itm)){
               selectedItems.add(itm);
             }
             fireEvents = true;
             this.listScrollPanel.ensureVisible(itm.getWidget());
           } else if(multiSelect && Event.getCurrentEvent().getShiftKey()){
 
 
               ListItem itm = items.get(selectedIndex - 1);
 
               if(!selectedItems.contains(itm)){
                 selectedItems.add(itm);
               }
               itm.onSelect();
               this.listScrollPanel.ensureVisible(itm.getWidget());
 
               ListItem prevItem = items.get(selectedIndex);
               if(selectedIndex != shiftOriginIdx && shiftOriginIdx < selectedIndex && selectedItems.contains(prevItem)){
                 selectedItems.remove(prevItem);
                 prevItem.onDeselect();
               }
 
               selectedIndex = selectedIndex - 1;
 
               fireEvents = true;
             } else {
               setSelectedIndex(selectedIndex - 1);
               scrollSelectedItemIntoView();
             }
           }
 
 
         break;
       case 40: // Down
         if(selectedIndex < items.size() -1){
           if(multiSelect && !Event.getCurrentEvent().getShiftKey()){
             for(ListItem itm : selectedItems){
               itm.onDeselect();
             }
             selectedItems.clear();
             ListItem itm = items.get(selectedIndex + 1);
             selectedIndex = selectedIndex + 1;
             itm.onSelect();
             if(!selectedItems.contains(itm)){
               selectedItems.add(itm);
             }
             fireEvents = true;
             this.listScrollPanel.ensureVisible(itm.getWidget());
           } else if(multiSelect && Event.getCurrentEvent().getShiftKey()){
 
             ListItem itm = items.get(selectedIndex + 1);
 
             if(!selectedItems.contains(itm)){
               selectedItems.add(itm);
             }
             itm.onSelect();
 
             ListItem prevItem = items.get(selectedIndex);
             if(selectedIndex != shiftOriginIdx && shiftOriginIdx > selectedIndex && selectedItems.contains(prevItem)){
               selectedItems.remove(prevItem);
               prevItem.onDeselect();
             }
 
             this.listScrollPanel.ensureVisible(itm.getWidget());
             
             selectedIndex = selectedIndex + 1;
 
             fireEvents = true;
           } else {
             setSelectedIndex(selectedIndex + 1);
             scrollSelectedItemIntoView();
           }
         }
         break;
       case 27: // ESC
       case 13: // Enter
         if(popupShowing){
           togglePopup();
         }
         break;
       case 65: // A
         if(Event.getCurrentEvent().getCtrlKey()){
           for(ListItem item : items){
             item.onSelect();
           }
           selectedItems.clear();
           selectedItems.addAll(items);
 
           fireEvents = true;
         }
         break;
 
     }
     if(fireEvents && this.suppressLayout == false){
       for(ChangeListener l : listeners){
         l.onChange(this);
       }
     }
   }
 
   public void setCommand(Command command) {
     this.command = command;
   }
   // ====================================== Listener Implementations =========================== //
 
   public void itemSelected(ListItem listItem, Event event) {
     fPanel.setFocus(true);
     if(multiSelect){
       handleSelection(listItem, event);
     } else {
       setSelectedItem(listItem);
     }
   }
 
   public void doAction(ListItem listItem) {
     if(command != null){
       command.execute();
     }
   }
 
 
   // ======================================= Inner Classes ===================================== //
 
   /**
    * Panel used as a drop-down popup.
    */
   private class DropPopupPanel extends PopupPanel {
     public DropPopupPanel(){
       super(true);
       setStyleName("drop-popup"); //$NON-NLS-1$
     }
 
     @Override
     public boolean onEventPreview(Event event) {
       if(DOM.isOrHasChild(CustomListBox.this.getElement(), DOM.eventGetTarget(event))){
 
         return true;
       }
       return super.onEventPreview(event);
     }
   }
 
   /**
    * Panel contained in the popup
    */
   private class PopupList extends VerticalPanel{
 
     public PopupList(){
       this.sinkEvents(Event.MOUSEEVENTS);
     }
 
     @Override
     public void onBrowserEvent(Event event) {
       super.onBrowserEvent(event);
     }
   }
 
   /**
    * This is the arrow rendered in the drop-down.
    */
   private class DropDownArrow extends SimplePanel{
     private SimplePanel img;
     private boolean enabled = true;
     public DropDownArrow(){
       img = new SimplePanel();
 
       this.setStylePrimaryName("combo-arrow"); //$NON-NLS-1$
       super.add(img);
       ElementUtils.preventTextSelection(this.getElement());
     }
     public void setEnabled(boolean enabled){
       if(this.enabled == enabled){
         return;
       }
       this.enabled = enabled;
       if(enabled){
         this.setStylePrimaryName("combo-arrow"); //$NON-NLS-1$
       } else {
         this.setStylePrimaryName("combo-arrow-disabled"); //$NON-NLS-1$
       }
 
     }
   }
 
   /**
    * Setting editable to true allows the user to specify their own value for the combobox.
    *
    * @param editable
    */
   public void setEditable(boolean editable){
     this.editable = editable;
     this.updateUI();
   }
 
   public boolean isEditable(){
     return this.editable;
   }
 
   /**
    * Returns the user-entered value in the case of an editable drop-down
    *
    * @return Value user has entered
    */
   public String getValue(){
     if(!editable){
       return null;
     } else {
       return (editableTextBox != null)
         ? editableTextBox.getText()
         : null;
     }
   }
 
   public void setValue(String text){
     this.val = text;
     if(editable){
       editableTextBox.setText(text);
       selectedIndex = -1;
       this.onChange(editableTextBox);
     }
   }
 
   public void onChange(Widget sender) {
     for(ChangeListener l : listeners){
       l.onChange(this);
     }
   }
 
   public void setEnabled(boolean enabled){
     this.enabled = enabled;
     if (editableTextBox != null) {
       editableTextBox.setEnabled(enabled);
     }
     arrow.setEnabled(enabled);
     this.setStylePrimaryName((this.enabled) ? "custom-list" : "custom-list-disabled"); //$NON-NLS-1$ //$NON-NLS-2$
   }
 
   public boolean isEnabled(){
     return this.enabled;
   }
 
   public Widget createProxy(){
     DefaultListItem item = new DefaultListItem();
     item.setText(getSelectedItem().getText());
     return item;
   }
 
   public void setDragController(DragController controller){
     dragController = controller;
     if(this.items.size() > 0){
       for(ListItem item : items){
         dragController.makeDraggable(item.getWidget());
       }
     }
   }
 
   public boolean isMultiSelect() {
     return multiSelect;
   }
 
   public void setMultiSelect(boolean multiSelect) {
     this.multiSelect = multiSelect;
   }
 }
