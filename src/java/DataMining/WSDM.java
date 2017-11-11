package DataMining;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.POST;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import weka.associations.Apriori;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.rules.M5Rules;
import weka.classifiers.rules.OneR;
import weka.classifiers.trees.M5P;
import weka.core.Instance;
import weka.core.json.JSONNode;
import weka.core.json.JSONInstances;
import weka.core.Instances;
import weka.classifiers.trees.J48;
import weka.clusterers.Cobweb;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;

@Path("json")
public class WSDM {

    @Context
    private UriInfo context;

    public WSDM() {
    }

    @Path("/classification/")
    @POST
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response Classification(@FormParam("training") String jsonmodel, @FormParam("test") String jsonpred, @FormParam("class") String className, @FormParam("type") String type) throws IOException, Exception {

        if (jsonmodel == null || jsonpred == null || className == null) {
            return Response.ok("{\"message\": \"Mandatory parameters not reported\"}", MediaType.APPLICATION_JSON).build();
        } else {
            if (jsonmodel.isEmpty() || jsonpred.isEmpty() || className.isEmpty()) {
                return Response.ok("{\"message\": \"Mandatory parameters not reported\"}", MediaType.APPLICATION_JSON).build();
            }
        }

        Util util = new Util();

        jsonmodel = util.isArrayJSON(jsonmodel);
        jsonpred = util.isArrayJSON(jsonpred);

        JSONNode jsonNodeModel = new JSONNode();
        JSONNode jsonNodePred = new JSONNode();
        jsonNodePred = util.buildHeader(jsonmodel, jsonNodePred);
        jsonNodePred = util.buildData(jsonpred, jsonNodePred);
        jsonNodeModel = util.jeka(jsonmodel);

        int classIndex = -1;

        Instances model = JSONInstances.toInstances(jsonNodeModel);
        Instances listToPred = JSONInstances.toInstances(jsonNodePred);

        for (int contModelInst = 0; contModelInst < model.numInstances(); contModelInst++) {
            Instance Inst = model.get(contModelInst);
            for (int contModelAtrib = 0; contModelAtrib < Inst.numAttributes(); contModelAtrib++) {
                Attribute Att = Inst.attribute(contModelAtrib);
                if (Att.name().equals(className)) {
                    classIndex = contModelAtrib;
                }
            }
        }

        model.setClassIndex(classIndex);
        listToPred.setClassIndex(classIndex);

        Classifier classifier;
        
        if (type != null) {
            switch (type) {
                case "tree": {
                    classifier = new J48();
                }
                break;
                case "rules": {
                    classifier = new OneR();
                }
                break;
                case "bayes": {
                    classifier = new NaiveBayes();
                }
                break;
                case "lazy": {
                    classifier = new IBk();
                }
                break;
                default: {
                    classifier = new J48();
                }
            }
        } else {
            classifier = new J48();
            type = "tree" ;
        }

        classifier.buildClassifier(model);

        List<Instance> InstancesPred = new ArrayList<>();
        Instance Inst;
        int NumInstances = listToPred.numInstances();
        for (int cont = 0; cont < NumInstances; cont++) {
            Inst = listToPred.instance(cont);
            Inst.setMissing(Inst.classIndex());
            double Predict = classifier.classifyInstance(Inst);
            Inst.setClassValue(Predict);
            InstancesPred.add(Inst);
        }

        String json = util.toJSON(InstancesPred , classifier , model , type , true ) ;

        return Response.ok(json, MediaType.APPLICATION_JSON).build();

    }

    @Path("/classification/")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public Response ClassificationGet(@QueryParam("listmodel") String jsonmodel, @QueryParam("listtopred") String jsonpred, @QueryParam("classname") String className) throws IOException, Exception {
        String json = "{ \"status\":0, \"message\":\"The requires to this Web Service support only POST method\" }";
        return Response.ok(json, MediaType.APPLICATION_JSON).build();
    }

    @Path("/association/")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public Response Association(@FormParam("data") String json, @FormParam("support")  double support,  @FormParam("confidence") double confidence) throws IOException, Exception {
        if (json == null) {
            return Response.ok("{\"message\": \"Mandatory parameters not reported\"}", MediaType.APPLICATION_JSON).build();
        } else {
            if (json.isEmpty()) {
                return Response.ok("{\"message\": \"Mandatory parameters not reported\"}", MediaType.APPLICATION_JSON).build();
            }
        }
        
        

        Util util = new Util();
        JSONNode jsonArff = util.jekaForAssociation(json);
        Instances data = JSONInstances.toInstances(jsonArff);
        Apriori apriori = new Apriori();
        
        if( support != 0 ){
            apriori.setUpperBoundMinSupport(support);
        }
        
        if( confidence != 0 ){
            apriori.setMinMetric(confidence);
        }
        
        apriori.buildAssociations(data);
        String jsonResult;

        if (apriori.getNumRules() > 0) {
            jsonResult = apriori.toJSON();
        } else {
            jsonResult = "\"status\":0 , \"message\":\"No large itemsets and rules found!\"";
        }

        return Response.ok(jsonResult, MediaType.APPLICATION_JSON).build();
    }

    @Path("/association/")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public Response AssociationGet(@QueryParam("data") String json) throws IOException, Exception {
        String jsonReturn = "{ \"status\":0, \"message\":\"The requires to this Web Service support only POST method\" }";
        return Response.ok(jsonReturn, MediaType.APPLICATION_JSON).build();
    }

    @Path("/cluster/")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public Response Cluster( @FormParam("data") String json , @FormParam("numGroups") int numGroups  ) throws IOException, Exception {
        if (json == null) {
            return Response.ok("{\"message\": \"Mandatory parameters not reported\"}", MediaType.APPLICATION_JSON).build();
        } else {
            if (json.isEmpty()) {
                return Response.ok("{\"message\": \"Mandatory parameters not reported\"}", MediaType.APPLICATION_JSON).build();
            }
        }

        Util util = new Util();
        System.out.println( numGroups ) ;
        JSONNode jsonArff = util.jeka(json);
        

        Instances data = JSONInstances.toInstances(jsonArff);

        SimpleKMeans cluster = new SimpleKMeans();
        
        if( numGroups != 0 ){
            cluster.setNumClusters(numGroups); 
        } else {
            Cobweb cobweb = new Cobweb();
            cobweb.buildClusterer(data);
            cluster.setNumClusters(cobweb.numberOfClusters()); 
        }
        
        cluster.setDisplayStdDevs(true);
        
        cluster.buildClusterer(data);

        Instances ResultCentroid = cluster.getClusterCentroids();
        Instances STDDEVCentroid = cluster.getClusterStandardDevs();
        Instances Relation = new Instances(ResultCentroid, (ResultCentroid.numInstances() + STDDEVCentroid.numInstances()));

        for (int cont = 0; cont < ResultCentroid.numInstances(); cont++) {
            Instance Centroid = ResultCentroid.instance(cont);
            Instance STDDEV = STDDEVCentroid.instance(cont);
            Relation.add(Centroid);
            Relation.add(STDDEV);
        }

        String result = util.toJSONCluster(Relation);

        return Response.ok(result, MediaType.APPLICATION_JSON).build();
    }

    @Path("/cluster/")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public Response ClusterGet(@QueryParam("data") String json) throws IOException, Exception {
        String jsonReturn = "{ \"status\":0, \"message\":\"The requires to this Web Service support only POST method\" }";
        return Response.ok(jsonReturn, MediaType.APPLICATION_JSON).build();
    }

    @Path("/regression/")
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public Response Regression(@FormParam("training") String jsonmodel, @FormParam("test") String jsonpred, @FormParam("class") String className, @FormParam("type") String type) throws IOException, Exception {

        if (jsonmodel == null || jsonpred == null || className == null) {
            return Response.ok("{\"message\": \"Mandatory parameters not reported\"}", MediaType.APPLICATION_JSON).build();
        } else {
            if (jsonmodel.isEmpty() || jsonpred.isEmpty() || className.isEmpty()) {
                return Response.ok("{\"message\": \"Mandatory parameters not reported\"}", MediaType.APPLICATION_JSON).build();
            }
        }

        Util util = new Util();

        jsonmodel = util.isArrayJSON(jsonmodel);
        jsonpred = util.isArrayJSON(jsonpred);

        JSONNode jsonNodeModel = new JSONNode();
        JSONNode jsonNodePred = new JSONNode();
        jsonNodePred = util.buildHeader(jsonmodel, jsonNodePred);
        jsonNodePred = util.buildData(jsonpred, jsonNodePred);
        jsonNodeModel = util.jeka(jsonmodel);

        int classIndex = -1;

        Instances model = JSONInstances.toInstances(jsonNodeModel);
        Instances listToPred = JSONInstances.toInstances(jsonNodePred);

        for (int contModelInst = 0; contModelInst < model.numInstances(); contModelInst++) {
            Instance Inst = model.get(contModelInst);
            for (int contModelAtrib = 0; contModelAtrib < Inst.numAttributes(); contModelAtrib++) {
                Attribute Att = Inst.attribute(contModelAtrib);
                if (Att.name().equals(className)) {
                    classIndex = contModelAtrib;
                }
            }
        }

        model.setClassIndex(classIndex);

        listToPred.setClassIndex(classIndex);
        
        Classifier regression;
        
        if (type != null) {
            switch (type) {
                case "tree": {
                    regression = new M5P();
                }
                break;
                case "rules": {
                    regression = new M5Rules();
                }
                break;
                case "lazy": {
                    regression = new IBk();
                }
                break;
                default: {
                    regression = new M5P();
                }
            }
        } else {
            regression = new M5P();
            type = "tree" ;
        }

        regression.buildClassifier(model);

        List<Instance> InstancesPred = new ArrayList<>();
        Instance Inst;
        int NumInstances = listToPred.numInstances();
        for (int cont = 0; cont < NumInstances; cont++) {
            Inst = listToPred.instance(cont);
            Inst.setMissing(Inst.classIndex());
            double Predict = regression.classifyInstance(Inst);
            Inst.setClassValue(Predict);
            InstancesPred.add(Inst);
        }

//        String json = util.toJSON(InstancesPred);
        
        String json = util.toJSON(InstancesPred , regression , model , type , false ) ;

        return Response.ok(json, MediaType.APPLICATION_JSON).build();
    }

    @Path("/regression/")
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
    public Response RegressionGet(@QueryParam("listmodel") String jsonmodel, @QueryParam("listtopred") String jsonpred, @QueryParam("classname") String className) throws IOException, Exception {
        String json = "{ \"status\":0, \"message\":\"The requires to this Web Service support only POST method\" }";
        return Response.ok(json, MediaType.APPLICATION_JSON).build();
    }

}
