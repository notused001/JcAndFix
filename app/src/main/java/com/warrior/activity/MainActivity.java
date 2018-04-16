package com.warrior.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.warrior.andfix.AndFixPatchManager;
import com.warrior.jcandfix.R;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final String FILE_END = ".apatch";
    private String mPatchDir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPatchDir = getExternalCacheDir().getAbsolutePath()+"/apatch";
        File file = new File(mPatchDir);
        if (file == null||file.exists()){
            file.mkdir();
        }
    }

    public void createBug(View view){
        String error = null;
        Log.e("createbug",error);
    }


    public void fixBug(View view){
        AndFixPatchManager.getInstance().addPatch(getPatchName());
    }

    private String getPatchName() {
        return mPatchDir.concat("fixtestbug").concat(FILE_END);
    }
}
