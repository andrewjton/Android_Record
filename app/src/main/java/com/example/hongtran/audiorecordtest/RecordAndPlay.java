package com.example.hongtran.audiorecordtest;

import android.app.Activity;
import android.os.Bundle;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;


public class RecordAndPlay extends Activity {


    String[] freqText = {"11.025 KHz (Lowest)", "16.000 KHz", "22.050 KHz", "44.100 KHz (Highest)"};
    Integer[] freqset = {11025, 16000, 22050, 44100};
    private ArrayAdapter<String> adapter;

    Spinner spFrequency;
    Button startRec, stopRec, playBack;

    Boolean recording;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_and_play);
        startRec = (Button) findViewById(R.id.startrec);
        stopRec = (Button) findViewById(R.id.stoprec);
        playBack = (Button) findViewById(R.id.playback);

        startRec.setOnClickListener(startRecOnClickListener);
        stopRec.setOnClickListener(stopRecOnClickListener);
        playBack.setOnClickListener(playBackOnClickListener);

        spFrequency = (Spinner) findViewById(R.id.frequency);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, freqText);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFrequency.setAdapter(adapter);

        stopRec.setEnabled(false);
    }

    OnClickListener startRecOnClickListener
            = new OnClickListener() {

        @Override
        public void onClick(View arg0) {

            Thread recordThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    recording = true;
                    startRecord();
                }

            });

            recordThread.start();
            startRec.setEnabled(false);
            stopRec.setEnabled(true);

        }
    };

    OnClickListener stopRecOnClickListener
            = new OnClickListener() {

        @Override
        public void onClick(View arg0) {
            recording = false;
            startRec.setEnabled(true); // turns on clickable function of button
            stopRec.setEnabled(false); //
        }
    };

    OnClickListener playBackOnClickListener
            = new OnClickListener() {

        @Override
        public void onClick(View v) {
            playRecord();
        }

    };

    private void startRecord() {

        File file = new File(Environment.getExternalStorageDirectory(), "test.pcm"); // where the file is stored

        int selectedPos = spFrequency.getSelectedItemPosition();
        int sampleFreq = freqset[selectedPos];

        final String promptStartRecord =
                "startRecord()\n"
                        + file.getAbsolutePath() + "\n"
                        + (String) spFrequency.getSelectedItem();

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(RecordAndPlay.this,
                        promptStartRecord,
                        Toast.LENGTH_LONG).show();
            }
        });

        try {
            file.createNewFile();

            OutputStream outputStream = new FileOutputStream(file);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
            DataOutputStream dataOutputStream = new DataOutputStream(bufferedOutputStream);

            int minBufferSize = AudioRecord.getMinBufferSize(sampleFreq,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);

            short[] audioData = new short[minBufferSize];

            AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    sampleFreq,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    minBufferSize);

            audioRecord.startRecording();

            while (recording) {
                int numberOfShort = audioRecord.read(audioData, 0, minBufferSize);
                for (int i = 0; i < numberOfShort; i++) {
                    dataOutputStream.writeShort(audioData[i]);
                }
            }

            audioRecord.stop();
            dataOutputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    void playRecord() {

        File file = new File(Environment.getExternalStorageDirectory(), "test.pcm");

        int shortSizeInBytes = Short.SIZE / Byte.SIZE;

        int bufferSizeInBytes = (int) (file.length() / shortSizeInBytes);
        short[] audioData = new short[bufferSizeInBytes];

        try {
            InputStream inputStream = new FileInputStream(file);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
            DataInputStream dataInputStream = new DataInputStream(bufferedInputStream);

            int i = 0;
            while (dataInputStream.available() > 0) {
                audioData[i] = dataInputStream.readShort();
                i++;
            }

            dataInputStream.close();

            int selectedPos = spFrequency.getSelectedItemPosition();
            int sampleFreq = freqset[selectedPos];

            final String promptPlayRecord =
                    "PlayRecord()\n"
                            + file.getAbsolutePath() + "\n"
                            + (String) spFrequency.getSelectedItem();

            Toast.makeText(RecordAndPlay.this,
                    promptPlayRecord,
                    Toast.LENGTH_LONG).show();

            AudioTrack audioTrack = new AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    sampleFreq,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSizeInBytes,
                    AudioTrack.MODE_STREAM);

            audioTrack.play();
            audioTrack.write(audioData, 0, bufferSizeInBytes);


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
