 package jp.long_long_float.cuick.compiler;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 
 import jp.long_long_float.cuick.ast.AtTestCase;
 import jp.long_long_float.cuick.ast.AtTestNode;
 import jp.long_long_float.cuick.ast.BuiltInCodeStmt;
 import jp.long_long_float.cuick.type.Type;
 import jp.long_long_float.cuick.utility.Pair;
 import lombok.Getter;
 import lombok.Setter;
 
 //https://sites.google.com/site/leihcrev/java/validsingleton
 
 public final class Table {
     private static final class TableHolder {
         private static final Table instance = new Table();
     }
     
     public static Table getInstance() {
         return Table.TableHolder.instance;
     }
     
     private Table() {
         
     }
     
     private List<Type> tuples = new ArrayList<Type>();
     private List<BuiltInCodeStmt> builtInCodes = new ArrayList<BuiltInCodeStmt>();
     @Getter @Setter private AtTestNode atTestNode;
     @Getter @Setter private boolean debugMode = false;
     
     public void entryTuple(Type tuple) {
         //この時点では完全な型ではないので(templateがない)
         //if(tuples.indexOf(tuple) == -1) {
         tuples.add(tuple);
         //}
     }
     
     public void entryBuiltInCodeStmt(BuiltInCodeStmt code) {
         if(!builtInCodes.contains(code)) {
             builtInCodes.add(code);
         }
     }
     
     /**
      * 一意なtupleのリストを返す
      */
     public List<Type> getTuples() {
         List<Type> ret = new ArrayList<Type>();
         for(Type tuple : tuples) {
             if(!ret.contains(tuple)) {
                 ret.add(tuple);
             }
         }
         return ret;
     }
     
     public String getRealTupleName(Type tuple) {
         return "tuple" + getTuples().indexOf(tuple);
     }
 
     public List<BuiltInCodeStmt> getBuiltInCodeStmt() {
         return builtInCodes;
     }
     
     public boolean equalsInCasesAndOutCases() {
         return atTestNode == null || (atTestNode != null && atTestNode.getInCases().size() == atTestNode.getOutCases().size());
     }
 
     public boolean isEnabledTest() {
         return atTestNode != null;
     }
     
     public List<Pair<AtTestCase, AtTestCase>> getTestCases() {
         if(!equalsInCasesAndOutCases()) return null;
         
         List<Pair<AtTestCase, AtTestCase>> ret = new ArrayList<Pair<AtTestCase,AtTestCase>>(atTestNode.getInCases().size());
         Iterator<AtTestCase> inItr = atTestNode.getInCases().iterator(), outItr = atTestNode.getOutCases().iterator();
         while(inItr.hasNext()) {
             ret.add(new Pair<AtTestCase, AtTestCase>(inItr.next(), outItr.next()));
         }
         return ret;
     }
 
     public boolean isDefinedWord(String name) {
         return Arrays.asList(
                "true",
                "false",
                "puts",
                "print"
                 ).contains(name);
     }
 }
