// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import <UIKit/UIKit.h>
#import "VoiceControlUserModel.h"
@class VoiceRoomUserListtCell;

NS_ASSUME_NONNULL_BEGIN

@protocol VoiceRoomUserListtCellDelegate <NSObject>

- (void)voiceRoomUserListtCell:(VoiceRoomUserListtCell *)voiceRoomUserListtCell clickButton:(id)model;

@end

@interface VoiceRoomUserListtCell : UITableViewCell

@property (nonatomic, strong) VoiceControlUserModel *model;

@property (nonatomic, weak) id<VoiceRoomUserListtCellDelegate> delegate;

@end

NS_ASSUME_NONNULL_END
