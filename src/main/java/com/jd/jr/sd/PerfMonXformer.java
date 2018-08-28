/**
 * @author liuifengyi
 *  上午10:46:20
 * @version 1.0
 * 文件描述
 */
package com.jd.jr.sd;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jd.jr.sd.httpclient.RedefinedHttpRequestExecutor;
import com.jd.jr.sd.jsf.RedefineClientProxyInvoker;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.util.HotSwapAgent;

public class PerfMonXformer implements ClassFileTransformer {

	private List<ClassLoader> loaderList = new ArrayList<ClassLoader>();

	// {
	// ParseMockFile.parse(mockList, PerfMonXformer.class.getClassLoader());
	// }

	private Logger logger = Logger.getLogger("System.err");

	public static Map<String, List<Map<String, String>>> mockList = new HashMap<String, List<Map<String, String>>>();
	// static {
	// ParseMockFile.parse(mockList, SDAgent.class.getClassLoader());
	// }

	private Instrumentation _inst;


	public PerfMonXformer(Instrumentation _inst) {
		this._inst = _inst;
	}

	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

		byte[] transformed = null;
		ClassPool pool = ClassPool.getDefault();
		CtClass cl = null;
		try {
			if (loader != null && !loaderList.contains(loader)) {
				logger.info("class nnnnnnn:" + className + "," + loader);
				synchronized (this) {
					if (!loaderList.contains(loader)) {
						logger.info("class nnnnnnn:" + className + "," + loader);
						logger.info(loader + " load properties");
						boolean parsed = ParseMockFile.parse(mockList, loader);
						// 读到配置文件的时候。可能相关配置class 已经读取了。 需要走重定义类
						if (parsed) {
							Class[] array = _inst.getAllLoadedClasses();
							logger.info("current loaded class length:" + array.length);
							for (Class cla : array) {
								this.mockRedefineHttp(cla);
								this.mockReDefinedJsf(cla);
							}
						}
						loaderList.add(loader);
					}
				}

			}
			
			// 这边是实时读取class mock的情况
			transformed = this.mockHttp(transformed, className, pool, cl, classfileBuffer);
			transformed = this.mockJsf(transformed, className, pool, cl, classfileBuffer);

		} catch (Exception e) {
			logger.log(Level.WARNING, "exception", e);
		} finally {
			if (cl != null) {
				cl.detach();
			}
		}
		return transformed;
	}

	private byte[] mockHttp(byte[] transformed, String className, ClassPool pool, CtClass cl, byte[] classfileBuffer)
			throws Exception {
		if (mockList.containsKey("http")) {
			if ("org.apache.http.protocol.HttpRequestExecutor".replaceAll("\\.", "/").equals(className)) {
				logger.log(Level.INFO, "mockHttp mockHttp redefine className " + className);
				cl = pool.makeClass(new java.io.ByteArrayInputStream(classfileBuffer));
				transformed = RedefinedHttpRequestExecutor.excute(cl, mockList.get("http"));
			}
		}
		return transformed;
	}

	private byte[] mockJsf(byte[] transformed, String className, ClassPool pool, CtClass cl, byte[] classfileBuffer)
			throws Exception {
		if (mockList.containsKey("jsf")) {
			if ("com.jd.jsf.gd.client.ClientProxyInvoker".replaceAll("\\.", "/").equals(className)) {
				logger.log(Level.INFO, "redefine className " + className);
				cl = pool.makeClass(new java.io.ByteArrayInputStream(classfileBuffer));
				transformed = RedefineClientProxyInvoker.excute(cl, mockList.get("jsf"));
			}
		}
		return transformed;
	}

	private void mockRedefineHttp(Class cla) throws Exception {
		if (mockList.containsKey("http")) {
			String name = cla.getName();
			if (name.equals("java.net.URL")) {
				logger.log(Level.INFO, "redefine className " + name);
				ClassPool pool = ClassPool.getDefault();
				CtClass cl = pool.get(name);
				RedefineURL.excute(cl, mockList.get("http"));
				logger.log(Level.INFO, "HotSwapAgent className " + cl);
				HotSwapAgent.redefine(cla, cl);
				logger.log(Level.INFO, "HotSwapAgent className " + cla);
			}
			if ("org.apache.http.protocol.HttpRequestExecutor".equals(name)) {
				logger.log(Level.INFO, "redefine className " + name);
				ClassPool pool = ClassPool.getDefault();
				CtClass cl = pool.get(name);
				RedefinedHttpRequestExecutor.excute(cl, mockList.get("http"));
			}
		}
	}

	private void mockReDefinedJsf(Class cla) throws Exception {
		if (mockList.containsKey("jsf")) {
			String name = cla.getName();
			if (name.equals("com.jd.jsf.gd.client.ClientProxyInvoker")) {
				ClassPool pool = ClassPool.getDefault();
				CtClass cl = pool.get(name);
				RedefineClientProxyInvoker.excute(cl, mockList.get("jsf"));
				HotSwapAgent.redefine(cla, cl);
			}
		}
	}
}