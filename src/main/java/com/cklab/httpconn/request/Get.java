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
/**
 * An HTTPRequest suited for GET requests.
 * 
 * @author krazy
 */

package com.cklab.httpconn.request;

import com.cklab.httpconn.reader.HTTPReader;

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
	 * Construct a Get object to request the home page associated with the {@link HTTPReader} that executes it.
	 */
	public Get() {
		this ("");
	}
	
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
