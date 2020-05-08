 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 
 public class AssessAllErrors {
 
     public static void main(String[] args) {
 
 	String alphaValuesFile = args[0];
 	int repl = Integer.parseInt(args[1]);
 	double maxt = Double.parseDouble(args[2]);
 	String prefix = args[3];
	String[] workloads = {"A1", "A2", "A3", "B1", "B2", "B3", "C1", "C2", "C3"};
 
 	Set<AverageError> set = new TreeSet<AverageError>();
 	List<Alpha> alphas = getAlphas(alphaValuesFile);
 
 	for (Alpha alpha : alphas) {
 	    double alp = alpha.alpha;
 	    List<Double> allErrors = new ArrayList<Double>();
 	    for (String workload : workloads) {
 		String workloadFile = prefix + "workload-" + workload;
 		String measuredExperimentsFile = "experiments-measured-" + workload;
 		List<Double> newValues = Combinations.assessError(workloadFile, repl, alp, maxt, measuredExperimentsFile);
 		allErrors.addAll(newValues);
 	    }
 
 	    double avg = 0.0;
 	    for (double val : allErrors) {
 		avg += val;
 	    }
 	    avg = avg / allErrors.size();
 	    double stdev = 0.0;
 	    for (double val : allErrors) {
 		stdev += Math.pow(avg - val,2);
 	    }
 	    stdev = Math.sqrt(stdev / allErrors.size());
 	    AverageError error = new AverageError();
 	    error.avg = avg;
 	    error.stdev = stdev;
 	    error.alp = alpha;
 	    set.add(error);
 	}
 	
 	int i = 0;
 	for (AverageError avg : set) {
 	    i++;
 	    System.out.println(i + "\t" + avg.avg + "\t" + avg.stdev + "\t" + avg.alp.alpha + "\t" + avg.alp.type);
 	}
 
     }
 
     private static List<Alpha> getAlphas(String file) {
 	List<String> lines = readFileContent(file);
 	List<Alpha> result = new ArrayList<Alpha>();
 	for (String line : lines) {
 	    String[] parts = line.split(" ");
 	    Alpha alp = new Alpha();
 	    alp.type = parts[2] + " " + parts[3];
 	    alp.alpha = Double.parseDouble(parts[4]);
 	    result.add(alp);
 	}
 	return result;
     }
 
     public static class Alpha {
 	public double alpha;
 	public String type;
     }
 
     public static class AverageError implements Comparable<AverageError> {
 	public double avg;
 	public double stdev;
 	public Alpha alp;
 
 	@Override
 	public int compareTo(AverageError other) {
 	    if (this.avg < other.avg) {
 		return -1;
 	    } else if (this.avg == other.avg) {
 		return 0;
 	    } else {
 		return 1;
 	    }
 	}
     }
 
     private static List<String> readFileContent(String filename) {
 	List<String> lines = new ArrayList<String>();
 	try {
 	    FileInputStream is = new FileInputStream(filename);
 	    DataInputStream in = new DataInputStream(is);
 	    BufferedReader br = new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8")));
 	    String strLine;
 	    while ((strLine = br.readLine()) != null) {
 		if (strLine.equals("")) {
 		    continue;
 		}
 		lines.add(strLine);
 	    }
 	    br.close();
 	} catch (Exception e) {
 	    e.printStackTrace();
 	    System.exit(1);
 	}
 	return lines;
     }
 
 }
