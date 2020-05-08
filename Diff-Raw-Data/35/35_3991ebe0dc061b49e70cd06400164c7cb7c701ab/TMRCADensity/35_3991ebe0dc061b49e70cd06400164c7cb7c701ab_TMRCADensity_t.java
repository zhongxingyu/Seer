 package statistics;
 
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import fitnessProviders.DNAFitness;
 import fitnessProviders.FitnessProvider;
 
 import population.Locus;
 import population.Recombineable;
 import tree.DiscreteGenTree;
 
 /**
  * This class collects the TMRCA of a sample as a function of position along a DNA sequence. 
  * It is implemented as a series of histograms, where each histogram is associated with a segment of the
  * DNA sequence. In this way we can track not just the mean TMRCA but also the variance, etc., as a function
  * of sequence position. 
  * 
  * Of course, in the absence of recombination this is will always be the same for all positions in the sequence, 
  * only with recombination might TMRCA vary from position to position. 
  * @author brendan
  *
  */
 public class TMRCADensity extends TreeStatistic {
 
 	public static final String identifier = "TMRCA Density map";
 	
 	int numHistos = 20;
 	int histoBins = 50;
 	int binSize = 100;
 	//Max depth tracked will be histoBins * binSize, which will be 5000 here. This must be made user-configurable
 	Histogram[] histos;
 	
 	Integer seqLength = null;
 	
 	public void summarize(PrintStream out) {
 		out.println("Summary for " + getIdentifier() + " ( " + getDescription() + " )");
 		out.println("Number of samples : \t" + histos[0].getCount());
 		
 		if (seqLength == null) {
 			out.println("No data collected.");
 			return;
 		}
 		
 		int histoIndex = 0;
 		int seqSpacing = seqLength / numHistos;
 		out.println(" Site range \t Mean TMRCA \t Stdev. TMRCA");
 		for(int site=0; site<seqLength; site += seqSpacing) {
 			out.println(site + " - " + (site+seqSpacing-1) + " : \t" + histos[histoIndex].getMean() + "\t" + histos[histoIndex].getStdev());
 			histoIndex++;
 		}
 		
 	}
 	
	public void clear() {
		seqLength = null;
		if (histos != null) {
			for(int i=0; i<histos.length; i++) {
				histos[i].clear();
			}
		}
	}
	
 	@Override
 	public void collect(DiscreteGenTree tree) {
 		if (tree==null)
 			return;
 		
 		if (histos == null) {
 			histos = new Histogram[numHistos];
 			for(int i=0; i<numHistos; i++) {
 				histos[i] = new Histogram(histoBins, 0, binSize);
 			}
 		}
 		
 		FitnessProvider fitnessData = tree.getTips().get(0).getFitnessData();
 		if (! (fitnessData instanceof DNAFitness)) {
 			throw new IllegalArgumentException("Cannot collect TMRCA density map for Loci without DNA");
 		}
 		
 		if (seqLength==null)
 			seqLength = ((DNAFitness)fitnessData).getLength();
 		
 		int histoIndex = 0;
 		int seqSpacing = seqLength / numHistos;
 		for(int site=0; site<seqLength; site += seqSpacing) {
 			int tmrca = getTMRCAForSite(tree, site);
 			histos[histoIndex].addValue(tmrca);
 			histoIndex++;
 		}
 		
 	}
 
 	/**
 	 * Returns the time to most recent common ancestor at the given site
 	 * @param tree
 	 * @param site
 	 * @return
 	 */
 	private int getTMRCAForSite(DiscreteGenTree tree, int site) {
 		int tmrca = 0;
 		List<Locus> ancestors = tree.getTips();
 		
 		while(ancestors.size()>1) {
 			ancestors = getParentsOfSampleForSite(ancestors, site);
 			tmrca++;
 		}
 		
 		return tmrca;
 	}
 	
 	/**
 	 * Returns a non-redundant list of all members of previous generation
 	 * that are ancestral to the given sample. 
 	 * @param sample
 	 * @return
 	 */
 	private List<Locus> getParentsOfSampleForSite(List<Locus> sample, int site) {
 		List<Locus> parents = new ArrayList<Locus>(sample.size());
 		
 		for(Locus kid : sample) {
			Locus parent = kid.getParentForSite(site);
			if (! parents.contains(parent)) {
				parents.add(parent);
 			}
 		}
 		return parents;
 	}
 	
 	@Override
 	public Statistic getNew(Options ops) {
 		return new TMRCADensity();
 	}
 
 	@Override
 	public String getIdentifier() {
 		return identifier;
 	}
 
 	@Override
 	public String getDescription() {
 		return "TMRCA as a function of position along the sequence";
 	}
 
 }
