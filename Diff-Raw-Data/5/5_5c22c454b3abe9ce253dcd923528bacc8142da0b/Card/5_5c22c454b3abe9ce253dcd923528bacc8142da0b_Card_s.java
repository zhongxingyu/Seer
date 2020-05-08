 package dominion481.game;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 public enum Card {
   //Base victory cards
 	Province(Type.VICTORY, 8, 6, 0),
 	Duchy(Type.VICTORY, 5, 1, 0),
 	Estate(Type.VICTORY, 2, 3, 0),
 	
 	//Base treasure cards
 	Gold(Type.TREASURE, 6, 0, 3),
 	Silver(Type.TREASURE, 3, 0, 2),
 	Copper(Type.TREASURE, 0, 0, 1),
 	
 	//Kingdom Cards
 	
 	/*
 	 * Cellar
 	 * + 1 Action
 	 * Discard any number of cards. +1 Card per card discarded.
 	 */
    Cellar(2) {
       void play(Player player, DominionState state) {
          List<Card> discards = player.cellar();
          for (Card discard : discards) {
             player.discard(discard);
          }
       
          for (int i = 0; i < discards.size(); i++) {
             player.draw();
          }
       }
    },
 	/*
 	 * Chapel
 	 * Trash up to 4 cards from hand.
 	 */
 	Chapel(2) {
 	   void play(Player player, DominionState state) {
 	      List<Card> trashes = player.chapel();
 	      for (Card trash : trashes) {
 	         if (!player.hand.remove(trash)) {
 	            throw new IllegalArgumentException("Trash " + trash + " is not in hand");
 	         }
 	      }
 	   }
 	},
    /*
     * Moat
     * +2 Cards
     * Reacts to protect against attacks
     */
    Moat(2) {
       void play(Player player, DominionState state) {
          player.draw();
          player.draw();
       }
    },
    /*
     * Chancellor
     * +(2)
     * You may put your deck into your discard
     */
    Chancellor(3) {
       void play(Player player, DominionState state) {
          player.coin += 2;
          
          if (player.chancellor()) {
             player.discard.addAll(player.deck);
             player.deck = new LinkedList<Card>();
          }
       }
    },
    /*
     * Village
     * +1 Card
     * +2 Actions
     */
    Village(3) {
       void play(Player player, DominionState state) {
          player.actions += 2;
          player.draw();
       }
    },
    /*
     * Woodcutter
     * +(2)
     * +1 Buy
     */
    Woodcutter(3) {
       void play(Player player, DominionState state) {
          player.coin += 2;
          player.buys += 1;
       }
    },
    /*
     * Workshop
     * Gain any card costing up to 4
     */
    Workshop(3) {
       void play(Player player, DominionState state) {
          Card gain = player.workshop();
          if (gain != null) {
             if (gain.cost <= 4) {
                player.gain(gain);
             }
             else {
                throw new IllegalArgumentException("Cannot workshop " + gain);
             }
          }
       }
    },
    /*
     * Bureaucrat
     * Gain a silver and place it atop of your deck
     * All others must place a VP card from hand atop their deck (if possible)
     */
    Bureaucrat(4, true) {
       //TODO
    },
    /*
     * Feast
     * Trash this. Gain a card costing up to 5
     */
    Feast(4) {
       public void play(Player player, DominionState state) {
          player.inPlay.remove(this);
          
          Card gain = player.feast();
          if (gain != null) {
             if (gain.cost <= 4) {
                player.gain(gain);
             }
             else {
                throw new IllegalArgumentException("Cannot feast " + gain);
             }
          }
       }
    },
    /*
     * Gardens
     * VP Value is deck size / 10, rounded down
     */
    Gardens(Type.VICTORY, 4, 0, 0) {
       public int getVp(Player player) {
          return (player.deck.size() + player.discard.size()) / 10;
       }
    },
    Militia(4, true) {
       //TODO
    },
    /*
     * Moneylender
     * Trash a copper from your hand. If you do, +(3)
     */
    Moneylender(4) {
       public void play(Player player, DominionState state) {
          if (player.hand.remove(Card.Copper)) {
             player.coin += 3;
          }
       }
    },
    /*
     * Remodel
     * Trash a card from your hand. Gain a card costing up to 2 more
     */
    Remodel(4) {
       public void play(Player player, DominionState state) {
          if (player.hand.size() == 0) {
             return;
          }
          
          Card[] remodel = player.remodel();
          Card trash = remodel[0];
          Card gain = remodel[1];
          
          if (!player.hand.remove(trash)) {
             throw new IllegalArgumentException("Trash " + trash + " is not in hand");
          }
          
          if (gain != null) {
             if (gain.cost <= trash.getCost() + 2) {
                player.gain(gain);
             }
             else {
               throw new IllegalArgumentException("Cannot remodel " + trash + "to "+ gain);
             }
          }
       } 
    },
    /*
     * Smithy
     * +3 Cards
     */
    Smithy(4) {
       public void play(Player player, DominionState state) {
          player.draw();
          player.draw();
          player.draw();
       }
    },
    Spy(4, true) {
       /* TODO */
    },
    Theif(4, true) {
       /* TODO */
    },
    ThroneRoom(4) {
       public void play(Player player, DominionState state) {
          Card card = player.throneRoom();
          
          if (card != null) {
             player.actions += 1;
             player.playAction(card);
             card.play(player, state);
          }
       }
    },
    CouncilRoom(5) {
       /* TODO */
    },
    /*
     * Festival
     * +2 Actions
     * +1 Buy
     * +(2)
     */
    Festival(5) {
       public void play(Player player, DominionState state) {
          player.actions += 2;
          player.buys += 1;
          player.coin += 2;
       }
    },
    /*
     * Festival
     * +2 Actions
     * +1 Buy
     * +(2)
     */
    Laboratory(5) {
       public void play(Player player, DominionState state) {
          player.actions += 1;
          player.draw();
          player.draw();
       }
    },
    /*
     * Library
     * Draw until you have 7 cards
     * You may set aside and later discard any actions drawn in this manner
     */
    Library(5) {
       public void play(Player player, DominionState state) {
          List<Card> setAside = new ArrayList<Card>();
          
          while (player.hand.size() < 7) {
             Card draw = player.draw();
             if (draw == null) {
                break;
             }
             
             if (draw.type == Type.ACTION) {
                if (player.libraryDiscard(draw)) {
                   setAside.add(draw);
                   player.hand.remove(draw);
                }
             }
          }
          
          player.discard.addAll(setAside);
       }
    },
    /*
     * Market
     * +1 Card, +1 Action, +1 Buy, +(1)
     */
    Market(5) {
       public void play(Player player, DominionState state) {
          player.coin += 1;
          player.actions += 1;
          player.buys += 1;
          player.draw();
       }
    },
    /*
     * Mine
     * Trash a treasure from hand. Gain a treasure costing up to 3 more... in hand
     */
    Mine(5) {
       public void play(Player player, DominionState state) {
          if (player.hand.size() == 0) {
             return;
          }
          
          Card[] remodel = player.mine();
          Card trash = remodel[0];
          Card gain = remodel[1];
          
          if (trash.type != Type.TREASURE) {
             throw new IllegalArgumentException("Cannot mine " + trash);
          }
          
          if (!player.hand.remove(trash)) {
             throw new IllegalArgumentException("Trash " + trash + " is not in hand");
          }
          
          if (gain != null) {
             if (gain.cost <= trash.getCost() + 3 && gain.type == Type.TREASURE) {
                //TODO Clean this up?
                player.gain(gain);
                player.discard.remove(gain);
                player.hand.add(gain);
             }
             else {
               throw new IllegalArgumentException("Cannot mine " + trash + "to "+ gain);
             }
          }
       } 
    },
    Witch(5, true) {
       /* TODO */
    },
    /*
     * Adventurer
     * Reveal cards from your deck until you find two treasures.
     * Add the treasures to your hand. Discard the revealed cards.
     */
    Adventurer(6) {
       public void play(Player player, DominionState state) {
          int found = 0;
          List<Card> setAside = new ArrayList<Card>();
          
          while (found < 2) {
             Card card = player.draw();
             if (card == null) {
                break;
             }
             else if (card.type == Type.TREASURE) {
                found++;
             }
             else {
                setAside.add(card);
                player.hand.remove(card);
             }
          }
          
          player.discard.addAll(setAside);
       }
    }
 	;
 	public enum Type {
 		ACTION, VICTORY, TREASURE;
 	}
 	
 	private final int cost, vp, treasureValue;
 	private final boolean attack;
 	public final Type type;
 	
 	public int getVp(Player player) { return vp; }
 	public int getCost() { return cost; }
 	public int getTreasureValue() { return treasureValue; }
 	
 	void play(Player player, DominionState state) {}
 	public void react() {}
 	
 	private Card(int cost) {
 	   this(cost, false);
 	}
 	
 	private Card(int cost, boolean attack) {
 	   type = Type.ACTION;
       this.cost = cost;
       vp = 0;
       treasureValue = 0;
       this.attack = attack;
 	}
 	
 	private Card(Type type, int cost, int vp, int treasureValue) {
 		this.cost = cost;
 		this.vp = vp;
 		this.treasureValue = treasureValue;
 		this.type = type;
 		this.attack = false;
 	}
 }
