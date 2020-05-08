 package nextmethod.web.mvc;
 
 import com.google.common.base.Objects;
 import com.google.common.base.Strings;
 import com.google.common.base.Throwables;
 import com.google.common.collect.Maps;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import java.util.List;
 import java.util.Map;
 
 import static com.google.common.base.Preconditions.checkArgument;
 import static com.google.common.base.Preconditions.checkNotNull;
 import static nextmethod.TypeHelpers.typeAs;
 import static nextmethod.reflection.TypeOfHelper.getType;
 
 /**
  *
  */
 public class ControllerActionInvoker implements IActionInvoker {
 
 	protected ActionResult createActionResult(final ControllerContext controllerContext, final ActionDescriptor actionDescriptor, final Object actionReturnValue) {
 		if (actionReturnValue == null)
 			return new EmptyResult();
 
 		final ActionResult actionResult = typeAs(actionReturnValue, ActionResult.class);
 		if (actionResult == null) {
 			return new ContentResult(Objects.toStringHelper(actionReturnValue).toString());
 		}
 		return actionResult;
 	}
 
 	private static <TFilter extends IMvcFilter> void addControllerToFilterList(@Nonnull final ControllerBase controller, @Nonnull final List<TFilter> filterList, @Nonnull final Class<TFilter> filterClass) {
 		final TFilter tFilter = typeAs(controller, filterClass);
 		if (tFilter != null) {
 			filterList.add(0, tFilter);
 		}
 	}
 
 	protected FilterInfo getFilters(final ControllerContext controllerContext, final ActionDescriptor actionDescriptor) {
 		final FilterInfo filters = actionDescriptor.getFilters();
 
 		// if the current controller implements one of the filter interfaces, it should be added to the list at position 0
 		final ControllerBase controller = controllerContext.getController();
 		addControllerToFilterList(controller, filters.getActionFilters(), IActionFilter.class);
 		addControllerToFilterList(controller, filters.getResultFilters(), IResultFilter.class);
 		addControllerToFilterList(controller, filters.getAuthorizationFilters(), IAuthorizationFilter.class);
 		addControllerToFilterList(controller, filters.getExceptionFilters(), IExceptionFilter.class);
 		return filters;
 	}
 
 	protected ControllerDescriptor getControllerDescriptor(final ControllerContext controllerContext) {
 		// TODO: DescriptorCache
 		return new ReflectedControllerDescriptor(getType(controllerContext.getController()));
 	}
 
 	protected ActionDescriptor findAction(final ControllerContext context, final ControllerDescriptor controllerDescriptor, final String actionName) {
 		return controllerDescriptor.findAction(context, actionName);
 	}
 
 	protected Map<String, Object> getParameterValues(final ControllerContext controllerContext, final ActionDescriptor actionDescriptor) {
 		return Maps.newHashMap();
 //		throw new NotImplementedException();
 	}
 
 	@Override
 	public boolean invokeAction(final ControllerContext controllerContext, final String actionName) {
 		checkNotNull(controllerContext);
 		checkArgument(!Strings.isNullOrEmpty(actionName));
 
 		final ControllerDescriptor controllerDescriptor = getControllerDescriptor(controllerContext);
 		final ActionDescriptor actionDescriptor = findAction(controllerContext, controllerDescriptor, actionName);
 		if (actionDescriptor != null) {
 			final FilterInfo filterInfo = getFilters(controllerContext, actionDescriptor);
 			try {
 				final AuthorizationContext authCtx = invokeAuthorizationFilters(controllerContext, filterInfo.getAuthorizationFilters(), actionDescriptor);
 				if (authCtx.getResult() != null) {
 					// the auth filter signaled that we should let it short-circuit the request
 					invokeActionResult(controllerContext, authCtx.getResult());
 				} else {
 					if (controllerContext.getController().isValidateRequest()) {
 						validateRequest(controllerContext);
 					}
 					final Map<String, Object> parameters = getParameterValues(controllerContext, actionDescriptor);
 					final ActionExecutedContext postActionCtx = invokeActionMethodWithFilters(controllerContext, filterInfo.getActionFilters(), actionDescriptor, parameters);
 					invokeActionResultWithFilters(controllerContext, filterInfo.getResultFilters(), postActionCtx.getResult());
 				}
 			}
//			catch (Thr) {
 			// This type of exception occurs as a result of Response.Redirect(), but we special-case so that
 			// the filters don't see this as an error.
 //				throw
 //			}
 			catch (final Exception ex) {
 				// something blew up, so execute the exception filters
 				final ExceptionContext exceptionContext = invokeExceptionFilters(controllerContext, filterInfo.getExceptionFilters(), ex);
 				if (!exceptionContext.isExceptionHandled())
 					Throwables.propagate(ex);
 
 				invokeActionResult(controllerContext, exceptionContext.getResult());
 			}
 
			invokeActionMethod(controllerContext, actionDescriptor, Maps.<String, Object>newHashMap());
 			return true;
 		}
 
 		// Notify controller that no method matched
 		return false;
 	}
 
 	protected ActionResult invokeActionMethod(final ControllerContext controllerContext, final ActionDescriptor actionDescriptor, @Nullable final Map<String, Object> parameters) {
 		final Object returnValue = actionDescriptor.execute(controllerContext, parameters);
 		return createActionResult(controllerContext, actionDescriptor, returnValue);
 	}
 
 	protected ActionExecutedContext invokeActionMethodWithFilters(final ControllerContext controllerContext, final List<IActionFilter> filters, final ActionDescriptor actionDescriptor, final Map<String, Object> parameters) {
 		final ActionExecutingContext preCtx = new ActionExecutingContext(controllerContext, actionDescriptor, parameters);
 		final ActionMethodFilterProcessor processor = new ActionMethodFilterProcessor(filters, new ActionMethodFilterProcessor.IActionMethodFunction() {
 			@Override
 			public ActionResult invoke(final ControllerContext context, final ActionDescriptor actionDescriptor, final Map<String, Object> parameters) {
 				return invokeActionMethod(context, actionDescriptor, parameters);
 			}
 		});
 
 		return processor.process(preCtx, controllerContext, actionDescriptor, parameters);
 	}
 
 	protected ResultExecutedContext invokeActionResultWithFilters(final ControllerContext controllerContext, final List<IResultFilter> filters, final ActionResult actionResult) {
 		final ResultExecutingContext resultExecutingContext = new ResultExecutingContext(controllerContext, actionResult);
 //		throw new NotImplementedException();
 		invokeActionResult(controllerContext, actionResult);
 		return null;
 	}
 
 	protected void invokeActionResult(final ControllerContext controllerContext, final ActionResult actionResult) {
 		actionResult.executeResult(controllerContext);
 	}
 
 	protected ExceptionContext invokeExceptionFilters(final ControllerContext controllerContext, List<IExceptionFilter> filters, final Exception exception) {
 		final ExceptionContext ctx = new ExceptionContext(controllerContext, exception);
 		for (IExceptionFilter filter : filters) {
 			filter.onException(ctx);
 		}
 		return ctx;
 	}
 
 	protected AuthorizationContext invokeAuthorizationFilters(final ControllerContext controllerContext, final List<IAuthorizationFilter> filters, final ActionDescriptor actionDescriptor) {
 		final AuthorizationContext authorizationContext = new AuthorizationContext(controllerContext, actionDescriptor);
 		for (IAuthorizationFilter filter : filters) {
 			filter.onAuthorization(authorizationContext);
 			if (authorizationContext.getResult() != null) {
 				break;
 			}
 		}
 		return authorizationContext;
 	}
 
 	static void validateRequest(final ControllerContext controllerContext) {
 		if (controllerContext.isChildAction())
 			return;
 
 //		controllerContext.getHttpContext().getRequest().validateInput();
 	}
 }
