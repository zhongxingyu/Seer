 /*
  * Copyright (c) 2009 Timothy Bourke
  * All rights reserved.
  *
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the "BSD License" which is distributed with the
  * software in the file ../../LICENSE.
  *
  * This program is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the BSD
  * License for more details.
  */
 
 package mnemogogo.mobile.htmlcsv;
 
 import java.lang.*;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.IOException;
 import java.util.Date;
 import javax.microedition.io.Connector;
import java.io.OutputStream;
import javax.microedition.io.file.FileConnection; /*JSR-75*/
 
 public class HtmlCsv
     implements CardList
 {
     private Card cards[];
     private RevQueue q;
     private int days_left;
     private Config config;
     private Progress progress;
 
     public long days_since_start;
     public OutputStreamWriter logfile;
     public String categories[];
 
     public static String ascii = "ASCII";
     public static String utf8 = "UTF-8";
 
     public HtmlCsv(String path, Progress prog)
 	throws IOException
     {
 	long start_time;
 	long adjusted_now;
 	int num_cards;
 	int path_len = path.length();
 	StringBuffer p = new StringBuffer(path_len + 20);
 	p.append(path);
 
 	progress = prog;
 
 	readConfig(p);
 
 	p.delete(path_len, p.length());
 	readDaysLeft(p);
 
 	p.delete(path_len, p.length());
 	calculateDaysSinceStart(p);
 
 	p.delete(path_len, p.length());
 	readCards(p);
 
 	p.delete(path_len, p.length());
 	readCategories(p);
 
 	if (config.logging()) {
	    p.delete(path_len, p.length());
	    p.append("prelog");

 	    System.out.println("here!"); // XXX
	    // TODO: check that this really appends?
	    FileConnection file =
		(FileConnection)Connector.open(p.toString(), Connector.WRITE);
	    OutputStream outs = file.openOutputStream(file.fileSize());
	    logfile = new OutputStreamWriter(outs, ascii);
 	    System.out.println("after!"); // XXX
 	}
 
 	q = new RevQueue(cards.length, days_since_start, config);
 	q.buildRevisionQueue(cards, progress);
     }
 
     public int daysLeft() {
 	return days_left;
     }
 
     public String getCategory(int n) {
 	if (0 <= n && n < categories.length) {
 	    return categories[n];
 	} else {
 	    return null;
 	}
     }
 
     public Card getCard(int serial) {
 	if (0 < serial && serial < cards.length) {
 	    return cards[serial];
 	} else {
 	    return null;
 	}
     }
 
     private void readConfig(StringBuffer path)
 	throws IOException
     {
 	InputStreamReader in = new InputStreamReader(
 		Connector.openInputStream(path.append("config").toString()),
 		ascii);
 	config = new Config(in);
 	in.close();
     }
 
     private void calculateDaysSinceStart(StringBuffer path)
 	throws IOException
     {
 	InputStreamReader in = new InputStreamReader(
 	    Connector.openInputStream(path.append("start_time").toString()),
 	    ascii);
 	long start_time = StatIO.readLong(in);
 	in.close();
 
 	Date now = new Date();
 	long adjusted_now = (now.getTime() / 1000) -
 				(config.dayStartsAt() * 3600);
 	days_since_start = (adjusted_now - start_time) / 86400;
     }
 
     private void readDaysLeft(StringBuffer path)
 	throws IOException
     {
 
 	InputStreamReader in = new InputStreamReader(
 	    Connector.openInputStream(path.append("last_day").toString()),
 	    ascii);
 	long last_day = StatIO.readLong(in);
 	in.close();
 
 	Date now = new Date();
 	days_left = Math.max(0, (int)(last_day - (now.getTime() / 86400)));
     }
 
     private void readCards(StringBuffer path)
 	throws IOException
     {
 	InputStreamReader in = new InputStreamReader(
 	    Connector.openInputStream(path.append("stats.csv").toString()),
 	    ascii);
 
 	int ncards = StatIO.readInt(in);
 	progress.startOperation(ncards * 3);
 
 	cards = new Card[ncards];
 	q = new RevQueue(ncards, days_since_start, config);
 
 	for (int i=0; i < ncards; ++i) {
 	    cards[i] = new Card(this, in, i);
 	    if (i % 10 == 0) {
 		progress.updateOperation(10);
 	    }
 	}
 
 	in.close();
     }
 
     public void writeCards(StringBuffer path, Progress progress)
 	throws IOException
     {
 	OutputStreamWriter out = new OutputStreamWriter(
 	    Connector.openOutputStream(path.append("stats.csv").toString()),
 	    ascii);
 
 	StatIO.writeInt(out, cards.length, "\n");
 
 	progress.startOperation(cards.length);
 	for (int i=0; i < cards.length; ++i) {
 	    cards[i].writeCard(out);
 
 	    if (i % 10 == 0 && progress != null) {
 		progress.updateOperation(10);
 	    }
 	}
 
 	out.close();
     }
 
     private void readCategories(StringBuffer path)
 	throws IOException
     {
 	InputStreamReader in = new InputStreamReader(
 	    Connector.openInputStream(path.append("categories").toString()),
 	    utf8);
 
 	int n = StatIO.readInt(in);
 	int bytesize = StatIO.readInt(in);
 
 	System.out.print("num categories: "); // XXX
 	System.out.println(n);
 
 	categories = new String[n];
 	for (int i=0; i < n; ++i) {
 	    categories[i] = StatIO.readLine(in);
 	    System.out.print("-> "); // XXX
 	    System.out.println(categories[i]); // XXX
 	}
 
 	in.close();
     }
 
     public int numScheduled() {
 	return q.numScheduled();
     }
 
     public Card getCard() {
 	return q.getCard();
     }
 
     public String toString() {
 	return q.toString();
     }
 
     public void dumpCards() {
 	System.out.println("----Cards:");
 	for (int i=0; i < cards.length; ++i) {
 	    System.out.print("  ");
 	    System.out.println(cards[i].toString());
 	}
     }

    public void close() {
	if (logfile != null) {
	    try {
		logfile.close();
	    } catch (IOException e) {}
	    logfile = null;
	}
    }
 }
 
