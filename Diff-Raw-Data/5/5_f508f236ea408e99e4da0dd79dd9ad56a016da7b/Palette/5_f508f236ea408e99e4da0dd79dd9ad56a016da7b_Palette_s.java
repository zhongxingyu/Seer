 package us.exultant.ahs.terminal;
 
 import us.exultant.ahs.terminal.Terminal.Color;
 import static us.exultant.ahs.terminal.TermCodes.*;
 
 public class Palette {
 	/**
 	 * @param $fg
 	 */
 	public Palette(Color $fg) {
		this($fg, null);
 	}
 	
 	/**
 	 * @param $bg
 	 * @param $fg
 	 */
 	public Palette(Color $fg, Color $bg) {
 		this($fg, $bg, false, false);
 	}
 
 	/**
 	 * @param $bg
 	 * @param $fg
 	 * @param $bold
 	 * @param $underline
 	 */
 	public Palette(Color $fg, Color $bg, Boolean $bold, Boolean $underline) {
 		this.$fg = $fg;
 		this.$bg = $bg;
 		this.$bold = $bold;
 		this.$underline = $underline;
 	}
 	
 	private Palette(Palette $x) {
 		this.$fg = $x.$fg;
 		this.$bg = $x.$bg;
 		this.$bold = $x.$bold;
 		this.$underline = $x.$underline;
 	}
 	
 	private Color	$fg;
 	private Color	$bg;
 	private Boolean	$bold;
 	private Boolean	$underline;
 	
 	/** Forks a new Palette with all the same settings as the subject except for the requested change. */
 	public Palette setForeground(Color $fg) {
 		if (this.$fg == $fg) return this;
 		Palette $v = new Palette(this); $v.$fg = $fg; return $v;
 	}
 	
 	/** Forks a new Palette with all the same settings as the subject except for the requested change. */
 	public Palette setBackground(Color $bg) {
 		if (this.$bg == $bg) return this;
 		Palette $v = new Palette(this); $v.$bg = $bg; return $v;
 	}
 	
 	/** Forks a new Palette with all the same settings as the subject except for the requested change. */
 	public Palette setBold(Boolean $bold) {
 		if (this.$bold == $bold) return this;
 		Palette $v = new Palette(this); $v.$bold = $bold; return $v;
 	}
 	
 	/** Forks a new Palette with all the same settings as the subject except for the requested change. */
 	public Palette setUnderline(Boolean $underline) {
 		if (this.$underline == $underline) return this;
 		Palette $v = new Palette(this); $v.$underline = $underline; return $v;
 	}
 	
 	
 	
 	String code() {
 		return
 		(($fg == null) ? "" : CSI+"3"+$fg.code()+"m") +
 		(($bg == null) ? "" : CSI+"4"+$bg.code()+"m") +
		//(($bold == null) ? "" : ($bold) ? CSI+"1;m" : "") +
 		(($underline == null) ? "" : ($underline) ? REND_UNDERLINE_ON : REND_UNDERLINE_OFF);
 	}
 	
 	//XXX:AHS:TERMINAL: would be nice to have something that can diff from an assumed palette to keep the number of characters we have to pump out to a minimum.  (on the other hand that can't be cached statically quite as handily.  well, maybe.)
 }
