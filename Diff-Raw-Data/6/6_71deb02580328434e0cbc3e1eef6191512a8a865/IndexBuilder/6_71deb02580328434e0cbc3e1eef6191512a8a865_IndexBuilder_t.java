 package com.subsolr.index;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.analysis.standard.StandardAnalyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.IndexWriterConfig;
 import org.apache.lucene.index.IndexableField;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.FSDirectory;
 import org.apache.lucene.util.Version;
 import org.apache.solr.schema.FieldType;
 import org.apache.solr.schema.SchemaField;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.InitializingBean;
 
 import com.subsolr.contextprocessor.DocumentContextProcessor;
 import com.subsolr.contextprocessor.FieldContextProcessor;
 import com.subsolr.contextprocessor.model.DocumentDefinition;
 import com.subsolr.contextprocessor.model.FieldDefinition;
 import com.subsolr.entityprocessors.model.Record;
 
 public class IndexBuilder implements InitializingBean {
 
 	private Version luceneVersion = null;
 	private String luceneDirectory = null;
 	private DocumentContextProcessor documentContextProcessor = null;
 	private FieldContextProcessor fieldContextProcessor = null;
 	public static final Logger logger = LoggerFactory.getLogger(IndexBuilder.class);
 
 	protected final static int INDEXED = 0x00000001;
 	protected final static int TOKENIZED = 0x00000002;
 	protected final static int STORED = 0x00000004;
 	protected final static int BINARY = 0x00000008;
 	protected final static int OMIT_NORMS = 0x00000010;
 	protected final static int OMIT_TF_POSITIONS = 0x00000020;
 	protected final static int STORE_TERMVECTORS = 0x00000040;
 	protected final static int STORE_TERMPOSITIONS = 0x00000080;
 	protected final static int STORE_TERMOFFSETS = 0x00000100;
 
 	protected final static int MULTIVALUED = 0x00000200;
 	protected final static int SORT_MISSING_FIRST = 0x00000400;
 	protected final static int SORT_MISSING_LAST = 0x00000800;
 
 	protected final static int REQUIRED = 0x00001000;
 	protected final static int OMIT_POSITIONS = 0x00002000;
 
 	protected final static int STORE_OFFSETS = 0x00004000;
 	protected final static int DOC_VALUES = 0x00008000;
 
 	static final String[] propertyNames = { "indexed", "tokenized", "stored", "binary", "omitNorms", "omitTermFreqAndPositions", "termVectors", "termPositions", "termOffsets", "multiValued",
 			"sortMissingFirst", "sortMissingLast", "required", "omitPositions", "storeOffsetsWithPositions", "docValues" };
 
 	public IndexBuilder(Version luceneVersion, String luceneDirectory, DocumentContextProcessor documentContextProcessor, FieldContextProcessor fieldContextProcessor) {
 		this.luceneDirectory = luceneDirectory;
 		this.documentContextProcessor = documentContextProcessor;
 		this.fieldContextProcessor = fieldContextProcessor;
 		this.luceneVersion = luceneVersion;
 	}
 
 	public void indexRecordsForDocument(List<Record> recordLists, String documentName) throws IOException, IllegalArgumentException, InstantiationException, IllegalAccessException,
 			InvocationTargetException, SecurityException, NoSuchMethodException {
 		IndexWriter writer = getIndexWriterForDocument(false, documentName);
 		logger.debug("processing documentName -  " + documentName);
 
 		for (Record record : recordLists) {
 			Document doc = new Document();
 			Map<String, String> valueByIndexName = record.getValueByIndexName();
 			for (Entry<String, String> fieldDefEntry : valueByIndexName.entrySet()) {
 
 				FieldDefinition fieldDefinition = fieldContextProcessor.getFieldDefinitionsByName(fieldDefEntry.getKey());
 				logger.debug("processing fieldDefinition -  " + fieldDefinition.getFieldName());
 				Class<? extends FieldType> fieldTypeClassName = fieldDefinition.getFieldTypeDefinition().getFieldTypeClassName();
 				FieldType fieldType = fieldTypeClassName.newInstance();
 				List<Analyzer> analyzer = fieldDefinition.getFieldTypeDefinition().getAnalyzer();
 				if (analyzer.size() != 0) {
 					fieldType.setAnalyzer(analyzer.get(0));
					fieldType.setIsExplicitAnalyzer(true);
 				}
 				//fieldType.setSimilarity(fieldDefinition.getFieldTypeDefinition().getSimilarityClassName());//TODO
 				// Similarity
 				SchemaField schemaField = new SchemaField(fieldDefinition.getFieldName(), fieldType, calcProps(fieldDefinition.getFieldName(), fieldType, fieldDefinition.getFieldProperties()), "");
 				IndexableField field = schemaField.createField(fieldDefEntry.getValue(), 1.0f);
 				doc.add(field);
 
 			}
 			writer.addDocument(doc);
			writer.commit();
 			logger.debug("processing done for  documentName -  " + documentName);
 
 		}
 		writer.close();
 
 	}
 
 	static int parseProperties(Map<String, ?> properties, boolean which, boolean failOnError) {
 		int props = 0;
 		for (Map.Entry<String, ?> entry : properties.entrySet()) {
 			Object val = entry.getValue();
 			if (val == null)
 				continue;
 			boolean boolVal = val instanceof Boolean ? (Boolean) val : Boolean.parseBoolean(val.toString());
 			if (boolVal == which) {
 				props |= propertyNameToInt(entry.getKey(), failOnError);
 			}
 		}
 		return props;
 	}
 
 	static int propertyNameToInt(String name, boolean failOnError) {
 		for (int i = 0; i < propertyNames.length; i++) {
 			if (propertyNames[i].equals(name)) {
 				return 1 << i;
 			}
 		}
 		if (failOnError && !"default".equals(name)) {
 			throw new IllegalArgumentException("Invalid field property: " + name);
 		} else {
 			return 0;
 		}
 	}
 
 	static int calcProps(String name, FieldType ft, Map<String, ?> props) {
 		int trueProps = parseProperties(props, true, true);
 		int falseProps = parseProperties(props, false, true);
 
 		int p = 0;
 
 		//
 		// If any properties were explicitly turned off, then turn off other
 		// properties
 		// that depend on that.
 		//
 		if (on(falseProps, STORED)) {
 			int pp = STORED | BINARY;
 			if (on(pp, trueProps)) {
 				throw new RuntimeException("SchemaField: " + name + " conflicting stored field options:" + props);
 			}
 			p &= ~pp;
 		}
 
 		if (on(falseProps, INDEXED)) {
 			int pp = (INDEXED | STORE_TERMVECTORS | STORE_TERMPOSITIONS | STORE_TERMOFFSETS);
 			if (on(pp, trueProps)) {
 				throw new RuntimeException("SchemaField: " + name + " conflicting 'true' field options for non-indexed field:" + props);
 			}
 			p &= ~pp;
 		}
 
 		if (on(falseProps, INDEXED) && on(falseProps, DOC_VALUES)) {
 			int pp = (SORT_MISSING_FIRST | SORT_MISSING_LAST);
 			if (on(pp, trueProps)) {
 				throw new RuntimeException("SchemaField: " + name + " conflicting 'true' field options for non-indexed/non-docValues field:" + props);
 			}
 			p &= ~pp;
 		}
 
 		if (on(falseProps, INDEXED)) {
 			int pp = (OMIT_NORMS | OMIT_TF_POSITIONS | OMIT_POSITIONS);
 			if (on(pp, falseProps)) {
 				throw new RuntimeException("SchemaField: " + name + " conflicting 'false' field options for non-indexed field:" + props);
 			}
 			p &= ~pp;
 
 		}
 
 		if (on(trueProps, OMIT_TF_POSITIONS)) {
 			int pp = (OMIT_POSITIONS | OMIT_TF_POSITIONS);
 			if (on(pp, falseProps)) {
 				throw new RuntimeException("SchemaField: " + name + " conflicting tf and position field options:" + props);
 			}
 			p &= ~pp;
 		}
 
 		if (on(falseProps, STORE_TERMVECTORS)) {
 			int pp = (STORE_TERMVECTORS | STORE_TERMPOSITIONS | STORE_TERMOFFSETS);
 			if (on(pp, trueProps)) {
 				throw new RuntimeException("SchemaField: " + name + " conflicting termvector field options:" + props);
 			}
 			p &= ~pp;
 		}
 
 		// override sort flags
 		if (on(trueProps, SORT_MISSING_FIRST)) {
 			p &= ~SORT_MISSING_LAST;
 		}
 
 		if (on(trueProps, SORT_MISSING_LAST)) {
 			p &= ~SORT_MISSING_FIRST;
 		}
 
 		p &= ~falseProps;
 		p |= trueProps;
 		return p;
 	}
 
 	static boolean on(int bitfield, int props) {
 		return (bitfield & props) != 0;
 	}
 
 	static boolean off(int bitfield, int props) {
 		return (bitfield & props) == 0;
 	}
 
 	private IndexWriter getIndexWriterForDocument(boolean b, String documentName) throws IOException {
 
 		StandardAnalyzer analyzer = new StandardAnalyzer(luceneVersion);
		Directory index = FSDirectory.open(new File(luceneDirectory + File.separator + documentName));
 		IndexWriterConfig config = new IndexWriterConfig(luceneVersion, analyzer);
 		IndexWriter indexWriter = new IndexWriter(index, config);
 
 		return indexWriter;
 	}
 
 	public void rebuildIndexes() throws IOException {
 	}
 
 	public void afterPropertiesSet() throws Exception {
 		Map<String, DocumentDefinition> documentDefinitions = documentContextProcessor.getDocumentDefinitions();
 		for (Entry<String, DocumentDefinition> documentEntryDefinition : documentDefinitions.entrySet()) {
 			String doucmentName = documentEntryDefinition.getKey();
 			DocumentDefinition documentDefinition = documentEntryDefinition.getValue();
 			List<Record> recordsToBeIndexed = documentDefinition.getRecordsToBeIndexed();
 			indexRecordsForDocument(recordsToBeIndexed, doucmentName);
 
 		}
 
 	}
 
 }
