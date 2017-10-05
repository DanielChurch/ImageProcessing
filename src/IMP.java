/*
 *Hunter Lloyd
 * Copyright.......I wrote, ask permission if you want to use it outside of class. 
 */

/*
 * Daniel Church
 * -02226198
 * 02/06/17
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
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
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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
	private int initWidth = 0, initHeight = 0; //for reset

	// your 2D array of pixels
	private int picture[][];

	//Allows user to undo changes, and eventually redo
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
		frame = new JFrame("Image Processing Software by Hunter Edited by Daniel Church");
		JMenuBar bar = new JMenuBar();
		JMenu file = new JMenu("File");
		JMenu functions = getFunctions();
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent ev) {
				quit();
			}
		});

		JMenuItem neww = new JMenuItem("New");
		neww.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_MASK));
		neww.addActionListener((ActionEvent ae) -> {
			label.setText("Drop an Image");
			label.setIcon(null);
		});
		file.add(neww);

		openItem = new JMenuItem("Open");
		openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK));
		openItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				handleOpen();
			}
		});
		file.add(openItem);

		JMenuItem save = new JMenuItem("Save");
		save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_MASK));
		file.add(save);
		save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Image currentImage = toolkit.createImage(new MemoryImageSource(width, height, pixels, 0, width));

				if (System.getProperty("os.name").toLowerCase().contains("windows")) {
					FileDialog fd = new FileDialog(frame, "Choose a file", FileDialog.SAVE);
					fd.setDirectory("%USERPROFILE%\\Pictures\\");
					fd.setVisible(true);
					String filename = fd.getFile();
					if (filename == null)
						return;
					else {
						BufferedImage bi = null;
						if (fd.getFile().toLowerCase().contains("jpg") || !fd.getFile().contains("."))
							bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
						else // Assume allowance of transparency
							bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
						Graphics2D g = (Graphics2D) bi.getGraphics();
						g.drawImage(currentImage, 0, 0, null);
						g.dispose();

						try {
							ImageIO.write(bi, "JPG", new File(fd.getDirectory() + "\\" + fd.getFile()
									+ (!fd.getFile().contains(".") ? ".jpg" : "")));
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}
				} else {
					JFileChooser chooser = new JFileChooser();
					chooser.setCurrentDirectory(new File("%USERPROFILE%\\Pictures\\"));
					int option = chooser.showOpenDialog(frame);
					if (option == JFileChooser.APPROVE_OPTION) {

						BufferedImage bi = null;
						if (chooser.getSelectedFile().getPath().toLowerCase().contains("jpg")
								|| !chooser.getSelectedFile().getPath().contains("."))
							bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
						else // Assume allowance of transparency
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
		resetItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_MASK));
		resetItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				reset();
			}
		});
		exitItem = new JMenuItem("Exit");
		exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.ALT_MASK));
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

		JMenuItem undo;
		edit.add(undo = menuFunction("Undo", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (historyIndex < 1)
					return;
				historyIndex--;
				setPicture(history.get(historyIndex));
			}
		}));
		undo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_MASK));
		
		JMenuItem redo;
		edit.add(redo = menuFunction("Redo - WIP", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (historyIndex >= history.size()-1)
					return;
				historyIndex++;
				setPicture(history.get(historyIndex));
			}
		}));
		redo.setEnabled(false);
		redo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_MASK));

		bar.add(functions);
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

		label = new JLabel("Drop an Image", SwingUtilities.CENTER);
		label.setDropTarget(new DropTarget(frame, new DropTargetListener() {

			@Override
			public void dropActionChanged(DropTargetDragEvent arg0) { }

			@Override
			public void drop(DropTargetDropEvent de) {
				Transferable transferable = de.getTransferable();
				if (de.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
					de.acceptDrop(de.getDropAction());
					try {
						List transferData = (List) transferable.getTransferData(DataFlavor.javaFileListFlavor);
						if (transferData != null && transferData.size() > 0) {
							for (Object f : transferData)
								if (f instanceof File)
									loadImage((File) f);
							de.dropComplete(true);
						}

					} catch (Exception ex) {
						ex.printStackTrace();
					}
				} else {
					de.rejectDrop();
				}
			}

			@Override
			public void dragOver(DropTargetDragEvent arg0) { }

			@Override
			public void dragExit(DropTargetEvent arg0) { }

			@Override
			public void dragEnter(DropTargetDragEvent arg0) { }
		}));
		label.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent m) {
				colorX = m.getXOnScreen();
				colorY = m.getYOnScreen();
				getValue();
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
				if (viewPort != null) {
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

		frame.setPreferredSize(new Dimension(600,600));
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

	}

	/*
	 * This method creates the pulldown menu and sets up listeners to selection
	 * of the menu choices. If the listeners are activated they call the methods
	 * for handling the choice, fun1, fun2, fun3, fun4, etc. etc.
	 */

	private JMenu getFunctions() {
		JMenu fun = new JMenu("Functions");

		fun.add(menuFunction("Set Tint", (ActionEvent ae) -> {
			JFrame f = new JFrame("Color Chooser");
			f.setLayout(new BorderLayout());
			JColorChooser jcc = new JColorChooser ();
			f.add(jcc, BorderLayout.CENTER);
			f.pack();
			f.setLocationRelativeTo(null);
			f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			JButton close;
			f.add(close = new JButton("Close"), BorderLayout.SOUTH);
			close.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					f.dispose();
				}
			});
			f.setVisible(true);
			jcc.getSelectionModel().addChangeListener(new ChangeListener () {
				boolean first = true;
				int[][] u;
				@Override
				public void stateChanged(ChangeEvent arg0) {
					if(first) {
						first = false;
						u = new int[picture.length][picture[0].length];
						for (int i = 0; i < height; i++)
							for (int j = 0; j < width; j++)
								u[i][j] = picture[i][j];
					} else {
						for (int i = 0; i < height; i++)
							for (int j = 0; j < width; j++)
								picture[i][j] = u[i][j];
						historyIndex--;
					}
						
					Color c = jcc.getColor();
					
					for (int i = 0; i < height; i++)
						for (int j = 0; j < width; j++) {
							int rgbArray[] = new int[4];
							
							// get three ints for R, G and B
							rgbArray = getPixelArray(picture[i][j]);

							rgbArray[1] *= c.getRed()/255f;
							rgbArray[2] *= c.getGreen()/255f;
							rgbArray[3] *= c.getBlue()/255f;
							// take three ints for R, G, B and put them back into a
							// single
							// int
							picture[i][j] = getPixels(rgbArray);
						}
					resetPicture();
				}
			});
			jcc.setVisible(true);
		}));

		fun.add(menuFunction("Invert", (ActionEvent ae) -> {
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
		}));

		fun.add(menuFunction("Rotate 90", (ActionEvent ae) -> {
			int[][] temp = picture;
			picture = new int[width][height];
			for (int i = 0; i < height; i++)
				for (int j = 0; j < width; j++)
					picture[j][i] = temp[height-1-i][j];
			width = height;
			height = picture.length;
			resetPicture();
		}));

		JMenu grayscale = new JMenu("Grayscale");
		fun.add(grayscale);

		grayscale.add(menuFunction("Average",(ActionEvent ae) -> {
			for (int i = 0; i < height; i++)
				for (int j = 0; j < width; j++) {
					int[] tempArray = getPixelArray(picture[i][j]);
					int value = (int) (tempArray[1] * 1f / 3 + tempArray[2] * 1f / 3 + tempArray[3] * 1f / 3);
					for (int k = 1; k < tempArray.length; k++)
						tempArray[k] = value;
					picture[i][j] = getPixels(tempArray);
				}
			resetPicture();
		}));

		grayscale.add(menuFunction("Min & Max", (ActionEvent ae) -> {
			for (int i = 0; i < height; i++)
				for (int j = 0; j < width; j++) {
					int[] tempArray = getPixelArray(picture[i][j]);
					int value = (int) (min(tempArray) / 2f + max(tempArray) / 2f);
					for (int k = 1; k < tempArray.length; k++)
						tempArray[k] = value;
					picture[i][j] = getPixels(tempArray);
				}
			resetPicture();
		}));

		grayscale.add(menuFunction("Luminocity", (ActionEvent ae) -> {
			luminocity();
			resetPicture();
		}));

		JMenu edgeDetection = new JMenu("Edge Detection");
		fun.add(edgeDetection);
		
		edgeDetection.add(menuFunction("Edge Detection", (ActionEvent ae) -> {
				JFrame f = new JFrame("Edge Threshold");
				JPanel maskSizeSliderPanel = new JPanel();
				maskSizeSliderPanel.setLayout(new BorderLayout());
				JSlider maskSizeSlider = new JSlider();
				maskSizeSlider.setMinimum(3);
				maskSizeSlider.setMaximum(15);
				maskSizeSlider.setValue(5);
				maskSizeSliderPanel.add(new JLabel("Mask Size: "), BorderLayout.WEST);
				maskSizeSliderPanel.add(maskSizeSlider, BorderLayout.CENTER);
				
				JSlider slider = new JSlider();
				JPanel sliderPanel = new JPanel();
				sliderPanel.setLayout(new BorderLayout());
				sliderPanel.add(new JLabel("Edge Threshold: "), BorderLayout.WEST);
				sliderPanel.add(slider, BorderLayout.CENTER);
				slider.setMinimum(10);
				slider.setMaximum(255);
				slider.setValue(125);
				ChangeListener cl;
				slider.addChangeListener(cl = new ChangeListener() {
					boolean first = true;
					int[][]u;
					@Override
					public void stateChanged(ChangeEvent e) {
						if(first) {
							first = false;
							u = new int[picture.length][picture[0].length];
							for (int i = 0; i < height; i++)
								for (int j = 0; j < width; j++)
									u[i][j] = picture[i][j];
						} else {
							for (int i = 0; i < height; i++)
								for (int j = 0; j < width; j++)
									picture[i][j] = u[i][j];
							historyIndex--;
						}
						
						int size = maskSizeSlider.getValue() % 2 == 1 ? maskSizeSlider.getValue() : maskSizeSlider.getValue() + 1;
						
						float [][] mask = new float[size][size];
						for(int i = 0; i < size; i++)
							mask[0][i] = -1;
						for(int i = 0; i < size; i++)
							mask[size-1][i] = -1;
						for(int i = 0; i < size; i++)
							mask[i][0] = -1;
						for(int i = 0; i < size; i++)
							mask[i][size-1] = -1;
						mask[size/2][size/2] = size * 2 + (size-2)*2;
						
						luminocity();
						applyMask(mask);
						for (int i = 0; i < height; i++)
							for (int j = 0; j < width; j++) {
								if(getPixelArray(picture[i][j])[1] > slider.getValue())
									picture[i][j] = Color.white.getRGB();
								else
									picture[i][j] = Color.black.getRGB();
							}
						resetPicture();
					}
				});
				
				maskSizeSlider.addChangeListener(cl);
				
				f.setLayout(new BorderLayout());
				f.add(sliderPanel, BorderLayout.CENTER);
				f.add(maskSizeSliderPanel, BorderLayout.NORTH);
				f.pack();
				f.setSize(300,100);
				f.setLocationRelativeTo(null);
				f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				JButton close;
				f.add(close = new JButton("Close"), BorderLayout.SOUTH);
				close.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						f.dispose();
					}
				});
				f.setVisible(true);
		}));

		edgeDetection.add(menuFunction("Sobel Edge Detection", (ActionEvent ae) -> {
			sobel();
			resetPicture();
		}));

		edgeDetection.add(menuFunction("Colored Sobel Edge Detection", (ActionEvent ae) -> {
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
					picture[i][j] = Color.HSBtoRGB((int) Math.toDegrees(Math.atan2(colorY, colorX)) / 360f, 1f, 1f);
				}
			resetPicture();
		}));

		fun.add(menuFunction("Blur", (ActionEvent ae) -> {
			JFrame f = new JFrame("Blur Amount");
			JSlider slider = new JSlider();
			slider.setMinimum(1);
			slider.setMaximum(25);
			slider.setValue(3);
			slider.addChangeListener(new ChangeListener() {
				boolean first = true;
				int[][]u;
				@Override
				public void stateChanged(ChangeEvent e) {
					if(first) {
						first = false;
						u = new int[picture.length][picture[0].length];
						for (int i = 0; i < height; i++)
							for (int j = 0; j < width; j++)
								u[i][j] = picture[i][j];
					} else {
						for (int i = 0; i < height; i++)
							for (int j = 0; j < width; j++)
								picture[i][j] = u[i][j];
						historyIndex--;
					}
					
					blur(slider.getValue() % 2 == 1 ? slider.getValue() : slider.getValue() + 1);
					resetPicture();
				}
			});
			f.setLayout(new BorderLayout());
			f.add(slider, BorderLayout.CENTER);
			f.pack();
			f.setSize(300,100);
			f.setLocationRelativeTo(null);
			f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			JButton close;
			f.add(close = new JButton("Close"), BorderLayout.SOUTH);
			close.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					f.dispose();
				}
			});
			f.setVisible(true);
		}));

		fun.add(menuFunction("Add Edges", (ActionEvent ae) -> {
			addEdges();
			resetPicture();
		}));

		fun.add(menuFunction("Subtract Edges", (ActionEvent ae) -> {
			subEdges();
			resetPicture();
		}));
		
		fun.add(menuFunction("Histogram", (ActionEvent ae) -> {
			histogram();
		}));
		
		fun.add(menuFunction("Color Equalization", (ActionEvent ae) -> {
			colorCorrection();
			resetPicture();
		}));

		fun.add(menuFunction("Object Tracking", (ActionEvent ae) -> {
			JOptionPane.showMessageDialog(frame, "Click a pixel and select the threshold you want.", "Edge Detection Instructions", JOptionPane.INFORMATION_MESSAGE);
		}));
		
		return fun;
	}

	private void blur(float size) {
		float[][] mask = new float[(int) size][(int) size];
		float[] inner = new float[(int) size];
		Arrays.fill(inner, 1/size/size);
		for(int i = 0; i < size; i++)
			mask[i] = inner;
		applyMask(mask);
	}

	private void subEdges() {
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
				for (int k = 1; k < rgba.length; k++) {
					rgba[k] -= rgba2[k];
					if (rgba[k] < 0)
						rgba[k] = 0;
				}
				temp[i][j] = getPixels(rgba);
			}

		for (int i = 0; i < height; i++)
			for (int j = 0; j < width; j++)
				picture[i][j] = temp[i][j];
	}

	private void addEdges() {
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
				for (int k = 1; k < rgba.length; k++) {
					rgba[k] += rgba2[k];
					if (rgba[k] > 255)
						rgba[k] = 255;
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
	}

	private void applyMask(float[][] mask) {
		int[][] temp = new int[picture.length][picture[0].length];
		for (int i = 0; i < height; i++)
			for (int j = 0; j < width; j++)
				temp[i][j] = getPixelArray(picture[i][j])[1];

		int maskWidth = (int) Math.floor(mask.length / 2);
		int maskHeight = (int) Math.floor(mask[0].length / 2);

		for (int i = maskHeight; i < height - maskHeight; i++)
			for (int j = maskWidth; j < width - maskWidth; j++) {
				int sum = 0;
				for (int m = -maskWidth; m <= maskWidth; m++)
					for (int k = -maskHeight; k <= maskHeight; k++)
						sum += temp[i - m][j - k] * mask[maskWidth - m][maskHeight - k];
				sum = clamp(sum,0,255);
				picture[i][j] = getPixels(new int[] { 255, sum, sum, sum });
			}
	}

	private float max = 0;
	private void histogram (){
		JFrame histogramFrame = new JFrame();
		histogramFrame.setTitle("Histogram");
		int histWidth = 768; //3*256
		int histHeight = 600;
		histogramFrame.getContentPane().setPreferredSize(new Dimension(histWidth, histHeight));
		histogramFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		//get count
		max = 0;
		int[][] count = new int[3][256];
		for (int i = 0; i < height; i++)
			for (int j = 0; j < width; j++){
				int[] color = getPixelArray(picture[i][j]);
				for(int k = 0; k < 3; k++)
					if(count[k][color[k+1]]++ > max) max = count[k][color[k+1]];
			}
		System.out.println(max);
		JPanel renderPanel = new JPanel(){
			@Override
			public void paintComponent(Graphics g){
				super.paintComponent(g);
				g.setColor(Color.DARK_GRAY);
				g.fillRect(0, 0, histWidth, histHeight);
				
				for(int i = 0; i < histWidth; i++) {
					int r = i < histWidth/3 ? i : i < 2*histWidth/3 ? 0 : 0;
					int gr = i < histWidth/3 ? 0 : i < 2*histWidth/3 ? i%256 : 0;
					int b = i < histWidth/3 ? 0 : i < 2*histWidth/3 ? 0 : i%256;
					g.setColor(new Color(r,gr,b));
					g.drawLine(i, histHeight, i, (int)(histHeight-histHeight*count[i < histWidth/3 ? 0 : i < 2*histWidth/3 ? 1 : 2][i%256]/max));
				}
			}
		};
		renderPanel.setSize(new Dimension(histWidth,histHeight));
		histogramFrame.add(renderPanel);
		histogramFrame.pack();
		histogramFrame.setVisible(true);
		histogramFrame.setLocationRelativeTo(null);
	}
	
	private void colorCorrection (){ //Color Equalization
		//calculate histogram
		int[][] count = new int[3][256];
		for (int i = 0; i < height; i++)
			for (int j = 0; j < width; j++){
				int[] color = getPixelArray(picture[i][j]);
				for(int k = 0; k < 3; k++)
					if(count[k][color[k+1]]++ > max) max = count[k][color[k+1]];
			}
		
		//create lookup table
		float[] px = new float[256];
		int sum = 0;
		for(int j = 0; j < 3; j++) {
			sum = 0;
			for(int i = 0; i < 256; i++) {
				sum+=count[j][i];
				px[i] = sum * 255 / (height * width);
			}
		}

		//use lookup table to convert
		for (int i = 0; i < height; i++)
			for (int j = 0; j < width; j++) {
				int[] argb = getPixelArray(picture[i][j]);
				for(int k = 0; k < 3; k++) {
					argb[k+1] = clamp((int)px[argb[k+1]], 0, 255);
					picture[i][j] = getPixels(argb);
				}
			}
	}

	private int clamp(int value, int min, int max){
		return value < min ? min : value > max ? max : value;
	}
	
	//Another form of edge detection
	private void sobel() {
		float[][] horizontalMask = new float[][] { { -1, 0, 1 }, { -2, 0, 2 }, { -1, 0, 1 } };
		float[][] verticalMask = new float[][] { { -1, -2, -1 }, { 0, 0, 0 }, { 1, 2, 1 } };
		luminocity();
		int[][] temp = new int[picture.length][picture[0].length];
		for (int i = 0; i < height; i++)
			for (int j = 0; j < width; j++)
				temp[i][j] = getPixelArray(picture[i][j])[1];

		for (int i = 1; i < height - 1; i++)
			for (int j = 1; j < width - 1; j++) {
				int colorX = (int) (temp[i][j] * horizontalMask[1][1] + temp[i - 1][j] * horizontalMask[1 - 1][1]
						+ temp[i][j - 1] * horizontalMask[1][1 - 1] + temp[i - 1][j - 1] * horizontalMask[1 - 1][1 - 1]
						+ temp[i + 1][j] * horizontalMask[1 + 1][1] + temp[i][j + 1] * horizontalMask[1][1 + 1]
						+ temp[i + 1][j + 1] * horizontalMask[1 + 1][1 + 1]
						+ temp[i - 1][j + 1] * horizontalMask[1 - 1][1 + 1]
						+ temp[i + 1][j - 1] * horizontalMask[1 + 1][1 - 1]);

				int colorY = (int) (temp[i][j] * verticalMask[1][1] + temp[i - 1][j] * verticalMask[1 - 1][1]
						+ temp[i][j - 1] * verticalMask[1][1 - 1] + temp[i - 1][j - 1] * verticalMask[1 - 1][1 - 1]
						+ temp[i + 1][j] * verticalMask[1 + 1][1] + temp[i][j + 1] * verticalMask[1][1 + 1]
						+ temp[i + 1][j + 1] * verticalMask[1 + 1][1 + 1]
						+ temp[i - 1][j + 1] * verticalMask[1 - 1][1 + 1]
						+ temp[i + 1][j - 1] * verticalMask[1 + 1][1 - 1]);
				int color = (int) Math.sqrt(colorX * colorX + colorY * colorY);
				picture[i][j] = getPixels(new int[] { 255, color, color, color });
			}
	}

	//returns the max value of an array
	private float max(int[] values) {
		float max = -Float.MAX_VALUE;
		for (int i = 1; i < values.length; i++) //start at 1 to ignore alpha
			if (values[i] > max)
				max = values[i];
		return max;
	}

	//returns the min value of an array
	private float min(int[] values) {
		float min = Float.MAX_VALUE;
		for (int i = 1; i < values.length; i++) //start at 1 to ignore alpha
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
		// Better file chooser for windows, but doesn't work well on other os (so i've heard)
		if (System.getProperty("os.name").toLowerCase().contains("windows")) {
			FileDialog fd = new FileDialog(frame, "Choose a file", FileDialog.LOAD);
			fd.setDirectory("%USERPROFILE%\\Pictures\\");
			fd.setVisible(true);
			String filename = fd.getFile();
			if (filename == null)
				return;
			else {
				loadImage(new File(fd.getDirectory() + "\\" + fd.getFile()));
			}
		} else {
			JFileChooser chooser = new JFileChooser();
			chooser.setCurrentDirectory(new File("%USERPROFILE%\\Pictures\\"));
			int option = chooser.showOpenDialog(frame);
			if (option == JFileChooser.APPROVE_OPTION) {
				loadImage(chooser.getSelectedFile());
			} else
				return;
		}
	}

	private void loadImage(File path) {
		pic = path;
		img = new ImageIcon(pic.getPath());
		history.clear();
		historyIndex = 0;
		history.add(img.getImage());
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
		
		label.setText("");
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

		label.setIcon(img);

		history.clear();
		historyIndex = 0;
		history.add(img.getImage());
	}

	/*
	 * This method is called to redraw the screen with the new image.
	 */
	private void resetPicture() {
		for (int i = 0; i < height; i++)
			for (int j = 0; j < width; j++)
				pixels[i * width + j] = picture[i][j];
		Image img2 = toolkit.createImage(new MemoryImageSource(width, height, pixels, 0, width));

		if (history.size() > historySize)
			history.removeFirst();
		if(historyIndex < historySize) {
			historyIndex++;
			if(history.size() > historyIndex)
				for(int i = historyIndex-1; i < history.size(); i++)
					history.removeLast();
		}
		history.add(img2);
		
		label.setIcon(new ImageIcon(img2));
	}

	private void setPicture(Image img) {
		width = img.getWidth(null);
		height = img.getHeight(null);
		PixelGrabber pg = new PixelGrabber(img, 0, 0, width, height, pixels, 0, width);
		try {
			pg.grabPixels();
		} catch (InterruptedException e) {
			System.err.println("Interrupted waiting for pixels");
			return;
		}
		turnTwoDimensional();

		label.setIcon(new ImageIcon(img));
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
		String jop = JOptionPane.showInputDialog(frame, "Please enter the threshold", "Threshold", JOptionPane.INFORMATION_MESSAGE);
		if(jop == null)
			return;
		int threshold;
		try{
			threshold = Integer.parseInt(jop);
		}catch(Exception e){
			threshold = 15; //Default to 15 if they don't enter a valid number
		}
		int[] col = null;
		try{
			Robot robot = new Robot(); //Robot class to remove the difficulties of finding array index of pixel since its centered in the jlabel
			robot.delay(100);
			Color c = robot.getPixelColor(colorX, colorY);
			
			col = new int[]{255, c.getRed(), c.getGreen(), c.getBlue()};
		}catch(Exception e){
			e.printStackTrace();
		}
		track(col, threshold);
		resetPicture();
	}
	
	//sets all pixels in image within a threshold of the given rgb value to white, and the others to black
	private void track(int[] col, int threshold){
		for (int i = 0; i < height; i++)
			for (int j = 0; j < width; j++) {
				int[] pixArr = getPixelArray(picture[i][j]);
				boolean inThreshold = true;
				for(int k = 1; k < 4; k++)
					if(!(pixArr[k] > col[k] - threshold && pixArr[k] < col[k] + threshold)) {
						inThreshold = false;
						break;
					}
				if(inThreshold)
					picture[i][j] = getPixels(new int[]{255, 255, 255, 255});
				else
					picture[i][j] = getPixels(new int[]{255, 0, 0, 0});
			}
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
		try {
			UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (Exception e) {
			e.printStackTrace();
		}
		new IMP();
	}

}
