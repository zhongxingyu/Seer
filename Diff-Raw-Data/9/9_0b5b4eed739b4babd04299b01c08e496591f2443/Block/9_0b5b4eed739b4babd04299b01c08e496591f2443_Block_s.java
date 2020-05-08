 package chameleon.oo.statement;
 
 import java.util.List;
 
 import org.rejuse.association.OrderedMultiAssociation;
 import org.rejuse.java.collections.Visitor;
 
 import chameleon.core.element.Element;
 import chameleon.core.lookup.LookupException;
 import chameleon.core.lookup.LookupStrategy;
 import chameleon.core.validation.Valid;
 import chameleon.core.validation.VerificationResult;
 import chameleon.exception.ChameleonProgrammerException;
 
 /**
  * @author Marko van Dooren
  */
 public class Block extends StatementImpl<Block> implements StatementListContainer<Block> {
 	//TODO: should this be a member, or should there be a separate ObjectInitializer that contains a block ?
 	//TODO: can any statement be a member ? In this case the methods of Member have to move up.
 
 	/**
 	 * STATEMENTS
 	 */
 	private OrderedMultiAssociation<Block,Statement> _statements = new OrderedMultiAssociation<Block,Statement>(this);
 
   public OrderedMultiAssociation<Block,Statement> getStatementsLink() {
     return _statements;
   }
 
   public void addStatement(Statement statement) {
   	if(statement != null) {
       _statements.add(statement.parentLink());
   	}
   }
   
   public void addInFront(Statement statement) {
 	  if(statement != null) {
 		  _statements.addInFront(statement.parentLink());
 	  }
   }
 
   public void removeStatement(Statement statement) {
   	if(statement != null) {
       _statements.remove(statement.parentLink());
   	}
   }
 
   public List<Statement> statements() {
     return _statements.getOtherEnds();
   }
 
   public Block clone() {
     final Block result = new Block();
     new Visitor<Statement>() {
       public void visit(Statement element) {
         result.addStatement(element.clone());
       }
     }.applyTo(statements());
     return result;
   }
   
   public void clear() {
   	for(Element child: children()) {
   		child.disconnect();
   	}
   }
 
   public List<? extends Element> children() {
     return statements();
   }
 
   public int getIndexOf(Statement statement) {
     return statements().indexOf(statement) + 1;
   }
   
 	public List<Statement> statementsAfter(Statement statement) {
 		List<Statement> statements = statements(); 
 		int index = statements.indexOf(statement);
 		// returns a view on a clone of _statements (getStatements() clones the list).
 		// the view depends on the local variable, but since no other references exist
 		// this is not a problem.
 		return statements.subList(index, statements.size());
 	}
 
  /*@
    @ public behavior
    @
    @ post getStatements().indexOf(element) == 0) ==> \result == parent().lexicalContext(this);
    @ post getStatements().indexOf(Element) > 0) ==> 
    @      \result == getStatements().elementAt(getStatements().indexOf(element) - 1).lexicalContext();
    @*/
 	public LookupStrategy lexicalLookupStrategy(Element element) throws LookupException {
 		List<Statement> declarations = statements();
 		int index = declarations.indexOf(element);
 		if(index == 0) {
 			return parent().lexicalLookupStrategy(this);
 		} else if (index > 0) {
 			return declarations.get(index-1).linearLookupStrategy();
 		} else {
 		  throw new ChameleonProgrammerException("Invoking lexicalContext(element) with an element that is not a child.");
 		}
 	}
 
 	@Override
 	public VerificationResult verifySelf() {
 		return Valid.create();
 	}
 
 	public void addBlock(Block block) {
 		if (block != null) {
 			for (Statement st : block.statements()) {
 				addStatement(st.clone());
 			}
 		}
 	}
 
 	public void addStatements(List<? extends Statement> statements) {
 		if (statements != null) {
 			for (Statement st : statements)
 				addStatement(st);
 		}
 	}
 
 	
 }
