import {
    NativeModules,
    DeviceEventEmitter,
    NativeEventEmitter,
    Platform
} from 'react-native'
import {
    EventConst
} from '../../constains/index'
import SSceneTool from './SSceneTool'
let SScene = NativeModules.SScene
const nativeEvt = new NativeEventEmitter(SScene);
export default (function () {
    /**
     * 打开工作空间
     * @param infoDic
     * @returns {Promise}
     */
    function openWorkspace(infoDic) {
        try {
            const type = infoDic.server.split('.').pop()
            Object.assign(infoDic, {
                type: getWorkspaceType(type)
            })
            return SScene.openWorkspace(infoDic)
        } catch (e) {
            console.error(e)
        }
    }

    function openMap(name) {
        try {
            return SScene.openMap(name)
        } catch (error) {
            console.log(error)
        }
    }

    function getMapList() {
        try {
            return SScene.getMapList()
        } catch (error) {
            console.log(error)
        }
    }

    function getLayerList() {
        try {
            return SScene.getLayerList()
        } catch (error) {
            console.log(error)
        }
    }


    function getTerrainLayerList() {
        try {
            return SScene.getTerrainLayerList()
        } catch (error) {
            console.log(error)
        }
    }

    function setTerrainLayerListVisible(name, value) {
        try {
            return SScene.setTerrainLayerListVisible(name, value)
        } catch (error) {
            console.log(error)
        }
    }

    function setVisible(name, value) {
        try {
            return SScene.setVisible(name, value)
        } catch (error) {
            console.log(error)
        }
    }

    function setSelectable(name, value) {
        try {
            return SScene.setSelectable(name, value)
        } catch (error) {
            console.log(error)
        }
    }

    function closeWorkspace() {
        try {
            return SScene.closeWorkspace()
        } catch (error) {
            console.log(error)
        }
    }

    function saveWorkspace(){
        try {
            return SScene.saveWorkspace()
        } catch (error) {
            console.log(error)
        }
    }

    function getFlyRouteNames() {
        try {
            return SScene.getFlyRouteNames()
        } catch (error) {
            console.log(error)
        }
    }

    function setPosition(index) {
        try {
            return SScene.setPosition(index)
        } catch (error) {
            console.log(error)
        }
    }

    function flyStart() {
        try {
            return SScene.flyStart()
        } catch (error) {
            console.log(error)
        }
    }

    function flyPause() {
        try {
            return SScene.flyPause()
        } catch (error) {
            console.log(error)
        }
    }

    function flyPauseOrStart() {
        try {
            return SScene.flyPauseOrStart()
        } catch (error) {
            console.log(error)
        }
    }

    function flyStop() {
        try {
            return SScene.flyStop()
        } catch (error) {
            console.log(error)
        }
    }

    function getFlyProgress(handlers) {
        try {
            if (Platform.OS === 'ios' && handlers) {
                if (typeof handlers.callback === 'function') {
                    nativeEvt.addListener(EventConst.SSCENE_FLY, function (e) {
                        handlers.callback(e)
                    })
                }
            } else if (Platform.OS === 'android' && handlers) {
                if (typeof handlers.callback === "function") {
                    DeviceEventEmitter.addListener(EventConst.SSCENE_FLY, function (e) {
                        handlers.callback(e);
                    });
                }
            }
            return SScene.getFlyProgress()
        } catch (error) {
            console.log(error)
        }
    }

    function zoom(scale) {
        try {
            return SScene.zoom(scale)
        } catch (error) {
            console.log(error)
        }
    }

    function setListener(){
        try {
            return SScene.setListener()
        } catch (error) {
            console.log(error)
        }
    }

    function getAttribute() {
        try {
            // if (Platform.OS === 'ios' && handlers) {
            //     if (typeof handlers.callback === 'function') {
            //         nativeEvt.addListener(EventConst.SSCENE_ATTRIBUTE, function (e) {
            //             handlers.callback(e)
            //         })
            //     }
            // } else if (Platform.OS === 'android' && handlers) {
            //     if (typeof handlers.callback === "function") {
            //         DeviceEventEmitter.addListener(EventConst.SSCENE_ATTRIBUTE, function (e) {
            //             handlers.callback(e)
            //         });
            //     }
            // }
            return SScene.getAttribute()
        } catch (error) {
            console.log(error)
        }
    }


    function addAttributeListener(handlers) {
        let listen
        if (Platform.OS === 'ios' && handlers) {
            if (typeof handlers.callback === 'function') {
                listen = nativeEvt.addListener(EventConst.SSCENE_ATTRIBUTE, function (e) {
                    handlers.callback(e)
                })
            }
        } else if (Platform.OS === 'android' && handlers) {
            if (typeof handlers.callback === "function") {
                listen = DeviceEventEmitter.addListener(EventConst.SSCENE_ATTRIBUTE, function (e) {
                    handlers.callback(e)
                });
            }
        }
        return listen
    }


    function setHeading() {
        try {
            return SScene.setHeading()
        } catch (error) {
            console.log(error)
        }
    }

    function clearSelection() {
        try {
            return SScene.clearSelection()
        } catch (error) {
            console.log(error)
        }
    }

    function initsymbol() {
        try {
            return SScene.initsymbol()
        } catch (error) {
            console.log(error)
        }
    }

    function startDrawPoint() {
        try {
            return SScene.startDrawPoint()
        } catch (error) {
            console.log(error)
        }
    }

    function startDrawLine() {
        try {
            return SScene.startDrawLine()
        } catch (error) {
            console.log(error)
        }
    }

    function startDrawArea() {
        try {
            return SScene.startDrawArea()
        } catch (error) {
            console.log(error)
        }
    }

    function startDrawText(handlers) {
        try {
            if (Platform.OS === 'ios' && handlers) {
                if (typeof handlers.callback === 'function') {
                    nativeEvt.addListener(EventConst.SSCENE_SYMBOL, function (e) {
                        handlers.callback(e)
                    })
                }
            } else if (Platform.OS === 'android' && handlers) {
                if (typeof handlers.callback === "function") {
                    DeviceEventEmitter.addListener(EventConst.SSCENE_SYMBOL, function (e) {
                        handlers.callback(e);
                    });
                }
            }
            return SScene.startDrawText()
        } catch (error) {
            console.log(error)
        }
    }

    function addGeoText(x,y,z,text) {
        try {
            return SScene.addGeoText(x, y,z,text)
        } catch (error) {
            console.log(error)
        }
    }

    function symbolback() {
        try {
            return SScene.symbolback()
        } catch (error) {
            console.log(error)
        }
    }

    function closeAllLabel() {
        try {
            return SScene.closeAllLabel()
        } catch (error) {
            console.log(error)
        }
    }

    function clearAllLabel() {
        try {
            return SScene.clearAllLabel()
        } catch (error) {
            console.log(error)
        }
    }

    function clearcurrentLabel() {
        try {
            return SScene.clearcurrentLabel()
        } catch (error) {
            console.log(error)
        }
    }

    function save() {
        try {
            return SScene.save()
        } catch (error) {
            console.log(error)
        }
    }

    function setAllLayersSelection(value) {
        try {
            return SScene.setAllLayersSelection(value)
        } catch (error) {
            console.log(error)
        }
    }

    function ensureVisibleLayer(name) {
        try {
            return SScene.ensureVisibleLayer(name)
        } catch (error) {
            console.log(error)
        }
    }

    // function addTerrainLayer(url, name) {
    //     try {
    //         return SScene.addTerrainLayer(url, name)
    //     } catch (error) {
    //         console.log(error)
    //     }
    // }

    function addTerrainCacheLayer(path, name) {
        try {
            return SScene.addTerrainCacheLayer(path, name)
        } catch (error) {
            console.log(error)
        }
    }
    function removeTerrainCacheLayer(name) {
        try {
            return SScene.removeTerrainCacheLayer(name)
        } catch (error) {
            console.log(error)
        }
    }
    function addImageCacheLayer(path, name) {
        try {
            return SScene.addImageCacheLayer(path, name)
        } catch (error) {
            console.log(error)
        }
    }
    //type: 1 tianditu 2 bingmap
    function changeBaseLayer(type) {
        try {
            return SScene.changeBaseLayer(type)
        } catch (error) {
            console.log(error)
        }
    }
    
    function removeImageCacheLayer(name) {
        try {
            return SScene.removeImageCacheLayer(name)
        } catch (error) {
            console.log(error)
        }
    }
    function getTerrainCacheNames() {
        try {
            return SScene.getTerrainCacheNames()
        } catch (error) {
            console.log(error)
        }
    }

    function setTerrainCacheName(vaue) {
        try {
            return SScene.setTerrainCacheName(vaue)
        } catch (error) {
            console.log(error)
        }
    }

    function getImageCacheNames() {
        try {
            return SScene.getImageCacheNames()
        } catch (error) {
            console.log(error)
        }
    }

    function setImageCacheName(value) {
        try {
            return SScene.setImageCacheName(value)
        } catch (error) {
            console.log(error)
        }
    }

    function addLayer3D(Url, Layer3DType, layerName, imageFormatType, dpi, addToHead,token) {
        try {
            // console.log(SScene.addLayer3D)
            return SScene.addLayer3D( Url, Layer3DType, layerName, imageFormatType, dpi, addToHead,token)
        } catch (error) {
            console.log(error)
        }
    }

    function startDrawFavorite(text,handlers) {
        try {
            if (Platform.OS === 'ios' && handlers) {
                if (typeof handlers.callback === 'function') {
                    nativeEvt.addListener(EventConst.SSCENE_FAVORITE, function (e) {
                        handlers.callback(e)
                    })
                }
            } else if (Platform.OS === 'android' && handlers) {
                if (typeof handlers.callback === "function") {
                    DeviceEventEmitter.addListener(EventConst.SSCENE_FAVORITE, function (e) {
                        handlers.callback(e);
                    });
                }
            }
            return SScene.startDrawFavorite(text)
        } catch (error) {
            console.log(error)
        }
    }

    function setFavoriteText(content) {
        try {
            return SScene.setFavoriteText(content)
        } catch (error) {
            console.log(error)
        }
    }
    
    function getcompass(){
        try {
            return SScene.getcompass()
        } catch (error) {
            console.log(error)
        }
    }

    function checkoutListener(event){
        try {
            return SScene.checkoutListener(event)
        } catch (error) {
            console.log(error)
        }
    }
    
    function setCircleFly(){
        try {
            return SScene.getFlyPoint()
        } catch (error) {
            console.log(error)
        }
    }

    function addCircleFlyListen(handlers){
        let listen
        if (Platform.OS === 'ios' && handlers) {
            if (typeof handlers.callback === 'function') {
                listen=nativeEvt.addListener(EventConst.SSCENE_CIRCLEFLY, function (e) {
                    handlers.callback(e)
                })
            }
        } else if (Platform.OS === 'android' && handlers) {
            if (typeof handlers.callback === "function") {
                listen= DeviceEventEmitter.addListener(EventConst.SSCENE_CIRCLEFLY, function (e) {
                    handlers.callback(e);
                });
            }
        }
        return listen
    }
    
    function startCircleFly(){
        try {
            return SScene.setCircleFly()
        } catch (error) {
            console.log(error)
        }
    }

    function stopCircleFly(){
        try {
            return SScene.stopCircleFly()
        } catch (error) {
            console.log(error)
        }
    }

    function clearCirclePoint(){
        try {
            return SScene.clearCirclePoint()
        } catch (error) {
            console.log(error)
        }
    }

     function clearLineAnalyst(){
        return SScene.setMeasureLineAnalyst()
      }
      
       function clearSquareAnalyst(){
        return SScene.setMeasureSquareAnalyst()
      }
      
       function setMeasureLineAnalyst(handlers) {
        try {
          if (Platform.OS === 'ios' && handlers) {
            if (typeof handlers.callback === 'function') {
               this.LineAnalystListener=nativeEvt.addListener(EventConst.ANALYST_MEASURELINE, function (e) {
                handlers.callback(e)
              })
            }
          } else if (Platform.OS === 'android' && handlers) {
            if (typeof handlers.callback === "function") {
                this.LineAnalystListener=DeviceEventEmitter.addListener(EventConst.ANALYST_MEASURELINE, function (e) {
                handlers.callback(e);
              });
            }
          }
          return SScene.setMeasureLineAnalyst()
        } catch (e) {
          console.error(e);
        }
      }
      
      
       function setMeasureSquareAnalyst(handlers) {
        try {
          if (Platform.OS === 'ios' && handlers) {
            if (typeof handlers.callback === 'function') {
              this.SquareAnalystListener=nativeEvt.addListener(EventConst.ANALYST_MEASURESQUARE, function (e) {
                handlers.callback(e)
              })
            }
          } else if (Platform.OS === 'android' && handlers) {
            if (typeof handlers.callback === "function") {
                this.SquareAnalystListener=DeviceEventEmitter.addListener(EventConst.ANALYST_MEASURESQUARE, function (e) {
                handlers.callback(e);
              });
            }
          }
          return SScene.setMeasureSquareAnalyst()
        } catch (e) {
          console.error(e);
        }
      }
      
      
       function closeAnalysis() {
        try {
            this.LineAnalystListener&&this.LineAnalystListener.remove()
            this.LineAnalystListener=null
            this.SquareAnalystListener&&this.SquareAnalystListener.remove()
            this.SquareAnalystListener=null
            return SScene.closeAnalysis()
        } catch (e) {
          console.error(e);
        }
      }

      function removeKMLOfWorkcspace(){
           try {
               return SScene.removeKMLOfWorkcspace()
           } catch (e) {
            console.error(e);
           }
      }

      function doZipFiles(fileList,toPath){
          try {
              return SScene.doZipFiles(fileList,toPath)
          } catch (error) {
            console.error(error);
          }
      }

      function getWorkspacePath(){
          try {
              return SScene.getWorkspacePath()
          } catch (error) {
            console.error(error);
          }
      }

      function getLableAttributeList(){
          try {
              return SScene.getLableAttributeList()
          } catch (error) {
            console.error(error);
          }
      }
      
      function flyToFeatureById(){
          try {
              return SScene.flyToFeatureById()
          } catch (error) {
            console.error(error);
          }
      }

      function getSetting(){
          try {
              return SScene.getSetting()
          } catch (error) {
            console.error(error);
          }
      }

    function import3DWorkspace(infoDic,bPrivate=true){
        try {
          const type = infoDic.server.split('.').pop()
          Object.assign(infoDic, {
              type: getWorkspaceType(type)
          })
            return SScene.import3DWorkspace(infoDic,bPrivate)
        } catch (error) {
          console.error(error);
        }
    }

    function openScence(name,bPrivate=true){
        try {
            return SScene.openScence(name,bPrivate)
        } catch (error) {
          console.error(error); 
        }
    }
    
    function is3DWorkspace(infoDic){
        try {
            // console.warn("is3DWorkspace")
            // console.log(SScene)
            // return
          const type = infoDic.server.split('.').pop()
          Object.assign(infoDic, {
              type: getWorkspaceType(type)
          })
            return SScene.is3DWorkspace(infoDic)
        } catch (error) {
          console.error(error); 
        }
    }

    function setCustomerDirectory(path){
        try {
            return SScene.setCustomerDirectory(path)
        } catch (error) {
          console.error(error);  
        }
    }
    
    function export3DScenceName(strScenceName,strDesFolder,bPrivate=true){
        try {
            return SScene.export3DScenceName(strScenceName,strDesFolder,bPrivate)
        } catch (error) {
            console.error(error);
        }
    }

    function resetCamera(){
        try {
            return SScene.resetCamera()
        } catch (error) {
            console.error(error);
        }
    }

    
    function setNavigationControlVisible(value){
        try {
            return SScene.setNavigationControlVisible(value)
        } catch (error) {
            console.error(error);
        }
    }
    function setAction(action3d){
        try {
            return SScene.setAction(action3d)
        } catch (error) {
            console.error(error);
        }
    }
    
    function getAttributeByName(name){
        try {
            return SScene.getAttributeByName(name)
        } catch (error) {
            console.error(error);
        }
    }

    function saveCurrentRoutStop(){
        try {
            return SScene.saveCurrentRoutStop()
        } catch (error) {
            console.error(error);
        }
    }

    function saveRoutStop(){
        try {
            return SScene.saveRoutStop()
        } catch (error) {
            console.error(error);
        }
    }

    function pauseRoutStop(){
        try {
            return SScene.pauseRoutStop()
        } catch (error) {
            console.error(error);
        }
    }
    
    function clearRoutStops(){
        try {
            return SScene.clearRoutStops()
        } catch (error) {
            console.error(error);
        }
    }

    function initPointSearch(){
        try {
             return SScene.initPointSearch()
        } catch (error) {
            console.error(error);
        }
    }


    function pointSearch(key){
        try {
            return SScene.pointSearch(key)
        } catch (error) {
            console.error(error);
        }
    }

    function setPointSearchListener(handlers){
        try {
            if (Platform.OS === 'ios' && handlers) {
                if (typeof handlers.callback === 'function') {
                   nativeEvt.addListener(EventConst.POINTSEARCH_KEYWORDS, function (e) {
                    handlers.callback(e)
                  })
                }
              } else if (Platform.OS === 'android' && handlers) {
                if (typeof handlers.callback === "function") {
                    DeviceEventEmitter.addListener(EventConst.POINTSEARCH_KEYWORDS, function (e) {
                    handlers.callback(e);
                  });
                }
              }
        } catch (error) {
            console.error(error);
        }
    }

    function toLocationPoint(index){
        try {
            return SScene.toLocationPoint(index)
        } catch (error) {
            console.error(error);
        }
    }

    function savePoint(index,pointType){
        try {
            return SScene.savePoint(index,pointType)
        } catch (error) {
            console.error(error); 
        }
    }

    function navigationLine(){
        try {
            return  SScene.navigationLine()
        } catch (error) {
            console.error(error); 
        }
    }

  /**
   * box裁剪
   * @param obj = {
   *   startX:int
   *   startX:int
   *   endX:int
   *   endY:int
   *   clipInner:Bool
   *   layers:[]
   * }
   * @returns {*}
   */
    function clipByBox(obj){
      try {
        return  SScene.clipByBox(obj)
      } catch (error) {
        console.error(error);
      }
    }

  /**
   * 清除裁剪
   * @returns {*}
   */
  function clipSenceClear(){
      try {
        return  SScene.clipSenceClear()
      } catch (error) {
        console.error(error);
      }
    }

    /**
   * 刷新距离和面积
   * @returns {*}
   */
  function displayDistanceOrArea(points){
    try {
        let point3Ds=[]
        for(let i=0;i<points.length;i++){
            let point=JSON.parse(points[i])
            point3Ds.push(point)
        }
      return  SScene.displayDistanceOrArea(point3Ds)
    } catch (error) {
      console.error(error);
    }
  }

    getWorkspaceType = (type) => {
        var value
        switch (type) {
            case 'SMWU':
            case 'smwu':
                value = 9
                break
            case 'SXWU':
            case 'sxwu':
                value = 8
                break
            case 'SMW':
            case 'smw':
                value = 5
                break
            case 'SXW':
            case 'sxw':
                value = 4
                break
            case 'UDB':
            case 'udb':
                value = 219
                break
            default:
                value = 1
                break
        }
        return value
    }

  function open3DNavigationMap() {
    try {
      return SScene.open3DNavigationMap()
    } catch (error) {
      console.log(error)
    }
  }

    /**
     * 获取当前场景
     * @returns {*}
     */
  function getSceneCenter(){
      try {
          return SScene.getSceneCenter()
      }catch (e) {
          console.error(e)
      }
  }

  function getSceneXMLfromWorkspace(serverUrl) {
    try {
        return SScene.getSceneXMLfromWorkspace(serverUrl)
    } catch (error) {
        console.error(e)
    }
  }

    let SSceneExp = {
        getSceneCenter,
        openWorkspace,
        closeWorkspace,
        saveWorkspace,
        setVisible,
        getLayerList,
        getMapList,
        openMap,
        setSelectable,
        getFlyRouteNames,
        setPosition,
        flyStart,
        flyPause,
        flyPauseOrStart,
        flyStop,
        getFlyProgress,
        zoom,
        setListener,
        getAttribute,
        addAttributeListener,
        setTerrainLayerListVisible,
        getTerrainLayerList,
        setHeading,
        clearSelection,
        initsymbol,
        startDrawPoint,
        startDrawLine,
        startDrawArea,
        startDrawText,
        addGeoText,
        symbolback,
        closeAllLabel,
        clearAllLabel,
        clearcurrentLabel,
        save,
        setAllLayersSelection,
        changeBaseLayer,
        startDrawFavorite,
        setFavoriteText,
        getcompass,
        checkoutListener,
        setCircleFly,
        startCircleFly,
        stopCircleFly,
        clearCirclePoint,
        addCircleFlyListen,
        setMeasureLineAnalyst,
        setMeasureSquareAnalyst,
        closeAnalysis,
        clearLineAnalyst,
        clearSquareAnalyst,
        removeKMLOfWorkcspace,
        doZipFiles,
        getWorkspacePath,
        getLableAttributeList,
        flyToFeatureById,
        getSetting,
        import3DWorkspace,
        openScence,
        is3DWorkspace,
        setCustomerDirectory,
        export3DScenceName,
        resetCamera,
        setNavigationControlVisible,
        setAction,
        getAttributeByName,
        saveCurrentRoutStop,
        saveRoutStop,
        pauseRoutStop,
        clearRoutStops,
        initPointSearch,
        pointSearch,
        setPointSearchListener,
        toLocationPoint,
        savePoint,
        navigationLine,
        ensureVisibleLayer,
        getImageCacheNames,
        setImageCacheName,
        getTerrainCacheNames,
        setTerrainCacheName,
        addTerrainCacheLayer,
        addImageCacheLayer,
        removeTerrainCacheLayer,
        removeImageCacheLayer,
        clipByBox,
        clipSenceClear,
      open3DNavigationMap,
      displayDistanceOrArea,
      getSceneXMLfromWorkspace,
    }
    Object.assign(SSceneExp, SSceneTool)
    return SSceneExp
})()