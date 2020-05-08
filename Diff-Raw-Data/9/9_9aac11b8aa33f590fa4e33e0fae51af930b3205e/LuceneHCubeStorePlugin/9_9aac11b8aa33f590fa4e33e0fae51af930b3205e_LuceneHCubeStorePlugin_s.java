 package io.analytica.hcube.plugins.store.lucene;
 
 import io.analytica.hcube.cube.HCounterType;
 import io.analytica.hcube.cube.HCube;
 import io.analytica.hcube.cube.HMetric;
 import io.analytica.hcube.dimension.HCategory;
 import io.analytica.hcube.dimension.HCubeKey;
 import io.analytica.hcube.impl.HCubeStorePlugin;
 import io.analytica.hcube.query.HQuery;
 import io.analytica.hcube.result.HSerie;
 import io.vertigo.kernel.exception.VRuntimeException;
 import io.vertigo.kernel.lang.Assertion;
 
 import java.io.IOException;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.analysis.core.SimpleAnalyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.DoubleField;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.document.LongField;
 import org.apache.lucene.document.StringField;
 import org.apache.lucene.document.TextField;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.IndexWriterConfig;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.RAMDirectory;
 import org.apache.lucene.util.Version;
 
 public class LuceneHCubeStorePlugin implements HCubeStorePlugin {
 	private final Directory directory;
 	private final IndexWriter indexWriter;
 	//--
 	private final Set<HCategory> rootCategories;
 	private final Map<HCategory, Set<HCategory>> categories;
 
 	public LuceneHCubeStorePlugin() {
 		directory = new RAMDirectory();
 		indexWriter = createIndexWriter(directory);
 		//---------------------------------------------------------------------
 		rootCategories = new HashSet<>();
 		categories = new HashMap<>();
 	}
 
 	private static IndexWriter createIndexWriter(final Directory directory) {
 		try {
 			final Analyzer analyzer = new SimpleAnalyzer(Version.LUCENE_46);
 			final IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_46, analyzer);
 			return new IndexWriter(directory, config);
 		} catch (final IOException e) {
 			throw new VRuntimeException(e);
 		}
 	}
 
 	public void merge(String appName, HCube cube) {
 		Assertion.checkNotNull(cube);
 		//---------------------------------------------------------------------
 		addCategory(cube.getKey().getCategory());
 
 		try {
 			for (final HCubeKey cubeKey : cube.getKey().drillUp()) {
 				final Document document = new Document();
 				document.add(new LongField("time", cubeKey.getTime().inMillis(), Field.Store.YES));
 				document.add(new TextField("category", cubeKey.getCategory().getId(), Field.Store.YES));

 				for (final HMetric metric : cube.getMetrics()) {
 					document.add(new StringField("metric", metric.getKey().getName(), Field.Store.YES));
 					document.add(new LongField(metric.getKey().getName() + ":count", metric.getCount(), Field.Store.YES));
 					document.add(new DoubleField(metric.getKey().getName() + ":sum", metric.get(HCounterType.sum), Field.Store.YES));
 					document.add(new DoubleField(metric.getKey().getName() + ":min", metric.get(HCounterType.min), Field.Store.YES));
 					document.add(new DoubleField(metric.getKey().getName() + ":max", metric.get(HCounterType.max), Field.Store.YES));
 					document.add(new DoubleField(metric.getKey().getName() + ":sqrSum", metric.get(HCounterType.sqrSum), Field.Store.YES));
 					if (metric.getKey().hasDistribution()) {
 						for (Entry<Double, Long> entry : metric.getDistribution().getData().entrySet()) {
							document.add(new DoubleField(metric.getKey().getName() + ":distribution:" + entry.getKey(), entry.getValue(), Field.Store.YES));
 						}
 					}

 				}
 				indexWriter.addDocument(document);
 			}
 		} catch (final IOException e) {
 			throw new VRuntimeException(e);
 		}
 
 	}
 
 	public Set<HCategory> getAllSubCategories(String appName, HCategory category) {
 		Assertion.checkNotNull(category);
 		//---------------------------------------------------------------------
 
 		Set<HCategory> set = categories.get(category);
 		return set == null ? Collections.<HCategory> emptySet() : Collections.unmodifiableSet(set);
 	}
 
 	public Set<HCategory> getAllRootCategories(String appName) {
 		return Collections.unmodifiableSet(rootCategories);
 	}
 
 	public Map<HCategory, HSerie> findAll(String appName, HQuery query) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public long count(String appName) {
 		IndexReader reader;
 		try {
 			reader = IndexReader.open(directory);
 			return reader.numDocs();
 		} catch (IOException e) {
 			throw new VRuntimeException(e);
 		}
 	}
 
 	private void addCategory(HCategory category) {
 		Assertion.checkNotNull(category);
 		//---------------------------------------------------------------------
 		HCategory currentCategory = category;
 		HCategory parentCategory;
 		boolean drillUp;
 		do {
 			parentCategory = currentCategory.drillUp();
 			//Optim :Si la catgorie existe dj alors sa partie gauche aussi !!
 			//On dispose donc d'une info pour savoir si il faut remonter 
 			drillUp = doPut(parentCategory, currentCategory);
 			currentCategory = parentCategory;
 		} while (drillUp);
 	}
 
 	private boolean doPut(HCategory parentCategory, HCategory category) {
 		Assertion.checkNotNull(category);
 		//---------------------------------------------------------------------
 		if (parentCategory == null) {
 			//category est une catgorie racine
 			rootCategories.add(category);
 			return false;
 		}
 		//category n'est pas une catgorie racine
 		Set<HCategory> set = categories.get(parentCategory);
 		if (set == null) {
 			set = new HashSet<>();
 			categories.put(parentCategory, set);
 		}
 		return set.add(category);
 	}
 }
