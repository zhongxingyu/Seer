 /**
    (C) Kyle Kamperschroer
 */
 package com.kylek.ripe.ui;
 
 import java.util.*;
 import java.io.*;
 import java.nio.ByteBuffer;
 import java.nio.charset.Charset;
 import java.nio.charset.StandardCharsets;
 import java.nio.file.Files;
 import java.nio.file.Paths;
 
 import com.kylek.ripe.core.*;
 
 public class RipeServer extends NanoHTTPD{
 
    ////////////////////////////////////
    // Members
    ////////////////////////////////////
     
    // The ripe controller
    private RIPEController mRipe;
 
    // The db file loc (BAD KYLE!)
    private static final String mDbLoc =
       "/media/Warehouse/Dropbox/School/Eclipse/workspace/ripe_v1_java/ripeDB.db";
 
    // A constant for the port we will be using
    private static final int PORT = 8778;
 
    // A string that we will replace
    private static final String CONTENT_STR = "#{CONTENT}#";
 
    // The web root
    private static final String WEB_ROOT = "www";
 
    ////////////////////////////////////
    // Constructors
    ////////////////////////////////////
 
    // The default constructor
    public RipeServer() throws IOException{
       super(PORT, new File(WEB_ROOT));
 
       // This will take a second
       System.out.print("Initializing RIPE...");
       mRipe = new RIPEController(mDbLoc);
       System.out.println(" done!");
    }
 
    ////////////////////////////////////
    // Setters and getters
    ////////////////////////////////////
 
    // None for now
 
    ////////////////////////////////////
    // Other methods
    ////////////////////////////////////
 
    // Main entry point
    public static void main( String[] args ){
       try{
          new RipeServer();
       }
       catch( IOException ioe )
       {
          System.err.println("Couldn't start server:\n" + ioe);
          System.exit(-1);
       }
       System.out.println("Listening on port " + PORT + ". Hit Enter to stop.\n");
       try
       {
          System.in.read(); 
       }
       catch( Throwable t ) {};
    }
 
    // Serve up a page!
    public Response serve(
       String uri,
       String method,
       Properties header,
       Properties parms,
       Properties files){
 
       System.out.println(
          "URI: " + uri + "\n" +
          "Method: " + method + "\n" +
          "Header: " + header.toString() + "\n" +
          "Parms: " + parms.toString() + "\n" +
          "Files: " + files.toString() + "\n");
 
       // If method is GET and one of the last 4 characters is a '.', serve
       // up a file.
       int dotIndex = uri.indexOf(".");
       if (method.equals("GET") &&
           dotIndex != -1 &&
           uri.lastIndexOf(".") >= uri.length() - 4){
          // Serve up a file!
          return serveFile(uri, header, new File(WEB_ROOT), true);
       }
           
 
       // This string will ultimately be the entire page. We will do a find/replace
       // for the string CONTENT_STR, once we have the rest of the page rendered.
       String page = renderPageFramework();
       String content = "";
 
       try{
          // Base the page on what is specified.
          String requestedPage = parms.getProperty("page");
 
          // Get the recipe id (if provided)
          String recipeId = parms.getProperty("recipe");
          int recId = -1;
          if (recipeId != null &&
              !recipeId.equals("")){
             recId = Integer.parseInt(recipeId);
          }
 
 
          // The recipe listing page
          if (requestedPage == null ||
              requestedPage.equals("recipes") ||
              requestedPage.equals("/")){
             content = renderRecipes();
          }
          // The view recipe page
          else if (requestedPage.equals("view")){
             // View a specific recipe
             content = renderRecipe(recId);
          }
          else if(requestedPage.equals("add_recipe")){
             // Oh boy! A new recipe!
             content = renderAddRecipe();
          }
          else if (requestedPage.equals("add_recipe_manual")){
             content = renderAddRecipeManual();
          }
          else if(requestedPage.equals("add_recipe_go")){
             // Render the parse results page.
             content = renderAddRecipeGo(parms, files);
          }
          else if(requestedPage.equals("edit")){
             // Render the edit recipe page
             content = renderEditRecipe(recId);
          }
          else if(requestedPage.equals("edit_go")){
             content = renderEditRecipeGo(parms);
          }
          else if(requestedPage.equals("remove")){
             // Get the current recipe id
             if (recipeId != null){
                recId = Integer.parseInt(recipeId);
             }
 	            
             if (recId < mRipe.getAllRecipes().size() &&
                 recId >= 0){
                // Remove the recipe
                boolean retVal = mRipe.removeRecipe(mRipe.getAllRecipes().get(recId));
 	                
                // Display a link back to the recipes page
                if (retVal){
                   content +=
                      "Recipe deleted!\n<br/>\n";
                }
                else{
                   content +=
                      "Error removing recipe!\n<br/>\n";
                }
 	                
                // Link back to the listing
                content +=
                   "<a href='/'>Back to listing</a>\n";
             }
             else{
                content +=
                   "Invalid recipe id: " + recipeId;
             }
          }
          else{
             content += "<p>Unknown page: " + requestedPage + "</p>\n";
          }
       }
       catch (Exception e){
          // Yikes. Keep running!
          e.printStackTrace();
          System.out.println(e);
          content += "An unexpected error occured. Continuing anyways!";
       }
 
       page = page.replace(CONTENT_STR, content);
       
       return new NanoHTTPD.Response( HTTP_OK, MIME_HTML, page );
    }
 
    //// Private methods ////
     
    // Add the header to our output page
    private String renderPageFramework(){
       String page = "<html>\n";
       page += addHead();
       page +=
          "    <body>\n" +
          "        <div id='ripe_header'>\n" +
          "            <span id='ripe_title' onclick=\"location.href='/'\">\n" +
          "               RIPE\n" +
          "            </span>\n" +
          "            <span id='ripe_subtitle'>\n" +
          "               Kyle's Recipe Parsing Engine\n" +
          "            </span>\n" +
          "        </div>\n" +
          "        <hr/>\n" +
                      renderNavBar() +
          "        <hr/>\n" +
          "        <div id='ripe_content'>\n" +
                       CONTENT_STR + "\n" +            
          "        </div>\n";
       page += addFooter();
       page +=
          "    </body>\n" +
          "</html>\n";
       return page;
    }
 
    // Add the head section
    private String addHead(){
       return
          "    <head>\n" +
          "        <title>RIPE: Recipe Parsing Engine</title>\n" +
          "        <link href='/stylesheets/ripe.css' rel='stylesheet' type='text/css' />\n" +
          "        <script src='/js/jquery-1.9.1.min.js' type='text/javascript'></script>\n" +
          "        <script src='/js/ripe.js' type='text/javascript'></script>\n" +
          "        <meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\" />\n" +
          "        <link href='http://fonts.googleapis.com/css?family=Droid+Sans:400,700' rel='stylesheet' type='text/css' />\n" +
          "    </head>\n";
    }
 
    // Add the footer to our output page
    private String addFooter(){
       return
          "    <hr/>\n" +
          "    <div id='ripe_footer'>\n" +
          "        <p>(C) Kyle Kamperschroer 2013</p>\n" +
          "    </div>\n";
    }
 
    // Render the sub page header
    private String renderContentHeader(String title){
       return
          "    <div id='ripe_content_header'>\n" +
          "        <span id='ripe_content_title'>" + title + "</span>\n" +
          "    </div>\n";
    }
 
    // Render the content for viewing all recipes
    private String renderRecipes(){
       String content = renderContentHeader("Listing Recipes");
 	
       // Build a table of our recipes
       content += 
          "<table border=1>\n" +
          "    <tr>\n" +
          "        <th>Recipe Name</th>\n" +
          "        <th colspan=2>Modify</th>\n" +
          "    </tr>\n";
 	
       // Add the recipes
       for (int i=0; i<mRipe.getAllRecipes().size(); i++){
          Recipe cur = mRipe.getAllRecipes().get(i);
          content +=
             "<tr>\n" +
             "   <td><a href=\"?page=view&recipe=" + i + "\">" + cur.getName() + "</a></td>\n" +
             "   <td><a href=\"/?page=edit&recipe=" + i + "\">Edit</a>\n" +
            "   <td><a class='remove_rec' href=\"/?page=remove&recipe=" + i + "\">Remove</a>\n" +
             "</tr>\n";
       }
 	
       content +=
          "</table>\n" +
          "<br/>\n" +
          "<a href=\"/?page=add_recipe\">Add a Recipe</a>\n";
 
       return content;
    }
 
    // Render a single recipe
    private String renderRecipe(final int recId){
       String content = "<div id='recipe'>\n";
       if (recId < mRipe.getAllRecipes().size() &&
           recId >= 0){
 
          // Get our current recipe
          Recipe recipe = mRipe.getAllRecipes().get(recId);
 	
          // The recipe name
          content += renderContentHeader(recipe.getName());
 
          // The attributes section
          content += renderRecipeAttributes(recipe);
 
          // The ingredients list section
          content += renderRecipeIngredientsList(recipe);
 
          // The directions section
          content += renderRecipeDirections(recipe);
 
          // End the recipe div
          content += "</div>\n";
          
          // Some recipe links
          content += renderEndRecipeLinks(recId);
       }
       else{
          content += "<p>Invalid recipe: " + recId + "</p>\n";
       }
       return content;
    }
 
    // Render the attributes section of the recipe
    private String renderRecipeAttributes(Recipe recipe){
       // The attributes section
       String content = "<div id='recipe_attributes'>\n";
          
       // The number of servings
       Yield yield = recipe.getYield();
       if (yield != null){
          double value = yield.getValue();
          String unit = yield.getUnit();
          String yieldStr = "";
          if (value > 0 &&
              value % 1.0 == 0){
             yieldStr += (int) value;
          }
          else{
             yieldStr += value;
          }
          yieldStr += " " + unit;
 
          content +=
             renderRecipeAttribute("Yield:",
                                   yieldStr);
       }
 
       // TODO : You could calculate any missing times, automatically!
          
       // The cook and prep time
       String cookTime = recipe.getCookTime();
       String prepTime = recipe.getPrepTime();
       String overallTime = recipe.getOverallTime();
       
       if (prepTime != null){
          content +=
             renderRecipeAttribute("Prep Time:",
                                   prepTime);
       }
       if (cookTime != null){
          content +=
             renderRecipeAttribute("Cook Time:",
                                   cookTime);
       }
       if (overallTime != null){
          content +=
             renderRecipeAttribute("Overall Time:",
                                   overallTime);
       }
 
       content += "</div>\n"; // The recipe_attributes div.
       return content;
    }
 
    // Render a single attribute
    private String renderRecipeAttribute(String attrName, String attrValue){
       return 
          "<span class='recipe_attribute'>\n" +
          "    <span class='recipe_attribute_name'>" + attrName + "</span>\n" +
          "    <span class='recipe_attribute_value'>\n" +
                   attrValue + "\n" +
          "    </span>\n" +
          "</span><br/>\n";
    }
 
    // Render the ingredients list
    private String renderRecipeIngredientsList(Recipe recipe){
       String content = "<div id='recipe_ingredients_list'>\n";
 
       // The logical "ingredients" separator
       content += "<span class='recipe_separator'>Ingredients:</span><br/>\n";
       
       // List the ingredients
       IngredientsList ings = recipe.getIngredients();
       Vector<MeasurementAndIngredient> meaIngs = ings.getIngredients();
 
       // Iterate through the ingredients, rendering each one.
       for (int i=0; i<meaIngs.size(); i++){
          // Get the individual components out
          MeasurementAndIngredient recipeIng = meaIngs.get(i);
 
          content += renderRecipeIngredient(recipeIng);
 
       }
       content += "</div>\n"; // Closing dive for recipe_ingredients_list
       return content;
    }
 
    // Render an individual measurement and ingredient
    private String renderRecipeIngredient(MeasurementAndIngredient recipeIng){
       String content = "<span class='recipe_ingredient'>\n";
       
       Measurement mea = recipeIng.getMeasurement();
       Measurement mea2 = recipeIng.getMeasurement2();
       Ingredient ing = recipeIng.getIngredient();
 
       if (mea != null){
          content += renderRecipeIngredientMeasurement(mea);
       }
      if (mea2 != null &&
          !mea2.getAmount().equals("") &&
          !mea2.getSpecifier().equals("") &&
          !mea2.getUnit().equals("")){
          content += " plus " +
             renderRecipeIngredientMeasurement(mea2);
       }
       if (ing != null){
          content += renderRecipeIngredientProduct(ing);
       }
 
       content += "\n</span><br/>\n"; // Closing div for recipe_ingredient
 
       return content;
    }
 
    // Render an individual measurement
    private String renderRecipeIngredientMeasurement(Measurement meas){
       String content = "";
       // Get the individual components
       String amount, specifier, unit;
       amount = meas.getAmount().toString();
       specifier = meas.getSpecifier().toString();
       unit = meas.getUnit().toString();
 
       if (!amount.equals("")){
          content += amount + " ";
       }
       if (!specifier.equals("")){
          content += specifier + " ";
       }
       if (!unit.equals("")){
          content += unit + " ";
       }
 
       return content;
    }
 
    // Render an individual ingredients product
    private String renderRecipeIngredientProduct(Ingredient ing){
       String content = "";
       String amazonUrl = ing.getAmazonUrl(); // Might be empty
       String productName = ing.getName();
       
       if (!amazonUrl.equals("")){
          // Use the amazon url
          content +=
             "<a href='" +
             ing.getAmazonUrl() +
             "'>" + productName + "</a>";
       }
       else{
          // No url, so just text
          content += productName;
       }
 
       // Get the special directions
       String specialDirections = ing.getSpecialDirections();
       if (!specialDirections.equals("")){
          // Don't add a space if we are just adding 
          if (specialDirections.charAt(0) != ','){
             content += " ";
          }
          content += specialDirections;
       }
 
       return content;
    }
 
    // Render the recipes directions
    private String renderRecipeDirections(Recipe recipe){
       String content = "<div id='recipe_directions'>\n";
 
       // The logical "Directions" separator
       content += "<span class='recipe_separator'>Directions:</span><br/>\n";
       
       String directions = recipe.getDirections();
 
       // Make the newlines html friendly
       directions = directions.replaceAll("(\r\n|\n)", "<br/>");
       content += directions;
 
       content += "</div>\n"; // Closing div of recipe_directions
       
       return content;
    }
 
    // Render some links that we want under the recipes page
    private String renderEndRecipeLinks(int recId){
       String content = "<div id='recipe_links'>\n";
 
       // Link to the edit content for this recipe
       content += renderEndRecipeLink("edit", "Edit", recId);
 
       // Link to the remove page for this recipe
       content += renderEndRecipeLink("remove", "Remove", recId);
 	
       // Link back to the listing
       content += renderEndRecipeLink("/", "Back to Listing", -1);
 
       content += "</div>\n"; // The closing div of recipe_links
       return content;
    }
 
    // Render an individual recipe link
    private String renderEndRecipeLink(String page, String visibleText, int recId){
       String content = "<span class='recipe_link'>\n";
       
       content += "<a href='?page=" + page;
       if (recId >= 0){
          content += "&recipe=" + recId;
       }
       content += "'>" + visibleText + "</a>\n";
 
       content += "</span>\n"; // The closing div of recipe_link
       return content;
    }
 
    // Redner the add recipe page
    private String renderAddRecipe(){
       // Generate the header for this page
       String content = renderContentHeader("Add Recipe");
 
       // Build the form
       content +=
          "<div id='add_recipe_form'>\n" +
          "<form action='?page=add_recipe_go' method='post'>\n" +
          "    Recipe:\n<br/>\n" +
          "    <textarea cols='80' rows='30' name='raw_recipe'>" +
          "</textarea>\n<br/>\n" +
          "    <input type='submit' value='Parse it!'/> or \n" +
          "    <input type='file' name='upfile' value='Upload from file'/>\n" +
          "</form>\n" +
          "</div>\n";
 
                     
       // Link back to the listing
       content +=
          "<br/><br/><br/><a href='/'>Back to listing</a>\n";
       
       return content;
    }
 
    // Render the add recipe go page
    private String renderAddRecipeGo(Properties parms, Properties files){
       String content = renderContentHeader("Parse Results");
       
       // Check if we have any files that were uploaded
       String upFile = files.getProperty("upfile");
       String recipe = parms.getProperty("raw_recipe");
       if (upFile != null &&
           !upFile.equals("")){
          // Oh boy, we have an uploaded file! Attempt to
          // read some text from it.
          try{
             recipe = readFile(upFile, StandardCharsets.UTF_8);
             System.out.println("Got recipe from file!");
             System.out.println(recipe);
          }catch (Exception e){
             // File seems to be bad
             content += "Was your file a valid text file?";
          }
       }
 		
       // Ask mRipe to parse the recipe
       Recipe parsed = mRipe.parseRecipe(recipe);
 	
       // Hopefully that worked! 
       // Add the recipe to our db
       boolean retVal = false;
       if (parsed != null){
          retVal = mRipe.addRecipe(parsed);
       }
 	
       if (retVal){
          content +=
             "Recipe parsed successfully. Click " +
             "<a href='?page=view&recipe=" + (mRipe.getAllRecipes().size()-1) +
             "'>here</a>.";
       }
       else{
          content += mRipe.getErrorMessage();
       }
       // Link back to the listing
       content +=
          "<br/><br/><br/>\n<a href='/'>Back to listing</a>\n";
 
       return content;
    }
 
    // Render the navigation bar
    private String renderNavBar(){
       String content = "<div id='ripe_navbar'>\n";
 
       // Render the links
       content +=
          renderNavLink("View Recipes", "/") +
          renderNavLink("Add Text Recipe", "?page=add_recipe") +
          renderNavLink("Add Recipe Manually", "?page=add_recipe_manual");
       
       content += "</div>\n";
       return content;
    }
 
    // Render a single nav link
    private String renderNavLink(String linkName, String linkLocation){
       String content =
          "<span class='ripe_nav_link' onclick=\"location.href='" +
          linkLocation + "'\">";
       content += linkName;
       content += "</span>\n";
       return content;
    }
 
    // Render the edit recipe page
    private String renderEditRecipe(int recId){
       String content = renderContentHeader("Edit Recipe");
 
       // Get the current recipe
       if (recId < mRipe.getAllRecipes().size() &&
           recId >= 0){
 
          // Get our current recipe
          Recipe recipe = mRipe.getAllRecipes().get(recId);
 
          content += renderRecipeForm(recipe, recId);
       }
       else{
          content += "Invalid recipe ID!";
       }
       return content;
    }
 
    // Render a recipe form
    private String renderRecipeForm(Recipe recipe, int recId){
       if (recipe == null){
          recipe = new Recipe();
       }
 
       // Build up the form
       String content =
          "<div id='edit_recipe_form'>\n" +
          "    <form action='?page=edit_go' method='post'>\n" +
          "        <span id='edit_recipe_title'>" +
          "            <fieldset id='edit_recipe_form_basic'>\n" +
          "                <legend>Basic</legend>\n" +
          "                    <label>Recipe Title<span class='label_desc'>What is your recipe called?</span></label> <input type='text' name='recipe_name' value=\"" +
          recipe.getName() + "\"/>" +
          "                </legend>\n" +
          "            </fieldset>\n" +
          "        </span>\n" +
          renderRecipeAttributesForm(recipe) +
          renderRecipeIngredientsListForm(recipe) +
          renderRecipeDirectionsForm(recipe) +
          "        <input type='submit' value='Save'/>\n" +
          "        <input type='button' value='Cancel' id='cancel_button'/>\n" +
          // A hidden field with the current recipe id (bad as far as security goes)
          "        <input type='hidden' id='recipe_id' name='recipe_id' value='" + recId + "'/>\n" +
          "    </form>\n" +
          "</div>\n";
 
       return content;
    }
 
    // Render the attributes portion of the form
    private String renderRecipeAttributesForm(Recipe recipe){
       Yield recYield = recipe.getYield();
       if (recYield == null){
          recYield = new Yield();
       }
       String content =
          "<fieldset id='edit_recipe_form_attributes'>\n" +
          "    <legend>Attributes</legend>\n" +
          "    <label>Yield Amount<span class='label_desc'>A number or decimal</span></label> <input type='text' name='yield_amount' value='" +
          recYield.getValue() + "'/>" +
          "    <label>Yield Units<span class='label_desc'>Servings, cups, etc.</span></label> <input type='text' name='yield_unit' value='" +
          recYield.getUnit() + "'/>\n" +
          "    <label>Preparation Time<span class='label_desc'>E.G. 25 minutes</span></label> <input type='text' name='prep_time' value='" +
          recipe.getPrepTime() + "'/>\n" +
          "    <label>Cook Time<span class='label_desc'>E.G. 35 m</span></label> <input type='text' name='cook_time' value='" +
          recipe.getCookTime() + "'/>\n" +
          "    <label>Overall Time<span class='label_desc'>E.G. 1 hr</span></label> <input type='text' name='overall_time' value='" +
          recipe.getOverallTime() + "'/>\n" +
          "</fieldset>\n";
       
       return content;
    }
 
    // Render the ingredients list portion of the form
    private String renderRecipeIngredientsListForm(Recipe recipe){
       // Get the ingredients list from the recipe
       IngredientsList objIngList = recipe.getIngredients();
       if (objIngList == null){
     	  objIngList = new IngredientsList();
       }
       Vector<MeasurementAndIngredient> vecIngList =
          objIngList.getIngredients();
       
       String content =
          "<fieldset id='edit_recipe_form_ingredients_list'>\n" +
          "    <legend>Ingredients</legend>\n";
 
       // Iterate through each ingredient
       int totalIngs = vecIngList.size();
 
       // We want at least 1 ingredient form to be shown
       if (totalIngs == 0){
          content += renderEditIngredient(null, 0);
          // Update totalIngs so it's visible in the hidden field
          totalIngs = 1;
       }
       else{
          for (int i=0; i<totalIngs; i++){
             content += renderEditIngredient(vecIngList.get(i), i);
          }
       }
       // Add an option to add another ingredient
       content += "<input type='button' id='add_ingredient' value='Add Another Ingredient'/>\n";
 
       // Add a hidden field which will contain our total count
       content += "<input type='hidden' id='hidden_total_ingredients' name='total_ingredients' value='" + totalIngs + "'/>\n";
 
       content += "</fieldset>";
       
       return content;
    }
 
    // Render an individual ingredient
    private String renderEditIngredient(MeasurementAndIngredient measIng, int index){
       String content = "";
       if (measIng == null){
          measIng = new MeasurementAndIngredient();
       }
       Measurement curMeas = measIng.getMeasurement();
       if (curMeas == null){
          curMeas = new Measurement();
       }
       Measurement curMeas2 = measIng.getMeasurement2();
       if (curMeas2 == null){
          curMeas2 = new Measurement();
       }
       Ingredient curIng = measIng.getIngredient();
       if (curIng == null){
          curIng = new Ingredient();
       }
          
       content +=
          "<fieldset class='ingredient_form' id='ingredient_" + index + "'>\n" +
          "   <legend>Ingredient " + (index + 1) + "</legend>\n" +
          "   <label>Amount<span class='label_desc'>Number or fraction</span></label> <input type='text' name='amount1_" + index + "' value='" +
          curMeas.getAmount() + "'>\n" +
          "   <label>Specifier<span class='label_desc'>E.G. (14oz)</span></label> <input type='text' name='specifier1_" + index + "' value='" +
          curMeas.getSpecifier() + "'>\n" +
          "   <label>Unit<span class='label_desc'>Can, jar, tsp, etc.</span></label> <input type='text' name='unit1_" + index + "' value='" +
          curMeas.getUnit() + "'>\n" +
          "      <label>Amount<span class='label_desc'>Optional 2nd amount</span></label> <input type='text' name='amount2_" + index + "' value='" +
          curMeas2.getAmount() + "'>\n" +
          "      <label>Specifier<span class='label_desc'>Optional 2nd specifier</span></label> <input type='text' name='specifier2_" + index + "' value='" +
          curMeas2.getSpecifier() + "'>\n" +
          "      <label>Unit<span class='label_desc'>Optional 2nd unit</span></label> <input type='text' name='unit2_" + index + "' value='" +
          curMeas2.getUnit() + "'>\n" +
          "      <label>Product<span class='label_desc'>E.G. Eggs, milk, etc.</span></label> <input type='text' name='product_" + index + "'value='" +
          curIng.getName() + "'>\n" +
          "   <label>Special Directions<span class='label_desc'>E.G. Shaken, stirred, etc.</span></label> <input type='text' name='specDir_" + index + "'value='" +
          curIng.getSpecialDirections() + "'>\n" +
          "   <input type='button' class='remove_ingredient' id='removeIng_" + index + "' value='Remove Ingredient'/>\n" +
          "\n" +
          "</fieldset>\n";
 
       return content;
    }
 
    // Render the recipe directions form
    private String renderRecipeDirectionsForm(Recipe recipe){
       String content =
          "<fieldset id='recipe_directions'>\n" +
          "   <legend>Directions</legend>\n" +
          "   <textarea name='recipe_directions' cols='70' rows='15'>" +
          recipe.getDirections() + "</textarea>\n" +
          "</fieldset>";
       
       return content;
    }
          
    // Render the edit recipe go page
    private String renderEditRecipeGo(Properties parms){
       String content = renderContentHeader("Edit Results");
 
       // Now determine the recipe's id, and save it
       int recId = Integer.parseInt(parms.getProperty("recipe_id"));
 
       // TODO --> This is a serious security issue. Hidden field, really?
       //          Should be good enough for a demo, however.
       // Update the recipe based on the recipe id
       if (recId > mRipe.getAllRecipes().size()){
          content += "<p>Recipe ID was found to be invalid.</p>\n";
          // Don't bother trying.
          return content;
       }
 
       // Check if this recipe's id is -1. If so, it's new!
       Recipe editedRecipe;
       if (recId == -1){
          editedRecipe = new Recipe();
       }else{
          // Get our recipe from our list of recipes based on it's id
          editedRecipe = mRipe.getAllRecipes().get(recId);
       }
 
       // Start by populating the title
       editedRecipe.setName(parms.getProperty("recipe_name"));
 
       // Set the different times
       editedRecipe.setPrepTime(parms.getProperty("prep_time"));
       editedRecipe.setCookTime(parms.getProperty("cook_time"));
       editedRecipe.setOverallTime(parms.getProperty("overall_time"));
 
       // Set the yield values
       String yieldAmt = parms.getProperty("yield_amount");
       String yieldUnit = parms.getProperty("yield_unit");
       Yield newYield = new Yield(Double.parseDouble(yieldAmt), yieldUnit);
       editedRecipe.setYield(newYield);
 
       // Determine the number of ingredients using the total_ingredients
       // TODO: If the user changed it to a non-int, what happens?
       int totalIngs = Integer.parseInt(parms.getProperty("total_ingredients"));
       IngredientsList ingList = new IngredientsList();
       Vector<MeasurementAndIngredient> measAndIngs =
          new Vector<MeasurementAndIngredient>();
 
       // Now populate the new vector of measurements and ingredients
       for (int i=0; i<totalIngs; i++){
          Measurement meas1 = new Measurement();
          Measurement meas2 = new Measurement();
          Ingredient ing = new Ingredient();
 
          // Populate each piece
          String amount1 = parms.getProperty("amount1_" + i);
          meas1.setAmount(amount1);
          String specifier1 = parms.getProperty("specifier1_" + i);
          meas1.setSpecifier(specifier1);
          String unit1 = parms.getProperty("unit1_" + i); 
          meas1.setUnit(unit1);
          String amount2 = parms.getProperty("amount2_" + i);
          meas2.setAmount(amount2);
          String specifier2 = parms.getProperty("specifier2_" + i); 
          meas2.setSpecifier(specifier2);
          String unit2 = parms.getProperty("unit2_" + i);
          meas2.setUnit(unit2);
          String product = parms.getProperty("product_" + i);
          ing.setName(product);
          String specDir = parms.getProperty("specDir_" + i);
          ing.setSpecialDirections(specDir);
          
          // Cool, now build the MeasurementAndIngredient and add it to the vector
          measAndIngs.add(new MeasurementAndIngredient(meas1, meas2, ing));
 
          // Debug message
          System.out.println("Ingredient " + i + ": " + amount1 + " " +
                             specifier1 + " " + unit1 + " " + amount2 + " " +
                             specifier2 + " " + unit2 + " " + product + " " +
                             specDir);
       }
 
       // Set the ingredients list value
       ingList.setIngredients(measAndIngs);
 
       // Set it as part of the recipe
       editedRecipe.setIngredients(ingList);
       
       // Set the directions
       editedRecipe.setDirections(parms.getProperty("recipe_directions"));
 
       // If recId was -1, this is a new recipe
       if (recId == -1){
          mRipe.addRecipe(editedRecipe);
       }
       else{
          // Update this recipe in our database
          mRipe.updateRecipe(editedRecipe);
       }
 
       content += renderContentHeader("Success") +
          "<p>Click " +
          "<a href='?page=view&recipe=" + (mRipe.getAllRecipes().size()-1) +
          "'>here</a>" +
          " to view the recipe!</p>\n";
       return content;
    }
 
    // Render the form to manually add a recipe
    private String renderAddRecipeManual(){
       String content = renderContentHeader("Manually Add Recipe");
 
       // Create an empty recipe
       Recipe recipe = new Recipe();
       content += renderRecipeForm(recipe, -1);
 
       return content;
    }
    
    // A useful utility method for reading a String from a file
    // Credit to erickson from SO:
    // http://stackoverflow.com/questions/326390/how-to-create-a-java-string-from-the-contents-of-a-file
    static String readFile(String path, Charset encoding) throws IOException{
       byte[] encoded = Files.readAllBytes(Paths.get(path));
       return encoding.decode(ByteBuffer.wrap(encoded)).toString();
    }
 }
