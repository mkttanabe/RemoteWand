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

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class NoSleepActivity extends Activity implements OnClickListener {
	private final String TAG = "RemoteWand";
    private WakeLock mWakeLock;
    private TextView mTextView;
    private int mCMode;
    private int[] mCTable = {Color.GRAY, Color.rgb(51,51,51), Color.BLACK};

    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.nosleep);
    	mTextView = (TextView)findViewById(R.id.TextView001);
    	mTextView.setOnClickListener(this);
    	// disable sleep mode 
    	PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
    	mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, getString(R.string.app_name));
    	mWakeLock.acquire();
    	mCMode = 0;;
    	mTextView.setTextColor(mCTable[0]);
    }
    
	@Override
    protected void onDestroy() {
		_Log.d(TAG, "NoSleepActivity: onDestroy");
	    super.onDestroy();
	    mWakeLock.release();
	}

	@Override
	public void onClick(View v) {
		if (v == (View)mTextView) {
			if (++mCMode > 2) {
				mCMode = 0;
			}
	    	mTextView.setTextColor(mCTable[mCMode]);
		}
	}
}