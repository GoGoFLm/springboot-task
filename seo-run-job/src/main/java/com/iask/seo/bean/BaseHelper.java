package com.iask.seo.bean;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;


/**
 * 
 * <p>
 * 原创度接口查询
 * </p>
 * 
 * @author flm
 * @date 2018年8月24日
 */
public class BaseHelper implements Serializable {

	private static final long serialVersionUID = -1514590519059608193L;
	
	private static final String URL_M = "https://m.baidu.com/s?pn=%s&word=%s";
	private static final String URL = "http://www.baidu.com/s?wd=%s&pn=0&rn=%s&tn=json&rqlang=cn&rsv_enter=0&rsv_sug=1";
	private static final Logger logger = LoggerFactory.getLogger(BaseHelper.class);
	private static final String IASK_URL = "/iask.sina.com.cn";

	private static final String REGFILE = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？\"]";
	private final static String regxpForHtml = "<([^>]*)>"; // 过滤所有以<开头以>结尾的标签

	private static String[] UA = {
			"Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.133 Safari/537.36",
			"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36",
			"Mozilla/5.0 (X11; Linux x86_64; rv:2.0b9pre) Gecko/20110111 Firefox/4.0b9pre",
			"Mozilla/5.0 (Windows NT 5.1; rv:2.0b9pre) Gecko/20110105 Firefox/4.0b9pre",
			"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.87 Safari/537.36 OPR/37.0.2178.32",
			"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.57.2 (KHTML, like Gecko) Version/5.1.7 Safari/534.57.2",
			"Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.133 Safari/537.36",
			"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 BIDUBrowser/8.3 Safari/537.36",
			"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.80 Safari/537.36 Core/1.47.277.400 QQBrowser/9.4.7658.400",
			"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.2125.122 Safari/537.36 SE 2.X MetaSr 1.0",
			"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.154 Safari/537.36 LBBROWSER", };

	/**
	 * pc
	 * 获取 站点的排名【前50】
	 * 
	 * @param questionTxt
	 * @return
	 */
	public static Map<String, Map<String, String>> getRank(String questionTxt) {
		Map<String, Map<String, String>> map = new HashMap<>();
		map.put("iask", null);
		map.put("ishare", null);
		try {
			String keyWord = questionTxt;

			// 【1】 过滤掉空格 换行符
			keyWord = keyWord.replaceAll(" ", "").replaceAll("	", "");
			String result = postBaidu(keyWord.trim(), "50");
			JSONArray array = null;
			String url = "";
			try {
				JSONObject jsonObject = null;
				try {
					jsonObject = JSONObject.parseObject(result);
				} catch (Exception e) {
					logger.error("解析json 出错... e:");
				}

				if (jsonObject == null || jsonObject.getJSONObject("feed") == null
						|| jsonObject.getJSONObject("feed").getJSONArray("entry") == null) {
				} else {
					boolean iaskOk = true;
					boolean ishareOk = true;
					array = jsonObject.getJSONObject("feed").getJSONArray("entry");
					for (int i = 0; i < array.size(); i++) {
						try {
							JSONObject json = array.getJSONObject(i);
							if (json == null || json.isEmpty()) {
								continue;
							}
							url = json.get("url").toString();
							if (url.equals("https://iask.sina.com.cn/") || url.equals("https://iask.sina.com.cn")) {
								continue;
							}
							if (url.equals("http://ishare.iask.sina.com.cn/")
									|| url.equals("http://ishare.iask.sina.com.cn")) {
								continue;
							}
							if (url.contains("/iask.sina.com.cn") && iaskOk) {
								Map<String, String> iask = new HashMap<>();
								iask.put("url", url);
								iask.put("pn", json.getString("pn"));
								map.put("iask", iask);
								iaskOk = false;
							} else if (url.contains("/ishare.iask.sina.com.cn") && ishareOk) {
								Map<String, String> ishare = new HashMap<>();
								ishare.put("url", url);
								ishare.put("pn", json.getString("pn"));
								map.put("ishare", ishare);
								ishareOk = false;
							} else if (!ishareOk && !iaskOk) {
								break;
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}

			} catch (Exception e1) {
				e1.printStackTrace();
			}
		} catch (Exception e) {
			logger.error("getRank error...", e);
		}
		return map;
	}
	
	
	/**
	 * m
	 * 获取 站点的排名
	 * 
	 * @param questionTxt 关键词
	 * @param page 		    页数
	 * @return
	 */
	public static Map<String, Map<String, String>> getRankM(String questionTxt , int page) {
		Map<String, Map<String, String>> map = new HashMap<>();
		map.put("iask", null);
		map.put("ishare", null);
		try {
			String keyWord = questionTxt;

			// 【1】 过滤掉空格 换行符
			keyWord = keyWord.replaceAll(" ", "").replaceAll("	", "");
			
			sendGetM(URL_M, keyWord, map , page);
		} catch (Exception e) {
			logger.error("getRank error...", e);
		}
		return map;
	}
	
	
	/**
	 * 得到 检索量得分
	 * 
	 * @return
	 */
	public static float getSearchValue(int pc, int m) {
		float value = 0;
		float v = 0;
		try {

			value = pc * 0.4f + m * 0.6f;

			if (value >= 0 && value < 1) {
				v = 0;
			} else if (value >= 1 && value < 5) {
				v = 1;
			} else if (value >= 5 && value < 10) {
				v = 2;
			} else if (value >= 10 && value < 20) {
				v = 3;
			} else if (value >= 20 && value < 50) {
				v = 4;
			} else if (value >= 50 && value < 100) {
				v = 5;
			} else if (value >= 100 && value < 200) {
				v = 6;
			} else if (value >= 200 && value < 500) {
				v = 7;
			} else if (value >= 500 && value < 1000) {
				v = 8;
			} else if (value >= 1000 && value < 2000) {
				v = 9;
			} else if (value >= 2000) {
				v = 10;
			}

		} catch (Exception e) {
			logger.error("getSearchAll 解析json 出错 error....", e);
		}
		return v;
	}

	/**
	 * 得到 收录量的得分
	 * 
	 * @return
	 */
	public static int getSearchAll(String questionTxt) {
		int value = 0;
		int v = 0;
		try {
			String keyWord = questionTxt;
			// 【1】 过滤掉空格 换行符
			keyWord = keyWord.replaceAll(" ", "").replaceAll("	", "");
			String result = postBaidu(keyWord.trim(), "1");
			JSONObject jsonObject = null;
			jsonObject = JSONObject.parseObject(result);
			if (jsonObject == null || jsonObject.getJSONObject("feed") == null
					|| jsonObject.getJSONObject("feed").getJSONArray("entry") == null) {
				return 0;
			}
			value = jsonObject.getJSONObject("feed").getIntValue("all");

			if (value >= 0 && value <= 10000) {
				v = 10;
			} else if (value > 10000 && value <= 50000) {
				v = 9;
			} else if (value > 50000 && value <= 100000) {
				v = 8;
			} else if (value > 100000 && value <= 200000) {
				v = 7;
			} else if (value > 200000 && value <= 300000) {
				v = 6;
			} else if (value > 300000 && value <= 500000) {
				v = 5;
			} else if (value > 500000 && value <= 800000) {
				v = 4;
			} else if (value > 800000 && value <= 1000000) {
				v = 3;
			} else if (value > 1000000 && value <= 5000000) {
				v = 2;
			} else if (value > 5000000 && value <= 10000000) {
				v = 1;
			} else if (value > 10000000) {
				v = 0;
			}
		} catch (Exception e) {
			logger.error("getSearchAll 解析json 出错 error....", e);
		}
		return v;
	}

	/**
	 * 获取 问我原创度
	 * 
	 * @param questionTxt
	 * @param collection
	 * @return
	 */
	public static float getWenwoOriginality(String questionTxt) {

		// 获取 问题的 前10 url
		List<String> urls = getWenwoUrls(questionTxt);
		int n = 0;
		int siteWeightscore = 0;
		float weightVaule = 0;
		int siteV = 0;
		if (urls != null && urls.size() > 0) {
			for (String url : urls) {

				String uString = url.split("/")[2];
				int s = url.split("/").length - 3;

				if (url.split("/").length < 3) { // url 末端 / 的 去掉
					s = s - 1;
				}
				if (s == 0) {
					siteV += 1;
				} else if (s == 1) {
					siteV += 2;
				} else if (s == 2) {
					siteV += 3;
				} else if (s == 3) {
					siteV += 4;
				} else {
					siteV += 5;
				}

				String v = "-1";

				DBObject qObject = new BasicDBObject();
				qObject.put("_id", uString);

				v = getURLRang(uString);
				
				if (!v.equals("-1")) {
					n++;
					int vSize = Integer.valueOf(v);
					int urlV = 0;

					if (url.contains("baidu.com")) { // 过滤 百度
						urlV = 0;
					} else {
						if (vSize == 0) {
							urlV = 10;
						} else if (vSize == 1) {
							urlV = 9;
						} else if (vSize == 2) {
							urlV = 8;
						} else if (vSize == 3) {
							urlV = 7;
						} else if (vSize == 4) {
							urlV = 6;
						} else if (vSize == 5) {
							urlV = 5;
						} else if (vSize == 6) {
							urlV = 4;
						} else if (vSize == 7) {
							urlV = 3;
						} else if (vSize == 8) {
							urlV = 2;
						} else if (vSize == 9) {
							urlV = 1;
						} else if (vSize == 10) {
							urlV = 0;
						}
					}

					siteWeightscore += urlV;
				}
			}
		}
		siteWeightscore += siteV;
		if (n != 0) {
			weightVaule = siteWeightscore / Float.valueOf(n + "");
		}

		return weightVaule;
	}

	// -----------------------------------------------------------------------------------------//
	// -----------------------------------------------------------------------------------------//
	// --------------------------------- 帮助类
	// ----------------------------------------------//
	// -----------------------------------------------------------------------------------------//
	// -----------------------------------------------------------------------------------------//

	/**
	 * 获取 问题的 url
	 * 
	 * @param question
	 * @return
	 */
	private static List<String> getWenwoUrls(String questionTxt) {
		List<String> urls = new ArrayList<>();
		try {
			String keyWord = questionTxt;

			// 【1】 过滤掉空格 换行符
			keyWord = keyWord.replaceAll(" ", "").replaceAll("	", "");
			String result = postBaidu(keyWord.trim(), "10");

			JSONArray array = null;
			String url = "";
			try {
				JSONObject jsonObject = null;
				try {
					jsonObject = JSONObject.parseObject(result);
				} catch (Exception e) {
					logger.error("解析json 出错... e:");
				}

				if (jsonObject == null || jsonObject.getJSONObject("feed") == null
						|| jsonObject.getJSONObject("feed").getJSONArray("entry") == null) {
					return null;
				}
				array = jsonObject.getJSONObject("feed").getJSONArray("entry");
				for (int i = 0; i < array.size(); i++) {
					try {
						JSONObject json = array.getJSONObject(i);
						if (json == null || json.isEmpty()) {
							continue;
						}
						url = json.get("url").toString();
						urls.add(url);
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return urls;
	}

	/**
	 * 获取 url 的 排名
	 * <p>
	 * https://baidurank.aizhan.com/api/br?domain=388g.com&style=images
	 * </p>
	 * 
	 * @param url
	 * @return
	 */
	private static String getURLRang(String url) {
		// logger.info("getURLRang ...");
		if (url == null || url == "") {
			return "-1";
		}
		BufferedReader in = null;
		try {
			URL realUrl = new URL("https://baidurank.aizhan.com/api/br?domain="
					+ url.replaceAll("https://", "").replaceAll("http://", "") + "&style=images");
			// 打开和URL之间的连接
			URLConnection conn = realUrl.openConnection();
			// 设置通用的请求属性
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
			// 建立实际的连接
			conn.connect();
			// 获取所有响应头字段
			Map<String, List<String>> map = conn.getHeaderFields();

			String result = map.get("Location") + "";
			if (result.contains("png")) {
				String[] aStrings = result.split("/");
				String png = aStrings[aStrings.length - 1];
				return png.replaceAll(".png]", "");
			}
		} catch (Exception e) {
			logger.error("getURLRang error", e);
		}
		// 使用finally块来关闭输入流
		finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return "-1";
	}

	/**
	 * 跑 收录
	 * 
	 * @return
	 */
	public static String runCollect(String url) {
		try {
			// 【1】 过滤掉空格 换行符
			String result = postBaidu(url.trim(), "10");
			JSONArray array = null;
			try {
				JSONObject jsonObject = null;
				try {
					jsonObject = JSONObject.parseObject(result);
				} catch (Exception e) {
					logger.error("解析json 出错... e:", e);
				}

				if (jsonObject == null || jsonObject.getJSONObject("feed") == null
						|| jsonObject.getJSONObject("feed").getJSONArray("entry") == null) {
					return "0";
				} else {
					array = jsonObject.getJSONObject("feed").getJSONArray("entry");
					String json = new Gson().toJson(array);
					if (json.contains(url)) {
						return "1";
					}
				}

			} catch (Exception e1) {
				e1.printStackTrace();
			}
		} catch (Exception e) {
			logger.error("runCollect error...", e);
		}
		return "0";
	}

	/**
	 * 获取 http 状态码
	 * @param url 
	 */
	public static int getHttpCode(String url) {
		try {
			URL u = new URL(url);
			HttpURLConnection uConnection = (HttpURLConnection) u.openConnection();
			uConnection.connect();
			uConnection.disconnect();
			
			return uConnection.getResponseCode();

		} catch (Exception e) {
			logger.error("getHttpCode error .....",e);
		}
		return 500;
	}

	
	
	/**
	 * 跑 排名
	 * 
	 * @return
	 */
	public static int runRankByKeyWord(String keyWord,String url) {
		try {
			// 【1】 过滤掉空格 换行符
			String result = postBaidu(keyWord.trim().replaceAll(" ", ""), "50");
			JSONArray array = null;
			try {
				JSONObject jsonObject = null;
				try {
					jsonObject = JSONObject.parseObject(result);
				} catch (Exception e) {
					logger.error("解析json 出错... e:", e);
				}

				if (jsonObject == null || jsonObject.getJSONObject("feed") == null
						|| jsonObject.getJSONObject("feed").getJSONArray("entry") == null) {
					return 500;
				} else {
					array = jsonObject.getJSONObject("feed").getJSONArray("entry");
					if(array==null || array.size()==0){
						return 0;
					}
					String u = "";
					int pn = 500;
					
					for (int i = 0; i < array.size(); i++) {
						try {
							JSONObject json = array.getJSONObject(i);
							if (json == null || json.isEmpty()) {
								continue;
							}
							u = json.get("url").toString();
							pn = json.getIntValue("pn");
							if(u.contains(url)){
								return pn;
							}
							
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}

			} catch (Exception e1) {
				e1.printStackTrace();
			}
		} catch (Exception e) {
			logger.error("runRankByKeyWord error...", e);
		}
		return 500;
	}
	
	
	
	private static String postBaidu(String content, String pn) {
		try {
			Random random = new Random();
			int r = random.nextInt(10);

			Map<String, Object> headerMap = new LinkedHashMap<>();
			headerMap.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
			headerMap.put("Accept-Encoding", "gzip, deflate, sdch, br");
			headerMap.put("Accept-Language", "zh-CN,zh;q=0.8");
			headerMap.put("Cache-Control", "max-age=0");
			headerMap.put("Connection", "keep-alive");
			headerMap.put("Host", "www.baidu.com");
			headerMap.put("Referer", "https://www.baidu.com/");
			headerMap.put("Upgrade-Insecure-Requests", "1");
			headerMap.put("User-Agent", UA[r]);
			// headerMap.put("Cookie","BAIDUID=30CC9619DA770E4C9CF90AC9D040C5D4:FG=1;
			// "
			// + "BIDUPSID=30CC9619DA770E4C9CF90AC9D040C5D4; PSTM=1535420203; "
			// + "BD_UPN=12314353; BDORZ=FFFB88E999055A3F8A630C64834BD6D0; "
			// +
			// "H_WISE_SIDS=125574_114550_125345_123763_125159_125405_120178_118894_118864_118853_118822_118790_125554_125454_125007_117332_124977_117429_125404_122789_124619_125311_123984_124559_125487_125172_125289_125582_125285_124525_124937_124030_110085_123289_124424_125427;
			// H_PS_PSSID=; "
			// + "delPer=0; BD_CK_SAM=1; PSINO=6; "
			// +
			// "H_PS_645EC=4f71tiFCf7wI8OjZaTfdbxqblH7qoW7VLPn6j7FNGPDUbXHKLAwnSQlmA%2Fqg42Jbj1Jkt6tU;
			// BDSVRTM=93");

			String result = sendGet(String.format(URL, content, pn), headerMap);
			return result;
		} catch (Exception e) {
			logger.error("postBaidu 访问 百度接口 出错 error ...", e);
			return null;
		}
	}

	/**
	 * 向指定URL发送GET方法的请求
	 * 
	 * @param url
	 *            发送请求的URL
	 * @param param
	 *            请求参数，请求参数应该是name1=value1&name2=value2的形式
	 * @return URL所代表远程资源的响应
	 */

	private static String sendGet(String url, Map<String, Object> headerMap) {
		String result = "";
		BufferedReader in = null;
		try {
			URL realUrl = new URL(url);
			// 打开和URL之间的连接
			URLConnection conn = realUrl.openConnection();
			// 设置通用的请求属性
			// conn.setRequestProperty("accept", "*/*");
			// conn.setRequestProperty("connection", "Keep-Alive");
			// conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible;
			// MSIE 6.0; Windows NT 5.1; SV1)");

			if (headerMap != null) {
				Iterator<Map.Entry<String, Object>> it = headerMap.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry<String, Object> element = (Map.Entry<String, Object>) it.next();
					conn.setRequestProperty(element.getKey(), element.getValue().toString());
				}
			}
			conn.setConnectTimeout(2000);
			// 建立实际的连接
			conn.connect();
			// 获取所有响应头字段
			Map<String, List<String>> map = conn.getHeaderFields();
			// 定义BufferedReader输入流来读取URL的响应
			in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			String line;
			while ((line = in.readLine()) != null) {
				result += "\n" + line;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// 使用finally块来关闭输入流
		finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return result;
	}

	private static String decodeUnicode(String theString) {
		char aChar;
		int len = theString.length();
		StringBuffer outBuffer = new StringBuffer(len);
		for (int x = 0; x < len;) {
			aChar = theString.charAt(x++);
			if (aChar == '\\') {
				aChar = theString.charAt(x++);
				if (aChar == 'u') {
					// Read the xxxx
					int value = 0;
					for (int i = 0; i < 4; i++) {
						aChar = theString.charAt(x++);
						switch (aChar) {
						case '0':
						case '1':
						case '2':
						case '3':
						case '4':
						case '5':
						case '6':
						case '7':
						case '8':
						case '9':
							value = (value << 4) + aChar - '0';
							break;
						case 'a':
						case 'b':
						case 'c':
						case 'd':
						case 'e':
						case 'f':
							value = (value << 4) + 10 + aChar - 'a';
							break;
						case 'A':
						case 'B':
						case 'C':
						case 'D':
						case 'E':
						case 'F':
							value = (value << 4) + 10 + aChar - 'A';
							break;
						default:
							throw new IllegalArgumentException("Malformed   \\uxxxx   encoding.");
						}

					}
					outBuffer.append((char) value);
				} else {
					if (aChar == 't')
						aChar = '\t';
					else if (aChar == 'r')
						aChar = '\r';
					else if (aChar == 'n')
						aChar = '\n';
					else if (aChar == 'f')
						aChar = '\f';
					outBuffer.append(aChar);
				}
			} else
				outBuffer.append(aChar);
		}
		return outBuffer.toString();
	}
	
	
	

	private static void sendGetM(String url_m,String keyword,Map<String, Map<String, String>> map ,int page) {
		String result = "";
		BufferedReader in = null;
		try {
			boolean iaskOk = true;
			boolean ishareOk = true;
			
			for(int i = 0;i< page ; i++){
//				System.out.println(url_m);
				URL realUrl = new URL(String.format(url_m,(i*10+1)+"" ,keyword));
				// 打开和URL之间的连接
				URLConnection conn = realUrl.openConnection();
				conn.setConnectTimeout(2000);
				// 建立实际的连接
				conn.connect();
				// 定义BufferedReader输入流来读取URL的响应
				in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
	
				String s;
				while ((s = in.readLine()) != null)
				{
					
					// 正则表达   已   {'fm' 开头，中间必须有  'mu': ， 以 '} 结束 
					Pattern pattern = Pattern.compile("\\{\'fm\'.*?\'mu\':\'(.*?)\'\\}");
					Matcher matcher = pattern.matcher(s);
					
					//循环，字符串中有多少个符合的，就循环多少次
					while(matcher.find()){
						//每一个符合正则的字符串
						String e = matcher.group();
						JSONObject json = JSONObject.parseObject(e); 
//						System.out.println(e);
						if (json == null || json.isEmpty()) {
							continue;
						}
						String url = json.get("mu").toString();		// 获取 url
						if (url.equals("https://m.iask.sina.com.cn/") || url.equals("https://m.iask.sina.com.cn")) {
							continue;
						}
						if (url.equals("http://m.ishare.iask.sina.com.cn/")
								|| url.equals("http://m.ishare.iask.sina.com.cn")) {
							continue;
						}
						if (url.contains("/m.iask.sina.com.cn") && iaskOk) {
							Map<String, String> iask = new HashMap<>();
							iask.put("url", url);
							iask.put("pn", json.getString("order"));
							map.put("iask", iask);
							iaskOk = false;
						} else if (url.contains("/m.ishare.iask.sina.com.cn") && ishareOk) {
							Map<String, String> ishare = new HashMap<>();
							ishare.put("url", url);
							ishare.put("pn", json.getString("order"));
							map.put("ishare", ishare);
							ishareOk = false;
						} else if (!ishareOk && !iaskOk) {
							return;
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("sendGetM error ...",e);
			e.printStackTrace();
		}
		// 使用finally块来关闭输入流
		finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public static String post(String urlAddr, Map<String, Object> paramMap)
			throws Exception {
		HttpURLConnection conn = null;
		String content = "";
		StringBuffer params = new StringBuffer();
		Iterator<Map.Entry<String, Object>> it = paramMap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Object> element = (Map.Entry<String, Object>) it
					.next();
			params.append((String) element.getKey());
			params.append("=");
			params.append(element.getValue());
			params.append("&");
		}

		if (params.length() > 0) {
			params.deleteCharAt(params.length() - 1);
		}
		BufferedReader br = null;
		try {
			URL url = new URL(urlAddr);
			conn = ((HttpURLConnection) url.openConnection());
			conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000); 
			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setUseCaches(false);
			conn.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			conn.setRequestProperty("Content-Length",
					String.valueOf(params.length()));
			conn.setDoInput(true);
			conn.connect();

			OutputStreamWriter out = new OutputStreamWriter(
					conn.getOutputStream(), "UTF-8");
			out.write(params.toString());
			out.flush();
			out.close();

			int code = conn.getResponseCode();
			if (code != 200) {
				logger.info("ERROR===" + code);
				InputStream in = conn.getInputStream();
				br = new BufferedReader(new InputStreamReader(
						in, "UTF-8"));
				String line = "";
				while ((line = br.readLine()) != null)
					content = content + line + "\r\n";
			} else {
				InputStream in = conn.getInputStream();
				br = new BufferedReader(new InputStreamReader(
						in, "UTF-8"));
				String line = "";
				while ((line = br.readLine()) != null)
					content = content + line + "\r\n";
			}
		} catch (Exception e) {
		    e.printStackTrace();
		    logger.error("urlAddr:"+urlAddr+" post error. ->" + params.toString(), e);
		} finally {
			if (conn!=null)conn.disconnect();
			if (br != null){
			    br.close();
			}
		}
		return content;
	}
	
	/**
	 * 从list中随机抽取元素
	 *
	 * @param list
	 * @param n
	 * @return void
	 * @throws @Title:
	 *             createRandomList
	 * @Description: TODO
	 */
	private static List randomList(List list, int n) {
		Map map = new HashMap();
		List listNew = new ArrayList();
		if (list.size() <= n) {
			return list;
		} else {
			while (map.size() < n) {
				int random = (int) (Math.random() * list.size());
				if (!map.containsKey(random)) {
					map.put(random, "");
					listNew.add(list.get(random));
				}
			}
			return listNew;
		}
	}

	/**
	 * 时间格式化成字符串
	 * 
	 * @param formatPattern
	 * @param date
	 * @return
	 */
	public static String formatDate(String formatPattern, Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat(formatPattern);
		return sdf.format(date);
	}

	/**
	 * 
	 * @param date
	 * @param day
	 * @return
	 */
	public static Date addDate(Date date, int day) {

		if (date == null)
			return null;
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DATE, day);// 24小时制
		date = cal.getTime();
		cal = null;
		return date;
	}

	public static void main(String[] args) {
		Map<String, Map<String, String>> map = getRankM("ss",5);
		Gson gson = new Gson();
		System.out.println(gson.toJson(map));
	}



}
