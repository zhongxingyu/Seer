 package edu.tum.lua.types;
 
 import java.util.Arrays;
 import java.util.List;
 
 import edu.tum.lua.BlockVisitor;
 import edu.tum.lua.GlobalEnvironment;
 import edu.tum.lua.LocalEnvironment;
 import edu.tum.lua.ast.Block;
 import edu.tum.lua.ast.LegacyAdapter;
 import edu.tum.lua.ast.LocalFuncDef;
 import edu.tum.lua.exceptions.LuaRuntimeException;
 
 public class LuaFunctionInterpreted implements LuaFunction {
 
 	private final LocalEnvironment environment;
 	private final List<String> argumentNames;
 	private final Block block;
 	private final boolean vararg;
 
 	public LuaFunctionInterpreted(List<String> args, boolean vararg, Block block, LocalEnvironment e) {
 		this.argumentNames = args;
 		this.vararg = vararg;
 		this.block = block;
 		this.environment = e;
 	}
 
 	public LuaFunctionInterpreted(LocalFuncDef node, LocalEnvironment e) {
 		environment = e;
 		argumentNames = LegacyAdapter.convert(node.args);
 		block = node.block;
 		vararg = node.varargs;
 	}
 
 	@Override
 	public List<Object> apply(List<Object> arguments) {
 		LocalEnvironment currentEnvironment = new LocalEnvironment(environment);
 
 		for (int i = 0; i < Math.min(argumentNames.size(), arguments.size()); i++) {
 			currentEnvironment.setLocal(argumentNames.get(i), arguments.get(i));
 		}
 
 		BlockVisitor blockVisitor;
 
 		if (vararg) {
 			blockVisitor = new BlockVisitor(currentEnvironment, arguments.subList(argumentNames.size(),
 					arguments.size()));
 		} else {
 			blockVisitor = new BlockVisitor(currentEnvironment);
 		}
 
 		block.accept(blockVisitor);
 
 		if (blockVisitor.getBreak()) {
 			throw new LuaRuntimeException("No loop to break");
 		}
 
 		return blockVisitor.getReturn();
 	}
 
 	@Override
 	public List<Object> apply(Object... arguments) {
 		return apply(Arrays.asList(arguments));
 	}
 
 	public GlobalEnvironment getGlobalEnvironment() {
 		return environment.getGlobalEnvironment();
 	}
 
 	public void setGlobalEnvironment(GlobalEnvironment e) {
 		environment.setGlobalEnvironment(e);
 	}
 }
