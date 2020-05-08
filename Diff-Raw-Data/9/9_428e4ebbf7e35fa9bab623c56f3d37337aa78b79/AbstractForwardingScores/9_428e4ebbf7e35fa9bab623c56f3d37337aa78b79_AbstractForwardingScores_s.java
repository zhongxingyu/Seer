 /*
 	cursus - Race series management program
 	Copyright 2011  Simon Arlott
 
 	This program is free software: you can redistribute it and/or modify
 	it under the terms of the GNU General Public License as published by
 	the Free Software Foundation, either version 3 of the License, or
 	(at your option) any later version.
 
 	This program is distributed in the hope that it will be useful,
 	but WITHOUT ANY WARRANTY; without even the implied warranty of
 	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 	GNU General Public License for more details.
 
 	You should have received a copy of the GNU General Public License
 	along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package eu.lp0.cursus.scoring;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import com.google.common.collect.LinkedListMultimap;
 import com.google.common.collect.Table;
 
 import eu.lp0.cursus.db.data.Pilot;
 import eu.lp0.cursus.db.data.Race;
 
 public abstract class AbstractForwardingScores implements Scores {
 	protected abstract ScoredData delegateScoredData();
 
 	protected abstract ScoresFactorySubset delegateScoresFactory();
 
 	protected abstract RaceLapsData delegateRaceLapsData();
 
 	protected abstract RacePointsData delegateRacePointsData();
 
 	protected abstract RacePenaltiesData delegateRacePenaltiesData();
 
 	protected abstract RacePositionsData delegateRacePositionsData();
 
 	protected abstract RaceDiscardsData delegateRaceDiscardsData();
 
 	protected abstract OverallPenaltiesData delegateOverallPenaltiesData();
 
 	protected abstract OverallPointsData delegateOverallPointsData();
 
 	protected abstract OverallPositionData delegateOverallPositionData();
 
 	// ScoredData
 	@Override
 	public Set<Pilot> getPilots() {
 		return delegateScoredData().getPilots();
 	}
 
 	@Override
 	public List<Race> getRaces() {
 		return delegateScoredData().getRaces();
 	}
 
 	// ScoresFactorySubset
 	@Override
 	public RaceLapsData newRaceLapsData(Scores scores) {
 		return delegateScoresFactory().newRaceLapsData(scores);
 	}
 
 	@Override
 	public RacePenaltiesData newRacePenaltiesData(Scores scores) {
 		return delegateScoresFactory().newRacePenaltiesData(scores);
 	}
 
 	@Override
 	public RacePointsData newRacePointsData(Scores scores) {
 		return delegateScoresFactory().newRacePointsData(scores);
 	}
 
 	@Override
 	public RacePositionsData newRacePositionsData(Scores scores) {
 		return delegateScoresFactory().newRacePositionsData(scores);
 	}
 
 	@Override
 	public RaceDiscardsData newRaceDiscardsData(Scores scores) {
 		return delegateScoresFactory().newRaceDiscardsData(scores);
 	}
 
 	@Override
 	public OverallPenaltiesData newOverallPenaltiesData(Scores scores) {
 		return delegateScoresFactory().newOverallPenaltiesData(scores);
 	}
 
 	@Override
 	public OverallPointsData newOverallPointsData(Scores scores) {
 		return delegateScoresFactory().newOverallPointsData(scores);
 	}
 
 	@Override
 	public OverallPositionData newOverallPositionData(Scores scores) {
 		return delegateScoresFactory().newOverallPositionData(scores);
 	}
 
 	// RaceLapsData
 	@Override
 	public int getLaps(Pilot pilot, Race race) {
 		return delegateRaceLapsData().getLaps(pilot, race);
 	}
 
 	@Override
 	public Map<Race, Integer> getLaps(Pilot pilot) {
 		return Collections.unmodifiableMap(delegateRaceLapsData().getLaps(pilot));
 	}
 
 	@Override
 	public Map<Pilot, Integer> getLaps(Race race) {
 		return Collections.unmodifiableMap(delegateRaceLapsData().getLaps(race));
 	}
 
 	@Override
 	public List<Pilot> getLapOrder(Race race) {
 		return Collections.unmodifiableList(delegateRaceLapsData().getLapOrder(race));
 	}
 
 	// RacePointsData
 	@Override
 	public Table<Pilot, Race, Integer> getRacePoints() {
 		return delegateRacePointsData().getRacePoints();
 	}
 
 	@Override
 	public int getRacePoints(Pilot pilot, Race race) {
 		return delegateRacePointsData().getRacePoints(pilot, race);
 	}
 
 	@Override
 	public Map<Race, Integer> getRacePoints(Pilot pilot) {
 		return Collections.unmodifiableMap(delegateRacePointsData().getRacePoints(pilot));
 	}
 
 	@Override
 	public Map<Pilot, Integer> getRacePoints(Race race) {
 		return Collections.unmodifiableMap(delegateRacePointsData().getRacePoints(race));
 	}
 
 	@Override
 	public Map<Race, Collection<Pilot>> getSimulatedRacePoints() {
 		return Collections.unmodifiableMap(delegateRacePointsData().getSimulatedRacePoints());
 	}
 
 	@Override
 	public Map<Pilot, Collection<Race>> getSimulatedPilotPoints() {
 		return Collections.unmodifiableMap(delegateRacePointsData().getSimulatedPilotPoints());
 	}
 
 	@Override
 	public boolean hasSimulatedRacePoints(Pilot pilot, Race race) {
 		return delegateRacePointsData().hasSimulatedRacePoints(pilot, race);
 	}
 
 	@Override
 	public Collection<Race> getSimulatedRacePoints(Pilot pilot) {
 		return Collections.unmodifiableCollection(delegateRacePointsData().getSimulatedRacePoints(pilot));
 	}
 
 	@Override
 	public Collection<Pilot> getSimulatedRacePoints(Race race) {
 		return Collections.unmodifiableCollection(delegateRacePointsData().getSimulatedRacePoints(race));
 	}
 
 	@Override
 	public int getFleetSize(Race race) {
 		return delegateRacePointsData().getFleetSize(race);
 	}
 
 	// RacePenaltiesData
 	@Override
 	public Table<Pilot, Race, Integer> getRacePenalties() {
 		return delegateRacePenaltiesData().getRacePenalties();
 	}
 
 	@Override
 	public Map<Race, Integer> getRacePenalties(Pilot pilot) {
 		return Collections.unmodifiableMap(delegateRacePenaltiesData().getRacePenalties(pilot));
 	}
 
 	@Override
 	public Map<Pilot, Integer> getRacePenalties(Race race) {
 		return Collections.unmodifiableMap(delegateRacePenaltiesData().getRacePenalties(race));
 	}
 
 	@Override
 	public int getRacePenalties(Pilot pilot, Race race) {
 		return delegateRacePenaltiesData().getRacePenalties(pilot, race);
 	}
 
 	// RacePositionsData
 	@Override
 	public Table<Race, Pilot, Integer> getRacePositions() {
 		return delegateRacePositionsData().getRacePositions();
 	}
 
 	@Override
 	public Map<Pilot, Integer> getRacePositions(Race race) {
 		return Collections.unmodifiableMap(delegateRacePositionsData().getRacePositions(race));
 	}
 
 	@Override
 	public Map<Race, LinkedListMultimap<Integer, Pilot>> getRacePositionsWithOrder() {
 		return Collections.unmodifiableMap(delegateRacePositionsData().getRacePositionsWithOrder());
 	}
 
 	@Override
 	public LinkedListMultimap<Integer, Pilot> getRacePositionsWithOrder(Race race) {
 		return delegateRacePositionsData().getRacePositionsWithOrder(race);
 	}
 
 	@Override
 	public int getRacePosition(Pilot pilot, Race race) {
 		return delegateRacePositionsData().getRacePosition(pilot, race);
 	}
 
 	@Override
 	public Map<Race, List<Pilot>> getRaceOrders() {
 		return Collections.unmodifiableMap(delegateRacePositionsData().getRaceOrders());
 	}
 
 	@Override
 	public List<Pilot> getRaceOrder(Race race) {
 		return Collections.unmodifiableList(delegateRacePositionsData().getRaceOrder(race));
 	}
 
 	// RaceDiscardsData
 	@Override
 	public Table<Pilot, Integer, Integer> getRaceDiscards() {
 		return delegateRaceDiscardsData().getRaceDiscards();
 	}
 
 	@Override
 	public Map<Integer, Integer> getRaceDiscards(Pilot pilot) {
 		return Collections.unmodifiableMap(delegateRaceDiscardsData().getRaceDiscards(pilot));
 	}
 
 	@Override
 	public Map<Pilot, Integer> getRaceDiscards(int discard) {
 		return Collections.unmodifiableMap(delegateRaceDiscardsData().getRaceDiscards(discard));
 	}
 
 	@Override
 	public int getRaceDiscard(Pilot pilot, int discard) {
 		return delegateRaceDiscardsData().getRaceDiscard(pilot, discard);
 	}
 
 	@Override
 	public Table<Pilot, Integer, Race> getDiscardedRaces() {
 		return delegateRaceDiscardsData().getDiscardedRaces();
 	}
 
 	@Override
 	public Map<Integer, Race> getDiscardedRaces(Pilot pilot) {
 		return Collections.unmodifiableMap(delegateRaceDiscardsData().getDiscardedRaces(pilot));
 	}
 
 	// OverallPenaltiesData
 	@Override
 	public Map<Pilot, Integer> getOverallPenalties() {
 		return Collections.unmodifiableMap(delegateOverallPenaltiesData().getOverallPenalties());
 	}
 
 	@Override
 	public int getOverallPenalties(Pilot pilot) {
 		return delegateOverallPenaltiesData().getOverallPenalties(pilot);
 	}
 
 	// OverallPointsData
 	@Override
 	public Map<Pilot, Integer> getOverallPoints() {
 		return Collections.unmodifiableMap(delegateOverallPointsData().getOverallPoints());
 	}
 
 	@Override
 	public int getOverallPoints(Pilot pilot) {
		return getOverallPoints(pilot);
 	}
 
 	@Override
 	public int getOverallFleetSize() {
 		return delegateOverallPointsData().getOverallFleetSize();
 	}
 
 	// OverallPositionData
 	@Override
 	public Map<Pilot, Integer> getOverallPositions() {
 		return Collections.unmodifiableMap(delegateOverallPositionData().getOverallPositions());
 	}
 
 	@Override
 	public LinkedListMultimap<Integer, Pilot> getOverallPositionsWithOrder() {
 		return delegateOverallPositionData().getOverallPositionsWithOrder();
 	}
 
 	@Override
 	public int getOverallPosition(Pilot pilot) {
 		return delegateOverallPositionData().getOverallPosition(pilot);
 	}
 
 	@Override
 	public List<Pilot> getOverallOrder() {
 		return Collections.unmodifiableList(delegateOverallPositionData().getOverallOrder());
 	}
 }
