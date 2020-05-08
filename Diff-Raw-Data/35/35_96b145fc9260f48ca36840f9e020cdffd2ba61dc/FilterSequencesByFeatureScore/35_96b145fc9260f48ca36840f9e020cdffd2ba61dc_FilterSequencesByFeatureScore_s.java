 package net.derkholm.nmica.extra.app.seq;
 
 import gfftools.GFFUtils;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.NoSuchElementException;
 
 import net.derkholm.nmica.build.NMExtraApp;
 import net.derkholm.nmica.build.VirtualMachine;
 
 import org.biojava.bio.Annotation;
 import org.biojava.bio.BioError;
 import org.biojava.bio.BioException;
 import org.biojava.bio.program.gff.GFFDocumentHandler;
 import org.biojava.bio.program.gff.GFFParser;
 import org.biojava.bio.program.gff.GFFRecord;
 import org.biojava.bio.program.gff.GFFWriter;
 import org.biojava.bio.seq.Sequence;
 import org.biojava.bio.seq.SequenceIterator;
 import org.biojava.bio.seq.db.HashSequenceDB;
 import org.biojava.bio.seq.db.IllegalIDException;
 import org.biojava.bio.seq.db.SequenceDB;
 import org.biojava.bio.seq.impl.SimpleSequence;
 import org.biojava.bio.symbol.Location;
 import org.biojava.utils.ChangeVetoException;
 import org.biojavax.bio.seq.RichSequence;
 import org.bjv2.util.cli.App;
 import org.bjv2.util.cli.Option;
 import org.bjv2.util.cli.UserLevel;
 
 import biobits.utils.IOTools;
 
 @App(overview = "Write out sequence regions covered by GFF features", generateStub = true)
 @NMExtraApp(launchName = "nmfilterseq", vm = VirtualMachine.SERVER)
 public class FilterSequencesByFeatureScore {
 
     public static enum Format {
     	GFF,
     	FASTA
     }
 	
 	private Format format = Format.GFF;
 	
 	private double minScore = Double.NaN;
 	@Option(help="Minimum score", optional=true)
 	public void setMinScore(double s) {
 		this.minScore = s;
 	}
 
 	private double maxScore = Double.NaN;
 	
 	private File seqFile;
 	private File seqsFile;
 	private HashSequenceDB seqDB;
 	private boolean validate;
 
 	private boolean negate;
 
 	private boolean negateScore;
 
 	private File[] featureFiles;
 
 	@Option(help="Maximum score", optional=true)
 	public void setMaxScore(double s) {
 		this.maxScore = s;
 	}
 
 	@Option(help="Validate input (check that sequence identifiers match)", optional=true, userLevel = UserLevel.DEBUG)
 	public void setValidate(boolean b) {
 		this.validate = b;
 	}
 	
 	@Option(help="Negate output (output features/sequences that don't fill the filter constraints", optional=true)
 	public void setNegate(boolean b) {
 		this.negate = b;
 	}
 	
 	@Option(help="Invert the score (positive <--> negative numbers). Use this if you need to use negative scores.", optional=true)
 	public void setInvertScore(boolean b) {
 		this.negateScore = b;
 	}
 	
 	
 	@Option(help = "Input sequences (output the intersecting sequences rather than a new GFF file)", optional=true)
 	public void setSeqs(File f) throws FileNotFoundException, ChangeVetoException, NoSuchElementException, BioException {
 		this.seqsFile = f;
 
 		this.seqDB = new HashSequenceDB();
 		for (SequenceIterator si = RichSequence
 				.IOTools
 				.readFastaDNA(
 						new BufferedReader(
 								new FileReader(f)), null); si.hasNext();) {
 
 			seqDB.addSequence(si.nextSequence());
 		}
 		this.format = Format.FASTA;
 	}
 
 	@Option(help="Input GFF file(s) for features")
 	public void setFeatures(File[] files) {
 		this.featureFiles = files;
 	}
 
 	public void main(String[] args) throws Exception {
 
 		Map<String,Location> locs = new HashMap<String, Location>();
 		for (File f : featureFiles) {
 			if (validate && (seqDB != null)) {
 				Map<String,Location> ls = GFFUtils.gffToLocationMap(f);
 				WriteCoveredSequences.validateGFFSequenceIdentifiersAgainstSequences(ls, seqDB);
 				locs.putAll(ls);
 			}
 		}
 		
 		GFFParser gffp = new GFFParser();
 		
 		for (File f : featureFiles) {
 			GFFFilteringDocumentHandler fDocHandler = 
 				new GFFFilteringDocumentHandler(
 						this.minScore,
 						this.maxScore,
 						this.negate,
 						this.negateScore,
 						this.format, 
 						seqDB);
 			
 			gffp.parse(IOTools.fileBufferedReader(f),fDocHandler);
 			fDocHandler.endDocument();
 		}
 	}
 
 	private static class GFFFilteringDocumentHandler implements GFFDocumentHandler {
 		private double minScore = Double.NaN;
 		private double maxScore = Double.NaN;
 		private Format format;
 		private GFFWriter gffWriter;
 		private SequenceDB sequences;
 		private boolean negate;
 		private boolean negateScore;
 
 		public GFFFilteringDocumentHandler() {
 			
 		}
 		
 		private GFFFilteringDocumentHandler(
 				double minScore, 
 				double maxScore, 
 				boolean negate,
 				boolean negateScores,
 				Format format, 
 				SequenceDB sequences) {
 			this.minScore = minScore;
 			this.maxScore = maxScore;
 			this.negate = negate;
 			this.negateScore = negateScores;
			this.gffWriter = new GFFWriter(new PrintWriter(System.out));
 			this.format = format;
 			this.sequences = sequences;
 		}
 		
 		public void startDocument(String locator) {
 		}
 
		public void endDocument() {
		}
 
 		public void commentLine(String comment) {
 		}
 
 		public void recordLine(GFFRecord record) {
 			boolean allowOutput = true;
 			double s = record.getScore();
 			double minS = this.minScore;
 			double maxS = this.maxScore;
 			if (this.negateScore) {
 				s = -s;
				double tempMinS = minS;
				double tempMaxS = maxS;
				minS = -tempMaxS;
				maxS = -tempMinS;
 			}
 			
 			if (!Double.isNaN(this.minScore)) {
				if (this.minScore > record.getScore()) {
 					allowOutput = false;
 				}
 			}
 			
			if (!Double.isNaN(this.maxScore)) {
				if (this.maxScore < record.getScore()) {
 					allowOutput = false;
 				}
 			}
 			
 			if (negate) {
 				allowOutput = !allowOutput;
 			}
 			
 			if (allowOutput) {
 				if (this.format == Format.GFF) {
 					this.gffWriter.recordLine(record);
 				} else {
 					Sequence seq;
 					try {
 						seq = new SimpleSequence(sequences.getSequence(record.getSeqName()).subList(record.getStart(),record.getEnd()),null,
 								String.format("%s_|%d-%d|",record.getSeqName(),record.getStart(),record.getEnd()),Annotation.EMPTY_ANNOTATION);
 					} catch (IllegalIDException e) {
 						throw new BioError(e);
 					} catch (BioException e) {
 						throw new BioError(e);
 					}
                 	try {
 						RichSequence.IOTools.writeFasta(System.out, seq, null);
 					} catch (IOException e) {
 						throw new BioError(e);
 					}
 					
 				}
 			}
 		}
 	}
 }
