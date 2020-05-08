 package lichen.controller;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 
 import lichen.model.Genus;
 import lichen.model.Species;
 import lichen.view.MainGUI;
 
 public class DataImporter {
 
 	private Genus lichen;
 	private String inipath = "";
 
 	public DataImporter() {
 
 		openStream();
 		//	test();
 	}
 
 	/**
 	 * @deprecated	
 	 */
 	private void test() {
 		for(Species g: lichen.returnAll()){
 			System.out.println(g.toString());
 		} 
 	}
 
 	/**
 	 * Opens the INI file for parsing lichen names and species
 	 * if FLECHTE.INI is not in the program directory a file chooser dialog is displayed
 	 * calls the parsing method when the file was found
 	 */
 	private void openStream(){
 
 		FileInputStream stream = null;
 		try {
 			stream = new FileInputStream("FLECHTE.INI");
 
 		} catch (FileNotFoundException e) { 
 			JFileChooser filechooser = new JFileChooser();
 			filechooser.showDialog(null, "Wähle .ini Datei");
 			String path = filechooser.getSelectedFile().toString();
 			try {
 				stream = new FileInputStream(path);
 			} catch (FileNotFoundException e1) {
 				JOptionPane.showMessageDialog(null, "Keine INI Datei gefunden");
 			}
 
 		}
		readFile(stream);
 	}
 
 
 	/**
 	 * Reads the INI File with the Name FLECHTE.INI
 	 * creates species objects from INI FIle 
 	 * TODO: für original INI FIle anpassen 
 	 * @pre: FLECHTE.INI must be in programm directory 
 	 * @post: species are species-object in lichen 
 	 * @post lichen.genusCount is set
 	 */
	private void readFile(FileInputStream stream) {
 		lichen = Genus.getInstance();
 		
 		try {
 			DataInputStream in = new DataInputStream(stream);
 
 			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
 			String s;
 			
 			Pattern g = Pattern.compile(Pattern.quote("["));
 			Pattern n = Pattern.compile(Pattern.quote("Anzahl="));
 			Pattern pathPattern = Pattern.compile(Pattern.quote("inipath="));
 			Pattern newPattern = Pattern.compile(Pattern.quote(";"));
 			Pattern stylePattern = Pattern.compile(Pattern.quote("style="));
 			
 			boolean flag = false;
 			Species species;
 			int i = 0;
 			int geni =0;
 
 			species = new Species();
 			String genus = "none";
 			String lastGenus = ""; 
 
 			while((s = reader.readLine()) != null){
 				Matcher gm = g.matcher(s);
 				Matcher nm = n.matcher(s);
 				Matcher pm = pathPattern.matcher(s); 
 				Matcher newm = newPattern.matcher(s);
 				Matcher styleM = stylePattern.matcher(s);
 				
 				if(pm.find()){
 					this.inipath = s.substring(9,s.length()-1); 
 				} 
 				
 				if(styleM.find()){
 					if(s.substring(6, s.length()).equalsIgnoreCase( "modern")){
 						MainGUI.setStyleModern(true);
 					}
 				}
 			
 				if(gm.find()){
 					flag = true; 
 					if(lastGenus != genus){ 
 						lastGenus = genus;
 						geni++;
 					}
 					genus = s.substring(1,s.length()-1);
 
 
 				}else{ 
 
 					if(nm.find() ){
 						int j;
 						i = Character.getNumericValue(s.charAt(7));
 						if(s.length() > 8){ 
 
 							i = i*10 + Character.getNumericValue(s.charAt(8));							
 						}
 
 					} else{ 
 						if(i != 0 && !(nm.find()) ){
 							species = new Species(); 
 
 							if(Character.isLetter(s.charAt(2))){ 
 								species.setName(s.substring(2)); 
 							}else{ 
 								species.setName(s.substring(3)); 
 							}
 							species.setGenus(genus);
 							this.lichen.push(species); 
 							i--;
 						}
 
 						if(flag && i ==0){
 							flag = false;
 						}
 					}
 				}
 			}
 			this.lichen.setGenusCount(geni);
 			in.close();
 
 		} catch (FileNotFoundException e) {
 			// TODO Auto-gecatch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 	/**
 	 * @return the inipath
 	 */
 	public String getInipath() {
 		return inipath;
 	}
 
 
 
 }
