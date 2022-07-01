//
//  VoiceRTMManager.m
//  SceneRTCDemo
//
//  Created by bytedance on 2021/3/16.
//

#import "VoiceRTMManager.h"

@implementation VoiceRTMManager

#pragma mark - Get Voice data

+ (void)getMeetingsWithBlock:(void (^)(NSArray *lists,
                                       RTMACKModel *model))block {
    NSDictionary *dic = [PublicParameterCompoments addTokenToParams:nil];
    [[VoiceRTCManager shareRtc] emitWithAck:@"csGetMeetings"
                                       with:dic
                                      block:^(RTMACKModel * _Nonnull ackModel) {
        NSMutableArray *modelLsts = [[NSMutableArray alloc] init];
        if ([ackModel.response isKindOfClass:[NSDictionary class]]) {
            NSArray *infos = ackModel.response[@"infos"];
            for (int i = 0; i < infos.count; i++) {
                VoiceControlRoomModel *roomMdoel = [VoiceControlRoomModel yy_modelWithJSON:infos[i]];
                [modelLsts addObject:roomMdoel];
            }
        }
        if (block) {
            block([modelLsts copy], ackModel);
        }
    }];
}

+ (void)createMeeting:(NSString *)roomName
             userName:(NSString *)userName
                block:(void (^)(NSString *token,
                                VoiceControlRoomModel *roomModel,
                                NSArray<VoiceControlUserModel *> *lists,
                                RTMACKModel *model))block {
    NSString *encodedString = [roomName stringByAddingPercentEncodingWithAllowedCharacters:[NSCharacterSet URLUserAllowedCharacterSet]];
    NSDictionary *dic = @{@"room_name" : encodedString,
                          @"user_name" : userName};
    dic = [PublicParameterCompoments addTokenToParams:dic];
    
    [[VoiceRTCManager shareRtc] emitWithAck:@"csCreateMeeting"
                                       with:dic
                                      block:^(RTMACKModel * _Nonnull ackModel) {
        NSString *token = @"";
        VoiceControlRoomModel *roomModel = nil;
        NSMutableArray *modelLsts = [[NSMutableArray alloc] init];
        if ([ackModel.response isKindOfClass:[NSDictionary class]]) {
            token = ackModel.response[@"token"];
            roomModel = [VoiceControlRoomModel yy_modelWithJSON:ackModel.response[@"info"]];
            NSArray *infos = ackModel.response[@"users"];
            for (int i = 0; i < infos.count; i++) {
                VoiceControlUserModel *model = [VoiceControlUserModel yy_modelWithJSON:infos[i]];
                [modelLsts addObject:model];
            }
        }
        if (block) {
            block(token, roomModel, [modelLsts copy], ackModel);
        }
    }];
}

+ (void)joinVoice:(NSString *)roomID
         userName:(NSString *)userName
            block:(void (^)(NSString *token,
                            VoiceControlRoomModel *roomModel,
                            NSArray<VoiceControlUserModel *> *lists,
                            RTMACKModel *model))block {
    NSDictionary *dic = @{};
    if (NOEmptyStr(roomID) && NOEmptyStr(userName)) {
        dic = @{@"room_id" : roomID,
                @"user_name" : userName};
    }
    dic = [PublicParameterCompoments addTokenToParams:dic];
    
    [[VoiceRTCManager shareRtc] emitWithAck:@"csJoinMeeting"
                                       with:dic
                                      block:^(RTMACKModel * _Nonnull ackModel) {
        NSString *token = @"";
        VoiceControlRoomModel *roomModel = nil;
        NSMutableArray *modelLsts = [[NSMutableArray alloc] init];
        if ([ackModel.response isKindOfClass:[NSDictionary class]]) {
            token = ackModel.response[@"token"];
            roomModel = [VoiceControlRoomModel yy_modelWithJSON:ackModel.response[@"info"]];
            NSArray *infos = ackModel.response[@"users"];
            for (int i = 0; i < infos.count; i++) {
                VoiceControlUserModel *model = [VoiceControlUserModel yy_modelWithJSON:infos[i]];
                if (model) {
                    [modelLsts addObject:model];
                }
            }
        }
        if (block) {
            block(token, roomModel, [modelLsts copy], ackModel);
        }
    }];

}

+ (void)leaveVoice:(void (^)(RTMACKModel * _Nonnull))block {
    NSDictionary *dic = [PublicParameterCompoments addTokenToParams:nil];
    [[VoiceRTCManager shareRtc] emitWithAck:@"csLeaveMeeting" with:dic block:block];
}

+ (void)getRaiseHandsWithBlock:(void (^)(NSArray<VoiceControlUserModel *> * _Nonnull, RTMACKModel * _Nonnull))block {
    NSDictionary *dic = [PublicParameterCompoments addTokenToParams:nil];
    [[VoiceRTCManager shareRtc] emitWithAck:@"csGetRaiseHands"
                                       with:dic
                                      block:^(RTMACKModel * _Nonnull ackModel) {
        NSMutableArray *modelLsts = [[NSMutableArray alloc] init];
        NSArray *data = (NSArray *)ackModel.response[@"users"];
        if (data && [data isKindOfClass:[NSArray class]]) {
            for (int i = 0; i < data.count; i++) {
                VoiceControlUserModel *userModel = [VoiceControlUserModel yy_modelWithJSON:data[i]];
                [modelLsts addObject:userModel];
            }
        }
        if (block) {
            block([modelLsts copy], ackModel);
        }
    }];
}

+ (void)getAudiencesWithBlock:(void (^)(NSArray<VoiceControlUserModel *> * _Nonnull, RTMACKModel * _Nonnull))block {
    NSDictionary *dic = [PublicParameterCompoments addTokenToParams:nil];
    [[VoiceRTCManager shareRtc] emitWithAck:@"csGetAudiences"
                                       with:dic
                                      block:^(RTMACKModel * _Nonnull ackModel) {
        NSMutableArray *modelLsts = [[NSMutableArray alloc] init];
        NSArray *data = (NSArray *)ackModel.response[@"users"];
        if (data && [data isKindOfClass:[NSArray class]]) {
            for (int i = 0; i < data.count; i++) {
                VoiceControlUserModel *userModel = [VoiceControlUserModel yy_modelWithJSON:data[i]];
                [modelLsts addObject:userModel];
            }
        }
        if (block) {
            block([modelLsts copy], ackModel);
        }
    }];
}

+ (void)reconnectWithBlock:(void (^)(VoiceControlRoomModel *, NSArray *users, RTMACKModel * _Nonnull))block {
    NSDictionary *dic = [PublicParameterCompoments addTokenToParams:nil];
    
    [[VoiceRTCManager shareRtc] emitWithAck:@"csReconnect" with:dic block:^(RTMACKModel * _Nonnull ackModel) {
        VoiceControlRoomModel *roomModel = nil;
        NSMutableArray *userLists = [[NSMutableArray alloc] init];
        if ([ackModel.response isKindOfClass:[NSDictionary class]]) {
            roomModel = [VoiceControlRoomModel yy_modelWithJSON:ackModel.response[@"info"]];
            NSArray *infos = ackModel.response[@"users"];
            for (int i = 0; i < infos.count; i++) {
                VoiceControlUserModel *model = [VoiceControlUserModel yy_modelWithJSON:infos[i]];
                if (model) {
                    [userLists addObject:model];
                }
            }
        }
        dispatch_async(dispatch_get_main_queue(), ^{
            if (block) {
                block(roomModel, [userLists copy], ackModel);
            }
        });
    }];
}

#pragma mark - Control Voice status

+ (void)inviteMic:(NSString *)userId block:(void (^)(RTMACKModel * _Nonnull))block {
    NSDictionary *dic = [PublicParameterCompoments addTokenToParams:nil];
    NSMutableDictionary *mutableDic = [dic mutableCopy];
    [mutableDic setValue:userId ?: @"" forKey:@"user_id"];
    dic = [mutableDic copy];
    [[VoiceRTCManager shareRtc] emitWithAck:@"csInviteMic"
                                       with:dic
                                      block:block];
}

+ (void)confirmMicWithBlock:(void (^)(RTMACKModel * _Nonnull))block {
    NSDictionary *dic = [PublicParameterCompoments addTokenToParams:nil];
    [[VoiceRTCManager shareRtc] emitWithAck:@"csConfirmMic"
                                       with:dic
                                      block:block];
}

+ (void)raiseHandsMicWithBlock:(void (^)(RTMACKModel * _Nonnull))block {
    NSDictionary *dic = [PublicParameterCompoments addTokenToParams:nil];
    [[VoiceRTCManager shareRtc] emitWithAck:@"csRaiseHandsMic"
                                       with:dic
                                      block:block];
}

+ (void)agreeMic:(NSString *)userId block:(void (^)(RTMACKModel * _Nonnull))block {
    NSDictionary *dic = [PublicParameterCompoments addTokenToParams:nil];
    NSMutableDictionary *mutableDic = [dic mutableCopy];
    [mutableDic setValue:userId ?: @"" forKey:@"user_id"];
    dic = [mutableDic copy];
    [[VoiceRTCManager shareRtc] emitWithAck:@"csAgreeMic"
                                       with:dic
                                      block:block];
}

+ (void)offSelfMicWithBlock:(void (^)(RTMACKModel * _Nonnull))block {
    NSDictionary *dic = [PublicParameterCompoments addTokenToParams:nil];
    [[VoiceRTCManager shareRtc] emitWithAck:@"csOffSelfMic"
                                       with:dic
                                      block:block];
}

+ (void)offMic:(NSString *)userId block:(void (^)(RTMACKModel * _Nonnull))block{
    NSDictionary *dic = [PublicParameterCompoments addTokenToParams:nil];
    NSMutableDictionary *mutableDic = [dic mutableCopy];
    [mutableDic setValue:userId ?: @"" forKey:@"user_id"];
    dic = [mutableDic copy];
    [[VoiceRTCManager shareRtc] emitWithAck:@"csOffMic"
                                       with:dic
                                      block:block];
}
                                                                      
+ (void)muteMic {
    NSDictionary *dic = [PublicParameterCompoments addTokenToParams:nil];
    [[VoiceRTCManager shareRtc] emitWithAck:@"csMuteMic" with:dic block:nil];
}

+ (void)unmuteMic {
    NSDictionary *dic = [PublicParameterCompoments addTokenToParams:nil];
    [[VoiceRTCManager shareRtc] emitWithAck:@"csUnmuteMic" with:dic block:nil];
}

#pragma mark - Notification message

+ (void)onJoinMeetingWithBlock:(void (^)(VoiceControlUserModel *userModel))block {
    [[VoiceRTCManager shareRtc] onSceneListener:@"onCsJoinMeeting"
                                          block:^(RTMNoticeModel * _Nonnull noticeModel) {
        VoiceControlUserModel *model = nil;
        if (noticeModel.data && [noticeModel.data isKindOfClass:[NSDictionary class]]) {
            model = [VoiceControlUserModel yy_modelWithJSON:noticeModel.data];
        }
        if (block) {
            block(model);
        }
    }];
}

+ (void)onLeaveMeetingWithBlock:(void (^)(VoiceControlUserModel *userModel))block {
    [[VoiceRTCManager shareRtc] onSceneListener:@"onCsLeaveMeeting"
                                          block:^(RTMNoticeModel * _Nonnull noticeModel) {
        VoiceControlUserModel *model = nil;
        if (noticeModel.data && [noticeModel.data isKindOfClass:[NSDictionary class]]) {
            model = [VoiceControlUserModel yy_modelWithJSON:noticeModel.data];
        }
        if (block) {
            block(model);
        }
    }];
}

+ (void)onRaiseHandsMicWithBlock:(void (^)(NSString *uid))block {
    [[VoiceRTCManager shareRtc] onSceneListener:@"onCsRaiseHandsMic"
                                              block:^(RTMNoticeModel * _Nonnull noticeModel) {
        NSString *uid = @"";
        if (noticeModel.data && [noticeModel.data isKindOfClass:[NSDictionary class]]) {
            uid = noticeModel.data[@"user_id"];
        }
        if (block) {
            block(uid);
        }
    }];
}

+ (void)onInviteMicWithBlock:(void (^)(NSString *uid))block {
    [[VoiceRTCManager shareRtc] onSceneListener:@"onCsInviteMic"
                                          block:^(RTMNoticeModel * _Nonnull noticeModel) {
        NSString *uid = @"";
        if (noticeModel.data && [noticeModel.data isKindOfClass:[NSDictionary class]]) {
            uid = noticeModel.data[@"user_id"];
        }
        if (block) {
            block(uid);
        }
    }];
}

+ (void)onMicOnWithBlock:(void (^)(VoiceControlUserModel *userModel))block {
    [[VoiceRTCManager shareRtc] onSceneListener:@"onCsMicOn"
                                          block:^(RTMNoticeModel * _Nonnull noticeModel) {
        VoiceControlUserModel *model = nil;
        if (noticeModel.data && [noticeModel.data isKindOfClass:[NSDictionary class]]) {
            model = [VoiceControlUserModel yy_modelWithJSON:noticeModel.data];
        }
        if (block) {
            block(model);
        }
    }];
}

+ (void)onMicOffWithBlock:(void (^)(NSString *uid))block {
    [[VoiceRTCManager shareRtc] onSceneListener:@"onCsMicOff"
                                          block:^(RTMNoticeModel * _Nonnull noticeModel) {
        NSString *uid = @"";
        if (noticeModel.data && [noticeModel.data isKindOfClass:[NSDictionary class]]) {
            uid = noticeModel.data[@"user_id"];
        }
        if (block) {
            block(uid);
        }
    }];
}

+ (void)onMuteMicWithBlock:(void (^)(NSString *uid))block {
    [[VoiceRTCManager shareRtc] onSceneListener:@"onCsMuteMic"
                                          block:^(RTMNoticeModel * _Nonnull noticeModel) {
        NSString *uid = @"";
        if (noticeModel.data && [noticeModel.data isKindOfClass:[NSDictionary class]]) {
            uid = noticeModel.data[@"user_id"];
        }
        if (block) {
            block(uid);
        }
    }];
}

+ (void)onUnmuteMic:(void (^)(NSString * _Nonnull uid))block {
    [[VoiceRTCManager shareRtc] onSceneListener:@"onCsUnmuteMic"
                                          block:^(RTMNoticeModel * _Nonnull noticeModel) {
        NSString *uid = @"";
        if (noticeModel.data && [noticeModel.data isKindOfClass:[NSDictionary class]]) {
            uid = noticeModel.data[@"user_id"];
        }
        if (block) {
            block(uid);
        }
    }];
}

+ (void)onMeetingEnd:(void (^)(BOOL result))block {
    [[VoiceRTCManager shareRtc] onSceneListener:@"onCsMeetingEnd"
                                          block:^(RTMNoticeModel * _Nonnull noticeModel) {
        NSString *roomID = @"";
        if (noticeModel.data && [noticeModel.data isKindOfClass:[NSDictionary class]]) {
            roomID = noticeModel.data[@"room_id"];
        }
        if (block) {
            block([roomID isEqualToString:[PublicParameterCompoments share].roomId]);
        }
    }];
}

+ (void)onHostChange:(void (^)(NSString *formerHostID, VoiceControlUserModel *hostUser))block {
    [[VoiceRTCManager shareRtc] onSceneListener:@"onCsHostChange"
                                          block:^(RTMNoticeModel * _Nonnull noticeModel) {
        NSString *forUid = @"";
        VoiceControlUserModel *model = nil;
        if (noticeModel.data && [noticeModel.data isKindOfClass:[NSDictionary class]]) {
            forUid = noticeModel.data[@"former_host_id"];
            model = [VoiceControlUserModel yy_modelWithJSON:noticeModel.data[@"host_info"]];
        }
        if (block) {
            block(forUid, model);
        }
    }];
}

@end
