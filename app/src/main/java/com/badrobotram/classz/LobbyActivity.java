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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

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

        try {
            //point to server
            socket = IO.socket(SERVER);

            //create connection
            socket.connect();

            getPlayers(gameCode);
            getName(uid);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        tvPlayers = findViewById(R.id.tvPlayers);
        tvPlayers.setText("Players: ");

        btnStartLobby = findViewById(R.id.btnStartLobby);
        btnStartLobby.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                //Intent intent = new Intent(LobbyActivity.this, Game.class);
                //startActivity(intent); //switch to generate game code page
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
                            System.out.println("****** ERROR ********");
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

        socket.on("rec username", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String data = (String) args[0];
                        username = data;
                        Toast.makeText(LobbyActivity.this, "You are " + username, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void getPlayers(String gameId){

        socket.emit("get players", gameId);
    }

    private void getName(String id){

        socket.emit("get username", id);
    }

}
