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
package com.cklab.httpconn.request;
import com.cklab.httpconn.util.FormData;

/**
 * Post class.
 * 
 * Used for an HTTP POST request.
 * 
 * @author cklab
 *
 */
public class Post extends HTTPRequest {

	
	/**
	 * Construct a POST object to the given Page. SSL off by default.
	 * @param page the page to request
	 */
	public Post(String page)
	{
		this(page, false);
	}

	/**
	 * Convert an HTTPRequest into a Post object.
	 * @param req the HTTPRequest
	 */
	public Post (HTTPRequest req)
	{
		this(req.getPage(), req.getCookies(), req.isUsingSSL());
	}
	
	/**
	 * Construct a Post object to the given page.
	 * @param page the page to request
	 * @param useSSL whether or not to use SSL.
	 */
	public Post(String page, boolean useSSL)
	{
		this(page, null, useSSL);
	}
	
	
	/**
	 * Construct a Post object to the given page and using the given cookies.
	 * @param page the page to request
	 * @param cookies the cookies (e.g. "cookieName1=cookieVal1; cookieName2=cookieVal2")
	 * @param useSSL whether or not to use SSL.
	 */
	public Post(String page, String cookies, boolean useSSL)
	{
		super("POST", page, "", cookies, useSSL);
	}
	
	/**
	 * Set the post data for the POST request.
	 * @param fd the post data
	 */
	public void setFormData(FormData[] fd)
	{
		String post = "";
		for (FormData data : fd)
		{
			post+=data.getName()+"="+data.getValue()+"&";
		}
		try {
			setFormData(post.substring(0,post.length()-1));
		} catch (Exception e) { e.printStackTrace(); }
	}

	/**
	 * Add a Post Data field to the existing post data in this Post object.
	 * @param fd the FormData to add
	 */
	public void addPostField(FormData fd)
	{
		String post = getFormData();
		if (post == null || post.equals(""))
			post = "";
		else 
			post = post+"&";
		setFormData(post+fd.getName()+"="+fd.getValue());
	}
	
	
	/**
	 * Get the value of the post data field with the given key.
	 * @param in_key the key name
	 * @return the value of the post data
	 */
	public String getFormField(String in_key)
	{
		String fields[] = post.split("&");
		for (int i = 0;i<fields.length;i++)
		{
			String params[] = fields[i].split("=");
			String key = params[0];
			String value = params[1];
			if (key.equals(in_key))
			{
				return value;
			}
		}
		return null;
	}
	/**
	 * Obtain a clone of this Post object.
	 * Preserved variables:
	 * 		page - the page
	 * 		useSSL - whether or not to use SSL
	 * 		cookies - the cookies for the HTTPRequest
	 * 		formPost - the POST string
	 * 
	 * @return the cloned Post object
	 */
	public Post clone()
	{
		Post ret = new Post(page, cookies, isUsingSSL());
		ret.setFormData(getFormData());
		return ret;
	}
}
