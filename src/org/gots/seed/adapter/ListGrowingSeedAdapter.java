/*******************************************************************************
 * Copyright (c) 2012 sfleury.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     sfleury - initial API and implementation
 ******************************************************************************/
package org.gots.seed.adapter;

import java.util.List;

import org.gots.R;
import org.gots.seed.GrowingSeed;
import org.gots.seed.GrowingSeedInterface;
import org.gots.seed.view.QuickSeedActionBuilder;
import org.gots.seed.view.SeedWidget;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ListGrowingSeedAdapter extends BaseAdapter implements OnClickListener {
	Context mContext;

	List<GrowingSeedInterface> mySeeds;

	// String currentAllotmentReference;
	BaseAdapter parentAdapter;

	public ListGrowingSeedAdapter(Context mContext, List<GrowingSeedInterface> seeds, BaseAdapter parentAdapter) {
		this.mContext = mContext;
		// this.currentAllotmentReference = allotmentReference;
		mySeeds = seeds;
		this.parentAdapter = parentAdapter;

	}

	@Override
	public int getCount() {
		return mySeeds.size();
	}

	@Override
	public GrowingSeedInterface getItem(int position) {
		return mySeeds.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		SeedWidget seedWidget = (SeedWidget) convertView;
		if (convertView == null) {
			GrowingSeed currentSeed = (GrowingSeed) getItem(position);

			seedWidget = new SeedWidget(mContext);
			seedWidget.setSeed(currentSeed);
			seedWidget.setOnClickListener(this);
			seedWidget.setTag(currentSeed);
			seedWidget.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.action_selector));

			// seedWidget.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.parcelle_dark_bg));

		} else {

		}

		return seedWidget;
	}

	@Override
	public void notifyDataSetChanged() {
		// GrowingSeedDBHelper helper = new GrowingSeedDBHelper(mContext);
		// mySeeds = helper.getSeedsByAllotment(currentAllotmentReference);

		// parentAdapter.notifyDataSetChanged();
		super.notifyDataSetChanged();
	}

	static class SeedViewHolder {
		TextView seedName;

		TextView seedHarvestPeriod;

		TextView seedSowingPeriod;
	}

	@Override
	public void onClick(View v) {
		QuickSeedActionBuilder actionBuilder = new QuickSeedActionBuilder((SeedWidget) v, parentAdapter);
		actionBuilder.show();
		// notifyDataSetChanged();
	}

}
