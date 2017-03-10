java spring+mybatis整合实现爬虫之《今日头条》搞笑动态图片爬取（详细）
--------------------------------------


----------

一.此爬虫介绍
-----
>今日头条本身就是做爬虫的，爬取各大网站的图片文字信息，再自己整合后推送给用户，特别是里面的动态图片，很有意思。在网上搜了搜，大多都是用Python来写的，本人是学习javaweb这块的，对正则表达式也不是很熟悉，就想着能不能换个我熟悉的方式来写。此爬虫使用spring+mybatis框架整合实现，使用mysql数据库保存爬取的数据，用jsoup来操作HTML的标签节点（完美避开正则表达式），获取文章中动态图片的链接，通过响应头中“Content-Type”的值来判断图片的格式，再将图片保存在本地。当然也可以爬取里面的文字，比如一些搞笑的黄段子，在此基础上稍加改动就可以实现，此爬虫只是提供一个入门的思路，更多好玩的爬虫玩法还待大家去开发，哈哈。

二.技术选型
------

>  1. 核心语言：java；
>  2. 核心框架：spring；
>  2. 持久层框架：mybatis；
>  3. 数据库连接池：Alibaba Drui；
>  4. 日志管理：Log4j；
>  5. jar包管理：maven； 。。。。

三.找规律，划重点
---------

> 打开头条首页，找到点击搞笑模块，点击F12,下滚后加载下一页，发现是通过ajax请求api来获取的数据，如下图：

![这里写图片描述](http://img.blog.csdn.net/20161226223513868?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvcXFfMjA5NTQ5NTk=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

> **这是响应的json数据，里面的参数和值顾名思义大家都懂得。**

![这里写图片描述](http://img.blog.csdn.net/20161226223904416?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvcXFfMjA5NTQ5NTk=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

> 是ajax访问就好解决了，通过我百度谷歌各种研究后发现，ajax请求的前三个参数是不变的，改变category参数是请求不同的模块，本列子是请求的搞笑模块所以值为funny，max_behot_time和max_behot_time_tmp这两个参数值是时间戳，首次请求是0，之后的值是响应json数据里面的next中的值。as和cp值是通过一段js生成的，其实就是一个加密了的时间戳而已。js代码后面会贴。

四.开始搭框架撸代码
-------

> 项目搭建后之后为下图所示的文件结构，不懂得自行谷歌  哈哈


![这里写图片描述](http://img.blog.csdn.net/20161226225657064?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvcXFfMjA5NTQ5NTk=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

> **不多说直接上核心代码了：**

```java
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

```

> **获取as和cp参数的js代码**

```javascript
function getParam(){
    var asas;
    var cpcp;
    var t = Math.floor((new Date).getTime() / 1e3)
      , e = t.toString(16).toUpperCase()
      , i = md5(t).toString().toUpperCase();
    if (8 != e.length){
        asas = "479BB4B7254C150";
        cpcp = "7E0AC8874BB0985";
    }else{
        for (var n = i.slice(0, 5), o = i.slice(-5), a = "", s = 0; 5 > s; s++){
            a += n[s] + e[s];
        }
        for (var r = "", c = 0; 5 > c; c++){
            r += e[c + 3] + o[c];
        }
        asas = "A1" + a + e.slice(-3);
        cpcp= e.slice(0, 3) + r + "E1";
    }
    return '{"as":"'+asas+'","cp":"'+cpcp+'"}';
}

!function(e) {
    "use strict";
    function t(e, t) {
        var n = (65535 & e) + (65535 & t)
          , r = (e >> 16) + (t >> 16) + (n >> 16);
        return r << 16 | 65535 & n
    }
    function n(e, t) {
        return e << t | e >>> 32 - t
    }
    function r(e, r, o, i, a, u) {
        return t(n(t(t(r, e), t(i, u)), a), o)
    }
    function o(e, t, n, o, i, a, u) {
        return r(t & n | ~t & o, e, t, i, a, u)
    }
    function i(e, t, n, o, i, a, u) {
        return r(t & o | n & ~o, e, t, i, a, u)
    }
    function a(e, t, n, o, i, a, u) {
        return r(t ^ n ^ o, e, t, i, a, u)
    }
    function u(e, t, n, o, i, a, u) {
        return r(n ^ (t | ~o), e, t, i, a, u)
    }
    function s(e, n) {
        e[n >> 5] |= 128 << n % 32,
        e[(n + 64 >>> 9 << 4) + 14] = n;
        var r, s, c, l, f, p = 1732584193, d = -271733879, h = -1732584194, m = 271733878;
        for (r = 0; r < e.length; r += 16)
            s = p,
            c = d,
            l = h,
            f = m,
            p = o(p, d, h, m, e[r], 7, -680876936),
            m = o(m, p, d, h, e[r + 1], 12, -389564586),
            h = o(h, m, p, d, e[r + 2], 17, 606105819),
            d = o(d, h, m, p, e[r + 3], 22, -1044525330),
            p = o(p, d, h, m, e[r + 4], 7, -176418897),
            m = o(m, p, d, h, e[r + 5], 12, 1200080426),
            h = o(h, m, p, d, e[r + 6], 17, -1473231341),
            d = o(d, h, m, p, e[r + 7], 22, -45705983),
            p = o(p, d, h, m, e[r + 8], 7, 1770035416),
            m = o(m, p, d, h, e[r + 9], 12, -1958414417),
            h = o(h, m, p, d, e[r + 10], 17, -42063),
            d = o(d, h, m, p, e[r + 11], 22, -1990404162),
            p = o(p, d, h, m, e[r + 12], 7, 1804603682),
            m = o(m, p, d, h, e[r + 13], 12, -40341101),
            h = o(h, m, p, d, e[r + 14], 17, -1502002290),
            d = o(d, h, m, p, e[r + 15], 22, 1236535329),
            p = i(p, d, h, m, e[r + 1], 5, -165796510),
            m = i(m, p, d, h, e[r + 6], 9, -1069501632),
            h = i(h, m, p, d, e[r + 11], 14, 643717713),
            d = i(d, h, m, p, e[r], 20, -373897302),
            p = i(p, d, h, m, e[r + 5], 5, -701558691),
            m = i(m, p, d, h, e[r + 10], 9, 38016083),
            h = i(h, m, p, d, e[r + 15], 14, -660478335),
            d = i(d, h, m, p, e[r + 4], 20, -405537848),
            p = i(p, d, h, m, e[r + 9], 5, 568446438),
            m = i(m, p, d, h, e[r + 14], 9, -1019803690),
            h = i(h, m, p, d, e[r + 3], 14, -187363961),
            d = i(d, h, m, p, e[r + 8], 20, 1163531501),
            p = i(p, d, h, m, e[r + 13], 5, -1444681467),
            m = i(m, p, d, h, e[r + 2], 9, -51403784),
            h = i(h, m, p, d, e[r + 7], 14, 1735328473),
            d = i(d, h, m, p, e[r + 12], 20, -1926607734),
            p = a(p, d, h, m, e[r + 5], 4, -378558),
            m = a(m, p, d, h, e[r + 8], 11, -2022574463),
            h = a(h, m, p, d, e[r + 11], 16, 1839030562),
            d = a(d, h, m, p, e[r + 14], 23, -35309556),
            p = a(p, d, h, m, e[r + 1], 4, -1530992060),
            m = a(m, p, d, h, e[r + 4], 11, 1272893353),
            h = a(h, m, p, d, e[r + 7], 16, -155497632),
            d = a(d, h, m, p, e[r + 10], 23, -1094730640),
            p = a(p, d, h, m, e[r + 13], 4, 681279174),
            m = a(m, p, d, h, e[r], 11, -358537222),
            h = a(h, m, p, d, e[r + 3], 16, -722521979),
            d = a(d, h, m, p, e[r + 6], 23, 76029189),
            p = a(p, d, h, m, e[r + 9], 4, -640364487),
            m = a(m, p, d, h, e[r + 12], 11, -421815835),
            h = a(h, m, p, d, e[r + 15], 16, 530742520),
            d = a(d, h, m, p, e[r + 2], 23, -995338651),
            p = u(p, d, h, m, e[r], 6, -198630844),
            m = u(m, p, d, h, e[r + 7], 10, 1126891415),
            h = u(h, m, p, d, e[r + 14], 15, -1416354905),
            d = u(d, h, m, p, e[r + 5], 21, -57434055),
            p = u(p, d, h, m, e[r + 12], 6, 1700485571),
            m = u(m, p, d, h, e[r + 3], 10, -1894986606),
            h = u(h, m, p, d, e[r + 10], 15, -1051523),
            d = u(d, h, m, p, e[r + 1], 21, -2054922799),
            p = u(p, d, h, m, e[r + 8], 6, 1873313359),
            m = u(m, p, d, h, e[r + 15], 10, -30611744),
            h = u(h, m, p, d, e[r + 6], 15, -1560198380),
            d = u(d, h, m, p, e[r + 13], 21, 1309151649),
            p = u(p, d, h, m, e[r + 4], 6, -145523070),
            m = u(m, p, d, h, e[r + 11], 10, -1120210379),
            h = u(h, m, p, d, e[r + 2], 15, 718787259),
            d = u(d, h, m, p, e[r + 9], 21, -343485551),
            p = t(p, s),
            d = t(d, c),
            h = t(h, l),
            m = t(m, f);
        return [p, d, h, m]
    }
    function c(e) {
        var t, n = "";
        for (t = 0; t < 32 * e.length; t += 8)
            n += String.fromCharCode(e[t >> 5] >>> t % 32 & 255);
        return n
    }
    function l(e) {
        var t, n = [];
        for (n[(e.length >> 2) - 1] = void 0,
        t = 0; t < n.length; t += 1)
            n[t] = 0;
        for (t = 0; t < 8 * e.length; t += 8)
            n[t >> 5] |= (255 & e.charCodeAt(t / 8)) << t % 32;
        return n
    }
    function f(e) {
        return c(s(l(e), 8 * e.length))
    }
    function p(e, t) {
        var n, r, o = l(e), i = [], a = [];
        for (i[15] = a[15] = void 0,
        o.length > 16 && (o = s(o, 8 * e.length)),
        n = 0; 16 > n; n += 1)
            i[n] = 909522486 ^ o[n],
            a[n] = 1549556828 ^ o[n];
        return r = s(i.concat(l(t)), 512 + 8 * t.length),
        c(s(a.concat(r), 640))
    }
    function d(e) {
        var t, n, r = "0123456789abcdef", o = "";
        for (n = 0; n < e.length; n += 1)
            t = e.charCodeAt(n),
            o += r.charAt(t >>> 4 & 15) + r.charAt(15 & t);
        return o
    }
    function h(e) {
        return unescape(encodeURIComponent(e))
    }
    function m(e) {
        return f(h(e))
    }
    function g(e) {
        return d(m(e))
    }
    function v(e, t) {
        return p(h(e), h(t))
    }
    function y(e, t) {
        return d(v(e, t))
    }
    function b(e, t, n) {
        return t ? n ? v(t, e) : y(t, e) : n ? m(e) : g(e)
    }
    "function" == typeof define && define.amd ? define("static/js/lib/md5", ["require"], function() {
        return b
    }) : "object" == typeof module && module.exports ? module.exports = b : e.md5 = b
}(this)
```

五.最后
------

> 我还发现了头条有个简约版，研究后发现这个简约版应该更好爬一些。

![这里写图片描述](http://img.blog.csdn.net/20161226230850348?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvcXFfMjA5NTQ5NTk=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

> 访问的格式是p+页码，直接读取每页里面的链接，就可以进行爬取了，就不再通过json串来获取文章地址，也不需要传什么限制参数，在本项目上稍加改动就可以了

![这里写图片描述](http://img.blog.csdn.net/20161226230958084?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvcXFfMjA5NTQ5NTk=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

![这里写图片描述](http://img.blog.csdn.net/20161226231623684?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvcXFfMjA5NTQ5NTk=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)


六.JUST DO IT
------------

> 。。。。。。。。。。。。。。。。。。。。。。