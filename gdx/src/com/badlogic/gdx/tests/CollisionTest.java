package com.badlogic.gdx.tests;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.RenderListener;
import com.badlogic.gdx.backends.desktop.JoglApplication;
import com.badlogic.gdx.graphics.FloatMesh;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.MeshRenderer;
import com.badlogic.gdx.graphics.ModelLoader;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Matrix;
import com.badlogic.gdx.math.Vector;
import com.badlogic.gdx.math.collision.CollisionMesh;
import com.badlogic.gdx.math.collision.EllipsoidCollider;
import com.badlogic.gdx.math.collision.SlideResponse;

public class CollisionTest implements RenderListener
{
	final float VERY_CLOSE_DISTANCE = 0.001f;
	final float SPEED = 1;	
	CollisionMesh cMesh;
	EllipsoidCollider collider;
	MeshRenderer mesh;
	PerspectiveCamera cam;		
	float[] lightColor = { 1, 1, 1, 0 };
	float[] lightPosition = { 2, 5, 10, 0 }; 
	float yAngle = 0;
	Matrix mat = new Matrix();	
	Vector axis = new Vector( 0, 1, 0 );
	Vector velocity = new Vector( 0, 0, 0 );	

	@Override
	public void surfaceCreated(Application app) 
	{	
		try 
		{
			FloatMesh m = (FloatMesh)ModelLoader.loadObj( new FileInputStream( "scene.obj" ), true );
			mesh = new MeshRenderer( app.getGraphics().getGL10(), m, true, true );			
			cam = new PerspectiveCamera();
			cam.setFov( 45 );
			cam.setNear( 0.1f );
			cam.setFar( 1000 );
			
			cMesh = new CollisionMesh( m, false );
			collider = new EllipsoidCollider( 1, 1, 1, new SlideResponse() );			
		} 
		catch (FileNotFoundException e) 
		{		
			e.printStackTrace();
		}
	}
	
	@Override
	public void render(Application app) 
	{
		processInput( app.getInput(), app.getGraphics().getDeltaTime() );
		
		GL10 gl = app.getGraphics().getGL10();
		gl.glClearColor( 0, 0, 0, 0 );
		gl.glClear( GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT );
		
		render3D( gl, app.getGraphics() );			
	}	
	
	private void render3D( GL10 gl, Graphics g )
	{
		gl.glEnable( GL10.GL_DEPTH_TEST );
		cam.setMatrices( g );
		setupLights( gl );					
		mesh.render(GL10.GL_TRIANGLES );
	}
	
	private void setupLights( GL10 gl )
	{
		gl.glEnable( GL10.GL_LIGHTING );
		gl.glEnable( GL10.GL_COLOR_MATERIAL );
		gl.glEnable( GL10.GL_LIGHT0 );				
		gl.glLightfv( GL10.GL_LIGHT0, GL10.GL_DIFFUSE, lightColor, 0 );
		gl.glLightfv( GL10.GL_LIGHT0, GL10.GL_POSITION, lightPosition, 0 );
	}
	
	private void processInput( Input input, float deltaTime )
	{
		if( input.isKeyPressed( Input.Keys.KEYCODE_DPAD_LEFT ) )
			yAngle += deltaTime * 90;
		if( input.isKeyPressed( Input.Keys.KEYCODE_DPAD_RIGHT ) )
			yAngle -= deltaTime * 90;
		
		cam.getDirection().set( 0, 0, -1 );
		mat.setToRotation( axis, yAngle );
		cam.getDirection().rot( mat );
				
		if( input.isKeyPressed( Input.Keys.KEYCODE_DPAD_UP ) )		
			velocity.add(cam.getDirection().tmp().mul( SPEED * deltaTime));
		if( input.isKeyPressed( Input.Keys.KEYCODE_DPAD_DOWN ) )
			velocity.add(cam.getDirection().tmp().mul(SPEED * -deltaTime));
		
		if( !collider.collide( cMesh, cam.getPosition(), velocity, 0.0000001f ) )
		{
			velocity.add( 0, -0.5f * deltaTime, 0 ); // gravity when we don't collide
			collider.collide( cMesh, cam.getPosition(), velocity, 0.0000001f );
		}
		
		cam.getPosition().add( velocity );
		velocity.mul( 0.95f ); // decay
		
		
		System.out.println( velocity );
	}
			
	@Override
	public void dispose(Application app) 
	{	
		
	}
	
	@Override
	public void surfaceChanged(Application app, int width, int height) 
	{	
		
	}

	public static void main( String[] argv )
	{
		JoglApplication app = new JoglApplication( "Collision Test", 480, 320, false );
		app.getGraphics().setRenderListener( new CollisionTest() );
	}
}
