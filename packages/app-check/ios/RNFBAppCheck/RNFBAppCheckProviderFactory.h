// Facade for multiple possible provider factory delegates, configurable dynamically instead of at startup

// Constructor does basically nothing?

// Implement configureProvider method:
// - determine which provider factory is desired via required arg
// - determine if token refresh is enabled via required arg
// - determine if debugToken is provided via nullable arg
// - instantiate correct provider and hold reference
//   - debug provider:
//     - maybe directly setting an environment variable could work? https://stackoverflow.com/questions/27139589/whats-the-idiomatic-way-of-setting-an-environment-variable-in-objective-c-coco
//     - ...otherwise if env var does not work
//       - subclass style: RNFBAppCheckDebugProvider, and we should print local token
//       - if a debugToken parameter was supplied, set RNFBAppCheckDebugProvider.configuredDebugToken
//     - print local token  https://github.com/firebase/firebase-ios-sdk/blob/c7e95996ff/FirebaseAppCheck/Sources/DebugProvider/FIRAppCheckDebugProviderFactory.m
//     - print if current token in provided by configuration, by environment variable, or local token?
//   - DeviceCheck provider is trivial to instantiate
//   - AppAttest requires an iOS14 check and should return an error if <14? https://firebase.google.com/docs/app-check/ios/app-attest-provider#objective-c_1
//     - maybe appAttestWithFallback ? (and fallback to DeviceCheck?)
