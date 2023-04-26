// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import <Foundation/Foundation.h>
#import "VoiceRoomEndView.h"

NS_ASSUME_NONNULL_BEGIN

@interface VoiceEndComponent : NSObject

@property (nonatomic, copy) void (^clickButtonBlock)(VoiceButtonStatus status);

- (void)showWithStatus:(VoiceEndStatus)status;

@end

NS_ASSUME_NONNULL_END
