package kot.android.photoblog;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();

        FirebaseMessaging.getInstance().setAutoInitEnabled(true);

        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if(!task.isSuccessful()){
                    Log.i(getString(R.string.DEBUG_TAG), "Task Failed");
                    return;
                }
                Log.i(getString(R.string.DEBUG_TAG), "The result :" + task.getResult().getToken());
            }
        });
    }

    private void createNotificationChannel() {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationChannel serviceChannel = new NotificationChannel(getString(R.string.CHANNEL_ID)
                    ,getString(R.string.CHANNEL_NEWS)
                    , NotificationManager.IMPORTANCE_DEFAULT);
            serviceChannel.setDescription(getString(R.string.CHANNEL_DESCRIPTION));
            serviceChannel.setShowBadge(true);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
}
