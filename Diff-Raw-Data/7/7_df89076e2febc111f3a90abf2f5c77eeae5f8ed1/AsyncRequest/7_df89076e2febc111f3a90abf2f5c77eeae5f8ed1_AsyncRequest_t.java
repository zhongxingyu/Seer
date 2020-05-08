 package com.luciofm.libs.vaptvupt;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.security.InvalidParameterException;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.os.AsyncTask;
 import android.os.Build;
 import android.text.TextUtils;
 import android.util.Log;
 
 import com.google.gson.Gson;
 
 public class AsyncRequest<T> extends AsyncTask<Request, Void, AsyncResponse<T>> {
 
 	private AsyncRequestListener<T> listener;
 	private Object data = null;
 	private Type type;
 
 	public AsyncRequest(Context context) {
 		this(context, null);
 	}
 
 	public AsyncRequest(Context context,
 			AsyncRequestListener<T> listener) {
 		this(context, listener, null);
 	}
 
 	public AsyncRequest(Context context,
 			AsyncRequestListener<T> listener, Object data) {
 		this.listener = listener;
 		this.data = data;
 		this.type = getSuperclassTypeParameter(getClass());
 
 		disableConnectionReuseIfNecessary();
 	}
 
 	static Type getSuperclassTypeParameter(Class<?> subclass) {
 		Type superclass = subclass.getGenericSuperclass();
 		if (superclass instanceof Class) {
 			throw new RuntimeException("Missing type parameter.");
 		}
 		ParameterizedType parameterized = (ParameterizedType) superclass;
 		return parameterized.getActualTypeArguments()[0];
 	}
 
 	@Override
 	protected AsyncResponse<T> doInBackground(Request... params) {
 		if (params.length == 0)
 			return new AsyncResponse<T>(new IndexOutOfBoundsException());
 
 		Request req = params[0];
 		AsyncResponse<T> response = null;
 		response = checkRequest(req);
 		if (response != null)
 			return response;
 
 		switch (req.getMethod()) {
 		case Request.GET:
 			response = doGet(req);
 			break;
 		case Request.POST:
 			response = doPost(req);
 		case Request.DELETE:
 			response = doDelete(req);
 		default:
 			break;
 		}
 
 		return response;
 	}
 
 	@Override
 	protected void onPostExecute(AsyncResponse<T> result) {
 		if (listener != null) {
 			if (result.getException() != null)
 				listener.onError(result.getException(), data);
 			else
 				listener.onSuccess(result.getResponse(), data);
 		}
 	}
 
 	private AsyncResponse<T> checkRequest(Request req) {
 		if (TextUtils.isEmpty(req.getUrl()))
 			return new AsyncResponse<T>(new InvalidParameterException("Invalid url"));
 		if (req.getMethod() > Request.MAX_METHOD)
 			return new AsyncResponse<T>(new InvalidParameterException("Method must be GET, POST or DELETE"));
 		return null;
 	}
 
 	private AsyncResponse<T> doGet(Request req) {
 		URL url;
 		HttpURLConnection connection;
 		AsyncResponse<T> response = null;
 
 		try {
 			url = new URL(req.getUrl());
 			connection = (HttpURLConnection) url.openConnection();
 			addHeaders(connection, req.getHeaders());
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 			return new AsyncResponse<T>(e);
 		} catch (IOException e) {
 			e.printStackTrace();
 			return new AsyncResponse<T>(e);
 		}
 
 		InputStream in = null;
 		try {
 			int responseCode = connection.getResponseCode();
 			if (responseCode != HttpURLConnection.HTTP_OK) {
 
 			}
 			Log.d("receitas", "Content lenght: " + connection.getContentLength());
 			in = connection.getInputStream();
 			response = readData(req, in);
 		} catch (IOException e) {
 			e.printStackTrace();
 			return new AsyncResponse<T>(e);
 		} catch (JSONException e) {
 			e.printStackTrace();
 			return new AsyncResponse<T>(e);
 		} finally {
 			if (in != null)
 				try {
 					in.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 		}
 
 		return response;
 	}
 
 	private AsyncResponse<T> readData(Request req, InputStream in) throws JSONException, IOException {
 		Gson gson = new Gson();
 		AsyncResponse<T> response;
		if (TextUtils.isEmpty(req.getResponseField())) {
			T data = gson.fromJson(new InputStreamReader(in), type);
			response = new AsyncResponse<T>(data);
		} else {
 			String str = new String(IOUtils.inputStreamToByteArray(in));
 			Log.d("receitas", "String: " + str.length());
 			JSONObject json = new JSONObject(str);
 			Object val = json.get(req.getResponseField());
 			String value = null;
 			if (val instanceof JSONArray) {
 				JSONArray arr = (JSONArray) val;
 				value = arr.toString();
 			} else if (val instanceof JSONObject) {
 				JSONObject obj = (JSONObject) val;
 				value = obj.toString();
 			} else if (val instanceof String)
 				value = (String) val;
 
 			T data = gson.fromJson(value, type);
 			response = new AsyncResponse<T>(data);
 		}
 		return response;
 	}
 
 	private AsyncResponse<T> doPost(Request req) {
 		return null;
 	}
 
 	private AsyncResponse<T> doDelete(Request req) {
 		return null;
 	}
 
 	@SuppressWarnings("rawtypes")
 	private void addHeaders(HttpURLConnection connection,
 			Map<String, String> headers) {
 		if (headers == null)
 			return;
 		Iterator it = headers.entrySet().iterator();
 		while (it.hasNext()) {
 			Map.Entry pairs = (Map.Entry) it.next();
 			connection.addRequestProperty((String)pairs.getKey(), (String)pairs.getValue());
 		}
 	}
 
 	@SuppressWarnings("deprecation")
 	private void disableConnectionReuseIfNecessary() {
 		// HTTP connection reuse which was buggy pre-froyo
 		if (Integer.parseInt(Build.VERSION.SDK) < Build.VERSION_CODES.FROYO) {
 			System.setProperty("http.keepAlive", "false");
 		}
 	}
 
 	public interface AsyncRequestListener<T> {
 		public void onError(Throwable exception, Object data);
 		public void onSuccess(T response, Object data);
 	}
 }
