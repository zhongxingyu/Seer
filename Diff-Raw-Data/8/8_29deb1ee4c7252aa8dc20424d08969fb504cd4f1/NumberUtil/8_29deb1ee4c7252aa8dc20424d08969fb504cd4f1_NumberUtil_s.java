 package br.com.bluesoft.commons.lang;
 
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.text.NumberFormat;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.lang.math.NumberUtils;
 
 public class NumberUtil {
 
 	private NumberUtil() {
 
 	}
 
 	/**
 	 * Retorna Zero se o Valor de Entrada for null
 	 */
 	public static int toZeroIfNull(final Integer number) {
 		if (number == null) {
 			return 0;
 		} else {
 			return number;
 		}
 	}
 
 	/**
 	 * Retorna Zero se o Valor de Entrada for null
 	 */
 	public static long toZeroIfNull(final Long number) {
 		if (number == null) {
 			return 0L;
 		} else {
 			return number;
 		}
 	}
 
 	/**
 	 * Retorna Zero se o Valor de Entrada for null
 	 * @param number
 	 * @return
 	 */
 	public static double toZeroIfNull(final Double number) {
 		if (number == null) {
 			return 0d;
 		} else {
 			return number;
 		}
 	}
 
 	/**
 	 * Retorna Zero se o Valor de Entrada for null
 	 */
 	public static float toZeroIfNull(final Float number) {
 		if (number == null) {
 			return 0f;
 		} else {
 			return number;
 		}
 	}
 
 	public static double toDouble(final String numero, final int casas) {
 		return NumberUtil.toDouble(numero) / Math.pow(10, casas);
 
 	}
 
 	public static double toDouble(final Object o) {
 		if (o == null) {
 			return 0d;
 		} else if (o instanceof BigDecimal) {
 			return ((BigDecimal) o).doubleValue();
 		} else {
 			return NumberUtils.toDouble(String.valueOf(o));
 		}
 	}
 
 	public static double toDouble(final Object o, final int casas) {
 		if (o == null) {
 			return 0d;
 		} else if (o instanceof BigDecimal) {
 			return ((BigDecimal) o).setScale(casas, RoundingMode.HALF_UP).doubleValue();
 		} else {
 			return NumberUtils.toDouble(String.valueOf(o), casas);
 		}
 	}
 
 	/**
 	 * Tenta converter uma String para Double caso não seja possível retorna o valor padrão (defaultValue).
 	 */
 	public static Double toDouble(final String parameter, final Double defaultValue) {
 		try {
 			return Double.parseDouble(parameter);
 		} catch (final Exception e) {
 			return defaultValue;
 		}
 	}
 
 	/**
 	 * Converte um double em int fazendo o arredondamento.
 	 */
 	public static int toInt(final double number) {
 		return BigDecimal.valueOf(number).intValue();
 	}
 
 	/**
 	 * Converte um bigDecimal para inteiro.
 	 */
 	public static int toInt(final BigDecimal number) {
 		if (number == null) {
 			return 0;
 		} else {
 			return number.intValue();
 		}
 	}
 
 	public static int toInt(final Object o) {
 		if (o instanceof BigDecimal) {
 			return NumberUtil.toInt((BigDecimal) o);
 		} else if (o instanceof Number) {
 			return new BigDecimal(String.valueOf(o)).intValue();
 		} else {
 			return NumberUtils.toInt(String.valueOf(o));
 		}
 	}
 
 	/**
 	 * Converte um objeto para um Byte ou retorna 0 se não for possível
 	 */
 	public static Byte toByte(final Object o) {
 		try {
 			return Byte.parseByte(String.valueOf(o));
 		} catch (final Exception e) {
 			return 0;
 		}
 
 	}
 
 	/**
 	 * Converte um objeto para um Short ou retorna 0 se não for possível
 	 */
 	public static Short toShort(final Object o) {
 		try {
 			return Short.parseShort(String.valueOf(o));
 		} catch (final Exception e) {
 			return 0;
 		}
 
 	}
 
 	/**
 	 * Tenta converter uma String para Inteiro caso não seja possível retorna o valor padrão (defaultValue).
 	 */
 	public static Integer toInt(final String parameter, final Integer defaultValue) {
 		try {
 			return Integer.parseInt(parameter);
 		} catch (final Exception e) {
 			return defaultValue;
 		}
 	}
 
 	public static long toLong(final BigDecimal number) {
 		if (number == null) {
 			return 0;
 		} else {
 			return number.longValue();
 		}
 	}
 
 	public static long toLong(final Object o) {
 		if (o instanceof BigDecimal) {
 			return NumberUtil.toLong((BigDecimal) o);
 		} else if (o instanceof Number) {
 			return new BigDecimal(String.valueOf(o)).longValue();
 		} else {
 			return NumberUtils.toLong(String.valueOf(o));
 		}
 	}
 
 	/**
 	 * Tenta converter uma String para long caso n�o seja poss�vel retorna o valor padr�o (defaultValue).
 	 */
 	public static Long toLong(final String parameter, final Long defaultValue) {
 		try {
 			return Long.parseLong(parameter);
 		} catch (final Exception e) {
 			return defaultValue;
 		}
 
 	}
 
 	/**
 	 * Tranforma um array de int em um array de N�meros inteiros.
 	 * @return
 	 */
 	public static Integer[] toIntegerArray(final int[] intArray) {
 		final Integer[] numeros = new Integer[intArray.length];
 		for (int i = 0; i < intArray.length; i++) {
 			numeros[i] = intArray[i];
 		}
 		return numeros;
 	}
 
 	/**
 	 * Tranforma um array de Object em um array de N�meros inteiros.
 	 */
 	public static Integer[] toIntegerArray(final Object[] strArray) {
 		if (strArray == null) {
 			return new Integer[0];
 		}
 		final Integer[] numeros = new Integer[strArray.length];
 		for (int i = 0; i < strArray.length; i++) {
 			numeros[i] = NumberUtil.toInt(strArray[i]);
 		}
 		return numeros;
 	}
 
 	/**
 	 * Tranforma um array de Object em um array de N�meros inteiros.
 	 */
 	public static Set<Integer> toIntegerSet(final Object[] strArray) {
 		if (strArray == null) {
 			return new HashSet<Integer>(0);
 		} else {
 			final Set<Integer> numeros = new HashSet<Integer>();
 			for (final Object element : strArray) {
 				numeros.add(NumberUtil.toInt(element));
 			}
 			return numeros;
 		}
 	}
 
 	/**
 	 * Tranforma um array de Object em um array de N�meros inteiros.
 	 */
 	public static int[] toIntArray(final Object[] strArray) {
 		if (strArray == null) {
 			return new int[0];
 		}
 		final int[] numeros = new int[strArray.length];
 		for (int i = 0; i < strArray.length; i++) {
 			numeros[i] = NumberUtil.toInt(strArray[i]);
 		}
 		return numeros;
 	}
 
 	public static int[] toIntArray(final Collection<? extends Object> collection) {
 		if (collection == null) {
 			return new int[0];
 		}
 		final int[] numeros = new int[collection.size()];
 
 		int i = 0;
 		for (final Object o : collection) {
 			numeros[i++] = NumberUtils.toInt(o.toString().trim());
 		}
 		return numeros;
 	}
 
 	public static Integer[] toIntegerArray(final Collection<? extends Object> collection) {
 
 		if (collection == null) {
 			return new Integer[0];
 		}
 		final Integer[] numeros = new Integer[collection.size()];
 
 		int i = 0;
 		for (final Object o : collection) {
 			numeros[i++] = NumberUtils.toInt(o.toString().trim());
 		}
 		return numeros;
 	}
 
 	/**
 	 * Tranforma um array de Object em um array de String.
 	 */
 	public static String[] toStringArray(final Object[] strArray) {
 		final String[] valores = new String[strArray.length];
 		for (int i = 0; i < strArray.length; i++) {
 			valores[i] = strArray[i].toString().trim();
 		}
 		return valores;
 	}
 
 	/**
 	 * Tira zeros a esquerda de uma de uma n�mero armazenado em uma String.
 	 */
 	public static String tiraZeroAEsquerda(final String numero) {
 		if (numero.charAt(0) == '0') {
 			return NumberUtil.tiraZeroAEsquerda(numero.substring(1, numero.length()));
 		} else {
 			return numero;
 		}
 	}
 
 	public static double toDouble(final Integer number) {
 		return NumberUtils.toDouble(String.valueOf(number));
 	}
 
 	public static BigDecimal toBigDecimal(final Object number) {
 		if (number == null) {
 			return BigDecimal.ZERO;
 		} else if (number instanceof BigDecimal) {
 			return (BigDecimal) number;
 		} else {
 			try {
 				return new BigDecimal(number.toString());
 			} catch (final Exception e) {
 				return BigDecimal.ZERO;
 			}
 		}
 	}
 
 	/**
 	 * Retorna uma lista com as partes inteira e decimal de um n�mero
 	 */
 	public static double[] divideInteiroDecimal(final Double number) {
 		final BigDecimal numberToConvert = new BigDecimal(number.toString());
 		final BigDecimal[] convertedNumbers = numberToConvert.divideAndRemainder(new BigDecimal("1"));
 
 		return NumberUtil.toDoubleArray(convertedNumbers);
 	}
 
 	/**
 	 * Converte um BigDecimal[] em double[]
 	 */
 	public static double[] toDoubleArray(final BigDecimal[] numbers) {
 		final double[] retorno = new double[numbers.length];
 		for (int i = 0; i < numbers.length; i++) {
 			retorno[i] = numbers[i].doubleValue();
 		}
 		return retorno;
 	}
 
 	/**
 	 * Divide um array de n�meros inteiros em v�rios de acordo com o tamanho m�ximo
 	 */
 	public static List<Long[]> divideArray(final Long[] numbers, final int maxSize) {
 		final List<Long[]> lista = new ArrayList<Long[]>();
 		final int totalSize = numbers.length;
 
 		int index = 0;
 		Long[] valores = new Long[maxSize];
 		for (int i = 0; i < totalSize; i++) {
 
 			if (index >= maxSize) {
 				index = 0;
 				lista.add(valores);
 				valores = new Long[maxSize];
 			}
 			valores[index++] = numbers[i];
 
 		}
 		lista.add(valores);
 		return lista;
 	}
 
 	/**
 	 * Converte uma String para int no formato do Locale atual. Se a string passada n�o for um n�mero v�lido, retorna 0.
 	 */
 	public static int toIntLocale(final String number) {
 		try {
 			return NumberFormat.getInstance().parse(number).intValue();
 		} catch (final ParseException e) {
 			return 0;
 		}
 	}
 
 	/**
 	 * Converte uma String para long no formato do Locale atual. Se a string passada n�o for um n�mero v�lido, retorna 0.
 	 */
 	public static long toLongLocale(final String number) {
 		try {
 			return NumberFormat.getInstance().parse(number).longValue();
 		} catch (final ParseException e) {
 			return 0l;
 		}
 	}
 
 	/**
 	 * Converte uma String para double no formato do Locale atual. Se a string passada n�o for um n�mero v�lido, retorna 0.
 	 */
 	public static double toDoubleLocale(final String number) {
 		try {
 			return NumberFormat.getInstance().parse(number).doubleValue();
 		} catch (final Exception e) {
 			return 0d;
 		}
 	}
 
 	/**
 	 * Converte uma String para double no formato do Locale atual. Se a string passada n�o for um n�mero v�lido, retorna o defaultValue.
 	 */
 	public static Double toDoubleLocale(final String number, final Double defaultValue) {
 		try {
 			return NumberFormat.getInstance().parse(number).doubleValue();
 		} catch (final ParseException e) {
 			return defaultValue;
 		}
 	}
 
 	/**
 	 * Converte uma String para double no formato do Locale atual. Se a string passada n�o for um n�mero v�lido, lan�a uma exception.
 	 */
 	public static double parseDoubleLocale(final String number) throws ParseException {
 		return NumberFormat.getInstance().parse(number).doubleValue();
 	}
 
 	/**
 	 * Converte um bigDecimal para double.
 	 */
 	public static Double toDouble(final BigDecimal number) {
 		return number.doubleValue();
 	}
 
 	/**
 	 * Trunca um n�mero com n casas decimais.
 	 */
 	public static double truncate(final double number, final int casasDecimais) {
 		final double numberPow = Math.pow(10, casasDecimais);
 		return Math.floor(number * numberPow) / numberPow;
 	}
 
 	public static Long[] toLongArray(final Object[] strArray) {
 		if (strArray == null) {
 			return new Long[0];
 		}
 		final Long[] numeros = new Long[strArray.length];
 		for (int i = 0; i < strArray.length; i++) {
 			numeros[i] = NumberUtil.toLong(strArray[i]);
 		}
 		return numeros;
 	}
 
 	/**
 	 * Tranforma um array de Object em um array de Long.
 	 */
 	public static Set<Long> toLongSet(final Object[] strArray) {
 		if (strArray == null) {
 			return new HashSet<Long>(0);
 		} else {
 			final Set<Long> numeros = new HashSet<Long>();
 			for (final Object element : strArray) {
 				numeros.add(NumberUtil.toLong(element));
 			}
 			return numeros;
 		}
 	}
 
 	/**
 	 * Verfica se n�o � um n�mero(isNaN = is Not a Number)
 	 */
 	public static boolean isNaN(final String number) {
 		try {
 			Double.parseDouble(number);
 		} catch (final NumberFormatException nf) {
 			return true;
 		}
 		return false;
 	}
 
 	public static double arredondar(final double numero, final int casas) {
 		return arredondar(numero, casas, RoundingMode.HALF_UP);
 	}
 
 	public static double arredondar(final double numero, final int casas, final RoundingMode roundingMode) {
 		return BigDecimal.valueOf(numero).setScale(casas, roundingMode).doubleValue();
 	}
 
 	public static BigDecimal toZeroIfNull(final BigDecimal number) {
 		if (number == null) {
 			return BigDecimal.ZERO;
 		} else {
 			return number;
 		}
 	}
 
 	public static boolean ehMaiorQueZero(final BigDecimal trocaTotal) {
 		return trocaTotal.compareTo(BigDecimal.ZERO) > 0;
 	}
 
 	public static boolean isZeroOrNull(final Number number) {
 		if (number == null || number.doubleValue() == 0d) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public static boolean isNullOrNegative(final Number number) {
 		if (number == null || number.doubleValue() < 0d) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public static boolean isNotNullAndNegative(final Number number) {
 		if (number != null && number.doubleValue() < 0d) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public static boolean isNotNullAndEqualsZero(final Number number) {
 		if (number != null && number.doubleValue() == 0d) {
 			return true;
 		}
 		return false;
 	}
 
 	private static final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance();
 
 	public static String formatCurrency(final Number number) {
 		return currencyFormatter.format(number);
 	}
 
 	private static final NumberFormat percentFormatter = NumberFormat.getPercentInstance();
 	static {
 		percentFormatter.setMinimumFractionDigits(0);
 		percentFormatter.setMaximumFractionDigits(1);
 	}
 
 	public static String formatPercent(final Number number) {
 		return percentFormatter.format(number);
 	}
 
 	/**
 	 * create and static formatter for 2,3 and 4 that are more frequently used for performance issues
 	 */
 	private static final NumberFormat decimalFormatterWith2 = NumberFormat.getInstance();
 	static {
 		decimalFormatterWith2.setMinimumFractionDigits(2);
 		decimalFormatterWith2.setMaximumFractionDigits(2);
 		decimalFormatterWith2.setRoundingMode(RoundingMode.DOWN);
 	}
 
 	/**
 	 * create and static formatter for 2,3 and 4 that are more frequently used for performance issues
 	 */
 	private static final NumberFormat decimalFormatterWith3 = NumberFormat.getInstance();
 	static {
 		decimalFormatterWith3.setMinimumFractionDigits(3);
 		decimalFormatterWith3.setMaximumFractionDigits(3);
 		decimalFormatterWith3.setRoundingMode(RoundingMode.DOWN);
 	}
 
 	/**
 	 * create and static formatter for 2,3 and 4 that are more frequently used for performance issues
 	 */
 	private static final NumberFormat decimalFormatterWith4 = NumberFormat.getInstance();
 	static {
 		decimalFormatterWith4.setMinimumFractionDigits(4);
 		decimalFormatterWith4.setMaximumFractionDigits(4);
 		decimalFormatterWith4.setRoundingMode(RoundingMode.DOWN);
 	}
 
 	public static String formatDecimal(final Number number) {
 		return formatDecimal(number, 2);
 	}
 
 	public static String formatDecimal(final Number number, final int minDecimals, final int maxDecimals) {
 		if (minDecimals == maxDecimals) {
 			switch (maxDecimals) {
 				case 2:
 					return decimalFormatterWith2.format(number);
 				case 3:
 					return decimalFormatterWith3.format(number);
 				case 4:
 					return decimalFormatterWith4.format(number);
 				default:
 					final NumberFormat decimal = NumberFormat.getInstance();
 					decimal.setMinimumFractionDigits(maxDecimals);
 					decimal.setMaximumFractionDigits(maxDecimals);
 					decimal.setRoundingMode(RoundingMode.DOWN);
 					return decimal.format(number);
 			}
 		} else {
 			final NumberFormat decimal = NumberFormat.getInstance();
 			decimal.setMinimumFractionDigits(minDecimals);
 			decimal.setMaximumFractionDigits(maxDecimals);
 			decimal.setRoundingMode(RoundingMode.DOWN);
 			return decimal.format(number);
 		}
 	}
 
 	public static String formatDecimal(final Number number, final int decimals) {
 		return formatDecimal(number, decimals, decimals);
 	}
 
 	/**
 	 * divide the number per 100 if the number is greater than zero otherwise return zero.
 	 */
 	public static double toPercent(final Object percent) {
 		return toPercent(percent, 4);
 	}
 
 	public static double toPercent(final Object percent, final int precision) {
 		final BigDecimal percentDecimal = NumberUtil.toBigDecimal(percent);
 		if (percentDecimal != null) {
 			if (percentDecimal.compareTo(BigDecimal.ZERO) > 0) {
				return percentDecimal.setScale(precision).divide(BigDecimal.valueOf(100d), RoundingMode.HALF_UP).setScale(precision).doubleValue();
 			}
 		}
 		return 0d;
 	}
 
 }
