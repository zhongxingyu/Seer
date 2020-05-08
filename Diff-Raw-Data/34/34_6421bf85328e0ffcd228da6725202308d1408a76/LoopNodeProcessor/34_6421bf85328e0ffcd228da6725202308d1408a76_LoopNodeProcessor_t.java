 package swp_compiler_ss13.fuc.ir.components.statements;
 
 import swp_compiler_ss13.common.ast.nodes.ExpressionNode;
 import swp_compiler_ss13.common.ast.nodes.binary.DoWhileNode;
 import swp_compiler_ss13.common.ast.nodes.binary.WhileNode;
 import swp_compiler_ss13.common.ast.nodes.leaf.BreakNode;
 import swp_compiler_ss13.common.ir.IntermediateCodeGeneratorException;
 import swp_compiler_ss13.common.types.Type.Kind;
 import swp_compiler_ss13.fuc.ir.GeneratorExecutor;
 import swp_compiler_ss13.fuc.ir.GeneratorState;
 import swp_compiler_ss13.fuc.ir.QuadrupleFactory;
 import swp_compiler_ss13.fuc.ir.components.NodeProcessor;
 import swp_compiler_ss13.fuc.ir.data.IntermediateResult;
 
 /**
  * Processor for all loop related nodes
  * 
  * @author "Frank Zechert"
  * @author "Danny Maasch"
  * @author kaworu
  * @version 3
  */
 public class LoopNodeProcessor extends NodeProcessor {
 
 	/**
 	 * Create a new instance of the loop node processor.
 	 * 
 	 * @param state
 	 *            the state of the intermediate code generator
 	 * @param executor
 	 *            the intermediate code generator executor
 	 */
 	public LoopNodeProcessor(GeneratorState state, GeneratorExecutor executor) {
 		super(state, executor);
 	}
 
 	/**
 	 * Process a BreakNode
 	 * 
 	 * @param node
 	 *            the breakNode to process
 	 */
 	public void processBreakNode(BreakNode node) {
 		String endOfLoop = this.state.getCurrentBreakLabel();
 		this.state.addIntermediateCode(QuadrupleFactory.jump(endOfLoop));
 	}
 
 	/**
 	 * Process a DoWhileNode
 	 * 
 	 * @param node
 	 *            The doWhileNode to process
 	 * @throws IntermediateCodeGeneratorException
 	 *             The condition of the loop is not of type boolean
 	 */
	public void processDoWhileNode(DoWhileNode node)
			throws IntermediateCodeGeneratorException {
 		// label before do while loop
 		String beforeLoop = this.state.createNewLabel();
 
 		// label at the end of the loop
 		String endOfLoop = this.state.createNewLabel();
 
 		this.state.pushBreakLabel(endOfLoop);
 
		this.state.addIntermediateCode(QuadrupleFactory.label(beforeLoop));

		this.executor.processWithoutResult(node.getLoopBody());

 		// evaluate the condition
 		ExpressionNode condition = node.getCondition();
 		IntermediateResult conditionResult = this.executor.process(condition);
 
 		// if condition does not evaluate to boolean throw an error
 		if (conditionResult.getType().getKind() != Kind.BOOLEAN) {
 			throw new IntermediateCodeGeneratorException(
					"Condition must be of type Boolean but is of type "
							+ conditionResult.getType());
 		}
 
 		// create the ir code
 
		this.state.addIntermediateCode(QuadrupleFactory.branch(
				conditionResult.getValue(), beforeLoop, endOfLoop));
 		this.state.addIntermediateCode(QuadrupleFactory.label(endOfLoop));
 
 		this.state.popBreakLabel();
 	}
 
 	/**
 	 * Process a WhileNode
 	 * 
 	 * @param node
 	 *            The whileNode to process
 	 * @throws IntermediateCodeGeneratorException
 	 *             The condition of the loop is not of type boolean
 	 */
	public void processWhileNode(WhileNode node)
			throws IntermediateCodeGeneratorException {
 
 		// label before condition
 		String beforeCondition = this.state.createNewLabel();
 
 		// label at the beginning of the loop body
 		String loopBody = this.state.createNewLabel();
 
 		// label after the loop
 		String endOfLoop = this.state.createNewLabel();
 
 		this.state.pushBreakLabel(endOfLoop);
 
 		this.state.addIntermediateCode(QuadrupleFactory.label(beforeCondition));
 
 		// evaluate the condition
 		ExpressionNode condition = node.getCondition();
 		IntermediateResult conditionResult = this.executor.process(condition);
 
 		// if condition does not evaluate to boolean throw an error
 		if (conditionResult.getType().getKind() != Kind.BOOLEAN) {
 			throw new IntermediateCodeGeneratorException(
					"Condition must be of type Boolean but is of type "
							+ conditionResult.getType());
 		}
 
 		// create the ir code
		this.state.addIntermediateCode(QuadrupleFactory.branch(
				conditionResult.getValue(), loopBody, endOfLoop));
 		this.state.addIntermediateCode(QuadrupleFactory.label(loopBody));
 
 		this.executor.processWithoutResult(node.getLoopBody());
 
 		this.state.addIntermediateCode(QuadrupleFactory.jump(beforeCondition));
 		this.state.addIntermediateCode(QuadrupleFactory.label(endOfLoop));
 
 		this.state.popBreakLabel();
 	}
 
 }
