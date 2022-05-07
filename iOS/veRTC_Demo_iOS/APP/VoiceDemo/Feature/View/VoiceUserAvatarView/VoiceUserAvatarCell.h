//
//  VoiceUserAvatarCell.h
//  veRTC_Demo
//
//  Created by bytedance on 2021/5/19.
//  Copyright © 2021 . All rights reserved.
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
