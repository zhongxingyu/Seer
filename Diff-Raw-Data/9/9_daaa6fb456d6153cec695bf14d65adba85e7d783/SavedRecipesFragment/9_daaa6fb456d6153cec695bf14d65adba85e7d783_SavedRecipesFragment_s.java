 /*
  * Copyright 2013 Adam Saturna
  *  Copyright 2013 Brian Trinh
  *  Copyright 2013 Ethan Mykytiuk
  *  Copyright 2013 Prateek Srivastava (@f2prateek)
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 
 package com.cmput301.recipebot.ui.fragments;
 
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.GridView;
 import android.widget.Toast;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.cmput301.recipebot.R;
 import com.cmput301.recipebot.model.Ingredient;
 import com.cmput301.recipebot.model.Recipe;
 import com.cmput301.recipebot.ui.RecipeActivity;
 import com.cmput301.recipebot.ui.adapters.RecipeGridAdapter;
 import com.github.rtyley.android.sherlock.roboguice.fragment.RoboSherlockFragment;
 
 import java.util.ArrayList;
 
 /**
  * A simple fragment that shows a list of {@link Recipe} items.
  */
 public class SavedRecipesFragment extends RoboSherlockFragment implements AdapterView.OnItemClickListener {
 
     private static final String LOGTAG = "SavedRecipesFragment";
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
         setHasOptionsMenu(true);
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         View v = inflater.inflate(R.layout.fragment_saved_recipes, container, false);
         GridView gridview = (GridView) v.findViewById(R.id.gridview);
         gridview.setAdapter(new RecipeGridAdapter(getSherlockActivity(), getTestRecipes()));
         gridview.setOnItemClickListener(this);
         return v;
     }
 
     @Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
         inflater.inflate(R.menu.fragment_saved_recipes, menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.menu_add_recipe:
                 addRecipe();
                 return true;
             case R.id.menu_search_saved_recipes:
                 // TODO : search recipes
                 Toast.makeText(getSherlockActivity(), "TODO: Search Recipe", Toast.LENGTH_LONG).show();
                 return true;
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
     /**
      * Start an activity to add a new Recipe
      */
     private void addRecipe() {
         Recipe recipe = new Recipe();
         recipe.setId(89);
         recipe.setName("Shake and Bake Chicken");
         recipe.setUser("Colonel Sanders");
         ArrayList<String> directions = new ArrayList<String>();
         directions.add("1. First Shake");
         directions.add("2. Then Bake");
         recipe.setDirections(directions);
         ArrayList<Ingredient> ingredients = new ArrayList<Ingredient>();
         ingredients.add(new Ingredient("Chicken", "g", 500f));
         ingredients.add(new Ingredient("Shake and Bake mix", "packet", 1f));
         ingredients.add(new Ingredient("Tequila", "bottle", 1f));
         recipe.setIngredients(ingredients);
         ArrayList<Uri> photos = new ArrayList<Uri>();
         photos.add(Uri.parse("http://www.kraftrecipes.com/assets/recipe_images/SHAKE_N_BAKE_Honey_Drummies.jpg"));
         photos.add(Uri.parse("http://images.media-allrecipes.com/userphotos/250x250/00/68/33/683349.jpg"));
         recipe.setPhotos(photos);
         Intent intent = new Intent(getSherlockActivity(), RecipeActivity.class);
         intent.putExtra(RecipeActivity.EXTRA_RECIPE, recipe);
         startActivity(intent);
     }
 
     @Override
     public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
         Toast.makeText(getSherlockActivity(), "clicked recipe # " + position, Toast.LENGTH_SHORT).show();
     }
 
     private static ArrayList<Recipe> getTestRecipes() {
 
         final String[] IMAGES = new String[]{
                 "https://lh6.googleusercontent.com/-jZgveEqb6pg/T3R4kXScycI/AAAAAAAAAE0/xQ7CvpfXDzc/s1024/sample_image_01.jpg",
                 "https://lh4.googleusercontent.com/-K2FMuOozxU0/T3R4lRAiBTI/AAAAAAAAAE8/a3Eh9JvnnzI/s1024/sample_image_02.jpg",
                 "https://lh5.googleusercontent.com/-SCS5C646rxM/T3R4l7QB6xI/AAAAAAAAAFE/xLcuVv3CUyA/s1024/sample_image_03.jpg",
                 "https://lh6.googleusercontent.com/-f0NJR6-_Thg/T3R4mNex2wI/AAAAAAAAAFI/45oug4VE8MI/s1024/sample_image_04.jpg",
                 "https://lh3.googleusercontent.com/-n-xcJmiI0pg/T3R4mkSchHI/AAAAAAAAAFU/EoiNNb7kk3A/s1024/sample_image_05.jpg",
                 "https://lh3.googleusercontent.com/-X43vAudm7f4/T3R4nGSChJI/AAAAAAAAAFk/3bna6D-2EE8/s1024/sample_image_06.jpg",
                 "https://lh5.googleusercontent.com/-MpZneqIyjXU/T3R4nuGO1aI/AAAAAAAAAFg/r09OPjLx1ZY/s1024/sample_image_07.jpg",
                 "https://lh6.googleusercontent.com/-ql3YNfdClJo/T3XvW9apmFI/AAAAAAAAAL4/_6HFDzbahc4/s1024/sample_image_08.jpg",
                 "https://lh5.googleusercontent.com/-Pxa7eqF4cyc/T3R4oasvPEI/AAAAAAAAAF0/-uYDH92h8LA/s1024/sample_image_09.jpg",
                 "https://lh4.googleusercontent.com/-Li-rjhFEuaI/T3R4o-VUl4I/AAAAAAAAAF8/5E5XdMnP1oE/s1024/sample_image_10.jpg",
                 "https://lh5.googleusercontent.com/-_HU4fImgFhA/T3R4pPVIwWI/AAAAAAAAAGA/0RfK_Vkgth4/s1024/sample_image_11.jpg",
                 "https://lh6.googleusercontent.com/-0gnNrVjwa0Y/T3R4peGYJwI/AAAAAAAAAGU/uX_9wvRPM9I/s1024/sample_image_12.jpg",
                 "https://lh3.googleusercontent.com/-HBxuzALS_Zs/T3R4qERykaI/AAAAAAAAAGQ/_qQ16FaZ1q0/s1024/sample_image_13.jpg",
                 "https://lh4.googleusercontent.com/-cKojDrARNjQ/T3R4qfWSGPI/AAAAAAAAAGY/MR5dnbNaPyY/s1024/sample_image_14.jpg",
                 "https://lh3.googleusercontent.com/-WujkdYfcyZ8/T3R4qrIMGUI/AAAAAAAAAGk/277LIdgvnjg/s1024/sample_image_15.jpg",
                 "https://lh6.googleusercontent.com/-FMHR7Vy3PgI/T3R4rOXlEKI/AAAAAAAAAGs/VeXrDNDBkaw/s1024/sample_image_16.jpg",
                 "https://lh4.googleusercontent.com/-mrR0AJyNTH0/T3R4rZs6CuI/AAAAAAAAAG0/UE1wQqCOqLA/s1024/sample_image_17.jpg",
                 "https://lh6.googleusercontent.com/-z77w0eh3cow/T3R4rnLn05I/AAAAAAAAAG4/BaerfWoNucU/s1024/sample_image_18.jpg",
                 "https://lh5.googleusercontent.com/-aWVwh1OU5Bk/T3R4sAWw0yI/AAAAAAAAAHE/4_KAvJttFwA/s1024/sample_image_19.jpg",
                 "https://lh6.googleusercontent.com/-q-js52DMnWQ/T3R4tZhY2sI/AAAAAAAAAHM/A8kjp2Ivdqg/s1024/sample_image_20.jpg",
                 "https://lh5.googleusercontent.com/-_jIzvvzXKn4/T3R4t7xpdVI/AAAAAAAAAHU/7QC6eZ10jgs/s1024/sample_image_21.jpg",
                 "https://lh3.googleusercontent.com/-lnGi4IMLpwU/T3R4uCMa7vI/AAAAAAAAAHc/1zgzzz6qTpk/s1024/sample_image_22.jpg",
         };
 
         ArrayList<Recipe> recipes = new ArrayList<Recipe>();
        for (int i = 0; i < 5; i++) {
             ArrayList<Uri> photos = new ArrayList<Uri>();
             photos.add(Uri.parse(IMAGES[i % IMAGES.length]));
             Recipe r = new Recipe();
             r.setId(0);
             r.setName("hi");
             r.setUser("bob");
             r.setPhotos(photos);
             recipes.add(r);
         }
 
         return recipes;
     }
 }
