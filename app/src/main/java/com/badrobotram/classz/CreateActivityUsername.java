package com.badrobotram.classz;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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

public class CreateActivityUsername extends AppCompatActivity {

    EditText txtUsername;
    Button btnContinueUname, btnCancelUname;
    private Socket socket;
    private static final String SERVER = "http://192.168.1.138:3000";; //use your own ipv4 local address here since localhost won't work

    String uid = "";
    String gameCode = "";
    String username = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_username);

        gameCode = getIntent().getStringExtra("game-code");
        uid = getIntent().getStringExtra("uid");

        txtUsername = findViewById(R.id.txtUsername);

        //Continue Button in Username Create
        btnContinueUname = findViewById(R.id.btnContinueUname);
        btnContinueUname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                username = txtUsername.getText().toString();

                if(!username.isEmpty()){
                    setName(uid, username);
                    createGame(gameCode, uid);

                    Intent intent = new Intent(CreateActivityUsername.this, LobbyActivity.class);
                    intent.putExtra("game-code", gameCode);
                    intent.putExtra("uid", uid);
                    startActivity(intent);
                    finish();
                }else{
                    Toast.makeText(CreateActivityUsername.this, "Please create a username", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Cancel Button in Username Create
        btnCancelUname = findViewById(R.id.btnCancelUname);
        btnCancelUname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                finish();
            }
        });

    }// end onCreate() method

    private void createGame(String gc, String userID){
        String jsonString = "{gameID: '" + gc + "', userID: '" + userID + "'}";

        try {
            JSONObject jsonData = new JSONObject(jsonString);
            socket.emit("create game", jsonData);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socket.disconnect();
    }

}//end class
