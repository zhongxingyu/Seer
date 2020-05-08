 package com.goodow.web.reader.client;
 
 import com.goodow.web.core.client.WebView;
 import com.goodow.web.core.shared.EntryViewer;
 import com.goodow.web.core.shared.Receiver;
 import com.goodow.web.core.shared.WebPlaceManager;
 import com.goodow.web.reader.client.style.ReadResources;
 import com.goodow.web.reader.shared.AsyncBookService;
 import com.goodow.web.reader.shared.Book;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.ui.HTMLPanel;
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 
 import com.googlecode.mgwt.ui.client.widget.ScrollPanel;
 import com.googlecode.mgwt.ui.client.widget.event.scroll.BeforeScrollEndEvent;
 import com.googlecode.mgwt.ui.client.widget.event.scroll.BeforeScrollStartEvent;
import com.googlecode.mgwt.ui.client.widget.event.scroll.ScrollAnimationEndEvent;
import com.googlecode.mgwt.ui.client.widget.event.scroll.ScrollAnimationStartEvent;
 
 import java.util.List;
 
 public class CategorizedBookList extends WebView<HTMLPanel> implements Receiver<List<Book>> {
 
   HTMLPanel bookList;
 
   @Inject
   AsyncBookService bookService;
   @Inject
   Provider<BookSummary> bookSummaryProvider;
   @Inject
   WebPlaceManager placeManager;
 
   CategoryListView categoryListView;
 
   private boolean conflit = false;
 
   @Override
   public void onSuccess(final List<Book> result) {
     bookList.clear();
     for (final Book book : result) {
       BookSummary view = bookSummaryProvider.get();
       view.setLandscape(setBookSummaryLandscapeCss());
       view.addDomHandler(new ClickHandler() {
         @Override
         public void onClick(ClickEvent event) {
           if (conflit) {
             return;
           }
           placeManager.goTo(book, EntryViewer.BOOKDETAIL);
         }
       }, ClickEvent.getType());
       view.setBook(book);
       bookList.add(view);
     }
   }
 
   @Override
   public void refresh() {
     bookService.getMyBooks().fire(this);
   }
 
   public String setBookSummaryLandscapeCss() {
     return BookSummary.INSTANCE.landscape().root1();
   }
 
   @Override
   public void start() {
     bookList = new HTMLPanel("");
 
     categoryListView = new CategoryListView();
 
     main.addStyleName(ReadResources.INSTANCE().categroyListCss().categorContainer());
 
     ScrollPanel scrollPanel = new ScrollPanel();
     scrollPanel.setWidget(bookList);
     scrollPanel.setScrollingEnabledX(false);
     scrollPanel.setScrollingEnabledY(true);
    scrollPanel.addScrollAnimationStartHandler(new ScrollAnimationStartEvent.Handler() {
 
       @Override
      public void onScrollAnimationStart(ScrollAnimationStartEvent event) {
         conflit = true;
       }
     });
    scrollPanel.addScrollAnimationEndHandler(new ScrollAnimationEndEvent.Handler() {
 
       @Override
      public void onScrollAnimationEnd(ScrollAnimationEndEvent event) {
         conflit = false;
       }
     });
 
     main.add(categoryListView);
     main.add(scrollPanel);
 
     new Timer() {
       @Override
       public void run() {
         refresh();
       }
     }.schedule(1);
   }
 
   @Override
   protected HTMLPanel createRoot() {
     return new HTMLPanel("");
   }
 
 }
