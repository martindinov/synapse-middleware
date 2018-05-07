package com.mygdx.game;

/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/


import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.math.Vector3;

/**
 * See: http://blog.xoppa.com/creating-a-shader-with-libgdx
 * @author Xoppa
 */
public class MyGdxGame implements ApplicationListener, MqttCallback {
    public PerspectiveCamera cam;
    public CameraInputController camController;
    public Shader shader;
    public RenderContext renderContext;
    public Model model;
    public Renderable renderable;
    public BlendingAttribute blendingAttribute;
    public ModelBuilder modelBuilder;
    public ModelBatch modelBatch;
    public ModelInstance instance;
    public Environment environment;
    public AssetManager manager;
    public Texture texture;
    public MqttClient client;
    public String brokerURL = "tcp://test.mosquitto.org:1883";
    private float rotation = 0.0f;
    private int seperations = 4;
    private float value_rotation = 0.0f, value_shape = 0.0f, value_color = 0.0f;
    private String player_rotation, player_shape, player_color;
    
    @Override
    public void create () {
    	environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
    	modelBatch = new ModelBatch();
    	blendingAttribute = new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        cam.position.set(0f, 1.5f, 2f);
        cam.lookAt(0,0,0);
        cam.near = 1f;
        cam.far = 300f;
        cam.update();
             
        modelBuilder = new ModelBuilder();
        model = modelBuilder.createSphere(2f, 2f, 2f, 2, 2, 
          new Material(ColorAttribute.createDiffuse(Color.OLIVE)),
          Usage.Position | Usage.Normal);

        blendingAttribute.opacity = 1f;
        model.materials.get(0).set(blendingAttribute);
        instance = new ModelInstance(model);
        
        manager = new AssetManager();
        manager.load("earth.jpg", Texture.class);
        manager.finishLoading();
        texture = manager.get("earth.jpg", Texture.class);
        
        try {
			createClient();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
     
    @Override
    public void render () {
    	value_rotation = Math.min(1.0f, value_rotation + Gdx.graphics.getDeltaTime()/20);
    	value_shape = Math.min(1.0f, value_shape + Gdx.graphics.getDeltaTime()/20);
    	value_color = Math.min(1.0f, value_color + Gdx.graphics.getDeltaTime()/20);
    	rotation += Gdx.graphics.getDeltaTime() * (5.0-value_rotation*4) * (5.0-value_rotation*4) * 5;

    	int newSep = 5 + (int)(value_shape * value_shape * 30);
    	if(newSep != seperations){
        model = modelBuilder.createSphere(2f, 2f, 2f, newSep, newSep, 
                new Material(TextureAttribute.createDiffuse(texture)),
                Usage.Position | Usage.Normal | Usage.TextureCoordinates);
    	}
    	seperations = newSep;
    	
    	model.materials.get(0).set(ColorAttribute.createDiffuse(0.2f + value_color * 0.8f, 0.7f + value_color * 0.3f, 0.4f + value_color * 0.6f, 1.0f));
    	
       	instance = new ModelInstance(model);
   		instance.transform.setToRotation(Vector3.Y, rotation);

        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl20.glEnable(GL20.GL_BLEND);
        Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl20.glEnable(GL20.GL_TEXTURE_2D);
        Gdx.gl20.glBlendEquation(GL20.GL_BLEND);
        
        modelBatch.begin(cam);
        texture.bind();
        modelBatch.render(instance, environment);
        modelBatch.end();
        Gdx.gl20.glDisable(GL20.GL_TEXTURE_2D);
    }
     
    @Override
    public void dispose () {
        modelBatch.dispose();
        model.dispose();
    }

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}
	

	private void createClient() throws Exception {
		String clientId = "JavaSample";
		MemoryPersistence persistence = new MemoryPersistence();
		client = new MqttClient(brokerURL, clientId, persistence);
		MqttConnectOptions connOpts = new MqttConnectOptions();
		connOpts.setCleanSession(true);
		client.connect(connOpts);
		client.setCallback(this);
		client.subscribe("appdata");
	}

	@Override
	public void connectionLost(Throwable arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		String payload = message.toString();
		if(player_shape != null && payload.startsWith(player_shape)){
			value_shape = getValueFromMessage(payload);
		}else if(player_color != null && payload.startsWith(player_color)) {
			value_color = getValueFromMessage(payload);			
		}else if(player_rotation != null && payload.startsWith(player_rotation)){
			value_rotation = getValueFromMessage(payload);
		}else if(player_shape == null){
			player_shape = getNameFromMessage(payload);
			value_shape = getValueFromMessage(payload);
		}else if(player_color == null){
			player_color = getNameFromMessage(payload);
			value_color = getValueFromMessage(payload);			
		}else if(player_rotation == null){
			player_rotation = getNameFromMessage(payload);
			value_rotation = getValueFromMessage(payload);
		}
	}
	
	private String getNameFromMessage(String message){
		return message.substring(0, message.indexOf(";"));
	}
	
	private float getValueFromMessage(String message){
		return Float.valueOf(message.substring(message.lastIndexOf(";")));
	}
}
