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
import android.content.pm.ResolveInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * ListAdapter implements a custom ArrayAdapter for displaying and
 * handling events related to the 'list_item' dialog.
 * This adapter takes a list of objects containing system information
 * about installed apps, and assigns names and icons to their respective list row.
 * Since this adapter is solely used for displaying the list, the logic for
 * handling the selection of a list row is handled where the dialog that
 * uses this adapter is created.
 */

public class ListAdapter extends ArrayAdapter<Object>
{
	private Context context;
	private int layoutId;
	private Object[] list;

	public ListAdapter(Context context, int layoutId, Object[] list)
	{
		super(context, layoutId, list);

		this.context = context;
		this.layoutId = layoutId;
		this.list = list;
	}

	public View getView(int position, View convertView, ViewGroup parent)
	{
		View row = convertView;

		if(row == null)
			row = LayoutInflater.from(context).inflate(layoutId, null);

		ImageView icon = (ImageView)row.findViewById(R.id.image);
		icon.setImageDrawable(((ResolveInfo)list[position]).loadIcon(context.getPackageManager()));

		TextView name = (TextView)row.findViewById(R.id.name);
		name.setText(((ResolveInfo)list[position]).loadLabel(context.getPackageManager()));

		return row;
	}
}