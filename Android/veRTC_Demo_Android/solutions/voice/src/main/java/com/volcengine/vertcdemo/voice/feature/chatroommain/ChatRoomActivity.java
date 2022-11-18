package com.volcengine.vertcdemo.voice.feature.chatroommain;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Process;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ss.video.rtc.demo.basic_module.acivities.BaseActivity;
import com.ss.video.rtc.demo.basic_module.ui.CommonDialog;
import com.ss.video.rtc.demo.basic_module.utils.SafeToast;
import com.ss.video.rtc.demo.basic_module.utils.Utilities;
import com.ss.video.rtc.demo.basic_module.utils.WindowUtils;
import com.volcengine.vertcdemo.core.AudioVideoConfig;
import com.volcengine.vertcdemo.core.SolutionDataManager;
import com.volcengine.vertcdemo.core.eventbus.SolutionDemoEventManager;
import com.volcengine.vertcdemo.core.eventbus.TokenExpiredEvent;
import com.volcengine.vertcdemo.core.net.IRequestCallback;
import com.volcengine.vertcdemo.voice.R;
import com.volcengine.vertcdemo.voice.bean.ChatMsgInfo;
import com.volcengine.vertcdemo.voice.bean.ChatRoomInfo;
import com.volcengine.vertcdemo.voice.bean.ChatUserInfo;
import com.volcengine.vertcdemo.voice.bean.CreateJoinRoomResult;
import com.volcengine.vertcdemo.voice.bean.HostChangeInfo;
import com.volcengine.vertcdemo.voice.bean.UserStatus;
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
import com.volcengine.vertcdemo.voice.event.UserRaiseHandsEvent;
import com.volcengine.vertcdemo.voice.event.SDKAudioPropertiesEvent;
import com.volcengine.vertcdemo.voice.event.UnmuteMicEvent;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class ChatRoomActivity extends BaseActivity {
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
    private CommonDialog mDialog;

    private final ChatRaisingDialog.UserOptionCallback mUserOptionCallback = (info, needShowDialog) -> {

        if (needShowDialog) {
            if (mDialog != null) {
                mDialog.dismiss();
            }
            CommonDialog dialog = new CommonDialog(this);
            dialog.setMessage("是否将该用户下麦？");
            dialog.setPositiveListener(dialogView -> {
                VoiceRTSClient rtmClient = VoiceRTCManager.getRTSClient();
                if (rtmClient != null) {
                    rtmClient.onUserOption(info);
                }
                dialog.dismiss();
            });
            dialog.setNegativeListener(dialogView -> dialog.dismiss());
            dialog.show();
            mDialog = dialog;
            return;
        }
        VoiceRTSClient rtmClient = VoiceRTCManager.getRTSClient();
        if (rtmClient != null) {
            rtmClient.onUserOption(info);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        requestPermissions(Manifest.permission.RECORD_AUDIO);
    }

    @Override
    protected void onGlobalLayoutCompleted() {
        super.onGlobalLayoutCompleted();

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
        mMicStatusIv.setOnClickListener((v) -> switchMic(!mIsOpen));


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
                    VoiceRTSClient rtmClient = VoiceRTCManager.getRTSClient();
                    if (rtmClient != null) {
                        rtmClient.requestRaiseHand();
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
            VoiceRTSClient rtmClient = VoiceRTCManager.getRTSClient();
            if (rtmClient != null) {
                rtmClient.requestJoinRoom(roomId, SolutionDataManager.ins().getUserName(), new IRequestCallback<CreateJoinRoomResult>() {
                    @Override
                    public void onSuccess(CreateJoinRoomResult data) {
                        onCreateJoinRoomResult(data);
                    }

                    @Override
                    public void onError(int errorCode, String message) {
                        onCreateJoinRoomResult(null);
                    }
                });
            }
        } else {
            onCreateJoinRoomResult(VoiceDataManger.sCreateRoomResult);
        }
    }

    @Override
    protected void setupStatusBar() {
        WindowUtils.setLayoutFullScreen(getWindow());
    }

    private void switchMic(boolean isOpen) {
        mIsOpen = isOpen;
        mMicStatusIv.setImageResource(isOpen ? R.drawable.voice_audio_enable : R.drawable.voice_audio_disable);
        mMicStatusTv.setText(isOpen ? "静音自己" : "取消静音");
        VoiceRTSClient rtmClient = VoiceRTCManager.getRTSClient();
        if (rtmClient != null){
            rtmClient.switchMic(isOpen);
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PERMISSION_GRANTED) {
            switchMic(true);
        }
    }

    public void attemptLeaveRoom() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ChatRoomActivity.this, R.style.transparentDialog);
        View view = getLayoutInflater().inflate(R.layout.layout_leave_voice_room, null);
        builder.setView(view);
        TextView titleTv = view.findViewById(R.id.leave_meeting_title);
        TextView confirmTv = view.findViewById(R.id.leave_meeting_confirm);
        TextView cancelTv = view.findViewById(R.id.leave_meeting_cancel);
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
        VoiceRTSClient rtmClient = VoiceRTCManager.getRTSClient();
        if (rtmClient != null) {
            rtmClient.requestLeaveRoom();
        }
        VoiceRTCManager.leaveRoom();
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
        CommonDialog dialog = new CommonDialog(this);
        dialog.setMessage("是否确认下麦？");
        dialog.setPositiveListener(v -> {
            VoiceRTSClient rtmClient = VoiceRTCManager.getRTSClient();
            if (rtmClient != null) {
                rtmClient.requestBecomeListener();
            }
            dialog.dismiss();
        });
        dialog.setNegativeListener(v -> dialog.dismiss());
        dialog.show();
        mDialog = dialog;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCSJoinMeetingEvent(JoinChatEvent event) {
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
    public void onCSLeaveMeetingEvent(LeaveChatEvent event) {
        if (VoiceDataManger.isSelf(event.user.userId)) {
            return;
        }
        ChatMsgInfo info = new ChatMsgInfo();
        info.content = event.user.userName + "  离开了房间";
        mChatRoomChatAdapter.addChatMsgInfo(info);
        mChatRoomAdapter.removeUser(event.user);
        mChatRv.post(() -> mChatRv.smoothScrollToPosition(mChatRoomChatAdapter.getItemCount()));
    }

    private void onCreateJoinRoomResult(CreateJoinRoomResult event) {
        VoiceDataManger.setRoomInfo(event);
        if (event != null && event.info != null && event.users != null) {
            mHostUserId = event.info.hostUserId;

            List<ChatUserInfo> speakers = getUserList(event.users, true);
            List<ChatUserInfo> audiences = getUserList(event.users, false);
            mIsOpen = false;
            for (ChatUserInfo info : event.users) {
                if (TextUtils.equals(info.userId, mUserId)) {
                    mIsRaiseHand = info.userStatus == UserStatus.UserStatusRaiseHands.getStatus();
                    mIsOpen = info.isMicOn && isHost();
                    break;
                }
            }
            mChatRoomAdapter.setSpeakerList(speakers);
            mChatRoomAdapter.setListenerList(audiences);
            mChatRoomAdapter.setChatRoomInfo(event.info);

            mLastTs = Math.max(0, (event.info.now - event.info.createdAt) / 1000000);
            mEnterTs = System.currentTimeMillis();
            mRoomIdTv.setText(event.info.roomId);
            VoiceRTCManager.joinRoom(event.token, event.info.roomId, mUserId);
            switchMic(mIsOpen);
            startCounting();

            updateUI();
        } else {
            CommonDialog dialog = new CommonDialog(this);
            dialog.setCancelable(false);
            dialog.setMessage("加入房间失败，回到房间列表页");
            dialog.setPositiveListener((v) -> finish());
            dialog.show();

            leaveRoom();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCSRaiseHandsMicEvent(UserRaiseHandsEvent event) {
        mChatRoomAdapter.updateRaiseHandStatus(event.userId);
        mIsSomeoneRaiseHand = true;
        updateUI();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCSInviteMicEvent(InviteMicEvent event) {
        if (!VoiceDataManger.isSelf(event.userId)) {
            return;
        }
        if (mDialog != null) {
            mDialog.dismiss();
        }
        if (TextUtils.equals(event.userId, mUserId)) {
            CommonDialog dialog = new CommonDialog(this);
            dialog.setMessage("主播邀请您上麦");
            dialog.setPositiveListener(v -> {
                VoiceRTSClient rtmClient = VoiceRTCManager.getRTSClient();
                if (rtmClient != null) {
                    rtmClient.confirmBecomeSpeaker();
                }
                dialog.dismiss();
            });
            dialog.setNegativeListener(v -> dialog.dismiss());
            dialog.show();
            mDialog = dialog;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCSMicOnEvent(MicOnEvent event) {
        if (TextUtils.equals(event.user.userId, mUserId)) {
            if (!hasAudioPermission()) {
                SafeToast.show("麦克风权限已关闭，请至设备设置页开启");
                requestPermissions(Manifest.permission.RECORD_AUDIO);
            }
            showToast("您已经成功上麦", true);
            mIsSpeaker = true;
            mIsRaiseHand = false;
            switchMic(true);
        }
        mChatRoomAdapter.onUserRoleChange(event.user.userId, true);
        updateUI();
    }

    public static boolean hasAudioPermission() {
        int res = Utilities.getApplicationContext().checkPermission(
                Manifest.permission.RECORD_AUDIO, android.os.Process.myPid(), Process.myUid());
        return res == PackageManager.PERMISSION_GRANTED;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCSMicOffEvent(MicOffEvent event) {
        if (TextUtils.equals(event.userId, mUserId)) {
            showToast("您已回到听众席", true);
            mIsSpeaker = false;
            switchMic(false);
        }
        mChatRoomAdapter.onUserRoleChange(event.userId, false);
        updateUI();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCSMeetingEndEvent(ChatEndEvent event) {
        SafeToast.show("房间已解散");
        leaveRoom();
        finish();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCSMuteMicEvent(MuteMicEvent event) {
        mChatRoomAdapter.updateUserMicStatus(event.userId, false);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCSUnmuteMicEvent(UnmuteMicEvent event) {
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
    public void onCSHostChangeEvent(HostChangeInfo event) {
        if (event.hostInfo != null && !TextUtils.isEmpty(event.hostInfo.userId)) {
            mHostUserId = event.hostInfo.userId;
        }
        if (TextUtils.equals(mUserId, event.hostInfo == null ? null : event.hostInfo.userId)) {
            mIsSomeoneRaiseHand = false;
            updateUI();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTokenExpiredEvent(TokenExpiredEvent event) {
        leaveRoom();
        finish();
    }
}
