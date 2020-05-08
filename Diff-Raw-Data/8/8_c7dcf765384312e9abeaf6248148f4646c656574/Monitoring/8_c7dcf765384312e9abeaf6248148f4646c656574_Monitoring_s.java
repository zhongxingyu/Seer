 //
 // typica - A client library for Amazon Web Services
 // Copyright (C) 2007,2008,2009 Xerox Corporation
 // 
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at
 //
 //     http://www.apache.org/licenses/LICENSE-2.0
 //
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 //
 
 package com.xerox.amazonws.monitoring;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.bind.JAXBException;
 
 import org.apache.commons.httpclient.HttpMethodBase;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.xerox.amazonws.common.AWSException;
 import com.xerox.amazonws.common.AWSQueryConnection;
 import com.xerox.amazonws.typica.monitor.jaxb.Dimension;
 import com.xerox.amazonws.typica.monitor.jaxb.GetMetricStatisticsResponse;
 import com.xerox.amazonws.typica.monitor.jaxb.GetMetricStatisticsResult;
 import com.xerox.amazonws.typica.monitor.jaxb.ListMetricsResponse;
 import com.xerox.amazonws.typica.monitor.jaxb.ListMetricsResult;
 import com.xerox.amazonws.typica.monitor.jaxb.Metrics;
 
 /**
  * A Java wrapper for the Monitoring web services API
  */
 public class Monitoring extends AWSQueryConnection {
 
     private static Log logger = LogFactory.getLog(Monitoring.class);
     private static String dateFormat = "yyyy-MM-dd'T'HH:mm:ss'Z'";
 
 	/**
 	 * Initializes the ec2 service with your AWS login information.
 	 *
      * @param awsAccessId The your user key into AWS
      * @param awsSecretKey The secret string used to generate signatures for authentication.
 	 */
     public Monitoring(String awsAccessId, String awsSecretKey) {
         this(awsAccessId, awsSecretKey, true);
     }
 
 	/**
 	 * Initializes the ec2 service with your AWS login information.
 	 *
      * @param awsAccessId The your user key into AWS
      * @param awsSecretKey The secret string used to generate signatures for authentication.
      * @param isSecure True if the data should be encrypted on the wire on the way to or from EC2.
 	 */
     public Monitoring(String awsAccessId, String awsSecretKey, boolean isSecure) {
         this(awsAccessId, awsSecretKey, isSecure, "monitoring.amazonaws.com");
     }
 
 	/**
 	 * Initializes the ec2 service with your AWS login information.
 	 *
      * @param awsAccessId The your user key into AWS
      * @param awsSecretKey The secret string used to generate signatures for authentication.
      * @param isSecure True if the data should be encrypted on the wire on the way to or from EC2.
      * @param server Which host to connect to.  Usually, this will be monitoring.amazonaws.com
 	 */
     public Monitoring(String awsAccessId, String awsSecretKey, boolean isSecure,
                              String server)
     {
         this(awsAccessId, awsSecretKey, isSecure, server,
              isSecure ? 443 : 80);
     }
 
     /**
 	 * Initializes the ec2 service with your AWS login information.
 	 *
      * @param awsAccessId The your user key into AWS
      * @param awsSecretKey The secret string used to generate signatures for authentication.
      * @param isSecure True if the data should be encrypted on the wire on the way to or from EC2.
      * @param server Which host to connect to.  Usually, this will be ec2.amazonaws.com
      * @param port Which port to use.
      */
     public Monitoring(String awsAccessId, String awsSecretKey, boolean isSecure,
                              String server, int port)
     {
 		super(awsAccessId, awsSecretKey, isSecure, server, port);
 		ArrayList<String> vals = new ArrayList<String>();
 		vals.add("2009-05-15");
 		super.headers.put("Version", vals);
     }
 
 	/**
 	 * Describe the AMIs that match the intersection of the criteria supplied
 	 * 
 	 * @param period granularity in seconds for the returned data points
 	 * @param namespace
 	 * @param statistics
 	 * @param dimensions one or more dimension along which to aggregate the data
 	 * @param startTime timestamp of the first datapoint
 	 * @param endTime timestamp of the last datapoint
 	 * @param measureName name of a measure for the gathered metric
 	 * @param unit standard unit of measurement for a given measure
 	 * @param customUnit user defined custom unit applied to a measure
 	 * @return A list of {@link Datapoint}.
 	 * @throws MonitoringException wraps checked exceptions
 	 */
 	public MetricStatisticsResult getMetricStatistics(int period,
 										List<Statistics> statistics, String namespace,
 										Map<String, String> dimensions, Date startTime, Date endTime,
 										String measureName, StandardUnit unit, String customUnit) throws MonitoringException {
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("Period", ""+period);
 		params.put("Namespace", namespace);
         SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat);
 		params.put("StartTime", dateFormatter.format(startTime));
 		params.put("EndTime", dateFormatter.format(endTime));
 		for (int i=0 ; i<statistics.size(); i++) {
 			params.put("Statistics.member."+(i+1), statistics.get(i).getStatId());
 		}
 		if (unit != null) {
 			params.put("Unit", unit.getUnitId());
 		}
 		if (customUnit != null && !customUnit.equals("")) {
 			params.put("CustomUnit", customUnit);
 		}
 		if (dimensions != null && dimensions.size() > 0) {
 			int i=0;
 			for (String key : dimensions.keySet()) {
 				String value = dimensions.get(key);
 				params.put("Dimensions.member."+(i+1)+".Name", key);
 				params.put("Dimensions.member."+(i+1)+".Value", value);
 				i++;
 			}
 		}
 		params.put("MeasureName", measureName);
 		GetMethod method = new GetMethod();
 		try {
 			GetMetricStatisticsResponse response =
 					makeRequestInt(method, "GetMetricStatistics", params, GetMetricStatisticsResponse.class);
 			GetMetricStatisticsResult result = response.getGetMetricStatisticsResult();
 			MetricStatisticsResult ret = new MetricStatisticsResult(result.getLabel());
 			List<Datapoint> dpList = ret.getDatapoints();
 			List<com.xerox.amazonws.typica.monitor.jaxb.Datapoint> dps = result.getDatapoints().getMembers();
 			for (com.xerox.amazonws.typica.monitor.jaxb.Datapoint dp : dps) {
 				dpList.add(new Datapoint(dp.getTimestamp().toGregorianCalendar(), dp.getSamples(),
 								dp.getAverage(), dp.getSum(), dp.getMinimum(), dp.getMaximum(),
 								dp.getUnit(), dp.getCustomUnit()));
 			}
 			return ret;
 		} finally {
 			method.releaseConnection();
 		}
 	}
 
 	/**
 	 * List the valid metrics that have recorded data available.
 	 * 
 	 * @return A list of {@link Metric}.
 	 * @throws MonitoringException wraps checked exceptions
 	 */
 	public List<Metric> listMetrics() throws MonitoringException {
 		Map<String, String> params = new HashMap<String, String>();
 		GetMethod method = new GetMethod();
 		try {
 			List<Metric> ret = new ArrayList<Metric>();
 			String nextToken = null;
 			do {
 				if (nextToken != null) {
 					params.put("NextToken", nextToken);
 				}
 				ListMetricsResponse response =
 						makeRequestInt(method, "ListMetrics", params, ListMetricsResponse.class);
			System.err.println("request id = "+response.getResponseMetadata().getRequestId());
 				ListMetricsResult result = response.getListMetricsResult();
 				Metrics mtrx = result.getMetrics();
 				for (com.xerox.amazonws.typica.monitor.jaxb.Metric m : mtrx.getMembers()) {
 					Metric met = new Metric(m.getMeasureName(), m.getNamespace());
 					for (Dimension d : m.getDimensions().getMembers()) {
 						met.addDimension(d.getName(), d.getValue());
 					}
 					ret.add(met);
 				}
 				nextToken = result.getNextToken();
 			} while (nextToken != null);
 
 			return ret;
 		} finally {
 			method.releaseConnection();
 		}
 	}
 
 	protected <T> T makeRequestInt(HttpMethodBase method, String action, Map<String, String> params, Class<T> respType)
 		throws MonitoringException {
 		try {
 			return makeRequest(method, action, params, respType);
 		} catch (AWSException ex) {
 			throw new MonitoringException(ex);
 		} catch (JAXBException ex) {
 			throw new MonitoringException("Problem parsing returned message.", ex);
 		} catch (MalformedURLException ex) {
 			throw new MonitoringException(ex.getMessage(), ex);
 		} catch (IOException ex) {
 			throw new MonitoringException(ex.getMessage(), ex);
 		}
 	}
 }
