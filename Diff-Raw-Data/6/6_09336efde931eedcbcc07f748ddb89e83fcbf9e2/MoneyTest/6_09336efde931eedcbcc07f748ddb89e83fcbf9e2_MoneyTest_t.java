 package de.gzockoll.types.money;
 
 import static org.hamcrest.core.Is.*;
 import static org.junit.Assert.*;
 
 import java.math.BigDecimal;
 import java.util.Locale;
 
 import org.joda.time.DateTime;
 import org.joda.time.Interval;
 import org.junit.Ignore;
 import org.junit.Test;
 
 import com.ibm.icu.util.Currency;
 import com.ibm.icu.util.ULocale;
 
 /**
  * Test for the money class.
  * 
  * Initially copied with permission from
  * https://github.com/MehrCurry/Money/blob/
  * gpc/src/test/java/de/gzockoll/types/money/MoneyTest.java.
  */
 public class MoneyTest {
 
 	/** EUR currencty. */
 	public static Currency EUR = Currency.getInstance("EUR");
 
 	/**
 	 * French Polynesia does not have subunit for their currency
 	 */
 	@Test
 	public void checkCurrencyWithUnusualFraction_FrenchPolynesia() {
 		Currency cfpFranc = Currency.getInstance(new ULocale.Builder()
 				.setRegion("PF").setLanguage("fr").build());
 		assertThat(cfpFranc.getDefaultFractionDigits(), is(0));
 		Money one = Money.fromMinor(1000, cfpFranc);
 		assertThat(one.getAmount().scale(), is(0));
 		assertThat(one.getAmount(), is(new BigDecimal("1000")));
 		assertThat(Money.fromMajor(33, cfpFranc),
 				is(Money.fromMinor(33, cfpFranc)));
 
 	}
 
 	/**
 	 * We can successfully build an currency object but this object does not
 	 * support the subunit called "Iraimbilanja"
 	 */
 	@Test
 	public void checkCurrencyWithUnusualFraction_Madagaskar() {
 		Currency ariary = Currency.getInstance(new ULocale.Builder().setRegion(
 				"MG").build());
 		assertThat(ariary.getDisplayName(), is("Madagaskar Ariary"));
 
 	}
 
 	/**
 	 * The Madagaskar Ariary does not fit in the decimal system so these test
 	 * will fail or doesnt even make sense
 	 */
 	@Ignore
 	@Test
 	public void checkCurrencyWithUnusualFraction_Madagaskar_SubUnit() {
 		Currency ariary = Currency.getInstance(new ULocale.Builder().setRegion(
 				"MG").build());
 		assertThat(ariary.getDisplayName(), is("Madagaskar Ariary"));
 		Money one = Money.fromMinor(5, ariary);
 		assertThat(one.getAmount(), is(new BigDecimal("1")));
 		assertThat(ariary.getDefaultFractionDigits(), is(0));
 
 	}
 
 	/**
 	 * Oman uses a 3-digit subunit for their currency
 	 */
 	@Test
 	public void checkCurrencyWithUnusualFraction_Oman() {
 		Currency omanRial = Currency.getInstance(new ULocale.Builder()
 				.setRegion("OM").setLanguage("ar").build());
 		assertThat(omanRial.getDefaultFractionDigits(), is(3));
 		Money one = Money.fromMinor(1000, omanRial);
 		assertThat(one.getAmount().scale(), is(3));
 		assertThat(one.getAmount(), is(new BigDecimal("1.000")));
 	}
 
 	/** Tests adding. */
 	@Test
 	public void testAdd() {
 		Money m1 = Money.euros(10);
 		Money m2 = Money.euros(11);
 		Money m3 = m1.add(m2);
 		assertThat(m3, is(Money.euros(21)));
 	}
 
 	/** Tests immutability of add method. */
 	@Test
 	public void testAddIsImmutable() {
 		Money m1 = Money.euros(10);
 		Money m2 = Money.euros(11);
 		Money m3 = m1.add(m2);
 		assertThat(m3, is(Money.euros(21)));
 		assertThat(m1, is(Money.euros(10)));
 	}
 
 	/**
 	 * Tests compound interest calculation with Euro.
 	 */
 	@Test
 	public void testCompountInterstEuro() {
 		Money m = Money.euros(100);
 		BigDecimal factor = new BigDecimal("1.03");
 		for (int i = 0; i < 400; i++) {
 			m = m.multiply(factor);
 		}
 		assertThat(m.scaled(), is(Money.fromMinor(1364237182, EUR)));
 	}
 
 	/**
 	 * Tests compound interest calculation with yen.
 	 */
 	@Test
 	public void testCompountInterstJen() {
 		Currency yen = Currency.getInstance("JPY");
 		Money m = Money.fromMajor(100, yen);
 		BigDecimal factor = new BigDecimal("1.03");
 		for (int i = 0; i < 400; i++) {
 			m = m.multiply(factor);
 		}
 		assertThat(m.scaled(), is(Money.fromMinor(13642372, yen)));
 	}
 
 	/**
 	 * Tests constructors.
 	 */
 	@Test
 	public void testConstuctorsEuro() {
 		Money m1 = Money.fromMajor(19, EUR);
 		Money m2 = Money.fromMinor(1900, EUR);
 		assertThat(m1, is(m2));
 	}
 
 	/**
 	 * Tests constructors.
 	 */
 	@Test
 	public void testConstuctorsYen() {
 		Currency yen = Currency.getInstance("JPY");
 		Money m1 = Money.fromMajor(19, yen);
 		Money m2 = Money.fromMinor(19, yen);
 		assertThat(m1, is(m2));
 	}
 
 	/**
 	 * Tests equals.
 	 */
 	@Test
 	public void testEquals() {
 		Money m1 = Money.euros(10);
 		Money m2 = Money.euros(11);
 		Money m3 = Money.euros(11);
 		assertThat(m1.equals(m2), is(false));
 		assertThat(m2.equals(m1), is(false));
 		assertThat(m1.equals(m3), is(false));
 		assertThat(m3.equals(m1), is(false));
 		assertThat(m3.equals(m2), is(true));
 		assertThat(m2.equals(m3), is(true));
 
 	}
 
 	/**
 	 * Tests hashcode.
 	 */
 	@Test
 	public void testHashCode() {
 		Money m1 = Money.euros(10);
 		Money m2 = Money.euros(11);
 		assertThat(m1.hashCode() == m2.hashCode(), is(false));
 		m1 = m1.add(Money.euros(1));
 		assertThat(m1, is(m2));
 		assertThat(m1.hashCode() == m2.hashCode(), is(true));
 	}
 
 	/**
 	 * Tests minor.
 	 */
 	@Test
 	public void testMinor() {
 		Money m1 = Money.fromMinor(1234, EUR);
 		assertThat(m1.asMinor(), is(1234l));
 
 		Money m2 = Money.fromMajor(12, EUR);
 		assertThat(m2.asMinor(), is(1200l));
 	}
 
 	/**
 	 * Tests multi allocation.
 	 */
 	@Test
 	public void testMultiAllocation() {
 		Money m = Money.euros(60);
 
 		Money[] alloc = m.allocate(new long[] { 3, 2, 1 });
 		assertThat(alloc.length, is(3));
 		assertThat(alloc[0], is(Money.euros(30)));
 		assertThat(alloc[1], is(Money.euros(20)));
 		assertThat(alloc[2], is(Money.euros(10)));
 	}
 
 	/**
 	 * Tests multi allocation with remainder.
 	 */
 	@Test
 	public void testMultiAllocationWithRemainder() {
 		Money m = Money.fromMinor(6002, EUR);
 
 		Money[] alloc = m.allocate(new long[] { 3, 2, 1 });
 		assertThat(alloc.length, is(3));
 		assertThat(alloc[0], is(Money.fromMinor(3001, EUR)));
 		assertThat(alloc[1], is(Money.fromMinor(2001, EUR)));
 		assertThat(alloc[2], is(Money.euros(10)));
 	}
 
 	/**
 	 * Tests multiply.
 	 */
 	@Test
 	public void testMultiply() {
 		Money m = Money.euros(100);
 		Money expected = Money.euros(19);
 		Money result = m.multiply(0.19).scaled();
 		assertThat(result, is(expected));
 		m = Money.euros(95);
 		result = m.multiply(0.19).scaled();
 		assertThat(result, is(Money.fromMinor(1805, EUR)));
 	}
 
 	/**
 	 * Tests negate.
 	 */
 	@Test
 	public void testNegate() {
 		Money m = Money.euros(10);
 		assertThat(m.negate(), is(Money.euros(-10)));
 		assertThat(m.negate().negate(), is(m));
 	}
 
 	/**
 	 * Test formatting
 	 */
 	@Test
 	public void testNumberFormat() {
 		Currency c2 = Currency.getInstance(Locale.US);
 		Money m2 = Money.fromMinor(10001, c2);
 
 		assertThat(m2.getAsFormattedString(Locale.US), is("$100.01"));
 	}
 
 	/**
 	 * Tests remainder allocation
 	 */
 	@Test
 	public void testRemainderAllocation() {
 		Money m = Money.euros(59);
 
 		Money[] alloc = m.allocate(3);
 		assertThat(alloc[0], is(Money.fromMinor(1967, EUR)));
 		assertThat(alloc[1], is(Money.fromMinor(1967, EUR)));
 		assertThat(alloc[2], is(Money.fromMinor(1966, EUR)));
 	}
 
 	/**
 	 * Tests simple allocation.
 	 */
 	@Test
 	public void testSimpleAllocation() {
 		Money m = Money.euros(60);
 
 		Money[] alloc = m.allocate(6);
 		for (Money x : alloc) {
 			assertThat(x, is(Money.euros(10)));
 		}
 	}
 
 	/**
 	 * Tests subtract.
 	 */
 	@Test
 	public void testSubtract() {
 		Money m1 = Money.euros(10);
 		Money m2 = Money.euros(11);
 		assertThat(m2.subtract(m1), is(Money.euros(1)));
 	}
 
 	/**
 	 * Test Currency Numeric Code
 	 */
 	@Test
 	public void the_currency_object_should_return_the_iso4217_numeric_code() {
 		Currency c1 = Currency.getInstance(Locale.UK);
 		Money m1 = Money.fromMajor(100, c1);
 		assertThat(m1.getCurrency().getNumericCode(), is(826));
 
 		Currency c2 = Currency.getInstance(Locale.US);
 		Money m2 = Money.fromMajor(100, c2);
 		assertThat(m2.getCurrency().getNumericCode(), is(840));
 
 		Currency c3 = Currency.getInstance(new ULocale.Builder()
 				.setRegion("SA").setLanguage("ar").build());
 		Money m3 = Money.fromMajor(100, c3);
 		assertThat(m3.getCurrency().getNumericCode(), is(682));
 
 		Money m4 = Money.fromMinor(37, Currency.getInstance("SEK"));
 		assertThat(m4.getCurrency().getNumericCode(), is(752));
 
 	}
 
 	/**
 	 * Test ISO Code As String
 	 */
 	@Test
 	public void the_currency_object_should_return_the_iso4217_string() {
 		Currency yen = Currency.getInstance(Locale.JAPAN);
 		Money m = Money.fromMajor(100, yen);
 		assertThat(m.getCurrency().getCurrencyCode(), is("JPY"));
 
 		Currency yuan = Currency.getInstance(Locale.CHINA);
 		Money m2 = Money.fromMajor(100, yuan);
 		assertThat(m2.getCurrency().getCurrencyCode(), is("CNY"));
 	}
 
 	/**
 	 * Test Currency Symbol
 	 */
 	@Test
 	public void the_currency_object_should_return_the_iso4217_symbol() {
		// Currency c1 = Currency.getInstance(Locale.UK);
		// Money m1 = Money.fromMajor(100, c1);
		// assertThat(m1.getCurrency().getSymbol(Locale.UK), is("£"));
 
 		Currency c2 = Currency.getInstance(Locale.US);
 		Money m2 = Money.fromMajor(100, c2);
 		assertThat(m2.getCurrency().getSymbol(Locale.US), is("$"));
 	}
 
 	/**
 	 * Test invalid currency
 	 */
 	@Test(expected = IllegalArgumentException.class)
 	public void using_fromMajor_with_an_invalid_currency_should_throw_an_exception() {
 		Currency c1 = Currency.getInstance("XYZ");
 		Money m1 = Money.fromMajor(100, c1);
 		fail("Exception expected!");
 	}
 
 	/**
 	 * Test invalid currency
 	 */
 	@Test(expected = IllegalArgumentException.class)
 	public void using_fromMinor_with_an_invalid_currency_should_throw_an_exception() {
 		Currency c1 = Currency.getInstance("XYZ");
 		Money m1 = Money.fromMinor(100, c1);
 		fail("Exception expected!");
 	}
 
 	/**
 	 * Test franc before 2002
 	 */
 	@Test
 	public void franc_should_be_valid_before_2002() {
 		Currency franc = Currency.getInstance("FRF");
 		DateTime aDate = new DateTime(2001, 12, 1, 0, 0);
 		assertThat(Money.isCurrencyValid(franc, new Interval(aDate, aDate)),
 				is(true));
 	}
 
 	/**
 	 * Test franc after 2002
 	 */
 	@Test
 	public void franc_should_be_invalid_after_2002() {
 		Currency franc = Currency.getInstance("FRF");
 		DateTime aDate = new DateTime(2002, 12, 1, 0, 0);
 		assertThat(Money.isCurrencyValid(franc, new Interval(aDate, aDate)),
 				is(false));
 	}
 }
