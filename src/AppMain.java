import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.custom.ScrolledComposite;

public class AppMain {

	protected Shell shell;
	protected ScrolledComposite scrolledComposite;
	protected ScrolledComposite compositeMain;
	private int compositeWidth = 256;
	private int compositeGridWidth = compositeWidth / 8;
	private int GRID_COUNT = 8;
	private int CANVAS_WIDTH = 768;
	private int SPRITE_WIDTH = CANVAS_WIDTH / 16;
	private int gridWidth = SPRITE_WIDTH / GRID_COUNT;
	private byte[] fileBytes;
	private int[][][] spriteBytes;
	private Color[][] palettes = {{new Color(84,84,84), new Color(48,0,136), new Color(8,58,0), new Color(0,0,0)},
								  {new Color(84,84,84), new Color(48,0,136), new Color(8,58,0), new Color(0,0,0)},
								  {new Color(84,84,84), new Color(48,0,136), new Color(8,58,0), new Color(0,0,0)},
								  {new Color(84,84,84), new Color(48,0,136), new Color(8,58,0), new Color(0,0,0)},
								  {new Color(84,84,84), new Color(48,0,136), new Color(8,58,0), new Color(0,0,0)},
								  {new Color(84,84,84), new Color(48,0,136), new Color(8,58,0), new Color(0,0,0)},
								  {new Color(84,84,84), new Color(48,0,136), new Color(8,58,0), new Color(0,0,0)},
								  {new Color(84,84,84), new Color(48,0,136), new Color(8,58,0), new Color(0,0,0)}};
	private int paletteX = 900;
	private int paletteY = 20;
	private int paletteWidth = 128;
	private int paletteHeight = 32;
	private int currentPaletteIndex = 0;
	private int currentPalette = 0;
	private int mouseX = 0;
	private int mouseY = 0;
	private int currentXIndex = 0;
	private int currentYIndex = 0;
	private int currentSpriteIndex = 0;
	private final int EDIT_MODE = 0;
	private final int SELECT_MODE = 1;
	private int currentMode = EDIT_MODE;
	private boolean showGrid = false;
	private boolean mouseDown = false;
	
	private String currentCharFileName = "";
	private Label lblTileNumber;
	
	private PaletteDialog paletteDialog;

	public static void main(String[] args) {
		try {
			AppMain window = new AppMain();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void open() {
		fileBytes = new byte[4096];
		spriteBytes = new int[256][8][8];
		Display display = Display.getDefault();
		shell = new Shell();
		shell.setSize(1300, 1000);
		shell.setText("NES Studio - " + currentCharFileName);
		shell.setLayout(null);
		
		paletteDialog = new PaletteDialog(shell);
		Button btnNewButton = new Button(shell, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				convertDisplayFormatToNesFormat(spriteBytes);
			}
		});
		btnNewButton.setBounds(1166, 613, 96, 27);
		btnNewButton.setText("Save CHR");
		
		Button btnLoadChr = new Button(shell, SWT.NONE);
		btnLoadChr.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					loadFile("src/ascii.chr");
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		btnLoadChr.setBounds(1166, 659, 96, 27);
		btnLoadChr.setText("Load CHR");
		
		lblTileNumber = new Label(shell, SWT.NONE);
		lblTileNumber.setBounds(995, 320, 150, 14);
		lblTileNumber.setText("Tile Number: " + currentSpriteIndex);
		
		Button btnEditMode = new Button(shell, SWT.RADIO);
		btnEditMode.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				currentMode = EDIT_MODE;
			}
		});
		btnEditMode.setBounds(995, 285, 89, 15);
		btnEditMode.setText("Edit Mode");
		btnEditMode.setSelection(true);
		
		Button btnSelectmode = new Button(shell, SWT.RADIO);
		btnSelectmode.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				currentMode = SELECT_MODE;
			}
		});
		btnSelectmode.setBounds(1129, 285, 116, 15);
		btnSelectmode.setText("SelectMode");
		
		Composite composite = new Composite(shell, SWT.NONE);
		composite.setBounds(997, 201, 64, 64);
		
		Label lblCurrentColor = new Label(shell, SWT.NONE);
		lblCurrentColor.setBounds(989, 181, 96, 14);
		lblCurrentColor.setText("Current Color");
		
		Button btnShowGrid = new Button(shell, SWT.CHECK);
		btnShowGrid.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				showGrid = !showGrid;
				compositeMain.redraw();
			}
		});
		btnShowGrid.setBounds(1129, 249, 93, 16);
		btnShowGrid.setText("Show Grid");
		btnShowGrid.setSelection(false);
		
		Composite compositePalette1 = new Composite(shell, SWT.NONE);
		compositePalette1.setBounds(989, 23, paletteWidth, paletteHeight);
		compositePalette1.addPaintListener(new PaintListener() {
			
			@Override
			public void paintControl(PaintEvent e) {
				GC gc = e.gc;
				int colorWidth = paletteWidth / 4;
				for (int i=0; i<4; i++) {
					gc.setBackground(palettes[0][i]);
					gc.fillRectangle(i*colorWidth, 0, colorWidth, paletteHeight);
				}
				
			}
		});
		compositePalette1.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseUp(MouseEvent arg0) {
				
			}
			
			@Override
			public void mouseDown(MouseEvent e) {
				currentPaletteIndex = e.x / (paletteWidth / 4);
				currentPalette = 0;
				compositeMain.redraw();
				scrolledComposite.redraw();
				composite.setBackground(palettes[currentPalette][currentPaletteIndex]);
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				currentPaletteIndex = e.x / (paletteWidth / 4);
				currentPalette = 0;
				palettes[currentPalette][currentPaletteIndex] = paletteDialog.open();
				compositeMain.redraw();
				scrolledComposite.redraw();
				composite.setBackground(palettes[currentPalette][currentPaletteIndex]);
			}
		});
		Composite compositePalette2 = new Composite(shell, SWT.NONE);
		compositePalette2.setBounds(989, 63, paletteWidth, paletteHeight);
		compositePalette2.addPaintListener(new PaintListener() {
			
			@Override
			public void paintControl(PaintEvent e) {
				GC gc = e.gc;
				int colorWidth = paletteWidth / 4;
				for (int i=0; i<4; i++) {
					gc.setBackground(palettes[1][i]);
					gc.fillRectangle(i*colorWidth, 0, colorWidth, paletteHeight);
				}
				
			}
		});
		compositePalette2.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseUp(MouseEvent arg0) {
				
			}
			
			@Override
			public void mouseDown(MouseEvent e) {
				currentPaletteIndex = e.x / (paletteWidth / 4);
				currentPalette = 1;
				compositeMain.redraw();
				scrolledComposite.redraw();
				composite.setBackground(palettes[currentPalette][currentPaletteIndex]);
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				currentPaletteIndex = e.x / (paletteWidth / 4);
				currentPalette = 1;
				palettes[currentPalette][currentPaletteIndex] = paletteDialog.open();
				compositeMain.redraw();
				scrolledComposite.redraw();
				composite.setBackground(palettes[currentPalette][currentPaletteIndex]);
			}
		});
		Composite compositePalette3 = new Composite(shell, SWT.NONE);
		compositePalette3.setBounds(989, 103, paletteWidth, paletteHeight);
		compositePalette3.addPaintListener(new PaintListener() {
			
			@Override
			public void paintControl(PaintEvent e) {
				GC gc = e.gc;
				int colorWidth = paletteWidth / 4;
				for (int i=0; i<4; i++) {
					gc.setBackground(palettes[2][i]);
					gc.fillRectangle(i*colorWidth, 0, colorWidth, paletteHeight);
				}
				
			}
		});
		compositePalette3.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseUp(MouseEvent arg0) {
				
			}
			
			@Override
			public void mouseDown(MouseEvent e) {
				currentPaletteIndex = e.x / (paletteWidth / 4);
				currentPalette = 2;
				compositeMain.redraw();
				scrolledComposite.redraw();
				composite.setBackground(palettes[currentPalette][currentPaletteIndex]);
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				currentPaletteIndex = e.x / (paletteWidth / 4);
				currentPalette = 2;
				palettes[currentPalette][currentPaletteIndex] = paletteDialog.open();
				compositeMain.redraw();
				scrolledComposite.redraw();
				composite.setBackground(palettes[currentPalette][currentPaletteIndex]);
			}
		});
		Composite compositePalette4 = new Composite(shell, SWT.NONE);
		compositePalette4.setBounds(989, 143, paletteWidth, paletteHeight);
		compositePalette4.addPaintListener(new PaintListener() {
			
			@Override
			public void paintControl(PaintEvent e) {
				GC gc = e.gc;
				int colorWidth = paletteWidth / 4;
				for (int i=0; i<4; i++) {
					gc.setBackground(palettes[3][i]);
					gc.fillRectangle(i*colorWidth, 0, colorWidth, paletteHeight);
				}
				
			}
		});
		compositePalette4.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseUp(MouseEvent arg0) {
				
			}
			
			@Override
			public void mouseDown(MouseEvent e) {
				currentPaletteIndex = e.x / (paletteWidth / 4);
				currentPalette = 3;
				compositeMain.redraw();
				scrolledComposite.redraw();
				composite.setBackground(palettes[currentPalette][currentPaletteIndex]);
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				currentPaletteIndex = e.x / (paletteWidth / 4);
				currentPalette = 3;
				palettes[currentPalette][currentPaletteIndex] = paletteDialog.open();
				compositeMain.redraw();
				scrolledComposite.redraw();
				composite.setBackground(palettes[currentPalette][currentPaletteIndex]);
			}
		});
		Composite compositePalette5 = new Composite(shell, SWT.NONE);
		compositePalette5.setBounds(1150, 23, paletteWidth, paletteHeight);
		compositePalette5.addPaintListener(new PaintListener() {
			
			@Override
			public void paintControl(PaintEvent e) {
				GC gc = e.gc;
				int colorWidth = paletteWidth / 4;
				for (int i=0; i<4; i++) {
					gc.setBackground(palettes[4][i]);
					gc.fillRectangle(i*colorWidth, 0, colorWidth, paletteHeight);
				}
				
			}
		});
		compositePalette5.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseUp(MouseEvent arg0) {
				
			}
			
			@Override
			public void mouseDown(MouseEvent e) {
				currentPaletteIndex = e.x / (paletteWidth / 4);
				currentPalette = 4;
				compositeMain.redraw();
				scrolledComposite.redraw();
				composite.setBackground(palettes[currentPalette][currentPaletteIndex]);
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				currentPaletteIndex = e.x / (paletteWidth / 4);
				currentPalette = 4;
				palettes[currentPalette][currentPaletteIndex] = paletteDialog.open();
				compositeMain.redraw();
				scrolledComposite.redraw();
				composite.setBackground(palettes[currentPalette][currentPaletteIndex]);
			}
		});
		Composite compositePalette6 = new Composite(shell, SWT.NONE);
		compositePalette6.setBounds(1150, 63, paletteWidth, paletteHeight);
		compositePalette6.addPaintListener(new PaintListener() {
			
			@Override
			public void paintControl(PaintEvent e) {
				GC gc = e.gc;
				int colorWidth = paletteWidth / 4;
				for (int i=0; i<4; i++) {
					gc.setBackground(palettes[5][i]);
					gc.fillRectangle(i*colorWidth, 0, colorWidth, paletteHeight);
				}
				
			}
		});
		compositePalette6.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseUp(MouseEvent arg0) {
				
			}
			
			@Override
			public void mouseDown(MouseEvent e) {
				currentPaletteIndex = e.x / (paletteWidth / 4);
				currentPalette = 5;
				compositeMain.redraw();
				scrolledComposite.redraw();
				composite.setBackground(palettes[currentPalette][currentPaletteIndex]);
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				currentPaletteIndex = e.x / (paletteWidth / 4);
				currentPalette = 5;
				palettes[currentPalette][currentPaletteIndex] = paletteDialog.open();
				compositeMain.redraw();
				scrolledComposite.redraw();
				composite.setBackground(palettes[currentPalette][currentPaletteIndex]);
			}
		});
		Composite compositePalette7 = new Composite(shell, SWT.NONE);
		compositePalette7.setBounds(1150, 103, paletteWidth, paletteHeight);
		compositePalette7.addPaintListener(new PaintListener() {
			
			@Override
			public void paintControl(PaintEvent e) {
				GC gc = e.gc;
				int colorWidth = paletteWidth / 4;
				for (int i=0; i<4; i++) {
					gc.setBackground(palettes[6][i]);
					gc.fillRectangle(i*colorWidth, 0, colorWidth, paletteHeight);
				}
				
			}
		});
		compositePalette7.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseUp(MouseEvent arg0) {
				
			}
			
			@Override
			public void mouseDown(MouseEvent e) {
				currentPaletteIndex = e.x / (paletteWidth / 4);
				currentPalette = 6;
				compositeMain.redraw();
				scrolledComposite.redraw();
				composite.setBackground(palettes[currentPalette][currentPaletteIndex]);
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				currentPaletteIndex = e.x / (paletteWidth / 4);
				currentPalette = 6;
				palettes[currentPalette][currentPaletteIndex] = paletteDialog.open();
				compositeMain.redraw();
				scrolledComposite.redraw();
				composite.setBackground(palettes[currentPalette][currentPaletteIndex]);
			}
		});
		Composite compositePalette8 = new Composite(shell, SWT.NONE);
		compositePalette8.setBounds(1150, 143, paletteWidth, paletteHeight);
		compositePalette8.addPaintListener(new PaintListener() {
			
			@Override
			public void paintControl(PaintEvent e) {
				GC gc = e.gc;
				int colorWidth = paletteWidth / 4;
				for (int i=0; i<4; i++) {
					gc.setBackground(palettes[7][i]);
					gc.fillRectangle(i*colorWidth, 0, colorWidth, paletteHeight);
				}
				
			}
		});
		compositePalette8.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseUp(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseDown(MouseEvent e) {
				currentPaletteIndex = e.x / (paletteWidth / 4);
				currentPalette = 7;
				compositeMain.redraw();
				scrolledComposite.redraw();
				composite.setBackground(palettes[currentPalette][currentPaletteIndex]);
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				currentPaletteIndex = e.x / (paletteWidth / 4);
				currentPalette = 7;
				palettes[currentPalette][currentPaletteIndex] = paletteDialog.open();
				compositeMain.redraw();
				scrolledComposite.redraw();
				composite.setBackground(palettes[currentPalette][currentPaletteIndex]);
			}
		});
		
		
		scrolledComposite = new ScrolledComposite(shell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setBounds(989, 350, 256+5, 256+5);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.addPaintListener(new PaintListener() {

			@Override
			public void paintControl(PaintEvent e) {
				GC gc = e.gc;
				gc.setForeground(display.getSystemColor(SWT.COLOR_GRAY));
				gc.setBackground(display.getSystemColor(SWT.COLOR_RED));
				
	            gc.drawRectangle(0, 0, compositeWidth, compositeWidth);
	            
	            for (int i=0; i<8; i++) {
            		for (int j=0; j<8; j++) {
            			int index = spriteBytes[currentSpriteIndex][i][j];
            			gc.setBackground(palettes[currentPalette][index]);
            			gc.fillRectangle(j*compositeGridWidth, i*compositeGridWidth, compositeGridWidth, compositeGridWidth);
            		}
	            }

	            // draw gridlines
	            for (int i=0; i<GRID_COUNT; i++) {
	            	gc.drawLine(i*compositeGridWidth, 0, i*compositeGridWidth, 256);
	            	if (i % 8 == 0)
	            		gc.drawLine(i*compositeGridWidth+1, 0, i*compositeGridWidth+1, 256);
	            }
	            for (int i=0; i<GRID_COUNT; i++) {
	            	gc.drawLine(0, i*compositeGridWidth, 256, i*compositeGridWidth);
	            	if (i % 8 == 0)
	            		gc.drawLine(0, i*compositeGridWidth+1, 256, i*compositeGridWidth+1);

	            }
			}
			
		});
		
		scrolledComposite.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseUp(MouseEvent e) {
				mouseDown = false;
			}
			
			@Override
			public void mouseDown(MouseEvent e) {
				spriteBytes[currentSpriteIndex][(e.y / compositeGridWidth)%8][(e.x / compositeGridWidth)%8] = currentPaletteIndex;
				compositeMain.redraw();
				scrolledComposite.redraw();
				mouseDown = true;
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent e) {

			}
		});
		scrolledComposite.addMouseMoveListener(new MouseMoveListener() {
			
			@Override
			public void mouseMove(MouseEvent e) {
				if (e.x > 0 && e.x < 256 && e.y > 0 && e.y < 256) {
					if (mouseDown) {
						spriteBytes[currentSpriteIndex][(e.y / compositeGridWidth)%8][(e.x / compositeGridWidth)%8] = currentPaletteIndex;
						compositeMain.redraw();
						scrolledComposite.redraw();
					}
				}
				else {
					mouseDown = false;
				}
			}
		});
		compositeMain = new ScrolledComposite(shell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		
		compositeMain.setBounds(200, 0, CANVAS_WIDTH+5, CANVAS_WIDTH+5);
		compositeMain.setExpandHorizontal(true);
		compositeMain.setExpandVertical(true);
		compositeMain.addPaintListener(new PaintListener() {
			@Override
			public void paintControl(PaintEvent e) {
				GC gc = e.gc;
				
				// Draw Sprite chars
	            for (int n=0; n<256; n++) {
	            	for (int i=0; i<8; i++) {
	            		for (int j=0; j<8; j++) {
	            			int index = spriteBytes[n][i][j];
	            			gc.setBackground(palettes[currentPalette][index]);
	            			gc.fillRectangle(j*gridWidth + (n%16)*8*gridWidth, i*gridWidth+ (n/16)*8*gridWidth, gridWidth, gridWidth);
	            		}
	            	}
	            }
	            
	            // draw outer frame
	            gc.setForeground(new Color(156,156,156));
	            gc.drawRectangle(0, 0, CANVAS_WIDTH, CANVAS_WIDTH);
	            
	            // draw gridlines
	            for (int i=0; i<GRID_COUNT*16; i++) {
	            	if (showGrid)
	            		gc.drawLine(i*gridWidth, 0, i*gridWidth, CANVAS_WIDTH);
	            	if (i % 8 == 0)
		            	gc.drawLine(i*gridWidth+1, 0, i*gridWidth+1, CANVAS_WIDTH);
	            }
	            for (int i=0; i<GRID_COUNT*16; i++) {
	            	if (showGrid)
	            		gc.drawLine(0, i*gridWidth, CANVAS_WIDTH, i*gridWidth);
	            	if (i % 8 == 0)
		            	gc.drawLine(0, i*gridWidth+1, CANVAS_WIDTH, i*gridWidth+1);

	            }
	            
	            // highlight current selected tile
	            int currentSpriteX = (currentSpriteIndex % 16) * gridWidth * 8;
	            int currentSpriteY = (currentSpriteIndex / 16) * gridWidth * 8;
    			gc.setForeground(display.getSystemColor(SWT.COLOR_YELLOW));
    			gc.drawRectangle(currentSpriteX, currentSpriteY, gridWidth*8, gridWidth*8);
	            
			}
		});
		
		compositeMain.addMouseListener(new MouseListener() {

			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}

			@Override
			public void mouseDown(MouseEvent e) {
				if (e.x < CANVAS_WIDTH && e.y < CANVAS_WIDTH) {
					if (currentMode == EDIT_MODE) {
						mouseX = e.x;
						currentXIndex = mouseX / gridWidth;
						mouseY = e.y;
						currentYIndex = mouseY / gridWidth;
						int spriteIndex = currentXIndex / 8 + (currentYIndex / 8)*16;
						spriteBytes[spriteIndex][currentYIndex%8][currentXIndex%8] = currentPaletteIndex;
						compositeMain.redraw();
						scrolledComposite.redraw();
					}
				}
				if (e.x < CANVAS_WIDTH && e.y < CANVAS_WIDTH) {
					if (currentMode == SELECT_MODE) {
						currentSpriteIndex = (e.x / gridWidth) / 8 + ((e.y / gridWidth) / 8)*16;
						compositeMain.redraw();
						scrolledComposite.redraw();
						lblTileNumber.setText("Tile Number: " + currentSpriteIndex);
					}
				}
				mouseDown = true;
			}

			@Override
			public void mouseUp(MouseEvent e) {
				mouseDown = false;
			}
			
		});
		compositeMain.addMouseMoveListener(new MouseMoveListener() {
			
			@Override
			public void mouseMove(MouseEvent e) {
				if (mouseDown) {
					if (e.x < CANVAS_WIDTH && e.y < CANVAS_WIDTH) {
						if (currentMode == EDIT_MODE) {
							mouseX = e.x;
							currentXIndex = mouseX / gridWidth;
							mouseY = e.y;
							currentYIndex = mouseY / gridWidth;
							int spriteIndex = currentXIndex / 8 + (currentYIndex / 8)*16;
							spriteBytes[spriteIndex][currentYIndex%8][currentXIndex%8] = currentPaletteIndex;
							compositeMain.redraw();
							scrolledComposite.redraw();
						}
					}
				}
			}
		});
		
		shell.layout();
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setSize(1300, 1000);
		shell.setText("SWT Application");
		shell.setLayout(null);
	}
	
	private int[][] convertNesFormatToDisplayFormat(byte[] nesFormattedChar, int offset) {
		int[][] displayFormattedChar = new int[8][8];
		for (int i=0; i<8; i++) {
			for (int j=0; j<8; j++) {
				displayFormattedChar[i][7-j] = ((nesFormattedChar[i + offset*16]   & (int)Math.pow(2, j)) >> j) + ((nesFormattedChar[(i+offset*16)+8] & (int)Math.pow(2, j)) >> j)*2;
			}
		}
		return displayFormattedChar;
	}
	
	private void convertDisplayFormatToNesFormat(int[][][] displayFormattedChar) {
		int[][] array1 = new int[8][8];
		int[][] array2 = new int[8][8];
		for (int n=0; n<256; n++) {
			array1 = new int[8][8];
			array2 = new int[8][8];
			for (int i=0; i<8; i++) {
				for (int j=0; j<8; j++) {
					if (displayFormattedChar[n][i][j] == 0) {
						array1[i][j] = 0;
						array2[i][j] = 0;
					}
					else if (displayFormattedChar[n][i][j] == 1) {
						array1[i][j] = 1;
						array2[i][j] = 0;
					}
					else if (displayFormattedChar[n][i][j] == 2) {
						array1[i][j] = 0;
						array2[i][j] = 1;
					}
					else if (displayFormattedChar[n][i][j] == 3) {
						array1[i][j] = 1;
						array2[i][j] = 1;
					}
				}
			}
			byte[] byteArray = intArrayToByteArray(array1, array2);
			for (int i=0; i<16; i++) {
				fileBytes[n*16 + i] = byteArray[i];
			}
		}
		
		printFileBytes();
		try {
			saveFile("src/ascii2.chr");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private byte[] intArrayToByteArray(int[][] array1, int[][] array2) {
		byte[] byteArray = new byte[16];
		for (int i=0; i<8; i++) {
			byte val1 = 0x00;
			val1 |= array1[i][7] * 0b00000001;
			val1 |= array1[i][6] * 0b00000010;
			val1 |= array1[i][5] * 0b00000100;
			val1 |= array1[i][4] * 0b00001000;
			val1 |= array1[i][3] * 0b00010000;
			val1 |= array1[i][2] * 0b00100000;
			val1 |= array1[i][1] * 0b01000000;
			val1 |= array1[i][0] * 0b10000000;
			byteArray[i] = val1;
			byte val2 = 0x00;
			val2 |= array2[i][7] * 0b00000001;
			val2 |= array2[i][6] * 0b00000010;
			val2 |= array2[i][5] * 0b00000100;
			val2 |= array2[i][4] * 0b00001000;
			val2 |= array2[i][3] * 0b00010000;
			val2 |= array2[i][2] * 0b00100000;
			val2 |= array2[i][1] * 0b01000000;
			val2 |= array2[i][0] * 0b10000000;
			byteArray[i+8] = val2;
		}
		return byteArray;
	}
	
	private void loadFile(String fileName) throws IOException {
		FileDialog fileOpenDialog = new FileDialog(shell);
		String fName = fileOpenDialog.open();
		FileInputStream fStream = new FileInputStream(new File(fName));
		currentCharFileName = fileOpenDialog.getFileName();
		shell.setText("NES Studio - " + currentCharFileName);
		// read file 16 bytes at a time into fileBytes array
		int offset = 0;
		int countRead = 1;
		while (countRead > 0 && offset < fileBytes.length) {
			countRead = fStream.read(fileBytes, offset, 16);
			offset += countRead;
		}
		fStream.close();
		
		for (int n=0; n<256; n++) {
			spriteBytes[n] = convertNesFormatToDisplayFormat(fileBytes, n);
		}
		compositeMain.redraw();
		scrolledComposite.redraw();
	}
	
	private void saveFile(String fileName) throws IOException {
		FileDialog outputFileDialog = new FileDialog(shell, SWT.SAVE);
		outputFileDialog.setFileName(currentCharFileName);
		String fName = outputFileDialog.open();
		FileOutputStream fStream = new FileOutputStream(new File(fName));
		fStream.write(fileBytes);
		currentCharFileName = outputFileDialog.getFileName();
		shell.setText("NES Studio - " + currentCharFileName);
	}
	
	private void printFileBytes() {
		// print fileBytes array
				int i=0;
				while (i<fileBytes.length) {
					System.out.printf("%02X", fileBytes[i]);
					i++;
					if (i % 4 == 0 && i != 0)
						System.out.print(" ");
					if (i % 16 == 0 && i != 0)
						System.out.println();
				}
	}
}
