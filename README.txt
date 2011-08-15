#-------------------------------------------------------------------------------
# Copyright (c) 2011 CK Lab.
# 
# This file is part of HTTPConn.
# 
# HTTPConn is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
# 
# HTTPConn is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
# 
# You should have received a copy of the GNU General Public License
# along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
#-------------------------------------------------------------------------------
HTTPConn 
======================

1) Intro
--------
HTTPConn is a Java library that makes it easy to send and receive HTTP Packets.


2) Installation
---------------

Place HTTPConn.jar into your project directory and add the JAR to your project build path.


3) Examples
-----------

3.1) Sending an HTTP GET request and reading the result
-------------------------------------------------------

	String buf;
	HTTPReader rdr = new HTTPConn("example.org"); // send the request to example.org
	Get get = new Get(""); // get the home page
	rdr.exec(get);		// execute the request
	
	Scanner in = get.getScanner();	// read the response
	while (in.hasNextLine())
	{
		buf = in.nextLine();
		// use the result in anyway you need to..	
	}

	get.die(); 	// clean up
	



3.2) Sending an HTTP POST request and reading the result
-------------------------------------------------------

	
	String buf;
	HTTPReader rdr = new HTTPConn("example.org"); // send the request to google.com
	Post post = new Post("index.php"); // post to page 
	FormData[] fd = {
		new FormData("key1", "value1"),
		new FormData("key2", "value2")
	};
	post.setFormData(fd);
	rdr.exec(post);		// execute the request
	
	Scanner in = post.getScanner();	// read the response
	while (in.hasNextLine())
	{
		buf = in.nextLine();
		// use the result in anyway you need to..	
	}

	post.die(); 	// clean up
