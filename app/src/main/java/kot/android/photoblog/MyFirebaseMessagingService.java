package kot.android.photoblog;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

import kot.android.photoblog.fragment.NotificationFragment;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private Map<String, Object> data;
    private FirebaseFirestore firebaseFirestore;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.i(getString(R.string.DEBUG_TAG), "Message received" );
        String message = remoteMessage.getNotification().getBody();
        String titledata = remoteMessage.getNotification().getTitle();
        data = new HashMap<>();
        data.put("title",titledata);
        data.put("message",message);
        data.put("date", FieldValue.serverTimestamp());
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("Notifications").document().set(data).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(MyFirebaseMessagingService.this, "Ok", Toast.LENGTH_SHORT).show();
                }
            }
        });
        triggerNotification(remoteMessage);
    }

    private void triggerNotification(RemoteMessage remoteMessage) {
        Intent intent = new Intent(this, MainActivity.class);
        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);
        taskStackBuilder.addNextIntentWithParentStack(intent);
        intent.putExtra("count", remoteMessage.getNotification().getTitle());
        PendingIntent pendingIntent = taskStackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,getString(R.string.CHANNEL_ID))
                .setSmallIcon(R.drawable.action_notification)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.action_add))
                .setContentTitle(remoteMessage.getNotification().getTitle())
                .setContentText(remoteMessage.getNotification().getBody())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setChannelId(getString(R.string.CHANNEL_ID))
                .setOngoing(true)
                .setAutoCancel(true);
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(getResources().getInteger(R.integer.notificationId),builder.build());
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Log.i(getString(R.string.DEBUG_TAG), "New Token" + s);
    }
}
