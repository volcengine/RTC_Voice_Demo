// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import <UIKit/UIKit.h>
@class VoiceRoomTopSelectView;

NS_ASSUME_NONNULL_BEGIN

@protocol VoiceRoomTopSelectViewDelegate <NSObject>

- (void)voiceRoomTopSelectView:(VoiceRoomTopSelectView *)voiceRoomTopSelectView clickCancelAction:(id)model;

- (void)voiceRoomTopSelectView:(VoiceRoomTopSelectView *)voiceRoomTopSelectView clickSwitchItem:(BOOL)isAudience;

@end

@interface VoiceRoomTopSelectView : UIView

@property (nonatomic, weak) id<VoiceRoomTopSelectViewDelegate> delegate;

@property (nonatomic, copy) NSString *titleStr;

@end

NS_ASSUME_NONNULL_END
