package at.ac.tuwien.infosys.viepepc.database.bootstrap.serverFarms;

import at.ac.tuwien.infosys.viepepc.library.entities.virtualmachine.ServerFarm;
import at.ac.tuwien.infosys.viepepc.library.entities.virtualmachine.VMType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by philippwaibel on 18/10/2016.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name="ServerFarms")
@XmlAccessorType(XmlAccessType.FIELD)
public class ServerFarms {

    @XmlElement(name = "ServerFarm")
    private List<ServerFarm> serverFarms = new ArrayList<>();

}
