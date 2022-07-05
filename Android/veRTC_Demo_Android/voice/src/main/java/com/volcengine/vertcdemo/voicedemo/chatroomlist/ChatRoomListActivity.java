package com.volcengine.vertcdemo.voicedemo.chatroomlist;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ss.video.rtc.demo.basic_module.acivities.BaseActivity;
import com.ss.video.rtc.demo.basic_module.utils.SafeToast;
import com.ss.video.rtc.demo.basic_module.utils.WindowUtils;
import com.volcengine.vertcdemo.bean.ChatRoomInfo;
import com.volcengine.vertcdemo.bean.RoomListInfo;
import com.volcengine.vertcdemo.core.VoiceConstants;
import com.volcengine.vertcdemo.core.eventbus.SolutionDemoEventManager;
import com.volcengine.vertcdemo.core.eventbus.TokenExpiredEvent;
import com.volcengine.vertcdemo.core.net.IRequestCallback;
import com.volcengine.vertcdemo.core.net.rtm.RTMBaseClient;
import com.volcengine.vertcdemo.core.net.rtm.RtmInfo;
import com.volcengine.vertcdemo.voice.R;
import com.volcengine.vertcdemo.voicedemo.createchatroom.CreateChatRoomActivity;
import com.volcengine.vertcdemo.core.VoiceChatDataManger;
import com.volcengine.vertcdemo.core.VoiceRtcManager;
import com.volcengine.vertcdemo.core.VoiceRtmClient;
import com.volcengine.vertcdemo.voicedemo.chatroommain.ChatRoomActivity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.LinkedList;
import java.util.List;

public class ChatRoomListActivity extends BaseActivity {

    private ChatRoomListAdapter mChatRoomListAdapter;
    private RtmInfo mRtmInfo;

    private final ChatRoomListAdapter.OnChatInfoClickListener mOnCharInfoClick = info -> {
        Intent intent = new Intent(ChatRoomListActivity.this, ChatRoomActivity.class);
        intent.putExtra(VoiceConstants.EXTRA_KEY_CHAT_ROOM_ID, info.roomId);
        intent.putExtra(VoiceConstants.EXTRA_KEY_USER_NAME, info.hostUserName);
        intent.putExtra(VoiceConstants.EXTRA_KEY_IS_CREATE_ROOM, false);
        startActivity(intent);
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent != null) {
            mRtmInfo = intent.getParcelableExtra(RtmInfo.KEY_RTM);
            VoiceChatDataManger.init(mRtmInfo);
        }
        setContentView(R.layout.activity_chat_room_list);
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

        RecyclerView chatRoomListRv = findViewById(R.id.voice_chat_list_rv);
        chatRoomListRv.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        mChatRoomListAdapter = new ChatRoomListAdapter(mOnCharInfoClick);
        chatRoomListRv.setAdapter(mChatRoomListAdapter);

        findViewById(R.id.voice_chat_list_create_room).setOnClickListener((v) ->
                startActivity(new Intent(ChatRoomListActivity.this, CreateChatRoomActivity.class)));

        SolutionDemoEventManager.register(this);
        initRTC();
    }

    /**
     * 初始化RTC
     */
    private void initRTC() {
        RTMBaseClient rtmClient = VoiceRtcManager.getRtmClient();
        if (rtmClient == null) {
            finish();
            return;
        }
        rtmClient.login(mRtmInfo.rtmToken, (resultCode, message) -> {
            if (resultCode == RTMBaseClient.LoginCallBack.SUCCESS) {
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
    public void finish() {
        super.finish();
        VoiceRtcManager.getRtmClient().removeAllEventListener();
        VoiceRtcManager.getRtmClient().logout();
        VoiceChatDataManger.release();
        SolutionDemoEventManager.unregister(this);
    }

    private void getRoomList() {
        VoiceRtmClient rtmClient = VoiceRtcManager.getRtmClient();
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
        List<ChatRoomInfo> list = new LinkedList<>();
        if (infoList != null) {
            list.addAll(infoList);
        }
        mChatRoomListAdapter.setData(list);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTokenExpiredEvent(TokenExpiredEvent event) {
        finish();
    }
}
