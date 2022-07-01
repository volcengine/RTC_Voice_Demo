//
//  VoiceDemo.m
//  AFNetworking
//
//  Created by bytedance on 2022/4/21.
//

#import "VoiceDemo.h"
#import "VoiceRoomListsViewController.h"
#import <Core/NetworkReachabilityManager.h>

@implementation VoiceDemo

- (void)pushDemoViewControllerBlock:(void (^)(BOOL result))block {
    [super pushDemoViewControllerBlock:block];
    [VoiceRTCManager shareRtc].networkDelegate = [NetworkReachabilityManager sharedManager];
    [[VoiceRTCManager shareRtc] connect:@"cs"
                             loginToken:[LocalUserComponents userModel].loginToken
                                  block:^(BOOL result) {
        if (result) {
            VoiceRoomListsViewController *next = [[VoiceRoomListsViewController alloc] init];
            UIViewController *topVC = [DeviceInforTool topViewController];
            [topVC.navigationController pushViewController:next animated:YES];
        } else {
            [[ToastComponents shareToastComponents] showWithMessage:@"连接失败"];
        }
        if (block) {
            block(result);
        }
    }];
}

@end
