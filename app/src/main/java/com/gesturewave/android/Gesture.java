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

import android.content.Context;

/**
 * Gesture POJO creates objects from the 'gestures' table in the database.
 * When the 'gestures' table is queried, the cursor indexes are used as the
 * arguments to the Gesture constructor
 */


public class Gesture
{
	private Context context;
	private int id;
	private boolean enabled;
	private int waves;
	private int direction;
	private int image;

	public Gesture(int enabled, int waves, int direction)
	{
		this.enabled = setEnabled(enabled);
		this.waves = waves;
		this.direction = direction;
	}

	public Gesture(Context context, int id, int enabled, int waves, int direction, String image)
	{
		this.context = context;
		this.id = id;
		this.enabled = setEnabled(enabled);
		this.waves = waves;
		this.direction = direction;
		this.image = setImage(image);
	}

	public Context getContext()
	{
		return context;
	}

	public void setContext(Context context)
	{
		this.context = context;
	}

	public int getId()
	{
		return id;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public boolean isEnabled()
	{
		return enabled;
	}

	public boolean setEnabled(int enabled)
	{
		return enabled > 0 ? true : false;
	}

	public int getWaves()
	{
		return waves;
	}

	public void setWaves(int waves)
	{
		this.waves = waves;
	}

	public int getDirection()
	{
		return direction;
	}

	public void setDirection(int direction)
	{
		this.direction = direction;
	}

	public int getImage()
	{
		return image;
	}

	public int setImage(String image)
	{
		return getContext().getResources().getIdentifier(image, "drawable", getContext().getPackageName());
	}

	@Override
	public boolean equals(Object object)
	{
		Gesture gesture = (Gesture)object;

		return (isEnabled() && gesture.isEnabled() && getWaves() == gesture.getWaves() && getDirection() == gesture.getDirection());
	}

	@Override
	public String toString()
	{
		String direction = "";

		switch(getDirection())
		{
			case 1: direction = "UP"; break;
			case 2: direction = "RIGHT"; break;
			case 3: direction = "DOWN"; break;
			case 4: direction = "LEFT"; break;
		}

		return "Tilt phone " + direction + " and wave hand " + getWaves() + " times over sensor";
	}
}