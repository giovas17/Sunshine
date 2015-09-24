package softwaremobility.darkgeat.sunshine.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import softwaremobility.darkgeat.sunshine.sync.SyncAdapter;

/**
 * Created by darkgeat on 9/24/15.
 */
public class SunshineSyncService extends Service {

    private static final String LOG_TAG = SunshineSyncService.class.getSimpleName();
    private static final Object sSyncAdapterLock = new Object();
    private static SyncAdapter sunshineSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.d(LOG_TAG,"onCreate - " + LOG_TAG);
        synchronized (sSyncAdapterLock){
            if(sunshineSyncAdapter == null){
                sunshineSyncAdapter = new SyncAdapter(getApplicationContext(),true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sunshineSyncAdapter.getSyncAdapterBinder();
    }
}
