// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import <UIKit/UIKit.h>
#import "VoiceControlUserModel.h"

NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSInteger, AvatarCellStatus) {
    AvatarCellStatusMic,
    AvatarCellStatusAudience,
};

@interface VoiceUserAvatarCell : UICollectionViewCell

@property (nonatomic, strong) VoiceControlUserModel *model;

@property (nonatomic, assign) AvatarCellStatus status;

@end

NS_ASSUME_NONNULL_END
