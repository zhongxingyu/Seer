 package mel.fencing.server;
 
 public class Game
 {
     //TODO RFI consider using powers of two so one bit each for color, canAdvance/Attack, canRetreat, and canParry 
     public static final int COLOR_NONE = -10;
     public static final int COLOR_WHITE = 0;
     public static final int COLOR_BLACK = 10;
     public static final int TURN_MOVE = 0;
     public static final int TURN_PARRY = 1;
     public static final int TURN_PARRY_OR_RETREAT = 2;
     public static final int TURN_BLACK_MOVE =               COLOR_BLACK+TURN_MOVE;
     public static final int TURN_BLACK_PARRY =              COLOR_BLACK+TURN_PARRY;
     public static final int TURN_BLACK_PARRY_OR_RETREAT =   COLOR_BLACK+TURN_PARRY_OR_RETREAT; 
     public static final int TURN_WHITE_MOVE =               COLOR_WHITE+TURN_MOVE;
     public static final int TURN_WHITE_PARRY =              COLOR_WHITE+TURN_PARRY;
     public static final int TURN_WHITE_PARRY_OR_RETREAT =   COLOR_WHITE+TURN_PARRY_OR_RETREAT; 
     public static final int TURN_GAME_OVER = -1;
     
     UserSession black;
     UserSession white;
     Deck deck;
     Hand whiteHand = new Hand();
     Hand blackHand = new Hand();
     int blackHP = 5;
     int whiteHP = 5;
     int blackpos = 23;
     int whitepos = 1;
     int turn = TURN_WHITE_MOVE;
     int parryVal = -1;
     int parryCount = -1;
     
     private Game(UserSession challenger, UserSession target)
     {
         black = challenger;
         white = target;
         deck = new Deck();
         deck.shuffle();
         black.setGame(this);
         black.setColor(COLOR_BLACK);
         white.setGame(this);
         white.setColor(COLOR_WHITE);
         sendNames();
         blackHand.fill(deck);
         sendBlackHand();
         whiteHand.fill(deck);
         sendWhiteHand();
     }
     
     public static Game newGame(UserSession challenger, UserSession target)
     {
         return new Game(challenger, target);
     }
     
     private void sendWhiteHand() { white.send(whiteHand.toString()); }
     private void sendBlackHand() { black.send(blackHand.toString()); }
     
     private void sendNames()
     {
         white.send("w"+black.getUsername());
         black.send("b"+white.getUsername());
     }
     
     private final int playerColor(UserSession player)
     {
         if(player == white) return COLOR_WHITE;
         if(player == black) return COLOR_BLACK;
         return COLOR_NONE;
     }
     
     synchronized void jumpAttack(UserSession player, String values)
     {
         if(values.length() != 3) send(player, "ESyntax error in attack:"+values);
         int distance = parseDigit(values.charAt(0));
         int value = parseDigit(values.charAt(1));
         int count = parseDigit(values.charAt(2));
         if(value<0 || count<1) send(player, "ESyntax error in attack:"+values);
         
         int color = playerColor(player);
         if(color == COLOR_NONE) send(player, "EYou are not a player in this game");
         if(turn != color+TURN_MOVE) 
         {
             if(turn == color+TURN_PARRY || turn == color+TURN_PARRY_OR_RETREAT) send(player, "EDefend before attacking");
             else send(player, "ENot your turn to attack");
             return;
         }
                 
         if(!handOf(color).hasCardWithCards(distance, value, count))
         {
             send(player, "EYou don't have the cards to attack ("+value+","+count+")");
             return;
         }
         
         // TODO handle attack logic
         parryVal = value;
         parryCount = count; 
     }
     
     synchronized void standingAttack(UserSession player, String values)
     {
         if(values.length() != 2) send(player, "ESyntax error in attack:"+values);
         int value = parseDigit(values.charAt(0));
         int count = parseDigit(values.charAt(1));
         if(value<0 || count<1) send(player, "ESyntax error in attack:"+values);
         
         int color = playerColor(player);
         if(color == COLOR_NONE) send(player, "EYou are not a player in this game");
         if(turn != color+TURN_MOVE) 
         {
             if(turn == color+TURN_PARRY || turn == color+TURN_PARRY_OR_RETREAT) send(player, "EDefend before attacking");
             else send(player, "ENot your turn to attack");
             return;
         }
 
         if(!handOf(color).hasCards(value, count))
         {
             send(player, "EYou don't have the cards to attack ("+value+","+count+")");
             return;
         }
         
         // TODO handle attack logic
         parryVal = value;
         parryCount = count;
     }
     
     synchronized void move(UserSession player, String values)
     {
         if(values.length() != 1) send(player, "ESyntax error in advance:"+values);
         int distance = parseDigit(values.charAt(0));
         if(distance < 0) send(player, "ESyntax error in advance:"+values);
         
         int color = playerColor(player);
         if(color == COLOR_NONE) send(player, "EYou are not a player in this game");
         if(turn != color+TURN_MOVE) 
         {
             if(turn == color+TURN_PARRY || turn == color+TURN_PARRY_OR_RETREAT) send(player, "EDefend before advancing");
             else send(player, "ENot your turn to advance");
             return;
         }
  
         if(blackpos-whitepos<=distance)
         {
             send(player, "EMay not move through other fencer");
             return;
         }
         
         Hand hand = handOf(color);
         if(!hand.hasCard(distance))
         {
             // hacked or buggy client
             send(player, "EYou don't have the advance card "+distance);
             return;
         }
 
         movePosOf(color, distance);        
         hand.removeByValue(distance);
         hand.fill(deck);
         if(deck.isEmpty()) { endGame(); return; }
         //TODO notify what action was taken
         turn = otherColor(color)+TURN_MOVE;
         notifyPositions();
         notifyHand(player, hand);
         notifyTurn();
     }
 
     synchronized void retreat(UserSession player, String values)
     {
         if(values.length() != 1) send(player, "ESyntax error in retreat:"+values);
         int distance = parseDigit(values.charAt(0));
         if(distance < 0) send(player, "ESyntax error in retreat:"+values);
         int color = playerColor(player);
         if(color == COLOR_NONE) send(player, "EYou are not a player in this game");
         if(turn != color+TURN_PARRY_OR_RETREAT && turn != color+TURN_MOVE)
         {
             if(turn == color+TURN_PARRY) send(player, "EYou cannot retreat from a standing attack");
             else send(player, "ENot your turn to retreat");
             return;
         }
         
         Hand hand = handOf(color);
         if(!hand.hasCard(distance))
         {
             // hacked or buggy client
             send(player, "EYou don't have the advance card "+distance);
             return;
         }
         
         movePosOf(color, -distance);
         hand.removeByValue(distance);
         hand.fill(deck);
         if(deck.isEmpty() || fencerOffStrip()) { endGame(); return; }
         //TODO notify what action was taken
         turn = otherColor(color)+TURN_MOVE;
         notifyPositions();
         notifyHand(player, hand);
         notifyTurn();
     }
     
     synchronized void parry(UserSession player)
     {
         int color = playerColor(player);
         if(color == COLOR_NONE) send(player, "EYou are not a player in this game");
         if(turn != color+TURN_PARRY_OR_RETREAT && turn != color+TURN_PARRY)
         {
             if(turn == color+TURN_MOVE) send(player, "EThere is no attack to parry");
             else send(player, "ENot your turn to parry");
             return;
         }
         
         if(!handOf(color).hasCards(parryVal, parryCount))
         {
             send(player, "EYou don't have the cards to parry ("+parryVal+","+parryCount+")");
             return;
         }
         
         //TODO remove cards and update state
     }
     
     private void endGame()
     {
         //TODO handle endGame
     }
     
     private final boolean fencerOffStrip()
     {
         return whitepos < 1 || blackpos > 23;
     }
     
     private void send(UserSession who, String what)
     {
         if(who != null) who.send(what);
     }
     
     private void sendAll(String what)
     {
         send(white, what);
         send(black, what);
     }
     
     static private final int parseDigit(char in)
     {
         if(in<'0' || in > '9') return -1;
         return in-'0';
     }
     
     private void notifyPositions()
     {
         StringBuilder sb = new StringBuilder(3);
         sb.append("x");
        sb.append('a'+whitepos-1);
        sb.append('a'+blackpos-1);
         sendAll(sb.toString());
     }
     
     private final void notifyHand(UserSession player, Hand hand)
     {
         send(player, hand.toString());
     }
     
     private void notifyTurn()
     {
         sendAll("t"+turn);
     }
     
     private final int otherColor(int color)
     {
         if(color == COLOR_WHITE) return COLOR_BLACK;
         if(color == COLOR_BLACK) return COLOR_WHITE;
         return COLOR_NONE;
     }
     
     private final Hand handOf(int color)
     {
         if(color == COLOR_WHITE) return whiteHand;
         if(color == COLOR_BLACK) return blackHand;
         return null;
     }
     
     private final void movePosOf(int color, int offset)
     {
         if(color == COLOR_WHITE) whitepos += offset;
         if(color == COLOR_BLACK) blackpos -= offset;
     }
 }
