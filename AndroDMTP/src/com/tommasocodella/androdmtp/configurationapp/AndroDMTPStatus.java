package com.tommasocodella.androdmtp.configurationapp;

import com.tommasocodella.androdmtp.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class AndroDMTPStatus extends Activity{
	private ImageView statusImageGreen;
	private ImageView statusImageRed;
	private ImageView statusImageYellow;
	
	
	@Override
	 public void onCreate(Bundle savedInstanceState){
		 super.onCreate(savedInstanceState);
		 setContentView(R.layout.dmtpstatus);
		 
		 
		 Button startButton = (Button) findViewById(R.id.startDMTP);
		 startButton.setOnClickListener(new StartStopListener());
		 
		 Button pauseButton = (Button) findViewById(R.id.pauseDMTP);
		 pauseButton.setOnClickListener(new PauseListener());
		 
		 statusImageGreen = (ImageView) findViewById(R.id.statusIconGreen);
		 statusImageRed = (ImageView) findViewById(R.id.statusIconRed);
		 statusImageYellow = (ImageView) findViewById(R.id.statusImageYellow);
		 
		 statusImageGreen.setVisibility(ImageView.INVISIBLE);
		 statusImageYellow.setVisibility(ImageView.INVISIBLE);
		 statusImageRed.setVisibility(ImageView.VISIBLE);
		 	 
		 startButton.setText("START");
		 pauseButton.setText("PAUSE");
	 }

	private class StartStopListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			Button startButton = (Button) findViewById(R.id.startDMTP);
			if(statusImageGreen.getVisibility() == ImageView.INVISIBLE){
				startButton.setText("STOP");
				statusImageGreen.setVisibility(ImageView.VISIBLE);
				statusImageRed.setVisibility(ImageView.INVISIBLE);
			}else{
				startButton.setText("START");
				statusImageGreen.setVisibility(ImageView.INVISIBLE);
				statusImageRed.setVisibility(ImageView.VISIBLE);
			}
		}
		
	}
	
	private class PauseListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			Button startButton = (Button) findViewById(R.id.pauseDMTP);
			if(statusImageYellow.getVisibility() == ImageView.VISIBLE){
				startButton.setText("RESUME");
				statusImageGreen.setVisibility(ImageView.VISIBLE);
				statusImageYellow.setVisibility(ImageView.INVISIBLE);
			}else{
				startButton.setText("PAUSE");
				statusImageGreen.setVisibility(ImageView.INVISIBLE);
				statusImageYellow.setVisibility(ImageView.VISIBLE);
			}
		}
		
	}
}
