
package DataMining;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.json.JSONNode;


public class Util {

    public int isnumeric(String num) {

        try {
            double number = Double.parseDouble(num);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }
    
    public String accuracy( Classifier classifier , Instances data  ) throws Exception{
        Evaluation eval = new Evaluation( data ) ;
        eval.evaluateModel( classifier , data ) ;
        
        return  ( ( eval.correct( ) / data.numInstances( ) ) * 100 ) + "%" ; 
    }

    public String isArrayJSON(String json) {

        int isArray;
        try {
            JSONArray jArray = new JSONArray(json);
            isArray = 1;

        } catch (Exception e) {
            isArray = 0;
        }
        if (isArray == 0) {
            json = "[" + json + "]";
        }
        return json;
    }

    public JSONNode jeka(String json) {
        int equalLabel = 0;
        int firstObject = 0;
        int objectKeys = 0;
        Util util = new Util();
        HashMap<String, String> mapObjects = new HashMap< String, String>();
        HashMap<Integer, String> mapAtributes = new HashMap<Integer, String>();
        HashMap<String, Integer> mapType = new HashMap<String, Integer>();
        HashMap<Integer, String> mapValues = new HashMap<Integer, String>();
        HashMap<String, ArrayList<String>> mapLabel = new HashMap<String, ArrayList<String>>();

        ArrayList<String> listLabel = new ArrayList();

        JSONObject jObject = new JSONObject();
        JSONArray jArray = new JSONArray(json);
        JSONArray jArrayNames = new JSONArray();
        int numObjects = jArray.length();
        int counterLabel = 2;
        int numAtributes = 0;
        int contAtributes = 0;
        for (int contObj = 0; contObj < numObjects; contObj++) {

            jObject = jArray.getJSONObject(contObj);
            jArrayNames = jObject.names();

            for (int contName = 0; contName < jArrayNames.length(); contName++) {
                String key = (String) jArrayNames.get(contName);
                String value = "" + jObject.get(key);
                mapObjects.put(key, value);

            }

            for (String atributeName : mapObjects.keySet()) {
                equalLabel = 0;
                contAtributes++;
                String atributeValue = mapObjects.get(atributeName);
                mapAtributes.put(contAtributes, atributeName);
                mapValues.put(contAtributes, atributeValue);
                if (firstObject == 0) {
                    objectKeys++;
                }

                int isnumeric = util.isnumeric(atributeValue);

                if (isnumeric == 1) {
                    mapType.put(atributeName, 1);
                } else {
                    mapType.put(atributeName, 0);
                }

                if (firstObject == 0 && mapType.get(atributeName) == 0) {
                    listLabel.add(atributeValue);
                    ArrayList aux = new ArrayList(listLabel);
                    mapLabel.put(atributeName, aux);
                    numAtributes++;
                    listLabel.clear();
                } else {
                    if (mapType.get(atributeName) == 0) {

                        for (String NameAtribute : mapLabel.keySet()) {
                            if (NameAtribute.equals(atributeName)) {
                                listLabel = mapLabel.get(atributeName);

                                if (!listLabel.contains(atributeValue)) {
                                    listLabel.add(atributeValue);
                                    ArrayList aux = new ArrayList(listLabel);
                                    mapLabel.put(atributeName, aux);
                                }
                            }
                        }
                    }
                }
            }
            firstObject++;
        }

        JSONNode nodeAll = new JSONNode();
        JSONNode nodeHeader = nodeAll.addObject("header");
        nodeHeader.addPrimitive("relation", "relation");
        JSONNode nodeHeaderAttributes = nodeHeader.addArray("attributes");

        JSONNode nodeHeaderAttributesObj[] = new JSONNode[mapType.size()];
        JSONNode nodeHeaderAttributesObjLabel[] = new JSONNode[mapType.size()];

        for (String nameAtribute : mapType.keySet()) {
            String Type = new String();
            int cont = 0;
            if (mapType.get(nameAtribute) == 0) {
                Type = "nominal";
            } else {
                Type = "numeric";
            }
            nodeHeaderAttributesObj[cont] = nodeHeaderAttributes.addObjectArrayElement();
            nodeHeaderAttributesObj[cont].addPrimitive("name", nameAtribute);
            nodeHeaderAttributesObj[cont].addPrimitive("type", Type);
            nodeHeaderAttributesObj[cont].addPrimitive("clase", Boolean.FALSE);
            nodeHeaderAttributesObj[cont].addPrimitive("weight", new Double(1.0));
            if (mapType.get(nameAtribute) == 0) {
                for (String nameAtributeLabel : mapLabel.keySet()) {
                    if (nameAtributeLabel.equals(nameAtribute)) {
                        nodeHeaderAttributesObjLabel[cont] = nodeHeaderAttributesObj[cont].addArray("labels");
                        listLabel = mapLabel.get(nameAtribute);
                        for (int contLabel = 0; contLabel < listLabel.size(); contLabel++) {
                            nodeHeaderAttributesObjLabel[cont].addArrayElement(listLabel.get(contLabel));
                        }
                    }
                }
            }
            cont++;
        }

        JSONNode nodeData = nodeAll.addArray("data");
        JSONNode nodeDataInst[] = new JSONNode[jArray.length()];
        JSONNode nodeDataInstValues[] = new JSONNode[jArray.length()];
        int contValue = 0;
        int contData = 0;
        int contValues = 1;

        for (contData = 0; contData <= (jArray.length() - 1); contData++) {
            nodeDataInst[contData] = nodeData.addObjectArrayElement();
            nodeDataInst[contData].addPrimitive("sparse", Boolean.FALSE);
            nodeDataInst[contData].addPrimitive("weight", new Double(1.0));
            nodeDataInstValues[contData] = nodeDataInst[contData].addArray("values");
            for (contValue = 0; contValue < objectKeys; contValue++) {
                try {
                    double number = Double.parseDouble(mapValues.get(contValues));
                    nodeDataInstValues[contData].addArrayElement(number);
                } catch (Exception e) {
                    nodeDataInstValues[contData].addArrayElement(mapValues.get(contValues));
                }
                contValues++;
            }

        }
        return nodeAll;
    }

    public JSONNode buildHeader(String json, JSONNode nodeAll) {
        int equalLabel = 0;
        int firstObject = 0;
        int objectKeys = 0;
        Util util = new Util();
        HashMap<String, String> mapObjects = new HashMap< String, String>();
        HashMap<Integer, String> mapAtributes = new HashMap<Integer, String>();
        HashMap<String, Integer> mapType = new HashMap<String, Integer>();
        HashMap<Integer, String> mapValues = new HashMap<Integer, String>();
        HashMap<String, ArrayList<String>> mapLabel = new HashMap<String, ArrayList<String>>();

        ArrayList<String> listLabel = new ArrayList();

        JSONObject jObject = new JSONObject();
        JSONArray jArray = new JSONArray(json);
        JSONArray jArrayNames = new JSONArray();
        int numObjects = jArray.length();
        int counterLabel = 2;
        int numAtributes = 0;
        int contAtributes = 0;
        for (int contObj = 0; contObj < numObjects; contObj++) {

            jObject = jArray.getJSONObject(contObj);

            jArrayNames = jObject.names();

            for (int contName = 0; contName < jArrayNames.length(); contName++) {
                String key = (String) jArrayNames.get(contName);
                String value = "" + jObject.get(key);
                mapObjects.put(key, value);

            }

            for (String atributeName : mapObjects.keySet()) {
                equalLabel = 0;
                contAtributes++;
                String atributeValue = mapObjects.get(atributeName);
                mapAtributes.put(contAtributes, atributeName);
                mapValues.put(contAtributes, atributeValue);
                if (firstObject == 0) {
                    objectKeys++;
                }

                int isnumeric = util.isnumeric(atributeValue);

                if (isnumeric == 1) {
                    mapType.put(atributeName, 1);
                } else {
                    mapType.put(atributeName, 0);
                }

                if (firstObject == 0 && mapType.get(atributeName) == 0) {
                    listLabel.add(atributeValue);
                    ArrayList aux = new ArrayList(listLabel);
                    mapLabel.put(atributeName, aux);
                    numAtributes++;
                    listLabel.clear();
                } else {
                    if (mapType.get(atributeName) == 0) {

                        for (String NameAtribute : mapLabel.keySet()) {
                            if (NameAtribute.equals(atributeName)) {
                                listLabel = mapLabel.get(atributeName);

                                if (!listLabel.contains(atributeValue)) {
                                    listLabel.add(atributeValue);
                                    ArrayList aux = new ArrayList(listLabel);
                                    mapLabel.put(atributeName, aux);
                                }
                            }
                        }
                    }
                }
            }
            firstObject++;
        }

        JSONNode nodeHeader = nodeAll.addObject("header");
        nodeHeader.addPrimitive("relation", "relation");
        JSONNode nodeHeaderAttributes = nodeHeader.addArray("attributes");

        JSONNode nodeHeaderAttributesObj[] = new JSONNode[mapType.size()];
        JSONNode nodeHeaderAttributesObjLabel[] = new JSONNode[mapType.size()];

        for (String nameAtribute : mapType.keySet()) {
            String Type = new String();
            int cont = 0;
            if (mapType.get(nameAtribute) == 0) {
                Type = "nominal";
            } else {
                Type = "numeric";
            }
            nodeHeaderAttributesObj[cont] = nodeHeaderAttributes.addObjectArrayElement();
            nodeHeaderAttributesObj[cont].addPrimitive("name", nameAtribute);
            nodeHeaderAttributesObj[cont].addPrimitive("type", Type);
            nodeHeaderAttributesObj[cont].addPrimitive("clase", Boolean.FALSE);
            nodeHeaderAttributesObj[cont].addPrimitive("weight", new Double(1.0));
            if (mapType.get(nameAtribute) == 0) {
                for (String nameAtributeLabel : mapLabel.keySet()) {
                    if (nameAtributeLabel.equals(nameAtribute)) {
                        nodeHeaderAttributesObjLabel[cont] = nodeHeaderAttributesObj[cont].addArray("labels");
                        listLabel = mapLabel.get(nameAtribute);
                        for (int contLabel = 0; contLabel < listLabel.size(); contLabel++) {
                            nodeHeaderAttributesObjLabel[cont].addArrayElement(listLabel.get(contLabel));
                        }
                    }
                }
            }
            cont++;
        }
        return nodeAll;
    }

    public JSONNode buildData(String json, JSONNode nodeAll) {
        int equalLabel = 0;
        int firstObject = 0;
        int objectKeys = 0;
        Util util = new Util();
        HashMap<String, String> mapObjects = new HashMap< String, String>();
        HashMap<Integer, String> mapAtributes = new HashMap<Integer, String>();
        HashMap<String, Integer> mapType = new HashMap<String, Integer>();
        HashMap<Integer, String> mapValues = new HashMap<Integer, String>();
        HashMap<String, ArrayList<String>> mapLabel = new HashMap<String, ArrayList<String>>();

        ArrayList<String> listLabel = new ArrayList();

        JSONObject jObject = new JSONObject();
        JSONArray jArray = new JSONArray(json);
        JSONArray jArrayNames = new JSONArray();
        int numObjects = jArray.length();
        int counterLabel = 2;
        int numAtributes = 0;
        int contAtributes = 0;
        for (int contObj = 0; contObj < numObjects; contObj++) {

            jObject = jArray.getJSONObject(contObj);
            jArrayNames = jObject.names();

            for (int contName = 0; contName < jArrayNames.length(); contName++) {
                String key = (String) jArrayNames.get(contName);
                String value = "" + jObject.get(key);
                mapObjects.put(key, value);

            }

            for (String atributeName : mapObjects.keySet()) {
                equalLabel = 0;
                contAtributes++;
                String atributeValue = mapObjects.get(atributeName);
                mapAtributes.put(contAtributes, atributeName);
                mapValues.put(contAtributes, atributeValue);
                if (firstObject == 0) {
                    objectKeys++;
                }

                int isnumeric = util.isnumeric(atributeValue);

                if (isnumeric == 1) {
                    mapType.put(atributeName, 1);
                } else {
                    mapType.put(atributeName, 0);
                }

                if (firstObject == 0 && mapType.get(atributeName) == 0) {
                    listLabel.add(atributeValue);
                    ArrayList aux = new ArrayList(listLabel);
                    mapLabel.put(atributeName, aux);
                    numAtributes++;
                    listLabel.clear();
                } else {
                    if (mapType.get(atributeName) == 0) {

                        for (String NameAtribute : mapLabel.keySet()) {
                            if (NameAtribute.equals(atributeName)) {
                                listLabel = mapLabel.get(atributeName);

                                if (!listLabel.contains(atributeValue)) {
                                    listLabel.add(atributeValue);
                                    ArrayList aux = new ArrayList(listLabel);
                                    mapLabel.put(atributeName, aux);
                                }
                            }
                        }
                    }
                }
            }
            firstObject++;
        }

        JSONNode nodeData = nodeAll.addArray("data");
        JSONNode nodeDataInst[] = new JSONNode[jArray.length()];
        JSONNode nodeDataInstValues[] = new JSONNode[jArray.length()];
        int contValue = 0;
        int contData = 0;
        int contValues = 1;

        for (contData = 0; contData <= (jArray.length() - 1); contData++) {
            nodeDataInst[contData] = nodeData.addObjectArrayElement();
            nodeDataInst[contData].addPrimitive("sparse", Boolean.FALSE);
            nodeDataInst[contData].addPrimitive("weight", new Double(1.0));
            nodeDataInstValues[contData] = nodeDataInst[contData].addArray("values");
            for (contValue = 0; contValue < objectKeys; contValue++) {
                try {
                    double number = Double.parseDouble(mapValues.get(contValues));
                    nodeDataInstValues[contData].addArrayElement(number);
                } catch (Exception e) {
                    nodeDataInstValues[contData].addArrayElement(mapValues.get(contValues));
                }
                contValues++;
            }

        }

        return nodeAll;
    }

    public String toJSON(List<Instance> InstancesPred) {

        JSONArray result = new JSONArray();
        JSONObject[] array = new JSONObject[InstancesPred.size()];
        
        for (int cont = 0; cont < InstancesPred.size(); cont++) {
            array[cont] = new JSONObject();
            Instance obj = InstancesPred.get(cont);
            
            for (int cont2 = 0; cont2 < obj.numAttributes(); cont2++) {
                Attribute atributo = obj.attribute(cont2);

                if (atributo.isNominal()) {
                    int indexValue = (int) obj.value(cont2); 
                    array[cont].putOnce(atributo.name(), atributo.value(indexValue));
                } else {
                    array[cont].put(atributo.name(), obj.value(cont2));
                }
            }
             
            result.put(array[cont]);
        }
        return result.toString();
    }
    
     public String toJSON(List<Instance> InstancesPred , Classifier classifier , Instances data , String type , boolean show ) throws Exception {

        JSONArray result = new JSONArray();
        JSONObject[] array = new JSONObject[InstancesPred.size()];
        
        JSONObject accuracy = new JSONObject( ) ;
        if( show ) {
            accuracy.put( "accuracy" , this.accuracy( classifier, data ) ) ;
        }
        accuracy.put( "algoritm" , classifier.getClass( ).getName() ) ;
        accuracy.put( "method" , type ) ;
        
        result.put( accuracy ) ;
        
        for (int cont = 0; cont < InstancesPred.size(); cont++) {
            array[cont] = new JSONObject();
            Instance obj = InstancesPred.get(cont);
            
            for (int cont2 = 0; cont2 < obj.numAttributes(); cont2++) {
                Attribute atributo = obj.attribute(cont2);

                if (atributo.isNominal()) {
                    int indexValue = (int) obj.value(cont2); 
                    array[cont].putOnce(atributo.name(), atributo.value(indexValue));
                } else {
                    array[cont].put(atributo.name(), obj.value(cont2));
                }
            }
             
            result.put(array[cont]);
        }
        return result.toString();
    }

    public JSONNode jekaForAssociation(String json) {

        JSONArray list = new JSONArray(json);
        JSONArray itemset = new JSONArray();
        ArrayList<String> itens = new ArrayList<String>();
        ArrayList<String> compItens = new ArrayList<String>();

        for (int countItemSet = 0; countItemSet < list.length(); countItemSet++) {
            itemset = list.getJSONArray(countItemSet);
            for (int countItem = 0; countItem < itemset.length(); countItem++) {
                if (!itens.contains(itemset.get(countItem))) {
                    itens.add((String) itemset.get(countItem));
                }
            }
        }

        JSONNode nodeAll = new JSONNode();
        JSONNode nodeHeader = nodeAll.addObject("header");
        nodeHeader.addPrimitive("relation", "relation");
        JSONNode nodeHeaderAttributes = nodeHeader.addArray("attributes");

        JSONNode nodeHeaderAttributesObj[] = new JSONNode[itens.size()];
        JSONNode nodeHeaderAttributesObjLabel[] = new JSONNode[itens.size()];

        for (int countItem = 0; countItem < itens.size(); countItem++) {
            nodeHeaderAttributesObj[countItem] = nodeHeaderAttributes.addObjectArrayElement();
            nodeHeaderAttributesObj[countItem].addPrimitive("name", itens.get(countItem));
            nodeHeaderAttributesObj[countItem].addPrimitive("type", "nominal");
            nodeHeaderAttributesObj[countItem].addPrimitive("clase", Boolean.FALSE);
            nodeHeaderAttributesObj[countItem].addPrimitive("weight", new Double(1.0));
            nodeHeaderAttributesObjLabel[countItem] = nodeHeaderAttributesObj[countItem].addArray("labels");
            nodeHeaderAttributesObjLabel[countItem].addArrayElement("t");
        }

        JSONNode nodeData = nodeAll.addArray("data");
        JSONNode nodeDataInst[] = new JSONNode[list.length()];
        JSONNode nodeDataInstItens[] = new JSONNode[list.length()];

        for (int countItemSet = 0; countItemSet < list.length(); countItemSet++) {
            nodeDataInst[countItemSet] = nodeData.addObjectArrayElement();
            nodeDataInst[countItemSet].addPrimitive("sparse", Boolean.FALSE);
            nodeDataInst[countItemSet].addPrimitive("weight", new Double(1.0));
            nodeDataInstItens[countItemSet] = nodeDataInst[countItemSet].addArray("values");
            itemset = list.getJSONArray(countItemSet);
            for (int countItem = 0; countItem < itemset.length(); countItem++) {
                compItens.add((String) itemset.get(countItem));
            }
            for (int countItem = 0; countItem < itens.size(); countItem++) {

                if (compItens.contains(itens.get(countItem))) {
                    nodeDataInstItens[countItemSet].addArrayElement("t");
                } else {
                    nodeDataInstItens[countItemSet].addArrayElement("?");
                }
            }
            compItens.clear();
        }
        return nodeAll;
    }

    public String toJSONCluster(Instances Relation) {
        
        JSONArray result = new JSONArray();
        JSONObject[] array  = new JSONObject[Relation.size()];
        JSONObject[] groups = new JSONObject[Relation.size()/2] ;
                
        for (int cont = 0; cont < Relation.size(); cont = cont + 2 ) {
            array[cont]   = new JSONObject();
            array[cont+1] = new JSONObject();
            
            groups[cont/2] = new  JSONObject() ;
            
            Instance centroid = Relation.get(cont);
            Instance std      = Relation.get(cont+1);
            
            for (int cont2 = 0; cont2 < centroid.numAttributes(); cont2++) {
                Attribute atributo = centroid.attribute(cont2);

                if (atributo.isNominal()) {
                    int indexValue = (int) centroid.value(cont2);
                    array[cont].putOnce(atributo.name(), atributo.value(indexValue));
                } else {
                    array[cont].put(atributo.name(), centroid.value(cont2));
                }
            }
            
           groups[cont/2].putOnce("CENTROID"+(cont / 2 + 1), array[cont]) ;
            
            for (int cont2 = 0; cont2 < std.numAttributes(); cont2++) {
                Attribute atributo = std.attribute(cont2);

                if (atributo.isNominal()) {
                    int indexValue = (int) std.value(cont2);
                    array[cont+1].putOnce(atributo.name(), atributo.value(indexValue));
                } else {
                    array[cont+1].put(atributo.name(), std.value(cont2));
                }
            }
            
            groups[cont/2].putOnce("STD"+(cont / 2 + 1), array[cont+1]) ;
            
            
            result.put(groups[cont/2]);
            
        }
        
        return result.toString();
    }
}
