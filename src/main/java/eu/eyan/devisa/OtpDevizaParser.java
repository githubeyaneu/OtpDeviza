package eu.eyan.devisa;

import static com.google.common.collect.Lists.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

import org.joda.time.DateTime;

public class OtpDevizaParser {

	public enum ÉrtékTípus {
		Egység, Közép, Valuta_vételi, Valuta_eladási, Csekk_vételi, Csekk_eladási, Deviza_vételi, Deviza_eladási;
		
		public int getNumber(){
			return newArrayList(ÉrtékTípus.values()).indexOf(this);
		}
	}

	public enum Valuta {
		AUD, BGN, CAD, CHF, CNY, CZK, DKK, EUR, GBP, HRK, JPY, NOK, PLN, RON, RSD, RUB, SEK, TRY, UAH, USD
	}

	public static final String PAGE = "https://www.otpbank.hu/portal/hu/Arfolyamok/OTP";
	private static String otpDevizaOldalHtml = null;
	private static DateTime utoljáraBeolvasva;

	public static float getValue(Valuta valuta, ÉrtékTípus értékTípus) {
		String tábla = getTáblaHtml(getOtpDevizaOldalHtml());
		String devizaAdatok = getDevizaAdatokHtml(tábla, valuta);
		devizaAdatok = devizaAdatok.replaceAll("\\s", "").replaceAll("<tdclass=\"num\">", "");
		String[] adatok = devizaAdatok.split("</td>");
		String adatSzöveg = adatok[értékTípus.getNumber()];
		return Float.parseFloat(adatSzöveg.replace(",", "."));
	}

	private static String getDevizaAdatokHtml(String tábla, Valuta valuta) {
		String adatokElejeKeresőkifejezés = valuta+"</b></td>";
		int adatokEleje = tábla.indexOf(adatokElejeKeresőkifejezés) + adatokElejeKeresőkifejezés.length();
		String adatokDevizaElejétől = tábla.substring(adatokEleje);
		int adatokVége = adatokDevizaElejétől.indexOf("</tr");
		String devizaAdatok = adatokDevizaElejétől.substring(0, adatokVége);
		return devizaAdatok;
	}

	private static String getTáblaHtml(String page) {
		int táblaEleje = page.indexOf("<table cellspacing=\"0\" cellpadding=\"0\" summary=\"\" class=\"appdata\" style=\"width: 721px; margin-left: 0;\">");
		String táblától = page.substring(táblaEleje);
		int táblaVége = táblától.indexOf("</table>");
		String tábla = táblától.substring(0, táblaVége);
		return tábla;
	}

	public static String getOtpDevizaOldalHtml() {
		if(otpDevizaOldalHtml == null || utoljáraBeolvasva.plusHours(1).isBeforeNow()){
			try {
				Scanner scanner = new Scanner(new URL(PAGE).openStream(), "UTF-8");
				try {
					otpDevizaOldalHtml = scanner.useDelimiter("\\A").next();
					utoljáraBeolvasva = new DateTime();
				} finally {
					scanner.close();
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return otpDevizaOldalHtml;
	}
}
