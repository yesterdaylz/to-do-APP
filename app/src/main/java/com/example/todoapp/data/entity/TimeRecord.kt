package com.example.todoapp.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
/**
 * 用于封装时间记录的实体类
 *
 *包含模式、开始时间、结束时间、持续时间、番茄个数
 *
 * 番茄个数规则：正计时与倒计时模式下，默认为1；
 *
 * 番茄钟模式下，根据实际完成的番茄数记录
 *
 * 通过username字段关联用户表
 */

@Entity(tableName = "time_record")
data class TimeRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val mode: String,
    val startTime: Long,
    val endTime: Long,
    val duration: Int,
    val tomatoCount: Int = 1//计时与倒计时模式下，默认为1
)
