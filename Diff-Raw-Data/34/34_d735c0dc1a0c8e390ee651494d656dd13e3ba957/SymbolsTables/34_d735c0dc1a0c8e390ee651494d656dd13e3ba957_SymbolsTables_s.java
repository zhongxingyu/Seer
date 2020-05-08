 package smartest;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.ListIterator;
 import java.util.UUID;
 
 /**
  * Implementation of symbols tables. This class maintains the context of symbols
  * tables whenever a new scope is entered, a new symbol table is appended
  * whenever a scope is exited, the associated symbol table is removed.
  * 
  * @author Aiman
  */
 public class SymbolsTables {
     static LinkedList<SymbolTable> tablesList = new LinkedList<SymbolTable>();
     /**
      * Scopes Index: a table that maps scope ids to its respective hashmap
      * object Whenever a new scope is entered, it's assigned a unique ID which
      * is later used to lookup variables during code generation.
      */
     static HashMap<String, SymbolTable> scopesIndex = new HashMap<String, SymbolTable>();
 
     public static void initialize() {
         /* Nothing to do */
     }
 
     /**
      * Insert a new symbol into the current scope's symbol table.
      * 
      * @param type
      * @param sourceName
      * @param targetName
      * @param lineNumber
      */
     public static String insertSymbol(String type, String sourceName,
             String targetName, String lineNumber) {
         if (Parser.DEBUG)
             System.out.println("in insertSymbol. Inserting " + sourceName);
         String[] data = { type, targetName, lineNumber,
                 tablesList.getLast().getScopeId() };
         tablesList.getLast().put(sourceName.toLowerCase(), data);
         return tablesList.getLast().getScopeId();
     }
 
     /**
      * Looks up a symbol in a specific scope.
      * 
      * @param sourceName
     * @return
      */
     public static String[] lookupInScope(String sourceName, String scopeId) {
         if (Parser.DEBUG)
             System.out.println("in lookupInScope. Looking up "
                     + sourceName.toLowerCase() + " in scope " + scopeId);
         return scopesIndex.get(scopeId).get(sourceName.toLowerCase());
     }
 
     /**
      * Returns whether a variable is defined within a specific scope.
      * 
      * @param sourceName
     * @return
      */
     public static boolean scopeContains(String sourceName, String scopeId) {
         if (Parser.DEBUG)
             System.out.println("in scopeContains. Looking up "
                     + sourceName.toLowerCase() + " in scope " + scopeId);
         return scopesIndex.get(scopeId).containsKey(sourceName.toLowerCase());
     }
 
     /**
      * Looks up a symbol in current table, if not found, steps out to outer
      * scope,...etc.
      * 
      * @param sourceName
     * @return
      */
     public static String[] lookupSymbol(String sourceName) {
         if (Parser.DEBUG)
             System.out.println("in lookupSymbol. Looking up "
                     + sourceName.toLowerCase() + " in (" + tablesList.size()
                     + ") symbols tables");
         String[] data = null;
         ListIterator<SymbolTable> itr = tablesList.listIterator(tablesList
                 .size());
         while (itr.hasPrevious()) {
             HashMap<String, String[]> table = itr.previous();
             if (table.containsKey(sourceName.toLowerCase())) {
                 data = table.get(sourceName.toLowerCase());
                 break;
             }
         }
         return data;
     }
 
     /**
      * Returns whether a symbol exists in current context.
      * 
     * @param lowerCase
     * @return
      */
     public static boolean containsSymbol(String name) {
         if (Parser.DEBUG)
             System.out.println("in containsSymbols. Looking up "
                     + name.toLowerCase() + " in (" + tablesList.size()
                     + ") symbols tables");
         ListIterator<SymbolTable> itr = tablesList.listIterator(tablesList
                 .size());
         while (itr.hasPrevious()) {
             HashMap<String, String[]> table = itr.previous();
             if (Parser.DEBUG)
                 System.out.print("Table contains " + table.size()
                         + " symbols: ");
             if (table.containsKey(name.toLowerCase())) {
                 if (Parser.DEBUG)
                     System.out.println(" found symbol.");
                 return true;
             }
             if (Parser.DEBUG)
                 System.out.println("symbol not found in this table");
         }
         return false;
     }
 
     /**
      * Creates new symbol table for new scope, and returns its unique ID
      */
     public static String enterNewScope() {
         SymbolTable newTable = new SymbolTable();
         String scopeId = UUID.randomUUID().toString();
         tablesList.add(newTable);
         newTable.setScopeId(scopeId);
         scopesIndex.put(scopeId, newTable);
         if (Parser.DEBUG)
             System.out.println("Entered new scope; id: " + scopeId);
         return scopeId;
     }
 
     /**
      * Removes current scope
      */
     public static void leaveCurrentScope() {
         SymbolTable table = tablesList.removeLast();
         if (Parser.DEBUG)
             System.out.println("Left scope: " + table.getScopeId());
     }
 }
 
 /**
  * symbolsTable representation: HashMaps where keys are the identifier names as
  * found in the source code Values are three-element arrays where the first
  * element represents the type of the identifier and the second element
  * represents a generated variable name to be used in the target code and the
  * third element is the line which this symbol was declared first at For more
  * details see DeclarationNode
  */
 class SymbolTable extends HashMap<String, String[]> {
     private static final long serialVersionUID = 1L;
     private String scopeId;
 
     /**
      * @return the scopeId
      */
     public String getScopeId() {
         return scopeId;
     }
 
     /**
      * @param scopeId
      *            the scopeId to set
      */
     public void setScopeId(String scopeId) {
         this.scopeId = scopeId;
     }
 }
