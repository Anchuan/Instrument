/**
 * @author liuifengyi
 *  上午10:46:20
 * @version 1.0
 * 文件描述
 */
package com.jd.jr.sd;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jd.jr.sd.httpclient.RedefinedHttpRequestExecutor;
import com.jd.jr.sd.jsf.RedefineClientProxyInvoker;

import javassist.ClassPool;
import javassist.CtClass;

public class PerfMonXformer implements ClassFileTransformer {

	private Map<String, List<Map<String, String>>> mockList;

	private Logger logger = Logger.getLogger("System.err");

	public PerfMonXformer(Map<String, List<Map<String, String>>> mockList) {
		this.mockList = mockList;
	}

	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

		
		byte[] transformed = null;

		ClassPool pool = ClassPool.getDefault();
		CtClass cl = null;

		try {
			// logger.info("className----" + className);
			
			if(mockList.containsKey("httpclient")) {
				if ("org.apache.http.protocol.HttpRequestExecutor".replaceAll("\\.", "/").equals(className)) {
					logger.log(Level.INFO, "redefine className " + className);
					cl = pool.makeClass(new java.io.ByteArrayInputStream(classfileBuffer));
					transformed = RedefinedHttpRequestExecutor.excute(cl, mockList.get("httpclient"));
				}
			}
			

			// logger.info("className----" + className);
			if (mockList.containsKey("jsf")) {
				if ("com.jd.jsf.gd.client.ClientProxyInvoker".replaceAll("\\.", "/").equals(className)) {
					logger.log(Level.INFO, "redefine className " + className);
					cl = pool.makeClass(new java.io.ByteArrayInputStream(classfileBuffer));
					transformed = RedefineClientProxyInvoker.excute(cl, mockList.get("jsf"));
				}
			}

		} catch (Exception e) {
			logger.log(Level.WARNING, "exception", e);
		} finally {
			if (cl != null) {
				cl.detach();
			}
		}
		return transformed;
	}
}