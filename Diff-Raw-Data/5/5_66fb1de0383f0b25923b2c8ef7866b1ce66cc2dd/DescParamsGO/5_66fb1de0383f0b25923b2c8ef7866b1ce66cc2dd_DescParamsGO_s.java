 /*
  *  Copyright 2008, 2009, 2010, 2011:
  *   Tobias Fleig (tfg[AT]online[DOT]de),
  *   Michael Haas (mekhar[AT]gmx[DOT]de),
  *   Johannes Kattinger (johanneskattinger[AT]gmx[DOT]de)
  *
  *  - All rights reserved -
  *
  *
  *  This file is part of Centuries of Rage.
  *
  *  Centuries of Rage is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  Centuries of Rage is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with Centuries of Rage.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 
 package de._13ducks.cor.game;
 
 import java.util.ArrayList;
 import java.util.List;
 import de._13ducks.cor.game.ability.Ability;
 import de._13ducks.cor.graphics.GOGraphicsData;
 
 /**
  * Beschreibt alle Objekt-Einstellungen, die aus den descType-Dateien eingelesen werden.
  * Einem Objekt kann beim Erstellen diese Beschreibung gegeben werden, dann werden die Werte entsprechend gesetzt.
  * Ein nachträgliches Ändern ist nur im Rahmen von Upgrades möglich.
  * Verwaltet auch Abilitys & Grafikdaten
  * Es existieren getter und setter für alle Parameter.
  * Objekte, mit diesen Parametern erstellt wurden bekommen nachträgliche Änderungen nicht mit.
  *
  * Es existieren erweiterte Versionen für die Unterklassen von GameObjekt.
  */
 public class DescParamsGO {
 
     /**
      * Ein dieses Objekt beschreibender String, bei allen Objekten dieses Typs gleich
      * z.B. "Bogenschütze"
      */
     private String descName;
     /**
      * Die Rüstungsklasse diese Objekts
      */
     private int armorType;
     /**
      * Schaden gegen andere Rüstungsklassen in Prozent.
      * Default ist 100
      */
     private int[] damageFactors;
     /**
      * Aktuelle Lebensenergie dieses Objekts.
      * Normalerweise stirbt das Objekt bei <=0
      */
     private int hitpoints;
     /**
      * Maximale Lebensenergie dieses Objekts.
      */
     private int maxhitpoints;
     /**
      * Die ID-Nummer dieser Einheit.
      * Wird für Zuordnungen mehrerer Einheiten des gleichen Typs verwendet.
      */
     private int descTypeId;
     /**
      * Die Liste mit den Abilitys dieses Objekts.
      * Abilitys werden im Hud als anklickbare Knöpfe dargestellt.
      */
     private List<Ability> abilitys;
     /**
      * Die Sichtweite dieses Objekts im FoW.
      * Gemessen in eckigen Kreisen um das Zentrum, (sogut es ganzzahlig geht)
      */
     private int visrange;
     /**
      * Diese Anzahl von Millisekunden müssen mindestens zwischen 2 Schlägen/Schüssen vergehen.
      */
     private int fireDelay;
     /**
      * Basis-Schaden dieses Objekts.
      */
     private int damage;
     /**
      * Die Reichweite dieser Einheit.
      * Echte Entfernungsmessung zwischen(!) den Einheiten.
      * Nahkämpfer haben also etwa 0
      */
     private double range;
     /**
      * Geschossgeschwindigkeit.
      * 0 ist für Nahkämpfer, also Instant-Hit.
      */
     private int bulletspeed = 15;
     /**
      * Geschosstextur. Nahkämpfer haben keine.
      */
     private String bullettexture;
     /**
      * Delay in Millisekunden zwischen dem Beginn eines Angriffs ("ausholen") und dem "zuschlagen". In dieser Zeit wird die Angriffsanimation abgespielt und die Einheit von der Grafikengine etwas vor bewegt. (nur Nahkampf)
      */
     private int atkdelay = 0;
     /**
      * Enthält alle Grafik- & Animationsdaten
      */
     private GOGraphicsData graphicsData;
     /**
      * Ein beschreibender String für GUI-Infos.
      * z.B. "Ein langsamer aber zäher Kämpfer"
      */
     private String descDescription;
     /**
      * Ein beschreibender String für GUI-Infos.
      * Sagt, gegen was dieses Objekt besonders stark ist.
      * Optional
      * z.B. "Light Infantry"
      */
     private String descPro;
     /**
      * Ein beschreibender String für GUI-Infos.
      * Sagt, gegen was dieses Objekt besonders schwach ist.
      * Optional
      * z.B. "Heavy Infantry"
      */
     private String descCon;
     /**
      * Heilrate dieses Objekts.
      * Mit dieser Rate heilt es andere in seiner Nähe, nicht sich selbst(!)
      */
     private int healRate = 0;
 
     public DescParamsGO() {
         damageFactors = new int[7];
         abilitys = new ArrayList<Ability>();
         graphicsData = new GOGraphicsData();
     }

     /**
      * Ein dieses Objekt beschreibender String, bei allen Objekten dieses Typs gleich
      * z.B. "Bogenschütze"
      * @return the descName
      */
     public String getDescName() {
         return descName;
     }
 
     /**
      * Ein dieses Objekt beschreibender String, bei allen Objekten dieses Typs gleich
      * z.B. "Bogenschütze"
      * @param descName the descName to set
      */
     public void setDescName(String descName) {
         this.descName = descName;
     }
 
     /**
      * Die Rüstungsklasse diese Objekts
      * @return the armorType
      */
     public int getArmorType() {
         return armorType;
     }
 
     /**
      * Die Rüstungsklasse diese Objekts
      * @param armorType the armorType to set
      */
     public void setArmorType(int armorType) {
         this.armorType = armorType;
     }
 
     /**
      * Schaden gegen andere Rüstungsklassen in Prozent.
      * Default ist 100
      * @return the damageFactors
      */
     public int[] getDamageFactors() {
         return damageFactors;
     }
 
     /**
      * Schaden gegen andere Rüstungsklassen in Prozent.
      * Default ist 100
      * @param damageFactors the damageFactors to set
      */
     public void setDamageFactors(int[] damageFactors) {
         this.damageFactors = damageFactors;
     }
 
     /**
      * Aktuelle Lebensenergie dieses Objekts.
      * Normalerweise stirbt das Objekt bei <=0
      * @return the hitpoints
      */
     public int getHitpoints() {
         return hitpoints;
     }
 
     /**
      * Aktuelle Lebensenergie dieses Objekts.
      * Normalerweise stirbt das Objekt bei <=0
      * @param hitpoints the hitpoints to set
      */
     public void setHitpoints(int hitpoints) {
         this.hitpoints = hitpoints;
     }
 
     /**
      * Maximale Lebensenergie dieses Objekts.
      * @return the maxhitpoints
      */
     public int getMaxhitpoints() {
         return maxhitpoints;
     }
 
     /**
      * Maximale Lebensenergie dieses Objekts.
      * @param maxhitpoints the maxhitpoints to set
      */
     public void setMaxhitpoints(int maxhitpoints) {
         this.maxhitpoints = maxhitpoints;
     }
 
     /**
      * Die ID-Nummer dieser Einheit.
      * Wird für Zuordnungen mehrerer Einheiten des gleichen Typs verwendet.
      * @return the descTypeId
      */
     public int getDescTypeId() {
         return descTypeId;
     }
 
     /**
      * Die ID-Nummer dieser Einheit.
      * Wird für Zuordnungen mehrerer Einheiten des gleichen Typs verwendet.
      * @param descTypeId the descTypeId to set
      */
     public void setDescTypeId(int descTypeId) {
         this.descTypeId = descTypeId;
     }
 
     /**
      * Die Liste mit den Abilitys dieses Objekts.
      * Abilitys werden im Hud als anklickbare Knöpfe dargestellt.
      * @return the abilitys
      */
     public List<Ability> getAbilitys() {
         return abilitys;
     }
 
     /**
      * Die Liste mit den Abilitys dieses Objekts.
      * Abilitys werden im Hud als anklickbare Knöpfe dargestellt.
      * @param abilitys the abilitys to set
      */
     public void setAbilitys(List<Ability> abilitys) {
         this.abilitys = abilitys;
     }
 
     /**
      * Die Sichtweite dieses Objekts im FoW.
      * Gemessen in eckigen Kreisen um das Zentrum, (sogut es ganzzahlig geht)
      * @return the visrange
      */
     public int getVisrange() {
         return visrange;
     }
 
     /**
      * Die Sichtweite dieses Objekts im FoW.
      * Gemessen in eckigen Kreisen um das Zentrum, (sogut es ganzzahlig geht)
      * @param visrange the visrange to set
      */
     public void setVisrange(int visrange) {
         this.visrange = visrange;
     }
 
     /**
      * Diese Anzahl von Millisekunden müssen mindestens zwischen 2 Schlägen/Schüssen vergehen.
      * @return the fireDelay
      */
     public int getFireDelay() {
         return fireDelay;
     }
 
     /**
      * Diese Anzahl von Millisekunden müssen mindestens zwischen 2 Schlägen/Schüssen vergehen.
      * @param fireDelay the fireDelay to set
      */
     public void setFireDelay(int fireDelay) {
         this.fireDelay = fireDelay;
     }
 
     /**
      * Basis-Schaden dieses Objekts.
      * @return the damage
      */
     public int getDamage() {
         return damage;
     }
 
     /**
      * Basis-Schaden dieses Objekts.
      * @param damage the damage to set
      */
     public void setDamage(int damage) {
         this.damage = damage;
     }
 
     /**
      * Die Reichweite dieser Einheit.
      * Echte Entfernungsmessung zwischen(!) den Einheiten.
      * Nahkämpfer haben also etwa 0
      * @return the range
      */
     public double getRange() {
         return range;
     }
 
     /**
      * Die Reichweite dieser Einheit.
      * Echte Entfernungsmessung zwischen(!) den Einheiten.
      * Nahkämpfer haben also etwa 0
      * @param range the range to set
      */
     public void setRange(double range) {
         this.range = range;
     }
 
     /**
      * Geschossgeschwindigkeit.
      * 0 ist für Nahkämpfer, also Instant-Hit.
      * @return the bulletspeed
      */
     public int getBulletspeed() {
         return bulletspeed;
     }
 
     /**
      * Geschossgeschwindigkeit.
      * 0 ist für Nahkämpfer, also Instant-Hit.
      * @param bulletspeed the bulletspeed to set
      */
     public void setBulletspeed(int bulletspeed) {
         this.bulletspeed = bulletspeed;
     }
 
     /**
      * Geschosstextur. Nahkämpfer haben keine.
      * @return the bullettexture
      */
     public String getBullettexture() {
         return bullettexture;
     }
 
     /**
      * Geschosstextur. Nahkämpfer haben keine.
      * @param bullettexture the bullettexture to set
      */
     public void setBullettexture(String bullettexture) {
         this.bullettexture = bullettexture;
     }
 
     /**
      * Delay in Millisekunden zwischen dem Beginn eines Angriffs ("ausholen") und dem "zuschlagen". In dieser Zeit wird die Angriffsanimation abgespielt und die Einheit von der Grafikengine etwas vor bewegt. (nur Nahkampf)
      * @return the atkdelay
      */
     public int getAtkdelay() {
         return atkdelay;
     }
 
     /**
      * Delay in Millisekunden zwischen dem Beginn eines Angriffs ("ausholen") und dem "zuschlagen". In dieser Zeit wird die Angriffsanimation abgespielt und die Einheit von der Grafikengine etwas vor bewegt. (nur Nahkampf)
      * @param atkdelay the atkdelay to set
      */
     public void setAtkdelay(int atkdelay) {
         this.atkdelay = atkdelay;
     }
 
     /**
      * Enthält alle Grafik- & Animationsdaten
      * @return the graphicsData
      */
     public GOGraphicsData getGraphicsData() {
         return graphicsData;
     }
 
     /**
      * Enthält alle Grafik- & Animationsdaten
      * @param graphicsData the graphicsData to set
      */
     public void setGraphicsData(GOGraphicsData graphicsData) {
         this.graphicsData = graphicsData;
     }
 
     /**
      * Ein beschreibender String für GUI-Infos.
      * z.B. "Ein langsamer aber zäher Kämpfer"
      * @return the descDescription
      */
     public String getDescDescription() {
         return descDescription;
     }
 
     /**
      * Ein beschreibender String für GUI-Infos.
      * z.B. "Ein langsamer aber zäher Kämpfer"
      * @param descDescription the descDescription to set
      */
     public void setDescDescription(String descDescription) {
         this.descDescription = descDescription;
     }
 
     /**
      * Ein beschreibender String für GUI-Infos.
      * Sagt, gegen was dieses Objekt besonders stark ist.
      * Optional
      * z.B. "Light Infantry"
      * @return the descPro
      */
     public String getDescPro() {
         return descPro;
     }
 
     /**
      * Ein beschreibender String für GUI-Infos.
      * Sagt, gegen was dieses Objekt besonders stark ist.
      * Optional
      * z.B. "Light Infantry"
      * @param descPro the descPro to set
      */
     public void setDescPro(String descPro) {
         this.descPro = descPro;
     }
 
     /**
      * Ein beschreibender String für GUI-Infos.
      * Sagt, gegen was dieses Objekt besonders schwach ist.
      * Optional
      * z.B. "Heavy Infantry"
      * @return the descCon
      */
     public String getDescCon() {
         return descCon;
     }
 
     /**
      * Ein beschreibender String für GUI-Infos.
      * Sagt, gegen was dieses Objekt besonders schwach ist.
      * Optional
      * z.B. "Heavy Infantry"
      * @param descCon the descCon to set
      */
     public void setDescCon(String descCon) {
         this.descCon = descCon;
     }
 
     /**
      * Heilrate dieses Objekts.
      * Mit dieser Rate heilt es andere in seiner Nähe, nicht sich selbst(!)
      * @return the healRate
      */
     public int getHealRate() {
         return healRate;
     }
 
     /**
      * Heilrate dieses Objekts.
      * Mit dieser Rate heilt es andere in seiner Nähe, nicht sich selbst(!)
      * @param healRate the healRate to set
      */
     public void setHealRate(int healRate) {
         this.healRate = healRate;
     }
 
 }
