 package translator.Statements;
 
 import translator.JavaScope;
 import xtc.tree.GNode;
 
 /**
  * Handles anything that can have its own scope, from a File to a Block.
  */
 class JavaStatement extends JavaScope {
 	/**
 	 * SAEKJFA;WIE JF K;LSDFJ ASILD JFASD;IFJ!!!!!!! WHY DOES JAVA NOT INHERIT CONSTRUCTORS?!?!?!?!?!?!?!?!?!??!
 	 * This feels so dirty and wrong.
 	 */
	JavaScope header;
	ArrayList<JavaScope> subscope = newArrayList<JavaScope>();
 	JavaStatement(JavaScope scope, GNode n) {
 		super(scope, n);
		header=n.dispatch(0);
		for (int i=1;i<n.size();++i){
			final GNode g=(GNode)n.get(i);
			if (g!=null){
				subscope.add((JavaScope)n.dispatch(g));
			}
		}
	}
	CodeBlock printMe(CodeBlock block){
		return null;
 	}
 }
