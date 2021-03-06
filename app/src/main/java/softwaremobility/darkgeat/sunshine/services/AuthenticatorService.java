package softwaremobility.darkgeat.sunshine.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import softwaremobility.darkgeat.sunshine.sync.Authenticator;

/**
 * Created by darkgeat on 9/24/15.
 */
public class AuthenticatorService extends Service {

    private Authenticator mAuthenticator;

    @Override
    public void onCreate() {
        mAuthenticator = new Authenticator(this);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}
