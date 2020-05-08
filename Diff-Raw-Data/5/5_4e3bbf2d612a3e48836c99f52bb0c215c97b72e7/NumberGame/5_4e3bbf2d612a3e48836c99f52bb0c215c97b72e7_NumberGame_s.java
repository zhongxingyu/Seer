 package it.chalmers.tendu.gamemodel.numbergame;
 
 import it.chalmers.tendu.defaults.Constants.Difficulty;
 import it.chalmers.tendu.gamemodel.GameId;
 import it.chalmers.tendu.gamemodel.MiniGame;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import com.badlogic.gdx.Gdx;
 
 public class NumberGame extends MiniGame {
 
 	private int playerCount;
 	private ArrayList<Integer> answerList;
 	private Map<Integer, ArrayList<Integer>> playerLists;
 	private int nbrCorrectAnswer;
 
 	/** No args constructor for reflection use */
 	protected NumberGame() {
 		super();
 	};
 
 	public NumberGame(int addTime, Difficulty difficulty, Map<String, Integer> players) {
 		super(addTime, difficulty, GameId.NUMBER_GAME, players);
 		nbrCorrectAnswer = 0;
 		playerCount = players.size();
 		switch (difficulty) {
 		case ONE:
 			this.setEndTime(30000);
 			answerList = createAnswer(playerCount);
 			break;
 		case TWO:
 			this.setEndTime(30000);
 			answerList = createAnswer(playerCount*2);
 			break;
 		default:
 			// TODO:
 			Gdx.app.debug("NumberGame Class", "Fix this switch case");
 		}
 		playerLists = divideAndConquer(answerList);
 
 	}
 
 	/**
 	 * Check if the number chosen is the right one according to the answerList
 	 * and sets gamestate to gameWon if all the numbers in answerList have been
 	 * correctly guessed.
 	 * 
 	 * @param num
 	 * @return
 	 */
 	public boolean checkNbr(int num) {
 		// TODO make sure it can't go out of bounds (make it prettier)
 		if (nbrCorrectAnswer < answerList.size()) {
 			if (answerList.get(nbrCorrectAnswer) == num) {
 				nbrCorrectAnswer++;
 				if (nbrCorrectAnswer == answerList.size()) {
 					gameWon();
 				}
 				return true;
 			} else {
 				this.changeTimeWith(-3000);
 				return false;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Returns a list with random numbers from 1-99 that represents the correct
 	 * answer in the game.
 	 * 
 	 * @param length
 	 * @return
 	 */
 	private ArrayList<Integer> createAnswer(int length) {
 		ArrayList<Integer> answerList = new ArrayList<Integer>();
 		int i = 0;
 		while (i < length) {
 			int randomNbr = 1 + (int) (Math.random() * 99);
 			if (!(answerList.contains(randomNbr))) {
 				answerList.add(randomNbr);
 				i++;
 			}
 		}
 		return answerList;
 	}
 
 	/**
 	 * Give each player a part of the answer.
 	 * 
 	 * @param list
 	 * @return
 	 */
 	private Map<Integer, ArrayList<Integer>> divideAndConquer(
 			ArrayList<Integer> list) {
 
 		Map<Integer, ArrayList<Integer>> newMap = new HashMap<Integer, ArrayList<Integer>>();
 
 		ArrayList<Integer> temp = new ArrayList<Integer>(answerList);
 
 		Collections.shuffle(temp);
 
 		for (int i = 0; i < playerCount; i++) {
 			ArrayList<Integer> newList = new ArrayList<Integer>();
 
 			for (int j = 0; j < answerList.size() / playerCount; j++) {
 				Integer r = temp.remove(0);
 				newList.add(r);
 			}
 			popAndShuffleList(newList);
 			newMap.put(i, newList);
 		}
 		return newMap;
 
 	}
 
 	/**
 	 * Fills up an array with random numbers until there are eight different
 	 * numbers in the array total and shuffles them.
 	 * 
 	 * @param list
 	 */
 	private void popAndShuffleList(ArrayList<Integer> list) {
 		int i = 0;
 		int length = list.size();
 		while (i < (8 - length)) {
 			int randomNbr = 1 + (int) (Math.random() * 99);
			if (!(list.contains(randomNbr))) {
 				list.add(randomNbr);
 				i++;
 			}
 		}
 		Collections.shuffle(list);
 	}
 
 	public ArrayList<Integer> getAnswerList() {
 		return answerList;
 	}
 
 	/**
	 * Return the indicated players list of numbers.
 	 * 
 	 * @param player
 	 * @return
 	 */
 	public ArrayList<Integer> getMyList() {
 		int playerNbr = getplayerNbr();
 		return playerLists.get(playerNbr);
 		// return answerList;
 	}
 
 	/**
 	 * Returns the numbers that have been answered correctly.
 	 * 
 	 * @return
 	 */
 	public ArrayList<Integer> getAnsweredNbrs() {
 		ArrayList<Integer> list = new ArrayList<Integer>();
 		for (int i = 0; i < nbrCorrectAnswer; i++) {
 			list.add(answerList.get(i));
 		}
 		return list;
 	}
 
 }
