 package info.gehrels.diplomarbeit.hypergraphdb;
 
 import info.gehrels.diplomarbeit.CachingImporter;
 import info.gehrels.diplomarbeit.Measurement;
 import info.gehrels.diplomarbeit.Node;
 import org.hypergraphdb.HGHandle;
 import org.hypergraphdb.HGValueLink;
 import org.hypergraphdb.HyperGraph;
 import org.hypergraphdb.indexing.ByTargetIndexer;
 import org.hypergraphdb.indexing.DirectValueIndexer;
 import static info.gehrels.diplomarbeit.Measurement.measure;
 import static info.gehrels.diplomarbeit.hypergraphdb.HyperGraphDBHelper.createHyperGraphDB;
 
 
 public class HyperGraphDBImporter extends CachingImporter<HGHandle> {
   private final HyperGraph hyperGraph;
 
   public static void main(final String[] args) throws Exception {
     measure(new Measurement<Void>() {
         @Override
         public void execute(Void database) throws Exception {
           new HyperGraphDBImporter(args[0], args[1]).importNow();
         }
       });
   }
 
   public HyperGraphDBImporter(String sourceFile, String dbPath) throws Exception {
     super(sourceFile);
     hyperGraph = createHyperGraphDB(dbPath);
 
     // For Nodes
     hyperGraph.getIndexManager().register(
       new DirectValueIndexer(hyperGraph.getTypeSystem().getTypeHandle(Long.class)));
 
     // For Edge labels
     hyperGraph.getIndexManager()
     .register(
       new DirectValueIndexer(hyperGraph.getTypeSystem().getTypeHandle(String.class)));
 
     // Index incoming edges
     hyperGraph.getIndexManager()
     .register(
      new ByTargetIndexer(hyperGraph.getTypeSystem().getTypeHandle(HGValueLink.class), 0));
 
     // Index outgoing edges
     hyperGraph.getIndexManager()
     .register(
      new ByTargetIndexer(hyperGraph.getTypeSystem().getTypeHandle(HGValueLink.class), 1));
   }
 
   @Override
   protected void createEdgeBetweenCachedNodes(HGHandle from, HGHandle to, String label) throws Exception {
     hyperGraph.add(new HGValueLink(label, from, to));
   }
 
   @Override
   protected HGHandle createNodeForCache(Node node) {
     return hyperGraph.add(node.id);
   }
 }
