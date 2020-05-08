 /*
  * Hex - a hex viewer and annotator
  * Copyright (C) 2009  Trejkaz, Hex Project
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.trypticon.hex.anno.primitive;
 
 import java.util.Map;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Arrays;
 
 import org.trypticon.hex.anno.Interpretor;
 import org.trypticon.hex.anno.InterpretorStorage;
 import org.trypticon.hex.anno.InterpretorInfo;
 
 /**
  * Storage support for primitive interpretors.
  *
  * @author trejkaz
  */
 public class PrimitiveInterpretorStorage implements InterpretorStorage {
     private final Map<Class<? extends Interpretor>, String> classToName =
             new HashMap<Class<? extends Interpretor>, String>(10);
     private final Map<String, Class<? extends Interpretor>> nameToClass =
             new HashMap<String, Class<? extends Interpretor>>(10);
 
     public PrimitiveInterpretorStorage() {
         register("uint2be", UShortInterpretorBE.class);
         register("uint2le", UShortInterpretorLE.class);
         register("uint4be", UIntInterpretorBE.class);
         register("uint4le", UIntInterpretorLE.class);
        register("uint4be", ULongInterpretorBE.class);
        register("uint4le", ULongInterpretorLE.class);
     }
 
     private void register(String name, Class<? extends Interpretor> klass) {
         classToName.put(klass, name);
         nameToClass.put(name, klass);
     }
 
     public List<InterpretorInfo> getInterpretorInfos() {
         // TODO: Interpretor info should be structured to allow categorising them as well, for menus.
 
         return Arrays.asList(new UShortInterpretorBEInfo(),
                              new UShortInterpretorLEInfo(),
                              new UIntInterpretorBEInfo(),
                              new UIntInterpretorLEInfo(),
                              new ULongInterpretorBEInfo(),
                              new ULongInterpretorLEInfo());
     }
 
     public Map<String, Object> toMap(Interpretor interpretor) {
         String name = classToName.get(interpretor.getClass());
         if (name != null) {
             Map<String, Object> result = new HashMap<String, Object>(1);
             result.put("name", name);
             return result;
         } else {
             return null;
         }
     }
 
     public Interpretor fromMap(Map<String, Object> map) {
         String name = (String) map.get("name");
         Class<? extends Interpretor> klass = nameToClass.get(name);
         if (klass != null) {
             try {
                 return klass.newInstance();
             } catch (InstantiationException e) {
                 throw new IllegalStateException("Constructor should have been no-op", e);
             } catch (IllegalAccessException e) {
                 throw new IllegalStateException("Constructor should have been accessible", e);
             }
         } else {
             return null;
         }
     }
 }
