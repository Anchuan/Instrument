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
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jd.jr.sd.jsf.RedefineClientProxyInvoker;

public class PerfMonXformer implements ClassFileTransformer {

	private List<ClassLoader> loaderList = new ArrayList<ClassLoader>();

	private Logger logger = Logger.getLogger("System.err");

	public static Map<String, List<Map<String, String>>> mockList = new HashMap<String, List<Map<String, String>>>();

	private Instrumentation _inst;

	private String agentArgs;

	public PerfMonXformer(String agentArgs, Instrumentation _inst) {
		this.agentArgs = agentArgs;
		this._inst = _inst;
	}

	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
			ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

		byte[] transformed = null;
		try {
			if (loader != null && !loaderList.contains(loader)) {
				logger.info("new loader:" + className + "," + loader);
				synchronized (this) {
					if (!loaderList.contains(loader)) {
						logger.info("new loader :" + className + "," + loader);
						logger.info(loader + " load properties");
						boolean parsed = ParseMockFile.parse(mockList, loader);
						// 读到配置文件的时候。可能相关配置class 已经读取了。 需要走重定义类
						if (parsed) {
							Class<?>[] array = _inst.getAllLoadedClasses();
							logger.info("current loaded class length:" + array.length);
							for (Class<?> cla : array) {
								if (mockList.containsKey("http")) {
									this.mockRedefineHttp(loader, cla);
									this.mockRedefineHttpClient(loader, cla);
								}
								if (mockList.containsKey("jsf")) {
									this.mockReDefinedJsf(loader, cla);
								}
							}
						}
						loaderList.add(loader);
					}
				}

			}
			
			// 这边是实时读取class mock的情况
			if (transformed == null && mockList.containsKey("http")) {
				transformed = this.mockHttp(loader, className, classfileBuffer);
			}
			if (transformed == null && mockList.containsKey("jsf")) {
				transformed = this.mockJsf(loader, className, classfileBuffer);
			}

		} catch (Exception e) {
			logger.log(Level.WARNING, "exception", e);
		}
		return transformed;
	}

	private byte[] mockHttp(ClassLoader loader, String className, byte[] classfileBuffer)
			throws Exception {
		byte[] transformed = null;
		if ("org.apache.http.protocol.HttpRequestExecutor".replaceAll("\\.", "/").equals(className)) {
			logger.log(Level.INFO, "mockHttp mockHttp redefine className " + className);
			Class<?> redefinedHttpRequestExecutorClass = loader
					.loadClass("com.jd.jr.sd.httpclient.RedefinedHttpRequestExecutor");
			Method method = redefinedHttpRequestExecutorClass.getDeclaredMethod("excuteMock", byte[].class, List.class);
			logger.log(Level.INFO, "RedefineURL method " + method);
			transformed = (byte[]) method.invoke(null,
					new Object[] { classfileBuffer, mockList.get("http") });

			// transformed = RedefinedHttpRequestExecutor.excuteMock(classfileBuffer,
			// mockList.get("http"));
		}
		return transformed;
	}



	private void mockRedefineHttp(ClassLoader loader, Class<?> cla) throws Exception {

		String name = cla.getName();
		if (name.equals("java.net.URL")) {
			logger.log(Level.INFO, "redefine className " + name);
			Class<?> redefineURLClass = loader.loadClass("com.jd.jr.sd.RedefineURL");
			Method method = redefineURLClass.getDeclaredMethod("excuteRedefinedMock", String.class,
					Instrumentation.class, Class.class, List.class);
			logger.log(Level.INFO, "RedefineURL method " + method);
			method.invoke(null, new Object[] { this.agentArgs, this._inst, cla, mockList.get("jsf") });
			// RedefineURL.excute1(this.agentArgs, this._inst, cla, mockList.get("http"));
			logger.log(Level.INFO, "HotSwapAgent className " + cla);
		}
	}

	private void mockRedefineHttpClient(ClassLoader loader, Class<?> cla) throws Exception {
		String name = cla.getName();
		if (name.equals("org.apache.http.protocol.HttpRequestExecutor")) {
			logger.log(Level.INFO, "redefine className " + name);
			Class<?> redefineURLClass = loader.loadClass("com.jd.jr.sd.httpclient.RedefinedHttpRequestExecutor");
			Method method = redefineURLClass.getDeclaredMethod("excuteRedefinedMock", String.class,
					Instrumentation.class, Class.class, List.class);
			logger.log(Level.INFO, "mockRedefineHttpClient method " + method);
			method.invoke(null, new Object[] { this.agentArgs, this._inst, cla, mockList.get("http") });
		}

	}

	private byte[] mockJsf(ClassLoader loader, String className, byte[] classfileBuffer) throws Exception {
		byte[] transformed = null;
		if ("com.jd.jsf.gd.client.ClientProxyInvoker".replaceAll("\\.", "/").equals(className)) {
			logger.log(Level.INFO, "redefine className " + className);
			Class<?> redefineClientProxyInvokerClass = loader.loadClass("com.jd.jr.sd.jsf.RedefineClientProxyInvoker");
			Method method = redefineClientProxyInvokerClass.getDeclaredMethod("excuteMock", byte[].class, List.class);
			logger.log(Level.INFO, "RedefineClientProxyInvoker method " + method);
			transformed = (byte[]) method.invoke(null, new Object[] { classfileBuffer, mockList.get("jsf") });
			// transformed = RedefineClientProxyInvoker.excute(cl, mockList.get("jsf"));
		}
		return transformed;
	}

	private void mockReDefinedJsf(ClassLoader loader, Class<?> cla) throws Exception {
		String name = cla.getName();
		if (name.equals("com.jd.jsf.gd.client.ClientProxyInvoker")) {
			Class<?> redefineURLClass = loader.loadClass("com.jd.jr.sd.jsf.RedefineClientProxyInvoker");
			Method method = redefineURLClass.getDeclaredMethod("excuteRedefinedMock", String.class,
					Instrumentation.class, Class.class, List.class);
			logger.log(Level.INFO, "mockRedefineHttpClient method " + method);
			method.invoke(null, new Object[] { this.agentArgs, this._inst, cla, mockList.get("http") });
			RedefineClientProxyInvoker.excuteRedefinedMock(this.agentArgs, this._inst, cla, mockList.get("jsf"));
		}

	}
}