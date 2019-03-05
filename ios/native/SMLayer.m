//
//  SMLayer.m
//  Supermap
//
//  Created by Yang Shang Long on 2018/11/16.
//  Copyright © 2018 Facebook. All rights reserved.
//

#import "SMLayer.h"

@implementation SMLayer

+ (NSArray *)getLayersByType:(int)type path:(NSString *)path {
    Map* map = [SMap singletonInstance].smMapWC.mapControl.map;
    Layers* layers = map.layers;
    int count = [layers getCount];
    
    NSMutableArray* arr = [[NSMutableArray alloc] init];
    if (path == nil || [path isEqualToString:@""]) {
        for (int i = 0; i < count; i++) {
            Layer* layer = [layers getLayerAtIndex:i];
            Dataset* dataset = layer.dataset;
            
            if (dataset == nil || dataset.datasetType == type || type == -1) {
                NSMutableDictionary* info = [self getLayerInfo:layer path:path];
                [arr addObject:info];
            }
        }
    } else {
        Layer* layer = [self findLayerByPath:path];
        if (layer.dataset == nil) {
            LayerGroup* layerGroup = (LayerGroup *)layer;
            for (int i = 0; i < [layerGroup getCount]; i++) {
                Layer* mLayer = [layerGroup getLayer:i];
                NSMutableDictionary* info = [self getLayerInfo:mLayer path:path];
                [arr addObject:info];
            }
        }
    }
    
    return arr;
}

+ (NSArray *)getLayersByGroupPath:(NSString *)path{
    Map* map = [SMap singletonInstance].smMapWC.mapControl.map;
    Layers* layers = map.layers;
    LayerGroup* layerGroup = nil;
    Layer* layer;
    NSMutableArray* arr = [[NSMutableArray alloc] init];
    NSArray* pathParams = [path componentsSeparatedByString:@"/"];
    if (path == nil || [path isEqualToString:@""] || pathParams.count == 1) {
        layer = [layers getLayerWithName:pathParams[0]];
    } else {
        layer = [self findLayerByPath:path];
    }
    if (layer.dataset == nil) {
        layerGroup = (LayerGroup *)layer;
    } else {
        return arr;
    }
    for (int i = 0; i < [layerGroup getCount]; i++) {
        Layer* layer = [layerGroup getLayer:i];
        NSMutableDictionary* info = [self getLayerInfo:layer path:path];
        [arr addObject:info];
    }
    
    return arr;
}

+ (NSMutableDictionary *)getLayerInfo:(Layer *)layer path:(NSString *)path {
    int themeType = (int)layer.theme.themeType;
    
    NSString* datasetName = @"";
    if (layer.dataset != nil) {
        datasetName = layer.dataset.name;
    }
    
    NSString* mLayerGroupName = layer.parentGroup.name;
    
    NSMutableDictionary* dictionary = [NSMutableDictionary dictionary];
    [dictionary setValue:layer.name forKey:@"name"];
    [dictionary setValue:layer.caption forKey:@"caption"];
    [dictionary setValue:layer.description forKey:@"description"];
    [dictionary setValue:[NSNumber numberWithBool:layer.editable] forKey:@"isEditable"];
    [dictionary setValue:[NSNumber numberWithBool:layer.visible] forKey:@"isVisible"];
    [dictionary setValue:[NSNumber numberWithBool:layer.selectable] forKey:@"isSelectable"];
    [dictionary setValue:[NSNumber numberWithBool:layer.isSnapable] forKey:@"isSnapable"];
    [dictionary setValue:mLayerGroupName forKey:@"groupName"];
    path = [path isEqualToString:@""] ? layer.name : [NSString stringWithFormat:@"%@/%@", path, layer.name];
    [dictionary setValue:path forKey:@"path"];
    [dictionary setValue:@(themeType) forKey:@"themeType"];
    
    if (layer.dataset != nil) {
        [dictionary setValue:[NSNumber numberWithInteger:layer.dataset.datasetType] forKey:@"type"];
        [dictionary setValue:datasetName forKey:@"datasetName"];
    } else {
        [dictionary setValue:@"layerGroup" forKey:@"type"];
    }
    return dictionary;
}

+ (void)setLayerVisible:(NSString *)path value:(BOOL)value {
    Layer* layer = [self findLayerByPath:path];
    layer.visible = value;
}

+ (void)setLayerEditable:(NSString *)path value:(BOOL)value {
    Layer* layer = [self findLayerByPath:path];
    layer.editable = value;
}

+ (Layer *)findLayerByPath:(NSString *)path {
    if (path == nil || [path isEqualToString:@""]) return nil;
    Map* map = [SMap singletonInstance].smMapWC.mapControl.map;
    Layers* layers = map.layers;
    
    NSArray* pathParams = [path componentsSeparatedByString:@"/"];
    Layer* layer;
    LayerGroup* layerGroup;
    layer = [layers getLayerWithName:pathParams[0]];
    for (int i = 1; i < pathParams.count; i++) {
        if (layer.dataset == nil) {
            layerGroup = (LayerGroup *)layer;
            layer = [layerGroup getLayerWithName:pathParams[i]];
        } else {
            break;
        }
    }
    return layer;
}

+ (NSDictionary *)getLayerAttribute:(NSString *)path page:(int)page size:(int)size {
    Layer* layer = [self findLayerByPath:path];
    DatasetVector* dv = (DatasetVector *)layer.dataset;
    
    Recordset* recordSet = [dv recordset:false cursorType:STATIC];
    long nCount = recordSet.recordCount > size ? size : recordSet.recordCount;
    NSMutableDictionary* dic = [NativeUtil recordsetToJsonArray:recordSet page:page size:nCount];
    [recordSet dispose];
    return dic;
}

+ (NSDictionary *)getSelectionAttributeByLayer:(NSString *)path page:(int)page size:(int)size {
    Layer* layer = [self findLayerByPath:path];
    Selection* selection = [layer getSelection];
    Recordset* recordSet = selection.toRecordset;
    
    [recordSet moveFirst];
    long nCount = recordSet.recordCount > size ? size : recordSet.recordCount;
    NSMutableDictionary* dic = [NativeUtil recordsetToJsonArray:recordSet page:page size:nCount]; // recordSet已经dispose了
    
    [recordSet dispose];
    [selection dispose];
    recordSet = nil;
    return dic;
}

+ (NSDictionary *)getAttributeByLayer:(NSString *)path ids:(NSArray *)ids {
    Layer* layer = [self findLayerByPath:path];
    NSString* filter = @"";
    for (int i = 0; i < ids.count; i++) {
        NSNumber* ID = ids[i];
        if (i == 0) {
            filter = [NSString stringWithFormat:@"SmID=%d", ID.intValue];
        } else {
            filter = [NSString stringWithFormat:@"%@ OR SmID=%d", filter, ID.intValue];
        }
    }
    QueryParameter* qp = [[QueryParameter alloc] init];
    [qp setAttriButeFilter:filter];
    [qp setCursorType:STATIC];
    
    DatasetVector* dv = (DatasetVector *)layer.dataset;;
    Recordset* recordSet = [dv query:qp];
    
    [recordSet moveFirst];
    NSMutableDictionary* dic = [NativeUtil recordsetToJsonArray:recordSet page:0 size:recordSet.recordCount]; // recordSet已经dispose了
    
    [recordSet dispose];
    recordSet = nil;
    return dic;
}

+ (NSString *)getLayerPath:(Layer *)layer {
    NSString* path = layer.name;
    while (layer.parentGroup != nil) {
        path = [NSString stringWithFormat:@"%@/%@", layer.parentGroup, path];
    }
    return path;
}

+ (Layer *)findLayerByDatasetName:(NSString *)datasetName {
    if (datasetName == nil || [datasetName isEqualToString:@""]) return nil;
    Map* map = [SMap singletonInstance].smMapWC.mapControl.map;
    Layers* layers = map.layers;
    Layer* targetLayer = nil;
    int count = layers.getCount;
    for (int i = 0; i < count; i++) {
        Layer* layer = [layers getLayerAtIndex:i];
        Dataset* dataset = layer.dataset;
        
        if (dataset.name == datasetName) {
            targetLayer = layer;
            break;
        }
    }
    
    return targetLayer;
}

+ (NSArray *)searchLayerAttribute:(NSString *)path params:(NSDictionary *)params page:(int *)page size:(int *)size {
    NSString* filter = [params objectForKey:@"filter"];
    NSString* key = [params objectForKey:@"key"];
    
    Layer* layer = [self findLayerByPath:path];
    DatasetVector* dv = (DatasetVector*) layer.dataset;
    
    QueryParameter* qp = [[QueryParameter alloc] init];
    Recordset* recordset;
    
    if (filter != nil && ![filter isEqualToString:@""]) {
        qp.attriButeFilter = filter;
        qp.cursorType = STATIC;
        recordset = [dv query:qp];
    } else if (key != nil && ![key isEqualToString:@""]) {
        FieldInfos* infos = dv.fieldInfos;
        NSString* sql = @"";
        for (int i = 0; i < infos.count; i++) {
            NSString* fieldName = [infos get:i].name;
            if (i == 0) {
                sql = [NSString stringWithFormat:@"%@ LIKE '%%%@%%'", fieldName, key];
            } else {
                sql = [NSString stringWithFormat:@"%@ OR %@ LIKE '%%%@%%'", sql, fieldName, key];
            }
        }
        qp.attriButeFilter = sql;
        qp.cursorType = STATIC;
        recordset = [dv query:qp];
    } else {
        recordset = [dv recordset:NO cursorType:STATIC];
    }
    
    [recordset moveFirst];
    NSArray* arr = [NativeUtil recordsetToJsonArray:recordset page:page size:size]; // recordSet已经dispose了
    
    [recordset dispose];
    recordset = nil;
    return arr;
}

+ (NSMutableArray *)searchSelectionAttribute:(NSString *)path searchKey:(NSString *)searchKey page:(int)page size:(int)size {
    Layer* layer = [self findLayerByPath:path];
    Selection* selection = [layer getSelection];
    Recordset* recordSet = selection.toRecordset;
    
    [recordSet moveFirst];
    long nCount = recordSet.recordCount > size ? size : recordSet.recordCount;
    NSMutableArray* arr = [NativeUtil recordsetToJsonArray:recordSet page:page size:nCount filterKey:searchKey]; // recordSet已经dispose了
    
    [recordSet dispose];
    [selection dispose];
    recordSet = nil;
    return arr;
}

@end
