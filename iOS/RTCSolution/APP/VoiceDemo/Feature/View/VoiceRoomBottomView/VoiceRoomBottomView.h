// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import <UIKit/UIKit.h>
#import "VoiceRoomItemButton.h"
@class VoiceRoomBottomView;

typedef NS_ENUM(NSInteger, VoiceRoomBottomStatus) {
    VoiceRoomBottomStatusList = 0,
    VoiceRoomBottomStatusMic,
    VoiceRoomBottomStatusData,
    VoiceRoomBottomStatusRaiseHand,
    VoiceRoomBottomStatusListRed,
    VoiceRoomBottomStatusDownHand,
};

@protocol VoiceRoomBottomViewDelegate <NSObject>

- (void)voiceRoomBottomView:(VoiceRoomBottomView *_Nonnull)voiceRoomBottomView itemButton:(VoiceRoomItemButton *_Nullable)itemButton didSelectStatus:(VoiceRoomBottomStatus)status;

@end

NS_ASSUME_NONNULL_BEGIN

@interface VoiceRoomBottomView : UIView

@property (nonatomic, weak) id<VoiceRoomBottomViewDelegate> delegate;

- (void)updateButtonStatus:(VoiceRoomBottomStatus)status close:(BOOL)isClose;

- (void)updateButtonStatus:(VoiceRoomBottomStatus)status close:(BOOL)isClose isTitle:(BOOL)isTitle;

- (void)replaceButtonStatus:(VoiceRoomBottomStatus)status newStatus:(VoiceRoomBottomStatus)newStatus;

- (ButtonStatus)getButtonStatus:(VoiceRoomBottomStatus)status;

- (void)updateBottomLists:(NSArray<NSNumber *> *)status;

@end

NS_ASSUME_NONNULL_END
