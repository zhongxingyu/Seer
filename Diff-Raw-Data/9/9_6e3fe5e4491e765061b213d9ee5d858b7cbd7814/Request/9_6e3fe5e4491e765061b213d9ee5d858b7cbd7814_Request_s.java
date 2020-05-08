 package http;
 
 import http.attribute.AbstractAttributeCollectionMap;
 import http.attribute.AttributeHashSetMap;
 import http.attribute.AttributeMap;
 import http.header.Header;
 import http.parameter.Parameter;
 
 import java.net.URL;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Set;
 
 import static http.Cookie.COOKIE;
 import static http.util.Asserts.assertNotNull;
 import static http.util.Checks.isNotEmpty;
 import static http.util.Checks.isNotNull;
 import static http.util.URIs.quietUrl;
 
 /**
  * Represents an {@code HTTP} request and can be populated with all the standard request components.
  *
  * @author Karl Bennett
  */
 public class Request<T> extends Message<T> {
 
     /**
      * Return a new {@link URL} that consists of the supplied {@code URL} minus it's query string.
      *
      * @param url the {@code URL} to copy.
      * @return a copy of the supplied {@code URL} minus it's query string.
      */
     private static URL urlMinusQuery(String url) {
 
         String[] parts = url.split("\\?");
 
         return quietUrl(parts[0]);
     }
 
 
     private final URL url;
     private AbstractAttributeCollectionMap<Parameter<String>, Set<Parameter<String>>> parameters;
 
     /**
      * Create a new {@code Request} that will be sent to the {@code HTTP} server at the supplied {@link URL}.
      *
      * @param url a {@code java.lang.String} containing the {@code URL} for the {@code HTTP} server.
      */
     public Request(String url) {
 
         this(quietUrl(url));
     }
 
     /**
      * Create a new {@code Request} that will be sent to the {@code HTTP} server at the supplied {@link URL}. It
      * will aso contain parameters supplied.
      *
      * @param url        a {@code java.lang.String} containing the {@code URL} for the {@code HTTP} server.
      * @param parameters any parameters that should be sent in the HTTP request.
      */
     public Request(String url, Collection<Parameter<String>> parameters) {
 
         this(quietUrl(url), parameters);
     }
 
     /**
      * Create a new {@code Request} that will be sent to the {@code HTTP} server at the supplied {@link URL}. It
      * will aso contain the headers and parameters supplied.
      *
      * @param url        a {@code java.lang.String} containing the {@code URL} for the {@code HTTP} server.
      * @param headers    any headers that should be sent in the HTTP request.
      * @param parameters any parameters that should be sent in the HTTP request.
      */
     public Request(String url, Collection<Header> headers, Collection<Parameter<String>> parameters) {
 
         this(quietUrl(url), headers, parameters);
     }
 
     /**
      * Create a new {@code Request} that will be sent to the {@code HTTP} server at the supplied {@link URL}. It
      * will aso contain the headers, parameters, and cookies supplied.
      *
      * @param url        a {@code java.lang.String} containing the {@code URL} for the {@code HTTP} server.
      * @param headers    any headers that should be sent in the HTTP request.
      * @param cookies    any cookies that should be sent in the HTTP request.
      * @param parameters any parameters that should be sent in the HTTP request.
      */
     public Request(String url, Collection<Header> headers, Collection<Cookie> cookies,
                    Collection<Parameter<String>> parameters) {
 
         this(quietUrl(url), headers, cookies, parameters);
     }
 
     /**
      * Create a new {@code Request} that will be sent to the {@code HTTP} server at the supplied {@link URL}.
      *
      * @param url a {@code java.lang.String} containing the {@code URL} for the {@code HTTP} server.
      */
     public Request(URL url) {
 
         this(url, Collections.<Parameter<String>>emptySet());
     }
 
 
     /**
      * Create a new {@code Request} that will be sent to the {@code HTTP} server at the supplied {@link URL}. It
      * will aso contain parameters supplied.
      *
      * @param url        a {@code java.net.URL} representing the URL for the {@code HTTP} server.
      * @param parameters any parameters that should be sent in the HTTP request.
      */
     public Request(URL url, Collection<Parameter<String>> parameters) {
 
         this(url, Collections.<Header>emptySet(), parameters);
     }
 
     /**
      * Create a new {@code Request} that will be sent to the {@code HTTP} server at the supplied {@link URL}. It
      * will aso contain the headers and parameters supplied.
      *
      * @param url        a {@code java.net.URL} representing the URL for the {@code HTTP} server.
      * @param headers    any headers that should be sent in the HTTP request.
      * @param parameters any parameters that should be sent in the HTTP request.
      */
     public Request(URL url, Collection<Header> headers, Collection<Parameter<String>> parameters) {
 
         this(url, headers, Collections.<Cookie>emptySet(), parameters);
     }
 
     /**
      * Create a new {@code Request} that will be sent to the {@code HTTP} server at the supplied {@link URL}. It
      * will aso contain the headers, parameters, and cookies supplied.
      *
      * @param url        a {@code java.net.URL} representing the URL for the {@code HTTP} server.
      * @param headers    any headers that should be sent in the HTTP request.
      * @param cookies    any cookies that should be sent in the HTTP request.
      * @param parameters any parameters that should be sent in the HTTP request.
      */
     public Request(URL url, Collection<Header> headers, Collection<Cookie> cookies,
                    Collection<Parameter<String>> parameters) {
        super(COOKIE, new AttributeHashSetMap<Header>(headers), new AttributeMap<Cookie>(cookies), null);
 
         assertNotNull("parameters", parameters);
 
         this.url = urlMinusQuery(url.toString());
 
         this.parameters = new AttributeHashSetMap<Parameter<String>>(parameters);
         this.parameters.addAll(Parameter.parse(url.getQuery()));
     }
 
 
     /**
      * @return the url for this request.
      */
     public URL getUrl() {
 
         // If we have some paramters in the request we should add them to the query string of the URL.
         if (isNotEmpty(parameters)) {
 
             return quietUrl(url.toString() + '?' + Parameter.toString(getParameters()));
         }
 
         return url;
     }
 
     /**
      * Get all the parameters set for the current {@code Message}.
      *
      * @return the message parameters.
      */
     public Set<Parameter<String>> getParameters() {
 
         return getAllValues(parameters);
     }
 
     /**
      * Get the {@link Parameter} with the supplied name. If the parameter does not exist this method will return null.
      *
      * @param name the name of the parameter to retrieve.
      * @return the requested parameter if it exists otherwise null.
      */
     public Set<Parameter<String>> getParameters(String name) {
 
         return parameters.get(name);
     }
 
     /**
      * Set the parameters for this {@code Message}, overwriting any that might have previously been set.
      *
      * @param parameters the new parameters for the message.
      */
     public void setParameters(Collection<Parameter<String>> parameters) {
 
         this.parameters.clear();
 
         this.parameters.addAll(parameters);
     }
 
     /**
      * Add a parameter to the {@code Message} made up of the supplied name and value. If a parameter with the supplied
      * name already exists then the supplied value will be added to the existing parameters values.
      *
      * @param name  the name of the new parameter.
      * @param value the value for the new parameter.
      */
     public void addParameter(String name, String value) {
 
         addParameter(new Parameter<String>(name, value));
     }
 
     /**
      * Add a {@link Parameter} to the {@code Message} appending it to any added previously. If a parameter with the a
      * matching name already exists then the new parameters value will be added to the existing parameters values.
      *
      * @param parameter the new parameter to add to the message.
      */
     public void addParameter(Parameter<String> parameter) {
 
         if (isNotNull(parameter)) this.parameters.add(parameter);
     }
 
     /**
      * Add the supplied {@link Parameter}s to the request.
      *
      * @param parameters the {@link Parameter}s to add.
      */
     public void addParameters(Collection<Parameter<String>> parameters) {
 
         this.parameters.addAll(parameters);
     }
 
     /**
      * Remove the supplied value from the {@link Parameter} with the supplied name. This will remove all the value from
      * the {@code Parameter} entry and then if no values are left it will remove the {@code Parameter} entry completely.
      *
      * @param name  the name of the {@code Parameter} to remove the value from.
      * @param value the value to remove.
      * @return a {@code Parameter} containing the name and value that were removed if a value was removed, otherwise
      *         {@code null}.
      */
     public Parameter<String> removeParameter(String name, String value) {
 
         return removeParameter(new Parameter<String>(name, value));
     }
 
     /**
      * Remove the supplied {@link Parameter} from the request. This will remove all the values in the supplied
      * {@code Parameter} and then if no values are left it will remove the {@code Parameter} entry completely.
      *
      * @param parameter the {@code Parameter} to remove.
      * @return the {@code Parameter} that was removed if one was removed, otherwise {@code null}.
      */
     public Parameter<String> removeParameter(Parameter<String> parameter) {
 
         return remove(parameters, parameter);
     }
 
     /**
      * Remove the supplied {@link Parameter}s.
      *
      * @param parameters the {@code Parameter}s to remove.
      * @return the {@code Parameter}s that were removed.
      */
     public Collection<Parameter<String>> removeParameters(Collection<Parameter<String>> parameters) {
 
         return removeAll(this.parameters, parameters);
     }
 }
