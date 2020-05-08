 package edu.chl.dat076.foodfeed.controller;
 
 import edu.chl.dat076.foodfeed.exception.AccessDeniedException;
 import edu.chl.dat076.foodfeed.model.entity.*;
 import edu.chl.dat076.foodfeed.model.flash.*;
 import edu.chl.dat076.foodfeed.model.service.*;
 import java.util.List;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.access.annotation.Secured;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.annotation.Validated;
 import org.springframework.web.bind.annotation.*;
 import org.springframework.web.client.ResourceAccessException;
 import org.springframework.web.servlet.mvc.support.RedirectAttributes;
 
 /**
  * Handles requests for recipes.
  */
 @Controller
 @RequestMapping("/recipes")
 public class RecipeController {
 
     @Autowired
     private UserService userService;
     @Autowired
     private RecipeService recipeService;
     private static final Logger logger = LoggerFactory
             .getLogger(RecipeController.class);
 
     /**
      * Shows a list of recipes
      */
     @RequestMapping(value = "", method = RequestMethod.GET)
     public String list(Model model) {
         List<Recipe> recipes = recipeService.findAll();
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
     public String add(Model model, @Validated Recipe recipe,
             BindingResult result, RedirectAttributes redirectAttributes) {
         if (result.hasErrors()) {
             return "recipes/add";
         }
         logger.info("Saving a new recipe.");
 
         User activeUser = userService.find(SecurityContextHolder.getContext()
                 .getAuthentication().getName());
 
         recipeService.create(recipe, activeUser);
 
         redirectAttributes.addFlashAttribute("flash", new FlashMessage(
                 "Created recipe " + recipe.getName() + ".", FlashType.INFO));
 
         return "redirect:/recipes/" + recipe.getId();
     }
 
     /**
      * Adds another ingredient when creating a recipe.
      */
     @RequestMapping(value = "/add", method = RequestMethod.POST, params = "add-ingredient")
     @Secured("ROLE_USER")
     public String addIngredientOnAdd(@ModelAttribute Recipe recipe) {
         logger.info("Adding another ingredient.");
         recipe.getIngredients().add(new Ingredient());
         return "recipes/add";
     }
 
     /**
      * Removes an ingredient when creating a recipe.
      */
     @RequestMapping(value = "/add", method = RequestMethod.POST, params = "remove-ingredient")
     @Secured("ROLE_USER")
     public String removeIngredientOnAdd(@ModelAttribute Recipe recipe,
             @RequestParam("remove-ingredient") int index) {
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
         Recipe recipe = recipeService.find(id);
         model.addAttribute("recipe", recipe);
 
         boolean ownedByLoggedInUser = recipe.getUser().getUsername()
                 .equals(SecurityContextHolder.getContext().getAuthentication().getName());
         model.addAttribute("ownedByLoggedInUser", ownedByLoggedInUser);
 
         return "recipes/show";
     }
 
     /**
      * Form to edit a recipe
      */
     @RequestMapping(value = "/{id}/edit", method = RequestMethod.GET)
     @Secured("ROLE_USER")
     public String editForm(@PathVariable long id, Model model) {
         Recipe recipe = recipeService.find(id);
         checkOwnership(recipe);
         logger.info("Showing form to edit recipe with ID " + recipe.getId() + ".");
         model.addAttribute("recipe", recipe);
         return "recipes/edit";
     }
 
     /**
      * Edits a recipe
      */
     @RequestMapping(value = "/{id}/edit", method = RequestMethod.POST, params = "edit")
     @Secured("ROLE_USER")
    public String edit(Model model, @Validated Recipe recipe, BindingResult result,
    @PathVariable long id, RedirectAttributes redirectAttributes) {
         if (result.hasErrors()) {
             return "recipes/edit";
         }
        Recipe oldRecipe = recipeService.find(id);
        checkOwnership(oldRecipe);
         logger.info("Updating recipe with ID " + recipe.getId() + ".");
         recipe.setUser(oldRecipe.getUser());
         recipeService.update(recipe);
         redirectAttributes.addFlashAttribute("flash", new FlashMessage(
                 "Edited recipe " + recipe.getName() + ".", FlashType.INFO));
         return "redirect:/recipes/" + recipe.getId();
     }
 
     /**
      * Adds an ingredient when editing a recipe.
      */
     @RequestMapping(value = "/{id}/edit", method = RequestMethod.POST, params = "add-ingredient")
     @Secured("ROLE_USER")
     public String addIngredientOnEdit(@ModelAttribute Recipe recipe) {
         logger.info("Adding another ingredient.");
         recipe.getIngredients().add(new Ingredient());
         return "recipes/edit";
     }
 
     /**
      * Removes an ingredient when editing a recipe.
      */
     @RequestMapping(value = "/{id}/edit", method = RequestMethod.POST, params = "remove-ingredient")
     @Secured("ROLE_USER")
     public String removeIngredientOnEdit(@ModelAttribute Recipe recipe,
             @RequestParam("remove-ingredient") int index) {
         logger.info("Removing ingredient att index " + index + ".");
         recipe.getIngredients().remove(index);
         return "recipes/edit";
     }
 
     /**
      * Deletes a recipe
      */
     @RequestMapping(value = "/{id}/delete", method = RequestMethod.GET)
     @Secured("ROLE_USER")
     public String delete(@PathVariable long id, Model model) {
         User authUser = userService.find(SecurityContextHolder.getContext().getAuthentication().getName());
         Recipe recipe = recipeService.find(id);
         checkOwnership(recipe);
         authUser.getRecipes().remove(recipe);
         logger.info("Deleting recipe with ID " + id + ".");
         recipeService.delete(recipe);
         return "redirect:/recipes";
     }
 
     @RequestMapping(value = "/{id}/confirmdelete", method = RequestMethod.GET)
     @Secured("ROLE_USER")
     public String confirmDelete(Model model, @PathVariable long id) {
         logger.info("Showing confirm delete view");
         model.addAttribute("recipe", recipeService.find(id));
         return "recipes/confirmdelete";
     }
 
     /**
      * Check that a user owns a recipe
      */
     private void checkOwnership(Recipe recipe) {
         try {
             User user = userService.find(SecurityContextHolder.getContext().getAuthentication().getName());
             if (!recipe.getUser().equals(user)) {
                 throw new AccessDeniedException();
             }
         } catch (ResourceAccessException e) {
             // This means user is not logged in
             throw new AccessDeniedException();
         }
     }
 }
