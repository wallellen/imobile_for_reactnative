import { NativeModules, Platform, NativeEventEmitter, DeviceEventEmitter} from 'react-native';
import { EventConst } from '../../constains'
let IPortalServiceNative = NativeModules.SIPortalService;
const callBackIOS = new NativeEventEmitter(IPortalServiceNative)
let iPortalUrl = undefined
function init() {
    if(Platform.OS === 'ios') {
        return IPortalServiceNative.init()
    }
}

function login (serverUrl, userName, password, remember) {
    iPortalUrl = serverUrl
    return IPortalServiceNative.login(serverUrl, userName, password, remember)
}

function logout () {
  if(Platform.OS === 'android') {
      let logoutUrl = iPortalUrl
      if(iPortalUrl.lastIndexOf('web') !== -1) {
        logoutUrl = iPortalUrl.substr(0, iPortalUrl.lastIndexOf('web')) + 'services'
      }
      IPortalServiceNative.logout(logoutUrl)
  } else{
    IPortalServiceNative.logout()
  }
}

function getIPortalCookie() {
    if(Platform.OS === 'android') {
        return IPortalServiceNative.getIPortalCookie()
    }
}

function getIPortalUrl() {
    return iPortalUrl
}

function getMyAccount() {
    return IPortalServiceNative.getMyAccount()
}

function getMyDatas(page, pageSize) {
    return IPortalServiceNative.getMyDatas(page, pageSize)
}

function deleteMyData(id) {
    return IPortalServiceNative.deleteMyData(id)
}

let  uploadingFileListener, uploadFileListener
function uploadData (path, fileName, cb) {
  uploadingFileListener && uploadingFileListener.remove()
  uploadFileListener && uploadFileListener.remove()
  if(Platform.OS === 'ios'){
      if(cb && typeof cb.onProgress === 'function' ) {
        uploadingFileListener = callBackIOS.addListener(EventConst.IPORTAL_SERVICE_UPLOADING, function (progress) {
            cb.onProgress(progress)
        })
      }
      if (cb && typeof cb.onResult === 'function') {
        uploadFileListener = callBackIOS.addListener(EventConst.IPORTAL_SERVICE_UPLOADED, function (value) {
          cb.onResult(value)
        })
      }
  } else {
    if (cb && typeof cb.onProgress === 'function') {
        uploadingFileListener = DeviceEventEmitter.addListener(EventConst.IPORTAL_SERVICE_UPLOADING, function (progress) {
          cb.onProgress(progress)
        })
      }
      if (cb && typeof cb.onResult === 'function') {
        uploadFileListener = DeviceEventEmitter.addListener(EventConst.IPORTAL_SERVICE_UPLOADED, function (result) {
          cb.onResult(result)
        })
      }
  }
      
  return IPortalServiceNative.uploadData(path, fileName)
}

function uploadDataByType (path, fileName,dataType, cb) {
    uploadingFileListener && uploadingFileListener.remove()
    uploadFileListener && uploadFileListener.remove()
    if(Platform.OS === 'ios'){
        if(cb && typeof cb.onProgress === 'function' ) {
          uploadingFileListener = callBackIOS.addListener(EventConst.IPORTAL_SERVICE_UPLOADING, function (progress) {
              cb.onProgress(progress)
          })
        }
        if (cb && typeof cb.onResult === 'function') {
          uploadFileListener = callBackIOS.addListener(EventConst.IPORTAL_SERVICE_UPLOADED, function (value) {
            cb.onResult(value)
          })
        }
    } else {
      if (cb && typeof cb.onProgress === 'function') {
          uploadingFileListener = DeviceEventEmitter.addListener(EventConst.IPORTAL_SERVICE_UPLOADING, function (progress) {
            cb.onProgress(progress)
          })
        }
        if (cb && typeof cb.onResult === 'function') {
          uploadFileListener = DeviceEventEmitter.addListener(EventConst.IPORTAL_SERVICE_UPLOADED, function (result) {
            cb.onResult(result)
          })
        }
    }
        
    return IPortalServiceNative.uploadDataByType(path, fileName,dataType)
  }

let downloadListener
function downloadMyData(path, id, cb) {
    downloadListener && downloadListener.remove()
    if(Platform.OS === 'android') {
        downloadListener = DeviceEventEmitter.addListener(EventConst.IPORTAL_SERVICE_DOWNLOADING, function (progress) {
            cb(progress);
          })
    } else {
        downloadListener = callBackIOS.addListener(EventConst.IPORTAL_SERVICE_DOWNLOADING, function (progress) {
            cb(progress);
          })
    }
    return IPortalServiceNative.downloadMyData(path, id)
}

function downloadMyDataByName(path, name, cb) {
    downloadListener && downloadListener.remove()
    if(Platform.OS === 'android') {
        downloadListener = DeviceEventEmitter.addListener(EventConst.IPORTAL_SERVICE_DOWNLOADING, function (progress) {
            cb(progress);
          })
    } else {
        downloadListener = callBackIOS.addListener(EventConst.IPORTAL_SERVICE_DOWNLOADING, function (progress) {
            cb(progress);
          })
    }
    return IPortalServiceNative.downloadMyDataByName(path, name)
}

function getMyServices(page, pageSize) {
    return IPortalServiceNative.getMyServices(page, pageSize)
}

function deleteMyService(id) {
    return IPortalServiceNative.deleteMyService(id)
}

function publishService(id) {
    if(Platform.OS === 'ios') {
    return IPortalServiceNative.publishService(id)
    }
}

function setServicesShareConfig(id, isPublic) {
    if(Platform.OS === 'ios') {
    return IPortalServiceNative.setServicesShareConfig(id, isPublic)
    }
}
export default {
    init,
    login,
    logout,
    getIPortalCookie,
    getIPortalUrl,
    getMyAccount,
    getMyDatas,
    getMyServices,
    deleteMyData,
    deleteMyService,
    publishService,
    setServicesShareConfig,
    uploadData,
    uploadDataByType,
    downloadMyData,
    downloadMyDataByName,
  }