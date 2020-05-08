 package cs309.a1.shared;
 
import cs309.a1.player.activities.EnterNameActivty;
 
 /**
  * This class will be used to represent constants in a common game. Each of the
  * constants below are classified by a section of the game in which they will be
  * used.
  */
 public class Constants {
 
 	/* GUI */
 	/**
 	 * A constant to limit the number of characters for a players name
 	 */
 	public static final int NAME_MAX_CHARS = 10;
 
 	/**
 	 * The display size of a card on a small handheld
 	 */
 	public static final int CARD_IMAGE_SCALE_SMALL = 200;
 
 	/**
 	 * The display size of a card on a medium handheld
 	 */
 	public static final int CARD_IMAGE_SCALE_MED = 400;
 
 	/**
 	 * The display size of a card on a large handheld
 	 */
 	public static final int CARD_IMAGE_SCALE_LARGE = 500;
 
 	/**
 	 * The maximum number of cards to be displayed on longest sides of tablet
 	 */
 	public static final int MAX_DISPLAYED = 13;
 
 	/**
 	 * The maximum number of cards to be displayed on shortest sides of tablet
 	 */
 	public static final int MAX_DIS_SIDES = 7;
 
 	/**
 	 * A constant to represent the name of the font to use
 	 */
 	public static final String FONT_NAME = "hammersmithone.ttf";
 
 	/* Game Constants */
 	/**
 	 * A constant to represent the Clubs suit
 	 */
 	public static final int SUIT_CLUBS = 0;
 
 	/**
 	 * A constant to represent the Diamonds suit
 	 */
 	public static final int SUIT_DIAMONDS = 1;
 
 	/**
 	 * A constant to represent the Hearts suit
 	 */
 	public static final int SUIT_HEARTS = 2;
 
 	/**
 	 * A constant to represent the Spades suit
 	 */
 	public static final int SUIT_SPADES = 3;
 
 	/**
 	 * A constant to represent the jokers
 	 */
 	public static final int SUIT_JOKER = 4;
 
 	/**
 	 * This is used by the connections screen to pass the player 1 name that
 	 * have been entered
 	 */
 	public static final String PLAYER_1 = "player1";
 
 	/**
 	 * This is used by the connections screen to pass the player 2 name that
 	 * have been entered
 	 */
 	public static final String PLAYER_2 = "player2";
 
 	/**
 	 * This is used by the connections screen to pass the player 3 name that
 	 * have been entered
 	 */
 	public static final String PLAYER_3 = "player3";
 
 	/**
 	 * This is used by the connections screen to pass the player 4 name that
 	 * have been entered
 	 */
 	public static final String PLAYER_4 = "player4";
 
 	/* JSON keys */
 	/**
 	 * this is a JSON key for the suit of a card
 	 */
 	public static final String SUIT = "suit";
 
 	/**
 	 * this is a JSON key for the value of a card
 	 */
 	public static final String VALUE = "value";
 
 	/**
 	 * this is a JSON key for the resource id of a card
 	 */
 	public static final String RESOURCE_ID = "resourceid";
 
 	/**
 	 * this is a JSON key for a card id
 	 */
 	public static final String ID = "id";
 
 	/**
 	 * this is the key for message type this is used to obtain the message type
 	 * information from the message sent over wireless
 	 */
 	public static final String MESSAGE_TYPE = "messagetype";
 
 	/**
 	 * this is a JSON key for getting the isTurn boolean
 	 */
 	public static final String TURN = "isturn";
 
 	/**
 	 * this is a JSON key for getting the player name
 	 */
 	public static final String PLAYER_NAME = "playername";
 
 	/**
 	 * this is a request code for the get EnterNameActivity
 	 */
	public static final int GET_PLAYER_NAME = Math.abs(EnterNameActivty.class
			.hashCode());
 
 	/* wireless connection message codes */
 	/**
 	 * message type for setting up a game
 	 */
 	public static final int SETUP = 0;
 
 	/**
 	 * message type for telling a player it is their turn
 	 */
 	public static final int IS_TURN = 1;
 
 	/**
 	 * message type for sending the card that was drawn
 	 */
 	public static final int CARD_DRAWN = 2;
 
 	/**
 	 * message type telling the player they won
 	 */
 	public static final int WINNER = 3;
 
 	/**
 	 * message type telling the player they lost
 	 */
 	public static final int LOSER = 4;
 
 	/**
 	 * message type for refreshing the game state by re-sending cards and whose
 	 * turn it is to all players
 	 */
 	public static final int REFRESH = 5;
 
 	/**
 	 * message type for pausing the game
 	 */
 	public static final int PAUSE = 6;
 
 	/**
 	 * message type for unpausing the game
 	 */
 	public static final int UNPAUSE = 7;
 
 	/**
 	 * message type for telling the players to end their game
 	 */
 	public static final int END_GAME = 8;
 
 	/**
 	 * message types that a player sends when playing a card
 	 */
 	public static final int PLAY_CARD = 9;
 
 	/**
 	 * message type that a player sends when requesting to draw a card
 	 */
 	public static final int DRAW_CARD = 10;
 
 	/* Language Options */
 	/**
 	 * A string representing the US locale
 	 */
 	public static final String LANGUAGE_US = "US";
 
 	/**
 	 * A string representing the German locale
 	 */
 	public static final String LANGUAGE_GERMAN = "German";
 
 	/**
 	 * A string representing the France locale
 	 */
 	public static final String LANGUAGE_FRANCE = "France";
 
 	/**
 	 * A string representing the Canada locale
 	 */
 	public static final String LANGUAGE_CANADA = "Canada";
 
 	/**
 	 * A string representing the UK locale
 	 */
 	public static final String LANGUAGE_UK = "UK";
 
 	/* Preferences Options */
 	/**
 	 * The name of the shared preferences to be used when getting the object
 	 */
 	public static final String PREFERENCES = "PREFERENCES";
 
 	/**
 	 * A constant representing the difficulty of the AI players in the game for
 	 * the preferences
 	 */
 	public static final String DIFFICULTY_OF_COMPUTERS = "DIFFICULTY OF COMPUTERS";
 
 	/**
 	 * A constant representing the computer difficulty easy
 	 */
 	public static final String EASY = "Easy";
 
 	/**
 	 * A constant representing the computer difficulty medium
 	 */
 	public static final String MEDIUM = "Medium";
 
 	/**
 	 * A constant representing the computer difficulty hard
 	 */
 	public static final String HARD = "Hard";
 
 	/**
 	 * A constant representing the connection type for the shared preferences
 	 */
 	public static final String CONNECTION_TYPE = "CONNECTION TYPE";
 
 	/**
 	 * A constant representing the connection type bluetooth
 	 */
 	public static final String BLUETOOTH = "Bluetooth";
 
 	/**
 	 * A constant representing the connection type wifi
 	 */
 	public static final String WIFI = "WiFi";
 
 	/**
 	 * A constant to represent a key in the shared preferences for the sound
 	 * effects option
 	 */
 	public static final String SOUND_EFFECTS = "SOUND EFFECTS";
 
 	/**
 	 * A constant to represent a key in the shared preferences for the speech
 	 * volume option
 	 */
 	public static final String SPEECH_VOLUME = "SPEECH VOLUME";
 
 	/**
 	 * A constant to represent a key in the shared preferences for the
 	 * language/locale option
 	 */
 	public static final String LANGUAGE = "LANGUAGE";
 
 	/**
 	 * A constant to represent a key in the shared preferences for the number of
 	 * computers in the game option
 	 */
 	public static final String NUMBER_OF_COMPUTERS = "NUMBER OF COMPUTERS";
 
 	/**
 	 * A constant to represent a key in the shared preferences for the game type
 	 */
 	public static final String GAME_TYPE = "GAME TYPE";
 
 	/**
 	 * A constant to represent a key in the shared preferences for the game type
 	 * crazy eights
 	 */
 	public static final String CRAZY_EIGHTS = "Crazy Eights";
 
 	/* AI Constants */
 	/**
 	 * The time you wait in between computer turns, about 1 and a half seconds
 	 * now
 	 */
 	public static final long COMPUTER_WAIT_TIME = 1500;
 }
