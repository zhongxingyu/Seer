 package ucbang.core;
 
 import java.io.IOException;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import ucbang.gui.ClientGUI;
 
 import ucbang.network.Server;
 
 public class Bang {
     Server server;
         
     public Player[] players;
     public int numPlayers;
     
     public int turn;
     
     public ArrayList<Card> drawPile = new ArrayList<Card>(); //the card on the bottom in stored in index 0, the card on top is stored in index size()-1
     public ArrayList<Card> discardPile = new ArrayList<Card>();
     
     public Deck deck;
     
     public Bang(int p, Server s) {
         server = s;
         numPlayers = p;
     }        
     /**
      * Create p players.
      * Create a draw pile.
      * Show everyone their roles.
      * Give them a choice between two character cards.
      * Give sheriff the first turn.
      * Draw cards equal to the number of life points.
      * Sheriff gets an additional card.
      * @param p
      */
     public void start(int p){
         //Assign roles
         ArrayList<Enum> roles = new ArrayList<Enum>();
         players = new Player[numPlayers];
         for(int n = 0; n<numPlayers; n++){
             players[n] = new Player(n, server.names.get(n));
         }
         
         System.out.println(players[0]+""+players[0]+players[0]+players[0]+players[0]+players[0]);
         
         switch(p){
             case 2: //DEBUG MODE
                 roles.add(Deck.Role.SHERIFF); roles.add(Deck.Role.OUTLAW); break;
             case 4:
                 roles.add(Deck.Role.SHERIFF); roles.add(Deck.Role.OUTLAW); 
                 roles.add(Deck.Role.OUTLAW); roles.add(Deck.Role.RENEGADE); break;
             case 5:
                 roles.add(Deck.Role.SHERIFF); roles.add(Deck.Role.OUTLAW); 
                 roles.add(Deck.Role.OUTLAW); roles.add(Deck.Role.RENEGADE); 
                 roles.add(Deck.Role.DEPUTY); break;
             case 6:
                 roles.add(Deck.Role.SHERIFF); roles.add(Deck.Role.OUTLAW); 
                 roles.add(Deck.Role.OUTLAW); roles.add(Deck.Role.RENEGADE); 
                 roles.add(Deck.Role.DEPUTY); roles.add(Deck.Role.OUTLAW); break;
             case 7:
                 roles.add(Deck.Role.SHERIFF); roles.add(Deck.Role.OUTLAW); 
                 roles.add(Deck.Role.OUTLAW); roles.add(Deck.Role.RENEGADE); 
                 roles.add(Deck.Role.DEPUTY); roles.add(Deck.Role.OUTLAW); 
                 roles.add(Deck.Role.DEPUTY); break;
             case 8:
                 roles.add(Deck.Role.SHERIFF); roles.add(Deck.Role.OUTLAW); 
                 roles.add(Deck.Role.OUTLAW); roles.add(Deck.Role.RENEGADE); 
                 roles.add(Deck.Role.DEPUTY); roles.add(Deck.Role.OUTLAW); 
                 roles.add(Deck.Role.OUTLAW); roles.add(Deck.Role.RENEGADE); break;
             default: 
                 System.out.print("Bad number of players!"); System.exit(0); break;
         }
         for(int n=0; n<numPlayers; n++){
             int role = roles.remove((int)(Math.random()*roles.size())).ordinal();
             server.sendInfo(n,"SetInfo:role:"+role);
             if(role==0){
                 server.sendInfo(n,"SetInfo:maxHP:1");
             }
         }
         /*for(Card s: deck.drawPile)
             System.out.print(s.name+" ");
         System.out.print("\n");*/
         
         //Assign character cards
         ArrayList<Enum> charList = new ArrayList<Enum>();
         for(Enum e: Deck.Characters.values()){
             charList.add(e);
         }
         for(int n = 0; n<numPlayers; n++){
             drawPile.add(new Card(charList.remove((int)(Math.random()*charList.size()))));
             drawPile.add(new Card(charList.remove((int)(Math.random()*charList.size()))));
             drawPile.add(new Card(charList.remove((int)(Math.random()*charList.size()))));
             drawPile.add(new Card(charList.remove((int)(Math.random()*charList.size()))));
             drawPile.add(new Card(charList.remove((int)(Math.random()*charList.size()))));
         }
         for(int n = 0; n<numPlayers; n++){
             playerDrawCard(n, 5);
         }
         
         server.promptAll("ChooseCharacter");
         
         //debug mode
 
         deck = new Deck();
     }
     
     public void start2(){
 
         
         //draw cards equal to lifepoints
         for(Player p1: players){
             playerDrawCard(p1, p1.lifePoints);
         }
        
         System.out.println("Cards in draw pile: ");
         String pile = "";
         for(Card s: drawPile)
             pile = pile + s.name + " "; //TODO: make large messages wrap around
         System.out.println(pile);
         System.out.println("\nCards in hand: ");
         for(Card s: players[0].hand)
             System.out.println(s.name+" ");
         System.out.println("\nYou are: " + Deck.Characters.values()[players[0].character] + ", the " + players[0].role.name() + "\n");
         
         //Give Sheriff the first turn (turn 0)
         for(int n=0; n<numPlayers; n++){
             if(players[n].role==Deck.Role.SHERIFF){
                 turn=n-1;
                 break;
             }
         }
         /*while(nextTurn()){ TODO: make this without a loop
         }*/
         System.out.println("GAME OVER");
     }
     
     //returns false if game is over
     public boolean nextTurn(){
         turn++;
         
         
         //check if player is dead
         int oldturn = turn;
         while(players[turn%numPlayers].lifePoints==0&&turn-oldturn<numPlayers){
             turn++;
         }
         
         //check jail/dynamite
         
         //draw two cards
         if(players[turn%numPlayers].specialDraw==0){ //TODO: get rid of specialDraw, move to a direct reference to character cards
             playerDrawCard(players[turn%numPlayers], 2);
         }
         else{
             //Yuck, there's alot of characters with this ability
         }
         
         int card = -2;
         while(card != -1){
             if(players[turn%numPlayers].hand.size()>0){
                 if(isGameWon())
                     return false;
                 //TODO: ACTUALLY PROMPT PLAYER TO PLAY CARD card = gui[turn%numPlayers].promptChooseCard(players[turn%numPlayers].hand, "Play a card!", "It's your turn", false);
                 if(card!=-1)
                     playCardFromHand(players[turn%numPlayers], players[turn%numPlayers].hand.get(card));
                 
             }
             else{
                 //normally, you'd still be able to play cards on field
                 card = -1;
             }
         }
         return !isGameWon();
     }
     
     /**
      * If written, this method would replace the return value above
      */
     public boolean isGameWon(){
         int deputies = 0;
         int sheriff = 0;
         int outlaws = 0;
         int renegades = 0;
         for(Player p: players){
             if(p.lifePoints>0){
                 switch((Deck.Role)p.role){
                     case DEPUTY: deputies++;
                     case SHERIFF: sheriff++; break;
                     case OUTLAW: outlaws++; break;
                     case RENEGADE: renegades++; break;
                 }
             }
         }
         if(sheriff==0){
             if(outlaws==0&&deputies==0){
                 System.out.println("Renegades win!");
                 return true;
             }
             else{
                 System.out.println("Outlaws win!");
                 return true;
             }
         }
         if(sheriff>=0){
             if(outlaws==0&&deputies==0){
                 System.out.println("Sheiff+Deputies win!");
                 return true;
             }
         }
         return false;
     }
     
     /**
      * Plays a card. This is one of the functions used to connect the GUI to the game.
      * @param c
      * @return Whether that is a legal move (boolean) //TODO: this is stupid
      */
     public boolean playCardFromHand(Player p, Card c){
         if(c.type == 4){
             System.out.println("A miss card cannot be played.");
             return false;
         }
         p.hand.remove(c);
         if(c.type == 3){
             //put it on the field
         }
         if(c.type == 2){
             int[] targets;
             if(c.target == 2){
                 targets = new int[]{1};//gui[p.id].promptChooseTargetPlayer()};//TODO: Actually prompt player to choose targets
             }
             else if(c.target == 4){
                 targets = new int[numPlayers-1];
                 int m = 0;
                 for(int n = 0; n<targets.length; n++, m++)
                     if(m!=p.id)
                         targets[n] = m;
                     else
                         n--;
             }
             else if(c.target ==3){
                 targets = new int[numPlayers];
                 for(int n = 0; n<targets.length; n++)
                     targets[n] = n;
             }
             else{
                 targets = new int[1]; //serves no purpose but to initialize value
             }
         
             //damage
             if(c.effect == Card.play.DAMAGE.ordinal()){
                 for(int target: targets){
                     int miss = -2; //or bang for indians
                     while(miss != -1 || (miss>=0 && miss<players[target].hand.size() && players[target].hand.get(miss).special==(c.name==Deck.CardName.INDIANS.name()?1:0) && players[target].hand.get(miss).effect==(c.name==Deck.CardName.INDIANS.name()?Card.play.DAMAGE.ordinal():Card.play.DAMAGE.ordinal()))){
                         miss = 1;//gui[target].promptChooseCard(players[target].hand, "Dodge!", "Play a miss?", false); //TODO: FIX
                     }
                     if(miss == -1){ //change this to a flag checking barrels/if target want to play a miss, etc.
                         while(players[target].lifePoints<=0){
                             System.out.println("Invalid Target!");
                             //gui[p.id].promptChooseTargetPlayer();//TODO: FIX
                         }
                         players[target].lifePoints--;
                         System.out.println(target+"'s hp: "+players[target].lifePoints);
                         if(players[target].lifePoints <= 0){
                             //gui[p.id].appendText("You killed player "+target+"! \nPlayer "+target+" was a(n) "+players[target].role.name()+" ("+players[target].role.ordinal()+")");//TODO:FIX
                             if(players[target].role.ordinal()==2){ //if he was an outlaw, claim bounty
                                 //gui[p.id].appendText("Draw 3 cards!");//TODO:FIX
                                 playerDrawCard(p, 3);
                             }
                         }
                     }
                 }
             }
             
             //heal
             if(c.effect == Card.play.HEAL.ordinal()){
                 for(int target: targets){
                     if(c.range != 1) //shiskey
                         players[target].lifePoints++;
                     else
                         players[target].lifePoints += 2;
                     if(players[target].lifePoints>players[target].maxLifePoints)
                         players[target].lifePoints = players[target].maxLifePoints;
                     System.out.println(target+"'s hp: "+players[target].lifePoints);
                 }
             }
             //draw
             if(c.effect == Card.play.DRAW.ordinal())
                 playerDrawCard(p, c.range);
             if(c.effect2 != 0 && c.effect2 == Card.play.DRAW.ordinal())
                 playerDrawCard(p, 1);
             discardPile.add(c);
         }
         //TODO: currently only removes the card from hand and sets it into discard
         return true;
     }
     
     public void playerDrawCard(int p, int n){
         Card c = drawCard();
         String s = "Draw:"+(c.type==1?"Character:":"Game:")+c.name+":";
         for(int m=1; m<n; m++){
             c = drawCard();
             s = s+c.name+":";
         }
         server.sendInfo(p, s);
     }
     
     //this one won't work anymore
     public void playerDrawCard(Player p, int n){
         /*if(n <= 2)
             gui[p.id].appendText("Draw "+n+" card(s).");
         else if(n>2)
             gui[p.id].appendText("Draw "+n+" cards!");*/
         for(int m=0; m<n; m++)
             server.sendInfo(p.id, drawCard().toString());
     }
     
     /**
      * Discards Player p's hand
      */
     public void playerDiscardHand(Player p){
         for(int n=p.hand.size()-1; n>=0; n--)
             playerDiscardCard(p, n);
     }
     
     /**
      * Discards card n in Player p's hand
      */
     public void playerDiscardCard(Player p, int n){
         Card c = p.hand.get(n);
         //is card a character card
         if(c.type==1){
             p.hand.remove(c);
         }
         else{
             discardPile.add(c);
         }
             
     }
     
     /**
      * Flips the top card of the drawPile. This card is then put in the discard 
      * pile. Used for barrels and other effects.
      * @return
      */
     public Card flipCard(){
         Card c = drawCard();
         discardPile.add(drawCard());
         return c;
     }
     
     /**
      * Draws one card. This card is either returned to the flipCard method or
      * the playerDrawCard method.
      * @param
      * @return Card
      */
     public Card drawCard(){
         if(drawPile.size()==0){
             shuffleDeck();
         }
         System.out.println(drawPile.get(drawPile.size()-1).name);
         return drawPile.remove(drawPile.size()-1);
     }
     
     /**
      * Shuffles the discard pile back into the deck.
      * Only used when draw pile is empty
      */
     public void shuffleDeck(){
         if(drawPile.size()>0){
             System.out.println("Error: did not need to shuffleDeck()");
             return;
         }
         while(discardPile.size()>0){
             drawPile.add(discardPile.remove((int)Math.random()*discardPile.size()));
         }
     }
 }
