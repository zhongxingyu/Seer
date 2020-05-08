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
 
 import java.util.Comparator;
 import java.util.List;
 import java.util.Map;
 
 import com.google.common.base.Predicates;
 import com.google.common.base.Supplier;
 import com.google.common.base.Suppliers;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Ordering;
 
 import eu.lp0.cursus.db.data.Pilot;
 
 public class PilotRacePlacingComparator<T extends ScoredData & RacePointsData & RaceDiscardsData> implements Comparator<Pilot> {
 	private final T scores;
 	private final PlacingMethod method;
 	private final Map<Pilot, Supplier<List<Integer>>> racePlacings;
 
 	public enum PlacingMethod {
 		INCLUDING_DISCARDS, EXCLUDING_DISCARDS;
 	}
 
 	public PilotRacePlacingComparator(T scores, PlacingMethod method) {
 		this.scores = scores;
 		this.method = method;
 
 		ImmutableMap.Builder<Pilot, Supplier<List<Integer>>> tmp = ImmutableMap.builder();
 		for (Pilot pilot : scores.getPilots()) {
 			tmp.put(pilot, Suppliers.memoize(new RacePlacingSupplier(pilot)));
 		}
 		racePlacings = tmp.build();
 	}
 
 	@Override
 	public int compare(Pilot o1, Pilot o2) {
		return Ordering.<Integer>natural().lexicographical().compare(racePlacings.get(o1).get(), racePlacings.get(o2).get());
 	}
 
 	protected class RacePlacingSupplier implements Supplier<List<Integer>> {
 		private final Pilot pilot;
 
 		public RacePlacingSupplier(Pilot pilot) {
 			this.pilot = pilot;
 		}
 
 		@Override
 		public List<Integer> get() {
 			switch (method) {
 			case INCLUDING_DISCARDS:
 				return Ordering.natural().sortedCopy(scores.getRacePoints(pilot).values());
 
 			case EXCLUDING_DISCARDS:
 				return Ordering.natural().sortedCopy(
 						Maps.filterKeys(scores.getRacePoints(pilot), Predicates.not(Predicates.in(scores.getDiscardedRaces(pilot)))).values());
 			}
 			return null;
 		}
 	}
 }
