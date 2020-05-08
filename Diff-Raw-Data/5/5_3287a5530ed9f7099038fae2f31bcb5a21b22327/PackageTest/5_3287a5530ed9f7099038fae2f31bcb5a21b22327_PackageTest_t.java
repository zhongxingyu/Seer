 package ruffkat.hombucha.store;
 
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 import ruffkat.hombucha.measure.Measurements;
 import ruffkat.hombucha.model.Container;
 import ruffkat.hombucha.model.Ferment;
 import ruffkat.hombucha.model.Friend;
 import ruffkat.hombucha.model.Ingredient;
 import ruffkat.hombucha.model.Item;
 import ruffkat.hombucha.model.Mushroom;
 import ruffkat.hombucha.model.Online;
 import ruffkat.hombucha.model.Processing;
 import ruffkat.hombucha.model.Recipe;
 import ruffkat.hombucha.util.Dates;
 
 import javax.measure.quantity.Mass;
 import javax.time.Duration;
 import javax.time.Instant;
 import javax.time.TimeSource;
 import java.math.BigDecimal;
 import java.net.URL;
 import java.util.Calendar;
 import java.util.Date;
 
 public class PackageTest extends FunctionalTest {
 
     @Autowired
     private TimeSource timeSource;
 
     @Autowired
     private Sources sources;
 
     @Autowired
     private Containers containers;
 
     @Autowired
     private Items items;
 
     @Autowired
     private Mushrooms mushrooms;
 
     @Autowired
     private Recipes recipes;
 
     @Autowired
     private Ferments ferments;
 
     @Autowired
     private Samples samples;
 
     @Test
     public void testBuildInventory()
             throws Exception {
         // Add sources
         Online westelm = sources.create(Online.class);
         westelm.setName("West Elm");
         westelm.setUrl(new URL("http://www.westelm.com"));
         entityManager.persist(westelm);
 
         Online naturalGrocers = sources.create(Online.class);
         naturalGrocers.setName("Natural Grocers");
         naturalGrocers.setUrl(new URL("http://www.naturalgrocers.com"));
         entityManager.persist(naturalGrocers);
 
         Online rishi = sources.create(Online.class);
         rishi.setName("Rishi Tea");
         rishi.setUrl(new URL("http://www.rishi.com"));
         entityManager.persist(rishi);
 
         Friend friend = sources.create(Friend.class);
         friend.setName("Zoid Berg");
         friend.setEmail("zoid@berg.com");
         friend.setPhone("111-111-1111");
         entityManager.persist(friend);
 
         // Add containers
         Container containerA = containers.create();
         containerA.setVolume(Measurements.volume("10.0 l"));
         containerA.setName("Green Tea Container");
         containerA.setReceived(Dates.date(Calendar.JUNE, 1, 2011));
         containerA.setSource(naturalGrocers);
         entityManager.persist(containerA);
 
         Container containerB = containers.create();
         containerB.setVolume(Measurements.volume("6.0 l"));
         containerB.setName("Ancient Pu-Erh Tea Container");
         containerB.setReceived(Dates.date(Calendar.JUNE, 11, 2011));
         containerB.setSource(westelm);
         entityManager.persist(containerB);
 
         Container containerC = containers.create();
         containerC.setVolume(Measurements.volume("6.0 l"));
         containerC.setName("Flavored Pu-Erh Tea Container");
         containerC.setReceived(Dates.date(Calendar.JUNE, 11, 2011));
         containerC.setSource(westelm);
         entityManager.persist(containerC);
 
         // Add some items
         Item<Mass> sugar = items.create(Mass.class);
         sugar.setName("Turbinado Sugar");
         sugar.setSource(naturalGrocers);
         sugar.setPrice(new BigDecimal("2.99"));
         sugar.setUnit(Measurements.mass("1 kg"));
         entityManager.persist(sugar);
 
         Item<Mass> tea = items.create(Mass.class);
         tea.setName("Ancient Emerald Lily");
         tea.setSource(rishi);
         tea.setPrice(new BigDecimal("25.99"));
         tea.setUnit(Measurements.mass("25 g"));
        entityManager.persist(tea);
 
         // Add a mother
         Mushroom mother = mushrooms.create();
         mother.setName("Squiddy");
         mother.setReceived(Dates.date(Calendar.MAY, 7, 2011));
         mother.setSource(friend);
         entityManager.persist(mother);
 
         // Add the babies
         Mushroom baby = mushrooms.create();
         baby.setName("Eggshell");
         baby.setMother(mother);
         baby.setReceived(Dates.date(Calendar.MAY, 7, 2011));
         baby.setSource(friend);
         entityManager.persist(baby);
 
         // Add recipes
         Recipe recipe = recipes.create();
         recipe.setName("MaltBrewCha");
         recipe.setYields(Measurements.volume("6.0 l"));
         recipe.setInstructions("Boil water, steep tea, add sugar, cool down");
         recipe.addIngredient(new Ingredient<Mass>(sugar, Measurements.mass("500 g")));
        recipe.addIngredient(new Ingredient<Mass>(tea, Measurements.mass("10 g")));
         entityManager.persist(recipe);
 
         Instant now = timeSource.instant();
         Instant later = now.plus(Duration.standardDays(10));
 
         // Start a batch
         Ferment batch = ferments.create();
         batch.setName("MaltBrewCha Run 1");
         batch.setVolume(Measurements.volume("6.0 l"));
         batch.setContainer(containerA);
         batch.setProcessing(Processing.BATCH);
         batch.setMushroom(mother);
         batch.setRecipe(recipe);
         batch.setStart(new Date(now.toEpochMillisLong()));
         batch.setStop(new Date(later.toEpochMillisLong()));
         entityManager.persist(batch);
     }
 }
