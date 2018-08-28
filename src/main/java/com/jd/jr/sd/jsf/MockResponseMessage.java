/**
 * @author liuifengyi
 *  上午9:28:49
 * @version 1.0
 * 文件描述
 */
package com.jd.jr.sd.jsf;

import java.util.logging.Logger;

import com.jd.jsf.gd.msg.BaseMessage;
import com.jd.jsf.gd.msg.ResponseMessage;

import io.netty.buffer.ByteBuf;

/**
 * 
  * @author liuifengyi
 *  上午9:28:49
 * @version 1.0
 * 类描述
 *  
 */
public class MockResponseMessage extends ResponseMessage {

	private String content;
	private Logger logger = Logger.getLogger(MockResponseMessage.class.getName());

	public MockResponseMessage(BaseMessage message, String outParam) {
		logger.info(message.toString());
		ByteBuf msg = message.getMsg();
		logger.info(msg == null ? null : new String(msg.array()));
		ByteBuf msgBody = message.getMsgBody();
		logger.info(msgBody == null ? null : new String(msgBody.array()));
		this.content = outParam;
	}
	@Override
	public Object getResponse() {
		return content;
	}

	@Override
	public boolean isError() {
		return false;
	}
}
