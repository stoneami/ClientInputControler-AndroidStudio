package com.stone.widget;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.stone.clientinputcontroler.MainActivity;

public class TouchPadView extends View {
	private final static String TAG = "TouchPadView";
	private final static boolean DEBUG = false;

	private TouchPadListener mListener;
	private GestureDetector mGestureDetector;
	private GestureDetector.OnGestureListener mOnGestureListener = new GestureDetector.OnGestureListener() {
		@Override
		public boolean onDown(MotionEvent motionEvent) {
			return false;
		}

		@Override
		public void onShowPress(MotionEvent motionEvent) {

		}

		@Override
		public boolean onSingleTapUp(MotionEvent motionEvent) {
			return false;
		}

		@Override
		public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
			return false;
		}

		@Override
		public void onLongPress(MotionEvent motionEvent) {

		}

		@Override
		public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
			return false;
		}
	};

	public void setTouchPadListener(TouchPadListener listener) {
		mListener = listener;
	}

	private void init(Context context){
		if(mGestureDetector == null) {
			mGestureDetector = new GestureDetector(context, mOnGestureListener);
			mGestureDetector.setOnDoubleTapListener(new DoubleTap());
		}
	}

	public TouchPadView(Context context) {
		super(context);
		init(context);
	}

	public TouchPadView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public TouchPadView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private int mStartX = -1;
	private int mStartY = -1;

	private int mAccuracy = 15;
	private final static int FACTOR = 2;

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		mGestureDetector.onTouchEvent(event);

		int action = event.getAction();

		if (DEBUG)
			Log.i(TAG, "TouchPadView.onTouchEvent(): action = " + action);

		if (action == MotionEvent.ACTION_DOWN) {
			mStartX = (int) event.getX();
			mStartY = (int) event.getY();
		} else if (action == MotionEvent.ACTION_UP) {
			mStartX = -1;
			mStartY = -1;
		} else if (action == MotionEvent.ACTION_MOVE) {
			if (mListener != null) {
				if (DEBUG)
					Log.i(TAG, "TouchPadView.onTouchEvent(): onMove()");				

				if (Math.abs((int) event.getX() - mStartX) - mAccuracy > 0
						|| Math.abs((int) event.getY() - mStartY) - mAccuracy > 0) {
					
					int dx = ((int) event.getX()-mStartX)/FACTOR;
					int dy = ((int) event.getY()-mStartY)/FACTOR;
					
					mListener.onMove(dx, dy);
					
					mStartX = (int) event.getX();
					mStartY = (int) event.getY();
				}
			}
		}else if(action == MotionEvent.ACTION_POINTER_UP){
            getContext().sendBroadcast(new Intent(MainActivity.MSG_ACTION).putExtra(MainActivity.INTENT_EXTRA_NAME, MainActivity.MSG_RIGHT_CLICK));
        }

		return true;
	}

	/*-
	 * For Inner Classes BEGIN>>>
	 */
	public interface TouchPadListener {
		boolean onMove(int dx, int dy);
	}

	public class DoubleTap implements GestureDetector.OnDoubleTapListener {
		@Override
		public boolean onDoubleTap(MotionEvent e) {
			TouchPadView.this.getContext().sendBroadcast(new Intent(MainActivity.MSG_ACTION).putExtra(MainActivity.INTENT_EXTRA_NAME, MainActivity.MSG_DOUBLE_CLICK));
			return true;
		}

		@Override
		public boolean onDoubleTapEvent(MotionEvent e) {
			return false;
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			TouchPadView.this.getContext().sendBroadcast(new Intent(MainActivity.MSG_ACTION).putExtra(MainActivity.INTENT_EXTRA_NAME, MainActivity.MSG_SINGLE_CLICK));
			return true;
		}
	}
}
