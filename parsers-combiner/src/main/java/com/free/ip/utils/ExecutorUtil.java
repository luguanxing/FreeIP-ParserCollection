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
     * Creates a thread pool where each thread runs an IpParser. Each IP is processed by a randomly chosen parser.
     * If parsing fails, it retries up to 3 times. The results are stored in a thread-safe list.
     * 线程池里只有固定个数个parser，对每个ip抽取parser，
     * 抽中后加锁，若失败重新抽取parser，最多抽3次
     *
     * @param ipList       the list of IPs to be processed
     * @param parserList   the list of IpParser instances to be used
     * @param sleepSeconds the number of seconds each thread should sleep after processing
     * @return a thread-safe list of IpInfo objects containing the results from all parsers
     */
    public static List<IpInfo> runParsers(List<String> ipList, List<IpParser> parserList, int sleepSeconds) {
        ConcurrentLinkedQueue<IpInfo> resultList = new ConcurrentLinkedQueue<>();

        // Create a lock for each parser
        List<ReentrantLock> locks = new ArrayList<>();
        for (int i = 0; i < parserList.size(); i++) {
            locks.add(new ReentrantLock());
        }

        // each thread competes for any parser
        ExecutorService executorService = Executors.newFixedThreadPool(parserList.size());
        for (String ip : ipList) {
            executorService.submit(() -> {
                boolean success = false;
                int attempts = 0;

                while (!success && attempts < 3) {
                    int parserIndex = ThreadLocalRandom.current().nextInt(parserList.size());
                    IpParser parser = parserList.get(parserIndex);
                    ReentrantLock lock = locks.get(parserIndex);

                    // Blocking until the lock is acquired
                    try {
                        lock.lock();
                        log.info(ip + " uses Parser => " + parser.getClass().getName());
                        IpInfo info = parser.getIpInfo(ip);
                        if (info != null) {
                            resultList.add(info);
                            success = true;
                        }
                    } finally {
                        lock.unlock();
                    }

                    // Sleep after each attempt
                    try {
                        TimeUnit.SECONDS.sleep(sleepSeconds);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                    if (!success) {
                        log.info(ip + "[" + attempts + "] failed with Parser => " + parser.getClass().getName());
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