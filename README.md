## Android-automation-library - a light, common android ui automaton library

Currently android uiautomator library doesn't support Toast and PopupWindow,this library used to support android ui automaton such as identify Toast and PopupWindow for Appium
* Tags: Appium, uiautomator, Toast, PopupWindow, Automation, Test, Android

## Features
* Identify Toast and PopupWindow
* Support HierarchyViewer on real device
* Collect statistics of first application launch time and all activity OnCreate/OnStart/OnResume time
* Collect all crash stack trace and logs

## Usage
##### 1. Add gradle dependency in target app
```groovy
dependencies {
    compile 'com.lwfwind:android-automation-library:1.0'
}
```

##### 2. Add one line in attachBaseContext method of Application class

```java
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
		
        //wind automation
		AutomationServer.install(this).enableStrictMode(true).enableCrashCatch(true).enableCollectDuration(true).setEmailTo("A@mail.com B@mail.com");
		
    }
```

