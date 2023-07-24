/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dell_lab
 */
public class TestConcurrencyAndParallelExecution {

    static final int NUM_THREADS = 20;
    //static AtomicLong cnt = new AtomicLong(0);
    static long cnt2 = 0;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
//        long t1 = System.currentTimeMillis();
//        cnt.set(doSequential(0, 100_000_000_000l));
//        System.out.println("cnt = " + cnt + " elapsed:" + (System.currentTimeMillis() - t1));
        
        long t2 = System.currentTimeMillis();
        doParallel(0, 10_000_000_000l);
        //System.out.println("cnt = " + cnt + " elapsed:" + (System.currentTimeMillis() - t2));
        System.out.println("cnt2 = " + cnt2 + " elapsed:" + (System.currentTimeMillis() - t2));
    }

    private static long doSequential(long from, long to) {
        long localCnt = 0;
        //cnt.set(0);
        for (long i = from; i < to; i++) {
            //for (int j = from; j < to; j++) {
                localCnt ++;
            //}
            
        }
        return localCnt;
    }

    private static long doParallel(long from, long to) {
        //cnt.set(0);
        cnt2=0;
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);
        long chunkSize = (to - from) / NUM_THREADS;
        long remainder = (to - from) % NUM_THREADS;

        for (int i = 0; i < NUM_THREADS; i++) {
            long start = from + i * chunkSize;
            long end = start + chunkSize + (i == NUM_THREADS - 1 ? remainder : 0);
            executor.submit(new CounterTask(start, end));
        }

        executor.shutdown();

        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return cnt2;
    }

    static class CounterTask implements Runnable {
        private final long start;
        private final long end;

        public CounterTask(long start, long end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public void run() {
            long localCnt = 0;
            for (long i = start; i < end; i++) {
                //for (int j = start; j < end; j++) {
                    localCnt ++;
                    //cnt.addAndGet(1);
                    //cnt2++;
                //}
                
            }
            //cnt.addAndGet(localCnt);
            cnt2+=localCnt;
        }
    }
}

