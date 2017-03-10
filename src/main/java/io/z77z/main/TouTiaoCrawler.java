package io.z77z.main;

import io.z77z.dao.FunnyMapper;
import io.z77z.entity.Funny;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.UUID;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class TouTiaoCrawler {

	// 搞笑板块的api地址
	public static final String FUNNY = "http://www.toutiao.com/api/pc/feed/?utm_source=toutiao&widen=1";

	// 头条首页地址
	public static final String TOUTIAO = "http://www.toutiao.com";

	// 使用"spring.xml"和"spring-mybatis.xml"这两个配置文件创建Spring上下文
	static ApplicationContext ac = new ClassPathXmlApplicationContext(
			"spring-mybatis.xml");

	// 从Spring容器中根据bean的id取出我们要使用的funnyMapper对象
	static FunnyMapper funnyMapper = (FunnyMapper) ac.getBean("funnyMapper");

	// 接口访问次数
	private static int refreshCount = 0;

	// 时间戳
	private static long time = 0;

	public static void main(String[] args) {
		System.out.println("----------开始干活！-----------------");
		while (true) {
			crawler(time);
		}
	}

	public static void crawler(long hottime) {// 传入时间戳，会获取这个时间戳的内容
		refreshCount++;
		System.out.println("----------第" + refreshCount + "次刷新------返回的请求时间为："
				+ hottime + "----------");
		String url = FUNNY + "&max_behot_time=" + hottime
				+ "&max_behot_time_tmp=" + hottime;
		JSONObject param = getUrlParam(); // 获取用js代码得到的as和cp的值
		// 定义接口访问的模块
		/*
		 * __all__ : 推荐 news_hot: 热点 funny：搞笑
		 */
		String module = "funny";
		url += "&as=" + param.get("as") + "&cp=" + param.get("cp")
				+ "&category=" + module;
		JSONObject json = null;
		try {
			json = getReturnJson(url);// 获取json串
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (json != null) {
			time = json.getJSONObject("next").getLongValue("max_behot_time");
			JSONArray data = json.getJSONArray("data");
			for (int i = 0; i < data.size(); i++) {
				try {
					JSONObject obj = (JSONObject) data.get(i);
					// 判断这条文章是否已经爬过
					if (funnyMapper.selectByGroupId((String) obj
							.get("group_id")) != null) {
						System.out
								.println("----------此文章已经爬过啦！-----------------");
						continue;
					}
					// 访问页面返回document对象
					String url1 = TOUTIAO + "/a" + obj.getString("group_id");
					Document document = getArticleInfo(url1);
					System.out.println("----------成功访问了文章：" + url1
							+ "-----------------");
					// 将document也存入
					obj.put("document", document.toString());
					// 将json对象转换成java Entity对象
					Funny funny = JSON.parseObject(obj.toString(), Funny.class);
					// json入库
					funny.setBehotTime(new Date());
					funnyMapper.insertSelective(funny);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {
			System.out.println("----------返回的json列表为空----------");
		}
	}

	// 访问接口，返回json封装的数据格式
	public static JSONObject getReturnJson(String url) {
		try {
			URL httpUrl = new URL(url);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					httpUrl.openStream(), "UTF-8"));
			String line = null;
			String content = "";
			while ((line = in.readLine()) != null) {
				content += line;
			}
			in.close();
			return JSONObject.parseObject(content);
		} catch (Exception e) {
			System.err.println("访问失败:" + url);
			e.printStackTrace();
		}
		return null;
	}

	// 获取网站的document对象
	public static Document getArticleInfo(String url) {
		try {
			Connection connect = Jsoup.connect(url);
			Document document;
			document = connect.get();
			Elements article = document.getElementsByClass("article-content");
			if (article.size() > 0) {
				Elements a = article.get(0).getElementsByTag("img");
				if (a.size() > 0) {
					for (Element e : a) {
						String url2 = e.attr("src");
						// 下载img标签里面的图片到本地
						saveToFile(url2);
					}
				}
			}
			return document;
		} catch (IOException e) {
			System.err.println("访问文章页失败:" + url + "  原因" + e.getMessage());
			return null;
		}
	}

	// 执行js获取as和cp参数值
	public static JSONObject getUrlParam() {
		JSONObject jsonObject = null;
		FileReader reader = null;
		try {
			ScriptEngineManager manager = new ScriptEngineManager();
			ScriptEngine engine = manager.getEngineByName("javascript");

			String jsFileName = "toutiao.js"; // 读取js文件

			reader = new FileReader(jsFileName); // 执行指定脚本
			engine.eval(reader);

			if (engine instanceof Invocable) {
				Invocable invoke = (Invocable) engine;
				Object obj = invoke.invokeFunction("getParam");
				jsonObject = JSONObject.parseObject(obj != null ? obj
						.toString() : null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return jsonObject;
	}

	// 通过url获取图片并保存在本地
	public static void saveToFile(String destUrl) {
		FileOutputStream fos = null;
		BufferedInputStream bis = null;
		HttpURLConnection httpUrl = null;
		URL url = null;
		String uuid = UUID.randomUUID().toString();
		String fileAddress = "d:\\imag/" + uuid;// 存储本地文件地址
		int BUFFER_SIZE = 1024;
		byte[] buf = new byte[BUFFER_SIZE];
		int size = 0;
		try {
			url = new URL(destUrl);
			httpUrl = (HttpURLConnection) url.openConnection();
			httpUrl.connect();
			String Type = httpUrl.getHeaderField("Content-Type");
			if (Type.equals("image/gif")) {
				fileAddress += ".gif";
			} else if (Type.equals("image/png")) {
				fileAddress += ".png";
			} else if (Type.equals("image/jpeg")) {
				fileAddress += ".jpg";
			} else {
				System.err.println("未知图片格式");
				return;
			}
			bis = new BufferedInputStream(httpUrl.getInputStream());
			fos = new FileOutputStream(fileAddress);
			while ((size = bis.read(buf)) != -1) {
				fos.write(buf, 0, size);
			}
			fos.flush();
			System.out.println("图片保存成功！地址：" + fileAddress);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassCastException e) {
			e.printStackTrace();
		} finally {
			try {
				fos.close();
				bis.close();
				httpUrl.disconnect();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		}
	}
}
