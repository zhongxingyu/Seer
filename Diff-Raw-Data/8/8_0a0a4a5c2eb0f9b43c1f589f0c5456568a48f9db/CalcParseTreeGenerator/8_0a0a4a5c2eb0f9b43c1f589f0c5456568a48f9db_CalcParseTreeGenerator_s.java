package tv.myhome.darkcircle.Calc;
 // CalcParseTreeGenerator.java 
 //
 // This class is for generating parse tree from token stream which is made by CalcTokenAnalyzer.
 //
 // This program can be distributed under the terms of GNU GPL v3 or later.
 // darkcircle.0426@gmail.com
 
import tv.myhome.darkcircle.Calc.Exceptions.InvalidSequenceTokenException;
import tv.myhome.darkcircle.Calc.Exceptions.ParenthesesMismatchException;
 
 public class CalcParseTreeGenerator {
 	
 	// private TokenArray sourceArray;
 	
 	public CalcParseTreeGenerator ( )
 	{
 		;
 	}
 	
 	public static ParseTreeUnit generateParseTree ( TokenArray sArray ) throws ParenthesesMismatchException, InvalidSequenceTokenException
 	{
 		int size = sArray.getSize();
 		
 		ParseTreeUnit pstu_root = new ParseTreeUnit();
 
 		int i = 0;
 		
 		TokenUnit arg0 = null;
 		TokenUnit op = null;
 		TokenUnit arg1 = null;
 		
 		int lparen_cnt = 0;
 		int rparen_cnt = 0;
 		
 		// Check whether Pairs of parentheses are match or not.
 		for ( ; i < size ; i++ )
 		{
 			if ( sArray.getToken(i).getTokenSubtype() == TokenSubtype.Left_Parenthesis ) lparen_cnt++;
 			if ( sArray.getToken(i).getTokenSubtype() == TokenSubtype.Righ_Parenthesis ) rparen_cnt++;		
 		}
 		
 		if ( lparen_cnt != rparen_cnt ) throw new ParenthesesMismatchException();
 		
 		i = 0;
 		
 		while ( i < size )
 		{
 			if ( i == 0 )
 			{
 				arg0 = sArray.getToken(i++);
 				
 				if ( arg0.getTokenSubtype() == TokenSubtype.Minus ){
 					TokenUnit zero = new TokenUnit ( TokenType.Integer, TokenSubtype.Decimal, "0" );
 
 					pstu_root.setNode(arg0);
 					pstu_root.setLeftLeapNode(zero);
 					
 					TokenArray subArray = new TokenArray();
 					
 					do
 					{
 						subArray.addToken(sArray.getToken(i++));
 					}
 					while ( i < size );
 					
 					pstu_root.addRightSubtree( generateParseTree( subArray ) );
 				}
 				else if ( arg0.getTokenSubtype() == TokenSubtype.Left_Parenthesis )
 				{
 					TokenArray subArray1 = new TokenArray(); 
 					
 					int skipPairofParentheses = 0;
 					
 					while ( true )
 					{
 						TokenUnit temp = new TokenUnit();
 						temp = sArray.getToken(i);
 						
 						if ( temp.getTokenSubtype() == TokenSubtype.Left_Parenthesis )
 							skipPairofParentheses++;
 						if ( temp.getTokenSubtype() == TokenSubtype.Righ_Parenthesis )
 							skipPairofParentheses--;
 						
 						if ( skipPairofParentheses == -1 )
 						{
 							i++;
 							break;
 						}
 						
 						subArray1.addToken(temp);
 						i++;
 					}
 					
 					if ( i == size || i == size - 1 )
 					{
 						if ( subArray1.getSize() == 1 )
 						{
 							pstu_root.setNode( subArray1.getToken( 0 ) );
 						}
 						else
 						{
 							pstu_root = generateParseTree ( subArray1 );
 						}
 					}
 					else
 					{
 						op = sArray.getToken(i++);
 					
 						if ( op.getTokenType() != TokenType.Operatr )
 							throw new InvalidSequenceTokenException("Missing Operator");
 						
 						if ( i == size )
 							throw new InvalidSequenceTokenException("Missing Operand.");
 						
 						arg1 = sArray.getToken(i++);
 						
 						pstu_root.setNode ( op );
 						pstu_root.addLeftSubtree(generateParseTree( subArray1 ));
 						
 						if ( arg1.getTokenType() == TokenType.Integer || arg1.getTokenType() == TokenType.FlPoint )	
 							pstu_root.setRightLeapNode(arg1);
 						
 						else if ( arg1.getTokenType() == TokenType.Parents && arg1.getTokenSubtype() == TokenSubtype.Left_Parenthesis )
 						{
 							TokenArray subArray2 = new TokenArray();
 							skipPairofParentheses = 0;
 							
 							while ( true )
 							{
 								TokenUnit temp = new TokenUnit();
 								temp = sArray.getToken(i);
 								
 								if ( temp.getTokenSubtype() == TokenSubtype.Left_Parenthesis )
 									skipPairofParentheses++;
 								if ( temp.getTokenSubtype() == TokenSubtype.Righ_Parenthesis )
 									skipPairofParentheses--;
 								
 								if ( skipPairofParentheses == -1 )
 								{
 									i++;
 									break;
 								}
 								
 								subArray2.addToken(temp);
 								i++;
 							}
 							
 							if ( subArray2.getSize() == 1 )
 							{
 								pstu_root.setRightLeapNode( subArray2.getToken( 0 ) );
 							}
 							else
 							{
 								pstu_root.addRightSubtree( generateParseTree ( subArray2 ) );
 							}
 						}
 					}
 				}
 				else if ( arg0.getTokenType() == TokenType.Integer || arg0.getTokenType() == TokenType.FlPoint )
 				{
 					if ( size == 1 ) 
 					{
 						pstu_root.setNode(arg0);
 						break;
 					}
 					
 					op = sArray.getToken(i++);
 					if ( op.getTokenType() != TokenType.Operatr )
 						throw new InvalidSequenceTokenException("Missing operator.");
 					
 					if ( i == size && op.getTokenSubtype() != TokenSubtype.Factorial )
 						throw new InvalidSequenceTokenException("Missing operand.");
 					
 					if ( op.getTokenSubtype() == TokenSubtype.Factorial )
 					{
 						TokenUnit zero = new TokenUnit (TokenType.Integer, TokenSubtype.Decimal, "0");
 						arg1 = zero;
 					}
 					else
 					{
 						arg1 = sArray.getToken(i++);
 					}
 					
 					pstu_root.setNode( op );
 					pstu_root.setLeftLeapNode( arg0 );
 					
 					if ( arg1.getTokenType() == TokenType.Integer || arg1.getTokenType() == TokenType.FlPoint )	
 						pstu_root.setRightLeapNode(arg1);
 					else if ( arg1.getTokenType() == TokenType.Parents && arg1.getTokenSubtype() == TokenSubtype.Left_Parenthesis )
 					{
 						TokenArray subArray2 = new TokenArray();
 						int skipPairofParentheses = 0;
 						
 						while ( true )
 						{
 							TokenUnit temp = new TokenUnit();
 							temp = sArray.getToken(i);
 							
 							if ( temp.getTokenSubtype() == TokenSubtype.Left_Parenthesis )
 								skipPairofParentheses++;
 							if ( temp.getTokenSubtype() == TokenSubtype.Righ_Parenthesis )
 								skipPairofParentheses--;
 							
 							if ( skipPairofParentheses == -1 )
 							{
 								i++;
 								break;
 							}
 							
 							subArray2.addToken(temp);
 							i++;
 						}
 						
 						if ( subArray2.getSize() == 1 )
 						{
 							pstu_root.setRightLeapNode( subArray2.getToken( 0 ) );
 						}
 						else
 						{
 							pstu_root.addRightSubtree( generateParseTree ( subArray2 ) );
 						}
 					}
 					// arg1.getTokenType() == TokenType.TriangleFunc
 					else if ( arg1.getTokenType() == TokenType.TriangleFunc || arg1.getTokenType() == TokenType.MathematFunc )
 					{
 						TokenUnit zero = new TokenUnit ( TokenType.Integer, TokenSubtype.Decimal, "0" );
 						ParseTreeUnit newSubtree2 = new ParseTreeUnit();
 						
 						newSubtree2.setNode(arg1); // triangle function
 						newSubtree2.setLeftLeapNode(zero);
 						
 						arg1 = sArray.getToken(i++);
 						
 						if ( arg1.getTokenType() != TokenType.Parents )
 						{
 							throw new InvalidSequenceTokenException("Left parenthesis is expected.");
 						}
 						
 						TokenArray subArray2 = new TokenArray();
 						int skipPairofParentheses = 0;
 						
 						while ( true )
 						{
 							TokenUnit temp = new TokenUnit();
 							temp = sArray.getToken(i);
 							
 							if ( temp.getTokenSubtype() == TokenSubtype.Left_Parenthesis )
 								skipPairofParentheses++;
 							if ( temp.getTokenSubtype() == TokenSubtype.Righ_Parenthesis )
 								skipPairofParentheses--;
 							
 							if ( skipPairofParentheses == -1 )
 							{
 								i++;
 								break;
 							}
 							
 							subArray2.addToken(temp);
 							i++;
 						}
 						
 						if ( subArray2.getSize() == 1 )
 						{
 							newSubtree2.setRightLeapNode( subArray2.getToken( 0 ) );
 						}
 						else
 						{
 							newSubtree2.addRightSubtree( generateParseTree( subArray2 ) );
 						}
 						
 						pstu_root.addRightSubtree(newSubtree2);
 					}
 					else if ( arg1.getTokenType() == TokenType.BaseConvFunc )
 					{
 						throw new InvalidSequenceTokenException("Base conversion function can be used for whole expression gets integer value.");
 					}
 					else
 					{
 						throw new InvalidSequenceTokenException("Missing operand.");
 					}
 				}
 				else if ( ( arg0.getTokenType() == TokenType.TriangleFunc || arg0.getTokenType() == TokenType.BaseConvFunc )
 						|| arg0.getTokenType() == TokenType.MathematFunc )
 				{
 					pstu_root.setNode( arg0 );
 					TokenUnit zero = new TokenUnit ( TokenType.Integer, TokenSubtype.Decimal, "0" );
 					pstu_root.setLeftLeapNode( zero );
 					arg1 = sArray.getToken(i++);
 					
 					TokenArray subArray2 = new TokenArray();
 					int skipPairofParentheses = 0;
 					
 					while ( true )
 					{
 						TokenUnit temp = new TokenUnit();
 						temp = sArray.getToken(i);
 						
 						if ( temp.getTokenSubtype() == TokenSubtype.Left_Parenthesis )
 							skipPairofParentheses++;
 						if ( temp.getTokenSubtype() == TokenSubtype.Righ_Parenthesis )
 							skipPairofParentheses--;
 						
 						if ( skipPairofParentheses == -1 )
 						{
 							i++;
 							break;
 						}
 						
 						subArray2.addToken(temp);
 						i++;
 					}
 					
 					if ( subArray2.getSize() == 1 )
 					{
 						pstu_root.setRightLeapNode( subArray2.getToken( 0 ) );
 					}
 					else
 					{
 						pstu_root.addRightSubtree( generateParseTree ( subArray2 ) );
 					}
 				}
 				else
 				{
 					throw new InvalidSequenceTokenException("Missing operand.");
 				}
 			}
 			
 			// if ( i != 0 )
 			else 
 			{
 				
 				op = sArray.getToken(i++); // something operator
 				if ( op.getTokenType() != TokenType.Operatr )
 					throw new InvalidSequenceTokenException("Missing operator.");
 				
 				if ( i == size && op.getTokenSubtype() != TokenSubtype.Factorial )
 					throw new InvalidSequenceTokenException("Missing operand.");
 				
 				if ( op.getTokenSubtype() != TokenSubtype.Factorial )
 					arg1 = sArray.getToken(i++); // can be integer, floating point, left parenthesis, or function
 				else 
 				{
 					TokenUnit zero = new TokenUnit ( TokenType.Integer, TokenSubtype.Decimal, "0" );
 					arg1 = zero;
 				}
 				
 				if ( pstu_root.getNode().getTokenType() == TokenType.BaseConvFunc )
 				{
 					throw new InvalidSequenceTokenException("more tokens cannot be here.");
 				}
 				
 				// The most priority
 				if ( pstu_root.getNode().getTokenSubtype() == TokenSubtype.Factorial )
 				{
 					ParseTreeUnit newSubtree = new ParseTreeUnit();
 					
 					newSubtree.setNode(op);
 					newSubtree.addLeftSubtree(pstu_root);
 					
 					if ( arg1.getTokenType() == TokenType.Integer || arg1.getTokenType() == TokenType.FlPoint )
 					{
 						newSubtree.setRightLeapNode(arg1);
 						pstu_root = newSubtree;
 					}
 					else if ( arg1.getTokenType() == TokenType.Parents && arg1.getTokenSubtype() == TokenSubtype.Left_Parenthesis )
 					{
 						TokenArray subArray2 = new TokenArray();
 						int skipPairofParentheses = 0;
 						
 						while ( true )
 						{
 							TokenUnit temp = new TokenUnit();
 							temp = sArray.getToken(i);
 							
 							if ( temp.getTokenSubtype() == TokenSubtype.Left_Parenthesis )
 								skipPairofParentheses++;
 							if ( temp.getTokenSubtype() == TokenSubtype.Righ_Parenthesis )
 								skipPairofParentheses--;
 							
 							if ( skipPairofParentheses == -1 )
 							{
 								i++;
 								break;
 							}
 							
 							subArray2.addToken(temp);
 							i++;
 						}
 						
 						if ( subArray2.getSize() == 1 )
 						{
 							newSubtree.setRightLeapNode( subArray2.getToken( 0 ) );
 						}
 						else
 						{
 							newSubtree.addRightSubtree( generateParseTree( subArray2 ) );
 						}
 						
 						pstu_root = newSubtree;
 					}
 				}
 				else if ( ( ( pstu_root.getNode().getTokenSubtype() == TokenSubtype.Power || pstu_root.getNode().getTokenSubtype() == TokenSubtype.Modulus ) ||
 						( pstu_root.getNode().getTokenSubtype() == TokenSubtype.Times || pstu_root.getNode().getTokenSubtype() == TokenSubtype.Divide ) ) ||
 						( pstu_root.getNode().getTokenType() == TokenType.TriangleFunc || pstu_root.getNode().getTokenType() == TokenType.MathematFunc ) )
 				{
 					ParseTreeUnit newSubtree = new ParseTreeUnit();
 					
 
 					if ( op.getTokenSubtype() == TokenSubtype.Power || op.getTokenSubtype() == TokenSubtype.Factorial )
 					{
 						newSubtree.setNode( op );
 						newSubtree.addLeftSubtree( pstu_root.getRightSubtree() );
 						
 						if ( arg1.getTokenType() == TokenType.Integer || arg1.getTokenType() == TokenType.FlPoint )
 						{
 							newSubtree.setRightLeapNode(arg1);
 							pstu_root.addRightSubtree(newSubtree);
 						}
 						else if ( arg1.getTokenType() == TokenType.Parents && arg1.getTokenSubtype() == TokenSubtype.Left_Parenthesis )
 						{
 							TokenArray subArray2 = new TokenArray();
 							int skipPairofParentheses = 0;
 							
 							while ( true )
 							{
 								TokenUnit temp = new TokenUnit();
 								temp = sArray.getToken(i);
 								
 								if ( temp.getTokenSubtype() == TokenSubtype.Left_Parenthesis )
 									skipPairofParentheses++;
 								if ( temp.getTokenSubtype() == TokenSubtype.Righ_Parenthesis )
 									skipPairofParentheses--;
 								
 								if ( skipPairofParentheses == -1 )
 								{
 									i++;
 									break;
 								}
 								
 								subArray2.addToken(temp);
 								i++;
 							}
 							
 							if ( subArray2.getSize() == 1 )
 							{
 								newSubtree.setRightLeapNode( subArray2.getToken( 0 ) );
 							}
 							else
 							{
 								newSubtree.addRightSubtree( generateParseTree( subArray2 ) );
 							}
 						}
 						else if ( ( arg1.getTokenType() == TokenType.TriangleFunc || arg1.getTokenType() == TokenType.BaseConvFunc )
 								|| arg1.getTokenType() == TokenType.MathematFunc )
 						{
 							TokenUnit zero = new TokenUnit ( TokenType.Integer, TokenSubtype.Decimal, "0" );
 							ParseTreeUnit newSubtree2 = new ParseTreeUnit();
 							
 							newSubtree2.setNode(arg1); // triangle function
 							newSubtree2.setLeftLeapNode(zero);
 							
 							arg1 = sArray.getToken(i++);
 							
 							if ( arg1.getTokenType() != TokenType.Parents )
 							{
 								throw new InvalidSequenceTokenException("Left parenthesis is expected.");
 							}
 							
 							TokenArray subArray2 = new TokenArray();
 							int skipPairofParentheses = 0;
 							
 							while ( true )
 							{
 								TokenUnit temp = new TokenUnit();
 								temp = sArray.getToken(i);
 								
 								if ( temp.getTokenSubtype() == TokenSubtype.Left_Parenthesis )
 									skipPairofParentheses++;
 								if ( temp.getTokenSubtype() == TokenSubtype.Righ_Parenthesis )
 									skipPairofParentheses--;
 								
 								if ( skipPairofParentheses == -1 )
 								{
 									i++;
 									break;
 								}
 								
 								subArray2.addToken(temp);
 								i++;
 							}
 							
 							if ( subArray2.getSize() == 1 )
 							{
 								newSubtree2.setRightLeapNode( subArray2.getToken( 0 ) );
 							}
 							else
 							{
 								newSubtree2.addRightSubtree( generateParseTree( subArray2 ) );
 							}
 							
 							newSubtree.addRightSubtree(newSubtree2);
 						}
 						else
 						{
 							throw new InvalidSequenceTokenException("Missing operand.");
 						}
 					}
 					else
 					{
 						newSubtree.setNode( op );
 						newSubtree.addLeftSubtree( pstu_root );
 						
 						if ( arg1.getTokenType() == TokenType.Integer || arg1.getTokenType() == TokenType.FlPoint )
 						{
 							newSubtree.setRightLeapNode(arg1);
 							pstu_root = newSubtree;
 						}
 						else if ( arg1.getTokenType() == TokenType.Parents || arg1.getTokenSubtype() == TokenSubtype.Left_Parenthesis )
 						{
 							TokenArray subArray2 = new TokenArray();
 							int skipPairofParentheses = 0;
 							
 							while ( true )
 							{
 								TokenUnit temp = new TokenUnit();
 								temp = sArray.getToken(i);
 								
 								if ( temp.getTokenSubtype() == TokenSubtype.Left_Parenthesis )
 									skipPairofParentheses++;
 								if ( temp.getTokenSubtype() == TokenSubtype.Righ_Parenthesis )
 									skipPairofParentheses--;
 								
 								if ( skipPairofParentheses == -1 )
 								{
 									i++;
 									break;
 								}
 								
 								subArray2.addToken(temp);
 								i++;
 							}
 							
 							if ( subArray2.getSize() == 1 )
 							{
 								newSubtree.setRightLeapNode( subArray2.getToken( 0 ) );
 							}
 							else
 							{
 								newSubtree.addRightSubtree( generateParseTree( subArray2 ) );
 							}
 							
 							pstu_root = newSubtree;
 						}
 						else if ( arg1.getTokenType() == TokenType.TriangleFunc || arg1.getTokenType() == TokenType.MathematFunc )
 						{
 							TokenUnit zero = new TokenUnit ( TokenType.Integer, TokenSubtype.Decimal, "0" );
 							ParseTreeUnit newSubtree2 = new ParseTreeUnit();
 							
 							newSubtree2.setNode(arg1); // triangle function
 							newSubtree2.setLeftLeapNode(zero);
 							
 							arg1 = sArray.getToken(i++);
 							
 							if ( arg1.getTokenType() != TokenType.Parents )
 							{
 								throw new InvalidSequenceTokenException("Left parenthesis is expected.");
 							}
 							
 							TokenArray subArray2 = new TokenArray();
 							int skipPairofParentheses = 0;
 							
 							while ( true )
 							{
 								TokenUnit temp = new TokenUnit();
 								temp = sArray.getToken(i);
 								
 								if ( temp.getTokenSubtype() == TokenSubtype.Left_Parenthesis )
 									skipPairofParentheses++;
 								if ( temp.getTokenSubtype() == TokenSubtype.Righ_Parenthesis )
 									skipPairofParentheses--;
 								
 								if ( skipPairofParentheses == -1 )
 								{
 									i++;
 									break;
 								}
 								
 								subArray2.addToken(temp);
 								i++;
 							}
 							
 							if ( subArray2.getSize() == 1 )
 							{
 								newSubtree2.setRightLeapNode( subArray2.getToken( 0 ) );
 							}
 							else
 							{
 								newSubtree2.addRightSubtree( generateParseTree( subArray2 ) );
 							}
 							
 							newSubtree.addRightSubtree(newSubtree2);
 							pstu_root = newSubtree;
 						}
 						else if ( arg1.getTokenType() == TokenType.BaseConvFunc )
 							throw new InvalidSequenceTokenException("Base conversion function can be used for whole expression gets integer value.");
 						else
 						{
 							throw new InvalidSequenceTokenException("Missing operand.");
 						}
 					}
 				}
 				
 				// The least priority
 				else if ( pstu_root.getNode().getTokenSubtype() == TokenSubtype.Plus || pstu_root.getNode().getTokenSubtype() == TokenSubtype.Minus )
 				{
 					ParseTreeUnit newSubtree = new ParseTreeUnit();
 					
 					
 					if ( ( op.getTokenSubtype() == TokenSubtype.Power || op.getTokenSubtype() == TokenSubtype.Modulus ) ||
 							( op.getTokenSubtype() == TokenSubtype.Times || op.getTokenSubtype() == TokenSubtype.Divide ) ||
 							op.getTokenSubtype() == TokenSubtype.Factorial )
 					{
 						newSubtree.setNode( op );
 						newSubtree.addLeftSubtree(pstu_root.getRightSubtree());
 						
 						if ( arg1.getTokenType() == TokenType.Integer || arg1.getTokenType() == TokenType.FlPoint )
 						{
 							newSubtree.setRightLeapNode(arg1);
 							pstu_root.addRightSubtree(newSubtree);
 						}
 						else if ( arg1.getTokenType() == TokenType.Parents || arg1.getTokenSubtype() == TokenSubtype.Left_Parenthesis )
 						{
 							TokenArray subArray2 = new TokenArray();
 							int skipPairofParentheses = 0;
 							
 							while ( true )
 							{
 								TokenUnit temp = new TokenUnit();
 								temp = sArray.getToken(i);
 								
 								if ( temp.getTokenSubtype() == TokenSubtype.Left_Parenthesis )
 									skipPairofParentheses++;
 								if ( temp.getTokenSubtype() == TokenSubtype.Righ_Parenthesis )
 									skipPairofParentheses--;
 								
 								if ( skipPairofParentheses == -1 )
 								{
 									i++;
 									break;
 								}
 								
 								subArray2.addToken(temp);
 								i++;
 							}
 							
 							if ( subArray2.getSize() == 1 )
 							{
 								newSubtree.setRightLeapNode( subArray2.getToken ( 0 ) );
 							}
 							else
 							{
 								newSubtree.addRightSubtree( generateParseTree( subArray2 ) );
 							}
 						}
 						else if ( arg1.getTokenType() == TokenType.TriangleFunc || arg1.getTokenType() == TokenType.MathematFunc )
 						{
 							TokenUnit zero = new TokenUnit ( TokenType.Integer, TokenSubtype.Decimal, "0" );
 							ParseTreeUnit newSubtree2 = new ParseTreeUnit();
 							
 							newSubtree2.setNode(arg1); // triangle function
 							newSubtree2.setLeftLeapNode(zero);
 							
 							arg1 = sArray.getToken(i++);
 							
 							if ( arg1.getTokenType() != TokenType.Parents )
 							{
 								throw new InvalidSequenceTokenException("Left parenthesis is expected.");
 							}
 							
 							TokenArray subArray2 = new TokenArray();
 							int skipPairofParentheses = 0;
 							
 							while ( true )
 							{
 								TokenUnit temp = new TokenUnit();
 								temp = sArray.getToken(i);
 								
 								if ( temp.getTokenSubtype() == TokenSubtype.Left_Parenthesis )
 									skipPairofParentheses++;
 								if ( temp.getTokenSubtype() == TokenSubtype.Righ_Parenthesis )
 									skipPairofParentheses--;
 								
 								if ( skipPairofParentheses == -1 )
 								{
 									i++;
 									break;
 								}
 								
 								subArray2.addToken(temp);
 								i++;
 							}
 							
 							if ( subArray2.getSize() == 1 )
 							{
 								newSubtree2.setRightLeapNode( subArray2.getToken( 0 ) );
 							}
 							else
 							{
 								newSubtree2.addRightSubtree( generateParseTree( subArray2 ) );
 							}
 							
 							newSubtree.addRightSubtree(newSubtree2);
 						}
 						else if ( arg1.getTokenType() == TokenType.BaseConvFunc )
 						{
 							throw new InvalidSequenceTokenException("Base conversion function can be used for whole expression gets integer value.");
 						}
 						else
 						{
 							throw new InvalidSequenceTokenException("Missing operand.");
 						}
 					}
 					else
 					{
 						newSubtree.setNode( op );
 						newSubtree.addLeftSubtree( pstu_root );
 						
 						if ( arg1.getTokenType() == TokenType.Integer || arg1.getTokenType() == TokenType.FlPoint )
 						{
 							newSubtree.setRightLeapNode(arg1);
 							pstu_root = newSubtree;
 						}
 						else if ( arg1.getTokenType() == TokenType.Parents || arg1.getTokenSubtype() == TokenSubtype.Left_Parenthesis )
 						{
 							TokenArray subArray2 = new TokenArray();
 							int skipPairofParentheses = 0;
 							
 							while ( true )
 							{
 								TokenUnit temp = new TokenUnit();
 								temp = sArray.getToken(i);
 								
 								if ( temp.getTokenSubtype() == TokenSubtype.Left_Parenthesis )
 									skipPairofParentheses++;
 								if ( temp.getTokenSubtype() == TokenSubtype.Righ_Parenthesis )
 									skipPairofParentheses--;
 								
 								if ( skipPairofParentheses == -1 )
 								{
 									i++;
 									break;
 								}
 								
 								subArray2.addToken(temp);
 								i++;
 							}
 							
 							if ( subArray2.getSize() == 1 )
 							{
 								newSubtree.setRightLeapNode( subArray2.getToken ( 0 ) );
 							}
 							else
 							{
 								newSubtree.addRightSubtree( generateParseTree( subArray2 ) );
 							}
 							pstu_root = newSubtree;
 						}
 						else if ( arg1.getTokenType() == TokenType.TriangleFunc || arg1.getTokenType() == TokenType.MathematFunc )
 						{
 							TokenUnit zero = new TokenUnit ( TokenType.Integer, TokenSubtype.Decimal, "0" );
 							ParseTreeUnit newSubtree2 = new ParseTreeUnit();
 							
 							newSubtree2.setNode(arg1); // triangle function
 							newSubtree2.setLeftLeapNode(zero);
 							
 							arg1 = sArray.getToken(i++);
 							
 							if ( arg1.getTokenType() != TokenType.Parents )
 							{
 								throw new InvalidSequenceTokenException("Left parenthesis is expected.");
 							}
 							
 							TokenArray subArray2 = new TokenArray();
 							int skipPairofParentheses = 0;
 							
 							while ( true )
 							{
 								TokenUnit temp = new TokenUnit();
 								temp = sArray.getToken(i);
 								
 								if ( temp.getTokenSubtype() == TokenSubtype.Left_Parenthesis )
 									skipPairofParentheses++;
 								if ( temp.getTokenSubtype() == TokenSubtype.Righ_Parenthesis )
 									skipPairofParentheses--;
 								
 								if ( skipPairofParentheses == -1 )
 								{
 									i++;
 									break;
 								}
 								
 								subArray2.addToken(temp);
 								i++;
 							}
 							
 							if ( subArray2.getSize() == 1 )
 							{
 								newSubtree2.setRightLeapNode( subArray2.getToken( 0 ) );
 							}
 							else
 							{
 								newSubtree2.addRightSubtree( generateParseTree( subArray2 ) );
 							}
 							
 							newSubtree.addRightSubtree(newSubtree2);
 							pstu_root = newSubtree;
 						}
 						else if ( arg1.getTokenType() == TokenType.BaseConvFunc )
 						{
 							throw new InvalidSequenceTokenException("Base conversion function can be used for whole expression gets integer value.");
 						}
 						else
 						{
 							throw new InvalidSequenceTokenException("Missing operand.");
 						}
 					}
 				}
 			}
 		}
 		
 		return pstu_root;
 	}	
 }
