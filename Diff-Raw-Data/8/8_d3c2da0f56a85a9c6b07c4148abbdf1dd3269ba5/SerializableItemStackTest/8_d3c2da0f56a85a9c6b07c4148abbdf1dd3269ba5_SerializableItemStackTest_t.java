 /**
  * LICENSING
  * 
  * This software is copyright by sunkid <sunkid@iminurnetz.com> and is
  * distributed under a dual license:
  * 
  * Non-Commercial Use:
  *    This program is free software: you can redistribute it and/or modify
  *    it under the terms of the GNU General Public License as published by
  *    the Free Software Foundation, either version 3 of the License, or
  *    (at your option) any later version.
  *
  *    This program is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *    GNU General Public License for more details.
  *
  *    You should have received a copy of the GNU General Public License
  *    along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * 
  * Commercial Use:
  *    Please contact sunkid@iminurnetz.com
  */
 package com.iminurnetz.bukkit.util.tests;
 
 import org.bukkit.DyeColor;
 import org.bukkit.Material;
 import org.bukkit.inventory.ItemStack;
 
 import com.iminurnetz.bukkit.util.SerializableItemStack;
 
 import junit.framework.TestCase;
 
 public class SerializableItemStackTest extends TestCase {
     public void testConversion() {
        ItemStack s = new ItemStack(Material.WOOL, 10, (short) 0, DyeColor.BLUE.getData());
         
         SerializableItemStack ss = new SerializableItemStack(s);
         
         assertEquals("Serialization succeeded", s.getData().getData(), DyeColor.BLUE.getData());
 
         ItemStack s2 = ss.getStack();
         assertEquals("Conversion succeeded", s, s2);
         assertEquals("Data is retained", s.getData().getData(), s2.getData().getData());
     }
 }
