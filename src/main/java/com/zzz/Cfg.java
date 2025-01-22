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
     * 行和索引宽度的映射
     * key 为所有文件内的真实行数
     * value 为行对应索引的固定宽度
     * 需要按key 排序，因为后面查询时需要从小到大遍历
     */
    TreeMap<Integer, Long> columnIndexWidthMap = Maps.newTreeMap();

}
