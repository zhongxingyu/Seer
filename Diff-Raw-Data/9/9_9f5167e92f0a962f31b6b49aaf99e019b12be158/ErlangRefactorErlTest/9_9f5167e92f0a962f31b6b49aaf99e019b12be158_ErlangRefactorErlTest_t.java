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
 package org.sonar.plugins.erlang.violation.refactorerl;
 
 import static org.junit.Assert.assertThat;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.configuration.Configuration;
 import org.hamcrest.Matchers;
 import org.junit.Before;
 import org.junit.Test;
 import org.sonar.api.profiles.RulesProfile;
 import org.sonar.api.resources.InputFile;
 import org.sonar.api.resources.InputFileUtils;
 import org.sonar.api.resources.Project;
 import org.sonar.api.rules.ActiveRule;
 import org.sonar.plugins.erlang.utils.ProjectUtil;
 import org.sonar.plugins.erlang.utils.RuleUtil;
 import org.sonar.plugins.erlang.violations.ErlangRuleManager;
 import org.sonar.plugins.erlang.violations.ErlangRuleRepository;
 import org.sonar.plugins.erlang.violations.ViolationReport;
 import org.sonar.plugins.erlang.violations.refactorerl.ErlangRefactorErl;
 
 public class ErlangRefactorErlTest {
 
 	private ErlangRefactorErl er;
 	private Configuration configuration;
 	private ViolationReport result;
 
 	@Before
 	public void setup() throws URISyntaxException, IOException {
 		er = new ErlangRefactorErl();
 		configuration = mock(Configuration.class);
 		RulesProfile rp = mock(RulesProfile.class);
 		List<ActiveRule> rlz = new ArrayList<ActiveRule>();
 		ActiveRule activeRule = RuleUtil.generateActiveRule("mcCabe","R001","maximum","10");
 		ActiveRule activeRule2 = RuleUtil.generateActiveRule("max_depth_of_calling","R002","maximum","10");
 		rlz.add(activeRule);
 		rlz.add(activeRule2);
 		when(rp.getActiveRulesByRepository("Erlang")).thenReturn(rlz);
 	    
 		File fileToAnalyse =  new File(getClass().getResource("/org/sonar/plugins/erlang/erlcount/src/refactorerl_issues.erl")
 				.toURI());
 		InputFile inputFile = InputFileUtils.create(fileToAnalyse.getParentFile(), fileToAnalyse);
 		ArrayList<InputFile> inputFiles = new ArrayList<InputFile>();
 		inputFiles.add(inputFile);
		Project project = ProjectUtil.getProject(inputFiles, null, configuration);
 		result = er.refactorErl(project, new ErlangRuleManager(ErlangRuleRepository.REFACTORERL_PATH),rp);
 	}
 
 	
 	@Test
 	public void checkRefactorErl() {
 		assertThat(result.getUnits().size(), Matchers.equalTo(2));
 		assertThat(result.getUnits().get(1).getMetricKey(), Matchers.equalTo("R001"));
 		assertThat(result.getUnits().get(1).getStartRow(), Matchers.equalTo(4));
 		assertThat(result.getUnits().get(1).getDescription(), Matchers.equalTo("mcCabe is 11 (max allowed is 10)"));
 		
 	}
 	
 }
