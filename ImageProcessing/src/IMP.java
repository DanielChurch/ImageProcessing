
/*
 *Hunter Lloyd
 * Copyrite.......I wrote, ask permission if you want to use it outside of class. 
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FileDialog;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

class IMP {
	private JFrame frame;
	private JPanel mp;
	private JButton start;
	private JScrollPane scroll;
	private JMenuItem openItem, exitItem, resetItem;
	private Toolkit toolkit;
	private File pic;
	private ImageIcon img;
	private int colorX, colorY;
	private int[] pixels;
	private int[] results;
	private JLabel label;
	// Instance Fields you will be using below

	// This will be your height and width of your 2d array
	private int height = 0, width = 0;
	private int initWidth = 0, initHeight = 0;

	// your 2D array of pixels
	private int picture[][];
	
	private LinkedList<Image> history;
	private int historySize = 10;
	private int historyIndex = 0;

	private int mouseX, mouseY;

	/*
	 * In the Constructor I set up the GUI, the frame the menus. The open
	 * pulldown menu is how you will open an image to manipulate.
	 */
	public IMP() {
		history = new LinkedList<Image>();
		toolkit = Toolkit.getDefaultToolkit();
		frame = new JFrame("Image Processing Software by Hunter");
		JMenuBar bar = new JMenuBar();
		JMenu file = new JMenu("File");
		JMenu functions = getFunctions();
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent ev) {
				quit();
			}
		});
		
		JMenuItem neww = new JMenuItem("New");//does nothing atm
		file.add(neww);
		
		openItem = new JMenuItem("Open");
		openItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				handleOpen();
			}
		});
		file.add(openItem);
		
		JMenuItem save = new JMenuItem("Save");
		file.add(save);
		save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Image currentImage = toolkit.createImage(new MemoryImageSource(width, height, pixels, 0, width));
				
				if (System.getProperty("os.name").toLowerCase().contains("windows")) {
					FileDialog fd = new FileDialog(frame, "Choose a file", FileDialog.SAVE);
					fd.setDirectory("C:\\Users\\Dan\\Dropbox\\rendered\\");
					fd.setVisible(true);
					String filename = fd.getFile();
					if (filename == null)
						return;
					else {
						BufferedImage bi = null;
						if(fd.getFile().toLowerCase().contains("jpg") || !fd.getFile().contains("."))
							bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
						else //Assume allowance of transparency
							bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
						Graphics2D g = (Graphics2D) bi.getGraphics();
						g.drawImage(currentImage, 0, 0, null);
						g.dispose();
						
						try {
							ImageIO.write(bi, "JPG", new File(fd.getDirectory() + "\\" + fd.getFile() + (!fd.getFile().contains(".") ? ".jpg" : "")));
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				} else {
					JFileChooser chooser = new JFileChooser();
					chooser.setCurrentDirectory(new File("C:\\Users\\Dan\\Dropbox\\"));
					int option = chooser.showOpenDialog(frame);
					if (option == JFileChooser.APPROVE_OPTION) {
						
						BufferedImage bi = null;
						if(chooser.getSelectedFile().getPath().toLowerCase().contains("jpg") || !chooser.getSelectedFile().getPath().contains("."))
							bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
						else //Assume allowance of transparency
							bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
						Graphics2D g = (Graphics2D) bi.getGraphics();
						g.drawImage(currentImage, 0, 0, null);
						g.dispose();
						
						try {
							ImageIO.write(bi, "JPG", chooser.getSelectedFile());
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					} else
						return;
				}
			}
		});
		resetItem = new JMenuItem("Reset");
		resetItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				reset();
			}
		});
		exitItem = new JMenuItem("Exit");
		exitItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				quit();
			}
		});
		
		file.add(resetItem);
		file.add(exitItem);
		bar.add(file);
		
		JMenu edit = new JMenu("Edit");
		bar.add(edit);
		
		edit.add(menuFunction("Undo", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(historyIndex >= historySize)
					return;
				historyIndex++;
				setPicture(history.get(history.size()-historyIndex));
			}
		}));
		
		edit.add(menuFunction("Redo", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(historyIndex <= 0)
					return;
				historyIndex--;
				setPicture(history.get(history.size()-historyIndex));
			}
		}));
		
		
		bar.add(functions);
		frame.setSize(600, 600);
		mp = new JPanel();
		mp.setBackground(new Color(0, 0, 0));
		mp.setLayout(new BorderLayout());
		mp.setAutoscrolls(true);
		scroll = new JScrollPane(mp);
		frame.getContentPane().add(scroll, BorderLayout.CENTER);
		JPanel butPanel = new JPanel();
		butPanel.setBackground(Color.black);
		start = new JButton("start");
		start.setEnabled(false);
		start.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
			}
		});
		butPanel.add(start);
		frame.getContentPane().add(butPanel, BorderLayout.SOUTH);
		frame.setJMenuBar(bar);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

		label = new JLabel("", SwingUtilities.CENTER);
		label.setDropTarget(new DropTarget(frame, new DropTargetListener() {
			
			@Override
			public void dropActionChanged(DropTargetDragEvent arg0) {}
			
			@Override
			public void drop(DropTargetDropEvent arg0) {
				 Transferable transferable = arg0.getTransferable();
	                if (arg0.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
	                    arg0.acceptDrop(arg0.getDropAction());
	                    try {
	                        List transferData = (List) transferable.getTransferData(DataFlavor.javaFileListFlavor);
	                        if (transferData != null && transferData.size() > 0) {
	                            for(Object f : transferData)
	                            	if(f instanceof File){
	                            		loadImage((File)f);
	                            	}
	                            	arg0.dropComplete(true);
	                        }

	                    } catch (Exception ex) {
	                        ex.printStackTrace();
	                    }
	                } else {
	                    arg0.rejectDrop();
	                }
			}
			
			@Override
			public void dragOver(DropTargetDragEvent arg0) {}
			
			@Override
			public void dragExit(DropTargetEvent arg0) {}
			
			@Override
			public void dragEnter(DropTargetDragEvent arg0) {}
		}));
		label.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent m) {
				colorX = m.getX();
				colorY = m.getY();
				System.out.println(colorX + "  " + colorY);
				getValue();
				start.setEnabled(true);
			}

			@Override
			public void mousePressed(MouseEvent me) {
				mouseX = me.getX();
				mouseY = me.getY();
			}
		});
		label.addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseDragged(MouseEvent me) {
				int dx = mouseX - me.getX();
				int dy = mouseY - me.getY();

				JViewport viewPort = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, mp);
				if(viewPort != null){
					Rectangle view = viewPort.getViewRect();
                    view.x += dx;
                    view.y += dy;
                    
                    mp.scrollRectToVisible(view);
				}

				mouseX = me.getX();
				mouseY = me.getY();
			}
		});
		mp.add(label, BorderLayout.CENTER);
	}

	/*
	 * This method creates the pulldown menu and sets up listeners to selection
	 * of the menu choices. If the listeners are activated they call the methods
	 * for handling the choice, fun1, fun2, fun3, fun4, etc. etc.
	 */

	private JMenu getFunctions() {
		JMenu fun = new JMenu("Functions");

		fun.add(menuFunction("MyExample - fun1 method", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				for (int i = 0; i < height; i++)
					for (int j = 0; j < width; j++) {
						int rgbArray[] = new int[4];

						// get three ints for R, G and B
						rgbArray = getPixelArray(picture[i][j]);
						
						rgbArray[1] = 0;
						// take three ints for R, G, B and put them back into a
						// single
						// int
						picture[i][j] = getPixels(rgbArray);
					}
				resetPicture();
			}
		}));
		
		fun.add(menuFunction("Invert", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				for (int i = 0; i < height; i++)
					for (int j = 0; j < width; j++) {
						int rgbArray[] = new int[4];

						// get three ints for R, G and B
						rgbArray = getPixelArray(picture[i][j]);
						
						rgbArray[1] = 255 - rgbArray[1];
						rgbArray[2] = 255 - rgbArray[2];
						rgbArray[3] = 255 - rgbArray[3];
						// take three ints for R, G, B and put them back into a
						// single
						// int
						picture[i][j] = getPixels(rgbArray);
					}
				resetPicture();
			}
		}));

		fun.add(menuFunction("Rotate 90�", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				int[][] temp = picture;
				picture = new int[width][height];
				for (int i = 0; i < height; i++)
					for (int j = 0; j < width; j++)
						picture[j][i] = temp[i][j];
				width = height;
				height = picture.length;
				resetPicture();
			}
		}));

		JMenu grayscale = new JMenu("Grayscale");
		fun.add(grayscale);

		grayscale.add(menuFunction("Average", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				for (int i = 0; i < height; i++)
					for (int j = 0; j < width; j++) {
						int[] tempArray = getPixelArray(picture[i][j]);
						int grayscale = (int) (tempArray[1] * 1f / 3 + tempArray[2] * 1f / 3 + tempArray[3] * 1f / 3);
						for (int k = 1; k < tempArray.length; k++)
							tempArray[k] = grayscale;
						picture[i][j] = getPixels(tempArray);
					}
				resetPicture();
			}
		}));

		grayscale.add(menuFunction("Min & Max", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				for (int i = 0; i < height; i++)
					for (int j = 0; j < width; j++) {
						int[] tempArray = getPixelArray(picture[i][j]);
						int grayscale = (int) (min(tempArray) / 2f + max(tempArray) / 2f);
						for (int k = 1; k < tempArray.length; k++)
							tempArray[k] = grayscale;
						picture[i][j] = getPixels(tempArray);
					}
				resetPicture();
			}
		}));

		grayscale.add(menuFunction("Luminocity", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				luminocity();
			}
		}));

		fun.add(menuFunction("Edge Detection", new ActionListener() {
			private int[][] mask = new int[][] { { -1, -1, -1 }, { -1, 8, -1 }, { -1, -1, -1 } };

			@Override
			public void actionPerformed(ActionEvent evt) {
				luminocity();
				int[][] temp = new int[picture.length][picture[0].length];
				for (int i = 0; i < height; i++)
					for (int j = 0; j < width; j++)
						temp[i][j] = picture[i][j];

				for (int i = 1; i < height - 1; i++)
					for (int j = 1; j < width - 1; j++) {
						int color = temp[i][j] * mask[1][1] + temp[i - 1][j] * mask[1 - 1][1]
								+ temp[i][j - 1] * mask[1][1 - 1] + temp[i - 1][j - 1] * mask[1 - 1][1 - 1]
								+ temp[i + 1][j] * mask[1 + 1][1] + temp[i][j + 1] * mask[1][1 + 1]
								+ temp[i + 1][j + 1] * mask[1 + 1][1 + 1] + temp[i - 1][j + 1] * mask[1 - 1][1 + 1]
								+ temp[i + 1][j - 1] * mask[1 + 1][1 - 1];
						picture[i][j] = color;
					}
				resetPicture();
			}
		}));

		fun.add(menuFunction("Sobel", new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent evt) {
				sobel();
				resetPicture();
			}
		}));
		
		fun.add(menuFunction("Colored Sobel", new ActionListener() {
			private float[][] horizontalMask = new float[][] { { -1, 0, 1 }, { -2, 0, 2 }, { -1, 0, 1 } };
			private float[][] verticalMask = new float[][] { { -1, -2, -1 }, { 0, 0, 0 }, { 1, 2, 1 } };

			@Override
			public void actionPerformed(ActionEvent evt) {
				luminocity();
				int[][] temp = new int[picture.length][picture[0].length];
				for (int i = 0; i < height; i++)
					for (int j = 0; j < width; j++)
						temp[i][j] = getPixelArray(picture[i][j])[1];

				for (int i = 1; i < height - 1; i++)
					for (int j = 1; j < width - 1; j++) {
						int colorX = (int) (temp[i][j] * horizontalMask[1][1]
								+ temp[i - 1][j] * horizontalMask[1 - 1][1] + temp[i][j - 1] * horizontalMask[1][1 - 1]
								+ temp[i - 1][j - 1] * horizontalMask[1 - 1][1 - 1]
								+ temp[i + 1][j] * horizontalMask[1 + 1][1] + temp[i][j + 1] * horizontalMask[1][1 + 1]
								+ temp[i + 1][j + 1] * horizontalMask[1 + 1][1 + 1]
								+ temp[i - 1][j + 1] * horizontalMask[1 - 1][1 + 1]
								+ temp[i + 1][j - 1] * horizontalMask[1 + 1][1 - 1]);

						int colorY = (int) (temp[i][j] * verticalMask[1][1] + temp[i - 1][j] * verticalMask[1 - 1][1]
								+ temp[i][j - 1] * verticalMask[1][1 - 1]
								+ temp[i - 1][j - 1] * verticalMask[1 - 1][1 - 1]
								+ temp[i + 1][j] * verticalMask[1 + 1][1] + temp[i][j + 1] * verticalMask[1][1 + 1]
								+ temp[i + 1][j + 1] * verticalMask[1 + 1][1 + 1]
								+ temp[i - 1][j + 1] * verticalMask[1 - 1][1 + 1]
								+ temp[i + 1][j - 1] * verticalMask[1 + 1][1 - 1]);
						picture[i][j] = Color.HSBtoRGB((int)Math.toDegrees(Math.atan2(colorY, colorX))/360f, 1f, 1f);
					}
				resetPicture();
			}
		}));
		
		fun.add(menuFunction("Colored Sobel", new ActionListener() {
			private float[][] horizontalMask = new float[][] { { -1, 0, 1 }, { -2, 0, 2 }, { -1, 0, 1 } };
			private float[][] verticalMask = new float[][] { { -1, -2, -1 }, { 0, 0, 0 }, { 1, 2, 1 } };

			@Override
			public void actionPerformed(ActionEvent evt) {
				luminocity();
				int[][] temp = new int[picture.length][picture[0].length];
				for (int i = 0; i < height; i++)
					for (int j = 0; j < width; j++)
						temp[i][j] = getPixelArray(picture[i][j])[1];

				for (int i = 1; i < height - 1; i++)
					for (int j = 1; j < width - 1; j++) {
						int colorX = (int) (temp[i][j] * horizontalMask[1][1]
								+ temp[i - 1][j] * horizontalMask[1 - 1][1] + temp[i][j - 1] * horizontalMask[1][1 - 1]
								+ temp[i - 1][j - 1] * horizontalMask[1 - 1][1 - 1]
								+ temp[i + 1][j] * horizontalMask[1 + 1][1] + temp[i][j + 1] * horizontalMask[1][1 + 1]
								+ temp[i + 1][j + 1] * horizontalMask[1 + 1][1 + 1]
								+ temp[i - 1][j + 1] * horizontalMask[1 - 1][1 + 1]
								+ temp[i + 1][j - 1] * horizontalMask[1 + 1][1 - 1]);

						int colorY = (int) (temp[i][j] * verticalMask[1][1] + temp[i - 1][j] * verticalMask[1 - 1][1]
								+ temp[i][j - 1] * verticalMask[1][1 - 1]
								+ temp[i - 1][j - 1] * verticalMask[1 - 1][1 - 1]
								+ temp[i + 1][j] * verticalMask[1 + 1][1] + temp[i][j + 1] * verticalMask[1][1 + 1]
								+ temp[i + 1][j + 1] * verticalMask[1 + 1][1 + 1]
								+ temp[i - 1][j + 1] * verticalMask[1 - 1][1 + 1]
								+ temp[i + 1][j - 1] * verticalMask[1 + 1][1 - 1]);
						
						int color = (int) Math.sqrt(colorX * colorX + colorY * colorY);
						if(color > 25)
							picture[i][j] = Color.HSBtoRGB((int)Math.toDegrees(Math.atan2(colorY, colorX))/360f, 1f, 1f);
						else
							picture[i][j] = getPixels(new int[] { 255, color, color, color });
					}
				resetPicture();
			}
		}));

		fun.add(menuFunction("Gaussian Blur", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				blur(5);
				resetPicture();
			}
		}));
		
		fun.add(menuFunction("More Edges", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				int[][] temp = new int[picture.length][picture[0].length];
				for (int i = 0; i < height; i++)
					for (int j = 0; j < width; j++)
						temp[i][j] = picture[i][j];
				
				blur(5);
				sobel();
				
				for (int i = 0; i < height; i++)
					for (int j = 0; j < width; j++)
						temp[i][j] -= picture[i][j];
				
				for (int i = 0; i < height; i++)
					for (int j = 0; j < width; j++)
						picture[i][j] = temp[i][j];
				resetPicture();
			}
		}));
		
		fun.add(menuFunction("Add Edges", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				addEdges();
				resetPicture();
			}
		}));
		
		fun.add(menuFunction("Subtract Edges", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				subEdges();
				resetPicture();
			}
		}));
		
		fun.add(menuFunction("Abstractify", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				addEdges();
				resetPicture();
				subEdges();
				resetPicture();
				addEdges();
				resetPicture();
				subEdges();
				resetPicture();
				addEdges();
				resetPicture();
				subEdges();
				resetPicture();
			}
		}));
		
		return fun;
	}
	
	private void subEdges (){
		int[][] temp = new int[picture.length][picture[0].length];
		for (int i = 0; i < height; i++)
			for (int j = 0; j < width; j++)
				temp[i][j] = picture[i][j];
		
		blur(5);
		sobel();
		
		for (int i = 0; i < height; i++)
			for (int j = 0; j < width; j++) {
				int[] rgba = getPixelArray(temp[i][j]);
				int[] rgba2 = getPixelArray(picture[i][j]);
				for(int k = 1; k < rgba.length; k++){
					rgba[k] -= rgba2[k];
					if(rgba[k] < 0)
						rgba[k]=0;
				}
				temp[i][j] = getPixels(rgba);
			}
		
		for (int i = 0; i < height; i++)
			for (int j = 0; j < width; j++)
				picture[i][j] = temp[i][j];
	}
	
	private void addEdges () {
		int[][] temp = new int[picture.length][picture[0].length];
		for (int i = 0; i < height; i++)
			for (int j = 0; j < width; j++)
				temp[i][j] = picture[i][j];
		
		blur(5);
		sobel();
		
		for (int i = 0; i < height; i++)
			for (int j = 0; j < width; j++) {
				int[] rgba = getPixelArray(temp[i][j]);
				int[] rgba2 = getPixelArray(picture[i][j]);
				for(int k = 1; k < rgba.length; k++){
					rgba[k] += rgba2[k];
					if(rgba[k] > 255)
						rgba[k]=255;
				}
				temp[i][j] = getPixels(rgba);
			}
		
		for (int i = 0; i < height; i++)
			for (int j = 0; j < width; j++)
				picture[i][j] = temp[i][j];
	}

	private void luminocity() {
		for (int i = 0; i < height; i++)
			for (int j = 0; j < width; j++) {
				int[] tempArray = getPixelArray(picture[i][j]);
				int grayscale = (int) (tempArray[1] * .2126 + tempArray[2] * .7152 + tempArray[3] * .0722);
				for (int k = 1; k < tempArray.length; k++)
					tempArray[k] = grayscale;
				picture[i][j] = getPixels(tempArray);
			}
		resetPicture();
	}

	private void blur(float amt) {
		GaussianFilter gf = new GaussianFilter(5f);
		BufferedImage in = new BufferedImage(img.getImage().getWidth(null), img.getImage().getHeight(null), BufferedImage.TYPE_INT_ARGB);
		
		Image currentImage = toolkit.createImage(new MemoryImageSource(width, height, pixels, 0, width));
		
		Graphics2D bGr = in.createGraphics();
	    bGr.drawImage(currentImage, 0, 0, null);
	    bGr.dispose();
		
		BufferedImage out = new BufferedImage(img.getImage().getWidth(null), img.getImage().getHeight(null), BufferedImage.TYPE_INT_ARGB);
		gf.filter(in, out);
		
		PixelGrabber pg = new PixelGrabber(out, 0, 0, width, height, pixels, 0, width);
		try {
			pg.grabPixels();
		} catch (InterruptedException e) {
			System.err.println("Interrupted waiting for pixels");
			return;
		}
		turnTwoDimensional();
//		int[][] temp = new int[picture.length][picture[0].length];
//		for (int i = 0; i < height; i++)
//			for (int j = 0; j < width; j++)
//				temp[i][j] = getPixelArray(picture[i][j])[1];
//		
//		for (int i = 1; i < height - 1; i++)
//			for (int j = 1; j < width - 1; j++) {
//				picture[i][j] = (int)(temp[i-1][j] * 1/9f + 
//						temp[i-1][j-1] * 1/9f + 
//						temp[i][j-1] * 1/9f + 
//						temp[i][j] * 1/9f + 
//						temp[i+1][j] * 1/9f + 
//						temp[i-1][j+1] * 1/9f + 
//						temp[i-1][j+1] * 1/9f + 
//						temp[i+1][j-1] * 1/9f + 
//						temp[i][j+1] * 1/9f);
//			}
		resetPicture();
	}
	
	private void sobel (){
		float[][] horizontalMask = new float[][] { { -1, 0, 1 }, { -2, 0, 2 }, { -1, 0, 1 } };
		float[][] verticalMask = new float[][] { { -1, -2, -1 }, { 0, 0, 0 }, { 1, 2, 1 } };
		luminocity();
		int[][] temp = new int[picture.length][picture[0].length];
		for (int i = 0; i < height; i++)
			for (int j = 0; j < width; j++)
				temp[i][j] = getPixelArray(picture[i][j])[1];

		for (int i = 1; i < height - 1; i++)
			for (int j = 1; j < width - 1; j++) {
				int colorX = (int) (temp[i][j] * horizontalMask[1][1]
						+ temp[i - 1][j] * horizontalMask[1 - 1][1] + temp[i][j - 1] * horizontalMask[1][1 - 1]
						+ temp[i - 1][j - 1] * horizontalMask[1 - 1][1 - 1]
						+ temp[i + 1][j] * horizontalMask[1 + 1][1] + temp[i][j + 1] * horizontalMask[1][1 + 1]
						+ temp[i + 1][j + 1] * horizontalMask[1 + 1][1 + 1]
						+ temp[i - 1][j + 1] * horizontalMask[1 - 1][1 + 1]
						+ temp[i + 1][j - 1] * horizontalMask[1 + 1][1 - 1]);

				int colorY = (int) (temp[i][j] * verticalMask[1][1] + temp[i - 1][j] * verticalMask[1 - 1][1]
						+ temp[i][j - 1] * verticalMask[1][1 - 1]
						+ temp[i - 1][j - 1] * verticalMask[1 - 1][1 - 1]
						+ temp[i + 1][j] * verticalMask[1 + 1][1] + temp[i][j + 1] * verticalMask[1][1 + 1]
						+ temp[i + 1][j + 1] * verticalMask[1 + 1][1 + 1]
						+ temp[i - 1][j + 1] * verticalMask[1 - 1][1 + 1]
						+ temp[i + 1][j - 1] * verticalMask[1 + 1][1 - 1]);
				int color = (int) Math.sqrt(colorX * colorX + colorY * colorY);
				picture[i][j] = getPixels(new int[] { 255, color, color, color });
			}
	}

	private float max(int[] values) {
		float max = -Float.MAX_VALUE;
		for (int i = 0; i < values.length; i++)
			if (values[i] > max)
				max = values[i];
		return max;
	}

	private float min(int[] values) {
		float min = Float.MAX_VALUE;
		for (int i = 0; i < values.length; i++)
			if (values[i] < min)
				min = values[i];
		return min;
	}

	private JMenuItem menuFunction(String name, ActionListener func) {
		JMenuItem menuItem = new JMenuItem(name);
		menuItem.addActionListener(func);
		return menuItem;
	}

	/*
	 * This method handles opening an image file, breaking down the picture to a
	 * one-dimensional array and then drawing the image on the frame. You don't
	 * need to worry about this method.
	 */
	private void handleOpen() {
		img = null;
		// Better file chooser for windows, but doesn't work well on other os
		if (System.getProperty("os.name").toLowerCase().contains("windows")) {
			FileDialog fd = new FileDialog(frame, "Choose a file", FileDialog.LOAD);
			fd.setDirectory("C:\\Users\\Dan\\Dropbox\\");
			fd.setVisible(true);
			String filename = fd.getFile();
			if (filename == null)
				return;
			else {
				loadImage(new File(fd.getDirectory() + "\\" + fd.getFile()));
			}
		} else {
			JFileChooser chooser = new JFileChooser();
			chooser.setCurrentDirectory(new File("C:\\Users\\Dan\\Dropbox\\"));
			int option = chooser.showOpenDialog(frame);
			if (option == JFileChooser.APPROVE_OPTION) {
				loadImage(chooser.getSelectedFile());
			} else
				return;
		}
		
	}
	
	private void loadImage(File path){
		pic = path;
		img = new ImageIcon(pic.getPath());
		
		initWidth = width = img.getIconWidth();
		initHeight = height = img.getIconHeight();

		label.setIcon(img);

		pixels = new int[width * height];

		results = new int[width * height];

		Image image = img.getImage();

		PixelGrabber pg = new PixelGrabber(image, 0, 0, width, height, pixels, 0, width);
		try {
			pg.grabPixels();
		} catch (InterruptedException e) {
			System.err.println("Interrupted waiting for pixels");
			return;
		}
		for (int i = 0; i < width * height; i++)
			results[i] = pixels[i];
		turnTwoDimensional();

		mp.revalidate();
	}

	/*
	 * The libraries in Java give a one dimensional array of RGB values for an
	 * image, I thought a 2-Dimensional array would be more usefull to you So
	 * this method changes the one dimensional array to a two-dimensional.
	 */
	private void turnTwoDimensional() {
		picture = new int[height][width];
		for (int i = 0; i < height; i++)
			for (int j = 0; j < width; j++)
				picture[i][j] = pixels[i * width + j];
	}

	/*
	 * This method takes the picture back to the original picture
	 */
	private void reset() {
		width = initWidth;
		height = initHeight;

		for (int i = 0; i < width * height; i++)
			pixels[i] = results[i];

		picture = new int[height][width];
		for (int i = 0; i < height; i++)
			for (int j = 0; j < width; j++)
				picture[i][j] = pixels[i * width + j];

		Image img2 = img.getImage();

		label.setIcon(new ImageIcon(img2));

		mp.revalidate();
	}

	/*
	 * This method is called to redraw the screen with the new image.
	 */
	private void resetPicture() {
		for (int i = 0; i < height; i++)
			for (int j = 0; j < width; j++)
				pixels[i * width + j] = picture[i][j];
		Image img2 = toolkit.createImage(new MemoryImageSource(width, height, pixels, 0, width));

		if(history.size() <= historySize)
			history.add(img2);
		else {
			history.removeFirst();
			history.add(img2);
		}
		
		label.setIcon(new ImageIcon(img2));
		mp.revalidate();
	}
	
	private void setPicture (Image img){
		
		PixelGrabber pg = new PixelGrabber(img, 0, 0, width, height, pixels, 0, width);
		try {
			pg.grabPixels();
		} catch (InterruptedException e) {
			System.err.println("Interrupted waiting for pixels");
			return;
		}
		turnTwoDimensional();
		
		
		label.setIcon(new ImageIcon(img));
		mp.revalidate();
	}

	/*
	 * This method takes a single integer value and breaks it down doing bit
	 * manipulation to 4 individual int values for A, R, G, and B values
	 */
	private int[] getPixelArray(int pixel) {
		int temp[] = new int[4];
		temp[0] = (pixel >> 24) & 0xff;
		temp[1] = (pixel >> 16) & 0xff;
		temp[2] = (pixel >> 8) & 0xff;
		temp[3] = (pixel) & 0xff;
		return temp;
	}

	/*
	 * This method takes an array of size 4 and combines the first 8 bits of
	 * each to create one integer.
	 */
	private int getPixels(int rgb[]) {
		int rgba = (rgb[0] << 24) | (rgb[1] << 16) | (rgb[2] << 8) | rgb[3];
		return rgba;
	}

	public void getValue() {
		int pix = picture[colorY][colorX];
		int temp[] = getPixelArray(pix);
		System.out.println("Color value " + temp[0] + " " + temp[1] + " " + temp[2] + " " + temp[3]);
	}

	/**************************************************************************************************
	 * This is where you will put your methods. Every method below is called
	 * when the corresponding pulldown menu is used. As long as you have a
	 * picture open first the when your fun1, fun2, fun....etc method is called
	 * you will have a 2D array called picture that is holding each pixel from
	 * your picture.
	 *************************************************************************************************/
	/*
	 * Example function that just removes all red values from the picture. Each
	 * pixel value in picture[i][j] holds an integer value. You need to send
	 * that pixel to getPixelArray the method which will return a 4 element
	 * array that holds A,R,G,B values. Ignore [0], that's the Alpha channel
	 * which is transparency, we won't be using that, but you can on your own.
	 * getPixelArray will breaks down your single int to 4 ints so you can
	 * manipulate the values for each level of R, G, B. After you make changes
	 * and do your calculations to your pixel values the getPixels method will
	 * put the 4 values in your ARGB array back into a single integer value so
	 * you can give it back to the program and display the new picture.
	 */
	// private void fun1() {}

	private void quit() {
		System.exit(0);
	}

	public static void main(String[] args) {
		new IMP();
	}

}
