/**
 * @author liuifengyi
 *  下午3:16:09
 * @version 1.0
 * 文件描述
 */
package com.jd.jr.sd;

import java.lang.instrument.Instrumentation;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.util.HotSwapAgent;

/**
 * 
  * @author liuifengyi
 *  下午3:16:09
 * @version 1.0
 * 类描述
 *  
 */
public class RedefineURL {

	private static Logger logger = Logger.getLogger(RedefineURL.class.getName());

	public static void excuteRedefinedMock(String agentArgs, Instrumentation _inst, Class cla,
			List<Map<String, String>> mockList) throws Exception {
		try {
			HotSwapAgent.premain(agentArgs, _inst);
		} catch (Throwable e) {
			logger.log(Level.WARNING, "RedefineURL执行HotSwapAgent异常,不能正常mock", e);
			return;
		}
		ClassPool pool = ClassPool.getDefault();
		CtClass cl = pool.get("java.net.URL");

		CtMethod method = cl.getDeclaredMethod("openConnection");
		// CtMethod method = cl.getDeclaredMethod("openConnection", pool.get(new
		// String[] { "java.net.Proxy" }));
		StringBuilder builder = new StringBuilder();
		builder.append("String url = this.protocol+\"://\"+this.host+this.path;");
		for (Map<String, String> s : mockList) {
			String url = s.get("urlOrInterFacade");
			String mockContent = s.get("outParam");
			if (mockContent != null) {
				mockContent = "\"" + mockContent.replaceAll("\"", "\\\\\"") + "\"";
			}
			String responseCode = s.get("responseCode");
			String responseMessage = s.get("responseMessage");
			String contentType = s.get("contentType");
			String headers = s.get("headers");

			builder.append("if(url.toLowerCase().equalsIgnoreCase(\"" + url.toLowerCase() + "\")) {");
			builder.append("String content=" + mockContent + ";");
			builder.append(
					"com.jd.jr.sd.MockHttpURLConnection mockConnection = new com.jd.jr.sd.MockHttpURLConnection(this,content);");
			if (responseCode != null) {
				builder.append("mockConnection.setResponseCode(\"" + responseCode + "\");");
			}

			if (responseMessage != null) {
				builder.append("mockConnection.setResponseMessage(\"" + responseMessage + "\");");
			}

			if (contentType != null) {
				builder.append("mockConnection.setContentType(\"" + contentType + "\");");
			}
			Object headers_ = JSONArray.parse(headers);
			JSONArray array = (JSONArray) headers_;
			if (array != null) {
				int headerLength = array.size();
				for (int k = 0; k < headerLength; k++) {
					JSONObject header = array.getJSONObject(k);
					Iterator<String> it = header.keySet().iterator();
					while (it.hasNext()) {
						String key = it.next();
						String value = header.getString(key);
						builder.append("mockConnection.addHeader(\"" + key + "\",\"" + value + "\");");
					}
				}
			}
			builder.append("return mockConnection;}");
		}
		logger.info("mockContent" + builder.toString());
		method.insertBefore(builder.toString());
		HotSwapAgent.redefine(cla, cl);
	}

}
