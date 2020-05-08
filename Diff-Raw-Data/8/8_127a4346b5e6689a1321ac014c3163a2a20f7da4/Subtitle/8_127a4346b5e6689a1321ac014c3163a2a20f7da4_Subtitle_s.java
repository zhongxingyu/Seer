 package com.alexrnl.subtitlecorrector.common;
 
 import com.alexrnl.commons.utils.object.AutoCompare;
 import com.alexrnl.commons.utils.object.AutoHashCode;
 import com.alexrnl.commons.utils.object.Field;
 
 /**
  * Class describing a simple subtitle.
  * @author Alex
  */
 public class Subtitle implements Comparable<Subtitle>, Cloneable {
 	
 	/** Timestamp (in milliseconds) for beginning of subtitle display */
 	private long			begin;
 	/** Timestamp (in milliseconds) for end of subtitle display */
 	private long			end;
 	/** The content of the subtitle */
 	private String			content;
 	
 	/**
 	 * Constructor #1.<br />
 	 * Default constructor.
 	 */
 	public Subtitle () {
 		this(0, 0, null);
 	}
 	
 	/**
 	 * Constructor #2.<br />
 	 * @param begin
 	 *        the beginning of the subtitle display (in milliseconds).
 	 * @param end
 	 *        the end of the subtitle display (in milliseconds).
 	 * @param content
 	 *        the content of the subtitle.
 	 */
 	public Subtitle (final long begin, final long end, final String content) {
 		super();
 		this.begin = begin;
 		this.end = end;
 		this.content = content;
 	}
 	
 	/**
 	 * Return the attribute begin.
 	 * @return the attribute begin.
 	 */
 	@Field
 	public long getBegin () {
 		return begin;
 	}
 	
 	/**
 	 * Set the attribute begin.
 	 * @param begin
 	 *        the attribute begin.
 	 */
 	public void setBegin (final long begin) {
 		this.begin = begin;
 	}
 	
 	/**
 	 * Return the attribute end.
 	 * @return the attribute end.
 	 */
 	@Field
 	public long getEnd () {
 		return end;
 	}
 	
 	/**
 	 * Set the attribute end.
 	 * @param end
 	 *        the attribute end.
 	 */
 	public void setEnd (final long end) {
 		this.end = end;
 	}
 	
 	/**
 	 * Return the attribute content.
 	 * @return the attribute content.
 	 */
 	@Field
 	public String getContent () {
 		return content;
 	}
 	
 	/**
 	 * Set the attribute content.
 	 * @param content
 	 *        the attribute content.
 	 */
 	public void setContent (final String content) {
 		this.content = content;
 	}
 	
 	/**
 	 * Computes the display duration of the subtitle.
 	 * @return the duration.
 	 */
 	public long getDuration () {
 		return end - begin;
 	}
 	
 	/**
 	 * Check the validity of the subtitle.<br />
 	 * A subtitle is valid if the end time is greater (or equal) to the begin time.
 	 * @return <true>true</true> if the subtitle is valid.
 	 */
 	public boolean isValid () {
 		return begin < end;
 	}
 	
 	@Override
 	public int hashCode () {
 		return AutoHashCode.getInstance().hashCode(this);
 	}
 
 	@Override
 	public boolean equals (final Object obj) {
 		if (!(obj instanceof Subtitle)) {
 			return false;
 		}
 		return AutoCompare.getInstance().compare(this, (Subtitle) obj);
 	}
 
 	/**
 	 * Return the text representation of the subtitle as follow:
 	 * 
 	 * <pre>
 	 * [begin, end] content
 	 * </pre>
 	 */
 	@Override
 	public String toString () {
 		return "[" + begin + ", " + end + "] " + content;
 	}
 	
 	/**
 	 * Compare the subtitles based on their begin times, then end times, and finally their content.
 	 */
 	@Override
 	public int compareTo (final Subtitle sub) {
 		final int beginCmp = Long.valueOf(begin).compareTo(sub.getBegin());
 		if (beginCmp != 0) {
 			return beginCmp;
 		}
 		final int endCmp = Long.valueOf(end).compareTo(sub.getEnd());
 		if (endCmp != 0) {
 			return endCmp;
 		}
 		return content.compareTo(sub.getContent());
 	}
 
 	@Override
 	public Subtitle clone () throws CloneNotSupportedException {
 		final Subtitle clone = (Subtitle) super.clone();
 		clone.setBegin(begin);
 		clone.setEnd(end);
 		clone.setContent(content);
 		return clone;
 	}
 	
 }
