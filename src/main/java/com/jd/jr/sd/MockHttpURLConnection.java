/**
 * @author liuifengyi
 *  下午2:42:42
 * @version 1.0
 * 文件描述
 */
package com.jd.jr.sd;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

/**
 * 
  * @author liuifengyi
 *  下午2:42:42
 * @version 1.0
 * 类描述
 *  
 */
public class MockHttpURLConnection extends HttpURLConnection {

	private Logger logger = Logger.getLogger(this.getClass().getName());

	private String content;

	/**
	 * @param url
	 */
	public MockHttpURLConnection(URL url, String content) {
		super(url);
		this.content = content;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see java.net.URLConnection#connect()
	 */
	@Override
	public void connect() throws IOException {
		logger.info("MockURLConnection connected");
	}

	@Override
	public InputStream getInputStream() {
		return new ByteArrayInputStream(content.getBytes());
	}

	@Override
	public Object getContent() throws IOException {
		return "getContent" + content;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see java.net.HttpURLConnection#disconnect()
	 */
	@Override
	public void disconnect() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.net.HttpURLConnection#usingProxy()
	 */
	@Override
	public boolean usingProxy() {
		// TODO Auto-generated method stub
		return false;
	}

}
