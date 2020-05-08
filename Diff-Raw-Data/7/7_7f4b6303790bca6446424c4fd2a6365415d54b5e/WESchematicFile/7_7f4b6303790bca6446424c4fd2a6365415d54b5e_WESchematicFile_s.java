 package com.mikeprimm.WorldMapper;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.zip.GZIPInputStream;
 
 import org.spout.nbt.ByteArrayTag;
 import org.spout.nbt.CompoundTag;
 import org.spout.nbt.ListTag;
 import org.spout.nbt.ShortTag;
 import org.spout.nbt.IntTag;
 import org.spout.nbt.Tag;
 import org.spout.nbt.stream.NBTInputStream;
 import org.spout.nbt.stream.NBTOutputStream;
 
 public class WESchematicFile {
     public int width, length, height;
     private CompoundTag schematicTag;
     private Map<String, Tag<?>> schematic;
     private byte[] ids;
     private byte[] extids;
     private byte[] data;
     private HashSet<String> tileEntityToDrop = new HashSet<String>();
     
     public void load(File file) throws IOException {
         FileInputStream stream = new FileInputStream(file);
         NBTInputStream nbtStream = new NBTInputStream(new GZIPInputStream(stream), false);
         // Schematic tag
         schematicTag = (CompoundTag) nbtStream.readTag();
         nbtStream.close();
         // Sanity check
         if (!schematicTag.getName().equals("Schematic")) {
             throw new IOException("Missing \"Schematic\" tag in " + file.getPath());
         }
         schematic = schematicTag.getValue();
         if (!schematic.containsKey("Blocks")) {
             throw new IOException("Missing \"Blocks\" tag in " + file.getPath());
         }
         // Get information
        width = ((ShortTag) schematic.get("Width")).getValue();
        height = ((ShortTag) schematic.get("Height")).getValue();
        length = ((ShortTag) schematic.get("Length")).getValue();
 
        // Get blocks
        ids = ((ByteArrayTag) schematic.get("Blocks")).getValue();
        data = ((ByteArrayTag) schematic.get("Data")).getValue();
        if (schematic.get("AddBlocks") != null) {
            extids = ((ByteArrayTag) schematic.get("AddBlocks")).getValue();
        }
     }
     
     public void save(File file) throws IOException {
         if (tileEntityToDrop.isEmpty() == false) {  // Any TEs to delete
             List<Tag> tileEntities = ((ListTag) schematic.get("TileEntities")).getValue();
             Iterator<Tag> iter = tileEntities.iterator();
             ArrayList<Tag> newtag = new ArrayList<Tag>();
             while (iter.hasNext()) {
                 Tag tag = iter.next();
                 if (!(tag instanceof CompoundTag)) continue;
                 CompoundTag ctag = (CompoundTag) tag;
                 int x = ((IntTag) ctag.getValue().get("x")).getValue();
                 int y = ((IntTag) ctag.getValue().get("y")).getValue();
                 int z = ((IntTag) ctag.getValue().get("z")).getValue();
                 if (!tileEntityToDrop.remove(""+x+","+y+","+z)) {
                     newtag.add(ctag);
                 }
             }
             schematic.put("TileEntities", new ListTag("TileEntities", CompoundTag.class, newtag));
         }
         NBTOutputStream stream = new NBTOutputStream(new FileOutputStream(file));
         stream.writeTag(schematicTag);
         stream.close();
     }
     
     public int getID(int x, int y, int z) {
         int index = y * width * length + z * width + x;
         int val = 0xFF & ((int) this.ids[index]);
         if (extids != null) { // No corresponding AddBlocks index
             if ((index & 1) == 0) {
                 val += (((int) extids[index >> 1]) & 0x0F) << 8;
             }
             else {
                 val += (((int) extids[index >> 1]) & 0xF0) << 4;
             }
         }
         return val;
     }
     public int getData(int x, int y, int z) {
         int index = y * width * length + z * width + x;
        if ((index & 1) == 0) {
            return ((int) (data[index >> 1] & 0x0F)) << 8;
        }
        else {
            return ((int) (data[index >> 1] & 0xF0)) << 4;
        }
     }
     public void setIDAndData(int x, int y, int z, int id, int dat) {
         //System.out.println(String.format("set %d,%d,%d to %03x:%x", x, y, z, id, dat));
         int index = y * width * length + z * width + x;
         if (id > 255) {
             if (extids == null) {
                 extids = new byte[(ids.length >> 1) + 1];
                 schematic.put("AddBlocks", new ByteArrayTag("AddBlocks", extids));
             }
             if ((index & 1) == 0) {
                 extids[index >> 1] = (byte)((extids[index >> 1] & 0xF0) | ((id >> 8) & 0xF));
             }
             else {
                 extids[index >> 1] = (byte)((extids[index >> 1] & 0x0F) | (((id >> 8) & 0xF) << 4));
             }
         }
         ids[index] = (byte) (id & 0xFF);
         data[index] = (byte)(dat & 0xF);
         //System.out.println(String.format("new %2x:%2x:%2x", (extids != null)?extids[index >> 1]:0, ids[index], data[index >> 1] ));
     }
     public void deleteTileEntity(int x, int y, int z) {
         String key = "" + x + "," + y + "," + z;
         tileEntityToDrop.add(key);
     }
 
 }
