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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.cklab.httpconn.util.InputTag;
import com.cklab.httpconn.util.Redirect;

/**
 * HTTPRequest class.
 * 
 * This is the main object that is used to execute requests via an HTTPReader.
 * 
 * @author cklab
 *
 */

public class HTTPRequest {

	protected String post, method, page, referrer, cookies;
	private int statusCode;
	protected InputStream iStream;
	protected OutputStream oStream;
	private Redirect redir; 
	private boolean useSSL;
	private StringBuilder body;
	private Map<String, List<String>> headers;
	
	
	private Hashtable<String, InputTag> inputs;
	/**
	 * Create an HTTP Request.
	 * @param method The method to use, e.g. "GET"
	 * @param page the page to load
	 * @param useSSL whether or not SSL should be used
	 */
	public HTTPRequest(String method, String page, boolean useSSL)
	{
		this(method,page,null,null, null, useSSL);
	}
	
	/**
	 * Create an HTTP Request.
	 * @param method The method to use, e.g. "GET"
	 * @param page the page to load
	 * @param referrer the referrer
	 * @param useSSL whether or not SSL should be used
	 */
	public HTTPRequest(String method, String page, String referrer, boolean useSSL)
	{
		this(method,page,null,referrer, null, useSSL);
	}
	
	/**
	 * Create an HTTP Request.
	 * @param method The method to use, e.g. "GET"
	 * @param page the page to load
	 * @param post the form data
	 * @param cookies the cookie string (each cookie separated by a semi-colon)
	 * @param useSSL whether or not SSL should be used
	 */
	public HTTPRequest(String method, String page, String post, String cookies, boolean useSSL)
	{
		this(method, page, post, null,cookies, useSSL);
	}
	
	/**
	 * Create an HTTP Request.
	 * @param method The method to use, e.g. "GET"
	 * @param page the page to load
	 * @param post the form data
	 * @param referrer the referrer
	 * @param cookies the cookie string (each cookie separated by a semi-colon)
	 * @param useSSL whether or not SSL should be used
	 */
	public HTTPRequest(String method, String page, String post, String referrer, String cookies, boolean useSSL)
	{
		this.method = method;
		this.page = page;
		this.post = post;
		this.referrer = referrer;
		this.cookies = cookies;
		this.useSSL = useSSL;
		this.redir = null;
		this.body = new StringBuilder();
		this.inputs = new Hashtable<String, InputTag>();
	}
	
	
	/**
	 * Read the page and store necessary information.
	 */
	public void readBody()
	{
		body.setLength(0);
		
		Scanner in = new Scanner(iStream);
		//Pattern inputTagPattern = Pattern.compile("<input.*?>");
		Pattern inputNamePattern = Pattern.compile("<input.*?name=\"(.*?)\".*?>");
		Pattern inputValuePattern = Pattern.compile("<input.*?value=\"(.*?)\".*?>");
		Pattern inputTypePattern = Pattern.compile("<input.*?type=\"(.*?)\".*?>");
		while (in.hasNextLine())
		{
			String buf = in.nextLine();
			body.append(buf+"\r\n");
			
			if (buf.contains("<input")) {
				// separate all the <input>'s
				
				Matcher m_name = inputNamePattern.matcher(buf);
				Matcher m_value = inputValuePattern.matcher(buf);
				Matcher m_type = inputTypePattern.matcher(buf);
				while (m_name.find() && m_value.find() && m_type.find())
				{
					
					String name = m_name.group(1).trim();
					String value = m_value.group(1).trim();
					String type = m_type.group(1);
					inputs.put(name, new InputTag(name, value, type));
				}
			}
		}
	}
	
	/**
	 * Get the form data.
	 * 
	 * Note: This is the key-value pair that is sent as a result of a POST request.
	 * @return the form data.	
	 */
	public String getFormData()
	{
		return post.replaceAll("\\s", "%20");
	}
	
	/**
	 * Set the form data. 
	 * 
	 * Note: This is the key-value pair that is sent as a result of a POST request.
	 * @param data the form data
	 */
	public void setFormData(String data)
	{
		this.post = data;
	}
	
	/**
	 * Set the headers.
	 * 
	 * The Map keys are Strings that represent the response-header field names. Each Map value is a List of Strings that represents the corresponding field values
	 * 
	 * @param headers the headers
	 */
	public void setHeaders(Map<String, List<String>> headers)
	{
		this.headers = headers;
	}
	
	/**
	 * Get the headers.
	 * 
	 * The Map keys are Strings that represent the response-header field names. Each Map value is a List of Strings that represents the corresponding field values
	 * 
	 * @return the headers
	 */
	public Map<String, List<String>> getHeaders()
	{
		return headers;
	}
	
	/**
	 * Set the referrer.
	 * @param ref the referrer.
	 */
	public void setReferrer(String ref)
	{
		this.referrer = ref;
	}
	
	
	/**
	 * Set the status code received as a result of executing this HTTP Request.
	 * @param statusCode the statusCode
	 */
	public void setStatusCode(int statusCode)
	{
		this.statusCode = statusCode;
	}
	
	/**
	 * Set the page to be visited.
	 * @param page the page to be visited
	 */
	public void setPage(String page)
	{
		this.page = page;
	}

	/**
	 * Get the page to be visited.
	 * @return the page to be visited.
	 */
	public String getPage()
	{
		return page;
	}
	
	/**
	 * The method used for this HTTP Request. (e.g. GET)
	 * @return the method used for this HTTP Request
	 */
	public String getMethod()
	{
		return method;
	}
	
	/**
	 * Get the referrer.
	 * @return the referrer.
	 */
	public String getReferrer()
	{
		return referrer;
	}
	
	/**
	 * Get the status code received as a result of executing this HTTP Request.
	 * @return the status code received as a result of executing this HTTP Request.
	 */
	public int getStatusCode()
	{
		return statusCode;
	}
	
	/**
	 * Get the cookies used in this HTTP Request.
	 * @return the cookies
	 */
	public String getCookies()
	{
		return cookies;
	}
	
	/**
	 * Set the cookies used in this HTTP Request.
	 * @param s the cookies
	 */
	public void setCookies(String s)
	{
		this.cookies = s;
	}
	
	/**
	 * The String representation of this HTTPRequest 
	 */
	public String toString()
	{
		return "[method="+getMethod()+
				",page="+getPage()+
				",referrer="+getReferrer()+
				",form_data="+getFormData()+
				",cookies="+getCookies()+
				",status_code="+getStatusCode()+
				",useSSL="+isUsingSSL()+
				"]";
	}
	/**
	 * Get a Scanner object to read the body of this HTTP Request.
	 * @see #getInputStream()
	 * @return a Scanner object to read the body of this HTTP Request.
	 */
	public Scanner getScanner()
	{
		//return in;
		//return new Scanner(iStream);
		return new Scanner(body.toString());
	}
	
	/**
	 * Set the body. This is what has been read from the page as a result of executing this HTTP Request. 
	 * @param body the body of the page
	 */
	public void setBody(String body)
	{
		this.body.setLength(0);
		
		if (body != null)
			this.body = new StringBuilder(body);
	}
	
	/**
	 * Get the body of this page (the content of this page)
	 * @return the body of the page.
	 */
	public String getBody()
	{
		return body.toString();
	}

	/**
	 * Get the <code>InputTag</code> associated with an input tag that has the given <code>name</code> field. 
	 * @param name the name field of the tag
	 * @return the <code>InputTag</code> associated with an input tag that has the given <code>name</code> field. 
	 */
	public InputTag getInputValueByName(String name)
	{
		if (name == null)
			return null;
		
		name = name.toLowerCase();
		InputTag input = inputs.get(name);
		return input;
	}
	
	
	/**
	 * Get all the <code>input</code> tags on the page 
	 * @return all the <code>input</code> tags on the page 
	 */
	public ArrayList<InputTag> getInputFields()
	{
		return getInputFields(null);
	}
	
	/**
	 * Get all the <code>input</code> tags on the page with the given type 
	 * @param type the type, e.g. <code>hidden</code>
	 * @return all the <code>input</code> tags on the page with the given type.
	 */
	public ArrayList<InputTag> getInputFields(String type)
	{
		ArrayList<InputTag> l = new ArrayList<InputTag>();
		Iterator<String> i = inputs.keySet().iterator();
		while (i.hasNext())
		{
			String name = i.next();
			InputTag input = inputs.get(name);
			if (type == null)
			{
				l.add(input);
			} else {
				if (input.getType().equalsIgnoreCase(type))
					l.add(input);
			}
		}
		return l;
	}
	
	/**
	 * The InputStream used for reading from the connection.
	 * @return the input stream.
	 */
	public InputStream getInputStream()
	{
		try {
			return new ByteArrayInputStream(body.toString().getBytes("UTF-8"));
		} catch (Exception e) { e.printStackTrace(); }
		return null;
		//return iStream;
	}
	
	/**
	 * The OutputStream used for writing to the connection.
	 * @return the input stream.
	 */
	public OutputStream getOutputStream()
	{
		return oStream;
	}

	
	/**
	 * Kill and cleanup the connection. 
	 */
	public void die()
	{
		try {
			if (iStream != null)
				iStream.close();
			if (oStream != null)
				oStream.close();
			
			iStream = null;
			headers.clear();
			body.setLength(0);
			headers = null;
		} catch (Exception e) { }
	}
	
	/**
	 * Set this HTTP Request to use SSL.
	 * @param useSSL whether or not SSL should be used.
	 */
	public void useSSL(boolean useSSL)
	{
		this.useSSL = useSSL;
	}
	
	/**
	 * Whether or not this HTTP Request is set to use SSL.
	 * @return true if SSL is used, false otherwise.
	 */
	public boolean isUsingSSL()
	{
		return useSSL;
	}

	/**
	 * Get the Redirect object that results from loading this page.
	 * @return the Redirect object that results from loading this page.
	 */
	public Redirect getRedirect()
	{
		return redir;
	}
	
	/**
	 * Get the Redirect object that results from loading this page.
	 * @param redir the Redirect object that results from loading this page.
	 */
	public void setRedirect(Redirect redir)
	{
		this.redir = redir;
	}
	
	/**
	 * Set the InputStream used to read this connection.
	 * @param in the InputStream used to read this connection.
	 */
	public void setInputStream(InputStream in)
	{
		this.iStream = in;
	}

	/**
	 * Clone this HTTP Request
	 */
	public HTTPRequest clone()
	{
		return new HTTPRequest(method, page, post, referrer, cookies, useSSL);
	}
}
