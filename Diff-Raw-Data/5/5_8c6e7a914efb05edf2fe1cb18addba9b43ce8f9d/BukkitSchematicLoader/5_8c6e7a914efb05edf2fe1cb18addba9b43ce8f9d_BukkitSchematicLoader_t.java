 package com.tehbeard.map.schematic.bukkit;
 
 
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.BrewingStand;
 import org.bukkit.block.Chest;
 import org.bukkit.block.CreatureSpawner;
 import org.bukkit.block.Dispenser;
 import org.bukkit.block.Furnace;
 import org.bukkit.block.Jukebox;
 import org.bukkit.block.NoteBlock;
 import org.bukkit.block.Sign;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.BookMeta;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.bukkit.inventory.meta.SkullMeta;
 
 import com.tehbeard.map.misc.ItemEnchantment;
 import com.tehbeard.map.misc.Item;
 import com.tehbeard.map.misc.MapUtils;
 import com.tehbeard.map.misc.WorldVector;
 import com.tehbeard.map.schematic.BlockType;
 import com.tehbeard.map.schematic.Schematic;
 import com.tehbeard.map.schematic.bukkit.worldedit.BlockData;
 import com.tehbeard.map.tileEntities.TileCauldron;
 import com.tehbeard.map.tileEntities.TileChest;
 import com.tehbeard.map.tileEntities.TileEntity;
 import com.tehbeard.map.tileEntities.TileFurnace;
 import com.tehbeard.map.tileEntities.TileNoteBlock;
 import com.tehbeard.map.tileEntities.TileRecordPlayer;
 import com.tehbeard.map.tileEntities.TileSign;
 import com.tehbeard.map.tileEntities.TileSpawner;
 import com.tehbeard.map.tileEntities.TileTrap;
 
 public class BukkitSchematicLoader {
 
     private Schematic schematic;
 
     public BukkitSchematicLoader(Schematic schematic){
         this.schematic = schematic;
     }
 
     public void paste(Location location,int rotations,byte[] layers){
         
         WorldVector l = new WorldVector(location.getBlockX(),location.getBlockY(),location.getBlockZ(),location.getWorld().getName());
         addBlocks(l,0,rotations,layers);
         addBlocks(l,1,rotations,layers);
         addBlocks(l,2,rotations,layers);
 
         World w = Bukkit.getWorld(l.getWorldName());
         for(TileEntity t:schematic.getTileEntities()){
 
            if(layers != null){
                if(!inArray(layers, schematic.getLayer(t.getX(), t.getY(), t.getZ()))){
                    continue;
                }
            }
             WorldVector relVector = new WorldVector(schematic.getOffset());
             relVector.addVector(new WorldVector(t.getX(), t.getY(),t.getZ(), null));
             relVector.rotateVector(rotations);
             
             Block b = w.getBlockAt(location.clone().add(
                     relVector.getX(),
                     relVector.getY(),
                     relVector.getZ()
                     )
                     );
 
             if(t instanceof TileChest){
                 placeChest(b,(TileChest) t);
             }
 
             if(t instanceof TileTrap){
                 placeDispenser(b,(TileTrap)t);
             }
             if(t instanceof TileSign){
                 placeSign(b,(TileSign)t);
             }
 
             if(t instanceof TileNoteBlock){
                 placeNoteBlock(b,(TileNoteBlock)t);
             }
             if(t instanceof TileFurnace){
                 placeFurnace(b,(TileFurnace)t);
             }
             if(t instanceof TileCauldron){
                 placeBrewStand(b,(TileCauldron)t);
             }
             if(t instanceof TileRecordPlayer){
                 placeJukebox(b,(TileRecordPlayer)t);
             }
             if(t instanceof TileSpawner){
                 placeSpawner(b, (TileSpawner) t);
             }
         }
     }
 
 
 
     private void addBlocks(WorldVector l,int blockOrder,int rotations,byte[] layers){
         World w = Bukkit.getWorld(l.getWorldName());
 
         WorldVector baseVector = new WorldVector(0, 0, 0, l.getWorldName());
         baseVector.addVector(l);
         for(int y = 0;y<schematic.getHeight();y++){
             for(int z = 0;z<schematic.getLength();z++){
                 for(int x = 0;x<schematic.getWidth();x++){
 
                     if(BlockType.getOrder(schematic.getBlockId(x, y, z)) == blockOrder){
                         if(layers != null){
                             if(!inArray(layers, schematic.getLayer(x, y, z))){
                                 continue;
                             }
                         }
                         WorldVector relVector = new WorldVector(schematic.getOffset());
                         relVector.addVector(new WorldVector(x, y, z, null));
                         relVector.rotateVector(rotations);
                         relVector.addVector(baseVector);
                         Block b = w.getBlockAt(
                                 relVector.getBlockX(), 
                                 relVector.getBlockY(),
                                 relVector.getBlockZ()
                                 );
 
                         int type = schematic.getBlockId(x, y, z) & 0xFF;
                         byte data = schematic.getBlockData(x, y, z);
                         for(int i =0;i<rotations;i++){
                             data = (byte) BlockData.rotate90(type, data);
                         }
                         b.setTypeId(type,false);
                         b.setData(data,false);
 
 
                         b.getState().update();
 
                     }
 
                 }	
             }	
         }
     }
     
     private boolean inArray(byte[] layers,byte b){
         for(byte c : layers){
             if(c==b){return true;}
         }
         return false;
     }
 
 
     
 
 
     private static void placeChest(Block b,TileChest chest){
         Chest c = (Chest)b.getState();
         Inventory i = c.getBlockInventory();
         doInventory(i, chest.getItems());
         c.update(true);
     }
 
     private static void placeDispenser(Block b, TileTrap trap) {
         Dispenser c = (Dispenser)b.getState();
         Inventory i = c.getInventory();
         doInventory(i, trap.getItems());
         c.update(true);
 
     }
 
     private static void placeSign(Block b, TileSign t) {
         Sign s = (Sign) b.getState();
         int i =0;
         for(String l : t.getLines()){
             s.setLine(i,l);
             i++;
         }
         s.update(true);
 
     }
 
     private static void placeNoteBlock(Block b, TileNoteBlock t) {
         NoteBlock s = (NoteBlock) b.getState();
         s.setRawNote(t.getNote());
         s.update(true);
 
     }
 
     private static void placeFurnace(Block b, TileFurnace t) {
         Furnace f = (Furnace) b.getState();
 
         f.setBurnTime(t.getBurn());
         f.setCookTime(t.getCook());
         doInventory(f.getInventory(), t.getItems());
         f.update(true);
     }
 
     private static void placeBrewStand(Block b, TileCauldron t) {
         BrewingStand bs = (BrewingStand)b.getState();
         bs.setBrewingTime(t.getBrew());
 
 
         doInventory(bs.getInventory(), t.getItems());
 
         bs.update(true);
     }
 
     private static void placeJukebox(Block b, TileRecordPlayer t) {
         Jukebox jb = (Jukebox)b.getState();
         jb.setRawData(t.getRecord());
         jb.update(true);
 
     }
 
     private static void placeSpawner(Block b,TileSpawner t){
         CreatureSpawner spawner = (CreatureSpawner) b.getState();
         spawner.setCreatureTypeByName(t.getId());
     }
 
     private static ItemStack makeItemStack(Item item){
         ItemStack is = new ItemStack(item.getId(),item.getCount(),item.getDamage());
         
         ItemMeta meta = is.getItemMeta();
         
         for(ItemEnchantment e : item.getEnchantments()){
             meta.addEnchant(Enchantment.getById(e.getType()), e.getLvl(),true);
         }
         
         if(item.getName()!=null){
             meta.setDisplayName(item.getName());
         }
         
         if(item.getLore()!=null){
             meta.setLore(item.getLore());
         }
         
         switch(is.getType()){
         case BOOK_AND_QUILL:
         case  WRITTEN_BOOK:
             BookMeta bookMeta = (BookMeta)meta;
             bookMeta.setPages(item.getBookPages());
             if(is.getType() == Material.WRITTEN_BOOK){
                 bookMeta.setAuthor(item.getBookAuthor());
                 bookMeta.setTitle(item.getBookTitle());
             }
             break;
         case SKULL_ITEM:
             if(item.getSkullOwner() !=null){
                 ((SkullMeta)meta).setOwner(item.getSkullOwner());
             }
             break;
         }
 
         
         is.setItemMeta(meta);
         return is;
 
     }
 
     private static void doInventory(Inventory inv, Item[] items){
         for(Item item : items){
             if(item == null){continue;}
 
             inv.setItem(item.getSlot(),makeItemStack(item));
         }
     }
 }
