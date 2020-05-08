 package service;
 
 import administration.domain.Bill;
 import administration.domain.Car;
 import administration.domain.Driver;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.config.ClientConfig;
 import com.sun.jersey.api.client.config.DefaultClientConfig;
 import java.io.Serializable;
 import java.util.Collection;
 import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
 import javax.enterprise.context.SessionScoped;
 import javax.ws.rs.core.MediaType;
 import sun.reflect.generics.reflectiveObjects.NotImplementedException;
 
@Stateless
 public class RekeningrijdersService implements Serializable {
 
     private WebResource service;
 
     public void payBill(Long billID) {
         Bill bill = getBill(billID);
         bill.pay();
         service.path("resources").path("bill").put(Bill.class, bill);
     }
 
     public void subscribeTrafficJamInfo() {
         throw new NotImplementedException();
     }
 
     public void editDriver(Driver driver) {
         service.path("resources").path("driver").put(Driver.class, driver);
     }
 
     public Driver getDriver(int bsn) {
         Driver driver = service.path("resources").path("driver").path(Integer.toString(bsn))
                 .accept(MediaType.APPLICATION_JSON).get(Driver.class);
         return driver;
     }
 
     public void register(Driver driver) {
         service.path("resources").path("driver").post(Driver.class, driver);
     }
    
     public void login() {
         throw new NotImplementedException();
     }
     
     public void logout() {
         throw new NotImplementedException();
     }
 
     public Collection<Bill> getBillsFromDriver(int bsn) {
         Driver driver = getDriver(bsn);
         return driver.getBills();
     }
 
     public Bill getBill(Long billID) {
         Bill bill = service.path("resources").path("bill").path(Long.toString(billID))
                 .accept(MediaType.APPLICATION_JSON).get(Bill.class);
 
         return bill;
     }
 
     public Collection<Car> getCarsFromDriver(int bsn) {
         Driver driver = getDriver(bsn);
         return driver.getCars();
     }
 
     public Car getCar(String licensePlate) {
         Car car = service.path("resources").path("car").path(licensePlate)
                 .accept(MediaType.APPLICATION_JSON).get(Car.class);
         return car;
     }
 
     public void editCar(Car car) {
         service.path("resources").path("var").put(Car.class, car);
     }
 
     @PostConstruct
     public void postConstruct() {
         ClientConfig config = new DefaultClientConfig();
         Client client = Client.create(config);
         service = client.resource("http://localhost:8080/Administration/");
     }
 }
