//
//  VoiceRoomUserListComponent.h
//  veRTC_Demo
//
//  Created by on 2021/5/19.
//  
//

#import <Foundation/Foundation.h>
#import "VoiceRoomAudienceListsView.h"
#import "VoiceRoomRaiseHandListsView.h"

NS_ASSUME_NONNULL_BEGIN

@interface VoiceRoomUserListComponent : NSObject

- (void)show:(void (^)(void))dismissBlock;

- (void)update;

@end

NS_ASSUME_NONNULL_END
