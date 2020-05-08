 /*
  * Copyright (c) 2000-2003 Netspective Communications LLC. All rights reserved.
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
  *    used to endorse products derived from The Software without without written consent of Netspective. "Netspective",
  *    "Axiom", "Commons", "Junxion", and "Sparx" may not appear in the names of products derived from The Software
  *    without written consent of Netspective.
  *
  * 5. Please attribute functionality where possible. We suggest using the "powered by Netspective" button or creating
  *    a "powered by Netspective(tm)" link to http://www.netspective.com for each application using The Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
  * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
  * ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
  * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
  * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
  * IF HE HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
  *
  * @author Shahid N. Shah
  */
 
 /**
 * $Id: ReportTest.java,v 1.11 2003-06-10 02:51:35 shahid.shah Exp $
  */
 
 package com.netspective.commons.report;
 
 import com.netspective.commons.io.Resource;
 import com.netspective.commons.report.tabular.*;
 import com.netspective.commons.report.tabular.column.GeneralColumn;
 import com.netspective.commons.xdm.XdmComponentFactory;
 import junit.framework.TestCase;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.List;
 import java.text.DecimalFormat;
 
 public class ReportTest extends TestCase
 {
 	public static String RESOURCE_NAME_ONE = "ReportTest-One.xml";
 	protected ReportsComponent component = null;
 	protected ReportsManager reportsManager = null;
 	protected Reports reports = null;
 
     public class TestReportDataSource extends AbstractTabularReportDataSource
     {
         private List rows = new ArrayList();
         private Object[] activeRow;
         private int activeRowNum = -1;
         private int lastRowNum;
 
         public TestReportDataSource(TabularReportValueContext vc)
         {
             super();
             for(int i = 0; i < 25; i++)
             {
                 rows.add(new Object[] { "row " + i, new Integer(100 + i), new Double(200 + i + (200.0 + i)/1000.0) });
             }
             lastRowNum = rows.size() - 1;
         }
 
         public int getTotalRows()
         {
             return rows.size();
         }
 
         public boolean hasMoreRows()
         {
             return activeRowNum < lastRowNum;
         }
 
         public boolean isScrollable()
         {
             return true;
         }
 
         public void setActiveRow(int rowNum)
         {
             activeRowNum = rowNum;
             activeRow = (Object[]) rows.get(activeRowNum);
         }
 
         public boolean next()
         {
             if(! hasMoreRows())
                 return false;
 
             setActiveRow(activeRowNum + 1);
             return true;
         }
 
         public Object getActiveRowColumnData(int columnIndex, int flags)
         {
             return activeRow[columnIndex];
         }
 
 	    public int getActiveRowNumber () {
 		    return activeRowNum;
 	    }
     }
 
 	public void testDataSource()
 	{
 		TabularReportDataSource trds = new TestReportDataSource(null);
 
 		assertNotNull(trds);
 		assertFalse(trds.isHierarchical());
 
 		for (int i = 0; i < 25; i ++)
 		{
 			assertTrue(trds.next());
 			assertEquals(i, trds.getActiveRowNumber());
 
 			// Verify stored data...
 			assertEquals("row " + i, trds.getActiveRowColumnData(0, 0));
 			assertEquals(new Integer(100 + i), trds.getActiveRowColumnData(1, 0));
 			assertEquals(new Double(200 + i + (200.0 + i)/1000.0), trds.getActiveRowColumnData(2, 0));
 		}
 	}
 
 	protected void setUp () throws Exception {
 		super.setUp();
 
 		component =	(ReportsComponent) XdmComponentFactory.get(
 			ReportsComponent.class,
 			new Resource(ReportTest.class, RESOURCE_NAME_ONE),
 			XdmComponentFactory.XDMCOMPFLAGS_DEFAULT);
 
 		assertNotNull(component);
 
 		if(component.getErrors().size() > 0)
 			System.out.println(component.getErrors());
 		assertEquals(0, component.getErrors().size());
 
 		reportsManager = component.getItems();
 		assertNotNull(reportsManager);
 		reports = reportsManager.getReports();
 		assertNotNull(reports);
 	}
 
 	public void testReports()
 	{
 		assertEquals(2, reports.size());
 
 		Report reportOne = reports.get(0);
 		Report reportTwo = reports.get("test-001");
 
 		assertEquals(reportOne, reportTwo);
 	}
 
 	public void testTabularReport()
 	{
 		TabularReport report = (TabularReport) reports.get(0);
         assertEquals("test-001", report.getName());
 
 		// This is the Add/Edit/Delete area under the report banner... non-existent in this one
         TabularReportColumns trColumns = report.getColumns();
 		assertNotNull(trColumns);
 		assertEquals(4, trColumns.size());
 
 		TabularReportColumn columnFourA = report.getColumn(3);
 		TabularReportColumn columnFourB = (TabularReportColumn) trColumns.get(3);
 		assertEquals(columnFourA, columnFourB);
 		assertEquals("Column D", columnFourA.getHeading().getTextValue(null));
 	}
 
 	public void testReportSkins()
 	{
 		TabularReportSkin skin = new TextReportSkin(".txt", "\t", null);
 		TextReportSkin trSkin = (TextReportSkin) skin;
 
 		assertEquals(".txt", skin.getFileExtension());
 		assertEquals("", skin.getBlankValue());
         assertEquals(String.class.getName(), skin.constructClassRef(String.class));
 
 		assertEquals(".txt", trSkin.getFileExtension());
 		assertEquals("\t", trSkin.getDelimiter());
 		assertNull(trSkin.getTextQualifier());
 		assertEquals("", trSkin.getBlankValue());
         assertEquals(String.class.getName(), trSkin.constructClassRef(String.class));
 	}
 
 	public void testReportValueContext() throws IOException
 	{
 		TabularReport report = (TabularReport) reports.get(0);
 
 		TabularReportSkin skin = new TextReportSkin(".txt", "\t", null);
 		TabularReportValueContext vc = new BasicTabularReportValueContext(report, skin);
 
         assertEquals(0, vc.getListeners().size());
 		assertEquals(report, vc.getReport());
 		assertEquals(skin, vc.getSkin());
 
 		assertEquals(4, vc.getVisibleColsCount());
 		vc.getState(3).getFlags().setFlag(TabularReportColumn.Flags.HIDDEN);
 		assertEquals(4, vc.getVisibleColsCount());
 
 		StringWriter expectedSw = new StringWriter();
 		expectedSw.write("Column A\tColumn B\tColumn C\t\n");
 		expectedSw.write("row 0\t100\t200.2\t\n");
 		expectedSw.write("row 1\t101\t201.201\t\n");
 		expectedSw.write("row 2\t102\t202.202\t\n");
 		expectedSw.write("row 3\t103\t203.203\t\n");
 		expectedSw.write("row 4\t104\t204.204\t\n");
 		expectedSw.write("row 5\t105\t205.205\t\n");
 		expectedSw.write("row 6\t106\t206.206\t\n");
 		expectedSw.write("row 7\t107\t207.207\t\n");
 		expectedSw.write("row 8\t108\t208.208\t\n");
 		expectedSw.write("row 9\t109\t209.209\t\n");
 		expectedSw.write("row 10\t110\t210.21\t\n");
 		expectedSw.write("row 11\t111\t211.211\t\n");
 		expectedSw.write("row 12\t112\t212.212\t\n");
 		expectedSw.write("row 13\t113\t213.213\t\n");
 		expectedSw.write("row 14\t114\t214.214\t\n");
 		expectedSw.write("row 15\t115\t215.215\t\n");
 		expectedSw.write("row 16\t116\t216.216\t\n");
 		expectedSw.write("row 17\t117\t217.217\t\n");
 		expectedSw.write("row 18\t118\t218.218\t\n");
 		expectedSw.write("row 19\t119\t219.219\t\n");
 		expectedSw.write("row 20\t120\t220.22\t\n");
 		expectedSw.write("row 21\t121\t221.221\t\n");
 		expectedSw.write("row 22\t122\t222.222\t\n");
 		expectedSw.write("row 23\t123\t223.223\t\n");
 		expectedSw.write("row 24\t124\t224.224\t\n");
 		expectedSw.write("25\t2,800\t5,305.3\t\n");
 
 		StringWriter sw = new StringWriter();
         TabularReportDataSource ds = new TestReportDataSource(vc);
        ds.setReportValueContext(vc);
 		vc.produceReport(sw, ds);
         ds.close();
 
 		assertEquals(expectedSw.toString(), sw.toString());
 	}
 
 	public void testTabularColumns()
 	{
 		TabularReport report = (TabularReport) reports.get(0);
 
 		TabularReportSkin skin = new TextReportSkin(".txt", "\t", null);
 		TabularReportValueContext vc = new BasicTabularReportValueContext(report, skin);
 
 		TabularReportColumns columns = vc.getColumns();
 		assertEquals(4, columns.size());
 
 		String[] colHeading = new String[] { "Column A", "Column B", "Column C", "Column D" };
 		for (int i = 0; i < columns.size(); i ++)
 		{
 			TabularReportColumn column = columns.getColumn(i);
 
 			// Tests that apply to all columns ...
 			assertEquals(0, column.getDataType());
 			column.setDataType(2);
 			assertEquals(2, column.getDataType());
 			column.setDataType(0);
 
 			assertNull(column.getBreak());
 			column.setBreak("Column C");
 			assertEquals("Column C", column.getBreak());
 			column.setBreak(null);
 
 			assertEquals(i, column.getColIndex());
 			assertNull(column.getConditionals());
 			assertEquals(colHeading[i], column.getHeading().getTextValue(null));
 
 			assertEquals(0, column.getWidth());
 			column.setWidth(10);
 			assertEquals(10, column.getWidth());
 
 			// Tests that apply to the non-output columns only ...
 			if (3 > i)
 			{
 				assertEquals(0, column.getFlags().getFlags());
 				assertNull(column.getOutput());
 			}
 
 			// Tests that apply to the summed numeric columns only ...
 			if (1 == i || 2 == i)
 			{
 				assertEquals(TabularReportColumn.ALIGN_RIGHT, column.getAlign());
 				assertEquals("sum", column.getCalcCmd());
 				assertEquals(DecimalFormat.class, column.getFormatter().getClass());
 			}
 
 			// Tests that apply to the floating point column only ...
 			if (2 == i)
 			{
 				column.setFormat("currency");
 				assertEquals(DecimalFormat.class, column.getFormatter().getClass());
 			}
 
 			// Tests that apply to the two string columns only ...
 			if (0 == i || 3 == i)
 			{
 				assertNull(column.getFormatter());
 
 				GeneralColumn gColumn = (GeneralColumn) column;
 				assertTrue(gColumn.isColIndexSet());
 			}
 
 			// Tests that apply to the counted column only ...
 			if (0 == i)
 			{
 				assertEquals("count", column.getCalcCmd());
 			}
 
 			// Tests that apply to the output column only ...
 			if (3 == i)
 			{
 				assertEquals("${0} ${1} ${2}", column.getOutput());
                 assertEquals(TabularReportColumn.Flags.HAS_OUTPUT_PATTERN, column.getFlags().getFlags());
 			}
 
 //			System.out.println("Column #" + i);
 //			System.out.println("\tFormatted: " + column.getFormatter());
 		}
 	}
 
 	public void testTabularReportColumnState()
 	{
 		TabularReport report = (TabularReport) reports.get(0);
 
 		TabularReportSkin skin = new TextReportSkin(".txt", "\t", null);
 		TabularReportValueContext vc = new BasicTabularReportValueContext(report, skin);
 
 		TabularReportColumnState[] trcState = vc.getStates();
 		String[] colHeadings = new String[] { "Column A", "Column B", "Column C", "Column D" };
 		for (int i = 0; i < trcState.length; i ++)
 		{
 			assertEquals(trcState[i], vc.getState(i));
 			assertTrue(trcState[i].isVisible());
 			assertEquals(colHeadings[i], report.getColumn(i).getHeading().getTextValue(vc));
 
 			String origOutputFormat = trcState[i].getOutputFormat();
 			trcState[i].setOutputFormat("${" + i + "}");
 			assertEquals("${" + i + "}", trcState[i].getOutputFormat());
 			trcState[i].setOutputFormat(origOutputFormat);
 
 			if (1 == i || 2 == i)
 				assertEquals("text-align: right;", trcState[i].getCssStyleAttrValue());
 			else
 				assertEquals("", trcState[i].getCssStyleAttrValue());
 
 			if (i < 3)
 			{
 				trcState[i].setOutputFormat(null);
 
 				assertTrue(trcState[i].haveCalc());
 				assertNotNull(trcState[i].getCalc());
 
 				assertEquals(0, trcState[i].getFlags().getFlags());
 				assertFalse(trcState[i].getFlags().flagIsSet(TabularReportColumn.Flags.HIDDEN));
 				trcState[i].getFlags().setFlag(TabularReportColumn.Flags.HIDDEN);
 				assertTrue(trcState[i].getFlags().flagIsSet(TabularReportColumn.Flags.HIDDEN));
 				trcState[i].getFlags().clearFlag(TabularReportColumn.Flags.HIDDEN);
 				assertFalse(trcState[i].getFlags().flagIsSet(TabularReportColumn.Flags.HIDDEN));
 				trcState[i].getFlags().updateFlag(TabularReportColumn.Flags.HIDDEN, true);
 				assertTrue(trcState[i].getFlags().flagIsSet(TabularReportColumn.Flags.HIDDEN));
 				trcState[i].getFlags().updateFlag(TabularReportColumn.Flags.HIDDEN, false);
 				assertFalse(trcState[i].getFlags().flagIsSet(TabularReportColumn.Flags.HIDDEN));
 
 				assertNull(trcState[i].getOutputFormat());
 			}
 			else
 			{
 				assertFalse(trcState[i].haveCalc());
 				assertEquals("${0} ${1} ${2}", trcState[i].getOutputFormat());
 			}
 		}
 	}
 
 }
 
