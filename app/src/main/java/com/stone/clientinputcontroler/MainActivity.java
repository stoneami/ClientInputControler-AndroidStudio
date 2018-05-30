package com.stone.clientinputcontroler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.stone.net.*;
import com.stone.utils.MouseAction;
import com.stone.utils.Util;
import com.stone.widget.softkeyboard.LatinKeyboard;
import com.stone.widget.softkeyboard.LatinKeyboardView;
import com.stone.widget.TouchPadView;
import com.stone.widget.TouchPadView.TouchPadListener;

public class MainActivity extends Activity implements
		KeyboardView.OnKeyboardActionListener, TouchPadListener {
	private final static String TAG = "MainActivity";

	private final static String IP_REGLEX = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."
			+ "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
			+ "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
			+ "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";

	public final static int MSG_SINGLE_CLICK = 0x1000;
	public final static int MSG_DOUBLE_CLICK = 0x1001;
	public final static int MSG_RIGHT_CLICK = 0x1002;

	public final static String MSG_ACTION = "ACTION_4_CLICK";
	public final static String INTENT_EXTRA_NAME = "MOUSE_ACTION";

	private String mServerIP = "192.168.1.105";
	private int mServerPort = 8888;

	private TouchPadView mTouchPadView;

	private int mMouseAccuracy = 3;

	private IntentFilter mFilter = new IntentFilter();
	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(! MSG_ACTION.equals(intent.getAction())) return;

			switch(intent.getIntExtra(INTENT_EXTRA_NAME, -1)) {
				case MSG_SINGLE_CLICK:
					mExecutors.execute(new SocketRunnable(MouseAction.CLICK, mServerIP,
							mServerPort));
					break;
				case MSG_DOUBLE_CLICK:
					mExecutors.execute(new SocketRunnable(MouseAction.DOUBLE_CLICK, mServerIP,
							mServerPort));
					break;
				case MSG_RIGHT_CLICK:
					mExecutors.execute(new SocketRunnable(MouseAction.RIGHT_CLICK, mServerIP,
							mServerPort));
					break;
				default:
					break;
			}
		}
	};

	private ExecutorService mExecutors = Executors.newSingleThreadExecutor();

	private LatinKeyboardView mKeyboardView;
	private LatinKeyboard mQwertyKeyboard, mSymbolsKeyboard,
			mSymbolsShiftedKeyboard;

	private AlertDialog mDialog;
	private View mDialogContent = null;

	private DialogInterface.OnClickListener mPositiveClickListener = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub
			if (which == DialogInterface.BUTTON_POSITIVE) {
				EditText ipEditText = (EditText) mDialogContent
						.findViewById(R.id.ip);
				EditText portEditText = (EditText) mDialogContent
						.findViewById(R.id.port);

				String IP = ipEditText.getText().toString();
				String port = portEditText.getText().toString();
				if (IP != null && port != null && IP.matches(IP_REGLEX)
						&& port.length() == 4) {
					mServerIP = IP;
					mServerPort = Integer.valueOf(port);

					// set step
					SeekBar seekBar = (SeekBar) mDialogContent
							.findViewById(R.id.sb_step);
					int step = seekBar.getProgress();
					if (step < 1) {
						step = 1;
						seekBar.setProgress(step);
					}
					String command = "#" + String.valueOf(step);
					if (command != null && command.matches("#[1-9]+[0-9]*")) {
						mExecutors.execute(new SocketRunnable(command,
								mServerIP, mServerPort));
						mMouseAccuracy = seekBar.getProgress() * 5;
					}
				} else {
					Toast.makeText(
							getApplicationContext(),
							MainActivity.this
									.getString(R.string.toast_invalid_ip),
							Toast.LENGTH_SHORT).show();
					mServerIP = "192.168.1.101";
					mServerPort = 8888;
				}
			}
		}

	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		init();
		ShowConfigServerDialog();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		registerReceiver(mReceiver,mFilter);
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		if (mDialog != null && mDialog.isShowing())
			mDialog.dismiss();

		unregisterReceiver(mReceiver);
	}

	private void init() {
		mTouchPadView = (TouchPadView) findViewById(R.id.touch_pad);
		mTouchPadView.setTouchPadListener(this);

		mKeyboardView = (LatinKeyboardView) findViewById(R.id.keyboard);
		mQwertyKeyboard = new LatinKeyboard(this, R.xml.qwerty);
		mQwertyKeyboard.setShifted(mShifted);
		mKeyboardView.setOnKeyboardActionListener(this);
		mKeyboardView.setKeyboard(mQwertyKeyboard);
		mKeyboardView.setVisibility(View.GONE);//Hide Keyboard
		mSymbolsKeyboard = new LatinKeyboard(this, R.xml.symbols);
		mSymbolsShiftedKeyboard = new LatinKeyboard(this, R.xml.symbols_shift);

		mFilter.addAction(MSG_ACTION);
	}

	private void setQwertyKeyboard() {
		mKeyboardView.setKeyboard(mQwertyKeyboard);
		mQwertyKeyboard.setShifted(mShifted);
	}

	private void setSymbolsKeyboard() {
		Keyboard currentKeyboard = mKeyboardView.getKeyboard();
		if (mSymbolsKeyboard == currentKeyboard) {
			mSymbolsKeyboard.setShifted(true);
			mKeyboardView.setKeyboard(mSymbolsShiftedKeyboard);
			mSymbolsShiftedKeyboard.setShifted(true);
		} else {
			mSymbolsShiftedKeyboard.setShifted(false);
			mKeyboardView.setKeyboard(mSymbolsKeyboard);
			mSymbolsKeyboard.setShifted(false);
		}
	}

	private void ShowConfigServerDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

		mDialogContent = getLayoutInflater().inflate(
				R.layout.config_server_dialog, null);
		SeekBar seekBar = (SeekBar) mDialogContent.findViewById(R.id.sb_step);
		seekBar.setProgress(mMouseAccuracy);

		mDialog = builder.setView(mDialogContent).setTitle("Please set server IP")
				.setPositiveButton("OK", mPositiveClickListener)
				.setNegativeButton("CANCEL", null).show();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		if (item.getItemId() == R.id.menu_settings) {
			ShowConfigServerDialog();
		}

		return super.onOptionsItemSelected(item);
	}

	private static int mTotalDx = 0;
	private static int mTotalDy = 0;

	private boolean shouldMove(int dx, int dy) {
		//Log.i("shouldMove","DST=" + Math.sqrt((double)(mTotalDx*mTotalDx + mTotalDy*mTotalDy)));

		mTotalDx += dx;
		mTotalDy += dy;

		if(Math.sqrt((double)(mTotalDx*mTotalDx + mTotalDy*mTotalDy)) - 10f > 0) {
			mTotalDx = 0;
			mTotalDy = 0;
			return true;
		}

		return false;
	}

	@Override
	public boolean onMove(int dxx, int dyy) {
		// TODO Auto-generated method stub
		if(shouldMove(dxx, dyy)) {
			String command = Util.createCommandString(mTotalDx + dxx,mTotalDy + dyy );
			mExecutors.execute(new SocketRunnable(command, mServerIP, mServerPort));
		}

		return false;
	}

	/*-
	 * For Keyboard BEGIN>>>
	 */
	private boolean mShifted = false;

	@Override
	public void onPress(int primaryCode) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRelease(int primaryCode) {
		// TODO Auto-generated method stub
		if (-1 == primaryCode) {// shift
			if ((LatinKeyboard) mKeyboardView.getKeyboard() == mQwertyKeyboard) {// Qwerty
				// shift
				mShifted = !mShifted;
				mQwertyKeyboard.setShifted(mShifted);
				mKeyboardView.invalidateAllKeys();
			} else {// symbols shift
				setSymbolsKeyboard();
			}
		} else if (-2 == primaryCode) {// 123
			setSymbolsKeyboard();
		} else if (-22 == primaryCode) {// symbol abc
			setQwertyKeyboard();
		}
	}

	@Override
	public void onKey(int primaryCode, int[] keyCodes) {
		// TODO Auto-generated method stub
		if (-1 == primaryCode) {// shift
			return;
		}

		if (primaryCode == -2) {// key 123
			return;
		}

		// symbols keyboard begin
		if (primaryCode == -22) {// abc
			return;
		}

		if (primaryCode == -11) {// shift
			return;
		}

		if (primaryCode == -3) {// hide keyboard
			return;
		}
		// symbols keyboard end

		if (primaryCode == -5) {// delete key
			primaryCode = 0x08;
		} else if (primaryCode >= 97 && primaryCode <= 122) {// 'a'-'z'
			if (mShifted) {
				primaryCode -= 32;// 'A'-'Z'
			}
		}
		mExecutors.execute(new SocketRunnable("##" + (char) primaryCode,
				mServerIP, mServerPort));
	}

	@Override
	public void onText(CharSequence text) {
		// TODO Auto-generated method stub

	}

	@Override
	public void swipeLeft() {
		// TODO Auto-generated method stub

	}

	@Override
	public void swipeRight() {
		// TODO Auto-generated method stub

	}

	@Override
	public void swipeDown() {
		// TODO Auto-generated method stub

	}

	@Override
	public void swipeUp() {
		// TODO Auto-generated method stub

	}

}
