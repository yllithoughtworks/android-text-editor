package com.thoughtworks.text.editor;

import android.app.Activity;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.thoughtworks.text.editor.mode.api.Mode;
import com.thoughtworks.text.editor.mode.api.Workspace;

import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

public class MainActivity extends Activity {

    Mode currentMode;
    private Framework framework;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        launchFramework();
        startBundles("bundles");

        ServiceTracker<Mode, Mode> tracker = new ServiceTracker<Mode, Mode>(framework.getBundleContext(), Mode.class, new ServiceTrackerCustomizer<Mode, Mode>() {
            @Override
            public Mode addingService(ServiceReference<Mode> reference) {
                final Mode mode = framework.getBundleContext().getService(reference);


                ViewGroup modesGroup = (ViewGroup) findViewById(R.id.modes_group);
                Button chooseModeButton = new Button(MainActivity.this);
                chooseModeButton.setText(mode.getName());


                final ViewGroup viewGroup = (ViewGroup) findViewById(R.id.workspace);
                final Workspace workspace = new Workspace() {
                    @Override
                    public ViewGroup getViewGroup() {
                        return viewGroup;
                    }

                    @Override
                    public TextView getEditor() {
                        return (TextView) viewGroup.findViewById(R.id.text_content);
                    }
                };

                chooseModeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        currentMode.inActive(MainActivity.this, workspace);
                        mode.active(MainActivity.this, workspace);
                        currentMode = mode;
                    }
                });
                modesGroup.addView(chooseModeButton);

                if (currentMode == null) {
                    mode.active(MainActivity.this, workspace);
                    currentMode = mode;
                }

                Log.d("kkkkkk", "installed " + mode.getName());

                return mode;
            }

            @Override
            public void modifiedService(ServiceReference<Mode> reference, Mode service) {

            }

            @Override
            public void removedService(ServiceReference<Mode> reference, Mode service) {

            }
        });
        tracker.open(true);
    }


    private void startBundles(String directory) {
        Resources res = getResources(); //if you are in an activity
        AssetManager am = res.getAssets();

        String jarFiles[] = new String[0];
        try {
            jarFiles = am.list("bundles");
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<org.osgi.framework.Bundle> installedBundles = new ArrayList<org.osgi.framework.Bundle>();

        InputStream stream = null;
        for (String bundleFileName : jarFiles) {
            if (!bundleFileName.endsWith(".jar")) {
                continue;
            }

            try {
                String location = "bundles/" + bundleFileName;
                stream = getAssets().open(location);
                installedBundles.add(framework.getBundleContext().installBundle(location, stream));
            } catch (BundleException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for (org.osgi.framework.Bundle installedBundle : installedBundles) {
            try {
                installedBundle.start();
            } catch (BundleException e) {
                e.printStackTrace();
            }
        }
    }

    private void launchFramework() {
        ServiceLoader<FrameworkFactory> frameworkFactories = ServiceLoader.load(FrameworkFactory.class);
        Iterator<FrameworkFactory> iterator = frameworkFactories.iterator();
        if (iterator.hasNext()) {
            framework = iterator.next().newFramework(getFrameworkConfig());
            try {
                framework.init();
                framework.start();
            } catch (BundleException e) {
                e.printStackTrace();
            }
        }
    }

    private static Map<String, String> getFrameworkConfig() {
        Map<String, String> config = new HashMap<String, String>();
//        config.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, "com.thoughtworks.osgi.workshop.definition");

        config.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, "com.thoughtworks.text.editor.mode.api;version=\"1.0\",android.content,android.graphics,android.view,android.widget");
        config.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
        try {
            config.put(Constants.FRAMEWORK_STORAGE, File.createTempFile("osgi", "launcher").getParent());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return config;
    }


}
