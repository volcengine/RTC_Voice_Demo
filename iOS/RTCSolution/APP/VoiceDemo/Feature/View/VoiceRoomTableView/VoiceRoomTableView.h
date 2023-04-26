// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import <UIKit/UIKit.h>
#import "VoiceRoomCell.h"
@class VoiceRoomTableView;

NS_ASSUME_NONNULL_BEGIN

@protocol VoiceRoomTableViewDelegate <NSObject>

- (void)voiceRoomTableView:(VoiceRoomTableView *)voiceRoomTableView didSelectRowAtIndexPath:(VoiceControlRoomModel *)model;

@end

@interface VoiceRoomTableView : UIView

@property (nonatomic, copy) NSArray *dataLists;

@property (nonatomic, weak) id<VoiceRoomTableViewDelegate> delegate;


@end

NS_ASSUME_NONNULL_END
