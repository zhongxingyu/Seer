 /*
  * Licensed under the Apache License, Version 2.0 (the "License").
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.coderthoughts.phototools.impl.ui.photocopy;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.FlowLayout;
 import java.awt.Window;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusAdapter;
 import java.awt.event.FocusEvent;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTextField;
 import javax.swing.SwingUtilities;
 import javax.swing.border.TitledBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import org.coderthoughts.phototools.api.PhotoIterable;
 import org.coderthoughts.phototools.api.PhotoIterable.Entry;
 import org.coderthoughts.phototools.api.PhotoMetadataProvider;
 import org.coderthoughts.phototools.api.PhotoSource;
 import org.coderthoughts.phototools.api.ToolPanel;
 import org.coderthoughts.phototools.impl.photocopy.Copier;
 import org.coderthoughts.phototools.util.DirTreeIterable;
 import org.coderthoughts.phototools.util.DirectoryPhotoIterable;
 import org.coderthoughts.phototools.util.OSGiTools;
 import org.coderthoughts.phototools.util.ui.UIUtils;
 import org.coderthoughts.phototools.util.ui.WrappingFlowLayout;
 import org.jdesktop.swingx.JXDatePicker;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.BundleException;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceReference;
 
 public class PhotoCopyToolPanel implements ToolPanel {
     static final SimpleDateFormat DATE_PICKER_FORMAT = new SimpleDateFormat("dd-MM-yyyy");
 
     private static final String PREFERENCE_FILENAME = "photocopy.storage";
     private static final String PREFERENCE_KEY_FROM_DATE = "fromMillis";
     private static final String PREFERENCE_KEY_TO_DATE = "toMillis";
     private static final String PREFERENCE_KEY_SOURCE_LOCATION = "sourceLocation";
     private static final String PREFERENCE_KEY_SOURCE_TYPE = "sourceType";
     private static final String PREFERENCE_KEY_TARGET_DIRECTORY = "targetDirectory";
 
     public static final String DEFAULT_TARGET_DATE_STRUCTURE = "yyyy/yyyy-MM-dd/";
 
 
     private final BundleContext bundleContext;
     private Collection<String> selectedImageNames;
     private JPanel sourceImagePNL;
     private JPanel targetImagePNL;
     private JTextField sourceTF;
     private JTextField targetTF;
     private PhotoIterable sourceIterable;
     private JComponent thePanel;
     private JSplitPane previewSplitPane;
     private JSplitPane toolSplitPane;
 
     private JXDatePicker fromDatePicker;
     private JXDatePicker toDatePicker;
 
     public PhotoCopyToolPanel(BundleContext ctx) {
         bundleContext = ctx;
     }
 
     @Override
     public String getTitle() {
         return "Photo Copy";
     }
 
     @Override
     public Component getPanel(final Window parentWindow) {
         JPanel panel = new JPanel();
         panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
 
         JPanel left = new JPanel(new BorderLayout());
         JPanel leftContents = new JPanel();
         leftContents.setLayout(new BoxLayout(leftContents, BoxLayout.Y_AXIS));
         left.add(leftContents, BorderLayout.NORTH); // add to BorderLayout.NORTH to keep contents compact
 
         JPanel sourcePanel = addSelectionPanel(leftContents);
         JPanel targetPanel = addSelectionPanel(leftContents);
 
         sourceImagePNL = new JPanel();
         JComponent sourcePreviewPane = getImagePreviewPanel(sourceImagePNL, "Source:");
 
         targetImagePNL = new JPanel();
         JComponent targetPreviewPane = getImagePreviewPanel(targetImagePNL, "Target:");
 
         addSourceGroup(parentWindow, sourcePanel);
         addTargetGroup(parentWindow, targetPanel);
 
         previewSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, sourcePreviewPane, targetPreviewPane);
 
         JScrollPane leftScroller = new JScrollPane(left, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
         toolSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScroller, previewSplitPane);
         toolSplitPane.setOneTouchExpandable(true);
         panel.add(toolSplitPane);
 
         JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
         buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
         panel.add(buttonPanel);
 
         JButton copyBTN = new JButton("Copy!");
         copyBTN.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 String errorMsg = null;
                 if (sourceIterable == null) {
                     errorMsg = "No source specified.";
                 }
                 if (!checkDir(targetTF.getText())) {
                     errorMsg = "Directory doesn't exist: " + targetTF.getText();
                 }
                 if (errorMsg == null) {
                     parentWindow.setVisible(false);
                     runCopyOperation();
                 } else {
                     JOptionPane.showMessageDialog(parentWindow, errorMsg,
                             "Problem starting copy operation", JOptionPane.ERROR_MESSAGE);
                 }
             }
         });
         buttonPanel.add(copyBTN);
 
         thePanel = panel;
         return thePanel;
     }
 
     @Override
     public void postLayout(Window parentWindow) {
         // previewSplitPane.setDividerLocation((int) (thePanel.getHeight() * 0.55));
         previewSplitPane.setDividerLocation(250);
         toolSplitPane.setDividerLocation((int) (thePanel.getWidth() * 0.6));
     }
 
     private JComponent getImagePreviewPanel(JPanel panel, String title) {
         panel.setLayout(new BorderLayout());
         panel.add(new JLabel(title), BorderLayout.NORTH);
         JScrollPane scrollPane = new JScrollPane(panel,
                 JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                 JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
         return scrollPane;
     }
 
     private JPanel addSelectionPanel(JPanel left) {
         JPanel panel = new JPanel();
         panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
         JPanel p = new JPanel(new FlowLayout(FlowLayout.LEADING));
         p.setAlignmentX(Component.LEFT_ALIGNMENT);
         p.add(panel);
         left.add(p);
         return panel;
     }
 
     private void addSourceGroup(final Window parentWindow, JPanel panel) {
         TitledBorder sourceBorder = BorderFactory.createTitledBorder("Source");
         sourceBorder.setTitlePosition(TitledBorder.TOP);
         panel.setBorder(sourceBorder);
 
         JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
         btnPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
         panel.add(btnPanel);
         btnPanel.add(new JLabel("Type:"));
 
         final Map<JRadioButton, PhotoSource> sources = new HashMap<JRadioButton, PhotoSource>();
         ButtonGroup group = new ButtonGroup();
         boolean first = true;
         for (ServiceReference ref : OSGiTools.getSortedServiceReferences(bundleContext, PhotoSource.class.getName(), null)) {
             PhotoSource ps = (PhotoSource) bundleContext.getService(ref);
             JRadioButton rb = new JRadioButton(ps.getLabel());
             if (first) {
                 first = false;
                 rb.setSelected(true);
             } else {
                 rb.setSelected(false);
             }
             btnPanel.add(rb);
             group.add(rb);
             sources.put(rb, ps);
         }
 
         sourceTF = addLocationSelector(panel, new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 for (JRadioButton rb : sources.keySet()) {
                     if (rb.isSelected()) {
                         PhotoSource ps = sources.get(rb);
                         PhotoIterable iterable = ps.getPhotoIterable(parentWindow, sourceTF.getText());
                         prepareSourceIterable(iterable);
                         if (iterable != null) {
                             setPreferenceValue(PREFERENCE_KEY_SOURCE_TYPE, ps.getLabel());
                             setPreferenceValue(PREFERENCE_KEY_SOURCE_LOCATION, iterable.getLocationString());
                         }
                     }
                 }
             }
         });
 
         JPanel sourceSelectionPanel = new JPanel(new BorderLayout());
         fromDatePicker = addDateSelector(sourceSelectionPanel, "From:", PREFERENCE_KEY_FROM_DATE);
 
         JPanel sourceSelectionRBPNL = new JPanel(new FlowLayout(FlowLayout.TRAILING));
         ButtonGroup buttonGroup = new ButtonGroup();
         JRadioButton allImages = new JRadioButton("All Images");
         allImages.setSelected(true);
         sourceSelectionRBPNL.add(allImages);
         buttonGroup.add(allImages);
         final JRadioButton selectedImages = new JRadioButton("Selected Images");
         sourceSelectionRBPNL.add(selectedImages);
         buttonGroup.add(selectedImages);
         final JButton selectButton = new JButton("Select...");
         selectButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 selectedImages.setSelected(true);
                 if (sourceIterable == null) {
                     JOptionPane.showMessageDialog(parentWindow, "Please select a source location first.",
                             "No Source Location", JOptionPane.INFORMATION_MESSAGE);
                 } else {
                     Collection<String> selected = SelectImageDialog.selectImages(bundleContext, parentWindow, sourceIterable, fromDatePicker, toDatePicker, selectedImageNames);
                     if (selected != null) {
                         selectedImageNames = selected;
                         updateLocationAsync(sourceImagePNL, sourceIterable);
                     }
                 }
             }
         });
         selectedImages.addChangeListener(new ChangeListener() {
             boolean savedSelectionState = false;
             @Override
             public void stateChanged(ChangeEvent e) {
                 if (selectedImages.isSelected() != savedSelectionState) {
                     savedSelectionState = selectedImages.isSelected();
                     if (selectedImageNames != null) {
                         selectedImageNames = null;
                         updateLocationAsync(sourceImagePNL, sourceIterable);
                     }
                 }
             }
         });
         sourceSelectionRBPNL.add(selectButton);
 
         sourceSelectionPanel.add(sourceSelectionRBPNL, BorderLayout.EAST);
         sourceSelectionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
         panel.add(sourceSelectionPanel);
 
         JPanel sourceSelectionPanel2 = new JPanel(new BorderLayout());
         toDatePicker = addDateSelector(sourceSelectionPanel2, "To:", PREFERENCE_KEY_TO_DATE);
         sourceSelectionPanel2.setAlignmentX(Component.LEFT_ALIGNMENT);
         panel.add(sourceSelectionPanel2);
 
         String storedSourceType = getPreferenceValue(PREFERENCE_KEY_SOURCE_TYPE);
         if (storedSourceType != null) {
             for (JRadioButton rb : sources.keySet()) {
                 PhotoSource ps = sources.get(rb);
                 if (storedSourceType.equals(ps.getLabel())) {
                     rb.setSelected(true);
                     String storedSourceLocation = getPreferenceValue(PREFERENCE_KEY_SOURCE_LOCATION);
                     if (storedSourceLocation != null) {
                         prepareSourceIterable(ps.getPhotoIterableFromLocation(storedSourceLocation));
                     }
                 }
             }
         }
     }
 
     private JXDatePicker addDateSelector(JPanel panel, String text, final String preferenceKey) {
         JPanel dateSelectorPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
         final JLabel label = new JLabel(text);
         dateSelectorPanel.add(label);
         final JXDatePicker picker = new JXDatePicker();
         picker.setEditable(true);
         picker.setFormats(DATE_PICKER_FORMAT);
         picker.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 Date d = picker.getDate();
                 if (d != null)
                     setPreferenceValue(preferenceKey, "" + d.getTime());
                 else
                     setPreferenceValue(preferenceKey, "");
 
                 updateLocationAsync(sourceImagePNL, sourceIterable);
             }
         });
         picker.getEditor().addFocusListener(new FocusAdapter() {
             @Override
             public void focusLost(FocusEvent e) {
                 try {
                     picker.commitEdit();
                 } catch (ParseException pe) {
                 }
             }
         });
         dateSelectorPanel.add(picker);
         panel.add(dateSelectorPanel);
 
         String initialMillis = getPreferenceValue(preferenceKey);
         if (initialMillis != null && initialMillis.length() > 0) {
             try {
                 picker.setDate(new Date(Long.parseLong(initialMillis)));
             } catch (Throwable th) {
                 // too bad, doesn't parse
             }
         }
         return picker;
     }
 
     private void prepareSourceIterable(PhotoIterable iterable) {
         if (iterable != null) {
             iterable.setExtensions(getSupportedFormats());
             iterable.freeze();
 
             sourceIterable = iterable;
             sourceTF.setText(iterable.getLocationString());
             updateLocationAsync(sourceImagePNL, iterable);
         }
     }
 
     private void addTargetGroup(final Window parentWindow, JPanel panel) {
         TitledBorder sourceBorder = BorderFactory.createTitledBorder("Target");
         sourceBorder.setTitlePosition(TitledBorder.TOP);
         panel.setBorder(sourceBorder);
 
         targetTF = addLocationSelector(panel, new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent arg0) {
                 JFileChooser chooser = new JFileChooser(targetTF.getText());
                 chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                 if (chooser.showOpenDialog(parentWindow) == JFileChooser.APPROVE_OPTION) {
                     String path = chooser.getSelectedFile().getAbsolutePath();
                     String newPath = possiblyFixTargetLocation(path);
                     if (!newPath.equals(path)) {
                         switch(JOptionPane.showConfirmDialog(parentWindow, "The selected directory is:\n " + path +
                                 "\nHowever the following nearby directory follows the expected structure:\n " + newPath +
                                 "\nUse that location instead?",
                                 "Possible Directory Mismatch", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE)) {
                         case JOptionPane.CANCEL_OPTION:
                             return;
                         case JOptionPane.YES_OPTION:
                             path = newPath;
                             break;
                         case JOptionPane.NO_OPTION:
                             break;
                         }
                     }
                     targetTF.setText(path);
                     setPreferenceValue(PREFERENCE_KEY_TARGET_DIRECTORY, path);
                     updateLocationAsync(targetImagePNL, new DirectoryPhotoIterable(targetTF.getText()).freeze());
                 }
             }
         });
 
         JPanel targetStructurePanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
         targetStructurePanel.add(new JLabel("Target directory structure:"));
         JTextField targetStructureTF = new JTextField(DEFAULT_TARGET_DATE_STRUCTURE, 25);
         targetStructureTF.setEnabled(false);
         targetStructurePanel.add(targetStructureTF);
         JButton guessButton = new JButton("Check");
         guessButton.setEnabled(false);
         targetStructurePanel.add(guessButton);
         targetStructurePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
         panel.add(targetStructurePanel);
 
         String storedTargetDir = getPreferenceValue(PREFERENCE_KEY_TARGET_DIRECTORY);
         if (storedTargetDir != null) {
             targetTF.setText(storedTargetDir);
             updateLocationAsync(targetImagePNL, new DirectoryPhotoIterable(storedTargetDir).freeze());
         }
     }
 
     private JTextField addLocationSelector(JPanel panel, ActionListener selectAction) {
         JPanel locationPNL = new JPanel(new FlowLayout(FlowLayout.LEADING));
         locationPNL.setAlignmentX(Component.LEFT_ALIGNMENT);
         panel.add(locationPNL);
 
         locationPNL.add(new JLabel("Location:"));
 
         JTextField locationTF = new JTextField(35);
         locationPNL.add(locationTF);
 
         JButton selectBTN = new JButton("Select...");
         selectBTN.addActionListener(selectAction);
         locationPNL.add(selectBTN);
 
         return locationTF;
     }
 
     private String possiblyFixTargetLocation(String path) {
         return possiblyFixTargetLocation(new File(path)).getAbsolutePath();
     }
 
     private File possiblyFixTargetLocation(File dir) {
         if (followsTargetStructure(dir))
             return dir;
 
         File parent = dir.getParentFile();
         if (followsTargetStructure(parent))
             return parent;
 
         File grandparent = parent.getParentFile();
         if (followsTargetStructure(grandparent))
             return grandparent;
 
         for (File f : new DirTreeIterable(dir)) {
             if (followsTargetStructure(f))
                 return f;
         }
         return dir;
     }
 
     private boolean followsTargetStructure(File dir) {
         File[] files = dir.listFiles();
         if (files == null)
             return false;
 
         for (File subdir : files) {
             if (followsTargetSubStructure(subdir))
                 return true;
         }
         return false;
     }
 
     private boolean followsTargetSubStructure(File dir) {
         if (!isYearDir(dir))
             return false;
 
         for (File subdir : dir.listFiles()) {
             if (!subdir.isDirectory())
                 continue;
 
             if (isDateDir(subdir))
                 return true;
         }
         return false;
     }
 
     private boolean isYearDir(File dir) {
         return dir.isDirectory() && dir.getName().matches("\\d\\d\\d\\d");
     }
 
     private boolean isDateDir(File dir) {
         return dir.isDirectory() && dir.getName().matches("\\d\\d\\d\\d[-]\\d\\d[-]\\d\\d");
     }
 
     protected void updateLocationAsync(final JPanel previewPNL, final PhotoIterable pi) {
         new Thread(new Runnable() {
             @Override
             public void run() {
                 updateLocation(previewPNL, pi);
             }
         }).start();
     }
 
     protected void updateLocation(JPanel previewPNL, PhotoIterable pi) {
         if (pi == null)
             return;
 
         previewPNL.removeAll();
         JLabel label = new JLabel("Searching ...");
         previewPNL.add(label, BorderLayout.NORTH);
         JPanel imagePNL = new JPanel();
         FlowLayout flowLayout = new WrappingFlowLayout();
         flowLayout.setAlignment(FlowLayout.LEADING);
         imagePNL.setLayout(flowLayout);
         previewPNL.add(imagePNL, BorderLayout.CENTER);
 
         int numImages = 0;
         for (Entry entry : pi) {
             String n = entry.getName().toLowerCase();
 
             boolean displayEntry = false;
             for (String ext : getSupportedFormats())
                 if (n.endsWith(ext))
                     displayEntry = true;
 
             if (displayEntry && selectedImageNames != null)
                 displayEntry = selectedImageNames.contains(entry.getName());
 
             if (displayEntry) {
                 if (addImageIfWithinConstraints(imagePNL, entry))
                     numImages++;
             } else {
                 try {
                     entry.getInputStream().close(); // Needed to get rid of the associated file
                 } catch (IOException e) {
                 }
             }
 
             if (numImages >= 6)
                 break;
         }
 
         if (numImages == 0)
             label.setText("No images found at location: " + pi.getLocationString());
         else
             label.setText("Found these images: " + pi.getLocationString());
     }
 
     private boolean addImageIfWithinConstraints(final JPanel imagePanel, final Entry entry) {
         final JPanel p = UIUtils.getPhotoPreview(bundleContext, entry, fromDatePicker.getDate(), toDatePicker.getDate());
         if (p != null) {
             SwingUtilities.invokeLater(new Runnable() {
                 public void run() {
                     imagePanel.add(p);
                 }
             });
         }
         return p != null;
     }
 
     private boolean checkDir(String text) {
         return new File(text).isDirectory();
     }
 
     private String getPreferenceValue(String key) {
         return getPreferences().getProperty(key);
     }
 
     private void setPreferenceValue(String key, String value) {
         Properties prefs = getPreferences();
         prefs.setProperty(key, value);
         try {
             prefs.store(new FileOutputStream(bundleContext.getDataFile(PREFERENCE_FILENAME)), getClass().getName() + " preferences");
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     private Properties getPreferences() {
         File stgFile = bundleContext.getDataFile(PREFERENCE_FILENAME);
         Properties prefs = new Properties();
         if (stgFile.exists()) {
             try {
                 prefs.load(new FileInputStream(stgFile));
             } catch (IOException e) {
             }
         }
         return prefs;
     }
 
     private String [] getSupportedFormats() {
         ServiceReference[] refs;
         try {
             refs = bundleContext.getServiceReferences(PhotoMetadataProvider.class.getName(), null);
         } catch (InvalidSyntaxException e) {
             return new String [] {};
         }
 
         List<String> formats = new ArrayList<String>();
         for (ServiceReference ref : refs) {
             formats.addAll(OSGiTools.getStringPlusProperty(ref.getProperty("format")));
         }
         return formats.toArray(new String[] {});
     }
 
     private void runCopyOperation() {
         try {
             new Copier(bundleContext).copy(sourceIterable, targetTF.getText(), DEFAULT_TARGET_DATE_STRUCTURE, fromDatePicker.getDate(), toDatePicker.getDate(), selectedImageNames);
         } catch (Throwable e) {
             e.printStackTrace();
             JOptionPane.showMessageDialog(null, e.getMessage(), "An error has occurred during the copy operation", JOptionPane.WARNING_MESSAGE);
         }
         System.out.println("Shutting down.");
 
         try {
             bundleContext.getBundle(0).stop();
         } catch (BundleException e) {
             e.printStackTrace();
         }
     }
 }
