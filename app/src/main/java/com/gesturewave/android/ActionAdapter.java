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
import android.widget.*;
import android.widget.CompoundButton.OnCheckedChangeListener;

/**
 * ActionAdapter implements a custom CursorAdapter for displaying and
 * handling events related to the 'action_list' ListView.
 * Action items are pulled from the database and converted to objects,
 * where they are then assigned to their respective ListView row.
 * When a checkbox is toggled, the database is updated to reflect the new checkbox status.
 */

public class ActionAdapter extends CursorAdapter implements OnCheckedChangeListener
{
	private DBHelper dbHelper;

	public ActionAdapter(Context context, DBHelper dbHelper, Cursor cursor)
	{
		super(context, cursor, FLAG_REGISTER_CONTENT_OBSERVER);

		this.dbHelper = dbHelper;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent)
	{
		 return LayoutInflater.from(context).inflate(R.layout.action_item, parent, false);
	}

	@Override
	public void bindView(View row, Context context, Cursor cursor)
	{
		Action action = new Action(cursor.getInt(1), cursor.getInt(2), cursor.getString(3));

		row.setTag(cursor.getInt(0));

		CheckBox checkbox = (CheckBox)row.findViewById(R.id.checkbox);
		checkbox.setTag(cursor.getInt(0));
		checkbox.setChecked(action.isEnabled());
		checkbox.setOnCheckedChangeListener(this);

		ImageView image = (ImageView)row.findViewById(R.id.image);
		image.setImageResource(dbHelper.getGestureImage(action.getGesture()));

		TextView text = (TextView)row.findViewById(R.id.text);
		text.setText(action.getAction());
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	{
		dbHelper.updateActionEnabled((Integer)buttonView.getTag(), isChecked);
	}
}