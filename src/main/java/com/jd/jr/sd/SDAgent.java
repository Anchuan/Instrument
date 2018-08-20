/**
 * @author liuifengyi
 *  上午10:45:19
 * @version 1.0
 * 文件描述
 */
package com.jd.jr.sd;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.util.HotSwapAgent;

/**
 * 
  * @author liuifengyi
 *  上午10:45:19
 * @version 1.0
 * 类描述
 *  
 */
public class SDAgent {

	private static Logger logger = Logger.getLogger(SDAgent.class.getName());

	private static List<String> supportProtocalList = Arrays.asList("http", "httpclient", "jsf");

	public static Map<String, List<Map<String, String>>> mockList = new HashMap<String, List<Map<String, String>>>();
	static {
		String fileName = "/sd-mock.properties";
		logger.info("begin read properties file-name:" + fileName);
		URL url = ClassFileTransformer.class.getResource(fileName);
		if (url != null) {
			logger.info("begin read properties file-name path:" + url.toString());
			try {

				BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
				String str = reader.readLine();
				logger.info("mock.properties str:" + str);
				while (str != null) {
					if (str.startsWith("#")) {
						logger.info("str:" + str + "是注释，忽略");
					} else {
						String[] parameterItems = str.split("\\t|\\s");
						if (parameterItems.length != 4) {
							logger.info("str:" + str + "格式不对 忽略");
						} else {
							String protocol = parameterItems[0];
							if (!supportProtocalList.contains(protocol)) {
								logger.info("str:" + str + "协议不支持 忽略");
							} else {
								List<Map<String, String>> protocolMockList = mockList.get(protocol);
								if (protocolMockList == null) {
									protocolMockList = new ArrayList<Map<String, String>>();
									mockList.put(protocol, protocolMockList);
								}
								String urlOrInterFacade = parameterItems[1];
								String inParam = parameterItems[2];
								String outParam = parameterItems[3];
								Map<String, String> mock = new HashMap<String, String>();
								mock.put("urlOrInterFacade", urlOrInterFacade);
								mock.put("inParam", inParam);
								mock.put("outParam", outParam);
								protocolMockList.add(mock);
							}

						}
					}
					str = reader.readLine();
					logger.info("mock.properties str:" + str);
				}
				reader.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	/**
	 * This method is called before the application’s main-method is called, when
	 * this agent is specified to the Java VM.
	 * 
	 * @throws Throwable
	 **/
	public static void premain(String agentArgs, Instrumentation _inst) throws Throwable {
		logger.info("PerfMonAgent.premain() was called.");
		// Initialize the static variables we use to track information.
		// Set up the class-file transformer.
		logger.info("getAllLoadedClassesLength----" + _inst.getAllLoadedClasses().length);
		logger.info(
				"getInitiatedClassesLength----" + _inst.getInitiatedClasses(null).length);
		HotSwapAgent agent = new HotSwapAgent();
		agent.premain(agentArgs, _inst);
		ClassFileTransformer trans = new PerfMonXformer(mockList);
		logger.info("Adding a PerfMonXformer instance to the JVM.");
		_inst.addTransformer(trans);

		if (mockList.containsKey("http")) {
			Class[] array = _inst.getAllLoadedClasses();
			for (Class cla : array) {
				String name = cla.getName();
				if (name.equals("java.net.URL")) {
					ClassPool pool = ClassPool.getDefault();
					CtClass cl = pool.get(name);
					RedefineURL.excute(cl, mockList.get("http"));
					agent.redefine(cla, cl);
				}
			}
		}


	}
}
