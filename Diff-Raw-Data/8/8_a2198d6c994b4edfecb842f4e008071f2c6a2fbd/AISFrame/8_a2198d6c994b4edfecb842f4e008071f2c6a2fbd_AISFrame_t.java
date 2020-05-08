 /**
  * Copyright (c) 2013, Martin Pecka (peci1@seznam.cz)
  * All rights reserved.
  * Licensed under the following BSD License.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  * Redistributions of source code must retain the above copyright
  * notice, this list of conditions and the following disclaimer.
  * 
  * Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimer in the
  * documentation and/or other materials provided with the distribution.
  * 
  * Neither the name Martin Pecka nor the
  * names of contributors may be used to endorse or promote products
  * derived from this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package cz.cuni.mff.peckam.ais.gui;
 
 import java.awt.EventQueue;
 import java.awt.Frame;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.Locale;
 import java.util.Properties;
 import java.util.concurrent.ExecutionException;
 
 import javax.swing.ComboBoxModel;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.SwingWorker;
 import javax.swing.peckam.JFileInput;
 
 import com.jgoodies.forms.factories.FormFactory;
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.RowSpec;
 
 import cz.cuni.mff.peckam.ais.AISLBLProductReader;
 import cz.cuni.mff.peckam.ais.AISResultOverlay;
 import cz.cuni.mff.peckam.ais.EvenlySampledIonogram;
 import cz.cuni.mff.peckam.ais.Ionogram;
 import cz.cuni.mff.peckam.ais.ProductOverlayType;
 import cz.cuni.mff.peckam.ais.result.FrameType;
 import cz.cuni.mff.peckam.ais.result.Orbit;
 
 /**
  * 
  * 
  * @author Martin Pecka
  */
 public class AISFrame
 {
 
     /** The main frame. */
     private JFrame             frmAisDataVisualizer;
     /** LBL file selection input. */
     private JFileInput         lblFileInput;
     /** The AIS renderer. */
     private ProductRenderer    renderer;
     /** Position in series. */
     private JComboBox<Integer> positionInSeriesComboBox;
 
     /** The product sets to display. */
     private Ionogram[]    ionograms = null;
     /** The product sets to display evenized. */
     private Ionogram[]         evenIonograms = null;
 
     /** Configuration. */
     private final Properties   props       = new Properties();
     /** The config file. */
     private final static File  CONFIG_FILE = new File("config.properties");
     /** The metadata of the currently loadad product set. */
     private JLabel             setMetadataLabel;
     /** Whether we want evenly distributed samples. */
     private JCheckBox          evenSamplesCheckBox;
     /**  */
     private ActionListener     updateIonogramsAction;
     /** Overlays. */
     private JComboBox<ProductOverlayType> overlaysComboBox;
     /** Detection results. */
     private JLabel                        resultLabel;
 
     /**
      * Launch the application.
      * 
      * @param args no args
      */
     public static void main(String[] args)
     {
         final PrintStream oldErr = System.err;
         final long startMilis = System.currentTimeMillis();
 
         System.setErr(new PrintStream(oldErr, true) {
             @Override
             public void println(String x)
             {
                 super.printf(Locale.ENGLISH, "%7.3f: " + x + "\n", (System.currentTimeMillis() - startMilis) / 1000.0);
             }
         });
 
         EventQueue.invokeLater(new Runnable() {
             @SuppressWarnings("synthetic-access")
             @Override
             public void run()
             {
                 try {
                     AISFrame window = new AISFrame();
                     window.frmAisDataVisualizer.setVisible(true);
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             }
         });
     }
 
     /**
      * Create the application.
      */
     public AISFrame()
     {
         try {
             props.load(new FileInputStream(CONFIG_FILE));
         } catch (IOException e2) {
             // supress
         }
 
         updateIonogramsAction = new ActionListener() {
             @SuppressWarnings("synthetic-access")
             @Override
             public void actionPerformed(final ActionEvent e)
             {
                 lblFileInput.setEnabled(false);
                 evenSamplesCheckBox.setEnabled(false);
                 positionInSeriesComboBox.setEnabled(false);
                 overlaysComboBox.setEnabled(false);
 
                 renderer.setLoadingText("Loading the .LBL file...");
 
                 ionograms = null;
                 evenIonograms = null;
 
                 new SwingWorker<Ionogram[], Void>() {
                     @Override
                     protected Ionogram[] doInBackground() throws Exception
                     {
                         try {
                             final String path = lblFileInput.getPath().trim();
                             if (path.isEmpty())
                                 return null;
 
                             Ionogram[] ionograms = new AISLBLProductReader().readFile(new File(path));
 
                             props.setProperty("defaultFile", path);
 
                             return ionograms;
                         } catch (IOException | IllegalStateException e1) {
                             e1.printStackTrace();
                             positionInSeriesComboBox.setEnabled(false);
                             overlaysComboBox.setEnabled(false);
                             JOptionPane.showMessageDialog(null, "The given file is not a valid .LBL file.");
                             renderer.setLoadingText("Loading the .LBL file has failed.");
                             lblFileInput.setEnabled(true);
                             return null;
                         }
                     }
 
                     @Override
                     protected void done()
                     {
                         try {
                             ionograms = get();
 
                             if (ionograms != null) {
                            	evenIonograms = new Ionogram[ionograms.length];
                            	
                                 final int oldIndex = positionInSeriesComboBox.getSelectedIndex();
 
                                 positionInSeriesComboBox
                                         .setModel(new NumericRangeComboBoxModel(0, ionograms.length - 1));
                                 positionInSeriesComboBox.setEnabled(true);
 
                                 if (e.getSource() != evenSamplesCheckBox) {
                                     positionInSeriesComboBox.setSelectedIndex(0);
                                 } else {
                                     positionInSeriesComboBox.setSelectedIndex(oldIndex);
                                 }
 
                                 lblFileInput.setEnabled(true);
                                 evenSamplesCheckBox.setEnabled(true);
                                 overlaysComboBox.setEnabled(true);
                             }
 
                             renderer.setLoadingText(null);
                         } catch (InterruptedException | ExecutionException e) {
                             e.printStackTrace();
 
                             lblFileInput.setEnabled(true);
                             evenSamplesCheckBox.setEnabled(false);
                             positionInSeriesComboBox.setEnabled(false);
                             overlaysComboBox.setEnabled(false);
 
                             JOptionPane.showMessageDialog(null, "Couldn't load the given .LBL file.");
                         }
                     }
 
                 }.execute();
             }
         };
 
         initialize();
 
         resultLabel = new JLabel("");
         frmAisDataVisualizer.getContentPane().add(resultLabel, "2, 6, 9, 1");
 
         renderer = new ProductRenderer();
         frmAisDataVisualizer.getContentPane().add(renderer, "2, 8, 9, 1, fill, fill");
 
         final String defaultFile = props.getProperty("defaultFile");
        if (defaultFile != null)
        	lblFileInput.setPath(defaultFile);
 
         frmAisDataVisualizer.addWindowListener(new WindowAdapter() {
             @SuppressWarnings("synthetic-access")
             @Override
             public void windowClosing(WindowEvent e)
             {
                 try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
                     props.store(fos, null);
                 } catch (IOException e1) {
                     // supress
                 }
             }
         });
 
         final ColorScale<Float> colorScale = new BoundedLogarithmicColorScale<>((float) Ionogram.MIN_VALUE / 10f,
                 (float) Ionogram.MAX_VALUE);
         final ActionListener updateImageListener = new ActionListener() {
             @SuppressWarnings("synthetic-access")
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 final Ionogram iono = getIonogram((int) positionInSeriesComboBox.getSelectedItem());
                 final ProductOverlayType type = (ProductOverlayType) overlaysComboBox.getSelectedItem();
                 if (iono.getOverlay(type) == null) {
                     final Orbit result = ((ProductOverlayType) overlaysComboBox.getSelectedItem()).getResult(new File(
                             lblFileInput.getPath()).getParentFile(), iono.getOrbitNumber());
                     if (result != null) {
                         final FrameType frame = result.getFrames()
                                 .get((int) positionInSeriesComboBox.getSelectedItem());
                         iono.addOverlay(new AISResultOverlay(iono, frame, type));
                     }
                 }
 
                 renderer.setProductAndColorScale(iono, colorScale, type);
                 updateSetMetadataLabel();
                 if (frmAisDataVisualizer.getExtendedState() == Frame.NORMAL)
                     frmAisDataVisualizer.pack();
             }
         };
         positionInSeriesComboBox.addActionListener(updateImageListener);
         overlaysComboBox.addActionListener(updateImageListener);
     }
 
     /**
      * Return the ionogram corresponding to the given position.
      * 
      * @param positionInSeries The position of the ionogram in the orbit frames list.
      * @return The ionogram.
      */
     private Ionogram getIonogram(int positionInSeries)
     {
         if (!evenSamplesCheckBox.isSelected()) {
             return ionograms[positionInSeries];
         } else {
             if (evenIonograms[positionInSeries] == null)
                 evenIonograms[positionInSeries] = new EvenlySampledIonogram(ionograms[positionInSeries]);
             return evenIonograms[positionInSeries];
         }
     }
 
     /**
      * Initialize the contents of the frame.
      */
     private void initialize()
     {
         frmAisDataVisualizer = new JFrame();
         frmAisDataVisualizer.setTitle("AIS Data Visualizer");
         frmAisDataVisualizer.setBounds(100, 100, 679, 496);
         frmAisDataVisualizer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frmAisDataVisualizer.getContentPane().setLayout(
                 new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.MIN_COLSPEC,
                         FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("default:grow"),
                         FormFactory.RELATED_GAP_COLSPEC, FormFactory.PREF_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
                         FormFactory.PREF_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, FormFactory.PREF_COLSPEC,
                         FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC,
                         FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("fill:default"),
                         FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
                         RowSpec.decode("default:grow"), FormFactory.RELATED_GAP_ROWSPEC, }));
 
         JLabel lblFileInputLabel = new JLabel(".LBL file to parse");
         frmAisDataVisualizer.getContentPane().add(lblFileInputLabel, "2, 2, left, fill");
 
         lblFileInput = new JFileInput();
         lblFileInput.getBrowseButton().setText("Browse...");
         lblFileInputLabel.setLabelFor(lblFileInput);
         frmAisDataVisualizer.getContentPane().add(lblFileInput, "4, 2, fill, center");
         lblFileInput.setColumns(10);
         lblFileInput.addActionListener(updateIonogramsAction);
 
         evenSamplesCheckBox = new JCheckBox("Evenly distributed samples");
         evenSamplesCheckBox.setSelected(true);
         frmAisDataVisualizer.getContentPane().add(evenSamplesCheckBox, "6, 2");
         evenSamplesCheckBox.addActionListener(updateIonogramsAction);
 
         positionInSeriesComboBox = new JComboBox<>();
         positionInSeriesComboBox.setEnabled(false);
         positionInSeriesComboBox.setModel(new DefaultComboBoxModel<>(new Integer[] {}));
         frmAisDataVisualizer.getContentPane().add(positionInSeriesComboBox, "8, 2, right, default");
 
         overlaysComboBox = new JComboBox<>();
         overlaysComboBox.setEnabled(false);
         overlaysComboBox.setModel(getOverlaysModel());
         frmAisDataVisualizer.getContentPane().add(overlaysComboBox, "10, 2, fill, default");
 
         setMetadataLabel = new JLabel(" ");
         frmAisDataVisualizer.getContentPane().add(setMetadataLabel, "2, 4, 9, 1, fill, top");
     }
 
     /**
      * Update the metadata label.
      */
     private void updateSetMetadataLabel()
     {
         setMetadataLabel.setText(renderer.getProduct().getMetadataString());
         resultLabel.setText(renderer.getProduct().getOverlay((ProductOverlayType) overlaysComboBox.getSelectedItem())
                 .getDescription());
     }
 
     /**
      * @return The overlays model.
      */
     protected ComboBoxModel<ProductOverlayType> getOverlaysModel()
     {
         return new DefaultComboBoxModel<>(new ProductOverlayType[] { new ProductOverlayType.Manual(),
                 new ProductOverlayType.SumsPeriodogram(), new ProductOverlayType.SumsFitting(),
                 new ProductOverlayType.SumsQuantile(), new ProductOverlayType.SumsCombined(),
                 new ProductOverlayType.VectorizationThinning() });
     }
 
 }
