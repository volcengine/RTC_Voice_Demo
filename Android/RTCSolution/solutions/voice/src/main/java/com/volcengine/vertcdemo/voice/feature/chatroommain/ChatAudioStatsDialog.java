// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.voice.feature.chatroommain;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDialog;

import com.volcengine.vertcdemo.common.WindowUtils;
import com.volcengine.vertcdemo.core.eventbus.SolutionDemoEventManager;
import com.volcengine.vertcdemo.voice.R;
import com.volcengine.vertcdemo.voice.event.SDKStreamStatsEvent;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Locale;

public class ChatAudioStatsDialog extends AppCompatDialog {

    private final View mView;
    private final TextView mAudioChannel;
    private final TextView mUploadSampleRate;
    private final TextView mUploadBitrate;
    private final TextView mUploadLossRate;
    private final TextView mDownloadBitrate;
    private final TextView mDownloadLossRate;
    private final TextView mDelay;

    public ChatAudioStatsDialog(Context context) {
        super(context, R.style.SolutionCommonDialog);
        setCancelable(true);

        mView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_voice_audio_stats, null);
        mView.findViewById(R.id.dialog_voice_audio_stats_hover).setOnClickListener(v -> dismiss());
        mView.findViewById(R.id.dialog_voice_audio_stats_close).setOnClickListener(v -> dismiss());
        mAudioChannel = mView.findViewById(R.id.dialog_voice_audio_channel_value);
        mUploadSampleRate = mView.findViewById(R.id.dialog_voice_audio_upload_sample_rate_value);
        mUploadBitrate = mView.findViewById(R.id.dialog_voice_audio_upload_bitrate_value);
        mUploadLossRate = mView.findViewById(R.id.dialog_voice_audio_upload_loss_rate_value);
        mDownloadBitrate = mView.findViewById(R.id.dialog_voice_audio_download_bitrate_value);
        mDownloadLossRate = mView.findViewById(R.id.dialog_voice_audio_download_loss_rate_value);
        mDelay = mView.findViewById(R.id.dialog_voice_audio_delay_value);

        onSDKStreamStatsEvent(new SDKStreamStatsEvent());
    }

    @Override
    public void show() {
        super.show();
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowUtils.getScreenWidth(getContext());
        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(params);
        getWindow().setContentView(mView, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        SolutionDemoEventManager.register(this);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        SolutionDemoEventManager.unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSDKStreamStatsEvent(SDKStreamStatsEvent event) {
        mAudioChannel.setText(String.valueOf(event.audioChannel));
        mUploadSampleRate.setText(String.valueOf(event.uploadSampleRate));
        mUploadBitrate.setText(String.format(Locale.ENGLISH, "%dkbps", (int) event.uploadBitrate));
        mUploadLossRate.setText(String.format(Locale.ENGLISH,"%d%%", (int) event.uploadLossRate));
        mDownloadBitrate.setText(String.format(Locale.ENGLISH,"%dkbps", (int) event.downloadBitrate));
        mDownloadLossRate.setText(String.format(Locale.ENGLISH,"%d%%", (int) event.downloadLossRate));
        mDelay.setText(String.format(Locale.ENGLISH, "%dms", event.delay));
    }
}
