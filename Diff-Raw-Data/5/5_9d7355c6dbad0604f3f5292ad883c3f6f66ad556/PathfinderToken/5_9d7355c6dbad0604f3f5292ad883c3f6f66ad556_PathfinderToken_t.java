 package net.sozinsoft.tokenlab;
 
 
 import com.google.gson.*;
 import net.rptools.maptool.model.*;
 import net.sozinsoft.tokenlab.dtd.*;
 import net.sozinsoft.tokenlab.dtd.Character;
 import net.sozinsoft.tokenlab.dtd.Weapon;
 import org.xml.sax.SAXException;
 
 import java.io.IOException;
 import java.util.*;
 
 public class PathfinderToken implements ICharacter, ITokenizable {
 
 
     private static final String SIZE_MOD = "SizeMod";
     private static final String AC_TEMP_BONUS = "ACTempBonus";
     private static final String AC_MISC_BONUS_2 = "ACMiscBonus2";
     private static final String AC_MISC_BONUS_1 = "ACMiscBonus1";
     private static final String AC_FROM_DEFLECT = "ACDeflectBonus";
     private static final String AC_FROM_DODGE = "ACDodgeBonus";
     private static final String AC_SHIELD_BONUS = "ACShieldBonus";
     private static final String AC_ARMOR_BONUS = "ACArmorBonus";
     public static  final String AC_FROM_NATURAL = "ACNaturalBonus";
     private static final String ARMOR_MAX_DEX_BONUS = "ArmorMaxDexBonus";
     private static final String ARMOR_CHECK_PENALTY = "ArmorCheckPenalty";
     private static final String MISC_BONUS = "MiscBonus";
     private static final String DAMAGE = "damage";
     private static final String ENHANCEMENT = "Enhancement";
     private static final String CLASS_BONUS = "ClassBonus";
     private static final String RESIST_BONUS = "ResistBonus";
     private static final String CLASS_SKILL = "ClassSkill";
     private static final String PATHFINDER = "Pathfinder";
 
 
 
 
     private static final String INIT_MOD = "InitMod";
     private static MacroDigester macroDigester = null;
     public static final String VALUE = "value";
     public static final String VALUE_MODIFIER = "valueModifier";
     public static final String BONUS = "bonus";
     public static final String BONUS_MODIFIER = "bonusModifier";
     public static final String TEMP_MODIFIER = "tempModifier";
     public static final String PROFESSION = "Profession";
     public static final String YES = "yes";
     public static final String RANKS = "Ranks";
     public static final String WEAPON_JSON = "WeaponJSON";
     public static final String SPELLS_JSON = "SpellJSON";
     public static final String FEATS_JSON = "FeatJSON";
     public static final String SPELLBOOK = "Spellbook";
     public static final String MEMORIZED = "Memorized";
     public static final String SPONTANEOUS = "Spontaneous";
     public static final String SPECIAL_ABILITIES_JSON = "SpecialAbilitiesJSON";
     public static final String SENSES = "Senses";
     public static final String AURAS = "Auras";
     public static final String HEALTH = "Health";
     public static final String DAMAGE_REDUCTION = "Damage Reduction";
     public static final String DEFENSIVE = "Defensive";
     public static final String IMMUNITIES = "Immunities";
     public static final String WEAKNESSES = "Weaknesses";
     public static final String MOVEMENT = "Movement";
     public static final String SKILL_ABILITIES = "Skill Abilities";
     public static final String ATTACK = "Attack";
     public static final String SPELL_LIKE = "Spell-like";
     public static final String OTHER = "Other";
     public static final String TRAITS_JSON = "TraitsJSON";
     public static final String SKILLS_JSON = "SkillsJSON";
     public static final String RESOURCES_JSON = "ResourcesJSON";
     public static final String SPONTANEOUS_RESOURCES_JSON = "SpontaneousResourcesJSON";
     public static final String MEMORIZED_RESOURCES_JSON = "MemorizedResourcesJSON";
     public static final String YELLOW = "yellow";
     public static final String DARKGRAY = "darkgray";
     public static final String BEGIN_ROLL = "[r:";
     public static final String END_ROLL = "]";
     public static final String SPELL_REFERENCE_MACRO_NAME = "Spell Reference";
     public static final String SPONTANEOUS_SPELL_RESOURCES_MACRO_NAME = "Spontaneous Spell Resources";
     public static final String NPC = "npc";
     public static final String STANDARD_ATTACK_MACRO_NAME = "Standard Attack";
     public static final String ATTACK_FULL = "Attack - Full";
 
 
     private Character _character;
     private TokenLabToken _token;
     private HashMap<String, Object> _propertyMap = new HashMap<String, Object>();
 
     public PathfinderToken(Character character ) throws Exception {
         _token = new TokenLabToken();
         _character = character;
         _token.setCommonProperties( this, _propertyMap );
 
         setAbilities();
         setSavingThrows();
         setArmorClass();
         setSkills();
         setWeapons();
         setSpells();
         setFeats();
         setSpecialAbilities();
         setVision();
         setInitiative();
         setTrackedResources();
         setSpellResources();
     }
 
     private HashMap<String, HashMap<String, Object >> _trackedResources = new HashMap<String, HashMap<String, Object>>();
     private void setTrackedResources() {
         for( Trackedresource tr : _character.getTrackedresources().getTrackedresource() ) {
             String trName = StringUtils.removeCommas(tr.getName());
             int min  = Integer.parseInt( tr.getMin() );
             int max  = Integer.parseInt( tr.getMax() );
             int used = Integer.parseInt( tr.getUsed() );
             HashMap<String, Object> trMap = new HashMap<String, Object>();
             trMap.put( "min", min);
             trMap.put( "max", max );
             trMap.put( "used", used );
             _trackedResources.put( trName, trMap );
         }
     }
 
     private HashMap<String, WeaponImpl> _weapons = new HashMap<String, WeaponImpl>();
     public HashMap<String, WeaponImpl> getWeapons() {
         return _weapons;
     }
 
     private void setWeapons() throws Exception {
         for( Weapon weapon: this._character.getMelee().getWeapon() ) {
 
             if ( _weapons.containsKey( weapon.getName() )) {
                 weapon.setName( weapon.getName() + " (" + weapon.getEquipped() + ")");
             }
             WeaponImpl wimpl = new WeaponImpl(weapon.getName(), weapon.getDamage(), weapon.getCategorytext(),
                     weapon.getCrit(), weapon.getAttack(), weapon.getEquipped(),
                     weapon.getCategorytext(), weapon.getDescription());
             _weapons.put( wimpl.name, wimpl);
 
         }
 
         for( Weapon weapon: this._character.getRanged().getWeapon() ) {
 
             if ( _weapons.containsKey( weapon.getName() )) {
                 weapon.setName( weapon.getName() + "(" + weapon.getEquipped() + ")");
             }
             WeaponImpl wimpl = new WeaponImpl(weapon.getName(), weapon.getDamage(), weapon.getCategorytext(),
                     weapon.getCrit(), weapon.getAttack(), weapon.getEquipped(),
                     weapon.getCategorytext(), weapon.getDescription());
             _weapons.put( wimpl.name, wimpl);
 
         }
 
     }
 
     public HashMap<String, PFSRDSpell> getSpellsByClass( String className) {
 
         ClassSpells cs = _spells.get(className);
         HashMap<String, PFSRDSpell > ret = cs.getAllSpells();
         return ret;
     }
 
     public SortedMap< Integer, HashMap<String, PFSRDSpell>> getSpellsByClassAndLevel( String className ) {
         ClassSpells cs = _spells.get(className);
         SortedMap<Integer, HashMap<String, PFSRDSpell >> ret = cs.getAllSpellsByLevel();
         return ret;
     }
 
 
     private HashMap< String, Feat > _feats = new HashMap<String, Feat>();
     private HashMap< String, Trait> _traits = new HashMap<String, Trait>();
     public HashMap< String, Feat > getFeats() {
         return _feats;
     }
     public void setFeats() {
         for( Feat f : _character.getFeats().getFeat() ) {
             //replace newlines with html breaks for nicer printing
             _feats.put(f.getName().replaceAll(",", ""), f);
         }
         for ( Trait t : _character.getTraits().getTrait()) {
             _traits.put( t.getName().replaceAll(",", ""), t );
         }
     }
 
     private SortedMap< String, TreeMap<String, Special >> _specials = new TreeMap<String, TreeMap<String, Special>>();
 
     public  SortedMap< String, TreeMap<String, Special >> getSpecialAbilities() {
         return _specials;
     }
     public void setSpecial( String specialName, Special special ) {
         TreeMap< String, Special > smap = _specials.get( specialName );
         if ( smap == null ) {
             smap = new TreeMap<String, Special>();
             _specials.put( specialName, smap );
         }
         smap.put( special.getName(), special );
     }
 
     private String _vision;
     public void setVision() {
         Vision v = new Vision( _character.getSenses().getSpecial() );
         _vision = v.getVision();
     }
 
     private String _initMod;
     public void setInitiative() {
         String total    = StringUtils.replacePlus(_character.getInitiative().getTotal());
         String fromAttr = StringUtils.replacePlus(_character.getInitiative().getAttrtext());
         int iTotal      = Integer.parseInt(total);
         int iFromAttr   = Integer.parseInt(fromAttr);
         int iMod        = iTotal - iFromAttr;
         _initMod        = Integer.toString(iMod);
 
     }
 
     public void setSpecialAbilities() {
 
         for ( Special s: _character.getSenses().getSpecial() ) {
             setSpecial(SENSES, s );
         }
         for( Special s: _character.getAuras().getSpecial() ) {
             setSpecial(AURAS, s );
         }
         for( Special s: _character.getHealth().getSpecial() ) {
             setSpecial(HEALTH, s );
         }
         for( Special s: _character.getDamagereduction().getSpecial()) {
             setSpecial(DAMAGE_REDUCTION, s );
         }
         for( Special s: _character.getDefensive().getSpecial()) {
             setSpecial(DEFENSIVE, s );
         }
         for( Special s: _character.getImmunities().getSpecial() ) {
             setSpecial(IMMUNITIES, s );
         }
         for( Special s: _character.getWeaknesses().getSpecial() ) {
             setSpecial(WEAKNESSES, s );
         }
         for( Special s: _character.getMovement().getSpecial()) {
             setSpecial(MOVEMENT, s );
         }
         for( Special s: _character.getSkillabilities().getSpecial()) {
             setSpecial(SKILL_ABILITIES, s );
         }
         for( Special s: _character.getAttack().getSpecial() ) {
             setSpecial(ATTACK, s );
         }
         for( Special s: _character.getSpelllike().getSpecial() ) {
             setSpecial(SPELL_LIKE, s );
         }
         for( Special s: _character.getOtherspecials().getSpecial()) {
             setSpecial(OTHER, s );
         }
     }
 
 
 
     private void setSpellResources() {
         for ( Spellclass sc : _character.getSpellclasses().getSpellclass() ) {
             if ( sc.getSpells().equals("Spontaneous")) {
                 setSpontaneousSpellResources( sc );
             }
         }
         setMemorizedResources();
     }
 
 
     private SortedMap<Integer, LinkedList<HashMap<String, Object>>> _memorizedSpellResources = new TreeMap<Integer, LinkedList<HashMap<String, Object>>>() ;
 
     private void setMemorizedResources() {
         if ( _character.getSpellsmemorized().getSpell().size() > 0 ) {
             buildSpellResource(_memorizedSpellResources, _character.getSpellsmemorized().getSpell() );
         }
     }
 
     private void buildSpellResource( SortedMap<Integer, LinkedList<HashMap<String, Object>>> spellResourceCollection,
                                     List<Spell> spells ) {
         
         for( Spell s : spells ) {
 
             String name = s.getName();
             
             int numRepeats = StringUtils.isXN( name );
             if ( numRepeats > 0 ) {
                 name = StringUtils.removeXN( name );
             }
             else {
                  numRepeats = 1;
             }
             
             for( int i = 0; i < numRepeats; i++ ) {
             
                 boolean unlimited = s.getUnlimited() != null && s.getUnlimited().equals("yes");
 
                 int level = Integer.parseInt( s.getLevel() );
 
                 int dc = Integer.parseInt( s.getDc() );
                 String className = s.getClazz() == null || s.getClazz().length() == 0? _spellToClassMap.get(name) : s.getClazz();
 
                 LinkedList<HashMap<String, Object>> spellsAtLevel = spellResourceCollection.get( level );
                 if ( spellsAtLevel == null ) {
                     spellsAtLevel = new LinkedList<HashMap<String, Object>>();
                     spellResourceCollection.put( level, spellsAtLevel);
                 }
                 HashMap<String,Object> spellAtLevel = new HashMap<String, Object>();
                 spellAtLevel.put( "name", name );
                 spellAtLevel.put( "unlimited", unlimited? 1: 0 );
                 spellAtLevel.put( "level", level );
                 spellAtLevel.put( "className", className);
                 spellAtLevel.put( "dc", dc );
                 spellAtLevel.put( "used", 0 );
                 spellsAtLevel.add( spellAtLevel );
             }
         }
     }
 
     private SortedMap<Integer, LinkedList<HashMap<String, Object>>> _spontaneousSpellResources =
             new TreeMap<Integer, LinkedList<HashMap<String, Object>>>();
 
     private void  setSpontaneousSpellResources( Spellclass spellClass ) {
         String spellClassName = spellClass.getName();
         for( Spelllevel spellLevel : spellClass.getSpelllevel() ) {
             int spellLevelInt = Integer.parseInt( spellLevel.getLevel() );
             boolean unlimited = spellLevel.getUnlimited() != null && spellLevel.getUnlimited().equals("yes");
             if ( unlimited ) { //don't bother with unlimited resources
                 continue;
             }
 
             int maxCasts = Integer.parseInt(spellLevel.getMaxcasts());
             int used = Integer.parseInt( spellLevel.getUsed());
             HashMap<String, Object> hmap = new HashMap<String, Object>();
             hmap.put( "spellLevel", spellLevelInt );
             hmap.put( "maxCasts", maxCasts );
             hmap.put( "used", used );
             hmap.put( "spellClassName", spellClassName );
 
             Integer hkey = spellLevelInt;
             LinkedList<HashMap<String, Object>> spellResourcesByClass;
             if( _spontaneousSpellResources.containsKey(hkey)) {
                 spellResourcesByClass = _spontaneousSpellResources.get( hkey );
             }
             else {
                 spellResourcesByClass = new LinkedList<HashMap<String, Object>>();
                 _spontaneousSpellResources.put( hkey, spellResourcesByClass );
             }
             spellResourcesByClass.push( hmap );
         }
     }
 
 
     private HashMap<String, ClassSpells > _spells= new HashMap<String, ClassSpells>();
     private HashMap<String, HashMap<String, Object>> _trackedSpells = new HashMap<String, HashMap<String, Object>>();
 
     public void setSpells() {
 
         for(Spellclass sc : _character.getSpellclasses().getSpellclass() ) {
             ClassSpells cs = null;
             if ( _spells.containsKey(sc.getName())) {
                 cs = _spells.get(sc.getName());
             }
             else {
                 cs = new ClassSpells(sc.getName());
                 _spells.put( sc.getName(), cs );
             }
 
             if ( sc.getSpells().equals(SPELLBOOK) ) {
                 addClassSpell( _character.getSpellbook().getSpell(), cs );
             }
             else if ( sc.getSpells().equals(MEMORIZED)) {
                 addClassSpell( _character.getSpellsmemorized().getSpell(), cs );
             }
             else if ( sc.getSpells().equals(SPONTANEOUS)) {
                 addClassSpell(  _character.getSpellsknown().getSpell(), cs );
             }
             else {
                 System.out.println("Unknown spell class " + sc.getSpells() );
             }
         }
     }
     
     private HashMap<String, String> _spellToClassMap = new HashMap<String, String>();
 
     private void addClassSpell(List<Spell> spells, ClassSpells cs) {
         for( Spell s: spells ) {
             cs.addSpell(s);
             if ( ! _spellToClassMap.containsKey(s.getName() ) ) {
                 _spellToClassMap.put( s.getName(), cs.getClassName() );
             }
         }
     }
 
 
     private static HashMap<IAttribute, IAttributeAbbreviated > attributeAbbreviations =
             new HashMap<IAttribute, IAttributeAbbreviated>();
     static {
         attributeAbbreviations.put( IAttribute.Strength, IAttributeAbbreviated.STR );
         attributeAbbreviations.put( IAttribute.Dexterity, IAttributeAbbreviated.DEX );
         attributeAbbreviations.put( IAttribute.Constitution, IAttributeAbbreviated.CON );
         attributeAbbreviations.put( IAttribute.Wisdom, IAttributeAbbreviated.WIS );
         attributeAbbreviations.put( IAttribute.Intelligence, IAttributeAbbreviated.INT );
     }
     private static IAttributeAbbreviated getAbbreviatedAttribute( IAttribute ia ) {
         return attributeAbbreviations.get(ia);
     }
 
     private HashMap< IAttribute, HashMap<String, Object> > _attributes = new HashMap<IAttribute, HashMap<String, Object>>();
 
     private void setAbilities() {
 
         for( Attribute attribute : _character.getAttributes().getAttribute() ) {
             HashMap<String, Object> attribJSON = new HashMap<String, Object>();
             _attributes.put( IAttribute.valueOf( attribute.getName() ), attribJSON );
             //_propertyMap.put( attribute.getName(), Integer.parseInt( attribute.getAttrvalue().getBase()) );
 
             attribJSON.put(VALUE, Integer.parseInt( attribute.getAttrvalue().getBase() ) );
             attribJSON.put(VALUE_MODIFIER, Integer.parseInt( StringUtils.replacePlus(attribute.getAttrbonus().getBase() ) ) );
 
             int attrBonusModified = Integer.parseInt( StringUtils.replacePlus( attribute.getAttrvalue().getModified() ) );
             int attrBonusBase =       Integer.parseInt( StringUtils.replacePlus( attribute.getAttrbonus().getModified() ) );
 
             if ( attrBonusModified == attrBonusBase ) {
                 attribJSON.put(BONUS, new Integer( 0 ) );
                 attribJSON.put(BONUS_MODIFIER, new Integer( 0 ) );
               //  _propertyMap.put( getAbbreviatedEnhancementBonusName(attribute),
                 //                  new Integer( 0  ) );
 
             }
             else {
                 attribJSON.put( BONUS, new Integer( attrBonusModified ) );
                 attribJSON.put( BONUS_MODIFIER, new Integer( StringUtils.replacePlus( attribute.getAttrbonus().getModified())) );
 
                 //_propertyMap.put( getAbbreviatedEnhancementBonusName(attribute),
                   //                new Integer( attrBonusModified  ) );
             }
             //_propertyMap.put(getAttributeDamageKeyName(attribute), new Integer(damage) );
             attribJSON.put( DAMAGE, new Integer( 0 ) );
             attribJSON.put(TEMP_MODIFIER, new Integer( 0 ) );
         }
 
     }
 
     public Integer getBaseAbilityScore(IAttribute iattribute) {
         //return (Integer)getTokenProperties(attribute.name() );
         HashMap<String, Object> attribute = _attributes.get( iattribute );
         return  (Integer)attribute.get(VALUE);
     }
 
     public Integer getBaseAbilityModifier(IAttribute iattribute) {
         //return (Integer)getTokenProperties(attribute.name() );
         HashMap<String, Object> attribute = _attributes.get( iattribute );
         return  (Integer)attribute.get(VALUE_MODIFIER);
     }
 
 
     public Integer getBonusAbilityScore(IAttribute iattribute) {
         HashMap<String, Object> attribute = _attributes.get( iattribute );
         return  (Integer)attribute.get(BONUS);
     }
 
     public Integer getBonusAbilityModifier(IAttribute iattribute) {
         HashMap<String, Object> attribute = _attributes.get( iattribute );
         return  (Integer)attribute.get(BONUS_MODIFIER);
     }
 
     public Integer getAbilityDamage(IAttribute iattribute ) {
         HashMap<String, Object> attribute = _attributes.get( iattribute );
         return  (Integer)attribute.get(DAMAGE);
     }
 
 
     private void setArmorClass() {
         Armorclass ac = _character.getArmorclass();
         _propertyMap.put( AC_ARMOR_BONUS,  ac.getFromarmor().length() > 0 ?
                                            Integer.parseInt( StringUtils.replacePlus( ac.getFromarmor() ) ) :
                                            new Integer( 0 ) );
         _propertyMap.put( AC_SHIELD_BONUS, ac.getFromshield().length() > 0 ?
                                            Integer.parseInt( StringUtils.replacePlus( ac.getFromshield() ) ) :
                                            new Integer( 0 ) );
         _propertyMap.put( AC_FROM_DEFLECT, ac.getFromdeflect().length() > 0 ?
                                            Integer.parseInt( StringUtils.replacePlus( ac.getFromdeflect() ) ) :
                                            new Integer( 0 ) );
         _propertyMap.put( AC_FROM_DODGE,  ac.getFromdodge().length() > 0 ?
                                            Integer.parseInt( StringUtils.replacePlus( ac.getFromdodge() ) ) :
                                            new Integer( 0 ) );
         _propertyMap.put( AC_FROM_NATURAL, ac.getFromnatural().length() > 0 ?
                                            Integer.parseInt( StringUtils.replacePlus( ac.getFromnatural() ) ) :
                                            new Integer( 0 ) );
         _propertyMap.put( SIZE_MOD,        ac.getFromsize().length() > 0 ?
                                            Integer.parseInt( StringUtils.replacePlus( ac.getFromsize() ) ) :
                                            new Integer( 0 ) );
         _propertyMap.put( AC_MISC_BONUS_1, ac.getFrommisc().length() > 0 ?
                                            Integer.parseInt( StringUtils.replacePlus( ac.getFrommisc() ) ) :
                                            new Integer( 0 ) );
         _propertyMap.put( AC_MISC_BONUS_2, new Integer( 0  ) );
         _propertyMap.put( AC_TEMP_BONUS,   new Integer( 0 ) );
         _propertyMap.put( ARMOR_MAX_DEX_BONUS, new Integer( 9999 ) );
 
         for ( Penalty p : _character.getPenalties().getPenalty() ) {
             if (p.getName().equals( "Armor Check Penalty") ) {
                 _propertyMap.put( ARMOR_CHECK_PENALTY, Integer.parseInt( p.getValue() ) );
             }
             else if ( p.getName().equals( "Max Dex Bonus") ) {
                 _propertyMap.put( ARMOR_MAX_DEX_BONUS, Integer.parseInt(p.getValue()) );
             }
         }
     }
 
 
 
 
 
     private void setSavingThrows() {
         for(Save s : _character.getSaves().getSave() ) {
             _propertyMap.put( s.getAbbr() + CLASS_BONUS,  s.getBase().length() > 0 ?
                                                           Integer.parseInt( StringUtils.replacePlus( s.getBase() ) ) :
                                                           new Integer( 0 ) );
             _propertyMap.put( s.getAbbr() + RESIST_BONUS, s.getFromresist().length() > 0 ?
                                                           Integer.parseInt( StringUtils.replacePlus( s.getFromresist() ) ) :
                                                           new Integer( 0 ) );
             _propertyMap.put( s.getAbbr() + MISC_BONUS,  s.getFrommisc().length() > 0 ?
                                                           Integer.parseInt( StringUtils.replacePlus( s.getFrommisc() ) ) :
                                                           new Integer( 0 ) );
         }
     }
 
     //ICharacter methods
     public String getName() {
         return _character.getName();
     }
 
     public String getPlayer() {
         return _character.getPlayername();
     }
 
     public String getRace() {
         return _character.getRace().getName();
     }
 
     public String getAlignment() {
         return _character.getAlignment().getName();
     }
 
     public String getDeity() {
         return _character.getDeity().getName();
     }
 
     public String getGender() {
         return _character.getPersonal().getGender();
     }
 
     public Integer getAge() {
         return Integer.parseInt(_character.getPersonal().getAge());
     }
 
     public String getHeight() {
         return _character.getPersonal().getCharheight().getText(); //bug in herolabs output here - it has the weight as the value
     }
 
     public String getWeight() {
         return _character.getPersonal().getCharweight().getText();
 
     }
 
     public Integer getSpeed() {
         return Integer.parseInt(_character.getMovement().getSpeed().getValue());
     }
 
     public Integer getLevel() {
         return Integer.parseInt( _character.getClasses().getLevel() ) ;
     }
 
     public String getClassAbbreviation() {
         return _character.getClasses().getSummaryabbr();
     }
 
     public String getSize() {
         return _character.getSize().getName();
     }
 
     public Integer getClassHitpoints() {
         return Integer.parseInt( _character.getHealth().getHitpoints() );
     }
 
 
     public Integer getSavingThrowClassBonus(ISavingThrow ist) {
         return (Integer)_propertyMap.get( ist.toString() + CLASS_BONUS );
     }
 
     public Integer getMiscellaneousSavingThrowBonus(ISavingThrow ist) {
         return (Integer)_propertyMap.get( ist.toString() + MISC_BONUS );
     }
 
     public Integer getResistanceSavingThrowBonus(ISavingThrow ist) {
         return (Integer)_propertyMap.get( ist.toString() + RESIST_BONUS );
     }
 
     public Integer getACArmorBonus() {
         return (Integer)_propertyMap.get( AC_ARMOR_BONUS );
     }
 
     public Integer getACFromShield() {
         return (Integer)_propertyMap.get( AC_SHIELD_BONUS );
     }
 
     public Integer getACFromDeflect() {
         return (Integer)_propertyMap.get( AC_FROM_DEFLECT );
     }
 
     public Integer getACFromDodge() {
         return (Integer)_propertyMap.get( AC_FROM_DODGE );
     }
 
     public Integer getACFromnNatural() {
         return (Integer)_propertyMap.get( AC_FROM_NATURAL );
     }
 
     public Integer getACFromSize() {
         return (Integer)_propertyMap.get( SIZE_MOD );
     }
 
     public Integer getACMisc() {
         return (Integer)_propertyMap.get( AC_MISC_BONUS_1 );
     }
 
     public Integer getBaseAttackBonus() {
         return Integer.parseInt( StringUtils.replacePlus( _character.getAttack().getBaseattack() ) );
     }
 
     public String getVision() {
         return _vision;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
 
     private Object getTokenProperties( String key ) {
         return _propertyMap.get(key);
     }
 
     private class IntegerSerializer implements JsonSerializer<Integer>  {
         public JsonElement serialize(Integer integer, java.lang.reflect.Type type, JsonSerializationContext jsonSerializationContext) {
             return new JsonPrimitive(integer.toString() );
         }
     }
 
     private class SpellSerializer implements JsonSerializer<PFSRDSpell> {
        public JsonElement serialize(PFSRDSpell spell, java.lang.reflect.Type type, JsonSerializationContext jsonSerializationContext) {
             return spell.jsonFields();
         }
     }
 
     public Token asToken( Config.ConfigEntry configEntry ) throws Exception {
 
         _token.createToken( this, configEntry );
         _token.setCommonProperties( (ICharacter)this, _propertyMap );
 
         loadMacros(_token.getToken());
 
         //set all the token properties.
         _token.getToken().setPropertyType(PATHFINDER);
 
         //PC or NPC
         if ( _character.getRole().equals("npc") ) {
             _token.getToken().setType(Token.Type.NPC);
         }
         else {
             _token.getToken().setType(Token.Type.PC);
         }
 
 
         //do the attributes
         GsonBuilder gson = new GsonBuilder();
         gson.registerTypeAdapter( Integer.class, new IntegerSerializer() ) ;
         gson.registerTypeAdapter( PFSRDSpell.class, new SpellSerializer());
         Gson gjson = gson.create();
         for( IAttribute ia : IAttribute.values() ) {
             String json = gjson.toJson(_attributes.get(ia) );
             _token.getToken().setProperty(ia.toString(), json);
         }
 
         for( String propKey : _propertyMap.keySet() ) {
            Object propertyValue = _propertyMap.get(propKey);
            if ( propertyValue != null ) {
                _token.getToken().setProperty(propKey, propertyValue.toString());
            }
         }
 
 
         _token.getToken().setProperty(FEATS_JSON, gjson.toJson(_feats));
 
         _token.getToken().setProperty(TRAITS_JSON, gjson.toJson(_traits));
 
         _token.getToken().setProperty(WEAPON_JSON,  gjson.toJson( _weapons ));
 
         _token.getToken().setProperty(SKILLS_JSON, gjson.toJson(_skills));
 
         _token.getToken().setProperty(RESOURCES_JSON, gjson.toJson(_trackedResources ));
 
 
         //this is a bit tortured, because you can't use triply nested for loops in maptool macros
         //but it is what it is
         LinkedList<HashMap<String, Object>> spontaneousSpellResources = new LinkedList<HashMap<String,Object>>();
         for( Integer level : _spontaneousSpellResources.keySet()) {
             LinkedList<HashMap<String, Object>> spellsByLevel = _spontaneousSpellResources.get( level );
             for( HashMap<String, Object> spellByLevel : spellsByLevel ) {
                 spontaneousSpellResources.add( spellByLevel );
             }
         }
        _token.getToken().setProperty(SPONTANEOUS_RESOURCES_JSON, gjson.toJson( spontaneousSpellResources));
 
         _token.getToken().setProperty(MEMORIZED_RESOURCES_JSON, gjson.toJson( _memorizedSpellResources));
 
 
         //set the spells.  this needs to basically happen by class.
 
         HashMap< String, SortedMap< Integer, HashMap<String, PFSRDSpell>>> characterSpells =
                 new HashMap<String, SortedMap<Integer, HashMap<String, PFSRDSpell>>>();
         for(Spellclass sc : _character.getSpellclasses().getSpellclass() ) {
             SortedMap< Integer, HashMap<String, PFSRDSpell>> spellsByLevel = this.getSpellsByClassAndLevel( sc.getName() );
             characterSpells.put( sc.getName(), spellsByLevel );
         }
 
         _token.getToken().setProperty(SPELLS_JSON, gjson.toJson(characterSpells));
 
         _token.getToken().setProperty(SPECIAL_ABILITIES_JSON, gjson.toJson(_specials));
 
         //init mod
         _token.getToken().setProperty(INIT_MOD, _initMod);
 
         //vision
         _token.getToken().setHasSight(true);
         _token.getToken().setSightType(_vision);
 
         //resources
 
 
         return _token.getToken();
     }
 
 
 
 
     private HashMap<String, HashMap<String, String >> _skills = new HashMap<String, HashMap<String, String>>() ;
 
     private void setSkills() {
         for(  Skill s :  _character.getSkills().getSkill() ) {
             String mungedSkillName = mungeSkillName( s.getName() );
             HashMap<String, String> skillMap = new HashMap<String, String>();
             skillMap.put( "value", s.getValue());
             skillMap.put( "ranks", s.getRanks() );
             skillMap.put( "description", s.getDescription());
             skillMap.put( "skillBonus", "0" );
             skillMap.put( "attrBonus", s.getAttrbonus());
             skillMap.put( "attrName", s.getAttrname());
             skillMap.put( "classSkill", s.getClassskill() != null && s.getClassskill().equals(YES) ? "1" : "0");
             skillMap.put( "armorCheckPenalty", s.getArmorcheck() != null && s.getArmorcheck().equals(YES) ? "1" : "0");
             skillMap.put( "useTrainedOnly", s.getTrainedonly() != null && s.getTrainedonly().equals(YES) ? "1" : "0" );
             skillMap.put( "usable", s.getUsable() != null && s.getUsable().equals(YES) ? "1" : "0" );
             skillMap.put( "tools", s.getTools()  != null && s.getTools().equals(YES) ? "1" : "0" );
             _skills.put( mungedSkillName, skillMap );
         }
     }
 
 
     private static boolean isClassSkill(Skill s) {
         return s.getClassskill() != null && s.getClassskill().equals( YES ) == true;
     }
 
 
 
 
     private void loadMacros(Token t) throws IOException, SAXException {
 
         if ( macroDigester == null ) {
             macroDigester = new MacroDigester( ResourceManager.getMacroConfigFile().getAbsolutePath() );
             macroDigester.parseConfigFile();
         }
 
         List<MacroButtonProperties> macroButtonSet = new ArrayList<MacroButtonProperties>();
 
         //do the generic macros first.
 
 
         int index = 1;
 
         index = buildGenericMacros(macroButtonSet, index);
         index = buildWeaponMacros(macroButtonSet, index);
         index = buildSpecialAbilityMacros(macroButtonSet, index);
         index = buildSkillMacros(macroButtonSet, index);
         index = buildMemorizedMacros( macroButtonSet, index );
         buildSubMacros( macroButtonSet, index );
 
 
         t.getMacroNextIndex(); //TODO: this is a hack to create the underlying macroPropertiesMap hash table
         t.replaceMacroList(macroButtonSet);
 
 	}
 
     private int buildMemorizedMacros( List<MacroButtonProperties> macroButtonSet, int index) throws IOException {
         if ( _memorizedSpellResources.size() == 0 ) {
             return index;
         }
         HashMap<String, MacroDigester.MacroEntry > memorizedMacroSet = macroDigester.getGroup( "Special Abilities - Memorized Spells");
 
         MacroDigester.MacroEntry resetEntry = (MacroDigester.MacroEntry) memorizedMacroSet.values().toArray()[1];
         IMacroReplacer defaultReplacer = new DefaultReplacer();
         MacroButtonProperties defaultProperties = resetEntry.getMacroButtonProperties( index++, defaultReplacer );
         macroButtonSet.add( defaultProperties );
 
         MacroDigester.MacroEntry macroEntry = (MacroDigester.MacroEntry) memorizedMacroSet.values().toArray()[0];
         for( Integer level : _memorizedSpellResources.keySet() ) {
             IMacroReplacer memorizedReplacer = new MemorizedSpellReplacer( level );
             MacroButtonProperties properties = macroEntry.getMacroButtonProperties( index++, memorizedReplacer );
             properties.setLabel( level.toString() );
             macroButtonSet.add( properties );
         }
 
         return index;
     }
 
     private int buildSubMacros(List<MacroButtonProperties> macroButtonSet, int index) throws IOException {
         HashMap<String, MacroDigester.MacroEntry > submacros = macroDigester.getGroup( "SUBMACROS");
 
         IMacroReplacer defaultReplacer = new DefaultReplacer();
         for( MacroDigester.MacroEntry macroEntry : submacros.values()) {
 
             //this is kind of a hack.
             if ( macroEntry.name.indexOf("spell") == 0 ) {
                 if (isSpellCaster()) {
                     continue;
                 }
             }
 
             MacroButtonProperties properties = macroEntry.getMacroButtonProperties( index++, defaultReplacer );
             macroButtonSet.add( properties );
         }
         return index  ;
     }
 
     private String mungeSkillName( String skillName ) {
         return skillName.replaceAll( "\\s|\\(|\\)", "");
     }
 
     private int buildSkillMacros(List<MacroButtonProperties> macroButtonSet, int index) throws IOException {
 
        HashMap<String, MacroDigester.MacroEntry > skillMacros = macroDigester.getGroup( "Skills");
 
        for ( Skill s : _character.getSkills().getSkill() ) {
 
            String skillName = s.getName();
            String mungedSkillName = mungeSkillName( skillName);
 
             String attributeName   = s.getAttrname();
             String attribShortName = CharacterAttribute.getShortName( attributeName );
             SkillReplacer replacer = new SkillReplacer( mungedSkillName, attribShortName  );
             MacroDigester.MacroEntry macroEntry = skillMacros.get("Skill Check");
 
            boolean disableButton = false;
 
             if ( isClassSkill(s) ) {
                 macroEntry.buttonColor = YELLOW;
             }
             else if ( s.getTrainedonly().equals(YES ) ) {
                 macroEntry.buttonColor = DARKGRAY;
                 if ( s.getRanks().length() == 0 || s.getRanks().equals("0")) {
                     disableButton = true;
                 }
 
             } else {
                 macroEntry.buttonColor = "white";
             }
 
             //todo: refactor the below into the replacer interface if I ever do it somewhere else.
             if ( ! disableButton ) {
                 macroEntry.toolTip = BEGIN_ROLL + s.getRanks() + END_ROLL;
                 macroEntry.name = skillName;
                 MacroButtonProperties properties = macroEntry.getMacroButtonProperties( index++, replacer );
                 macroButtonSet.add( properties );
             }
         }
 
         return index;
 
     }
 
     private int buildGenericMacros(List<MacroButtonProperties> macroButtonSet, int index) throws IOException {
         index = buildMacros(macroButtonSet, "Basic", index);
         return index;
     }
 
     private int buildSpecialAbilityMacros(List<MacroButtonProperties> macroButtonSet, int index) throws IOException {
         index = buildMacros(macroButtonSet, "Special Abilities", index);
         return index;
     }
 
     private int buildMacros(List<MacroButtonProperties> macroButtonSet, String groupName, int index) throws IOException {
         HashMap<String, MacroDigester.MacroEntry > genericMacros = macroDigester.getGroup( groupName );
         IMacroReplacer defaultReplacer = new DefaultReplacer();
         for( MacroDigester.MacroEntry macroEntry : genericMacros.values()) {
 
             //kind of a small hack, special for spells
             //if the character doesn't have spells, don't bother creating a spell button
             //TODO: probably a smoother way to do this
             if ( macroEntry.name.equals(SPELL_REFERENCE_MACRO_NAME) ) {
                 if (isSpellCaster()) {
                     continue;
                 }
             }
             if ( macroEntry.name.equals(SPONTANEOUS_SPELL_RESOURCES_MACRO_NAME) ) {   //same for the above :-)
                 if (_spontaneousSpellResources.size() == 0 ) {
                     continue;
                 }
             }
             MacroButtonProperties properties = macroEntry.getMacroButtonProperties( index++, defaultReplacer );
             macroButtonSet.add( properties );
         }
         return index;
     }
 
     private boolean isSpellCaster() {
         return _character.getSpellclasses().getSpellclass().size() == 0;
     }
 
     private boolean isNPC()
     {
         return _character.getRole().equals(NPC);
     }
 
     private int buildWeaponMacros(List<MacroButtonProperties> macroButtonSet, int index) throws IOException {
 
         //next do the power macros
         int sortPrefix = 0;
         HashMap<String, MacroDigester.MacroEntry > powerMacros = macroDigester.getGroup("Fight");
         for( WeaponImpl wimpl : _weapons.values()) {
 
             MacroDigester.MacroEntry macroEntry = powerMacros.get(STANDARD_ATTACK_MACRO_NAME);
 
             if ( sortPrefix == 0 ) {
                 sortPrefix = Integer.parseInt(macroEntry.sortPrefix); //bootstrap the sortPrefix
             } else {
                 ++sortPrefix;
                 macroEntry.sortPrefix = new Integer( sortPrefix ).toString();
             }
 
             macroEntry.name = wimpl.name;
             MacroButtonProperties properties = macroEntry.getMacroButtonProperties( index++,
                                                                                     new WeaponNameReplacer( isNPC(), wimpl.name, new Integer(1) ) );
             macroButtonSet.add( properties );
 
             //and set all the full attack stuff
 
             if ( wimpl.numFullAttacks > 1 ) {
                 for ( Integer attackCount : wimpl.sortedAttacks() ) {
                     MacroDigester.MacroEntry fullAttackMacroEntry = powerMacros.get(ATTACK_FULL);
                     ++sortPrefix;
                     fullAttackMacroEntry.sortPrefix = new Integer( sortPrefix ).toString();
                     fullAttackMacroEntry.name = attackCount.toString();
                     MacroButtonProperties faProperties = fullAttackMacroEntry.getMacroButtonProperties( index++,
                                                                                     new WeaponNameReplacer( isNPC(), wimpl.name, attackCount ) );
                     macroButtonSet.add( faProperties );
 
                 }
             }
         }
 
         return index;
     }
 
 
 }
