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
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

/**
 *  LockScreenActivity acts as a dummy activity to invisibly allow the system
 *  to unlock the screen. Since unlocking the screen involves gaining access to
 *  the current window in an activity, calling this from a running service
 *  involves creating a dummy activity for execution. This activity simply executes
 *  the required WindowManager functions and then is destroyed.
 */

public class LockScreenActivity extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
		                     | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
		                     | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
		                     | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		new Handler().postDelayed(new Runnable()
		{
			public void run()
			{
				LockScreenActivity.this.finish();
			}
		}, 500);
	}
}