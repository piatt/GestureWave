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

/**
 * Action POJO creates objects from the 'actions' table in the database.
 * When the 'actions' table is queried, the cursor indexes are used as the
 * arguments to the Action constructor.
 */

public class Action
{
	private boolean enabled;
	private int gesture;
	private String action;

	public Action(int enabled, int gesture, String action)
	{
		this.enabled = setEnabled(enabled);
		this.gesture = gesture;
		this.action = action;
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public boolean setEnabled(int enabled)
	{
		return enabled > 0 ? true : false;
	}

	public int getGesture()
	{
		return gesture;
	}

	public void setGesture(int gesture)
	{
		this.gesture = gesture;
	}

	public String getAction()
	{
		return action;
	}

	public void setAction(String action)
	{
		this.action = action;
	}
}