 package haw.ci.lib;
 
 import static haw.ci.lib.Tokens.*;
 
 import java.util.Arrays;
 import java.util.List;
 
 public class Parser {
 	private ITokenStream tokenStream;
 	private Yytoken current;
 	public Parser(ITokenStream tokenStream) {
 		this.tokenStream = tokenStream;
 	}
 //IdentList = ident {, ident}.
 	private void IdentList() {
 		accept(IDENTIFER);
 		while (accept(COMMA)) {
 			accept(IDENTIFER);
 		}
 	}
 //ArrayType = ARRAY [ IndexExpression ] OF Type.
 	private void Arraytype() {
 		accept(ARRAY);
 		accept(BRACE_SQUARE_OPEN);
 		IndexExpression();
 		accept(OF);
 		Type();
 	}
 //FieldList = [IdentList : Type].
 	private void FieldList() {
 		if (accept(IDENTIFER)) {
 			accept(COLON);
 			Type();
 		}
 	}
 //RecordType = RECORD FieldList {; FieldList} END.
 	private void RecordType() {
 		accept(RECORD);
 		FieldList();
 		while(accept(SEMICOLON)) {
 			FieldList();
 		}
 		accept(END);
 	}
 //Type = ident | ArrayType | RecordType.
 	private void Type() {
 		switch (current.getToken()) {
 		case IDENTIFER:
 			// was hier?
 			break;
 		case ARRAY:
 			Arraytype();
 			break;
 		case RECORD:
 			RecordType();
 			break;
 		}
 	}
 //FPSection = [VAR] IdentList : Type.
 	private void FPSection() {
 		accept(VAR);
 		IdentList();
 		accept(COLON);
 		Type();
 	}
 //FormalParameters = FPSection {; FPSection}.
 	private void FormalParameters() {
 		FPSection();
 		while (accept(SEMICOLON)) {
 			FPSection();
 		}
 	}
 //ProcedureHeading = PROCEDURE ident ( [FormalParameters] ).
 	private void ProcedureHeading() {
 		accept(PROCEDURE);
 		accept(IDENTIFER);
 		accept(BRACE_ROUND_OPEN);
 		FormalParameters();
 		accept(BRACE_ROUND_CLOSE);
 	}
 //ProcedureBody = Declarations BEGIN StatementSequence END.
 	private void ProcedureBody() {
 		Declarations();
 		accept(BEGIN);
 		StatementSequence();
 		accept(END);
 	}
 //ProcedureDeclaration = ProcedureHeading ; ProcedureBody ident.
 	private void ProcedureDeclatation() {
 		ProcedureHeading();
 		accept(SEMICOLON);
 		ProcedureBody();
 		accept(IDENTIFER);
 	}
 //Declarations = 
 //	[CONST ident = Expression ; {ident = Expression ;}] 
 //	[TYPE ident = Type ; {ident = Type ;}]
 //	[VAR IdentList : Type ; {IdentList : Type ;}] 
 // 	{ProcedureDeclaration ;}.
 	private void Declarations() {
 		// TODO: implement
 	}
 //Module = MODULE ident ; Declarations BEGIN StatementSequence END ident ..
 	private void Module() {
 		accept(MODULE);
 		accept(IDENTIFER);
 		accept(SEMICOLON);
 		Declarations();
 		accept(BEGIN);
 		StatementSequence();
 		accept(END);
 		accept(IDENTIFER);
 		accept(DOT);
 	}
 //Assignment = ident Selector := Expression.
 	private void Assignment() {
 		accept(IDENTIFER);
 		accept(ASSIGN);
 		Expression();
 	}
 //ActualParameters = Expression {, Expression}.
 	private void ActualParameters() {
 		Expression();
 		while(accept(COMMA)) {
 			Expression();
 		}
 	}
 //ProcedureCall = ident ( [ActualParameters] ).
 	private void ProcedureCall() {
 		accept(IDENTIFER);
 		accept(BRACE_ROUND_OPEN);
 		ActualParameters();
 		accept(BRACE_ROUND_CLOSE);
 	}
 //IfStatement = IF Expression THEN StatementSequence {ELSIF Expression THEN StatementSequence} [ELSE StatementSequence] END.
 	private void IfStatement() {
 		accept(IF);
 		Expression();
 		accept(THEN);
 		StatementSequence();
 		while (accept(ELSIF)) {
 			Expression();
 			accept(THEN);
 			StatementSequence();
 		}
 		if (accept(ELSE)) {
 			StatementSequence();
 		}
 		accept(END);
 	}
 //WhileStatement = WHILE Expression DO StatementSequence END.
 	private AbstractNode WhileStatement() {
		AbstractNode node, expression, statementSequence;
 		if (accept(WHILE)) {
 			expression = Expression();
 			if (accept(DO)) {
 				statementSequence = StatementSequence();
 				if (accept(END)) {
 					node = new WhileStatementNode(expression, statementSequence);
 				}
 			}
 		}
 		return node;
 	}
 //RepeatStatement = REPEAT StatementSequence UNTIL Expression.
 	private void RepeatStatement() {
 		accept(REPEAT);
 		StatementSequence();
 		accept(UNTIL);
 		Expression();
 	}
 //Statement = [Assignment | ProcedureCall | IfStatement | PRINT Expression | WhileStatement | RepeatStatement].
 	private AbstractNode Statement() {
 		// TODO: implement
 		return null;
 	}
 //StatementSequence = Statement {; Statement}.
 	private StatementSequenceNode StatementSequence() {
 		List<AbstractNode> list = Arrays.asList(Statement());
 
 		while (accept(SEMICOLON)) {
 			list.add(Statement());
 		}
 
 		return new StatementSequenceNode(list);
 	}
 //Selector = {. ident | [ Expression ]}.
 	private void Selector() {
 		// TODO: while
 		switch(current.getToken()) {
 		case DOT:
 			next();
 			accept(IDENTIFER);
 			break;
 		case BRACE_SQUARE_OPEN:
 			next();
 			Expression();
 			accept(BRACE_SQUARE_CLOSE);
 			break;
 		}
 	}
 //Factor = ident Selector | integer | string | Read | ( Expression ).
 	private AbstractNode Factor() {
 		switch(current.getToken()) {
 		case IDENTIFER:
 			break;
 		case INTEGER:
 			break;
 		// TODO: String
 		case READ:
 			Read();
 			break;
 		case BRACE_ROUND_OPEN:
 			next();
 			Expression();
 			accept(BRACE_ROUND_CLOSE);
 			break;
 		}
 	}
 //Read = READ [Prompt].
 	private void Read() {
 		accept(READ);
 		Prompt();
 	}
 //Prompt = string.
 	private void Prompt() {
 		// TODO: implement
 	}
 //Term = Factor {(* | /) Factor}.
 	private AbstractNode Term() {
 		AbstractNode node = Factor();
 
 		if (accept(MATH_MUL) || accept(MATH_DIV)) {
 			node = new BinaryOperationNode(current.getToken(), node, Factor());
 		}
 
 		return node;
 	}
 //SimpleExpression = [-] Term {(+ | -) Term}.
 	private AbstractNode SimpleExpression() {
 		AbstractNode node;
 		if (accept(MATH_SUB)) {
 			node = new NegatedNode(Term());
 		} else {
 			node = Term();
 		}
 
 		if (accept(MATH_ADD) || accept(MATH_SUB)) {
 			node = new BinaryOperationNode(current.getToken(), node, Term());
 		}
 
 		return node;
 	}
 //Expression = SimpleExpression [(= | # | < | <= | > | >=) SimpleExpression].
 	private AbstractNode Expression() {
 		AbstractNode node;
 		node = SimpleExpression();
 		while (accept(EQUAL) || accept(LESS) || accept(LESS_EQUAL) || accept(MORE) || accept(MORE_EQUAL)) {
 			node = new BinaryOperationNode(current.getToken(), node, SimpleExpression());
 		}
 
 		return node;
 	}
 //IndexExpression = integer | ConstIdent.
 	private void IndexExpression() {
 		ConstIdent();
 	}
 //ConstIdent = ident.
 	private void ConstIdent() {
 		accept(IDENTIFER);
 	}
 
 	public String build() {
 		next();
 		Module();
 		return "yeah";
 	}
 
 	private Yytoken next() {
 		return this.current = tokenStream.nextToken();
 	}
 	
 	private boolean accept(Tokens token) {
 		if (current.getToken() == token) {
 			next();
 			return true;
 		}
 		return false;
 	}
 	
 	private boolean except(Tokens token) {
 		if (accept(token)) {
 			System.out.println(String.format("%s not accepted at line %d", token, current.getLine()));
 			return false;
 		}
 		return true;
 	}
 }
