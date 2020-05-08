 public class HeadsUp {
 	static String HUD[] = new String[Constants.HEIGHT];
 
 	static public void init(){ 
	for (int i = 0; i < Constants.HEIGHT; i++) {
			set(i,"");
		}
 		set(0,Constants.MSG_GAMENAME);
 		set(1,Constants.MSG_GAMESUBTITLE);
 	}
 	static public String get(int index){
 		return HUD[index];
 	}
 
 	static public void set(int index,String string){
 		HUD[index] = string;
 	}
 }
