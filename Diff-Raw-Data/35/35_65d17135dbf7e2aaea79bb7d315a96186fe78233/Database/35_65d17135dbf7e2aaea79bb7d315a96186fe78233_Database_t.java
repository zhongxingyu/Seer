 package com.github.cmput301w13t04.food.controller;
 
 import java.util.ArrayList;
 import java.util.concurrent.ExecutionException;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 
 import android.os.AsyncTask;
 
 import com.github.cmput301w13t04.food.model.Ingredient;
 import com.github.cmput301w13t04.food.model.Photo;
 import com.github.cmput301w13t04.food.model.Recipe;
 import com.github.cmput301w13t04.food.model.query.QueryID;
 import com.github.cmput301w13t04.food.model.query.QueryIngredients;
 import com.github.cmput301w13t04.food.model.query.ResultsRecipe;
 import com.github.cmput301w13t04.food.model.query.QueryEmail;
 import com.google.gson.Gson;
 
 /**
  * An object used for communication with the online database
  * 
  * @author W13T04
  * 
  */
 public class Database {
 
 	public Database() {
 	}
 
 	public Recipe searchByID(Long id) {
 		QueryID query = new QueryID(String.valueOf(id));
 		try {
 			return new SearchRecipeByIDTask().execute(query).get();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	private class SearchRecipeByIDTask extends AsyncTask<QueryID, Void, Recipe> {
 
 		@Override
 		protected Recipe doInBackground(QueryID... queries) {
 			try {
 				HttpClient httpClient = new DefaultHttpClient();
 				HttpPost httpPost = new HttpPost(
 						"http://earthiverse.ath.cx:9200/food/recipe/_search");
 
 				QueryID query = queries[0];
 
 				StringEntity get = new StringEntity(new Gson().toJson(query));
 				httpPost.setEntity(get);
 
 				HttpResponse response = httpClient.execute(httpPost);
 				String responseBody = EntityUtils
 						.toString(response.getEntity());
 
 				ResultsRecipe result = new Gson().fromJson(responseBody,
 						ResultsRecipe.class);
 
 				// Download the recipe pictures to device
 				imgurController imageHost = new imgurController();
 				Recipe recipe = result.getRecipe(0);
 				for (int j = 0; j < recipe.getPhotos().size(); j++) {
 					try {
 						Photo photo = recipe.getPhoto(j);
 
 						// Save to device
 						String localPath = imageHost.fetch(photo.getPath());
 						// Set new path
 						photo.setPath(localPath);
 					} catch (Exception e) {
 						// Something went wrong
 					}
 				}
 
 				// Download the recipe ingredients
 				for (int j = 0; j < recipe.getIngredients().size(); j++) {
 					try {
 						Photo photo = recipe.getIngredients().get(j).getPhoto();
 
 						// Save to device
 						String localPath = imageHost.fetch(photo.getPath());
 						// Set new path
 						photo.setPath(localPath);
 					} catch (Exception e) {
 						// Something went wrong
 					}
 				}
 
 				return recipe;
 			} catch (Exception e) {
 				// Something went wrong...
 			}
 			return null;
 		}
 
 	}
 
 	public ArrayList<Recipe> searchRecipeByEmail(String email) {
 		QueryEmail query = new QueryEmail(email.trim());
 		try {
 			return new SearchRecipeByEmailTask().execute(query).get();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	private class SearchRecipeByEmailTask extends
 			AsyncTask<QueryEmail, Void, ArrayList<Recipe>> {
 
 		@Override
 		protected ArrayList<Recipe> doInBackground(QueryEmail... queries) {
 			try {
 				HttpClient httpClient = new DefaultHttpClient();
 				HttpPost httpPost = new HttpPost(
 						"http://earthiverse.ath.cx:9200/food/recipe/_search");
 
 				QueryEmail query = queries[0];
 
 				StringEntity get = new StringEntity(new Gson().toJson(query));
 				httpPost.setEntity(get);
 
 				HttpResponse response = httpClient.execute(httpPost);
 				String responseBody = EntityUtils
 						.toString(response.getEntity());
 
 				ResultsRecipe result = new Gson().fromJson(responseBody,
 						ResultsRecipe.class);
 
 				ArrayList<Recipe> recipes = result.getRecipes();
 
 				// Download the first recipe pictures to device
 				imgurController imageHost = new imgurController();
 				for (int j = 0; j < recipes.size(); j++) {
 					Recipe recipe = recipes.get(j);
 					try {
 						Photo photo = recipe.getPhoto(0);
 
 						// Save to device
 						String localPath = imageHost.fetch(photo.getPath());
 						// Set new path
 						photo.setPath(localPath);
 					} catch (Exception e) {
 						// No first photo
 					}
 				}
 
 				return recipes;
 			} catch (Exception e) {
 				// Something went wrong...
 			}
 			return null;
 		}
 	}
 
 	public ArrayList<Recipe> searchByIngredients(
 			ArrayList<Ingredient> ingredients) {
 		QueryIngredients query = new QueryIngredients(ingredients);
 		try {
 			return new SearchRecipeByIngredientsTask().execute(query).get();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	private class SearchRecipeByIngredientsTask extends
 			AsyncTask<QueryIngredients, Void, ArrayList<Recipe>> {
 
 		@Override
 		protected ArrayList<Recipe> doInBackground(QueryIngredients... queries) {
 			try {
 				HttpClient httpClient = new DefaultHttpClient();
 				HttpPost httpPost = new HttpPost(
 						"http://earthiverse.ath.cx:9200/food/recipe/_search");
 
 				QueryIngredients query = queries[0];
 
 				StringEntity get = new StringEntity(new Gson().toJson(query));
 				httpPost.setEntity(get);
 
 				HttpResponse response = httpClient.execute(httpPost);
 				String responseBody = EntityUtils
 						.toString(response.getEntity());
 
 				ResultsRecipe result = new Gson().fromJson(responseBody,
 						ResultsRecipe.class);
 
 				ArrayList<Recipe> recipes = result.getRecipes();
 
 				// Download the first recipe pictures to device
 				imgurController imageHost = new imgurController();
 				for (int j = 0; j < recipes.size(); j++) {
 					Recipe recipe = recipes.get(j);
 					try {
 						Photo photo = recipe.getPhoto(0);
 
 						// Save to device
 						String localPath = imageHost.fetch(photo.getPath());
 						// Set new path
 						photo.setPath(localPath);
 					} catch (Exception e) {
 						// No first photo
 					}
 				}
 
 				return recipes;
 			} catch (Exception e) {
 				// Something went wrong...
 			}
 			return null;
 		}
 	}
 
 	/**
 	 * Publish a recipe to the online database
 	 * 
 	 * @param recipe
 	 */
 	public Boolean publishRecipe(Recipe recipe) {
 		try {
 			Boolean check = new PublishRecipeTask().execute(recipe).get();
 			return check;
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return false;
 		// TODO: Post this JSON to the server
 	}
 
 	private class PublishRecipeTask extends AsyncTask<Recipe, Void, Boolean> {
 		protected Boolean doInBackground(Recipe... recipes) {
 			try {
 				HttpClient httpClient = new DefaultHttpClient();
 				imgurController imageHost = new imgurController();
 				for (int i = 0; i < recipes.length; i++) {
 					Recipe recipe = recipes[i];
 
 					HttpPost httpPost = new HttpPost(
 							"http://www.earthiverse.ath.cx:9200/food/recipe/"
 									+ String.valueOf(recipe.getId()));
 
 					// Post recipe photos
 					ArrayList<Photo> photos = recipe.getPhotos();
 					ArrayList<String> oldRecipePhotoPaths = new ArrayList<String>();
 					for (int j = 0; j < photos.size(); j++) {
 						// Post image to server, get new path
 						oldRecipePhotoPaths.add(photos.get(j).getPath());
 
 						String remotePath = imageHost.post(photos.get(j)
 								.getPath());
 
 						// Assign new path
 						photos.get(j).setPath(remotePath);
 					}
 
 					ArrayList<Ingredient> ingredients = recipe.getIngredients();
 					ArrayList<String> oldIngredientPhotoPaths = new ArrayList<String>();
 					for (int j = 0; j < ingredients.size(); j++) {
 						// Post image to server, get new path
 						try {
 							// Get local path
 							oldIngredientPhotoPaths.add(ingredients.get(j)
 									.getPhoto().getPath());
 
 							String remotePath = imageHost.post(ingredients
 									.get(j).getPhoto().getPath());
 
 							// Assign new path
 							ingredients.get(j).getPhoto().setPath(remotePath);
 						} catch (Exception e) {
 							// No photo on ingredient
 						}
 					}
 
 					// Send converted recipe to ElasticSearch
 					StringEntity post = new StringEntity(
 							new Gson().toJson(recipe));
 					httpPost.setEntity(post);
 
 					httpClient.execute(httpPost);
 
 					// TODO: Verify response
 
 					// Restore Photo Paths
 					// Recipe Images
 					for (int j = 0; j < photos.size(); j++) {
 						photos.get(j).setPath(oldRecipePhotoPaths.get(j));
 					}
 					// Ingredient Images
 					for (int j = 0; j < ingredients.size(); j++) {
 						try {
 							ingredients.get(j).getPhoto()
 									.setPath(oldIngredientPhotoPaths.get(j));
 						} catch (Exception e) {
 							// No photo on ingredient
 						}
 					}
 				}
 			} catch (Exception e) {
 				// Something went wrong
 				e.printStackTrace();
 				return false;
 			}
 
 			return true;
 		}
 	}
 }
