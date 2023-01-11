package com.volcengine.vertcdemo.voice.feature.createchatroom;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.ss.video.rtc.demo.basic_module.acivities.BaseActivity;
import com.ss.video.rtc.demo.basic_module.adapter.TextWatcherAdapter;
import com.ss.video.rtc.demo.basic_module.utils.SafeToast;
import com.ss.video.rtc.demo.basic_module.utils.WindowUtils;
import com.volcengine.vertcdemo.common.LengthFilterWithCallback;
import com.volcengine.vertcdemo.core.SolutionDataManager;
import com.volcengine.vertcdemo.core.net.ErrorTool;
import com.volcengine.vertcdemo.utils.DebounceClickListener;
import com.volcengine.vertcdemo.voice.core.Constants;
import com.volcengine.vertcdemo.voice.core.VoiceDataManger;
import com.volcengine.vertcdemo.voice.core.VoiceRTCManager;
import com.volcengine.vertcdemo.voice.core.VoiceRTSClient;
import com.volcengine.vertcdemo.core.eventbus.SolutionDemoEventManager;
import com.volcengine.vertcdemo.voice.bean.CreateJoinRoomResult;
import com.volcengine.vertcdemo.core.eventbus.TokenExpiredEvent;
import com.volcengine.vertcdemo.core.net.IRequestCallback;
import com.volcengine.vertcdemo.voice.R;
import com.volcengine.vertcdemo.voice.feature.chatroommain.ChatRoomActivity;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Pattern;

public class CreateChatRoomActivity extends BaseActivity {

    public static final String INPUT_REGEX = "^[\\u4e00-\\u9fa5a-zA-Z0-9@_-]+$";
    public static final int ROOM_NAME_MAX_LENGTH = 20; // 房间名称长度限制
    public static final int USER_NAME_MAX_LENGTH = 18; // 用户名称长度限制

    private View mRoomConfirmBtn;
    private View mToastLayout;
    private TextView mToastTv;
    private EditText mRoomTitleInput;
    private TextView mRoomTitleInputEt;
    private EditText mUserNameInput;
    private TextView mUserNameInputError;

    private boolean mUserNameOverflow = false; // 用户名字是否过长

    private final TextWatcherAdapter mUserNameTextWatcher = new TextWatcherAdapter() {

        @Override
        public void afterTextChanged(Editable s) {
            Log.d("afterTextChanged", s.toString());
            checkUserName(s.toString());
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_chat_room);
    }

    @Override
    protected void onGlobalLayoutCompleted() {
        super.onGlobalLayoutCompleted();

        ImageView backArrow = findViewById(R.id.title_bar_left_iv);
        backArrow.setImageResource(R.drawable.back_arrow);
        backArrow.setOnClickListener(v -> finish());
        TextView title = findViewById(R.id.title_bar_title_tv);
        title.setText("语音沙龙");

        mToastLayout = findViewById(R.id.create_room_toast_layout);
        mToastTv = findViewById(R.id.create_room_toast_text);
        mRoomConfirmBtn = findViewById(R.id.create_chat_room_btn);
        mRoomTitleInput = findViewById(R.id.create_chat_room_title_et);
        mRoomTitleInputEt = findViewById(R.id.create_chat_room_title_waring_tv);

        // 检查房间名称是否过长
        InputFilter roomNameFilter = new LengthFilterWithCallback(ROOM_NAME_MAX_LENGTH, (overflow) -> {
            mRoomTitleInputEt.setVisibility(overflow ? View.VISIBLE : View.INVISIBLE);
        });
        InputFilter[] roomNameFilters = new InputFilter[]{roomNameFilter};
        mRoomTitleInput.setFilters(roomNameFilters);

        mUserNameInput = findViewById(R.id.create_chat_room_id_et);
        mUserNameInputError = findViewById(R.id.create_chat_room_id_waring_tv);
        mUserNameInput.setText(SolutionDataManager.ins().getUserName());

        // 检查用户名称是否过长
        InputFilter userNameFilter = new LengthFilterWithCallback(USER_NAME_MAX_LENGTH, (overflow) -> {
            mUserNameOverflow = overflow;
            checkUserName(mUserNameInput.getText().toString());
        });
        InputFilter[] userNameFilters = new InputFilter[]{userNameFilter};
        mUserNameInput.setFilters(userNameFilters);

        mUserNameInput.removeTextChangedListener(mUserNameTextWatcher);
        mUserNameInput.addTextChangedListener(mUserNameTextWatcher);
        mRoomConfirmBtn.setOnClickListener(DebounceClickListener.create((v) -> {
            String roomTitle = mRoomTitleInput.getText().toString().trim();
            String userName = mUserNameInput.getText().toString().trim();
            if (TextUtils.isEmpty(roomTitle) || TextUtils.isEmpty(userName)) {
                showToast("输入不得为空");
                return;
            }
            if (userName.length() > USER_NAME_MAX_LENGTH || !Pattern.matches(INPUT_REGEX, userName)) {
                return;
            }
            VoiceRTSClient rtsClient = VoiceRTCManager.getRTSClient();
            if (rtsClient == null) {
                return;
            }
            try {
                final String encodeRoomTitle = URLEncoder.encode(roomTitle, "UTF-8");
                rtsClient.requestCreateRoom(encodeRoomTitle, userName, new IRequestCallback<CreateJoinRoomResult>() {
                    @Override
                    public void onSuccess(CreateJoinRoomResult data) {
                        onCreateJoinRoomResult(data);
                    }

                    @Override
                    public void onError(int errorCode, String message) {
                        SafeToast.show(ErrorTool.getErrorMessageByErrorCode(errorCode, message));
                    }
                });
            } catch (UnsupportedEncodingException ignored) {

            }
        }));
        SolutionDemoEventManager.register(this);
    }

    @Override
    protected void setupStatusBar() {
        WindowUtils.setLayoutFullScreen(getWindow());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SolutionDemoEventManager.unregister(this);
    }

    private void showToast(String toast) {
        mToastLayout.setVisibility(View.VISIBLE);
        mToastLayout.removeCallbacks(mAutoDismiss);
        mToastLayout.postDelayed(mAutoDismiss, 2000);
        mToastTv.setText(toast);
    }

    /**
     * 检查用户名称是否符合规范
     * @param userName 用户名称
     */
    private void checkUserName(String userName) {
        if (Pattern.matches(INPUT_REGEX, userName)) {
            mRoomConfirmBtn.setEnabled(true);
            if (mUserNameOverflow) {
                mUserNameInputError.setText(R.string.audio_input_wrong_content_waring);
                mUserNameInputError.setVisibility(View.VISIBLE);
            } else {
                mUserNameInputError.setVisibility(View.INVISIBLE);
            }
        } else {
            mRoomConfirmBtn.setEnabled(false);
            mUserNameInputError.setText(R.string.audio_input_wrong_content_waring);
            mUserNameInputError.setVisibility(View.VISIBLE);
        }
    }

    public void onCreateJoinRoomResult(CreateJoinRoomResult event) {
        VoiceDataManger.setRoomInfo(event);
        if (event.info != null && event.users != null) {
            String roomTitle = mRoomTitleInput.getText().toString().trim();
            String userName = mUserNameInput.getText().toString().trim();
            SolutionDataManager.ins().setUserName(userName);
            Intent intent = new Intent(CreateChatRoomActivity.this, ChatRoomActivity.class);
            intent.putExtra(Constants.EXTRA_KEY_USER_NAME, userName);
            intent.putExtra(Constants.EXTRA_KEY_ROOM_TITLE, roomTitle);
            startActivity(intent);
            CreateChatRoomActivity.this.finish();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTokenExpiredEvent(TokenExpiredEvent event) {
        finish();
    }
}
