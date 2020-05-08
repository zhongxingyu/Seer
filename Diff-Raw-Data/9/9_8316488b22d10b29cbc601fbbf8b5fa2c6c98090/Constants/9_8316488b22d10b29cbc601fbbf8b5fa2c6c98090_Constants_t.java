 /*
  * StratoFlyer is a simple 2D space shooter built for educational purposes.
  * Copyright (C) 2012	Justin Zeng (Whackatre)
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package net.skyrealm.flyer;
 
 /**
  * Constants.java
  * @author justin.zeng1
  * 
  * Stores game constants.
  * >inb4s1gma.
  *
  */
 
 public class Constants {
 	/**
 	 * The title of the game.
 	 */
 	public static final String TITLE = "StratoFlyer";
 	
 	/**
 	 * The version of the game (for the title).
 	 */
 	public static final String VERSION = "2.3";
 	
 	/**
 	 * The path to the images/resources.
 	 */
 	public static final String PATH = "./images/";
 	
 	/**
 	 * The FPS rate which the game runs in.
 	 */
 	public static final int FPS_RATE = 60;
 	
 	/**
 	 * The dimensions of the game.
 	 */
 	public static final int WIDTH = 512;
 	public static final int HEIGHT = 512;
 	
 	/**
 	 * The max speed of the player in pixels.
 	 */
 	public static final int MAX_SPEED = 2;
 	
 	/**
 	 * The maximum amount of dots at one time.
 	 */
 	public static final int MAX_DOTS = 25;
 	
 	/**
 	 * The frequency of dot generation.
 	 * The higher the value, the less frequent dots appear.
 	 */
 	public static final int DOT_OCC_RATE = 20;
 	
 	/**
 	 * The speed of the dots.
 	 */
 	public static final int DOT_SPEED = 1;
 	
 	/**
 	 * Dot size (in pixels).
 	 */
 	public static final int DOT_SIZE = 4;
 	
 	/**
 	 * The speed of the bullets.
 	 */
 	public static final int BULLET_SPEED = 3;
 	
 	/**
 	 * The width of the bullet.
 	 */
 	public static final int BULLET_WIDTH = 8;
 	
 	/**
 	 * The height of the bullet.
 	 */
 	public static final int BULLET_HEIGHT = 16;
 	
 	/**
 	 * The pixel movement speed of the bad stars.
 	 */
 	public static final int STAR_SPEED = 2;
 	
 	/**
 	 * The interval at which the player is
 	 * allowed to shoot a bullet (in ms).
 	 */
 	public static final int SHOOT_INTERVAL = 300;
 	
 	/**
 	 * The player score multiplier.
 	 */
 	public static final int SCORE_MULTIPLIER = 1000;
 	
 	/**
 	 * The player's default health.
 	 */
 	public static final int PLR_DEFAULT_HEALTH = 10;
 	
 	/**
 	 * The occurance at which the stars
 	 * decide to shoot a bullet. The
 	 * higher the number, the less frequently
 	 * the star will decide to shoot.
 	 */
 	public static final int STAR_SHOOT_OCC = 1000;
 	
 	public static final int STAR_DIST_X = 25;
 	
 	public static final int STAR_DIST_Y = 15;

	
	public static final String TANK_IMAGE = "notsoenemyplane.gif";
	
	public static final String STAR_IMAGE = "Star_Ouro.gif";
	
	public static final String WIN_IMAGE = "YOU WIN.png";
	
	public static final String LOSE_IMAGE = "YOU LOSE.png";
 }
