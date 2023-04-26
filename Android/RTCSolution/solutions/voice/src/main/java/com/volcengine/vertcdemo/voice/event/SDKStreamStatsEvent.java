// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.voice.event;

/**
 * SDK 音频数据统计更新事件
 */
public class SDKStreamStatsEvent {
    public int audioChannel; // 本地音频声道数
    public int uploadSampleRate; // 音频上行采样率
    public float uploadBitrate; // 音频上行码率
    public float uploadLossRate; // 音频上行丢包率
    public float downloadBitrate; // 音频下行码率
    public float downloadLossRate; // 音频下行丢包率
    public int delay; // 本地音频声道数
}
