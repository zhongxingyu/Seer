 package ru.spbau.bioinf.tagfinder.ui;
 
 import edu.ucsd.msalign.align.PropertyUtil;
 import edu.ucsd.msalign.align.idevalue.IdEValue;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.File;
 import java.io.IOException;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import java.util.Properties;
 import javax.swing.JComponent;
 import ru.spbau.bioinf.tagfinder.Acid;
 import ru.spbau.bioinf.tagfinder.Analyzer;
 import ru.spbau.bioinf.tagfinder.Configuration;
 import ru.spbau.bioinf.tagfinder.Consts;
 import ru.spbau.bioinf.tagfinder.Peak;
 import ru.spbau.bioinf.tagfinder.PeakType;
 import ru.spbau.bioinf.tagfinder.PrecursorMassShiftFinder;
 import ru.spbau.bioinf.tagfinder.Protein;
 import ru.spbau.bioinf.tagfinder.Scan;
 import ru.spbau.bioinf.tagfinder.ShiftEngine;
 
 public class ScanView extends JComponent {
 
     private static NumberFormat df = NumberFormat.getInstance();
     public static final int LINE_HEIGHT = 20;
 
     static {
         df.setMaximumFractionDigits(2);
     }
 
     List<TooltipCandidate> tooltips = new ArrayList<TooltipCandidate>();
 
     private final Configuration conf;
 
     private Scan scan;
     List<Peak> peaks = null;
 
     int proteinId = -1;
     private Protein protein = null;
     private double scale = 0.5;
     private Peak selectedPeak = null;
 
     private Dimension dimension = new Dimension(1000, 10);
     private List<List<Peak[]>> components = new ArrayList<List<Peak[]>>();
 
     private List<Protein> proteins;
     private double[] proteinSpectrum;
     double bestShift = 0;
 
     public ScanView(final Configuration conf, List<Protein> proteins, final TagFinder tagFinder) {
         this.conf = conf;
         this.proteins = proteins;
 
         addMouseMotionListener(new MouseAdapter() {
             @Override
             public void mouseMoved(MouseEvent e) {
                 TooltipCandidate tooltip = getTooltip(e);
                 if (tooltip != null) {
                     setToolTipText(tooltip.getText());
                 }
             }
         });
 
         addMouseListener(new MouseAdapter() {
             @Override
             public void mouseClicked(MouseEvent e) {
                 requestFocus();
                 TooltipCandidate tooltip = getTooltip(e);
                 if (tooltip != null) {
                     if (selectedPeak != tooltip.getPeak()) {
                         selectedPeak = tooltip.getPeak();
                         repaint();
                     }
                 }
             }
         });
         this.addKeyListener(new KeyAdapter() {
             @Override
             public void keyReleased(KeyEvent e) {
                 char keyChar = Character.toLowerCase(e.getKeyChar());
                 if (keyChar == '+') {
                     scale *= 1.1;
                     updateDimension();
                 }
                 if (keyChar == '-') {
                     scale *= 0.9;
                     updateDimension();
                 }
 
                 String keyText = KeyEvent.getKeyText(e.getKeyCode());
                 if (e.isControlDown()) {
                     if (keyText.equals("R")) {
                         if (protein != null && scan != null) {
                             List<Peak> reducedPeaks = new ArrayList<Peak>();
                             for (Peak peak : scan.getPeaks()) {
                                 if (getPeakColor(peak.getValue()) == Color.BLACK &&
                                         getPeakColor(peak.getYPeak().getValue()) == Color.BLACK) {
                                     reducedPeaks.add(peak);
                                 }
                             }
                             Scan reducedScan = new Scan(scan, reducedPeaks, proteinId);
                             tagFinder.addScanTab(reducedScan);
                         }
                     }
                     if (!scan.getName().equals(Integer.toString(scan.getId()))) {
                         if (keyText.equals("S")) {
                             try {
                                 System.out.println("Start computing E-value...");
                                 String filePrefix = "reduced";
                                 File reduced = new File(filePrefix);
                                 String scanName = scan.saveTxt(reduced);
                                 try {
                                     String[] args = {conf.getProteinDatabaseFile().getCanonicalPath(),
                                             filePrefix + "/" + scanName,
                                             "CID", "C57", "2", "15", filePrefix + "/" //+ pairsName
                                     };
                                     Properties properties = PropertyUtil.genePropertiesForId(args);
                                     IdEValue comp = new IdEValue(properties);
                                     System.out.println("Processing scan " + scan.getName() + " protein " + proteinId);
                                     comp.process(scan.getId(), Integer.toString(proteinId));
                                     System.out.println("Done");
                                 } catch (Exception e1) {
                                     e1.printStackTrace();
                                 }
                             } catch (IOException ioe) {
                                 ioe.printStackTrace();
                             }
                         }
                     }
                 }
             }
         });
     }
 
     private TooltipCandidate getTooltip(MouseEvent e) {
         int x = e.getX();
         int y = e.getY();
         for (TooltipCandidate tooltipCandidate : tooltips) {
             if (tooltipCandidate.isValid(x, y / LINE_HEIGHT + 1, scale)) {
                 return tooltipCandidate;
             }
         }
         return null;
     }
 
     public boolean setProteinId(int newProteinId) {
         if (newProteinId != proteinId) {
             if (newProteinId >= 0 && proteins.size() > newProteinId) {
                 this.proteinId = newProteinId;
                 protein = proteins.get(proteinId);
                 proteinSpectrum = ShiftEngine.getSpectrum(protein.getSimplifiedAcids());
                 if (scan != null) {
                     initBestShift();
                 } else {
                     bestShift = 0;
                 }
                 return true;
             }
         }
         return false;
     }
 
     public Protein getProtein() {
         return protein;
     }
 
     private int totalTags;
 
     public boolean setScan(Scan scan) {
         if (scan != this.scan) {
             this.scan = scan;
             Analyzer analyzer = new Analyzer(conf);
             double precursorMassShift = PrecursorMassShiftFinder.getPrecursorMassShift(conf, scan);
             peaks = scan.createSpectrumWithYPeaks(precursorMassShift);
             List<List<Peak>> components = analyzer.getComponents(peaks);
             this.components.clear();
             totalTags = 0;
             for (List<Peak> component : components) {
                 List<Peak[]> tags = analyzer.getTags(component);
                 totalTags += tags.size();
                 this.components.add(tags);
             }
 
             if (proteinSpectrum != null) {
                 initBestShift();
             }
 
             updateDimension();
             return true;
         }
         return false;
     }
 
     private void updateDimension() {
         dimension = new Dimension((int)(scan.getPrecursorMass() * scale) + 400, (components.size() + totalTags + 6) * LINE_HEIGHT);
         repaint();
     }
 
     @Override
     public Dimension getMinimumSize() {
         return dimension;
     }
 
     @Override
     public Dimension getPreferredSize() {
         return dimension;
     }
 
     @Override
     public Dimension getMaximumSize() {
         return dimension;
     }
 
     @Override
     public void paint(Graphics g) {
         g.setColor(Color.WHITE);
         g.fillRect(0, 0, getWidth(), getHeight());
         g.setColor(Color.BLACK);
         tooltips.clear();
         int line = 1;
 
         if (scan != null) {
             for (Peak peak : peaks) {
                 drawPeak(g, peak, line, -bestShift);
             }
             double precursorMass = scan.getPrecursorMass();
             double end = precursorMass + bestShift;
             drawPeak(g, line, bestShift);
             drawPeak(g, line, end);
             g.drawString(df.format(precursorMass) +  " + " + df.format(bestShift), (int)(end*scale) + 3, 15);
         }
 
         line++;
 
 
 
         if (protein != null) {
             String sequence = protein.getSimplifiedAcids();
             double pos = 0;
             for (int cur = 0; cur < sequence.length(); cur++) {
                 pos += drawLetter(g, line, pos, Acid.getAcid(sequence.charAt(cur)));
                 drawPeak(g, line, pos);
             }
             line++;
         }
 
         for (int i = 0; i < components.size(); i++) {
             List<Peak[]> component =  components.get(i);
             double min = scan.getPrecursorMass();
             for (Peak[] tag : component) {
                 double v = tag[0].getValue();
                 if (v < min) {
                     min = v;
                 }
             }
             g.drawString("Component " + (i + 1), 3, line * LINE_HEIGHT);
             line ++;
             for (Peak[] tag : component) {
                 for (int j = 0; j < tag.length; j++) {
                     Peak peak = tag[j];
                     double peakValue = drawPeak(g, peak, line, min);
                     if (j + 1 < tag.length) {
                         double delta = tag[j + 1].getValue() - peakValue;
                         drawLetter(g, line, peakValue - min, Acid.getAcid(delta));
                     }
                 }
                 line ++;
             }
         }
     }
 
     private double drawPeak(Graphics g, Peak peak, int y, double min) {
         double peakValue = peak.getValue();
         double value = (peakValue - min);
         if (proteinSpectrum != null) {
             Color color = getPeakColor(peakValue);
             g.setColor(color);
         }
         drawPeak(g, y, value, peak);
         g.setColor(Color.BLACK);
         return peakValue;
     }
 
     private Color getPeakColor(double peakValue) {
         Color color = Color.BLACK;
         double v = peakValue + bestShift;
         if (ShiftEngine.contains(proteinSpectrum, v)) {
             color = Color.GREEN;
         } else if (ShiftEngine.contains(proteinSpectrum, v, v - 1, v + 1, v + Consts.WATER, v - Consts.WATER, v - Consts.AMMONIA, v + Consts.AMMONIA)) {
             color = Color.ORANGE;
         }
         return color;
     }
 
     private void initBestShift() {
         double precursorMass =  scan.getPrecursorMass() + PrecursorMassShiftFinder.getPrecursorMassShift(conf, scan);
        List<Double> shifts = ShiftEngine.getShifts(scan.getPeaks(), precursorMass, proteinSpectrum);
 
         double bestScore = 0;
         double[] spectrum = ShiftEngine.getSpectrum(scan.getPeaks(), precursorMass);
         for (Double shift : shifts) {
             double nextScore = ShiftEngine.getScore(spectrum, proteinSpectrum, shift);
             if (nextScore > bestScore) {
                 bestScore = nextScore;
                 bestShift = shift;
             }
         }
     }
 
     private void drawPeak(Graphics g, int y, double value) {
         drawPeak(g, y, value, null);
     }
 
     private void drawPeak(Graphics g, int line, double value, Peak peak) {
         int v = (int)(value * scale);
         int y = line * LINE_HEIGHT;
         g.drawLine(v, y, v, y - LINE_HEIGHT + 3);
         double tooltipValue = peak != null ? peak.getValue() : value;
         tooltips.add(new TooltipCandidate(value, line, df.format(tooltipValue), peak));
         if (peak != null) {
             drawIon(g, line, v, peak.getPeakType());
             if (peak == selectedPeak) {
                 g.setColor(Color.BLUE);
                 g.drawRect(v - 5, y - LINE_HEIGHT, 10, LINE_HEIGHT + 3);
             }
         }
     }
 
     private void drawIon(Graphics g, int line, int  v, PeakType peakType) {
         int x1 = peakType == PeakType.B ? v - 3 : v + 3;
         int y = line * LINE_HEIGHT;
         g.drawLine(x1, y - LINE_HEIGHT + 3, v, y - LINE_HEIGHT + 3);
         g.drawLine(x1, y, v, y);
     }
 
     private double drawLetter(Graphics g, int line, double pos, Acid acid) {
         double delta = acid.getMass();
         double x = pos + delta / 2 - 3;
         x *= scale;
         g.drawString(acid.name(), (int) (x), line * LINE_HEIGHT - 4);
         return delta;
     }
 }
