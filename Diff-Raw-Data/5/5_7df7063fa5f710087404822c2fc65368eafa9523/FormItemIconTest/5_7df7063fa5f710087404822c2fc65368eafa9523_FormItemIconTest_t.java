 package org.vaadin.smartgwt.server.form.fields;
 
 import static org.junit.Assert.*;
 import static org.mockito.Matchers.*;
 import static org.mockito.Mockito.*;
 
 import java.util.Map;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.ArgumentCaptor;
 import org.vaadin.smartgwt.server.form.DynamicForm;
 import org.vaadin.smartgwt.server.form.fields.events.FormItemClickHandler;
 import org.vaadin.smartgwt.server.form.fields.events.FormItemIconClickEvent;
 
 import com.google.common.collect.Maps;
 import com.google.web.bindery.event.shared.HandlerRegistration;
 import com.vaadin.terminal.PaintException;
 import com.vaadin.terminal.PaintTarget;
 import com.vaadin.terminal.gwt.server.JsonPaintTarget;
 
 public class FormItemIconTest {
 	private FormItemIcon formItemIcon;
 
 	@Before
 	public void before() {
 		formItemIcon = new FormItemIcon();
 	}
 
 	@Test
 	public void test_canAddFormItemIconClickHandler() {
 		final FormItemClickHandler handler = mock(FormItemClickHandler.class);
 		formItemIcon.addFormItemClickHandler(handler);
 		assertTrue(formItemIcon.getFormItemClickHandlers().contains(handler));
 	}
 
 	@Test
 	public void test_canRemoveFormItemClickHandlerWithHandlerRegistration() {
 		final FormItemClickHandler handler = mock(FormItemClickHandler.class);
 		final HandlerRegistration registration = formItemIcon.addFormItemClickHandler(handler);
 		registration.removeHandler();
 		assertFalse(formItemIcon.getFormItemClickHandlers().contains(handler));
 	}
 
 	@Test
 	public void test_paintHasFormItemClickHandlersAttributeWhenHandlersAreRegistered() throws PaintException {
 		formItemIcon.addFormItemClickHandler(mock(FormItemClickHandler.class));
 		final PaintTarget paintTarget = mock(JsonPaintTarget.class);
 		formItemIcon.paintContent(paintTarget);
		verify(paintTarget).addAttribute("*hasFormItemClickHandlers", true);
 	}
 
 	@Test
 	public void test_dontPaintHasFormItemClickHandlersAttributeWhenNoHandlerRegistered() throws PaintException {
 		final PaintTarget paintTarget = mock(JsonPaintTarget.class);
 		formItemIcon.paintContent(paintTarget);
		verify(paintTarget, never()).addAttribute(eq("*hasFormItemClickHandlers"), anyBoolean());
 	}
 
 	@Test
 	public void test_firesEventFromChangedVariables() {
 		final FormItemClickHandler handler = mock(FormItemClickHandler.class);
 		formItemIcon.addFormItemClickHandler(handler);
 
 		final Map<String, Object> variables = Maps.newHashMap();
 		variables.put("formItemIconClickEvent.form", mock(DynamicForm.class));
 		variables.put("formItemIconClickEvent.item", mock(FormItem.class));
 		variables.put("formItemIconClickEvent.icon", mock(FormItemIcon.class));
 
 		formItemIcon.changeVariables(null, variables);
 
 		final ArgumentCaptor<FormItemIconClickEvent> captor = ArgumentCaptor.forClass(FormItemIconClickEvent.class);
 		verify(handler).onFormItemClick(captor.capture());
 		assertEquals(variables.get("formItemIconClickEvent.form"), captor.getValue().getForm());
 		assertEquals(variables.get("formItemIconClickEvent.item"), captor.getValue().getItem());
 		assertEquals(variables.get("formItemIconClickEvent.icon"), captor.getValue().getIcon());
 	}
 
 	@Test
 	public void test_noEventFiredWhenNoEventInChangedVariable() {
 		final FormItemClickHandler handler = mock(FormItemClickHandler.class);
 		formItemIcon.addFormItemClickHandler(handler);
 
 		final Map<String, Object> variables = Maps.newHashMap();
 		variables.put("something", 1);
 
 		formItemIcon.changeVariables(null, variables);
 		verify(handler, never()).onFormItemClick(any(FormItemIconClickEvent.class));
 	}
 }
