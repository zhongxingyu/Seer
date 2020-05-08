 import java.util.ArrayList;
 
 
 public class ArgCheck {
 	
 	public static void check(String[] argss){
 		ArrayList<String> args = new ArrayList<String>();
 		for(String s : argss) args.add(s);
 		
 		if(args.contains("randommu")){
 			Constants._murand = true;
 			if(args.indexOf("randommu") < args.size() - 2){
 				Constants.randMuStart = Double.parseDouble(args.get(args.indexOf("randommu")+1));
				Constants.randMuStart = Double.parseDouble(args.get(args.indexOf("randommu")+2));
 			}
 		}
 		if(args.contains("mu")){
 			Constants.muCheck = true;
 			if(args.indexOf("mu") < args.size() - 1){
 				Constants.muIncUp = Double.parseDouble(args.get(args.indexOf("mu")+1));
 			}
 		}
 		if(args.contains("constantep")){
 			Constants.ConstantEp = true;
 			if(args.indexOf("constantep") < args.size() - 1){
 				Main.monitor.constants._epsilon = Double.parseDouble(args.get(args.indexOf("constantep") + 1 ));
 			}
 		}
 		if(args.contains("repulse")){
 			Constants.Repulsive = true;
 			if(args.indexOf("repulse") < args.size() - 1){
 				Constants.repuslivePer = Integer.parseInt(args.get(args.indexOf("repulse") + 1 ));
 			}
 		}
 		if(args.contains("nodes")){
 			Constants.resetVals(Integer.parseInt(args.get(args.indexOf("nodes") + 1)),
 					Integer.parseInt(args.get(args.indexOf("nodes") + 2)));
 		}
 		if(args.contains("nomigrate")){
 			Constants.migrateSwitch = false;
 		}
 		if(args.contains("numthreads")){
 			Constants.numThreads = Integer.parseInt(args.get(args.indexOf("numthreads") + 1));
 		}
 		if(args.contains("verbose")){
 			Constants.verbose = true;
 			if(args.indexOf("verbose") < args.size() - 1){
 				if(args.get(args.indexOf("verbose") + 1) == "thread"){
 					Constants.numThreadsVerbose = 0;
 					Constants.threadVerbose = Integer.parseInt(args.get(args.indexOf("verbose") + 2));
 				}
 				else if(args.get(args.indexOf("verbose") + 1) == "num"){
 					Constants.numThreadsVerbose = Integer.parseInt(args.get(args.indexOf("verbose") + 2));
 				}
 			}
 		}
 		if(args.contains("simstep")){
 			Constants._SIM_epsilon_step = Double.parseDouble(args.get(args.indexOf("simstep") + 1));
 		}
 		if(args.contains("trials")){
 			Constants._trials = Integer.parseInt(args.get(args.indexOf("trials") + 1));
 		}
 		if(args.contains("exdegree")){
 			Constants._avgdegree = Integer.parseInt(args.get(args.indexOf("exdegree") + 1));
 		}
 		if(args.contains("dynamicgroups")){
 			Constants.DynamicGroups = true;
 		}
 		if(args.contains("groupratio")){
 			Constants.groupRatio = Integer.parseInt(args.get(args.indexOf("groupratio") + 1));
 		}
 		if(args.contains("groups")){
 			Constants._groups = Integer.parseInt(args.get(args.indexOf("groups") + 1));
 		}
 		if(args.contains("setmu")){
 			Constants._mu = Integer.parseInt(args.get(args.indexOf("setmu") + 1));
 		}
 		if(args.contains("output")){
 			Constants._OUTPUT_PATH = args.get(args.indexOf("output") + 1);
 		}
 	}
 }
