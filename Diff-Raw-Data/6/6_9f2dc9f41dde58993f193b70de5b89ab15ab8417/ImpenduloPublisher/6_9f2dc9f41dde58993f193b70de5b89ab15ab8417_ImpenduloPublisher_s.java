 //Copyright (c) 2013, The Impendulo Authors
 //All rights reserved.
 //
 //Redistribution and use in source and binary forms, with or without modification,
 //are permitted provided that the following conditions are met:
 //
 //  Redistributions of source code must retain the above copyright notice, this
 //  list of conditions and the following disclaimer.
 //
 //  Redistributions in binary form must reproduce the above copyright notice, this
 //  list of conditions and the following disclaimer in the documentation and/or
 //  other materials provided with the distribution.
 //
 //THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 //ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 //WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 //DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 //ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 //(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 //LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 //ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 //(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 //SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package runner;
 
 import gov.nasa.jpf.Config;
 import gov.nasa.jpf.Error;
 import gov.nasa.jpf.report.Reporter;
 import gov.nasa.jpf.report.Statistics;
 import gov.nasa.jpf.report.XMLPublisher;
 import gov.nasa.jpf.vm.ElementInfo;
 import gov.nasa.jpf.vm.StackFrame;
 import gov.nasa.jpf.vm.ThreadInfo;
 import gov.nasa.jpf.vm.ThreadList;
 
 import java.lang.reflect.Field;
 import java.util.List;
 
 /**
  * This class extends XMLPublisher so that we can easily customise the XML
  * output generated. See
  * http://godoc.org/github.com/godfried/impendulo/tool/jpf#Report for more
  * information.
  * 
  * @author godfried
  * 
  */
 public class ImpenduloPublisher extends XMLPublisher {
 
 	public ImpenduloPublisher(Config conf, Reporter reporter) {
 		super(conf, reporter);
 	}
 
 	@Override
 	protected void publishStatistics() {
 		Statistics stat = reporter.getStatistics();
 		out.println("  <statistics>");
 		out.println("    <elapsed-time>"
 				+ String.valueOf(reporter.getElapsedTime()) + "</elapsed-time>");
 		out.println("    <new-states>" + stat.newStates + "</new-states>");
 		out.println("    <visited-states>" + stat.visitedStates
 				+ "</visited-states>");
 		out.println("    <backtracked-states>" + stat.backtracked
 				+ "</backtracked-states>");
 		out.println("    <end-states>" + stat.endStates + "</end-states>");
 		out.println("    <max-memory unit=\"MB\">" + (stat.maxUsed >> 20)
 				+ "</max-memory>");
 		out.println("  </statistics>");
 	}
 
 	@Override
 	protected void publishResult() {
 		List<Error> errors = reporter.getErrors();
 		out.println("  <errors>");
 		out.print("    <total>");
 		out.print(errors.size());
 		out.println("</total>");
 		for (int i = 0; i < errors.size(); i++) {
 			boolean foundMatch = false;
 			Error current = errors.get(i);
 			for (int j = i + 1; j < errors.size(); j++) {
 				Error check = errors.get(j);
 				if (current.getDescription().equals(check.getDescription())
 						&& current.getDetails().equals(check.getDetails())) {
 					foundMatch = true;
 					break;
 				}
 			}
 			if (foundMatch) {
 				continue;
 			}
 			out.println("    <error>");
 			out.print("      <property>");
 			out.print(current.getProperty().getClass().getName());
 			out.println("</property>");
 			out.print("      <description>");
 			out.print(current.getProperty().getExplanation());
 			out.println("      </description>");
 			Field threadsField;
 			try {
 				threadsField = current.getClass()
 						.getDeclaredField("threadList");
 				threadsField.setAccessible(true);
 				ThreadInfo[] threads = ((ThreadList) threadsField.get(current))
 						.getThreads();
 				out.println("      <threads>");
 				for (ThreadInfo ti : threads) {
 					out.println("        <thread id=\"" + ti.getId()
 							+ "\" name=\"" + ti.getName() + "\" status=\""
 							+ ti.getStateName() + "\">");
 					// owned locks
 					for (ElementInfo e : ti.getLockedObjects()) {
 						out.println("          <lock-owned object=\"" + e
 								+ "\"/>");
 					}
 					// requested locks
 					ElementInfo ei = ti.getLockObject();
 					if (ei != null) {
 						out.println("          <lock-request object=\"" + ei
 								+ "\"/>");
 					}
 					// stack frames
 					for (StackFrame frame : ti) {
 						if (!frame.isDirectCallFrame()) {
 							out.println("          <frame line=\""
									+ frame.getLine() + "\">"
 									+ frame.getStackTraceInfo() + "</frame>");
 						}
 					}
 					out.println("        </thread>");
 				}
 				out.println("      </threads>");
 			} catch (NoSuchFieldException | SecurityException
 					| IllegalArgumentException | IllegalAccessException e) {
 				e.printStackTrace();
 			}
 
 			out.println("    </error>");
 		}
 		out.println("  </errors>");
 	}
 }
