package com.free.ip.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;

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
    double latitude;
    double longitude;
}
