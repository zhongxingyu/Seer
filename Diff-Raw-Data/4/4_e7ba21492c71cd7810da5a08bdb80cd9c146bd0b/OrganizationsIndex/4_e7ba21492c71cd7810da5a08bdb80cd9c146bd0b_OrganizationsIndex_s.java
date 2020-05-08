 // 
 // Copyright (c) 2004, Caltha - Gajda, Krzewski, Mach, Potempski Sp.J. 
 // All rights reserved. 
 // 
 // Redistribution and use in source and binary forms, with or without modification,  
 // are permitted provided that the following conditions are met: 
 // 
 // * Redistributions of source code must retain the above copyright notice,  
 //       this list of conditions and the following disclaimer. 
 // * Redistributions in binary form must reproduce the above copyright notice,  
 //       this list of conditions and the following disclaimer in the documentation  
 //       and/or other materials provided with the distribution. 
 // * Neither the name of the Caltha - Gajda, Krzewski, Mach, Potempski Sp.J.  
 //       nor the names of its contributors may be used to endorse or promote products  
 //       derived from this software without specific prior written permission. 
 // 
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"  
 // AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED  
 // WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 // IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,  
 // INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,  
 // BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
 // OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,  
 // WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)  
 // ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE  
 // POSSIBILITY OF SUCH DAMAGE. 
 // 
 
 package net.cyklotron.cms.ngodatabase.organizations;
 
 import java.io.IOException;
 import java.io.Reader;
 import java.util.Collections;
 import java.util.List;
 
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.analysis.LowerCaseFilter;
 import org.apache.lucene.analysis.TokenStream;
 import org.apache.lucene.analysis.standard.StandardFilter;
 import org.apache.lucene.analysis.standard.StandardTokenizer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.document.NumericField;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.search.BooleanClause;
 import org.apache.lucene.search.BooleanQuery;
 import org.apache.lucene.search.FieldCache;
 import org.apache.lucene.search.FieldComparator;
 import org.apache.lucene.search.FieldComparatorSource;
 import org.apache.lucene.search.FuzzyQuery;
 import org.apache.lucene.search.NumericRangeQuery;
 import org.apache.lucene.search.PrefixQuery;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.Sort;
 import org.apache.lucene.search.SortField;
 import org.apache.lucene.search.spans.SpanFirstQuery;
 import org.apache.lucene.search.spans.SpanTermQuery;
 import org.apache.lucene.util.Version;
 import org.jcontainer.dna.Logger;
 import org.objectledge.filesystem.FileSystem;
 import org.objectledge.utils.Timer;
 
 import net.cyklotron.cms.ngodatabase.AbstractIndex;
 import net.cyklotron.cms.ngodatabase.Organization;
 import net.cyklotron.cms.search.analysis.AlphanumericFilter;
 
 /**
  * @author lukasz, rafal
  */
 public class OrganizationsIndex
     extends AbstractIndex<Organization>
 {
     // constants /////////////////////////////////////////////////////////////
 
     private static final int FUZZY_QUERY_PREFIX_LENGTH = 4;
 
     private static final float FUZZY_QUERY_MIN_SIMILARITY = 0.75f;
 
     private static final int MAX_RESULTS = 25;
 
     private static final int MAX_TOKEN_LENGTH = 25;
 
     private static final String INDEX_PATH = "ngo/database/incoming/index";
 
     public OrganizationsIndex(FileSystem fileSystem, Logger log)
         throws IOException
     {
         super(fileSystem, log, INDEX_PATH);
     }
 
     protected Analyzer getAnalyzer(FileSystem fileSystem)
         throws IOException
     {
         return new OrganizationNameAnalyzer();
     }
 
     protected Document toDocument(Organization organization)
     {
         Document document = new Document();
         document
             .add(new NumericField("id", 4, Field.Store.YES, true).setLongValue(organization.getId()));
         document.add(new Field("name", organization.getName(), Field.Store.YES, Field.Index.ANALYZED,
             Field.TermVector.WITH_POSITIONS_OFFSETS));
         document.add(new Field("province", organization.getProvince(), Field.Store.YES,
             Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS));
         document.add(new Field("city", organization.getCity(), Field.Store.YES, Field.Index.ANALYZED,
             Field.TermVector.WITH_POSITIONS_OFFSETS));
         document.add(new Field("street", organization.getStreet(), Field.Store.YES,
             Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS));
         document.add(new Field("postCode", organization.getPostCode(), Field.Store.YES,
             Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS));
         return document;
     }
 
     protected Organization fromDocument(Document document)
     {
         long id = Long.parseLong(document.get("id"));
         String name = document.get("name");
         String province = document.get("province");
         String city = document.get("city");
         String street = document.get("street");
         String postCode = document.get("postCode");
         return new Organization(id, name, province, city, street, postCode);
     }
 
     public Organization getOrganization(Long id)
     {
         try
         {
             Query query = NumericRangeQuery.newLongRange("id", id, id, true, true);
             return singleResult(getSearcher().search(query, 1));
         }
         catch(Exception e)
         {
             logger.error("search error", e);
             return null;
         }
     }
 
     public List<Organization> getOrganizations(String name)
     {
         try
         {
             BooleanQuery query = new BooleanQuery();
             List<Term> terms = analyze("name", name);
             int i = 0;
             for(Term term : terms)
             {
 				if(FUZZY_QUERY_PREFIX_LENGTH < term.text().length())
 				{
 					FuzzyQuery fuzzyQuery = new FuzzyQuery(term,FUZZY_QUERY_MIN_SIMILARITY,FUZZY_QUERY_PREFIX_LENGTH);
 					fuzzyQuery.setBoost((1 - (getSearcher().docFreq(term) / getSearcher().maxDoc()))/10);
 					query.add(fuzzyQuery, BooleanClause.Occur.SHOULD);
 				}
 				SpanFirstQuery spanFirstQuery = new SpanFirstQuery(new SpanTermQuery(term), ++i);
             	spanFirstQuery.setBoost((terms.size()+1)-i);
             	query.add(spanFirstQuery, BooleanClause.Occur.SHOULD);
 				
                 PrefixQuery prefixQuery = new PrefixQuery(term);
                 prefixQuery.setBoost(1);
                 query.add(prefixQuery, BooleanClause.Occur.SHOULD);
             }
             terms = analyze("city", name);
 			SpanFirstQuery spanFirstQuery = new SpanFirstQuery(new SpanTermQuery(terms.get(terms.size()-1)), 1);
         	spanFirstQuery.setBoost(1);
         	query.add(spanFirstQuery, BooleanClause.Occur.SHOULD);
             PrefixQuery prefixQuery = new PrefixQuery(terms.get(terms.size()-1));
             prefixQuery.setBoost(1);
             query.add(prefixQuery, BooleanClause.Occur.SHOULD);
             
             Timer timer = new Timer();
             Sort sort = new Sort(new SortField[]{SortField.FIELD_SCORE,new SortField("name",new OrganizationNameFieldComparator()),new SortField("city",SortField.STRING)});
             List<Organization> results = results(getSearcher().search(query, null, MAX_RESULTS, sort));
             logger.debug("query: " + query.toString() + " " + results.size() + " in "
                 + timer.getElapsedMillis() + "ms");
             return results;
         }
         catch(Exception e)
         {
             logger.error("search error", e);
             return Collections.emptyList();
         }
     }
 
     private static class OrganizationNameAnalyzer
         extends Analyzer
     {
         private static final class SavedStreams
         {
             StandardTokenizer tokenStream;
 
             TokenStream filteredTokenStream;
         }
 
         @Override
         public TokenStream reusableTokenStream(String fieldName, Reader reader)
             throws IOException
         {
             SavedStreams streams = (SavedStreams)getPreviousTokenStream();
             if(streams == null)
             {
                 streams = new SavedStreams();
                 setPreviousTokenStream(streams);
                 streams.tokenStream = new StandardTokenizer(Version.LUCENE_30, reader);
                 streams.tokenStream.setMaxTokenLength(MAX_TOKEN_LENGTH);
                 streams.filteredTokenStream = new StandardFilter(streams.tokenStream);
                 streams.filteredTokenStream = new LowerCaseFilter(streams.filteredTokenStream);
                 streams.filteredTokenStream = new AlphanumericFilter(streams.filteredTokenStream);
             }
             else
             {
                 streams.tokenStream.reset(reader);
             }
             return streams.filteredTokenStream;
         }
 
         @Override
         public TokenStream tokenStream(String fieldName, Reader reader)
         {
             StandardTokenizer tokenStream = new StandardTokenizer(Version.LUCENE_30, reader);
             tokenStream.setMaxTokenLength(MAX_TOKEN_LENGTH);
             TokenStream filteredTokenStream = new StandardFilter(tokenStream);
             filteredTokenStream = new LowerCaseFilter(filteredTokenStream);
             filteredTokenStream = new AlphanumericFilter(filteredTokenStream);
             return filteredTokenStream;
         }
     }
     
     
     public class OrganizationNameFieldComparator
         extends FieldComparatorSource
     {
 
         public FieldComparator newComparator(String fieldname, int numHits, int sortPos,
             boolean reversed)
             throws IOException
         {
             return new OrganizationNameComparator(numHits, fieldname);
         }
 
         public class OrganizationNameComparator
             extends FieldComparator
         {
             private final String[] values;
 
             private String[] currentReaderValues;
 
             private final String field;
 
             private String bottom;
 
             OrganizationNameComparator(int numHits, String field)
             {
                 values = new String[numHits];
                 this.field = field;
             }
 
             public String getValue(String value)
             {
                 return value.replaceAll("[^\\p{L}\\p{N}]", "");
             }
 
             @Override
             public int compare(int slot1, int slot2)
             {
                final String slot1Value = values[slot1];
                final String slot2Value = values[slot2];
                 return slot1Value.compareTo(slot2Value);
             }
 
             @Override
             public int compareBottom(int doc)
             {
                 final String docValue = getValue(currentReaderValues[doc]);
                 final String bottomValue = getValue(bottom);
                 return bottomValue.compareTo(docValue);
             }
 
             @Override
             public void copy(int slot, int doc)
             {
                 values[slot] = currentReaderValues[doc];
             }
 
             @Override
             public void setNextReader(IndexReader reader, int docBase)
                 throws IOException
             {
                 currentReaderValues = FieldCache.DEFAULT.getStrings(reader, field);
             }
 
             @Override
             public void setBottom(final int bottom)
             {
                 this.bottom = values[bottom];
             }
 
             @Override
             public Comparable<? > value(int slot)
             {
                 return String.valueOf(getValue(values[slot]));
             }
 
         }
     }
 
 }
