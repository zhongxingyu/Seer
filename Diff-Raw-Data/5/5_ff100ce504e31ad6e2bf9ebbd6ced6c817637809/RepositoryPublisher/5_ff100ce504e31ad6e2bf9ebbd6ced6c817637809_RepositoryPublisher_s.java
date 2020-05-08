 package org.virtual.sr;
 
 import com.hp.hpl.jena.graph.Triple;
 import com.hp.hpl.jena.query.Query;
 import com.hp.hpl.jena.query.QueryExecutionFactory;
 import com.hp.hpl.jena.query.QueryFactory;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 
 import javax.xml.transform.Source;
 import javax.xml.transform.stream.StreamSource;
 
 import org.sdmxsource.sdmx.api.constants.STRUCTURE_OUTPUT_FORMAT;
 import org.sdmxsource.sdmx.api.manager.output.StructureWritingManager;
 import org.sdmxsource.sdmx.api.model.beans.SdmxBeans;
 import org.sdmxsource.sdmx.api.model.beans.codelist.CodelistBean;
 import org.sdmxsource.sdmx.structureparser.manager.impl.StructureWritingManagerImpl;
 import org.sdmxsource.sdmx.util.beans.container.SdmxBeansImpl;
 import org.virtualrepository.Asset;
 import org.virtualrepository.impl.Type;
 import org.virtualrepository.spi.Publisher;
 
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.Statement;
 import com.hp.hpl.jena.rdf.model.StmtIterator;
 import com.hp.hpl.jena.sparql.util.FmtUtils;
 import com.hp.hpl.jena.update.UpdateExecutionFactory;
 import com.hp.hpl.jena.update.UpdateFactory;
 import com.hp.hpl.jena.update.UpdateRequest;
 
 /**
  * A {@link Publisher} for the Semantic Repository that works with RDF models of
  * arbitrary asset types.
  *
  * @author Fabio Simeoni
  *
  * @param <A> the type of Assets published by this publisher
  */
 public class RepositoryPublisher<A extends Asset> implements Publisher<A, Model> {
 
     private final RepositoryConfiguration configuration;
     private final Type<A> assetType;
 
     public RepositoryPublisher(Type<A> assetType, RepositoryConfiguration configuration) {
         this.assetType = assetType;
         this.configuration = configuration;
     }
 
     @Override
     public Type<A> type() {
         return assetType;
     }
 
     @Override
     public Class<Model> api() {
         return Model.class;
     }
 
     @Override
     public void publish(A asset, Model rdf) throws Exception {
 
         System.out.println("publishing to " + configuration.publishURI());
         rdf.write(System.out);
         StmtIterator stmts = rdf.listStatements();
         String triples = "";
         while (stmts.hasNext()) {
             Statement s = stmts.next();
            triples = FmtUtils.stringForTriple(s.asTriple()) + ".";
         }
         
         UpdateExecutionFactory.createRemote(UpdateFactory.create("insert data {" + triples + "}"), configuration.publishURI().toString()).execute();
        System.out.println(QueryExecutionFactory.sparqlService("http://168.202.3.223:3030/ds/query", "ask {" + triples + "}").execAsk());
 
 
     }
 
     //helpers
     Source xmlOf(CodelistBean bean) {
 
         SdmxBeans beans = new SdmxBeansImpl();
         beans.addCodelist(bean);
 
         ByteArrayOutputStream stream = new ByteArrayOutputStream(1024);
 
         STRUCTURE_OUTPUT_FORMAT format = STRUCTURE_OUTPUT_FORMAT.SDMX_V21_STRUCTURE_DOCUMENT;
 
         StructureWritingManager manager = new StructureWritingManagerImpl();
         manager.writeStructures(beans, format, stream);
 
         return new StreamSource(new ByteArrayInputStream(stream.toByteArray()));
     }
 }
