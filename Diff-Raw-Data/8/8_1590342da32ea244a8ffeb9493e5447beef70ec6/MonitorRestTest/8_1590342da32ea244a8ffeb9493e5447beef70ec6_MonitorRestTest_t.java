 /**
  * Copyright (c) 2010 Pyxis Technologies inc.
  *
  * This is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA,
  * or see the FSF site: http://www.fsf.org.
  */
 package it.com.pyxis.jira.monitoring.rest;
 
 import java.util.List;
 import javax.ws.rs.core.MediaType;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.atlassian.jira.functest.framework.FuncTestCase;
 import com.pyxis.jira.monitoring.rest.RestUserIssueActivity;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.GenericType;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.*;
 
 public class MonitorRestTest
 		extends FuncTestCase {
 
 	private static final long REST1_ISSUE_ID = 10030;
 	private static final long REST2_ISSUE_ID = 10040;
 
 	private Client client = Client.create();
 	private WebResource service;
 
 	@Before
 	protected void setUpTest() {
 //		client.addFilter(new LoggingFilter());
 		client.addFilter(new HTTPBasicAuthFilter("admin", "admin"));
 		service = client.resource("http://localhost:2990/jira/rest/monitor/1.0");
 
 		administration.restoreData("it-MonitorRestTest.xml");
 	}
 
 	@Test
 	public void testCanFoundActivityOfOurselfOnIssue() {
 
 		List<RestUserIssueActivity> actual = getActivities(REST1_ISSUE_ID);
 		assertThat(actual.size(), is(equalTo(0)));
 
 		navigation.issue().viewIssue("REST-1");
 		assertions.assertNodeByIdExists("monitor_activity_admin");
 
 		actual = getActivities(REST1_ISSUE_ID);
 		assertThat(actual.size(), is(equalTo(1)));
 
 		RestUserIssueActivity activity = actual.get(0);
 		assertThat(activity.getIssueId(), is(equalTo(REST1_ISSUE_ID)));
 		assertThat(activity.getName(), is(equalTo(ADMIN)));
 	}
 
 	@Test
 	public void testDeletingAnIssue() {
 
 		navigation.issue().viewIssue("REST-2");
 
		List<RestUserIssueActivity> actual = getActivities(REST2_ISSUE_ID);
		assertThat(actual.size(), is(greaterThanOrEqualTo(1)));
 
 		navigation.issue().deleteIssue("REST-2");
 
 		actual = getActivities(REST2_ISSUE_ID);
 		assertThat(actual.size(), is(equalTo(0)));
 	}
 
 	private List<RestUserIssueActivity> getActivities(long id) {
 
 		return service.path("users")
 				.queryParam("issueId", String.valueOf(id))
 				.accept(MediaType.APPLICATION_XML_TYPE)
 				.get(new GenericType<List<RestUserIssueActivity>>() {
 				});
 	}
 }
