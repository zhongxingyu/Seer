 package nzgot.ec;
 
 import jebl.evolution.align.SystemOut;
 import jebl.evolution.align.scores.Blosum80;
 import jebl.evolution.io.FastaExporter;
 import jebl.evolution.io.FastaImporter;
 import jebl.evolution.io.ImportException;
 import jebl.evolution.sequences.BasicSequence;
 import jebl.evolution.sequences.Sequence;
 import jebl.evolution.sequences.SequenceType;
 import jebl.evolution.taxa.Taxon;
 import nzgot.core.community.OTU;
 import nzgot.core.community.OTUs;
 import nzgot.core.community.io.CommunityImporter;
 import nzgot.core.logger.Logger;
 import nzgot.core.util.SequenceUtil;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 
 /**
  * Automatic error correction
  * @author Thomas Hummel
  * @author Walter Xie
  */
 public class AutomaticEC {
 
 	final String workPath;
 
 	//Sequence files
 	final String fileSeq;
 	final String fileRef;
 	final String fileCor;
 	final String fileRand;
 	final String fileControl;
 	
 	//Mapping files
 	final String mapSeqOtu;
 	final String mapOtuRef;
 	
 	final String fileCorCountMatrix;
 	
 	List<Sequence> sequences;
 	List<Sequence> references;
 	List<Sequence> sequencesCor;
 	List<Sequence> sequencesRand;
 	List<Sequence> sequencesControl;
 
     public AutomaticEC(String workPath) {
         this.workPath = workPath;
 
         fileSeq = workPath + "seq.fasta";
         fileRef = workPath + "ref.fasta";
         fileCor = workPath + "corrected.fasta";
         fileRand = workPath + "randomCorrected.fasta";
         fileControl = workPath + "control.fasta";
 
         //Mapping files
         mapSeqOtu = workPath + "seqOtuMap.txt";
         mapOtuRef = workPath + "otuRefMap.txt";
 
         fileCorCountMatrix = workPath + "correctionCountMatrix.txt";
     }
 
     /**
 	 *Automatic error correction of all sequences given 
 	 *in the <code>fileSeq</code> file which mapped to the <code>fileRef</code> file 
 	 */
 	
 	
 	
 	public void doEC() throws IOException, ImportException{
         File sequenceIn = new File(fileSeq);
         File referenceIn = new File(fileRef);
         File sequenceOut = new File(fileCor);
         File sequenceOut2 = new File(fileControl);
         File sequenceOut3 = new File(fileRand);
 
 		FastaImporter sequenceImport = new FastaImporter(sequenceIn , SequenceType.NUCLEOTIDE);
 		FastaImporter referenceImport = new FastaImporter(referenceIn, SequenceType.AMINO_ACID);
 		sequences = sequenceImport.importSequences();
 		references =referenceImport.importSequences();
 		sequencesCor = new ArrayList<Sequence>(2000);
 		sequencesRand = new ArrayList<Sequence>(2000);
 		sequencesControl = new ArrayList<Sequence>(2000);
 
         File file = new File(mapSeqOtu);
         OTUs otus = new OTUs(file.getName());
         CommunityImporter.importOTUsAndMappingFromUCFile(file, otus);
 
         file = new File(mapOtuRef);
         CommunityImporter.importRefSeqMappingFromUCFile(file, otus);
 
         double count = 0;
         double size = sequences.size();
         
         String referenceLabel;
         String referenceSeq;
         
        AlignAndCorrect ac = new AlignAndCorrect(new Blosum80(), -10, -10, -100, myGeneticCode.INVERTEBRATE_MT);

         Correction cor = new Correction();
         LinkedHashMap<Taxon,int[]> correctionCountMatrix = new LinkedHashMap<Taxon, int[]>();
         
 
         for (Sequence seq : sequences) {
         	
 			count++;
 			referenceLabel = null;
 			referenceSeq = null;
 			
             OTU otu = (OTU) otus.getOTUOfSeq(seq.getTaxon().toString());
             if (otu != null && otu.getReference() != null)
                 referenceLabel = otu.getReference().toString();
 
 			//reference String 
 			if (referenceLabel != null)
 				referenceSeq = SequenceUtil.getSequenceStringFrom(referenceLabel, references);
 				
 			//correct sequence with reference alignment and save in list
 			if (referenceSeq != null) {
 				
 				ac.doAlignment(seq.getString(), referenceSeq);
 				try{
                     String[] match = ac.getMatch();
                     correctionCountMatrix.put(seq.getTaxon(), ac.getCorrectionCount().clone()); //cloning correctionCounts  
                     Sequence correctedSeq = new BasicSequence(SequenceType.NUCLEOTIDE, seq.getTaxon(), match[1]); //TODO Delete gaps
                     Sequence randCorSeq = new BasicSequence(SequenceType.NUCLEOTIDE, seq.getTaxon(), ac.getRandomCorrection());
                     ac.doMatch(new SystemOut(), "", match);
 					sequencesCor.add(correctedSeq);
 					sequencesRand.add(randCorSeq);
 					sequencesControl.add(seq);
                     Logger.getLogger().debug(String.format("%.5g", ((count / size)) * 100) + "%");
 				}
 				catch (NullPointerException e) {
                     Logger.getLogger().debug(seq.getTaxon().toString());
 				}
 			}
 		}
         
         cor.writeCorrectionMatrix(correctionCountMatrix, fileCorCountMatrix);
         
         //Corrected sequences
         Writer write = new OutputStreamWriter(new FileOutputStream(sequenceOut));
 		FastaExporter fe = new FastaExporter(write);
 		fe.exportSequences(sequencesCor);
 		write.flush();
 		write.close();
 		
 		//Control sequences
 		Writer write2 = new OutputStreamWriter(new FileOutputStream(sequenceOut2));
 		FastaExporter fe2 = new FastaExporter(write2);
 		fe2.exportSequences(sequencesControl);
 		write2.flush();
 		write2.close();
 		
 		//Random corrected sequences
 		Writer write3 = new OutputStreamWriter(new FileOutputStream(sequenceOut3));
 		FastaExporter fe3 = new FastaExporter(write3);
 		fe3.exportSequences(sequencesRand);
 		write3.flush();
 		write3.close();
 	}
 
 
 	public static void main(String[] args) throws ImportException, IOException{
         if (args.length != 1) throw new IllegalArgumentException("Working path is missing in the argument !");
 
         String workPath = args[0];
         System.out.println("\nWorking path = " + workPath);
 
 		AutomaticEC ec = new AutomaticEC(workPath);
 		ec.doEC();
 	}
 
 }
