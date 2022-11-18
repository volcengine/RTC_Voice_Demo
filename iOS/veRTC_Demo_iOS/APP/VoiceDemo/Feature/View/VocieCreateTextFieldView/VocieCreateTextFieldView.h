//
//  VocieTextFileView.h
//  veRTC_Demo
//
//  Created by on 2021/5/21.
//  
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface VocieCreateTextFieldView : UIView

@property (nonatomic, assign, readonly) BOOL isIllega;

@property (nonatomic, assign) BOOL isCheckIllega;

@property (nonatomic, assign) BOOL isModify;

@property (nonatomic, assign) NSInteger maxLimit;

@property (nonatomic, assign) BOOL isOnlyNumber;

@property (nonatomic, copy, nullable) NSString *errorMessage;

@property (nonatomic, assign) CGFloat rightSpace;

@property (nonatomic, copy) NSString *placeholderStr;

@property (nonatomic, assign) UIKeyboardType boardType;

@property (nonatomic, copy, nullable) NSString *text;

@property (nonatomic, copy) void (^textFieldChangeBlock)(NSString *text);

- (instancetype)initWithModify:(BOOL)isModify;

- (void)resignFirstResponder;

- (void)becomeFirstResponder;

@end

NS_ASSUME_NONNULL_END
