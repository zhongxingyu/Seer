 package testspring.testspring;
 
 import org.aspectj.lang.ProceedingJoinPoint;
 import org.springframework.beans.factory.annotation.Autowired;
 
 public class MeasureTime {
 	
 	@Autowired
 	private StoreRecord store;
 	public void recordStartTime()
 	{
 		System.out.println("Record Start Time!");
 	}
 
 	public void recordRuntime(ProceedingJoinPoint joinpoint)
 	{
 		try {
 			System.out.println("Before Method");
 			long start = System.currentTimeMillis();
 
 			joinpoint.proceed();
 
 			long end = System.currentTimeMillis();
 			System.out.println("After Method");
 			System.out.println("The performance took " + (end - start)
 					+ " milliseconds.");
 			//create RunTimeRecord
			RuntimeRecord record = new RuntimeRecord(Long.toString(start), Long.toString(end), joinpoint.getTarget().getClass().getName(), joinpoint.getSignature().getName());
 			//SerializeObject serialize = new JSONSerialization();
 			System.out.println(record);
 			store.doStore(record);
 		} catch (Throwable e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 }
