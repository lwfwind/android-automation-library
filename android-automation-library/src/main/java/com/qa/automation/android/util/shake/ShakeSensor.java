package com.qa.automation.android.util.shake;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;


public class ShakeSensor implements SensorEventListener {

    public static final int DEFAULT_SHAKE_SPEED = 2000;
    private static final int UPTATE_INTERVAL_TIME = 100;
    protected final String TAG = this.getClass().getName();
    protected Context mContext = null;
    protected SensorManager mSensorManager = null;
    protected Sensor mSensor = null;
    protected OnShakeListener mShakeListener = null;
    private int mSpeedShreshold = DEFAULT_SHAKE_SPEED;
    private boolean isStart = false;

    private float mLastX = 0.0f;
    private float mLastY = 0.0f;
    private float mLastZ = 0.0f;

    private long mLastUpdateTime;

    public ShakeSensor(Context context) {
        this(context, ShakeSensor.DEFAULT_SHAKE_SPEED);
    }

    public ShakeSensor(Context context, int speedShreshold) {
        mContext = context;
        mSpeedShreshold = speedShreshold;
    }

    public boolean register() {
        mSensorManager = (SensorManager) mContext
                .getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager != null) {
            mSensor = mSensorManager
                    .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (mSensor != null) {
                isStart = mSensorManager.registerListener(this, mSensor,
                        SensorManager.SENSOR_DELAY_GAME);
            } else {
                Log.d(TAG, "### 传感器初始化失败!");
            }
        }
        return isStart;
    }


    public void unregister() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
            isStart = false;
            mShakeListener = null;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(TAG, "### onAccuracyChanged,  accuracy = " + accuracy);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // 现在检测时间
        long currentUpdateTime = System.currentTimeMillis();
        // 两次检测的时间间隔
        long timeInterval = currentUpdateTime - mLastUpdateTime;
        if (timeInterval < UPTATE_INTERVAL_TIME) {
            return;
        }
        // 现在的时间变成last时间
        mLastUpdateTime = currentUpdateTime;

        // 获得x,y,z坐标
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        // 获得x,y,z的变化值
        float deltaX = x - mLastX;
        float deltaY = y - mLastY;
        float deltaZ = z - mLastZ;

        // 将现在的坐标变成last坐标
        mLastX = x;
        mLastY = y;
        mLastZ = z;

        // 获取摇晃速度
        double speed = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ
                * deltaZ)
                / timeInterval * 10000;
        // 达到速度阀值，回调给开发者
        if (speed >= mSpeedShreshold && mShakeListener != null) {
            mShakeListener.onShakeComplete(event);
        }

    } // end of onSensorChanged

    /**
     * 获取 mShakeListener
     *
     * @return 返回 mShakeListener
     */
    public OnShakeListener getShakeListener() {
        return mShakeListener;
    }

    public void setShakeListener(OnShakeListener listener) {
        this.mShakeListener = listener;
    }

    public int getSpeedShreshold() {
        return mSpeedShreshold;
    }

    public void setSpeedShreshold(int speedShreshold) {
        if (speedShreshold < 0) {
            speedShreshold = 0;
            Log.e(TAG, "speedShreshold速度阀值不能小于0，自动重置为0.");
        }
        this.mSpeedShreshold = speedShreshold;
    }


    public Sensor getSensor() {
        return mSensor;
    }


    public interface OnShakeListener {
        public void onShakeComplete(SensorEvent event);
    }

}
