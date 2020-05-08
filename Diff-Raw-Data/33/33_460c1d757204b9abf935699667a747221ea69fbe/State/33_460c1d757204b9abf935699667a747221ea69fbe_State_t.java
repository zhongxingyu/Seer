 package states;
 
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 
 public class State {
 	public String getName(){
 		return "";
 	}
 	public Image getFlag() throws SlickException{
 		return new Image("res/flags/" + getName() + ".png");
 	}
 	public int getSecedeProbability(){
		return 100000 - ProbabilityFinder.getReadyScale(getGDP() * 100, getUnemploymentRate(), getSize(), getPopulation(), doesBorderForeignCountry());
 	}
 	protected int getGDP(){
 		return 0;
 	}
 	protected float getUnemploymentRate(){
 		return 1;
 	}
 	protected int getSize(){
 		return 0;
 	}
 	protected int getPopulation(){
 		return 0;
 	}
 	protected boolean doesBorderForeignCountry(){
 		return false;
 	}
 }
