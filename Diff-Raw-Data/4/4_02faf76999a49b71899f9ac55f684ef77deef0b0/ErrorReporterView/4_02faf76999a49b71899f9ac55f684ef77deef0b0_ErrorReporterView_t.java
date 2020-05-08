 package org.isatools.errorreporter.ui;
 
 import com.explodingpixels.macwidgets.IAppWidgetFactory;
 import org.isatools.errorreporter.html.ErrorMessageWriter;
 import org.isatools.errorreporter.model.ErrorLevel;
 import org.isatools.errorreporter.model.ErrorMessage;
 import org.isatools.errorreporter.model.ISAFileErrorReport;
 import org.isatools.errorreporter.model.FileType;
 import org.isatools.errorreporter.ui.borders.RoundedBorder;
 import org.isatools.errorreporter.ui.utils.UIHelper;
 
 import javax.swing.*;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.TitledBorder;
 import java.awt.*;
 
 import org.jdesktop.fuse.*;
 import org.jdesktop.fuse.ResourceInjector;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.*;
 import java.util.List;
 
 public class ErrorReporterView extends JPanel {
 
     static {
         ResourceInjector.addModule("org.jdesktop.fuse.swing.SwingModule");
 
         ResourceInjector.get("error-gui-package.style").load(
                 ErrorReporterView.class.getResource("/dependency-injections/error-gui-package.properties"));
     }
 
     @InjectedResource
     private ImageIcon isatabLogo;
 
     private JPanel assayContainer;
 
     private JLabel fileInfoText;
 
     private ErrorMessageWriter errorMessageWriter;
     private JEditorPane messagePane;
 
     private List<ISAFileErrorReport> errorReports;
     private boolean showHeaderLogo;
 
     public ErrorReporterView(List<ISAFileErrorReport> errorReports) {
         this(errorReports, false);
     }
 
     public ErrorReporterView(List<ISAFileErrorReport> errorReports, boolean showHeaderLogo) {
         this.showHeaderLogo = showHeaderLogo;
         ResourceInjector.get("error-gui-package.style").inject(this);
 
         this.errorReports = errorReports;
     }
 
 
     public void createGUI() {
         setLayout(new BorderLayout());
         setOpaque(false);
 
         createList();
         createMessagesPanel();
        if (errorReports.size() > 0) {
            updateMessageListContent(errorReports.get(0));
        }
     }
 
     private void createList() {
 
         assayContainer = new JPanel(new FlowLayout(FlowLayout.LEFT));
         assayContainer.setBackground(UIHelper.BG_COLOR);
 
         errorMessageWriter = new ErrorMessageWriter();
 
         for (ISAFileErrorReport errorReport : errorReports) {
             ErrorInformationPanel errorInformationPanel = new ErrorInformationPanel(errorReport);
 
             errorInformationPanel.addPropertyChangeListener("errorSelected", new PropertyChangeListener() {
                 public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                     if (propertyChangeEvent.getNewValue() instanceof ISAFileErrorReport) {
                         ISAFileErrorReport errorReport = (ISAFileErrorReport) propertyChangeEvent.getNewValue();
 
                         updateMessageListContent(errorReport);
                     }
                 }
             });
 
             assayContainer.add(errorInformationPanel);
         }
 
         JScrollPane scroller = new JScrollPane(assayContainer,
                 JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 
         scroller.getViewport().setOpaque(false);
         scroller.setOpaque(false);
         scroller.setBorder(new EmptyBorder(1, 1, 1, 1));
         scroller.setPreferredSize(new Dimension(400, 110));
 
         IAppWidgetFactory.makeIAppScrollPane(scroller);
 
         Box container = Box.createVerticalBox();
         container.setOpaque(false);
 
         if (showHeaderLogo) {
             JPanel iconContainer = new JPanel(new BorderLayout());
             iconContainer.setOpaque(false);
             iconContainer.add(new JLabel(isatabLogo), BorderLayout.EAST);
 
             container.add(iconContainer);
         }
 
         container.add(UIHelper.createLabel("<html><b>Click on one of the boxes below</b> to view it's errors and warnings.</html>", UIHelper.VER_10_PLAIN, UIHelper.DARK_GREEN_COLOR, SwingConstants.LEFT));
         container.add(Box.createVerticalStrut(5));
         container.add(scroller);
         container.add(Box.createVerticalGlue());
 
         add(container, BorderLayout.NORTH);
     }
 
     private void createMessagesPanel() {
         Box infoBox = Box.createVerticalBox();
         infoBox.setOpaque(false);
         infoBox.add(Box.createVerticalStrut(10));
         fileInfoText = UIHelper.createLabel("", UIHelper.VER_10_BOLD, UIHelper.DARK_GREEN_COLOR, SwingConstants.LEFT);
         infoBox.add(fileInfoText);
         infoBox.add(Box.createVerticalStrut(10));
 
         JPanel centralPanel = new JPanel(new BorderLayout());
         centralPanel.setOpaque(false);
         centralPanel.setBorder(null);
 
         centralPanel.add(infoBox, BorderLayout.NORTH);
 
         messagePane = new JEditorPane();
         messagePane.setContentType("text/html");
         messagePane.setBackground(UIHelper.BG_COLOR);
         messagePane.setSelectionColor(UIHelper.LIGHT_GREEN_COLOR);
         messagePane.setSelectedTextColor(UIHelper.BG_COLOR);
         messagePane.setEditable(false);
         messagePane.setBorder(null);
 
         JScrollPane scroller = new JScrollPane(messagePane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
         scroller.setBorder(null);
         scroller.getViewport().setOpaque(false);
         scroller.setOpaque(false);
 
         IAppWidgetFactory.makeIAppScrollPane(scroller);
 
         centralPanel.add(scroller, BorderLayout.CENTER);
 
         add(centralPanel, BorderLayout.CENTER);
     }
 
     private void updateMessageListContent(ISAFileErrorReport errorReport) {
         fileInfoText.setText("Errors and warnings found in " + errorReport.getFileName());
         String html = errorMessageWriter.createHTMLRepresentationOfErrors(errorReport);
         messagePane.setText(html);
     }
 
 
     public static void main(String[] args) {
         JFrame container = new JFrame();
 
         container.setUndecorated(true);
         container.setPreferredSize(new Dimension(400, 400));
 
         container.setBackground(UIHelper.BG_COLOR);
 
         List<ISAFileErrorReport> report = new ArrayList<ISAFileErrorReport>();
 
         List<ErrorMessage> messages = new ArrayList<ErrorMessage>();
 
         messages.add(new ErrorMessage(ErrorLevel.ERROR, "no publication doi field was found in i_investigation.txt"));
         messages.add(new ErrorMessage(ErrorLevel.WARNING, "no publication title field was found in s_investigation.txt, this is quite a disappointing turn of events!"));
         messages.add(new ErrorMessage(ErrorLevel.WARNING, "no publication doi field was found in a_investigation.txt, omg now this list item is going to be a bit bigger! " +
                 "Maybe even bigger? I don't know, it's too big perhaps."));
         messages.add(new ErrorMessage(ErrorLevel.ERROR, "no publication title field was found in s_investigation.txt, this is quite a disappointing turn of events!"));
         messages.add(new ErrorMessage(ErrorLevel.WARNING, "no publication doi field was found in a_investigation.txt, omg now this list item is going to be a bit bigger! " +
                 "Maybe even bigger? I don't know, it's too big perhaps."));
 
 
         ISAFileErrorReport report1 = new ISAFileErrorReport("i_investigation.txt", FileType.INVESTIGATION, messages);
         ISAFileErrorReport report2 = new ISAFileErrorReport("a_microarray.txt", FileType.MICROARRAY, messages);
         ISAFileErrorReport report3 = new ISAFileErrorReport("a_nmr.txt", FileType.NMR, messages);
         ISAFileErrorReport report4 = new ISAFileErrorReport("s_sample.txt", FileType.STUDY_SAMPLE, messages);
 
         report.add(report1);
         report.add(report2);
         report.add(report3);
         report.add(report4);
 
         ErrorReporterView view = new ErrorReporterView(report);
         view.createGUI();
 
         container.add(view);
         container.pack();
 
         container.setVisible(true);
     }
 }
