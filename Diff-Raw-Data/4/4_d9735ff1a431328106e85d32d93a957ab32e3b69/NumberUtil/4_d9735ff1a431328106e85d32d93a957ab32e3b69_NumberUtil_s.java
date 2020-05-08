 package br.com.bluesoft.commons.lang;
 
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.text.DecimalFormat;
 import java.text.DecimalFormatSymbols;
 import java.text.NumberFormat;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.lang.math.NumberUtils;
 
 /**
  * @author AndreFaria
  */
 public class NumberUtil {
 
 	/**
 	 * Retorna Zero se o Valor de Entrada for null
 	 * @param number
 	 * @return
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
 	 * @param number
 	 * @return
 	 */
 	public static long toZeroIfNull(final Long number) {
 		if (number == null) {
 			return 0;
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
 			return 0;
 		} else {
 			return number;
 		}
 	}
 
 	/**
 	 * Retorna Zero se o Valor de Entrada for null
 	 * @param number
 	 * @return
 	 */
 	public static float toZeroIfNull(final Float number) {
 		if (number == null) {
 			return 0;
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
 	 * Tenta converter uma String para Double caso no seja possvel retorna o valor padro (defaultValue).
 	 * @param parameter
 	 * @param defaultValue
 	 * @return Integer
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
 	 * @param number
 	 * @return
 	 */
 	public static int toInt(final double number) {
 		final NumberFormat numberFormat = DecimalFormat.getInstance();
 		numberFormat.setGroupingUsed(false);
 		return Integer.valueOf(numberFormat.format(number));
 	}
 
 	/**
 	 * Converte um bigDecimal para inteiro.
 	 * @param number
 	 * @return
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
 		} else {
 			return NumberUtils.toInt(String.valueOf(o));
 		}
 	}
 
 	/**
 	 * Tenta converter uma String para Inteiro caso no seja possvel retorna o valor padro (defaultValue).
 	 * @param parameter
 	 * @param defaultValue
 	 * @return Integer
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
 		} else {
 			return NumberUtils.toLong(String.valueOf(o));
 		}
 	}
 
 	/**
 	 * Tenta converter uma String para long caso no seja possvel retorna o valor padro (defaultValue).
 	 * @param parameter
 	 * @param defaultValue
 	 * @return
 	 */
 	public static Long toLong(final String parameter, final Long defaultValue) {
 		try {
 			return Long.parseLong(parameter);
 		} catch (final Exception e) {
 			return defaultValue;
 		}
 
 	}
 
 	/**
 	 * Tranforma um array de int em um array de Nmeros inteiros.
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
 	 * Tranforma um array de Object em um array de Nmeros inteiros.
 	 * @return
 	 */
 	public static Integer[] toIntegerArray(final Object[] strArray) {
 		if (strArray == null) {
 			return new Integer[0];
 		}
 		final Integer[] numeros = new Integer[strArray.length];
 		for (int i = 0; i < strArray.length; i++) {
 			numeros[i] = NumberUtils.toInt(strArray[i].toString().trim());
 		}
 		return numeros;
 	}
 
 	/**
 	 * Tranforma um array de Object em um array de Nmeros inteiros.
 	 * @return
 	 */
 	public static Set<Integer> toIntegerSet(final Object[] strArray) {
 		if (strArray == null) {
 			return new HashSet<Integer>(0);
 		} else {
 			final Set<Integer> numeros = new HashSet<Integer>();
 			for (final Object element : strArray) {
 				numeros.add(NumberUtils.toInt(element.toString().trim()));
 			}
 			return numeros;
 		}
 	}
 
 	/**
 	 * Tranforma um array de Object em um array de Nmeros inteiros.
 	 * @return
 	 */
 	public static int[] toIntArray(final Object[] strArray) {
 		if (strArray == null) {
 			return new int[0];
 		}
 		final int[] numeros = new int[strArray.length];
 		for (int i = 0; i < strArray.length; i++) {
 			numeros[i] = NumberUtils.toInt(strArray[i].toString().trim());
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
 
 	/**
 	 * Tranforma um array de Object em um array de String.
 	 * @return
 	 */
 	public static String[] toStringArray(final Object[] strArray) {
 		final String[] valores = new String[strArray.length];
 		for (int i = 0; i < strArray.length; i++) {
 			valores[i] = strArray[i].toString().trim();
 		}
 		return valores;
 	}
 
 	/**
 	 * Tira zeros a esquerda de uma de uma nmero armazenado em uma String.
 	 * @param numero
 	 * @return String
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
 		} else {
 			try {
 				return new BigDecimal(number.toString());
 			} catch (final Exception e) {
 				return BigDecimal.ZERO;
 			}
 		}
 	}
 
 	/**
 	 * Retorna uma lista com as partes inteira e decimal de um nmero
 	 * @param number
 	 * @return
 	 */
 	public static double[] divideInteiroDecimal(final Double number) {
 		final BigDecimal numberToConvert = new BigDecimal(number);
 		final BigDecimal[] convertedNumbers = numberToConvert.divideAndRemainder(new BigDecimal("1"));
 
 		return NumberUtil.toDoubleArray(convertedNumbers);
 	}
 
 	/**
 	 * Converte um BigDecimal[] em double[]
 	 * @param numbers
 	 * @return
 	 */
 	public static double[] toDoubleArray(final BigDecimal[] numbers) {
 		final double[] retorno = new double[numbers.length];
 		for (int i = 0; i < numbers.length; i++) {
 			retorno[i] = numbers[i].doubleValue();
 		}
 		return retorno;
 	}
 
 	/**
 	 * Divide um array de nmeros inteiros em vrios de acordo com o tamanho mximo
 	 * @param numbers
 	 * @param size
 	 * @return Integer[][]
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
 	 * Converte uma String para int no formato do Locale atual. Se a string passada no for um nmero vlido, retorna 0.
 	 * @param number
 	 * @return
 	 */
 	public static int toIntLocale(final String number) {
 		try {
 			return NumberFormat.getInstance().parse(number).intValue();
 		} catch (final ParseException e) {
 			return 0;
 		}
 	}
 
 	/**
 	 * Converte uma String para long no formato do Locale atual. Se a string passada no for um nmero vlido, retorna 0.
 	 * @param number
 	 * @return
 	 */
 	public static long toLongLocale(final String number) {
 		try {
 			return NumberFormat.getInstance().parse(number).longValue();
 		} catch (final ParseException e) {
 			return 0l;
 		}
 	}
 
 	/**
 	 * Converte uma String para double no formato do Locale atual. Se a string passada no for um nmero vlido, retorna 0.
 	 * @param number
 	 * @return double
 	 */
 	public static double toDoubleLocale(final String number) {
 		try {
 			return NumberFormat.getInstance().parse(number).doubleValue();
 		} catch (final ParseException e) {
 			return 0d;
 		}
 	}
 
 	/**
 	 * Converte uma String para double no formato do Locale atual. Se a string passada no for um nmero vlido, retorna o defaultValue.
 	 * @param number
 	 * @return double
 	 */
 	public static Double toDoubleLocale(final String number, final Double defaultValue) {
 		try {
 			return NumberFormat.getInstance().parse(number).doubleValue();
 		} catch (final ParseException e) {
 			return defaultValue;
 		}
 	}
 
 	/**
 	 * Converte uma String para double no formato do Locale atual. Se a string passada no for um nmero vlido, lana uma exception.
 	 * @param number
 	 * @return double
 	 */
 	public static double parseDoubleLocale(final String number) {
 		try {
 			return NumberFormat.getInstance().parse(number).doubleValue();
 		} catch (final ParseException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	/**
 	 * Converte um bigDecimal para double.
 	 * @param number
 	 * @return
 	 */
 	public static Double toDouble(final BigDecimal number) {
 		return number.doubleValue();
 	}
 
 	/**
 	 * Trunca um nmero com n casas decimais.
 	 * @param number
 	 * @param maximunFractionDigits
 	 * @return
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
 			numeros[i] = NumberUtils.toLong(strArray[i].toString().trim());
 		}
 		return numeros;
 	}
 
 	/**
 	 * Tranforma um array de Object em um array de Long.
 	 * @return
 	 */
 	public static Set<Long> toLongSet(final Object[] strArray) {
 		if (strArray == null) {
 			return new HashSet<Long>(0);
 		} else {
 			final Set<Long> numeros = new HashSet<Long>();
 			for (final Object element : strArray) {
 				numeros.add(NumberUtils.toLong(element.toString().trim()));
 			}
 			return numeros;
 		}
 	}
 
 	/**
 	 * Verfica se no  um nmero(isNaN= is Not a Number)
 	 * @param number
 	 * @return
 	 */
 	public static boolean isNaN(final String number) {
 		try {
 			Double.parseDouble(number);
 		} catch (final NumberFormatException nf) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Arredonda um nmero decimal sempre para cima
 	 * @param number
 	 * @return
 	 */
 	public static double ceil(final double number) {
 		return Math.ceil(number);
 	}
 
 	public static double arredondar(final double numero, final int casas) {
 		final DecimalFormat dc = (DecimalFormat) NumberFormat.getInstance();
 		final DecimalFormatSymbols ds = dc.getDecimalFormatSymbols();
 		ds.setDecimalSeparator('.');
 		ds.setGroupingSeparator('\0');
 
 		dc.setDecimalFormatSymbols(ds);
 		dc.setMinimumFractionDigits(casas);
 		dc.setMaximumFractionDigits(casas);
 		final String n = dc.format(numero);
 		return Double.parseDouble(n);
 	}
 
 	public static BigDecimal toZeroIfNull(final BigDecimal number) {
 		if (number == null) {
 			return BigDecimal.ZERO;
 		} else {
 			return number;
 		}
 	}
 
 }
