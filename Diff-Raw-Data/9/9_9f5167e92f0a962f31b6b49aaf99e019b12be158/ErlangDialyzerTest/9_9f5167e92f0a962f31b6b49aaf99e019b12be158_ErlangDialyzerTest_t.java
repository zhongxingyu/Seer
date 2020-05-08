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
 package org.sonar.plugins.erlang.violation.dialyzer;
 
 import static org.junit.Assert.assertThat;
 import static org.mockito.Mockito.mock;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 
 import org.apache.commons.configuration.Configuration;
 import org.hamcrest.Matchers;
 import org.junit.Before;
 import org.junit.Test;
 import org.sonar.api.resources.InputFile;
 import org.sonar.api.resources.InputFileUtils;
 import org.sonar.api.resources.Project;
 import org.sonar.plugins.erlang.utils.ProjectUtil;
 import org.sonar.plugins.erlang.violations.ErlangRuleManager;
 import org.sonar.plugins.erlang.violations.ErlangRuleRepository;
 import org.sonar.plugins.erlang.violations.ViolationReport;
 import org.sonar.plugins.erlang.violations.dialyzer.ErlangDialyzer;
 
 public class ErlangDialyzerTest {
 
 	private ErlangDialyzer ed;
 	private Configuration configuration;
 	private ViolationReport result;
 
 	@Before
 	public void setup() throws URISyntaxException, IOException {
 		ed = new ErlangDialyzer();
 		configuration = mock(Configuration.class);
 		 
 		File fileToAnalyse =  new File(getClass().getResource("/org/sonar/plugins/erlang/erlcount/src/erlcount_lib.erl")
 				.toURI());
 		InputFile inputFile = InputFileUtils.create(fileToAnalyse.getParentFile(), fileToAnalyse);
 		ArrayList<InputFile> inputFiles = new ArrayList<InputFile>();
 		inputFiles.add(inputFile);
		Project project = ProjectUtil.getProject(inputFiles, null, configuration);
 		result = ed.dialyzer(project, new ErlangRuleManager(ErlangRuleRepository.DIALYZER_PATH));
 	}
 
 	@Test
 	public void checkDialyzer() {
 		assertThat(result.getUnits().size(), Matchers.equalTo(3));
 		assertThat(result.getUnits().get(0).getMetricKey(), Matchers.equalTo("D019"));
 		assertThat(result.getUnits().get(0).getStartRow(), Matchers.equalTo(54));
 	}
 }
