 package GUI.TV;
 
 import javax.swing.*;
 import javax.swing.event.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.io.*;
 import javax.swing.table.*;
 
 import javax.swing.JDialog.*;
 import java.awt.Rectangle;
 import java.util.Vector;
 
 import java.util.Properties;
 import org.omg.CORBA.ORB;
 import org.omg.PortableServer.POA;
 import org.omg.PortableServer.POAHelper;
 import broker.radio.*;
 import org.omg.CosNaming.NameComponent;
 import org.omg.CosNaming.NamingContext;
 import org.omg.CosNaming.NamingContextHelper;
 
 
 import java.lang.*;
 import javax.xml.parsers.*;
 import org.xml.sax.InputSource;
 import org.w3c.dom.*;
 
 import java.lang.reflect.Array;
 
 public class ScreenTv extends Thread implements TvPanelInterface{
     private String[] nome;
     private String[] cognome;
     private String[] scuderia;
     private String circuitName;
     private int tentativi = 5;
     
     private boolean inWhile = true;
     private boolean[] endRace;
     private boolean[] ritRace;
     
     private classificationTable classTable = new classificationTable();
     private bestPerformance best = new bestPerformance();
     private Competition_Monitor_Radio monitor;
     private org.omg.CORBA.Object obj;
     private int[] provaArray;
     private dati[] datiArray;
     private dati[] datiArrayDoppiati;
     
     private JFrame parent;
     private JPanel logPanel;
     private GridBagConstraints gridLog = new GridBagConstraints();
     private competitorLog[] infos;
     private float updTime;
     
     private DefaultTableModel model_1 = new DefaultTableModel(); 
     private DefaultTableModel model_2 = new DefaultTableModel(); 
     
     private FlowLayout f = new FlowLayout();
     private GridBagConstraints classificGrid = new GridBagConstraints();
 
     private DefaultTableModel[] modelClassific = new DefaultTableModel[]{model_1, model_2};
     private int current_index =0;
 
     private String corbaloc;
     private ORB orb;
     private int numComp;
     private int numLap;
     
     private float lenghtCircuit;
     private float[] arrayInfo;
     private float[] arrayOldInfo;
 
     //parsing xml
     private Integer idCompetitor;
     private String stateValue;
     private Integer checkpointValue;
     private Integer sectorValue;
 
     private int current_lap =0;
     private boolean new_table = false;
 
     public ScreenTv(String corbalocIn, Competition_Monitor_Radio monitorIn, String nameType, float updTimeIn){
 	System.out.println("ScreenTv : 0");
 	parent = new JFrame(nameType);
 
 	corbaloc = corbalocIn;
 	monitor = monitorIn;
 	updTime = updTimeIn;
 	//effettua la connessione
     }
 
     public void readConfiguration(){
 	String xmlConfString;
 	Float circuitLength;
 	org.omg.CORBA.StringHolder xmlConf = new org.omg.CORBA.StringHolder("");
 	try {
 	    circuitLength = monitor.Get_CompetitionConfiguration(xmlConf);
 	    lenghtCircuit = circuitLength;
 	    xmlConfString = xmlConf.value;
 	    System.out.println(xmlConf.value);
 	    DocumentBuilderFactory dbf =
 		DocumentBuilderFactory.newInstance();
 
 	    DocumentBuilder db = dbf.newDocumentBuilder();
 	    InputSource is = new InputSource();
 	    is.setCharacterStream(new StringReader(xmlConfString));
 
 	    Document doc = db.parse(is);
 
 	    NodeList nodes = doc.getElementsByTagName("competitionConfiguration");
 	    Element upd = (Element) nodes.item(0);
 
 	    numLap = new Integer(getNode("laps", upd)).intValue();
 	    numComp = new Integer(getNode("competitors", upd)).intValue();
 	    circuitName = getNode("name", upd);
 
 	    endRace = new boolean[numComp];
 	    ritRace = new boolean[numComp];
 	    datiArray = new dati[numComp];
 	    datiArrayDoppiati = new dati[numComp];
 	    infos = new competitorLog[numComp];
 	    for(int number = 0; number <numComp; number++){
 		endRace[number]= false;
 		ritRace[number]= false;
 	    }
 	}
 	catch(Exception eccIn){
 // 	    eccIn.printStackTrace();
 	}
     }
     public void writeDati(String xmlComp, int numCompetitor){
 	try{
 	    DocumentBuilderFactory dbf =  DocumentBuilderFactory.newInstance();
 
 	    DocumentBuilder db = dbf.newDocumentBuilder();
 	    InputSource is = new InputSource();
 	    is.setCharacterStream(new StringReader(xmlComp));
 
 	    Document doc = db.parse(is);
 
 	    NodeList nodes = doc.getElementsByTagName("competitorConfiguration");
 	    Element upd = (Element) nodes.item(0);
 
 	    nome[numCompetitor] = getNode("name", upd); 
 	    cognome[numCompetitor] = getNode("surname", upd);
 	    scuderia[numCompetitor] = getNode("team", upd);
 	}
 	catch(Exception eccIn){
 // 	    eccIn.printStackTrace();
 	}
     }
     public void addLogInfo(){
 	logPanel = new JPanel(new BorderLayout());
 	logPanel.setLayout(new GridBagLayout());
 	logPanel.setBorder(BorderFactory.createTitledBorder(null, "Log Competition", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION));
 	logPanel.setPreferredSize(new Dimension(0,30+35*numComp));
 }
 
 
     public void run(){
 	readConfiguration();
 	String circuit = new String("Circuit "+circuitName+" - Lenght = "+lenghtCircuit+" metres");
 	classTable.addTables(model_1, model_2, numComp);
 	best.addBest(circuit);
 	addLogInfo();
 	parent.add(classTable.panel1, BorderLayout.CENTER);
 	parent.add(best.getInfoUp(), BorderLayout.NORTH);
 	parent.add(logPanel, BorderLayout.SOUTH);
 	parent.pack();
 	parent.setVisible(true);
 	parent.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 	try{
 	    nome = new String[numComp];
 	    cognome = new String[numComp];
 	    scuderia = new String[numComp];
 	    String xmlComp;
 
 	    for(int index = 0; index<numComp; index++){
 		try{
 		    xmlComp = monitor.Get_CompetitorConfiguration((short)(index+1));
 		    System.out.println(xmlComp);
 		    writeDati(xmlComp, index);
 
 		    infos[index] = new competitorLog(" Id "+(index+1)+"  "+cognome[index]+" ");
 
 		    gridLog.fill = GridBagConstraints.HORIZONTAL;
 		    gridLog.gridx = 0;
 		    gridLog.gridy = index;
 		    gridLog.ipady = 5;
 		    logPanel.add(infos[index].getName(),gridLog);
 		    gridLog.fill = GridBagConstraints.HORIZONTAL;
 		    gridLog.gridx = 1;
 		    gridLog.gridy = index;
 		    gridLog.ipady = 5;
 		    logPanel.add(infos[index].getState(),gridLog);
 
 		    gridLog.fill = GridBagConstraints.HORIZONTAL;
 		    gridLog.gridx = 2;
 		    gridLog.gridy = index;
 		    gridLog.ipady = 5;
 		    logPanel.add(infos[index].getCheckpoint(),gridLog);
 		    gridLog.fill = GridBagConstraints.HORIZONTAL;
 		    gridLog.gridx = 3;
 		    gridLog.gridy = index;
 		    gridLog.ipady = 5;
 		    logPanel.add(infos[index].getSector(),gridLog);
 		    gridLog.fill = GridBagConstraints.HORIZONTAL;
 		    gridLog.gridx = 4;
 		    gridLog.gridy = index;
 		    gridLog.ipady = 5;
 		    logPanel.add(infos[index].getLap(),gridLog);
 		    model_1.insertRow(index,new Object[]{index+1, "---","---","---","---"});
 		    model_2.insertRow(index,new Object[]{index+1, "---","---","---","---"});
 		    logPanel.updateUI();
 		}
 		catch(Exception debug){
 		    JOptionPane.showMessageDialog(parent,debug.getStackTrace(), "Error",JOptionPane.ERROR_MESSAGE);
 		}
 	    }
 
 	    float interval = updTime;
 	    current_lap=0;
 	    while(inWhile){
 		try{
 		    boolean exit=true;
 		    org.omg.CORBA.StringHolder updateString = new org.omg.CORBA.StringHolder();
 		    arrayInfo = monitor.Get_CompetitionInfo(updTime, updateString);
 		    readXml(updateString.value);
 		    best.setClock("Time "+convert(updTime));
 		    // ho le tabelle completate , datiArray contiene i dati relativi alla classifica mentre datiArrayDoppiati contiene i dati dei doppiati.
 		    int index= 0;
 		    try{
 			System.out.println("DATIARRAY[0].GETLAP() =" +datiArray[0].getLap());
 			if(datiArray[0].getLap()>current_lap){//sono in presenza di una nuova classifica da inserire, inserisco i doppiati in quella vecchia e poi inverto le classifiche, svuoto quella nuova e ci inserisco i dati giusti
 			    System.out.println("LORY DEBUG : NUOVA CLASSIFICA 1");
 			    int diff; 
 			    int posiz = modelClassific[current_index].getRowCount();
 			    System.out.println("LORY DEBUG : NUOVA CLASSIFICA 2 POSIZ = "+posiz);
 
 			    try{
 				while(index < datiArray.length){//inserisco prima quelli che hanno un tempo nella lap prima, controllando che non ci siano già
 				    System.out.println("LORY DEBUG : AGGIORNAMENTO, CERCO SE CI SONO GIA OPPURE NO ");
 				    if(datiArray[index].getLap() < datiArray[0].getLap()){//ho trovato un concorrente con lap < di quella del primo concorrente, devo controllare se c'è già
 					System.out.println("LORY DEBUG : IF");
 
 					//scorro le righe della tabella per vedere se è già inserito.
 					for(int indTable = 0; indTable < posiz; indTable++){
 		
 					    String operand1 = new String(datiArray[index].getId()+" : "+cognome[datiArray[index].getId()-1]);// stringa da ricercare nella tabella
 					    System.out.println("LORY DEBUG : OPERAND 1 "+operand1);
 
 					    String operand2 = (String)modelClassific[current_index].getValueAt(indTable, 1);//new String(modelClassific[current_index].getValueAt(i, 1));//scorro i le stringhe composte come "id : cognome"
 					    System.out.println("LORY DEBUG : OPERAND 2 "+operand2);
 
 					    if(operand1.equals(operand2)){//ho già il dato
 						System.out.println("LORY DEBUG : HO GIA' IL DATO'");
 
 						indTable = posiz +1;// esco dal for, ho già la riga
 					    }
 					    else{
 						//la riga non c'è ancora, continuo a scorrere oppure sono a fine della classifica e non l'ho trovata
 						if(indTable == posiz -1){//sono alla fine della classifica, devo inserire la riga e poi aumentare di uno posiz ovviamente
 						    System.out.println("LORY DEBUG : INSERIMENTO NUOVA RIGA");
 						    System.out.println("posiz = "+posiz+" "+ datiArray[index].getId()+" : "+cognome[datiArray[index].getId()-1]+" time = "+convert(datiArray[index].getTime()));
 						    modelClassific[current_index].addRow(new Object[]{posiz, datiArray[index].getId()+" : "+cognome[datiArray[index].getId()-1],convert(datiArray[index].getTime())});
 						    posiz = posiz+1;
 						    indTable = posiz+1;
 						}
 					    }
 
 					}
 				    }
 				    index=index+1;
 				}
 			    }catch (NullPointerException npEcc){
 
 // 				npEcc.printStackTrace();
 			    }
 			    try{
 				index=0;
 				while(index < datiArrayDoppiati.length){//finchè non ho finito di scrivere i dati
 				    System.out.println("LORY : PRE DIFF=");
 				    diff = current_lap + 1 - datiArrayDoppiati[index].getLap();
 				    System.out.println("LORY : diff ="+diff);
 				    if(diff==1){modelClassific[current_index].addRow(new Object[]{posiz+index, datiArrayDoppiati[index].getId()+" : "+cognome[datiArrayDoppiati[index].getId()-1]," + 1 giro"});
 				    }
 				    else{modelClassific[current_index].addRow(new Object[]{posiz+index, datiArrayDoppiati[index].getId()+" : "+cognome[datiArrayDoppiati[index].getId()-1]," + "+diff+" giri"});
 				    }
 				    index=index+1;
 				}
 
 			    }catch (NullPointerException npEcc){
 // 				npEcc.printStackTrace();
 			    }
 			    System.out.println(" LORY : PRIMA DI INVERT");
 			    classTable.invert(current_index, new Integer(current_lap+1));//inverto le classifiche
 			    current_lap = current_lap +1;
 			    current_index=(current_index+1)%2;
 			    System.out.println("modelClassific[current_index].getRowCount() dopo invert "+modelClassific[current_index].getRowCount());
 			    int rowNumber = modelClassific[current_index].getRowCount();
 			    for(int w=0; w<rowNumber; w++){//rimuovo la vecchia classifica scritta su questa tabella.
 				System.out.println("DEBUG : RIMOZIONE VECCHIA CLASSIFICA "+w);
 				modelClassific[current_index].removeRow(0);
 			    }
 			    index=0;
 			    while(index < datiArray.length){//posso scrivere la classifica
 				System.out.println("LORY DEBUG : INDEX = "+index );
 				modelClassific[current_index].insertRow(index,new Object[]{index, datiArray[index].getId()+" : "+cognome[datiArray[index].getId()-1],convert(datiArray[index].getTime())});
 				index=index+1;
 			    }
 			}
 			else{//non sono in presenza di un nuovo giro
 			    System.out.println(" ELSE : NON SONO IN PRESENZA DI UN NUOVO GIRO, CURRENT LAP = "+current_lap);
 			    int varCiclo=0;
 			    System.out.println("modelClassific[current_index].getRowCount()=="+modelClassific[current_index].getRowCount());
 			    int rowCount = modelClassific[current_index].getRowCount();
 			    for(varCiclo =0; varCiclo<rowCount;varCiclo++){//rimuovo la vecchia classifica scritta su questa tabella.
 				System.out.println("DEBUG 2 : RIMOZIONE VECCHIA CLASSIFICA "+varCiclo);
 				modelClassific[current_index].removeRow(0);
 			    }
 
 			    varCiclo=0;
 			    while(varCiclo<datiArray.length){//posso scrivere la classifica
 			      if(datiArray[varCiclo].getLap()==current_lap){
 				    try{
 					System.out.println(" dopo cancellazione --- modelClassific[current_index].getRowCount()=="+modelClassific[current_index].getRowCount());
 					System.out.println("DEBUG 3 : SCRIVO NUOVA CLASSIFICA "+varCiclo);
 					System.out.println("Dati da scrivere qw= "+varCiclo+" congnome "+cognome[datiArray[varCiclo].getId()-1] +" "+ convert(datiArray[varCiclo].getTime()));
 					modelClassific[current_index].addRow(new Object[]{varCiclo,datiArray[varCiclo].getId()+" : "+cognome[datiArray[varCiclo].getId()-1],convert(datiArray[varCiclo].getTime())});
 					System.out.println("DEBUG 4 : SCRITTA NUOVA CLASSIFICA "+varCiclo);
 				    }
 				    catch(Exception ecd ){//ecd.printStackTrace();
 					varCiclo = datiArray.length +1;
 				    }
 				}
 				varCiclo = varCiclo+1;
 		 	   }
 
 			}
 		    }
 		    catch(NullPointerException eccCl){
 // 			System.out.println("LORY DEBUG : CLASSIFICA NON ANCORA PRESENTE");
 		    }
 		    catch(Exception eccGen){
 // 			eccGen.printStackTrace();
 // 			System.out.println("LORY DEBUG : ECCEZIONE GENERICA");
 
 		    }
 		    updTime = updTime + interval;
 		    //sleep((long)((interval-0.001)*1000));
		    sleep(100);
 		    for(int boolArray=0; boolArray<Array.getLength(endRace); boolArray++){
 			if(endRace[boolArray]==false){//controllo se tutti hanno finito la gara
 			    exit=false;
 			    boolArray=Array.getLength(endRace)+1;
 			}
 		    }
 		    if(exit==false){//se non tutti hanno finito continua altrimenti esci dal ciclo
 			inWhile=true;
 		    }
 		    else{inWhile=false;
 			JOptionPane.showMessageDialog(parent, "Race over", "Messagge from competition",JOptionPane.INFORMATION_MESSAGE);
 		    };
 		}
 		catch(org.omg.CORBA.COMM_FAILURE connEcc){
 		    JOptionPane.showMessageDialog(parent, "Attention : problem with connection..attend 3 seconds\n remaining attempts = "+tentativi,"Connection Error",JOptionPane.ERROR_MESSAGE);
 		    if(tentativi == 0 ){
 			inWhile=false;
 		    }
 		    tentativi = tentativi -1;
 		    this.sleep(3000);
 		}
 	    }
 	}
 	catch(Exception e){//e.printStackTrace();
 }
     }
     // parsing xml
     public void readXml(String xmlRecords){//, float istant){
 	System.out.println("stringa da parsare : \n"+xmlRecords);
 	try {
 	    DocumentBuilderFactory dbf =
 		DocumentBuilderFactory.newInstance();
 	    DocumentBuilder db = dbf.newDocumentBuilder();
 	    InputSource is = new InputSource();
 	    is.setCharacterStream(new StringReader(xmlRecords));
 
 	    Document doc = db.parse(is);
 
 	    NodeList nodes32 = doc.getElementsByTagName("competitors");
 	    Element prova32 = (Element) nodes32.item(0);
 
 	    NodeList nodes = prova32.getElementsByTagName("competitor");
 	    Element prova = (Element) nodes.item(0);
 
 	    //qua conto i figli
 
 	    for (int i=0; i < nodes.getLength(); i++) {
 
 		Element element = (Element) nodes.item(i);
 		NodeList comp = doc.getElementsByTagName("competitor");
 		Element line = (Element) comp.item(i);
 		System.out.println("competitor: " + getCharacterDataFromElement(line)+" length ="+nodes.getLength());
 
 		Attr attributoComp =  line.getAttributeNode("id");//(Attr) attributiComp.item(0);
 
 		Attr attributoCompEnd =  line.getAttributeNode("end");//(Attr) attributiComp.item(0);
 
 		Attr attributoCompRit =  line.getAttributeNode("retired");//(Attr) attributiComp.item(0);
 
 		System.out.println("attributo id : "+attributoComp.getNodeValue());
 	
 		NodeList check = element.getElementsByTagName("checkpoint");
 		line = (Element) check.item(0);
 
 		Attr attributoCheck =  line.getAttributeNode("compPosition");//(Attr) attributiCheck.item(0);
 		System.out.println("attributo checkpoint "+getCharacterDataFromElement(line)+" : "+attributoCheck.getNodeValue());
 		String temp = attributoCheck.getNodeValue();
 		Attr attributoCheck_2 = line.getAttributeNode("pitstop");
 		System.out.println("attributo checkpoint "+getCharacterDataFromElement(line)+" : "+attributoCheck_2.getNodeValue());
 		System.out.println("lap : "+getNode("lap", element));
 		System.out.println("sector : "+getNode("sector", element));
 		int ind = new Integer(attributoComp.getNodeValue()).intValue();
 
 		if(attributoCompRit.getValue().equals("TRUE")){
 			if(ritRace[ind-1]== false){
 			    ritRace[ind-1] = true;
 			    endRace[ind-1] = true;
 			}
 
 			infos[new Integer(attributoComp.getNodeValue()).intValue()-1].setLap(getNode("lap", element));
 			if(temp.equals("arriving")){
 			    infos[new Integer(attributoComp.getNodeValue()).intValue()-1].setState(" Ritired while arriving ");}
 			else{
 			    if(temp.equals("passed")){
 				infos[new Integer(attributoComp.getNodeValue()).intValue()-1].setState(" Ritired after passing ");}
 			    else{
 				infos[new Integer(attributoComp.getNodeValue()).intValue()-1].setState(" Ritired on ");}
 			}
 			infos[new Integer(attributoComp.getNodeValue()).intValue()-1].setCheckpoint(getNode("checkpoint", element));
 			infos[new Integer(attributoComp.getNodeValue()).intValue()-1].setSector(getNode("sector", element));
 		    }
 		else {
 			if(attributoCompEnd.getValue().equals("TRUE")){
 			  infos[new Integer(attributoComp.getNodeValue()).intValue()-1].setLap(getNode("lap", element));
 			  infos[new Integer(attributoComp.getNodeValue()).intValue()-1].setState(" End Race ");
 			  infos[new Integer(attributoComp.getNodeValue()).intValue()-1].setCheckpoint(getNode("checkpoint", element));
 			  infos[new Integer(attributoComp.getNodeValue()).intValue()-1].setSector(getNode("sector", element));
 
 			  if(endRace[ind-1]== false){
 			    endRace[ind-1] = true;
 			  }
 			  }
 			else{
 			      if(attributoCheck_2.getNodeValue().equals("TRUE")){// sono al pitstop?
 // 				    JOptionPane.showMessageDialog(parent, "Competitor ai box!", "Messagge from competition",JOptionPane.INFORMATION_MESSAGE);
 				    System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
 				    if(new Integer(getNode("checkpoint",element)).intValue() == 1){
 					if (temp.equals("arriving")){
 					    infos[new Integer(attributoComp.getNodeValue()).intValue()-1].setLap(getNode("lap", element));
 					    infos[new Integer(attributoComp.getNodeValue()).intValue()-1].setState(" leaving box ");
 					    infos[new Integer(attributoComp.getNodeValue()).intValue()-1].setCheckpoint(getNode("checkpoint", element));
 					    infos[new Integer(attributoComp.getNodeValue()).intValue()-1].setSector(" Box ");
 					}
 					else{
 					    infos[new Integer(attributoComp.getNodeValue()).intValue()-1].setLap(getNode("lap", element));
 					    infos[new Integer(attributoComp.getNodeValue()).intValue()-1].setState(" in box ");
 					    infos[new Integer(attributoComp.getNodeValue()).intValue()-1].setCheckpoint(getNode("checkpoint", element));
 					    infos[new Integer(attributoComp.getNodeValue()).intValue()-1].setSector(" Box ");
 					}
 				    }
 				}
 			  else{
 			  infos[new Integer(attributoComp.getNodeValue()).intValue()-1].setLap(getNode("lap", element));
 			  infos[new Integer(attributoComp.getNodeValue()).intValue()-1].setState(temp);
 			  infos[new Integer(attributoComp.getNodeValue()).intValue()-1].setCheckpoint(getNode("checkpoint", element));
 			  infos[new Integer(attributoComp.getNodeValue()).intValue()-1].setSector(getNode("sector", element));
 			  }
 			}
 		    }
 	     }	
 	    System.out.println("bestTimes");
 	    NodeList bestT = doc.getElementsByTagName("bestTimes");
 	    Element element = (Element)bestT.item(0);
 	
 	    NodeList lap = element.getElementsByTagName("lap");
 	    Element line = (Element) lap.item(0);
 	    NamedNodeMap attributiLap =line.getAttributes();
 
 
 	    Attr attributoLap = (Attr) attributiLap.item(0);
 	    System.out.println("attributo lap "+getCharacterDataFromElement(line)+" : "+attributoLap.getNodeValue());
 	    if(new Integer(attributoLap.getNodeValue()).intValue() !=-1){
 		Float f=new Float(getNode("time", element));
 
 		best.setBestLap(attributoLap.getNodeValue(),convert(f.floatValue()), getNode("competitorId", element));
 	    }
 
 	
 	    NodeList bestSector = doc.getElementsByTagName("sectors");
 	    element = (Element)bestSector.item(0);
 
 	    NodeList sector = element.getElementsByTagName("sector");
 	    line = (Element) sector.item(0);
 	    NamedNodeMap attributiSector =line.getAttributes();
 	    Attr attributoSector = (Attr) attributiSector.item(0);
 	    System.out.println("attributo sector 1 "+getCharacterDataFromElement(line)+" : "+attributoSector.getNodeValue());
 	    System.out.println("--time : "+getNode("time", line));
 
 	    System.out.println("--id : "+getNode("competitorId", line));
 	    if(new Double(getNode("time",line)).intValue() !=-1.0){	
 		Float f=new Float(getNode("time", line));
 		best.setBestSector(1, getNode("competitorId", line), getNode("lap", line), convert(f.floatValue()));
 	    }
 	    sector = element.getElementsByTagName("sector");
 	    line = (Element) sector.item(1);
 	    attributiSector =line.getAttributes();
 	    attributoSector = (Attr) attributiSector.item(0);
 	    System.out.println("attributo sector 2 "+getCharacterDataFromElement(line)+" : "+attributoSector.getNodeValue());
 	    System.out.println("--time : "+getNode("time", line));
 	    System.out.println("--id : "+getNode("competitorId", line));
 	    if(new Double(getNode("time",line)).intValue() !=-1.0){	
 		Float f=new Float(getNode("time", line));
 		best.setBestSector(2, getNode("competitorId", line), getNode("lap", line), convert(f.floatValue()));
 	
 	    }
 	    sector = element.getElementsByTagName("sector");
 	    line = (Element) sector.item(2);
 	    attributiSector =line.getAttributes();
 	    attributoSector = (Attr) attributiSector.item(0);
 	    System.out.println("attributo sector 3"+getCharacterDataFromElement(line)+" : "+attributoSector.getNodeValue());
 	    System.out.println("--time : "+getNode("time", line));
 	    System.out.println("--id : "+getNode("competitorId", line));
 	    if(new Double(getNode("time",line)).intValue() !=-1.0){	
 		Float f=new Float(getNode("time", line));
 		best.setBestSector(3, getNode("competitorId", line), getNode("lap", line), convert(f.floatValue()));
 	    }
 	    try{
 
 		System.out.println("--printing classific : ");
 		NodeList cl = doc.getElementsByTagName("classification");
 		Element elementTemp = (Element)cl.item(0);
 	
 		NodeList comp42 =elementTemp.getElementsByTagName("competitor");
 		Element compEl = (Element) comp42.item(0);
 		int w=0;
 		int j=0;
 		System.out.println(" LORY : COMP42.GETLENGTH ="+comp42.getLength()+" arrayinfo.length = "+arrayInfo.length);
 		datiArray = new dati [arrayInfo.length];
 		datiArrayDoppiati = new dati[comp42.getLength() - arrayInfo.length];
 		for (int i=0; i < comp42.getLength(); i++) {
 
 		    element = (Element) comp42.item(i);
 		    line = (Element) comp42.item(i);
 		    System.out.println("----competitor: " + getCharacterDataFromElement(line)+" length ="+comp42.getLength());
 		    NamedNodeMap attributiComp =line.getAttributes();
 
 		    Attr attributoComp = (Attr) attributiComp.item(0);
 		    System.out.println("------attributo id : "+attributoComp.getNodeValue());
 	
 		    System.out.println("------lap : "+getNode("lap", line));
 
 		    if(j<arrayInfo.length){
 			try{
 			    datiArray[i] = new dati(new Integer(getNode("lap", line)).intValue(), new Integer(attributoComp.getNodeValue()).intValue(), arrayInfo[j]);
 			    System.out.println("posizione "+i+" del concorrente "+datiArray[i].getId()+" in tempo : "+convert(datiArray[i].getTime()));
 			    j=j+1;
 			}
 			catch(Exception arrayBound){
 // 			    arrayBound.printStackTrace();
 			    j= arrayInfo.length +1;
 			}
 		    }
 		    else{
 			datiArrayDoppiati[w] = new dati(new Integer(getNode("lap", line)).intValue(), new Integer(attributoComp.getNodeValue()).intValue(), (float)-2.0);
 			System.out.println("DOPPIATI concorrente "+datiArrayDoppiati[w].getId()+" in tempo : "+convert(datiArrayDoppiati[w].getTime()));
 
 			w=w+1;
 		    }
 		}
 	    }
 	    catch (Exception e){
 // 		System.out.println("classification non presente");
 	    }
 	
 	}
 	catch (Exception e) {
 // 	    e.printStackTrace();
 // 	    System.out.println("eccezione in readXml");
 	}
     }
     public static String getNode(String tag, Element element){
 	NodeList compId = element.getElementsByTagName(tag);
         Element line = (Element) compId.item(0);
 	// 	System.out.println("Id Comp : "+getCharacterDataFromElement(line));
 	return getCharacterDataFromElement(line);
     }
     public static String getCharacterDataFromElement(Element e) {
 	Node child = e.getFirstChild();
 	if (child instanceof CharacterData) {
 	    CharacterData cd = (CharacterData) child;
 	    return cd.getData();
 	}
 	return "-";
     }
 
 
     public String convert(float timeIn){
 
 	int ore = (int)(timeIn/3600);
 	int minuti = (int)(timeIn/60)-(60*ore);
 	int secondi = (int)(timeIn-(minuti*60+ore*3600));
 	int millesimi = (int)((timeIn - (minuti*60+ore*3600+secondi))*1000);
 	//int decimi = (int)((timeIn - (minuti*60+ore*3600+secondi))*10);
 	String time;
 	if(minuti<10){
 	    if(secondi <10){
 		if(millesimi<10){
 		   time = new String("0"+ore+":0"+minuti+":0"+secondi+":00"+millesimi);}
 		else if(millesimi<100){
 		   time = new String("0"+ore+":0"+minuti+":0"+secondi+":0"+millesimi);}
 		else{
 		    time = new String("0"+ore+":0"+minuti+":0"+secondi+":"+millesimi);}
 	    }
 	    else{
 		if(millesimi<10){
 		   time = new String("0"+ore+":0"+minuti+":"+secondi+":00"+millesimi);}
 		else if(millesimi<100){
 		   time = new String("0"+ore+":0"+minuti+":"+secondi+":0"+millesimi);}
 		else{
 		    time = new String("0"+ore+":0"+minuti+":"+secondi+":"+millesimi);}
 	    }
 	}
 	else{
 	    if(secondi <10){
 		if(millesimi<10){
 		   time = new String("0"+ore+":"+minuti+":0"+secondi+":00"+millesimi);}
 		else if(millesimi<100){
 		   time = new String("0"+ore+":"+minuti+":0"+secondi+":0"+millesimi);}
 		else{
 		    time = new String("0"+ore+":"+minuti+":0"+secondi+":"+millesimi);}
 	    }
 	    else{
 		if(millesimi<10){
 		   time = new String("0"+ore+":"+minuti+":"+secondi+":00"+millesimi);}
 		else if(millesimi<100){
 		   time = new String("0"+ore+":"+minuti+":"+secondi+":0"+millesimi);}
 		else{
 		    time = new String("0"+ore+":"+minuti+":"+secondi+":"+millesimi);}
 	    }}
 
 	return time;
     }
 
 }
 
 class dati{
     private int lap=-1;
     private int id=-1;
     private float time =(float)-1.0;
     public int getLap(){
 	return lap;
     }
     public float getTime(){
 	return time;
     }
     public int getId(){
 	return id;
     }
     public dati(int lapIn, int idIn, float timeIn){
 	lap = lapIn;
 	id = idIn;
 	time = timeIn;
     }
 }
 
 //oggetto tabella con classifiche
 class classificationTable{
     private int tabellaCorrente =1;
     private JTable classific_1;
     private JTable classific_2;
     private JScrollPane panelCl_1;
     private JPanel classificPanel;
     private JScrollPane panelCl_2;
     public JPanel panel1;
     private JPanel panelClass1;
     private JPanel panelClass2;
     private GridBagConstraints gridPanel = new GridBagConstraints();
     private GridBagConstraints gridPanel2 = new GridBagConstraints();
     private JLabel lap1 = new JLabel("Lap 0");
     private JLabel lap2 = new JLabel("-");
     public void invert(int i, Integer lap){
 	if(i==1){
 	    panel1.add(panelClass2, BorderLayout.WEST);
 	    panel1.add(panelClass1, BorderLayout.EAST);
 	    lap1.setText("Lap "+lap.toString());
 	}
 	else{
 	    panel1.add(panelClass1, BorderLayout.WEST);
 	    panel1.add(panelClass2, BorderLayout.EAST);
 	    lap2.setText("Lap "+lap.toString());
 
 	}
 	panel1.updateUI();
     }
 
     public void addTables(DefaultTableModel model_1, DefaultTableModel model_2, int compNum){
 	model_1.addColumn("Position");
 	model_1.addColumn("Competitor"); 
 	model_1.addColumn("Time");
 	model_2.addColumn("Position");
 	model_2.addColumn("Competitor"); 
 	model_2.addColumn("Time");
 	classific_1 = new JTable(model_1);
 	classific_2 = new JTable(model_2);
 
 	TableColumn column = null;
 	column = classific_1.getColumnModel().getColumn(0);
 	column.setPreferredWidth(20);
 	TableColumn column2 = null;
 	column2 = classific_2.getColumnModel().getColumn(0);
 	column2.setPreferredWidth(20);
 
 	panelCl_1 = new JScrollPane(classific_1);
 	panelCl_1.setPreferredSize(new Dimension(300, 70));
 
 	panelCl_2 = new JScrollPane(classific_2);
 
 	panelCl_2.setPreferredSize(new Dimension(300, 70));
 	panelCl_1.setVerticalScrollBar(new JScrollBar());
 	panelCl_1.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 
 	panelCl_2.setVerticalScrollBar(new JScrollBar());
 	panelCl_2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 
 
 	panel1 = new JPanel(new BorderLayout());
 	panelClass1 = new JPanel(new BorderLayout());
 	panelClass1.add(lap1, BorderLayout.NORTH);
 	panelClass1.add(panelCl_1, BorderLayout.CENTER);
 	panelClass2 = new JPanel(new BorderLayout());
 	panelClass2.add(lap2, BorderLayout.NORTH);
 	panelClass2.add(panelCl_2, BorderLayout.CENTER);
 	panel1.setBorder(BorderFactory.createTitledBorder(null, "Classific", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION));
 	panel1.setPreferredSize(new Dimension(620  , 35+40*compNum));// TODO : 35 moltiplicato per il numero di concorrenti, farsi passare numero concorrenti
 	panel1.add(panelClass1, BorderLayout.CENTER);
 	panel1.add(panelClass2, BorderLayout.WEST);
 
     }
 }
 
 class bestPerformance{
     private JPanel bestPanel;
     private JPanel infoUp;
 
     private GridBagConstraints bestGrid = new GridBagConstraints();
     private JTextField textBoxLap = new JTextField("-",3);
     private JTextField textBoxLapId = new JTextField("-",2);
     private JTextField textBoxLapTime = new JTextField("-",10);
     private JLabel labelLap = new JLabel("Best lap n° : ");
     private JLabel labelLapId = new JLabel(" by competitor : ");
     private JLabel labelLapTime = new JLabel(" in time : ");
 
     private JTextField textBoxSector1Lap = new JTextField("-",3);
     private JTextField textBoxSector1Id = new JTextField("-",2);
     private JTextField textBoxSector1Time = new JTextField("-",10);
     private JLabel labelSector1 = new JLabel("Best Sector 1 at lap n° : ");
     private JLabel labelSector1Id = new JLabel(" by competitor : ");
     private JLabel labelSector1Time = new JLabel(" in time : ");
 
 
     private JTextField textBoxSector2Lap = new JTextField("-",3);
     private JTextField textBoxSector2Id = new JTextField("-",2);
     private JTextField textBoxSector2Time = new JTextField("-",10);
     private JLabel labelSector2 = new JLabel("Best Sector 2 at lap n° : ");
     private JLabel labelSector2Id = new JLabel(" by competitor : ");
     private JLabel labelSector2Time = new JLabel(" in time : ");
 
 
     private JTextField textBoxSector3Lap = new JTextField("-",3);
     private JTextField textBoxSector3Id = new JTextField("-",2);
     private JTextField textBoxSector3Time = new JTextField("-",10);
     private JLabel labelSector3 = new JLabel("Best Sector 3 at lap n° : ");
     private JLabel labelSector3Id = new JLabel(" by competitor : ");
     private JLabel labelSector3Time = new JLabel(" in time : ");
 
     private JLabel labelClock = new JLabel("Time 00:00:00:000");
     private JLabel labelCircuit;
 
     public JPanel getInfoUp(){
 	return infoUp;
     }
     public JPanel getBestPanel(){
 	return bestPanel;
     }
     public JLabel getClock(){
 	return labelClock;
     }
     public void setClock(String timeIn){
 	labelClock.setText(timeIn);
     }
     public void addBest(String stringForLabel){
 	bestPanel = new JPanel(new BorderLayout());
 	infoUp = new JPanel(new BorderLayout());
 
 	bestPanel.setLayout(new GridBagLayout());
 	bestPanel.setBorder(BorderFactory.createTitledBorder(null, "Best Performance", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION));
 
 	labelClock.setFont(new Font("Serif", Font.BOLD, 25));
 	labelCircuit= new JLabel(stringForLabel);
 	labelCircuit.setFont(new Font("Serif", Font.BOLD, 15));
 	bestGrid.fill = GridBagConstraints.HORIZONTAL;
 	bestGrid.gridx = 0;
 	bestGrid.gridy = 0;
 	bestGrid.ipady = 5;
 	bestPanel.add(labelLap,bestGrid);
 	bestGrid.fill = GridBagConstraints.HORIZONTAL;
 	bestGrid.gridx = 1;
 	bestGrid.gridy = 0;
 	bestGrid.ipady = 5;
 	bestPanel.add(textBoxLap,bestGrid);
 	bestGrid.fill = GridBagConstraints.HORIZONTAL;
 	bestGrid.gridx = 2;
 	bestGrid.gridy = 0;
 	bestGrid.ipady = 5;
 	bestPanel.add(labelLapId,bestGrid);
 	bestGrid.fill = GridBagConstraints.HORIZONTAL;
 	bestGrid.gridx = 3;
 	bestGrid.gridy = 0;
 	bestGrid.ipady = 5;
 	bestPanel.add(textBoxLapId,bestGrid);
 	bestGrid.fill = GridBagConstraints.HORIZONTAL;
 	bestGrid.gridx = 4;
 	bestGrid.gridy = 0;
 	bestGrid.ipady = 5;
 	bestPanel.add(labelLapTime,bestGrid);
 	bestGrid.fill = GridBagConstraints.HORIZONTAL;
 	bestGrid.gridx = 5;
 	bestGrid.gridy = 0;
 	bestGrid.ipady = 5;
 	bestPanel.add(textBoxLapTime,bestGrid);
 	//best sector
 
 	bestGrid.fill = GridBagConstraints.HORIZONTAL;
 	bestGrid.gridx = 0;
 	bestGrid.gridy = 1;
 	bestGrid.ipady = 5;
 	bestPanel.add(labelSector1,bestGrid);
 	bestGrid.fill = GridBagConstraints.HORIZONTAL;
 	bestGrid.gridx = 1;
 	bestGrid.gridy = 1;
 	bestGrid.ipady = 5;
 	bestPanel.add(textBoxSector1Lap,bestGrid);
 	bestGrid.fill = GridBagConstraints.HORIZONTAL;
 	bestGrid.gridx = 2;
 	bestGrid.gridy = 1;
 	bestGrid.ipady = 5;
 	bestPanel.add(labelSector1Id,bestGrid);
 	bestGrid.fill = GridBagConstraints.HORIZONTAL;
 	bestGrid.gridx = 3;
 	bestGrid.gridy = 1;
 	bestGrid.ipady = 5;
 	bestPanel.add(textBoxSector1Id,bestGrid);
 	bestGrid.fill = GridBagConstraints.HORIZONTAL;
 	bestGrid.gridx = 4;
 	bestGrid.gridy = 1;
 	bestGrid.ipady = 5;
 	bestPanel.add(labelSector1Time,bestGrid);
 	bestGrid.fill = GridBagConstraints.HORIZONTAL;
 	bestGrid.gridx = 5;
 	bestGrid.gridy = 1;
 	bestGrid.ipady = 5;
 	bestPanel.add(textBoxSector1Time,bestGrid);
 
 
 	bestGrid.fill = GridBagConstraints.HORIZONTAL;
 	bestGrid.gridx = 0;
 	bestGrid.gridy = 2;
 	bestGrid.ipady = 5;
 	bestPanel.add(labelSector2,bestGrid);
 	bestGrid.fill = GridBagConstraints.HORIZONTAL;
 	bestGrid.gridx = 1;
 	bestGrid.gridy = 2;
 	bestGrid.ipady = 5;
 	bestPanel.add(textBoxSector2Lap,bestGrid);
 	bestGrid.fill = GridBagConstraints.HORIZONTAL;
 	bestGrid.gridx = 2;
 	bestGrid.gridy = 2;
 	bestGrid.ipady = 5;
 	bestPanel.add(labelSector2Id,bestGrid);
 	bestGrid.fill = GridBagConstraints.HORIZONTAL;
 	bestGrid.gridx = 3;
 	bestGrid.gridy = 2;
 	bestGrid.ipady = 5;
 	bestPanel.add(textBoxSector2Id,bestGrid);
 	bestGrid.fill = GridBagConstraints.HORIZONTAL;
 	bestGrid.gridx = 4;
 	bestGrid.gridy = 2;
 	bestGrid.ipady = 5;
 	bestPanel.add(labelSector2Time,bestGrid);
 	bestGrid.fill = GridBagConstraints.HORIZONTAL;
 	bestGrid.gridx = 5;
 	bestGrid.gridy = 2;
 	bestGrid.ipady = 5;
 	bestPanel.add(textBoxSector2Time,bestGrid);
 
 
 	bestGrid.fill = GridBagConstraints.HORIZONTAL;
 	bestGrid.gridx = 0;
 	bestGrid.gridy = 3;
 	bestGrid.ipady = 5;
 	bestPanel.add(labelSector3,bestGrid);
 	bestGrid.fill = GridBagConstraints.HORIZONTAL;
 	bestGrid.gridx = 1;
 	bestGrid.gridy = 3;
 	bestGrid.ipady = 5;
 	bestPanel.add(textBoxSector3Lap,bestGrid);
 	bestGrid.fill = GridBagConstraints.HORIZONTAL;
 	bestGrid.gridx = 2;
 	bestGrid.gridy = 3;
 	bestGrid.ipady = 5;
 	bestPanel.add(labelSector3Id,bestGrid);
 	bestGrid.fill = GridBagConstraints.HORIZONTAL;
 	bestGrid.gridx = 3;
 	bestGrid.gridy = 3;
 	bestGrid.ipady = 5;
 	bestPanel.add(textBoxSector3Id,bestGrid);
 	bestGrid.fill = GridBagConstraints.HORIZONTAL;
 	bestGrid.gridx = 4;
 	bestGrid.gridy = 3;
 	bestGrid.ipady = 5;
 	bestPanel.add(labelSector3Time,bestGrid);
 	bestGrid.fill = GridBagConstraints.HORIZONTAL;
 	bestGrid.gridx = 5;
 	bestGrid.gridy = 3;
 	bestGrid.ipady = 5;
 	bestPanel.add(textBoxSector3Time,bestGrid);
 	infoUp.add(labelClock, BorderLayout.NORTH);
 	infoUp.add(labelCircuit, BorderLayout.CENTER);
 	infoUp.add(bestPanel, BorderLayout.SOUTH);
     }
 
     public void setBestLap(String lap,String  time,String  id){
 	textBoxLap.setText(lap);
 	textBoxLapTime.setText(time);
 	textBoxLapId.setText(id);
 
     }
     public void setBestSector(int sector, String id, String lap, String time){
 	if(sector == 1){
 	    textBoxSector1Lap.setText(lap);
 	    textBoxSector1Id.setText(id);
 	    textBoxSector1Time.setText(time);
 	}
 	if(sector == 2){
 	    textBoxSector2Lap.setText(lap);
 	    textBoxSector2Id.setText(id);
 	    textBoxSector2Time.setText(time);
 	}
 	if(sector == 3){
 	    textBoxSector3Lap.setText(lap);
 	    textBoxSector3Id.setText(id);
 	    textBoxSector3Time.setText(time);
 	}
 
 
     }
 }
 
 class competitorLog{
     private JLabel checkpoint = new JLabel("");
     private JLabel lap = new JLabel("");
     private JLabel name;
     private JLabel state = new JLabel(" Iscritto alla competizione ");
     private JLabel sector = new JLabel("");
     public void setSector(String sectorIn){
 	sector.setText(", sector "+sectorIn);
     }
     public void setCheckpoint(String checkIn){
 	int value = new Integer(checkIn).intValue();
 	if(value <10){checkpoint.setText(" checkpoint "+checkIn+" ");}
 	else{checkpoint.setText(" checkpoint "+checkIn);}
     }
     public JLabel getCheckpoint(){
 	return checkpoint;
     }
     public void setLap(String lapIn){
 	int value = new Integer(lapIn).intValue();
 	if(value <10){lap.setText(" during lap "+lapIn+" ");}
 	else{lap.setText(" during lap "+lapIn);}
     }
 
     public JLabel getState(){
 	return state;
     }
 
     public void setState(String stateIn){
 	if(stateIn.equals("arriving")){
 	    state.setText(stateIn +" to ");
 	}
 	else {
 	    state.setText(stateIn+"     ");
 	}
     }
 
     public JLabel getName(){
 	return name;
     }
     public JLabel getSector(){
 	return sector;
     }
     public JLabel getLap(){
 	return lap;
     }
     public competitorLog(String nameIn){
 	name= new JLabel(nameIn);
     }
 }
