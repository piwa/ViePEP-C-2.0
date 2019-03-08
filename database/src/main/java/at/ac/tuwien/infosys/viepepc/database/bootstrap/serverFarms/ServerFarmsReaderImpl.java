package at.ac.tuwien.infosys.viepepc.database.bootstrap.serverFarms;

import at.ac.tuwien.infosys.viepepc.database.inmemory.database.InMemoryCacheImpl;
import at.ac.tuwien.infosys.viepepc.library.entities.virtualmachine.VMType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.util.Comparator;

/**
 * Created by philippwaibel on 18/10/2016.
 */
@Component
@Slf4j
public class ServerFarmsReaderImpl {

    @Autowired
    private InMemoryCacheImpl inMemoryCache;

    @Value("${server.farms.path}")
    private String serverFarmPath;

    public void readServerFarms() {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(ServerFarms.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            InputStream file = this.getClass().getResourceAsStream(serverFarmPath);
            ServerFarms serverFarms = (ServerFarms) jaxbUnmarshaller.unmarshal(file);

            inMemoryCache.getServerFarms().addAll(serverFarms.getServerFarms());

        } catch (JAXBException | NullPointerException e) {
            log.error("Exception", e);
        }

    }
}
