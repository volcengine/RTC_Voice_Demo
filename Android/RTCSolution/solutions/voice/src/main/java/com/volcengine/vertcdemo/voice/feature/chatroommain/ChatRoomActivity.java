// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.voice.feature.chatroommain;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Process;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.volcengine.vertcdemo.common.SolutionBaseActivity;
import com.volcengine.vertcdemo.common.SolutionCommonDialog;
import com.volcengine.vertcdemo.common.SolutionToast;
import com.volcengine.vertcdemo.core.AudioVideoConfig;
import com.volcengine.vertcdemo.core.SolutionDataManager;
import com.volcengine.vertcdemo.core.eventbus.AppTokenExpiredEvent;
import com.volcengine.vertcdemo.core.eventbus.SolutionDemoEventManager;
import com.volcengine.vertcdemo.core.net.IRequestCallback;
import com.volcengine.vertcdemo.voice.R;
import com.volcengine.vertcdemo.voice.bean.ChatMsgInfo;
import com.volcengine.vertcdemo.voice.bean.ChatRoomInfo;
import com.volcengine.vertcdemo.voice.bean.ChatUserInfo;
import com.volcengine.vertcdemo.voice.bean.CreateJoinRoomResult;
import com.volcengine.vertcdemo.voice.bean.HostChangeInfo;
import com.volcengine.vertcdemo.voice.bean.UserStatus;
import com.volcengine.vertcdemo.voice.bean.VoiceReconnectResponse;
import com.volcengine.vertcdemo.voice.core.Constants;
import com.volcengine.vertcdemo.voice.core.VoiceDataManger;
import com.volcengine.vertcdemo.voice.core.VoiceRTCManager;
import com.volcengine.vertcdemo.voice.core.VoiceRTSClient;
import com.volcengine.vertcdemo.voice.event.ChatEndEvent;
import com.volcengine.vertcdemo.voice.event.InviteMicEvent;
import com.volcengine.vertcdemo.voice.event.JoinChatEvent;
import com.volcengine.vertcdemo.voice.event.LeaveChatEvent;
import com.volcengine.vertcdemo.voice.event.MicOffEvent;
import com.volcengine.vertcdemo.voice.event.MicOnEvent;
import com.volcengine.vertcdemo.voice.event.MuteMicEvent;
import com.volcengine.vertcdemo.voice.event.SDKAudioPropertiesEvent;
import com.volcengine.vertcdemo.voice.event.UnmuteMicEvent;
import com.volcengine.vertcdemo.voice.event.UserRaiseHandsEvent;
import com.volcengine.vertcdemo.voice.event.VoiceReconnectToRoomEvent;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class ChatRoomActivity extends SolutionBaseActivity {
    private final int mHighLightColor = Color.parseColor("#4080FF");
    private final int mNormalColor = Color.parseColor("#86909C");

    private ChatRoomAdapter mChatRoomAdapter;
    private ChatRoomChatAdapter mChatRoomChatAdapter;

    private View mToastLayout;
    private View mMuteLocalLayout;
    private TextView mToastTv;
    private TextView mRoomIdTv;
    private TextView mDurationTv;
    private TextView mMicStatusTv;
    private TextView mRaiseHandTv;
    private ImageView mRaiseHandIv;
    private ImageView mMicStatusIv;
    private ImageView mAudioStatsIv;
    private RecyclerView mChatRv;

    private long mLastTs = 0;
    private long mEnterTs = 0;

    private String mUserId = "";
    private String mHostUserId = "";
    private boolean mIsSpeaker = false;
    private boolean mIsRaiseDialogShowing = false;
    private boolean mIsRaiseHand = false;
    private boolean mIsSomeoneRaiseHand = false;
    private boolean mIsOpen = true;
    private SolutionCommonDialog mDialog;

    private final ChatRaisingDialog.UserOptionCallback mUserOptionCallback = (info, needShowDialog) -> {

        if (needShowDialog) {
            if (mDialog != null) {
                mDialog.dismiss();
            }
            SolutionCommonDialog dialog = new SolutionCommonDialog(this);
            dialog.setMessage("是否将该用户下麦？");
            dialog.setPositiveListener(dialogView -> {
                VoiceRTSClient rtsClient = VoiceRTCManager.ins().getRTSClient();
                if (rtsClient != null) {
                    rtsClient.onUserOption(info);
                }
                dialog.dismiss();
            });
            dialog.setNegativeListener(dialogView -> dialog.dismiss());
            dialog.show();
            mDialog = dialog;
            return;
        }
        VoiceRTSClient rtsClient = VoiceRTCManager.ins().getRTSClient();
        if (rtsClient != null) {
            rtsClient.onUserOption(info);
        }
    };

    private final Runnable mAutoDismiss = () -> {
        if (isFinishing()) {
            return;
        }
        if (mToastLayout != null) {
            mToastLayout.setVisibility(View.GONE);
        }
    };

    private final Runnable mDurationCounting = new Runnable() {
        @Override
        public void run() {
            if (!isFinishing()) {
                updateDuration();
                if (mDurationTv != null) {
                    mDurationTv.postDelayed(mDurationCounting, 500);
                }
            }
        }
    };

    private final IRequestCallback<VoiceReconnectResponse> mReconnectCallback = new IRequestCallback<VoiceReconnectResponse>() {
        @Override
        public void onSuccess(VoiceReconnectResponse data) {
            onReconnect(data);
        }

        @Override
        public void onError(int errorCode, String message) {
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        View roomTitleBar = findViewById(R.id.layout_room_title_bar);

        ViewCompat.setOnApplyWindowInsetsListener(roomTitleBar, (v, windowInsets) -> {
            final Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            mlp.topMargin = insets.top;
            v.setLayoutParams(mlp);
            return WindowInsetsCompat.CONSUMED;
        });

        Intent intent = getIntent();
        boolean isCreateRoom = intent.getBooleanExtra(Constants.EXTRA_KEY_IS_CREATE_ROOM, true);
        String roomId = intent.getStringExtra(Constants.EXTRA_KEY_CHAT_ROOM_ID);
        String roomTitle = intent.getStringExtra(Constants.EXTRA_KEY_ROOM_TITLE);
        String userName = SolutionDataManager.ins().getUserName();

        mUserId = VoiceDataManger.getUid();

        ImageView leaveBtn = findViewById(R.id.chat_room_leave);
        leaveBtn.setOnClickListener(v -> attemptLeaveRoom());
        mRoomIdTv = findViewById(R.id.chat_room_id);
        mRoomIdTv.setText(roomId);
        mDurationTv = findViewById(R.id.chat_room_duration);
        mDurationTv.setText("00:00");
        TextView prefix = findViewById(R.id.chat_room_host_prefix);
        if (!TextUtils.isEmpty(userName)) {
            prefix.setText(userName.substring(0, 1));
        }

        mMuteLocalLayout = findViewById(R.id.voice_chat_room_mute_local_layout);
        mMicStatusIv = findViewById(R.id.voice_chat_room_mute_local_btn);
        mMicStatusTv = findViewById(R.id.voice_chat_room_mute_local_txt);
        mMicStatusIv.setOnClickListener((v) -> publishMic(!mIsOpen));


        RecyclerView userRv = findViewById(R.id.layout_room_user_rv);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(ChatRoomActivity.this, 4);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return position == 0 ? 4 : 1;
            }
        });
        userRv.setLayoutManager(gridLayoutManager);
        mChatRoomAdapter = new ChatRoomAdapter();
        userRv.setAdapter(mChatRoomAdapter);

        ChatRoomInfo chatRoomInfo = new ChatRoomInfo();
        chatRoomInfo.hostUserName = roomTitle;
        mChatRoomAdapter.setChatRoomInfo(chatRoomInfo);

        mChatRv = findViewById(R.id.layout_room_chat_rv);
        mChatRv.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        mChatRoomChatAdapter = new ChatRoomChatAdapter();
        mChatRv.setAdapter(mChatRoomChatAdapter);

        mToastLayout = findViewById(R.id.room_toast_layout);
        mToastTv = findViewById(R.id.room_toast_text);
        mToastLayout.setOnClickListener((v) -> {
            mToastLayout.setVisibility(View.GONE);
        });

        mRaiseHandTv = findViewById(R.id.voice_chat_room_raise_hand_txt);
        mRaiseHandIv = findViewById(R.id.voice_chat_room_raise_hand_btn);
        mRaiseHandIv.setOnClickListener((v) -> {
            if (isHost()) {
                openRaisingList();
            } else {
                if (mIsSpeaker) {
                    confirmBecomeListener();
                } else {
                    mIsRaiseHand = true;
                    VoiceRTSClient rtsClient = VoiceRTCManager.ins().getRTSClient();
                    if (rtsClient != null) {
                        rtsClient.requestRaiseHand();
                    }
                    requestPermissions(Manifest.permission.RECORD_AUDIO);
                    updateUI();
                }
            }
        });
        findViewById(R.id.voice_chat_room_audio_stats_btn).setOnClickListener((v) -> {
            openAudioStatsList();
        });

        updateUI();
        SolutionDemoEventManager.register(this);
        if (!isCreateRoom) {
            VoiceRTSClient rtsClient = VoiceRTCManager.ins().getRTSClient();
            if (rtsClient != null) {
                rtsClient.requestJoinRoom(roomId, SolutionDataManager.ins().getUserName(), new IRequestCallback<CreateJoinRoomResult>() {
                    @Override
                    public void onSuccess(CreateJoinRoomResult data) {
                        onCreateJoinRoomResult(data);
                    }

                    @Override
                    public void onError(int errorCode, String message) {
                        onCreateJoinRoomResult(null, null, null);
                    }
                });
            }
        } else {
            onCreateJoinRoomResult(VoiceDataManger.sToken, VoiceDataManger.sRoomInfo, VoiceDataManger.getUserList());
        }


        requestPermissions(Manifest.permission.RECORD_AUDIO);
    }


    /**
     * 控制音频流发送
     *
     * @param publish 发送
     */
    private void publishMic(boolean publish) {
        mIsOpen = publish;
        mMicStatusIv.setImageResource(publish ? R.drawable.voice_audio_enable : R.drawable.voice_audio_disable);
        mMicStatusTv.setText(publish ? "静音自己" : "取消静音");
        VoiceRTSClient rtsClient = VoiceRTCManager.ins().getRTSClient();
        if (rtsClient != null) {
            rtsClient.switchMic(publish);
        }
    }

    private boolean isHost() {
        return !TextUtils.isEmpty(mUserId) && TextUtils.equals(mUserId, mHostUserId);
    }

    @Override
    public void onBackPressed() {
        attemptLeaveRoom();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SolutionDemoEventManager.unregister(this);
    }

    public void attemptLeaveRoom() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ChatRoomActivity.this, R.style.transparentDialog);
        View view = getLayoutInflater().inflate(R.layout.layout_leave_voice_room, null);
        builder.setView(view);
        TextView titleTv = view.findViewById(R.id.leave_chat_title);
        TextView confirmTv = view.findViewById(R.id.leave_chat_confirm);
        TextView cancelTv = view.findViewById(R.id.leave_chat_cancel);
        builder.setCancelable(true);
        final AlertDialog dialog = builder.create();
        if (isHost()) {
            if (mChatRoomAdapter.getSpeakerCount() > 1) {
                titleTv.setText("离开会将房间移交给下一位连麦主播，是否确认离开？");
            } else {
                titleTv.setText("离开会解散房间，是否确认离开？");
            }
        } else {
            titleTv.setText("是否确认离开房间？");
        }

        confirmTv.setOnClickListener((v) -> {
            dialog.dismiss();
            leaveRoom();
            ChatRoomActivity.this.finish();
        });

        cancelTv.setOnClickListener((v) -> dialog.dismiss());
        dialog.show();
    }

    private void leaveRoom() {
        VoiceRTSClient rtsClient = VoiceRTCManager.ins().getRTSClient();
        if (rtsClient != null) {
            rtsClient.requestLeaveRoom();
        }
        VoiceRTCManager.ins().leaveRoom();
    }

    private void openRaisingList() {
        ChatRaisingDialog chatRaisingDialog = new ChatRaisingDialog(this, mUserOptionCallback);
        chatRaisingDialog.setOnDismissListener((dialogInterface) -> {
            mIsRaiseDialogShowing = false;
            mIsSomeoneRaiseHand = false;
            updateUI();
        });
        chatRaisingDialog.show();
        mIsRaiseDialogShowing = true;
    }

    private void openAudioStatsList() {
        ChatAudioStatsDialog chatRaisingDialog = new ChatAudioStatsDialog(this);
        chatRaisingDialog.show();
    }

    private List<ChatUserInfo> getUserList(List<ChatUserInfo> userInfoList, boolean isSpeaker) {
        if (userInfoList == null || userInfoList.isEmpty()) {
            return null;
        }
        ArrayList<ChatUserInfo> speakers = new ArrayList<>();
        for (ChatUserInfo info : userInfoList) {
            if (info != null) {
                if (isSpeaker) {
                    if (info.userStatus == UserStatus.UserStatusOnMicrophone.getStatus() || info.isHost) {
                        speakers.add(info);
                    }
                } else {
                    if (info.userStatus != UserStatus.UserStatusOnMicrophone.getStatus() && !info.isHost) {
                        speakers.add(info);
                    }
                }
            }
        }
        return speakers;
    }

    private void startCounting() {
        mDurationTv.removeCallbacks(mDurationCounting);
        mDurationTv.post(mDurationCounting);
    }

    private void updateDuration() {
        long duration = System.currentTimeMillis() - mEnterTs + mLastTs;
        long min = duration / 1000 / 60;
        long s = (duration / 1000) % 60;
        String str = "";
        if (min < 10) {
            str = str + "0";
        }
        str += min;
        str += ":";
        if (s < 10) {
            str = str + "0";
        }
        str += s;
        mDurationTv.setText(str);
    }

    private void updateUI() {
        int micVisible = (mIsSpeaker || isHost()) ? View.VISIBLE : View.GONE;
        mMuteLocalLayout.setVisibility(micVisible);

        if (!isHost()) {
            if (mIsSpeaker) {
                mRaiseHandIv.setImageResource(R.drawable.voice_disable_raise_hand_unselected);
                mRaiseHandTv.setTextColor(mNormalColor);
                mRaiseHandTv.setText("下麦");
            } else {
                if (mIsRaiseHand) {
                    mRaiseHandIv.setImageResource(R.drawable.voice_raise_hand_selected);
                    mRaiseHandTv.setTextColor(mHighLightColor);
                } else {
                    mRaiseHandIv.setImageResource(R.drawable.voice_raise_hand_unselected);
                    mRaiseHandTv.setTextColor(mNormalColor);
                }
                mRaiseHandTv.setText("举手上麦");
            }
        } else {
            mRaiseHandTv.setText("列表管理");
            if (mIsRaiseDialogShowing || !mIsSomeoneRaiseHand) {
                mRaiseHandIv.setImageResource(R.drawable.voice_user_list_unselected);
                mRaiseHandTv.setTextColor(mNormalColor);
            } else {
                if (mIsSomeoneRaiseHand) {
                    mRaiseHandIv.setImageResource(R.drawable.voice_raise_hand_selected);
                    mRaiseHandTv.setTextColor(mHighLightColor);
                } else {
                    mRaiseHandIv.setImageResource(R.drawable.voice_user_list_unselected);
                    mRaiseHandTv.setTextColor(mNormalColor);
                }
            }
        }
    }

    private void showToast(String toast, boolean isShort) {
        mToastLayout.setVisibility(View.VISIBLE);
        mToastLayout.removeCallbacks(mAutoDismiss);
        if (isShort) {
            mToastLayout.postDelayed(mAutoDismiss, 2000);
        }
        mToastTv.setText(toast);
    }

    private void confirmBecomeListener() {
        if (mDialog != null) {
            mDialog.dismiss();
        }
        SolutionCommonDialog dialog = new SolutionCommonDialog(this);
        dialog.setMessage("是否确认下麦？");
        dialog.setPositiveListener(v -> {
            VoiceRTSClient rtsClient = VoiceRTCManager.ins().getRTSClient();
            if (rtsClient != null) {
                rtsClient.requestBecomeListener();
            }
            dialog.dismiss();
        });
        dialog.setNegativeListener(v -> dialog.dismiss());
        dialog.show();
        mDialog = dialog;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onJoinRoomEvent(JoinChatEvent event) {
        if (VoiceDataManger.isSelf(event.user.userId)) {
            return;
        }
        ChatMsgInfo info = new ChatMsgInfo();
        info.content = event.user.userName + "  加入了房间";
        mChatRoomChatAdapter.addChatMsgInfo(info);
        mChatRoomAdapter.addUser(event.user);
        mChatRv.post(() -> mChatRv.smoothScrollToPosition(mChatRoomChatAdapter.getItemCount()));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLeaveChatEvent(LeaveChatEvent event) {
        if (VoiceDataManger.isSelf(event.user.userId)) {
            return;
        }
        ChatMsgInfo info = new ChatMsgInfo();
        info.content = event.user.userName + "  退出了房间";
        mChatRoomChatAdapter.addChatMsgInfo(info);
        mChatRoomAdapter.removeUser(event.user);
        mChatRv.post(() -> mChatRv.smoothScrollToPosition(mChatRoomChatAdapter.getItemCount()));
    }

    private void onCreateJoinRoomResult(@NonNull CreateJoinRoomResult data) {
        VoiceDataManger.setInfo(data);
        onCreateJoinRoomResult(data.token, data.info, data.users);
    }

    private void onCreateJoinRoomResult(String token, ChatRoomInfo roomInfo, List<ChatUserInfo> users) {
        if (roomInfo != null && users != null) {
            mHostUserId = roomInfo.hostUserId;

            List<ChatUserInfo> speakers = getUserList(users, true);
            List<ChatUserInfo> audiences = getUserList(users, false);
            mIsOpen = false;
            for (ChatUserInfo info : users) {
                if (TextUtils.equals(info.userId, mUserId)) {
                    mIsRaiseHand = info.userStatus == UserStatus.UserStatusRaiseHands.getStatus();
                    mIsOpen = info.isMicOn && isHost();
                    break;
                }
            }
            mChatRoomAdapter.setSpeakerList(speakers);
            mChatRoomAdapter.setListenerList(audiences);
            mChatRoomAdapter.setChatRoomInfo(roomInfo);

            mLastTs = Math.max(0, (roomInfo.now - roomInfo.createdAt) / 1000000);
            mEnterTs = System.currentTimeMillis();
            mRoomIdTv.setText(roomInfo.roomId);
            VoiceRTCManager.ins().joinRoom(token, roomInfo.roomId, mUserId);
            VoiceRTCManager.ins().enableLocalAudio(isHost());
            publishMic(mIsOpen && isHost());
            startCounting();

            updateUI();
        } else {
            VoiceDataManger.clearInfo();
            SolutionCommonDialog dialog = new SolutionCommonDialog(this);
            dialog.setCancelable(false);
            dialog.setMessage("加入房间失败，回到房间列表页");
            dialog.setPositiveListener((v) -> finish());
            dialog.show();

            leaveRoom();
        }
    }

    private void onReconnect(VoiceReconnectResponse response) {
        if (response != null && response.info != null && response.users != null) {
            VoiceDataManger.setInfo(response);
            List<ChatUserInfo> speakers = getUserList(response.users, true);
            List<ChatUserInfo> audiences = getUserList(response.users, false);
            mIsOpen = false;
            for (ChatUserInfo info : response.users) {
                if (TextUtils.equals(info.userId, mUserId)) {
                    mIsRaiseHand = info.userStatus == UserStatus.UserStatusRaiseHands.getStatus();
                    mIsOpen = info.isMicOn && isHost();
                    break;
                }
            }
            mChatRoomAdapter.setSpeakerList(speakers);
            mChatRoomAdapter.setListenerList(audiences);
            mChatRoomAdapter.setChatRoomInfo(response.info);

            updateUI();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRaiseHandsMicEvent(UserRaiseHandsEvent event) {
        mChatRoomAdapter.updateRaiseHandStatus(event.userId);
        mIsSomeoneRaiseHand = true;
        updateUI();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onInviteMicEvent(InviteMicEvent event) {
        if (!VoiceDataManger.isSelf(event.userId)) {
            return;
        }
        if (mDialog != null) {
            mDialog.dismiss();
        }
        if (TextUtils.equals(event.userId, mUserId)) {
            SolutionCommonDialog dialog = new SolutionCommonDialog(this);
            dialog.setMessage("主播邀请您上麦");
            dialog.setPositiveListener(v -> {
                VoiceRTSClient rtsClient = VoiceRTCManager.ins().getRTSClient();
                if (rtsClient != null) {
                    rtsClient.confirmBecomeSpeaker();
                }
                dialog.dismiss();
            });
            dialog.setNegativeListener(v -> dialog.dismiss());
            dialog.show();
            mDialog = dialog;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMicOnEvent(MicOnEvent event) {
        if (TextUtils.equals(event.user.userId, mUserId)) {
            if (!hasAudioPermission()) {
                SolutionToast.show("麦克风权限已关闭，请至设备设置页开启");
                requestPermissions(Manifest.permission.RECORD_AUDIO);
            }
            showToast("您已经成功上麦", true);
            mIsSpeaker = true;
            mIsRaiseHand = false;
            VoiceRTCManager.ins().enableLocalAudio(true);
            publishMic(true);
        }
        mChatRoomAdapter.onUserRoleChange(event.user.userId, true);
        updateUI();
    }

    public boolean hasAudioPermission() {
        int res = checkPermission(Manifest.permission.RECORD_AUDIO, android.os.Process.myPid(), Process.myUid());
        return res == PackageManager.PERMISSION_GRANTED;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMicOffEvent(MicOffEvent event) {
        if (TextUtils.equals(event.userId, mUserId)) {
            showToast("您已回到听众席", true);
            mIsSpeaker = false;
            VoiceRTCManager.ins().enableLocalAudio(false);
            publishMic(false);
        }
        mChatRoomAdapter.onUserRoleChange(event.userId, false);
        updateUI();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onChatEndEvent(ChatEndEvent event) {
        SolutionToast.show("房间已解散");
        leaveRoom();
        finish();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMuteMicEvent(MuteMicEvent event) {
        mChatRoomAdapter.updateUserMicStatus(event.userId, false);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUnmuteMicEvent(UnmuteMicEvent event) {
        mChatRoomAdapter.updateUserMicStatus(event.userId, true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSDKAudioPropertiesEvent(SDKAudioPropertiesEvent event) {
        if (event.audioPropertiesList == null || event.audioPropertiesList.size() == 0) {
            return;
        }
        List<String> uidList = new ArrayList<>();
        for (SDKAudioPropertiesEvent.SDKAudioProperties info : event.audioPropertiesList) {
            if (info.audioPropertiesInfo.linearVolume >= AudioVideoConfig.VOLUME_MIN_THRESHOLD) {
                if (!TextUtils.isEmpty(info.userId)) {
                    uidList.add(info.userId);
                }
            }
        }
        mChatRoomAdapter.updateVolumeStatus(uidList);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHostChangeEvent(HostChangeInfo event) {
        if (event.hostInfo != null && !TextUtils.isEmpty(event.hostInfo.userId)) {
            mHostUserId = event.hostInfo.userId;
            if (TextUtils.equals(mHostUserId, SolutionDataManager.ins().getUserId())) {
                SolutionToast.show(R.string.self_become_host);
            }
        }
        if (TextUtils.equals(mUserId, event.hostInfo == null ? null : event.hostInfo.userId)) {
            mIsSomeoneRaiseHand = false;
            updateUI();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTokenExpiredEvent(AppTokenExpiredEvent event) {
        leaveRoom();
        finish();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReconnectEvent(VoiceReconnectToRoomEvent event) {
        String roomId = getIntent().getStringExtra(Constants.EXTRA_KEY_CHAT_ROOM_ID);
        VoiceRTCManager.ins().getRTSClient().requestReconnect(roomId, mReconnectCallback);
    }
}
