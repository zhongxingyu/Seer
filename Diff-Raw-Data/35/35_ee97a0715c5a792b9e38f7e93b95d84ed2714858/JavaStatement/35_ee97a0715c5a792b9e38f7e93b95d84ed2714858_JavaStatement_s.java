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
 	JavaStatement(JavaScope scope, GNode n) {
 		super(scope, n);
 	}
 }
