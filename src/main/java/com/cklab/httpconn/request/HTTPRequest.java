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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.cklab.httpconn.reader.HTTPReader;
import com.cklab.httpconn.util.FormData;
import com.cklab.httpconn.util.InputTag;
import com.cklab.httpconn.util.PostFormat;
import com.cklab.httpconn.util.Redirect;
import com.eclipsesource.json.JsonObject;

/**
 * HTTPRequest class.
 * 
 * This is the main object that is used to execute requests via an HTTPReader.
 * 
 * @author cklab
 * 
 */

public class HTTPRequest implements Cloneable {

	private Map<String, List<String>>	headers;
	private ArrayList<InputTag>			inputs;

	private Map<String, List<String>>	headersToSend;
	protected List<FormData>			postFields;
	protected String					method;
	protected String					page;
	protected String					referrer;
	protected String					cookies;

	protected PostFormat				postFormat;

	private StringBuilder				body;
	private int							statusCode;

	private boolean						useSSL;

	private Redirect					redirect;

	

	/**
	 * Create an HTTP Request.
	 * 
	 * @param method
	 *            The method to use, e.g. "GET"
	 * @param page
	 *            the page to load
	 * @param useSSL
	 *            whether or not SSL should be used
	 */
	public HTTPRequest(String method, String page, boolean useSSL) {
		this(method, page, new ArrayList<FormData>(), null, null, useSSL);
	}

	/**
	 * Create an HTTP Request.
	 * 
	 * @param method
	 *            The method to use, e.g. "GET"
	 * @param page
	 *            the page to load
	 * @param referrer
	 *            the referrer
	 * @param useSSL
	 *            whether or not SSL should be used
	 */
	public HTTPRequest(String method, String page, String referrer, boolean useSSL) {
		this(method, page, new ArrayList<FormData>(), referrer, null, useSSL);
	}

	/**
	 * Create an HTTP Request.
	 * 
	 * @param method
	 *            The method to use, e.g. "GET"
	 * @param page
	 *            the page to load
	 * @param post
	 *            the form data
	 * @param cookies
	 *            the cookie string (each cookie separated by a semi-colon)
	 * @param useSSL
	 *            whether or not SSL should be used
	 */
	public HTTPRequest(String method, String page, String post, String cookies, boolean useSSL) {
		this(method, page, new ArrayList<FormData>(), null, cookies, useSSL);
		setFormData(post);
	}

	/**
	 * Create an HTTP Request.
	 * 
	 * @param method
	 *            The method to use, e.g. "GET"
	 * @param page
	 *            the page to load
	 * @param post
	 *            the form data
	 * @param referrer
	 *            the referrer
	 * @param cookies
	 *            the cookie string (each cookie separated by a semi-colon)
	 * @param useSSL
	 *            whether or not SSL should be used
	 */
	public HTTPRequest(String method, String page, List<FormData> postFields, String referrer, String cookies, boolean useSSL) {
		this.method = method;
		this.page = page;
		this.referrer = referrer;
		this.cookies = cookies;
		this.useSSL = useSSL;
		this.redirect = null;

		// we don't expect multiple threads to populate the body, let's stick with StringBuilder for now
		this.body = new StringBuilder();

		this.inputs = new ArrayList<InputTag>();
		this.headersToSend = new HashMap<String, List<String>>();
		this.postFields = postFields;
		this.postFormat = PostFormat.QUERY;
	}

	public void setPostFormat(PostFormat postFormat) {
		this.postFormat = postFormat;
	}

	/**
	 * Read the page and store necessary information.
	 * 
	 * TODO FIXME switch to an HTML Parser for the inputs: Potential solutions include JSoup/TagSoup/JTidy
	 */
	public void readBody(InputStream iStream) {
		// to avoid potential memory leak
		// -- if this HTTPRequest is read multiple times for some reason, `body` can get arbitrarily large
		body.setLength(0);

		// TODO revise, don't use regex here
		Scanner in = new Scanner(iStream);
		Pattern inputNamePattern = Pattern.compile("<input.*?name=\"(.*?)\".*?>");
		Pattern inputValuePattern = Pattern.compile("<input.*?value=\"(.*?)\".*?>");
		Pattern inputTypePattern = Pattern.compile("<input.*?type=\"(.*?)\".*?>");
		while (in.hasNextLine()) {
			String buf = in.nextLine();
			body.append(buf + "\r\n");

			if (buf.contains("<input")) {
				// separate all the <input>'s

				Matcher m_name = inputNamePattern.matcher(buf);
				Matcher m_value = inputValuePattern.matcher(buf);
				Matcher m_type = inputTypePattern.matcher(buf);
				while (m_name.find() && m_value.find() && m_type.find()) {

					String name = m_name.group(1).trim();
					String value = m_value.group(1).trim();
					String type = m_type.group(1);
					inputs.add(new InputTag(name, value, type));
				}
			}
		}
	}

	/**
	 * Get the form data.
	 * 
	 * Note: This is the key-value pair that is sent as a result of a POST request.
	 * 
	 * @return the form data.
	 */
	public String getFormData() {
		if (postFormat == PostFormat.JSON) {
			JsonObject data = new JsonObject();
			for (FormData fd : postFields) {
				String value = fd.getValue();
				data.add(fd.getName(), value);
			}
			
			return data.toString();
		} else {
			StringBuilder formData = new StringBuilder();
			for (FormData fd : postFields) {
				String value = fd.getValue();
				value = value.replaceAll("=", "%3D").replaceAll(";", "%3B").replaceAll("\\+", "%2B").replaceAll("/", "%2F").replaceAll("\\s", "+");

				formData.append(fd.getName());
				formData.append("=");
				formData.append(value);
				formData.append("&");
			}

			if (formData.length() > 0) {
				formData.setLength(formData.length() - 1); // remove last &
			}
			return formData.toString();
		}
	}

	/**
	 * Set the form data.
	 * 
	 * Note: This is the key-value pair that is sent as a result of a POST request.
	 * 
	 * @param data
	 *            the form data
	 */
	public void setFormData(String data) {
		if (data == null || data.length() <= 0) {
			return;
		}

		String fields[] = data.split("&");

		for (int i = 0; i < fields.length; i++) {
			String params[] = fields[i].split("=");

			if (params.length <= 0) {
				System.err.println("Malformed key/value pair for Post object");
				continue;
			}
			String key = params[0];
			String value = "";

			if (params.length >= 2) {
				value = params[1];
			}

			postFields.add(new FormData(key, value));
		}
	}

	/**
	 * Set the post data for the POST request.
	 * 
	 * @param fd
	 *            the post data
	 */
	public void setFormData(FormData[] fd) {
		for (FormData data : fd) {
			postFields.add(data);
		}
	}

	/**
	 * Add a header to be sent to the server. This will replace any default headers set by the {@link HTTPReader}.
	 * 
	 * @param headerKey
	 * @param headerValue
	 */
	public void addHeader(String headerKey, String headerValue) {
		List<String> values = headersToSend.get(headerKey);
		if (values == null) {
			values = new ArrayList<String>();
			headersToSend.put(headerKey, values);
		}
		values.add(headerValue);
	}
	
	/**
	 * Add a header to be sent to the server. This will replace any default headers set by the {@link HTTPReader}.
	 * 
	 * @param headerKey
	 * @param headerValue
	 */
	public void removeHeader(String headerKey) {
		headersToSend.remove(headerKey);
	}

	/**
	 * The headers to be sent to the server. This will replace any default headers set by the {@link HTTPReader}.
	 * 
	 * @return
	 */
	public Map<String, List<String>> getHeadersToSend() {
		return headersToSend;
	}

	/**
	 * Set the headers.
	 * 
	 * The Map keys are Strings that represent the response-header field names. Each Map value is a List of Strings that
	 * represents the corresponding field values
	 * 
	 * @param headers
	 *            the headers received from the server
	 */
	public void setHeaders(Map<String, List<String>> headers) {
		this.headers = headers;
	}

	/**
	 * Get the headers.
	 * 
	 * The Map keys are Strings that represent the response-header field names. Each Map value is a List of Strings that
	 * represents the corresponding field values
	 * 
	 * @return the headers received from the server
	 */
	public Map<String, List<String>> getHeaders() {
		return headers;
	}

	/**
	 * Set the referrer.
	 * 
	 * @param ref
	 *            the referrer.
	 */
	public void setReferrer(String ref) {
		this.referrer = ref;
	}

	/**
	 * Set the status code received as a result of executing this HTTP Request.
	 * 
	 * @param statusCode
	 *            the statusCode
	 */
	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	/**
	 * Set the page to be visited.
	 * 
	 * @param page
	 *            the page to be visited
	 */
	public void setPage(String page) {
		this.page = page;
	}

	/**
	 * Get the page to be visited.
	 * 
	 * @return the page to be visited.
	 */
	public String getPage() {
		return page;
	}

	/**
	 * The method used for this HTTP Request. (e.g. GET)
	 * 
	 * @return the method used for this HTTP Request
	 */
	public String getMethod() {
		return method;
	}

	/**
	 * Get the referrer.
	 * 
	 * @return the referrer.
	 */
	public String getReferrer() {
		return referrer;
	}

	/**
	 * Get the status code received as a result of executing this HTTP Request.
	 * 
	 * @return the status code received as a result of executing this HTTP Request.
	 */
	public int getStatusCode() {
		return statusCode;
	}

	/**
	 * Get the cookies used in this HTTP Request.
	 * 
	 * @return the cookies
	 */
	public String getCookies() {
		return cookies;
	}

	/**
	 * Get the Content-Type from the response header.
	 * 
	 * @return
	 */
	public String getContentType() {
		String contentType = null;
		if (headers != null) {
			List<String> types = headers.get("Content-Type");
			if (types.size() > 0) {
				contentType = types.get(0);
			}
		}
		return contentType;
	}

	/**
	 * Set the cookies used in this HTTP Request.
	 * 
	 * @param s
	 *            the cookies
	 */
	public void setCookies(String s) {
		this.cookies = s;
	}

	/**
	 * The String representation of this HTTPRequest
	 */
	public String toString() {
		return "[method=" + getMethod() + ",page=" + getPage() + ",referrer=" + getReferrer() + ",form_data=" + getFormData() + ",cookies="
				+ getCookies() + ",status_code=" + getStatusCode() + ",useSSL=" + isUsingSSL() + "]";
	}

	/**
	 * Get a Scanner object to read the body of this HTTP Request.
	 * 
	 * @see #getInputStream()
	 * @return a Scanner object to read the body of this HTTP Request.
	 */
	public Scanner getScanner() {
		return new Scanner(body.toString());
	}

	/**
	 * Set the body. This is what has been read from the page as a result of executing this HTTP Request.
	 * 
	 * @param body
	 *            the body of the page
	 */
	public void setBody(String body) {
		this.body.setLength(0);

		if (body != null)
			this.body = new StringBuilder(body);
	}

	/**
	 * Get the body of this page (the content of this page)
	 * 
	 * @return the body of the page.
	 */
	public String getBody() {
		return body.toString();
	}

	/**
	 * Get a list <code>InputTag</code> associated with an input tag that has the given <code>name</code> field.
	 * 
	 * @param name
	 *            the name field of the tag
	 * @return the list of <code>InputTag</code> objects associated with an input tag that has the given
	 *         <code>name</code> field.
	 */
	public ArrayList<InputTag> getInputsByName(String name) {
		if (name == null) {
			return null;
		}

		ArrayList<InputTag> found = new ArrayList<InputTag>();
		for (InputTag tag : inputs) {
			if (name.equals(tag.getName())) {
				found.add(tag);
			}
		}
		return found;
	}

	/**
	 * Get all the <code>input</code> tags on the page
	 * 
	 * @return all the <code>input</code> tags on the page
	 */
	public ArrayList<InputTag> getInputFields() {
		return getInputFields(null);
	}

	/**
	 * Get all the <code>input</code> tags on the page with the given type
	 * 
	 * @param type
	 *            the type, e.g. <code>hidden</code>
	 * @return all the <code>input</code> tags on the page with the given type.
	 */
	public ArrayList<InputTag> getInputFields(String type) {
		ArrayList<InputTag> inputFields = new ArrayList<InputTag>();
		for (InputTag tag : inputs) {
			if (type == null) {
				inputFields.add(tag);
			} else {
				if (tag.getType().equalsIgnoreCase(type)) {
					inputFields.add(tag);
				}
			}
		}
		return inputFields;
	}

	/**
	 * The InputStream used for reading the body of the {@link HTTPRequest}.
	 * 
	 * @return the input stream.
	 */
	public InputStream getInputStream() {
		try {
			return new ByteArrayInputStream(body.toString().getBytes("UTF-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Set this HTTP Request to use SSL.
	 * 
	 * @param useSSL
	 *            whether or not SSL should be used.
	 */
	public void useSSL(boolean useSSL) {
		this.useSSL = useSSL;
	}

	/**
	 * Whether or not this HTTP Request is set to use SSL.
	 * 
	 * @return true if SSL is used, false otherwise.
	 */
	public boolean isUsingSSL() {
		return useSSL;
	}

	/**
	 * Get the Redirect object that results from loading this page.
	 * 
	 * @return the Redirect object that results from loading this page.
	 */
	public Redirect getRedirect() {
		return redirect;
	}

	/**
	 * Get the Redirect object that results from loading this page.
	 * 
	 * @param redir
	 *            the Redirect object that results from loading this page.
	 */
	public void setRedirect(Redirect redir) {
		this.redirect = redir;
	}

	/**
	 * Clone this HTTP Request.
	 * <p>
	 * Cloned attributes are: <br/>
	 * HTTP Method <br/>
	 * Page <br/>
	 * Post Data <br/>
	 * Referrer <br/>
	 * Cookies <br/>
	 * SSL Support<br/>
	 * <br/>
	 * 
	 * The body is not copied as the clone is intended to potnetially be re-used.
	 */
	public HTTPRequest clone() {
		HTTPRequest clone = new HTTPRequest(method, page, postFields, referrer, cookies, useSSL);

		// are there any other attributes we want to copy besides the basics?
		return clone;
	}
}
