 /*
  * Created on Oct 19, 2006
  */
 package zz.utils;
 
 public abstract class Future<T> extends Thread
 {
 	private T itsResult;
 	private Throwable itsException;
 	private boolean itsDone = false;
 	
 	public Future()
 	{
 		start();
 	}
 
 	@Override
 	public synchronized final void run()
 	{
 		try
 		{
 			itsResult = fetch();
 		}
 		catch (Throwable e)
 		{
 			itsException = e;
 		}
 		itsDone = true;
 		notifyAll();
 	}
 	
 	protected abstract T fetch() throws Throwable;
 	
 	public synchronized T get()
 	{
 		try
 		{
 			while (! itsDone) wait();
 			
 			if (itsException != null) throw new RuntimeException(itsException);
 			return itsResult;
 		}
 		catch (InterruptedException e)
 		{
 			throw new RuntimeException(e);
 		}
 	}
 	
 	public boolean isDone()
 	{
 		return itsDone;
 	}
 }
