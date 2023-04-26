// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import <UIKit/UIKit.h>
#import "VoiceRoomUserListtCell.h"
@class VoiceRoomAudienceListsView;

NS_ASSUME_NONNULL_BEGIN

@protocol VoiceRoomAudienceListsViewDelegate <NSObject>

- (void)voiceRoomAudienceListsView:(VoiceRoomAudienceListsView *)voiceRoomAudienceListsView clickButton:(VoiceControlUserModel *)model;

@end


@interface VoiceRoomAudienceListsView : UIView

@property (nonatomic, copy) NSArray<VoiceControlUserModel *> *dataLists;

@property (nonatomic, weak) id<VoiceRoomAudienceListsViewDelegate> delegate;

@end

NS_ASSUME_NONNULL_END
