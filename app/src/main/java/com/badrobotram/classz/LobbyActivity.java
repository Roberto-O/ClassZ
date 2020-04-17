package com.badrobotram.classz;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
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

    private Button btnCancelLobby, btnStartLobby;
    private TextView tvPlayers, tvCountdown;

    private String gameCode = "";
    private String uid = "";
    private String username = "";
    private boolean amIHost = false;
    private ArrayList<String> players = new ArrayList<>();
    private HashSet<String> h = new HashSet<>();

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
                socket.emit("link", gameCode);
                getName(uid);
                getPlayers(gameCode);
                amIHostCheck(gameCode, uid);
            }
        }, 700);


        tvPlayers = findViewById(R.id.tvPlayers);
        tvPlayers.setText("Players: ");

        tvCountdown = findViewById(R.id.tvStartingSoon);
        tvCountdown.setText("Starting Soon...");

        //Start Button Lobby
        btnStartLobby = findViewById(R.id.btnStartLobby);
        btnStartLobby.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                socket.emit("start countdown", gameCode);
            }
        });

        //Cancel Button Lobby
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
                } catch (InterruptedException e) { }
            }
        };

        t.start();

        socket.on("begin countdown", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new CountDownTimer(11500, 1000) {
                            int i = 10;
                            @Override
                            public void onTick(long millisUntilFinished) {
                                tvCountdown.setText(i + "");
                                i--;
                                if(i < 0){
                                    onFinish();
                                }
                            }

                            @Override
                            public void onFinish() {
                                tvCountdown.setText("Starting Game");
                                //Intent intent = new Intent(LobbyActivity.this, Game.class);
                                //startActivity(intent);
                            }
                        }.start();
                    }
                });
            }
        });
    }//end onCreate()

    private void amIHostCheck(String gc, String userid){
        String jsonString = "{gameID: '" + gc + "', userID: '" + userid + "'}";

        try {
            JSONObject jsonData = new JSONObject(jsonString);
            socket.emit("get host", jsonData);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        socket.on("am host", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String data = (String) args[0];
                        amIHost = Boolean.valueOf(data);

                        if(!amIHost){
                            btnStartLobby.setAlpha(.5f);
                            btnStartLobby.setClickable(false);
                        }else{
                            Toast.makeText(LobbyActivity.this, "You are host!", Toast.LENGTH_SHORT).show();
                            tvPlayers.setText("Host: " + username + "    Players: " + players.size());
                        }
                    }
                });
            }
        });
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
                    }//end run()
                });
            }
        });

        socket.on("host name", new Emitter.Listener() { //listen for 'rec players' emit from server
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String data = (String) args[0];
                        tvPlayers.setText("Host: " + data + "    Players: " + players.size());
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
