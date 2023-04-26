// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface VoiceRoomParamInfoModel : NSObject

@property (nonatomic, strong) NSString *numChannels;
@property (nonatomic, strong) NSString *sentSampleRate;
@property (nonatomic, strong) NSString *sentKBitrate;
@property (nonatomic, strong) NSString *audioLossRate;

@property (nonatomic, strong) NSString *recordKBitrate;
@property (nonatomic, strong) NSString *recordLossRate;
@property (nonatomic, strong) NSString *rtt;

@end

NS_ASSUME_NONNULL_END
