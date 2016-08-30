## Android-automation-library - a light, common android ui automaton library

Currently android uiautomator library doesn't support Toast and PopupWindow,this library used to support android ui automaton such as identify Toast and PopupWindow
* Tags: Appium, uiautomator, Toast, PopupWindow, Automation, Test,Android

## Features
* Support HierarchyViewer on real device
* Identify Toast
* Identify PopupWindow
* Highlight Element

## Usage
##### 1. Add gradle dependency in target app
```groovy
dependencies {
    compile 'com.github.lwfwind.automation:android-automation-library:2.1'
}
```

##### 2. Add below in onCreate method of Application class

```java
public class MyApplication extends MultiDexApplication {
     public void onCreate() {
            super.onCreate();
    
            //automation
            if (EnvConfig.currentEnv() != EnvConfig.ENV_MASTER) {
                AutomationServer.startListening(this);
            }
    }
}
```

