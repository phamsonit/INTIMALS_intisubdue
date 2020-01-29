package convert;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.io.File;
import java.io.FileWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;
import org.w3c.dom.Document;

import java.net.URLEncoder;
import java.net.URLDecoder;

import java.util.Set;

//convert groums into graphs
public class ConvertGroum {

    private int vertexID;
    private int edgeID;
    private int nbGraph;
    private FileWriter fileOut;
    private Set<String> methodCalls = new HashSet<>();
    //////////
    private void readMethod(String path){
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                methodCalls.add(line);
            }
        }catch (IOException e) {System.out.println("Error: reading method file "+e);}

    }
    //create graphs from groums
    private void createGraph(Node root){
        try {
            //store new ID and old ID of vertices of each graph
            Map<String,String> realID = new HashMap<>();
            //store IDs of the control nodes
            Set<String> CfgNodeID = new HashSet<>();

            NodeList nodeList = root.getChildNodes();
            for(int i=0; i< nodeList.getLength(); ++i){
                if(nodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    String label = nodeList.item(i).getNodeName();
                    label = label.substring(label.length()-4);
                    switch (label){
                        case "Node":
                            String nodeLabel;
                            String nodeId;
                            String type;
                            String lineNb="";
                            //if node is VarNode or DataNode
                            /*
                            SUBDUE uses all attributes to compute the graph extension.
                            Two nodes are considered as equivalent if they have the same attributes.
                            Therefore, we cannot store line number in the attributes since the line numbers
                            of attributes of those nodes are different
                            */
                            //TODO: choose only CfgNode which are in the method call list
                            if(nodeList.item(i).hasChildNodes()){
                                if(nodeList.item(i).getNodeName().equals("VarNode")){
                                    nodeId = nodeList.item(i).getAttributes().item(0).getNodeValue();
                                    //get Type of VarNode
                                    nodeLabel = nodeList.item(i).getAttributes().item(1).getNodeValue();
                                    type = "VarNode";
                                    ++vertexID;
                                    fileOut.write("{\"vertex\": {\n");
                                    fileOut.write("\"id\": \"" +vertexID+"\",\n");
                                    fileOut.write("\"attributes\": {\"label\":\"" + Util.formatLabel(nodeLabel) + "\"," +
                                            "\"type\":\"" + type + "\"},\n");
                                    //"\"line\":\"" + lineNb + "\"},\n");
                                    fileOut.write("\"timestamp\": \"1\"}},\n");
                                    //keep the mapping of real ID node and vertexID in the json file
                                    realID.put(nodeId,String.valueOf(vertexID));

                                }else{//CfgNode
                                    nodeId = nodeList.item(i).getAttributes().item(0).getNodeValue();
                                    nodeLabel = nodeList.item(i).getTextContent().replace("\"","-");
                                    type = "CfgNode";
                                    //if(nodeList.item(i).getAttributes().getLength()>1)
                                    //lineNb = nodeList.item(i).getAttributes().item(1).getNodeValue();
                                    //store ID of CfgNode IDs occurred in the list of common method call
                                    if(methodCalls.contains(Util.decode(nodeList.item(i).getTextContent()))){
                                        //CfgNodeID.add(nodeId);
                                        ++vertexID;
                                        fileOut.write("{\"vertex\": {\n");
                                        fileOut.write("\"id\": \"" +vertexID+"\",\n");
                                        fileOut.write("\"attributes\": {\"label\":\"" + Util.formatLabel(nodeLabel) + "\"," +
                                                "\"type\":\"" + type + "\"},\n");
                                        //"\"line\":\"" + lineNb + "\"},\n");
                                        fileOut.write("\"timestamp\": \"1\"}},\n");
                                        //keep the mapping of real ID node and vertexID in the json file
                                        realID.put(nodeId,String.valueOf(vertexID));
                                    }
                                }
                            }
                            break;

                        case "Edge":
                            //remove all TransitiveEdges from the graph
                            if(nodeList.item(i).getNodeName().equals("TransitiveEdge")) break;

                            if(nodeList.item(i).hasAttributes()) {
                                String edge_from = "";
                                String edge_to = "";
                                String transitiveEdgeFrom="";
                                String transitiveEdgeTo="";
                                NamedNodeMap nodeMap = nodeList.item(i).getAttributes();
                                for (int j=0; j<nodeMap.getLength(); ++j) {
                                    switch (nodeMap.item(j).getNodeName()){
                                        case "From":
                                            edge_from = realID.get(nodeMap.item(j).getNodeValue());
                                            transitiveEdgeFrom = nodeMap.item(j).getNodeValue();
                                            break;
                                        case "To":
                                            edge_to = realID.get(nodeMap.item(j).getNodeValue());
                                            transitiveEdgeTo = nodeMap.item(j).getNodeValue();
                                            break;
                                    }
                                }
                                //keep TransitiveEdges which connect to/from VarNodes and CfgNodes
                                if(!realID.containsKey(transitiveEdgeFrom) || !realID.containsKey(transitiveEdgeTo)) break;
                                //keep TransitiveEdges which connect to/from CfgNodes occurred in the list of method calls
                                //if(!CfgNodeID.contains(transitiveEdgeFrom) || !CfgNodeID.contains(transitiveEdgeTo)) break;

                                ++edgeID;
                                fileOut.write("{\"edge\": {\n");
                                fileOut.write("\"id\": \""+edgeID+"\",\n");
                                fileOut.write("\"source\": \""+edge_from+"\",\n");
                                fileOut.write("\"target\": \""+edge_to+"\",\n");
                                fileOut.write("\"attributes\": {\"label\": \""+nodeList.item(i).getNodeName()+"\"},\n");
                                fileOut.write("\"directed\": \"true\",\n");
                                fileOut.write("\"timestamp\": \"1\"}},\n");
                            }
                            break;
                    }
                    //System.out.println();
                }
            }
            ++nbGraph;
        }catch (Exception e){
            System.out.println("create graph error "+e);
        }
    }

    public void run(String inPath, String outPath){
        try{
            //
            vertexID=0;
            edgeID=0;
            nbGraph=0;
            //
            fileOut  = new FileWriter(outPath);
            fileOut.write("[\n");
            //
            //read method call file methods_i.txt
            String[] temp = inPath.split("/");
            String clusterID = temp[temp.length-1].split("_")[1];
            String methodsFile = inPath + "/" + "methods_"+clusterID+".txt";
            readMethod(methodsFile);

            //read xml files
            File f = new File(inPath);
            File[] subdir = f.listFiles();
            Arrays.sort(subdir);
            for (File fi : subdir) {
                if (fi.isFile() && fi.getName().charAt(0)!='.' ) {
                    String[] fullName = fi.getName().split("\\.");
                    String ext = fullName[fullName.length - 1];
                    //String fileName = fullName[0];
                    if(ext.toLowerCase().equals("xml")){
                        //System.out.print("reading file: " + f + "/" + fi.getName());
                        //System.out.println(f+"/"+fi.getName());
                        String fileName = f+"/"+fi.getName();
                        //System.out.println(fileName);
                        File fXmlFile = new File(fileName);
                        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                        Document doc = dBuilder.parse(fXmlFile);
                        doc.getDocumentElement().normalize();

                        createGraph(doc.getDocumentElement());

                    }
                }else {
                    if (fi.isDirectory()) {
                        System.out.println("read sub-directory");//TODO read file in sub-directories
                    }
                }
            }
            //System.out.println("]");
            fileOut.write("{}\n");
            fileOut.write("]\n");
            fileOut.flush();
            fileOut.close();
            System.out.println("#input graphs: "+nbGraph);

        }catch (Exception e){
            System.out.println("conver groums error");
        }
    }
}