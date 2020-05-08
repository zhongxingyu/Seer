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
 package eu.lp0.cursus.ui.series;
 
 import java.awt.BorderLayout;
 import java.awt.Frame;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.SwingUtilities;
 
 import eu.lp0.cursus.db.DatabaseSession;
 import eu.lp0.cursus.db.dao.SeriesDAO;
 import eu.lp0.cursus.db.data.Pilot;
 import eu.lp0.cursus.db.data.Series;
 import eu.lp0.cursus.ui.component.AbstractDatabaseTab;
 import eu.lp0.cursus.ui.component.DatabaseTableModel;
 import eu.lp0.cursus.ui.component.DatabaseWindow;
 import eu.lp0.cursus.util.Background;
 
 public class SeriesPilotsTab<O extends Frame & DatabaseWindow> extends AbstractDatabaseTab<O, Series> {
 	private JScrollPane scrollPane;
 	private JTable table;
 	private DatabaseTableModel<Pilot, O> model;
 	@SuppressWarnings("unused")
 	private Series currentSeries = null;
 
 	private static final SeriesDAO seriesDAO = new SeriesDAO();
 
 	public SeriesPilotsTab(O win) {
 		super(Series.class, win, "tab.pilots"); //$NON-NLS-1$
 		initialise();
 	}
 
 	private void initialise() {
 		setLayout(new BorderLayout(0, 0));
 
 		scrollPane = new JScrollPane();
 		add(scrollPane, BorderLayout.CENTER);
 
 		table = new JTable();
 		scrollPane.setViewportView(table);
 
 		model = new PilotTableModel<O>(win);
 		model.setupEditableModel(table);
 	}
 
 	@Override
 	public void tabRefresh(Series series) {
 		assert (Background.isExecutorThread());
 
 		final Series newSeries;
 		final List<Pilot> newPilots;
 
 		win.getDatabase().startSession();
 		try {
 			DatabaseSession.begin();
 
 			newSeries = seriesDAO.get(series);
 			newPilots = new ArrayList<Pilot>(newSeries.getPilots());
 			DatabaseSession.commit();
 
 			seriesDAO.detach(newSeries);
 		} finally {
 			win.getDatabase().endSession();
 		}
 
 		SwingUtilities.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				currentSeries = newSeries;
 				model.updateModel(newPilots);
 			}
 		});
 	}
 
 	@Override
 	public void tabClear() {
 		assert (Background.isExecutorThread());
 
 		SwingUtilities.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				currentSeries = null;
				model.updateModel(Collections.<Pilot>emptyList());
 			}
 		});
 	}
 }
