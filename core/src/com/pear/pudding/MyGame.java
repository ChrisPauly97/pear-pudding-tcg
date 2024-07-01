package com.pear.pudding;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.net.*;
import com.pear.pudding.screen.LoadingScreen;

import java.io.IOException;

public class MyGame extends Game {
    public AssetManager manager = new AssetManager();
    private ServerSocket serverSocket;
    private Socket clientSocket;

    public void startServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started on port " + port);

            // Wait for clients to connect
            while (true) {
                clientSocket = serverSocket.accept();

                // Handle client connection in a separate thread or method
                handleClientConnection(clientSocket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void handleClientConnection(Socket clientSocket) {
        
        // Implement your logic to handle client connections here
    }

    @Override
    public void create()
    {

        HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
        Net.HttpRequest httpRequest = requestBuilder.newRequest().method(Net.HttpMethods.GET).url("http://localhost:8080").build();
        Gdx.net.sendHttpRequest(httpRequest, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                Gdx.app.log("HTTP", httpResponse.getResultAsString());
            }

            @Override
            public void failed(Throwable t) {

            }

            @Override
            public void cancelled() {

            }
        });
        setScreen(new LoadingScreen(this));
    }
}

