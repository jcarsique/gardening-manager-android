package org.gots.seed;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.gots.broadcast.BroadCastMessages;
import org.gots.garden.GardenInterface;
import org.gots.garden.GardenManager;
import org.gots.preferences.GotsPreferences;
import org.gots.seed.provider.GotsSeedProvider;
import org.gots.seed.provider.local.LocalSeedProvider;
import org.gots.seed.provider.nuxeo.NuxeoSeedProvider;
import org.gots.utils.NotConfiguredException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

public class GotsSeedManager extends BroadcastReceiver implements GotsSeedProvider {

    private static final String TAG = "GotsSeedManager";

    private static GotsSeedManager instance;

    private Context mContext;

    private GotsSeedProvider mSeedProvider;

    private boolean initDone = false;

    private static Exception firstCall;

    private GotsSeedManager() {
        // mLocalProvider = new LocalSeedProvider(mContext);
    }

    protected void setSeedProvider() {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (GotsPreferences.getInstance().isConnectedToServer() && ni != null && ni.isConnected()) {
            mSeedProvider = new NuxeoSeedProvider(mContext);
        } else
            mSeedProvider = new LocalSeedProvider(mContext);
    }

    public static synchronized GotsSeedManager getInstance() {
        if (instance == null) {
            instance = new GotsSeedManager();
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
//        mContext.registerReceiver(this, new IntentFilter(BroadCastMessages.CONNECTION_SETTINGS_CHANGED));
        setSeedProvider();
        initDone = true;
    }

    public void finalize() {
//        mContext.unregisterReceiver(this);
        initDone = false;
        mContext = null;
        instance = null;
    }

    @Override
    public List<BaseSeedInterface> getVendorSeeds(boolean force) {

        return new NuxeoSeedProvider(mContext).getVendorSeeds(force);
    }

    @Override
    public void getAllFamilies() {
        // TODO Auto-generated method stub

    }

    @Override
    public void getFamilyById(int id) {
        // TODO Auto-generated method stub

    }

    @Override
    public BaseSeedInterface getSeedById() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BaseSeedInterface createSeed(BaseSeedInterface seed) {
        AsyncTask<BaseSeedInterface, Integer, BaseSeedInterface> task = new AsyncTask<BaseSeedInterface, Integer, BaseSeedInterface>() {
            @Override
            protected BaseSeedInterface doInBackground(BaseSeedInterface... params) {

                return mSeedProvider.createSeed(params[0]);
            }
        }.execute(seed);
        try {
            return task.get();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            Log.e(TAG, e.getMessage(), e);
        } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }

    @Override
    public BaseSeedInterface updateSeed(BaseSeedInterface newSeed) {
        return mSeedProvider.updateSeed(newSeed);
    }

    @Override
    public void addToStock(BaseSeedInterface vendorSeed, GardenInterface garden) {
        mSeedProvider.addToStock(vendorSeed, garden);
    }

    @Override
    public void removeToStock(BaseSeedInterface vendorSeed) {
        mSeedProvider.removeToStock(vendorSeed);

    }

    @Override
    public List<BaseSeedInterface> getMyStock(GardenInterface garden) {
        return mSeedProvider.getMyStock(garden);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (BroadCastMessages.CONNECTION_SETTINGS_CHANGED.equals(intent.getAction())||BroadCastMessages.GARDEN_SETTINGS_CHANGED.equals(intent.getAction())) {
            setSeedProvider();
        }
    }

    @Override
    public void remove(BaseSeedInterface vendorSeed) {
        mSeedProvider.remove(vendorSeed);
    }
}
