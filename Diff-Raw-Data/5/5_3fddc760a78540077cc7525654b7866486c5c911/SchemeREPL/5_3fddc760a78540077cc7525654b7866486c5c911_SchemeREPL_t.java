 package com.folone.replscheme;
 
 import com.folone.Evaluator;
 
 import jscheme.JScheme;
 import android.app.Service;
 import android.content.Intent;
 import android.os.IBinder;
 import android.os.RemoteException;
 
 public class SchemeREPL extends Service {
 
 	private final Evaluator.Stub evaluator = new Evaluator.Stub() {
 		
	    private JScheme scheme = new JScheme();
	    
 		public String evaluate(String script) throws RemoteException {
			
 			String result = "Something went wrong.";
 			try {
 				result = scheme.eval(script).toString();
 			} catch (Exception e) {
 				result = e.getMessage();
 			}
 			return result;
 		}
 	};
 	
 	@Override
 	public IBinder onBind(Intent intent) {
 		return evaluator;
 	}
 
 }
