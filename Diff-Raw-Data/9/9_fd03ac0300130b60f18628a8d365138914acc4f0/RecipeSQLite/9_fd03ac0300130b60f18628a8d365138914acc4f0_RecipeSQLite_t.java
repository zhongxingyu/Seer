 package ca.dreamteam.newrecipebook.Helpers;
 
 import java.util.ArrayList;
 import java.util.List;
 import ca.dreamteam.newrecipebook.Models.Recipe;
 import ca.dreamteam.newrecipebook.Helpers.RecipeSqlTable;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.SQLException;
 import android.database.sqlite.SQLiteDatabase;
 
 public class RecipeSQLite{
     private SQLiteDatabase database;
     private RecipeSqlTable dbHelper;
     private String[] allColumns = { RecipeSqlTable.COLUMN_ID, RecipeSqlTable.COLUMN_NAME,
             RecipeSqlTable.COLUMN_AUTHOR, RecipeSqlTable.COLUMN_INGREDIENTS,
             RecipeSqlTable.COLUMN_INSTRUCTIONS, RecipeSqlTable.COLUMN_TAGS,
             RecipeSqlTable.COLUMN_COMMENTS, RecipeSqlTable.COLUMN_FAVORITED };
 
     public RecipeSQLite(Context context) {
         dbHelper = new RecipeSqlTable(context);
     }
 
     public void open() throws SQLException {
         database = dbHelper.getWritableDatabase();
     }
 
     public void close() {
         dbHelper.close();
     }
 
     private Recipe cursorToRecipe(Cursor cursor) {
         Recipe recipe = new Recipe();
         recipe.setId(cursor.getLong(0));
         recipe.setName(cursor.getString(1));
         return recipe;
     }
 
    public Recipe createRecipe(Recipe recipe){
         ContentValues values = new ContentValues();
        values.put(RecipeSqlTable.COLUMN_NAME, recipe.name);
        values.put(RecipeSqlTable.COLUMN_AUTHOR, recipe.author);
        values.put(RecipeSqlTable.COLUMN_INSTRUCTIONS, recipe.instructions);
 //        valuse.put(RecipeSqlTable.COLUMN_INGREDIENTS, ingredients);
 //        values.put(RecipeSqlTable.COLUMN_TAGS, tags);
         
         
         //under the assumption that i have to insert all of the columns here
         //make sure you check the spelling
 
         long insertId = database.insert(RecipeSqlTable.TABLE_RECIPES, null, values);
         Cursor cursor = database.query(RecipeSqlTable.TABLE_RECIPES,
                 allColumns, RecipeSqlTable.COLUMN_ID + " = " + insertId, null,
                 null, null, null);
         cursor.moveToFirst();
         Recipe newRecipe = cursorToRecipe(cursor);
         cursor.close();
         return newRecipe;
     }
 
     public void deleteRecipe(Recipe recipe) {
         long id = recipe.getId();
         System.out.println("Recipe deleted with id: " + id);
         database.delete(RecipeSqlTable.TABLE_RECIPES, RecipeSqlTable.COLUMN_ID
                 + " = " + id, null);
     }
 
     public List<Recipe> getAllRecipes(){
         List<Recipe> recipes = new ArrayList<Recipe>();
         Cursor cursor = database.query(RecipeSqlTable.TABLE_RECIPES,
                 allColumns, null, null, null, null, null);
         cursor.moveToFirst();
         while (!cursor.isAfterLast()) {
             Recipe recipe = cursorToRecipe(cursor);
             recipes.add(recipe);
             cursor.moveToNext();
         }
         // Make sure to close the cursor
         cursor.close();
         return recipes;
     }
 }
