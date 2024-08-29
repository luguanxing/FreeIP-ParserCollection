package com.free.ip;


import com.free.ip.parser.IpParser;
import com.free.ip.parser.ParserFactory;
import com.free.ip.pojo.IpInfo;
import com.free.ip.pojo.IpinfoEnum;
import com.free.ip.utils.ExecutorUtil;
import com.free.ip.utils.InoutUtil;
import lombok.extern.log4j.Log4j2;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
public class Runner {

    public static void main(String[] args) {
        // read & parse input arguments
        if (args.length < 2) {
            log.error("Input IPData filePath and output filePath and info fields(ip=1;country=12countryCode=3;region=4;regionCode=5;city=6;isp=7;latitude=8;longitude=9).");
            log.error("Example: '/data/ipdata.txt /data/ipinfo.txt 1;2;3;4;5;6;7;8;9'");
            System.exit(1);
        }
        String inputPath = args[0];
        String outputPath = args[1];
        String fields = args[2];

        // read input file & choose parsers
        List<String> ipList = InoutUtil.readIpFile(inputPath);
        List<IpinfoEnum> conditions = Arrays.asList(fields.split(";"))
                .stream()
                .map(s -> IpinfoEnum.values()[Integer.parseInt(s) - 1])
                .collect(Collectors.toList());
        List<IpParser> conditionalParsers = ParserFactory.getConditionalParsers(conditions);

        // using parsers to parse ip data
        List<IpInfo> ipInfos = ExecutorUtil.runParsers(ipList, conditionalParsers, 1);

        // write result to output file
        if (InoutUtil.writeResultFile(outputPath, ipInfos)) {
            log.info("SUCCESS");
        } else {
            log.error("FAILED");
        }
    }

}
