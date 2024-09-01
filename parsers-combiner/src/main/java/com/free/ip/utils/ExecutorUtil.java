package com.free.ip.utils;

import com.free.ip.parser.IpParser;
import com.free.ip.pojo.IpInfo;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Log4j2
public class ExecutorUtil {

    public static final int EACH_IP_ATTEMPTS = 5;

    public static final int EACH_PARSER_ATTEMPTS = 3;

    /**
     * Parses a list of IP addresses using a provided list of parsers. Each IP address is attempted
     * to be parsed using the parsers. If parsing fails, it retries until success or the maximum
     * number of attempts is reached. If a parser fails consecutively more than a certain number of times,
     * it is discarded. If all parsers become unusable, the task is terminated early.
     *
     * 解析一组IP地址，使用提供的解析器列表。每个IP地址会尝试使用解析器进行解析，
     * 如果解析失败，则会重试，直到成功或达到最大尝试次数。如果某个解析器连续失败
     * 超过一定次数，则不再使用该解析器。如果所有解析器都不可用，则提前终止任务。
     *
     * @param ipList       A list of IP addresses to be parsed
     *                     待解析的IP地址列表
     * @param parserList   A list of parsers to be used for parsing the IP addresses
     *                     用于解析IP地址的解析器列表
     * @param sleepSeconds The number of seconds to sleep between each attempt
     *                     每次尝试之间的休眠时间（秒）
     * @return             A list containing the results of the IP parsing
     *                     包含解析结果的列表
     */
    public static List<IpInfo> runParsers(List<String> ipList, List<IpParser> parserList, int sleepSeconds) {
        ConcurrentLinkedQueue<IpInfo> resultList = new ConcurrentLinkedQueue<>();

        // Create a blocking queue for the parsers
        BlockingQueue<IpParser> parserQueue = new LinkedBlockingQueue<>(parserList);

        // Map to track the consecutive failure count for each parser
        ConcurrentHashMap<IpParser, Integer> parserFailureCntMap = new ConcurrentHashMap<>();

        // Counter to track the number of failed parsers
        AtomicInteger failedParsersCount = new AtomicInteger(0);

        ExecutorService executorService = Executors.newFixedThreadPool(parserList.size());
        for (String ip : ipList) {
            executorService.submit(() -> {
                boolean success = false;
                int attempts = 0;

                while (!success && attempts < EACH_IP_ATTEMPTS) {
                    IpParser parser = null;
                    IpParser lastParser;
                    int parserFailures = 0;
                    try {
                        // Fetch a parser from safe queue and wait until successful
                        parser = parserQueue.take();
                        // Use the parser to get the IP info
                        log.info(ip + " uses Parser => " + parser.getClass().getName());
                        IpInfo info = parser.getIpInfo(ip);
                        if (info != null) {
                            success = true;
                            resultList.add(info);
                            parserFailureCntMap.put(parser, 0);
                        } else {
                            throw new RuntimeException("Parser returned null");
                        }
                    } catch (Exception e) {
                        log.info(ip + "[" + attempts + "] failed with Parser => " + parser.getClass().getName());
                        attempts++;
                        success = false;
                        // Increment failure count
                        parserFailures = parserFailureCntMap.getOrDefault(parser, 0) + 1;
                        parserFailureCntMap.put(parser, parserFailures);
                        // print the failed parser map
                        log.error("current parserFailureCntMap ----> " + parserFailureCntMap);
                    } finally {
                        lastParser = parser;

                        // Exceeding threshold, discard parser
                        if (parserFailures >= EACH_PARSER_ATTEMPTS) {
                            log.error("Parser " + parser.getClass().getName() + " exceeded failure threshold and will be removed.");
                            // Increase the failed parsers count
                            failedParsersCount.incrementAndGet();
                            // discard the parser
                            parser = null;
                        }
                        // Return the parser back to the queue if not null
                        if (parser != null) {
                            try {
                                parserQueue.put(parser);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }
                        // Check if all parsers are now unusable based on the failure count
                        if (failedParsersCount.get() == parserList.size()) {
                            log.error("All parsers have failed. Terminating.");
                            executorService.shutdownNow();
                        }
                    }

                    // Sleep after each attempt no matter success or failure
                    log.info(String.format(
                            "IP Parse Summary => IP: %s | Attempt: %d | Result: %s | last Parser: %s | Sleeping for: %d seconds",
                            ip,
                            attempts,
                            (success ? "SUCCESS" : "FAILURE"),
                            lastParser.getClass().getSimpleName(),
                            sleepSeconds
                    ));
                    try {
                        TimeUnit.SECONDS.sleep(sleepSeconds);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }

                if (!success) {
                    log.error("parse failed too many times for IP: " + ip + ", terminating...");
                    executorService.shutdownNow();
                }
            });
        }

        executorService.shutdown();
        try {
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return new ArrayList<>(resultList);
    }
}