 package com.goodow.web.reader.client;
 
 import com.goodow.web.core.client.ScrollView;
 import com.goodow.web.core.shared.EntryViewer;
 import com.goodow.web.core.shared.Receiver;
 import com.goodow.web.core.shared.WebPlaceManager;
 import com.goodow.web.reader.shared.AsyncBookService;
 import com.goodow.web.reader.shared.Book;
 
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.ui.HTMLPanel;
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 
 import com.googlecode.mgwt.dom.client.event.tap.TapEvent;
 import com.googlecode.mgwt.dom.client.event.tap.TapHandler;
 import com.googlecode.mgwt.ui.client.widget.event.scroll.BeforeScrollEndEvent;
 import com.googlecode.mgwt.ui.client.widget.event.scroll.BeforeScrollStartEvent;
 import com.googlecode.mgwt.ui.client.widget.touch.TouchPanel;
 
 import java.util.List;
 
 public abstract class AbstractBookList extends ScrollView implements Receiver<List<Book>> {
 
   TouchPanel container;
 
   HTMLPanel bookList;
 
   @Inject
   AsyncBookService bookService;
   @Inject
   Provider<BookSummary> bookSummaryProvider;
   @Inject
   WebPlaceManager placeManager;
 
   private boolean conflit = false;
 
   @Override
   public void onSuccess(final List<Book> result) {
     bookList.clear();
     for (final Book book : result) {
       BookSummary view = bookSummaryProvider.get();
       view.setLandscape(setBookSummaryLandscapeCss());
       view.setBook(book);
       view.addDomHandler(new ClickHandler() {
         @Override
         public void onClick(ClickEvent event) {
           if (conflit) {
             return;
           }
           placeManager.goTo(book, EntryViewer.BOOKDETAIL);
         }
       }, ClickEvent.getType());
       bookList.add(view);
     }
 
   }
 
   @Override
   public void refresh() {
     bookService.getMyBooks().fire(this);
   }
 
   public String setBookSummaryLandscapeCss() {
     return BookSummary.INSTANCE.landscape().root();
   }
 
   @Override
   protected void start() {
     container = new TouchPanel();
     bookList = new HTMLPanel("");
    main.addBeforeScrollStartHandler(new BeforeScrollStartEvent.Handler() {
 
       @Override
      public void onBeforeScrollStart(BeforeScrollStartEvent event) {
         conflit = true;
       }
     });
    main.addBeforeScrollEndHandler(new BeforeScrollEndEvent.Handler() {
 
       @Override
      public void onBeforeScrollStart(BeforeScrollEndEvent event) {
         conflit = false;
       }
     });
 
     container.add(bookList);
     main.add(container);
     main.addStyleName(BooksBrowser.bundle.booksAppCss().main());
 
     new Timer() {
       @Override
       public void run() {
         refresh();
       }
     }.schedule(1);
   }
 }
