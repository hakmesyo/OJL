package jazari.gui;

import java.awt.BasicStroke;
import jazari.image_processing.ImageProcess;
import jazari.types.TStatistics;
import jazari.matrix.CMatrix;
import jazari.factory.FactoryUtils;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.ButtonGroup;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;
import jazari.interfaces.call_back_interface.CallBackTrigger;
import jazari.utils.MyDialog;
import jazari.utils.pascalvoc.AnnotationPascalVOCFormat;
import jazari.utils.pascalvoc.PascalVocAttribute;
import jazari.utils.pascalvoc.PascalVocBoundingBox;
import jazari.utils.pascalvoc.PascalVocLane;
import jazari.utils.pascalvoc.PascalVocObject;
import jazari.utils.pascalvoc.PascalVocPolygon;
import jazari.utils.pascalvoc.PascalVocSource;

public class PanelPicture extends JPanel implements KeyListener, MouseWheelListener {

    private Point p = new Point();
    public boolean isChainProcessing = false;
    private Point mousePos = new Point(0, 0);
    //private CPoint drawableMousePos = new CPoint(0, 0);
    private boolean isPolygonPressed = false;
    private Point mousePosTopLeft = new Point(0, 0);
    private Point mousePosTopRight = new Point(0, 0);
    private Point mousePosBottomLeft = new Point(0, 0);
    private Point mousePosBottomRight = new Point(0, 0);
    private boolean lblShow = true;
    private boolean showRegion = false;
    private BufferedImage currBufferedImage;
    private BufferedImage originalBufferedImage;
    private BufferedImage originalBufferedImageTemp;
    //private TimeWatch watch = TimeWatch.start();
    private JRadioButtonMenuItem items[];
    private final JPopupMenu popupMenu = new JPopupMenu();
    private int fromLeft = 10;
    private int fromTop = 30;
    private boolean activateOriginal = false;
    private boolean activateSaveImage = false;
    private boolean activateHistogram = false;
    private boolean activateStatistics = false;
    private boolean activateRedChannel = false;
    private boolean activateRevert = false;
    private boolean activateBinarize = false;
    public boolean activateCrop = false;
    private boolean activateScreenCapture = false;
    private boolean activateCmd = false;
    private boolean isCropStarted = false;
    private boolean isMouseDraggedForImageMovement;
    public boolean activateBoundingBox = false;
    public boolean activatePolygon = false;
    public boolean activateLaneDetection = false;
    private boolean activateGreenChannel = false;
    private boolean activateBlueChannel = false;
    private boolean activateRGB = false;
    private boolean activateGray = false;
    private boolean activateHSV = false;
    private boolean activateEdge = false;
    private boolean activateEqualize = false;
    private boolean activateAutoSize = false;
    private AnnotationPascalVOCFormat pascalVocXML;
    private List<PascalVocObject> listPascalVocObject = new ArrayList();
    private PascalVocSource source = new PascalVocSource();
    private PascalVocBoundingBox selectedBBox = null;
    private boolean activateAutoSizeAspect = false;
    private TStatistics stat;
    private DecimalFormat df = new DecimalFormat("#");
    private String imagePath = "-1";
    private String imageFolder;
    private String fileName;
    public float[][] imgData;
    private FrameImageHistogram histFrame = null;
    private final int panWidth = 50;
    private boolean isRedChannel = false;
    private boolean isGreenChannel = false;
    private boolean isBlueChannel = false;
    private boolean isBBoxResizeTopLeft = false;
    private boolean isBBoxResizeTopRight = false;
    private boolean isBBoxResizeBottomLeft = false;
    private boolean isBBoxResizeBottomRight = false;
    private File[] imageFiles;
    private int imageIndex = 0;
    private FrameImage frame;
    private String caption;
    private String lastSelectedClassName;
    private Color lastSelectedBoundingBoxColor;
    private Color lastSelectedPolygonColor;
    private Color defaultBoundingBoxColor = new Color(255, 255, 0);
    private boolean isMouseDraggedForBoundingBoxMovement = false;
    private boolean isMouseDraggedForPolygonMovement = false;
    public boolean isSeqenceVideoFrame;
    public Map<String, Color> mapBBoxColor = new HashMap();
    private String xmlFileName;
    private String currentFolderName;
    private PascalVocObject selectedPascalVocObject;
    private PascalVocPolygon selectedPolygon;
    private PascalVocLane selectedLane;
    private Point[] lastPositionOfDraggedBBox;
    private Point[] lastPositionOfDraggedPolygon;
    private Point referenceMousePositionForImageMovement;
    private Point currentMousePositionForImageMovement;
    private int defaultStrokeWidth = 2;
    //private int indexOfCurrentImageFile = 0;
    private Color colorDashedLine = new Color(255, 255, 0);
    private Point lastPolygonPoint = new Point(0, 0);
    private Polygon polygon = new Polygon();
    private boolean isCancelledPolygon = false;
    private int selectedNodeIndexLeftMouse = -1;
    private int selectedNodeIndexRightMouse = -1;
    private boolean isFirstClickOutside;
    boolean activateLabelVisibility = false;
    private Rectangle currentBoundingBox;
    private int flag_once = 0;
    private int prev_width = 500;
    private int prev_height = 500;
    public boolean isBBoxCancelled = false;
    private boolean isPolygonCancelled = false;
    private boolean isBBoxDragged = false;
    private Point referenceDragPos;
    private Point relativeDragPosFromTop = new Point();
    private boolean isPolygonDragged = false;
    private Point referencePolygonDragPos;
    private Point relativePolygonDragPosFromTop = new Point();
    private int img_width;
    private int img_height;
    private float zoom_factor = 1;
    public float original_zoom_factor = 1;
    public Rectangle selectionRect;
    public BufferedImage selectionRectImage;
    private int setImageCounter = 0;
    private Graphics2D gr2d;
    private int incrMouseX;
    private int incrMouseY;
    private double scale = 1.0;
    private Point zoomPoint = new Point(0, 0);
    public BufferedImage rawImage;
    private Point selectedSplinePoint;
    private ArrayList<PascalVocLane> splines = new ArrayList();

    public PanelPicture() {
        this.frame = new FrameImage();
        initialize();
        this.addMouseWheelListener(this);
    }

    /**
     * There exists two constructors
     *
     * @param frame
     */
    public PanelPicture(FrameImage frame) {
        this.frame = frame;
        initialize();
        this.addMouseWheelListener(this);
    }

    public PanelPicture(JFrame frame) {
        if (frame instanceof FrameImage) {
            this.frame = (FrameImage) frame;
        }
        initialize();
        this.addMouseWheelListener(this);
    }
    //***************************************************************************

    private void setImagePath(String path) {
        if (path == null || path.equals("")) {
            imagePath = path;
            return;
        }
        imagePath = path;
        File f = new File(imagePath);
        imageFolder = f.getParent();
        imageFiles = FactoryUtils.getFileArrayInFolderForImages(imageFolder);
        //imageFiles = sortFileListByNumber(imageFiles);
        fileName = f.getName();
        for (int i = 0; i < imageFiles.length; i++) {
            if (imageFiles[i].getName().equals(fileName)) {
                imageIndex = i;
                break;
            }
        }
        frame.imagePath = imagePath;
        String[] s = FactoryUtils.splitPath(imagePath);
        frame.titleImageInfo = (s[s.length - 1]);
    }

    public File[] sortFileListByNumber(File[] files) {
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                int n1 = extractNumber(o1.getName());
                int n2 = extractNumber(o2.getName());
                return n1 - n2;
            }

            private int extractNumber(String name) {
                int i = 0;
                try {
                    String number = name.split("_")[0];
                    i = Integer.parseInt(number);
                } catch (NumberFormatException e) {
                    i = 0;
                }
                return i;
            }
        });
        return files;

    }

    public void setImage(BufferedImage image, String imagePath, String caption, boolean isClearBbox) {
        if (setImageCounter++ > 50) {
            setImageCounter = 0;
            System.gc();
        }
        this.caption = caption;
        String folderName = FactoryUtils.getFolderPath(imagePath);
        currentFolderName = folderName;
        if ((activateBoundingBox || activatePolygon || activateLaneDetection) && imagePath != null && !imagePath.isEmpty()) {
            String fileName = FactoryUtils.getFileName(imagePath) + ".xml";
            xmlFileName = folderName + "/" + fileName;
            boolean checkXML = new File(folderName, fileName).exists();
            if (checkXML) {
                if (FactoryUtils.isFileExist(folderName + "/class_labels.txt")) {
                    mapBBoxColor = buildHashMap(folderName + "/class_labels.txt");
                }
                AnnotationPascalVOCFormat bb = FactoryUtils.deserializePascalVocXML(folderName + "/" + fileName);
                //if (isClearBbox && !isSeqenceVideoFrame) {
                if (isClearBbox) {
                    listPascalVocObject.clear();
                    listPascalVocObject = bb.lstObjects;
                }

                showRegion = true;
                //activateBoundingBox = true;
                source = bb.source;
            }
            if (activateLaneDetection) {
                readLanesFromTxt();
            }
        }
        currBufferedImage = ImageProcess.clone(image);
        originalBufferedImage = ImageProcess.clone(image);
        originalBufferedImageTemp = ImageProcess.clone(image);
        imgData = ImageProcess.bufferedImageToArray2D(currBufferedImage);
        if (activateStatistics) {
            currBufferedImage = ImageProcess.toGrayLevel(originalBufferedImage);
            imgData = ImageProcess.imageToPixelsFloat(currBufferedImage);
            stat = TStatistics.getStatistics(currBufferedImage);
        }
        zoom_factor = original_zoom_factor;

        updateImagePosition();

        repaint();
        if (!this.imagePath.equals(imagePath)) {
            setImagePath(imagePath);
        }
    }

    public void setImage(BufferedImage image, String imagePath, String caption) {
        setImage(image, imagePath, caption, true);
    }

    public void setImage(BufferedImage image) {
        setImage(image, "", "");
    }

    public void setZoomImage(BufferedImage image, String imagePath, String caption) {
        this.caption = caption;
        if (activateBoundingBox && imagePath != null && !imagePath.isEmpty()) {
            String fileName = FactoryUtils.getFileName(imagePath) + ".xml";
            String folderName = FactoryUtils.getFolderPath(imagePath);
            boolean checkXML = new File(folderName, fileName).exists();
//            if (checkXML) {
//                AnnotationPascalVOCFormat bb = FactoryUtils.deserializePascalVocXML(folderName + "/" + fileName);
//                listPascalVocObject.clear();
//                listPascalVocObject = bb.lstObjects;
//                showRegion = true;
//                source = bb.source;
//                activateBoundingBox = true;
//            }
        }

        currBufferedImage = image;
        imgData = ImageProcess.bufferedImageToArray2D(currBufferedImage);
        repaint();
        if (!this.imagePath.equals(imagePath)) {
            setImagePath(imagePath);
        }
    }

    public BufferedImage getImage() {
        return currBufferedImage;
    }

    public TStatistics getStatistics() {
        stat = TStatistics.getStatistics(currBufferedImage);
        return stat;
    }

    public void setFrame(FrameImage frm) {
        this.frame = frm;
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D gr = (Graphics2D) g;
        gr2d = gr;

        RenderingHints rh = new RenderingHints(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        gr.setRenderingHints(rh);

        if (flag_once == 0) {
            activateAutoSizeAspect = false;
            if (originalBufferedImage != null) {
                prev_width = originalBufferedImage.getWidth() + 300;
                prev_height = originalBufferedImage.getHeight() + 183;
            }
            flag_once = 1;
        }

        gr.setColor(Color.BLACK);
        gr.fillRect(0, 0, getWidth(), getHeight());
        gr.setColor(Color.GREEN);

        int wPanel = this.getWidth();
        int hPanel = this.getHeight();

        if (currBufferedImage != null) {
            img_width = (int) (0.8 * currBufferedImage.getWidth());
            img_height = (int) (0.8 * currBufferedImage.getHeight());

            updatePanelOffsetValuesWhileImageMoving();

            paintIfImageAutoSized(gr);

            gr.drawImage(currBufferedImage, fromLeft, fromTop, this);

            gr.setColor(Color.blue);
            gr.drawRect(fromLeft, fromTop, currBufferedImage.getWidth(), currBufferedImage.getHeight());

            if (activateHistogram && histFrame != null) {
                showHistogram();
            }
            if (activateStatistics && stat != null) {
                paintStatistics(gr);
            }
//            System.out.println("activateLaneDetection:"+activateLaneDetection);
//            System.out.println("showRegion:"+showRegion);

            if (showRegion) {
                if (activateBoundingBox) {
                    paintBoundingBoxes(gr);
                } else if (activatePolygon) {
                    paintPolygons(gr);
                } else if (activateLaneDetection) {
                    paintLaneSpline(gr);
                } else if (activateCrop && isCropStarted) {
                    paintCrop(gr);
                } else if (activateCrop && selectionRect != null) {
                    paintSelectionRect(gr);
                }
            }
            if (isMouseInCanvas()) {
                paintPixelInfo(gr, currBufferedImage.getWidth(), currBufferedImage.getHeight());
                paintMouseDashedLines(gr, wPanel, hPanel, colorDashedLine);
            }

        }
        paintFrameRectangle(gr, wPanel, hPanel);
        paintComponents(g);
    }

    public void updateObjectProperties(String ret, String objectType) {
        mapBBoxColor = buildHashMap(currentFolderName + "/class_labels.txt");
        if (objectType.equals("bbox")) {
            if (selectedBBox == null) {
                if (ret != null && !ret.split(":")[0].isEmpty()) {
                    lastSelectedClassName = ret.split(":")[0];
                    lastSelectedBoundingBoxColor = buildColor(ret.split(":")[1]);
                    isBBoxCancelled = false;
                } else {
                    isBBoxCancelled = true;
                    selectedBBox = null;
                    repaint();
                    return;
                }
                PascalVocBoundingBox bbox = new PascalVocBoundingBox(lastSelectedClassName, currentBoundingBox, 0, 0, lastSelectedBoundingBoxColor);
                selectedBBox = bbox;
                listPascalVocObject.add(new PascalVocObject(selectedBBox.name, "Unspecified", 0, 0, 0, selectedBBox, null, null));
                repaint();
                selectedBBox = null;
            } else {
                if (ret != null && !ret.split(":")[0].isEmpty()) {
                    lastSelectedClassName = ret.split(":")[0];
                    lastSelectedBoundingBoxColor = buildColor(ret.split(":")[1]);
                    selectedPascalVocObject.name = lastSelectedClassName;
                    isBBoxCancelled = false;
                    selectedBBox.name = lastSelectedClassName;
                    lastSelectedClassName = ret.split(":")[0];
                    lastSelectedBoundingBoxColor = buildColor(ret.split(":")[1]);
                } else {
                    isBBoxCancelled = true;
                }
            }
        } else if (objectType.equals("polygon")) {
            if (ret == null) {
                return;
            }

            if (selectedPolygon != null && !(lastSelectedClassName == null || lastSelectedClassName.isEmpty())) {
                lastSelectedClassName = ret.split(":")[0];
                lastSelectedPolygonColor = buildColor(ret.split(":")[1]);
                isPolygonCancelled = false;
                selectedPolygon.name = lastSelectedClassName;
                selectedPolygon.color = lastSelectedPolygonColor;
                for (PascalVocObject pvo : listPascalVocObject) {
                    if (pvo.polygonContainer.equals(selectedPolygon)) {
                        pvo.name = lastSelectedClassName;
                        if (pvo.bndbox != null) {
                            pvo.bndbox.name = pvo.name;
                        }
                    }

                }
                repaint();
            } else if (lastSelectedClassName == null || lastSelectedClassName.isEmpty()) {
                if (!ret.split(":")[0].isEmpty()) {
                    lastSelectedClassName = ret.split(":")[0];
                    lastSelectedPolygonColor = buildColor(ret.split(":")[1]);
                    Polygon pol = FactoryUtils.clone(polygon);
                    pol = unScaleWithZoomFactor(pol);
                    PascalVocPolygon poly = new PascalVocPolygon(lastSelectedClassName, pol, 0, 0, lastSelectedPolygonColor);
                    selectedPolygon = poly;
                    listPascalVocObject.add(new PascalVocObject(selectedPolygon.name, "Unspecified", 0, 0, 0, null, selectedPolygon, null));
                } else {
                    isPolygonCancelled = true;
                }
            } else {
                Polygon pol = FactoryUtils.clone(polygon);
                pol = unScaleWithZoomFactor(pol);
                PascalVocPolygon poly = new PascalVocPolygon(lastSelectedClassName, pol, 0, 0, lastSelectedPolygonColor);
                selectedPolygon = poly;
                listPascalVocObject.add(new PascalVocObject(selectedPolygon.name, "Unspecified", 0, 0, 0, null, selectedPolygon, null));

            }
            polygon.reset();
            isPolygonPressed = false;
            isFirstClickOutside = true;
            selectedPolygon = null;
            repaint();

        }
    }

    private String setBoundingBoxProperties(String className) {
        MyDialog dlg = new MyDialog(frame, imageFolder, className);
        String results = dlg.run();
        return results;
    }

    private Color buildColor(String str) {
        String[] s = str.split(" ");
        int r = Integer.parseInt(s[1]);
        int g = Integer.parseInt(s[2]);
        int b = Integer.parseInt(s[3]);
        Color ret = new Color(r, g, b);
        return ret;
    }

    private void initialize() {

        activateCrop = true;
        addKeyListener(this);
        setFocusable(true);
        requestFocus();

        ItemHandler handler = new ItemHandler();
        String[] elements = {
            "Next Image",
            "Prev Image",
            "Clone",
            "Load Image",
            "Save Image",
            "Screen Capture",
            "Statistics",
            "Histogram",
            "Revert",
            "Binarize",
            "Original",
            "Gray",
            "HSV",
            "Red",
            "Green",
            "Blue",
            "Edge",
            "Equalize",
            "Smooth",
            "Sharpen",
            "Crop",
            "Resize Images",
            "Convert VOC XML to Yolo",
            "Build JSON as TuSimple"
        };

        ButtonGroup itemsGroup = new ButtonGroup();
        items = new JRadioButtonMenuItem[elements.length];

        // construct each menu item and add to popup menu; also  
        // enable event handling for each menu item  
        for (int i = 0; i < elements.length; i++) {
            items[i] = new JRadioButtonMenuItem(elements[i]);
            popupMenu.add(items[i]);
            itemsGroup.add(items[i]);
            items[i].addActionListener(handler);
        }
        setComponentPopupMenu(null);

        this.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        this.updateUI();

        addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateImagePosition();
                repaint();
            }

            @Override
            public void componentMoved(ComponentEvent e) {
            }

            @Override
            public void componentShown(ComponentEvent e) {
            }

            @Override
            public void componentHidden(ComponentEvent e) {
            }
        });

        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && !e.isConsumed()) {
                    e.consume();
                    if (activateBoundingBox && selectedBBox != null) {
                        new FrameObjectProperties(frame, selectedBBox.name, "bbox").setVisible(true);
                    } else if (activatePolygon && selectedPolygon != null) {
                        new FrameObjectProperties(frame, selectedPolygon.name, "polygon").setVisible(true);
                    } else if (activateLaneDetection && selectedLane != null) {
                        //new FrameObjectProperties(frame, selectedLane.name, "lane").setVisible(true);
                        String laneClass = FactoryUtils.inputMessage("Set lane class index", "write numeric value from 1 to 5");
                        int laneClassIndex = Integer.parseInt(laneClass);
                        if (laneClassIndex >= 1 && laneClassIndex <= 5) {
                            PascalVocLane tempLane = selectedLane.clone();
                            tempLane.name = laneClass;
                            tempLane.color = FactoryUtils.getColorForLaneDetection(laneClassIndex);
                            if (!tempLane.spline.isEmpty()) {
                                splines.add(tempLane);
                                selectedLane.spline.clear();
                            }
                        }
                    } else {
                        currBufferedImage = ImageProcess.clone(originalBufferedImage);
                        adjustImageToPanel(currBufferedImage, false);
                        polygon.reset();
                        polygon = new Polygon();
                        repaint();
                    }
                } else if (e.getClickCount() == 1 && !e.isConsumed()) {
                    e.consume();
                    if (activatePolygon && selectedPolygon != null) {
                        Point p = new Point(e.getPoint().x - fromLeft, e.getPoint().y - fromTop);
                        int node_index = isClickedOnPolygonEdge(selectedPolygon.polygon, p);
                        if (node_index != -1) {
                            Point point = constraintMousePosition(e);
                            point.x = unScaleWithZoomFactor(point.x - fromLeft);
                            point.y = unScaleWithZoomFactor(point.y - fromTop);
                            insertPointOnPolygonAt(selectedPolygon.polygon, point, node_index);
                            repaint();
                        }
                    } else if (activateLaneDetection && e.getButton()==MouseEvent.BUTTON1) {
                        if (selectedLane == null) {
                            selectedLane = new PascalVocLane("", new ArrayList<Point>(), Color.blue);
                        }
                        selectedLane.spline.add(e.getPoint());
                        showRegion = true;
                        repaint();
                    }
                }
            }

            public void mousePressed(java.awt.event.MouseEvent e) {
                requestFocus();
                /**
                 * eğer mouse orta tekerleğine bastıysa zoom edilmiş imge
                 * hareket ettirilmek isteniyorsa
                 */
                if (e.getButton() == MouseEvent.BUTTON2) {
                    if (currBufferedImage.getWidth() > getWidth() || currBufferedImage.getHeight() > getHeight()) {
                        isMouseDraggedForImageMovement = true;
                        referenceMousePositionForImageMovement = FactoryUtils.clone(e.getPoint());
                        currentMousePositionForImageMovement = FactoryUtils.clone(e.getPoint());
                        setCursor(new Cursor(Cursor.MOVE_CURSOR));
                    } else {
                        isMouseDraggedForImageMovement = false;
                    }
                }

                mousePosTopLeft = constraintMousePosition(e);

                /**
                 * eğer annotation checkboxları seçilmeden resmin bir bölgesi
                 * seçilmek isteniyorsa
                 */
                if (activateCrop && e.getButton() == MouseEvent.BUTTON1) {
                    showRegion = true;
                    //isCropStarted = true;
                    mousePosTopLeft = constraintMousePosition(e);
                    selectionRect = new Rectangle(mousePosTopLeft.x, mousePosTopLeft.y, 0, 0);
                    //repaint();
                    return;
                }
                
                /**
                 * eğer activateLaneDetection ise ve mouse sağ tuşuna basıldıysa o anki lane işini iptal et
                 */
                if (activateLaneDetection && e.getButton()==MouseEvent.BUTTON3) {
                    selectedLane.spline.clear();                    
                }
                
                /**
                 * eğer bbox annoation yapılmak isteniyorsa
                 */
                if (activateBoundingBox && e.getButton() == MouseEvent.BUTTON1) {
                    showRegion = true;
                    mousePosTopLeft = constraintMousePosition(e);
                    if (!isBBoxDragged) {
                        //selectedBBox = isMouseClickedOnBoundingBox();
                    }
                    if (selectedBBox != null) {
                        Point p = new Point(e.getPoint().x - fromLeft, e.getPoint().y - fromTop);
                        //Point p = e.getPoint();
                        isBBoxResizeTopLeft = false;
                        isBBoxResizeTopRight = false;
                        isBBoxResizeBottomLeft = false;
                        isBBoxResizeBottomRight = false;
                        int t = 4;
                        if (p.x > scaleWithZoomFactor(selectedBBox.xmin) - t && p.x < scaleWithZoomFactor(selectedBBox.xmin) + t && p.y > scaleWithZoomFactor(selectedBBox.ymin) - t && p.y < scaleWithZoomFactor(selectedBBox.ymin) + t) {
                            isBBoxResizeTopLeft = true;
                        } else if (p.x > scaleWithZoomFactor(selectedBBox.xmax) - t && p.x < scaleWithZoomFactor(selectedBBox.xmax) + t && p.y > scaleWithZoomFactor(selectedBBox.ymin) - t && p.y < scaleWithZoomFactor(selectedBBox.ymin) + t) {
                            isBBoxResizeTopRight = true;
                        } else if (p.x > scaleWithZoomFactor(selectedBBox.xmax) - t && p.x < scaleWithZoomFactor(selectedBBox.xmax) + t && p.y > scaleWithZoomFactor(selectedBBox.ymax) - t && p.y < scaleWithZoomFactor(selectedBBox.ymax) + t) {
                            isBBoxResizeBottomRight = true;
                        } else if (p.x > scaleWithZoomFactor(selectedBBox.xmin) - t && p.x < scaleWithZoomFactor(selectedBBox.xmin) + t && p.y > scaleWithZoomFactor(selectedBBox.ymax) - t && p.y < scaleWithZoomFactor(selectedBBox.ymax) + t) {
                            isBBoxResizeBottomLeft = true;
                        } else if (p.x > scaleWithZoomFactor(selectedBBox.xmin) && p.x < scaleWithZoomFactor(selectedBBox.xmax) && p.y > scaleWithZoomFactor(selectedBBox.ymin) && p.y < scaleWithZoomFactor(selectedBBox.ymax)) {
                            isBBoxDragged = true;
                            referenceDragPos = e.getPoint();
                            relativeDragPosFromTop.x = referenceDragPos.x - (scaleWithZoomFactor(selectedBBox.xmin) + fromLeft);
                            relativeDragPosFromTop.y = referenceDragPos.y - (scaleWithZoomFactor(selectedBBox.ymin) + fromTop);
                            //System.out.println("rel:"+relativeDragPosFromTop);
                        } else {
                            isBBoxDragged = false;
                        }
                    } else {
                        isBBoxCancelled = false;
                        isBBoxDragged = false;
                    }
                    /**
                     * eğer polygonal annoation yapılmak isteniyorsa
                     */
                } else if (activatePolygon) {
                    showRegion = true;
                    isPolygonPressed = true;
                    lastPolygonPoint = constraintMousePosition(e);
                    Point p = new Point(e.getPoint().x - fromLeft, e.getPoint().y - fromTop);
                    int t = 10;
                    if (polygon.npoints > 0) {
                        return;
                    }
                    selectedPolygon = getSelectedPolygon(constraintMousePosition(e));
                    if (selectedPolygon != null) {
                        int n = selectedPolygon.polygon.npoints;
                        Polygon poly = selectedPolygon.polygon;
                        if (e.getButton() == MouseEvent.BUTTON1) {
                            selectedNodeIndexLeftMouse = -1;
                            for (int i = 0; i < n; i++) {
                                if (p.x > scaleWithZoomFactor(poly.xpoints[i]) - t && p.x < scaleWithZoomFactor(poly.xpoints[i]) + t && p.y > scaleWithZoomFactor(poly.ypoints[i]) - t && p.y < scaleWithZoomFactor(poly.ypoints[i]) + t) {
                                    selectedNodeIndexLeftMouse = i;
                                    System.out.println(i + ".nodu tuttu");
                                    isPolygonDragged = false;
                                    return;
                                }
                            }
                        } else if (SwingUtilities.isRightMouseButton(e)) {
                            selectedNodeIndexRightMouse = -1;
                            for (int i = 0; i < n; i++) {
                                if (p.x > scaleWithZoomFactor(poly.xpoints[i]) - t && p.x < scaleWithZoomFactor(poly.xpoints[i]) + t && p.y > scaleWithZoomFactor(poly.ypoints[i]) - t && p.y < scaleWithZoomFactor(poly.ypoints[i]) + t) {
                                    selectedNodeIndexRightMouse = i;
                                    isPolygonDragged = false;
                                    return;
                                }
                            }
                        }
                        if (p.x > scaleWithZoomFactor(selectedPolygon.getXMin()) && p.x < scaleWithZoomFactor(selectedPolygon.getXMax()) && p.y > scaleWithZoomFactor(selectedPolygon.getYMin()) && p.y < scaleWithZoomFactor(selectedPolygon.getYMax())) {
                            isPolygonDragged = true;
                            referencePolygonDragPos = constraintMousePosition(e);
                            relativePolygonDragPosFromTop.x = referencePolygonDragPos.x - (scaleWithZoomFactor(selectedPolygon.getXMin()) + fromLeft);
                            relativePolygonDragPosFromTop.y = referencePolygonDragPos.y - (scaleWithZoomFactor(selectedPolygon.getYMin()) + fromTop);
                        } else {
                            isPolygonDragged = false;
                        }
                    } else {
                        isPolygonCancelled = false;
                        isPolygonDragged = false;
                    }

                } else if (activateLaneDetection) {
                    //eğer lane detection için splines tabanlı bir annotation yapılmak isteniyorsa
                    selectedSplinePoint = findSelectedSplinePoint(e.getPoint());
                }
                if (activatePolygon && isPolygonPressed && SwingUtilities.isRightMouseButton(e)) {
                    isCancelledPolygon = true;
                }
                repaint();
            }

            public void mouseReleased(java.awt.event.MouseEvent e) {

                if (e.getButton() == MouseEvent.BUTTON2) {
                    if (isMouseDraggedForImageMovement) {
                        isMouseDraggedForImageMovement = false;
                        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                        repaint();
                        return;
                    }
                }

                if (activateLaneDetection && e.getButton() == MouseEvent.BUTTON1) {
                    selectedSplinePoint = null;
                }

                if (activatePolygon && e.getButton() == MouseEvent.BUTTON1) {
                    Point p = constraintMousePosition(e);
                    if (!isPolygonDragged && selectedNodeIndexLeftMouse == -1) {
                        if (polygon.npoints == 0) {
                            selectedPolygon = getSelectedPolygon(p);
                        }

                        if (selectedPolygon != null) {
                            repaint();
                            return;
                        } else {
                            if (isFirstClickOutside) {
                                isFirstClickOutside = false;
                                repaint();
                                return;
                            }
                        }
                    }
                }

                if (activateCrop) {
                    isCropStarted = false;
                    if (selectionRect != null) {
                        if (selectionRect.width < 2 || selectionRect.height < 2) {
                            selectionRect = null;
                        } else {
                            Rectangle rect = new Rectangle(selectionRect.x - fromLeft, selectionRect.y - fromTop, selectionRect.width, selectionRect.height);
                            selectionRectImage = ImageProcess.cropImage(currBufferedImage, rect);
                        }
                    }
                    repaint();
                } else if (activatePolygon && polygon.npoints > 0 && SwingUtilities.isRightMouseButton(e)) {
                    isPolygonPressed = false;
                    isCancelledPolygon = false;
                    polygon.reset();
                    selectedPolygon = null;
                    lastPolygonPoint = null;
                    repaint();
                    return;

                } else if (activatePolygon && isPolygonPressed && SwingUtilities.isRightMouseButton(e)) {
                    if (selectedPolygon != null && selectedNodeIndexRightMouse != -1) {
                        setDefaultCursor();
                        removePointFromPolygon(selectedPolygon.polygon, selectedNodeIndexRightMouse);
                        repaint();
                        return;
                    }
                } else if (activatePolygon && isPolygonPressed && e.getButton() == MouseEvent.BUTTON1) {
                    setDefaultCursor();
                    mousePos = constraintMousePosition(e);

                    if (selectedPolygon != null && selectedNodeIndexLeftMouse != -1) {
                        setDefaultCursor();
                        mousePos = constraintMousePosition(e);
                        selectedPolygon.polygon.xpoints[selectedNodeIndexLeftMouse] = unScaleWithZoomFactorX(mousePos.x) - fromLeft;
                        selectedPolygon.polygon.ypoints[selectedNodeIndexLeftMouse] = unScaleWithZoomFactorY(mousePos.y) - fromTop;
                        repaint();
                        return;
                    }
                    if (!isPolygonDragged) {
                        Point p = constraintMousePosition(e);
                        if (isReleasedNearStartPolygon(p, polygon)) {
                            if (lastSelectedClassName == null || lastSelectedClassName.isEmpty()) {
                                new FrameObjectProperties(frame, null, "polygon").setVisible(true);
                                return;
                            } else {
                                Polygon pol = FactoryUtils.clone(polygon);
                                pol = unScaleWithZoomFactor(pol);
                                PascalVocPolygon poly = new PascalVocPolygon(lastSelectedClassName, pol, 0, 0, lastSelectedPolygonColor);
                                selectedPolygon = poly;
                                listPascalVocObject.add(new PascalVocObject(selectedPolygon.name, "Unspecified", 0, 0, 0, null, selectedPolygon, null));

                            }
                            polygon.reset();
                            isPolygonPressed = false;
                            isFirstClickOutside = true;
                            selectedPolygon = null;
                            repaint();
                            return;

                        } else {
                            polygon.addPoint(p.x, p.y);
                        }
                    }

                } else if (activateBoundingBox && e.getButton() == MouseEvent.BUTTON1) {
                    setDefaultCursor();
                    mousePos = constraintMousePosition(e);

                    //eğer bounding box noktaları resize edilmişse
                    if (isBBoxResizeTopLeft && selectedBBox != null) {
                        mousePosTopLeft = constraintMousePosition(e);
                        selectedBBox.xmin = unScaleWithZoomFactorX(mousePosTopLeft.x) - fromLeft;
                        selectedBBox.ymin = unScaleWithZoomFactorY(mousePosTopLeft.y) - fromTop;
                        isBBoxResizeTopLeft = false;
                        repaint();
                        return;
                    } else if (isBBoxResizeTopRight && selectedBBox != null) {
                        mousePosTopRight = constraintMousePosition(e);
                        selectedBBox.xmax = unScaleWithZoomFactorX(mousePosTopRight.x) - fromLeft;
                        selectedBBox.ymin = unScaleWithZoomFactorY(mousePosTopRight.y) - fromTop;
                        isBBoxResizeTopRight = false;
                        repaint();
                        return;
                    } else if (isBBoxResizeBottomLeft && selectedBBox != null) {
                        mousePosBottomLeft = constraintMousePosition(e);
                        selectedBBox.xmin = unScaleWithZoomFactorX(mousePosBottomLeft.x) - fromLeft;
                        selectedBBox.ymax = unScaleWithZoomFactorY(mousePosBottomLeft.y) - fromTop;
                        isBBoxResizeBottomLeft = false;
                        repaint();
                        return;
                    } else if (isBBoxResizeBottomRight && selectedBBox != null) {
                        mousePosBottomRight = constraintMousePosition(e);
                        selectedBBox.xmax = unScaleWithZoomFactorX(mousePosBottomRight.x) - fromLeft;
                        selectedBBox.ymax = unScaleWithZoomFactorY(mousePosBottomRight.y) - fromTop;
                        isBBoxResizeBottomRight = false;
                        repaint();
                        return;
                    }

                    mousePosBottomRight = constraintMousePosition(e);
                    if (!FactoryUtils.isMousePosEqual(mousePosTopLeft, mousePosBottomRight)) {
                        if (isBBoxDragged) {
                            updateSelectedBBoxPosition();
                            isBBoxDragged = false;
                            repaint();
                            return;
                        }
                        int w = unScaleWithZoomFactor(Math.abs(mousePosTopLeft.x - mousePosBottomRight.x));
                        int h = unScaleWithZoomFactor(Math.abs(mousePosTopLeft.y - mousePosBottomRight.y));
                        if (w < 5 || h < 5) {
                            return;
                        }
                        currentBoundingBox = new Rectangle(unScaleWithZoomFactor(mousePosTopLeft.x - fromLeft), unScaleWithZoomFactor(mousePosTopLeft.y - fromTop), w, h);

                        if (lastSelectedClassName == null || lastSelectedClassName.isEmpty()) {
                            new FrameObjectProperties(frame, null, "bbox").setVisible(true);
                            return;
                        }

                        if (selectedBBox == null) {
                            Rectangle r = new Rectangle(unScaleWithZoomFactor(mousePosTopLeft.x - fromLeft), unScaleWithZoomFactor(mousePosTopLeft.y - fromTop), w, h);
                            PascalVocBoundingBox bbox = new PascalVocBoundingBox(lastSelectedClassName, r, 0, 0, lastSelectedBoundingBoxColor);
                            selectedBBox = bbox;
                            listPascalVocObject.add(new PascalVocObject(selectedBBox.name, "Unspecified", 0, 0, 0, selectedBBox, null, null));
                            repaint();
                            selectedBBox = null;
                        }

                        return;
                    }
                    if (!isBBoxDragged) {
                        selectedBBox = isMouseClickedOnBoundingBox();
                        repaint();
                        return;
                    }
                    repaint();
                } else if (activateLaneDetection && e.getButton() == MouseEvent.BUTTON1) {
                    setDefaultCursor();
                    mousePos = constraintMousePosition(e);
                    selectedSplinePoint = null;
                }

                checkForTriggerEvent(e);

            }

            private void checkForTriggerEvent(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }

            private PascalVocBoundingBox isMouseClickedOnBoundingBox() {
                PascalVocBoundingBox ret = null;
                Point relativeMousePos = new Point(mousePos.x - fromLeft, mousePos.y - fromTop);
                //System.out.println("scaledMousePos = " + scaledMousePos);
                if (listPascalVocObject.size() == 0) {
                    return null;
                } else {
                    for (PascalVocObject obj : listPascalVocObject) {
                        PascalVocBoundingBox bbox = obj.bndbox;
                        if (FactoryUtils.isPointInROI(relativeMousePos, scaleWithZoomFactor(bbox.getRectangle(5)))) {
                            ret = bbox;
                            selectedPascalVocObject = obj;
                            return ret;
                        }
                    }
                    return ret;
                }
            }

        });

        this.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {

            public void mouseMoved(java.awt.event.MouseEvent e) {
                isMouseDraggedForPolygonMovement = isMouseDraggedForBoundingBoxMovement = false;
                mousePos = e.getPoint();
                if (activateBoundingBox && selectedBBox != null) {
                    Point p = new Point(e.getPoint().x - fromLeft, e.getPoint().y - fromTop);
                    int t = 4;
                    if (p.x > scaleWithZoomFactor(selectedBBox.xmin) - t && p.x < scaleWithZoomFactor(selectedBBox.xmin) + t && p.y > scaleWithZoomFactor(selectedBBox.ymin) - t && p.y < scaleWithZoomFactor(selectedBBox.ymin) + t) {
                        setCursor(new Cursor(Cursor.NW_RESIZE_CURSOR));
                    } else if (p.x > scaleWithZoomFactor(selectedBBox.xmax) - t && p.x < scaleWithZoomFactor(selectedBBox.xmax) + t && p.y > scaleWithZoomFactor(selectedBBox.ymin) - t && p.y < scaleWithZoomFactor(selectedBBox.ymin) + t) {
                        setCursor(new Cursor(Cursor.NE_RESIZE_CURSOR));
                    } else if (p.x > scaleWithZoomFactor(selectedBBox.xmax) - t && p.x < scaleWithZoomFactor(selectedBBox.xmax) + t && p.y > scaleWithZoomFactor(selectedBBox.ymax) - t && p.y < scaleWithZoomFactor(selectedBBox.ymax) + t) {
                        setCursor(new Cursor(Cursor.SE_RESIZE_CURSOR));
                    } else if (p.x > scaleWithZoomFactor(selectedBBox.xmin) - t && p.x < scaleWithZoomFactor(selectedBBox.xmin) + t && p.y > scaleWithZoomFactor(selectedBBox.ymax) - t && p.y < scaleWithZoomFactor(selectedBBox.ymax) + t) {
                        setCursor(new Cursor(Cursor.SW_RESIZE_CURSOR));
                    } else if (p.x > scaleWithZoomFactor(selectedBBox.xmin) && p.x < scaleWithZoomFactor(selectedBBox.xmax) && p.y > scaleWithZoomFactor(selectedBBox.ymin) && p.y < scaleWithZoomFactor(selectedBBox.ymax)) {
                        setCursor(new Cursor(Cursor.MOVE_CURSOR));
                    } else {
                        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    }
                } else if (activatePolygon && selectedPolygon != null) {
                    Point p = new Point(e.getPoint().x - fromLeft, e.getPoint().y - fromTop);
                    int t = 10;
                    int n = selectedPolygon.polygon.npoints;
                    Polygon poly = selectedPolygon.polygon;
                    if (poly.contains(p)) {
                        setCursor(new Cursor(Cursor.MOVE_CURSOR));
                    } else {
                        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                    }
                    for (int i = 0; i < n; i++) {
                        if (p.x > scaleWithZoomFactor(poly.xpoints[i]) - t && p.x < scaleWithZoomFactor(poly.xpoints[i]) + t && p.y > scaleWithZoomFactor(poly.ypoints[i]) - t && p.y < scaleWithZoomFactor(poly.ypoints[i]) + t) {
                            setCursor(new Cursor(Cursor.HAND_CURSOR));
                        }
                    }
                }
                repaint();
            }

            public void mouseDragged(java.awt.event.MouseEvent e) {
                incrMouseX = constraintMousePosition(e).x - mousePos.x;
                incrMouseY = constraintMousePosition(e).y - mousePos.y;
                mousePos = constraintMousePosition(e);
                if (activateLaneDetection) {
                    if (selectedSplinePoint != null) {
                        selectedSplinePoint.setLocation(e.getPoint());
                        repaint();
                    }
                }
                if (activateBoundingBox || activateCrop || activatePolygon) {
                    if (SwingUtilities.isLeftMouseButton(e)) {
                        isMouseDraggedForPolygonMovement = isMouseDraggedForBoundingBoxMovement = true;
                        if (activateCrop) {
                            mousePosBottomRight = constraintMousePosition(e);
                            isCropStarted = true;
                            int width = 0;
                            int height = 0;
                            if (mousePosBottomRight.x > mousePosTopLeft.x) {
                                width = mousePosBottomRight.x - mousePosTopLeft.x;
                                if (mousePosBottomRight.y > mousePosTopLeft.y) {
                                    height = mousePosBottomRight.y - mousePosTopLeft.y;
                                } else {
                                    height = mousePosTopLeft.y - mousePosBottomRight.y;
                                    selectionRect.y += incrMouseY;
                                }
                                selectionRect.setSize(width, height);
                                repaint();
                                return;
                            } else {
                                width = mousePosTopLeft.x - mousePosBottomRight.x;
                                selectionRect.x += incrMouseX;
                                if (mousePosBottomRight.y > mousePosTopLeft.y) {
                                    height = mousePosBottomRight.y - mousePosTopLeft.y;
                                } else {
                                    height = mousePosTopLeft.y - mousePosBottomRight.y;
                                    selectionRect.y += incrMouseY;
                                }
                                selectionRect.setSize(width, height);
                                repaint();
                                return;
                            }
                        }
                    }
                } else if (activatePolygon && isPolygonPressed && selectedPolygon == null) {
                    //setDefaultCursor();
                    mousePos = constraintMousePosition(e);
                }

                if (SwingUtilities.isMiddleMouseButton(e)) {
                    if (currBufferedImage.getWidth() > getWidth() || currBufferedImage.getHeight() > getHeight()) {
                        isMouseDraggedForImageMovement = true;
                        currentMousePositionForImageMovement = FactoryUtils.clone(e.getPoint());
                        //System.out.println("image b. hareket ediyor");
                    }
                }
                repaint();
            }
        }
        );
    }

    private Point findSelectedSplinePoint(Point mousePoint) {
        for (PascalVocLane lane : splines) {
            for (Point point : lane.spline) {
                if (point.distance(mousePoint) <= 5) { // Assuming a point is selected if it's within 5 pixels
                    return point;
                }
            }
        }
        return null;
    }

    public void setMatrixData(float[][] data) {
        currBufferedImage = ImageProcess.pixelsToImageGray(data);
        imgData = ImageProcess.bufferedImageToArray2D(currBufferedImage);
        repaint();
    }

    public void setMatrixData(float[][][] data) {
        currBufferedImage = ImageProcess.pixelsToImageColorArgbFormat(data);
        imgData = ImageProcess.bufferedImageToArray2D(currBufferedImage);
        repaint();
    }

    private void removePointFromPolygon(Polygon poly, int index) {
        List<Integer> xList = new ArrayList();
        List<Integer> yList = new ArrayList();
        int n = poly.npoints;
        for (int i = 0; i < n; i++) {
            xList.add(poly.xpoints[i]);
            yList.add(poly.ypoints[i]);
        }
        xList.remove(index);
        yList.remove(index);
        poly.reset();
        for (int i = 0; i < n - 1; i++) {
            poly.addPoint(xList.get(i), yList.get(i));
        }

    }

    private void insertPointOnPolygonAt(Polygon poly, Point p, int index) {
        List<Integer> xList = new ArrayList();
        List<Integer> yList = new ArrayList();
        int n = poly.npoints;
        for (int i = 0; i < n; i++) {
            xList.add(poly.xpoints[i]);
            yList.add(poly.ypoints[i]);
        }
        xList.add(index + 1, p.x);
        yList.add(index + 1, p.y);
        poly.reset();
        for (int i = 0; i < n + 1; i++) {
            poly.addPoint(xList.get(i), yList.get(i));
        }
    }

    public Point getRelativeSelectedRectangleTopLeft() {
        return new Point(selectionRect.x - fromLeft, selectionRect.y - fromTop);
    }

    private int isClickedOnPolygonEdge(Polygon poly, Point p) {
        int n = poly.npoints;
        int x_from = 0;
        int y_from = 0;
        int x_to = 0;
        int y_to = 0;
        for (int i = 1; i <= n; i++) {
            x_from = scaleWithZoomFactor(poly.xpoints[i - 1]);
            y_from = scaleWithZoomFactor(poly.ypoints[i - 1]);
            if (i < n) {
                x_to = scaleWithZoomFactor(poly.xpoints[i]);
                y_to = scaleWithZoomFactor(poly.ypoints[i]);
            } else {
                x_to = scaleWithZoomFactor(poly.xpoints[0]);
                y_to = scaleWithZoomFactor(poly.ypoints[0]);
            }

            Line2D line = new Line2D.Double(x_from, y_from, x_to, y_to);
            if (line.ptSegDist(p) <= 3) {
                return i - 1;
            }
        }
        return -1;
    }

    private PascalVocPolygon getSelectedPolygon(Point mp) {
        selectedPolygon = null;
        for (PascalVocObject pvo : listPascalVocObject) {
            if (pvo.polygonContainer == null) {
                continue;
            }
            Polygon poly = scaleWithZoomFactor(FactoryUtils.clone(pvo.polygonContainer.polygon));
            if (poly.contains(mp)) {
                selectedPolygon = pvo.polygonContainer;
            }
        }
        if (selectedPolygon != null) {
            isFirstClickOutside = true;
        }
        return selectedPolygon;
    }

    private boolean isReleasedNearStartPolygon(Point p1, Polygon poly) {
        if (poly == null || poly.npoints <= 1) {
            return false;
        }
        Point p = new Point(poly.xpoints[0], poly.ypoints[0]);
        return p1.equals(p) || (Math.abs(p1.x - p.x) < 5 && Math.abs(p1.y - p.y) < 5);
    }

    private void cropImage() {
        Rectangle rect = new Rectangle(selectionRect.x - fromLeft, selectionRect.y - fromTop, selectionRect.width, selectionRect.height);
        BufferedImage bf = ImageProcess.cropImage(currBufferedImage, rect);
        ImageProcess.saveImage(bf, imageFolder + "/cropped_image.jpg");
        new FrameImage(CMatrix.getInstance(bf), imageFolder + "/cropped_image.jpg", "").setVisible(true);
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        float temp_zoom_factor = zoom_factor;
        temp_zoom_factor -= e.getWheelRotation() / 32.0f;
        if (temp_zoom_factor > 2.0) {
            return;
        }
        zoom_factor = temp_zoom_factor;
        float w = (1.0f * originalBufferedImage.getWidth() * zoom_factor);
        float h = (1.0f * originalBufferedImage.getHeight() * zoom_factor);
        if (original_zoom_factor < 1) {
            originalBufferedImage = ImageProcess.clone(rawImage);
            w = (1.0f * rawImage.getWidth() * zoom_factor);
            h = (1.0f * rawImage.getHeight() * zoom_factor);
        }
        frame.setZoomFactor(zoom_factor);

        currBufferedImage = ImageProcess.resizeAspectRatio(originalBufferedImage, (int) w, (int) h);
        setZoomImage(currBufferedImage, imagePath, caption);
        e.consume();
    }

    private void setCursorWith(Cursor cursor) {
        this.setCursor(cursor);
    }

    private void setDefaultCursor() {
        this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }

    private Point constraintMousePosition(MouseEvent e) {
        int frLeft = fromLeft;
        int frTop = fromTop;
        int imgW = currBufferedImage.getWidth();
        int imgH = currBufferedImage.getHeight();
        Point pp = e.getPoint();
        if (pp.x < frLeft) {
            pp.x = frLeft;
        }
        if (pp.x > frLeft + imgW) {
            pp.x = frLeft + imgW - 1;
        }
        if (pp.y < frTop) {
            pp.y = frTop;
        }
        if (pp.y > frTop + imgH) {
            pp.y = frTop + imgH - 1;
        }
        return pp;
    }

    private String getImageSize() {
        if (originalBufferedImage == null) {
            return "";
        }
        String str = "[" + currBufferedImage.getHeight() + "," + currBufferedImage.getWidth() + "] ";
        return str;
    }

    public void saveImage() {
        ImageProcess.imwrite(currBufferedImage);
    }

    public void saveImage(String fileName) {
        ImageProcess.imwrite(currBufferedImage, fileName);
    }

    private void drawNewBoundingBoxOnScreen(Graphics2D gr, int stroke, Point topLeft, Point currentMousePos, Color col) {
        if (FactoryUtils.isMousePosEqual(topLeft, currentMousePos)) {
            return;
        }
        gr.setColor(col);
        int sp1_x = topLeft.x;
        int sp1_y = topLeft.y;
        int sp2_x = currentMousePos.x;
        int sp2_y = currentMousePos.y;
        int w = 0;
        int h = 0;
        w = (Math.abs(sp2_x - sp1_x));
        h = (Math.abs(sp2_y - sp1_y));
        gr.setStroke(new BasicStroke(stroke));
        gr.setColor(col);
        gr.drawRect(sp1_x, sp1_y, w, h);
    }

    private void resizeSelectedBoundingBoxOnScreen(Graphics2D gr, PascalVocBoundingBox bbox, int stroke, Point p1, Point p2, Color col) {
        if (FactoryUtils.isMousePosEqual(p1, p2)) {
            return;
        }

        if (bbox != null) {
            gr.setColor(col);
            int sp1_x = (p1.x);
            int sp1_y = (p1.y);
            int sp2_x = (p2.x);
            int sp2_y = (p2.y);

            int width = gr.getFontMetrics().stringWidth(bbox.name) + 8;
            gr.fillRect(sp1_x - 1, sp1_y - 22, width, 22);

            gr.setStroke(new BasicStroke(stroke));
            gr.setColor(Color.BLACK);
            gr.setFont(new Font("Dialog", Font.BOLD, 12));
            gr.drawString(bbox.name, sp1_x + 2, sp1_y - 5);

            int w = (sp2_x - sp1_x);
            int h = (sp2_y - sp1_y);
            gr.setStroke(new BasicStroke(stroke));
            gr.setColor(col);
            gr.drawRect(sp1_x, sp1_y, w, h);

            if (bbox.equals(selectedBBox)) {
                //draw corners rectangles
                int wx = 12;
                int ww = 6;
                gr.setColor(Color.white);
                gr.fillRect(sp1_x - ww, sp1_y - ww, wx, wx);
                gr.fillRect(sp1_x - ww + w, sp1_y - ww, wx, wx);
                gr.fillRect(sp1_x - ww + w, sp1_y - ww + h, wx, wx);
                gr.fillRect(sp1_x - ww, sp1_y - ww + h, wx, wx);
                gr.setStroke(new BasicStroke(1));
                gr.setColor(Color.black);
                gr.drawRect(sp1_x - ww, sp1_y - ww, wx, wx);
                gr.drawRect(sp1_x - ww + w, sp1_y - ww, wx, wx);
                gr.drawRect(sp1_x - ww + w, sp1_y - ww + h, wx, wx);
                gr.drawRect(sp1_x - ww, sp1_y - ww + h, wx, wx);
            }
        }
    }

    private void draggedSelectedBoundingBoxOnScreen(Graphics2D gr, PascalVocBoundingBox bbox, int stroke, Point p1, Point p2, Color col) {
        if (FactoryUtils.isMousePosEqual(p1, p2)) {
            return;
        }

        if (bbox != null) {
            gr.setColor(col);
            int sp1_x = (p1.x);
            int sp1_y = (p1.y);

            int width = gr.getFontMetrics().stringWidth(bbox.name) + 8;
            gr.fillRect(sp1_x - 1, sp1_y - 22, width, 22);

            gr.setStroke(new BasicStroke(stroke));
            gr.setColor(Color.BLACK);
            gr.setFont(new Font("Dialog", Font.BOLD, 12));
            gr.drawString(bbox.name, sp1_x + 2, sp1_y - 5);
            int w = scaleWithZoomFactor(bbox.xmax - bbox.xmin);
            int h = scaleWithZoomFactor(bbox.ymax - bbox.ymin);
            gr.setStroke(new BasicStroke(stroke));
            gr.setColor(col);
            gr.drawRect(sp1_x, sp1_y, w, h);

            if (bbox.equals(selectedBBox)) {
                //draw corners rectangles
                int wx = 12;
                int ww = 6;
                gr.setColor(Color.white);
                gr.fillRect(sp1_x - ww, sp1_y - ww, wx, wx);
                gr.fillRect(sp1_x - ww + w, sp1_y - ww, wx, wx);
                gr.fillRect(sp1_x - ww + w, sp1_y - ww + h, wx, wx);
                gr.fillRect(sp1_x - ww, sp1_y - ww + h, wx, wx);
                gr.setStroke(new BasicStroke(1));
                gr.setColor(Color.black);
                gr.drawRect(sp1_x - ww, sp1_y - ww, wx, wx);
                gr.drawRect(sp1_x - ww + w, sp1_y - ww, wx, wx);
                gr.drawRect(sp1_x - ww + w, sp1_y - ww + h, wx, wx);
                gr.drawRect(sp1_x - ww, sp1_y - ww + h, wx, wx);
            }
        }
    }

    private void draggedSelectedPolygonOnScreen(Graphics2D gr, PascalVocPolygon poly, int stroke, Point p1, Point p2, Color col) {
        if (FactoryUtils.isMousePosEqual(p1, p2)) {
            return;
        }
        if (poly != null) {
            gr.setStroke(new BasicStroke(stroke));
            gr.setColor(col);

            int w = scaleWithZoomFactor(poly.getWidth());
            int h = scaleWithZoomFactor(poly.getHeight());

            int dx = p1.x - (scaleWithZoomFactor(selectedPolygon.getXMin()) + fromLeft);
            int dy = p1.y - (scaleWithZoomFactor(selectedPolygon.getYMin()) + fromTop);
            translateSelectedPolygonPosition(dx, dy);
        }
    }

    private void drawBoundingBoxOnImage(Graphics2D gr, PascalVocBoundingBox bbox, int stroke, Point p1, Point p2, Color col) {
        if (FactoryUtils.isMousePosEqual(p1, p2)) {
            return;
        }

        if (bbox != null) {
            gr.setColor(col);
            int sp1_x = scaleWithZoomFactorX(p1.x);
            int sp1_y = scaleWithZoomFactorY(p1.y);

            if (activateLabelVisibility) {
                int width = gr.getFontMetrics().stringWidth(bbox.name) + 8;
                gr.fillRect(sp1_x - 1, sp1_y - 22, width, 22);
                gr.setStroke(new BasicStroke(stroke));
                gr.setColor(Color.BLACK);
                gr.setFont(new Font("Dialog", Font.BOLD, 12));
                gr.drawString(bbox.name, sp1_x + 2, sp1_y - 5);
            }

            int w = 0;
            int h = 0;
            w = scaleWithZoomFactor(Math.abs(p2.x - p1.x));
            h = scaleWithZoomFactor(Math.abs(p2.y - p1.y));
            gr.setStroke(new BasicStroke(stroke));
            gr.setColor(col);
            gr.drawRect(sp1_x, sp1_y, w, h);

            if (bbox.equals(selectedBBox)) {
                //draw corners rectangles
                int wx = 8;
                int ww = 4;
                gr.setColor(Color.white);
                gr.fillRect(sp1_x - ww, sp1_y - ww, wx, wx);
                gr.fillRect(sp1_x - ww + w, sp1_y - ww, wx, wx);
                gr.fillRect(sp1_x - ww + w, sp1_y - ww + h, wx, wx);
                gr.fillRect(sp1_x - ww, sp1_y - ww + h, wx, wx);
                gr.setStroke(new BasicStroke(1));
                gr.setColor(Color.black);
                gr.drawRect(sp1_x - ww, sp1_y - ww, wx, wx);
                gr.drawRect(sp1_x - ww + w, sp1_y - ww, wx, wx);
                gr.drawRect(sp1_x - ww + w, sp1_y - ww + h, wx, wx);
                gr.drawRect(sp1_x - ww, sp1_y - ww + h, wx, wx);
            }
        }
    }

    private boolean isMouseInCanvas() {
        return (mousePos.x > fromLeft && mousePos.x < fromLeft + currBufferedImage.getWidth() && mousePos.y > fromTop && mousePos.y < fromTop + currBufferedImage.getHeight());
    }

    private void paintBoundingBoxes(Graphics2D gr) {
        if (isBBoxResizeTopLeft) {
            this.setCursor(new Cursor(Cursor.NW_RESIZE_CURSOR));
            mousePosTopLeft = FactoryUtils.clone(mousePos);
            Point p = new Point(scaleWithZoomFactorX(selectedBBox.xmax + fromLeft), scaleWithZoomFactorY(selectedBBox.ymax + fromTop));
            resizeSelectedBoundingBoxOnScreen(gr, selectedBBox, defaultStrokeWidth, mousePosTopLeft, p, mapBBoxColor.get(selectedBBox.name));
        } else if (isBBoxResizeTopRight) {
            this.setCursor(new Cursor(Cursor.NE_RESIZE_CURSOR));
            mousePosTopRight = FactoryUtils.clone(mousePos);
            Point p1 = new Point(scaleWithZoomFactorX(selectedBBox.xmin + fromLeft), mousePosTopRight.y);
            Point p2 = new Point(mousePosTopRight.x, scaleWithZoomFactorY(selectedBBox.ymax + fromTop));
            resizeSelectedBoundingBoxOnScreen(gr, selectedBBox, defaultStrokeWidth, p1, p2, mapBBoxColor.get(selectedBBox.name));
        } else if (isBBoxResizeBottomLeft) {
            this.setCursor(new Cursor(Cursor.SW_RESIZE_CURSOR));
            mousePosBottomLeft = FactoryUtils.clone(mousePos);
            Point p1 = new Point(mousePosBottomLeft.x, scaleWithZoomFactorY(selectedBBox.ymin + fromTop));
            Point p2 = new Point(scaleWithZoomFactorX(selectedBBox.xmax + fromLeft), mousePosBottomLeft.y);
            resizeSelectedBoundingBoxOnScreen(gr, selectedBBox, defaultStrokeWidth, p1, p2, mapBBoxColor.get(selectedBBox.name));
        } else if (isBBoxResizeBottomRight) {
            this.setCursor(new Cursor(Cursor.SE_RESIZE_CURSOR));
            mousePosBottomRight = FactoryUtils.clone(mousePos);
            Point p1 = new Point(scaleWithZoomFactorX(selectedBBox.xmin + fromLeft), scaleWithZoomFactorY(selectedBBox.ymin + fromTop));
            resizeSelectedBoundingBoxOnScreen(gr, selectedBBox, defaultStrokeWidth, p1, mousePosBottomRight, mapBBoxColor.get(selectedBBox.name));
        } else if (selectedBBox != null && isBBoxDragged && isMouseDraggedForBoundingBoxMovement) {
            //eğer bbox tutulup hareket ettiriliyorsa
            this.setCursor(new Cursor(Cursor.MOVE_CURSOR));
            lastPositionOfDraggedBBox = calculateDraggingBBoxPosition();
            //System.out.println(lastPositionOfDraggedBBox[0]);
            draggedSelectedBoundingBoxOnScreen(gr, selectedBBox, defaultStrokeWidth, lastPositionOfDraggedBBox[0], lastPositionOfDraggedBBox[1], Color.orange);
        } else if (selectedBBox == null && !isBBoxCancelled && isMouseDraggedForBoundingBoxMovement) {
            //şayet herhangi bir bbox seçilmeden ekranda yeni bir bbox çiziliyorsa
            drawNewBoundingBoxOnScreen(gr, defaultStrokeWidth, mousePosTopLeft, mousePos, defaultBoundingBoxColor);
        }

        //draw all bboxes including selectedbbox
        for (PascalVocObject obj : listPascalVocObject) {
            PascalVocBoundingBox bbox = obj.bndbox;
            Point p1 = new Point(bbox.xmin + fromLeft, bbox.ymin + fromTop);
            Point p2 = new Point(bbox.xmax + fromLeft, bbox.ymax + fromTop);
            drawBoundingBoxOnImage(gr, bbox, defaultStrokeWidth, p1, p2, mapBBoxColor.get(obj.name));
        }
    }

    private void paintPolygons(Graphics2D gr) {
        int w = 10;
        gr.setStroke(new BasicStroke(2));
        if (selectedPolygon != null && isPolygonDragged && isMouseDraggedForPolygonMovement) {
            //eğer bbox tutulup hareket ettiriliyorsa
            this.setCursor(new Cursor(Cursor.MOVE_CURSOR));
            lastPositionOfDraggedPolygon = calculateDraggingPolygonPosition();
            draggedSelectedPolygonOnScreen(gr, selectedPolygon, defaultStrokeWidth, lastPositionOfDraggedPolygon[0], lastPositionOfDraggedPolygon[1], Color.orange);
        }
        float[] cols = new float[4];

        for (PascalVocObject pvo : listPascalVocObject) {
            if (pvo.polygonContainer == null) {
                continue;
            }
            Polygon poly = scaleWithZoomFactor(pvo.polygonContainer.polygon);
            Color col = Color.yellow;
            if (mapBBoxColor.get(pvo.name) != null) {
                cols = mapBBoxColor.get(pvo.name).getRGBComponents(cols);
                col = new Color(cols[0], cols[1], cols[2], 0.35f);
            }

            gr.setColor(mapBBoxColor.get(pvo.name));
            gr.drawPolygon(poly);
            gr.setColor(col);
            gr.fillPolygon(poly);

            if (activateLabelVisibility) {
                gr.setColor(Color.white);
                Rectangle rect = poly.getBounds();
                int width = gr.getFontMetrics().stringWidth(pvo.name) + 8;
                gr.fillRect(rect.x + (rect.width - width) / 2, rect.y + (rect.height - 22) / 2, width, 22);
                gr.setColor(Color.BLACK);
                gr.drawRect(rect.x + (rect.width - width) / 2, rect.y + (rect.height - 22) / 2, width, 22);
                gr.setFont(new Font("Dialog", Font.BOLD, 12));
                gr.drawString(pvo.name, rect.x + (rect.width - width) / 2 + 3, rect.y + rect.height / 2 + 4);
            }
        }
        if (selectedPolygon != null) {
            drawSelectedPolygonNode(gr, scaleWithZoomFactor(selectedPolygon.polygon), w);
        }
        if (polygon.npoints > 0) {
            int n = polygon.npoints;
            gr.setColor(Color.green);
            gr.drawPolyline(polygon.xpoints, polygon.ypoints, polygon.npoints);
            gr.drawLine(polygon.xpoints[n - 1], polygon.ypoints[n - 1], mousePos.x, mousePos.y);
            boolean isNear = FactoryUtils.isNear(mousePos, new Point(polygon.xpoints[0], polygon.ypoints[0]), 5);
            if (isNear) {
                for (int i = 0; i < n; i++) {
                    gr.setColor(Color.red);
                    gr.fillOval(polygon.xpoints[i] - w / 2, polygon.ypoints[i] - w / 2, w, w);
                    gr.setColor(Color.green);
                    gr.drawOval(polygon.xpoints[i] - w / 2, polygon.ypoints[i] - w / 2, w, w);
                }
                w = 20;
                gr.setColor(Color.red);
                gr.fillOval(mousePos.x - w / 2, mousePos.y - w / 2, w, w);
                gr.setColor(Color.green);
                gr.drawOval(mousePos.x - w / 2, mousePos.y - w / 2, w, w);
            } else {
                for (int i = 0; i < n; i++) {
                    gr.fillOval(polygon.xpoints[i] - w / 2, polygon.ypoints[i] - w / 2, w, w);
                }
                gr.fillOval(mousePos.x - w / 2, mousePos.y - w / 2, w, w);
            }

        }

    }

    private void paintLaneSpline(Graphics2D gr) {
        //System.out.println("çiziyor");
        gr.setStroke(new BasicStroke(20, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (PascalVocLane lane : splines) {
            gr.setColor(lane.color);
            if (lane.spline.size() > 1) {
                drawSpline(gr, lane.spline);
            }

            // Draw clicked points
            gr.setColor(Color.RED);
            for (Point point : lane.spline) {
                gr.fillOval(point.x - 5, point.y - 5, 10, 10);
            }
        }

        if (selectedLane != null) {
            // Draw current spline
            gr.setColor(Color.LIGHT_GRAY);
            if (selectedLane.spline.size() > 1) {
                drawSpline(gr, selectedLane.spline);
            }

            // Draw clicked points for current spline
            gr.setColor(Color.RED);
            for (Point point : selectedLane.spline) {
                gr.fillOval(point.x - 5, point.y - 5, 10, 10);
            }
        }
    }

    private void drawSpline(Graphics2D g2d, ArrayList<Point> spline) {
        Path2D path = new Path2D.Double();
        path.moveTo(spline.get(0).getX(), spline.get(0).getY());

        for (int i = 0; i < spline.size() - 1; i++) {
            double xAvg = (spline.get(i).getX() + spline.get(i + 1).getX()) / 2;
            double yAvg = (spline.get(i).getY() + spline.get(i + 1).getY()) / 2;
            path.quadTo(spline.get(i).getX(), spline.get(i).getY(), xAvg, yAvg);
        }

        Point lastPoint = spline.get(spline.size() - 1);
        path.lineTo(lastPoint.getX(), lastPoint.getY());

        g2d.draw(path);
    }

    private void paintCrop(Graphics2D gr) {

        Stroke dashed = new BasicStroke(3,
                BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_BEVEL,
                0,
                new float[]{9},
                0);
        gr.setStroke(dashed);
        gr.setColor(colorDashedLine);
        //gr.setColor(Color.red);
        gr.drawRect(selectionRect.x, selectionRect.y, selectionRect.width, selectionRect.height);
        //System.out.println("dragged:"+selectionRect);
        gr.setStroke(new BasicStroke(1));
    }

    private void paintMouseDashedLines(Graphics2D gr, int wPanel, int hPanel, Color color) {
        Stroke dashed = new BasicStroke(3,
                BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_BEVEL,
                0,
                new float[]{9},
                0);
        gr.setStroke(dashed);
        gr.setColor(color);
        gr.drawLine(0, mousePos.y, wPanel - 1, mousePos.y);
        gr.drawLine(mousePos.x, 0, mousePos.x, hPanel - 1);
    }

    private void paintFrameRectangle(Graphics2D gr, int wPanel, int hPanel) {
        gr.setStroke(new BasicStroke(1));
        gr.setColor(Color.red);
        gr.drawRect(0, 0, wPanel - 1, hPanel - 1);
        gr.drawRect(1, 1, wPanel - 3, hPanel - 3);
    }

    private void paintStatistics(Graphics2D gr) {
        int sW = 150;
        int sH = 200;
        int sPX = this.getWidth() - sW - 5;
        int sPY = 5;
        int dh = 24;

        gr.setColor(Color.black);
        gr.fillRect(sPX, sPY, sW, sH);
        gr.setColor(Color.GREEN);
        gr.drawRect(sPX, sPY, sW, sH);
        sPX += 20;
        gr.drawString("Mean = " + (stat.mean), sPX, sPY += dh);
        gr.drawString("Std Dev = " + (stat.std), sPX, sPY += dh);
        gr.drawString("Entropy = " + (stat.entropy), sPX, sPY += dh);
        gr.drawString("Contrast = " + (stat.contrast), sPX, sPY += dh);
        gr.drawString("Kurtosis = " + (stat.kurtosis), sPX, sPY += dh);
        gr.drawString("Skewness = " + (stat.skewness), sPX, sPY += dh);
        gr.setColor(Color.ORANGE);
        gr.drawString("Ideal Exposure Score", sPX, sPY += dh);
        gr.drawString("= " + stat.adaptiveExposureScore, sPX + 60, sPY += dh);
    }

    private void showHistogram() {
        imgData = ImageProcess.imageToPixelsFloat(currBufferedImage);
        histFrame.setHistogramData(CMatrix.getInstance(imgData));
        histFrame.setVisible(true);
    }

    private void paintPixelInfo(Graphics2D gr, int wImg, int hImg) {
        if (lblShow && mousePos.x > fromLeft && mousePos.y > fromTop && mousePos.x < fromLeft + wImg && mousePos.y < fromTop + hImg) {
            p.x = mousePos.x - fromLeft;
            p.y = mousePos.y - fromTop;
            if (currBufferedImage.getType() == BufferedImage.TYPE_BYTE_GRAY) {
                frame.setPixelInfo(getImageSize() + " Pos=(" + p.y + ":" + p.x + ") Value=" + imgData[p.y][p.x] + " Img Type=TYPE_BYTE_GRAY");
            } else if (currBufferedImage.getType() == BufferedImage.TYPE_INT_RGB) {
                String s = "" + new Color((int) imgData[p.y][p.x], true);
                s = s.replace("java.awt.Color", "");
                frame.setPixelInfo(getImageSize() + " Pos=(" + p.y + ":" + p.x + ") Value=" + s + " Img Type=TYPE_INT_RGB");// + " RGB=" + "(" + r + "," + g + "," + b + ")");
            } else if (currBufferedImage.getType() == BufferedImage.TYPE_3BYTE_BGR) {
                String s = "" + new Color((int) imgData[p.y][p.x], true);
                s = s.replace("java.awt.Color", "");
                frame.setPixelInfo(getImageSize() + " Pos=(" + p.y + ":" + p.x + ") Value=" + s + " Img Type=TYPE_3BYTE_BGR");// + " RGB=" + "(" + r + "," + g + "," + b + ")");
            } else {
                String s = "" + new Color((int) imgData[p.y][p.x], true);
                s = s.replace("java.awt.Color", "");
                frame.setPixelInfo(getImageSize() + " Pos=(" + p.y + ":" + p.x + ") Value=" + s + " Img Type=" + currBufferedImage.getType());// + " RGB=" + "(" + r + "," + g + "," + b + ")");
            }
        }
    }

    private void paintIfImageAutoSized(Graphics2D gr) {
        if (activateAutoSize) {
            currBufferedImage = ImageProcess.resize(originalBufferedImage, this.getWidth() - 2 * panWidth, this.getHeight() - 2 * panWidth);
            imgData = ImageProcess.bufferedImageToArray2D(currBufferedImage);
            frame.setPixelInfo(getImageSize() + "X:Y");
        } else if (activateAutoSizeAspect) {
            if (this.getWidth() - 2 * panWidth > 5 && this.getHeight() - 2 * panWidth > 5) {
                currBufferedImage = ImageProcess.resizeAspectRatio(originalBufferedImageTemp, this.getWidth() - 2 * panWidth, this.getHeight() - 2 * panWidth);
                imgData = ImageProcess.bufferedImageToArray2D(currBufferedImage);
                frame.setPixelInfo(getImageSize() + "X:Y");

            }
        }
    }

    public Map<String, Color> buildHashMap(String path) {
        String[] s = FactoryUtils.readFile(path).split("\n");
        Map<String, Color> ret = new HashMap();
        for (String str : s) {
            String[] msg = str.split(":");
            ret.put(msg[0], buildColor(msg[1]));
        }
        return ret;
    }

    private BufferedImage adjustImageToPanel(BufferedImage bf, boolean isClearBbox) {
        if (bf.getHeight() > 950) {
            float zoom_factor = 950.0f / bf.getHeight();
            int w = (int) (bf.getWidth() * zoom_factor);
            int h = (int) (bf.getHeight() * zoom_factor);
            frame.setZoomFactor(FactoryUtils.formatFloat(zoom_factor, 4));
            original_zoom_factor = FactoryUtils.formatFloat(zoom_factor, 4);
            zoom_factor = original_zoom_factor;
            bf = ImageProcess.resizeAspectRatio(bf, w, h);
        } else {
            zoom_factor = original_zoom_factor = 1.0f;
            frame.setZoomFactor(FactoryUtils.formatFloat(zoom_factor, 4));
        }
        frame.titleImageInfo = (imageFiles[imageIndex].getName() + "      [ " + (imageIndex + 1) + " / " + imageFiles.length + " ]");
        fileName = imageFiles[imageIndex].getName();
        imagePath = imageFiles[imageIndex].getAbsolutePath();

        if (!isSeqenceVideoFrame) {
            if (isClearBbox) {
                listPascalVocObject.clear();
                selectedBBox = null;
            }
        }
        setImage(bf, imagePath, caption, isClearBbox);
        frame.img = bf;
        frame.imagePath = imagePath;
        frame.stretchFrame();
        return bf;
    }

    private int scaleWithZoomFactor(int val) {
        return (int) (val * zoom_factor);
    }

    private int unScaleWithZoomFactor(int val) {
        return (int) (val / zoom_factor);
    }

    private Polygon unScaleWithZoomFactor(Polygon p) {
        Polygon pol = new Polygon();
        int n = p.npoints;
        for (int i = 0; i < n; i++) {
            pol.addPoint(unScaleWithZoomFactor(p.xpoints[i] - fromLeft), unScaleWithZoomFactor(p.ypoints[i] - fromTop));
        }
        return pol;
    }

    private Polygon scaleWithZoomFactor(Polygon poly) {
        Polygon ret = new Polygon();
        for (int i = 0; i < poly.npoints; i++) {
            ret.addPoint(scaleWithZoomFactorX(poly.xpoints[i] + fromLeft), scaleWithZoomFactorY(poly.ypoints[i] + fromTop));
        }
        return ret;
    }

    private Point shiftPointTo(Point p, int valX, int valY) {
        return new Point(p.x + valX, p.y + valY);
    }

    private Point scaleWithZoomFactor(Point val) {
        return new Point(scaleWithZoomFactorX(val.x), scaleWithZoomFactorY(val.y));
    }

    private Point unScaleWithZoomFactor(Point val) {
        return new Point(unScaleWithZoomFactorX(val.x), unScaleWithZoomFactorY(val.y));
    }

    private Rectangle scaleWithZoomFactor(Rectangle p) {
        return new Rectangle(scaleWithZoomFactor(p.x), scaleWithZoomFactor(p.y), scaleWithZoomFactor(p.width), scaleWithZoomFactor(p.height));
    }

    private int scaleWithZoomFactorX(int val) {
        int pureVal = val - fromLeft;
        return Math.round(pureVal * zoom_factor) + fromLeft;
    }

    private int scaleWithZoomFactorY(int val) {
        int pureVal = val - fromTop;
        return Math.round(pureVal * zoom_factor) + fromTop;
    }

    private int unScaleWithZoomFactorX(int val) {
        int pureVal = val - fromLeft;
        return Math.round(pureVal / zoom_factor) + fromLeft;
    }

    private int unScaleWithZoomFactorY(int val) {
        int pureVal = val - fromTop;
        return Math.round(pureVal / zoom_factor) + fromTop;
    }

    private void updateSelectedBBoxPosition() {
        if (selectedBBox != null && lastPositionOfDraggedBBox != null) {
            int w = selectedBBox.xmax - selectedBBox.xmin;
            int h = selectedBBox.ymax - selectedBBox.ymin;
            selectedBBox.xmin = unScaleWithZoomFactorX(lastPositionOfDraggedBBox[0].x) - fromLeft;
            selectedBBox.ymin = unScaleWithZoomFactorY(lastPositionOfDraggedBBox[0].y) - fromTop;
            selectedBBox.xmax = selectedBBox.xmin + w;
            selectedBBox.ymax = selectedBBox.ymin + h;
        }
    }

    private void updateSelectedPolygonPosition(int dx, int dy) {
        if (selectedPolygon != null && lastPositionOfDraggedPolygon != null) {
            selectedPolygon.setLocationTopLeft(dx, dy);
        }
    }

    private void translateSelectedPolygonPosition(int dx, int dy) {
        if (selectedPolygon != null && lastPositionOfDraggedPolygon != null) {
            selectedPolygon.translate(dx, dy);
        }
    }

    private Point[] calculateDraggingBBoxPosition() {
        Point[] ret = new Point[2];
        int p1x = mousePos.x - relativeDragPosFromTop.x;
        p1x = (p1x > fromLeft) ? p1x : fromLeft;
        int p1y = mousePos.y - relativeDragPosFromTop.y;
        p1y = (p1y > fromTop) ? p1y : fromTop;
        int p2x = p1x + scaleWithZoomFactor(selectedBBox.xmax - selectedBBox.xmin);
        if (p2x > fromLeft + currBufferedImage.getWidth()) {
            p2x = fromLeft + currBufferedImage.getWidth();
            p1x = p2x - scaleWithZoomFactor(selectedBBox.getWidth());
        }
        int p2y = p1y + scaleWithZoomFactor(selectedBBox.ymax - selectedBBox.ymin);
        if (p2y > fromTop + currBufferedImage.getHeight()) {
            p2y = fromTop + currBufferedImage.getHeight();
            p1y = p2y - scaleWithZoomFactor(selectedBBox.getHeight());
        }
        Point p1 = new Point(p1x, p1y);
        Point p2 = new Point(p2x, p2y);
        ret[0] = p1;
        ret[1] = p2;
        return ret;
    }

    private Point[] calculateDraggingPolygonPosition() {
        Point[] ret = new Point[2];
        int p1x = mousePos.x - relativePolygonDragPosFromTop.x;
        p1x = (p1x > fromLeft) ? p1x : fromLeft;
        int p1y = mousePos.y - relativePolygonDragPosFromTop.y;
        p1y = (p1y > fromTop) ? p1y : fromTop;
        int p2x = p1x + scaleWithZoomFactor(selectedPolygon.getWidth());
        if (p2x > fromLeft + currBufferedImage.getWidth()) {
            p2x = fromLeft + currBufferedImage.getWidth();
            p1x = p2x - scaleWithZoomFactor(selectedPolygon.getWidth());
        }
        int p2y = p1y + scaleWithZoomFactor(selectedPolygon.getHeight());
        if (p2y > fromTop + currBufferedImage.getHeight()) {
            p2y = fromTop + currBufferedImage.getHeight();
            p1y = p2y - scaleWithZoomFactor(selectedPolygon.getHeight());
        }
        Point p1 = new Point(p1x, p1y);
        Point p2 = new Point(p2x, p2y);
        ret[0] = p1;
        ret[1] = p2;
        return ret;
    }

    private Point[] calculateDraggingBBoxPositionV3() {
        Point[] ret = new Point[2];
        int p1x = selectedBBox.xmin + fromLeft + (mousePos.x - referenceDragPos.x);
        p1x = (p1x > fromLeft) ? p1x : fromLeft;
        int p1y = selectedBBox.ymin + fromTop + (mousePos.y - referenceDragPos.y);
        p1y = (p1y > fromTop) ? p1y : fromTop;
        int p2x = p1x + (selectedBBox.xmax - selectedBBox.xmin);
        if (p2x > fromLeft + currBufferedImage.getWidth()) {
            p2x = fromLeft + currBufferedImage.getWidth();
            p1x = p2x - selectedBBox.getWidth();
        }
        int p2y = p1y + (selectedBBox.ymax - selectedBBox.ymin);
        if (p2y > fromTop + currBufferedImage.getHeight()) {
            p2y = fromTop + currBufferedImage.getHeight();
            p1y = p2y - selectedBBox.getHeight();
        }
        Point p1 = new Point(p1x, p1y);
        Point p2 = new Point(p2x, p2y);
        ret[0] = p1;
        ret[1] = p2;
        return ret;

    }

    private void updateImagePosition() {
        if (currBufferedImage != null) {
            int wPanel = this.getWidth();
            int hPanel = this.getHeight();

            int wImg = currBufferedImage.getWidth();
            int hImg = currBufferedImage.getHeight();

            fromLeft = (wPanel - wImg) / 2;
            fromTop = (hPanel - hImg) / 2;

        }
    }

    private void updatePanelOffsetValuesWhileImageMoving() {
        if (isMouseDraggedForImageMovement) {
            fromLeft += (currentMousePositionForImageMovement.x - referenceMousePositionForImageMovement.x);
            fromTop += (currentMousePositionForImageMovement.y - referenceMousePositionForImageMovement.y);
            referenceMousePositionForImageMovement = FactoryUtils.clone(currentMousePositionForImageMovement);
        }
    }

    public void setDashedLineColor() {
        colorDashedLine = JColorChooser.showDialog(null, "Choose Color for BoundingBox", colorDashedLine);
    }

    private void drawSelectedPolygonNode(Graphics2D gr, Polygon poly, int r) {
        int n = poly.npoints;
        gr.setColor(Color.green);
        gr.drawPolyline(poly.xpoints, poly.ypoints, poly.npoints);
        gr.drawLine(poly.xpoints[n - 1], poly.ypoints[n - 1], poly.xpoints[0], poly.ypoints[0]);
        for (int i = 0; i < n; i++) {
            gr.setColor(Color.red);
            gr.fillOval(poly.xpoints[i] - r / 2, poly.ypoints[i] - r / 2, r, r);
            gr.setColor(Color.green);
            gr.drawOval(poly.xpoints[i] - r / 2, poly.ypoints[i] - r / 2, r, r);
        }
    }

    public void updateImageLocation() {
        //System.out.println("burası tetiklendi");
        updateImagePosition();
    }

    private void paintSelectionRect(Graphics2D gr) {
        //System.out.println("mouse released:"+selectionRect);
        gr.setColor(new Color(255, 255, 255, 128)); // Semi-transparent white color
        gr.fill(selectionRect);
        gr.setColor(Color.RED);
        gr.setStroke(new BasicStroke(3));
        gr.draw(selectionRect);
        gr.setStroke(new BasicStroke(1));
    }

    private void saveLanesAsMask() {
        BufferedImage img = CMatrix.getInstance()
                .zeros(currBufferedImage.getHeight(), currBufferedImage.getWidth())                
                .getImage()
                ;
        Graphics2D gr=(Graphics2D)img.getGraphics();
        System.out.println("fromLeft:"+fromLeft);
        System.out.println("fromTop:"+fromTop);
        gr.setStroke(new BasicStroke(20, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (PascalVocLane lane : splines) {
            int col=Integer.parseInt(lane.name);
            gr.setColor(new Color(col,col,col));
            drawSpline(gr,shiftLanePos(lane.spline));
        }
        FactoryUtils.makeDirectory(imageFolder + "/seg_label");
        CMatrix.getInstance(img).imsave(imageFolder + "/seg_label/" + FactoryUtils.getFileName(fileName) + ".png");        
    }
    
    private ArrayList<Point> shiftLanePos(ArrayList<Point> spline){
        ArrayList<Point> ret=new ArrayList();
        for (Point p : spline) {
            ret.add(new Point(p.x-fromLeft,p.y-fromTop));
        }
        return ret;
    }

    private class ItemHandler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            // determine which menu item was selected  
            try {
                setDefaultValues();
                JRadioButtonMenuItem obj = (JRadioButtonMenuItem) e.getSource();
                if (obj.getText().equals("Next Image")) {
                    loadNextImage();
                    return;
                } else if (obj.getText().equals("Prev Image")) {
                    loadPrevImage();
                    return;
                } else if (obj.getText().equals("Clone")) {
                    new FrameImage(CMatrix.getInstance(currBufferedImage), imagePath, caption).setVisible(true);
                    return;
                } else if (obj.getText().equals("Screen Capture")) {
                    newScreenCaptureInstance();
                    return;
                } else if (obj.getText().equals("Load Image")) {
                    activateStatistics = false;
                    File fl = ImageProcess.readImageFileFromFolderWithDirectoryPath(imagePath);
                    if (fl == null) {
                        return;
                    }
                    BufferedImage bf = ImageProcess.readImageFromFile(fl.getAbsolutePath());
                    if (bf != null) {
                        originalBufferedImage = bf;
                        imagePath = fl.getAbsolutePath();
                        setImagePath(imagePath);
                        caption = imagePath;
                        setImage(bf, imagePath, caption, true);
                        frame.img = originalBufferedImage;

                    } else {
                        return;
                    }
                } else if (obj.getText().equals("Save Image")) {
                    ImageProcess.imwrite(currBufferedImage);
                } else if (obj.getText().equals("Histogram")) {
                    activateHistogram = true;
                    if (selectionRect != null) {
                        selectionRectImage = getCroppedImage();
                        CMatrix.getInstance(selectionRectImage).imhist();
                    } else {
                        CMatrix.getInstance(currBufferedImage).imhist();
                    }

                } else if (obj.getText().equals("Statistics")) {
                    activateStatistics = true;
                    currBufferedImage = ImageProcess.toGrayLevel(originalBufferedImage);
                    imgData = ImageProcess.bufferedImageToArray2D(currBufferedImage);
                    stat = TStatistics.getStatistics(currBufferedImage);
                } else if (obj.getText().equals("Original")) {
                    activateOriginal = true;
                    activateStatistics = false;
                    currBufferedImage = ImageProcess.clone(originalBufferedImage);
                    originalBufferedImageTemp = ImageProcess.clone(originalBufferedImage);
                    imgData = ImageProcess.bufferedImageToArray2D(currBufferedImage);
                    repaint();
                } else if (obj.getText().equals("Revert")) {
                    activateRevert = true;
                    if (selectionRect != null) {
                        selectionRectImage = getCroppedImage();
                        selectionRectImage = ImageProcess.revert(selectionRectImage);
                        currBufferedImage = ImageProcess
                                .overlayImage(currBufferedImage, selectionRectImage, new Point(selectionRect.x - fromLeft, selectionRect.y - fromTop), 1f);
                    } else {
                        currBufferedImage = ImageProcess.revert(currBufferedImage);
                    }
                    originalBufferedImageTemp = ImageProcess.clone(currBufferedImage);
                    imgData = ImageProcess.bufferedImageToArray2D(originalBufferedImageTemp);
                } else if (obj.getText().equals("Binarize")) {
                    activateBinarize = true;
                    if (selectionRect != null) {
                        selectionRectImage = getCroppedImage();
                        selectionRectImage = ImageProcess.binarizeColorImage(selectionRectImage);
                        selectionRectImage = ImageProcess.convertToBufferedImageTypes(selectionRectImage, currBufferedImage.getType());
                        currBufferedImage = ImageProcess
                                .overlayImage(currBufferedImage, selectionRectImage, new Point(selectionRect.x - fromLeft, selectionRect.y - fromTop), 0.5f);
                    } else {
                        if (currBufferedImage.getType() != 10) {
                            currBufferedImage = ImageProcess.rgb2gray(currBufferedImage);
                        }
                        currBufferedImage = ImageProcess.binarizeColorImage(currBufferedImage);
                    }
                    originalBufferedImageTemp = ImageProcess.clone(currBufferedImage);
                    imgData = ImageProcess.bufferedImageToArray2D(currBufferedImage);
                } else if (obj.getText().equals("Gray")) {
                    activateGray = true;
                    if (selectionRect != null) {
                        selectionRectImage = getCroppedImage();
                        selectionRectImage = ImageProcess.rgb2gray(selectionRectImage);
                        currBufferedImage = ImageProcess
                                .overlayImage(currBufferedImage, selectionRectImage, new Point(selectionRect.x - fromLeft, selectionRect.y - fromTop), 0.5f);
                    } else {
                        currBufferedImage = ImageProcess.rgb2gray(currBufferedImage);
                    }
                    originalBufferedImageTemp = ImageProcess.clone(currBufferedImage);
                    imgData = ImageProcess.bufferedImageToArray2D(currBufferedImage);
                } else if (obj.getText().equals("HSV")) {
                    activateHSV = true;
                    if (selectionRect != null) {
                        selectionRectImage = getCroppedImage();
                        selectionRectImage = ImageProcess.toHSVColorSpace(selectionRectImage);
                        currBufferedImage = ImageProcess
                                .overlayImage(currBufferedImage, selectionRectImage, new Point(selectionRect.x - fromLeft, selectionRect.y - fromTop), 0.5f);
                    } else {
                        currBufferedImage = ImageProcess.toHSVColorSpace(currBufferedImage);
                    }
                    originalBufferedImageTemp = ImageProcess.clone(currBufferedImage);
                    imgData = ImageProcess.bufferedImageToArray2D(currBufferedImage);
                } else if (obj.getText().equals("Red")) {
                    isRedChannel = true;
                    isBlueChannel = isGreenChannel = false;
                    activateRedChannel = true;
                    if (selectionRect != null) {
                        selectionRectImage = getCroppedImage();
                        selectionRectImage = ImageProcess.isolateChannel(selectionRectImage, "red");
                        currBufferedImage = ImageProcess
                                .overlayImage(currBufferedImage, selectionRectImage, new Point(selectionRect.x - fromLeft, selectionRect.y - fromTop), 0.5f);
                    } else {
                        currBufferedImage = ImageProcess.isolateChannel(currBufferedImage, "red");
                    }
                    originalBufferedImageTemp = ImageProcess.clone(currBufferedImage);
                    imgData = ImageProcess.bufferedImageToArray2D(currBufferedImage);
                } else if (obj.getText().equals("Green")) {
                    isGreenChannel = true;
                    isRedChannel = isBlueChannel = false;
                    activateGreenChannel = true;
                    if (selectionRect != null) {
                        selectionRectImage = getCroppedImage();
                        selectionRectImage = ImageProcess.isolateChannel(selectionRectImage, "green");
                        currBufferedImage = ImageProcess
                                .overlayImage(currBufferedImage, selectionRectImage, new Point(selectionRect.x - fromLeft, selectionRect.y - fromTop), 0.5f);
                    } else {
                        currBufferedImage = ImageProcess.isolateChannel(currBufferedImage, "green");
                    }
                    originalBufferedImageTemp = ImageProcess.clone(currBufferedImage);
                    imgData = ImageProcess.bufferedImageToArray2D(currBufferedImage);
                } else if (obj.getText().equals("Blue")) {
                    isBlueChannel = true;
                    isRedChannel = isGreenChannel = false;
                    activateBlueChannel = true;
                    if (selectionRect != null) {
                        selectionRectImage = getCroppedImage();
                        selectionRectImage = ImageProcess.isolateChannel(selectionRectImage, "blue");
                        currBufferedImage = ImageProcess
                                .overlayImage(currBufferedImage, selectionRectImage, new Point(selectionRect.x - fromLeft, selectionRect.y - fromTop), 0.5f);
                    } else {
                        currBufferedImage = ImageProcess.isolateChannel(currBufferedImage, "blue");
                    }
                    originalBufferedImageTemp = ImageProcess.clone(currBufferedImage);
                    imgData = ImageProcess.bufferedImageToArray2D(currBufferedImage);
                } else if (obj.getText().equals("Equalize")) {
                    activateEqualize = true;
                    if (selectionRect != null) {
                        selectionRectImage = getCroppedImage();
                        selectionRectImage = ImageProcess.equalizeHistogram(selectionRectImage);
                        currBufferedImage = ImageProcess
                                .overlayImage(currBufferedImage, selectionRectImage, new Point(selectionRect.x - fromLeft, selectionRect.y - fromTop), 0.5f);
                    } else {
                        currBufferedImage = ImageProcess.equalizeHistogram(ImageProcess.rgb2gray(currBufferedImage));
                    }
                    originalBufferedImageTemp = ImageProcess.clone(currBufferedImage);
                    imgData = ImageProcess.bufferedImageToArray2D(currBufferedImage);
                } else if (obj.getText().equals("Edge")) {
                    activateEdge = true;
                    if (selectionRect != null) {
                        selectionRectImage = getCroppedImage();
                        selectionRectImage = ImageProcess.edgeDetectionCanny(selectionRectImage, 0.3f, 1.0f, 2.5f, 3, false);
                        currBufferedImage = ImageProcess
                                .overlayImage(currBufferedImage, selectionRectImage, new Point(selectionRect.x - fromLeft, selectionRect.y - fromTop), 0.5f);
                    } else {
                        if (currBufferedImage.getType() != 10) {
                            currBufferedImage = ImageProcess.rgb2gray(currBufferedImage);
                        }
                        currBufferedImage = ImageProcess.edgeDetectionCanny(currBufferedImage, 0.3f, 1.0f, 2.5f, 3, false);
                    }
                    originalBufferedImageTemp = ImageProcess.clone(currBufferedImage);
                    imgData = ImageProcess.bufferedImageToArray2D(currBufferedImage);
                } else if (obj.getText().equals("Smooth")) {
                    if (selectionRect != null) {
                        selectionRectImage = getCroppedImage();
                        selectionRectImage = ImageProcess.filterGaussian(selectionRectImage, 3);
                        currBufferedImage = ImageProcess
                                .overlayImage(currBufferedImage, selectionRectImage, new Point(selectionRect.x - fromLeft, selectionRect.y - fromTop), 0.5f);
                    } else {
                        currBufferedImage = ImageProcess.filterGaussian(currBufferedImage, 3);
                    }
                    originalBufferedImageTemp = ImageProcess.clone(currBufferedImage);
                    imgData = ImageProcess.bufferedImageToArray2D(currBufferedImage);
                } else if (obj.getText().equals("AutoSize")) {
                    activateAutoSize = true;
                } else if (obj.getText().equals("Resize Images")) {
                    FrameResizeImages frm = new FrameResizeImages(imageFolder);
                    frm.setVisible(true);
                } else if (obj.getText().equals("AutoSizeAspect")) {
                    activateAutoSizeAspect = true;
                } else if (obj.getText().equals("Crop")) {
                    activateCrop = true;
                    cropImage();
                } else if (obj.getText().equals("Convert VOC XML to Yolo")) {
                    String subFolder = FactoryUtils.inputMessage("set subfolder name");
                    String msg = FactoryUtils.convertPascalVoc2YoloFormatBatch(imageFolder, subFolder, "detection");
                    FactoryUtils.showMessageTemp("Pascal VOC XMLs converted to Yolo format at " + msg, 3000, new CallBackTrigger() {
                        @Override
                        public void trigger() {
                        }
                    });
                } else if (activateLaneDetection && obj.getText().equals("Build JSON as TuSimple")) {
                    String str=FactoryUtils.buildJsonFileAsTuSimpleFormat(imageFolder);
                } else if (obj.getText().equals("Command Interpreter")) {
                    activateCmd = true;
                    FrameScriptEditor frm = new FrameScriptEditor();
                    frm.setVisible(true);
                    frm.setLocation(30, 200);
                    frm.setAlwaysOnTop(true);
                } else if (obj.getText().equals("Generate Segmentation Masks")) {
                    if (activatePolygon) {
                        FactoryUtils.generateSegmentationMasks(imageFiles);
                    }
                }
                repaint();
            } catch (Exception ex) {
                System.err.println(ex.toString());
            }
        }

    }

    private BufferedImage getCroppedImage() {
        Rectangle rect = new Rectangle(selectionRect.x - fromLeft, selectionRect.y - fromTop, selectionRect.width, selectionRect.height);
        BufferedImage bf = ImageProcess.cropImage(currBufferedImage, rect);
        return bf;
    }

    private void newScreenCaptureInstance() {
        FrameScreenCapture frm = new FrameScreenCapture(this);
        frm.setVisible(true);
    }

    private void loadNextImage() {
        if (imageIndex < imageFiles.length - 1) {
            imageIndex++;
        }
        if (!isSeqenceVideoFrame) {
            listPascalVocObject.clear();
            selectedBBox = null;
            selectedPolygon = null;
        }
        BufferedImage bf = ImageProcess.readImageFromFile(imageFiles[imageIndex]);
        setImage(bf, imagePath, caption, true);
        System.out.println("activateCrop:" + activateCrop);
        frame.titleImageInfo = (imageFiles[imageIndex].getPath());
        fileName = imageFiles[imageIndex].getName();
        imagePath = imageFiles[imageIndex].getAbsolutePath();
    }

    private void loadPrevImage() {
        if (imageIndex > 0) {
            imageIndex--;
        }
        if (!isSeqenceVideoFrame) {
            listPascalVocObject.clear();
            selectedBBox = null;
            selectedPolygon = null;
        }
        BufferedImage bf = ImageProcess.readImageFromFile(imageFiles[imageIndex]);
        setImage(bf, imagePath, caption, true);
        frame.titleImageInfo = (imageFiles[imageIndex].getPath());
        fileName = imageFiles[imageIndex].getName();
        imagePath = imageFiles[imageIndex].getAbsolutePath();
    }

    private void savePascalVocXML() {
        List<PascalVocObject> lstObject = new ArrayList();
        for (PascalVocObject obj : listPascalVocObject) {
            PascalVocBoundingBox bbox = obj.bndbox;
            PascalVocPolygon polygon = obj.polygonContainer;
            List<PascalVocAttribute> attributeList = obj.attributeList;
            String name = "";
            if (activateBoundingBox) {
                name = bbox.name;
            } else if (activatePolygon) {
                if (polygon != null) {
                    name = polygon.name;
                    if (bbox != null) {
                        name = bbox.name;
                    }
                }
            }
            lstObject.add(new PascalVocObject(name, "", 0, 0, 0, bbox, polygon, attributeList));
        }
        if (lstObject.size() > 0) {
            String xml = FactoryUtils.serializePascalVocXML(imageFolder, fileName, imagePath, new PascalVocSource(), lstObject);
        } else {
            File file = new File(imagePath);
            if (FactoryUtils.isFileExist(imageFolder + "/" + FactoryUtils.getFileName(file.getName()) + ".xml")) {
                FactoryUtils.deleteFile(imageFolder + "/" + FactoryUtils.getFileName(file.getName()) + ".xml");
            }
        }
    }

    private void saveLanesAsTxt() {
        String txtFilePath = imageFolder + "/" + FactoryUtils.getFileName(fileName) + ".txt";
        String content = "";
        for (PascalVocLane lane : splines) {
            String s = "lane:" + lane.name + ":";
            for (Point p : lane.spline) {
                s += p.x + "," + p.y + ":";
            }
            s = FactoryUtils.removeLastChar(s);
            content += s + "\n";
        }
        FactoryUtils.writeToFile(txtFilePath, content);
    }

    private void readLanesFromTxt() {
        splines.clear();
        showRegion=true;
        String txtFilePath = imageFolder + "/" + FactoryUtils.getFileName(fileName) + ".txt";
        if (!FactoryUtils.isFileExist(txtFilePath)) {
            return;
        }

        String content = FactoryUtils.readFile(txtFilePath);
        String[] rows = content.split("\n");
        for (String row : rows) {
            String[] str = row.split(":");
            ArrayList<Point> spline = new ArrayList<>();
            for (int i = 2; i < str.length; i++) {
                String[] pnt = str[i].split(",");
                spline.add(new Point(Integer.parseInt(pnt[0]), Integer.parseInt(pnt[1])));
            }
            PascalVocLane lane = new PascalVocLane(str[1], spline, null);
            splines.add(lane);
        }
    }

    public void setActivateStatistics(boolean activateStatistics) {
        this.activateStatistics = activateStatistics;
    }

    private void setDefaultValues() {
        activateScreenCapture = false;
        activateBoundingBox = false;
        activateSaveImage = false;
        activateRevert = false;
        activateBinarize = false;
        activateOriginal = false;
        activateHistogram = false;
        activateStatistics = false;
        activateRedChannel = false;
        activateGreenChannel = false;
        activateBlueChannel = false;
        activateRGB = false;
        activateGray = false;
        activateHSV = false;
        activateEdge = false;
        activateEqualize = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_UP) {
            if (imageIndex < imageFiles.length - 1) {
                imageIndex++;
            }
        } else if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_DOWN) {
            if (imageIndex > 0) {
                imageIndex--;
            }
        } else if (key == KeyEvent.VK_S) {
            if (activateBoundingBox) {
                savePascalVocXML();
                if (imageIndex + 1 >= imageFiles.length) {
                    return;
                }
                if (!isSeqenceVideoFrame) {
                    listPascalVocObject.clear();
                    selectedBBox = null;
                }
                imageIndex++;
                BufferedImage bf = ImageProcess.readImageFromFile(imageFiles[imageIndex]);
                rawImage = ImageProcess.clone(bf);
                adjustImageToPanel(bf, true);
            } else if (activatePolygon) {
                savePascalVocXML();
                if (imageIndex + 1 >= imageFiles.length) {
                    return;
                }
                if (!isSeqenceVideoFrame) {
                    listPascalVocObject.clear();
                    selectedPolygon = null;
                }
                imageIndex++;
                BufferedImage bf = ImageProcess.readImageFromFile(imageFiles[imageIndex]);
                rawImage = ImageProcess.clone(bf);
                adjustImageToPanel(bf, true);
            } else if (activateLaneDetection) {
                saveLanesAsTxt();
                saveLanesAsMask();
                if (imageIndex + 1 >= imageFiles.length) {
                    return;
                }
                imageIndex++;
                BufferedImage bf = ImageProcess.readImageFromFile(imageFiles[imageIndex]);
                rawImage = ImageProcess.clone(bf);
                adjustImageToPanel(bf, true);

            }
            return;
        } else if (key == KeyEvent.VK_DELETE || key == 8) { //key==8 maco işletim sistemi için eklendi VK_DELETE i görmüyordu
            if (activateBoundingBox) {
                if (selectedBBox != null) {
                    PascalVocObject temp_obj = null;
                    for (PascalVocObject obj : listPascalVocObject) {
                        if (obj.bndbox.equals(selectedBBox)) {
                            temp_obj = obj;
                            break;
                        }
                    }
                    if (temp_obj != null) {
                        listPascalVocObject.remove(temp_obj);
                    }
                    selectedBBox = null;
                } else {
                    listPascalVocObject.clear();
                }
            } else if (activatePolygon) {
                if (selectedPolygon != null) {
                    PascalVocObject temp_obj = null;
                    for (PascalVocObject obj : listPascalVocObject) {
                        if (obj.polygonContainer.equals(selectedPolygon)) {
                            temp_obj = obj;
                            break;
                        }
                    }
                    if (temp_obj != null) {
                        temp_obj.polygonContainer.polygon = null;
                        temp_obj.polygonContainer = null;
                        listPascalVocObject.remove(temp_obj);
                    }
                    selectedPolygon = null;
                } else {
                    listPascalVocObject.clear();
                }

            }
            repaint();
            return;
        }
        BufferedImage bf = ImageProcess.readImageFromFile(imageFiles[imageIndex]);
        rawImage = ImageProcess.clone(bf);
        adjustImageToPanel(bf, true);

        e.consume();
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }
}
