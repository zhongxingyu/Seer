 package statistics;
 
 import java.io.PrintStream;
 import java.util.List;
 
 import tree.DiscreteGenTree;
 
 /**
  * This class tracks the density of recombination breakpoints along the strand of DNA
  * @author brendan
  *
  */
 public class BreakpointDensity extends TreeStatistic {
 
 	public static final String identifier = "Breakpoint density map";
 	
 	//We divide the dna into this many bins for density calculation purposes
 	final int bins = 20; 
 	Histogram histo;
 	
 	@Override
 	public Statistic getNew(Options ops) {
 		return new BreakpointDensity();
 	}
 
 	@Override
 	public String getIdentifier() {
 		return identifier;
 	}
 
 	@Override
 	public void collect(DiscreteGenTree tree) {
 		if (tree == null)
 			return;
 		
 		if (histo == null) {
 			int dnaLength = pop.getInd(0).getRecombineableData().length();
 			histo = new Histogram(bins, 0, dnaLength / bins);
 		}
 		
 		List<Integer> breakpoints = tree.collectBreakPoints();
 		for(Integer bp : breakpoints) {
 			histo.addValue(bp);
 		}
 		
 	}
 
 	public void summarize(PrintStream out) {
 		out.println("Summary for " + getIdentifier() + " ( " + getDescription() + " )");
 		out.println("Number of samples : \t" + histo.getCount());
 		String histoStr = histo.toString();
 		out.println(histoStr);
 	}
 	
 	@Override
 	public String getDescription() {
 		return "Breakpoint density map";
 	}
 
 }
