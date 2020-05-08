 package model;
 
 import java.util.*;
 
 /**
  * @author Bruno Bajtela
  */
 class SpielfeldImpl implements Spielfeld {
 
     List<Spieler> playerlist;
 
     HashMap<Spieler, PlayerColor> playercolors;
 
     HashMap<Spieler, FeldImpl> currentPos;
 
     FeldImpl firstPlayFeld;
 
     EnumMap<PlayerColor, FeldImpl> StartFields;
 
     EnumMap<PlayerColor, FeldImpl> FinishFields;
 
     EnumMap<PlayerColor, FeldImpl> FirstFields;
 
     // Erstellt ein neues Spielfeld. Erster Spieler ist gelb, zweiter grün, dritter rot, vierter blau.
     public SpielfeldImpl(List<Spieler> players) {
         if (players == null || players.size() < 2)
             throw new IllegalArgumentException("At least 2 players are required.");
 
         if (players.size() > 4)
             throw new IllegalArgumentException("More than 4 players are not supported.");
 
         playerlist = new ArrayList<Spieler>();
         playerlist.addAll(players);
 
         playercolors = new HashMap<Spieler, PlayerColor>();
         currentPos = new HashMap<Spieler, FeldImpl>();
         StartFields = new EnumMap<PlayerColor, FeldImpl>(PlayerColor.class);
         FinishFields = new EnumMap<PlayerColor, FeldImpl>(PlayerColor.class);
         FirstFields = new EnumMap<PlayerColor, FeldImpl>(PlayerColor.class);
 
 
         playercolors.put(playerlist.get(0), PlayerColor.Yellow);
         playercolors.put(playerlist.get(1), PlayerColor.Green);
 
         if (playerlist.size() > 2) {
             playercolors.put(playerlist.get(2), PlayerColor.Red);
         }
         if (playerlist.size() > 3) {
             playercolors.put(playerlist.get(3), PlayerColor.Blue);
         }
 
         //Generiere Startfelder.
         int count = 41;
         for (PlayerColor p : PlayerColor.values()) {
             StartFeld first = new StartFeld(count++, p);
 
             FeldImpl last = first;
             for (int i = 0; i < 3; i++) {
                 StartFeld next = new StartFeld(count++, p);
                 last.setNext(next);
                 last = next;
             }
 
             StartFields.put(p, first);
         }
         //Generiere Zielfelder.
 
         for (PlayerColor p : PlayerColor.values()) {
             ZielFeld first = new ZielFeld(count++, p);
 
             FeldImpl last = first;
             for (int i = 0; i < 3; i++) {
                 ZielFeld next = new ZielFeld(count++, p);
                 last.setNext(next);
                 last = next;
             }
 
             FinishFields.put(p, first);
 
         }
 
         /// Hauptfeld generieren
 
         count = 1;
         //Starte mit gelbem Firstfeld.
 
         firstPlayFeld = new FirstFeld(count++, PlayerColor.Yellow);
         FirstFields.put(PlayerColor.Yellow, firstPlayFeld);
 
         FeldImpl last = firstPlayFeld;
 
         // 8 regulaere Felder.
         for (int i = 0; i < 8; i++) {
             FeldImpl next = new FeldImpl(count++);
             last.setNext(next);
             last = next;
         }
         //Gruenes letztes Feld.
         LastFeld lf = new LastFeld(count++, PlayerColor.Green);
         lf.setNextFinish(FinishFields.get(PlayerColor.Green));
         last.setNext(lf);
         last = lf;
 
         //Gruenes startfeld.
         FirstFeld ff = new FirstFeld(count++, PlayerColor.Green);
         FirstFields.put(PlayerColor.Green, ff);
         last.setNext(ff);
         last = ff;
 
         // 8 regulaere Felder.
         for (int i = 0; i < 8; i++) {
             FeldImpl next = new FeldImpl(count++);
             last.setNext(next);
             last = next;
         }
 
         //Rotes letztes Feld.
         lf = new LastFeld(count++, PlayerColor.Red);
         lf.setNextFinish(FinishFields.get(PlayerColor.Red));
         last.setNext(lf);
         last = lf;
 
         //Rotes startfeld.
         ff = new FirstFeld(count++, PlayerColor.Red);
         FirstFields.put(PlayerColor.Red, ff);
         last.setNext(ff);
         last = ff;
 
         // 8 regulaere Felder.
         for (int i = 0; i < 8; i++) {
             FeldImpl next = new FeldImpl(count++);
             last.setNext(next);
             last = next;
         }
 
         //Blaues letztes Feld.
         lf = new LastFeld(count++, PlayerColor.Blue);
         lf.setNextFinish(FinishFields.get(PlayerColor.Blue));
         last.setNext(lf);
         last = lf;
 
         //Blaues startfeld.
         ff = new FirstFeld(count++, PlayerColor.Blue);
         FirstFields.put(PlayerColor.Blue, ff);
         last.setNext(ff);
         last = ff;
 
         // 8 regulaere Felder.
         for (int i = 0; i < 8; i++) {
             FeldImpl next = new FeldImpl(count++);
             last.setNext(next);
             last = next;
         }
 
         //Gelbes letztes Feld.
         lf = new LastFeld(count, PlayerColor.Yellow);
         lf.setNextFinish(FinishFields.get(PlayerColor.Yellow));
         last.setNext(lf);
         last = lf;
 
         //Verbinde gelbes letztes feld mit gelbem startfeld.
         last.setNext(firstPlayFeld);
 
         //Done!
 
         //Setze Spieler auf startfelder.
 
         resetPlayers();
 
 
     }
 
     private void resetPlayers() {
         for (Spieler p : playerlist) {
             if (!currentPos.containsKey(p))
                 currentPos.put(p, StartFields.get(playercolors.get(p)));
 
             resetPlayer(p);
         }
     }
 
 
     public Feld getNewField(Spieler player, Integer distance) {
         PlayerColor c = playercolors.get(player);
         if (c == null)
             throw new IllegalArgumentException("Invalid player.");
 
         FeldImpl current = currentPos.get(player);
 
         if (distance < 1)
             return current;
 
         if (!current.isRegularField())
             return FirstFields.get(c);
 
         while (distance > 0) {
             if (current.isLastField() && current.getOwner() == c) {
                 current = current.getNextFinish();
                 break;
             }
 
             current = current.getNext();
             distance--;
         }
 
         return current;
 
     }
 
     public void setPlayerToField(Spieler player, Feld field) {
         PlayerColor c = playercolors.get(player);
         if (c == null)
             throw new IllegalArgumentException("Invalid player.");
         if (field == null || !(field instanceof FeldImpl)) //Bad code, bessere Idee?
             throw new IllegalArgumentException("Invalid field.");
 
         if (field.getContent() != null)
             throw new IllegalArgumentException("Field already occupied!");
 
         FeldImpl current = currentPos.get(player);
         current.setContent(null);
 
 
         FeldImpl f = (FeldImpl) field;
 
         f.setContent(player);
         currentPos.put(player, f);
 
     }
 
     public void resetPlayer(Spieler player) {
         PlayerColor c = playercolors.get(player);
         if (c == null)
             throw new IllegalArgumentException("Invalid player.");
 
         FeldImpl f = StartFields.get(c);
 
         this.setPlayerToField(player, f);
 
 
     }
 
     public Integer distanceToFinish(Spieler player) {
         PlayerColor c = playercolors.get(player);
         if (c == null)
             throw new IllegalArgumentException("Invalid player.");
 
         FeldImpl current = currentPos.get(player);
 
         if (!current.isRegularField())
             return -1;
 
         int distance = 0;
 
         while (!(current.isLastField() && current.getOwner() == c)) {
             current = current.getNext();
             distance++;
         }
 
         return ++distance;
     }
 
     public Boolean isPlayerInStart(Spieler player) {
         PlayerColor c = playercolors.get(player);
         if (c == null)
             throw new IllegalArgumentException("Invalid player.");
 
         FeldImpl f = StartFields.get(c);
         return f.getContent() == player;
     }
 
     public Boolean isPlayerFinished(Spieler player) {
         PlayerColor c = playercolors.get(player);
         if (c == null)
             throw new IllegalArgumentException("Invalid player.");
 
         FeldImpl f = FinishFields.get(c);
         return f.getContent() == player;
     }
 
     protected List<Feld> iterateToEnd(FeldImpl start) {
         LinkedList<Feld> list = new LinkedList<Feld>();
 
         if (start == null)
             return list;
 
         FeldImpl f = start;
         list.add(f);
 
         FeldImpl i = f.getNext();
         list.add(i);
 
         while (i.hasNext() && i.getNext() != f) {
             i = i.getNext();
             list.add(i);
         }
         return list;
     }
 
     @Override
     public List<Feld> getPlayFields() {
         return iterateToEnd(firstPlayFeld);
     }
 
     @Override
     public List<Feld> getPlayer1StartFields() {
         return iterateToEnd(StartFields.get(PlayerColor.Yellow));
     }
 
     @Override
     public List<Feld> getPlayer2StartFields() {
         return iterateToEnd(StartFields.get(PlayerColor.Green));
     }
 
     @Override
     public List<Feld> getPlayer3StartFields() {
         return iterateToEnd(StartFields.get(PlayerColor.Red));
     }
 
     @Override
     public List<Feld> getPlayer4StartFields() {
         return iterateToEnd(StartFields.get(PlayerColor.Blue));
     }
 
     @Override
     public List<Feld> getPlayer1FinishFields() {
         return iterateToEnd(FinishFields.get(PlayerColor.Yellow));
     }
 
     @Override
     public List<Feld> getPlayer2FinishFields() {
         return iterateToEnd(FinishFields.get(PlayerColor.Green));
     }
 
     @Override
     public List<Feld> getPlayer3FinishFields() {
         return iterateToEnd(FinishFields.get(PlayerColor.Red));
     }
 
     @Override
     public List<Feld> getPlayer4FinishFields() {
         return iterateToEnd(FinishFields.get(PlayerColor.Blue));
     }
 
     protected int getPlayerNumber(Spieler p) {
         PlayerColor c = playercolors.get(p);
 
         if (c == null)
             return -1;
 
         return getColorNumber(c);
     }
 
     protected int getColorNumber(PlayerColor c) {
         if (c.equals(PlayerColor.Yellow))
             return 1;
         if (c.equals(PlayerColor.Green))
             return 2;
         if (c.equals(PlayerColor.Red))
             return 3;
         else
             return 4;
     }
 
 
     /////////////////Feld-Klassen
 
     protected class FeldImpl implements Feld {
         // Figur die momentan auf dem Feld steht.
         Spieler current;
 
         // Das naechste regulaere Spielfeld.
         FeldImpl next;
 
         //Die feldnummer
         int id;
 
         protected FeldImpl(int id) {
             current = null;
             next = null;
             this.id = id;
         }
 
         protected void setNext(FeldImpl f) {
             this.next = f;
         }
 
         protected FeldImpl getNext() {
             return this.next;
         }
 
         protected boolean hasNext() {
             return this.next != null;
         }
 
         protected void setNextFinish(FeldImpl f) {
             throw new UnsupportedOperationException("Basic Field has no finish field successor.");
         }
 
         protected FeldImpl getNextFinish() {
             throw new UnsupportedOperationException("Basic Field has no finish field successor.");
         }
 
         protected boolean hasNextFinish() {
             return false;
         }
 
         //Ist Feld auf dem Hauptspielplan oder ein Start/Zielfeld?
         protected boolean isRegularField() {
             return true;
         }
 
         protected void setOwner(PlayerColor player) {
             throw new UnsupportedOperationException("Basic Field has no owner.");
         }
 
         public PlayerColor getOwner() {
             throw new UnsupportedOperationException("Basic Field has no owner.");
         }
 
         @Override
         public String getIndex() {
             return "field" + id;
         }
 
         @Override
         public String getString() {
            String ret = "Feld " + id;
             if (isFirstField()) {
                 ret += ": erstes Feld Spieler ";
                 ret += getColorNumber(getOwner());
             } else if (isLastField()) {
                 ret += ": letztes Feld Spieler ";
                 ret += getColorNumber(getOwner());
             }
 
             if (getContent() != null) {
                 ret += ": Spieler ";
                 ret += getPlayerNumber(getContent());
             }
             return ret;
         }
 
         @Override
         public String getImgString() {
             String ret = "resources/img/field";
             if (isFirstField()) {
                 ret += getColorNumber(getOwner());
             }
             if (getContent() != null) {
                 ret += "_player";
                 ret += getPlayerNumber(getContent());
             }
             ret += ".png";
             return ret;
         }
 
 
         protected void setContent(Spieler player) {
             this.current = player;
         }
 
         public Spieler getContent() {
             return this.current;
         }
 
         public Boolean isFirstField() {
             return false;
         }
 
         public Boolean isLastField() {
             return false;
         }
 
 
     }
 
     // Start- und Zielfelder, haben Owner
     protected class NonRegularFeld extends FeldImpl {
         //Der Besitzer des Feldes, falls es ein startfeld, Zielfeld, erstes oder letztes Feld ist.
         PlayerColor owner;
 
         protected NonRegularFeld(int id, PlayerColor owner) {
             super(id);
             this.owner = owner;
         }
 
         @Override
         protected void setOwner(PlayerColor p) {
             this.owner = p;
         }
 
         @Override
         public PlayerColor getOwner() {
             return owner;
         }
 
     }
 
     //Erstes Feld
     protected class FirstFeld extends NonRegularFeld {
         protected FirstFeld(int id, PlayerColor owner) {
             super(id, owner);
         }
 
         @Override
         public Boolean isFirstField() {
             return true;
         }
     }
 
     //Letztes Feld, hat nextZielFeld
     protected class LastFeld extends NonRegularFeld {
         FeldImpl nextZielFeld;
 
         protected LastFeld(int id, PlayerColor owner) {
             super(id, owner);
             nextZielFeld = null;
         }
 
         @Override
         public Boolean isLastField() {
             return true;
         }
 
         @Override
         protected void setNextFinish(FeldImpl f) {
             nextZielFeld = f;
         }
 
         @Override
         protected FeldImpl getNextFinish() {
             return nextZielFeld;
         }
 
         @Override
         protected boolean hasNextFinish() {
             return nextZielFeld != null;
         }
     }
 
     //Start-und Zielfeld
     protected class ExtraFeld extends NonRegularFeld {
         protected ExtraFeld(int id, PlayerColor owner) {
             super(id, owner);
         }
 
         @Override
         protected boolean isRegularField() {
             return false;
         }
 
         @Override
         public String getImgString() {
             String ret = "resources/img/field";
             ret += getColorNumber(getOwner());
             if (getContent() != null) {
                 ret += "_player";
                 ret += getPlayerNumber(getContent());
             }
             ret += ".png";
             return ret;
         }
     }
 
     protected class StartFeld extends ExtraFeld {
         protected StartFeld(int id, PlayerColor owner) {
             super(id, owner);
         }
 
         @Override
         public String getString() {
            String ret = "Feld " + id + ": Startfeld Spieler ";
             ret += getColorNumber(getOwner());
 
 
             if (getContent() != null) {
                 ret += ": Spieler ";
                 ret += getPlayerNumber(getContent());
             }
 
             return ret;
         }
 
     }
 
     protected class ZielFeld extends ExtraFeld {
         protected ZielFeld(int id, PlayerColor owner) {
             super(id, owner);
         }
 
         @Override
         public String getString() {
            String ret = "Feld " + id + ": Zielfeld Spieler ";
             ret += getColorNumber(getOwner());
 
 
             if (getContent() != null) {
                 ret += ": Spieler ";
                 ret += getPlayerNumber(getContent());
             }
 
             return ret;
         }
 
     }
 
 }
