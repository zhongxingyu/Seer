 package logic;
 
 import java.io.File;
 
 public class Onderdeel {
 	private String _antwoord;
 	private File _plaatje;
 	public Onderdeel(String antwoord, File plaatje) {
 		setAntwoord(antwoord);
 		setPlaatje(plaatje);
 	}
 	
 	public File getPlaatje(){
 		return _plaatje;
 	}
 	
 	public void setPlaatje(File plaatje) {
 		_plaatje = plaatje;
 	}
 
 	public String getAntwoord() {
 		return _antwoord;
 	}
 	public void setAntwoord(String antwoord) {
 		_antwoord = antwoord;
 	}

	public boolean isGoed() {
		// TODO Auto-generated method stub
		return false;
	}
 }
