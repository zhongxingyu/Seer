 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 
 import javax.swing.BoxLayout;
 import javax.swing.ButtonGroup;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JTabbedPane;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import org.jmol.adapter.smarter.SmarterJmolAdapter;
 import org.jmol.api.JmolAdapter;
 import org.jmol.api.JmolStatusListener;
 import org.jmol.api.JmolViewer;
 
 /*
  * Created on Apr 19, 2005
  *
  */
 
 /**
  * @author brian
  *
  */
 public class Molecules extends JFrame {
 	
 	final String drugDotsScript = "select S58; spacefill off; dots on;";
 	final String drugSfScript = "select S58; spacefill on; dots off;";
 	final String coxDotsScript = "select 120 or 90 or 355 or 530 or 385"
 		+ "or 531 or 381; spacefill off; dots on;";
 	final String coxSfScript = "select 120 or 90 or 355 or 530 or 385"
 		+ "or 531 or 381; spacefill on; dots off;";
 	final String AcpkScript = "select atomno=4561; color cpk;";
 	final String ApurpScript = "select atomno=4561; color purple;";
 	final String BcpkScript = "select atomno=4578; color cpk;";
 	final String BgreenScript = "select atomno=4578; color green;";
 	final String CcpkScript = "select atomno=4579; color cpk;";
 	final String CpinkScript = "select atomno=4579; color pink;";
 	final String DcpkScript = "select atomno=4582; color cpk;";
 	final String DwhiteScript = "select atomno=4582; color white;";
 	
 	JLabel statusLabel;
 	JLabel buttonNameLabel;
 	JTabbedPane problemPane;
 	
 	public Molecules() {
		super("Molecules in 3-dimensions  2.3");
 		addWindowListener(new ApplicationCloser());
 		Container contentPane = getContentPane();
 		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.X_AXIS));
 		
 		JPanel moleculePanel = new JPanel();
 		moleculePanel.setLayout(new BoxLayout(moleculePanel, BoxLayout.Y_AXIS));
 		final JmolPanel jmolPanel = new JmolPanel();
 		final JmolViewer viewer = jmolPanel.getViewer();
 		jmolPanel.setPreferredSize(new Dimension(600,600));
 		moleculePanel.add(jmolPanel);
 		
 		buttonNameLabel = new JLabel("Information on buttons pressed shown here.");
 		moleculePanel.add(buttonNameLabel);
 		buttonNameLabel.setOpaque(true);
 		buttonNameLabel.setForeground(Color.BLUE);
 		statusLabel = new JLabel("Information on atoms clicked shown here.");
 		moleculePanel.add(statusLabel);
 
 		contentPane.add(moleculePanel);
 				
 		problemPane = new JTabbedPane();
 		problemPane.addTab("Small Molecules", makeSmallMolecPanel(jmolPanel, viewer, 
 				buttonNameLabel));
 		problemPane.addTab("Small Polypeptides", makeSmallPolypepPanel(jmolPanel, viewer,
 				buttonNameLabel));
 		problemPane.addTab("Lysozyme I", makeLysozymeIPanel(jmolPanel, viewer,
 				buttonNameLabel));
 		problemPane.addTab("Lysozyme II", makeLysozymeIIPanel(jmolPanel, viewer,
 				buttonNameLabel));
 		problemPane.addTab("Lysozyme III", makeLysozymeIIIPanel(jmolPanel, viewer,
 				buttonNameLabel));
 		problemPane.addTab("Enzyme AAG", makeAAGPanel(jmolPanel, viewer,
 				buttonNameLabel));
 		problemPane.addTab("Enzyme COX-2", makeCOXPanel(jmolPanel, viewer,
 				buttonNameLabel));
 		problemPane.addTab("COX-2 Inhibitors", makeCOXDrugPanel(jmolPanel, viewer,
 				buttonNameLabel));
 		problemPane.addTab("DNA Structure", makeDNAPanel(jmolPanel, viewer,
 				buttonNameLabel));
 		problemPane.addTab("Hemglobin", makeHemoPanel(jmolPanel, viewer,
 				buttonNameLabel));
 		
 		contentPane.add(problemPane);		
 		
 		problemPane.addChangeListener(new ChangeListener() {
 			public void stateChanged(ChangeEvent e) {
 				viewer.evalString("zap;"
 						+ "spacefill off;"
 						+ "wireframe off;"
 						+ "backbone off;"
 						+ "slab off;"
 						+ "spin off;"
 						+ "cartoon off;"
 						+ "hbonds off;"
 						+ "dots off;");
 				buttonNameLabel.setText("   ");
 			}
 			
 		});
 
 	}
 	
 	public static void main(String[] args) {
 		Molecules myMolecs = new Molecules();
 		myMolecs.pack();
 		myMolecs.setVisible(true);
 	}
 	
 	public JPanel makeSmallMolecPanel(final JmolPanel jmolPanel, final JmolViewer viewer, 
 			final JLabel buttonNameLabel) {
 		JPanel buttonPanel = new JPanel();
 		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
 		
 		buttonPanel.add(new JLabel("<html><font color=red size=+2>"
 				+ "Small Molecules<br></font</html>"));
 		buttonPanel.add(makeLoadStructureButton("Load the linear form of glucose",
 				"D-glucose.pdb",
 				null,
 				buttonNameLabel,
 				jmolPanel));
 		buttonPanel.add(makeLoadStructureButton("Load the linear form of fructose",
 				"D-fructose.pdb",
 				null,
 				buttonNameLabel,
 				jmolPanel));
 		buttonPanel.add(makeLoadStructureButton("Load the circular form of glucose",
 				"beta-D-glucopyranose.pdb",
 				null,
 				buttonNameLabel,
 				jmolPanel));
 		buttonPanel.add(makeLoadStructureButton("Load the first amino acid",
 				"AA1.PDB",
 				null,
 				buttonNameLabel,
 				jmolPanel));
 		buttonPanel.add(makeLoadStructureButton("Load the second amino acid",
 				"AA2.PDB",
 				null,
 				buttonNameLabel,
 				jmolPanel));
 		buttonPanel.add(makeLoadStructureButton("Load the third amino acid",
 				"AA3.PDB",
 				null,
 				buttonNameLabel,
 				jmolPanel));
 		buttonPanel.add(new JLabel("<html><br></html>"));
 		buttonPanel.add(new JLabel(
 				new ImageIcon(Molecules.class.getResource("cpkColors.gif"))));
 		
 		return buttonPanel;
 	}
 	
 	public JPanel makeSmallPolypepPanel(final JmolPanel jmolPanel, final JmolViewer viewer, 
 			final JLabel buttonNameLabel) {
 		JPanel buttonPanel = new JPanel();
 		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
 		
 		buttonPanel.add(new JLabel("<html><font color=red size=+2>"
 				+ "Small Polypeptides<br></font></html>"));
 		buttonPanel.add(makeLoadStructureButton("Load the first tripeptide",
 				"tripeptide1.pdb",
 				null,
 				buttonNameLabel,
 				jmolPanel));
 		buttonPanel.add(makeLoadStructureButton("Load the second tripeptide",
 				"tripeptide2.pdb",
 				null,
 				buttonNameLabel,
 				jmolPanel));
 		buttonPanel.add(new JLabel("<html><br></html>"));
 		buttonPanel.add(new JLabel(
 				new ImageIcon(Molecules.class.getResource("cpkColors.gif"))));
 		
 		return buttonPanel;
 	}
 	
 	public JPanel makeLysozymeIPanel(final JmolPanel jmolPanel, final JmolViewer viewer, 
 			final JLabel buttonNameLabel) {
 		JPanel buttonPanel = new JPanel();
 		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
 		buttonPanel.add(new JLabel("<html><font color=red size=+2>"
 				+ "Lysozyme 2<sup>o</sup> Structure"
 				+ "<br></font></html>"));
 		buttonPanel.add(makeLoadStructureButton("Load lysozyme and show backbone.",
 				"1LYD.PDB",
 				"spacefill off; wireframe off;"
 				+ "backbone 0.3; color structure;",
 				buttonNameLabel,
 				jmolPanel));
 		buttonPanel.add(makeScriptButton("Show alpha helix.",
 				"restrict protein; wireframe off;" 
 				+ "spacefill 0; backbone 0.3;"
 				+ "color structure; center selected; "
 				+ "reset; restrict 60-80; center selected; "
 				+ "zoom 200",
 				buttonNameLabel,
 				jmolPanel));
 		buttonPanel.add(makeScriptButton("Show beta sheets.",
 				"restrict protein; wireframe off; "
 				+ "spacefill 0; backbone 0.3; "
 				+ "color structure; center selected; "
 				+ "reset; restrict 12-59; center selected; "
 				+ "zoom 300",
 				buttonNameLabel,
 				jmolPanel));
 		buttonPanel.add(new JLabel("<html><br></html>"));
 		buttonPanel.add(new JLabel(
 				new ImageIcon(Molecules.class.getResource("structureColors.gif"))));
 		
 		return buttonPanel;
 	}
 	
 	public JPanel makeLysozymeIIPanel(final JmolPanel jmolPanel, final JmolViewer viewer, 
 			final JLabel buttonNameLabel) {
 		JPanel buttonPanel = new JPanel();
 		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
 		
 		buttonPanel.add(new JLabel("<html><font color=red size=+2>"
 				+ "Lysozyme 3<sup>o</sup> Structure I"
 				+ "<br></font></html>"));
 		buttonPanel.add(makeLoadStructureButton("Load lysozyme and show exterior; red = phobic.",
 				"1LYD.PDB",
 				"slab off; restrict protein; "
 				+ "spacefill on; "
 				+ "select all; color white; "
 				+ "select hydrophobic; color red",
 				buttonNameLabel,
 				jmolPanel));
 		buttonPanel.add(makeScriptButton("Show interior; red = phobic.",
 				"restrict protein; spacefill on; "
 				+ "select all; color white; "
 				+ "dots off; wireframe off;"
 				+ "select hydrophobic; color red; "
 				+ "reset; move 90 -90 0  0  0 0 0  0  5;"
 				+ "delay 5;"
 				+ "slab on;"
 				+ "slab 50",
 				buttonNameLabel,
 				jmolPanel));
 		buttonPanel.add(makeScriptButton("Show Valines.",
 				"slab off; restrict protein; "
 				+ "select protein; color yellow; "
 				+ "spacefill off; wireframe off; dots on; "
 				+ "backbone off; select val; dots off; "
 				+ "color cpk; spacefill on",
 				buttonNameLabel,
 				jmolPanel));
 		buttonPanel.add(makeScriptButton("Show Lysines.",
 				"slab off; restrict protein; "
 				+ "select protein; color yellow; "
 				+ "spacefill off; wireframe off; dots on; "
 				+ "backbone off; select lys; dots off; "
 				+ "color cpk; spacefill on",
 				buttonNameLabel,
 				jmolPanel));
 		buttonPanel.add(new JLabel("<html><br></html>"));
 		buttonPanel.add(new JLabel(
 				new ImageIcon(Molecules.class.getResource("cpkColors.gif"))));
 		
 		return buttonPanel;
 	}
 	
 	public JPanel makeLysozymeIIIPanel(final JmolPanel jmolPanel, final JmolViewer viewer, 
 			final JLabel buttonNameLabel) { 
 		JPanel buttonPanel = new JPanel();
 		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
 		
 		JRadioButton bsButton = new JRadioButton("Show atoms as Ball & Stick");
 		bsButton.setSelected(true);
 		JRadioButton sfButton = new JRadioButton("Show atoms as Spacefill");
 		ButtonGroup group = new ButtonGroup();
 		group.add(bsButton);
 		group.add(sfButton);
 		
 		bsButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				viewer.evalString("spacefill 0.5; wireframe 0.2; ");
 			}
 			
 		});
 		sfButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				viewer.evalString("spacefill on; ");
 			}
 			
 		});
 		
 		buttonPanel.add(new JLabel("<html><font color=red size=+2>"
 				+ "Lysozyme 3<sup>o</sup> Structure II"
 				+ "<br></font></html>"));
 		buttonPanel.add(makeSizeSensitiveLoadStructureButton("Load Lysozyme.",
 				"1LYD.PDB",
 				"restrict protein; "
 				+ "spacefill 0.5; wireframe 0.2; ",
 				buttonNameLabel,
 				bsButton,
 				true,
 				jmolPanel));
 		buttonPanel.add(makeSizeSensitiveScriptButton("Show Glu 11 and Arg 145.",
 				"restrict protein; spacefill off; "
 				+ "backbone off; "
 				+ "reset; select all; wireframe on; color [60,60,60]; "
 				+ "select 11 or 145; color cpk; ",
 				buttonNameLabel,
 				bsButton,
 				true,
 				jmolPanel));
 		buttonPanel.add(makeSizeSensitiveScriptButton("Show Asp 10 and Tyr 161.",
 				"restrict protein; spacefill off; "
 				+ "backbone off; "
 				+ "reset; select all; wireframe on; color [60,60,60]; "
 				+ "select 10 or 161; color cpk; ",
 				buttonNameLabel,
 				bsButton,
 				true,
 				jmolPanel));
 		buttonPanel.add(makeSizeSensitiveScriptButton("Show Gln 105 and Trp 138.",
 				"restrict protein; spacefill off; "
 				+ "backbone off; "
 				+ "reset; select all; wireframe on; color [60,60,60]; "
 				+ "select 105 or 138; color cpk; ",
 				buttonNameLabel,
 				bsButton,
 				true,
 				jmolPanel));
 		buttonPanel.add(makeSizeSensitiveScriptButton("Show Met 102 and Phe 114.",
 				"restrict protein; spacefill off; "
 				+ "backbone off; "
 				+ "reset; select all; wireframe on; color [60,60,60]; "
 				+ "select 102 or 114; color cpk; ",
 				buttonNameLabel,
 				bsButton,
 				true,
 				jmolPanel));
 		buttonPanel.add(makeSizeSensitiveScriptButton("Show Tyr 24 and Lys 35.",
 				"restrict protein; spacefill off; "
 				+ "backbone off; "
 				+ "reset; select all; wireframe on; color [60,60,60]; "
 				+ "select 24 or 35; color cpk; ",
 				buttonNameLabel,
 				bsButton,
 				true,
 				jmolPanel));
 		buttonPanel.add(makeScriptButton("Show Phe 67 and Secondary Struct.",
 				"restrict protein; spacefill off; "
 				+ "reset; select all; wireframe off; "
 				+ "backbone 0.2; color structure; "
 				+ "select 67; color red; spacefill on;"
 				+ "center selected;",
 				buttonNameLabel,
 				jmolPanel));
 		buttonPanel.add(new JLabel("<html><br></html>"));
 		
 		buttonPanel.add(bsButton);
 		buttonPanel.add(sfButton);
 		buttonPanel.add(new JLabel("<html><br></html>"));
 		
 		buttonPanel.add(new JLabel(
 				new ImageIcon(Molecules.class.getResource("cpkColors.gif"))));
 		
 		return buttonPanel;
 	}
 	
 	public JPanel makeAAGPanel(final JmolPanel jmolPanel, final JmolViewer viewer, 
 			final JLabel buttonNameLabel) {
 		JPanel buttonPanel = new JPanel();
 		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
 		
 		JRadioButton bsButton1 = new JRadioButton("Show atoms as Ball & Stick");
 		JRadioButton sfButton1 = new JRadioButton("Show atoms as Spacefill");
 		sfButton1.setSelected(true);
 		ButtonGroup group1 = new ButtonGroup();
 		group1.add(bsButton1);
 		group1.add(sfButton1);
 		
 		bsButton1.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				viewer.evalString("spacefill 0.5; wireframe 0.2; ");
 			}
 			
 		});
 		sfButton1.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				viewer.evalString("spacefill on; ");
 			}
 			
 		});
 		
 		buttonPanel.add(new JLabel("<html><font color=red size=+2>"
 				+ "The DNA-Repair Enzyme AAG"
 				+ "<br></font></html>"));
 		buttonPanel.add(makeLoadStructureButton("Load AAG and DNA.",
 				"1BNK.PDB",
 				"restrict not water; spacefill on;"
 				+ "select not water; color chain; "
 				+ "select ligand; color red;"
 				+ "select not water;",
 				buttonNameLabel,
 				jmolPanel));
 		buttonPanel.add(makeSizeSensitiveScriptButton("Show Arg 182 in the protein and T8 in the DNA.",
 				"restrict not water; spacefill off; "
 				+ "reset; wireframe on; color [60,60,60];"
 				+ "select T8 or 182 or atomno=1679; color cpk; ",
 				buttonNameLabel,
 				bsButton1,
 				true,
 				jmolPanel));
 		buttonPanel.add(makeSizeSensitiveScriptButton("Show Thr 143 in the protein and G23 in the DNA.",
 				"restrict not water; spacefill off; "
 				+ "reset; wireframe on; color [60,60,60];"
 				+ "select G23 or 143 or atomno=1973; color cpk; ",
 				buttonNameLabel,
 				bsButton1,
 				true,
 				jmolPanel));
 		buttonPanel.add(makeSizeSensitiveScriptButton("Show Met 164 in the protein and T19 in the DNA.",
 				"restrict not water; spacefill off; "
 				+ "reset; wireframe on; color [60,60,60];"
 				+ "select T19 or 164; color cpk; ",
 				buttonNameLabel,
 				bsButton1,
 				true,
 				jmolPanel));
 		buttonPanel.add(makeSizeSensitiveScriptButton("Show Tyr 162 in the protein and T8 in the DNA.",
 				"restrict not water; spacefill off; "
 				+ "reset; wireframe on; color [60,60,60];"
 				+ "select T8 or 162; color cpk; ",
 				buttonNameLabel,
 				bsButton1,
 				true,
 				jmolPanel));
 		
 		buttonPanel.add(new JLabel("<html><br></html>"));
 		
 		buttonPanel.add(bsButton1);
 		buttonPanel.add(sfButton1);
 		buttonPanel.add(new JLabel("<html><br></html>"));
 		
 		buttonPanel.add(new JLabel(
 				new ImageIcon(Molecules.class.getResource("cpkColors.gif"))));
 		
 		return buttonPanel;
 	}
 	
 	public JPanel makeCOXPanel(final JmolPanel jmolPanel, final JmolViewer viewer, 
 			final JLabel buttonNameLabel){ 
 		JPanel buttonPanel = new JPanel();
 		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
 		
 		buttonPanel.add(new JLabel("<html><font color=red size=+2>"
 				+ "The Enzyme COX-2 I"
 				+ "<br></font></html>"));
 		buttonPanel.add(makeLoadStructureButton("Load COX-2 with drug bound.",
 				"6COX.PDB",
 				"restrict protein or S58; spacefill on;"
 				+ "select protein or S58; color chain; "
 				+ "select S58; color red;"
 				+ "move -45 0 0  0  0 0 0  0  2",
 				buttonNameLabel,
 				jmolPanel));
 		JRadioButton sphereButton = new JRadioButton("Show protein atoms as Spheres");
 		sphereButton.setSelected(true);
 		JRadioButton dotButton = new JRadioButton("Show protein atoms as Dots");
 		ButtonGroup group2 = new ButtonGroup();
 		group2.add(sphereButton);
 		group2.add(dotButton);
 		sphereButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				viewer.evalString("select protein; wireframe off; spacefill on; dots off; ");
 			}
 		});
 		dotButton.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				viewer.evalString("select protein; wireframe off; spacefill off; dots on; ");
 			}
 		});
 		buttonPanel.add(sphereButton);
 		buttonPanel.add(dotButton);
 		
 		buttonPanel.add(new JLabel("<html><br></html>"));
 		
 		buttonPanel.add(new JLabel(
 				new ImageIcon(Molecules.class.getResource("cpkColors.gif"))));
 		
 		return buttonPanel;
 	}
 	
 	public JPanel makeCOXDrugPanel(final JmolPanel jmolPanel, final JmolViewer viewer, 
 			final JLabel buttonNameLabel){ 
 		JPanel buttonPanel = new JPanel();	    	                       		
 		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
 		final JCheckBox drugDotsCheckbox = new JCheckBox("Show Drug as Dots");
 		final JCheckBox coxDotsCheckbox = new JCheckBox("Show COX-2 as Dots");
 		final JCheckBox aColorCheckbox = new JCheckBox("Color Position A Purple");
 		final JCheckBox bColorCheckbox = new JCheckBox("Color Position B Green");
 		final JCheckBox cColorCheckbox = new JCheckBox("Color Postion C Pink");
 		final JCheckBox dColorCheckbox = new JCheckBox("Color Postion D White");
 		
 		
 		buttonPanel.add(new JLabel("<html><font color=red size=+2>"
 				+ "The Enzyme COX-2 II"
 				+ "<br></font></html>"));
 		buttonPanel.add(makeSensitiveLoadStructureButton("Load COX-2 with model drug bound.",
 				"edited_cox2.pdb",
 				"reset; select S58; spacefill on; center selected;"
 				+ "select protein or hem; wireframe off; spacefill off;"
 				+ "select 120 or 90 or 355 or 530 or 385 or 531 or 381;"
 				+ "wireframe 0.2; spacefill on;"
 				+ "zoom 300;",
 				buttonNameLabel,
 				drugDotsCheckbox,
 				coxDotsCheckbox,
 				aColorCheckbox,
 				bColorCheckbox,
 				cColorCheckbox,
 				dColorCheckbox,
 				jmolPanel));
 		buttonPanel.add(makeSensitiveLoadStructureButton("Load COX-2 with celebrex bound.",
 				"celebrex_cox.pdb",
 				"reset; select S58; spacefill on; center selected;"
 				+ "select protein or hem; wireframe off; spacefill off;"
 				+ "select 120 or 90 or 355 or 530 or 385 or 531 or 381;"
 				+ "wireframe 0.2; spacefill on;"
 				+ "zoom 300;",
 				buttonNameLabel,
 				drugDotsCheckbox,
 				coxDotsCheckbox,
 				aColorCheckbox,
 				bColorCheckbox,
 				cColorCheckbox,
 				dColorCheckbox,
 				jmolPanel));
 		
 		drugDotsCheckbox.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				if (drugDotsCheckbox.isSelected()){
 					viewer.evalString(drugDotsScript);
 				} else {
 					viewer.evalString(drugSfScript);
 				}
 			}
 		});
 		coxDotsCheckbox.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				if (coxDotsCheckbox.isSelected()){
 					viewer.evalString(coxDotsScript);
 				} else {
 					viewer.evalString(coxSfScript);
 				}
 			}
 		});
 		aColorCheckbox.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				if (aColorCheckbox.isSelected()){
 					viewer.evalString(ApurpScript);
 				} else {
 					viewer.evalString(AcpkScript);
 				}
 			}
 		});
 		bColorCheckbox.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				if (bColorCheckbox.isSelected()){
 					viewer.evalString(BgreenScript);
 				} else {
 					viewer.evalString(BcpkScript);
 				}
 			}
 		});
 		cColorCheckbox.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				if (cColorCheckbox.isSelected()){
 					viewer.evalString(CpinkScript);
 				} else {
 					viewer.evalString(CcpkScript);
 				}
 			}
 		});
 		dColorCheckbox.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				if (dColorCheckbox.isSelected()){
 					viewer.evalString(DwhiteScript);
 				} else {
 					viewer.evalString(DcpkScript);
 				}
 			}
 		});
 		
 		buttonPanel.add(drugDotsCheckbox);
 		buttonPanel.add(coxDotsCheckbox);
 		buttonPanel.add(aColorCheckbox);
 		buttonPanel.add(bColorCheckbox);
 		buttonPanel.add(cColorCheckbox);
 		buttonPanel.add(dColorCheckbox);
 		
 		
 		buttonPanel.add(new JLabel("<html><br></html>"));	    
 		buttonPanel.add(new JLabel(
 				new ImageIcon(Molecules.class.getResource("cpkColors.gif"))));
 		
 		return buttonPanel;
 	}
 	
 	public JPanel makeDNAPanel(final JmolPanel jmolPanel, final JmolViewer viewer,
 			final JLabel buttonNameLabel) {
 		JPanel buttonPanel = new JPanel();
 		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
 		buttonPanel.add(new JLabel("<html><font color=red size=+2>"
 				+ "DNA Structure."
 				+ "<br></font></html>"));
 		
 		JRadioButton bsButton2 = new JRadioButton("Show atoms as Ball & Stick");
 		JRadioButton sfButton2 = new JRadioButton("Show atoms as Spacefill");
 		sfButton2.setSelected(true);
 		ButtonGroup group1 = new ButtonGroup();
 		group1.add(bsButton2);
 		group1.add(sfButton2);
 		
 		bsButton2.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				viewer.evalString("spacefill 0.5; wireframe 0.2; ");
 			}
 			
 		});
 		sfButton2.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				viewer.evalString("spacefill on; ");
 			}
 			
 		});
 
 		buttonPanel.add(makeSizeSensitiveLoadStructureButton("Load First DNA Molecule.",
 				"DNA1.pdb",
 				"select all; "
 				+ "spacefill on; color cpk; "
 				+ "rotate x 60;"
 				+ "rotate z 28;",
 				buttonNameLabel,
 				bsButton2,
 				true,
 				jmolPanel));
 		
 		buttonPanel.add(makeSizeSensitiveScriptButton("Show two strands.",
 				"select all;"
 				+ "color green;"
 				+ "select backbone; color chain;"
 				+ "select atomno==2 or atomno==497; color purple;"
 				+ "select atomno==480 or atomno==968; color white;"
 				+ "select all;",
 				buttonNameLabel,
 				bsButton2,
 				false,
 				jmolPanel));
 
 		buttonPanel.add(makeSizeSensitiveScriptButton("Color code bases and two strands.",
 				"select A; color yellow;"
 				+ "select G; color green;"
 				+ "select C; color blue;"
 				+ "select T; color red;"
 				+ "select backbone; color chain;"
 				+ "select atomno==2 or atomno==497; color purple;"
 				+ "select atomno==480 or atomno==968; color white;"
 				+ "select all;",
 				buttonNameLabel,
 				bsButton2,
 				false,
 				jmolPanel));
 		
 		buttonPanel.add(makeSizeSensitiveLoadStructureButton("Load Second DNA Molecule.",
 				"DNA2.pdb",
 				"select all; "
 				+ "spacefill on; color cpk; "
 				+ "select *.c3*; color white;"
 				+ "select *.c5*; color purple;"
 				+ "rotate x 60;"
 				+ "rotate z 28;"
 				+ "select all;",
 				buttonNameLabel,
 				bsButton2,
 				false,
 				jmolPanel));
 
 		buttonPanel.add(new JLabel("<html><br></html>"));
 		
 		buttonPanel.add(bsButton2);
 		buttonPanel.add(sfButton2);
 
 		buttonPanel.add(new JLabel("<html><br></html>"));	    
 		buttonPanel.add(new JLabel(
 				new ImageIcon(Molecules.class.getResource("cpkColors.gif"))));
 	
         return buttonPanel;
 	}
 	
 	public JPanel makeHemoPanel(final JmolPanel jmolPanel, final JmolViewer viewer, 
 			final JLabel buttonNameLabel){
 		JPanel buttonPanel = new JPanel();	    	                       		
 		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
 		buttonPanel.add(new JLabel("<html><font color=red size=+2>"
 				+ "The Protein Hemoglobin."
 				+ "<br></font></html>"));
 		
 		buttonPanel.add(makeLoadStructureButton("Load Hemoglobin and show 4 "
 				+ "chains and heme.",
 				"1a3n.pdb",
 				"restrict ligand or protein; dots off; "
 				+ "spacefill on; color chain; "
 				+ "select ligand; color red; ",
 				buttonNameLabel,
 				jmolPanel));
 		
 		buttonPanel.add(makeScriptButton("Show Gly 107 and heme.",
 				"select protein or ligand; spacefill off; "
 				+ "wireframe off; dots on; color yellow; "
 				+ "select 107 and (:b or :d); color cpk; spacefill on; "
 				+ "select ligand and (:b or :d); color cpk; spacefill on; ",
 				buttonNameLabel,
 				jmolPanel));
 		buttonPanel.add(makeScriptButton("Show Ala 27 and heme.",
 				"select protein or ligand; spacefill off; "
 				+ "wireframe off; dots on; color yellow; "
 				+ "select 27 and (:b or :d); color cpk; spacefill on; "
 				+ "select ligand and (:b or :d); color cpk; spacefill on; ",
 				buttonNameLabel,
 				jmolPanel));
 		buttonPanel.add(makeScriptButton("Show Lys 17 and heme.",
 				"select protein or ligand; spacefill off; "
 				+ "wireframe off; dots on; color yellow; "
 				+ "select 17 and (:b or :d); color cpk; spacefill on; "
 				+ "select ligand and (:b or :d); color cpk; spacefill on; ",
 				buttonNameLabel,
 				jmolPanel));
 		buttonPanel.add(makeScriptButton("Show Val 111 and heme.",
 				"select protein or ligand; spacefill off; "
 				+ "wireframe off; dots on; color yellow; "
 				+ "select 111 and (:b or :d); color cpk; spacefill on; "
 				+ "select ligand and (:b or :d); color cpk; spacefill on; ",
 				buttonNameLabel,
 				jmolPanel));
 		buttonPanel.add(makeScriptButton("Show His 63 and heme.",
 				"select protein or ligand; spacefill off; "
 				+ "wireframe off; dots on; color yellow; "
 				+ "select 63 and (:b or :d); color cpk; spacefill on; "
 				+ "select ligand and (:b or :d); color cpk; spacefill on; ",
 				buttonNameLabel,
 				jmolPanel));
 		buttonPanel.add(makeScriptButton("Show Lys 66 and heme.",
 				"select protein or ligand; spacefill off; "
 				+ "wireframe off; dots on; color yellow; "
 				+ "select 66 and (:b or :d); color cpk; spacefill on; "
 				+ "select ligand and (:b or :d); color cpk; spacefill on; ",
 				buttonNameLabel,
 				jmolPanel));
 		buttonPanel.add(new JLabel("<html><br></html>"));	    
 		buttonPanel.add(new JLabel(
 				new ImageIcon(Molecules.class.getResource("cpkColors.gif"))));
 		;
 		
 		return buttonPanel;
 	}
 	
 	
 	public JButton makeLoadStructureButton(final String buttonLabel, 
 			final String pdbFile,
 			final String script,
 			final JLabel buttonNameLabel,
 			JmolPanel jmolPanel){
 		final JmolViewer viewer = jmolPanel.getViewer();
 	
 		JButton button = new JButton("<html><font color=green>"
 				+ buttonLabel
 				+ "</font></html>");
 		button.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				viewer.openStringInline(getPDBasString(pdbFile));
 				if (script != null){
 					viewer.evalString(script);
 				}
 				buttonNameLabel.setText("\"" + buttonLabel + "\"");
 			}
 		});
 		return button;
 	}
 	
 	public JButton makeSizeSensitiveLoadStructureButton(final String buttonLabel, 
 			final String pdbFile,
 			final String script,
 			final JLabel buttonNameLabel,
 			final JRadioButton controlButton,
 			final boolean cpk,
 			JmolPanel jmolPanel){
 		final JmolViewer viewer = jmolPanel.getViewer();
 		
 		JButton button = new JButton("<html><font color=green>"
 				+ buttonLabel
 				+ "</font></html>");
 		button.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				viewer.openStringInline(getPDBasString(pdbFile));
 				String sizeString = "spacefill on;";
 				if (controlButton.isSelected()){
 					sizeString = "spacefill 0.5; wireframe 0.2; ";
 				}
 				if (script != null){
 					if (cpk) {
 					    viewer.evalString(script + sizeString +
 					         "select all; color cpk; ");
 					} else {
 						viewer.evalString(script + sizeString +
 								"select all; ");
 					}
 				}
 				buttonNameLabel.setText("\"" + buttonLabel + "\"");
 			}
 		});
 		return button;
 	}
 	
 	public JButton makeSensitiveLoadStructureButton(final String buttonLabel, 
 			final String pdbFile,
 			final String baseScript,
 			final JLabel buttonNameLabel,
 			final JCheckBox drugCheckBox,
 			final JCheckBox coxCheckBox,
 			final JCheckBox AcheckBox,
 			final JCheckBox BcheckBox,
 			final JCheckBox CcheckBox,
 			final JCheckBox DcheckBox,
 			JmolPanel jmolPanel){
 		final JmolViewer viewer = jmolPanel.getViewer();
 		
 		JButton button = new JButton("<html><font color=green>"
 				+ buttonLabel
 				+ "</html></html>");
 		button.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				viewer.openStringInline(getPDBasString(pdbFile));
 				StringBuffer addOnScript = new StringBuffer();
 				if (baseScript != null){
 					if (drugCheckBox.isSelected()){
 						addOnScript.append(drugDotsScript);
 					} else {
 						addOnScript.append(drugSfScript);
 					}
 					
 					if (coxCheckBox.isSelected()){
 						addOnScript.append(coxDotsScript);
 					} else {
 						addOnScript.append(coxSfScript);
 					}
 					
 					if (AcheckBox.isSelected()){
 						addOnScript.append(ApurpScript);
 					} else {
 						addOnScript.append(AcpkScript);
 					}
 					
 					if (BcheckBox.isSelected()){
 						addOnScript.append(BgreenScript);
 					} else {
 						addOnScript.append(BcpkScript);
 					}
 					
 					if (CcheckBox.isSelected()){
 						addOnScript.append(CpinkScript);
 					} else {
 						addOnScript.append(CcpkScript);
 					}
 					
 					if (DcheckBox.isSelected()){
 						addOnScript.append(DwhiteScript);
 					} else {
 						addOnScript.append(DcpkScript);
 					}
 					
 					viewer.evalString(baseScript + addOnScript.toString());
 					buttonNameLabel.setText("\"" + buttonLabel + "\"");
 				}
 			}
 		});
 		return button;
 	}
 	
 	public JButton makeScriptButton(final String buttonLabel, 
 			final String script,
 			final JLabel buttonNameLabel,
 			JmolPanel jmolPanel){
 		final JmolViewer viewer = jmolPanel.getViewer();
 		
 		JButton button = new JButton(buttonLabel);
 		button.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				viewer.evalString(script);
 				buttonNameLabel.setText("\"" + buttonLabel + "\"");
 			}
 		});
 		return button;
 	}
 	
 	public JButton makeSizeSensitiveScriptButton(final String buttonLabel, 
 			final String script,
 			final JLabel buttonNameLabel,
 			final JRadioButton controlButton,
 			final boolean zoom,
 			JmolPanel jmolPanel){
 		final JmolViewer viewer = jmolPanel.getViewer();
 		
 		JButton button = new JButton(buttonLabel);
 		
 		button.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				String sizeString = "spacefill on;";
 				if (controlButton.isSelected()){
 					sizeString = "spacefill 0.5; wireframe 0.2; ";
 				} 
 				if (zoom){
 				    viewer.evalString(script + sizeString
 						+ "center selected; zoom 400; ");
 				} else {
 				    viewer.evalString(script + sizeString
 							+ "center selected; ");					
 				}
 				buttonNameLabel.setText("\"" + buttonLabel + "\"");
 			}
 		});
 		return button;
 	}
 	
 	public String getPDBasString(String PDBfileName){
 		StringBuffer moleculeString = new StringBuffer();
 		URL moleculeURL = Molecules.class.getResource(PDBfileName);
 		InputStream moleculeInput = null;
 		try {
 			moleculeInput = moleculeURL.openStream();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		BufferedReader moleculeStream = 
 			new BufferedReader(new InputStreamReader(moleculeInput));
 		String line = null;
 		try {
 			while ((line = moleculeStream.readLine())	!= null ){
 				moleculeString.append(line);
 				moleculeString.append("\n");
 			}
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 		return moleculeString.toString();
 	}
 	
 	class ApplicationCloser extends WindowAdapter {
 		public void windowClosing(WindowEvent e) {
 			System.exit(0);
 		}
 	}
 	
 	class JmolPanel extends JPanel 
 	implements JmolStatusListener {
 		JmolViewer viewer;
 		JmolAdapter adapter;
 		JmolPanel() {
 			adapter = new SmarterJmolAdapter(null);
 			viewer = org.jmol.viewer.Viewer.allocateViewer(this, adapter);
 			viewer.setJmolStatusListener(this);
 		}
 		
 		public JmolViewer getViewer() {
 			return viewer;
 		}
 		
 		final Dimension currentSize = new Dimension();
 		final Rectangle rectClip = new Rectangle();
 		
 		public void paint(Graphics g) {
 			getSize(currentSize);
 			g.getClipBounds(rectClip);
 			viewer.renderScreenImage(g, currentSize, rectClip);
 		}
 		
 		//jmol status listener methods
 		
 		public void scriptStatus(String statusString) {
 			if (statusString.equals("Script completed")){
 				statusLabel.setText("<html><font color=green>"
 						+ "Ready</font></html>");
 				return;
 			} 
 			if (statusString.endsWith("atoms selected")) {
 				statusLabel.setText("<html><font color=red>"
 						+ "Animation running.</font></html>");
 				return;
 			}
 			statusLabel.setText("<html><font color=red>" 
 					+ statusString 
 					+ "</font></html>");
 			
 		}
 		
 		public void notifyAtomPicked(int arg0, String atomInfo) {
 			String editedAtomInfo = new String("");
 			if (problemPane.getSelectedIndex() < 2){
 				editedAtomInfo = justAtomNames(atomInfo);
 			} else {
 				editedAtomInfo = aminoAcidInfo(atomInfo);
 			}
 			statusLabel.setText("You just clicked: " + editedAtomInfo);
 		}
 		
 		public void notifyFileLoaded(String arg0, String arg1, String arg2, Object arg3, String arg4) {}
 		public void scriptEcho(String arg0) {}
 		public void setStatusMessage(String arg0) {}
 		public void notifyScriptTermination(String arg0, int arg1) {}
 		public void handlePopupMenu(int arg0, int arg1) {}
 		public void notifyMeasurementsChanged() {}
 		public void notifyFrameChanged(int arg0) {}
 		public void showUrl(String arg0) {}
 		public void showConsole(boolean arg0) {}
 	}
 	
 	public String justAtomNames(String atomInfo){
 		String atom = String.valueOf(atomInfo.charAt(
 				atomInfo.indexOf(".") + 1));
 		if (atom.equals("H")) return new String("An Hydrogen Atom");
 		if (atom.equals("C")) return new String("A Carbon Atom");
 		if (atom.equals("N")) return new String("A Nitrogen Atom");
 		if (atom.equals("O")) return new String("An Oxygen Atom");
 		return new String("A " + atom + " Atom");
 	}
 	
 	public String aminoAcidInfo(String atomInfo){
 		int firstOpenBracket = atomInfo.indexOf("[");
 		int firstCloseBracket = atomInfo.indexOf("]");
 		int firstDot = atomInfo.indexOf(".", firstCloseBracket);
 		int star = atomInfo.indexOf("*");
 		String name = atomInfo.substring(firstOpenBracket + 1, firstCloseBracket);
 		String num = atomInfo.substring(firstCloseBracket + 1, firstDot);
 		
 		if (num.indexOf(":") != -1) {
 			num = num.substring(0, num.indexOf(":"));
 		}
 		if (name.equals("A") || name.equals("G") 
 				|| name.equals("C") || name.equals("T")){
 			String carbonNumber = "";
 			if (star != -1) {
 				if (atomInfo.substring(star - 2, star - 1).equals("C")) {
 			        carbonNumber = "; " + atomInfo.substring(star - 1, star) + "\' Carbon";
 				}
 			}
 			return new String("DNA base " + name + " ; Number " + num + carbonNumber);
 		}
 		
 		if (name.equals("YRR")){
 			return new String("Modified DNA base; Number " + num);
 		}
 		
 		if (name.equals("S58")){
 			return new String("the Drug molecule");
 		}
 		
 		if (name.equals("HEM")){
 			return new String("Heme group");
 		}
 		
 		return new String("Amino acid " + name + " ; Number " + num);
 	}
 	
 }
