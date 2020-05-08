 package org.hackystat.projectbrowser.page.projects;
 
 import java.util.ArrayList;
 import java.util.List;
 import org.apache.wicket.util.tester.WicketTester;
 import org.hackystat.projectbrowser.ProjectBrowserApplication;
 import org.hackystat.projectbrowser.ProjectBrowserSession;
 import org.hackystat.projectbrowser.test.ProjectBrowserTestHelper;
 import org.hackystat.sensorbase.resource.projects.jaxb.Property;
 import org.junit.Assert;
 import org.junit.Test;
 
 /**
  * Tests the Projects Model.
  * 
  * @author Randy Cox
  */
 public class TestProjectsModel extends ProjectBrowserTestHelper {
 
   /**
    * Test add prop uri row.
    */
   @Test
   public void testAddPropUriRow() { // NOPMD WicketTester has its own assert classes.
     new WicketTester(new ProjectBrowserApplication(getTestProperties()));
     ProjectsSession session = ProjectBrowserSession.get().getProjectsSession();
     ProjectsModel model = session.getProjectsModel();
 
     model.createProject();
     int count = model.getPropUriRowsView().size();
     model.addPropUriRow();
     Assert.assertEquals("addPropUriRow should add one row.", count + 1, model.getPropUriRowsView()
         .size());
   }
 
   /**
    * Test get extra uri rows.
    */
   @Test
   public void testGetExtraUriRows() { // NOPMD WicketTester has its own assert classes.
     new WicketTester(new ProjectBrowserApplication(getTestProperties()));
     ProjectsSession session = ProjectBrowserSession.get().getProjectsSession();
     ProjectsModel model = session.getProjectsModel();
 
     int testValue = 11;
     model.setExtraPropertyUriRows(testValue);
     Assert.assertEquals("getExtraUriRows get/set should be equal.", testValue, model
         .getExtraPropertyUriRows());
   }
 
   /**
    * Test uri pattern access.
    */
   @Test
   public void testUriRowAccess() { // NOPMD WicketTester has its own assert classes.
     List<String> list = new ArrayList<String>();
     list.add("URI_1");
     list.add("URI_2");
 
     new WicketTester(new ProjectBrowserApplication(getTestProperties()));
     ProjectsSession session = ProjectBrowserSession.get().getProjectsSession();
     ProjectsModel model = session.getProjectsModel();
     model.createProject();
     model.setProjectUriPatterns(list);
     String result = model.getProjectUriCommaStr();
     Assert.assertEquals("Uri comma str", "URI_1, URI_2", result);
   }
 
   /**
    * Test remove bold.
    */
   @Test
   public void testRemoveBold() { // NOPMD Wicket has its own asserts
     new WicketTester(new ProjectBrowserApplication(getTestProperties()));
     ProjectsSession session = ProjectBrowserSession.get().getProjectsSession();
     ProjectsModel model = session.getProjectsModel();
     String line = "test line";
     String boldLine = "<b>" + line + "</b>";
     Assert.assertEquals(line, model.removeBold(boldLine));
   }
 
   /**
    * Test remove bold.
    */
   @Test
   public void testProjectMembers() { // NOPMD Wicket has its own asserts
     new WicketTester(new ProjectBrowserApplication(getTestProperties()));
     ProjectsSession session = ProjectBrowserSession.get().getProjectsSession();
     ProjectsModel model = session.getProjectsModel();
     model.createProject();
 
     String member1 = "member1";
     String member2 = "member2";
     String member3 = "member3";
 
     List<String> members = new ArrayList<String>();
     members.add(member1);
     members.add(member2);
     members.add(member3);
     model.setProjectMembers(members);
 
     Assert.assertEquals(3, model.getProjectMembers().size());
     Assert.assertEquals(member1, model.getProjectMembers().get(0));
     Assert.assertEquals(member2, model.getProjectMembers().get(1));
     Assert.assertEquals(member3, model.getProjectMembers().get(2));
 
     members.remove(1);
 
     model.removeMembers(members);
     Assert.assertEquals(1, model.getProjectMembers().size());
     Assert.assertEquals(member2, model.getProjectMembers().get(0));
 
     Assert.assertEquals(member2, model.getProjectMembersStr());
     model.setProjectMembersStr("one,two,three");
     Assert.assertEquals("one, two, three", model.getProjectMembersStr());
   }
 
   /**
    * Test project access.
    */
   @Test
   public void testProjectAccess() { // NOPMD Wicket has its own asserts
     new WicketTester(new ProjectBrowserApplication(getTestProperties()));
     ProjectsSession session = ProjectBrowserSession.get().getProjectsSession();
     ProjectsModel model = session.getProjectsModel();
     model.createProject();
     String name = "name";
     String owner = "owner";
     String desc = "desc";
     model.setProjectName(name);
     model.setProjectOwner(owner);
     model.setProjectDesc(desc);
     String start = model.getProjectStartDate().toString();
     String end = model.getProjectEndDate().toString();
     String expected = "Project = \n  name = name\n  owner = owner\n  desc = desc\n" + "  start = "
         + start + "\n  end = " + end + "\n  members = \n  invitations = \n"
         + "  spectators = \n  properties = \n  uris = *\n";
     Assert.assertEquals(expected, model.getProjectStr());
   }
 
   /**
    * Test project properites access.
    */
   @Test
   public void testProjectProperiesAccess() { // NOPMD Wicket has its own asserts
     new WicketTester(new ProjectBrowserApplication(getTestProperties()));
     ProjectsSession session = ProjectBrowserSession.get().getProjectsSession();
     ProjectsModel model = session.getProjectsModel();
     model.createProject();
 
     List<Property> items = new ArrayList<Property>();
     Property one = new Property();
     one.setKey("key1");
     one.setValue("value1");
     Property two = new Property();
     two.setKey("key2");
     two.setValue("value2");
     Property three = new Property();
     three.setKey("key3");
     three.setValue("value3");
     items.add(one);
     items.add(two);
     items.add(three);
     model.setProjectProperties(items);
     String expected = "key1=value1\nkey2=value2\nkey3=value3";
     Assert.assertEquals(expected, model.getProjectPropertiesStr());
   }
 
   /**
    * Test parsing of member comma or cr delimited string.
    */
   @Test
   public void testParseMembersStr() { // NOPMD Wicket has its own asserts
     String one = "one";
     String two = "two";
     String three = "three";
     String comma = ",";
     String cr = "\n";
     String testCommaStr = one + comma + two + comma + " " + three;
     String testCrStr = one + cr + two + cr + " " + three;
     ProjectsModel model = new ProjectsModel();
 
     List<String> result;
     result = model.parseMembersStr(testCommaStr);
     Assert.assertEquals(one, result.get(0));
     Assert.assertEquals(two, result.get(1));
     Assert.assertEquals(three, result.get(2));
 
     result = model.parseMembersStr(testCrStr);
     Assert.assertEquals(one, result.get(0));
     Assert.assertEquals(two, result.get(1));
     Assert.assertEquals(three, result.get(2));
   }
 }
