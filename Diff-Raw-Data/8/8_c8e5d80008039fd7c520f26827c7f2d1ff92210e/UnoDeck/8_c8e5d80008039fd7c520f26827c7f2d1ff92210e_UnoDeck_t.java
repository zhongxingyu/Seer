 /*   _______ __ __                    _______                    __   
  *  |     __|__|  |.--.--.-----.----.|_     _|.----.-----.--.--.|  |_ 
  *  |__     |  |  ||  |  |  -__|   _|  |   |  |   _|  _  |  |  ||   _|
  *  |_______|__|__| \___/|_____|__|    |___|  |__| |_____|_____||____|
  * 
  *  Copyright 2008 - Gustav Tiger, Henrik Steen and Gustav "Gussoh" Sohtell
  * 
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  * 
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  * 
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package silvertrout.commons.game;
 
 import java.util.LinkedList;
 /**
  *
  **
  */
 public class UnoDeck{
     private LinkedList<UnoCard> cards;
     private LinkedList<UnoCard> trash;
     public UnoDeck(){
         cards = new LinkedList<UnoCard>();
         trash = new LinkedList<UnoCard>();
         /* fill the deck with cards */
         try{
             for(int c = 0; c < 4; c++){
                 /* common cards */
                 for(int r = 0; r <= 9; r++){
                    for(int i = 0; i < 2; i++)
                         cards.add(new UnoCard(c,r));
                 }
                 /* r, s and dt */
                for(int i = 0; i < 2; i++)
                     for(int r = 10; r < 13; r++)
                         cards.add(new UnoCard(c,r));
             }
             /* WILD and WD4 */
             for(int i = 13; i <= 14; i++){
                for(int j = 0; j < 4; j++){
                     cards.add(new UnoCard(-1,i));
                 }
             }
             /* shuffle: */
             shuffle();
         }catch(Exception e){
             e.printStackTrace();
         }
 
     }
 
     public void shuffle(){
         java.util.Collections.shuffle(cards);
     }
 
     public UnoCard drawCard(){
         if(cards.isEmpty()){
             cards.addAll(trash);
             trash = new LinkedList<UnoCard>();
             for(UnoCard c: cards){
                 if(c.getRank() == 13 || c.getRank() == 14)
                     c.setColor(-1);
             }
             shuffle();
         }
         return cards.removeFirst();
     }
     public void throwCard(UnoCard c){
         trash.add(c);
     }
 }
