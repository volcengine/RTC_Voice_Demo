// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT

package com.volcengine.vertcdemo.voice.feature.chatroomlist;

import static com.volcengine.vertcdemo.core.net.rts.RTSInfo.KEY_RTS;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Keep;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vertcdemo.joinrtsparams.bean.JoinRTSRequest;
import com.vertcdemo.joinrtsparams.common.JoinRTSManager;
import com.volcengine.vertcdemo.common.IAction;
import com.volcengine.vertcdemo.common.SolutionBaseActivity;
import com.volcengine.vertcdemo.core.SolutionDataManager;
import com.volcengine.vertcdemo.core.eventbus.AppTokenExpiredEvent;
import com.volcengine.vertcdemo.core.eventbus.SolutionDemoEventManager;
import com.volcengine.vertcdemo.core.net.IRequestCallback;
import com.volcengine.vertcdemo.core.net.ServerResponse;
import com.volcengine.vertcdemo.core.net.rts.RTSBaseClient;
import com.volcengine.vertcdemo.core.net.rts.RTSInfo;
import com.volcengine.vertcdemo.utils.AppUtil;
import com.volcengine.vertcdemo.voice.bean.ChatRoomInfo;
import com.volcengine.vertcdemo.voice.bean.RoomListInfo;
import com.volcengine.vertcdemo.voice.core.Constants;
import com.volcengine.vertcdemo.voice.core.VoiceDataManger;
import com.volcengine.vertcdemo.voice.core.VoiceRTCManager;
import com.volcengine.vertcdemo.voice.core.VoiceRTSClient;
import com.volcengine.vertcdemo.voice.databinding.ActivityChatRoomListBinding;
import com.volcengine.vertcdemo.voice.feature.chatroommain.ChatRoomActivity;
import com.volcengine.vertcdemo.voice.feature.createchatroom.CreateChatRoomActivity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class ChatRoomListActivity extends SolutionBaseActivity {
    private static final int REQUEST_REFRESH_ROOM = 1345;

    private static final String TAG = "ChatRoomListActivity";

    private ActivityChatRoomListBinding mViewBinding;

    private ChatRoomListAdapter mChatRoomListAdapter;
    private RTSInfo mRTSInfo;

    private final ChatRoomListAdapter.OnChatInfoClickListener mOnCharInfoClick = info -> {
        Intent intent = new Intent(ChatRoomListActivity.this, ChatRoomActivity.class);
        intent.putExtra(Constants.EXTRA_KEY_CHAT_ROOM_ID, info.roomId);
        intent.putExtra(Constants.EXTRA_KEY_USER_NAME, info.hostUserName);
        intent.putExtra(Constants.EXTRA_KEY_IS_CREATE_ROOM, false);
        startActivityForResult(intent, REQUEST_REFRESH_ROOM);
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewBinding = ActivityChatRoomListBinding.inflate(getLayoutInflater());
        setContentView(mViewBinding.getRoot());

        Intent intent = getIntent();
        if (intent != null) {
            mRTSInfo = intent.getParcelableExtra(RTSInfo.KEY_RTS);
        }

        mViewBinding.voiceChatListTitleBarLayout.setLeftBack(v -> onBackPressed());
        mViewBinding.voiceChatListTitleBarLayout.setTitle("语音沙龙");
        mViewBinding.voiceChatListTitleBarLayout.setRightText("刷新", v -> getRoomList());

        mViewBinding.voiceChatListRv.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        mChatRoomListAdapter = new ChatRoomListAdapter(mOnCharInfoClick);
        mViewBinding.voiceChatListRv.setAdapter(mChatRoomListAdapter);

        mViewBinding.voiceChatListCreateRoom.setOnClickListener((v) ->
                startActivityForResult(new Intent(this, CreateChatRoomActivity.class), REQUEST_REFRESH_ROOM));

        SolutionDemoEventManager.register(this);

        initRTC();
    }

    /**
     * 初始化RTC
     */
    private void initRTC() {
        VoiceRTCManager.ins().initEngine(mRTSInfo);
        RTSBaseClient rtsClient = VoiceRTCManager.ins().getRTSClient();
        if (rtsClient == null) {
            finish();
            return;
        }
        rtsClient.login(mRTSInfo.rtsToken, (resultCode, message) -> {
            if (resultCode == RTSBaseClient.LoginCallBack.SUCCESS) {
                getRoomList();
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        VoiceRTCManager.ins().getRTSClient().removeAllEventListener();
        VoiceRTCManager.ins().getRTSClient().logout();
        VoiceRTCManager.ins().destroyEngine();
        VoiceDataManger.release();
        SolutionDemoEventManager.unregister(this);
    }

    private void getRoomList() {
        VoiceRTSClient rtsClient = VoiceRTCManager.ins().getRTSClient();
        if (rtsClient != null) {
            rtsClient.requestRoomList(new IRequestCallback<RoomListInfo>() {
                @Override
                public void onSuccess(RoomListInfo data) {
                    setRoomList(data.infos);
                }

                @Override
                public void onError(int errorCode, String message) {

                }
            });
        }
    }

    private void setRoomList(List<ChatRoomInfo> infoList) {
        List<ChatRoomInfo> list = new ArrayList<>();
        if (infoList != null) {
            list.addAll(infoList);
        }
        mChatRoomListAdapter.setData(list);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTokenExpiredEvent(AppTokenExpiredEvent event) {
        finish();
    }

    @Keep
    @SuppressWarnings("unused")
    public static void prepareSolutionParams(Activity activity, IAction<Object> doneAction) {
        Log.d(TAG, "prepareSolutionParams() invoked");
        IRequestCallback<ServerResponse<RTSInfo>> callback = new IRequestCallback<ServerResponse<RTSInfo>>() {
            @Override
            public void onSuccess(ServerResponse<RTSInfo> response) {
                RTSInfo data = response == null ? null : response.getData();
                if (data == null || !data.isValid()) {
                    onError(-1, "");
                    return;
                }
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setClass(AppUtil.getApplicationContext(), ChatRoomListActivity.class);
                intent.putExtra(KEY_RTS, data);
                activity.startActivity(intent);
                if (doneAction != null) {
                    doneAction.act(null);
                }
            }

            @Override
            public void onError(int errorCode, String message) {
                if (doneAction != null) {
                    doneAction.act(null);
                }
            }
        };
        JoinRTSRequest request = new JoinRTSRequest(Constants.SOLUTION_NAME_ABBR,SolutionDataManager.ins().getToken());
        JoinRTSManager.setAppInfoAndJoinRTM(request, callback);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (REQUEST_REFRESH_ROOM == requestCode) {
            // refresh the list
            getRoomList();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
