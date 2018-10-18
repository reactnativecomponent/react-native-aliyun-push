//
//  DWMsgModel+CoreDataProperties.h
//  RCTAliyunPush
//
//  Created by Dowin on 2018/10/17.
//  Copyright © 2018年 Facebook. All rights reserved.
//
//

#import "DWMsgModel+CoreDataClass.h"


NS_ASSUME_NONNULL_BEGIN

@interface DWMsgModel (CoreDataProperties)

+ (NSFetchRequest<DWMsgModel *> *)fetchRequest;

@property (nullable, nonatomic, copy) NSString *account;
@property (nullable, nonatomic, retain) NSData *data;
@property (nullable, nonatomic, copy) NSString *msgId;
@property (nullable, nonatomic, copy) NSString *msgtype;
@property (nullable, nonatomic, copy) NSString *timeString;

@end

NS_ASSUME_NONNULL_END
