//
//  DWCoreDataTool.h
//  RCTAliyunPush
//
//  Created by Dowin on 2018/10/17.
//  Copyright © 2018年 Facebook. All rights reserved.
//

#import <Foundation/Foundation.h>



@interface DWCoreDataTool : NSObject

+ (DWCoreDataTool *)sharedInstance;

//插入对象
- (BOOL)insertClickedWithDict:(NSDictionary *)dict;
//删除数据
- (BOOL)deleteData;
//根据account 和page查询
- (NSMutableArray *)queryModelDictWithPage:(int)page account:(NSString *)strAccount;


@end


