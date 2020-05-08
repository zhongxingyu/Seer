 /**
 * GenericDao interface
  *
 * @copyright 2010 Monits
  * @license   Copyright (C) 2010. All rights reserved
  * @version   Release: 1.0.0
  * @link      http://www.monits.com/
  * @since     1.0.0
  */
 package com.monits.commons;
 
 import java.util.List;
 
 /**
 * GenericDao interface
  *
  * @author    José Manuel Rolón<jmrolon@monits.com>
 * @copyright 2010 Monits
 * @license   Copyright (C) 2010. All rights reserved
  * @version   Release: 1.0.0
  * @link      http://www.monits.com/
  * @since     1.0.0
  */
 public class PaginatedResult<T> {
 
 	private int acutalPage;
 
 	private int totalPage;
 
	private List<T> pagedList;
 
 	/**
 	 * Basic constructor with all fields
 	 *
 	 * @param acutalPage The Selected page
 	 * @param totalPage The total amount of pages
	 * @param pagedList The items in the selected page
 	 */
	public PaginatedResult(int acutalPage, int totalPage, List<T> pagedList) {
 		super();
 		this.acutalPage = acutalPage;
 		this.totalPage = totalPage;
		this.pagedList = pagedList;
 	}
 
 	/**
 	 * @return the acutalPage
 	 */
 	public int getAcutalPage() {
 		return acutalPage;
 	}
 
 	/**
 	 * @return the totalPage
 	 */
 	public int getTotalPage() {
 		return totalPage;
 	}
 
 	/**
	 * @return the pagedList
 	 */
	public List<T> getPagedList() {
		return pagedList;
 	}
 
 }
