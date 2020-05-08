 package net.derkholm.nmica.extra.app.seq.nextgen;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import net.derkholm.nmica.build.NMExtraApp;
 import net.derkholm.nmica.build.VirtualMachine;
 import net.sf.samtools.SAMRecord;
 import net.sf.samtools.util.CloseableIterator;
 
 import org.biojava.bio.BioException;
 import org.bjv2.util.cli.App;
 
 import sun.rmi.rmic.Names;
 
 @NMExtraApp(launchName = "ngcount", vm = VirtualMachine.SERVER)
 @App(overview = "Output the number of reads mapped to each of the reference sequences.", generateStub = true)
 public class CountReads extends SAMProcessor {
 	
 	private String[] names;
 	private int[] readCounts;
 	private int currentRefSeqIndex;
 	
 	public void main(String[] args) throws BioException {
 		this.names = (String[]) 
 			this.refSeqLengths.keySet().toArray(
 				new String[this.refSeqLengths.keySet().size()]);
 		this.readCounts = new int[names.length];
 		
 		setIterationType(IterationType.MAPPED_TO_REF);
 		setQueryType(QueryType.OVERLAP);
 		initializeSAMReader();
 		
 		process();
 		
 		for (String seqName : this.names) {
 			for (int i = 0; i < this.names.length; i++) {
 				if (this.names[i].equals(seqName)) {
 					System.out.printf("%s\t%d%n",seqName, this.readCounts[i]);
 					continue;
 				}
 			}
 		}
 	}
 	
 	private void setCurrentRefSeqName(String seqName) {
 		for (int i = 0; i < this.names.length; i++) {
 			if (this.names[i].equals(seqName)) {
 				this.currentRefSeqIndex = i;
				return;
 			}
 		}
		throw new IllegalStateException("Did not find an index for the reference sequence");
 	}
 	
 	public void processAndClose(
 			CloseableIterator<SAMRecord> recs, 
 			String refName, 
 			int len) {
		System.err.println(refName);
 		while (recs.hasNext()) {
 			SAMRecord r = recs.next();
 			if (r.getMappingQuality() < this.qualityCutoff) continue;
 			this.readCounts[this.currentRefSeqIndex] += 1;
 		}
 		recs.close();
 	}
 }
