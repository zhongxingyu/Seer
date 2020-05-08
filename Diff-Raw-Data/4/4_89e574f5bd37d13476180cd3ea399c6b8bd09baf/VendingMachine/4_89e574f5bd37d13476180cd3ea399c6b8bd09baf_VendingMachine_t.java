 import akka.stm.Ref;
 import akka.stm.Atomic;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 public class VendingMachine implements Runnable {
 	 private final long MAXLEVEL = 6;
 	 private int numDays;
 	  final Ref<Long> cookies = new Ref<Long>(MAXLEVEL);
 	  final Ref<Long> candy = new Ref<Long>(MAXLEVEL);
 	  
	  final Ref<Long> cookiesBought = new Ref<Long>(0L);
	  final Ref<Long> candyBought = new Ref<Long>(0L);
 	 
 	  final Ref<Boolean> keepRunning = new Ref<Boolean>(true);
 //	  private ScheduledExecutorService replenishTimer =
 //	    Executors.newScheduledThreadPool(10);
 
 
 	  private VendingMachine(int numDays) {this.numDays = numDays;}
 	  
 	  private void init() {   
 //	    replenishTimer.schedule(new Runnable() {
 //	      public void run() { 
 //	        replenish();
 //	        if (keepRunning.get()) replenishTimer.schedule(
 //	          this, 1, TimeUnit.SECONDS);
 //	      }
 //	    }, 1, TimeUnit.SECONDS);\
 		  replenish();
 	  }
 	  
 	  public static VendingMachine create(int numDays) {
 	    final VendingMachine vend = new VendingMachine(numDays);
 	    vend.init();
 	    return vend;
 	  }
 
 	  public void stopVending() { keepRunning.swap(false); }
 
 	  public long getCookiesAvailable() { return cookies.get(); }
 	  public long getCandyAvailable() { return candy.get(); }
 
 	  public long getCookiesBought() { return cookiesBought.get(); }
 	  public long getCandyBought() { return candyBought.get(); }
 
 	  
 	  
 	  public boolean buyCookies(final long units) {
 		    return  new Atomic<Boolean>() {
 		      public Boolean atomically() {
 		        long cookiesLeft = cookies.get();
 		        if(units > 0 && cookiesLeft >= units) {
 		          cookies.swap(cookiesLeft - units);
 		          cookiesBought.swap(cookiesBought.get() + 1);
 		          return true;          
 		        } else {
 		          return false;
 		        }
 		      }  
 		    }.execute();
 		  }
 	  
 	  public boolean buyCandy(final long units) {
 		    return  new Atomic<Boolean>() {
 		      public Boolean atomically() {
 		        long candyLeft = candy.get();
 		        if(units > 0 && candyLeft >= units) {
 		          candy.swap(candyLeft - units);
 		          candyBought.swap(candyBought.get() + 1);
 		          return true;          
 		        } else {
 		          return false;
 		        }
 		      }  
 		    }.execute();
 		  }
 
 	  
 	  private void replenish() {
 		    new Atomic() {
 		      public Object atomically() {
 		        long cookiesLeft = cookies.get();
 		        long candyLeft  = candy.get();
 		        while (cookiesLeft < MAXLEVEL) cookies.swap(cookiesLeft + 1);
 		        while (candyLeft < MAXLEVEL) candy.swap(cookiesLeft + 1);
 		        return null;
 		      }
 		    }.execute();
 		  }
 
 	@Override
 	public void run() {
 		for (int i = 0; i < numDays/3; i++)
 		{
 			replenish();
 			try {
 				Thread.sleep(3000);
 			} catch (InterruptedException e) {
 				System.err.println("Sleep had an error");
 				e.printStackTrace();
 			}
 		}
 		
 	}
 }
