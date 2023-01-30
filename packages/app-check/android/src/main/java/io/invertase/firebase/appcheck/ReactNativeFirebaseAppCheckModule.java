package io.invertase.firebase.appcheck;

/*
 * Copyright (c) 2016-present Invertase Limited & Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this library except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import com.facebook.react.bridge.*;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.AppCheckProviderFactory;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory;
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory;
import io.invertase.firebase.common.ReactNativeFirebaseModule;
import io.invertase.firebase.common.ReactNativeFirebaseJSON;
import io.invertase.firebase.common.ReactNativeFirebaseMeta;
import io.invertase.firebase.common.ReactNativeFirebasePreferences;
import java.lang.reflect.*;

public class ReactNativeFirebaseAppCheckModule extends ReactNativeFirebaseModule {
  private static final String TAG = "AppCheck";
  private static final String LOGTAG = "RNFBAppCheck";
  private static final String KEY_APPCHECK_TOKEN_REFRESH_ENABLED = "app_check_token_auto_refresh";

  static boolean isAppCheckTokenRefreshEnabled() {
    boolean enabled;
    ReactNativeFirebaseJSON json = ReactNativeFirebaseJSON.getSharedInstance();
    ReactNativeFirebaseMeta meta = ReactNativeFirebaseMeta.getSharedInstance();
    ReactNativeFirebasePreferences prefs = ReactNativeFirebasePreferences.getSharedInstance();

    if (prefs.contains(KEY_APPCHECK_TOKEN_REFRESH_ENABLED)) {
      enabled = prefs.getBooleanValue(KEY_APPCHECK_TOKEN_REFRESH_ENABLED, true);
      Log.d(LOGTAG, "isAppCheckCollectionEnabled via RNFBPreferences: " + enabled);
    } else if (json.contains(KEY_APPCHECK_TOKEN_REFRESH_ENABLED)) {
      enabled = json.getBooleanValue(KEY_APPCHECK_TOKEN_REFRESH_ENABLED, true);
      Log.d(LOGTAG, "isAppCheckCollectionEnabled via RNFBJSON: " + enabled);
    } else {
      enabled = meta.getBooleanValue(KEY_APPCHECK_TOKEN_REFRESH_ENABLED, true);
      Log.d(LOGTAG, "isAppCheckCollectionEnabled via RNFBMeta: " + enabled);
    }

    if (BuildConfig.DEBUG) {
      if (!json.getBooleanValue(KEY_APPCHECK_TOKEN_REFRESH_ENABLED, false)) {
        enabled = false;
      }
      Log.d(
          LOGTAG,
          "isAppCheckTokenRefreshEnabled after checking "
              + KEY_APPCHECK_TOKEN_REFRESH_ENABLED
              + ": "
              + enabled);
    }

    Log.d(LOGTAG, "isAppCheckTokenRefreshEnabled final value: " + enabled);
    return enabled;
  }

  private boolean isAppDebuggable() throws Exception {
      boolean isDebuggable = false;
      PackageManager pm = getContext().getPackageManager();
      if (pm != null) {
        isDebuggable =
            (0
                != (pm.getApplicationInfo(getContext().getPackageName(), 0).flags
                    & ApplicationInfo.FLAG_DEBUGGABLE));
      }
      Log.d(LOGTAG, "debuggable status? " + isDebuggable);
      return isDebuggable;
  }

  ReactNativeFirebaseAppCheckModule(ReactApplicationContext reactContext) {
    super(reactContext, TAG);
  }

  @ReactMethod
  public void activate(
      String appName, String siteKeyProvider, boolean isTokenAutoRefreshEnabled, Promise promise) {
    try {
      FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
      firebaseAppCheck.setTokenAutoRefreshEnabled(isTokenAutoRefreshEnabled);


      if (isAppDebuggable()) {
        Log.d(LOGTAG, "app is debuggable, configuring AppCheck for testing mode");
        if (BuildConfig.FIREBASE_APP_CHECK_DEBUG_TOKEN != "null") {
          Log.d(LOGTAG, "debug app check token found in BuildConfig. Installing known token.");
          // Get DebugAppCheckProviderFactory class
          Class<DebugAppCheckProviderFactory> debugACFactoryClass =
              DebugAppCheckProviderFactory.class;

          // Get the (undocumented) constructor accepting a debug token as string
          Class<?>[] argType = {String.class};
          Constructor c = debugACFactoryClass.getDeclaredConstructor(argType);

          // Create a object containing the constructor arguments
          // and initialize a new instance.
          Object[] cArgs = {BuildConfig.FIREBASE_APP_CHECK_DEBUG_TOKEN};
          firebaseAppCheck.installAppCheckProviderFactory(
              (AppCheckProviderFactory) c.newInstance(cArgs));
        } else {
          Log.d(LOGTAG, "no debug app check token found in BuildConfig. Check Log for dynamic test token to configure in console.");
          firebaseAppCheck.installAppCheckProviderFactory(
              DebugAppCheckProviderFactory.getInstance());
        }

      } else {
        firebaseAppCheck.installAppCheckProviderFactory(
            SafetyNetAppCheckProviderFactory.getInstance());
      }
    } catch (Exception e) {
      rejectPromiseWithCodeAndMessage(promise, "unknown", "internal-error", "unimplemented");
      return;
    }
    promise.resolve(null);
  }

  @ReactMethod
  public void setTokenAutoRefreshEnabled(String appName, boolean isTokenAutoRefreshEnabled) {
    FirebaseApp firebaseApp = FirebaseApp.getInstance(appName);
    FirebaseAppCheck.getInstance(firebaseApp).setTokenAutoRefreshEnabled(isTokenAutoRefreshEnabled);
  }

  @ReactMethod
  public void getToken(String appName, boolean forceRefresh, Promise promise) {
    FirebaseApp firebaseApp = FirebaseApp.getInstance(appName);
    Tasks.call(
            getExecutor(),
            () -> {
              return Tasks.await(
                  FirebaseAppCheck.getInstance(firebaseApp).getAppCheckToken(forceRefresh));
            })
        .addOnCompleteListener(
            getExecutor(),
            (task) -> {
              if (task.isSuccessful()) {
                WritableMap tokenResultMap = Arguments.createMap();
                tokenResultMap.putString("token", task.getResult().getToken());
                promise.resolve(tokenResultMap);
              } else {
                Log.e(
                    LOGTAG,
                    "RNFB: Unknown error while fetching AppCheck token "
                        + task.getException().getMessage());
                rejectPromiseWithCodeAndMessage(
                    promise, "token-error", task.getException().getMessage());
              }
            });
  }
}
