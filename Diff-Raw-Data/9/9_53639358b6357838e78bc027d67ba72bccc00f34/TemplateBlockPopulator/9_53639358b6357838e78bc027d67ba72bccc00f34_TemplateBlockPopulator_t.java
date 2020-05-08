 package me.riking.templateworlds.impl.bukkit;
 
 import java.util.Random;
 
 import org.bukkit.Chunk;
 import org.bukkit.ChunkSnapshot;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.block.Beacon;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockState;
 import org.bukkit.block.BrewingStand;
 import org.bukkit.block.Chest;
 import org.bukkit.block.CommandBlock;
 import org.bukkit.block.CreatureSpawner;
 import org.bukkit.block.Dispenser;
 import org.bukkit.block.Dropper;
 import org.bukkit.block.Furnace;
 import org.bukkit.block.Hopper;
 import org.bukkit.block.Jukebox;
 import org.bukkit.block.NoteBlock;
 import org.bukkit.block.Sign;
 import org.bukkit.block.Skull;
 import org.bukkit.generator.BlockPopulator;
 import org.bukkit.inventory.FurnaceInventory;
 import org.bukkit.inventory.Inventory;
 
 public class TemplateBlockPopulator extends BlockPopulator {
     private final World templateWorld;
 
     public TemplateBlockPopulator(World world) {
         templateWorld = world;
     }
 
     @Override
     public void populate(World world, Random random, Chunk targetChunk) {
         Chunk sourceChunk = templateWorld.getChunkAt(targetChunk.getX(), targetChunk.getZ());
         ChunkSnapshot sourceSnap = sourceChunk.getChunkSnapshot();
 
         {
             int bx = sourceChunk.getX() << 4;
             int bz = sourceChunk.getZ() << 4;
 
             Block target;
             int px, pz;
             for (int py = 0; py < 256; py++) {
                 for (int x = 0; x < 16; x++) {
                     px = bx + x;
                     for (int z = 0; z < 16; z++) {
                         pz = bz + z;
                         target = targetChunk.getBlock(px, py, pz);
                        target.setTypeIdAndData(sourceSnap.getBlockTypeId(x, py, z), (byte) sourceSnap.getBlockData(x, py, z), false);
                     }
                 }
             }
         }
         {
             Location loc = new Location(world, 0, 0, 0);
             BlockState target;
             for (BlockState source : sourceChunk.getTileEntities()) {
                 source.getLocation(loc);
                 target = targetChunk.getBlock(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()).getState();
                 if (!source.getClass().equals(target.getClass())) {
                     System.out.println(String.format("[WARNING] TemplateWorlds: BlockState at %d, %d, %d skipped because BlockState classes were different: source %s, target %s", loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), source.getClass().getName(), target.getClass().getName()));
                     continue;
                 }
                 if (source instanceof Beacon) {
                     Beacon so = (Beacon) source;
                     Beacon ta = (Beacon) target;
                     // XXX misses info
                     copyInventory(so.getInventory(), ta.getInventory());
                 } else if (source instanceof BrewingStand) {
                     BrewingStand so = (BrewingStand) source;
                     BrewingStand ta = (BrewingStand) target;
                     ta.setBrewingTime(so.getBrewingTime());
                     copyInventory(so.getInventory(), ta.getInventory());
                     ta.getInventory().setIngredient(so.getInventory().getIngredient());
                 } else if (source instanceof Chest) {
                     Chest so = (Chest) source;
                     Chest ta = (Chest) target;
                     // getBlockInventory() ignores neighbors
                     copyInventory(so.getBlockInventory(), ta.getBlockInventory());
                 } else if (source instanceof CommandBlock) {
                     CommandBlock so = (CommandBlock) source;
                     CommandBlock ta = (CommandBlock) target;
                     ta.setCommand(so.getCommand());
                     ta.setName(so.getName());
                 } else if (source instanceof CreatureSpawner) {
                     CreatureSpawner so = (CreatureSpawner) source;
                     CreatureSpawner ta = (CreatureSpawner) target;
                     // XXX misses info
                     ta.setSpawnedType(so.getSpawnedType());
                     ta.setDelay(so.getDelay());
                 } else if (source instanceof Dispenser) {
                     Dispenser so = (Dispenser) source;
                     Dispenser ta = (Dispenser) target;
                     copyInventory(so.getInventory(), ta.getInventory());
                 } else if (source instanceof Dropper) {
                     Dropper so = (Dropper) source;
                     Dropper ta = (Dropper) target;
                     copyInventory(so.getInventory(), ta.getInventory());
                 } else if (source instanceof Furnace) {
                     Furnace so = (Furnace) source;
                     Furnace ta = (Furnace) target;
                     ta.setBurnTime(so.getBurnTime());
                     ta.setCookTime(so.getCookTime());
                     FurnaceInventory invso = so.getInventory();
                     FurnaceInventory invta = ta.getInventory();
                     invta.setFuel(invso.getFuel());
                     invta.setResult(invso.getResult());
                     invta.setSmelting(invso.getSmelting());
                 } else if (source instanceof Hopper) {
                     Hopper so = (Hopper) source;
                     Hopper ta = (Hopper) target;
                     copyInventory(so.getInventory(), ta.getInventory());
                 } else if (source instanceof Jukebox) {
                     Jukebox so = (Jukebox) source;
                     Jukebox ta = (Jukebox) target;
                     ta.setPlaying(so.getPlaying());
                 } else if (source instanceof NoteBlock) {
                     NoteBlock so = (NoteBlock) source;
                     NoteBlock ta = (NoteBlock) target;
                     ta.setRawNote(so.getRawNote());
                 } else if (source instanceof Sign) {
                     Sign so = (Sign) source;
                     Sign ta = (Sign) target;
                     ta.setLine(0, so.getLine(0));
                     ta.setLine(1, so.getLine(1));
                     ta.setLine(2, so.getLine(2));
                     ta.setLine(3, so.getLine(3));
                 } else if (source instanceof Skull) {
                     Skull so = (Skull) source;
                     Skull ta = (Skull) target;
                     ta.setSkullType(so.getSkullType());
                     ta.setRotation(so.getRotation());
                     ta.setOwner(so.getOwner());
                 }
                 target.update();
             }
         }
 
         // Request cleanup
         sourceChunk = null;
         templateWorld.unloadChunkRequest(targetChunk.getX(), targetChunk.getZ());
     }
 
     public void copyInventory(Inventory source, Inventory dest) {
         dest.setContents(source.getContents());
     }
 }
