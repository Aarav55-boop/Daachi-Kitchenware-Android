# Daachi™ Kitchenware — Android WebView App

- Website: https://daachi.co.in/
- Android package: `com.daachi.aap`
- Kotlin, min SDK 24
- Red splash screen with supplied camel logo, white text, tagline ALWAYS HAPPINESS
- Loads existing website; does not create a separate product catalog or backend

## Open in VS Code
Extract this ZIP and open the **Daachi-Kitchenware-Android** folder. It is a full Android Gradle project (not a web page).

## Build APK without downloading Gradle on your laptop
1. Create a GitHub repository and upload **the contents** of this folder, including `.github/workflows/build-apk.yml`.
2. Open repository → **Actions** → **Build Daachi Android APK** → **Run workflow**.
3. After success, open the run → **Artifacts** → download `Daachi-Kitchenware-debug-APK`.
4. Unzip artifact to obtain `app-debug.apk` for testing on your Android phone.

**Important:** Debug APK is for testing, not Play Store release. Play Store needs a securely signed release AAB, developer account and policy review. Do not upload a release signing keystore to a public repository.

## Checkout and payment testing
WebView can display your website, but real cart/checkout, login, OTP, payment gateway redirects, UPI app switching, file uploads and Boost360 order delivery must be tested on a physical phone. Some payment providers prohibit or do not support embedded WebView flows; those may require opening checkout in the external browser or a provider-supported SDK. This project does not guarantee checkout compatibility or Play Store approval.

## Notes
- Requires internet access.
- No access to the Boost360 admin dashboard is built in; customers see the public storefront.
- For Play Store publication, ensure the app provides acceptable user value beyond a minimal website wrapper and complies with current Google Play policies.


## Welcome design update
Red camel splash, white welcome screen with shopping illustration, START SHOPPING button. Welcome is shown once; later launches load the website.
