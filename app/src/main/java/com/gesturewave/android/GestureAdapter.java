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
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

/**
 * GestureAdapter implements a custom CursorAdapter for displaying and
 * handling events related to the 'gesture_item' dialog.
 * Gesture items are pulled from the database and converted to objects,
 * where they are then assigned to their respective list row.
 * Since this adapter is solely used for displaying the list, the logic for
 * handling the selection of a list row is handled where the dialog that
 * uses this adapter is created.
 */

public class GestureAdapter extends CursorAdapter
{
	public GestureAdapter(Context context, Cursor cursor)
	{
		super(context, cursor, FLAG_REGISTER_CONTENT_OBSERVER);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent)
	{
		return LayoutInflater.from(context).inflate(R.layout.gesture_item, parent, false);
	}

	@Override
	public void bindView(View row, Context context, Cursor cursor)
	{
		Gesture gesture = new Gesture(context, cursor.getInt(0), cursor.getInt(1), cursor.getInt(2), cursor.getInt(3), cursor.getString(4));

		row.setTag(gesture.getId());

		ImageView image = (ImageView)row.findViewById(R.id.image);
		image.setImageResource(gesture.getImage());
	}
}