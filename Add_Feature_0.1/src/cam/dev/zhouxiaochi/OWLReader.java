package cam.dev.zhouxiaochi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.esri.core.geometry.Point;


public class OWLReader {
	  final static Logger logger = LoggerFactory.getLogger(OWLReader.class);

	
	
	/****
	 * The class that stores all the infomation of a node within OWL file
	 * @param name --> the name of the node (the string after #)
	 * @param type   --> the nodetype, for exampe numericalValue
	 * @param value  --> the value of a numerical value
	 * @param unit    --> the unit of the value 
	 */
	
    public static class Device
	{
		public  String Id;
		public  Point Point;
		public  String[] Name_list;
		public  String[] Value_list; 

	}	
	
	public static class OWLfileNode
	{
		OWLfileNode(String name, String type, String value, String unit, String parent) {
			NodeName = name;
			NodeType = type;
			NodeValue = value;
			ValueUnit = unit;
			ParentNodeName = parent;
		}
		
		public  String NodeName;
		public  String NodeType;
		public  String NodeValue;
		public  String ValueUnit;
		public  String ParentNodeName;
		public boolean CarryData = false;
	}	
	
	
public static NodeList individuals;	
public static ArrayList<String> nodelist = new ArrayList<String>();

public static Map<String,OWLfileNode> owlnodemap = new HashMap<>();//map links name to node info
public static Map<String,Node> nodemap = new HashMap<>();//map links name to actual node object
public static Set<OWLfileNode> theNodeList = new LinkedHashSet<>(); // the raw nodelist that stores every node connected the the target
public static Set<OWLfileNode> theFinalNodeList = new LinkedHashSet<>(); // the filterd nodelist that returns the nodes that carries data
public static Set<String> theNameList = new LinkedHashSet<>();


public static ArrayList<String> name_list = new ArrayList<String>();
public static ArrayList<String> value_list = new ArrayList<String>();
public static ArrayList<String> relationships = new ArrayList<String>();

private static ArrayList<String> deviceNameList = new ArrayList<String>();
public static double x = 0;
public static double y = 0; 


  // for storing coordinates of a batch of devices  





public static ArrayList<String> read_owl_file(String filename, String deviceName) throws IOException, Exception{
	
	return read_owl_file(filename, deviceName, false);
}


 
	public static ArrayList<String> read_owl_file (String filename, String deviceName, boolean expandOneLevelOnly) throws Exception, IOException {

		nodelist.clear();
		name_list.clear();
		value_list.clear();
		relationships.clear();
		nodemap.clear();
		theNodeList.clear();
		theFinalNodeList.clear();
		theNameList.clear();
		owlnodemap.clear();
		


		if(filename == null)
		{
			logger.warn("owl file location not defined. Function read owl file terminated");
		// System.out.println("WARNING: owl file location not defined. Function read owl file terminated");
		return null;
		} 
		
		
		
		
	   File inputFile = new File(filename);
       DocumentBuilderFactory dbFactory  = DocumentBuilderFactory.newInstance();
       DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
       Document doc = dBuilder.parse(inputFile);
       doc.getDocumentElement().normalize();
       Element root = doc.getDocumentElement();
       individuals = root.getElementsByTagName("owl:NamedIndividual");
       
       for(int i = 0 ; i < individuals.getLength() ; i ++)
       {
    	   String name = individuals.item(i).getAttributes().item(0).getNodeValue();
    	   name = name.substring(name.indexOf("#") + 1 );
    	   nodelist.add(name);
    	   nodemap.put(name, individuals.item(i));
       }
       
       
		//if no specific device return full nodelist
		if(deviceName == null)
		{
			return nodelist;
		}
       
       
       
       
       
       OWLfileNode startnode = new OWLfileNode(deviceName,null,null,null,"JesusChrist");
      
       
       expand(startnode, expandOneLevelOnly);
       

       
       
       
       // ------------------------------------------
       for(OWLfileNode node : theNodeList)
       {
    	//   System.out.println("Name:  " + node.NodeName + "|| Type: " + node.NodeType + 	"|| Value: " + node.NodeValue + "||Unit: " + node.ValueUnit + "|| Parent: " +  node.ParentNodeName ); 
   		if(node.CarryData)
   		{
   			theNameList.add(node.NodeName);
   			owlnodemap.put(node.NodeName, node);
   		}
       }
       
       
       
       /**
       
       
       for(OWLfileNode node: theFinalNodeList)
       {
     //    System.out.println("Name:  " + node.NodeName + "*** Type: " + node.NodeType + 	"*** Value: " + node.NodeValue + "*** Unit: " + node.ValueUnit + "*** Parent: " +  node.ParentNodeName ); 
         node.NodeName = node.NodeName.replaceAll("-", "_").trim();
        
         System.out.println("-----------> " +  node.NodeName);
         name_list.add(node.NodeName.trim());
         value_list.add(node.NodeValue.trim());
         if(node.ValueUnit!=null)
         {
        	 name_list.add(node.NodeName + "_Unit" );
        	 value_list.add(node.ValueUnit);
         }
         
         
       }
       
       **/
       
       
       for(String name : theNameList)
       { 
    	   
    	   OWLfileNode node = owlnodemap.get(name);
    	   node.NodeName = node.NodeName.replaceAll("-", "_").trim();
    	   //logger.info("-----------> " +  node.NodeName);
    	   //System.out.println("-----------> " +  node.NodeName);
           name_list.add(node.NodeName.trim());
           value_list.add(node.NodeValue.trim());
           if(node.ValueUnit!=null)
           {
          	 name_list.add(node.NodeName + "_Unit" );
          	 value_list.add(node.ValueUnit);
           }
           
       }
       
 
		for(int i = 0; i < name_list.size(); i ++)
		{
			String item = name_list.get(i);
			if(item.contains("_x_") && !(item.contains("Unit")))
			{
				 x = Double.parseDouble(value_list.get(i));
			//	 logger.info("x---> " + x);
				 
			}
			
			if(item.contains("_y_") && !(item.contains("Unit")))
			{
				 y = Double.parseDouble(value_list.get(i));
				// logger.info("y---> " + y);

			}
			
		}
		
        return null;
       
    }
	
	
	
	
	
	  public static void expand(OWLfileNode node){
		  expand(node, false);
	  }

	//TODO: Modified so that if has attribute sameAs, search the sameAs node instead
	  public static void expand(OWLfileNode node, Boolean expandOneLevel)
	{
		
		if(nodemap.get(node.NodeName)!=null)///only run if node do exist
		{
		Node targetnode = nodemap.get(node.NodeName);
		NodeList childnodes = targetnode.getChildNodes();
		for(int i = 0; i < childnodes.getLength(); i++)
		{
			if(childnodes.item(i).getNodeType() ==  Node.ELEMENT_NODE) // make sure the node is a node that stores data
			{
			String nodename = childnodes.item(i).getAttributes().item(0).getNodeValue();// find the value of NamedIndividual , extract the name after #
			String nodetype = childnodes.item(i).getNodeName();
			String nodevalue = null;
			if(!(nodename.lastIndexOf("#") == nodename.length() - 1) &&!(nodetype.contains("rdf:type")))// check whether it is empty after #
				{
				
				if(nodetype.contains("owl:sameAs")) {//contains allias?
					nodename = nodename.split("#")[1];
					 logger.info("a same as allias:"+nodename);
					OWLfileNode newNode = new OWLfileNode(nodename,nodetype,"","",node.NodeName);
					expand(newNode, false);//expand allias node 
				}
				else{
					logger.info(nodename);
					logger.info("Node Type --> : " + nodetype);
					
					logger.info("---------------------------------------");
					
					nodename = nodename.split("#")[1];
					
					if(nodetype.toLowerCase().contains("unit"))
					{
				//		System.out.println("This is a unit --> " + nodename);
 						node.ValueUnit = nodename;
					}
					else
					{
				//	if(nodetype.contains("numerical"))
						if(true)
					{
					nodevalue =  childnodes.item(i).getTextContent();
					node.NodeValue = nodevalue;
					node.CarryData = true;
					}
				
					/**
					if(nodetype.contains("realize"))
					{
		//				System.out.println("Here is a realizes -- > " +  nodename);
						
						getRelationships( nodemap.get(nodename));
						
					}
					**/
					
					
					OWLfileNode newNode = new OWLfileNode(nodename,nodetype,"","",node.NodeName);
				//	System.out.println("Name:  " + newNode.NodeName + " Type: " + newNode.NodeType +
				//			" Value: " + newNode.NodeValue + " Unit: " + newNode.ValueUnit + " Parent: " +  newNode.ParentNodeName ); 
					theNodeList.add(newNode); //  && 
					if(nodemap.get(nodename)!=null   && !(newNode.NodeName.contentEquals(node.ParentNodeName)) &&!expandOneLevel) // check whether such node exists
					{
						if(!(nodetype.contains("topology:enters")||nodetype.contains("topology:leaves")))
						{
						expand(newNode,false);
						}
					}
					}
					}
				}
			
			}
		}
		
		theNodeList.add(node);
		
		
	}
	 
	}

	  public static void getRelationships(Node relationship)
	  { 
	//	  System.out.println("Running relationship ---> ");
		  relationships = new ArrayList<String>();
		  NodeList list = relationship.getChildNodes();
	//	  System.out.println("list size --  " + list.getLength());
		  for(int i = 0; i < list.getLength();i++)
		  {
			  if((list.item(i).getNodeType() == Node.ELEMENT_NODE) && (list.item(i).getNodeName().contains("topology"))){
			   String temp =  list.item(i).getAttributes().item(0).getNodeValue();
	//		   System.out.println("relationship ---> " + temp);
			   if(!relationships.contains(temp.split("#")[1]))
			   {
				   
				   relationships.add(temp.split("#")[1]);
			   }
			    
			  }
		  }
		   removeDuplicates(relationships);
	  }
 	
	  public static void removeDuplicates(ArrayList<String> arraylist)
	  {
 		   for(int i = 0; i < arraylist.size(); i++)
		   {
 
			   for(int j = 0; j < arraylist.size(); j++)
			   {
				   if(arraylist.get(i).equals(arraylist.get(j))&& (i!=j))
				   {
					   arraylist.remove(j);
				   }
			   }
		   }
		  
	  }
 	
 	
 	 public static List<String> getDeviceList(String fileName) throws IOException, Exception{
 		 
 		 if(deviceNameList == null){//lazy initiation 
 		 String plantName = fileName.split("\\.")[0];//extract plant name
 		 
 		read_owl_file(fileName, plantName, true);//read owl to first level of plant node
 		
 		for(OWLfileNode node :theNodeList){
 			logger.info(node.NodeName+"  "+node.NodeType+"  "+node.NodeValue);
 			if(node.NodeType.contains("hasSubsystem")){//is this attri shows a subsystem?
 				//>YES! Then it is a device
 				deviceNameList.add(node.NodeName);
 			}
 			
 		}
 		 }
 	 return deviceNameList;
 	 }
 	
 	
 	 
 	 
 	 
 	/****
 	 * This function is used to expand certain type of equipment in batch. 
 	 * @param filename --> the name of the owl file
 	 * @param type     --> the type of device to be expanded
 	 * @throws Exception 
 	 */
 	 
 	public static ArrayList<Device> BatchOperationForPoint(String filename, String type, Boolean filterOn, String filter) throws Exception
 	{
 	   ArrayList<Device> device_list = new ArrayList<Device>();
 	   File inputFile = new File(filename);
       DocumentBuilderFactory dbFactory  = DocumentBuilderFactory.newInstance();
       DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
       Document doc = dBuilder.parse(inputFile);
       doc.getDocumentElement().normalize();
       Element root = doc.getDocumentElement();
       individuals = root.getElementsByTagName("owl:NamedIndividual");
       for(int i = 0 ; i < individuals.getLength() ; i++)
       {
     	   
    	   if(individuals.item(i).hasChildNodes())
			{
				Node childnode = individuals.item(i).getChildNodes().item(1);
				if(childnode.hasAttributes())
				{
					 String type_name = childnode.getAttributes().item(0).getNodeValue().split("#")[1];
					 if(type_name.contentEquals(type))
					 {
						 String node_name = individuals.item(i).getAttributes().item(0).getNodeValue().split("#")[1];
						 
						
						 if(filterOn)
						 {
							 
							 if( node_name.matches(filter))
							 {
								 logger.info("Node name -->" + node_name);
								 name_list.clear();
								 value_list.clear();
								 read_owl_file(filename,node_name,false);	 
							
								 Device new_device = new Device();
								 new_device.Name_list = name_list.toArray(new String[name_list.size()]);
								 new_device.Value_list = value_list.toArray(new String[value_list.size()]);
								 
								 new_device.Point = new Point(x,y);
								 new_device.Id = node_name;

								 device_list.add(new_device);
	 
							 }
						 }
						 else
						 {
							logger.info("Node name -->" + node_name);
							 name_list.clear();
							 value_list.clear();
							 read_owl_file(filename,node_name,false);	 
						
							 Device new_device = new Device();
							 new_device.Name_list = name_list.toArray(new String[name_list.size()]);
							 new_device.Value_list = value_list.toArray(new String[value_list.size()]);
							 
							 new_device.Point = new Point(x,y);
							 new_device.Id = node_name;

							 device_list.add(new_device);
 
						 }
					   
					 }
 				}
			}
       }
 
       return device_list;
       
 	}
 	  
 	
 	
 	
 	
 	
 	
}
