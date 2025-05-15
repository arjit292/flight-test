package com.example.flightfinder;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
public class FlightSearchServiceTest {

    @Autowired
    private FlightSearchService flightSearchService;

    @Autowired
    private FlightRepository flightRepository;

    @Test
    public void testATQtoBLR() {
        List<Map<String, Map<String, Integer>>> result = flightSearchService.findFastestPaths("ATQ", "BLR");

        assertThat(result).isNotEmpty();
        assertThat(result.get(0)).containsKey("ATQ_BLR");
        assertThat(result.get(1)).containsKeys("ATQ_DEL_BLR", "ATQ_BOM_BLR", "ATQ_CCU_BLR", "ATQ_PNQ_BLR");
    }

    @Test
    public void testIXCtoCOK() {
        List<Map<String, Map<String, Integer>>> result = flightSearchService.findFastestPaths("IXC", "COK");

        assertThat(result).isNotEmpty();
        assertThat(result.get(0)).containsKeys("IXC_DEL_COK", "IXC_BLR_COK", "IXC_BOM_COK", "IXC_CCU_COK", "IXC_MAA_COK");
    }

    @Test
    public void testIXCtoGAU() {
        List<Map<String, Map<String, Integer>>> result = flightSearchService.findFastestPaths("IXC", "GAU");

        assertThat(result).isNotEmpty();
        assertThat(result.get(0)).containsKey("IXC_GAU");
        assertThat(result.get(1)).containsKeys("IXC_DEL_GAU", "IXC_CCU_GAU", "IXC_BLR_GAU", "IXC_HYD_GAU");
    }
}