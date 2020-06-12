/*
 * Copyright 2018 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.ardemo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ArFragment mFragment;
    private GestureDetector mGestureDetector;
    private ImageView postImageView;
    private ImageView monitorImageView;
    private ConstraintLayout editTextConstraintLayout;
    private TextView monitorTag;
    private EditText editTextField;
    private Button saveTextButton;
    private FloatingActionButton fab;
    private int selectedId = -1;
    public  static String commandRes;
    public  static String oldMessage="";
    public static String taskState = "todo";
    private ViewRenderable solarControlsRenderable;
    // True once scene is loaded
    private boolean hasFinishedLoading = false;
    public int counter = 0;
    CompletableFuture<ViewRenderable> solarControlsStage;
    public String TAG="MainActivity";
    private int port = 22;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        fab = findViewById(R.id.fab);
        postImageView = findViewById(R.id.postIcon);
        monitorImageView = findViewById(R.id.monitorIcon);
        editTextConstraintLayout = findViewById(R.id.changePostItTextConstraintLayout);
        saveTextButton = findViewById(R.id.saveTextButton);
        editTextField = findViewById(R.id.editTextField);
        monitorTag = findViewById(R.id.current_state);
        mFragment = (ArFragment)
                getSupportFragmentManager().findFragmentById(R.id.sceneform_fragment);
        // Build a renderable from a 2D View.
        solarControlsStage=
                ViewRenderable.builder().setView(this, R.layout.solar_controls).build();

        //on tapping of the scene, we want to interact with the world
        mFragment.getArSceneView().getScene().setOnTouchListener((hitTestResult, motionEvent) -> mGestureDetector.onTouchEvent(motionEvent));

        mGestureDetector =
                new GestureDetector(
                        this,
                        new GestureDetector.SimpleOnGestureListener() {
                            @Override
                            public boolean onSingleTapUp(MotionEvent e) {
                                tapAddObject(e);
                                return true;
                            }

                            @Override
                            public boolean onDown(MotionEvent e) {
                                return true;
                            }
                        });

        CompletableFuture.allOf(solarControlsStage).handle((notUsed, throwable) -> {

            if (throwable != null) {
                DemoUtils.displayError(this, "Unable to load renderable", throwable);
                return null;
            }
            try {
                solarControlsRenderable = solarControlsStage.get();

                // Everything finished loading successfully.
                hasFinishedLoading = true;

            } catch (InterruptedException | ExecutionException ex) {
                DemoUtils.displayError(this, "Unable to load renderable", ex);
            }
            return null;

                });

        //take a photo on clicking of the fab
        fab.setOnClickListener(view -> PhotoUtils.takePhoto(mFragment));

        //click listener for selecting that you want to post a note.
        postImageView.setOnClickListener(this);
        monitorImageView.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.postIcon:
            {if (selectedId == R.id.postIcon) {
                    //remove selection
                    selectedId = -1;
                    postImageView.setBackground(null);
                } else {
                    //selecting a post it note
                    selectedId = R.id.postIcon;
                    postImageView.setBackground(getDrawable(R.drawable.icon_outline));
                    monitorImageView.setBackground(null);
                }
                break;}

            case R.id.monitorIcon:
            {if (selectedId == R.id.monitorIcon) {
                    //remove selection
                    selectedId = -1;
                    monitorImageView.setBackground(null);
                } else {
                    //selecting a post it note
                    selectedId = R.id.monitorIcon;
                    monitorImageView.setBackground(getDrawable(R.drawable.icon_outline));
                    postImageView.setBackground(null);
                }}
                break;
        }

    }

    /**
     * Method that takes the user's tap event and creates an anchor from it
     * to attach a renderable post it note.
     *
     * @param motionEvent
     */
    private void tapAddObject(MotionEvent motionEvent) {
        Frame frame = mFragment.getArSceneView().getArFrame();

        if (selectedId == -1 || motionEvent == null || frame == null ||
                frame.getCamera().getTrackingState() != TrackingState.TRACKING)
            return;

        // then we need to check the id to judge add which


        for (HitResult hit : frame.hitTest(motionEvent)) {
            Trackable trackable = hit.getTrackable();
            if ((trackable instanceof Plane &&
                    ((Plane) trackable).isPoseInPolygon(hit.getHitPose()))) {
                //set the 3d model to the anchor
                buildRenderable(mFragment, hit.createAnchor(), selectedId);

                //remove selected item after a successful set.
                selectedId = -1;
                postImageView.setBackground(null);
                break;

            }
        }
    }

    /**
     * Method to build the renderable post it note && monitor.
     *
     * @param fragment
     * @param anchor
     */
    private void buildRenderable(ArFragment fragment, Anchor anchor, int selectedId) {

        switch (selectedId){
            case R.id.postIcon: {
                ModelRenderable.builder()
                        .setSource(fragment.getContext(), Uri.parse("post_it.sfb"))
                        .build()
                        .thenAccept(renderable -> addNodeToScene(fragment, anchor, renderable, selectedId))
                        .exceptionally((throwable -> {
                            AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            builder.setMessage(throwable.getMessage())
                                    .setTitle("Codelab error!");
                            AlertDialog dialog = builder.create();
                            dialog.show();
                            return null;
                        }));
                break;
            }

            case R.id.monitorIcon: {
                ModelRenderable.builder()
                        .setSource(fragment.getContext(), Uri.parse("monitor_it.sfb"))
                        .build()
                        .thenAccept(renderable -> addNodeToScene(fragment, anchor, renderable, selectedId))
                        .exceptionally((throwable -> {
                            AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            builder.setMessage(throwable.getMessage())
                                    .setTitle("Codelab error!");
                            AlertDialog dialog = builder.create();
                            dialog.show();
                            return null;
                        }));
                break;
            }

            default:
                break;
        }


    }


    /**
     * Method to take a renderable and attach it to the anchor point the user selected.
     *
     * @param fragment
     * @param anchor
     * @param renderable
     */
    @SuppressLint("DefaultLocale")
    private void addNodeToScene(ArFragment fragment, Anchor anchor, Renderable renderable, int selectedId) {

        AnchorNode anchorNode = new AnchorNode(anchor);

        if (selectedId == R.id.postIcon) {
            TransformableNode postitNode = new TransformableNode(fragment.getTransformationSystem());
            postitNode.setRenderable(renderable);
            postitNode.setParent(anchorNode);

            //rotate the post it to stick to the flat surface.
            //postitNode.setLocalRotation(new Quaternion(.65f, 0f, 0f, -.5f));

            //add text view node
            ViewRenderable.builder().setView(this, R.layout.post_it_text).build()
                    .thenAccept(viewRenderable -> {
                        Node noteText = new Node();
                        noteText.setParent(fragment.getArSceneView().getScene());
                        noteText.setParent(postitNode);
                        viewRenderable.setShadowCaster(false);
                        viewRenderable.setShadowReceiver(false);
                        noteText.setRenderable(viewRenderable);
                        // noteText.setLocalPosition(new Vector3(0.0f, -0.05f, 0f));
                        noteText.setLocalPosition(new Vector3(0f, -0.03f, -0.01f));
                        //noteText.setLocalPosition(new Vector3(0f, -0.09f, -0.01f));
                    });

            //adding a tap listener to change the text of a note
            postitNode.setOnTapListener((hitTestResult, motionEvent) -> {
                //select it on touching so we can rotate it and position it as needed
                postitNode.select();

                //toggle the edit text view.
                if (editTextConstraintLayout.getVisibility() == View.GONE) {
                    editTextConstraintLayout.setVisibility(View.VISIBLE);
                    saveTextButton.setText(getString(R.string.save));


                    //save the text when the user wants to
                    saveTextButton.setOnClickListener(view -> {
                        TextView tv;
                        for (Node nodeInstance : postitNode.getChildren()) {
                            if (nodeInstance.getRenderable() instanceof ViewRenderable) {
                                tv = ((ViewRenderable) nodeInstance.getRenderable()).getView().findViewById(R.id.postItNoteTextView);
                                String preDeal = editTextField.getText().toString();
                                String postDeal="";
                                // change the width
                                for (int i =0; i<preDeal.length()/5;i++){
                                    postDeal += preDeal.substring(i*5,(i+1)*5);
                                    postDeal += "<br></br>";
                                }
                                postDeal += preDeal.substring((int)(preDeal.length()/5)*5, preDeal.length());

                                tv.setText(Html.fromHtml(postDeal));
                                editTextConstraintLayout.setVisibility(View.GONE);
                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                break;
                            }
                        }
                    });
                } else {
                    editTextConstraintLayout.setVisibility(View.GONE);
                }
            });


            fragment.getArSceneView().getScene().addChild(anchorNode);
            postitNode.select();
        }

        else if (selectedId==R.id.monitorIcon){
            // solve the logic in monitor module
            TransformableNode monitoritNode = new TransformableNode(fragment.getTransformationSystem());
            monitoritNode.setRenderable(renderable);
            monitoritNode.setParent(anchorNode);
            monitoritNode.setLocalRotation(new Quaternion(0f, 0.5f, 0f, 0f));
            //rotate the post it to stick to the flat surface.
            //postitNode.setLocalRotation(new Quaternion(.65f, 0f, 0f, -.5f));


            int localCounter = counter;
            //monitorTag.setText("1");
            counter++;
            Node solarControls = new Node();
            solarControls.setParent(monitoritNode);
            solarControls.setRenderable(solarControlsRenderable);
            solarControls.setLocalPosition(new Vector3(-0.5f, 1.25f, 0.0f));
            solarControls.setLocalRotation(new Quaternion(0.0f, -0.65f, 0f, 0.65f));
            //add text view node
            ViewRenderable.builder().setView(this, R.layout.post_it_text).build()
                    .thenAccept(viewRenderable -> {
                        Node noteText = new Node();
                        noteText.setParent(fragment.getArSceneView().getScene());
                        noteText.setParent(monitoritNode);
                        viewRenderable.setShadowCaster(false);
                        viewRenderable.setShadowReceiver(false);
                        noteText.setRenderable(viewRenderable);
                        // noteText.setLocalPosition(new Vector3(0.0f, -0.05f, 0f));
                        noteText.setLocalPosition(new Vector3(0.1f, 0.1f, 0f));
                        noteText.setLocalRotation(new Quaternion(0.0f, 0.65f, 0f, 0.65f));
                    });

            //adding a tap listener to change the text of a note
            monitoritNode.setOnTapListener((hitTestResult, motionEvent) -> {
                //select it on touching so we can rotate it and position it as needed
                monitoritNode.select();

                //toggle the edit text view.
                if (editTextConstraintLayout.getVisibility() == View.GONE) {
                    editTextConstraintLayout.setVisibility(View.VISIBLE);
                    editTextField.setHint("# machine " + localCounter + " Notice:please enter the command");
                    saveTextButton.setText(getString(R.string.execute));

                    //save the text when the user wants to
                    saveTextButton.setOnClickListener(view -> {
                        TextView tv;
                        for (Node nodeInstance : monitoritNode.getChildren()) {
                            if (nodeInstance.getRenderable() instanceof ViewRenderable) {
                                tv = ((ViewRenderable) nodeInstance.getRenderable()).getView().findViewById(R.id.postItNoteTextView);
                                try {
                                    if ((ViewRenderable) nodeInstance.getRenderable() == solarControlsStage.get()) continue;
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                }
                                String command = editTextField.getText().toString();
                                taskState = "todo";
                                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
                                String date = df.format(new Date());

                                try {
                                    new AsyncTask<Integer, Void, Void>(){
                                        @Override
                                        protected Void doInBackground(Integer... params) {
                                    // insert ssh command execute
                                            try{
                                                commandRes = SSHUtils.executeRemoteCommand("root",getResources().getString(R.string.ecs_password), getResources().getString(R.string.ecs_ip), port, command);
                                                Log.d(TAG, "doInBackground: "+commandRes+commandRes.indexOf("/n"));
                                                taskState = "done";

                                                char[] arrayString = new char[commandRes.length()];
                                                arrayString = commandRes.toCharArray();
                                                for (int i = 0; i<commandRes.length();i++){
                                                    Log.d(TAG, "doInBackground: "+ "current char" + i + ":" + (int)(arrayString[i]) + "/t" + "origin char"+ arrayString[i]);
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            return null;
                                        }
                                    }.execute(1).get();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                }

//                                while (taskState.equals("done")==false){
//
//                                }
//                                taskState = "todo";

                                Log.d(TAG, "setText: "+commandRes);
                                tv.setGravity(Gravity.LEFT);

                                String message =  oldMessage + "<br></br>" + "<br></br>" + "<font size='2' color='#E91E63'><big>"+date+"</big></font>" + "<br></br>" +
                                                    "<font size='2' color='#8BC34A'><big>"+LoginActivity.userName+"$ "+command+"</big></font>" + "<br></br>" +
                                                    "<font size='2' color='#58bbed'><big>"+commandRes+"</big></font>";
                                if (command.indexOf("-l")!=-1){
                                    int num = 10;
                                    char space = (char) num;
                                    String spaceStr = String.valueOf(space);
                                    message = message.replace(spaceStr,"<br></br>");
                                }


//                                char[]  arrayString = commandRes.toCharArray();
//                                for (int i = 0; i<commandRes.length();i++){
//                                    Log.d(TAG, "addNodeToScene: "+ "current char" + i + ":" + (int)(arrayString[i]) + "/t" + "origin char"+ arrayString[i]);
//                                }
//                                Log.d(TAG, "addNodeToScene: " + message);
                                oldMessage = message;
                                tv.setText(Html.fromHtml(message));
                                               Toast toast=Toast.makeText(MainActivity.this,"Toast提示消息"+commandRes,Toast.LENGTH_SHORT    );
                                               toast.setGravity(Gravity.CENTER, 0, 0);
                                               toast.show();
                                Log.d(TAG, "addNodeToScene: "+message);
                                editTextConstraintLayout.setVisibility(View.GONE);
                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                break;
                            }
                        }


                    });
                } else {
                    editTextConstraintLayout.setVisibility(View.GONE);
                }
            });


            fragment.getArSceneView().getScene().addChild(anchorNode);
            monitoritNode.select();
        }
        else {

        }
    }

}
