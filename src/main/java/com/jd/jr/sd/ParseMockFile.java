/**
 * @author liuifengyi
 *  下午4:56:49
 * @version 1.0
 * 文件描述
 */
package com.jd.jr.sd;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * 
  * @author liuifengyi
 *  下午4:56:49
 * @version 1.0
 * 类描述
 *  
 */
public class ParseMockFile {
	
	private static List<String> supportProtocalList = Arrays.asList("http", "httpclient", "jsf");

	private static List<String> fileList = new ArrayList<String>();

	private static Logger logger = Logger.getLogger(ParseMockFile.class.getName());
	
	public static boolean parse(Map<String, List<Map<String, String>>> mockList, ClassLoader loader) {
		String fileName = "sd-mock.properties";
		URL url = loader.getResource(fileName);
		if (url != null) {
			logger.info("begin read properties file-name:" + fileName);
			logger.info("properties url:" + url);
			if (fileList.contains(url.toString())) {
				return false;
			}

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
								try {
									List<Map<String, String>> protocolMockList = mockList.get(protocol);
									if (protocolMockList == null) {
										protocolMockList = new ArrayList<Map<String, String>>();
										mockList.put(protocol, protocolMockList);
									}
									String urlOrInterFacade = parameterItems[1];
									String inParam = parameterItems[2];
									String outParam = parameterItems[3];
									Object obj = JSONObject.parse(outParam);
									logger.info("out param :" + obj.getClass());
									if (!(obj instanceof JSONObject)) {
										throw new RuntimeException("MOCK 的内容必须是JSON结构化数据,本条Mock无效:" + str);

									}
									JSONObject mockContent = (JSONObject)obj;
									// http协议进一步解析header、contentType
									Map<String, String> mock = new HashMap<String, String>();
									if ("http".equals(protocol)) {
										String contentType = mockContent.getString("contentType");
										if (contentType != null) {
											mock.put("contentType", contentType.replaceAll("\"", "\\\\\""));
										}

										String headers = mockContent.getString("headers");
										headers = headers == null ? "[]" : headers;
										Object headers_ = JSONArray.parse(headers);
										if (!(headers_ instanceof JSONArray)) {
											logger.warning("header 必须配置为json数组形式,当前header配置无效" + headers);
										}else {
											mock.put("headers", headers);
										}

										String responseCode = mockContent.getString("responseCode");
										if (responseCode != null) {
											mock.put("responseCode", responseCode);
										}
										String responseMessage = mockContent.getString("responseMessage");
										if (responseMessage != null) {
											mock.put("responseMessage", responseMessage.replaceAll("\"", "\\\\\""));
										}
										outParam = mockContent.getString("outContent");
									} else {
										outParam = obj.toString();
									}
									if (outParam != null) {
										// outParam = outParam.replaceAll("\"", "\\\\\"");
										// outParam = "\"" + outParam + "\"";
										mock.put("outParam", outParam);
									}

									mock.put("urlOrInterFacade", urlOrInterFacade);
									mock.put("inParam", inParam);

									protocolMockList.add(mock);
								} catch (Exception e) {
									logger.log(Level.SEVERE, "本条数据解析异常,配置无效" + str, e);
									e.printStackTrace();
								}
							}
						}
					}
					str = reader.readLine();
					logger.info("mock.properties str:" + str);
				}
				reader.close();
				fileList.add(url.toString());
				return true;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		return false;
	}

}
