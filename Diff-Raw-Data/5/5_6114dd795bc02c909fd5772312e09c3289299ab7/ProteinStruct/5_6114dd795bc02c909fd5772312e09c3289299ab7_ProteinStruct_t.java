 /*
  * Copyright (c) 2011-2012 Vikram Sundar.
  * All Rights Reserved.
  */
 package org.vikramdock;
 
 import java.io.*;
 import java.util.*;
 import java.util.zip.*;
 import java.lang.*;
 import java.lang.reflect.*;
 
 public class ProteinStruct {
 	ArrayList<Atom> structure;
 	ArrayList<Atom> surface;
 	ArrayList<Bond> bonds;
 	ArrayList<Bond> surfacebonds;
 	ArrayList<Atom> backbone;
 	ArrayList<Atom> surfacebackbone;
 	ArrayList<Bond> backbonebonds;
 	ArrayList<Bond> surfacebackbonebonds;
 	String[] parsedsequence = new String[50];
 	HashMap chaintranslator;
 	HashMap reversechaintrans;
 	HashMap translator1;
 	HashMap translator2;
 	HashMap translator3;
 	HashMap translator4;
 	String filepath;
 	int chaincount;
 	double xcoordcent;
 	double ycoordcent;
 	double zcoordcent;
 	boolean rotated;
 	double size;
 	double[][] sizes = new double[(int)(2*Math.PI/Constants.THETAINC)][(int)(Math.PI/Constants.PHIINC)];
 	double[][][][] potentials;
 	public ProteinStruct(String filepath) {
 		try {
 			chaintranslator = new HashMap();
 			structure = new ArrayList<Atom>();
 			surface = new ArrayList<Atom>();
 			bonds = new ArrayList<Bond>();
 			surfacebonds = new ArrayList<Bond>();
 			backbone = new ArrayList<Atom>();
 			surfacebackbone = new ArrayList<Atom>();
 			backbonebonds = new ArrayList<Bond>();
 			surfacebackbonebonds = new ArrayList<Bond>();
 			translator1 = new HashMap();
 			translator2 = new HashMap();
 			translator3 = new HashMap();
 			translator4 = new HashMap();
 			reversechaintrans = new HashMap();
 			translator1.put("ALA",0);
 			translator1.put("CYS",1);
 			translator1.put("ASP",2);
 			translator1.put("GLU",3);
 			translator1.put("PHE",4);
 			translator1.put("GLY",5);
 			translator1.put("HIS",6);
 			translator1.put("ILE",7);
 			translator1.put("LYS",8);
 			translator1.put("LEU",9);
 			translator1.put("MET",10);
 			translator1.put("ASN",11);
 			translator1.put("PRO",12);
 			translator1.put("GLN",13);
 			translator1.put("ARG",14);
 			translator1.put("SER",15);
 			translator1.put("THR",16);
 			translator1.put("VAL",17);
 			translator1.put("TRP",18);
 			translator1.put("TYR",19);
 			translator2.put("ALA","A");
 			translator2.put("CYS","C");
 			translator2.put("ASP","D");
 			translator2.put("GLU","E");
 			translator2.put("PHE","F");
 			translator2.put("GLY","G");
 			translator2.put("HIS","H");
 			translator2.put("ILE","I");
 			translator2.put("LYS","K");
 			translator2.put("LEU","L");
 			translator2.put("MET","M");
 			translator2.put("ASN","N");
 			translator2.put("PRO","P");
 			translator2.put("GLN","Q");
 			translator2.put("ARG","R");
 			translator2.put("SER","S");
 			translator2.put("THR","T");
 			translator2.put("VAL","V");
 			translator2.put("TRP","W");
 			translator2.put("TYR","Y");
 			translator3.put("A",0);
 			translator3.put("C",1);
 			translator3.put("D",2);
 			translator3.put("E",3);
 			translator3.put("F",4);
 			translator3.put("G",5);
 			translator3.put("H",6);
 			translator3.put("I",7);
 			translator3.put("K",8);
 			translator3.put("L",9);
 			translator3.put("M",10);
 			translator3.put("N",11);
 			translator3.put("P",12);
 			translator3.put("Q",13);
 			translator3.put("R",14);
 			translator3.put("S",15);
 			translator3.put("T",16);
 			translator3.put("V",17);
 			translator3.put("W",18);
 			translator3.put("Y",19);
 			translator4.put("A","ALA");
 			translator4.put("C","CYS");
 			translator4.put("D","ASP");
 			translator4.put("E","GLU");
 			translator4.put("F","PHE");
 			translator4.put("G","GLY");
 			translator4.put("H","HIS");
 			translator4.put("I","ILE");
 			translator4.put("K","LYS");
 			translator4.put("L","LEU");
 			translator4.put("M","MET");
 			translator4.put("N","ASN");
 			translator4.put("P","PRO");
 			translator4.put("Q","GLN");
 			translator4.put("R","ARG");
 			translator4.put("S","SER");
 			translator4.put("T","THR");
 			translator4.put("V","VAL");
 			translator4.put("W","TRP");
 			translator4.put("Y","TYR");
 			this.filepath = filepath;
 			rotated = false;
 			parseSequence(filepath);
 			parseStructure(filepath);
 			determineSurface();
 			potentials = new double[(int)((2*Math.ceil(size/Constants.GRIDGRAINSIZE)*Constants.GRIDGRAINSIZE + 2*Constants.VDWDISTTHRESHOLD)/Constants.GRIDGRAINSIZE)][(int)((2*Math.ceil(size/Constants.GRIDGRAINSIZE)*Constants.GRIDGRAINSIZE + 2*Constants.VDWDISTTHRESHOLD)/Constants.GRIDGRAINSIZE)][(int)((2*Math.ceil(size/Constants.GRIDGRAINSIZE)*Constants.GRIDGRAINSIZE + 2*Constants.VDWDISTTHRESHOLD)/Constants.GRIDGRAINSIZE)][5];
 			detBondsBackbone();
 			detPotentials();
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 	public ProteinStruct(String[] parsedsequence, ArrayList<Atom> surface, ArrayList<Bond> bonds, ArrayList<Atom> backbone, ArrayList<Bond> backbonebonds, double[][] sizes, double[][][][] potentials, int chaincount, double xcoordcent, double ycoordcent, double zcoordcent) {
 		this.parsedsequence = parsedsequence;
 		this.structure = surface;
 		this.surface = surface;
 		this.bonds = bonds;
 		this.surfacebonds = bonds;
 		this.backbone = backbone;
 		this.surfacebackbone = backbone;
 		this.backbonebonds = backbonebonds;
 		this.surfacebackbonebonds = backbonebonds;
 		this.sizes = sizes;
 		this.potentials = potentials;
 		rotated = true;
 		translator1 = new HashMap();
 		translator2 = new HashMap();
 		translator3 = new HashMap();
 		translator4 = new HashMap();
 		translator1.put("ALA",0);
 		translator1.put("CYS",1);
 		translator1.put("ASP",2);
 		translator1.put("GLU",3);
 		translator1.put("PHE",4);
 		translator1.put("GLY",5);
 		translator1.put("HIS",6);
 		translator1.put("ILE",7);
 		translator1.put("LYS",8);
 		translator1.put("LEU",9);
 		translator1.put("MET",10);
 		translator1.put("ASN",11);
 		translator1.put("PRO",12);
 		translator1.put("GLN",13);
 		translator1.put("ARG",14);
 		translator1.put("SER",15);
 		translator1.put("THR",16);
 		translator1.put("VAL",17);
 		translator1.put("TRP",18);
 		translator1.put("TYR",19);
 		translator2.put("ALA","A");
 		translator2.put("CYS","C");
 		translator2.put("ASP","D");
 		translator2.put("GLU","E");
 		translator2.put("PHE","F");
 		translator2.put("GLY","G");
 		translator2.put("HIS","H");
 		translator2.put("ILE","I");
 		translator2.put("LYS","K");
 		translator2.put("LEU","L");
 		translator2.put("MET","M");
 		translator2.put("ASN","N");
 		translator2.put("PRO","P");
 		translator2.put("GLN","Q");
 		translator2.put("ARG","R");
 		translator2.put("SER","S");
 		translator2.put("THR","T");
 		translator2.put("VAL","V");
 		translator2.put("TRP","W");
 		translator2.put("TYR","Y");
 		translator3.put("A",0);
 		translator3.put("C",1);
 		translator3.put("D",2);
 		translator3.put("E",3);
 		translator3.put("F",4);
 		translator3.put("G",5);
 		translator3.put("H",6);
 		translator3.put("I",7);
 		translator3.put("K",8);
 		translator3.put("L",9);
 		translator3.put("M",10);
 		translator3.put("N",11);
 		translator3.put("P",12);
 		translator3.put("Q",13);
 		translator3.put("R",14);
 		translator3.put("S",15);
 		translator3.put("T",16);
 		translator3.put("V",17);
 		translator3.put("W",18);
 		translator3.put("Y",19);
 		translator4.put("A","ALA");
 		translator4.put("C","CYS");
 		translator4.put("D","ASP");
 		translator4.put("E","GLU");
 		translator4.put("F","PHE");
 		translator4.put("G","GLY");
 		translator4.put("H","HIS");
 		translator4.put("I","ILE");
 		translator4.put("K","LYS");
 		translator4.put("L","LEU");
 		translator4.put("M","MET");
 		translator4.put("N","ASN");
 		translator4.put("P","PRO");
 		translator4.put("Q","GLN");
 		translator4.put("R","ARG");
 		translator4.put("S","SER");
 		translator4.put("T","THR");
 		translator4.put("V","VAL");
 		translator4.put("W","TRP");
 		translator4.put("Y","TYR");
 		filepath = "INVALID";
 		this.chaincount = chaincount;
 		this.xcoordcent = xcoordcent;
 		this.ycoordcent = ycoordcent;
 		this.zcoordcent = zcoordcent;
 	}
 	public ProteinStruct(ProteinStruct clone) {
 		this.filepath = clone.getFilepath();
 		structure = new ArrayList<Atom>();
 		surface = new ArrayList<Atom>();
 		bonds = new ArrayList<Bond>();
 		surfacebonds = new ArrayList<Bond>();
 		backbone = new ArrayList<Atom>();
 		surfacebackbone = new ArrayList<Atom>();
 		backbonebonds = new ArrayList<Bond>();
 		surfacebackbonebonds = new ArrayList<Bond>();
 		translator1 = new HashMap();
 		translator2 = new HashMap();
 		translator3 = new HashMap();
 		translator4 = new HashMap();
 		reversechaintrans = new HashMap();
 		ArrayList clonestruct = clone.getStructure();
 		for (int i = 0; i < clonestruct.size(); i++) {
 			Atom current = (Atom)clonestruct.get(i);
 			this.structure.add(new Atom(current));
 		}
 		clonestruct = clone.getSurface();
 		for (int i = 0; i < clonestruct.size(); i++) {
 			Atom current = (Atom)clonestruct.get(i);
 			this.surface.add(new Atom(current));
 		}
 		clonestruct = clone.getBackbone();
 		for (int i = 0; i < clonestruct.size(); i++) {
 			Atom current = (Atom)clonestruct.get(i);
 			this.backbone.add(new Atom(current));
 		}
 		clonestruct = clone.getSurfaceBackbone();
 		for (int i = 0; i < clonestruct.size(); i++) {
 			Atom current = (Atom)clonestruct.get(i);
 			this.surfacebackbone.add(new Atom(current));
 		}
 		ArrayList clonebonds = clone.getBonds();
 		for (int i = 0; i < clonebonds.size(); i++) {
 			Bond current = (Bond)clonebonds.get(i);
 			this.bonds.add(new Bond(current));
 		}
 		clonebonds = clone.getSurfaceBonds();
 		for (int i = 0; i < clonebonds.size(); i++) {
 			Bond current = (Bond)clonebonds.get(i);
 			this.surfacebonds.add(new Bond(current));
 		}
 		clonebonds = clone.getBackboneBonds();
 		for (int i = 0; i < clonebonds.size(); i++) {
 			Bond current = (Bond)clonebonds.get(i);
 			this.backbonebonds.add(new Bond(current));
 		}
 		clonebonds = clone.getSurfaceBackboneBonds();
 		for (int i = 0; i < clonebonds.size(); i++) {
 			Bond current = (Bond)clonebonds.get(i);
 			this.surfacebackbonebonds.add(new Bond(current));
 		}
 		translator1.put("ALA",0);
 		translator1.put("CYS",1);
 		translator1.put("ASP",2);
 		translator1.put("GLU",3);
 		translator1.put("PHE",4);
 		translator1.put("GLY",5);
 		translator1.put("HIS",6);
 		translator1.put("ILE",7);
 		translator1.put("LYS",8);
 		translator1.put("LEU",9);
 		translator1.put("MET",10);
 		translator1.put("ASN",11);
 		translator1.put("PRO",12);
 		translator1.put("GLN",13);
 		translator1.put("ARG",14);
 		translator1.put("SER",15);
 		translator1.put("THR",16);
 		translator1.put("VAL",17);
 		translator1.put("TRP",18);
 		translator1.put("TYR",19);
 		translator2.put("ALA","A");
 		translator2.put("CYS","C");
 		translator2.put("ASP","D");
 		translator2.put("GLU","E");
 		translator2.put("PHE","F");
 		translator2.put("GLY","G");
 		translator2.put("HIS","H");
 		translator2.put("ILE","I");
 		translator2.put("LYS","K");
 		translator2.put("LEU","L");
 		translator2.put("MET","M");
 		translator2.put("ASN","N");
 		translator2.put("PRO","P");
 		translator2.put("GLN","Q");
 		translator2.put("ARG","R");
 		translator2.put("SER","S");
 		translator2.put("THR","T");
 		translator2.put("VAL","V");
 		translator2.put("TRP","W");
 		translator2.put("TYR","Y");
 		translator3.put("A",0);
 		translator3.put("C",1);
 		translator3.put("D",2);
 		translator3.put("E",3);
 		translator3.put("F",4);
 		translator3.put("G",5);
 		translator3.put("H",6);
 		translator3.put("I",7);
 		translator3.put("K",8);
 		translator3.put("L",9);
 		translator3.put("M",10);
 		translator3.put("N",11);
 		translator3.put("P",12);
 		translator3.put("Q",13);
 		translator3.put("R",14);
 		translator3.put("S",15);
 		translator3.put("T",16);
 		translator3.put("V",17);
 		translator3.put("W",18);
 		translator3.put("Y",19);
 		translator4.put("A","ALA");
 		translator4.put("C","CYS");
 		translator4.put("D","ASP");
 		translator4.put("E","GLU");
 		translator4.put("F","PHE");
 		translator4.put("G","GLY");
 		translator4.put("H","HIS");
 		translator4.put("I","ILE");
 		translator4.put("K","LYS");
 		translator4.put("L","LEU");
 		translator4.put("M","MET");
 		translator4.put("N","ASN");
 		translator4.put("P","PRO");
 		translator4.put("Q","GLN");
 		translator4.put("R","ARG");
 		translator4.put("S","SER");
 		translator4.put("T","THR");
 		translator4.put("V","VAL");
 		translator4.put("W","TRP");
 		translator4.put("Y","TYR");
 		this.chaincount = clone.getChaincount();
 		this.xcoordcent = clone.getXCoordCent();
 		this.ycoordcent = clone.getYCoordCent();
 		this.zcoordcent = clone.getZCoordCent();
 		this.parsedsequence = clone.getParsedsequence();
 		this.rotated = clone.getRotated();
 		this.size = clone.getSize();
 		this.sizes = clone.getSizes();
 		this.potentials = clone.getPotentials();
 	}
 	public void parseSequence(String filepath) throws Exception {
 		try {
 			FileInputStream fis = new FileInputStream(filepath);
 			InputStreamReader isr = new InputStreamReader(fis);
 			BufferedReader br = new BufferedReader(isr);
 			String[] seqraw = new String[10000];
 			int seqcount = 0;
 			while(true) {
 				String s = br.readLine();
 				if(s != null) {
 					String[] ssplit = new String[20];
 					ssplit = s.split(" ");
 					if (ssplit[0].equals("SEQRES")) {
 						seqraw[seqcount] = s;
 						seqcount++;
 					}
 				} else {
 					break;
 				}
 			}
 			chaincount = -1;
 			for (int i = 0; i < 10; i++) {
 				parsedsequence[i] = "";
 			}
 			char currentchain = ' ';
 			char newchain = ' ';
 			for (int i = 0; i < seqcount; i++) {
 				if(currentchain != seqraw[i].charAt(11)) {
 					chaincount = chaincount + 1;
 					newchain = seqraw[i].charAt(11);
 					chaintranslator.put(newchain,chaincount);
 					reversechaintrans.put(chaincount,newchain);
 					currentchain = newchain; 
 					for (int j = 19; j <= 67; j=j+4) {
 						if (translator2.get(seqraw[i].substring(j,j+3)) != null) {
 							parsedsequence[chaincount] = parsedsequence[chaincount] + translator2.get(seqraw[i].substring(j,j+3));
 						}
 					}
 				} else {
 					for (int j = 19; j <= 67; j=j+4) {
 						if (translator2.get(seqraw[i].substring(j,j+3)) != null) {
 							parsedsequence[chaincount] = parsedsequence[chaincount] + translator2.get(seqraw[i].substring(j,j+3));
 						}
 					}
 				}
 			}
 		} catch (Exception ex) {
 			ex.printStackTrace();
 			throw ex;
 		}
 	}
 	public void parseStructure(String filepath) throws Exception {
 		try {
 			FileInputStream fis = new FileInputStream(filepath);
 			InputStreamReader isr = new InputStreamReader(fis);
 			BufferedReader br = new BufferedReader(isr);
 			String[] structraw = new String[10000];
 			int structcount = 0;
 			while(true) {
 				String s = br.readLine();
 				if(s != null) {
 					String[] ssplit = new String[20];
 					ssplit = s.split(" ");
 					if (ssplit[0].equals("ATOM")) {
 						structraw[structcount] = s;
 						structcount++;
 					}
 				} else {
 					break;
 				}
 			}
 			double xcoordsum = 0;
 			double ycoordsum = 0;
 			double zcoordsum = 0;
 			for (int i = 0; i < structcount; i++) {
 				String catom = structraw[i];
 				double xcoord = Double.parseDouble(removeSpace(catom.substring(30,38)));
 				double ycoord = Double.parseDouble(removeSpace(catom.substring(38,46)));
 				double zcoord = Double.parseDouble(removeSpace(catom.substring(46,54)));
 				char element = ' ';
 				if (catom.length() > 77) {
 					element = catom.charAt(77);
 				}				
 				int resnum = Integer.parseInt(removeSpace(catom.substring(22,26)));
 				int atomnum = Integer.parseInt(removeSpace(catom.substring(6,11)));
 				int chainnum = Integer.parseInt(chaintranslator.get(catom.charAt(21)).toString());
 				String eType = removeSpace(catom.substring(12,16));
 				if (element == ' ') {
 					element = eType.charAt(0);
 				}
 				Atom next = new Atom(xcoord, ycoord, zcoord, element, resnum, atomnum, chainnum, eType);
 				if (Double.isNaN(xcoord) || Double.isNaN(ycoord) || Double.isNaN(zcoord)) {
 					System.err.println("NaN found");
 					next.printAtomErr();
 				}
 				structure.add(next);
 				xcoordsum += xcoord;
 				ycoordsum += ycoord;
 				zcoordsum += zcoord;
 			}
 			xcoordcent = xcoordsum/structcount;
 			ycoordcent = ycoordsum/structcount;
 			zcoordcent = zcoordsum/structcount;
 		} catch (Exception ex) {
 			ex.printStackTrace();
 			throw ex;
 		}
 	}
 	public void determineSurface() {
 		for (int i = 0; i < (int)(2*Math.PI/Constants.THETAINC); i++) {
 			for (int j = 0; j < (int)(Math.PI/Constants.PHIINC); j++) {
 				sizes[i][j] = Double.POSITIVE_INFINITY;
 			}
 		}
 		ArrayList newstructure = new ArrayList<Atom>();
 		for (int i = 0; i < structure.size(); i++) {
 			Atom current = (Atom)structure.get(i);
 			Atom trcurrent = current.transAtom(-xcoordcent, -ycoordcent, -zcoordcent);
 			newstructure.add(trcurrent);
 		}
 		ArrayList structurenew = new ArrayList<Atom>();
 		for (int i = 0; i < newstructure.size(); i++) {
 			Atom current = (Atom)newstructure.get(i);
 			current.setSpherical();
 			structurenew.add(current);
 		}
 		double maxnumoverall = 0;
 		for (double i = Constants.THETAINC/2; i < 2*Math.PI; i += Constants.THETAINC) {
 			for (double j = Constants.PHIINC/2; j < Math.PI; j += Constants.PHIINC) {
 				double maxnum = 0;
 				for (int k = 0; k < structurenew.size(); k++) {
 					Atom current = (Atom)structurenew.get(k);
 					if (Math.abs(current.getYcoord() - i) <= Constants.THETAINC/2 && Math.abs(current.getZcoord() - j) <= Constants.PHIINC/2 && current.getXcoord() >= maxnum) {
 						maxnum = current.getXcoord();
 					}
 					if (current.getXcoord() >= maxnumoverall) {
 						maxnumoverall = current.getXcoord();
 					}
 				}
 				sizes[(int)((i-Constants.THETAINC/2)*(1/Constants.THETAINC))][(int)((j-Constants.PHIINC/2)*(1/Constants.PHIINC))] = maxnum;
 				for (int k = 0; k < structurenew.size(); k++) {
 					Atom current = (Atom)structurenew.get(k);
 					if (Math.abs(current.getYcoord() - i) <= Constants.THETAINC/2 && Math.abs(current.getZcoord() - j) <= Constants.PHIINC/2 && maxnum - current.getXcoord() <= 2*Constants.SURFACESIZE) {
 						surface.add(this.structure.get(k));
 					}
 				}
 			}
 		}
 		size = maxnumoverall;
 	}
 	public void detBondsBackbone() {
 		Atom first = (Atom)structure.get(0);
 		ArrayList<Atom> AminoAcid = new ArrayList<Atom>();
 		int resnum = first.getResnum();
 		int counter = 0;
 		for (int i = 0; i < structure.size(); i++) {
 			Atom current = (Atom)structure.get(i);
 			if (current.getResnum() == first.getResnum()) {
 				AminoAcid.add(current);
 			} else {
 				counter = i;
 				break;
 			}
 		}
 		Atom Catom = null;
 		Atom CAatom = null;
 		Atom Natom = null;
 		Atom Oatom = null;
 		Atom CatomLast = null;
 		Bond newb1 = null;
 		Bond newb2 = null;
 		Bond newb3 = null;
 		char restype = ' ';
 		for (int i = 0; i < AminoAcid.size(); i++) {
 			Atom current = (Atom)AminoAcid.get(i);
 			if (current.getEtype() == "C") {
 				Catom = current;
 			} else if (current.getEtype() == "CA") {
 				CAatom = current;
 			} else if (current.getEtype() == "N") {
 				Natom = current;
 			} else if (current.getEtype() == "O") {
 				Oatom = current;
 			}
 		}
 		if (Catom != null && CAatom != null && Natom != null && Oatom != null) {
 			backbone.add(Catom);
 			backbone.add(CAatom);
 			backbone.add(Natom);
 			newb1 = new Bond(Natom, CAatom, Constants.BONDCONST);
 			newb2 = new Bond(CAatom, Catom, Constants.BONDCONST);
 			newb3 = new Bond(Catom, Oatom, Constants.BONDCONST);
 			bonds.add(newb1);
 			bonds.add(newb2);
 			bonds.add(newb3);
 			backbonebonds.add(newb1);
 			backbonebonds.add(newb2);
 			CatomLast = Catom;
 			restype = parsedsequence[CAatom.getChainnum()].charAt(resnum - 1);
 			AminoAcid.remove(Catom);
 			AminoAcid.remove(CAatom);
 			AminoAcid.remove(Natom);
 			AminoAcid.remove(Oatom);
 			if (restype == 'A') {
 				Atom CBatom = AminoAcid.get(0);
 				Bond newb4 = new Bond(CBatom, CAatom, Constants.BONDCONST); 
 				bonds.add(newb4);
 			} else if (restype == 'C') {
 				Atom CBatom = null;
 				Atom SGatom = null;
 				for (int i = 0; i < AminoAcid.size(); i++) {
 					Atom current = (Atom)AminoAcid.get(i);
 					if (current.getEtype() == "CB") {
 						CBatom = current;
 					} else {
 						SGatom = current;
 					}
 				}
 				Bond newb4 = new Bond(CBatom, CAatom, Constants.BONDCONST);
 				Bond newb5 = new Bond(CBatom, SGatom, Constants.BONDCONST);
 				bonds.add(newb4);
 				bonds.add(newb5);
 			} else if (restype == 'D') {
 				Atom CBatom = null;
 				Atom CGatom = null;
 				Atom OD1atom = null;
 				Atom OD2atom = null;
 				for (int i = 0; i < AminoAcid.size(); i++) {
 					Atom current = (Atom)AminoAcid.get(i);
 					if (current.getEtype() == "CB") {
 						CBatom = current;
 					} else if (current.getEtype() == "CG") {
 						CGatom = current;
 					} else if (current.getEtype() == "OD1") {
 						OD1atom = current;
 					} else {
 						OD2atom = current;
 					}
 				}
 				Bond newb7 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 				Bond newb4 = new Bond(CBatom, CGatom, Constants.BONDCONST);
 				Bond newb5 = new Bond(CGatom, OD1atom, Constants.BONDCONST);
 				Bond newb6 = new Bond(CGatom, OD2atom, Constants.BONDCONST);
 				bonds.add(newb4);
 				bonds.add(newb5);
 				bonds.add(newb6);
 				bonds.add(newb7);
 			} else if (restype == 'E') {
 				Atom CBatom = null;
 				Atom CGatom = null;
 				Atom CDatom = null;
 				Atom OE1atom = null;
 				Atom OE2atom = null;
 				for (int i = 0; i < AminoAcid.size(); i++) {
 					Atom current = (Atom)AminoAcid.get(i);
 					if (current.getEtype() == "CB") {
 						CBatom = current;
 					} else if (current.getEtype() == "CG") {
 						CGatom = current;
 					} else if (current.getEtype() == "CD") {
 						CDatom = current;
 					} else if (current.getEtype() == "OE1") {
 						OE1atom = current;
 					} else {
 						OE2atom = current;
 					}
 				}
 				Bond newb4 = new Bond(CBatom, CGatom, Constants.BONDCONST);
 				Bond newb5 = new Bond(CDatom, OE1atom, Constants.BONDCONST);
 				Bond newb6 = new Bond(CDatom, OE2atom, Constants.BONDCONST);
 				Bond newb7 = new Bond(CGatom, CDatom, Constants.BONDCONST);
 				Bond newb8 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 				bonds.add(newb4);
 				bonds.add(newb5);
 				bonds.add(newb6);
 				bonds.add(newb7);
 				bonds.add(newb8);
 			} else if (restype == 'F') {
 				Atom CBatom = null;
 				Atom CGatom = null;
 				Atom CD1atom = null;
 				Atom CD2atom = null;
 				Atom CE1atom = null;
 				Atom CE2atom = null;
 				Atom CZatom = null;
 				for (int i = 0; i < AminoAcid.size(); i++) {
 					Atom current = (Atom)AminoAcid.get(i);
 					if (current.getEtype() == "CB") {
 						CBatom = current;
 					} else if (current.getEtype() == "CG") {
 						CGatom = current;
 					} else if (current.getEtype() == "CD1") {
 						CD1atom = current;
 					} else if (current.getEtype() == "CD2") {
 						CD2atom = current;
 					} else if (current.getEtype() == "CE1") {
 						CE1atom = current;
 					} else if (current.getEtype() == "CE2") {
 						CE2atom = current;
 					} else {
 						CZatom = current;
 					}
 				}
 				Bond newb4 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 				Bond newb5 = new Bond(CBatom, CGatom, Constants.BONDCONST);
 				Bond newb6 = new Bond(CGatom, CD1atom, Constants.BONDCONST);
 				Bond newb7 = new Bond(CD1atom, CD2atom, Constants.BONDCONST);
 				Bond newb8 = new Bond(CD2atom, CZatom, Constants.BONDCONST);
 				Bond newb9 = new Bond(CZatom, CE1atom, Constants.BONDCONST);
 				Bond newb10 = new Bond(CE1atom, CE2atom, Constants.BONDCONST);
 				Bond newb11 = new Bond(CE2atom, CGatom, Constants.BONDCONST);
 				bonds.add(newb4);
 				bonds.add(newb5);
 				bonds.add(newb6);
 				bonds.add(newb7);
 				bonds.add(newb8);
 				bonds.add(newb9);
 				bonds.add(newb10);
 				bonds.add(newb11);	
 			} else if (restype == 'G') {
 				//This is intentionally left blank.
 			} else if (restype == 'H') {
 				Atom CBatom = null;
 				Atom CGatom = null;
 				Atom ND1atom = null;
 				Atom CD2atom = null;
 				Atom CE1atom = null;
 				Atom NE2atom = null;
 				for (int i = 0; i < AminoAcid.size(); i++) {
 					Atom current = (Atom)AminoAcid.get(i);
 					if (current.getEtype() == "CB") {
 						CBatom = current;
 					} else if (current.getEtype() == "CG") {
 						CGatom = current;
 					} else if (current.getEtype() == "ND1") {
 						ND1atom = current;
 					} else if (current.getEtype() == "CD2") {
 						CD2atom = current;
 					} else if (current.getEtype() == "CE1") {
 						CE1atom = current;
 					} else {
 						NE2atom = current;
 					}
 				}
 				Bond newb4 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 				Bond newb5 = new Bond(CBatom, CGatom, Constants.BONDCONST);
 				Bond newb6 = new Bond(CGatom, ND1atom, Constants.BONDCONST);
 				Bond newb7 = new Bond(ND1atom, CD2atom, Constants.BONDCONST);
 				Bond newb10 = new Bond(CE1atom, NE2atom, Constants.BONDCONST);
 				Bond newb11 = new Bond(NE2atom, CGatom, Constants.BONDCONST);
 				bonds.add(newb4);
 				bonds.add(newb5);
 				bonds.add(newb6);
 				bonds.add(newb7);
 				bonds.add(newb10);
 				bonds.add(newb11);	
 			} else if (restype == 'I') {
 				Atom CBatom = null;
 				Atom CG1atom = null;
 				Atom CG2atom = null;
 				Atom CD1atom = null;
 				for (int i = 0; i < AminoAcid.size(); i++) {
 					Atom current = (Atom)AminoAcid.get(i);
 					if (current.getEtype() == "CB") {
 						CBatom = current;
 					} else if (current.getEtype() == "CG1") {
 						CG1atom = current;
 					} else if (current.getEtype() == "CG2") {
 						CG2atom = current;
 					} else {
 						CD1atom = current;
 					}
 				}
 				Bond newb4 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 				Bond newb5 = new Bond(CBatom, CG1atom, Constants.BONDCONST);
 				Bond newb6 = new Bond(CG1atom, CG2atom, Constants.BONDCONST);
 				Bond newb7 = new Bond(CBatom, CD1atom, Constants.BONDCONST);
 				bonds.add(newb4);
 				bonds.add(newb5);
 				bonds.add(newb6);
 				bonds.add(newb7);
 			} else if (restype == 'K') {
 				Atom CBatom = null;
 				Atom CGatom = null;
 				Atom CDatom = null;
 				Atom CEatom = null;
 				Atom NZatom = null;
 				for (int i = 0; i < AminoAcid.size(); i++) {
 					Atom current = (Atom)AminoAcid.get(i);
 					if (current.getEtype() == "CB") {
 						CBatom = current;
 					} else if (current.getEtype() == "CG") {
 						CGatom = current;
 					} else if (current.getEtype() == "CD") {
 						CDatom = current;
 					} else if (current.getEtype() == "CE") {
 						CEatom = current;
 					} else {
 						NZatom = current;
 					}
 				}
 				Bond newb4 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 				Bond newb5 = new Bond(CBatom, CGatom, Constants.BONDCONST);
 				Bond newb6 = new Bond(CGatom, CDatom, Constants.BONDCONST);
 				Bond newb7 = new Bond(CDatom, CEatom, Constants.BONDCONST);
 				Bond newb8 = new Bond(CEatom, NZatom, Constants.BONDCONST);
 				bonds.add(newb4);
 				bonds.add(newb5);
 				bonds.add(newb6);
 				bonds.add(newb7);
 				bonds.add(newb8);
 			} else if (restype == 'L') {
 				Atom CBatom = null;
 				Atom CG1atom = null;
 				Atom CD2atom = null;
 				Atom CD1atom = null;
 				for (int i = 0; i < AminoAcid.size(); i++) {
 					Atom current = (Atom)AminoAcid.get(i);
 					if (current.getEtype() == "CB") {
 						CBatom = current;
 					} else if (current.getEtype() == "CG1") {
 						CG1atom = current;
 					} else if (current.getEtype() == "CD2") {
 						CD2atom = current;
 					} else {
 						CD1atom = current;
 					}
 				}
 				Bond newb4 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 				Bond newb5 = new Bond(CBatom, CG1atom, Constants.BONDCONST);
 				Bond newb6 = new Bond(CG1atom, CD2atom, Constants.BONDCONST);
 				Bond newb7 = new Bond(CG1atom, CD1atom, Constants.BONDCONST);
 				bonds.add(newb4);
 				bonds.add(newb5);
 				bonds.add(newb6);
 				bonds.add(newb7);
 			} else if (restype == 'M') {
 				Atom CBatom = null;
 				Atom CGatom = null;
 				Atom SDatom = null;
 				Atom CEatom = null;
 				for (int i = 0; i < AminoAcid.size(); i++) {
 					Atom current = (Atom)AminoAcid.get(i);
 					if (current.getEtype() == "CB") {
 						CBatom = current;
 					} else if (current.getEtype() == "CG") {
 						CGatom = current;
 					} else if (current.getEtype() == "SD") {
 						SDatom = current;
 					} else {
 						CEatom = current;
 					}
 				}
 				Bond newb4 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 				Bond newb5 = new Bond(CBatom, CGatom, Constants.BONDCONST);
 				Bond newb6 = new Bond(CGatom, SDatom, Constants.BONDCONST);
 				Bond newb7 = new Bond(SDatom, CEatom, Constants.BONDCONST);
 				bonds.add(newb4);
 				bonds.add(newb5);
 				bonds.add(newb6);
 				bonds.add(newb7);
 			} else if (restype == 'N') {
 				Atom CBatom = null;
 				Atom CGatom = null;
 				Atom OD1atom = null;
 				Atom ND2atom = null;
 				for (int i = 0; i < AminoAcid.size(); i++) {
 					Atom current = (Atom)AminoAcid.get(i);
 					if (current.getEtype() == "CB") {
 						CBatom = current;
 					} else if (current.getEtype() == "CG") {
 						CGatom = current;
 					} else if (current.getEtype() == "OD1") {
 						OD1atom = current;
 					} else {
 						ND2atom = current;
 					}
 				}
 				Bond newb7 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 				Bond newb4 = new Bond(CBatom, CGatom, Constants.BONDCONST);
 				Bond newb5 = new Bond(CGatom, OD1atom, Constants.BONDCONST);
 				Bond newb6 = new Bond(CGatom, ND2atom, Constants.BONDCONST);
 				bonds.add(newb4);
 				bonds.add(newb5);
 				bonds.add(newb6);
 				bonds.add(newb7);
 			} else if (restype == 'P') {
 				Atom CBatom = null;
 				Atom CGatom = null;
 				Atom CDatom = null;
 				for (int i = 0; i < AminoAcid.size(); i++) {
 					Atom current = (Atom)AminoAcid.get(i);
 					if (current.getEtype() == "CB") {
 						CBatom = current;
 					} else if (current.getEtype() == "CG") {
 						CGatom = current;
 					} else {
 						CDatom = current;
 					} 
 				}
 				Bond newb4 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 				Bond newb5 = new Bond(CBatom, CGatom, Constants.BONDCONST);
 				Bond newb6 = new Bond(CGatom, CDatom, Constants.BONDCONST);
 				Bond newb7 = new Bond(CDatom, Natom, Constants.BONDCONST);
 				bonds.add(newb4);
 				bonds.add(newb5);
 				bonds.add(newb6);
 				bonds.add(newb7);
 			} else if (restype == 'Q') {
 				Atom CBatom = null;
 				Atom CGatom = null;
 				Atom CDatom = null;
 				Atom OE1atom = null;
 				Atom NE2atom = null;
 				for (int i = 0; i < AminoAcid.size(); i++) {
 					Atom current = (Atom)AminoAcid.get(i);
 					if (current.getEtype() == "CB") {
 						CBatom = current;
 					} else if (current.getEtype() == "CG") {
 						CGatom = current;
 					} else if (current.getEtype() == "CD") {
 						CDatom = current;
 					} else if (current.getEtype() == "OE1") {
 						OE1atom = current;
 					} else {
 						NE2atom = current;
 					}
 				}
 				Bond newb4 = new Bond(CBatom, CGatom, Constants.BONDCONST);
 				Bond newb5 = new Bond(CDatom, OE1atom, Constants.BONDCONST);
 				Bond newb6 = new Bond(CDatom, NE2atom, Constants.BONDCONST);
 				Bond newb7 = new Bond(CGatom, CDatom, Constants.BONDCONST);
 				Bond newb8 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 				bonds.add(newb4);
 				bonds.add(newb5);
 				bonds.add(newb6);
 				bonds.add(newb7);
 				bonds.add(newb8);
 			} else if (restype == 'R') {
 				Atom CBatom = null;
 				Atom CGatom = null;
 				Atom CDatom = null;
 				Atom NEatom = null;
 				Atom CZatom = null;
 				Atom NH1atom = null;
 				Atom NH2atom = null;
 				for (int i = 0; i < AminoAcid.size(); i++) {
 					Atom current = (Atom)AminoAcid.get(i);
 					if (current.getEtype() == "CB") {
 						CBatom = current;
 					} else if (current.getEtype() == "CG") {
 						CGatom = current;
 					} else if (current.getEtype() == "CD") {
 						CDatom = current;
 					} else if (current.getEtype() == "NE") {
 						NEatom = current;
 					} else if (current.getEtype() == "CZ") {
 						CZatom = current;
 					} else if (current.getEtype() == "NH1") {
 						NH1atom = current;
 					} else {
 						NH2atom = current;
 					}
 				}
 				Bond newb4 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 				Bond newb5 = new Bond(CBatom, CGatom, Constants.BONDCONST);
 				Bond newb6 = new Bond(CGatom, CDatom, Constants.BONDCONST);
 				Bond newb7 = new Bond(CDatom, NEatom, Constants.BONDCONST);
 				Bond newb8 = new Bond(NEatom, CZatom, Constants.BONDCONST);
 				Bond newb9 = new Bond(CZatom, NH1atom, Constants.BONDCONST);
 				Bond newb10 = new Bond(CZatom, NH2atom, Constants.BONDCONST);
 				bonds.add(newb4);
 				bonds.add(newb5);
 				bonds.add(newb6);
 				bonds.add(newb7);
 				bonds.add(newb8);
 				bonds.add(newb9);
 				bonds.add(newb10);
 			} else if (restype == 'S') {
 				Atom CBatom = null;
 				Atom OGatom = null;
 				for (int i = 0; i < AminoAcid.size(); i++) {
 					Atom current = (Atom)AminoAcid.get(i);
 					if (current.getEtype() == "CB") {
 						CBatom = current;
 					} else {
 						OGatom = current;
 					}
 				}
 				Bond newb4 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 				Bond newb5 = new Bond(CBatom, OGatom, Constants.BONDCONST);
 				bonds.add(newb4);
 				bonds.add(newb5);
 			} else if (restype == 'T') {
 				Atom CBatom = null;
 				Atom CG1atom = null;
 				Atom OG2atom = null;
 				for (int i = 0; i < AminoAcid.size(); i++) {
 					Atom current = (Atom)AminoAcid.get(i);
 					if (current.getEtype() == "CB") {
 						CBatom = current;
 					} else if (current.getEtype() == "CG1") {
 						CG1atom = current;
 					} else {
 						OG2atom = current;
 					}
 				}
 				Bond newb4 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 				Bond newb5 = new Bond(CBatom, CG1atom, Constants.BONDCONST);
 				Bond newb6 = new Bond(CBatom, OG2atom, Constants.BONDCONST);
 				bonds.add(newb4);
 				bonds.add(newb5);
 				bonds.add(newb6);
 			} else if (restype == 'V') {
 				Atom CBatom = null;
 				Atom CG1atom = null;
 				Atom CG2atom = null;
 				for (int i = 0; i < AminoAcid.size(); i++) {
 					Atom current = (Atom)AminoAcid.get(i);
 					if (current.getEtype() == "CB") {
 						CBatom = current;
 					} else if (current.getEtype() == "CG1") {
 						CG1atom = current;
 					} else {
 						CG2atom = current;
 					}
 				}
 				Bond newb4 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 				Bond newb5 = new Bond(CBatom, CG1atom, Constants.BONDCONST);
 				Bond newb6 = new Bond(CBatom, CG2atom, Constants.BONDCONST);
 				bonds.add(newb4);
 				bonds.add(newb5);
 				bonds.add(newb6);
 			} else if (restype == 'W') {
 				Atom CBatom = null;
 				Atom CGatom = null;
 				Atom CD1atom = null;
 				Atom CD2atom = null;
 				Atom NE1atom = null;
 				Atom CE2atom = null;
 				Atom CE3atom = null;
 				Atom CZ2atom = null;
 				Atom CZ3atom = null;
 				Atom CH2atom = null;
 				for (int i = 0; i < AminoAcid.size(); i++) {
 					Atom current = (Atom)AminoAcid.get(i);
 					if (current.getEtype() == "CB") {
 						CBatom = current;
 					} else if (current.getEtype() == "CG") {
 						CGatom = current;
 					} else if (current.getEtype() == "CD1") {
 						CD1atom = current;
 					} else if (current.getEtype() == "CD2") {
 						CD2atom = current;
 					} else if (current.getEtype() == "NE1") {
 						NE1atom = current;
 					} else if (current.getEtype() == "CE2") {
 						CE2atom = current;
 					} else if (current.getEtype() == "CZ2") {
 						CZ2atom = current;
 					} else if (current.getEtype() == "CZ3") {
 						CZ3atom = current;
 					} else {
 						CH2atom = current;
 					}
 				}
 				Bond newb4 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 				Bond newb5 = new Bond(CBatom, CGatom, Constants.BONDCONST);
 				Bond newb6 = new Bond(CGatom, CD1atom, Constants.BONDCONST);
 				Bond newb7 = new Bond(CGatom, CD2atom, Constants.BONDCONST);
 				Bond newb8 = new Bond(CD1atom, NE1atom, Constants.BONDCONST);
 				Bond newb9 = new Bond(CD2atom, CE2atom, Constants.BONDCONST);
 				Bond newb10 = new Bond(CD2atom, CE3atom, Constants.BONDCONST);
 				Bond newb11 = new Bond(CE2atom, CZ2atom, Constants.BONDCONST);
 				Bond newb12 = new Bond(CE3atom, CZ3atom, Constants.BONDCONST);
 				Bond newb13 = new Bond(CZ2atom, CH2atom, Constants.BONDCONST);
 				Bond newb14 = new Bond(CZ3atom, CH2atom, Constants.BONDCONST);
 				bonds.add(newb4);
 				bonds.add(newb5);
 				bonds.add(newb6);
 				bonds.add(newb7);
 				bonds.add(newb8);
 				bonds.add(newb9);
 				bonds.add(newb10);
 				bonds.add(newb11);	
 				bonds.add(newb12);
 				bonds.add(newb13);
 				bonds.add(newb14);
 			} else if (restype == 'Y') {
 				Atom CBatom = null;
 				Atom CGatom = null;
 				Atom CD1atom = null;
 				Atom CD2atom = null;
 				Atom CE1atom = null;
 				Atom CE2atom = null;
 				Atom CZatom = null;
 				Atom OHatom = null;
 				for (int i = 0; i < AminoAcid.size(); i++) {
 					Atom current = (Atom)AminoAcid.get(i);
 					if (current.getEtype() == "CB") {
 						CBatom = current;
 					} else if (current.getEtype() == "CG") {
 						CGatom = current;
 					} else if (current.getEtype() == "CD1") {
 						CD1atom = current;
 					} else if (current.getEtype() == "CD2") {
 						CD2atom = current;
 					} else if (current.getEtype() == "CE1") {
 						CE1atom = current;
 					} else if (current.getEtype() == "CE2") {
 						CE2atom = current;
 					} else if (current.getEtype() == "CZ") {
 						CZatom = current;
 					} else {
 						OHatom = current;
 					}
 				}
 				Bond newb4 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 				Bond newb5 = new Bond(CBatom, CGatom, Constants.BONDCONST);
 				Bond newb6 = new Bond(CGatom, CD1atom, Constants.BONDCONST);
 				Bond newb7 = new Bond(CD1atom, CD2atom, Constants.BONDCONST);
 				Bond newb8 = new Bond(CD2atom, CZatom, Constants.BONDCONST);
 				Bond newb9 = new Bond(CZatom, CE1atom, Constants.BONDCONST);
 				Bond newb10 = new Bond(CE1atom, CE2atom, Constants.BONDCONST);
 				Bond newb11 = new Bond(CE2atom, CGatom, Constants.BONDCONST);
 				Bond newb12 = new Bond(CZatom, OHatom, Constants.BONDCONST);
 				bonds.add(newb4);
 				bonds.add(newb5);
 				bonds.add(newb6);
 				bonds.add(newb7);
 				bonds.add(newb8);
 				bonds.add(newb9);
 				bonds.add(newb10);
 				bonds.add(newb11);	
 				bonds.add(newb12);
 			} else {
 				System.err.println("UNKNOWN RES TYPE " + restype);
 			}
 		}
 		while (true) {
 			first = (Atom)structure.get(counter);
 			resnum = first.getResnum();
 			if (counter >= structure.size() - 1) {
 				break;
 			}
 			for (int i = counter; i < structure.size(); i++) {
 				Atom current = (Atom)structure.get(i);
 				if (current.getResnum() == first.getResnum()) {
 					AminoAcid.add(current);
 					counter = i;
 				} else {
 					counter = i;
 					break;
 				}
 			}
 			Catom = null;
 			CAatom = null;
 			Natom = null;
 			Oatom = null;
 			for (int i = 0; i < AminoAcid.size(); i++) {
 				Atom current = (Atom)AminoAcid.get(i);
 				if (current.getEtype() == "C") {
 					Catom = current;
 				} else if (current.getEtype() == "CA") {
 					CAatom = current;
 				} else if (current.getEtype() == "N") {
 					Natom = current;
 				} else if (current.getEtype() == "O") {
 					Oatom = current;
 				}
 			}
 			if (Catom != null && CAatom != null && Natom != null && Oatom != null && CatomLast != null) {
 				backbone.add(Catom);
 				backbone.add(CAatom);
 				backbone.add(Natom);
 				Bond newb0 = new Bond(CatomLast, Natom, Constants.BONDCONST);
 				newb1 = new Bond(Natom, CAatom, Constants.BONDCONST);
 				newb2 = new Bond(CAatom, Catom, Constants.BONDCONST);
 				newb3 = new Bond(Catom, Oatom, Constants.BONDCONST);
 				bonds.add(newb0);
 				bonds.add(newb1);
 				bonds.add(newb2);
 				bonds.add(newb3);
 				backbonebonds.add(newb0);
 				backbonebonds.add(newb1);
 				backbonebonds.add(newb2);
 				CatomLast = Catom;
 				restype = parsedsequence[CAatom.getChainnum()].charAt(resnum - 1);
 				AminoAcid.remove(Catom);
 				AminoAcid.remove(CAatom);
 				AminoAcid.remove(Natom);
 				AminoAcid.remove(Oatom);
 				if (restype == 'A') {
 					Atom CBatom = AminoAcid.get(0);
 					Bond newb4 = new Bond(CBatom, CAatom, Constants.BONDCONST); 
 					bonds.add(newb4);
 				} else if (restype == 'C') {
 					Atom CBatom = null;
 					Atom SGatom = null;
 					for (int i = 0; i < AminoAcid.size(); i++) {
 						Atom current = (Atom)AminoAcid.get(i);
 						if (current.getEtype() == "CB") {
 							CBatom = current;
 						} else {
 							SGatom = current;
 						}
 					}
 					Bond newb4 = new Bond(CBatom, CAatom, Constants.BONDCONST);
 					Bond newb5 = new Bond(CBatom, SGatom, Constants.BONDCONST);
 					bonds.add(newb4);
 					bonds.add(newb5);
 				} else if (restype == 'D') {
 					Atom CBatom = null;
 					Atom CGatom = null;
 					Atom OD1atom = null;
 					Atom OD2atom = null;
 					for (int i = 0; i < AminoAcid.size(); i++) {
 						Atom current = (Atom)AminoAcid.get(i);
 						if (current.getEtype() == "CB") {
 							CBatom = current;
 						} else if (current.getEtype() == "CG") {
 							CGatom = current;
 						} else if (current.getEtype() == "OD1") {
 							OD1atom = current;
 						} else {
 							OD2atom = current;
 						}
 					}
 					Bond newb7 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 					Bond newb4 = new Bond(CBatom, CGatom, Constants.BONDCONST);
 					Bond newb5 = new Bond(CGatom, OD1atom, Constants.BONDCONST);
 					Bond newb6 = new Bond(CGatom, OD2atom, Constants.BONDCONST);
 					bonds.add(newb4);
 					bonds.add(newb5);
 					bonds.add(newb6);
 					bonds.add(newb7);
 				} else if (restype == 'E') {
 					Atom CBatom = null;
 					Atom CGatom = null;
 					Atom CDatom = null;
 					Atom OE1atom = null;
 					Atom OE2atom = null;
 					for (int i = 0; i < AminoAcid.size(); i++) {
 						Atom current = (Atom)AminoAcid.get(i);
 						if (current.getEtype() == "CB") {
 							CBatom = current;
 						} else if (current.getEtype() == "CG") {
 							CGatom = current;
 						} else if (current.getEtype() == "CD") {
 							CDatom = current;
 						} else if (current.getEtype() == "OE1") {
 							OE1atom = current;
 						} else {
 							OE2atom = current;
 						}
 					}
 					Bond newb4 = new Bond(CBatom, CGatom, Constants.BONDCONST);
 					Bond newb5 = new Bond(CDatom, OE1atom, Constants.BONDCONST);
 					Bond newb6 = new Bond(CDatom, OE2atom, Constants.BONDCONST);
 					Bond newb7 = new Bond(CGatom, CDatom, Constants.BONDCONST);
 					Bond newb8 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 					bonds.add(newb4);
 					bonds.add(newb5);
 					bonds.add(newb6);
 					bonds.add(newb7);
 					bonds.add(newb8);
 				} else if (restype == 'F') {
 					Atom CBatom = null;
 					Atom CGatom = null;
 					Atom CD1atom = null;
 					Atom CD2atom = null;
 					Atom CE1atom = null;
 					Atom CE2atom = null;
 					Atom CZatom = null;
 					for (int i = 0; i < AminoAcid.size(); i++) {
 						Atom current = (Atom)AminoAcid.get(i);
 						if (current.getEtype() == "CB") {
 							CBatom = current;
 						} else if (current.getEtype() == "CG") {
 							CGatom = current;
 						} else if (current.getEtype() == "CD1") {
 							CD1atom = current;
 						} else if (current.getEtype() == "CD2") {
 							CD2atom = current;
 						} else if (current.getEtype() == "CE1") {
 							CE1atom = current;
 						} else if (current.getEtype() == "CE2") {
 							CE2atom = current;
 						} else {
 							CZatom = current;
 						}
 					}
 					Bond newb4 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 					Bond newb5 = new Bond(CBatom, CGatom, Constants.BONDCONST);
 					Bond newb6 = new Bond(CGatom, CD1atom, Constants.BONDCONST);
 					Bond newb7 = new Bond(CD1atom, CD2atom, Constants.BONDCONST);
 					Bond newb8 = new Bond(CD2atom, CZatom, Constants.BONDCONST);
 					Bond newb9 = new Bond(CZatom, CE1atom, Constants.BONDCONST);
 					Bond newb10 = new Bond(CE1atom, CE2atom, Constants.BONDCONST);
 					Bond newb11 = new Bond(CE2atom, CGatom, Constants.BONDCONST);
 					bonds.add(newb4);
 					bonds.add(newb5);
 					bonds.add(newb6);
 					bonds.add(newb7);
 					bonds.add(newb8);
 					bonds.add(newb9);
 					bonds.add(newb10);
 					bonds.add(newb11);	
 				} else if (restype == 'G') {
 					//This is intentionally left blank.
 				} else if (restype == 'H') {
 					Atom CBatom = null;
 					Atom CGatom = null;
 					Atom ND1atom = null;
 					Atom CD2atom = null;
 					Atom CE1atom = null;
 					Atom NE2atom = null;
 					for (int i = 0; i < AminoAcid.size(); i++) {
 						Atom current = (Atom)AminoAcid.get(i);
 						if (current.getEtype() == "CB") {
 							CBatom = current;
 						} else if (current.getEtype() == "CG") {
 							CGatom = current;
 						} else if (current.getEtype() == "ND1") {
 							ND1atom = current;
 						} else if (current.getEtype() == "CD2") {
 							CD2atom = current;
 						} else if (current.getEtype() == "CE1") {
 							CE1atom = current;
 						} else {
 							NE2atom = current;
 						}
 					}
 					Bond newb4 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 					Bond newb5 = new Bond(CBatom, CGatom, Constants.BONDCONST);
 					Bond newb6 = new Bond(CGatom, ND1atom, Constants.BONDCONST);
 					Bond newb7 = new Bond(ND1atom, CD2atom, Constants.BONDCONST);
 					Bond newb10 = new Bond(CE1atom, NE2atom, Constants.BONDCONST);
 					Bond newb11 = new Bond(NE2atom, CGatom, Constants.BONDCONST);
 					bonds.add(newb4);
 					bonds.add(newb5);
 					bonds.add(newb6);
 					bonds.add(newb7);
 					bonds.add(newb10);
 					bonds.add(newb11);	
 				} else if (restype == 'I') {
 					Atom CBatom = null;
 					Atom CG1atom = null;
 					Atom CG2atom = null;
 					Atom CD1atom = null;
 					for (int i = 0; i < AminoAcid.size(); i++) {
 						Atom current = (Atom)AminoAcid.get(i);
 						if (current.getEtype() == "CB") {
 							CBatom = current;
 						} else if (current.getEtype() == "CG1") {
 							CG1atom = current;
 						} else if (current.getEtype() == "CG2") {
 							CG2atom = current;
 						} else {
 							CD1atom = current;
 						}
 					}
 					Bond newb4 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 					Bond newb5 = new Bond(CBatom, CG1atom, Constants.BONDCONST);
 					Bond newb6 = new Bond(CG1atom, CG2atom, Constants.BONDCONST);
 					Bond newb7 = new Bond(CBatom, CD1atom, Constants.BONDCONST);
 					bonds.add(newb4);
 					bonds.add(newb5);
 					bonds.add(newb6);
 					bonds.add(newb7);
 				} else if (restype == 'K') {
 					Atom CBatom = null;
 					Atom CGatom = null;
 					Atom CDatom = null;
 					Atom CEatom = null;
 					Atom NZatom = null;
 					for (int i = 0; i < AminoAcid.size(); i++) {
 						Atom current = (Atom)AminoAcid.get(i);
 						if (current.getEtype() == "CB") {
 							CBatom = current;
 						} else if (current.getEtype() == "CG") {
 							CGatom = current;
 						} else if (current.getEtype() == "CD") {
 							CDatom = current;
 						} else if (current.getEtype() == "CE") {
 							CEatom = current;
 						} else {
 							NZatom = current;
 						}
 					}
 					Bond newb4 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 					Bond newb5 = new Bond(CBatom, CGatom, Constants.BONDCONST);
 					Bond newb6 = new Bond(CGatom, CDatom, Constants.BONDCONST);
 					Bond newb7 = new Bond(CDatom, CEatom, Constants.BONDCONST);
 					Bond newb8 = new Bond(CEatom, NZatom, Constants.BONDCONST);
 					bonds.add(newb4);
 					bonds.add(newb5);
 					bonds.add(newb6);
 					bonds.add(newb7);
 					bonds.add(newb8);
 				} else if (restype == 'L') {
 					Atom CBatom = null;
 					Atom CG1atom = null;
 					Atom CD2atom = null;
 					Atom CD1atom = null;
 					for (int i = 0; i < AminoAcid.size(); i++) {
 						Atom current = (Atom)AminoAcid.get(i);
 						if (current.getEtype() == "CB") {
 							CBatom = current;
 						} else if (current.getEtype() == "CG1") {
 							CG1atom = current;
 						} else if (current.getEtype() == "CD2") {
 							CD2atom = current;
 						} else {
 							CD1atom = current;
 						}
 					}
 					Bond newb4 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 					Bond newb5 = new Bond(CBatom, CG1atom, Constants.BONDCONST);
 					Bond newb6 = new Bond(CG1atom, CD2atom, Constants.BONDCONST);
 					Bond newb7 = new Bond(CG1atom, CD1atom, Constants.BONDCONST);
 					bonds.add(newb4);
 					bonds.add(newb5);
 					bonds.add(newb6);
 					bonds.add(newb7);
 				} else if (restype == 'M') {
 					Atom CBatom = null;
 					Atom CGatom = null;
 					Atom SDatom = null;
 					Atom CEatom = null;
 					for (int i = 0; i < AminoAcid.size(); i++) {
 						Atom current = (Atom)AminoAcid.get(i);
 						if (current.getEtype() == "CB") {
 							CBatom = current;
 						} else if (current.getEtype() == "CG") {
 							CGatom = current;
 						} else if (current.getEtype() == "SD") {
 							SDatom = current;
 						} else {
 							CEatom = current;
 						}
 					}
 					Bond newb4 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 					Bond newb5 = new Bond(CBatom, CGatom, Constants.BONDCONST);
 					Bond newb6 = new Bond(CGatom, SDatom, Constants.BONDCONST);
 					Bond newb7 = new Bond(SDatom, CEatom, Constants.BONDCONST);
 					bonds.add(newb4);
 					bonds.add(newb5);
 					bonds.add(newb6);
 					bonds.add(newb7);
 				} else if (restype == 'N') {
 					Atom CBatom = null;
 					Atom CGatom = null;
 					Atom OD1atom = null;
 					Atom ND2atom = null;
 					for (int i = 0; i < AminoAcid.size(); i++) {
 						Atom current = (Atom)AminoAcid.get(i);
 						if (current.getEtype() == "CB") {
 							CBatom = current;
 						} else if (current.getEtype() == "CG") {
 							CGatom = current;
 						} else if (current.getEtype() == "OD1") {
 							OD1atom = current;
 						} else {
 							ND2atom = current;
 						}
 					}
 					Bond newb7 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 					Bond newb4 = new Bond(CBatom, CGatom, Constants.BONDCONST);
 					Bond newb5 = new Bond(CGatom, OD1atom, Constants.BONDCONST);
 					Bond newb6 = new Bond(CGatom, ND2atom, Constants.BONDCONST);
 					bonds.add(newb4);
 					bonds.add(newb5);
 					bonds.add(newb6);
 					bonds.add(newb7);
 				} else if (restype == 'P') {
 					Atom CBatom = null;
 					Atom CGatom = null;
 					Atom CDatom = null;
 					for (int i = 0; i < AminoAcid.size(); i++) {
 						Atom current = (Atom)AminoAcid.get(i);
 						if (current.getEtype() == "CB") {
 							CBatom = current;
 						} else if (current.getEtype() == "CG") {
 							CGatom = current;
 						} else {
 							CDatom = current;
 						} 
 					}
 					Bond newb4 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 					Bond newb5 = new Bond(CBatom, CGatom, Constants.BONDCONST);
 					Bond newb6 = new Bond(CGatom, CDatom, Constants.BONDCONST);
 					Bond newb7 = new Bond(CDatom, Natom, Constants.BONDCONST);
 					bonds.add(newb4);
 					bonds.add(newb5);
 					bonds.add(newb6);
 					bonds.add(newb7);
 				} else if (restype == 'Q') {
 					Atom CBatom = null;
 					Atom CGatom = null;
 					Atom CDatom = null;
 					Atom OE1atom = null;
 					Atom NE2atom = null;
 					for (int i = 0; i < AminoAcid.size(); i++) {
 						Atom current = (Atom)AminoAcid.get(i);
 						if (current.getEtype() == "CB") {
 							CBatom = current;
 						} else if (current.getEtype() == "CG") {
 							CGatom = current;
 						} else if (current.getEtype() == "CD") {
 							CDatom = current;
 						} else if (current.getEtype() == "OE1") {
 							OE1atom = current;
 						} else {
 							NE2atom = current;
 						}
 					}
 					Bond newb4 = new Bond(CBatom, CGatom, Constants.BONDCONST);
 					Bond newb5 = new Bond(CDatom, OE1atom, Constants.BONDCONST);
 					Bond newb6 = new Bond(CDatom, NE2atom, Constants.BONDCONST);
 					Bond newb7 = new Bond(CGatom, CDatom, Constants.BONDCONST);
 					Bond newb8 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 					bonds.add(newb4);
 					bonds.add(newb5);
 					bonds.add(newb6);
 					bonds.add(newb7);
 					bonds.add(newb8);
 				} else if (restype == 'R') {
 					Atom CBatom = null;
 					Atom CGatom = null;
 					Atom CDatom = null;
 					Atom NEatom = null;
 					Atom CZatom = null;
 					Atom NH1atom = null;
 					Atom NH2atom = null;
 					for (int i = 0; i < AminoAcid.size(); i++) {
 						Atom current = (Atom)AminoAcid.get(i);
 						if (current.getEtype() == "CB") {
 							CBatom = current;
 						} else if (current.getEtype() == "CG") {
 							CGatom = current;
 						} else if (current.getEtype() == "CD") {
 							CDatom = current;
 						} else if (current.getEtype() == "NE") {
 							NEatom = current;
 						} else if (current.getEtype() == "CZ") {
 							CZatom = current;
 						} else if (current.getEtype() == "NH1") {
 							NH1atom = current;
 						} else {
 							NH2atom = current;
 						}
 					}
 					Bond newb4 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 					Bond newb5 = new Bond(CBatom, CGatom, Constants.BONDCONST);
 					Bond newb6 = new Bond(CGatom, CDatom, Constants.BONDCONST);
 					Bond newb7 = new Bond(CDatom, NEatom, Constants.BONDCONST);
 					Bond newb8 = new Bond(NEatom, CZatom, Constants.BONDCONST);
 					Bond newb9 = new Bond(CZatom, NH1atom, Constants.BONDCONST);
 					Bond newb10 = new Bond(CZatom, NH2atom, Constants.BONDCONST);
 					bonds.add(newb4);
 					bonds.add(newb5);
 					bonds.add(newb6);
 					bonds.add(newb7);
 					bonds.add(newb8);
 					bonds.add(newb9);
 					bonds.add(newb10);
 				} else if (restype == 'S') {
 					Atom CBatom = null;
 					Atom OGatom = null;
 					for (int i = 0; i < AminoAcid.size(); i++) {
 						Atom current = (Atom)AminoAcid.get(i);
 						if (current.getEtype() == "CB") {
 							CBatom = current;
 						} else {
 							OGatom = current;
 						}
 					}
 					Bond newb4 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 					Bond newb5 = new Bond(CBatom, OGatom, Constants.BONDCONST);
 					bonds.add(newb4);
 					bonds.add(newb5);
 				} else if (restype == 'T') {
 					Atom CBatom = null;
 					Atom CG1atom = null;
 					Atom OG2atom = null;
 					for (int i = 0; i < AminoAcid.size(); i++) {
 						Atom current = (Atom)AminoAcid.get(i);
 						if (current.getEtype() == "CB") {
 							CBatom = current;
 						} else if (current.getEtype() == "CG1") {
 							CG1atom = current;
 						} else {
 							OG2atom = current;
 						}
 					}
 					Bond newb4 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 					Bond newb5 = new Bond(CBatom, CG1atom, Constants.BONDCONST);
 					Bond newb6 = new Bond(CBatom, OG2atom, Constants.BONDCONST);
 					bonds.add(newb4);
 					bonds.add(newb5);
 					bonds.add(newb6);
 				} else if (restype == 'V') {
 					Atom CBatom = null;
 					Atom CG1atom = null;
 					Atom CG2atom = null;
 					for (int i = 0; i < AminoAcid.size(); i++) {
 						Atom current = (Atom)AminoAcid.get(i);
 						if (current.getEtype() == "CB") {
 							CBatom = current;
 						} else if (current.getEtype() == "CG1") {
 							CG1atom = current;
 						} else {
 							CG2atom = current;
 						}
 					}
 					Bond newb4 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 					Bond newb5 = new Bond(CBatom, CG1atom, Constants.BONDCONST);
 					Bond newb6 = new Bond(CBatom, CG2atom, Constants.BONDCONST);
 					bonds.add(newb4);
 					bonds.add(newb5);
 					bonds.add(newb6);
 				} else if (restype == 'W') {
 					Atom CBatom = null;
 					Atom CGatom = null;
 					Atom CD1atom = null;
 					Atom CD2atom = null;
 					Atom NE1atom = null;
 					Atom CE2atom = null;
 					Atom CE3atom = null;
 					Atom CZ2atom = null;
 					Atom CZ3atom = null;
 					Atom CH2atom = null;
 					for (int i = 0; i < AminoAcid.size(); i++) {
 						Atom current = (Atom)AminoAcid.get(i);
 						if (current.getEtype() == "CB") {
 							CBatom = current;
 						} else if (current.getEtype() == "CG") {
 							CGatom = current;
 						} else if (current.getEtype() == "CD1") {
 							CD1atom = current;
 						} else if (current.getEtype() == "CD2") {
 							CD2atom = current;
 						} else if (current.getEtype() == "NE1") {
 							NE1atom = current;
 						} else if (current.getEtype() == "CE2") {
 							CE2atom = current;
 						} else if (current.getEtype() == "CZ2") {
 							CZ2atom = current;
 						} else if (current.getEtype() == "CZ3") {
 							CZ3atom = current;
 						} else {
 							CH2atom = current;
 						}
 					}
 					Bond newb4 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 					Bond newb5 = new Bond(CBatom, CGatom, Constants.BONDCONST);
 					Bond newb6 = new Bond(CGatom, CD1atom, Constants.BONDCONST);
 					Bond newb7 = new Bond(CGatom, CD2atom, Constants.BONDCONST);
 					Bond newb8 = new Bond(CD1atom, NE1atom, Constants.BONDCONST);
 					Bond newb9 = new Bond(CD2atom, CE2atom, Constants.BONDCONST);
 					Bond newb10 = new Bond(CD2atom, CE3atom, Constants.BONDCONST);
 					Bond newb11 = new Bond(CE2atom, CZ2atom, Constants.BONDCONST);
 					Bond newb12 = new Bond(CE3atom, CZ3atom, Constants.BONDCONST);
 					Bond newb13 = new Bond(CZ2atom, CH2atom, Constants.BONDCONST);
 					Bond newb14 = new Bond(CZ3atom, CH2atom, Constants.BONDCONST);
 					bonds.add(newb4);
 					bonds.add(newb5);
 					bonds.add(newb6);
 					bonds.add(newb7);
 					bonds.add(newb8);
 					bonds.add(newb9);
 					bonds.add(newb10);
 					bonds.add(newb11);	
 					bonds.add(newb12);
 					bonds.add(newb13);
 					bonds.add(newb14);
 				} else if (restype == 'Y') {
 					Atom CBatom = null;
 					Atom CGatom = null;
 					Atom CD1atom = null;
 					Atom CD2atom = null;
 					Atom CE1atom = null;
 					Atom CE2atom = null;
 					Atom CZatom = null;
 					Atom OHatom = null;
 					for (int i = 0; i < AminoAcid.size(); i++) {
 						Atom current = (Atom)AminoAcid.get(i);
 						if (current.getEtype() == "CB") {
 							CBatom = current;
 						} else if (current.getEtype() == "CG") {
 							CGatom = current;
 						} else if (current.getEtype() == "CD1") {
 							CD1atom = current;
 						} else if (current.getEtype() == "CD2") {
 							CD2atom = current;
 						} else if (current.getEtype() == "CE1") {
 							CE1atom = current;
 						} else if (current.getEtype() == "CE2") {
 							CE2atom = current;
 						} else if (current.getEtype() == "CZ") {
 							CZatom = current;
 						} else {
 							OHatom = current;
 						}
 					}
 					Bond newb4 = new Bond(CAatom, CBatom, Constants.BONDCONST);
 					Bond newb5 = new Bond(CBatom, CGatom, Constants.BONDCONST);
 					Bond newb6 = new Bond(CGatom, CD1atom, Constants.BONDCONST);
 					Bond newb7 = new Bond(CD1atom, CD2atom, Constants.BONDCONST);
 					Bond newb8 = new Bond(CD2atom, CZatom, Constants.BONDCONST);
 					Bond newb9 = new Bond(CZatom, CE1atom, Constants.BONDCONST);
 					Bond newb10 = new Bond(CE1atom, CE2atom, Constants.BONDCONST);
 					Bond newb11 = new Bond(CE2atom, CGatom, Constants.BONDCONST);
 					Bond newb12 = new Bond(CZatom, OHatom, Constants.BONDCONST);
 					bonds.add(newb4);
 					bonds.add(newb5);
 					bonds.add(newb6);
 					bonds.add(newb7);
 					bonds.add(newb8);
 					bonds.add(newb9);
 					bonds.add(newb10);
 					bonds.add(newb11);	
 					bonds.add(newb12);
 				} else {
 					System.err.println("UNKNOWN RES TYPE " + restype);
 				}
 			}
 		}
 		detSurfaceBonds();
 		detSurfaceBackbone();
 		detSurfaceBackboneBonds();
 	}
 	public void detSurfaceBonds() {
 		for (int i = 0; i < bonds.size(); i++) {
 			Bond current = (Bond)bonds.get(i);
 			Atom first = current.getFirst();
 			Atom second = current.getSecond();
 			if (surface.contains(first) && surface.contains(second)) {
 				surfacebonds.add(current);
 			}
 		}
 	}
 	public void detSurfaceBackbone() {
 		for (int i = 0; i < backbone.size(); i++) {
 			Atom current = (Atom)backbone.get(i);
 			if (surface.contains(current)) {
 				surfacebackbone.add(current);
 			}
 		}
 	}
 	public void detSurfaceBackboneBonds() {
 		for (int i = 0; i < backbonebonds.size(); i++) {
 			Bond current = (Bond)backbonebonds.get(i);
 			Atom first = current.getFirst();
 			Atom second = current.getSecond();
 			if (surface.contains(first) && surface.contains(second)) {
 				surfacebackbonebonds.add(current);
 			}
 		}
 	}
 	public void detPotentials() {
 		for (int i = 0; i < surface.size(); i++) {
 			Atom current = (Atom)surface.get(i);
 			double cx = current.getXcoord();
 			double cy = current.getYcoord();
 			double cz = current.getZcoord();
 			char cel = current.getElement(); 
 			for (double j = -Math.ceil(size/Constants.GRIDGRAINSIZE)*Constants.GRIDGRAINSIZE - Constants.VDWDISTTHRESHOLD + xcoordcent; j < Math.ceil(size/Constants.GRIDGRAINSIZE)*Constants.GRIDGRAINSIZE + Constants.VDWDISTTHRESHOLD + xcoordcent; j += Constants.GRIDGRAINSIZE) {
 				for (double k = -Math.ceil(size/Constants.GRIDGRAINSIZE)*Constants.GRIDGRAINSIZE - Constants.VDWDISTTHRESHOLD + ycoordcent; k < Math.ceil(size/Constants.GRIDGRAINSIZE)*Constants.GRIDGRAINSIZE + Constants.VDWDISTTHRESHOLD + ycoordcent; k += Constants.GRIDGRAINSIZE) {
 					for (double l = -Math.ceil(size/Constants.GRIDGRAINSIZE)*Constants.GRIDGRAINSIZE - Constants.VDWDISTTHRESHOLD + zcoordcent; l < Math.ceil(size/Constants.GRIDGRAINSIZE)*Constants.GRIDGRAINSIZE + Constants.VDWDISTTHRESHOLD + zcoordcent; l += Constants.GRIDGRAINSIZE) {
 						double distance = Math.sqrt(Math.pow(cx - j, 2) + Math.pow(cy - k, 2) + Math.pow(cz - l, 2));
 						if (distance < Constants.VDWDISTTHRESHOLD) {
 							double EC = 0;
 							double EN = 0;
 							double EO = 0;
 							double ES = 0;
 							double EH = 0;
 							if (cel == 'C') {
 								EC = Constants.C12_C_C/Math.pow(distance, 12) - Constants.C6_C_C/Math.pow(distance, 6);
 								EN = Constants.C12_C_N/Math.pow(distance, 12) - Constants.C6_C_N/Math.pow(distance, 6);
 								EO = Constants.C12_C_O/Math.pow(distance, 12) - Constants.C6_C_O/Math.pow(distance, 6);
 								ES = Constants.C12_C_S/Math.pow(distance, 12) - Constants.C6_C_S/Math.pow(distance, 6);
 								EH = Constants.C12_C_H/Math.pow(distance, 12) - Constants.C6_C_H/Math.pow(distance, 6);
 							} else if (cel == 'N') {
 								EC = Constants.C12_C_N/Math.pow(distance, 12) - Constants.C6_C_N/Math.pow(distance, 6);
 								EN = Constants.C12_N_N/Math.pow(distance, 12) - Constants.C6_N_N/Math.pow(distance, 6);
 								EO = Constants.C12_N_O/Math.pow(distance, 12) - Constants.C6_N_O/Math.pow(distance, 6);
 								ES = Constants.C12_N_S/Math.pow(distance, 12) - Constants.C6_N_S/Math.pow(distance, 6);
 								EH = Constants.C12_N_H/Math.pow(distance, 12) - Constants.C6_N_H/Math.pow(distance, 6);
 							} else if (cel == 'O') {
 								EC = Constants.C12_C_O/Math.pow(distance, 12) - Constants.C6_C_O/Math.pow(distance, 6);
 								EN = Constants.C12_N_O/Math.pow(distance, 12) - Constants.C6_N_O/Math.pow(distance, 6);
 								EO = Constants.C12_O_O/Math.pow(distance, 12) - Constants.C6_O_O/Math.pow(distance, 6);
 								ES = Constants.C12_O_S/Math.pow(distance, 12) - Constants.C6_O_S/Math.pow(distance, 6);
 								EH = Constants.C12_O_H/Math.pow(distance, 12) - Constants.C6_O_H/Math.pow(distance, 6);
 							} else if (cel == 'S') {
 								EC = Constants.C12_C_S/Math.pow(distance, 12) - Constants.C6_C_S/Math.pow(distance, 6);
 								EN = Constants.C12_N_S/Math.pow(distance, 12) - Constants.C6_N_S/Math.pow(distance, 6);
 								EO = Constants.C12_O_S/Math.pow(distance, 12) - Constants.C6_O_S/Math.pow(distance, 6);
 								ES = Constants.C12_S_S/Math.pow(distance, 12) - Constants.C6_S_S/Math.pow(distance, 6);
 								EH = Constants.C12_S_H/Math.pow(distance, 12) - Constants.C6_S_H/Math.pow(distance, 6);
 							} else if (cel == 'H') {
 								EC = Constants.C12_C_H/Math.pow(distance, 12) - Constants.C6_C_H/Math.pow(distance, 6);
 								EN = Constants.C12_N_H/Math.pow(distance, 12) - Constants.C6_N_H/Math.pow(distance, 6);
 								EO = Constants.C12_O_H/Math.pow(distance, 12) - Constants.C6_O_H/Math.pow(distance, 6);
 								ES = Constants.C12_S_H/Math.pow(distance, 12) - Constants.C6_S_H/Math.pow(distance, 6);
 								EH = Constants.C12_H_H/Math.pow(distance, 12) - Constants.C6_H_H/Math.pow(distance, 6);
 							}
 							potentials[(int)((j+Math.ceil(size/Constants.GRIDGRAINSIZE)*Constants.GRIDGRAINSIZE+Constants.VDWDISTTHRESHOLD - xcoordcent)/Constants.GRIDGRAINSIZE)][(int)((k+Math.ceil(size/Constants.GRIDGRAINSIZE)*Constants.GRIDGRAINSIZE+Constants.VDWDISTTHRESHOLD - ycoordcent)/Constants.GRIDGRAINSIZE)][(int)((l+Math.ceil(size/Constants.GRIDGRAINSIZE)*Constants.GRIDGRAINSIZE+Constants.VDWDISTTHRESHOLD - zcoordcent)/Constants.GRIDGRAINSIZE)][0] += EC;
 							potentials[(int)((j+Math.ceil(size/Constants.GRIDGRAINSIZE)*Constants.GRIDGRAINSIZE+Constants.VDWDISTTHRESHOLD - xcoordcent)/Constants.GRIDGRAINSIZE)][(int)((k+Math.ceil(size/Constants.GRIDGRAINSIZE)*Constants.GRIDGRAINSIZE+Constants.VDWDISTTHRESHOLD - ycoordcent)/Constants.GRIDGRAINSIZE)][(int)((l+Math.ceil(size/Constants.GRIDGRAINSIZE)*Constants.GRIDGRAINSIZE+Constants.VDWDISTTHRESHOLD - zcoordcent)/Constants.GRIDGRAINSIZE)][1] += EN;
 							potentials[(int)((j+Math.ceil(size/Constants.GRIDGRAINSIZE)*Constants.GRIDGRAINSIZE+Constants.VDWDISTTHRESHOLD - xcoordcent)/Constants.GRIDGRAINSIZE)][(int)((k+Math.ceil(size/Constants.GRIDGRAINSIZE)*Constants.GRIDGRAINSIZE+Constants.VDWDISTTHRESHOLD - ycoordcent)/Constants.GRIDGRAINSIZE)][(int)((l+Math.ceil(size/Constants.GRIDGRAINSIZE)*Constants.GRIDGRAINSIZE+Constants.VDWDISTTHRESHOLD - zcoordcent)/Constants.GRIDGRAINSIZE)][2] += EO;
 							potentials[(int)((j+Math.ceil(size/Constants.GRIDGRAINSIZE)*Constants.GRIDGRAINSIZE+Constants.VDWDISTTHRESHOLD - xcoordcent)/Constants.GRIDGRAINSIZE)][(int)((k+Math.ceil(size/Constants.GRIDGRAINSIZE)*Constants.GRIDGRAINSIZE+Constants.VDWDISTTHRESHOLD - ycoordcent)/Constants.GRIDGRAINSIZE)][(int)((l+Math.ceil(size/Constants.GRIDGRAINSIZE)*Constants.GRIDGRAINSIZE+Constants.VDWDISTTHRESHOLD - zcoordcent)/Constants.GRIDGRAINSIZE)][3] += ES;
 							potentials[(int)((j+Math.ceil(size/Constants.GRIDGRAINSIZE)*Constants.GRIDGRAINSIZE+Constants.VDWDISTTHRESHOLD - xcoordcent)/Constants.GRIDGRAINSIZE)][(int)((k+Math.ceil(size/Constants.GRIDGRAINSIZE)*Constants.GRIDGRAINSIZE+Constants.VDWDISTTHRESHOLD - ycoordcent)/Constants.GRIDGRAINSIZE)][(int)((l+Math.ceil(size/Constants.GRIDGRAINSIZE)*Constants.GRIDGRAINSIZE+Constants.VDWDISTTHRESHOLD - zcoordcent)/Constants.GRIDGRAINSIZE)][4] += EH;
 						}
 					}
 				}
 			}
 		}
 	}
 	public String removeSpace(String test) {
 		String answer = "";
 		for (int i = 0; i < test.length(); i++) {
 			if (test.charAt(i) != ' ') {
 				answer = answer + test.charAt(i);
 			}
 		}
 		return answer;
 	}
 	public String getFilepath() {
 		return filepath;
 	}
 	public String[] getParsedsequence() {
 		return parsedsequence;
 	}
 	public ArrayList getStructure() {
 		return structure;
 	}
 	public ArrayList getSurface() {
 		return surface;
 	}
 	public ArrayList getBonds() {
 		return bonds;
 	}
 	public ArrayList getSurfaceBonds() {
 		return surfacebonds;
 	}
 	public ArrayList getBackbone() {
 		return backbone;
 	}
 	public ArrayList getSurfaceBackbone() {
 		return surfacebackbone;
 	}
 	public ArrayList getBackboneBonds() {
 		return backbonebonds;
 	}
 	public ArrayList getSurfaceBackboneBonds() {
 		return surfacebackbonebonds;
 	}
 	public double getXCoordCent() {
 		return xcoordcent;
 	}
 	public double getYCoordCent() {
 		return ycoordcent;
 	}
 	public double getZCoordCent() {
 		return zcoordcent;
 	}
 	public int getChaincount() {
 		return chaincount;
 	}
 	public boolean getRotated() {
 		return rotated;
 	}
 	public double getSize() {
 		return size;
 	}
 	public double[] getCent() {
 		double[] centcoords = new double[3];
 		centcoords[0] = xcoordcent;
 		centcoords[1] = ycoordcent;
 		centcoords[2] = zcoordcent;
 		return centcoords;
 	}
 	public double[][] getSizes() {
 		return sizes;
 	}
 	public double[][][][] getPotentials() {
 		return potentials;
 	}
 	public ProteinStruct transrot(double xmov, double ymov, double zmov, double theta, double phi) {
 		ArrayList<Atom> newstruct = new ArrayList<Atom>();
 		for (int i = 0; i < surface.size(); i++) {
 			Atom current = (Atom)surface.get(i);
 			Atom trcurrent = current.transAtom(xmov, ymov, zmov).rotateAtom(xcoordcent, ycoordcent, zcoordcent, theta, phi);
 			newstruct.add(trcurrent);
 		}
 		ArrayList<Atom> newbackbone = new ArrayList<Atom>();
 		for (int i = 0; i < surfacebackbone.size(); i++) {
 			Atom current = (Atom)surfacebackbone.get(i);
 			Atom trcurrent = current.transAtom(xmov, ymov, zmov).rotateAtom(xcoordcent, ycoordcent, zcoordcent, theta, phi);
 			newbackbone.add(trcurrent);
 		}
 		ProteinStruct answer = new ProteinStruct(parsedsequence, newstruct, surfacebonds, newbackbone, surfacebackbonebonds, sizes, potentials, chaincount, xcoordcent + xmov, ycoordcent + ymov, zcoordcent + zmov);
 		return answer;
 	} 
 	public ProteinStruct transrotall(double xmov, double ymov, double zmov, double theta, double phi) {
 		ArrayList<Atom> newstruct = new ArrayList<Atom>();
 		for (int i = 0; i < structure.size(); i++) {
 			Atom current = (Atom)structure.get(i);
 			Atom trcurrent = current.transAtom(xmov, ymov, zmov).rotateAtom(xcoordcent, ycoordcent, zcoordcent, theta, phi);
 			newstruct.add(trcurrent);
 		}
 		ArrayList<Atom> newbackbone = new ArrayList<Atom>();
 		for (int i = 0; i < backbone.size(); i++) {
 			Atom current = (Atom)backbone.get(i);
 			Atom trcurrent = current.transAtom(xmov, ymov, zmov).rotateAtom(xcoordcent, ycoordcent, zcoordcent, theta, phi);
 			newbackbone.add(trcurrent);
 		}
 		ProteinStruct answer = new ProteinStruct(parsedsequence, newstruct, bonds, newbackbone, backbonebonds, sizes, potentials, chaincount, xcoordcent + xmov, ycoordcent + ymov, zcoordcent + zmov);
 		return answer;
 	}
 	public ProteinStruct transrotpolar(double rmov, double thetamov, double phimov, double theta, double phi) {
 		double xmov = rmov * Math.cos(thetamov) * Math.sin(phimov);
 		double ymov = rmov * Math.sin(thetamov) * Math.sin(phimov);
 		double zmov = rmov * Math.cos(phimov);
 		ArrayList<Atom> newstruct = new ArrayList<Atom>();
 		for (int i = 0; i < surface.size(); i++) {
 			Atom current = (Atom)surface.get(i);
 			Atom trcurrent = current.transAtom(rmov, 0, 0).rotateAtom(0, 0, 0, thetamov, phimov).rotateAtom(xcoordcent + xmov, ycoordcent + ymov, zcoordcent + zmov, theta, phi);
 			newstruct.add(trcurrent);
 		}
 		ArrayList<Atom> newbackbone = new ArrayList<Atom>();
 		for (int i = 0; i < surfacebackbone.size(); i++) {
 			Atom current = (Atom)surfacebackbone.get(i);
 			Atom trcurrent = current.transAtom(rmov, 0, 0).rotateAtom(0, 0, 0, thetamov, phimov).rotateAtom(xcoordcent + xmov, ycoordcent + ymov, zcoordcent + zmov, theta, phi);
 			newbackbone.add(trcurrent);
 		}
 		ProteinStruct answer = new ProteinStruct(parsedsequence, newstruct, surfacebonds, newbackbone, surfacebackbonebonds, sizes, potentials, chaincount, xcoordcent + xmov, ycoordcent + ymov, zcoordcent + zmov);
 		return answer;
 	}
 	public ProteinStruct transrotpolarall(double rmov, double thetamov, double phimov, double theta, double phi) {
 		double xmov = rmov * Math.cos(thetamov) * Math.sin(phimov);
 		double ymov = rmov * Math.sin(thetamov) * Math.sin(phimov);
 		double zmov = rmov * Math.cos(phimov);
 		ArrayList<Atom> newstruct = new ArrayList<Atom>();
 		for (int i = 0; i < structure.size(); i++) {
			Atom current = (Atom)structure.get(i);
 			Atom trcurrent = current.transAtom(rmov, 0, 0).rotateAtom(0, 0, 0, thetamov, phimov).rotateAtom(xcoordcent + xmov, ycoordcent + ymov, zcoordcent + zmov, theta, phi);
 			newstruct.add(trcurrent);
 		}
 		ArrayList<Atom> newbackbone = new ArrayList<Atom>();
 		for (int i = 0; i < backbone.size(); i++) {
			Atom current = (Atom)backbone.get(i);
 			Atom trcurrent = current.transAtom(rmov, 0, 0).rotateAtom(0, 0, 0, thetamov, phimov).rotateAtom(xcoordcent + xmov, ycoordcent + ymov, zcoordcent + zmov, theta, phi);
 			newbackbone.add(trcurrent);
 		}
 		ProteinStruct answer = new ProteinStruct(parsedsequence, newstruct, surfacebonds, newbackbone, surfacebackbonebonds, sizes, potentials, chaincount, xcoordcent + xmov, ycoordcent + ymov, zcoordcent + zmov);
 		return answer;
 	}
 	public void printSequence() {
 		System.out.println("BEGIN SEQUENCE OF " + filepath);
 		for (int i = 0; i < chaincount; i++) {
 			System.out.println("SEQUENCE CHAIN NO " + i + " " + parsedsequence[i]);
 		}
 		System.out.println("END SEQUENCE OF " + filepath);
 	}
 	public void printStructure() {
 		System.out.println("BEGIN STRUCTURE OF " + filepath);
 		for (int i = 0; i < structure.size(); i++) {
 			Atom current = (Atom)structure.get(i);
 			current.printAtom();
 		}
 		System.out.println("END STRUCTURE OF " + filepath);
 	}
 	public void printSurface() {
 		for (int i = 0; i < surface.size(); i++) {
 			Atom current = (Atom)surface.get(i);
 			current.printAtomPDB();
 		}
 	}
 	public void printStructurePDB() {
 		for (int i = 0; i < structure.size(); i++) {
 			Atom current = (Atom)structure.get(i);
 			current.printAtomPDB();
 		}
 	}
 	public Atom getAtomByNum(int atomnum) {
 		for (int i = 0; i < surface.size(); i++) {
 			Atom current = (Atom)surface.get(i);
 			if (current.getAtomnum() == atomnum) {
 				return current;
 			}
 		}
 		return null;
 	}
 }
