//
//  DWMsgModel+CoreDataProperties.m
//  RCTAliyunPush
//
//  Created by Dowin on 2018/10/17.
//  Copyright © 2018年 Facebook. All rights reserved.
//
//

#import "DWMsgModel+CoreDataProperties.h"

@implementation DWMsgModel (CoreDataProperties)

+ (NSFetchRequest<DWMsgModel *> *)fetchRequest {
	return [NSFetchRequest fetchRequestWithEntityName:@"DWMsgModel"];
}

@dynamic account;
@dynamic data;
@dynamic msgId;
@dynamic msgtype;
@dynamic timeString;

@end
