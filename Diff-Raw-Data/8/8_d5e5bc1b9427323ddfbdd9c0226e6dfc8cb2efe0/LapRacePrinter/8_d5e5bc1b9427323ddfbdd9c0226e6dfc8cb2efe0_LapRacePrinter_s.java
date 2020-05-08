 package enduro.racer.printer;
 
 import java.util.HashMap;
 import java.util.Iterator;
 
 import enduro.racedata.Time;
 import enduro.racer.Racer;
 
 public class LapRacePrinter implements RacerPrinter {
 
 	private int numLapse, extraRunnerInformation;
 	private String extraInformation = "";
 	
 	public LapRacePrinter(String[] extraInformation) {
 		numLapse = 3;
 		extraRunnerInformation = extraInformation.length;
 		
 		for(int i = 0; i < extraRunnerInformation; i++) {
 			this.extraInformation += extraInformation[i] + "; ";
 		}
 	}
 	
 	public String printTopInformation() {
 		StringBuilder out = new StringBuilder();
 		out.append(extraInformation);
 		
 		out.append("#Varv; TotalTid");
 		
 		for(int i = 1; i <= numLapse; i++) {
 			out.append("; Varv");
 			out.append(i);
 		}
 		out.append("; Start");
 		for(int i = 1; i < numLapse; i++) {
 			out.append("; Varvning");
 			out.append(i);
 		}
		out.append("; Ml");
 		return out.toString();
 	}
 	
 	public String print(Racer r, HashMap<String, String> extraInformation) {
 		StringBuilder errorTrail = new StringBuilder();
 		StringBuilder out = new StringBuilder();
 		
 		printRunnerInformation(r, out, errorTrail);
 		
 		printNumLapses(r, out, errorTrail);
 		
 		printTotalTime(r, out, errorTrail);
 		
 		printLapses(r, out, errorTrail);
 		
 		printStart(r, out, errorTrail);
 		
 		printVarvning(r, out, errorTrail);
 		
 		printGoal(r, out, errorTrail);
 		
 		//removes a trailing "; " that the reference result file does not have.
 		out.delete(out.length()-2, out.length());
 		
 		if(errorTrail.length() > 0) {
 			out.append("; ");
 			out.append(errorTrail.toString());
 			out.delete(out.length()-1, out.length());
 		}
 		
 		return out.toString();
 	}
 	
 	/**
 	 * this function prints the last finish time. naive and simple solution. risk of
 	 * printing same values multiple times. should be fixed.
 	 * 
 	 * @param r the racer that is printed
 	 * @param out the stringbuilder class that will in the end supply the result.
 	 * @param errorTrail the stringbuilder class that will summarize the errors collected.
 	 */
 	private void printGoal(Racer r, StringBuilder out, StringBuilder errorTrail) {
 		if(r.finishTimes.size() > 0 && r.startTimes.size() > 0)
 			out.append(r.finishTimes.last());
 		out.append("; ");
 	}
 
 	/**
 	 * this function prints the actual lapse times of the actual race.
 	 * Padding with "; " if too few finish times exists.
 	 * Does not write to the errorTrail.
 	 * Does not print the last "official" lapse (numLapse), but checks against numLapse-1
 	 * as the next value asked is the finish time.
 	 * 
 	 * @param r the racer that is printed
 	 * @param out the stringbuilder class that will in the end supply the result.
 	 * @param errorTrail the stringbuilder class that will summarize the errors collected.
 	 */
 	private void printVarvning(Racer r, StringBuilder out, StringBuilder errorTrail) {
 		int i = 0;
 		Iterator<Time> itr = r.finishTimes.iterator();
 		for(; i < Math.min(numLapse-1, r.finishTimes.size());i++) {
 			out.append(itr.next());
 			out.append("; ");
 		}
 		
 		
 		if(i < numLapse)
 			for(; i < numLapse-1; i++) {
 				out.append("; ");
 			}
 	}
 
 	/**
 	 * this function prints the start number. if too many starttimes exists they are printed
 	 * to the error trail.
 	 * 
 	 * "flera starttider?" is printed if anything is written to the error trail. 
 	 * @param r the racer that is printed
 	 * @param out the stringbuilder class that will in the end supply the result.
 	 * @param errorTrail the stringbuilder class that will summarize the errors collected.
 	 */
 	private void printStart(Racer r, StringBuilder out, StringBuilder errorTrail) {
 		if(r.startTimes.size() > 0) {
 			out.append(r.startTimes.first());
 			out.append("; ");
 			if(r.startTimes.size()!=1) {
 				errorTrail.append("Flera starttider? " );
 				Iterator<Time> itr = r.startTimes.iterator();
 				itr.next();
 				while(itr.hasNext())
 					errorTrail.append(itr.next() + " ");
 			}
 		} else {
 			out.append("--:--:--; ");
 			errorTrail.append("Start? ");
 		}
 	}
 
 	/**
 	 * this function prints all lapses up and including the lapse limit parameter set in the constructor.
 	 * If there are more lapses than allowed it adds to the error trail, if there are fewer lapses than expexted
 	 * padding of the type "; " is added until the number is complete.
 	 * 
 	 * "for manga varv" is printed if anything is written to the error trail. 
 	 * @param r the racer that is printed
 	 * @param out the stringbuilder class that will in the end supply the result.
 	 * @param errorTrail the stringbuilder class that will summarize the errors collected.
 	 */
 	private void printLapses(Racer r, StringBuilder out, StringBuilder errorTrail) {
 		int i = 0;
 		if(r.startTimes.size() > 0) {
 			Time before = r.startTimes.first();
 			Iterator<Time> itr = r.finishTimes.iterator();
 			
 			for(; i < Math.min(r.finishTimes.size(), numLapse); i++) {
 				Time next = itr.next();
 				
 				Time diff = before.getTotalTime(next);
 				
 				out.append(diff);
 				
 				if(diff.compareTo(new Time("00.15.00")) < 0)
					errorTrail.append("Omjlig varvtid? ");
 				
 				out.append("; ");
 				before = next;
 			}
 			if(numLapse < r.finishTimes.size()) {
				errorTrail.append("fr mnga varv ");
 				for(; i < r.finishTimes.size(); i++) {
 					errorTrail.append(itr.next());
 					errorTrail.append(" ");
 				}
 			} else if(numLapse > r.finishTimes.size()) {
 				for(; i < numLapse; i++)
 					out.append("; ");
 			}
 		} else {
 			for(; i < numLapse; i++)
 				out.append("; ");
 		}
 		
 		
 		
 	}
 
 	/**
 	 * this function calculates the total time given a starttime and finishtime exists.
 	 * @param r the racer that is printed
 	 * @param out the stringbuilder class that will in the end supply the result.
 	 * @param errorTrail the stringbuilder class that will summarize the errors collected.
 	 */
 	private void printTotalTime(Racer r, StringBuilder out, StringBuilder errorTrail) {
 		if(r.startTimes.size() > 0) {
 			if(r.finishTimes.size() > 0) {
 				out.append(r.startTimes.first().getTotalTime(r.finishTimes.last()));
 				out.append("; ");
 			} else {
 				out.append("--:--:--; ");
 			}
 		} else {
 			out.append("--:--:--; ");
 		}
 	}
 
 	/**
 	 * this function calculates the runner name and all extra information
 	 * that was inserted with the user. Unknown information is padded with ;<whitespace>
 	 * this is a "dumb" implementation as it does not map data which data is printed, it just uses the
 	 * racer arraylist.
 	 * @param r the racer that is printed
 	 * @param out the stringbuilder class that will in the end supply the result.
 	 * @param errorTrail the stringbuilder class that will summarize the errors collected.
 	 */
 	private void printRunnerInformation(Racer r, StringBuilder out, StringBuilder errorTrail) {
 		int i = 0;
 		
 		for(; i < r.racerInformation.size(); i++) {
 			out.append(r.racerInformation.get(i));
 			out.append("; ");
 		}
 		for(; i < extraRunnerInformation; i++) {
 			out.append("; ");
 		}
 	}
 	
 	/**
 	 * calculates the number of lapses that the contestant has run.
 	 * @param r the racer that is printed
 	 * @param out the stringbuilder class that will in the end supply the result.
 	 * @param errorTrail the stringbuilder class that will summarize the errors collected.
 	 */
 	private void printNumLapses(Racer r, StringBuilder out, StringBuilder errorTrail) {
 		if(r.startTimes.size() > 0)
 			out.append(r.finishTimes.size());
 		else
 			out.append("0");
 		out.append("; ");
 	}
 	
 }
