//
//  VoiceDemo.m
//  AFNetworking
//
//  Created by on 2022/4/21.
//

#import "VoiceDemo.h"
#import "JoinRTSParams.h"
#import "VoiceRoomListsViewController.h"
#import <Core/NetworkReachabilityManager.h>

@implementation VoiceDemo

- (void)pushDemoViewControllerBlock:(void (^)(BOOL result))block {
    [super pushDemoViewControllerBlock:block];

    JoinRTSInputModel *inputModel = [[JoinRTSInputModel alloc] init];
    inputModel.scenesName = @"cs";
    inputModel.loginToken = [LocalUserComponent userModel].loginToken;
    __weak __typeof(self) wself = self;
    [JoinRTSParams getJoinRTSParams:inputModel
                             block:^(JoinRTSParamsModel * _Nonnull model) {
        [wself joinRTS:model block:block];
    }];
}

- (void)joinRTS:(JoinRTSParamsModel * _Nonnull)model
          block:(void (^)(BOOL result))block{
    if (!model) {
        [[ToastComponent shareToastComponent] showWithMessage:@"连接失败"];
        if (block) {
            block(NO);
        }
        return;
    }
    // Connect RTS
    [[VoiceRTCManager shareRtc] connect:model.appId
                               RTSToken:model.RTSToken
                              serverUrl:model.serverUrl
                              serverSig:model.serverSignature
                                    bid:model.bid
                                  block:^(BOOL result) {
        if (result) {
            VoiceRoomListsViewController *next = [[VoiceRoomListsViewController alloc] init];
            UIViewController *topVC = [DeviceInforTool topViewController];
            [topVC.navigationController pushViewController:next animated:YES];
        } else {
            [[ToastComponent shareToastComponent] showWithMessage:@"连接失败"];
        }
        if (block) {
            block(result);
        }
    }];
}

@end
