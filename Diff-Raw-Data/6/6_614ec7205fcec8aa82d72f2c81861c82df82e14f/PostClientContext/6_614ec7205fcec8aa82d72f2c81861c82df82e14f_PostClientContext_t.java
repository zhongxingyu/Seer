 package net.unit8.sastruts.easyapi.client;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.UUID;
 
 import net.unit8.sastruts.easyapi.EasyApiException;
 import net.unit8.sastruts.easyapi.XStreamFactory;
 import net.unit8.sastruts.easyapi.client.handler.MessageHandler;
 import net.unit8.sastruts.easyapi.dto.RequestDto;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.util.EntityUtils;
 import org.seasar.framework.beans.BeanDesc;
 import org.seasar.framework.beans.PropertyDesc;
 import org.seasar.framework.beans.factory.BeanDescFactory;
 import org.seasar.framework.exception.IORuntimeException;
 import org.seasar.framework.log.Logger;
 import org.seasar.framework.util.StringConversionUtil;
 import org.seasar.framework.util.tiger.CollectionsUtil;
 
 import com.thoughtworks.xstream.annotations.XStreamAlias;
 import com.thoughtworks.xstream.annotations.XStreamOmitField;
 
 public class PostClientContext<T> extends ClientContext<T> {
 	private static final Logger logger = Logger.getLogger(ClientContext.class);
 
 	private T data;
 
 	public PostClientContext() {
 		params = new ArrayList<NameValuePair>();
 	}
 
 	public PostClientContext<T> to(String name) {
 		this.name = name;
 		return this;
 	}
 
 	public int execute() throws EasyApiException {
 		EasyApiSetting setting = settingProvider.get(name);
 		HttpPost method = new HttpPost(buildUri(setting));
 		processRequestHeaders(method);
 		if (data != null) {
 			XStreamFactory.setOmitFields(data);
 			HttpEntity entity = null;
 			switch (setting.getRequestType()) {
 			case URL_ENCODE:
 				List<NameValuePair> parameters = CollectionsUtil.newArrayList();
 				BeanDesc beanDesc = BeanDescFactory
 						.getBeanDesc(data.getClass());
 				for (int i = 0; i < beanDesc.getPropertyDescSize(); i++) {
 					PropertyDesc propertyDesc = beanDesc.getPropertyDesc(i);
 					if (propertyDesc.getField().getAnnotation(
 							XStreamOmitField.class) == null) {
 						Object value = propertyDesc.getValue(data);
 						if (value != null) {
 							XStreamAlias aliasAnno = propertyDesc.getField()
 									.getAnnotation(XStreamAlias.class);
 							parameters.add(new BasicNameValuePair(
 									aliasAnno == null ? propertyDesc
 											.getPropertyName() : aliasAnno
 											.value(), StringConversionUtil
 											.toString(value)));
 						}
 					}
 				}
 				try {
 					entity = new UrlEncodedFormEntity(parameters,
 							setting.getEncoding());
 				} catch (UnsupportedEncodingException e) {
 					throw new IORuntimeException(e);
 				}
 				break;
 			default:
 				RequestDto requestDto = new RequestDto();
 				requestDto.body = data;
 				String xml = XStreamFactory.getInstance().toXML(requestDto);
 				try {
 					entity = new StringEntity(xml, setting.getEncoding());
 				} catch (UnsupportedEncodingException e) {
 					throw new IORuntimeException(e);
 				}
 				break;
 			}
 			method.setEntity(entity);
 		}
 
 		String transactionId = UUID.randomUUID().toString();
 		if (transactionIdName != null)
 			method.addHeader(transactionIdName, transactionId);
 		InputStream in = null;
 		HttpEntity entity = null;
		boolean execStatus = false;
 		try {
 			logger.log("ISEA0001", new Object[] { transactionId, name });
 			if (settingProvider.useMock) {
 				in = getMockResponseStream();
 			} else {
 				HttpResponse response = client.execute(method);
 				logger.error(response.getStatusLine());
 				entity = response.getEntity();
 				in = entity.getContent();
 			}
 			Class<?> resultSetDtoClass = XStreamFactory.getResutSetClass(data);
 			if (resultSetDtoClass != null) {
 				XStreamFactory.setBodyDto(resultSetDtoClass);
 			} else {
 				XStreamFactory.setBodyDto(data.getClass());
 			}
 			MessageHandler<T> handler = handlerProvider.get(setting.getResponseType());
 			handler.handle(in, data, setting);
			execStatus = true;
 		} catch (EasyApiException e) {
 			e.setTransactionId(transactionId);
 			throw e;
 		} catch (IOException e) {
 			throw new IORuntimeException(e);
 		} finally {
			logger.log("ISEA0002", new Object[] { transactionId, name, execStatus ? "end  " : "abend"});
 			IOUtils.closeQuietly(in);
 			EntityUtils.consumeQuietly(entity);
 		}
 		return 1;
 	}
 
 	public Object getDto() {
 		return this.data;
 	}
 
 	public void setDto(T data) {
 		this.data = data;
 	}
 
 	public PostClientContext<T> addHeader(String name, String value) {
 		return (PostClientContext<T>) super.addHeader(name, value);
 	}
 
 }
