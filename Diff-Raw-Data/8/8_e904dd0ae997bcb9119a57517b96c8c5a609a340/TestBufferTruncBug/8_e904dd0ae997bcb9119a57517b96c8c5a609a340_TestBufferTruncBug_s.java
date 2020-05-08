 /*
  * Copyright (c)2004 Mark Logic Corporation
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  * The use of the Apache License does not indicate that this project is
  * affiliated with the Apache Software Foundation.
  */
 package com.marklogic.xqrunner;
 
 import junit.framework.TestCase;
 
 /**
  * Created by IntelliJ IDEA.
  * User: ron
  * Date: Feb 17, 2005
  * Time: 12:22:34 PM
  */
 public class TestBufferTruncBug extends TestCase
 {
 	private static final String DOCURI = "/testdocs/bigtext.xml";
 
 	private XQDataSource dataSource = null;
 	private XQRunner runner = null;
 	private String docText;
 
 	protected void setUp () throws Exception
 	{
 		super.setUp ();
 
 		dataSource = TestServerConfig.getDataSource();
 		runner = TestServerConfig.getRunner();
 	}
 
 	public void testElement() throws XQException
 	{
 		String text = genText (4096);
 		String query = "<foo>" + text + "</foo>";
 
 		XQResult result = runner.runQuery (dataSource.newQuery (query));
 
 		assertEquals (query, result.asString());
 	}
 
 	public void testText() throws XQException
 	{
 		String text = genText (4096);
 		String query = "<foo>" + text + "</foo>/text()";
 
 		XQResult result = runner.runQuery (dataSource.newQuery (query));
 
 		assertEquals (text, result.asString());
 	}
 
 	public void testElementStreaming() throws XQException
 	{
 		String text = genText (4096);
 		String query = "<foo>" + text + "</foo>";
 
 		XQResult result = runner.runQueryStreaming (dataSource.newQuery (query));
 
 		assertEquals (query, result.asString());
 	}
 
 	public void testTextStreaming() throws XQException
 	{
 		String text = genText (4096);
 		String query = "<foo>" + text + "</foo>/text()";
 
 		XQResult result = runner.runQueryStreaming (dataSource.newQuery (query));
 
 		assertEquals (text, result.asString());
 	}
 
 	public void testDocStreaming() throws XQException
 	{
 		docText = genText (4096);
 
 		String query = "xdmp:document-insert (\"" + DOCURI + "\", " +
 			"<root><foo>" + docText + "</foo></root>)";
 
 		runner.runQuery (dataSource.newQuery (query));
 
 		query = "doc(\"" + DOCURI + "\")/root/foo/text()";
 
 		XQResult result = runner.runQueryStreaming (dataSource.newQuery (query));
 
 		assertEquals (docText, result.asString());
 
		runner.runQuery (dataSource.newQuery ("xdmp:document-delete (\"" + DOCURI + "\""));
 	}
 
 	public void testDoc() throws XQException
 	{
 		docText = genText (4096);
 
 		String query = "xdmp:document-insert (\"" + DOCURI + "\", " +
 			"<root><foo>" + docText + "</foo></root>)";
 
 		runner.runQuery (dataSource.newQuery (query));
 
 		query = "fn:string (doc(\"" + DOCURI + "\")/root/foo)";
 
 		XQResult result = runner.runQuery (dataSource.newQuery (query));
 
 		assertEquals (docText, result.asString());
 
		runner.runQuery (dataSource.newQuery ("xdmp:document-delete (\"" + DOCURI + "\""));
 	}
 
 	public void testDocTextLength() throws XQException
 	{
 		docText = genText (4096);
 
 		String query = "xdmp:document-insert (\"" + DOCURI + "\", " +
 			"<root><foo>" + docText + "</foo></root>)";
 
 		runner.runQuery (dataSource.newQuery (query));
 
 		query = "fn:string (doc(\"" + DOCURI + "\")/root/foo)";
 
 		XQResult result = runner.runQuery (dataSource.newQuery (query));
 
 		assertEquals (docText.length(), result.asString().length());
 
		runner.runQuery (dataSource.newQuery ("xdmp:document-delete (\"" + DOCURI + "\""));
 	}
 
 	private String genText (int size)
 	{
 		char [] digits = "0123456789".toCharArray();
 		StringBuffer sb = new StringBuffer (size);
 
 		for (int i = 0; i < size; i++) {
 			sb.append (digits [i % 10]);
 		}
 
 		return (sb.toString());
 	}
 }
