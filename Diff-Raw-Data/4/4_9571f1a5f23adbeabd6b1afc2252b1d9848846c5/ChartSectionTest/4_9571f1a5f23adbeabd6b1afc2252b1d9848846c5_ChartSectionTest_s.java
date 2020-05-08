 /* Copyright (c) 2009 Stanford University
  *
  * Permission to use, copy, modify, and distribute this software for any
  * purpose with or without fee is hereby granted, provided that the above
  * copyright notice and this permission notice appear in all copies.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
  * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
  * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
  * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
  * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
  * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
  * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
  */
 
 package org.fiz;
 
 import java.util.*;
 import org.fiz.test.*;
 
 /**
  * Junit tests for the ChartSection class
  */
 
 public class ChartSectionTest extends junit.framework.TestCase {
     protected Dataset cats = new Dataset(
             "record", new Dataset("day", "Mon", "cats", "2"),
             "record", new Dataset("day", "Tue", "cats", "4"));
 
     protected Dataset noCats = new Dataset();
 
     protected ChartSection cs;
     protected ClientRequest cr;
 
     public void setUp() {
         cs = new ChartSection(new Dataset());
         cs.chart = "chart0";
         cs.js = new StringBuilder();
         cr = new ClientRequestFixture();
         cr.addDataRequest("getCats", RawDataManager.newRequest(cats));
         cr.addDataRequest("noCats", RawDataManager.newRequest(noCats));
         cs.cr = cr;
     }
 
     public void test_constructor() {
         Dataset properties = new Dataset("foo", "bar");
         cs = new ChartSection(properties);
         assertNotSame("properties has been cloned", properties, cs.properties);
         assertEquals("contents are equal", "bar", cs.properties.check("foo"));
     }
 
     public void test_render_includedJS() {
         cs.render(cr);
         assertEquals("static/fiz/Chart.js, static/fiz/ChartAxis.js, " +
                      "static/fiz/ChartBar.js, static/fiz/ChartFormat.js, " +
                      "static/fiz/ChartLegend.js, static/fiz/ChartLine.js, " +
                      "static/fiz/ChartPlot.js, static/fiz/ChartScatter.js, " +
                      "static/fiz/ChartSeries.js, static/fiz/ChartTicks.js, " +
                      "static/fiz/ChartTitle.js, static/fiz/Fiz.js, " +
                     "static/fiz/FizCommon.js, static/fiz/canvas.text.js",
                     cr.getHtml().getJsFiles());
     }
 
     public void test_render_HTML_noId() {
         cs.render(cr);
         assertEquals("<div>\n<canvas width=\"500\" height=\"400\" id=\"chart0\">" +
                      "Your browser does not support the canvas element</canvas>\n</div>\n",
                      cr.getHtml().getBody().toString());
         //TestUtil.assertXHTML(cr.getHtml().getBody().toString());
     }
 
 
     public void test_render_HTML_withId() {
         cs =  new ChartSection(new Dataset("id", "foo"));
         cs.render(cr);
         assertEquals("with Id", "<div>\n<canvas width=\"500\" height=\"400\" id=\"foo\">" +
                      "Your browser does not support the canvas element</canvas>\n</div>\n",
                      cr.getHtml().getBody().toString());
         //TestUtil.assertXHTML(cr.getHtml().getBody().toString());
     }
 
     public void test_render_topLevelPlot() {
         cs = new ChartSection(new Dataset("request", "noCats"));
         cs.render(cr);
         assertEquals("Fiz.ids.chart0 = {};\n" +
                      "Fiz.ids.chart0.chart = new Fiz.Chart(\"chart0\");\n" +
                      "Fiz.ids.chart0.chart0_data = [];\n" +
                      "Fiz.ids.chart0.chart0 = new Fiz.Chart.Bar(Fiz.ids.chart0.chart0_data);\n" +
                      "Fiz.ids.chart0.chart.addPlot(Fiz.ids.chart0.chart0);\n" +
                      "Fiz.ids.chart0.chart.draw();\n", cs.js.toString());
 
     }
 
     public void test_render_topLevelSeries() {
         cs = new ChartSection(new Dataset("series", new Dataset("request", "noCats")));
         cs.render(cr);
         assertEquals("Fiz.ids.chart0 = {};\n" +
                      "Fiz.ids.chart0.chart = new Fiz.Chart(\"chart0\");\n" +
                      "Fiz.ids.chart0.chart0 = new Fiz.Chart.Bar();\n" +
                      "Fiz.ids.chart0.series0_data = [];\n" +
                      "Fiz.ids.chart0.series0 = new Fiz.Chart.Series(Fiz.ids.chart0.series0_data);\n" +
                      "Fiz.ids.chart0.chart0.addSeries(Fiz.ids.chart0.series0);\n" +
                      "Fiz.ids.chart0.chart.addPlot(Fiz.ids.chart0.chart0);\n" +
                      "Fiz.ids.chart0.chart.draw();\n", cs.js.toString());
 
     }
 
     public void test_render_withProperties() {
         cs = new ChartSection(new Dataset("request", "noCats", "foo", "bar"));
         cs.render(cr);
         assertEquals("Fiz.ids.chart0 = {};\n" +
                      "Fiz.ids.chart0.chart = new Fiz.Chart(\"chart0\");\n" +
                      "Fiz.ids.chart0.chart0_data = [];\n" +
                      "Fiz.ids.chart0.chart0 = new Fiz.Chart.Bar(Fiz.ids.chart0.chart0_data);\n" +
                      "Fiz.ids.chart0.chart0.set(\"foo\", \"bar\");\n" +
                      "Fiz.ids.chart0.chart.addPlot(Fiz.ids.chart0.chart0);\n" +
                      "Fiz.ids.chart0.chart.draw();\n", cs.js.toString());
     }
 
 
     public void test_render_multiplePlots() {
         cs = new ChartSection(new Dataset("plot", new Dataset("request", "noCats"),
                                           "plot", new Dataset("request", "getMoreCats")));
         cr.addDataRequest("getMoreCats", RawDataManager.newRequest(noCats));
         cs.render(cr);
         assertEquals("Fiz.ids.chart0 = {};\n" +
                      "Fiz.ids.chart0.chart = new Fiz.Chart(\"chart0\");\n" +
                      "Fiz.ids.chart0.plot0_data = [];\n" +
                      "Fiz.ids.chart0.plot0 = new Fiz.Chart.Bar(Fiz.ids.chart0.plot0_data);\n" +
                      "Fiz.ids.chart0.chart.addPlot(Fiz.ids.chart0.plot0);\n" +
                      "Fiz.ids.chart0.plot1_data = [];\n" +
                      "Fiz.ids.chart0.plot1 = new Fiz.Chart.Bar(Fiz.ids.chart0.plot1_data);\n" +
                      "Fiz.ids.chart0.chart.addPlot(Fiz.ids.chart0.plot1);\n" +
                      "Fiz.ids.chart0.chart.draw();\n", cs.js.toString());
 
     }
 
     public void test_addPlot_noSeries() {
         cs.addPlot(new Dataset("request", "noCats"));
         assertEquals("chart0.plot0_data = [];\nchart0.plot0 = new " +
                      "Fiz.Chart.Bar(chart0.plot0_data);\nchart0.chart." +
                      "addPlot(chart0.plot0);\n", cs.js.toString());
     }
 
     public void test_addPlot_noSeriesWithId() {
         cs.addPlot(new Dataset("request", "noCats", "id", "foo"));
         assertEquals("chart0.foo_data = [];\nchart0.foo = new " +
                      "Fiz.Chart.Bar(chart0.foo_data);\nchart0.chart." +
                      "addPlot(chart0.foo);\n", cs.js.toString());
     }
 
     public void test_addPlot_noSeriesWithType() {
         cs.addPlot(new Dataset("request", "noCats", "type", "Line"));
         assertEquals("chart0.plot0_data = [];\nchart0.plot0 = new " +
                      "Fiz.Chart.Line(chart0.plot0_data);\nchart0.chart." +
                      "addPlot(chart0.plot0);\n", cs.js.toString());
     }
 
     public void test_addPlot_noSeries_withProperties() {
         cs.addPlot(new Dataset("request", "noCats", "foo", "bar"));
         assertEquals("chart0.plot0_data = [];\n" +
                      "chart0.plot0 = new Fiz.Chart.Bar(chart0.plot0_data);\n" +
                      "chart0.plot0.set(\"foo\", \"bar\");\n" +
                      "chart0.chart.addPlot(chart0.plot0);\n", cs.js.toString());
     }
 
     public void test_addPlot_withSeries() {
         cr.addDataRequest("getMoreCats", RawDataManager.newRequest(noCats));
         cs.addPlot(new Dataset("series", new Dataset("request", "noCats"),
                                "series", new Dataset("request", "getMoreCats")));
         assertEquals("chart0.plot0 = new Fiz.Chart.Bar();\n" +
                      "chart0.series0_data = [];\n" +
                      "chart0.series0 = new Fiz.Chart.Series(chart0.series0_data);\n" +
                      "chart0.plot0.addSeries(chart0.series0);\n" +
                      "chart0.series1_data = [];\n" +
                      "chart0.series1 = new Fiz.Chart.Series(chart0.series1_data);\n" +
                      "chart0.plot0.addSeries(chart0.series1);\n" +
                      "chart0.chart.addPlot(chart0.plot0);\n", cs.js.toString());
 
     }
 
     public void test_addPlot_withSeriesWithId() {
         cs.addPlot(new Dataset("series", new Dataset("request", "noCats",
                                                      "id", "foo")));
         assertEquals("chart0.plot0 = new Fiz.Chart.Bar();\n" +
                      "chart0.foo_data = [];\n" +
                      "chart0.foo = new Fiz.Chart.Series(chart0.foo_data);\n" +
                      "chart0.plot0.addSeries(chart0.foo);\n" +
                      "chart0.chart.addPlot(chart0.plot0);\n", cs.js.toString());
 
     }
 
     public void test_addPlot_withSeriesWithType() {
         cs.addPlot(new Dataset("series", new Dataset("request", "noCats"), "type", "Line"));
         assertEquals("chart0.plot0 = new Fiz.Chart.Line();\n" +
                      "chart0.series0_data = [];\n" +
                      "chart0.series0 = new Fiz.Chart.Series(chart0.series0_data);\n" +
                      "chart0.plot0.addSeries(chart0.series0);\n" +
                      "chart0.chart.addPlot(chart0.plot0);\n", cs.js.toString());
 
     }
 
     public void test_addPlot_withSeriesWithProperties() {
         cs.addPlot(new Dataset("series", new Dataset("request", "noCats", "bar", "foo")));
         assertEquals("chart0.plot0 = new Fiz.Chart.Bar();\n" +
                      "chart0.series0_data = [];\n" +
                      "chart0.series0 = new Fiz.Chart.Series(chart0.series0_data);\n" +
                      "chart0.series0.set(\"bar\", \"foo\");\n" +
                      "chart0.plot0.addSeries(chart0.series0);\n" +
                      "chart0.chart.addPlot(chart0.plot0);\n", cs.js.toString());
 
     }
 
     public void test_getTypeOfPlot() {
         assertEquals("null", "Bar", cs.getTypeOfPlot(new Dataset()));
         assertEquals("given", "Scatter", cs.getTypeOfPlot(new Dataset("type", "Scatter")));
     }
 
     public void test_addData_noRequest() {
         try {
             cs.addData(new Dataset("xId", "day", "yId", "cats"));
             fail("Dataset.MissingValueError not thrown");
         } catch (Dataset.MissingValueError e) {
             assertEquals("couldn't find dataset element \"request\"", e.getMessage());
             return;
         }
     }
 
     public void test_addData_missingRequest() {
         try {
             cs.addData(new Dataset("request", "bogus", "xId", "day", "yId", "cats"));
             fail("InternalError not thrown");
         } catch (InternalError e) {
             assertEquals("couldn't find data request named \"bogus\"", e.getMessage());
             return;
         }
     }
 
     public void test_addData_nullResponse() {
         cr.addDataRequest("error", RawDataManager.newError());
         try {
             cs.addData(new Dataset("request", "error", "xId", "day", "yId", "cats"));
             fail("InternalError not thrown");
         } catch (DataRequest.RequestError e) {
             assertEquals("error in data request raw.error: ", e.getMessage());
             return;
         }
     }
 
     public void test_addData_badXId() {
         try {
             cs.addData(new Dataset("request", "getCats", "xId", "bogus",
                                    "yId", "cats", "id", ""));
         fail("Dataset.MissingValueError not thrown");
         } catch (Dataset.MissingValueError e) {
             assertEquals("couldn't find dataset element \"bogus\"", e.getMessage());
             return;
         }
     }
 
     public void test_addData_badYId() {
         try {
             cs.addData(new Dataset("request", "getCats", "xId", "day",
                                    "yId", "bogus", "id", ""));
             fail("Dataset.MissingValueError not thrown");
         } catch (Dataset.MissingValueError e) {
             assertEquals("couldn't find dataset element \"bogus\"", e.getMessage());
             return;
         }
     }
 
     public void test_addData() {
         cs.addData(new Dataset("request", "getCats", "xId", "day", "yId", "cats", "id", "chart0"));
         assertEquals("chart0.chart0_data = [[\"Mon\", 2], [\"Tue\", 4]];\n", cs.js.toString());
     }
 
     public void test_setProperties_subobject() {
         cs.setProperties(new Dataset("xAxis", new Dataset("title", "foo")), "chart");
         assertEquals("chart0.chart.set(\"xAxisTitle\", \"foo\");\n", cs.js.toString());
     }
 
     public void test_setProperties_chartObjChartVar() {
         cs.setProperties(new Dataset("borderColor", "foo"), "chart");
         assertEquals("chart0.chart.set(\"borderColor\", \"foo\");\n", cs.js.toString());
     }
 
     public void test_setProperties_chartObjPlotVar() {
         cs.setProperties(new Dataset("bar", "foo"), "chart");
         assertEquals("", cs.js.toString());
     }
 
     public void test_setProperties_plotObjChartVar() {
         cs.setProperties(new Dataset("borderColor", "foo"), "plot");
         assertEquals("", cs.js.toString());
     }
     public void test_setProperties_plotObjPlotVar() {
         cs.setProperties(new Dataset("baz", "foo"), "plot");
         assertEquals("chart0.plot.set(\"baz\", \"foo\");\n", cs.js.toString());
     }
 
     public void test_setProperties_reserved() {
         cs.setProperties(new Dataset("xId", "", "yId", "", "request", "",
                                      "id", "", "type", "", "plot", "",
                                      "series", ""), "foo");
         assertEquals("", cs.js.toString());
     }
 
     public void test_combineCamel() {
         assertEquals("normal strings", "fooBar", cs.combineCamel("foo", "bar"));
         assertEquals("empty first param", "Baz", cs.combineCamel("", "baz"));
         assertEquals("empty second param", "baz", cs.combineCamel("baz", ""));
     }
 
     public void test_hasKey() {
         String[] keys = {"foo", "bar"};
         assertTrue("has key", cs.hasKey(keys, "bar"));
         assertFalse("does't have key", cs.hasKey(keys, "baz"));
     }
 
     public void test_getId_noId() {
         Dataset data = new Dataset("foo", "bar");
         cr = new ClientRequestFixture();
         String id = cs.getId(data, "baz");
         assertEquals("return value", "baz0", id);
         assertEquals("dataset value", "baz0", data.check("id"));
         assertEquals("second time", "baz1", cs.getId(new Dataset(), "baz"));
     }
 
     public void test_getId_withId() {
         Dataset data = new Dataset("id", "foo4");
         assertEquals("foo4", cs.getId(data, "baz"));
     }
 }
