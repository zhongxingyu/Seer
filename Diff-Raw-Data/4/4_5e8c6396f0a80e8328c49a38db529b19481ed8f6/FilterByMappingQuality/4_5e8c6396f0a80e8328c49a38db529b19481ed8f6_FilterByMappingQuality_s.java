 package net.derkholm.nmica.extra.app.seq.nextgen;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.OutputStream;
 
 import net.derkholm.nmica.build.NMExtraApp;
 import net.derkholm.nmica.build.VirtualMachine;
 import net.sf.samtools.SAMFileReader;
 import net.sf.samtools.SAMFileWriter;
 import net.sf.samtools.SAMFileWriterFactory;
 import net.sf.samtools.SAMRecord;
 import net.sf.samtools.SAMFileReader.ValidationStringency;
 
 import org.biojava.bio.BioException;
 import org.bjv2.util.cli.App;
 import org.bjv2.util.cli.Option;
 
 @App(overview = "Filter mapped reads by mapping quality", generateStub = true)
 @NMExtraApp(launchName = "ngfiltermap", vm = VirtualMachine.SERVER)
 public class FilterByMappingQuality extends FilteringSAMProcessor {
 
 	private SAMFileReader sam;
 	private int mapQual = 10;
 	private File outFile;
 
 	@Option(help="Mapping quality threshold (default = 10)")
 	public void setMappingQualityAbove(int i) {
 		this.mapQual = i;
 	}
 	
 	public void main(String[] args) throws FileNotFoundException, BioException {
 		initializeSAMReader();
 		initializeSAMWriter(false);
 		process();
 		
 		outWriter.close();
 	}
 	
 	@Override
 	public void process(SAMRecord rec, int readIndex) {
		if (rec.getMappingQuality() < this.mapQual) {
 			outWriter.addAlignment(rec);
 		}	
 	}
 }
