 package org.fizzbuzzwoof.businesslogic;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import static java.lang.String.format;
 import static java.lang.String.valueOf;
 
 public class NormalModeGame {
 	private static int INITIAL_VALUE = 1;
 
 	private final FizzBuzz.Type fizzBuzz;
 	private int counter = INITIAL_VALUE;
 	private int secondsLeft = 60;
 	private final Score score = new Score();
 	private LinkedList<String> messagesForUser = new LinkedList<String>();
 
 	public NormalModeGame(FizzBuzz.Type fizzBuzz) {
 		this.fizzBuzz = fizzBuzz;
 	}
 
 	public boolean userMadeMove(String answer) {
 		if (!fizzBuzz.isCorrectAnswer(answer, counter)) {
 			score.userWasWrong();
 			return false;
 		} else {
 			score.userWasRight();
 			counter++;
			if (counter == 49) messagesForUser.addLast("Please upgrade to premium version to access numbers above 50");
			if (counter == 50) messagesForUser.addLast("Nah.. just kidding :)");
 			return true;
 		}
 	}
 
 	public List<String> allPossibleMoves() {
 		return fizzBuzz.allChoices(counter);
 	}
 
 	public void oneSecondHasPassed() {
		if (secondsLeft == 1) messagesForUser.addLast("Your score is: " + score());
 		if (secondsLeft > 0) secondsLeft--;
 	}
 
 	public String timeLeft() {
 		if (secondsLeft == 60) return "01:00";
 		else return "00:" + format("%02d", secondsLeft);
 	}
 
 	public String score() {
 		return score.asString();
 	}
 
 	public String messageForUser() {
 		if (messagesForUser.isEmpty()) return null;
 		return messagesForUser.removeFirst();
 	}
 
 	public boolean isOver() {
 		return secondsLeft <= 0;
 	}
 
 
 	static class Score {
 		private int score = 0;
 		private int rightAnswersInARow = 0;
 		private int wrongAnswersInARow = 0;
 
 		public void userWasRight() {
 			rightAnswersInARow++;
 			wrongAnswersInARow = 0;
 			if (rightAnswersInARow > 10) {
 				score += (rightAnswersInARow / 10);
 			}
 			score++;
 		}
 
 		public void userWasWrong() {
 			rightAnswersInARow = 0;
 			wrongAnswersInARow++;
 			if (wrongAnswersInARow > 2) score--;
 		}
 
 		public String asString() {
 			return valueOf(score);
 		}
 	}
 }
