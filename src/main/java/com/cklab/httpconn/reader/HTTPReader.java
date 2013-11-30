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
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.BindException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

import com.cklab.httpconn.request.Get;
import com.cklab.httpconn.request.HTTPRequest;
import com.cklab.httpconn.util.FormData;
import com.cklab.httpconn.util.Redirect;

/**
 * HTTPReader class.
 * 
 * Used to execute HTTPRequests. Talks to the HTTP Server specified in the constructor.
 * 
 * @author cklab
 * 
 */
public class HTTPReader extends Thread implements Cloneable {

	public static final int					HTTP_SERVICE_UNAVAILABLE	= 503;

	/**
	 * The User-Agent that is sent to the HTTP server.
	 */
	public static String					USER_AGENT					= "HTTPConn for Java";

	private static boolean					DEBUG;

	private String							site;

	private int								port;

	private Proxy							proxy;

	protected HashMap<String, FormData>	cookies;

	private boolean							useProxy;
	private boolean							followRedirects;
	private boolean							handleCookies;

	private HostnameVerifier				hostnameVerifier;

	public HTTPReader() {
		this(null);
	}

	/**
	 * Create an HTTPReader for the given site. on port 80.
	 * 
	 * @param site
	 *            the host to execute HTTPRequests on.
	 */
	public HTTPReader(String site) {
		this(site, 80, true);
	}

	/**
	 * Create an HTTPReader for the given site on port 80.
	 * 
	 * @param site
	 *            the host to execute HTTPRequests on.
	 * @param followRedirects
	 *            whether or not to follow redirects automatically.
	 */

	public HTTPReader(String site, boolean followRedirects) {
		this(site, 80, followRedirects);
	}

	/**
	 * Create an HTTPReader for the given site on the given port
	 * 
	 * @param site
	 *            the host to execute HTTPRequests on.
	 * @param port
	 *            the port for this host
	 */
	public HTTPReader(String site, int port) {
		this(site, port, true);
	}

	/**
	 * Create an HTTPReader for the given site on the given port
	 * 
	 * @param site
	 *            the host to execute HTTPRequests on.
	 * @param port
	 *            the port for this host
	 * @param followRedirects
	 *            whether or not to follow redirects automatically.
	 */
	public HTTPReader(String site, int port, boolean followRedirects) {
		this(site, port, new HashMap<String, FormData>(), followRedirects);
	}

	/**
	 * Create an HTTPReader for the given site on the given port
	 * 
	 * @param site
	 *            the host to execute HTTPRequests on.
	 * @param port
	 *            the port for this host
	 * @param cookies
	 *            the cookies to use for this HTTPReader
	 * @param followRedirects
	 *            whether or not to follow redirects automatically.
	 */
	public HTTPReader(String site, int port, HashMap<String, FormData> cookies, boolean followRedirects) {
		this.site = site;
		this.port = port;
		this.cookies = cookies;
		this.followRedirects = followRedirects;
		this.handleCookies = true;
		this.useProxy = false;
	}

	/**
	 * Enable/disable debugging
	 * 
	 * @param debug
	 *            true if debugging should be enabled, false otherwise.
	 */
	public static void setDebug(boolean debug) {
		DEBUG = debug;
	}

	/**
	 * Execute an HTTPRequest on this host.
	 * 
	 * @param req
	 *            the request to execute.
	 */
	public void exec(HTTPRequest req) {
		exec(req, true);
	}

	/**
	 * Execute an HTTPRequest on this HTTPReader.
	 * 
	 * 
	 * @param req
	 *            The HTTPRequest to execute
	 * @param retry
	 *            whether or not a the request should be re-attempted in case of failure
	 */

	private synchronized void exec(HTTPRequest req, boolean retry) {
		HttpURLConnection conn = null;

		if (req == null) {
			System.err.println("Null Request to exec()");
			return;
		}

		req.setBody(null);

		if (handleCookies) {
			req.setCookies(getCookies());
		}

		try {

			// we manually handle these by building Redirect objects, so the connection should never follow redirects
			HttpURLConnection.setFollowRedirects(false);

			// build a HttpURLConnection for the HTTPRequest we were given
			conn = getHttpURLConnection(req);

			// and we're off!
			conn.connect();

			if (req.getMethod().equals("POST")) {
				// for a POST method, we need to send the post data: do that here
				DataOutputStream oStream = new DataOutputStream(conn.getOutputStream());
				oStream.writeBytes(req.getFormData());
				oStream.flush();
				oStream.close();
			}

			// we should be done with our end of the contract, it's time to parse the response from the HTTP Server
			parseServerResponse(req, conn);

			conn.disconnect();
			conn = null;

		} catch (BindException be) {
			if (conn != null) {
				// System.out.println("Conn: "+conn.getURL());
			}
			be.printStackTrace();
			try {
				// Thread.sleep(5*1000);
			} catch (Exception ex) {
			}
			if (retry)
				exec(req, false);
		} catch (Exception e) {
			e.printStackTrace();
			try {
				// Thread.sleep(5*1000);
			} catch (Exception ex) {
			}
			if (retry)
				exec(req, false);
		}

		req = null;
	}

	/**
	 * After we have setup the connection and sent our request, we will parse the response.
	 * 
	 * This method currently serves a subroutine for {@link #exec(HTTPRequest, boolean)} to populate the
	 * {@link HTTPRequest} with the server's response.
	 * 
	 * @param req
	 * @param conn
	 * @throws IOException
	 */
	private void parseServerResponse(HTTPRequest req, HttpURLConnection conn) throws IOException {

		InputStream iStream = conn.getInputStream();

		// let's tell the HTTPRequest a little about the response
		req.setStatusCode(conn.getResponseCode());
		req.setHeaders(conn.getHeaderFields());

		if (handleCookies) {
			readCookies(req);
		}

		// find out if we had a redirect from this request
		Get redirect = new Get(conn.getHeaderField("Location"));
		if (redirect.getPage() != null) {
			// yup, there's a redirect!
			String host = getSite();
			String page = redirect.getPage();

			if (redirect.getPage().startsWith("https")) {
				redirect.useSSL(true);
			}

			if (redirect.getPage().startsWith("http")) {
				host = getHostFromURI(redirect.getPage());
				page = getPageFromURI(redirect.getPage());
			}
			redirect.setPage(page);

			Redirect redir;

			// here we determine whether the site leads to an external site (hence we cannot use this HTTPReader), or we
			// are staying local
			if (host.equals(getSite()) || host.equals("")) {
				redir = new Redirect(this, redirect);
			} else {
				redir = new Redirect(new HTTPReader(host), redirect);
			}

			req.setRedirect(redir);
		}

		// read the body of the request
		req.readBody(iStream);

		// we should be done with this stream here, release the resource
		if (iStream != null) {
			try {
				iStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (req.getRedirect() != null && followRedirects && !req.getRedirect().isFollowed()) {
			// in case there was a redirect from our request, we will build the entire chain
			followRedirect(req);
		}

	}

	/**
	 * Creates and returns a HttpURLConnection associated with the {@link HTTPRequest}
	 * 
	 * @param req
	 *            the request
	 * @return the appropriate HttpUrlConnection (can be HttpsURLConnection if the {@link HTTPRequest} is using SSL).
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws ProtocolException
	 */
	private HttpURLConnection getHttpURLConnection(HTTPRequest req) throws MalformedURLException, IOException, ProtocolException {
		HttpURLConnection conn;

		// in order to use a URLConnection, we need the protocol in the front: find the correct protocol to use
		String urlStr = site;
		if (!site.startsWith("http")) {
			if (req.isUsingSSL()) {
				urlStr = "https://" + site;
			} else {
				urlStr = "http://" + site;
			}
		}

		// construct the URL object and create the connection
		URL url = new URL(urlStr + "/" + req.getPage());
		if (useProxy && proxy != null) {
			if (req.isUsingSSL()) {
				conn = (HttpsURLConnection) url.openConnection(proxy);
			} else {
				conn = (HttpURLConnection) url.openConnection(proxy);
			}
		} else {
			if (req.isUsingSSL()) {
				conn = (HttpsURLConnection) url.openConnection();
			} else {
				conn = (HttpURLConnection) url.openConnection();
			}
		}

		// for handling SSL Certificates.. if the user specifies a verifier, then we should use it
		if (req.isUsingSSL() && hostnameVerifier != null) {
			HttpsURLConnection sslConnection = (HttpsURLConnection) conn;
			sslConnection.setHostnameVerifier(hostnameVerifier);
		}

		// set up the request -- TODO: do the request properties need further customization by the user?
		conn.setRequestProperty("User-Agent", getUserAgent());
		conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		conn.setRequestProperty("Accept-Language", "en-us,en;q=0.5");
		conn.setRequestProperty("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7");

		// add the user's custom headers
		if (req.getHeadersToSend() != null) {
			for (Entry<String, List<String>> entry : req.getHeadersToSend().entrySet()) {
				for (String value : entry.getValue()) {
					conn.setRequestProperty(entry.getKey(), value);
				}
			}
		}

		if (req.getReferrer() != null) {
			conn.setRequestProperty("Referer", req.getReferrer());
		}

		if (req.getCookies() != null && req.getCookies().length() != 0) {
			conn.setRequestProperty("Cookie", req.getCookies());
		}

		conn.setRequestMethod(req.getMethod());

		// TODO these are not yet customizable, perhaps they should be variables that can be defined by the user
		conn.setConnectTimeout(15 * 1000);
		conn.setReadTimeout(15 * 1000);
		conn.setUseCaches(false);

		// we will handle both in and out
		conn.setDoInput(true);
		conn.setDoOutput(true);

		return conn;
	}

	/**
	 * Read the cookies from an HTTPRequest
	 * 
	 * @param req
	 */
	public synchronized void readCookies(HTTPRequest req) {

		Map<String, List<String>> headers = req.getHeaders();
		if (headers == null) {
			if (DEBUG) {
				System.err.println("readCookies(): NULL headers for " + req.getPage());
			}
			return;
		}
		Iterator<String> it = headers.keySet().iterator();
		ArrayList<String> reversed = new ArrayList<String>();

		while (it.hasNext()) {
			String key = it.next();
			List<String> h = headers.get(key);
			// System.out.println(key);

			if (key != null && key.equals("Set-Cookie")) {
				for (String value : h)
					if (key != null && value != null)
						reversed.add(0, value);
			}
		}

		for (String value : reversed) {
			String cookie_string = value.split(";")[0];

			String cookie_name = cookie_string.split("=")[0];

			// the +1 here accounts for the '=' that was removed by split()
			String cookie_value = cookie_string.substring(cookie_name.length() + 1, cookie_string.length());

			if (cookie_name != null && cookie_value != null) {
				// System.out.println("some cookie: "+cookie_name+" with value "+cookie_value);
				addCookie(cookie_name, cookie_value);
			}
		}
	}

	/**
	 * Set the host to be used for this HTTPReader
	 * 
	 * @param site
	 *            the host
	 */
	public void setSite(String site) {
		this.site = site;
	}

	/**
	 * Set a proxy that can be used to execute HTTPRequests on this HTTPReader.
	 * 
	 * Note: the proxy is not automatically used, see {@link #useProxy(boolean)}
	 * 
	 * @param host
	 *            the proxy host
	 * @param port
	 *            the proxy port
	 * @param PROXY_TYPE
	 *            the proxy type
	 * @see #useProxy(boolean)
	 */
	public void setProxy(String host, int port, Proxy.Type PROXY_TYPE) {
		setProxy(host, port, null, null, PROXY_TYPE);
	}

	/**
	 * Set a proxy that can be used to execute HTTPRequests on this HTTPReader.
	 * 
	 * Note: the proxy is not automatically used, see {@link #useProxy(boolean)}
	 * 
	 * @param host
	 *            the proxy host
	 * @param port
	 *            the proxy port
	 * @param username
	 *            the proxy username
	 * @param password
	 *            the proxy password
	 * @param PROXY_TYPE
	 *            the proxy type
	 * @see #useProxy(boolean)
	 */
	public void setProxy(String host, int port, final String username, final String password, Proxy.Type PROXY_TYPE) {
		if (username != null && password != null) {
			Authenticator.setDefault(new Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, password.toCharArray());
				}
			});

		}

		SocketAddress sa = new InetSocketAddress(host, port);
		this.proxy = new Proxy(PROXY_TYPE, sa);
	}

	/**
	 * Enable use of the set proxy on this connection.
	 * 
	 * @param useProxy
	 *            whether or not proxies should be used.
	 */
	public void useProxy(boolean useProxy) {
		this.useProxy = useProxy;
	}

	/**
	 * Set a proxy that can be used on this HTTPReader.
	 * 
	 * @see #useProxy(boolean)
	 * @param proxy
	 */
	public void setProxy(Proxy proxy) {
		this.proxy = proxy;
	}

	/**
	 * Get the proxy that is set on this HTTPReader.
	 * 
	 * @return the Proxy
	 */
	public Proxy getProxy() {
		return proxy;
	}

	/**
	 * Get the host page used.
	 * 
	 * @return the host page.
	 */
	public String getSite() {
		if (site.endsWith("/"))
			site = site.substring(0, site.length() - 1);
		return site.trim();
	}

	/**
	 * The user agent used on this HTTPReader.
	 * 
	 * @return the user agent
	 */
	public static String getUserAgent() {
		return USER_AGENT;
	}

	/**
	 * Set the user agent to be used.
	 * 
	 * @param ua
	 *            the user agent to be used.
	 */
	public static void setUserAgent(String ua) {
		USER_AGENT = ua;
	}

	/**
	 * Get the value of a particular cookie.
	 * 
	 * @param cookie
	 * @return the value of a particular cookie.
	 */
	public String getCookie(String cookie) {
		FormData fd = cookies.get(cookie);
		if (fd == null)
			return null;
		return fd.getValue();
	}

	/**
	 * Get the cookie string.
	 * 
	 * @return the cookie (=string (each cookie key-value pair is delimited with a semi-colon)
	 */
	public String getCookies() {
		String cookies = "";
		Iterator<String> i = this.cookies.keySet().iterator();
		while (i.hasNext()) {
			String key = i.next();
			FormData fd = this.cookies.get(key);
			cookies += fd.getName() + "=" + fd.getValue() + "; ";
		}
		if (cookies != null && cookies.equals(""))
			cookies = null;
		if (cookies != null) {
			cookies = cookies.substring(0, cookies.length() - 1);
		}

		// System.out.println("Cookies: ["+cookies+"] for ["+this+"]");
		return cookies;
	}

	public ArrayList<FormData> getCookieList() {
		return new ArrayList<FormData>(cookies.values());
	}

	/**
	 * Get the full URI to the given HTTPRequest page on this host.
	 * 
	 * @param req
	 *            the HTTPRequest
	 * @return the full URI to the given HTTPRequest page on this host.
	 */
	public String getAbsoluteURI(HTTPRequest req) {
		String uri;
		if (req.isUsingSSL())
			uri = "https://";
		else
			uri = "http://";

		uri += site + "/" + req.getPage();
		return uri;
	}

	/**
	 * Set whether or not cookies should be handled by the HTTPReader.
	 * 
	 * @param handleCookies
	 *            whether or not cookies should be handled by HTTPReader.
	 */
	public void handleCookies(boolean handleCookies) {
		this.handleCookies = handleCookies;
	}

	/**
	 * Whether or not a proxy is being used on the requests executed.
	 * 
	 * @return true if a proxy is used to execute requests, false otherwise.
	 */
	public boolean isUsingProxy() {
		return useProxy;
	}

	/**
	 * Read the page.
	 * 
	 * @param req
	 *            the request to read
	 * @return true if the page was successfully read, false otherwise.
	 */
	public boolean read(HTTPRequest req) {
		if (req == null)
			return false;

		Scanner in = req.getScanner();
		while (in.hasNextLine()) {
			in.nextLine();
		}
		return true;
	}

	/**
	 * Follow a redirect
	 */
	private void followRedirect(HTTPRequest originalReq) {
		// in the case we get a 503, we do not want to try to execute again..
		if (originalReq.getStatusCode() == HTTP_SERVICE_UNAVAILABLE) {
			return;
		}

		HTTPReader redirRdr = originalReq.getRedirect().getHTTPReader();
		HTTPRequest redirReq = originalReq.getRedirect().getHTTPRequest();

		if (redirRdr.getSite().equals(getSite()) && this != redirRdr) {
			// same cookies for the same site...
			if (handleCookies) {
				// System.out.println("Transfer cookies for redirect");
				// System.out.println("adding cookies: "+getCookies());
				redirRdr.addCookies(getCookies());
			}
			// also transfer the proxy settings
			redirRdr.setProxy(getProxy());
		}

		if (DEBUG) {
			System.out.println("Redirect to: " + redirRdr.getSite() + "/" + redirReq.getPage());
		}

		originalReq.getRedirect().setFollowed(true);
		redirRdr.exec(redirReq);

	}

	/**
	 * Get the host site from the URI.
	 * 
	 * @param uri
	 *            the uri
	 * @return the host
	 */
	public static String getHostFromURI(String uri) {
		String host = "";
		if (uri.charAt(uri.length() - 1) != '/')
			uri += "/";
		Matcher m = Pattern.compile("(?:http|https)://(.*?)(/|\\?)(.+)").matcher(uri);
		if (m.find()) {
			host = m.group(1);
		}
		if (DEBUG)
			System.out.println("getHostFromURI: [" + uri + "]");
		return host;
	}

	/**
	 * Get the page name from the URI.
	 * 
	 * @param uri
	 *            the uri
	 * @return the page name
	 */
	public static String getPageFromURI(String uri) {
		String page = "";
		// if (uri.charAt(uri.length()-1) != '/')
		// uri+="/";
		Matcher m = Pattern.compile("(?:http|https)://(.*?)/(.+)?").matcher(uri);
		if (m.find()) {
			page = m.group(2);
		}
		if (page == null)
			return "";
		if (page.endsWith("/")) {
			// page = page.substring(0, page.length()-1);
		}
		if (DEBUG)
			System.out.println("getPageFromURI: [" + page + "]");
		return page;
	}

	/**
	 * Add a cookie to be used.
	 * 
	 * @param key
	 *            the cookie name
	 * @param value
	 *            the cookie value
	 */
	public void addCookie(String key, String value) {
		if (key == null || value == null) {
			if (DEBUG) {
				System.err.println("Failed to add cookie, null value: " + key + "=" + value);
			}
			return;
		}

		if (value.equals("deleted")) {
			// TODO: verify this behavior, so far there haven't been any problems -- should the expiration date be used
			// in conjunction with this value?
			// update: just a value of `deleted` seems to be working fine so far...
			if (DEBUG) {
				System.out.println("Delete Cookie: " + key);
			}
			cookies.remove(key);
		} else {
			if (DEBUG) {
				System.out.println("Add cookie " + key + "=" + value);
			}

			FormData cookie = new FormData(key.trim(), value.trim());
			if (!cookie.invalid()) {
				cookies.put(key, cookie);
			}
		}
	}

	/**
	 * Remove a cookie.
	 * 
	 * @param key
	 *            the name of the cookie
	 */
	public void deleteCookie(String key) {
		if (key == null) {
			return;
		}

		if (DEBUG) {
			System.out.println("Del cookie: " + key);
		}

		cookies.remove(key);
	}

	/**
	 * Add the given String of cookies
	 * 
	 * @param cookies
	 *            the cookies, delimited by a semi-colon.
	 */
	public void addCookies(String cookies) {
		if (cookies == null || cookies.length() == 0) {
			if (DEBUG) {
				System.err.println("Null cookies in addCoookies()");
			}
			return;
		}

		String[] cookie = cookies.split(";");

		for (int i = 0; i < cookie.length; i++) {
			String[] params = cookie[i].split("=");
			if (params.length < 2) {
				continue;
			}

			StringBuilder sb = new StringBuilder(params[1].trim());
			// reconstruct the value portion in case we the cookie had a value with any '='s in it
			for (int a = 2; a < params.length; a++) {
				sb.append("=");
				sb.append(params[a].trim());
			}

			addCookie(params[0].trim(), sb.toString());
		}
	}

	/**
	 * Clone this HTTPReader.
	 */
	public HTTPReader clone() {
		return new HTTPReader(site, port, (HashMap<String, FormData>) cookies.clone(), followRedirects);
	}

	/**
	 * Set whether or not redirects should be automatically followed.
	 * 
	 * @param redir
	 *            whether or not redirects should be automatically followed.
	 */
	public void setFollowRedirects(boolean redir) {
		this.followRedirects = redir;
	}

	/**
	 * Define a {@link HostnameVerifier}.
	 * 
	 * @param hostnameVerifier
	 */
	public void setHostnameVerifier(HostnameVerifier hostnameVerifier) {
		this.hostnameVerifier = hostnameVerifier;
	}
}
