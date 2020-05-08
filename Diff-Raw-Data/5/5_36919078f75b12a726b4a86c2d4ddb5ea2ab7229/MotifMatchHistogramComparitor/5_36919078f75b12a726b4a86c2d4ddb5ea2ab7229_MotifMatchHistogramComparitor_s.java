 package net.derkholm.nmica.extra.app;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import net.derkholm.nmica.build.NMExtraApp;
 import net.derkholm.nmica.build.VirtualMachine;
 import net.derkholm.nmica.model.HistogramElementIFace;
 import net.derkholm.nmica.model.ScoredSequenceHit;
 import net.derkholm.nmica.model.motif.extra.BucketComparison;
 import net.derkholm.nmica.model.motif.extra.BucketComparisonElement;
 import net.derkholm.nmica.model.motif.extra.HistogramElement;
 
 import org.bjv2.util.cli.App;
 import org.bjv2.util.cli.Option;
 
 
 @App(overview="Compare real and theoretical motif-score distributions", generateStub=true)
 @NMExtraApp(launchName="nmhistcomp", vm=VirtualMachine.SERVER)
 public class MotifMatchHistogramComparitor {
 	private double bucketSize = 1.0;
	private double confidence = 0.01;
 	private File theoreticalFile;
 	private File realFile;
 	private List<HistogramElementIFace> reference;
 	private List<HistogramElementIFace> real;
 	
 	public MotifMatchHistogramComparitor() {
 	}
 	
 	public MotifMatchHistogramComparitor(List<ScoredSequenceHit> reference, List<ScoredSequenceHit> real) {
 		this.setReference(reference);
 		this.setReal(real);
 	}
 	
 	@Option(help="Bucket size in bits (default=1.0)", optional=true)
 	public void setBucketSize(double bucketSize) {
 		this.bucketSize = bucketSize;
 	}
 
 	@Option(help="Confidence threshold (default=0.01)", optional=true)
 	public void setConfidence(double confidence) {
 		this.confidence = confidence;
 	}
 	
 	@Option(help="Theoretical distribution (output format of nmweightwords)")
 	public void setTheoretical(File f) throws Exception {
 		this.theoreticalFile = f;
 		this.reference = bucket(theoreticalFile);
 	}
 	
 	@Option(help="Observed distribution in input sequences " +
 			"as tab separated records with sequence identifier and score per line " +
 			"(e.g. first and sixth column of a GFF)")
 	public void setObserved(File f) throws Exception {
 		this.realFile = f;
 		this.real = bucket(realFile);
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void setReference(List<ScoredSequenceHit> list) {
 		this.reference = (List)list;
 		//System.err.println("Setting reference");
 		this.assignBucketFor(list);
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void setReal(List<ScoredSequenceHit> list) {
 		this.real = (List)list;
 		//System.err.println("Setting real");
 		this.assignBucketFor(list);
 	}
 
 	private List<HistogramElementIFace> bucket(File f)
 		throws Exception {
 		List<HistogramElementIFace> bl = new ArrayList<HistogramElementIFace>();
 		BufferedReader br = new BufferedReader(new FileReader(f));
 		for (String line = br.readLine(); line != null; line = br.readLine()) {
 			StringTokenizer t = new StringTokenizer(line);
 			t.nextToken();
 			int bucket = (int) 
 				Math.floor(
 					Math.abs(Double.parseDouble(t.nextToken())) / bucketSize);
 			double weight = 1.0;
 			if (t.hasMoreTokens()) {
 				weight = Double.parseDouble(t.nextToken());
 			}
 			bl.add(new HistogramElement(bucket, weight));
 		}
 		return bl;
 	}
 	
 	private void assignBucketFor(List<ScoredSequenceHit> hits) {
 		//System.err.println("Assigning bucket for " + hits.size() + " hits");
 		for (ScoredSequenceHit hit : hits) {
 			//System.err.println(hit.hitWeight());
 			int bucket = (int) Math.floor(Math.abs(hit.score()) / bucketSize);
 			hit.setBucket(bucket);
 		}
 	}
 	
 	public List<BucketComparisonElement> compare() {
 		List<BucketComparisonElement> buckets = new ArrayList<BucketComparisonElement>();
 		BucketComparison bucketComparison = new BucketComparison(reference, real, bucketSize);
 
 		for (int b = 0; b < bucketComparison.buckets(); ++b) {
 			//System.err.println(bucketComparison.outputString(b, this.confidence));
 			buckets.add(bucketComparison.compare(b, this.confidence));
 		}
 		
 		return buckets;
 	}
 	
 	public void main(String[] args) throws Exception {
 		BucketComparison bucketComparison = new BucketComparison(reference, real, bucketSize);
 
 		for (int b = 0; b < bucketComparison.buckets(); ++b) {
 			bucketComparison.outputString(b, confidence);
 		}
 	}
 
 	public double determineCutoff(double confidenceThreshold) {
 		List<BucketComparisonElement> elems = compare();
 		
 		int bucketsIgnored = 0;
 		for (int i = 0; i < elems.size(); i++) {
 			BucketComparisonElement e = elems.get(i);
 			
 			if (e.getBarMax() > confidenceThreshold) {
 				if (i > 0 || (bucketsIgnored > 1)) {
					return -bucketSize * (i - 1);
 				} else {
 					bucketsIgnored++;
 				}
 			}
 		}
 		
 		System.err.println("Could not determine cutoff. Will return NaN.");
 		return Double.NaN;
 	}
 
 }
