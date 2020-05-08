 /*
  * Copyright (c) 2000-2004 Netspective Communications LLC. All rights reserved.
  *
  * Netspective Communications LLC ("Netspective") permits redistribution, modification and use of this file in source
  * and binary form ("The Software") under the Netspective Source License ("NSL" or "The License"). The following
  * conditions are provided as a summary of the NSL but the NSL remains the canonical license and must be accepted
  * before using The Software. Any use of The Software indicates agreement with the NSL.
  *
  * 1. Each copy or derived work of The Software must preserve the copyright notice and this notice unmodified.
  *
  * 2. Redistribution of The Software is allowed in object code form only (as Java .class files or a .jar file
  *    containing the .class files) and only as part of an application that uses The Software as part of its primary
  *    functionality. No distribution of the package is allowed as part of a software development kit, other library,
  *    or development tool without written consent of Netspective. Any modified form of The Software is bound by these
  *    same restrictions.
  *
  * 3. Redistributions of The Software in any form must include an unmodified copy of The License, normally in a plain
  *    ASCII text file unless otherwise agreed to, in writing, by Netspective.
  *
  * 4. The names "Netspective", "Axiom", "Commons", "Junxion", and "Sparx" are trademarks of Netspective and may not be
  *    used to endorse or appear in products derived from The Software without written consent of Netspective.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
  * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
  * ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
  * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
  * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
  * IF IT HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
  */
 package com.netspective.commons.report.tabular;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.netspective.commons.value.ValueSource;
 import com.netspective.commons.value.source.StaticValueSource;
 
 public abstract class AbstractTabularReportDataSource implements TabularReportDataSource
 {
     private static final Log log = LogFactory.getLog(AbstractTabularReportDataSource.class);
 
     //TODO: this does not handle non-scrollable data sources yet -- need to implement by using brute-force method
     //      that will just go to the beginning of data source and use next() to scroll down to given row like Sparx 2.x
     public class ScrollState implements TabularReportDataSourceScrollState
     {
         private String identifier;
         private TabularReport report;
         private int activePage;
         private int rowsPerPage;
         private int totalPages;
         private boolean haveMoreRows;    // used only for non-scrollable data sources
         private int rowsProcessed;       // used only for non-scrollable data sources
         private boolean reachedEndOnce;  // used only for non-scrollable data sources
 
         public ScrollState(String identifier)
         {
             setIdentifier(identifier);
             activePage = 1;
             rowsProcessed = 0;
             haveMoreRows = true;
             reachedEndOnce = false;
         }
 
         /**
          * Gets the report object associated with the scroll state
          */
         public TabularReport getReport()
         {
             return report;
         }
 
         /**
          * Sets the report object associated with the scroll state
          */
         public void setReport(TabularReport report)
         {
             this.report = report;
         }
 
         /**
          * Keeps track of the number of rows that has been viewed before reaching the end of the resultset
          */
         public void accumulateRowsProcessed(int rowsProcessed)
         {
             if(!reachedEndOnce)
                 this.rowsProcessed += rowsProcessed;
         }
 
         /**
          * Sets the flags to indicate that the end of the result set has been reached and there are no more rows
          * available
          */
         public void setNoMoreRows()
         {
             haveMoreRows = false;
             reachedEndOnce = true;
         }
 
         /**
          * Get the currently viewed active page number
          */
         public int getActivePage()
         {
             return activePage;
         }
 
         /**
          * Gets the datasource object associated with the scroll state
          *
          * @return TabularReportDataSource
          */
         public TabularReportDataSource getDataSource()
         {
             return AbstractTabularReportDataSource.this;
         }
 
         public String getIdentifier()
         {
             return identifier;
         }
 
         public void setIdentifier(String identifier)
         {
             this.identifier = identifier;
         }
 
         /**
          * Gets the number of rows per page for display
          */
         public int getRowsPerPage()
         {
             return rowsPerPage;
         }
 
         /**
          * Gets the number of rows already processed
          */
         public int getRowsProcessed()
         {
             return rowsProcessed;
         }
 
         /**
          * Gets the total number of rows
          */
         public int getTotalPages()
         {
             return totalPages;
         }
 
         /**
          * Sets the active page within the scroll state and calculates the current row number within the
          * result set
          */
         public void setActivePage(int page)
         {
             recordActivity();
 
             this.activePage = page;
             int activePageRowStart = ((activePage - 1) * rowsPerPage) + 1;
             AbstractTabularReportDataSource.this.setActiveRow(activePageRowStart);
         }
 
         /**
          * Sets the rows per page for the scroll state and calculates the total number of pages for the scroll state
          * based on the total number of rows in the result set.
          */
         public void setRowsPerPage(int rowsPerPage)
         {
             this.rowsPerPage = rowsPerPage;
             // get the total number of rows in the result set
             int totalRows = AbstractTabularReportDataSource.this.getTotalRows();
             // calculate the total number of pages
             this.totalPages = (totalRows % rowsPerPage == 0)
                               ? (totalRows / rowsPerPage) : ((totalRows / rowsPerPage) + 1);
 
         }
 
         /**
          * Recalculates the active page number using the delta value
          */
         public void setPageDelta(int delta)
         {
             setActivePage(getActivePage() + delta);
         }
 
         /**
          * Sets the active page to the first page
          */
         public void setPageFirst()
         {
             setActivePage(1);
         }
 
         /**
          * Sets the active page to the last page number
          */
         public void setPageLast()
         {
             setActivePage(getTotalPages());
         }
 
         public void close()
         {
             AbstractTabularReportDataSource.this.close();
         }
 
         public boolean isClosed()
         {
             return AbstractTabularReportDataSource.this.isClosed();
         }
     }
 
     protected class AutoCloseTask extends TimerTask
     {
         public void run()
         {
             if(log.isDebugEnabled())
                 log.debug("Automatically closing " + this + " after " + autoCloseInactivityDuration + " milliseconds of inactivity.");
             close();
         }
     }
 
     private static ValueSource defaultNoDataFoundMsg = new StaticValueSource("No data available.");
     private boolean closed;
     protected TabularReportValueContext reportValueContext;
     private long creationTime = System.currentTimeMillis();
     private long lastAccessed = System.currentTimeMillis();
     private long autoCloseInactivityDuration = 0;
     private Timer autoCloseTimer;
 
     public AbstractTabularReportDataSource()
     {
     }
 
     public TabularReportValueContext getReportValueContext()
     {
         return reportValueContext;
     }
 
     public void setReportValueContext(TabularReportValueContext reportValueContext)
     {
         this.reportValueContext = reportValueContext;
     }
 
     public void recordActivity()
     {
         lastAccessed = System.currentTimeMillis();
         if(autoCloseTimer != null)
         {
             if(log.isDebugEnabled())
                 log.debug("Activity recorded in " + this + ", resetting to auto close in " + autoCloseInactivityDuration + " milliseconds.");
             scheduleAutoCloseCheck();
         }
     }
 
     public abstract boolean hasMoreRows();
 
     public abstract boolean next();
 
     public abstract void setActiveRow(int rowNum);
 
     public abstract boolean isScrollable();
 
     public abstract int getTotalRows();
 
     public TabularReportDataSourceScrollState createScrollState(String identifier)
     {
         return new ScrollState(identifier);
     }
 
     public int getActiveRowNumber()
     {
         return 0;
     }
 
     public String getHeadingRowColumnData(int columnIndex)
     {
         ValueSource vs = reportValueContext.getReport().getColumn(columnIndex).getHeading();
         return vs != null ? vs.getTextValue(reportValueContext) : null;
     }
 
     public Object getActiveRowColumnData(int columnIndex, int flags)
     {
         return null;
     }
 
     public Object getActiveRowColumnData(String columnName, int flags)
     {
        throw new TabularReportException("getActiveRowColumnData(vc, columnName) is not suppored");
     }
 
     public boolean isHierarchical()
     {
         return false;
     }
 
     public TabularReportDataSource.Hierarchy getActiveHierarchy()
     {
         return null;
     }
 
     public boolean isActiveRowSelected()
     {
         return false;
     }
 
     public ValueSource getNoDataFoundMessage()
     {
         return defaultNoDataFoundMsg;
     }
 
     public void setAutoClose(long autoCloseInactivityDuration)
     {
         this.autoCloseInactivityDuration = autoCloseInactivityDuration;
         scheduleAutoCloseCheck();
     }
 
     protected void scheduleAutoCloseCheck()
     {
         if(log.isDebugEnabled())
             log.debug("Setting " + this + " to auto close in " + autoCloseInactivityDuration + " milliseconds.");
 
         if(autoCloseTimer != null)
         {
             autoCloseTimer.cancel();
             autoCloseTimer = null;
         }
 
         if(autoCloseInactivityDuration > 0)
         {
             autoCloseTimer = new Timer();
             autoCloseTimer.schedule(new AutoCloseTask(), autoCloseInactivityDuration);
         }
     }
 
     public void close()
     {
         if(log.isDebugEnabled())
             log.debug("Closing " + this + ".");
 
         if(autoCloseTimer != null)
         {
             autoCloseTimer.cancel();
             autoCloseTimer = null;
         }
         closed = true;
     }
 
     public boolean isClosed()
     {
         return closed;
     }
 
     public boolean isActive(long timeOut)
     {
         return (System.currentTimeMillis() - lastAccessed) > timeOut;
     }
 }
