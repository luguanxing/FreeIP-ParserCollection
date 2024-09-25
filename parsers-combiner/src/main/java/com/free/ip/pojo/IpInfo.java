package com.free.ip.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class IpInfo {
    String ip;
    String country;
    String countryCode;
    String region;
    String regionCode;
    String city;
    String isp;
    Double latitude;
    Double longitude;
    String parserName;
    Long parseTimeTs;

    // core fields
    public IpInfo(String ip, String country, String countryCode, String region, String regionCode, String city, String isp, Double latitude, Double longitude) {
        this.ip = ip;
        this.country = country;
        this.countryCode = countryCode;
        this.region = region;
        this.regionCode = regionCode;
        this.city = city;
        this.isp = isp;
        this.latitude = latitude;
        this.longitude = longitude;
    }

}
