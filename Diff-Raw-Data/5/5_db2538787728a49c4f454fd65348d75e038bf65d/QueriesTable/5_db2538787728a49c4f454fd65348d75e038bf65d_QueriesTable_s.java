 /*
  * Copyright (c) 2010
  *
  * This file is part of HibernateJConsole.
  *
  *     HibernateJConsole is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  *
  *     HibernateJConsole is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  *
  *     You should have received a copy of the GNU General Public License
  *     along with HibernateJConsole.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package net.sf.hibernate.jconsole.ui;
 
 import net.sf.hibernate.jconsole.AbstractStatisticsContext;
 import net.sf.hibernate.jconsole.formatters.QueryHighlighter;
 import net.sf.hibernate.jconsole.formatters.ToolTipQueryHighlighter;
 import net.sf.hibernate.jconsole.stats.QueryStatistics;
 import net.sf.hibernate.jconsole.ui.widgets.*;
 
 import java.util.Map;
 import java.util.Vector;
 
 import static net.sf.hibernate.jconsole.stats.StatisticsUtil.toQueryPerformance;
 import static net.sf.hibernate.jconsole.stats.StatisticsUtil.toTotalAverageTime;
 
 /**
  * Implements a JTable containing all hibernate queries.
  *
  * @author Juergen_Kellerer, 2009-11-19
  * @version 1.0
  */
 public class QueriesTable extends AbstractRefreshableJTable<QueryStatistics> {
 
 	static final Column[] COLUMNS = {
 			new Column("Query", "The HQL query.", Comparable.class),
 			new Column("Cached", "<html>The percentage of result sets that were retrieved <i>from the cache</i><br/>" +
 					"rather than fetching them from the DB.</html>", Comparable.class),
 			new Column("Performance", "<html>The relative performance in comparison to other queries.<br/>" +
					"<i>(The single query performance is estimated, using avg-time * total-invocations)</i></html>", Comparable.class),
 			new Column("Time in DB", "<html>The total amount of time spent inside the DB.<br/>" +
					"<i>(The time is estimated, using avg-time * direct-invocations)</i></html>", Comparable.class),
 			new Column("Invocations", "The total amount of invocations (cached & direct).", Comparable.class),
 			new Column("Rows Fetched", "The number of rows directly fetched from the DB.", Long.class),
 	};
 
 	long maxExecutionCount;
 	double maxQueryPerformance;
 	double maxTotalAverageTime;
 
 	final QueryHighlighter highlighter = new QueryHighlighter();
 	final ToolTipQueryHighlighter toolTipHighlighter = new ToolTipQueryHighlighter();
 	final NotAvailableBarTableCell cacheNotAvailable = new NotAvailableBarTableCell("<html>This query is uncached<br/>" +
 			"(no cache puts, nor hits).</html>");
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected Vector toTableRow(String key, QueryStatistics s) {
 		Vector<Object> v = new Vector<Object>(COLUMNS.length);
 
 		v.add(new TableCellJLabel(key, toolTipHighlighter.highlight(key), highlighter));
 
 		if (s.getCachePutCount() == 0 && s.getCacheHitCount() == 0)
 			v.add(cacheNotAvailable);
 		else
 			v.add(new HitrateTableCell(s.getCacheHitCount(), s.getCacheMissCount(), s.getCachePutCount()));
 
 
 		double databaseTime = toTotalAverageTime(s);
 
 		v.add(new QueryPerformanceTableCell(maxQueryPerformance, toQueryPerformance(s)));
 
 		v.add(new TimingTableCell(maxTotalAverageTime, databaseTime,
 				s.getExecutionAvgTime(), s.getExecutionMaxTime(), s.getExecutionMinTime()));
 
 		v.add(new ExecutionCountTableCell(maxExecutionCount, s.getExecutionCount() + s.getCacheHitCount(), s.getExecutionCount()));
 		v.add(s.getExecutionRowCount());
 
 		return v;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected Map<String, QueryStatistics> toTableData(AbstractStatisticsContext context) {
 		maxTotalAverageTime = maxQueryPerformance = maxExecutionCount = 0;
 
 		for (QueryStatistics s : context.getQueryStatistics().values()) {
 			maxExecutionCount = Math.max(maxExecutionCount, s.getExecutionCount() + s.getCacheHitCount());
 			maxTotalAverageTime = Math.max(maxTotalAverageTime, toTotalAverageTime(s));
 			maxQueryPerformance = Math.max(maxQueryPerformance, toQueryPerformance(s));
 		}
 
 		return context.getQueryStatistics();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected Column[] getColumns() {
 		return COLUMNS;
 	}
 }
