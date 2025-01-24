package com.zzz;

import com.google.common.collect.Maps;
import lombok.Data;

import java.util.Map;
import java.util.TreeMap;

@Data
public class Cfg {
    /**
     * 每个文件的最大写入数量
     */
    private Integer fileMaxSize;
    /**
     * 总行数
     */
    private Integer totalSize;
    /**
     * 编码
     */
    private String charset;

    /**
     * 索引文件索引间固定宽度因子
     * 默认基于写入的第一条数据的宽度+3
     */
    private Long indexFixedWidthFactor;

    /**
     * 最新文件的当前写入行
     */
    private Integer newCurrentRow = 1;

    /**
     * 最新的数据文件的当前索引间固定宽度大小
     */
    private Long newIndexFixedWidth = 0l;

    /**
     * 最新数据文件的最后行偏移量
     */
    private long newDataFilePointer;
    /**
     * 最新索引文件的最后行偏移量
     */
    private long newIndexFilePointer;

    /**
     * 行和索引宽度的映射
     * key 为所有文件内的真实行数
     * value 为行对应索引的固定宽度
     * 需要按key 排序，因为后面查询时需要从小到大遍历
     */
    TreeMap<Integer, Long> columnIndexWidthMap = Maps.newTreeMap();

}
