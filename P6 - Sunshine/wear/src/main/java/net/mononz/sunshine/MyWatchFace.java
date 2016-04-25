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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.Time;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

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

    private class Engine extends CanvasWatchFaceService.Engine {

        final Handler mUpdateTimeHandler = new EngineHandler(this);
        boolean mRegisteredTimeZoneReceiver = false;
        Paint mBackgroundPaint;
        Paint mTextTimePaint;
        Paint mTextDatePaint;
        Paint mTextDatePaintAmbient;
        Paint mTextTempHighPaint;
        Paint mTextTempLowPaint;
        boolean mAmbient;

        Calendar mTime;

        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mTime.setTimeZone(TimeZone.getDefault());
                mTime.setTimeInMillis(System.currentTimeMillis());
            }
        };

        float mXOffsetTime;
        float mXOffsetDate;
        float mXOffsetTimeAmbient;

        float mYOffsetTime;
        float mYOffsetDate;
        float mDividerYOffset;
        float mWeatherYOffset;

        String mTempHigh = "36" + (char) 0x00B0;
        String mTempLow = "22" + (char) 0x00B0;


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
            mTextTempLowPaint = createTextPaint(Color.WHITE);

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
                registerReceiver();

                // Update time zone in case it changed while we weren't visible.
                mTime.setTimeZone(TimeZone.getDefault());
                mTime.setTimeInMillis(System.currentTimeMillis());
            } else {
                unregisterReceiver();
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

            // Section : Image


            // Section : Temp High
            float highLength = 0;
            if (mTempHigh != null) {
                highLength = mTextTempHighPaint.measureText(mTempHigh);
                float xOffsetTempHigh = highLength / 2;
                canvas.drawText(mTempHigh, bounds.centerX() - xOffsetTempHigh, mWeatherYOffset, mTextTempHighPaint);
            }

            // Section : Temp Low
            if (mTempLow != null) {
                canvas.drawText(mTempLow, bounds.centerX() + (highLength / 2) + getResources().getDimension(R.dimen.x_padding_tiny), mWeatherYOffset, mTextTempLowPaint);
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
                long delayMs = INTERACTIVE_UPDATE_RATE_MS
                        - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
            }
        }
    }
}
