import com.free.ip.parser.IpParser;
import com.free.ip.parser.ParserFactory;
import com.free.ip.pojo.IpinfoEnum;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

@Log4j2
public class TestFactory {

    @Test
    public void testConditionalParser() {
        // Get all conditional parsers that support IP field
        List<IpParser> conditionalParsers0 = ParserFactory.getConditionalParsers(
                Arrays.asList(
                        IpinfoEnum.IP
                )
        );
        log.info("conditionalParsers0 size : " + conditionalParsers0.size());

        // Get all conditional parsers that support IP and REGION_CODE fields
        List<IpParser> conditionalParsers1 = ParserFactory.getConditionalParsers(
                Arrays.asList(
                        IpinfoEnum.IP,
                        IpinfoEnum.REGION_CODE
                )
        );
        log.info("conditionalParsers1 size : " + conditionalParsers1.size());
    }

}
