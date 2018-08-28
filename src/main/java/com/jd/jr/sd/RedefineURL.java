/**
 * @author liuifengyi
 *  下午3:16:09
 * @version 1.0
 * 文件描述
 */
package com.jd.jr.sd;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import javassist.CtClass;
import javassist.CtMethod;

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

	public static byte[] excute(CtClass cl, List<Map<String, String>> mockList) throws Exception {
		CtMethod[] methods = cl.getMethods();
		for (int i = 0; i < methods.length; i++) {
			if (methods[i].isEmpty() == false) {
				CtMethod method = methods[i];
				String methodName = method.getName();
				String completeMethodName = "java/net/URL" + "/" + methodName;
				if (completeMethodName.equals("java/net/URL/openConnection")) {
					logger.info("Transforming " + completeMethodName);
					StringBuilder builder = new StringBuilder();
					builder.append("String url = this.protocol+\"://\"+this.host+this.path;");
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
						builder.append("return mockConnection;}");
					}
					logger.info("mockContent" + builder.toString());
					method.insertBefore(builder.toString());
				}
			}
		}
		return cl.toBytecode();
	}


}
