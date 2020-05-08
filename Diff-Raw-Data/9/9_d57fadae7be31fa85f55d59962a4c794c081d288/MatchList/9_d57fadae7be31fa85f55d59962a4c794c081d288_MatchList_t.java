 /**
  * Warlock, the open-source cross-platform game client
  *  
  * Copyright 2008, Warlock LLC, and individual contributors as indicated
  * by the @authors tag. 
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package cc.warlock.core.script.javascript;
 
 import java.util.HashMap;
 import java.util.concurrent.BlockingQueue;
 
 import org.mozilla.javascript.Function;
 import org.mozilla.javascript.Script;
 import org.mozilla.javascript.Scriptable;
 import org.mozilla.javascript.ScriptableObject;
 
 import cc.warlock.core.script.IMatch;
 import cc.warlock.core.script.internal.RegexMatch;
 import cc.warlock.core.script.internal.TextMatch;
 
 public class MatchList extends ScriptableObject {
 	
 	private static final long serialVersionUID = 2034366988104765649L;
 	
 	private HashMap<IMatch, Runnable> matches = new HashMap<IMatch, Runnable>();
 	private BlockingQueue<String> queue;
 	private JavascriptScript script;
 	
 	public MatchList() {
 	}
 	
 	public void jsConstructor() {
 		JavascriptCommands jsCommands = (JavascriptCommands)this.getParentScope().get("script", this.getParentScope());
 		script = jsCommands.getScript();
 		queue = script.getCommands().createLineQueue();
 	}
 	
 	private class JSMatchData implements Runnable {
 		
 		private Function function;
 		private String expression;
 		protected Object[] args = new Object[0];
 		
 		public JSMatchData(Function function, String expression) {
 			this.function = function;
 			this.expression = expression;
 		}
 		
 		public void run() {
 			if(function != null) {
 				function.call(script.getContext(), script.getScope(), null, args);
 			}
 			if(expression != null) {
 				Script jsCommand = script.getContext().compileString(expression, "matchWait", 0, null);
 
 				jsCommand.exec(script.getContext(), script.getScope());
 			}
 		}
 	}
 	
 	public IMatch jsFunction_match(String text, Object f) {
 		if(!script.isRunning()) {
 			throw new Error();
 		}
 		TextMatch match = new TextMatch(text, true);
 		Function function = null;
		if (f != null && f instanceof Function)
			function = (Function)f;
 		
 		matches.put(match, new JSMatchData(function, null));
 		
 		return match;
 	}
 	
 	private class JSRegexMatchData extends JSMatchData {
 		
 		private RegexMatch match;
 		
 		public JSRegexMatchData(RegexMatch match, Function function, String expression) {
 			super(function, expression);
 			this.match = match;
 		}
 		
 		public void run() {
 			Scriptable groups = script.getContext().newArray(script.getScope(), match.groups().size());
 			int i = 0;
 			for(String str : match.groups()) {
 				script.getScope().put(i, groups, str);
 				i++;
 			}
 			
 			args = new Object[] { groups };
 			super.run();
 		}
 	}
 	
 	public IMatch jsFunction_matchRe(String regex, Object f) {
 		script.checkStop();
 		
 		RegexMatch match = new RegexMatch(regex);
 		Function function = null;
		if (f != null && f instanceof Function)
			function = (Function)f;
 		
 		matches.put(match, new JSRegexMatchData(match, function, null));
 		
 		return match;
 	}
 	
 	public IMatch jsFunction_matchWait() {
 		script.checkStop();
 		
 		try {
 			IMatch match = script.getCommands().matchWait(matches.keySet(), queue, 0.0);
 		
 			if (match != null)
 			{
 //				script.getCommands().echo("Got a match!");
 				matches.get(match).run();
 			}
 
 			return match;
 		} catch(InterruptedException e) {
 			script.checkStop();
 			return null;
 		}
 	}
 	
 	@Override
 	public String getClassName() {
 		return "MatchList";
 	}
 
 }
