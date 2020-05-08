 package br.usp.pf.app;
 
 
 import br.usp.pf.dmat.DmatCreator;
 
 public class DmatCreatorApp {
 	
 	public static void createMatrix(String dyFile, String jumpsFile, String outputFolder) throws Exception {
 		
 		DmatCreator dmc = new DmatCreator();
 
 		dmc.createDmat(dyFile, jumpsFile, outputFolder) ;
     }
 
 	public static void main(String[] args) throws Exception {
 		DmatCreator dmc = new DmatCreator();
		String folder = "/work1/wokspace/pf/data/0012-frust-cut=3/";
 		dmc.createDmat(folder + "dy_file.data", null, folder);
 	}
 }
