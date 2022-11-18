package com.volcengine.vertcdemo.voice.feature.chatroomlist;

import static com.volcengine.vertcdemo.core.net.rts.RTSInfo.KEY_RTM;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Keep;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ss.video.rtc.demo.basic_module.acivities.BaseActivity;
import com.ss.video.rtc.demo.basic_module.utils.SafeToast;
import com.ss.video.rtc.demo.basic_module.utils.Utilities;
import com.ss.video.rtc.demo.basic_module.utils.WindowUtils;
import com.vertcdemo.joinrtsparams.bean.JoinRTSRequest;
import com.vertcdemo.joinrtsparams.common.JoinRTSManager;
import com.volcengine.vertcdemo.common.IAction;
import com.volcengine.vertcdemo.core.SolutionDataManager;
import com.volcengine.vertcdemo.core.eventbus.SolutionDemoEventManager;
import com.volcengine.vertcdemo.core.eventbus.TokenExpiredEvent;
import com.volcengine.vertcdemo.core.net.IRequestCallback;
import com.volcengine.vertcdemo.core.net.ServerResponse;
import com.volcengine.vertcdemo.core.net.rts.RTSBaseClient;
import com.volcengine.vertcdemo.core.net.rts.RTSInfo;
import com.volcengine.vertcdemo.voice.R;
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

public class ChatRoomListActivity extends BaseActivity {

    private static final String TAG = "ChatRoomListActivity";

    private ActivityChatRoomListBinding mViewBinding;

    private ChatRoomListAdapter mChatRoomListAdapter;
    private RTSInfo mRtmInfo;

    private final ChatRoomListAdapter.OnChatInfoClickListener mOnCharInfoClick = info -> {
        Intent intent = new Intent(ChatRoomListActivity.this, ChatRoomActivity.class);
        intent.putExtra(Constants.EXTRA_KEY_CHAT_ROOM_ID, info.roomId);
        intent.putExtra(Constants.EXTRA_KEY_USER_NAME, info.hostUserName);
        intent.putExtra(Constants.EXTRA_KEY_IS_CREATE_ROOM, false);
        startActivity(intent);
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewBinding = ActivityChatRoomListBinding.inflate(getLayoutInflater());
        setContentView(mViewBinding.getRoot());

        Intent intent = getIntent();
        if (intent != null) {
            mRtmInfo = intent.getParcelableExtra(RTSInfo.KEY_RTM);
        }
    }

    @Override
    protected void onGlobalLayoutCompleted() {
        super.onGlobalLayoutCompleted();

        ImageView backArrow = findViewById(R.id.title_bar_left_iv);
        backArrow.setImageResource(R.drawable.back_arrow);
        backArrow.setOnClickListener(v -> finish());
        TextView title = findViewById(R.id.title_bar_title_tv);
        title.setText("语音沙龙");
        TextView rightRv = findViewById(R.id.title_bar_right_tv);
        rightRv.setText("刷新");
        rightRv.setOnClickListener((v) -> getRoomList());

        mViewBinding.voiceChatListRv.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        mChatRoomListAdapter = new ChatRoomListAdapter(mOnCharInfoClick);
        mViewBinding.voiceChatListRv.setAdapter(mChatRoomListAdapter);

        mViewBinding.voiceChatListCreateRoom.setOnClickListener((v) ->
                startActivity(new Intent(ChatRoomListActivity.this, CreateChatRoomActivity.class)));

        SolutionDemoEventManager.register(this);

        initRTC();
    }

    /**
     * 初始化RTC
     */
    private void initRTC() {
        VoiceRTCManager.createEngine(mRtmInfo);
        RTSBaseClient rtmClient = VoiceRTCManager.getRTSClient();
        if (rtmClient == null) {
            finish();
            return;
        }
        rtmClient.login(mRtmInfo.rtmToken, (resultCode, message) -> {
            if (resultCode == RTSBaseClient.LoginCallBack.SUCCESS) {
                getRoomList();
            } else {
                SafeToast.show("Login Rtm Fail Error:" + resultCode + ",Message:" + message);
            }
        });
    }

    @Override
    protected void setupStatusBar() {
        WindowUtils.setLayoutFullScreen(getWindow());
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        VoiceRTCManager.getRTSClient().removeAllEventListener();
        VoiceRTCManager.getRTSClient().logout();
        VoiceRTCManager.destroyEngine();
        VoiceDataManger.release();
        SolutionDemoEventManager.unregister(this);
    }

    private void getRoomList() {
        VoiceRTSClient rtmClient = VoiceRTCManager.getRTSClient();
        if (rtmClient != null) {
            rtmClient.requestRoomList(new IRequestCallback<RoomListInfo>() {
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
    public void onTokenExpiredEvent(TokenExpiredEvent event) {
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
                intent.setClass(Utilities.getApplicationContext(), ChatRoomListActivity.class);
                intent.putExtra(KEY_RTM, data);
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
        JoinRTSRequest request = new JoinRTSRequest();
        request.scenesName = Constants.SOLUTION_NAME_ABBR;
        request.loginToken = SolutionDataManager.ins().getToken();

        JoinRTSManager.setAppInfoAndJoinRTM(request, callback);
    }
}
