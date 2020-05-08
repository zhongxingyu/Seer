 package fr.geobert.radis.robotium;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 import org.acra.sender.GoogleFormSender;
 
 import android.test.ActivityInstrumentationTestCase2;
 import android.util.Log;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.jayway.android.robotium.solo.Solo;
 
 import fr.geobert.radis.db.CommonDbAdapter;
 import fr.geobert.radis.tools.DBPrefsManager;
 import fr.geobert.radis.tools.Formater;
 import fr.geobert.radis.tools.Tools;
 import fr.geobert.radis.R;
 
 @SuppressWarnings({ "unchecked", "rawtypes" })
 public class AccountListTest extends ActivityInstrumentationTestCase2 {
 	public static final String TAG = "RadisRobotium";
 	protected static final String TARGET_PACKAGE_ID = "fr.geobert.radis";
 	protected static final String LAUNCHER_ACTIVITY_FULL_CLASSNAME = "fr.geobert.radis.AccountList";
 
 	/*
 	 * Account Tests
 	 */
 	public static final String ACCOUNT_NAME = "Test";
 	public static final String ACCOUNT_START_SUM = "1000,50";
 	public static final String ACCOUNT_START_SUM_FORMATED_IN_EDITOR = "1 000,50";
 	public static final String ACCOUNT_START_SUM_FORMATED_ON_LIST = "1 000,50 €";
 	public static final String ACCOUNT_DESC = "Test Description";
 	public static final String ACCOUNT_NAME_2 = "Test2";
 	public static final String ACCOUNT_START_SUM_2 = "2000,50";
 	public static final String ACCOUNT_START_SUM_FORMATED_ON_LIST_2 = "2 000,50 €";
 	public static final String ACCOUNT_DESC_2 = "Test Description 2";
 
 	private static Class<?> launcherActivityClass;
 	static {
 		try {
 			launcherActivityClass = Class
 					.forName(LAUNCHER_ACTIVITY_FULL_CLASSNAME);
 		} catch (ClassNotFoundException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public AccountListTest() throws ClassNotFoundException {
 		super(TARGET_PACKAGE_ID, launcherActivityClass);
 	}
 
 	protected Solo solo;
 
 	protected void trashDb() {
 		CommonDbAdapter db = CommonDbAdapter.getInstance(getActivity());
 		db.trashDatabase();
 	}
 
 	private void printCurrentTextViews() {
 		ArrayList<TextView> tvs = solo.getCurrentTextViews(null);
 		for (int i = 0; i < tvs.size(); ++i) {
 			TextView v = tvs.get(i);
 			Log.i(AccountListTest.TAG, "TextView " + i + ": " + v.getText());
 		}
 	}
 
 	private void printCurrentEditTexts() {
 		ArrayList<EditText> tvs = solo.getCurrentEditTexts();
 		for (int i = 0; i < tvs.size(); ++i) {
 			EditText v = tvs.get(i);
 			Log.i(AccountListTest.TAG, "EditText " + i + ": " + v.getText());
 		}
 	}
 
 	private void printCurrentButtons() {
 		ArrayList<Button> tvs = solo.getCurrentButtons();
 		for (int i = 0; i < tvs.size(); ++i) {
 			TextView v = tvs.get(i);
 			Log.i(AccountListTest.TAG, "Buttons " + i + ": " + v.getText());
 		}
 	}
 
 	private void sleep(long ms) {
 		try {
 			Thread.sleep(ms);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	protected void setUp() throws Exception {
 		solo = new Solo(getInstrumentation(), getActivity());
 		trashDb();
 	}
 
 	@Override
 	public void tearDown() throws Exception {
 		try {
 			solo.finishOpenedActivities();
 			trashDb();
 		} catch (Throwable e) {
 			e.printStackTrace();
 		}
 		getActivity().finish();
 		super.tearDown();
 	}
 
 	public void testPreConditions() {
 		assertEquals(0, solo.getCurrentListViews().get(0).getCount());
 	}
 
 	public void addAccount() {
 		solo.clickOnButton(0);
 		solo.enterText(0, ACCOUNT_NAME);
 		solo.enterText(1, ACCOUNT_START_SUM);
 		solo.enterText(4, ACCOUNT_DESC);
 		solo.clickOnText("Ok");
 		solo.waitForView(ListView.class);
 		assertEquals(1, solo.getCurrentListViews().get(0).getCount());
 		assertEquals(ACCOUNT_NAME, solo.getText(2).getText().toString());
 		assertEquals(ACCOUNT_START_SUM_FORMATED_ON_LIST, solo.getText(3)
 				.getText().toString());
 	}
 
 	public void addAccount2() {
 		solo.clickOnButton(0);
 		solo.enterText(0, ACCOUNT_NAME_2);
 		solo.enterText(1, ACCOUNT_START_SUM_2);
 		solo.enterText(4, ACCOUNT_DESC_2);
 		solo.clickOnText("Ok");
 		solo.waitForView(ListView.class);
 		assertEquals(2, solo.getCurrentListViews().get(0).getCount());
 	}
 
 	public void editAccount() {
 		solo.clickLongInList(0);
 		solo.clickOnMenuItem("Modifier");
 		assertEquals(ACCOUNT_NAME, solo.getEditText(0).getText().toString());
 		assertEquals(ACCOUNT_START_SUM_FORMATED_IN_EDITOR, solo.getEditText(1)
 				.getText().toString());
 		assertEquals(ACCOUNT_DESC, solo.getEditText(4).getText().toString());
 		solo.clearEditText(0);
 		solo.enterText(0, ACCOUNT_NAME_2);
 		solo.clearEditText(1);
 		solo.enterText(1, ACCOUNT_START_SUM_2);
 		solo.clearEditText(4);
 		solo.enterText(4, ACCOUNT_DESC_2);
 		solo.clickOnText("Ok");
 		// assertEquals(1, solo.getCurrentListViews().get(0).getCount());
 		assertEquals(ACCOUNT_NAME_2, solo.getText(2).getText().toString());
 		assertEquals(ACCOUNT_START_SUM_FORMATED_ON_LIST_2, solo.getText(3)
 				.getText().toString());
 	}
 
 	public void deleteAccount(int originalCount) {
 		solo.clickLongInList(0);
 		solo.clickOnMenuItem("Supprimer");
 		solo.clickOnButton("Oui");
 		solo.waitForView(ListView.class);
 		assertEquals(originalCount - 1, solo.getCurrentListViews().get(0)
 				.getCount());
 	}
 
 	public void testQuickAddStateOnHomeScreen() {
 		CommonDbAdapter.getInstance(getActivity());
 		DBPrefsManager.getInstance(getActivity()).resetAll();
 
 		assertFalse(solo.getEditText(0).isEnabled());
 		assertFalse(solo.getEditText(1).isEnabled());
 		assertFalse(solo.getButton(2).isEnabled());
 		printCurrentTextViews();
 		TextView v = solo.getText(4);
 		assertEquals(
 				getActivity().getString(
 						fr.geobert.radis.R.string.quickadd_target_no_account),
 				v.getText());
 		addAccount();
 		assertEquals(
 				getActivity().getString(
 						fr.geobert.radis.R.string.quickadd_target_to_configure),
 				v.getText());
 		solo.pressMenuItem(1);
 		solo.clickInList(2);
 		solo.clickInList(0);
 		solo.goBack();
 		assertEquals(
 				getActivity()
 						.getString(fr.geobert.radis.R.string.quickadd_target,
 								ACCOUNT_NAME), v.getText());
 		assertTrue(solo.getEditText(0).isEnabled());
 		assertTrue(solo.getEditText(1).isEnabled());
 		assertFalse(solo.getButton(2).isEnabled());
 
 		// test adding op
 		solo.enterText(0, "quick add");
 		solo.enterText(1, "10,50");
 		solo.clickOnButton(2);
 		assertEquals("990,00 €", solo.getText(3).getText().toString());
 		solo.clickInList(0);
 		assertNotNull(solo.getText("quick add"));
 		solo.goBack();
 
 		solo.enterText(0, "quick add2");
 		solo.enterText(1, "+10,50");
 		solo.clickOnButton(2);
 		assertEquals("1 000,50 €", solo.getText(3).getText().toString());
 		solo.clickInList(0);
 		assertNotNull(solo.getText("quick add"));
 		assertNotNull(solo.getText("quick add2"));
 		solo.goBack();
 
 		// test labels and state
 		editAccount();
 		assertEquals(
 				getActivity().getString(
 						fr.geobert.radis.R.string.quickadd_target,
 						ACCOUNT_NAME_2), v.getText());
 
 		addAccount2();
 		deleteAccount(2);
 		assertEquals(
 				getActivity().getString(
 						fr.geobert.radis.R.string.quickadd_target_to_configure),
 				v.getText());
 
 		deleteAccount(1);
 		assertFalse(solo.getEditText(0).isEnabled());
 		assertFalse(solo.getEditText(1).isEnabled());
 		assertFalse(solo.getButton(2).isEnabled());
 		assertEquals(
 				getActivity().getString(
 						fr.geobert.radis.R.string.quickadd_target_no_account),
 				v.getText());
 	}
 
 	/*
 	 * Operations Tests
 	 */
 
 	private static final String OP_TP = "Operation 1";
 	private static final String OP_AMOUNT = "10.50";
 	private static final String OP_AMOUNT_FORMATED = "-10,50";
 	private static final String OP_TAG = "Tag 1";
 	private static final String OP_MODE = "Carte bleue";
 	private static final String OP_DESC = "Robotium Operation 1";
 
 	private static final String OP_AMOUNT_2 = "100";
 
 	private void setUpOpTest() {
 		addAccount();
 		solo.clickInList(0);
 	}
 
 	public void addOp() {
 		setUpOpTest();
 
 		solo.pressMenuItem(0);
 		solo.enterText(3, OP_TP);
 		for (int i = 0; i < OP_AMOUNT.length(); ++i) {
 			solo.enterText(4, String.valueOf(OP_AMOUNT.charAt(i)));
 		}
 		solo.enterText(5, OP_TAG);
 		solo.enterText(6, OP_MODE);
 		solo.enterText(7, OP_DESC);
 		solo.clickOnButton("Ok");
 		assertTrue(solo.getText(1).getText().toString().contains("= 990,00"));
 		assertTrue(solo.getText(4).getText().toString()
 				.equals(OP_AMOUNT_FORMATED));
 	}
 
 	public void addManyOps() {
 		setUpOpTest();
 		for (int j = 0; j < 30; ++j) {
 			solo.pressMenuItem(0);
 			solo.enterText(3, OP_TP + j);
 			solo.enterText(4, OP_AMOUNT_2);
 			solo.enterText(5, OP_TAG);
 			solo.enterText(6, OP_MODE);
 			solo.enterText(7, OP_DESC);
 			solo.clickOnButton("Ok");
 		}
 		assertTrue(solo.getText(1).getText().toString().contains("= -1 999,50"));
 	}
 
 	public void testEditOp() {
 		addManyOps();
 		solo.clickLongInList(5, 0);
 		solo.clickOnMenuItem("Modifier");
 		solo.clearEditText(4);
 		solo.enterText(4, "103");
 		solo.clickOnButton("Ok");
 		assertTrue(solo.getText(1).getText().toString().contains("= -1 796,50"));
 	}
 
 	public void testDisableAutoNegate() {
 		setUpOpTest();
 		solo.pressMenuItem(0);
 		solo.enterText(3, OP_TP);
 		solo.enterText(4, "+");
 		for (int i = 0; i < OP_AMOUNT.length(); ++i) {
 			solo.enterText(4, String.valueOf(OP_AMOUNT.charAt(i)));
 		}
 		printCurrentEditTexts();
 		assertTrue(solo.getEditText(4).getText().toString().equals("+10,50"));
 		solo.clickOnButton("Ok");
 		printCurrentTextViews();
 		assertTrue(solo.getText(1).getText().toString().contains("= 1 011,00"));
 		printCurrentTextViews();
 		assertTrue(solo.getText(5).getText().toString().equals("10,50"));
 	}
 
 	/**
 	 * Schedule ops
 	 */
 
 	public void testNoAccount() {
 		assertFalse(solo.getButton(getString(R.string.scheduled_ops)).isEnabled());
 	}
 
 	private void setUpSchOp() {
 		solo.pressMenuItem(1);
 		solo.clickOnText(getString(R.string.prefs_insertion_date_label));
 		solo.clearEditText(0);
 		GregorianCalendar today = new GregorianCalendar();
 		Tools.clearTimeOfCalendar(today);
 		today.add(Calendar.DAY_OF_MONTH, 1);
 		solo.enterText(0, Integer.toString(today.get(Calendar.DAY_OF_MONTH)));
 		solo.clickOnButton("Ok");
 		solo.goBack();
 		addAccount();
 		assertTrue(solo.getButton(getString(R.string.scheduled_ops)).isEnabled());
 		solo.clickOnButton(getString(R.string.scheduled_ops));
 		assertEquals(0, solo.getCurrentListViews().get(0).getCount());
 	}
 
 	public void addScheduleOp() {
 		setUpSchOp();
 		solo.clickOnImageButton(0);
 		GregorianCalendar today = new GregorianCalendar();
 		Tools.clearTimeOfCalendar(today);
 		solo.setDatePicker(0, today.get(Calendar.YEAR),
 				today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));
 		solo.enterText(3, OP_TP);
 		solo.enterText(4, "9,50");
 		solo.enterText(5, OP_TAG);
 		solo.enterText(6, OP_MODE);
 		solo.enterText(7, OP_DESC);
 		solo.clickOnButton("Ok");
 		solo.waitForView(ListView.class);
 		assertEquals(1, solo.getCurrentListViews().get(0).getCount());
 		solo.goBack();
 		solo.clickInList(0);
 		sleep(1000);
 		// -1 is for "get more ops" line
 		// assertEquals(1, solo.getCurrentListViews().get(0).getCount() - 1);
 		Log.d(TAG, "solo.getText(1).getText().toString() : "
 				+ solo.getText(1).getText().toString());
 		assertTrue(solo.getText(1).getText().toString().contains("= 991,00"));
 	}
 
 	public void testEditScheduledOp() {
 		addScheduleOp();
 		solo.clickLongInList(0);
 		solo.clickOnMenuItem(getString(R.string.edit));
 		solo.clearEditText(4);
 		solo.enterText(4, "-7,50");
 		solo.clickOnButton(getString(R.string.ok));
 		solo.clickOnButton(getString(R.string.update));
 		printCurrentTextViews();
 		assertTrue(solo.getText(1).getText().toString().contains("= 993,00"));
 	}
 
 	private int setupDelOccFromOps() {
 		setUpSchOp();
 		solo.clickOnImageButton(0);
 		GregorianCalendar today = new GregorianCalendar();
 		Tools.clearTimeOfCalendar(today);
 		today.add(Calendar.DAY_OF_MONTH, -14);
 		solo.setDatePicker(0, today.get(Calendar.YEAR),
 				today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));
 		solo.enterText(3, OP_TP);
 		solo.enterText(4, "1,00");
 		solo.enterText(5, OP_TAG);
 		solo.enterText(6, OP_MODE);
 		solo.enterText(7, OP_DESC);
 		solo.clickOnButton(getString(R.string.scheduling));
 		solo.pressSpinnerItem(1, -1);
 		solo.clickOnButton(getString(R.string.ok));
 		solo.waitForView(ListView.class);
 		solo.goBack();
 		solo.clickInList(0);
 		solo.waitForActivity("OperationList");
 		int nbOps = solo.getCurrentListViews().get(0).getCount();
 		solo.clickInList(nbOps);
 		sleep(5000);
 		nbOps = solo.getCurrentListViews().get(0).getCount();
 		printCurrentTextViews();
		sleep(15000);
		Log.d(TAG, "nbOps : " + nbOps);
 		Log.d(TAG, "interface text : " + solo.getText(1).getText().toString()
 				+ " / " + Formater.getSumFormater().format(1000.5 - nbOps + 1));
 		assertTrue(solo.getText(1).getText().toString()
 				.contains(Formater.getSumFormater().format(1000.5 - nbOps + 1)));
 		return nbOps;
 	}
 
 	public void testDelFutureOccurences() {
 		int nbOps = setupDelOccFromOps();
 		Log.d(TAG, "mbOPS : " + nbOps);
 		solo.clickLongInList(nbOps - 3);
 		solo.clickOnMenuItem(getString(R.string.delete));
 		solo.clickOnButton(getString(R.string.del_all_following));
 		solo.waitForView(ListView.class);
 		solo.clickInList(solo.getCurrentListViews().get(0).getCount());
		assertEquals(2, solo.getCurrentListViews().get(0).getCount());
 		printCurrentTextViews();
 		Log.d(TAG, "interface text : " + solo.getText(1).getText().toString()
 				+ " / " + Formater.getSumFormater().format(1000.5 - 2));
 		assertTrue(solo.getText(1).getText().toString()
 				.contains(Formater.getSumFormater().format(1000.5 - 2)));
 	}
 
 	public void testDelAllOccurencesFromOps() {
 		int nbOps = setupDelOccFromOps();
 		solo.clickLongInList(nbOps - 3);
 		solo.clickOnMenuItem(getString(R.string.delete));
 		solo.clickOnButton(getString(R.string.del_all_occurrences));
 		sleep(1000);
 		// 1 is the footer
 		assertEquals(1, solo.getCurrentListViews().get(0).getCount());
 		solo.goBack();
 		printCurrentTextViews();
 		assertTrue(solo.getText(3).getText().toString()
 				.contains(Formater.getSumFormater().format(1000.5)));
 	}
 
 	// issue 112
 	public void testCancelSchEdition() {
 		Picker picker = new Picker(solo);
 		setupDelOccFromOps();
 		solo.pressMenuItem(1);
 		solo.waitForActivity("ScheduledOpList");
 		final CharSequence date = solo.getCurrentTextViews(null).get(2).getText(); 
 		solo.clickInList(0);
 		solo.waitForActivity("ScheduledOperationEditor");
 		GregorianCalendar today = new GregorianCalendar();
 		Tools.clearTimeOfCalendar(today);
 		today.add(Calendar.MONTH, -2);
 		picker.clickOnDatePicker(today.get(Calendar.MONTH) + 1,
 				today.get(Calendar.DAY_OF_MONTH), today.get(Calendar.YEAR));
 		solo.clickOnButton(getString(R.string.ok));
 		solo.clickOnButton(getString(R.string.cancel));
 		solo.clickOnButton(getString(R.string.cancel));
 		solo.waitForActivity("ScheduledOpList");
 		printCurrentTextViews();
 		Log.d(TAG, "before date : " + date);
 		assertEquals(date, solo.getCurrentTextViews(null).get(2).getText());
 	}
 
 	// issue 59 test
 	public void testDeleteAllOccurences() {
 		setUpSchOp();
 		solo.clickOnImageButton(0);
 		GregorianCalendar today = new GregorianCalendar();
 		Tools.clearTimeOfCalendar(today);
 		today.add(Calendar.MONTH, -2);
 		solo.setDatePicker(0, today.get(Calendar.YEAR),
 				today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));
 		solo.enterText(3, OP_TP);
 		solo.enterText(4, "9,50");
 		solo.enterText(5, OP_TAG);
 		solo.enterText(6, OP_MODE);
 		solo.enterText(7, OP_DESC);
 		solo.clickOnButton("Ok");
 		solo.waitForView(ListView.class);
 		solo.goBack();
 		solo.clickInList(0);
 		solo.clickInList(2);
 		sleep(2000);
 		solo.clickInList(3);
 		sleep(2000);
 
 		// -1 is for "get more ops" line
 		assertEquals(3, solo.getCurrentListViews().get(0).getCount() - 1);
 		solo.pressMenuItem(1);
 		solo.clickLongInList(0);
 		solo.clickOnMenuItem("Supprimer");
 		solo.clickOnButton("Toutes");
 		solo.goBack();
 
 		// -1 is for "get more ops" line
 		assertEquals(0, solo.getCurrentListViews().get(0).getCount() - 1);
 	}
 
 	/**
 	 * Infos
 	 */
 
 	// test adding info with different casing
 	public void testAddExistingInfo() {
 		setUpOpTest();
 		solo.pressMenuItem(0);
 		solo.enterText(3, OP_TP);
 		for (int i = 0; i < OP_AMOUNT.length(); ++i) {
 			solo.enterText(4, String.valueOf(OP_AMOUNT.charAt(i)));
 		}
 		solo.clickOnButton(2);
 		solo.clickOnButton("Créer");
 		solo.enterText(0, "Atest");
 		solo.clickOnButton("Ok");
 		sleep(1000);
 		assertEquals(1, solo.getCurrentListViews().get(0).getCount());
 		solo.clickOnButton(2);
 		solo.clickOnButton("Créer");
 		solo.enterText(0, "ATest");
 		solo.clickOnButton("Ok");
 		assertNotNull(solo.getText(getActivity().getString(
 				fr.geobert.radis.R.string.item_exists)));
 	}
 
 	// issue 50 test
 	public void testAddInfoAndCreateOp() {
 		setUpOpTest();
 		solo.pressMenuItem(0);
 		solo.enterText(3, OP_TP);
 		for (int i = 0; i < OP_AMOUNT.length(); ++i) {
 			solo.enterText(4, String.valueOf(OP_AMOUNT.charAt(i)));
 		}
 		solo.clickOnButton(2);
 		solo.clickOnButton("Créer");
 		solo.enterText(0, "Atest");
 		solo.clickOnButton("Ok");
 		solo.clickInList(0);
 		solo.clickOnButton("Ok");
 		solo.clickOnButton("Ok");
 		solo.pressMenuItem(0);
 		solo.clickOnButton(2);
 		sleep(1000);
 		assertEquals(1, solo.getCurrentListViews().get(0).getCount());
 	}
 
 	private void addOpOnDate(GregorianCalendar t) {
 		solo.pressMenuItem(0);
 		solo.setDatePicker(0, t.get(Calendar.YEAR), t.get(Calendar.MONTH),
 				t.get(Calendar.DAY_OF_MONTH));
 		solo.enterText(3, OP_TP);
 		solo.enterText(4, "1");
 		solo.clickOnButton("Ok");
 	}
 
 	private void setUpProjTest1() {
 		addAccount();
 		solo.clickInList(0);
 		GregorianCalendar today = new GregorianCalendar();
 		Tools.clearTimeOfCalendar(today);
 		today.set(Calendar.DAY_OF_MONTH,
 				Math.min(today.get(Calendar.DAY_OF_MONTH), 28));
 		today.add(Calendar.MONTH, -2);
 		for (int i = 0; i < 6; ++i) {
 			addOpOnDate(today);
 			today.add(Calendar.MONTH, +1);
 		}
 	}
 
 	private String getString(final int id) {
 		return getActivity().getString(id);
 	}
 
 	private String getDateStr(Calendar cal) {
 		return Formater.getFullDateFormater(getActivity())
 				.format(cal.getTime());
 	}
 
 	public void testProjectionFromOpList() {
 		// test mode 0
 		setUpProjTest1();
 		GregorianCalendar today = new GregorianCalendar();
 		Tools.clearTimeOfCalendar(today);
 		today.set(Calendar.DAY_OF_MONTH,
 				Math.min(today.get(Calendar.DAY_OF_MONTH), 28));
 		today.add(Calendar.MONTH, 3);
 		Log.d(TAG, "testProjectionFromOpList : " + getDateStr(today) + " VS "
 				+ solo.getButton(0).getText().toString());
 
 		assertTrue(solo.getButton(0).getText().toString()
 				.contains(getDateStr(today)));
 		assertTrue(solo.getButton(0).getText().toString().contains("= 994,50"));
 		assertTrue(solo.getText(0).getText().toString().contains("= 997,50"));
 
 		// test mode 1
 		solo.clickOnButton(0);
 		solo.pressSpinnerItem(0, 1);
 		assertTrue(solo.getEditText(0).isEnabled());
 		today = new GregorianCalendar();
 		today.set(Calendar.DAY_OF_MONTH,
 				Math.min(today.get(Calendar.DAY_OF_MONTH), 28));
 		Tools.clearTimeOfCalendar(today);
 		solo.enterText(0, Integer.toString(Math.min(
 				today.get(Calendar.DAY_OF_MONTH), 28)));
 		solo.clickOnButton("Ok");
 		today.add(Calendar.MONTH, 1);
 		Log.d(TAG, "1DATE : " + getDateStr(today));
 		Log.d(TAG, "1DATE displayed : "
 				+ solo.getButton(0).getText().toString());
 		assertTrue(solo.getButton(0).getText().toString()
 				.contains(getDateStr(today)));
 		assertTrue(solo.getButton(0).getText().toString().contains("= 996,50"));
 		assertTrue(solo.getText(0).getText().toString().contains("= 997,50"));
 
 		// test mode 2
 		solo.clickOnButton(0);
 		solo.pressSpinnerItem(0, 1);
 		assertTrue(solo.getEditText(0).isEnabled());
 		today = new GregorianCalendar();
 		Tools.clearTimeOfCalendar(today);
 		today.set(Calendar.DAY_OF_MONTH, 28);
 		today.add(Calendar.MONTH, +3);
 		solo.enterText(0, getDateStr(today));
 		solo.clickOnButton("Ok");
 
 		Log.d(TAG, "2DATE : " + getDateStr(today));
 		Log.d(TAG, "2DATE displayed : "
 				+ solo.getButton(0).getText().toString());
 		Log.d(TAG, "solo.getButton(0).getText() : "
 				+ solo.getButton(0).getText());
 
 		assertTrue(solo.getButton(0).getText().toString()
 				.contains(getDateStr(today)));
 		assertTrue(solo.getButton(0).getText().toString().contains("= 994,50"));
 		assertTrue(solo.getText(0).getText().toString().contains("= 997,50"));
 
 		solo.clickInList(solo.getCurrentListViews().get(0).getCount());
 		sleep(1000);
 		solo.clickInList(solo.getCurrentListViews().get(0).getCount());
 		sleep(1000);
 		solo.clickInList(5);
 		sleep(1000);
 		assertTrue(solo.getButton(0).getText().toString().contains("= 994,50"));
 		assertTrue(solo.getText(0).getText().toString().contains("= 998,50"));
 
 		// test back to mode 0
 		solo.clickOnButton(0);
 		solo.pressSpinnerItem(0, -2);
 		assertFalse(solo.getEditText(0).isEnabled());
 		solo.clickOnButton("Ok");
 
 		Log.d(TAG, "0DATE : " + getDateStr(today));
 		Log.d(TAG, "0DATE displayed : "
 				+ solo.getButton(0).getText().toString());
 		Log.d(TAG, "solo.getButton(0).getText() : "
 				+ solo.getButton(0).getText());
 
 		assertTrue(solo.getButton(0).getText().toString().contains("= 994,50"));
 		assertTrue(solo.getText(0).getText().toString().contains("= 998,50"));
 	}
 
 	public void testProjectionFromAccount() {
 		setUpProjTest1();
 		sleep(5000);
 		assertTrue(solo.getButton(0).getText().toString().contains("= 994,50"));
 		assertTrue(solo.getText(0).getText().toString().contains("= 997,50"));
 
 		// test mode 1
 		solo.goBack();
 		solo.clickLongInList(0);
 		solo.clickOnMenuItem("Modifier");
 		solo.pressSpinnerItem(1, 1);
 		assertTrue(solo.getEditText(3).isEnabled());
 		GregorianCalendar today = new GregorianCalendar();
 		Tools.clearTimeOfCalendar(today);
 		solo.enterText(3, Integer.toString(Math.min(
 				today.get(Calendar.DAY_OF_MONTH), 28)));
 		solo.clickOnButton("Ok");
 
 		assertTrue(solo.getText(3).getText().toString().contains("996,50"));
 
 		// test mode 2
 		solo.clickLongInList(0);
 		solo.clickOnMenuItem("Modifier");
 		solo.pressSpinnerItem(1, 1);
 		assertTrue(solo.getEditText(3).isEnabled());
 		today.set(Calendar.DAY_OF_MONTH, 28);
 		today.add(Calendar.MONTH, +2);
 		solo.enterText(3, Integer.toString(today.get(Calendar.DAY_OF_MONTH))
 				+ "/" + Integer.toString(today.get(Calendar.MONTH) + 1) + "/"
 				+ Integer.toString(today.get(Calendar.YEAR)));
 		solo.clickOnButton("Ok");
 		assertTrue(solo.getText(3).getText().toString().contains("995,50"));
 
 		// test back to mode 0
 		solo.clickLongInList(0);
 		solo.clickOnMenuItem("Modifier");
 		solo.pressSpinnerItem(1, -2);
 		assertFalse(solo.getEditText(3).isEnabled());
 		solo.clickOnButton("Ok");
 		assertTrue(solo.getText(3).getText().toString().contains("994,50"));
 	}
 
 	public void addOpMode1() {
 		// add account
 		solo.clickOnButton(0);
 		solo.enterText(0, ACCOUNT_NAME);
 		solo.enterText(1, ACCOUNT_START_SUM);
 		solo.enterText(4, ACCOUNT_DESC);
 		solo.pressSpinnerItem(1, 1);
 		assertTrue(solo.getEditText(3).isEnabled());
 		GregorianCalendar today = new GregorianCalendar();
 		Tools.clearTimeOfCalendar(today);
 		today.add(Calendar.DAY_OF_MONTH, 1);
 		solo.enterText(3, Integer.toString(today.get(Calendar.DAY_OF_MONTH)));
 		solo.clickOnButton("Ok");
 		solo.clickInList(0);
 		assertTrue(solo.getText(0).getText().toString().contains("= 1 000,50"));
 		Log.d(TAG, "addOpMode1 before add "
 				+ solo.getCurrentListViews().get(0).getCount());
 		today.add(Calendar.DAY_OF_MONTH, -1);
 		addOpOnDate(today);
 		assertTrue(solo.getButton(0).getText().toString().contains("= 999,50"));
 		assertTrue(solo.getText(0).getText().toString().contains("= 999,50"));
 		Log.d(TAG, "addOpMode1 after one add "
 				+ solo.getCurrentListViews().get(0).getCount());
 		// add op after X
 		today.add(Calendar.MONTH, +1);
 		addOpOnDate(today);
 		solo.clickInList(0);
 		assertTrue(solo.getButton(0).getText().toString().contains("= 999,50"));
 		assertTrue(solo.getText(0).getText().toString().contains("= 998,50"));
 		Log.d(TAG, "addOpMode1 after two add "
 				+ solo.getCurrentListViews().get(0).getCount());
 		// add op before X of next month, should update the current sum
 		today.add(Calendar.MONTH, -2);
 		addOpOnDate(today);
 		// Log.d(TAG, "addOpMode1 after three add " +
 		// solo.getCurrentListViews().get(0).getCount());
 		solo.clickInList(0);
 		assertTrue(solo.getButton(0).getText().toString().contains("= 998,50"));
 		assertTrue(solo.getText(0).getText().toString().contains("= 997,50"));
 	}
 
 	public void editOpMode1() {
 		addOpMode1();
 
 		solo.clickLongInList(0);
 		solo.clickOnMenuItem("Modifier");
 		solo.clearEditText(4);
 		solo.enterText(4, "+2");
 		solo.clickOnButton("Ok");
 		solo.clickInList(0);
 		assertTrue(solo.getButton(0).getText().toString().contains("= 998,50"));
 		assertTrue(solo.getText(0).getText().toString().contains("= 1 000,50"));
 		// Log.d(TAG, "editOpMode1 after one edit " +
 		// solo.getCurrentListViews().get(0).getCount());
 
 		solo.clickInList(3);
 		solo.clickLongInList(3);
 		solo.clickOnMenuItem("Modifier");
 		solo.clearEditText(4);
 		solo.enterText(4, "+2");
 		solo.clickOnButton("Ok");
 		// Log.d(TAG, "editOpMode1 after one edit " +
 		// solo.getCurrentListViews().get(0).getCount());
 		solo.clickInList(0);
 		assertEquals(3, solo.getCurrentListViews().get(0).getCount() - 1);
 		assertTrue(solo.getButton(0).getText().toString()
 				.contains("= 1 001,50"));
 		assertTrue(solo.getText(0).getText().toString().contains("= 1 003,50"));
 	}
 
 	public void addOpMode2() {
 		// add account
 		solo.clickOnButton(0);
 		solo.enterText(0, ACCOUNT_NAME);
 		solo.enterText(1, ACCOUNT_START_SUM);
 		solo.enterText(4, ACCOUNT_DESC);
 		solo.pressSpinnerItem(1, 2);
 		assertTrue(solo.getEditText(3).isEnabled());
 		GregorianCalendar today = new GregorianCalendar();
 		Tools.clearTimeOfCalendar(today);
 		today.add(Calendar.DAY_OF_MONTH, 1);
 		solo.enterText(3, Integer.toString(today.get(Calendar.DAY_OF_MONTH))
 				+ "/" + Integer.toString(today.get(Calendar.MONTH) + 1) + "/"
 				+ Integer.toString(today.get(Calendar.YEAR)));
 		solo.clickOnButton("Ok");
 		solo.clickInList(0);
 		Log.d(TAG, "addOpMode2 before add "
 				+ solo.getCurrentListViews().get(0).getCount());
 		assertTrue(solo.getText(0).getText().toString().contains("= 1 000,50"));
 
 		today.add(Calendar.DAY_OF_MONTH, -1);
 		addOpOnDate(today);
 		// Log.d(TAG, "addOpMode2 after one add " +
 		// solo.getCurrentListViews().get(0).getCount());
 		assertTrue(solo.getButton(0).getText().toString().contains("= 999,50"));
 		assertTrue(solo.getText(0).getText().toString().contains("= 999,50"));
 
 		// add op after X
 		today.add(Calendar.MONTH, +1);
 		addOpOnDate(today);
 		// Log.d(TAG, "addOpMode2 after two add " +
 		// solo.getCurrentListViews().get(0).getCount());
 		solo.clickInList(0);
 		assertTrue(solo.getButton(0).getText().toString().contains("= 999,50"));
 		assertTrue(solo.getText(0).getText().toString().contains("= 998,50"));
 
 		// add op before X of next month, should update the current sum
 		today.add(Calendar.MONTH, -2);
 		addOpOnDate(today);
 		// Log.d(TAG, "addOpMode2 after three add " +
 		// solo.getCurrentListViews().get(0).getCount());
 		solo.clickInList(0);
 		assertTrue(solo.getButton(0).getText().toString().contains("= 998,50"));
 		assertTrue(solo.getText(0).getText().toString().contains("= 997,50"));
 	}
 
 	public void editOpMode2() {
 		addOpMode2();
 
 		solo.clickLongInList(0);
 		solo.clickOnMenuItem("Modifier");
 		solo.clearEditText(4);
 		solo.enterText(4, "+2");
 		solo.clickOnButton("Ok");
 		solo.clickInList(0);
 		assertTrue(solo.getButton(0).getText().toString().contains("= 998,50"));
 		assertTrue(solo.getText(0).getText().toString().contains("= 1 000,50"));
 		// Log.d(TAG, "editOpMode2 after one edit " +
 		// solo.getCurrentListViews().get(0).getCount());
 		solo.clickInList(3);
 		solo.clickLongInList(3);
 		solo.clickOnMenuItem("Modifier");
 		solo.clearEditText(4);
 		solo.enterText(4, "+2");
 		solo.clickOnButton("Ok");
 		solo.clickInList(0);
 		// Log.d(TAG, "editOpMode2 after two edit " +
 		// solo.getCurrentListViews().get(0).getCount());
 		assertEquals(3, solo.getCurrentListViews().get(0).getCount() - 1);
 		assertTrue(solo.getButton(0).getText().toString()
 				.contains("= 1 001,50"));
 		assertTrue(solo.getText(0).getText().toString().contains("= 1 003,50"));
 	}
 
 	private void delOps() {
 		solo.clickLongInList(0);
 		solo.clickOnMenuItem("Supprimer");
 		solo.clickOnButton("Oui");
 		assertTrue(solo.getButton(0).getText().toString()
 				.contains("= 1 001,50"));
 		assertTrue(solo.getText(0).getText().toString().contains("= 1 001,50"));
 		solo.clickLongInList(2);
 		solo.clickOnMenuItem("Supprimer");
 		solo.clickOnButton("Oui");
 		assertTrue(solo.getButton(0).getText().toString().contains("= 999,50"));
 		assertTrue(solo.getText(0).getText().toString().contains("= 999,50"));
 	}
 
 	public void testDelOpMode1() {
 		editOpMode1();
 		delOps();
 	}
 
 	public void testDelOpMode2() {
 		editOpMode2();
 		delOps();
 	}
 
 	// test for issue 95
 	public void DelOpMode1_2() {
 		setUpOpTest();
 		for (int j = 0; j < 10; ++j) {
 			solo.pressMenuItem(0);
 			solo.enterText(3, OP_TP + j);
 			solo.enterText(4, "10");
 			solo.enterText(5, OP_TAG);
 			solo.enterText(6, OP_MODE);
 			solo.enterText(7, OP_DESC);
 			solo.clickOnButton("Ok");
 		}
 		Log.d(TAG, "testDelOpMode1_2 sum at selection after first adds: "
 				+ solo.getText(1).getText().toString());
 		assertTrue(solo.getText(1).getText().toString().contains("= 900,50"));
 		GregorianCalendar today = new GregorianCalendar();
 		Tools.clearTimeOfCalendar(today);
 		today.set(Calendar.DAY_OF_MONTH,
 				Math.min(today.get(Calendar.DAY_OF_MONTH), 28));
 		today.add(Calendar.MONTH, -1);
 		for (int i = 0; i < 10; ++i) {
 			addOpOnDate(today);
 		}
 
 		solo.clickInList(9);
 		Log.d(TAG, "testDelOpMode1_2 sum at selection : "
 				+ solo.getText(0).getText().toString());
 		Log.d(TAG, "testDelOpMode1_2 sum at proj : "
 				+ solo.getButton(0).getText().toString());
 		assertTrue(solo.getText(0).getText().toString().contains("= 980,50"));
 		assertTrue(solo.getButton(0).getText().toString().contains("= 890,50"));
 		solo.clickInList(9);
 
 		Log.d(TAG, "testDelOpMode1_2 sum at selection 2 : "
 				+ solo.getText(0).getText().toString());
 		Log.d(TAG, "testDelOpMode1_2 sum at proj 2 : "
 				+ solo.getButton(0).getText().toString());
 		assertTrue(solo.getText(0).getText().toString().contains("= 990,50"));
 		assertTrue(solo.getButton(0).getText().toString().contains("= 890,50"));
 		solo.clickLongInList(9);
 		sleep(1000);
 		solo.clickOnMenuItem("Supprimer");
 		solo.clickOnButton("Oui");
 
 		Log.d(TAG, "testDelOpMode1_2 sum at selection after del : "
 				+ solo.getText(0).getText().toString());
 		Log.d(TAG, "testDelOpMode1_2 sum at proj after del : "
 				+ solo.getButton(0).getText().toString());
 		assertTrue(solo.getButton(0).getText().toString().contains("= 891,50"));
 		assertTrue(solo.getText(0).getText().toString().contains("= 991,50"));
 	}
 }
