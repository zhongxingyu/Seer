 package com.ezgliding.igc;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.nio.charset.Charset;
 import java.nio.file.Files;
 import java.nio.file.FileSystems;
 import java.nio.file.Path;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.TimeZone;
 
 public class Parser {
 
 	private enum HSubType { 
 		DTE, FXA, PLT, CM2, GTY, GID, DTM, RFW, RHW, FTY, GPS, PRS, CID, CCL
 	}
 
 	private static Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
 
 	static { cal.set(Calendar.MILLISECOND, 0); }
 
 	public Parser() {
 
 	}
 
 	public Flight parse(Path location) throws IOException, ParseException {
 		BufferedReader reader;
 		try {
 			reader = Files.newBufferedReader(location, Charset.defaultCharset());
 		} catch (IOException e) { 
 			throw new IOException("Failed to open location: " + location + "\n" + e);
 
 		}
 
 		String content = "", line;
 		try {
 			while ((line = reader.readLine()) != null)
 				content += line + "\n";
 		} catch (IOException e) { 
 			throw new IOException("Error reading file: " + location + "\n" + e);
 		}
 
 		return parse(content);
 	}
 
 	public Flight parse(String content) throws ParseException {
 		if (content == null) return null;
 
 		Flight flight = new Flight();
 
 		String[] lines = content.split("\n");
 		for (int i=0; i<lines.length; i++) {
 			if (lines[i].charAt(0) != 'C')
 				parseLine(lines[i], flight);
 			else { // C is a special case, need to pass multiple lines at once
 				ArrayList<String> cLines = new ArrayList<String>();
				cLines.add(lines[i]);
				while (lines[i+1].charAt(0) == 'C')
					cLines.add(lines[++i]);
 				parseC(cLines.toArray(new String[] {}), flight);
 			}
 		}
 
 		return flight;
 	}	
 
 	private void parseLine(String line, Flight flight) throws ParseException {
 		if (line == null || flight == null) return;
 
 		char type = line.charAt(0);
 		switch(type) {
 			case 'A':
 				parseA(line, flight);
 				break;
 			case 'B': 
 				parseB(line, flight);
 				break;
 			case 'H': 
 				parseH(line, flight);
 				break;
 			default: break;
 		}
 	}
 
 	private void parseA(String line, Flight flight) throws ParseException {
 		if (line == null || flight == null) return;
 
 		flight.setManufacturer(line.substring(1,4));
 		flight.setUniqueId(line.substring(4,7));
 		flight.setAdditionalData(line.substring(7));
 	}
 
 	private void parseB(String line, Flight flight) throws ParseException {
 		if (line == null || flight == null) return;
 
 		Fix fix = new Fix(
 			Integer.parseInt(line.substring(1,3))*3600 
 				+ Integer.parseInt(line.substring(3,5))*60
 				+ Integer.parseInt(line.substring(5,7)),
 			Util.minDec2decimal(line.substring(7,15)),
 			Util.minDec2decimal(line.substring(15,24)),
 			Integer.parseInt(line.substring(25,30)),
 			Integer.parseInt(line.substring(30,35)),
 			line.charAt(24)
 		);
 		flight.addFix(fix);
 	}
 
 	private void parseC(String[] lines, Flight flight) throws ParseException {
 		if (lines == null || flight == null) return;
 
 		Task task = new Task();
 		cal.set(
 			Integer.parseInt(lines[0].substring(5,7)), 
 			Integer.parseInt(lines[0].substring(3,5))-1, 
 			Integer.parseInt(lines[0].substring(1,3)),
 			Integer.parseInt(lines[0].substring(7,9)), 
 			Integer.parseInt(lines[0].substring(9,11)), 
 			Integer.parseInt(lines[0].substring(11,13)));
 		task.setDate(cal.getTime());
 		cal.set(
 			Integer.parseInt(lines[0].substring(17,19)), 
 			Integer.parseInt(lines[0].substring(15,17))-1, 
 			Integer.parseInt(lines[0].substring(13,15)),
 			0,0,0); 
 		task.setFlightDate(cal.getTime());
 		task.setTaskId(Integer.parseInt(lines[0].substring(19, 23)));
 		task.setDescription(lines[0].substring(25));
 
 		task.setTakeoff(new WayPoint( new Fix(0, 
 					Util.minDec2decimal(lines[1].substring(1,9)),
 					Util.minDec2decimal(lines[1].substring(9,18)),
 					0, 0, 'A'),
 				lines[1].substring(19)));
 		task.setStart(new WayPoint(new Fix(0, 
 					Util.minDec2decimal(lines[2].substring(1,9)),
 					Util.minDec2decimal(lines[2].substring(9,18)),
 					0, 0, 'A'),
 				lines[2].substring(19)));
 		task.setFinish(new WayPoint(new Fix(0, 
 					Util.minDec2decimal(lines[lines.length-2].substring(1,9)),
 					Util.minDec2decimal(lines[lines.length-2].substring(9,18)),
 					0, 0, 'A'),
 				lines[lines.length-2].substring(19)));
 		task.setLanding(new WayPoint(new Fix(0, 
 					Util.minDec2decimal(lines[lines.length-1].substring(1,9)),
 					Util.minDec2decimal(lines[lines.length-1].substring(9,18)),
 					0, 0, 'A'),
 				lines[lines.length-1].substring(19)));
 
 		for (int i=3; i<lines.length-2; i++)
 			task.addTurnPoint(new WayPoint(new Fix(0, 
 					Util.minDec2decimal(lines[i].substring(1,9)),
 					Util.minDec2decimal(lines[i].substring(9,18)),
 					0, 0, 'A'),
 				lines[i].substring(19)));
 
 		flight.setTask(task);
 
 	}
 
 	private void parseH(String line, Flight flight) throws ParseException {
 		if (line == null || flight == null) return;
 
 		String subType = line.substring(2,5);
 
 		switch (HSubType.valueOf(subType)) {
 			case DTE:
 				cal.set(
 					Integer.parseInt(line.substring(9,11)), 
 					Integer.parseInt(line.substring(7,9))-1, 
 					Integer.parseInt(line.substring(5,7)),
 					0, 0, 0);
 				flight.setDate(cal.getTime());
 				break;
 			case FXA:
 				flight.setFixAccuracy(Integer.parseInt(line.substring(5,8)));
 				break;
 			case PLT:
 				flight.setPilot(line.substring(line.indexOf(":")+1));
 				break;
 			case CM2:
 				flight.setCrew2(line.substring(line.indexOf(":")+1));
 				break;
 			case GTY:
 				flight.setGliderType(line.substring(line.indexOf(":")+1));
 				break;
 			case GID:
 				flight.setGliderId(line.substring(line.indexOf(":")+1));
 				break;
 			case DTM:
 				flight.setGpsDatum(line.substring(line.indexOf(":")+1));
 				break;
 			case RFW:
 				flight.setFirmwareVersion(line.substring(line.indexOf(":")+1));
 				break;
 			case RHW:
 				flight.setHardwareVersion(line.substring(line.indexOf(":")+1));
 				break;
 			case FTY:
 				flight.setFrType(line.substring(line.indexOf(":")+1));
 				break;
 			case GPS:
 				flight.setGpsManufacturer(line.substring(line.indexOf(":")+1));
 				break;
 			case PRS:
 				flight.setPressAltSensor(line.substring(line.indexOf(":")+1));
 				break;
 			case CID:
 				flight.setCompetitionId(line.substring(line.indexOf(":")+1));
 				break;
 			case CCL:
 				flight.setCompetitionClass(line.substring(line.indexOf(":")+1));
 				break;
 			default:
 				break;
 		}
 	}
 
 }
