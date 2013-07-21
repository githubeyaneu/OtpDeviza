package eu.eyan.devisa;

import static eu.eyan.devisa.OtpDevizaParser.Valuta.*;
import static eu.eyan.devisa.OtpDevizaParser.ÉrtékTípus.*;
import static eu.eyan.logging.Logging.*;
import static java.util.TimeZone.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Scanner;
import java.util.TimeZone;

import javax.mail.MessagingException;

import org.joda.time.DateTime;

import eu.eyan.mail.SendMail;

public class OtpDevizaParser {

	private static final SimpleDateFormat DÁTUM_FORMÁTUM_NAP = new SimpleDateFormat("MMMM dd", new Locale("hu"));
	private static final SimpleDateFormat DÁTUM_FORMÁTUM_PERC = new SimpleDateFormat("yyyy.MMMM.dd HH:mm", new Locale("hu"));
	private static final String FT = " Ft";
	private static final String BR = System.lineSeparator();

	public enum ÉrtékTípus {
		Egység, Közép, Valuta_vételi, Valuta_eladási, Csekk_vételi, Csekk_eladási, Deviza_vételi, Deviza_eladási;
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
		String adatSzöveg = adatok[értékTípus.ordinal()];
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
	
	public static void main(String args[])
	{
		final String username = System.getenv("GMAIL_USER");
		final String password = System.getenv("GMAIL_PASS");
		final String from = System.getenv("GMAIL_FROM");
		final String to = System.getenv("GMAIL_TO");

		logInfo("Helyi időzóna: " + TimeZone.getDefault());
		logInfo("Helyi idő: " + new Date());
		Date mostBerlinben = Calendar.getInstance(getTimeZone("Europe/Berlin")).getTime();
		
		boolean sikerült = false;
		int próbálkozás = 0;
		int MAX_PRÓBÁLKOZÁS = 3;
		float euróEladási = -1;
		float euróVételi = -1;
		float euróKözép = -1;
		while (!sikerült && ++próbálkozás <= MAX_PRÓBÁLKOZÁS)
		{
			try {
				logInfo(próbálkozás + ". próbálkozás: ");
				euróKözép = getValue(EUR, Közép);
				euróVételi = getValue(EUR, Deviza_vételi);
				euróEladási = getValue(EUR, Deviza_eladási);
				logInfo(euróKözép + " " + euróVételi + " " + euróEladási);
				sikerült = true;
			} catch (Exception e) {
				logError("Sikertelen: ", e);
				otpDevizaOldalHtml = null;
			}
		}
		if(!sikerült) {
			throw new RuntimeException("Nem sikerült meghatározni az euróárfolyamot!");
		}
		
		String subject = EUR + " " + euróKözép + FT + " - " + DÁTUM_FORMÁTUM_NAP.format(mostBerlinben);
		String body    = new StringBuilder("Otp " + EUR + "" + BR + BR)
								   .append("Közép:          " + euróKözép + FT + BR + BR)
								   .append("Deviza_vételi:  " + euróVételi + FT + BR + BR)
								   .append("Deviza_eladási: " + euróEladási + FT + BR + BR)
								   .append(BR + BR)
								   .append(DÁTUM_FORMÁTUM_PERC.format(mostBerlinben))
								   .toString();
		try {
			logInfo("Email küldése:" + BR
					+ "  " + username + BR 
					+ "  " + password + BR 
					+ "  " + from + BR 
					+ "  " + to + BR 
					+ "  " + subject + BR 
					+ "  " + body);
			SendMail.send_Gmail_TLS(username, password, from, to, subject, body);
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}
}
