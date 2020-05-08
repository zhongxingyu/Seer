 /*
 	cursus - Race series management program
 	Copyright 2012  Simon Arlott
 
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
 package eu.lp0.cursus.xml.scores;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.google.common.base.Preconditions;
 import com.google.common.base.Predicates;
 import com.google.common.collect.HashBasedTable;
 import com.google.common.collect.HashMultimap;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.LinkedHashMultimap;
 import com.google.common.collect.Multimap;
 import com.google.common.collect.Table;
 
 import eu.lp0.cursus.db.data.Class;
 import eu.lp0.cursus.db.data.Event;
 import eu.lp0.cursus.db.data.Penalty;
 import eu.lp0.cursus.db.data.Pilot;
 import eu.lp0.cursus.db.data.Race;
 import eu.lp0.cursus.db.data.RaceAttendee;
 import eu.lp0.cursus.db.data.RaceNumber;
 import eu.lp0.cursus.db.data.Series;
 import eu.lp0.cursus.scoring.scores.impl.GenericScores;
 import eu.lp0.cursus.xml.data.entity.DataXMLClass;
 import eu.lp0.cursus.xml.data.entity.DataXMLEvent;
 import eu.lp0.cursus.xml.data.entity.DataXMLPenalty;
 import eu.lp0.cursus.xml.data.entity.DataXMLPilot;
 import eu.lp0.cursus.xml.data.entity.DataXMLRace;
 import eu.lp0.cursus.xml.data.entity.DataXMLRaceAttendee;
 import eu.lp0.cursus.xml.data.entity.DataXMLRaceNumber;
 import eu.lp0.cursus.xml.data.entity.DataXMLSeries;
 import eu.lp0.cursus.xml.data.ref.DataXMLClassMember;
 import eu.lp0.cursus.xml.data.ref.DataXMLClassRef;
 import eu.lp0.cursus.xml.data.ref.DataXMLEventRef;
 import eu.lp0.cursus.xml.data.ref.DataXMLPilotRef;
 import eu.lp0.cursus.xml.data.ref.DataXMLRaceRef;
 import eu.lp0.cursus.xml.data.ref.DataXMLSeriesRef;
 import eu.lp0.cursus.xml.scores.data.ScoresXMLOverallScore;
 import eu.lp0.cursus.xml.scores.data.ScoresXMLRaceScore;
 import eu.lp0.cursus.xml.scores.results.AbstractScoresXMLResults;
 import eu.lp0.cursus.xml.scores.results.ScoresXMLEventRaceResults;
 import eu.lp0.cursus.xml.scores.results.ScoresXMLEventResults;
 import eu.lp0.cursus.xml.scores.results.ScoresXMLRaceResults;
 import eu.lp0.cursus.xml.scores.results.ScoresXMLSeriesEventResults;
 import eu.lp0.cursus.xml.scores.results.ScoresXMLSeriesResults;
 
 public class XMLScores {
 	private ScoresXML scoresXML;
 	private Map<String, Series> series = new HashMap<String, Series>();
 	private Map<String, Class> classes = new HashMap<String, Class>();
 	private Map<String, Pilot> pilots = new HashMap<String, Pilot>();
 	private Map<String, Event> events = new HashMap<String, Event>();
 	private Map<String, Race> races = new HashMap<String, Race>();
 
 	private Multimap<? super AbstractScoresXMLResults, Race> resultsRaces = LinkedHashMultimap.create();
 	private Multimap<? super AbstractScoresXMLResults, Event> resultsEvents = HashMultimap.create();
 	private Multimap<? super AbstractScoresXMLResults, Pilot> resultsPilots = HashMultimap.create();
 
 	private Map<Race, ScoresXMLEventRaceResults> seriesEventRaceResults = new HashMap<Race, ScoresXMLEventRaceResults>();
 	private Table<ScoresXMLEventResults, Race, ScoresXMLEventRaceResults> eventEventRaceResults = HashBasedTable.create();
 
 	private Map<? super AbstractScoresXMLResults, Table<Pilot, Race, ScoresXMLRaceScore>> resultsPilotRaceScores = new HashMap<AbstractScoresXMLResults, Table<Pilot, Race, ScoresXMLRaceScore>>();
 	private Table<? super AbstractScoresXMLResults, Pilot, ScoresXMLOverallScore> resultsPilotOverallScores = HashBasedTable.create();
 
 	public XMLScores(ScoresXML scoresXML) {
 		this.scoresXML = scoresXML;
 		extractEntities();
 		if (scoresXML.getSeriesResults() != null) {
 			extractSeriesResults();
 		}
 		if (scoresXML.getEventResults() != null) {
 			extractEventResults();
 		}
 		if (scoresXML.getRaceResults() != null) {
 			extractRaceResults();
 		}
 	}
 
 	public GenericScores newInstance(ScoresXMLSeriesResults results) {
 		return newInstance(results, new Subset(results));
 	}
 
 	public GenericScores newInstance(ScoresXMLEventResults results) {
 		return newInstance(results, new Subset(results));
 	}
 
 	public GenericScores newInstance(ScoresXMLRaceResults results) {
 		return newInstance(results, new Subset(results));
 	}
 
 	Series dereference(DataXMLSeriesRef seriesRef) {
 		return series.get(seriesRef.getSeries());
 	}
 
 	Class dereference(DataXMLClassRef class_) {
 		return classes.get(class_.getClass_());
 	}
 
 	Pilot dereference(DataXMLPilotRef pilotRef) {
 		return pilots.get(pilotRef.getPilot());
 	}
 
 	Event dereference(DataXMLEventRef event) {
 		return events.get(event.getEvent());
 	}
 
 	Race dereference(DataXMLRaceRef raceRef) {
 		return races.get(raceRef.getRace());
 	}
 
 	private void extractEntities() {
 		DataXMLSeries xmlSeries = scoresXML.getSeries();
 		Series series_ = new Series(wrapNull(xmlSeries.getName()), wrapNull(xmlSeries.getDescription()));
 		series.put(xmlSeries.getId(), series_);
 
 		for (DataXMLClass xmlClass : xmlSeries.getClasses()) {
 			Class class_ = new Class(series_, wrapNull(xmlClass.getName()), wrapNull(xmlClass.getDescription()));
 			classes.put(xmlClass.getId(), class_);
 			series_.getClasses().add(class_);
 		}
 
 		for (DataXMLPilot xmlPilot : xmlSeries.getPilots()) {
 			Pilot pilot_ = new Pilot(series_, wrapNull(xmlPilot.getName()), xmlPilot.getGender(), wrapNull(xmlPilot.getCountry()));
 			pilots.put(xmlPilot.getId(), pilot_);
 			series_.getPilots().add(pilot_);
 
 			DataXMLRaceNumber xmlRaceNumber = xmlPilot.getRaceNumber();
 			RaceNumber raceNumber = new RaceNumber(pilot_, wrapNull(xmlRaceNumber.getOrganisation()), xmlRaceNumber.getNumber());
 			pilot_.setRaceNumber(raceNumber);
 
 			if (xmlPilot.getClasses() != null) {
 				for (DataXMLClassMember xmlClass : xmlPilot.getClasses()) {
 					pilot_.getClasses().add(dereference(xmlClass));
 				}
 			}
 		}
 
 		for (DataXMLEvent xmlEvent : xmlSeries.getEvents()) {
 			Event event = new Event(series_, wrapNull(xmlEvent.getName()), wrapNull(xmlEvent.getDescription()));
 			events.put(xmlEvent.getId(), event);
 			series_.getEvents().add(event);
 
 			for (DataXMLRace xmlRace : xmlEvent.getRaces()) {
 				Race race = new Race(event, wrapNull(xmlRace.getName()), wrapNull(xmlRace.getDescription()));
 				races.put(xmlRace.getId(), race);
 				event.getRaces().add(race);
 
 				if (xmlRace.getAttendees() != null) {
 					for (DataXMLRaceAttendee xmlRaceAttendee : xmlRace.getAttendees()) {
 						RaceAttendee attendee = new RaceAttendee(race, dereference(xmlRaceAttendee), xmlRaceAttendee.getType());
 						race.getAttendees().put(attendee.getPilot(), attendee);
 
 						if (xmlRaceAttendee.getPenalties() != null) {
 							for (DataXMLPenalty xmlPenalty : xmlRaceAttendee.getPenalties()) {
 								Penalty penalty = new Penalty(xmlPenalty.getType(), xmlPenalty.getValue(), wrapNull(xmlPenalty.getReason()));
 								attendee.getPenalties().add(penalty);
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 
 	static String wrapNull(String value) {
 		return value == null ? "" : value; //$NON-NLS-1$
 	}
 
 	private void extractSeriesResults() {
 		ScoresXMLSeriesResults seriesResults = scoresXML.getSeriesResults();
 		for (DataXMLEventRef event : seriesResults.getEvents()) {
 			resultsEvents.put(seriesResults, dereference(event));
 		}
 
 		Table<Pilot, Race, ScoresXMLRaceScore> raceScores = HashBasedTable.create();
 		for (ScoresXMLSeriesEventResults seriesEventResults : seriesResults.getEventResults()) {
 			for (ScoresXMLEventRaceResults eventRaceResults : seriesEventResults.getRaceResults()) {
 				seriesEventRaceResults.put(dereference(eventRaceResults), eventRaceResults);
 
 				Race race = dereference(eventRaceResults);
 				resultsRaces.put(seriesResults, race);
 				for (ScoresXMLRaceScore raceScore : eventRaceResults.getRacePilots()) {
 					raceScores.row(dereference(raceScore)).put(race, raceScore);
 				}
 			}
 		}
 		resultsPilotRaceScores.put(seriesResults, raceScores);
 
 		for (ScoresXMLOverallScore overallScore : seriesResults.getOverallPilots()) {
 			Pilot pilot = dereference(overallScore);
 			resultsPilotOverallScores.row(seriesResults).put(pilot, overallScore);
 			resultsPilots.put(seriesResults, pilot);
 		}
 	}
 
 	private void extractEventResults() {
 		for (ScoresXMLEventResults eventResult : scoresXML.getEventResults()) {
 			for (DataXMLEventRef event : eventResult.getEvents()) {
 				resultsEvents.put(eventResult, dereference(event));
 			}
 
 			Table<Pilot, Race, ScoresXMLRaceScore> raceScores = HashBasedTable.create();
 			for (ScoresXMLEventRaceResults eventRaceResults : eventResult.getRaceResults()) {
 				eventEventRaceResults.row(eventResult).put(dereference(eventRaceResults), eventRaceResults);
 
 				Race race = dereference(eventRaceResults);
 				resultsRaces.put(eventResult, race);
 				for (ScoresXMLRaceScore raceScore : eventRaceResults.getRacePilots()) {
 					raceScores.row(dereference(raceScore)).put(race, raceScore);
 				}
 			}
 			resultsPilotRaceScores.put(eventResult, raceScores);
 
 			for (ScoresXMLOverallScore overallScore : eventResult.getOverallPilots()) {
 				Pilot pilot = dereference(overallScore);
 				resultsPilotOverallScores.row(eventResult).put(pilot, overallScore);
 				resultsPilots.put(eventResult, pilot);
 			}
 		}
 	}
 
 	private void extractRaceResults() {
 		for (ScoresXMLRaceResults raceResult : scoresXML.getRaceResults()) {
 			Race race = dereference(raceResult);
 			resultsRaces.put(raceResult, race);
 
 			Table<Pilot, Race, ScoresXMLRaceScore> raceScores = HashBasedTable.create();
 			for (ScoresXMLRaceScore raceScore : raceResult.getRacePilots()) {
 				raceScores.row(dereference(raceScore)).put(race, raceScore);
 			}
 			resultsPilotRaceScores.put(raceResult, raceScores);
 
 			for (ScoresXMLOverallScore overallScore : raceResult.getOverallPilots()) {
 				Pilot pilot = dereference(overallScore);
 				resultsPilotOverallScores.row(raceResult).put(pilot, overallScore);
 				resultsPilots.put(raceResult, pilot);
 			}
 		}
 	}
 
 	private GenericScores newInstance(AbstractScoresXMLResults results, Subset subset) {
 		return new GenericScores(ImmutableSet.copyOf(resultsPilots.get(results)), ImmutableList.copyOf(resultsRaces.get(results)),
 				ImmutableSet.copyOf(resultsEvents.get(results)), Predicates.<Pilot>alwaysTrue(), new XMLScoresFactory(this, subset), new XMLScorer());
 	}
 
 	class Subset {
 		private final ScoresXMLSeriesResults seriesResults;
 		private final ScoresXMLEventResults eventResults;
 		private final ScoresXMLRaceResults raceResults;
 
 		private final Map<Race, ScoresXMLEventRaceResults> eventRaceResults;
 		private final Table<Pilot, Race, ScoresXMLRaceScore> pilotRaceScores;
 		private final Map<Pilot, ScoresXMLOverallScore> pilotOverallScores;
 
 		public Subset(ScoresXMLSeriesResults seriesResults) {
 			Preconditions.checkNotNull(seriesResults);
 			Preconditions.checkArgument(scoresXML.getSeriesResults().equals(seriesResults));
 			this.seriesResults = seriesResults;
 			this.eventResults = null;
 			this.raceResults = null;
 			this.eventRaceResults = seriesEventRaceResults;
 			this.pilotRaceScores = resultsPilotRaceScores.get(seriesResults);
 			this.pilotOverallScores = resultsPilotOverallScores.row(seriesResults);
 		}
 
 		public Subset(ScoresXMLEventResults eventResults) {
 			Preconditions.checkNotNull(eventResults);
 			Preconditions.checkArgument(scoresXML.getEventResults().contains(eventResults));
 			this.seriesResults = null;
 			this.eventResults = eventResults;
 			this.raceResults = null;
 			this.eventRaceResults = eventEventRaceResults.row(eventResults);
 			this.pilotRaceScores = resultsPilotRaceScores.get(eventResults);
 			this.pilotOverallScores = resultsPilotOverallScores.row(eventResults);
 		}
 
 		public Subset(ScoresXMLRaceResults raceResults) {
 			Preconditions.checkNotNull(raceResults);
 			Preconditions.checkArgument(scoresXML.getRaceResults().contains(raceResults));
 			this.seriesResults = null;
 			this.eventResults = null;
 			this.raceResults = raceResults;
 			this.eventRaceResults = null;
 			this.pilotRaceScores = resultsPilotRaceScores.get(raceResults);
 			this.pilotOverallScores = resultsPilotOverallScores.row(raceResults);
 		}
 
 		public int getDiscards() {
 			if (seriesResults != null) {
 				return seriesResults.getDiscards();
 			} else if (eventResults != null) {
 				return eventResults.getDiscards();
 			} else {
 				return 0;
 			}
 		}
 
 		public int getFleetSize(Race race) {
 			if (eventRaceResults != null) {
 				return eventRaceResults.get(race).getFleet();
 			} else {
 				return raceResults.getFleet();
 			}
 		}
 
 		public List<ScoresXMLRaceScore> getRaceScores(Race race) {
 			if (eventRaceResults != null) {
 				return eventRaceResults.get(race).getRacePilots();
 			} else {
 				return raceResults.getRacePilots();
 			}
 		}
 
 		public ScoresXMLRaceScore getRaceScore(Pilot pilot, Race race) {
 			return pilotRaceScores.row(pilot).get(race);
 		}
 
 		public List<ScoresXMLOverallScore> getOverallScores() {
 			if (seriesResults != null) {
 				return seriesResults.getOverallPilots();
 			} else if (eventResults != null) {
 				return eventResults.getOverallPilots();
 			} else if (raceResults != null) {
 				return raceResults.getOverallPilots();
 			}
 			Preconditions.checkState(false);
 			return null;
 		}
 
 		public ScoresXMLOverallScore getOverallScore(Pilot pilot) {
 			return pilotOverallScores.get(pilot);
 		}
 	}
 }
