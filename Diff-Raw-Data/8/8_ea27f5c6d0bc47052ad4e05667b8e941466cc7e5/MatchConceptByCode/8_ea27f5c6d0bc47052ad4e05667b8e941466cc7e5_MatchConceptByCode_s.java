 package gov.nih.nci.evs.browser.test;
 
 import java.util.*;
 import org.LexGrid.LexBIG.DataModel.Collections.*;
 import org.LexGrid.LexBIG.DataModel.Core.*;
 import org.LexGrid.LexBIG.LexBIGService.*;
import org.LexGrid.concepts.Concept;
 import org.LexGrid.LexBIG.Utility.Iterators.*;
 import org.LexGrid.LexBIG.Utility.*;
 
 import org.LexGrid.LexBIG.LexBIGService.CodedNodeSet.*;
 import org.apache.log4j.*;
 
 public class MatchConceptByCode {
     private static Logger _logger = Logger.getLogger(MatchConceptByCode.class);
 
     public MatchConceptByCode() {
     }
 
 
     public static LocalNameList vector2LocalNameList(Vector<String> v)
     {
         if (v == null) return null;
         LocalNameList list = new LocalNameList();
         for (int i=0; i<v.size(); i++)
         {
             String vEntry = (String) v.elementAt(i);
             list.addEntry(vEntry);
         }
         return list;
     }
 
 
     public static CodedNodeSet restrictToSource(CodedNodeSet cns, String source) {
         if (cns == null) return cns;
         if (source == null || source.compareTo("*") == 0 || source.compareTo("") == 0 || source.compareTo("ALL") == 0) return cns;
 
         LocalNameList contextList = null;
         LocalNameList sourceLnL = null;
         NameAndValueList qualifierList = null;
 
         Vector<String> w2 = new Vector<String>();
         w2.add(source);
         sourceLnL = vector2LocalNameList(w2);
         LocalNameList propertyLnL = null;
         CodedNodeSet.PropertyType[] types = new PropertyType[] {PropertyType.PRESENTATION};
         try {
             cns = cns.restrictToProperties(propertyLnL, types, sourceLnL, contextList, qualifierList);
         } catch (Exception ex) {
             _logger.error("restrictToSource throws exceptions.");
             return null;
         }
         return cns;
     }
 
     public static ConceptReferenceList createConceptReferenceList(
             String[] codes, String codingSchemeName) {
         if (codes == null) {
             return null;
         }
         ConceptReferenceList list = new ConceptReferenceList();
         for (int i = 0; i < codes.length; i++) {
             ConceptReference cr = new ConceptReference();
             cr.setCodingSchemeName(codingSchemeName);
             cr.setConceptCode(codes[i]);
             list.addConceptReference(cr);
         }
         return list;
     }
 
 
     protected void displayRef(ResolvedConceptReference ref){
         _logger.debug(ref.getConceptCode() + ":" + ref.getEntityDescription().getContent());
     }
 
 
 
     public static ResolvedConceptReferencesIterator matchConceptCode(String scheme, String version, String matchText, String source, String matchAlgorithm) {
 
 _logger.debug("Calling matchConceptCode " + matchText);
 _logger.debug("matchConceptCode scheme " + scheme);
 _logger.debug("matchConceptCode matchText " + matchText);
 _logger.debug("matchConceptCode matchAlgorithm " + matchAlgorithm);
 
         LexBIGService lbs = RemoteServerUtilTest.createLexBIGService();
         Vector v = new Vector();
         CodingSchemeVersionOrTag versionOrTag = new CodingSchemeVersionOrTag();
         if (version != null) versionOrTag.setVersion(version);
         CodedNodeSet cns = null;
         ResolvedConceptReferencesIterator iterator = null;
         try {
             cns = lbs.getNodeSet(scheme, versionOrTag, null);
             if (source != null) cns = restrictToSource(cns, source);
             CodedNodeSet.PropertyType[] propertyTypes = null;
             LocalNameList sourceList = null;
             LocalNameList contextList = null;
             NameAndValueList qualifierList = null;
             cns = cns.restrictToMatchingProperties(ConvenienceMethods.createLocalNameList(new String[]{"conceptCode"}),
                       propertyTypes, sourceList, contextList,
                       qualifierList,matchText, matchAlgorithm, null);
 
             LocalNameList restrictToProperties = new LocalNameList();
             SortOptionList sortCriteria = null;
             try {
                 boolean resolveConcepts = true;
                 try {
                     long ms = System.currentTimeMillis(), delay = 0;
                     iterator = cns.resolve(sortCriteria, null, restrictToProperties, null, resolveConcepts);
                     if (iterator == null) {
                         return matchConceptCode(scheme, version, matchText);
                     }
 
                     int size = iterator.numberRemaining();
                     _logger.debug("size: " + size);
 
                     // test
                     if (size == 0) {
                         return matchConceptCode(scheme, version, matchText);
                     }
 
                 }  catch (Exception e) {
                     _logger.error("Method: SearchUtils.matchConceptCode");
                     _logger.error("* ERROR: cns.resolve throws exceptions.");
                     _logger.error("* " + e.getClass().getSimpleName() + ": " +
                         e.getMessage());
                 }
 
             } catch (Exception ex) {
                 ex.printStackTrace();
                 return null;
             }
 
         } catch (Exception ex) {
             //_logger.error("WARNING: searchByCode throws exception.");
             ex.printStackTrace();
         }
 
 
         //return iterator;
         if (source == null || source.compareToIgnoreCase("ALL") == 0) {
             return filterIterator(iterator, scheme, version, matchText);
         }
         return iterator;
     }
 
 
     public static ResolvedConceptReferencesIterator matchConceptCode(String scheme, String version, String code) {
 
 _logger.debug("matchConceptCode scheme " + scheme);
 _logger.debug("matchConceptCode code " + code);
 
 
         LexBIGService lbSvc = RemoteServerUtilTest.createLexBIGService();
         Vector v = new Vector();
         CodingSchemeVersionOrTag versionOrTag = new CodingSchemeVersionOrTag();
         if (version != null) versionOrTag.setVersion(version);
         CodedNodeSet cns = null;
         ResolvedConceptReferencesIterator iterator = null;
         try {
             cns = lbSvc.getNodeSet(scheme, versionOrTag, null);
             CodedNodeSet.PropertyType[] propertyTypes = null;
             //LocalNameList sourceList = null;
             //LocalNameList contextList = null;
             //NameAndValueList qualifierList = null;
             ConceptReferenceList crefs = createConceptReferenceList(
                     new String[] { code }, scheme);
 
             try {
                 cns = lbSvc.getCodingSchemeConcepts(scheme,
                         versionOrTag);
             } catch (Exception e) {
                 e.printStackTrace();
                 return null;
             }
 
             if (cns == null) {
                 _logger.warn("getConceptByCode getCodingSchemeConcepts returns null??? " + scheme);
                 return null;
             }
 
             cns = cns.restrictToCodes(crefs);
             LocalNameList restrictToProperties = new LocalNameList();
             SortOptionList sortCriteria = null;
             try {
                 boolean resolveConcepts = true;
                 try {
                     long ms = System.currentTimeMillis(), delay = 0;
                     iterator = cns.resolve(sortCriteria, null, restrictToProperties, null, resolveConcepts);
 
                     int size = iterator.numberRemaining();
                 }  catch (Exception e) {
                     _logger.error("Method: SearchUtil.matchConceptCode");
                     _logger.error("* ERROR: cns.resolve throws exceptions.");
                     _logger.error("* " + e.getClass().getSimpleName() + ": " +
                         e.getMessage());
                 }
 
             } catch (Exception ex) {
                 ex.printStackTrace();
                 return null;
             }
 
         } catch (Exception ex) {
             _logger.error("WARNING: searchByCode throws exception.");
         }
         //[#26386] Need app to be able to distinguish/prioritize/display matching code vrs. matching name results
 
         return filterIterator(iterator, scheme, version, code);
         //return iterator;
     }
 
 
     public static ResolvedConceptReferencesIterator filterIterator(ResolvedConceptReferencesIterator iterator, String scheme, String version, String code) {
         if (iterator == null) return null;
         try {
             int num = iterator.numberRemaining();
             if (num <= 1) {
                 return iterator;
             }
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         try {
             while(iterator.hasNext()){
                 try {
                     ResolvedConceptReference[] refs = iterator.next(100).getResolvedConceptReference();
                     for(ResolvedConceptReference ref : refs){
                         if (ref.getConceptCode().compareTo(code) == 0) {
                             long ms0 = System.currentTimeMillis(), delay0 = 0;
                             LexBIGService lbSvc = new RemoteServerUtilTest().createLexBIGService();
                             if (lbSvc == null)
                             {
                                 _logger.warn("lbSvc = null");
                                 return null;
                             }
                             CodingSchemeVersionOrTag versionOrTag = new CodingSchemeVersionOrTag();
                             if (version != null) versionOrTag.setVersion(version);
 
                             ConceptReferenceList crefs = createConceptReferenceList(
                                     new String[] { code }, scheme);
 
                             CodedNodeSet cns = null;
                             try {
                                 try {
                                     cns = lbSvc.getNodeSet(scheme, versionOrTag, null);
                                 } catch (Exception e) {
                                     e.printStackTrace();
                                     return null;
                                 }
 
                                 if (cns == null) {
                                     _logger.warn("getConceptByCode getCodingSchemeConcepts returns null??? " + scheme);
                                     return null;
                                 }
 
                                 cns = cns.restrictToCodes(crefs);
                                 return cns.resolve(null, null, null);
                             } catch (Exception ex) {
 
                             }
                         }
                     }
                 } catch (Exception ex) {
                     break;
                 }
             }
         } catch (Exception ex) {
 
         }
         return null;
     }
 
 
 
    public static Concept matchConceptByCode(String scheme, String version, String matchText, String source, String matchAlgorithm) {
 
 _logger.debug("searchUtils matchConceptByCode scheme: " + scheme);
 _logger.debug("searchUtils matchConceptByCode version: " + version);
 _logger.debug("searchUtils matchConceptByCode matchText: " + matchText);
 _logger.debug("searchUtils matchConceptByCode source: " + source);
 _logger.debug("searchUtils matchConceptByCode matchAlgorithm: " + matchAlgorithm);
 
 
         ResolvedConceptReferencesIterator iterator = matchConceptCode(scheme, version, matchText, source, matchAlgorithm);
         if (iterator == null) {
             _logger.warn("searchUtils matchConceptCode returns null??? " + matchText);
             return null;
         }
         try {
             while(iterator.hasNext()){
                 ResolvedConceptReference[] refs = iterator.next(100).getResolvedConceptReference();
                 for(ResolvedConceptReference ref : refs){
 
                     if (ref == null) {
                         _logger.warn("(***) matchConceptByCode ref == null??");
                     }
 
                     else if (ref.getReferencedEntry() == null) {
                         _logger.warn("(***) matchConceptByCode ref.getReferencedEntry() == null??");
                     }
 
                     else if (ref.getReferencedEntry().getEntityCode() == null) {
                         _logger.warn("(***) matchConceptByCode ref.getReferencedEntry().getEntityCode() == null??");
                     }
 
                     else {
                         if (ref.getReferencedEntry().getEntityCode().equals(matchText)) {
                             _logger.debug("(RESULT) matchConceptByCode ref.getReferencedEntry().getEntityCode() == " + ref.getReferencedEntry().getEntityCode());
                             return ref.getReferencedEntry();
                         }
                     }
 
                 }
             }
         }  catch (Exception e) {
             _logger.error("Method: SearchUtila.matchConceptByCode");
             _logger.error("* ERROR: cns.resolve throws exceptions.");
             _logger.error("* " + e.getClass().getSimpleName() + ": " +
                 e.getMessage());
         }
         return null;
     }
 
 
     public static void main(String[] args) throws Exception {
         MatchConceptByCode test = new MatchConceptByCode();
 
         String scheme = "Gene Ontology";
         String code = "GO:0000018";
         String version = null;
 
         _logger.debug("==================matchConceptByCode=========================" + code);
 
        Concept c = test.matchConceptByCode(scheme, version, code, null, "LuceneQuery");
 
         if (c == null) {
             _logger.debug("Concept is null???");
         } else {
             _logger.debug("Concept is NOT null -- " + c.getEntityCode());
         }
 
     }
 
 
 }
 
