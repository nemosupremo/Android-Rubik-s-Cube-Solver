package com.droidtools.rubiksolver;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

/*
 * Ported from:
 * http://software.rubikscube.info/AnimCube/
 * Created by Josef Jelinek 2001-2004
 */
public class CubeSurface extends SurfaceView implements Callback, Runnable {

	SurfaceHolder surfaceHolder;

	Thread animThread;
	Thread drawThread;
	boolean interrupted;
	boolean restarted;
	boolean animating;
	boolean editable;
	private boolean mirrored;
	int moveDir;
	int movePos;
	int speed;
	int doubleSpeed;
	int mCanvasWidth = 1;
	int mCanvasHeight = 1;
	double scale;
	boolean moveOne;
	boolean moveAnimated = true;
	boolean twisting;
	boolean natural = true;
	boolean spinning;
	
	// TODO(bbrown): Probably remove this.
	private static final double ORIGINAL_ANGLE = 0;
	double currentAngle;
	private int twistedLayer;
	private int twistedMode;
	private final int[][] cube = new int[6][9];
	private final int[][] initialCube = new int[6][9];
	private int persp; // perspective deformation
	private int progressHeight = 6;
	boolean hint;
	private int align;
	private double faceShift;
	boolean mRunning = false;
	SparseIntArray colorMap;

	private static final double[] EYE = { 0.0, 0.0, -1.0 };
	private static final double[] EYE_X = { 1.0, 0.0, 0.0 }; // (sideways)
	// TODO(bbrown): Not sure why this isn't 0,1,0 or 0,-1,0
	private static final double[] EYE_Y = new double[3]; // (vertical)
	
	private final double[] initialEye = new double[3];
	private final double[] initialEyeX = new double[3];
	private final double[] initialEyeY = new double[3];
	private final double[] tempEye = new double[3];
	private final double[] tempEyeX = new double[3];
	private final double[] tempEyeY = new double[3];
	// temporary eye vectors for second twisted sub-cube rotation (antislice)
	private final double[] tempEye2 = new double[3];
	private final double[] tempEyeX2 = new double[3];
	private final double[] tempEyeY2 = new double[3];
	// temporary vectors to compute visibility in perspective projection
	private final double[] perspEye = new double[3];
	private final double[] perspEyeI = new double[3];
	private final double[] perspNormal = new double[3];
	// eye arrays to store various eyes for various modes
	private final double[][] eyeArray = new double[3][];
	private final double[][] eyeArrayX = new double[3][];
	private final double[][] eyeArrayY = new double[3][];
	
	private static final int[][] EYE_ORDER = { { 1, 0, 0 }, { 0, 1, 0 }, { 1, 1, 0 },
		{ 1, 1, 1 }, { 1, 0, 1 }, { 1, 0, 2	 } };
	private static final int[][] BLACK_MODE = { { 0, 2, 2 }, { 2, 1, 2 }, { 2, 2, 2 },
		{ 2, 2, 2 }, { 2, 2, 2 }, { 2, 2, 2 } };
    private static final int[][] DRAW_ORDER = { { 0, 1, 2 }, { 2, 1, 0 }, { 0, 2, 1 } };

	private final int[][][][] blockArray = new int[3][][][];
	
	private static final int[][][] ROTATION_COS = {
		{ { 1, 0, 0 }, { 0, 0, 0 }, { 0, 0, 1 } }, // U-D
		{ { 1, 0, 0 }, { 0, 1, 0 }, { 0, 0, 0 } }, // F-B
		{ { 0, 0, 0 }, { 0, 1, 0 }, { 0, 0, 1 } } // L-R
	};
	private static final int[][][] ROTATION_SIN = {
		{ { 0, 0, 1 }, { 0, 0, 0 }, { -1, 0, 0 } }, // U-D
		{ { 0, 1, 0 }, { -1, 0, 0 }, { 0, 0, 0 } }, // F-B
		{ { 0, 0, 0 }, { 0, 0, 1 }, { 0, -1, 0 } } // L-R
	};
	private static final int[][][] ROTATION_VECTOR = {
		{ { 0, 0, 0 }, { 0, 1, 0 }, { 0, 0, 0 } }, // U-D
		{ { 0, 0, 0 }, { 0, 0, 0 }, { 0, 0, 1 } }, // F-B
		{ { 1, 0, 0 }, { 0, 0, 0 }, { 0, 0, 0 } } // L-R
	};
	
	// Sign of the rotation direction per face.
	// Up, Down, Front, Back, Left, Right
	private static final int[] ROTATION_SIGN = { 1, -1, 1, -1, 1, -1 }; 
	// Cube dimensions in number of facelets (mincol, maxcol, minrow, maxrow)
	// for compact cube.
	private static final int[][][] CUBE_BLOCKS = {
		    { { 0, 3 }, { 0, 3 } }, // Up
			{ { 0, 3 }, { 0, 3 } }, // Down
			{ { 0, 3 }, { 0, 3 } }, // Front
			{ { 0, 3 }, { 0, 3 } }, // Back
			{ { 0, 3 }, { 0, 3 } }, // Left
			{ { 0, 3 }, { 0, 3 } } // Right
	};
	// Subcube dimensions
	private final int[][][] topBlocks = new int[6][][];
	private final int[][][] midBlocks = new int[6][][];
	private final int[][][] botBlocks = new int[6][][];
	// All possible subcube dimensions for top and bottom layers.
	private static final int[][][] TOP_BLOCK_TABLE = { 
	    { { 0, 0 }, { 0, 0 } }, { { 0, 3 }, { 0, 3 } }, { { 0, 3 }, { 0, 1 } },
	    { { 0, 1 }, { 0, 3 } }, { { 0, 3 }, { 2, 3 } }, { { 2, 3 }, { 0, 3 } } };
	// Subcube dimensions for middle layers
	private static final int[][][] MID_BLOCK_TABLE = { { { 0, 0 }, { 0, 0 } },
		{ { 0, 3 }, { 1, 2 } }, { { 1, 2 }, { 0, 3 } } };
	// Indices to topBlockTable[] and botBlockTable[] for each twistedLayer
	// value
	private static final int[][] TOP_BLOCK_FACE_DIMENSION = {
		// Up Down Front Back Left Right
		{ 1, 0, 3, 3, 2, 3 }, // Up
		{ 0, 1, 5, 5, 4, 5 }, // Down
		{ 2, 3, 1, 0, 3, 2 }, // Front
		{ 4, 5, 0, 1, 5, 4 }, // Back
		{ 3, 2, 2, 4, 1, 0 }, // Left
		{ 5, 4, 4, 2, 0, 1 } // Right
	};
	private static final int[][] MID_BLOCK_FACE_DIMENSION = {
		// Up Down Front Back Left Right
		{ 0, 0, 2, 2, 1, 2 }, // Up
		{ 0, 0, 2, 2, 1, 2 }, // Down
		{ 1, 2, 0, 0, 2, 1 }, // Front
		{ 1, 2, 0, 0, 2, 1 }, // Back
		{ 2, 1, 1, 1, 0, 0 }, // Left
		{ 2, 1, 1, 1, 0, 0 } // Right
	};
	private static final int[][] BOTTOM_BLOCK_FACE_DIMENSION = {
	    // Up Down Front Back Left Right
		{ 0, 1, 5, 5, 4, 5 }, // Up
		{ 1, 0, 3, 3, 2, 3 }, // Down
		{ 4, 5, 0, 1, 5, 4 }, // Front
		{ 2, 3, 1, 0, 3, 2 }, // Back
		{ 5, 4, 4, 2, 0, 1 }, // Left
		{ 3, 2, 2, 4, 1, 0 } // Right
	};

	private static final double[][] FACE_NORMALS = {
	    { 0.0, -1.0, 0.0 }, // Up
		{ 0.0, 1.0, 0.0 }, // Down
		{ 0.0, 0.0, -1.0 }, // Front
		{ 0.0, 0.0, 1.0 }, // Bottom
		{ -1.0, 0.0, 0.0 }, // Left
		{ 1.0, 0.0, 0.0 } // Right
	};
	// Vertex co-ordinates
	private static final double[][] CORNER_COORDINATES = {
	    { -1.0, -1.0, -1.0 }, // UFL
		{ 1.0, -1.0, -1.0 }, // UFR
		{ 1.0, -1.0, 1.0 }, // UBR
		{ -1.0, -1.0, 1.0 }, // UBL
		{ -1.0, 1.0, -1.0 }, // DFL
		{ 1.0, 1.0, -1.0 }, // DFR
		{ 1.0, 1.0, 1.0 }, // DBR
		{ -1.0, 1.0, 1.0 } // DBL
	};
	// vertices of each face
	private static final int[][] FACE_CORNERS = {
	    { 0, 1, 2, 3 }, // U: UFL UFR UBR UBL
		{ 4, 7, 6, 5 }, // D: DFL DBL DBR DFR
		{ 0, 4, 5, 1 }, // F: UFL DFL DFR UFR
		{ 2, 6, 7, 3 }, // B: UBR DBR DBL UBL
		{ 0, 3, 7, 4 }, // L: UFL UBL DBL DFL
		{ 1, 5, 6, 2 } // R: UFR DFR DBR UBR
	};
	// corresponding corners on the opposite face
	private static final int[][] OPPOSITE_CORNERS = {
	    { 0, 3, 2, 1 }, // U->D
		{ 0, 3, 2, 1 }, // D->U
		{ 3, 2, 1, 0 }, // F->B
		{ 3, 2, 1, 0 }, // B->F
		{ 0, 3, 2, 1 }, // L->R
		{ 0, 3, 2, 1 }, // R->L
	};
	private static final int[][] ADJACENT_FACES = {
	    { 2, 5, 3, 4 }, // U: F R B L
		{ 4, 3, 5, 2 }, // D: L B R F
		{ 4, 1, 5, 0 }, // F: L D R U
		{ 5, 1, 4, 0 }, // B: R D L U
		{ 0, 3, 1, 2 }, // L: U B D F
		{ 2, 1, 3, 0 } // R: F D B U
	};

	private static final int[] FACE_TWIST_DIRECTIONS = { 1, 1, -1, -1, -1, -1 };

	// top facelet cycle
	private static final int[] CYCLE_ORDER = { 0, 1, 2, 5, 8, 7, 6, 3 };
	// side facelet cycle offsets
	private static final int[] CYCLE_FACTORS = { 1, 3, -1, -3, 1, 3, -1, -3 };
	private static final int[] CYCLE_OFFSETS = { 0, 2, 8, 6, 3, 1, 5, 7 };
	// indices for faces of layers
	private static final int[][] CYCLE_LAYER_SIDES = {
	    { 3, 3, 3, 0 }, // U: F=6-3k R=6-3k B=6-3k L=k
		{ 2, 1, 1, 1 }, // D: L=8-k B=2+3k R=2+3k F=2+3k
		{ 3, 3, 0, 0 }, // F: L=6-3k D=6-3k R=k U=k
		{ 2, 1, 1, 2 }, // B: R=8-k D=2+3k L=2+3k U=8-k
		{ 3, 2, 0, 0 }, // L: U=6-3k B=8-k D=k F=k
		{ 2, 2, 0, 1 } // R: F=8-k D=8-k B=k U=2+3k
	};
	// indices for sides of center layers
	private static final int[][] CYCLE_CENTERS = {
	    { 7, 7, 7, 4 }, // E'(U): F=7-3k R=7-3k B=7-3k L=3+k
		{ 6, 5, 5, 5 }, // E (D): L=5-k B=1+3k R=1+3k F=1+3k
		{ 7, 7, 4, 4 }, // S (F): L=7-3k D=7-3k R=3+k U=3+k
		{ 6, 5, 5, 6 }, // S'(B): R=5-k D=1+3k L=1+3k U=5-k
		{ 7, 6, 4, 4 }, // M (L): U=7-3k B=8-k D=3+k F=3+k
		{ 6, 6, 4, 5 } // M'(R): F=5-k D=5-k B=3+k U=1+3k
	};
	
	private final int[] twistBuffer = new int[12];
	// polygon co-ordinates to fill (cube faces or facelets)
	private final int[] fillX = new int[4];
	private final int[] fillY = new int[4];
	// projected vertex co-ordinates (to screen)
	private final double[] coordsX = new double[8];
	private final double[] coordsY = new double[8];
	private final double[][] cooX = new double[6][4];
	private final double[][] cooY = new double[6][4];
	
	private static final double[][] BORDER = { { 0.10, 0.10 }, { 0.90, 0.10 },
			{ 0.90, 0.90 }, { 0.10, 0.90 } };
	private static final int[][] FACTORS = { { 0, 0 }, { 0, 1 }, { 1, 1 },
			{ 1, 0 } };
	private final double[] tempNormal = new double[3];
	/*private final double[] faceShiftX = new double[6];
	private final double[] faceShiftY = new double[6];
	
	private int dragAreas;
	private final int[][] dragCornersX = new int[18][4];
	private final int[][] dragCornersY = new int[18][4];
	private final double[] dragDirsX = new double[18];
	private final double[] dragDirsY = new double[18];
	private static final int[][][] dragBlocks = {
		{ { 0, 0 }, { 3, 0 }, { 3, 1 }, { 0, 1 } },
		{ { 3, 0 }, { 3, 3 }, { 2, 3 }, { 2, 0 } },
		{ { 3, 3 }, { 0, 3 }, { 0, 2 }, { 3, 2 } },
		{ { 0, 3 }, { 0, 0 }, { 1, 0 }, { 1, 3 } },
		// center slices
		{ { 0, 1 }, { 3, 1 }, { 3, 2 }, { 0, 2 } },
		{ { 2, 0 }, { 2, 3 }, { 1, 3 }, { 1, 0 } } };
	private static final int[][] areaDirs = {
	    { 1, 0 }, { 0, 1 }, { -1, 0 },
		{ 0, -1 }, { 1, 0 }, { 0, 1 } };
	private static final int[][] twistDirs = {
	    { 1, 1, 1, 1, 1, -1 }, // U
		{ 1, 1, 1, 1, 1, -1 }, // D
		{ 1, -1, 1, -1, 1, 1 }, // F
		{ 1, -1, 1, -1, -1, 1 }, // B
		{ -1, 1, -1, 1, -1, -1 }, // L
		{ 1, -1, 1, -1, 1, 1 } // R
	};
	
	// which layers belongs to dragCorners
	private int[] dragLayers = new int[18];
	// which layer modes dragCorners
	private int[] dragModes = new int[18]; 
	// current drag directions
	private double dragX;
	private double dragY;
	private int[][] move;
	private int[][] demoMove;*/
	
	private int[] curMove;
	
	private Object drawLock = new Object();

	public CubeSurface(Context context, AttributeSet attrs) {
		super(context, attrs);
		surfaceHolder = getHolder();
		surfaceHolder.addCallback(this);
		setFocusable(true); // make sure we get key events
	}
	
	public void init(RubikCube.CubeState state, SparseIntArray colorMap) {
		init(state.toAnimCubeState(), colorMap);
	}
	
	public byte[] cubeState() {
		byte[] state = new byte[54];
		int k = 0;
		for (int i = 0; i < 6; i++) {
			for (int j = 0; j < 9; j++) {
				state[k] = (byte)cube[i][j];
				k++;
			}
		}
		return state;
	}

	public void init(byte[] state, SparseIntArray colorMap) {
		this.colorMap = colorMap;
		String param;
		animThread = new Thread(this, "Cube Animator");
		drawThread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (mRunning) {
					Canvas c = null;
					try {
						c = surfaceHolder.lockCanvas(null);
						synchronized (surfaceHolder) {
							if (c != null) {
								doDraw(c);
							}
						}
					} finally {
						// do this in a finally so that if an exception is
						// thrown
						// during the above, we don't leave the Surface in an
						// inconsistent state
						if (c != null) {
							surfaceHolder.unlockCanvasAndPost(c);
						}
					}
				}
			}

		}, "Cube Drawer");

		//byte[] state = cubestate.toAnimCubeState();
		int k = 0;
		for (int i = 0; i < 6; i++) {
			for (int j = 0; j < 9; j++) {
				cube[i][j] = state[k];
				k++;
			}
		}

		double pi12 = Math.PI / 12;
		param = "lluu";
		for (int i = 0; i < param.length(); i++) {
			double angle = pi12;
			switch (Character.toLowerCase(param.charAt(i))) {
			case 'd':
				angle = -angle;
			case 'u':
				vRotY(EYE, angle);
				vRotY(EYE_X, angle);
				break;
			case 'f':
				angle = -angle;
			case 'b':
				vRotZ(EYE, angle);
				vRotZ(EYE_X, angle);
				break;
			case 'l':
				angle = -angle;
			case 'r':
				vRotX(EYE, angle);
				vRotX(EYE_X, angle);
				break;
			}
		}
		vNorm(vMul(EYE_Y, EYE, EYE_X)); // fix eyeY
		speed = 40;
		doubleSpeed = speed * 3 / 2;
		persp = 2;
		scale = 1.0;// / (1.0 / 10.0);
		hint = false;
		align = 1;
		for (int i = 0; i < 6; i++)
			for (int j = 0; j < 9; j++)
				initialCube[i][j] = cube[i][j];
		for (int i = 0; i < 3; i++) {
			initialEye[i] = EYE[i];
			initialEyeX[i] = EYE_X[i];
			initialEyeY[i] = EYE_Y[i];
		}
		//move = new int[0][0];
	}
	
	public void move(int[] move, int dir, boolean one, boolean anim) {
		curMove = move;
		moveDir = dir;
		moveOne = one;
		moveAnimated = anim;
		synchronized (animThread) {
			animThread.notify();
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		synchronized (surfaceHolder) {
			Log.d("SURFACE_CHANGE",
					String.format("Width - %d Height - %d", width, height));
			mCanvasWidth = width;
			mCanvasHeight = height;
		}

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (!isInEditMode()) {
			setRunning(true);
			animThread.start();
			drawThread.start();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		boolean retry = true;
		setRunning(false);
		while (retry) {
			try {
				synchronized (animThread) {
					animThread.notify();
				}
				animThread.join();
				retry = false;
			} catch (InterruptedException e) {
			}
		}
		Log.d("SURFACE", "Anim thread dead");
		retry = true;
		while (retry) {
			try {
				drawThread.join();
				retry = false;
			} catch (InterruptedException e) {
			}
		}
		Log.d("SURFACE", "draw thread dead");
	}
	
	private void CopyBlockArray(int blockIndex, int[][][] sourceArray) {
		blockArray[blockIndex] = new int [sourceArray.length][][];
		for (int i = 0; i < sourceArray.length; i++) {
			blockArray[blockIndex][i] = new int [sourceArray[i].length][];
			for (int j = 0; j < sourceArray[i].length; j++) {
				blockArray[blockIndex][i][j] = new int [sourceArray[i][j].length];
				for (int k = 0; k < sourceArray[i][j].length; k++) {
					blockArray[blockIndex][i][j][k] = sourceArray[i][j][k];
				}
			}
		}
	}

	public void doDraw(Canvas c) {
		// Copy the state for drawing.
		int[][] drawCube;
		double currentAngleForDraw;
		int twistedLayerForDraw;
		boolean naturalForDraw;
		int twistedModeForDraw;
		synchronized (drawLock) {
			// Copy the state for drawing.
			drawCube = cube;
			currentAngleForDraw = currentAngle;
			twistedLayerForDraw = twistedLayer;
			naturalForDraw = natural;
			twistedModeForDraw = twistedMode;
			
			if (!naturalForDraw) {
				CopyBlockArray(0, topBlocks);
				CopyBlockArray(1, midBlocks);
				CopyBlockArray(2, botBlocks);
			}
		}
		// Log.d("SURFACE", "drawing");
		// create offscreen buffer for double buffering
		/*
		 * if (image == null || size.width != width || size.height -
		 * buttonHeight != height) { width = size.width; height = size.height;
		 * image = createImage(width, height); graphics = image.getGraphics();
		 * textHeight = graphics.getFontMetrics().getHeight() -
		 * graphics.getFontMetrics().getLeading(); if (buttonBar == 1) height -=
		 * buttonHeight; drawButtons = true; }
		 */
		// graphics.setColor(bgColor);
		// graphics.setClip(0, 0, width, height);
		// graphics.fillRect(0, 0, width, height);
		c.drawColor(0xFF000000);
		// synchronized (animThread) {
		// dragAreas = 0;
		if (naturalForDraw) // compact cube
			fixBlock(c, EYE, EYE_X, EYE_Y, CUBE_BLOCKS, 3, drawCube); // draw cube and fill
															// drag areas
		else { // in twisted state
				// compute top observer
			double cosA = Math.cos(ORIGINAL_ANGLE + currentAngleForDraw);
			double sinA = Math.sin(ORIGINAL_ANGLE + currentAngleForDraw)
					* ROTATION_SIGN[twistedLayerForDraw];
			for (int i = 0; i < 3; i++) {
				tempEye[i] = 0;
				tempEyeX[i] = 0;
				for (int j = 0; j < 3; j++) {
					int axis = twistedLayerForDraw / 2;
					tempEye[i] += EYE[j]
							* (ROTATION_VECTOR[axis][i][j] + ROTATION_COS[axis][i][j] * cosA + ROTATION_SIN[axis][i][j]
									* sinA);
					tempEyeX[i] += EYE_X[j]
							* (ROTATION_VECTOR[axis][i][j] + ROTATION_COS[axis][i][j] * cosA + ROTATION_SIN[axis][i][j]
									* sinA);
				}
			}
			vMul(tempEyeY, tempEye, tempEyeX);
			// compute bottom anti-observer
			double cosB = Math.cos(ORIGINAL_ANGLE - currentAngleForDraw);
			double sinB = Math.sin(ORIGINAL_ANGLE - currentAngleForDraw)
					* ROTATION_SIGN[twistedLayerForDraw];
			for (int i = 0; i < 3; i++) {
				tempEye2[i] = 0;
				tempEyeX2[i] = 0;
				for (int j = 0; j < 3; j++) {
					int axis = twistedLayerForDraw / 2;
					tempEye2[i] += EYE[j]
							* (ROTATION_VECTOR[axis][i][j] + ROTATION_COS[axis][i][j] * cosB + ROTATION_SIN[axis][i][j]
									* sinB);
					tempEyeX2[i] += EYE_X[j]
							* (ROTATION_VECTOR[axis][i][j] + ROTATION_COS[axis][i][j] * cosB + ROTATION_SIN[axis][i][j]
									* sinB);
				}
			}
			vMul(tempEyeY2, tempEye2, tempEyeX2);
			eyeArray[0] = EYE;
			eyeArrayX[0] = EYE_X;
			eyeArrayY[0] = EYE_Y;
			eyeArray[1] = tempEye;
			eyeArrayX[1] = tempEyeX;
			eyeArrayY[1] = tempEyeY;
			eyeArray[2] = tempEye2;
			eyeArrayX[2] = tempEyeX2;
			eyeArrayY[2] = tempEyeY2;
			
			// perspective corrections
			vSub(vScale(vCopy(perspEye, EYE), 5.0 + persp),
					vScale(vCopy(perspNormal, FACE_NORMALS[twistedLayerForDraw]),
							1.0 / 3.0));
			vSub(vScale(vCopy(perspEyeI, EYE), 5.0 + persp),
					vScale(vCopy(perspNormal, FACE_NORMALS[twistedLayerForDraw ^ 1]),
							1.0 / 3.0));
			double topProd = vProd(perspEye, FACE_NORMALS[twistedLayerForDraw]);
			double botProd = vProd(perspEyeI, FACE_NORMALS[twistedLayerForDraw ^ 1]);
			int orderMode;
			if (topProd < 0 && botProd > 0) // top facing away
				orderMode = 0;
			else if (topProd > 0 && botProd < 0) // bottom facing away: draw
													// it first
				orderMode = 1;
			else
				// both top and bottom layer facing away: draw them first
				orderMode = 2;
			fixBlock(c,
					eyeArray[EYE_ORDER[twistedModeForDraw][DRAW_ORDER[orderMode][0]]],
					eyeArrayX[EYE_ORDER[twistedModeForDraw][DRAW_ORDER[orderMode][0]]],
					eyeArrayY[EYE_ORDER[twistedModeForDraw][DRAW_ORDER[orderMode][0]]],
					blockArray[DRAW_ORDER[orderMode][0]],
					BLACK_MODE[twistedModeForDraw][DRAW_ORDER[orderMode][0]],
					drawCube);
			fixBlock(c,
					eyeArray[EYE_ORDER[twistedModeForDraw][DRAW_ORDER[orderMode][1]]],
					eyeArrayX[EYE_ORDER[twistedModeForDraw][DRAW_ORDER[orderMode][1]]],
					eyeArrayY[EYE_ORDER[twistedModeForDraw][DRAW_ORDER[orderMode][1]]],
					blockArray[DRAW_ORDER[orderMode][1]],
					BLACK_MODE[twistedModeForDraw][DRAW_ORDER[orderMode][1]],
					drawCube);
			fixBlock(c,
					eyeArray[EYE_ORDER[twistedModeForDraw][DRAW_ORDER[orderMode][2]]],
					eyeArrayX[EYE_ORDER[twistedModeForDraw][DRAW_ORDER[orderMode][2]]],
					eyeArrayY[EYE_ORDER[twistedModeForDraw][DRAW_ORDER[orderMode][2]]],
					blockArray[DRAW_ORDER[orderMode][2]],
					BLACK_MODE[twistedModeForDraw][DRAW_ORDER[orderMode][2]],
					drawCube);
		}
		// if (!pushed && !animating) // no button should be deceased
		// buttonPressed = -1;
		// if (!demo && move.length > 0) {
		/*
		 * if (move[curMove].length > 0) { // some turns
		 * graphics.setColor(Color.black); graphics.drawRect(0, height -
		 * progressHeight, width - 1, progressHeight - 1);
		 * graphics.setColor(textColor); int progress = (width - 2)
		 * realMovePos(move[curMove], movePos) / realMoveLength(move[curMove]);
		 * graphics.fillRect(1, height - progressHeight + 1, progress,
		 * progressHeight - 2); graphics.setColor(bgColor.darker());
		 * graphics.fillRect(1 + progress, height - progressHeight + 1, width -
		 * 2 - progress, progressHeight - 2); String s = "" +
		 * moveLength(move[curMove], movePos) + "/" + moveLength(move[curMove],
		 * -1) + metricChar[metric]; int w =
		 * graphics.getFontMetrics().stringWidth(s); int x = width - w - 2; int
		 * y = height - progressHeight - 2; // int base = //
		 * graphics.getFontMetrics().getDescent(); //if (moveText > 0 &&
		 * textHeight > 0) { // drawString(graphics, s, x, y - textHeight); //
		 * drawMoveText(graphics, y); //} else // drawString(graphics, s, x, y);
		 * }
		 */
		/*
		 * if (move.length > 1) { // more sequences graphics.setClip(0, 0,
		 * width, height); int b = graphics.getFontMetrics().getDescent(); int y
		 * = textHeight - b; String s = "" + (curMove + 1) + "/" + move.length;
		 * int w = graphics.getFontMetrics().stringWidth(s); int x = width - w -
		 * buttonHeight - 2; drawString(graphics, s, x, y); // draw button
		 * graphics.setColor(buttonBgColor); graphics.fill3DRect(width -
		 * buttonHeight, 0, buttonHeight, buttonHeight, buttonPressed != 7);
		 * drawButton(graphics, 7, width - buttonHeight / 2, buttonHeight / 2);
		 * }
		 */
		// }
		// if (curInfoText >= 0) {
		// graphics.setClip(0, 0, width, height);
		// int b = graphics.getFontMetrics().getDescent();
		// int y = textHeight - b;
		// drawString(graphics, infoText[curInfoText], 0, y);
		// }
		// if (drawButtons && buttonBar != 0) // omit unneccessary redrawing
		// drawButtons(graphics);
		// }
		// g.drawImage(image, 0, 0, this);

	}

	public void setRunning(boolean running) {
		mRunning = running;
	}

	@Override
	public void run() {
		synchronized (animThread) {
			interrupted = false;
			do {
				if (restarted) {
					// System.err.println("run: notify");
					animThread.notify();
				}
				try {
					// System.err.println("run: wait");
					animThread.wait();
					// System.err.println("run: run");
				} catch (InterruptedException e) {
					break;
				}
				if (restarted)
					continue;
				if (!mRunning)
					continue;
				boolean restart = false;
				animating = true;
				// int[] mv = demo ? demoMove[0] : move[curMove];
				int[] mv = curMove;
				// int[] mv = new int[0];

				if (moveDir > 0) {
					if (movePos >= mv.length) {
						movePos = 0;
					}
				} else {
					if (movePos == 0)
						movePos = mv.length;
				}

				while (mRunning) {
					if (moveDir < 0) {
						if (movePos == 0)
							break;
						movePos--;
					}
					if (mv[movePos] == -1) {
						// repaint();
						if (!moveOne)
							sleep(33 * speed);
					} else if (mv[movePos] >= 1000) {
						// curInfoText = moveDir > 0 ? mv[movePos] - 1000 : -1;
					} else {
						int num = mv[movePos] % 4 + 1;
						int mode = mv[movePos] / 4 % 6;
						boolean clockwise = num < 3;
						if (num == 4)
							num = 2;
						if (moveDir < 0) {
							clockwise = !clockwise;
							num = 4 - num;
						}
						spin(mv[movePos] / 24, num, mode, clockwise,
								moveAnimated);
						if (moveOne)
							restart = true;
					}
					if (moveDir > 0) {
						movePos++;
						if (movePos < mv.length && mv[movePos] >= 1000) {
							// curInfoText = mv[movePos] - 1000;
							movePos++;
						}
						if (movePos == mv.length) {
							// if (!demo)
							// break;
							movePos = 0;
							break;
							// initInfoText(mv);
							//for (int i = 0; i < 6; i++)
							//	for (int j = 0; j < 9; j++)
							//		cube[i][j] = initialCube[i][j];
						}
					} // else
						// curInfoText = -1;
					if (interrupted || restarted || restart || !mRunning)
						break;
				}
				animating = false;
				// drawButtons = true;
				// repaint();
				// if (demo) {
				// clear();
				// demo = false;
				// }
			} while (!interrupted && mRunning);
		}
		// System.err.println("Interrupted!");
	} // run()

	private void sleep(int time) {
		synchronized (animThread) {
			try {
				animThread.wait(time);
			} catch (InterruptedException e) {
				interrupted = true;
			}
		}
	}

	private void spin(int layer, int num, int mode, boolean clockwise,
			boolean animated) {

		synchronized (drawLock) {
			twisting = false;
			natural = true;
			spinning = true;
			//ORIGINAL_ANGLE = 0;
		}
		if (FACE_TWIST_DIRECTIONS[layer] > 0)
			clockwise = !clockwise;
		if (animated) {
			double phit = Math.PI / 2; // target for currentAngle (default pi/2)
			double phis = clockwise ? 1.0 : -1.0; // sign
			int turnTime = 67 * speed; // milliseconds to be used for one turn
			if (num == 2) {
				phit = Math.PI;
				turnTime = 67 * doubleSpeed; // double turn is usually faster
												// than two quarter turns
			}
			synchronized (drawLock) {
				twisting = true;
				twistedLayer = layer;
				twistedMode = mode;
				splitCube(layer); // start twisting
			}
			long sTime = System.currentTimeMillis();
			long lTime = sTime;
			double d = phis * phit / turnTime;
			final double epsilon = 0.00001;
			for (currentAngle = 0; currentAngle * phis < (phit - epsilon); ) {
				// repaint();
				sleep(25);
				if (interrupted || restarted)
					break;
				lTime = System.currentTimeMillis();
				
				synchronized (drawLock) {
					currentAngle = d * (lTime - sTime);
				}
			}
		}
	
		synchronized (drawLock) {
			currentAngle = 0;
			twisting = false;
			natural = true;
			twistLayers(cube, layer, num, mode);
			spinning = false;
		}
		
		// if (animated)
		// repaint();
	}

	private void splitCube(int layer) {
		for (int i = 0; i < 6; i++) { // for all faces
			topBlocks[i] = TOP_BLOCK_TABLE[TOP_BLOCK_FACE_DIMENSION[layer][i]];
			botBlocks[i] = TOP_BLOCK_TABLE[BOTTOM_BLOCK_FACE_DIMENSION[layer][i]];
			midBlocks[i] = MID_BLOCK_TABLE[MID_BLOCK_FACE_DIMENSION[layer][i]];
		}
		natural = false;
	}

	private void twistLayers(int[][] cube, int layer, int num, int mode) {
		switch (mode) {
		case 3:
			twistLayer(cube, layer ^ 1, num, false);
		case 2:
			twistLayer(cube, layer, 4 - num, false);
		case 1:
			twistLayer(cube, layer, 4 - num, true);
			break;
		case 5:
			twistLayer(cube, layer ^ 1, 4 - num, false);
			twistLayer(cube, layer, 4 - num, false);
			break;
		case 4:
			twistLayer(cube, layer ^ 1, num, false);
		default:
			twistLayer(cube, layer, 4 - num, false);
		}
	}

	private void drawPolygon(Canvas c, int color, int[] fillX, int[] fillY,
			int nPoints, boolean fill) {
		Paint paint = new Paint();
		if (fill)
			paint.setStyle(Paint.Style.FILL);
		paint.setColor(color | 0xFF << 24);
		Path path = new Path();
		// Log.d("PATH", "---------------------");
		path.moveTo(fillX[0], fillY[0]);
		// Log.d("PATH", String.format("X: %d Y:%d", fillX[0], fillY[0]));
		for (int i = 1; i < nPoints; i++) {
			path.lineTo(fillX[i], fillY[i]);
			// Log.d("PATH", String.format("X: %d Y:%d", fillX[i], fillY[i]));
		}
		// Log.d("PATH", "---------------------");
		c.drawPath(path, paint);
	}

	private static int darker(int color) {
		int r = (int) (((color >> 16) & 0xFF) * .7);
		int g = (int) (((color >> 8) & 0xFF) * .7);
		int b = (int) ((color & 0xFF) * .7);
		return 0xFF << 24 | r << 16 | g << 8 | b;
	}

	private void twistLayer(int[][] cube, int layer, int num, boolean middle) {
		if (!middle) {
			// rotate top facelets
			for (int i = 0; i < 8; i++)
				// to buffer
				twistBuffer[(i + num * 2) % 8] = cube[layer][CYCLE_ORDER[i]];
			for (int i = 0; i < 8; i++)
				// to cube
				cube[layer][CYCLE_ORDER[i]] = twistBuffer[i];
		}
		// rotate side facelets
		int k = num * 3;
		for (int i = 0; i < 4; i++) { // to buffer
			int n = ADJACENT_FACES[layer][i];
			int c = middle ? CYCLE_CENTERS[layer][i] : CYCLE_LAYER_SIDES[layer][i];
			int factor = CYCLE_FACTORS[c];
			int offset = CYCLE_OFFSETS[c];
			for (int j = 0; j < 3; j++) {
				twistBuffer[k % 12] = cube[n][j * factor + offset];
				k++;
			}
		}
		k = 0; // MS VM JIT bug if placed into the loop init
		for (int i = 0; i < 4; i++) { // to cube
			int n = ADJACENT_FACES[layer][i];
			int c = middle ? CYCLE_CENTERS[layer][i] : CYCLE_LAYER_SIDES[layer][i];
			int factor = CYCLE_FACTORS[c];
			int offset = CYCLE_OFFSETS[c];
			int j = 0; // MS VM JIT bug if for is used
			while (j < 3) {
				cube[n][j * factor + offset] = twistBuffer[k];
				j++;
				k++;
			}
		}
	}

	private void fixBlock(Canvas c, double[] eye, double[] eyeX, double[] eyeY,
			int[][][] blocks, int mode, int[][] drawCube) {
		// project 3D co-ordinates into 2D screen ones
		for (int i = 0; i < 8; i++) {
			double min = mCanvasWidth < mCanvasHeight ? mCanvasWidth
					: mCanvasHeight - progressHeight;
			double x = min / 3.7 * vProd(CORNER_COORDINATES[i], eyeX) * scale;
			double y = min / 3.7 * vProd(CORNER_COORDINATES[i], eyeY) * scale;
			double z = min / (5.0 + persp) * vProd(CORNER_COORDINATES[i], eye)
					* scale;
			x = x / (1 - z / min); // perspective transformation
			y = y / (1 - z / min); // perspective transformation
			coordsX[i] = mCanvasWidth / 2.0 + x;
			if (align == 0)
				coordsY[i] = (mCanvasHeight - progressHeight) / 2.0 * scale - y;
			else if (align == 2)
				coordsY[i] = mCanvasHeight - progressHeight
						- (mCanvasHeight - progressHeight) / 2.0 * scale - y;
			else
				coordsY[i] = (mCanvasHeight - progressHeight) / 2.0 - y;
		}
		// setup corner co-ordinates for all faces
		for (int i = 0; i < 6; i++) { // all faces
			for (int j = 0; j < 4; j++) { // all face corners
				cooX[i][j] = coordsX[FACE_CORNERS[i][j]];
				cooY[i][j] = coordsY[FACE_CORNERS[i][j]];
			}
		}
		if (hint) { // draw hint hiden facelets
			for (int i = 0; i < 6; i++) { // all faces
				vSub(vScale(vCopy(perspEye, eye), 5.0 + persp), FACE_NORMALS[i]); // perspective
																					// correction
				if (vProd(perspEye, FACE_NORMALS[i]) < 0) { // draw only hiden
															// faces
					vScale(vCopy(tempNormal, FACE_NORMALS[i]), faceShift);
					double min = mCanvasWidth < mCanvasHeight ? mCanvasWidth
							: mCanvasHeight - progressHeight;
					double x = min / 3.7 * vProd(tempNormal, eyeX);
					double y = min / 3.7 * vProd(tempNormal, eyeY);
					double z = min / (5.0 + persp) * vProd(tempNormal, eye);
					x = x / (1 - z / min); // perspective transformation
					y = y / (1 - z / min); // perspective transformation
					int sideW = blocks[i][0][1] - blocks[i][0][0];
					int sideH = blocks[i][1][1] - blocks[i][1][0];
					if (sideW > 0 && sideH > 0) { // this side is not only black
						// draw colored facelets
						for (int n = 0, p = blocks[i][1][0]; n < sideH; n++, p++) {
							for (int o = 0, q = blocks[i][0][0]; o < sideW; o++, q++) {
								for (int j = 0; j < 4; j++) {
									getCorners(i, j, fillX, fillY, q
											+ BORDER[j][0], p + BORDER[j][1],
											mirrored);
									fillX[j] += mirrored ? -x : x;
									fillY[j] -= y;
								}
								drawPolygon(c,
										colorMap.get(drawCube[i][p * 3 + q]),
										fillX, fillY, 4, true);
								// graphics.setColor(colors[drawCube[i][p * 3 +
								// q]]);
								// graphics.fillPolygon(fillX, fillY, 4);
								drawPolygon(
										c,
										darker(colorMap.get(drawCube[i][p * 3 + q])),
										fillX, fillY, 4, false);
								// graphics.setColor(colors[drawCube[i][p * 3 + q]]
								// .darker());
								// graphics.drawPolygon(fillX, fillY, 4);
							}
						}
					}
				}
			}
		}
		// draw black antialias
		for (int i = 0; i < 6; i++) { // all faces
			int sideW = blocks[i][0][1] - blocks[i][0][0];
			int sideH = blocks[i][1][1] - blocks[i][1][0];
			if (sideW > 0 && sideH > 0) {
				for (int j = 0; j < 4; j++)
					// corner co-ordinates
					getCorners(i, j, fillX, fillY, blocks[i][0][FACTORS[j][0]],
							blocks[i][1][FACTORS[j][1]], mirrored);
				// int color;
				// if (sideW == 3 && sideH == 3)
				// graphics.setColor(bgColor2);
				// else
				// graphics.setColor(Color.black);
				// graphics.drawPolygon(fillX, fillY, 4);
				drawPolygon(c, 0, fillX, fillY, 4, false);
			}
		}
		// find and draw black inner faces
		for (int i = 0; i < 6; i++) { // all faces
			int sideW = blocks[i][0][1] - blocks[i][0][0];
			int sideH = blocks[i][1][1] - blocks[i][1][0];
			if (sideW <= 0 || sideH <= 0) { // this face is inner and only black
				for (int j = 0; j < 4; j++) { // for all corners
					int k = OPPOSITE_CORNERS[i][j];
					fillX[j] = (int) (cooX[i][j] + (cooX[i ^ 1][k] - cooX[i][j]) * 2.0 / 3.0);
					fillY[j] = (int) (cooY[i][j] + (cooY[i ^ 1][k] - cooY[i][j]) * 2.0 / 3.0);
					if (mirrored)
						fillX[j] = mCanvasWidth - fillX[j];
				}
				// graphics.setColor(Color.black);
				// graphics.fillPolygon(fillX, fillY, 4);
				drawPolygon(c, 0, fillX, fillY, 4, true);
			} else {
				// draw black face background (do not care about normals and
				// visibility!)
				for (int j = 0; j < 4; j++)
					// corner co-ordinates
					getCorners(i, j, fillX, fillY, blocks[i][0][FACTORS[j][0]],
							blocks[i][1][FACTORS[j][1]], mirrored);
				// graphics.setColor(Color.black);
				// graphics.fillPolygon(fillX, fillY, 4);
				drawPolygon(c, 0, fillX, fillY, 4, true);
			}
		}
		// draw all visible faces and get dragging regions
		for (int i = 0; i < 6; i++) { // all faces
			vSub(vScale(vCopy(perspEye, eye), 5.0 + persp), FACE_NORMALS[i]); // perspective
																				// correction
			if (vProd(perspEye, FACE_NORMALS[i]) > 0) { // draw only faces
														// towards us
				int sideW = blocks[i][0][1] - blocks[i][0][0];
				int sideH = blocks[i][1][1] - blocks[i][1][0];
				if (sideW > 0 && sideH > 0) { // this side is not only black
					// draw colored facelets
					for (int n = 0, p = blocks[i][1][0]; n < sideH; n++, p++) {
						for (int o = 0, q = blocks[i][0][0]; o < sideW; o++, q++) {
							for (int j = 0; j < 4; j++)
								getCorners(i, j, fillX, fillY,
										q + BORDER[j][0], p + BORDER[j][1],
										mirrored);
							drawPolygon(c,
									darker(colorMap.get(drawCube[i][p * 3 + q])),
									fillX, fillY, 4, false);
							// graphics.setColor(colors[drawCube[i][p * 3 + q]]
							// .darker());
							// graphics.drawPolygon(fillX, fillY, 4);
							drawPolygon(c, colorMap.get(drawCube[i][p * 3 + q]),
									fillX, fillY, 4, true);
							// graphics.setColor(colors[drawCube[i][p * 3 + q]]);
							// graphics.fillPolygon(fillX, fillY, 4);
						}
					}
				}
				if (!editable || animating) // no need of twisting while
											// animating
					continue;
				// horizontal and vertical directions of face - interpolated
				/*
				 * double dxh = (cooX[i][1] - cooX[i][0] + cooX[i][2] -
				 * cooX[i][3]) / 6.0; double dyh = (cooX[i][3] - cooX[i][0] +
				 * cooX[i][2] - cooX[i][1]) / 6.0; double dxv = (cooY[i][1] -
				 * cooY[i][0] + cooY[i][2] - cooY[i][3]) / 6.0; double dyv =
				 * (cooY[i][3] - cooY[i][0] + cooY[i][2] - cooY[i][1]) / 6.0; if
				 * (mode == 3) { // just the normal cube for (int j = 0; j < 6;
				 * j++) { // 4 areas 3x1 per face + 2 // center slices for (int
				 * k = 0; k < 4; k++) // 4 points per area getCorners(i, k,
				 * dragCornersX[dragAreas], dragCornersY[dragAreas],
				 * dragBlocks[j][k][0], dragBlocks[j][k][1], false);
				 * dragDirsX[dragAreas] = (dxh * areaDirs[j][0] + dxv
				 * areaDirs[j][1]) twistDirs[i][j]; dragDirsY[dragAreas] = (dyh
				 * * areaDirs[j][0] + dyv areaDirs[j][1]) twistDirs[i][j];
				 * dragLayers[dragAreas] = adjacentFaces[i][j % 4]; if (j >= 4)
				 * dragLayers[dragAreas] &= ~1; dragModes[dragAreas] = j / 4;
				 * dragAreas++; if (dragAreas == 18) break; } } else if (mode ==
				 * 0) { // twistable top layer if (i != twistedLayer && sideW >
				 * 0 && sideH > 0) { // only // 3x1 // faces int j = sideW == 3
				 * ? (blocks[i][1][0] == 0 ? 0 : 2) : (blocks[i][0][0] == 0 ? 3
				 * : 1); for (int k = 0; k < 4; k++) getCorners(i, k,
				 * dragCornersX[dragAreas], dragCornersY[dragAreas],
				 * dragBlocks[j][k][0], dragBlocks[j][k][1], false);
				 * dragDirsX[dragAreas] = (dxh * areaDirs[j][0] + dxv
				 * areaDirs[j][1]) twistDirs[i][j]; dragDirsY[dragAreas] = (dyh
				 * * areaDirs[j][0] + dyv areaDirs[j][1]) twistDirs[i][j];
				 * dragLayers[dragAreas] = twistedLayer; dragModes[dragAreas] =
				 * 0; dragAreas++; } } else if (mode == 1) { // twistable center
				 * layer if (i != twistedLayer && sideW > 0 && sideH > 0) { //
				 * only // 3x1 // faces int j = sideW == 3 ? 4 : 5; for (int k =
				 * 0; k < 4; k++) getCorners(i, k, dragCornersX[dragAreas],
				 * dragCornersY[dragAreas], dragBlocks[j][k][0],
				 * dragBlocks[j][k][1], false); dragDirsX[dragAreas] = (dxh *
				 * areaDirs[j][0] + dxv areaDirs[j][1]) twistDirs[i][j];
				 * dragDirsY[dragAreas] = (dyh * areaDirs[j][0] + dyv
				 * areaDirs[j][1]) twistDirs[i][j]; dragLayers[dragAreas] =
				 * twistedLayer; dragModes[dragAreas] = 1; dragAreas++; } }
				 */
			}
		}
	}

	private void getCorners(int face, int corner, int[] cornersX,
			int[] cornersY, double factor1, double factor2, boolean mirror) {
		factor1 /= 3.0;
		factor2 /= 3.0;
		double x1 = cooX[face][0] + (cooX[face][1] - cooX[face][0]) * factor1;
		double y1 = cooY[face][0] + (cooY[face][1] - cooY[face][0]) * factor1;
		double x2 = cooX[face][3] + (cooX[face][2] - cooX[face][3]) * factor1;
		double y2 = cooY[face][3] + (cooY[face][2] - cooY[face][3]) * factor1;
		cornersX[corner] = (int) (0.5 + x1 + (x2 - x1) * factor2);
		cornersY[corner] = (int) (0.5 + y1 + (y2 - y1) * factor2);
		if (mirror)
			cornersX[corner] = mCanvasWidth - cornersX[corner];
	}

	private static double[] vCopy(double[] vector, double[] srcVec) {
		vector[0] = srcVec[0];
		vector[1] = srcVec[1];
		vector[2] = srcVec[2];
		return vector;
	}

	private static double[] vNorm(double[] vector) {
		double length = Math.sqrt(vProd(vector, vector));
		vector[0] /= length;
		vector[1] /= length;
		vector[2] /= length;
		return vector;
	}

	private static double[] vScale(double[] vector, double value) {
		vector[0] *= value;
		vector[1] *= value;
		vector[2] *= value;
		return vector;
	}

	private static double vProd(double[] vec1, double[] vec2) {
		return vec1[0] * vec2[0] + vec1[1] * vec2[1] + vec1[2] * vec2[2];
	}

	/*private static double[] vAdd(double[] vector, double[] srcVec) {
		vector[0] += srcVec[0];
		vector[1] += srcVec[1];
		vector[2] += srcVec[2];
		return vector;
	}*/

	private static double[] vSub(double[] vector, double[] srcVec) {
		vector[0] -= srcVec[0];
		vector[1] -= srcVec[1];
		vector[2] -= srcVec[2];
		return vector;
	}

	private static double[] vMul(double[] vector, double[] vec1, double[] vec2) {
		vector[0] = vec1[1] * vec2[2] - vec1[2] * vec2[1];
		vector[1] = vec1[2] * vec2[0] - vec1[0] * vec2[2];
		vector[2] = vec1[0] * vec2[1] - vec1[1] * vec2[0];
		return vector;
	}

	private static double[] vRotX(double[] vector, double angle) {
		double sinA = Math.sin(angle);
		double cosA = Math.cos(angle);
		double y = vector[1] * cosA - vector[2] * sinA;
		double z = vector[1] * sinA + vector[2] * cosA;
		vector[1] = y;
		vector[2] = z;
		return vector;
	}

	private static double[] vRotY(double[] vector, double angle) {
		double sinA = Math.sin(angle);
		double cosA = Math.cos(angle);
		double x = vector[0] * cosA - vector[2] * sinA;
		double z = vector[0] * sinA + vector[2] * cosA;
		vector[0] = x;
		vector[2] = z;
		return vector;
	}

	private static double[] vRotZ(double[] vector, double angle) {
		double sinA = Math.sin(angle);
		double cosA = Math.cos(angle);
		double x = vector[0] * cosA - vector[1] * sinA;
		double y = vector[0] * sinA + vector[1] * cosA;
		vector[0] = x;
		vector[1] = y;
		return vector;
	}
}
