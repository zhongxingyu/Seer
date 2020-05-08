 /*
  *
  *  JMoney - A Personal Finance Manager
  *  Copyright (c) 2004 Nigel Westbury <westbury@users.sourceforge.net>
  *
  *
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation; either version 2 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program; if not, write to the Free Software
  *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
  *
  */
 
 package net.sf.jmoney.reconciliation;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Currency;
 import java.util.Date;
 import java.util.Vector;
 
 import net.sf.jmoney.model2.CurrencyAccount;
 import net.sf.jmoney.reconciliation.parser.SimpleDOMParser;
 import net.sf.jmoney.reconciliation.parser.SimpleElement;
 import net.sf.jmoney.reconciliation.reconcilePage.ImportStatementDialog;
 
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Shell;
 
 /**
  * Provides an implementation of the net.sf.jmoney.reconciliation.bankstatements
  * extension point. This extension supports the import of Financial Exchange
  * files (OFX and QFX files).
  * 
  * @author Nigel Westbury
  */
 public class OfxImport implements IBankStatementSource {
 	public Collection<EntryData> importEntries(Shell shell, CurrencyAccount account, Date defaultStartDate, Date defaultEndDate) {
 
 		// Prompt the user for the file.
 		FileDialog dialog = new FileDialog(shell);
 		dialog.setFilterExtensions(new String[] { "*.OFX", "*.QFX" });
 		dialog.setFilterNames(new String[] {
 				"Open Financial Exchange Files (*.OFX)",
 				"Quicken Financial Exchange Files (*.QFX)" });
 		String fileName = dialog.open();
 
 		if (fileName == null) {
 			return null;
 		}
 
 		ImportStatementDialog dialog2 = new ImportStatementDialog(shell, defaultStartDate, defaultEndDate, null);
 		if (dialog2.open() != Dialog.OK) {
 			return null;
 		}
 		Date startDate = dialog2.getStartDate();
 		Date endDate = dialog2.getEndDate();
 		
 		File sessionFile = new File(fileName);
 
 		BufferedReader buffer = null;
 		try {
 			buffer = new BufferedReader(new FileReader(sessionFile));
 		} catch (FileNotFoundException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 			return null;
 		}
 
 		return getEntries(buffer, startDate, endDate);
 	}
 
 	Collection<EntryData> getEntries(BufferedReader buffer, Date startDate, Date endDate) {
 		SimpleDOMParser parser = new SimpleDOMParser();
 		SimpleElement rootElement = null;
 		try {
 			rootElement = parser.parse(buffer);
 			String xml = rootElement.toXMLString(0);
 			System.out.println(xml);
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 
 		Collection<EntryData> entries = new Vector<EntryData>();
 		if (rootElement == null)
 			return entries;
 
 		entries = parseOfx(rootElement, startDate, endDate);
 
 		return entries;
 	}
 
 	private Collection<EntryData> parseOfx(SimpleElement rootElement, Date startDate, Date endDate) {
 		Vector<EntryData> v = new Vector<EntryData>();
 		SimpleElement foundElement = rootElement.findElement("CURDEF");
 		Currency currency = null;
 		if (foundElement != null) {
 			// found a default currency to use
 			String currencyCode = foundElement.getTrimmedText();
 			currency = Currency.getInstance(currencyCode);
 		}
 
 		foundElement = rootElement.findElement("STMTTRN");
 		while (foundElement != null) {
 			EntryData data = parseSTMTTRN(foundElement, currency);
 			if ((startDate == null
 					|| data.getClearedDate().compareTo(startDate) >= 0)
 					&& (endDate == null
 							|| data.getClearedDate().compareTo(endDate) <= 0)) {
 				v.add(data);
 			}
 					
 			
 			foundElement = foundElement.getNextSibling();
 		}
 		return v;
 	}
 
 	private EntryData parseSTMTTRN(SimpleElement foundElement, Currency currency) {
 		EntryData entryData = new EntryData();
 
 		SimpleElement tmpElement = foundElement.findElement("DTPOSTED");
 		String data = tmpElement.getTrimmedText();
 
 		// For some extraordinary reason, the date pattern does not match.
 		/*
 		 * System.out.println("data=" + childMatch.group(2) + "Y"); Matcher
 		 * dateMatch = datePattern.matcher(data); System.out.println("data=" +
 		 * childMatch.group(2) + "Z"); if (!dateMatch.matches()) { throw new
 		 * RuntimeException("bad date"); }
 		 * 
 		 * int year = Integer.parseInt(childMatch.group(1)); int month =
 		 * Integer.parseInt(childMatch.group(2)); int day =
 		 * Integer.parseInt(childMatch.group(3));
 		 */
 		// So let's just extract another way
 		int year = Integer.parseInt(data.substring(0, 4));
 		int month = Integer.parseInt(data.substring(4, 6));
 		int day = Integer.parseInt(data.substring(6, 8));
 
 		// Date object is always based on local time, which is ok because the
 		// Date as milliseconds is never persisted, and the timezone won't change
 		// within a session (will it?)
 		Calendar cal = Calendar.getInstance();
 		cal.set(year, month-1, day, 0, 0, 0);
 		cal.set(Calendar.MILLISECOND, 0);
 		entryData.setClearedDate(cal.getTime());
 
 		tmpElement = foundElement.findElement("TRNAMT");
 		data = tmpElement.getTrimmedText();
 
 		// long amount = CurrencyParser.parseAmount(data, currency);
 		// NOTE [roel] : in Belgium we use ',' as decimal separator
 		// I have tried using DecimalFormat but I am unable to 'guess' the
 		// Locale based on info available in the ofx-file. -> solution : this
 		// dirty hack
 
 		// We got rounding errors using double, so try this...
 		// I am sure it can be tidied up, but I just want something that
 		// works.
 		String [] parts = data.replace(',', '.').split("\\.");
 		long amount = Integer.parseInt(parts[0]) * 100;
 		if (parts.length > 1) {
 			
 			// QFX seems to put extra zeroes at the end.
 			// (or at least hsabank.com does)
 			// Remove them to get a length of two.
 			while (parts[1].length() > 2) {
 				if (parts[1].charAt(parts[1].length()-1) != '0') {
 					throw new RuntimeException("Extra digits and they are not zeroes");
 				}
 				parts[1] = parts[1].substring(0, parts[1].length() - 1);
 			}
			if (parts[1].length() == 1) {
				/*
				 * Some banks (e.g. Citibank) only put one digit after the
				 * decimal place if the second digit is zero (number of pennies
				 * is divisible by ten).
				 */
				parts[1] = parts[1] + "0";
			}
 			if (data.startsWith("-")) {
 				amount -= Integer.parseInt(parts[1]);
 			} else {
 				amount += Integer.parseInt(parts[1]);
 			}
 		}
 //		long amount = CurrencyParser.double2long(currency, Double
 //				.parseDouble(data.replace(',', '.')));
 
 		entryData.setAmount(amount);
 
 		tmpElement = foundElement.findElement("FITID");
 		if (tmpElement != null)
 			entryData.setUniqueId(tmpElement.getTrimmedText());
 
 		tmpElement = foundElement.findElement("NAME");
 		if (tmpElement != null)
 			entryData.setName(tmpElement.getTrimmedText());
 
 		tmpElement = foundElement.findElement("MEMO");
 		if (tmpElement != null)
 			entryData.setMemo(tmpElement.getTrimmedText());
 
 		// TRNTYPE is in QFX files only?
 		tmpElement = foundElement.findElement("TRNTYPE");
 		if (tmpElement != null)
 			entryData.setType(tmpElement.getTrimmedText());
 
 		tmpElement = foundElement.findElement("CHECKNUM");
 		if (tmpElement != null) {
 			String trimmedCheckNum = tmpElement.getTrimmedText();
 			// QFX (or at least hsabank.com) sets CHECKNUM to zero even though not a check.
 			// This is probably a bug at HSA Bank, but we ignore check numbers of zero.
 			if (!trimmedCheckNum.equals("0")) {
 				entryData.setCheck(trimmedCheckNum);
 			}
 		}
 
 		// NOTE [roel] : moved this workaround into
 		// EntryData#assignPropertyValues
 		// It seems that QFX format has a <NAME> and a <MEMO> line,
 		// whereas OFX has only a name.
 		// It is a mess because sometimes the payee name is in the
 		// <NAME> field and sometimes it is in the <MEMO> field.
 		// (At least with the data from Bank of America)
 		// Just combine the two.
 		// if (name == null && memo != null) {
 		// entryData.setMemo(memo);
 		// } else if (name != null && memo == null) {
 		// entryData.setMemo(name);
 		// } else if (name != null && memo != null) {
 		// entryData.setMemo(name + " " + memo);
 		// }
 
 		return entryData;
 	}
 }
