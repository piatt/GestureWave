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

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

/**
 * SettingsActivity is the first activity that is started when the app runs.
 * The purpose of this activity is simply to handle switching the service on or off,
 * and to start the settings interface fragment. Logic is included to make sure that
 * the state of the service toggle switch is accurate even after the activity is
 * paused or destroyed as well as for when the user toggles the switch manually.
 */

public class SettingsActivity extends Activity implements OnCheckedChangeListener
{
    private Switch service_switch;
	private boolean running = false;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);
		getFragmentManager().findFragmentById(R.id.settings);

		running = isServiceRunning();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu, menu);

		View view = menu.findItem(R.id.menu_switch).getActionView();
		service_switch = (Switch)view.findViewById(R.id.service_switch);
		service_switch.setChecked(running);
		service_switch.setOnCheckedChangeListener(this);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	{
		if(!running)
		{
			startService(new Intent(this, SensorService.class));
			running = true;
		}
		else
		{
			stopService(new Intent(this, SensorService.class));
			running = false;
		}
	}

	private boolean isServiceRunning()
	{
		boolean running = false;

		ActivityManager manager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);

		for(RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
		{
			if(SensorService.class.getName().equals(service.service.getClassName()))
			{
				running = true;
				break;
			}
		}

		return running;
	}
}