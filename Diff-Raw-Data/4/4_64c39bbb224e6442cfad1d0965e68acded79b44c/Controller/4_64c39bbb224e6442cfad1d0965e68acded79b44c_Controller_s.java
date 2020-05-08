 package se.turbotorsk.mybar.controller;
 
 import java.util.LinkedList;
 
 import android.content.ContentResolver;
 import se.turbotorsk.mybar.model.Data;
 import se.turbotorsk.mybar.model.Drink;
 
 public class Controller {
	private ContentResolver contentResolver = null;
 	public final static Controller controller = new Controller();
 	//private CoreLib cl = new CoreLib();
 	private Data data = null;
 	private int myBarID = 1;
 	private DrinkManager dm = null;
 	//		ContentResolver contentResolver = CoreLib.ContentResolver();
 	public Controller()
 	{
 		//contentResolver = CoreLib.ContentResolver();
 		dm = new DrinkManager();
 		
 		//data = new Data(contentResolver);	
 	}
 
 	public LinkedList<Drink> getMyBar()
 	{
 		//dm.getMyBar(data.getMyIngredients(this.myBarID), data.getAllDrinks(this.contentResolver));			Implement this in data....
 		return null;
 	}
 	
 	public String[] getDrinkNamesAsArray()
 	{
 		String[] array = null;
 		int i = 0;
		for(Drink drink : data.getAllDrinks(this.contentResolver)){
 			array[i++] = drink.getName();	
 		}
 		return array;
 	}
 	
 	
 	public Drink getIngredientById(int id){
 		return null;
 	}
 	//-----------------------------------------------------Methods for getting and setting favorite drinks ----------------------------------
 	
 	
 	public boolean addFavoriteDrink(int id){
 		return false;
 	}
 	
 	public LinkedList<Drink> getFavoritDrinks()
 	{
 		return null;
 	}
 	
 	// -----------------------------------------------------Methods for Ingredient categories ------------------------------------
 	
 	public boolean addMyBarCat(String Name)
 	{
 		return false; //data.AddMyBarCat(String name);							Add this method in data. 
 	}
 	
 	public boolean addIngredientToList(int ingredientID)
 	{
 		// data.AddIngredientToList(ingredientID, this.myBarID); 				Add this method in data. 
 		return false; 
 	}
 	
 	public boolean changeMyBarID(int myBarID)
 	{
 		if(true){ //Kolla om ID finns
 			this.myBarID = myBarID;
 			return true;
 		}	
 		else return false; 
 	}
 	
 	public String[][] listMyBarIngredientCategories()
 	{
 		//data.listMyBarIngredientCategories(this.myBarID);						Add this method in data.
 		return null;
 	}
 }
