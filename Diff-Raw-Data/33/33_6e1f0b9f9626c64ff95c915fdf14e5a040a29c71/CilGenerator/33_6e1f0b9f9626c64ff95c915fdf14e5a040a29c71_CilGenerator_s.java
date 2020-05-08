 package test.compiler.tools;
 
 import java.util.UUID;
 
 public class CilGenerator {
 	private final static String newLine = System.getProperty("line.separator");
 	private final String INIT_CODE;
 	private final String END_CODE;
 	private StringBuilder cBuilder;
 	private StringBuilder lBuilder;
 	private StringBuilder laBuilder;
 	private boolean hasLocalVars; 
 	
 
 	public CilGenerator() {
 		INIT_CODE = String
 				.format(".assembly extern mscorlib {auto}%s.assembly lilWildc { }%s.class public auto ansi lilWildc extends [mscorlib]System.Object {",
 						newLine, newLine);
 		END_CODE = String.format("}");
 		cBuilder = new StringBuilder(
 				".method private static  specialname  rtspecialname default void .cctor ()  cil managed {");
 		lBuilder = new StringBuilder(".locals init (");
 		laBuilder = new StringBuilder();
 		hasLocalVars = false;
 	}
 
 	public String getInitCode() {
 		return INIT_CODE;
 	}
 
 	public String getEndCode() {
 		return END_CODE;
 	}
 
 	public String genConstructor() {
 		cBuilder.append(String.format("%s}", newLine));
 		return this.cBuilder.toString();
 	}
 
 	public String genGlobalVar(String var) {
 		return String.format(".field public static int32 %s", var);
 	}
 
 	public String genGlobalVarArray(String var, String size) {
 		cBuilder.append(String.format("%sldc.i4 %s", newLine, size));
 		cBuilder.append(String.format("%snewarr int32", newLine));
 		cBuilder.append(String.format("%sstsfld int32[] lilWildc::%s", newLine,
 				var));
 		return String.format(".field public static int32[] %s", var);
 	}
 
 	public String genProcedureStart(String name) {
 		StringBuilder sb = new StringBuilder();
 		sb.append(String.format(".method static public void %s() il managed",
 				name));
 		sb.append(newLine);
 		sb.append("{");
 		sb.append(newLine);
 		if (name.equals("main"))
 			sb.append(".entrypoint");
 		sb.append(newLine);
 		sb.append(".maxstack 8");
 		return sb.toString();
 	}
 
 	public void addLocalVar(String id) {
 		if(!hasLocalVars){
 			lBuilder.append("class [mscorlib]System.Collections.Generic.List`1<object> printList,");
 			lBuilder.append("int32 printCount,");
 			lBuilder.append(String.format("int32 %s ", id));
 			hasLocalVars = true;
 		}
 		else{
 			lBuilder.append(String.format(", int32 %s ", id));
 		}
 	}
 	
 	public void addLocalVarArray(String id, String size) {
 		if(!hasLocalVars){
 			lBuilder.append("class [mscorlib]System.Collections.Generic.List`1<object> list,");
 			lBuilder.append(String.format("int32[] %s ", id));
 			hasLocalVars = true;
 			laBuilder.append(String.format("ldc.i4.s %d", size));
 		}
 		else{
 			lBuilder.append(String.format(", int32[] %s ", id));
 			laBuilder.append(String.format("%sldc.i4.s %s", newLine, size));
 		}
 		laBuilder.append(String.format("%snewarr [mscorlib]System.Int32", newLine));
 		laBuilder.append(String.format("%sstloc %s", newLine, id));
 	}
 	
 	public String getLocalVars(){
 		lBuilder.append(')');
 		String output = hasLocalVars ? lBuilder.toString() : null;
 		lBuilder = new StringBuilder(".locals init (");
 		hasLocalVars = false;
 		return output + getLocalArrayInit();
 	}
 	
 	private String getLocalArrayInit(){
 		String output = laBuilder.toString();
 		laBuilder = new StringBuilder();
 		return output;
 	}	
 	
 	public String input(String var){
 		StringBuilder sb = new StringBuilder("call string [mscorlib]System.Console::ReadLine()");
 		sb.append(String.format("%sstloc %s", newLine, var));
 		return sb.toString();
 	}
 	
 	public String clearPrintList(){
 		StringBuilder sb = new StringBuilder("newobj instance void class [mscorlib]System.Collections.Generic.List`1<object>::.ctor()");
 		sb.append(String.format("%sstloc printList", newLine));
 		return sb.toString();
 	}
 	
 	public String addPrintString(String string){
 		StringBuilder sb = new StringBuilder("ldloc printList");
 		sb.append(String.format("%sldstr %s", newLine, string));
 		sb.append(String.format("%scallvirt instance void class [mscorlib]System.Collections.Generic.List`1<object>::Add(!0)", newLine));
 		return sb.toString();
 	}
 	
 	public String addPrintExpr(){
 		return "callvirt instance void class [mscorlib]System.Collections.Generic.List`1<object>::Add(!0)";
 	}
 	
 	public String whileLoop(String innerCode){
 		String startLoop = UUID.randomUUID().toString();
 		String endLoop = UUID.randomUUID().toString();
 		
 		StringBuilder sb = new StringBuilder(String.format("%s:", startLoop));
 		sb.append(String.format("%s%s:",newLine, endLoop));
 		return sb.toString();		
 	}
 	
 	public String printLoop(){
		String beginLoop = UUID.randomUUID().toString();
		String endLoop = UUID.randomUUID().toString();
 		StringBuilder sb = new StringBuilder("ldc.i4 0");
 		sb.append(String.format("%sstloc printCount", newLine));
 		
 		//Check to see if there are any elements in the list
 		sb.append(String.format("%sldloc printList", newLine));
		sb.append(String.format("%scallvirt instance void class [mscorlib]System.Collections.Generic.List`1<object>::Count()", newLine));
 		sb.append(String.format("%sldc.i4.s 1", newLine));
		sb.append(String.format("%sblt %s", newLine, endLoop));
 		//End the check of elements in the list
 		
 		//Start the loop
 		sb.append(String.format("%s%s:", newLine, beginLoop));
 		
 		//Load an Item to be printed and print it
 		sb.append(String.format("%sldloc printList", newLine));
 		sb.append(String.format("%sldloc printCount", newLine));
		sb.append(String.format("%scallvirt instance void class [mscorlib]System.Collections.Generic.List`1<object>::Item(!0)", newLine));
		sb.append(String.format("%scall void [mscorlib]System.Console::Write(string)", newLine));
 
 		
 		sb.append(String.format("%sldloc printCount", newLine));
 		sb.append(String.format("%sldloc printList", newLine));
		sb.append(String.format("%scallvirt instance void class [mscorlib]System.Collections.Generic.List`1<object>::Count()", newLine));
 		sb.append(String.format("%sldloc printCount", newLine));
 		sb.append(String.format("%sldc.i4.s 1", newLine));
 		sb.append(String.format("%sadd", newLine));
 		sb.append(String.format("%sstloc printCount", newLine));
		sb.append(String.format("%sblt.s %s", newLine, beginLoop));
 		
 		sb.append(String.format("%s%s:", newLine, endLoop));
 		return sb.toString();
 	}
 	
 }
