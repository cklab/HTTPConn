/*******************************************************************************
 * Copyright (C) 2011 CKLab
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package com.cklab.httpconn.request;
import java.util.HashMap;

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
		super("POST", page, null, cookies, useSSL);
	}
	
	/**
	 * Set the post data for the POST request.
	 * @param fd the post data
	 */
//	public void setFormData(FormData[] fd)
//	{
//		String post = "";
//		for (FormData data : fd)
//		{
//			String string = data.getValue().replaceAll("=", "%3D").replaceAll(";", "%3B").replaceAll("\\+", "%2B").replaceAll("/", "%2F");
//			post+=data.getName()+"="+string+"&";
//		}
//		try {
//			setFormData(post.substring(0,post.length()-1));
//		} catch (Exception e) { e.printStackTrace(); }
//	}

	/**
	 * Add a Post Data field to the existing post data in this Post object.
	 * @param fd the FormData to add
	 */
	public void addFormField(FormData fd)
	{
		postFields.add(fd);
	}
	
	
	/**
	 * Get the value of the post data field with the given key.
	 * @param in_key the key name
	 * @return the value of the post data
	 */
	public String getFormField(String in_key)
	{
		for (FormData fd : postFields) {
			if (fd.getName().equals(in_key)) {
				return fd.getValue();
			}
		}
		return null;
	}
}
