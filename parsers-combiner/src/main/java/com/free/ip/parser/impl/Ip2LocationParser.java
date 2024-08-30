package com.free.ip.parser.impl;

import com.free.ip.parser.IpParser;
import com.free.ip.pojo.IpInfo;
import com.free.ip.pojo.IpinfoEnum;
import lombok.extern.log4j.Log4j2;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Log4j2
public class Ip2LocationParser implements IpParser {

    public static String API_URL = "https://api.ip2location.io/?ip=";

    @Override
    public Set<IpinfoEnum> getSupportedFields() {
        return new HashSet<>(Arrays.asList(
                IpinfoEnum.IP,
                IpinfoEnum.COUNTRY,
                IpinfoEnum.COUNTRY_CODE,
                IpinfoEnum.REGION,
                IpinfoEnum.CITY,
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

            // add HTTP request header
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/92.0.4515.159 Safari/537.36");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("Accept-Language", "en-US,en;q=0.9");
            conn.setRequestProperty("Connection", "keep-alive");

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
                json.optString("country_name"),
                json.optString("country_code"),
                json.optString("region_name"),
                null,
                json.optString("city_name"),
                null,
                json.optDouble("latitude"),
                json.optDouble("longitude")
        );
    }

}
