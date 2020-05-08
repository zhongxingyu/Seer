 package org.net;
 
 import java.io.Serializable;
 
 public class ClusterMapUtil {
 
 	static class GetQuery {
 		static class Request implements Serializable {
 			private static final long serialVersionUID = 1l;
 			protected Object key;
 		}
 
 		static class Response implements Serializable {
 			private static final long serialVersionUID = 1l;
 			protected Object value;
 		}
 	}
 
 	static class PutQuery {
 		static class Request implements Serializable {
 			private static final long serialVersionUID = 1l;
 			protected Object key, value;
 		}
 
 		static class Response implements Serializable {
 			private static final long serialVersionUID = 1l;
			protected Object value;
 		}
 	}
 
 }
