 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.custom.visualdiff;
 
 import org.netbeans.api.diff.Diff;
 import org.netbeans.api.diff.DiffView;
 import org.netbeans.api.diff.Difference;
 import org.netbeans.api.diff.StreamSource;
 
 import java.awt.BorderLayout;
 import java.awt.CardLayout;
 import java.awt.Container;
 import java.awt.EventQueue;
 
 import java.io.IOException;
 import java.io.Reader;
 import java.io.StringReader;
 import java.io.Writer;
 
 import javax.swing.SwingWorker;
 
 /**
  * This panel allows the embedding of Netbeans' diff component.
  *
  * @version  $Revision$, $Date$
  */
 public class DiffPanel extends javax.swing.JPanel {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(DiffPanel.class);
 
     //~ Instance fields --------------------------------------------------------
 
     protected DiffView view;
     protected FileToDiff left;
     protected FileToDiff right;
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JLabel lblWaitingImage;
     private javax.swing.JPanel pnlDiff;
     private javax.swing.JPanel pnlWaiting;
     // End of variables declaration//GEN-END:variables
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new DiffPanel object.
      */
     public DiffPanel() {
         initComponents();
         // showWaiting();
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * Starts the retrieval and embedding of a new diff component in a SwingWorker. While the differences of both files
      * are computed, a "please wait" image is displayed. When the SwingWorker got a new diff component, it's embedded
      * and displayed.
      */
     public void update() {
         showWaiting();
         if ((left == null) || (right == null)) {
             LOG.warn("At least one file is null. The diff component can't be created.");
             return;
         }
 
         new SwingWorker<DiffView, Void>() {
 
                 @Override
                 protected DiffView doInBackground() throws Exception {
                     final StreamSource sourceLeft = new MyStreamSource(left);
                     final StreamSource sourceRight = new MyStreamSource(right);
 
                     return Diff.getDefault().createDiff(sourceLeft, sourceRight);
                 }
 
                 @Override
                 protected void done() {
                     try {
                         view = get();
                         pnlDiff.removeAll();
                         pnlDiff.add(view.getComponent(), BorderLayout.CENTER);
                         showDiff();
                     } catch (Exception e) {
                         LOG.error("Could not update diff component.", e);
                     }
                 }
             }.execute();
     }
 
     /**
      * Starts a new Runnable which shows the waiting screen.
      */
    public void showWaiting() {
         final Runnable waitRunnable = new ShowCardRunnable(this, "waiting"); // NOI18N
 
         if (EventQueue.isDispatchThread()) {
             waitRunnable.run();
         } else {
             EventQueue.invokeLater(waitRunnable);
         }
     }
 
     /**
      * Starts a new Runnable which shows the diff component.
      */
    public void showDiff() {
         final Runnable diffRunnable = new ShowCardRunnable(this, "diff"); // NOI18N
 
         if (EventQueue.isDispatchThread()) {
             diffRunnable.run();
         } else {
             EventQueue.invokeLater(diffRunnable);
         }
     }
 
     /**
      * Sets the information for the left part of the diff component. The DiffPanel will be updated.
      *
      * @param  content   The content of the file to be shown on the left side.
      * @param  mimetype  The mimetype of the file to be shown on the left side.
      * @param  title     The title of the file to be shown on the left side.
      */
     public void setLeft(final String content, final String mimetype, final String title) {
         this.left = createFileToDiff(content, mimetype, title);
         update();
     }
 
     /**
      * Sets the information for the right part of the diff component. The DiffPanel will be updated.
      *
      * @param  content   The content of the file to be shown on the right side.
      * @param  mimetype  The mimetype of the file to be shown on the right side.
      * @param  title     The title of the file to be shown on the right side.
      */
     public void setRight(final String content, final String mimetype, final String title) {
         this.right = createFileToDiff(content, mimetype, title);
         update();
     }
 
     /**
      * Sets the information for both parts of the diff component. The DiffPanel will be updated.
      *
      * @param  contentLeft    The content of the file to be shown on the left side.
      * @param  mimetypeLeft   The mimetype of the file to be shown on the left side.
      * @param  titleLeft      The title of the file to be shown on the left side.
      * @param  contentRight   The content of the file to be shown on the right side.
      * @param  mimetypeRight  The mimetype of the file to be shown on the right side.
      * @param  titleRight     The title of the file to be shown on the right side.
      */
     public void setLeftAndRight(final String contentLeft,
             final String mimetypeLeft,
             final String titleLeft,
             final String contentRight,
             final String mimetypeRight,
             final String titleRight) {
         this.left = createFileToDiff(contentLeft, mimetypeLeft, titleLeft);
         this.right = createFileToDiff(contentRight, mimetypeRight, titleRight);
         update();
     }
 
     /**
      * Gives access to Netbeans' diff component.
      *
      * @return  Netbeans' diff component.
      */
     public DiffView getDiffView() {
         return view;
     }
 
     /**
      * Helper method to verify if a given content is valid. Content is valid, if it contains at least one character.
      *
      * @param   content  The content to verify.
      *
      * @return  A flag indicating whether the given content is valid or not.
      */
     protected static boolean isValidContent(final String content) {
         return (content != null) && (content.trim().length() > 0);
     }
 
     /**
      * A factory method to create FileToDiff objects. Returns null if the given content is not valid.
      *
      * @param   content   The content of the FileToDiff.
      * @param   mimetype  The mimetype of the FileToDiff.
      * @param   title     The title of the FileToDiff.
      *
      * @return  A FileToDiff object wrapping the given parameters or null if the given content is invalid.
      */
     protected static FileToDiff createFileToDiff(final String content, final String mimetype, final String title) {
         if (isValidContent(content)) {
             return new FileToDiff(content, mimetype, title);
         }
 
         return null;
     }
 
     /**
      * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
      * content of this method is always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         pnlWaiting = new javax.swing.JPanel();
         lblWaitingImage = new javax.swing.JLabel();
         pnlDiff = new javax.swing.JPanel();
 
         setLayout(new java.awt.CardLayout());
 
         pnlWaiting.setLayout(new java.awt.BorderLayout());
 
         lblWaitingImage.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         lblWaitingImage.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/custom/visualdiff/load.png"))); // NOI18N
         pnlWaiting.add(lblWaitingImage, java.awt.BorderLayout.CENTER);
 
         add(pnlWaiting, "waiting");
 
         pnlDiff.setLayout(new java.awt.BorderLayout());
         add(pnlDiff, "diff");
     } // </editor-fold>//GEN-END:initComponents
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * A helper class to switch between both cards of its parent's layout.
      *
      * @version  $Revision$, $Date$
      */
     protected class ShowCardRunnable implements Runnable {
 
         //~ Instance fields ----------------------------------------------------
 
         private Container parent;
         private String cardToShow;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new ShowCardRunnable object.
          *
          * @param  parent      The parent of this helper. Should be a DiffPanel object.
          * @param  cardToShow  The card to display.
          */
         public ShowCardRunnable(final Container parent, final String cardToShow) {
             this.parent = parent;
             this.cardToShow = cardToShow;
         }
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public void run() {
             if (parent.getLayout() instanceof CardLayout) {
                 ((CardLayout)parent.getLayout()).show(parent, cardToShow);
             }
         }
     }
 
     /**
      * A wrapper class for the necessary information of the left or right part of Netbeans' diff component.
      *
      * @version  $Revision$, $Date$
      */
     protected static class FileToDiff {
 
         //~ Instance fields ----------------------------------------------------
 
         private String content;
         private String mimetype;
         private String title;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new FileToDiff object.
          *
          * @param  content   The content to diff.
          * @param  mimetype  The mimetype of the content.
          * @param  title     The title to display.
          */
         public FileToDiff(final String content, final String mimetype, final String title) {
             this.content = content;
             this.mimetype = mimetype;
             this.title = title;
         }
 
         //~ Methods ------------------------------------------------------------
 
         /**
          * DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          */
         public String getContent() {
             return content;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param  content  DOCUMENT ME!
          */
         public void setContent(final String content) {
             this.content = content;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          */
         public String getMimetype() {
             return mimetype;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param  mimetype  DOCUMENT ME!
          */
         public void setMimetype(final String mimetype) {
             this.mimetype = mimetype;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          */
         public String getTitle() {
             return title;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param  title  DOCUMENT ME!
          */
         public void setTitle(final String title) {
             this.title = title;
         }
     }
 
     /**
      * Custom StreamSource implementation which handles FileToDiff objects.
      *
      * @version  $Revision$, $Date$
      */
     protected static class MyStreamSource extends StreamSource {
 
         //~ Instance fields ----------------------------------------------------
 
         private FileToDiff fileToDiff;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new MyStreamSource object.
          *
          * @param  fileToDiff  The FileToDiff object to wrap.
          */
         public MyStreamSource(final FileToDiff fileToDiff) {
             this.fileToDiff = fileToDiff;
         }
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public String getName() {
             return "name";
         }
 
         @Override
         public String getTitle() {
             return fileToDiff.getTitle();
         }
 
         @Override
         public String getMIMEType() {
             return fileToDiff.getMimetype();
         }
 
         @Override
         public Reader createReader() throws IOException {
             return new StringReader(fileToDiff.getContent());
         }
 
         @Override
         public Writer createWriter(final Difference[] conflicts) throws IOException {
             return null;
         }
     }
 }
