 package hhu.propra_2013.gruppe_13;
 
 public class CoreLevel {
 	
 //	Figure 	figure;
 	private int 	mode;
 //	private Map		map;
 	
 	private int 	stage = 0; 	//in welchen Level sind wir, nützlich für das Endziel 
 	private String 	boss; 	//damit man weiß was der Boss ist, hauptsächlich für den NPC
 	
 	private int 	randomStartX = (int)(6*Math.random())+2;
 	private int 	randomStartY = (int)(6*Math.random())+2; //Variablen zum zufälligen setzen des Startraums
 	
 	private int  	construction[][] = new int[10][10];	//Erst Bestimmung der Positionen der Räume damit bei der Erzeugung die Positionen der Nachbarn bekannt sind
 														//int um so den Raum Typ zu codieren, sodass das int array mit einem SChritt in ein Raum-Array umgesetzt werden kann
 	
 	
 	//Array muss der Klasse bekannt sein um eine get Methode zu implementieren, wird beim Builder resettet 
 	private CoreRoom level[][] = new CoreRoom[10][10]; //Sollte genug Platz für ausreichend seltsame Konstruktionen bieten, der erste Wert sei der X Wert
 	//Konstruktor
 	CoreLevel(int inMode){
 //		figure 	= inFigure;
 		mode	= inMode;
 //		map		= inMap;
 	}
 	
 	//Getter
 	//*************************************************************************************
 	int[][] getConstruction() {
 		return construction;
 	}
 	
 	String getBoss(){
 		return boss;
 	}
 	
 	int getStage(){
 		return stage;
 	}
 	
 	int getStartX(){
 		return randomStartX;
 	}
 	
 	int getStartY(){
 		return randomStartY;
 	}
 	
 	CoreRoom getRoom(int inX, int inY){ //gibt einen Raum mit koordinaten X,Y zurück
 		return level[inX][inY]; //falls das ganze Array erwünscht ist muss man auf der 'anderen seite' 2 for schleifen schachteln und durchlaufen lassen
 	}
 	//***************************************************************************************
 	//Setter
 	
 	void setStage(int inStage){
 		stage = inStage;
 		return;
 	}
 	
 	void setBoss(String inBoss){
 		boss = inBoss;
 		return;
 	}
 	//**********************************************************************************************
 	
 	//LevelBuilder
 	void buildLevel(int inStage, String inBoss){//Stage beginnt bei 1 zu zählen
 		//Hier initialisiert damit es nur ein Level in der Logik gibt das jedes mal neu gebaut wird
 		System.out.println("X "+randomStartX+" Y "+randomStartY);
 		//Hier wird das Raum Array resettet, da ich nicht bei jedem Aufruf ein neues (wie bei construction) spawnen kann
 		for (int i = 0; i<10; i++){
 			for (int j = 0; j < 10; j++){
 				level[i][j] = null;
 			}
 		}
 		stage = inStage;
 		boss = inBoss;		
 		
 		boolean tempTop, tempBottom, tempLeft, tempRight;//für die Bestimmung der Nachbarn eines Raums
 		CoreRoom tempRoom;// = new CoreRoom(figure, stage, boss); //zwischenspeicher für einen Raum, zum hinzufügen benötigt
 
		int maxRooms = 6+2*stage;
 		int x,y; //aktuelle Koordinaten im Raum-Array
 
 		for (x = 0;x < 10; x++){
 			for (y = 0; y < 10; y++){
 				construction[x][y] = 0; //nullen des construction array
 			}
 		}
 		
 		
 		int actRooms = 0; //Zahl der bereits existierenden Räume
 		int probCounter =0; //garantiert das alle X Möglichkeiten ein Raum entsteht damit die Schleife nicht ewig läuft
 		boolean shopSet = false; //for setting the shop
 		boolean bossSet = false; //for setting the BossRoom
 		
 		
 		construction[randomStartX][randomStartY] = 1;
 
 		//was im folgenden passieren soll:
 		//gehe das construction array durch
 		//prüfe ob ein Nachbar des aktuellen Feldes von einem Raum besetzt ist (deswegen werden die Ränder und Ecken speziell betrachtet, daher der aufgeblähte Code)
 		//falls ^ja würfeln ob dort ein neuer Raum spawnt
 		//falls ja actRooms++, repeat
 		//falls nein probCounter++, bei einem bestimmten probCounter Wert wird garantiert ein Raum gespawnt
 		
 		while (actRooms <= maxRooms){ //Laufe bis alle Räume gesetzt sind
 			for (y=0; y<10; y++){	//gehe Array durch
 				for (x=0; x<10; x++){ //Beginne Fälle bei den Ecken, dnn müssen die nicht mehr bei den Rändern betrachtet werden (falls ich else if richtig verstanden habe)
 					if (x == 0 && y == 0  && actRooms <= maxRooms && construction[x][y] == 0){    //obere linke ecke
 						if (construction[x+1][y] != 0 || construction[x][y+1] != 0){ //falls es einen Nachbarn gibt
 							if(Math.random()<0.3 || probCounter ==5) { //würfle ob es einen Raum geben soll, oder setze ihn falls die letzten 5 Fälle kein Raum gesetzt wurde
 								construction[x][y] = 2; //Raum setzen
 								actRooms++; //es gibt nun einen Raum mehr
 								probCounter = 0; //der Raum-Garantie-Counter wird resetet
 							}
 							else {probCounter++;} // falls es keinen Raum gibt wird der Raum-Garantie-Counter erhöht
 						}
 					}//weitere Zweige analog
 					else if (x == 0 && y == 9 && actRooms <= maxRooms && construction[x][y] == 0) { //untere linke ecke
 						if (construction[x+1][y] != 0 || construction[x][y-1] != 0){
 							if(Math.random()<0.3 || probCounter == 5) {
 								construction[x][y] = 2;
 								actRooms++;
 								probCounter = 0;
 							}
 							else {probCounter++;}
 						}
 					}
 					else if (x == 9 && y ==0 && actRooms <= maxRooms && construction[x][y] == 0){ //obere rechte Array ecke
 						if (construction[x-1][y] != 0 || construction[x][y+1] != 0){
 							if(Math.random()<0.3 || probCounter == 5) {
 								construction[x][y] = 2;
 								actRooms++;
 								probCounter = 0;
 							}
 							else {probCounter++;}
 						}
 					}
 					else if (x == 9 && y == 9 && actRooms <= maxRooms && construction[x][y] == 0){ //untere rechte Array ecke
 						if (construction[x-1][y] != 0 || construction[x][y-1] != 0){
 							if(Math.random()<0.3 || probCounter == 5) {
 								construction[x][y] = 2;
 								actRooms++;
 								probCounter = 0;
 							}
 							else {probCounter++;}
 						}
 					}
 					else if (x != 0 && x != 9 && y == 0 && actRooms <= maxRooms && construction[x][y] == 0){ //oberer Rand
 						if (construction[x+1][y] != 0 || construction[x-1][y] != 0 || construction[x][y+1] != 0){
 							if(Math.random()<0.3 || probCounter == 5) {
 								construction[x][y] = 2;
 								actRooms++;
 								probCounter = 0;
 							}
 							else {probCounter++;}
 						}
 					}
 					else if (x != 0 && x != 9 && y == 9 && actRooms <= maxRooms && construction[x][y] == 0){ //unterer Rand
 						if (construction[x+1][y] != 0 || construction[x-1][y] != 0 || construction[x][y-1] != 0){
 							if(Math.random()<0.3 || probCounter == 5) {
 								construction[x][y] = 2;
 								actRooms++;
 								probCounter = 0;
 							}
 							else {probCounter++;}
 						}
 					}
 					else if (x == 0 && y != 0 && y != 9 && actRooms <= maxRooms && construction[x][y] == 0){ //linker Rand
 						if (construction[x+1][y] != 0 || construction[x][y+1] != 0 || construction[x][y-1] != 0){
 							if(Math.random()<0.3 || probCounter == 5) {
 								construction[x][y] = 2;
 								actRooms++;
 								probCounter = 0;
 							}
 							else {probCounter++;}
 						}
 					}
 					else if (x == 9 && y != 0 && y != 9 && actRooms <= maxRooms && construction[x][y] == 0){ //rechter Rand
 						if (construction[x-1][y] != 0 || construction[x][y+1] != 0 || construction[x][y-1] != 0){
 							if(Math.random()<0.3 || probCounter == 5) {
 								construction[x][y] = 2;
 								actRooms++;
 								probCounter = 0;
 							}
 							else {probCounter++;}
 						}
 					}
 					else if (x != 0 && x != 9 && y != 0 && y!= 9 && actRooms <= maxRooms && construction[x][y] == 0){ //irgendwo mittig
 						if (construction[x+1][y] != 0 || construction[x-1][y] != 0 || construction[x][y+1] != 0 || construction[x][y-1] != 0){
 							if(Math.random()<0.3 || probCounter == 5) {
 								construction[x][y] = 2;
 								actRooms++;
 								probCounter = 0;
 							}
 							else {probCounter++;}
 						}
 						
 					}
 				}
 			}	
 		}// ab hier normale Räume + startraum
 		System.out.println("Räume sind da");
 		
 		//unperformant as fuck, but should work TODO: change that (the unperformant part, not the working part)
 		//set Shop:
 		while (shopSet == false){
 			for (x = 0; x < 10;x++){
 				for (y = 0; y < 10; y++){
 					if (x == 0 && y == 0  && shopSet == false && construction[x][y] == 0){    //obere linke ecke
 						if (construction[x+1][y] != 0 || construction[x][y+1] != 0){ //falls es einen Nachbarn gibt
 							if(Math.random()<0.1 || probCounter ==10) { //würfle ob es einen Raum geben soll, oder setze ihn falls die letzten 5 Fälle kein Raum gesetzt wurde
 								construction[x][y] = 3; //Raum setzen
 								shopSet = true; //nun gibt es einen shop
 								probCounter = 0; //der Raum-Garantie-Counter wird resetet
 							}
 							else {probCounter++;} // falls es keinen Raum gibt wird der Raum-Garantie-Counter erhöht
 						}
 					}//weitere Zweige analog
 					else if (x == 0 && y == 9 && shopSet == false && construction[x][y] == 0) { //untere linke ecke
 						if (construction[x+1][y] != 0 || construction[x][y-1] != 0){
 							if(Math.random()<0.1 || probCounter == 10) {
 								construction[x][y] = 3;
 								shopSet = true;
 								probCounter = 0;
 							}
 							else {probCounter++;}
 						}
 					}
 					else if (x == 9 && y ==0 && shopSet == false && construction[x][y] == 0){ //obere rechte Array ecke
 						if (construction[x-1][y] != 0 || construction[x][y+1] != 0){
 							if(Math.random()<0.1 || probCounter == 10) {
 								construction[x][y] = 3;
 								shopSet = true;
 								probCounter = 0;
 							}
 							else {probCounter++;}
 						}
 					}
 					else if (x == 9 && y == 9 && shopSet == false && construction[x][y] == 0){ //untere rechte Array ecke
 						if (construction[x-1][y] != 0 || construction[x][y-1] != 0){
 							if(Math.random()<0.1 || probCounter == 10) {
 								construction[x][y] = 3;
 								shopSet = true;
 								probCounter = 0;
 							}
 							else {probCounter++;}
 						}
 					}
 					else if (x != 0 && x != 9 && y == 0 && shopSet == false && construction[x][y] == 0){ //oberer Rand
 						if (construction[x+1][y] != 0 || construction[x-1][y] != 0 || construction[x][y+1] != 0){
 							if(Math.random()<0.1 || probCounter == 10) {
 								construction[x][y] = 3;
 								shopSet = true;
 								probCounter = 0;
 							}
 							else {probCounter++;}
 						}
 					}
 					else if (x != 0 && x != 9 && y == 9 && shopSet == false && construction[x][y] == 0){ //unterer Rand
 						if (construction[x+1][y] != 0 || construction[x-1][y] != 0 || construction[x][y-1] != 0){
 							if(Math.random()<0.1 || probCounter == 10) {
 								construction[x][y] = 3;
 								shopSet = true;
 								probCounter = 0;
 							}
 							else {probCounter++;}
 						}
 					}
 					else if (x == 0 && y != 0 && y != 9 && shopSet == false && construction[x][y] == 0){ //linker Rand
 						if (construction[x+1][y] != 0 || construction[x][y+1] != 0 || construction[x][y-1] != 0){
 							if(Math.random()<0.1 || probCounter == 10) {
 								construction[x][y] = 3;
 								shopSet = false;
 								probCounter = 0;
 							}
 							else {probCounter++;}
 						}
 					}
 					else if (x == 9 && y != 0 && y != 9 && shopSet == false && construction[x][y] == 0){ //rechter Rand
 						if (construction[x-1][y] != 0 || construction[x][y+1] != 0 || construction[x][y-1] != 0){
 							if(Math.random()<0.1 || probCounter == 10) {
 								construction[x][y] = 3;
 								shopSet = true;
 								probCounter = 0;
 							}
 							else {probCounter++;}
 						}
 					}
 					else if (x != 0 && x != 9 && y != 0 && y!= 9 && shopSet == false && construction[x][y] == 0){ //irgendwo mittig
 						if (construction[x+1][y] != 0 || construction[x-1][y] != 0 || construction[x][y+1] != 0 || construction[x][y-1] != 0){
 							if(Math.random()<0.1 || probCounter == 10) {
 								construction[x][y] = 3;
 								shopSet = true;
 								probCounter = 0;
 							}
 							else {probCounter++;}
 						}
 						
 					}
 
 				}
 			}
 		} 
 		
 		//Ab hier normale räume + startraum + shop
 		System.out.println("Shop ist da");
 		while (bossSet == false){ //setze Bossraum an eine stelle wo er nur einen Nachbarn hat
 			System.out.println("trying to be a boss");
 			for (x = 0; x < 10; x++){ //unter umständen sind die benennung der fälle der fallunterscheidung murks, TODO Check that
 				for (y = 0; y < 10; y++){
 					if (x == 0 && y == 0  && bossSet == false && construction[x][y] == 0){    //obere linke ecke
 						if (construction[x+1][y] != 0 ^ construction[x][y+1] != 0){ //falls es GENAU einen Nachbarn gibt
 							if(Math.random()<0.1 || probCounter == 10) { //würfle ob es einen Raum geben soll, oder setze ihn falls die letzten 5 Fälle kein Raum gesetzt wurde
 								construction[x][y] = 4; //Raum setzen
 								bossSet = true; //es gibt nun einen Raum mehr
 								probCounter = 0; //der Raum-Garantie-Counter wird resetet
 							}
 							else {probCounter++;} // falls es keinen Raum gibt wird der Raum-Garantie-Counter erhöht
 						}
 					}
 					//weitere Zweige analog
 					else if (x == 0 && y == 9 && bossSet == false && construction[x][y] == 0) { //untere linke ecke
 						if (construction[x+1][y] != 0 ^ construction[x][y-1] != 0){
 							if(Math.random()<0.1 || probCounter == 10) {
 								construction[x][y] = 4;
 								bossSet = true;
 								probCounter = 0;
 							}
 							else {probCounter++;}
 						}
 					}
 					else if (x == 9 && y ==0 && bossSet == false && construction[x][y] == 0){ //obere rechte Array ecke
 						if (construction[x-1][y] != 0 ^ construction[x][y+1] != 0){
 							if(Math.random()<0.1 || probCounter == 10) {
 								construction[x][y] = 4;
 								bossSet = true;
 								probCounter = 0;
 							}
 							else {probCounter++;}
 						}
 					}
 					else if (x == 9 && y == 9 && bossSet == false && construction[x][y] == 0){ //untere rechte Array ecke
 						if (construction[x-1][y] != 0 ^ construction[x][y-1] != 0){
 							if(Math.random()<0.1 || probCounter == 10) {
 								construction[x][y] = 4;
 								bossSet = true;
 								probCounter = 0;
 							}
 							else {probCounter++;}
 						}
 					}
 					else if (x != 0 && x != 9 && y == 0 && bossSet == false && construction[x][y] == 0){ //oberer Rand
 						if (construction[x+1][y] != 0 ^ construction[x-1][y] != 0 ^ construction[x][y+1] != 0){
 							if(Math.random()<0.1 || probCounter == 10) {
 								construction[x][y] = 4;
 								bossSet = true;
 								probCounter = 0;
 							}
 							else {probCounter++;}
 						}
 					}
 					else if (x != 0 && x != 9 && y == 9 && bossSet == false && construction[x][y] == 0){ //unterer Rand
 						if (construction[x+1][y] != 0 ^ construction[x-1][y] != 0 ^ construction[x][y-1] != 0){
 							if(Math.random()<0.1 || probCounter == 10) {
 								construction[x][y] = 4;
 								bossSet = true;
 								probCounter = 0;
 							}
 							else {probCounter++;}
 						}
 					}
 					else if (x == 0 && y != 0 && y != 9 && bossSet == false && construction[x][y] == 0){ //linker Rand
 						if (construction[x+1][y] != 0 ^ construction[x][y+1] != 0 ^ construction[x][y-1] != 0){
 							if(Math.random()<0.1 || probCounter == 10) {
 								construction[x][y] = 4;
 								bossSet = true;
 								probCounter = 0;
 							}
 							else {probCounter++;}
 						}
 					}
 					else if (x == 9 && y != 0 && y != 9 && bossSet == false && construction[x][y] == 0){ //rechter Rand
 						if (construction[x-1][y] != 0 ^ construction[x][y+1] != 0 ^ construction[x][y-1] != 0){
 							if(Math.random()<0.1 || probCounter == 10) {
 								construction[x][y] = 4;
 								bossSet = true;
 								probCounter = 0;
 							}
 							else {probCounter++;}
 						}
 					}
 					else if (x != 0 && x != 9 && y != 0 && y!= 9 && bossSet == false && construction[x][y] == 0){ //irgendwo mittig
 						if (construction[x+1][y] != 0 ^ construction[x-1][y] != 0 ^ construction[x][y+1] != 0 ^ construction[x][y-1] != 0){
 							if(Math.random()<0.1 || probCounter == 10) {
 								construction[x][y] = 4;
 								bossSet = true;
 								probCounter = 0;
 							}
 							else {probCounter++;}
 						}
 						
 					}
 
 				}
 			}
 		}
 		System.out.println("passt nun");
 		//Ab hier enthält construction die 'Blaupause' des levels
 		//jetzt wird construction in ein Raum Array umgesetzt
 		for (x = 0; x < 10; x++){
 			for (y = 0; y < 10; y++){
 				tempTop = false; //Reset der Nachbarschaftsvariablen
 				tempBottom = false;
 				tempLeft = false;
 				tempRight = false;
 				if (construction[x][y] != 0){ //wieder mit ewiger Fallunterscheidung für Ränder und Ecken
 					if (x != 0 && x != 9 && y == 0){ //obere Kante
 						if (construction[x-1][y] != 0){tempLeft = true;} //prüfe ob es nachbarn gibt
 						if (construction[x+1][y] != 0){tempRight = true;} //falls ja:
 						if (construction[x][y+1] != 0){tempBottom = true;}//spawne raum mit passenden Nachbarn
 						tempRoom = new CoreRoom(stage, boss,tempTop, tempBottom, tempLeft, tempRight, mode);
 						level[x][y] = tempRoom;
 						//see what kind of Room is supposed to be there
 						 //and set it accordingly
 						switch (construction[x][y]){ 
 													
 						case 1:
 							level[x][y].setType("Start");
 							break;
 							
 						case 2:
 							level[x][y].setType("Raum");
 							break;
 						
 						case 3:
 							level[x][y].setType("Shop");
 							break;
 							
 						case 4: 
 							level[x][y].setType("BossRaum");
 							break;
 						}
 						level[x][y].buildRoom();
 						//System.out.println("links "+tempLeft+" rechts "+tempRight+" oben "+tempTop+" unten "+tempBottom);
 					}
 					else if (x != 0 && x != 9 && y == 9){//untere Kante
 						if (construction[x-1][y] != 0){tempLeft = true;}
 						if (construction[x+1][y] != 0){tempRight = true;}
 						if (construction[x][y-1] != 0){tempTop = true;}
 						tempRoom = new CoreRoom(stage, boss,tempTop, tempBottom, tempLeft, tempRight, mode);
 						level[x][y] = tempRoom;
 						switch (construction[x][y]){ 
 						
 						case 1:
 							level[x][y].setType("Start");
 							break;
 							
 						case 2:
 							level[x][y].setType("Raum");
 							break;
 						
 						case 3:
 							level[x][y].setType("Shop");
 							break;
 							
 						case 4: 
 							level[x][y].setType("BossRaum");
 							break;
 						}
 						level[x][y].buildRoom();
 						//System.out.println("links "+tempLeft+" rechts "+tempRight+" oben "+tempTop+" unten "+tempBottom);
 					}
 					else if (x == 0 && y == 0){ //obere linke ecke
 						if (construction[x+1][y] != 0){tempRight = true;}
 						if (construction[x][y+1] != 0){tempBottom = true;}
 						tempRoom = new CoreRoom(stage, boss,tempTop, tempBottom, tempLeft, tempRight, mode);
 						level[x][y] = tempRoom;
 						switch (construction[x][y]){ 
 						
 						case 1:
 							level[x][y].setType("Start");
 							break;
 							
 						case 2:
 							level[x][y].setType("Raum");
 							break;
 						
 						case 3:
 							level[x][y].setType("Shop");
 							break;
 							
 						case 4: 
 							level[x][y].setType("BossRaum");
 							break;
 						}
 						level[x][y].buildRoom();
 						//System.out.println("links "+tempLeft+" rechts "+tempRight+" oben "+tempTop+" unten "+tempBottom);
 					}
 					else if (x == 9 && y == 0){ //obere rechte ecke
 						if (construction[x-1][y] != 0){tempLeft = true;}
 						if (construction[x][y+1] != 0){tempBottom = true;}
 						tempRoom = new CoreRoom(stage, boss,tempTop, tempBottom, tempLeft, tempRight, mode);
 						level[x][y] = tempRoom;
 						switch (construction[x][y]){ 
 						
 						case 1:
 							level[x][y].setType("Start");
 							break;
 							
 						case 2:
 							level[x][y].setType("Raum");
 							break;
 						
 						case 3:
 							level[x][y].setType("Shop");
 							break;
 							
 						case 4: 
 							level[x][y].setType("BossRaum");
 							break;
 						}
 						level[x][y].buildRoom();
 						//System.out.println("links "+tempLeft+" rechts "+tempRight+" oben "+tempTop+" unten "+tempBottom);
 					}
 					else if (x == 0 && y != 0 && y != 9){ //linker Rand
 						if (construction[x+1][y] != 0){tempRight = true;}
 						if (construction[x][y-1] != 0){tempTop = true;}
 						if (construction[x][y+1] != 0){tempBottom = true;}
 						tempRoom = new CoreRoom(stage, boss,tempTop, tempBottom, tempLeft, tempRight, mode);
 						level[x][y] = tempRoom;
 						switch (construction[x][y]){ 
 						
 						case 1:
 							level[x][y].setType("Start");
 							break;
 							
 						case 2:
 							level[x][y].setType("Raum");
 							break;
 						
 						case 3:
 							level[x][y].setType("Shop");
 							break;
 							
 						case 4: 
 							level[x][y].setType("BossRaum");
 							break;
 						}
 						level[x][y].buildRoom();
 						//System.out.println("links "+tempLeft+" rechts "+tempRight+" oben "+tempTop+" unten "+tempBottom);
 					}
 					else if (x == 9 && y != 0 && y != 9){ //rechter Rand
 						if (construction[x-1][y] != 0){tempLeft = true;}
 						if (construction[x][y-1] != 0){tempTop = true;}
 						if (construction[x][y+1] != 0){tempBottom = true;}
 						tempRoom = new CoreRoom(stage, boss,tempTop, tempBottom, tempLeft, tempRight, mode);
 						level[x][y] = tempRoom;
 						switch (construction[x][y]){ 
 						
 						case 1:
 							level[x][y].setType("Start");
 							break;
 							
 						case 2:
 							level[x][y].setType("Raum");
 							break;
 						
 						case 3:
 							level[x][y].setType("Shop");
 							break;
 							
 						case 4: 
 							level[x][y].setType("BossRaum");
 							break;
 						}
 						level[x][y].buildRoom();
 						//System.out.println("links "+tempLeft+" rechts "+tempRight+" oben "+tempTop+" unten "+tempBottom);
 					}
 					else if (x == 0 && y == 9){// untere linke ecke
 						if (construction[x+1][y] != 0){tempRight = true;}
 						if (construction[x][y-1] != 0){tempTop = true;}
 						tempRoom = new CoreRoom(stage, boss,tempTop, tempBottom, tempLeft, tempRight, mode);
 						level[x][y] = tempRoom;
 						switch (construction[x][y]){ 
 						
 						case 1:
 							level[x][y].setType("Start");
 							break;
 							
 						case 2:
 							level[x][y].setType("Raum");
 							break;
 						
 						case 3:
 							level[x][y].setType("Shop");
 							break;
 							
 						case 4: 
 							level[x][y].setType("BossRaum");
 							break;
 						}
 						level[x][y].buildRoom();
 						//System.out.println("links "+tempLeft+" rechts "+tempRight+" oben "+tempTop+" unten "+tempBottom);
 					}
 					else if (x == 9 && y == 9){ //untere rechte ecke
 						if (construction[x-1][y] != 0){tempLeft = true;}
 						if (construction[x][y-1] != 0){tempTop = true;}
 						tempRoom = new CoreRoom(stage, boss,tempTop, tempBottom, tempLeft, tempRight, mode);
 						level[x][y] = tempRoom;
 						switch (construction[x][y]){ 
 						
 						case 1:
 							level[x][y].setType("Start");
 							break;
 							
 						case 2:
 							level[x][y].setType("Raum");
 							break;
 						
 						case 3:
 							level[x][y].setType("Shop");
 							break;
 							
 						case 4: 
 							level[x][y].setType("BossRaum");
 							break;
 						}
 						level[x][y].buildRoom();
 						//System.out.println("links "+tempLeft+" rechts "+tempRight+" oben "+tempTop+" unten "+tempBottom);
 					}
 					else if (x != 0 && x != 9 && y !=0 && y != 9){ //irgendwo mittig
 						if (construction[x-1][y] != 0){tempLeft = true;}
 						if (construction[x+1][y] != 0){tempRight = true;}
 						if (construction[x][y-1] != 0){tempTop = true;}
 						if (construction[x][y+1] != 0){tempBottom = true;}
 						tempRoom = new CoreRoom(stage, boss,tempTop, tempBottom, tempLeft, tempRight, mode);
 						level[x][y] = tempRoom;
 						switch (construction[x][y]){ 
 						
 						case 1:
 							level[x][y].setType("Start");
 							break;
 							
 						case 2:
 							level[x][y].setType("Raum");
 							break;
 						
 						case 3:
 							level[x][y].setType("Shop");
 							break;
 							
 						case 4: 
 							level[x][y].setType("BossRaum");
 							break;
 						}
 						level[x][y].buildRoom();
 						//System.out.println("links "+tempLeft+" rechts "+tempRight+" oben "+tempTop+" unten "+tempBottom);
 					}
 				}
 			}
 		}
 		
 		
 //		Map map = new Map();
 //		map.setRoom(construction);
 		
 		System.out.println("Level Fertig!");
 		for(y = 0;y <= 9;y++){
 			for(x = 0;x <= 9;x++){
 				System.out.print(construction [x][y] +"  ");
 				//map.setRoom(construction[x][y],x,y);
 			}
 			System.out.println();
 		}
 		//Ab jetzt ist das level Array fertig
 		
 		
 	}
 }
