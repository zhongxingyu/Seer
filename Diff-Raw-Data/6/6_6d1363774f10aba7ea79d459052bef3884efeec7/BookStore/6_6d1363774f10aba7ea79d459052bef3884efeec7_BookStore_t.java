 package com.goodow.web.reader.client;
 
 import com.goodow.web.core.shared.WebPlace;
 import com.goodow.web.reader.client.style.ReadResources;
 
 import com.google.gwt.dom.client.Style.Position;
 import com.google.gwt.event.logical.shared.SelectionEvent;
 import com.google.gwt.event.logical.shared.SelectionHandler;
 import com.google.gwt.place.shared.PlaceChangeEvent;
 import com.google.gwt.place.shared.PlaceController;
 import com.google.gwt.user.client.ui.AcceptsOneWidget;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.IsWidget;
 import com.google.gwt.user.client.ui.SimplePanel;
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 import com.google.web.bindery.event.shared.EventBus;
 
 import com.googlecode.mgwt.dom.client.event.tap.TapEvent;
 import com.googlecode.mgwt.dom.client.event.tap.TapHandler;
 import com.googlecode.mgwt.ui.client.MGWTStyle;
 import com.googlecode.mgwt.ui.client.theme.base.TabBarCss;
 import com.googlecode.mgwt.ui.client.widget.HeaderButton;
 import com.googlecode.mgwt.ui.client.widget.HeaderPanel;
 import com.googlecode.mgwt.ui.client.widget.LayoutPanel;
 import com.googlecode.mgwt.ui.client.widget.tabbar.TabBarButton;
 import com.googlecode.mgwt.ui.client.widget.tabbar.TabPanel;
 
 @Singleton
 public class BookStore extends Composite implements AcceptsOneWidget, PlaceChangeEvent.Handler {
 
   @Inject
   protected PlaceController placeController;
 
   private LayoutPanel main;
 
   protected HeaderPanel headerPanel;
   protected HeaderButton rightButton;
   protected HeaderButton leftButton;
   protected HTML title;
 
   protected FlowPanel tabPanel;
   protected SimplePanel tabContainer;
   protected TabPanel.TabBar tabBar;
 
   private final ReaderPlugin plugin;
 
   @Inject
   public BookStore(final EventBus eventBus, final ReaderPlugin plugin) {
     this.plugin = plugin;
     main = new LayoutPanel();
 
     headerPanel = new HeaderPanel();
     title = new HTML();
 
     headerPanel.setCenterWidget(title);
 
     rightButton = new HeaderButton();
     rightButton.setRoundButton(true);
     rightButton.setText("书架");
     rightButton.addTapHandler(new TapHandler() {
 
       @Override
       public void onTap(final TapEvent event) {
         placeController.goTo(plugin.bookshelfPlace);
       }
     });
 
     leftButton = new HeaderButton();
     leftButton.setRoundButton(true);
     leftButton.setText("设置");
 
     headerPanel.setLeftWidget(leftButton);
     headerPanel.setRightWidget(rightButton);
 
     eventBus.addHandler(PlaceChangeEvent.TYPE, this);
 
     tabPanel = new FlowPanel();
     TabBarCss css = MGWTStyle.getTheme().getMGWTClientBundle().getTabBarCss();
     tabPanel.addStyleName(css.tabPanel());
 
     tabContainer = new SimplePanel();
     tabContainer.addStyleName(css.tabPanelContainer());
     tabContainer.getElement().getStyle().setPosition(Position.RELATIVE);
 
     tabBar = new TabPanel.TabBar(css);
     tabBar.addSelectionHandler(new SelectionHandler<Integer>() {
       @Override
       public void onSelection(final SelectionEvent<Integer> event) {
         int index = event.getSelectedItem();
         WebPlace newPlace = plugin.bookstorePlace.getChild(index);
         placeController.goTo(newPlace);
       }
     });
 
     for (WebPlace place : plugin.bookstorePlace.getChildren()) {
       TabBarButton button = new TabBarButton(place.getButtonImage());
       button.setText(place.getButtonText());
       tabBar.add(button);
     }
 
     tabPanel.add(tabContainer);
     tabPanel.add(tabBar);
 
     main.add(headerPanel);
     main.add(tabPanel);
 
     headerPanel.addStyleName(ReadResources.INSTANCE().readHeadCss().headPanel());
 
     initWidget(main);
   }
 
   @Override
   public void onPlaceChange(final PlaceChangeEvent event) {
     refresh();
   }
 
   public void refresh() {
     WebPlace currentPlace = (WebPlace) placeController.getWhere();
     int index = plugin.bookstorePlace.getChildren().indexOf(currentPlace);
    if (index >= 0) {
      tabBar.setSelectedButton(index, true);
      title.setText(currentPlace.getTitle());
    }
   }
 
   @Override
   public void setWidget(final IsWidget w) {
     tabContainer.setWidget(w);
     w.asWidget().addStyleName(
         MGWTStyle.getTheme().getMGWTClientBundle().getLayoutCss().fillPanelExpandChild());
   }
 }
