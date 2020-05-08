 package org.wicketstuff.scriptaculous.dragdrop;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.markup.MarkupStream;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.util.resource.IResourceStream;
 import org.wicketstuff.scriptaculous.JavascriptBuilder;
 import org.wicketstuff.scriptaculous.ScriptaculousAjaxBehavior;
 import org.wicketstuff.scriptaculous.JavascriptBuilder.JavascriptFunction;
 import org.wicketstuff.scriptaculous.effect.Effect;
 
 
 /**
  * 
  * @see http://wiki.script.aculo.us/scriptaculous/show/Droppables.add
  */
 public abstract class DraggableTarget extends WebMarkupContainer
 {
 	private static final long serialVersionUID = 1L;
 	private final ScriptaculousAjaxBehavior onDropBehavior;
 	private final Map dropOptions = new HashMap();
 
 	public DraggableTarget(String id)
 	{
 		super(id);
 
 		setOutputMarkupId(true);
 		this.onDropBehavior = new DraggableTargetBehavior();
 		add(onDropBehavior);
 	}
 
 	protected abstract void onDrop(String input, AjaxRequestTarget target);
 
 	public void accepts(DraggableImage image)
 	{
 		// TODO: this should build a string array of classes so that one target
 		// can accept multiple classes.
 		dropOptions.put("accept", image.getStyleClass());
 	}
 
 	protected void onRender(MarkupStream markupStream)
 	{
 		super.onRender(markupStream);
 
		dropOptions.put("onDrop", new JavascriptFunction("function() { wicketAjaxGet('"
				+ onDropBehavior.getCallbackUrl() + "'); }"));

 		JavascriptBuilder builder = new JavascriptBuilder();
 		builder.addLine("Droppables.add('" + getMarkupId() + "', ");
 		builder.addOptions(dropOptions);
 		builder.addLine(");");
 
 		getResponse().write(builder.buildScriptTagString());
 	}
 	
 	private class DraggableTargetBehavior extends ScriptaculousAjaxBehavior
 	{
 		private static final long serialVersionUID = 1L;
 
 		protected IResourceStream getResponse() {
 			String input = getRequest().getParameter("id");
 			AjaxRequestTarget target = new AjaxRequestTarget();
 			getRequestCycle().setRequestTarget(target);
 			target.addComponent(DraggableTarget.this);
 			target.appendJavascript(new Effect.Highlight(DraggableTarget.this).toJavascript());
 
 			onDrop(input, target);
 			return null;
 		}
 
 	}
 	
 }
