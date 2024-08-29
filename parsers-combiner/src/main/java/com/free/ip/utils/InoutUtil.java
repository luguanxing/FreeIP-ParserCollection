package com.free.ip.utils;

import com.free.ip.pojo.IpInfo;
import lombok.extern.log4j.Log4j2;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Log4j2
public class InoutUtil {

    /**
     * Reads a file containing IP addresses, one per line.
     *
     * @param ipFilePath the path to the file containing the IP addresses
     * @return a list of IP addresses as strings
     */
    public static List<String> readIpFile(String ipFilePath) {
        List<String> ipList = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(ipFilePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                ipList.add(line.trim());
            }
        } catch (IOException e) {
            log.error("Error reading IP file: " + e.getMessage());
        }
        return ipList;
    }

    /**
     * Writes parsed IP data to a JSON file, one JSON object per line.
     *
     * @param resultFilePath the path to the output file
     * @param ipInfoList a list of JSONObject representing parsed IP data
     * @return true if the file was written successfully, false otherwise
     */
    public static boolean writeResultFile(String resultFilePath, List<IpInfo> ipInfoList) {
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(resultFilePath))) {
            for (IpInfo ipInfo : ipInfoList) {
                bw.write(new JSONObject(ipInfo).toString());
                bw.newLine();
            }
            return true;
        } catch (IOException e) {
            log.error("Error writing result file: " + e.getMessage());
            return false;
        }
    }

}
