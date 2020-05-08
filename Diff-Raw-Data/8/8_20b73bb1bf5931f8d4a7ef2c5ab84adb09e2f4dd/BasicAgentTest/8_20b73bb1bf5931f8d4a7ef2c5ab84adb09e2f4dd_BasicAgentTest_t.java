 /* BasicAgentTest.java
 
 	Purpose:
 		
 	Description:
 		
 	History:
 		2012/3/22 Created by dennis
 
 Copyright (C) 2011 Potix Corporation. All Rights Reserved.
 */
 package org.zkoss.zats.testcase;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Stack;
 import java.util.logging.Logger;
 
 import org.eclipse.jetty.util.TypeUtil;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Assert;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.zkoss.zats.mimic.AgentException;
 import org.zkoss.zats.mimic.ComponentAgent;
 import org.zkoss.zats.mimic.DesktopAgent;
 import org.zkoss.zats.mimic.Resource;
 import org.zkoss.zats.mimic.Zats;
 import org.zkoss.zats.mimic.impl.Util;
 import org.zkoss.zats.mimic.impl.operation.SwitchedSortAgentImpl;
 import org.zkoss.zats.mimic.operation.BookmarkAgent;
 import org.zkoss.zats.mimic.operation.CheckAgent;
 import org.zkoss.zats.mimic.operation.ClickAgent;
 import org.zkoss.zats.mimic.operation.CloseAgent;
 import org.zkoss.zats.mimic.operation.DragAgent;
 import org.zkoss.zats.mimic.operation.FocusAgent;
 import org.zkoss.zats.mimic.operation.GroupAgent;
 import org.zkoss.zats.mimic.operation.HoverAgent;
 import org.zkoss.zats.mimic.operation.InputAgent;
 import org.zkoss.zats.mimic.operation.KeyStrokeAgent;
 import org.zkoss.zats.mimic.operation.MoveAgent;
 import org.zkoss.zats.mimic.operation.MultipleSelectAgent;
 import org.zkoss.zats.mimic.operation.OpenAgent;
 import org.zkoss.zats.mimic.operation.PagingAgent;
 import org.zkoss.zats.mimic.operation.RenderAgent;
 import org.zkoss.zats.mimic.operation.SelectAgent;
 import org.zkoss.zats.mimic.operation.SizeAgent;
 import org.zkoss.zats.mimic.operation.SortAgent;
 import org.zkoss.zats.mimic.operation.UploadAgent;
 import org.zkoss.zk.ui.AbstractComponent;
 import org.zkoss.zk.ui.Component;
 import org.zkoss.zk.ui.Desktop;
 import org.zkoss.zk.ui.HtmlBasedComponent;
 import org.zkoss.zk.ui.event.Events;
 import org.zkoss.zul.Button;
 import org.zkoss.zul.Label;
 import org.zkoss.zul.Listbox;
 import org.zkoss.zul.Listheader;
 import org.zkoss.zul.Listitem;
 import org.zkoss.zul.Paging;
 import org.zkoss.zul.Tree;
 import org.zkoss.zul.Treecol;
 import org.zkoss.zul.Treeitem;
 import org.zkoss.zul.Vbox;
 
 /**
  * @author dennis
  *
  */
 public class BasicAgentTest {
 
 	private static Logger logger = Logger.getLogger(BasicAgentTest.class.getName());
 	
 	private static final String[] componentNames = { "a", "applet", "button", "captcha", "fileupload", "fisheye", "fisheyebar", "html",
 			"include", "image", "imagemap", "label", "menu", "menubar", "menuitem", "menupopup", "menuseparator",
 			"popup", "progressmeter", "separator", "space", "toolbar", "toolbarbutton", "bandbox", "colorbox",
 			"combobox", "comboitem", "datebox", "decimalbox", "doublebox", "doublespinner", "intbox", "longbox",
 			"spinner", "textbox", "timebox", "checkbox", "radio", "radiogroup", "slider", "caption", "div",
 			"groupbox", "panel", "span", "tabbox", "tab", "window", "grid", "detail", "group", "listbox",
 			"listitem", "listgroup", "tree", "treeitem" };
 	@BeforeClass
 	public static void init()
 	{
 		Zats.init(".");
 	}
 
 	@AfterClass
 	public static void end()
 	{
 		Zats.end();
 	}
 
 	@After
 	public void after()
 	{
 		Zats.cleanup();
 	}
 	
 	
 	@Test
 	public void testKeyStrokeAgent(){
 		DesktopAgent desktopAgent = Zats.newClient().connect("/~./basic/keystroke.zul");
 		
 		ComponentAgent inp1 = desktopAgent.query("#inp1");
 		ComponentAgent inp2 = desktopAgent.query("#inp2");
 		ComponentAgent l1 = desktopAgent.query("#l1");
 		
 		Assert.assertEquals("", l1.as(Label.class).getValue());
 		
 		inp1.stroke("#enter");
 		Assert.assertEquals("ENTER key is pressed", l1.as(Label.class).getValue());
 		
 		inp1.stroke("#esc");
 		Assert.assertEquals("ESC key is pressed", l1.as(Label.class).getValue());
 		
 		inp1.stroke("^a");
 		Assert.assertEquals("Ctrl+A is pressed,alt:false,ctrl:true,shift:false", l1.as(Label.class).getValue());
 		
 		inp1.stroke("@b");
 		Assert.assertEquals("Alt+B is pressed,alt:true,ctrl:false,shift:false", l1.as(Label.class).getValue());
 		
 		inp1.stroke("#f8");
 		Assert.assertEquals("F8 is pressed,alt:false,ctrl:false,shift:false", l1.as(Label.class).getValue());
 		
 		
 		inp2.stroke("#right");
 		Assert.assertEquals("keyCode:39 is pressed,alt:false,ctrl:false,shift:false", l1.as(Label.class).getValue());
 		
 		inp2.as(KeyStrokeAgent.class).stroke("$#left");
 		Assert.assertEquals("keyCode:37 is pressed,alt:false,ctrl:false,shift:true", l1.as(Label.class).getValue());
 		
 		try{
 			inp1.as(KeyStrokeAgent.class).stroke("^a#right");//2 key
 			Assert.fail("should not go here");
 		}catch(AgentException x){}
 		
 		try{
 			inp1.as(KeyStrokeAgent.class).stroke("^");//no keycode
 			Assert.fail("should not go here");
 		}catch(AgentException x){}
 		
 	}
 	
 	@Test
 	public void testInputAgent1(){
 		DesktopAgent desktopAgent = Zats.newClient().connect("/~./basic/type1.zul");
 		
 		ComponentAgent l = desktopAgent.query("#l1");
 		ComponentAgent inp = desktopAgent.query("#inp1");
 		
 		final String TEXT_4_SELECTION="ABCDE";
 		//bandbox
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		
 		inp.type("A");
 		Assert.assertEquals("A",l.as(Label.class).getValue());
 		
 		inp.type("");
 		Assert.assertEquals("A",l.as(Label.class).getValue());
 		
 		inp.type(TEXT_4_SELECTION);
 		Assert.assertEquals(TEXT_4_SELECTION,l.as(Label.class).getValue());
 		
 		inp.as(InputAgent.class).select(1, 2);
 		Assert.assertEquals(TEXT_4_SELECTION.substring(1, 2),l.as(Label.class).getValue());
 		
 		//combobox
 		l = desktopAgent.query("#l2");
 		inp = desktopAgent.query("#inp2");
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		inp.type("C");
 		Assert.assertEquals("C",l.as(Label.class).getValue());
 		inp.type("");
 		Assert.assertEquals("C",l.as(Label.class).getValue());
 		inp.type(TEXT_4_SELECTION);
 		Assert.assertEquals(TEXT_4_SELECTION,l.as(Label.class).getValue());
 		
 		inp.as(InputAgent.class).select(1, 2);
 		Assert.assertEquals(TEXT_4_SELECTION.substring(1, 2),l.as(Label.class).getValue());
 		
 		//textbox
 		l = desktopAgent.query("#l10");
 		inp = desktopAgent.query("#inp10");
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		inp.type("E");
 		Assert.assertEquals("E",l.as(Label.class).getValue());
 		inp.type("");
 		Assert.assertEquals("E",l.as(Label.class).getValue());
 		inp.type(TEXT_4_SELECTION);
 		Assert.assertEquals(TEXT_4_SELECTION,l.as(Label.class).getValue());
 		
 		inp.as(InputAgent.class).select(1, 2);
 		Assert.assertEquals(TEXT_4_SELECTION.substring(1, 2),l.as(Label.class).getValue());
 		
 		final String DIGIT_4_SELECTION="12345";
 		
 		//decimalbox
 		l = desktopAgent.query("#l4");
 		inp = desktopAgent.query("#inp4");
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		inp.type("1");
 		Assert.assertEquals("1.0",l.as(Label.class).getValue());
 		inp.type("-1");
 		Assert.assertEquals("1.0",l.as(Label.class).getValue());
 
 		inp.type("2.33");
 		Assert.assertEquals("2.33",l.as(Label.class).getValue());
 		
 		inp.type(DIGIT_4_SELECTION);
 		inp.as(InputAgent.class).select(1, 2);
 		Assert.assertEquals(DIGIT_4_SELECTION.substring(1, 2),l.as(Label.class).getValue());
 		
 		//doublebox
 		l = desktopAgent.query("#l5");
 		inp = desktopAgent.query("#inp5");
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		
 		inp.type("3");
 		Assert.assertEquals("3.0",l.as(Label.class).getValue());
 		inp.type("-3");
 		Assert.assertEquals("3.0",l.as(Label.class).getValue());
 		inp.type("4.33");
 		Assert.assertEquals("4.33",l.as(Label.class).getValue());
 		
 		inp.type(DIGIT_4_SELECTION);
 		inp.as(InputAgent.class).select(1, 2);
 		Assert.assertEquals(DIGIT_4_SELECTION.substring(1, 2),l.as(Label.class).getValue());
 		
 		//doublespinner
 		l = desktopAgent.query("#l6");
 		inp = desktopAgent.query("#inp6");
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		
 		inp.type("5");
 		Assert.assertEquals("5.0",l.as(Label.class).getValue());
 		inp.type("-5");
 		Assert.assertEquals("5.0",l.as(Label.class).getValue());
 		inp.type("6.33");
 		Assert.assertEquals("6.33",l.as(Label.class).getValue());
 		
 		inp.type(DIGIT_4_SELECTION);
 		inp.as(InputAgent.class).select(1, 2);
 		Assert.assertEquals(DIGIT_4_SELECTION.substring(1, 2),l.as(Label.class).getValue());
 		
 		//intbox
 		l = desktopAgent.query("#l7");
 		inp = desktopAgent.query("#inp7");
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		inp.type("7");
 		Assert.assertEquals("7",l.as(Label.class).getValue());
 		inp.type("-7");
 		Assert.assertEquals("7",l.as(Label.class).getValue());
 		inp.type("8");
 		Assert.assertEquals("8",l.as(Label.class).getValue());
 		
 		inp.type(DIGIT_4_SELECTION);
 		inp.as(InputAgent.class).select(1, 2);
 		Assert.assertEquals(DIGIT_4_SELECTION.substring(1, 2),l.as(Label.class).getValue());
 		
 		
 		//longbox
 		l = desktopAgent.query("#l8");
 		inp = desktopAgent.query("#inp8");
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		
 		inp.type("9");
 		Assert.assertEquals("9",l.as(Label.class).getValue());
 		inp.type("-9");
 		Assert.assertEquals("9",l.as(Label.class).getValue());
 		inp.type("10");
 		Assert.assertEquals("10",l.as(Label.class).getValue());
 		
 		inp.type(DIGIT_4_SELECTION);
 		inp.as(InputAgent.class).select(1, 2);
 		Assert.assertEquals(DIGIT_4_SELECTION.substring(1, 2),l.as(Label.class).getValue());
 		
 		
 		//spinner
 		l = desktopAgent.query("#l9");
 		inp = desktopAgent.query("#inp9");
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		inp.type("11");
 		Assert.assertEquals("11",l.as(Label.class).getValue());
 		inp.type("-11");
 		Assert.assertEquals("11",l.as(Label.class).getValue());
 		inp.type("12");
 		Assert.assertEquals("12",l.as(Label.class).getValue());
 		
 		inp.type(DIGIT_4_SELECTION);
 		inp.as(InputAgent.class).select(1, 2);
 		Assert.assertEquals(DIGIT_4_SELECTION.substring(1, 2),l.as(Label.class).getValue());
 		
 		//datebox
 		l = desktopAgent.query("#l3");
 		inp = desktopAgent.query("#inp3");
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		inp.type("20120223");
 		Assert.assertEquals("20120223",l.as(Label.class).getValue());
 		inp.type("20110101");
 		Assert.assertEquals("20120223",l.as(Label.class).getValue());
 		inp.type("20120320");
 		Assert.assertEquals("20120320",l.as(Label.class).getValue());
 		
 		inp.as(InputAgent.class).select(1, 2);
 		Assert.assertEquals("20120320".substring(1, 2),l.as(Label.class).getValue());
 		
 		
 		//timebox
 		l = desktopAgent.query("#l11");
 		inp = desktopAgent.query("#inp11");
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		inp.type("13:00");
 		Assert.assertEquals("13:00",l.as(Label.class).getValue());
 		inp.type("10:00");
 		Assert.assertEquals("13:00",l.as(Label.class).getValue());
 		inp.type("14:02");
 		Assert.assertEquals("14:02",l.as(Label.class).getValue());
 		
 		inp.as(InputAgent.class).select(1, 2);
 		Assert.assertEquals("14:02".substring(1, 2),l.as(Label.class).getValue());
 		
 		//colorbox
 		l = desktopAgent.query("#l12");
 		inp = desktopAgent.query("colorbox");
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		inp.type("#0000ff");
 		Assert.assertEquals("#0000ff",l.as(Label.class).getValue());
 		try{
 			inp.type("invalid value");
 			fail("Cannot accept invalid input");
 		}catch(AgentException ae){
 			Assert.assertEquals("#0000ff",l.as(Label.class).getValue());
 		}
 		try{
 			inp.as(InputAgent.class).typing("invalid value");
 			fail("Colorbox does not support typing");
 		}catch(AgentException ae){
 			Assert.assertEquals("#0000ff",l.as(Label.class).getValue());
 		}
 	}
 	
 	@Test
 	public void testInputAgent2(){
 		DesktopAgent desktopAgent = Zats.newClient().connect("/~./basic/type2.zul");
 		
 		ComponentAgent l = desktopAgent.query("#l1");
 		ComponentAgent inp = desktopAgent.query("#inp1");
 		//bandbox
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		
 		inp.type("A");
 		Assert.assertEquals("A",l.as(Label.class).getValue());
 		
 		inp.type("");
 		Assert.assertEquals("A",l.as(Label.class).getValue());
 		
 		inp.type("B");
 		Assert.assertEquals("B",l.as(Label.class).getValue());
 		
 		//combobox
 		l = desktopAgent.query("#l2");
 		inp = desktopAgent.query("#inp2");
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		inp.type("C");
 		Assert.assertEquals("C",l.as(Label.class).getValue());
 		inp.type("");
 		Assert.assertEquals("C",l.as(Label.class).getValue());
 		inp.type("D");
 		Assert.assertEquals("D",l.as(Label.class).getValue());
 		
 		
 		//textbox
 		l = desktopAgent.query("#l10");
 		inp = desktopAgent.query("#inp10");
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		inp.type("E");
 		Assert.assertEquals("E",l.as(Label.class).getValue());
 		inp.type("");
 		Assert.assertEquals("E",l.as(Label.class).getValue());
 		inp.type("F");
 		Assert.assertEquals("F",l.as(Label.class).getValue());
 		
 		//decimalbox
 		l = desktopAgent.query("#l4");
 		inp = desktopAgent.query("#inp4");
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		inp.type("1");
 		Assert.assertEquals("1.0",l.as(Label.class).getValue());
 		inp.type("-1");
 		Assert.assertEquals("1.0",l.as(Label.class).getValue());
 
 		inp.type("2,222.33");
 		Assert.assertEquals("2222.33",l.as(Label.class).getValue());
 		
 		//doublebox
 		l = desktopAgent.query("#l5");
 		inp = desktopAgent.query("#inp5");
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		
 		inp.type("3");
 		Assert.assertEquals("3.0",l.as(Label.class).getValue());
 		inp.type("-3");
 		Assert.assertEquals("3.0",l.as(Label.class).getValue());
 		inp.type("4,444.33");
 		Assert.assertEquals("4444.33",l.as(Label.class).getValue());
 		
 		//doublespinner
 		l = desktopAgent.query("#l6");
 		inp = desktopAgent.query("#inp6");
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		
 		inp.type("5");
 		Assert.assertEquals("5.0",l.as(Label.class).getValue());
 
 		inp.type("-5");
 		Assert.assertEquals("5.0",l.as(Label.class).getValue());
 
 		inp.type("6,666.33");
 		Assert.assertEquals("6666.33",l.as(Label.class).getValue());
 		
 		//intbox
 		l = desktopAgent.query("#l7");
 		inp = desktopAgent.query("#inp7");
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		inp.type("7");
 		Assert.assertEquals("7",l.as(Label.class).getValue());
 		inp.type("-7");
 		Assert.assertEquals("7",l.as(Label.class).getValue());
 		inp.type("8,888");
 		Assert.assertEquals("8888",l.as(Label.class).getValue());
 		
 		
 		
 		
 		//longbox
 		l = desktopAgent.query("#l8");
 		inp = desktopAgent.query("#inp8");
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		
 		inp.type("9");
 		Assert.assertEquals("9",l.as(Label.class).getValue());
 		inp.type("-9");
 		Assert.assertEquals("9",l.as(Label.class).getValue());
 		inp.type("1,110");
 		Assert.assertEquals("1110",l.as(Label.class).getValue());
 		
 		
 		
 		//spinner
 		l = desktopAgent.query("#l9");
 		inp = desktopAgent.query("#inp9");
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		inp.type("11");
 		Assert.assertEquals("11",l.as(Label.class).getValue());
 		inp.type("-11");
 		Assert.assertEquals("11",l.as(Label.class).getValue());
 		inp.type("1,112");
 		Assert.assertEquals("1112",l.as(Label.class).getValue());
 		
 		//datebox
 		l = desktopAgent.query("#l3");
 		inp = desktopAgent.query("#inp3");
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		inp.type("23022012");
 		Assert.assertEquals("20120223",l.as(Label.class).getValue());
 		inp.type("01012011");
 		Assert.assertEquals("20120223",l.as(Label.class).getValue());
 		inp.type("20032012");
 		Assert.assertEquals("20120320",l.as(Label.class).getValue());
 		
 		//timebox
 		l = desktopAgent.query("#l11");
 		inp = desktopAgent.query("#inp11");
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		inp.type("00:13");
 		Assert.assertEquals("13:00",l.as(Label.class).getValue());
 		inp.type("00:10");
 		Assert.assertEquals("13:00",l.as(Label.class).getValue());
 		inp.type("02:14");
 		Assert.assertEquals("14:02",l.as(Label.class).getValue());
 	}
 	
 	
 	@Test
 	public void testInputAgent3(){
 		DesktopAgent desktopAgent = Zats.newClient().connect("/~./basic/type3.zul");
 		
 		ComponentAgent l = desktopAgent.query("#l1");
 		ComponentAgent inp = desktopAgent.query("#inp1");
 		
 		//bandbox
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		
 		inp.type("A");
 		Assert.assertEquals("A",l.as(Label.class).getValue());
 		
 		inp.type(null);
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		
 		inp.type("A1");
 		Assert.assertEquals("A1",l.as(Label.class).getValue());
 		
 		inp.type("");
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		
 		inp.as(InputAgent.class).input("A2");
 		Assert.assertEquals("A2",l.as(Label.class).getValue());
 		inp.as(InputAgent.class).input(null);
 		Assert.assertEquals("",l.as(Label.class).getValue());
 
 		
 		//combobox
 		l = desktopAgent.query("#l2");
 		inp = desktopAgent.query("#inp2");
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		inp.type("C");
 		Assert.assertEquals("C",l.as(Label.class).getValue());
 		inp.type(null);
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		inp.type("C1");
 		Assert.assertEquals("C1",l.as(Label.class).getValue());
 		inp.type("");
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		
 		inp.as(InputAgent.class).input("C2");
 		Assert.assertEquals("C2",l.as(Label.class).getValue());
 		inp.as(InputAgent.class).input(null);
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		
 
 		
 		//textbox
 		l = desktopAgent.query("#l10");
 		inp = desktopAgent.query("#inp10");
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		inp.type("E");
 		Assert.assertEquals("E",l.as(Label.class).getValue());
 		inp.type(null);
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		inp.type("E1");
 		Assert.assertEquals("E1",l.as(Label.class).getValue());
 		inp.type("");
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		
 		inp.as(InputAgent.class).input("E2");
 		Assert.assertEquals("E2",l.as(Label.class).getValue());
 		inp.as(InputAgent.class).input(null);
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		
 		//decimalbox
 		l = desktopAgent.query("#l4");
 		inp = desktopAgent.query("#inp4");
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		inp.type("1");
 		Assert.assertEquals("1.0",l.as(Label.class).getValue());
 		inp.type(null);
 		Assert.assertEquals("null",l.as(Label.class).getValue());
 		inp.type("-1");
 		Assert.assertEquals("-1.0",l.as(Label.class).getValue());
 		inp.type("");
 		Assert.assertEquals("null",l.as(Label.class).getValue());
 		
 		inp.as(InputAgent.class).input(11);
 		Assert.assertEquals("11.0",l.as(Label.class).getValue());
 		inp.as(InputAgent.class).input(null);
 		Assert.assertEquals("null",l.as(Label.class).getValue());
 		
 		//doublebox
 		l = desktopAgent.query("#l5");
 		inp = desktopAgent.query("#inp5");
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		
 		inp.type("3");
 		Assert.assertEquals("3.0",l.as(Label.class).getValue());
 		inp.type(null);
 		Assert.assertEquals("null",l.as(Label.class).getValue());
 		inp.type("-3");
 		Assert.assertEquals("-3.0",l.as(Label.class).getValue());
 		inp.type("");
 		Assert.assertEquals("null",l.as(Label.class).getValue());
 		
 		inp.as(InputAgent.class).input(33.0);
 		Assert.assertEquals("33.0",l.as(Label.class).getValue());
 		inp.as(InputAgent.class).input(null);
 		Assert.assertEquals("null",l.as(Label.class).getValue());
 		
 		//doublespinner
 		l = desktopAgent.query("#l6");
 		inp = desktopAgent.query("#inp6");
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		
 		inp.type("5");
 		Assert.assertEquals("5.0",l.as(Label.class).getValue());
 		inp.type(null);
 		Assert.assertEquals("null",l.as(Label.class).getValue());
 		inp.type("-5");
 		Assert.assertEquals("-5.0",l.as(Label.class).getValue());
 		inp.type("");
 		Assert.assertEquals("null",l.as(Label.class).getValue());
 		
 		inp.as(InputAgent.class).input(55D);
 		Assert.assertEquals("55.0",l.as(Label.class).getValue());
 		inp.as(InputAgent.class).input(null);
 		Assert.assertEquals("null",l.as(Label.class).getValue());
 		
 		//intbox
 		l = desktopAgent.query("#l7");
 		inp = desktopAgent.query("#inp7");
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		inp.type("7");
 		Assert.assertEquals("7",l.as(Label.class).getValue());
 		inp.type(null);
 		Assert.assertEquals("null",l.as(Label.class).getValue());
 		inp.type("-7");
 		Assert.assertEquals("-7",l.as(Label.class).getValue());
 		inp.type("");
 		Assert.assertEquals("null",l.as(Label.class).getValue());
 		
 		inp.as(InputAgent.class).input(77);
 		Assert.assertEquals("77",l.as(Label.class).getValue());
 		inp.as(InputAgent.class).input(null);
 		Assert.assertEquals("null",l.as(Label.class).getValue());
 		
 		//longbox
 		l = desktopAgent.query("#l8");
 		inp = desktopAgent.query("#inp8");
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		
 		inp.type("9");
 		Assert.assertEquals("9",l.as(Label.class).getValue());
 		inp.type(null);
 		Assert.assertEquals("null",l.as(Label.class).getValue());
 		inp.type("-9");
 		Assert.assertEquals("-9",l.as(Label.class).getValue());
 		inp.type("");
 		Assert.assertEquals("null",l.as(Label.class).getValue());
 		
 		inp.as(InputAgent.class).input(99L);
 		Assert.assertEquals("99",l.as(Label.class).getValue());
 		inp.as(InputAgent.class).input(null);
 		Assert.assertEquals("null",l.as(Label.class).getValue());
 		
 		//spinner
 		l = desktopAgent.query("#l9");
 		inp = desktopAgent.query("#inp9");
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		inp.type("11");
 		Assert.assertEquals("11",l.as(Label.class).getValue());
 		
 		//zk 6 has bug, I comment it out untile bug fixed
 		//http://tracker.zkoss.org/browse/ZK-1117
 		if(Util.isZKVersion(5)){
 			inp.type(null);
 			Assert.assertEquals("null",l.as(Label.class).getValue());
 		}
 		inp.type("-11");
 		Assert.assertEquals("-11",l.as(Label.class).getValue());
 		if(Util.isZKVersion(5)){
 			inp.type("");
 			Assert.assertEquals("null",l.as(Label.class).getValue());
 		}
 		
 		inp.as(InputAgent.class).input(111);
 		Assert.assertEquals("111",l.as(Label.class).getValue());
 		if(Util.isZKVersion(5)){
 			inp.as(InputAgent.class).input(null);
 			Assert.assertEquals("null",l.as(Label.class).getValue());
 		}
 		
 		//datebox
 		l = desktopAgent.query("#l3");
 		inp = desktopAgent.query("#inp3");
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		inp.type("20120223");
 		Assert.assertEquals("20120223",l.as(Label.class).getValue());
 		inp.type(null);
 		Assert.assertEquals("null",l.as(Label.class).getValue());
 		inp.type("20110101");
 		Assert.assertEquals("20110101",l.as(Label.class).getValue());
 		inp.type("");
 		Assert.assertEquals("null",l.as(Label.class).getValue());
 		
 		Date d = new Date(2012-1900,0,1);
 		
 		inp.as(InputAgent.class).input(d);
 		Assert.assertEquals("20120101",l.as(Label.class).getValue());
 		inp.as(InputAgent.class).input(null);
 		Assert.assertEquals("null",l.as(Label.class).getValue());
 		
 		//timebox
 		l = desktopAgent.query("#l11");
 		inp = desktopAgent.query("#inp11");
 		Assert.assertEquals("",l.as(Label.class).getValue());
 		inp.type("13:00");
 		Assert.assertEquals("13:00",l.as(Label.class).getValue());
 		inp.type(null);
 		Assert.assertEquals("null",l.as(Label.class).getValue());
 		inp.type("10:00");
 		Assert.assertEquals("10:00",l.as(Label.class).getValue());
 		inp.type("");
 		Assert.assertEquals("null",l.as(Label.class).getValue());
 		
 		d = new Date(2012-1900,0,1,3,44);
 		
 		inp.as(InputAgent.class).input(d);
 		Assert.assertEquals("03:44",l.as(Label.class).getValue());
 		inp.as(InputAgent.class).input(null);
 		Assert.assertEquals("null",l.as(Label.class).getValue());
 	}
 	
 	@Test
 	public void testOpenAgentTree(){
 		DesktopAgent desktopAgent = Zats.newClient().connect("/~./basic/open-tree.zul");
 		
 		ComponentAgent tree = desktopAgent.query("#tree");
 		List<ComponentAgent> items = tree.queryAll("treeitem");
 		Assert.assertEquals(2, items.size());
 		
 		Stack<ComponentAgent> stack = new Stack<ComponentAgent>();
 		stack.addAll(items);
 		
 		while(!stack.empty()){
 			ComponentAgent item = stack.pop();
 			
 			if(item.query("treechildren")!=null){
 				Assert.assertFalse(item.as(Treeitem.class).isOpen());
 				items = item.query("treechildren").queryAll("treeitem");//the sub-treeitem.
 				Assert.assertEquals(0, items.size());
 				
 				item.as(OpenAgent.class).open(true);//trigger open to load the tree item.
 				
 				Assert.assertTrue(item.as(Treeitem.class).isOpen());
 				items = item.query("treechildren").queryAll("treeitem");//the sub-treeitem.
 				Assert.assertEquals(2, items.size());
 				for(ComponentAgent si:items){
 					stack.push(si);
 				}
 			}
 		}
 		
 		items = tree.queryAll("treeitem");
 		Assert.assertEquals(14, items.size());
 	}
 	
 	@Test
 	public void testFocusAgent() {
 		DesktopAgent desktopAgent = Zats.newClient().connect("/~./basic/focus.zul");
 		Label curr = desktopAgent.query("#current").as(Label.class);
 		Label lost = desktopAgent.query("#lost").as(Label.class);
 		assertTrue(curr.getValue().length() <= 0);
 		assertTrue(curr.getValue().length() <= 0);
 
 		for (int i = 1; i <= 17; ++i) {
 			ComponentAgent comp = desktopAgent.query("#c" + i);
 			comp.as(FocusAgent.class).focus();
 			String name = comp.as(AbstractComponent.class).getDefinition().getName();
 			assertEquals(name, curr.getValue());
 			comp.as(FocusAgent.class).blur();
 			assertEquals(name, lost.getValue());
 		}
 	}
 
 	@Test
 	public void testCheckAgent() {
 		DesktopAgent desktopAgent = Zats.newClient().connect("/~./basic/check.zul");
 
 		// validate msg
 		Label msg = desktopAgent.query("#msg").as(Label.class);
 		assertTrue(msg.getValue().length() <= 0);
 
 		// test checkbox and menuitem
 		String label = "";
 		for (int i = 1; i <= 6; ++i) {
 			desktopAgent.query("#c" + i).as(CheckAgent.class).check(true);
 			label += "c" + i + " ";
 			assertEquals(label, msg.getValue());
 		}
 		// test radiogroup
 		for (int i = 7; i <= 9; ++i) {
 			desktopAgent.query("#c" + i).as(CheckAgent.class).check(true);
 			assertEquals(label + "c" + i + " ", msg.getValue());
 		}
 	}
 
 	@Test
 	public void testClickAgent() {
 		DesktopAgent desktopAgent = Zats.newClient().connect("/~./basic/click.zul");
 		assertEquals("Hello World!", desktopAgent.query("#msg").as(Label.class).getValue());
 		desktopAgent.query("#btn").as(ClickAgent.class).click();
 		assertEquals("Welcome", desktopAgent.query("#msg").as(Label.class).getValue());
 	}
 	
 	@Test
 	public void testMultipleSelectAgent() {
 		DesktopAgent desktopAgent = Zats.newClient().connect("/~./basic/multiple-select.zul");
 
 		Label msg = desktopAgent.query("#msg").as(Label.class);
 		assertEquals("", msg.getValue());
 
 		ComponentAgent listbox = desktopAgent.query("#lb");
 		assertEquals(4, listbox.as(Listbox.class).getChildren().size()); // include header
 		List<ComponentAgent> items = listbox.queryAll("listitem");
 
 		// listbox multiple selection
 		items.get(0).as(MultipleSelectAgent.class).select();
 		assertEquals("[i0]", msg.getValue());
 		assertEquals(1, listbox.as(Listbox.class).getSelectedCount());
 		items.get(1).as(MultipleSelectAgent.class).select();
 		assertEquals("[i0, i1]", msg.getValue());
 		assertEquals(2, listbox.as(Listbox.class).getSelectedCount());
 		items.get(2).as(MultipleSelectAgent.class).select();
 		assertEquals("[i0, i1, i2]", msg.getValue());
 		assertEquals(3, listbox.as(Listbox.class).getSelectedCount());
 		items.get(1).as(MultipleSelectAgent.class).deselect();
 		assertEquals("[i0, i2]", msg.getValue());
 		assertEquals(2, listbox.as(Listbox.class).getSelectedCount());
 		items.get(0).as(MultipleSelectAgent.class).deselect();
 		assertEquals("[i2]", msg.getValue());
 		assertEquals(1, listbox.as(Listbox.class).getSelectedCount());
 		items.get(2).as(MultipleSelectAgent.class).deselect();
 		assertEquals("[]", msg.getValue());
 		assertEquals(0, listbox.as(Listbox.class).getSelectedCount());
 		items.get(2).as(MultipleSelectAgent.class).deselect(); // should happen nothing
 		assertEquals("[]", msg.getValue());
 		assertEquals(0, listbox.as(Listbox.class).getSelectedCount());
 		
 		// listbox single selection (extra test)
 		desktopAgent.query("#lbcb checkbox").as(CheckAgent.class).check(false);
 		String[] values = { "[i0]", "[i1]", "[i2]" };
 		for (int i = 0; i < 3; ++i) {
 			items.get(i).as(SelectAgent.class).select();
 			assertEquals(values[i], msg.getValue());
 		}
 		
 		// tree multiple selection
 		desktopAgent.query("#ti1").as(MultipleSelectAgent.class).select();
 		assertEquals("[ti1]", msg.getValue());
 		desktopAgent.query("#ti1-2").as(MultipleSelectAgent.class).select();
 		assertEquals("[ti1, ti1-2]", msg.getValue());
 		desktopAgent.query("#ti1-1").as(MultipleSelectAgent.class).select();
 		assertEquals("[ti1, ti1-1, ti1-2]", msg.getValue());
 		desktopAgent.query("#ti1-1").as(MultipleSelectAgent.class).select();
 		assertEquals("[ti1, ti1-1, ti1-2]", msg.getValue());
 		desktopAgent.query("#ti1").as(MultipleSelectAgent.class).deselect();
 		assertEquals("[ti1-1, ti1-2]", msg.getValue());
 		desktopAgent.query("#ti1-2").as(MultipleSelectAgent.class).deselect();
 		assertEquals("[ti1-1]", msg.getValue());
 		desktopAgent.query("#ti1-2").as(MultipleSelectAgent.class).deselect();
 		assertEquals("[ti1-1]", msg.getValue());
 		desktopAgent.query("#ti1-1").as(MultipleSelectAgent.class).deselect();
 		assertEquals("[]", msg.getValue());
 		
 		// tree multiple selection - single select at multiple selection mode
 		desktopAgent.query("#ti1-2").as(SelectAgent.class).select();
 		assertEquals("[ti1-2]", msg.getValue());
 		desktopAgent.query("#ti1-1").as(SelectAgent.class).select();
 		assertEquals("[ti1-1]", msg.getValue());
 		desktopAgent.query("#ti1").as(SelectAgent.class).select();
 		assertEquals("[ti1]", msg.getValue());
 
 		// tree multiple selection - with check mark
 		desktopAgent.queryAll("#tcb > checkbox").get(1).as(CheckAgent.class).check(true);
 		assertTrue(desktopAgent.query("#t").as(Tree.class).isCheckmark());
 
 		desktopAgent.query("#ti1").as(MultipleSelectAgent.class).select();
 		assertEquals("[ti1]", msg.getValue());
 		desktopAgent.query("#ti1-2").as(MultipleSelectAgent.class).select();
 		assertEquals("[ti1, ti1-2]", msg.getValue());
 		desktopAgent.query("#ti1-1").as(MultipleSelectAgent.class).select();
 		assertEquals("[ti1, ti1-1, ti1-2]", msg.getValue());
 		desktopAgent.query("#ti1-1").as(MultipleSelectAgent.class).select();
 		assertEquals("[ti1, ti1-1, ti1-2]", msg.getValue());
 		desktopAgent.query("#ti1").as(MultipleSelectAgent.class).deselect();
 		assertEquals("[ti1-1, ti1-2]", msg.getValue());
 		desktopAgent.query("#ti1-2").as(MultipleSelectAgent.class).deselect();
 		assertEquals("[ti1-1]", msg.getValue());
 		desktopAgent.query("#ti1-2").as(MultipleSelectAgent.class).deselect();
 		assertEquals("[ti1-1]", msg.getValue());
 		desktopAgent.query("#ti1-1").as(MultipleSelectAgent.class).deselect();
 		assertEquals("[]", msg.getValue());
 
 		// tree single selection (extra test)
 		desktopAgent.queryAll("#tcb > checkbox").get(0).as(CheckAgent.class).check(false);
 		assertFalse(desktopAgent.query("#t").as(Tree.class).isMultiple());
 
 		desktopAgent.query("#ti1-2").as(SelectAgent.class).select();
 		assertEquals("[ti1-2]", msg.getValue());
 		desktopAgent.query("#ti1-1").as(SelectAgent.class).select();
 		assertEquals("[ti1-1]", msg.getValue());
 		desktopAgent.query("#ti1").as(SelectAgent.class).select();
 		assertEquals("[ti1]", msg.getValue());
 
 		try {
 			desktopAgent.query("#ti1").as(MultipleSelectAgent.class).select();
 			fail();
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 		}
 	}
 	
 	@Test
 	public void testSelectAgent() {
 		DesktopAgent desktop = Zats.newClient().connect("/~./basic/select.zul");
 
 		Label selected = desktop.query("#selected").as(Label.class);
 		assertEquals("", selected.getValue());
 
 		// combobox
 		String[] labels = new String[] { "cbi1", "cbi2", "cbi3" };
 		List<ComponentAgent> cbitems = desktop.queryAll("#cb > comboitem");
 		assertEquals(labels.length, cbitems.size());
 		for (int i = 0; i < labels.length; ++i) {
 			cbitems.get(i).as(SelectAgent.class).select();
 			assertEquals(labels[i], selected.getValue());
 		}
 
 		// tabbox
 		labels = new String[] { "tb1.tab1", "tb1.tab2" };
 		List<ComponentAgent> tab = desktop.queryAll("#tb1 tab");
 		assertEquals(labels.length, tab.size());
 		for (int i = 0; i < labels.length; ++i) {
 			tab.get(i).as(SelectAgent.class).select();
 			assertEquals(labels[i], selected.getValue());
 		}
 
 		// tab
 		labels = new String[] { "tb2.tab1", "tb2.tab2" };
 		tab = desktop.queryAll("#tb2 tab");
 		assertEquals(labels.length, tab.size());
 		for (int i = 0; i < labels.length; ++i) {
 			tab.get(i).as(SelectAgent.class).select();
 			assertEquals(labels[i], selected.getValue());
 		}
 
 		// tree
 		labels = new String[] { "ti1", "ti1.1", "ti1.2" };
 		List<ComponentAgent> titems = desktop.queryAll("#t treeitem");
 		assertEquals(labels.length, titems.size());
 		for (int i = 0; i < labels.length; ++i) {
 			titems.get(i).as(SelectAgent.class).select();
 			assertEquals(labels[i], selected.getValue());
 		}
 	}
 	
 	@Test
 	public void testCloseAgent(){
 		DesktopAgent desktopAgent = Zats.newClient().connect("/~./basic/close.zul");
 		
 		ComponentAgent panel = desktopAgent.query("panel[title='closable']");
 		panel.as(CloseAgent.class).close();
 		Assert.assertNull(((Component)panel.getDelegatee()).getPage());
 		
 		ComponentAgent window = desktopAgent.query("window[title='closable']");
 		window.as(CloseAgent.class).close();
 		Assert.assertNull(((Component)window.getDelegatee()).getPage());
 		
 		ComponentAgent tab = desktopAgent.query("tab[label='closable']");
 		tab.as(CloseAgent.class).close();
 		Assert.assertNull(((Component)tab.getDelegatee()).getPage());
 		
 		// TODO close a closable=false component, it will still be closed. ignore this case for now.
 		//	panel = desktopAgent.query("panel[title='non-close']");
 		//	panel.as(CloseAgent.class).close();
 		//	Assert.assertNotNull(panel.getDelegatee().getPage());
 	}
 	
 	@Test
 	public void testOpenAgent(){
 		DesktopAgent desktop = Zats.newClient().connect("/~./basic/open.zul");
 
 		Label open = desktop.query("#open").as(Label.class);
 		Label close = desktop.query("#close").as(Label.class);
 		assertEquals("", open.getValue());
 		assertEquals("", close.getValue());
 
 		String status[] = { "", "" };
 		// bandbox
 		String id = "#aBandbox";
 		status[0] = id.substring(1);
 		desktop.query(id).as(OpenAgent.class).open(true);
 		assertEquals(status[0], open.getValue());
 		assertEquals(status[1], close.getValue());
 		status[1] = id.substring(1);
 		desktop.query(id).as(OpenAgent.class).open(false);
 		assertEquals(status[0], open.getValue());
 		assertEquals(status[1], close.getValue());
 
 		// combobox
 		id = "#aCombobox";
 		status[0] = id.substring(1);
 		desktop.query(id).as(OpenAgent.class).open(true);
 		assertEquals(status[0], open.getValue());
 		assertEquals(status[1], close.getValue());
 		status[1] = id.substring(1);
 		desktop.query(id).as(OpenAgent.class).open(false);
 		assertEquals(status[0], open.getValue());
 		assertEquals(status[1], close.getValue());
 
 		// groupbox
 		id = "#aGroupbox";
 		status[0] = id.substring(1);
 		desktop.query(id).as(OpenAgent.class).open(true);
 		assertEquals(status[0], open.getValue());
 		assertEquals(status[1], close.getValue());
 		status[1] = id.substring(1);
 		desktop.query(id).as(OpenAgent.class).open(false);
 		assertEquals(status[0], open.getValue());
 		assertEquals(status[1], close.getValue());
 
 		// detail
 		id = "#aDetail";
 		status[0] = id.substring(1);
 		desktop.query(id).as(OpenAgent.class).open(true);
 		assertEquals(status[0], open.getValue());
 		assertEquals(status[1], close.getValue());
 		status[1] = id.substring(1);
 		desktop.query(id).as(OpenAgent.class).open(false);
 		assertEquals(status[0], open.getValue());
 		assertEquals(status[1], close.getValue());
 
 		// group
 		id = "#aGroup";
 		status[0] = id.substring(1);
 		desktop.query(id).as(OpenAgent.class).open(true);
 		assertEquals(status[0], open.getValue());
 		assertEquals(status[1], close.getValue());
 		status[1] = id.substring(1);
 		desktop.query(id).as(OpenAgent.class).open(false);
 		assertEquals(status[0], open.getValue());
 		assertEquals(status[1], close.getValue());
 
 		// listgroup
 		id = "#aListgroup";
 		status[0] = id.substring(1);
 		desktop.query(id).as(OpenAgent.class).open(true);
 		assertEquals(status[0], open.getValue());
 		assertEquals(status[1], close.getValue());
 		status[1] = id.substring(1);
 		desktop.query(id).as(OpenAgent.class).open(false);
 		assertEquals(status[0], open.getValue());
 		assertEquals(status[1], close.getValue());
 
 		// treeitem
 		id = "#treeitem1";
 		status[0] = id.substring(1);
 		desktop.query(id).as(OpenAgent.class).open(true);
 		assertEquals(status[0], open.getValue());
 		assertEquals(status[1], close.getValue());
 		status[1] = id.substring(1);
 		desktop.query(id).as(OpenAgent.class).open(false);
 		assertEquals(status[0], open.getValue());
 		assertEquals(status[1], close.getValue());
 		id = "#treeitem1-2";
 		status[0] = id.substring(1);
 		desktop.query(id).as(OpenAgent.class).open(true);
 		assertEquals(status[0], open.getValue());
 		assertEquals(status[1], close.getValue());
 		status[1] = id.substring(1);
 		desktop.query(id).as(OpenAgent.class).open(false);
 		assertEquals(status[0], open.getValue());
 		assertEquals(status[1], close.getValue());
 		
 		//panel
 		id="panel";
 		desktop.query(id).as(OpenAgent.class).open(true);
 		assertEquals(id, open.getValue());
 		desktop.query(id).as(OpenAgent.class).open(false);
 		assertEquals(id, close.getValue());
 		
 		//window
 		id="window";
 		desktop.query(id).as(OpenAgent.class).open(true);
 		assertEquals(id, open.getValue());
 		desktop.query(id).as(OpenAgent.class).open(false);
 		assertEquals(id, close.getValue());
 		
 		//Center    
 		id="center";
 		desktop.query(id).as(OpenAgent.class).open(true);
 		assertEquals(id, open.getValue());
 		desktop.query(id).as(OpenAgent.class).open(false);
 		assertEquals(id, close.getValue());
 		
 		//East    
 		id="east";
 		desktop.query(id).as(OpenAgent.class).open(true);
 		assertEquals(id, open.getValue());
 		desktop.query(id).as(OpenAgent.class).open(false);
 		assertEquals(id, close.getValue());
 		
 		//North
 		id="north";
 		desktop.query(id).as(OpenAgent.class).open(true);
 		assertEquals(id, open.getValue());
 		desktop.query(id).as(OpenAgent.class).open(false);
 		assertEquals(id, close.getValue());
 		
 		//South
 		id="south";
 		desktop.query(id).as(OpenAgent.class).open(true);
 		assertEquals(id, open.getValue());
 		desktop.query(id).as(OpenAgent.class).open(false);
 		assertEquals(id, close.getValue());
 		
 		//West
 		id="west";
 		desktop.query(id).as(OpenAgent.class).open(true);
 		assertEquals(id, open.getValue());
 		desktop.query(id).as(OpenAgent.class).open(false);
 		assertEquals(id, close.getValue());
 		
 		//splitter
 		id="splitter";
 		desktop.query(id).as(OpenAgent.class).open(true);
 		assertEquals(id, open.getValue());
 		desktop.query(id).as(OpenAgent.class).open(false);
 		assertEquals(id, close.getValue());
 		
 		//popup
 		id="popup";
 		desktop.query(id).as(OpenAgent.class).open(true);
 		assertEquals(id, open.getValue());
 		desktop.query(id).as(OpenAgent.class).open(false);
 		assertEquals(id, close.getValue());
 	}
 	
 	@Test
 	public void testCKEditorInputAgent() {
 		DesktopAgent desktop = Zats.newClient().connect("/~./basic/type-ckeditor.zul");
 
 		Label eventName = desktop.query("#eventName").as(Label.class);
 		Label change = desktop.query("#change").as(Label.class);
 		Label changing = desktop.query("#changing").as(Label.class);
 		assertEquals("", eventName.getValue());
 		assertEquals("", change.getValue());
 		assertEquals("", changing.getValue());
 
 		desktop.query("#ck").as(InputAgent.class).typing("Hello");
 		assertEquals("onChanging", eventName.getValue());
 		assertEquals("", change.getValue());
 		assertEquals("Hello", changing.getValue());
 
 		desktop.query("#ck").as(InputAgent.class).type("Hello world");
 		assertEquals("onChange", eventName.getValue());
 		assertEquals("Hello world", change.getValue());
 		assertEquals("Hello", changing.getValue());
 	}
 	
 		
 	@Test
 	public void testClickAll() {
 		DesktopAgent desktop = Zats.newClient().connect("/~./basic/click-all.zul");
 
 		Label target = desktop.query("#target").as(Label.class);
 		Label event = desktop.query("#eventName").as(Label.class);
 		assertEquals("", target.getValue());
 		assertEquals("", event.getValue());
 		
 		ComponentAgent comps = desktop.query("#comps");
 		assertNotNull(comps);
 		
 		
 		for (String name : componentNames) {
 			ClickAgent agent = comps.query(name).as(ClickAgent.class);
 			agent.click();
 			assertEquals(name, target.getValue());
 			assertEquals(Events.ON_CLICK, event.getValue());
 			agent.doubleClick();
 			assertEquals(name, target.getValue());
 			assertEquals(Events.ON_DOUBLE_CLICK, event.getValue());
 			agent.rightClick();
 			assertEquals(name, target.getValue());
 			assertEquals(Events.ON_RIGHT_CLICK, event.getValue());
 		}
 	}
 	
 	@Test
 	public void testHover() {
 		DesktopAgent desktop = Zats.newClient().connect("/~./basic/click-all.zul");
 
 		Label target = desktop.query("#target").as(Label.class);
 		Label event = desktop.query("#eventName").as(Label.class);
 		assertEquals("", target.getValue());
 		assertEquals("", event.getValue());
 		
 		ComponentAgent comps = desktop.query("#comps");
 		assertNotNull(comps);
 		
 		
 		for (String name : componentNames) {
 			HoverAgent agent = comps.query(name).as(HoverAgent.class);
 			agent.moveOver();
 			assertEquals(name, target.getValue());
 			assertEquals(Events.ON_MOUSE_OVER, event.getValue());
 			agent.moveOut();
 			assertEquals(name, target.getValue());
 			assertEquals(Events.ON_MOUSE_OUT, event.getValue());
 		}
 	}
 	
 	/*
 	 * I found listbox and grid preload 20 items instead of 7 which is specified in configuration reference
 	 */
 	@Test
 	public void testRendererAgent() {
 		DesktopAgent desktop = Zats.newClient().connect("/~./basic/render.zul");
 
 		List<ComponentAgent> indexes = desktop.query("#index").queryAll("comboitem");
 		assertEquals(1000, indexes.size());
 		Label ic = desktop.query("#listitemContent").as(Label.class);
 		Label rc = desktop.query("#rowContent").as(Label.class);
 		assertEquals("", ic.getValue());
 		assertEquals("", rc.getValue());
 
 		int index = 0;
 		indexes.get(index).as(SelectAgent.class).select();
 		assertEquals("item" + index, ic.getValue());
 		assertEquals("item" + index, rc.getValue());
 
 		for (int i = 900; i <= 999; ++i) {
 			indexes.get(i).as(SelectAgent.class).select();
 			assertEquals(i + " doesn't render", ic.getValue());
 			assertEquals(i + " doesn't render", rc.getValue());
 		}
 
 		desktop.query("#listbox").as(RenderAgent.class).render(900, 949);
 		desktop.query("#grid").as(RenderAgent.class).render(900, 949);
 		for (int i = 900; i <= 949; ++i) {
 			indexes.get(i).as(SelectAgent.class).select();
 			assertEquals("item" + i, ic.getValue());
 			assertEquals("item" + i, rc.getValue());
 		}
 		for (int i = 950; i <= 999; ++i) {
 			indexes.get(i).as(SelectAgent.class).select();
 			assertEquals(i + " doesn't render", ic.getValue());
 			assertEquals(i + " doesn't render", rc.getValue());
 		}
 
 		desktop.query("#listbox").as(RenderAgent.class).render(0, 999);
 		desktop.query("#grid").as(RenderAgent.class).render(0, 999);
 		for (int i = 0; i <= 999; ++i) {
 			indexes.get(i).as(SelectAgent.class).select();
 			assertEquals("item" + i, ic.getValue());
 			assertEquals("item" + i, rc.getValue());
 		}
 	}
 	
 	@Test
 	public void testKeyStrokeAgentOnInputElements() {
 		// prepare all CtrlKey strings
 		char[] words = new char[26];
 		for (char c = 'a'; c <= 'z'; ++c)
 			words[(int) (c - 'a')] = c;
 
 		char[] numbers = new char[10];
 		for (char c = '0'; c <= '9'; ++c)
 			numbers[(int) (c - '0')] = c;
 
 		String[] keys = { "#home", "#end", "#ins", "#del", "#bak", "#left", "#right", "#up", "#down", "#pgup", "#pgdn",
 				"#f1", "#f2", "#f3", "#f4", "#f5", "#f6", "#f7", "#f8", "#f9", "#f10", "#f11", "#f12" };
 		//javascript key code, http://www.cambiaresearch.com/articles/15/javascript-char-codes-key-codes
 		int[] keycodes = { 36, 35, 45, 46, 8, 37, 39, 38, 40, 33, 34, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122,
 				123 };
 
 		Map<String, String> map = new HashMap<String, String>();
 		map.put(keys[0], "" + keycodes[0]);
 		map.put(keys[1], "" + keycodes[1]);
 
 		List<String> ctrls = new ArrayList<String>();
 		List<String> alts = new ArrayList<String>();
 		List<String> shifts = new ArrayList<String>();
 		int c = 65;
 		for (char w : words) {
 			ctrls.add("^" + w);
 			alts.add("@" + w);
 			map.put("^" + w, "" + c);
 			map.put("@" + w, "" + c);
 			c++;
 		}
 		c = 48;
 		for (char n : numbers) {
 			ctrls.add("^" + n);
 			alts.add("@" + n);
 			map.put("^" + n, "" + c);
 			map.put("@" + n, "" + c);
 			c++;
 		}
 		for (int i = 0; i < keys.length; ++i) {
 			String n = keys[i];
 			ctrls.add("^" + n);
 			alts.add("@" + n);
 			shifts.add("$" + n);
 			map.put("^" + n, "" + keycodes[i]);
 			map.put("@" + n, "" + keycodes[i]);
 			map.put("$" + n, "" + keycodes[i]);
 		}
 
 		//		generate a string contained all ctrl key 
 		//		for(String s : ctrls)
 		//			System.out.print(s);
 		//		for(String s : alts)
 		//			System.out.print(s);
 		//		for(String s : shifts)
 		//			System.out.print(s);
 		//		System.out.println("");
 
 		DesktopAgent desktop = Zats.newClient().connect("/~./basic/keystroke-input.zul");
 
 		Label target = desktop.query("#target").as(Label.class);
 		Label ref = desktop.query("#ref").as(Label.class);
 		Label event = desktop.query("#eventName").as(Label.class);
 		Label code = desktop.query("#code").as(Label.class);
 		Label ctrl = desktop.query("#ctrl").as(Label.class);
 		assertEquals("", target.getValue());
 		assertEquals("", ref.getValue());
 		assertEquals("", event.getValue());
 		assertEquals("", code.getValue());
 		assertEquals("", ctrl.getValue());
 
 		// components handle event
 		List<ComponentAgent> comps = desktop.query("#bySelf").getChildren();
 		assertEquals(17, comps.size());
 
 		// onOk
 		for (ComponentAgent comp : comps) {
 			comp.stroke("#enter");
 			assertEquals(((Component)comp.getDelegatee()).getDefinition().getName(), target.getValue());
 			assertEquals(((Component)comp.getDelegatee()).getDefinition().getName(), ref.getValue());
 			assertEquals(Events.ON_OK, event.getValue());
 			assertEquals("13", code.getValue());
 			assertEquals("none", ctrl.getValue());
 		}
 
 		// onCancel
 		for (ComponentAgent comp : comps) {
 			comp.stroke("#esc");
 			assertEquals(((Component)comp.getDelegatee()).getDefinition().getName(), target.getValue());
 			assertEquals(((Component)comp.getDelegatee()).getDefinition().getName(), ref.getValue());
 			assertEquals(Events.ON_CANCEL, event.getValue());
 			assertEquals("27", code.getValue());
 			assertEquals("none", ctrl.getValue());
 		}
 
 		// onCtrlKey - ctrl
 		for (String k : ctrls) {
 			for (ComponentAgent comp : comps) {
 				comp.stroke(k);
 				assertEquals(((Component)comp.getDelegatee()).getDefinition().getName(), target.getValue());
 				assertEquals(((Component)comp.getDelegatee()).getDefinition().getName(), ref.getValue());
 				assertEquals(Events.ON_CTRL_KEY, event.getValue());
 				assertEquals(map.get(k), code.getValue());
 				assertEquals("ctrl", ctrl.getValue());
 			}
 		}
 
 		// onCtrlKey - alt
 		for (String k : alts) {
 			for (ComponentAgent comp : comps) {
 				comp.stroke(k);
 				assertEquals(((Component)comp.getDelegatee()).getDefinition().getName(), target.getValue());
 				assertEquals(((Component)comp.getDelegatee()).getDefinition().getName(), ref.getValue());
 				assertEquals(Events.ON_CTRL_KEY, event.getValue());
 				assertEquals(map.get(k), code.getValue());
 				assertEquals("alt", ctrl.getValue());
 			}
 		}
 
 		// onCtrlKey - shift
 		for (String k : shifts) {
 			for (ComponentAgent comp : comps) {
 				comp.stroke(k);
 				assertEquals(((Component)comp.getDelegatee()).getDefinition().getName(), target.getValue());
 				assertEquals(((Component)comp.getDelegatee()).getDefinition().getName(), ref.getValue());
 				assertEquals(Events.ON_CTRL_KEY, event.getValue());
 				assertEquals(map.get(k), code.getValue());
 				assertEquals("shift", ctrl.getValue());
 			}
 		}
 
 		// parent component handle event
 		ComponentAgent parent = desktop.query("#byParent");
 		String targetName = ((Component)parent.getDelegatee()).getDefinition().getName();
 		comps = parent.getChildren();
 		assertEquals(17, comps.size());
 
 		// onOk
 		for (ComponentAgent comp : comps) {
 			comp.stroke("#enter");
 			assertEquals(targetName, target.getValue());
 			assertEquals(((Component)comp.getDelegatee()).getDefinition().getName(), ref.getValue());
 			assertEquals(Events.ON_OK, event.getValue());
 			assertEquals("13", code.getValue());
 			assertEquals("none", ctrl.getValue());
 		}
 
 		// onCancel
 		for (ComponentAgent comp : comps) {
 			comp.stroke("#esc");
 			assertEquals(targetName, target.getValue());
 			assertEquals(((Component)comp.getDelegatee()).getDefinition().getName(), ref.getValue());
 			assertEquals(Events.ON_CANCEL, event.getValue());
 			assertEquals("27", code.getValue());
 			assertEquals("none", ctrl.getValue());
 		}
 
 		// onCtrlKey - ctrl
 		for (String k : ctrls) {
 			for (ComponentAgent comp : comps) {
 				comp.stroke(k);
 				assertEquals(targetName, target.getValue());
 				assertEquals(((Component)comp.getDelegatee()).getDefinition().getName(), ref.getValue());
 				assertEquals(Events.ON_CTRL_KEY, event.getValue());
 				assertEquals(map.get(k), code.getValue());
 				assertEquals("ctrl", ctrl.getValue());
 			}
 		}
 
 		// onCtrlKey - alt
 		for (String k : alts) {
 			for (ComponentAgent comp : comps) {
 				comp.stroke(k);
 				assertEquals(targetName, target.getValue());
 				assertEquals(((Component)comp.getDelegatee()).getDefinition().getName(), ref.getValue());
 				assertEquals(Events.ON_CTRL_KEY, event.getValue());
 				assertEquals(map.get(k), code.getValue());
 				assertEquals("alt", ctrl.getValue());
 			}
 		}
 
 		// onCtrlKey - shift
 		for (String k : shifts) {
 			for (ComponentAgent comp : comps) {
 				comp.stroke(k);
 				assertEquals(targetName, target.getValue());
 				assertEquals(((Component)comp.getDelegatee()).getDefinition().getName(), ref.getValue());
 				assertEquals(Events.ON_CTRL_KEY, event.getValue());
 				assertEquals(map.get(k), code.getValue());
 				assertEquals("shift", ctrl.getValue());
 			}
 		}
 	}
 	
 	@Test
 	public void testTypingAgent() {
 		DesktopAgent desktop = Zats.newClient().connect("/~./basic/typing.zul");
 
 		// labels for validation
 		Label event = desktop.query("#eventName").as(Label.class);
 		Label target = desktop.query("#target").as(Label.class);
 		Label value = desktop.query("#value").as(Label.class);
 		assertEquals("", event.getValue());
 		assertEquals("", target.getValue());
 		assertEquals("", value.getValue());
 
 		// components handle event
 		List<ComponentAgent> comps = desktop.query("#inputs").getChildren();
 		assertEquals(11, comps.size());
 
 		for (int i = 0; i < comps.size(); ++i) {
 			// typing
 			String text = "type " + i;
 			ComponentAgent comp = comps.get(i);
 			comp.as(InputAgent.class).typing(text);
 			// validate
 			assertEquals("onChanging", event.getValue());
 			assertEquals(((Component) comp.getDelegatee()).getDefinition().getName(), target.getValue());
 			assertEquals(text, value.getValue());
 		}
 	}
 	
 	@Test
 	public void testMaxMinAgent() {
 		DesktopAgent desktop = Zats.newClient().connect("/~./basic/max-min.zul");
 		Label eventName = desktop.query("#eventName").as(Label.class);
 		Label target = desktop.query("#target").as(Label.class);
 		Label flag = desktop.query("#flag").as(Label.class);
 		assertEquals("", eventName.getValue());
 		assertEquals("", target.getValue());
 		assertEquals("", flag.getValue());
 
 		String targetName = "window";
 		SizeAgent agent = desktop.query(targetName).as(SizeAgent.class);
 
 		agent.maximize(true);
 		assertEquals("onMaximize", eventName.getValue());
 		assertEquals(targetName, target.getValue());
 		assertEquals("true", flag.getValue());
 
 		agent.maximize(false);
 		assertEquals("onMaximize", eventName.getValue());
 		assertEquals(targetName, target.getValue());
 		assertEquals("false", flag.getValue());
 
 		agent.minimize(true);
 		assertEquals("onMinimize", eventName.getValue());
 		assertEquals(targetName, target.getValue());
 		assertEquals("true", flag.getValue());
 
 		agent.minimize(false);
 		assertEquals("onMinimize", eventName.getValue());
 		assertEquals(targetName, target.getValue());
 		assertEquals("false", flag.getValue());
 
 		targetName = "panel";
 		agent = desktop.query(targetName).as(SizeAgent.class);
 
 		agent.maximize(true);
 		assertEquals("onMaximize", eventName.getValue());
 		assertEquals(targetName, target.getValue());
 		assertEquals("true", flag.getValue());
 
 		agent.maximize(false);
 		assertEquals("onMaximize", eventName.getValue());
 		assertEquals(targetName, target.getValue());
 		assertEquals("false", flag.getValue());
 
 		agent.minimize(true);
 		assertEquals("onMinimize", eventName.getValue());
 		assertEquals(targetName, target.getValue());
 		assertEquals("true", flag.getValue());
 
 		agent.minimize(false);
 		assertEquals("onMinimize", eventName.getValue());
 		assertEquals(targetName, target.getValue());
 		assertEquals("false", flag.getValue());
 
 		// test disabled
 		for (ComponentAgent ca : desktop.queryAll("#switches button"))
 			ca.as(ClickAgent.class).click();
 
 		try {
 			desktop.query("window").as(SizeAgent.class).maximize(true);
 			fail();
 		} catch (AgentException e) {
 		}
 		try {
 			desktop.query("window").as(SizeAgent.class).minimize(true);
 			fail();
 		} catch (AgentException e) {
 		}
 		try {
 			desktop.query("panel").as(SizeAgent.class).maximize(true);
 			fail();
 		} catch (AgentException e) {
 		}
 		try {
 			desktop.query("panel").as(SizeAgent.class).minimize(true);
 			fail();
 		} catch (AgentException e) {
 		}
 	}
 	
 	@Test
 	public void testDragDrop(){
 		DesktopAgent desktop = Zats.newClient().connect("/~./basic/drag.zul");
 		ComponentAgent leftBox = desktop.query("#left");
 		Assert.assertEquals(6, leftBox.queryAll("listitem").size());
 		
 		ComponentAgent rightBox = desktop.query("#right");
 		Assert.assertEquals(2, rightBox.queryAll("listitem").size());
 		Assert.assertNull(rightBox.query("listcell[label='ZK Forge']"));
 		
 		//move 1 item from left to right
 		ComponentAgent draggedItem = leftBox.query("listcell[label='ZK Forge']").getParent();
 		draggedItem.as(DragAgent.class).dropOn(rightBox);
 		Assert.assertEquals(5, leftBox.queryAll("listitem").size());
 		Assert.assertEquals(3, rightBox.queryAll("listitem").size());
 		Assert.assertNotNull(rightBox.query("listcell[label='ZK Forge']"));
 		
 		//move lower item before upper item
 		ComponentAgent upperItem = rightBox.query("listcell[label='ZK Studio']").getParent();
 		ComponentAgent lowerItem = rightBox.query("listcell[label='ZK Forge']").getParent();
 		Assert.assertEquals(1, upperItem.as(Listitem.class).getIndex());
 		Assert.assertEquals(2, lowerItem.as(Listitem.class).getIndex());
 		lowerItem.as(DragAgent.class).dropOn(upperItem);
 		Assert.assertEquals(2, upperItem.as(Listitem.class).getIndex());
 		Assert.assertEquals(1, lowerItem.as(Listitem.class).getIndex());
 	}
 	
 	@Test
 	public void testSizeOperation() {
 		DesktopAgent desktop = Zats.newClient().connect("/~./basic/size.zul");
 		Label eventName = desktop.query("#eventName").as(Label.class);
 		Label target = desktop.query("#target").as(Label.class);
 		Label width = desktop.query("#width").as(Label.class);
 		Label height = desktop.query("#height").as(Label.class);
 		assertEquals("", eventName.getValue());
 		assertEquals("", target.getValue());
 		assertEquals("", width.getValue());
 		assertEquals("", height.getValue());
 
 		String targetName = "window";
 		SizeAgent agent = desktop.query(targetName).as(SizeAgent.class);
 
 		agent.resize(-1, -1); // do nothing
 		assertEquals("", eventName.getValue());
 		assertEquals("", target.getValue());
 		assertEquals("", width.getValue());
 		assertEquals("", height.getValue());
 		
 		int[][] args = { 
 			{ 50, -1 }, 
 			{ -1, 50 }, 
 			{ 100, 100 }, 
 			{ -1, -1},
 		};
 		String[][] except = {
 			{ "50px", "100px" }, // default min-height 
 			{ "50px", "50px" }, 
 			{ "100px", "100px" }, 
 			{ "100px", "100px" }, // do nothing
 		};
 
 		for (int i = 0; i < args.length; ++i) {
 			agent.resize(args[i][0], args[i][1]);
 			assertEquals("onSize", eventName.getValue());
 			assertEquals(targetName, target.getValue());
 			assertEquals(except[i][0], width.getValue());
 			assertEquals(except[i][1], height.getValue());
 		}
 		
 		targetName = "panel";
 		agent = desktop.query(targetName).as(SizeAgent.class);
 
 		args = new int[][]{ 
 			{ -1, 50 }, 
 			{ 50, -1 }, 
 			{ 100, 100 }, 
 			{ -1, -1},
 		};
 		except = new String[][]{
 			{ "200px", "50px" }, // default min-width 
 			{ "50px", "50px" }, 
 			{ "100px", "100px" }, 
 			{ "100px", "100px" }, // do nothing
 		};
 
 		for (int i = 0; i < args.length; ++i) {
 			agent.resize(args[i][0], args[i][1]);
 			assertEquals("onSize", eventName.getValue());
 			assertEquals(targetName, target.getValue());
 			assertEquals(except[i][0], width.getValue());
 			assertEquals(except[i][1], height.getValue());
 		}
 	}
 
 	@Test
 	public void testPaging(){
 		DesktopAgent desktop = Zats.newClient().connect("/~./basic/paging.zul");
 		
 		//listbox's paging
 		ComponentAgent paging = desktop.query("listbox > paging");
 		Assert.assertEquals(0, paging.as(Paging.class).getActivePage());
 		
 		paging.as(PagingAgent.class).moveTo(1);
 		Assert.assertEquals("1", desktop.query("#listboxPageIndex").as(Label.class).getValue());
 		
 		//grid's paging
 		paging = desktop.query("#grid > paging");
 		Assert.assertEquals(0, paging.as(Paging.class).getActivePage());
 		
 		paging.as(PagingAgent.class).moveTo(1);
 		Assert.assertEquals("1", desktop.query("#gridPageIndex").as(Label.class).getValue());
 		
 		//tree's paging
 		paging = desktop.query("tree > paging");
 		Assert.assertEquals(0, paging.as(Paging.class).getActivePage());
 		
 		paging.as(PagingAgent.class).moveTo(1);
 		Assert.assertEquals("1", desktop.query("#treePageIndex").as(Label.class).getValue());
 		
 		//paging itself
 		paging = desktop.query("#pg");
 		paging.as(PagingAgent.class).moveTo(1);
 		Assert.assertEquals("1", desktop.query("#leftGridPageIndex").as(Label.class).getValue());
 		Assert.assertEquals("1", desktop.query("#rightGridPageIndex").as(Label.class).getValue());
 		
 		//move out of page bound
 		try{
 			paging.as(PagingAgent.class).moveTo(-1);
 			fail();
 		}catch(AgentException e){
 			logger.fine("expected exception: "+e.getMessage());
 		}
 		try{
 			paging.as(PagingAgent.class).moveTo(paging.as(Paging.class).getPageCount());
 			fail();
 		}catch(AgentException e){
 			logger.fine("expected exception: "+e.getMessage());
 		}
 	}
 	
 	@Test
 	public void testBookmarkAgent() {
 		DesktopAgent desktopAgent = Zats.newClient().connect("/~./basic/bookmark.zul");
 		assertEquals("Hello World!", desktopAgent.query("#msg").as(Label.class).getValue());
 		
 		desktopAgent.as(BookmarkAgent.class).change("ABCD");
 		assertEquals("Welcome ABCD", desktopAgent.query("#msg").as(Label.class).getValue());
 		
 		desktopAgent.query("#btn").as(ClickAgent.class).click();
 		assertEquals("XYZ", desktopAgent.as(Desktop.class).getBookmark());
 	}
 	
 	//	unsupported temporary, the behavior of it isn't compatible with the resize agent. 
 	//	@Test
 	public void testColumnSizeOperation() {
 		DesktopAgent desktop = Zats.newClient().connect("/~./basic/size-column.zul");
 		Label eventName = desktop.query("#eventName").as(Label.class);
 		Label target = desktop.query("#target").as(Label.class);
 		Label index = desktop.query("#index").as(Label.class);
 		Label width = desktop.query("#width").as(Label.class);
 		Label previousWidth = desktop.query("#previousWidth").as(Label.class);
 		assertEquals("", eventName.getValue());
 		assertEquals("", target.getValue());
 		assertEquals("", index.getValue());
 		assertEquals("", width.getValue());
 		assertEquals("", previousWidth.getValue());
 		
 		String[][] args = new String[][] {
 				{"#gc0" , "-1" , "999"},
 				{"#gc0" , "100" , "-1"},
 				{"#gc0" , "110" , "-1"},
 				{"#gc2" , "120" , "-1"},
 				{"#gc1" , "130" , "-1"},
 				{"#gc1" , "130" , "-1"},
 		};
 		String[][] except = new String[][] {
 				{ "", "", "", "", "" },
 				{ "onColSize", "columns", "0", "100px", "null" },
 				{ "onColSize", "columns", "0", "110px", "100px" },
 				{ "onColSize", "columns", "2", "120px", "200px" },
 				{ "onColSize", "columns", "1", "130px", "200px" },
 				{ "onColSize", "columns", "1", "130px", "200px" },
 		};
 		for (int i = 0; i < args.length; ++i) {
 			String id = args[i][0];
 			int w = Integer.parseInt(args[i][1]), h = Integer.parseInt(args[i][2]);
 			desktop.query(id).as(SizeAgent.class).resize(w, h);
 			assertEquals(except[i][0], eventName.getValue());
 			assertEquals(except[i][1], target.getValue());
 			assertEquals(except[i][2], index.getValue());
 			assertEquals(except[i][3], width.getValue());
 			assertEquals(except[i][4], previousWidth.getValue());
 		}
 		
 		args = new String[][] {
 				{"#lh0" , "-1" , "999"},
 				{"#lh0" , "100" , "-1"},
 				{"#lh0" , "110" , "-1"},
 				{"#lh2" , "120" , "-1"},
 				{"#lh1" , "130" , "-1"},
 				{"#lh1" , "130" , "-1"},
 		};
 		except = new String[][] {
 				{ "onColSize", "columns", "1", "130px", "200px" },
 				{ "onColSize", "listhead", "0", "100px", "null" },
 				{ "onColSize", "listhead", "0", "110px", "100px" },
 				{ "onColSize", "listhead", "2", "120px", "200px" },
 				{ "onColSize", "listhead", "1", "130px", "200px" },
 				{ "onColSize", "listhead", "1", "130px", "200px" },
 		};
 		for (int i = 0; i < args.length; ++i) {
 			String id = args[i][0];
 			int w = Integer.parseInt(args[i][1]), h = Integer.parseInt(args[i][2]);
 			desktop.query(id).as(SizeAgent.class).resize(w, h);
 			assertEquals(except[i][0], eventName.getValue());
 			assertEquals(except[i][1], target.getValue());
 			assertEquals(except[i][2], index.getValue());
 			assertEquals(except[i][3], width.getValue());
 			assertEquals(except[i][4], previousWidth.getValue());
 		}
 		
 		args = new String[][] {
 				{"#tc0" , "-1" , "999"},
 				{"#tc0" , "100" , "-1"},
 				{"#tc0" , "110" , "-1"},
 				{"#tc2" , "120" , "-1"},
 				{"#tc1" , "130" , "-1"},
 				{"#tc1" , "130" , "-1"},
 		};
 		except = new String[][] {
 				{ "onColSize", "listhead", "1", "130px", "200px" },
 				{ "onColSize", "treecols", "0", "100px", "null" },
 				{ "onColSize", "treecols", "0", "110px", "100px" },
 				{ "onColSize", "treecols", "2", "120px", "200px" },
 				{ "onColSize", "treecols", "1", "130px", "200px" },
 				{ "onColSize", "treecols", "1", "130px", "200px" },
 		};
 		for (int i = 0; i < args.length; ++i) {
 			String id = args[i][0];
 			int w = Integer.parseInt(args[i][1]), h = Integer.parseInt(args[i][2]);
 			desktop.query(id).as(SizeAgent.class).resize(w, h);
 			assertEquals(except[i][0], eventName.getValue());
 			assertEquals(except[i][1], target.getValue());
 			assertEquals(except[i][2], index.getValue());
 			assertEquals(except[i][3], width.getValue());
 			assertEquals(except[i][4], previousWidth.getValue());
 		}
 	}
 	
 	//column's label in group.zul
 	final private String COLUMN_AUTHOR = "Author";
 	final private String COLUMN_TITLE = "Title";
 	
 	@Test
 	public void testGroup(){
 		
 		DesktopAgent desktop = Zats.newClient().connect("/~./basic/group-sort.zul");
 		ComponentAgent groupingColumn = desktop.query("column[label='"+COLUMN_AUTHOR+"']");
 		groupingColumn.as(GroupAgent.class).group();
 		
 		Label groupingLabel = desktop.query("#groupingColumn").as(Label.class);
 		Assert.assertEquals(COLUMN_AUTHOR, groupingLabel.getValue());
 		
 		groupingColumn = desktop.query("column[label='"+COLUMN_TITLE+"']");
 		groupingColumn.as(GroupAgent.class).group();
 		
 		Assert.assertEquals(COLUMN_TITLE, groupingLabel.getValue());
 	}	
 
 	
 	@Test
 	public void testScroll() {
 		DesktopAgent desktop = Zats.newClient().connect("/~./basic/scroll.zul");
 		Label msg1 = desktop.query("#msg1").as(Label.class);
 		Assert.assertEquals("", msg1.getValue());
 
 		// slider 1; 0 to 100
 		Integer[] args = new Integer[] { 0, 50, 100 };
 		String[] expected = new String[] {
 				"s1,onScroll,0",
 				"s1,onScroll,50", 
 				"s1,onScroll,100", 
 		};
 		InputAgent slider = desktop.query("#s1").as(InputAgent.class);
 		for (int i = 0; i < args.length; ++i) {
 			slider.input(args[i]);
 			assertEquals(expected[i], msg1.getValue());
 		}
 
 		// slider 2; 0 to 200
 		args = new Integer[] { 0, 199 , 200 };
 		expected = new String[] {
 				"s2,onScroll,0",
 				"s2,onScroll,199", 
 				"s2,onScroll,200", 
 		};
 		slider = desktop.query("#s2").as(InputAgent.class);
 		for (int i = 0; i < args.length; ++i) {
 			slider.input(args[i]);
 			assertEquals(expected[i], msg1.getValue());
 		}
 
 		// compatibility
 		slider.input(1);
 		assertEquals("s2,onScroll,1", msg1.getValue());
 		slider.input(2L);
 		assertEquals("s2,onScroll,2", msg1.getValue());
 		slider.input((short) 3);
 		assertEquals("s2,onScroll,3", msg1.getValue());
 		slider.input((byte) 4);
 		assertEquals("s2,onScroll,4", msg1.getValue());
 		slider.input(BigInteger.valueOf(5L));
 		assertEquals("s2,onScroll,5", msg1.getValue());
 		slider.input("6");
 		assertEquals("s2,onScroll,6", msg1.getValue());
 		slider.input("   7   ");
 		assertEquals("s2,onScroll,7", msg1.getValue());
 
 		// out of bounds
 		slider.input(200);
 		assertEquals("s2,onScroll,200", msg1.getValue());
 		try {
 			slider.input(-1);
 			fail();
 		} catch (AgentException e) {
 			assertEquals("s2,onScroll,200", msg1.getValue());
 		}
 		try {
 			slider.input(201);
 			fail();
 		} catch (AgentException e) {
 			assertEquals("s2,onScroll,200", msg1.getValue());
 		}
 
 		// wrong value, type or syntax
 		try {
 			slider.input(null);
 			fail();
 		} catch (AgentException e) {
 			assertEquals("s2,onScroll,200", msg1.getValue());
 		}
 		try {
 			slider.input("");
 			fail();
 		} catch (AgentException e) {
 			assertEquals("s2,onScroll,200", msg1.getValue());
 		}
 		try {
 			slider.input("   ");
 			fail();
 		} catch (AgentException e) {
 			assertEquals("s2,onScroll,200", msg1.getValue());
 		}
 		try {
 			slider.input("100px");
 			fail();
 		} catch (AgentException e) {
 			assertEquals("s2,onScroll,200", msg1.getValue());
 		}
 		try {
 			slider.input(100.0);
 			fail();
 		} catch (AgentException e) {
 			assertEquals("s2,onScroll,200", msg1.getValue());
 		}
 		try {
 			slider.input("100.0");
 			fail();
 		} catch (AgentException e) {
 			assertEquals("s2,onScroll,200", msg1.getValue());
 		}
 	}
 	
 	
 	@Test
 	public void testMoveAgent() {
 		DesktopAgent desktop = Zats.newClient().connect("/~./basic/move.zul");
 		Label target = desktop.query("#target").as(Label.class);
 		Label eventName = desktop.query("#eventName").as(Label.class);
 		Label left = desktop.query("#left").as(Label.class);
 		Label top = desktop.query("#top").as(Label.class);
 		Assert.assertEquals("", target.getValue());
 		Assert.assertEquals("", eventName.getValue());
 		Assert.assertEquals("", left.getValue());
 		Assert.assertEquals("", top.getValue());
 		
 		String name = "window";
 		int[][] args = new int[][]{
 				{-100, -100},	
 				{-100, 100},	
 				{100, -100},	
 				{100, 100},	
 		};
 		String[][] expected = new String[][]{
 				{name , "onMove" , "-100px" , "-100px"},	
 				{name , "onMove" , "-100px" , "100px"},	
 				{name , "onMove" , "100px" , "-100px"},	
 				{name , "onMove" , "100px" , "100px"},	
 		};
 		ComponentAgent comp = desktop.query(name);
 		MoveAgent moveAgent = comp.as(MoveAgent.class);
 		for (int i = 0; i < args.length; ++i) {
 			moveAgent.moveTo(args[i][0], args[i][1]);
 			Assert.assertEquals(expected[i][0], target.getValue());
 			Assert.assertEquals(expected[i][1], eventName.getValue());
 			Assert.assertEquals(expected[i][2], left.getValue());
 			Assert.assertEquals(expected[i][3], top.getValue());
 			Assert.assertEquals(left.getValue(), comp.as(HtmlBasedComponent.class).getLeft());
 			Assert.assertEquals(top.getValue(), comp.as(HtmlBasedComponent.class).getTop());
 		}
 		
 		name = "panel";
 		args = new int[][]{
 				{-100, -100},	
 				{-100, 100},	
 				{100, -100},	
 				{100, 100},	
 		};
 		expected = new String[][]{
 				{name , "onMove" , "-100px" , "-100px"},	
 				{name , "onMove" , "-100px" , "100px"},	
 				{name , "onMove" , "100px" , "-100px"},	
 				{name , "onMove" , "100px" , "100px"},	
 		};
 		comp = desktop.query(name);
 		moveAgent = comp.as(MoveAgent.class);
 		for (int i = 0; i < args.length; ++i) {
 			moveAgent.moveTo(args[i][0], args[i][1]);
 			Assert.assertEquals(expected[i][0], target.getValue());
 			Assert.assertEquals(expected[i][1], eventName.getValue());
 			Assert.assertEquals(expected[i][2], left.getValue());
 			Assert.assertEquals(expected[i][3], top.getValue());
 			Assert.assertEquals(left.getValue(), comp.as(HtmlBasedComponent.class).getLeft());
 			Assert.assertEquals(top.getValue(), comp.as(HtmlBasedComponent.class).getTop());
 		}
 	}
 
 		/*
 		 * Sort grid's column and verify its ascending order.
 		 */
 		@Test
 		public void testSort(){
 			DesktopAgent desktop = Zats.newClient().connect("/~./basic/group-sort.zul");
 
 			//column
 			ComponentAgent sortingColumn = desktop.query("column[label='"+COLUMN_AUTHOR+"']");
 			Label sortStatus = desktop.query("#sortStatus").as(Label.class);
 
 			sortingColumn.as(SortAgent.class).sort(true);
 			Assert.assertEquals(COLUMN_AUTHOR+",true", sortStatus.getValue());
 			sortingColumn.as(SortAgent.class).sort(false);
 			Assert.assertEquals(COLUMN_AUTHOR+",false", sortStatus.getValue());
 
 			sortingColumn = desktop.query("column[label='"+COLUMN_TITLE+"']");
 
 			sortingColumn.as(SortAgent.class).sort(true);
 			Assert.assertEquals(COLUMN_TITLE+",true", sortStatus.getValue());
 			sortingColumn.as(SortAgent.class).sort(false);
 			Assert.assertEquals(COLUMN_TITLE+",false", sortStatus.getValue());
 
 			//listheader
 			ComponentAgent sortingHeader =  desktop.query("listheader[label='Name']");
 			Assert.assertEquals(SwitchedSortAgentImpl.DESCENDING, sortingHeader.as(Listheader.class).getSortDirection());
 			//can sort in specified order in spite of its original sorting order
 			sortingHeader.as(SortAgent.class).sort(true);
 			Assert.assertEquals("Name", sortStatus.getValue());
 			Assert.assertEquals(SwitchedSortAgentImpl.ASCENDING, sortingHeader.as(Listheader.class).getSortDirection());
 			sortingHeader.as(SortAgent.class).sort(false);
 			Assert.assertEquals(SwitchedSortAgentImpl.DESCENDING, sortingHeader.as(Listheader.class).getSortDirection());
 			//repeat sorting in the same order should work correctly 
 			sortingHeader.as(SortAgent.class).sort(false);
 			Assert.assertEquals(SwitchedSortAgentImpl.DESCENDING, sortingHeader.as(Listheader.class).getSortDirection());
 
 			sortingHeader =  desktop.query("listheader[label='Gender']");
 			sortingHeader.as(SortAgent.class).sort(false);
 			Assert.assertEquals(SwitchedSortAgentImpl.DESCENDING, sortingHeader.as(Listheader.class).getSortDirection());
 			Assert.assertEquals("Gender", sortStatus.getValue());
 
 			//treecol
 			sortingColumn = desktop.query("treecol[label='Description']");
 
 			//can sort in specified order in spite of its original sorting order
 			sortingColumn.as(SortAgent.class).sort(false);
 			Assert.assertEquals("Description", sortStatus.getValue());
 			Assert.assertEquals(SwitchedSortAgentImpl.DESCENDING, sortingColumn.as(Treecol.class).getSortDirection());
 			sortingColumn.as(SortAgent.class).sort(true);
 			Assert.assertEquals(SwitchedSortAgentImpl.ASCENDING, sortingColumn.as(Treecol.class).getSortDirection());
 			//repeat sorting in the same order should work correctly 
 			sortingColumn.as(SortAgent.class).sort(true);
 			Assert.assertEquals(SwitchedSortAgentImpl.ASCENDING, sortingColumn.as(Treecol.class).getSortDirection());
 
 		}
 		
 	private String fetchString(InputStream is) throws Exception {
 		StringBuilder sb = new StringBuilder();
 		try {
 			Reader r = new InputStreamReader(is);
 			r = new BufferedReader(r);
 			int c;
 			while ((c = r.read()) >= 0)
 				sb.append((char) c);
 		} finally {
 			Util.close(is);
 		}
 		return sb.toString();
 	}
 	
 	@Test
 	public void testDownload() throws Exception {
 		DesktopAgent desktop = Zats.newClient().connect("/~./basic/download.zul");
 		assertTrue(desktop.query("#dummy").is(Button.class));
 		assertTrue(desktop.query("#btn0").is(Button.class));
 		assertTrue(desktop.query("#btn1").is(Button.class));
 		assertTrue(desktop.query("#btn2").is(Button.class));
 		// temp file
 		String path = desktop.query("#path").as(Label.class).getValue();
 		assertTrue(path != null && path.length() > 0);
 		File temp = new File(path);
 		assertTrue(temp.canRead());
 
 		// no download
 		assertTrue(desktop.getDownloadable() == null);
 		desktop.query("#dummy").click();
 		assertTrue(desktop.getDownloadable() == null);
 
 		// download from file
 		desktop.query("#btn0").click();
 		Resource downloadable = desktop.getDownloadable();
 		assertTrue(downloadable != null);
 		assertEquals(temp.getName(), downloadable.getName());
 		assertEquals("Hello ZK!\nThis is a test file!", fetchString(downloadable.getInputStream()));
 
 		// no download again
 		desktop.query("#dummy").click();
 		assertTrue(desktop.getDownloadable() == null);
 
 		// download from data
 		desktop.query("#btn1").click();
 		downloadable = desktop.getDownloadable();
 		assertTrue(downloadable != null);
 		assertEquals("test.txt", downloadable.getName());
 		assertEquals("Hello world!\nHello ZK!", fetchString(downloadable.getInputStream()));
 
 		// download from file and resumable
 		desktop.query("#btn2").click();
 		downloadable = desktop.getDownloadable();
 		assertTrue(downloadable != null);
 		assertEquals(temp.getName(), downloadable.getName());
 		assertEquals("Hello ZK!\nThis is a test file!", fetchString(downloadable.getInputStream()));
 
 		// download last file (invoke download twice in one AU event)
 		desktop.query("#btn3").click();
 		downloadable = desktop.getDownloadable();
 		assertTrue(downloadable != null);
 		assertEquals("file1.txt", downloadable.getName());
 		assertEquals("This is no. 1!", fetchString(downloadable.getInputStream()));
 	}
 	
 	@Test
 	public void testUploadAgent() throws Exception {
 
 		DesktopAgent desktop = Zats.newClient().connect("/~./basic/upload.zul");
 		Vbox results = desktop.query("#results").as(Vbox.class);
 		Assert.assertEquals(0, results.getChildren().size());
 
 		// prepare temp. file for testing 
 		File textFile = File.createTempFile("zats-upload-text-", ".tmp");
 		textFile.deleteOnExit();
 		String text = "Hello! World!\r\nHello! ZK!\r\n";
 		byte[] raw = text.getBytes("ISO-8859-1");
 		String binary = TypeUtil.toHexString(raw).toUpperCase();
 		FileOutputStream fos = new FileOutputStream(textFile);
 		fos.write(raw);
 		fos.close();
 
 		// binary file
 		for (int i = 0; i < 4; ++i) {
 			String id = "#btn" + i;
 			UploadAgent agent = desktop.query(id).as(UploadAgent.class);
 			agent.upload(textFile, null);
 			agent.finish();
 			Assert.assertEquals(textFile.getName(), desktop.query("#file0 .name").as(Label.class).getValue());
 			Assert.assertEquals("application/octet-stream", desktop.query("#file0 .contentType").as(Label.class)
 					.getValue());
 			Assert.assertEquals("octet-stream", desktop.query("#file0 .format").as(Label.class).getValue());
 			Assert.assertEquals(binary, desktop.query("#file0 .binary").as(Label.class).getValue());
 			Assert.assertEquals("", desktop.query("#file0 .text").as(Label.class).getValue());
 			Assert.assertEquals("", desktop.query("#file0 .width").as(Label.class).getValue());
 			Assert.assertEquals("", desktop.query("#file0 .height").as(Label.class).getValue());
 			desktop.query("#clean").click(); // clean results
 		}
 
 		// text stream
 		UploadAgent agent = desktop.query("#btn0").as(UploadAgent.class);
 		agent.upload("sample.txt", new ByteArrayInputStream(raw), "text/plain");
 		agent.finish();
 		Assert.assertEquals("sample.txt", desktop.query("#file0 .name").as(Label.class).getValue());
 		Assert.assertEquals("text/plain", desktop.query("#file0 .contentType").as(Label.class).getValue());
 		Assert.assertEquals("txt", desktop.query("#file0 .format").as(Label.class).getValue());
 		Assert.assertEquals("", desktop.query("#file0 .binary").as(Label.class).getValue());
 		Assert.assertEquals(text, desktop.query("#file0 .text").as(Label.class).getValue());
 		Assert.assertEquals("", desktop.query("#file0 .width").as(Label.class).getValue());
 		Assert.assertEquals("", desktop.query("#file0 .height").as(Label.class).getValue());
 
 		// image stream
 		raw = new byte[] { -119, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 10, 0, 0, 0, 10, 8,
 				2, 0, 0, 0, 2, 80, 88, -22, 0, 0, 0, 4, 103, 65, 77, 65, 0, 0, -79, -113, 11, -4, 97, 5, 0, 0, 0, 9,
 				112, 72, 89, 115, 0, 0, 18, 116, 0, 0, 18, 116, 1, -34, 102, 31, 120, 0, 0, 0, 39, 73, 68, 65, 84, 40,
 				83, 99, 124, 43, -93, -62, -128, 4, 76, -89, 106, 32, 115, -103, -112, 57, -104, 108, -102, 74, 51, 42,
 				109, -12, 65, -74, 82, 118, 122, -61, 96, 113, 26, 0, -35, -38, 4, -123, -73, -75, -2, 83, 0, 0, 0, 0,
 				73, 69, 78, 68, -82, 66, 96, -126 };
 		binary = TypeUtil.toHexString(raw).toUpperCase();
 		agent = desktop.query("#btn0").as(UploadAgent.class);
 		agent.upload("test.png", new ByteArrayInputStream(raw), "image/png");
 		agent.finish();
 		Assert.assertEquals("test.png", desktop.query("#file0 .name").as(Label.class).getValue());
 		Assert.assertEquals("image/png", desktop.query("#file0 .contentType").as(Label.class).getValue());
 		Assert.assertEquals("png", desktop.query("#file0 .format").as(Label.class).getValue());
 		Assert.assertEquals(binary, desktop.query("#file0 .binary").as(Label.class).getValue());
 		Assert.assertEquals("", desktop.query("#file0 .text").as(Label.class).getValue());
 		Assert.assertEquals("10px", desktop.query("#file0 .width").as(Label.class).getValue());
 		Assert.assertEquals("10px", desktop.query("#file0 .height").as(Label.class).getValue());
 		
 		// can't upload
 		try {
 			desktop.query("#clean").as(UploadAgent.class).upload(textFile, null);
 			fail("should throw exception");
 		} catch (AgentException e) {
 			System.out.println(e.getMessage());
 		}
 	}
 	
 	@Test
 	public void testUploadAgentWithDialog() throws Exception {
 		DesktopAgent desktop = Zats.newClient().connect("/~./basic/upload.zul");
 		Vbox results = desktop.query("#results").as(Vbox.class);
 		Assert.assertEquals(0, results.getChildren().size());
 
 		// prepare files for testing 
 		File textFile = File.createTempFile("zats-upload-text-", ".tmp");
 		textFile.deleteOnExit();
 		String text = "Hello! World!\r\nHello! ZK!\r\n";
 		byte[] textRaw = text.getBytes("ISO-8859-1");
 		String textBinary = TypeUtil.toHexString(textRaw).toUpperCase();
 		FileOutputStream fos = new FileOutputStream(textFile);
 		fos.write(textRaw);
 		fos.close();
 		File imageFile = File.createTempFile("zats-upload-image-", ".png");
 		imageFile.deleteOnExit();
 		byte[] imageRaw = new byte[] { -119, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 10, 0,
 				0, 0, 10, 8, 2, 0, 0, 0, 2, 80, 88, -22, 0, 0, 0, 4, 103, 65, 77, 65, 0, 0, -79, -113, 11, -4, 97, 5,
 				0, 0, 0, 9, 112, 72, 89, 115, 0, 0, 18, 116, 0, 0, 18, 116, 1, -34, 102, 31, 120, 0, 0, 0, 39, 73, 68,
 				65, 84, 40, 83, 99, 124, 43, -93, -62, -128, 4, 76, -89, 106, 32, 115, -103, -112, 57, -104, 108, -102,
 				74, 51, 42, 109, -12, 65, -74, 82, 118, 122, -61, 96, 113, 26, 0, -35, -38, 4, -123, -73, -75, -2, 83,
 				0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126 };
 		String imageBinary = TypeUtil.toHexString(imageRaw).toUpperCase();
 		fos = new FileOutputStream(imageFile);
 		fos.write(imageRaw);
 		fos.close();
 
 		// single upload
 		// text
 		desktop.query("#btn4").click();
 		UploadAgent agent = desktop.as(UploadAgent.class);
 		agent.upload(textFile, "text/plain");
 		agent.finish();
 		Assert.assertEquals(textFile.getName(), desktop.query("#file0 .name").as(Label.class).getValue());
 		Assert.assertEquals("text/plain", desktop.query("#file0 .contentType").as(Label.class).getValue());
 		Assert.assertEquals("txt", desktop.query("#file0 .format").as(Label.class).getValue());
 		Assert.assertEquals("", desktop.query("#file0 .binary").as(Label.class).getValue());
 		Assert.assertEquals(text, desktop.query("#file0 .text").as(Label.class).getValue());
 		Assert.assertEquals("", desktop.query("#file0 .width").as(Label.class).getValue());
 		Assert.assertEquals("", desktop.query("#file0 .height").as(Label.class).getValue());
 		// binary
 		desktop.query("#btn4").click();
 		agent = desktop.as(UploadAgent.class);
 		agent.upload(textFile, "application/octet-stream");
 		agent.finish();
 		Assert.assertEquals(textFile.getName(), desktop.query("#file0 .name").as(Label.class).getValue());
 		Assert.assertEquals("application/octet-stream", desktop.query("#file0 .contentType").as(Label.class).getValue());
 		Assert.assertEquals("octet-stream", desktop.query("#file0 .format").as(Label.class).getValue());
 		Assert.assertEquals(textBinary, desktop.query("#file0 .binary").as(Label.class).getValue());
 		Assert.assertEquals("", desktop.query("#file0 .text").as(Label.class).getValue());
 		Assert.assertEquals("", desktop.query("#file0 .width").as(Label.class).getValue());
 		Assert.assertEquals("", desktop.query("#file0 .height").as(Label.class).getValue());
 		// image
 		desktop.query("#btn4").click();
 		agent = desktop.as(UploadAgent.class);
 		agent.upload(imageFile, "image/png");
 		agent.finish();
		Assert.assertEquals(imageFile.getName(), desktop.query("#file0 .name").as(Label.class).getValue());
 		Assert.assertEquals("image/png", desktop.query("#file0 .contentType").as(Label.class).getValue());
 		Assert.assertEquals("png", desktop.query("#file0 .format").as(Label.class).getValue());
 		Assert.assertEquals(imageBinary, desktop.query("#file0 .binary").as(Label.class).getValue());
 		Assert.assertEquals("", desktop.query("#file0 .text").as(Label.class).getValue());
 		Assert.assertEquals("10px", desktop.query("#file0 .width").as(Label.class).getValue());
 		Assert.assertEquals("10px", desktop.query("#file0 .height").as(Label.class).getValue());
 
 		// multiple upload
 		desktop.query("#btn5").click();
 		agent = desktop.as(UploadAgent.class);
 		agent.upload(textFile, "text/plain");
 		agent.upload(textFile, "application/octet-stream");
		agent.upload(imageFile, "image/png");
 		agent.finish();
 		Assert.assertEquals(3, desktop.query("#results").getChildren().size());
 		// text
 		Assert.assertEquals(textFile.getName(), desktop.query("#file0 .name").as(Label.class).getValue());
 		Assert.assertEquals("text/plain", desktop.query("#file0 .contentType").as(Label.class).getValue());
 		Assert.assertEquals("txt", desktop.query("#file0 .format").as(Label.class).getValue());
 		Assert.assertEquals("", desktop.query("#file0 .binary").as(Label.class).getValue());
 		Assert.assertEquals(text, desktop.query("#file0 .text").as(Label.class).getValue());
 		Assert.assertEquals("", desktop.query("#file0 .width").as(Label.class).getValue());
 		Assert.assertEquals("", desktop.query("#file0 .height").as(Label.class).getValue());
 		// binary
 		Assert.assertEquals(textFile.getName(), desktop.query("#file1 .name").as(Label.class).getValue());
 		Assert.assertEquals("application/octet-stream", desktop.query("#file1 .contentType").as(Label.class).getValue());
 		Assert.assertEquals("octet-stream", desktop.query("#file1 .format").as(Label.class).getValue());
 		Assert.assertEquals(textBinary, desktop.query("#file1 .binary").as(Label.class).getValue());
 		Assert.assertEquals("", desktop.query("#file1 .text").as(Label.class).getValue());
 		Assert.assertEquals("", desktop.query("#file1 .width").as(Label.class).getValue());
 		Assert.assertEquals("", desktop.query("#file1 .height").as(Label.class).getValue());
 		// image
		Assert.assertEquals(imageFile.getName(), desktop.query("#file2 .name").as(Label.class).getValue());
 		Assert.assertEquals("image/png", desktop.query("#file2 .contentType").as(Label.class).getValue());
 		Assert.assertEquals("png", desktop.query("#file2 .format").as(Label.class).getValue());
 		Assert.assertEquals(imageBinary, desktop.query("#file2 .binary").as(Label.class).getValue());
 		Assert.assertEquals("", desktop.query("#file2 .text").as(Label.class).getValue());
 		Assert.assertEquals("10px", desktop.query("#file2 .width").as(Label.class).getValue());
 		Assert.assertEquals("10px", desktop.query("#file2 .height").as(Label.class).getValue());
 	}
 }
 
