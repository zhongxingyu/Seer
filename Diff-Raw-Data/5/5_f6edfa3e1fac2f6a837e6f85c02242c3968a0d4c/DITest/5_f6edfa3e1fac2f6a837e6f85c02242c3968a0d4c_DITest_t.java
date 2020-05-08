 package pl.playbit.di;
 
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 import pl.playbit.di.car.Car;
 import pl.playbit.di.car.better.BetterCar;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertNotNull;
 
 public class DITest {
 
     @DataProvider
     public Object[][] shouldInjectDependencies() {
         return new Object[][] {
                 //Car class,      name,     speed, engine name,  carburetor, exhaust
                 {Car.class,       "Crow",   200,   "Kaalakiota", "Good",      null},
                 {BetterCar.class, "Raptor", 220,   "Lai Dai",    "Good",     "Caldari"}
         };
     }
 
     @Test(dataProvider = "shouldInjectDependencies")
     public <T extends Car> void shouldInjectDependencies(Class<T> clazz, String name, int maxSpeed, String engineVendor,
                                              String carburetorVendor, String exhaustVendor) {
         T car = null;
         try {
             car = Context.create(clazz);
         } catch (Exception e) {
             e.printStackTrace();
         }
         assertNotNull(car);
         assertNotNull(car.getEngine());
         assertNotNull(car.getEngine().getCarburetor());
         assertNotNull(car.getEngine().getExhaust());
 
         assertEquals(name, car.getName());
         assertEquals(maxSpeed, car.getMaxSpeed());
         assertEquals(engineVendor, car.getEngine().getName());
         assertEquals(carburetorVendor, car.getEngine().getCarburetor().getName());
         assertEquals(exhaustVendor, car.getEngine().getExhaust().getName());
     }
 
 }
