/**
 * @author liuifengyi
 *  下午6:08:55
 * @version 1.0
 * 文件描述
 */
package com.jd.jr.sd.httpclient;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jd.jr.sd.RedefineURL;

import javassist.CtClass;
import javassist.CtMethod;

/**
 * 
  * @author liuifengyi
 *  下午6:08:55
 * @version 1.0
 * 类描述
 *  
 */
public class RedefinedHttpRequestExecutor {
	private static Logger logger = Logger.getLogger(RedefineURL.class.getName());

	public static byte[] excute(CtClass cl, List<Map<String, String>> mockList)
			throws Exception {
		CtMethod[] methods = cl.getMethods();
		for (int i = 0; i < methods.length; i++) {
			if (methods[i].isEmpty() == false) {
				CtMethod method = methods[i];
				String methodName = method.getName();
				String completeMethodName = "org/apache/http/protocol/HttpRequestExecutor" + "/" + methodName;
				if (completeMethodName.equals("org/apache/http/protocol/HttpRequestExecutor/doSendRequest")) {
					logger.info("Transforming " + completeMethodName);
					StringBuilder builder = new StringBuilder();
					builder.append(
							"org.apache.http.client.methods.HttpRequestWrapper httpRequestWrapper = (org.apache.http.client.methods.HttpRequestWrapper)$1;");
					builder.append(
							"org.apache.http.HttpRequest httpRequest = httpRequestWrapper.getOriginal();");
					builder.append(
							"org.apache.http.client.methods.HttpRequestBase httpRequestBase = (org.apache.http.client.methods.HttpRequestBase)httpRequest;");
					 builder.append("java.net.URL url_ = httpRequestBase.getURI().toURL();");
					builder.append("String url = url_.getProtocol()+\"://\"+url_.getHost()+url_.getPath();");
					builder.append("System.out.println(\"request----url\"+url);");
					logger.info("Transforming param " + mockList);
					for (Map<String, String> s : mockList) {
						String url = s.get("urlOrInterFacade");
						String mockContent = s.get("outParam");
						String responseCode = s.get("responseCode");
						String responseMessage = s.get("responseMessage");
						String contentType = s.get("contentType");
						String headers = s.get("headers");
						builder.append("if(url.toLowerCase().startsWith(\"" + url.toLowerCase() + "\")) {");
						builder.append("String content=" + mockContent + ";");
						builder.append(
								"com.jd.jr.sd.httpclient.MockCloseableHttpResponse mockResponse = new com.jd.jr.sd.httpclient.MockCloseableHttpResponse(content);");
						if (responseCode != null) {
							if (responseMessage == null) {
								responseMessage = "";
							}
							builder.append("mockResponse.setResponseCode(\"" + responseCode + "\",\"" + responseMessage
									+ "\");");
						}

						if (contentType != null) {
							builder.append("mockResponse.setContentType(\"" + contentType + "\");");
						}

						Object headers_ = JSONArray.parse(headers);
						JSONArray array = (JSONArray) headers_;
						int headerLength = array.size();
						for (int k = 0; k < headerLength; k++) {
							JSONObject header = array.getJSONObject(k);
							Iterator<String> it = header.keySet().iterator();
							while (it.hasNext()) {
								String key = it.next();
								String value = header.getString(key);
								builder.append("mockResponse.addHeader(\"" + key + "\",\"" + value + "\");");
							}
						}

						builder.append(
								"return mockResponse;}");
					}
					logger.info("mockContent" + builder.toString());
					method.insertBefore(builder.toString());
				}
			}
		}
		return cl.toBytecode();
	}
}
