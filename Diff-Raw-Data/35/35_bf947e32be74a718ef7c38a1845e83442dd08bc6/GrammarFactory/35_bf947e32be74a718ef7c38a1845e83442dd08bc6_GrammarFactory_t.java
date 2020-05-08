 /*
  * ModelCC, under ModelCC Shared Software License, www.modelcc.org. Luis Quesada Torres.
  */
 
 
 package org.modelcc.language.syntax;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * Grammar factory.
  * @author elezeta
  * @serial
  */
 public final class GrammarFactory implements Serializable {
 
     /**
      * Serial Version ID
      */
     private static final long serialVersionUID = 31415926535897932L;
 
     /**
      * Set of grammar rules.
      */
     private Set<Rule> rules;
 
     /**
      * Start type.
      */
     private Object startType;
 
     /**
      * The parser data factory.
      */
     private ParserDataFactory dataFactory;
     
     /**
      * The token symbol builder.
      */
     private SymbolBuilder tsb;
     
     /**
      * Default constructor.
      */
     public GrammarFactory() {
         rules = new HashSet<Rule>();
         startType = null;
         dataFactory = null;
         tsb = null;
     }
 
     /**
      * Adds a rule.
      * @param r the rule to add.
      */
     public void addRule(Rule r) {
         if (r != null)
          rules.add(r);
     }
 
     /**
      * Removes a rule.
      * @param r the rule to remove.
      */
     public void removeRule(Rule r) {
         rules.remove(r);
     }
 
     /**
      * @return the rule set.
      */
     public Set<Rule> getRules() {
         return Collections.unmodifiableSet(rules);
     }
 
     /**
      * @return the start type
      */
     public Object getStartType() {
         return startType;
     }
 
     /**
      * @param startType the start type to set
      */
     public void setStartType(Object startType) {
         this.startType = startType;
     }
 
     /**
      * Generates a grammar.
      * @throws NullRuleElementException whenever a rule element is null
      * @return the grammar.
      */
     public Grammar create() throws NullRuleElementException {
         Set<Object> lefts = new HashSet<Object>();
         Set<Object> elements = new HashSet<Object>();
         Map<Object,Set<Object>> emptyRules = new HashMap<Object,Set<Object>>();
         Map<Object,Rule> emptyRuleRules = new HashMap<Object,Rule>();
         // -------------
         // Check rule integrity.
         // -------------
         {
 
             // Auxiliar variables.
             Iterator<Rule> ite;
             Iterator<RuleElement> ite2;
             Rule r;
             RuleElement elem;
 
             for (ite = rules.iterator();ite.hasNext();) {
                 r = ite.next();
                 if (r.getLeft() == null)
                     throw new NullRuleElementException();
                 else {
                     lefts.add(r.getLeft().getType());
                     elements.add(r.getLeft().getType());
                 }
                 if (r.getRight() == null)
                     throw new NullRuleElementException();
 
                 for (ite2 = r.getRight().iterator();ite2.hasNext();) {
                     elem = ite2.next();
                     if (elem == null)
                         throw new NullRuleElementException();
                     else
                         elements.add(elem.getType());
                 }
             }
 
         }
 
 
         // -------------
         // Check empty rules.
         // -------------
         {
 
             // Auxiliar variables.
             Iterator<Rule> iter;
             Iterator<RuleElement> itee;
             RuleElement e;
             Rule r;
 
             for (iter = rules.iterator();iter.hasNext();) {
                 r = iter.next();
                 if (r.getRight().isEmpty()) {
                     emptyRules.put(r.getLeft().getType(),null);
                     emptyRuleRules.put(r.getLeft().getType(),r);
                    //System.out.println("GrammarFactory.java empty rule"+r);
                     iter.remove();
                 }
             }
 
             boolean found;
             Set<Rule> rulesCopy = new HashSet<Rule>();
             for (iter = rules.iterator();iter.hasNext();) {
                 r = iter.next();
                 List<RuleElement> eles = new ArrayList<RuleElement>();
                 eles.addAll(r.getRight());
                 rulesCopy.add(new Rule(r.getLeft(),eles,r.getUserData(),r.getBuilder(),r.getPostBuilder()));
             }
             do {
                 found = false;
                 for (iter = rulesCopy.iterator();iter.hasNext();) {
                     r = iter.next();
                    if (!emptyRules.containsKey(r.getLeft().getType())) {
                        for (itee = r.getRight().iterator();itee.hasNext();) {
                            e = itee.next();
                            if (emptyRules.containsKey(e.getType())) {
                                itee.remove();
                                if (r.getRight().isEmpty()) {
                                    found = true;
                                    emptyRules.put(r.getLeft().getType(),null);
                                    emptyRuleRules.put(r.getLeft().getType(),r);
                                }
                             }
                         }
                     }
                 }
             } while (found);
 
         }
         return new Grammar(emptyRules,rules,startType,emptyRuleRules,dataFactory,tsb);
 
     }
 
     /**
      * @return the dataFactory
      */
     public ParserDataFactory getDataFactory() {
         return dataFactory;
     }
 
     /**
      * @param dataFactory the dataFactory to set
      */
     public void setDataFactory(ParserDataFactory dataFactory) {
         this.dataFactory = dataFactory;
     }
 
 
     /**
      * @param sb the token symbol builder.
      */
     public void setTokenSymbolBuilder(SymbolBuilder sb) {
         this.tsb = sb;
     }
 
 }
