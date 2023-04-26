// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import <Foundation/Foundation.h>
#import "VoiceRoomAudienceListsView.h"
#import "VoiceRoomRaiseHandListsView.h"

NS_ASSUME_NONNULL_BEGIN

@interface VoiceRoomUserListComponent : NSObject

- (void)show:(void (^)(void))dismissBlock;

- (void)update;

@end

NS_ASSUME_NONNULL_END
