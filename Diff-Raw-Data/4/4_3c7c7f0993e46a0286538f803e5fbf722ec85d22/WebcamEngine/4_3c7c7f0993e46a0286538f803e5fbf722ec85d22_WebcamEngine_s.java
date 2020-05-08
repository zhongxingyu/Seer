 package com.jernejovc.bohinjweatherinfo.webcamengine;
 
 import java.util.ArrayList;
 
 public class WebcamEngine {
 	ArrayList<String[]> webcamUrls;
 	
 	public WebcamEngine()
 	{
 		webcamUrls = new ArrayList<String[]>();
 		
 		webcamUrls.add(new String [] {"Bohinjska Bistrica","http://www.drsc.si/kamere/kamslike/bohinjska/slike/boh1_0001.jpg"});
 		webcamUrls.add(new String [] {"Ribčev Laz","http://www3.bohinj.si/cam/slika3.jpg"});                                                                                         
 		webcamUrls.add(new String [] {"Ribčev Laz (v živo)","http://firma.sportnet.si:8080/dvs2000/r1.jpg"}); //http://77.234.129.226:8080/dvs2000/r2.jpg                                                    
 		webcamUrls.add(new String [] {"Bohinjska Češnjica","http://www2.arnes.si/~smisma1/canon.jpg"});                                                                             
 		//webcamUrls.put("Vogel - 4","http://www.snezni-telefon.si/Images/Kamere/6_d.jpg");
 		webcamUrls.add(new String [] {"Vogel - 1","http://www.snezni-telefon.si/Images/Kamere/6_a.jpg"}); 
 		webcamUrls.add(new String [] {"Vogel - 2","http://www.snezni-telefon.si/Images/Kamere/6_b.jpg"});
 		webcamUrls.add(new String [] {"Vogel - 3","http://www.snezni-telefon.si/Images/Kamere/6_c.jpg"});
		webcamUrls.add(new String [] {"Vogel - 4","http://www.skylinewebcams.com/images/cam/126.jpg"});
		webcamUrls.add(new String [] {"Vogel - 5","http://www.skylinewebcams.com/images/cam/129.jpg"});
 		webcamUrls.add(new String [] {"Radarska slika", "http://www.arso.gov.si/vreme/napovedi%20in%20podatki/radar.gif"});
 		webcamUrls.add(new String [] {"Radarska slika - animacija", "http://www.arso.gov.si/vreme/napovedi%20in%20podatki/radar_anim.gif"});
 		                                                                           
 		                                                                                                                                                     
 		//webcamUrls.put("Orožnova koča","http://www.bohinj.si/cam/lisc/slika1.jpg");
 	}
 
 	public ArrayList<String[]> getWebcamUrls() {
 		return webcamUrls;
 	}
 }
