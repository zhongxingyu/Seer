 package swp_compiler_ss13.fuc.symbolTable;
 
 import java.util.HashMap;
 
 import org.apache.log4j.Logger;
 
 import swp_compiler_ss13.common.optimization.Liveliness;
 import swp_compiler_ss13.common.parser.SymbolTable;
 import swp_compiler_ss13.common.types.Type;
 
 public class SymbolTableImpl implements SymbolTable {
 
 	private static long ext;
 	private static Logger logger = Logger.getLogger(SymbolTableImpl.class);
 
 	private SymbolTable parent = null;
 	private HashMap<String, Type> symbolMap;
 	private HashMap<String, Liveliness> liveMap;
 	private HashMap<String, String> aliasMap;
 
 	public SymbolTableImpl(SymbolTable parent) {
 		this.parent = parent;
 		this.symbolMap = new HashMap<String, Type>();
 		this.liveMap = new HashMap<String, Liveliness>();
 		this.aliasMap = new HashMap<String, String>();
 	}
 
 	public SymbolTableImpl() {
 		this.symbolMap = new HashMap<String, Type>();
 		this.liveMap = new HashMap<String, Liveliness>();
 		this.aliasMap = new HashMap<String, String>();
 	}
 
 	@Override
 	public SymbolTable getParentSymbolTable() {
 		return this.parent;
 	}
 
 	@Override
 	public Boolean isDeclared(String identifier) {
 		if (this.parent == null) {
 			return this.symbolMap.containsKey(identifier);
 		} else {
 			if (this.symbolMap.containsKey(identifier)) {
 				return true;
 			} else {
 				return this.parent.isDeclared(identifier);
 			}
 		}
 	}
 
 	@Override
 	public Type lookupType(String identifier) {
 		if (this.parent == null) {
 			return this.symbolMap.get(identifier);
 		} else {
 			if (this.symbolMap.containsKey(identifier)) {
 				return this.symbolMap.get(identifier);
 			} else {
 				return this.parent.lookupType(identifier);
 			}
 		}
 	}
 
 	@Override
 	public Boolean insert(String identifier, Type type) {
 		if (!this.isDeclared(identifier)) {
 			this.symbolMap.put(identifier, type);
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public Boolean remove(String identifier) {
 		this.aliasMap.remove(identifier);
 		return this.symbolMap.remove(identifier) != null;
 	}
 
 	@Override
 	public void setLivelinessInformation(String identifier,
 			Liveliness liveliness) {
 		this.liveMap.put(identifier, liveliness);
 	}
 
 	@Override
 	public Liveliness getLivelinessInformation(String identifier) {
 		return this.liveMap.get(identifier);
 	}
 
 	@Override
 	public String getNextFreeTemporary() {
 		String temp;
 		do {
 			temp = "tmp" + ext;
 			ext++;
 		} while (this.isDeclared(temp));
 		return temp;
 	}
 
 	@Override
 	public void putTemporary(String identifier, Type type) {
 		if (this.parent != null) {
 			this.parent.putTemporary(identifier, type);
 		} else {
 			this.insert(identifier, type);
 		}
 
 	}
 
 	@Override
 	public SymbolTable getRootSymbolTable() {
 		if (this.parent == null) {
 			return this;
 		} else {
 			return this.parent.getRootSymbolTable();
 		}
 	}
 
 	@Override
 	public Boolean isDeclaredInCurrentScope(String identifier) {
 		return this.symbolMap.containsKey(identifier);
 	}
 
 	@Override
 	public Type lookupTypeInCurrentScope(String identifier) {
 		return this.symbolMap.get(identifier);
 	}
 
 	@Override
 	public void setIdentifierAlias(String identifier, String alias) {
 		if (this.isDeclaredInCurrentScope(identifier)) {
 			this.aliasMap.put(identifier, alias);
 		}
 		else {
 			logger.warn("Can not set an alias for identifier " + identifier
 					+ " beacuase it is not declared in this symbol table!");
 			logger.warn("Use getDeclaringSymbolTable() to get the correct symbol table that declares " + identifier);
 		}
 	}
 
 	@Override
 	public String getIdentifierAlias(String identifier) {
		return this.aliasMap.get(identifier);
 	}
 
 	@Override
 	public SymbolTable getDeclaringSymbolTable(String identifier) {
 		if (this.isDeclaredInCurrentScope(identifier)) {
 			return this;
 		} else {
 			if (this.parent != null) {
 				return this.parent.getDeclaringSymbolTable(identifier);
 			} else {
 				return null;
 			}
 		}
 	}
 
 }
