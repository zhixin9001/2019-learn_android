package com.example.zhixin.recrep;

import android.graphics.Color;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private MediaRecorder mediaRecorder;
    // 以文件的形式保存
    private File recordFile;
    private RecordPlayer player;
    public static int[] PIE_COLORS = {
            Color.rgb(181, 194, 202), Color.rgb(129, 216, 200), Color.rgb(241, 214, 145),
            Color.rgb(108, 176, 223), Color.rgb(195, 221, 155), Color.rgb(251, 215, 191),
            Color.rgb(237, 189, 189), Color.rgb(172, 217, 243), Color.rgb(105, 221, 155),
            Color.rgb(251, 205, 191), Color.rgb(237, 189, 109)
    };

    private int count = 0;

    //    private RecordPlayer player;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        recordFile = new File(Environment.getExternalStorageDirectory() + "/Download/", "kk.amr");
        final Handler handler = new Handler();


        final Button btnStop = (Button) findViewById(R.id.replay);
        final Button btnRecord = (Button) findViewById(R.id.record);
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
// TODO Auto-generated method stub
//要做的事情
                count++;
                btnStop.setBackgroundColor(PIE_COLORS[(count + 1) % 10]);
                btnRecord.setBackgroundColor(PIE_COLORS[count % 10]);
                handler.postDelayed(this, 500);
            }
        };
        btnRecord.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int action = motionEvent.getAction();
                if (action == MotionEvent.ACTION_DOWN) { // 按下
                    count++;
                    Toast.makeText(getBaseContext(), "开始录音", Toast.LENGTH_SHORT).show();
                    startRecording();
                    btnStop.setBackgroundColor(PIE_COLORS[(count + 5) % 10]);
                    handler.postDelayed(runnable, 500);//每两秒执行一次runnable.
                } else if (action == MotionEvent.ACTION_UP) { // 松开
                    Toast.makeText(getBaseContext(), "开始播放", Toast.LENGTH_SHORT).show();
                    stopRecording();
                    player = new RecordPlayer(MainActivity.this);
                    player.playRecordFile(recordFile);

                    btnRecord.setBackgroundColor(PIE_COLORS[count % 10]);
                    handler.removeCallbacks(runnable);
                }
                return false;
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                count++;
                btnStop.setBackgroundColor(PIE_COLORS[(count + 5) % 10]);
                player = new RecordPlayer(MainActivity.this);
                Toast.makeText(getBaseContext(), "开始播放", Toast.LENGTH_SHORT).show();
                player.playRecordFile(recordFile);
            }
        });
    }

    private void startRecording() {
        mediaRecorder = new MediaRecorder();
        // 判断，若当前文件已存在，则删除
        if (recordFile.exists()) {
            recordFile.delete();
        }
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        mediaRecorder.setOutputFile(recordFile.getAbsolutePath());

        try {
            // 准备好开始录音
            mediaRecorder.prepare();

            mediaRecorder.start();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void stopRecording() {
        if (recordFile != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
