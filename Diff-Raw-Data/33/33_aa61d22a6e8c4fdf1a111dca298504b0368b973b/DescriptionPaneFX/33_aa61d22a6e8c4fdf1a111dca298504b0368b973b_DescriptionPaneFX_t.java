 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package Sirius.navigator.ui;
 
 import javafx.application.Platform;
 
 import javafx.embed.swing.JFXPanel;
 
 import javafx.geometry.Insets;
 
 import javafx.scene.Scene;
 import javafx.scene.layout.BorderPane;
 import javafx.scene.layout.Pane;
 import javafx.scene.web.WebEngine;
 import javafx.scene.web.WebView;
 
 import org.apache.log4j.Logger;
 
 import javax.swing.SwingUtilities;
 
 /**
 * An implementation of DescriptionPane which uses JavaFX WebKit Component to render XHTML content.
  *
  * @author   daniel
  * @version  $Revision$, $Date$
  */
 public class DescriptionPaneFX extends DescriptionPane {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static JFXPanel browserFxPanel;
     private static final Logger LOG = Logger.getLogger(DescriptionPaneFX.class);
 
     //~ Instance fields --------------------------------------------------------
 
     private WebEngine webEng = null;
     private WebView webView;
     private Pane browserPane;
 //    private NavigatorJsBridgeImpl bridge = new NavigatorJsBridgeImpl();
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new DescriptionPaneFX object.
      */
     public DescriptionPaneFX() {
         LOG.fatal("JavaFX WebView Description Pane");
         Platform.setImplicitExit(false);
         browserFxPanel = new JFXPanel();
         Platform.runLater(new Runnable() {
 
                 @Override
                 public void run() {
                     browserPane = createBrowser();
                     final Scene scene = new Scene(browserPane);
                     browserFxPanel.setScene(scene);
                 }
             });
         add(browserFxPanel, "html");
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * can only be called on FX application Thread !
      *
      * @return  DOCUMENT ME!
      */
     private Pane createBrowser() {
         webView = new WebView();
 //         disabling the context menue
         // custom context menue are not possible atm see https://javafx-jira.kenai.com/browse/RT-20306
         webView.setContextMenuEnabled(false);
 
         // disabling scoll bars
 // view.getChildrenUnmodifiable().addListener(new ListChangeListener<Node>() {
 // public void onChanged(ListChangeListener.Change<? extends Node> change) {
 // Set<Node> deadSeaScrolls = view.lookupAll(".scroll-bar");
 // for (Node scroll : deadSeaScrolls) {
 // scroll.setVisible(false);
 // }
 // }
 // });
 
         webEng = webView.getEngine();
         final BorderPane pane = new BorderPane();
         pane.setPadding(new Insets(5));
         pane.setCenter(webView);
         return pane;
     }
 
     @Override
     public void setPageFromURI(final String page) {
         Platform.runLater(new Runnable() {
 
                 @Override
                 public void run() {
                     webEng.load(page);
                     SwingUtilities.invokeLater(new Runnable() {
 
                             @Override
                             public void run() {
                                 DescriptionPaneFX.this.invalidate();
                                 DescriptionPaneFX.this.repaint();
                             }
                         });
                 }
             });
     }
 
     @Override
     public void setPageFromContent(final String page) {
         Platform.runLater(new Runnable() {
 
                 @Override
                 public void run() {
                     webEng.loadContent(page);
                 }
             });
     }
 
     @Override
     public void setPageFromContent(final String page,
             final String baseURL) {
         Platform.runLater(new Runnable() {
 
                 @Override
                 public void run() {
                     webEng.load(page);
                 }
             });
     }
 }
