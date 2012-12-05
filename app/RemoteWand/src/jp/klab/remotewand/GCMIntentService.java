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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.google.android.gcm.GCMBaseIntentService;
import static jp.klab.remotewand.Constant.SENDER_ID;
import static jp.klab.remotewand.Constant.MYACTION_NOTIFY;
import static jp.klab.remotewand.Constant.MYEXTRA_NAME_EVENT;	
import static jp.klab.remotewand.Constant.MYEXTRA_NAME_REGID;
import static jp.klab.remotewand.Constant.MYEXTRA_EVENT_REGISTERED;
import static jp.klab.remotewand.Constant.MYEXTRA_EVENT_UNREGISTERED;
import static jp.klab.remotewand.Constant.MYEXTRA_EVENT_ERROR;
import static jp.klab.remotewand.Constant.MYEXTRA_NAME_ERRORID;

public class GCMIntentService extends GCMBaseIntentService {
	private final String TAG = "RemoteWand";
	public GCMIntentService() {
		super(SENDER_ID);
	}

	@Override
	protected void onMessage(Context context, Intent intent) {
		Bundle extras = intent.getExtras();
		if (extras != null) {
			String action = (String) extras.get("action");
			_Log.d(TAG, "GCMIntentService: onMessage: action=" + action);
			if (action != null && action.equals("camera")) {
				Intent it = new Intent(getApplicationContext(), CameraActivity.class);
				it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(it);
			}
		}
	}

	@Override
	public void onRegistered(Context context, String registrationId) {
		_Log.d(TAG, "GCMIntentService: onRegistered id=" + registrationId);
		Intent it = new Intent(MYACTION_NOTIFY);
		it.putExtra(MYEXTRA_NAME_EVENT, MYEXTRA_EVENT_REGISTERED);
		it.putExtra(MYEXTRA_NAME_REGID, registrationId);
		sendBroadcast(it);
	}

	@Override
	public void onUnregistered(Context context, String registrationId) {
		_Log.d(TAG, "GCMIntentService: onUnregistered");
		Intent it = new Intent(MYACTION_NOTIFY);
		it.putExtra(MYEXTRA_NAME_EVENT, MYEXTRA_EVENT_UNREGISTERED);
		sendBroadcast(it);
	}

	@Override
	public void onError(Context context, String errorId) {
		_Log.d(TAG, "GCMIntentService: onError errid=" + errorId);
		Intent it = new Intent(MYACTION_NOTIFY);
		it.putExtra(MYEXTRA_NAME_EVENT, MYEXTRA_EVENT_ERROR);
		it.putExtra(MYEXTRA_NAME_ERRORID, errorId);
		sendBroadcast(it);
	}
}
