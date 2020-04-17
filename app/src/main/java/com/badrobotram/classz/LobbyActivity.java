package com.badrobotram.classz;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;


public class LobbyActivity extends AppCompatActivity {

    private MyRecyclerViewAdapter adapter = null;
    private RecyclerView recyclerView;
    private LinearLayoutManager llm;
    private Socket socket;
    private static final String SERVER = "http://192.168.1.138:3000"; //use your own ipv4 local address here since localhost won't work

    Button btnCancelLobby, btnStartLobby;
    TextView tvPlayers;

    String gameCode = "";
    String uid = "";
    String username = "";
    ArrayList<String> players = new ArrayList<>();
    HashSet<String> h = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        gameCode = getIntent().getStringExtra("game-code");
        uid = getIntent().getStringExtra("uid");


        //point to server
        try {
            socket = IO.socket(SERVER);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        //create connection *needs to be delayed by .7 seconds or else glitches and disconnects
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                socket.connect();
                getPlayers(gameCode);
                getName(uid);
                socket.emit("test", "onCreate Lobby");
            }
        }, 700);


        tvPlayers = findViewById(R.id.tvPlayers);
        tvPlayers.setText("Players: ");

        btnStartLobby = findViewById(R.id.btnStartLobby);
        btnStartLobby.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                startCountdown();
                //Intent intent = new Intent(LobbyActivity.this, Game.class);
                //startActivity(intent);
            }
        });

        btnCancelLobby = findViewById(R.id.btnCancelLobby);
        btnCancelLobby.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                finish();
            }
        });

        // set up the RecyclerView
        recyclerView = findViewById(R.id.rvPlayerList);
        llm = new LinearLayoutManager(getApplicationContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);
        adapter = new MyRecyclerViewAdapter(players);
        recyclerView.setAdapter( adapter );

        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(1000); //sleep one second
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                getPlayers(gameCode); //check for new players
                            }
                        });
                    }
                } catch (InterruptedException e) {
                    System.out.println(e);
                }
            }
        };

        t.start();
    }//end onCreate()

    private void startCountdown(){
        //coming soon
    }

    private void getPlayers(String gameId){

        socket.emit("get players", gameId);

        socket.on("rec players", new Emitter.Listener() { //listen for 'rec players' emit from server
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject data = (JSONObject) args[0];
                        JSONArray dataArray = null;

                        try {
                            dataArray = data.getJSONArray("playerArr");
                            for (int i = 0; i < dataArray.length(); i++) {
                                String player = dataArray.getString(i).replaceAll("\"", "").replaceAll("\\[", "").replaceAll("\\]","");

                                if(!h.contains(player)){ //if name isn't duplicate add
                                    h.add(player);
                                    players.add(player);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        for(String p : players){
                            System.out.println(p);
                        }
                        adapter.notifyDataSetChanged(); //update list
                        tvPlayers.setText("Players: " + players.size());
                    }//end run()
                });
            }
        });
    }

    private void getName(String id){

        socket.emit("get username", id);

        socket.on("rec username", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String data = (String) args[0];
                        username = data;
                        Toast.makeText(LobbyActivity.this, username + " has joined", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socket.disconnect();
    }

}
