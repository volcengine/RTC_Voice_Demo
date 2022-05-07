//
//  VoiceRoomNavView.h
//  quickstart
//
//  Created by bytedance on 2021/3/23.
//  Copyright © 2021 . All rights reserved.
//

#import <UIKit/UIKit.h>
#import "VoiceRTMManager.h"
@class VoiceRoomNavView;

typedef NS_ENUM(NSInteger, RoomNavStatus) {
    RoomNavStatusHangeup
};

NS_ASSUME_NONNULL_BEGIN

@protocol VoiceRoomNavViewDelegate <NSObject>

- (void)voiceRoomNavView:(VoiceRoomNavView *)voiceRoomNavView didSelectStatus:(RoomNavStatus)status;

@end

@interface VoiceRoomNavView : UIView

@property (nonatomic, strong) VoiceControlRoomModel *roomModel;

@property (nonatomic, weak) id<VoiceRoomNavViewDelegate> delegate;

@property (nonatomic, assign) NSInteger meetingTime;

@end

NS_ASSUME_NONNULL_END
