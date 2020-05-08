 /**
  * nox-util — Utility programs for headless environments
  *
  * Copyright © 2012  Mattias Andrée (maandree@kth.se)
  *
  * This library is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this library.  If not, see <http://www.gnu.org/licenses/>.
  */
 package se.kth.maandree.noxutil;
 
 import java.util.*;
 import java.io.*;
 
 
 /**
  * The main class of the alarm program
  *
  * @author  Mattias Andrée, <a href="mailto:maandree@kth.se">maandree@kth.se</a>
  */
 public class NoxAlarm
 {
     /**
      * Non-constructor
      */
     private NoxAlarm()
     {
 	assert false : "This class [NoxAlarm] is not meant to be instansiated.";
     }
     
     
     
     /**
      * This is the main entry point of the program
      * 
      * @param  args  Startup arguments
      */
     public static void main(final String... args)
     {
 	final ArrayList<String> lineList = new ArrayList<String>();
 	
 	final String[] files;
 	if ((args == null) || (args.length == 0))
 	    files = new String[] { "~/.noxalarm".replace("~", getProperty("HOME")) };
 	else
 	    files = args;
 	
 	for (final String file : files)
 	{
 	    final File f = new File(file);
 	    if (f.exists() == false)
 	    {
 		System.err.println("nox-alarm: error: file not found: " + file);
 		continue;
 	    }
 	    InputStream fis = null;
 	    try
 	    {
 		fis = new BufferedInputStream(new FileInputStream(f));
 		final Scanner sc = new Scanner(fis);
 		int lineIndex = 0;
 		for (;;)
 		{
 		    lineIndex++;
 		    String line = sc.nextLine();
 		    if (line == null)
 			break;
 		    while (line.startsWith(" ") || line.startsWith("\t"))
 			line = line.substring(1);
 		    
 		    if (line.isEmpty())
 			continue;
 		    
 		    int colon = line.indexOf(':');
 		    int space = line.indexOf(' ');
 		    int tab   = line.indexOf('\t');
 		    
 		    if (space < 0)
 			space = tab;
 		    if ((space < 0) || (5 < space) || (colon < 0) || (space < colon))
 		    {
 			System.err.println("nox-alarm: error: malform line (" + file + ": " + lineIndex + ")");
 			continue;
 		    }
 		    
 		    while (colon < 2)
 		    {
 			colon++;
 			space++;
 			line = '0' + line;
 		    }
 		    
 		    if (space < 5)
 		    {
 			System.err.println("nox-alarm: error: malform line (" + file + ": " + lineIndex + ")");
 			continue;
 		    }
 		    
 		    lineList.add(line);
 		}
 	    }
 	    catch (final Throwable err)
 	    {
 		System.err.println("nox-alarm: error: " + err.getMessage());
 	    }
 	    finally
 	    {
 		if (fis != null)
 		    try
 		    {
 			fis.close();
 		    }
 		    catch (final Throwable err)
 		    {
 			System.err.println("nox-alarm: error: cannot close file stream: " + file);
 		    }
 	    }
 	}
 	
 	final String[] lines = new String[lineList.size()];
 	lineList.toArray(lines);
 	Arrays.sort(lines);
 	
 	final int[] hours = new int[lines.length];
 	final int[] minutes = new int[lines.length];
 	final String[] commands = new String[lines.length];
 	
 	String ttyin = null, ttyout = null, ttyerr = null;
 	try
 	{
 	    ttyin  = (new File("/dev/stdin" )).getCanonicalPath();
 	    ttyout = (new File("/dev/stdout")).getCanonicalPath();
 	    ttyerr = (new File("/dev/stderr")).getCanonicalPath();
 	}
 	catch (final Throwable err)
 	{
 	    System.err.println("nox-alarm: fatal: can't fetch stdin, stdout and stderr.");
 	    return;
 	}
 	
 	for (int i = 0, n = lines.length; i < n; i++)
         {
 	    hours[i]    = Integer.parseInt(lines[i].substring(0, 2));
 	    minutes[i]  = Integer.parseInt(lines[i].substring(3, 5));
 	    commands[i] = lines[i].substring(6);
 	    
 	    commands[i] +=  " < " + ttyin;
 	    commands[i] +=  " > " + ttyout;
 	    commands[i] += " 2> " + ttyerr;
 	}
 	
 	final Scanner sc = new Scanner(System.in);
 	
 	for (int i = 0, n = lines.length; i < n; i++)
 	    try
 	    {
 		final int hour = hours[i];
 		final int minute = minutes[i];
 		final String command = commands[i];
 		
 		final int nowH = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
 		final int nowM = Calendar.getInstance().get(Calendar.MINUTE);
 		
 		String alarm = "0" + hour;
 		alarm = alarm.substring(alarm.length() - 2);
 		alarm += ':';
 		if (minute < 10)
 		    alarm += '0';
 		alarm += minute;
 		
 		String clock = "0" + nowH;
 		clock = clock.substring(clock.length() - 2);
 		clock += ':';
 		if (nowM < 10)
 		    clock += '0';
 		clock += nowM;
 		
 		System.out.print("\033c");
 		System.out.println("Alarm time: " + alarm);
 		System.out.println("Clock time: " + clock);
 		
 		final int alarmMin = hour * 60 + minute;
 		final int nowMin = nowH * 60 + nowM;
 		
 		final int diffMin = alarmMin - nowMin;
 		
 		if (diffMin == 0)
 		{
 		    run(command);
 		    System.out.print("\033c");
		    final String check = "Press Enter to snooze, otherwise type this line itself, in upper case.";
 		    System.out.println(check);
		    if (sc.nextLine().equals(check.toUpperCase()) == false)
 			i--;
 		    continue;
 		}
 		
 		if (diffMin == 1)
 		{
 		    final int remaining = 60 - Calendar.getInstance().get(Calendar.SECOND);
 		    Thread.sleep((remaining > 10 ? 10 : remaining) * 1000);
 		}
 		else
 		    Thread.sleep(20000);
 		i--;
 	    }
 	    catch (final Throwable err)
 	    {
 		//resume
 	    }
 	
 	System.out.println("Nothing more to do.");
 	System.out.println("Goodbye!");
     }
     
     
     /**
      * Runs a command
      *
      * @param  command  The command to run
      */
     public static void run(final String command)
     {
 	try
 	{
 	    final Process process = (new ProcessBuilder("/bin/sh", "-c", command)).start();
 	    final InputStream stream = process.getInputStream();
 	    for (;;)
 		if (stream.read() == -1)
 		    break;
 	    
 	    try
 	    {
 		stream.close();
 	    }
 	    catch (final Throwable err)
 	    {
 		//Ignore
 	    }
 	}
 	catch (final Throwable err)
 	{
 	    //Ignore
 	}
     }
     
     
     /**
      * Gets a system property
      *
      * @param   property  The property name
      * @return            The property value
      */
     public static String getProperty(final String property)
     {
 	try
 	{
 	    final Process process = (new ProcessBuilder("/bin/sh", "-c", "echo $" + property)).start();
 	    String rcs = new String();
 	    final InputStream stream = process.getInputStream();
 	    int c;
 	    while (((c = stream.read()) != '\n') && (c != -1))
 		rcs += (char)c;
 	    try
 	    {
 		stream.close();
 	    }
 	    catch (final Throwable err)
 	    {
 		//Ignore
 	    }
 	    return rcs;
 	}
 	catch (final Throwable err)
 	{
 	    return new String();
 	}
     }
 }
