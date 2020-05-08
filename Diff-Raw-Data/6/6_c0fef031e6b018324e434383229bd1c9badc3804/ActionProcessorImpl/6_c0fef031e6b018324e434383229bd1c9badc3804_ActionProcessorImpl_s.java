 /*
  * Copyright 2004-2008 the Seasar Foundation and the Others.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  * either express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  */
 package org.seasar.cubby.controller.impl;
 
 import static org.seasar.cubby.CubbyConstants.ATTR_ACTION;
 import static org.seasar.cubby.CubbyConstants.ATTR_PARAMS;
 import static org.seasar.cubby.CubbyConstants.ATTR_ROUTINGS;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.seasar.cubby.action.Action;
 import org.seasar.cubby.action.ActionResult;
 import org.seasar.cubby.controller.ActionProcessor;
 import org.seasar.cubby.controller.RequestParser;
 import org.seasar.cubby.controller.RequestParserSelector;
 import org.seasar.cubby.controller.RoutingsDispatcher;
 import org.seasar.cubby.exception.ActionRuntimeException;
import org.seasar.cubby.filter.CubbyHttpServletRequestWrapper;
 import org.seasar.cubby.routing.Routing;
 import org.seasar.cubby.util.CubbyUtils;
 import org.seasar.framework.container.SingletonS2Container;
 import org.seasar.framework.log.Logger;
 
 /**
  * リクエストのパスを元にアクションメソッドを決定して実行するクラスの実装です。
  * 
  * @author baba
  * @since 1.0.0
  */
 public class ActionProcessorImpl implements ActionProcessor {
 
 	/** ロガー。 */
 	private static final Logger logger = Logger
 			.getLogger(ActionProcessorImpl.class);
 
 	/** 空の引数。 */
 	private static final Object[] EMPTY_ARGS = new Object[0];
 
 	/** リクエストパラメータに応じたルーティングを割り当てるためのクラス。 */
 	private RoutingsDispatcher routingsDispatcher;
 
 	/** リクエスト解析器。 */
 	private RequestParserSelector requestParserSelector;
 
 	/**
 	 * リクエストパラメータに応じたルーティングを割り当てるためのクラスを設定します。
 	 * 
 	 * @param routingsDispatcher
 	 *            リクエストパラメータに応じたルーティングを割り当てるためのクラス
 	 */
 	public void setRoutingsDispatcher(
 			final RoutingsDispatcher routingsDispatcher) {
 		this.routingsDispatcher = routingsDispatcher;
 	}
 
 	/**
 	 * リクエスト解析器セレクタを設定します。
 	 * 
 	 * @param requestParserSelector
 	 *            リクエスト解析器
 	 */
 	public void setRequestParserSelector(
 			final RequestParserSelector requestParserSelector) {
 		this.requestParserSelector = requestParserSelector;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public ActionResult process(final HttpServletRequest request,
 			final HttpServletResponse response) throws Exception {
 		final Map<String, Routing> routings = CubbyUtils.getAttribute(request,
 				ATTR_ROUTINGS);
 		if (routings == null) {
 			return null;
 		}
 
 		request.removeAttribute(ATTR_ROUTINGS);
 		final Map<String, Object[]> parameterMap = parseRequest(request);
 		request.setAttribute(ATTR_PARAMS, parameterMap);
 
 		final Routing routing = routingsDispatcher.dispatch(routings,
 				parameterMap);
 		if (routing == null) {
 			return null;
 		}
 
 		final Class<? extends Action> actionClass = routing.getActionClass();
 		final Method method = routing.getMethod();
 		if (logger.isDebugEnabled()) {
 			logger.log("DCUB0004", new Object[] { request.getRequestURI() });
 			logger.log("DCUB0005", new Object[] { method });
 		}
 		final Action action = SingletonS2Container.getComponent(actionClass);
 		request.setAttribute(ATTR_ACTION, action);
 		final ActionResult result = invoke(action, method);
 		if (result == null) {
 			throw new ActionRuntimeException("ECUB0101",
 					new Object[] { method });
 		}
		final HttpServletRequest wrappedRequest = new CubbyHttpServletRequestWrapper(
				request);
		result.execute(action, actionClass, method, wrappedRequest, response);
 		return result;
 	}
 
 	/**
 	 * 指定されたアクションのメソッドを実行します。
 	 * 
 	 * @param action
 	 *            アクション
 	 * @param method
 	 *            アクションメソッド
 	 * @return アクションメソッドの実行結果
 	 */
 	private ActionResult invoke(final Action action, final Method method)
 			throws Exception {
 		try {
 			final ActionResult result = (ActionResult) method.invoke(action,
 					EMPTY_ARGS);
 			return result;
 		} catch (final InvocationTargetException ex) {
 			logger.log(ex);
 			final Throwable target = ex.getTargetException();
 			if (target instanceof Error) {
 				throw (Error) target;
 			} else if (target instanceof RuntimeException) {
 				throw (RuntimeException) target;
 			} else {
 				throw (Exception) target;
 			}
 		}
 	}
 
 	/**
 	 * リクエストをパースしてパラメータを取り出し、{@link Map}に変換して返します。
 	 * 
 	 * @param request
 	 *            リクエスト
 	 * @return リクエストパラメータの{@link Map}
 	 */
 	private Map<String, Object[]> parseRequest(final HttpServletRequest request) {
 		final RequestParser requestParser = requestParserSelector
 				.select(request);
 		if (requestParser == null) {
 			throw new NullPointerException("requestParser");
 		}
 		if (logger.isDebugEnabled()) {
 			logger.log("DCUB0016", new Object[] { requestParser });
 		}
 		final Map<String, Object[]> parameterMap = requestParser
 				.getParameterMap(request);
 		return parameterMap;
 	}
 
 }
