//
//  DWCoreDataTool.m
//  RCTAliyunPush
//
//  Created by Dowin on 2018/10/17.
//  Copyright © 2018年 Facebook. All rights reserved.
//

#import "DWCoreDataTool.h"
#import <CoreData/CoreData.h>
#import "DWMsgModel+CoreDataClass.h"

@interface DWCoreDataTool ()

@end

@implementation DWCoreDataTool

static DWCoreDataTool * sharedInstance = nil;
static NSManagedObjectContext * _context = nil;

+ (BOOL)requiresMainQueueSetup {
    return YES;
}

+ (id)allocWithZone:(NSZone *)zone {
    static DWCoreDataTool *sharedInstance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedInstance = [super allocWithZone:zone];
    });
    return sharedInstance;
}

//copy返回单例本身
- (id)copyWithZone:(NSZone *)zone
{
    return self;
}

- (instancetype)init
{
    
    if (!(self = [super init])) {
        
    }
    sharedInstance = self;
    return self;
    
}

//获取单例
+ (DWCoreDataTool *)sharedInstance
{
    @synchronized(self) {
        if (sharedInstance == nil){
            sharedInstance = [[self alloc] init];
            [self createSqlite];
        }
    }
    return sharedInstance;
}


#pragma mark -- CoreData
//创建数据库
+(void)createSqlite{
    //1、创建模型对象
    NSURL *modelURL = [[NSBundle mainBundle] URLForResource:@"Model" withExtension:@"momd"];
    //根据模型文件创建模型对象
    NSManagedObjectModel *model = [[NSManagedObjectModel alloc] initWithContentsOfURL:modelURL];
    //2、创建持久化存储助理：数据库
    //利用模型对象创建助理对象
    NSPersistentStoreCoordinator *store = [[NSPersistentStoreCoordinator alloc] initWithManagedObjectModel:model];
    //数据库的名称和路径
    NSString *docStr = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) lastObject];
    NSString *sqlPath = [docStr stringByAppendingPathComponent:@"coreData.sqlite"];
    NSLog(@"数据库 path = %@", sqlPath);
    NSURL *sqlUrl = [NSURL fileURLWithPath:sqlPath];
    NSError *error = nil;
    //设置数据库相关信息 添加一个持久化存储库并设置存储类型和路径，NSSQLiteStoreType：SQLite作为存储库
    [store addPersistentStoreWithType:NSSQLiteStoreType configuration:nil URL:sqlUrl options:nil error:&error];
    if (error) {
        NSLog(@"-------添加数据库失败:%@",error);
    } else {
        NSLog(@"-------添加数据库成功");
    }
    //3、创建上下文 保存信息 操作数据库
    NSManagedObjectContext *context = [[NSManagedObjectContext alloc] initWithConcurrencyType:NSMainQueueConcurrencyType];
    //关联持久化助理
    context.persistentStoreCoordinator = store;
    _context = context;
}

//插入对象
- (BOOL)insertClickedWithDict:(NSDictionary *)dict{
    
    NSArray *modelArr = [self queryModelWithMsgId:[dict objectForKey:@"msgId"]];
    DWMsgModel *model;
    if (modelArr.count) {
        model = [modelArr firstObject];
    }else{
        model = [NSEntityDescription insertNewObjectForEntityForName:@"DWMsgModel" inManagedObjectContext:_context];
    }
    model.msgId = [NSString stringWithFormat:@"%@",[dict objectForKey:@"msgId"]];
    model.timeString = [NSString stringWithFormat:@"%@",[dict objectForKey:@"timeString"]];
    model.msgtype = [dict objectForKey:@"msgtype"];
    model.account = [NSString stringWithFormat:@"%@",[[dict objectForKey:@"data"] objectForKey:@"account"] ? :@""];
    NSData *tmpData = [NSJSONSerialization dataWithJSONObject:dict options:NSJSONWritingPrettyPrinted error:nil];
    model.data = tmpData;
    
    //   3.保存更新/插入的数据
    NSError *error = nil;
    if ([_context save:&error]) {
        NSLog(@"-------数据插入到数据库成功");
        return YES;
    }else{
        NSLog(@"-------数据插入到数据库失败, %@",error);
        return NO;
    }
}

//根据msgID查询model
- (NSArray *)queryModelWithMsgId:(NSString *)strId{
    //创建查询请求
    NSFetchRequest *request = [NSFetchRequest fetchRequestWithEntityName:@"DWMsgModel"];
    NSPredicate *pre = [NSPredicate predicateWithFormat:@"msgId = %@",strId];
    request.predicate = pre;
    //发送请求
    NSArray *resArray = [_context executeFetchRequest:request error:nil];
    
    return resArray;
}

//删除数据
- (BOOL)deleteData{
    //创建删除请求
    NSFetchRequest *deleRequest = [NSFetchRequest fetchRequestWithEntityName:@"DWMsgModel"];
    //返回需要删除的对象数组
    NSArray *deleArray = [_context executeFetchRequest:deleRequest error:nil];
    
    //从数据库中删除
    for (DWMsgModel *model in deleArray) {
        [_context deleteObject:model];
    }
    
    NSError *error = nil;
    //保存--记住保存
    if ([_context save:&error]) {
        NSLog(@"删除成功");
        return  YES;
    }else{
        NSLog(@"删除数据失败, %@", error);
        return  NO;
    }
}

//根据account 和page查询
- (NSMutableArray *)queryModelDictWithPage:(int)page account:(NSString *)strAccount{
    static int limit=20;
    NSFetchRequest *request=[NSFetchRequest fetchRequestWithEntityName:@"DWMsgModel"];
    request.fetchLimit=limit;
    request.fetchOffset=(page-1)*limit;
    NSPredicate *predicate=[NSPredicate predicateWithFormat:@"account=%@ or account=%@",strAccount,@""];
    request.predicate=predicate;
    
    NSSortDescriptor *timeSorter=[NSSortDescriptor sortDescriptorWithKey:@"timeString" ascending:NO];
    
    request.sortDescriptors=@[timeSorter];
    
    NSArray *array=[_context executeFetchRequest:request error:nil];
    NSMutableArray *dictArr = [NSMutableArray array];
    for (DWMsgModel *tmpModel in array) {
        NSDictionary *dict = [NSJSONSerialization JSONObjectWithData:tmpModel.data options:NSJSONReadingMutableContainers error:nil];
        [dictArr addObject:dict];
    }
    return dictArr;
}

//查询所有对象
- (NSMutableArray *)queryAllModelDict{
    //查询所有数据的请求
    NSFetchRequest *request = [NSFetchRequest fetchRequestWithEntityName:@"DWMsgModel"];
    NSArray *resArray = [_context executeFetchRequest:request error:nil];
    NSMutableArray *dictArr = [NSMutableArray array];
    for (DWMsgModel *tmpModel in resArray) {
        NSDictionary *dict = [NSJSONSerialization JSONObjectWithData:tmpModel.data options:NSJSONReadingMutableContainers error:nil];
        [dictArr addObject:dict];
    }
    return dictArr;
}


@end
