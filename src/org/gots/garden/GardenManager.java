package org.gots.garden;

import java.util.List;

import org.gots.DatabaseHelper;
import org.gots.broadcast.BroadCastMessages;
import org.gots.garden.provider.GardenProvider;
import org.gots.garden.provider.local.LocalGardenProvider;
import org.gots.garden.provider.nuxeo.NuxeoGardenProvider;
import org.gots.preferences.GotsPreferences;
import org.gots.utils.NotConfiguredException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class GardenManager extends BroadcastReceiver {
    private static final String TAG = "GardenManager";

    private static GardenManager instance;

    private static Exception firstCall;

    private Context mContext;

    private GardenProvider gardenProvider = null;

    private boolean initDone = false;

    private GardenManager() {
    }

    /**
     * After first call, {@link #initIfNew(Context)} must be called else a {@link NotConfiguredException} will be thrown
     * on the second call attempt.
     */
    public static synchronized GardenManager getInstance() {
        if (instance == null) {
            instance = new GardenManager();
            firstCall = new Exception();
        } else if (!instance.initDone) {
            throw new NotConfiguredException(firstCall);
        }
        return instance;
    }

    /**
     * If it was already called once, the method returns without any change.
     */
    public synchronized void initIfNew(Context context) {
        if (initDone) {
            return;
        }
        this.mContext = context;
        setGardenProvider();
        initDone = true;
    }

    private void setGardenProvider() {
        // new AsyncTask<Void, Integer, Void>() {
        // @Override
        // protected Void doInBackground(Void... params) {
        if (GotsPreferences.getInstance().isConnectedToServer()) {
            gardenProvider = new NuxeoGardenProvider(mContext);
        } else {
            // return null;
            // }
            // }.execute();
            gardenProvider = new LocalGardenProvider(mContext);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (BroadCastMessages.CONNECTION_SETTINGS_CHANGED.equals(intent.getAction())) {
            setGardenProvider();
        }
    }

    public long addGarden(GardenInterface garden) {
        GardenInterface newGarden = gardenProvider.createGarden(garden);

        setCurrentGarden(newGarden);

        GoogleAnalyticsTracker tracker = GoogleAnalyticsTracker.getInstance();
        tracker.trackEvent("Garden", "location", newGarden.getLocality(), 0);

        return newGarden.getId();
    }

    private void changeDatabase(int position) {
        DatabaseHelper helper = new DatabaseHelper(mContext);
        helper.setDatabase(position);

        // WeatherManager wm = new WeatherManager(mContext);
        // wm.getWeatherFromWebService(getcurrentGarden());

    }

    public GardenInterface getCurrentGarden() {
        GardenInterface garden = gardenProvider.getCurrentGarden();
        if (garden != null)
            changeDatabase((int) garden.getId());
        return garden;
    }

    public void setCurrentGarden(GardenInterface garden) {
        GotsPreferences.getInstance().set(GotsPreferences.ORG_GOTS_CURRENT_GARDENID, (int) garden.getId());
        Log.d("setCurrentGarden", "[" + garden.getId() + "] " + garden.getLocality()
                + " has been set as current workspace");
        changeDatabase((int) garden.getId());
    }

    public void removeGarden(GardenInterface garden) {
        gardenProvider.removeGarden(garden);
    }

    public void updateCurrentGarden(GardenInterface garden) {
        gardenProvider.updateGarden(garden);
    }

    public void update() {
        // new RefreshTask().execute(new Object(), false);

    }

    // private class RefreshTask extends AsyncTask<Object, Boolean, Long> {
    // @Override
    // protected Long doInBackground(Object... params) {
    //
    // GotsConnector connector;
    // if (!isLocalStore)
    // // connector = new SimpleConnector();
    // connector = new NuxeoConnector(mContext);
    // else
    // connector = new LocalConnector(mContext);
    // List<BaseSeedInterface> seeds = connector.getAllSeeds();
    //
    // VendorSeedDBHelper theSeedBank = new VendorSeedDBHelper(mContext);
    // for (Iterator<BaseSeedInterface> iterator = seeds.iterator();
    // iterator.hasNext();) {
    // BaseSeedInterface baseSeedInterface = iterator.next();
    // if (theSeedBank.getSeedByReference(baseSeedInterface.getReference()) ==
    // null)
    // theSeedBank.insertSeed(baseSeedInterface);
    //
    // }
    // return null;
    // }
    //
    // @Override
    // protected void onPostExecute(Long result) {
    // // VendorSeedDBHelper myBank = new VendorSeedDBHelper(mContext);
    // // ArrayList<BaseSeedInterface> vendorSeeds;
    // // vendorSeeds = myBank.getVendorSeeds();
    //
    // // setListAdapter(new ListVendorSeedAdapter(mContext, vendorSeeds));
    // Toast.makeText(mContext, "Updated", 20).show();
    //
    // super.onPostExecute(result);
    // }
    // }

    public List<GardenInterface> getMyGardens() {
        return gardenProvider.getMyGardens();
    }

}
