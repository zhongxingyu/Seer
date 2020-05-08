 package com.mikeprimm.WorldMapper;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.Reader;
 import java.util.BitSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.spout.nbt.ByteArrayTag;
 import org.spout.nbt.CompoundMap;
 import org.spout.nbt.CompoundTag;
 import org.spout.nbt.ListTag;
 import org.spout.nbt.Tag;
 import org.spout.nbt.util.NBTMapper;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonIOException;
 import com.google.gson.JsonSyntaxException;
 
 public class WorldMapper {
     // Biome names, ordered by index/ID - lowercase with spaces removed (as done in MCPatcher)
     public static final String[] biomes = {
          "ocean", "plains", "desert", "extremehills", "forest", "taiga", "swampland", "river", "hell",
          "sky", "frozenocean", "frozenriver", "iceplains", "icemountains", "mushroomisland", "mushroomislandshore",
          "beach", "deserthills", "foresthills", "taigahills", "extremehillsedge", "jungle", "junglehills"
     };
          
     // Index is block ID * 16 + meta, value is new block ID *16 + meta
     private static int blkid_map[] = new int[4096 * 16];
     private static int biome_blkid_map[][] = new int[256][];
     private static BitSet blkid_biome_specific = new BitSet(); // Flags which source IDs to scrap tile entity
     private static BitSet blkid_toss_tileentity = new BitSet(); // Flags which source IDs to scrap tile entity
 
     private static class BlockMapping {
         private int blkid;
         private int meta = -1;
         private int newblkid;
         private int newmeta = -1;
         private boolean tosstileentity = false;
         private String biomes[] = null;
     }
     private static class MappingConfig {
         private BlockMapping[] blocks;
     }
     
     private static class MappedChunk {
         Tag<?> level;
         int bcnt;   // Number of blocks mapped
         int tescrubbed; // Number of tile entities scrubbed
         CompoundMap value;  // Base value for chunk
         List<CompoundTag> tileents; // List of tile entites (original)
         LinkedList<CompoundTag> new_tileents; // New list, if modified
         byte[] biomes; // biome data (ZX order)
         List<CompoundTag> sections; // Chunk sections
 
         @SuppressWarnings("unchecked")
         MappedChunk(Tag<?> lvl) throws IOException {
             level = lvl;
             CompoundMap val = NBTMapper.getTagValue(level, CompoundMap.class);
             value = NBTMapper.getTagValue(val.get("Level"), CompoundMap.class);
             if (value == null) throw new IOException("Chunk is missing Level data");
 
             this.tileents = NBTMapper.getTagValue(value.get("TileEntities"), List.class);
             if (tileents == null) throw new IOException("Chunk is missing TileEntities data");
             // Get biomes (ZX order)
             biomes = NBTMapper.getTagValue(value.get("Biomes"), byte[].class);
             if ((biomes == null) || (biomes.length < 256)) { throw new IOException("No value for Biomes in chunk"); }
             
             // Get sections of chunk
             sections = NBTMapper.getTagValue(value.get("Sections"), List.class);
             if (sections == null) { throw new IOException("No value for Sections in chunk"); }
 
             bcnt = tescrubbed = 0;
         }
         // Process chunk
         void processChunk() throws IOException {
             // Loop through the sections
             for (CompoundTag sect : sections) {
                 processSection(sect.getValue());
             }
             // If modified tile entities list, replace it
             if (new_tileents != null) {
                 value.put("TileEntities", new ListTag<CompoundTag>("TileEntities", CompoundTag.class, new_tileents));
                 new_tileents = null;
             }
         }
         void processSection(CompoundMap sect) throws IOException {
             Byte y = NBTMapper.getTagValue(sect.get("Y"), Byte.class);
             if (y == null) throw new IOException("Section missing Y field");
             byte[] blocks = NBTMapper.getTagValue(sect.get("Blocks"), byte[].class);
             int yoff = y.intValue() * 16; // Base Y value of section
             if ((blocks == null) || (blocks.length < 4096)) throw new IOException("Section missing Blocks field");
             byte[] extblocks = NBTMapper.getTagValue(sect.get("Add"), byte[].class); // Might be null
             if ((extblocks != null) && (extblocks.length < 2048))  throw new IOException("Section missing Data field");
             byte[] data = NBTMapper.getTagValue(sect.get("Data"), byte[].class);
             if ((data == null) || (data.length < 2048)) throw new IOException("Section missing Data field");
             
             for (int i = 0, j = 0; i < 4096; j++) { // YZX order
                 int id, meta;
                 int extid = 0;
                 int datavals = data[j];
                 int idmataval = 0;
                 int newidmetaval;
                 if (extblocks != null) {
                     extid = 255 & extblocks[j];
                 }
                 // Process even values
                 id = (255 & blocks[i]) | ((extid & 0xF) << 8);
                 meta = (datavals & 0xF);
                 idmataval = (id << 4) | meta;
                 newidmetaval = blkid_map[idmataval];
                 int biomeid = biomes[(i & 0x7F) << 1];
                 newidmetaval = getBiomeSpecificID(idmataval, biomeid);
                 
                 if (newidmetaval != idmataval) {    // New value?
                     if (blkid_toss_tileentity.get(idmataval)) { // If scrubbing tile entity
                         deleteTileEntity(i & 0xF, ((i >> 8) & 0xF) + yoff, (i >> 4) & 0xF, idmataval);
                     }
                     id = (newidmetaval >> 4);
                     meta = (newidmetaval & 0xF);
                     if ((id > 256) && (extblocks == null)) {
                         extblocks = new byte[2048];
                         sect.put("Add", new ByteArrayTag("Add", extblocks));
                     }
                     blocks[i] = (byte)(255 & id);
                     if (extblocks != null) {
                         extid = (byte) ((extid & 0xF0) | ((id >> 8) & 0xF));
                     }
                     datavals = (byte) ((datavals & 0xF0) | (meta & 0xF));
                     bcnt++;
                 }
                 i++;
                 // Process odd values
                 id = (255 & blocks[i]) | ((extid & 0xF0) << 4);
                 meta = (datavals & 0xF0) >> 4;
                 idmataval = (id << 4) | meta;
                 newidmetaval = blkid_map[idmataval];
                 biomeid = biomes[((i & 0x7F) << 1) + 1];
                 newidmetaval = getBiomeSpecificID(idmataval, biomeid);
                 if (newidmetaval != idmataval) {    // New value?
                     if (blkid_toss_tileentity.get(idmataval)) { // If scrubbing tile entity
                         deleteTileEntity(i & 0xF, ((i >> 8) & 0xF) + yoff, (i >> 4) & 0xF, idmataval);
                     }
                     id = (newidmetaval >> 4);
                     meta = (newidmetaval & 0xF);
                     if ((id > 256) && (extblocks == null)) {
                         extblocks = new byte[2048];
                         sect.put("Add", new ByteArrayTag("Add", extblocks));
                     }
                     blocks[i] = (byte)(255 & id);
                     if (extblocks != null) {
                         extid = (byte) ((extid & 0x0F) | ((id >> 4) & 0xF0));
                     }
                     datavals = (byte) ((datavals & 0x0F) | ((meta << 4) & 0xF0));
                     bcnt++;
                 }
                 data[j] = (byte)(0xFF & datavals);
                 if (extblocks != null) {
                     extblocks[j] = (byte)(0xFF & extid);
                 }
                 i++;
             }
         }
         private void deleteTileEntity(int x, int y, int z, int idmeta) {
             if (new_tileents == null) {
                 new_tileents = new LinkedList<CompoundTag>(tileents);
             }
             Iterator<CompoundTag> te_iter = new_tileents.iterator();
             while (te_iter.hasNext()) {
                 CompoundTag te = te_iter.next();
                 CompoundMap ted = te.getValue();
                 if (ted == null) continue;
                 Integer tex = NBTMapper.getTagValue(ted.get("x"), Integer.class);
                 Integer tey = NBTMapper.getTagValue(ted.get("y"), Integer.class);
                 Integer tez = NBTMapper.getTagValue(ted.get("z"), Integer.class);
                 if ((tex == null) || (tey == null) || (tez == null)) continue;
                 // If matches on chunk relatve coordinates
                 if (((tex & 0xF) == x) && (tey == y) && ((tez & 0xF) == z)) {
                     te_iter.remove();
                     tescrubbed++;
                     return;
                 }
             }
         }
     }
     private static void defaultMap() {
         // Default to trivial mapping
         for (int i = 0; i < blkid_map.length; i++) {
             blkid_map[i] = i;
         }
         for (int i = 0; i < biome_blkid_map.length; i++) {
             biome_blkid_map[i] = null;
         }
         blkid_toss_tileentity.clear();
     }
     private static void processMapDefinition(MappingConfig cfg) throws IOException {
         if (cfg.blocks == null) {
             throw new IOException("'blocks' array not found.");
         }
         // Default the mapping
         defaultMap();
         // Traverse block mapping objects
         for (BlockMapping mb : cfg.blocks) {
             if (mb == null) continue;
 
             if (mb.biomes != null) {    // Biome specific?
                 // Mark blockID+meta as having biome specific mapping
                 if (mb.meta < 0) {
                     for (int meta = 0; meta < 16; meta++) {
                         blkid_biome_specific.set((mb.blkid*16) + meta);
                     }
                 }
                 else {
                     blkid_biome_specific.set((mb.blkid*16) + mb.meta);
                 }
                 for (int bidx = 0; bidx < mb.biomes.length; bidx++) {
                     int biomeid = findBiomeIndex(mb.biomes[bidx]);
                     if (biomeid < 0) {
                         throw new IOException("Invalid biome name: " + mb.biomes[bidx]);
                     }
                     if (biome_blkid_map[biomeid] == null) {
                         biome_blkid_map[biomeid] = new int[blkid_map.length];
                         for (int i = 0; i < blkid_map.length; i++) {
                             biome_blkid_map[biomeid][i] = i;
                         }
                     }
                     updateMapping(mb, biome_blkid_map[biomeid]);
                 }
             }
             else {
                 updateMapping(mb, blkid_map);
             }
         }
         // Print parsed mapping
         for (int i = 0; i < blkid_map.length; i++) {
             if (blkid_map[i] != i) {
                 System.out.println("Map " + (i>>4) + ":" + (i & 0xF) + " to " + (blkid_map[i] >> 4) + ":" + (blkid_map[i] & 0xF) + 
                         (blkid_toss_tileentity.get(i)?", discard tile entity":""));
             }
         }
     }
     private static void updateMapping(BlockMapping mb, int[] map) {
         // Now, fill in the mapping records
         if (mb.meta < 0) {
             for (int meta = 0; meta < 16; meta++) {
                 int idx = (mb.blkid*16) + meta;
                 if (mb.newmeta < 0)
                     map[idx] = (mb.newblkid * 16) + meta;
                 else
                     map[idx] = (mb.newblkid * 16) + mb.newmeta;
                 // If scrapping tile entity
                 if(mb.tosstileentity) {
                     blkid_toss_tileentity.set(idx);
                 }
             }   
         }
         else {
             if (mb.newmeta < 0)
                 map[(mb.blkid*16) + mb.meta] = (mb.newblkid * 16) + mb.meta;
             else
                 map[(mb.blkid*16) + mb.meta] = (mb.newblkid * 16) + mb.newmeta;
             if(mb.tosstileentity) {
                 blkid_toss_tileentity.set((mb.blkid*16) + mb.meta);
             }
         }
     }
     /**
      * Main routine for running mapper
      * 
      * @param args - <world directory> <map-file> <destination directory>
      */
     public static void main(String[] args) {
         if (args.length < 3) {
             System.err.println("Required arguments: src-world-dir map-file.json dest-world-dir");
             System.exit(1);
         }
         // Get and validate source directory
         File srcdir = new File(args[0]);
         if (!srcdir.isDirectory()) {
             System.err.println("Source '" + args[0] + "' must be existing world directory.");
             System.exit(1);
         }
         // Get and read map file
         File mapfile = new File(args[1]);
         if (!mapfile.isFile()) {
             System.err.println("Mapping file '" + args[0] + "' must be existing JSON encoded mapping file.");
             System.exit(1);
         }
         // Read and parse the file
         Gson parser = new Gson();
         Reader rdr = null;
         try {
             rdr = new FileReader(mapfile);
             MappingConfig cfg = parser.fromJson(rdr,  MappingConfig.class);
             processMapDefinition(cfg);
         } catch (JsonSyntaxException jsx) {
             System.err.println("Mapping file syntax error: " + jsx.getMessage());
             System.exit(1);
         } catch (JsonIOException jiox) {
             System.err.println("Mapping file I/O error: " + jiox.getMessage());
             System.exit(1);
         } catch (IOException iox) {
             System.err.println("Mapping file error: " + iox.getMessage());
             System.exit(1);
         } finally {
             if (rdr != null) { try { rdr.close(); } catch (IOException iox) {} }
         }
         // Check destination
         File destdir = new File(args[2]);
         if (destdir.exists() == false) {    // Create if needed
             destdir.mkdirs();
         }
         if (!destdir.isDirectory()) {
             System.err.println("Destination '" + args[1] + "' is not directory.");
             System.exit(1);
         }
         try {
             if (srcdir.getCanonicalPath().equals(destdir.getCanonicalPath())) {
                 System.err.println("Destination directory cannot be same as source directory.");
                 System.exit(1);
             }
         } catch (IOException e) {
             System.err.println("Destination directory cannot be same as source directory.");
             System.exit(1);
         }
         try {
             processWorldMapping(srcdir, destdir);
             
             System.out.println("World mapping completed");
             System.exit(0);
         } catch (IOException iox) {
             System.err.println(iox.getMessage());
             System.exit(1);
         }
     }
     
     private static void processWorldMapping(File src, File dest) throws IOException {
         File[] srcfiles = src.listFiles();
         if (srcfiles == null) return;
         
         for (File srcfile : srcfiles) {
             String srcname = srcfile.getName();
             if (srcfile.isDirectory()) {    // If directory, create copy in destination and recurse
                 File destdir = new File(dest, srcname);
                 destdir.mkdir();
                 processWorldMapping(srcfile, destdir);
             }
             else if (srcname.endsWith(".mca")) {    // If region file
                 processRegionFile(srcfile, new File(dest, srcname));
             }
             //TODO: other file types we need to handle : level.dat
             
             else {  // Else, just copy file
                 processFileCopy(srcfile, new File(dest, srcname));
             }
         }
     }
     // Process a region file
     private static void processRegionFile(File srcfile, File destfile) throws IOException {
         boolean success = false;
         int bcnt = 0;
         int tecnt = 0;
         int cupdated = 0;
        RegionFile destf = null;
         try {
             // Copy source file to destination
             processFileCopy(srcfile, destfile);
             // Load region file
            destf = new RegionFile(destfile);
             destf.load();
             int cnt = 0;
             for (int x = 0; x < 32; x++) {
                 for (int z = 0; z < 32; z++) {
                     if(destf.chunkExists(x, z)) {   // If chunk exists
                         cnt++;
                         Tag<?> tag = destf.readChunk(x, z);
                         if (tag == null) { System.err.println("Chunk " + x + "," + z + " exists but not read"); continue; }
                         MappedChunk mc = new MappedChunk(tag);
                         mc.processChunk();
                         // Test if updated
                         if (mc.bcnt > 0) {
                             bcnt += mc.bcnt;
                             tecnt += mc.tescrubbed;
                             cupdated++;
                             // Write updated chunk data
                             destf.writeChunk(x, z, mc.level);
                         }
                     }
                 }
             }
             success = true;
 
             System.out.println("Region " + destfile.getPath() + ", " + cnt + " chunks: updated " + bcnt + " blocks in " + cupdated + " chunks, " + tecnt + " TileEntities scrubbed");
         } finally {
             if (!success) {
                 destfile.delete();
             }
            if (destf != null) {
                destf.cleanup();
            }
         }
     }
     // Process a generic file (just copy)
     private static void processFileCopy(File srcfile, File destfile) throws IOException {
         byte[] buf = new byte[2048];
         FileInputStream fis = null;
         FileOutputStream fos = null;
         try {
             fis = new FileInputStream(srcfile);
             fos = new FileOutputStream(destfile);
             int rlen;
             while ((rlen = fis.read(buf)) > 0) {
                 fos.write(buf,  0,  rlen);
             }
         } finally {
             if (fis != null) { try { fis.close(); } catch (IOException iox) {} }
             if (fos != null) { try { fos.close(); } catch (IOException iox) {} }
         }
         System.out.println("Copied " + srcfile.getPath() + " to " + destfile.getPath());
     }
     private static int findBiomeIndex(String name) {
         String n = name.toLowerCase().replace(" ", "");
         for (int i = 0; i < biomes.length; i++) {
             if (biomes[i].equals(n)) {
                 return i;
             }
         }
         return -1;
     }
     private static int getBiomeSpecificID(int idmetaval, int biomeid) {
         // If biome specific mapping defined?
         if (blkid_biome_specific.get(idmetaval)) {
             int[] map = biome_blkid_map[biomeid];
             if (map != null) {
                 int id = map[idmetaval];
                 if (id != idmetaval) {
                     return id;
                 }
             }
         }
         return blkid_map[idmetaval];
     }
 
 }
