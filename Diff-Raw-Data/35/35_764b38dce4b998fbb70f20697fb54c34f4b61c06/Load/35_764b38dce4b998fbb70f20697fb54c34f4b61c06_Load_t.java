 package no.hiof.stud.localstories;
 
 import java.util.List;
 
 public class Load {
 	private static String header;
 	private static String text;
 	private static String[] pictures;
 	private static String[] audio;
 	private static String date;
 	private static String keywords;
 	private static Event evt;
 	public static void load(){
 		Library.emptyList();
 		//Event 1
 		header="Hello World";
 		text="Festning";
 		date = "09.04.2013";
		pictures=new String[]{};
 		audio=new String[]{"Hey", "there"};
 		keywords = "Hello,World,day,thing,programing";
 		evt = new Event(header,text,date,pictures,audio,keywords);
 		evt.setLat(59.119885f);
 		evt.setLng(11.395483f);
 		Library.addEvent(evt);
 		//Event 2
 		header="Goodbye World";
 		text="Stadion";
 		date = "21.12.2012";
 		pictures=new String[]{"Photo", "Image"};
		audio=new String[]{};
 		keywords = "End, of,the,day,thing,programing";
 		evt = new Event(header,text,date,pictures,audio,keywords);
 		evt.setLat(59.122319f);
 		evt.setLng(11.374111f);
 		Library.addEvent(evt);
 		//Event 3
 		header="Påskekrim på HiOF";
 		text="Påskekrim i HiOFs bokbad Krimforfatter Jan-Erik Fjell var gjest under bokbadet til biblioteket ved Høgskolen i Østfold 18. mars 2013. 'Jeg håper min nye bok er klar for lansering i august', sa Jan-Erik Fjell under sitt besøk på Remmen i ettermiddag. Snaut en uke før årets påskeferie tok krimforfatteren tilhørerne med inn i sin skrivestue, arbeidshverdag og kontakt med forlagsbransjen. Bokbad med krim og påskeliljer i biblioteketet møte med Jan-Erik Fjells hovedfigur i krimbøkene hans - Anton Brekke - ble det også tid til. Jan-Erik Fjell er både som forfatter og radiovert kjent for å kunne formidle seg godt.I 2010 vant han Bokhandlerprisen i Norge for sin krimdebut med boka 'Tysteren'. Under samtalen ved Høgskolen i Østfold fortalte Jan-Erik Fjell Åpent om hvordan han jobber seg fram med to-tre utvalgte sparringspartnerne - kapittel for kapittel. Mord for mord.";
 		date = "18.03.2013";
		pictures=new String[]{};
		audio=new String[]{};
 		keywords = "Krimforfatter,Jan-Erik,Jan,Erik,Fjell";
 		evt = new Event(header,text,date,pictures,audio,keywords);
 		evt.setLat(59.129154f);//TODO: add correct location
 		evt.setLng(11.353394f);//TODO: add correct location
 		Library.addEvent(evt);
 		//Event 4
 		header="Åpningen av HiOF";
 		text="Høgskolesenteret i Halden ble offisielt innviet 15. september 2006. Kunnskapsminister Øystein Djupedal gjorde sin entrè på rød løper. Høgskoledirektør Birgitta Rødstøl Næss tok imot statsråden sammen med rektor Elin Nesje Vestli og adm. dir. i Statsbygg Øivind Christoffersen foran hovedinngangen på Remmen.";
 		date = "15.09.2006";
		pictures=new String[]{};
		audio=new String[]{};
 		keywords = "Halden, Åpning, høgskole,høyskole, resepsjon";
 		evt = new Event(header,text,date,pictures,audio,keywords);
 		evt.setLat(59.129107f);//TODO: add correct location
 		evt.setLng(11.352600f);//TODO: add correct location
 		Library.addEvent(evt);
 	}
 	
 }
