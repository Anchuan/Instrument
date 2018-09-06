/**
 * @author liuifengyi
 *  上午9:34:05
 * @version 1.0
 * 文件描述
 */
package com.jd.jr.sd.jsf;

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
 *  上午9:34:05
 * @version 1.0
 * 类描述
 *  
 */
public class RedefineClientProxyInvoker {

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
		CtClass cl = pool.get("com.jd.jsf.gd.client.ClientProxyInvoker");
		// CtClass cl = pool.get(cla.getName());
		if (cl != null) {
			try {
				HotSwapAgent.premain(agentArgs, _inst);
				excute(cl, mockList);
				cl.detach();
				HotSwapAgent.redefine(cla, cl);
			} catch (Throwable e) {
				logger.log(Level.WARNING, "RedefineClientProxyInvoker 执行HotSwapAgent异常,不能正常mock", e);
				return;
			}
		}
	}

	private static void excute(CtClass cl, List<Map<String, String>> mockList) throws Exception {
		CtMethod method = cl.getDeclaredMethod("invoke");
		String jsonArrayConfigStr = JSON.toJSONString(mockList);
		logger.info("jsonArrayConfigStr pre " + jsonArrayConfigStr);
		jsonArrayConfigStr = "\""
				+ jsonArrayConfigStr.replaceAll("\\\"", "\\\\\"").replaceAll("\\\\\\\\\"", "\\\\\\\\\\\\\"") + "\"";

		StringBuilder builder = new StringBuilder();
		logger.info("Transforming param " + mockList);
		builder.append(
				"com.jd.jr.sd.jsf.MockResponseMessage mockResponse = new com.jd.jr.sd.jsf.MockResponseMessage($1,"
						+ jsonArrayConfigStr + ");");
		builder.append("if(mockResponse.isMock()){return mockResponse;}");
		logger.info("mock content:" + builder.toString());
		method.insertBefore(builder.toString());
	}
}
