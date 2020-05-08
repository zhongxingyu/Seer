 package me.guillsowns.docgym.business;
 
 import java.awt.Color;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.Locale;
 
 import me.guillsowns.docgym.domain.ScheduleEntry;
 
 import org.apache.commons.lang3.time.DateUtils;
 
 import com.lowagie.text.Chunk;
 import com.lowagie.text.Document;
 import com.lowagie.text.DocumentException;
 import com.lowagie.text.Element;
 import com.lowagie.text.Font;
 import com.lowagie.text.PageSize;
 import com.lowagie.text.Paragraph;
 import com.lowagie.text.Rectangle;
 import com.lowagie.text.pdf.BaseFont;
 import com.lowagie.text.pdf.PdfContentByte;
 import com.lowagie.text.pdf.PdfPCell;
 import com.lowagie.text.pdf.PdfPTable;
 import com.lowagie.text.pdf.PdfWriter;
 import com.lowagie.text.pdf.draw.VerticalPositionMark;
 
 public class PDFCalendar
 {
 	public static final String	LANGUAGE	= "en";
 	protected Font				normal;
 	protected Font				small;
 	protected Font				bold;
 	private Rectangle			paperSize	= PageSize.LETTER;
 
 	public PDFCalendar() throws DocumentException, IOException
 	{
 		// fonts
		BaseFont bf_normal = BaseFont.createFont("c://windows/fonts/arial.ttf", BaseFont.WINANSI, BaseFont.EMBEDDED);
 		normal = new Font(bf_normal, 16);
 		small = new Font(bf_normal, 8);
		BaseFont bf_bold = BaseFont.createFont("c://windows/fonts/arialbd.ttf", BaseFont.WINANSI, BaseFont.EMBEDDED);
 		bold = new Font(bf_bold, 14);
 	}
 
 	public void createPdf(OutputStream os, Locale locale, List<ScheduleEntry> entries, int year, int month) throws IOException, DocumentException
 	{
 		Document document = new Document(paperSize.rotate());
 		PdfWriter writer = PdfWriter.getInstance(document, os);
 		document.open();
 
 		PdfPTable table;
 		Calendar calendar;
 		PdfContentByte canvas = writer.getDirectContent();
 
 		calendar = new GregorianCalendar(year, month, 1);
 		if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
 		{
 			calendar.add(Calendar.DAY_OF_WEEK, 1);
 		}
 		else if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)
 		{
 			calendar.add(Calendar.DAY_OF_WEEK, 2);
 		}
 		// we should have the first weekday here.
 
 		table = new PdfPTable(5);
 		table.setTotalWidth(paperSize.getHeight() - 10);
 
 		table.getDefaultCell().setBackgroundColor(Color.WHITE);
 		table.addCell(getMonthCell(calendar, locale));
 
 		int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
 		int day = 1;
 		int position = 2;
 		// add empty cells
 		while (position != calendar.get(Calendar.DAY_OF_WEEK))
 		{
 			position++;
 			table.addCell("");
 		}
 
 		// add cells for each day
 		while (day <= daysInMonth)
 		{
 			if (day <= daysInMonth)
 			{
 				table.addCell(getDayCell(calendar, locale, entries));
 			}
 			if (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.FRIDAY)
 			{
 				calendar.add(Calendar.DAY_OF_WEEK, 1);
 				++day;
 			}
 			else
 			{
 				calendar.add(Calendar.DAY_OF_WEEK, 3);
 				day += 3;
 			}
 		}
 		table.completeRow();
 
 		// write the table to an absolute position
 		table.writeSelectedRows(0, -1, 5, table.getTotalHeight() + 40, canvas);
 
 		// step 5
 		document.close();
 	}
 
 	public PdfPCell getMonthCell(Calendar calendar, Locale locale)
 	{
 		PdfPCell cell = new PdfPCell();
 		cell.setColspan(5);
 		cell.setBackgroundColor(Color.WHITE);
 		cell.setUseDescender(true);
 		Paragraph p = new Paragraph(String.format(locale, "%1$tB %1$tY", calendar), bold);
 		p.setAlignment(Element.ALIGN_CENTER);
 		cell.addElement(p);
 		return cell;
 	}
 
 	public PdfPCell getDayCell(Calendar calendar, Locale locale, List<ScheduleEntry> entries)
 	{
 		PdfPCell cell = new PdfPCell();
 		cell.setPadding(3);
 		cell.setMinimumHeight(100);
 
 		// set the background color, based on the type of day
 		if (isSunday(calendar))
 		{
 			cell.setBackgroundColor(Color.GRAY);
 		}
 		else if (isSpecialDay(calendar))
 		{
 			cell.setBackgroundColor(Color.LIGHT_GRAY);
 		}
 		else
 		{
 			cell.setBackgroundColor(Color.WHITE);
 		}
 		// set the content in the language of the locale
 		Chunk chunk = new Chunk(String.format(locale, "%1$ta", calendar), small);
 		chunk.setTextRise(8);
 		// a paragraph with the day
 		Paragraph p = new Paragraph(chunk);
 		// a separator
 		p.add(new Chunk(new VerticalPositionMark()));
 		// and the number of the day
 		p.add(new Chunk(String.format(locale, "%1$te", calendar), normal));
 		cell.addElement(p);
 
 		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
 
 		for (ScheduleEntry e : getTodaysEntries(calendar, entries))
 		{
 			String startString = sdf.format(e.getTimeStart());
 			String desc = e.getTitle();
 
 			Paragraph text = new Paragraph(startString + " - " + desc);
 			cell.addElement(text);
 		}
 		return cell;
 	}
 
 	private List<ScheduleEntry> getTodaysEntries(Calendar today, List<ScheduleEntry> list)
 	{
 		List<ScheduleEntry> todays = new ArrayList<ScheduleEntry>();
 
 		for (ScheduleEntry e : list)
 		{
 			if (DateUtils.isSameDay(today.getTime(), e.getTimeStart()))
 			{
 				todays.add(e);
 			}
 		}
 
 		Collections.sort(todays, new ScheduleComparator());
 		return todays;
 	}
 
 	public boolean isSunday(Calendar calendar)
 	{
 		if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
 		{
 			return true;
 		}
 		return false;
 	}
 
 	public boolean isSpecialDay(Calendar calendar)
 	{
 		if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)
 		{
 			return true;
 		}
 		return false;
 	}
 }
