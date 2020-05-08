 /**
  * Brian White Fall 2004
  * 
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation; either version 2 of the License, or (at your option) any later
  * version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
  * Place - Suite 330, Boston, MA 02111-1307, USA.
  * 
  * @author Brian White
  * 
  */
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JEditorPane;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
 public class MolCalc extends JFrame {
 
 	final static String versionNumber = new String("2.3");
 	JLabel outputInfo;
 	JLabel jmeLabel ;
 	JPanel topPanel;
 	JPanel bottomPanel;
 	JButton calculateButton;
 	JButton aboutButton;
 	JButton helpButton;
 
 	JME myJME = new JME();
 
 	public MolCalc() {
 		super ("Molecular Properties Calculator Version " + versionNumber);
 		outputInfo = new JLabel("Ready");
 		jmeLabel = new JLabel("<html><body><font size=-2>" +
 		"JME Editor courtesy of Peter Ertl, Novartis</body></html>");
 		topPanel = new JPanel();
 		bottomPanel = new JPanel();
 		calculateButton = new JButton("Calculate Formula and logP");
 		aboutButton = new JButton("About MolCalc");
 		helpButton = new JButton("Help");
 		bottomPanel.setLayout(new BorderLayout());
 		outputInfo.setSize(new Dimension(450,80));
 		bottomPanel.add(outputInfo, BorderLayout.CENTER);
 		bottomPanel.add(calculateButton, BorderLayout.SOUTH);
 		bottomPanel.setPreferredSize(new Dimension(480,100));
 
 		topPanel.add(helpButton);
 		topPanel.add(jmeLabel);
 		topPanel.add(aboutButton);
 
 		topPanel.setPreferredSize(new Dimension(480,50));
 
 		setSize(500,600);
 		getContentPane().setLayout(new BorderLayout());
 
 		getContentPane().add(topPanel, BorderLayout.NORTH);
 		getContentPane().add(myJME, BorderLayout.CENTER);
 		getContentPane().add(bottomPanel, BorderLayout.SOUTH);
 
 		myJME.init();
 		myJME.start();
 
 		outputInfo.setText("Ready for input");
 
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		Dimension screenSize = this.getToolkit().getScreenSize();
 		setLocation((screenSize.width / 4), (screenSize.height / 4));
 
 		aboutButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				JOptionPane.showMessageDialog(null,
 						"<html><body>"
 						+ "<center>Molecular Calculator Version " 
 						+ versionNumber + "<br>"
 						+ "<br>"
 						+ "Brian White (2005)<br>"
 						+ "brian.white@umb.edu<br>"
 						+ "JME Editor courtesy of Peter Ertl, Novartis"
 						+ "</body></html>",
 						"About MolCalc",
 						JOptionPane.PLAIN_MESSAGE);
 			}
 		});
 
 		helpButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				final JEditorPane helpPane = new JEditorPane();
 				helpPane.setEditable(false);
 				helpPane.setContentType("text/html");
 
 				try {
 					helpPane.setPage(MolCalc.class.getResource("index.html"));
 				} catch (Exception ex) {
 					JOptionPane.showMessageDialog(null,
 							"Be sure the help folder is in the same folder as the program.",
 							"Can't find help file.", JOptionPane.ERROR_MESSAGE);
 					return;
 				}
 
 				JScrollPane helpScrollPane = new JScrollPane(helpPane);
 				JDialog helpDialog = new JDialog(getMasterFrame(), "Molecular Properties Calculator Help");
 				helpDialog.getContentPane().setLayout(new BorderLayout());
 				helpDialog.getContentPane().add(helpScrollPane, BorderLayout.CENTER);
 				Dimension screenSize = getMasterFrame().getToolkit().getScreenSize();
 				helpDialog.setBounds((screenSize.width / 2), (screenSize.height / 2),
 						(screenSize.width * 4 / 10), (screenSize.height * 4 / 10));
 				helpDialog.show();
 			}
 
 		});
 
 
 		calculateButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				computeAndDisplay(myJME.molFile(), myJME.smiles(), myJME.jmeFile(), outputInfo);
 			}
 
 		});
 
 	}
 
 	public static void main(String[] args) {
 		MolCalc myMolCalc = new MolCalc();
 		myMolCalc.setVisible(true);
 	}
 
 	JFrame getMasterFrame() {
 		return this;
 	}
 
 	String computeAndDisplay(String molString, String smileString,
 			String jmeString, JLabel outputInfo) {
 
 		if (molString.equals("") || smileString.equals("") || jmeString.equals("")) {
 			outputInfo.setText("");
 			return new String("");
 		}
 
 		outputInfo.setText("");
 		outputInfo.setForeground(Color.BLACK);
 
 		StringBuffer atomDataLines = new StringBuffer(); //for output
 		String logpString = new String();
 		String formulaString = new String();
 		String errorString = new String();
 
 		ArrayList atomList = new ArrayList();
 		atomList.add(new Atom()); // make atom[0] a blank
 
 		// get the molecule data from the molstring
 		String[] molStringLines = molString.split("\n");
 
 		//        for (int i = 0; (i < molStringLines.length); i++) {
 		//            System.out.println(i + ":" + molStringLines[i]);
 		//        }
 
 		//get number of bonds & number of atoms
 		String[] line3Parts = molStringLines[3].split("[ ]+");
 		int numAtoms = Integer.parseInt(line3Parts[1]);
 		int numBonds = Integer.parseInt(line3Parts[2]);
 
 		//read the atom lines & create appropriate atoms
 		for (int i = 1; i < (numAtoms + 1); i++) {
 			String[] atomLineParts = molStringLines[i + 3].split("[ ]+");
 			String element = new String(atomLineParts[4]);
 			Atom atom = new Atom();
 			atom.setElement(element);
 			atomList.add(atom);
 		}
 
 		//read the charge lines & set atom charge
 		for (int i = 0; i < molStringLines.length; i++) {
 			Pattern chargeLine = Pattern.compile("CHG");
 			Matcher chargeLineMatcher = chargeLine.matcher(molStringLines[i]);
 			if (chargeLineMatcher.find()) {
 				String[] chargeLineParts = molStringLines[i].split("[ ]+");
 				int atomNumber = Integer.parseInt(chargeLineParts[3]);
 				int charge = Integer.parseInt(chargeLineParts[4]);
 				Atom atom = (Atom) atomList.get(atomNumber);
 				atom.setCharge(charge);
 			}
 		}
 
 		// fill the bond array with 0's
 		int[][] bondArray = new int[numAtoms + 1][numAtoms + 1];
 		for (int i = 1; i < (numAtoms + 1); i++) {
 			Arrays.fill(bondArray[i], 0);
 		}
 
 		//read in the bond lines & fill the bondArray
 		for (int i = 1; (i < numBonds + 1); i++) {
 			String[] bondLineParts = molStringLines[i + numAtoms + 3]
 			                                        .split("[ ]+");
 			int firstAtom = Integer.parseInt(bondLineParts[1]);
 			int secondAtom = Integer.parseInt(bondLineParts[2]);
 			int bondIndex = Integer.parseInt(bondLineParts[3]);
 			bondArray[firstAtom][secondAtom] = bondIndex;
 			bondArray[secondAtom][firstAtom] = bondIndex;
 		}
 
 		// go thru data & process each atom's bonds & neighbors
 		// round 1: get hybridization info
 		for (int i = 1; i < (numAtoms + 1); i++) {
 			Atom currentAtom = (Atom) atomList.get(i); // get the center of this
 			// group
 			for (int j = 1; j < (numAtoms + 1); j++) {
 				if (bondArray[i][j] != 0) {
 					currentAtom.updateHybridization(bondArray[i][j]);
 				}
 			}
 		}
 
 		//round 2: find if aromatic
 		// to be aromatic, it must be a 6-membered ring of sp2 carbons/nitrogens
 		// with alternating
 		// single & double bonds
 
 		for (int i = 1; i < (numAtoms + 1); i++) {
 			Atom firstAtom = (Atom) atomList.get(i);
 			if ((firstAtom.getElement().equals("C") || firstAtom.getElement()
 					.equals("N"))
 					&& (firstAtom.getHybridization() == 2 || firstAtom
 							.getAromatic())) {
 				for (int j = 1; j < (numAtoms + 1); j++) {
 					Atom secondAtom = (Atom) atomList.get(j);
 					if ((bondArray[i][j] == 1
 							|| bondArray[i][j] == 2
 							|| (secondAtom.getAromatic() && bondArray[i][j] != 0) || (firstAtom
 									.getAromatic() && bondArray[i][j] != 0))
 									&& (secondAtom.getElement().equals("C") || secondAtom
 											.getElement().equals("N"))
 											&& (secondAtom.getHybridization() == 2) && (i != j)) {
 						for (int k = 1; k < (numAtoms + 1); k++) {
 							Atom thirdAtom = (Atom) atomList.get(k);
 							if (((bondArray[j][k] == 1 && bondArray[i][j] == 2) //these
 									// first
 									// 2
 									// look
 									// for
 									// alternating
 									|| (bondArray[j][k] == 2 && bondArray[i][j] == 1) //single
 									// &
 									// double
 									// bonds
 									|| (thirdAtom.getAromatic() && bondArray[j][k] != 0) // or a
 									// bond
 									// to
 									// an
 									// aro
 									// atom
 									|| (secondAtom.getAromatic() && bondArray[j][k] != 0) // or a
 									// bond
 									// from
 									// an
 									// aro
 									// atom
 							)
 							&& (thirdAtom.getElement().equals("C") || thirdAtom
 									.getElement().equals("N")) //must
 									// be C/N
 									&& (thirdAtom.getHybridization() == 2) //must
 									// be
 									// sp2
 									&& ((k != i) && (k != j)) //must not be any
 									// other atom so
 									// far
 							) { //in this chain (no backtracking)
 								for (int l = 1; l < (numAtoms + 1); l++) {
 									Atom fourthAtom = (Atom) atomList.get(l);
 									if (((bondArray[k][l] == 1 && bondArray[j][k] == 2)
 											|| (bondArray[k][l] == 2 && bondArray[j][k] == 1)
 											|| (fourthAtom.getAromatic() && bondArray[k][l] != 0) || (thirdAtom
 													.getAromatic() && bondArray[k][l] != 0))
 													&& (fourthAtom.getElement().equals(
 													"C") || fourthAtom
 													.getElement().equals("N"))
 													&& (fourthAtom.getHybridization() == 2)
 													&& ((l != i) && (l != j) && (l != k))) {
 										for (int m = 1; m < (numAtoms + 1); m++) {
 											Atom fifthAtom = (Atom) atomList
 											.get(m);
 											if (((bondArray[l][m] == 1 && bondArray[k][l] == 2)
 													|| (bondArray[l][m] == 2 && bondArray[k][l] == 1)
 													|| (fifthAtom.getAromatic() && bondArray[l][m] != 0) || (fourthAtom
 															.getAromatic() && bondArray[l][m] != 0))
 															&& (fifthAtom.getElement()
 																	.equals("C") || fifthAtom
 																	.getElement()
 																	.equals("N"))
 																	&& (fifthAtom
 																			.getHybridization() == 2)
 																			&& ((m != i) && (m != j)
 																					&& (m != k) && (m != l))) {
 												for (int n = 1; n < (numAtoms + 1); n++) {
 													Atom sixthAtom = (Atom) atomList
 													.get(n);
 													if (((bondArray[m][n] == 1 && bondArray[l][m] == 2)
 															|| (bondArray[m][n] == 2 && bondArray[l][m] == 1)
 															|| (sixthAtom
 																	.getAromatic() && bondArray[m][n] != 0) || (fifthAtom
 																			.getAromatic() && bondArray[m][n] != 0))
 																			&& (sixthAtom
 																					.getElement()
 																					.equals("C") || sixthAtom
 																					.getElement()
 																					.equals("N"))
 																					&& (sixthAtom
 																							.getHybridization() == 2)
 																							&& ((n != i)
 																									&& (n != j)
 																									&& (n != k)
 																									&& (n != l) && (n != m))) {
 														//now we have 6 sp2 N/C
 														// connected by
 														// alternating
 														//single & double bonds
 														// if 1 connects to 6
 														// properly, we have an
 														// aromatic ring
 														if ((bondArray[n][i] == 2 && bondArray[m][n] == 1)
 																|| (bondArray[n][i] == 1 && bondArray[m][n] == 2)) {
 															firstAtom
 															.setAromatic();
 															secondAtom
 															.setAromatic();
 															thirdAtom
 															.setAromatic();
 															fourthAtom
 															.setAromatic();
 															fifthAtom
 															.setAromatic();
 															sixthAtom
 															.setAromatic();
 														}
 													}
 												}
 											}
 										}
 									}
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 
 		//round 3: neighbor counts
 		for (int i = 1; i < (numAtoms + 1); i++) {
 			Atom currentAtom = (Atom) atomList.get(i); // get the center of this
 			// group
 			for (int j = 1; j < (numAtoms + 1); j++) {
 				if (bondArray[i][j] != 0) {
 					Atom currentNeighbor = (Atom) atomList.get(j);
 					currentAtom.processNeighbor(currentNeighbor.getElement(),
 							bondArray[i][j], currentNeighbor.getCharge(),
 							currentNeighbor.getAromatic());
 				}
 			}
 		}
 
 		//round 4: check for pi-bonded neighbors
 		//be careful - the carbons in ethylene have no pi neighbors
 		//since the pi bond is between them
 		//to be a pi neighbor, it must be sp2/sp and double-bonded to
 		//a different atom than the center
 		//first, go atom-by-atom
 		for (int i = 1; i < (numAtoms + 1); i++) {
 			Atom currentAtom = (Atom) atomList.get(i);
 			//then check all of its neighbors
 			for (int j = 1; j < (numAtoms + 1); j++) {
 				if (bondArray[i][j] != 0) {
 					Atom currentNeighbor = (Atom) atomList.get(j);
 					if (currentNeighbor.getHybridization() < 3) { //sp2 or sp
 						// neighbor
 						//go thru the neighbor's neighbors
 						for (int k = 1; k < (numAtoms + 1); k++) {
 							if ((bondArray[j][k] > 1) && (k != i)) {
 								currentAtom.incNumNeighborPi();
 							}
 						}
 					}
 				}
 			}
 
 		}
 
 		//round 5: calculate number of H neighbors for each atom and sum up charge
 		int h = 0;		
 		int charge = 0;
 
 		for (int i = 1; i < (numAtoms + 1); i++) {
 			Atom atom = (Atom) atomList.get(i);
 			atom.getNumNeighborHs();
 			h = h + atom.numNeighborHs;
 			charge = charge + atom.charge;
 		}
 
 		//round 6: see if an amide (N with neighbor carbonyl)
 		for (int i = 1; i < (numAtoms + 1); i++) {
 			Atom currentAtom = (Atom) atomList.get(i); // get the center of this
 			// group
 			for (int j = 1; j < (numAtoms + 1); j++) {
 				if (bondArray[i][j] != 0) {
 					Atom currentNeighbor = (Atom) atomList.get(j);
 					if (currentAtom.getElement().equals("N")
 							&& currentNeighbor.isACarbonyl) {
 						currentAtom.setAmide();
 					}
 				}
 			}
 		}
 
 		//round 7: check for "illegal" atoms
 		StringBuffer illegalAtoms = new StringBuffer();
 		for (int i = 1; i < (numAtoms + 1); i++) {
 			Atom atom = (Atom) atomList.get(i);
 			if (atom.getElement().equals("C")) {
 				if (atom.getCharge() != 0)
 					illegalAtoms.append("A Charged C atom.<br>");
 			}
 			if (atom.getElement().equals("N")) {
 				if (atom.getCharge() > 1)
 					illegalAtoms
 					.append("An N atom with too high + charge.<br>");
 				if (atom.getCharge() < 0)
 					illegalAtoms.append("An N atom with - charge.<br>");
 				if ((atom.getNumNeighborHs() < 0)
 						&& !(atom.doubleBondedNeighbor.equals("O2")
 								&& (atom.getNumNeighborHs() == -2)
 								&& (atom.getNumNeighborCs() == 1)
 								&& (atom.getNumNeighborXs() == 2) && (atom
 										.getCharge() == 0))) { //don't worry if nitro
 					illegalAtoms.append("An N atom making too many bonds.<br>");
 				}
 			}
 			if (atom.getElement().equals("O")) {
 				if (atom.getCharge() < -1)
 					illegalAtoms
 					.append("An O atom with too high - charge.<br>");
 				if (atom.getCharge() > 0)
 					illegalAtoms.append("An O atom with + charge.<br>");
 			}
 			if (atom.getElement().equals("S")) {
 				if (atom.getCharge() > 0)
 					illegalAtoms.append("An S atom with a + charge.<br>");
 				if ((atom.getNumNeighborHs() < 0)
 						&& (atom.getNumNeighborHs() != -4)
 						&& (atom.getNumNeighborHs() != -2)) {
 					illegalAtoms
 					.append("An S atom not making 2, 4, or 6 bonds.<br>");
 				}
 			}
 			if (atom.getElement().equals("P")) {
 				if (atom.getCharge() != 0)
 					illegalAtoms.append("A Charged P atom.<br>");
 				if (atom.numNeighborHs != -3) {
 					illegalAtoms
 					.append("A P atom not making 5 bonds.<br>");
 				}
 			}
 			if (atom.getElement().equals("F") || atom.getElement().equals("Cl")
 					|| atom.getElement().equals("Br")
 					|| atom.getElement().equals("I")) {
 				if (atom.getCharge() != 0)
 					illegalAtoms.append("A Charged " + atom.getElement()
 							+ " atom.<br>");
 			}
 			if (atom.getElement().equals("X")) {
 				illegalAtoms.append("An X atom.<br>");
 			}
 		}
 		errorString = "";
 		if (illegalAtoms.length() != 0) {
 			errorString = "<html><body>It is not possible to calculate logp<br>"
 				+ "for your molecule because it contains:<br>"
 				+ illegalAtoms.toString() + "</body></html>";
 		}
 
 		//print out to stdout various debugging info
 		//        for (int i = 1; i < (numAtoms + 1); i++) {
 		//            StringBuffer neighborList = new StringBuffer();
 		//            Atom atom = (Atom)atomList.get(i);
 		//            for (int j = 1; j < (numAtoms + 1); j++) {
 		//                if (bondArray[i][j] != 0) {
 		//                    neighborList.append(j + " ");
 		//                }
 		//            }
 		//            System.out.println(i + " " + atom.toString() + " bonded to: "
 		//                + neighborList.toString());
 		//        }
 		//        System.out.println("");
 
 		//compute total logp = sum of individual atom contributions.
 		double logp = 0.000;
 		if (illegalAtoms.length() == 0) {
 			NumberFormat nf = NumberFormat.getInstance();
 			nf.setMaximumFractionDigits(3);
 
 			for (int i = 1; i < (numAtoms + 1); i++) {
 				Atom atom = (Atom) atomList.get(i);
 				AtomSpec currentAtomSpec = atom.getAtomSpec();
 				logp = logp + currentAtomSpec.getAtomLogp();
 
 				StringBuffer neighbors = new StringBuffer();
 				for (int j = 1; j < (numAtoms + 1); j++) {
 					if (bondArray[i][j] != 0) {
 						neighbors.append(j + " ");
 					}
 				}
 
 				atomDataLines.append(i + " " + currentAtomSpec.getAtomType()
 						+ "; bonded to: " + neighbors.toString() + "; logp= "
 						+ nf.format(currentAtomSpec.getAtomLogp()) + "\n");
 			}
 			if (logp < 0) {
 				logpString = "<font color=green>logp = " + nf.format(logp)
 				+ "</font>";
 			} else {
 				logpString = "<font color=red>logp = " + nf.format(logp)
 				+ "</font>";
 			}
 		}
 
 		//now compute the formula
 		// set the counters to zero
 		numBonds = 0;
 		int numAromaticAtoms = 0;
 		int c = 0;
 		int n = 0;
 		int o = 0;
 		int s = 0;
 		int p = 0;
 		int cl = 0;
 		int br = 0;
 		int f = 0;
 
 		StringBuffer formula = new StringBuffer();
 
 		for (int i = 0; i < smileString.length(); i++) {
 			switch (smileString.charAt(i)) {
 			case 'C':
 				c++;
 				break;
 				// remove H counting since using numNeighborH's works better
 				//			case 'H':  
 				//				h++;
 				//				break;
 			case 'N':
 				n++;
 				break;
 			case 'O':
 				o++;
 				break;
 			case 'S':
 				s++;
 				break;
 			case 'P':
 				p++;
 				break;
 			case 'l': // the second letter of 'Cl' so the last C was really 'Cl'
 				c--;
 				cl++;
 				break;
 			case 'B':
 				br++;
 				break;
 			case 'F':
 				f++;
 				break;
 			case 'c': //aromatic C
 				c++;
 				numAromaticAtoms++;
 				break;
 			case 'n': //aromatic N
 				n++;
 				numAromaticAtoms++;
 				break;
 			case 's': //aromatic S
 				s++;
 				numAromaticAtoms++;
 				break;
 			case 'o': // aromatic o
 				o++;
 				numAromaticAtoms++;
 				break;
 			case '#': //triple bond
 				numBonds++;
 			case '=': //double bond
 				numBonds++;
 				break;
 			}
 		}
 
 		// old code - doesn't calculate 7-membered aromatic rings with heteroatoms right!
 		// calculate the number of h's
 		//first, the max # of h's per atom
 		//		h = h + 4 * c + 3 * n + 2 * o + 2 * s + 5 * p + cl + br + f;
 
 		//then, take away h's for bonds, charge, etc.
 		//first, get the number of bonds from the jme file
 		//		String[] jmeFilePieces = jmeString.split(" +");
 		//		numBonds = numBonds + Integer.parseInt(jmeFilePieces[1]);
 
 		//then, since the smile string has [NH3+], etc, need to remove the
 		//  H that was counted in the atom count above
 		//		Pattern chargedNPattern = Pattern.compile("[Nn]H[23]?+");
 		//		String[] parts = chargedNPattern.split(smileString);
 		//		int numChargedNWithH = parts.length - 1;
 
 		//		h = h - numChargedNWithH;
 
 		//then the charge - get by adding up lines from mol file
 		//		for (int i = 0; i < molStringLines.length; i++) {
 		//			if (molStringLines[i].indexOf("CHG") != -1) {
 		//				charge = charge
 		//						+ Integer.parseInt(molStringLines[i].substring(
 		//								molStringLines[i].length() - 3).trim());
 		//			}
 		//		}
 
 		//		h = h - 2 * numBonds - numAromaticAtoms + charge;		
 
 		prettyPrint("C", c, formula);
 		prettyPrint("H", h, formula);
 		prettyPrint("N", n, formula);
 		prettyPrint("O", o, formula);
 		prettyPrint("P", p, formula);
 		prettyPrint("S", s, formula);
 		prettyPrint("Cl", cl, formula);
 		prettyPrint("Br", br, formula);
 		prettyPrint("F", f, formula);
 
 		if (charge != 0) {
 			formula.append("(");
 			if (charge < 0) {
 				formula.append("-");
 			} else {
 				formula.append("+");
 			}
 
 			if (Math.abs(charge) != 1) {
 				formula.append(String.valueOf(charge));
 			}
 			formula.append(")");
 		}
 
 		formulaString = "Formula: " + formula.toString();
 
 		//now show it all
 		if (!errorString.equals("")) {
 			outputInfo.setForeground(Color.RED);
 			outputInfo.setText(errorString);
 		} else {
 			outputInfo.setForeground(Color.BLACK);
 			outputInfo.setText("<html><body>" + formulaString + "<br>"
 					+ logpString + "</body></html>");
 		}
 
 		return atomDataLines.toString();
 	}
 
 	private void prettyPrint(String atomLabel, int number,
 			StringBuffer outString) {
 		if (number == 0) {
 			return;
 		}
 		outString.append(atomLabel);
 
 		if (number == 1) {
 			outString.append(" ");
 			return;
 		}
 
 		outString.append("<sub>" + String.valueOf(number) + "</sub> ");
 
 	}
 
 }
