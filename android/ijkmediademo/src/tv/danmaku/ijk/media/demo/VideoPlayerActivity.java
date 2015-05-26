/*
 * Copyright (C) 2013 Zhang Rui <bbcallen@gmail.com>
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

package tv.danmaku.ijk.media.demo;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import tv.danmaku.ijk.media.widget.MediaController;
import tv.danmaku.ijk.media.widget.VideoView;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;

public class VideoPlayerActivity extends Activity {
	private VideoView mVideoView;
	private View mBufferingIndicator;
	private MediaController mMediaController;

	private String mVideoPath;
	private ImageView mSnapshortView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_player);

		//mVideoPath = "/sdcard/download/720.flv";
		mVideoPath = "http://14.204.84.49:12580/live/ZZTVHQ/playlist.m3u8";

		Intent intent = getIntent();
		String intentAction = intent.getAction();
		if (!TextUtils.isEmpty(intentAction)
				&& intentAction.equals(Intent.ACTION_VIEW)) {
			mVideoPath = intent.getDataString();
		}

		if (TextUtils.isEmpty(mVideoPath)) {
			mVideoPath = new File(Environment.getExternalStorageDirectory(),
					"download/test.mp4").getAbsolutePath();
		}

		mBufferingIndicator = findViewById(R.id.buffering_indicator);
		mMediaController = new MediaController(this);

		mVideoView = (VideoView) findViewById(R.id.video_view);
		mVideoView.setMediaController(mMediaController);
		mVideoView.setMediaBufferingIndicator(mBufferingIndicator);
		mVideoView.setVideoPath(mVideoPath);
		mVideoView.requestFocus();
		mVideoView.start();
		
		mSnapshortView = (ImageView) findViewById(R.id.snapshort);
		mSnapshortView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                getSnapShot();
            }
        });
	}
	
	// 截图
    private final static int SNAPTSHOT_SUCCESS = 0;
    private final static int SNAPTSHOT_FAILED = 1;
    
    private Handler snapShotHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
            case SNAPTSHOT_SUCCESS:
                Toast.makeText(VideoPlayerActivity.this, "截图保存在：" + msg.obj, Toast.LENGTH_LONG).show();
                break;
            case SNAPTSHOT_FAILED:
                Toast.makeText(VideoPlayerActivity.this, "抱歉，截图失败，请稍后重试" + msg.obj, Toast.LENGTH_LONG).show();
                break;
            }
        }
    };
	private void getSnapShot() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap srcBitmap = Bitmap.createBitmap(mVideoView.getVideoWidth(),
                        mVideoView.getVideoHeight(), Bitmap.Config.ARGB_8888);
                boolean flag = mVideoView.getCurrentFrame(srcBitmap);
                Uri imgUri = null;
                if (srcBitmap != null && flag) {
                    // 给图片打水印
//                    Bitmap newBitmap = Bitmap.createBitmap(mVideoView.getVideoWidth(),
//                            mVideoView.getVideoHeight(), Bitmap.Config.ARGB_8888);
//                    Canvas canvas = new Canvas(newBitmap);
//                    Bitmap waterBitmap = BitmapFactory.decodeResource(getResources(),
//                            R.drawable.ic_xxx);
//                    canvas.drawBitmap(srcBitmap, 0, 0, null);
//                    canvas.drawBitmap(waterBitmap, srcBitmap.getWidth() - waterBitmap.getWidth(),
//                            srcBitmap.getHeight() - waterBitmap.getHeight(), null);
//                    canvas.save(Canvas.ALL_SAVE_FLAG);
//                    canvas.restore();
//                    // 回收旧图片
//                    srcBitmap.recycle();
//                    waterBitmap.recycle();
                    
                    // 保存图片
                    File screenshotsDirectory = new File(Environment
                            .getExternalStorageDirectory().getPath()
                            + "/ijkplayer/snapshot");
                    if (!screenshotsDirectory.exists()) {
                        screenshotsDirectory.mkdirs();
                    }

                    File savePath = new File(
                            screenshotsDirectory.getPath()
                                    + "/"
                                    + new SimpleDateFormat("yyyyMMddHHmmss")
                                            .format(new Date()) + ".jpg");
//                    if (ImageUtils.saveBitmap(savePath.getPath(), newBitmap)) {
                    if (ImageUtils.saveBitmap(savePath.getPath(), srcBitmap)) {
                        imgUri = Uri.fromFile(savePath);
                    }
                    if (imgUri != null) {
                        // sendBroadcast(new
                        // Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        // imgUri));
                        Message msg = new Message();
                        msg.what = SNAPTSHOT_SUCCESS;
                        msg.obj = imgUri;
                        snapShotHandler.sendMessage(msg);
                    } else {
                        Message msg = new Message();
                        msg.what = SNAPTSHOT_FAILED;
                        snapShotHandler.sendMessage(msg);
                    }
                } else {
                    Message msg = new Message();
                    msg.what = SNAPTSHOT_FAILED;
                    snapShotHandler.sendMessage(msg);
                }
            }
        }).start();
    }
}
