 package prototype;
 
 import java.util.List;
 
 public class Map 
 {
 	private boolean spielertot=false;
 	private float sekundenTakt=0;
 	private static Tile[][] tiles;
 	private List<Gegner> Enemys;
 	private static int x_Tiles;
 	private static int y_Tiles;
 	private String nextmap;
 	
 	//KONSTRUKTOR
 	public Map(int x_Tiles, int y_Tiles, String nextmap){
 		this.nextmap=nextmap;
 		Map.x_Tiles = x_Tiles+1;
 		Map.y_Tiles = y_Tiles+1;
 		tiles = new Tile[x_Tiles+2][y_Tiles+2];
 		for(int x = 1; x <= x_Tiles; x++)
 		{
 			for(int y = 0; y <= y_Tiles; y++){
 				tiles[x][y] = new  Tile(x*40, y*40, false, 1); //Initialisiert jedes Feld der nutzbaren Map
 			}
 		}
 		
 		// Zwangsrahmen
 		
 		for(int x = 0; x <= x_Tiles+1;x++){tiles[x][0]= new Tile(x*40, 0*40, true, 2);}
 		for(int x = 0; x <= x_Tiles+1;x++){tiles[x][y_Tiles+1]= new Tile(x*40, (y_Tiles+1)*40, true, 2);}
 		for(int y = 0; y <= y_Tiles+1;y++){tiles[0][y]= new Tile(0*40, y*40, true, 2);}
 		for(int y = 0; y <= y_Tiles+1;y++){tiles[x_Tiles+1][y]= new Tile((x_Tiles+1)*40, y*40, true, 2);}
 	}
 
 	//METHODEN
 	public Tile getTile(int x,int y)
 	{
 		return tiles[x][y];
 	}
 	
 	public int getXTiles(){
 		return x_Tiles;
 	}
 	
 	public int getYTiles() {
 		return y_Tiles;
 	}
 	
 	public void setNextMap(String nextmap)
 	{
 		this.nextmap = nextmap;
 	}
 	
 	public String getNextMap(){
 		return nextmap;
 	}
 	
 	public void setSpielerTod(boolean tot)
 	{
 		spielertot=tot;
 		sekundenTakt=0;//damit das Blinken immer auf schwarz beginnt und gleichmaeig startet
 	}
 	
 	public void spielerTodAnimation(float frametime)//Blinken fuer Spielertod
 	{
 		sekundenTakt+=frametime;
 		if(sekundenTakt>=2)sekundenTakt=0;
 		if(spielertot&&sekundenTakt<1)
 		{
 			for(int x = 0; x <=x_Tiles; x++)
 			{
 				for(int y = 0; y <= y_Tiles; y++)
 				{
 					tiles[x][y].setTex(5);
 				}
 			}
 		}else if(spielertot)//sekundentakt zwischen 1und 2 ist hier klar und muss nicht nochmal geprueft werden
 		{
 			for(int x = 0; x <=x_Tiles; x++)
 			{
 				for(int y = 0; y <=y_Tiles; y++)
 				{
 					tiles[x][y].setTex(6);
 				}
 			}
 		}
 	}
 	
 	public void errMap()//Errstellt map die auf Fehler hinweisen soll 
 	{
		for(int x = 0; x<32;x++)
 		{
			for(int y = 0;y<18;y++)
 			{
 				tiles[x][y].setErr();
 			}
 		}
 	}
 	
 	public void setWin(){ 		//Pokale Pokale Pokale
 		for(int x = 0; x<=x_Tiles;x++)
 		{
 			for(int y = 0;y<y_Tiles;y++)
 			{
 				tiles[x][y].setWintile();
 			}
 		}
 	}
 	
 	//Maps
 	public void  erstelleTestMap(List<Gegner> Enemys){
 		this.Enemys=Enemys;
 		for(int a = 2; a<32; a++){
 			for(int b = 2; b<18; b++){
 				tiles[a][b] = new Tile(a*40,b*40,false,1);
 			}
 		}
 		for(int a = 1; a<2; a++){
 			for(int b = 1; b<18; b++){
 				tiles[a][b] = new Tile(a*40,b*40,true,2);
 			}
 		}
 		
 		for(int a = 32; a<33; a++){
 			for(int b = 1; b<18; b++){
 				tiles[a][b] = new Tile(a*40,b*40,true,2);
 			}
 		}
 		for(int a = 1; a<33; a++){
 			for(int b = 01; b<2; b++){
 				tiles[a][b] = new Tile(a*40,b*40,true,2);
 			}
 		}
 		for(int a = 1; a<33; a++){
 			for(int b = 18; b<19; b++){
 				tiles[a][b] = new Tile(a*40,b*40,true,2);
 			}
 		}
 		tiles[6][6].setTrap();
 		Enemys.add(new Gegner( 500, 400, Enemys)); //spawnen in Testmap
 		Enemys.add(new Gegner(1000, 500, Enemys));
 		Enemys.add(new Gegner( 1000, 600, Enemys));
 		Enemys.add(new Gegner(1000, 400, Enemys));
 		Enemys.add(new Gegner(1000, 200, Enemys));
 	}
 
 }      
