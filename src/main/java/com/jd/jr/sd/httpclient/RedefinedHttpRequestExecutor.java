/**
 * @author liuifengyi
 *  下午6:08:55
 * @version 1.0
 * 文件描述
 */
package com.jd.jr.sd.httpclient;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

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
//					builder.append("String url = httpUriRequest.getURI().toString();");
					builder.append("System.out.println(\"request----url\"+url);");
					logger.info("Transforming param " + mockList);
					for (Map<String, String> s : mockList) {
						String url = s.get("urlOrInterFacade");
						String mockContent = s.get("outParam");
						builder.append("if(url.startsWith(\"" + url + "\")) {");
						builder.append(
								"return new com.jd.jr.sd.httpclient.MockCloseableHttpResponse(\"" + mockContent
										+ "\");}");
					}
					logger.info("mockContent" + builder.toString());
					method.insertBefore(builder.toString());
				}
			}
		}
		return cl.toBytecode();
	}
}
