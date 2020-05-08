 package br.usp.pf.app;
 
 
 import br.usp.pf.dmat.DmatCreator;
 
 public class DmatCreatorApp {
 	
 	public static void createMatrix(String dyFile, String jumpsFile, String outputFolder) throws Exception {
 		
 		DmatCreator dmc = new DmatCreator();
 
 		dmc.createDmat(dyFile, jumpsFile, outputFolder) ;
     }
 
 	public static void main(String[] args) throws Exception {
		String sequence = "2221";
		int gaps = 1000;
		String folder = "/work1/wokspace/pf/data/" + sequence + "/dinamico/" + gaps + "/cut3/";
		
 		DmatCreator dmc = new DmatCreator();
 		dmc.createDmat(folder + "dy_file.data", null, folder);
 	}
 }
