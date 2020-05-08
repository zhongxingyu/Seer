 package org.weymouth.demo;
 
 import org.weymouth.demo.model.Data;
 import org.weymouth.demo.model.Results;
 import org.weymouth.demo.work.Input;
 import org.weymouth.demo.work.Output;
 import org.weymouth.demo.work.Process;
 
 public class Main {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 
 		Input in = new Input();
 		Process worker = new Process();
 		Output out = new Output();
 		
 		Data data;
 		
 		while ((data = in.next()) != null){
 			Results r = worker.process(data);
 			out.report(r);
 		}
 	}
 
 }
