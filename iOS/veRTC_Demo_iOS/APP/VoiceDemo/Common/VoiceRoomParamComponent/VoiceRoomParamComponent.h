//
//  VoiceRoomParamComponent.h
//  veRTC_Demo
//
//  Created by on 2021/5/24.
//  
//

#import <Foundation/Foundation.h>
#import "VoiceRoomParamInfoModel.h"

NS_ASSUME_NONNULL_BEGIN

@interface VoiceRoomParamComponent : NSObject

- (void)show;

- (void)updateModel:(VoiceRoomParamInfoModel *)model;

@end

NS_ASSUME_NONNULL_END
