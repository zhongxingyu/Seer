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
 package eu.lp0.cursus.test;
 
 import java.util.List;
 import java.util.Map;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import eu.lp0.cursus.db.DatabaseSession;
 import eu.lp0.cursus.db.data.Event;
 import eu.lp0.cursus.db.data.Pilot;
 import eu.lp0.cursus.db.data.PilotAtEvent;
 
 public class PilotAtEventTests extends AbstractDatabaseTest {
 	@Test
 	public void eventToPilotMapTypes() {
 		// Save data
 		db.startSession();
 		try {
 			DatabaseSession.begin();
 
 			Event event = eventDAO.findAll().get(0);
 
 			Pilot pilot1 = new Pilot(event.getSeries(), "Test 1"); //$NON-NLS-1$
 			pilotDAO.persist(pilot1);
 			event.getPilots().put(pilot1, new PilotAtEvent(event, pilot1));
 
 			Pilot pilot2 = new Pilot(event.getSeries(), "Test 2"); //$NON-NLS-1$
 			pilotDAO.persist(pilot2);
 			event.getPilots().put(pilot2, new PilotAtEvent(event, pilot2));
 
 			Pilot pilot3 = new Pilot(event.getSeries(), "Test 3"); //$NON-NLS-1$
 			pilotDAO.persist(pilot3);
 			event.getPilots().put(pilot3, new PilotAtEvent(event, pilot3));
 
 			eventDAO.persist(event);
 
 			DatabaseSession.commit();
 		} finally {
 			db.endSession();
 		}
 
 		// Check data
 		db.startSession();
 		try {
 			DatabaseSession.begin();
 
 			Event event = eventDAO.findAll().get(0);
 
 			Assert.assertEquals(3, event.getPilots().size());
 			for (Map.Entry<?, ?> entry : event.getPilots().entrySet()) {
 				Assert.assertEquals(Pilot.class, entry.getKey().getClass());
 				Assert.assertEquals(PilotAtEvent.class, entry.getValue().getClass());
 			}
 
 			DatabaseSession.commit();
 		} finally {
 			db.endSession();
 		}
 	}
 
 	@Test
 	public void pilotToEventMapTypes() {
 		// Save data
 		db.startSession();
 		try {
 			DatabaseSession.begin();
 
 			Event event = eventDAO.findAll().get(0);
 
 			Pilot pilot1 = new Pilot(event.getSeries(), "Test 1"); //$NON-NLS-1$
 			pilotDAO.persist(pilot1);
 			event.getPilots().put(pilot1, new PilotAtEvent(event, pilot1));
 
 			Pilot pilot2 = new Pilot(event.getSeries(), "Test 2"); //$NON-NLS-1$
 			pilotDAO.persist(pilot2);
 			event.getPilots().put(pilot2, new PilotAtEvent(event, pilot2));
 
 			Pilot pilot3 = new Pilot(event.getSeries(), "Test 3"); //$NON-NLS-1$
 			pilotDAO.persist(pilot3);
 			event.getPilots().put(pilot3, new PilotAtEvent(event, pilot3));
 
 			eventDAO.persist(event);
 
 			DatabaseSession.commit();
 		} finally {
 			db.endSession();
 		}
 
 		// Check data
 		db.startSession();
 		try {
 			DatabaseSession.begin();
 
 			List<Pilot> pilots = pilotDAO.findAll();
 
 			Assert.assertEquals(3, pilots.size());
 			for (Pilot pilot : pilots) {
 				Assert.assertEquals(1, pilot.getEvents().size());
 				for (Map.Entry<?, ?> entry : pilot.getEvents().entrySet()) {
 					Assert.assertEquals(Event.class, entry.getKey().getClass());
 					Assert.assertEquals(PilotAtEvent.class, entry.getValue().getClass());
 				}
 			}
 
 			DatabaseSession.commit();
 		} finally {
 			db.endSession();
 		}
 	}
}
