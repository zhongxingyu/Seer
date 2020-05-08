 /*
  * SoftButton.java
  *
  * Created on July 18, 2007, 7:16 PM
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package com.totsp.gwittir.client.ui;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.ui.ClickListener;
 import com.google.gwt.user.client.ui.ClickListenerCollection;
 import com.google.gwt.user.client.ui.FocusListener;
 import com.google.gwt.user.client.ui.FocusPanel;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.KeyboardListener;
 import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.MouseListener;
 import com.google.gwt.user.client.ui.MouseListenerAdapter;
 import com.google.gwt.user.client.ui.MouseWheelListener;
 import com.google.gwt.user.client.ui.SourcesMouseEvents;
 import com.google.gwt.user.client.ui.SourcesMouseWheelEvents;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * This is a button widget implemented entirely without the native
  * button widget. This avoids Z-index overlay problems, and provides
  * enhanced functionality.
  *
  * <p>Base CSS class: gwittir-SoftButton</p>
  * <p>Extended CSS Classes:
  *    <ul>
  *        <li>disabled</li>
  *        <li>focussed</li>
  *        <li>pressed</li>
  *        <li>hover</li>
  *    </ul>
  * </p>
  * <p>Example styling:
  *    <code><pre>
  * .gwittir-SoftButton.disabled {
  *    border-style: inset;
  *    border-width: 2px;
  *    color: gray;
  *    background-color: white;
  * }
  * </pre></code></p>
  * <p>This is implemented with a base CSS style of table, so table based
  * CSS attributes like <code>vertical-align</code> may be applied.</p>
  * @author <a href="mailto:cooper@screaming-penguin.com">Robert "kebernet" Cooper</a>
  */
 public class SoftButton extends Button implements SourcesMouseEvents,
         SourcesMouseWheelEvents {
     private ClickListener listener;
     private FocusListener focus;
     private boolean focused;
     private FocusPanel softBase;
     private Grid grid;
     private MouseListener hover;
     private Widget content;
     private boolean enabled;
     private String baseStyleName;
     private ClickListenerCollection clickers;
     private long lastClick = 0;
     /**
      * Creates a new instance with an empty text value.
      */
     public SoftButton() {
         super();
     }
     
     /**
      * Creates a new instance.
      * @param label String value containing the text to apply in Label format.
      */
     public SoftButton(String label) {
         super(label);
     }
     
     /**
      *
      */
     public void addClickListener(ClickListener listener) {
         this.clickers.add(listener);
     }
     
     public void addFocusListener(FocusListener listener) {
         this.softBase.addFocusListener(listener);
     }
     
     public void addKeyboardListener(KeyboardListener listener) {
         this.softBase.addKeyboardListener(listener);
     }
     
     public void addMouseListener(MouseListener listener) {
         this.softBase.addMouseListener(listener);
     }
     
     public void addMouseWheelListener(MouseWheelListener listener) {
         this.softBase.addMouseWheelListener(listener);
     }
     
     public void addStyleName(String style) {
         this.grid.addStyleName(style);
     }
     
     public int getAbsoluteLeft() {
         int retValue;
         
         retValue = this.grid.getAbsoluteLeft();
         
         return retValue;
     }
     
     /**
      *
      */
     public int getAbsoluteTop() {
         int retValue;
         
         retValue = this.grid.getAbsoluteTop();
         
         return retValue;
     }
     
     /**
      * Returns the widget that composes the internals of the button.
      * @return
      */
     public Widget getContent() {
         return this.content;
     }
     
     public String getHTML() {
         if(this.content instanceof HTML) {
             return ((HTML) this.content).getHTML();
         } else {
             return ((Label) this.content).getText();
         }
     }
     
     public int getOffsetHeight() {
         int retValue;
         
         retValue = this.grid.getOffsetHeight();
         
         return retValue;
     }
     
     public int getOffsetWidth() {
         int retValue;
         
         retValue = this.grid.getOffsetWidth();
         
         return retValue;
     }
     
     public String getStyleName() {
         String retValue;
         retValue = this.grid.getStyleName();
         
         return retValue;
     }
     
     public int getTabIndex() {
         int retValue;
         
         retValue = this.softBase.getTabIndex();
         
         return retValue;
     }
     
     public String getText() {
         if(this.content instanceof HTML) {
             return ((HTML) this.content).getHTML();
         } else if(this.content instanceof Label) {
             return ((Label) this.content).getText();
         } else {
             return this.content.toString();
         }
     }
     
     public String getTitle() {
         String retValue;
         
         retValue = this.softBase.getTitle();
         
         return retValue;
     }
     
     public int hashCode() {
         int retValue;
         
         retValue = this.softBase.hashCode();
         
         return retValue;
     }
     
     protected void init() {
        this.setBaseStyleName("gwittir-SoftButton");
         this.clickers = new ClickListenerCollection();
         this.softBase = new FocusPanel();
         this.grid = new Grid(1, 1);
         DOM.setStyleAttribute(this.softBase.getElement(), "display", "inline");
         this.setContent(new Label());
         this.softBase.setWidget(grid);
         
         final SoftButton instance = this;
         listener = new ClickListener() {
             public void onClick(Widget sender) {
                 //GWT.log("Clicked " + getAction(), null);
                 long clickTime = System.currentTimeMillis();
                 if( clickTime - lastClick >= 100){
                     lastClick = clickTime;
                     clickers.fireClick( instance );
                     if(enabled && (getAction() != null)) {
                         getAction().execute(instance);
                     }
                 }
             }
         };
         this.softBase.addClickListener(listener);
         this.focus = new FocusListener() {
             public void onLostFocus(Widget sender) {
                 focused = false;
                 if( enabled ){
                     setStyleName( getBaseStyleName());
                 }
                 
             }
             
             public void onFocus(Widget sender) {
                 focused = true;
                 if(enabled && !getStyleName().equals( getBaseStyleName()+"-pressed")){
                     setStyleName(getBaseStyleName()+"-focused");
                     
                 }
             }
         };
         this.addFocusListener(this.focus);
         this.hover = new MouseListenerAdapter() {
             public void onMouseUp(Widget sender, int x, int y) {
                 if( enabled ){
                     setStyleName( getBaseStyleName()+"-focused" );
                 }
             }
             
             public void onMouseDown(Widget sender, int x, int y) {
                 //GWT.log("Press", null);
                 if(enabled){
                     setStyleName( getBaseStyleName()+"-pressed");
                 }
             }
             
             public void onMouseLeave(Widget sender) {
                 if(enabled){
                     if( focused ){
                         setStyleName( getBaseStyleName()+"-focused" );
                     } else {
                         setStyleName( getBaseStyleName());
                     }
                 }
             }
             
             public void onMouseEnter(Widget sender) {
                 if(enabled){
                     setStyleName(getBaseStyleName()+"-hover");
                 }
             }
         };
         this.softBase.addMouseListener(hover);
         this.softBase.addKeyboardListener(new KeyboardListenerAdapter() {
             public void onKeyPress(Widget sender, char keyCode,
                     int modifiers) {
                 if((keyCode == ' ')
                 || (keyCode == KeyboardListener.KEY_ENTER)) {
                     if(enabled && (getAction() != null)) {
                         listener.onClick( instance );
                         setStyleName( getBaseStyleName()+"-focused" );
                
                     }
                 }
                 
                 
             }
             
             public void onKeyUp(Widget sender, char keyCode, int modifiers) {
                 if( enabled ){
                     setStyleName( getBaseStyleName()+"-focused" );
                 }
             }
             
             public void onKeyDown(Widget sender, char keyCode, int modifiers) {
                 if(enabled){
                     setStyleName(getBaseStyleName()+"-pressed");
                 }
             }
         });
         this.setRenderer(new ToStringRenderer());
         this.initWidget(this.softBase);
        this.setStyleName(getBaseStyleName());
         this.setEnabled(true);
     }
     
     public boolean isEnabled() {
         return this.enabled;
     }
     
     public void removeClickListener(ClickListener listener) {
         this.clickers.remove(listener);
     }
     
     public void removeFocusListener(FocusListener listener) {
         this.softBase.removeFocusListener(listener);
     }
     
     public void removeKeyboardListener(KeyboardListener listener) {
         this.softBase.removeKeyboardListener(listener);
     }
     
     public void removeMouseListener(MouseListener listener) {
         this.softBase.removeMouseListener(listener);
     }
     
     public void removeMouseWheelListener(MouseWheelListener listener) {
         this.softBase.removeMouseWheelListener(listener);
     }
     
     public void removeStyleName(String style) {
         this.grid.removeStyleName(style);
     }
     
     public void setAccessKey(char key) {
         this.softBase.setAccessKey(key);
     }
     
     /**
      * Sets the internals of the button to the specified widget.
      *
      * <p>This can be used to provide icons or other non-standard elements
      * for the button.</p>
      * @param w Widget to place inside the button.
      */
     public void setContent(Widget w) {
         //for(Iterator it = this.styleNames.iterator(); it.hasNext(); ){
         //    w.addStyleName( (String) it.next() );
         //}
         this.content = w;
         //GWT.log("Setting Content: " + w.toString(), null);
         this.grid.setWidget(0, 0, this.content);
     }
     
     public void setEnabled(boolean enabled) {
         this.enabled = enabled;
         
         if(!this.enabled) {
             this.setStyleName(this.getBaseStyleName()+"-disabled");
         } else {
             this.setStyleName(this.getBaseStyleName());
         }
     }
     
    
     public void setFocus(boolean focused) {
         this.softBase.setFocus(focused);
     }
     
     public void setHTML(String html) {
         if(this.content instanceof HTML) {
             ((HTML) this.content).setHTML(html);
         } else {
             this.setContent(new HTML(html));
         }
         
         Object old = this.getValue();
         this.setText((this.getRenderer() != null)
         ? this.getRenderer().render(html) : ("" + html));
     }
     
     public void setPixelSize(int width, int height) {
         this.grid.setPixelSize(width, height);
     }
     
     public void setSize(String width, String height) {
         this.grid.setSize(width, height);
     }
     
     public void setStyleName(String style) {
         GWT.log( style, null );
         this.grid.setStyleName(style);
     }
     
     public void setTabIndex(int index) {
         this.softBase.setTabIndex(index);
     }
     
     public void setText(String text) {
         //GWT.log("Setting text " + text, null);
         
         if(this.content instanceof Label) {
             //GWT.log("Label text " + text, null);
             ((Label) this.content).setText(text);
         } else {
             //GWT.log("New Label text " + text, null);
             this.setContent(new Label(text));
         }
     }
     
     public void setTitle(String title) {
         this.softBase.setTitle(title);
     }
     
     public void setWidth(String width) {
         this.grid.setWidth(width);
     }
     
     public void setHeight(String height) {
         this.grid.setHeight(height);
     }
 
     public String getBaseStyleName() {
         return baseStyleName;
     }
 
     public void setBaseStyleName(String baseStyleName) {
         this.baseStyleName = baseStyleName;
         this.setStyleName( baseStyleName );
     }
     
 }
