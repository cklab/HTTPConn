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
package com.cklab.httpconn.reader;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.BindException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import com.cklab.httpconn.request.Get;
import com.cklab.httpconn.request.HTTPRequest;
import com.cklab.httpconn.util.FormData;
import com.cklab.httpconn.util.Redirect;


/**
 * HTTPReader class.
 * 
 * Used to execute HTTPRequests. Talks to the HTTP Server specified in the constructor.
 * @author cklab
 *
 */
public class HTTPReader extends Thread {

	public static final int HTTP_SERVICE_UNAVAILABLE = 503;

	/**
	 * The User-Agent that is sent to the HTTP server.
	 */
	public static String USER_AGENT = "HTTPConn for Java";


	private static boolean DEBUG;
	private String site;
	private int port;
	private boolean useProxy;
	private Proxy proxy;

	private HTTPRequest lastRequest;
	protected Hashtable<String,FormData> cookies;
	private boolean followRedirects;
	private boolean handleCookies;


	private HostnameVerifier hostnameVerifier;

	public HTTPReader()
	{
		this(null);
	}

	/**
	 * Create an HTTPReader for the given site. on port 80.
	 * @param site the host to execute HTTPRequests on.
	 */
	public HTTPReader(String site)
	{
		this(site,80, true);
	}

	/**
	 * Create an HTTPReader for the given site on port 80.
	 * @param site the host to execute HTTPRequests on.
	 * @param followRedirects whether or not to follow redirects automatically.
	 */

	public HTTPReader(String site,  boolean followRedirects)
	{
		this(site,80, followRedirects);
	}

	/**
	 * Create an HTTPReader for the given site on the given port
	 * @param site the host to execute HTTPRequests on.
	 * @param port the port for this host
	 */
	public HTTPReader(String site, int port)
	{
		this(site,port, true);
	}

	/**
	 * Create an HTTPReader for the given site on the given port
	 * @param site the host to execute HTTPRequests on.
	 * @param port the port for this host
	 * @param followRedirects whether or not to follow redirects automatically.
	 */
	public HTTPReader(String site, int port, boolean followRedirects)
	{
		this(site, port, new Hashtable<String, FormData>(), followRedirects);

	}

	/**
	 * Create an HTTPReader for the given site on the given port
	 * @param site the host to execute HTTPRequests on.
	 * @param port the port for this host
	 * @param cookies the cookies to use for this HTTPReader
	 * @param followRedirects whether or not to follow redirects automatically.
	 */
	public HTTPReader(String site, int port, Hashtable<String,FormData> cookies,  boolean followRedirects)
	{
		this.site = site;
		this.port = port;
		this.cookies = cookies;
		this.followRedirects = followRedirects;
		this.handleCookies = true;
		this.useProxy = false;
	}


	/**
	 * Enable/disable debugging
	 * @param debug true if debugging should be enabled, false otherwise.
	 */
	public static void setDebug(boolean debug)
	{
		DEBUG = debug;
	}

	/**
	 * Execute an HTTPRequest on this host.
	 * @param req the request to execute.
	 */
	public void exec(HTTPRequest req)
	{
		exec(req, true);
	}

	/**
	 * Execute an HTTPRequest on this host.
	 * 
	 * 
	 * @param req The HTTPRequest to execute
	 * @param tryagain whether or not a second request should be attempted
	 */

	private synchronized void exec(HTTPRequest req, boolean tryagain)
	{
		HttpURLConnection conn = null;
		if (req == null) 
		{
			System.err.println("Null Request to exec()");
			return;
		}

		req.setBody(null);
		lastRequest = req;
		//System.out.println("exec: ["+req.getPage()+"] class: ["+this+"]");
		/*System.out.println("["+DateFormat.getDateTimeInstance(3,2).format(new Date().getTime())+"]"+
				"exec(): do cookies");*/
		if (handleCookies) 
			req.setCookies(getCookies());

		try {
			HttpURLConnection.setFollowRedirects(false);
			//String str_url = (site.startsWith("http://")?"":"http://")+site;
			String str_url = site;
			if (!site.startsWith("http"))
			{
				if (req.isUsingSSL())
					str_url = "https://"+site;
				else
					str_url = "http://"+site;
			}
			/*else {
				if (site.startsWith("https://")) {

				}
			}*/


			URL url = new URL(str_url+"/"+req.getPage());
			if (useProxy && proxy != null)
			{
				if (req.isUsingSSL())
					conn = (HttpsURLConnection)url.openConnection(proxy);
				else
					conn = (HttpURLConnection)url.openConnection(proxy);
			} else {
				if (req.isUsingSSL())
					conn = (HttpsURLConnection)url.openConnection();
				else 
					conn = (HttpURLConnection)url.openConnection();
			}

			if (req.isUsingSSL() && hostnameVerifier != null) {
				HttpsURLConnection sslConnection = (HttpsURLConnection)conn;
				sslConnection.setHostnameVerifier(hostnameVerifier);
			}


			// settings to look liek firefox..
			conn.setRequestProperty("User-Agent", getUserAgent());
			conn.setRequestProperty("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			conn.setRequestProperty("Accept-Language","en-us,en;q=0.5");
			conn.setRequestProperty("Accept-Charset","ISO-8859-1,utf-8;q=0.7,*;q=0.7");
			if (req.getReferrer() != null)
				conn.setRequestProperty("Referer",req.getReferrer());
			if (req.getCookies() != null && !req.getCookies().equals(""))
				conn.setRequestProperty("Cookie",req.getCookies());
			conn.setRequestMethod(req.getMethod());


			conn.setConnectTimeout(15*1000);
			conn.setReadTimeout(15*1000);
			conn.setUseCaches (false);
			conn.setDoInput(true);
			conn.setDoOutput(true);


			/*System.out.println("["+DateFormat.getDateTimeInstance(3,2).format(new Date().getTime())+"]"+
			"exec(): connect");*/

			conn.connect();


			/*System.out.println("["+DateFormat.getDateTimeInstance(3,2).format(new Date().getTime())+"]"+
			"exec(): connected");*/
			if (req.getMethod().equals("POST")) {
				/*System.out.println("["+DateFormat.getDateTimeInstance(3,2).format(new Date().getTime())+"]"+
				"exec(): Send the POST");*/
				DataOutputStream oStream = new DataOutputStream(conn.getOutputStream());
				oStream.writeBytes(req.getFormData());
				oStream.flush();
				oStream.close();
				/*System.out.println("["+DateFormat.getDateTimeInstance(3,2).format(new Date().getTime())+"]"+
				"exec(): POST sent");*/
			}


			InputStream iStream = conn.getInputStream();
			/*	System.out.println("["+DateFormat.getDateTimeInstance(3,2).format(new Date().getTime())+"]"+
				"exec(): got inputstream");*/
			//OutputStream oStream = conn.getOutputStream();

			req.setInputStream(iStream);
			req.setStatusCode(conn.getResponseCode());
			req.setHeaders(conn.getHeaderFields());

			/*System.out.println("["+DateFormat.getDateTimeInstance(3,2).format(new Date().getTime())+"]"+
			"exec(): read request body");*/

			if (handleCookies)
				readCookies(req);


			Get redirect = new Get(conn.getHeaderField("Location"));
			//System.out.println("Location: ["+conn.getHeaderField("Location")+"]");
			if (redirect.getPage() != null)
			{

				String host = getSite();
				String page = redirect.getPage();
				if (redirect.getPage().startsWith("https"))
				{
					redirect.useSSL(true);
				}

				if (redirect.getPage().startsWith("http"))
				{

					host = getHostFromURI(redirect.getPage());
					page = getPageFromURI(redirect.getPage());
				}


				redirect.setPage(page);

				/*System.out.println("Redirect to: ["+host+"] page: ["+page+"] redirect: ["
						+redirect.getPage()+"] from: ["+lastRequest.getPage()+"]");*/
				Redirect redir;

				//System.out.println("host is: ["+host+"] and i'm ["+getSite()+"]");
				if (host.equals(getSite()) || host.equals("")) {
					redir = new Redirect(this, redirect);
				} else
					redir = new Redirect(new HTTPReader(host), redirect);

				lastRequest.setRedirect(redir);
			}
			//System.out.println(hasRedirect() +"&&"+ followRedirects);


			if (hasRedirect() && followRedirects)
			{
				// we first want to get the headers (maybe there's a cookie..?)
				followRedirect();
				req.setBody(getRedirect().getHTTPRequest().getBody());
				req.setInputStream(lastRequest.getRedirect().getHTTPRequest().getInputStream());
				//lastRequest.setRedirect(null);

			} else {
				req.readBody();
			}

			//if (conn != null)
			//	conn.disconnect();

			if (iStream != null)
				iStream.close();
			conn.disconnect();
			iStream = null;
			conn = null;

		} catch (NullPointerException npe) {
			npe.printStackTrace();
		} catch (BindException be) {
			if (conn != null) {
				//				System.out.println("Conn: "+conn.getURL());
			}
			be.printStackTrace();
			try {
				Thread.sleep(5*1000);	
			} catch (Exception ex) { }
			if (tryagain)
				exec(req, false); 
		}catch (Exception e) {
			e.printStackTrace();
			try {
				Thread.sleep(5*1000);	
			} catch (Exception ex) { }
			if (tryagain)
				exec(req, false); 
		}

		req.die();
		req = null;
	}

	/**
	 * Read the cookies from this HTTPRequest
	 * @param req
	 */
	public synchronized void readCookies(HTTPRequest req)
	{

		Map<String, List<String>> headers = req.getHeaders();
		if (headers == null)
		{
			if (DEBUG)
				System.err.println("readCookies(): NULL headers for "+lastRequest.getPage());
			return;
		}
		Iterator<String> it = headers.keySet().iterator();
		ArrayList<String> reversed = new ArrayList<String>();

		while (it.hasNext())
		{
			String key = it.next();
			List<String> h = headers.get(key);
			//System.out.println(key);

			if (key != null && key.equals("Set-Cookie"))
			{
				for (String value : h)
					if (key != null && value != null) 
						reversed.add(0,value);
			}
		}

		for (String value : reversed)
		{
			String cookie_string = value.split(";")[0];

			String cookie_name = cookie_string.split("=")[0];

			// the +1 here accounts for the '=' that was removed by split()
			String cookie_value = cookie_string.substring(cookie_name.length()+1, cookie_string.length());


			if (cookie_name != null && cookie_value != null) {
				//System.out.println("some cookie: "+cookie_name+" with value "+cookie_value);
				addCookie(cookie_name, cookie_value);
			}
		}
	}

	/**
	 * Set the host to be used for this HTTPReader
	 * @param site the host
	 */
	public void setSite(String site)
	{
		this.site = site;
	}

	/**
	 * Set a proxy that can be used to execute HTTPRequests on this HTTPReader.
	 * 
	 * Note: the proxy is not automatically used, see {@link #useProxy(boolean)}
	 * @param host the proxy host
	 * @param port the proxy port
	 * @param PROXY_TYPE the proxy type
	 * @see #useProxy(boolean)
	 */
	public void setProxy(String host, int port, Proxy.Type PROXY_TYPE)
	{
		setProxy(host, port, null, null, PROXY_TYPE);
	}

	/**
	 * Set a proxy that can be used to execute HTTPRequests on this HTTPReader.
	 * 
	 * Note: the proxy is not automatically used, see {@link #useProxy(boolean)}
	 * @param host the proxy host
	 * @param port the proxy port
	 * @param username the proxy username
	 * @param password the proxy password
	 * @param PROXY_TYPE the proxy type
	 * @see #useProxy(boolean)
	 */
	public void setProxy(String host, int port, final String username, final String password, Proxy.Type PROXY_TYPE)
	{
		if (username != null && password != null)
		{
			Authenticator.setDefault(new Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new
							PasswordAuthentication(username,password.toCharArray());
				}});

		}

		SocketAddress sa = new InetSocketAddress(host, port);
		this.proxy = new Proxy(PROXY_TYPE, sa);
	}

	/**
	 * Enable use of the set proxy on this connection.
	 * @param useProxy whether or not proxies should be used.
	 */
	public void useProxy(boolean useProxy)
	{
		this.useProxy = useProxy;
	}

	/**
	 * Set a proxy that can be used on this HTTPReader.
	 * @see #useProxy(boolean)
	 * @param proxy
	 */
	public void setProxy(Proxy proxy)
	{
		this.proxy = proxy;

	}

	/**
	 * Get the proxy that is set on this HTTPReader.
	 * @return the Proxy
	 */
	public Proxy getProxy()
	{
		return proxy;
	}

	/**
	 * Get the host page used.
	 * @return the host page.
	 */
	public String getSite()
	{
		if (site.endsWith("/"))
			site = site.substring(0, site.length()-1);
		return site.trim();
	}

	/**
	 * The user agent used on this HTTPReader.
	 * @return the user agent
	 */
	public static String getUserAgent()
	{
		return USER_AGENT;
	}

	/**
	 * Set the user agent to be used. 
	 * @param ua the user agent to be used.
	 */
	public static void setUserAgent(String ua)
	{
		USER_AGENT = ua;
	}

	/**
	 * Get the value of a particular cookie.
	 * @param cookie
	 * @return the value of a particular cookie.
	 */
	public String getCookie(String cookie)
	{
		FormData fd = cookies.get(cookie);
		if (fd == null) return null;
		return fd.getValue();
	}

	/**
	 * Get the cookie string.
	 * @return the cookie (=string (each cookie key-value pair is delimited with a semi-colon) 
	 */
	public String getCookies()
	{
		String cookies = "";
		Iterator<String> i = this.cookies.keySet().iterator();
		while (i.hasNext())
		{
			String key = i.next();
			FormData fd = this.cookies.get(key);
			cookies+=fd.getName()+"="+fd.getValue()+"; ";
		}
		if (cookies != null && cookies.equals(""))
			cookies = null;
		if (cookies != null) 
		{
			cookies = cookies.substring(0, cookies.length()-1);
		}

		//System.out.println("Cookies: ["+cookies+"] for ["+this+"]");
		return cookies;
	}

	/**
	 * Get the full URI to the given HTTPRequest page on this host.
	 * @param req the HTTPRequest
	 * @return the full URI to the given HTTPRequest page on this host.
	 */
	public String getAbsoluteURI(HTTPRequest req)
	{
		String uri;
		if (req.isUsingSSL())
			uri = "https://";
		else
			uri = "http://";

		uri+=site+"/"+req.getPage();
		return uri;
	}

	/**
	 * Set whether or not cookies should be handled by the HTTPReader. 
	 * @param handleCookies whether or not cookies should be handled by HTTPReader.
	 */
	public void handleCookies(boolean handleCookies)
	{
		this.handleCookies = handleCookies;
	}

	/**
	 * Get the last Redirect this HTTPReader saw.
	 * @return the last Redirect this HTTPReader saw.
	 */
	public Redirect getRedirect()
	{
		return lastRequest.getRedirect();
	}

	/**
	 * Whether or not there was a redirect in the last request that was executed.
	 * @return true if the last request had a redirect, false otherwise.
	 */
	private boolean hasRedirect()
	{
		return (lastRequest.getRedirect() != null);
	}

	/**
	 * Whether or not a proxy is being used on the requests executed.
	 * @return true if a proxy is used to execute requests, false otherwise. 
	 */
	public boolean isUsingProxy()
	{
		return useProxy;
	}

	/**
	 * Read the page.
	 * @param req the request to read
	 * @return true if the page was successfully read, false otherwise.
	 */
	public boolean read(HTTPRequest req)
	{
		if (req == null)
			return false;

		Scanner in = req.getScanner();
		while (in.hasNextLine())
		{
			in.nextLine();
		}
		return true;
	}

	/**
	 * Follow a redirect
	 */
	private void followRedirect()
	{
		//we want to save the request from which the redirect came because
		//after we execute the redirect, the redirect becomes the "lastRequest"
		//This will cause getRedirect() to fail
		HTTPRequest lastRequest = this.lastRequest;
		
		// in the case we get a 503, we do not want to try to execute again..
		if (this.lastRequest.getStatusCode() == HTTP_SERVICE_UNAVAILABLE) {
			return;
		}
		
		HTTPReader rdr = getRedirect().getHTTPReader();
		HTTPRequest req = getRedirect().getHTTPRequest();

		//System.out.println("redir:["+rdr.getSite()+"] me:["+getSite()+"]");
		if (rdr.getSite().equals(getSite()))
		{
			//same cookies for the same site...

			if (handleCookies)
			{

				//System.out.println("Transfer cookies for redirect");
				//System.out.println("adding cookies: "+getCookies());
				rdr.addCookies(getCookies());
			}

			// also transfer the proxy settings
			rdr.setProxy(getProxy());
		}
		if (DEBUG)
			System.out.println("Redirect to: "+rdr.getSite()+"/"+req.getPage());

		//System.out.println(getPageFromURI(val));
		rdr.exec(req);
		this.lastRequest = lastRequest;
	}

	/**
	 * Get the host site from the URI.
	 * @param uri the uri
	 * @return the host
	 */
	public static String getHostFromURI(String uri)
	{
		String host = "";
		if (uri.charAt(uri.length()-1) != '/')
			uri+="/";
		Matcher m = Pattern.compile("(?:http|https)://(.*?)(/|\\?)(.+)").matcher(uri);
		if (m.find())
		{
			host = m.group(1);
		}
		if (DEBUG)
			System.out.println("getHostFromURI: ["+uri+"]");
		return host;
	}

	/**
	 * Get the page name from the URI.
	 * @param uri the uri
	 * @return the page name
	 */
	public static String getPageFromURI(String uri)
	{
		String page = "";
		//if (uri.charAt(uri.length()-1) != '/')
		//	uri+="/";
		Matcher m = Pattern.compile("(?:http|https)://(.*?)/(.+)?").matcher(uri);
		if (m.find())
		{
			page = m.group(2);
		}
		if (page == null)
			return "";
		if (page.endsWith("/")) {
			//page = page.substring(0, page.length()-1);
		}
		if (DEBUG)
			System.out.println("getPageFromURI: ["+page+"]");
		return page;
	}

	/**
	 * Add a cookie to be used.
	 * @param key the cookie name
	 * @param value the cookie value
	 */
	public void addCookie(String key, String value)
	{
		if (key == null || value == null)
		{

			if (DEBUG)
				System.err.println("Failed to add cookie, null value");
			return;
		}

		if (value.equals("deleted")) {
			if (DEBUG)
				System.out.println("Delete Cookie: "+key);
			cookies.remove(key);
		} else {
			//System.out.println("Add cookie: ["+key+"="+value+"] for ["+this+"]");
			//System.out.println("Add cookie "+key+"="+value);
			FormData cookie = new FormData(key.trim(),value.trim());
			if (!cookie.invalid())
				cookies.put(key, cookie);
		}
	}

	/**
	 * Remove a cookie.
	 * @param key the name of the cookie
	 */
	public void delCookie(String key)
	{
		if (key == null)
			return;
		System.out.println("Del cookie: "+key);
		cookies.remove(key);
	}

	/**
	 * Add the given String of cookies
	 * @param cookies the cookies, delimited by a semi-colon.
	 */
	public void addCookies(String cookies)
	{
		if (cookies == null || cookies.equals(""))
		{
			//System.err.println("Null cookies in addCoookies()");
			return;
		}
		String[] cookie = cookies.split(";");
		for (int i =0;i<cookie.length;i++)
		{
			String[] params = cookie[i].split("=");
			if (params.length < 2) continue;


			String value =params[1].trim();

			for (int a = 2; a< params.length;a++)
			{
				value+="="+params[a].trim();
			}
			addCookie(params[0].trim(), value);
		}
	}

	/**
	 * Clone this HTTPReader.
	 */
	public HTTPReader clone()
	{
		return new HTTPReader(site, port, (Hashtable<String, FormData>)cookies.clone(), followRedirects);
	}

	/**
	 * Set whether or not redirects should be automatically followed.
	 * @param redir whether or not redirects should be automatically followed.
	 */
	public void setFollowRedirects(boolean redir)
	{
		this.followRedirects = redir;
	}

	
	public void setHostnameVerifier(HostnameVerifier hostnameVerifier) {
		this.hostnameVerifier = hostnameVerifier;
	}
}
