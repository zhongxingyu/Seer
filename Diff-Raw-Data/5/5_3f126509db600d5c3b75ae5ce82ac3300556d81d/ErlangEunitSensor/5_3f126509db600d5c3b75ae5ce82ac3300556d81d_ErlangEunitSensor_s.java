 /*
  * Sonar Erlang Plugin
  * Copyright (C) 2012 Tamás Kende
  * kende.tamas@gmail.com
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
  */
 package org.sonar.plugins.erlang.sensor;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 import org.apache.commons.io.FileUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.sonar.api.batch.SensorContext;
 import org.sonar.api.measures.CoreMetrics;
 import org.sonar.api.measures.Measure;
 import org.sonar.api.resources.InputFile;
 import org.sonar.api.resources.Project;
 import org.sonar.api.utils.ParsingUtils;
 import org.sonar.plugins.erlang.ErlangPlugin;
 import org.sonar.plugins.erlang.language.Erlang;
 import org.sonar.plugins.erlang.language.ErlangFile;
import org.sonar.plugins.erlang.testmetrics.eunit.EunitReportParserTest;
 import org.sonar.plugins.erlang.testmetrics.eunit.Report;
 import org.sonar.plugins.erlang.testmetrics.utils.GenericExtFilter;
 import org.sonar.plugins.erlang.testmetrics.utils.TestSensorUtils;
 
 public final class ErlangEunitSensor extends AbstractErlangSensor {
 
 	public ErlangEunitSensor(Erlang erlang) {
 		super(erlang);
 	}
 
 	private final static Logger LOG = LoggerFactory.getLogger(ErlangEunitSensor.class);
 
 	public void analyse(Project project, SensorContext context) {
 		collect(project, context, new File(project.getFileSystem().getBasedir(), erlang.getEunitFolder()));
 	}
 
 	protected void collect(final Project project, final SensorContext context, File reportsDir) {
 		LOG.debug("Parsing eunit results from folder {}", reportsDir);
 
 		GenericExtFilter filter = new GenericExtFilter(".xml");
 
 		if (reportsDir.isDirectory() == false) {
 			LOG.warn("Folder does not exist {}", reportsDir);
 			return;
 		}
 
 		String[] list = reportsDir.list(filter);
 
 		if (list.length == 0) {
 			LOG.warn("no files end with : ", reportsDir);
 			return;
 		}
 
 		for (String file : list) {
 			if(!file.matches(".*_eunit.xml")){
 				continue;
 			}
 			/**
 			 * TODO how can I get specific test resources??? they were added by the ErlangSourceImporterSensor.... or it is not possible?
 			 * like: 
 			 * String resourceKey = project.getEffectiveKey()+":"+eunitTestName;
 			 * org.sonar.api.resources.File unitTestFileResource = new org.sonar.api.resources.File(erlang, resourceKey);
 			 */
 			String eunitTestName = file.replaceAll("(TEST-)(.*?)(\\.xml)", "$2").concat(ErlangPlugin.EXTENSION);
 			
 			InputFile eunitFile = TestSensorUtils.findFileForReport(project.getFileSystem().testFiles(erlang.getKey()),eunitTestName);
 			ErlangFile unitTestFileResource = ErlangFile.fromInputFile(eunitFile, true);
 			LOG.debug("Adding unittest resource: {}", unitTestFileResource.toString());
 
 			String source = "";
 			File eunitReport = new File(reportsDir, file);
 			try {
 				source = FileUtils.readFileToString(eunitReport, project.getFileSystem().getSourceCharset().name());
 			} catch (IOException e) {
 				source = "Could not find source for JsTestDriver unit test: " + file + " in any of test directories";
 				LOG.debug(source, e);
 			}
 
			Report report = EunitReportParserTest.parse(eunitReport);
 			if (report.getTests() > 0) {
 				double testsCount = report.getTests() - report.getSkipped();
 				context.saveMeasure(unitTestFileResource, CoreMetrics.SKIPPED_TESTS, report.getSkipped());
 				context.saveMeasure(unitTestFileResource, CoreMetrics.TESTS, testsCount);
 				context.saveMeasure(unitTestFileResource, CoreMetrics.TEST_ERRORS, report.getErrors());
 				context.saveMeasure(unitTestFileResource, CoreMetrics.TEST_FAILURES, report.getFailures());
 				context.saveMeasure(unitTestFileResource, CoreMetrics.TEST_EXECUTION_TIME,
 						report.getDurationMilliseconds());
 				double passedTests = testsCount - report.getErrors() - report.getFailures();
 				if (testsCount > 0) {
 					double percentage = passedTests * 100d / testsCount;
 					context.saveMeasure(unitTestFileResource, CoreMetrics.TEST_SUCCESS_DENSITY,
 							ParsingUtils.scaleValue(percentage));
 				}
 				context.saveMeasure(unitTestFileResource, new Measure(CoreMetrics.TEST_DATA, report.getTestSuite().toXml()));
 			}
 
 		}
 
 	}
 
 	protected String getUnitTestFileName(String className) {
 		String fileName = className.substring(className.indexOf('.') + 1);
 		fileName = fileName.replace('.', '/');
 		fileName = fileName + ".js";
 		return fileName;
 	}
 
 	protected File getUnitTestFile(List<File> testDirectories, String name) {
 		File unitTestFile = new File("");
 		for (File dir : testDirectories) {
 			unitTestFile = new File(dir, name);
 
 			if (unitTestFile.exists()) {
 				break;
 			}
 		}
 		return unitTestFile;
 	}
 
 	@Override
 	public String toString() {
 		return getClass().getSimpleName();
 	}
 }
