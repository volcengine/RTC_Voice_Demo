//
//  VoiceRoomViewController.m
//  veRTC_Demo
//
//  Created by bytedance on 2021/5/18.
//  Copyright © 2021 . All rights reserved.
//

#import "VoiceRoomViewController.h"
#import "VoiceRoomView.h"
#import "VoiceRoomBottomView.h"
#import "VoiceRoomNavView.h"
#import "VoiceRoomUserListCompoments.h"
#import "VoiceEndCompoments.h"
#import "VoiceIMCompoments.h"
#import "VoiceRoomParamCompoments.h"
#import "VoiceRoomViewController+SocketControl.h"
#import "SystemAuthority.h"
#import "VoiceRTMManager.h"
#import "NetworkingTool.h"

@interface VoiceRoomViewController () <VoiceRoomNavViewDelegate, VoiceRoomBottomViewDelegate, VoiceRTCManagerDelegate>

@property (nonatomic, strong) VoiceRoomView *roomView;
@property (nonatomic, strong) VoiceRoomBottomView *bottomView;
@property (nonatomic, strong) VoiceRoomNavView *navView;
@property (nonatomic, strong) VoiceRoomParamCompoments *paramCompoments;
@property (nonatomic, strong) VoiceRoomUserListCompoments *userListCompoments;
@property (nonatomic, strong) VoiceEndCompoments *endCompoments;
@property (nonatomic, strong) VoiceIMCompoments *imCompoments;

@end

@implementation VoiceRoomViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    self.view.backgroundColor = [UIColor clearColor];
    
    [self addSocketListener];
    [self addBgGradientLayer];
    [self addSubviewAndConstraints];
    
    [self loadDataWithRoomInfo];
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    [self.navigationController setNavigationBarHidden:YES animated:NO];
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    
    self.navigationController.interactivePopGestureRecognizer.enabled = NO;
}

#pragma mark - Notification

- (void)voiceControlChange:(NSDictionary *)dic {
    if ([dic isKindOfClass:[NSDictionary class]]) {
        NSString *type = dic[@"type"];
        if ([type isEqualToString:@"resume"]) {
            self.userLists = dic[@"users"];
            self.roomModel = (VoiceControlRoomModel *)(dic[@"roomModel"]);
            [self updateRoomViewWithData];
        } else if ([type isEqualToString:@"exit"]) {
            [self hangUp];
            [[ToastComponents shareToastComponents] showWithMessage:@"房间已解散" delay:0.8];
        } else {
            
        }
    }
}

#pragma mark - Publish Action

- (void)addUser:(VoiceControlUserModel *)userModel {
    [self.roomView joinUser:userModel];
    [self.userListCompoments update];
    
    VoiceIMModel *imModel = [[VoiceIMModel alloc] init];
    imModel.isJoin = YES;
    imModel.userModel = userModel;
    [self.imCompoments addIM:imModel];
}

- (void)removeUser:(VoiceControlUserModel *)userModel {
    [self.roomView leaveUser:userModel.user_id];
    [self.userListCompoments update];
    
    VoiceIMModel *imModel = [[VoiceIMModel alloc] init];
    imModel.isJoin = NO;
    imModel.userModel = userModel;
    [self.imCompoments addIM:imModel];
}

- (void)receivedRaiseHandWithUser:(NSString *)uid {
    if ([self currentLoginuserModel].is_host) {
        [self.bottomView replaceButtonStatus:VoiceRoomBottomStatusList newStatus:VoiceRoomBottomStatusListRed];
        [self.userListCompoments update];
    }
    [self.roomView updateUserHand:uid isHand:YES];
    [self.roomView reloadData];
}

- (void)receivedRaiseHandInviteWithAudience {
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
    [self.roomView audienceRaisedHandsSuccess:userModel];
    [self.userListCompoments update];
    if ([userModel.user_id isEqualToString:[LocalUserComponents userModel].uid]) {
        VoiceControlUserModel *localUser = [self currentLoginuserModel];
        localUser.user_status = 2;
        localUser.is_mic_on = YES;
        [self.bottomView updateBottomLists:[self getBottomListsWithModel:localUser]];
        [[ToastComponents shareToastComponents] showWithMessage:@"您已成功上麦"];
        [[VoiceRTCManager shareRtc] makeCoHost:YES];
        [[VoiceRTCManager shareRtc] muteLocalAudioStream:NO];
        [self checkMicrophoneSystemAuthority];
    }
}

- (void)receivedLowerHandSucceedWithUser:(NSString *)uid {
    [self.roomView hostLowerHandSuccess:uid];
    [self.userListCompoments update];
    if ([uid isEqualToString:[LocalUserComponents userModel].uid]) {
        VoiceControlUserModel *localUser = [self currentLoginuserModel];
        localUser.user_status = 0;
        localUser.is_mic_on = NO;
        [self.bottomView updateBottomLists:[self getBottomListsWithModel:localUser]];
        [[ToastComponents shareToastComponents] showWithMessage:@"您已回到听众席"];
        [[VoiceRTCManager shareRtc] makeCoHost:NO];
    }
}

- (void)receivedMicChangeWithMute:(BOOL)isMute uid:(NSString *)uid {
    [self.roomView updateUserMic:uid isMute:isMute];
}

- (void)receivedHostChangeWithNewHostUid:(VoiceControlUserModel *)hostUser {
    [self.roomView updateHostUser:hostUser.user_id];
    if ([[LocalUserComponents userModel].uid isEqualToString:hostUser.user_id]) {
        // 如果当前登录用户为新主持人
        // If the currently logged-in user is the new host
        [self.bottomView updateBottomLists:[self getBottomListsWithModel:[self currentLoginuserModel]]];
        [self.bottomView updateButtonStatus:VoiceRoomBottomStatusMic close:!hostUser.is_mic_on isTitle:NO];
        [[VoiceRTCManager shareRtc] muteLocalAudioStream:!hostUser.is_mic_on];
        [[ToastComponents shareToastComponents] showWithMessage:@"您已成为主播"];
    }
}

- (void)receivedMeetingEnd {
    [self hangUp];
    VoiceControlUserModel *loginUserModel = [self currentLoginuserModel];
    if (!loginUserModel.is_host) {
        [[ToastComponents shareToastComponents] showWithMessage:@"房间已解散" delay:0.8];
    }
}

- (void)hangUp {
    // rtm api
    __weak __typeof(self) wself = self;
    [VoiceRTMManager leaveVoice:^(RTMACKModel * _Nonnull model) {
        // ui
        [wself navigationControllerPop];
    }];
    [[VoiceRTCManager shareRtc] leaveRTCRoom];
}


#pragma mark - Load Data

- (void)loadDataWithRoomInfo {
    if (IsEmptyStr(self.roomModel.room_id)) {
        [self joinVocie];
    } else {
        [self joinChannel];
    }
}

- (void)joinVocie {
    __weak __typeof(self)wself = self;
    [VoiceRTMManager joinVoice:self.roomID
                             userName:self.userName
                                block:^(NSString * _Nonnull token,
                                        VoiceControlRoomModel * _Nonnull roomModel,
                                        NSArray<VoiceControlUserModel *> * _Nonnull lists,
                                        RTMACKModel * _Nonnull model) {
        if (model.result) {
            wself.userLists = lists;
            wself.token = token;
            wself.roomModel = roomModel;
            [wself joinChannel];
        } else {
            [wself showJoinFailedAlert];
        }
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
        [self.userListCompoments show:^{
            [wself restoreBottomViewMenuStatus];
        }];
    } else if (status == VoiceRoomBottomStatusRaiseHand) {
        __weak __typeof(self) wself = self;
        [VoiceRTMManager raiseHandsMicWithBlock:^(RTMACKModel * _Nonnull model) {
            if (model.result) {
                [wself.bottomView updateButtonStatus:VoiceRoomBottomStatusRaiseHand close:YES];
                [wself checkMicrophoneSystemAuthority];
            } else {
                [[ToastComponents shareToastComponents] showWithMessage:@"操作失败，请重试"];
            }
        }];
    } else if (status == VoiceRoomBottomStatusMic) {
        [SystemAuthority authorizationStatusWithType:AuthorizationTypeAudio block:^(BOOL isAuthorize) {
            if (itemButton.status == ButtonStatusNone) {
                //mute
                [VoiceRTMManager muteMic];
                [[VoiceRTCManager shareRtc] muteLocalAudioStream:YES];
            } else {
                //unmute
                [VoiceRTMManager unmuteMic];
                [[VoiceRTCManager shareRtc] muteLocalAudioStream:NO];
            }
            itemButton.status = itemButton.status == ButtonStatusNone ? ButtonStatusActive : ButtonStatusNone;
        }];
    } else if (status == VoiceRoomBottomStatusData) {
        [self.paramCompoments show];
    } else if (status == VoiceRoomBottomStatusDownHand) {
        AlertActionModel *alertModel = [[AlertActionModel alloc] init];
        alertModel.title = @"确定";
        AlertActionModel *cancelModel = [[AlertActionModel alloc] init];
        cancelModel.title = @"取消";
        __weak __typeof(self) wself = self;
        [[AlertActionManager shareAlertActionManager] showWithMessage:@"是否确认下麦？" actions:@[cancelModel, alertModel]];
        alertModel.alertModelClickBlock = ^(UIAlertAction * _Nonnull action) {
            if ([action.title isEqualToString:@"确定"]) {
                [VoiceRTMManager offSelfMicWithBlock:^(RTMACKModel * _Nonnull model) {
                    if (model.result) {
                        [wself sendDownloadHand];
                    } else {
                        [[ToastComponents shareToastComponents] showWithMessage:@"操作失败，请重试"];
                    }
                }];
            }
        };
    } else {
        
    }
}

#pragma mark - VoiceRoomNavViewDelegate

- (void)voiceRoomNavView:(VoiceRoomNavView *)voiceRoomNavView didSelectStatus:(RoomNavStatus)status {
    if (status == RoomNavStatusHangeup) {
        [self showEndView];
    }
}

#pragma mark - VoiceRTCManagerDelegate

- (void)voiceRTCManager:(VoiceRTCManager *)voiceRTCManager changeParamInfo:(VoiceRoomParamInfoModel *)model {
    [self.paramCompoments updateModel:model];
}

- (void)voiceRTCManager:(VoiceRTCManager *_Nonnull)voiceRTCManager reportAllAudioVolume:(NSDictionary<NSString *, NSNumber *> *_Nonnull)volumeInfo {
    [self.roomView updateHostVolume:volumeInfo];
}


#pragma mark - Private Action

- (void)joinChannel {
    if (NOEmptyStr(self.roomModel.room_id)) {
        //Activate SDK
        [VoiceRTCManager shareRtc].delegate = self;
        [[VoiceRTCManager shareRtc] joinChannelWithToken:self.token
                                                  roomID:self.roomModel.room_id
                                                     uid:[LocalUserComponents userModel].uid];
        __weak __typeof(self)wself = self;
        [VoiceRTCManager shareRtc].rtcJoinRoomBlock = ^(NSString *roomId, NSInteger errorCode, NSInteger joinType) {
            if (joinType == 0) {
                // 首次加入房间
                if (errorCode == 0) {
                    //Refresh the UI
                    [wself updateRoomViewWithData];
                } else {
                    [wself showJoinFailedAlert];
                }
            } else {
                // 断线重新加入房间
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

- (void)sendDownloadHand {
    NSString *uid = [LocalUserComponents userModel].uid;
    [self.roomView updateUserHand:uid isHand:NO];
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
    [self.endCompoments showWithStatus:status];
    __weak __typeof(self) wself = self;
    self.endCompoments.clickButtonBlock = ^(VoiceButtonStatus status) {
        if (status == VoiceButtonStatusEnd ||
            status == VoiceButtonStatusLeave) {
            [wself hangUp];
        } else if (status == VoiceButtonStatusCancel) {
            //cancel
        }
        wself.endCompoments = nil;
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
        if ([userModel.user_id isEqualToString:[LocalUserComponents userModel].uid]) {
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

- (VoiceRoomUserListCompoments *)userListCompoments {
    if (!_userListCompoments) {
        _userListCompoments = [[VoiceRoomUserListCompoments alloc] init];
    }
    return _userListCompoments;
}

- (VoiceEndCompoments *)endCompoments {
    if (!_endCompoments) {
        _endCompoments = [[VoiceEndCompoments alloc] init];
    }
    return _endCompoments;
}

- (VoiceIMCompoments *)imCompoments {
    if (!_imCompoments) {
        _imCompoments = [[VoiceIMCompoments alloc] initWithSuperView:self.view];
    }
    return _imCompoments;
}

- (VoiceRoomParamCompoments *)paramCompoments {
    if (!_paramCompoments) {
        _paramCompoments = [[VoiceRoomParamCompoments alloc] init];
    }
    return _paramCompoments;
}

@end
