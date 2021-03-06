 /*******************************************************************************
  * Copyright (c) 2010 Tasktop Technologies and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Tasktop Technologies - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.mylyn.bugzilla.tests.ui;
 
 import junit.framework.TestCase;
 
 import org.eclipse.jface.text.Region;
 import org.eclipse.jface.text.hyperlink.IHyperlink;
 import org.eclipse.mylyn.internal.bugzilla.core.BugzillaCorePlugin;
 import org.eclipse.mylyn.internal.bugzilla.ui.TaskAttachmentHyperlink;
 import org.eclipse.mylyn.internal.bugzilla.ui.tasklist.BugzillaConnectorUi;
 import org.eclipse.mylyn.internal.tasks.core.TaskTask;
 import org.eclipse.mylyn.tasks.core.TaskRepository;
 import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
 import org.eclipse.mylyn.tasks.ui.TaskHyperlink;
 
 /**
  * @author Steffen Pingel
  */
 public class BugzillaHyperlinkDetectorTest extends TestCase {
 
 	private BugzillaConnectorUi connector;
 
 	private TaskRepository repository;
 
 	private TaskTask task;
 
 	private TaskAttachmentHyperlink alink(int offset, int length, String attachmentId) {
 		TaskAttachmentHyperlink link = new TaskAttachmentHyperlink(new Region(offset, length), repository, attachmentId);
 		return link;
 	}
 
 	private void assertHyperlinks(String string, IHyperlink... expected) {
 		IHyperlink[] links = connector.findHyperlinks(repository, task, string, -1, 0);
 		if (expected.length == 0) {
 			assertNull(links);
 			return;
 		}
 		assertNotNull("Expected hyperlinks in " + string, links);
 		assertEquals(expected.length, links.length);
 		for (int i = 0; i < links.length; i++) {
 			assertEquals(expected[i], links[i]);
 		}
 	}
 
 	private TaskHyperlink link(int offset, int length, String taskId) {
 		return link(offset, length, taskId, null);
 	}
 
 	private TaskHyperlink link(int offset, int length, String taskId, String commentId) {
 		TaskHyperlink link = new TaskHyperlink(new Region(offset, length), repository, taskId);
 		if (commentId != null) {
 			link.setSelection(TaskAttribute.PREFIX_COMMENT + commentId);
 		}
 		return link;
 	}
 
 	@Override
 	protected void setUp() throws Exception {
 		repository = new TaskRepository(BugzillaCorePlugin.CONNECTOR_KIND, "http://localhost");
 		task = new TaskTask(BugzillaCorePlugin.CONNECTOR_KIND, "http://localhost", "123");
 		connector = new BugzillaConnectorUi();
 	}
 
 	public void testFindHyperlinksAttachment() {
 		assertHyperlinks("attachment 123", alink(0, 14, "123"));
 		assertHyperlinks("attachment  123", alink(0, 15, "123"));
 		assertHyperlinks("attachment  # 123", alink(0, 17, "123"));
 		assertHyperlinks("attachment#1", alink(0, 12, "1"));
 		assertHyperlinks("attachment (id=123)", alink(0, 19, "123"));
 		assertHyperlinks("Created attachment 123", alink(0, 22, "123"));
 		assertHyperlinks("Created an attachment 123", alink(0, 25, "123"));
 		assertHyperlinks("Created an attachment (id=123)", alink(0, 30, "123"));
 	}
 
 	public void testFindHyperlinksBug() {
 		assertHyperlinks("bug123", link(0, 6, "123"));
 		assertHyperlinks("bug 123", link(0, 7, "123"));
 		assertHyperlinks("bug  123", link(0, 8, "123"));
 		assertHyperlinks("bug#123", link(0, 7, "123"));
 		assertHyperlinks("bug  #  123", link(0, 11, "123"));
 	}
 
 	public void testFindHyperlinksTask() {
 		assertHyperlinks("task123", link(0, 7, "123"));
 	}
 
 	public void testFindHyperlinksDuplicateOf() {
		assertHyperlinks("duplicate of 123", link(0, 15, "123"));
 	}
 
 	public void testFindHyperlinksBugComment() {
 		assertHyperlinks("bug 123 comment 12", link(0, 18, "123", "12"));
 		assertHyperlinks("bug#123 comment 12", link(0, 18, "123", "12"));
 		assertHyperlinks("bug 123 comment#12", link(0, 18, "123", "12"));
 		assertHyperlinks("bug#123 comment#12", link(0, 18, "123", "12"));
 		assertHyperlinks("bug  123  comment#  12", link(0, 22, "123", "12"));
 		assertHyperlinks("bug456comment#1", link(0, 15, "456", "1"));
 	}
 
 	public void testFindHyperlinksBugNoComment() {
 		assertHyperlinks("bug 123#c1", link(0, 7, "123"));
 		assertHyperlinks("bug 123#1", link(0, 7, "123"));
 		assertHyperlinks("bug#123#c1", link(0, 7, "123"));
 		assertHyperlinks("bug#123#1", link(0, 7, "123"));
 	}
 
 	public void testFindHyperlinksComment() {
 		assertHyperlinks("comment#12", link(0, 10, "123", "12"));
 		assertHyperlinks("comment  #12", link(0, 12, "123", "12"));
 		assertHyperlinks("comment 1", link(0, 9, "123", "1"));
 	}
 
 	public void testFindHyperlinksInline() {
 		assertHyperlinks("abc bug 123 def", link(4, 7, "123"));
 	}
 
 	public void testFindHyperlinksMultiple() {
 		assertHyperlinks("bug 456#comment#12", link(0, 7, "456"), link(8, 10, "123", "12"));
 		assertHyperlinks("bug 123             bug 456", link(0, 7, "123"), link(20, 7, "456"));
 	}
 
 	public void testFindHyperlinksNoAttachment() {
 		assertHyperlinks("attachment");
 		assertHyperlinks("attachmen 123");
 		assertHyperlinks("attachment id");
 		assertHyperlinks("attachment id");
		assertHyperlinks("Create attachment 123");
 	}
 
 	public void testFindHyperlinksNoBug() {
 		assertHyperlinks("bu 123");
 		assertHyperlinks("bu# 123");
 		assertHyperlinks("bug");
 		assertHyperlinks("bugcomment");
 		assertHyperlinks("bug#comment");
 	}
 
 	public void testFindHyperlinksNoComment() {
 		assertHyperlinks("c 12");
 		assertHyperlinks("#c12");
 		assertHyperlinks("comment");
 	}
 }
