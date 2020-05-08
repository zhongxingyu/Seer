 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.  Add a copy constructor for the scale.
  */
 package surlaburn;
 
 import java.util.ArrayList;
 import java.util.Scanner;
 
 /**
  *
  * @author Tom
  */
 public class Recipe {
 
   //
   // Fields
   //
 
   private String description;
   private String title;
   private ArrayList<Ingredient> listOfIngredients = new ArrayList<>();
   private ArrayList<Step> listOfSteps = new ArrayList<>();
   private int numOfPeople;
  private int numOfPeopleStart;
   private ArrayList<Ingredient> newList = new ArrayList<>();
   
   //
   // Constructors
   //
   public Recipe (Scanner inputFile) 
   {
       String garbage = inputFile.nextLine();
       title = garbage.trim();
       
             while(!inputFile.hasNext(":END-OF-RECIPE:"))
             {
                 garbage = inputFile.next();
                 
           switch (garbage) {
               case ":Recipe-description:":
                   description = inputFile.nextLine().trim();
                   //garbage = inputFile.next();
                   break;
               case ":Ingredient-name:":
                   Ingredient i = new Ingredient(inputFile);
                   listOfIngredients.add(i);
                   //when an ingredient is read in, it eats the blank line
                   String removeGap = inputFile.nextLine();
                   //garbage = inputFile.next();
                   break;
               case ":Step:":
                   Step s = new Step(inputFile);
                   listOfSteps.add(s);
                   //System.out.println("This is the list of steps");
                   //garbage = inputFile.next();
                   break;
               case ":Recipe-serves:":
                  numOfPeopleStart = inputFile.nextInt();
                   break;
           }
   }
   } 
   
   public Recipe(String t, String d, ArrayList<Ingredient> listOfI, 
           ArrayList<Step> listOfS, int x)
   {  
      this.description = d;
      this.title = t;
      this.listOfSteps.addAll(listOfS);
      this.setNumOfPeople(x);
      this.listOfIngredients.addAll(listOfI);
      
      
   }
   
 
   /**
    * Set the value of description
    * @param newVar the new value of description
    */
   public void setDescription ( String newVar ) {
     description = newVar;
   }
 
   /**
    * Get the value of description
    * @return the value of description
    */
   public String getDescription ( ) {
     return description;
   }
 
   /**
    * Set the value of title
    * @param newVar the new value of title
    */
   public void setTitle ( String newVar ) {
     title = newVar;
   }
 
   /**
    * Get the value of title
    * @return the value of title
    */
   public String getTitle ( ) {
     return title;
   }
 
   /**
    * Set the value of listOfIingredients
    * @param newVar the new value of listOfIingredients
    */
   public void setListOfIngredients ( ArrayList<Ingredient> newVar ) {
     listOfIngredients = newVar;
   }
 
   /**
    * Get the value of listOfIingredients
    * @return the value of listOfIingredients
    */
   public ArrayList<Ingredient> getListOfIngredients ( ) {
     return listOfIngredients;
   }
 
   /**
    * Set the value of listOfSteps
    * @param newVar the new value of listOfSteps
    */
   public void setListOfSteps ( ArrayList<Step> newVar ) {
     listOfSteps.clear();
     for(int i = 0; i < newVar.size(); i++)
     {
         listOfSteps.add(newVar.get(i));
     }
   }
 
   /**
    * Get the value of listOfSteps
    * @return the value of listOfSteps
    */
   public ArrayList<Step> getListOfSteps ( ) {
     return listOfSteps;
   }
 
   /**
    * Set the value of numOfPeople
    * @param newVar the new value of numOfPeople
    */
   public void setNumOfPeople ( int newVar ) {
     this.numOfPeople = newVar;
   }
 
   /**
    * Get the value of numOfPeople
    * @return the value of numOfPeople
    */
   public int getNumOfPeople ( ) {
     return this.numOfPeople;
   }
 
   //
   // Other methods
   //
 
   /**
    * Sets the number of people to cook for
    * @param        count
    */
   
   
   public ArrayList<Ingredient> howMany(int x ) {
     
       setNumOfPeople(x);
     for(int i = 0; i<this.listOfIngredients.size(); i++)
     {
          this.newList.add(this.listOfIngredients.get(i).scale(this.getNumOfPeople()));
     }
     this.listOfIngredients.clear();
     this.listOfIngredients.addAll(0, newList);
     this.newList.clear();
     return this.listOfIngredients;
   }
   
 
     @Override
     public String toString()
 {
     return "Title: " + title + "\nDescription: " + description +  
             "\n\tIngredients: " + this.listOfIngredients.toString() + "\n\tSteps: " 
             + listOfSteps.toString();
 }
     public Recipe copy(int x)
     {
         Recipe newRecp = new Recipe(this.getTitle(),this.getDescription(),
                 this.getListOfIngredients(),this.getListOfSteps(),x);
         newRecp.setListOfIngredients(howMany(x));
         return newRecp;
     }
 }
