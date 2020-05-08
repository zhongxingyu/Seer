 package org.bireme.dia.analysis;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.apache.lucene.document.Document;
 import org.apache.lucene.index.DirectoryReader;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.search.PhraseQuery;
 import org.apache.lucene.search.TopDocs;
 import org.apache.lucene.store.FSDirectory;
 import org.apache.lucene.store.IOContext;
 import org.apache.lucene.store.RAMDirectory;
 
 public class DeCSEngine implements SynonymEngine {
     private final IndexSearcher searcher;
 
     // flag para informar se sera gerado chaves com as categorias e sinomimos dos descritores
     private final boolean addCategory;
     private final boolean addSyn;
     private final boolean keysForQualifiers;
     private final boolean onlyQualifiers;
 
     public DeCSEngine(final String indexPath,
                       final boolean category,
                       final boolean syn,
                       final boolean keyqlf,
                       final boolean onlyqlf) throws IOException {
 
         this.addCategory = category;
         this.addSyn = syn;
         this.keysForQualifiers = keyqlf;
         this.onlyQualifiers = onlyqlf;
 
         final File indexDir = new File(indexPath);
         final RAMDirectory ramDir = new RAMDirectory(
                                     FSDirectory.open(indexDir), IOContext.READ);
         final DirectoryReader reader = DirectoryReader.open(ramDir);
 
         searcher = new IndexSearcher(reader);
     }
 
     @Override
     public String[] getSynonyms(final String term) throws IOException {
         final ArrayList<String> synList = new ArrayList<String>();
         final String descriptor;
         final String qualifier;
         final String searchIndex;
         Document decsTerm = null;
 
         // tratamento para descritor padrao LILACS
         if (term.startsWith("^d")) {
             //retira antes de indexar o qualificador
             if (term.contains("^s")) {
                 descriptor = term.substring(2,term.indexOf("^s"));
                 qualifier =  term.substring(term.indexOf("^s")+2);
             } else {
                 descriptor = term.substring(2);
                 qualifier = null;
             }
         } else {
             // tratamento para descritores livres /
             if (term.contains("/")){
                 descriptor = term.substring(0,term.indexOf('/'));
                 qualifier = term.substring(term.indexOf('/') + 1);
             } else {
                 descriptor = term;
                 qualifier = null;
             }
         }
         // verifica se eh um descritor codificado DeCS e seta o indice de busca
         if (descriptor.matches("[0-9]+")) {
             searchIndex = "id";
         } else {
             searchIndex = "descriptor";
         }
 
         if (onlyQualifiers && (qualifier == null)) {
             return null;
         }
 
         if (keysForQualifiers && (qualifier != null)) {
             final Document decsQlf = decsKey(qualifier, searchIndex);
             if (decsQlf != null) {
                 synList.addAll(extractKeyValues(decsQlf));
             }
             if (onlyQualifiers) {
                 return synList.toArray(new String[0]);
             }
         }
 
         if (!descriptor.isEmpty()) {
             decsTerm = decsKey(descriptor, searchIndex);
             if (decsTerm != null){
                 synList.addAll(extractKeyValues(decsTerm));
             }
         }
 
         if (keysForQualifiers && (qualifier != null)){
             final Document decsQlf = decsKey(qualifier, searchIndex);
             if (decsQlf != null){
                 synList.addAll(extractKeyValues(decsQlf));
             }
         }
 
         // realiza o join entre o descritor e qualificador (usando o campo de termo autorizado)
         if ((qualifier != null) && !qualifier.isEmpty()) {
             final Document decsQlf = decsKey(qualifier, searchIndex);
 
             if ((decsTerm != null) && (decsQlf != null)) {
                 final String[] joinDesc = decsTerm.getValues("descriptor_full");
                 final String[] joinQlf  = decsQlf.getValues("descriptor_full");
 
                 for (int i = 0; i < joinDesc.length; i++) {
                     String joinDescQlf = joinDesc[i];
                     if ( ! joinQlf[i].startsWith("/") ) {
                         joinDescQlf += "/";
                     }
                     joinDescQlf  += joinQlf[i];
                     synList.add(joinDescQlf);
 
                     // adiciona tambem a chave contendo o descritor com o qualificador no formato abreviatura (ex. /di = diagnostico)
                     if (decsQlf.get("abbreviation") != null){
                         final String abbreviation = decsQlf.get("abbreviation");
                         joinDescQlf = joinDesc[i] + "/" + abbreviation;
                         synList.add(joinDescQlf);
                     }
                 }
             }
         }
 
         return synList.toArray(new String[0]);
     }
 
     private Document decsKey(final String code,
                              final String index) throws IOException {
         final PhraseQuery query = new PhraseQuery();
         Document key = null;
 
         // remove zeros a esquerda do codigo para match com indice de ID do DeCS
         final String code2 = code.replaceAll("^0*","");
 
         query.add(new Term(index, code2));
 
         final TopDocs hits = searcher.search(query, 1);
         if (hits.totalHits > 0) {
             final int docID = hits.scoreDocs[0].doc;
             key = searcher.doc(docID);
         }
 
         return key;
     }
 
     private List<String> extractKeyValues(final Document key) {
        // add authorized terms in 3 languages
        final List<String> keyValues = Arrays.asList(key.getValues("descriptor"));
 
         // add descriptor synonymous in search keys
         if (addSyn) {            
            //keyValues.addAll(Arrays.asList(key.getValues("syn")));
            for (String val : key.getValues("syn")) {
                keyValues.add(val);
            }
         }
         // add descriptor category in search keys
         if (addCategory) {
            //keyValues.addAll(Arrays.asList(key.getValues("category")));
            for (String val : key.getValues("category")) {
                keyValues.add(val);
            }
         }
 
         return keyValues;
     }
 }
