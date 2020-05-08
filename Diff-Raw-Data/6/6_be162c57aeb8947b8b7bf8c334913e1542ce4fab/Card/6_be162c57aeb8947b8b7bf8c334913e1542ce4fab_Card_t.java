 package netbang.core;
 
 import java.util.Arrays;
 
 public class Card {
     public String description="";
     public Enum e;
     public String name;
     public int ordinal;
     /**
      * 1 = char, 2 = play, 3 = greenfield, 4 = miss, 5 = bluefield
      */
     public int type;
     /**
      * 1 = self, 2 = choose 1 player, 3 = all, 4 = all others
      */
     public enum Targets{SELF, ONE, ALL, OTHERS};
     public Targets target;
     /**
      * For play cards: 1 = deal damage, 2 = heal, 3 = miss, 4 = draw
          * For char cards: 1 = special draw
      */
     public int effect;
     public int effect2; // secondary effects only affect player
     public int special; // HP for char cards, ???? for other cards, 1 for beer
                         // and bangs, 1 for miss, 2 for dodge
     public boolean discardToPlay; // cards that need a discard to play
     public int range; // used for guns and panic and #cards drawn
         
     public int location; //0 = in hand, 1 = on field, 2 = played
     public static enum play {
         DAMAGE, HEAL, MISS, DRAW, STEAL, DISCARD, DUEL, JAIL
     }; // played cards
 
     public static enum field {
         DAMAGE, HEAL, MISS, DRAW, STEAL, DISCARD, BARREL, DYNAMITE, GUN, HORSE_RUN, HORSE_CHASE
     }; // field cards
     public enum suit{CLUB, DIAMOND, HEART, SPADE};
 
     public Card(Enum e) {
         this.e = e;
         ordinal = e.ordinal();
         name = e.toString();
         if (e instanceof Deck.Characters) {
             type = 1;
             int[] threehp = new int[] { 3, 6, 8, 16, 21, 27, 28, 30 };
             if (Arrays.binarySearch(threehp, ordinal) >= 0
                    && ordinal == threehp[Arrays.binarySearch(threehp, ordinal)]) {// awkward way of doing contains
                 special = 3;
             } else
                 special = 4;
                         switch ((Deck.Characters) e) {
                             case  BART_CASSIDY:
                                     description="*Draws a card each time loses a life point.";
                                     break;
                             case  BLACK_JACK:
                                     description="Reveals the second card drawn. 50% chance of drawing another card.";
                                     effect = 1;
                                     break;
                             case  CALAMITY_JANET:
                                     description="*May play a Miss as a BANG!, and vice versa.";
                                     break;
                             case  EL_GRINGO:
                                     description="*When damaged by a player, draws a card from that player.";
                                     break;
                             case  JESSE_JONES:
                                     description="*May draw his first card from the hand of a player.";
                                     effect = 1;
                                     break;
                             case  JOURDONNAIS:
                                     description="25% chance to dodge a BANG!";
                                     break;
                             case  KIT_CARLSON:
                                     description="*May look at the top three cards of the deck and choose which two to draw.";
                                     effect = 1;
                                     break;
                             case  LUCKY_DUKE:
                                     description="*When effected by a chance card, he gets to roll twice.";
                                     break;
                             case  PAUL_REGRET:
                                     description="Other players see him as 1 farther.";
                                     break;
                             case  PEDRO_RAMIREZ:
                                     description="*May draw his first card from the top of the discard.";
                                     effect = 1;
                                     break;
                             case  ROSE_DOOLAN:
                                     description="Sees other players as 1 closer.";
                                     break;
                             case  SID_KETCHUM:
                                     description="*May discard two cards to regain one life.";
                                     break;
                             case  SLAB_THE_KILLER:
                                     description="*Players must use two misses against his BANG!s.";
                                     break;
                             case  SUZY_LAFAYETTE:
                                     description="*Draws a card when she has no cards in her hand.";
                                     break;
                             case  VULTURE_SAM:
                                     description="*Takes eliminated player's hands.";
                                     break;
                             case  WILLY_THE_KID:
                                     description="May play any amount of BANG! cards on his turn.";
                                     break;
                             case  APACHE_KID:
                                     description="*25% chance of being immune to any card except discard and steal cards and during duels.";
                                     break;
                             case  BELLE_STAR:
                                     description="*No cards in front of any other player has any effect during her turn.";
                                     break;
                             case  BILL_NOFACE:
                                     description="*Draws cards equal to the amount of life he's missing plus one.";
                                     effect = 1;
                                     break;
                             case  CHUCK_WENGAM:
                                     description="*May sacrifice one life point to draw 2 cards on his turn.";
                                     break;
                             case  DOC_HOLYDAY:
                                     description="*May discard two cards to play a BANG!. Does not count towarsds the BANG! limit.";
                                     break;
                             case  ELENA_FUENTE:
                                     description="*Can discard any card to play a Miss effect.";
                                     break;
                             case  GREG_DIGGER:
                                     description="*Regains two life when a player is eliminated.";
                                     break;
                             case  HERB_HUNTER:
                                     description="*Draws two cards when a player is eliminated.";
                                     break;
                             case  JOSE_DELGADO:
                                     description="*May discard a blue card from his hand to draw two cards.";
                                     break;
                             case  MOLLY_STARK:
                                     description="*Draws one card for each Miss, Beer, or BANG! she plays off her turn.";
                                     break;
                             case  PAT_BRENNAN:
                                     description="*May draw one card only in play in front of any player.";
                                     effect = 1;
                                     break;
                             case  PIXIE_PETE:
                                     description="Draws four cards during his turn instead of two.";
                                     effect = 1;
                                     break;
                             case  SEAN_MALLORY:
                                     description="*Does not have to discard cards at the end of his turn.";
                                     break;
                             case  TEQUILA_JOE:
                                     description="Regains two life from Beers.";
                                     break;
                             case  VERA_CUSTER:
                                     description="*At the beginning of a turn, gets any player's ability for the whole round.";
                                     break;
                             default:
                                     break;
                         }
         } else {
             // TODO: find out what kind of card it is
 
             switch ((Deck.CardName) e) {
             // put all direct damage cards here
             case BACK:
                 break;
             case BANG:
                 type = 2;
                 special = 1;
                 range = 0;
                 target = Targets.ONE;
                 effect = play.DAMAGE.ordinal();
                 description = "BANG! cards are the main method to reduce other players' life points. \n" +
                         "If you want to play a BANG! card to hit one of the players, determine: a) what \n" +
                         "the distance to that player is, and b) if your weapon is capable of reaching that distance. ";
                 break;
             case PUNCH:
                 type = 2;
                 target = Targets.ONE;
                 range = 1;
                 effect = play.DAMAGE.ordinal();
                 description="This cards has same effect as BANG! card, but on distance 1.\n" +
                         " This cards isnt count as BANG!";
                 break;
             case GATLING:
                 type = 2;
                 target = Targets.OTHERS;
                 effect = play.DAMAGE.ordinal();
                 description="The symbols show: a BANG! to all the other players.";
                 break;
             case HOWITZER:
                 type = 3;
                 target = Targets.OTHERS;
                 effect = play.DAMAGE.ordinal();
                 description="The current player play a Howitzer card in front of him.\n" +
                         "Starting with the next player's turn, he can discard it for BANG!\n" +
                         "effect to all players.. This card is not count as BANG!.";
                 break;
             case INDIANS:
                 type = 2;
                 special = 2;
                 target = Targets.OTHERS;
                 effect = play.DAMAGE.ordinal();
                 description="Each player, excluding the one who played this card, may discard a BANG!\n" +
                         " card, or lose a life point. Neither Missed! nor Barrel has effect in this case.";
                 break;
             case KNIFE:
                 type = 3;
                 target = Targets.ONE;
                 range = 1;
                 effect = play.DAMAGE.ordinal();
                 description="The current player play a Knife card in front of him. Starting with the next\n" +
                         "player's turn, he can discard it for BANG! effect. This card is not count as BANG!";
                 break;
             case BUFFALO_RIFLE:
                 type = 3;
                 target = Targets.ONE;
                 range = -1;
                 effect = play.DAMAGE.ordinal();
                 description="The current player play a Buffalo Rifle card in front of him. Starting with the\n" +
                         "next player's turn, he can discard it for BANG! effect to 1 player at every distance.\n" +
                         "This card is not count as BANG!";
                 break;
             case SPRINGFIELD:
                 type = 2;
                 target = Targets.ONE;
                 discardToPlay = true;
                 range = -1;
                 effect = play.DAMAGE.ordinal();
                 description="Player which is on turn, discard card Springfield together with another card on the\n" +
                         "deck. Than he choose one player, which is target of attack with BANG! effect.";
                 break;
             case PEPPERBOX:
                 type = 3;
                 target = Targets.ONE;
                 range = 0;
                 effect = play.DAMAGE.ordinal();
                 description="The current player play a PepperBox card in front of him. Starting with the next\n" +
                         " player's turn, he can discard it for BANG! effect to 1 player at visible distance.\n" +
                         "This card is not count as BANG!.";
                 break; // TODO: make same range as bang
             case DERRINGER:
                 type = 3;
                 target = Targets.ONE;
                 range = 1;
                 effect = play.DAMAGE.ordinal();
                 effect2 = play.DRAW.ordinal();
                 description="The current player plays a Derringer card in front of her. During one of her following\n" +
                         "turns, provided she still has the card in front of her, she can choose to discard it to a BANG!\n" +
                         "on a player at a distance of 1, and also draw a card from the deck. ";
                 break;
             case DUEL:
                 type = 2;
                 target = Targets.ONE;
                 range = -1;
                 effect = play.DUEL.ordinal();
                 description="The player playing this card challenges any other player (at any distance), staring him in\n" +
                         "the eyes. The challenged player may discard a BANG! card (even though it is not his turn!).\n" +
                         "If he does, the player who played the Duel card may discard a BANG! card, and so on: the first\n" +
                         "player failing to play a BANG! card loses one life point, and the duel is over. Note: you cannot\n" +
                         "use the Barrel or play Missed! cards during a duel, and the Duel is not considered a BANG! card.";
                 break;
             case DYNAMITE:
                 type = 3;
                 target = Targets.SELF;
                 effect = field.DYNAMITE.ordinal();
                                 description="The player puts this card on his field. On his next turn, it has a 20% chance of blowing up.\n" +
                                                 "If it doesn't blow up, it is given to the player to his left, and then it has a chance of\n" +
                                                 "blowing up on that player. If it blows up, it does three damage to that player.\n" + 
                                                 "If it continues through all players without blowing up, it is discarded.";
                                 break;
             case MISS:
                 type = 4;
                 special = 1;
                 effect = play.MISS.ordinal();
                                 description="Playing a miss in response to a damage card, such as a BANG!, prevents you from losing life.";
                 break;
             case DODGE:
                 type = 4;
                 effect = play.MISS.ordinal();
                                 effect2 = play.DRAW.ordinal();
                                 description="A miss with the added effect of drawing a card.";
                 break;
             case BIBLE:
                 type = 3;
                 effect = play.MISS.ordinal();
                                 effect2 = play.DRAW.ordinal();
                                 description="A miss that must be first placed on the field to be used.";
                 break;
             case IRON_PLATE:
                 type = 3;
                 effect = play.MISS.ordinal();
                                 description="A miss that must be first placed on the field to be used.";
                 break;
             case SOMBRERO:
                 type = 3;
                 effect = play.MISS.ordinal();
                                 description="A miss that must be first placed on the field to be used.";
                 break;
             case TEN_GALLON_HAT:
                 type = 3;
                 effect = play.MISS.ordinal();
                                 description="A miss that must be first placed on the field to be used.";
                 break;
             case BARREL:
                 type = 5;
                 effect = field.BARREL.ordinal();
                                 description="Each time you are targetted by a BANG! effect, there is a 25% chance that you will MISS!\n" +
                                                 "Does not work on Gatling or Howitzer.";
                                 break;
             case WELLS_FARGO:
                 type = 2;
                 range = 3;
                 effect = play.DRAW.ordinal();
                                 description="Draw THREE cards!";
                 break;
             case STAGECOACH:
                 type = 2;
                 range = 2;
                 effect = play.DRAW.ordinal();
                                 description="Draw two cards.";
                 break;
             case CONESTOGA:
                 type = 3;
                 range = 2;
                 effect = play.DRAW.ordinal();
                                 description="After staying on the field for one turn, can be discarded to draw two cards.";
                 break;
             case PONY_EXPRESS:
                 type = 3;
                 range = 3;
                 effect = play.DRAW.ordinal();
                                 description="After staying on the field for one turn, can be discarded to draw three cards.";
                 break;
             case GENERAL_STORE:
                 type = 2;
                 target = Targets.ALL;
                 range = 1;
                 effect = play.DRAW.ordinal();
                                 description="After staying on the field for one turn, can be discarded to draw two cards.";
                 break;
 
             case JAIL:
                 type = 2; //TODO: make special case for jail
                 range = -1;
                                 target = Targets.ONE;
                 effect = play.JAIL.ordinal();
                                 description="25% chance that target player skips his next turn. Cannot be used on the Sheriff.";
                 break; // special case: even though jail remains on the field of
                         // a player, it is "played"
 
             case APPALOOSA:
                 type = 5;
                 effect = field.HORSE_CHASE.ordinal();
                                 description="Other players' distances are decreased by one when you target them.";
                 break;
             case SILVER:
                 type = 5;
                 effect = field.HORSE_CHASE.ordinal();
                                 description="Other players' distances are decreased by one when you target them.";
                 break;
             case MUSTANG:
                 type = 5;
                 effect = field.HORSE_RUN.ordinal();
                                 description="Your distance from other players is increased by one when they target you.";
                 break;
             case HIDEOUT:
                 type = 5;
                 effect = field.HORSE_RUN.ordinal();
                                 description="Your distance from other players is increased by one when they target you.";
                 break; // you heard me: a hideout is a horse.
 
             case BEER:
                 type = 2;
                 target = Targets.SELF;
                                 range = 1;
                 special = 1;
                 effect = play.HEAL.ordinal();
                                 description="Regain one life.";
                 break;
             case TEQUILA:
                 type = 2;
                 target = Targets.ONE;
                                 range = 1;
                 discardToPlay = true;
                 effect = play.HEAL.ordinal();
                                 description="Discard a card to play, target player regains one life.";
                 break;
             case WHISKY:
                 type = 2;
                 target = Targets.SELF;
                 range = 2;
                 discardToPlay = true;
                 effect = play.HEAL.ordinal();
                                 description="Discard a card to play, regain 2 life.";
                 break; // special case: heals 2 hp, so i guess i'll use "range"
             case CANTEEN:
                 type = 3;
                 target = Targets.SELF;
                                 range = 1;
                 effect = play.HEAL.ordinal();
                 break;
             case SALOON:
                 type = 2;
                 target = Targets.ALL;
                                 range = 1;
                 effect = play.HEAL.ordinal();
                                 description="All players regain one life.";
                 break;
 
             case BRAWL:
                 type = 2;
                 target = Targets.OTHERS;
                 discardToPlay = true;
                 play.DISCARD.ordinal();
                                 description="Discard a card to play. Choose one card for each player; that player discards it.";
                                 break;
             case CAN_CAN:
                 type = 3;
                 target = Targets.ONE;
                 range = -1;
                 effect = play.STEAL.ordinal();
                                 description="After staying on the field for one turn, may be discarded to draw a card in any\n" +
                                                 "opponent's hand or field.";
                 break;
             case RAG_TIME:
                 type = 2;
                 target = Targets.ONE;
                 range = -1;
                 discardToPlay = true;
                 effect = play.STEAL.ordinal();
                                 description="Draw a card in any opponent's hand or field at any distance. Requires a discard to play.";
                 break;
             case PANIC:
                 type = 2;
                 target = Targets.ONE;
                 range = 1;
                 effect = play.STEAL.ordinal();
                                 description="Draw a card in any opponent's hand or field at a range of 1.";
                 break;
             case CAT_BALLOU:
                 type = 2;
                 target = Targets.ONE;
                 range = -1;
                 effect = play.DISCARD.ordinal();
                                 description="Pick a card in any opponent's hand or field. That card is discarded.";
                 break;
 
             case VOLCANIC:
                 type = 5;
                 special = 1;
                 range = 1;
                 effect = field.GUN.ordinal();
                                 description="A gun with a range of 1. Can play unlimited BANGS! with this gun.";
                 break;
             case SCHOFIELD:
                 type = 5;
                 range = 2;
                 effect = field.GUN.ordinal();
                                 description="A gun with a range of 2.";
                 break;
             case REMINGTON:
                 type = 5;
                 range = 3;
                 effect = field.GUN.ordinal();
                                 description="A gun with a range of 3.";
                 break;
             case REV_CARBINE:
                 type = 5;
                 range = 4;
                 effect = field.GUN.ordinal();
                                 description="A gun with a range of 4.";
                 break;
             case WINCHESTER:
                 type = 5;
                 range = 5;
                 effect = field.GUN.ordinal();
                                 description="A gun with a range of 5.";
                 break;
 
             default:
                 break;
             }
         }
     }
 
     //for display purposes only:
     //TODO: What is this?
     public static Card playedCard(Enum e){
         return null;
     }
     
     public void setLocation(int i){
         location = i;
     }
         
     public String toString(){
         return name;
     }
 }
