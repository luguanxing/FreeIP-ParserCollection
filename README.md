# 背景 Background
**免费就是硬道理！** 在互联网上，[免费IP解析资源](https://github.com/ihmily/ip-info-api)具有不可替代的重要性，但在需要处理大量数据时，选择付费服务可能会带来不小的成本压力。
然而，免费IP解析服务通常存在一些缺点，例如返回的数据字段不完全统一，以及各类使用限制（例如每日调用次数限制等）。

**Free is justice!** On the internet, [free IP parsing resources](https://github.com/ihmily/ip-info-api) are of irreplaceable importance, especially when dealing with large volumes of data, where opting for paid services can lead to significant cost pressures. However, free IP parsing services often come with certain drawbacks, such as inconsistencies in the returned data fields and various usage limitations (e.g., daily call limits).

<br/><br/><br/>


# 项目介绍 Project Introduction
FreeIPParsers-Combiner 项目的初衷便是解决这些问题。这是一个用 Java 编写的开源项目，通过将多个免费IP 解析接口整合到一起，从而构建一个好用、能用的免费IP解析器。

The FreeIPParsers-Combiner project was initiated to address these challenges. This is an open-source project written in Java that integrates multiple free IP parsing interfaces to build a functional and useful free IP parser.


<br/><br/><br/>

# 项目特点 Project Features
1. 支持对免费IP解析器的灵活扩展，只需要实现”抽取ipData“接口和"解析ipData"两个接口以及返回一个接口的”支持字段列表“即可扩展
2. 查询时只需要的”字段列表“和对应ip即可
3. 实现了线程池实现查询，内含自动重试和休眠，以及原始数据文件的读入和解析文件的输出
<br/>

1. Flexible Expansion of Free IP Parsers: The project supports the flexible expansion of free IP parsers. You can extend the project simply by implementing two interfaces: "fetchIpData" and "parseIpData," and by returning a "supported fields list" for the interface.
2. Field-Based Querying: Queries only require the "fields list" and the corresponding IP address, making the process straightforward and efficient.
3. Thread Pool Implementation: The project includes a thread pool for handling queries, featuring automatic retry and sleep mechanisms, as well as the ability to read raw data files and output parsed files.


<br/><br/><br/>


# Code Example

添加一个解析器只需要实现以下接口；

To add a parser, you only need to implement the following interfaces;


```
public interface IpParser {

    Set<IpinfoEnum> getSupportedFields();

    JSONObject fetchIpData(String ip);

    IpInfo parseIpData(JSONObject json);

    default IpInfo getIpInfo(String ip) {
        JSONObject ipData = fetchIpData(ip);
        if (ipData == null) {
            return null;
        }
        return parseIpData(ipData);
    }

}

```

<br/><br/>

一个具体的Parser实现例子：

An example of implementation of Parser:

```
@Log4j2
public class IpSbParser implements IpParser {

    public static final String API_URL = "https://api.ip.sb/geoip/";

    @Override
    public Set<IpinfoEnum> getSupportedFields() {
        return new HashSet<>(Arrays.asList(
                IpinfoEnum.IP,
                IpinfoEnum.COUNTRY,
                IpinfoEnum.COUNTRY_CODE,
                IpinfoEnum.REGION,
                IpinfoEnum.REGION_CODE,
                IpinfoEnum.CITY,
                IpinfoEnum.ISP,
                IpinfoEnum.LATITUDE,
                IpinfoEnum.LONGITUDE
        ));
    }

    @Override
    public JSONObject fetchIpData(String ip) {
        try {
            URL url = new URL(API_URL + ip);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5 * 1000);
            conn.setReadTimeout(5 * 1000);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setUseCaches(false);
            conn.setInstanceFollowRedirects(false);
            conn.setRequestMethod("GET");
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                StringBuilder builder = new StringBuilder();
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                for (String s = br.readLine(); s != null; s = br.readLine()) {
                    builder.append(s);
                }
                br.close();
                return new JSONObject(builder.toString());
            }
        } catch (Exception e) {
            log.error("Failed to extract JSON object from " + API_URL, e);
        }
        return null;
    }

    @Override
    public IpInfo parseIpData(JSONObject json) {
        return new IpInfo(
                json.optString("ip"),
                json.optString("country"),
                json.optString("country_code"),
                json.optString("region"),
                json.optString("region_code"),
                json.optString("city"),
                json.optString("isp"),
                json.optDouble("latitude"),
                json.optDouble("longitude")
        );
    }

}
```



<br/><br/>


使用IP解析的示例代码如下：

The example usage code of parsing IPs is as follows:

```
List<String> ipList = InoutUtil.readIpFile("/data/ips.txt");
List<IpParser> conditionalParsers = ParserFactory.getConditionalParsers(Arrays.asList(
        IpinfoEnum.IP,
        IpinfoEnum.COUNTRY,
        IpinfoEnum.COUNTRY_CODE,
        IpinfoEnum.REGION,
        IpinfoEnum.CITY
));
List<IpInfo> ipInfos = ExecutorUtil.runParsers(ipList, conditionalParsers, 1);
InoutUtil.writeResultFile("/data/ipinfos.txt", ipInfos);
```
