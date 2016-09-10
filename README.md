## Android-automation-library - a light, common android ui automaton library

Currently android uiautomator library doesn't support Toast and PopupWindow,this library used to support android ui automaton such as identify Toast and PopupWindow
* Tags: Appium, uiautomator, Toast, PopupWindow, Automation, Test, Android

## Features
* Support HierarchyViewer on real device
* Identify Toast and PopupWindow
* Collect statistics of first application launch time and all activity OnCreate/OnStart/OnResume time
* Collect all crash stack trace and logs

## Usage
##### 1. Add gradle dependency in target app
```groovy
dependencies {
    compile 'com.github.lwfwind.automation:android-automation-library:2.7'
}
```

##### 2. Add one line in attachBaseContext method of Application class

```java
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
		
        //wind automation
		AutomationServer.install(this);
		
    }
```

