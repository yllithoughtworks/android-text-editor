package com.thoughtworks.text.editor;

import android.app.Activity;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.thoughtworks.text.editor.layout.api.EditorLayout;

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

    private Framework framework;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        launchFramework();

        ServiceTracker<EditorLayout, EditorLayout> tracker = new ServiceTracker<EditorLayout, EditorLayout>(framework.getBundleContext(), EditorLayout.class, new ServiceTrackerCustomizer<EditorLayout, EditorLayout>() {
            @Override
            public EditorLayout addingService(ServiceReference<EditorLayout> reference) {
                Log.d("main-activity", "get editor layout of");
                EditorLayout editorLayout = framework.getBundleContext().getService(reference);
                editorLayout.active(MainActivity.this);
                editorLayout.getWorkspace().getEditor().setText("HELLO");
                ViewGroup main = (ViewGroup) findViewById(R.id.application_container);
                editorLayout.getViewGroup().setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                main.addView(editorLayout.getViewGroup());
                return editorLayout;
            }

            @Override
            public void modifiedService(ServiceReference<EditorLayout> reference, EditorLayout service) {

            }

            @Override
            public void removedService(ServiceReference<EditorLayout> reference, EditorLayout service) {

            }
        });
        tracker.open(true);
        startBundles("bundles");

        Log.d("main-activity", "start all bundles!!!");



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

                if(installedBundle.getSymbolicName()==null||!installedBundle.getSymbolicName().contains("gson")){
                    installedBundle.start();
                }

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

        config.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, "" +
                "com.thoughtworks.text.editor.mode.api;version=\"1.0\"" +
                ",com.thoughtworks.text.editor.layout.api;version=\"1.0\"" +
                ",android.content" +
                ",android.graphics" +
                ",android.view" +
                ",android.widget" +
                ",android.util");


        config.put(Constants.FRAMEWORK_SYSTEMCAPABILITIES,"osgi.ee;osgi.ee=\"JavaSE\";version:List<Version>=\"1.0,1.1,1.2,1.3,1.4,1.5,1.6,1.7\"");


        //
        config.put(Constants.FRAMEWORK_STORAGE_CLEAN, Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT);
        try {
            config.put(Constants.FRAMEWORK_STORAGE, File.createTempFile("osgi", "launcher").getParent());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return config;
    }


}
