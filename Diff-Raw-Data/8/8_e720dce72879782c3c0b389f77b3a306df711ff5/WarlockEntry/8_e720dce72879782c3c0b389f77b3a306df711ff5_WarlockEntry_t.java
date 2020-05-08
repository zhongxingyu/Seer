 package cc.warlock.rcp.ui;
 
 import java.util.ArrayList;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.ST;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.custom.VerifyKeyListener;
 import org.eclipse.swt.events.KeyListener;
 import org.eclipse.swt.events.VerifyEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Composite;
 
 import cc.warlock.core.client.ICommand;
 import cc.warlock.core.client.IWarlockClientViewer;
 import cc.warlock.rcp.ui.macros.IMacro;
 import cc.warlock.rcp.ui.macros.MacroRegistry;
 
 public class WarlockEntry implements VerifyKeyListener {
 
 	private StyledText widget;
 	private IWarlockClientViewer viewer;
 	private boolean searchMode = false;
 	private StringBuffer searchText = new StringBuffer();
 	private String searchCommand = "";
 	
 	public WarlockEntry(Composite parent, IWarlockClientViewer viewer) {
 		this.viewer = viewer;
 		widget = new StyledText(parent, SWT.BORDER | SWT.SINGLE);
 		widget.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 1, 1));
 		widget.setEditable(true);
 		widget.setLineSpacing(2);
 		widget.addVerifyKeyListener(this);
 	}
 	
 	public String getText() {
 		if(searchMode)
 			return searchCommand;
 		else
 			return widget.getText();
 	}
 	
 	public void setText(String text) {
 		widget.setText(text);
 		widget.setCaretOffset(text.length());
 	}
 	
 	public void setSelection(int start) {
 		widget.setSelection(start);
 	}
 	
 	public void addKeyListener(KeyListener listener) {
 		widget.addKeyListener(listener);
 	}
 	
 	protected boolean isKeypadKey (int code) {
 		return (code == SWT.KEYPAD_0 || code == SWT.KEYPAD_1 || code == SWT.KEYPAD_2
 				|| code == SWT.KEYPAD_3 || code == SWT.KEYPAD_4 || code == SWT.KEYPAD_5
 				|| code == SWT.KEYPAD_6 || code == SWT.KEYPAD_7 || code == SWT.KEYPAD_8
 				|| code == SWT.KEYPAD_9 || code == SWT.KEYPAD_ADD || code == SWT.KEYPAD_CR
 				|| code == SWT.KEYPAD_DECIMAL || code == SWT.KEYPAD_DIVIDE || code == SWT.KEYPAD_EQUAL
 				|| code == SWT.KEYPAD_EQUAL || code == SWT.KEYPAD_MULTIPLY || code == SWT.KEYPAD_SUBTRACT);
 	}
 
 	public StyledText getWidget() {
 		return widget;
 	}
 	
 	public void verifyKey(VerifyEvent e) {
 		//System.out.println("got char \"" + e.character + "\"");
 		for (IMacro macro : MacroRegistry.instance().getMacros())
 		{
 			if (macro.getKeyCode() == e.keyCode && macro.getModifiers() == e.stateMask)
 			{
 				try {
 					leaveSearchMode();
 					macro.execute(viewer);
 					//keyHandled = true;
 				} catch (Exception ex) {
 					// TODO Auto-generated catch block
 					ex.printStackTrace();
 				}
 
 				e.doit = false;
 				return;
 			}
 		}
 		
 		if (!widget.isFocusControl() || searchMode) {
 			if(entryCharacters.contains(e.character))
 			{
 				append(e.character);
 				e.doit = false;
				if(!widget.isFocusControl())
					widget.setFocus();
 			} else if(e.character == '\b') {
 				if(searchMode) {
 					searchText.setLength(searchText.length() - 1);
 					search();
 				} else {
 					widget.invokeAction(ST.DELETE_PREVIOUS);
 				}
 				e.doit = false;
				if(!widget.isFocusControl())
					widget.setFocus();
 			}
 		}
 	}
 	
 	private void leaveSearchMode() {
 		if(searchMode) {
 			 searchMode = false;
 			 setText(searchCommand);
 			 searchCommand = "";
 			 searchText.setLength(0);
 		 }
 	}
 	
 	public void append(char ch) {
 		if(searchMode) {
 			searchText.append(ch);
 			search();
 		} else {
 			int offset = widget.getCaretOffset();
 			widget.insert(String.valueOf(ch));
 			widget.setCaretOffset(offset + 1);
 		}
 	}
 	
 	private void search() {
 		ICommand foundCommand = viewer.getWarlockClient().getCommandHistory().search(searchText.toString());
 		if(foundCommand != null) {
 			searchCommand = foundCommand.getCommand();
 		}
 		setSearchText();
 	}
 	
 	private static final char[] entryChars = new char[] {
 		'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
 		'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
 		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
 		'.', '/', '{', '}', '<', '>', ',', '?', '\'', '"', ':', ';', '[', ']', '|', '\\', '-', '_', '+', '=',
 		'~', '`', '!', '@', '#', '$', '%', '^', '&', '*', '(', ')', ' '
 	};
 	
 	private static final ArrayList<Character> entryCharacters = new ArrayList<Character>();
 	static {
 		for (char c : entryChars) {
 			entryCharacters.add(c);
 		}
 	}
 	
 	public void prevCommand() {
 		ICommand prevCommand = viewer.getWarlockClient().getCommandHistory().prev();
 		
 		if(prevCommand != null)
 			setText(prevCommand.getCommand());
 		else
 			setText("");
 	}
 	
 	public void nextCommand() {
 		ICommand nextCommand = viewer.getWarlockClient().getCommandHistory().next();
 		if(nextCommand != null) {
 			setText(nextCommand.getCommand());
 		} else {
 			setText("");
 		}
 	}
 	
 	public void searchHistory() {
 		if(searchMode) {
 			ICommand foundCommand = viewer.getWarlockClient().getCommandHistory().searchBefore(searchText.toString());
 			if(foundCommand != null) {
 				searchCommand = foundCommand.getCommand();
 			}
 			setSearchText();
 		} else {
 			searchMode = true;
 			setSearchText();
 		}
 	}
 	
 	private void setSearchText() {
 		widget.setText("(reverse history search)'" + searchText.toString() + "': " + searchCommand);
 	}
 	
 	public void submit() {
 		String command;
 		if(searchMode) {
 			command = searchCommand;
 			leaveSearchMode();
 		} else {
 			command = widget.getText();
 		}
 		viewer.getWarlockClient().send(command);
 		viewer.getWarlockClient().getCommandHistory().addCommand(command);
 		viewer.getWarlockClient().getCommandHistory().resetPosition();
 		setText("");
 	}
 	
 	public void repeatLastCommand() {
 		if (viewer.getWarlockClient().getCommandHistory().size() >= 1) {
 			ICommand command = viewer.getWarlockClient().getCommandHistory().getLastCommand();
 			viewer.getWarlockClient().send(command.getCommand());
 		}
 	}
 	
 	public void repeatSecondToLastCommand() {
 		if (viewer.getWarlockClient().getCommandHistory().size() >= 2)
 		{
 			ICommand command = viewer.getWarlockClient().getCommandHistory().getCommandAt(1);
 			viewer.getWarlockClient().send(command.getCommand());
 		}
 	}
 }
