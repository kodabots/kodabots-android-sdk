# Koda Bots SDK
## 1. Installation guide

1. Add our Maven Repository to your `settings.build.gradle`
```
dependencyResolutionManagement {  
    repositories {  
        google()  
        mavenCentral()
          
	    // Add these lines
        maven(  
            url = "https://europe-maven.pkg.dev/kodaai-mobile-sdk/kotlin-sdk"  
        )  
    }  
}
```

2. Add dependency to your project level `build.gradle` file
   `implementation("ai.koda.mobile.sdk:koda-core:<latest_version>") `

3. In your `AndroidManifest.xml` you need to add Client Token provided by KODA Bots as in example below
```
<application>
.
.
.
<meta-data android:name="ai.koda.mobile.sdk.ClientToken"  
    android:value="ADD_YOUR_KEY_HERE"/>
.
.
.
</application>
```

## 2. Usage guide
- In your Application class onCreate method plese invoke ```KodaBotsSDK.init(this)``` to initialize SDK
- Before you use our webview, call ```KodaBotsSDK.requestPermissions``` method to acquire required permissions from user
- We provide convenience method ```KodaBotsSDK.hasPermissions``` which will tell if permissions are granted
- To obtain fragment to display KODA Bots, you need to call ```KodaBotsSDK.generateFragment()```, you can also pass user profile, blockId, background color, progress color, custom lottie animation asset path, other configs and callback to your application with stat's events and error events
- ```KodaBotsSDK``` class also have deinitialize method which you need to call when you close application
- ```KodaBotsSDK``` provides calls to our REST API as function with callback or suspend function if you use Kotlin Coroutines

## 3. Methods overview
- getUnreadCount is available without initialization of webview, inside ```KodaBotsSDK```, returns unread count of messages
- sendBlock is available after initialization of webview, inside ```KodaBotsWebViewFragment```, returns true if send to webview, false if not initialized
- syncUserProfile is available after initialization of webview, inside ```KodaBotsWebViewFragment```, returns true if send to webview, false if not initialized


- Default loader: https://lottiefiles.com/36219-loader  
  Thanks to Sinan Özkök, creator of loader