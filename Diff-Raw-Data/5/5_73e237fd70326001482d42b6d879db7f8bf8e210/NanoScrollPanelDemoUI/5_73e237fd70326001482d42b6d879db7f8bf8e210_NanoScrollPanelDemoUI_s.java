 package org.vaadin.hhe.nanoscrollpanel.demo;
 
 import org.vaadin.hhe.nanoscrollpanel.NanoScrollPanel;
 import org.vaadin.hhe.nanoscrollpanel.NanoScrollPanel.NanoScrollEvent;
 import org.vaadin.hhe.nanoscrollpanel.NanoScrollPanel.NanoScrollPanelListener;
 
 import com.vaadin.server.VaadinRequest;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Button.ClickListener;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.Notification;
 import com.vaadin.ui.Panel;
 import com.vaadin.ui.UI;
 import com.vaadin.ui.VerticalLayout;
 
 public class NanoScrollPanelDemoUI extends UI {
 
     private static final long serialVersionUID = 7992657815796718844L;
     
     private Component scrollToTarget = null;
     
     @SuppressWarnings("serial")
     @Override
     protected void init(VaadinRequest request) {
         final NanoScrollPanel nPanel = new NanoScrollPanel();
         nPanel.setWidth("400px");
         nPanel.setHeight("400px");
         // flash user there are more content
         nPanel.flashScrollbar();
         nPanel.setPreventPageScrolling(true);
         nPanel.addNanoScrollListener(new NanoScrollPanelListener() {
             @Override
             public void onScroll(NanoScrollEvent event) {
                 Notification.show("NanoScrollEvent catched",
                         "Event type is "+event.getType(),
                         Notification.Type.HUMANIZED_MESSAGE);
             }
         });
         nPanel.addClickListener(new com.vaadin.event.MouseEvents.ClickListener() {
             @Override
             public void click(com.vaadin.event.MouseEvents.ClickEvent event) {
                 if(event.isDoubleClick()) {
                     Notification.show("NanoScrollPanel was double clicked", "Event",
                             Notification.Type.HUMANIZED_MESSAGE);
                 } else {
                     Notification.show("NanoScrollPanel was clicked", "Event",
                             Notification.Type.HUMANIZED_MESSAGE);
                 }
             }
         });
         
         final VerticalLayout vLayout = new VerticalLayout();
         for(int i=0; i<50; ++i) {
             Label l = new Label("This is a example of NanoScrollPanel "+i+".");
             l.setId("Label"+i);
             if(i==25) scrollToTarget = l;
             vLayout.addComponent(l);
         }
         
         Button btn = new Button("Add more");
         vLayout.addComponent(btn);
         btn.addClickListener(new ClickListener() {
             @Override
             public void buttonClick(ClickEvent event) {
                 vLayout.addComponent(new Label("This is more and more test."));
             }
         });
         
         Button btn2 = new Button("Remove one");
         vLayout.addComponent(btn2);
         btn2.addClickListener(new ClickListener() {
             @Override
             public void buttonClick(ClickEvent event) {
                 if(vLayout.getComponentCount()>54)
                     vLayout.removeComponent(vLayout.getComponent(vLayout.getComponentCount()-1));
             }
         });
         
         Button btn3 = new Button("Shrink");
         vLayout.addComponent(btn3);
         btn3.addClickListener(new ClickListener() {
             @Override
             public void buttonClick(ClickEvent event) {
                 nPanel.setHeight(nPanel.getHeight()*0.8f, nPanel.getHeightUnits());
             }
         });
         
         Button btn4 = new Button("Expand");
         vLayout.addComponent(btn4);
         btn4.addClickListener(new ClickListener() {
             @Override
             public void buttonClick(ClickEvent event) {
                 nPanel.setHeight(nPanel.getHeight()/0.8f, nPanel.getHeightUnits());
             }
         });
         
         nPanel.setContent(vLayout);
         
         VerticalLayout nanoScrollLayout = new VerticalLayout();
         nanoScrollLayout.addComponent(nPanel);
         
         Button flashBtn = new Button("Flash Scrollbar");
         flashBtn.addClickListener(new ClickListener() {
             @Override
             public void buttonClick(ClickEvent event) {
                 nPanel.flashScrollbar();
             }
         });
         nanoScrollLayout.addComponent(flashBtn);
         
         Button scrollTopBtn = new Button("Scroll To Top");
         scrollTopBtn.addClickListener(new ClickListener() {
             @Override
             public void buttonClick(ClickEvent event) {
                 nPanel.flashScrollbar();
                 nPanel.scrollToTop();
             }
         });
         nanoScrollLayout.addComponent(scrollTopBtn);
         
         Button scrollBottomBtn = new Button("Scroll To Bottom");
         scrollBottomBtn.addClickListener(new ClickListener() {
             @Override
             public void buttonClick(ClickEvent event) {
                 nPanel.flashScrollbar();
                 nPanel.scrollToBottom();
             }
         });
         nanoScrollLayout.addComponent(scrollBottomBtn);
         
         Button scrollToBtn = new Button("Scroll To 25");
         scrollToBtn.addClickListener(new ClickListener() {
             @Override
             public void buttonClick(ClickEvent event) {
                 nPanel.flashScrollbar();
                 nPanel.scrollTo(scrollToTarget);
             }
         });
         nanoScrollLayout.addComponent(scrollToBtn);
         
         
         HorizontalLayout hLayout = new HorizontalLayout();
         hLayout.addComponent(nanoScrollLayout);
         
         Panel normalPanel = new Panel();
         normalPanel.setWidth("400px");
         normalPanel.setHeight("400px");
         final VerticalLayout normalPanelContentLayout = new VerticalLayout();
         normalPanelContentLayout.setMargin(true);
         for(int i=0; i<50; ++i) {
            Label l = new Label("This is a example of Normal Panel "+i+".");
            l.setId("Label"+i);
            if(i==25) scrollToTarget = l;
            normalPanelContentLayout.addComponent(l);
         }
         normalPanel.setContent(normalPanelContentLayout);
         hLayout.addComponent(normalPanel);
         
         setContent(hLayout);
     }
 }
