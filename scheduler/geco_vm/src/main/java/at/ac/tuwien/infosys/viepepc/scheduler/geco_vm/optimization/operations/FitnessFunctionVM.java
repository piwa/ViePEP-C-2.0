package at.ac.tuwien.infosys.viepepc.scheduler.geco_vm.optimization.operations;

import at.ac.tuwien.infosys.viepepc.database.inmemory.database.InMemoryCacheImpl;
import at.ac.tuwien.infosys.viepepc.library.entities.virtualmachine.ServerFarm;
import at.ac.tuwien.infosys.viepepc.library.entities.virtualmachine.VMType;
import at.ac.tuwien.infosys.viepepc.library.entities.workflow.ProcessStep;
import at.ac.tuwien.infosys.viepepc.scheduler.geco_vm.optimization.entities.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.uncommons.watchmaker.framework.FitnessEvaluator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
@SuppressWarnings("Duplicates")
public class FitnessFunctionVM implements FitnessEvaluator<Chromosome2> {

    @Autowired
    private InMemoryCacheImpl inMemoryCache;

//    @Value("${fitness.cost.cpu}")
//    private double cpuCost = 14; // dollar cost for 1 vCPU for 1 second
//    @Value("${fitness.cost.ram}")
//    private double ramCost = 3; // dollar cost for 1 GB for 1 second

    @Value("${fitness.leasing.cost.factor}")
    private double leasingCostFactor = 10;

    @Value("${company.turnover.cost}")
    private double turnoverCost;
    @Value("${gdpr.penalty.max}")
    private double maxPenalty;


    @Getter
    private double leasingCost = 0;
    @Getter
    private double penaltyCost = 0;

    @Setter
    private DateTime optimizationEndTime;

    @Override
    public double getFitness(Chromosome2 chromosome, List<? extends Chromosome2> list) {

        double leasingCost = 0;

        // calculate VM leasing cost

        Set<VirtualMachineSchedulingUnit> virtualMachineSchedulingUnits = new HashSet<>();
        for (ServiceTypeSchedulingUnit typeSchedulingUnit : chromosome.getFlattenChromosome()) {
            VirtualMachineSchedulingUnit machineSchedulingUnit = typeSchedulingUnit.getVirtualMachineSchedulingUnit();
            if (machineSchedulingUnit != null) {
                machineSchedulingUnit.getServiceTypeSchedulingUnits().add(typeSchedulingUnit);
                virtualMachineSchedulingUnits.add(machineSchedulingUnit);
            }
        }

//        Set<VirtualMachineSchedulingUnit> virtualMachineSchedulingUnits = chromosome.getFlattenChromosome().stream().map(ServiceTypeSchedulingUnit::getVirtualMachineSchedulingUnit).collect(Collectors.toSet());
        for (VirtualMachineSchedulingUnit virtualMachineSchedulingUnit : virtualMachineSchedulingUnits) {
            VMType vmType = virtualMachineSchedulingUnit.getVmType();
            Duration cloudResourceUsageDuration;
            if (virtualMachineSchedulingUnit.getCloudResourceUsageInterval().getStart().isBefore(optimizationEndTime)) {
                cloudResourceUsageDuration = new Duration(optimizationEndTime, virtualMachineSchedulingUnit.getCloudResourceUsageInterval().getEnd());
            } else {
                cloudResourceUsageDuration = new Duration(virtualMachineSchedulingUnit.getCloudResourceUsageInterval());
            }
            leasingCost = leasingCost + (vmType.getCores() * vmType.getCostsCPU() * cloudResourceUsageDuration.getStandardSeconds() + vmType.getRamPoints() / 1000 * vmType.getCostsRAM() * cloudResourceUsageDuration.getStandardSeconds()) * leasingCostFactor / 100;
        }

        this.leasingCost = leasingCost;


        // penalty cost
        double penaltyCost = 0;
        for (VirtualMachineSchedulingUnit virtualMachineSchedulingUnit : virtualMachineSchedulingUnits) {

            String vmProvider = virtualMachineSchedulingUnit.getVirtualMachineInstance().getVmType().getProvider();
            String vmCountry = virtualMachineSchedulingUnit.getVirtualMachineInstance().getVmType().getCountry();

            for (ServerFarm serverFarm : inMemoryCache.getServerFarms()) {

                for (ServiceTypeSchedulingUnit serviceTypeSchedulingUnit : virtualMachineSchedulingUnit.getServiceTypeSchedulingUnits()) {
                    for (Chromosome.Gene gene : serviceTypeSchedulingUnit.getGenes()) {
                        if (gene.getProcessStepSchedulingUnit().getProcessStep().isContainsPrivacyRelevantData()) {
                            if (serverFarm.getProvider().equals(vmProvider) && serverFarm.getLocation().equals(vmCountry) && !serverFarm.isEuCompliant()) {
                                if (turnoverCost * 0.04 < maxPenalty) {
                                    penaltyCost = turnoverCost * 0.04;
                                } else {
                                    penaltyCost = maxPenalty;
                                }
                            }
                        }
                    }
                }
            }
        }
        this.penaltyCost = penaltyCost;

        for (VirtualMachineSchedulingUnit alreadyUsedVirtualMachineSchedulingUnit : virtualMachineSchedulingUnits) {
            alreadyUsedVirtualMachineSchedulingUnit.getServiceTypeSchedulingUnits().clear();
        }
        return leasingCost + penaltyCost;
    }

    @Override
    public boolean isNatural() {
        return false;
    }

}
