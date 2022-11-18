//
//  CreateRoomViewController.m
//  veRTC_Demo
//
//  Created by on 2021/5/18.
//  
//

#import "VoiceCreateRoomViewController.h"
#import "VocieCreateTextFieldView.h"
#import "VoiceRoomViewController.h"

@interface VoiceCreateRoomViewController ()

@property (nonatomic, strong) VocieCreateTextFieldView *roomNameTextFieldView;
@property (nonatomic, strong) VocieCreateTextFieldView *userNameTextFieldView;
@property (nonatomic, strong) UIButton *joinButton;

@end

@implementation VoiceCreateRoomViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    self.view.backgroundColor = [UIColor colorFromHexString:@"#272E3B"];
    
    [self.view addSubview:self.roomNameTextFieldView];
    [self.roomNameTextFieldView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.mas_equalTo(30);
        make.right.mas_equalTo(-30);
        make.height.mas_equalTo(32);
        make.top.equalTo(self.navView.mas_bottom).offset(53);
    }];
    
    [self.view addSubview:self.userNameTextFieldView];
    [self.userNameTextFieldView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.mas_equalTo(30);
        make.right.mas_equalTo(-30);
        make.height.mas_equalTo(32);
        make.top.equalTo(self.roomNameTextFieldView.mas_bottom).offset(32);
    }];
    
    [self.view addSubview:self.joinButton];
    [self.joinButton mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.mas_equalTo(30);
        make.right.mas_equalTo(-30);
        make.height.mas_equalTo(50);
        make.top.equalTo(self.userNameTextFieldView.mas_bottom).offset(40);
    }];
    
    self.userNameTextFieldView.text = [LocalUserComponent userModel].name;
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    
    self.navTitle = @"语音沙龙";
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    
    [self.roomNameTextFieldView becomeFirstResponder];
}

- (void)joinButtonAction:(UIButton *)sender {
    if (IsEmptyStr(self.roomNameTextFieldView.text)) {
        [[ToastComponent shareToastComponent] showWithMessage:@"输入不得为空"];
        return;
    }
    if (IsEmptyStr(self.userNameTextFieldView.text) ||
        ![LocalUserComponent isMatchUserName:self.userNameTextFieldView.text]) {
        [[ToastComponent shareToastComponent] showWithMessage:@"输入不得为空"];
        return;
    }
    [[ToastComponent shareToastComponent] showLoading];
    __weak __typeof(self) wself = self;
    [VoiceRTMManager createMeeting:self.roomNameTextFieldView.text
                          userName:self.userNameTextFieldView.text
                             block:^(NSString * _Nonnull token, VoiceControlRoomModel * _Nonnull roomModel, NSArray<VoiceControlUserModel *> * _Nonnull lists, RTMACKModel * _Nonnull model) {
        if (model.result) {
            [PublicParameterComponent share].roomId = roomModel.room_id;
            VoiceRoomViewController *next = [[VoiceRoomViewController alloc] initWithToken:token roomModel:roomModel userLists:lists];
            [wself.navigationController pushViewController:next animated:YES];
            
            BaseUserModel *userModel = [LocalUserComponent userModel];
            userModel.name = wself.userNameTextFieldView.text;
            [LocalUserComponent updateLocalUserModel:userModel];
        } else {
            [[ToastComponent shareToastComponent] showWithMessage:model.message];
        }
        [[ToastComponent shareToastComponent] dismiss];
    }];
}

- (void)touchesBegan:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event {
    [self.roomNameTextFieldView resignFirstResponder];
    [self.userNameTextFieldView resignFirstResponder];
}

#pragma mark - getter

- (VocieCreateTextFieldView *)roomNameTextFieldView {
    if (!_roomNameTextFieldView) {
        _roomNameTextFieldView = [[VocieCreateTextFieldView alloc] initWithModify:NO];
        _roomNameTextFieldView.placeholderStr = @"请输入房间主题";
        _roomNameTextFieldView.maxLimit = 20;
        _roomNameTextFieldView.isCheckIllega = NO;
        _roomNameTextFieldView.errorMessage = @"房间主题长度限制 1-20位";
    }
    return _roomNameTextFieldView;
}

- (VocieCreateTextFieldView *)userNameTextFieldView {
    if (!_userNameTextFieldView) {
        _userNameTextFieldView = [[VocieCreateTextFieldView alloc] initWithModify:NO];
        _userNameTextFieldView.placeholderStr = @"请输入用户昵称";
        _userNameTextFieldView.maxLimit = 18;
    }
    return _userNameTextFieldView;
}

- (UIButton *)joinButton {
    if (!_joinButton) {
        _joinButton = [[UIButton alloc] init];
        _joinButton.backgroundColor = [UIColor colorFromHexString:@"#4080FF"];
        [_joinButton setTitle:@"进入房间" forState:UIControlStateNormal];
        [_joinButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
        _joinButton.titleLabel.font = [UIFont systemFontOfSize:16 weight:UIFontWeightRegular];
        [_joinButton addTarget:self action:@selector(joinButtonAction:) forControlEvents:UIControlEventTouchUpInside];
        _joinButton.layer.cornerRadius = 25;
        _joinButton.layer.masksToBounds = YES;
    }
    return _joinButton;
}


@end
