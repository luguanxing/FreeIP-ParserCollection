import com.free.ip.parser.IpParser;
import com.free.ip.parser.ParserFactory;
import com.free.ip.pojo.IpInfo;
import com.free.ip.pojo.IpinfoEnum;
import com.free.ip.utils.ExecutorUtil;
import com.free.ip.utils.InoutUtil;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

@Log4j2
public class TestRunner {

    @Test
    public void testExecutor() {
        List<String> ipList = Arrays.asList(
                "67.189.89.89",
                "113.116.246.117",
                "175.100.48.127",
                "27.210.152.236",
                "213.10.31.166",
                "167.172.224.138"
        );
        List<IpParser> conditionalParsers = ParserFactory.getConditionalParsers(Arrays.asList(
                IpinfoEnum.IP,
                IpinfoEnum.COUNTRY,
                IpinfoEnum.COUNTRY_CODE,
                IpinfoEnum.REGION,
                IpinfoEnum.CITY
        ));
        System.err.println("conditionalParsers.size()=" + conditionalParsers.size());
        List<IpInfo> ipInfos = ExecutorUtil.runParsers(ipList, conditionalParsers, 1);
        for (IpInfo ipInfo : ipInfos) {
            log.info(ipInfo);
        }
    }

    @Test
    public void testInout() {
        List<String> ipList = InoutUtil.readIpFile("/Users/luguanxing/ips.txt");
        List<IpParser> conditionalParsers = ParserFactory.getConditionalParsers(Arrays.asList(
                IpinfoEnum.IP,
                IpinfoEnum.COUNTRY,
                IpinfoEnum.COUNTRY_CODE,
                IpinfoEnum.REGION,
                IpinfoEnum.CITY
        ));
        List<IpInfo> ipInfos = ExecutorUtil.runParsers(ipList, conditionalParsers, 1);
        if (InoutUtil.writeResultFile("/Users/luguanxing/ipinfos.txt", ipInfos)) {
            log.info("SUCCESS");
        } else {
            log.error("FAILED");
        }
    }

}
