 
 public class Pokemon implements Battleable {
     private Species species;
     private int level;
     private IVs ivs;
     private int ev_hp;
     private int ev_atk;
     private int ev_def;
     private int ev_spd;
     private int ev_spc;
     private int hp;
     private int atk;
     private int def;
     private int spd;
     private int spc;
     private int totalExp;
     private Moveset moves;
     private boolean wild;
     private boolean atkBadge = false;
     private boolean defBadge = false;
     private boolean spdBadge = false;
     private boolean spcBadge = false;
     
     //defaults to wild pokemon
     public Pokemon(Species s, int newLevel) {
         this(s, newLevel, true);
         setExpForLevel();
     }
     //default based off of species and level (works on all trainer pokemon, except leaders' tms)
     public Pokemon(Species s, int newLevel, boolean wild) {
         species = s;
         level = newLevel;
         ivs = new IVs();
         setZeroEVs();
         moves = Moveset.defaultMoveset(species, level, Settings.isRB);
         calculateStats();
         this.wild = wild;
         setExpForLevel();
     }    
     //will work for leaders
     public Pokemon(Species s, int newLevel, Moveset moves, boolean wild) {
         this(s, newLevel, wild);
         this.moves = moves;
         calculateStats();
         setExpForLevel();
     }    
     public Pokemon(Species s, int newLevel, IVs ivs, boolean wild) {
         species = s;
         level = newLevel;
         this.ivs = ivs;
         setZeroEVs();
         moves = Moveset.defaultMoveset(species, level, Settings.isRB);
         calculateStats();
         this.wild = wild;
         setExpForLevel();
     }
     public Pokemon(Species s, int newLevel, Moveset moves, IVs ivs, boolean wild) {
         species = s;
         level = newLevel;
         this.ivs = ivs;
         setZeroEVs();
         this.moves = moves;
         calculateStats();
         this.wild = wild;
         setExpForLevel();
     }
     
     //TODO constructor which accepts EVs
     public void setZeroEVs() {
         ev_hp = 0;
         ev_atk = 0;
         ev_def = 0;
         ev_spc = 0;
         ev_spd = 0;
     }
     
     //call this to update your stats
     //automatically called on level ups/rare candies, but not just from gaining stat EV
     private void calculateStats() {
         hp = calcHPWithIV(ivs.getHPIV());
         atk = calcAtkWithIV(ivs.getAtkIV());
         def = calcDefWithIV(ivs.getDefIV());
         spd = calcSpdWithIV(ivs.getSpdIV());
         spc = calcSpcWithIV(ivs.getSpcIV());
     }
     private int calcHPWithIV(int iv) {
         return (iv + species.getBaseHP() + (int) Math.sqrt(ev_hp)/8 + 50)*level/50 + 10;
     }
     private int calcAtkWithIV(int iv) {
         return (iv + species.getBaseAtk() + (int) Math.sqrt(ev_atk)/8)*level/50 + 5;
     }
     private int calcDefWithIV(int iv) {
         return (iv + species.getBaseDef() + (int) Math.sqrt(ev_def)/8)*level/50 + 5;
     }
     private int calcSpdWithIV(int iv) {
         return (iv + species.getBaseSpd() + (int) Math.sqrt(ev_spd)/8)*level/50 + 5;
     }
     private int calcSpcWithIV(int iv) {
         return (iv + species.getBaseSpc() + (int) Math.sqrt(ev_spc)/8)*level/50 + 5;
     }
 
     private void setExpForLevel() {
         totalExp = ExpCurve.lowestExpForLevel(species.getCurve(), level);
     }
     //TODO: EV setter
     
     public int getHP() {
         return hp;
     }
     //badge boosts
     public int getAtk() {
         return (int) (atkBadge ? 9 * atk /8 : atk);
     }
     public int getDef() {
         return (int) (defBadge ? 9 * def /8 : def);
     }
     public int getSpc() {
         return (int) (spcBadge ? 9 * spc /8 : spc);
     }
     public int getSpd() {
         return (int) (spdBadge ? 9 * spd /8 : spd);
     }
     //not affected by badge boosts
     public int getTrueAtk() {
         return atk;
     }
     public int getTrueDef() {
         return def;
     }
     public int getTrueSpc() {
         return spc;
     }
     public int getTrueSpd() {
         return spd;
     }
     public int getLevel() {
         return level;
     }
     public Species getSpecies() {
         return species;
     }
     public void setMoveset(Moveset m) {
         moves = m;
     }
     public Moveset getMoveset() {
         return moves;
     }
     public boolean isWild() {
         return wild;
     }
     public int getTotalExp() {
         return totalExp;
     }
     public int expGiven() {
         return (int) ((isWild() ? 1 : 1.5) * species.getKillExp() * level / 7);
     }
     
     
     public String toString() {
         String endl = Constants.endl;
         return levelName() + "" + " EXP Needed: " + expToNextLevel() + "/" + expForLevel() + endl +
                String.format("\tHP\tATK%s\tDEF%s\tSPD%s\tSPC%s", atkBadge ? "*" : "",
                        defBadge ? "*" : "", spdBadge ? "*" : "", spcBadge ? "*" : "") + endl +
                String.format("\t%s\t%s\t%s\t%s\t%s",getHP(),getAtk(),getDef(),getSpd(),getSpc()) + endl +
                String.format("IV\t%s\t%s\t%s\t%s\t%s", ivs.getHPIV(),ivs.getAtkIV(),
                              ivs.getDefIV(),ivs.getSpdIV(),ivs.getSpcIV()) + endl +
                String.format("EV\t%s\t%s\t%s\t%s\t%s", ev_hp,ev_atk,ev_def,ev_spd,ev_spc)
                + endl + moves.toString();    
     }
     
     //utility getters
     public String levelName() {
         return "L" + level + " " + getSpecies().getName();
     }
     public String pokeName() {
         return getSpecies().getName();
     }
     public String statsStr() {
         return String.format("%s/%s/%s/%s/%s",getHP(),getAtk(),getDef(),getSpd(),getSpc());
     }
     
     //experience methods
     //exp needed to get to next level
     public int expToNextLevel() {
         return ExpCurve.expToNextLevel(species.getCurve(), level, totalExp);
     }
     //total exp needed to get from this level to next level (no partial exp)
     public int expForLevel() {
         return ExpCurve.expForLevel(species.getCurve(), level);
     }
     //in game actions
     
     //gain num exp
     private void gainExp(int num) {
         totalExp += num;
         //update lvl if necessary
         while(expToNextLevel() <= 0 && level < 100) {
             level++;
             calculateStats();
         }
     }
     //gain stat exp from a pokemon of species s
     private void gainStatExp(Species s){
         ev_hp += s.getBaseHP();
         ev_atk += s.getBaseAtk();
         ev_def += s.getBaseDef();
         ev_spc += s.getBaseSpc();
         ev_spd += s.getBaseSpd();
     }
     
     @Override
     public void battle(Pokemon p) {
         //p is the one that gets leveled up
         //this is the one that dies like noob
         //be sure to gain EVs before the exp
         p.gainStatExp(this.getSpecies());
         p.gainExp(this.expGiven());
     }
     
     //gains from eating stat/level boosters
     public void eatRareCandy() {
         if(level < 100) {
             level++;
             setExpForLevel();
             calculateStats();
         }
     }
     public void eatHPUp() {
         if (ev_hp >= 25600)
             return;
         ev_hp += 2560;
         calculateStats();
     }
     public void eatProtein() {
         if (ev_atk >= 25600)
             return;
         ev_atk += 2560;
         calculateStats();
     }
     public void eatIron() {
         if (ev_def >= 25600)
             return;
         ev_def += 2560;
         calculateStats();
     }
     public void eatCalcium() {
         if (ev_spc >= 25600)
             return;
         ev_spc += 2560;
         calculateStats();
     }
     public void eatCarbos() {
        if (ev_hp >= 25600)
             return;
        ev_hp += 2560;
         calculateStats();
     }
     
     //TODO: proper evolution
     public void evolve(Species s) {
         species = s;
     }
     
     //badge get/set
     public boolean isAtkBadge() {
         return atkBadge;
     }
     public void setAtkBadge(boolean atkBadge) {
         this.atkBadge = atkBadge;
     }
     public boolean isDefBadge() {
         return defBadge;
     }
     public void setDefBadge(boolean defBadge) {
         this.defBadge = defBadge;
     }
     public boolean isSpdBadge() {
         return spdBadge;
     }
     public void setSpdBadge(boolean spdBadge) {
         this.spdBadge = spdBadge;
     }
     public boolean isSpcBadge() {
         return spcBadge;
     }
     public void setSpcBadge(boolean spcBadge) {
         this.spcBadge = spcBadge;
     }
     public void setAllBadges() {
         atkBadge = true;
         defBadge = true;
         spdBadge = true;
         spcBadge = true;
     }
     public void loseAllBadges() {
         atkBadge = false;
         defBadge = false;
         spdBadge = false;
         spcBadge = false;
     }
     
     //a printout of stat ranges given this pokemon's EVs (not IVs)
     public String statRanges() {
         int[] possibleHPs = new int[16];
         int[] possibleAtks = new int[16];
         int[] possibleDefs = new int[16];
         int[] possibleSpds = new int[16];
         int[] possibleSpcs = new int[16];
         for(int i = 0; i < 16; i++) {
             possibleHPs[i] = calcHPWithIV(i);
             possibleAtks[i] = calcAtkWithIV(i);
             possibleDefs[i] = calcDefWithIV(i);
             possibleSpds[i] = calcSpdWithIV(i);
             possibleSpcs[i] = calcSpcWithIV(i);
         }
         StringBuilder sb = new StringBuilder(levelName() + Constants.endl);
         sb.append("IV  |0   |1   |2   |3   |4   |5   |6   |7   |8   |9   |10  |11  |12  |13  |14  |15  " + Constants.endl + 
                   "------------------------------------------------------------------------------------" + Constants.endl);
         sb.append("HP  ");
         for(int i = 0; i < 16; i++) {
             sb.append(String.format("|%1$4s", possibleHPs[i]));  //pad left, length 4
         }
         sb.append(Constants.endl);
         sb.append("ATK ");
         for(int i = 0; i < 16; i++) {
             sb.append(String.format("|%1$4s", possibleAtks[i]));  //pad left, length 4
         }
         sb.append(Constants.endl);
         sb.append("DEF ");
         for(int i = 0; i < 16; i++) {
             sb.append(String.format("|%1$4s", possibleDefs[i]));  //pad left, length 4
         }
         sb.append(Constants.endl);
         sb.append("SPD ");
         for(int i = 0; i < 16; i++) {
             sb.append(String.format("|%1$4s", possibleSpds[i]));  //pad left, length 4
         }
         sb.append(Constants.endl);
         sb.append("SPC ");
         for(int i = 0; i < 16; i++) {
             sb.append(String.format("|%1$4s", possibleSpcs[i]));  //pad left, length 4
         }
         sb.append(Constants.endl);
         
         return sb.toString();
     }
 }
