package com.volcengine.vertcdemo.voice.feature.chatroommain;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ss.video.rtc.demo.basic_module.utils.WindowUtils;
import com.volcengine.vertcdemo.voice.bean.ChatUserInfo;
import com.volcengine.vertcdemo.voice.bean.GetUserList;
import com.volcengine.vertcdemo.voice.event.JoinChatEvent;
import com.volcengine.vertcdemo.voice.event.LeaveChatEvent;
import com.volcengine.vertcdemo.voice.event.MicOffEvent;
import com.volcengine.vertcdemo.voice.event.MicOnEvent;
import com.volcengine.vertcdemo.voice.event.UserRaiseHandsEvent;
import com.volcengine.vertcdemo.core.eventbus.SolutionDemoEventManager;
import com.volcengine.vertcdemo.voice.bean.UserStatus;
import com.volcengine.vertcdemo.core.net.IRequestCallback;
import com.volcengine.vertcdemo.voice.R;
import com.volcengine.vertcdemo.voice.core.VoiceDataManger;
import com.volcengine.vertcdemo.voice.core.VoiceRTCManager;
import com.volcengine.vertcdemo.voice.core.VoiceRTSClient;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class ChatRaisingDialog extends AppCompatDialog {

    private final int mHighLightColor = Color.parseColor("#ffffff");
    private final int mNormalColor = Color.parseColor("#86909C");

    private final View mView;
    private final List<ChatUserInfo> mRaiseData = new ArrayList<>();
    private final List<ChatUserInfo> mListenerData = new ArrayList<>();
    private final RecyclerView mRaiseRv;
    private final RecyclerView mListenerRv;
    private final RaisingAdapter mRaiseAdapter;
    private final RaisingAdapter mListenerAdapter;


    public ChatRaisingDialog(Context context, UserOptionCallback userOptionCallback) {
        super(context, R.style.CommonDialog);
        setCancelable(true);

        mView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_voice_users, null);

        mView.findViewById(R.id.dialog_voice_users_hover).setOnClickListener((v) -> dismiss());

        mRaiseRv = mView.findViewById(R.id.dialog_voice_users_speaker);
        mRaiseRv.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        mRaiseAdapter = new RaisingAdapter(mRaiseData, userOptionCallback);
        mRaiseRv.setAdapter(mRaiseAdapter);

        mListenerRv = mView.findViewById(R.id.dialog_voice_users_listener);
        mListenerRv.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        mListenerAdapter = new RaisingAdapter(mListenerData, userOptionCallback);
        mListenerRv.setAdapter(mListenerAdapter);

        TextView raiseTv = mView.findViewById(R.id.dialog_voice_users_raise_list);
        TextView listenerTv = mView.findViewById(R.id.dialog_voice_users_listener_list);
        View raiseIndicator = mView.findViewById(R.id.dialog_voice_users_raise_indicator);
        View listenerIndicator = mView.findViewById(R.id.dialog_voice_users_listener_indicator);
        mRaiseRv.setVisibility(View.VISIBLE);
        mListenerRv.setVisibility(View.GONE);
        raiseTv.setTextColor(mHighLightColor);
        listenerTv.setTextColor(mNormalColor);
        raiseIndicator.setVisibility(View.VISIBLE);
        listenerIndicator.setVisibility(View.GONE);
        mView.findViewById(R.id.dialog_voice_users_raise_list).setOnClickListener(v -> {
            mRaiseRv.setVisibility(View.VISIBLE);
            mListenerRv.setVisibility(View.GONE);
            raiseTv.setTextColor(mHighLightColor);
            listenerTv.setTextColor(mNormalColor);
            raiseIndicator.setVisibility(View.VISIBLE);
            listenerIndicator.setVisibility(View.GONE);
        });
        mView.findViewById(R.id.dialog_voice_users_listener_list).setOnClickListener(v -> {
            mRaiseRv.setVisibility(View.GONE);
            mListenerRv.setVisibility(View.VISIBLE);
            raiseTv.setTextColor(mNormalColor);
            listenerTv.setTextColor(mHighLightColor);
            raiseIndicator.setVisibility(View.GONE);
            listenerIndicator.setVisibility(View.VISIBLE);
        });
        mView.findViewById(R.id.dialog_voice_users_close).setOnClickListener(v -> {
            dismiss();
        });
    }

    public void setRaiseUserList(List<ChatUserInfo> infoList) {
        mRaiseData.clear();
        if (infoList != null) {
            mRaiseData.addAll(infoList);
        }
        mRaiseAdapter.setData(infoList);
    }

    public void setListenerUserList(List<ChatUserInfo> infoList) {
        mListenerData.clear();
        if (infoList != null) {
            mListenerData.addAll(infoList);
        }
        mListenerAdapter.setData(infoList);
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
        initData();
    }

    @Override
    public void dismiss() {
        super.dismiss();
        SolutionDemoEventManager.unregister(this);
    }

    private void initData() {
        VoiceRTSClient rtmClient = VoiceRTCManager.getRTSClient();
        if (rtmClient != null) {
            rtmClient.requestRaiseHandUserList(new IRequestCallback<GetUserList>() {
                @Override
                public void onSuccess(GetUserList data) {
                    if (isShowing()) {
                        setRaiseUserList(data.users);
                    }
                }

                @Override
                public void onError(int errorCode, String message) {

                }
            });
            rtmClient.requestListenerUserList(new IRequestCallback<GetUserList>() {
                @Override
                public void onSuccess(GetUserList data) {
                    if (isShowing()) {
                        setListenerUserList(data.users);
                    }
                }

                @Override
                public void onError(int errorCode, String message) {

                }
            });
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCSJoinMeetingEvent(JoinChatEvent event) {
        if (VoiceDataManger.isSelf(event.user.userId)) {
            return;
        }
        initData();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCSLeaveMeetingEvent(LeaveChatEvent event) {
        if (VoiceDataManger.isSelf(event.user.userId)) {
            return;
        }
        initData();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCSRaiseHandsMicEvent(UserRaiseHandsEvent event) {
        ChatUserInfo info = mListenerAdapter.removeUser(event.userId);
        if (info != null) {
            mRaiseAdapter.addUser(info);
        }
        mRaiseAdapter.updateUserStatusChanged(event.userId, UserStatus.UserStatusRaiseHands.getStatus());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCSMicOnEvent(MicOnEvent event) {
        ChatUserInfo info = mListenerAdapter.removeUser(event.user.userId);
        if (info != null) {
            mRaiseAdapter.addUser(info);
        }
        mRaiseAdapter.updateUserStatusChanged(event.user.userId, UserStatus.UserStatusOnMicrophone.getStatus());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCSMicOffEvent(MicOffEvent event) {
        ChatUserInfo info = mRaiseAdapter.removeUser(event.userId);
        if (info != null) {
            mListenerAdapter.addUser(info);
        }
        mListenerAdapter.updateUserStatusChanged(event.userId, UserStatus.UserStatusAudience.getStatus());
    }


    private static class RaisingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final List<ChatUserInfo> mData = new ArrayList<>();
        private final UserOptionCallback mUserOptionCallback;

        public RaisingAdapter(List<ChatUserInfo> data, UserOptionCallback userOptionCallback) {
            mData.addAll(data);
            mUserOptionCallback = userOptionCallback;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_voice_users, parent, false);
            return new RaisingViewHolder(view, mUserOptionCallback);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof RaisingViewHolder) {
                ((RaisingViewHolder) holder).bind(mData.get(position));
            }
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        public void setData(List<ChatUserInfo> userInfo) {
            mData.clear();
            if (userInfo != null) {
                mData.addAll(userInfo);
            }
            notifyDataSetChanged();
        }

        public void updateUserStatusChanged(String uid, int status) {
            if (TextUtils.isEmpty(uid)) {
                return;
            }
            for (int i = 0; i < mData.size(); i++) {
                if (TextUtils.equals(mData.get(i).userId, uid)) {
                    mData.get(i).userStatus = status;
                    notifyItemChanged(i);
                    return;
                }
            }
        }

        public void addUser(ChatUserInfo info) {
            if (info == null || TextUtils.isEmpty(info.userId)) {
                return;
            }
            for (int i = 0; i < mData.size(); i++) {
                if (TextUtils.equals(mData.get(i).userId, info.userId)) {
                    return;
                }
            }
            mData.add(info);
            notifyItemInserted(mData.size());
        }

        public ChatUserInfo removeUser(String uid) {
            if (TextUtils.isEmpty(uid)) {
                return null;
            }
            for (int i = 0; i < mData.size(); i++) {
                if (TextUtils.equals(mData.get(i).userId, uid)) {
                    ChatUserInfo info = mData.remove(i);
                    notifyItemRemoved(i);
                    return info;
                }
            }
            return null;
        }
    }

    private static class RaisingViewHolder extends RecyclerView.ViewHolder {

        private final TextView mUserPrefixTv;
        private final TextView mUserNameTv;
        private final TextView mOptionTv;
        private final UserOptionCallback mUserOptionCallback;
        private ChatUserInfo mInfo;

        public RaisingViewHolder(@NonNull View itemView, UserOptionCallback userOptionCallback) {
            super(itemView);
            mUserPrefixTv = itemView.findViewById(R.id.item_voice_user_prefix);
            mUserNameTv = itemView.findViewById(R.id.item_voice_user_name);
            mOptionTv = itemView.findViewById(R.id.item_voice_user_option);
            mUserOptionCallback = userOptionCallback;
            mOptionTv.setOnClickListener((v) -> {
                if (mInfo != null && mUserOptionCallback != null) {
                    ChatUserInfo info = mInfo;
                    if (info.userStatus == UserStatus.UserStatusOnMicrophone.getStatus()) {
                        mUserOptionCallback.onClick(mInfo, true);
                    } else {
                        mUserOptionCallback.onClick(mInfo, false);
                    }
                }
            });
        }

        public void bind(ChatUserInfo info) {
            mInfo = info;
            mUserPrefixTv.setText(info.userName.substring(0, 1));
            mUserNameTv.setText(info.userName);
            if (info.userStatus == UserStatus.UserStatusRaiseHands.getStatus()) {
                mOptionTv.setText("同意");
                mOptionTv.setBackgroundResource(R.drawable.item_voice_listener_option_unselected_bg);
            } else if (info.userStatus == UserStatus.UserStatusOnMicrophone.getStatus()) {
                mOptionTv.setText("下麦");
                mOptionTv.setBackgroundResource(R.drawable.item_voice_listener_option_selected_bg);
            } else {
                mOptionTv.setText("邀请上麦");
                mOptionTv.setBackgroundResource(R.drawable.item_voice_listener_option_unselected_bg);
            }
        }
    }

    public interface UserOptionCallback {
        void onClick(ChatUserInfo info, boolean needShowDialog);
    }
}