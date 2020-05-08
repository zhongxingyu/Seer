 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 
 public class TaskAssignment {
 	public static int numTasks = 0;
 	public static int numMachines = 0;
 	public static ArrayList<Task> tasks = new ArrayList<Task>();
 	public static ArrayList<Double> machineSpeeds = new ArrayList<Double>();
 	public static ArrayList<Machine> machines = new ArrayList<Machine>();
 	
 	public static void getInput(String filename) throws IOException {
 		BufferedReader reader = new BufferedReader(new FileReader(filename));
 		String line = reader.readLine();
 		numTasks = Integer.parseInt(line);
 		line = reader.readLine();
 		numMachines = Integer.parseInt(line);
 		line = reader.readLine();
 		String[] taskStrings = line.split(" ");
 		for (int i = 0; i < taskStrings.length; i++) {
 			Task task = new Task(i, Integer.parseInt(taskStrings[i]));
 			tasks.add(task);
 		}
 		line = reader.readLine();
 		String[] machineStrings = line.split(" ");
 		for (int i = 0; i < machineStrings.length; i++) {
 			ArrayList<Task> tasks = new ArrayList<Task>();
 			Machine machine = new Machine(i, tasks, Integer.parseInt(machineStrings[i]));
 			machines.add(machine);
 		}
 		reader.close();
 	}
 	
 	public static double algowars() {
		machines.get(0).getTasks().add(tasks.get(0));
		tasks.remove(0);
 
 		double maxTime = 0;
 		for(int i = 0; i < tasks.size(); i++) {
 			int minIndex = 0;
 			double minVal = Double.MAX_VALUE;
 			for (int j = 0; j < machines.size(); j++) {
 				double time = tasks.get(i).getProcessing_time()/machines.get(j).getSpeed();
 				double machineMax = time + machines.get(j).calcCurrentTime();
 				if(machineMax < minVal)
 				{
 					minVal = machineMax;
 					minIndex = j;
 					maxTime = minVal;
 				}
 				
 			}
 			machines.get(minIndex).getTasks().add(tasks.get(i));
 		}
 		return maxTime;
 	}
 
 	public static void main(String[] args) throws IOException {
		getInput("input16.txt");
 		Collections.sort(tasks, new Comparator<Task>() {
 			public int compare(Task o1, Task o2) {
 				return o1.getProcessing_time().compareTo(o2.getProcessing_time());
 			}
 		});
 		Collections.sort(machineSpeeds);
 		double solution = algowars();
 		
 		for (int i = 0; i < machines.size(); i++){
 			ArrayList<Task> finalTasks = machines.get(i).getTasks();
 			for(Task j : finalTasks) {
 				System.out.print(j.getTask_id() + " ");
 			}
 			System.out.println();
 		}
 		System.out.println(Math.round(solution*100)/100.0);
 	}
 
 }
 
 
