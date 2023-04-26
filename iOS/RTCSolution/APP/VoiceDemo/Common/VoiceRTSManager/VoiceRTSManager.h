// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import <Foundation/Foundation.h>
#import "VoiceControlUserModel.h"
#import "VoiceControlRoomModel.h"

#import "RTSACKModel.h"
#import "BaseUserModel.h"

NS_ASSUME_NONNULL_BEGIN

@interface VoiceRTSManager : NSObject

#pragma mark - Get Voice data

//csGetMeetings
+ (void)getMeetingsWithBlock:(void (^ __nullable)(NSArray *lists,
                                                  RTSACKModel *model))block;

+ (void)createMeeting:(NSString *)roomName
             userName:(NSString *)userName
                block:(void (^ __nullable)(NSString *token,
                                           VoiceControlRoomModel *roomModel,
                                           NSArray<VoiceControlUserModel *> *lists,
                                           RTSACKModel *model))block;

/*
 * Join the Voice
 * @param loginModel Login user data
 * @param block Callback
 */
+ (void)joinVoice:(NSString *)userName
            block:(void (^)(NSString *token,
                            VoiceControlRoomModel *roomModel,
                            NSArray<VoiceControlUserModel *> *lists,
                            RTSACKModel *model))block;

/*
 * Leave Voice
 */
+ (void)leaveVoice:(void (^ __nullable)(RTSACKModel *model))block;

/*
 * Get the participant list/participant status
 * @param userId User ID
 * @param block Callback
 */
+ (void)getRaiseHandsWithBlock:(void (^ __nullable)(NSArray<VoiceControlUserModel *> *userLists, RTSACKModel *model))block;


+ (void)getAudiencesWithBlock:(void (^ __nullable)(NSArray<VoiceControlUserModel *> *userLists, RTSACKModel *model))block;

+ (void)reconnectWithBlock:(void (^)(VoiceControlRoomModel *, NSArray *users, RTSACKModel * _Nonnull))block;

#pragma mark - Control Voice status

/*
 * Invite Mic
 * @param userId Host ID to be handed over
 * @param block Callback
 */
+ (void)inviteMic:(NSString *)userId block:(void (^ __nullable)(RTSACKModel *model))block;

/*
 * Confirmation
 * @param userId Users who want to mute, do not pass means to mute all users
 * @param block Callback
 */
+ (void)confirmMicWithBlock:(void (^ __nullable)(RTSACKModel *model))block;

/*
 * Raise your hand in wheat
 * @param userId ID of the user who requested to turn on the microphone
 * @param block Callback
 */
+ (void)raiseHandsMicWithBlock:(void (^ __nullable)(RTSACKModel *model))block;

/*
 * Agree to serve
 * @param userId ID of the user who requested to turn on the microphone
 * @param block Callback
 */
+ (void)agreeMic:(NSString *)userId block:(void (^ __nullable)(RTSACKModel *model))block;

// Download (user)
+ (void)offSelfMicWithBlock:(void (^ __nullable)(RTSACKModel *model))block;

// Switch to normal user (host)
+ (void)offMic:(NSString *)userId block:(void (^ __nullable)(RTSACKModel *model))block;

/*
 * Turn On Mic
 */
+ (void)muteMic;

/*
 * Turn Off Mic
 */
+ (void)unmuteMic;


#pragma mark - Notification message

/*
 * User join Notification
 * @param block Callback
 */
+ (void)onJoinMeetingWithBlock:(void (^)(VoiceControlUserModel *userModel))block;

/*
 * User leave Notification
 * @param block Callback
 */
+ (void)onLeaveMeetingWithBlock:(void (^)(VoiceControlUserModel *userModel))block;

/*
 * User raises hand Notification
 * @param block Callback
 */
+ (void)onRaiseHandsMicWithBlock:(void (^)(NSString *uid))block;

/*
 * Audience receives the invitation to the microphone Notification
 * @param block Callback
 */
+ (void)onInviteMicWithBlock:(void (^)(NSString *uid))block;

/*
 * Successful user registration Notification
 * @param block Callback
 */
+ (void)onMicOnWithBlock:(void (^)(VoiceControlUserModel *userModel))block;

/*
 * Successful user download Notification
 * @param block Callback
 */
+ (void)onMicOffWithBlock:(void (^)(NSString *uid))block;

/*
 * User silent notification Notification
 * @param block Callback
 */
+ (void)onMuteMicWithBlock:(void (^)(NSString *uid))block;

/*
 * User unmute notification Notification
 * @param block Callback
 */
+ (void)onUnmuteMic:(void (^)(NSString * _Nonnull uid))block;

/*
 * Is over Notification
 * @param block Callback
 */
+ (void)onMeetingEnd:(void (^)(BOOL result))block;

/*
 * Handover host Notification
 * @param block Callback
 */
+ (void)onHostChange:(void (^)(NSString *formerHostID, VoiceControlUserModel *hostUser))block;

@end

NS_ASSUME_NONNULL_END
