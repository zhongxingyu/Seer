 package net.derkholm.nmica.extra.app.seq.nextgen;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.SortedSet;
 
 import net.derkholm.nmica.build.NMExtraApp;
 import net.derkholm.nmica.build.VirtualMachine;
 import net.derkholm.nmica.extra.app.seq.nextgen.RetrievePeakSequencesFromEnsembl.PeakEntry;
 import net.derkholm.nmica.extra.app.seq.nextgen.RetrievePeakSequencesFromEnsembl.PeakFormat;
 import net.derkholm.nmica.extra.app.seq.nextgen.RetrievePeakSequencesFromEnsembl.RankOrder;
 import net.derkholm.nmica.extra.app.seq.nextgen.RetrievePeakSequencesFromEnsembl.RankedProperty;
 
 import org.biojava.bio.program.gff.GFFWriter;
 import org.biojava.bio.program.gff.SimpleGFFRecord;
 import org.biojava.bio.seq.StrandedFeature;
 import org.bjv2.util.cli.App;
 import org.bjv2.util.cli.Option;
 
 @App(overview = "Convert peak files from various formats to GFF", generateStub = true)
 @NMExtraApp(launchName = "ngpeak2gff", vm = VirtualMachine.SERVER)
 public class PeaksToGFF {
 	
 	private PeakFormat format;
 	private FileReader peaksReader;
 	private RankOrder rankOrder = RetrievePeakSequencesFromEnsembl.RankOrder.DESC;
 	private RankedProperty rankedProperty = RetrievePeakSequencesFromEnsembl.RankedProperty.P_VALUE;
 	private int aroundPeak;
 	private int maxLength;
 	private int minLength;
 	private int maxCount;
 
 	@Option(help="Peak file format")
 	public void setFormat(RetrievePeakSequencesFromEnsembl.PeakFormat format) {
 		this.format = format;
 	}
 
 	@Option(help="Input peak file")
 	public void setPeaks(FileReader f) {
 		this.peaksReader = f;
 	}
 	
 	@Option(help="Positions to fetch around peak maximae", optional=true)
 	public void setAroundPeak(int i) {
 		this.aroundPeak = i;
 	}
 	
 	@Option(help="Maximum peak length (not defined by default)",optional=true)
 	public void setMaxLength(int maxLength) {
 		this.maxLength = maxLength;
 	}
 	
 	@Option(help="Minimum peak length (default=20)",optional=true)
 	public void setMinLength(int minLength) {
 		this.minLength = minLength;
 	}
 	
 	@Option(help="Maximum number of peaks to output", optional=true)
 	public void setMaxCount(int maxCount) {
 		this.maxCount = maxCount;
 	}
 	
 	@Option(help="Rank order", optional=true)
 	public void setRankOrder(RankOrder rankOrder) {
 		this.rankOrder = rankOrder;
 	}
 	
 	@Option(help="Ranked property", optional=true)
 	public void setRankedProperty(RankedProperty rankedProp) {
 		this.rankedProperty = rankedProp;
 	}
 	
 	public void main(String[] args) throws FileNotFoundException, IOException {
 		SortedSet<PeakEntry> peaks = RetrievePeakSequencesFromEnsembl.parsePeaks(
 				new BufferedReader(peaksReader), 
 				format, 
 				rankOrder, 
 				rankedProperty, 
 				aroundPeak, 
 				minLength, 
 				maxLength);
 		
 		if (maxCount == 0) {
 			maxCount = peaks.size();
 		}
 		
 		System.err.printf("Parsed %d peaks", peaks.size());
 		GFFWriter writer = new GFFWriter(new PrintWriter(System.out));
 		
 		Iterator<PeakEntry> peakIterator = peaks.iterator();
		int i = 0;
 		
 		while (i < maxCount) {
 			final PeakEntry peak = peakIterator.next();
 			SimpleGFFRecord rec = new SimpleGFFRecord();
 			rec.setComment(String.format("\tp-value:%.3f fdr:%.3f", peak.score,peak.fdr));
 			rec.setEnd(peak.endCoord);
 			rec.setFeature("peak");
 			rec.setFrame(0);
 			Map<String,Object> attribs = new HashMap<String,Object>();
 			
 			attribs.put("fdr", peak.fdr);
 			attribs.put("score", peak.score);
 			
 			
 			rec.setGroupAttributes(attribs);
 			rec.setScore(peak.fdr);
 			rec.setSeqName(peak.seqName);
 			rec.setSource(format.name());
 			rec.setStart(peak.startCoord);
 			rec.setStrand(StrandedFeature.UNKNOWN);
 			
 			writer.recordLine(rec);
 			writer.endDocument();
 			
			i++;
 		}
 		writer.endDocument();
 
 	}
 }
