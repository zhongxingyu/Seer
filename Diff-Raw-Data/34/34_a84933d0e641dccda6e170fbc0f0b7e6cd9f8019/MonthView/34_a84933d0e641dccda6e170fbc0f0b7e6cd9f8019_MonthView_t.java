 package no.knubo.accounting.client.views;
 
 import java.util.List;
 
 import no.knubo.accounting.client.Constants;
 import no.knubo.accounting.client.Elements;
 import no.knubo.accounting.client.I18NAccount;
 import no.knubo.accounting.client.Util;
 import no.knubo.accounting.client.cache.MonthHeaderCache;
 import no.knubo.accounting.client.misc.AuthResponder;
 import no.knubo.accounting.client.misc.ImageFactory;
 import no.knubo.accounting.client.misc.ServerResponse;
 import no.knubo.accounting.client.views.modules.YearMonthComboHelper;
 
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.json.client.JSONObject;
 import com.google.gwt.json.client.JSONString;
 import com.google.gwt.json.client.JSONValue;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.DockPanel;
 import com.google.gwt.user.client.ui.FlexTable;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Hyperlink;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.ListBox;
 import com.google.gwt.user.client.ui.Widget;
 
 public class MonthView extends Composite implements ClickHandler,
 		ChangeHandler, ServerResponse {
 
 	private static MonthView instance;
 
 	public static MonthView getInstance(Constants constants,
 			I18NAccount messages, ViewCallback caller, Elements elements) {
 		if (instance == null) {
 			instance = new MonthView(constants, messages, caller, elements);
 		}
 		return instance;
 	}
 
 	private FlexTable table;
 
 	private final Constants constants;
 
 	private ListBox monthYearCombo;
 
 	private final I18NAccount messages;
 
 	private Image backImage;
 
 	private Image nextImage;
 
 	private int currentYear;
 
 	private int currentMonth;
 
 	private DockPanel dockPanel;
 
 	private final ViewCallback caller;
 
 	private YearMonthComboHelper yearMonthComboHelper;
 
 	private final Elements elements;
 
 	public MonthView(Constants constants, I18NAccount messages,
 			ViewCallback caller, Elements elements) {
 		this.constants = constants;
 		this.messages = messages;
 		this.caller = caller;
 		this.elements = elements;
 
 		dockPanel = new DockPanel();
 		newTable();
 
 		backImage = ImageFactory.previousImage("MonthView.backImage");
 		backImage.addClickHandler(this);
 
 		nextImage = ImageFactory.nextImage("MonthView.nextImage");
 		nextImage.addClickHandler(this);
 
 		monthYearCombo = new ListBox(false);
 		monthYearCombo.setStyleName("monthyearcombo");
 		monthYearCombo.setVisibleItemCount(1);
 		monthYearCombo.addChangeHandler(this);
 
 		yearMonthComboHelper = new YearMonthComboHelper(constants, messages,
 				monthYearCombo, elements);
 
 		HorizontalPanel navPanel = new HorizontalPanel();
 		navPanel.add(backImage);
 		navPanel.add(monthYearCombo);
 		navPanel.add(nextImage);
 
 		dockPanel.add(navPanel, DockPanel.NORTH);
 
 		initWidget(dockPanel);
 	}
 
 	public void init() {
 		dockPanel.remove(table);
 		newTable();
 
 		getData("");
 	}
 
 	public void init(int year, int month) {
 		dockPanel.remove(table);
 		newTable();
 		getData("month=" + month + "&year=" + year);
 	}
 
 	private void newTable() {
 		table = new FlexTable();
 		setupHeaders();
 		dockPanel.add(table, DockPanel.CENTER);
 	}
 
 	private void getData(String params) {
 		yearMonthComboHelper.fillYearMonthCombo();
 
 		AuthResponder.get(constants, messages, this,
 				"accounting/showmonth.php?" + params);
 	}
 
 	private void setupHeaders() {
 		table.setStyleName("tableborder");
 		table.insertRow(0);
 		table.insertRow(0);
 
 		table.getRowFormatter().setStyleName(0, "header");
 		table.getRowFormatter().setStyleName(1, "debkred");
 
 		int row = 0;
 		int col = 1;
 		table.getFlexCellFormatter().setColSpan(row + 1, 0, 4);
 		table.getCellFormatter().setStyleName(row + 1, 0, "leftborder");
 
 		table.setText(row, col++, elements.attachment());
 		table.setText(row, col++, elements.date());
 
 		table.setText(row, col++, elements.description());
 
 		/* Column-position for row 2 */
 		int col2 = 1;
 		List<String> names = MonthHeaderCache.getInstance(constants, messages)
 				.headers();
 
 		for (String header : names) {
 
 			/* The posts headers */
 			table.getFlexCellFormatter().setColSpan(row, col, 2);
 			table.getCellFormatter().setStyleName(row, col, "center");
 			table.setText(row, col++, header);
 
 			/* Add DEBET/KREDIT headers */
 
 			table.getCellFormatter().setStyleName(row + 1, col2, "leftborder");
 			table.setText(row + 1, col2++, elements.debet());
 			table.getCellFormatter().setStyleName(row + 1, col2, "rightborder");
 			table.setText(row + 1, col2++, elements.kredit());
 		}
 	}
 
 	public void serverResponse(JSONValue jsonValue) {
 		JSONObject root = jsonValue.isObject();
 
 		JSONValue monthInfo = root.get("monthinfo");
		
 		JSONObject monthObj = monthInfo.isObject();
 
 		JSONValue debetsums = monthObj.get("debetsums");
 		JSONValue creditsums = monthObj.get("creditsums");
 		JSONValue lines = monthObj.get("lines");
 
 		currentYear = Util.getInt(root.get("year"));
 		currentMonth = Util.getInt(root.get("month"));
 
 		yearMonthComboHelper.setIndex(currentYear, currentMonth);
 
 		JSONArray array = lines.isArray();
 
 		showLines(array);
		if(array.size() > 0) {
		    showDebetCreditSums(debetsums.isObject(), creditsums.isObject());
		}
 
 	}
 
 	private void showDebetCreditSums(JSONObject debetSums, JSONObject creditSums) {
 		int row = table.getRowCount();
 		table.setText(row, 3, elements.sum());
 		table.getRowFormatter().setStyleName(row, "sumline");
 		render_posts(row, debetSums, creditSums, false);
 
 		/* Subtract credit sum from debet sum to show sum for total */
 		for (String key : creditSums.keySet()) {
 			String toSubtract = Util.str(creditSums.get(key));
 
 			String oldValue = "0";
 			if (debetSums.containsKey(key)) {
 				oldValue = Util.str(debetSums.get(key));
 			}
 
 			double sum = Double.parseDouble(oldValue)
 					- Double.parseDouble(toSubtract);
 
 			debetSums.put(key, new JSONString(String.valueOf(sum)));
 		}
 
 		render_posts(row + 1, debetSums, new JSONObject(), false);
 		table.getRowFormatter().setStyleName(row + 1, "sumline");
 	}
 
 	private void showLines(JSONArray array) {
 		/* Every 3. line flip style for row */
 		String rowStyle = "line1";
 
 		/* Renders table with money data */
 		for (int i = 0; i < array.size(); i++) {
 
 			JSONObject rowdata = array.get(i).isObject();
 
 			if (rowdata == null) {
 				Window.alert("Didn't get rowdata:" + array.get(i));
 				continue;
 			}
 			/* +2 to skip headers. */
 			int rowIndex = table.insertRow(i + 2);
 			table.getRowFormatter().setStyleName(rowIndex, rowStyle);
 
 			if (i % 3 == 2) {
 				rowStyle = (rowStyle.equals("line1")) ? "line2" : "line1";
 			}
 
 			table.setText(rowIndex, 0, Util.str(rowdata.get("Postnmb")) + "/"
 					+ Util.str(rowdata.get("Id")));
 			table.getCellFormatter().setStyleName(rowIndex, 0, "right");
 
 			table.setText(rowIndex, 1, Util.str(rowdata.get("Attachment")));
 			table.getCellFormatter().setStyleName(rowIndex, 1, "right");
 
 			table.setText(rowIndex, 2, Util.str(rowdata.get("date")));
 			table.getCellFormatter().setStyleName(rowIndex, 2, "datefor");
 
 			Hyperlink link = new Hyperlink(
 					Util.str(rowdata.get("Description")), "detail"
 							+ Util.str(rowdata.get("Id")));
 			link.addClickHandler(this);
 			table.setWidget(rowIndex, 3, link);
 
 			table.getCellFormatter().setStyleName(rowIndex, 3, "desc");
 
 			boolean flagDebetKreditNotMatching = Util.getBoolean(rowdata.get("debetKreditMismatch"));
             render_posts(rowIndex, rowdata.get("groupDebetMonth"), rowdata
 					.get("groupKredMonth"), flagDebetKreditNotMatching);
 		}
 	}
 
 	private void render_posts(int rowIndex, JSONValue debet, JSONValue kred,
 			boolean flagDebetKreditNotMatching) {
 		JSONObject debetObj = debet.isObject();
 		JSONObject kredObj = kred.isObject();
 
 		int col = 4;
 		for (String k : MonthHeaderCache.getInstance(constants, messages)
 				.keys()) {
 
 			/* DEBET */
 			printDebKredVal(rowIndex, debetObj, col, k, flagDebetKreditNotMatching);
 			col++;
 
 			/* KREDIT */
 			printDebKredVal(rowIndex, kredObj, col, k, flagDebetKreditNotMatching);
 			col++;
 		}
 	}
 
 	private void printDebKredVal(int rowIndex, JSONObject obj, int col,
 			String k, boolean flagDebetKreditNotMatching) {
 		table.getCellFormatter().setStyleName(rowIndex, col, "right");
 
 		if (obj == null) {
 			return;
 		}
 		JSONValue value = obj.get(k);
 
 		if (value != null) {
 			String money = Util.money(Util.fixMoney(Util.str(value)));
 
 			if (flagDebetKreditNotMatching) {
 				table.getCellFormatter().addStyleName(rowIndex, col, "flag");
 			}
 
 			table.setText(rowIndex, col, money);
 		}
 	}
 
 	public void onClick(ClickEvent event) {
 		Widget sender = (Widget) event.getSource();
 		if (sender == backImage) {
 			newView(false);
 		} else if (sender == nextImage) {
 			newView(true);
 		} else {
 			Hyperlink link = (Hyperlink) sender;
 
 			String token = link.getTargetHistoryToken();
 			String line = token.substring(6);
 			openDetails(link, line);
 		}
 	}
 
 	/**
 	 * Opens up the detail window for a given line.
 	 * 
 	 * @param link
 	 * 
 	 * @param line
 	 */
 	private void openDetails(Hyperlink link, String line) {
 		PostView pv = PostView
 				.show(messages, constants, caller, line, elements);
 		int left = link.getAbsoluteLeft() + 100;
 		int top = link.getAbsoluteTop() + 10;
 		pv.setPopupPosition(left, top);
 		pv.show();
 	}
 
 	private void newView(boolean nextMonth) {
 		dockPanel.remove(table);
 		newTable();
 		if (nextMonth) {
 			int m = currentMonth + 1;
 
 			if (m > 12) {
 				int y = currentYear + 1;
 				getData("month=1&year=" + y);
 			} else {
 				getData("month=" + m + "&year=" + currentYear);
 			}
 		} else {
 			int m = currentMonth - 1;
 
 			if (m < 1) {
 				int y = currentYear - 1;
 				getData("month=12&year=" + y);
 			} else {
 				getData("month=" + m + "&year=" + currentYear);
 			}
 		}
 	}
 
 	public void onChange(ChangeEvent event) {
 		ListBox listBox = (ListBox) event.getSource();
 
 		String value = Util.getSelected(listBox);
 		String[] monthYear = value.split("/");
 		init(Integer.parseInt(monthYear[0]), Integer.parseInt(monthYear[1]));
 	}
 
 }
