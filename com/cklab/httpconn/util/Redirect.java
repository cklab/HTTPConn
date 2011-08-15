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
package com.cklab.httpconn.util;


import com.cklab.httpconn.reader.HTTPReader;
import com.cklab.httpconn.request.HTTPRequest;

/**
 * A redirect object issued by HTTPReader.
 * @author cklab
 *
 */
public class Redirect {

	private HTTPReader rdr;		//the reader created for this redirect
	private HTTPRequest req;	//the HTTPRequest created for this redirect
	
	/**
	 * Create a Redirect that leads to the use of the given HTTPReader and HTTPRequest.
	 * @param rdr the HTTPReader for this redirect
	 * @param req the HTTPRequest for this redirect
	 */
	public Redirect(HTTPReader rdr, HTTPRequest req)
	{
		this.rdr = rdr;
		this.req = req;
	}
	
	/**
	 * Get the HTTPReader for this redirect.
	 * @return the HTTPReader for this redirect
	 */
	public HTTPReader getHTTPReader()
	{
		return rdr;
	}
	
	/**
	 * Get the HTTP Request for this redirect.
	 * @return the HTTP Request for this redirect.
	 */
	public HTTPRequest getHTTPRequest()
	{
		return req;
	}
}
