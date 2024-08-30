package com.free.ip.utils;

import com.free.ip.parser.IpParser;
import com.free.ip.pojo.IpInfo;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

@Log4j2
public class ExecutorUtil {

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

        ExecutorService executorService = Executors.newFixedThreadPool(parserList.size());
        for (String ip : ipList) {
            executorService.submit(() -> {
                boolean success = false;
                int attempts = 0;

                while (!success && attempts < 3) {
                    IpParser parser = null;
                    try {
                        // Fetch a parser from safe queue and wait until successful
                        parser = parserQueue.take();
                        // Use the parser to get the IP info
                        log.info(ip + " uses Parser => " + parser.getClass().getName());
                        IpInfo info = parser.getIpInfo(ip);
                        if (info != null) {
                            resultList.add(info);
                            success = true;
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    } finally {
                        if (parser != null) {
                            // Put the parser back into the safe queue
                            try {
                                parserQueue.put(parser);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }
                    }

                    // Sleep after each attempt
                    try {
                        TimeUnit.SECONDS.sleep(sleepSeconds);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                    if (!success) {
                        log.info(ip + "[" + attempts + "] failed with Parser => " + (parser != null ? parser.getClass().getName() : "null"));
                        attempts++;
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