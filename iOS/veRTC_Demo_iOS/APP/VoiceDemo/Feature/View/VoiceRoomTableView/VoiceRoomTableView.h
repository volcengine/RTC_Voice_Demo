//
//  VoiceRoomTableView.h
//  veRTC_Demo
//
//  Created by bytedance on 2021/5/18.
//  Copyright © 2021 . All rights reserved.
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
