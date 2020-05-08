 /**
  * 
  */
 package org.whizu.ui;
 
 import org.whizu.dom.Markup;
 import org.whizu.jquery.Function;
 import org.whizu.jquery.Script;
 import org.whizu.widget.Widget;
 
 /**
  * @author Rudy D'hauwe
  */
 class Timeout extends Widget {
 
 	private Function action;
 	private int milliseconds;
 
 	public Timeout(int milliseconds, Function action) {
 		this.milliseconds = milliseconds;
 		this.action = action;
 		compile();
 	}
 
 	@Override
 	public Markup compile() {
 		Script script = compile(action);
		String text = "setTimeout(function(){" + script.toJavaScript() + "}," + milliseconds + ")";
 		System.out.println("Timeout script: " + text);
 		getRequest().execute(text);
 		return null;
 	}
 }
