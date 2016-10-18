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
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.ListView;

public class SettingsFragment extends Fragment implements OnClickListener, OnItemClickListener, OnItemLongClickListener
{
	private Context context;
	private DBHelper dbHelper;
	private ImageButton action_help;
	private ImageButton app_add;
	private ImageButton app_help;
	private CursorAdapter action_adapter;
	private CursorAdapter app_adapter;
	private ListView action_list;
	private ListView app_list;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.settings_list, container, false);

		context = this.getActivity();

		//CREATES A NEW DATABASE INSTANCE, CREATES IT IF IT DOESN'T EXIST, AND OPENS IT FOR QUERYING
		dbHelper = new DBHelper(context);
		dbHelper.createDatabase();
		dbHelper.open();

		//INITIALIZES BUTTONS AND SETS CLICK LISTENERS
		action_help = (ImageButton)view.findViewById(R.id.action_help);
		action_help.setOnClickListener(this);
		app_add = (ImageButton)view.findViewById(R.id.app_add);
		app_add.setOnClickListener(this);
		app_help = (ImageButton)view.findViewById(R.id.app_help);
		app_help.setOnClickListener(this);

		//INITIALIZES ACTION LISTVIEW, GETS DATA FROM DB, AND SETS CLICK LISTENERS
		action_adapter = new ActionAdapter(context, dbHelper, dbHelper.getActions());

		action_list = (ListView)view.findViewById(R.id.action_list);
		action_list.setAdapter(action_adapter);
		action_list.setOnItemClickListener(this);

		//INITIALIZES APP LISTVIEW, GETS DATA FROM DB, AND SETS CLICK AND LONG CLICK LISTENERS
		app_adapter = new AppAdapter(context, dbHelper, dbHelper.getApps());

		app_list = (ListView)view.findViewById(R.id.app_list);
		app_list.setAdapter(app_adapter);
		app_list.setOnItemClickListener(this);
		app_list.setOnItemLongClickListener(this);

		return view;
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();

		//CLOSES DATABASE CONNECTION WHEN APP CLOSES
		dbHelper.close();
	}

	@Override
	public void onClick(View view)
	{
		switch(view.getId())
		{
			case R.id.action_help: showActionHelpDialog();
								   break;
			case R.id.app_add: dbHelper.addApp();
							   app_adapter.changeCursor(dbHelper.getApps());
							   break;
			case R.id.app_help: showAppHelpDialog();
								break;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> listView, View view, int position, long id)
	{
		final int listId = listView.getId();
		final int rowId = (Integer)view.getTag();

		final GestureAdapter adapter = new GestureAdapter(context, dbHelper.getAvailableGestures());

		//CREATES AND DISPLAYS DIALOG WITH LIST OF AVAILABLE GESTURES, PULLED FROM DB, AND UPDATES DB WITH USER CHOICE
		new AlertDialog.Builder(context).setTitle(R.string.gesture_title)
		                                .setAdapter(adapter, new DialogInterface.OnClickListener()
		                                {
			                                @Override
			                                public void onClick(DialogInterface dialog, int which)
			                                {
				                                Cursor cursor = (Cursor)adapter.getItem(which);

				                                if(listId == R.id.action_list)
				                                {
					                                dbHelper.linkGesture(0, rowId, cursor.getInt(0));
					                                action_adapter.changeCursor(dbHelper.getActions());
				                                }
				                                else if(listId == R.id.app_list)
				                                {
					                                dbHelper.linkGesture(1, rowId, cursor.getInt(0));
					                                app_adapter.changeCursor(dbHelper.getApps());
				                                }
			                                }
		                                }).create().show();
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> listView, View view, int position, long id)
	{
		final int rowId = (Integer)view.getTag();

		//CREATES AND DISPLAYS DIALOG CONFIRMING ROW DELETION AND UPDATES DB WITH USER CHOICE
		new AlertDialog.Builder(context).setMessage(R.string.delete_message)
		       .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener()
		       {
			       public void onClick(DialogInterface dialog, int id)
			       {
				   	    dbHelper.deleteApp(rowId);
				        app_adapter.changeCursor(dbHelper.getApps());
			       }
		       })
		       .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
		       {
			       public void onClick(DialogInterface dialog, int id) {}
		       }).create().show();

		return true;
	}

	//CREATES AND DISPLAYS HELP DIALOG FOR ACTION LISTVIEW
	private void showActionHelpDialog()
	{
		new AlertDialog.Builder(context).setTitle(R.string.action_help_title).setView(LayoutInflater.from(context).inflate(R.layout.action_dialog, null)).create().show();
	}

	//CREATES AND DISPLAYS HELP DIALOG FOR APP LISTVIEW
	private void showAppHelpDialog()
	{
		new AlertDialog.Builder(context).setTitle(R.string.app_help_title).setView(LayoutInflater.from(context).inflate(R.layout.app_dialog, null)).create().show();
	}
}