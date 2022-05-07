//
//  VoiceRoomUserListCompoments.h
//  veRTC_Demo
//
//  Created by bytedance on 2021/5/19.
//  Copyright © 2021 . All rights reserved.
//

#import <Foundation/Foundation.h>
#import "VoiceRoomAudienceListsView.h"
#import "VoiceRoomRaiseHandListsView.h"

NS_ASSUME_NONNULL_BEGIN

@interface VoiceRoomUserListCompoments : NSObject

- (void)show:(void (^)(void))dismissBlock;

- (void)update;

@end

NS_ASSUME_NONNULL_END
