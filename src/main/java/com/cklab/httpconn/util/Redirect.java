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
package com.cklab.httpconn.util;

import com.cklab.httpconn.reader.HTTPReader;
import com.cklab.httpconn.request.HTTPRequest;

/**
 * A redirect object issued by HTTPReader.
 * 
 * @author cklab
 * 
 */
public class Redirect {

	private HTTPReader	rdr;		// the reader created for this redirect
	private HTTPRequest	req;		// the HTTPRequest created for this redirect

	boolean				followed;

	/**
	 * Create a Redirect that leads to the use of the given HTTPReader and HTTPRequest.
	 * 
	 * @param rdr
	 *            the HTTPReader for this redirect
	 * @param req
	 *            the HTTPRequest for this redirect
	 */
	public Redirect(HTTPReader rdr, HTTPRequest req) {
		this.rdr = rdr;
		this.req = req;
	}

	/**
	 * Get the HTTPReader for this redirect.
	 * 
	 * @return the HTTPReader for this redirect
	 */
	public HTTPReader getHTTPReader() {
		return rdr;
	}

	/**
	 * Get the HTTP Request for this redirect.
	 * 
	 * @return the HTTP Request for this redirect.
	 */
	public HTTPRequest getHTTPRequest() {
		return req;
	}
	
	public boolean isFollowed() {
		return followed;
	}
	
	public void setFollowed(boolean followed) {
		this.followed = followed;
	}
}
