 package sammko.quantumCraft.resources;
 
 public class BlockTextureMatrix {
 	
 
 	//texture layout in the file. 0x = Hex; 1st digit = Columns, 2nd digit = Rows. 0 - F (0 - 15)
 	public static TextureIndex OrePositronium = new TextureIndex(0xE0);
 	public static TextureIndex OrePlutonium = new TextureIndex(0xF0);
 	public static TextureIndex OreRadium = new TextureIndex(0xE1);
	public static TextureIndex OreGammatronium = new TextureIndex(0xF2);
	public static TextureIndex OreNeutrinium = new TextureIndex(0xF1);
 	public static TextureIndex OreDepleted = new TextureIndex(0xE2);
 	
 	public static TextureIndex Err = new TextureIndex(0x14);
 	
 	public static TextureIndex[] Deco = {
 		new TextureIndex(0x50), new TextureIndex(0x70), new TextureIndex(0x60), new TextureIndex(0x80), new TextureIndex(0x90),
 		new TextureIndex(0x51), new TextureIndex(0x71), new TextureIndex(0x61), new TextureIndex(0x81), new TextureIndex(0x91),
 		new TextureIndex(0x52), new TextureIndex(0x72), new TextureIndex(0x62), new TextureIndex(0x82), new TextureIndex(0x92),
 		new TextureIndex(0x53)
 	};
 	
 	public static TextureIndex[] MachineFront = {
 		new TextureIndex(0x74), new TextureIndex(0x84), new TextureIndex(0x94), new TextureIndex(0xA4),
 		new TextureIndex(0x75), new TextureIndex(0x85), new TextureIndex(0x95), new TextureIndex(0xA5),
 		new TextureIndex(0x76), new TextureIndex(0x86), new TextureIndex(0x96), new TextureIndex(0xA6),
 		new TextureIndex(0x77), new TextureIndex(0x87), new TextureIndex(0x97), new TextureIndex(0xA7)
 	};
 	
 	public static TextureIndex[] MachineTop = {
 		new TextureIndex(0x79), new TextureIndex(0x7A), new TextureIndex(0x7B), new TextureIndex(0x7C),
 		new TextureIndex(0x89), new TextureIndex(0x8A), new TextureIndex(0x8B), new TextureIndex(0x8C),
 		new TextureIndex(0x99), new TextureIndex(0x9A), new TextureIndex(0x9B), new TextureIndex(0x9C),
 		new TextureIndex(0xA9), new TextureIndex(0xAA), new TextureIndex(0xAB), new TextureIndex(0xAC)
 	};
 	
 	public static TextureIndex EBlockBackSingle = new TextureIndex(0x00);
 	public static TextureIndex EBlockBackTop = new TextureIndex(0x01);
 	public static TextureIndex EBlockBackMid = new TextureIndex(0x02);
 	public static TextureIndex EBlockBackBot = new TextureIndex(0x03);
 	public static TextureIndex EBlockSideSingle = new TextureIndex(0x04);
 }
