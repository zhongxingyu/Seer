 package com.game.mobileblocks;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import com.game.Units.Block;
 
 public class Level {
 
 	private int maxSpeed;
 	private int minSpeed;
 	private int maxBlocksPerRow;
 	private int blockGenSpeed;
 	private int gameWidth;
	private int creationSpeed;
 	
 	private List<Block> blockList;
 	private Random rand;
 	private ColorPicker colorPicker = new ColorPicker();
 	
 	public Level(int gameWidth) {
 		this(5, 1, 3, 5, gameWidth);
 	}
 	
 	public Level(int maxSpeed, int minSpeed, int maxBlocksPerRow,
 			int blockGenSpeed, int gameWidth) {
 		this.maxSpeed = maxSpeed;
 		this.minSpeed = minSpeed;
 		this.maxBlocksPerRow = maxBlocksPerRow;
 		this.blockGenSpeed = blockGenSpeed;
 		this.gameWidth = gameWidth;
		this.creationSpeed = 500;
	}
	
	public int getCreationSpeed()
	{
		return creationSpeed;
 	}
 	
 	public List<Block> randomBlocks() {
 		rand = new Random();
 		Block currBlock;
 		blockList = new ArrayList<Block>();
 		for (int i = 0; i < rand.nextInt(maxBlocksPerRow); i++) {
 			currBlock = new Block(rand.nextInt(gameWidth - Block.width),0, colorPicker.randomColor());
 			while (collisionCheck(currBlock, blockList)) {
 				currBlock = new Block(rand.nextInt(gameWidth - Block.width),0,colorPicker.randomColor());
 			}
 			blockList.add(currBlock);
 		}
 		return blockList;
 	}
 	
 	public boolean collisionCheck(Block currBlock, List<Block> blockList) {
 		for (Block b : blockList) {
 			if (currBlock.isCollisionBlock(b.getX(), b.getY(), b.getWidth(), b.getHeight())) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public void update(int score) {
 		if (score % 30 == 0) {
 			maxSpeed++;
 		}
 		if (score % 35 == 0) {
 			minSpeed++;
 		}
 		if (score % 50 == 0) {
 			blockGenSpeed++;
 			maxBlocksPerRow++;
 		}
 	}
 	
 	
 }
