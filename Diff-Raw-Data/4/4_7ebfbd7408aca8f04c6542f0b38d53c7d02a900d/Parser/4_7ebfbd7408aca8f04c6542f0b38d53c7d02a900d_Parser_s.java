 package togos.schemaschema.parser;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import togos.lang.BaseSourceLocation;
 import togos.lang.ParseError;
 import togos.lang.SourceLocation;
 import togos.schemaschema.parser.ast.ASTNode;
 import togos.schemaschema.parser.ast.Block;
 import togos.schemaschema.parser.ast.Command;
 import togos.schemaschema.parser.ast.Parameterized;
 import togos.schemaschema.parser.ast.Phrase;
 import togos.schemaschema.parser.ast.Word;
 import togos.schemaschema.parser.asyncstream.BaseStreamSource;
 import togos.schemaschema.parser.asyncstream.Collector;
 import togos.schemaschema.parser.asyncstream.StreamDestination;
 
 @SuppressWarnings("incomplete-switch")
 public class Parser extends BaseStreamSource<Command> implements StreamDestination<Token>
 {
 	static class UnexpectedWord extends ParseError {
 		private static final long serialVersionUID = 2664614368054031256L;
 		public UnexpectedWord( Token t ) {
 			super("Unexpected word: '"+t.text+"'", t);
 		}
 	}
 	
 	static class UnexpectedSymbol extends ParseError {
 		private static final long serialVersionUID = 3440650069785927790L;
 		public UnexpectedSymbol( Token t ) {
 			super("Unexpected symbol: '"+t.text+"'", t);
 		}
 	}
 	
 	static class UnexpectedEndError extends ParseError {
 		private static final long serialVersionUID = 2386975431485359225L;
 		public UnexpectedEndError( SourceLocation sLoc ) {
 			super( "Unexpected end of file while parsing expression", sLoc );
 		}
 	}
 	
 	static class UnexpectedStateException extends RuntimeException {
 		private static final long serialVersionUID = 2386975431485359225L;
 		public UnexpectedStateException( Enum<?> state ) {
 			super( "Unexpected parser state: '"+state.name()+"'" );
 		}
 	}
 	
 	static ParseError unexpectedTokenError( Token t ) {
 		return (t.type == Token.Type.SYMBOL) ? new UnexpectedSymbol(t) : new UnexpectedWord(t); 
 	}
 	
 	interface ParseState {
 		public ParseState token( Token t ) throws Exception;
 		public ParseState end() throws Exception;
 	}
 	
 	interface CommandListParseState extends ParseState {
 		public ParseState command( Command c ) throws Exception;
 	}
 	
 	interface ParameterizedListParseState extends ParseState {
 		public ParseState parameterized( Parameterized p ) throws Exception;
 	}
 	
 	interface PhraseListParseState extends ParseState {
 		public ParseState phrase( Phrase p ) throws Exception;
 	}
 	
 	interface BlockListParseState extends ParseState {
 		public ParseState block( Block b ) throws Exception;
 	}
 	
 	static class PhraseParseState implements ParseState {
 		protected final PhraseListParseState parent;
 		protected final ArrayList<Word> words = new ArrayList<Word>();
 		protected SourceLocation firstSLoc = BaseSourceLocation.NONE;
 
 		public PhraseParseState( PhraseListParseState parent ) {
 			this.parent = parent;
 		}
 		
 		Phrase toPhrase() {
 			return new Phrase( words.toArray( new Word[words.size()] ), firstSLoc );
 		}
 		
 		@Override public ParseState end() throws Exception {
 			parent.phrase( toPhrase() );
 			return parent.end();
 		}
 		
 		@Override public ParseState token( Token t ) throws Exception {
 			if( firstSLoc == BaseSourceLocation.NONE ) firstSLoc = t;
 			switch( t.type ) {
 			case SYMBOL:
 				return parent.phrase( toPhrase() ).token(t);
 			default:
 				words.add( new Word(t.text, t) );
 				return this;
 			}
 		}
 	}
 	
 	static class ParameterizedParseState implements ParseState, PhraseListParseState, ParameterizedListParseState {
 		enum State { NEW, SUBJECT, PARAMS, DONE }
 		protected final ParameterizedListParseState parent;
 		State state = State.NEW;
 		Phrase subject = null;
 		List<Parameterized> parameters = new ArrayList<Parameterized>();
 		protected SourceLocation firstSLoc = BaseSourceLocation.NONE;
 		
 		public ParameterizedParseState( ParameterizedListParseState parent ) {
 			this.parent = parent;
 		}
 		
 		public Parameterized toParameterized() throws ParseError {
 			if( subject == null ) {
 				throw new ParseError("Parameterized expression has no subject", firstSLoc);
 			}
 			return new Parameterized( subject, parameters.toArray(new Parameterized[parameters.size()]), firstSLoc );
 		}
 		
 		@Override public ParseState token(Token t) throws Exception {
 			switch( state ) {
 			case NEW:
 				state = State.SUBJECT;
 				return new PhraseParseState( this ).token(t);
 			case SUBJECT:
 				switch( t.type ) {
 				case SYMBOL:
 					if( "(".equals(t.text) ) {
 						state = State.PARAMS;
 						return new ParameterizedParseState(this);
 					} else {
 						return parent.parameterized( toParameterized() ).token(t);
 					}
 				}
 				throw unexpectedTokenError(t);
 			case PARAMS:
 				switch( t.type ) {
 				case SYMBOL:
 					if( ",".equals(t.text) ) {
 						return new ParameterizedParseState(this);
 					} else if( ")".equals(t.text) ) {
 						this.state = State.DONE;
 						return parent.parameterized( toParameterized() );
 					}
 				}
 				throw unexpectedTokenError(t);
 			default:
 				throw new UnexpectedStateException(state);
 			}
 			
 		}
 		
 		@Override public ParseState phrase( Phrase phrase ) throws Exception {
 			if( this.state != State.SUBJECT ) throw new UnexpectedStateException(state);
 			if( this.subject != null ) {
 				throw new RuntimeException("Parameterized already has a subject");
 			}
 			this.subject = phrase;
 			return this;
 		}
 		
 		@Override public ParseState parameterized( Parameterized param ) throws Exception {
 			if( state != State.PARAMS ) throw new UnexpectedStateException(state);
 			this.parameters.add( param );
 			return this;
 		}
 		
 		@Override public ParseState end() throws Exception {
 			if( state == State.PARAMS ) throw new UnexpectedEndError(firstSLoc);
 			return parent.parameterized( toParameterized() ).end();
 		}
 	}
 	
 	static class BlockParseState implements CommandListParseState {
 		protected final BlockListParseState parent;
 		List<Command> commands = new ArrayList<Command>();
 		protected SourceLocation firstSLoc = BaseSourceLocation.NONE;
 		
 		public BlockParseState( BlockListParseState parent ) {
 			this.parent = parent;
 		}
 		
 		protected Block toBlock() {
 			return new Block( commands.toArray(new Command[commands.size()]), firstSLoc );
 		}
 		
 		@Override public ParseState token(Token t) throws Exception {
 			if( firstSLoc == BaseSourceLocation.NONE ) firstSLoc = t;
 			
 			switch( t.type ) {
 			case SYMBOL:
 				if( "}".equals(t.text) ) {
 					return parent.block( toBlock() ).token(t);
 				} else if( "\n".equals(t.text) ) {
 					return this;
 				}
 			default:
 				return new CommandParseState(this).token(t);
 			}
 		}
 		
 		@Override public ParseState command(Command c) throws Exception {
 			commands.add(c);
 			return this;
 		}
 		
 		@Override public ParseState end() throws Exception {
 			return parent.block( toBlock() ).end();
 		}
 	}
 	
 	static class CommandParseState implements ParseState, ParameterizedListParseState, BlockListParseState, CommandListParseState {
 		enum State { NEW, SUBJECT, MODIFIERS, BODY, SINGLE_COMMAND, DONE }
 		protected final CommandListParseState parent;
 		protected Parameterized subject = null;
 		protected List<Parameterized> modifiers = new ArrayList<Parameterized>();
 		protected Block body = Block.EMPTY;
 		protected SourceLocation firstSLoc = BaseSourceLocation.NONE;
 		protected State state = State.NEW;
 		
 		public CommandParseState( CommandListParseState parent ) {
 			this.parent = parent;
 		}
 		
 		protected Command toCommand() {
 			return new Command( subject, modifiers.toArray(new Parameterized[modifiers.size()]), body, firstSLoc );
 		}
 		
 		@Override public ParseState token(Token t) throws Exception {
 			if( firstSLoc == BaseSourceLocation.NONE ) firstSLoc = t;
 			
 			switch( state ) {
 			case NEW:
 				state = State.SUBJECT;
 				return new ParameterizedParseState( this ).token(t);
 			case SUBJECT:
 				switch( t.type ) {
 				case SYMBOL:
 					if( ":".equals(t.text) ) {
 						state = State.MODIFIERS;
 						return new ParameterizedParseState(this);
 					} else if( "\n".equals(t.text) ) {
 						return parent.command( toCommand() ).token(t);
 					} else if( "{".equals(t.text) ) {
 						state = State.BODY;
 						return new BlockParseState(this);
 					} else if( "=".equals(t.text) ) {
 						state = State.SINGLE_COMMAND;
 						return new CommandParseState(this);
 					}
 				}
 				throw unexpectedTokenError(t);
 			case MODIFIERS:
 				switch( t.type ) {
 				case SYMBOL:
 					if( ":".equals(t.text) ) {
 						state = State.MODIFIERS;
 						return new ParameterizedParseState(this);
 					} else if( "\n".equals(t.text) ) {
 						return parent.command( toCommand() ).token(t);
 					} else if( "{".equals(t.text) ) {
 						state = State.BODY;
 						return new BlockParseState(this);
 					} else if( "=".equals(t.text) ) {
 						state = State.SINGLE_COMMAND;
 						return new CommandParseState(this);
 					}
 				}
 				throw unexpectedTokenError(t);
 			case BODY:
 				switch( t.type ) {
 				case SYMBOL:
 					if( "}".equals(t.text) ) {
 						this.state = State.DONE;
 						return this;
 					}
 				}
 				throw unexpectedTokenError(t);
 			case DONE:
 				switch( t.type ) {
 				case SYMBOL:
 					if( "\n".equals(t.text) || "}".equals(t.text) || ")".equals(t.text) || ";".equals(t.text) ) {
 						return parent.command( toCommand() ).token(t);
 					}
 				}
 				throw unexpectedTokenError(t);
 			default:
 				throw new UnexpectedStateException(state);
 			}
 		}
 		
 		@Override public ParseState parameterized(Parameterized pize) throws Exception {
 			switch( state ) {
 			case SUBJECT:
 				subject = pize;
 				return this;
 			case MODIFIERS:
 				modifiers.add(pize);
 				return this;
 			}
 			throw new UnexpectedStateException(state);
 		}
 		
 		@Override public ParseState block(Block block) throws Exception {
 			if( state != State.BODY ) throw new UnexpectedStateException(state);
 			this.body = block;
 			return this;
 		}
 		
 		@Override public ParseState end() throws Exception {
 			switch( state ) {
 			case BODY:
 				throw new UnexpectedEndError( firstSLoc );
 			}
 			return parent.command( toCommand() ).end(); 
 		}
 		
 		@Override public ParseState command(Command command) throws Exception {
 			if( state != State.SINGLE_COMMAND ) throw new UnexpectedStateException(state);
 			this.body = new Block( new Command[]{ command }, command.sLoc );
 			this.state = State.DONE;
 			return this;
 		}
 	}
 	
 	class RootParseState implements CommandListParseState {
 		@Override public ParseState token(Token t) throws Exception {
 			switch( t.type ) {
 			case SYMBOL:
 				if( "\n".equals(t.text) ) {
 					return this;
 				} else {
 					throw unexpectedTokenError(t);
 				}
 			default:
 				return new CommandParseState( this ).token( t );
 			}
 		}
 		
 		@Override public ParseState end() throws Exception {
 			_end();
 			return this;
 		}
 		
 		public ParseState command( Command c ) throws Exception {
 			_data( c );
 			return this;
 		}
 	}
 	
 	ParseState parseState = new RootParseState();
 	
 	@Override public void data( Token t ) throws Exception {
 		parseState = parseState.token( t );
 	}
 
 	@Override public void end() throws Exception {
 		parseState = parseState.end();
 	}	
 	
 	public static ASTNode parseCommand( String commandSource, SourceLocation sLoc ) throws Exception {
 		Tokenizer t = new Tokenizer();
 		t.setSourceLocation( sLoc );
 		Parser p = new Parser();
 		ArrayList<ASTNode> destList = new ArrayList<ASTNode>();
 		Collector<ASTNode> collector = new Collector<ASTNode>(destList);
 		p.pipe( collector );
 		t.pipe( p );
 		t.data( commandSource.toCharArray() );
 		t.end();
 		if( destList.size() == 0 ) {
 			throw new ParseError("Script contains no nodes", sLoc );
 		} else if( destList.size() > 1 ) {
 			throw new ParseError("Script contains more than one root node", destList.get(1).sLoc ); 
 		} else {
 			return destList.get(0);
 		}
 	}
 }
