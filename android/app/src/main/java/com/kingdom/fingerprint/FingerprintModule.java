package com.kingdom.fingerprint;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableNativeMap;

import fingerprint.com.fingerprintrecognition.FingerprintUtil;
import fingerprint.com.fingerprintrecognition.KeyguardLockScreenManager;
import fingerprint.com.fingerprintrecognition.R;
import fingerprint.com.fingerprintrecognition.core.FingerprintCore;

@SuppressWarnings("MissingPermission")
public class FingerprintModule extends ReactContextBaseJavaModule {
    ReactApplicationContext reactContext;
    private FingerprintCore mFingerprintCore;
    private KeyguardLockScreenManager mKeyguardLockScreenManager;

    private Toast mToast;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Promise mPromise;


    public FingerprintModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        initFingerprintCore();
    }
    private void initFingerprintCore() {
        mFingerprintCore = new FingerprintCore(this.reactContext);
        mFingerprintCore.setFingerprintManager(mResultListener);
        mKeyguardLockScreenManager = new KeyguardLockScreenManager(this.reactContext);
    }
    private FingerprintCore.IFingerprintResultListener mResultListener = new FingerprintCore.IFingerprintResultListener() {
        @Override
        public void onAuthenticateSuccess() {
            toastTipMsg(R.string.fingerprint_recognition_success);
            WritableNativeMap writableNativeMap = new WritableNativeMap();
            writableNativeMap.putInt("code", 1);
            writableNativeMap.putString("message", "指纹识别成功！");
            mPromise.resolve(writableNativeMap);
            //resetGuideViewState();
        }

        @Override
        public void onAuthenticateFailed(int helpId) {
            toastTipMsg(R.string.fingerprint_recognition_failed);
            //mPromise.reject("0","指纹识别失败，请重试！");
            //mFingerGuideTxt.setText(R.string.fingerprint_recognition_failed);
        }

        @Override
        public void onAuthenticateError(int errMsgId) {
            //resetGuideViewState();
            //mPromise.reject("0","指纹识别错误，等待几秒之后再重试！");
            toastTipMsg(R.string.fingerprint_recognition_error);
        }

        @Override
        public void onStartAuthenticateResult(boolean isSuccess) {

        }
    };
    /**
     * 开始指纹识别
     */
    @ReactMethod
    public void startFingerprintRecognition(Promise promise) {
        WritableNativeMap writableNativeMap = new WritableNativeMap();
//            writableNativeMap.putInt("code", FINGERPRINT_ACQUIRED_AUTH_FAILED);
//            writableNativeMap.putString("message", "Fingerprint was recognized as not valid.");
        if (mFingerprintCore.isSupport()) {
            if (!mFingerprintCore.isHasEnrolledFingerprints()) {
                toastTipMsg(R.string.fingerprint_recognition_not_enrolled);
                FingerprintUtil.openFingerPrintSettingPage(this.reactContext);
                writableNativeMap.putInt("code", 0);
                writableNativeMap.putString("message", "您还没有录制指纹，请录入！");
                promise.resolve(writableNativeMap);
                return;
            }
            toastTipMsg(R.string.fingerprint_recognition_tip);
            //mFingerGuideTxt.setText(R.string.fingerprint_recognition_tip);
            //mFingerGuideImg.setBackgroundResource(R.drawable.fingerprint_guide);
            if (mFingerprintCore.isAuthenticating()) {
                toastTipMsg(R.string.fingerprint_recognition_authenticating);
            } else {
                mPromise = promise;
                mFingerprintCore.startAuthenticate();
            }
        } else {
            toastTipMsg(R.string.fingerprint_recognition_not_support);
            writableNativeMap.putInt("code", 0);
            writableNativeMap.putString("message", "此设备不支持指纹解锁");
            promise.resolve(writableNativeMap);
        }
    }
    /**
     * 取消指纹识别
     */
    @ReactMethod
    public void cancelFingerprintRecognition(Promise promise) {
        if (mFingerprintCore.isAuthenticating()) {
            mFingerprintCore.cancelAuthenticate();
            //resetGuideViewState();
        }
        WritableNativeMap writableNativeMap = new WritableNativeMap();
        writableNativeMap.putInt("code", 1);
        writableNativeMap.putString("message", "指纹识别取消成功！");
        promise.resolve(writableNativeMap);
    }
    /**
     *测试密码解锁屏幕
     */
    @ReactMethod
    public void startFingerprintRecognitionUnlockScreen(Promise promise) {
        if (mKeyguardLockScreenManager == null) {
            WritableNativeMap writableNativeMap = new WritableNativeMap();
            writableNativeMap.putInt("code", 1);
            writableNativeMap.putString("message", "初始化锁屏密码失败！");
            promise.resolve(writableNativeMap);
            return;
        }
        if (!mKeyguardLockScreenManager.isOpenLockScreenPwd()) {
            toastTipMsg(R.string.fingerprint_not_set_unlock_screen_pws);
            FingerprintUtil.openFingerPrintSettingPage(this.reactContext);
            return;
        }
        Activity activity = this.getCurrentActivity();

        mKeyguardLockScreenManager.showAuthenticationScreen(activity);
        reactContext.addActivityEventListener(mActivityEventListener);
        mPromise  = promise;
    }

    private final ActivityEventListener mActivityEventListener = new BaseActivityEventListener() {

        @Override
        public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
            super.onActivityResult(activity, requestCode, resultCode, data);
            if (requestCode == KeyguardLockScreenManager.REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS) {
                // Challenge completed, proceed with using cipher
                WritableNativeMap writableNativeMap = new WritableNativeMap();
                if (resultCode == Activity.RESULT_OK) {
                    writableNativeMap.putInt("code", 1);
                    writableNativeMap.putString("message", "系统密码识别成功！");
                    toastTipMsg(R.string.sys_pwd_recognition_success);
                    mPromise.resolve(writableNativeMap);
                } else {
                    toastTipMsg(R.string.sys_pwd_recognition_failed);
                }

            }
        }
    };
    /**
     *打开系统设置手势密码页面
     */
    @ReactMethod
    public void enterSysFingerprintSettingPage() {
        FingerprintUtil.openFingerPrintSettingPage(this.reactContext);
    }
    private void toastTipMsg(int messageId) {
        try{
            if (mToast == null) {
                mToast = Toast.makeText(this.reactContext, messageId, Toast.LENGTH_SHORT);
            }
            mToast.setText(messageId);
            mToast.cancel();
            mHandler.removeCallbacks(mShowToastRunnable);
            mHandler.postDelayed(mShowToastRunnable, 0);
        }catch (Exception e){

        }

    }

    private void toastTipMsg(String message) {
        if (mToast == null) {
            mToast = Toast.makeText(this.reactContext, message, Toast.LENGTH_LONG);
        }
        mToast.setText(message);
        mToast.cancel();
        mHandler.removeCallbacks(mShowToastRunnable);
        mHandler.postDelayed(mShowToastRunnable, 200);
    }

    private Runnable mShowToastRunnable = new Runnable() {
        @Override
        public void run() {
            mToast.show();
        }
    };





    @Override
    public String getName() {
        return "FingerprintAndroid";
    }


}
