import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class PaletteDialog extends Dialog{
	
	private int width = 1000;
	private int height = 400;
	private int colorBoxWidth = 896;
	private int colorBoxHeight = 256;
	private int colorGridWidth = colorBoxWidth / 16;
	private int colorGridHeight = colorBoxHeight / 4;
	private Color[] colors = {new Color(84,84,84), 			// 0x00
							  new Color(0,30,116),
							  new Color(8,16,144),
							  new Color(48,0,136),
							  new Color(68,0,100),
							  new Color(92,0,48),
							  new Color(84,4,0),
							  new Color(60,24,0),
							  new Color(32,42,0),
							  new Color(8,58,0),
							  new Color(0,64,0),
							  new Color(0,60,0),
							  new Color(0,50,60),
							  new Color(0,0,0),
							  new Color(0,0,0),
							  new Color(0,0,0),
							  
							  new Color(152,150,152),		// 0x10
							  new Color(8,76,196),
							  new Color(48,50,236),
							  new Color(92,30,228),
							  new Color(136,20,176),
							  new Color(160,20,100),
							  new Color(152,34,32),
							  new Color(120,60,0),
							  new Color(84,90,0),
							  new Color(40,114,0),
							  new Color(8,124,0),
							  new Color(0,118,40),
							  new Color(0,102,120),
							  new Color(0,0,0),
							  new Color(0,0,0),
							  new Color(0,0,0),
							  
							  new Color(236,238,236),		// 0x20
							  new Color(76,154,236),
							  new Color(120,124,236),
							  new Color(176,98,236),
							  new Color(228,84,236),
							  new Color(236,88,180),
							  new Color(236,106,100),
							  new Color(212,136,32),
							  new Color(160,170,0),
							  new Color(116,196,0),
							  new Color(76,208,32),
							  new Color(56,204,108),
							  new Color(56,180,204),
							  new Color(60,60,60),
							  new Color(60,60,60),
							  new Color(60,60,60),
							  
							  new Color(236,238,236),		// 0x30
							  new Color(168,204,236),
							  new Color(188,188,236),
							  new Color(212,178,236),
							  new Color(236,174,236),
							  new Color(236,174,212),
							  new Color(236,180,176),
							  new Color(228,196,144),
							  new Color(204,210,120),
							  new Color(180,222,120),
							  new Color(168,226,144),
							  new Color(152,226,180),
							  new Color(160,214,228),
							  new Color(160,162,160),
							  new Color(160,162,160),
							  new Color(160,162,160)
	};
	private int colorIndex = 0;
	
	public PaletteDialog(Shell parent) {
		super(parent);
		// TODO Auto-generated constructor stub
	}
	
	public Color open()
	{
		Shell parent = getParent(); 
		Shell dialog = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL); 
		dialog.setSize(width,height);
		dialog.setText("Choose Palette Color"); 
		Composite colorComposite = new Composite(dialog, SWT.BORDER);
		colorComposite.setBounds(10, 10, colorBoxWidth, colorBoxHeight);
		colorComposite.addPaintListener(new PaintListener() {
			
			@Override
			public void paintControl(PaintEvent e) {
				GC gc = e.gc;
				gc.setBackground(colors[1]);
				gc.setForeground(colors[32]);
				gc.fillRectangle(0, 0, colorBoxWidth, colorBoxHeight);
				for (int i=0; i<4; i++) {
					for (int j=0; j<16; j++) {
						gc.setBackground(colors[i*16 + j]);
						gc.fillRectangle(colorGridWidth*j, colorGridHeight*i, colorGridWidth, colorGridHeight);
						if (i > 1 && j == 0)
							gc.setForeground(colors[15]);
						else
							gc.setForeground(colors[32]);
						gc.drawText(String.format("0x%02X", i*16+j), colorGridWidth*j + colorGridWidth/2-12, colorGridHeight*i + colorGridHeight/2 - 8);
					}
				}
			}
		});
		colorComposite.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseUp(MouseEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseDown(MouseEvent e) {
				// TODO Auto-generated method stub
				colorIndex = e.x / colorGridWidth + 16 * (e.y / colorGridHeight);
			}
			
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				// TODO Auto-generated method stub
				colorIndex = e.x / colorGridWidth + 16 * (e.y / colorGridHeight);
				dialog.dispose();
			}
		});
		Display display = parent.getDisplay();
		Button btn = new Button(dialog, SWT.NONE);
		dialog.open(); 
		while (!dialog.isDisposed()) 
		{ if (!display.readAndDispatch()) display.sleep(); 
		} 
		return colors[colorIndex];
	}
}
