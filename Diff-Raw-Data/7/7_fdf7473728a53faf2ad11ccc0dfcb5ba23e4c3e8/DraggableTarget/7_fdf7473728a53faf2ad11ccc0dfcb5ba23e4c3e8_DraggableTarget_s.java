 package org.wicketstuff.scriptaculous.dragdrop;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.apache.wicket.Component;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.behavior.IBehavior;
 import org.apache.wicket.markup.MarkupStream;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.wicketstuff.scriptaculous.JavascriptBuilder;
 import org.wicketstuff.scriptaculous.ScriptaculousAjaxBehavior;
import org.wicketstuff.scriptaculous.JavascriptBuilder.AjaxCallbackJavascriptFunction;
 import org.wicketstuff.scriptaculous.effect.Effect;
 import org.wicketstuff.scriptaculous.sortable.SortableContainer;
 
 
 /**
  * Target for drag/drop operations.
  * user can drop a Draggable item onto this component to perform ajax operation.
  *
  * @see http://wiki.script.aculo.us/scriptaculous/show/Droppables.add
  */
 public abstract class DraggableTarget extends WebMarkupContainer
 {
 	private static final long serialVersionUID = 1L;
 	private final ScriptaculousAjaxBehavior onDropBehavior = new DraggableTargetBehavior();
 	private final Map dropOptions = new HashMap();
 
 	public DraggableTarget(String id)
 	{
 		super(id);
 
 		setOutputMarkupId(true);
 		add(onDropBehavior);
 	}
 
 	/**
 	 * extension point for defining functionality when a {@link DraggableImage} is dropped.
 	 * @param input the id attribute of the dropped component
 	 */
 	protected abstract void onDrop(String input, AjaxRequestTarget target);
 
 	/**
 	 * configure the draggable target to accept a component.
 	 * The component must have a {@link DraggableBehavior} attached to it.
 	 * @param component
 	 */
 	public void accepts(Component component) {
 		for (Iterator iter = component.getBehaviors().iterator(); iter.hasNext();) {
 			IBehavior behavior = (IBehavior) iter.next();
 			if (behavior instanceof DraggableBehavior) {
 				addAcceptClass(((DraggableBehavior)behavior).getDraggableClassName());
 			}
 		}
 	}
 
 	/**
 	 * configure the draggable target to accept any draggable item from the {@link SortableContainer}
 	 * The sortable container needs to override {@link SortableContainer#getDraggableClassName()} 
 	 * in order for the draggable target to know what to accept.
 	 * @param container
 	 */
 	public void acceptAll(SortableContainer container) {
 		addAcceptClass(container.getDraggableClassName());
 	}
 
 	/**
 	 * TODO: this should build a string array of classes so that one target
 	 * can accept multiple classes.
 	 * @param className
 	 */
 	private void addAcceptClass(String className) {
 		dropOptions.put("accept", className);
 	}
 
 	/**
 	 * set an additional CSS class for when an accepted Draggable is hovered over it.
 	 * default is none
 	 * @param className
 	 */
 	public void setHoverClass(String className) {
 		dropOptions.put("hoverclass", className);
 	}
 
 	protected void onRender(MarkupStream markupStream)
 	{
 		super.onRender(markupStream);
 
		dropOptions.put("onDrop", new AjaxCallbackJavascriptFunction(onDropBehavior));
 
 		JavascriptBuilder builder = new JavascriptBuilder();
 		builder.addLine("Droppables.add('" + getMarkupId() + "', ");
 		builder.addOptions(dropOptions);
 		builder.addLine(");");
 
 		getResponse().write(builder.buildScriptTagString());
 	}
 
 	private class DraggableTargetBehavior extends ScriptaculousAjaxBehavior
 	{
 		private static final long serialVersionUID = 1L;
 
 		public void onRequest() {
 			String input = getRequest().getParameter("id");
 			AjaxRequestTarget target = new AjaxRequestTarget();
 			getRequestCycle().setRequestTarget(target);
 			target.addComponent(DraggableTarget.this);
 			target.appendJavascript(new Effect.Highlight(DraggableTarget.this).toJavascript());
 
 			onDrop(input, target);
 		}
 	}
 }
