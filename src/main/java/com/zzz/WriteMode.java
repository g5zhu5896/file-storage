package com.zzz;

/**
 * 写入模式
 */
public enum WriteMode {
    //覆盖模式，会删除当前文件重写
    OVERRIDE,
    //追加模式，会在最新文件后面添加，当文件夹已有写入数据文件时会导致 build配置的 charset、fileMaxSize、indexFixedWidthFactor无效
    APPEND,
    //不允许重复写入
    NOT_ALLOW_REPEATED;

}
