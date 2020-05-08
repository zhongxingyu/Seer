 package com.leafdigital.hawthorn.loadtest;
 
 /** Test thread. */
 public final class TestThread extends Thread
 {
 	private LoadTest test;
 	private int index;
 
 	/**
 	 * @param test Main class
	 * @param index Index of thread
 	 */
 	public TestThread(LoadTest test, int index)
 	{
 		super("Load test thread");
 		this.test = test;
 		this.index = index;
 		start();
 	}
 
 	@Override
 	public void run()
 	{
 		while(true)
 		{
 			EventSource user = test.getNextEvent();
 			if(user == null)
 			{
 				return;
 			}
 			user.event(index);
 		}
 	}
 }
