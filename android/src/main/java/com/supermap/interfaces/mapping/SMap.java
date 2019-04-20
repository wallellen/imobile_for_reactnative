/**
 *
 */
package com.supermap.interfaces.mapping;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.supermap.containts.EventConst;
import com.supermap.data.*;
import com.supermap.data.Enum;
import com.supermap.data.Maps;
import com.supermap.data.Point;
import com.supermap.data.Point2D;
import com.supermap.data.Point2Ds;
import com.supermap.data.PrjCoordSys;
import com.supermap.data.PrjCoordSysType;
import com.supermap.data.Resources;
import com.supermap.data.Workspace;
import com.supermap.mapping.Action;
import com.supermap.mapping.ColorLegendItem;
import com.supermap.mapping.CallOut;
import com.supermap.mapping.CalloutAlignment;
import com.supermap.mapping.GeometryAddedListener;
import com.supermap.mapping.GeometryEvent;
import com.supermap.mapping.GeometrySelectedEvent;
import com.supermap.mapping.GeometrySelectedListener;
import com.supermap.mapping.Layer;
import com.supermap.mapping.LayerGroup;
import com.supermap.mapping.LayerSettingVector;
import com.supermap.mapping.Layers;
import com.supermap.mapping.Legend;
import com.supermap.mapping.LegendItem;
import com.supermap.mapping.LegendView;
import com.supermap.mapping.MapControl;
import com.supermap.mapping.MeasureListener;
import com.supermap.mapping.Selection;
import com.supermap.mapping.Theme;
import com.supermap.mapping.ThemeGridRange;
import com.supermap.mapping.ThemeRange;
import com.supermap.mapping.ThemeType;
import com.supermap.mapping.ThemeUnique;
import com.supermap.mapping.collector.Collector;
import com.supermap.smNative.SMLayer;
import com.supermap.smNative.SMMapWC;
import com.supermap.smNative.SMSymbol;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class SMap extends ReactContextBaseJavaModule {
    public static final String REACT_CLASS = "SMap";
    private static SMap sMap;
    private static ReactApplicationContext context;
    private static MeasureListener mMeasureListener;
    private GestureDetector mGestureDetector;
    private GeometrySelectedListener mGeometrySelectedListener;
    public static int fillNum;
    public static Color[] fillColors;
    public static Random random;// 用于保存产生随机的线风格颜色的Random对象
    String rootPath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();


    public Selection getSelection() {
        return selection;
    }

    public void setSelection(Selection selection) {
        this.selection = selection;
    }

    private Selection selection;

    public SMMapWC getSmMapWC() {
        return smMapWC;
    }

    public void setSmMapWC(SMMapWC smMapWC) {
        this.smMapWC = smMapWC;
    }

    private SMMapWC smMapWC;

    private Point2D defaultMapCenter = null;

    public SMap(ReactApplicationContext context) {
        super(context);
        this.context = context;
    }

    @Override
    public String getName() {
        return REACT_CLASS;
    }

    public static SMap getInstance() {
        if (sMap == null) {
            sMap = new SMap(context);
        }
        if (sMap.smMapWC == null) {
            sMap.smMapWC = new SMMapWC();
        }
        setWorkspace(null);
        return sMap;
    }

    public static SMap getInstance(ReactApplicationContext context) {
        if (sMap == null) {
            sMap = new SMap(context);
        }
        if (sMap.smMapWC == null) {
            sMap.smMapWC = new SMMapWC();
        }
        setWorkspace(null);
        return sMap;
    }

    public static void setInstance(MapControl mapControl) {
        sMap = getInstance();
        sMap.smMapWC.setMapControl(mapControl);
        setWorkspace(null);
    }

    public static void setWorkspace(Workspace workspace) {
        if (sMap.smMapWC.getWorkspace() == null) {
            if (workspace == null) {
                Workspace _workspace = new Workspace();
                sMap.smMapWC.setWorkspace(_workspace);
            } else {
                sMap.smMapWC.setWorkspace(workspace);
            }
        }
    }

    public static SMMapWC getSMWorkspace() {
        return getInstance().smMapWC;
    }


    /**
     * 获取许可文件状态
     *
     * @param promise
     */
    @ReactMethod
    public void getEnvironmentStatus(Promise promise) {
        try {
            LicenseStatus status = Environment.getLicenseStatus();
            WritableMap statusMap = Arguments.createMap();
            statusMap.putBoolean("isActivated", status.isActivated());
            statusMap.putBoolean("isLicenseValid", status.isLicenseValid());
            statusMap.putBoolean("isLicenseExist", status.isLicenseExsit());
            statusMap.putBoolean("isTrailLicense", status.isTrailLicense());
            statusMap.putString("startDate", status.getStartDate().toString());
            statusMap.putString("expireDate", status.getExpireDate().toString());
            statusMap.putString("version", status.getVersion() + "");
            promise.resolve(statusMap);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 添加marker
     * @param longitude
     * @param latitude
     * @param promise
     */
    @ReactMethod
    public void showMarker(double longitude, double latitude, Promise promise) {
        try {
            sMap = getInstance();
            sMap.smMapWC.getMapControl().getMap().refresh();

            Point2D pt = new Point2D(longitude,latitude);
            if (sMap.smMapWC.getMapControl().getMap().getPrjCoordSys().getType() != PrjCoordSysType.PCS_EARTH_LONGITUDE_LATITUDE) {
                Point2Ds point2Ds = new Point2Ds();
                point2Ds.add(pt);
                PrjCoordSys prjCoordSys = new PrjCoordSys();
                prjCoordSys.setType(PrjCoordSysType.PCS_EARTH_LONGITUDE_LATITUDE);
                CoordSysTransParameter parameter = new CoordSysTransParameter();

                CoordSysTranslator.convert(point2Ds, prjCoordSys, sMap.smMapWC.getMapControl().getMap().getPrjCoordSys(), parameter, CoordSysTransMethod.MTH_GEOCENTRIC_TRANSLATION);
                pt = point2Ds.getItem(0);
            }
            final  Point2D mapPt = pt;//new Point2D(11584575.605042318,3573118.555091877);
            getCurrentActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    final  Point2D mapPt = new Point2D(11584575.605042318,3573118.555091877);
                    GeoPoint point = new GeoPoint(mapPt.getX(),mapPt.getY());
                    GeoStyle style = new GeoStyle();
                    style.setMarkerSymbolID(1);
                    style.setMarkerSize(new Size2D(25,25));
                    style.setLineColor(new Color(255,0,0,255));
                    point.setStyle(style);
                    sMap.smMapWC.getMapControl().getMap().getTrackingLayer().add(point,"marker_###");

                    sMap.smMapWC.getMapControl().getMap().setCenter(mapPt);
                    sMap.smMapWC.getMapControl().getMap().setScale(0.000011947150294723098);
                    sMap.smMapWC.getMapControl().getMap().refresh();
                }
            });
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }
    /**
     * 移除marker
     *

     * @param promise
     */
    @ReactMethod
    public void deleteMarker(Promise promise) {
        try {
            sMap.smMapWC.getMapControl().getMap().getTrackingLayer().removeLabel("marker_###");
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }


    /**
     * 刷新地图
     *
     * @param promise
     */
    @ReactMethod
    public void refreshMap(Promise promise) {
        try {
            sMap = getInstance();
            sMap.smMapWC.getMapControl().getMap().refresh();

            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 打开工作空间
     *
     * @param data
     * @param promise
     */
    @ReactMethod
    public void openWorkspace(ReadableMap data, Promise promise) {
        try {
            sMap = getInstance();
            Map params = data.toHashMap();
            boolean result = sMap.smMapWC.openWorkspace(params);
            if (result) {
                sMap.smMapWC.getMapControl().getMap().setWorkspace(sMap.smMapWC.getWorkspace());
            }
            sMap.smMapWC.getMapControl().getMap().setVisibleScalesEnabled(false);
            sMap.smMapWC.getMapControl().setMagnifierEnabled(true);
            sMap.smMapWC.getMapControl().getMap().setAntialias(true);
            sMap.smMapWC.getMapControl().getMap().refresh();

            promise.resolve(result);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 以数据源形式打开工作空间setLayerFieldInfo
     *
     * @param data
     * @param defaultIndex 默认显示Map 图层索引
     * @param promise
     */
    @ReactMethod
    public void openDatasourceWithIndex(ReadableMap data, int defaultIndex, boolean toHead, boolean visable, Promise promise) {
        try {
            sMap = getInstance();
            Map params = data.toHashMap();
            Datasource datasource = sMap.smMapWC.openDatasource(params);
            sMap.smMapWC.getMapControl().getMap().setWorkspace(sMap.smMapWC.getWorkspace());

            if (datasource != null && defaultIndex >= 0 && datasource.getDatasets().getCount() > 0) {
                Dataset ds = datasource.getDatasets().get(defaultIndex);
                com.supermap.mapping.Map map = sMap.smMapWC.getMapControl().getMap();
                map.setDynamicProjection(true);
                Layer layer = map.getLayers().add(ds, toHead);
                layer.setVisible(visable);
                if (ds.getType() == DatasetType.REGION) {
                    LayerSettingVector setting = (LayerSettingVector) layer.getAdditionalSetting();
                    setting.getStyle().setLineSymbolID(5);
                }
                if (ds.getType() == DatasetType.REGION || ds.getType() == DatasetType.REGION3D) {
                    LayerSettingVector setting = (LayerSettingVector) layer.getAdditionalSetting();
                    setting.getStyle().setFillForeColor(this.getFillColor());
                    setting.getStyle().setLineColor(this.getLineColor());
                } else if (ds.getType() == DatasetType.LINE || ds.getType() == DatasetType.NETWORK || ds.getType() == DatasetType.NETWORK3D
                        || ds.getType() == DatasetType.LINE3D) {
                    LayerSettingVector setting = (LayerSettingVector) layer.getAdditionalSetting();
                    setting.getStyle().setLineColor(this.getLineColor());
                    if (ds.getType() == DatasetType.NETWORK || ds.getType() == DatasetType.NETWORK3D) {
                        map.getLayers().add(((DatasetVector) ds).getChildDataset(), true);
                    }
                } else if (ds.getType() == DatasetType.POINT || ds.getType() == DatasetType.POINT3D) {
                    LayerSettingVector setting = (LayerSettingVector) layer.getAdditionalSetting();
                    setting.getStyle().setLineColor(this.getLineColor());
                }
            }

            sMap.smMapWC.getMapControl().getMap().setVisibleScalesEnabled(false);
            sMap.smMapWC.getMapControl().getMap().refresh();

            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 以数据源形式打开工作空间
     *
     * @param data
     * @param defaultName 默认显示Map 图层名称
     * @param promise
     */
    @ReactMethod
    public void openDatasourceWithName(ReadableMap data, String defaultName, boolean toHead, boolean visable, Promise promise) {
        try {
            sMap = getInstance();
            Map params = data.toHashMap();
            Datasource datasource = sMap.smMapWC.openDatasource(params);
            sMap.smMapWC.getMapControl().getMap().setWorkspace(sMap.smMapWC.getWorkspace());

            if (datasource != null && !defaultName.equals("")) {
                Dataset ds = datasource.getDatasets().get(defaultName);
                sMap.smMapWC.getMapControl().getMap().setDynamicProjection(true);
                Layer layer = sMap.smMapWC.getMapControl().getMap().getLayers().add(ds, toHead);
                layer.setVisible(visable);
            }
            sMap.smMapWC.getMapControl().getMap().refresh();

            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 不同于上次选用的填充颜色的颜色
     *
     * @return
     */
    public static Color getFillColor() {

        Color result = new Color(255, 192, 203);
        if (fillNum >= getFillColors().length) {
            fillNum = 0;
        }
        result = getFillColors()[fillNum];
        fillNum++;
        return result;

    }

    /**
     * 获取随机的用于线风格的颜色
     *
     * @return
     */
    public static Color getLineColor() {
        return getRandomLineColor();
    }

    /**
     * 产生随机的用于线风格的颜色 经过初步试验，新产生的线颜色，饱和度【0-240】最好在30-100之间 亮度【0-240】最好在75-120之间
     *
     * @return
     */
    private static Color getRandomLineColor() {
        Color result = new Color(255, 192, 203);
        try {
            if (random == null) {
                random = new Random();
            }
            result = new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255));
        } catch (Exception ex) {
        }
        return result;
    }

    private static Color[] getFillColors() {
        if (fillColors == null) {
            fillColors = new Color[10];
            fillColors[0] = new Color(224, 207, 226);
            fillColors[1] = new Color(151, 191, 242);
            fillColors[2] = new Color(242, 242, 186);
            fillColors[3] = new Color(190, 255, 232);
            fillColors[4] = new Color(255, 190, 232);
            fillColors[5] = new Color(255, 190, 190);
            fillColors[6] = new Color(255, 235, 175);
            fillColors[7] = new Color(233, 255, 190);
            fillColors[8] = new Color(234, 225, 168);
            fillColors[9] = new Color(174, 241, 176);
        }
        return fillColors;
    }

    /**
     * 根据名称关闭数据源，datasourceName为空则全部关闭
     *
     * @param datasourceName
     * @param promise
     */
    @ReactMethod
    public void closeDatasourceWithName(String datasourceName, Promise promise) {
        try {
            sMap = getInstance();
            Datasources datasources = sMap.smMapWC.getWorkspace().getDatasources();
            Boolean isClose = true;
            if (datasourceName.equals("")) {
                for (int i = 0; i < datasources.getCount(); i++) {
                    if (datasources.get(i) != null && datasources.get(i).isOpened()) {
                        isClose = datasources.close(i) && isClose;
                    }
                }
            } else {
                if (datasources.get(datasourceName) != null) {
                    isClose = datasources.close(datasourceName);
                }
            }

            promise.resolve(isClose);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 根据序号关闭数据源，index = -1 则全部关闭
     *
     * @param index
     * @param promise
     */
    @ReactMethod
    public void closeDatasourceWithIndex(int index, Promise promise) {
        try {
            sMap = getInstance();
            Datasources datasources = sMap.smMapWC.getWorkspace().getDatasources();
            Boolean isClose = true;
            if (index == -1) {
                for (int i = 0; i < datasources.getCount(); i++) {
                    if (datasources.get(i) != null && datasources.get(i).isOpened()) {
                        isClose = datasources.close(i) && isClose;
                    }
                }
            } else {
                if (datasources.get(index) != null) {
                    isClose = datasources.close(index);
                }
            }

            promise.resolve(isClose);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 工作空间是否被修改
     *
     * @param promise
     */
    @ReactMethod
    public void workspaceIsModified(Promise promise) {
        try {
            sMap = getInstance();
            boolean result = sMap.smMapWC.getWorkspace().isModified();
            promise.resolve(result);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 保存工作空间
     *
     * @param promise
     */
    @ReactMethod
    public void saveWorkspace(Promise promise) {
        try {
            sMap = getInstance();
            boolean result = sMap.smMapWC.saveWorkspace();
            promise.resolve(result);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 根据工作空间连接信息保存工作空间
     *
     * @param data
     * @param promise
     */
    @ReactMethod
    public void saveWorkspaceWithInfo(ReadableMap data, Promise promise) {
        try {
            sMap = getInstance();
            Map info = data.toHashMap();
            boolean result = sMap.smMapWC.saveWorkspaceWithInfo(info);
            promise.resolve(result);
        } catch (Exception e) {
            promise.reject(e);
        }
    }


    /**
     * 获取UDB中数据集名称
     *
     * @param path    UDB在内存中路径
     * @param promise
     */
    @ReactMethod
    public void getUDBName(String path, Promise promise) {
        try {
            File tempFile = new File(path.trim());
            String[] strings = tempFile.getName().split("\\.");
            String udbName = strings[0];
            Datasource datasource;
            Workspace workspace = null;
            sMap = getInstance();
            DatasourceConnectionInfo datasourceconnection = new DatasourceConnectionInfo();

//            if (sMap.smMapWC.getMapControl().getMap().getWorkspace().getDatasources().indexOf(udbName) != -1) {
//                sMap.smMapWC.getMapControl().getMap().getWorkspace().getDatasources().close(udbName);
//            }
            if (sMap.smMapWC.getMapControl() == null) {
                workspace = new Workspace();
                datasourceconnection.setEngineType(EngineType.UDB);
                datasourceconnection.setServer(path);
                datasourceconnection.setAlias(udbName);
                datasource = workspace.getDatasources().open(datasourceconnection);
            } else {
                sMap.smMapWC.getMapControl().getMap().setWorkspace(sMap.smMapWC.getWorkspace());
                if (sMap.smMapWC.getMapControl().getMap().getWorkspace().getDatasources().indexOf(udbName) != -1) {
                    datasource = sMap.smMapWC.getMapControl().getMap().getWorkspace().getDatasources().get(udbName);
                } else {
                    datasourceconnection.setEngineType(EngineType.UDB);
                    datasourceconnection.setServer(path);
                    datasourceconnection.setAlias(udbName);
                    datasource = sMap.smMapWC.getMapControl().getMap().getWorkspace().getDatasources().open(datasourceconnection);
                }
            }
            Datasets datasets = datasource.getDatasets();
            int count = datasets.getCount();

            WritableArray arr = Arguments.createArray();
            for (int i = 0; i < count; i++) {
                Dataset dataset = datasets.get(i);
                String name = dataset.getName();
                WritableMap writeMap = Arguments.createMap();
                writeMap.putString("title", name);
                arr.pushMap(writeMap);
            }
            if (workspace != null) {
                workspace.dispose();
            }
            datasourceconnection.dispose();
            promise.resolve(arr);
        } catch (Exception e) {
            promise.reject(e);
        }
    }


    /**
     * 根据名字显示图层
     *
     * @param name
     * @param viewEntire
     * @param center
     * @param promise
     */
    @ReactMethod
    public void openMapByName(String name, boolean viewEntire, ReadableMap center, Promise promise) {
        try {
            sMap = getInstance();
            com.supermap.mapping.Map map = sMap.smMapWC.getMapControl().getMap();
            Maps maps = sMap.smMapWC.getWorkspace().getMaps();

            Boolean isOpen = false;

            if (maps.getCount() > 0) {
                String mapName = name;

                if (name.equals("")) {
                    mapName = maps.get(0);
                }

                isOpen = map.open(mapName);

                if (isOpen) {
                    if (viewEntire) {
                        map.viewEntire();
                    }

                    if (center != null && center.hasKey("x") && center.hasKey("y")) {
                        Double x = center.getDouble("x");
                        Double y = center.getDouble("y");
                        Point2D point2D = new Point2D(x, y);
                        map.setCenter(point2D);
                    }

                    defaultMapCenter = new Point2D(map.getCenter());
                    sMap.smMapWC.getMapControl().setAction(Action.PAN);
                    map.setVisibleScalesEnabled(false);
                    map.refresh();
                }
            }

            promise.resolve(isOpen);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 根据序号显示图层
     *
     * @param index
     * @param viewEntire
     * @param center
     * @param promise
     */
    @ReactMethod
    public void openMapByIndex(int index, boolean viewEntire, ReadableMap center, Promise promise) {
        try {
            sMap = getInstance();
            com.supermap.mapping.Map map = sMap.smMapWC.getMapControl().getMap();
            Maps maps = sMap.smMapWC.getWorkspace().getMaps();

            Boolean isOpen = index < 0;

            if (maps.getCount() > 0 && index >= 0) {
                if (index >= maps.getCount()) index = maps.getCount() - 1;
                String name = maps.get(index);

                isOpen = map.open(name);

                if (isOpen) {
                    if (viewEntire) {
                        map.viewEntire();
                    }

                    if (center != null && center.hasKey("x") && center.hasKey("y")) {
                        Double x = center.getDouble("x");
                        Double y = center.getDouble("y");
                        Point2D point2D = new Point2D(x, y);
                        map.setCenter(point2D);
                    }
                    defaultMapCenter = new Point2D(map.getCenter());
                    sMap.smMapWC.getMapControl().setAction(Action.PAN);
                    map.setVisibleScalesEnabled(false);
                    map.refresh();
                }
            }
            promise.resolve(isOpen);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 获取工作空间地图列表
     *
     * @param promise
     */
    @ReactMethod
    public void getMaps(Promise promise) {
        try {
            sMap = getInstance();
            Maps maps = sMap.smMapWC.getWorkspace().getMaps();
            WritableArray mapList = Arguments.createArray();
            for (int i = 0; i < maps.getCount(); i++) {
                WritableMap mapInfo = Arguments.createMap();
                String mapName = maps.get(i);
                mapInfo.putString("title", mapName);
                mapList.pushMap(mapInfo);
            }
            promise.resolve(mapList);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 获取工作空间地图列表
     *
     * @param promise
     */
    @ReactMethod
    public void getMapInfo(Promise promise) {
        try {
            sMap = getInstance();
            com.supermap.mapping.Map map = sMap.smMapWC.getMapControl().getMap();

            WritableMap mapInfo = Arguments.createMap();
            mapInfo.putString("name", map.getName());
            mapInfo.putString("description", map.getDescription());
            mapInfo.putBoolean("isModified", map.isModified());

            promise.resolve(mapInfo);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 关闭工作空间及地图控件
     *
     * @param promise
     */
    @ReactMethod
    public void closeWorkspace(Promise promise) {
        try {
//            getCurrentActivity().runOnUiThread(new DisposeThread(promise));
            sMap = getInstance();
            MapControl mapControl = sMap.smMapWC.getMapControl();
            Workspace workspace = sMap.smMapWC.getWorkspace();
            com.supermap.mapping.Map map = mapControl.getMap();
            defaultMapCenter = null;
            map.close();
            map.dispose();
//                mapControl.dispose();
            workspace.close();
//            workspace.dispose();

//            sMap.smMapWC.setMapControl(null);
//            sMap.smMapWC.setWorkspace(null);
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 关闭地图
     *
     * @param promise
     */
    @ReactMethod
    public void closeMap(Promise promise) {
        try {
            sMap = getInstance();
            MapControl mapControl = sMap.smMapWC.getMapControl();
            if (mapControl != null) {
                com.supermap.mapping.Map map = mapControl.getMap();
                defaultMapCenter = null;
                map.close();
            }
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 设置Selection样式
     *
     * @param layerPath
     * @param styleJson
     * @param promise
     */
    @ReactMethod
    public void setSelectionStyle(String layerPath, String styleJson, Promise promise) {
        try {
            sMap = getInstance();
            MapControl mapControl = sMap.smMapWC.getMapControl();

            Layer layer = SMLayer.findLayerByPath(layerPath);
            Selection selection = layer.getSelection();
            GeoStyle style = new GeoStyle();
            style.fromJson(styleJson);
            selection.setStyle(style);

            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 清除Selection
     *
     * @param promise
     */
    @ReactMethod
    public void clearSelection(Promise promise) {
        try {
            sMap = getInstance();
            MapControl mapControl = sMap.smMapWC.getMapControl();

            Layers layers = mapControl.getMap().getLayers();
            for (int i = 0; i < layers.getCount(); i++) {
                Selection selection = layers.get(i).getSelection();
                if (selection != null) {
                    selection.clear();
                }

                mapControl.getMap().refresh();
            }
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    class DisposeThread implements Runnable {

        private Promise promise;

        public DisposeThread(Promise promise) {
            this.promise = promise;
        }

        @Override
        public void run() {
            try {
                sMap = getInstance();
                MapControl mapControl = sMap.smMapWC.getMapControl();
                Workspace workspace = sMap.smMapWC.getWorkspace();
                com.supermap.mapping.Map map = mapControl.getMap();
                mapControl.getEditHistory().dispose();

                map.close();
                map.dispose();
//                mapControl.dispose();
                workspace.close();
                workspace.dispose();

//                sMap.smMapWC.setMapControl(null);
                sMap.smMapWC.setWorkspace(null);
                promise.resolve(true);
            } catch (Exception e) {
                promise.resolve(e);
            }
        }
    }

    @ReactMethod
    public void setAction(int actionType, Promise promise) {
        try {
            sMap = getInstance();
            Action action = (Action) Enum.parse(Action.class, actionType);
            sMap.smMapWC.getMapControl().setAction(action);

            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    @ReactMethod
    public void undo(Promise promise) {
        try {
            sMap = getInstance();
            sMap.smMapWC.getMapControl().undo();
            sMap.smMapWC.getMapControl().getMap().refresh();

            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    @ReactMethod
    public void redo(Promise promise) {
        try {
            sMap = getInstance();
            sMap.smMapWC.getMapControl().redo();
            sMap.smMapWC.getMapControl().getMap().refresh();

            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    @ReactMethod
    public void addMeasureListener(Promise promise) {
        try {
            sMap = getInstance();
            mMeasureListener = new MeasureListener() {
                @Override
                public void lengthMeasured(double curResult, Point curPoint) {
                    WritableMap map = Arguments.createMap();
                    map.putDouble("curResult", curResult);
                    WritableMap point = Arguments.createMap();
                    point.putDouble("x", curPoint.getX());
                    point.putDouble("y", curPoint.getY());
                    map.putMap("curPoint", point);

                    context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit(EventConst.MEASURE_LENGTH, map);
                }

                @Override
                public void areaMeasured(double curResult, Point curPoint) {
                    WritableMap map = Arguments.createMap();
                    map.putDouble("curResult", curResult);
                    WritableMap point = Arguments.createMap();
                    point.putDouble("x", curPoint.getX());
                    point.putDouble("y", curPoint.getY());
                    map.putMap("curPoint", point);

                    context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit(EventConst.MEASURE_AREA, map);
                }

                @Override
                public void angleMeasured(double curAngle, Point curPoint) {
                    WritableMap map = Arguments.createMap();
                    map.putDouble("curAngle", curAngle);
                    WritableMap point = Arguments.createMap();
                    point.putDouble("x", curPoint.getX());
                    point.putDouble("y", curPoint.getY());
                    map.putMap("curPoint", point);

                    context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit(EventConst.MEASURE_ANGLE, map);
                }
            };

            sMap.smMapWC.getMapControl().addMeasureListener(mMeasureListener);
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    @ReactMethod
    public void removeMeasureListener(Promise promise) {
        try {
            sMap = getInstance();
            sMap.smMapWC.getMapControl().removeMeasureListener(mMeasureListener);
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /******************************************** 地图工具 *****************************************************/
    /**
     * 放大缩小
     *
     * @param scale
     * @param promise
     */
    @ReactMethod
    public void zoom(double scale, Promise promise) {
        try {
            sMap = getInstance();
            com.supermap.mapping.Map map = sMap.smMapWC.getMapControl().getMap();
            map.zoom(scale);
            map.refresh();
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 设置比例尺
     *
     * @param scale
     * @param promise
     */
    @ReactMethod
    public void setScale(double scale, Promise promise) {
        try {
            sMap = getInstance();
            com.supermap.mapping.Map map = sMap.smMapWC.getMapControl().getMap();
            map.setScale(scale);
            map.refresh();
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 设置地图手势旋转是否可用
     *
     * @param enable
     * @param promise
     */
    @ReactMethod
    public void enableRotateTouch(boolean enable, Promise promise) {
        try {
            sMap = getInstance();
            sMap.smMapWC.getMapControl().enableRotateTouch(enable);
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 设置地图手势俯仰是否可用
     *
     * @param enable
     * @param promise
     */
    @ReactMethod
    public void enableSlantTouch(boolean enable, Promise promise) {
        try {
            sMap = getInstance();
            sMap.smMapWC.getMapControl().enableSlantTouch(enable);
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }


    /**
     * 地图裁剪
     *
     * @param points
     * @param layersInfo
     * @param mapName
     * @param nModule
     * @param addition
     * @param isPrivate
     * @param promise
     */
    @ReactMethod
    public void clipMap(ReadableArray points, ReadableArray layersInfo, String mapName, String nModule, ReadableMap addition, boolean isPrivate, Promise promise) {
        try {
            if (points.size() == 0) {
                promise.reject("points can not be empty!");
            } else {
                sMap = getInstance();

                Point2Ds point2Ds = new Point2Ds();
                for (int i = 0; i < points.size(); i++) {
                    ReadableMap p = points.getMap(i);
                    Point point = new Point((int) p.getDouble("x"), (int) p.getDouble("y"));
                    Point2D point2D = sMap.smMapWC.getMapControl().getMap().pixelToMap(point);

                    point2Ds.add(point2D);
                }

                GeoRegion region = new GeoRegion(point2Ds);

                if (mapName.equals("")) {
                    mapName = null;
                }
                String[] args = new String[1];
                args[0] = mapName;
                if (sMap.smMapWC.clipMap(sMap.smMapWC.getMapControl().getMap(), region, layersInfo, args)) {
                    WritableMap writeMap = Arguments.createMap();
                    writeMap.putBoolean("result", true);

                    String resultName = args[0];
                    if (resultName != null && !resultName.equals("")) {
                        Map<String, String> additionMap = new HashMap<>();
                        ReadableMapKeySetIterator keySetIterator = addition.keySetIterator();
                        while (keySetIterator.hasNextKey()) {
                            String key = keySetIterator.nextKey();
                            additionMap.put(key, addition.getString(key));
                        }
                        resultName = sMap.smMapWC.saveMapName(resultName, sMap.smMapWC.getWorkspace(), nModule, additionMap, true, true, isPrivate);
                    }
                    writeMap.putString("mapName", resultName);
                    promise.resolve(writeMap);
                } else {
                    promise.reject(null, "Clip map failed!");
                }

            }
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /******************************************** 地图工具 END*****************************************************/


    @ReactMethod
    public void submit(Promise promise) {
        try {
            sMap = SMap.getInstance();
            sMap.smMapWC.getMapControl().submit();

            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    @ReactMethod
    public void cancel(Promise promise) {
        try {
            sMap = SMap.getInstance();
            sMap.smMapWC.getMapControl().cancel();

            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 保存地图
     *
     * @param name
     * @param autoNaming 为true的话若有相同名字的地图则自动命名
     * @param promise
     */
    @ReactMethod
    public void saveMap(String name, Boolean autoNaming, Promise promise) {
        try {
            sMap = getInstance();
            com.supermap.mapping.Map map = sMap.smMapWC.getMapControl().getMap();
            boolean mapSaved = false;
            boolean wsSaved = false;
            String _name = name;
            if (_name == null || _name.equals("")) {
                if (map.getName() != null && !map.getName().equals("")) {
                    mapSaved = map.save();
                } else if (map.getLayers().getCount() > 0) {
                    _name = map.getLayers().get(0).getName();
                    int i = 0;
                    if (autoNaming) {
                        while (!mapSaved) {
                            _name = i == 0 ? name : (name + i);
                            try {
                                mapSaved = map.save(_name);
                            } catch (Exception e) {
                                mapSaved = false;
                            }
                            i++;
                        }
                    } else {
                        mapSaved = map.save(_name);
                    }
                }
            } else {
                mapSaved = map.save(_name);
            }
            wsSaved = sMap.smMapWC.getWorkspace().save();
//            wsSaved = true;

            if (mapSaved && wsSaved) {
                promise.resolve(_name);
            } else {
                promise.resolve(mapSaved && wsSaved);
            }
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 移除指定位置的地图
     *
     * @param index
     * @param promise
     */
    @ReactMethod
    public void removeMapByIndex(int index, Promise promise) {
        try {
            sMap = getInstance();
            Maps maps = sMap.smMapWC.getWorkspace().getMaps();
            boolean result = false;
            if (maps.getCount() > 0 && index < maps.getCount()) {
                if (index == -1) {
                    for (int i = maps.getCount() - 1; i >= 0; i--) {
                        String name = maps.get(i);
                        result = maps.remove(i) && result;
                        sMap.smMapWC.getWorkspace().getResources().getMarkerLibrary().getRootGroup().getChildGroups().remove(name, false);
                        sMap.smMapWC.getWorkspace().getResources().getLineLibrary().getRootGroup().getChildGroups().remove(name, false);
                        sMap.smMapWC.getWorkspace().getResources().getFillLibrary().getRootGroup().getChildGroups().remove(name, false);
                    }
                } else {
                    String name = maps.get(index);
                    result = maps.remove(index);
                    sMap.smMapWC.getWorkspace().getResources().getMarkerLibrary().getRootGroup().getChildGroups().remove(name, false);
                    sMap.smMapWC.getWorkspace().getResources().getLineLibrary().getRootGroup().getChildGroups().remove(name, false);
                    sMap.smMapWC.getWorkspace().getResources().getFillLibrary().getRootGroup().getChildGroups().remove(name, false);
                }
            }

            promise.resolve(result);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 移除指定名称的地图
     *
     * @param name
     * @param promise
     */
    @ReactMethod
    public void removeMapByName(String name, Promise promise) {
        try {
            sMap = getInstance();
            Maps maps = sMap.smMapWC.getWorkspace().getMaps();
            boolean result = false;
            if (maps.getCount() > 0 && (name == null || name.equals(""))) {
                for (int i = 0; i < maps.getCount(); i++) {
                    String _name = maps.get(i);
                    result = maps.remove(i) && result;
                    sMap.smMapWC.getWorkspace().getResources().getMarkerLibrary().getRootGroup().getChildGroups().remove(_name, false);
                    sMap.smMapWC.getWorkspace().getResources().getLineLibrary().getRootGroup().getChildGroups().remove(_name, false);
                    sMap.smMapWC.getWorkspace().getResources().getFillLibrary().getRootGroup().getChildGroups().remove(_name, false);
                }
            } else if (maps.getCount() > 0 && maps.indexOf(name) >= 0) {
                result = maps.remove(name);
                sMap.smMapWC.getWorkspace().getResources().getMarkerLibrary().getRootGroup().getChildGroups().remove(name, false);
                sMap.smMapWC.getWorkspace().getResources().getLineLibrary().getRootGroup().getChildGroups().remove(name, false);
                sMap.smMapWC.getWorkspace().getResources().getFillLibrary().getRootGroup().getChildGroups().remove(name, false);
            }

            promise.resolve(result);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 地图另存为
     *
     * @param name
     * @param promise
     */
    @ReactMethod
    public void saveAsMap(String name, Promise promise) {
        try {
            sMap = getInstance();
            com.supermap.mapping.Map map = sMap.smMapWC.getMapControl().getMap();
            boolean result = false;
            if (name != null && !name.equals("")) {
                result = map.saveAs(name);
                result = result && sMap.smMapWC.getWorkspace().save();
            }

            promise.resolve(result);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 检查地图是否有改动
     *
     * @param promise
     */
    @ReactMethod
    public void mapIsModified(Promise promise) {
        try {
            sMap = getInstance();
            com.supermap.mapping.Map map = sMap.smMapWC.getMapControl().getMap();
            boolean idModified = map.isModified();

            promise.resolve(idModified);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 检查地图是否有改动
     *
     * @param promise
     */
    @ReactMethod
    public void getMapIndex(String name, Promise promise) {
        try {
            int index = -1;
            sMap = getInstance();
            com.supermap.mapping.Map map = sMap.smMapWC.getMapControl().getMap();
            Maps maps = sMap.smMapWC.getWorkspace().getMaps();

            if (name == null || name.equals("")) {
                if (map != null) {
                    index = maps.indexOf(map.getName());
                }
            } else {
                index = maps.indexOf(name);
            }

            promise.resolve(index);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 移动到当前位置
     *
     * @param promise
     */
    @ReactMethod
    public void moveToCurrent(Promise promise) {
        try {
            MoveToCurrentThread moveToCurrentThread = new MoveToCurrentThread(promise);
            moveToCurrentThread.run();

            sMap.smMapWC.getMapControl().getMap().setAngle(0);
            sMap.smMapWC.getMapControl().getMap().SetSlantAngle(0);
//            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 移动到当前位置
     *
     * @param promise
     */
    @ReactMethod
    public void moveToPoint(ReadableMap point, Promise promise) {
        try {
            if (point.hasKey("x") && point.hasKey("y")) {
                Point2D point2D = new Point2D(point.getDouble("x"), point.getDouble("y"));
                MoveToCurrentThread moveToCurrentThread = new MoveToCurrentThread(point2D, promise);
                moveToCurrentThread.run();
                SMap.getInstance().getSmMapWC().getMapControl().getMap().setAngle(0);
                SMap.getInstance().getSmMapWC().getMapControl().getMap().SetSlantAngle(0);

//                promise.resolve(true);
            } else {
                promise.resolve(false);
            }

        } catch (Exception e) {
            promise.reject(e);
        }
    }

    class MoveToCurrentThread implements Runnable {

        private Promise promise;
        private Point2D point2D;

        public MoveToCurrentThread(Promise promise) {
            this.promise = promise;
        }

        public MoveToCurrentThread(Point2D point2D, Promise promise) {
            this.promise = promise;
            this.point2D = point2D;
        }

        @Override
        public void run() {
            try {
                sMap = getInstance();
                MapControl mapControl = sMap.smMapWC.getMapControl();
                Collector collector = mapControl.getCollector();

                Point2D pt;
                if (this.point2D == null) {
                    collector.openGPS();
                    pt = collector.getGPSPoint();
                } else {
                    pt = this.point2D;
                }

                Boolean isMove = false;
                if (pt != null) {
                    // Point2D point2D = new Point2D(pt);

                    if (pt.getX() <= 180 && pt.getX() >= -180 && pt.getY() >= -90 && pt.getY() <= 90) {
                        if (mapControl.getMap().getPrjCoordSys().getType() != PrjCoordSysType.PCS_EARTH_LONGITUDE_LATITUDE) {
                            Point2Ds point2Ds = new Point2Ds();
                            point2Ds.add(pt);
                            PrjCoordSys prjCoordSys = new PrjCoordSys();
                            prjCoordSys.setType(PrjCoordSysType.PCS_EARTH_LONGITUDE_LATITUDE);
                            CoordSysTransParameter parameter = new CoordSysTransParameter();

                            CoordSysTranslator.convert(point2Ds, prjCoordSys, mapControl.getMap().getPrjCoordSys(), parameter, CoordSysTransMethod.MTH_GEOCENTRIC_TRANSLATION);
                            pt = point2Ds.getItem(0);
                        }
                    } else {
                        if (mapControl.getMap().getPrjCoordSys().getType() != PrjCoordSysType.PCS_SPHERE_MERCATOR) {
                            Point2Ds point2Ds = new Point2Ds();
                            point2Ds.add(pt);
                            PrjCoordSys prjCoordSys = new PrjCoordSys();
                            prjCoordSys.setType(PrjCoordSysType.PCS_SPHERE_MERCATOR);
                            CoordSysTransParameter parameter = new CoordSysTransParameter();

                            CoordSysTranslator.convert(point2Ds, prjCoordSys, mapControl.getMap().getPrjCoordSys(), parameter, CoordSysTransMethod.MTH_GEOCENTRIC_TRANSLATION);
                            pt = point2Ds.getItem(0);
                        }
                    }

//                    if (mapControl.getMap().getPrjCoordSys().getType() != PrjCoordSysType.PCS_EARTH_LONGITUDE_LATITUDE) {
//                        Point2Ds point2Ds = new Point2Ds();
//                        point2Ds.add(pt);
//                        PrjCoordSys prjCoordSys = new PrjCoordSys();
//                        prjCoordSys.setType(PrjCoordSysType.PCS_EARTH_LONGITUDE_LATITUDE);
//                        CoordSysTransParameter parameter = new CoordSysTransParameter();
//
//                        CoordSysTranslator.convert(point2Ds, prjCoordSys, mapControl.getMap().getPrjCoordSys(), parameter, CoordSysTransMethod.MTH_GEOCENTRIC_TRANSLATION);
//                        pt = point2Ds.getItem(0);
//                    }
                }
                if (pt != null && mapControl.getMap().getBounds().contains(pt)) {
                    mapControl.getMap().setCenter(pt);
                    isMove = true;
                } else {
                    if (defaultMapCenter != null) {
                        mapControl.getMap().setCenter(defaultMapCenter);
                    }
                    //  mapControl.panTo(mapControl.getMap().getCenter(), 200);
                }

                mapControl.getMap().refresh();
                promise.resolve(isMove);
            } catch (Exception e) {
                promise.resolve(e);
            }
        }
    }

    /**
     * 监听长按动作和滚动动作
     *
     * @param promise
     */
    @ReactMethod
    public void setGestureDetector(final Promise promise) {
        try {
            mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {

                public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                        float distanceX, float distanceY) {
                    WritableMap mapE1 = Arguments.createMap();
                    mapE1.putInt("x", (int) e1.getX());
                    mapE1.putInt("y", (int) e1.getY());

                    WritableMap mapE2 = Arguments.createMap();
                    mapE2.putInt("x", (int) e2.getX());
                    mapE2.putInt("y", (int) e2.getY());

                    WritableMap map = Arguments.createMap();
                    map.putMap("start", mapE1);
                    map.putMap("end", mapE2);
                    map.putDouble("dx", distanceX);
                    map.putDouble("dy", distanceY);


                    context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit(EventConst.MAP_SCROLL, map);
                    return false;
                }

                public boolean onDown(MotionEvent event) {
                    WritableMap map = Arguments.createMap();
                    map.putInt("x", (int) event.getX());
                    map.putInt("y", (int) event.getY());

                    context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit(EventConst.MAP_TOUCH_BEGAN, map);
                    return false;
                }

                public boolean onSingleTapUp(MotionEvent e) {
                    WritableMap map = Arguments.createMap();
                    map.putInt("x", (int) e.getX());
                    map.putInt("y", (int) e.getY());

                    context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit(EventConst.MAP_SINGLE_TAP, map);
                    return false;
                }

                public void onLongPress(MotionEvent event) {
                    WritableMap map = Arguments.createMap();
                    map.putInt("x", (int) event.getX());
                    map.putInt("y", (int) event.getY());

                    context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit(EventConst.MAP_LONG_PRESS, map);
                }

                public boolean onSingleTapConfirmed(MotionEvent e) {
                    WritableMap map = Arguments.createMap();
                    map.putInt("x", (int) e.getX());
                    map.putInt("y", (int) e.getY());

                    context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit(EventConst.MAP_SINGLE_TAP_CONFIR, map);
                    return false;
                }

                public boolean onDoubleTap(MotionEvent e) {
                    WritableMap map = Arguments.createMap();
                    map.putInt("x", (int) e.getX());
                    map.putInt("y", (int) e.getY());

                    context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit(EventConst.MAP_DOUBLE_TAP, map);
                    return false;
                }
            });
            sMap = getInstance();
            MapControl mapControl = sMap.smMapWC.getMapControl();
            mapControl.setGestureDetector(mGestureDetector);
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }


    @ReactMethod
    public void deleteGestureDetector(Promise promise) {
        try {
            sMap = getInstance();
            MapControl mapControl = sMap.smMapWC.getMapControl();
            mapControl.deleteGestureDetector();

            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    @ReactMethod
    public void addGeometrySelectedListener(Promise promise) {
        try {
            mGeometrySelectedListener = new GeometrySelectedListener() {
                @Override
                public void geometrySelected(GeometrySelectedEvent event) {
                    int id = event.getGeometryID();
                    Layer layer = event.getLayer();

                    WritableMap map = Arguments.createMap();
                    WritableMap layerInfo = Arguments.createMap();
                    layerInfo.putString("name", layer.getName());
                    layerInfo.putString("caption", layer.getCaption());
                    layerInfo.putBoolean("editable", layer.isEditable());
                    layerInfo.putBoolean("visible", layer.isVisible());
                    layerInfo.putBoolean("selectable", layer.isSelectable());
                    layerInfo.putInt("type", layer.getDataset().getType().value());
                    layerInfo.putString("path", SMLayer.getLayerPath(layer));

                    map.putMap("layerInfo", layerInfo);
                    map.putInt("id", id);

                    SMap.getInstance().setSelection(layer.getSelection());

                    context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                            .emit(EventConst.MAP_GEOMETRY_SELECTED, map);
                }

                @Override
                public void geometryMultiSelected(ArrayList<GeometrySelectedEvent> events) {
                    try {
                        ArrayList<Map> arr = new ArrayList();
                        WritableArray array = Arguments.createArray();
                        for (int i = 0; i < events.size(); i++) {
                            GeometrySelectedEvent event = events.get(i);
                            int id = event.getGeometryID();
                            Layer layer = event.getLayer();

                            boolean isExist = false;
                            for (int j = 0; j < arr.size(); j++) {
                                String name = ((WritableMap) arr.get(j).get("layerInfo")).getString("name");
                                if (layer.getName().equals(name)) {
                                    isExist = true;
                                    WritableArray ids = ((WritableArray) arr.get(j).get("ids"));
                                    ids.pushInt(id);
                                }
                            }


                            if (!isExist) {
                                Map<String, Object> layerSelection = new HashMap<>();

//                                WritableMap layerSelection = Arguments.createMap();

                                WritableArray ids = Arguments.createArray();
                                WritableMap layerInfo = Arguments.createMap();

                                layerInfo.putString("name", layer.getName());
                                layerInfo.putString("caption", layer.getCaption());
                                layerInfo.putBoolean("editable", layer.isEditable());
                                layerInfo.putBoolean("visible", layer.isVisible());
                                layerInfo.putBoolean("selectable", layer.isSelectable());
                                layerInfo.putInt("type", layer.getDataset().getType().value());
                                layerInfo.putString("path", SMLayer.getLayerPath(layer));

//                                layerSelection.putMap("layerInfo", layerInfo);
//                                layerSelection.putArray("ids", ids);

                                ids.pushInt(id);

                                layerSelection.put("layerInfo", layerInfo);
                                layerSelection.put("ids", ids);

                                arr.add(layerSelection);
                            }
                        }

                        for (int k = 0; k < arr.size(); k++) {
                            WritableMap map = Arguments.createMap();
                            map.putMap("layerInfo", (WritableMap) arr.get(k).get("layerInfo"));
                            map.putArray("ids", (WritableArray) arr.get(k).get("ids"));
                            array.pushMap(map);
                        }

                        WritableMap geometries = Arguments.createMap();
                        geometries.putArray("geometries", array);
                        context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                                .emit(EventConst.MAP_GEOMETRY_MULTI_SELECTED, geometries);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            };
            sMap = getInstance();
            MapControl mapControl = sMap.smMapWC.getMapControl();
            mapControl.addGeometrySelectedListener(mGeometrySelectedListener);
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    @ReactMethod
    public void removeGeometrySelectedListener(Promise promise) {
        try {
            sMap = getInstance();
            MapControl mapControl = sMap.smMapWC.getMapControl();
            mapControl.removeGeometrySelectedListener(mGeometrySelectedListener);
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 指定编辑几何对象
     *
     * @param geoID
     * @param layerName
     * @param promise
     */
    @ReactMethod
    public void appointEditGeometry(int geoID, String layerName, Promise promise) {
        try {
            sMap = getInstance();
            MapControl mapControl = sMap.smMapWC.getMapControl();
            Layer layer = mapControl.getMap().getLayers().get(layerName);
            boolean result = mapControl.appointEditGeometry(geoID, layer);
            layer.setEditable(true);
            promise.resolve(result);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 获取指定SymbolGroup中所有的group
     *
     * @param type
     * @param path
     * @param promise
     */
    @ReactMethod
    public void getSymbolGroups(String type, String path, Promise promise) {
        try {
            sMap = getInstance();
            Resources resources = sMap.smMapWC.getWorkspace().getResources();
            WritableArray groups = SMSymbol.getSymbolGroups(resources, type, path);

            promise.resolve(groups);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 获取指定SymbolGroup中所有的symbol
     *
     * @param type
     * @param path
     * @param promise
     */
    @ReactMethod
    public void findSymbolsByGroups(String type, String path, Promise promise) {
        try {
            sMap = getInstance();
            Resources resources = sMap.smMapWC.getWorkspace().getResources();
            WritableArray symbols = SMSymbol.findSymbolsByGroups(resources, type, path);

            promise.resolve(symbols);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 导入工作空间
     *
     * @param wInfo
     * @param strFilePath
     * @param breplaceDatasource
     * @param promise
     */
    @ReactMethod
    public void importWorkspace(ReadableMap wInfo, String strFilePath, boolean breplaceDatasource, Promise promise) {
        try {
            sMap = SMap.getInstance();
            boolean result = sMap.smMapWC.importWorkspaceInfo(wInfo.toHashMap(), strFilePath, breplaceDatasource, true);
            promise.resolve(result);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 导出工作空间
     *
     * @param arrMapNames
     * @param strFileName
     * @param isFileReplace
     * @param promise
     */
    @ReactMethod
    public void exportWorkspace(ReadableArray arrMapNames, String strFileName, boolean isFileReplace, ReadableMap extraMap, Promise promise) {
        try {

            sMap = getInstance();
            boolean result = sMap.smMapWC.exportMapNames(arrMapNames, strFileName, isFileReplace, extraMap);
            promise.resolve(result);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    // mapName 地图名字（不含后缀）
    // ofModule 模块名（默认传空）
    // isPrivate 是否是用户数据
    // exportWorkspacePath 导出的工作空间绝对路径（含后缀）
    @ReactMethod
    public void exportWorkspaceByMap(String mapName, String exportWorkspacePath, ReadableMap mapParam, Promise promise) {
        try {
            sMap = getInstance();
            WritableMap param = Arguments.createMap();
            param.merge(mapParam);
            param.putBoolean("IsReplaceSymbol", true);
            boolean openResult = sMap.getSmMapWC().openMapName(mapName, sMap.getSmMapWC().getWorkspace(), param);
            boolean exportResult = false;
            if (openResult) {
                WritableArray array = Arguments.createArray();
                ((WritableArray) array).pushString(mapName);
                exportResult = sMap.getSmMapWC().exportMapNames(array, exportWorkspacePath, true, null);
                Maps maps = sMap.getSmMapWC().getWorkspace().getMaps();
                maps.clear();
                SMap.getInstance().getSmMapWC().getWorkspace().getResources().getMarkerLibrary().getRootGroup().getChildGroups().remove(mapName, false);
                SMap.getInstance().getSmMapWC().getWorkspace().getResources().getLineLibrary().getRootGroup().getChildGroups().remove(mapName, false);
                SMap.getInstance().getSmMapWC().getWorkspace().getResources().getFillLibrary().getRootGroup().getChildGroups().remove(mapName, false);
                sMap.getSmMapWC().getWorkspace().getDatasources().closeAll();
            }
            promise.resolve(exportResult);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 获取图层标题列表及对应的数据集类型
     *
     * @param promise
     */
    @ReactMethod
    public void getLayersNames(Promise promise) {
        try {
            sMap = getInstance();
            Layers layers = sMap.smMapWC.getMapControl().getMap().getLayers();
            int count = layers.getCount();
            WritableArray arr = Arguments.createArray();
            for (int i = 0; i < count; i++) {
                //获取图层标题（区别于图层的名称）
                String caption = layers.get(i).getCaption();
                WritableMap writeMap = Arguments.createMap();

                //获取数据集类型
                DatasetType type = layers.get(i).getDataset().getType();
                String datasetType = "";
                if (type == DatasetType.POINT) {
                    datasetType = "POINT";
                } else if (type == DatasetType.LINE) {
                    datasetType = "LINE";
                } else if (type == DatasetType.REGION) {
                    datasetType = "REGION";
                } else if (type == DatasetType.GRID) {
                    datasetType = "GRID";
                } else if (type == DatasetType.TEXT) {
                    datasetType = "TEXT";
                } else if (type == DatasetType.IMAGE) {
                    datasetType = "IMAGE";
                } else {
                    datasetType = type.toString();
                }

                writeMap.putString("title", caption);
                writeMap.putString("datasetType", datasetType);
                arr.pushMap(writeMap);
            }
            promise.resolve(arr);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    @ReactMethod
    public void isModified(Promise promise) {
        try {
            sMap = getInstance();
            boolean bWorspaceModified = sMap.smMapWC.getWorkspace().isModified();
            boolean bMapModified = sMap.smMapWC.getMapControl().getMap().isModified();
            if (!bWorspaceModified && !bMapModified)
                promise.resolve(false);
            else
                promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    @ReactMethod
    public void getMapName(Promise promise) {
        try {
            sMap = getInstance();
            String mapName = sMap.smMapWC.getMapControl().getMap().getName();
            promise.resolve(mapName);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 保存地图为XML
     */
    @ReactMethod
    public void saveMapToXML(String filePath, Promise promise) {
        try {
            sMap = getInstance();
            String mapName = filePath.substring(filePath.lastIndexOf('/') + 1, filePath.lastIndexOf('.'));

            int count = sMap.smMapWC.getWorkspace().getMaps().getCount();
            for (int i = 0; i < count; i++) {
                String name = sMap.smMapWC.getWorkspace().getMaps().get(i);
                if (mapName.equals(name)) {
                    sMap.smMapWC.getMapControl().getMap().save();
                    break;
                }
                if (i == count - 1) {
                    sMap.smMapWC.getMapControl().getMap().saveAs(mapName);
                }
            }

            if (count == 0) {
                sMap.smMapWC.getMapControl().getMap().saveAs(mapName);
            }
            String mapXML = sMap.smMapWC.getMapControl().getMap().toXML();


            if (!mapXML.equals("")) {
                File file = new File(filePath);

                FileWriter fileWritter = new FileWriter(file);
                fileWritter.write(mapXML);
                fileWritter.close();
            }

            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 加载地图XML，显示地图
     *
     * @param
     */
    @ReactMethod
    public void openMapFromXML(String filePath, Promise promise) {
        try {
            sMap = getInstance();
            String mapName = filePath.substring(filePath.lastIndexOf('/') + 1, filePath.lastIndexOf('.'));
            File file = new File(filePath);
            Reader reader = null;

            reader = new InputStreamReader(new FileInputStream(file));
            char[] buffer = new char[1024];
            int index = 0;
            String strXML = "";
            while ((index = reader.read(buffer)) != -1) {
                strXML += String.valueOf(buffer);
            }

            int count = sMap.smMapWC.getWorkspace().getMaps().getCount();
            for (int i = 0; i < count; i++) {
                String name = sMap.smMapWC.getWorkspace().getMaps().get(i);
                if (mapName.equals(name)) {
                    break;
                }
                if (i == count - 1) {
                    sMap.smMapWC.getWorkspace().getMaps().add(mapName, strXML);
                }
            }
            if (count == 0) {
                sMap.smMapWC.getWorkspace().getMaps().add(mapName, strXML);
            }
            sMap.smMapWC.getMapControl().getMap().open(mapName);
            sMap.smMapWC.getMapControl().getMap().refresh();
            promise.resolve(true);

        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 获取地图对应的数据源
     *
     * @param
     */
    @ReactMethod
    public void getMapDatasourcesAlias(Promise promise) {
        try {
            sMap = getInstance();
            Layers layers = sMap.smMapWC.getMapControl().getMap().getLayers();
            int count = layers.getCount();

            String datasourceName = "";
            ArrayList<String> datasourceNamelist = new ArrayList<String>();
            WritableArray arr = Arguments.createArray();
            for (int i = 0; i < count; i++) {
                Dataset dataset = layers.get(i).getDataset();
                if (dataset != null) {
                    String dataSourceAlias = dataset.getDatasource().getAlias();

                    if (!datasourceNamelist.contains(dataSourceAlias)) {
                        datasourceNamelist.add(dataSourceAlias);

                        WritableMap writeMap = Arguments.createMap();
                        writeMap.putString("title", dataSourceAlias + ".udb");
                        arr.pushMap(writeMap);
                    }
                }

            }

//            datasourceName.substring( 2,datasourceName.length()-2 );
            promise.resolve(arr);

        } catch (Exception e) {
            promise.reject(e);
        }

    }

    /**
     * 添加数据集到当前地图
     *
     * @param readableMap
     * @param promise
     */
    @ReactMethod
    public void addDatasetToMap(ReadableMap readableMap, Promise promise) {
        try {
            sMap = SMap.getInstance();
            HashMap<String, Object> data = readableMap.toHashMap();
            String datastourceName = null;
            String datasetName = null;

            if (data.containsKey("DatasourceName")) {
                datastourceName = data.get("DatasourceName").toString();
            }
            if (data.containsKey("DatasetName")) {
                datasetName = data.get("DatasetName").toString();
            }

            Workspace workspace = sMap.smMapWC.getWorkspace();
            if (datastourceName != null && datasetName != null) {
                Datasource datasource = workspace.getDatasources().get(datastourceName);
                Dataset dataset = datasource.getDatasets().get(datasetName);

                com.supermap.mapping.Map map = sMap.smMapWC.getMapControl().getMap();
                Layer layer = map.getLayers().add(dataset, true);
                if (dataset.getType() == DatasetType.REGION) {
                    LayerSettingVector setting = (LayerSettingVector) layer.getAdditionalSetting();
                    setting.getStyle().setLineSymbolID(5);
                }
                if (dataset.getType() == DatasetType.REGION || dataset.getType() == DatasetType.REGION3D) {
                    LayerSettingVector setting = (LayerSettingVector) layer.getAdditionalSetting();
                    setting.getStyle().setFillForeColor(getFillColor());
                    setting.getStyle().setLineColor(getLineColor());
                } else if (dataset.getType() == DatasetType.LINE || dataset.getType() == DatasetType.NETWORK || dataset.getType() == DatasetType.NETWORK3D
                        || dataset.getType() == DatasetType.LINE3D) {
                    LayerSettingVector setting = (LayerSettingVector) layer.getAdditionalSetting();
                    setting.getStyle().setLineColor(getLineColor());
                    if (dataset.getType() == DatasetType.NETWORK || dataset.getType() == DatasetType.NETWORK3D) {
                        map.getLayers().add(((DatasetVector) dataset).getChildDataset(), true);
                    }
                } else if (dataset.getType() == DatasetType.POINT || dataset.getType() == DatasetType.POINT3D) {
                    LayerSettingVector setting = (LayerSettingVector) layer.getAdditionalSetting();
                    setting.getStyle().setLineColor(getLineColor());
                }

                map.setVisibleScalesEnabled(false);
                map.refresh();

                promise.resolve(layer != null);
            } else {
                promise.resolve(false);
            }
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 导出地图为xml
     *
     * @param name
     * @param nModule
     * @param addition
     * @param isNew
     * @param promise
     */
    @ReactMethod
    public void saveMapName(String name, String nModule, ReadableMap addition, boolean isNew, boolean bResourcesModified, boolean bPrivate, Promise promise) {
        try {
            sMap = SMap.getInstance();
            boolean mapSaved = false;
            boolean bNew = true;

            com.supermap.mapping.Map map = sMap.smMapWC.getMapControl().getMap();
            if (map.getName() != null && !map.getName().equals("")) {
                bNew = false;
            }

            String oldName = map.getName();

            if (name == null || name.equals("")) {
                if (map.getName() != null && !map.getName().equals("")) {
                    bNew = false;
                    mapSaved = map.save();
                    name = map.getName();
                } else if (map.getLayers().getCount() > 0) {
                    bNew = true;
                    Layers layers = map.getLayers();
                    Layer layer = layers.get(layers.getCount() - 1);
                    name = layer.getName();
                    int i = 0;
                    while (!mapSaved) {
                        name = i == 0 ? name : (name + i);
                        mapSaved = map.save(name);
                        i++;
                    }
                }
            } else {
                if (name.equals(map.getName())) {
                    bNew = false;
                    mapSaved = map.save();
                    name = map.getName();
                } else {
                    bNew = true;
                    mapSaved = isNew ? map.saveAs(name) : map.save(name);
                }
            }

//            boolean bResourcesModified = sMap.smMapWC.getWorkspace().getMaps().getCount() > 1;
            String mapName = "";

            Map<String, String> additionInfo = new HashMap<>();
            ReadableMapKeySetIterator keys = addition.keySetIterator();
            while (keys.hasNextKey()) {
                String key = keys.nextKey();
                additionInfo.put(key, addition.getString(key));
            }
            if (mapSaved) {
                mapName = sMap.smMapWC.saveMapName(name, sMap.smMapWC.getWorkspace(), nModule, additionInfo, (isNew || bNew), bResourcesModified, bPrivate);
            }

            // isNew为true，另存为后保证当前地图是原地图
            boolean isOpen = false;
            if (oldName != null && !oldName.equals("") && !oldName.equals(mapName) && isNew) {
                isOpen = map.open(oldName);
                if (isOpen && sMap.getSmMapWC().getWorkspace().getMaps().indexOf(mapName) >= 0) {
                    sMap.getSmMapWC().getWorkspace().getMaps().remove(mapName);
                }
                map.refresh();
            }

            promise.resolve(mapName);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 导入文件工作空间到程序目录
     *
     * @param infoMap
     * @param nModule
     * @param promise
     */
    @ReactMethod
    public void importWorkspaceInfo(ReadableMap infoMap, String nModule, boolean bPrivate, Promise promise) {
        try {
            sMap = SMap.getInstance();
            List<String> list = sMap.smMapWC.importWorkspaceInfo(infoMap.toHashMap(), nModule, bPrivate);
            WritableArray mapsInfo = Arguments.createArray();
            if (list == null) {
                 promise.resolve(mapsInfo);
            }else {
                if (list.size() > 0) {
                    for (int i = 0; i < list.size(); i++) {
                        mapsInfo.pushString(list.get(i));
                    }
                }
                promise.resolve(mapsInfo);
            }
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 导入数据源到程序目录
     *
     * @param strFile
     * @param strModule
     * @param promise
     */
    @ReactMethod
    public void importDatasourceFile(String strFile, String strModule, Promise promise) {
        try {
//            sMap = SMap.getInstance();
//            DatasourceConnectionInfo datasourceConnectionInfo = new DatasourceConnectionInfo();
//            datasourceConnectionInfo.setServer(strFile);
//            datasourceConnectionInfo.setEngineType(EngineType.UDB);
//            Datasource datasource = sMap.smMapWC.getWorkspace().getDatasources().open(datasourceConnectionInfo);
//            if(datasource.getDescription().equals("Label")){
//                String todatasource=rootPath+"/iTablet/User/"+sMap.smMapWC.getUserName()+"/Data/Label/Label.udb";
//                File udb=new File(todatasource);
//                if(udb.exists()){
//                    sMap.getSmMapWC().copyDataset(strFile,todatasource);
//                }
//            }else {
//                String result = sMap.smMapWC.importDatasourceFile(strFile, strModule);
//                promise.resolve(result);
//            }
//            datasourceConnectionInfo.dispose();
            String result = sMap.smMapWC.importDatasourceFile(strFile, strModule);
            promise.resolve(result);
        } catch (Exception e) {
            promise.reject(e);
        }
    }


    /**
     * 大工作空间打开本地地图
     *
     * @param strMapName
     * @param promise
     */
    @ReactMethod
    public void openMapName(String strMapName, ReadableMap mapParam, Promise promise) {
        try {
            sMap = SMap.getInstance();
            boolean result = sMap.smMapWC.openMapName(strMapName, sMap.smMapWC.getWorkspace(), mapParam);

            promise.resolve(result);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 设置地图反走样
     *
     * @param value
     * @param promise
     */
    @ReactMethod
    public void setAntialias(boolean value, Promise promise) {
        try {
            sMap = SMap.getInstance();
            com.supermap.mapping.Map map = sMap.getSmMapWC().getMapControl().getMap();
            map.setAntialias(value);
            map.refresh();

            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 获取是否反走样
     *
     * @param promise
     */
    @ReactMethod
    public void isAntialias(Promise promise) {
        try {
            sMap = SMap.getInstance();
            com.supermap.mapping.Map map = sMap.getSmMapWC().getMapControl().getMap();
            boolean result = map.isAntialias();

            promise.resolve(result);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 设置固定比例尺
     *
     * @param value
     * @param promise
     */
    @ReactMethod
    public void setVisibleScalesEnabled(boolean value, Promise promise) {
        try {
            sMap = SMap.getInstance();
            com.supermap.mapping.Map map = sMap.getSmMapWC().getMapControl().getMap();
            map.setVisibleScalesEnabled(value);
            map.refresh();

            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 获取是否固定比例尺
     *
     * @param promise
     */
    @ReactMethod
    public void isVisibleScalesEnabled(Promise promise) {
        try {
            sMap = SMap.getInstance();
            com.supermap.mapping.Map map = sMap.getSmMapWC().getMapControl().getMap();
            boolean result = map.isVisibleScalesEnabled();

            promise.resolve(result);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 检查是否有打开的地图
     *
     * @param promise
     */
    @ReactMethod
    public void isAnyMapOpened(Promise promise) {
        try {
            sMap = getInstance();
            com.supermap.mapping.Map map = sMap.smMapWC.getMapControl().getMap();
            int count = map.getLayers().getCount();
            boolean isAny = true;
            if (count <= 0) {
                isAny = false;
            }

            promise.resolve(isAny);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 把指定地图中的图层添加到当前打开地图中
     *
     * @param srcMapName 要添加的地图
     * @param promise
     */
    @ReactMethod
    public void addMap(String srcMapName, ReadableMap mapParam, Promise promise) {
        try {
            sMap = getInstance();
            com.supermap.mapping.Map map = sMap.smMapWC.getMapControl().getMap();
            boolean result = sMap.smMapWC.addLayersFromMap(srcMapName, map, mapParam);

            promise.resolve(result);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 批量添加图层
     *
     * @param datasetNames
     * @param promise
     */
    @ReactMethod
    public void addLayers(ReadableArray datasetNames, String datastourceName, Promise promise) {
        try {
            if (datasetNames == null || datasetNames.size() == 0 || datastourceName == null || datastourceName.isEmpty()) {
                promise.resolve(false);
                return;
            }

            Workspace workspace = sMap.smMapWC.getWorkspace();
            Datasource datasource = workspace.getDatasources().get(datastourceName);
            com.supermap.mapping.Map map = sMap.smMapWC.getMapControl().getMap();
            Layers layers = map.getLayers();

            ArrayList<Dataset> datasets_point = new ArrayList<>();
            ArrayList<Dataset> datasets_line = new ArrayList<>();
            ArrayList<Dataset> datasets_region = new ArrayList<>();
            ArrayList<Dataset> datasets_text = new ArrayList<>();
            ArrayList<Dataset> datasets_else = new ArrayList<>();
            for (int i = 0; i < datasetNames.size(); i++) {
                String datasetName = datasetNames.getString(i);
                Dataset dataset = datasource.getDatasets().get(datasetName);


                if (dataset.getType() == DatasetType.REGION || dataset.getType() == DatasetType.REGION3D) {
                    datasets_region.add(dataset);
                } else if (dataset.getType() == DatasetType.LINE || dataset.getType() == DatasetType.NETWORK || dataset.getType() == DatasetType.NETWORK3D
                        || dataset.getType() == DatasetType.LINE3D) {
                    datasets_line.add(dataset);
                } else if (dataset.getType() == DatasetType.POINT || dataset.getType() == DatasetType.POINT3D) {
                    datasets_point.add(dataset);
                } else if (dataset.getType() == DatasetType.TEXT) {
                    datasets_text.add(dataset);
                } else {
                    datasets_else.add(dataset);
                }
            }

            ArrayList<Dataset> datasets = new ArrayList<>();
            datasets.addAll(datasets_else);
            datasets.addAll(datasets_region);
            datasets.addAll(datasets_line);
            datasets.addAll(datasets_point);
            datasets.addAll(datasets_text);

            if (datasets.size() > 0) {
                MapControl mapControl = SMap.getSMWorkspace().getMapControl();
                mapControl.getEditHistory().addMapHistory();

                for (int i = 0; i < datasets.size(); i++) {
                    Dataset dataset = datasets.get(i);

                    Layer layer = layers.add(dataset, true);
                    if (dataset.getType() == DatasetType.REGION) {
                        LayerSettingVector setting = (LayerSettingVector) layer.getAdditionalSetting();
                        setting.getStyle().setLineSymbolID(5);
                    }
                    if (dataset.getType() == DatasetType.REGION || dataset.getType() == DatasetType.REGION3D) {
                        LayerSettingVector setting = (LayerSettingVector) layer.getAdditionalSetting();
                        setting.getStyle().setFillForeColor(getFillColor());
                        setting.getStyle().setLineColor(getLineColor());
                    } else if (dataset.getType() == DatasetType.LINE || dataset.getType() == DatasetType.NETWORK || dataset.getType() == DatasetType.NETWORK3D
                            || dataset.getType() == DatasetType.LINE3D) {
                        LayerSettingVector setting = (LayerSettingVector) layer.getAdditionalSetting();
                        setting.getStyle().setLineColor(getLineColor());
                        if (dataset.getType() == DatasetType.NETWORK || dataset.getType() == DatasetType.NETWORK3D) {
                            map.getLayers().add(((DatasetVector) dataset).getChildDataset(), true);
                        }
                    } else if (dataset.getType() == DatasetType.POINT || dataset.getType() == DatasetType.POINT3D) {
                        LayerSettingVector setting = (LayerSettingVector) layer.getAdditionalSetting();
                        setting.getStyle().setLineColor(getLineColor());
                    }
                }
                map.setVisibleScalesEnabled(false);
                map.refresh();
            }

            promise.resolve(true);
        } catch (Exception e) {
            Log.e(REACT_CLASS, e.getMessage());
            e.printStackTrace();
            promise.reject(e);
        }
    }

    /**
     * 导入符号库
     *
     * @param path
     * @param isReplace 是否替换
     * @param promise
     */
    @ReactMethod
    public void importSymbolLibrary(String path, boolean isReplace, Promise promise) {
        try {
            sMap = SMap.getInstance();
            boolean result = sMap.smMapWC.appendFromFile(SMap.getInstance().smMapWC.getWorkspace().getResources(), path, isReplace);

            promise.resolve(result);
        } catch (Exception e) {
            Log.e(REACT_CLASS, e.getMessage());
            e.printStackTrace();
            promise.reject(e);
        }
    }

    /**
     * 设置是否压盖
     *
     * @param value
     * @param promise
     */
    @ReactMethod
    public void setOverlapDisplayed(boolean value, Promise promise) {
        try {
            sMap = SMap.getInstance();
            com.supermap.mapping.Map map = sMap.getSmMapWC().getMapControl().getMap();
            map.setOverlapDisplayed(value);
            map.refresh();

            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 是否已经开启压盖
     *
     * @param promise
     */
    @ReactMethod
    public void isOverlapDisplayed(Promise promise) {
        try {
            sMap = SMap.getInstance();
            com.supermap.mapping.Map map = sMap.getSmMapWC().getMapControl().getMap();
            boolean result = map.isOverlapDisplayed();

            promise.resolve(result);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    @ReactMethod
    public void getMapsByFile(String path, Promise promise) {
        try {
            WorkspaceType type = null;
            if (path.contains("sxwu")) {
                type = WorkspaceType.SXWU;
            } else if (path.contains("smwu")) {
                type = WorkspaceType.SMWU;
            } else if (path.contains("sxw")) {
                type = WorkspaceType.SXW;
            } else if (path.contains("smw")) {
                type = WorkspaceType.SMW;
            }
            Workspace workspace = new Workspace();
            WorkspaceConnectionInfo wsInfo = new WorkspaceConnectionInfo();
            wsInfo.setServer(path);
            wsInfo.setType(type);
            boolean result = workspace.open(wsInfo);
            WritableArray arr = Arguments.createArray();
            if (result) {
                for (int i = 0; i < workspace.getMaps().getCount(); i++) {
                    arr.pushString(workspace.getMaps().get(i));
                }
            }

            workspace.close();
            wsInfo.dispose();
            workspace.dispose();

            promise.resolve(arr);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 显示全幅
     *
     * @param promise
     */
    @ReactMethod
    public void viewEntire(Promise promise) {
        try {
            sMap = SMap.getInstance();
            com.supermap.mapping.Map map = sMap.getSmMapWC().getMapControl().getMap();
            map.viewEntire();
            map.refresh();

            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 开启动态投影
     *
     * @param promise
     */
    @ReactMethod
    public void setDynamicProjection(Promise promise) {
        try {
            sMap = SMap.getInstance();
            com.supermap.mapping.Map map = sMap.getSmMapWC().getMapControl().getMap();
            map.setDynamicProjection(true);
            map.refresh();

            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    // 添加指定字段到数据集
    private void addFieldInfo(DatasetVector dv, String name, FieldType type, boolean required, String value, int maxLength) {
        FieldInfos infos = dv.getFieldInfos();
        if (infos.indexOf(name) != -1) {//exists
            infos.remove(name);
        }
        FieldInfo newInfo = new FieldInfo();
        newInfo.setName(name);
        newInfo.setType(type);
        newInfo.setMaxLength(maxLength);
        newInfo.setDefaultValue(value);
        newInfo.setRequired(required);
        infos.add(newInfo);
    }

    /**
     * 新建标注数据集
     *
     * @param promise
     */
    @ReactMethod
    public void newTaggingDataset(String name,String userpath, Promise promise) {
        try {
            sMap = SMap.getInstance();
            Workspace workspace = sMap.smMapWC.getMapControl().getMap().getWorkspace();
            Datasource opendatasource = workspace.getDatasources().get("Label_"+userpath+"#");
//            sMap.smMapWC.getWorkspace().getConnectionInfo().getServer();
            if (opendatasource == null) {
                DatasourceConnectionInfo info = new DatasourceConnectionInfo();
                info.setAlias("Label_"+userpath+"#");
                info.setEngineType(EngineType.UDB);
                info.setServer(rootPath + "/iTablet/User/"+userpath+"/Data/Datasource/Label_"+userpath+"#.udb");
                Datasource datasource = workspace.getDatasources().open(info);
                if (datasource != null) {
                    Datasets datasets = datasource.getDatasets();
                    String datasetName = datasets.getAvailableDatasetName(name);
                    DatasetVectorInfo datasetVectorInfo = new DatasetVectorInfo();
                    datasetVectorInfo.setType(DatasetType.CAD);
                    datasetVectorInfo.setEncodeType(EncodeType.NONE);
                    datasetVectorInfo.setName(datasetName);
                    DatasetVector datasetVector = datasets.create(datasetVectorInfo);
                    //创建数据集时创建好字段
                    addFieldInfo(datasetVector, "name", FieldType.TEXT, false, "", 255);
                    addFieldInfo(datasetVector, "remark", FieldType.TEXT, false, "", 255);
                    addFieldInfo(datasetVector, "address", FieldType.TEXT, false, "", 255);

                    Dataset ds = datasets.get(datasetName);
                    com.supermap.mapping.Map map = sMap.smMapWC.getMapControl().getMap();
                    Layer layer = map.getLayers().add(ds, true);
                    layer.setEditable(true);
                    datasetVectorInfo.dispose();
                    datasetVector.close();
                    info.dispose();
                    promise.resolve(datasetName);
                }
            } else {
                Datasets datasets = opendatasource.getDatasets();
                String datasetName = datasets.getAvailableDatasetName(name);
                DatasetVectorInfo datasetVectorInfo = new DatasetVectorInfo();
                datasetVectorInfo.setType(DatasetType.CAD);
                datasetVectorInfo.setEncodeType(EncodeType.NONE);
                datasetVectorInfo.setName(datasetName);
                DatasetVector datasetVector = datasets.create(datasetVectorInfo);
                //创建数据集时创建好字段
                addFieldInfo(datasetVector, "name", FieldType.TEXT, false, "", 255);
                addFieldInfo(datasetVector, "remark", FieldType.TEXT, false, "", 255);
                addFieldInfo(datasetVector, "address", FieldType.TEXT, false, "", 255);

                Dataset ds = datasets.get(datasetName);
                com.supermap.mapping.Map map = sMap.smMapWC.getMapControl().getMap();
                Layer layer = map.getLayers().add(ds, true);
                layer.setEditable(true);
                datasetVectorInfo.dispose();
                datasetVector.close();

                promise.resolve(datasetName);
            }
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 删除标注数据集
     *
     * @param promise
     */
    @ReactMethod
    public void removeTaggingDataset(String name,String userpath, Promise promise) {
        try {
            sMap = SMap.getInstance();
            Workspace workspace = sMap.smMapWC.getMapControl().getMap().getWorkspace();
            Datasource opendatasource = workspace.getDatasources().get("Label_"+userpath+"#");
            if (opendatasource == null) {
                DatasourceConnectionInfo info = new DatasourceConnectionInfo();
                info.setAlias("Label_"+userpath+"#");
                info.setEngineType(EngineType.UDB);
                info.setServer(rootPath + "/iTablet/User/"+userpath+"/Data/Datasource/Label_"+userpath+"#.udb");
                Datasource datasource = workspace.getDatasources().open(info);
                if (datasource != null) {
                    Datasets datasets = datasource.getDatasets();
                    datasets.delete(name);
                }
                promise.resolve(true);
            } else {
                Datasets datasets = opendatasource.getDatasets();
                datasets.delete(name);
                promise.resolve(true);
            }
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 导入标注数据集
     *
     * @param promise
     */
    @ReactMethod
    public void openTaggingDataset(String userpath, Promise promise) {
        try {
            sMap = SMap.getInstance();
            Workspace workspace = sMap.smMapWC.getMapControl().getMap().getWorkspace();
            Datasource opendatasource = workspace.getDatasources().get("Label_"+userpath+"#");
            if (opendatasource == null) {
                DatasourceConnectionInfo info = new DatasourceConnectionInfo();
                info.setAlias("Label_"+userpath+"#");
                info.setEngineType(EngineType.UDB);
                info.setServer(rootPath + "/iTablet/User/"+userpath+"/Data/Datasource/Label_"+userpath+"#.udb");
                Datasource datasource = workspace.getDatasources().open(info);
                if (datasource != null) {
                    Datasets datasets = datasource.getDatasets();
                    com.supermap.mapping.Map map = sMap.smMapWC.getMapControl().getMap();
                    for(int i =0;i<datasets.getCount();i++) {
                        Dataset ds = datasets.get(i);
                        String addname = ds.getName()+"@Label_"+userpath+"#";
                        boolean add = true;
                        Layers maplayers = map.getLayers();
                        for(int j=0 ; j<maplayers.getCount();j++){
                            if(maplayers.get(j).getCaption().equals(addname)){
                                add = false;
                            }
                        }
                        if(add) {
                            Layer layer = map.getLayers().add(ds, true);
                            layer.setEditable(false);
                            layer.setVisible(false);
                        }
                    }
                }
                promise.resolve(true);
            } else {
                Datasets datasets = opendatasource.getDatasets();
                com.supermap.mapping.Map map = sMap.smMapWC.getMapControl().getMap();
                for(int i =0;i<datasets.getCount();i++) {
                    Dataset ds = datasets.get(i);
                    String addname = ds.getName()+"@Label_"+userpath+"#";
                    boolean add = true;
                    Layers maplayers = map.getLayers();
                    for(int j=0 ; j<maplayers.getCount();j++){
                        if(maplayers.get(j).getCaption().equals(addname)){
                            add = false;
                        }
                    }
                    if(add) {
                        Layer layer = map.getLayers().add(ds, true);
                        layer.setEditable(false);
                        layer.setVisible(false);
                    }
                }
                promise.resolve(true);
            }
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 获取默认标注
     * @param promise
     */
    @ReactMethod
    public void getDefaultTaggingDataset(String userpath, Promise promise) {
        try {
            sMap = SMap.getInstance();
            Workspace workspace = sMap.smMapWC.getMapControl().getMap().getWorkspace();
            Datasource opendatasource = workspace.getDatasources().get("Label_"+userpath+"#");
            if (opendatasource != null) {
                String datasetname = "";
                    Datasets datasets = opendatasource.getDatasets();
                    for(int i =0;i<datasets.getCount();i++){
                        Dataset dataset = datasets.get(i);
                        datasetname = dataset.getName();
                }
                promise.resolve(datasetname);
            }
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 获取当前标注
     * @param promise
     */
    @ReactMethod
    public void getCurrentTaggingDataset(String name, Promise promise) {
        try {
            sMap = SMap.getInstance();
            com.supermap.mapping.Map map = sMap.smMapWC.getMapControl().getMap();
                String datasetname = "";
                Layer layer = map.getLayers().get(name);
                layer.setVisible(true);
                layer.setEditable(true);
                map.refresh();
                datasetname = layer.getDataset().getName();
                promise.resolve(datasetname);
        } catch (Exception e) {
            promise.reject(e);
        }
    }



    /**
     * 获取标注图层
     *
     * @param promise
     */
    @ReactMethod
    public void getTaggingLayers(String userpath, Promise promise) {
        try {
            sMap = SMap.getInstance();
            Workspace workspace = sMap.smMapWC.getMapControl().getMap().getWorkspace();
            Datasource opendatasource = workspace.getDatasources().get("Label_"+userpath+"#");
            if (opendatasource == null) {
                DatasourceConnectionInfo info = new DatasourceConnectionInfo();
                info.setAlias("Label_"+userpath+"#");
                info.setEngineType(EngineType.UDB);
                info.setServer(rootPath + "/iTablet/User/"+userpath+"/Data/Datasource/Label_"+userpath+"#.udb");
                Datasource datasource = workspace.getDatasources().open(info);
                if (datasource != null) {
                    Datasets datasets = datasource.getDatasets();
                    com.supermap.mapping.Map map = sMap.smMapWC.getMapControl().getMap();
                    WritableArray arr = Arguments.createArray();
                    Layers layers = map.getLayers();

                    for(int i = 0 ; i < datasets.getCount() ; i++){
                        Dataset ds = datasets.get(i);
                        for (int j=0;j<layers.getCount();j++){
                            Layer layer = layers.get(j);
                            if(layer.getDataset()==ds){
                                WritableMap writeMap = SMLayer.getLayerInfo(layer, "");
                                arr.pushMap(writeMap);
                            }
                        }
                    }
                    promise.resolve(arr);
                }
            } else {
                Datasets datasets = opendatasource.getDatasets();
                com.supermap.mapping.Map map = sMap.smMapWC.getMapControl().getMap();
                WritableArray arr = Arguments.createArray();
                Layers layers = map.getLayers();

                for(int i = 0 ; i < datasets.getCount() ; i++){
                    Dataset ds = datasets.get(i);
                    for (int j=0;j<layers.getCount();j++){
                        Layer layer = layers.get(j);
                        if(layer.getDataset()==ds){
                            WritableMap writeMap = SMLayer.getLayerInfo(layer, "");
                            arr.pushMap(writeMap);
                        }
                    }
                }
                promise.resolve(arr);
            }
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 添加数据集属性字段
     *
     * @param promise
     */
    @ReactMethod
    public void addRecordset(String datasetName, String filedInfoName, String value,String userpath, Promise promise) {
        try {
            sMap = SMap.getInstance();
            Workspace workspace = sMap.smMapWC.getMapControl().getMap().getWorkspace();
            Datasource opendatasource = workspace.getDatasources().get("Label_"+userpath+"#");
            if (opendatasource == null) {
                DatasourceConnectionInfo info = new DatasourceConnectionInfo();
                info.setAlias("Label_"+userpath+"#");
                info.setEngineType(EngineType.UDB);
                info.setServer(rootPath + "/iTablet/User/"+userpath+"/Data/Datasource/Label_"+userpath+"#.udb");
                Datasource datasource = workspace.getDatasources().open(info);
                if (datasource != null) {
                    Datasets datasets = datasource.getDatasets();
                    DatasetVector dataset = (DatasetVector) datasets.get(datasetName);
                    modifyLastAttribute(dataset, filedInfoName, value);
                }
                promise.resolve(true);
            } else {
                Datasets datasets = opendatasource.getDatasets();
                DatasetVector dataset = (DatasetVector) datasets.get(datasetName);
                modifyLastAttribute(dataset, filedInfoName, value);
                promise.resolve(true);
            }
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    // 修改最新的属性值
    private void modifyLastAttribute(Dataset dataset, String filedInfoName, String value) {
        if (dataset == null) {
            return;
        }
        if (filedInfoName == null) {
            return;
        }
        if (value == null || value.isEmpty()) {
            return;
        }

        DatasetVector dtVector = (DatasetVector) dataset;
        Recordset recordset = dtVector.getRecordset(false, CursorType.DYNAMIC);
        if (recordset == null) {
            return;
        }
        recordset.moveLast();
        recordset.edit();

        //the dataset didn't have '' fieldinfo
        FieldInfos fieldInfos = recordset.getFieldInfos();
        if (fieldInfos.indexOf(filedInfoName) == -1) {
            return;
        }
        recordset.setFieldValue(filedInfoName, value);

        recordset.update();
        recordset.close();
        recordset.dispose();
    }

    /**
     * 设置最小比例尺范围
     *
     * @param promise
     */
    @ReactMethod
    public void setMinVisibleScale(String name, double number, Promise promise) {
        try {
            sMap = SMap.getInstance();
            Layer layer = sMap.getSmMapWC().getMapControl().getMap().getLayers().get(name);
            double scale = 1 / number;
            layer.setMinVisibleScale(scale);
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 设置最小比例尺范围
     *
     * @param promise
     */
    @ReactMethod
    public void setMaxVisibleScale(String name, double number, Promise promise) {
        try {
            sMap = SMap.getInstance();
            Layer layer = sMap.getSmMapWC().getMapControl().getMap().getLayers().get(name);
            double scale = 1 / number;
            layer.setMaxVisibleScale(scale);
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 添加文字标注
     *
     * @param promise
     */
    @ReactMethod
    public void addTextRecordset(String dataname, String name,String userpath, int x, int y, Promise promise) {
        try {
            sMap = SMap.getInstance();
            sMap.smMapWC.getMapControl().getEditHistory().addMapHistory();
            Point2D p = sMap.smMapWC.getMapControl().getMap().pixelToMap(new Point(x, y));
            Workspace workspace = sMap.smMapWC.getMapControl().getMap().getWorkspace();
            Datasource opendatasource = workspace.getDatasources().get("Label_"+userpath+"#");
            Datasets datasets = opendatasource.getDatasets();
            DatasetVector dataset = (DatasetVector) datasets.get(dataname);
            dataset.setReadOnly(false);
            Recordset recordset = dataset.getRecordset(false, CursorType.DYNAMIC);
            TextPart textPart = new TextPart();
            textPart.setAnchorPoint(p);
            textPart.setText(name);
            GeoText geoText = new GeoText();
            geoText.addPart(textPart);
            recordset.addNew(geoText);
            recordset.update();
            recordset.close();
            geoText.dispose();
            recordset.dispose();
            sMap.smMapWC.getMapControl().getMap().refresh();
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 获取点击坐标
     *
     * @param promise
     */
    @ReactMethod
    public void getGestureDetector(final Promise promise) {
        try {
            sMap = SMap.getInstance();
            final float[] x = new float[1];
            final float[] y = new float[1];
            sMap.smMapWC.getMapControl().setGestureDetector(new GestureDetector(context, new GestureDetector.OnGestureListener() {
                @Override
                public boolean onDown(MotionEvent e) {
                    return false;
                }

                @Override
                public void onShowPress(MotionEvent e) {

                }

                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    x[0] = e.getX();
                    y[0] = e.getY();
                    WritableMap writeMap = Arguments.createMap();
                    writeMap.putDouble("x", x[0]);
                    writeMap.putDouble("y", y[0]);
                    promise.resolve(writeMap);
                    sMap.smMapWC.getMapControl().deleteGestureDetector();

                    return true;
                }

                @Override
                public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                    return false;
                }

                @Override
                public void onLongPress(MotionEvent e) {

                }

                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                    return false;
                }
            }));
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 设置标注面随机色
     *
     * @param promise
     */
    @ReactMethod
    public void setTaggingGrid(String name,String userpath, Promise promise) {
        try {
            sMap = SMap.getInstance();
            MapControl mapControl = sMap.smMapWC.getMapControl();
            Workspace workspace = sMap.smMapWC.getMapControl().getMap().getWorkspace();
            Datasource opendatasource = workspace.getDatasources().get("Label_"+userpath+"#");
            final DatasetVector dataset = (DatasetVector) opendatasource.getDatasets().get(name);
            final GeoStyle geoStyle = new GeoStyle();
            geoStyle.setFillForeColor(this.getFillColor());
            geoStyle.setFillBackColor(this.getFillColor());
            geoStyle.setMarkerSize(new Size2D(10, 10));
            geoStyle.setLineColor(new Color(0, 133, 255));
            geoStyle.setFillOpaqueRate(50);//加透明度更美观
            //geoStyle.setLineColor(new Color(0,206,209));
            mapControl.addGeometryAddedListener(new GeometryAddedListener() {
                @Override
                public void geometryAdded(GeometryEvent event) {
                    int id[] = new int[1];
                    id[0] = event.getID();
                    Recordset recordset = dataset.query(id, CursorType.DYNAMIC);
                    recordset.edit();
                    Geometry geometry = recordset.getGeometry();
                    if(geometry!=null) {
                        geometry.setStyle(geoStyle);
                        recordset.setGeometry(geometry);
                        recordset.update();
                    }
                }
            });
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 设置标注默认的结点，线，面颜色
     *
     * @param promise
     */
    @ReactMethod
    public void setLabelColor(/*ReadableMap readableMap,*/ Promise promise) {
        try {
            sMap = SMap.getInstance();
            MapControl mapControl = sMap.smMapWC.getMapControl();
            mapControl.setStrokeColor(0x3999FF);
//            mapControl.setStrokeFillColor();
            mapControl.setStrokeWidth(1);

            GeoStyle geoStyle_P = new GeoStyle();
//            geoStyle_P.setMarkerAngle(14.0);
//            geoStyle_P.setFillForeColor(new Color(0,133,255));
//            geoStyle_P.setLineColor(new Color(0,133,255));
//            geoStyle_P.setMarkerSize(new Size2D(10, 10));
//            geoStyle_P.setPointColor(new Color(0,133,255));
//            geoStyle_P.setMarkerSymbolID(322);
//            mapControl.setNodeStyle(geoStyle_P);

            Workspace workspace = mapControl.getMap().getWorkspace();
            Resources m_resources = workspace.getResources();
            SymbolMarkerLibrary symbol_M = m_resources.getMarkerLibrary();
            if (symbol_M.contains(322)) {
                geoStyle_P.setMarkerSymbolID(322);
                mapControl.setNodeStyle(geoStyle_P);
            } else if (symbol_M.contains(313)) {
                geoStyle_P.setMarkerSymbolID(313);
                mapControl.setNodeStyle(geoStyle_P);
            } else if (symbol_M.contains(321)) {
                geoStyle_P.setMarkerSymbolID(321);
                mapControl.setNodeStyle(geoStyle_P);
            } else {
                mapControl.setNodeColor(0x3999FF);
                mapControl.setNodeSize(2.0);
            }

            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }


    /**
     * 更新图例
     *
     * @param promise
     */
    @ReactMethod
    public void updateLegend(Promise promise){
        try {
            sMap = SMap.getInstance();
            MapControl mapControl = sMap.smMapWC.getMapControl();

            Layers layers = mapControl.getMap().getLayers();
            Map<String, String> map = new HashMap<String, String>();
            for(int i=0 ;i<layers.getCount();i++){
                Layer layer = layers.get(i);
                if(layer.getTheme()!=null){
                    if(layer.getTheme().getType()== ThemeType.RANGE || layer.getTheme().getType()== ThemeType.UNIQUE || layer.getTheme().getType()== ThemeType.GRIDRANGE){
                        if(layer.getTheme().getType()== ThemeType.RANGE){
                            ThemeRange themeRange = (ThemeRange) layer.getTheme();
                            for(int a=0;a<themeRange.getCount();a++){
                                GeoStyle GeoStyle = themeRange.getItem(a).getStyle();
                                map.put(themeRange.getItem(a).getCaption(), GeoStyle.getFillForeColor().toColorString());
                            }
                        }
                        if(layer.getTheme().getType()== ThemeType.UNIQUE){
                            ThemeUnique themeUnique = (ThemeUnique) layer.getTheme();
                            for(int a=0;a<themeUnique.getCount();a++){
                                GeoStyle GeoStyle = themeUnique.getItem(a).getStyle();
                                map.put(themeUnique.getItem(a).getCaption(), GeoStyle.getFillForeColor().toColorString());
                            }
                        }
                        if(layer.getTheme().getType()== ThemeType.GRIDRANGE){
                            ThemeGridRange themeGridRange = (ThemeGridRange) layer.getTheme();
                            for(int a=0;a<themeGridRange.getCount();a++){
                                map.put(themeGridRange.getItem(a).getCaption(), themeGridRange.getItem(a).getColor().toColorString());
                            }
                        }
                    }
                }
            }

            Legend lengend = mapControl.getMap().getLegend();

            for (Map.Entry<String, String> entry : map.entrySet()) {
                int color = android.graphics.Color.parseColor(entry.getValue());
                ColorLegendItem colorLegendItem = new ColorLegendItem();
                colorLegendItem.setColor(color);
                colorLegendItem.setCaption(entry.getKey());
                lengend.addColorLegendItem(2,colorLegendItem);
            }


            mapControl.getMap().refresh();

            promise.resolve(true);

        } catch (Exception e) {
            promise.reject(e);
        }
    }


    /**
     * 标绘动画
     *
     * @param promise
     */
    @ReactMethod
    public void plotAnimation(Promise promise) {

    }



/************************************** 地图编辑历史操作 BEGIN****************************************/

    /**
     * 把对地图操作记录到历史
     *
     * @param promise
     */
    @ReactMethod
    public void addMapHistory(Promise promise) {
        try {
            sMap = SMap.getInstance();
            MapControl mapControl = sMap.smMapWC.getMapControl();
            mapControl.getEditHistory().addMapHistory();
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 获取地图操作记录数量
     *
     * @param promise
     */
    @ReactMethod
    public void getMapHistoryCount(Promise promise) {
        try {
            sMap = SMap.getInstance();
            MapControl mapControl = sMap.smMapWC.getMapControl();
            int count = mapControl.getEditHistory().getCount();

            promise.resolve(count);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 获取地图操作记录当前index
     *
     * @param promise
     */
    @ReactMethod
    public void getMapHistoryCurrentIndex(Promise promise) {
        try {
            sMap = SMap.getInstance();
            MapControl mapControl = sMap.smMapWC.getMapControl();
            int index = mapControl.getEditHistory().getCurrentIndex();

            promise.resolve(index);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 地图操作记录重做到index
     *
     * @param index
     * @param promise
     */
    @ReactMethod
    public void redoWithIndex(int index, Promise promise) {
        try {
            sMap = SMap.getInstance();
            MapControl mapControl = sMap.smMapWC.getMapControl();
            boolean result = mapControl.getEditHistory().redo(index);

            promise.resolve(result);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 地图操作记录撤销到index
     *
     * @param index
     * @param promise
     */
    @ReactMethod
    public void undoWithIndex(int index, Promise promise) {
        try {
            sMap = SMap.getInstance();
            MapControl mapControl = sMap.smMapWC.getMapControl();
            boolean result = mapControl.getEditHistory().undo(index);

            promise.resolve(result);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 地图操作记录移除两个index之间的记录
     *
     * @param start
     * @param end
     * @param promise
     */
    @ReactMethod
    public void removeRange(int start, int end, Promise promise) {
        try {
            sMap = SMap.getInstance();
            MapControl mapControl = sMap.smMapWC.getMapControl();
            boolean result = mapControl.getEditHistory().removeRange(start, end);

            promise.resolve(result);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 地图操作记录移除index位置的记录
     *
     * @param promise
     */
    @ReactMethod
    public void remove(int index, Promise promise) {
        try {
            sMap = SMap.getInstance();
            MapControl mapControl = sMap.smMapWC.getMapControl();
            boolean result = mapControl.getEditHistory().remove(index);

            promise.resolve(result);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    /**
     * 清除地图操作记录
     *
     * @param promise
     */
    @ReactMethod
    public void clear(int index, Promise promise) {
        try {
            sMap = SMap.getInstance();
            MapControl mapControl = sMap.smMapWC.getMapControl();
            boolean result = mapControl.getEditHistory().clear();

            promise.resolve(result);
        } catch (Exception e) {
            promise.reject(e);
        }
    }
    /************************************** 地图编辑历史操作 END ****************************************/
}
