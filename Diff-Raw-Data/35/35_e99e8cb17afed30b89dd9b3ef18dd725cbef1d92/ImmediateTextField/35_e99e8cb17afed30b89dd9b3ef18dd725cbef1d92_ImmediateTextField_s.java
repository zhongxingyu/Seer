 /*
  * $Id: ImmediateTextField.java 673 2006-04-06 12:53:07 -0700 (Thu, 06 Apr 2006)
  * joco01 $ $Revision$ $Date$
  * 
  * ==============================================================================
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package wicket.contrib.markup.html.form;
 
 import wicket.MarkupContainer;
 import wicket.ResourceReference;
 import wicket.contrib.dojo.DojoAjaxHandler;
 import wicket.markup.ComponentTag;
 import wicket.markup.html.IHeaderResponse;
 import wicket.markup.html.form.TextField;
 import wicket.model.IModel;
 import wicket.util.resource.IResourceStream;
 import wicket.util.resource.StringBufferResourceStream;
 import wicket.util.string.AppendingStringBuffer;
import wicket.util.value.ValueMap;
 
 
 /**
  * Simple Dojo textfield folowing the example of ImmediateCheckBox
  * 
  * Textfield that updates the server side model using AJAX whenever the user
  * focusses on another formcomponent (onblur). After updating, the method
  * onAjaxModelUpdated is called to allow users to do custom handling like
  * persisting the change to a database.
  * 
  * Example:
  * 
  * add(textfield = new ImmediateTextField("textfield", new
  * PropertyModel(Index.this, "inputText")) { protected void onAjaxModelUpdated() {
  * //to see if model has been updated System.out.println("model: " +
  * (Index.this.toString())); } } private static final long serialVersionUID =
  * 1L;} );
  * 
  * @author Eelco Hillenius
  * @author Marco van de Haar
  * @author Ruud Booltink
  * @param <T>
  */
 public class ImmediateTextField<T> extends TextField<T>
 {
 	/**
 	 * Construct.
 	 * 
 	 * @param parent
 	 * @param id
 	 *            component id
 	 */
 	public ImmediateTextField(MarkupContainer parent, String id)
 	{
 		super(parent, id);
 		add(new ImmediateUpdateAjaxHandler());
 	}
 
 	/**
 	 * Construct.
 	 * 
 	 * @param parent
 	 * 
 	 * @param id
 	 * @param model
 	 */
 	public ImmediateTextField(MarkupContainer parent, String id, IModel<T> model)
 	{
 		super(parent, id, model);
 		add(new ImmediateUpdateAjaxHandler());
 	}
 
 	/**
 	 * @param parent
 	 * @param id
 	 * @param type
 	 */
 	public ImmediateTextField(MarkupContainer parent, String id, Class<T> type)
 	{
 		super(parent, id, type);
 		add(new ImmediateUpdateAjaxHandler());
 	}
 
 	/**
 	 * @param parent
 	 * @param id
 	 * @param model
 	 * @param type
 	 */
 	public ImmediateTextField(MarkupContainer parent, String id, IModel<T> model, Class<T> type)
 	{
 		super(parent, id, model, type);
 		add(new ImmediateUpdateAjaxHandler());
 	}
 
 
 	/**
 	 * Called after the model is updated. Use this method to e.g. update the
 	 * persistent model. Does nothing by default.
 	 */
 	protected void onAjaxModelUpdated()
 	{
 	}
 
 	/**
 	 * Ajax handler that immediately updates the attached component when the
 	 * onblur event happens.
 	 */
 	private static class ImmediateUpdateAjaxHandler extends DojoAjaxHandler
 	{
 		/** checkbox this handler is attached to. */
 		private ImmediateTextField textField;
 
 		/**
 		 * Construct.
 		 */
 		public ImmediateUpdateAjaxHandler()
 		{
 		}
 
 		/**
 		 * @see wicket.contrib.dojo.DojoAjaxHandler#renderHead(wicket.markup.html.IHeaderResponse)
 		 */
 		@Override
 		public void renderHead(IHeaderResponse response)
 		{
 			super.renderHead(response);
 
 			response.renderJavascriptReference(new ResourceReference(ImmediateTextField.class,
 					"ImmediateTextField.js"));
 		}
 
 		/**
 		 * Attaches the event handler for the given component to the given tag.
 		 * 
 		 * @param tag
 		 *            The tag to attach
 		 */
 		public final void onComponentTag(final ComponentTag tag)
 		{
			final ValueMap attributes = tag.getAttributes();
 			final AppendingStringBuffer attributeValue = new AppendingStringBuffer(
 					"javascript:immediateTextField('").append(getCallbackUrl()).append("', '")
 					.append(textField.getInputName()).append("', this.value);");
 			attributes.put("onblur", attributeValue);
 		}
 
 		/**
 		 * @see wicket.AjaxHandler#onBind()
 		 */
 		protected void onBind()
 		{
 			this.textField = (ImmediateTextField)getComponent();
 		}
 
 		/**
 		 * Gets the resource to render to the requester.
 		 * 
 		 * @return the resource to render to the requester
 		 */
 		protected final IResourceStream getResponse()
 		{
 			// let the form component update its model
 			textField.convert();
 			textField.updateModel();
 			textField.onAjaxModelUpdated();
 			return new StringBufferResourceStream();
 		}
 	}
 
 }
