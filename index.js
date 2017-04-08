'use strict'
import {
  NativeModules, 
  NativeEventEmitter
} from 'react-native';

export const {FingerprintAndroid} =  NativeModules;
export const startFingerprintRecognition = Fingerprint.startFingerprintRecognition;
export const cancelFingerprintRecognition = Fingerprint.cancelFingerprintRecognition;
export const startFingerprintRecognitionUnlockScreen = Fingerprint.startFingerprintRecognitionUnlockScreen;
export const enterSysFingerprintSettingPage = Fingerprint.enterSysFingerprintSettingPage;
