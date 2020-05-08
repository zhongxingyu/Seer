 /**
  * Copyright 2012 www.tilepacker.org
  */
 package org.tilepacker.core;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.GnuParser;
 import org.apache.commons.cli.ParseException;
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.BasicGame;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.SpriteSheet;
 
 /**
  * Main entry class
  * @author Thomas Cashman
  */
 public class TilePacker extends BasicGame {
 	public static String FORMAT = "PNG";
 	public static String SOURCE_DIRECTORY = "";
 	public static String TARGET_DIRECTORY = "";
 
 	private File[] inputFiles;
 	private List<Tileset> tilesets;
 
 	/**
 	 * Constructor
 	 */
 	public TilePacker() {
 		super("TilePacker");
 		tilesets = new ArrayList<Tileset>();
 	}
 
 	@Override
 	public void init(GameContainer gc) throws SlickException {
 		File sourceDirectory = new File(SOURCE_DIRECTORY);
 		inputFiles = sourceDirectory.listFiles();
 		Tileset tileset = new Tileset();
 		for(int i = 0; i < inputFiles.length; i++) {
 			if(inputFiles[i].getAbsolutePath().endsWith("." + FORMAT.toLowerCase())) {
 				System.out.println("INFO: Reading " + inputFiles[i].getAbsolutePath());
 				SpriteSheet spriteSheet = new SpriteSheet(inputFiles[i].getAbsolutePath(), Tile.WIDTH, Tile.HEIGHT);
 				for(int x = 0; x < spriteSheet.getHorizontalCount(); x++) {
 					for(int y = 0; y < spriteSheet.getVerticalCount(); y++) {
 						Tile tile = new Tile(spriteSheet.getSubImage(x, y));
 						if(!tileset.add(tile)) {
 							tilesets.add(tileset);
 							tileset = new Tileset();
 						}
 					}
 				}
 			}
 		}
 		tilesets.add(tileset);
 	}
 
 	@Override
 	public void update(GameContainer gc, int delta) throws SlickException {}
 
 	public void render(GameContainer gc, Graphics g) throws SlickException {
 		for(int i = 0; i < tilesets.size(); i++) {
 			System.out.println("INFO: Saving tileset - " + i);
 			tilesets.get(i).save(TARGET_DIRECTORY + i + "." + FORMAT.toLowerCase(), FORMAT);
 		}
 		System.exit(0);
 	}
 
 	public static void main(String[] args) {
 		CommandLineParser parser = new GnuParser();
 		CommandLine commandLine = null;
 		try {
 			commandLine = parser.parse(new TilePackerOptions(), args);
 		} catch (ParseException exp) {
 			System.err.println("ERROR: " + exp.getMessage());
 			return;
 		}
 
 		Tile.WIDTH = Integer.parseInt(commandLine
 				.getOptionValue(TilePackerOptions.TILE_WIDTH));
 		Tile.HEIGHT = Integer.parseInt(commandLine
 				.getOptionValue(TilePackerOptions.TILE_HEIGHT));
 		Tileset.MAX_WIDTH = Integer.parseInt(commandLine
 				.getOptionValue(TilePackerOptions.TILESET_WIDTH));
 		Tileset.MAX_HEIGHT = Integer.parseInt(commandLine
 				.getOptionValue(TilePackerOptions.TILESET_HEIGHT));
 		TilePacker.FORMAT = commandLine
 				.getOptionValue(TilePackerOptions.FORMAT);
 		TilePacker.SOURCE_DIRECTORY = commandLine
 				.getOptionValue(TilePackerOptions.SOURCE_DIRECTORY);
 		TilePacker.TARGET_DIRECTORY = commandLine
 				.getOptionValue(TilePackerOptions.TARGET_DIRECTORY);
 
 		try {
 			AppGameContainer gc = new AppGameContainer(new TilePacker(),
 					Tileset.MAX_WIDTH, Tileset.MAX_HEIGHT, false);
 			gc.start();
 		} catch (Exception e) {
 			e.printStackTrace();
 			return;
 		}
 	}
 }
