package com.free.ip.parser;

import com.free.ip.pojo.IpInfo;
import com.free.ip.pojo.IpinfoEnum;
import org.json.JSONObject;

import java.util.Set;

public interface IpParser {

    /**
     * @return a set of Parser's supported fields
     */
    Set<IpinfoEnum> getSupportedFields();

    /**
     * Extract the JSON object
     *
     * @param ip
     * @return a JSON object if successful, null otherwise
     */
    JSONObject fetchIpData(String ip);

    /**
     * Parse the JSON object and return an IpInfo object
     *
     * @param json
     * @return an IpInfo object if successful, null otherwise
     */
    IpInfo parseIpData(JSONObject json);

    /**
     * Get the IpInfo object for the given IP
     *
     * @param ip
     * @return an IpInfo object if successful, null otherwise
     */
    default IpInfo getIpInfo(String ip) {
        JSONObject ipData = fetchIpData(ip);
        if (ipData == null) {
            return null;
        }
        IpInfo ipInfo = parseIpData(ipData);
        ipInfo.setParserName(this.getClass().getSimpleName());
        ipInfo.setParseTimeTs(System.currentTimeMillis());
        return ipInfo;
    }

}
