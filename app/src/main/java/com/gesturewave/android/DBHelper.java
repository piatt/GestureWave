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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper
{
	private static final String DB_PATH = "/data/data/" + SettingsActivity.class.getPackage().getName() + "/databases/";
	private static final String DB_NAME = "data";
	private static final String TABLE_ACTIONS = "actions";
	private static final String TABLE_APPS = "apps";
	private static final String TABLE_GESTURES = "gestures";
	private static final String KEY_ID = "_id";
	private static final String KEY_ENABLED = "enabled";
	private static final String KEY_GESTURE = "gesture";
	private static final String KEY_ACTION = "action";
	private static final String KEY_PACKAGE = "package";
	private static final String KEY_APP = "app";
	private static final String KEY_WAVES = "waves";
	private static final String KEY_POSITION = "position";
	private static final String KEY_IMAGE = "image";
	private int MAX_APP_SHORTCUTS;
	private final Context context;
	private SQLiteDatabase db;

	public DBHelper(Context context)
	{
		super(context, DB_NAME, null, 1);
		this.context = context;
	}

	//CREATES A NEW INTERNAL DB ONLY IF IT DOESN'T ALREADY EXIST
	public void createDatabase()
	{
		boolean dbExists = checkDatabase();

		if(!dbExists)
		{
			getReadableDatabase();
			copyDatabase();
		}
	}

	//TRIES TO OPEN DB, RETURNING FALSE IF IT DOESN'T EXIST
	private boolean checkDatabase()
	{
		boolean dbExists = false;

		try
		{
			db = SQLiteDatabase.openDatabase(DB_PATH + DB_NAME, null, SQLiteDatabase.OPEN_READONLY);
		}
		catch(SQLiteException e)
		{
			Log.d(DB_NAME, "Database does not exist yet.", e);
		}

		dbExists = db != null ? true : false;

		if(db != null)
			db.close();

		return dbExists;
	}

	//CREATES A NEW INTERNAL DB, COPYING THE DATA FROM THE DATA FILE INCLUDED IN THE APK
	private void copyDatabase()
	{
		try
		{
			String file = DB_PATH + DB_NAME;
			InputStream input = context.getAssets().open(DB_NAME);
			OutputStream output = new FileOutputStream(file);

			byte[] buffer = new byte[1024];
			int length;
			while((length = input.read(buffer)) > 0)
			{
				output.write(buffer, 0, length);
			}

			output.flush();
			output.close();
			input.close();
		}
		catch(Exception e)
		{
			Log.d(DB_NAME, "Error initializing database!", e);
		}
	}

	@Override
	public void onCreate(SQLiteDatabase db) {}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

	//OPENS THE DB CONNECTION FOR QUERYING
	public void open()
	{
		db = getWritableDatabase();

		MAX_APP_SHORTCUTS = getGestures().getCount() - getActions().getCount();
	}

	//RETURNS A CURSOR CONTAINING THE ENTIRE GESTURES TABLE
	public Cursor getGestures()
	{
		return db.rawQuery("select * from " + TABLE_GESTURES, null);
	}

	//RETURNS A CURSOR CONTAINING ONLY THE GESTURES WHICH HAVE THE ENABLED FLAG SET TO FALSE
	public Cursor getAvailableGestures()
	{
		return db.rawQuery("select * from " + TABLE_GESTURES + " where " + KEY_ENABLED + " = " + 0, null);
	}

	//RETURNS A LIST OF GESTURE OBJECTS FROM THE CURSOR CONTAINING THE GESTURES HAVING THE ENABLED FLAG SET TO TRUE
	public ArrayList<Gesture> getEnabledGestures()
	{
		ArrayList<Gesture> gestures = new ArrayList<Gesture>();

		Cursor cursor = db.rawQuery("select * from " + TABLE_GESTURES + " where " + KEY_ENABLED + " = " + 1, null);

		cursor.moveToFirst();
		while(!cursor.isAfterLast())
		{
			gestures.add(new Gesture(context, cursor.getInt(0), cursor.getInt(1), cursor.getInt(2), cursor.getInt(3), cursor.getString(4)));
			cursor.moveToNext();
		}

		cursor.close();


		return gestures;
	}

	//RETURNS THE GENERATED RESOURCE ID THAT MATCHES THE NAME OF THE IMAGE RESOURCE AS STORED IN THE GESTURES TABLE
	public int getGestureImage(int id)
	{
		int image = 0;

		if(id == 0)
			image = R.drawable.gesture0;
		else
		{
			Cursor cursor = db.rawQuery("select * from " + TABLE_GESTURES + " where " + KEY_ID + " = " + id, null);
			cursor.moveToFirst();

			image = new Gesture(context, cursor.getInt(0), cursor.getInt(1), cursor.getInt(2), cursor.getInt(3), cursor.getString(4)).getImage();

			cursor.close();
		}

		return image;
	}

	//RETURNS THE GESTURE ID STORED IN EITHER THE ACTIONS OR APPS TABLE FOR A PARTICULAR ACTION OR APP
	public int getGestureId(int table, int id)
	{
		int gesture = 0;
		Cursor cursor;

		switch(table)
		{
			case 0: cursor = db.rawQuery("select * from " + TABLE_ACTIONS + " where " + KEY_ID + " = " + id, null);
					cursor.moveToFirst();
					gesture = new Action(cursor.getInt(1), cursor.getInt(2), cursor.getString(3)).getGesture();
					cursor.close();
					break;
			case 1: cursor = db.rawQuery("select * from " + TABLE_APPS + " where " + KEY_ID + " = " + id, null);
					cursor.moveToFirst();
					gesture = new App(cursor.getInt(1), cursor.getInt(2), cursor.getString(3), cursor.getString(4)).getGesture();
					cursor.close();
					break;
		}

		return gesture;
	}

	//SETS THE STATUS OF THE GESTURE FLAG TO EITHER TRUE OR FALSE
	public void updateGestureEnabled(int id, boolean enabled)
	{
		if(id != 0)
		{
			int status = enabled ? 1 : 0;

			ContentValues values = new ContentValues();
			values.put(KEY_ENABLED, status);

			db.update(TABLE_GESTURES, values, KEY_ID + " = ?", new String[]{String.valueOf(id)});
		}
	}

	//THIS IS CALLED WHEN THE USER CHOOSES A GESTURE TO LINK WITH A PARTICULAR ACTION OR APP
	//FIRST THE PREVIOUSLY LINKED GESTURE HAS ITS ENABLED FLAG SET TO FALSE,
	//THEN THE NEWLY LINKED GESTURE HAS ITS ENABLED FLAG SET TO TRUE.
	//LASTLY, THE ACTION OR APP ENTRY THAT WAS CHANGED IS UPDATED WITH THE NEW GESTURE ID
	public void linkGesture(int table, int id, int gesture)
	{
		updateGestureEnabled(getGestureId(table, id), false);
		updateGestureEnabled(gesture, true);

		ContentValues values = new ContentValues();
		values.put(KEY_GESTURE, gesture);

		switch(table)
		{
			case 0: db.update(TABLE_ACTIONS, values, KEY_ID + " = ?", new String[]{String.valueOf(id)}); break;
			case 1: db.update(TABLE_APPS, values, KEY_ID + " = ?", new String[]{String.valueOf(id)}); break;
		}
	}

	//THIS IS CALLED WHEN THE USER EXECUTES A GESTURE IN THE SENSOR SERVICE
	//EITHER AN ACTION ID OR AN APP PACKAGE NAME IS RETURNED, LETTING THE SERVICE KNOW WHAT TO DO
	public String getGestureLink(int id)
	{
		String result = "";

		Cursor action_cursor = db.rawQuery("select " + KEY_ID + " from " + TABLE_ACTIONS + " where " + KEY_GESTURE + " = " + id + " and " + KEY_ENABLED + " = " + 1, null);
		Cursor app_cursor = db.rawQuery("select " + KEY_PACKAGE + " from " + TABLE_APPS + " where " + KEY_GESTURE + " = " + id + " and " + KEY_ENABLED + " = " + 1, null);

		if(action_cursor.getCount() > 0)
		{
			action_cursor.moveToFirst();
			result = String.valueOf(action_cursor.getInt(0));
			action_cursor.close();
		}
		else if(app_cursor.getCount() > 0)
		{
			app_cursor.moveToFirst();
			result = app_cursor.getString(0);
			app_cursor.close();
		}

		return result;
	}

	//RETURNS A CURSOR CONTAINING THE ENTIRE ACTIONS TABLE
	public Cursor getActions()
	{
		return db.rawQuery("select * from " + TABLE_ACTIONS, null);
	}

	//RETURNS A CURSOR CONTAINING THE ENTIRE APPS TABLE
	public Cursor getApps()
	{
		return db.rawQuery("select * from " + TABLE_APPS, null);
	}

	//SETS THE STATUS OF THE ACTION ENABLED FLAG TO EITHER TRUE OR FALSE
	public void updateActionEnabled(int id, boolean enabled)
	{
		int status = enabled ? 1 : 0;

		ContentValues values = new ContentValues();
		values.put(KEY_ENABLED, status);

		db.update(TABLE_ACTIONS, values, KEY_ID + " = ?", new String[]{String.valueOf(id)});
	}

	//SETS THE STATUS OF THE APP ENABLED FLAG TO EITHER TRUE OR FALSE
	public void updateAppEnabled(int id, boolean enabled)
	{
		int status = enabled ? 1 : 0;

		ContentValues values = new ContentValues();
		values.put(KEY_ENABLED, status);

		db.update(TABLE_APPS, values, KEY_ID + " = ?", new String[]{String.valueOf(id)});
	}

	//UPDATES THE PACKAGE NAME AND APP NAME LINKED TO AN APP ENTRY IN THE DB
	//WHEN A USER CHOOSES AN APP FROM THE LIST OF INSTALLED APPS ON THEIR DEVICE
	public void updateAppNames(int id, String package_name, String app_name)
	{
		ContentValues values = new ContentValues();
		values.put(KEY_PACKAGE, package_name);
		values.put(KEY_APP, app_name);

		db.update(TABLE_APPS, values, KEY_ID + " = ?", new String[]{String.valueOf(id)});
	}

	//THIS IS CALLED WHEN THE USER CLICKS THE ADD BUTTON TO ADD AN APP SHORTCUT
	//THIS CREATES A NEW APP SHORTCUT ENTRY IN THE APPS TABLE, INITIALIZED TO DEFAULT VALUES
	//THIS METHOD ONLY ALLOWS THE USER TO CREATE AS MANY APPS AS THERE ARE AVAILABLE GESTURES
	public void addApp()
	{
		if(getApps().getCount() < MAX_APP_SHORTCUTS)
		{
			ContentValues values = new ContentValues();
			values.put(KEY_ENABLED, 0);

			db.insert(TABLE_APPS, null, values);
		}
		else
			Toast.makeText(context, R.string.add_error, Toast.LENGTH_SHORT).show();
	}

	//THIS IS CALLED WHEN THE USER LONG PRESSES ON AN APP SHORTCUT ENTRY
	//THIS REMOVES THE APP SHORTCUT ENTRY FROM THE APPS TABLE
	//AND FREES UP THE LINKED GESTURE AS AVAILABLE AGAIN
	public void deleteApp(int id)
	{
		updateGestureEnabled(getGestureId(1, id), false);

		db.delete(TABLE_APPS, KEY_ID + " = ?", new String[]{String.valueOf(id)});
	}
}