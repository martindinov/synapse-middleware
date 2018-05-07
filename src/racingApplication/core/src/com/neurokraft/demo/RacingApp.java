package com.neurokraft.demo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONArray;
import org.json.JSONObject;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.CatmullRomSpline;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class RacingApp extends ApplicationAdapter implements MqttCallback {
	SpriteBatch batch;
	Texture img;
	List<Sprite> sprites = new ArrayList<Sprite>();
	List<Float> positions = new ArrayList<Float>();
	List<Float> speeds = new ArrayList<Float>();
	List<Float> crashTime = new ArrayList<Float>();
	List<Float> recoverTime = new ArrayList<Float>();
	List<Long> roundTime = new ArrayList<Long>();
	HashMap<String, Integer> players = new HashMap<String, Integer>();
	long bestTime = Long.MAX_VALUE;
	Sprite spriteRoad;
	Sprite spriteRoadDark;
	Sprite spriteStart;
	Vector2[] points = new Vector2[1024];
	Vector2[] derivs = new Vector2[1024];
	Vector2[] dataSet = new Vector2[]{
			new Vector2(0.15f,0.75f),
			new Vector2(0.85f,0.75f), 
			new Vector2(0.92f,0.15f),
			new Vector2(0.7f,0.12f),
			new Vector2(0.65f,0.6f),
			new Vector2(0.3f,0.6f),
			new Vector2(0.23f,0.48f),
			new Vector2(0.25f,0.25f),			
			new Vector2(0.5f,0.25f),
//			new Vector2(0.25f,0.25f),
			new Vector2(0.47f,0.12f),
			new Vector2(0.1f,0.15f)};
	ShapeRenderer shaper;
	Camera camera;
	Viewport viewport;
	boolean firstFrame = true;
	float speed = 0.03f;
	float current = 0;
    MqttClient client;
    String brokerURL = "tcp://localhost:12888";
    BitmapFont font;
    
	@Override
	public void create () {
		camera = new PerspectiveCamera();
		viewport = new FitViewport(800,480,camera);
		batch = new SpriteBatch();
		img = new Texture("car.png");

//		sprites.add(new Sprite(img));
//		sprites.get(1).setColor(0.8f, 0.3f, 0.3f, 1.0f);
//		sprites.get(1).setSize(40f, 20f);
//		sprites.get(1).setOrigin(20f, 10f);
//		positions.add(0.5f);
//		speeds.add(0.6f);
		spriteRoad = new Sprite(new Texture("road.png"));
		spriteRoad.setSize(6f, 60f);
		spriteRoad.setOrigin(3f, 30f);
		spriteRoadDark = new Sprite(new Texture("road_dark.png"));
		spriteRoadDark.setSize(6f, 60f);
		spriteRoadDark.setOrigin(3f, 30f);
		spriteStart = new Sprite(new Texture("start.png"));
		spriteStart.setSize(12f, 60f);
		spriteStart.setOrigin(6f, 30f);

		font = new BitmapFont();

		CatmullRomSpline<Vector2> myCatmull = new CatmullRomSpline<Vector2>(dataSet, true);
		for(int i = 0; i < points.length; ++i)
		{
			points[i] = new Vector2();
			derivs[i] = new Vector2();
			myCatmull.valueAt(points[i], ((float)i)/((float)points.length-1));
			myCatmull.derivativeAt(derivs[i], ((float)i)/((float)points.length-1));
		}
		

		shaper = new ShapeRenderer();
		
        try {
			createClient();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0.1f, 0.6f, 0.25f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		for(int i = 0; i < positions.size(); i++){
			int posIndex = (int)(positions.get(i)*1023);
			if(crashTime.get(i) > 0){
				float posX = sprites.get(i).getX() + (speeds.get(i) * (crashTime.get(i))) * derivs[posIndex].x * 3;
				float posY = sprites.get(i).getY() + (speeds.get(i) * (crashTime.get(i))) * derivs[posIndex].y * 3;
				int rotDir = derivs[posIndex].angle() - derivs[posIndex+1].angle() > 0 ? -1 : 1;
				sprites.get(i).rotate(Gdx.graphics.getDeltaTime() * crashTime.get(i) * 180 * rotDir);
				sprites.get(i).setPosition(posX, posY);
				
				crashTime.set(i, crashTime.get(i) - Gdx.graphics.getDeltaTime());
				continue;
			}
			
			recoverTime.set(i, recoverTime.get(i) - Gdx.graphics.getDeltaTime());
			positions.set(i, (Gdx.graphics.getDeltaTime() * speed * speeds.get(i)) / derivs[posIndex].len() + positions.get(i));
			if(positions.get(i) >= 1){
				positions.set(i, 0f);
				long time = System.currentTimeMillis();
				if(time - roundTime.get(i) < bestTime){
					System.err.println("Roundtime: " + (time - roundTime.get(i)) + ". NEW BESTTIME");
					bestTime = time - roundTime.get(i);
				}else{
					System.out.println("Roundtime: " + (time - roundTime.get(i)) + ". That is " + ((time - roundTime.get(i)) - bestTime) + " slower than the best.");
				}
				roundTime.set(i, System.currentTimeMillis());
			}
			
			float angle = derivs[posIndex].angle();
			float angleChange = Math.abs(angle - derivs[posIndex+1].angle());
			if(angleChange > 150) angleChange = 360-angleChange;
			if(1 - speeds.get(i) + .25 < angleChange / 8 && recoverTime.get(i) <= 0){
					crashTime.set(i, 2.0f);
					recoverTime.set(i, 1.0f);
			}
//					System.out.println("Crash: with diff " + ((1 - speeds.get(i) + .25) - (angleChange / 8)));
//			if(derivs[posIndex].angle() - derivs[posIndex + 1].angle() < speeds.get(i)){
//				System.out.println("Crash: " + (derivs[posIndex].angle() - derivs[posIndex+1].angle()) + " vs " + speeds.get(i));
//			}
			sprites.get(i).setRotation(angle);
			sprites.get(i).setPosition(points[posIndex].x * Gdx.graphics.getWidth(), points[posIndex].y * Gdx.graphics.getHeight());		    
		}

		batch.begin();
		for(int i = 0; i < positions.size(); i++)
			font.draw(batch, "Player " + (i+1) + ": " + (Math.round(speeds.get(i)*240*10) / 10) + "km/h", 10, 480 - (20*i));
		for(int i = 0; i < 1024; i++){
			if(i % 2 == 0){
				spriteRoad.setPosition(points[i].x * Gdx.graphics.getWidth(), points[i].y * Gdx.graphics.getHeight());
				spriteRoad.setRotation(derivs[i].angle());
				spriteRoad.draw(batch);
			}else{
				spriteRoadDark.setPosition(points[i].x * Gdx.graphics.getWidth(), points[i].y * Gdx.graphics.getHeight());
				spriteRoadDark.setRotation(derivs[i].angle());
				spriteRoadDark.draw(batch);
			}
		}
		spriteStart.setPosition(points[7].x * Gdx.graphics.getWidth(), points[7].y * Gdx.graphics.getHeight());
		spriteStart.setRotation(derivs[7].angle());
		spriteStart.draw(batch);
		for(int i = 0; i < positions.size(); i++){
			sprites.get(i).draw(batch);
		}
		batch.end();
	}

	public void resize(int width, int height) {
		viewport.update(width, height);
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
		JSONObject o = new JSONObject(message.toString());
		for(String name : o.keySet()){
			try{
				if(!players.containsKey(name)){
					players.put(name, positions.size());
					sprites.add(new Sprite(img));
					int red = (positions.size() % 2);
					int green = (positions.size() % 4) >= 2 ? 1 : 0;
					int blue = (positions.size() % 8) >= 4 ? 1 : 0;
					sprites.get(positions.size()).setColor(0.3f + 0.7f * red, 0.3f + 0.7f * green, 0.3f + 0.7f * blue, 1.0f);
					sprites.get(positions.size()).setSize(40f, 20f);
					sprites.get(positions.size()).setOrigin(20f, 10f);
					positions.add(0f);
					speeds.add(0.0f);
					crashTime.add(0f);
					recoverTime.add(0f);
					roundTime.add(System.currentTimeMillis());
				}else{
					JSONObject p = o.getJSONObject(name);
					JSONArray a = p.getJSONArray("attention");
					speeds.set(players.get(name), ((Double) a.get(a.length()-1)).floatValue());
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
}
