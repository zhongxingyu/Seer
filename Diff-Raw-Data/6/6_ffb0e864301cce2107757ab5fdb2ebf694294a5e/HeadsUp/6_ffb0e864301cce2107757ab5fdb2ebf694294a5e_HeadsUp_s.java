 public class HeadsUp {
 	static String HUD[] = new String[Constants.HEIGHT];
 
 	static public void init(){ 
 		set(0,Constants.MSG_GAMENAME);
 		set(1,Constants.MSG_GAMESUBTITLE);
		set(2,"");
		set(3,"");
 	}
 	static public String get(int index){
 		return HUD[index];
 	}
 
 	static public void set(int index,String string){
 		HUD[index] = string;
 	}
 }
