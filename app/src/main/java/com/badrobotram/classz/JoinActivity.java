package com.badrobotram.classz;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.UUID;

public class JoinActivity extends AppCompatActivity {

    EditText txtGameCode;
    Button btnContinueJoin;
    Button btnCancelJoin;
    private Socket socket;
    private static final String SERVER = "http://192.168.50.126:3000";
    private String gameCode = "";
    private String uniqueID = UUID.randomUUID().toString();
    private boolean gameExists = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        try {
            //point to server
            socket = IO.socket(SERVER);

            //create connection
            socket.connect();

            // emit the event join alongside with a uniqueID
            socket.emit("connect to server", uniqueID);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        txtGameCode = findViewById(R.id.txtEnterCodeJoin);

        //Clicking Continue in Join
        btnContinueJoin = findViewById(R.id.btnContinueJoin);
        btnContinueJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                gameCode = txtGameCode.getText().toString();

                if(!gameCode.isEmpty()){
                    doesGameExist(gameCode);
                }else{
                    Toast.makeText(JoinActivity.this, "Please enter a game code", Toast.LENGTH_SHORT).show();
                }

            }
        });

        //Clicking Cancel in Join
        btnCancelJoin = findViewById(R.id.btnCancelJoin);
        btnCancelJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                finish();
            }
        });

        socket.on("game exists", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String data = (String) args[0];
                        gameExists = Boolean.valueOf(data);

                        if(gameExists){
                            Intent intent = new Intent(JoinActivity.this, JoinActivityUsername.class);
                            intent.putExtra("game-code", gameCode);
                            intent.putExtra("uid", uniqueID);
                            startActivity(intent); //switch to game code screen
                            finish();
                        }else{
                            Toast.makeText(JoinActivity.this, "Lobby " + gameCode + " does not exist.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    private void doesGameExist(String gc){
        socket.emit("does game exist", gc);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socket.disconnect();
    }

}
