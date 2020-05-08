 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Year2012.com.BCHS;
 
 /**
  *
  * @author laxman
  */
 public class SafetyHelper
 {
 	private static class ThreadTask implements Runnable
 	{
 		private SafetyHelper threadExe;
 		
 		public ThreadTask(SafetyHelper threadExe)
 		{
 			this.threadExe =  threadExe;
 			
 		}
 		
 		public void run()
 		{
 			threadExe.task();
 		}
 	}
         private static SafetyHelper instance = new SafetyHelper();
         
         public static SafetyHelper getInstance()
         {
             return instance;
         }
 	
 	Thread threadRun;
 	boolean thread_keepalive;
         boolean isSafe;
         
         SafetyObject[] safetyArray;
 	
 	public SafetyHelper()
 	{
 		threadRun = new Thread(new ThreadTask(this), "Task");
 		threadRun.setPriority(Thread.NORM_PRIORITY);
 		threadRun.start();
 		
 		thread_keepalive = false;
 		
 	}
 	private void task()
 	{
 
 		while (thread_keepalive)
 		{
 			
 			for (int i = 0; i < safetyArray.length; i++)
 	
 				if (!safetyArray[i].isSafe())
 					safetyArray[i].stop();
 		}
 	}
 	
 	public void release()
 	{
 		thread_keepalive = false;
 	}
     
	/*public void addSafetyObject(SafetyObject newSafetyObject)
 	{
 		SafetyObject[] temp = new SafetyObject[safetyArray.length + 1];
 		
 		for (int i = 0; i < safetyArray.length; i++)
 			temp[i] = safetyArray[i];
 		
 		temp[temp.length-1] = newSafetyObject;
 		
 		safetyArray = temp;
	}*/	
        
 }
