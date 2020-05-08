 package com.yogocodes.httpmonitor.gui.listeners;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
import com.yogocodes.httpmonitor.core.MonitorResult;
 import com.yogocodes.httpmonitor.core.MonitorResultSummarizer;
 import com.yogocodes.httpmonitor.core.MonitorResultSummarizerFactory;
 import com.yogocodes.httpmonitor.gui.form.HttpMonitorAppForm;
 import com.yogocodes.httpmonitor.gui.form.HttpMonitorAppFormFactory;
 import com.yogocodes.httpmonitor.gui.form.MonitorResultTableModel;
 
 public class ResultTableRefreshActionListenerImpl implements ActionListener {
 
 	private final static Logger LOG = LoggerFactory
 			.getLogger(ResultTableRefreshActionListenerImpl.class);
 
 	@Override
 	public void actionPerformed(final ActionEvent e) {
 		LOG.debug("refreshing result table");
 
 		final HttpMonitorAppForm appFormInstance = HttpMonitorAppFormFactory
 				.getAppFormInstance();
 
 		final MonitorResultTableModel tableModel = appFormInstance
 				.getMonitorResultTableModel();
 
		final MonitorResult result = new MonitorResult();
		result.setUrl("http://localhost/robots.txt");
		result.setTime(12345l);
 		tableModel.clearData();
 		final MonitorResultSummarizer summarizer = MonitorResultSummarizerFactory
 				.getInstance();
 		tableModel.getResults().addAll(summarizer.getSummaries());
 
 		tableModel.fireTableDataChanged();
 		LOG.debug("refreshed result table");
 	}
 
 }
