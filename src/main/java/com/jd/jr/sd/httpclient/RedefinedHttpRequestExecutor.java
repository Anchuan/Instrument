/**
 * @author liuifengyi
 *  下午6:08:55
 * @version 1.0
 * 文件描述
 */
package com.jd.jr.sd.httpclient;

import java.lang.instrument.Instrumentation;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.alibaba.fastjson.JSON;
import com.jd.jr.sd.RedefineURL;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.util.HotSwapAgent;

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

	public static byte[] excuteMock(byte[] classfileBuffer, List<Map<String, String>> mockList) throws Exception {
		ClassPool pool = ClassPool.getDefault();
		CtClass cl = pool.makeClass(new java.io.ByteArrayInputStream(classfileBuffer));
		excute(cl, mockList);
		byte[] transformed = cl.toBytecode();
		cl.detach();
		return transformed;
	}

	public static void excuteRedefinedMock(String agentArgs, Instrumentation _inst, Class cla,
			List<Map<String, String>> mockList)
			throws Exception {
		ClassPool pool = ClassPool.getDefault();
		CtClass cl = pool.get("org.apache.http.protocol.HttpRequestExecutor");
		// CtClass cl = pool.get(cla.getName());
		if (cl != null) {
			try {
				HotSwapAgent.premain(agentArgs, _inst);
				excute(cl, mockList);
				cl.detach();
				HotSwapAgent.redefine(cla, cl);
			} catch (Throwable e) {
				logger.log(Level.WARNING, "RedefinedHttpRequestExecutor执行HotSwapAgent异常,不能正常mock", e);
				return;
			}
		}
	}

	private static void excute(CtClass cl, List<Map<String, String>> mockList) throws Exception {
		CtMethod method = cl.getDeclaredMethod("doSendRequest");
		String jsonArrayConfigStr = JSON.toJSONString(mockList);
		logger.info("jsonArrayConfigStr pre " + jsonArrayConfigStr);
		jsonArrayConfigStr = "\""
				+ jsonArrayConfigStr.replaceAll("\\\"", "\\\\\"").replaceAll("\\\\\\\\\"", "\\\\\\\\\\\\\"") + "\"";
		logger.info("jsonArrayConfigStr " + jsonArrayConfigStr);
		StringBuilder builder = new StringBuilder();
		builder.append(
				"com.jd.jr.sd.httpclient.MockCloseableHttpResponse mockResponse = new com.jd.jr.sd.httpclient.MockCloseableHttpResponse("
						+ jsonArrayConfigStr + ",$1,$2,$3);");
		builder.append("if(mockResponse.isMock()){return mockResponse;}");
		logger.info("mockContent:" + builder.toString());
		method.insertBefore(builder.toString());
	}
}
