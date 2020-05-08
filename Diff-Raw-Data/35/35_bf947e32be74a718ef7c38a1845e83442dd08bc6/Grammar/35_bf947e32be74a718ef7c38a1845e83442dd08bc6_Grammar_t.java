 /*
  * ModelCC, under ModelCC Shared Software License, www.modelcc.org. Luis Quesada Torres.
  */
 
 
 package org.modelcc.language.syntax;
 
 import java.io.Serializable;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import org.modelcc.parser.fence.Symbol;
 
 /**
  * Grammar.
  * @author elezeta
  * @serial
  */
 public final class Grammar implements Serializable {
 
     /**
      * Serial Version ID
      */
     private static final long serialVersionUID = 31415926535897932L;
 
     /**
      * Set of empty rules.
      */
     private Map<Object,Set<Object>> emptyRules;
 
     /**
      * Map of empty rule rules.
      */
     private Map<Object,Rule> emptyRuleRules;
 
     /**
      * Set of rules.
      */
     private Set<Rule> rules;
 
     /**
      * Start type.
      */
     private Object startType;
 
     /**
      * Start rules.
      */
     private Map<Object,Set<Rule>> startRules;
 
     /**
      * Firststar
      */
     private Map<Object,Set<Object>> firstStar;
 
     /**
      * The parser data factory.
      */
     private ParserDataFactory dataFactory;
     
     /**
      * The token symbol builder.
      */
     private SymbolBuilder tsb;
     
     /**
      * The default token symbol builder.
      */
     private static SymbolBuilder defaultTsb = new SymbolBuilder() {
 
         /**
          * Serial Version ID
          */
         private static final long serialVersionUID = 31415926535897932L;
 
         @Override
         public boolean build(Symbol t, Object data) {
             return true;
         }
     };
     
     /**
      * The default token symbol builder.
      */
     private static ParserDataFactory defaultDataFactory = new ParserDataFactory() {
 
         /**
          * Serial Version ID
          */
         private static final long serialVersionUID = 31415926535897932L;
 
         @Override
         public Object generate() {
             return null;
         }
 
     };
     
     /**
      * Constructor.
      * @param emptyRules the set of empty rules.
      * @param rules the rules list.
      * @param startType the start type.
      * @param emptyRuleRules the empty rule rules map.
      * @param dataFactory the parser data factory. 
      * @param tsb the token symbol builder.
      */
     public Grammar(Map<Object,Set<Object>> emptyRules,Set<Rule> rules,Object startType,Map<Object,Rule> emptyRuleRules,ParserDataFactory dataFactory,SymbolBuilder tsb) {
         this(emptyRules,rules,startType,emptyRuleRules);
         this.dataFactory = dataFactory;
         this.tsb = tsb;
         if (this.dataFactory == null)
             this.dataFactory = defaultDataFactory;
         if (tsb == null)
             this.tsb = defaultTsb;
     }
 
     /**
      * Constructor.
      * @param emptyRules the set of empty rules.
      * @param rules the rules list.
      * @param startType the start type.
      * @param emptyRuleRules the empty rule rules map.
      */
     public Grammar(Map<Object,Set<Object>> emptyRules,Set<Rule> rules,Object startType,Map<Object,Rule> emptyRuleRules) {
         try {
             this.emptyRules = emptyRules;
             this.rules = rules;
             this.startType = startType;
             this.emptyRuleRules = emptyRuleRules;
             this.startRules = new HashMap<Object,Set<Rule>>();
             Rule r;
             for (Iterator<Rule> ite = rules.iterator();ite.hasNext();) {
                 r = ite.next();
                 int i = 0;
                 do {
 	                Set<Rule> se = startRules.get(r.getRight().get(i).getType());
 	                if (se == null) {
 	                    se = new HashSet<Rule>();
 	                    startRules.put(r.getRight().get(i).getType(),se);
 	                }
 	                se.add(r);
 	                i++;
                 } while (emptyRules.containsKey(r.getRight().get(i-1).getType()) && i < r.getRight().size());
             }
 
             // Calculate firstStar:
             Set<Object> objs = new HashSet<Object>();
             firstStar = new HashMap<Object,Set<Object>>();
             for (Iterator<Rule> ite = rules.iterator();ite.hasNext();) {
                 r = ite.next();
                 Set<Object> se = firstStar.get(r.getLeft().getType());
                 if (se == null) {
                     se = new HashSet<Object>();
                     firstStar.put(r.getLeft().getType(),se);
                 }
                 se.add(r.getLeft().getType());
                 firstStarFill(se,r,0,emptyRules,objs);
                 for (int i = 0;i < r.getRight().size();i++) {
                     se = firstStar.get(r.getRight().get(i).getType());
                     if (se == null) {
                         se = new HashSet<Object>();
                         firstStar.put(r.getRight().get(i).getType(),se);
                     }
                     se.add(r.getRight().get(i).getType());
                 }
             }
 
             // While updateed
             boolean updated = true;
             while (updated) {
                 updated = false;
                 for (Iterator<Object> ite = objs.iterator();ite.hasNext();) {
                     Object o = ite.next();
                     for (Iterator<Object> ite2 = objs.iterator();ite2.hasNext();) {
                         Object o2 = ite2.next();
                         if (firstStar.get(o) != null && firstStar.get(o2) != null) {
                             if (firstStar.get(o).contains(o2)) {
                                 for (Iterator<Object> ite3 = firstStar.get(o2).iterator();ite3.hasNext();) {
                                     Object o3 = ite3.next();
                                     if (!firstStar.get(o).contains(o3)) {
                                         firstStar.get(o).add(o3);
                                         updated = true;
                                     }
                                 }
                             }
                         }
                     }
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
             System.exit(0);
         }
     }
     
     /**
      * Fills the first* set of a rule
      * @param se the set
      * @param r the rule.
      * @param i
      * @param emptyRules 
      */
    private void firstStarFill(Set<Object> se, Rule r, int i, Map<Object,Set<Object>> emptyRules,Set<Object> objs) {
         se.add(r.getRight().get(i).getType());
         objs.add(r.getLeft().getType());
         objs.add(r.getRight().get(i).getType());
         if (emptyRules.containsKey(r.getRight().get(i).getType()) && r.getRight().size()>i+1) 
             firstStarFill(se,r,i+1,emptyRules,objs);
     }
 
     /**
      * @return the set of rules.
      */
     public Set<Rule> getRules() {
         return Collections.unmodifiableSet(rules);
     }
 
     /**
      * @return the startType
      */
     public Object getStartType() {
         return startType;
     }
 
     /**
      * @return the set of empty rules
      */
     public Map<Object,Set<Object>> getEmptyRules() {
         return Collections.unmodifiableMap(emptyRules);
     }
 
     /**
      * @return the empty rule rules.
      */
     public Map<Object,Rule> getEmptyRuleRules() {
         return Collections.unmodifiableMap(emptyRuleRules);
     }
 
     /**
      * @return the startRules
      */
     public Map<Object,Set<Rule>> getStartRules() {
         return Collections.unmodifiableMap(startRules);
     }
 
     /**
      * @return the firstStar
      */
     public Map<Object,Set<Object>> getFirstStar() {
         return Collections.unmodifiableMap(firstStar);
     }
     
     /**
      * Converts to string.
      * @return the resulting string.
      */
     @Override
     public String toString() {
         String ret = "";
         for (Iterator<Rule> ite = rules.iterator();ite.hasNext();)
             ret += ite.next()+"\n";
         ret += "\n";
         for (Iterator<Object> ite = emptyRules.keySet().iterator();ite.hasNext();)
             ret += "empty: "+ite.next()+"\n";
         return ret;
         
     }
 
     /**
      * @return the dataFactory
      */
     public ParserDataFactory getDataFactory() {
         return dataFactory;
     }
 
     /**
      * @return the token symbol builder.
      */
     public SymbolBuilder getTsb() {
         return tsb;
     }
 }
