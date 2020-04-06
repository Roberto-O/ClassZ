package com.badrobotram.classz;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

public class JoinActivityUsername extends AppCompatActivity {

    EditText txtUsername;
    Button btnContinueUnameJoin, btnCancelUnameJoin;
    private Socket socket;
    private static final String SERVER = "http://192.168.50.126:3000"; //use your own ipv4 local address here since localhost won't work

    String uid = "";
    String gameCode = "";
    String username = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_username);

        gameCode = getIntent().getStringExtra("game-code");
        uid = getIntent().getStringExtra("uid");

        txtUsername = findViewById(R.id.txtUsernameJoin);

        //Continue Button in Username Join
        btnContinueUnameJoin = findViewById(R.id.btnContinueUnameJoin);
        btnContinueUnameJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                username = txtUsername.getText().toString();

                if(!username.isEmpty()){
                    setName(uid, username);
                    joinGame(gameCode, uid);
                }else{
                    Toast.makeText(JoinActivityUsername.this, "Please create a username", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Cancel Button in Username Join
        btnCancelUnameJoin = findViewById(R.id.btnCancelUnameJoin);
        btnCancelUnameJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                finish();
            }
        });

    }// end onCreate() method

    private void setName(String uid, String uname){
        String jsonString = "{userID: '" + uid + "', uname: '" + uname + "'}";

        try {
            //point to server
            socket = IO.socket(SERVER);

            //create connection
            socket.connect();

            //package the data and send it
            JSONObject jsonData = new JSONObject(jsonString);
            socket.emit("set name", jsonData);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }catch (JSONException e) {
            e.printStackTrace();
        }

    }//end join() method

    private void joinGame(String gc, String userID){
        String jsonString = "{gameID: '" + gc + "', userID: '" + userID + "'}";

        try {
            JSONObject jsonData = new JSONObject(jsonString);
            socket.emit("join game", jsonData);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socket.disconnect();
    }
}

