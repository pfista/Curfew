package com.curfew;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Rohan on 11/2/13.
 */
public class CurfewService extends Service {
    public IBinder onBind(Intent intent) {
        return null;
    }
}
