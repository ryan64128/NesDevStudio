import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.custom.ScrolledComposite;

public class AppMain {

	protected Shell shell;
	protected ScrolledComposite scrolledComposite;
	private int compositeWidth = 256;
	private int compositeGridWidth = compositeWidth / 8;
	private int GRID_COUNT = 8;
	private int CANVAS_WIDTH = 768;
	private int SPRITE_WIDTH = CANVAS_WIDTH / 16;
	private int gridWidth = SPRITE_WIDTH / GRID_COUNT;
	private byte[] fileBytes;
	private int[][][] spriteBytes;
	private int[] palette = {SWT.COLOR_BLACK, SWT.COLOR_BLUE, SWT.COLOR_YELLOW, SWT.COLOR_WHITE};
	private int paletteX = 900;
	private int paletteY = 20;
	private int paletteWidth = 100;
	private int paletteHeight = 25;
	private int currentPaletteIndex = 0;
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

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			AppMain window = new AppMain();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open the window.
	 */
	public void open() {
		fileBytes = new byte[4096];
		spriteBytes = new int[256][8][8];
		Display display = Display.getDefault();
		shell = new Shell();
		shell.setSize(1300, 1000);
		shell.setText("NES Studio - " + currentCharFileName);
		shell.setLayout(null);
		
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
		
		scrolledComposite = new ScrolledComposite(shell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setBounds(989, 350, 256, 256);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.setBackground(display.getSystemColor(palette[2]));
		
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
		composite.setBackground(display.getSystemColor(palette[currentPaletteIndex]));
		
		Label lblCurrentColor = new Label(shell, SWT.NONE);
		lblCurrentColor.setBounds(989, 171, 96, 14);
		lblCurrentColor.setText("Current Color");
		
		Button btnShowGrid = new Button(shell, SWT.CHECK);
		btnShowGrid.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				showGrid = !showGrid;
				shell.redraw();
			}
		});
		btnShowGrid.setBounds(1129, 249, 93, 16);
		btnShowGrid.setText("Show Grid");
		btnShowGrid.setSelection(false);
		scrolledComposite.addPaintListener(new PaintListener() {

			@Override
			public void paintControl(PaintEvent e) {
				GC gc = e.gc;
				gc.setForeground(display.getSystemColor(SWT.COLOR_GRAY));
				gc.setBackground(display.getSystemColor(SWT.COLOR_RED));
				
	            //Rectangle rect = shell.getClientArea();
	            gc.drawRectangle(0, 0, compositeWidth, compositeWidth);
	            
	            for (int i=0; i<8; i++) {
            		for (int j=0; j<8; j++) {
            			int index = spriteBytes[currentSpriteIndex][i][j];
            			gc.setBackground(display.getSystemColor(palette[index]));
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
				// TODO Auto-generated method stub
//				System.out.println("mouseX: " + e.x + " mouseY: " + e.y);
//				System.out.println("x: " + e.x / compositeGridWidth + " y: " + e.y / compositeGridWidth);
				spriteBytes[currentSpriteIndex][(e.y / compositeGridWidth)%8][(e.x / compositeGridWidth)%8] = currentPaletteIndex;
				shell.redraw();
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
						shell.redraw();
						scrolledComposite.redraw();
					}
				}
				else {
					mouseDown = false;
				}
			}
		});
		
		shell.addPaintListener(new PaintListener() {
			
			@Override
			public void paintControl(PaintEvent e) {
				GC gc = e.gc;
				gc.setForeground(display.getSystemColor(SWT.COLOR_GRAY));
				gc.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
				
	            //Rectangle rect = shell.getClientArea();
	            gc.fillRectangle(0, 0, CANVAS_WIDTH, CANVAS_WIDTH);
	            gc.drawRectangle(0, 0, CANVAS_WIDTH, CANVAS_WIDTH);
	            gc.setBackground(display.getSystemColor(palette[0]));
	            gc.fillRectangle(paletteX, paletteY, paletteWidth/4, paletteHeight);
	            gc.setBackground(display.getSystemColor(palette[1]));
	            gc.fillRectangle(paletteX+paletteWidth/4, paletteY, paletteWidth/4, paletteHeight);
	            gc.setBackground(display.getSystemColor(palette[2]));
	            gc.fillRectangle(paletteX+2*paletteWidth/4, paletteY, paletteWidth/4, paletteHeight);
	            gc.setBackground(display.getSystemColor(palette[3]));
	            gc.fillRectangle(paletteX+3*paletteWidth/4, paletteY, paletteWidth/4, paletteHeight);
	            
	            // Draw Sprite chars
	            for (int n=0; n<256; n++) {
	            	for (int i=0; i<8; i++) {
	            		for (int j=0; j<8; j++) {
	            			int index = spriteBytes[n][i][j];
	            			gc.setBackground(display.getSystemColor(palette[index]));
	            			gc.fillRectangle(j*gridWidth + (n%16)*8*gridWidth, i*gridWidth+ (n/16)*8*gridWidth, gridWidth, gridWidth);
	            		}
	            	}
	            }
	            
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
	            
	            // Print MouseX and MouseY
	            gc.setForeground(display.getSystemColor(SWT.COLOR_YELLOW));
	            gc.drawText("x: " + currentXIndex + ", y: " + currentYIndex, 1050, 750);
	            
			}
		});
		
		shell.addMouseListener(new MouseListener() {

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
						shell.redraw();
						scrolledComposite.redraw();
					}
				}
//					else if (currentMode == SELECT_MODE) {
//						currentSpriteIndex = (e.x / gridWidth) / 8 + ((e.y / gridWidth) / 8)*16;
//						shell.redraw();
//						scrolledComposite.redraw();
//						lblTileNumber.setText("Tile Number: " + currentSpriteIndex);
//					}
//				}
				if (e.x < CANVAS_WIDTH && e.y < CANVAS_WIDTH) {
					if (currentMode == SELECT_MODE) {
						currentSpriteIndex = (e.x / gridWidth) / 8 + ((e.y / gridWidth) / 8)*16;
						shell.redraw();
						scrolledComposite.redraw();
						lblTileNumber.setText("Tile Number: " + currentSpriteIndex);
					}
				}
				mouseDown = true;
				
				if (e.x > paletteX && e.x < paletteX+paletteWidth && e.y > paletteY && e.y < paletteY + paletteHeight) {
					currentPaletteIndex = (e.x - paletteX) / (paletteWidth / 4);
					composite.setBackground(display.getSystemColor(palette[currentPaletteIndex]));
				}
			}

			@Override
			public void mouseUp(MouseEvent e) {
				mouseDown = false;
			}
			
		});
		shell.addMouseMoveListener(new MouseMoveListener() {
			
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
							shell.redraw();
							scrolledComposite.redraw();
						}
//						else if (currentMode == SELECT_MODE) {
//							currentSpriteIndex = (e.x / gridWidth) / 8 + ((e.y / gridWidth) / 8)*16;
//							shell.redraw();
//							scrolledComposite.redraw();
//							lblTileNumber.setText("Tile Number: " + currentSpriteIndex);
//						}
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
		shell.redraw();
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
