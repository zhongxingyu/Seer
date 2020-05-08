 package com.managehelper.ui.frames;
 
 import com.managehelper.model.ApplicationContext;
 import com.managehelper.model.Team;
 import com.managehelper.model.TeamRateEvaluator;
 import com.managehelper.ui.CloseListener;
 import com.managehelper.ui.ManageFrame;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.ControlEditor;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.custom.TableCursor;
 import org.eclipse.swt.events.*;
 import org.eclipse.swt.widgets.*;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import static com.managehelper.ui.UiUtils.addSelectionListener;
 import static com.managehelper.ui.UiUtils.setCustomHeight;
 import static java.lang.Double.valueOf;
 
 public class TableFrame implements ManageFrame<ApplicationContext> {
 
     protected Shell shell;
     List<TeamBoard> tables = new ArrayList<TeamBoard>();
     //private List<Integer[][]> integers = new ArrayList<Integer[][]>();
 
     public TableFrame() {
     }
 
     public static void addMouseListener(final Table table, final TableCursor cursor, final ControlEditor editor) {
         cursor.addMouseListener(new MouseAdapter() {
             @Override
             public void mouseUp(MouseEvent e) {
                 final Text text = new Text(cursor, SWT.NONE);
                 TableItem row = cursor.getRow();
                 final int column = cursor.getColumn();
                 //If select diagonal item
                 if (column == 0 || row == table.getItem(column - 1)) {
                     return;
                 }
                 row.getText(column);
                 text.setText(row.getText(column));
                 text.addKeyListener(new KeyAdapter() {
                     public void keyPressed(KeyEvent e) {
                         TableItem row = cursor.getRow();
                         int column = cursor.getColumn();
                         row.setText(column, text.getText());
                         // close the text editor when the user hits "ESC"
                         if (e.character == SWT.ESC) {
                             text.dispose();
                         }
                     }
                 });
                 // close the text editor when the user clicks away
                 text.addFocusListener(new FocusAdapter() {
                     public void focusLost(FocusEvent e) {
                         final TableItem item = cursor.getRow();
                         item.setText(column, text.getText());
                         text.dispose();
                     }
                 });
                 editor.setEditor(text);
                 text.setFocus();
             }
         });
     }
 
     @Override
     public void open(ApplicationContext applicationContext) {
         Display display = Display.getDefault();
         if(shell != null) {
             shell.setVisible(true);
             return;
         }
         shell = new Shell();
         shell.setSize(600, 447);
         shell.setText("Шаг 2: Ввод Данных");
         shell.setLayout(null);
 
         createContents(applicationContext);
 
         shell.open();
         shell.layout();
         shell.addListener(SWT.Close, new CloseListener(shell, applicationContext));
         while (!shell.isDisposed()) {
             if (!display.readAndDispatch()) {
                 display.sleep();
             }
         }
     }
 
     @Override
     public void close() {
         shell.dispose();
     }
 
     protected void createContents(final ApplicationContext applicationContext) {
         final int numberOfParticipants = applicationContext.getNumOfParticipants();
         int numberOfGroups = applicationContext.getNumOfGroups();
 
         ScrolledComposite scrolledComposite = new ScrolledComposite(shell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
         scrolledComposite.setBounds(10, 10, 540, 353);
         scrolledComposite.setExpandHorizontal(true);
         scrolledComposite.setExpandVertical(true);
 
         TabFolder tabFolder = new TabFolder(scrolledComposite, SWT.NONE);
 
         //Crete tabs
         for (int i = 0; i < numberOfGroups; i++) {
             TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
             tabItem.setText("Группа №" + (i + 1));
 
             Composite composite = new Composite(tabFolder, SWT.NONE);
             tabItem.setControl(composite);
 
             ScrolledComposite scrolledHolder = new ScrolledComposite(composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
             scrolledHolder.setBounds(0, 0, 380, 277);
             scrolledHolder.setExpandHorizontal(true);
             scrolledHolder.setExpandVertical(true);
 
             Text cost = new Text(composite, SWT.BORDER);
             cost.setBounds(200, 282, 76, 21);
 
             final Table table = createTable(numberOfParticipants, scrolledHolder);
             final Team team = new Team();
             team.setParticipants(applicationContext.getNumOfParticipants());
             final TeamBoard board = new TeamBoard(team, table, cost);
             tables.add(board);
 
             createInfoLables(composite);
 
             createBoardLables(board, composite);
 
         }
 
         setDiagonalValues(applicationContext);
 
         scrolledComposite.setContent(tabFolder);
         scrolledComposite.setMinSize(tabFolder.computeSize(SWT.DEFAULT, SWT.DEFAULT));
 
         final Button evaluateBtn = new Button(shell, SWT.NONE);
         evaluateBtn.addSelectionListener(new SelectionAdapter() {
             @Override
             public void widgetSelected(SelectionEvent arg0) {
                 setAllValues(numberOfParticipants);
                 evaluateIndexes(applicationContext);
                 evaluateRaiting(applicationContext);
 
                 applicationContext.setUnfinishedState(false);
             }
         });
 
 
         evaluateBtn.setBounds(334, 373, 75, 25);
         evaluateBtn.setText("Обработать");
 
         Button continueBtn = new Button(shell, SWT.NONE);
         continueBtn.setBounds(467, 373, 75, 25);
         continueBtn.setText("Продолжить");
 
         continueBtn.addSelectionListener(new SelectionAdapter() {
             @Override
             public void widgetSelected(SelectionEvent selectionEvent) {
                 if (applicationContext.isUnfinishedState()) {
                     return;
                 }
                 for (TeamBoard board : tables) {
                     applicationContext.getTeams().add(board.getTeam());
                 }
                 applicationContext.getManager().next(applicationContext);
             }
         });
 
     }
 
     private void evaluateRaiting(ApplicationContext applicationContext) {
         final double array[][] = new double[tables.size()][5];
         for (int i = 0; i < tables.size(); i++) {
             final Team team = tables.get(i).getTeam();
             array[i][0] = team.getIndex();
             array[i][1] = team.getGroupUnity();
             array[i][2] = team.getMedianaPlus();
             array[i][3] = team.getMedianaMinus();
             array[i][4] = team.getCost();
         }
 
         final double[] rating = applicationContext.getEvaluator().evaluateRating(array, applicationContext.getNumOfGroups());
 
         applicationContext.getEvaluator().normalize(array, applicationContext.getNumOfGroups());
 
         applicationContext.setNormalized(array);
         for (int i = 0; i < rating.length; i++){
             final Team team = tables.get(i).getTeam();
             team.setRating(rating[i]);
         }
     }
 
     private void evaluateIndexes(ApplicationContext applicationContext) {
         for (TeamBoard board : tables) {
             final TeamRateEvaluator evaluator = applicationContext.getEvaluator();
             final int[][] values = board.getBoardValues();
             final double index = evaluator.evaluateIndex(values, board.getTeam());
             final double unity = evaluator.evaluateGroupUnity(values, board.getTeam());
             final double plus = evaluator.evaluateMedianaMinus(values, board.getTeam());
             final double minus = evaluator.evaluateMedianaPlus(values, board.getTeam());
 
             board.getIndex1Value().setText("" + index);
             board.getIndex2Value().setText("" + unity);
             board.getIndex3Value().setText("" + minus);
             board.getIndex4Value().setText("" + plus);
         }
     }
 
     private void createInfoLables(Composite composite) {
         Label index1 = new Label(composite, SWT.NONE);
         index1.setBounds(386, 10, 55, 15);
         index1.setText("A: ");
 
         Label index2 = new Label(composite, SWT.NONE);
         index2.setBounds(386, 75, 55, 15);
         index2.setText("I (group): ");
 
         Label index3 = new Label(composite, SWT.NONE);
         index3.setBounds(447, 10, 55, 15);
         index3.setText("Ms+: ");
 
         Label index4 = new Label(composite, SWT.NONE);
         index4.setBounds(447, 75, 55, 15);
         index4.setText("Ms-: ");
 
         Label lblCosts = new Label(composite, SWT.NONE);
         lblCosts.setBounds(21, 283, 170, 15);
         lblCosts.setText("Стоимость обучения группы");
     }
 
     private Table createTable(int numberOfParticipants, ScrolledComposite scrolledHolder) {
         final Table table = new Table(scrolledHolder, SWT.BORDER | SWT.FULL_SELECTION);
         table.setLinesVisible(true);
         table.setHeaderVisible(true);
 
         final TableCursor cursor = new TableCursor(table, SWT.NONE);
         final ControlEditor editor = new ControlEditor(cursor);
         editor.grabHorizontal = true;
         editor.grabVertical = true;
 
         addSelectionListener(table, cursor, editor);
 
         addMouseListener(table, cursor, editor);
 
 
         TableColumn tableColumn = new TableColumn(table, SWT.NONE);
         tableColumn.setWidth(50);
         tableColumn.setText("№ Участника");
 
         for (int j = 0; j < numberOfParticipants; j++) {
             TableColumn tableColumn1 = new TableColumn(table, SWT.NONE);
             tableColumn1.setResizable(true);
             tableColumn1.setWidth(50);
             tableColumn1.setText("" + (j + 1));
         }
 
 
 
         for (int j = 0; j < numberOfParticipants; j++) {
             TableItem tableItem = new TableItem(table, SWT.NONE);
             tableItem.setText("" + (j + 1));
         }
 
 
         scrolledHolder.setContent(table);
         scrolledHolder.setMinSize(table.computeSize(SWT.DEFAULT, SWT.DEFAULT));
         setCustomHeight(table, 50);
         return table;
     }
 
     private void createAdditionalFields(Table table) {
 
         TableColumn tableColumn1 = new TableColumn(table, SWT.NONE);
         tableColumn1.setResizable(true);
         tableColumn1.setWidth(25);
         tableColumn1.setText("");
         final TableColumn tableColumn2 = new TableColumn(table, SWT.NONE);
         tableColumn2.setWidth(50);
         tableColumn2.setText("+");
         final TableColumn tableColumn3 = new TableColumn(table, SWT.NONE);
         tableColumn3.setWidth(50);
         tableColumn3.setText("-");
         final TableColumn tableColumn4 = new TableColumn(table, SWT.NONE);
         tableColumn4.setWidth(50);
         tableColumn4.setText("Всего");
         TableItem tableItem = new TableItem(table, SWT.NONE);
         tableItem.setText("");
         TableItem tableItem1 = new TableItem(table, SWT.NONE);
         tableItem1.setText("+");
         TableItem tableItem2 = new TableItem(table, SWT.NONE);
         tableItem2.setText("-");
         TableItem tableItem3 = new TableItem(table, SWT.NONE);
         tableItem3.setText("Всего");
     }
 
     private void setAllValues(int numberOfParticipants) {
         for (TeamBoard board : tables) {
            int index = board.getTable().getTopIndex();
             final int[][] ints = new int[numberOfParticipants][numberOfParticipants];
             while (index < board.getTable().getItemCount()) {
                 final TableItem item = board.getTable().getItem(index);
                 for (int i = 1; i < board.getTable().getColumnCount(); i++) {
                     final String value = item.getText(i).trim();
                     //If value present, set value as marked
                     if (value.length() > 0 && !value.equals("-") && !value.equals("0")) {
                         ints[index][i - 1] = 1;
                     }
                     else if (value.length() > 0 && value.equals("-")) {
                         ints[index][i - 1] = 0;
                     }
                     else if(value.length() == 0 || value.equals("0")) {
                         ints[index][i - 1] = -1;
                     }
 
                 }
                 index++;
             }
             final String text = board.getCosts().getText();
             board.getTeam().setCost(valueOf(text.isEmpty() ? "0" : text));
             board.setBoardValues(ints);
         }
     }
 
     private void setDiagonalValues(ApplicationContext context) {
         for (TeamBoard board : tables) {
             int index = 0;
             while (index < context.getNumOfParticipants()) {
                 for (int i = 0; i < context.getNumOfParticipants(); i++) {
                     if (i == index) {
                         final TableItem item = board.getTable().getItem(index);
                         item.setText(i + 1, "0");
                         item.setChecked(false);
                     }
                 }
                 index++;
             }
         }
     }
 
     private void createBoardLables(final TeamBoard board, Composite composite) {
         Label index1Value = new Label(composite, SWT.NONE);
         index1Value.setBounds(386, 41, 55, 15);
         index1Value.setText("");
         board.setIndex1Value(index1Value);
 
         Label index2Value = new Label(composite, SWT.NONE);
         index2Value.setBounds(386, 106, 55, 15);
         index2Value.setText("");
         board.setIndex2Value(index2Value);
 
         Label index3Value = new Label(composite, SWT.NONE);
         index3Value.setBounds(447, 41, 55, 15);
         index3Value.setText("");
         board.setIndex3Value(index3Value);
 
         Label index4Value = new Label(composite, SWT.NONE);
         index4Value.setBounds(447, 106, 55, 15);
         index4Value.setText("");
         board.setIndex4Value(index4Value);
     }
 
     private class TeamBoard {
         private final Team team;
         private final Table table;
         private final Text costs;
         Label index1Value;
         Label index2Value;
         Label index3Value;
         Label index4Value;
         private int[][] boardValues;
 
         private TeamBoard(Team team, Table table, Text costs) {
             this.team = team;
             this.table = table;
             this.costs = costs;
         }
 
         private Label getIndex1Value() {
             return index1Value;
         }
 
         private void setIndex1Value(Label index1Value) {
             this.index1Value = index1Value;
         }
 
         private Label getIndex2Value() {
             return index2Value;
         }
 
         private void setIndex2Value(Label index2Value) {
             this.index2Value = index2Value;
         }
 
         private Label getIndex3Value() {
             return index3Value;
         }
 
         private void setIndex3Value(Label index3Value) {
             this.index3Value = index3Value;
         }
 
         private Label getIndex4Value() {
             return index4Value;
         }
 
         private void setIndex4Value(Label index4Value) {
             this.index4Value = index4Value;
         }
 
         private Team getTeam() {
             return team;
         }
 
         private Table getTable() {
             return table;
         }
 
         private int[][] getBoardValues() {
             return boardValues;
         }
 
         private void setBoardValues(int[][] boardValues) {
            this.boardValues = boardValues;
         }
 
         @Deprecated
         private int[][] getSimpleBoardValues() {
             int intArray[][] = new int[boardValues.length][boardValues[0].length];
             for (int i = 0; i < intArray.length; i++) {
                 for (int j = 0; j < intArray[0].length; j++) intArray[i][j] = boardValues[i][j];
             }
             return intArray;
         }
 
         private Text getCosts() {
             return costs;
         }
     }
 
 
 }
