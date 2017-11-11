package DataMining;

import java.util.Set;
import javax.ws.rs.core.Application;

/*
 * @author LUCAS ANGELI GIMENES
 */
@javax.ws.rs.ApplicationPath("weka")
public class ApplicationConfig extends Application {

    @Override 
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new java.util.HashSet<>();
        addRestResourceClasses(resources);
        return resources;
    }

    private void addRestResourceClasses(Set<Class<?>> resources) {
        resources.add(DataMining.WSDM.class);
    }
    
}
