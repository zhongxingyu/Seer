 /* TeamFileParser.java
  *
  * Created April 8, 2009
  *
  * This file is a part of Shoddy Battle.
  * Copyright (C) 2009  Catherine Fitzpatrick and Benjamin Gwin
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Affero General Public License
  * as published by the Free Software Foundation; either version 3
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program; if not, visit the Free Software Foundation, Inc.
  * online at http://gnu.org.
  */
 
 package shoddybattleclient.utils;
 
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 import org.xml.sax.Attributes;
 import shoddybattleclient.shoddybattle.Generation;
 import shoddybattleclient.shoddybattle.Pokemon;
 import shoddybattleclient.shoddybattle.Pokemon.Gender;
 import shoddybattleclient.shoddybattle.PokemonNature;
 import shoddybattleclient.shoddybattle.PokemonSpecies;
 
 /**
  * This class parses the XML Shoddy Battle 2 team format, as well as the
  * terrible Shoddy Battle 1 binary team format crafted by Catherine
  *
  * @author ben
  */
 public class TeamFileParser extends DefaultHandler {
     /** Constants used in the Java file format. **/
     private final static short STREAM_MAGIC = (short)0xaced;
     private final static short STREAM_VERSION = 5;
     private final static char TC_NULL = (char)0x70;
     private final static char TC_REFERENCE = (char)0x71;
     private final static char TC_CLASSDESC = (char)0x72;
     private final static char TC_OBJECT = (char)0x73;
     private final static char TC_STRING = (char)0x74;
     private final static char TC_ARRAY = (char)0x75;
     private final static char TC_CLASS = (char)0x76;
     private final static char TC_BLOCKDATA = (char)0x77;
     private final static char TC_ENDBLOCKDATA = (char)0x78;
     private final static char TC_RESET = (char)0x79;
     private final static char TC_BLOCKDATALONG = (char)0x7A;
     private final static char TC_EXCEPTION = (char)0x7B;
     private final static char TC_LONGSTRING = (char) 0x7C;
     private final static char TC_PROXYCLASSDESC = (char) 0x7D;
     private final static int baseWireHandle = 0x7E0000;
     private final static char SC_WRITE_METHOD = 0x01;
     private final static char SC_BLOCK_DATA = 0x08;
     private final static char SC_SERIALIZABLE = 0x02;
     private final static char SC_EXTERNALIZABLE = 0x04;
 
     private List<Pokemon> m_pokemon = new ArrayList<Pokemon>();
 
     private Pokemon tempPoke;
     private String tempStr;
     private int moveIndex;
 
     public Pokemon[] parseTeam(String file, Generation generation) {
         m_pokemon = new ArrayList<Pokemon>();
         DataInputStream is = null;
         try {
             try {
                 is = new DataInputStream(new FileInputStream(file));
                 short magic = is.readShort();
                 boolean sb1 = true;
                 if (magic != STREAM_MAGIC) {
                     sb1 = false;
                 }
                 short version = is.readShort();
                 if (version != STREAM_VERSION) {
                     sb1 = false;
                 }
                 Pokemon[] team = (sb1) ? parseShoddyBattle1Team(is) :
                         parseShoddyBattle2Team(file);
                 if (team.length == 0) return null;
                 for (Pokemon p : team) {
                     List<String> moves = new ArrayList<String>();
                     List<Integer> ppUps = new ArrayList<Integer>();
                     for (int i = 0; i < p.moves.length; i++) {
                         String m = p.moves[i];
                         if (m != null) {
                             moves.add(m);
                             ppUps.add(p.ppUps[i]);
                         }
                     }
                     p.moves = moves.toArray(new String[moves.size()]);
                     Integer[] temp = ppUps.toArray(new Integer[ppUps.size()]);
                     int[] temp2 = new int[temp.length];
                     for (int i = 0; i < temp.length; i++) {
                         temp2[i] = temp[i];
                     }
                     p.ppUps = temp2;
 
                     PokemonSpecies ps = generation.getSpeciesByName(p.species);
 
                     // Set the species to the case-sensitive version
                     p.species = ps.getName();
 
                     // If the pokemon has no ability  put in a default one
                     if (p.ability == null) {
                         p.ability = ps.getAbilities()[0];
                     }
 
                     // If the pokemon has no gender put in a default one
                     if (p.gender == null) {
                         if (ps.getGenders() == Gender.GENDER_BOTH) {
                             p.gender = Gender.GENDER_MALE;
                         } else {
                             p.gender = ps.getGenders();
                         }
                     }
                 }
                 return team;
             } catch (Exception e) {
                 e.printStackTrace();
                 return null;
             } finally {
                 if (is != null) {
                     is.close();
                 }
             }
         } catch (IOException e) {
             e.printStackTrace();
             return null;
         }
     }
 
     private Pokemon[] parseShoddyBattle2Team(String file) {
         SAXParserFactory spf = SAXParserFactory.newInstance();
         Pokemon[] ret;
         try {
            FileInputStream stream = new FileInputStream(file);
             SAXParser sp = spf.newSAXParser();
            sp.parse(stream, this);
         } catch (Exception e) {
             ret = null;
         }
         ret = new Pokemon[m_pokemon.size()];
         m_pokemon.toArray(ret);
         return ret;
     }
 
     @Override
     public void startElement (String uri, String localName,
 			      String qName, Attributes attributes) throws SAXException {
         if (qName.equals("pokemon")) {
             tempPoke = new Pokemon();
             tempPoke.species = attributes.getValue("species");
             moveIndex = 0;
         } else if (qName.equals("move")) {
             try {
                 tempPoke.ppUps[moveIndex] = Integer.parseInt(attributes.getValue("pp-up"));
             } catch (NumberFormatException e) {
                 tempPoke.ppUps[moveIndex] = 3;
             }
         } else if (qName.equals("stat")) {
             int statIndex = getStatIndex(attributes.getValue("name"));
             try {
                 tempPoke.ivs[statIndex] = Integer.parseInt(attributes.getValue("iv"));
             } catch (NumberFormatException e) {
                 tempPoke.ivs[statIndex] = 31;
             }
             try {
                 tempPoke.evs[statIndex] = Integer.parseInt(attributes.getValue("ev"));
             } catch (NumberFormatException e) {
                 tempPoke.evs[statIndex] = 0;
             }
         } else if (qName.equals("shiny")) {
             tempPoke.shiny = true;
         }
         tempStr = "";
 
     }
 
     @Override
     public void characters(char[] ch, int start, int length) throws SAXException {
         String addend = new String(ch, start, length);
         tempStr += addend;
     }
     
     @Override
     public void endElement(String uri, String localName, String qName) throws SAXException {
         if (qName.equals("nickname")) {
             tempPoke.nickname = tempStr;
         } else if (qName.equals("level")) {
             try {
                 tempPoke.level = Integer.parseInt(tempStr);
             } catch (NumberFormatException e) {
                 tempPoke.level = 100;
             }
         } else if (qName.equals("gender")) {
             if (tempStr.equals("Male")) {
                 tempPoke.gender = Gender.GENDER_MALE;
             } else if (tempStr.equals("Female")) {
                 tempPoke.gender = Gender.GENDER_FEMALE;
             } else {
                 tempPoke.gender = Gender.GENDER_NONE;
             }
         } else if (qName.equals("nature")) {
             tempPoke.nature = tempStr.trim();
         } else if (qName.equals("item")) {
             tempPoke.item = tempStr.trim();
         } else if (qName.equals("ability")) {
             tempPoke.ability = tempStr.trim();
         } else if (qName.equals("move")) {
             tempPoke.moves[moveIndex] = tempStr.trim();
             moveIndex++;
         } else if (qName.equals("pokemon")) {
             m_pokemon.add(tempPoke);
         } else if (qName.equals("happiness")) {
             try {
                 tempPoke.happiness = Integer.valueOf(tempStr);
             } catch (NumberFormatException e) {
                 tempPoke.happiness = 255;
             }
         }
     }
 
     private int getStatIndex(String s) {
         for (int i = 0; i < Pokemon.STAT_COUNT; i++) {
             if (s.equals(Pokemon.getStatName(i))) {
                 return i;
             }
         }
         return -1;
     }
 
     private int m_handle;
     private Map<Integer, StreamObject> m_objects;
     //translate between Shoddy Battle 1 and 2 nature IDs (unused)
     private int[] m_natures = { 1, 2, 3, 4, 5, 7, 8, 9, 10, 11,
     13, 14, 15, 16, 17, 19, 20, 21, 22, 23, 24, 0, 12, 18, 6 };
 
     private Pokemon[] parseShoddyBattle1Team(DataInputStream is) throws IOException {
         m_handle = -1;
         m_objects = new HashMap<Integer, StreamObject>();
         // uuid string
         readObject(is);
         // pokemon array
         readObject(is);
         return m_pokemon.toArray(new Pokemon[m_pokemon.size()]);
     }
 
     public class StreamObject {
         public int type;
         public int handle;
     }
 
     public class Field {
         public boolean endBlockData;
         public boolean object;
         public char typeCode;
         public String name;
         public String type;
     }
 
     public class StreamClassDesc extends StreamObject {
         public String name;
         public long uid;
         public char flags;
         public List<Field> fields = new ArrayList<Field>();
         StreamClassDesc superclass;
     }
 
     public class StreamArray extends StreamObject {
         public StreamClassDesc pClassDesc;
         public List<StreamObject> elements = new ArrayList<StreamObject>();
         public List<Integer> intElements = new ArrayList<Integer>();
         public boolean objects;
     }
 
     public class StreamString extends StreamObject {
         public String data;
     }
 
     public class StreamNature extends StreamObject {
         public int nature;
     }
 
     private StreamObject readObject(DataInputStream is) throws IOException {
         char flag = (char)is.read();
         switch (flag) {
             case TC_OBJECT:
                 return readNewObject(is);
             case TC_CLASS:
                 break;
             case TC_ARRAY:
                 StreamClassDesc desc = readClassDesc(is);
                 StreamArray ret = new StreamArray();
                 ret.type = TC_ARRAY;
                 ret.handle = newHandle();
                 int size = is.readInt();
                 char code = desc.name.charAt(1);
                 ret.objects = (code == 'L');
                 for (int i = 0; i < size; i++) {
                     if (ret.objects) {
                         StreamObject p = readObject(is);
                         ret.elements.add(p);
                     } else if (code == 'I') {
                         int datum = is.readInt();
                         ret.intElements.add(datum);
                     }
                 }
                 return storeObject(ret);
             case TC_STRING:
                 StreamString retString = new StreamString();
                 retString.type = TC_STRING;
                 retString.handle = newHandle();
                 retString.data = is.readUTF();
                 return storeObject(retString);
             case TC_CLASSDESC:
                 return readNewClassDesc(is);
             case TC_PROXYCLASSDESC:
                 break;
             case TC_REFERENCE:
                 return readPrevObject(is);
             case TC_NULL:
                 return null;
             case TC_BLOCKDATA:
                 size = is.read();
                 for (int i = 0; i < size; i++) {
                     is.read();
                 }
                 break;
         }
         return null;
     }
 
     private StreamObject readNewObject(DataInputStream is) throws IOException {
         StreamClassDesc desc = readClassDesc(is);
         int handle = newHandle();
         if (desc != null) {
             if (desc.name.equals("shoddybattle.Pokemon")) {
                 readPokemon(desc, is);
             } else if (desc.name.equals("mechanics.AdvanceMechanics")
                     || (desc.name.equals("mechanics.JewelMechanics"))) {
                 readObject(is);
             } else if (desc.name.equals("java.util.Random")) {
                 readRandomObject(is);
             } else if (desc.name.equals("mechanics.moves.MoveListEntry")) {
                 StreamObject move = readObject(is);
                 StreamString ret = new StreamString();
                 ret.handle = handle;
                 ret.type = TC_STRING;
                 if (move.type == TC_STRING) {
                     ret.data = ((StreamString)move).data;
                 }
                 if ((desc.flags & SC_WRITE_METHOD) != 0) {
                     is.read();
                 }
                 return storeObject(ret);
             } else if (desc.name.equals("mechanics.PokemonNature")) {
                 int nature = is.readInt();
                 StreamNature ret = new StreamNature();
                 ret.handle = handle;
                 ret.type = TC_OBJECT;
                 ret.nature = nature;
                 return storeObject(ret);
             }
         }
         StreamObject ret = new StreamObject();
         ret.handle = handle;
         ret.type = TC_OBJECT;
         return storeObject(ret);
     }
 
     private StreamClassDesc readClassDesc(DataInputStream is) throws IOException {
         char flag = (char)is.read();
         if (flag == TC_CLASSDESC) {
             return readNewClassDesc(is);
         } else if ((flag == TC_PROXYCLASSDESC) || (flag == TC_NULL)) {
             return null;
         } else if (flag == TC_REFERENCE) {
             return (StreamClassDesc)readPrevObject(is);
         }
         return null;
     }
 
     private StreamClassDesc readNewClassDesc(DataInputStream is) throws IOException {
         StreamClassDesc desc = new StreamClassDesc();
         desc.name = is.readUTF();
         desc.uid = is.readLong();
         desc.handle = newHandle();
         desc.flags = (char)is.read();
         int count = is.readShort();
         for (int i = 0; i < count; i++) {
             Field f = new Field();
             f.endBlockData = false;
             f.typeCode = (char)is.read();
             f.name = is.readUTF();
             if ((f.typeCode == '[') || (f.typeCode == 'L')) {
                 f.object = true;
                 StreamObject obj = readObject(is);
                 if (obj.type == TC_STRING) {
                     f.type = ((StreamString)obj).data;
                 }
             } else {
                 f.object = false;
             }
             desc.fields.add(f);
         }
         is.read();
         desc.superclass = readClassDesc(is);
         if (desc.superclass != null) {
             List<Field> fields = desc.superclass.fields;
             for (Field f : desc.fields) {
                 fields.add(f);
             }
             desc.fields = fields;
         }
 
         if ((desc.flags & SC_WRITE_METHOD) != 0) {
             Field field = new Field();
             field.endBlockData = true;
             desc.fields.add(field);
         }
 
         return (StreamClassDesc)storeObject(desc);
     }
 
     private StreamObject readPrevObject(DataInputStream is) throws IOException {
         is.readShort();
         int handle = is.readShort();
         return m_objects.get(handle);
     }
 
     private StreamObject readPokemon(StreamClassDesc desc, DataInputStream is) throws IOException {
         Pokemon p = new Pokemon();
         List<Field> fields = desc.fields;
         for (Field f : fields) {
             if (f.endBlockData) {
                 is.read();
                 continue;
             }
             if (!f.object) {
                 if (f.name.equals("m_gender")) {
                     p.gender = Gender.getGender(is.readInt());
                 } else if (f.name.equals("m_level")) {
                     p.level = is.readInt();
                 } else if (f.name.equals("m_shiny")) {
                     p.shiny = is.readBoolean();
                 } else {
                     if (f.typeCode == 'I') {
                         is.readInt();
                     } else if (f.typeCode == 'Z') {
                         is.read();
                     }
                 }
             } else {
                 StreamObject obj = readObject(is);
                 if (obj == null) continue;
                 if (obj.type == TC_STRING) {
                     String data = ((StreamString)obj).data;
                     if (f.name.equals("m_name")) {
                         p.species = data;
                     } else if (f.name.equals("m_abilityName")) {
                         p.ability = data;
                     } else if (f.name.equals("m_itemName")) {
                         p.item = data;
                     } else if (f.name.equals("m_nickname")) {
                         p.nickname = data;
                     }
                 } else if (obj.type == TC_ARRAY) {
                     StreamArray arr = (StreamArray)obj;
                     if (!arr.objects) {
                         int length = (f.name.equals("m_ppUp"))
                                 ? Pokemon.MOVE_COUNT : Pokemon.STAT_COUNT;
                         int[] vals = new int[length];
                         for (int i = 0; i < vals.length; i++) {
                             vals[i] = arr.intElements.get(i);
                         }
                         if (f.name.equals("m_iv")) {
                             p.ivs = vals;
                         } else if (f.name.equals("m_ev")) {
                             p.evs = vals;
                         } else if (f.name.equals("m_ppUp")) {
                             p.ppUps = vals;
                         }
                     } else {
                         String[] moves = new String[Pokemon.MOVE_COUNT];
                         for (int i = 0; i < moves.length; i++) {
                             StreamString str = ((StreamString)arr.elements.get(i));
                             if (str != null) {
                                 moves[i] = str.data;
                             }
                         }
                         p.moves = moves;
                     }
                 } else if (obj.type == TC_OBJECT) {
                     if (f.name.equals("m_nature")) {
                         p.nature = PokemonNature.getNature(((StreamNature)obj).nature).getName();
                     }
                 }
             }
         }
         p.happiness = 255;
         m_pokemon.add(p);
         return null;
     }
 
     private void readRandomObject(DataInputStream is) throws IOException {
         is.readLong();
         is.readLong();
         is.read();
         is.read();
     }
 
     private int newHandle() {
         m_handle++;
         return m_handle;
     }
 
     private StreamObject storeObject(StreamObject obj) {
         m_objects.put(obj.handle, obj);
         return obj;
     }
 
     public static void main(String[] args) {
         TeamFileParser tfp = new TeamFileParser();
         Pokemon[] team = tfp.parseTeam("/Users/ben/Downloads/trickyteam", null);
         System.out.println("Content-type: text/plain");
         System.out.println();
         if (team != null) {
             for (Pokemon p : team) {
                 System.out.println("Level " + p.level + " " + p.species);
                 System.out.println("Gender: " + p.gender);
                 System.out.println("Nickname: " + p.nickname);
                 System.out.println("Ability: " + p.ability);
                 System.out.println("Item: " + p.item);
                 System.out.println("IVs: " + java.util.Arrays.toString(p.ivs));
                 System.out.println("EVs: " + java.util.Arrays.toString(p.evs));
                 System.out.println("Moves:");
                 for (int i = 0; i < p.moves.length; i++) {
                     System.out.println("\t" + p.moves[i] + " (" + p.ppUps[i] + " pp ups)");
                 }
                 System.out.println();
                 System.out.println();
             }
         }
     }
 }
