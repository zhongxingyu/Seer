 package edu.ycp.cs.netcoder.shared.logchange;
 
import java.util.Arrays;

 import com.google.gwt.user.client.rpc.IsSerializable;
 
 /**
  * Object representing a textual change.
  * The client sends these to the server so that we
  * can capture the user's edit history.
  */
 public class Change implements IsSerializable {
 	/*
     private long id;
     private long userId;
     private long problemId;
     */
 	private String type;
 	private int startRow, startColumn, endRow, endColumn;
 	private long timestamp;
 	private String[] text;
 
 	// Zero-arg constructor - required for serialization
 	public Change() {
 	}
 	
 	private Change(ChangeType type, int sr, int sc, int er, int ec, long ts) {
 		this.type = type.toString();
 		this.startRow = sr;
 		this.startColumn = sc;
 		this.endRow = er;
 		this.endColumn = ec;
 		this.timestamp = ts;
 	}
 	
 	public Change(ChangeType type, int sr, int sc, int er, int ec, long ts, String text) {
 		this(type, sr, sc, er, ec, ts);
 		//this.text = Collections.singletonList(text);
 		this.text = new String[]{ text };
 	}
 	
 	public Change(ChangeType type, int sr, int sc, int er, int ec, long ts, String[] textToAdopt) {
 		this(type, sr, sc, er, ec, ts);
 		this.text = textToAdopt;
 	}
 	
 	public ChangeType getType() {
 		return ChangeType.valueOf(type);
 	}
 	
 	/**
 	 * Get single chunk of text (for INSERT_TEXT and REMOVE_TEXT events).
 	 * 
 	 * @return chunk of text inserted or removed
 	 */
 	public String getText() {
 		assert text.length == 1;
 		return text[0];
 	}
 
 	/**
 	 * Get given line (for INSERT_LINES and REMOVE_LINES) events.
 	 * 
 	 * @param index index of the inserted or removed line (0 for first).
 	 * @return the inserted or removed line
 	 */
 	public String getLine(int index) {
 		return text[index];
 	}
 	
 	/**
 	 * Get number of lines.
 	 * Always 1 for INSERT_TEXT and REMOVE_TEXT events.
 	 * Could be greater than 1 for INSERT_LINES and REMOVE_LINES events.
 	 * 
 	 * @return number of lines
 	 */
 	public int getNumLines() {
 		return text.length;
 	}
 	
 	/**
 	 * @return start row of change
 	 */
 	public int getStartRow() {
 		return startRow;
 	}
 
 	/**
 	 * @return start column of change
 	 */
 	public int getStartColumn() {
 		return startColumn;
 	}
 	
 	/**
 	 * @return end row of change
 	 */
 	public int getEndRow() {
 		return endRow;
 	}
 	
 	/**
 	 * @return end column of change
 	 */
 	public int getEndColumn() {
 		return endColumn;
 	}
 	
 	/**
 	 * @return timestamp of change (milliseconds since epoch), as reported by client
 	 */
 	public long getTimestamp() {
 		return timestamp;
 	}
 	
 	@Override
 	public String toString() {
		return type + "," + startRow + "," + startColumn + "," + endRow + "," + endColumn + "," + timestamp + "," + Arrays.asList(text);
 	}
 }
