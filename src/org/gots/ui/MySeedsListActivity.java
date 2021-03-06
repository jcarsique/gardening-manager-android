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

import java.util.ArrayList;

import org.gots.allotment.sql.AllotmentDBHelper;
import org.gots.bean.BaseAllotmentInterface;
import org.gots.seed.BaseSeedInterface;
import org.gots.seed.adapter.MySeedsListAdapter;
import org.gots.seed.providers.local.sql.VendorSeedDBHelper;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListAdapter;

import com.actionbarsherlock.app.SherlockListFragment;

public class MySeedsListActivity extends SherlockListFragment {
    private MySeedsListAdapter listAdapter;

    private BaseAllotmentInterface allotment;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getActivity().getIntent().getExtras() != null) {
            String allotmentRef = getActivity().getIntent().getExtras().getString("org.gots.allotment.reference");
            if (allotmentRef != null) {
                AllotmentDBHelper helper = new AllotmentDBHelper(getActivity());
                allotment = helper.getAllotmentByName(allotmentRef);
            }
        }
        VendorSeedDBHelper myBank = new VendorSeedDBHelper(getActivity());
        ArrayList<BaseSeedInterface> mySeeds = myBank.getMySeeds();

        listAdapter = new MySeedsListAdapter(getActivity(), allotment, mySeeds);
        setListAdapter(listAdapter);

        // if (mySeeds.size() == 0) {
        // Intent intent = new Intent().setClass(this,
        // MySeedsListFirstTimeActivity.class);
        // startActivity(intent);
        // }
    }

    @Override
    public ListAdapter getListAdapter() {
        // TODO Auto-generated method stub
        return super.getListAdapter();
    }

    public void update() {
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        Log.i("onresume", this.getClass().getName());
        super.onResume();
    }

    // @Override
    // protected void onResume() {
    // super.onResume();
    // listAdapter.notifyDataSetChanged();
    //
    // }
    //
    // @Override
    // public boolean onCreateOptionsMenu(Menu menu) {
    // // MenuInflater inflater = getMenuInflater();
    // // inflater.inflate(R.menu.menu_stock, menu);
    // return super.onCreateOptionsMenu(menu);
    // }
    //
    // @Override
    // public boolean onOptionsItemSelected(MenuItem item) {
    // switch (item.getItemId()) {
    //
    // case R.id.new_seed_barcode:
    // IntentIntegrator integrator = new IntentIntegrator(this);
    // integrator.initiateScan();
    // return true;
    // case R.id.help:
    // Intent browserIntent = new Intent(Intent.ACTION_VIEW,
    // Uri.parse(HelpUriBuilder.getUri(getClass().getSimpleName())));
    // startActivity(browserIntent);
    //
    // return true;
    // default:
    // return super.onOptionsItemSelected(item);
    // }
    //
    // }
}
