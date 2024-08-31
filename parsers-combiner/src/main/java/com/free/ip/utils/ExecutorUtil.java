package com.free.ip.utils;

import com.free.ip.parser.IpParser;
import com.free.ip.pojo.IpInfo;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

@Log4j2
public class ExecutorUtil {

    public static final int EACH_IP_ATTEMPTS = 5;

    public static final int EACH_PARSER_ATTEMPTS = 3;

    /**
     * Creates a thread pool where each thread runs an IpParser. Each IP is processed by a parser fetched from a
     * thread-safe queue. If parsing fails, it retries up to 3 times. The results are stored in a thread-safe list.
     * 将 IpParser 实例放入一个线程安全的队列中，每个线程可以从队列中取出一个 IpParser 实例进行处理，
     * 处理完成后再将 IpParser 实例放回队列。这样可以避免多个线程竞争同一个 IpParser 实例，从而提高执行效率
     *
     * @param ipList       the list of IPs to be processed
     * @param parserList   the list of IpParser instances to be used
     * @param sleepSeconds the number of seconds each thread should sleep after processing
     * @return a thread-safe list of IpInfo objects containing the results from all parsers
     */
    public static List<IpInfo> runParsers(List<String> ipList, List<IpParser> parserList, int sleepSeconds) {
        ConcurrentLinkedQueue<IpInfo> resultList = new ConcurrentLinkedQueue<>();

        // Create a blocking queue for the parsers
        BlockingQueue<IpParser> parserQueue = new LinkedBlockingQueue<>(parserList);

        // Map to track the consecutive failure count for each parser
        ConcurrentHashMap<IpParser, Integer> failureCountMap = new ConcurrentHashMap<>();

        // Counter to track the number of failed parsers
        AtomicInteger failedParsersCount = new AtomicInteger(0);

        ExecutorService executorService = Executors.newFixedThreadPool(parserList.size());
        for (String ip : ipList) {
            executorService.submit(() -> {
                boolean success = false;
                int attempts = 0;

                while (!success && attempts < EACH_IP_ATTEMPTS) {
                    IpParser parser = null;
                    IpParser lastParser = null;
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
                            failureCountMap.put(parser, 0);
                        } else {
                            throw new RuntimeException("Parser returned null");
                        }
                    } catch (Exception e) {
                        log.info(ip + "[" + attempts + "] failed with Parser => " + parser.getClass().getName());
                        attempts++;
                        success = false;
                        // Increment failure count
                        parserFailures = failureCountMap.getOrDefault(parser, 0) + 1;
                        failureCountMap.put(parser, parserFailures);
                    } finally {
                        lastParser = parser;

                        // Exceeding threshold, discard parser
                        if (parserFailures >= EACH_PARSER_ATTEMPTS) {
                            log.error("Parser " + parser.getClass().getName() + " exceeded failure threshold and will be removed.");
                            System.err.println("Parser " + parser.getClass().getName() + " exceeded failure threshold and will be removed.");
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
                            (lastParser != null ? lastParser.getClass().getSimpleName() : "N/A"),
                            sleepSeconds
                    ));
                    try {
                        TimeUnit.SECONDS.sleep(sleepSeconds);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
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