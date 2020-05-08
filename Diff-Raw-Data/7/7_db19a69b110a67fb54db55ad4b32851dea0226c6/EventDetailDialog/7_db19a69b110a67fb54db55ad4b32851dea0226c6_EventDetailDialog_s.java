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
 package eu.lp0.cursus.ui.event;
 
 import eu.lp0.cursus.db.dao.EventDAO;
 import eu.lp0.cursus.db.dao.SeriesDAO;
 import eu.lp0.cursus.db.data.Event;
 import eu.lp0.cursus.ui.common.CommonDetailDialog;
 import eu.lp0.cursus.ui.component.DatabaseWindow;
 
 public class EventDetailDialog extends CommonDetailDialog<Event> {
 	private static final SeriesDAO seriesDAO = new SeriesDAO();
 	private static final EventDAO eventDAO = new EventDAO();
 
 	public EventDetailDialog(DatabaseWindow win, String title, Event event, boolean isUpdate) {
 		super(win, title, eventDAO, event, isUpdate);
 	}
 
 	@Override
 	protected void prePersist(Event event) {
 		event.setSeries(seriesDAO.get(event.getSeries()));
 	}
 
 	@Override
 	protected void postSave() {
 		win.refreshRaceList();
 	}
 }
