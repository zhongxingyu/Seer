 package com.ss.code2html.engine.formatter;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 
 import com.ss.code2html.engine.IHtmlFormatter;
 import com.ss.code2html.engine.IHtmlTheme;
 import com.ss.code2html.utils.Utils;
 
 public abstract class AbstractHtmlFormatter implements IHtmlFormatter {
 
 	public abstract FormatCtx createCtx();
 	
 	public abstract FormatCtx formatLineOfCode(String line, IHtmlTheme theme, FormatCtx ctx);
 
 	@Override
 	public String format(BufferedReader reader, IHtmlTheme theme) {
 		String code = formatImpl(reader, theme);
 		return wrap(code, theme);
 	}
 
 	private String formatImpl(BufferedReader reader, IHtmlTheme theme) {
 		try {
 			String line = reader.readLine();
 			FormatCtx ctx = createCtx();
 			while (line != null) {
 				ctx = formatLineOfCode(line, theme, ctx);
 				line = reader.readLine();
 			}
 			return ctx.getFormattedLine();
 		} catch (IOException e) {
 			System.out.println("Exception while formating code");
 			return "failed to parse";
 		}
 	}
 
 	private String wrap(String code, IHtmlTheme theme) {
 		StringBuilder sb = new StringBuilder();
 
 		sb.append("<div style='font-size:12px;line-height:1.3;padding:7px;border:1px solid "
 				+ theme.getBorderColor()
 				+ ";-moz-border-radius: 3px;-webkit-border-radius: 3px;border-radius: 3px;position:relative;background-color: "
 				+ theme.getBackgroundColor() + ";'>");
 		sb.append(genCopyrights(theme));
 		sb.append(code);
 		sb.append("</div>");
 
 		return sb.toString();
 	}
 
 	private String genCopyrights(IHtmlTheme theme) {
         StringBuilder sb = new StringBuilder();
        sb.append("<div style='font-size:10px;color:" + theme.getTrademarkColor() + ";font-style:italic;width:100%;text-align:right;'>");
 		sb.append("<span>Powered by</span>&nbsp;<a style='color:"
 				+ theme.getTrademarkColor()
				+ ";' href='https://github.com/ssinica/code2html'>code2html</a>");
         sb.append("</div>");
         return sb.toString();
     }
 	
 	protected String genSpan(String text, String style) {
 		return "<span style='" + style + "'>" + text + "</span>";
 	}
 
 	protected String wrapWithSpacesAddBr(String line, int left, int right) {
 		StringBuffer buf = new StringBuffer();
 		for (int i = 0; i < left; i++) {
 			buf.append("&nbsp;");
 		}
 		buf.append(line);
 		for (int i = 0; i < right; i++) {
 			buf.append("&nbsp;");
 		}
 		buf.append("<br>");
 		return buf.toString();
 	}
 
 	protected String trimRight(String value) {
 		return value.replaceAll("\\s*$", "");
 	}
 
 	protected String trimLeft(String value) {
 		return value.replaceAll("^\\s*", "");
 	}
 
 	protected String[] checkForRigthPunctuation(String token) {
 		String[] ret = new String[2];
 		if (Utils.isEmpty(token) || token.length() < 2) {
 			ret[0] = token;
 			return ret;
 		}
 		if (token.endsWith(":") || token.endsWith(",") || token.endsWith(";")) {
 			ret[0] = token.substring(0, token.length() - 1);
 			ret[1] = token.substring(token.length() - 1);
 		} else {
 			ret[0] = token;
 		}
 		return ret;
 	}
 
 }
