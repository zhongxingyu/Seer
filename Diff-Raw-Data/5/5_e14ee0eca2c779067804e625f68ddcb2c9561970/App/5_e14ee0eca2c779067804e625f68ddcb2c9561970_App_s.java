 package rose.gwt.client;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.TreeMap;
 
 import rosa.search.SearchResult;
 import rosa.search.SearchResult.SearchMatch;
 import rosa.gwt.common.client.Searcher.UserField;
 import rosa.gwt.common.client.codexview.CodexController;
 import rosa.gwt.common.client.codexview.CodexImage;
 import rosa.gwt.common.client.codexview.CodexModel;
 import rosa.gwt.common.client.codexview.CodexOpening;
 import rosa.gwt.common.client.codexview.CodexView;
 import rosa.gwt.common.client.codexview.CodexView.Mode;
 import rosa.gwt.common.client.codexview.RoseBook;
 import rosa.gwt.common.client.codexview.SimpleCodexController;
 import rosa.gwt.common.client.data.Book;
 import rosa.gwt.common.client.data.CharacterNamesTable;
 import rosa.gwt.common.client.data.CollectionDataTable;
 import rosa.gwt.common.client.data.IllustrationTitlesTable;
 import rosa.gwt.common.client.data.ImageTagging;
 import rosa.gwt.common.client.data.NarrativeSectionsTable;
 import rosa.gwt.common.client.data.NarrativeTagging;
 import rosa.gwt.common.client.data.Repository;
 import rosa.gwt.common.client.dynimg.FsiImageServer;
 import rosa.gwt.common.client.dynimg.ImageServer;
 import rosa.gwt.common.client.resource.Labels;
 
 import com.google.gwt.cell.client.AbstractCell;
 import com.google.gwt.cell.client.Cell;
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.core.client.GWT.UncaughtExceptionHandler;
 import com.google.gwt.core.client.Scheduler;
 import com.google.gwt.core.client.Scheduler.ScheduledCommand;
 import com.google.gwt.dom.client.Style.Unit;
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyDownEvent;
 import com.google.gwt.event.dom.client.KeyDownHandler;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.http.client.URL;
 import com.google.gwt.i18n.client.LocaleInfo;
 import com.google.gwt.resources.client.ExternalTextResource;
 import com.google.gwt.resources.client.ResourceCallback;
 import com.google.gwt.resources.client.ResourceException;
 import com.google.gwt.resources.client.TextResource;
 import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
 import com.google.gwt.safehtml.shared.SafeHtmlUtils;
 import com.google.gwt.user.cellview.client.CellList;
 import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Anchor;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.CheckBox;
 import com.google.gwt.user.client.ui.ComplexPanel;
 import com.google.gwt.user.client.ui.DecoratedPopupPanel;
 import com.google.gwt.user.client.ui.DialogBox;
 import com.google.gwt.user.client.ui.DockLayoutPanel;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HasVerticalAlignment;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Hyperlink;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.InsertPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.Panel;
 import com.google.gwt.user.client.ui.RootLayoutPanel;
 import com.google.gwt.user.client.ui.ScrollPanel;
 import com.google.gwt.user.client.ui.SplitLayoutPanel;
 import com.google.gwt.user.client.ui.TabLayoutPanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.view.client.SelectionChangeEvent;
 import com.google.gwt.view.client.SingleSelectionModel;
 
 import rosa.gwt.common.client.*;
 
 
 public class App implements EntryPoint {
     public static final String LC;
     private static final String DEFAULT_LC = "en";
     private static final String DEFAULT_LC_NAME = "English";
     private static final String FRENCH_LC = "fr";
     private static final String FRENCH_LC_NAME = "Français";
 
     static {
         String s = LocaleInfo.getCurrentLocale().getLocaleName();
         LC = s.equals("default") ? DEFAULT_LC : s;
     }
 
     // TODO move all of this to properties file, take advantage of lc
     // TODO put in Config.properties
 
     private static final int MAX_SEARCH_RESULTS = 20;
     private static final String DATA_PATH = "data/";
     private static final String HELP_PATH = "/help/help_";
     private static final int MIN_BOOK_READER_WIDTH = 600;
     private static final int MAX_BOOK_READER_WIDTH = 800;
     private static final int MIN_BOOK_BROWSER_WIDTH = 400;
     private static final int MAX_BOOK_BROWSER_WIDTH = 600;
     private static final int VIEWPORT_RESIZE_INCREMENT = 100;
 
     private static final String VIEW_CORPUS_URL = "http://spreadsheets.google.com/ccc?key=pqpY1IVVBy-A3ALgAzePRSA";
     private static final String VIEW_NARRATIVE_SECTIONS_URL = "http://spreadsheets.google.com/ccc?key=pqpY1IVVBy-CInMHM4LDKBw&pub=1";
     private static final String BUG_SUBMIT_EMAIL = "contactus@romandelarose.org";
 
     private static final String SEARCH_BOOK_RESTRICT_KEY = "BOOK";
 
     private final rosa.gwt.common.client.SearchServiceAsync searchservice = GWT
             .create(rosa.gwt.common.client.SearchService.class);
 
     private Searcher searcher;
     private Repository col;
     private Book book; // currently viewed Book
 
     private Panel content;
     private Panel sidebar;
     private int selectedImageIndex = -1; // currently selected image of book
 
     // Loaded on demand
     private CollectionDataTable coldata = null;
     private IllustrationTitlesTable illustitles = null;
     private CharacterNamesTable charnames = null;
     private NarrativeSectionsTable narsecs = null;
     private String[][] charnames_variants = null;
 
     private LoadingDialog loadingdialog = new LoadingDialog();
 
     private boolean use_flash;
 
     private DialogBox page_turner_annotation;
 
     private void addSidebarLocaleSelector() {
         String current = LC;
 
         if (current.equals(DEFAULT_LC)) {
             addSidebarItem(new Label(DEFAULT_LC_NAME));
             addSidebarItem(new Anchor(FRENCH_LC_NAME, "App.html?locale="
                     + FRENCH_LC + "#" + History.getToken()));
         } else {
             addSidebarItem(new Label(FRENCH_LC_NAME));
             addSidebarItem(new Anchor(DEFAULT_LC_NAME, "App.html#"
                     + History.getToken()));
         }
     }
 
     public void browseBook(int image) {
         if (use_flash) {
             browseBookFSI(image);
         } else {
             useBuiltinImageViewer(CodexView.Mode.IMAGE_BROWSER, image);
         }
     }
 
     private void browseBookFSI(int image) {
         String title = Labels.INSTANCE.browseImages() + ": "
                 + col.fullBookName(book.bookDataIndex());
         initDisplay(title, true);
 
         updateSelectedImage(image);
 
         EmbeddedObjectViewer.DisplayCallback cb = new EmbeddedObjectViewer.DisplayCallback() {
             public String display(String width, String height) {
                 return FSIService.embedFSIShowcase(book.id(),
                         translateBookIndexToShowcaseIndex(selectedImageIndex),
 						   width, height, LC);
             }
         };
         EmbeddedObjectViewer viewer = new EmbeddedObjectViewer(cb,
                 VIEWPORT_RESIZE_INCREMENT, MIN_BOOK_BROWSER_WIDTH,
                 MAX_BOOK_BROWSER_WIDTH, 4.0 / 3.0, title);
 
         InsertPanel toolbar = viewer.toolbar();
 
         final TextBox gobox = new TextBox();
         gobox.setStylePrimaryName("GoTextBox");
         toolbar.insert(gobox, 0);
 
         gobox.addKeyDownHandler(new KeyDownHandler() {
             public void onKeyDown(KeyDownEvent event) {
                 if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                     int image = book.guessImage(gobox.getText());
 
                     if (image != -1) {
                         FSIService
                                 .fsishowcaseSelectImage(translateBookIndexToShowcaseIndex(image));
                     }
                 }
             }
         });
 
         FSIService
                 .setupFSIShowcaseCallback(new FSIService.FSIShowcaseCallback() {
                     public void imageSelected(int index) {
                         selectedImageIndex = index;
                         String image = book.imageName(selectedImageIndex);
                         image = Book.shortImageName(image);
                         gobox.setText(image);
                     }
                 });
 
         content.add(viewer);
         content.add(new HTML(book.imagePermissionStatement()));
     }
 
     private int translateBookIndexToShowcaseIndex(int bookindex) {
         int i = 0;
 
         for (int j = 0; j < book.numImages(); j++) {
             String image = book.imageName(j);
 
             if (--bookindex < 0) {
                 break;
             }
 
             if (!Book.isMissingImage(image)) {
                 i++;
             }
         }
 
         return i;
     }
 
     private Panel createSearchResultsNav(int page, int numpages,
             String tokenqueryprefix) {
         HorizontalPanel nav = new HorizontalPanel();
         nav.setStylePrimaryName("ResultsNav");
         nav.setSpacing(2);
 
         if (numpages > 1) {
             if (page > 0) {
                 int offset = (page - 1) * MAX_SEARCH_RESULTS;
                 Hyperlink h = new Hyperlink(Labels.INSTANCE.previous(),
                         tokenqueryprefix + ';' + offset);
                 nav.add(h);
             }
 
             for (int i = 0; i < numpages; i++) {
                 if (i == page) {
                     nav.add(contentHeader("" + (i + 1)));
                 } else {
                     int offset = i * MAX_SEARCH_RESULTS;
                     Hyperlink h = new Hyperlink("" + (i + 1), tokenqueryprefix
                             + ';' + offset);
                     nav.add(h);
                 }
             }
 
             if (page < numpages - 1) {
                 int offset = (page + 1) * MAX_SEARCH_RESULTS;
                 Hyperlink h = new Hyperlink(Labels.INSTANCE.next(),
                         tokenqueryprefix + ';' + offset);
                 nav.add(h);
             }
         }
 
         return nav;
     }
 
     private void displaySearchResults(Panel panel, String tokenqueryprefix,
             SearchResult result) {
         int numpages = result.total / MAX_SEARCH_RESULTS;
 
         if (result.total % MAX_SEARCH_RESULTS != 0) {
             numpages++;
         }
 
         int page = result.offset / MAX_SEARCH_RESULTS;
 
         Panel nav = createSearchResultsNav(page, numpages, tokenqueryprefix);
         panel.add(nav);
 
         Grid grid = new Grid((result.matches.length / 2)
                 + (result.matches.length % 2), 4);
 
         for (int i = 0; i < result.matches.length; i++) {
             SearchMatch m = result.matches[i];
 
             int resultrow = i / 2;
             int resultcol = (i % 2) * 2;
 
             String thumbhtml;
             String imagename;
             String bookid;
 
             if (m.loc.contains(".")) {
                 bookid = Book.bookIDFromImage(m.loc);
                 thumbhtml = FSIService.embedStaticImage(m.loc + ".tif", 64, 64);
                 imagename = m.loc.substring(m.loc.indexOf('.') + 1);
             } else {
                 bookid = m.loc;
                 imagename = null;
                 thumbhtml = FSIService.embedStaticImage(m.loc
                         + ".binding.frontcover.tif", 64, 64);
             }
 
             grid.setWidget(resultrow, resultcol, new Hyperlink(thumbhtml, true,
                     Action.READ_BOOK.toToken(m.loc)));
 
             FlowPanel desc = new FlowPanel();
             grid.setWidget(resultrow, resultcol + 1, desc);
 
             String resultname = (imagename == null ? "" : imagename + ": ")
                     + col.fullBookName(col.findBookByID(bookid));
 
             desc.add(new Anchor(resultname, "#"
                     + Action.READ_BOOK.toToken(m.loc)));
 
             StringBuilder context = new StringBuilder();
 
             for (int j = 0; j < m.snippets.size();) {
                 String field = m.snippets.get(j++);
                 String snippet = m.snippets.get(j++);
 
                 Searcher.UserField uf = Searcher.UserField
                         .findByLuceneField(field);
                 field = uf == null ? field : uf.display;
 
                 context.append("<span class='ResultField'>" + field
                         + ":</span>");
                 context.append("<span class='ResultSnippet'> " + snippet);
                 if (j != m.snippets.size()) {
                     context.append(", ");
                 }
                 context.append("</span>");
             }
 
             desc.add(new HTML(context.toString()));
         }
 
         panel.add(grid);
         panel.add(createSearchResultsNav(page, numpages, tokenqueryprefix));
     }
 
     // Fills in values if non-null
 
     private Widget createAdvancedSearchWidget(UserField[] userfields,
             String[] userqueries, String[] restrictedbookids) {
         Panel panel = new FlowPanel();
 
         Button search = new Button(Labels.INSTANCE.search());
 
         // panel.setSpacing(5);
 
         final FlexTable table = new FlexTable();
 
         Button add = new Button(Labels.INSTANCE.addSearchField());
 
         panel.add(table);
         panel.add(add);
 
         final ListBox chosenbooks = new ListBox();
 
         // Called to search filled in query
 
         final ClickHandler searchlistener = new ClickHandler() {
             public void onClick(ClickEvent event) {
                 // Build up search history token
 
                 String[] data = new String[(table.getRowCount() * 2) + 1
                         + (chosenbooks.getItemCount() > 0 ? 2 : 0)];
                 int dataindex = 0;
                 boolean emptyquery = true;
 
                 for (int i = 0; i < table.getRowCount(); i++) {
                     ListBox lb = (ListBox) table.getWidget(i, 0);
                     TextBox tb = (TextBox) table.getWidget(i, 1);
 
                     int sel = lb.getSelectedIndex();
 
                     if (sel != -1) {
                         String userquery = tb.getText().trim();
                         String userfield = Searcher.UserField.values()[sel]
                                 .name();
 
                         if (userquery.isEmpty()) {
                             userfield = null;
                             userquery = null;
                         } else {
                             emptyquery = false;
                         }
 
                         data[dataindex++] = userfield;
                         data[dataindex++] = userquery;
                     }
                 }
 
                 if (chosenbooks.getItemCount() > 0) {
                     data[dataindex++] = SEARCH_BOOK_RESTRICT_KEY;
 
                     StringBuilder sb = new StringBuilder();
                     int len = chosenbooks.getItemCount();
                     for (int i = 0; i < len; i++) {
                         sb.append(chosenbooks.getValue(i));
 
                         if (i != len - 1) {
                             sb.append(',');
                         }
                     }
 
                     data[dataindex++] = sb.toString();
                 }
 
                 data[dataindex] = "0";
 
                 if (!emptyquery) {
                     History.newItem(Action.SEARCH.toToken(data));
                 }
             }
         };
 
         ClickHandler addlistener = new ClickHandler() {
             public void onClick(ClickEvent event) {
                 int row = table.getRowCount();
 
                 table.setWidget(row, 0, createAdvancedSearchFieldSelector());
 
                 TextBox tb = new TextBox();
                 table.setWidget(row, 1, tb);
 
                 tb.addKeyDownHandler(new KeyDownHandler() {
                     public void onKeyDown(KeyDownEvent event) {
                         if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                             searchlistener.onClick(null);
                         }
                     }
                 });
 
                 final Button remove = new Button(
                         Labels.INSTANCE.removeSearchField());
 
                 table.setWidget(row, 2, remove);
 
                 remove.addClickHandler(new ClickHandler() {
                     public void onClick(ClickEvent event) {
                         for (int row = 0; row < table.getRowCount(); row++) {
                             if (table.getWidget(row, 2) == remove) {
                                 table.removeRow(row);
                             }
                         }
                     }
                 });
             }
         };
 
         add.addClickHandler(addlistener);
 
         if (userfields != null) {
             for (int i = 0; i < userfields.length; i++) {
                 if (userfields[i] == null) {
                     continue;
                 }
 
                 int row = table.getRowCount();
                 addlistener.onClick(null);
 
                 ListBox lb = (ListBox) table.getWidget(row, 0);
                 lb.setItemSelected(userfields[i].ordinal(), true);
                 TextBox tb = (TextBox) table.getWidget(row, 1);
                 tb.setText(userqueries[i]);
             }
         } else {
             addlistener.onClick(null);
             addlistener.onClick(null);
             addlistener.onClick(null);
         }
 
         final ListBox availbooks = new ListBox();
         final Button clearbutton = new Button(Labels.INSTANCE.clearTextBox());
 
         HorizontalPanel hp = new HorizontalPanel();
         hp.setSpacing(2);
 
         hp.add(chosenbooks);
         hp.add(clearbutton);
         hp.setCellVerticalAlignment(clearbutton,
                 HasVerticalAlignment.ALIGN_BOTTOM);
 
         panel.add(availbooks);
         panel.add(hp);
         panel.add(search);
 
         chosenbooks.setVisibleItemCount(5);
         availbooks.setVisibleItemCount(1);
 
         search.addClickHandler(searchlistener);
 
         availbooks.addClickHandler(new ClickHandler() {
             public void onClick(ClickEvent event) {
                 int sel = availbooks.getSelectedIndex();
 
                 if (sel > 0) {
                     chosenbooks.setVisible(true);
                     clearbutton.setVisible(true);
 
                     chosenbooks.addItem(availbooks.getItemText(sel),
                             availbooks.getValue(sel));
                     availbooks.removeItem(sel);
                 }
             }
         });
 
         chosenbooks.addClickHandler(new ClickHandler() {
             public void onClick(ClickEvent event) {
                 int sel = chosenbooks.getSelectedIndex();
                 availbooks.addItem(chosenbooks.getItemText(sel),
                         chosenbooks.getValue(sel));
                 chosenbooks.removeItem(sel);
 
                 if (chosenbooks.getItemCount() == 0) {
                     clearbutton.click();
                 }
             }
         });
 
         ClickHandler clearlistener = new ClickHandler() {
             public void onClick(ClickEvent event) {
                 chosenbooks.clear();
                 chosenbooks.setVisible(false);
                 clearbutton.setVisible(false);
 
                 availbooks.clear();
                 availbooks.addItem(Labels.INSTANCE.restrictByBook());
 
                 for (int i = 0; i < col.numBooks(); i++) {
                     availbooks.addItem(col.fullBookName(i),
                             col.bookData(i, Repository.Category.ID));
                 }
             }
         };
 
         clearbutton.addClickHandler(clearlistener);
         clearlistener.onClick(null);
 
         if (restrictedbookids != null) {
             for (String bookid : restrictedbookids) {
                 for (int i = 0; i < availbooks.getItemCount(); i++) {
                     if (availbooks.getValue(i).equals(bookid)) {
                         chosenbooks.setVisible(true);
                         clearbutton.setVisible(true);
                         chosenbooks.addItem(availbooks.getItemText(i), bookid);
                         availbooks.removeItem(i);
                         break;
                     }
                 }
             }
         }
 
         return panel;
     }
 
     private ListBox createAdvancedSearchFieldSelector() {
         ListBox lb = new ListBox();
 
         for (Searcher.UserField uf : Searcher.UserField.values()) {
             lb.addItem(uf.display);
         }
 
         return lb;
     }
 
     private Widget createSearchWidget() {
         FlowPanel top = new FlowPanel();
 
         top.setStylePrimaryName("Search");
 
         final TextBox querybox = new TextBox();
 
         Button search = new Button(Labels.INSTANCE.search());
         top.add(querybox);
         top.add(search);
 
         Hyperlink adv = new Hyperlink(Labels.INSTANCE.advancedSearch(),
                 Action.SEARCH.toToken());
         top.add(adv);
 
         search.addClickHandler(new ClickHandler() {
             public void onClick(ClickEvent event) {
                 String s = querybox.getText().trim();
 
                 if (s.length() > 0) {
                     History.newItem(Action.SEARCH.toToken(
                             Searcher.UserField.ALL.name(), s, "0"));
                 }
             }
         });
 
         querybox.addKeyDownHandler(new KeyDownHandler() {
             public void onKeyDown(KeyDownEvent event) {
                 if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                     String s = querybox.getText().trim();
 
                     if (s.length() > 0) {
                         History.newItem(Action.SEARCH.toToken(
                                 Searcher.UserField.ALL.name(), s, "0"));
                     }
                 }
             }
         });
 
         return top;
     }
 
     private void loadBook(String bookid, final String nexttoken) {
         int bookindex = col.findBookByID(bookid);
 
         loadingdialog.display();
 
         HttpGet.Callback<Book> cb = new HttpGet.Callback<Book>() {
             public void failure(String error) {
                 loadingdialog.error(error);
             }
 
             public void success(Book result) {
                 loadingdialog.hide();
                 book = result;
                 col.loadedBooks().put(book.id(), book);
                 historyChanged(nexttoken);
             }
         };
 
         Book.load(GWT.getHostPageBaseURL() + DATA_PATH, bookid, bookindex, LC, cb);
     }
 
     private void handleHistoryTokenError(String token) {
         Window.alert("Invalid location: " + token);
         viewHome();
     }
 
     private void historyChanged(String token) {
         // TODO hack
         page_turner_annotation.hide();
 
         if (token.isEmpty()) {
             viewHome();
             return;
         }
 
         Action state = Action.fromToken(token);
         List<String> args = Action.tokenArguments(token);
 
         if (state == null) {
             handleHistoryTokenError(token);
             return;
         }
 
         if (state == Action.HOME) {
             viewHome();
         } else if (state == Action.VIEW_NARRATIVE_SECTIONS) {
             viewNarrativeSections();
         } else if (state == Action.VIEW_PARTNERS) {
             viewPage(Labels.INSTANCE.partners(),
                     Resources.INSTANCE.partnersHtml());
         } else if (state == Action.VIEW_ROSE_HISTORY) {
             viewPage(Labels.INSTANCE.roseHistory(),
                     Resources.INSTANCE.roseHistoryHtml());
         } else if (state == Action.VIEW_CORPUS) {
             viewCorpus();
         } else if (state == Action.VIEW_ILLUSTRATION_TITLES) {
             viewIllustrationTitles();
         } else if (state == Action.VIEW_COLLECTION_DATA) {
             viewCollectionData();
         } else if (state == Action.VIEW_CHARACTER_NAMES) {
             viewCharacterNames();
         } else if (state == Action.VIEW_DONATION) {
             viewPage(Labels.INSTANCE.donation(),
                     Resources.INSTANCE.donationHtml());
         } else if (state == Action.VIEW_TERMS) {
             viewPage(Labels.INSTANCE.termsAndConditions(),
                     Resources.INSTANCE.termsAndConditionsHtml());
         } else if (state == Action.VIEW_CONTACT) {
             viewPage(Labels.INSTANCE.contactUs(),
                     Resources.INSTANCE.contactHtml());
         } else if (state == Action.VIEW_PROJECT_HISTORY) {
             viewPage(Labels.INSTANCE.projectHistory(),
                     Resources.INSTANCE.projectHistoryHtml());
         } else if (state == Action.VIEW_BOOK_BIB) {
             if (args.size() != 1) {
                 handleHistoryTokenError(token);
                 return;
             }
 
             String bookid = args.get(0);
 
             if (col.findBookByID(bookid) == -1) {
                 handleHistoryTokenError(token);
                 return;
             }
 
             if (selectBook(bookid, token)) {
                 return;
             } else {
                 viewBookBibliography();
             }
         } else if (state == Action.VIEW_BOOK) {
             if (args.size() != 1) {
                 handleHistoryTokenError(token);
                 return;
             }
 
             String bookid = args.get(0);
 
             if (col.findBookByID(bookid) == -1) {
                 handleHistoryTokenError(token);
                 return;
             }
 
             if (selectBook(bookid, token)) {
                 return;
             } else {
                 viewBook();
             }
         } else if (state == Action.READ_BOOK) {
             if (args.size() != 1) {
                 handleHistoryTokenError(token);
                 return;
             }
 
             String bookid = args.get(0);
             String image = null;
 
             int i = bookid.indexOf('.');
 
             if (i != -1) {
                 image = bookid.substring(i + 1);
                 bookid = bookid.substring(0, i);
             }
 
             if (col.findBookByID(bookid) == -1) {
                 handleHistoryTokenError(token);
                 return;
             }
 
             if (selectBook(bookid, token)) {
                 return;
             } else {
                 readBook(image == null ? -1 : book.guessImage(image));
             }
         } else if (state == Action.BROWSE_BOOK) {
             if (args.size() != 1) {
                 handleHistoryTokenError(token);
                 return;
             }
 
             String bookid = args.get(0);
             String image = null;
 
             int i = bookid.indexOf('.');
 
             if (i != -1) {
                 image = bookid.substring(i + 1);
                 bookid = bookid.substring(0, i);
             }
 
             if (col.findBookByID(bookid) == -1) {
                 handleHistoryTokenError(token);
             }
 
             if (selectBook(bookid, token)) {
                 return;
             } else {
                 browseBook(image == null ? -1 : book.guessImage(image));
             }
         } else if (state == Action.SELECT_BOOK) {
             if (args.size() != 1) {
                 handleHistoryTokenError(token);
                 return;
             }
 
             Repository.Category cat = null;
 
             for (Repository.Category d : Repository.Category.values()) {
                 if (d.name().equals(args.get(0))) {
                     cat = d;
                 }
             }
 
             if (cat == null) {
                 handleHistoryTokenError(token);
                 return;
             }
 
             viewBookPicker(cat);
         } else if (state == Action.SEARCH) {
             if (args.size() == 0) {
                 viewSearch(null, null, null, 0, null);
                 return;
             }
 
             if (args.size() < 3 || (args.size() & 1) == 0) {
                 handleHistoryTokenError(token);
                 return;
             }
 
             int offset;
 
             try {
                 offset = Integer.parseInt(args.get(args.size() - 1));
             } catch (NumberFormatException e) {
                 handleHistoryTokenError(token);
                 return;
             }
 
             String[] userqueries = new String[(args.size() - 1) / 2];
             Searcher.UserField[] userfields = new Searcher.UserField[userqueries.length];
             String[] bookrestrict = null;
 
             int argindex = 0;
             for (int i = 0; i < userqueries.length; i++) {
                 userfields[i] = null;
 
                 if (SEARCH_BOOK_RESTRICT_KEY.equals(args.get(argindex))) {
                     argindex++;
                     bookrestrict = args.get(argindex++).split(",");
 
                     for (String bookid : bookrestrict) {
                         if (col.findBookByID(bookid) == -1) {
                             handleHistoryTokenError(token);
                         }
                     }
                 } else {
                     for (Searcher.UserField uf : Searcher.UserField.values()) {
                         if (uf.name().equals(args.get(argindex))) {
                             userfields[i] = uf;
                         }
                     }
 
                     if (userfields[i] == null) {
                         handleHistoryTokenError(token);
                         return;
                     }
 
                     argindex++;
                     userqueries[i] = args.get(argindex++);
                 }
             }
 
             String searchtokenprefix = token.substring(0,
                     token.lastIndexOf(';'));
 
             viewSearch(userfields, userqueries, bookrestrict, offset,
                     searchtokenprefix);
         } else {
             handleHistoryTokenError(token);
             return;
         }
 
         Analytics.track(state, book == null ? null : book.id(), args);
     }
 
     private void viewPage(String label, ExternalTextResource html) {
         book = null;
         initDisplay(label, false);
 
         addHtml(content, html);
     }
 
     /**
      * Add external html resource to a panel. On success also add the given
      * widgets.
      * 
      * @param panel
      * @param html
      * @param widgets
      */
     private static void addHtml(final Panel panel, ExternalTextResource html,
             final Widget... widgets) {
         try {
             html.getText(new ResourceCallback<TextResource>() {
                 public void onSuccess(TextResource resource) {
                     panel.add(new HTML(resource.getText()));
 
                     for (Widget w : widgets) {
                         panel.add(w);
                     }
                 }
 
                 public void onError(ResourceException e) {
                     reportInternalError("Failed to load external resource", e);
                 }
             });
         } catch (ResourceException e) {
             reportInternalError("Failed to load external resource", e);
         }
     }
 
     private static void reportInternalError(String message, Throwable e) {
         Window.alert("This is likely a bug. Please report to "
                 + BUG_SUBMIT_EMAIL
                 + ". Include your operating system and version, browser and version, url you were visiting, and what you did to trigger the problem. \nInternal error: "
                 + message + "\n" + e.getMessage()
                 + (e.getCause() == null ? "" : "\nCaused by: " + e.getCause()));
     }
 
     private static Image getImage(String name, String alt) {
         Image img = new Image(GWT.getModuleBaseURL() + name);
 
         img.setAltText(alt);
 
         return img;
     }
 
     public void onModuleLoad() {
         GWT.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
             public void onUncaughtException(Throwable e) {
                 reportInternalError("Uncaught exception", e);
                 viewHome();
             }
         });
 
         Resources.INSTANCE.css().ensureInjected();
 
         DockLayoutPanel dock = new DockLayoutPanel(Unit.PX);
 
         FlowPanel header = new FlowPanel();
 
         header.add(getImage("banner_image1.gif", "banner"));
         header.add(getImage("banner_text.jpg",
                 "Roman de la Rose Digital Library"));
         header.add(createSearchWidget());
 
         content = new FlowPanel();
         sidebar = new FlowPanel();
 
         header.setStylePrimaryName("Header");
         content.setStylePrimaryName("Content");
         sidebar.setStylePrimaryName("Sidebar");
         dock.setStylePrimaryName("Main");
 
         dock.addNorth(header, 96);
         dock.addWest(new ScrollPanel(sidebar), 181);
         dock.add(new ScrollPanel(content));
 
         RootLayoutPanel.get().add(dock);
 
         History.addValueChangeHandler(new ValueChangeHandler<String>() {
             public void onValueChange(ValueChangeEvent<String> event) {
                 historyChanged(event.getValue());
             }
         });
 
         page_turner_annotation = new DialogBox(false, false);
         page_turner_annotation.setAnimationEnabled(true);
 
         searcher = new Searcher(searchservice);
         col = new Repository(Util.parseCSVTable(Resources.INSTANCE
                 .bookBrowseTable().getText()));
         use_flash = browserSupportsFlash();
 
         History.fireCurrentHistoryState();
     }
 
     public boolean browserSupportsFlash() {
         String agent = Window.Navigator.getUserAgent();
 
         if (agent.contains("iPad") || agent.contains("iPhone")
                 || agent.contains("iPod")) {
             return false;
         }
 
         return true;
     }
 
     private void updateSelectedImage(int image) {
         if (image == -1) {
             for (int i = 0; i < book.numImages(); i++) {
                 if (!Book.isMissingImage(book.imageName(i))) {
                     selectedImageIndex = i;
                     break;
                 }
             }
         } else {
             selectedImageIndex = image;
         }
     }
 
     private void readBook(int image) {
         if (use_flash) {
             readBookFSIViewer(image);
         } else {
             useBuiltinImageViewer(CodexView.Mode.PAGE_TURNER, image);
         }
     }
 
     private void useBuiltinImageViewer(CodexView.Mode mode, int image) {
         String title = Labels.INSTANCE.pageTurner() + ": "
                 + col.fullBookName(book.bookDataIndex());
         initDisplay(title, true);
 
         updateSelectedImage(image);
 
         Panel toolbar = new FlowPanel();
 
         final Panel reader_toolbar = new FlowPanel();
         
         final TextBox gobox = new TextBox();
         gobox.setStylePrimaryName("GoTextBox");
 
         Button reader_button = new Button(Labels.INSTANCE.pageTurner());
         Button browser_button = new Button(Labels.INSTANCE.browseImages());
         
         toolbar.add(reader_button);
         toolbar.add(browser_button);
         
         RoseBook rose_book = new RoseBook(book.imagesTable());
         final CodexModel rose_book_model = rose_book.model();
         final CodexController rose_book_ctrl = new SimpleCodexController(
                 rose_book_model);
         ImageServer img_server = new FsiImageServer(
                 "http://fsiserver.library.jhu.edu/server");
 
         ScrollPanel content_scroll = (ScrollPanel) content.getParent();
 
         final CodexView rose_book_view = new CodexView(img_server,
                 rose_book_model, rose_book_ctrl, content_scroll);
 
         Button next = new Button(Labels.INSTANCE.next());
         Button first = new Button(Labels.INSTANCE.first());
         Button last = new Button(Labels.INSTANCE.last());
         Button prev = new Button(Labels.INSTANCE.previous());
                        
         next.addClickHandler(new ClickHandler() {
             public void onClick(ClickEvent event) {
                 rose_book_ctrl.gotoNextOpening();
             }
         });
 
         prev.addClickHandler(new ClickHandler() {
             public void onClick(ClickEvent event) {
                 rose_book_ctrl.gotoPreviousOpening();
             }
         });
 
         first.addClickHandler(new ClickHandler() {
             public void onClick(ClickEvent event) {
                 rose_book_ctrl.gotoOpening(rose_book_model.opening(0));
             }
         });
 
         last.addClickHandler(new ClickHandler() {
             public void onClick(ClickEvent event) {
                 rose_book_ctrl.gotoOpening(rose_book_model
                         .opening(rose_book_model.numOpenings() - 1));
             }
         });
 
         reader_toolbar.add(first);
         reader_toolbar.add(prev);
         reader_toolbar.add(gobox);
         reader_toolbar.add(next);
         reader_toolbar.add(last);
 
         reader_button.addClickHandler(new ClickHandler() {
             public void onClick(ClickEvent event) {
                 reader_toolbar.setVisible(true);
                 rose_book_view.setMode(Mode.PAGE_TURNER);
             }
         });
 
         browser_button.addClickHandler(new ClickHandler() {
             public void onClick(ClickEvent event) {
                 reader_toolbar.setVisible(false);
                 page_turner_annotation.hide();
                 rose_book_view.setMode(Mode.IMAGE_BROWSER);
             }
         });
 
         gobox.addKeyDownHandler(new KeyDownHandler() {
             public void onKeyDown(KeyDownEvent event) {
                 if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                     int index = book.guessImage(gobox.getText());
 
                     if (index != -1) {
                         index /= 2;
 
                         if (index < rose_book_model.numOpenings()) {
                             rose_book_ctrl.gotoOpening(rose_book_model
                                     .opening(index));
                         }
                     }
                 }
             }
         });
 
         final ListBox sidechoice = new ListBox();
         sidechoice.setVisibleItemCount(1);
 
         sidechoice.addItem(Labels.INSTANCE.show());
 
         if (!col.bookData(book.bookDataIndex(),
                 Repository.Category.TRANSCRIPTION).equals(
                 Labels.INSTANCE.none())) {
             sidechoice.addItem(Labels.INSTANCE.transcription());
             sidechoice.addItem(Labels.INSTANCE.transcription() + " ["
                     + Labels.INSTANCE.lecoy() + "]");
         }
 
         if (!col.bookData(book.bookDataIndex(),
                 Repository.Category.ILLUSTRATION_TAGGING).equals(
                 Labels.INSTANCE.none())) {
             sidechoice.addItem(Labels.INSTANCE.illustrationDescription());
         }
 
         if (!col.bookData(book.bookDataIndex(),
                 Repository.Category.NARRATIVE_TAGGING).equals(
                 Labels.INSTANCE.none())) {
             sidechoice.addItem(Labels.INSTANCE.narrativeSections());
         }
 
         final ChangeHandler sidechoicelistener = new ChangeHandler() {
             public void onChange(ChangeEvent event) {
                 String choice = sidechoice.getItemText(sidechoice
                         .getSelectedIndex());
 
                 if (!page_turner_annotation.isShowing()) {
                     page_turner_annotation.setPopupPosition(
                             content.getAbsoluteLeft()
                                     + rose_book_view.getUsedWidth() + 2,
                             content.getAbsoluteTop());
                 }
 
                 if (choice.startsWith(Labels.INSTANCE.transcription())) {
                     boolean lecoy = choice.contains(Labels.INSTANCE.lecoy());
 
                     page_turner_annotation.setText(Labels.INSTANCE
                             .transcription());
                     page_turner_annotation.setWidget(displayTranscription(lecoy));
                     page_turner_annotation.show();
 
                     Analytics.trackEvent("Book", "display-trans", book.id());
                 } else if (choice.equals(Labels.INSTANCE
                         .illustrationDescription())) {
                     page_turner_annotation.setText(Labels.INSTANCE
                             .illustrationDescription());
                     page_turner_annotation.clear();
                     displayIllustrationKeywordsOnRight(page_turner_annotation);
                     page_turner_annotation.show();
 
                     Analytics.trackEvent("Book", "display-illus-tags",
                             book.id());
                 } else if (choice.equals(Labels.INSTANCE.narrativeSections())) {
                     page_turner_annotation.setText(Labels.INSTANCE
                             .narrativeSections());
                     page_turner_annotation.clear();
                     displayNarrativeSectionsOnRight(page_turner_annotation);
                     page_turner_annotation.show();
 
                     Analytics.trackEvent("Book", "display-nar-secs", book.id());
                 } else {
                     page_turner_annotation.hide();
                 }
             }
         };
 
         sidechoice.addChangeHandler(sidechoicelistener);
 
         if (sidechoice.getItemCount() > 1) {
             reader_toolbar.add(sidechoice);
         }
 
         rose_book_ctrl.addChangeHandler(new CodexController.ChangeHandler() {
             public void viewChanged(List<CodexImage> view) {
                 reader_toolbar.setVisible(false);
                 page_turner_annotation.hide();
 
                 if (view.size() == 1) {
                     CodexImage img = view.get(0);
                     selectedImageIndex = book.findImage(img.label());
                 }
             }
 
             public void openingChanged(CodexOpening opening) {
                 reader_toolbar.setVisible(true);
 
                 selectedImageIndex = opening.position() * 2;
                 gobox.setText(opening.label());
 
                 // update side display
                 sidechoicelistener.onChange(null);
             }
         });
                                
         content.add(toolbar);
         content.add(rose_book_view);
         content.add(reader_toolbar);
         content.add(new HTML(book.imagePermissionStatement()));
 
         if (mode == Mode.IMAGE_BROWSER) {
             reader_toolbar.setVisible(false);
         } else if (mode == Mode.PAGE_TURNER) {
             reader_toolbar.setVisible(true);
 
             int index = selectedImageIndex / 2;
 
             if (index < rose_book_model.numOpenings()) {
                 rose_book_ctrl.gotoOpening(rose_book_model.opening(index));
             } else {
                 rose_book_ctrl.gotoOpening(rose_book_model.opening(0));
             }
         } else if (mode == Mode.IMAGE_VIEWER) {
             if (selectedImageIndex < rose_book_model.numImages()) {
                 rose_book_ctrl.setView(rose_book_model
                         .image(selectedImageIndex));
             } else {
                 rose_book_ctrl.setView(rose_book_model
                         .nonOpeningImage(selectedImageIndex
                                 - rose_book_model.numImages()));
             }
         }
 
         rose_book_view.setMode(mode);
     }
 
     // TODO stack layout panel for menu choices
 
     private void readBookFSIViewer(int image) {
         String title = Labels.INSTANCE.pageTurner() + ": "
                 + col.fullBookName(book.bookDataIndex());
         initDisplay(title, true);
 
         updateSelectedImage(image);
 
         EmbeddedObjectViewer.DisplayCallback cb = new EmbeddedObjectViewer.DisplayCallback() {
             public String display(String width, String height) {
                 return FSIService.embedFSIPages(book.id(), selectedImageIndex,
 						width, height, LC);
             }
         };
 
         EmbeddedObjectViewer viewer = new EmbeddedObjectViewer(cb,
                 VIEWPORT_RESIZE_INCREMENT, MIN_BOOK_READER_WIDTH,
                 MAX_BOOK_READER_WIDTH, 3.0 / 4.0, title);
         InsertPanel toolbar = viewer.toolbar();
 
         final TextBox gobox = new TextBox();
         gobox.setStylePrimaryName("GoTextBox");
 
         toolbar.insert(gobox, 0);
 
         gobox.addKeyDownHandler(new KeyDownHandler() {
             public void onKeyDown(KeyDownEvent event) {
                 if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                     int index = book.guessImage(gobox.getText());
 
                     if (index != -1) {
                         FSIService.fsipagesGotoImage(index + 1);
                     }
                 }
             }
         });
 
         final ComplexPanel display = new HorizontalPanel();
         display.add(viewer);
 
         final ListBox sidechoice = new ListBox();
         sidechoice.setVisibleItemCount(1);
 
         sidechoice.addItem(Labels.INSTANCE.show());
 
         if (!col.bookData(book.bookDataIndex(),
                 Repository.Category.TRANSCRIPTION).equals(
                 Labels.INSTANCE.none())) {
             sidechoice.addItem(Labels.INSTANCE.transcription());
             sidechoice.addItem(Labels.INSTANCE.transcription() + " ["
                     + Labels.INSTANCE.lecoy() + "]");
         }
 
         if (!col.bookData(book.bookDataIndex(),
                 Repository.Category.ILLUSTRATION_TAGGING).equals(
                 Labels.INSTANCE.none())) {
             sidechoice.addItem(Labels.INSTANCE.illustrationDescription());
         }
 
         if (!col.bookData(book.bookDataIndex(),
                 Repository.Category.NARRATIVE_TAGGING).equals(
                 Labels.INSTANCE.none())) {
             sidechoice.addItem(Labels.INSTANCE.narrativeSections());
         }
 
         final ChangeHandler sidechoicelistener = new ChangeHandler() {
             public void onChange(ChangeEvent event) {
                 // Should only be page turner and metadata display in hp
                 while (display.getWidgetCount() > 1) {
                     display.remove(1);
                 }
 
                 String choice = sidechoice.getItemText(sidechoice
                         .getSelectedIndex());
 
                 if (choice.startsWith(Labels.INSTANCE.transcription())) {
                     boolean lecoy = choice.contains(Labels.INSTANCE.lecoy());
                     
                     display.add(displayTranscription(lecoy));
                     Analytics.trackEvent("Book", "display-trans", book.id());
                 } else if (choice.equals(Labels.INSTANCE
                         .illustrationDescription())) {
                     displayIllustrationKeywordsOnRight(display);
                     Analytics.trackEvent("Book", "display-illus-tags",
                             book.id());
                 } else if (choice.equals(Labels.INSTANCE.narrativeSections())) {
                     displayNarrativeSectionsOnRight(display);
                     Analytics.trackEvent("Book", "display-nar-secs", book.id());
                 }
             }
         };
 
         sidechoice.addChangeHandler(sidechoicelistener);
 
         FSIService.setupFSIPagesCallback(new FSIService.FSIPagesCallback() {
             public void pageChanged(int page) {
                 selectedImageIndex = page;
 
                 if (page == book.numImages()) {
                     String image = Book.shortImageName(book.imageName(page - 1));
                     gobox.setText(image);
                 } else {
                     String image = Book.shortImageName(book.imageName(page));
 
                     if (page > 0) {
                         String imagev = Book.shortImageName(book
                                 .imageName(page - 1));
                         gobox.setText(imagev + ", " + image);
                     } else {
                         gobox.setText(image);
                     }
                 }
 
                 // update transcription display
                 sidechoicelistener.onChange(null);
             }
 
             public void imageInfo(String info) {
                 selectedImageIndex = FSIService
                         .getImageIndexFromPagesInfo(info);
                 gobox.setText(Book.shortImageName(book
                         .imageName(selectedImageIndex)));
             }
         });
 
         if (sidechoice.getItemCount() > 1) {
             toolbar.add(sidechoice);
         }
 
         content.add(display);
         content.add(new HTML(book.imagePermissionStatement()));
     }
 
     private Widget displayTranscription(final boolean lecoy) {
         final ComplexPanel container = new FlowPanel();
 
         String recto = null;
         String verso = null;
         String image = book.imageName(selectedImageIndex);
 
         if (Book.isRectoImage(selectedImageIndex)) {
             recto = image;
             verso = book.imageName(selectedImageIndex - 1);
         } else {
             if (selectedImageIndex + 1 < book.numImages()) {
                 recto = book.imageName(selectedImageIndex + 1);
             }
 
             verso = image;
         }
 
         if (recto != null) {
             recto = Book.isFolioImage(recto) && !Book.isMissingImage(recto) ? recto
                     : null;
         }
 
         if (verso != null) {
             verso = Book.isFolioImage(verso) && !Book.isMissingImage(verso) ? verso
                     : null;
         }
 
         String[] urls;
         final String[] imagenames;
 
         if (recto == null && verso == null) {
             Label l = new Label(Labels.INSTANCE.transcriptionUnavailable());
             l.setStylePrimaryName("TranscriptionUnavailableError");
             container.add(l);
             
             return container;
         } else if (recto == null) {
             urls = new String[] { GWT.getHostPageBaseURL() + DATA_PATH
                     + Book.transcriptionPath(verso) };
             imagenames = new String[] { Book.shortImageName(verso) };
         } else if (verso == null) {
             urls = new String[] { GWT.getHostPageBaseURL() + DATA_PATH
                     + Book.transcriptionPath(recto) };
             imagenames = new String[] { Book.shortImageName(recto) };
         } else {
             urls = new String[] {
                     GWT.getHostPageBaseURL() + DATA_PATH
                             + Book.transcriptionPath(verso),
                     GWT.getHostPageBaseURL() + DATA_PATH
                             + Book.transcriptionPath(recto) };
             imagenames = new String[] { Book.shortImageName(verso),
                     Book.shortImageName(recto) };
         }
         
         HttpGet.request(urls, new HttpGet.Callback<String[]>() {
             public void failure(String error) {
                 if (container.getWidgetCount() == 1) {
                     Label l = new Label(Labels.INSTANCE
                             .transcriptionUnavailable());
                     l.setStylePrimaryName("TranscriptionUnavailableError");
                     container.add(l);
                 }
             }
 
             public void success(String[] results) {
                 for (int i = 0; i < results.length; i++) {
                     if (results[i].isEmpty()) {
                         results[i] = null;
                     }
                 }
 
                 Widget trans = TranscriptionViewer.createTranscriptionViewer(
                         results, imagenames, container.getOffsetHeight() - 50,
                         lecoy);
                 container.add(trans);
             }
         });
         
         return container;
     }
 
     private void displayIllustrationKeywordsOnRight(final Panel container) {
         int recto = -1;
         int verso = -1;
 
         ImageTagging illus = book.illustrations();
 
         if (illus == null) {
             String illusurl = GWT.getHostPageBaseURL() + DATA_PATH
                     + book.illustrationsPath();
 
             loadingdialog.display();
 
             HttpGet.request(illusurl, new HttpGet.Callback<String>() {
                 public void failure(String error) {
                     loadingdialog.error(error);
                 }
 
                 public void success(String result) {
                     loadingdialog.hide();
                     book.setIllustrations(result);
 
                     if (book.illustrations() != null) {
                         displayIllustrationKeywordsOnRight(container);
                     }
                 }
             });
         } else {
             if (Book.isRectoImage(selectedImageIndex)) {
                 recto = selectedImageIndex;
 
                 if (recto > 0) {
                     verso = selectedImageIndex - 1;
                 }
             } else {
                 if (selectedImageIndex + 1 < book.numImages()) {
                     recto = selectedImageIndex + 1;
                 }
 
                 verso = selectedImageIndex;
             }
 
             TabLayoutPanel tabpanel = new TabLayoutPanel(1.5, Unit.EM);
             tabpanel.addStyleName("ImageDescription");
 
             if (verso != -1) {
                 displayIllustrationKeywords(tabpanel, verso);
             }
 
             if (recto != -1) {
                 displayIllustrationKeywords(tabpanel, recto);
             }
 
             if (tabpanel.getWidgetCount() > 0) {
                 tabpanel.selectTab(0);
                 container.add(tabpanel);
             }
         }
     }
 
     private void displayIllustrationKeywords(TabLayoutPanel tabpanel, int image) {
         ImageTagging illus = book.illustrations();
 
         int count = 1;
         List<Integer> indexes = illus.findImageIndexes(image);
 
         for (int i : indexes) {
             String name = Book.shortImageName(book.imageName(image))
                     + (indexes.size() > 1 ? " " + count++ : "");
             tabpanel.add(new ScrollPanel(illus.displayImage(i)), name);
         }
     }
 
     private void displayNarrativeSections(TabLayoutPanel tabpanel, int image) {
         NarrativeTagging narmap = book.narrativeMap();
 
         List<Integer> indexes = narmap.findImageIndexes(book, image);
 
         // Tab name -> panel, one for each column
         HashMap<String, Panel> newtabs = new HashMap<String, Panel>(2);
 
         String imagename = Book.shortImageName(book.imageName(image));
 
         for (int section : indexes) {
             String tabname = imagename + "." + narmap.startColumn(section);
             Panel p = newtabs.get(tabname);
 
             if (p == null) {
                 p = new FlowPanel();
                 newtabs.put(tabname, p);
                 tabpanel.add(new ScrollPanel(p), tabname);
             }
 
             p.add(narmap.displaySection(section, narsecs));
         }
     }
 
     private void displayNarrativeSectionsOnRight(final Panel container) {
         int recto = -1;
         int verso = -1;
 
         // Load narrative sections if needed
         if (narsecs == null) {
             loadingdialog.display();
 
             try {
                 Resources.INSTANCE.narrativeSectionsTable().getText(
                         new ResourceCallback<TextResource>() {
                             public void onSuccess(TextResource resource) {
                                 loadingdialog.hide();
 
                                 narsecs = new NarrativeSectionsTable(resource
                                         .getText());
                                 displayNarrativeSectionsOnRight(container);
                             }
 
                             public void onError(ResourceException e) {
                                 loadingdialog.hide();
                                 reportInternalError(
                                         "Failed to load external resource", e);
                             }
                         });
             } catch (ResourceException e) {
                 loadingdialog.hide();
                 reportInternalError("Failed to load external resource", e);
             }
         } else {
             NarrativeTagging narmap = book.narrativeMap();
 
             if (narmap == null) {
                 loadingdialog.display();
                 String narmapurl = GWT.getHostPageBaseURL() + DATA_PATH
                         + book.narrativeMapPath();
 
                 HttpGet.request(narmapurl, new HttpGet.Callback<String>() {
                     public void failure(String error) {
                         loadingdialog.error(error);
                     }
 
                     public void success(String result) {
                         loadingdialog.hide();
                         book.setNarrativeMap(result);
 
                         if (book.narrativeMap() != null) {
                             displayNarrativeSectionsOnRight(container);
                         }
                     }
                 });
             } else {
                 if (Book.isRectoImage(selectedImageIndex)) {
                     recto = selectedImageIndex;
 
                     if (recto > 0) {
                         verso = selectedImageIndex - 1;
                     }
                 } else {
                     if (selectedImageIndex + 1 < book.numImages()) {
                         recto = selectedImageIndex + 1;
                     }
 
                     verso = selectedImageIndex;
                 }
 
                 TabLayoutPanel tabpanel = new TabLayoutPanel(1.5, Unit.EM);
 
                 tabpanel.addStyleName("ImageDescription");
 
                 if (verso != -1) {
                     displayNarrativeSections(tabpanel, verso);
                 }
 
                 if (recto != -1) {
                     displayNarrativeSections(tabpanel, recto);
                 }
 
                 if (tabpanel.getWidgetCount() > 0) {
                     tabpanel.selectTab(0);
                     container.add(tabpanel);
                 }
             }
         }
     }
 
     // TODO use DialogBox
 
     private class LoadingDialog extends DecoratedPopupPanel {
         private final Panel panel;
 
         public LoadingDialog() {
             super(false);
             this.panel = new FlowPanel();
             panel.setStylePrimaryName("LoadingDialog");
             add(panel);
         }
 
         public void display() {
             panel.clear();
             panel.add(new Label(Labels.INSTANCE.usingWebService()));
             panel.add(new Label("..."));
             center();
         }
 
         public void error(String error) {
             // make sure dialog is visible
             content.clear();
 
             panel.clear();
             panel.add(new Label(Labels.INSTANCE.error()));
             panel.add(new Label(error));
             addCloseButton(true);
             center();
         }
 
         private void addCloseButton(final boolean gohomeonclose) {
             Button close = new Button(Labels.INSTANCE.close(),
                     new ClickHandler() {
                         public void onClick(ClickEvent event) {
                             hide();
 
                             if (gohomeonclose) {
                                 History.newItem(Action.HOME.toToken());
                             }
                         }
                     });
 
             panel.add(close);
         }
     }
 
     // If search arguments are null, just displays search dialog
 
     private void viewSearch(final Searcher.UserField[] userfields,
             final String[] userqueries, final String[] restrictedbookids,
             final int offset, final String searchtokenprefix) {
         book = null;
 
         if (userfields == null || userqueries == null) {
             initDisplay(Labels.INSTANCE.search(), true);
             content.add(createAdvancedSearchWidget(userfields, userqueries,
                     restrictedbookids));
             return;
         }
 
         loadingdialog.display();
 
         // Load character names as needed
 
         if (charnames_variants == null) {
             try {
                 Resources.INSTANCE.characterNamesTable().getText(
                         new ResourceCallback<TextResource>() {
                             public void onSuccess(TextResource resource) {
                                 loadingdialog.hide();
 
                                 charnames = new CharacterNamesTable(resource
                                         .getText());
                                 charnames_variants = charnames
                                         .asSearchVariants();
 
                                 viewSearch(userfields, userqueries,
                                         restrictedbookids, offset,
                                         searchtokenprefix);
                             }
 
                             public void onError(ResourceException e) {
                                 loadingdialog.hide();
                                 reportInternalError(
                                         "Failed to load external resource", e);
                             }
                         });
             } catch (ResourceException e) {
                 loadingdialog.hide();
                 reportInternalError("Failed to load external resource", e);
             }
         } else {
             AsyncCallback<SearchResult> cb = new AsyncCallback<SearchResult>() {
                 public void onFailure(Throwable caught) {
                     loadingdialog.error(caught.getMessage());
                 }
 
                 public void onSuccess(SearchResult result) {
                     loadingdialog.hide();
                     initDisplay(result.total + " " + Labels.INSTANCE.hits(),
                             true);
 
                     content.add(createAdvancedSearchWidget(userfields,
                             userqueries, restrictedbookids));
                     displaySearchResults(content, searchtokenprefix, result);
                 }
             };
 
             searcher.searchCollection(userfields, userqueries,
                     restrictedbookids, offset, MAX_SEARCH_RESULTS,
                     charnames_variants, cb);
         }
     }
 
     /**
      * Select and load book as needed.
      * 
      * @param id
      * @param nexttoken
      * @return whether or not book is being loaded
      */
     private boolean selectBook(String id, String nexttoken) {
         Book newbook = col.loadedBooks().get(id);
 
         if (newbook == null) {
             loadBook(id, nexttoken);
             return true;
         }
 
         book = newbook;
 
         return false;
     }
 
     private void addSidebarHeader(String header) {
         Label label = new Label(header);
         label.setStylePrimaryName("SidebarHeader");
         sidebar.add(label);
     }
 
     private void addSidebarItem(String text, String token) {
         addSidebarItem(createSidebarHyperlink(text, token));
     }
 
     private void addSidebarItem(Widget w) {
         w.setStylePrimaryName("SidebarItem");
         sidebar.add(w);
     }
 
     private static Widget createSidebarHyperlink(String name, String token) {
         if (History.getToken().startsWith(token)) {
             Label l = new Label(name);
             l.addStyleName("SidebarSelected");
             return l;
         } else {
             return new Hyperlink(name, token);
         }
     }
 
     private void setupSidebar() {
         sidebar.clear();
 
         sidebar.add(createSidebarHyperlink(Labels.INSTANCE.mainPage(),
                 Action.HOME.toToken()));
         sidebar.add(createSidebarHyperlink(Labels.INSTANCE.roseHistory(),
                 Action.VIEW_ROSE_HISTORY.toToken()));
         sidebar.add(createSidebarHyperlink(Labels.INSTANCE.roseCorpus(),
                 Action.VIEW_CORPUS.toToken()));
         sidebar.add(createSidebarHyperlink(Labels.INSTANCE.collectionData(),
                 Action.VIEW_COLLECTION_DATA.toToken()));
         sidebar.add(createSidebarHyperlink(Labels.INSTANCE.narrativeSections(),
                 Action.VIEW_NARRATIVE_SECTIONS.toToken()));
         sidebar.add(createSidebarHyperlink(
                 Labels.INSTANCE.illustrationTitles(),
                 Action.VIEW_ILLUSTRATION_TITLES.toToken()));
         sidebar.add(createSidebarHyperlink(Labels.INSTANCE.characterNames(),
                 Action.VIEW_CHARACTER_NAMES.toToken()));
 
         Anchor help = new Anchor(Labels.INSTANCE.help());
         sidebar.add(help);
 
         if (book != null) {
             addSidebarHeader(Labels.INSTANCE.book());
             addSidebarItem(Labels.INSTANCE.description(),
                     Action.VIEW_BOOK.toToken(book.id()));
             addSidebarItem(Labels.INSTANCE.pageTurner(),
                     Action.READ_BOOK.toToken(book.id()));
             addSidebarItem(Labels.INSTANCE.browseImages(),
                     Action.BROWSE_BOOK.toToken(book.id()));
 
             if (!col.bookData(book.bookDataIndex(),
                     Repository.Category.BIBLIOGRAPHY).equals("0")) {
                 addSidebarItem(Labels.INSTANCE.bibliography(),
                         Action.VIEW_BOOK_BIB.toToken(book.id()));
             }
         }
 
         addSidebarHeader(Labels.INSTANCE.selectBookBy());
 
         for (Repository.Category d : Repository.Category.values()) {
             if (d == Repository.Category.SHELFMARK
                     || d == Repository.Category.ILLUSTRATION_TAGGING
                     || d == Repository.Category.NARRATIVE_TAGGING) {
                 continue;
             }
 
             addSidebarItem(d.display(), Action.SELECT_BOOK.toToken(d.name()));
         }
 
         addSidebarHeader(Labels.INSTANCE.project());
         addSidebarItem(createSidebarHyperlink(
                 Labels.INSTANCE.termsAndConditions(),
                 Action.VIEW_TERMS.toToken()));
         addSidebarItem(createSidebarHyperlink(Labels.INSTANCE.partners(),
                 Action.VIEW_PARTNERS.toToken()));
         addSidebarItem(createSidebarHyperlink(Labels.INSTANCE.projectHistory(),
                 Action.VIEW_PROJECT_HISTORY.toToken()));
         addSidebarItem(createSidebarHyperlink(Labels.INSTANCE.donation(),
                 Action.VIEW_DONATION.toToken()));
         addSidebarItem(new Anchor(Labels.INSTANCE.blog(),
                 "http://romandelarose.blogspot.com"));
         addSidebarItem(createSidebarHyperlink(Labels.INSTANCE.contactUs(),
                 Action.VIEW_CONTACT.toToken()));
 
         addSidebarHeader(Labels.INSTANCE.language());
         addSidebarLocaleSelector();
 
         help.addClickHandler(new ClickHandler() {
             public void onClick(ClickEvent event) {
                 Util.popupWindowURL("help", 700, 600, HELP_PATH + LC + ".html",
                         "toolbar=yes,menubar=no,scrollbars=yes,resizable=yes");
                 Analytics.trackEvent("Page", "view", "help");
             }
         });
 
         // TODO translate
         addSidebarHeader("Features");
         final CheckBox flash = new CheckBox("Flash");
         flash.setValue(use_flash);
         addSidebarItem(flash);
 
         flash.addClickHandler(new ClickHandler() {
             public void onClick(ClickEvent event) {
                 use_flash = flash.getValue();
                 History.fireCurrentHistoryState();
             }
         });
 
         Label lastupdated = new Label(Labels.INSTANCE.updated() + ": "
                 + Util.appLastModified());
         lastupdated.setStylePrimaryName("SidebarLastUpdated");
         sidebar.add(lastupdated);
     }
 
     private void initDisplay(String title, boolean displaytitleincontent) {
         Window.setTitle("Roman de la Rose: " + title);
 
         content.clear();
 
         if (displaytitleincontent) {
             Label label = new Label(title);
             label.setStylePrimaryName("ContentTitle");
             content.add(label);
         }
 
         setupSidebar();
     }
 
     private void viewBook() {
         initDisplay(col.fullBookName(book.bookDataIndex()), true);
         content.add(book.displayDescription(col));
     }
 
     private void viewBookPicker(Repository.Category cat) {
         book = null;
         initDisplay(Labels.INSTANCE.selectBook(), true);
 
         content.add(createBookPicker(col, cat));
     }
 
     private static Widget contentHeader(String s) {
         return label(s, "ContentHeader");
     }
 
     private static Widget label(String s, String style) {
         Widget w = new Label(s);
         w.setStylePrimaryName(style);
         return w;
     }
 
     private static Widget createBookPicker(final Repository col,
             Repository.Category category) {
         final TreeMap<String, List<Integer>> entries = col.browse(category);
 
         Cell<String> datacell = new AbstractCell<String>() {
             public void render(Cell.Context context, String value,
                     SafeHtmlBuilder sb) {
 
                 if (value != null) {
                     sb.appendEscaped(value);
 
                     int count = entries.get(value).size();
 
                     if (count > 1) {
                         sb.appendEscaped(" (" + count + ")");
                     }
                 }
             }
         };
 
         final CellList<String> datalist = new CellList<String>(datacell);
         datalist.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.BOUND_TO_SELECTION);
 
         List<String> keys = new ArrayList<String>(entries.keySet());
         datalist.setRowData(keys);
 
         Cell<Integer> booknamecell = new AbstractCell<Integer>() {
             public void render(Cell.Context context, Integer bookid,
                     SafeHtmlBuilder sb) {
 
                 if (bookid != null) {
                     String id = col.bookData(bookid, Repository.Category.ID);
                     String fullname = col.fullBookName(bookid);
                     String a = "<a href='#"
                             + URL.encode(Action.VIEW_BOOK.toToken(id)) + "'>";
 
                     // TODO use template
                     sb.append(SafeHtmlUtils.fromTrustedString(a));
                     sb.appendEscaped(fullname);
                     sb.append(SafeHtmlUtils.fromTrustedString("</a>"));
                 }
             }
         };
 
         final CellList<Integer> booklist = new CellList<Integer>(booknamecell);
         booklist.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);
 
         final SingleSelectionModel<String> datalistsel = new SingleSelectionModel<String>();
         datalist.setSelectionModel(datalistsel);
 
         datalistsel
                 .addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
                     public void onSelectionChange(SelectionChangeEvent event) {
                         String selected = datalistsel.getSelectedObject();
 
                         if (selected != null) {
                             booklist.setRowData(entries.get(selected));
                         }
                     }
                 });
 
         SplitLayoutPanel split = new SplitLayoutPanel();
         split.setStylePrimaryName("BookPicker");
 
         FlowPanel left = new FlowPanel();
         left.add(label(category.display(), "BookPicker-header"));
         left.add(new ScrollPanel(datalist));
 
         FlowPanel right = new FlowPanel();
         right.add(label(Labels.INSTANCE.book(), "BookPicker-header"));
         right.add(new ScrollPanel(booklist));
 
         // TODO would be nice to do this by percentage...
         split.addWest(left, 300);
         split.add(right);
 
         datalist.getParent().setStylePrimaryName("BookPicker-list");
         booklist.getParent().setStylePrimaryName("BookPicker-list");
 
         // Setting focus always needs to be a deferred command
         Scheduler.get().scheduleDeferred(new ScheduledCommand() {
             public void execute() {
                 if (datalist.getVisibleItemCount() > 0) {
                     datalist.getSelectionModel().setSelected(
                             datalist.getVisibleItem(0), true);
                     datalist.setFocus(true);
                 }
             }
         });
 
         return split;
     }
 
     private void viewHome() {
         book = null;
         initDisplay(Labels.INSTANCE.mainPage(), false);
 
         HTML w = new HTML(Resources.INSTANCE.homeHtml().getText());
         w.setStylePrimaryName("Home");
         w.setWordWrap(true);
         content.add(w);
     }
 
     private void viewNarrativeSections() {
         book = null;
         initDisplay(Labels.INSTANCE.narrativeSections(), true);
 
         if (narsecs == null) {
             loadingdialog.display();
 
             try {
                 Resources.INSTANCE.narrativeSectionsTable().getText(
                         new ResourceCallback<TextResource>() {
                             public void onSuccess(TextResource resource) {
                                 loadingdialog.hide();
                                 narsecs = new NarrativeSectionsTable(resource
                                         .getText());
                                 viewNarrativeSections();
                             }
 
                             public void onError(ResourceException e) {
                                 loadingdialog.hide();
                                 reportInternalError(
                                         "Failed to load external resource", e);
                             }
                         });
             } catch (ResourceException e) {
                 loadingdialog.hide();
                 reportInternalError("Failed to load external resource", e);
             }
         } else {
             addHtml(content, Resources.INSTANCE.narrativeSectionsHtml(),
                     new Anchor(Labels.INSTANCE.viewInGoogleDocs(),
                             VIEW_NARRATIVE_SECTIONS_URL), narsecs);
         }
     }
 
     private void viewCorpus() {
         book = null;
         initDisplay(Labels.INSTANCE.roseCorpus(), true);
 
         addHtml(content,
                 Resources.INSTANCE.roseCorpusHtml(),
                 new Anchor(Labels.INSTANCE.viewInGoogleDocs(), VIEW_CORPUS_URL),
                 new HTML(
                         "<iframe width='100%' height='600px' frameborder='0' src='https://spreadsheets0.google.com/pub?hl=en&hl=en&key=0AsygG-3xkhMdcHFwWTFJVlZCeS1BM0FMZ0F6ZVBSU0E&single=true&gid=0&output=html&widget=true'></iframe>"));
     }
 
     private void viewCollectionData() {
         book = null;
         initDisplay(Labels.INSTANCE.collectionData(), true);
 
         if (coldata == null) {
             loadingdialog.display();
 
             try {
                 Resources.INSTANCE.collectionDataTable().getText(
                         new ResourceCallback<TextResource>() {
                             public void onSuccess(TextResource resource) {
                                 loadingdialog.hide();
                                 coldata = new CollectionDataTable(resource
                                         .getText());
                                 viewCollectionData();
                             }
 
                             public void onError(ResourceException e) {
                                 loadingdialog.hide();
                                 reportInternalError(
                                         "Failed to load external resource", e);
                             }
                         });
             } catch (ResourceException e) {
                 loadingdialog.hide();
                 reportInternalError("Failed to load external resource", e);
             }
         } else {
             // Hack to make spreadsheet downloadable
             String name = "collection_data.csv";
 
             if (LC.equals("fr")) {
                 name = "collection_data_fr.csv";
             }
 
             addHtml(content,
                    Resources.INSTANCE.narrativeSectionsHtml(),
                     new Anchor(Labels.INSTANCE.download(), GWT
                             .getHostPageBaseURL() + DATA_PATH + name), coldata);
         }
     }
 
     private void viewIllustrationTitles() {
         book = null;
         initDisplay(Labels.INSTANCE.illustrationTitles(), true);
 
         if (illustitles == null) {
             loadingdialog.display();
 
             try {
                 Resources.INSTANCE.illustrationTitlesTable().getText(
                         new ResourceCallback<TextResource>() {
                             public void onSuccess(TextResource resource) {
                                 loadingdialog.hide();
                                 illustitles = new IllustrationTitlesTable(
                                         resource.getText());
                                 viewIllustrationTitles();
                             }
 
                             public void onError(ResourceException e) {
                                 loadingdialog.hide();
                                 reportInternalError(
                                         "Failed to load external resource", e);
                             }
                         });
             } catch (ResourceException e) {
                 loadingdialog.hide();
                 reportInternalError("Failed to load external resource", e);
             }
         } else {
             addHtml(content, Resources.INSTANCE.illustrationTitlesHtml(),
                     illustitles);
         }
     }
 
     private void viewCharacterNames() {
         book = null;
         initDisplay(Labels.INSTANCE.characterNames(), true);
 
         if (charnames == null) {
             loadingdialog.display();
 
             try {
                 Resources.INSTANCE.characterNamesTable().getText(
                         new ResourceCallback<TextResource>() {
                             public void onSuccess(TextResource resource) {
                                 loadingdialog.hide();
 
                                 charnames = new CharacterNamesTable(resource
                                         .getText());
                                 charnames_variants = charnames
                                         .asSearchVariants();
                                 viewCharacterNames();
                             }
 
                             public void onError(ResourceException e) {
                                 loadingdialog.hide();
                                 reportInternalError(
                                         "Failed to load external resource", e);
                             }
                         });
             } catch (ResourceException e) {
                 loadingdialog.hide();
                 reportInternalError("Failed to load external resource", e);
             }
         } else {
             addHtml(content, Resources.INSTANCE.characterNamesHtml(), charnames);
         }
     }
 
     private void viewBookBibliography() {
         initDisplay(Labels.INSTANCE.bibliography(), true);
 
         if (book.bibliography() == null) {
             loadingdialog.display();
 
             String url = GWT.getHostPageBaseURL() + DATA_PATH
                     + book.bibliographyPath();
 
             HttpGet.request(url, new HttpGet.Callback<String>() {
                 public void failure(String error) {
                     loadingdialog.error(error);
                 }
 
                 public void success(String data) {
                     loadingdialog.hide();
                     book.setBibliography(data);
                     viewBookBibliography();
                 }
             });
         } else {
             content.add(book.bibliography().display());
         }
     }
 }
