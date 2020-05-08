 package wicket.contrib.markup.html.form;
 
 /*
  * 
  */
 
 import java.util.List;
 
 import wicket.MarkupContainer;
 import wicket.contrib.dojo.DojoAjaxHandler;
 import wicket.markup.ComponentTag;
 import wicket.markup.html.IHeaderResponse;
 import wicket.markup.html.form.RadioChoice;
 import wicket.model.IModel;
 import wicket.util.resource.IResourceStream;
 import wicket.util.resource.StringBufferResourceStream;
 import wicket.util.string.AppendingStringBuffer;
import wicket.util.value.IValueMap;
 
 
 /**
  * @author Ruud Booltink
  * @author Marco van de Haar
  * @param <T>
  */
 public class ImmediateRadioChoice<T> extends RadioChoice<T>
 {
 
 	/**
 	 * Ajax handler that immediately updates the attached component when the
 	 * onclick event happens.
 	 */
 	public static class ImmediateUpdateAjaxHandler extends DojoAjaxHandler
 	{
 		/** checkbox this handler is attached to. */
 		private ImmediateRadioChoice radioButton;
 
 		private int size;
 
 		/**
 		 * Construct.
 		 */
 		public ImmediateUpdateAjaxHandler()
 		{
 		}
 
 		/**
 		 * Attaches the event handler for the given component to the given tag.
 		 * 
 		 * @param tag
 		 *            The tag to attach
 		 */
 		public final void onComponentTag(final ComponentTag tag)
 		{
 			// List l = getChoices();
			final IValueMap attributes = tag.getAttributes();
 			final AppendingStringBuffer attributeValue = new AppendingStringBuffer(
 					"javascript:immediateRadioButton('").append(getCallbackUrl()).append("', '")
 					.append(radioButton.getInputName()).append("',  getSelectedRadio('").append(
 							radioButton.getInputName()).append("' ,").append(
 							((ImmediateRadioChoice)(getComponent())).getNumItems() + "))");
 			attributes.put("onclick", attributeValue);
 		}
 
 		/**
 		 * @see wicket.contrib.dojo.DojoAjaxHandler#renderHead(wicket.markup.html.IHeaderResponse)
 		 */
 		@Override
 		public void renderHead(IHeaderResponse response)
 		{
 			super.renderHead(response);
 			AppendingStringBuffer s = new AppendingStringBuffer(
 					"\t<script language=\"JavaScript\" type=\"text/javascript\">\n"
 							+ "\tfunction getSelectedRadio(nodeId, numItems)\n\t{\n"
 							+ "\t\tvar value = 'NOT_SET';\n"
 							+ "\t\tvar i = 0;\n"
 							+ "\t\twhile(value=='NOT_SET')\n"
 							+ "\t\t{\n"
 							+ "\t\t\tvar itemId = nodeId + '_' + i;\n"
 							+ "\t\t\tvar item = document.getElementById(itemId);\n"
 							+ "\t\t\tif(item.checked)\n"
 							+ "\t\t\t{\n"
 							+ "\t\t\t\tvalue = item.value;\n"
 							+ "\t\t\t}\n"
 							+ "\t\t\t++i;\n"
 							+ "\t\t}\n\t\treturn value;\n"
 							+ "\t}\n\n"
 							+ "\tfunction immediateRadioButton(componentUrl, componentPath, val) { \n"
 							+ "\t\tdojo.io.bind({\n"
 							+ "\t\t\turl: componentUrl + '&' + componentPath + '=' + val,\n"
 							+ "\t\t\tmimetype: \"text/plain\",\n"
 							+ "\t\t\tload: function(type, data, evt) {");
 
 			if (radioButton.getJSCallbackFunctionName() != null)
 			{
 				s.append(radioButton.getJSCallbackFunctionName()).append("(type, data, evt);");
 			}
 
 			s.append("}\n\t\t});\n\t}\n\t</script>\n");
 
 			response.renderString(s.toString());
 		}
 
 
 		/**
 		 * Gets the resource to render to the requester.
 		 * 
 		 * @return the resource to render to the requester
 		 */
 		protected final IResourceStream getResponse()
 		{
 			// let the form component update its model
 			radioButton.convert();
 			radioButton.updateModel();
 			radioButton.onAjaxModelUpdated();
 			return radioButton.getResponseResourceStream();
 		}
 
 		/**
 		 * @see wicket.behavior.AjaxHandler#onBind()
 		 */
 		protected void onBind()
 		{
 			this.radioButton = (ImmediateRadioChoice)getComponent();
 		}
 	}
 
 
 	private final int numItems;
 
 	/**
 	 * Construct.
 	 * 
 	 * @param parent
 	 * 
 	 * @param id
 	 * @param model
 	 * @param list
 	 */
 	public ImmediateRadioChoice(MarkupContainer parent, String id, IModel<T> model, List<T> list)
 	{
 		super(parent, id, model, list);
 		this.numItems = list.size();
 		add(new ImmediateUpdateAjaxHandler());
 	}
 
 
 	/**
 	 * @return number of items
 	 */
 	public int getNumItems()
 	{
 		return this.numItems;
 	}
 
 	/**
 	 * Gets the default choice.
 	 * 
 	 * @param selected
 	 * @return The default choice
 	 * @see wicket.markup.html.form.AbstractSingleSelectChoice#getDefaultChoice(java.lang.Object)
 	 */
 	protected CharSequence getDefaultChoice(java.lang.Object selected)
 	{
 		return getChoices().get(0).toString();
 	}
 
 	/**
 	 * Returns the name of the javascript method that will be invoked when the
 	 * processing of the ajax callback is complete. The method must have the
 	 * following signature: <code>function(type, data, evt)</code> where the
 	 * data argument will be the value of the resouce stream provided by
 	 * <code>getResponseResourceStream</code> method.
 	 * 
 	 * For example if we want to echo the value returned by
 	 * getResponseResourceStream stream we can implement it as follows: <code>
 	 * <pre>
 	 *         
 	 *         getJsCallbackFunctionName() {return(&quot;handleit&quot;);}
 	 *         
 	 *         in javascript:
 	 *         
 	 *         function handleit(type, data, evt) { alert(data); } 
 	 * </pre>
 	 * </code>
 	 * 
 	 * @see ImmediateCheckBox#getResponseResourceStream()
 	 * @return name of the client-side javascript callback handler
 	 */
 	protected String getJSCallbackFunctionName()
 	{
 		return null;
 	}
 
 	/**
 	 * returns the resource stream whose value will become the value of the
 	 * <code>data</code> argument in the defined client-side javascript
 	 * callback handler.
 	 * 
 	 * 
 	 * @return resource stream used as <code>data</code> argument in
 	 *         client-side javascript callback handler
 	 */
 	protected IResourceStream getResponseResourceStream()
 	{
 		return new StringBufferResourceStream();
 	}
 
 	/**
 	 * Called after the model is updated. Use this method to e.g. update the
 	 * persistent model. Does nothing by default.
 	 */
 	protected void onAjaxModelUpdated()
 	{
 	}
 
 }
