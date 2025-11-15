# Face Authentication Limitation

## Issue
On Android devices where both fingerprint and face unlock are enrolled, when requesting face authentication using `BIOMETRIC_WEAK`, the system may still show the fingerprint prompt instead of face unlock.

## Root Cause
This is a limitation of Android's `BiometricPrompt` API. The API doesn't provide a way to force face-only authentication when both biometric types are enrolled. The system decides which biometric to show, and on many devices, it prefers fingerprint.

## Current Implementation
The library uses `BIOMETRIC_WEAK` for face authentication, which should prefer face unlock, but:
- On devices with only face enrolled: Works correctly, shows face unlock
- On devices with both enrolled: May show fingerprint (system decision)
- On devices with only fingerprint: Will show fingerprint (expected)

## Workarounds
1. **Check device capabilities**: The library checks if face is available before attempting
2. **User guidance**: Inform users that on devices with both biometrics, the system may show fingerprint
3. **Device-specific**: Some manufacturers (Samsung, etc.) may handle this differently

## Android API Limitation
The `BiometricPrompt` API doesn't provide:
- A way to query which specific biometrics are enrolled
- A way to force face-only when both are available
- Direct control over which biometric prompt is shown

## Recommendations
1. For apps requiring face-only: Check if fingerprint is also available and inform the user
2. Consider using PIN/pattern as an alternative if face-only is critical
3. Test on actual devices to see manufacturer-specific behavior

## Future Improvements
- Monitor Android API updates for better biometric control
- Consider using manufacturer-specific APIs if available (Samsung Pass, etc.)
- Provide better user feedback about which biometric will be used
