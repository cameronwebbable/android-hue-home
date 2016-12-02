package com.cameronwebb.webbhouse;

import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.cameronwebb.webbhouse.dummy.DummyContent;
import com.philips.lighting.hue.listener.PHLightListener;
import com.philips.lighting.hue.sdk.PHHueSDK;
import com.philips.lighting.model.PHBridge;
import com.philips.lighting.model.PHBridgeResource;
import com.philips.lighting.model.PHHueError;
import com.philips.lighting.model.PHLight;
import com.philips.lighting.model.PHGroup;
import com.philips.lighting.model.PHLightState;

public class MainActivity extends AppCompatActivity implements LightFragment.OnListFragmentInteractionListener {
    private PHHueSDK phHueSDK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        phHueSDK = PHHueSDK.create();

        Button button = (Button) findViewById(R.id.connect_button);
        if (phHueSDK.getSelectedBridge() == null) {
            button.setText(R.string.btn_find_bridge);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    lightHandler();
                }
            });

        }
        else {
            button.setVisibility(View.GONE);
            lightHandler();
        }

    }

    public void lightHandler() {
        PHBridge bridge = phHueSDK.getSelectedBridge();

        if (bridge == null) {
            Intent intent = new Intent(getApplicationContext(), PHHomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
                intent.addFlags(0x8000); // equal to Intent.FLAG_ACTIVITY_CLEAR_TASK which is only available from API level 11
            startActivity(intent);
        }


        else {
            List<PHGroup> groups = bridge.getResourceCache().getAllGroups();


            LightFragment fragment = (LightFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);
            fragment.updateData(groups);

        }

    }

    @Override
    protected void onDestroy() {
        PHBridge bridge = phHueSDK.getSelectedBridge();

        if (bridge != null) {
            phHueSDK.disableHeartbeat(bridge);
        }

        super.onDestroy();
    }

    @Override
    public void onListFragmentInteraction(PHGroup group) {
        PHBridge bridge = phHueSDK.getSelectedBridge();

        List<String> lights = group.getLightIdentifiers();
        Map<String, PHLight> allLights = bridge.getResourceCache().getLights();

        if (lights != null) {
            for (String lightId : lights) {
                PHLight light = allLights.get(lightId);
                if (light != null) {
                    Boolean isOn = light.getLastKnownLightState().isOn();
                    light.getLastKnownLightState().setOn(!isOn);
                    bridge.updateLightState(light, light.getLastKnownLightState());

                }
            }
        }
    }
}
