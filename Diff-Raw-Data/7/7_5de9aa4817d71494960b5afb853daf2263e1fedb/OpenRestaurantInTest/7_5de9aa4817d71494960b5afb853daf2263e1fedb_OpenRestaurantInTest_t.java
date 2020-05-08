 package com.dslplatform.examples.intermediate.test.setup;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import org.junit.Before;
 
 import com.dslplatform.client.Bootstrap;
 import com.dslplatform.examples.intermediate.*;
 import com.dslplatform.examples.intermediate.Consumables.repositories.*;
 import com.dslplatform.examples.intermediate.Cookbook.repositories.RecipeRepository;
 import com.dslplatform.patterns.ServiceLocator;
 
 /** This class holds test setup and utilities. */
 
 public abstract class OpenRestaurantInTest {
 
     protected Restaurant restaurant = null;
     protected RecipeRepository recipeRepository;
     protected MealRepository mealRepository;
     protected MealInfoRepository mealInfoRepository;
     protected BeverageRepository beverageRepository;
 
     @Before
     /**  This function illustrates how to perform an application setup. */
     public void setUp() throws Exception {
         final String pathnameRaw = System.getProperty("user.home")
                 + "/.config/dslcookbook/intermediate.ini";
         final String pathname = pathnameRaw;
 
          // 1. instantate a locator form the project.ini
         ServiceLocator locator = Bootstrap.init(pathname);
         restaurant = new Restaurant(locator);
         // 2. Grab instance of whatever repository
         //    you wish to use from this locator
         recipeRepository = locator.resolve(RecipeRepository.class);
         mealRepository = locator.resolve(MealRepository.class);
         beverageRepository = locator.resolve(BeverageRepository.class);
         mealInfoRepository = locator.resolve(MealInfoRepository.class);
     }
 
     protected <T> List<T> flatten(final T[][] A) {
         final List<T> res = new LinkedList<T>();
         for (T[] t : A) res.addAll(Arrays.asList(t));
         return res;
     }
 
     protected <T> Set<T> toSet(final T[][] A) {
         final Set<T> res = new HashSet<T>();
         for (T[] t : A)
             res.addAll(Arrays.asList(t));
         return res;
     }
 
     protected <T> Set<T> toSet(final T[] A) {
         final Set<T> res = new HashSet<T>();
         res.addAll(Arrays.asList(A));
         return res;
     }
 
     protected byte[] toFile(final String filename, final byte[] data)
             throws IOException {
         FileOutputStream fileOutputStream = new FileOutputStream(filename);
         fileOutputStream.write(data);
         fileOutputStream.close();
         return data;
     }
    public static void info(final String arg) {
        System.out.println(arg);
    }
 
    public static void info(final String[] args) {
        for (String arg : args) info("  " + arg);
    }
 }
