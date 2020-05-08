 package org.rsbot.script.methods;
 
import org.rsbot.script.util.PaintUIUtil;
import org.rsbot.script.util.PaintUtil;
 
 public class Paint extends MethodProvider {
 
 	public Paint(final MethodContext ctx) {
 		super(ctx);
 	}
 
 	/**
 	 * Creates a new PaintUtil.
 	 *
 	 * @return New instance of PaintUtil.
 	 * @see PaintUtil
 	 */
 	public PaintUtil createPaint() {
 		return new PaintUtil(methods.bot.getMethodContext(),
 				methods.bot.getBufferGraphics());
 	}
 
 	/**
 	 * Creates a new PaintUIUtil.
 	 *
 	 * @return New instance of PaintUIUtil.
 	 * @see PaintUIUtil
 	 */
 	public PaintUIUtil createUI() {
 		return new PaintUIUtil(methods.bot.getBufferGraphics());
 	}
 
 }
