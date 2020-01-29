package convert;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.*;

//convert ASTs into graphs
public class ConvertAST {

    private int top;
    private int vertexID;
    private int edgeID;

    private ArrayList<Integer> sr;
    private FileWriter fileOut;

    private Map<String,Set<String>> whiteLabels = new HashMap<>();
    private Set<String> rootLabels = new LinkedHashSet<>();

    //////////////
    private void readRootLabel(String path, Set<String> _rootLabels){

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                if( ! line.isEmpty() && line.charAt(0) != '#' ){
                    String[] str_tmp = line.split(" ");
                    _rootLabels.add(str_tmp[0]);
                }
            }
        }catch (IOException e) {System.out.println("Error: reading listRootLabel "+e);}
    }

    //read white labels from given file
    private void readWhiteLabel(String path, Map<String,Set<String> > _whiteLabels){
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                if( ! line.isEmpty() && line.charAt(0) != '#' ) {
                    String[] str_tmp = line.split(" ");
                    String ASTNode = str_tmp[0];
                    Set<String> children = new HashSet<>();
                    for(int i=1; i<str_tmp.length; ++i){
                        children.add(str_tmp[i]);
                    }
                    _whiteLabels.put(ASTNode,children);
                }
            }
        }catch (IOException e) {System.out.println("Error: reading white list "+e);}
    }

    //create json graphs from ASTs
    private void createASTGraph(Node root){
        try {
            //create a list to store the id of vertices
            if(root.getNodeType() == Node.ELEMENT_NODE){
                String label = Util.formatLabel(root.getNodeName());
                vertexID++;
                //create vertex
                fileOut.write("{\"vertex\": {\n");
                fileOut.write("\"id\": \"" +vertexID+"\",\n");
                fileOut.write("\"attributes\": {\"label\":\"" + label + "\"},\n");
                fileOut.write("\"timestamp\": \"1\"}},\n");

                sr.add(vertexID);

                if(root.hasChildNodes()){
                    NodeList nodeList = root.getChildNodes();
                    if(nodeList.getLength()==1){
                        //create vertex for leaf node
                        vertexID++;
                        fileOut.write("{\"vertex\": {\n");
                        fileOut.write("\"id\": \"" +vertexID+"\",\n");
                        fileOut.write("\"attributes\": {\"label\":\"" + Util.formatLabel(root.getTextContent().trim()) + "\"},\n");
                        fileOut.write("\"timestamp\": \"1\"}},\n");
                        //
                        sr.add(vertexID);
                        top = sr.size()-1;
                        int child = sr.get(top);
                        int parent = sr.get(top - 1);
                        //
                        edgeID++;
                        fileOut.write("{\"edge\": {\n");
                        fileOut.write("\"id\": \""+edgeID+"\",\n");
                        fileOut.write("\"source\": \""+parent+"\",\n");
                        fileOut.write("\"target\": \""+child+"\",\n");
                        fileOut.write("\"attributes\": {\"label\": \"children\" },\n");
                        fileOut.write("\"directed\": \"true\",\n");
                        fileOut.write("\"timestamp\": \"1\"}},\n");
                        //
                        sr.remove(top);
                    }else{
                        //NodeList nodeList = root.getChildNodes();
                        //only allow children labels which are in the white list
                        if(whiteLabels.containsKey(root.getNodeName())){
                            //System.out.println(node.getNodeName());
                            Set<String> temp = whiteLabels.get(root.getNodeName());
                            for(int i=0; i<nodeList.getLength(); ++i)
                                if(nodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                                    if(temp.contains(nodeList.item(i).getNodeName())) {
                                        //System.out.println(nodeList.item(i).getNodeName());
                                        createASTGraph(nodeList.item(i));
                                    }
                                }
                        }else{
                            //recur reading every child node
                            for (int i = 0; i < nodeList.getLength(); ++i) {
                                if (nodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                                    createASTGraph(nodeList.item(i));
                                }
                            }
                        }

                    }
                }
                top = sr.size() - 1;
                if( top < 1) return;
                int child = sr.get(top);
                int parent = sr.get(top - 1);
                edgeID++;
                fileOut.write("{\"edge\": {\n");
                fileOut.write("\"id\": \""+edgeID+"\",\n");
                fileOut.write("\"source\": \""+parent+"\",\n");
                fileOut.write("\"target\": \""+child+"\",\n");
                fileOut.write("\"attributes\": {\"label\": \"children\" },\n");
                fileOut.write("\"directed\": \"true\",\n");
                fileOut.write("\"timestamp\": \"1\"}},\n");
                //
                sr.remove(top);
            }
        }catch (Exception e){
            System.out.println("create graph error "+e + " file ");
        }
    }

    public void run(String inPath, String outPath){
        try{
            vertexID = 0;
            edgeID = 0;
            top = 0;
            sr = new ArrayList<>();
            //
            fileOut  = new FileWriter(outPath);
            fileOut.write("[\n");
            //read white labels from file
            readWhiteLabel("listWhiteLabel.txt", whiteLabels);
            readRootLabel("listRootLabel.txt", rootLabels);

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
                        System.out.println(fileName);
                        //
                        File fXmlFile = new File(fileName);
                        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                        Document doc = dBuilder.parse(fXmlFile);
                        doc.getDocumentElement().normalize();
                        //filter root label here
                        NodeList nList = doc.getElementsByTagName("TypeDeclaration");
                        //
                        createASTGraph(nList.item(0));

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
            System.out.println("#input graphs: ");
        }catch (Exception e){
            System.out.println("convert AST error");
        }


    }
}
