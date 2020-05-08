 /*
  * Copyright (c) 2013. John May <jwmay@users.sf.net>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package uk.ac.ebi.mdk.io.domain;
 
 import org.apache.log4j.Logger;
 import uk.ac.ebi.caf.utility.version.annotation.CompatibleSince;
 import uk.ac.ebi.mdk.domain.entity.GeneProduct;
 import uk.ac.ebi.mdk.domain.entity.Metabolite;
 import uk.ac.ebi.mdk.domain.entity.Reconstruction;
 import uk.ac.ebi.mdk.domain.entity.collection.Genome;
 import uk.ac.ebi.mdk.domain.entity.reaction.MetabolicReaction;
 import uk.ac.ebi.mdk.io.AbstractDataOutput;
 import uk.ac.ebi.mdk.io.EntityOutput;
 import uk.ac.ebi.mdk.io.EntityWriter;
 import uk.ac.ebi.mdk.io.IdentifierOutput;
 
 import java.io.DataOutput;
 import java.io.IOException;
 import java.util.Collection;
 
 /**
  * ReconstructionDataWriter_0_9 - 12.03.2012 <br/>
  * <p/>
  * Writes a reconstruction to a data output stream.
  *
  * @author johnmay
  * @author $Author$ (this version)
  * @version $Rev$
  */
@CompatibleSince("0.9")
 public class ReconstructionDataWriter_1_3_3
         implements EntityWriter<Reconstruction> {
 
     private static final Logger LOGGER = Logger.getLogger(ReconstructionDataWriter_1_3_3.class);
 
     private DataOutput out;
     private EntityOutput entityOut;
     private IdentifierOutput identifierOutput;
     private AbstractDataOutput output;
 
     public ReconstructionDataWriter_1_3_3(DataOutput out,
                                           IdentifierOutput identifierOutput,
                                           EntityOutput entityOut){
         this.out = out;
         this.identifierOutput = identifierOutput;
         this.entityOut = entityOut;
     }
 
     public void write(Reconstruction reconstruction) throws IOException {
 
         // write the UUID (first in, first out)
         out.writeUTF(reconstruction.uuid().toString());
 
         // write taxonomy identifier
         identifierOutput.write(reconstruction.getTaxonomy());
 
         // container
         out.writeUTF(reconstruction.getContainer().getAbsolutePath());
 
         // GENOME
         Genome genome = reconstruction.getGenome();
         entityOut.writeData(genome);
 
         // METABOLOME
         Collection<Metabolite> metabolites = reconstruction.getMetabolome();
         out.writeInt(metabolites.size());
         for(Metabolite m : metabolites){
             entityOut.writeData(m);
         }
 
         // PROTEOME
         Collection<GeneProduct> products = reconstruction.getProducts();
         out.writeInt(products.size());
         for(GeneProduct p : products){
             entityOut.write(p);
         }
 
         // REACTOME
         Collection<MetabolicReaction> reactions = reconstruction.getReactome();
         out.writeInt(reactions.size());
         for(MetabolicReaction r : reactions){
             entityOut.writeData(r);
         }
 
     }
 
 }
