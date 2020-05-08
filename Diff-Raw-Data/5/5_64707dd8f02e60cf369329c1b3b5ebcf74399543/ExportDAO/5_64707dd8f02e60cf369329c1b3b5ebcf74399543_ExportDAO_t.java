 package com.order.src.dao;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Vector;
 
 import com.order.src.ConnectionManager;
 import com.order.src.objects.Header;
 import com.order.src.objects.SoldTo;
 
 public class ExportDAO {
 
 	// atlantia database
 	private String database;
 
 	// bev database
 	private String database2;
 
 	public ExportDAO(String database) {
 		this.database = database;
 	}
 
 	public ExportDAO(String database, String database2) {
 		// atlantia database
 		this.database = database;
 		// bev database
 		this.database2 = database2;
 	}
 
 	public Connection getConn() throws SQLException {
 		return new ConnectionManager().getConnection(database);
 	}
 
 	public Connection getConn2() throws SQLException {
 		return new ConnectionManager().getConnection(database2);
 	}
 
 	// gets header infomation from the new bve database
 	public Vector<Header> getHeaderInformation() throws SQLException {
 		String sql = "";
 		
 		sql = "SELECT h.order_no number, h.cust_no custno, h.cust_po_no custpono, h.ref_no walmartcustpono "
 				+ "FROM bve_order h "
 				+ "WHERE h.order_no in (select distinct order_no from bve_order_dtl WHERE bvcmtdqty > 0) "
 						+ "AND h.ord_status = 'C' "
//						+ "AND (h.ord_status = 'C' OR h.cust_no = 'WALMART.CA') "						//this line gets any walmart data... remove this once the data is okay
 				+ "ORDER BY h.order_no";
 		Statement stmt = getConn2().createStatement();
 		ResultSet rs = stmt.executeQuery(sql);
 
 		Vector<Header> headers = new Vector<Header>();
 
 		while (rs.next()) {
 			Header header = new Header();
 			header.setNo(rs.getString("number"));			//number
 			header.setComment(getComments(header.getNo()));
 			header.setContact("");
 			header.setInvoice("");
 			header.setNumber(rs.getString("custno"));	//ref_no
			if ("WALMART.CA".equalsIgnoreCase(header.getNumber().trim())){
 				header.setPoNo(rs.getString("walmartcustpono"));	//po_no for walmart in the ref_no column
 			}else{
 				header.setPoNo(rs.getString("custpono"));   //po_no for everybody else in cust_po_no column
 			}
 			header.setRefNo("");
 			header.setService("");
 			header.setShipVia("");
 			header.setShipOn("");
 
 			headers.add(header);
 		}
 
 		return headers;
 	}
 	
 	private String getComments(String number) throws SQLException {
 		String comment = "";
 		
 		String sql = "SELECT n_data comment FROM bve_notes where n_key = '" + number.trim() + "' order by bvrvadddate, bvrvaddtime";
 			
 		Statement stmt = getConn2().createStatement();
 		ResultSet rs = stmt.executeQuery(sql);
 		
 		while (rs.next()) {
 			comment = rs.getString("comment");
 		}
 		
 		return comment;
 	}
 
 	// gets detail information from the new bev database
 	public Vector<com.order.src.objects.Line> getDetailLineInformation(String number) throws SQLException {
 		String sql = "SELECT order_no number, ord_sequence recno, ord_part_no item, ord_description description, bvcmtdqty qty, comment comment, ord_part_whse warehouse "
 				+ "FROM bve_order_dtl "
 				+ "WHERE ord_part_no <> '' AND bvcmtdqty > 0 "
 				+ "AND order_no like '%" + number.trim() + "%'";
 
 		Statement stmt = getConn2().createStatement();
 		ResultSet rs = stmt.executeQuery(sql);
 
 		Vector<com.order.src.objects.Line> lines = new Vector<com.order.src.objects.Line>();
 		while (rs.next()) {
 			com.order.src.objects.Line line = new com.order.src.objects.Line();
 			line.setComment(rs.getString("comment"));
 			line.setDescription(rs.getString("description"));
 			line.setItem(rs.getString("item"));
 			line.setLot("");
 			line.setNo(rs.getString("recno"));
 			line.setQty(rs.getInt("qty"));
 			line.setSerial("");
 			line.setWarehouse(rs.getString("warehouse").trim());
 
 			lines.add(line);
 		}
 
 		return lines;
 	}
 	
 	// gets sold to information from the atlantia database
 	public com.order.src.objects.SoldTo getSoldToInformation(String number, String custno) throws SQLException {
 		String sql = "";
 		
 		if ("WALMART.CA".equalsIgnoreCase(custno.trim())){
 			sql = "SELECT comment comment " +
 					"FROM bve_order_dtl " +
 					"WHERE order_no like '%" + number.trim() + "%' " +
 					"AND ord_sequence = 1";
 		}else{
 			sql = "SELECT a.addr_type addrtype, a.name name, a.bvaddr1 addr1, a.bvaddr2 addr2, a.bvcity city, a.ship_desc shipdesc, "
 				+ "a.bvprovstate prov, a.bvcountrycode country, a.bvpostalcode postalcode, a.bvcocontact1name cname, "
 				+ "a.bvaddrtelno1 tel1, a.bvaddrtelno2 tel2, a.bvaddremail email "
 				+ "FROM order_address a "
 				+ "WHERE a.cev_no = '" + number.trim() + "' "
 				+ "AND a.addr_type = 'B'";
 		}
 		
 		Statement stmt = null;
 		if ("WALMART.CA".equalsIgnoreCase(custno.trim())){
 			stmt = getConn2().createStatement();
 		}else{
 			stmt = getConn().createStatement();
 		}
 		ResultSet rs = stmt.executeQuery(sql);
 
 		SoldTo soldTo = new SoldTo();
 		String soldToString = "";
 		if ("WALMART.CA".equalsIgnoreCase(custno.trim())){
 			while (rs.next()){
 				soldToString = rs.getString("comment");
 			}
 		}else{
 			while (rs.next()){
 				soldTo.setAddress1(rs.getString("addr1"));
 				soldTo.setAddress2(rs.getString("addr2"));
 				soldTo.setCity(rs.getString("city"));
 				soldTo.setCode(rs.getString("addrtype"));
 				soldTo.setContact(rs.getString("cname"));
 				soldTo.setCountry(rs.getString("country"));
 				soldTo.setEmail(rs.getString("email"));
 				soldTo.setFax(rs.getString("tel2"));
 				soldTo.setName(rs.getString("name"));
 				soldTo.setPhone(rs.getString("tel1"));
 				soldTo.setPostal(rs.getString("postalcode"));
 				soldTo.setProvince(rs.getString("prov"));
 			}
 		}
 		
 		if ("WALMART.CA".equalsIgnoreCase(custno.trim())){
 			String[] soldToArray = soldToString.split(","); 
 			soldTo.setName(" ");
 			soldTo.setAddress1(" ");
 			soldTo.setAddress2(" ");
 			soldTo.setCity(" ");
 			soldTo.setProvince(" ");
 			soldTo.setPostal(" ");
 			soldTo.setCountry(" ");
 			
 			//   0          1           2        3       4            5           6
 			//<Contact>,<Address1>,<Address2>,<City>,<Province>,<Postal Code>,<Country>
 			if (soldToArray.length > 0){
 				soldTo.setName(soldToArray[0]);	
 			}			
 			if (soldToArray.length > 1){
 				soldTo.setAddress1(soldToArray[1]);
 			}
 			if (soldToArray.length > 2){
 				soldTo.setAddress2(soldToArray[2]);
 			}
 			if (soldToArray.length > 3){
 				soldTo.setCity(soldToArray[3]);
 			}
 			if (soldToArray.length > 4){
 				soldTo.setProvince(soldToArray[4]);
 			}
 			if (soldToArray.length > 5){
 				soldTo.setPostal(soldToArray[5]);
 			}
 			if (soldToArray.length > 6){
 				if ("CA".equals(soldToArray[6])){
 					soldTo.setCountry("CDN");
 				}else{
 					soldTo.setCountry(soldToArray[6]);
 				}
 			}
 			soldTo.setCode("B");		//address type 'B' for Bill
 			soldTo.setContact(" ");		//blank, not provided
 			soldTo.setEmail(" ");		//blank, not provided
 			soldTo.setFax(" ");			//blank, not provided
 			soldTo.setPhone(" ");		//blank, not provided
 			
 			
 		}
 
 		return soldTo;
 	}
 
 	// gets shipto to information from the atlantia database
 	public com.order.src.objects.ShipTo getShipToInformation(String number) throws SQLException {
 		String sql = "SELECT a.addr_type addrtype, a.name name, a.bvaddr1 addr1, a.bvaddr2 addr2, a.bvcity city, a.ship_desc shipdesc, "
 			+ "a.bvprovstate prov, a.bvcountrycode country, a.bvpostalcode postalcode, a.bvcocontact1name cname, "
 			+ "a.bvaddrtelno1 tel1, a.bvaddrtelno2 tel2, a.bvaddremail email "
 			+ "FROM order_address a "
 			+ "WHERE a.cev_no = '" + number.trim() + "' "
 			+ "AND a.addr_type = 'S'";
 
 		Statement stmt = getConn().createStatement();
 		ResultSet rs = stmt.executeQuery(sql);
 
 		com.order.src.objects.ShipTo shipTo = new com.order.src.objects.ShipTo();
 		while (rs.next()){
 			shipTo.setAddress1(rs.getString("addr1"));
 			shipTo.setAddress2(rs.getString("addr2"));
 			shipTo.setCity(rs.getString("city"));
 			shipTo.setCode(rs.getString("addrtype"));
 			shipTo.setContact(rs.getString("cname"));
 			shipTo.setCountry(rs.getString("country"));
 			shipTo.setEmail(rs.getString("email"));
 			shipTo.setFax(rs.getString("tel2"));
 			shipTo.setName(rs.getString("name"));
 			shipTo.setPhone(rs.getString("tel1"));
 			shipTo.setPostal(rs.getString("postalcode"));
 			shipTo.setProvince(rs.getString("prov"));
 		}
 
 		return shipTo;
 	}
 
 }
