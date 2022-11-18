//
//  VoiceRoomViewController.m
//  veRTC_Demo
//
//  Created by on 2021/5/18.
//  
//

#import "VoiceRoomViewController.h"
#import "VoiceRoomView.h"
#import "VoiceRoomBottomView.h"
#import "VoiceRoomNavView.h"
#import "VoiceRoomUserListComponent.h"
#import "VoiceEndComponent.h"
#import "VoiceRoomParamComponent.h"
#import "VoiceRoomViewController+SocketControl.h"
#import "SystemAuthority.h"
#import "VoiceRTMManager.h"
#import "NetworkingTool.h"

@interface VoiceRoomViewController () <VoiceRoomNavViewDelegate, VoiceRoomBottomViewDelegate, VoiceRTCManagerDelegate>

@property (nonatomic, strong) VoiceRoomView *roomView;
@property (nonatomic, strong) VoiceRoomBottomView *bottomView;
@property (nonatomic, strong) VoiceRoomNavView *navView;
@property (nonatomic, strong) VoiceRoomParamComponent *paramComponent;
@property (nonatomic, strong) VoiceRoomUserListComponent *userListComponent;
@property (nonatomic, strong) VoiceEndComponent *endComponent;
@property (nonatomic, strong) BaseIMComponent *imComponent;

@property (nonatomic, copy) NSString *token;
@property (nonatomic, strong) VoiceControlRoomModel *roomModel;
@property (nonatomic, copy) NSArray<VoiceControlUserModel *> *userLists;

@end

@implementation VoiceRoomViewController

- (instancetype)initWithToken:(NSString *)token
                    roomModel:(VoiceControlRoomModel *)roomModel
                    userLists:(NSArray<VoiceControlUserModel *> *)userLists {
    self = [super init];
    if (self) {
        self.token = token;
        self.roomModel = roomModel;
        self.userLists = userLists;
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    self.view.backgroundColor = [UIColor clearColor];
    
    // 设置业务服务器监听
    // set RTS monitor
    [self addSocketListener];
    
    // UI 初始化
    // UI initialization
    [self addBgGradientLayer];
    [self addSubviewAndConstraints];
    
    // 加入业务服务器房间和RTC房间
    // Join business server room and RTC room
    [self loadDataWithJoinRoom];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    [self.navigationController setNavigationBarHidden:YES animated:NO];
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    self.navigationController.interactivePopGestureRecognizer.enabled = NO;
}

- (void)loadDataWithJoinRoom {
    if (NOEmptyStr(self.roomModel.room_id)) {
        // 如果是房主，无需加入业务房间，直接加入RTC房间。
        // Directly join the RTC room
        [self joinRTCRoom];
    } else {
        // 如果是观众，需先加入业务房间，然后再加入RTC房间。
        // join the room
        __weak __typeof(self) wself = self;
        [self joinServerRoom:^(BOOL result) {
            if (result) {
                [wself joinRTCRoom];
            }
        }];
    }
}

#pragma mark - Notification

- (void)voiceControlChange:(NSDictionary *)dic {
    // 收到断线重连通知
    // Receive disconnection reconnection notification
    if ([dic isKindOfClass:[NSDictionary class]]) {
        NSString *type = dic[@"type"];
        if ([type isEqualToString:@"resume"]) {
            self.userLists = dic[@"users"];
            self.roomModel = (VoiceControlRoomModel *)(dic[@"roomModel"]);
            [self updateRoomViewWithData];
        } else if ([type isEqualToString:@"exit"]) {
            [self hangUp];
            [[ToastComponent shareToastComponent] showWithMessage:@"房间已解散" delay:0.8];
        } else {
            
        }
    }
}

#pragma mark - Server Listener Action

- (void)addUser:(VoiceControlUserModel *)userModel {
    // 收到业务服务器消息，有人加入房间
    // Received from the business server, someone joined the room
    [self.roomView joinUser:userModel];
    [self.userListComponent update];
    
    [self addIMMessage:YES userModel:userModel];
}

- (void)removeUser:(VoiceControlUserModel *)userModel {
    // 收到业务服务器消息，有人离开房间
    // Received business server, someone left the room
    [self.roomView leaveUser:userModel.uid];
    [self.userListComponent update];
    [self addIMMessage:NO userModel:userModel];
}

- (void)receivedRaiseHandWithUser:(NSString *)uid {
    // 收到业务服务器消息，有人举手上麦
    // When the business server is received, someone raises his hand to put on the microphone
    if ([self currentLoginuserModel].is_host) {
        [self.bottomView replaceButtonStatus:VoiceRoomBottomStatusList newStatus:VoiceRoomBottomStatusListRed];
        [self.userListComponent update];
    }
    [self.roomView updateUserHand:uid isHand:YES];
    [self.roomView reloadData];
}

- (void)receivedRaiseHandInviteWithAudience {
    // 收到业务服务器消息，主播邀请您上麦
    // After receiving the business server, the host invites you to mic
    AlertActionModel *alertModel = [[AlertActionModel alloc] init];
    alertModel.title = @"确定";
    AlertActionModel *cancelModel = [[AlertActionModel alloc] init];
    cancelModel.title = @"取消";
    [[AlertActionManager shareAlertActionManager] showWithMessage:@"主播邀请您上麦" actions:@[cancelModel, alertModel]];
    alertModel.alertModelClickBlock = ^(UIAlertAction * _Nonnull action) {
        if ([action.title isEqualToString:@"确定"]) {
            [VoiceRTMManager confirmMicWithBlock:nil];
        }
    };
}

- (void)receivedRaiseHandSucceedWithUser:(VoiceControlUserModel *)userModel {
    // 收到业务服务器消息，有观众上麦成功
    // Received a message from the business server, and some viewers successfully uploaded the microphone
    [self.roomView audienceRaisedHandsSuccess:userModel];
    [self.userListComponent update];
    if ([userModel.uid isEqualToString:[LocalUserComponent userModel].uid]) {
        VoiceControlUserModel *localUser = [self currentLoginuserModel];
        localUser.user_status = 2;
        localUser.is_mic_on = YES;
        [self.bottomView updateBottomLists:[self getBottomListsWithModel:localUser]];
        [[ToastComponent shareToastComponent] showWithMessage:@"您已成功上麦"];
        [[VoiceRTCManager shareRtc] makeCoHost:YES];
        [[VoiceRTCManager shareRtc] muteLocalAudioStream:NO];
        [self checkMicrophoneSystemAuthority];
    }
}

- (void)receivedLowerHandSucceedWithUser:(NSString *)uid {
    // 收到业务服务器消息，有观众下麦成功
    // Received a message from the business server, and some viewers successfully downloaded the microphone
    [self.roomView hostLowerHandSuccess:uid];
    [self.userListComponent update];
    if ([uid isEqualToString:[LocalUserComponent userModel].uid]) {
        VoiceControlUserModel *localUser = [self currentLoginuserModel];
        localUser.user_status = 0;
        localUser.is_mic_on = NO;
        [self.bottomView updateBottomLists:[self getBottomListsWithModel:localUser]];
        [[ToastComponent shareToastComponent] showWithMessage:@"您已回到听众席"];
        [[VoiceRTCManager shareRtc] makeCoHost:NO];
    }
}

- (void)receivedMicChangeWithMute:(BOOL)isMute uid:(NSString *)uid {
    // 收到业务服务器消息，有用户麦克风状态变化
    // Received business server message, there is user microphone status change
    [self.roomView updateUserMic:uid isMute:isMute];
}

- (void)receivedHostChangeWithNewHostUid:(VoiceControlUserModel *)hostUser {
    [self.roomView updateHostUser:hostUser.uid];
    // 如果当前登录用户为新主持人
    // If the currently logged-in user is the new host
    if ([[LocalUserComponent userModel].uid isEqualToString:hostUser.uid]) {
        [self.bottomView updateBottomLists:[self getBottomListsWithModel:[self currentLoginuserModel]]];
        [self.bottomView updateButtonStatus:VoiceRoomBottomStatusMic close:!hostUser.is_mic_on isTitle:NO];
        [[VoiceRTCManager shareRtc] muteLocalAudioStream:!hostUser.is_mic_on];
        [[ToastComponent shareToastComponent] showWithMessage:@"您已成为主播"];
    }
}

- (void)receivedMeetingEnd {
    // 收到业务服务器消息，房间结束
    // The business server message is received, the room ends
    [self hangUp];
    VoiceControlUserModel *loginUserModel = [self currentLoginuserModel];
    if (!loginUserModel.is_host) {
        [[ToastComponent shareToastComponent] showWithMessage:@"房间已解散" delay:0.8];
    }
}

#pragma mark - Server Load Data

- (void)joinServerRoom:(void (^)(BOOL result))block {
    // 向业务服务器发起加入房间请求
    // Initiate a room join request to the business server
    __weak __typeof(self)wself = self;
    [[ToastComponent shareToastComponent] showLoading];
    [VoiceRTMManager joinVoice:[LocalUserComponent userModel].name
                         block:^(NSString * _Nonnull token,
                                 VoiceControlRoomModel * _Nonnull roomModel,
                                 NSArray<VoiceControlUserModel *> * _Nonnull lists,
                                 RTMACKModel * _Nonnull model) {
        if (model.result) {
            wself.userLists = lists;
            wself.token = token;
            wself.roomModel = roomModel;
        } else {
            [wself showJoinFailedAlert];
        }
        if (block) {
            block(model.result);
        }
        [[ToastComponent shareToastComponent] dismiss];
    }];
}

- (void)loadDataWithMakeCoHost {
    // 向业务服务器发起上麦请求
    // Initiate a make cohost request to the business server
    __weak __typeof(self) wself = self;
    [[ToastComponent shareToastComponent] showLoading];
    [VoiceRTMManager raiseHandsMicWithBlock:^(RTMACKModel * _Nonnull model) {
        if (model.result) {
            [wself.bottomView updateButtonStatus:VoiceRoomBottomStatusRaiseHand close:YES];
            [wself checkMicrophoneSystemAuthority];
        } else {
            [[ToastComponent shareToastComponent] showWithMessage:@"操作失败，请重试"];
        }
        [[ToastComponent shareToastComponent] dismiss];
    }];
}

- (void)loadDataWithLostCoHost {
    // 向业务服务器发起下麦请求
    // Initiate a lost cohost request to the business server
    __weak __typeof(self) wself = self;
    [[ToastComponent shareToastComponent] showLoading];
    [VoiceRTMManager offSelfMicWithBlock:^(RTMACKModel * _Nonnull model) {
        if (model.result) {
            [wself updateLostCohostStatus];
        } else {
            [[ToastComponent shareToastComponent] showWithMessage:@"操作失败，请重试"];
        }
        [[ToastComponent shareToastComponent] dismiss];
    }];
}

- (void)voiceReconnect {
    __weak __typeof(self) wself = self;
    [VoiceRTMManager reconnectWithBlock:^(VoiceControlRoomModel * _Nonnull roomModel, NSArray * _Nonnull users, RTMACKModel * _Nonnull ackModel) {
        NSString *type = @"";
        if (ackModel.result) {
            type = @"resume";
        } else if (ackModel.code == RTMStatusCodeUserIsInactive ||
                   ackModel.code == RTMStatusCodeRoomDisbanded ||
                   ackModel.code == RTMStatusCodeUserNotFound) {
            type = @"exit";
        } else {
            
        }
        if (NOEmptyStr(type)) {
            NSMutableDictionary *dic = [[NSMutableDictionary alloc] init];
            [dic setValue:type forKey:@"type"];
            [dic setValue:roomModel forKey:@"roomModel"];
            [dic setValue:users forKey:@"users"];
            [wself voiceControlChange:dic];
        }
    }];
}

#pragma mark - VoiceRoomBottomViewDelegate

- (void)voiceRoomBottomView:(VoiceRoomBottomView *_Nonnull)voiceRoomBottomView itemButton:(VoiceRoomItemButton *_Nullable)itemButton didSelectStatus:(VoiceRoomBottomStatus)status {
    if (status == VoiceRoomBottomStatusList ||
        status == VoiceRoomBottomStatusListRed) {
        __weak __typeof(self) wself = self;
        [self.userListComponent show:^{
            [wself restoreBottomViewMenuStatus];
        }];
    } else if (status == VoiceRoomBottomStatusRaiseHand) {
        [self loadDataWithMakeCoHost];
    } else if (status == VoiceRoomBottomStatusMic) {
        [SystemAuthority authorizationStatusWithType:AuthorizationTypeAudio block:^(BOOL isAuthorize) {
            if (itemButton.status == ButtonStatusNone) {
                [VoiceRTMManager muteMic];
                [[VoiceRTCManager shareRtc] muteLocalAudioStream:YES];
            } else {
                [VoiceRTMManager unmuteMic];
                [[VoiceRTCManager shareRtc] muteLocalAudioStream:NO];
            }
            itemButton.status = itemButton.status == ButtonStatusNone ? ButtonStatusActive : ButtonStatusNone;
        }];
    } else if (status == VoiceRoomBottomStatusData) {
        [self.paramComponent show];
    } else if (status == VoiceRoomBottomStatusDownHand) {
        AlertActionModel *alertModel = [[AlertActionModel alloc] init];
        alertModel.title = @"确定";
        AlertActionModel *cancelModel = [[AlertActionModel alloc] init];
        cancelModel.title = @"取消";
        __weak __typeof(self) wself = self;
        [[AlertActionManager shareAlertActionManager] showWithMessage:@"是否确认下麦？" actions:@[cancelModel, alertModel]];
        alertModel.alertModelClickBlock = ^(UIAlertAction * _Nonnull action) {
            if ([action.title isEqualToString:@"确定"]) {
                [wself loadDataWithLostCoHost];
            }
        };
    } else {
        
    }
}

#pragma mark - VoiceRoomNavViewDelegate

- (void)voiceRoomNavView:(VoiceRoomNavView *)voiceRoomNavView
         didSelectStatus:(RoomNavStatus)status {
    if (status == RoomNavStatusHangeup) {
        [self showEndView];
    }
}

#pragma mark - VoiceRTCManagerDelegate

- (void)voiceRTCManager:(VoiceRTCManager *)voiceRTCManager changeParamInfo:(VoiceRoomParamInfoModel *)model {
    [self.paramComponent updateModel:model];
}

- (void)voiceRTCManager:(VoiceRTCManager *_Nonnull)voiceRTCManager reportAllAudioVolume:(NSDictionary<NSString *, NSNumber *> *_Nonnull)volumeInfo {
    [self.roomView updateHostVolume:volumeInfo];
}

#pragma mark - Private Action

- (void)joinRTCRoom {
    if (NOEmptyStr(self.roomModel.room_id)) {
        [VoiceRTCManager shareRtc].delegate = self;
        [[VoiceRTCManager shareRtc] joinRTCRoomWithToken:self.token
                                                  roomID:self.roomModel.room_id
                                                     uid:[LocalUserComponent userModel].uid];
        __weak __typeof(self)wself = self;
        [VoiceRTCManager shareRtc].rtcJoinRoomBlock = ^(NSString *roomId, NSInteger errorCode, NSInteger joinType) {
            if (joinType == 0) {
                // 首次加入房间
                // join the room for the first time
                if (errorCode == 0) {
                    [wself updateRoomViewWithData];
                    [wself addIMMessage:YES
                              userModel:(VoiceControlUserModel *)[LocalUserComponent userModel]];
                } else {
                    [wself showJoinFailedAlert];
                }
            } else {
                // 断线重新加入房间
                // disconnect and rejoin the room
                [wself voiceReconnect];
            }
        };
    } else {
        [self showJoinFailedAlert];
    }
}

- (void)updateRoomViewWithData {
    [self.roomView updateAllUser:self.userLists roomModel:self.roomModel];
    self.navView.roomModel = self.roomModel;
    [self.bottomView updateBottomLists:[self getBottomListsWithModel:[self currentLoginuserModel]]];
    VoiceControlUserModel *loginUserModel = [self currentLoginuserModel];
    if (loginUserModel.is_host || loginUserModel.user_status == 2) {
        [[VoiceRTCManager shareRtc] makeCoHost:YES];
    } else {
        [[VoiceRTCManager shareRtc] makeCoHost:NO];
    }
    [[VoiceRTCManager shareRtc] muteLocalAudioStream:!loginUserModel.is_mic_on];
    [self.bottomView updateButtonStatus:VoiceRoomBottomStatusMic close:!loginUserModel.is_mic_on isTitle:NO];
    [self checkMicrophoneSystemAuthority];
}

- (void)checkMicrophoneSystemAuthority {
    [SystemAuthority authorizationStatusWithType:AuthorizationTypeAudio block:^(BOOL isAuthorize) {
        if (!isAuthorize) {
            AlertActionModel *alertModel = [[AlertActionModel alloc] init];
            alertModel.title = @"确定";
            [[AlertActionManager shareAlertActionManager] showWithMessage:@"麦克风权限已关闭，请至设备设置页开启。" actions:@[alertModel]];
        }
    }];
}

- (void)updateLostCohostStatus {
    [self.roomView updateUserHand:[LocalUserComponent userModel].uid isHand:NO];
    [self.roomView reloadData];
    [self.bottomView replaceButtonStatus:VoiceRoomBottomStatusDownHand newStatus:VoiceRoomBottomStatusRaiseHand];
}

- (void)restoreBottomViewMenuStatus {
    [self.bottomView replaceButtonStatus:VoiceRoomBottomStatusListRed newStatus:VoiceRoomBottomStatusList];
}

- (void)addSubviewAndConstraints {
    [self.view addSubview:self.roomView];
    [self.view addSubview:self.bottomView];
    [self.view addSubview:self.navView];
    
    [self.navView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.height.mas_equalTo([DeviceInforTool getStatusBarHight] + 44);
        make.width.equalTo(self.view);
        make.top.left.equalTo(self.view);
    }];
    
    [self.bottomView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.height.mas_equalTo([DeviceInforTool getVirtualHomeHeight] + 64);
        make.left.width.equalTo(self.view);
        make.bottom.equalTo(self.view);
    }];
    
    [self.roomView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.right.equalTo(self.view);
        make.top.equalTo(self.navView.mas_bottom);
        make.bottom.equalTo(self.bottomView.mas_top);
    }];
}

- (void)showEndView {
    VoiceEndStatus status = VoiceEndStatusAudience;
    if ([self currentLoginuserModel].is_host) {
        if ([self.roomView hostNumber] <= 1) {
            status = VoiceEndStatusHostOnly;
        } else {
            status = VoiceEndStatusHost;
        }
    }
    [self.endComponent showWithStatus:status];
    __weak __typeof(self) wself = self;
    self.endComponent.clickButtonBlock = ^(VoiceButtonStatus status) {
        if (status == VoiceButtonStatusEnd ||
            status == VoiceButtonStatusLeave) {
            [wself hangUp];
        } else if (status == VoiceButtonStatusCancel) {
            //cancel
        }
        wself.endComponent = nil;
    };
}

- (void)navigationControllerPop {
    UIViewController *jumpVC = nil;
    for (UIViewController *vc in self.navigationController.viewControllers) {
        if ([NSStringFromClass([vc class]) isEqualToString:@"VoiceRoomListsViewController"]) {
            jumpVC = vc;
            break;
        }
    }
    if (jumpVC) {
        [self.navigationController popToViewController:jumpVC animated:YES];
    } else {
        [self.navigationController popViewControllerAnimated:YES];
    }
}

- (VoiceControlUserModel *)currentLoginuserModel {
    VoiceControlUserModel *currentModel = nil;
    for (VoiceControlUserModel *userModel in [self.roomView allUserLists]) {
        if ([userModel.uid isEqualToString:[LocalUserComponent userModel].uid]) {
            currentModel = userModel;
            break;
        }
    }
    return currentModel;
}

- (NSArray *)getBottomListsWithModel:(VoiceControlUserModel *)userModel {
    NSArray *bottomLists = nil;
    if (userModel.is_host) {
        bottomLists = @[@(VoiceRoomBottomStatusList),
                        @(VoiceRoomBottomStatusMic),
                        @(VoiceRoomBottomStatusData)];
    } else {
        if (userModel.user_status == 2) {
            bottomLists = @[@(VoiceRoomBottomStatusDownHand),
                            @(VoiceRoomBottomStatusMic),
                            @(VoiceRoomBottomStatusData)];
        } else {
            bottomLists = @[@(VoiceRoomBottomStatusRaiseHand),
                            @(VoiceRoomBottomStatusData)];
        }
    }
    return bottomLists;
}

- (void)addBgGradientLayer {
    UIColor *startColor = [UIColor colorFromHexString:@"#30394A"];
    UIColor *endColor = [UIColor colorFromHexString:@"#1D2129"];
    CAGradientLayer *gradientLayer = [CAGradientLayer layer];
    gradientLayer.frame = self.view.bounds;
    gradientLayer.colors = @[(__bridge id)[startColor colorWithAlphaComponent:1.0].CGColor,
                             (__bridge id)[endColor colorWithAlphaComponent:1.0].CGColor];
    gradientLayer.startPoint = CGPointMake(.0, .0);
    gradientLayer.endPoint = CGPointMake(.0, 1.0);
    [self.view.layer addSublayer:gradientLayer];
}

- (void)showJoinFailedAlert {
    AlertActionModel *alertModel = [[AlertActionModel alloc] init];
    alertModel.title = @"确定";
    __weak __typeof(self)wself = self;
    alertModel.alertModelClickBlock = ^(UIAlertAction * _Nonnull action) {
        if ([action.title isEqualToString:@"确定"]) {
            [wself hangUp];
        }
    };
    [[AlertActionManager shareAlertActionManager] showWithMessage:@"加入房间失败，回到房间列表页" actions:@[alertModel]];
}

- (void)addIMMessage:(BOOL)isJoin
           userModel:(VoiceControlUserModel *)userModel {
    NSString *unitStr = isJoin ? @"加入了房间" : @"离开房间";
    BaseIMModel *imModel = [[BaseIMModel alloc] init];
    imModel.message = [NSString stringWithFormat:@"%@ %@", userModel.name, unitStr];
    [self.imComponent addIM:imModel];
}

- (void)hangUp {
    __weak __typeof(self) wself = self;
    [VoiceRTMManager leaveVoice:^(RTMACKModel * _Nonnull model) {
        [wself navigationControllerPop];
    }];
    [[VoiceRTCManager shareRtc] leaveRTCRoom];
}

#pragma mark - Getter

- (VoiceRoomView *)roomView {
    if (!_roomView) {
        _roomView = [[VoiceRoomView alloc] init];
    }
    return _roomView;
}

- (VoiceRoomBottomView *)bottomView {
    if (!_bottomView) {
        _bottomView = [[VoiceRoomBottomView alloc] init];
        _bottomView.delegate = self;
    }
    return _bottomView;
}

- (VoiceRoomNavView *)navView {
    if (!_navView) {
        _navView = [[VoiceRoomNavView alloc] init];
        _navView.delegate = self;
    }
    return _navView;
}

- (VoiceRoomUserListComponent *)userListComponent {
    if (!_userListComponent) {
        _userListComponent = [[VoiceRoomUserListComponent alloc] init];
    }
    return _userListComponent;
}

- (VoiceEndComponent *)endComponent {
    if (!_endComponent) {
        _endComponent = [[VoiceEndComponent alloc] init];
    }
    return _endComponent;
}

- (BaseIMComponent *)imComponent {
    if (!_imComponent) {
        _imComponent = [[BaseIMComponent alloc] initWithSuperView:self.view];
    }
    return _imComponent;
}

- (VoiceRoomParamComponent *)paramComponent {
    if (!_paramComponent) {
        _paramComponent = [[VoiceRoomParamComponent alloc] init];
    }
    return _paramComponent;
}

@end
