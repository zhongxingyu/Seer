 package essentials;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Input;
 
 public class Updater {
 	private boolean lag = false;
 	private int overMax = 0;
 	public void update(GameContainer gc, int j){
 		checkForLag(j);
 		Input input = gc.getInput();
 		if(input.isKeyDown(Input.KEY_ESCAPE))
 			gc.exit();
 	}
 	private void checkForLag(int delta){
 		if(delta > 18)
 			overMax++;
 		else
 			overMax = 0;
 		if(overMax > 10)
 			lag = true;
 	}
 	public boolean getLag(){
 		return lag;
 	}
 }
