 package nl.nikhef.xhtmlrenderer.swing;
 
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Window;
 import java.awt.event.ActionEvent;
 import java.awt.print.PrinterException;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.Properties;
 import java.util.logging.LogManager;
 
 import javax.swing.AbstractAction;
 import javax.swing.AbstractButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JRadioButton;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.JToggleButton;
 import javax.swing.text.JTextComponent;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import junit.extensions.abbot.ComponentTestFixture;
 
 import nl.nikhef.jgridstart.gui.Main;
 
 import org.junit.Test;
 import org.w3c.dom.Element;
 import org.xhtmlrenderer.css.parser.FSRGBColor;
 import org.xml.sax.SAXException;
 
 import abbot.finder.ComponentFinder;
 import abbot.finder.ComponentNotFoundException;
 import abbot.finder.Matcher;
 import abbot.finder.MultipleComponentsFoundException;
 import abbot.finder.matchers.ClassMatcher;
 import abbot.tester.ComponentTester;
 
 /** Tests for all implementations of {@link ITemplatePanel}.
  * <p>
  * This is an abstract class, each derived class must implement the method
  * {@linkplain #createPanel} which creates a new {@linkplain ITemplatePanel}
  * instance. Note that the returned class must be derived from {@link Component}
  * as well, since it must be shown on screen.
  */
 public abstract class ITemplatePanelTest extends ComponentTestFixture {
     
     /** Abbot tester */
     private ComponentTester tester = null;
     /** Current component under test, created by createPanel */
     private ITemplatePanel panel = null;
     /** Current frame that contains the panel, also created by createPanel */
     private Window frame = null;
     /** Whether showFrame() is called for the first time, see createPanel() */
     private boolean isFirstTime = true;
     
     protected void setUp() throws IOException {
 	tester = new ComponentTester();
 	LogManager.getLogManager().readConfiguration(Main.class.getResourceAsStream("/logging.properties"));
     }
     protected void tearDown() {
 	// Default JUnit test runner keeps references to Tests for its
 	// lifetime, so TestCase fields won't be GC'd for the lifetime of the
 	// runner. 
 	tester = null;
     }
     
     /** Helper method: return a new ITemplatePanel instance.
      * <p>
      * This is to be overridden by the implementation because it needs
      * to create the class specific to that implementation.
      */
     protected abstract ITemplatePanel createPanel();
     /** Helper method: show window frame.
      * <p>
      * This show the frame, creating one when needed. 
      */
     protected Window showFrame(ITemplatePanel panel) {
 	if (panel instanceof Window) {
 	    showWindow((Window)panel);
 	    return ((Window)panel);
 	} else {
 	    return showFrame((Component)panel);
 	}
     }
     
     /** Helper method: create template panel from a html body
      * 
      * @throws ParserConfigurationException 
      * @throws IOException 
      * @throws SAXException */
     protected ITemplatePanel createPanel(String body, Properties p) throws SAXException, IOException, ParserConfigurationException {
 	// now create the html panel
 	panel = createPanel();
 	panel.setDocument(TemplateDocumentTest.parseBody(body));
 	if (p!=null)
 	    panel.setData(p);
 	frame = showFrame(panel);
 	frame.setMinimumSize(new Dimension(200, 100));
 	waitForWindow(frame, true);
 	// On Mac OS X I have seen the first test fail because the window wasn't
 	// realised fully even when showFrame returned (It couldn't find any
 	// child component). To work around this, we wait a little when this is
 	// the first time a panel is created.
 	if (isFirstTime) {
 	    sleep();
 	    isFirstTime = false;
 	}
 	
 	return panel;
     }
     protected ITemplatePanel createPanel(String body) throws Exception {
 	return createPanel(body, null);
     }
     /** Helper method: find all components matching the Matcher.
      * <p>
      * This is like
      * {@link ComponentTestFixture#getFinder getFinder}.{@link ComponentFinder#find find}
      * but returns multiple Components instead of only one.
      */
     protected Component[] findMany(Component c, Matcher m) {
 	ArrayList<Component> match = new ArrayList<Component>();
 	Collection<?> children = getHierarchy().getComponents(c);
 	for (Iterator<?> it = children.iterator(); it.hasNext();) {
 	    Component comp = (Component)it.next();
 	    // add component if it matches
 	    if (m.matches(comp))
 		match.add(comp);
 	    // and add any children
 	    Component[] matchingChildren = findMany(comp, m);
 	    if (matchingChildren!=null && matchingChildren.length>0) {
 		for (int i=0; i<matchingChildren.length; i++)
 		    match.add(matchingChildren[i]);
 	    }
 	}
 	return match.toArray(new Component[]{});
     }
     protected Component[] findMany(Matcher m) throws InterruptedException, InvocationTargetException {
 	// invoke and wait for an event in the gui thread, so that really
 	// all events are handled
 	java.awt.EventQueue.invokeAndWait(new Runnable() {
 	    public void run() { }
 	});
 	return findMany((Component)panel, m);
     }
     /** as {@link ComponentFinder#find}
      * <p>
      * This convenience method allows customization or working around bugs
      * in find().
      * 
      * @throws MultipleComponentsFoundException 
      * @throws ComponentNotFoundException 
      * @throws InvocationTargetException 
      * @throws InterruptedException */
     protected Component find(Matcher m) throws MultipleComponentsFoundException, ComponentNotFoundException, InterruptedException, InvocationTargetException {
 	guiSleep();
 	// only then look it up
 	return getFinder().find(m);
     }
     /** Helper method: test the panel's contents 
      * @throws ParserConfigurationException 
      * @throws IOException 
      * @throws SAXException */
     protected boolean bodyEquals(ITemplatePanel panel, String body) throws SAXException, IOException, ParserConfigurationException {
 	return TemplateDocumentTest.bodyEquals(panel.getDocument(), body);
     }
     /** Helper method: wait for AWT queue to finish.
      * <p>
      * This is done a couple of times to really make it work ... :/ */
     protected void guiSleep() throws InterruptedException, InvocationTargetException {
 	guiSleep(3);
     }
     protected void guiSleep(int times) throws InterruptedException, InvocationTargetException {
 	// invoke and wait for an event in the gui thread, so that really
 	// all events are handled
 	for (int i=0; i<times; i++) {
 	    java.awt.EventQueue.invokeAndWait(new Runnable() {
 		public void run() { }
 	    });
 	    sleep();
 	}
     }
     /** Get resource from class. Look in Class's space first for overriding
      * by child classes, fallback to the package this class is in. */
     protected String getResourceURI(String name) {
 	URL url = getClass().getResource(name);
 	if (url!=null) return url.toExternalForm();
 	url = ITemplatePanelTest.class.getResource(name);
 	if (url!=null) return url.toExternalForm();
 	return null;
     }
     /** Returns whether this {@link Component} is a form element or no. */
     protected boolean isFormComponent(Component c) {
 	return	c instanceof AbstractButton &&
 		c instanceof JComboBox &&
 		c instanceof JList &&
 		c instanceof JTextComponent;
     }
     
     /** Test data binding */
     @Test
     public void testData1() throws Exception {
 	createPanel("");
 	assertNotNull(panel.data());
 	assertNull(panel.data().getProperty("foobar"));
 	panel.data().setProperty("foobar", "yeah");
 	assertEquals("yeah", panel.data().getProperty("foobar"));
     }
     /** Test data binding with supplied Properties */
     @Test
     public void testData2() throws Exception {
 	Properties p = new Properties();
 	createPanel("", p);
 	assertEquals(p, panel.data());
 	assertNull(panel.data().getProperty("foobar"));
 	p.setProperty("foobar", "yeah");
 	assertEquals("yeah", panel.data().getProperty("foobar"));	
     }
     /** Test data binding with setData() */
     @Test
     public void testData3() throws Exception {
 	Properties p = new Properties();
 	p.setProperty("foo", "bar");
 	panel = createPanel();
 	panel.setDocument(TemplateDocumentTest.parseBody("<p c='${foo}'/>"));
 	assertTrue(bodyEquals(panel, "<p/>"));
 	panel.setData(p);
 	assertTrue(bodyEquals(panel, "<p>bar</p>"));
     }
     /** Test construction from external page */
     @Test
     public void testData4() throws Exception {
 	panel = createPanel();
 	panel.setDocument(getResourceURI("testData4.html"));
 	assertTrue(bodyEquals(panel, "<p/>"));
 	Properties p = new Properties();
 	p.setProperty("foo", "bar");
 	panel.setData(p);
 	assertTrue(bodyEquals(panel,"<p>bar</p>"));
     }
     /** Test if reloading a page works with external stylesheet.
      * 
      * TODO make sure no exception is thrown down in xhtmlrenderer that is caught internally */
     @Test
     public void testReloadStylesheet() throws Exception {
 	panel = createPanel();
 	panel.setDocument(getResourceURI("testData5.html"));
 	assertTrue(bodyEquals(panel, "<p/>"));
 	panel.refresh();
 	assertTrue(bodyEquals(panel, "<p/>"));
     }
     /** Test stylesheet loaded correctly */
     @Test
     public void testStylesheetApplied() throws Exception {
 	panel = createPanel();
 	panel.setDocument(getResourceURI("testData5.html"));
 	assertTrue(bodyEquals(panel, "<p/>"));
 	// check that stylesheet was indeed loaded
 	Element e = (Element)TemplateDocumentTest.getBody(panel.getDocument()).getFirstChild();
 	assertEquals( FSRGBColor.RED, panel.getSharedContext().getStyle(e).getBackgroundColor());	
     }
     /** Test if loading a page from a jar works including stylesheet
      * 
      * TODO make sure no exception is thrown down in xhtmlrenderer that is caught internally */
     @Test
     public void testLoadFromJar() throws Exception {
 	String url = getResourceURI("testData6.jar");
 	url = "jar:"+url+"!/some/path/testData.html";
 	panel = createPanel();
 	panel.setDocument(url);
 	assertTrue(bodyEquals(panel, "<p/>"));
 	// check that stylesheet was loaded
 	Element e = (Element)TemplateDocumentTest.getBody(panel.getDocument()).getFirstChild();
 	assertEquals( FSRGBColor.RED, panel.getSharedContext().getStyle(e).getBackgroundColor());
     }
     
     
     /** Text input: test if value="foo" in html sets a textfield's value */
     @Test
     public void testTextfieldValue() throws Exception {
 	createPanel(" <input type='text' name='name' value='hi there'/>");
 	JTextField field = (JTextField)find(new ClassMatcher(JTextField.class));
 	assertEquals("hi there", field.getText());
     }
     /** Text input: test if setting a textfield's value from properties works */
     @Test
     public void testTextfieldValueFromProperty() throws Exception {
 	Properties p = new Properties();
 	p.setProperty("name", "second hi there");
 	createPanel(" <input type='text' name='name'/>", p);
 	JTextField field = (JTextField)find(new ClassMatcher(JTextField.class));
 	assertEquals("second hi there", field.getText());
     }
     /** Text input: test if updating a text field updates its property */ 
     @Test
     public void testTextfieldValueUpdate() throws Exception {
 	createPanel("<input type='text' name='name'/>");
 	JTextField field = (JTextField)find(new ClassMatcher(JTextField.class));
 	field.setText("what is this");
 	assertEquals("what is this", panel.data().getProperty("name"));
     }
 
     /** Checkbox: test if unchecked by default */
     @Test
     public void testCheckboxDefault() throws Exception {
 	createPanel("<input type='checkbox' name='foo'/>");
 	JToggleButton btn = (JToggleButton)find(new ClassMatcher(JToggleButton.class));
 	assertFalse(btn.isSelected());
     }
     /** Checkbox: test if checked html attribute makes it checked */
     @Test
     public void testCheckboxValue() throws Exception {
 	createPanel("<input type='checkbox' name='foo' checked='checked'/>");
 	JToggleButton btn = (JToggleButton)find(new ClassMatcher(JToggleButton.class));
 	assertTrue(btn.isSelected());
     }
     /** Checkbox: test if checkbox is set from property (true) */
     @Test
     public void testCheckboxValueFromProperty1() throws Exception {
 	Properties p = new Properties();
 	p.setProperty("foo", "true");
 	createPanel("<input type='checkbox' name='foo'/>", p);
 	JToggleButton btn = (JToggleButton)find(new ClassMatcher(JToggleButton.class));
 	assertTrue(btn.isSelected());
     }
     /** Checkbox: test if checkbox is set from property (false) */
     @Test
     public void testCheckboxValueFromProperty2() throws Exception {
 	Properties p = new Properties();
 	p.setProperty("foo", "false");
 	createPanel("<input type='checkbox' name='foo'/>", p);
 	JToggleButton btn = (JToggleButton)find(new ClassMatcher(JToggleButton.class));
 	assertFalse(btn.isSelected());
     }
     
     /** Radio button: test if none is selected by default */
     @Test
     public void testRadioDefault() throws Exception {
 	createPanel("<input type='radio' name='foo' value='foovalue'/>" +
 	            "<input type='radio' name='foo' value='foovalue2'/>");
 	Component[] btns = findMany(new ClassMatcher(JRadioButton.class));
 	for (int i=0; i<btns.length; i++)
 	    assertFalse(((JRadioButton)btns[i]).isSelected());
     }
     /** Radio button: test if the indicated one is selected */
     @Test
     public void testRadioValue1() throws Exception {
 	createPanel("<input type='radio' name='foo' value='foovalue' checked='checked'/>" +
 	            "<input type='radio' name='foo' value='foovalue2'/>");
 	Component[] btns = findMany(new ClassMatcher(JRadioButton.class));
 	assertTrue(((JRadioButton)btns[0]).isSelected());
 	assertFalse(((JRadioButton)btns[1]).isSelected());
     }
     /** Radio button: test if the other one can be is selected */
     @Test
     public void testRadioValue2() throws Exception {
 	createPanel("<input type='radio' name='foo' value='foovalue'/>" +
 	            "<input type='radio' name='foo' value='foovalue2' checked='checked'/>");
 	Component[] btns = findMany(new ClassMatcher(JRadioButton.class));
 	assertFalse(((JRadioButton)btns[0]).isSelected());
 	assertTrue(((JRadioButton)btns[1]).isSelected());
     }
     /** Radio button: test if property selects the correct radiobutton */
     @Test
     public void testRadioValueFromProperty1() throws Exception {
 	Properties p = new Properties();
 	p.setProperty("foo", "foovalue");
 	createPanel("<input type='radio' name='foo' value='foovalue'/>" +
 	            "<input type='radio' name='foo' value='foovalue2'/>", p);
 	Component[] btns = findMany(new ClassMatcher(JRadioButton.class));
 	assertTrue(((JRadioButton)btns[0]).isSelected());
 	assertFalse(((JRadioButton)btns[1]).isSelected());
     }
     /** Radio button: test if property selects the other radiobutton */
     @Test
     public void testRadioValueFromProperty2() throws Exception {
 	Properties p = new Properties();
 	p.setProperty("foo", "foovalue2");
 	createPanel("<input type='radio' name='foo' value='foovalue'/>" +
 	            "<input type='radio' name='foo' value='foovalue2'/>", p);
 	Component[] btns = findMany(new ClassMatcher(JRadioButton.class));
 	assertFalse(((JRadioButton)btns[0]).isSelected());
 	assertTrue(((JRadioButton)btns[1]).isSelected());
     }
     /** Radio button: test if clicking a button updates the property */
     @Test
     public void testRadioValueUpdate() throws Exception {
 	createPanel("<input type='radio' name='foo' value='foovalue'/>" +
 	            "<input type='radio' name='foo' value='foovalue2'/>");
 	Component[] btns = findMany(new ClassMatcher(JRadioButton.class));
 	assertFalse(((JRadioButton)btns[0]).isSelected());
 	assertFalse(((JRadioButton)btns[1]).isSelected());
 
 	((JRadioButton)btns[0]).setSelected(true);
 	assertEquals("foovalue", panel.data().getProperty("foo"));
 	assertTrue(((JRadioButton)btns[0]).isSelected());
 	assertFalse(((JRadioButton)btns[1]).isSelected());
 
 	((JRadioButton)btns[1]).setSelected(true);
 	assertEquals("foovalue2", panel.data().getProperty("foo"));
 	assertFalse(((JRadioButton)btns[0]).isSelected());
 	assertTrue(((JRadioButton)btns[1]).isSelected());
     }
     
     /* TODO add tests for combobox/list in both modes */
     
     /* TODO add tests for submit, incl. setSubmitAction */
     
     /** Test the readonly attribute */
     @Test
     public void testReadonly() throws Exception {
 	createPanel("<input type='radio' name='ra' readonly='readonly'/>" +
 	            "<input type='checkbox' name='rb' readonly='readonly'/>" +
 	            "<input type='text' name='rc' readonly='readonly'/>" +
 	            "<input type='button' name='rd' readonly='readonly'/>");
 	Component[] comps = findMany(new ClassMatcher(Component.class));
 	for (int i=0; i<comps.length; i++)
 	    assertFalse(isFormComponent(comps[i]) && comps[i].isEnabled());
     }
     /** Test the readonly attribute is unset by default */
     @Test
     public void testNoReadonly() throws Exception {
 	createPanel("<input type='radio' name='a'/>" +
 	            "<input type='checkbox' name='b'/>" +
 	            "<input type='text' name='c'/>" +
 	            "<input type='button' name='d'/>");
 	Component[] comps = findMany(new ClassMatcher(Component.class));
 	for (int i=0; i<comps.length; i++)
 	    assertTrue(!isFormComponent(comps[i]) || comps[i].isEnabled());
     }
     /** Test the readonly attribute set by <tt>.lock</tt> property */
     @Test
     public void testReadonlyFromProperty() throws Exception {
 	Properties p = new Properties();
 	p.setProperty("a.lock", "true");
 	p.setProperty("b.lock", "true");
 	p.setProperty("c.lock", "true");
 	p.setProperty("d.lock", "true");
 	createPanel("<input type='radio' name='a'/>" +
 	            "<input type='checkbox' name='b'/>" +
 	            "<input type='text' name='c'/>" +
 	            "<input type='button' name='d'/>", p);
 	Component[] comps = findMany(new ClassMatcher(Component.class));
 	for (int i=0; i<comps.length; i++)
 	    assertFalse(isFormComponent(comps[i]) && comps[i].isEnabled());
     }
     
     /** Test that dating the properties refreshes the document */
     @Test
     public void testSetDataRefresh() throws Exception {
 	createPanel("<input type='text' name='txt'/>");
 	JTextField field = (JTextField)find(new ClassMatcher(JTextField.class));
 	assertEquals("", field.getText());
 	Properties p = new Properties();
 	p.setProperty("txt", "well");
 	panel.setData(p);
 	// note that swing needs some time before the gui is fully updated
 	field = (JTextField)find(new ClassMatcher(JTextField.class));
 	assertEquals("well", field.getText());
     }
     /** Test that refresh reparses the document */
     @Test
     public void testRefresh() throws Exception {
 	createPanel("<input type='text' name='txt'/>");
 	JTextField field = (JTextField)find(new ClassMatcher(JTextField.class));
 	assertEquals("", field.getText());
 	panel.data().setProperty("txt", "well");
 	panel.refresh();
 	// note that swing needs some time before the gui is fully updated
 	field = (JTextField)find(new ClassMatcher(JTextField.class));
 	assertEquals("well", field.getText());
     }
     
     /** Test that the first form element receives the focus on display and
      *  input can be entered. */
     @Test
     public void testFocusInput() throws Exception {
 	createPanel("<input type='text' name='txt'/>");
 	guiSleep();
 	assertTrue(find(new ClassMatcher(JTextField.class)).hasFocus());
 	// We don't want to need to click the field before input can be entered.
 	// On Windows it does seem to work by default, on Linux it
 	// works only when the focus policy is click-to-focus.
 	//getRobot().click((JTextField)find(new ClassMatcher(JTextField.class)));
 	getRobot().keyString("test input");
 	assertEquals("test input", panel.data().getProperty("txt"));
     }
     /** Test that the first form element receives the focus on display and
      *  input can be entered, and a second one gets it after tab. */
     @Test
     public void testFocusInput2() throws Exception {
 	createPanel("<input type='text' name='txt'/><input type='checkbox' name='chk'/>");
 	guiSleep();
 	// We don't want to need to click the field before input can be entered.
 	// On Windows it does seem to work by default, on Linux it
 	// works only when the focus policy is click-to-focus.
 	//getRobot().click((JTextField)find(new ClassMatcher(JTextField.class)));
 	getRobot().keyString("test input");
 	assertEquals("test input", panel.data().getProperty("txt"));
 	getRobot().keyStroke('\t');
 	assertTrue(find(new ClassMatcher(JToggleButton.class)).hasFocus());
 	getRobot().keyStroke(' ');
 	assertTrue(Boolean.valueOf(panel.data().getProperty("chk")));
     }
 
     /** test page for use by test main */
     protected static final String testBody = 
 	    "<h1>Test page</h1>"+
 	    // check substitution in ordinary attribute
 	    "<p>Check that this points to <a href='${theurl}'>www.w3.org</a>.</p>"+
 	    // check literal conditional
 	    "<p>Basic conditionals are<span style='color:red' if='false'> not</span> "+
 	    "<span style='color:green' if='true'>working</span></p>"+
 	    // check negated conditional on set property
 	    "<p>Variable foo is<span style='color:red' if='!${foo}'> not</span> set</p>"+
 	    // check conditional on set property
 	    "<p>Variable foo is<span style='color:green' if='${foo}'> certainly</span> set</p>"+
 	    // check conditional on unset property
 	    "<p>Variable bar is<span style='color:green' if='!${bar}'> not</span> set</p>"+
 	    // check substitution with set property
 	    "<p>And so foo is set to '<i c='${foo}'></i>', "+
 	    // check substitution with unset property
 	    "while bar is set to '<i c='${bar}'>(removed)</i>' (should be empty).</p>"+
 	    // select
 	    "<p>And a <select name='sel'><option value='bad'>bad</option><option value='selected'>selected</option></select> select box with "+
 	    // radio buttons
 	    "<input type='radio' name='rad' value='one'/>one or <input type='radio' name='rad' value='two'/>two radio buttons</p>"+
 	    // check readonly attribute on form element and value from property
 	    "<form><p><input type='checkbox' readonly='readonly' name='chk' id='chk'/> <label for='chk'>a checked readonly checkbox</label></p>"+
 	    // check that submit button sets property values from elements
 	    "<p>type <input type='text' name='txt' value='**this is bad text**'/> and <input type='submit' name='show' value='submit'/></p></form>"+
 	    // check a locked input element
 	    "<p>this is a <input type='text' name='txtlocked' value='readonly'/> input element</p>"+
 	    // add print button
 	    "<form><p>you can also <input type='submit' name='print' value='print'/> this page.</p></form>"+
 	    // test putting in html from a variable
 	    "<div c='${somehtml}'>hmm, <span style='color:red'>you shouldn't see this text</span></div>"+
 	    // and some conditional expression tests
 	    "<p>Nothing should be shown after the colon: <span style='color:red'>" +
 	    "<span if='false'>1</span> " +
 	    "<span if='!true'>2</span> " +
 	    "<span if='(false)'>3</span> " +
 	    "<span if='(!true)'>4</span> " +
 	    "<span if='!(true)'>5</span> " +
 	    "<span if='((false))'>6</span> " +
 	    "<span if='!(!(false))'>7</span> " +
 	    "<span if='false and true'>8</span> " +
 	    "<span if='!(false or true)'>9</span> " +
 	    "<span if='(false or true) and false'>A</span> " +
 	    // now (partly simulated) variable expansion
 	    "<span if=''>B</span> " +
 	    "<span if=' '>C</span> " +
 	    "<span if='${_nothing_}'>D</span> " +
 	    "<span if='(${_nothing_})'>E</span> " +
 	    "<span if='${_nothing_} and true'>F</span> " +
 	    "<span if='true and ${_nothing_}'>G</span> " +
 	    "<span if='${_nothing_} or ${_nothingelse_}'>H</span> " +
 	    "<span if='something and false'>I</span> " +
 	    "<span if='!something'>J</span> " +
 	    "</span></p>";
     protected static final String testPage =
 	    "<html>"+
 	    "<head><title>Test page</title></head>"+
 	    "<body>"+
 	    testBody +
 	    "</body>"+
 	    "</html>";	    
     /** set test pane for use with {@linkplain #testPage} */
     protected static void setTestPane(Window wnd, ITemplatePanel pane) {
 	pane.data().setProperty("foo", "the contents of this foo variable");
 	pane.data().setProperty("theurl", "http://www.w3.org/");
 	pane.data().setProperty("chk", "true");
 	pane.data().setProperty("txt", "some text");
 	pane.data().setProperty("txtlocked.lock", "true");
 	pane.data().setProperty("sel", "selected");
 	pane.data().setProperty("somehtml", "you should see <em>this</em> text");
 	// don't set "bar"
 
 	pane.setSubmitAction(new AbstractAction() {
 	    public void actionPerformed(ActionEvent e) {
 		ITemplatePanel pane = (ITemplatePanel)e.getSource();
 		if (e.getActionCommand().equals("show"))
 		    JOptionPane.showMessageDialog(null,
 			    "Checkbox: "+pane.data().getProperty("chk")+"\n"+
 			    "Selection: "+pane.data().getProperty("sel")+"\n"+
 			    "Radio: "+pane.data().getProperty("rad")+"\n"+
 			    "Text: "+pane.data().getProperty("txt"),
 			    "Form submitted",
 			    JOptionPane.INFORMATION_MESSAGE);
 		else if (e.getActionCommand().equals("print"))
 		    try {
 			pane.print();
 		    } catch (PrinterException e1) {
 			e1.printStackTrace();
 		    }
 	    }
 	});
     }
     
     /** An interactive test to play with */
    public static void main(ITemplatePanel spane, String[] args) throws Exception {
	final ITemplatePanel pane = spane; // for inner class reference
 	DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
 	pane.setDocument(builder.parse(new ByteArrayInputStream(testPage.getBytes())));
 	((Component)pane).setPreferredSize(new Dimension(500, 600));
 	Window wnd;
 	if (pane instanceof Window) {
 	    wnd = (Window)pane;
 	} else {
 	    JFrame frame = new JFrame();
 	    frame.getContentPane().add(new JScrollPane((Component)pane));
 	    frame.setTitle("TemplatePane Test");
 	    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	    wnd = frame;
 	}
	setTestPane(wnd, spane);
 	wnd.setSize(((Component)pane).getPreferredSize());
 	wnd.pack();
 	wnd.setVisible(true);
     }
 }
