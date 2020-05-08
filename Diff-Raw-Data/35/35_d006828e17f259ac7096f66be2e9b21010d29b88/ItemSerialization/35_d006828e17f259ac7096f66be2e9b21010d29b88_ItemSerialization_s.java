 package com.github.yukinoraru.ToggleInventory;
 
import net.minecraft.server.v1_4_R1.NBTBase;
import net.minecraft.server.v1_4_R1.NBTTagCompound;
import net.minecraft.server.v1_4_R1.NBTTagList;
import org.bukkit.craftbukkit.v1_4_R1.inventory.CraftInventoryCustom;
import org.bukkit.craftbukkit.v1_4_R1.inventory.CraftItemStack;
 
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.math.BigInteger;
 
 public class ItemSerialization {
     public static String toBase64(Inventory inventory) {
         ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
         DataOutputStream dataOutput = new DataOutputStream(outputStream);
         NBTTagList itemList = new NBTTagList();
 
         // Save every element in the list
         for (int i = 0; i < inventory.getSize(); i++) {
             NBTTagCompound outputObject = new NBTTagCompound();
             CraftItemStack craft = getCraftVersion(inventory.getItem(i));
 
             // Convert the item stack to a NBT compound
             if (craft != null)
                 CraftItemStack.asNMSCopy(craft).save(outputObject);
             itemList.add(outputObject);
         }
 
         // Now save the list
         NBTBase.a(itemList, dataOutput);
 
         // Serialize that array
         return new BigInteger(1, outputStream.toByteArray()).toString(32);
         //return encodeBase64(outputStream.toByteArray());
     }
 
     public static Inventory fromBase64(String data) {
         ByteArrayInputStream inputStream = new ByteArrayInputStream(new BigInteger(data, 32).toByteArray());
         //ByteArrayInputStream inputStream = new ByteArrayInputStream(decodeBase64(data));
         NBTTagList itemList = (NBTTagList) NBTBase.b(new DataInputStream(inputStream));
         Inventory inventory = new CraftInventoryCustom(null, itemList.size());
 
         for (int i = 0; i < itemList.size(); i++) {
             NBTTagCompound inputObject = (NBTTagCompound) itemList.get(i);
 
             // IsEmpty
             if (!inputObject.isEmpty()) {
                 inventory.setItem(i, CraftItemStack.asCraftMirror(
                  	net.minecraft.server.v1_4_R1.ItemStack.createStack(inputObject)));
             }
         }
 
         // Serialize that array
         return inventory;
     }
 
     private static CraftItemStack getCraftVersion(ItemStack stack) {
         if (stack instanceof CraftItemStack)
             return (CraftItemStack) stack;
         else if (stack != null)
             return CraftItemStack.asCraftCopy(stack);
         else
             return null;
     }
 
     public static Inventory getArmorInventory(PlayerInventory inventory) {
         ItemStack[] armor = inventory.getArmorContents();
         CraftInventoryCustom storage = new CraftInventoryCustom(null, armor.length);
 
         for (int i = 0; i < armor.length; i++)
             storage.setItem(i, armor[i]);
         return storage;
     }
 }
