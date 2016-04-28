/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.mononz.sunshine;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Digital watch face with seconds. In ambient mode, the seconds aren't displayed. On devices with
 * low-bit ambient mode, the text is drawn without anti-aliasing in ambient mode.
 */
public class MyWatchFace extends CanvasWatchFaceService {

    private static final Typeface NORMAL_TYPEFACE = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);
    private static final Typeface BOLD_TYPEFACE = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD);

    /**
     * Update rate in milliseconds for interactive mode. We update once a second since seconds are
     * displayed in interactive mode.
     */
    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);

    /**
     * Handler message id for updating the time periodically in interactive mode.
     */
    private static final int MSG_UPDATE_TIME = 0;

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private static class EngineHandler extends Handler {
        private final WeakReference<MyWatchFace.Engine> mWeakReference;

        public EngineHandler(MyWatchFace.Engine reference) {
            mWeakReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            MyWatchFace.Engine engine = mWeakReference.get();
            if (engine != null) {
                switch (msg.what) {
                    case MSG_UPDATE_TIME:
                        engine.handleUpdateTimeMessage();
                        break;
                }
            }
        }
    }

    private class Engine extends CanvasWatchFaceService.Engine implements DataApi.DataListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

        // Wearable Keys
        private static final String ROUTE_TO_PHONE   = "/WEAR_TO_PHONE";
        private static final String ROUTE_FROM_PHONE = "/PHONE_TO_WEAR";
        private static final String TIMESTAMP        = "TIMESTAMP";
        private static final String KEY_TEMP_HIGH    = "TEMP_HIGH";
        private static final String KEY_TEMP_LOW     = "TEMP_LOW";
        private static final String KEY_WEATHER_ICON = "WEATHER_ICON";

        final Handler mUpdateTimeHandler = new EngineHandler(this);
        boolean mRegisteredTimeZoneReceiver = false;

        Paint mBackgroundPaint;

        Paint mTextTimePaint;
        Paint mTextDatePaint;
        Paint mTextDatePaintAmbient;
        Paint mTextTempHighPaint;
        Paint mTextTempLowPaint;
        Paint mTextTempLowPaintAmbient;
        boolean mAmbient;

        Calendar mTime;

        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mTime.setTimeZone(TimeZone.getDefault());
                mTime.setTimeInMillis(System.currentTimeMillis());
            }
        };

        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(MyWatchFace.this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();

        float mXOffsetTime;
        float mXOffsetDate;
        float mXOffsetTimeAmbient;

        float mYOffsetTime;
        float mYOffsetDate;
        float mDividerYOffset;
        float mWeatherYOffset;

        String mTempHigh;
        String mTempLow;
        Bitmap mWeatherIcon;

        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        boolean mLowBitAmbient;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            setWatchFaceStyle(new WatchFaceStyle.Builder(MyWatchFace.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_VARIABLE)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .build());

            Resources resources = MyWatchFace.this.getResources();
            mYOffsetTime = resources.getDimension(R.dimen.offset_y_time);
            mYOffsetDate = resources.getDimension(R.dimen.offset_y_date);
            mDividerYOffset = resources.getDimension(R.dimen.offset_y_divider);
            mWeatherYOffset = resources.getDimension(R.dimen.offset_y_temperatures);

            mBackgroundPaint = new Paint();
            mBackgroundPaint.setColor(ContextCompat.getColor(MyWatchFace.this, R.color.background_blue));

            mTextTimePaint = new Paint();
            mTextTimePaint = createTextPaint(Color.WHITE);

            mTextDatePaint = new Paint();
            mTextDatePaint = createTextPaint(ContextCompat.getColor(MyWatchFace.this, R.color.watch_text_secondary));

            mTextDatePaintAmbient = new Paint();
            mTextDatePaintAmbient = createTextPaint(Color.WHITE);

            mTextTempHighPaint = new Paint();
            mTextTempHighPaint = createTextPaint(Color.WHITE);
            mTextTempHighPaint.setTypeface(BOLD_TYPEFACE);

            mTextTempLowPaint = new Paint();
            mTextTempLowPaint = createTextPaint(ContextCompat.getColor(MyWatchFace.this, R.color.watch_text_secondary));

            mTextTempLowPaintAmbient = new Paint();
            mTextTempLowPaintAmbient = createTextPaint(Color.WHITE);

            mTime = Calendar.getInstance();
        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            super.onDestroy();
        }

        private Paint createTextPaint(int textColor) {
            Paint paint = new Paint();
            paint.setColor(textColor);
            paint.setTypeface(NORMAL_TYPEFACE);
            paint.setAntiAlias(true);
            return paint;
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                mGoogleApiClient.connect();

                registerReceiver();

                // Update time zone in case it changed while we weren't visible.
                mTime.setTimeZone(TimeZone.getDefault());
                mTime.setTimeInMillis(System.currentTimeMillis());
            } else {
                unregisterReceiver();

                if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                    Wearable.DataApi.removeListener(mGoogleApiClient, this);
                    mGoogleApiClient.disconnect();
                }
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            MyWatchFace.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            MyWatchFace.this.unregisterReceiver(mTimeZoneReceiver);
        }

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);

            // Load resources that have alternate values for round watches.
            Resources resources = MyWatchFace.this.getResources();
            boolean isRound = insets.isRound();
            mXOffsetTime = resources.getDimension(isRound ? R.dimen.offset_x_time_round : R.dimen.offset_x_time);
            mXOffsetDate = resources.getDimension(isRound ? R.dimen.offset_x_date_round : R.dimen.offset_x_date);
            mXOffsetTimeAmbient = resources.getDimension(isRound ? R.dimen.offset_x_time_round_ambient : R.dimen.offset_x_time_ambient);
            float timeTextSize = resources.getDimension(isRound ? R.dimen.text_size_time_round : R.dimen.text_size_time);
            float dateTextSize = resources.getDimension(isRound ? R.dimen.text_size_date_round : R.dimen.text_size_date);
            float tempHighTextSize = resources.getDimension(isRound ? R.dimen.text_size_temp_round : R.dimen.text_size_temp);
            float tempLowTextSize = resources.getDimension(isRound ? R.dimen.text_size_temp_round : R.dimen.text_size_temp);

            mTextTimePaint.setTextSize(timeTextSize);
            mTextDatePaint.setTextSize(dateTextSize);
            mTextDatePaintAmbient.setTextSize(dateTextSize);
            mTextTempHighPaint.setTextSize(tempHighTextSize);
            mTextTempLowPaint.setTextSize(tempLowTextSize);
            mTextTempLowPaintAmbient.setTextSize(tempLowTextSize);
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            if (mAmbient != inAmbientMode) {
                mAmbient = inAmbientMode;
                if (mLowBitAmbient) {
                    mTextTimePaint.setAntiAlias(!inAmbientMode);
                    mTextDatePaint.setAntiAlias(!inAmbientMode);
                    mTextDatePaintAmbient.setAntiAlias(!inAmbientMode);
                    mTextTempHighPaint.setAntiAlias(!inAmbientMode);
                    mTextTempLowPaint.setAntiAlias(!inAmbientMode);
                    mTextTempLowPaintAmbient.setAntiAlias(!inAmbientMode);
                }
                invalidate();
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {

            // Section : Background. Off in Ambient, Blue in interactive
            if (isInAmbientMode()) {
                canvas.drawColor(Color.BLACK);
            } else {
                canvas.drawRect(0, 0, bounds.width(), bounds.height(), mBackgroundPaint);
            }

            mTime.setTimeZone(TimeZone.getDefault());
            mTime.setTimeInMillis(System.currentTimeMillis());

            SimpleDateFormat formatTimeLong = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            SimpleDateFormat formatTimeShort = new SimpleDateFormat("HH:mm", Locale.getDefault());
            SimpleDateFormat formatDate = new SimpleDateFormat("EEE, MMM dd yyyy", Locale.getDefault());

            // Section : Time. In ambient, only display hours and minutes due to minute based update frequency
            String textTime = mAmbient ? formatTimeShort.format(mTime.getTime()) : formatTimeLong.format(mTime.getTime());
            float xOffsetTime = mTextTimePaint.measureText(textTime) / 2;
            canvas.drawText(textTime, bounds.centerX() - xOffsetTime, mYOffsetTime, mTextTimePaint);

            // Section : Date
            String dateText = formatDate.format(mTime.getTime());
            float xOffsetDate = mTextDatePaint.measureText(dateText) / 2;
            Paint datePaint = mAmbient ? mTextDatePaintAmbient : mTextDatePaint;
            canvas.drawText(dateText, bounds.centerX() - xOffsetDate, mYOffsetDate, datePaint);

            // Section : Divider
            canvas.drawLine(bounds.centerX() - getResources().getDimension(R.dimen.divider_half_length), mDividerYOffset, bounds.centerX() + getResources().getDimension(R.dimen.divider_half_length), mDividerYOffset, datePaint);

            // Section : Temp High
            float highLength = 0;
            if (mTempHigh != null) {
                highLength = mTextTempHighPaint.measureText(mTempHigh);
                float xOffsetTempHigh = highLength / 2;
                canvas.drawText(mTempHigh, bounds.centerX() - xOffsetTempHigh, mWeatherYOffset, mTextTempHighPaint);
            }

            // Section : Temp Low
            if (mTempLow != null) {
                Paint lowTemp = mAmbient ? mTextTempLowPaintAmbient : mTextTempLowPaint;
                canvas.drawText(mTempLow, bounds.centerX() + (highLength / 2) + getResources().getDimension(R.dimen.x_padding_tiny), mWeatherYOffset, lowTemp);
            }

            // Section : Image
            if (!mAmbient && mWeatherIcon != null) {
                canvas.drawBitmap(mWeatherIcon, bounds.centerX() - (highLength / 2) - mWeatherIcon.getWidth() - getResources().getDimension(R.dimen.x_padding_tiny), mWeatherYOffset - mWeatherIcon.getHeight() + 7, null);
            }
        }

        /**
         * Starts the {@link #mUpdateTimeHandler} timer if it should be running and isn't currently
         * or stops it if it shouldn't be running but currently is.
         */
        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        /**
         * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer should
         * only run when we're visible and in interactive mode.
         */
        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }

        /**
         * Handle updating the time periodically in interactive mode.
         */
        private void handleUpdateTimeMessage() {
            invalidate();
            if (shouldTimerBeRunning()) {
                long timeMs = System.currentTimeMillis();
                long delayMs = INTERACTIVE_UPDATE_RATE_MS - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
            }
        }

        @Override
        public void onConnected(Bundle bundle) {
            Wearable.DataApi.addListener(mGoogleApiClient, Engine.this);
            requestWeatherInfo();
        }

        @Override
        public void onConnectionSuspended(int i) {

        }

        @Override
        public void onDataChanged(DataEventBuffer dataEvents) {
            for (DataEvent dataEvent : dataEvents) {
                if (dataEvent.getType() == DataEvent.TYPE_CHANGED) {
                    DataMap dataMap = DataMapItem.fromDataItem(dataEvent.getDataItem()).getDataMap();
                    String path = dataEvent.getDataItem().getUri().getPath();
                    if (path.equals(ROUTE_FROM_PHONE)) {
                        if (dataMap.containsKey(KEY_TEMP_HIGH)) {
                            mTempHigh = dataMap.getString(KEY_TEMP_HIGH);
                        }
                        if (dataMap.containsKey(KEY_TEMP_LOW)) {
                            mTempLow = dataMap.getString(KEY_TEMP_LOW);
                        }
                        if (dataMap.containsKey(KEY_WEATHER_ICON)) {
                            try {
                                Asset profileAsset = dataMap.getAsset(KEY_WEATHER_ICON);
                                loadBitmapFromAsset(profileAsset);
                            } catch (Exception e) {
                                mWeatherIcon = null;
                            }
                        }
                    }
                }
            }
            invalidate();
        }

        // citation for decoding bitmap : https://possiblemobile.com/2014/07/create-custom-ongoing-notification-android-wear/
        public void loadBitmapFromAsset(final Asset asset) {
            if (asset == null) {
                mWeatherIcon = null;
                return;
            }
            new AsyncTask<Asset, Void, Bitmap>() {
                @Override
                protected Bitmap doInBackground(Asset... assets) {
                    ConnectionResult result = mGoogleApiClient.blockingConnect(1000, TimeUnit.MILLISECONDS);
                    if (!result.isSuccess()) {
                        return null;
                    }
                    // convert asset into a file descriptor and block until it's ready
                    InputStream assetInputStream = Wearable.DataApi.getFdForAsset(mGoogleApiClient, assets[0]).await().getInputStream();
                    if (assetInputStream == null) {
                        Log.w("Wear", "Requested an unknown Asset.");
                        return null;
                    }
                    return BitmapFactory.decodeStream(assetInputStream);
                }

                @Override
                protected void onPostExecute(Bitmap bitmap) {
                    if (bitmap != null) {
                        float scaledWidth = (mTextTempHighPaint.getTextSize() / bitmap.getHeight()) * bitmap.getWidth();
                        mWeatherIcon = Bitmap.createScaledBitmap(bitmap, (int) scaledWidth, (int) mTextTempHighPaint.getTextSize(), true);
                    } else {
                        mWeatherIcon = null;
                    }
                }
            }.execute(asset);
        }

        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {

        }

        public void requestWeatherInfo() {
            PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(ROUTE_TO_PHONE);
            putDataMapRequest.getDataMap().putLong(TIMESTAMP, System.currentTimeMillis());
            PutDataRequest request = putDataMapRequest.asPutDataRequest();
            Wearable.DataApi.putDataItem(mGoogleApiClient, request);
        }

    }
}
