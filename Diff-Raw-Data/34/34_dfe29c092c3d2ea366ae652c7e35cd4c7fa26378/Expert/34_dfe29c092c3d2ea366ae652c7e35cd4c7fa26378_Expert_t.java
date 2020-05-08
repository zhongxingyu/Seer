 /*
  * Genealogy expert system.
  * Semestral work for the University of West Bohemia.
  * 
  * Written by Zdeněk Janeček, 2013
  * Share it freely under conditions of GNU GPL v3
  */
 
 package kiv.janecekz;
 
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 
 public class Expert {
     public static final int POS_ID = 0;
     public static final int POS_DESC = 1;
     public static final int POS_MALENAME = 2;
     public static final int POS_FEMALENAME = 3;
     public static final int POS_RULE = 4;
 
     private static final String EXP_DATA = "expertData";
     private static final String P = "P";
     private static final String C = "C";
 
     private NTree tree;
     private Relation[] relations;
 
     private class Relation {
         public final int id;
         public final String desc;
         public final String maleName;
         public final String femaleName;
 
         public String rule;
 
         public Relation(int id, String desc, String maleName, String femaleName) {
             this.id = id;
             this.desc = desc;
             this.maleName = maleName;
             this.femaleName = femaleName;
         }
     }
 
     public Expert(NTree tree, BufferedReader br) {
         this.tree = tree;
 
         try {
             processExpInput(EXP_DATA);
         } catch (FileNotFoundException e) {
             Starter.dieWithError("(EE) Soubor nenalezen");
         } catch (IOException e) {
             Starter.dieWithError("(EE) Chyba vstupu výstupu");
         }
     }
 
     /**
      * Function for Predicate logic expression. Method fills {@code b} with
      * childrens of all elements of {@code a}.
      * 
      * @param a
      *            parent nodes
      * @param b
      *            child nodes
      */
     public Collection<Node> parentOf(Collection<Node> a) {
         Collection<Node> b = new HashSet<Node>();
 
         for (Node node : a) {
             b.add(node.getFather());
             b.add(node.getMother());
         }
 
         return b;
     }
 
     /**
      * Function for Predicate logic expression. Method fills {@code b} with
      * parents of all elements of {@code a}.
      * 
      * @param a
      *            children nodes
      * @param b
      *            parent nodes
      */
     public Collection<Node> childrenOf(Collection<Node> a) {
         Collection<Node> b = new HashSet<Node>();
 
         for (Node node : a) {
             b.addAll(node.listOfChilds());
         }
 
         return b;
     }
 
     /**
      * Function for Predicate logic expression. Method fills b with all objects
      * that are in {@code a} but without objects in {@code b}.
      * 
      * @param a
      *            source nodes
      * @param b
      *            nodes to exclude from a
      */
     public Collection<Node> notThis(Collection<Node> a, Collection<Node> b) {
         Collection<Node> newSet = new HashSet<Node>();
         for (Node node : a) {
             if (!(b.contains(node)))
                 newSet.add(node);
         }
 
         return newSet;
     }
 
     public Collection<Node> listChilds(Collection<Node> persons,
             Collection<Node> result) {
         result.clear();
 
         if (result instanceof HashSet) {
             for (Node p : persons) {
                 result.addAll(p.listOfChilds());
             }
         } else {
             for (Node p : persons) {
                 for (Node c : p.listOfChilds()) {
                     if (!result.contains(c))
                         result.add(c);
                 }
 
             }
         }
 
         return result;
     }
 
     public Collection<Node> listParents(Collection<Node> persons,
             Collection<Node> result) {
         result.clear();
 
         if (result instanceof HashSet) {
             for (Node p : persons) {
                 result.add(p.getFather());
                 result.add(p.getMother());
             }
         } else {
             for (Node p : persons) {
                 if (!result.contains(p.getFather()))
                     result.add(p.getFather());
                 if (!result.contains(p.getMother()))
                     result.add(p.getMother());
             }
         }
 
         return result;
     }
 
     /**
      * Compares complex rule with general path. Like PxaPabCbcNca with PPC gives
      * true.
      * 
      * @param rule1
      * @param rule2
      * @return
      */
     private boolean compareRel(String rule1, String rule2) {
         StringBuilder rule1strip = new StringBuilder();
         // clean rule1
         for (int i = 0; i < rule1.length(); i++) {
             if (rule1.charAt(i) == 'P' || rule1.charAt(i) == 'C') {
                 rule1strip.append(rule1.charAt(i));
                 i += 2;
             } else {
                i += 2;
             }
         }
 
         return rule1strip.toString().equals(rule2);
     }
 
     public int getRealationId(String rule) {
         for (int i = 0; i < relations.length; i++) {
             if (compareRel(relations[i].rule, rule)) {
                 return i;
             }
         }
 
         return -1;
     }
 
     public String[] getRelationInfo(int id) {
         String[] result = new String[5];
         result[POS_ID] = Integer.toString(id);
         result[POS_DESC] = relations[id].desc;
         result[POS_MALENAME] = relations[id].maleName;
         result[POS_FEMALENAME] = relations[id].femaleName;
         result[POS_RULE] = relations[id].rule;
 
         return result;
     }
 
     public String[][] getRelations() {
         String[][] result = new String[relations.length][5];
         for (int i = 0; i < relations.length; i++) {
             result[i] = getRelationInfo(i);
         }
 
         return result;
     }
 
     /**
      * Searches for some person and returns their relation.
      * 
      * @param from
      * @param target
      * @return
      */
     public String findPerson(Node from, Node target) {
         if (from == null || target == null) {
             return "(II) Nemůžu pokračovat";
         }
 
         String result = "Nenalezen";
         LinkedList<Node> q = new LinkedList<Node>();
         boolean[] visited = new boolean[tree.size()];
 
         String[] paths = new String[tree.size()];
         for (int i = 0; i < paths.length; i++) {
             paths[i] = "";
         }
 
         visited[from.getId()] = true;
         q.add(from);
 
         while (!q.isEmpty()) {
             Node v = q.pollFirst();
 
             if (v.equals(target)) {
                 int id = getRealationId(paths[v.getId()]);
                 if (id == -1) {
                     result = "Nenalezen vztah";
                 } else {
                     if (v.isMale())
                         result = "Nalezen " + v.getName() + " ve vztahu "
                                 + relations[id].maleName;
                     else
                         result = "Nalezena " + v.getName() + " ve vztahu "
                                 + relations[id].femaleName;
                 }
             }
 
             for (Node w : v.listOfChilds()) {
                 if (!visited[w.getId()]) {
                     visited[w.getId()] = true;
                     paths[w.getId()] = paths[v.getId()] + C;
                     q.add(w);
                 }
             }
 
             Node[] parents = { v.getFather(), v.getMother() };
 
             for (Node w : parents) {
                 if (w == null)
                     continue;
 
                 if (!visited[w.getId()]) {
                     visited[w.getId()] = true;
                     paths[w.getId()] = paths[v.getId()] + P;
                     q.add(w);
                 }
             }
         }
 
         return result;
     }
 
     /**
      * Finds all persons in specified relation.
      * 
      * @param from
      *            Person to search from.
      * @param rel
      *            wanted relation
      * @param male
      *            is male or female
      * @return LinkedList of person instances
      */
     public Collection<Node> findPersons(Node from, int rel, boolean male) {
         if (from == null)
             return null;
 
         String rule = relations[rel].rule;
         LinkedList<Node> q = new LinkedList<Node>();
         boolean[] visited = new boolean[tree.size()];
         Collection<Node> result = new LinkedList<Node>();
 
         String[] paths = new String[tree.size()];
         for (int i = 0; i < paths.length; i++) {
             paths[i] = "";
         }
 
         visited[from.getId()] = true;
         q.add(from);
 
         while (!q.isEmpty()) {
             Node v = q.pollFirst();
 
             if (v.isMale() == male && compareRel(rule, paths[v.getId()])) {
                 result.add(v);
             }
 
             for (Node w : v.listOfChilds()) {
                 if (!visited[w.getId()]) {
                     visited[w.getId()] = true;
                     paths[w.getId()] = paths[v.getId()] + C;
                     q.add(w);
                 }
             }
 
             Node[] parents = { v.getFather(), v.getMother() };
 
             for (Node w : parents) {
                 if (w == null)
                     continue;
 
                 if (!visited[w.getId()]) {
                     visited[w.getId()] = true;
                     paths[w.getId()] = paths[v.getId()] + P;
                     q.add(w);
                 }
             }
         }
 
         return result;
     }
 
     public boolean testRelation(Node from, Node target, int rel, boolean male) {
         boolean result = false;
         HashMap<Character, Collection<Node>> terms;
         terms = new HashMap<Character, Collection<Node>>();
 
         HashSet<Node> first = new HashSet<Node>();
         HashSet<Node> second = null;
        Collection<Node> test = null;
 
         Collection<Node> initial = new HashSet<Node>();
         initial.add(from);
         terms.put('x', initial);
 
         String val = relations[rel].rule;
 
         for (int symIt = 0; symIt < val.length(); symIt++) {
             switch (val.charAt(symIt)) {
             case 'P':
                 first = (HashSet<Node>) terms.get(new Character(val
                         .charAt(++symIt)));
                // TODO: check if doesn't have any parents. NULL pointer
                test = parentOf(first);
                if (test.contains(tree.getId(0)) || test == null)
                    return false;
                else
                    second = (HashSet<Node>) test;
                 terms.put(val.charAt(++symIt), new HashSet<Node>(second));
                 break;
             case 'C':
                 first = (HashSet<Node>) terms.get(new Character(val
                         .charAt(++symIt)));
                test = childrenOf(first);
                if (test == null)
                    return false;
                else
                    second = (HashSet<Node>) test;
                 terms.put(val.charAt(++symIt), new HashSet<Node>(second));
                 break;
             case 'N':
                 first = (HashSet<Node>) terms.get(new Character(val
                         .charAt(++symIt)));
                 second = (HashSet<Node>) terms.get(new Character(val
                         .charAt(++symIt)));
                 second = (HashSet<Node>) notThis(first, second);
                 break;
             default:
                 break;
             }
         }
 
        if (male == target.isMale() && second.contains(target))
             result = true;
         else
             result = false;
 
         return result;
     }
 
     public Collection<Node> bloodline(Node from, int deep) {
         Collection<Node> res = new HashSet<Node>();
         LinkedList<Node> q = new LinkedList<Node>();
 
         q.add(from);
 
         int d = 0;
         int timeToDepthIncrease = 1;
         boolean pendingDepthIncrease = false;
         while (!q.isEmpty()) {
             if (d > deep)
                 break;
 
             Node v = q.pollFirst();
             res.add(v);
             timeToDepthIncrease--;
             
             if (timeToDepthIncrease == 0) {
                 d++;
                 pendingDepthIncrease = true;
             }
 
             Node[] parents = { v.getFather(), v.getMother() };
 
             for (Node w : parents) {
                 if (w == null || w.equals(tree.getId(0)))
                     continue;
 
                 q.add(w);
             }
             
             if (pendingDepthIncrease) {
                 timeToDepthIncrease = q.size();
                 pendingDepthIncrease = false;
             }
         }
 
         res.remove(from);
         
         return res;
     }
 
     private void processExpInput(String file) throws FileNotFoundException,
             IOException {
         BufferedReader input = null;
         input = new BufferedReader(new FileReader(file));
 
         String curLine;
 
         // We add one virtual nod to save end of line.
         int count = Integer.parseInt(input.readLine().trim());
 
         // first line shows count of input data
         relations = new Relation[count];
 
         for (int i = 0; i < count; i++) {
             curLine = input.readLine();
             String[] values = Starter.getTokens(curLine);
             int id = Integer.parseInt(values[POS_ID]);
 
             Relation onerel = new Relation(id, values[POS_DESC],
                     values[POS_MALENAME], values[POS_FEMALENAME]);
 
             curLine = input.readLine();
             onerel.rule = curLine.trim();
 
             relations[i] = onerel;
         }
 
         input.close();
     }
 }
