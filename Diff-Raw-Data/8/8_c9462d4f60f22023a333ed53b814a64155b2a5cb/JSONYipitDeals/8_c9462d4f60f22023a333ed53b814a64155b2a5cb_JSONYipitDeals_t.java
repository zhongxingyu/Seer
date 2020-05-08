 package me.deal.shared.json;
 
 import java.util.ArrayList;
 
 public class JSONYipitDeals {
 	
 	public ArrayList<JSONYipitDeal> deals;
 	
 	public class JSONYipitDeal {
 		public Integer id;
 		public String date_added;
 		public String end_date;
 		public JSONDiscount discount;
 		public JSONPrice price;
 		public JSONValue value;
 		public String title;
 		public String yipit_title;
 		public String yipit_url;
 		public String mobile_url;
 		public JSONImage images;
 		public ArrayList<JSONTag> tags;
 		public JSONBusiness business;
 		public JSONSource source;
 		
 		public class JSONDiscount {
 			public Double raw;
 		}
 		
 		public class JSONPrice {
 			public String raw;
 		}
 		
 		public class JSONValue {
 			public String raw;
 		}
 		
 		public class JSONImage {
 			public String image_big;
 			public String image_small;
 		}
 		
 		public class JSONTag {
 			public String name;
 			public String slug;
 			public String url;
 		}
 		
 		public class JSONBusiness {
 			public String name;
 			public ArrayList<JSONLocation> locations;
 			
 			public class JSONLocation {
 				public String address;
				public String locality;
 				public String phone;
 				public String lat;
 				public String lon;
 				public String state;
 				public String zip_code;
 			}
 		}
 		
 		public class JSONSource {
 			public Integer paid;
 		}
 	}
 }
