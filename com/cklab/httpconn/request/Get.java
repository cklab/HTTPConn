/*******************************************************************************
 * Copyright (c) 2011 CK Lab.
 * 
 * This file is part of HTTPConn.
 * 
 * HTTPConn is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * HTTPConn is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with HTTPConn. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
/**
 * An HTTPRequest suited for GET requests.
 * 
 * @author krazy
 */

package com.cklab.httpconn.request;

/**
 * Get Class.
 * 
 * Used for an HTTP GET request.
 * 
 * @author cklab
 *
 */
public class Get extends HTTPRequest {

	/**
	 * Construct a Get object to the given Page. SSL off by default.
	 * @param page the page to request
	 */
	public Get(String page)
	{
		this(page, false);
	}
	
	/**
	 * Convert an HTTPRequest into a Get object.
	 * @param req the HTTPRequest
	 */
	public Get(HTTPRequest req)
	{
		this(req.getPage(), req.getReferrer(), req.isUsingSSL());
	}
	
	/**
	 * Construct a Get object to the given page from given referrer. SSL off by default.
	 * @param page the page to request
	 * @param referrer the referrer
	 */
	public Get(String page, String referrer)
	{
		this(page, referrer, false);
	}
	
	/**
	 * Construct a Get object to the given page.
	 * @param page the page to request
	 * @param useSSL whether or not to use SSL.
	 */
	public Get(String page, boolean useSSL)
	{
		this(page, null, useSSL);
	}
	
	/**
	 * Construct a Get object to the given page from given referrer.
	 * @param page the page to request
	 * @param referrer the referrer
	 * @param useSSL whether or not to use SSL.
	 */
	public Get(String page, String referrer, boolean useSSL)
	{
		super("GET", page, referrer, useSSL);
	}
}
