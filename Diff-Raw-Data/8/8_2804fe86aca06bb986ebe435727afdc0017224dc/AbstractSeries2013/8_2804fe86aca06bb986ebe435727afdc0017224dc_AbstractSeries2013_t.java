 /*
 	cursus - Race series management program
 	Copyright 2013  Simon Arlott
 
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
 package org.spka.cursus.test.series_2013;
 
 import org.spka.cursus.scoring.SPKAConstants;
 import org.spka.cursus.test.AbstractSeries;
 
 import eu.lp0.cursus.db.DatabaseSession;
 import eu.lp0.cursus.db.data.Class;
 import eu.lp0.cursus.db.data.Event;
 import eu.lp0.cursus.db.data.Gender;
 import eu.lp0.cursus.db.data.Pilot;
 import eu.lp0.cursus.db.data.Race;
 import eu.lp0.cursus.db.data.RaceAttendee;
 import eu.lp0.cursus.db.data.RaceNumber;
 import eu.lp0.cursus.db.data.RaceTally;
 import eu.lp0.cursus.db.data.Series;
 import eu.lp0.cursus.scoring.scorer.Scorer;
 import eu.lp0.cursus.scoring.scorer.ScorerFactory;
 
 public abstract class AbstractSeries2013 extends AbstractSeries {
 	protected static final String SERIES_NAME = "SPKA Race Series 2013/14"; //$NON-NLS-1$
 	protected static final int SERIES_FLEET = 11;
 	protected static final int SERIES_FLEET_AT_EVENT1 = 11;
 
 	protected static final String EVENT1_NAME = "Event 1"; //$NON-NLS-1$
 	protected static final String EVENT1_DESC = "Luce Bay (26/10/2013)"; //$NON-NLS-1$
 	protected static final int EVENT1_FLEET = 11;
 	protected static final String RACE1_NAME = "Race 1"; //$NON-NLS-1$
	protected static final String RACE1_DESC = "Luce Bay (26/10/2013)"; //$NON-NLS-1$
 	protected static final int RACE1_PILOTS = 11;
 	protected static final String RACE2_NAME = "Race 2"; //$NON-NLS-1$
	protected static final String RACE2_DESC = "Luce Bay (26/10/2013)"; //$NON-NLS-1$
 	protected static final int RACE2_PILOTS = 11;
 	protected static final String RACE3_NAME = "Race 3"; //$NON-NLS-1$
	protected static final String RACE3_DESC = "Luce Bay (26/10/2013)"; //$NON-NLS-1$
 	protected static final int RACE3_PILOTS = 11;
 
 	protected Scorer scorer = ScorerFactory.newScorer(SPKAConstants.UUID_2012);
 
 	protected Class junior;
 	protected Class _16inWheel;
 	protected Pilot sco018;
 	protected Pilot sco066;
 	protected Pilot sco069;
 	protected Pilot sco087;
 	protected Pilot sco116;
 	protected Pilot sco156;
 	protected Pilot sco159;
 	protected Pilot sco179;
 	protected Pilot sco296;
 	protected Pilot sco561;
 	protected Pilot sco808;
 
 	private Series _series;
 	private Event _event1;
 	private Race _race1;
 	private Race _race2;
 	private Race _race3;
 
 	protected void createSeriesData() throws Exception {
 		if (_series != null) {
 			return;
 		}
 
 		db.startSession();
 		try {
 			DatabaseSession.begin();
 
 			// Create the 2013/14 series
 			Series series = new Series(SERIES_NAME);
 
 			// Create classes
 			junior = new Class(series, "Junior"); //$NON-NLS-1$
 			series.getClasses().add(junior);
 
 			_16inWheel = new Class(series, "16\" Wheel"); //$NON-NLS-1$
 			series.getClasses().add(_16inWheel);
 
 			// Add all the pilots
 			sco018 = new Pilot(series, "SCO018", Gender.MALE, "Scotland"); //$NON-NLS-1$ //$NON-NLS-2$
 			series.getPilots().add(sco018);
 
 			sco066 = new Pilot(series, "SCO066", Gender.MALE, "Scotland"); //$NON-NLS-1$ //$NON-NLS-2$
 			sco066.getClasses().add(_16inWheel);
 			series.getPilots().add(sco066);
 
 			sco069 = new Pilot(series, "SCO069", Gender.MALE, "Scotland"); //$NON-NLS-1$ //$NON-NLS-2$
 			series.getPilots().add(sco069);
 
 			sco087 = new Pilot(series, "SCO087", Gender.MALE, "Scotland"); //$NON-NLS-1$ //$NON-NLS-2$
 			series.getPilots().add(sco087);
 
 			sco116 = new Pilot(series, "SCO116", Gender.MALE, "Scotland"); //$NON-NLS-1$ //$NON-NLS-2$
 			series.getPilots().add(sco116);
 
 			sco156 = new Pilot(series, "SCO156", Gender.MALE, "Scotland"); //$NON-NLS-1$ //$NON-NLS-2$
 			sco156.getClasses().add(junior);
 			series.getPilots().add(sco156);
 
 			sco159 = new Pilot(series, "SCO159", Gender.MALE, "Scotland"); //$NON-NLS-1$ //$NON-NLS-2$
 			series.getPilots().add(sco159);
 
 			sco179 = new Pilot(series, "SCO179", Gender.MALE, "Scotland"); //$NON-NLS-1$ //$NON-NLS-2$
 			series.getPilots().add(sco179);
 
 			sco296 = new Pilot(series, "SCO296", Gender.FEMALE, "Scotland"); //$NON-NLS-1$ //$NON-NLS-2$
 			sco296.getClasses().add(_16inWheel);
 			series.getPilots().add(sco296);
 
 			sco561 = new Pilot(series, "SCO561", Gender.MALE, "Scotland"); //$NON-NLS-1$ //$NON-NLS-2$
 			series.getPilots().add(sco561);
 
 			sco808 = new Pilot(series, "SCO808", Gender.MALE, "Scotland"); //$NON-NLS-1$ //$NON-NLS-2$
 			series.getPilots().add(sco808);
 
 			// Create race numbers
 			for (Pilot pilot : series.getPilots()) {
 				pilot.setRaceNumber(RaceNumber.valueOfFor(pilot.getName(), pilot));
 			}
 
 			// Save
 			seriesDAO.persist(series);
 
 			DatabaseSession.commit();
 
 			_series = series;
 		} finally {
 			db.endSession();
 		}
 	}
 
 	protected void createEvent1Data() throws Exception {
 		createSeriesData();
 
 		if (_event1 != null) {
 			return;
 		}
 
 		db.startSession();
 		try {
 			DatabaseSession.begin();
 
 			Series series = seriesDAO.find(SERIES_NAME);
 
 			Event event1 = new Event(series, EVENT1_NAME, EVENT1_DESC);
 			series.getEvents().add(event1);
 			eventDAO.persist(event1);
 
 			DatabaseSession.commit();
 
 			_event1 = event1;
 		} finally {
 			db.endSession();
 		}
 	}
 
 	protected void createEvent1Races() throws Exception {
 		createRace1Data();
 		createRace2Data();
 		createRace3Data();
 	}
 
 	protected void createRace1Data() throws Exception {
 		createEvent1Data();
 
 		if (_race1 != null) {
 			return;
 		}
 
 		db.startSession();
 		try {
 			DatabaseSession.begin();
 
 			Series series = seriesDAO.find(SERIES_NAME);
 			Event event1 = eventDAO.find(series, EVENT1_NAME);
 
 			Race race1 = new Race(event1, RACE1_NAME, RACE1_DESC);
 			event1.getRaces().add(race1);
 			race1.getAttendees().put(sco018, new RaceAttendee(race1, sco018, RaceAttendee.Type.PILOT));
 			race1.getAttendees().put(sco066, new RaceAttendee(race1, sco066, RaceAttendee.Type.PILOT));
 			race1.getAttendees().put(sco069, new RaceAttendee(race1, sco069, RaceAttendee.Type.PILOT));
 			race1.getAttendees().put(sco087, new RaceAttendee(race1, sco087, RaceAttendee.Type.PILOT));
 			race1.getAttendees().put(sco116, new RaceAttendee(race1, sco116, RaceAttendee.Type.PILOT));
 			race1.getAttendees().put(sco156, new RaceAttendee(race1, sco156, RaceAttendee.Type.PILOT));
 			race1.getAttendees().put(sco159, new RaceAttendee(race1, sco159, RaceAttendee.Type.PILOT));
 			race1.getAttendees().put(sco179, new RaceAttendee(race1, sco179, RaceAttendee.Type.PILOT));
 			race1.getAttendees().put(sco296, new RaceAttendee(race1, sco296, RaceAttendee.Type.PILOT));
 			race1.getAttendees().put(sco561, new RaceAttendee(race1, sco561, RaceAttendee.Type.PILOT));
 			race1.getAttendees().put(sco808, new RaceAttendee(race1, sco808, RaceAttendee.Type.PILOT));
 			race1.getTallies().add(new RaceTally(RaceTally.Type.START));
 			addLaps(race1, "69,179,808,116,18,159,87,156,66,296,561"); //$NON-NLS-1$
 			addLaps(race1, "69,179,116,808,18,159,87,156,66,296,561"); //$NON-NLS-1$
 			addLaps(race1, "69,179,116,808,159,18,156,87,66"); //$NON-NLS-1$
 			addLaps(race1, "69,116,179,808,159,18,87,156"); //$NON-NLS-1$
 			addLaps(race1, "69,116,179,159,18,808"); //$NON-NLS-1$
 			addLaps(race1, "69,116,179,159,18"); //$NON-NLS-1$
 			addLaps(race1, "69,116,179"); //$NON-NLS-1$
 			addLaps(race1, "69,116,179"); //$NON-NLS-1$
 			raceDAO.persist(race1);
 
 			DatabaseSession.commit();
 
 			_race1 = race1;
 		} finally {
 			db.endSession();
 		}
 	}
 
 	protected void createRace2Data() throws Exception {
 		createEvent1Data();
 
 		if (_race2 != null) {
 			return;
 		}
 
 		db.startSession();
 		try {
 			DatabaseSession.begin();
 
 			Series series = seriesDAO.find(SERIES_NAME);
 			Event event1 = eventDAO.find(series, EVENT1_NAME);
 
 			Race race2 = new Race(event1, RACE2_NAME, RACE2_DESC);
 			event1.getRaces().add(race2);
 			race2.getAttendees().put(sco018, new RaceAttendee(race2, sco018, RaceAttendee.Type.PILOT));
 			race2.getAttendees().put(sco066, new RaceAttendee(race2, sco066, RaceAttendee.Type.PILOT));
 			race2.getAttendees().put(sco069, new RaceAttendee(race2, sco069, RaceAttendee.Type.PILOT));
 			race2.getAttendees().put(sco087, new RaceAttendee(race2, sco087, RaceAttendee.Type.PILOT));
 			race2.getAttendees().put(sco116, new RaceAttendee(race2, sco116, RaceAttendee.Type.PILOT));
 			race2.getAttendees().put(sco156, new RaceAttendee(race2, sco156, RaceAttendee.Type.PILOT));
 			race2.getAttendees().put(sco159, new RaceAttendee(race2, sco159, RaceAttendee.Type.PILOT));
 			race2.getAttendees().put(sco179, new RaceAttendee(race2, sco179, RaceAttendee.Type.PILOT));
 			race2.getAttendees().put(sco296, new RaceAttendee(race2, sco296, RaceAttendee.Type.PILOT));
 			race2.getAttendees().put(sco561, new RaceAttendee(race2, sco561, RaceAttendee.Type.PILOT));
 			race2.getAttendees().put(sco808, new RaceAttendee(race2, sco808, RaceAttendee.Type.PILOT));
 			race2.getTallies().add(new RaceTally(RaceTally.Type.START));
 			addLaps(race2, "69,179,808,159,87,18,156,116,66,296"); //$NON-NLS-1$
 			addLaps(race2, "69,179,808,159,87,18,116,156,66"); //$NON-NLS-1$
 			addLaps(race2, "69,179,808,159,116,87,18,156,66"); //$NON-NLS-1$
 			addLaps(race2, "69,159,116,87,18,808,156"); //$NON-NLS-1$
 			addLaps(race2, "69,116,159,87"); //$NON-NLS-1$
 			addLaps(race2, "69"); //$NON-NLS-1$
 			raceDAO.persist(race2);
 
 			DatabaseSession.commit();
 
 			_race2 = race2;
 		} finally {
 			db.endSession();
 		}
 	}
 
 	protected void createRace3Data() throws Exception {
 		createEvent1Data();
 
 		if (_race3 != null) {
 			return;
 		}
 
 		db.startSession();
 		try {
 			DatabaseSession.begin();
 
 			Series series = seriesDAO.find(SERIES_NAME);
 			Event event1 = eventDAO.find(series, EVENT1_NAME);
 
 			Race race3 = new Race(event1, RACE3_NAME, RACE3_DESC);
 			event1.getRaces().add(race3);
 			race3.getAttendees().put(sco018, new RaceAttendee(race3, sco018, RaceAttendee.Type.V_SCORER));
 			race3.getAttendees().put(sco066, new RaceAttendee(race3, sco066, RaceAttendee.Type.PILOT));
 			race3.getAttendees().put(sco069, new RaceAttendee(race3, sco069, RaceAttendee.Type.PILOT));
 			race3.getAttendees().put(sco087, new RaceAttendee(race3, sco087, RaceAttendee.Type.PILOT));
 			race3.getAttendees().put(sco116, new RaceAttendee(race3, sco116, RaceAttendee.Type.PILOT));
 			race3.getAttendees().put(sco156, new RaceAttendee(race3, sco156, RaceAttendee.Type.PILOT));
 			race3.getAttendees().put(sco159, new RaceAttendee(race3, sco159, RaceAttendee.Type.PILOT));
 			race3.getAttendees().put(sco179, new RaceAttendee(race3, sco179, RaceAttendee.Type.PILOT));
 			race3.getAttendees().put(sco296, new RaceAttendee(race3, sco296, RaceAttendee.Type.PILOT));
 			race3.getAttendees().put(sco561, new RaceAttendee(race3, sco561, RaceAttendee.Type.PILOT));
 			race3.getAttendees().put(sco808, new RaceAttendee(race3, sco808, RaceAttendee.Type.PILOT));
 			race3.getTallies().add(new RaceTally(RaceTally.Type.START));
 			addLaps(race3, "69,116,808,159,179,87,156,66,561"); //$NON-NLS-1$
 			addLaps(race3, "69,808,116,159,179,87,156,66"); //$NON-NLS-1$
 			addLaps(race3, "69,808,116,179,159,87,156,66"); //$NON-NLS-1$
 			addLaps(race3, "69,808,116,179,159,87,156"); //$NON-NLS-1$
 			addLaps(race3, "808,69,116,179,159,87"); //$NON-NLS-1$
 			addLaps(race3, "69,808,116,179,159"); //$NON-NLS-1$
 			addLaps(race3, "69,808,116,179"); //$NON-NLS-1$
 			addLaps(race3, "69,808,116"); //$NON-NLS-1$
 			raceDAO.persist(race3);
 
 			DatabaseSession.commit();
 
 			_race3 = race3;
 		} finally {
 			db.endSession();
 		}
 	}
 }
