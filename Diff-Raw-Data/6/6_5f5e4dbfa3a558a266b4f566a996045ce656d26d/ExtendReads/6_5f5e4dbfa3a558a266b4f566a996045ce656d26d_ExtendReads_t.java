 package net.derkholm.nmica.extra.app.seq.nextgen;
 
 import java.io.File;
 
 import net.derkholm.nmica.build.NMExtraApp;
 import net.derkholm.nmica.build.VirtualMachine;
 import net.sf.samtools.SAMFileWriter;
 import net.sf.samtools.SAMFileWriterFactory;
 import net.sf.samtools.SAMRecord;
 
 import org.biojava.bio.BioException;
 import org.bjv2.util.cli.App;
 import org.bjv2.util.cli.Option;
 
 @NMExtraApp(launchName = "ngextend", vm = VirtualMachine.SERVER)
 @App(overview = "Extend reads by specified number of nucleotides", generateStub = true)
 public class ExtendReads extends FilteringSAMProcessor {
 	private int extendReadsBy;
 	
 	@Option(help="Expand reads by specified number of nucleotides (bound by reference sequence ends)")
 	public void setBy(int i) {
 		this.extendReadsBy = i;
 	}
 	
 	public void main(String[] args) throws BioException {
 		setIterationType(IterationType.ONE_BY_ONE);
 		initializeSAMReader();
 		initializeSAMWriter(false);
 		
 		process();
 		outWriter.close();
 	}
 	
 	@Override
 	public void process(SAMRecord rec, int readIndex) {
 		int len = refSeqLengths.get(rec.getReferenceName());
 		if (!rec.getReadNegativeStrandFlag()) {
 			rec.setAlignmentEnd(Math.max(len, rec.getAlignmentEnd() + extendReadsBy));
 		} else {
 			rec.setAlignmentStart(Math.min(0, rec.getAlignmentStart() - extendReadsBy));
 		}
 		outWriter.addAlignment(rec);
 	}
 }
