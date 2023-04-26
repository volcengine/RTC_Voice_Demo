// 
// Copyright (c) 2023 Beijing Volcano Engine Technology Ltd.
// SPDX-License-Identifier: MIT
// 

#import "VoiceRoomView.h"
#import "VoiceRoomTitleCell.h"
#import "VoiceRoomMicCell.h"
#import "VoiceRoomAudienceCell.h"
#import "VoiceRoomSubTitleCell.h"

@interface VoiceRoomView ()<UITableViewDelegate, UITableViewDataSource>

@property (nonatomic, strong) NSMutableArray<VoiceControlUserModel *> *hostLists;
@property (nonatomic, strong) NSMutableArray<VoiceControlUserModel *> *audienceLists;
@property (nonatomic, strong) UITableView *roomTableView;
@property (nonatomic, strong) NSMutableArray *roomDataLists;
@property (nonatomic, strong) VoiceControlRoomModel *roomModel;
@property (nonatomic, strong) GCDTimer *timer;
@property (nonatomic, strong) dispatch_semaphore_t lock;

@end


@implementation VoiceRoomView

- (instancetype)init
{
    self = [super init];
    if (self) {
        self.backgroundColor = [UIColor clearColor];
        
        [self addSubview:self.roomTableView];
        [self.roomTableView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.edges.equalTo(self);
        }];
        
        __weak __typeof(self) wself = self;
        [self.timer startTimerWithSpace:0.5 block:^(BOOL result) {
            [wself timerMethod];
        }];
    }
    return self;
}


#pragma mark - Publish Action

- (void)updateAllUser:(NSArray<VoiceControlUserModel *> *)userLists roomModel:(VoiceControlRoomModel *)roomModel {
    self.roomModel = roomModel;
    
    dispatch_semaphore_wait(self.lock, DISPATCH_TIME_FOREVER);
    [self.hostLists removeAllObjects];
    [self.audienceLists removeAllObjects];
    if (userLists && userLists.count > 0) {
        for (VoiceControlUserModel *userModel in userLists) {
            if (userModel.is_host || userModel.user_status == 2) {
                [self.hostLists addObject:userModel];
            } else {
                [self.audienceLists addObject:userModel];
            }
        }
    }
    [self updateRoomDataLists];
    dispatch_semaphore_signal(self.lock);
}

- (void)joinUser:(VoiceControlUserModel *)user {
    if (!self.roomModel) {
        return;
    }
    dispatch_semaphore_wait(self.lock, DISPATCH_TIME_FOREVER);
    VoiceControlUserModel *deleteUserModel = nil;
    for (VoiceControlUserModel *model in self.hostLists) {
        if ([model.uid isEqualToString:user.uid]) {
            deleteUserModel = model;
        }
    }
    if (deleteUserModel) {
        [self.hostLists removeObject:deleteUserModel];
    }
    
    NSInteger replaceIndex = -1;
    VoiceControlUserModel *replaceUserModel = nil;
    for (int i = 0; i < self.audienceLists.count; i++) {
        VoiceControlUserModel *currentUser = self.audienceLists[i];
        if ([currentUser.uid isEqualToString:user.uid]) {
            replaceIndex = i;
            replaceUserModel = user;
            break;
        }
    }
    if (replaceIndex >= 0 && replaceUserModel) {
        [self.audienceLists replaceObjectAtIndex:replaceIndex withObject:replaceUserModel];
    } else {
        [self.audienceLists insertObject:user atIndex:0];
    }
    [self updateRoomDataLists];
    dispatch_semaphore_signal(self.lock);
}

- (void)leaveUser:(NSString *)user {
    dispatch_semaphore_wait(self.lock, DISPATCH_TIME_FOREVER);
    VoiceControlUserModel *leaveUserModel = nil;
    for (VoiceControlUserModel *model in self.hostLists) {
        if ([model.uid isEqualToString:user]) {
            leaveUserModel = model;
        }
    }
    if (leaveUserModel) {
        [self.hostLists removeObject:leaveUserModel];
        [self updateRoomDataLists];
        leaveUserModel = nil;
    }
    for (VoiceControlUserModel *model in self.audienceLists) {
        if ([model.uid isEqualToString:user]) {
            leaveUserModel = model;
        }
    }
    if (leaveUserModel) {
        [self.audienceLists removeObject:leaveUserModel];
        [self updateRoomDataLists];
    }
    dispatch_semaphore_signal(self.lock);
}

- (void)audienceRaisedHandsSuccess:(VoiceControlUserModel *)userModel {
    dispatch_semaphore_wait(self.lock, DISPATCH_TIME_FOREVER);
    VoiceControlUserModel *newHostUser = nil;
    VoiceControlUserModel *deleteHostUser = nil;
    for (VoiceControlUserModel *model in self.audienceLists) {
        if ([model.uid isEqualToString:userModel.uid]) {
            newHostUser = userModel;
            deleteHostUser = model;
            break;
        }
    }
    if (newHostUser) {
        [self.audienceLists removeObject:deleteHostUser];
        [self.hostLists addObject:newHostUser];
        [self updateRoomDataLists];
    }
    dispatch_semaphore_signal(self.lock);
}

- (void)hostLowerHandSuccess:(NSString *)uid {
    dispatch_semaphore_wait(self.lock, DISPATCH_TIME_FOREVER);
    VoiceControlUserModel *newAudienceUser = nil;
    for (VoiceControlUserModel *model in self.hostLists) {
        if ([model.uid isEqualToString:uid]) {
            newAudienceUser = model;
            newAudienceUser.user_status = 0;
        }
    }
    if (newAudienceUser) {
        [self.hostLists removeObject:newAudienceUser];
        [self.audienceLists insertObject:newAudienceUser atIndex:0];
        [self updateRoomDataLists];
    }
    dispatch_semaphore_signal(self.lock);
}

- (void)updateHostVolume:(NSDictionary<NSString *, NSNumber *> *_Nonnull)volumeInfo {
    for (VoiceControlUserModel *model in self.hostLists) {
        NSNumber *volume = [volumeInfo objectForKey:model.uid];
        model.volume = [volume integerValue];
    }
}

- (void)updateHostUser:(NSString *)uid {
    dispatch_semaphore_wait(self.lock, DISPATCH_TIME_FOREVER);
    for (VoiceControlUserModel *model in self.hostLists) {
        if ([model.uid isEqualToString:uid]) {
            model.is_host = YES;
        } else {
            model.is_host = NO;
        }
    }
    dispatch_semaphore_signal(self.lock);
}

- (void)updateUserHand:(NSString *)uid isHand:(BOOL)isHand {
    dispatch_semaphore_wait(self.lock, DISPATCH_TIME_FOREVER);
    for (VoiceControlUserModel *model in self.audienceLists) {
        if ([model.uid isEqualToString:uid]) {
            model.user_status = isHand ? 1 : 0;
            break;
        }
    }
    dispatch_semaphore_signal(self.lock);
}

- (void)updateUserMic:(NSString *)uid isMute:(BOOL)isMute {
    dispatch_semaphore_wait(self.lock, DISPATCH_TIME_FOREVER);
    for (VoiceControlUserModel *model in self.hostLists) {
        if ([model.uid isEqualToString:uid]) {
            model.is_mic_on = !isMute;
            break;
        }
    }
    dispatch_semaphore_signal(self.lock);
}

- (NSArray<VoiceControlUserModel *> *)allUserLists {
    dispatch_semaphore_wait(self.lock, DISPATCH_TIME_FOREVER);
    NSMutableArray *lists = [[NSMutableArray alloc] init];
    [lists addObjectsFromArray:self.hostLists];
    [lists addObjectsFromArray:self.audienceLists];
    dispatch_semaphore_signal(self.lock);
    return [lists copy];
}

#pragma mark - Private Action

- (void)timerMethod {
    [self reloadData];
}

- (void)updateRoomDataLists {
    [self.roomDataLists removeAllObjects];
    //title
    if (self.roomModel && NOEmptyStr(self.roomModel.room_name)) {
        [self.roomDataLists addObject:self.roomModel.room_name];
    } else {
        [self.roomDataLists addObject:@""];
    }
    //host
    [self.roomDataLists addObject:[self.hostLists copy]];
    //sub title
    [self.roomDataLists addObject:[NSString stringWithFormat:@"其他听众%ld人", (long)self.audienceLists.count]];
    //audience
    [self.roomDataLists addObject:[self.audienceLists copy]];
    [self reloadData];
}

- (void)reloadData {
    [self.roomTableView reloadData];
}

- (NSInteger)hostNumber {
    return self.hostLists.count;
}

#pragma mark - UITableViewDelegate

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    UITableViewCell *cell = [UITableViewCell new];
    switch (indexPath.row) {
        case 0:
            cell = [tableView dequeueReusableCellWithIdentifier:@"VoiceRoomTitleCellID" forIndexPath:indexPath];
            ((VoiceRoomTitleCell *)cell).titleStr = self.roomDataLists[indexPath.row];
            break;
        case 1:
            cell = [tableView dequeueReusableCellWithIdentifier:@"VoiceRoomMicCellID" forIndexPath:indexPath];
            ((VoiceRoomMicCell *)cell).dataLists = self.roomDataLists[indexPath.row];
            break;
        case 2:
            cell = [tableView dequeueReusableCellWithIdentifier:@"VoiceRoomSubTitleCellID" forIndexPath:indexPath];
            ((VoiceRoomSubTitleCell *)cell).titleStr = self.roomDataLists[indexPath.row];
            break;
        case 3:
            cell = [tableView dequeueReusableCellWithIdentifier:@"VoiceRoomAudienceCellID" forIndexPath:indexPath];
            ((VoiceRoomAudienceCell *)cell).dataLists = self.roomDataLists[indexPath.row];
            break;
        default:
            break;
    }
    cell.selectionStyle = UITableViewCellSelectionStyleNone;
    return cell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:NO];
}

#pragma mark - UITableViewDataSource

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.roomDataLists.count;
}


#pragma mark - getter


- (UITableView *)roomTableView {
    if (!_roomTableView) {
        _roomTableView = [[UITableView alloc] init];
        _roomTableView.separatorStyle = UITableViewCellSeparatorStyleNone;
        _roomTableView.delegate = self;
        _roomTableView.dataSource = self;
        [_roomTableView registerClass:VoiceRoomTitleCell.class forCellReuseIdentifier:@"VoiceRoomTitleCellID"];
        [_roomTableView registerClass:VoiceRoomMicCell.class forCellReuseIdentifier:@"VoiceRoomMicCellID"];
        [_roomTableView registerClass:VoiceRoomSubTitleCell.class forCellReuseIdentifier:@"VoiceRoomSubTitleCellID"];
        [_roomTableView registerClass:VoiceRoomAudienceCell.class forCellReuseIdentifier:@"VoiceRoomAudienceCellID"];
        _roomTableView.backgroundColor = [UIColor clearColor];
        _roomTableView.rowHeight = UITableViewAutomaticDimension;
        _roomTableView.estimatedRowHeight = 36;
        _roomTableView.estimatedSectionFooterHeight = 0;
        _roomTableView.estimatedSectionHeaderHeight = 0;
    }
    return _roomTableView;
}

- (NSMutableArray *)roomDataLists {
    if (!_roomDataLists) {
        _roomDataLists = [[NSMutableArray alloc] init];
    }
    return _roomDataLists;
}

- (NSMutableArray<VoiceControlUserModel *> *)hostLists {
    if (!_hostLists) {
        _hostLists = [[NSMutableArray alloc] init];
    }
    return _hostLists;
}

- (NSMutableArray<VoiceControlUserModel *> *)audienceLists {
    if (!_audienceLists) {
        _audienceLists = [[NSMutableArray alloc] init];
    }
    return _audienceLists;
}

- (GCDTimer *)timer {
    if (!_timer) {
        _timer = [[GCDTimer alloc] init];
    }
    return _timer;
}

- (dispatch_semaphore_t)lock {
    if (_lock == nil) {
        _lock = dispatch_semaphore_create(1);
    }
    return _lock;
}

@end
