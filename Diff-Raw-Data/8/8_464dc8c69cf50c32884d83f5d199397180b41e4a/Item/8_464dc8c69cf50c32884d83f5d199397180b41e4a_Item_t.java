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
 package com.iminurnetz.bukkit.util;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.material.MaterialData;
 
 /**
  * An Item is a block or crafted item that holds information about the type and state of the Material
  * that it consists of.
  * @author <a href="mailto:sunkid@iminurnetz.com">sunkid</a>
  *
  */
 public class Item {
 	protected Material material;
 	protected MaterialData data;
 
 	public Item(Block block) {
 		this.material = block.getState().getType();
 		this.data = block.getState().getData();
 	}
 
 	public Item(Material material) {
 		this(material, material.getNewData((byte) 0));
 	}
 	
 	public Item(String item) throws InstantiationException {
 		if (item == null)
 			throw new InstantiationException("Item specification cannot be null");
 		
 		String[] words = item.split(":");
 		
 		if (words.length > 2)
 			throw new InstantiationException("Incorrect Item specification");
 		
 		Material m = MaterialUtils.getMaterial(words[0]);
 		if (m == null)
 			throw new InstantiationException("Cannot parse material information from '" + words[0] + "'");
 		
 		this.material = m;
 		
 		if (words.length == 2) {
 			this.data = MaterialUtils.getData(material, words[1]);
 		} else {
			this.data = null;
 		}
 	}
 
 	public Item(Material material, MaterialData data) {
 		this.material = material;
 		this.data = data;
 	}
 
 	public MaterialData getData() {
 		return data;
 	}
 	
 	public Material getMaterial() {
 		return material;
 	}
 	
 	public void setData(MaterialData data) {
 		this.data = data;
 	}
 	
 	public void setMaterial(Material material) {
 		this.material = material;
 	}
 
 	public boolean isBlock() {
 		return getMaterial().isBlock();
 	}
 	
 	public ItemStack getStack() {
 	    return getStack(1);
 	}
 	
 	public ItemStack getStack(int n) {
         byte d = 0;
         if (getData() != null) {
             d = getData().getData();
            return new ItemStack(getMaterial(), n, d);
         }
         
        return new ItemStack(getMaterial(), n);
 	}
 }
