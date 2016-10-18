/*
Copyright (C) 2012 GestureWave

This file is part of GestureWave.

GestureWave is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

GestureWave is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with GestureWave. If not, see <http://www.gnu.org/licenses/>.
*/

package com.gesturewave.android;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.*;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class SensorService extends Service implements SensorEventListener
{
	//THIS IS FOR TESTING PURPOSES ONLY
	public static boolean isTest = false;

	private boolean handlerLock;
	private boolean statusBarDown;
	private int sensorChangeCount;
	private int direction;
	private DBHelper dbHelper;
	private AudioManager audioManager;
	private Handler waveHandler;
	private IntentFilter screenFilter;
	private KeyguardManager keyguardManager;
	private BroadcastReceiver screenStateReceiver;
	private SensorManager sensorManager;
	private Sensor accelerometerSensor;
	private Sensor proximitySensor;

	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	@Override
	public void onCreate()
	{
		super.onCreate();

		//CREATES A NEW DATABASE INSTANCE
		dbHelper = new DBHelper(this);

		handlerLock = false;
		statusBarDown = false;
		sensorChangeCount = 0;
		direction = 0;

		audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		waveHandler = new Handler();
		screenFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
		screenFilter.addAction(Intent.ACTION_SCREEN_OFF);
		keyguardManager = (KeyguardManager)getSystemService(KEYGUARD_SERVICE);
		screenStateReceiver = setScreenStateReceiver(this);
		sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
		accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		//THIS GETS CALLED IF SERVICE IS NOT BEING RUN UNDER A TEST
		if(!isTest)
		{
			//STARTS THE SERVICE AS A FOREGROUND TASK AND ADDS NOTIFICATION TO STATUS PANEL
			startForeground(1, getNotification());
		}

		//OPENS DB FOR QUERYING
		dbHelper.open();

		//REGISTERS LISTENER FOR SCREEN ON/OFF EVENTS
		registerReceiver(screenStateReceiver, screenFilter);

		//REGISTERS LISTENER FOR PROXIMITY SENSOR INPUT
		sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);

		return START_STICKY;
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();

		//dbHelper.close();
		unregisterReceiver(screenStateReceiver);
		sensorManager.unregisterListener(this, proximitySensor);
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {}

	@Override
	public void onSensorChanged(SensorEvent event)
	{
		//RESETS HANDLER LOCK WHEN COUNT RESETS
		if(sensorChangeCount == 0)
			handlerLock = false;

		//UPDATES NUMBER OF TIMES SENSOR INPUT HAS BEEN GIVEN BY USER
		sensorChangeCount++;

		//MUTEX THAT EXECUTES THE FOLLOWING BLOCK OF CODE ONLY IF THIS IS THE FIRST TIME ONSENSORCHANGED HAS BEEN CALLED AFTER A SENSORCHANGECOUNT RESET
		if(!handlerLock)
		{
			//LOCKS THIS BLOCK OF CODE FOR ANY SEQUENTIAL CALLS TO ONSENSORCHANGED UNTIL LOCK IS RESET
			handlerLock = true;

			//EXECUTES THE FOLLOWING RUN CODE 1000 MILLISECONDS AFTER FIRST CALL TO ONSENSORCHANGED
			waveHandler.postDelayed(new Runnable()
			{
				@Override
				public void run()
				{
					sensorManager.registerListener(new SensorEventListener()
					{
						@Override
						public void onSensorChanged(SensorEvent event)
						{
							//GETS CURRENT TILT POSITION FROM ACCELEROMETER VALUES
							direction = getDirection((int)event.values[0], (int)event.values[1], (int)event.values[2]);
						}

						@Override
						public void onAccuracyChanged(Sensor sensor, int accuracy) {}

					}, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);

					//CHECKS FOR A MATCHING GESTURE FROM DB
					int gestureId = getGestureMatch(sensorChangeCount / 2, direction, dbHelper.getEnabledGestures());

					//IF MATCHING GESTURE EXISTS, FIND AND EXECUTE ITS ACTION OR APP
					if(gestureId != 0)
					{
						String result = dbHelper.getGestureLink(gestureId);

						try
						{
							int id = Integer.parseInt(result);

							switch(id)
							{
								case 1: unlockAction(); break;
								case 2: statusBarAction(); break;
								case 3: ringerModeAction(); break;
							}

						}
						catch(NumberFormatException e)
						{
							if(!result.isEmpty())
								launchApp(result);
						}
					}

					//RESETS COUNT SO THAT SENSOR CAN BEGIN TAKING NEW INPUT
					sensorChangeCount = 0;
					direction = 0;
				}
			}, 1000);
		}
	}

	private BroadcastReceiver setScreenStateReceiver(final SensorEventListener sensorEventListener)
	{
		return new BroadcastReceiver()
		{
			@Override
			public void onReceive(Context context, Intent intent)
			{
				//HANDLES SCREEN ON/OFF ACTIONS
				if(intent.getAction().equals(Intent.ACTION_SCREEN_ON))
				{
					//RE-REGISTERS PROXIMITY SENSOR LISTENER WHEN SCREEN IS ON TO ACCEPT USER INPUT
					sensorManager.registerListener(sensorEventListener, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
					statusBarDown = false;
				}
				else if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
				{
					//LOCKS THE SCREEN WHEN SCREEN IS OFF AND NOT IN A PHONE CALL
					if(audioManager.getMode() != AudioManager.MODE_IN_CALL)
						getSystemService(Context.DEVICE_POLICY_SERVICE);

					//UNREGISTERS PROXIMITY SENSOR LISTENER WHEN SCREEN IS OFF TO PREVENT USER INPUT
					sensorManager.unregisterListener(sensorEventListener, proximitySensor);
				}
			}
		};
	}

	//CREATES AND DISPLAYS A NOTIFICATION WHEN THE SERVICE IS STARTED
	private Notification getNotification()
	{
		Intent intent = new Intent(this, SettingsActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

		Notification notification = new Notification.Builder(this)
				.setContentTitle(getString(R.string.app_name))
				.setContentText(getString(R.string.notification_text))
				.setSmallIcon(R.drawable.icon)
				.setContentIntent(PendingIntent.getActivity(this, 0, intent, 0))
				.setOngoing(true)
				.build();

		return notification;
	}

	//EXPANDS OR CONTRACTS NOTIFICATION PANEL
	private void statusBarAction()
	{
		try
		{
			Object statusBarService = getSystemService("statusbar");
			Class<?> statusBarManager = Class.forName("android.app.StatusBarManager");
			Method collapse = statusBarManager.getMethod("collapse");
			Method expand = statusBarManager.getMethod("expand");

			if(statusBarDown)
				collapse.invoke(statusBarService);
			else
				expand.invoke(statusBarService);

			statusBarDown = !statusBarDown;
		}
		catch(Exception ex) {}
	}

	//UNLOCKS DEVICE FROM LOCK SCREEN
	private void unlockAction()
	{
		if(audioManager.getMode() != AudioManager.MODE_IN_CALL && keyguardManager.inKeyguardRestrictedInputMode())
		{
			Intent intent = new Intent(getApplicationContext(), LockScreenActivity.class);
			intent.addFlags(Intent.FLAG_FROM_BACKGROUND | Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		}
	}

	private void ringerModeAction()
	{
		//CYCLES THROUGH RINGER MODES DEPENDING ON CURRENT MODE
		switch(audioManager.getRingerMode())
		{
			case AudioManager.RINGER_MODE_SILENT:
				audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
				Toast.makeText(SensorService.this, getString(R.string.ringer_vibrate), Toast.LENGTH_SHORT).show();
				break;
			case AudioManager.RINGER_MODE_VIBRATE:
				audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
				Toast.makeText(SensorService.this, getString(R.string.ringer_normal), Toast.LENGTH_SHORT).show();
				break;
			case AudioManager.RINGER_MODE_NORMAL:
				audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
				Toast.makeText(SensorService.this, getString(R.string.ringer_silent), Toast.LENGTH_SHORT).show();
				break;
		}
	}

	private void launchApp(String packageName)
	{
		//LAUNCHES APPLICATION FROM PACKAGE NAME IF IT EXISTS
		try
		{
			Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);

			if(intent != null)
				startActivity(intent);
			else
			{
				intent = new Intent(packageName);
				startActivity(intent);
			}
		}
		catch(ActivityNotFoundException err)
		{
			Toast.makeText(this, getString(R.string.app_error), Toast.LENGTH_SHORT).show();
		}
	}

	//CONVERTS ACCELEROMETER VALUES TO A DIRECTION: 1, 2, 3, 4 BEING UP, RIGHT, DOWN, OR LEFT, RESPECTIVELY
	private int getDirection(int x, int y, int z)
	{
		int direction = 0;

		if(Math.abs(y) > Math.abs(x))
		{
			if(y < -2)
				direction = 1;
			else if(y > 2)
				direction = 3;
		}
		else if(Math.abs(y) < Math.abs(x))
		{
			if(x < -2)
				direction = 2;
			else if(x > 2)
				direction = 4;
		}

		return direction;
	}

	//CHECKS THE NEWLY CREATED GESTURE WITH THE STORED AND ENABLED GESTURES IN THE DB
	private int getGestureMatch(int waves, int direction, ArrayList<Gesture> gestures)
	{
		int gestureId = 0;
		Gesture new_gesture = new Gesture(1, waves, direction);

		for(Gesture gesture : gestures)
		{
			if(new_gesture.equals(gesture))
			{
				gestureId = gesture.getId();
				break;
			}
		}

		return gestureId;
	}
}