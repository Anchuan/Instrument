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

import com.alibaba.fastjson.JSON;
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
					String jsonArrayConfigStr = JSON.toJSONString(mockList);
					logger.info("jsonArrayConfigStr pre " + jsonArrayConfigStr);
					jsonArrayConfigStr = "\""
							+ jsonArrayConfigStr.replaceAll("\\\"", "\\\\\"").replaceAll("\\\\\\\\\"", "\\\\\\\\\\\\\"")
							+ "\"";
					logger.info("jsonArrayConfigStr " + jsonArrayConfigStr);
					logger.info("Transforming " + completeMethodName);
					StringBuilder builder = new StringBuilder();
					builder.append(
							"com.jd.jr.sd.httpclient.MockCloseableHttpResponse mockResponse = new com.jd.jr.sd.httpclient.MockCloseableHttpResponse("
									+ jsonArrayConfigStr + ",$1,$2,$3);");
					builder.append("if(mockResponse.isMock()){return mockResponse;}");
					logger.info("mockContent:" + builder.toString());
					method.insertBefore(builder.toString());
				}
			}
		}
		return cl.toBytecode();
	}
}
