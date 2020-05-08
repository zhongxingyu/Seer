 package br.com.bluesoft.commons.lang;
 
 import java.sql.Date;
 import java.sql.Timestamp;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.math.NumberUtils;
 import org.joda.time.DateMidnight;
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeUtils;
 import org.joda.time.LocalDate;
 import org.joda.time.Period;
 import org.joda.time.PeriodType;
 import org.joda.time.chrono.GregorianChronology;
 
 import br.com.bluesoft.commons.domain.Periodo;
 
 public class DateUtil {
 
 	private static String DEFAULT_DATE_FORMAT = "dd/MM/yyyy";
 
 	private static final DateFormat formaterMesNumerico = new SimpleDateFormat("MM");
 	private static final DateFormat formaterMesExtenso = new SimpleDateFormat("MMMM");
 	private static final DateFormat formaterDefault = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
 
 	/**
 	 * Obtem o dia do m�s de uma data.
 	 * @param data
 	 * @return Date
 	 */
 	public static int obterDia(final java.util.Date data) {
 		return new DateTime(data).getDayOfMonth();
 	}
 
 	/**
 	 * Obtem o dia no ano de uma data.
 	 * @param data
 	 * @return Date
 	 */
 	public static int obterDiaNoAno(final java.util.Date data) {
 		return new DateTime(data).getDayOfYear();
 	}
 
 	/**
 	 * Obtem o M�s de uma data
 	 * @param data
 	 * @return Date
 	 */
 	public static int obterMes(final java.util.Date data) {
 		return new DateTime(data).getMonthOfYear();
 	}
 
 	/**
 	 * Obtem o ano de uma data
 	 * @param data
 	 * @return Date
 	 */
 	public static int obterAno(final java.util.Date data) {
 		return new DateTime(data).getYear();
 	}
 
 	/**
 	 * Converte um Objeto em Data
 	 * @param date
 	 * @return java.sql.Date
 	 */
 	public static java.sql.Date toDate(final Object d) {
 		if (d instanceof java.sql.Date)
 			return (java.sql.Date) d;
 		else if (d instanceof java.sql.Timestamp)
 			return new java.sql.Date(((Timestamp) d).getTime());
 		else if (d instanceof String)
 			return toDate(((String) d));
 		else
 			return null;
 	}
 
 	/**
 	 * Converte um Objeto em Data
 	 * @param date
 	 * @return java.sql.Date
 	 */
 	public static java.sql.Timestamp toTimestamp(final Object d) {
 		if (d instanceof java.sql.Timestamp)
 			return (java.sql.Timestamp) d;
 		else if (d instanceof java.util.Date)
 			return new java.sql.Timestamp(((java.util.Date) d).getTime());
 		else
 			return null;
 	}
 
 	/**
 	 * Converte uma String em Data
 	 * @param date
 	 * @return java.sql.Date
 	 */
 	public static java.sql.Date toDate(final String date) {
 		if (date == null)
 			return null;
 		else if (date.length() <= 0)
 			return null;
 		try {
 			return toDate(formaterDefault.parse(date));
 		} catch (final ParseException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	/**
 	 * Transforma uma String data segundo um determinado padr�o.
 	 * @param data
 	 * @param pattern
 	 * @return Date
 	 * @throws ParseException
 	 */
 	public static Date toDate(final String data, final String pattern) throws ParseException {
 		final SimpleDateFormat df = new SimpleDateFormat(pattern);
 		return new java.sql.Date(df.parse(data).getTime());
 	}
 
 	/**
 	 * Converte um java.util.Date em um java.sql.Date
 	 * @param date
 	 * @return Date
 	 */
 	public static java.sql.Date toDate(final java.util.Date date) {
 		if (date == null)
 			return null;
 
		return new java.sql.Date(date.getTime());
 	}
 
 	public static void setTimeDelay(final long timeDelay) {
 		DateTimeUtils.setCurrentMillisOffset(timeDelay);
 	}
 
 	public static long currentTimeMillis() {
 		return DateTimeUtils.currentTimeMillis();
 	}
 
 	/**
 	 * Obtem a data Atual
 	 */
 	public static Date getDate() {
		return new java.sql.Date(currentTimeMillis());
 	}
 
 	public static int getAnoAtual() {
 		return obterAno(getDate());
 	}
 
 	public static int getMesAtual() {
 		return obterMes(getDate());
 	}
 
 	/**
 	 * Obtem a data e hora atual
 	 */
 	public static java.sql.Timestamp getTimestamp() {
 		return new java.sql.Timestamp(currentTimeMillis());
 	}
 
 	public static Date getDate(final int dias) {
 		final DateMidnight date = new DateMidnight(GregorianChronology.getInstance()).plus(new Period(0, 0, 0, dias, 0, 0, 0, 0));
 		return new java.sql.Date(date.toDate().getTime());
 	}
 
 	public static String formatDate(final java.util.Date date) {
 		if (date == null)
 			return StringUtil.EMPTY;
 		else
 			return formaterDefault.format(date);
 	}
 
 	public static String formatDate(final java.util.Date data, final String pattern) {
 		return new SimpleDateFormat(pattern).format(data);
 	}
 
 	public static Calendar toCalendar(final Object date) {
 		if (date == null)
 			return null;
 
 		final Calendar calendar = Calendar.getInstance();
 		if (date instanceof java.util.Date)
 			calendar.setTime((java.util.Date) date);
 		else if (date instanceof String)
 			calendar.setTime(toDate(date));
 
 		return calendar;
 	}
 
 	public static Periodo getAnoMovel() {
 
 		final GregorianCalendar gCalendar = new GregorianCalendar();
 		gCalendar.set(Calendar.DAY_OF_MONTH, gCalendar.getMaximum(Calendar.DAY_OF_MONTH));
 		final java.util.Date dataFinal = gCalendar.getTime();
 		gCalendar.add(Calendar.MONTH, -11);
 		gCalendar.set(Calendar.DAY_OF_MONTH, gCalendar.getMinimum(Calendar.DAY_OF_MONTH));
 		final java.util.Date dataInicial = gCalendar.getTime();
 		return new Periodo(new Date(dataInicial.getTime()), new Date(dataFinal.getTime()));
 	}
 
 	public static Date getPrimeiroDiaDaSemana(final Date data) {
 		final GregorianCalendar calendar = new DateTime(data).toGregorianCalendar();
 
 		final int diaDaSemanaAtual = calendar.get(Calendar.DAY_OF_WEEK);
 		if (diaDaSemanaAtual > 1) {
 			calendar.set(Calendar.DAY_OF_WEEK, 1);
 			calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) - 7);
 		}
 
 		return new java.sql.Date(calendar.getTime().getTime());
 	}
 
 	public static Date getPrimeiroDiaDoMes(final Date data) {
 		final GregorianCalendar calendar = new DateTime(data).toGregorianCalendar();
 		calendar.set(Calendar.DAY_OF_MONTH, 1);
 		return new java.sql.Date(calendar.getTime().getTime());
 	}
 
 	public static Date getUltimoDiaDoMes(final Date data) {
 		final GregorianCalendar calendar = new DateTime(data).toGregorianCalendar();
 		calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
 		return new java.sql.Date(calendar.getTime().getTime());
 	}
 
 	public static Date getUltimoDiaDaSemana(final Date data) {
 		final GregorianCalendar calendar = new DateTime(data).toGregorianCalendar();
 
 		final int diaDaSemanaAtual = calendar.get(Calendar.DAY_OF_WEEK);
 		if (diaDaSemanaAtual < 7)
 			calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 7 - diaDaSemanaAtual);
 
 		return new java.sql.Date(calendar.getTime().getTime());
 	}
 
 	public static Periodo getPeriodoQuinzenal(final Date data) {
 
 		final GregorianCalendar calendar = new DateTime(data).toGregorianCalendar();
 		calendar.set(Calendar.HOUR, 0);
 		calendar.set(Calendar.MINUTE, 0);
 		calendar.set(Calendar.SECOND, 0);
 		calendar.set(Calendar.MILLISECOND, 0);
 
 		java.util.Date dataInicial;
 		java.util.Date dataFinal;
 		if (calendar.get(Calendar.DAY_OF_MONTH) < 16) {
 			calendar.set(Calendar.DAY_OF_MONTH, 1);
 			dataInicial = calendar.getTime();
 			calendar.set(Calendar.DAY_OF_MONTH, 15);
 			dataFinal = calendar.getTime();
 		} else {
 			calendar.set(Calendar.DAY_OF_MONTH, 16);
 			dataInicial = calendar.getTime();
 			calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
 			dataFinal = calendar.getTime();
 		}
 
 		return Periodo.valueOf(dataInicial, dataFinal);
 
 	}
 
 	public static Periodo getPeriodoMensal(final Date data) {
 
 		final GregorianCalendar calendar = new DateTime(data).toGregorianCalendar();
 		calendar.set(Calendar.HOUR, 0);
 		calendar.set(Calendar.MINUTE, 0);
 		calendar.set(Calendar.SECOND, 0);
 		calendar.set(Calendar.MILLISECOND, 0);
 
 		calendar.set(Calendar.DAY_OF_MONTH, 1);
 		final java.util.Date dataInicial = calendar.getTime();
 		calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
 		final java.util.Date dataFinal = calendar.getTime();
 
 		return Periodo.valueOf(dataInicial, dataFinal);
 
 	}
 
 	public static Periodo getPeriodoSemanal(final Date data) {
 		return Periodo.valueOf(getPrimeiroDiaDaSemana(data), getUltimoDiaDaSemana(data));
 	}
 
 	public static Periodo getPeriodoAnual(final Date data) {
 		final GregorianCalendar calendar = new DateTime(data).toGregorianCalendar();
 		calendar.set(Calendar.HOUR, 0);
 		calendar.set(Calendar.MINUTE, 0);
 		calendar.set(Calendar.SECOND, 0);
 		calendar.set(Calendar.MILLISECOND, 0);
 
 		calendar.set(Calendar.DAY_OF_YEAR, 1);
 		final java.util.Date dataInicial = calendar.getTime();
 		calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR));
 		final java.util.Date dataFinal = calendar.getTime();
 
 		return Periodo.valueOf(dataInicial, dataFinal);
 	}
 
 	public static Date addDays(final Date date, final int days) {
		return toDate(new DateTime(date).plusDays(days));
 
 	}
 
 	public static int getDiferencaEntreDatas(final Date dataInicial, final Date dataFinal) {
 		return diferencaEntreDatasEmDias(dataInicial, dataFinal);
 	}
 
 	public static int diferencaEntreDatasEmDias(final Date dataInicial, final Date dataFinal) {
 		final LocalDate data1 = new LocalDate(dataInicial.getTime());
 		final LocalDate data2 = new LocalDate(dataFinal.getTime());
 
 		final Period period = new Period(data1, data2, PeriodType.dayTime());
 
 		return period.getDays();
 	}
 
 	public static boolean diferencaEntreDatasEhMaiorQueQuantidadeDeDias(final Date dataInicial, final Date dataFinal, final int quantidadeDeDias) {
 		return diferencaEntreDatasEmDias(dataInicial, dataFinal) > quantidadeDeDias;
 	}
 
 	public static boolean getPrimeiraDataEhMaiorQueSegundaData(final Date dataInicial, final Date dataFinal) {
 		return diferencaEntreDatasEmDias(dataInicial, dataFinal) < 0;
 	}
 
 	public static boolean getPrimeiraDataEhMenorQueSegundaData(final Date dataInicial, final Date dataFinal) {
 		return diferencaEntreDatasEmDias(dataInicial, dataFinal) > 0;
 	}
 
 	public static boolean getPrimeiraDataEhIgualASegundaData(final Date dataInicial, final Date dataFinal) {
 		return diferencaEntreDatasEmDias(dataInicial, dataFinal) == 0;
 	}
 
 	/**
 	 * Converte uma data no formato dd/MM/yyyy em MM-yyyy
 	 * @param data
 	 * @return
 	 */
 	public static String toCompetencia(final Date data) {
 		final DateFormat dfCompetencia = new SimpleDateFormat("MM-yyyy");
 		return dfCompetencia.format(data);
 	}
 
 	/**
 	 * Converte uma data para um número inteiro no formato "yyyyMMdd", ex: 07/09/2006 -> 20060907
 	 * @param date
 	 * @return int
 	 */
 	public static int dateToInt(final java.util.Date date) {
 		final DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
 		return date == null ? 0 : NumberUtils.toInt(dateFormat.format(date));
 	}
 
 	public static int competenciaToAnoMes(final String competencia) {
 		try {
 			final DateFormat dfCompetencia = new SimpleDateFormat("MM-yyyy");
 			final DateFormat dfAnoMes = new SimpleDateFormat("yyyyMM");
 			return NumberUtil.toInt(dfAnoMes.format(dfCompetencia.parse(competencia)));
 		} catch (final Exception e) {
 			return 0;
 		}
 	}
 
 	public static java.sql.Date mesAnoToDateSql(final int mesAno) {
 		try {
 			final DateFormat dateFormat = new SimpleDateFormat("yyyyMM");
 			return new java.sql.Date(dateFormat.parse(String.valueOf(mesAno)).getTime());
 		} catch (final Exception e) {
 			return null;
 		}
 	}
 
 	/**
 	 * Converte uma número inteiro no formato"yyyyMMdd" para uma data
 	 * @param date
 	 * @return java.util.Date
 	 */
 	public static java.util.Date intToDate(final int date) {
 		final DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
 		if (date < 10000000)
 			return null;
 		try {
 			return dateFormat.parse(String.valueOf(date));
 		} catch (final Exception e) {
 			return null;
 		}
 	}
 
 	public static int obterTotalDeDiasDoAno(final int ano) {
 		final GregorianCalendar calendar = new GregorianCalendar(ano, 1, 1);
 		return calendar.getActualMaximum(Calendar.DAY_OF_YEAR);
 	}
 
 	public static String getPrimeiraCompetenciaDoAno(final int ano) {
 		final GregorianCalendar calendar = new GregorianCalendar(ano, 1, 1);
 		calendar.set(Calendar.HOUR, 0);
 		calendar.set(Calendar.MINUTE, 0);
 		calendar.set(Calendar.SECOND, 0);
 		calendar.set(Calendar.MILLISECOND, 0);
 
 		calendar.set(Calendar.DAY_OF_YEAR, 1);
 
 		final java.util.Date data = calendar.getTime();
 
 		final DateFormat dfCompetencia = new SimpleDateFormat("MM-yyyy");
 		return dfCompetencia.format(toDate(data));
 
 	}
 
 	public static String getUltimaCompetenciaDoAno(final int ano) {
 		final GregorianCalendar calendar = new GregorianCalendar(ano, 1, 1);
 		calendar.set(Calendar.HOUR, 0);
 		calendar.set(Calendar.MINUTE, 0);
 		calendar.set(Calendar.SECOND, 0);
 		calendar.set(Calendar.MILLISECOND, 0);
 
 		calendar.set(Calendar.DAY_OF_YEAR, 1);
 		calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR));
 
 		final java.util.Date dataFinal = calendar.getTime();
 
 		final DateFormat dfCompetencia = new SimpleDateFormat("MM-yyyy");
 		return dfCompetencia.format(toDate(dataFinal));
 
 	}
 
 	public static String anoMesToCompetencia(final int anoMes) {
 		try {
 			final DateFormat dfAnoMes = new SimpleDateFormat("yyyyMM");
 			final DateFormat dfCompetencia = new SimpleDateFormat("MM-yyyy");
 			return dfCompetencia.format(dfAnoMes.parse(String.valueOf(anoMes)));
 		} catch (final Exception e) {
 			return null;
 		}
 	}
 
 	public static String diaMesToDiaMesFormatado(final int diaMes) {
 		try {
 			final DateFormat dfAnoMes = new SimpleDateFormat("ddMM");
 			final DateFormat dfDiaMesFormatado = new SimpleDateFormat("dd/MM");
 			return dfDiaMesFormatado.format(dfAnoMes.parse(PositionalFileUtil.formatNumber(diaMes, 4)));
 		} catch (final Exception e) {
 			return null;
 		}
 	}
 
 	public static String obterDataPorExtenso(final Date data) {
 		final DateFormat dayFormat = new SimpleDateFormat("dd");
 		final DateFormat monthFormat = new SimpleDateFormat("MMMMM");
 		final DateFormat yearFormat = new SimpleDateFormat("yyyy");
 
 		return dayFormat.format(data) + " de " + monthFormat.format(data) + " de " + yearFormat.format(data);
 	}
 
 	public static String obterMesPorExtenso(final int mes) {
 		try {
 			return formaterMesExtenso.format(formaterMesNumerico.parse(StringUtils.leftPad(String.valueOf(mes), 2, '0')));
 		} catch (final Exception e) {
 			return null;
 		}
 	}
 }
