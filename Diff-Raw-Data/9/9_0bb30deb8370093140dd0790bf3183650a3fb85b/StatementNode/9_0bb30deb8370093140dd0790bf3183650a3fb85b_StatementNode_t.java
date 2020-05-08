 package nodes;
 
 import java.util.Scanner;
 
 import core.Parser;
 import core.Robot;
 import core.RobotProgramNode;
 
 public class StatementNode implements RobotProgramNode {
 
 	private RobotProgramNode child;
 
 	@Override
 	public void execute(Robot robot) {
 		child.execute(robot);
 	}
 
 	@Override
 	public boolean parse(Scanner s, int depth) {
 		if(!s.hasNext()){
 			Parser.fail("Empty Expression", s);
 		}
 		if(s.hasNext(Parser.ACTIONPAT)){
 			child = new ActionNode();
 			return child.parse(s, depth);
 		}
 		if(s.hasNext(Parser.LOOPPAT)){
 			child = new LoopNode();
 			return child.parse(s, depth);
 		}
 		if(s.hasNext(Parser.IFPAT)){
 			child = new IfNode();
 			return child.parse(s, depth);
 		}
 		if(s.hasNext(Parser.WHILEPAT)){
 			child = new WhileNode();
 			return child.parse(s, depth);
 		}
 		if(s.hasNext(Parser.VARPAT)){
 			child = new AssignmentNode();
 			return child.parse(s, depth);
 		}
		Parser.fail("Unknown Statement", s);
 		return false;
 	}
 
 	@Override
 	public String toString(){
 		return child.toString();
 	}
 
 }
