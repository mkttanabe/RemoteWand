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

import java.io.IOException;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class CameraActivity extends Activity {
	private final String TAG = "RemoteWand";

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        LinearLayout ll = new LinearLayout(this);
        CameraView sv = new CameraView(this);
        ll.addView(sv);
        setContentView(ll);
    }
	
	public class CameraView extends SurfaceView
		implements SurfaceHolder.Callback, Camera.PictureCallback, Camera.ShutterCallback {
		private Camera mCamera;
		private Context mActivityContext;

		public CameraView(Context ctx) {
			super(ctx);
			mActivityContext = ctx;
			mCamera = null;
			SurfaceHolder holder = getHolder();
			holder.addCallback(this);
			holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			_Log.d(TAG, "CameraActivity: surfaceCreated");
			mCamera = Camera.open();
			if (mCamera != null) {
				try {
					mCamera.setPreviewDisplay(holder);
					mCamera.startPreview();
				} catch (IOException e) {
					mCamera.stopPreview();
					mCamera.release();
					mCamera = null;
					return;
				}
				try {
					Thread.sleep(600);
				} catch (InterruptedException e) {
				}
			}
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int f, int w, int h) {
			_Log.d(TAG, "CameraActivity: surfaceChanged");
			if (mCamera != null) {
				//Camera.Parameters p = mCamera.getParameters();
				//p.setPreviewSize(w,h);
				//mCamera.setParameters(p);
				mCamera.startPreview();
				// 1st parameter (ShutterCallback) is necessary
				// for the shutter sound on Android 4.x
				mCamera.takePicture(this, null, this);
			}
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			_Log.d(TAG, "CameraActivity: surfaceDestroyed");
			if (mCamera != null) {
				mCamera.stopPreview();
				mCamera.release();
				mCamera = null;
			}
		}

		// PictureCallback
		@Override
		public void onPictureTaken(byte[] data, Camera c) {
			_Log.d(TAG, "CameraActivity: onPictureTaken");
			// first, get the image size
			BitmapFactory.Options option = new BitmapFactory.Options();
			option.inJustDecodeBounds = true;
			BitmapFactory.decodeByteArray(data, 0, data.length, option); 		
			int val = Math.min(option.outWidth, option.outHeight);

			// resize the image to VGA quality (to avoid "out of memory")
			int scale = val / 480;
			_Log.d(TAG, "w=" + option.outWidth + " h=" + option.outHeight + " scale=" + scale);
			option.inSampleSize = scale;
			option.inJustDecodeBounds = false; 			
			Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length, option); 		
			String name = "RemoteWand";  
			MediaStore.Images.Media.insertImage(getContext().getContentResolver(), bmp, name, null);

			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
			((Activity)mActivityContext).finish();
		}

		// ShutterCallback
		@Override
		public void onShutter() {  
		}  
	}
}
