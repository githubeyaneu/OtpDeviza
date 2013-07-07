package eu.eyan.devisa;

import static org.fest.assertions.Assertions.*;

import org.junit.Test;

import eu.eyan.devisa.OtpDevizaParser.Valuta;
import eu.eyan.devisa.OtpDevizaParser.ÉrtékTípus;

public class OtpDevizaParserTest {

	private static final int FELSŐ_HATÁR = 310;
	private static final int ALSÓ_HATÁR = 250;

	@Test
	public void testGetValue() {
		float eurKözép = OtpDevizaParser.getValue(Valuta.EUR, ÉrtékTípus.Közép);
		float eurEgység = OtpDevizaParser.getValue(Valuta.EUR, ÉrtékTípus.Egység);
		assertThat(eurKözép).isGreaterThan(ALSÓ_HATÁR).isLessThan(FELSŐ_HATÁR);
		assertThat(eurEgység).isEqualTo(1);
	}
	
	@Test
	public void testGetPage() {
		String page = OtpDevizaParser.getOtpDevizaOldalHtml();
		assertThat(page).isNotEmpty();
	}

	@Test
	public void testEmlékezetőÜzenet() {
		String page = OtpDevizaParser.getOtpDevizaOldalHtml();
		assertThat(page).isNotEmpty();
	}

}
