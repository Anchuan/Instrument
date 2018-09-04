/**
 * @author liuifengyi
 *  上午9:34:05
 * @version 1.0
 * 文件描述
 */
package com.jd.jr.sd.jsf;

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
 *  上午9:34:05
 * @version 1.0
 * 类描述
 *  
 */
public class RedefineClientProxyInvoker {

	private static Logger logger = Logger.getLogger(RedefineURL.class.getName());

	public static byte[] excute(CtClass cl, List<Map<String, String>> mockList) throws Exception {
		logger.info("redefine:" + cl);
		CtMethod[] methods = cl.getMethods();
		for (int i = 0; i < methods.length; i++) {
			if (methods[i].isEmpty() == false) {
				CtMethod method = methods[i];
				String methodName = method.getName();
				String completeMethodName = "com.jd.jsf.gd.client.ClientProxyInvoker".replaceAll("\\.", "/") + "/"
						+ methodName;
				if (completeMethodName
						.equals("com.jd.jsf.gd.client.ClientProxyInvoker".replaceAll("\\.", "/") + "/invoke")) {
					logger.info("Transforming " + completeMethodName);
					String jsonArrayConfigStr = JSON.toJSONString(mockList);
					logger.info("jsonArrayConfigStr pre " + jsonArrayConfigStr);
					jsonArrayConfigStr = "\""
							+ jsonArrayConfigStr.replaceAll("\\\"", "\\\\\"").replaceAll("\\\\\\\\\"", "\\\\\\\\\\\\\"")
							+ "\"";

					StringBuilder builder = new StringBuilder();
					logger.info("Transforming param " + mockList);
					builder.append(
							"com.jd.jr.sd.jsf.MockResponseMessage mockResponse = new com.jd.jr.sd.jsf.MockResponseMessage($1,"
									+ jsonArrayConfigStr + ");");
					builder.append("if(mockResponse.isMock()){return mockResponse;}");
					logger.info(builder.toString());
					method.insertBefore(builder.toString());
				}
			}
		}
		return cl.toBytecode();
	}
}
