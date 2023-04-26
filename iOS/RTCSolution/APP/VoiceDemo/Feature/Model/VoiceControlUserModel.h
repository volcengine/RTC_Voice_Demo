// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface VoiceControlUserModel : BaseUserModel

@property (nonatomic, assign) BOOL is_host;

/*
 * user_status
 * 观众：user_status = 0
 * 举手：user_status = 1
 * 上麦：user_status = 2
 */
@property (nonatomic, assign) NSInteger user_status;

@property (nonatomic, assign) BOOL is_mic_on;

@property (nonatomic, assign) NSInteger created_at;

@property (nonatomic, assign) NSInteger volume;

@end

NS_ASSUME_NONNULL_END
