 package net.unit8.sastruts.easyapi.client;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import net.unit8.sastruts.easyapi.EasyApiException;
 import net.unit8.sastruts.easyapi.EasyApiSystemException;
 import net.unit8.sastruts.easyapi.XStreamFactory;
 import net.unit8.sastruts.easyapi.dto.ResponseDto;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.math.RandomUtils;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.params.SyncBasicHttpParams;
 import org.seasar.framework.container.SingletonS2Container;
 import org.seasar.framework.exception.IORuntimeException;
 import org.seasar.framework.util.ResourceUtil;
 
 public class GetClientContext<T> extends ClientContext<T> {
 	private EasyApiSettingProvider provider;
 
 	private HttpClient client;
 	private String name;
 	private Class<T> dtoClass;
 
 	public GetClientContext(HttpClient client, Class<T> dtoClass) {
 		this.dtoClass = dtoClass;
 		this.client = client;
 		provider = SingletonS2Container.getComponent(EasyApiSettingProvider.class);
 		params = new SyncBasicHttpParams();
 	}
 
 	public GetClientContext<T> from(String name) throws EasyApiException {
 		this.name = name;
 		return this;
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<T> getResultList() throws EasyApiException {
 		T obj = executeQuery();
 		if (Collection.class.isAssignableFrom(obj.getClass())) {
 			return new ArrayList<T>((Collection<T>) obj);
 		} else {
 			List<T> results = new ArrayList<T>();
 			results.add(obj);
 			return results;
 		}
 	}
 
 	public T getSingleResult() throws EasyApiException {
 		return executeQuery();
 	}
 
 	@SuppressWarnings("unchecked")
 	protected T executeQuery() throws EasyApiException {
 		if (provider.useMock) return processMock();
 		EasyApiSetting setting = provider.get(name);
 		HttpGet method = new HttpGet(setting.getHost() + processDynamicPath(setting.getPath()));
 		method.setParams(params);
 		try {
 			HttpResponse response = client.execute(method);
 			HttpEntity entity = response.getEntity();
 			InputStream in = entity.getContent();
 			XStreamFactory.setBodyDto(dtoClass);
 			ResponseDto responseDto = (ResponseDto)XStreamFactory.getInstance().fromXML(in);
 			processHeader(responseDto);
 
 			if (dtoClass.isInstance(responseDto.body)) {
 				return (T)responseDto.body;
 			} else {
 				throw new EasyApiSystemException("mismatch DTO type.");
 			}
 		} catch (IOException e) {
 			throw new IORuntimeException(e);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private T processMock() throws EasyApiException {
 		File dir = ResourceUtil.getResourceAsFile("mock/" + name);
 		if (!dir.exists()) {
 			return null;
 		}
 		Collection<File> dataFiles = FileUtils.listFiles(dir, new String[]{"xml"}, false);
 		if (dataFiles.isEmpty())
 			return null;
 
 		File dataFile = dataFiles.toArray(new File[0])[RandomUtils.nextInt(dataFiles.size())];
		XStreamFactory.setBodyDto(dtoClass);
 		ResponseDto responseDto = (ResponseDto)XStreamFactory.getInstance().fromXML(dataFile);
 		processHeader(responseDto);
 		return (T)responseDto.body;
 	}
 
 }
