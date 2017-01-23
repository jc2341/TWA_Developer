package cam.dev.zhouxiaochi;

import java.awt.EventQueue;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.xml.parsers.ParserConfigurationException;

import org.json.JSONException;
import org.xml.sax.SAXException;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.esri.core.geometry.CoordinateConversion;
import com.esri.core.geometry.Envelope;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Line;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.geometry.Transformation2D;
import com.esri.core.io.UserCredentials;
import com.esri.core.map.CallbackListener;
import com.esri.core.map.Feature;
import com.esri.core.map.FeatureSet;
import com.esri.core.map.Graphic;
import com.esri.core.portal.Portal;
import com.esri.core.portal.WebMap;
import com.esri.core.renderer.SimpleRenderer;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.tasks.ags.query.Query;
import com.esri.map.ArcGISFeatureLayer;
import com.esri.map.ArcGISFeatureLayer.SELECTION_METHOD;
import com.esri.map.ArcGISTiledMapServiceLayer;
import com.esri.map.GroupLayer;
import com.esri.map.JMap;
import com.esri.map.Layer;
import com.esri.map.LayerEvent;
import com.esri.map.LayerList;
import com.esri.map.LayerListEventListenerAdapter;
import com.esri.map.MapOptions;
import com.esri.map.MapOptions.MapType;
import com.esri.map.MapOverlay;
import com.esri.map.QueryMode;
import com.esri.map.popup.*;
import com.esri.toolkit.overlays.HitTestEvent;
import com.esri.toolkit.overlays.HitTestListener;
import com.esri.toolkit.overlays.HitTestOverlay;
import com.esri.toolkit.overlays.InfoPopupOverlay;
 

public class App {
	
	public static int layerID;
	public static ArcGISFeatureLayer  Layer;
	public static  JCheckBox checkbox;
	
	public static String target;
	public static String[] nameList;
	public static String[] targets;
	public static String[] types;
	
	
	public static double[] x_array;
	public static double[] y_array;
	public static ArrayList<ArrayList<String>> relationship_array;
	
	public static Map<String, ArcGISFeatureLayer> LayerMap ;
	
	public static  ArcGISFeatureLayer linelayer;
	
	
	public static ArrayList<Rectangle> rect_list = new ArrayList<Rectangle>();
	public static ArrayList<Connection> connections = new ArrayList<Connection>();
	public static ArrayList<String> combination = new ArrayList<String>();
	
	public static ArrayList<Graphic[]> all_features = new ArrayList<Graphic[]>();
	public static ArrayList<ArcGISFeatureLayer> all_layers = new ArrayList<ArcGISFeatureLayer>();
	 
	public static int[] count;
	public static  final Map<String, ArcGISFeatureLayer> editlayer = new LinkedHashMap<>();
 
	 
	  public static void create_object(double x, double y, ArcGISFeatureLayer thisLayer, String type, String name) throws Exception
	  {
		
	 
		  thisLayer.setOperationMode(QueryMode.SELECTION_ONLY);
		    
		    FeatureWriter featurewriter = new FeatureWriter();
		    if(thisLayer.getDefaultSpatialReference() == null)
		    {
		    	JOptionPane.showMessageDialog(null, "Sorry, the layer is not yet fully loaded");
		    }
		    else
		    {
		    Graphic[] g = featurewriter.createFeature(type, x, y,thisLayer.getDefaultSpatialReference(),name);
		    rect_list.add(featurewriter.obstacle);
		    thisLayer.applyEdits(g, null, null, null);
		    all_layers.add(thisLayer);
		    all_features.add(g);
		    }
		  }
  
	  public static void create_line(double x, double y,double x2, double y2)
	  {
		  	linelayer.setOperationMode(QueryMode.SELECTION_ONLY);
		    
 
		    if(linelayer.getDefaultSpatialReference() == null)
		    {
		    	JOptionPane.showMessageDialog(null, "Sorry, the layer is not yet fully loaded");
		    }
		    else
		    {
 
		
		 
		    	  
		    	 Polyline line = new Polyline();
		    	 line.startPath(new Point(x,y));
		    	 line.lineTo(new Point(x2,y2));
		 
		    	 
				  SimpleLineSymbol outline = new SimpleLineSymbol(new Color(0, 150, 0), 300);
				    
	
				  
				   double angleRad = Math.toRadians(-45);
			          Transformation2D rotateTx = new Transformation2D();
			          rotateTx.rotate(Math.cos(angleRad), Math.sin(angleRad),  FeatureWriter.p_left_button);
		 
			          line.applyTransformation(rotateTx);
				 
						 Graphic polygonGraphic = new Graphic(line, outline);
						 Graphic[] adds = {polygonGraphic};
				 
				
		    	linelayer.applyEdits(adds, null, null, null);
		    
		    }
	  }
	  
	  

	   
  
	  

	  
	   
	
  final static SimpleFillSymbol testcolor = new SimpleFillSymbol(Color.black, new SimpleLineSymbol(Color.cyan, 1), SimpleFillSymbol.Style.SOLID);
  final static SimpleLineSymbol linecolor = new SimpleLineSymbol(Color.pink,3);
  
  final static SimpleLineSymbol linecolor2 = new SimpleLineSymbol(Color.GREEN,3);
  
  
  
  
  private JFrame window;
  private static JMap map;

  public App() throws Exception {
	    
 
	  
	  
 
	  JButton Load_feature = new JButton("Load Feature From OWL");
	  Load_feature.setLocation(0, 0);
	  Load_feature.setSize(190,30);
  
	  JButton Draw_connections = new JButton("Draw the connections");
	  Draw_connections.setLocation(0, 30);
	  Draw_connections .setSize(190,30);
  
	  JButton Delete = new JButton("Delete the features");
	  Delete.setLocation(0, 60);
	  Delete .setSize(190,30);
	  
 
    window = new JFrame();
    window.setSize(190, 130);
    window.setLocationRelativeTo(null); // center on screen
    window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    window.getContentPane().setLayout(new BorderLayout(0, 0));
    
    
 
    window.add(Load_feature);
    window.add(Draw_connections);
    window.add(Delete);

   
    
    // dispose map just before application window is closed.
    
    
    
    window.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent windowEvent) {
        super.windowClosing(windowEvent);
        map.dispose();
      }
    });
    
    
    
    
    //-------------------------------------------------------------------------------------------------------------------------
    
    Load_feature.addActionListener(new ActionListener() {
    	@Override
    	public void actionPerformed(ActionEvent arg0) {
    			try {
    				 
    				
    				for(int i = 0; i < targets.length; i++)
    				{
		 		        create_object(x_array[i],y_array[i],LayerMap.get(targets[i]),types[i],targets[i]); 
    				}
    			    drawLines();
    			
    			} catch (SAXException | IOException | ParserConfigurationException e) {
					// TODO Auto-generated catch block
					
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

    	
    	}
    	
    	
    	
    });
    
    Draw_connections.addActionListener(new ActionListener() {
    	@Override
    	public void actionPerformed(ActionEvent arg0) {
    		
    		drawconnection();
        	rect_list.clear();
	    	connections.clear();
    	}
    });
    
    Delete.addActionListener(new ActionListener() {
    	@Override
    	public void actionPerformed(ActionEvent arg0) {
    		System.out.println("Delete Clicked 1 ");
    		delete();
    		
    	}
    });
    
 
    
    //-------------------------------------------------------------------------------------------------------------------------
   
    
 
    
    
    //-------------------------------------------------------------------------------------------------------------------------
    MapOptions mapOptions = new MapOptions(MapType.TOPO);
    map = new JMap(mapOptions);

    ArcGISTiledMapServiceLayer tiledLayer = new ArcGISTiledMapServiceLayer("http://services.arcgisonline.com/ArcGIS/rest/services/World_Topo_Map/MapServer");
 
    
    LayerList layerList = new LayerList();
    layerList.add(tiledLayer);
    UserCredentials user = new UserCredentials();
    user.setUserAccount("kleinelanghorstmj", "h3OBhT0gR4u2k22XZjQltp"); // Access secure feature layer service using login username and password
	 
    
    //================================================================
   
     LayerMap = new HashMap<String, ArcGISFeatureLayer>();
    
     
     
     readlist();

 
   
     relationship_array = new ArrayList<ArrayList<String>>();
     x_array = new double[targets.length];
     y_array = new double[targets.length];
      
     linelayer = new ArcGISFeatureLayer("http://services5.arcgis.com/9i99ftvHsa6nxRGj/arcgis/rest/services/linelayer003/FeatureServer/0", user);
   //  http://services5.arcgis.com/9i99ftvHsa6nxRGj/arcgis/rest/services/BuildingLayer/FeatureServer/0
     ArcGISFeatureLayer buildinglayer = new ArcGISFeatureLayer("http://services5.arcgis.com/9i99ftvHsa6nxRGj/arcgis/rest/services/BuildingLayer/FeatureServer/0", user);

     
     
     
    for(int i = 0; i < targets.length; i++)
    {

    target =  targets[i];
    createLayer(target);
    
    String idx = FeatureServiceUpdater.layerID;

    System.out.println("#######################################################");
    System.out.println(OWLReader.relationships);
    
    relationship_array.add(OWLReader.relationships);

			 
      
    ArcGISFeatureLayer  newLayer = new ArcGISFeatureLayer("http://services5.arcgis.com/9i99ftvHsa6nxRGj/arcgis/rest/services/TEST017/FeatureServer/" + targets[i], user);
   
  //  ArcGISFeatureLayer  newLayer = new ArcGISFeatureLayer("http://services5.arcgis.com/9i99ftvHsa6nxRGj/arcgis/rest/services/TEST017/FeatureServer/" + idx, user);

    all_layers.add(newLayer);
    LayerMap.put(target, newLayer);
    x_array[i] = OWLReader.x;
    y_array[i] = OWLReader.y;
    
    
 
   
    
    
    
    SimpleRenderer renderer  = new SimpleRenderer(testcolor);
    newLayer.setRenderer(renderer);
    layerList.add(newLayer);
   
    map.getLayers().add(newLayer);
    
    editlayer.put(target,newLayer);
    
    }
    //================================================================
    
 
    LayerMap.put("linelayer", linelayer);
    LayerMap.put("buidlinglayer",buildinglayer);
    SimpleRenderer renderer2 = new SimpleRenderer(linecolor);
    buildinglayer.setRenderer(renderer2);
    linelayer.setRenderer(renderer2);
    layerList.add(buildinglayer);
    layerList.add(linelayer);
    
     
    
    layerList.add(tiledLayer);
    map.getLayers().add(linelayer);
    map.getLayers().add(buildinglayer);
  
    
    
    Point mapCenter = new Point(11543665,141400);
    map.setExtent(new Envelope(mapCenter,7200,5400));

    window.getContentPane().add(map);
    
    KMLReader reader = new KMLReader();
    reader.readkml(buildinglayer);
 
  }

  /**
   * Starting point of this application.
   * @param args
 * @throws Exception 
 * @throws IOException 
   */
  public static void main(String[] args) throws IOException, Exception {
 
	  	 
		 
    EventQueue.invokeLater(new Runnable() {

      @Override
      public void run() {
        try {
          App application = new App();
          application.window.setVisible(true);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }
  
  public  static void createLayer(String targetName) throws IOException, Exception
  {
	  if(!targetName.contains("linelayer"))
	  {
	  OWLReader.read_owl_file(null, targetName);
	  
	    
	    
		int length = OWLReader.name_list.size();
		
		String[] typeList = new String[length];
		 nameList = new String[length]; 
		
		
		System.out.println("--------------------------------------");
		
		for(int i = 0; i < length; i++)
		{
			typeList[i] = "esriFieldTypeString";
		}
		
		for(int i = 0; i < length; i++)
		{
			nameList[i] =  OWLReader.name_list.get(i);

		}
		
		
 
		 
	 
	  
		//construct name map of lists
		Map<String, String[]> attrLists = new HashMap<String, String[]>();

		attrLists.put("name", nameList);
		attrLists.put("type", typeList);
		attrLists.put("alias", nameList);
		int lengthOfEachList = nameList.length;
  //      FeatureServiceUpdater.generateLayer(lengthOfEachList, attrLists, target);
	  }
	  else
	  {
		  
		  
	  }
		
  }
  
  
  public static void readlist()
  {
	  ArrayList<String> temp = new ArrayList<String>();
	  try (BufferedReader br = new BufferedReader(new FileReader("map.txt"))) {

			String sCurrentLine;

			while ((sCurrentLine = br.readLine()) != null) {
				temp.add(sCurrentLine);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	  
	  targets = new String[temp.size()];
	  types = new String[temp.size()];
	  int counter = 0; 
	  for(String item : temp)
	  {
	//	  System.out.println(temp); 
	//	  System.out.println("=======================================");
		 targets[counter] = item.split("#")[0];
	     types[counter]    = item.split("#")[1];
		  counter++;
	  }
	  
	  
  }
  

  public static void drawLines()
  {
	  boolean dup_flag = false;
	  count = new int[rect_list.size()];
	  int counter_offset = 0;
	 for(int i = 0; i < relationship_array.size(); i++)
	 {
		 ArrayList<String> relationship = relationship_array.get(i);
		 for(int j = 0; j < relationship.size();j++)
		 {
			 
			 for(int m = 0; m < relationship_array.size(); m++ )
			 {
				 ArrayList<String> relationship2 = relationship_array.get(m);
				 for(int n = 0; n <  relationship2.size(); n++)
				 {
					 String one = relationship.get(j);
					 String two = relationship2.get(n);
					 
					 if(	(i != m)&& (one.equals(two)))
					 {
						
					     
						 dup_flag = false;
						 
						 for(String comb : combination)
						 {
							 if(comb.contains(targets[i]) && comb.contains(targets[m]))
							 {
								 dup_flag = true;
							 }
							 
						 }
						 
						 if(!dup_flag)
						 {
							 combination.add(targets[i] + targets[m]);
							 Point  point_from = new Point(x_array[i],y_array[i]);
							 Point point_to = new Point(x_array[m],y_array[m]);
						    
							 double angleRad = Math.toRadians(45);
					           Transformation2D rotateTx = new Transformation2D();
					           rotateTx.rotate(Math.cos(angleRad), Math.sin(angleRad), FeatureWriter.p_left_button);
					          
					           point_from.applyTransformation(rotateTx);
					           point_to.applyTransformation(rotateTx);
					  
					         
					           
					 //        create_line( point_from.getX(),point_from.getY(),point_to.getX(),point_to.getY());
					    
					  		 double start_x =  rect_list.get(i).getLowerLeft().getX();
					      	 double start_y =  rect_list.get(i).getLowerLeft().getY();
					           
					  		 double end_x =  rect_list.get(m).getLowerLeft().getX();
					      	 double end_y =  rect_list.get(m).getLowerLeft().getY();
					      	 
					      	 
					      	 
					      	 
					      	 if(start_y > end_y)
					      	 {
					      	 
					         start_y = rect_list.get(i).getLowerLeft().getY();
							 start_x = rect_list.get(i).getLowerLeft().getX() + count[i] * 0.5;
							 
							 
							 end_y = rect_list.get(m).getUpperLeft().getY();
							 end_x = rect_list.get(m).getUpperLeft().getX() + count[m] * 0.5;
							 
							 
							 count[i] = count[i] + 1;
							 count[m] = count[m] + 1;
							 
							 System.out.print("Here we are -- >  " );
							 
					      	 }
					      	 else
					      	 {
					      		 
						         start_y = rect_list.get(i).getUpperLeft().getY();
								 start_x = rect_list.get(i).getUpperLeft().getX() + count[i] * 0.5;
								 
								 
								 end_y = rect_list.get(m).getLowerLeft().getY();
								 end_x = rect_list.get(m).getLowerLeft().getX() + count[m] * 0.5;
								 
								 
								 count[i] = count[i] + 1;
								 count[m] = count[m] + 1;
					      		 
								 System.out.print("Here we are -- >  " );
					      		 
					      	 }
					      	 
					      	
	
					      	 point_from = new Point(start_x,start_y);
					      	 point_to     = new Point(end_x,end_y);
					      	  
					    	 Connection this_connection = new Connection(point_from,point_to);
							 
							 connections.add(this_connection);
					  
 
						 
						 
						 
						 
						 
						 
						 }
 
	
					 }
					 
				 }

			 }
			 
	//		 System.out.println(combination);
			 
		 }
		 
	 }
	    for(Connection c : connections)
	    {
	    	 System.out.println("startpoint x y " + c.start.getX() + "--" + c.start.getY() );
	    	 System.out.println("endpoint  x y " + c.end.getX() + "--" + c.end.getY() );
	    	
	    }
		  
  }

	   
  public static int getIndex(String[] array, String item)
  {
	  int index = 0;
	  
	  return index;
  }
  
  
  
  public static void drawconnection()
  {
	    Draw_connections.obstacles  =  rect_list;
	    Draw_connections.connections = connections;
	     
		linelayer.setOperationMode(QueryMode.SELECTION_ONLY);
	    
		 
	    if(linelayer.getDefaultSpatialReference() == null)
	    {
	    	JOptionPane.showMessageDialog(null, "Sorry, the layer is not yet fully loaded");
	    }
	    else
	    { 
	    	 
	 
			 Graphic[] adds = Draw_connections.createConnections();
			 linelayer.applyEdits(adds, null, null, null);
	    }

  }
  
  
  public static void delete()
  {
	  all_layers.add(linelayer);
	  for(int i = 0 ; i < all_layers.size() ; i++)
	  {
		  //all_layers.get(i).applyEdits(null, all_features.get(i), null, null);
		  int[] ids = all_layers.get(i).getGraphicIDs();
		  for(int id : ids)
		  {
			  System.out.println(id);
		  }
		  
		  all_layers.get(i).setSelectionIDs(ids, true);
		  
		  
		  all_layers.get(i).applyEdits(null, all_layers.get(i).getSelectedFeatures(), null, null);
	  }
  }
  

  
  public static void removeElement(Object[] a, int del) {
	    System.arraycopy(a,del+1,a,del,a.length-1-del);
	}
  
 
  
  
  
  
  
  
  
}
