/**
 * @author liuifengyi
 *  下午3:16:09
 * @version 1.0
 * 文件描述
 */
package com.jd.jr.sd;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

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
						builder.append("if(url.startsWith(\"" + url + "\")) {");
						builder.append("return new com.jd.jr.sd.httpclient.MockHttpURLConnection(this,\"" + mockContent
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
