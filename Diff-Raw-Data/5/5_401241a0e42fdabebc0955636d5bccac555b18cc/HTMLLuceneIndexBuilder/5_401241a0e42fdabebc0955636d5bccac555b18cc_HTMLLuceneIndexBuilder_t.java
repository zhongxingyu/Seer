 package edu.columbia.watson.twitter;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.analysis.standard.StandardAnalyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.document.LongField;
 import org.apache.lucene.document.StringField;
 import org.apache.lucene.document.TextField;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.IndexWriterConfig;
 import org.apache.lucene.index.IndexWriterConfig.OpenMode;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.FSDirectory;
 import org.apache.lucene.util.Version;
 
 public class HTMLLuceneIndexBuilder {
 	private static final String docsPath = "/mnt/corpus/post_process/htmls";
 	private static final String indexPath = "/mnt/corpus/html_index";
 
 	private static void indexDocs(IndexWriter writer, File file) throws IOException{
 		if (file.canRead()) {
 			if (file.isDirectory()) {
 				String[] files = file.list();
 				if (files != null) {
 					for (int i = 0; i < files.length; i++) {
 						indexDocs(writer, new File(file, files[i]));
 					}
 				}
 			} else {
 				FileInputStream fis;
 				try {
 					fis = new FileInputStream(file);
 				} catch (FileNotFoundException fnfe) {
 					return;
 				}
 				try {
 					Document doc = new Document();
 					String fullName = file.getName();
					int index = fullName.indexOf('.');
					if (index == -1)
						return;
					Long id = Long.parseLong(fullName.substring(0,index));
 					doc.add(new StringField("path", file.getAbsolutePath(), Field.Store.YES));	
 					doc.add(new TextField("contents", new BufferedReader(new InputStreamReader(fis, "UTF-8"))));
 					doc.add(new LongField("tweetID", id, Field.Store.NO));
 					writer.addDocument(doc);
 				}finally {
 					fis.close();
 				}
 
 			}
 		}
 	}
 
 
 	public static void main(String args[]) throws IOException{
 	
 		final File docDir = new File(docsPath);
 		if (!docDir.exists() || !docDir.canRead()) {
 			System.out.println("Document directory '" + docDir.getAbsolutePath()+ "' does not exist or is not readable, please check the path");
 			System.exit(1);
 		}
 
 		System.out.println("Indexing to directory '" + indexPath + "'...");
 
 		Directory dir = FSDirectory.open(new File(indexPath));
 		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_42);
 
 		IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_42, analyzer);
 		iwc.setOpenMode(OpenMode.CREATE);
 
 		IndexWriter writer = new IndexWriter(dir, iwc);
 
 		System.out.println("Ready to index");
 		indexDocs(writer, docDir);
 
 		writer.close();
 		System.out.println("All done!");
 
 	}
 
 
 }
