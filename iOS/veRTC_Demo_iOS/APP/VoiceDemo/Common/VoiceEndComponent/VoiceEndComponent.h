//
//  VoiceEndComponent.h
//  veRTC_Demo
//
//  Created by on 2021/5/19.
//  
//

#import <Foundation/Foundation.h>
#import "VoiceRoomEndView.h"

NS_ASSUME_NONNULL_BEGIN

@interface VoiceEndComponent : NSObject

@property (nonatomic, copy) void (^clickButtonBlock)(VoiceButtonStatus status);

- (void)showWithStatus:(VoiceEndStatus)status;

@end

NS_ASSUME_NONNULL_END
