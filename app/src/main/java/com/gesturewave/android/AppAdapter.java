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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.CompoundButton.OnCheckedChangeListener;
import java.util.List;

/**
 * AppAdapter implements a custom CursorAdapter for displaying and
 * handling events related to the 'app_list' ListView.
 * App items are pulled from the database and converted to objects,
 * where they are then assigned to their respective ListView row.
 * When a checkbox is toggled, the database is updated to reflect the new checkbox status.
 * When an app icon button is pressed, a dialog list is displayed,
 * which allows the user to select an app to bind to that row from
 * the list of installed apps on the device. The database is updated with the selection,
 * and the ListView is refreshed with a updated Cursor pulled from the database.
 */

public class AppAdapter extends CursorAdapter implements OnClickListener, OnCheckedChangeListener
{
	private Context context;
	private DBHelper dbHelper;

	public AppAdapter(Context context, DBHelper dbHelper, Cursor cursor)
	{
		super(context, cursor, FLAG_REGISTER_CONTENT_OBSERVER);

		this.context = context;
		this.dbHelper = dbHelper;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent)
	{
		return LayoutInflater.from(context).inflate(R.layout.app_item, parent, false);
	}

	@Override
	public void bindView(View row, Context context, Cursor cursor)
	{
		App app = new App(cursor.getInt(1), cursor.getInt(2), cursor.getString(3), cursor.getString(4));

		row.setTag(cursor.getInt(0));

		CheckBox checkbox = (CheckBox)row.findViewById(R.id.checkbox);
		checkbox.setTag(cursor.getInt(0));
		checkbox.setChecked(app.isEnabled());
		checkbox.setOnCheckedChangeListener(this);

		ImageView gesture = (ImageView)row.findViewById(R.id.gesture);
		gesture.setImageResource(dbHelper.getGestureImage(app.getGesture()));

		TextView text = (TextView)row.findViewById(R.id.text);
		text.setText(app.getAppName());

		ImageButton image = (ImageButton)row.findViewById(R.id.image);
		image.setTag(cursor.getInt(0));
		image.setOnClickListener(this);

		if(!app.getPackageName().isEmpty())
		{
			try
			{
				image.setImageDrawable(context.getPackageManager().getApplicationIcon(app.getPackageName()));
			}
			catch(PackageManager.NameNotFoundException e) {}
		}
		else
			image.setImageResource(R.drawable.icon);
	}

	@Override
	public void onClick(View view)
	{
		final int rowId = (Integer)view.getTag();

		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.addCategory(Intent.CATEGORY_LAUNCHER);

		List<ResolveInfo> applications = context.getPackageManager().queryIntentActivities(intent, 0);

		final ListAdapter adapter = new ListAdapter(context, R.layout.list_item, applications.toArray());

		new AlertDialog.Builder(context).setTitle(R.string.dialog_title)
		                                .setAdapter(adapter, new DialogInterface.OnClickListener()
		                                {
			                                @Override
			                                public void onClick(DialogInterface dialog, int which)
			                                {
				                                ResolveInfo application = (ResolveInfo)adapter.getItem(which);

				                                dbHelper.updateAppNames(rowId, application.activityInfo.applicationInfo.packageName.toString(), application.loadLabel(context.getPackageManager()).toString());
				                                changeCursor(dbHelper.getApps());
			                                }
		                                }).create().show();
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	{
		dbHelper.updateAppEnabled((Integer)buttonView.getTag(), isChecked);
	}
}