package com.treemap;

import java.util.Comparator;
import java.util.TreeMap;

/**
 * Created with IntelliJ IDEA.
 * User: bolao
 * Date: 2018/8/23 14:37
 * Version: V1.0
 * To change this template use File | Settings | File Templates.
 * Description:    主要是用来积累用到的treeMap操作
 */
public class TreeMapUtils {

    /**
     * 使用treeMap 按从小到大的顺序进行排序
     */
    public void compare() {
        TreeMap<Integer, String> treeMap = new TreeMap<Integer, String>(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1.compareTo(o2);
            }
        });
        treeMap.put(1, "hui");
        treeMap.put(5, "zhang");
        treeMap.put(3, "li");
        System.out.println("第一次" + treeMap);
        treeMap.put(2, "zhao");
        System.out.println("第二次" + treeMap);
    }


    public static void main(String[] args) {
        TreeMapUtils treeMapUtils = new TreeMapUtils();
        // 用来测试treeMap的排序
        treeMapUtils.compare();
    }

}
