 /**
  * Warlock, the open-source cross-platform game client
  *  
  * Copyright 2008, Warlock LLC, and individual contributors as indicated
  * by the @authors tag. 
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package cc.warlock.rcp.prefs;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.jface.bindings.keys.KeyStroke;
 import org.eclipse.jface.bindings.keys.SWTKeySupport;
 import org.eclipse.jface.fieldassist.IContentProposal;
 import org.eclipse.jface.fieldassist.IContentProposalProvider;
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.CellEditor;
 import org.eclipse.jface.viewers.EditingSupport;
 import org.eclipse.jface.viewers.ILabelProviderListener;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerFilter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.IWorkbenchPropertyPage;
 import org.eclipse.ui.part.PageBook;
 
 import cc.warlock.core.client.IWarlockClient;
 import cc.warlock.core.client.settings.internal.ClientSettings;
 import cc.warlock.core.client.settings.macro.CommandMacroHandler;
 import cc.warlock.core.client.settings.macro.IMacro;
 import cc.warlock.core.client.settings.macro.IMacroCommand;
 import cc.warlock.core.client.settings.macro.IMacroHandler;
 import cc.warlock.core.client.settings.macro.IMacroProvider;
 import cc.warlock.core.client.settings.macro.internal.Macro;
 import cc.warlock.rcp.ui.ContentAssistCellEditor;
 import cc.warlock.rcp.ui.KeyStrokeCellEditor;
 import cc.warlock.rcp.ui.KeyStrokeText;
 import cc.warlock.rcp.ui.WarlockSharedImages;
 import cc.warlock.rcp.ui.KeyStrokeText.KeyStrokeLockListener;
 
 /**
  *
  * @author Marshall Culpepper
  */
 public class MacrosPreferencePage extends PreferencePageUtils implements
 		IWorkbenchPropertyPage {
 
 	public static final String PAGE_ID = "cc.warlock.rcp.prefs.macros";
 	
 	protected static String COLUMN_COMMAND = "command";
 	protected static String COLUMN_KEY = "key";
 	
 	protected TableViewer macroTableView;
 	protected IWarlockClient client;
 	protected ClientSettings settings;
 	
 	protected ArrayList<Macro> macros = new ArrayList<Macro>();
 
 	protected Button addMacroButton;
 	protected Button removeMacroButton;
 	protected Button clearMacrosButton;
 	protected Button defaultMacrosButton;
 	
 	protected Macro selectedMacro;
 	protected PageBook filterBook;
 
 	protected Text commandText;
 	protected KeyStrokeText keyComboText;
 	protected Menu filterMenu;
 	protected boolean filterByCommand = true;
 	
 	@Override
 	protected Control createContents(Composite parent) {
 		Composite main = new Composite(parent, SWT.NONE);
 		main.setLayout(new GridLayout(2, false));
 		
 		Composite filterComposite = new Composite(main, SWT.NONE);
 		GridLayout layout = new GridLayout(3, false);
 		layout.marginWidth = layout.marginHeight = 0;
 		filterComposite.setLayout(layout);
 		GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);
 //		data.horizontalSpan = 2;
 		filterComposite.setLayoutData(data);
 		
 		new Label(filterComposite, SWT.NONE).setText("Filter: ");
 	
 		filterBook = new PageBook(filterComposite, SWT.NONE);
 		filterBook.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
 		
 		commandText = new Text(filterBook, SWT.BORDER);
 		commandText.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				macroTableView.refresh();
 			}
 		});
 		
 		keyComboText = new KeyStrokeText(filterBook, SWT.BORDER);
 		keyComboText.addKeyStrokeLockListener(new KeyStrokeLockListener() {
 			public void keyStrokeLocked() {
 				macroTableView.refresh();
 			}
 			public void keyStrokeUnlocked() {
 				macroTableView.refresh();
 			}
 		});
 		
 		filterBook.showPage(commandText);
 
 		Button filterButton = new Button(filterComposite, SWT.ARROW | SWT.DOWN);
 		filterMenu = new Menu(filterButton);
 		filterButton.addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event event) {
 				filterMenu.setVisible(true);
 			}
 		});
 		
 		MenuItem filterByCommand = new MenuItem(filterMenu, SWT.RADIO);
 		filterByCommand.setText("Filter by command");
 		filterByCommand.setSelection(true);
 		filterByCommand.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				MacrosPreferencePage.this.filterByCommand = true;
 				filterBook.showPage(commandText);
 				macroTableView.refresh();
 			}
 		});
 		
 		MenuItem filterByKeyCombo= new MenuItem(filterMenu, SWT.RADIO);
 		filterByKeyCombo.setText("Filter by key combo");
 		filterByKeyCombo.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				MacrosPreferencePage.this.filterByCommand = false;
 				filterBook.showPage(keyComboText.getText());
 				macroTableView.refresh();
 			}
 		});
 		filterButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.FILL, false, false));
 		
 		new Label(main, SWT.NONE);
 		macroTableView = new TableViewer(main, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
 		TableViewerColumn commandColumn = new TableViewerColumn(macroTableView, SWT.NONE, 0);
 		commandColumn.getColumn().setText("Command");
 		commandColumn.getColumn().setWidth(225);
 		commandColumn.setEditingSupport(new CommandEditingSupport(macroTableView));
 		
 		TableViewerColumn keyColumn = new TableViewerColumn(macroTableView, SWT.NONE, 1);
 		keyColumn.getColumn().setText("Key Combination");
 		keyColumn.getColumn().setWidth(125);
 		keyColumn.setEditingSupport(new KeyStrokeEditingSupport(macroTableView));
 		
 		macroTableView.setUseHashlookup(true);
 		macroTableView.setColumnProperties(new String[] {COLUMN_COMMAND, COLUMN_KEY});
 		macroTableView.setContentProvider(new ArrayContentProvider());
 		macroTableView.setLabelProvider(new LabelProvider());
 		macroTableView.addFilter(new ViewerFilter() {
 			public boolean select(Viewer viewer, Object parentElement, Object element) {
 				IMacro macro = (IMacro) element;
 				Collection<IMacroHandler> handlers = macro.getHandlers();
 				
 				if (handlers.size() == 1) {
 					IMacroHandler handler = (IMacroHandler) handlers.toArray()[0];
 					if (handler instanceof CommandMacroHandler) {
 						return true;
 					}
 				}
 				return false;
 			}	
 		});
 		macroTableView.addFilter(new MacroFilter());
 		
 		macroTableView.addSelectionChangedListener(new ISelectionChangedListener() {
 			public void selectionChanged(SelectionChangedEvent event) {
 				if (macroTableView.getSelection().isEmpty()) {
 					removeMacroButton.setEnabled(false);
 				} else {
 					selectedMacro = (Macro) ((IStructuredSelection)macroTableView.getSelection()).getFirstElement();
 					removeMacroButton.setEnabled(true);
 				}
 			}
 		});
 		
 		for (IMacro macro : settings.getAllMacros()) {
 			if (macro instanceof Macro) {
 				macros.add(new Macro((Macro)macro));
 			}
 		}
 		
 		macroTableView.setInput(macros);
 		macroTableView.getTable().setHeaderVisible(true);
 		int listHeight = macroTableView.getTable().getItemHeight() * 8;
 		Rectangle trim = macroTableView.getTable().computeTrim(0, 0, 0, listHeight);
 		data = new GridData(GridData.FILL, GridData.FILL, true, true);
 		data.heightHint = trim.height;
 		
 		macroTableView.getTable().setLayoutData(data);
 		
 		Composite macroButtons = new Composite(main, SWT.NONE);
 		macroButtons.setLayout(new GridLayout(1, true));
 		macroButtons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
 		
 		addMacroButton = new Button(macroButtons, SWT.PUSH);
 		addMacroButton.setText("Add Macro");
 		addMacroButton.setImage(WarlockSharedImages.getImage(WarlockSharedImages.IMG_ADD));
 		addMacroButton.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				addMacroSelected();
 			}
 		});
 		addMacroButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
 		
 		removeMacroButton = new Button(macroButtons, SWT.PUSH);
 		removeMacroButton.setText("Remove Macro");
 		removeMacroButton.setImage(WarlockSharedImages.getImage(WarlockSharedImages.IMG_REMOVE));
 		removeMacroButton.setEnabled(false);
 		removeMacroButton.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				removeMacroSelected();
 			}
 		});
 		removeMacroButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
 		
 		Label filler = new Label(macroButtons, SWT.NONE);
 		filler.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));	
 		
 		clearMacrosButton = new Button(macroButtons, SWT.PUSH);
 		clearMacrosButton.setText("Clear Macros");
 		clearMacrosButton.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				clearMacros();
 			}
 		});
 		clearMacrosButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
 		
 		defaultMacrosButton = new Button(macroButtons, SWT.PUSH);
 		defaultMacrosButton.setText("Reset to Defaults");
 		defaultMacrosButton.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				setupDefaultMacros();
 			}
 		});
 		defaultMacrosButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
 		
 		
 		return main;
 	}
 
 	protected class MacroFilter extends ViewerFilter {
 		public boolean select(Viewer viewer, Object parentElement, Object element) {
 			IMacro macro = (IMacro)element;
 			
 			if (filterByCommand) {
 				String command = getCommandMacroHandler(macro).getCommand();
 				
 				if (command.equals("")) {
 					return true;
 				}
 				
 				return command.toLowerCase().contains(commandText.getText().toLowerCase());
 			} else {
 				KeyStroke stroke = keyComboText.getKeyStroke();
 				if (stroke != null && stroke.getNaturalKey() != KeyStroke.NO_KEY) {
 					return (stroke.getModifierKeys() == macro.getModifiers() && stroke.getNaturalKey() == macro.getKeyCode());
 				}
 				return true;
 			}
 		}
 	}
 	
 	protected ArrayList<Macro> addedMacros = new ArrayList<Macro>();
 	protected void addMacroSelected ()
 	{
 		Macro macro = new Macro(settings.getMacroConfigurationProvider(), 0);
 		macro.addHandler(new CommandMacroHandler(""));
 		
 		addedMacros.add(macro);
 		macros.add(macro);
 		macroTableView.add(macro);
 		
 		macroTableView.editElement(macro, 0);
 	}
 	
 	protected ArrayList<Macro> removedMacros = new ArrayList<Macro>();
 
 	protected void removeMacroSelected ()
 	{
 		addedMacros.remove(selectedMacro);
 		
 		if (macros.remove(selectedMacro)) {
 			removedMacros.add(selectedMacro);
 		}
 		
 		macroTableView.remove(selectedMacro);
 	}
 	
 	
 	protected void setupDefaultMacros() {
 		// There probably is a better place to put this.
 		clearMacros();
 		createRawMacro("\\xxml toggle containers\\r",99,65536);
 		createRawMacro("\\xxml toggle dialogs\\r",100,65536);
 		createRawMacro("{ExportDialog}",101,327680);
 		createRawMacro("{HighlightsDialog}",104,327680);
 		createRawMacro("{ImportDialog}",105,327680);
 		createRawMacro("{ToggleLinks}",108,65536);
 		createRawMacro("{ToggleMusic}",109,65536);
 		createRawMacro("{ToggleImages}",105,65536);
 		createRawMacro("{ToggleSounds}",115,65536);
 		createRawMacro("{MacrosDialog}",109,327680);
 		createRawMacro("{ChooseSkin}",115,327680);
 		createRawMacro("{VariablesDialog}",118,65536);
 		createRawMacro("{MacroSet}0",48,65536);
 		createRawMacro("{MacroSet}1",49,65536);
 		createRawMacro("{MacroSet}2",50,65536);
 		createRawMacro("{MacroSet}3",51,65536);
 		createRawMacro("{MacroSet}4",52,65536);
 		createRawMacro("{MacroSet}5",53,65536);
 		createRawMacro("{MacroSet}6",54,65536);
 		createRawMacro("{MacroSet}7",55,65536);
 		createRawMacro("{MacroSet}8",56,65536);
 		createRawMacro("{MacroSet}9",57,65536);
 		createRawMacro("{Restart}",16777230,262144);
 		createRawMacro("\\xretreat\\r",114,262144);
 		createRawMacro("\\xlook\\r",16777259,0);
 		createRawMacro("\\xhealth\\r",16777263,0);
 		createRawMacro("\\xnotoriety\\r",16777258,0);
 		createRawMacro("\\xmana\\r",16777261,0);
 		createRawMacro("\\xup\\r",16777262,0);
 		createRawMacro("\\xdown\\r",16777264,0);
 		createRawMacro("\\xsw\\r",16777265,0);
 		createRawMacro("\\xs\\r",16777266,0);
 		createRawMacro("\\xse\\r",16777267,0);
 		createRawMacro("\\xw\\r",16777268,0);
 		createRawMacro("\\xout\\r",16777269,0);
 		createRawMacro("\\xe\\r",16777270,0);
 		createRawMacro("\\xnw\\r",16777271,0);
 		createRawMacro("\\xn\\r",16777272,0);
 		createRawMacro("\\xne\\r",16777273,0);
 		createRawMacro("{PageUp}",16777221,0);
 		createRawMacro("{PageDown}",16777222,0);
 		createRawMacro("{LineUp}",16777221,131072);
 		createRawMacro("{LineDown}",16777222,131072);
 		createRawMacro("{HistoryPrev}",16777217,0);
 		createRawMacro("{HistoryNext}",16777218,0);
 		createRawMacro("{RepeatLast}",13,262144);
 		createRawMacro("{RepeatSecondToLast}",13,65536);
 		createRawMacro("{ReturnOrRepeatLast}",16777296,0);
 		createRawMacro("{RepeatLast}",16777296,262144);
 		createRawMacro("{RepeatSecondToLast}",16777296,65536);
 		createRawMacro("{CycleWindows}",9,0);
 		createRawMacro("{BufferTop}",16777221,262144);
 		createRawMacro("{BufferBottom}",16777222,262144);
 		createRawMacro("{BufferTop}",16777223,262144);
 		createRawMacro("{BufferBottom}",16777224,262144);
 		createRawMacro("{CycleWindowsReverse}",9,131072);
 		createRawMacro("\\xassess\\r",97,262144);
 		createRawMacro("{copy}",99,262144);
 		createRawMacro("{cut}",120,262144);
 		createRawMacro("{paste}",118,262144);
 		createRawMacro("{PauseScript}",27,131072);
 		createRawMacro("peer down\\r",16777264,262144);
 		createRawMacro("\\xpath focus damage\\r",16777265,131072);
 		createRawMacro("\\xaft to port\\r",16777265,65536);
 		createRawMacro("peer sw\\r",16777265,262144);
 		createRawMacro("\\xpath focus quick\\r",16777266,131072);
 		createRawMacro("\\xaft\\r",16777266,65536);
 		createRawMacro("peer s\\r",16777266,262144);
 		createRawMacro("\\xpath focus ease\\r",16777267,131072);
 		createRawMacro("\\xaft to starboard\\r",16777267,65536);
 		createRawMacro("peer se\\r",16777267,262144);
 		createRawMacro("\\xport\\r",16777268,65536);
 		createRawMacro("peer w\\r",16777268,262144);
 		createRawMacro("peer out\\r",16777269,262144);
 		createRawMacro("\\xstarboard\\r",16777270,65536);
 		createRawMacro("peer e\\r",16777270,262144);
 		createRawMacro("\\xforward to port\\r",16777271,65536);
 		createRawMacro("peer nw\\r",16777271,262144);
 		createRawMacro("\\xforward\\r",16777272,65536);
 		createRawMacro("peer n\\r",16777272,262144);
 		createRawMacro("\\xforward to starboard\\r",16777273,65536);
 		createRawMacro("peer ne\\r",16777273,262144);
 		createRawMacro("peer up\\r",16777262,262144);
 		createRawMacro("\\xpath sense\\r",16777263,131072);
 		createRawMacro("\\xpath check\\r",16777258,131072);
 		createRawMacro("\\xdemeanor neutral\\r",101,65536);
 		createRawMacro("\\xdemeanor reserved\\r",119,65536);
 		createRawMacro("\\xdemeanor cold\\r",113,65536);
 		createRawMacro("\\xget \\?\\rput \\? in my %container\\r",116,262144);
 		createRawMacro("\\xremove my %helmet\\rput my %helmet in my %container\\r\\premove my %gloves\\rput my %gloves in my %container\\r",16777235,0);
 		createRawMacro("\\xopen my \\?\\ropen my %gpouch\\r\\pfill my %gpouch with my \\?\\r\\pclose my %gpouch\\rclose my \\?\\r",16777228,0);
 		createRawMacro("\\xopen my %gpouch\\rappr my %gpouch\\r\\p\\p\\p\\p\\p\\p\\p\\p\\p\\xclose my %gpouch\\r",16777230,0);
 		createRawMacro("\\xopen my \\?\\rl in my \\?\\r\\pclose my \\?\\r",16777231,0);
 		createRawMacro("\\xget my %helmet\\rwear my %helmet\\r\\pget my %gloves\\rwear my %gloves\\r",16777234,0);
 		createRawMacro("\\xdemeanor friendly\\r",114,65536);
 		createRawMacro("\\xdemeanor warm\\r",116,65536);
 		createRawMacro("\\xpath focus power\\r",16777268,131072);
 		createRawMacro("\\xremove my %shield\\rstow my %shield\\r",16777235,262144);
 		createRawMacro("\\xget my %shield\\rwear my %shield\\r",16777234,262144);
 	}
 	
 	// Mostly just to support setupDefaultMacros
 	protected void createRawMacro(String cmd, int keycode, int keymod) {
 		Macro macro = new Macro(settings.getMacroConfigurationProvider(), 0);
 		macro.setModifiers(keymod);
 		macro.setKeyCode(keycode);
 		macro.addHandler(new CommandMacroHandler(cmd));
 		
 		addedMacros.add(macro);
 		macros.add(macro);
 		macroTableView.add(macro);
 	}
 	
 	protected void clearMacros() {
 		// Clear out all exiting macros.
 		Table macroTable = macroTableView.getTable();
 		if(macroTable != null)
 			macroTable.clearAll();
 		addedMacros.clear();
 		for (Macro currentMacro: macros) {
			// System Macros have no handlers, and shouldn't be removed.
			if (!currentMacro.getHandlers().isEmpty()) {
				macroTableView.remove(currentMacro);
				removedMacros.add(currentMacro);
			}
 		}
 	}
 	
 	protected CommandMacroHandler getCommandMacroHandler (IMacro macro) {
 		return (CommandMacroHandler)macro.getHandlers().toArray()[0];
 	}
 	
 	protected class LabelProvider implements ITableLabelProvider
 	{
 		public void addListener(ILabelProviderListener listener) {}
 		public void dispose() {}
 		public Image getColumnImage(Object element, int columnIndex) {
 			return null;
 		}
 		public String getColumnText(Object element, int columnIndex) {
 			IMacro macro = (IMacro) element;
 			if (columnIndex == 0) {
 				return getCommandMacroHandler(macro).getCommand();
 			} else {
 				
 				return SWTKeySupport.getKeyFormatterForPlatform().format(KeyStroke.getInstance(macro.getModifiers(), macro.getKeyCode()));
 			}
 		}
 		public boolean isLabelProperty(Object element, String property) {
 			return true;
 		}
 		public void removeListener(ILabelProviderListener listener) {}
 	}
 	
 	protected class CommandEditingSupport extends EditingSupport implements IContentProposalProvider
 	{
 		protected ContentAssistCellEditor editor;
 		
 		public CommandEditingSupport (TableViewer viewer) {
 			super(viewer);
 			
 			editor = new ContentAssistCellEditor(macroTableView.getTable(), SWT.SINGLE, new char[] { '{', '$', '\\' }, this);
 		}
 		
 		protected boolean canEdit(Object element) {
 			return true;
 		}
 		
 		protected CellEditor getCellEditor(Object element) { 
 			return editor;
 		}
 		
 		protected Object getValue(Object element) {
 			return getCommandMacroHandler((IMacro)element).getCommand();
 		}
 		
 		protected void setValue(Object element, Object value) {
 			getCommandMacroHandler((IMacro)element).setCommand((String)value);
 			((Macro)element).setNeedsUpdate(true);
 			
 			macroTableView.update(element, null);
 		}
 		
 		protected class MacroCommandContentProposal implements IContentProposal
 		{
 			protected IMacroCommand command;
 			protected String contents;
 			protected int position;
 		
 			public MacroCommandContentProposal (IMacroCommand command, String contents, int position)
 			{
 				this.command = command;
 				this.contents = contents;
 				this.position = position;
 			}
 			
 			public String getContent() {
 				String content = "{" + this.command.getIdentifier() + "}";
 				
 				int leftBracketIndex = contents.substring(0, position).lastIndexOf('{');
 				String text = contents.substring(0, leftBracketIndex) + content + contents.substring(position);
 				
 				return text;
 			}
 			
 			public int getCursorPosition() {
 				return getContent().length();
 			}
 			
 			public String getDescription() {
 				return command.getDescription();
 			}
 			
 			public String getLabel() {
 				return "{"+command.getIdentifier()+"}";
 			}
 		}
 		
 		public IContentProposal[] getProposals(String contents, int position) {
 			ArrayList<IContentProposal> proposals = new ArrayList<IContentProposal>();
 			
 			int lastRightBracket = contents.substring(0, position).lastIndexOf('{');
 			
 			if (lastRightBracket >= 0) {
 				String commandSubstr = contents.substring(lastRightBracket+1, position).toLowerCase();
 				
 				for (IMacroCommand command : settings.getAllMacroCommands()) {
 					if (command.getIdentifier().toLowerCase().startsWith(commandSubstr)) {
 						proposals.add(new MacroCommandContentProposal(
 							command, contents.substring(0, contents.length()-1), position-1));
 					}
 				}
 			}
 			
 			return proposals.toArray(new IContentProposal[proposals.size()]);
 		}
 	}
 	
 	protected class KeyStrokeEditingSupport extends EditingSupport
 	{
 		protected KeyStrokeCellEditor editor;
 		
 		public KeyStrokeEditingSupport (TableViewer viewer)
 		{
 			super(viewer);
 			
 			this.editor = new KeyStrokeCellEditor(macroTableView.getTable(), SWT.SINGLE);
 		}
 		
 		protected boolean canEdit(Object element) {
 			return true;
 		}
 
 		protected CellEditor getCellEditor(Object element) {
 			return editor;
 		}
 
 		protected Object getValue(Object element) {
 			Macro macro = (Macro) element;
 			return KeyStroke.getInstance(macro.getModifiers(), macro.getKeyCode());
 		}
 
 		protected void setValue(Object element, Object value) {
 			Macro macro = (Macro) element;
 			KeyStroke stroke = (KeyStroke) value;
 			
 			macro.setModifiers(stroke.getModifierKeys());
 			macro.setKeyCode(stroke.getNaturalKey());
 			
 			macroTableView.update(macro, null);
 		}	
 	}
 
 	@Override
 	public void setElement(IAdaptable element) {
 		client = (IWarlockClient)element.getAdapter(IWarlockClient.class);
 		settings = (ClientSettings)client.getClientSettings();
 	}
 	
 	@Override
 	public boolean performOk() {
 		for (Macro macro : macros) {
 			if (macro.needsUpdate() && !addedMacros.contains(macro)) {
 				IMacroProvider provider = (IMacroProvider) macro.getProvider();
 				provider.replaceMacro(macro.getOriginalMacro(), macro);
 			}
 		}
 		
 		for (Macro macro : removedMacros) {
 			IMacroProvider provider = (IMacroProvider) macro.getProvider();
 			provider.removeMacro(macro.getOriginalMacro());
 		}
 		
 		for (Macro macro : addedMacros) {
 			settings.getMacroConfigurationProvider().addMacro(macro);
 		}
 		
 		return true;
 	}
 }
