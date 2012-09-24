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
package org.gots.ui;

import org.gots.R;
import org.gots.analytics.GotsAnalytics;
import org.gots.seed.BaseSeedInterface;
import org.gots.seed.sql.VendorSeedDBHelper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Gallery;
import android.widget.TextView;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class SeedActivity extends Activity {
	private int seedId;

	protected BaseSeedInterface mSeed;

	private float downXValue;

	protected int resultCameraActivity = 1;

	private Gallery gallery;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.seed);

		GotsAnalytics.getInstance(getApplication()).incrementActivityCount();
		GoogleAnalyticsTracker.getInstance().trackPageView(getClass().getSimpleName());

		if (getIntent().getExtras() == null || getIntent().getExtras().getInt("org.gots.seed.id") <= 0) {
			Log.e("SeedActivity", "You must provide a org.gots.seed.id as an Extra Int");
			finish();
			return;
		}
		this.seedId = getIntent().getExtras().getInt("org.gots.seed.id");
		VendorSeedDBHelper helper = new VendorSeedDBHelper(this);
		mSeed = helper.getSeedById(seedId);

		TextView seedDescription = (TextView) findViewById(R.id.IdSeedDescriptionEnvironment);
		seedDescription.setText(mSeed.getDescriptionGrowth());

		seedDescription = (TextView) findViewById(R.id.IdSeedDescriptionCulture);
		seedDescription.setText(mSeed.getDescriptionCultivation());

		seedDescription = (TextView) findViewById(R.id.IdSeedDescriptionEnnemi);
		seedDescription.setText(mSeed.getDescriptionDiseases());

		seedDescription = (TextView) findViewById(R.id.IdSeedDescriptionHarvest);
		seedDescription.setText(mSeed.getDescriptionHarvest());

		

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == resultCameraActivity) {
			gallery.refreshDrawableState();
			gallery.invalidate();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	protected void onDestroy() {
	GotsAnalytics.getInstance(getApplication()).decrementActivityCount();
		super.onDestroy();
	}

}
