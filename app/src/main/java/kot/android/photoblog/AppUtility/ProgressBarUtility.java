package kot.android.photoblog.AppUtility;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

public class ProgressBarUtility {
    private  ProgressBar progressBar;

    public ProgressBarUtility(Context context){
        ViewGroup layout = (ViewGroup) ((Activity)context).findViewById(android.R.id.content)
                .getRootView();

        progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleLarge);
        progressBar.setIndeterminate(true);


        RelativeLayout rl = new RelativeLayout(context);
        RelativeLayout.LayoutParams params = new
                RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);

        rl.setGravity(Gravity.CENTER);
        rl.addView(progressBar);

        layout.addView(rl,params);
        hide();
    }

    public void hide() {
        progressBar.setVisibility(View.INVISIBLE);
    }
    public void show(){
        progressBar.setVisibility(View.VISIBLE);
    }

}
