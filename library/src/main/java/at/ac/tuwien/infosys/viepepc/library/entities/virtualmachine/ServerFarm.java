package at.ac.tuwien.infosys.viepepc.library.entities.virtualmachine;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.io.Serializable;


@Entity
@Table(name = "server_farm")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class ServerFarm implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected Long tableId;

    @XmlElement
    private String provider;
    @XmlElement
    private String location;
    @XmlElement(name="eu_compliant")
    private boolean euCompliant;

}
