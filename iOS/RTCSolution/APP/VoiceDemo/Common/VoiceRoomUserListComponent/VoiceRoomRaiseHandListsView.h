// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import <UIKit/UIKit.h>
#import "VoiceRoomUserListtCell.h"
@class VoiceRoomRaiseHandListsView;

NS_ASSUME_NONNULL_BEGIN

@protocol VoiceRoomRaiseHandListsViewDelegate <NSObject>

- (void)voiceRoomRaiseHandListsView:(VoiceRoomRaiseHandListsView *)voiceRoomRaiseHandListsView clickButton:(VoiceControlUserModel *)model;

@end

@interface VoiceRoomRaiseHandListsView : UIView

@property (nonatomic, copy) NSArray<VoiceControlUserModel *> *dataLists;

@property (nonatomic, weak) id<VoiceRoomRaiseHandListsViewDelegate> delegate;

@end

NS_ASSUME_NONNULL_END
