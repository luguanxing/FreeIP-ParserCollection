import com.free.ip.parser.IpParser;
import com.free.ip.pojo.IpInfo;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.util.Set;

@Log4j2
public class TestParser {

    @Test
    public void testSingleParser() {
        log.info(new com.free.ip.parser.impl.Ip_ApiParser().getIpInfo("67.189.89.89"));
        log.info(new com.free.ip.parser.impl.Ip_ApiParser().getIpInfo("113.116.246.117"));
        log.info(new com.free.ip.parser.impl.Ip_ApiParser().getIpInfo("122.13.25.57"));
    }

    @Test
    public void testParserAvailability() {
        String testIp = "113.116.246.117";
        String packageToScan = "com.free.ip.parser.impl";

        // Get all parsers
        Reflections reflections = new Reflections(
                new ConfigurationBuilder()
                        .setUrls(ClasspathHelper.forPackage(packageToScan))
                        .filterInputsBy(new FilterBuilder().includePackage(packageToScan))
                        .setScanners(Scanners.SubTypes)
        );
        Set<Class<? extends IpParser>> implementations = reflections.getSubTypesOf(IpParser.class);

        // Test all parsers
        int successCount = 0;
        for (Class<? extends IpParser> implClass : implementations) {
            log.info("Testing parser: " + implClass.getName());
            try {
                IpParser parser = implClass.getDeclaredConstructor().newInstance();
                IpInfo info = parser.getIpInfo(testIp);
                if (info != null) {
                    log.info(info);
                    successCount++;
                }
            } catch (Exception e) {
                log.error(e);
            }
        }
        log.info("testParserAvailability: " + successCount + " out of " + implementations.size() + " parsers are available");
    }

}
