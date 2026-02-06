package com.project.abac.cloudsim;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;

import org.cloudbus.cloudsim.provisioners.*;


import java.util.*;

public class CloudSimManager {

    private static DatacenterBroker broker;

    public static void initCloudSim() {

        int numUsers = 1;
        Calendar calendar = Calendar.getInstance();
        boolean traceFlag = false;

        CloudSim.init(numUsers, calendar, traceFlag);

        Datacenter datacenter = createDatacenter("ABAC_DC");
        broker = createBroker();

        System.out.println("[CLOUDSIM] Initialized");
    }

    public static long executePartialDecryption(int groupCount) {

        // Cloudlet length proportional to number of attribute groups
        long cloudletLength = groupCount * 10000;

        Cloudlet cloudlet = new Cloudlet(
                0,
                cloudletLength,
                1,
                300,
                300,
                new UtilizationModelFull(),
                new UtilizationModelFull(),
                new UtilizationModelFull()
        );

        cloudlet.setUserId(broker.getId());

        Vm vm = new Vm(
                0,
                broker.getId(),
                1000,     // MIPS
                1,
                2048,     // RAM
                1000,
                10000,
                "Xen",
                new CloudletSchedulerTimeShared()
        );

        broker.submitVmList(Collections.singletonList(vm));
        broker.submitCloudletList(Collections.singletonList(cloudlet));

        CloudSim.startSimulation();
        CloudSim.stopSimulation();

        double execTime = cloudlet.getActualCPUTime();

        System.out.println("[CLOUDSIM] Partial decryption cloudlet executed");
        System.out.println("[CLOUDSIM] Cloud execution time: " + execTime + " ms");

        return (long) execTime;
    }

    private static Datacenter createDatacenter(String name) {

        List<Host> hostList = new ArrayList<>();

        List<Pe> peList = List.of(new Pe(0, new PeProvisionerSimple(2000)));

        Host host = new Host(
                0,
                new RamProvisionerSimple(8192),
                new BwProvisionerSimple(10000),
                1000000,
                peList,
                new VmSchedulerTimeShared(peList)
        );

        hostList.add(host);

        DatacenterCharacteristics characteristics =
                new DatacenterCharacteristics(
                        "x86",
                        "Linux",
                        "Xen",
                        hostList,
                        10.0,
                        3.0,
                        0.05,
                        0.1,
                        0.1
                );

        try {
            return new Datacenter(
                    name,
                    characteristics,
                    new VmAllocationPolicySimple(hostList),
                    new ArrayList<>(),
                    0
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static DatacenterBroker createBroker() {
        try {
            DatacenterBroker broker = new DatacenterBroker("ABAC_BROKER");
            return broker;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
