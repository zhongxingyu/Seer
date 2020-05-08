 package com.hp.it.sonar.access.impl;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import com.hp.it.common.string.StringUtil;
 import com.hp.it.sonar.access.PortalAccessor;
 import com.hp.it.sonar.access.ProjectAccessor;
 import com.hp.it.sonar.access.RuleAccessor;
 import com.hp.it.sonar.bean.Project;
 import com.hp.it.sonar.bean.Rule;
 import com.hp.it.sonar.bean.Violation;
 
 public class PortalDataAccessor implements PortalAccessor {
 
 	private final static String PROJECT_OVERVIEW = "::portalURL/dashboard/index/::projectId?period=::periodPhase";
 
 	private final static String VIOLATION_DRILLDOWN = "::portalURL/drilldown/violations/::projectId?period=::periodPhase&priority=::priority";
 
 	private String portalURL;
 
 	private ProjectAccessor projectDao;
 
 	private RuleAccessor ruleDao;
 
 	public PortalDataAccessor(String portalURL) {
 		this.portalURL = portalURL;
 	}
 
 	public Map<String, String> retrieveViolationChangeSummary(String group, String artifact, int period) {
 		Project project = projectDao.getProject(group, artifact);
 		String urlStr = PROJECT_OVERVIEW.replaceFirst("::portalURL", this.portalURL)
 				.replaceFirst("::projectId", String.valueOf(project.getId()))
 				.replaceFirst("::periodPhase", String.valueOf(period));
 		Map map = new HashMap();
 		try {
 			URL url = new URL(urlStr);
 			String content = StringUtil.urlToString(url);
 			Pattern pa = Pattern
 					.compile("<span id='m_(blocker|critical|major|minor|info)_violations'.*>(.*)</span>.*</td>\\s<td>\\s<span class='var(.*)'><b>(.*)</b></span></td>");
 			Matcher ma = pa.matcher(content);
 
 			while (ma.find()) {
 				map.put(ma.group(1), ma.group(2).replaceAll(",", "") + " " + ma.group(4).replaceAll(",", ""));
 			}
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 		return map;
 	}
 
 	public void retrieveRecentChange(Project root, Collection<Violation> violations, int period,
 			String violationPriority) {
 		String urlStr = VIOLATION_DRILLDOWN.replaceFirst("::portalURL", this.portalURL)
 				.replaceFirst("::projectId", String.valueOf(root.getId()))
 				.replaceFirst("::periodPhase", String.valueOf(period))
 				.replaceFirst("::priority", violationPriority.toUpperCase());
		String regex = "<td>\\s<a href=\"(.*)\" title=\"\\w*:\\s(.*)\">(.*)</a>\\s*</td>\\s<td class=\"right\" nowrap=\"nowrap\">\\s*<span><span class=.*>(\\+|-)(\\d+)</span></span>\\s</td>";
 
 		Matcher ma = null;
 		try {
 			URL url = new URL(urlStr);
 			String content = StringUtil.urlToString(url);
 			Pattern pa = Pattern.compile(regex);
 			ma = pa.matcher(content);
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 
 		while (ma.find()) {
 			Violation violation = new Violation();
 			violations.add(violation);
 			Collection<Project> projects = new ArrayList<Project>();
 			violation.setProjects(projects);
 			Rule rule = ruleDao.getRuleByPluginRuleKey(ma.group(2));
 			violation.setRule(rule);
 
 			if ("+".equals(ma.group(4))) {
 				violation.setAscend(true);
 			} else {
 				violation.setAscend(false);
 			}
 			violation.setDelta(Integer.valueOf(ma.group(5)));
 
 			try {
 				URL portal = new URL(portalURL);
 				int portNum = portal.getPort();
 				String tempUrlStr = null;
 				if (portNum == 80) {
 					tempUrlStr = "http://" + portal.getHost();
 				} else {
 					tempUrlStr = "http://" + portal.getHost() + ":" + portNum;
 				}
 				URL url = new URL(tempUrlStr + ma.group(1));
 				String eachViolation = StringUtil.urlToString(url);
 				Pattern pa2 = Pattern
						.compile("<img.*src=\".*/images/q/CLA.png\\?\\d+\".*/>.*\\s*<a href=\"#\" onclick=.*\\s*alt=\"(.*)\" title=\"(.*)\">(.*)</a>\\s*</td>\\s*<td class=\"right.*\" nowrap>\\s*<span class=.*>(\\+|-)(\\d+)</span>\\s*.*\\s*</td>");
 				Matcher ma2 = pa2.matcher(eachViolation);
 				while (ma2.find()) {
 					Project proj = projectDao.getProject(root.getKee() + ":" + ma2.group(1));
 					proj.setViolationDelta(Integer.valueOf(ma2.group(5)));
 					projects.add(proj);
 				}
 			} catch (MalformedURLException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public ProjectAccessor getProjectDao() {
 		return projectDao;
 	}
 
 	public void setProjectDao(ProjectAccessor projectDao) {
 		this.projectDao = projectDao;
 	}
 
 	public RuleAccessor getRuleDao() {
 		return ruleDao;
 	}
 
 	public void setRuleDao(RuleAccessor ruleDao) {
 		this.ruleDao = ruleDao;
 	}
 
 }
