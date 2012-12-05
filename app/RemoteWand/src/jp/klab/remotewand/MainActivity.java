/*
* Copyright (C) 2012 KLab Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package jp.klab.remotewand;

import java.io.File;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import com.google.android.gcm.GCMRegistrar;
import static jp.klab.remotewand.Constant.SENDER_ID;
import static jp.klab.remotewand.Constant.MYACTION_NOTIFY;
import static jp.klab.remotewand.Constant.MYEXTRA_NAME_EVENT;	
import static jp.klab.remotewand.Constant.MYEXTRA_NAME_REGID;
import static jp.klab.remotewand.Constant.MYEXTRA_EVENT_REGISTERED;
import static jp.klab.remotewand.Constant.MYEXTRA_EVENT_UNREGISTERED;
import static jp.klab.remotewand.Constant.MYEXTRA_EVENT_ERROR;
import static jp.klab.remotewand.Constant.MYEXTRA_NAME_ERRORID;

public class MainActivity extends Activity implements OnClickListener {
	private final String TAG = "RemoteWand";
	private final String PKG = "jp.klab.remotewand";
	private final String DROPBOX = "com.dropbox.android";
	private Button mButtonRegister;
    private Button mButtonUnregister;
    private Button mButtonNoSleep;
    private EditText mEditPassword;
    private String mPass;
    private MyBroadcastReceiver mReceiver;
    private String mVersion;

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.main);
    	mButtonRegister = (Button)findViewById(R.id.button1);
    	mButtonUnregister = (Button)findViewById(R.id.button2);
    	mButtonNoSleep = (Button)findViewById(R.id.button3);
    	mEditPassword = (EditText)findViewById(R.id.editText1);
    	mEditPassword.setInputType(InputType.TYPE_CLASS_TEXT |
    			InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS );

    	mButtonRegister.setOnClickListener(this);
    	mButtonUnregister.setOnClickListener(this);
    	mButtonNoSleep.setOnClickListener(this);

    	GCMRegistrar.checkDevice(this);
    	GCMRegistrar.checkManifest(this);
    	final String regId = GCMRegistrar.getRegistrationId(this);
    	if (regId.equals("")) {
    		mySetViewStatus(true);
    	} else {
    		mySetViewStatus(false);
    	}
    	mReceiver = new MyBroadcastReceiver();
    	registerReceiver(mReceiver, new IntentFilter(MYACTION_NOTIFY));
    	
    	try {
			mVersion = getPackageManager().getPackageInfo(PKG, 0).versionName;
		} catch (NameNotFoundException e) {
			mVersion = "";
		}
    	if (AppIsInstalled(DROPBOX) != true) {
			showDialogMessage(this, getString(R.string.MsgNoDropbox), false);
    	}
    }
    
	@Override
    protected void onDestroy() {
		_Log.d(TAG, "MainActivity: onDestroy");
	    File html = new File(HtmlForm.fileName());
	    if (html.exists()) {
	    	html.delete();
	    }
	    if (mReceiver != null) {
	    	unregisterReceiver(mReceiver);
	    }
	    try {
	    	GCMRegistrar.onDestroy(getApplicationContext());
	    } catch (Exception e) {
	    }
	    super.onDestroy();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, 0, Menu.NONE, R.string.WordDropboxPage).
			setIcon(android.R.drawable.ic_media_play );
		menu.add(0, 1, Menu.NONE, R.string.WordAbout).
			setIcon(android.R.drawable.ic_menu_help);
		menu.add(0, 2, Menu.NONE, R.string.WordExit).
			setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case 0: // Dropbox page
			Uri uri = Uri.parse("market://details?id=" + DROPBOX);
			Intent it = new Intent(Intent.ACTION_VIEW, uri);
			startActivity(it);
			return true;
		case 1: // About
			showDialogMessage(this, getString(R.string.app_name) +
							" " + mVersion + "\r\n\r\n" +
							getString(R.string.CopyRightString) , false);
			return true;
		case 2: // exit
			finish();
			return	true;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		if (v == (View)mButtonRegister) {
			mPass = mEditPassword.getText().toString();
			if (mPass.length() <= 0) {
				showDialogMessage(this, getString(R.string.MsgSpecifyPassword), false);
				return;
			}
			new AlertDialog.Builder(this)
			.setTitle(R.string.app_name)
			.setIcon(R.drawable.icon)
			.setMessage(R.string.MsgRegisterDevice)
			.setPositiveButton(R.string.WordYes,
					new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						GCMRegistrar.register(getApplicationContext(), SENDER_ID);
					}
				})
			.setNegativeButton(R.string.WordNo,
					new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				})
			.show();
		}
		else if (v == (View)mButtonUnregister) {
			new AlertDialog.Builder(this)
			.setTitle(R.string.app_name)
			.setIcon(R.drawable.icon)
			.setMessage(R.string.MsgUnregisterDevice)
			.setPositiveButton(R.string.WordYes,
					new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						GCMRegistrar.unregister(getApplicationContext());
					}
				})
			.setNegativeButton(R.string.WordNo,
					new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				})
			.show();
		}
		else if (v == (View)mButtonNoSleep) {
			Intent it = new Intent(getApplicationContext(), NoSleepActivity.class);
			startActivity(it);
			finish();
		}
	}
	
	private boolean createMail(String regid) {
        Account[] accounts =
        	AccountManager.get(this).getAccountsByType("com.google");
        String cryptStr = MyCrypt.Crypt(regid, mPass);
        if (cryptStr == null) {
        	return false;
        }
		if (!HtmlForm.create(cryptStr, getApplicationContext())) {
			return false;
		}
        Intent it = new Intent();
		it.setAction(Intent.ACTION_SEND);
		String[] mailto = {accounts[0].name};
		it.putExtra(Intent.EXTRA_EMAIL, mailto);
		it.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.WordMailSubject));
		it.putExtra(Intent.EXTRA_TEXT, getString(R.string.MsgMailText));
		it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		it.setType("application/octed-stream");
		it.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + HtmlForm.fileName()));
		startActivity(it);
		return true;
	}
	
	public class MyBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
	    	PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
	    	WakeLock wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, getString(R.string.app_name));
	    	wakeLock.acquire();
			Bundle extras = intent.getExtras();
			String event = extras.getString(MYEXTRA_NAME_EVENT);
			_Log.d(TAG, "event=" + event);
			if (event.equals(MYEXTRA_EVENT_REGISTERED)) {
				mySetViewStatus(false);
				final String regid = extras.getString(MYEXTRA_NAME_REGID);
				new AlertDialog.Builder(context)
				.setTitle(R.string.app_name)
				.setIcon(R.drawable.icon)
				.setCancelable(false) // disable back button 
				.setMessage(R.string.MsgCreateMail)
				.setPositiveButton("OK",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						createMail(regid);
					}
				})
				.show();
			}
			else if (event.equals(MYEXTRA_EVENT_UNREGISTERED)) {
				mySetViewStatus(true);
				showDialogMessage(context, getString(R.string.MsgUnregistered), false);
			}
			else if (event.equals(MYEXTRA_EVENT_ERROR)) {
				String errId = extras.getString(MYEXTRA_NAME_ERRORID);
				showDialogMessage(context, errId, false);
			}
			wakeLock.release();
		}
	}

	private void showDialogMessage(Context ctx, String msg, final boolean bFinish) {
		new AlertDialog.Builder(ctx)
			.setTitle(R.string.app_name)
			.setIcon(R.drawable.icon)
			.setMessage(msg)
			.setPositiveButton("OK",
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					if (bFinish) {
						finish();
					}
				}
			})
			.show();
	}
	
    private void mySetViewStatus(boolean bMode) {
    	mButtonRegister.setEnabled(bMode);
    	mButtonRegister.setClickable(bMode);
    	mEditPassword.setText("");
    	mEditPassword.setEnabled(bMode);
    	mEditPassword.setClickable(bMode);
    	mButtonUnregister.setEnabled(!bMode);
    	mButtonUnregister.setClickable(!bMode);
    	mButtonNoSleep.setEnabled(!bMode);
    	mButtonNoSleep.setClickable(!bMode);
    }	

    private boolean AppIsInstalled(String packageName) {
		PackageManager pm = getApplicationContext().getPackageManager();
		try {
			ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);
		} catch (NameNotFoundException e) {
			return false;
		}
		return true;
	}
}