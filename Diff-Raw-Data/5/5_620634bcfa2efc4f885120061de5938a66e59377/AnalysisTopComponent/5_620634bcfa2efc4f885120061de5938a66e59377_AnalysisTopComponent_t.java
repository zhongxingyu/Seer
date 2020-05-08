 package org.purview.ui.analyse;
 
 import java.awt.BorderLayout;
 import java.awt.image.BufferedImage;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import javax.swing.BoxLayout;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.SwingUtilities;
 import org.openide.util.ImageUtilities;
 import org.openide.util.NbBundle;
 import org.openide.windows.Mode;
 import org.openide.windows.TopComponent;
 import org.purview.core.analysis.Analyser;
 import org.purview.core.data.Color;
 import org.purview.core.data.Matrix;
 import org.purview.core.report.Message;
 import org.purview.core.report.ReportEntry;
 import org.purview.core.session.AnalysisSession;
 import org.purview.core.session.AnalysisStats;
 import org.purview.core.transforms.ImageToMatrix;
 import scala.collection.Iterator;
 import scala.collection.JavaConversions;
 import scala.collection.Set;
 
 /**
  * Top component which displays something.
  */
 final class AnalysisTopComponent extends TopComponent implements Runnable {
 
     private static final String ICON_PATH = "org/purview/ui/analyse/analyse.png";
     private static final String PREFERRED_ID = "AnalysisTopComponent";
     private static final String analyserDescr = NbBundle.getMessage(AnalysisTopComponent.class, "LBL_Analyser") + ": ";
     private static final String statusDescr = NbBundle.getMessage(AnalysisTopComponent.class, "LBL_Status") + ": ";
     private static final String runningAnalyser = NbBundle.getMessage(AnalysisTopComponent.class, "LBL_RunningAnalyser");
     private final JScrollPane listScroller = new JScrollPane();
     private final JProgressBar progressBar = new JProgressBar();
     private final JProgressBar subProgressBar = new JProgressBar();
     private final JTextArea statusList = new JTextArea();
     private final AnalysisSession session;
     private final BufferedImage image;
     private final String imageName;
 
     public AnalysisTopComponent(final String imageName, final BufferedImage image,
             final List<Analyser<Matrix<Color>>> analysers) {
         initComponents();
         setName(NbBundle.getMessage(AnalysisTopComponent.class, "CTL_AnalysisTopComponent", imageName));
         setToolTipText(NbBundle.getMessage(AnalysisTopComponent.class, "HINT_AnalysisTopComponent"));
         setIcon(ImageUtilities.loadImage(ICON_PATH, true));
 
         final Matrix<Color> matrix = new ImageToMatrix().apply(image);
         session = new AnalysisSession<Matrix<Color>>(
                 JavaConversions.asBuffer(analysers).toSeq(),
                 matrix);
         this.image = image;
         this.imageName = imageName;
     }
 
     public void run() {
         AnalysisStats stats = new AnalysisStats() {
 
             private int oldProgress = -1;
             private int oldSubProgress = -1;
 
             @Override
             public void reportProgress(final float progress) {
                 final int actualProgress = (int) (1000 * progress);
                 if (actualProgress != oldProgress) {
                     SwingUtilities.invokeLater(new Runnable() {
 
                         public void run() {
                             progressBar.setIndeterminate(false);
                             progressBar.setValue(actualProgress);
                            progressBar.setStringPainted(actualProgress < 1000);
                         }
                     });
                     oldProgress = actualProgress;
                 }
             }
 
             @Override
             public void reportSubProgress(final float progress) {
                 final int actualProgress = (int) (1000 * progress);
                 if (actualProgress != oldSubProgress) {
                     SwingUtilities.invokeLater(new Runnable() {
 
                         public void run() {
                             subProgressBar.setIndeterminate(false);
                             subProgressBar.setValue(actualProgress);
                            progressBar.setStringPainted(actualProgress < 1000);
                         }
                     });
                     oldSubProgress = actualProgress;
                 }
             }
 
             @Override
             public void reportAnalyser(final String analyser) {
                 SwingUtilities.invokeLater(new Runnable() {
 
                     public void run() {
                         progressBar.setString(analyserDescr + analyser);
                         statusList.append("⇒ " + runningAnalyser + ": \"" + analyser + "\"\n");
                     }
                 });
             }
 
             @Override
             public void reportStatus(final String status) {
                 SwingUtilities.invokeLater(new Runnable() {
 
                     public void run() {
                         subProgressBar.setString(statusDescr + status);
                         statusList.append(status + "\n");
                     }
                 });
             }
         };
         final scala.collection.Map<Analyser<Matrix<Color>>, Set<ReportEntry>> results = session.run(stats);
         stats.reportStatus("Done"); //TODO: translate
         final Iterator<Analyser<Matrix<Color>>> analyserIter = results.keySet().iterator();
         final Map<Analyser<Matrix<org.purview.core.data.Color>>, List<ReportEntry>> report =
                 new HashMap<Analyser<Matrix<org.purview.core.data.Color>>, List<ReportEntry>>();
 
         while (analyserIter.hasNext()) {
             final Analyser<Matrix<Color>> analyser = analyserIter.next();
             final Set<ReportEntry> resultsForAnalyser = results.apply(analyser);
             final Iterator<ReportEntry> reportIter = resultsForAnalyser.iterator();
             final LinkedList<ReportEntry> entries = new LinkedList<ReportEntry>();
 
             while (reportIter.hasNext()) {
                 entries.add(reportIter.next());
             }
             Collections.sort(entries, new Comparator<ReportEntry>() {
 
                 public int compare(ReportEntry o1, ReportEntry o2) {
                     final int r = o1.level().name().compareTo(o2.level().name());
                     
                     if (r == 0) {
                         return ((Message) o1).message().compareTo(((Message) o2).message());
                     } else {
                         return r;
                     }
                 }
             });
             report.put(analyser, entries);
         }
 
         SwingUtilities.invokeLater(new Runnable() {
 
             public void run() {
                 final ResultsTopComponent resultsComp = new ResultsTopComponent(imageName, image, report);
                 resultsComp.open();
                 resultsComp.requestActive();
             }
         });
     }
 
     /**
      * This method is called from within the constructor to
      * initialize the form.
      */
     private void initComponents() {
         setLayout(new BorderLayout());
 
         progressBar.setMinimum(0);
         progressBar.setMaximum(1000);
         progressBar.setIndeterminate(false);
         progressBar.setStringPainted(true);
         subProgressBar.setMinimum(0);
         subProgressBar.setMaximum(1000);
         subProgressBar.setIndeterminate(false);
         subProgressBar.setStringPainted(true);
 
         final JPanel container = new JPanel();
         container.setLayout(new BoxLayout(container, BoxLayout.PAGE_AXIS));
         container.add(progressBar);
         container.add(subProgressBar);
         add(container, BorderLayout.PAGE_END);
 
         statusList.setEditable(false);
         listScroller.setViewportView(statusList);
 
         add(listScroller, BorderLayout.CENTER);
     }
 
     @Override
     public int getPersistenceType() {
         return TopComponent.PERSISTENCE_NEVER;
     }
 
     @Override
     protected String preferredID() {
         return PREFERRED_ID;
     }
 
     @Override
     public List<Mode> availableModes(final List<Mode> input) {
         final LinkedList<Mode> result = new LinkedList<Mode>();
         for (final Mode mode : input) {
             if (mode.getName().equals("output") || mode.getName().endsWith("NewMode")) {
                 result.add(mode);
             }
         }
         return result;
     }
 }
