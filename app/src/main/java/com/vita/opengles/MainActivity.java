package com.vita.opengles;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.vita.opengles.activity.TriangleActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initClickListener();
    }

    private void initClickListener() {
        findViewById(R.id.btn_triangle).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Class clz = null;
        switch (view.getId()) {
            case R.id.btn_triangle:
                clz = TriangleActivity.class;
                break;
        }
        jumpTo(clz);
    }

    private void jumpTo(Class clz) {
        if (clz == null) return;
        startActivity(new Intent(this, clz));
    }
}
