 /**
  *
  * Breakthrough Game
  *
  * Skeleton by Yngvi Björnsson
  *
  */
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.StringTokenizer;
 
 public class Breakthrough {
 
     final static int MAX_ROWS = 8;
     final static int MAX_COLS = 8;
 
     public static void main( String[] args )
     {
 
         System.out.println( "Welcome to Breakthrough! ('h' for help)" );
 
         State            state             = new BreakthroughState( MAX_ROWS, MAX_COLS );
         Agent            agents[]          = { new AgentAlphaBeta(), new AgentDiscovery() };
         Game             game              = new Game( new BreakthroughState( (BreakthroughState) state ) );
         long             maxSearchLimit    = 0;     // 0 = limit disabled.
         long             maxSearchTimeMsec = 1000;  // 0 = limit disabled.
         long             maxSearchNodes    = 0;
         boolean          silenceAgent      = false;
         boolean          silenceDisplay    = false;
 
         agents[0].setSilence( silenceAgent );
         agents[1].setSilence(silenceAgent);
         agents[0].setThinklimit( maxSearchLimit, maxSearchNodes, maxSearchTimeMsec );
         agents[1].setThinklimit( maxSearchLimit, maxSearchNodes, maxSearchTimeMsec );
 
         try {
 
             BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 
             if ( !silenceDisplay ) { state.display(); }
 
             // Command loop.
             System.out.print("> ");
             String line = br.readLine();
             while ( line != null ) {
                 StringTokenizer st = new StringTokenizer( line );
                 if ( st.hasMoreTokens() ) {
                     String command = st.nextToken();
 
                     if ( command.equals("h") ) {
                         // Print help menu.
                         System.out.println("Options:");
                         System.out.println("  i [<fen>] : Initialize, new game (with startup position <fen>).");
                         System.out.println("  m <c>     : Make a move (e.g. 'm a2-b3').");
                         System.out.println("  r         : Retract last move.");
                         System.out.println("  d [on/off]: Display board (on/off always/never display).");
                         System.out.println("  l <n>     : Set maximum search limit (depth/simulation count).");
                         System.out.println("  t <n>     : Set maximum search time (msec.).");
                         System.out.println("  n <n>     : Set maximum search nodes.");
                         System.out.println("  v         : Toogle verbose mode on/off.");
                         System.out.println("  g         : Go, thinking! Find and play (the best) move.");
                         System.out.println("  a <n>     : Autoplay a <n> game match pairs (alternating colors).");
                         System.out.println("  q         : Quit the program.");
                     }
                     else if ( command.equals("i") ) {
                         // Initialize to start position.
                         if ( st.hasMoreTokens() ) {
                             String board = st.nextToken();
                             String toMove = "";
                             if ( st.hasMoreTokens() ) {
                                 toMove = st.nextToken();
                             }
                             String fen = board + " "  + toMove;
                             if ( !state.setup( fen ) ) {
                                 System.out.println( " => Input error, setup state illegal.");
                             }
                         }
                         else {
                             state.reset();
                         }
                         game.reset( state );
                         state.display();
                     }
                     else if ( command.equals("m") ) {
                         // Make the provided move (if legal).
                         if ( st.hasMoreTokens() ) {
                             String strMove = st.nextToken();
                             Move move = state.isLegalMove( strMove );
                             if ( move != null ) {
                                 state.make( move );
                                 game.addMove( move );
                                 state.display();
                             }
                             else {
                                 System.out.print( " => State error, illegal move: '" + strMove + "'" );
                                 System.out.println( move );
                             }
                         }
                         else {
                             System.out.println( " => Input error, move missing.");
                         }
                     }
                     else if ( command.equals("r") ) {
                         // Retract a move.
                         Move lastMove = game.getLastMove();
                         if ( lastMove != null ) {
                             state.retract( lastMove );
                             game.removeLastMove();
                             state.display();
                         }
                     }
                     else if ( command.equals("l") ) {
                         // Set the nominal search depth/simulation-count.
                         if ( st.hasMoreTokens() ) {
                             try {
                                 maxSearchLimit = Long.valueOf(st.nextToken());
                                 System.out.println( "l:" + maxSearchLimit +
                                         " n:" + maxSearchNodes + " t:" + maxSearchTimeMsec );
                                 agents[0].setThinklimit( maxSearchLimit, maxSearchNodes, maxSearchTimeMsec );
                                 agents[1].setThinklimit( maxSearchLimit, maxSearchNodes, maxSearchTimeMsec );
                             } catch (NumberFormatException e ) {
                                 System.out.println( " => Input error, depth not an integer.");
                             }
                         }
                         else {
                             System.out.println( " => Input error, limit missing.");
                         }
                     }
                     else if ( command.equals("t") ) {
                         // Set the nominal search depth.
                         if ( st.hasMoreTokens() ) {
                             try {
                                 maxSearchTimeMsec = Long.valueOf(st.nextToken());
                                 System.out.println( "l:" + maxSearchLimit +
                                         " n:" + maxSearchNodes + " t:" + maxSearchTimeMsec );
                                 agents[0].setThinklimit( maxSearchLimit, maxSearchNodes, maxSearchTimeMsec );
                                 agents[1].setThinklimit( maxSearchLimit, maxSearchNodes, maxSearchTimeMsec );
                             } catch (NumberFormatException e ) {
                                 System.out.println( " => Input error, depth not an integer.");
                             }
                         }
                         else {
                             System.out.println( " => Input error, msec missing.");
                         }
                     }
                     else if ( command.equals("n") ) {
                         // Set the nominal search depth.
                         if ( st.hasMoreTokens() ) {
                             try {
                                 maxSearchNodes = Long.valueOf(st.nextToken());
                                 System.out.println( "l:" + maxSearchLimit +
                                         " n:" + maxSearchNodes + " t:" + maxSearchTimeMsec );
                                 agents[0].setThinklimit( maxSearchLimit, maxSearchNodes, maxSearchTimeMsec );
                                 agents[1].setThinklimit( maxSearchLimit, maxSearchNodes, maxSearchTimeMsec );
                             } catch (NumberFormatException e ) {
                                 System.out.println( " => Input error, depth not an integer.");
                             }
                         }
                         else {
                             System.out.println( " => Input error, nodes missing.");
                         }
                     }
                     else if ( command.equals("d") ) {
                         if ( st.hasMoreTokens() ) {
                             String str = st.nextToken();
                             if ( str.equals( "on") ) {
                                 silenceDisplay = false;
                             }
                             else if ( str.equals( "off") ) {
                                 silenceDisplay = true;
                             }
                         }
                         else {
                             state.display();
                         }
                     }
                     else if ( command.equals("v") ) {
                         silenceAgent = !silenceAgent;
                         agents[0].setSilence( silenceAgent );
                         agents[1].setSilence( silenceAgent );
                     }
                     else if ( command.equals("g") ) {
                         // Search to find the best move, then play it.
                         if ( !state.isTerminal() ) {
                             Move move = agents[state.getPlayerToMove()].playMove(state, game);
                             if ( move != null ) {
                                 state.make( move );
                                 game.addMove( move );
                                 System.out.print( "bestmove " );
                                 System.out.println( move.toStr() );
                                 if ( !silenceDisplay ) { state.display(); }
                                 if ( state.isTerminal() ) {
                                     System.out.print( "gameover ");
                                     State.Result result = state.getResult();
                                     if ( result == State.Result.Win ) {
                                         if ( state.getPlayerToMove() == 0 ) {
                                             System.out.println(  "1-0" );
                                         }
                                         else {
                                             System.out.println(  "0-1" );
                                         }
                                     }
                                     else if ( result == State.Result.Loss ) {
                                         if ( state.getPlayerToMove() == 0 ) {
                                             System.out.println(  "0-1" );
                                         }
                                         else {
                                             System.out.println(  "1-0" );
                                         }                                    }
                                     else {
                                         System.out.println("Unknown");
                                     }
                                 }
                             }
                             else {
                                 System.out.println( " => Search error, returned illegal move." );
                             }
                         }
 
                     }
                     else if ( command.equals("a") ) {
                         // Autoplay a <n> game match.
                         if ( st.hasMoreTokens() ) {
                             try {
                                 int maxGamePairs = Integer.valueOf(st.nextToken());
                                 int[] outcome = {0, 0};
                                 for ( int n=0 ; n < maxGamePairs; ++n ) {
                                     playAMatch(agents, 0, state, game, outcome);
                                     System.out.println( "outcome: " + outcome[0] + " - " + outcome[1] );
                                     playAMatch(agents, 1, state, game, outcome);
                                     System.out.println( "outcome: " + outcome[0] + " - " + outcome[1] );
                                 }
                                 System.out.println( "total: " + outcome[0] + " - " + outcome[1] );
                             } catch (NumberFormatException e ) {
                                 System.out.println( " => Input error, number of games not an integer.");
                             }
                         }
                         else {
                             System.out.println( " => Input error, number of games missing.");
                         }
                     }
 
                     else if ( command.equals("q") ) {
                         // Break out of loop and quit program.
                         break;
                     }
                     else {
                         System.out.println(" => Unknown command.");
                     }
 
                 }
                 System.out.print("> ");
                 line = br.readLine();
             }
 
         } catch (IOException ioe) {
             System.out.println("IO error reading standard input.");
             System.exit(1);
         }
     }
 
 
     private static void playAMatch( Agent agents[], int goesFirst, State state, Game game, int [] outcome )
     {
         int toMove = goesFirst;
         state.reset();
         game.reset( state );
         while ( !state.isTerminal() ) {
            System.out.println("To move: " + agents[toMove].getName());
             Agent agent = agents[ toMove ];
             Move move = agent.playMove( state, game );
             state.make( move );
             game.addMove( move );
             //System.out.print( "bestmove " );
             //System.out.println( move.toStr() );
             toMove = ( toMove==0 ? 1 : 0 );
         }
         int lastMove = (toMove == 0) ? 1 : 0;
         if ( state.getResult() == State.Result.Win ) {
             outcome[ toMove ] += 1;
         }
         else if ( state.getResult() == State.Result.Loss ) {
             outcome[ lastMove ] += 1;
         }
     }
 }
