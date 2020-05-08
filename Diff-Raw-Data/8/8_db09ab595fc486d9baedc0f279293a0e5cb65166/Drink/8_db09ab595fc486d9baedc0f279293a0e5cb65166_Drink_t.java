 package main.jadrin.ontology;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import jade.content.Concept;
 
 
 public class Drink implements Concept, Serializable{
     /**
      * 
      */
     private static final long serialVersionUID = 1L;
     private String name;
     private ArrayList<Ingredient> ingredients;
     private Recipe recipe;
 
     public String getName(){
         return name;
     }
 
     public void setName(String name){
         this.name = name;
     }
     
     public Recipe getRecipe(){
         return recipe;
     }
     
     public void setRecipe(Recipe recipe){
         this.recipe=recipe;
     }
     
     public void setIngredients(ArrayList<Ingredient> ingredients){
         this.ingredients = ingredients;
     }
     
     public ArrayList<Ingredient> getIngredients(){
         return ingredients;
     }
 
 //	public Drink(String name, ArrayList<Ingredient> ingredients, String recipe) {
 //		super();
 //		this.name = name;
 //		this.ingredients = ingredients;
 //		this.recipe = recipe;
 //	}
 	
 	public Drink(String name, String[] ingredients, String recipe) {
 		super();
 		this.name = name;
 //		this.ingredients = new ArrayList<Ingredient>(Arrays.asList(ingredients));
 		this.recipe = new Recipe();
 		this.recipe.setContent(recipe);
 		this.ingredients = new ArrayList<Ingredient>(ingredients.length);
 		for (int i=0; i< ingredients.length; i++){
 			Ingredient ingredient = new  Ingredient();
 			ingredient.setName(ingredients[i]);
 			this.ingredients.add(ingredient);
 		}
 	}
 	
 	public Drink(){
 		super();
 	}
 	
 
 	 public void writeFacts(java.io.Writer out){
 		String nameStr = serializeName();
 		String recipeStr = serializeRecipe();
 		String ingredientsStr = serializeIngredients();
 		// out.write(arg0);
 	}
 
 	private String serializeName() {
 		String[] words = this.name.split("\\s+");
 		StringBuilder builder = new StringBuilder();
 		builder.append("[");
 		
 		for (String str : words) {
 			builder.append("['");
 			builder.append(str);
 			builder.append("']");
 		}
 		
 		builder.append("]");
 		return builder.toString();
 	}
	
//	private String serializeSimpleString(String str)
 
 	private String serializeRecipe() {
 		return "['" + this.recipe.getContent() + "']";
 	}
 
 	private String serializeIngredients() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 
 }
