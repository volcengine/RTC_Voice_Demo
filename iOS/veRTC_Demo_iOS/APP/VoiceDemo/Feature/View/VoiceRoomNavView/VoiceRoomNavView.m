//
//  RoomNavView.m
//  quickstart
//
//  Created by on 2021/3/23.
//  
//

#import "VoiceRoomNavView.h"
#import "GCDTimer.h"
#import "VoiceAvatarComponent.h"

@interface VoiceRoomNavView ()

@property (nonatomic, strong) BaseButton *hangeupButton;
@property (nonatomic, strong) VoiceAvatarComponent *avatarComponent;

@property (nonatomic, strong) UIView *contentView;
@property (nonatomic, strong) UILabel *roomIdLabel;
@property (nonatomic, strong) UIView *lineView;
@property (nonatomic, strong) UILabel *timeLabel;

@property (nonatomic, strong) GCDTimer *timer;
@property (nonatomic, assign) NSInteger secondValue;

@end

@implementation VoiceRoomNavView

- (instancetype)init {
    self = [super init];
    if (self) {
        self.backgroundColor = [UIColor clearColor];
        [self addSubviewAndConstraints];
        
        __weak __typeof(self) wself = self;
        [self.timer startTimerWithSpace:1 block:^(BOOL result) {
            [wself timerMethod];
        }];
        [self.timer suspend];
    }
    return self;
}

- (void)hangeupButtonAction:(UIButton *)sender {
    if ([self.delegate respondsToSelector:@selector(voiceRoomNavView:didSelectStatus:)]) {
        [self.delegate voiceRoomNavView:self didSelectStatus:RoomNavStatusHangeup];
    }
}

#pragma mark - Publish Action

- (void)setMeetingTime:(NSInteger)meetingTime {
    self.secondValue = meetingTime;
    [self.timer resume];
}

- (void)setRoomModel:(VoiceControlRoomModel *)roomModel {
    _roomModel = roomModel;
    NSString *roomIDStr = roomModel.room_id;
    if (roomIDStr.length > 9) {
        NSString *aboveStr = [roomIDStr substringToIndex:3];
        NSString *afterStr = [roomIDStr substringWithRange:NSMakeRange(roomIDStr.length - 3, 3)];
        roomIDStr = [NSString stringWithFormat:@"%@...%@", aboveStr, afterStr];
    }
    self.roomIdLabel.text = [NSString stringWithFormat:@"ID：%@", roomIDStr];
    
    NSInteger time = (roomModel.now - roomModel.created_at) / 1000000000;
    if (time < 0) {
        time = 0;
    }
    self.meetingTime = time;
    
    self.avatarComponent.text = [LocalUserComponent userModel].name;
}


#pragma mark - Private Action

- (void)timerMethod {
    NSInteger minute = self.secondValue / 60;
    NSInteger second = self.secondValue - (minute * 60);
    NSString *minuteStr = (minute < 10) ? [NSString stringWithFormat:@"0%ld", minute] : [NSString stringWithFormat:@"%ld", (long)minute];
    NSString *secondStr = (second < 10) ? [NSString stringWithFormat:@"0%ld", second] : [NSString stringWithFormat:@"%ld", (long)second];
    self.timeLabel.text = [NSString stringWithFormat:@"%@:%@", minuteStr, secondStr];
    self.secondValue++;
}

- (void)addSubviewAndConstraints {
    [self addSubview:self.hangeupButton];
    [self.hangeupButton mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.equalTo(self).mas_offset(16.f);
        make.centerY.equalTo(self).mas_offset([DeviceInforTool getStatusBarHight]/2);
        make.height.width.mas_equalTo(24.f);
    }];
    
    [self addSubview:self.avatarComponent];
    [self.avatarComponent mas_makeConstraints:^(MASConstraintMaker *make) {
        make.right.equalTo(self).mas_offset(-16.f);
        make.centerY.equalTo(self).mas_offset([DeviceInforTool getStatusBarHight]/2);
        make.height.width.mas_equalTo(30);
    }];
    
    [self.contentView addSubview:self.lineView];
    [self.contentView addSubview:self.roomIdLabel];
    [self.contentView addSubview:self.timeLabel];
    [self addSubview:self.contentView];
    [self.contentView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.height.mas_equalTo(44);
        make.centerX.equalTo(self);
        make.centerY.equalTo(self.hangeupButton);
    }];
    
    [self.roomIdLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.equalTo(self.contentView);
        make.centerY.equalTo(self.hangeupButton);
        make.height.mas_equalTo(44);
    }];
    
    [self.lineView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.size.mas_equalTo(CGSizeMake(1, 12));
        make.left.equalTo(self.roomIdLabel.mas_right).offset(10);
        make.centerY.equalTo(self.hangeupButton);
    }];
    
    [self.timeLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.equalTo(self.lineView.mas_right).offset(8);
        make.right.equalTo(self.contentView);
        make.centerY.equalTo(self.hangeupButton);
    }];
}

#pragma mark - getter

- (UIView *)contentView {
    if (!_contentView) {
        _contentView = [[UIView alloc] init];
    }
    return _contentView;
}

- (UILabel *)timeLabel {
    if (!_timeLabel) {
        _timeLabel = [[UILabel alloc] init];
        _timeLabel.textColor = [UIColor colorFromHexString:@"#86909C"];
        _timeLabel.font = [UIFont systemFontOfSize:12.f];
        _timeLabel.text = @"00:00";
        _timeLabel.backgroundColor = [UIColor clearColor];
    }
    return _timeLabel;
}

- (UILabel *)roomIdLabel {
    if (!_roomIdLabel) {
        _roomIdLabel = [[UILabel alloc] init];
        _roomIdLabel.textColor = [UIColor whiteColor];
        _roomIdLabel.font = [UIFont systemFontOfSize:16.f];
    }
    return _roomIdLabel;
}

- (UIView *)lineView {
    if (!_lineView) {
        _lineView = [[UIView alloc] init];
        _lineView.backgroundColor = [UIColor colorFromHexString:@"#4E5969"];
    }
    return _lineView;
}

- (VoiceAvatarComponent *)avatarComponent {
    if (!_avatarComponent) {
        _avatarComponent = [[VoiceAvatarComponent alloc] init];
        _avatarComponent.layer.cornerRadius = 15;
        _avatarComponent.layer.masksToBounds = YES;
        _avatarComponent.fontSize = 16;
    }
    return _avatarComponent;
}

- (BaseButton *)hangeupButton {
    if (!_hangeupButton) {
        _hangeupButton = [BaseButton buttonWithType:UIButtonTypeCustom];
        [_hangeupButton setImage:[UIImage imageNamed:@"voice_leave" bundleName:HomeBundleName] forState:UIControlStateNormal];
        [_hangeupButton addTarget:self action:@selector(hangeupButtonAction:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _hangeupButton;
}

- (GCDTimer *)timer {
    if (!_timer) {
        _timer = [[GCDTimer alloc] init];
    }
    return _timer;
}

@end
