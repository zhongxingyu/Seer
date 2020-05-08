 package edu.chl.dat076.foodfeed.controller;
 
 import edu.chl.dat076.foodfeed.model.dao.RecipeDao;
 import edu.chl.dat076.foodfeed.model.entity.Ingredient;
 import edu.chl.dat076.foodfeed.model.entity.Recipe;
 import java.util.List;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.access.annotation.Secured;
 import org.springframework.stereotype.Controller;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.annotation.Validated;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 /**
  * Handles requests for recipes.
  */
 @Controller
 @RequestMapping("/recipes")
 @Transactional
 public class RecipeController {
 
     @Autowired
     private RecipeDao recipeDao;
     private static final Logger logger = LoggerFactory
             .getLogger(RecipeController.class);
 
     /**
      * Shows a list of recipes
      */
     @RequestMapping(value = "", method = RequestMethod.GET)
     public String list(Model model) {
         List<Recipe> recipes = recipeDao.findAll();
         logger.info("Listing " + recipes.size() + " recipes.");
         model.addAttribute("recipes", recipes);
         return "recipes/list";
     }
 
     /**
      * Form to add a recipe
      */
     @RequestMapping(value = "/add", method = RequestMethod.GET)
     @Secured("ROLE_USER")
     public String addForm(Model model) {
         logger.info("Showing form to add a Recipe.");
         Recipe recipe = new Recipe();
         recipe.getIngredients().add(new Ingredient());
         model.addAttribute("recipe", recipe);
         return "recipes/add";
     }
 
     /**
      * Creates a recipe
      */
     @RequestMapping(value = "/add", method = RequestMethod.POST, params = "add")
     @Secured("ROLE_USER")
     public String add(Model model, @Validated Recipe recipe, BindingResult result) {
         if (result.hasErrors()) {
             return "recipes/add";
         }
         logger.info("Saving a new recipe.");
         recipeDao.create(recipe);
         return "redirect:/recipes/" + recipe.getId();
     }
 
     /**
      * Adds another ingredient when creating a recipe.
      */
     @RequestMapping(value = "/add", method = RequestMethod.POST, params = "add-ingredient")
     @Secured("ROLE_USER")
     public String addIngredientOnAdd(Model model, @Validated Recipe recipe, BindingResult result) {
         logger.info("Adding another ingredient.");
         recipe.getIngredients().add(new Ingredient());
         return "recipes/add";
     }
 
     /**
      * Removes an ingredient when creating a recipe.
      */
     @RequestMapping(value = "/add", method = RequestMethod.POST, params = "remove-ingredient")
     @Secured("ROLE_USER")
     public String removeIngredientOnAdd(Model model, @Validated Recipe recipe,
             BindingResult result, @RequestParam("remove-ingredient") int index) {
        logger.info("Removing ingredient att index " + index + ".");
         recipe.getIngredients().remove(index);
         return "recipes/add";
     }
 
     /**
      * Shows a recipe
      */
     @RequestMapping(value = "/{id}", method = RequestMethod.GET)
     public String show(@PathVariable long id, Model model) {
         logger.info("Showing recipe with ID " + id + ".");
         model.addAttribute("recipe", recipeDao.find(id));
         return "recipes/show";
     }
 
     /**
      * Form to edit a recipe
      */
     @RequestMapping(value = "{id}/edit", method = RequestMethod.GET)
     @Secured("ROLE_USER")
     public String editForm(@PathVariable long id, Model model) {
         Recipe recipe = recipeDao.find(id);
         logger.info("Showing form to edit recipe with ID " + recipe.getId() + ".");
         model.addAttribute("recipe", recipe);
         return "recipes/edit";
     }
 
     /**
      * Edits a recipe
      */
     @RequestMapping(value = "/{id}/edit", method = RequestMethod.POST, params = "edit")
     @Secured("ROLE_USER")
     public String edit(Model model, @Validated Recipe recipe, BindingResult result) {
         if (result.hasErrors()) {
             return "recipes/edit";
         }
         logger.info("Updating recipe with ID " + recipe.getId() + ".");
         recipeDao.update(recipe);
         return "redirect:/recipes/" + recipe.getId();
     }
 
     /**
      * Adds an ingredient when editing a recipe.
      */
     @RequestMapping(value = "/{id}/edit", method = RequestMethod.POST, params = "add-ingredient")
     @Secured("ROLE_USER")
     public String addIngredientOnEdit(Model model, @Validated Recipe recipe, BindingResult result) {
         logger.info("Adding another ingredient.");
         recipe.getIngredients().add(new Ingredient());
         return "recipes/edit";
     }
 
     /**
      * Removes an ingredient when editing a recipe.
      */
     @RequestMapping(value = "/{id}/edit", method = RequestMethod.POST, params = "remove-ingredient")
     @Secured("ROLE_USER")
     public String removeIngredientOnEdit(Model model, @Validated Recipe recipe,
             BindingResult result, @RequestParam("remove-ingredient") int index) {
        logger.info("Removing ingredient att index " + index + ".");
         recipe.getIngredients().remove(index);
         return "recipes/edit";
     }
 
     /**
      * Deletes a recipe
      */
     @RequestMapping(value = "/{id}/delete", method = RequestMethod.GET)
     public String delete(@PathVariable long id, Model model) {
         logger.info("Deleting recipe with ID " + id + ".");
         recipeDao.delete(id);
         return "redirect:/recipes";
     }
 }
