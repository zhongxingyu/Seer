 package com.gamblerstools.racingpost.model;
 
 import com.gamblerstools.model.FinishingPosition;
 import com.gamblerstools.model.RaceOutcome;
 
 public class RacingPostFinishingPosition implements
		FinishingPosition {
 
 	private RaceOutcome outcome;
 	private Integer position;
 
 	public RacingPostFinishingPosition(RaceOutcome outcome, Integer position) {
 		this.outcome = outcome;
 		this.position = position;
 		validateOutcomePositionCombination(outcome, position);
 	}
 
 	private void validateOutcomePositionCombination(RaceOutcome outcome, Integer position) {
 		if(outcome==RaceOutcome.FINISHED) {
 			if(position==null || position<=0 ) {
 				throw new IllegalArgumentException("Position ("+position+") must be a positive integer when outcome is "+outcome.name());
 			}
 		} else {
 			if(position!=null) {
 				throw new IllegalArgumentException("Position ("+position+") must be null when outcome is "+outcome.name());
 			}
 		}
 	}
 
 	public RacingPostFinishingPosition(RaceOutcome outcome) {
 		this(outcome, null);
 	}
 
 	@Override
 	public Integer getFinishingPosition() {
 		return position;
 	}
 
 	@Override
 	public RaceOutcome getRaceOutcome() {
 		return outcome;
 	}
 
 	@Override
 	public String toString() {
 		return "RacingPostFinishingPosition [outcome=" + outcome
 				+ ", position=" + position + "]";
 	}
 
 	@Override
 	public int compareTo(FinishingPosition other) {
 		if (this.getRaceOutcome() == RaceOutcome.FINISHED) {
 			if (other.getRaceOutcome() == RaceOutcome.FINISHED) {
 				// both finished
 				return this.getFinishingPosition()
 						- other.getFinishingPosition();
 			} else {
 				// this finished, other didn't
 				return -getFinishingPosition();
 			}
 		} else {
 			if (other.getRaceOutcome() == RaceOutcome.FINISHED) {
 				// other finished, this didn't
 				return other.getFinishingPosition();
 			} else {
 				// Neither finished
 				return 0;
 			}
 		}
 	}
 
 }
