// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import "VoiceEndComponent.h"

@interface VoiceEndComponent ()

@property (nonatomic, strong) VoiceRoomEndView *endView;
@property (nonatomic, strong) UIButton *maskView;

@end

@implementation VoiceEndComponent


- (instancetype)init {
    self = [super init];
    if (self) {
        
    }
    return self;
}

#pragma mark - Publish Action

- (void)showWithStatus:(VoiceEndStatus)status {
    UIViewController *rootVC = [DeviceInforTool topViewController];;
    
    [rootVC.view addSubview:self.maskView];
    [self.maskView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.edges.equalTo(rootVC.view);
    }];

    [rootVC.view addSubview:self.endView];
    [self.endView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.width.mas_equalTo(590/2);
        make.center.equalTo(rootVC.view);
    }];

    self.endView.VoiceEndStatus = status;
    __weak __typeof(self) wself = self;
    self.endView.clickButtonBlock = ^(VoiceButtonStatus status) {
        [wself dismissEndView];
        if (wself.clickButtonBlock) {
            wself.clickButtonBlock(status);
        }
    };
}

#pragma mark - Private Action

- (void)dismissEndView {
    [self.endView removeFromSuperview];
    self.endView = nil;
    [self.maskView removeFromSuperview];
    self.maskView = nil;
}

#pragma mark - getter

- (VoiceRoomEndView *)endView {
    if (!_endView) {
        _endView = [[VoiceRoomEndView alloc] init];
        [_endView setBackgroundColor:[UIColor colorFromHexString:@"#272E3B"]];
        _endView.layer.masksToBounds = YES;
        _endView.layer.cornerRadius = 4;
    }
    return _endView;
}

- (UIButton *)maskView {
    if (!_maskView) {
        _maskView = [[UIButton alloc] init];
        [_maskView setBackgroundColor:[UIColor colorFromRGBHexString:@"#101319" andAlpha:0.7 * 255]];
    }
    return _maskView;
}

- (void)dealloc {
    NSLog(@"dealloc %@",NSStringFromClass([self class]));
}

@end
