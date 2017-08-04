package com.android.miniexplorer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.webkit.WebView;
import com.android.miniexplorer.Utilities;

import static com.android.miniexplorer.Utilities.STREAMING_VR_URL;

public class VRCardBoard extends Activity{
    WebView wb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vrcard_board);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        wb = (WebView) findViewById(R.id.wb);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Utilities.setFullScreen(getWindow());
        wb.loadUrl(STREAMING_VR_URL);
    }

    @Override
    protected void onStop() {
        super.onStop();
        wb.loadUrl("about:blank");
    }

    @Override
    protected void onPause() {
        super.onPause();
        wb.loadUrl("about:blank");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }
}
