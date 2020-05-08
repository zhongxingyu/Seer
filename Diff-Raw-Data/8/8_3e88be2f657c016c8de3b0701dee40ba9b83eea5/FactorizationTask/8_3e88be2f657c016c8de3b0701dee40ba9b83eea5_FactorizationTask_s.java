 package com.example.multithread;
 
 import java.math.BigInteger;
 import java.util.ArrayList;
 import com.example.algorithms.FactorizationAlgo;
 import android.os.AsyncTask;
import android.util.Log;
 
 class FactorizationTask extends
 		AsyncTask<Pair<BigInteger, FactorizationAlgo>, Void, Void> {
 
 	private OnFactorizationTaskCompleteListener callback;
 	private TaskCounter taskCounter;
 	boolean taskComplete = false;
 	private String factorizationResult;
 
 	FactorizationTask(OnFactorizationTaskCompleteListener callback,
 			TaskCounter taskCounter) {
 		this.callback = callback;
 		this.taskCounter = taskCounter;
 	}
 
 	@Override
 	protected Void doInBackground(Pair<BigInteger, FactorizationAlgo>... arg0) {
 		
 		
 		synchronized (taskCounter) {
 			taskCounter.addTask("Thread " + Thread.currentThread().getId()
 					+ ". Starting factorization: " + arg0[0].first);
 		}
 		factorizationResult = getMultiplicationString(arg0[0].second
 				.run(arg0[0].first));
 
 		completeTask(false);
 		return null;
 	}
 
 	public void completeTask(boolean fail) {
 		synchronized (taskCounter) {
 
 			if (!taskComplete) {
 				taskCounter.removeTask("Thread "
 						+ Thread.currentThread().getId()
 						+ ". factorization found: " + factorizationResult);
 			//	if(!fail)
 				   this.callback.onTaskComplete(factorizationResult);
 				taskComplete = true;
 			}
 		}
 
 	}
 
 	/**
 	 * get a string to be displayed as a result
 	 * 
 	 * @param factors
 	 *            list of factors
 	 * @return factors separated by '*'
 	 */
 	private String getMultiplicationString(ArrayList<BigInteger> factors) {
 		StringBuilder result = new StringBuilder();
 		boolean firstFactor = true;
 		for (BigInteger factor : factors) {
 			if (!firstFactor) {
 				result.append("*");
 			}
 			result.append(factor.toString());
 			firstFactor = false;
 		}
 		return result.toString();
 	}
 
 	@Override
 	protected void onCancelled() {
 		this.callback.onTaskComplete(null);
 	}
 }
