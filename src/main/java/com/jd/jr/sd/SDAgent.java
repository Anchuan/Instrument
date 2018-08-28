/**
 * @author liuifengyi
 *  上午10:45:19
 * @version 1.0
 * 文件描述
 */
package com.jd.jr.sd;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.logging.Logger;

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
		HotSwapAgent.agentmain(agentArgs, _inst);
		// HotSwapAgent.premain(agentArgs, _inst);
		ClassFileTransformer trans = new PerfMonXformer(_inst);
		logger.info("Adding a PerfMonXformer instance to the JVM.");
		_inst.addTransformer(trans);

	}
}
