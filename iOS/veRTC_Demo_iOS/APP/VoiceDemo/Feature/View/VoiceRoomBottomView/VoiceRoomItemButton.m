//
//  VoiceRoomItemButton.m
//  quickstart
//
//  Created by on 2021/3/24.
//  
//

#import "VoiceRoomItemButton.h"

@interface VoiceRoomItemButton ()

@property (nonatomic, strong) UILabel *desLabel;

@end

@implementation VoiceRoomItemButton

- (instancetype)init {
    self = [super init];
    if (self) {
        self.clipsToBounds = NO;
        
        [self addSubview:self.desLabel];
        [self.desLabel mas_makeConstraints:^(MASConstraintMaker *make) {
            make.bottom.equalTo(self).offset(-10);
            make.centerX.equalTo(self);
        }];
    }
    return self;
}

- (void)setDesTitle:(NSString *)desTitle {
    _desTitle = desTitle;
    
    self.desLabel.text = desTitle;
}

- (void)setIsAction:(BOOL)isAction {
    _isAction = isAction;
    
    if (isAction) {
        self.desLabel.textColor = [UIColor colorFromHexString:@"#4080FF"];
    } else {
        self.desLabel.textColor = [UIColor colorFromHexString:@"#86909C"];
    }
}

#pragma mark - getter

- (UILabel *)desLabel {
    if (!_desLabel) {
        _desLabel = [[UILabel alloc] init];
        _desLabel.textColor = [UIColor colorFromHexString:@"#86909C"];
        _desLabel.font = [UIFont systemFontOfSize:12];
    }
    return _desLabel;
}

@end
