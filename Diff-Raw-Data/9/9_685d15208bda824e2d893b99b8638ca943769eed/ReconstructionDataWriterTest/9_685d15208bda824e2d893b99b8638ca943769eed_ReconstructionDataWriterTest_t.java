 package uk.ac.ebi.chemet.io.entity.marshal.recon;
 
 import org.apache.log4j.Logger;
 import org.biojava3.core.sequence.ChromosomeSequence;
 import org.junit.Test;
 import uk.ac.ebi.caf.utility.version.Version;
 import uk.ac.ebi.mdk.io.*;
 import uk.ac.ebi.mdk.domain.identifier.basic.*;
 import uk.ac.ebi.mdk.domain.entity.DefaultEntityFactory;
 import uk.ac.ebi.mdk.domain.entity.ReconstructionImpl;
 import uk.ac.ebi.mdk.domain.entity.reaction.MetabolicParticipantImplementation;
 import uk.ac.ebi.mdk.domain.entity.reaction.compartment.Organelle;
 import uk.ac.ebi.mdk.domain.entity.*;
 import uk.ac.ebi.mdk.domain.entity.collection.Chromosome;
 import uk.ac.ebi.mdk.domain.entity.reaction.Direction;
 import uk.ac.ebi.mdk.domain.identifier.basic.ReconstructionIdentifier;
 import uk.ac.ebi.mdk.domain.identifier.basic.BasicGeneIdentifier;
 import uk.ac.ebi.mdk.domain.identifier.basic.ChromosomeNumber;
 import uk.ac.ebi.mdk.domain.entity.reaction.MetabolicReaction;
 import uk.ac.ebi.mdk.domain.entity.EntityFactory;
 import uk.ac.ebi.mdk.domain.identifier.Taxonomy;
 
 import java.io.*;
 
 /**
  * ReconstructionDataWriterTest - 12.03.2012 <br/>
  * <p/>
  * Class descriptions.
  *
  * @author johnmay
  * @author $Author$ (this version)
  * @version $Rev$
  */
 public class ReconstructionDataWriterTest {
 
     private static final Logger LOGGER = Logger.getLogger(ReconstructionDataWriterTest.class);
 
     @Test
     public void testWrite() throws IOException, ClassNotFoundException {
 
         Version v = new Version("0.9");
         EntityFactory factory = DefaultEntityFactory.getInstance();
 
         ByteArrayOutputStream bytes = new ByteArrayOutputStream();
         DataOutputStream out = new DataOutputStream(bytes);
         EntityOutput entityOut = new EntityDataOutputStream(v, out,
                                                                   factory,
                                                                   new AnnotationDataOutputStream(out, v),
                                                                   new ObservationDataOutputStream(out, v));
 
         entityOut.write(createReconstruction());
 
         DataInputStream din = new DataInputStream(new ByteArrayInputStream(bytes.toByteArray()));
         EntityInput in = new EntityDataInputStream(v, din, factory,
                                                          new AnnotationDataInputStream(din, v),
                                                          new ObservationDataInputStream(din, v));
 
 
         ReconstructionImpl recon = in.read();
 
         for(Metabolite m : recon.getMetabolome()){
             System.out.println(m);
         }
         for(MetabolicReaction r : recon.getReactome()){
             System.out.println(r + "modifiers: " + r.getModifiers());
         }
         for(Gene g : recon.getGenes()){
             System.out.println(g + ": " + g.getStart() + ":" + g.getEnd() + " sequence " + g.getSequence().getSequenceAsString());
         }
         for(GeneProduct gp : recon.getProducts()){
             System.out.println(gp + ": " + gp.getGenes());
         }
 
     }
 
     public ReconstructionImpl createReconstruction() {
 
         ReconstructionImpl reconstruction = new ReconstructionImpl(new ReconstructionIdentifier("recon-1"),
                                                            "a reconstruction",
                                                            "recon");
 
         reconstruction.setTaxonomy(new Taxonomy(12, "ecoli", Taxonomy.Kingdom.BACTERIA, "", ""));
 
         EntityFactory factory = DefaultEntityFactory.getInstance();
 
         Metabolite m1 = factory.ofClass(Metabolite.class, BasicChemicalIdentifier.nextIdentifier(), "ATP", "atp");
         Metabolite m2 = factory.ofClass(Metabolite.class, BasicChemicalIdentifier.nextIdentifier(), "GTP", "gtp");
         Metabolite m3 = factory.ofClass(Metabolite.class, BasicChemicalIdentifier.nextIdentifier(), "GDP", "adp");
         Metabolite m4 = factory.ofClass(Metabolite.class, BasicChemicalIdentifier.nextIdentifier(), "ADP", "adp");
         Metabolite m5 = factory.ofClass(Metabolite.class, BasicChemicalIdentifier.nextIdentifier(), "H+", "h");
         Metabolite m6 = factory.ofClass(Metabolite.class, BasicChemicalIdentifier.nextIdentifier(), "CO2", "co2");
         Metabolite m7 = factory.ofClass(Metabolite.class, BasicChemicalIdentifier.nextIdentifier(), "Water", "h2o");
 
         reconstruction.addMetabolite(m1);
         reconstruction.addMetabolite(m2);
         reconstruction.addMetabolite(m3);
         reconstruction.addMetabolite(m4);
         reconstruction.addMetabolite(m5);
         reconstruction.addMetabolite(m6);
         reconstruction.addMetabolite(m7);
 
         MetabolicReaction r1 = factory.ofClass(MetabolicReaction.class, BasicReactionIdentifier.nextIdentifier(), "rxn1", "r1");
         MetabolicReaction r2 = factory.ofClass(MetabolicReaction.class, BasicReactionIdentifier.nextIdentifier(), "rxn2", "r2");
 
         r1.addReactant(new MetabolicParticipantImplementation(m1));
         r1.addReactant(new MetabolicParticipantImplementation(m2));
         r1.addProduct(new MetabolicParticipantImplementation(m3));
 
         r2.addReactant(new MetabolicParticipantImplementation(m3, Organelle.EXTRACELLULAR));
         r2.addProduct(new MetabolicParticipantImplementation(m3));
 
         reconstruction.addReaction(r1);
         reconstruction.addReaction(r2);
 
         GeneProduct p1 = factory.ofClass(ProteinProduct.class, new BasicProteinIdentifier(), "prot1", "p1");
         GeneProduct p2 = factory.ofClass(ProteinProduct.class, new BasicProteinIdentifier(), "prot2", "p2");
         GeneProduct rna1 = factory.ofClass(RibosomalRNA.class, new BasicRNAIdentifier(), "rna1", "rna1");
         GeneProduct rna2 = factory.ofClass(TransferRNA.class, new BasicRNAIdentifier(), "rna2", "rna2");
 
         reconstruction.addProduct(p1);
         reconstruction.addProduct(p2);
         reconstruction.addProduct(rna1);
         reconstruction.addProduct(rna2);
 
         r1.addModifier(p1);
         r2.setDirection(Direction.BACKWARD);
 
         Chromosome chromosome = factory.ofClass(Chromosome.class, new ChromosomeNumber(1), "Chromosome 1", "ch1");
 
         reconstruction.getGenome().add(chromosome);
         chromosome.setSequence(new ChromosomeSequence("AACGTGCTGATCGTACGTAGCTAGCTAGCATGCATGCATGCATGACTGCATAC".toLowerCase()));
 
         Gene g1 = factory.ofClass(Gene.class, new BasicGeneIdentifier(), "Gene 1", "g1");
         g1.setStart(1);
         g1.setEnd(5);
         Gene g2 = factory.ofClass(Gene.class, new BasicGeneIdentifier(), "Gene 2", "g2");
         g2.setStart(1);
         g2.setEnd(6);
         Gene g3 = factory.ofClass(Gene.class, new BasicGeneIdentifier(), "Gene 3", "g3");
         g3.setStart(1);
         g3.setEnd(7);
         Gene g4 = factory.ofClass(Gene.class, new BasicGeneIdentifier(), "Gene 4", "g4");
         g4.setStart(1);
         g4.setEnd(8);
 
         p1.addGene(g1);
         p1.addGene(g2);
 
         rna1.addGene(g3);
 
         chromosome.add(g1);
         chromosome.add(g2);
         chromosome.add(g3);
         chromosome.add(g4);
 
         return reconstruction;
 
     }
 
 }
