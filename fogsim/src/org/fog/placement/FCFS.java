package org.fog.placement;

import java.util.*;

import com.sun.source.doctree.SeeTree;
import org.apache.commons.math3.util.Pair;
import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.application.AppEdge;
import org.fog.application.AppModule;
import org.fog.application.Application;
import org.fog.application.selectivity.SelectivityModel;
import org.fog.entities.Actuator;
import org.fog.entities.FogDevice;
import org.fog.entities.Sensor;
import org.fog.entities.Tuple;
import org.fog.utils.Logger;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane;

public class FCFS extends ModulePlacement{

    protected ModuleMapping moduleMapping;
    protected List<Sensor> sensors;
    protected List<Actuator> actuators;
    protected Map<Integer, Double> currentCpuLoad;

    /**
     * Stores the current mapping of application modules to fog devices
     */
    protected Map<Integer, List<String>> currentModuleMap;
    protected Map<Integer, Map<String, Double>> currentModuleLoadMap;
    protected Map<Integer, Map<String, Integer>> currentModuleInstanceNum;
    protected Integer nbreArea;
    protected Integer nbresensors;


    public FCFS(List<FogDevice> fogDevices, List<Sensor> sensors, List<Actuator> actuators,
                Application application, ModuleMapping moduleMapping, int nbreArea, int nbresensors){
        this.setFogDevices(fogDevices);
        this.setApplication(application);
        this.setModuleMapping(moduleMapping);
        this.setModuleToDeviceMap(new HashMap<String, List<Integer>>());
        this.setDeviceToModuleMap(new HashMap<Integer, List<AppModule>>());
        setnbrArea(nbreArea);
        setnbrsensor(nbresensors);
        setSensors(sensors);
        setActuators(actuators);
        setCurrentCpuLoad(new HashMap<Integer, Double>());
        setCurrentModuleMap(new HashMap<Integer, List<String>>());
        setCurrentModuleLoadMap(new HashMap<Integer, Map<String, Double>>());
        setCurrentModuleInstanceNum(new HashMap<Integer, Map<String, Integer>>());
        for(FogDevice dev : getFogDevices()){
            getCurrentCpuLoad().put(dev.getId(), 0.0);
            getCurrentModuleLoadMap().put(dev.getId(), new HashMap<String, Double>());
            getCurrentModuleMap().put(dev.getId(), new ArrayList<String>());
            getCurrentModuleInstanceNum().put(dev.getId(), new HashMap<String, Integer>());
        }

        mapModules();
        setModuleInstanceCountMap(getCurrentModuleInstanceNum());
    }

    @Override
    protected void mapModules() {

        for(String deviceName : getModuleMapping().getModuleMapping().keySet()){
            for(String moduleName : getModuleMapping().getModuleMapping().get(deviceName)){
                int deviceId = CloudSim.getEntityId(deviceName);
                getCurrentModuleMap().get(deviceId).add(moduleName);
                getCurrentModuleLoadMap().get(deviceId).put(moduleName, 0.0);
                getCurrentModuleInstanceNum().get(deviceId).put(moduleName, 0);
            }
        }
/*
*
* in this phase we get all the modules with their cpuload
*
 */

        List<List<Integer>> leafToRootPaths = getLeafToRootPaths();
        Map<AppEdge,Double> rateEdge = edgeRate(leafToRootPaths.get(2));
        Map<AppModule,Double> cpu = cpuLoad(getApplication().getModules(), rateEdge);
        System.out.println(rateEdge.size());
Application app= getApplication();
        List<AppEdge> EdgeList=app.getEdges();
        Set<AppModule> placed_modules= new HashSet<AppModule>();
        Set<AppModule> nonPlacedModules=new HashSet<AppModule>();

                List<AppModule> listmodul = app.getModules();

        for(int deviceId : getCurrentModuleMap().keySet()) {
            if (!getCurrentModuleMap().get(deviceId).isEmpty()) {
                for(String md : getCurrentModuleMap().get(deviceId)){

             placed_modules.add(app.getModuleByName(md));   }
            }
        }
        for (AppModule md : listmodul)
        {
            if (!(placed_modules.contains(md)))
            {
                nonPlacedModules.add(md);
            }
        }
        System.out.print("message");
/*
*in this part we place modules in devices
*
 */
int nb= getNbArea();
List<FogDevice> routers= new ArrayList<FogDevice>();
for (FogDevice dev :getFogDevices())
{
    if(dev.getName().contains("d-"))
    routers.add(dev);
}

while(nb>0)
{   Set<AppModule> mod=nonPlacedModules;
while(!mod.isEmpty()){
    AppModule modToPlace= mod.iterator().next();
    placeMod(routers,modToPlace,cpu.get(modToPlace));
    mod.remove(modToPlace);}
nb--;
    //System.out.println("remove: "+ modToPlace.getName());
}

        for(int deviceId : getCurrentModuleMap().keySet()){
            for(String module : getCurrentModuleMap().get(deviceId)){
                createModuleInstanceOnDevice(getApplication().getModuleByName(module), getFogDeviceById(deviceId));
            }

        }
    }




    protected double getRateOfSensor(String sensorType){
        for(Sensor sensor : getSensors()){
            if(sensor.getTupleType().equals(sensorType))
                return 1/sensor.getTransmitDistribution().getMeanInterTransmitTime();
        }
        return 0;
    }
private Map<AppEdge, Double> edgeRate(List<Integer> path)
{
    List<String> placedModules = new ArrayList<String>();
    Map<AppEdge, Double> appEdgeToRate = new HashMap<AppEdge, Double>();

    /**
     * Periodic edges have a fixed periodicity of tuples, so setting the tuple rate beforehand
     */
    for(AppEdge edge : getApplication().getEdges()){
        if(edge.isPeriodic()){
            appEdgeToRate.put(edge, 1/edge.getPeriodicity());
        }
    }

    for(Integer deviceId : path){
        FogDevice device = getFogDeviceById(deviceId);
        Map<String, Integer> sensorsAssociated = getAssociatedSensors(device);
        Map<String, Integer> actuatorsAssociated = getAssociatedActuators(device);
        placedModules.addAll(sensorsAssociated.keySet()); // ADDING ALL SENSORS TO PLACED LIST
        placedModules.addAll(actuatorsAssociated.keySet()); // ADDING ALL ACTUATORS TO PLACED LIST

        /*
         * Setting the rates of application edges emanating from sensors
         */
        for(String sensor : sensorsAssociated.keySet()){
            for(AppEdge edge : getApplication().getEdges()){
                if(edge.getSource().equals(sensor)){
                    appEdgeToRate.put(edge, sensorsAssociated.get(sensor)*getRateOfSensor(sensor));
                }
            }
        }

        /*
         * Updating the AppEdge rates for the entire application based on knowledge so far
         */
        boolean changed = true;
        while(changed){		//Loop runs as long as some new information is added
            changed=false;
            Map<AppEdge, Double> rateMap = new HashMap<AppEdge, Double>(appEdgeToRate);
            for(AppEdge edge : rateMap.keySet()){
                AppModule destModule = getApplication().getModuleByName(edge.getDestination());
                if(destModule == null)continue;
                Map<Pair<String, String>, SelectivityModel> map = destModule.getSelectivityMap();
                for(Pair<String, String> pair : map.keySet()){
                    if(pair.getFirst().equals(edge.getTupleType())){
                        double outputRate = appEdgeToRate.get(edge)*map.get(pair).getMeanRate(); // getting mean rate from SelectivityModel
                        AppEdge outputEdge = getApplication().getEdgeMap().get(pair.getSecond());
                        if(!appEdgeToRate.containsKey(outputEdge) || appEdgeToRate.get(outputEdge)!=outputRate){
                            // if some new information is available
                            changed = true;
                        }
                        appEdgeToRate.put(outputEdge, outputRate);
                    }
                }
            }
        }}
        return appEdgeToRate;
}

    /**
     * in this part we get the cpu load of all the modules
     * @param Am
     * @param EdgeRate
     */
    private Map<AppModule, Double> cpuLoad(List<AppModule> Am,Map<AppEdge,Double> EdgeRate)
    {
        Map<AppModule, Double> CpuLoad = new HashMap<AppModule, Double>();
        for (AppModule module : Am){
            double totalCpuLoad = 0;
        for(AppEdge edge : getApplication().getEdges()){		// take all incoming edges
            if(edge.getDestination().equals(module.getName())){
                double rate = EdgeRate.get(edge);
                totalCpuLoad += rate*edge.getTupleCpuLength();
            }
        }
        CpuLoad.put(module,totalCpuLoad);
        }
        return CpuLoad;
    }

    /**
     * place modules in routers
     * @param routers
     * @param apM
     */
    private void placeMod(List<FogDevice>routers,AppModule apM,Double CpuLoad)
{
    for (FogDevice dev : routers)
    {
       System.out.println(dev.getName());
        System.out.println(dev.getHost().getTotalMips());
        if (CpuLoad+getCurrentCpuLoad().get(dev.getId())<dev.getHost().getTotalMips())
        {
            getCurrentCpuLoad().put(dev.getId(),CpuLoad+getCurrentCpuLoad().get(dev.getId()));
            if(!currentModuleMap.containsKey(dev.getId()))
                currentModuleMap.put(dev.getId(), new ArrayList<String>());
            currentModuleMap.get(dev.getId()).add(apM.getName());
            //getCurrentModuleInstanceNum().get(dev.getId()).put(apM.getName(), getCurrentModuleInstanceNum().get(dev.getId()).get(apM.getName())+1);
        }
        else
        {
           List<FogDevice>parent=new ArrayList<FogDevice>();
            for (FogDevice dev1 : routers)
            {
                if (!parent.contains(dev1))
                    parent.add(getDeviceById(dev1.getParentId()));
            }
            placeMod(parent,apM,CpuLoad);
            return;
        }

    }


}



    /**
     * Gets all sensors associated with fog-device <b>device</b>
     * @param device
     * @return map from sensor type to number of such sensors
     */
    private Map<String, Integer> getAssociatedSensors(FogDevice device) {
        Map<String, Integer> endpoints = new HashMap<String, Integer>();
        for(Sensor sensor : getSensors()){
            if(sensor.getGatewayDeviceId()==device.getId()){
                if(!endpoints.containsKey(sensor.getTupleType()))
                    endpoints.put(sensor.getTupleType(), 0);
                endpoints.put(sensor.getTupleType(), endpoints.get(sensor.getTupleType())+1);
            }
        }
        return endpoints;
    }

    /**
     * Gets all actuators associated with fog-device <b>device</b>
     * @param device
     * @return map from actuator type to number of such sensors
     */
    private Map<String, Integer> getAssociatedActuators(FogDevice device) {
        Map<String, Integer> endpoints = new HashMap<String, Integer>();
        for(Actuator actuator : getActuators()){
            if(actuator.getGatewayDeviceId()==device.getId()){
                if(!endpoints.containsKey(actuator.getActuatorType()))
                    endpoints.put(actuator.getActuatorType(), 0);
                endpoints.put(actuator.getActuatorType(), endpoints.get(actuator.getActuatorType())+1);
            }
        }
        return endpoints;
    }
    //lehna njibou thnaya lkoll
    @SuppressWarnings("serial")
    protected List<List<Integer>> getPaths(final int fogDeviceId){
        FogDevice device = (FogDevice)CloudSim.getEntity(fogDeviceId);
        if(device.getChildrenIds().size() == 0){
            final List<Integer> path =  (new ArrayList<Integer>(){{add(fogDeviceId);}});
            List<List<Integer>> paths = (new ArrayList<List<Integer>>(){{add(path);}});
            return paths;
        }
        List<List<Integer>> paths = new ArrayList<List<Integer>>();
        for(int childId : device.getChildrenIds()){
            List<List<Integer>> childPaths = getPaths(childId);
            for(List<Integer> childPath : childPaths)
                childPath.add(fogDeviceId);
            paths.addAll(childPaths);
        }
        return paths;
    }

    protected List<List<Integer>> getLeafToRootPaths(){
        FogDevice cloud=null;
        for(FogDevice device : getFogDevices()){
            if(device.getName().equals("cloud"))
                cloud = device;
        }
        return getPaths(cloud.getId());
    }

    public ModuleMapping getModuleMapping() {
        return moduleMapping;
    }

    public void setModuleMapping(ModuleMapping moduleMapping) {
        this.moduleMapping = moduleMapping;
    }

    public Map<Integer, List<String>> getCurrentModuleMap() {
        return currentModuleMap;
    }

    public void setCurrentModuleMap(Map<Integer, List<String>> currentModuleMap) {
        this.currentModuleMap = currentModuleMap;
    }

    public List<Sensor> getSensors() {
        return sensors;
    }

    public void setSensors(List<Sensor> sensors) {
        this.sensors = sensors;
    }

    public List<Actuator> getActuators() {
        return actuators;
    }

    public void setActuators(List<Actuator> actuators) {
        this.actuators = actuators;
    }

    public Map<Integer, Double> getCurrentCpuLoad() {
        return currentCpuLoad;
    }

    public void setCurrentCpuLoad(Map<Integer, Double> currentCpuLoad) {
        this.currentCpuLoad= currentCpuLoad;
    }

    public Map<Integer, Map<String, Double>> getCurrentModuleLoadMap() {
        return currentModuleLoadMap;
    }

    public void setCurrentModuleLoadMap(
            Map<Integer, Map<String, Double>> currentModuleLoadMap) {
        this.currentModuleLoadMap = currentModuleLoadMap;
    }

    public Map<Integer, Map<String, Integer>> getCurrentModuleInstanceNum() {
        return currentModuleInstanceNum;
    }

    public void setCurrentModuleInstanceNum(
            Map<Integer, Map<String, Integer>> currentModuleInstanceNum) {
        this.currentModuleInstanceNum = currentModuleInstanceNum;
    }
    public void setnbrArea(int nb)
    {
        this.nbreArea=nb;
    }
    public void setnbrsensor(int nb)
    {
        this.nbresensors=nb;
    }
    public int getNbArea()
    {
        return nbreArea;
    }
    public int getnbrsensor()
    {
        return nbresensors;
    }
}
