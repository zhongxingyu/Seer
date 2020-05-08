 package pl.poznan.put.cs.bioserver.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.util.Collections;
 
 import javax.swing.ButtonGroup;
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JEditorPane;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 import org.apache.commons.lang3.StringUtils;
 import org.biojava.bio.structure.Chain;
 import org.biojava.bio.structure.Structure;
 import org.biojava3.alignment.Alignments.PairwiseSequenceAlignerType;
 import org.biojava3.alignment.template.AlignedSequence;
 import org.biojava3.alignment.template.PairwiseSequenceAligner;
 import org.biojava3.alignment.template.SequencePair;
 import org.biojava3.core.sequence.compound.AminoAcidCompound;
 import org.biojava3.core.sequence.compound.NucleotideCompound;
 import org.biojava3.core.sequence.template.Sequence;
import org.jmol.util.Logger;
 
 import pl.poznan.put.cs.bioserver.alignment.SequenceAligner;
 import pl.poznan.put.cs.bioserver.helper.Helper;
 import pl.poznan.put.cs.bioserver.helper.PdbManager;
 
 /**
  * A panel in the main window that has all options related to sequence
  * alignment.
  * 
  * @author tzok
  */
 public class SequenceAlignmentPanel extends JPanel implements PdbChangeListener {
     private final class AlignSequences implements ActionListener {
         @Override
         public void actionPerformed(ActionEvent e) {
             if (settingsInstructionsPanel.settingsPanel.pdbPanel.getListModel()
                     .size() != 2) {
                 warning();
                 return;
             }
 
             Structure[] structures = PdbManager.getStructures(Collections
                     .list(settingsInstructionsPanel.settingsPanel.pdbPanel
                             .getListModel().elements()));
             Chain chains[] = new Chain[2];
             chains[0] = structures[0]
                     .getChain(settingsInstructionsPanel.settingsPanel.pdbPanel
                             .getComboBoxFirst().getSelectedIndex());
             chains[1] = structures[1]
                     .getChain(settingsInstructionsPanel.settingsPanel.pdbPanel
                             .getComboBoxSecond().getSelectedIndex());
 
             PairwiseSequenceAlignerType type;
             if (settingsInstructionsPanel.settingsPanel.buttonPanel.radioGlobal
                     .isSelected()) {
                 type = PairwiseSequenceAlignerType.GLOBAL;
             } else {
                 type = PairwiseSequenceAlignerType.LOCAL;
             }
 
             boolean isRNA = Helper.isNucleicAcid(chains[0]);
             if (isRNA != Helper.isNucleicAcid(chains[1])) {
                 String message = "Structures meant to be aligned "
                         + "represent different molecule types!";
                Logger.error(message);
                 JOptionPane.showMessageDialog(null, message, "Error",
                         JOptionPane.ERROR_MESSAGE);
                 return;
             }
 
             int gaps, length, minScore, maxScore, score;
             double similarity;
             if (isRNA) {
                 SequenceAligner<NucleotideCompound> aligner = new SequenceAligner<>(
                         NucleotideCompound.class);
                 PairwiseSequenceAligner<Sequence<NucleotideCompound>, NucleotideCompound> sequenceAligner = aligner
                         .alignSequences(chains[0], chains[1], type);
                 SequencePair<Sequence<NucleotideCompound>, NucleotideCompound> pair = sequenceAligner
                         .getPair();
 
                 gaps = 0;
                 for (AlignedSequence<Sequence<NucleotideCompound>, NucleotideCompound> as : pair
                         .getAlignedSequences()) {
                     gaps += StringUtils.countMatches(as.getSequenceAsString(),
                             "-");
                 }
                 length = pair.getLength();
                 score = sequenceAligner.getScore();
                 minScore = sequenceAligner.getMinScore();
                 maxScore = sequenceAligner.getMaxScore();
                 similarity = sequenceAligner.getSimilarity();
 
                 resultsPanel.textArea.setText(pair.toString());
             } else {
                 SequenceAligner<AminoAcidCompound> aligner = new SequenceAligner<>(
                         AminoAcidCompound.class);
                 PairwiseSequenceAligner<Sequence<AminoAcidCompound>, AminoAcidCompound> sequenceAligner = aligner
                         .alignSequences(chains[0], chains[1], type);
                 SequencePair<Sequence<AminoAcidCompound>, AminoAcidCompound> pair = sequenceAligner
                         .getPair();
 
                 gaps = 0;
                 for (AlignedSequence<Sequence<AminoAcidCompound>, AminoAcidCompound> as : pair
                         .getAlignedSequences()) {
                     gaps += StringUtils.countMatches(as.getSequenceAsString(),
                             "-");
                 }
                 length = pair.getLength();
                 score = sequenceAligner.getScore();
                 minScore = sequenceAligner.getMinScore();
                 maxScore = sequenceAligner.getMaxScore();
                 similarity = sequenceAligner.getSimilarity();
 
                 resultsPanel.textArea.setText(pair.toString());
             }
 
             resultsPanel.scorePanel.scoreLabel.setText(String.format(
                     "%d (min: %d, max: %d)", score, minScore, maxScore));
             resultsPanel.scorePanel.similarityLabel.setText(String.format(
                     "%.0f%%", 100.0 * similarity));
             resultsPanel.scorePanel.gapsLabel.setText(String.format(
                     "%d/%d (%.0f%%)", gaps, length, 100.0 * gaps / length));
         }
     }
 
     private class SettingsInstructionsPanel extends JPanel {
         private class SettingsPanel extends JPanel {
             private class ButtonPanel extends JPanel {
                 private static final long serialVersionUID = 1L;
                 private JButton buttonAddFile;
                 private JButton buttonAlign;
                 private JRadioButton radioGlobal;
                 private JRadioButton radioLocal;
 
                 public ButtonPanel() {
                     super();
                     buttonAddFile = new JButton("Load structure(s)");
                     buttonAlign = new JButton("Compute alignment");
                     radioGlobal = new JRadioButton("Global", true);
                     radioLocal = new JRadioButton("Local");
 
                     buttonAlign.setEnabled(false);
 
                     ButtonGroup group = new ButtonGroup();
                     group.add(radioGlobal);
                     group.add(radioLocal);
 
                     add(buttonAddFile);
                     add(buttonAlign);
                     add(new JLabel("Alignment type:"));
                     add(radioGlobal);
                     add(radioLocal);
                 }
             }
 
             private static final long serialVersionUID = 1L;
             private ButtonPanel buttonPanel;
             private PdbPanel pdbPanel;
 
             public SettingsPanel() {
                 super();
                 buttonPanel = new ButtonPanel();
                 pdbPanel = new PdbPanel(SequenceAlignmentPanel.this);
 
                 setLayout(new GridLayout(3, 1));
                 add(buttonPanel);
                 add(pdbPanel);
             }
         }
 
         private class InstructionsPanel extends JPanel {
             private static final long serialVersionUID = 1L;
 
             // ////////////////////////////////////////////////////////////
             // fields
             private JEditorPane editorPane;
 
             // ////////////////////////////////////////////////////////////
             // constructors
             public InstructionsPanel() {
                 editorPane = new JEditorPane();
                 editorPane.setBackground(new Color(0, 0, 0, 0));
                 editorPane.setContentType("text/html");
                 editorPane.setEditable(false);
                 editorPane
                         .setText("Instructions:<ol>"
                                 + "<li>Load structure(s) from files (PDB or mmCif)</li>"
                                 + "<li>Select global (Smith-Waterman) or local (Needleman-Wunsch) alignment method</li>"
                                 + "<li>Compute alignment</li></ol>"
                                 + "Additional information:<ul>"
                                 + "<li>Open penalty: 1</li>"
                                 + "<li>Extension penalty: 10</li>"
                                 + "<li>Substitution matrix for nucleic acids: NUC 4.4</li>"
                                 + "<li>Substitution matrix for proteins: BLOSUM62</li></ul>");
 
                 setLayout(new GridLayout(1, 1));
                 add(editorPane);
             }
         }
 
         private static final long serialVersionUID = 1L;
         private SettingsPanel settingsPanel;
 
         public SettingsInstructionsPanel() {
             settingsPanel = new SettingsPanel();
 
             setLayout(new GridLayout(1, 2));
             add(settingsPanel);
             add(new InstructionsPanel());
         }
     }
 
     private class ResultsPanel extends JPanel {
         private class ScorePanel extends JPanel {
             // /////////////////////////////////////////////////////////////////
             // fields
             private static final long serialVersionUID = 1L;
             private JLabel gapsLabel;
             private JLabel scoreLabel;
             private JLabel similarityLabel;
 
             // /////////////////////////////////////////////////////////////////
             // constructors
             public ScorePanel() {
                 scoreLabel = new JLabel();
                 similarityLabel = new JLabel();
                 gapsLabel = new JLabel();
 
                 setLayout(new FlowLayout());
                 add(new JLabel("Score:"));
                 add(scoreLabel);
                 add(new JLabel("Similarity:"));
                 add(similarityLabel);
                 add(new JLabel("Gaps:"));
                 add(gapsLabel);
             }
         }
 
         // /////////////////////////////////////////////////////////////////////
         // fields
         private static final long serialVersionUID = 1L;
         private JTextArea textArea;
         private ScorePanel scorePanel;
 
         // /////////////////////////////////////////////////////////////////////
         // constructors
         public ResultsPanel() {
             scorePanel = new ScorePanel();
             textArea = new JTextArea();
             textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN,
                     SequenceAlignmentPanel.FONT_SIZE));
             textArea.setEditable(false);
 
             setLayout(new BorderLayout());
             add(scorePanel, BorderLayout.NORTH);
             add(new JScrollPane(textArea), BorderLayout.CENTER);
         }
     }
 
     private static final int FONT_SIZE = 20;
 
     private static final long serialVersionUID = 1L;
     private final JFileChooser chooser = new JFileChooser();
     private SettingsInstructionsPanel settingsInstructionsPanel;
     private ResultsPanel resultsPanel;
 
     public SequenceAlignmentPanel() {
         super(new BorderLayout());
 
         settingsInstructionsPanel = new SettingsInstructionsPanel();
         resultsPanel = new ResultsPanel();
 
         add(settingsInstructionsPanel, BorderLayout.NORTH);
         add(resultsPanel, BorderLayout.CENTER);
 
         chooser.addChoosableFileFilter(new FileNameExtensionFilter(
                 "PDB file format", "pdb", "pdb1", "ent", "brk", "gz"));
         chooser.setMultiSelectionEnabled(true);
 
         settingsInstructionsPanel.settingsPanel.buttonPanel.buttonAddFile
                 .addActionListener(new ActionListener() {
                     @Override
                     public void actionPerformed(ActionEvent event) {
                         if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
                             return;
                         }
                         for (File f : chooser.getSelectedFiles()) {
                             if (!addFile(f)) {
                                 break;
                             }
                         }
                     }
                 });
 
         settingsInstructionsPanel.settingsPanel.buttonPanel.buttonAlign
                 .addActionListener(new AlignSequences());
     }
 
     boolean addFile(File path) {
         DefaultListModel<String> listModel = settingsInstructionsPanel.settingsPanel.pdbPanel
                 .getListModel();
         if (listModel.size() >= 2) {
             warning();
             return false;
         }
         String absolutePath = path.getAbsolutePath();
         PdbManager.loadStructure(absolutePath);
         listModel.addElement(absolutePath);
         settingsInstructionsPanel.settingsPanel.pdbPanel.refreshComboBoxes();
         if (listModel.size() == 2) {
             settingsInstructionsPanel.settingsPanel.buttonPanel.buttonAlign
                     .setEnabled(true);
         }
         return true;
     }
 
     void warning() {
         JOptionPane.showMessageDialog(this,
                 "You must have exactly two molecules", "Warning",
                 JOptionPane.WARNING_MESSAGE);
     }
 
     @Override
     public void pdbListChanged() {
         settingsInstructionsPanel.settingsPanel.buttonPanel.buttonAlign
                 .setEnabled(false);
     }
 }
