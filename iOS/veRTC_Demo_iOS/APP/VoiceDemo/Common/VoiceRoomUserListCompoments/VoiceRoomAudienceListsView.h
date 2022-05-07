//
//  VoiceRoomUserListView.h
//  veRTC_Demo
//
//  Created by bytedance on 2021/5/18.
//  Copyright © 2021 . All rights reserved.
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
