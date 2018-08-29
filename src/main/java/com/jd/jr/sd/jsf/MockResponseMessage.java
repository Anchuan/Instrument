/**
 * @author liuifengyi
 *  上午9:28:49
 * @version 1.0
 * 文件描述
 */
package com.jd.jr.sd.jsf;

import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.alibaba.fastjson.JSON;
import com.jd.jsf.gd.msg.BaseMessage;
import com.jd.jsf.gd.msg.Invocation;
import com.jd.jsf.gd.msg.RequestMessage;
import com.jd.jsf.gd.msg.ResponseMessage;

/**
 * 
  * @author liuifengyi
 *  上午9:28:49
 * @version 1.0
 * 类描述
 *  
 */
public class MockResponseMessage extends ResponseMessage {

	private Object obj;
	private Logger logger = Logger.getLogger(MockResponseMessage.class.getName());

	private boolean isMock;

	public boolean isMock() {
		return isMock;
	}

	public MockResponseMessage(BaseMessage message, String outParam, String methodStr) {
		RequestMessage requestMessage = (RequestMessage) message;
		Invocation body = requestMessage.getInvocationBody();
		String className = body.getClazzName();
		String methodName = body.getMethodName();
		String methodStr_ = className + "." + methodName;
		if (methodStr_.equals(methodStr)) {
			logger.info("jsf mock:" + methodStr_);
			Class cla = null;
			try {
				cla = Class.forName(className);
				Method method = cla.getMethod(methodName, body.getArgClasses());
				Class returnClass = method.getReturnType();
				Object obj = JSON.parseObject(outParam, returnClass);
				this.obj = obj;
				isMock = true;
			} catch (Exception e) {
				logger.log(Level.WARNING, "处理数据异常", e);
			}
		}


	}
	@Override
	public Object getResponse() {
		return obj;
	}

	@Override
	public boolean isError() {
		return false;
	}
}
