 package conn4;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.CommandResult;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 import com.mongodb.Mongo;
 import com.mongodb.MongoURI;
 import com.mongodb.WriteConcern;
 import com.mongodb.WriteResult;
 import java.io.*;
 import java.util.*;
 
 import com.mongodb.hadoop.output.MongoUpdateKey;
 import org.apache.hadoop.conf.*;
 import org.apache.hadoop.io.*;
 import org.apache.hadoop.mapreduce.*;
 import org.apache.hadoop.mapreduce.Mapper.Context;
 import org.bson.*;
 
 import com.mongodb.hadoop.*;
 import com.mongodb.hadoop.io.BSONWritable;
 import com.mongodb.hadoop.util.*;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.hadoop.util.ToolRunner;
 
 /**
  * Created: Mar 24, 2011  12:53:40 PM
  *
  * @author Joseph Shraibman
  * @version $Revision: 1.1 $  $Date:  $ $Author: jks $
  */
 public class Connect4Generator extends MongoTool {
 
     
     private static final Log log = LogFactory.getLog( Connect4Generator.class );
     private final static String height_field_name = Connect4Generator.class.getName()+".height";
     private final static String POSSIBILITIES_KEY = "possibilities";
     private final static String BEST_MOVES_KEY = "bestmoves";
     private final static String DONE_KEY = "dn";
                               private final static String DONE_GEN_VALUE = "g";
                               private final static String DONE_BEST_MOVE_VALUE = "b";
 
         private final static BasicDBObject lose0 = new BasicDBObject("lose", 0);
         final BasicDBObject DOES_NOT_EXIST = new com.mongodb.BasicDBObject("$exists", Boolean.FALSE);
         final BasicDBObject DOES_EXIST = new com.mongodb.BasicDBObject("$exists", Boolean.TRUE);
     
     private enum CounterKeys{ ERROR };
     
     //input: boards of generation n-1
     //send child boards to reducer
     public static class BoardGenMapper extends Mapper<Object, BSONObject, Text, BSONWritable> {
         int height = -1;
         private final Text outputKey = new Text();
 
         @Override
         protected void map(Object key, BSONObject value, Context context) throws IOException, InterruptedException {
             BoardImp board = BoardImp. getBoard(height, value);
             final List<Board> allMoves = board.getAllMoves();
             if(allMoves.isEmpty() && ! board.isWinning())
                 log.error("Why doesn't this board have children?\n"+board);
             if (log.isDebugEnabled())
                 log.debug("board generated "+allMoves.size() +" child boards");
             for (Board childN : allMoves) {
                 outputKey.set(childN.toString());
                 context.write(outputKey, new BSONWritable(childN.toBSONObject() ));
             }
         }
         @Override
         protected void setup(Context context) throws IOException, InterruptedException {
             this.height = context.getConfiguration().getInt(height_field_name, -1);
             super.setup(context);
         }
         
     }
     public static class BoardGenReducer extends Reducer<Text, BSONWritable, String, BSONObject> {
         int height = -1;
 
         @Override
         protected void reduce(Text key, Iterable<BSONWritable> values, Context context) throws IOException, InterruptedException {
             //all the boards should be the same, so just get the first one
             BSONWritable bsonBoard = values.iterator().next();
             //write input boards to the database
             context.write( key.toString(), bsonBoard);
             if (bsonBoard.containsKey("wins")){
                 //update parents of winning boards here
                 BoardImp board = BoardImp.getBoard(height, bsonBoard);
                 
                 //In this example red wins
                 //update parents bestresult->lose0
                 //for (parent : board.getParents()) //parent is black's move
                 //   parent bestresult->lose0
                 //   for (grandparent : parent.getparents()) //red's move
                 //           bestresult->win1, bestresults[colidx]->win1
                 //         for (greatgrandparents) //black's move
                 //              bestresults[colidx]->lose2
                 //              do not update best result
                 if ("tie".equals(bsonBoard.get("wins")))
                      updateParentsOfTies(board.getAllParents(), context);
                 else
                     updateParentsOfWinningBoard(board.getAllParents(), context);
             }
         }
         private final static BasicDBObject win1 = new BasicDBObject("win", 1);
         private final static BasicDBObject lose1 = new BasicDBObject("lose", 1);
         
         //set {"lose" : 0} as best move of all parent boards
          private void updateParentsOfWinningBoard(List<BoardImp> parents,   Context context) throws IOException, InterruptedException {
             for (BoardImp board : parents) {
                 {
                     //The opposing player will lose no matter what he does in the previous turn
                     updateBoard(board, context, new BasicDBObject(BoardImp.BEST_RESULT_FIELD_NAME, lose0));
                 }
                 for (BoardImp grandparent : board.getAllParents()) {
                     {
                         //This player has a move that will force a win after his next move
                         final BasicDBObject toSet = new BasicDBObject(BoardImp.BEST_RESULT_FIELD_NAME, win1);
                         toSet.append(BoardImp.BEST_RESULTS_FIELD_NAME + "." + grandparent.getChildIsMove(), win1);
                         toSet.append(BoardImp.BEST_MOVE_FIELD_NAME, grandparent.getChildIsMove());
                         updateBoard(grandparent, context, toSet);
                     }
                     for (BoardImp greatGrandParent : grandparent.getAllParents()) {
                         final BasicDBObject toSet = new BasicDBObject(BoardImp.BEST_RESULTS_FIELD_NAME + "." + greatGrandParent.getChildIsMove(), lose1);
                         updateBoard(greatGrandParent, context, toSet);
                     }
                 }
             }
          }
           private void updateParentsOfTies(List<BoardImp> parents, Context context) throws IOException, InterruptedException {
               for (BoardImp board : parents) {
                   final BasicDBObject toSet = new BasicDBObject(BoardImp.BEST_RESULTS_FIELD_NAME + "." + board.getChildIsMove(), "tie");
                   updateBoard(board, context, toSet);
               }//for
           }
         @Override
         protected void setup(Context context) throws IOException, InterruptedException {
             this.height = context.getConfiguration().getInt(height_field_name, -1);
             super.setup(context);
         }
     }
     private static void updateBoard(BoardImp board, org.apache.hadoop.mapreduce.Reducer.Context context, BasicDBObject update)
             throws IOException, InterruptedException{
         final BasicDBObject val = new BasicDBObject("$set", update);
         context.write(MongoUpdateKey.getFor(board.toString()), val);
     }
 
     /** Query for all boards of generation N-1. For each of those, generate all its
      * children and write them to the database */
     private boolean generateBoardsForGeneration(int move) throws Exception{
         final Configuration conf = getConf();
         //NOTE: must do this BEFORE Job is created
         final MongoConfig mongo_conf = new MongoConfig(conf);
         {
             com.mongodb.BasicDBObject query = new com.mongodb.BasicDBObject();
             query.append( BoardImp.NUM_CHECKERS_FIELD_NAME, move-1 );
             query.append("wins", DOES_NOT_EXIST);
             mongo_conf.setQuery(query);
         }
         final Job job = new Job( conf , "conn4 generator move "+move);
         
         job.setMapperClass( BoardGenMapper.class );
         job.setReducerClass( BoardGenReducer.class );
         
         job.setJarByClass(this.getClass());
         
         job.setOutputKeyClass( String.class );
         job.setOutputValueClass( BSONObject.class );
         job.setMapOutputKeyClass( Text.class );
         job.setMapOutputValueClass( BSONWritable.class );
         
         job.setInputFormatClass( MongoInputFormat.class );
         job.setOutputFormatClass( MongoOutputFormat.class );
         
         return job.waitForCompletion(true);
     }
     /** All the work is done in the reducing phase, so this is basically an identity mapper */
     public static class BestMoveMapper extends Mapper<String, BSONObject, Text, BSONWritable> {
         
         @Override
         protected void map(String key, BSONObject value, Context context) throws IOException, InterruptedException {
             context.write(new Text(key) ,  new BSONWritable((BSONObject) value));
         }
     }
     public static class BestMoveReducer  extends Reducer<Text, BSONWritable, String, BSONObject> {
         int height = -1;
         @Override
         protected void reduce(Text key, Iterable<BSONWritable> values, Context context) throws IOException, InterruptedException {
             try{
                 //At this point we know what all the best move values are for the next generation, so we can set
                 //the best move values for this generation.
                 //There should be only one board in values
                 BSONObject bsonBoard = values.iterator().next();
                 BoardImp board = BoardImp.getBoard(height, bsonBoard);
                 BasicBSONObject bestMoves = (BasicBSONObject) bsonBoard.get(BoardImp.BEST_RESULTS_FIELD_NAME);
                 if (bestMoves == null){
                     log.warn("bestMoves of "+board+" is null, all children are: "+board.getAllMoves());
                     return;
                     //BUG: why is this null?
                 }
                 Possibility bestPossibility = getBestPossibility(bestMoves);
                 Object bestMove = bestPossibility.compResult;
                 //now I know what my best move is, write to database
                 updateBoard(board, context, new BasicDBObject(BoardImp.BEST_RESULT_FIELD_NAME, bestMove)
                         .append(BoardImp.BEST_MOVE_FIELD_NAME, bestPossibility.column));
                 //now update parents
                 Object bestParent = bestPossibility.getParent(); //what the parent's best_results[i] should be set to
                 for (BoardImp parent : board.getAllParents()) {
                     final BasicDBObject toSet = new BasicDBObject();
                     toSet.append(BoardImp.BEST_RESULTS_FIELD_NAME + "." + parent.getChildIsMove(), bestParent);
                     updateBoard(parent, context, toSet);
                 }//for
             }catch(RuntimeException t){
                 context.getCounter(CounterKeys.ERROR).increment(1);
                 throw t;
             }
         }
         @Override
         protected void setup(Context context) throws IOException, InterruptedException {
             this.height = context.getConfiguration().getInt(height_field_name, -1);
             super.setup(context);
         }
         private static Possibility getBestPossibility(BasicBSONObject moves){
             Possibility best = null;
             for (Map.Entry<String, Object> me : moves.entrySet()) {
                 Possibility p = new Possibility(Integer.parseInt(me.getKey()), me.getValue());
                 if (p.isBetterThan(best)){
                     best = p;
                 }
             }
             return best;
         }
     }
 
     /** Based on the next generation, calculate the best move you can make for boards in this generation
      * @param move a.k.a. generation number */
     private boolean setBestMoves(int move) throws IOException, InterruptedException, ClassNotFoundException{
         //when this is run all of the BEST_MOVE_FIELD_NAME arrays should be filled out
         //query (board where bestresult == null && numcheckers = numcheckers)
         final Configuration conf = getConf();
         {
             //NOTE: must do this BEFORE Job is created
             final MongoConfig mongo_conf = new MongoConfig(conf);
             com.mongodb.BasicDBObject query = new com.mongodb.BasicDBObject(BoardImp.NUM_CHECKERS_FIELD_NAME, move);
             query.append(BoardImp.BEST_RESULT_FIELD_NAME, DOES_NOT_EXIST);
             //winning boards have their information set when they are generated, no need to look at its children to
             //determine best move (in fact it doesn't have any children)
             query.append("wins",  DOES_NOT_EXIST);
             mongo_conf.setQuery(query);
         }
         final Job job = new Job(conf , "conn4 generator setBestMoves("+move+")");
         job.setMapperClass( BestMoveMapper.class );
         job.setReducerClass( BestMoveReducer.class );
         
         job.setJarByClass(this.getClass());
         
         job.setOutputKeyClass( String.class );
         job.setOutputValueClass( BSONObject.class );
         job.setMapOutputKeyClass( Text.class );
         job.setMapOutputValueClass( BSONWritable.class );
         
         job.setInputFormatClass( MongoInputFormat.class );
         job.setOutputFormatClass( MongoOutputFormat.class );
         final boolean ans = job.waitForCompletion(true);
         if (! ans){
             log.error("setBestMoves("+move+") job returned false");
             return ans;
         }
         try{
             long errors = job.getCounters().findCounter(CounterKeys.ERROR).getValue();
             if (errors > 0)
                 System.out.println("setBestMoves("+move+") had "+errors+" errors");
             return errors == 0;
         }catch(Exception e){
             log.error("setBestMoves("+move+") catch "+e.getClass()+" getting error counter");
         }
         return true;
     }
     //------------
     final private static BasicDBObject statekey = new BasicDBObject("_id", "state");
     private DBObject getState(DBCollection coll){
          DBCursor cursor = coll.find(statekey);
          if (cursor.hasNext())
              return cursor.next();
          return null;
     }
     private boolean setState(DBCollection coll, String place, int move){
         final WriteResult result = coll.update(statekey, new BasicDBObject("$set", new BasicDBObject(place, move)));
         System.out.println("setState(): result is: "+result);
         final CommandResult lastError = result.getLastError();
        if (lastError == null)
                 return true;
        System.err.println("Could not set state, err is: "+lastError);
         return false;
     }
     //Can't do right now bec. of type safety can't mix and match Text and MongoUpdateKey
     //todo: update plugin to have a superclass of  MongoUpdateKey (MongoUpdate) with
     //another subclass for simple setting so we can use both
     private static void markDone(Context context, String key, String what){
     //      context.write(MongoUpdateKey.getFor(key),
     //              new BSONWritable(new BasicDBObject(DONE_KEY, what)) );
     }
      
     @Override
     public int run(String[] args) throws Exception{
 //        BestMoveMapper.class.newInstance();
         int width = 4;
         int height = 4;
         final String defaultDbUri = "mongodb://localhost:30010/";
         final String DefaultDbName = "test";
         for (int i = 0; i < args.length; i++) {
             String argi = args[i];
             if ("--width".equals(argi))
                     width = Integer.parseInt(args[++i]);
             else if ("--height".equals(argi))
                 height = Integer.parseInt(args[++i]);
 //            else if ("--uri".equals(argi))
 //                defaultDbUri = args[++i];
             else {
                 System.err.println("Unknown argument: "+argi);
                 System.exit(1);
             }
         }
         final Configuration conf = getConf();
         
         MongoURI origUri =  MongoConfigUtil.getOutputURI( conf);
         log.info("uri from conf is: "+origUri);
         if (origUri == null)
             origUri = new MongoURI(defaultDbUri);
         MongoURI wholeMuri = origUri;
         String dbName = origUri.getDatabase();
         String collName = null;
         if (dbName == null || dbName.length() == 0)
             dbName = DefaultDbName;
         else
             collName = origUri.getCollection();
         if (collName == null || collName.length() == 0) {
             collName = "conn4boards" + width + "x" + height;
             wholeMuri = new MongoURI(origUri + "/" + dbName + "." + collName);
         }
         log.info(" collname: "+collName+" dName: "+dbName+" whole uri: "+wholeMuri);
         Mongo m = new Mongo(wholeMuri);
         try{
             final DB db = m.getDB(wholeMuri.getDatabase());
             DBCollection coll = db.getCollection(collName);
 
             //saved state, so we don't have to restart partially done jobs from the start
             final DBObject stateDoc = getState(coll);
             final int num_moves = height * width ;
             //which move round we start generating
             int genstart = 1;
             //which round we start setting the best move fields
             int beststart = num_moves - 1;
 
             if (stateDoc == null) { //this is a fresh database
                 //1) put empty board into database
                 BoardImp board = new BoardImp(width, height);
                 final WriteResult insertResult = coll.insert(board.toBSONObject(), new WriteConcern(1));
 
                 System.out.println("inserted initial empty board, result: " + insertResult);
                 coll.insert(new BasicDBObject("_id", "state").append("gen", 0));
                 try {
                     DB adminDb = m.getDB("admin");
                     BasicDBObject command = new BasicDBObject("shardcollection", dbName + "." + collName).append("key", new BasicDBObject("_id", 1));
                     final CommandResult commandResult = adminDb.command(command);
                     log.info("running shard command result: " + commandResult);
                 } catch (Exception e) {
                     log.error("Caught ex trying to shard collection " + dbName + "." + collName, e);
                     return -1;
                 }
             } else {
                 log.info("Read state from db: "+stateDoc);
                 Number genDid = (Number) stateDoc.get("gen");
                 if (genDid != null){
                     genstart = genDid.intValue() + 1;
                     //remove any partial results of generation x+1
                     coll.remove(new BasicDBObject(BoardImp.NUM_CHECKERS_FIELD_NAME, genstart));//todo: get rid of need for this
                 }else{
                     Number bestDid = (Number) stateDoc.get("best");
                     if (bestDid != null) {
                         beststart = bestDid.intValue() - 1;
                         genstart = num_moves + 1; //don't do generation stage
                     } else {
                         System.out.println("Am I finished? state doc is: " + stateDoc);
                         return 0;
                     }
                 }
             }//else
 
 
             conf.setInt(height_field_name, height);
             System.out.println("Setting uris to "+wholeMuri);
             MongoConfigUtil.setInputURI( conf, wholeMuri);
             MongoConfigUtil.setOutputURI( conf, wholeMuri);
 
 
             for(int move = genstart ; move <= num_moves; move++){
                 System.out.println("--------------- starting generateBoards for move "+move+"/"+num_moves);
                 if (!generateBoardsForGeneration(move)){
                     System.out.println("generateBoardsForGeneration("+move+") returned false, exiting");
                     return 1;
                 }
                 if (!setState(coll, "gen", move))
                     return 1;
                 //if (move == 2)
                 //   break; //for testing
             }
             for(int move = beststart ; move >= 0 ; move--){
                 System.out.println("--------------- running setBestMoves("+move+") ("+beststart+" -> 0)");
                 if (!setBestMoves(move)){
                     System.out.println("setBestMoves("+move+") returned false, exiting");
                     return 1;
                 }
                 if (!setState(coll, "best", move))
                     return 2;
             }
         }finally{
             if (m != null)
                 m.close();
         }
         return 0;
     }
     private static class Possibility{
         Object compResult; //original Object
         String result; //win/lose/tie
         int moves;
         int column;
         Possibility(int col, Object compResult){
             this.column = col;
             this.compResult = compResult;
             if ("tie".equals(compResult))
                 result = "tie";
             else{
                 BasicBSONObject bobj = (BasicBSONObject) compResult;
                 final Map.Entry<String, Object> me = bobj.entrySet().iterator().next();
                 result = me.getKey();
                 if ( ! ("win".equals(result) || "lose".equals(result)))
                     throw new IllegalArgumentException("result is not win/lose/tie: '"+result+"' "+result.getClass());
                 moves = ((Number)me.getValue()).intValue();
             }
        }
         boolean isBetterThan(Possibility other){
             if (other == null)
                 return true; //anything is better than null
             if ("wins".equals(result)){
                 //This is a winning move, the other isn't, so this is better
                 if (! "wins".equals(other.result))
                     return true;
                 //This move wins earlier than the other move
                 return moves < other.moves;
             }else if ("lose".equals(result)){
                 //This losing move is better than the other losing move if it takes longer to lose
                 return "lose".equals(other.result) && moves > other.moves;
             }else{ //tie
                 return "lose".equals(other.result);
             }
        }
        Object getParent(){
            if ("lose".equals(result)){
                return new BasicBSONObject("win", moves + 1);
            }else if ("win".equals(result)){
                return new BasicBSONObject("lose", moves );
            }else
                return result; //tie
        }
     }
     /*
      * board looks like:
      * board string: {{, , , r, b},{...}}
      * best moves: array of arrays. for each colum who wins in how many moves [[red, 1],[black,2],[draw,3]]
      * best result: for this player (red or black) what is the best result possible for this board in a perfectly played game?
      * wins: present if board is a final board. possible values: red, black, draw
      * numcheckers: number of checkers on the board. If even it is red's turn, if odd black's turn
      */
     public static final void main(String[] args) throws Exception {
         org.apache.log4j.Logger.getLogger(com.mongodb.hadoop.io.BSONWritable.class).setLevel(org.apache.log4j.Level.WARN);
         org.apache.log4j.Logger.getLogger(com.mongodb.hadoop.input.MongoInputSplit.class).setLevel(org.apache.log4j.Level.DEBUG);
         org.apache.log4j.Logger.getLogger(com.mongodb.hadoop.MongoInputFormat.class).setLevel(org.apache.log4j.Level.DEBUG);
         
         Configuration conf = new Configuration();
         ToolRunner.run(conf, new Connect4Generator(), args);
     }
 }
