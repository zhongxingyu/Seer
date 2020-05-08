 package junit.tutorial;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 
 import org.hamcrest.BaseMatcher;
 import org.hamcrest.Description;
 import org.hamcrest.Matcher;
 
 public class IsDate extends BaseMatcher<Date> {
 
 	private final int yyyy;
 	private final int mm;
 	private final int dd;
 	private Object actual;
 
 	IsDate(int yyyy, int mm, int dd) {
 		this.yyyy = yyyy;
 		this.mm = mm;
 		this.dd = dd;
 	}
 
 	@Override
 	public boolean matches(Object actual) {
 		this.actual = actual;
 		if (!(actual instanceof Date)) return false;
 		Calendar cal = Calendar.getInstance();
 		cal.setTime((Date)actual);
 		if (yyyy != cal.get(Calendar.YEAR)) return false;
		if (mm != cal.get(Calendar.MONTH)) return false;
 		if (dd != cal.get(Calendar.DATE)) return false;
 		return true;
 	}
 
 	// matches メソッドがfalseを返した場合このメソッドが呼び出される
 	// フレームワークが比較検証に失敗した理由を通知するメソッド
 	@Override
 	public void describeTo(Description desc) {
 		desc.appendValue(String.format("%d/%02d/%02d", yyyy, mm, dd));
 		if (actual != null) {
 			desc.appendText(" but actual is ");
			desc.appendValue(new SimpleDateFormat("yyyy//mm/dd").format((Date) actual));
 		}
 	}
 
 	public static Matcher<Date> dateOf(int yyyy, int mm, int dd) {
 		return new IsDate(yyyy, mm, dd);
 	}
 }
