 /*
  * Copyright (C) 2011 Emmanuel Tourdot
  *
  * This library is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Lesser General Public License as published by the Free
  * Software Foundation; either version 2.1 of the License, or (at your option)
  * any later version.
  *
  * This library is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this library; if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  *
  * $Id$
  */
 package org.trancecode.xproc.step;
 
 import com.google.common.base.Strings;
 import com.google.common.io.Closeables;
 
 import java.io.ByteArrayOutputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.nio.charset.Charset;
 import java.nio.charset.IllegalCharsetNameException;
 import java.util.Map;
 
 import javax.mail.internet.ContentType;
 import javax.mail.internet.ParseException;
 
 import net.sf.saxon.s9api.Axis;
 import net.sf.saxon.s9api.QName;
 import net.sf.saxon.s9api.Serializer;
 import net.sf.saxon.s9api.XdmItem;
 import net.sf.saxon.s9api.XdmNode;
 import net.sf.saxon.s9api.XdmNodeKind;
 import org.apache.commons.lang.StringEscapeUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.http.Header;
 import org.apache.http.HeaderElement;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpHost;
 import org.apache.http.NameValuePair;
 import org.apache.http.auth.AuthScope;
 import org.apache.http.auth.UsernamePasswordCredentials;
 import org.apache.http.client.AuthCache;
 import org.apache.http.client.CredentialsProvider;
 import org.apache.http.client.methods.HttpDelete;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpHead;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpPut;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.client.params.AuthPolicy;
 import org.apache.http.client.protocol.ClientContext;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.entity.mime.FormBodyPart;
 import org.apache.http.entity.mime.HttpMultipartMode;
 import org.apache.http.entity.mime.MIME;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.ContentBody;
 import org.apache.http.entity.mime.content.StringBody;
 import org.apache.http.impl.auth.BasicScheme;
 import org.apache.http.impl.auth.DigestScheme;
 import org.apache.http.impl.client.BasicAuthCache;
 import org.apache.http.impl.client.BasicCredentialsProvider;
 import org.apache.http.message.BasicHeader;
 import org.apache.http.message.HeaderGroup;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HttpContext;
 import org.trancecode.logging.Logger;
 import org.trancecode.xml.saxon.SaxonAxis;
 import org.trancecode.xml.saxon.SaxonLocation;
 import org.trancecode.xproc.PipelineException;
 import org.trancecode.xproc.XProcExceptions;
 import org.trancecode.xproc.XProcXmlModel;
 
 /**
  * User: Emmanuel Tourdot Date: 18 feb. 2011 Time: 06:35:39
  */
 class RequestParser
 {
     private static final Logger LOG = Logger.getLogger(RequestParser.class);
     private static final String DEFAULT_MULTIPART_TYPE = "multipart/mixed";
 
     private final Map<QName, Object> serializationOptions;
     private final XProcHttpRequest request = new XProcHttpRequest();
 
     public RequestParser(final Map<QName, Object> serializationOptions)
     {
         this.serializationOptions = serializationOptions;
     }
 
     public XProcHttpRequest parseRequest(final XdmNode requestNode)
     {
         final String method = requestNode.getAttributeValue(XProcXmlModel.Attributes.METHOD);
         if (Strings.isNullOrEmpty(method))
         {
             throw XProcExceptions.xc0006(requestNode);
         }
         request.setHeaders(parseHeaders(requestNode));
         request.setEntity(parseMultipart(requestNode));
         if (!request.hasEntity())
         {
             request.setEntity(parseBody(requestNode));
         }
         if (request.hasEntity())
         {
             checkCoherenceHeaders(request.getHeaders(), request.getEntity(), requestNode);
             if (!(StringUtils.equalsIgnoreCase(HttpPut.METHOD_NAME, method) || StringUtils.equalsIgnoreCase(
                     HttpPost.METHOD_NAME, method)))
             {
                 throw XProcExceptions.xc0005(requestNode);
             }
         }
 
         final boolean status = Boolean.valueOf(requestNode.getAttributeValue(XProcXmlModel.Attributes.STATUS_ONLY));
         final boolean detailed = Boolean.valueOf(requestNode.getAttributeValue(XProcXmlModel.Attributes.DETAILED));
         if (status && !detailed)
         {
             throw XProcExceptions.xc0004(requestNode);
         }
         request.setDetailled(detailed);
         request.setStatusOnly(status);
 
         final String href = requestNode.getAttributeValue(XProcXmlModel.Attributes.HREF);
         final URI hrefUri = requestNode.getBaseURI().resolve(href);
         if (hrefUri.getPort() != -1)
         {
             request.setHttpHost(new HttpHost(hrefUri.getHost(), hrefUri.getPort(), hrefUri.getScheme()));
         }
         else
         {
             request.setHttpHost(new HttpHost(hrefUri.getHost()));
         }
 
         final CredentialsProvider credentialsProvider = parseAuthentication(requestNode);
         request.setCredentials(credentialsProvider);
         request.setHttpRequest(constructMethod(method, hrefUri));
         request.setOverrideContentType(requestNode.getAttributeValue(XProcXmlModel.Attributes.OVERRIDE_CONTENT_TYPE));
 
         return request;
     }
 
     private void checkCoherenceHeaders(final HeaderGroup headers, final HttpEntity entity, final XdmNode requestNode)
     {
         final Header ctHeaders = headers.getFirstHeader("content-type");
         final Header ctEntity = entity.getContentType();
         if (ctHeaders != null && ctEntity != null)
         {
             final HeaderElement elmHeaderCt = ctHeaders.getElements()[0];
             final HeaderElement elmEntityCt = ctEntity.getElements()[0];
             if (!StringUtils.equalsIgnoreCase(elmHeaderCt.getName(), elmEntityCt.getName()))
             {
                 throw XProcExceptions.xc0020(requestNode);
             }
             for (final NameValuePair pairEntity : elmEntityCt.getParameters())
             {
                 final NameValuePair pairHeader = elmHeaderCt.getParameterByName(pairEntity.getName());
                 if (pairHeader != null)
                 {
                     if (!StringUtils.equalsIgnoreCase(pairHeader.getValue(), pairEntity.getValue()))
                     {
                         throw XProcExceptions.xc0020(requestNode);
                     }
                 }
             }
         }
 
     }
 
     private HeaderGroup parseHeaders(final XdmNode requestNode)
     {
         final HeaderGroup group = new HeaderGroup();
         final Iterable<XdmNode> children = SaxonAxis.childElements(requestNode, XProcXmlModel.Elements.HEADER);
         for (final XdmNode child : children)
         {
             final String nameHeader = child.getAttributeValue(XProcXmlModel.Attributes.NAME);
             final String valueHeader = child.getAttributeValue(XProcXmlModel.Attributes.VALUE);
             if (!Strings.isNullOrEmpty(nameHeader) && !Strings.isNullOrEmpty(valueHeader))
             {
                 group.addHeader(new BasicHeader(nameHeader, valueHeader));
             }
         }
         return group;
     }
 
     private MultipartEntity parseMultipart(final XdmNode requestNode)
     {
         final XdmNode child = SaxonAxis.childElement(requestNode, XProcXmlModel.Elements.MULTIPART);
         if (child != null)
         {
             final String contentTypeAtt = child.getAttributeValue(XProcXmlModel.Attributes.CONTENT_TYPE);
             final String contentType = Strings.isNullOrEmpty(contentTypeAtt) ? DEFAULT_MULTIPART_TYPE : contentTypeAtt;
             final String boundary = child.getAttributeValue(XProcXmlModel.Attributes.BOUNDARY);
             if (StringUtils.startsWith(boundary, "--"))
             {
                 throw XProcExceptions.xc0002(requestNode);
             }
             final MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.STRICT, boundary,
                     Charset.forName("UTF-8"))
             {
                 @Override
                 protected String generateContentType(final String boundary, final Charset charset)
                 {
                     final StringBuilder buffer = new StringBuilder();
                     buffer.append(contentType).append("; boundary=").append(boundary);
                     return buffer.toString();
                 }
             };
             final Iterable<XdmNode> bodies = SaxonAxis.childElements(child, XProcXmlModel.Elements.BODY);
             for (final XdmNode body : bodies)
             {
                 final FormBodyPart contentBody = getContentBody(body);
                 if (contentBody != null)
                 {
                     reqEntity.addPart(contentBody);
                 }
             }
 
             return reqEntity;
         }
         return null;
     }
 
     private String getContentString(final XdmNode node, final ContentType contentType, final String encoding)
     {
        if (!StringUtils.isEmpty(encoding) && !StringUtils.equalsIgnoreCase(encoding, Steps.ENCODING_BASE64))
        {
            throw XProcExceptions.xc0052(SaxonLocation.of(node));
        }
         final StringBuilder contentBuilder = new StringBuilder();
         if (!StringUtils.containsIgnoreCase(contentType.getSubType(), "xml")
                 || StringUtils.equalsIgnoreCase(encoding, Steps.ENCODING_BASE64))
         {
             final Iterable<XdmItem> children = SaxonAxis.axis(node, Axis.CHILD);
             for (final XdmItem aNode : children)
             {
                 if (!XdmNodeKind.TEXT.equals(((XdmNode) aNode).getNodeKind()))
                 {
                     throw XProcExceptions.xc0028(SaxonLocation.of(node));
                 }
                 else
                 {
                     contentBuilder.append(StringEscapeUtils.unescapeHtml(aNode.toString()));
                 }
             }
         }
         else
         {
             final Iterable<XdmItem> children = SaxonAxis.axis(node, Axis.CHILD);
             boolean oneElement = false;
             for (final XdmItem aNode : children)
             {
                 final XdmNodeKind kind = ((XdmNode) aNode).getNodeKind();
                 if (XdmNodeKind.TEXT.equals(kind) && !StringUtils.isEmpty(aNode.getStringValue().trim()))
                 {
                     throw XProcExceptions.xc0022(node);
                 }
                 else if (XdmNodeKind.ELEMENT.equals(kind))
                 {
                     if (oneElement)
                     {
                         throw XProcExceptions.xc0022(node);
                     }
                     else
                     {
                         oneElement = true;
                     }
                 }
             }
         }
         if (StringUtils.equalsIgnoreCase("xml", contentType.getSubType()))
         {
             final ByteArrayOutputStream targetOutputStream = new ByteArrayOutputStream();
             final Serializer serializer = Steps.getSerializer(targetOutputStream, serializationOptions);
             serializer.setOutputProperty(Serializer.Property.MEDIA_TYPE, contentType.toString());
             try
             {
                 node.getProcessor().writeXdmValue(SaxonAxis.childElement(node), serializer);
             }
             catch (final Exception e)
             {
                 throw new PipelineException("Error while trying to write document", e);
             }
             finally
             {
                 Closeables.closeQuietly(targetOutputStream);
             }
             contentBuilder.append(targetOutputStream.toString());
         }
         final String id = node.getAttributeValue(XProcXmlModel.Attributes.ID);
         verifyHeader(id, "Content-ID", node);
         final String description = node.getAttributeValue(XProcXmlModel.Attributes.DESCRIPTION);
         verifyHeader(description, "Content-Description", node);
         final String disposition = node.getAttributeValue(XProcXmlModel.Attributes.DISPOSITION);
         verifyHeader(disposition, "Content-Disposition", node);
         return contentBuilder.toString();
     }
 
     private Charset getCharset(final String charset, final String defaultCharset)
     {
         return charset != null ? Charset.forName(charset) : Charset.forName("utf-8");
     }
 
     private FormBodyPart getContentBody(final XdmNode node)
     {
         final String contentTypeAtt = node.getAttributeValue(XProcXmlModel.Attributes.CONTENT_TYPE);
         final String encoding = node.getAttributeValue(XProcXmlModel.Attributes.ENCODING);
         final ContentType contentType = Steps.getContentType(contentTypeAtt, node);
         final String contentString = getContentString(node, contentType, encoding);
         final StringBody body;
         try
         {
             body = new StringBody(contentString, contentType.toString(), getCharset(
                     contentType.getParameter("charset"), "utf-8"));
         }
         catch (final UnsupportedEncodingException e)
         {
             throw XProcExceptions.xc0020(node);
         }
 
         final String id = node.getAttributeValue(XProcXmlModel.Attributes.ID);
         final String description = node.getAttributeValue(XProcXmlModel.Attributes.DESCRIPTION);
         final String disposition = node.getAttributeValue(XProcXmlModel.Attributes.DISPOSITION);
         final FormBodyPart bodyPart = new FormBodyPart("body", body)
         {
             @Override
             protected void generateContentDisp(final ContentBody body)
             {
                 if (disposition != null)
                 {
                     addField(MIME.CONTENT_DISPOSITION, disposition);
                 }
             }
 
             @Override
             protected void generateTransferEncoding(final ContentBody body)
             {
                 if (encoding != null)
                 {
                     addField(MIME.CONTENT_TRANSFER_ENC, encoding);
                 }
             }
 
             @Override
             protected void generateContentType(final ContentBody body)
             {
                 final StringBuilder buffer = new StringBuilder();
                 buffer.append(body.getMimeType());
                 if (body.getCharset() != null)
                 {
                     try
                     {
                         final String testCharset = new ContentType(body.getMimeType()).getParameter("charset");
                         if (testCharset != null)
                         {
                             final Charset charset = Charset.forName(testCharset);
                             if (!StringUtils.equalsIgnoreCase(charset.displayName(), body.getCharset()))
                             {
                                 buffer.append("; charset=").append(body.getCharset().toLowerCase());
                             }
                         }
                         else
                         {
                             buffer.append("; charset=utf-8");
                         }
                     }
                     catch (final ParseException e)
                     {
                         throw XProcExceptions.xc0020(node);
                     }
                     catch (final IllegalCharsetNameException e)
                     {
                         throw XProcExceptions.xc0020(node);
                     }
                 }
                 addField(MIME.CONTENT_TYPE, buffer.toString());
             }
         };
         if (id != null)
         {
             bodyPart.addField("Content-ID", id);
         }
         if (description != null)
         {
             bodyPart.addField("Content-Description", description);
         }
 
         return bodyPart;
     }
 
     private void verifyHeader(final String headerVal, final String headerName, final XdmNode node)
     {
         if (StringUtils.isNotBlank(headerVal))
         {
             final Header header = request.getHeaders().getFirstHeader(headerName);
             if (header != null)
             {
                 if (!StringUtils.equalsIgnoreCase(headerVal, header.getValue()))
                 {
                     throw XProcExceptions.xc0020(node);
                 }
             }
         }
     }
 
     private StringEntity parseBody(final XdmNode node)
     {
         final XdmNode body = SaxonAxis.childElement(node, XProcXmlModel.Elements.BODY);
         if (body != null)
         {
             final String contentTypeAtt = body.getAttributeValue(XProcXmlModel.Attributes.CONTENT_TYPE);
             final String encoding = body.getAttributeValue(XProcXmlModel.Attributes.ENCODING);
             final ContentType contentType = Steps.getContentType(contentTypeAtt, body);
             final String contentString = getContentString(body, contentType, encoding);
             try
             {
                 return new StringEntity(contentString, contentType.toString(), getCharset(
                         contentType.getParameter("charset"), "utf-8").toString());
             }
             catch (final UnsupportedEncodingException e)
             {
                 throw XProcExceptions.xc0020(body);
             }
         }
         return null;
     }
 
     private CredentialsProvider parseAuthentication(final XdmNode requestNode)
     {
         final String username = requestNode.getAttributeValue(XProcXmlModel.Attributes.USERNAME);
         if (!Strings.isNullOrEmpty(username))
         {
             final String password = requestNode.getAttributeValue(XProcXmlModel.Attributes.PASSWORD);
             final String authMethod = requestNode.getAttributeValue(XProcXmlModel.Attributes.AUTH_METHOD);
             if (!StringUtils.equalsIgnoreCase(AuthPolicy.BASIC, authMethod)
                     && !StringUtils.equalsIgnoreCase(AuthPolicy.DIGEST, authMethod))
             {
                 throw XProcExceptions.xc0003(requestNode);
             }
 
             final CredentialsProvider credsProvider = new BasicCredentialsProvider();
             final HttpHost httpHost = request.getHttpHost();
             credsProvider.setCredentials(new AuthScope(httpHost.getHostName(), httpHost.getPort()),
                     new UsernamePasswordCredentials(username, password));
 
             return credsProvider;
         }
         return null;
     }
 
     private HttpContext parseContext(final AuthPolicy policy)
     {
         final HttpHost httpHost = request.getHttpHost();
         final AuthCache authCache = new BasicAuthCache();
         if (AuthPolicy.BASIC.equalsIgnoreCase(policy.toString()))
         {
             final BasicScheme basicAuth = new BasicScheme();
             authCache.put(httpHost, basicAuth);
         }
         else
         {
             final DigestScheme digestAuth = new DigestScheme();
             authCache.put(httpHost, digestAuth);
         }
         final BasicHttpContext localContext = new BasicHttpContext();
         localContext.setAttribute(ClientContext.AUTH_CACHE, authCache);
         return localContext;
     }
 
     private HttpRequestBase constructMethod(final String method, final URI hrefUri)
     {
         final HttpEntity httpEntity = request.getEntity();
         final HeaderGroup headers = request.getHeaders();
         if (StringUtils.equalsIgnoreCase(HttpPost.METHOD_NAME, method))
         {
             final HttpPost httpPost = new HttpPost(hrefUri);
             for (final Header h : headers.getAllHeaders())
             {
                 if (!StringUtils.equalsIgnoreCase("Content-Type", h.getName()))
                 {
                     httpPost.addHeader(h);
                 }
             }
             httpPost.setEntity(httpEntity);
             return httpPost;
         }
         else if (StringUtils.equalsIgnoreCase(HttpPut.METHOD_NAME, method))
         {
             final HttpPut httpPut = new HttpPut(hrefUri);
             httpPut.setEntity(httpEntity);
             return httpPut;
         }
         else if (StringUtils.equalsIgnoreCase(HttpDelete.METHOD_NAME, method))
         {
             final HttpDelete httpDelete = new HttpDelete(hrefUri);
             httpDelete.setHeaders(headers.getAllHeaders());
             return httpDelete;
         }
         else if (StringUtils.equalsIgnoreCase(HttpGet.METHOD_NAME, method))
         {
             final HttpGet httpGet = new HttpGet(hrefUri);
             httpGet.setHeaders(headers.getAllHeaders());
             return httpGet;
 
         }
         else if (StringUtils.equalsIgnoreCase(HttpHead.METHOD_NAME, method))
         {
             final HttpHead httpHead = new HttpHead(hrefUri);
             httpHead.setHeaders(headers.getAllHeaders());
             return httpHead;
         }
         return null;
     }
 }
