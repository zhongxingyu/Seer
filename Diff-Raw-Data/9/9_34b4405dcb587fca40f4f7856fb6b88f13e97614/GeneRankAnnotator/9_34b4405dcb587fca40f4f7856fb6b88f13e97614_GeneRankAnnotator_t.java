 package txtfnnl.uima.analysis_component;
 
 import ciir.umass.edu.learning.DataPoint;
 import ciir.umass.edu.learning.RankList;
 import org.apache.uima.analysis_component.AnalysisComponent;
 import org.apache.uima.cas.FSIterator;
 import org.apache.uima.jcas.JCas;
 import org.apache.uima.jcas.cas.FSArray;
 import org.apache.uima.jcas.tcas.Annotation;
 import org.apache.uima.resource.ExternalResourceDescription;
 import org.apache.uima.util.Level;
 import org.uimafit.descriptor.ConfigurationParameter;
 import org.uimafit.descriptor.ExternalResource;
 import txtfnnl.uima.cas.Property;
 import txtfnnl.uima.resource.StringMapResource;
 import txtfnnl.uima.tcas.SemanticAnnotation;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * Created with IntelliJ IDEA. User: fleitner Date: 27/06/2013 Time: 19:05 To change this template
  * use File | Settings | File Templates.
  */
 public
 class GeneRankAnnotator extends RankedListAnnotator {
 
   public static final String MODEL_KEY_GENE_SYMBOL_CNT_RESOURCE = "GeneSymbolCountsResource";
   @ExternalResource(key = MODEL_KEY_GENE_SYMBOL_CNT_RESOURCE, mandatory = true)
   private StringMapResource<Map<String, Integer>> geneSymbolCounts;
 
   public static final String MODEL_KEY_SYMBOL_CNT_RESOURCE = "SymbolCountsResource";
   @ExternalResource(key = MODEL_KEY_SYMBOL_CNT_RESOURCE, mandatory = true)
   private StringMapResource<Integer> symbolCounts;
 
   public static final String MODEL_KEY_GENE_LINK_CNT_RESOURCE = "GeneLinkCountsResource";
   @ExternalResource(key = MODEL_KEY_GENE_LINK_CNT_RESOURCE, mandatory = true)
   private StringMapResource<Integer> geneLinkCounts;
 
   public static final String PARAM_TAXA_ANNOTATOR_URI = "TaxaAnnotatorUri";
   @ConfigurationParameter(name = PARAM_TAXA_ANNOTATOR_URI,
                           description = "The annotator URI that made the taxon ID annotations.")
   private String taxaAnnotatorUri;
 
   public static final String PARAM_TAXA_NAMESPACE = "TaxaNamespace";
   @ConfigurationParameter(name = PARAM_TAXA_NAMESPACE,
                           description = "The NS in which the taxon ID annotations were made.",
                           defaultValue = LinnaeusAnnotator.DEFAULT_NAMESPACE)
   private String taxaNamespace;
 
   public static
   class Builder extends RankedListAnnotator.Builder {
     protected
     Builder(Class<? extends AnalysisComponent> klass,
             ExternalResourceDescription rankerResourceDescription) {
       super(klass, rankerResourceDescription);
     }
 
     /**
      * Create a new configuration builder; Note that the counter descriptors are mandatory values
      * and should be added before building the configuration.
      */
     public
     Builder(ExternalResourceDescription rankerResourceDescription) {
       this(GeneRankAnnotator.class, rankerResourceDescription);
     }
 
     /**
      * Add the required .tsv file containing three columns: a gene ID, its symbol, and the count for
      * that particular pair.
      */
     public
     Builder setGeneSymbolCounts(ExternalResourceDescription res) {
       setRequiredParameter(MODEL_KEY_GENE_SYMBOL_CNT_RESOURCE, res);
       return this;
     }
 
     /** Add the required .tsv file containing two columns: a gene symbol and its count. */
     public
     Builder setSymbolCounts(ExternalResourceDescription res) {
       setRequiredParameter(MODEL_KEY_SYMBOL_CNT_RESOURCE, res);
       return this;
     }
 
     /**
      * Add the required .tsv file containing two columns:a  gene ID and its count of associated
      * reference links.
      */
     public
     Builder setGeneLinkCounts(ExternalResourceDescription res) {
       setRequiredParameter(MODEL_KEY_GENE_LINK_CNT_RESOURCE, res);
       return this;
     }
 
     /** Set the annotator URI of taxa annotations to use for filtering annotations. */
     public
     Builder setTaxaAnnotatorUri(String uri) {
       setOptionalParameter(PARAM_TAXA_ANNOTATOR_URI, uri);
       return this;
     }
 
     /** Set the namespace of taxa annotations to use for filtering annotations. */
     public
     Builder setTaxaNamespace(String ns) {
       setOptionalParameter(PARAM_TAXA_NAMESPACE, ns);
       return this;
     }
   }
 
   public static
   Builder configure(ExternalResourceDescription rankerResourceDescription) {
     return new Builder(rankerResourceDescription);
   }
 
   @Override
   protected
   void process(JCas jcas, RankList rl) {
     Map<String, Map<String, String>> ranks = new HashMap<String, Map<String, String>>();
     for (int i = 0; i < rl.size(); ++i) {
       DataPoint dp = rl.get(i);
      String geneIdName = dp.getDescription().substring(2);
       String geneId = geneIdName.substring(0, geneIdName.indexOf(':'));
       String name = geneIdName.substring(geneIdName.indexOf(':') + 1);
       logger.log(Level.FINE, "ranked geneId=''{0}'' name=''{1}''", new String[] { geneId, name});
       if (!ranks.containsKey(geneId)) {
         Map<String, String> nameRanks = new HashMap<String, String>();
         nameRanks.put(name, String.format("%f", dp.getLabel()));
         ranks.put(geneId, nameRanks);
       } else {
         ranks.get(geneId).put(name, String.format("%f", dp.getLabel()));
       }
     }
     FSIterator<Annotation> it = getAnnotationIterator(jcas);
     while (it.hasNext()) {
       SemanticAnnotation ann = (SemanticAnnotation) it.next();
       String geneId = ann.getIdentifier();
       String name = ann.getCoveredText();
       Property rank = new Property(jcas);
       rank.setName(RANK_PROPERTY);
       logger.log(Level.FINE, "annotated geneId=''{0}'' name=''{1}''", new String[] { geneId, name});
       rank.setValue(ranks.get(geneId).get(name));
       ann.addProperty(jcas, rank);
     }
   }
 
   @Override
   protected
   RankList getRankedList(JCas jcas, String qid) {
     FSIterator<Annotation> it = getAnnotationIterator(jcas);
     Set<String> done = new HashSet<String>();
     RankList rl = new RankList();
     Map<String, Integer> geneIds = new HashMap<String, Integer>();
     Map<String, Integer> names = new HashMap<String, Integer>();
     Map<String, Integer> geneIdNamePairs = new HashMap<String, Integer>();
     Map<String, Integer> links = new HashMap<String, Integer>();
     Map<String, Integer> symbols = new HashMap<String, Integer>();
     Map<String, Integer> geneIdSymbolPairs = new HashMap<String, Integer>();
     while (it.hasNext()) {
       SemanticAnnotation ann = (SemanticAnnotation) it.next();
       String geneId = ann.getIdentifier();
       String name = ann.getCoveredText();
       String geneIdName = String.format("%s:%s", geneId, name);
       String symbol = null;
       FSArray props = ann.getProperties();
       for (int i = 0; i < props.size(); ++i) {
         Property p = (Property) props.get(i);
         if (p.getName().equals("name")) symbol = p.getValue();
       }
       String geneIdSymbol = String.format("%s:%s", geneId, symbol);
       if (geneIds.containsKey(geneId)) {
         geneIds.put(geneId, geneIds.get(geneId) + 1);
       } else {
         geneIds.put(geneId, 1);
       }
       if (names.containsKey(name)) {
         names.put(name, names.get(name) + 1);
       } else {
         names.put(name, 1);
       }
       if (geneIdNamePairs.containsKey(geneIdName)) {
         geneIdNamePairs.put(geneIdName, geneIdNamePairs.get(geneIdName) + 1);
       } else {
         geneIdNamePairs.put(geneIdName, 1);
       }
       if (geneIds.containsKey(geneId)) {
         geneIds.put(geneId, geneIds.get(geneId) + 1);
       } else {
         geneIds.put(geneId, 1);
       }
       if (!links.containsKey(geneId)) {
         if (geneLinkCounts.containsKey(geneId)) {
           links.put(geneId, geneLinkCounts.get(geneId));
         } else {
           links.put(geneId, 0);
         }
       }
       if (!symbols.containsKey(symbol)) {
         if (symbolCounts.containsKey(symbol)) {
           symbols.put(symbol, symbolCounts.get(symbol));
         } else {
           symbols.put(symbol, 0);
         }
       }
       if (!geneIdSymbolPairs.containsKey(geneIdSymbol)) {
         if (geneSymbolCounts.containsKey(geneId) &&
             geneSymbolCounts.get(geneId).containsKey(symbol)) {
           geneIdSymbolPairs.put(geneIdSymbol, geneSymbolCounts.get(geneId).get(symbol));
         } else {
           geneIdSymbolPairs.put(geneIdSymbol, 0);
         }
       }
     }
     Map<String, Integer> taxIds = new HashMap<String, Integer>();
     FSIterator<Annotation> taxIt = jcas.createFilteredIterator(
         SemanticAnnotation.getIterator(jcas),
         SemanticAnnotation.makeConstraint(jcas, taxaAnnotatorUri, taxaNamespace)
     );
     while (taxIt.hasNext()) {
       SemanticAnnotation ann = (SemanticAnnotation) taxIt.next();
       String taxId = ann.getIdentifier();
       if (taxIds.containsKey(taxId)) {
         taxIds.put(taxId, taxIds.get(taxId) + 1);
       } else {
         taxIds.put(taxId, 1);
       }
     }
     double normGeneIds = getNorm(geneIds);
     double normNames = getNorm(names);
     double normGeneIdNamePairs = getNorm(geneIdNamePairs);
     double normLinks = getNorm(links);
     double normSymbols = getNorm(symbols);
     double normGeneIdSymbolPairs = getNorm(geneIdSymbolPairs);
     double normTaxIds = getNorm(taxIds);
     it.moveToFirst();
     while (it.hasNext()) {
       SemanticAnnotation ann = (SemanticAnnotation) it.next();
       String geneId = ann.getIdentifier();
       String name = ann.getCoveredText();
       String geneIdName = String.format("%s:%s", geneId, name);
       if (!done.contains(geneIdName)) {
         done.add(geneIdName);
         String symbol = null;
         String taxId = null;
         FSArray props = ann.getProperties();
         for (int i = 0; i < props.size(); ++i) {
           Property p = (Property) props.get(i);
           if (p.getName().equals("name")) symbol = p.getValue();
           if (p.getName().equals(GeneAnnotator.TAX_ID_PROPERTY)) taxId = p.getValue();
         }
         String geneIdSymbol = String.format("%s:%s", geneId, symbol);
         double[] features = new double[] {
             // 1 string similarity
             ann.getConfidence(),
             // 2 gene ID count
             geneIds.get(geneId) / normGeneIds,
             // 3 actual name count
             names.get(name) / normNames,
             // 4 actual name, gene ID pair count
             geneIdNamePairs.get(geneIdName) / normGeneIdNamePairs,
             // 5 gene link count
             links.get(geneId) / normLinks,
             // 6 symbol count
             symbols.get(symbol) / normSymbols,
             // 7 gene symbol counts
             geneIdSymbolPairs.get(geneIdSymbol) / normGeneIdSymbolPairs,
             // 8 tax ID counts
             taxIds.get(taxId) / normTaxIds
         };
         rl.add(makeDataPoint(0.5F, qid, geneIdName, features));
       }
     }
     return rl;
   }
 
   private
   double getNorm(Map<String, Integer> counts) {
     int norm = 0;
     for (int i : counts.values())
       norm = Math.max(norm, i);
     return (double) norm;
   }
 }
