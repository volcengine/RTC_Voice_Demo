//
//  VoiceNavViewController.h
//  quickstart
//
//  Created by on 2021/3/22.
//  
//

#import <UIKit/UIKit.h>
#import "Core.h"

NS_ASSUME_NONNULL_BEGIN

@interface VoiceNavViewController : UIViewController

@property (nonatomic, copy) NSString *navTitle;

@property (nonatomic, strong) UIView *navView;

@property (nonatomic, strong) UIView *bgView;

@property (nonatomic, copy) NSString *rightTitle;

- (void)rightButtonAction:(BaseButton *)sender;

@end

NS_ASSUME_NONNULL_END
