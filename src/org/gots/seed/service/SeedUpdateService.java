package org.gots.seed.service;

import java.util.ArrayList;
import java.util.List;

import org.gots.broadcast.BroadCastMessages;
import org.gots.seed.BaseSeedInterface;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class SeedUpdateService extends GotsService {
    public static final String ISNEWSEED = "org.gots.isnewseed";


    static Intent intent = null;

    private static boolean isNewSeed = false;


    private List<BaseSeedInterface> newSeeds = new ArrayList<BaseSeedInterface>();

    private String TAG = "SeedUpdateService";

    // private GotsSeedProvider mRemoteProvider;

    private Handler handler = new Handler();

    @Override
    public IBinder onBind(Intent arg0) {
        intent = new Intent(BroadCastMessages.SEED_DISPLAYLIST);

        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // List<BaseSeedInterface> newSeeds = new
        // ArrayList<BaseSeedInterface>();
        Log.d(TAG, "Starting service : checking seeds from web services");

        // VendorSeedDBHelper helper = new VendorSeedDBHelper(this);
        // mRemoteProvider.getVendorSeeds();
        new AsyncTask<Void, Integer, List<BaseSeedInterface>>() {

            protected void onPreExecute() {

                super.onPreExecute();
            };

            @Override
            protected List<BaseSeedInterface> doInBackground(Void... params) {
                return seedManager.getVendorSeeds(true);

            }

            protected void onPostExecute(List<BaseSeedInterface> vendorSeeds) {
                newSeeds = seedManager.getNewSeeds();
                if (newSeeds != null && newSeeds.size() > 0) {
                    SeedNotification notification = new SeedNotification(getApplicationContext());
                    notification.createNotification (newSeeds);
                }
                handler.removeCallbacks(sendUpdatesToUI);
                handler.postDelayed(sendUpdatesToUI, 0); // 1 second
                super.onPostExecute(vendorSeeds);
                stopSelf();
            };
        }.execute();

        return super.onStartCommand(intent, flags, startId);
    }

    private Runnable sendUpdatesToUI = new Runnable() {
        public void run() {
            displaySeedsAvailable();
            // handler.postDelayed(this, 5000); // 5 seconds
            // stopSelf();
        }
    };


    private void displaySeedsAvailable() {
        Log.d(TAG, "displaySeedsAvailable send broadcast");

        intent.putExtra(ISNEWSEED, isNewSeed);
        // intent.putExtra("counter", String.valueOf(++counter));
        sendBroadcast(intent);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        // mNM.cancel(NOTIFICATION);
        Log.d(TAG, "Stopping service : " + newSeeds.size() + " seeds found");
        super.onDestroy();

    }

}
