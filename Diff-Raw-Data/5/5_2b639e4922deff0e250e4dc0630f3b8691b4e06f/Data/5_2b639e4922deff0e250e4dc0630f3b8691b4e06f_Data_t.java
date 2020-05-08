 /*
 New BSD License
 Copyright (c) 2012, MyBar Team All rights reserved.
 mybar@turbotorsk.se
 Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 �	Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 �	Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 �	Neither the name of the MyBar nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 /**
  * 
  * 
  * @author Dag Friden, Mathias Karlgren (matkarlg)
  * 
  */
 
 package se.turbotorsk.mybar.model;
 
 import java.util.LinkedList;
 
 import se.turbotorsk.mybar.controller.Controller;
 import se.turbotorsk.mybar.controller.MyBarApplication;
 import se.turbotorsk.mybar.model.database.DrinkTable;
 import se.turbotorsk.mybar.model.database.IngredientTable;
 import se.turbotorsk.mybar.model.database.MyBarContentProvider;
 import se.turbotorsk.mybar.model.database.MyBarTable;
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.net.Uri;
 import android.util.Log;
 
 public class Data {
 
 	private static final boolean SQLITE = true;
 	private static final boolean EDATA = false;
 	private static final boolean FAKE = false;
 	private static Drink exampleDrink1 = null, exampleDrink2 = null;
 	private static LinkedList<Ingredient> fakeIngredientList = null;
 	private static LinkedList<Drink> fakeDrinkList = null;
 
 	// private Ingredient fakeIngredient1 = null, fakeIngredient2 = null,
 	// fakeIngredient3 = null;
 	// private XXXXXXXX sqlite;
 	// private XXXXXXXX jsonParser;
 	// private XXXXXXXX httpGet;
 
 	/**
 	 * Inserts TestData.
 	 * 
 	 * @return 0.
 	 */
 	public static int insertTestData() {
 		if (SQLITE) {
 			Uri myBarUri = null;
 
 			// Drinks uses autoincrement in the _id field.
 			Drink[] testDrinks = {
 					new Drink(0, "Margarita", "http://repro.mybar.turbotorsk.se/img/no_img.png",
 							"Martini Glass", "ingredients here", "Margarita instructions", 5, 0),
 					new Drink(0, "Tequila", "http://repro.mybar.turbotorsk.se/img/no_img.png",
 							"Shot Glass", "ingredients here", "Pour Tequila in shot glass", 5, 0),
 					new Drink(0, "Cosmopolitan", "http://repro.mybar.turbotorsk.se/img/no_img.png",
 							"Martini Glass", "ingredients here", "Cosmopolitan instructions", 5, 0),
 					new Drink(0, "Cuba Libre", "http://repro.mybar.turbotorsk.se/img/no_img.png",
 							"Highball Glass", "ingredients here", "Cuba Libre instructions", 5, 0),
 					new Drink(0, "Martini", "http://repro.mybar.turbotorsk.se/img/no_img.png",
 							"Martini Glass", "ingredients here", "Pour Martini in glass", 5, 0),
 					new Drink(0, "Irish Coffee", "http://repro.mybar.turbotorsk.se/img/no_img.png",
 							"Coffee Glass", "ingredients here", "Irish Coffee instructions", 5, 0) };
 
 			// Insert testDrinks.
 			for (Drink testDrink : testDrinks) {
 				ContentValues values = testDrink.getContentValues();
 				myBarUri = MyBarApplication.ContentResolver().insert(
 						MyBarContentProvider.CONTENTURI_DRINK, values);
 				Log.d(Data.class.getClass().getName(),
 						"Inserted Drink. Created row: " + myBarUri.toString());
 			}
 
 			// No autoincrement in Ingredients. Set the _id field manually.
 			Ingredient[] testIngredients = {
 					new Ingredient(1, "Koskenkorva Vodka", "http://repro.mybar.turbotorsk.se/img/no_img.png", "Vodka", 40,
 							"<insert description>"),
 					new Ingredient(2, "Baileys", "http://repro.mybar.turbotorsk.se/img/no_img.png", "Liqueur", 20,
 							"<insert description>") };
 
 			// Insert testIngredients.
 			for (Ingredient testIngredient : testIngredients) {
 				ContentValues values = testIngredient.getContentValues();
 				myBarUri = MyBarApplication.ContentResolver().insert(
 						MyBarContentProvider.CONTENTURI_INGREDIENT, values);
 				Log.d(Data.class.getClass().getName(), "Inserted Ingredient. Created row: "
 						+ myBarUri.toString());
 			}
 		}
 		if (EDATA) {}
 		if (FAKE) { // Creates "fake" data to be used when FAKE is true (for
 					// testing purpose).
 			fakeDrinkList = new LinkedList<Drink>();
 			fakeDrinkList.add(exampleDrink1 = new Drink(1, "1;2;2;1", "http://www.google.se",
 					"test1", "ingredient1", "description1", 3, 0));
 			fakeDrinkList.add(exampleDrink2 = new Drink(2, "1;3;3;1", "http://www.google.se",
 					"test2", "ingredient2", "description2", 2, 0));
 			// fakeIngredientList= new LinkedList<Ingredient>();
 			// fakeIngredientList.add(fakeIngredient1 = new Ingredient(1,
 			// "Vodka", "http://www.google.se", 2, "Nice Vodka!"));
 			// fakeIngredientList.add(fakeIngredient2 = new Ingredient(2,
 			// "Dark Rom", "http://www.google.se", 2, "Nice Rom!"));
 			// fakeIngredientList.add(fakeIngredient3 = new Ingredient(3,
 			// "Lime", "http://www.google.se", 2, "Nice Lime!"));
 		}
 		return 0;
 	}
 
 	/**
 	 * Deleted testData.
 	 * 
 	 * @return rowsDeleted.
 	 */
 	public static int deleteTestData() {
 		int rowsDeleted = 0;
 		if (SQLITE) {
 			rowsDeleted += MyBarApplication.ContentResolver().delete(
 					MyBarContentProvider.CONTENTURI_DRINK, null, null);
 			rowsDeleted += MyBarApplication.ContentResolver().delete(
 					MyBarContentProvider.CONTENTURI_INGREDIENT, null, null);
 			rowsDeleted += MyBarApplication.ContentResolver().delete(
 					MyBarContentProvider.CONTENTURI_MYBAR, null, null);
 		}
 		return rowsDeleted;
 	}
 	
 	/**
 	 * Adds a new ingredient to MyBarTable
 	 * 
 	 * @param ingredientID ID of the ingredient.
 	 * @param location "Home", "Work".
 	 * @return 0 if successful.
 	 */
 	public static int addMyBar(int ingredientID, String location) {
 		if (SQLITE) {
 
 			/**
 			 * SQLITE addMyBar(int ID, String location).
 			 */
 			ContentValues values = new ContentValues();
 			values.put("ingredientid", ingredientID);
 			values.put("location", location);
 			MyBarApplication.ContentResolver().insert(
 					MyBarContentProvider.CONTENTURI_MYBAR, values);
 
 			// Print the added drink.
 			Log.d(Data.class.getClass().getName(), "Added ID to " +
 					"MyBarTable: " + values.get("ingredientid"));
 
 			return 0;
 			/**
 			 * End of SQLite addMyBar(int ID, String location).
 			 */
 		}
 		if (EDATA) {}
 		if (FAKE) {}
 
 		return 1;
 	}
 
 	/**
 	 * Removes a row in MyBarTable
 	 * 
 	 * @param ingredientID the ingredients ID field
 	 * @param location Example: "Home", "Work".
 	 * @return 0 if successful. 0 if error. See LogCat.
 	 */
 	public static int dropMyBar(int ingredientID, String location) {
 		if (SQLITE) {
 
 			/**
 			 * SQLITE dropMyBar(int ingredientID, String location).
 			 */
 			// Choose which columns you want to query. null queries all columns.
 //			String[] projection = { MyBarTable.COLUMN_ID, MyBarTable.COLUMN_INGREDIENTID };
 
 			// Query database.
 			Cursor cursor = MyBarApplication.ContentResolver().query(
 					MyBarContentProvider.CONTENTURI_MYBAR, null,
 					MyBarTable.COLUMN_INGREDIENTID
 					+ " = ? AND " + MyBarTable.COLUMN_LOCATION + " = ? ", 
 					new String[]{Integer.toString(ingredientID), location}, null);
 
 			// Successful query?.
 			if (cursor != null) {
 
 				// Is there any data from the requested Query.
 				if (cursor.moveToFirst()) {
 
 					MyBarApplication.ContentResolver().delete(
 							MyBarContentProvider.CONTENTURI_MYBAR, MyBarTable.COLUMN_INGREDIENTID
 							+ " = ? AND " + MyBarTable.COLUMN_LOCATION + " = ? ", 
 							new String[]{Integer.toString(ingredientID), location});
 
 					// Print the removed ingredient.
 					Log.d(Data.class.getClass().getName(),
 							"Removed ID from MyBarTable: "
 									+ cursor.getInt(cursor
 											.getColumnIndexOrThrow(MyBarTable.COLUMN_ID))
 									+ " "
 									+ cursor.getString(cursor
 											.getColumnIndexOrThrow(MyBarTable.COLUMN_INGREDIENTID))
 									+ " "
 									+ cursor.getString(cursor
 											.getColumnIndexOrThrow(MyBarTable.COLUMN_LOCATION)));
 
 					// Close the cursor.
 					cursor.close();
 
 					return 0;
 
 				} else {
 					// Error message in LogCat.
 					Log.e(Data.class.getClass().getName(), "dropMyBar(): " 
 					+ ingredientID + " " + location + " doesn't exist");
 
 					// Close the cursor.
 					cursor.close();
 
 					// ingredient doesn't exist. Return 1.
 					return 1;
 				}
 			}
 			/**
 			 * End of SQLite dropMyBar(int ingredientID, String location).
 			 */
 		}
 		if (EDATA) {}
 		if (FAKE) {}
 
 		return 1;
 	}
 
 	/**
 	 * Inserts a new Drink in the DrinkTable. Trying to insert another Drink
 	 * with the same name yields an error message.
 	 * 
 	 * @param name a Drink object that should be inserted into the database.
 	 * @return 0 if successful, 1 if error. See LogCat.
 	 */
 	public static int addDrink(Drink name) {
 		if (SQLITE) {
 
 			/**
 			 * SQLITE addDrink(Drink name).
 			 */
 			ContentValues values = name.getContentValues();
 
 			// Choose which columns you want to query. null queries all columns.
 			String[] projection = { DrinkTable.COLUMN_ID, DrinkTable.COLUMN_NAME };
 
 			// Query database.
 			Cursor cursor = MyBarApplication.ContentResolver().query(
 					MyBarContentProvider.CONTENTURI_DRINK, projection,
 					DrinkTable.COLUMN_NAME + " = ? ", new String[] { name.getName() }, null);
 
 			// Successful query?.
 			if (cursor != null) {
 
 				// Is there any data from the requested Query.
 				if (cursor.moveToFirst()) {
 
 					// Error message in LogCat.
 					Log.e(Data.class.getClass().getName(), "addDrink(): " + name.getName()
 							+ " already exists");
 
 					// Close the cursor.
 					cursor.close();
 
 					// name already exists. Return 1.
 					return 1;
 
 				} else {
 					MyBarApplication.ContentResolver().insert(
 							MyBarContentProvider.CONTENTURI_DRINK, values);
 
 					// Print the added drink.
 					Log.d(Data.class.getClass().getName(), "Added Drink: " + name.getName());
 
 					// Close the cursor.
 					cursor.close();
 
 					return 0;
 				}
 			}
 			/**
 			 * End of SQLite addDrink(Drink name).
 			 */
 		}
 		if (EDATA) {}
 		if (FAKE) {}
 
 		return 1;
 	}
 
 	/**
 	 * Removes a Drink in the DrinkTable.
 	 * 
 	 * @param ID an int _id that should be removed from the database.
 	 * @return 0 if successful, 1 if error. See LogCat.
 	 */
 	public static int dropDrink(int ID) {
 		if (SQLITE) {
 
 			/**
 			 * SQLITE dropDrink(Drink name).
 			 */
 			ContentValues values = new ContentValues();
 
 			// Choose which columns you want to query. null queries all columns.
 			String[] projection = { DrinkTable.COLUMN_ID, DrinkTable.COLUMN_NAME };
 
 			// Query database.
 			Cursor cursor = MyBarApplication.ContentResolver().query(
 					MyBarContentProvider.CONTENTURI_DRINK, projection,
 					DrinkTable.COLUMN_ID + "=" + ID, null, null);
 
 			// Successful query?.
 			if (cursor != null) {
 
 				// Is there any data from the requested Query.
 				if (cursor.moveToFirst()) {
 
 					MyBarApplication.ContentResolver().delete(
 							MyBarContentProvider.CONTENTURI_DRINK, DrinkTable.COLUMN_ID + "=" + ID,
 							null);
 
 					// Print the removed drink.
 					Log.d(Data.class.getClass().getName(),
 							"Removed Drink: "
 									+ cursor.getInt(cursor
 											.getColumnIndexOrThrow(DrinkTable.COLUMN_ID))
 									+ " "
 									+ cursor.getString(cursor
 											.getColumnIndexOrThrow(DrinkTable.COLUMN_NAME)));
 
 					// Close the cursor.
 					cursor.close();
 
 					return 0;
 
 				} else {
 					// Error message in LogCat.
 					Log.e(Data.class.getClass().getName(), "dropDrink(): " + ID + " doesn't exist");
 
 					// Close the cursor.
 					cursor.close();
 
 					// name doesn't exist. Return 1.
 					return 1;
 				}
 			}
 			/**
 			 * End of SQLite dropDrink(Drink name).
 			 */
 		}
 		if (EDATA) {}
 		if (FAKE) {}
 
 		return 1;
 	}
 	
 	/**
 	 * This method returns everything in the MyBarTable.
 	 * 
 	 * @return MyBar object.
 	 */
 	public static LinkedList<MyBar> getAllMyBar() {
 		if (SQLITE) {
 
 			/**
 			 * SQLITE getAllMyBar().
 			 */
 			LinkedList<MyBar> mybarList = new LinkedList<MyBar>();
 
 			// Choose which columns you want to query. null queries all columns.
 			// String[] projection = { DrinkTable.COLUMN_NAME,
 			// DrinkTable.COLUMN_DESCRIPTION, DrinkTable.COLUMN_RATING };
 
 			// Query database.
 			Cursor cursor = MyBarApplication.ContentResolver().query(
 					MyBarContentProvider.CONTENTURI_MYBAR, null, null, null, null);
 
 			// Successful query?.
 			if (cursor != null) {
 
 				// Is there any data from the requested Query.
 				if (cursor.moveToFirst()) {
 
 					do {
 						int _id = cursor.getInt(cursor.getColumnIndexOrThrow(MyBarTable.COLUMN_ID));
 						int ingredientid = cursor.getInt(cursor
 								.getColumnIndexOrThrow(MyBarTable.COLUMN_INGREDIENTID));
 						String location = cursor.getString(cursor
 								.getColumnIndexOrThrow(MyBarTable.COLUMN_LOCATION));
 						mybarList.add(new MyBar(_id, ingredientid, location));
 					} while (cursor.moveToNext());
 
 					// Close the cursor.
 					cursor.close();
 
 					return mybarList;
 				} else {
 					// Close the cursor.
 					cursor.close();
 
 					// No mybar in Query. Return Empty LinkedList<MyBar>.
 					return new LinkedList<MyBar>();
 				}
 			}
 			/**
 			 * End of SQLite getAllMyBar().
 			 */
 		}
 		if (EDATA) {}
 		if (FAKE) {}
 		return null;
 	}
 
 	/**
 	 * This method return all Drinks from the local SQLite database.
 	 * 
 	 * @return Drink object.
 	 */
 	public static LinkedList<Drink> getAllDrinks() {
 		if (SQLITE) {
 
 			/**
 			 * SQLITE getAllDrinks().
 			 */
 			LinkedList<Drink> drinkList = new LinkedList<Drink>();
 
 			// Choose which columns you want to query. null queries all columns.
 			// String[] projection = { DrinkTable.COLUMN_NAME,
 			// DrinkTable.COLUMN_DESCRIPTION, DrinkTable.COLUMN_RATING };
 
 			// Query database.
 			Cursor cursor = MyBarApplication.ContentResolver().query(
 					MyBarContentProvider.CONTENTURI_DRINK, null, null, null, null);
 
 			// Successful query?.
 			if (cursor != null) {
 
 				// Is there any data from the requested Query.
 				if (cursor.moveToFirst()) {
 
 					do {
 						int _id = cursor.getInt(cursor.getColumnIndexOrThrow(DrinkTable.COLUMN_ID));
 						String name = cursor.getString(cursor
 								.getColumnIndexOrThrow(DrinkTable.COLUMN_NAME));
 						String url = cursor.getString(cursor
 								.getColumnIndexOrThrow(DrinkTable.COLUMN_URL));
 						String glass = cursor.getString(cursor
 								.getColumnIndexOrThrow(DrinkTable.COLUMN_GLASS));
 						String ingredient = cursor.getString(cursor
 								.getColumnIndexOrThrow(DrinkTable.COLUMN_INGREDIENT));
 						String description = cursor.getString(cursor
 								.getColumnIndexOrThrow(DrinkTable.COLUMN_DESCRIPTION));
 						int rating = cursor.getInt(cursor
 								.getColumnIndexOrThrow(DrinkTable.COLUMN_RATING));
 						int favorite = cursor.getInt(cursor
 								.getColumnIndexOrThrow(DrinkTable.COLUMN_FAVORITE));
 						drinkList.add(new Drink(_id, name, url, glass, ingredient, description,
 								rating, favorite));
 					} while (cursor.moveToNext());
 
 					// Close the cursor.
 					cursor.close();
 
 					return drinkList;
 				} else {
 					// Close the cursor.
 					cursor.close();
 
 					// No drinks in Query. Return Empty LinkedList<Drink>.
 					return new LinkedList<Drink>();
 				}
 			}
 			/**
 			 * End of SQLite getAllDrinks().
 			 */
 		}
 		if (EDATA) {}
 		if (FAKE) {
 			return fakeDrinkList;
 		}
 		return null;
 	}
 
 	/**
 	 * This method return all Ingredients from the local SQLite database.
 	 * 
 	 * @return Ingredient object.
 	 */
 	public static LinkedList<Ingredient> getAllIngredients() {
 		if (SQLITE) {
 
 			/**
 			 * SQLITE getAllIngredients().
 			 */
 			LinkedList<Ingredient> ingredientList = new LinkedList<Ingredient>();
 
 			// Choose which columns you want to query. null queries all columns.
 			// String[] projection = { IngredientTable.COLUMN_NAME,
 			// IngredientTable.COLUMN_DESCRIPTION };
 
 			// Query database.
 			Cursor cursor = MyBarApplication.ContentResolver().query(
 					MyBarContentProvider.CONTENTURI_INGREDIENT, null, null, null, null);
 
 			// Successful query?.
 			if (cursor != null) {
 
 				// Is there any data from the requested Query.
 				if (cursor.moveToFirst()) {
 
 					do {
 						int _id = cursor.getInt(cursor
 								.getColumnIndexOrThrow(IngredientTable.COLUMN_ID));
 						String name = cursor.getString(cursor
 								.getColumnIndexOrThrow(IngredientTable.COLUMN_NAME));
 						String url = cursor.getString(cursor
 								.getColumnIndexOrThrow(IngredientTable.COLUMN_URL));
 						String type = cursor.getString(cursor
 								.getColumnIndexOrThrow(IngredientTable.COLUMN_TYPE));
 						int alcoholcontent = cursor.getInt(cursor
 								.getColumnIndexOrThrow(IngredientTable.COLUMN_ALCOHOLCONTENT));
 						String description = cursor.getString(cursor
 								.getColumnIndexOrThrow(IngredientTable.COLUMN_DESCRIPTION));
 						ingredientList.add(new Ingredient(_id, name, url, type, alcoholcontent,
 								description));
 					} while (cursor.moveToNext());
 
 					// Close the cursor.
 					cursor.close();
 
 					return ingredientList;
 				} else {
 					// Close the cursor.
 					cursor.close();
 
 					// No ingredients in Query. Return Empty LinkedList<Ingredient>.
 					return new LinkedList<Ingredient>();
 				}
 			}
 			/**
 			 * End of SQLite getAllIngredients().
 			 */
 		}
 		if (EDATA) {}
 		if (FAKE) {}
 		return null;
 	}
 
 	/**
 	 * This method return a Drink (thumbnail, name, rating, description) SQL.
 	 * Query: SELECT * WHERE _id = id.
 	 * 
 	 * @param ID
 	 * @return
 	 */
 	public static Drink getDrinkByID(int ID) {
 		if (SQLITE) {
 
 			/**
 			 * SQLITE getDrinkByID().
 			 */
 			// Choose which columns you want to query. null queries all columns.
 			// String[] projection = { DrinkTable.COLUMN_NAME,
 			// DrinkTable.COLUMN_DESCRIPTION, DrinkTable.COLUMN_RATING };
 
 			// Query database.
 			Cursor cursor = MyBarApplication.ContentResolver().query(
 					MyBarContentProvider.CONTENTURI_DRINK, null, DrinkTable.COLUMN_ID + "=" + ID,
 					null, null);
 
 			// Successful query?.
 			if (cursor != null) {
 
 				// Is there any data from the requested Query.
 				if (cursor.moveToFirst()) {
 
 					// Test Print
 					// Log.d(this.getClass().getName(),
 					// cursor.getString(cursor.getColumnIndexOrThrow(DrinkTable.COLUMN_NAME)));
 					// Log.d(this.getClass().getName(),
 					// cursor.getString(cursor.getColumnIndexOrThrow(DrinkTable.COLUMN_DESCRIPTION)));
 					// Log.d(this.getClass().getName(),
 					// cursor.getString(cursor.getColumnIndexOrThrow(DrinkTable.COLUMN_RATING)));
 
 					int _id = cursor.getInt(cursor.getColumnIndexOrThrow(DrinkTable.COLUMN_ID));
 					String name = cursor.getString(cursor
 							.getColumnIndexOrThrow(DrinkTable.COLUMN_NAME));
 					String url = cursor.getString(cursor
 							.getColumnIndexOrThrow(DrinkTable.COLUMN_URL));
 					String glass = cursor.getString(cursor
 							.getColumnIndexOrThrow(DrinkTable.COLUMN_GLASS));
 					String ingredient = cursor.getString(cursor
 							.getColumnIndexOrThrow(DrinkTable.COLUMN_INGREDIENT));
 					String description = cursor.getString(cursor
 							.getColumnIndexOrThrow(DrinkTable.COLUMN_DESCRIPTION));
 					int rating = cursor.getInt(cursor
 							.getColumnIndexOrThrow(DrinkTable.COLUMN_RATING));
 					int favorite = cursor.getInt(cursor
 							.getColumnIndexOrThrow(DrinkTable.COLUMN_FAVORITE));
 					Drink drink = new Drink(_id, name, url, glass, ingredient, description, rating,
 							favorite);
 
 					// Close the cursor.
 					cursor.close();
 
 					return drink;
 				} else {
 					// Error message in LogCat.
 					Log.e(Data.class.getClass().getName(), "ID doesn't exist");
 
 					// ID doesn't exist. Return Drink with ID = 0.
 					Drink drink = new Drink(0, "", "", "", "", "", 0, 0);
 
 					// Close the cursor.
 					cursor.close();
 
 					return drink;
 				}
 			}
 			/**
 			 * End of SQLite getDrinkByID().
 			 */
 		}
 		if (EDATA) {}
 		if (FAKE) {
 			return exampleDrink1;
 		}
 
 		return null;
 	}
 
 	/**
 	 * This method return a Ingredient SQL Query: SELECT * WHERE _id = id.
 	 * 
 	 * @param ID
 	 * @return
 	 */
 	public static Ingredient getIngredientByID(int ID) {
 		if (SQLITE) {
 
 			/**
 			 * SQLITE getIngredientByID()
 			 */
 			// Choose which columns you want to query. null queries all columns.
 			// String[] projection = { IngredientTable.COLUMN_NAME,
 			// IngredientTable.COLUMN_DESCRIPTION };
 
 			// Query database.
 			Cursor cursor = MyBarApplication.ContentResolver().query(
 					MyBarContentProvider.CONTENTURI_INGREDIENT, null,
 					IngredientTable.COLUMN_ID + "=" + ID, null, null);
 
 			// Successful query?.
 			if (cursor != null) {
 
 				// Is there any data from the requested Query.
 				if (cursor.moveToFirst()) {
 
 					// Test Print.
 					// Log.d(this.getClass().getName(),
 					// cursor.getString(cursor.getColumnIndexOrThrow(DrinkTable.COLUMN_NAME)));
 
 					int _id = cursor
 							.getInt(cursor.getColumnIndexOrThrow(IngredientTable.COLUMN_ID));
 					String name = cursor.getString(cursor
 							.getColumnIndexOrThrow(IngredientTable.COLUMN_NAME));
 					String url = cursor.getString(cursor
 							.getColumnIndexOrThrow(IngredientTable.COLUMN_URL));
 					String type = cursor.getString(cursor
 							.getColumnIndexOrThrow(IngredientTable.COLUMN_TYPE));
 					int alcoholcontent = cursor.getInt(cursor
 							.getColumnIndexOrThrow(IngredientTable.COLUMN_ALCOHOLCONTENT));
 					String description = cursor.getString(cursor
 							.getColumnIndexOrThrow(IngredientTable.COLUMN_DESCRIPTION));
 					Ingredient ingredient = new Ingredient(_id, name, url, type, alcoholcontent,
 							description);
 
 					// Close the cursor.
 					cursor.close();
 
 					return ingredient;
 				} else {
 					// Error message in LogCat.
 					Log.e(Data.class.getClass().getName(), "ID doesn't exist");
 
 					// ID doesn't exist. Return Ingredient with ID = 0.
 					Ingredient ingredient = new Ingredient(0, "", "", "", 0, "");
 
 					// Close the cursor.
 					cursor.close();
 
 					return ingredient;
 				}
 			}
 			/**
 			 * End of SQLite getIngredientByID().
 			 */
 		}
 		if (EDATA) {}
 		if (FAKE) {}
 
 		return null;
 	}
 
 	/**
 	 * This method return all Drinks with Favorite column set, from the local
 	 * SQLite database.
 	 * 
 	 * @return Drink object.
 	 */
 	public static LinkedList<Drink> getAllFavorites() {
 		if (SQLITE) {
 
 			/**
 			 * SQLITE getAllFavorites().
 			 */
 			LinkedList<Drink> drinkList = new LinkedList<Drink>();
 
 			// Choose which columns you want to query. null queries all columns.
 			// String[] projection = { DrinkTable.COLUMN_NAME,
 			// DrinkTable.COLUMN_DESCRIPTION, DrinkTable.COLUMN_RATING };
 
 			// Query database.
 			Cursor cursor = MyBarApplication.ContentResolver().query(
 					MyBarContentProvider.CONTENTURI_DRINK, null, "favorite=1", null, null);
 
 			// Successful query?.
 			if (cursor != null) {
 
 				// Is there any data from the requested Query.
 				if (cursor.moveToFirst()) {
 
 					do {
 						int _id = cursor.getInt(cursor.getColumnIndexOrThrow(DrinkTable.COLUMN_ID));
 						String name = cursor.getString(cursor
 								.getColumnIndexOrThrow(DrinkTable.COLUMN_NAME));
 						String url = cursor.getString(cursor
 								.getColumnIndexOrThrow(DrinkTable.COLUMN_URL));
 						String glass = cursor.getString(cursor
 								.getColumnIndexOrThrow(DrinkTable.COLUMN_GLASS));
 						String ingredient = cursor.getString(cursor
 								.getColumnIndexOrThrow(DrinkTable.COLUMN_INGREDIENT));
 						String description = cursor.getString(cursor
 								.getColumnIndexOrThrow(DrinkTable.COLUMN_DESCRIPTION));
 						int rating = cursor.getInt(cursor
 								.getColumnIndexOrThrow(DrinkTable.COLUMN_RATING));
 						int favorite = cursor.getInt(cursor
 								.getColumnIndexOrThrow(DrinkTable.COLUMN_FAVORITE));
 						drinkList.add(new Drink(_id, name, url, glass, ingredient, description,
 								rating, favorite));
 					} while (cursor.moveToNext());
 
 					// Close the cursor.
 					cursor.close();
 
 					return drinkList;
 				} else {
 					// Close the cursor.
 					cursor.close();
 
 					// No Favorites. Return Empty LinkedList<Drink>.
 					return new LinkedList<Drink>();
 				}
 			}
 			/**
 			 * End of SQLite getAllFavorites().
 			 */
 		}
 		if (EDATA) {}
 		if (FAKE) {}
 		return null;
 	}
 
 	/**
 	 * This method sets a Drink to a favorite.
 	 * 
 	 * @param ID
 	 * @return 0 if OK. 1 if ID doesn't exist.
 	 */
 	public static int setFavoriteByID(int ID) {
 		if (SQLITE) {
 
 			/**
 			 * SQLITE setFavoriteByID().
 			 */
 			ContentValues values = new ContentValues();
 
 			// Choose which columns you want to query. null queries all columns.
 			String[] projection = { DrinkTable.COLUMN_ID, DrinkTable.COLUMN_NAME };
 
 			// Query database.
 			Cursor cursor = MyBarApplication.ContentResolver().query(
 					MyBarContentProvider.CONTENTURI_DRINK, projection,
 					DrinkTable.COLUMN_ID + "=" + ID, null, null);
 
 			// Successful query?.
 			if (cursor != null) {
 
 				// Is there any data from the requested Query.
 				if (cursor.moveToFirst()) {
 
 					values.put("favorite", 1);
 					int rowUpdated = MyBarApplication.ContentResolver().update(
 							MyBarContentProvider.CONTENTURI_DRINK, values,
 							DrinkTable.COLUMN_ID + "=" + ID, null);
 
 					// Print the favorited drink.
 					Log.d(Data.class.getClass().getName(),
 							"Favorited Drink: "
 									+ cursor.getString(cursor
 											.getColumnIndexOrThrow(DrinkTable.COLUMN_NAME))
 									+ " How many rows favorited: " + Integer.toString(rowUpdated));
 
 					// Close the cursor.
 					cursor.close();
 
 					return 0;
 				} else {
 					// Close the cursor.
 					cursor.close();
 
 					// ID doesn't exist. Return 1.
 					return 1;
 				}
 			}
 			/**
 			 * End of SQLite setFavoriteByID().
 			 */
 		}
 		if (EDATA) {}
 		if (FAKE) {}
 
 		return 1;
 	}
 
 	/**
 	 * This method sets a Drink to a favorite.
 	 * 
 	 * @param name
 	 * @return 0 if OK. 1 if name doesn't exist.
 	 */
 	public static int setFavoriteByName(String name) {
 		if (SQLITE) {
 
 			/**
 			 * SQLITE setFavoriteByName().
 			 */
 			ContentValues values = new ContentValues();
 
 			// Choose which columns you want to query. null queries all columns.
 			String[] projection = { DrinkTable.COLUMN_ID, DrinkTable.COLUMN_NAME };
 
 			// Query database.
 			Cursor cursor = MyBarApplication.ContentResolver().query(
 					MyBarContentProvider.CONTENTURI_DRINK, projection,
 					DrinkTable.COLUMN_NAME + " = ? ", new String[] { name }, null);
 
 			// Successful query?.
 			if (cursor != null) {
 
 				// Is there any data from the requested Query.
 				if (cursor.moveToFirst()) {
 
 					values.put("favorite", 1);
 					int rowUpdated = MyBarApplication.ContentResolver().update(
 							MyBarContentProvider.CONTENTURI_DRINK, values,
 							DrinkTable.COLUMN_NAME + " = ? ", new String[] { name });
 
 					// Print the favorited drink.
 					Log.d(Data.class.getClass().getName(),
 							"Favorited Drink: "
 									+ cursor.getString(cursor
 											.getColumnIndexOrThrow(DrinkTable.COLUMN_NAME))
 									+ " How many rows favorited: " + Integer.toString(rowUpdated));
 
 					// Close the cursor.
 					cursor.close();
 
 					return 0;
 				} else {
 					// Close the cursor.
 					cursor.close();
 
 					// name doesn't exist. Return 1.
 					return 1;
 				}
 			}
 			/**
 			 * End of SQLite setFavoriteByName().
			 */
 		}
 		if (EDATA) {}
 		if (FAKE) {}
 
 		return 1;
 	}
 
 	/**
 	 * Sets the columns in the Drink table to different values.
 	 * 
 	 * @param name Name of the Drink to update.
 	 * @param column The column to update.
 	 * @param set The value to update the column with.
 	 * @return 0 if successful.
 	 */
 	public static int setDrink(String name, String column, int set) {
 		if (SQLITE) {
 
 			/**
 			 * SQLITE setDrink(String name, String column, int set).
 			 */
 			ContentValues values = new ContentValues();
 
 			// Choose which columns you want to query. null queries all columns.
 			String[] projection = { DrinkTable.COLUMN_ID, DrinkTable.COLUMN_NAME };
 
 			// Query database.
 			Cursor cursor = MyBarApplication.ContentResolver().query(
 					MyBarContentProvider.CONTENTURI_DRINK, projection,
 					DrinkTable.COLUMN_NAME + " = ? ", new String[] { name }, null);
 
 			// Successful query?.
 			if (cursor != null) {
 
 				// Is there any data from the requested Query.
 				if (cursor.moveToFirst()) {
 
 					values.put(column, set);
 					int rowUpdated = MyBarApplication.ContentResolver().update(
 							MyBarContentProvider.CONTENTURI_DRINK, values,
 							DrinkTable.COLUMN_NAME + " = ? ", new String[] { name });
 
 					// Print the updated drink.
 					Log.d(Data.class.getClass().getName(),
 							"Drink: "
 									+ cursor.getString(cursor
 											.getColumnIndexOrThrow(DrinkTable.COLUMN_NAME))
 									+ ". Rows updated in this query: "
 									+ Integer.toString(rowUpdated));
 
 					// Close the cursor.
 					cursor.close();
 
 					return 0;
				} else {
 					// Error message in LogCat.
 					Log.e(Data.class.getClass().getName(), "name doesn't exist");
 
 					// Close the cursor.
 					cursor.close();
 
 					// name doesn't exist. Return 1.
 					return 1;
 				}
 			}
 			/**
 			 * End of SQLite setDrink(String name, String column, int set).
 			 */
 		}
 		if (EDATA) {}
 		if (FAKE) {}
 
 		return 1;
 	}
 
 	/**
 	 * Sets the columns in the Drink table to different values.
 	 * 
 	 * @param ID _id of the Drink to update.
 	 * @param column The column to update.
 	 * @param set The value to update the column with.
 	 * @return 0 if successful.
 	 */
 	public static int setDrink(int ID, String column, int set) {
 		if (SQLITE) {
 
 			/**
 			 * SQLITE setDrink(int ID, String column, int set).
 			 */
 			ContentValues values = new ContentValues();
 
 			// Choose which columns you want to query. null queries all columns.
 			String[] projection = { DrinkTable.COLUMN_ID, DrinkTable.COLUMN_NAME };
 
 			// Query database.
 			Cursor cursor = MyBarApplication.ContentResolver().query(
 					MyBarContentProvider.CONTENTURI_DRINK, projection,
 					DrinkTable.COLUMN_ID + "=" + ID, null, null);
 
 			// Successful query?.
 			if (cursor != null) {
 
 				// Is there any data from the requested Query.
 				if (cursor.moveToFirst()) {
 
 					values.put(column, set);
 					int rowUpdated = MyBarApplication.ContentResolver().update(
 							MyBarContentProvider.CONTENTURI_DRINK, values,
 							DrinkTable.COLUMN_ID + "=" + ID, null);
 
 					// Print the updated drink.
 					Log.d(Data.class.getClass().getName(),
 							"Drink: "
 									+ cursor.getString(cursor
 											.getColumnIndexOrThrow(DrinkTable.COLUMN_NAME))
 									+ ". Rows updated in this query: "
 									+ Integer.toString(rowUpdated));
 
 					// Close the cursor.
 					cursor.close();
 
 					return 0;
 				} else {
 					// Error message in LogCat.
 					Log.e(Data.class.getClass().getName(), "ID doesn't exist");
 
 					// Close the cursor.
 					cursor.close();
 
 					// ID doesn't exist. Return 1.
 					return 1;
 				}
 			}
 			/**
 			 * End of SQLite setDrink(int ID, String column, int set).
 			 */
 		}
 		if (EDATA) {}
 		if (FAKE) {}
 
 		return 1;
 	}
 
 	public static String[] getDrinkNameAsArray() {
 		String[] drinks;
 		if (SQLITE) {
 			LinkedList<String> nameList = new LinkedList<String>();
 
 			for (int i = 0; i < Data.getAllDrinks().size(); i++) {
 				nameList.add(Data.getAllDrinks().get(i).getName());
 
 				Log.d(Controller.class.getClass().getName(), ""
 						+ Data.getAllDrinks().get(i).get_id() + " "
 						+ Data.getAllDrinks().get(i).getName());
 			}
 
 			String[] array = new String[nameList.size()];
 			nameList.toArray(array);
 
 			return array;
 		}
 		if (EDATA) {}
 		if (FAKE) {
 			drinks = new String[20];
 			drinks[0] = "Cola och tonic";
 			drinks[1] = "Cola lime";
 			drinks[2] = "Cola gin";
 			drinks[3] = "Cola citrus";
 			drinks[4] = "Cola hallon";
 			drinks[5] = "Cola light";
 			drinks[6] = "Cola sun";
 			drinks[7] = "Cola looka";
 			drinks[8] = "Cola with ice";
 			drinks[9] = "Rom and cocke";
 
 			return drinks;
 		}
 		return null;
 	}
 
 	/**
 	 * Search for Drinks in the database.
 	 * 
 	 * @param searchName
 	 * @return A LinkedList with the Drink containing the searchName string.
 	 */
 	public static LinkedList<Drink> searchDrinkName(String searchName) {
 		if (FAKE) {
 			return fakeDrinkList;
 		}
 		return null;
 	}
 
 	/**
 	 * Search for ingredients in the database.
 	 * 
 	 * @param search Search for ingredient.
 	 * @param limit Limit returned ingredients.
 	 * @return A LinkedList with the ingredients containing the searchName
 	 *         string.
 	 */
 	public static LinkedList<Ingredient> searchIngredients(String search, int limit) {
 		if (SQLITE) {
 
 			/**
 			 * SQLITE searchIngredients().
 			 */
 			LinkedList<Ingredient> ingredientList = new LinkedList<Ingredient>();
 
 			// Choose which columns you want to query. null queries all columns.
 			// String[] projection = { IngredientTable.COLUMN_NAME,
 			// IngredientTable.COLUMN_DESCRIPTION };
 
 			// Query database.
 			Cursor cursor = MyBarApplication.ContentResolver().query(
 					MyBarContentProvider.CONTENTURI_INGREDIENT, null, IngredientTable.COLUMN_NAME +
 					" LIKE ? ", new String[]{"%"+search+"%"}, 
 					IngredientTable.COLUMN_NAME+" LIMIT "+limit);
 
 			// Successful query?.
 			if (cursor != null) {
 
 				// Is there any data from the requested Query.
 				if (cursor.moveToFirst()) {
 
 					do {
 						int _id = cursor.getInt(cursor
 								.getColumnIndexOrThrow(IngredientTable.COLUMN_ID));
 						String name = cursor.getString(cursor
 								.getColumnIndexOrThrow(IngredientTable.COLUMN_NAME));
 						String url = cursor.getString(cursor
 								.getColumnIndexOrThrow(IngredientTable.COLUMN_URL));
 						String type = cursor.getString(cursor
 								.getColumnIndexOrThrow(IngredientTable.COLUMN_TYPE));
 						int alcoholcontent = cursor.getInt(cursor
 								.getColumnIndexOrThrow(IngredientTable.COLUMN_ALCOHOLCONTENT));
 						String description = cursor.getString(cursor
 								.getColumnIndexOrThrow(IngredientTable.COLUMN_DESCRIPTION));
 						ingredientList.add(new Ingredient(_id, name, url, type, alcoholcontent,
 								description));
 						Log.d(Data.class.getName(), "Search returned: " + name);
 					} while (cursor.moveToNext());
 
 					// Close the cursor.
 					cursor.close();
 
 					return ingredientList;
 				} else {
 					// Close the cursor.
 					cursor.close();
 
 					// No ingredients in Query. Return Empty LinkedList<Ingredient>.
 					return new LinkedList<Ingredient>();
 				}
 			}
 			/**
 			 * End of SQLite searchIngredients().
 			 */
 		}
 		if (EDATA) {}
 		if (FAKE) {}
 		return null;
 	}
 }
