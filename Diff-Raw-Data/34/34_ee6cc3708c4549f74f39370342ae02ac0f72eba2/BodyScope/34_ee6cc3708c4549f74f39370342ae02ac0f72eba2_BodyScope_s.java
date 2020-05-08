 package uk.ac.ic.doc.cfg.model.scope;
 
 import org.python.pydev.parser.jython.SimpleNode;
 
 import uk.ac.ic.doc.cfg.model.BasicBlock;
 
 public class BodyScope extends ScopeWithParent {
 
 	private SimpleNode[] nodes;
 
 	public BodyScope(SimpleNode[] nodes, BasicBlock root, Scope parent) {
 		super(parent, root);
 		this.nodes = nodes;
 	}
 
 	@Override
 	protected ScopeExits doProcess() throws Exception {
 
 		BasicBlock bodyRoot = null;
 		ScopeExits body = new ScopeExits();
 		ScopeExits previousStatement = new ScopeExits();
 		ScopeExits lastStatement = new ScopeExits();
 
 		for (int i = 0; i < nodes.length; i++) {
 
 			// If there are more statements in this body and the statement we
 			// just processed had multiple escape edges, the processing of the
 			// next statement must start at a new basic block and the
 			// fallthrough escape edges must link to it.
 			if (lastStatement.isEndOfBlock()) {
				BasicBlock nextBlock = newBlock();
				setCurrentBlock(nextBlock);
				lastStatement.linkFallThroughsTo(nextBlock);
 			}
 
 			previousStatement = lastStatement;
 			lastStatement = (ScopeExits) nodes[i].accept(this);
 
 			// The first statement that actually contributes a basic
 			// block is the root block for this body
 			if (bodyRoot == null) {
 				bodyRoot = lastStatement.getRoot();
 			}
 			if (getCurrentBlock() == null) {
 				setCurrentBlock(lastStatement.getRoot());
 			}
 
 			// The statement processor may have added to the current basic block
 			// or it may have started a new one. We can detect this by comparing
 			// the root it returns with the current block. If they don't match
 			// then the processor started a new basic block. In this case we
 			// must link the current block to the new root.
			if (getCurrentBlock() != lastStatement.getRoot()) {
				assert previousStatement.getFallthroughQueue().size() == 1;
				
 				previousStatement.linkFallThroughsTo(lastStatement);
 				setCurrentBlock(lastStatement.getRoot());
 			}
			
 			// Union all statements' breakouts as we don't process them at this
 			// level
 			body.getBreakoutQueue().addAll(lastStatement.getBreakoutQueue());
 		}
 
 		// Only push up last statement's fallthroughs as the others are
 		// dealt with by the body processing loop here
 		body.getFallthroughQueue().addAll(lastStatement.getFallthroughQueue());
		
 		body.setRoot(bodyRoot);
		
 		assert body.exitSize() > 0;
 		return body;
 	}
 }
