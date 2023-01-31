package io.invertase.firebase.appcheck;

/*
 * Copyright (c) 2023-present Invertase Limited & Contributors
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

 import android.util.Log;

import com.google.android.gms.tasks.Task;

import com.google.firebase.appcheck.AppCheckProvider;
import com.google.firebase.appcheck.AppCheckToken;

public class ReactNativeFirebaseAppCheckProvider implements AppCheckProvider {
  private static final String LOGTAG = "RNFBAppCheck";

// Facade for multiple possible provider factory delegates, configurable dynamically instead of at startup

// Constructor does basically nothing? TODO verify this assertion for Android
// -- https://firebase.google.com/docs/app-check/android/custom-provider

// TODO determine how to instantiate+configure this object, from javascript through to native

// Implement configureProvider method:
// - determine which provider factory is desired via required arg
// - determine if token refresh is enabled via required arg
// - determine if debugToken is provided via nullable arg
// - instantiate correct provider and hold reference
//   - debug provider:
//     - TODO determine how to configure debug token, and how to print it
//   - SafetyNet provider is trivial to instantiate
//   - AppAttest requires an iOS14 check and should return an error if <14? https://firebase.google.com/docs/app-check/ios/app-attest-provider#objective-c_1
//     - maybe appAttestWithFallback ? (and fallback to DeviceCheck?)

    @Override
    public Task<AppCheckToken> getToken() {
      // TODO - delegate to the native provider we are currently configured for
      return null;
    }

    public void configure(String providerName, String debugToken) {
      Log.d(LOGTAG, "Provider::configure with providerName/debugToken: " + providerName + "/(not shown)");
    }

}