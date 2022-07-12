package com.example.sqliteapp;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    public void onLongPress(MotionEvent e) {

    }

    public boolean onDoubleTap(MotionEvent e) {
        return false;
    }

    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;

    }

    public void onShowPress(MotionEvent e) {

    }

    public boolean onDown(MotionEvent e) {
        // Must return true to get matching events for this down event.
        return true;
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, final float distanceX, float distanceY) {
        return super.onScroll(e1, e2, distanceX, distanceY);
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        // do something
    //e1.getX()

        return super.onFling(e1, e2, velocityX, velocityY);
    }
}