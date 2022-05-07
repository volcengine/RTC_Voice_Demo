//
//  VoiceRoomRaiseHandListsView.h
//  veRTC_Demo
//
//  Created by bytedance on 2021/5/19.
//  Copyright © 2021 . All rights reserved.
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
