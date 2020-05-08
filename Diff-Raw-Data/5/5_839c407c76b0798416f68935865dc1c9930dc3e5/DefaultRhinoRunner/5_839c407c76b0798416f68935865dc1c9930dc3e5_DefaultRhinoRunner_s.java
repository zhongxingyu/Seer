 package org.eclipse.dltk.rhino.dbgp;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.Scriptable;
 
 public class DefaultRhinoRunner {
 
 	public void run(String[] args) {
 		Context cx = Context.enter();
 		if (args.length > 1) {
 			String host = args[1];
			String porg = args[2];
 			DBGPDebugger debugger;
 			try {
				final Socket s = new Socket(host, Integer.parseInt(porg));
 				debugger = new DBGPDebugger(s, args[0], args[3], cx);
 
 				debugger.start();
 				cx.setDebugger(debugger, null);
 
 				Scriptable scope = cx.initStandardObjects();
 				extraInit(scope, cx);
 				synchronized (debugger) {
 					try {
 						debugger.isInited = true;
 						debugger.wait();
 					} catch (InterruptedException e) {
 						throw new IllegalStateException();
 					}
 				}
 				try {
 					try {
 						Thread.sleep(200);
 					} catch (InterruptedException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 					cx.setGeneratingDebug(true);
 					cx.setOptimizationLevel(-1);
 					cx.evaluateReader(scope, new FileReader(args[0]),new File(args[0]).getAbsolutePath(),
 							0, null);
 
 				} catch (FileNotFoundException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				debugger.notifyEnd();
 			} catch (NumberFormatException e) {
 				e.printStackTrace();
 			} catch (UnknownHostException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		} else {
 			Scriptable scope = cx.initStandardObjects();
 			try {
 				cx.evaluateReader(scope, new FileReader(args[0]), args[0], 0,
 						null);
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 
 	}
 
 	protected void extraInit(Scriptable scope, Context cx) {
 
 	}
 }
