 package notifier;
 
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.Locale;
 import java.util.TimeZone;
 import java.util.logging.Logger;
 import java.util.regex.Pattern;
 
 import javax.jdo.Extent;
 import javax.jdo.PersistenceManager;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.htmlparser.Node;
 import org.htmlparser.Parser;
 import org.htmlparser.filters.HasAttributeFilter;
 import org.htmlparser.filters.TagNameFilter;
 import org.htmlparser.tags.LinkTag;
 import org.htmlparser.util.NodeList;
 import org.htmlparser.util.SimpleNodeIterator;
 
 //履歴
 //2010/5/4 16:30 作成
 @SuppressWarnings("serial")
 public class SRMUpdaterServlet extends HttpServlet {
 	private static final Logger log = Logger.getLogger(SRMNotifierServlet.class
 			.getName());
 	private static final Locale japan = Locale.JAPAN;
 	private static final SimpleDateFormat format;
 	private static final int updateScheduleHour = 15;
 	private static final String[] months = { "jan", "feb", "mar", "apr", "may",
 			"jun", "jul", "aug", "sep", "oct", "nov", "dec" };
 	static {
 		format = new SimpleDateFormat("yyyy年MM月dd日（E） HH時mm分", japan);
 		format.setTimeZone(TimeZone.getTimeZone("GMT+09:00"));
 	}
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		GregorianCalendar cal = new GregorianCalendar();
 		Date now = cal.getTime();
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		try {
 			log.info("[" + now + ":" + format.format(now) + "]");
 			updateSRM(pm, now, "thisMonth");
 			// 次の月のSRM更新を一日に一回
 			if (cal.get(Calendar.HOUR_OF_DAY) == updateScheduleHour) {
 				cal.add(Calendar.MONTH, 1);
 				int nextMonth = cal.get(Calendar.MONTH);
 				int year = cal.get(Calendar.YEAR) % 100;
 				updateSRM(pm, now, months[nextMonth] + "_" + year);
 			}
 		} catch (Exception e) {
 			log.warning(e.getMessage());
 		} finally {
 			pm.close();
 		}
 	}
 
 	private void updateSRM(PersistenceManager pm, Date now, String month) {
 		List<SRM> updates = getSRMs(month);
 		Extent<SRM> extent = pm.getExtent(SRM.class, true);
 		for (SRM srm : extent) {
 			log.info("格納済みデータ" + srm);
 			// check update
 			for (SRM update : new ArrayList<SRM>(updates)) {
 				if ((srm.getName().equals(update.getName()))
 						&& !srm.equals(update)) {
 					srm.update(update);
 					updates.remove(update);
 					log.info(srm.getName() + " のデータを更新 to " + srm);
 				} else if (srm.equals(update)) {
 					updates.remove(update);
 				}
 			}
 		}
 		// insert new dates
 		// SRM nearest = getNearestSRM(pm); // 2010/7/19 19:18
 		// 更新通知に必要だけどいらないので無効化
 		for (SRM update : updates) {
 			log.info("追加する？ :" + "[update=" + update + "]" + "[now=" + now
 					+ "] u.isafter(n) " + update.getRegisterTime().after(now));
 			if (update.getRegisterTime().after(now)) {
 				try {
 					pm.makePersistent(update);
 					log.info(update.getName() + " のデータを追加 " + update);
 				} catch (Exception e) {
 					log.warning(e.getMessage());
 				}
 			}
 		}
 	}
 
 	private ArrayList<SRM> getSRMs(String month) {
 		ArrayList<SRM> result = new ArrayList<SRM>();
 		try {
 			Parser parser = new Parser(
 					"http://www.topcoder.com/tc?module=Static&d1=calendar&d2="
 							+ month);
			log
					.info("カレンダー取得 from http://www.topcoder.com/tc?module=Static&d1=calendar&d2="
							+ month);
 			NodeList list = parser
 					.parse(new HasAttributeFilter("class", "srm"));
 			SimpleNodeIterator it = list.elements();
 			while (it.hasMoreNodes()) {
 				NodeList children = it.nextNode().getChildren();
 				children.keepAllNodesThatMatch(new TagNameFilter("a"));
 				Node a = children.elementAt(0);
 				if (a instanceof LinkTag) {
 					LinkTag link = (LinkTag) a;
 					SRM srm = new SRM();
 					srm.setName(link.toPlainTextString());
 					srm.setUrl(link.getLink());
 					List<Date> dates = getTimes(srm.getUrl());
 					srm.setRegisterTime(dates.get(0));
 					srm.setCompetisionTime(dates.get(1));
 					srm.setCount(0);
 					result.add(srm);
 					log.info("SRMデータ解析 : " + srm);
 				}
 			}
 		} catch (Exception e) {
 			log.warning(e.getMessage());
 		}
 		return result;
 	}
 
 	private List<Date> getTimes(String url) {
 		ArrayList<Date> dates = new ArrayList<Date>();
 		try {
 			Parser parser = new Parser(url);
 			NodeList list = parser.parse(new HasAttributeFilter("class",
 					"statText"));
 			SimpleNodeIterator it = list.elements();
 			String day = "";
 			while (it.hasMoreNodes()) {
 				Node node = it.nextNode();
 				String text = node.toPlainTextString().replaceAll("\\s", "");
 				if (Pattern.matches("..\\..+", text)) {
 					day = text;
 				} else {
 					// 02.17.201006:00PMEST "MM.dd.yyyyhh:mmaz"
 					// version5追記 EDTなるサマータイムが存在．でもjavaだから問題なかった．
 					SimpleDateFormat format2 = new SimpleDateFormat(
 							"MM.dd.yyyyhh:mmaz", Locale.US);
 					Date parse = format2.parse(day + text);
 					dates.add(parse);
 					log.info("データ取得 from " + url + ",date=" + day + text
 							+ " to " + parse + " and " + format.format(parse));
 
 				}
 			}
 		} catch (Exception e) {
 		}
 		return dates;
 	}
 
 }
