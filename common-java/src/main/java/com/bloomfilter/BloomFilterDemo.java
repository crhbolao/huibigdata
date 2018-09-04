package com.bloomfilter;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created with IntelliJ IDEA.
 * User: bolao
 * Date: 2018/9/4 13:38
 * Version: V1.0
 * To change this template use File | Settings | File Templates.
 * Description:  BloomFilterDemo  布隆过滤器
 */
public class BloomFilterDemo {

    /**
     * 初始化布隆过滤器的大小
     */
    private static int size = 100000;

    /**
     * 初始化 bloomFileter
     */
    private static BloomFilter<Integer> bloomFilter = BloomFilter.create(Funnels.integerFunnel(), size,  0.0001);

    /**
     * 表示遗漏的总数
     */
    private static AtomicInteger lostCount = new AtomicInteger(0);

    /**
     * 表示误判的总数
     */
    private static AtomicInteger errorCount = new AtomicInteger(0);

    public void dataFilter(){

        for (int i = 0; i< size; i++){
            bloomFilter.put(i);
        }

        for (int i = 0; i < size; i++){
            if (!bloomFilter.mightContain(i)){
                System.out.println("有坏人逃脱了！！！");
                lostCount.getAndIncrement();
            }
        }

        for (int i = size + 10000; i < size + 20000; i++){
            if (bloomFilter.mightContain(i)){
                errorCount.getAndIncrement();
            }
        }

        System.out.println("遗漏的数量为：" + lostCount.get());
        System.out.println("误判的数量为：" + errorCount.get());

    }

    public static void main(String[] args) {
        BloomFilterDemo bloomFilterDemo = new BloomFilterDemo();
        bloomFilterDemo.dataFilter();
    }

}
