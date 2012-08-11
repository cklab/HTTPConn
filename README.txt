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

	String buffer;
	HTTPReader rdr = new HTTPConn("example.org"); // send the request to example.org
	Get get = new Get(); // get the home page
	rdr.exec(get);		// execute the request
	
	Scanner in = get.getScanner();	// read the response
	while (in.hasNextLine())
	{
		buffer = in.nextLine();
		// use the result in anyway you need to..	
	}

	



3.2) Sending an HTTP POST request and reading the result
-------------------------------------------------------

	
	String buffer;
	HTTPReader rdr = new HTTPConn("example.org"); // send the request to example.org
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
		buffer = in.nextLine();
		// use the result in anyway you need to..	
	}
	
3.3) Using SSL

    String buffer;
	HTTPReader rdr = new HTTPConn("example.org"); // send the request to example.org
	Get get = new Get(); // get the home page
	get.useSSL(true);
	
	rdr.exec(get);		// execute the request
	
	Scanner in = get.getScanner();	// read the response
	while (in.hasNextLine())
	{
		buffer = in.nextLine();
		// use the result in anyway you need to..	
	}