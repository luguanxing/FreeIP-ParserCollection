package com.free.ip.parser;

import com.free.ip.pojo.IpinfoEnum;
import lombok.extern.log4j.Log4j2;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Log4j2
public class ParserFactory {

    public static List<IpParser> getAllParsers() {
        String packageToScan = "com.free.ip.parser.impl";

        Reflections reflections = new Reflections(
                new ConfigurationBuilder()
                        .setUrls(ClasspathHelper.forPackage(packageToScan))
                        .filterInputsBy(new FilterBuilder().includePackage(packageToScan))
                        .setScanners(Scanners.SubTypes)
        );
        Set<Class<? extends IpParser>> implementations = reflections.getSubTypesOf(IpParser.class);

        List<IpParser> parsers = new ArrayList<>();
        for (Class<? extends IpParser> implClass : implementations) {
            try {
                IpParser parser = implClass.getDeclaredConstructor().newInstance();
                parsers.add(parser);
            } catch (Exception e) {
                log.error(e);
            }
        }
        return parsers;
    }

    public static List<IpParser> getConditionalParsers(List<IpinfoEnum> fields) {
        List<IpParser> allParsers = getAllParsers();
        List<IpParser> conditionalParsers = new ArrayList<>();
        for (IpParser parser : allParsers) {
            if (parser.getSupportedFields().containsAll(fields)) {
                conditionalParsers.add(parser);
            }
        }
        return conditionalParsers;
    }

}
